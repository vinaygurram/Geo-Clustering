package old.Util;

/**
 * Created by gurramvinay on 6/15/15.
 */
public class Location {

    private double lat;
    private double lon;
    public Location(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String toString(){
        return lat+","+lon;
    }
}
