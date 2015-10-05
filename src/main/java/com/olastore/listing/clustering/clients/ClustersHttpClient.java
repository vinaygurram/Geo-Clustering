package com.olastore.listing.clustering.clients;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

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
    try {
      HttpGet httpGet = new HttpGet(uri);
      HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200){
        resultObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
      }else {
        logger.debug("Request failed "+ responseCode);
        logger.debug("Message is"+ httpResponse.getStatusLine().getReasonPhrase());
      }
    }catch (Exception e){
      logger.error(e.getMessage());
    }finally {
      try {
        closeableHttpClient.close();
      }catch (IOException i){
        logger.info(i.getMessage());
      }
    }
    return resultObject;
  }

  public JSONObject executePost(URI uri,String data) {
    JSONObject resultObject = null;
    try {
      HttpPost httpPost = new HttpPost(uri);
      httpPost.setEntity(new StringEntity(data, Charset.defaultCharset()));
      HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 201 || responseCode == 204){
        resultObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
      }else {
        logger.debug("Request failed" + responseCode);
        logger.debug("Message is "+httpResponse.getStatusLine().getReasonPhrase());
      }
    }catch (Exception e){
      logger.error(e.getMessage());
    }finally {
      try {
        closeableHttpClient.close();
      }catch (IOException i){
        logger.error(i.getMessage());
      }
    }
    return resultObject;
  }

  public JSONObject executePut(URI uri,String data) {
    JSONObject resultObject = null;
    try {
      HttpPut httpPut = new HttpPut(uri);
      httpPut.setEntity(new StringEntity(data, Charset.defaultCharset()));
      HttpResponse httpResponse = closeableHttpClient.execute(httpPut);
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 201 || responseCode == 204){
        resultObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
      }else {
        logger.debug("Request failed" + responseCode);
        logger.debug("Message is "+httpResponse.getStatusLine().getReasonPhrase());
      }
    }catch (Exception e){
      logger.error(e.getMessage());
    }finally {
      try {
        closeableHttpClient.close();
      }catch (IOException i){
        logger.error(i.getMessage());
      }
    }
    return resultObject;
  }

  /**
   * only supporting delete without data
   * @param uri uri which is getting deleted
   */
  public void executeDelete(URI uri) {
    try {
      HttpDelete httpDelete = new HttpDelete(uri);
      HttpResponse httpResponse = closeableHttpClient.execute(httpDelete);
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if(responseCode == 200 || responseCode == 204){
        logger.debug("deleted successfully");
      }else {
        logger.debug("Request failed "+ responseCode);
        logger.debug("Message is"+ httpResponse.getStatusLine().getReasonPhrase());
      }
    }catch (Exception e){
      logger.error(e.getMessage());
    }finally {
      try {
        closeableHttpClient.close();
      }catch (IOException i){
        logger.info(i.getMessage());
      }
    }
  }


}
