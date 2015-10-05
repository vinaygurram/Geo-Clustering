package com.olastore.listing.clustering.algorithms;

import com.olastore.listing.clustering.clients.ESClient;
import com.olastore.listing.clustering.geo.BoundingBox;
import com.olastore.listing.clustering.pojos.ClusterDefinition;
import com.olastore.listing.clustering.pojos.ClusterPoint;
import com.olastore.listing.clustering.geo.Geopoint;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to create clusters. It will have all the utility Objects. Created by gurramvinay on 6/16/15.
 */
public class GeoClustering {

  public static ESClient esClient;
  public static Map esConfig;
  public static Map clustersConfig;

  public static ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
  public static ConcurrentHashMap<String, ClusterPoint> clusterPoints = new ConcurrentHashMap<String, ClusterPoint>();
  public static ConcurrentHashMap<String, List<ClusterDefinition>> computedClusters = new ConcurrentHashMap<String, List<ClusterDefinition>>();
  public static List<String> pushedClusters = Collections.synchronizedList(new ArrayList<String>());
  public static ConcurrentHashMap<String, Integer> clusterProductCoverage = new ConcurrentHashMap<>();
  public static ConcurrentHashMap<String, Integer> clusterSubCatCoverage = new ConcurrentHashMap<>();
  public static AtomicInteger jobsRun = new AtomicInteger();
  public static ConcurrentHashMap<String, Double> clusterRankMap = new ConcurrentHashMap<>();
  public static Set<String> popularProdSet = new HashSet<>();
  public static AtomicInteger bulkDocCount = new AtomicInteger(0);
  public static StringBuilder bulkDoc = new StringBuilder();
  public static Map yamlMap = null;
  public static Logger logger = LoggerFactory.getLogger(GeoClustering.class);

  public GeoClustering(ESClient esClient, Map esConfig, Map clustersConfig){
    this.esClient = esClient;
    this.esConfig = esConfig;
    this.clustersConfig = clustersConfig;
  }

  private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision) {

    Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
        box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
    return boxCoverage.getHashes();
  }

  private BoundingBox getBangaloreBox() {

    Geopoint topleft = new Geopoint((Double)((HashMap)yamlMap.get("bbox_top_left")).get("lat"), (Double)((HashMap)yamlMap.get("bbox_top_left")).get("lon"));
    Geopoint botright = new Geopoint((Double)((HashMap)yamlMap.get("bbox_bot_right")).get("lat"), (Double)((HashMap)yamlMap.get("bbox_bot_right")).get("lon"));
    return new BoundingBox(topleft, botright);
  }

  public List<String> getBlrGeoHashes() {

    BoundingBox bbox = getBangaloreBox();
    Set<String> hashes = getGeoHashOfBoundingBox(bbox, (Integer) yamlMap.get("clusters_geo_precision"));
    Iterator<String> iterator = hashes.iterator();
    int valitGeoHashCount = 0;
    List<String> geohashList = new ArrayList<String>();
    while (iterator.hasNext()) {
      String thisHash = iterator.next();
      geohashList.add(thisHash);
    }
    logger.info("Total number of hashes are "+ geohashList.size());
    return geohashList;
  }

  //read yaml file to get the map
  private void readYAML() {

    try {
      Yaml yaml = new Yaml();
      yamlMap = (Map) yaml.load(new FileInputStream(new File("src/main/resources/config/app.yaml")));
      logger.info("configuration reading is complete");
    }catch (Exception e){
      logger.error("configuration reading failed");
    }
  }
  public Set<String> generateProductSetFromCSV(String pathtoFile, boolean isFnv) {

    Set<String> productIdSet = new HashSet<String>();
    FileReader fileReader = null;
    BufferedReader bufferedReader = null;
    try {
      fileReader = new FileReader(new File(pathtoFile));
      bufferedReader = new BufferedReader(fileReader);
      String line = bufferedReader.readLine();
      while ((line = bufferedReader.readLine()) != null) {
        String[] values = line.split(",");
        String productId = values[0];
        if (isFnv) productId = productId.substring(1, productId.length() - 1);
        productIdSet.add(productId);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    } finally {
      try {
        fileReader.close();
        bufferedReader.close();
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
    return productIdSet;
  }

  public void createFreshClusteringIndices() {

    try {
      //delete cluster related indices
      String indexName = (String) esConfig.get("geo_hash_index_name") + "," +(String) esConfig.get("clusters_index_name");
      esClient.deleteIndex(indexName);

      //create geo hash index
      FileEntity fileEntity = new FileEntity(new File((String) esConfig.get("cluster_mappings_file_path")));
      JSONObject create_geo_response = esClient.createIndex((String) esConfig.get("geo_hash_index_name"), new FileEntity(new File((String) esConfig.get("geo_mappings_file_path"))));
      logger.info("Creating geo mappings ",create_geo_response.toString());
      JSONObject create_cluster_response = esClient.createIndex((String) esConfig.get("clusters_index_name"), new FileEntity(new File((String) esConfig.get("cluster_mappings_file_path"))));
      logger.info("Creating cluster mappings ",create_cluster_response.toString());
    }catch (Exception e){
      logger.error("Exception in create/delete indices ",e);
    }
  }



  //generate Hash set from the csv
  private Set<String> generatePopularProductSet() {

    Set<String> productIdSet = new HashSet<String>();
    FileReader fileReader = null;
    BufferedReader bufferedReader = null;
    try {
      fileReader = new FileReader(new File((String)clustersConfig.get("popular_products_file_path")));
      bufferedReader = new BufferedReader(fileReader);
      String line = bufferedReader.readLine();
      while ((line = bufferedReader.readLine()) != null) {
        productIdSet.add(line);
      }
    } catch (Exception e) {
      logger.error("Exception happened!",e);
    } finally {
      try {
        fileReader.close();
        bufferedReader.close();
      } catch (Exception e) {
        logger.error("Exception happened!",e);
      }
    }
    return productIdSet;
  }


  public void createClusters(){

    //generate popular products
    popularProdSet = generatePopularProductSet();
    if(popularProdSet.size() ==0 ){
      logger.error("Popular products is zero. Stopping now");
      return;
    }
    logger.info("Popular items reading completed. Total number of popular products are "+popularProdSet.size());

    createFreshClusteringIndices();



  }

  public static void main(String[] args) {

    long time_s = System.currentTimeMillis();
    logger.info("Clustering logic start "+time_s);
    try {
      GeoClustering geoClustering = new GeoClustering(null,null,null);
      geoClustering.readYAML();
      // read configuration file
      //generate popular products list if popular products are zero stop
      popularProdSet = geoClustering.generatePopularProductSet();
      if(popularProdSet.size() ==0 ){
        logger.error("Popular products is zero. Stopping now");
        return;
      }
      logger.info("Popular items reading completed. Total number of popular products are "+popularProdSet.size());
      //create fresh indexes and get hashes to be ready
      geoClustering.createFreshClusteringIndices();
      //List<String> geoHashList = geoClustering.getBlrGeoHashes();
      List<String> geoHashList = new ArrayList<>();
      geoHashList = new ArrayList<String>();
      geoHashList.add("tdr4phx");
      geoHashList.add("tdr0ftn");
      geoHashList.add("tdr1vzc");
      geoHashList.add("tdr1yrb");
      ExecutorService executorService = Executors.newFixedThreadPool(10);
      List<Future<String>> futuresList = new ArrayList<>();
      for (String geoHash : geoHashList) {
        Future<String> thisFuture = executorService.submit(new WorkerThread(geoHash));
        futuresList.add(thisFuture);
      }
      executorService.shutdown();
      executorService.awaitTermination(1, TimeUnit.DAYS);
      if(!bulkDoc.toString().isEmpty()){
        HttpClient httpClient = HttpClientBuilder.create().build();
        String es_bulk_api = (String) yamlMap.get("es_bulk_api");
        HttpPost httpPost = new HttpPost(es_bulk_api);
        httpPost.setEntity(new StringEntity(GeoClustering.bulkDoc.toString()));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        logger.info("Response from ES for  is "+ httpResponse.getEntity().toString());
        int code = httpResponse.getStatusLine().getStatusCode();
        if(code!=200 && code!=201) {
          logger.info(httpResponse.getStatusLine().toString());
        }
      }
      long time_e = System.currentTimeMillis();
      logger.info(" Total time taken is "+(time_e - time_s) + "ms");
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
