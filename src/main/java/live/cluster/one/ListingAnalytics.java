package live.cluster.one;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by gurramvinay on 8/3/15.
 */
public class ListingAnalytics {

    public void getAggr(String geoHash) throws UnsupportedEncodingException {
        try {
            String query = "{ \"size\": 0, \"query\": { \"filtered\": { \"query\": { \"match_all\": {} }, \"filter\": " +
                    "{ \"geo_distance\": { \"distance\": \"3km\", \"store_details.location\": \""+geoHash+"\" } } } }," +
                    " \"aggregations\": { \"sub_cat\": { \"terms\": { \"field\": \"product_details.sub_category_id\" } } } }";
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
    }
}
