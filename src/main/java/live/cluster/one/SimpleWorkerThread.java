package live.cluster.one;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import gridbase.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by gurramvinay on 7/1/15.
 */
public class SimpleWorkerThread implements  Runnable{
    GeoCLusteringNew geoCLusteringNew;
    String geohash;
    HashMap<String,List<String>> map;


    SimpleWorkerThread(GeoCLusteringNew geoCLusteringNew,String geohash,HashMap<String,List<String>> map){
        this.geoCLusteringNew = geoCLusteringNew;
        this.geohash = geohash;
        this.map = map;
    }

    public void run() {
        List<ESShop> esShopgList = geoCLusteringNew.getStoresForGeoHash(geohash,map);
        List<ClusteringPoint> clusterPoints = geoCLusteringNew.getClusteringPoints(esShopgList);
        LatLong ll = GeoHash.decodeHash(geohash);
        String[] pp = {};
        ClusteringPoint cp = new ClusteringPoint("gggggeee",pp,new Geopoint(ll.getLat(),ll.getLon()));
        ClusterStrategyNew clusterStrategyNew = new ClusterStrategyNew();
        List<ClusterObj> clusterObjList = clusterStrategyNew.createClusters(cp,clusterPoints);
        //GeoCLusteringNew.clusterNumber.incrementAndGet();
        geoCLusteringNew.pushClusterToES(clusterObjList);

    }
}
