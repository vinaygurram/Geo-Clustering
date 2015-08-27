package clusters.create;

import clusters.create.objects.ClusterObj;
import clusters.create.objects.ClusteringPoint;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import clusters.create.objects.Geopoint;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread to from clusters given the store ids
 * This will push the results into ES as well
 * Created by gurramvinay on 7/1/15.
 */
public class SimpleWorkerThread implements  Runnable{
    String geohash;
    SimpleWorkerThread(String geohash){
        this.geohash = geohash;

    }

    public void run() {
        List<String>points = getClusetringPointsForGeoHash(geohash);
        if(points.size()==0) return;
        ClusterStrategy clusterStrategyNew = new ClusterStrategy();
        LatLong gll = GeoHash.decodeHash(geohash);
        Geopoint geopoint = new Geopoint(gll.getLat(),gll.getLon());
        List<ClusterObj> clusterObjList = clusterStrategyNew.createClusters(geopoint, points);
        if(clusterObjList.size()>0) pushClusterToES(clusterObjList);
        if(GeoClustering.jobsRun.getAndIncrement()%50==0)System.out.println(GeoClustering.jobsRun);

    }


    /**
     * Calls listing index to find stores within 6kms
     * Calls stores index to get lat,long values for stores
     * */
    public List<String> getClusetringPointsForGeoHash(String geohash){
        List<String> reShops = new ArrayList<String>();
        try {
            String uri = GeoClustering.ES_REST_API +"/"+GeoClustering.LISTING_INDEX+"/"+ GeoClustering.ES_SEARCH_END_POINT;

            HttpPost postRequest = new HttpPost(uri);
            String ssd ="{\"size\":0,\"query\":{\"filtered\":{\"filter\":{\"geo_distance\":{\"distance\":\"6km\"," +
                    "\"store_details.location\":\""+geohash+"\"}}}},\"aggregations\":{\"stores_unique\":{\"terms\":{\"field\":\"store_details.id\",\"size\":0}}}}";

            postRequest.setEntity(new StringEntity(ssd));
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(postRequest);
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
            jsonObject = jsonObject.getJSONObject("aggregations");
            jsonObject = jsonObject.getJSONObject("stores_unique");
            JSONArray stores = jsonObject.getJSONArray("buckets");
            for(int i=0;i<stores.length();i++){

                //Get location
                String id = stores.getJSONObject(i).getInt("key")+"";
                ClusteringPoint clusteringPoint;
                boolean is_store_exists = false;
                if(GeoClustering.clusterPoints.containsKey(id)){
                    is_store_exists = true;
                }else {

                    try {
                        URL url = new URL(GeoClustering.ES_REST_API +"/"+GeoClustering.STORES_INDEX+"/"+GeoClustering.STORES_INDEX_TYPE+"/"+id);
                        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                        JSONObject response2 = new JSONObject(IOUtils.toString(httpURLConnection.getInputStream()));
                        JSONObject response1 = response2.getJSONObject("_source").getJSONObject("store_details");
                        if(!(response2.getBoolean("found")==false || response1.getString("store_state").contentEquals("active"))) continue;
                        double lat = response1.getJSONObject("location").getDouble("lat");
                        double lng = response1.getJSONObject("location").getDouble("lon");
                        clusteringPoint = new ClusteringPoint(id,new Geopoint(lat,lng));
                        GeoClustering.clusterPoints.put(id,clusteringPoint);
                        is_store_exists = true;

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(is_store_exists)reShops.add(id);

            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return reShops;
    }



    public void pushClusterToES(List<ClusterObj> clusterObjs){
        HttpClient httpClient;

        try {
            JSONObject geoDoc = new JSONObject();
            geoDoc.put("id", clusterObjs.get(0).getGeoHash());
            geoDoc.put("clusters_count", clusterObjs.size());
            JSONArray clusters = new JSONArray();
            geoDoc.put("clusters", clusters);

            for(ClusterObj clusterObj : clusterObjs){
                List<String> stringList = clusterObj.getPoints();
                Collections.sort(stringList);
                StringBuilder sb = new StringBuilder();
                for(String s : stringList){
                    sb.append("-");
                    sb.append(s);
                }
                String hash = sb.toString().substring(1);

                JSONObject thisCluster = new JSONObject();
                thisCluster.put("cluster_id", hash);
                thisCluster.put("distance", clusterObj.getDistance());
                thisCluster.put("status", clusterObj.isStatus());
                thisCluster.put("rank",clusterObj.getRank());
                clusters.put(thisCluster);

                if(GeoClustering.pushedClusters.contains(hash)){
                }else {

                    String uri = GeoClustering.ES_REST_API +"/"+GeoClustering.CLUSTERS_INDEX+"/"+GeoClustering.CLUSTERS_INDEX_TYPE+"/"+hash;
                    HttpPost postRequest = new HttpPost(uri);
                    String jsonString = clusterObj.getJSON().toString();
                    postRequest.setEntity(new StringEntity(jsonString));
                    //send post request
                    httpClient = HttpClientBuilder.create().build();
                    HttpResponse response = httpClient.execute(postRequest);
                    int code = response.getStatusLine().getStatusCode();
                    if(code!=200 && code!= 201){
                        System.out.println(response.getEntity().toString());
                        System.out.println("Error --3");
                    }
                    GeoClustering.pushedClusters.add(hash);
                }
            }

            HttpPost httpPost = new HttpPost(GeoClustering.ES_REST_API +"/"+ GeoClustering.GEO_HASH_INDEX+"/"+GeoClustering.GEO_HASH_INDEX_TYPE+"/"+clusterObjs.get(0).getGeoHash());
            httpPost.setEntity(new StringEntity(geoDoc.toString()));
            httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int code = httpResponse.getStatusLine().getStatusCode();
            if(!(code==200 || code==201)) {

                System.out.println(httpResponse.getEntity().toString());
                System.out.println("Error --2");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
