package clusters.create;

import clusters.create.LObject.ClusterObjNew;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import clusters.create.LObject.Geopoint;

import java.util.ArrayList;
import java.util.List;

/**
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
        ClusterStrategyNew clusterStrategyNew = new ClusterStrategyNew();
        LatLong gll = GeoHash.decodeHash(geohash);
        Geopoint geopoint = new Geopoint(gll.getLat(),gll.getLon());
        List<ClusterObjNew> clusterObjList = clusterStrategyNew.createClusters1(geopoint, points);
        int pCount = clusterStrategyNew.getProductCount();
        int sbCount  = clusterStrategyNew.getSubCatCount();
        GeoCLusteringNew.pushClusterToES(clusterObjList,pCount,sbCount);
        System.out.println("count - "+GeoCLusteringNew.count++);
    }
}
