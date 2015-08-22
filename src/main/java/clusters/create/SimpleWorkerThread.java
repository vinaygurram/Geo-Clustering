package clusters.create;

import clusters.create.objects.ClusterObj;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import clusters.create.objects.Geopoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread to from clusters given the store ids
 * This will push the results into ES as well
 * Created by gurramvinay on 7/1/15.
 */
public class SimpleWorkerThread implements  Runnable{
    String geohash;
    List<String> points = new ArrayList<String>();


    SimpleWorkerThread(List<String> points,String geohash){
        this.geohash = geohash;
        this.points = points;
    }

    public void run() {
        ClusterStrategy clusterStrategyNew = new ClusterStrategy();
        LatLong gll = GeoHash.decodeHash(geohash);
        Geopoint geopoint = new Geopoint(gll.getLat(),gll.getLon());
        List<ClusterObj> clusterObjList = clusterStrategyNew.createClusters(geopoint, points);
        GeoClustering.pushClusterToES(clusterObjList);
        System.out.println(GeoClustering.jobsRun.incrementAndGet());
    }




}
