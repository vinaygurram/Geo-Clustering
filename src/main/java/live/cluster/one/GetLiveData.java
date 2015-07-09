package live.cluster.one;

import gridbase.Geopoint;
import org.json.JSONArray;
import org.json.JSONObject;

import live.cluster.one.LObject.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 7/9/15.
 * Need to generalize inputStream --> String method
 * Need to genralzie method to take URL and give String
 */
public class GetLiveData {

    public static List<Store> stores = new ArrayList<Store>();


    public List<Store> getStoresData(){

        try {
            //make connection to listing service api
            URL url = new URL("http://listing-service.olastore.com/list/stores?lat=12.969558&lng=77.685487&radius=40000000000");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("client","ff@tnt");
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            //parse input stream
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            String fResult = result.toString();

            //make stores
            JSONObject jsonObject = new JSONObject(fResult);
            JSONArray storesObj = jsonObject.getJSONArray("stores");
            List<Store> rStores = new ArrayList<Store>();
            for(int i=0;i<storesObj.length();i++){
                Store store = new Store(storesObj.getJSONObject(i).getString("id"),
                        new Geopoint(storesObj.getJSONObject(i).getDouble("lat"),storesObj.getJSONObject(i).getDouble("lon")));
                stores.add(store);
                rStores.add(store);
            }
            return rStores;

        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<Store>();
    }

    public List<Store> getProductsOfStores(List<Store> stores){
        System.out.println("jj");
       for(Store store: stores){
          getProductOfStores(store);
       }
        return stores;
    }

    public void getProductOfStores(Store store){

        try {
            //make connection to seller

            int k=1;
            while(k<170){
                URL url = new URL("http://seller-engine.olastore.com/stores/"+store.getId()+"/inventories?page="+k);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //Parse the data
                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuffer result = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                String fResult = result.toString();
                JSONObject jsonObject = new JSONObject(fResult);
                JSONArray products = jsonObject.getJSONArray("products");
                for(int i=0;i<products.length();i++){
                    JSONObject product = products.getJSONObject(i);
                    if(product.getString("state").contentEquals("available")){
                        Product product1 = new Product(product.getString("id"),product.getInt("quantity"),
                                product.getInt("price"),product.getString("state"));
                        store.addUProduct(product1);
                    }
                }
                k++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public List<Store> getCatOfStores(List<Store> stores){
        for(Store store: stores){
            List<Product> products = store.getProducts();
            for(Product product: products){
                try {
                    //make a call to catlog API to get Cat & Sup Cat & Sub Cat
                    URL url = new URL("http://catalog-engine.olastore.com/v1/category/tree/product/"+product.getId()+"/path");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //Parse the data
                    BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    StringBuffer result = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    String fResult = result.toString();
                    JSONObject jsonObject = new JSONObject(fResult);
                    JSONArray primaryPath = jsonObject.getJSONArray("primary_path");
                    for(int i=0;i<primaryPath.length();i++){
                        JSONObject poj = primaryPath.getJSONObject(i);
                        String nidPath = poj.getString("node_id_path");
                        String[] ids = nidPath.split(">");
                        store.addSupCat(ids[0]);
                        store.addCat(ids[1]);
                        if(ids.length>2){
                            store.addSubCat(ids[2]);
                        }
                    }
                    System.out.println();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        System.out.println();
        pushDataToES(stores);
        return stores;
    }
    public void pushDataToES(List<Store> stores){
        System.out.println("sdafasd");
        for(Store store: stores){
            try {
                URL url = new URL("http://localhost:9200/stores_live/store/"+store.getId());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                //make data

                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(store.getJsonObject().toString().getBytes());

                //Call
                InputStream inputStream = urlConnection.getInputStream();
                String ss = inputStream.toString();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        GetLiveData getLiveData = new GetLiveData();
        List<Store> stores1 = getLiveData.getStoresData();;
        stores1 = getLiveData.getProductsOfStores(stores1);
        //stores1 = getLiveData.getCatOfStores(stores1);
        getLiveData.pushDataToES(stores1);
    }

}
