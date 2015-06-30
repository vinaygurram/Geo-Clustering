package old.Util;

import org.apache.commons.math3.ml.clustering.Clusterable;
import java.util.Arrays;

public class LocationWrapper implements Clusterable {
    private double[] points;
    private Location location;

    public LocationWrapper(Location location) {
        this.location = location;
        this.points = new double[] { location.getLat(), location.getLon() };
    }

    public Location getLocation() {
        return location;
    }

    public double[] getPoint() {
        return points;
    }

    @Override
    public String toString(){
        return Arrays.toString(points);
    }
}