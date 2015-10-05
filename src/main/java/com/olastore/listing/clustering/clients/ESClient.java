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

  public static void pushClusterToES(List<ClusterDefinition> clusterDefinitions){
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
        thisCluster.put("status", clusterDefinition.isStatus());
        thisCluster.put("rank", clusterDefinition.getRank());
        clusters.put(thisCluster);

        if(GeoClustering.pushedClusters.contains(hash)){
        }else {
          String clusters_indexing_api = (String) GeoClustering.yamlMap.get("es_document_api") ;
          clusters_indexing_api = clusters_indexing_api.replace(":index_name",(String)GeoClustering.yamlMap.get("clusters_index_name"));
          clusters_indexing_api = clusters_indexing_api.replace(":index_type",(String)GeoClustering.yamlMap.get("clusters_index_type"));
          clusters_indexing_api = clusters_indexing_api.replace(":id",hash);
          HttpPost postRequest = new HttpPost(clusters_indexing_api);
          String jsonString = clusterDefinition.toString();
          postRequest.setEntity(new StringEntity(jsonString));
          //send post request
          httpClient = HttpClientBuilder.create().build();
          HttpResponse response = httpClient.execute(postRequest);
          int code = response.getStatusLine().getStatusCode();
          if(code!=200 && code!= 201){
            LOG.error("Error in pushing geo hashes "+ response.getStatusLine());
          }
          GeoClustering.pushedClusters.add(hash);
        }
      }
      //make doc for pushing
      String thisDocAsString = "{\"index\" : {\"_index\" : \"" +(String)GeoClustering.yamlMap.get("geo_hash_index_name")+ "\",\"_type\" : \""
          + (String)GeoClustering.yamlMap.get("geo_hash_index_type")+ "\",\"_id\":\""
          + clusterDefinitions.get(0).getGeoHash() + "\" }}\n" +geoDoc.toString() + "\n";
      String maxString = "";

      synchronized (GeoClustering.bulkDoc){
        GeoClustering.bulkDoc.append(thisDocAsString);
        GeoClustering.bulkDocCount.incrementAndGet();
        if(GeoClustering.bulkDocCount.get()>500){
          maxString = GeoClustering.bulkDoc.toString();
          GeoClustering.bulkDoc = new StringBuilder();
          GeoClustering.bulkDocCount =new AtomicInteger(0);
        }
      }

      if(!maxString.isEmpty()){
        httpClient = HttpClientBuilder.create().build();
        String geo_hashes_bulk_api = (String) GeoClustering.yamlMap.get("es_bulk_api");
        HttpPost httpPost = new HttpPost(geo_hashes_bulk_api);
        //HttpPost httpPost = new HttpPost(GeoClustering.ES_REST_API +"/"+ GeoClustering.ES_BULK_END_POINT);
        httpPost.setEntity(new StringEntity(maxString));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        int code = httpResponse.getStatusLine().getStatusCode();
        if(code!=200 && code!=201) {
          LOG.error("Error in bulk indexing " + httpResponse.getStatusLine());
        }
      }

    }catch (Exception e){
      LOG.error("Something went wrong while pushing clusters "+ e.getMessage());
    }
  }
}
