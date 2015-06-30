package gridbase;

/**
 * Created by gurramvinay on 6/19/15.
 */
public class ESShop {
    private String name;
    private String[] pids;
    private String id;
    private Geopoint location;

    public ESShop(String name,String[] pids,String id, Geopoint location){
        this.name = name;
        this.pids = pids;
        this.id = id;
        this.location = location;
    }



    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPids() {
        return pids;
    }

    public void setpis(String[] pids) {
        this.pids = pids;
    }

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

    @Override
    public ESShop clone(){
        ESShop esShop = new ESShop(name,pids,id,location.clone());
        return esShop;
    }
}
