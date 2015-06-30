package gridbase;

import com.github.davidmoten.geo.Coverage;
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

/**
 * Created by gurramvinay on 6/16/15.
 */
public class GeoCLustering {
    private static int GEO_PRECISION = 7;
    private static double BLR_TOP_LEFT_LAT =13.1245;
    private static double BLR_TOP_LEFT_LON =77.3664;
    private static double BLR_BOT_RIGHT_LAT =12.8342;
    private static double BLR_BOT_RIGHT_LON =77.8155;



    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision){
        Coverage boxCoverage = com.github.davidmoten.geo.GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
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

    public List<ESShop> getStoresForGeoHash(String geohash){

        String ESAPI = "http://localhost:9200/stores/store";

        List<ESShop> reShops = new ArrayList<ESShop>();

        HttpClient httpClient = null;
        StringBuffer result = new StringBuffer();
        try {
            String uri = ESAPI+"/_search";
            httpClient = HttpClientBuilder.create().build();

            HttpPost postRequest = new HttpPost(uri);

            String ssd = "{\"query\": \"filtered\": {\"query\": {\"match_all\": {}},\"filter\": " +
                    "{\"geo_distance\": {\"distance\": \"1km\",\"location\": \"tdr1t\"}}}}}";
            //String ss ="{\"name\":\""+s.getName()+"\",\"location\":\""+s.getLat()+","+s.getLon()+"\",\"id\":\""+s.getId()+"\"}";
            ssd = "{size:\"200\",query: {filtered: {query: {match_all: {}},filter: {geo_distance: {distance: \"1km\",location: \""+geohash+"\"}}}}}";
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
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String locationOb = jsonObject1.getJSONObject("_source").getString("location");
                String[] ll = locationOb.split(",");
                double lat = Double.parseDouble(ll[0]);
                double lon = Double.parseDouble(ll[1]);
                String product_string = jsonObject1.getJSONObject("_source").getString("product_array");
                product_string = product_string.substring(1,product_string.length()-1);
                ESShop tempShop = new ESShop(jsonObject1.getString("_id"),product_string.split(","),
                        jsonObject1.getJSONObject("_source").getString("name"),new Geopoint(lat,lon));
                reShops.add(tempShop);
                System.out.println("id is "+jsonObject1.getString("_id"));
            }
            System.out.println(" length of array "+jsonArray.length());
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


    //Temporary requirement for forming a cluster is 95% of the products
    public void doClustering(List<ESShop> esShops){

        //Cloning the shops
        List<ESShop> tempShop = new ArrayList<ESShop>(esShops.size());
        for(ESShop es: esShops){
            tempShop.add(es.clone());
        }

        //compute the distance matrix
        HashMap<String,HashMap<String,Double>> distancematrix = computeDistanceMatrix(esShops);

        HashMap<String,List<String>> clusters = new HashMap<String, List<String>>();

        List<String> clusteredShops = new ArrayList<String>();

        for(ESShop startingPoint: esShops){

            ESShop clusterPoint = startingPoint.clone();
            if(clusteredShops.contains(startingPoint.getId())) continue;
            clusteredShops.add(startingPoint.getId());
            //now try to create the cluster with this point

            int product_count=clusterPoint.getPids().length;
            List<String> thisCList = new ArrayList<String>();
            thisCList.add(startingPoint.getId());
            double distance = -1;
            while(product_count<98){
                List<String> rList = getNearestShop(distance,clusterPoint,distancematrix.get(startingPoint.getId()));
                distance = Double.parseDouble(rList.get(0));
                String shopKey = rList.get(1);
                ESShop clusterShop = searchShop(shopKey,esShops);
                clusteredShops.add(clusterShop.getId());
                thisCList.add(clusterShop.getId());
                String[] pids = mergeShopProducts(startingPoint, clusterShop);
                product_count = pids.length;
                startingPoint.setpis(pids);
            }
            clusters.put(startingPoint.getId(),thisCList);
        }

        System.out.println(clusters.toString());
    }


    public ESShop searchShop(String key, List<ESShop> shops){
        for (ESShop s: shops){
            if(s.getId()==key) return s;
        }

        return null;
    }

    public String[] mergeShopProducts(ESShop s1, ESShop s2){
        List<String> shops = new ArrayList<String>(100);
        for(int i=0;i<s1.getPids().length;i++){
            shops.add(s1.getPids()[i]);
        }

        for(String s: s2.getPids()){
            boolean repeated = false;
            for(String d: s1.getPids()){
                if(s.contentEquals(d)) repeated = true;
            }
            if(!repeated) shops.add(s);
        }
        String[] rStrings = new String[shops.size()];
        for(int i=0;i<shops.size();i++){
            rStrings[i] = shops.get(i);
        }
        return rStrings;
    }


    /*public void getShopCloseToCluster(List<ESShop> shopList, List<Geopoint> gpList){
        double minDistance = Double.MAX_VALUE;
        String rSHop =null;
        for(ESShop shop : shopList){
            double totalDistance =0;
            for(Geopoint g: gpList){
                double sDistance = Geopoint.getDistance(g,shop.getLocation());
                totalDistance+=sDistance;
            }
            if(totalDistance<minDistance)
        }
    }*/

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


    public ESShop selectStartingPoint(List<ESShop> esShops){
        ESShop mShop = null;
        int maxId = Integer.MIN_VALUE;
        for(ESShop s: esShops){
            if(s.getPids().length>maxId){
                maxId = s.getPids().length;
                mShop = s;
            }

        }
        return mShop;
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

    public static void main(String[] args){
        GeoCLustering geoCLustering = new GeoCLustering();
        //geoCLustering.getBlrGeoHashes();
        //geoCLustering.postDataToES(geoCLustering.generatRandomShops());
        geoCLustering.doClustering(geoCLustering.getStoresForGeoHash("tdr1t"));
    }
}
