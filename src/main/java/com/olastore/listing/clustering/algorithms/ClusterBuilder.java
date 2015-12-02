package com.olastore.listing.clustering.algorithms;

import com.olastore.listing.clustering.clients.ESClient;
import com.olastore.listing.clustering.redis.RedisClient;
import com.olastore.listing.clustering.redis.RedisClientOperation;
import com.olastore.listing.clustering.redis.RedisClientOperationImpl;
import com.olastore.listing.clustering.utils.ConfigReader;
import com.olastore.listing.clustering.utils.GeoHashUtil;
import com.olastore.listing.clustering.lib.models.ClusterPoint;
import com.olastore.listing.clustering.utils.Util;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.FileEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to create clusters. It will have all the utility Objects.
 */
public class ClusterBuilder {

  public static ESClient esClient;
  private Map esConfig;
  private Map clustersConfig;
  private Map redisConfig;
  private static RedisClientOperation redisClientOperation =null;

  public static ConcurrentHashMap<String, ClusterPoint> clusterPoints = new ConcurrentHashMap<>();
  public static List<String> pushedClusters = Collections.synchronizedList(new ArrayList<String>());
  public static ConcurrentHashMap<String, Double> clusterRankMap = new ConcurrentHashMap<>();
  public static Set<String> popularProdSet = new HashSet<>();
  public static AtomicInteger bulkDocCount = new AtomicInteger(0);
  public static StringBuilder bulkDoc = new StringBuilder();
  public static Logger logger = LoggerFactory.getLogger(ClusterBuilder.class);
  public static ConcurrentHashMap<String, String> storeStatusMap = new ConcurrentHashMap<>();
  public static HashMap<String,String> cityStoreRadiusMap = new HashMap<>();

  public ClusterBuilder(String env, ConfigReader esConfigReader, ConfigReader clustersConfigReader, ConfigReader redisConfigReader) throws FileNotFoundException {
    String esHostKey = "es_host_" + env;
    this.esConfig = esConfigReader.readAllValues();
    this.clustersConfig = clustersConfigReader.readAllValues();
    this.redisConfig = redisConfigReader.readAllValues();
    RedisClient redisClient = new RedisClient(env);
    this.redisClientOperation = new RedisClientOperationImpl(redisClient.getResource(),redisConfig);
    this.esClient = new ESClient((String) esConfig.get(esHostKey));
  }


  private boolean createClusteringIndices() {
    try {
      File file = new File("tmpFile");
      FileUtils.copyInputStreamToFile(
          getClass().getClassLoader().getResourceAsStream((String) esConfig.get("geo_mappings_file_path")),
          file);
      JSONObject create_geo_response = esClient.createIndex((String) esConfig.get("geo_hash_index_name"),
          new FileEntity(file));
      if(create_geo_response==null) {
        logger.error("Exception in creating indices");
        return false;
      };
      logger.info("Creating geo mappings #{}", create_geo_response.toString());

      FileUtils.copyInputStreamToFile(getClass().getClassLoader()
          .getResourceAsStream((String) esConfig.get("cluster_mappings_file_path")), file);
      JSONObject create_cluster_response = esClient.createIndex((String) esConfig.get("clusters_index_name"),
          new FileEntity(file));
      logger.info("Creating cluster mappings #{}", create_cluster_response.toString());
      file.delete();
      return true;
    } catch (Exception e) {
      logger.error("Exception in create/delete indices #{}", e);
    }
    return false;
  }

  private void changeAliases() throws URISyntaxException {

    String clusterIndex = (String) esConfig.get("clusters_index_name");
    String geoHashIndex = (String)esConfig.get("geo_hash_index_name");
    String oneDayBackClustersIndex = Util.getIndexCreatedNdaysBack(clusterIndex,1);
    String twoDaysBackClustersIndex = Util.getIndexCreatedNdaysBack(clusterIndex,2);
    String oneDayBackgeoHashIndex = Util.getIndexCreatedNdaysBack(geoHashIndex,1);
    String twoDaysBackgeoHashIndex = Util.getIndexCreatedNdaysBack(geoHashIndex,2);
    String clustersAlias = (String)esConfig.get("clusters_alias");
    String geohashAlias = (String) esConfig.get("geohash_alias");

    String aliasAddData = "{\"actions\":[" +
        "{\"add\":{\"index\":\""+clusterIndex+"\",\"alias\":\""+clustersAlias+"\"}}," +
        "{\"add\":{\"index\":\""+geoHashIndex+"\",\"alias\":\""+geohashAlias+"\"}}]}";
    String aliasDeleteData = "{\"actions\":[" +
        "{\"remove\":{\"index\":\""+oneDayBackClustersIndex+"\",\"alias\":\""+clustersAlias+"\"}}," +
        "{\"remove\":{\"index\":\""+oneDayBackgeoHashIndex+"\",\"alias\":\""+geohashAlias+"\"}}]}";
    JSONObject result = esClient.changeAliases(aliasAddData);
    if (result==null) {
      logger.error("Failed to add aliases ");
      return;
    }
    logger.info("Aliases Created Successfully"+ result);
    result = esClient.changeAliases(aliasDeleteData);
    if(result==null) logger.error("Aliases deletion failed");
    logger.info("Aliases deleted " +result);
    esClient.deleteIndex(twoDaysBackClustersIndex + "," + twoDaysBackgeoHashIndex);
  }

  private Set<String> initializePopularProductSet() {
    Set<String> productIdSet = new HashSet<>();
    FileReader fileReader = null;
    BufferedReader bufferedReader = null;
    try {
      File file = new File("tmpFile");
      FileUtils.copyInputStreamToFile(getClass().getClassLoader()
          .getResourceAsStream((String) clustersConfig.get("popular_products_file_path")), file);
      fileReader = new FileReader(file);
      bufferedReader = new BufferedReader(fileReader);
      String line = bufferedReader.readLine();
      while ((line = bufferedReader.readLine()) != null) {
        productIdSet.add(line);
      }
      file.delete();
    } catch (Exception e) {
      logger.error("Exception happened!{}", e);
    } finally {
      try {
        if (fileReader != null)
          fileReader.close();
        if (bufferedReader != null)
          bufferedReader.close();
      } catch (Exception e) {
        logger.error("Exception happened!{}", e);
      }
    }
    return productIdSet;
  }

  public void createClusters(String[] cities) throws Exception {
    // generate popular products
    popularProdSet = initializePopularProductSet();
    if (popularProdSet.size() == 0) {
      logger.error("Popular products is zero. Stopping now");
      return;
    }
    logger.info("Popular items reading completed. Total number of popular products are " + popularProdSet.size());

    //create cluster indices
    esConfig = com.olastore.listing.clustering.utils.Util.setClusterIndexes(esConfig, "geo_hash_index_name",
        "clusters_index_name");
    boolean isIndicesCreated = createClusteringIndices();
    if(!isIndicesCreated) {
      logger.error("Error in creating clusters Indices. Stopping now");
      return;
    }

    //creating clusters
    for(int i=0;i<cities.length;i++){
      if(i==0) esConfig = com.olastore.listing.clustering.utils.Util.setListingIndexNameForCity(esConfig, "listing_index_name",
          cities[i]);
      else  esConfig = com.olastore.listing.clustering.utils.Util.updateListingIndexNameForCity(esConfig, "listing_index_name",
          cities[i-1],cities[i]);
      createClustersForCity(cities[i]);
    }
    changeAliases();
  }

  private void generateStoreRadiusCombinationsForCity(String city) {
    String combination = redisClientOperation.getParamsForCity(city);
    if(combination==null || combination.isEmpty()) {
      cityStoreRadiusMap.put(city,"NA");
    }else {
      cityStoreRadiusMap.put(city,combination);
    }
  }

  private String getRadiusStoreCombination(String city, String geoHash) {
    String geoComb = redisClientOperation.getParamsForGeoHash(geoHash);
    if(geoComb==null || geoComb.isEmpty()) {
      if(cityStoreRadiusMap.get(city)==null) {
        generateStoreRadiusCombinationsForCity(city);
      }
      geoComb = cityStoreRadiusMap.get(city);
      if(geoComb.contentEquals("NA")) geoComb = (String)clustersConfig.get("default_store_radius_combination");
    }
    return geoComb;
  }

  public void createClustersForCity(String city ) throws Exception {
    GeoHashUtil geoHashUtil = new GeoHashUtil();
    List<String> geoHashList = geoHashUtil.getGeoHashesForArea(city, this.clustersConfig);

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (String geoHash : geoHashList) {
      String combination = getRadiusStoreCombination(city,geoHash);
      String[] params = combination.split("-");
      executorService.submit(new ClusterWorker(geoHash, esConfig, clustersConfig,Integer.parseInt(params[0]),Integer.parseInt(params[1])));
    }
    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.DAYS);
    if (!bulkDoc.toString().isEmpty()) {
      esClient.pushToESBulk("", "", bulkDoc.toString());
    }
  }
}
