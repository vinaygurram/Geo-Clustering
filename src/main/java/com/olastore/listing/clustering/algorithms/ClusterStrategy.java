package com.olastore.listing.clustering.algorithms;

import com.github.davidmoten.geo.GeoHash;
import com.olastore.listing.clustering.utils.DistanceMatrix;
import com.olastore.listing.clustering.lib.models.Geopoint;
import com.olastore.listing.clustering.lib.models.ClusterDefinition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * defining strategy to create the geo hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * distance matrix is computed for each geo hash
 */

public class ClusterStrategy {

  private static Logger LOG = LoggerFactory.getLogger(ClusterStrategy.class);

  private DistanceMatrix distanceMatrix;
    private Map esConfig;

  public void createDistanceMatrix(Geopoint geoHash, List<String> points) {
    List<String> ttpoints = new ArrayList<String>(points);
    this.distanceMatrix = new DistanceMatrix(geoHash,ttpoints);
  }

  public List<ClusterDefinition> createClusters(Geopoint geoHash,  List<String>points, Map esConfig) {

      this.esConfig = esConfig;
    if(points==null || points.size()==0) return new ArrayList<ClusterDefinition>();
    int clustersForCombination = 0;

    createDistanceMatrix(geoHash, points);
    List<ClusterDefinition> validClusters = new ArrayList<ClusterDefinition>();
    String encodedGeoHash = GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude(),7);
    ClusterDefinition temp;
    for(String s: points){

      List<String> thisList = new ArrayList<String>();
      thisList.add(s);
      temp = checkValidCluster(geoHash,thisList);
      if(temp!=null){
        clustersForCombination++;
        temp.setGeoHash(encodedGeoHash);
        validClusters.add(temp);
      }
    }

    if(clustersForCombination==0) return validClusters;
    clustersForCombination = 0;

    List<List<String>>clusters = get2CClusters(points);
    for(List<String> clusterObj : clusters){
      temp = checkValidCluster(geoHash,clusterObj);
      if(temp!=null){
        clustersForCombination++;
        temp.setGeoHash(encodedGeoHash);
        validClusters.add(temp);
      }

    }

    if(clustersForCombination==0) return validClusters;
    clustersForCombination = 0;

    clusters =get3CClusters(points);
    for(List<String> clusterObj : clusters){
      temp = checkValidCluster(geoHash,clusterObj);
      if(temp!=null){
        clustersForCombination++;
        temp.setGeoHash(encodedGeoHash);
        validClusters.add(temp);
      }

    }

    if(clustersForCombination==0) return validClusters;
    clustersForCombination = 0;

    if(points.size()>3){
      clusters = get4CClusters(points);
      for(List<String> clusterObj : clusters){
        temp = checkValidCluster(geoHash,clusterObj);
        if(temp!=null){
          clustersForCombination++;
          temp.setGeoHash(encodedGeoHash);
          validClusters.add(temp);
        }
      }

    }
    this.distanceMatrix = null;
    return validClusters;
  }

  //Helper methods
  /**
   * computes rank for a given cluster
   * Rank r = (popularProductsInCluster/totalPopularProducts)
   */
  public void setRankParameters(Set<String> popularProductsSet,ClusterDefinition clusterObj) {

    String storeIdString = "";
    String clusterId = "";
    List<String> stores = clusterObj.getPoints();
    Collections.sort(stores);
    for(String s: stores){
      storeIdString += "\""+s+"\",";
      clusterId +="-"+s;
    }
    clusterId = clusterId.substring(1);
    storeIdString = storeIdString.substring(0,storeIdString.length()-1);

    if(ClusterBuilder.clusterRankMap.containsKey(clusterId)){
      clusterObj.setProductsCount(ClusterBuilder.clusterProductCoverage.get(clusterId));
      clusterObj.setSubCatCount(ClusterBuilder.clusterSubCatCoverage.get(clusterId));
      clusterObj.setRank(ClusterBuilder.clusterRankMap.get(clusterId));
      return;
    }

    Set<String> productsSet = new HashSet<>();
    int subCatCount = 0;
    try {

      String query = "{\"size\": 0,\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[" +
          "{\"terms\":{\"store_details.id\":["+storeIdString+"]}}," +
          "{\"term\":{\"product_details.available\":true}}," +
          "{\"term\":{\"product_details.status\":\"current\"}}]}}}}," +
          "\"aggregations\":{\"unique_products\":{\"terms\":{\"field\":\"product_details.id\",\"size\":0}}," +
          "\"sub_cat_count\":{\"cardinality\":{\"field\":\"product_details.sub_category_id\"}}}}";
      JSONObject result = ClusterBuilder.esClient.searchES((String) esConfig.get("listing_index_name"),
              (String) esConfig.get("listing_index_type"), query);
      JSONObject esResult = result.getJSONObject("aggregations");
      JSONArray uniqueProdBuckets = esResult.getJSONObject("unique_products").getJSONArray("buckets");
      for(int i=0;i<uniqueProdBuckets.length();i++){
        String productId = uniqueProdBuckets.getJSONObject(i).getString("key");
        productsSet.add(productId);
      }
      subCatCount = esResult.getJSONObject("sub_cat_count").getInt("value");
    }catch (Exception e){
      LOG.error("Found error while setting ranks " + e.toString());
    }

    ClusterBuilder.clusterProductCoverage.put(clusterId,productsSet.size());
    ClusterBuilder.clusterSubCatCoverage.put(clusterId,subCatCount);
    clusterObj.setProductsCount(productsSet.size());
    clusterObj.setSubCatCount(subCatCount);

    Set<String> intesection = new HashSet<String>(productsSet);
    intesection.retainAll(popularProductsSet);
    int popular_products_count = intesection.size();
    double rank = ((double) popular_products_count/(double)popularProductsSet.size());

    ClusterBuilder.clusterRankMap.put(clusterId,rank);
    clusterObj.setRank(rank);
  }

  /**
   * Shortest Distance if store has only 2 points
   */
  public double getShortestDistanceFor2(Geopoint geohashPoint, List<String> points) {

    double dbp = distanceMatrix.getDistance(points.get(0),points.get(1));
    String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude(),7);
    double dfg = distanceMatrix.getDistance(geoString,points.get(0));
    double dsg = distanceMatrix.getDistance(geoString,points.get(1));
    dfg = dfg + dbp;
    dsg = dsg+dbp;
    return dfg>dsg?dsg:dfg;
  }

  public double getShortestDistanceForMultiPoints(Geopoint geohashPoint, List<String> points) {
    double smallestDistace = Double.MAX_VALUE;
    String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude(),7);
    for(String tp : points){
      double di = distanceBtPoints(tp,geoString);

      double si = getShortestDistanceWithPoint(tp,points);
      double total = di+si;
      if(total <smallestDistace) {
        smallestDistace = total;
      }
    }
    return smallestDistace;
  }

  /**
   * helper method
   * @return the distance between two geo points
   */
  public double distanceBtPoints(String c1, String c2){
    return distanceMatrix.getDistance(c1,c2);
  }


  /**
   * computes the shortest path using all possible combinations
   * @return Double shortest distance which connects all the distances
   */
  public double getShortestDistanceWithPoint(String cp2, List<String> list) {
    try {
      List<String> ss = new ArrayList<String>();
      for(String s: list){
        if(s.contentEquals(cp2)) continue;
        ss.add(s);
      }
      String[] idsString = new String[ss.size()];
      idsString = ss.toArray(idsString);
      List<List<String>> permutations = permute(idsString);
      double gDistance = Double.MAX_VALUE;
      for(List<String> possiblity : permutations){

        double tempDist = distanceMatrix.getDistance(cp2,possiblity.get(0));
        for(int i=0;i<possiblity.size()-1;i++){
          tempDist+=distanceMatrix.getDistance(possiblity.get(i),possiblity.get(i+1));
        }
        if(tempDist < gDistance) gDistance = tempDist;
      }
      return  gDistance;
    }catch (Exception e){
    }
    return Double.MAX_VALUE;
  }

  /**
   * Create 3 possible combinations with everything
   */
  public List<List<String>> get3CClusters(List<String> stringList) {

    if(stringList.size()<3) return new ArrayList<List<String>>();

    List<List<String>> totalList = new ArrayList<List<String>>();
    for(int i=0;i<stringList.size();i++){
      List<String> tempList = new ArrayList<String>();
      tempList.add(stringList.get(i));
      for(int j=i+1;j+1<stringList.size() ;j++){
        tempList.add(stringList.get(j));
        tempList.add(stringList.get(j+1));
        totalList.add(tempList);
        tempList = new ArrayList<String>();
        tempList.add(stringList.get(i));
      }
    }
    return totalList;

  }

  /**
   *Get all 4 possible combinations
   */
  public List<List<String>> get4CClusters(List<String> idList) {

    if(idList.size()<4) return new ArrayList<List<String>>();

    List<List<String>> totalList = new ArrayList<List<String>>();
    for(int i=0;i<idList.size();i++){
      List<String> tempList = new ArrayList<String>();
      tempList.add(idList.get(i));
      for(int j=i+1;j+2<idList.size();j++){
        tempList.add(idList.get(j));
        tempList.add(idList.get(j+1));
        tempList.add(idList.get(j+2));
        totalList.add(tempList);
        tempList = new ArrayList<String>();
        tempList.add(idList.get(i));
      }
    }
    return totalList;
  }

  /**
   * Get all 5 possible combinations
   */
  public List<List<String>> get5CClusters(List<String> idList) {

    if(idList.size()<5) return new ArrayList<List<String>>();
    List<List<String>> totalList = new ArrayList<List<String>>();
    for(int i=0;i<idList.size();i++){
      List<String> tempList = new ArrayList<String>();
      tempList.add(idList.get(i));
      for(int j=i+1;j+3<idList.size();j++){
        tempList.add(idList.get(j));
        tempList.add(idList.get(j+1));
        tempList.add(idList.get(j+2));
        tempList.add(idList.get(j+3));
        totalList.add(tempList);
        tempList = new ArrayList<String>();
        tempList.add(idList.get(i));
      }
    }
    return totalList;
  }

  /**
   * Get all possible 6 combinations
   */
  public List<List<String>> get6CClusters(List<String> idList) {

    if(idList.size()<6) return new ArrayList<List<String>>();
    List<List<String>> totalList = new ArrayList<List<String>>();
    for(int i=0;i<idList.size();i++){
      List<String> tempList = new ArrayList<String>();
      tempList.add(idList.get(i));
      for(int j=i+1;j+4<idList.size();j++){
        tempList.add(idList.get(j));
        tempList.add(idList.get(j+1));
        tempList.add(idList.get(j+2));
        tempList.add(idList.get(j+3));
        tempList.add(idList.get(j+4));
        totalList.add(tempList);
        tempList = new ArrayList<String>();
        tempList.add(idList.get(i));
      }
    }
    return totalList;
  }

  /**
   * Get all 2 possible combinations
   */
  public List<List<String>> get2CClusters(List<String> strings){

    if(strings.size()<2) return new ArrayList<>();
    List<List<String>> totalList = new ArrayList<List<String>>();
    for(int i=0;i<strings.size();i++){
      List<String> tempList = new ArrayList<String>();
      tempList.add(strings.get(i));
      for(int j=i+1;j<strings.size() ;j++){
        tempList.add(strings.get(j));
        totalList.add(tempList);
        tempList = new ArrayList<String>();
        tempList.add(strings.get(i));
      }
    }
    return totalList;
  }

  /**
   * Check if the point cluster is valid
   */
  public ClusterDefinition checkValidCluster(Geopoint geoHash,List<String> storeIdList){
    double shortDistance = Double.MAX_VALUE ;
    if(storeIdList.size()==1){
      shortDistance = Geopoint.getDistance(geoHash, ClusterBuilder.clusterPoints.get(storeIdList.get(0)).getLocation());
    }else if(storeIdList.size()==2){
      shortDistance =getShortestDistanceFor2(geoHash, storeIdList);
    }else if(storeIdList.size()>=3){
      shortDistance = getShortestDistanceForMultiPoints(geoHash, storeIdList);
    }
    if(shortDistance>8) return null;

    ClusterDefinition clusterDefinition = new ClusterDefinition();
    for(String s: storeIdList){
      clusterDefinition.setStatus(ClusterBuilder.storeStatusMap.get(s));
      clusterDefinition.addPoint(s);
    }
    clusterDefinition.setDistance(shortDistance);
    setRankParameters(ClusterBuilder.popularProdSet, clusterDefinition);

    return clusterDefinition;
  }

  /**
   * Creates all possible String ids;
   */
  public List<List<String>> permute(String[] ids) {
    List<List<String>> permutations = new ArrayList<List<String>>();
    permutations.add(new ArrayList<String>());
    for ( int i = 0; i < ids.length; i++ ) {
      List<List<String>> current = new ArrayList<List<String>>();
      for ( List<String> permutation : permutations ) {
        for ( int j = 0, n = permutation.size() + 1; j < n; j++ ) {
          List<String> temp = new ArrayList<String>(permutation);
          temp.add(j, ids[i]);
          current.add(temp);
        }
      }
      permutations = new ArrayList<>(current);
    }
    return permutations;
  }


}