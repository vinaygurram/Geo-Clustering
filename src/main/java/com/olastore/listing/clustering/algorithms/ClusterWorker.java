package com.olastore.listing.clustering.algorithms;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.olastore.listing.clustering.lib.models.Geopoint;
import com.olastore.listing.clustering.lib.models.ClusterDefinition;
import com.olastore.listing.clustering.lib.models.ClusterPoint;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Class creates clusters for a geo hash and push the clusters into ES. Created by gurramvinay on 7/1/15.
 */
public class ClusterWorker implements Callable<String> {
  String geohash;
  Map esConfig;
  Map clustersConfig;


  ClusterWorker(String geohash, Map esConfig, Map clustersConfig) {
    this.geohash = geohash;
    this.clustersConfig = clustersConfig;
    this.esConfig = esConfig;

  }
  private static final Logger logger = LoggerFactory.getLogger(ClusterWorker.class);

  /**
   * Calls listing index to find stores within 6kms
   * Calls stores index to get lat,long values for stores
   */
  public List<String> getClusetringPointsForGeoHash(String geohash) {
    List<String> reShops = new ArrayList<String>();
    try {
      int cluster_radius = (Integer) clustersConfig.get("clusters_radius");
      String query ="{\"size\":0,\"query\":{\"filtered\":{\"filter\":{\"geo_distance\":{\"distance\":\""+cluster_radius+"km\"," +
          "\"store_details.location\":\""+geohash+"\"}}}}," +
          "\"aggregations\":{\"stores_unique\":{\"terms\":{\"field\":\"store_details.id\",\"size\":0}}}}";
      JSONObject jsonObject = ClusterBuilder.esClient.searchES((String) esConfig.get("listing_index_name"),
          (String) esConfig.get("listing_index_type"),query);
      jsonObject = jsonObject.getJSONObject("aggregations");
      jsonObject = jsonObject.getJSONObject("stores_unique");
      JSONArray stores = jsonObject.getJSONArray("buckets");
      logger.info("Stores for geo hash {}",stores);
      for(int i=0;i<stores.length();i++){
        String id = stores.getJSONObject(i).getInt("key")+"";
        ClusterPoint clusterPoint;
        boolean is_store_exists = false;
        if(ClusterBuilder.clusterPoints.containsKey(id)){
          is_store_exists = true;
        }else if(ClusterBuilder.deletedStores.contains(id)){
        }else {
          try {
            JSONObject response2 = ClusterBuilder.esClient.getESDoc((String) esConfig.get("stores_index_name"),
                (String) esConfig.get("stores_index_type"), id);
            if(response2!=null){
              JSONObject response1 = response2.getJSONObject("_source").getJSONObject("store_details");
              if(!response2.getBoolean("found")) continue;
              double lat = response1.getJSONObject("location").getDouble("lat");
              double lng = response1.getJSONObject("location").getDouble("lon");
              ClusterBuilder.storeStatusMap.put(id,response1.getString("store_state"));
              clusterPoint = new ClusterPoint(id,new Geopoint(lat,lng));
              logger.info("Cluster Point {}",clusterPoint);
              ClusterBuilder.clusterPoints.put(id, clusterPoint);
              is_store_exists = true;
            }else {
              logger.error("Store not found");
              ClusterBuilder.deletedStores.add(id);
            }
          }catch (Exception e){
            logger.error("Store not found {}",e);
          }
        }
        if(is_store_exists)reShops.add(id);
      }
    }catch (JSONException e){
      logger.error("Json exceptions {}",e);
    }catch (Exception e){
      logger.error("Exception {}",e);
    }
    return reShops;
  }

  @Override
  public String call() throws Exception {
    List<String>points = getClusetringPointsForGeoHash(geohash);
    if(points.size()==0) return "DONE for "+geohash+"-- no shops within the raidus";
    LatLong gll = GeoHash.decodeHash(geohash);
    Geopoint geopoint = new Geopoint(gll.getLat(),gll.getLon());
    List<ClusterDefinition> clusterDefinitionList = new ClusterStrategy().createClusters(geopoint, points, esConfig);
    if(clusterDefinitionList.size()>0)pushClusters(clusterDefinitionList);
    points = null;
    clusterDefinitionList = null;
    if(ClusterBuilder.jobsRun.getAndIncrement()%50==0){
      logger.info("Jobs run total is {}", ClusterBuilder.jobsRun);
    }
    return "DONE for "+geohash;
  }

  public void pushClusters(List<ClusterDefinition> clusterDefinitions){
    HttpClient httpClient;
    try {
      JSONObject geoDoc = new JSONObject();
      geoDoc.put("id", clusterDefinitions.get(0).getGeoHash());
      geoDoc.put("clusters_count", clusterDefinitions.size());
      JSONArray clusters = new JSONArray();
      geoDoc.put("clusters", clusters);

      for(ClusterDefinition clusterDefinition : clusterDefinitions){
        List<String> stringList = clusterDefinition.getPoints();
        Collections.sort(stringList);
        StringBuilder sb = new StringBuilder();
        for(String s : stringList){
          sb.append("-");
          sb.append(s);
        }
        String hash = sb.toString().substring(1);
        JSONObject thisCluster = new JSONObject();
        thisCluster.put("cluster_id", hash);
        thisCluster.put("distance", clusterDefinition.getDistance());
        clusters.put(thisCluster);

        if(!ClusterBuilder.pushedClusters.contains(hash)){
          ClusterBuilder.esClient.pushToES((String) esConfig.get("clusters_index_name"),
              (String) esConfig.get("clusters_index_type"),hash,clusterDefinition.toString());
          ClusterBuilder.pushedClusters.add(hash);
        }
      }
      String thisDocAsString = "{\"index\" : {\"_index\" : \"" +
          (String) esConfig.get("geo_hash_index_name")+ "\",\"_type\" : \""
          + (String) esConfig.get("geo_hash_index_type")+ "\",\"_id\":\""
          + clusterDefinitions.get(0).getGeoHash() + "\" }}\n" +geoDoc.toString() + "\n";
      String maxString = "";

      synchronized (ClusterBuilder.bulkDoc){
        ClusterBuilder.bulkDoc.append(thisDocAsString);
        ClusterBuilder.bulkDocCount.incrementAndGet();
        if(ClusterBuilder.bulkDocCount.get()>500){
          maxString = ClusterBuilder.bulkDoc.toString();
          ClusterBuilder.bulkDoc = new StringBuilder();
          ClusterBuilder.bulkDocCount =new AtomicInteger(0);
        }
      }

      if(!maxString.isEmpty()){
        ClusterBuilder.esClient.pushToESBulk("","",maxString);
      }

    }catch (Exception e){
      logger.error("Something went wrong while pushing clusters {}", e);
    }
  }
}
