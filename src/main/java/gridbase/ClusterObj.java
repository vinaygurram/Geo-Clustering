package gridbase;

import com.github.davidmoten.geo.GeoHash;
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
    private int productCount;
    private double distance ;
    private String geoHash;


    //Object methods

    public void addPoint(ClusteringPoint p){
        //add to points
        points.add(p);

        //Update products
        if(products==null){
            products = p.getProducts();
        }else{
            products = mergeShopProducts(products,p.getProducts());
        }
    }

    public void removePoint(ClusteringPoint p){
        points.remove(p);
    }

    /**
     * Merges products and returns them
     * Optimization :: Use bitset
     * Optimization :: sort and merge arrays it will be faster
     * */
    public String[] mergeShopProducts(String[] s1, String[] s2){
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

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
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

    @Override
    public String toString(){
        return new StringBuilder().append(points).append(" Pcount ").append(products.length).append("\n").toString();
    }


    public JSONObject getJSON(){
        JSONObject jo = new JSONObject();
        JSONObject cluster = new JSONObject();

        cluster.put("name",geoHash);
        cluster.put("cluster_id","_id");
        cluster.put("load",1);
        cluster.put("rank",1);
        JSONArray jsonArray = new JSONArray();
        for(ClusteringPoint c:  points){
            jsonArray.put(c.getId());
        }
        cluster.put("shop_ids", jsonArray);
        cluster.put("geohash",geoHash);
        jo.put("clusterOB",cluster);
        return jo;
    }

}
