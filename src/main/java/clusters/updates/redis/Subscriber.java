package clusters.updates.redis;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.util.*;

public class Subscriber extends JedisPubSub {

    private Logger logger = LoggerFactory.getLogger(Subscriber.class);
    private Set<String> updatedStores;

    @Override
    public void onMessage(String channel,  String message){
        if(channel.contentEquals("store_update")) {
            JSONObject messageObject = new JSONObject(message);
            System.out.println("online stores are "+ messageObject.getJSONObject("online_stores"));
            System.out.println("offline stores are "+ messageObject.getJSONObject("offline_stores"));
            System.out.println("inventory change in stores "+ messageObject.getJSONObject("inventory_update"));

            //handle online stores ;; make the clusters online
            Set<String> onlineClusters = new HashSet<>();
            JSONObject onlineStores = messageObject.getJSONObject("online_stores");
            String[] onlineStoreIds = onlineStores.getString("store_ids").split(",");
            for(String storeId : onlineStoreIds){
                List<String> clusters = getClustersWithStoreId(storeId);
                onlineClusters.addAll(clusters);
            }

            //handle offline stores
            Set<String> offlineClustes = new HashSet<>();
            JSONObject offlineStores = messageObject.getJSONObject("offline_stores");
            String[] offlineStoreIds = offlineStores.getString("store_ids").split(",");
            for(String storeId : offlineStoreIds) {
                List<String> clusters = getClustersWithStoreId(storeId);
                offlineClustes.addAll(clusters);
            }

            //handle inventory change
            Set<String> updateClusters = new HashSet<>();
            JSONObject updatedStores = messageObject.getJSONObject("inventory_update");
            String[] updatedStoreIds = updatedStores.getString("store_ids").split(",");
            for(String storeId : updatedStoreIds){
                List<String> clusters = getClustersWithStoreId(storeId);
                updateClusters.addAll(clusters);
            }

            //

        }
    }
    @Override
        public void onPMessage(String pattern, String channel, String message) {
        }
    @Override
        public void onSubscribe(String channel, int subscribedChannels) {
    }
    @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
    }
    @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }
    @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
    }


    public void handleUpdates(Set<String> onlineClusters, Set<String>offlineClusters, Set<String> updatedClusters){

        //we will do a one bulk call
        StringBuilder ss= new StringBuilder();
        for(String onlineCluster : onlineClusters){
            JSONObject clusterObject = new JSONObject();
            clusterObject.put("is_online","true");
            ss.append("{\"update\" : {\"_id\":\""+onlineCluster+"\"}}");
            ss.append("\n");
            ss.append(clusterObject.toString());
            ss.append("\n");

        }
        for(String offlineCluster : offlineClusters){
            JSONObject clusterObject = new JSONObject();
            clusterObject.put("is_online","offline");
            ss.append("{\"index\" : {\"_id\":\""+offlineCluster+"\"}}");
            ss.append("\n");
            ss.append(clusterObject.toString());
            ss.append("\n");
        }
        for(String updatedCluster : updatedClusters){
            JSONObject clusterObject = new JSONObject();
            HashMap<String, Integer> clusterCoverage = getCoverageOfCluster(updatedCluster);



        }
    }


    public List<String> getClustersWithStoreId(String storeId){

        List<String> clusterIdList = new ArrayList<String>();
        try {
            String clusterAPI = "http://localhost:9200/live_geo_clusters/";
            String query = "{\"_source\":\"false\",\"query\":{\"term\":{\"stores.store_id\":\""+storeId+"\"}}}";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(clusterAPI);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            System.out.println("status is "+httpResponse.getStatusLine().getStatusCode());
            JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            result = result.getJSONObject("hits");
            JSONArray hitsArray = result.getJSONArray("hits");
            for(int i=0;i<hitsArray.length();i++){
                JSONObject thisObject = hitsArray.getJSONObject(i);
                String clusterId = thisObject.getString("_id");
                clusterIdList.add(clusterId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return clusterIdList;
    }


    public HashMap<String,Integer> getCoverageOfCluster(String clusterId){

        HashMap<String,Integer> coverageMap = new HashMap<String, Integer>();
        coverageMap.put("product_coverage", 0 );
        coverageMap.put("sub_cat_coverage", 0 );
        try {
            String ESAPI = "http://localhost:9200/listing/_search" ;
            String[] stores  = clusterId.split("-");
            String storeIdString = "";
            for(String s: stores){
                storeIdString += "\""+s+"\",";
            }
            storeIdString = storeIdString.substring(0,storeIdString.length()-1);
            String query_with_cat_tree= "{\"size\":0,\"query\":{\"terms\":{\"store.id\":["+storeIdString+"]}}," +
                    "\"aggregations\":{\"product_coverage\":{\"cardinality\":{\"field\":\"product.id\"}}," +
                    "\"sub_cat_coverage\":{\"cardinality\":{\"field\":\"product.sub_cat_id\"}}," +
                    "\"super_categories\":{\"terms\":{\"field\":\"product.sup_cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"categories\":{\"terms\":{\"field\":\"product.cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"sub_categories\":{\"terms\":{\"field\":\"product.sub_cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"products_count\":{\"cardinality\":{\"field\":\"product.id\"}}}}}}}}}}";

            String query= "{\"size\":0,\"query\":{\"terms\":{\"store.id\":[\""+storeIdString+"\"]}}," +
                    "\"aggregations\":{\"product_coverage\":{\"cardinality\":{\"field\":\"product.id\"}}," +
                    "\"sub_cat_coverage\":{\"cardinality\":{\"field\":\"product.sub_cat_id\"}}}}";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ESAPI);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            JSONObject resultObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            resultObject = resultObject.getJSONObject("hits");
            JSONObject aggrs = resultObject.getJSONObject("aggregations");
            int subCatCov = aggrs.getJSONObject("sub_cat_coverage").getInt("value");
            int productCov = aggrs.getJSONObject("product_coverage").getInt("value");
            coverageMap.put("sub_cat_cov",subCatCov);
            coverageMap.put("product_cov",productCov);
        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageMap;
    }

    public static void main(String[] args) {
        Subscriber subscriber = new Subscriber();
        subscriber.getCoverageOfCluster("100023");
    }

}
