package com.olastore.listing.clustering.clients;

import org.apache.http.client.methods.*;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Can only execute one operation. Supporting creating resources both from POST and PUT.
 * Not supporting 202 request accepted as valid response.
 * @author gurramvinay
 */
public class ClustersHttpClient {

  private CloseableHttpClient closeableHttpClient;
  private static final Logger logger = LoggerFactory.getLogger(ClustersHttpClient.class);

  public ClustersHttpClient(CloseableHttpClient closeableHttpClient) {
    this.closeableHttpClient = closeableHttpClient;
  }

  public JSONObject executeGet(URI uri) {
    JSONObject resultObject = null;
    CloseableHttpResponse closeableHttpResponse = null;
    try {
      HttpGet httpGet = new HttpGet(uri);
      closeableHttpResponse = closeableHttpClient.execute(httpGet, new BasicHttpContext());
      int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200){
        resultObject = new JSONObject(EntityUtils.toString(closeableHttpResponse.getEntity()));
      }else {
        logger.debug("Request failed "+ responseCode);
        logger.debug("Message is"+ closeableHttpResponse.getStatusLine().getReasonPhrase());
      }
      EntityUtils.consume(closeableHttpResponse.getEntity());
    }catch (Exception e){
      logger.error("Exception happened!",e);
    }finally {
      try {
        closeableHttpResponse.close();
      }catch (IOException i){
        logger.error("Exception happened!",i);
      }
    }
    return resultObject;
  }

  public JSONObject executePost(URI uri,AbstractHttpEntity entity) {
    JSONObject resultObject = null;
    CloseableHttpResponse closeableHttpResponse = null;
    try {
      HttpPost httpPost = new HttpPost(uri);
      httpPost.setEntity(entity);
      closeableHttpResponse = closeableHttpClient.execute(httpPost, new BasicHttpContext());
      int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 201 || responseCode == 204){
        resultObject = new JSONObject(EntityUtils.toString(closeableHttpResponse.getEntity()));
      }else {
        logger.debug("Request failed" + responseCode);
        logger.debug("Message is "+closeableHttpResponse.getStatusLine().getReasonPhrase());
      }
      EntityUtils.consume(closeableHttpResponse.getEntity());
    }catch (Exception e) {
    logger.error("Exception happened!",e);
    }finally {
      try {
        closeableHttpResponse.close();
      }catch (IOException i){
        logger.error("Exception happened!",i);
      }
    }
    return resultObject;
  }

  public JSONObject executePut(URI uri,AbstractHttpEntity entity) {
    JSONObject resultObject = null;
    CloseableHttpResponse closeableHttpResponse = null;
    try {
      HttpPut httpPut = new HttpPut(uri);
      httpPut.setEntity(entity);
      closeableHttpResponse = closeableHttpClient.execute(httpPut , new BasicHttpContext());
      int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 201 || responseCode == 204){
        resultObject = new JSONObject(EntityUtils.toString(closeableHttpResponse.getEntity()));
      }else {
        logger.debug("Request failed" + responseCode);
        logger.debug("Message is "+closeableHttpResponse.getStatusLine().getReasonPhrase());
      }
      EntityUtils.consume(closeableHttpResponse.getEntity());
    }catch (Exception e){
      logger.error("Exception happened!",e);
    }finally {
      try {
        closeableHttpResponse.close();
      }catch (IOException i){
        logger.error("Exception happened!",i);
      }
    }
    return resultObject;
  }

  /**
   * only supporting delete without data
   * @param uri uri which is getting deleted
   */
  public void executeDelete(URI uri) {
    CloseableHttpResponse closeableHttpResponse = null;
    try {
      HttpDelete httpDelete = new HttpDelete(uri);
      closeableHttpResponse  = closeableHttpClient.execute(httpDelete, new BasicHttpContext());
      int responseCode = closeableHttpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 204){
        logger.info("deleted successfully");
        logger.info("response is " + EntityUtils.toString(closeableHttpResponse.getEntity()));
      }else {
        logger.error("Request failed " + responseCode);
        logger.error("Message is " + closeableHttpResponse.getStatusLine());
      }
      EntityUtils.consume(closeableHttpResponse.getEntity());
    }catch (Exception e){
      logger.error("Exception happened!",e);
    }finally {
      try {
        closeableHttpResponse.close();
      }catch (IOException i){
        logger.error("Exception happened!",i);
      }
    }
  }

}
