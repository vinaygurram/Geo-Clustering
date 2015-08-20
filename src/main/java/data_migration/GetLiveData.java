package data_migration;

import clusters.create.objects.Geopoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public List<String> generateIds(){
        List<String> list = new ArrayList<String>();
        try {
            int k=1;
            while(k<170){
                URL url = new URL("http://seller-engine.olastore.com/stores/100006/inventories?page="+k);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuffer result = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                String fResult = result.toString();
                JSONObject jsonObject = new JSONObject(fResult);
                JSONArray jsonArray = jsonObject.getJSONArray("products");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String id =jsonObject1.getString("id");
                    if(!list.contains(id)){
                       list.add(id);
                    }
                }
                System.out.println(k);
                k++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }


    public List<PCat> GetCat(List<String> ids) {
        List<PCat> pCats = new ArrayList<PCat>();
        BufferedReader bufferedReader;
        try {
            for(String id:ids){
                URL url = new URL("http://catalog-engine.olastore.com/v1/category/tree/product/"+id+"/path");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                bufferedReader =  new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuffer result = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                String fResult = result.toString();
                JSONObject jsonObject = new JSONObject(fResult);
                JSONArray ppath = jsonObject.getJSONArray("primary_path");
                for(int i =0;i<ppath.length();i++){
                    JSONObject jo = ppath.getJSONObject(i);
                    String node_path = jo.getString("node_id_path");
                    String[] cats = node_path.split(">");
                    String sbc = "";
                    if(cats.length>2) sbc = cats[2];
                    PCat pCat = new PCat(id,cats[0],sbc,cats[1]);
                    pCats.add(pCat);
                }
            }
            return pCats;
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<PCat>();
    }

    public List<PCat> generateProductsFromFile(String path){
        FileReader fileReader=null;
        BufferedReader bufferedReader=null;
        List<PCat> rPcat = new ArrayList<PCat>();
        try {
            fileReader = new FileReader(path);
            bufferedReader = new BufferedReader(fileReader);
            String line= bufferedReader.readLine();
            while((line=bufferedReader.readLine())!=null){
                String[] lineArray = line.split("\t");
                String sbCat = "";
                if(lineArray.length==4){
                    sbCat = lineArray[3];
                }
                PCat pCat = new PCat(lineArray[0],lineArray[1],sbCat,lineArray[2]);
                rPcat.add(pCat);
            }
            bufferedReader.close();
            fileReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
                fileReader.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return rPcat;
    }


    public void pushPcatToES(){
        //List<String> ids = generateIds();
        HttpClient httpClient = null;
        httpClient = HttpClientBuilder.create().build();
        long l =1;
        //List<PCat> pCats = GetCat(ids);
        List<PCat> pCats = generateProductsFromFile("/Users/gurramvinay/Downloads/catalog_segment.csv");
        for(PCat pCat : pCats){
            try {

                HttpPost postRequest = new HttpPost("http://localhost:9200/products_list_new/products");
                postRequest.setEntity(new StringEntity(pCat.getJSON().toString()));
                //send post request
                HttpResponse response = httpClient.execute(postRequest);

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                JSONObject jsonObject = new JSONObject(result.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void updateStoresWithFNV(String path){
        try {

            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while((line = bufferedReader.readLine())!=null){
                String[] cts = line.split("\t");
                String fnv = cts[1];
                String id = cts[3];
                boolean fnvB = false;
                if(fnv.contentEquals("FnV")){
                    fnvB = true;
                }
                String ESAPI = "http://localhost:9200/stores_live/store/"+id+"/_update";
                JSONObject jo = new JSONObject();
                jo.put("script","ctx._source.fnv = \""+fnvB+"\"");

                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(ESAPI);
                post.setEntity(new StringEntity(jo.toString()));

                HttpResponse response = httpClient.execute(post);
                long code= response.getEntity().getContentLength();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //
    public static void main(String[] args){
        GetLiveData getLiveData = new GetLiveData();
        //List<Store> stores1 = getLiveData.getStoresData();;
        //stores1 = getLiveData.getProductsOfStores(stores1);
        //stores1 = getLiveData.getCatOfStores(stores1);
        //getLiveData.pushDataToES(stores1);
        //getLiveData.pushPcatToES();
        // getLiveData.pushPcatToES();
        getLiveData.updateStoresWithFNV("/Users/gurramvinay/Downloads/stores-fnv.txt");
    }

}
