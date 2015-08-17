package clusters.updates.redis;

import clusters.create.LObject.CatalogTree;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Subscriber extends JedisPubSub {

    private Logger logger = LoggerFactory.getLogger(Subscriber.class);

    @Override
    public void onMessage(String channel,  String message){
        if(channel.contentEquals("store_update")) {
            JSONObject messageObject = new JSONObject(message);
            System.out.println("online stores are "+ messageObject.getJSONObject("online_stores"));
            System.out.println("offline stores are "+ messageObject.getJSONObject("offline_stores"));
            System.out.println("inventory change in stores "+ messageObject.getJSONObject("inventory_update"));

            //handle online stores ;; make the clusters online


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

    public void handleInventoryAddEvent(String message){
        try {
            JSONObject inventoryAddObject = new JSONObject(message);
            String storeId = inventoryAddObject.getString("store_id");
            List<String> clusterIds = getClustersWithStoreId(storeId);

            //Update clusters product coverage, sub cat coverage, category Tree
            //TODO
            //Make sure listing is already consumed this event;
            for(String clusterId : clusterIds){



                //get product coverage

                //get sub category coverage

                //get category tree

                //update the cluster

            }
        }catch (Exception e){
            e.printStackTrace();
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


    public HashMap<String,String> getCoverageOfCluster(String clusterId){

        HashMap<String,String> coverageMap = new HashMap<String, String>();
        coverageMap.put("product_coverage", 0 + "");
        coverageMap.put("sub_cat_coverage", 0 + "");
        coverageMap.put("cat_tree", "{}");
        try {
            String[] stores  = clusterId.split("-");
            String storeIdString = "";
            for(String s: stores){
                storeIdString += "\""+s+"\",";
            }
            storeIdString = storeIdString.substring(0,storeIdString.length()-1);
            String query = "{\"size\":0,\"query\":{\"terms\":{\"store.id\":["+storeIdString+"]}}," +
                    "\"aggregations\":{\"product_coverage\":{\"cardinality\":{\"field\":\"product.id\"}}," +
                    "\"sub_cat_coverage\":{\"cardinality\":{\"field\":\"product.sub_cat_id\"}}," +
                    "\"super_categories\":{\"terms\":{\"field\":\"product.sup_cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"categories\":{\"terms\":{\"field\":\"product.cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"sub_categories\":{\"terms\":{\"field\":\"product.sub_cat_id\",\"size\":0}," +
                    "\"aggregations\":{\"products_count\":{\"cardinality\":{\"field\":\"product.id\"}}}}}}}}}}";
            String ESAPI = "http://localhost:9200/listing/_search" ;
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ESAPI);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            JSONObject resultObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            resultObject = resultObject.getJSONObject("hits");
            JSONObject aggrs = resultObject.getJSONObject("aggregations");
            int subCatCov = aggrs.getJSONObject("sub_cat_coverage").getInt("value");
            int productCov = aggrs.getJSONObject("product_coverage").getInt("value");
            CatalogTree catalogTree = createCatTree(aggrs.getJSONObject("super_categories"));


        }catch (Exception e){
            e.printStackTrace();
        }
        return coverageMap;
    }

    public CatalogTree createCatTree(JSONObject catTreeObject){
        CatalogTree catalogTree = new CatalogTree();
        JSONArray supCatBuckets = catTreeObject.getJSONArray("buckets");
        for(int i=0;i<supCatBuckets.length();i++){
            JSONObject superCatObj = supCatBuckets.getJSONObject(i);
            int supCatId = superCatObj.getInt("key");
            JSONArray catBuckets = superCatObj.getJSONObject("categories").getJSONArray("buckets");
            for(int j = 0;j<catBuckets.length();j++){

            }
        }
        return null;


    }
}
