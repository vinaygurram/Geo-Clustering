package live.cluster.one;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by gurramvinay on 8/3/15.
 */
public class ListingAnalytics {

    ListingAnalytics(FileWriter fileWriter){
        this.fileWriter = fileWriter;
    }

    public FileWriter fileWriter;

    private int getSubCat(String geoHash) throws UnsupportedEncodingException {
        try {
            String query = "{\"size\":0,\"query\":{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":" +
                    "{\"must\":[{\"geo_distance\":{\"distance\":\"3km\",\"store.location\":\""+geoHash+"\"}}," +
                    "{\"term\":{\"product.state\":\"available\"}}]}}}},\"aggregations\":{\"sub_cat\":{\"cardinality\":" +
                    "{\"field\":\"product.sub_cat_id\"}}}}";
            String ES_API = "http://localhost:9200/listing/_search";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ES_API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String response = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(response);
            jsonObject = jsonObject.getJSONObject("aggregations");
            jsonObject = jsonObject.getJSONObject("sub_cat");
            return jsonObject.getInt("value");



        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    private int getProducts(String geoHash){

        try {
            String query = "{\"size\":0,\"query\":{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":" +
                    "{\"must\":[{\"geo_distance\":{\"distance\":\"3km\",\"store.location\":\""+geoHash+"\"}}," +
                    "{\"term\":{\"product.state\":\"available\"}}]}}}},\"aggregations\":{\"product_count\":{\"cardinality\":" +
                    "{\"field\":\"product.id\"}}}}";

            String ES_API = "http://localhost:9200/listing/_search";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ES_API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String response = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(response);
            jsonObject = jsonObject.getJSONObject("aggregations");
            jsonObject = jsonObject.getJSONObject("product_count");
            return jsonObject.getInt("value");

        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public int writeCSV(List<String> geoHashList) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            fileWriter.write("geo_hash,product_count,sub_cat_count");
            fileWriter.write("\n");
            for (String s : geoHashList) {
                SThread temp = new SThread(s);
                executorService.execute(temp);
            }
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    class SThread implements Runnable {
        String geo;
        public SThread(String s ){
            geo = s;
        }
        public  void run(){
            try {
                int productCount = getProducts(geo);
                int subCatCount = getSubCat(geo);
                fileWriter.write(geo + "," + productCount + "," + subCatCount);
                fileWriter.write("\n");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
