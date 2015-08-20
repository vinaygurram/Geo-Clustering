package clusters.create;

import clusters.create.LObject.*;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gurramvinay on 6/16/15.
 */
public class GeoClustering {
    private static int GEO_PRECISION = 7;

    private static double BIG_BLR_TOP_LEFT_LAT =13.1245;
    private static double BIG_BLR_TOP_LEFT_LON =77.3664;
    private static double BIG_BLR_BOT_RIGHT_LAT =12.8342;
    private static double BIG_BLR_BOT_RIGHT_LON =77.8155;

    private static double BLR_TOP_LEFT_LAT =13.1109114;
    private static double BLR_TOP_LEFT_LON =77.4625397;
    private static double BLR_BOT_RIGHT_LAT =12.824522500000002;
    private static double BLR_BOT_RIGHT_LON =77.7495575;

    public  static ConcurrentHashMap<String,List<String>> map = new ConcurrentHashMap<String, List<String>>();
    public static ConcurrentHashMap<String ,ClusteringPoint> clusterPoints = new ConcurrentHashMap<String, ClusteringPoint>();
    public static ConcurrentHashMap<String ,List<ClusterObj>> computedClusters = new ConcurrentHashMap<String, List<ClusterObj>>();
    public static ArrayList<String> pushedClusters = new ArrayList<String>();
    public static HashMap<String,Integer> clusterProductCoverage = new HashMap<String, Integer>();
    public static HashMap<String,Integer> clusterSubCatCoverage = new HashMap<String, Integer>();


    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision){
        Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
                box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
        System.out.println(boxCoverage.toString());
        return boxCoverage.getHashes();
    }

    private BoundingBox getBangaloreBox(){
        Geopoint topleft = new Geopoint(BLR_TOP_LEFT_LAT,BLR_TOP_LEFT_LON);
        Geopoint botright = new Geopoint(BLR_BOT_RIGHT_LAT,BLR_BOT_RIGHT_LON);
        return new BoundingBox(topleft,botright);
    }

    public String getZone(String geoHash){

        LatLong latLong = GeoHash.decodeHash(geoHash);

        try {
            String geo_api = "http://geokit.qa.olahack.in/localities?lat="+latLong.getLat()+"&lng="+latLong.getLon()+"";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(geo_api);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            if(result.getString("status").contentEquals("SUCCESS")){
                if(result.has("locality") && !result.isNull("locality")){
                    return result.getJSONObject("locality").getString("name");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getBlrGeoHashes(){
        BoundingBox bbox = getBangaloreBox();
        Set<String> hashes = getGeoHashOfBoundingBox(bbox,GEO_PRECISION);
        Iterator<String> iterator = hashes.iterator();
        int i=0;
        List<String> geohashList = new ArrayList<String>();
        while(iterator.hasNext()){
            String thisHash = iterator.next();
            String zone = getZone(thisHash);
            if(!(zone.isEmpty() || zone.contentEquals(""))){
                geohashList.add(thisHash);
            }
            i++;
        }
        System.out.println("total number of hashes "+i);
        return geohashList;
    }

    /**
     * Calls listing index to find stores within 6kms
     * Calls stores index to get lat,long values for stores
     * */
     public List<String> getClusetringPointsForGeoHash(String geohash){
        List<String> reShops = new ArrayList<String>();
        HttpClient httpClient = null;
        try {
            String uri = "http://localhost:9200/listing/_search";
            httpClient = HttpClientBuilder.create().build();

            HttpPost postRequest = new HttpPost(uri);
            String ssd ="{\"size\":0,\"query\":{\"filtered\":{\"filter\":{\"geo_distance\":{\"distance\":\"6km\"," +
                    "\"store.location\":\""+geohash+"\"}}}},\"aggregations\":{\"stores_unique\":{\"terms\":{\"field\":\"store.id\"}}}}";

            postRequest.setEntity(new StringEntity(ssd));
            HttpResponse response = httpClient.execute(postRequest);
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
            jsonObject = jsonObject.getJSONObject("aggregations");
            jsonObject = jsonObject.getJSONObject("stores_unique");
            JSONArray stores = jsonObject.getJSONArray("buckets");
            for(int i=0;i<stores.length();i++){

                //Get location
                String id = stores.getJSONObject(i).getString("key");
                ClusteringPoint clusteringPoint;
                if(clusterPoints.containsKey(id)){
                }else {
                    URL url = new URL("http://localhost:9200/stores/store/"+id);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    JSONObject response1 = new JSONObject(IOUtils.toString(httpURLConnection.getInputStream()));
                    response1 = response1.getJSONObject("_source");
                    String lat_lng =response1.getString("location");
                    String[] latlng = lat_lng.split(",");
                    double lat = Double.parseDouble(latlng[0]);
                    double lng = Double.parseDouble(latlng[1]);
                    clusteringPoint = new ClusteringPoint(id,new Geopoint(lat,lng));
                    clusterPoints.put(id,clusteringPoint);
                }
                reShops.add(id);

            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            httpClient.getConnectionManager().shutdown();
        }
        return reShops;
    }

    public static synchronized void pushClusterToES(List<ClusterObj> clusterObjs){
        HttpClient httpClient;

        for(ClusterObj clusterObj : clusterObjs){
            StringBuffer result = new StringBuffer();
            List<Long> numbers = new ArrayList<Long>();
            try {
                httpClient = HttpClientBuilder.create().build();
                List<String> stringList = clusterObj.getPoints();

                Collections.sort(stringList);
                StringBuilder sb = new StringBuilder();
                for(String s : stringList){
                    sb.append("" +
                            "" +
                            "" +
                            "-");
                    sb.append(s);
                }
                String hash = sb.toString().substring(1);
                String geoHash = clusterObj.getGeoHash();


                String ESAPI = "http://localhost:9200/geo_hash/hash_type/"+geoHash+"/_update";

                //Upsert Object
                JSONObject upsertObject = new JSONObject();
                upsertObject.put("id", geoHash);
                upsertObject.put("clusters_count", 1);
                JSONArray clusters = new JSONArray();
                JSONObject thisCluster = new JSONObject();
                thisCluster.put("cluster_id", hash);
                thisCluster.put("distance", clusterObj.getDistance());
                thisCluster.put("status", clusterObj.isStatus());
                thisCluster.put("rank",clusterObj.getRank());
                clusters.put(thisCluster);
                upsertObject.put("clusters", clusters);


                //Update object
                JSONObject fObject1 = new JSONObject();
                fObject1.put("script","ctx._source.clusters += obj;ctx._source.clusters_count += 1");

                JSONObject object = new JSONObject();
                object.put("obj",thisCluster);
                fObject1.put("params",object);
                fObject1.put("upsert",upsertObject);

                HttpPost post = new HttpPost(ESAPI);
                post.setEntity(new StringEntity(fObject1.toString()));

                HttpResponse response = httpClient.execute(post);
                int code = response.getStatusLine().getStatusCode();
                if(code!=200 || code!= 201){
                    System.out.println(code+"--3");
                }

                if(pushedClusters.contains(hash)){
                }else {

                    ESAPI = "http://localhost:9200/live_geo_clusters/geo_cluster/"+hash;
                    String uri = ESAPI;
                    HttpPost postRequest = new HttpPost(uri);
                    String ssd = clusterObj.getJSON().toString();
                    postRequest.setEntity(new StringEntity(ssd));
                    //send post request
                    response = httpClient.execute(postRequest);
                    code = response.getStatusLine().getStatusCode();
                    if(code!=200 || code!= 201){
                        System.out.println(code+"--2");

                    }
                    pushedClusters.add(hash);
                }
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){

        long time_s = System.currentTimeMillis();
        try {
            GeoClustering geoClustering = new GeoClustering();
            List<String> geoHashList = geoClustering.getBlrGeoHashes();
            //geoHashList = new ArrayList<String>();
            //geoHashList.add("tdr4phx");
            //geoHashList.add("tdr1vzcs");
            //geoHashList.add("tdr1yrb");
            ExecutorService executorService = Executors.newFixedThreadPool(8);
            for(String geoHash : geoHashList){
                List<String> clusterPoints = geoClustering.getClusetringPointsForGeoHash(geoHash);
                if(clusterPoints.size()>0){
                    SimpleWorkerThread thread = new SimpleWorkerThread(clusterPoints,geoHash);
                    executorService.execute(thread);
                }
            }
            executorService.shutdown();

        }catch (Exception e){
            e.printStackTrace();
        }
        long time_e = System.currentTimeMillis();
        System.out.println("Time taken is " + (time_e-time_s)+"ms");
    }

}
