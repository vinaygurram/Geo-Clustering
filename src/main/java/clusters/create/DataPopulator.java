package clusters.create;

import clusters.create.DBObjects.JDCBC;
import clusters.create.DBObjects.ListingObject;
import clusters.create.DBObjects.Store;
import clusters.create.LObject.Geopoint;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Get the stores from Sellers
 * Get the Catalog Tree from Catalog
 * Get the Products from MySQL dump (Temporary)
 * Created by gurramvinay on 8/3/15.
 */
public class DataPopulator {

    private final String SELLER_STORES_API = "http://seller-engine.olastore.com/stores";
    private Connection connection;
    private  static HashMap<String,String[]> productMap = new HashMap<String, String[]>();
    private  static HashMap<String,String[]> product2Map = new HashMap<String, String[]>();
    private  static HashMap<String,String[]> product3Map = new HashMap<String, String[]>();
    private  static HashMap<String,String> productStatusMap = new HashMap<String, String>();

    public DataPopulator(Connection connection){
        this.connection  = connection;
    }

    private List<Store> getStoresData(){
        List<Store> rStores = new ArrayList<Store>();
        try {
            URL url;
            for(int i=1;i<5;i++){
                url = new URL(SELLER_STORES_API+"?city_code=BAN&page="+i);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                JSONObject result = responseToJSON(httpURLConnection.getInputStream());
                JSONArray storesArray = result.getJSONArray("stores");
                for(int k=0;k<storesArray.length();k++){
                    JSONObject thisObject = storesArray.getJSONObject(k);
                    double lat=0;
                    double lng=0;
                    if((!thisObject.has("lat")) || thisObject.get("lat").equals(null)){
                    }else {
                        lat = thisObject.getDouble("lat");
                        lng = thisObject.getDouble("lng");
                    }

                    String name ="";
                    if((!thisObject.has("name")) || thisObject.get("name").equals(null)){
                    }else {
                        name = thisObject.getString("name");
                    }
                    Store store = new Store(name,lat,lng,
                            thisObject.getInt("id")+"",thisObject.getString("state"));
                    rStores.add(store);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  rStores;
    }

    //ToDO make it a bulk call
    private void pushStoresToES(List<Store> storeList){
        String uriString = "http://localhost:9200";
        String indexName = "stores";
        String indexType ="store";
        try {
            for(Store store: storeList){
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(uriString+"/"+indexName+"/"+indexType+"/"+store.getId());
                httpPost.setEntity(new StringEntity(store.toString()));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int code = httpResponse.getStatusLine().getStatusCode();
                System.out.println("response code is " + code);
            }
        }catch (Exception e){e.printStackTrace();}
    }

    private JSONObject responseToJSON(InputStream inputStream){
        JSONObject rObject = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            String fResult = result.toString();
            rObject = new JSONObject(fResult);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rObject;
    }

    public void populateStoresIndex(){
        List<Store> storeList = getStoresData();
        pushStoresToES(storeList);
    }

    public void populateListingIndex(){
        List<Store> storeList = getStoresData();
        for(int i=0;i<1839945;i+=10000){
            List<ListingObject> listingObjects = createListingData(i,storeList);
            pushListingObjectsToES(listingObjects);
        }
    }

    private List<String> getCityStores(String city){
        List<String> storeIds = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        city = city.toUpperCase();
        try {
            preparedStatement = connection.prepareStatement("SELECT id FROM `stores` WHERE city_code = \""+city+"\"");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String id = resultSet.getString("id");
                storeIds.add(id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(!resultSet.isClosed())  resultSet.close();
                if(!preparedStatement.isClosed()) preparedStatement.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return storeIds;

    }

    private List<ListingObject> createListingData(int from,List<Store> storeList){
        List<ListingObject> rObjects = new ArrayList<ListingObject>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT variant_id,store_id,state FROM inventories ORDER BY  id limit ?,?");
            preparedStatement.setInt(1, from);
            preparedStatement.setInt(2, from+10000);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                String id = resultSet.getString("variant_id");
                String store_id = resultSet.getString("store_id");
                String state = resultSet.getString("state");
                ListingObject listingObject = new ListingObject();

                boolean found = false;
                for(Store store: storeList){
                    if(store.getId().contentEquals(store_id)){
                        found = true;
                        listingObject.setLocation(store.getLocation());
                        listingObject.setStore_name(store.getName());
                        listingObject.setStore_state(store.getState());
                    }
                }

                if(!found){
                    continue;
                }


                listingObject.setProductId(id);
                listingObject.setAvailable(state);
                listingObject.setStore_id(store_id);



                if(listingObject.getStore_name()==null) listingObject.setStore_name("NOT Present");
                String[] catPath = getCategoryPath(id);
                if(catPath.length>0){
                    listingObject.setSup_cat_id(catPath[0]);
                    listingObject.setCat_id(catPath[1]);
                    if(catPath.length>2) listingObject.setSub_cat_id(catPath[2]);
                }
                listingObject.setState(productStatusMap.get(id));
                rObjects.add(listingObject);

                //adding extra objects if sec paths are available
                if(product2Map.containsKey(id)){
                    ListingObject listingObject1 = new ListingObject();
                    catPath = product2Map.get(id);
                    if(catPath.length>0){

                        listingObject1.setAvailable(state);
                        listingObject1.setStore_id(store_id);
                        listingObject1.setLocation(listingObject.getLocation());
                        listingObject1.setStore_name(listingObject.getStore_name());
                        listingObject1.setStore_state(listingObject.getStore_state());
                        listingObject1.setState(productStatusMap.get(id));


                        listingObject1.setSup_cat_id(catPath[0]);
                        listingObject1.setCat_id(catPath[1]);
                        if(catPath.length>2) listingObject1.setSub_cat_id(catPath[2]);
                    }
                    listingObject1.setProductId(id+"_1");
                    rObjects.add(listingObject1);
                }
                if(product3Map.containsKey(id)){
                    ListingObject listingObject1 = new ListingObject();
                    catPath = product3Map.get(id);
                    if(catPath.length>0){
                        listingObject1.setAvailable(state);
                        listingObject1.setStore_id(store_id);
                        listingObject1.setLocation(listingObject.getLocation());
                        listingObject1.setStore_name(listingObject.getStore_name());
                        listingObject1.setStore_state(listingObject.getStore_state());
                        listingObject1.setState(productStatusMap.get(id));
                        listingObject.setSup_cat_id(catPath[0]);
                        listingObject.setCat_id(catPath[1]);
                        if(catPath.length>2) listingObject.setSub_cat_id(catPath[2]);
                    }
                    listingObject1.setProductId(id+"_2");
                    rObjects.add(listingObject1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(!resultSet.isClosed()) resultSet.close();
                if(!preparedStatement.isClosed()) preparedStatement.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("docs processed "+ (from+10000));
        return rObjects;
    }

    public String[] getCategoryPath(String productId){
        String[] idList = {};
        if(productMap.containsKey(productId)){
            return productMap.get(productId);
        }else {
            //Make a call to catalog and get products category path
            try {
                //URL url = new URL("http://catalog-engine.olastore.com/v1/category/tree/product/"+productId+"/path");
                URL url = new URL("http://catalog-engine.olastore.com/v1/catalog/products?productIds="+productId+"&filters=derived,category");
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                String resultString = IOUtils.toString(httpURLConnection.getInputStream());
                JSONObject result = new JSONObject(resultString);
                result = result.getJSONObject("products");
                JSONObject productObject = result.getJSONObject(productId);

                JSONArray pPath = productObject.getJSONObject("categoryData").getJSONArray("primary");
                JSONObject thisPath = pPath.getJSONObject(0);
                idList = thisPath.getString("nodeIdPath").split(">");
                productMap.put(productId,idList);

                JSONArray sPath = productObject.getJSONObject("categoryData").getJSONArray("secondary");
                if(sPath.length()>0){
                    thisPath = sPath.getJSONObject(0);
                    idList = thisPath.getString("nodeIdPath").split(">");
                    product2Map.put(productId,idList);

                    if(sPath.length()>1){
                        thisPath = sPath.getJSONObject(1);
                        idList = thisPath.getString("nodeIdPath").split(">");
                        product3Map.put(productId,idList);
                    }
                }
                String status = productObject.getString("productStatus");
                productStatusMap.put(productId,status);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return idList;
    }

    private synchronized void pushListingObjectsToES(List<ListingObject> listingObjects){
        if(listingObjects.size()==0) return;
        String uri = "http://localhost:9200";
        String indexName = "listing";
        String indexType = "list";
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            String fUri = uri+"/"+indexName+"/"+indexType+"/_bulk";
            HttpPost httpPost = new HttpPost(fUri);
            StringBuilder ss = new StringBuilder();
            for(ListingObject listingObject : listingObjects){
                ss.append(listingObject.giveAsBulk());
            }
            httpPost.setEntity(new StringEntity(ss.toString()));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int code = httpResponse.getStatusLine().getStatusCode();
            if(code!=201) {
                System.out.println("ERROR " + code);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pushCatalogMap(){

        for(String s: productMap.keySet()){
            JSONObject jo = new JSONObject();
            String[] catArray = productMap.get(s);
            jo.put("id",s);
            jo.put("sup_cat_id",catArray[0]);
            jo.put("cat_id",catArray[1]);
            jo.put("sub_cat_id","");
            if(catArray.length>2)jo.put("sub_cat_id",catArray[2]);
            try {
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost("http://localhost:9200/products/product/"+s);
                httpPost.setEntity(new StringEntity(jo.toString()));
                httpClient.execute(httpPost);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        DataPopulator dataPopulator = new DataPopulator(JDCBC.getConnection());
        dataPopulator.populateListingIndex();
        //dataPopulator.pushCatalogMap();
        //dataPopulator.populateStoresIndex();
    }

}
