package com.olastore.listing.clustering.algorithms;

import com.olastore.listing.clustering.clients.ESClient;
import com.olastore.listing.clustering.geo.GeoHashUtil;
import com.olastore.listing.clustering.pojos.ClusterPoint;
import org.apache.http.entity.FileEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
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

  public static ConcurrentHashMap<String, ClusterPoint> clusterPoints = new ConcurrentHashMap<>();
  public static List<String> pushedClusters = Collections.synchronizedList(new ArrayList<String>());
  public static ConcurrentHashMap<String, Integer> clusterProductCoverage = new ConcurrentHashMap<>();
  public static ConcurrentHashMap<String, Integer> clusterSubCatCoverage = new ConcurrentHashMap<>();
  public static AtomicInteger jobsRun = new AtomicInteger();
  public static ConcurrentHashMap<String, Double> clusterRankMap = new ConcurrentHashMap<>();
  public static Set<String> popularProdSet = new HashSet<>();
  public static AtomicInteger bulkDocCount = new AtomicInteger(0);
  public static StringBuilder bulkDoc = new StringBuilder();
  public static Logger logger = LoggerFactory.getLogger(GeoClustering.class);

  public GeoClustering(String env, Map esConfig, Map clustersConfig){
    String esHostKey = "es_host_"+env;
    this.esClient = new ESClient((String)esConfig.get(esHostKey));
    this.esConfig = esConfig;
    this.clustersConfig = clustersConfig;
  }

  public void createFreshClusteringIndices() {
    try {
      //delete cluster related indices
      String indexName = esConfig.get("geo_hash_index_name") + "," + esConfig.get("clusters_index_name");
      esClient.deleteIndex(indexName);

      //create geo hash index
      JSONObject create_geo_response = esClient.createIndex((String) esConfig.get("geo_hash_index_name"), new FileEntity(new File((String) esConfig.get("geo_mappings_file_path"))));
      //logger.info("Creating geo mappings ",create_geo_response.toString());
      JSONObject create_cluster_response = esClient.createIndex((String) esConfig.get("clusters_index_name"), new FileEntity(new File((String) esConfig.get("cluster_mappings_file_path"))));
      //logger.info("Creating cluster mappings ",create_cluster_response.toString());
    }catch (Exception e){
      //logger.error("Exception in create/delete indices ",e);
    }
  }


  //generate Hash set from the csv
  private Set<String> generatePopularProductSet() {

    Set<String> productIdSet = new HashSet<>();
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
      //logger.error("Exception happened!",e);
    } finally {
      try {
        if(fileReader!=null)fileReader.close();
        if(bufferedReader!=null)bufferedReader.close();
      } catch (Exception e) {
        //logger.error("Exception happened!",e);
      }
    }
    return productIdSet;
  }


  public void createClusters(String city) throws Exception {

    //generate popular products
    popularProdSet = generatePopularProductSet();
    if(popularProdSet.size() ==0 ){
      //logger.error("Popular products is zero. Stopping now");
      return;
    }
    //logger.info("Popular items reading completed. Total number of popular products are "+popularProdSet.size());

    //re create cluster indices
    createFreshClusteringIndices();

    //get geo hashes for the area
    GeoHashUtil geoHashUtil = new GeoHashUtil();
    List<String> geoHashList = geoHashUtil.getGeoHashesForArea(city);
//
//    List<String> geoHashList = new ArrayList<>();
//    geoHashList.add("tdr4phx");
//    geoHashList.add("tdr0ftn");
//    geoHashList.add("tdr1vzc");
//    geoHashList.add("tdr1yrb");

    //run clustering algo
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (String geoHash : geoHashList) {
      executorService.submit(new ClusteringWorker(geoHash));
    }
    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.DAYS);
    if(!bulkDoc.toString().isEmpty()){
      JSONObject result = esClient.pushToESBulk("", "", bulkDoc.toString());
      //logger.info("Response from ES for  is "+ result);
    }
  }

}
