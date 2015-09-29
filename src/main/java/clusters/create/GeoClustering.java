package clusters.create;

import clusters.create.objects.BoundingBox;
import clusters.create.objects.ClusterObj;
import clusters.create.objects.ClusteringPoint;
import clusters.create.objects.Geopoint;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to create clusters
 * It will have all the utility Objects
 * Created by gurramvinay on 6/16/15.
 */
public class GeoClustering {

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

    //bulk
    public static AtomicInteger bulkDocCount = new AtomicInteger(0);
    public static StringBuilder bulkDoc = new StringBuilder();

    public static Map yamlMap = null;

    public static Logger logger = LoggerFactory.getLogger(GeoClustering.class);



    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision) {
        Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
                box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
        logger.info("Hashes produced are "+ boxCoverage.getHashes());
        return boxCoverage.getHashes();
    }

    private BoundingBox getBangaloreBox() {

        Geopoint topleft = new Geopoint((Double)((HashMap)yamlMap.get("bbox_top_left")).get("lat"), (Double)((HashMap)yamlMap.get("bbox_top_left")).get("lon"));
        Geopoint botright = new Geopoint((Double)((HashMap)yamlMap.get("bbox_bot_right")).get("lat"), (Double)((HashMap)yamlMap.get("bbox_bot_right")).get("lon"));
        return new BoundingBox(topleft, botright);
    }

//    public String getZone(String geoHash) {
//
//        LatLong latLong = GeoHash.decodeHash(geoHash);
//
//        try {
//            String geo_api = GEOKIT_API + "?lat=" + latLong.getLat() + "&lng=" + latLong.getLon();
//            HttpClient httpClient = HttpClientBuilder.create().build();
//            HttpGet httpGet = new HttpGet(geo_api);
//            HttpResponse httpResponse = httpClient.execute(httpGet);
//            JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
//            if (result.getString("status").contentEquals("SUCCESS")) {
//                if (result.has("locality") && !result.isNull("locality")) {
//                    return result.getJSONObject("locality").getString("name");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }


    public List<String> getBlrGeoHashes() {
        BoundingBox bbox = getBangaloreBox();
        Set<String> hashes = getGeoHashOfBoundingBox(bbox, (Integer)yamlMap.get("clusters_geo_precision"));
        Iterator<String> iterator = hashes.iterator();
        int valitGeoHashCount = 0;
        List<String> geohashList = new ArrayList<String>();
        while (iterator.hasNext()) {
            String thisHash = iterator.next();
            geohashList.add(thisHash);
        }
        logger.info("Total number of hashes are "+ geohashList.size());
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
            logger.error(e.getMessage());
        } finally {
            try {
                fileReader.close();
                bufferedReader.close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return productIdSet;
    }

    public void createFreshClusteringIndices(){
        try {
            //delete both geo and cluster index
            HttpClient httpClient = HttpClientBuilder.create().build();
            String multi_delete_api = (String)yamlMap.get("es_multi_delete_api");
            multi_delete_api = multi_delete_api.replace(":index_name1",(String) yamlMap.get("geo_hash_index_name"));
            multi_delete_api = multi_delete_api.replace(":index_name2",(String) yamlMap.get("clusters_index_name"));
            HttpDelete httpDelete = new HttpDelete(multi_delete_api);
            HttpResponse httpResponse = httpClient.execute(httpDelete);
            logger.info("ES response to delete geohash and cluster indexes is "+EntityUtils.toString(httpResponse.getEntity()));

            //create geo hash index
            httpClient = HttpClientBuilder.create().build();
            String geo_settings_api = (String) yamlMap.get("es_settings_api");
            geo_settings_api = geo_settings_api.replace(":index_name", (String) yamlMap.get("geo_hash_index_name"));
            HttpPost httpPost = new HttpPost(geo_settings_api);
            httpPost.setEntity(new FileEntity(new File("bin/mappings/geo_mappings.txt")));
            httpResponse = httpClient.execute(httpPost);
            logger.info("ES response to insert mappings for geo hash index is "+EntityUtils.toString(httpResponse.getEntity()));

            //create geo clusters index
            httpClient = HttpClientBuilder.create().build();
            String clusters_settings_api = (String)yamlMap.get("es_settings_api");
            clusters_settings_api = clusters_settings_api.replace(":index_name",(String)yamlMap.get("clusters_index_name"));
            httpPost = new HttpPost(clusters_settings_api);
            httpPost.setEntity(new FileEntity(new File("bin/mappings/cluster_mappings.txt")));
            httpResponse = httpClient.execute(httpPost);
            logger.info("ES response to insert mappings for geo clusters api is "+ EntityUtils.toString(httpResponse.getEntity()));
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    //read yaml file to get the map
    private void readYAML(){
        try {
            Yaml yaml = new Yaml();
            yamlMap = (Map) yaml.load(new FileInputStream(new File("src/main/resources/config.yaml")));
            logger.info("Yaml reading is complete");
        }catch (Exception e){
            logger.error("Yaml configuration reading failed");
        }
    }

    public static void main(String[] args) {
        long time_s = System.currentTimeMillis();
        logger.info("Clustering logic start "+time_s);
        try {
            GeoClustering geoClustering = new GeoClustering();
            geoClustering.readYAML();
            geoClustering.createFreshClusteringIndices();
            List<String> geoHashList = geoClustering.getBlrGeoHashes();
//            List<String> geoHashList = new ArrayList<>();
//            geoHashList = new ArrayList<String>();
//            geoHashList.add("tdr4phx");
//            geoHashList.add("tdr0ftn");
//            geoHashList.add("tdr1vzc");
//            geoHashList.add("tdr1yrb");

            //generate both fnv and non-fnv sets
            nfnvProdSet = geoClustering.generateProductSetFromCSV(nfnvFilePath, false);
            logger.info("NFNV Set created. NFNV set is "+nfnvProdSet);
            fnvProdSet = geoClustering.generateProductSetFromCSV(fnvFilePath, true);
            logger.info("FNV Set created. FNV set is "+fnvProdSet);
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Future<String>> futuresList = new ArrayList<>();
            for (String geoHash : geoHashList) {
                Future<String> thisFuture = executorService.submit(new SimpleWorkerThread(geoHash));
                futuresList.add(thisFuture);
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);
            if(!bulkDoc.toString().isEmpty()){
                HttpClient httpClient = HttpClientBuilder.create().build();
                //HttpPost httpPost = new HttpPost(GeoClustering.ES_REST_API +"/"+ GeoClustering.ES_BULK_END_POINT);
                String es_bulk_api = (String) yamlMap.get("es_bulk_api");
                HttpPost httpPost = new HttpPost(es_bulk_api);
                httpPost.setEntity(new StringEntity(GeoClustering.bulkDoc.toString()));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                logger.info("Response from ES for  is "+ httpResponse.getEntity().toString());
                int code = httpResponse.getStatusLine().getStatusCode();
                if(code!=200 && code!=201) {
                    logger.info(httpResponse.getStatusLine().toString());
                }
            }
            long time_e = System.currentTimeMillis();
            logger.info(" Total time taken is "+(time_e - time_s) + "ms");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
