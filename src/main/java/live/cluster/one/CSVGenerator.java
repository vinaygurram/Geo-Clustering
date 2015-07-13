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
            fileWriter.write("id,geoHash,rank,sub_cat_count,product_count,distance");
            fileWriter.write("\n");
            HttpClient httpClient = HttpClientBuilder.create().build();
            int from =0;
            while(from<110000){
                String query = "{\"size\": 10000,\"from\":"+from+",\"fields\":[\"clusterOB.distance\",\"clusterOB.product_count\",\"clusterOB.geohash\",\"clusterOB.rank\",\"clusterOB.sub_cat_count\",\"store.shop_ids\"], \"query\": {\"match_all\": {}}}";
                String ES_API = "http://localhost:9200/live_geo_clusters_new1/_search";

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
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject fieldsObject = jsonArray.getJSONObject(i).getJSONObject("fields");
                    String id = jsonArray.getJSONObject(i).getString("_id");
                    String geoHash  = fieldsObject.getJSONArray("clusterOB.geohash").getString(0);
                    double rank = fieldsObject.getJSONArray("clusterOB.rank").getDouble(0);
                    int sub_cat_count = fieldsObject.getJSONArray("clusterOB.sub_cat_count").getInt(0);
                    int product_count = fieldsObject.getJSONArray("clusterOB.product_count").getInt(0);
                    double distance = fieldsObject.getJSONArray("clusterOB.distance").getDouble(0);
                    fileWriter.write(id+","+geoHash+","+rank+","+sub_cat_count+","+product_count+","+distance);
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

