package old.core;

import old.Util.Location;
import old.Util.LocationWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gurramvinay on 6/12/15.
 */
public class ESDataHandler {

    private final static Logger logger = LoggerFactory.getLogger(ESDataHandler.class);
    private final static String ESAPI = "http://es.qa.olahack.in/stores/";

    public List<LocationWrapper> getLocationData(){
        List<LocationWrapper> locationWrapperList = new ArrayList<LocationWrapper>();
        try {

            //String testString= "{\"size\":\"20\",\"from\":\"0\",\"query\":{\"match_all\":{}},\"aggs\":{\"stores_agg\":{\"terms\":{\"field\":\"store_details.id\"}}}}";
            String queryString= "{\"size\":100,\"query\":{\"match_all\":{}}}";
            String results = getESResults(queryString);

            JSONObject resultObject = new JSONObject(results);
            JSONArray hits = resultObject.getJSONObject("hits").getJSONArray("hits");

            for(int i=0;i<hits.length();i++){
                JSONObject joo = hits.getJSONObject(i).getJSONObject("_source").getJSONObject("store_details");
                logger.info(joo.getJSONObject("location").toString());
                locationWrapperList.add(new LocationWrapper(new Location(joo.getJSONObject("location").getDouble("lat"),joo.getJSONObject("location").getDouble("lon"))));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return locationWrapperList;
    }



    public String getESResults(String qString){
        HttpClient httpClient = null;
        StringBuffer result = new StringBuffer();
       try {
            String uri = ESAPI+"_search";
            httpClient = HttpClientBuilder.create().build();

            HttpPost postRequest = new HttpPost(uri);
            postRequest.setHeader("Content-Type","application/json");
            postRequest.setEntity(new StringEntity(qString));

            //send post request
            HttpResponse response = httpClient.execute(postRequest);

            logger.info("\nSending 'POST' request to URL : " + uri);
            logger.info("Post parameters : " + postRequest.getEntity());
            logger.info("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
       }catch (IOException e){
           logger.error(e.getMessage(),e);
       }catch (JSONException e){
           logger.error(e.getMessage(),e);

       }catch (Exception e){
           logger.error(e.getMessage(),e);
       }finally {
           httpClient.getConnectionManager().shutdown();
       }
        return result.toString();
    }

    public static void main(String[] args){

        ESDataHandler dh = new ESDataHandler();

        dh.getLocationData();
    }

}
