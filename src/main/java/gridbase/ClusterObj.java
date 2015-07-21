package gridbase;

import live.cluster.one.LObject.CatalogTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 6/30/15.
 */
public class ClusterObj {

    //Fields
    private String name;
    private List<ClusteringPoint> points = new ArrayList<ClusteringPoint>();
    private String[] products = {};
    private double distance ;
    private String geoHash;
    private double rank;
    private int num_stores;
    private String[] sub_cat={};

    //Object methods

    public void addPoint(ClusteringPoint p){
        //add to points
        points.add(p);
        num_stores++;

        //Update products
//        if(products==null){
//            products = p.getProducts();
//        }else{
//            products = mergeShopProducts(products,p.getProducts());
//        }
//
//        //update sub categories
//        if(sub_cat==null){
//            sub_cat = p.getSubCat();
//        }else {
//            sub_cat = mergeShopProducts(sub_cat,p.getSubCat());
//       }
    }

    /**
     * Merges products and returns them
     * Optimization :: Use bitset
     * Optimization :: merge arrays it will be faster
     * */
    public String[] mergeShopProducts(String[] s1, String[] s2){

        if(s2==null){
            return s1;
        }
        if(s1==null){
            return s2;
        }
        List<String> shops = new ArrayList<String>(100);
        for(int i=0;i<s1.length;i++){
            shops.add(s1[i]);
        }

        for(String s: s2){

            boolean repeated = false;
            for(String d: s1){
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

    //Getter and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClusteringPoint> getPoints() {
        return points;
    }

    public void setPoints(List<ClusteringPoint> points) {
        this.points = points;
    }

    public String[] getProducts() {
        return products;
    }

    public void setProducts(String[] products) {
        this.products = products;
    }


    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String[] getSub_cat() {
        return sub_cat;
    }

    public void setSub_cat(String[] sub_cat) {

        this.sub_cat = sub_cat;
    }

    public int getNum_stores() {
        return num_stores;
    }

    public void setNum_stores(int num_stores) {
        this.num_stores = num_stores;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    @Override
    public String toString(){
        return new StringBuilder().append(points).append(" Pcount ").append(products.length).append("\n").toString();
    }


    public JSONObject getJSON(){
        JSONObject jo = new JSONObject();
        JSONObject cluster = new JSONObject();

        cluster.put("name",geoHash);
        cluster.put("load",1);
        cluster.put("rank",rank);
        cluster.put("distance",distance);
        JSONArray jsonArray = new JSONArray();
        for(ClusteringPoint c:  points){
            jsonArray.put(c.getId());
        }
        cluster.put("sub_cat_count",sub_cat.length);
        cluster.put("product_count",products.length);
        cluster.put("shop_ids", jsonArray);
        cluster.put("geohash",geoHash);
        cluster.put("stores_count",num_stores);
        jo.put("clusterOB",cluster);
        return jo;
    }

}
