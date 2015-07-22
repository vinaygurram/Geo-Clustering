package gridbase;

import java.util.List;

/**
 * Created by gurramvinay on 6/26/15.
 */
public class ClusteringPoint {

    private Geopoint location;
    private String id;
    private String[] products;
    private String[] subCat;
    private boolean isClustered = false;
    private boolean isFnv = false;


    //constructor
    public ClusteringPoint(String id, String[] products, Geopoint location ){
        this.id = id;
        this.products = products;
        this.location = location;
    }

    public ClusteringPoint(String id, List<String> productsList,List<String> subcatLlist, Geopoint location ){
        this.id = id;
        this.products = new String[productsList.size()];
        for(int i=0;i<productsList.size();i++){
            this.products[i] = productsList.get(i);
        }
        this.subCat = new String[subcatLlist.size()];
        for(int i=0;i<subcatLlist.size();i++){
            this.subCat[i] = subcatLlist.get(i);
        }
        this.location = location;
    }

    //Getter and Setter Methods
    public Geopoint getLocation() {
        return location;
    }

    public void setLocation(Geopoint location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getProducts() {
        return products;
    }

    public void setProducts(String[] products) {
        this.products = products;
    }

    public String[] getSubCat() {
        return subCat;
    }

    public boolean isClustered() {
        return isClustered;
    }

    public void setIsClustered(boolean isClustered) {
        this.isClustered = isClustered;
    }

    public void setSubCat(String[] subCat) {
        this.subCat = subCat;
    }

    public boolean isFnv() {
        return isFnv;
    }

    public void setIsFnv(boolean isFnv) {
        this.isFnv = isFnv;
    }

    @Override
    public String toString(){
        return new StringBuilder().append(id).append(" ").append(location).append(";;;").toString();
    }
}
