package clusters.create;

import clusters.create.objects.BoundingBox;
import clusters.create.objects.ClusterObj;
import clusters.create.objects.ClusteringPoint;
import clusters.create.objects.Geopoint;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.mongodb.DBObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jongo.Find;
import org.jongo.Jongo;
import org.jongo.MongoCursor;
import org.jongo.ResultHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to create clusters
 * It will have all the utility Objects
 * Created by gurramvinay on 6/16/15.
 */
public class GeoClustering {
    private static int GEO_PRECISION = 7;

    private static double BLR_TOP_LEFT_LAT = 13.11091;
    private static double BLR_TOP_LEFT_LON = 77.46253;
    private static double BLR_BOT_RIGHT_LAT = 12.81581;
    private static double BLR_BOT_RIGHT_LON = 77.79075;

    public static ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
    public static ConcurrentHashMap<String, ClusteringPoint> clusterPoints = new ConcurrentHashMap<String, ClusteringPoint>();
    public static ConcurrentHashMap<String, List<ClusterObj>> computedClusters = new ConcurrentHashMap<String, List<ClusterObj>>();
    public static List<String> pushedClusters = Collections.synchronizedList(new ArrayList<String>());
    public static ConcurrentHashMap<String, Integer> clusterProductCoverage = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> clusterSubCatCoverage = new ConcurrentHashMap<>();
    public static AtomicInteger jobsRun = new AtomicInteger();
    public static ConcurrentHashMap<String, Double> clusterRankMap = new ConcurrentHashMap<>();

    public static final int maxProductCount = 17000;
    public static final double relFNCCoverageCoeff = 0.3;
    public static final double relNFNCCoverageCoeff = 0.3;
    public static final double relProductCoverageCoeff = 0.35;

    public static Set<String> fnvProdSet = new HashSet<>();
    public static Set<String> nfnvProdSet = new HashSet<>();
    public static final String nfnvFilePath = "src/main/resources/popular_nfnc.csv";
    public static final String fnvFilePath = "src/main/resources/fnv_pids.csv";

    public final static String ES_REST_API = "http://localhost:9200";
    public final static String GEOKIT_API = "http://geokit.qa.olahack.in/localities";

    public static final String GEO_HASH_INDEX = "geo_hash_6";
    public static final String GEO_HASH_INDEX_TYPE = "hash_type";
    public static final String CLUSTERS_INDEX = "live_geo_clusters_6";
    public static final String CLUSTERS_INDEX_TYPE = "geo_cluster";
    public static final String LISTING_INDEX = "listing";
    public static final String STORES_INDEX = "stores";
    public static final String STORES_INDEX_TYPE = "store";

    public static final String ES_SEARCH_END_POINT = "_search";
    public static final String ES_BULK_END_POINT = "_bulk";

    //bulk
    public static AtomicInteger bulkDocCount = new AtomicInteger(0);
    public static StringBuilder bulkDoc = new StringBuilder();



    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision) {
        Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
                box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
        System.out.println(boxCoverage.toString());
        return boxCoverage.getHashes();
    }

    private BoundingBox getBangaloreBox() {
        Geopoint topleft = new Geopoint(BLR_TOP_LEFT_LAT, BLR_TOP_LEFT_LON);
        Geopoint botright = new Geopoint(BLR_BOT_RIGHT_LAT, BLR_BOT_RIGHT_LON);
        return new BoundingBox(topleft, botright);
    }

    public String getZone(String geoHash) {

        LatLong latLong = GeoHash.decodeHash(geoHash);

        try {
            String geo_api = GEOKIT_API + "?lat=" + latLong.getLat() + "&lng=" + latLong.getLon();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(geo_api);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            if (result.getString("status").contentEquals("SUCCESS")) {
                if (result.has("locality") && !result.isNull("locality")) {
                    return result.getJSONObject("locality").getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getZoneFromMongo(String geoHash) {

        String zoneString = "";
        try {
            LatLong latLong = GeoHash.decodeHash(geoHash);
            Jongo jongo = MongoJClient.getJongoClietn();
            String query = "{polygons:{\"$geoIntersects\":{\"$geometry\":{\"type\":\"Point\",\"coordinates\":[" + latLong.getLon() + "," + latLong.getLat() + "]}}}}";
            Find result = jongo.getCollection(MongoJClient.MONGO_COLLECTION).find(query);
            MongoCursor<String> zones = result.map(new ResultHandler<String>() {
                @Override
                public String map(DBObject dbObject) {
                    if (dbObject.containsField("name")) {
                        return (String) dbObject.get("name");
                    }
                    return "";
                }
            });
            if (zones.hasNext()) {
                zoneString = zones.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zoneString;
    }

    public List<String> getBlrGeoHashes() {
        BoundingBox bbox = getBangaloreBox();
        Set<String> hashes = getGeoHashOfBoundingBox(bbox, GEO_PRECISION);
        Iterator<String> iterator = hashes.iterator();
        int valitGeoHashCount = 0;
        List<String> geohashList = new ArrayList<String>();
        while (iterator.hasNext()) {
            String thisHash = iterator.next();
            geohashList.add(thisHash);
            //String zone = getZoneFromMongo(thisHash);
            //if(!(zone.isEmpty() || zone.contentEquals(""))){
            //    geohashList.add(thisHash);
            //    valitGeoHashCount++;
            //}
        }
        // MongoJClient.close();
        System.out.println("total number of hashes " + geohashList.size());
        return geohashList;
    }


    public Set<String> generateProductSetFromCSV(String pathtoFile, boolean isFnv) {

        Set<String> productIdSet = new HashSet<String>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(new File(pathtoFile));
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                String productId = values[0];
                if (isFnv) productId = productId.substring(1, productId.length() - 1);
                productIdSet.add(productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return productIdSet;
    }

    public static void main(String[] args) {

        long time_s = System.currentTimeMillis();
        System.out.println("start time is " + time_s);
        try {
            GeoClustering geoClustering = new GeoClustering();
            List<String> geoHashList = geoClustering.getBlrGeoHashes();
//            List<String> geoHashList = new ArrayList<>();
//            geoHashList = new ArrayList<String>();
//            geoHashList.add("tdr4phx");
//            geoHashList.add("tdr0ftn");
//            geoHashList.add("tdr1vzc");
//            geoHashList.add("tdr1yrb");

            //generate both fnv and non-fnv sets
            nfnvProdSet = geoClustering.generateProductSetFromCSV(nfnvFilePath, false);
            fnvProdSet = geoClustering.generateProductSetFromCSV(fnvFilePath, true);
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            for (String geoHash : geoHashList) {
                SimpleWorkerThread thread = new SimpleWorkerThread(geoHash);
                executorService.execute(thread);
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);
            if(!bulkDoc.toString().isEmpty()){

                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(GeoClustering.ES_REST_API +"/"+ GeoClustering.ES_BULK_END_POINT);
                httpPost.setEntity(new StringEntity(GeoClustering.bulkDoc.toString()));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                if(code!=200 && code!=201) {
                    System.out.println(httpResponse.getStatusLine());

                }
            }

            long time_e = System.currentTimeMillis();
            System.out.println("Time taken is " + (time_e - time_s) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
