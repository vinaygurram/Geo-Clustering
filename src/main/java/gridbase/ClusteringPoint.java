package gridbase;

/**
 * Created by gurramvinay on 6/26/15.
 */
public class ClusteringPoint {

    private Geopoint location;
    private String id;
    private String[] products;


    //constructor
    public ClusteringPoint(String id, String[] products, Geopoint location ){
        this.id = id;
        this.products = products;
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

    @Override
    public String toString(){
        return new StringBuilder().append(id).append(" ").append(location).append(";;;").toString();
    }
}
