package reports;

import clusters.create.LObject.BoundingBox;
import clusters.create.LObject.Geopoint;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 *
 * Computes total product count and relevant product count in 3km of a lat/long
 * Created by gurramvinay on 8/13/15.
 */
public class RelevantProducts {


    private static int GEO_PRECISION = 7;
    private static double BIG_BLR_TOP_LEFT_LAT =13.1245;
    private static double BIG_BLR_TOP_LEFT_LON =77.3664;
    private static double BIG_BLR_BOT_RIGHT_LAT =12.8342;
    private static double BIG_BLR_BOT_RIGHT_LON =77.8155;

    private static double BLR_TOP_LEFT_LAT =13.1109114;
    private static double BLR_TOP_LEFT_LON =77.4625397;
    private static double BLR_BOT_RIGHT_LAT =12.824522500000002;
    private static double BLR_BOT_RIGHT_LON =77.7495575;

    private List<String> createBangaloreHashes(){
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
        return geohashList;
    }

    private BoundingBox getBangaloreBox(){
        Geopoint topleft = new Geopoint(BLR_TOP_LEFT_LAT,BLR_TOP_LEFT_LON);
        Geopoint botright = new Geopoint(BLR_BOT_RIGHT_LAT,BLR_BOT_RIGHT_LON);
        return new BoundingBox(topleft,botright);
    }

    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision){
        Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
                box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);

        return boxCoverage.getHashes();
    }

    private HashMap<String,List<String>> createZoneGeoMap(List<String> geoHashList){
        HashMap<String, List<String>> zoneGeoHashMap = new HashMap<String, List<String>>();
        for(String geohash: geoHashList){
            String zoneForGeoHash = getZone(geohash);
            if(!zoneForGeoHash.isEmpty()){
                if(zoneGeoHashMap.containsKey(zoneForGeoHash)){
                    List<String> geoList = zoneGeoHashMap.get(zoneForGeoHash);
                    geoList.add(geohash);
                }else {
                    List<String> geoList = new ArrayList<String>();
                    geoList.add(geohash);
                    zoneGeoHashMap.put(zoneForGeoHash,geoList);
                }
            }
        }
        return zoneGeoHashMap;
    }

    private ESTempObject getProductsForGeoHash(String geoHash,int radius){

        //String esAPI  = "http://es.qa.olahack.in/listing/_search";
        String esAPI  = "http://escluster.olastore.com:9200/listing/_search";
        ESTempObject esTempObject = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(esAPI);
            String query = "{\"size\":0,\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[" +
                    "{\"geo_distance\":{\"store_details.location\":\""+geoHash+"\",\"distance\":\""+radius+"km\"}}," +
                    "{\"term\":{\"product_details.available\":true}}," +
                    "{\"term\":{\"product_details.status\":\"current\"}}]}}}}," +
                    "\"aggregations\":{\"unique_products\":{\"terms\":{\"field\":\"product_details.id\",\"size\":0}}," +
                    "\"stores_count\":{\"cardinality\":{\"field\":\"store_details.id\"}}," +
                    "\"sub_cat_count\":{\"cardinality\":{\"field\":\"product_details.sub_category_id\"}}}}";
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            JSONObject esResult = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            if(esResult.getJSONObject("hits").getInt("total")==0) return null;
            esResult = esResult.getJSONObject("aggregations");
            int storesCount = esResult.getJSONObject("stores_count").getInt("value");
            int subCatCount = esResult.getJSONObject("sub_cat_count").getInt("value");
            Set<String> productList = new HashSet<String>();
            JSONArray uniqueProdBuckets = esResult.getJSONObject("unique_products").getJSONArray("buckets");
            for(int i=0;i<uniqueProdBuckets.length();i++){
                String productId = uniqueProdBuckets.getJSONObject(i).getString("key");
                productList.add(productId);
            }
            esTempObject = new ESTempObject();
            esTempObject.setStoresCount(storesCount);
            esTempObject.setSubCatCount(subCatCount);
            esTempObject.setProducts(productList);
            esTempObject.setProduct_count(productList.size());
        }catch (Exception e){
            e.printStackTrace();
        }
        return esTempObject;
    }


    public ESTempObject setRelProductcount(ESTempObject esTempObject, Set<String> fnvSet, Set<String> nonFnvRelSet){

        Set<String> productsSet = esTempObject.getProducts();
        Set<String> intesection = new HashSet<String>(productsSet);
        intesection.retainAll(fnvSet);
        esTempObject.setFnvCount(intesection.size());
        intesection = new HashSet<String>(productsSet);
        intesection.retainAll(nonFnvRelSet);
        esTempObject.setRelNFNVCount(intesection.size());
        return esTempObject;
    }


    public void createZoneRelProducList(Set<String> fnvSet, Set<String> nFnvRelSet){
        FileWriter fileWriter = null;
        try {
            //fileWriter = new FileWriter(new File("src/main/resources/zoneRelProdData.csv"));
            fileWriter = new FileWriter(new File("/home/vinay/zoneRelProdData.csv"));
            fileWriter.write("geo_hash,zone," +
                    "rel_nfnv_prod_count_3,fnv_prod_count_3,prod_count_3,stores_count_3,sub_cat_count_3" +
                    "rel_nfnv_prod_count_4,fnv_prod_count_4,prod_count_4,stores_count_4,sub_cat_count_4"+
                    "rel_nfnv_prod_count_5,fnv_prod_count_5,prod_count_5,stores_count_5,sub_cat_count_5");
            fileWriter.write("\n");
            List<String> blrGeoHashes = createBangaloreHashes();
            for(String geoHash: blrGeoHashes){
                String zone = getZone(geoHash);
                if(zone.isEmpty()) continue;
                ESTempObject es_3 = getProductsForGeoHash(geoHash,3);
                if(es_3!=null){
                    es_3 = setRelProductcount(es_3,fnvSet,nFnvRelSet);
                    ESTempObject es_4 = getProductsForGeoHash(geoHash,4);
                    es_4 = setRelProductcount(es_4,fnvSet,nFnvRelSet);
                    ESTempObject es_5 = getProductsForGeoHash(geoHash,5);
                    es_5 = setRelProductcount(es_5,fnvSet,nFnvRelSet);
                    fileWriter.write(geoHash+","+zone);
                    fileWriter.write(","+es_3.getRelNFNVCount()+","+es_3.getFnvCount()+","+es_3.getProduct_count()+","+es_3.getStoresCount()+","+es_3.getSubCatCount());
                    fileWriter.write(","+es_4.getRelNFNVCount()+","+es_4.getFnvCount()+","+es_4.getProduct_count()+","+es_4.getStoresCount()+","+es_4.getSubCatCount());
                    fileWriter.write(","+es_5.getRelNFNVCount()+","+es_5.getFnvCount()+","+es_5.getProduct_count()+","+es_5.getStoresCount()+","+es_5.getSubCatCount());
                    fileWriter.write("\n");
                }
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

    public Set<String> generateProductSetFromCSV(String pathtoFile, boolean isFnv){

        Set<String> productIdSet = new HashSet<String>();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader  = new FileReader(new File(pathtoFile));
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine() ;
            while((line=bufferedReader.readLine())!=null){
                String[] values = line.split(",");
                String productId = values[0];
                if(isFnv) productId = productId.substring(1,productId.length()-1);
                productIdSet.add(productId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                fileReader.close();
                bufferedReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return productIdSet;
    }

    public String getZone(String geoHash){

        LatLong latLong = GeoHash.decodeHash(geoHash);

        try {
            //String geo_api = "http://geokit.olastore.com/localities?lat="+latLong.getLat()+"&lng="+latLong.getLon()+"";
            String geo_api = "http://geokit.qa.olahack.in/localities?lat="+latLong.getLat()+"&lng="+latLong.getLon()+"";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(geo_api);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            if(result.getString("status").contentEquals("SUCCESS")){
                if(result.has("locality") && !result.isNull("locality")){
                    return result.getJSONObject("locality").getString("name");
                    //return result.getJSONArray("locality").getJSONObject(0).getString("name");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public void generateLocalityReport(String filepath){
        FileWriter fileWriter = null;
       try {
           fileWriter = new FileWriter(new File(filepath));
           fileWriter.write("geo_hash,locality");
           fileWriter.write("\n");

           List<String> blrHashes = createBangaloreHashes();
           int i=0;
           for(String geo_hash: blrHashes){
               System.out.print(i++);
               String locality = getZone(geo_hash);
               if(!(locality.isEmpty() || locality.contentEquals(""))){
                   System.out.println("locality is "+locality);
                   fileWriter.write(geo_hash+","+locality);
                   fileWriter.write("\n");
               }
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


    public static void main(String[] args){
        RelevantProducts relevantProducts = new RelevantProducts();
        //Set<String> nonFnvRelPidSet = relevantProducts.generateProductSetFromCSV(args[0],false);
        //Set<String> fnvPidSet = relevantProducts.generateProductSetFromCSV(args[1],true);
        //relevantProducts.createZoneRelProducList(fnvPidSet,nonFnvRelPidSet);
        relevantProducts.generateLocalityReport("src/main/resources/locality_report.csv");


    }

}
