package live.cluster.one.DBObjects;

import gridbase.Geopoint;
import org.json.JSONObject;

/**
 * Created by gurramvinay on 8/3/15.
 */
public class ListingObject {
    public String productId;
    public String store_id;
    public String store_name;
    public String store_state;
    public Geopoint location;
    public String state;


    //Getters and Setters

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public Geopoint getLocation() {
        return location;
    }

    public void setLocation(Geopoint location) {
        this.location = location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getStore_state() {
        return store_state;
    }

    public void setStore_state(String store_state) {
        this.store_state = store_state;
    }

    //Common Methods
    public boolean equals(Object object){
        if(!(object instanceof ListingObject)) return false;
        ListingObject listingObject = (ListingObject) object;
        if(listingObject.getProductId().contentEquals(productId)) return true;
        return false;
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("id",store_id);
        jsonObject1.put("name",store_name);
        jsonObject1.put("state",store_state);
        jsonObject1.put("location",location.getLatitude()+","+location.getLongitude());
        jsonObject.put("store",jsonObject1);
        jsonObject1 = new JSONObject();
        jsonObject1.put("id",productId);
        jsonObject1.put("state",state);
        jsonObject.put("product",jsonObject1);
        return  jsonObject.toString();
    }

    public String giveAsBulk(){
        String ss = "";
        ss += "{\"index\" : {\"_id\":\""+store_id+"-"+productId+"\"}}";
        ss +="\n";
        ss+= toString();
        ss+= "\n";
        return ss;
    }
}
