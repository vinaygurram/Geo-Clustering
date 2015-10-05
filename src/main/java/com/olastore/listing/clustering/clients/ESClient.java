package com.olastore.listing.clustering.clients;

import com.olastore.listing.clustering.algorithms.GeoClustering;
import com.olastore.listing.clustering.pojos.ClusterDefinition;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by meetanshugupta on 01/10/15.
 */

public class ESClient {

  ClustersHttpClientFactory clustersHttpClientFactory;
  ClustersESURIBuilder clustersESURIBuilder;


  public ESClient(String esHost){
    clustersHttpClientFactory = new ClustersHttpClientFactory();
    clustersESURIBuilder = new ClustersESURIBuilder(esHost);
  }

  public static Logger LOG = Logger.getLogger(ESClient.class);

  public JSONObject searchES(String indexName,String indexType, String query) throws URISyntaxException {
    StringEntity stringEntity = new StringEntity(query, Charset.defaultCharset());
    HashMap<String,String> map = new HashMap<>();
    if(!indexName.contentEquals("")) {
      map.put("index_name",indexName);
      if(!indexType.contentEquals("")) map.put("index_type",indexType);
    }
    URI searchUri = clustersESURIBuilder.getESSearchEndPoint(map);
    return  new ClustersHttpClient(clustersHttpClientFactory.getHttpClient()).executePost(searchUri,stringEntity);
  }

  public JSONObject getESDoc(String indexName,String indexType,String docID) throws URISyntaxException {
    HashMap<String,String> map = new HashMap<>();
    map.put("index_name",indexName);
    map.put("index_type",indexType);
    map.put("id",docID);
    URI docUri = clustersESURIBuilder.getDocEndPoint(map);
    return new ClustersHttpClient(clustersHttpClientFactory.getHttpClient()).executeGet(docUri);
  }

  public JSONObject pushToES(String indexName,String indexType,String id, String data) throws URISyntaxException {
    StringEntity entity = new StringEntity(data,Charset.defaultCharset());
    HashMap<String,String> map = new HashMap<>();
    map.put("index_name",indexName);
    map.put("index_type",indexType);
    map.put("id",id);
    URI docUri = clustersESURIBuilder.getDocEndPoint(map);
    return new ClustersHttpClient(clustersHttpClientFactory.getHttpClient()).executePost(docUri,entity);
  }

  public JSONObject pushToESBulk (String indexName,String indexType,String data) throws URISyntaxException {
    StringEntity entity = new StringEntity(data,Charset.defaultCharset());
    HashMap<String,String> map = new HashMap<>();
    if(!indexName.contentEquals("")) {
      map.put("index_name",indexName);
      if(!indexType.contentEquals("")) map.put("index_type",indexType);
    }
    URI bulkURI = clustersESURIBuilder.getBulkEndPoint(map);
    return new ClustersHttpClient((clustersHttpClientFactory.getHttpClient())).executePost(bulkURI,entity);

  }

  public JSONObject createIndex(String indexName, FileEntity fileEntity) throws URISyntaxException {
    URI indexUri = clustersESURIBuilder.getIndexURI(indexName);
    return new ClustersHttpClient(clustersHttpClientFactory.getHttpClient()).executePut(indexUri,fileEntity);
  }

  public void deleteIndex(String indexName) throws URISyntaxException {
    URI indexUri = clustersESURIBuilder.getIndexURI(indexName);
    new ClustersHttpClient(clustersHttpClientFactory.getHttpClient()).executeDelete(indexUri);
  }


}
