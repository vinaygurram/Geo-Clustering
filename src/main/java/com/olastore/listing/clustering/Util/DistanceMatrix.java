package com.olastore.listing.clustering.Util;

import com.olastore.listing.clustering.algorithms.GeoClustering;
import com.github.davidmoten.geo.GeoHash;
import com.olastore.listing.clustering.geo.Geopoint;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
/**
 * Generic Class to Calculate all the distances for all the clustering points
 * It will be computed once and remain constant for the execution.
 * GOOGLE Distances can be implemented here
 * Created by gurramvinay on 8/10/15.
 */
public class DistanceMatrix {

  private final HashMap<String, Double> dMatrix = new HashMap<String, Double>();
  private final String CCATTERM = "##";

  public DistanceMatrix(Geopoint geoHash, List<String> points) {
    computeDistancMatrix(geoHash,points);
  }

  private String getHash(String id1, String id2,boolean order) {

    if(order) return new StringBuilder().append(id1).append(CCATTERM).append(id2).toString();
    else return new StringBuilder().append(id2).append(CCATTERM).append(id1).toString();
  }

  public HashMap<String,Double> getNodeDistanceMatrix(String id) {

    HashMap<String, Double> ndMap = new HashMap<String, Double>();
    Set<String> keySet = dMatrix.keySet();
    for(String key : keySet){
      String[] keys = key.split("##");
      if(keys[0].contentEquals(id)){
        ndMap.put(keys[1],dMatrix.get(key));
      }else if(keys[1].contentEquals(id)){
        ndMap.put(keys[0],dMatrix.get(key));
      }
    }
    return ndMap;
  }

  public double getDistance(String id1, String id2) {

    String hash1 = getHash(id1,id2,true);
    if(this.dMatrix.containsKey(hash1)) return dMatrix.get(hash1);
    hash1 = getHash(id1,id2,false);
    if(this.dMatrix.containsKey(hash1)) return dMatrix.get(hash1);
    return 0d;
  }

  synchronized private void computeDistancMatrix(Geopoint geoHashString,List<String> clusteringPoints) {

    for(String cp: clusteringPoints){
      dMatrix.put(getHash(GeoHash.encodeHash(geoHashString.getLatitude(),geoHashString.getLongitude(),7),cp,true),Geopoint.getDistance(geoHashString, GeoClustering.clusterPoints.get(cp).getLocation()));

      for(String tp: clusteringPoints){
        if(!cp.contentEquals(tp)){
          String hashId = getHash(tp,cp,true);
          String hashId2 = getHash(tp,cp,false);
          if(dMatrix.containsKey(hashId) || dMatrix.containsKey(hashId2)){
          }else {
            dMatrix.put(hashId,Geopoint.getDistance(GeoClustering.clusterPoints.get(cp).getLocation(), GeoClustering.clusterPoints.get(tp).getLocation()));
          }
        }
      }
    }
  }

}
