package live.cluster.one;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

/**
 * Created by gurramvinay on 7/11/15.
 */
public class CSVGenerator {

    public void getESData(){

        try {

            FileWriter fileWriter = new FileWriter("src/main/resources/esData.csv");
            //fileWriter.write("id,geoHash,rank,sub_cat_count,product_count,distance,stores_count,shop_ids");
            fileWriter.write("id,sub_cat_count,product_count,cluster_ids,distances");
            fileWriter.write("\n");
            HttpClient httpClient = HttpClientBuilder.create().build();
            int from =0;
            while(from<461000){
                String query = "{\"size\": 10000,\"from\":"+from+",\"fields\":[\"clusterOB.distance\",\"clusterOB.product_count\",\"clusterOB.geohash\",\"clusterOB.rank\",\"clusterOB.sub_cat_count\",\"store.shop_ids\",\"clusterOB.stores_count\",\"clusterOB.shop_ids\"], \"query\": {\"match_all\": {}}}";
                //query = "{\"size\": 10000,\"from\":"+from+",\"fields\":[\"clusterOB.distance\",\"clusterOB.product_count\",\"clusterOB.geohash\",\"clusterOB.rank\",\"clusterOB.sub_cat_count\",\"store.shop_ids\",\"clusterOB.stores_count\",\"clusterOB.shop_ids\"], \"query\": {\"match_all\": {}}}";
                query = "{\"size\": 10000, \"query\": { \"match_all\": {} }, \"fields\": [ \"sub_cat_count\", \"clusters.cluster_id\", \"id\", \"product_count\",\"clusters.distance\" ] }";
                String ES_API = "http://localhost:9200/live_geo_clusters_new3/_search";
                ES_API = "http://localhost:9200/geo_hash/_search";

                HttpPost httpPost = new HttpPost(ES_API);
                httpPost.setEntity(new StringEntity(query));
                HttpResponse response = httpClient.execute(httpPost);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                JSONObject jsonObject = new JSONObject(result.toString());
                JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
//                for(int i=0;i<jsonArray.length();i++){
//                    JSONObject fieldsObject = jsonArray.getJSONObject(i).getJSONObject("fields");
//                    String id = jsonArray.getJSONObject(i).getString("_id");
//                    String geoHash  = fieldsObject.getJSONArray("clusterOB.geohash").getString(0);
//                    double rank = fieldsObject.getJSONArray("clusterOB.rank").getDouble(0);
//                    int sub_cat_count = fieldsObject.getJSONArray("clusterOB.sub_cat_count").getInt(0);
//                    int product_count = fieldsObject.getJSONArray("clusterOB.product_count").getInt(0);
//                    double distance = fieldsObject.getJSONArray("clusterOB.distance").getDouble(0);
//                    int store_count = fieldsObject.getJSONArray("clusterOB.stores_count").getInt(0);
//                    JSONArray store_ids = fieldsObject.getJSONArray("clusterOB.shop_ids");
//                    String shop_ids = store_ids.getString(0)+":"+store_ids.getString(1);
//                    //double
//                    fileWriter.write(id+","+geoHash+","+rank+","+sub_cat_count+","+product_count+","+distance+","+store_count+","+shop_ids);
//                    fileWriter.write("\n");
//
//                }
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i).getJSONObject("fields");
                    String id  = jsonObject1.getJSONArray("id").getString(0);
                    String suCount  = jsonObject1.getJSONArray("sub_cat_count").getInt(0)+"";
                    String pCount  = jsonObject1.getJSONArray("product_count").getInt(0)+"";
                    JSONArray clusters  = jsonObject1.getJSONArray("clusters.cluster_id");
                    String clusterId="";
                    for(int k=0;k<clusters.length();k++){
                        clusterId +="##"+clusters.getString(k);
                    }
                    clusterId = clusterId.substring(1);
                    JSONArray distanceArray = jsonObject1.getJSONArray("clusters.distance");
                    String distances = "";
                    for(int k=0;k<distanceArray.length();k++){
                        distances += "-"+distanceArray.getDouble(k);
                    }
                    distances = distances.substring(1);
                    fileWriter.write(id+","+suCount+","+pCount+","+clusterId+","+distances);
                    fileWriter.write("\n");
                }
                from+=10000;

            }
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void getESData1(){

        try {

            FileWriter fileWriter = new FileWriter("src/main/resources/esData1.csv");
            //fileWriter.write("id,geoHash,rank,sub_cat_count,product_count,distance,stores_count,shop_ids");
            fileWriter.write("id,sub_cat_count,product_count,store_ids");
            fileWriter.write("\n");
            HttpClient httpClient = HttpClientBuilder.create().build();
            int from =0;
            while(from<30000){
                String query = "{ \"size\": 10000, \"query\": { \"match_all\": {} }, \"fields\": [ \"sub_cat_count\", \"product_count\", \"stores.store_id\" ] }";
                String ES_API = "http://localhost:9200/live_geo_clusters/_search";

                HttpPost httpPost = new HttpPost(ES_API);
                httpPost.setEntity(new StringEntity(query));
                HttpResponse response = httpClient.execute(httpPost);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                JSONObject jsonObject = new JSONObject(result.toString());
                JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
//                for(int i=0;i<jsonArray.length();i++){
//                    JSONObject fieldsObject = jsonArray.getJSONObject(i).getJSONObject("fields");
//                    String id = jsonArray.getJSONObject(i).getString("_id");
//                    String geoHash  = fieldsObject.getJSONArray("clusterOB.geohash").getString(0);
//                    double rank = fieldsObject.getJSONArray("clusterOB.rank").getDouble(0);
//                    int sub_cat_count = fieldsObject.getJSONArray("clusterOB.sub_cat_count").getInt(0);
//                    int product_count = fieldsObject.getJSONArray("clusterOB.product_count").getInt(0);
//                    double distance = fieldsObject.getJSONArray("clusterOB.distance").getDouble(0);
//                    int store_count = fieldsObject.getJSONArray("clusterOB.stores_count").getInt(0);
//                    JSONArray store_ids = fieldsObject.getJSONArray("clusterOB.shop_ids");
//                    String shop_ids = store_ids.getString(0)+":"+store_ids.getString(1);
//                    //double
//                    fileWriter.write(id+","+geoHash+","+rank+","+sub_cat_count+","+product_count+","+distance+","+store_count+","+shop_ids);
//                    fileWriter.write("\n");
//
//                }
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i).getJSONObject("fields");
                    String id = jsonArray.getJSONObject(i).getString("_id");
                    String suCount  = jsonObject1.getJSONArray("sub_cat_count").getInt(0)+"";
                    String pCount  = jsonObject1.getJSONArray("product_count").getInt(0)+"";
                    JSONArray stores  = jsonObject1.getJSONArray("stores.store_id");
                    String clusterId="";
                    for(int k=0;k<stores.length();k++){
                        clusterId +="##"+stores.getString(k);
                    }
                    clusterId = clusterId.substring(2);
                    fileWriter.write(id+","+suCount+","+pCount+","+clusterId);
                    fileWriter.write("\n");
                }
                from+=10000;

            }
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        CSVGenerator csvGenerator = new CSVGenerator();
        csvGenerator.getESData1();
    }

}

