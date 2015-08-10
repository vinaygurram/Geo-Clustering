package live.cluster.one.LObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 7/9/15.
 */
public class Store {
    private String id;
    private Geopoint location;
    private List<Product> products= new ArrayList<Product>();
    private int productsCount;
    private List<String> supCatList = new ArrayList<String>();
    private List<String> catList = new ArrayList<String>();
    private List<String> subCatList=  new ArrayList<String>();
    private int supCatCount;
    private int subCatCount;
    private int catCount;

    //constructor
    public Store(String id,Geopoint location){
        this.id = id;
        this.location = location;
    }


    //Object Methods

    //Assuming seller service gives unique Product
    public void addUProduct(Product product){
        products.add(product);
        productsCount++;
    }

    public void addSupCat(String scat){
        if(!supCatList.contains(scat)){
            supCatList.add(scat);
            supCatCount++;
        }
    }

    public void addSubCat(String scat){
        if(subCatList.contains(scat)){
            subCatList.add(scat);
            subCatCount++;
        }
    }

    public void addCat(String cat){
        if(!catList.contains(cat)){
            catList.add(cat);
            catCount++;
        }
    }


    //Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geopoint getLocation() {
        return location;
    }

    public void setLocation(Geopoint location) {
        this.location = location;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(int productsCount) {
        this.productsCount = productsCount;
    }

    public List<String> getSupCatList() {
        return supCatList;
    }

    public void setSupCatList(List<String> supCatList) {
        this.supCatList = supCatList;
    }

    public List<String> getCatList() {
        return catList;
    }

    public void setCatList(List<String> catList) {
        this.catList = catList;
    }

    public List<String> getSubCatList() {
        return subCatList;
    }

    public void setSubCatList(List<String> subCatList) {
        this.subCatList = subCatList;
    }

    public int getSupCatCount() {
        return supCatCount;
    }

    public void setSupCatCount(int supCatCount) {
        this.supCatCount = supCatCount;
    }

    public int getSubCatCount() {
        return subCatCount;
    }

    public void setSubCatCount(int subCatCount) {
        this.subCatCount = subCatCount;
    }

    public int getCatCount() {
        return catCount;
    }

    public void setCatCount(int catCount) {
        this.catCount = catCount;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Store)) return false;
        Store product = (Store) obj;
        if(product.getId()!=this.id) return false;
        if(!product.getLocation().equals(location)) return false;
        return true;
    }

    public JSONObject getJsonObject(){
        JSONObject fJo = new JSONObject();
        JSONObject jo = new JSONObject();
        jo.put("store_id",id);
        jo.put("location",location.toString());
        jo.put("sup_cat_list",supCatList.toString());
        jo.put("sub_cat_list",subCatList.toString());
        jo.put("cat_list",catList.toString());
        JSONArray productsA = new JSONArray();
        for(Product product: products){
            productsA.put(product.getJSONObj());
        }
        jo.put("products",productsA);
        jo.put("products_count",productsCount);
        fJo.put("store",jo);
        return  fJo;
    }
}
