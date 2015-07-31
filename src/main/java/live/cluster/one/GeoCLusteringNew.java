package live.cluster.one;

import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import gridbase.*;
import live.cluster.one.LObject.CatalogTree;
import live.cluster.one.LObject.ClusterObjNew;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gurramvinay on 6/16/15.
 */
public class GeoCLusteringNew {
    private static int GEO_PRECISION = 7;
    private static double BLR_TOP_LEFT_LAT =13.1245;
    private static double BLR_TOP_LEFT_LON =77.3664;
    private static double BLR_BOT_RIGHT_LAT =12.8342;
    private static double BLR_BOT_RIGHT_LON =77.8155;
    public  static ConcurrentHashMap<String,List<String>> map = new ConcurrentHashMap<String, List<String>>();
    public static ConcurrentHashMap<String ,ClusteringPoint> clusterPoints = new ConcurrentHashMap<String, ClusteringPoint>();
    public static ConcurrentHashMap<String,String[]> product3MergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,String[]> subCat3MergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,String[]> subCatMultiMergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,String[]> product2MergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,String[]> productMultiMergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,String[]> subCat2MergerMap = new ConcurrentHashMap<String, String[]>();
    public static ConcurrentHashMap<String,CatalogTree> catalogTreeMap = new ConcurrentHashMap<String,CatalogTree>();
    public static ConcurrentHashMap<String ,List<ClusterObj>> computedClusters = new ConcurrentHashMap<String, List<ClusterObj>>();
    public static ArrayList<String> pushedClusters = new ArrayList<String>();
    public static HashMap<String,Integer> geoProductCoverage = new HashMap<String, Integer>();
    public static HashMap<String,Integer> geoSubCatCoverage = new HashMap<String, Integer>();
    public static int count=0;



    public int generateProdCatMap(ConcurrentHashMap<String,List<String>> productsCatMap){
        int m = 0;
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            while (m<19000){
                String query = "{query:{match_all:{}},size:1000,from:"+m+"}";
                m = m+1000;
                HttpPost httpPost = new HttpPost("http://localhost:9200/products_list_new/_search");
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
                    JSONObject tempJO = jsonArray.getJSONObject(i);
                    tempJO = tempJO.getJSONObject("_source");
                    tempJO = tempJO.getJSONObject("product");
                    String id = tempJO.getString("id");
                    List<String> catList= new ArrayList<String>();
                    catList.add(tempJO.getString("spcat"));
                    catList.add(tempJO.getString("cat"));
                    catList.add(tempJO.getString("sbcat"));
                    productsCatMap.put(id,catList);
                }
            }
            return 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
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

    public List<String> getBlrGeoHashes(){
        BoundingBox bbox = getBangaloreBox();
        Set<String> hashes = getGeoHashOfBoundingBox(bbox,GEO_PRECISION);
        Iterator<String> iterator = hashes.iterator();
        int i=0;
        List<String> geohashList = new ArrayList<String>();
        while(iterator.hasNext()){
            String thisHash = iterator.next();
            geohashList.add(thisHash);
            i++;
        }
        System.out.println("total number of hashes "+i);
        return geohashList;
    }

    private List<Shop> generatRandomShops(){
        double minLat = 12.8342;
        double minLon = 77.3664;
        double maxLat = 13.1245;
        double maxLon = 77.8155;

        List<Double> latList = new ArrayList<Double>(100);
        List<Double> lonList = new ArrayList<Double>(100);

        for(int i=0;i<100;i++){
            double tempLat = minLat + Math.random()*(maxLat-minLat);
            while(latList.contains(tempLat)){
                 tempLat = minLat + Math.random()*(maxLat-minLat);
            }
            double tempLon = minLon + Math.random()*(maxLon-minLon);
            while(lonList.contains(tempLat)){
                tempLon = minLon + Math.random()*(maxLon-minLon);
            }
            latList.add(tempLat);
            lonList.add(tempLon);
        }

        List<Shop> shopList = new ArrayList<Shop>();

        for(int i=1;i<101;i++){
            for (int j=1;j<101;j++){
                Shop tempShop = new Shop(latList.get(i-1),lonList.get(j-1),""+i+j);
                shopList.add(tempShop);
            }
        }
        System.out.println(shopList.toString());
        return shopList;
    }

    public void postDataToES(List<Shop> shopList){

        String ESAPI = "http://localhost:9200/stores/store";



        int i=0;
        for (Shop s: shopList){
            HttpClient httpClient = null;
            StringBuffer result = new StringBuffer();
            try {
                String uri = ESAPI+"/"+s.getId();
                httpClient = HttpClientBuilder.create().build();

                HttpPut putRequest = new HttpPut(uri);

                String ss ="{\"name\":\""+s.getName()+"\",\"location\":\""+s.getLat()+","+s.getLon()+"\",\"id\":\""+s.getId()+"\",\"product_array\":\"" +s.getProducst()+"\"}";

                putRequest.setEntity(new StringEntity(ss));

                //send post request
                HttpResponse response = httpClient.execute(putRequest);


                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
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

        }
    }

     public List<ESShop> getStoresForGeoHash(String geohash,ConcurrentHashMap<String,List<String>> map){

        String ESAPI = "http://localhost:9200/stores_live";

        List<ESShop> reShops = new ArrayList<ESShop>();

        HttpClient httpClient = null;
        StringBuffer result = new StringBuffer();
        try {
            String uri = ESAPI+"/_search";
            httpClient = HttpClientBuilder.create().build();

            HttpPost postRequest = new HttpPost(uri);
            String ssd = "{\"size\":40,\"fields\": [\"store.store_id\",\"store.location\",\"store.cat_list\",\"store.products.id\"," +
                    "\"store.products_count\",\"store.sub_cat_list\",\"fnv\"],\"query\": {\"match_all\": {}}," +
                    "\"filter\": {\"geo_distance\": {\"distance\":\"6km\",\"location\": \""+geohash+"\"}}}";
            postRequest.setEntity(new StringEntity(ssd));
            //send post request
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
            if(jsonArray.length()>1){
               // System.out.println();
            }
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                JSONObject fieldsObj = jsonObject1.getJSONObject("fields");
                String id = fieldsObj.getJSONArray("store.store_id").getString(0);
                JSONArray productsArray = fieldsObj.getJSONArray("store.products.id");
                String location = fieldsObj.getJSONArray("store.location").getString(0);
                String[] ll = location.split(",");
                double lat = Double.parseDouble(ll[0]);
                double lon = Double.parseDouble(ll[1]);
                ESShop esShop = new ESShop(id,productsArray,id,new Geopoint(lat,lon),map);
                if(fieldsObj.keySet().contains("fnv")){
                    if(fieldsObj.getJSONArray("fnv").getBoolean(0))esShop.setFnv(true);
                }
                reShops.add(esShop);
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

    public ESShop searchShop(String key, List<ESShop> shops){
        for (ESShop s: shops){
            if(s.getId()==key) return s;
        }

        return null;
    }



    //Convert shops to clustering points
    public List<String> getClusteringPoints(List<ESShop> esShops){
        List<String> clusteringPoints = new ArrayList<String>();
        for(ESShop shop : esShops){
            //Store Them in HashMap
            ClusteringPoint cp = null;

            if(GeoCLusteringNew.clusterPoints.containsKey(shop.getId())){
                cp = GeoCLusteringNew.clusterPoints.get(shop.getId());

            }else {
                cp =new ClusteringPoint(shop.getId(),shop.getProductIDList(),shop.getCatList(),shop.getLocation());
                if(shop.isFnv())cp.setIsFnv(true);
                GeoCLusteringNew.clusterPoints.put(shop.getId(), cp);
            }
            clusteringPoints.add(shop.getId());
        }
        return clusteringPoints;
    }


    public List<String> getNearestShop(double distance,ESShop shop,HashMap<String,Double> nodeDistanceMatrix){

        double minDistance = Double.MAX_VALUE;
        List<String> rList = new ArrayList<String>();
        String shopKey = "";
        for(String s: nodeDistanceMatrix.keySet()){
           double tempDistance = nodeDistanceMatrix.get(s);
            if(tempDistance>distance && tempDistance<minDistance){
                minDistance = tempDistance;
                shopKey =s;
            }
        }
        rList.add(String.valueOf(minDistance));
        rList.add(shopKey);
        return rList;
    }


    public HashMap<String,HashMap<String,Double>> computeDistanceMatrix(List<ESShop> esShops){
       HashMap<String,HashMap<String,Double>> distanceMatrix = new HashMap<String, HashMap<String, Double>>();
       for(ESShop s: esShops){
           HashMap<String,Double> thisHas = new HashMap<String, Double>();
           for(ESShop s1: esShops){
               if(s.getId().contentEquals(s1.getId())) continue;
               double dist = Geopoint.getDistance(s1.getLocation(),s.getLocation());
               thisHas.put(s1.getId(),dist);
           }
           distanceMatrix.put(s.getId(),thisHas);
       }
        return distanceMatrix;
    }


    public static void pushClusterToES(List<ClusterObjNew> clusterObjs,int pCount, int sbCount){
        HttpClient httpClient = null;

        for(ClusterObjNew clusterObj : clusterObjs){
            StringBuffer result = new StringBuffer();
            List<Long> numbers = new ArrayList<Long>();
            try {
                httpClient = HttpClientBuilder.create().build();
                List<String> stringList = clusterObj.getPoints();
                String hash = getHashForCHM(stringList);
                String geoHash = clusterObj.getGeoHash();

                if(pushedClusters.contains(hash)){
                    String ESAPI = "http://localhost:9200/geo_hash1/hash_type/";
                    HttpPost post = new HttpPost(ESAPI);

                    JSONObject fObject = new JSONObject();

                    fObject.put("script","ctx._source.clusters += obj");
                    JSONObject thisCluster = new JSONObject();
                    thisCluster.put("cluster_id", hash);
                    thisCluster.put("geo_hash", geoHash);
                    thisCluster.put("distance", clusterObj.getDistance());
                    thisCluster.put("status", clusterObj.isStatus());
                    thisCluster.put("rank",clusterObj.getRank());
                    JSONObject object = new JSONObject();
                    object.put("obj",thisCluster);
                    fObject.put("params",object);
                    post.setEntity(new StringEntity(thisCluster.toString()));
                    HttpResponse response = httpClient.execute(post);
                    HttpEntity code = response.getEntity();

                }else {

                    String ESAPI = "http://localhost:9200/live_geo_clusters1/geo_cluster/"+hash;
                    String uri = ESAPI;
                    HttpPost postRequest = new HttpPost(uri);
                    String ssd = clusterObj.getJSON().toString();
                    postRequest.setEntity(new StringEntity(ssd));
                    //send post request
                    HttpResponse response = httpClient.execute(postRequest);
                    HttpEntity code =response.getEntity();
                    pushedClusters.add(hash);

                    ESAPI = "http://localhost:9200/geo_hash1/hash_type/";

                    //UPSERT doc
                    JSONObject fObject = new JSONObject();
                    fObject.put("id",geoHash);
                    fObject.put("product_count",pCount);
                    fObject.put("sub_cat_count",sbCount);
                    JSONArray clusters = new JSONArray();
                    JSONObject thisCluster = new JSONObject();
                    thisCluster.put("cluster_id", hash);
                    thisCluster.put("geo_hash", geoHash);
                    thisCluster.put("distance", clusterObj.getDistance());
                    thisCluster.put("status", clusterObj.isStatus());
                    thisCluster.put("rank",clusterObj.getRank());
                    clusters.put(thisCluster);
                    fObject.put("clusters",clusters);


                    //Update object
                    JSONObject fObject1 = new JSONObject();
                    fObject1.put("script","ctx._source.clusters += obj");

                    JSONObject object = new JSONObject();
                    object.put("obj",thisCluster);
                    fObject1.put("params",object);
                    fObject1.put("upsert",fObject);

                    HttpPost post = new HttpPost(ESAPI);
                    post.setEntity(new StringEntity(thisCluster.toString()));

                    response = httpClient.execute(post);
                    code = response.getEntity();
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
            GeoCLusteringNew geoCLusteringNew = new GeoCLusteringNew();

            //Make geohashes and product category map
            List<String> geoHashList = geoCLusteringNew.getBlrGeoHashes();

            //geoHashList = new ArrayList<String>();
            //geoHashList.add("tdr4phx");
            //geoHashList.add("tdr1vzcs");
            //geoHashList.add("tdr1yrb");
            int tt = geoCLusteringNew.generateProdCatMap(geoCLusteringNew.map);

            ExecutorService executorService = Executors.newCachedThreadPool();
            for(String s : geoHashList){
                List<ESShop> shops = geoCLusteringNew.getStoresForGeoHash(s, map);
                List<String> clusterPoints = geoCLusteringNew.getClusteringPoints(shops);

                if(clusterPoints.size()>0){
                    SimpleWorkerThread thread = new SimpleWorkerThread(clusterPoints,s);
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

    //Helper function to create the hash
    public static String getHashForCHM(List<String> strings){
        Collections.sort(strings);
        StringBuilder sb = new StringBuilder();
        for(String s : strings){
            sb.append("" +
                    "" +
                    "" +
                    "-");
            sb.append(s);
        }
        return sb.toString().substring(1);
    }
}
