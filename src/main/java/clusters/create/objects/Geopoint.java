package clusters.create.objects;

/**
 * Generic Object to denote the location of any object
 * It has both latitude and longitude
 * Created by gurramvinay on 8/10/15.
 */
public class Geopoint {
    private double longitude;
    private double latitude;
    private static final double earthRadius = 6378.1; // kilometers

    public Geopoint(double lat, double lon){
        this.latitude = lat;
        this.longitude = lon;
    }

    //Object methods
    public static double getDistance(Geopoint l1,Geopoint l2){

        double lat1 = Math.toRadians(l1.getLatitude());
        double lat2 = Math.toRadians(l2.getLatitude());
        double long1 = Math.toRadians(l1.getLongitude());
        double long2 = Math.toRadians(l2.getLongitude());

        double dist = earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
                * Math.cos(lat2) * Math.cos(Math.abs(long1 - long2)));
        return dist;
    }

    //Getters and Setters
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    //common methods
    @Override
    public String toString(){
        return new StringBuilder().append(latitude).append(",").append(longitude).toString();
    }
    @Override
    public Geopoint clone(){
        return new Geopoint(latitude,longitude);
    }
}
