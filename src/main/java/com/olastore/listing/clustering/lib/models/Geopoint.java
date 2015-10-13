package com.olastore.listing.clustering.lib.models;

/**
 * Generic Object to denote the location of any object
 * Created by gurramvinay on 8/10/15.
 */
public class Geopoint {

  private double longitude;
  private double latitude;
  private static final double earthRadius = 6372.8; // kilometers

  public Geopoint(double lat, double lon) {

    this.latitude = lat;
    this.longitude = lon;
  }

  public static double getDistance(Geopoint l1,Geopoint l2) {

    double dLat = Math.toRadians(l2.getLatitude() - l1.getLatitude());
    double dLon = Math.toRadians(l2.getLongitude() - l1.getLongitude());
    double lat1 = Math.toRadians(l1.getLatitude());
    double lat2 = Math.toRadians(l2.getLatitude());

    double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
    double c = 2 * Math.asin(Math.sqrt(a));
    return earthRadius * c;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(latitude).append(",").append(longitude).toString();
  }
  @Override
  public Geopoint clone() {
    return new Geopoint(latitude,longitude);
  }
}