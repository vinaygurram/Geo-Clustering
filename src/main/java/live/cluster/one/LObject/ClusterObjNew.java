package live.cluster.one.LObject;

import gridbase.ClusterObj;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 6/30/15.
 */
public class ClusterObjNew {

    //Fields
    private String name;
    private List<String> points = new ArrayList<String>();
    private double distance ;
    private String geoHash;
    private double rank;
    private int num_stores;
    private int productsCount;
    private int subCatCount;
    private CatalogTree catalogTree;
    private boolean status;


    //Object methods

    public void addPoint(String p){
        //add to points
        points.add(p);
        num_stores++;

    }


    //Getter and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
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

    public int getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(int productsCount) {
        this.productsCount = productsCount;
    }

    public int getSubCatCount() {
        return subCatCount;
    }

    public void setSubCatCount(int subCatCount) {
        this.subCatCount = subCatCount;
    }

    public CatalogTree getCatalogTree() {
        return catalogTree;
    }

    public void setCatalogTree(CatalogTree catalogTree) {
        this.catalogTree = catalogTree;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return new StringBuilder().append(points).append(" Pcount ").append(productsCount).append("\n").toString();
    }


    public JSONObject getJSON(){
        JSONObject jo = new JSONObject();
        JSONObject cluster = new JSONObject();

        cluster.put("name",geoHash);
        cluster.put("load",1);
        cluster.put("rank",rank);
        cluster.put("distance",distance);
        JSONArray jsonArray = new JSONArray();
        for(String c:  points){
            jsonArray.put(c);
        }
        cluster.put("cat_tree",catalogTree.getJSONOArray());
        cluster.put("sub_cat_count",subCatCount);
        cluster.put("product_count",productsCount);
        cluster.put("shop_ids", jsonArray);
        cluster.put("geohash",geoHash);
        cluster.put("stores_count",num_stores);
        cluster.put("status",status);
        jo.put("clusterOB",cluster);
        return jo;
    }
}
