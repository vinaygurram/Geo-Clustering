package reports.DBObjects;

import clusters.create.objects.Geopoint;
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
    public String sup_cat_id="";
    public String cat_id = "";
    public String sub_cat_id ="";
    public String state;
    public String available;


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

    public String getSup_cat_id() {
        return sup_cat_id;
    }

    public void setSup_cat_id(String sup_cat_id) {
        this.sup_cat_id = sup_cat_id;
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getSub_cat_id() {
        return sub_cat_id;
    }

    public void setSub_cat_id(String sub_cat_id) {

        this.sub_cat_id = sub_cat_id;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
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
        JSONObject productObject = new JSONObject();
        JSONObject storeObject = new JSONObject();

        storeObject.put("id",store_id);
        storeObject.put("state",store_state);
        storeObject.put("location", location.getLatitude() + "," + location.getLongitude());

        productObject.put("id", productId);
        productObject.put("state", state);
        productObject.put("available",available);
        productObject.put("sup_category_id", sup_cat_id);
        productObject.put("sub_category_id", sub_cat_id);
        productObject.put("category_id", cat_id);


        jsonObject.put("product_details",productObject);
        jsonObject.put("store_details",storeObject);
        return  jsonObject.toString();
    }

    public String giveAsBulk(){
        String ss = "";
        ss += "{\"index\" : {\"_id\":\""+store_id+"-"+productId+"\"}}";
        ss +="\n";
        ss+= this.toString();
        ss+= "\n";
        return ss;
    }
}
