package reports;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gurramvinay on 7/11/15.
 */
public class CSVGenerator {
    public static HashMap<String, List<Integer>> clusterMap = new HashMap<String, List<Integer>>();

    public void getESData(){

        try {

            generateClusterData();
            FileWriter fileWriter = new FileWriter("src/main/resources/geo_hash.csv");
            fileWriter.write("id,cluster_sub_cat_count,cluster_product_count,geo_product_count,geo_sub_cat_count,cluster_id,clusters_count");
            fileWriter.write("\n");
            HttpClient httpClient = HttpClientBuilder.create().build();

            String API = "http://localhost:9200/geo_hash/_search";
            String query = "{\"from\": 0,\"size\" :40318,\"query\":{\"match_all\":{}}}";

            HttpPost httpPost = new HttpPost(API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse response = httpClient.execute(httpPost);
            JSONObject result =  new JSONObject(EntityUtils.toString(response.getEntity()));
            result = result.getJSONObject("hits");
            JSONArray hits = result.getJSONArray("hits");
            for(int i=0;i<hits.length();i++){
                JSONObject thisDocument = hits.getJSONObject(i).getJSONObject("_source");

                String id = thisDocument.getString("id");
                int geo_product_count = thisDocument.getInt("product_count");
                int geo_sub_cat_count = thisDocument.getInt("sub_cat_count");

                String clusterIdF = "NN";
                String clusterIdS = "NN";
                int product_count_f = -1;
                int product_count_s = -1;
                int sub_cat_count_f = -1;
                int sub_cat_count_s = -1;

                JSONArray clusters = thisDocument.getJSONArray("clusters");
                for(int j=0;j<clusters.length();j++){
                    JSONObject tempCluster = clusters.getJSONObject(j);
                    String tempId = tempCluster.getString("cluster_id");
                    int temp_p_count = clusterMap.get(tempId).get(0);
                    int temp_sub_cat_count =clusterMap.get(tempId).get(1);
                    if(temp_p_count>product_count_f){
                        product_count_s = product_count_f;
                        product_count_f = temp_p_count;
                        sub_cat_count_s = sub_cat_count_f;
                        sub_cat_count_f =temp_sub_cat_count;
                        clusterIdS = clusterIdF;
                        clusterIdF = tempId;

                    }else if(temp_p_count>product_count_s){
                        product_count_s = temp_p_count;
                        sub_cat_count_s = temp_sub_cat_count;
                        clusterIdS = tempId;
                    }
                }

                if(clusters.length()>2){
                    String ss="";
                }

                fileWriter.write(id+","+sub_cat_count_f+","+product_count_f+","+geo_product_count+","+geo_sub_cat_count+","+clusterIdF+","+clusters.length());
                fileWriter.write("\n");
                fileWriter.write(id+","+sub_cat_count_s+","+product_count_s+","+geo_product_count+","+geo_sub_cat_count+","+clusterIdS+","+clusters.length());
                fileWriter.write("\n");
            }

            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void generateClusterData(){
        String API = "http://localhost:9200/live_geo_clusters/_search";
        String query = "{\"size\":1000,\"fields\":[\"sub_cat_count\",\"product_count\"],\"query\":{\"match_all\":{}}}";
        try {

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse response = httpClient.execute(httpPost);
            JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
            result = result.getJSONObject("hits");
            JSONArray hits = result.getJSONArray("hits");
            for(int i=0;i<hits.length();i++){
                String id = hits.getJSONObject(i).getString("_id");
                JSONObject fields = hits.getJSONObject(i).getJSONObject("fields");
                int product_count = fields.getJSONArray("product_count").getInt(0);
                int sub_cat_count = fields.getJSONArray("sub_cat_count").getInt(0);
                List<Integer> countList = new ArrayList<Integer>();
                countList.add(product_count);
                countList.add(sub_cat_count);
                clusterMap.put(id,countList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void getESData1(){

        try {

            FileWriter fileWriter = new FileWriter("src/main/resources/clusters.csv");
            //fileWriter.write("id,geoHash,rank,sub_cat_count,product_count,distance,stores_count,shop_ids");
            fileWriter.write("sub_cat_count,product_count,stores,stores_count");
            fileWriter.write("\n");
            HttpClient httpClient = HttpClientBuilder.create().build();
            int from =0;
            while(from<30000){
                String query = "{ \"size\": 10000, \"query\": { \"match_all\": {} }, \"fields\": [ \"sub_cat_count\", \"product_count\",\"stores_count\" ] }";
                String ES_API = "http://localhost:9200/live_geo_clusters1/_search";

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
                    String stores_count  = jsonObject1.getJSONArray("stores_count").getInt(0)+"";
                    fileWriter.write(suCount+","+pCount+","+id+","+stores_count);
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
        csvGenerator.getESData();
    }

}

