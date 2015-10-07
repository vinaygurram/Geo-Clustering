package com.olastore.listing.clustering.clients;


import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * @author gurramvinay
 */
public class ClustersHttpClientFactory {

  private static PoolingHttpClientConnectionManager httpClientPoolManager;
  private static ConnectionKeepAliveStrategy keepAliveStrategy;
  private static ClustersHttpClient clustersHttpClient;

  private static final int TIME_OUT = 5;

  protected ClustersHttpClientFactory() {
    init();
  }

  private void init() {
    createConnectionPool();
    createKeepAliveStrategy();
  }

  public ClustersHttpClient getHttpClient(){
    if(clustersHttpClient ==null){
      CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(httpClientPoolManager).setKeepAliveStrategy(keepAliveStrategy).build();
      clustersHttpClient = new ClustersHttpClient(closeableHttpClient);
    }
    return clustersHttpClient;
  }

  private void createConnectionPool() {

    httpClientPoolManager = new PoolingHttpClientConnectionManager();
    httpClientPoolManager.setMaxTotal(15);
    httpClientPoolManager.setDefaultMaxPerRoute(10);

  }

  private void createKeepAliveStrategy() {

    keepAliveStrategy = new ConnectionKeepAliveStrategy() {
      @Override
      public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        HeaderElementIterator it = new BasicHeaderElementIterator
            (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
          HeaderElement he = it.nextElement();
          String param = he.getName();
          String value = he.getValue();
          if (value != null && param.equalsIgnoreCase
              ("timeout")) {
            return Long.parseLong(value) * 1000;
          }
        }
        return TIME_OUT * 1000;
      }
    };
  }

}
