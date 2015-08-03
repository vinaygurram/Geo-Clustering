package live.cluster.one;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by gurramvinay on 8/3/15.
 */
public class ListingAnalytics {

    private int getSubCat(String geoHash) throws UnsupportedEncodingException {
        try {
            String query = "{ \"size\": 0, \"query\": { \"filtered\": { \"query\": { \"match_all\": {} }, \"filter\": " +
                    "{ \"geo_distance\": { \"distance\": \"3km\", \"store_details.location\": \""+geoHash+"\" } } } }," +
                    " \"aggregations\": { \"sub_cat\": { \"terms\": { \"field\": \"product.sub_category_id\" } } } }";
            String ES_API = "http://localhost:9200/listing/_search";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ES_API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String response = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(response);

        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }


    private int getProducts(String geoHash){

        try {
            String query = "{ \"size\": 0, \"query\": { \"filtered\": { \"query\": { \"match_all\": {} }, \"filter\": " +
                    "{ \"geo_distance\": { \"distance\": \"3km\", \"store_details.location\": \""+geoHash+"\" } } } }," +
                    " \"aggregations\": { \"sub_cat\": { \"terms\": { \"field\": \"product.id\" } } } }";
            String ES_API = "http://localhost:9200/listing/_search";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ES_API);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String response = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonObject = new JSONObject(response);

        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public void writeCSV(List<String> geoHashList){

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File("src/main/resources/coverageIn3km.csv"));
            fileWriter.write("geo_hash,product_count,sub_cat_count");
            for(String s: geoHashList){
                int productCount = getProducts(s);
                int subCatCount = getSubCat(s);
                fileWriter.write(s+","+productCount+","+subCatCount);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fileWriter.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
