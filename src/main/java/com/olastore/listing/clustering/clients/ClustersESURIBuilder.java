package com.olastore.listing.clustering.clients;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by gurramvinay on 10/5/15.
 */
public class ClustersESURIBuilder {

  private String ES_HOST;
  private String schme = "http";
  private enum endPoints {

    SEARCH("_search"),BULK("_bulk");
    private final String name;

    private endPoints(String name){
      this.name = name;
    }

    public boolean equalsName(String otherName){
      return (otherName == null) ? false : name.equals(otherName);
    }

    @Override
    public String toString(){
      return this.name;
    }
  };

  ClustersESURIBuilder(String esHOST) {
    this.ES_HOST = esHOST;
  }

  ClustersESURIBuilder(String esHOST, String schme){
    this.ES_HOST = esHOST;
    this.schme = schme;
  }

  public URI getESSearchEndPoint(HashMap<String,String> searchLevels) throws URISyntaxException {
    StringBuilder path = new StringBuilder();
    if(searchLevels.containsKey("index_name")){
      path.append("/").append(searchLevels.get("index_name"));
    }
    if(searchLevels.containsKey("index_type")){
      path.append("/").append(searchLevels.get("index_type"));
    }
    return  new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();
  }

  public URI getBulkEndPoint(HashMap<String,String> bulkLevels) throws URISyntaxException {
    StringBuilder path = new StringBuilder();
    if(bulkLevels.containsKey("index_name")){
      path.append("/").append(bulkLevels.get("index_name"));
    }
    if(bulkLevels.containsKey("index_type")){
      path.append("/").append(bulkLevels.get("index_type"));
    }
    path.append("/").append(endPoints.BULK);
    return  new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();
  }

  public URI getBulkEndPoint() throws URISyntaxException {
    return  getBulkEndPoint(new HashMap<String, String>());
  }

  public URI getDocEndPoint(HashMap<String,String> docMap) throws URISyntaxException {
    StringBuilder path = new StringBuilder();
    path.append("/").append(docMap.get("index_name"));
    path.append("/").append(docMap.get("index_type"));
    path.append("/").append(docMap.get("id"));
    return  new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();

  }
}
