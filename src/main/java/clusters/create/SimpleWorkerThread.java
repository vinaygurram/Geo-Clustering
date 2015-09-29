package clusters.create;

import clusters.create.objects.ClusterObj;
import clusters.create.objects.ClusteringPoint;
import clusters.create.objects.Geopoint;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread to from clusters given the store ids
 * This will push the results into ES as well
 * Created by gurramvinay on 7/1/15.
 */
public class SimpleWorkerThread implements Callable<String>{
    String geohash;
    SimpleWorkerThread(String geohash){
        this.geohash = geohash;

    }

    /**
     * Calls listing index to find stores within 6kms
     * Calls stores index to get lat,long values for stores
     * */
    public List<String> getClusetringPointsForGeoHash(String geohash){
        List<String> reShops = new ArrayList<String>();
        try {
            String listing_serach_api = (String)GeoClustering.yamlMap.get("es_search_api");
            listing_serach_api = listing_serach_api.replace(":index_name",(String)GeoClustering.yamlMap.get("listing_index_name"));
            listing_serach_api = listing_serach_api.replace(":index_type",(String)GeoClustering.yamlMap.get("listing_index_type"));
            int cluster_radius = (Integer) GeoClustering.yamlMap.get("clusters_radius");
            HttpPost postRequest = new HttpPost(listing_serach_api);
            String ssd ="{\"size\":0,\"query\":{\"filtered\":{\"filter\":{\"geo_distance\":{\"distance\":\""+cluster_radius+"km\"," +
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
                        String  store_document_api = (String)GeoClustering.yamlMap.get("es_document_api");
                        store_document_api = store_document_api.replace(":index_name",(String)GeoClustering.yamlMap.get("stores_index_name"));
                        store_document_api = store_document_api.replace(":index_type",(String)GeoClustering.yamlMap.get("stores_index_type"));
                        store_document_api = store_document_api.replace(":id",id);
                        //URL url = new URL(GeoClustering.ES_REST_API +"/"+GeoClustering.STORES_INDEX+"/"+GeoClustering.STORES_INDEX_TYPE+"/"+id);
                        URL url = new URL(store_document_api);
                        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                        JSONObject response2 = new JSONObject(IOUtils.toString(httpURLConnection.getInputStream()));
                        GeoClustering.logger.info(" Response from ES for getting stores is "+response2);
                        JSONObject response1 = response2.getJSONObject("_source").getJSONObject("store_details");
                        if(!(!response2.getBoolean("found") || response1.getString("store_state").contentEquals("active"))) continue;
                        double lat = response1.getJSONObject("location").getDouble("lat");
                        double lng = response1.getJSONObject("location").getDouble("lon");
                        clusteringPoint = new ClusteringPoint(id,new Geopoint(lat,lng));
                        GeoClustering.clusterPoints.put(id,clusteringPoint);
                        is_store_exists = true;

                    }catch (Exception e){
                        GeoClustering.logger.error(e.getMessage());
                    }
                }
                if(is_store_exists)reShops.add(id);

            }
        }catch (IOException e){
            GeoClustering.logger.error(e.getMessage());
        }catch (JSONException e){
            GeoClustering.logger.error(e.getMessage());
        }catch (Exception e){
            GeoClustering.logger.error(e.getMessage());
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
                    String clusters_indexing_api = (String) GeoClustering.yamlMap.get("es_document_api") ;
                    clusters_indexing_api = clusters_indexing_api.replace(":index_name",(String)GeoClustering.yamlMap.get("clusters_index_name"));
                    clusters_indexing_api = clusters_indexing_api.replace(":index_type",(String)GeoClustering.yamlMap.get("clusters_index_type"));
                    clusters_indexing_api = clusters_indexing_api.replace(":id",hash);
                    HttpPost postRequest = new HttpPost(clusters_indexing_api);
                    String jsonString = clusterObj.getJSON().toString();
                    postRequest.setEntity(new StringEntity(jsonString));
                    //send post request
                    httpClient = HttpClientBuilder.create().build();
                    HttpResponse response = httpClient.execute(postRequest);
                    int code = response.getStatusLine().getStatusCode();
                    if(code!=200 && code!= 201){
                        GeoClustering.logger.error("Error in pushing geo hashes "+ response.getStatusLine());
                    }
                    GeoClustering.pushedClusters.add(hash);
                }
            }
            //make doc for pushing
            String thisDocAsString = "{\"index\" : {\"_index\" : \"" +(String)GeoClustering.yamlMap.get("geo_hash_index_name")+ "\",\"_type\" : \""
                    + (String)GeoClustering.yamlMap.get("geo_hash_index_type")+ "\",\"_id\":\""
                    + clusterObjs.get(0).getGeoHash() + "\" }}\n" +geoDoc.toString() + "\n";
            String maxString = "";

            synchronized (GeoClustering.bulkDoc){
                GeoClustering.bulkDoc.append(thisDocAsString);
                GeoClustering.bulkDocCount.incrementAndGet();
                if(GeoClustering.bulkDocCount.get()>500){
                    maxString = GeoClustering.bulkDoc.toString();
                    GeoClustering.bulkDoc = new StringBuilder();
                    GeoClustering.bulkDocCount =new AtomicInteger(0);
                }
            }

            if(!maxString.isEmpty()){
                httpClient = HttpClientBuilder.create().build();
                String geo_hashes_bulk_api = (String) GeoClustering.yamlMap.get("es_bulk_api");
                HttpPost httpPost = new HttpPost(geo_hashes_bulk_api);
                //HttpPost httpPost = new HttpPost(GeoClustering.ES_REST_API +"/"+ GeoClustering.ES_BULK_END_POINT);
                httpPost.setEntity(new StringEntity(maxString));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if(code!=200 && code!=201) {
                    GeoClustering.logger.error("Error in bulk indexing " + httpResponse.getStatusLine());
                }
            }

        }catch (Exception e){
            GeoClustering.logger.error("Something went wrong while pushing clusters "+ e.getMessage());
        }
    }

    @Override
    public String call() throws Exception {
        List<String>points = getClusetringPointsForGeoHash(geohash);
        if(points.size()==0) return "DONE for "+geohash+"-- no shops within the raidus";
        LatLong gll = GeoHash.decodeHash(geohash);
        Geopoint geopoint = new Geopoint(gll.getLat(),gll.getLon());
        List<ClusterObj> clusterObjList = new ClusterStrategy().createClusters(geopoint, points);
        if(clusterObjList.size()>0) pushClusterToES(clusterObjList);
        points = null;
        clusterObjList = null;
        if(GeoClustering.jobsRun.getAndIncrement()%50==0){
            GeoClustering.logger.info("Jobs run total is "+ GeoClustering.jobsRun);
        }
        return "DONE for "+geohash+ " -- "+clusterObjList.size()+" clusters made";
    }
}
