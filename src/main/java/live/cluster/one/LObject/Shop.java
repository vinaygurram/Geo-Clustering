package live.cluster.one.LObject;

import java.util.BitSet;

public class Shop {

    private double lat;
    private double lon;
    private String name;
    private String id;
    private BitSet aa;


    public Shop(double lat, double lon, String id){
        this.lat = lat;
        this.lon = lon;
        this.id = id;
        this.name = id + "ffShop";
        aa = new BitSet(100);
        for(int i=0;i<100;i++){
            double dd = Math.random();
            if(dd>0.5) {
                aa.set(i);
            }
        }
    }

    @Override
    public String toString(){
        return new StringBuilder().append(id).append(":").append(lat).append(",").append(lon).toString();
    }

    //Getter and Setter

    public String getProducst(){
        return aa.toString();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}