package live.cluster.one;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import gridbase.*;

import java.util.List;

/**
 * Created by gurramvinay on 7/1/15.
 */
public class SimpleWorkerThread implements  Runnable{
    GeoCLustering geoCLustering;
    String geohash;

    SimpleWorkerThread(GeoCLustering geoCLustering,String geohash){
        this.geoCLustering = geoCLustering;
        this.geohash = geohash;
    }

    public void run() {
        List<ESShop> esShopgList = geoCLustering.getStoresForGeoHash(geohash);
        List<ClusteringPoint> clusterPoints = geoCLustering.getClusteringPoints(esShopgList);
        LatLong ll = GeoHash.decodeHash(geohash);
        String[] pp = {};
        ClusteringPoint cp = new ClusteringPoint("gggggeee",pp,new Geopoint(ll.getLat(),ll.getLon()));
        ClusterStrategy clusterStrategy = new ClusterStrategy(clusterPoints,cp);
        List<ClusterObj> clusterObjList = clusterStrategy.createClusters(cp,clusterPoints);
        //GeoCLustering.clusterNumber.incrementAndGet();
        geoCLustering.pushClusterToES(clusterObjList);

    }
}
