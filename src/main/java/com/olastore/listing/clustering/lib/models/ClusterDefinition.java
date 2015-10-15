package com.olastore.listing.clustering.lib.models;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Cluster Object with all the properties required. Created by gurramvinay on 6/30/15.
 */
public class ClusterDefinition{

  private String name;
  private List<String> points = new ArrayList<String>();
  private double distance ;
  private String geoHash;
  private double rank;
  private int num_stores;
  private String status;

  public void addPoint(String p) {
    points.add(p);
    num_stores++;

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPoints() {
    return points;
  }

  public void setPoints(List<String> points) {
    this.points = points;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public String getGeoHash() {
    return geoHash;
  }

  public void setGeoHash(String geoHash) {
    this.geoHash = geoHash;
  }

  public int getNum_stores() {
    return num_stores;
  }

  public void setNum_stores(int num_stores) {
    this.num_stores = num_stores;
  }

  public double getRank() {
    return rank;
  }

  public void setRank(double rank) {
    this.rank = rank;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return getJSON().toString();
  }

  public JSONObject getJSON() {

    JSONObject cluster = new JSONObject();
    cluster.put("rank",rank);
    JSONArray storeIdArry = new JSONArray();
    for (String c:  points) {
      storeIdArry.put(c);
    }
    cluster.put("stores", storeIdArry);
    cluster.put("stores_count",num_stores);
    cluster.put("status",status);
    return cluster;
  }

}
