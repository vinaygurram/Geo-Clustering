package reports.DBObjects;

import clusters.create.objects.Geopoint;
import org.json.JSONObject;

/**
 * Seller Inventory Store Object
 * Created by gurramvinay on 8/3/15.
 */
public class Store {
    private String name;
    private Geopoint location;
    private String id;
    private String state;

    public Store(String name, double lat, double lon, String id, String state){
        this.name = name;
        this.location = new Geopoint(lat,lon);
        this.id = id;
        this.state = state;
    }

    //Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    //Common Methods
    public String toString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("location",location.getLatitude()+","+location.getLongitude());
        jsonObject.put("name",name);
        jsonObject.put("state",state);
        return jsonObject.toString();
    }

    public boolean equals(Object object){
        if (!(object instanceof Store)) return false;
        Store oStore = (Store) object;
        if(id.contentEquals(oStore.getId())) return true;
        return false;
    }

}
