package gridbase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 6/30/15.
 */
public class ClusterObj {

    //Fields
    private String name;
    private List<ClusteringPoint> points = new ArrayList<ClusteringPoint>();
    private String[] products = null;
    private int productCount;
    private double distance ;


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


}
