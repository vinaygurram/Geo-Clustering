package core;

import Util.LocationWrapper;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import java.util.List;

/**
 * Created by gurramvinay on 6/15/15.
 */
public class ClusterImpl {

    public static void main(String[] args){

        ESDataHandler esDataHandler = new ESDataHandler();
        List<LocationWrapper> locationWrapperList = esDataHandler.getLocationData();
        for(LocationWrapper obj : locationWrapperList){
            System.out.println(obj.getLocation().toString());
        }

        ClusterImpl cimpl = new ClusterImpl();
        cimpl.createKMeansCluster(locationWrapperList,5);
        cimpl.createDBSCANCluster(locationWrapperList,2.0,3);
    }

    public void createKMeansCluster(List<LocationWrapper> locList, int k){
        createKMeansCluster(locList,k,1000);
    }

    public void createKMeansCluster(List<LocationWrapper> lolList, int k,int maxIterations){
        KMeansPlusPlusClusterer<LocationWrapper> clusterer = new KMeansPlusPlusClusterer<LocationWrapper>(k, maxIterations);
        List<CentroidCluster<LocationWrapper>> clusterResults = clusterer.cluster(lolList);
        System.out.println("creating k-means cluster");
        // output the clusters
        for (int i=0; i<clusterResults.size(); i++) {
            System.out.println("Cluster " + i);
            for (LocationWrapper locationWrapper : clusterResults.get(i).getPoints()){
                System.out.println(locationWrapper.getLocation());
                System.out.println();
            }
        }
        System.out.println("end of k-means cluster");
    }

    public void createDBSCANCluster(List<LocationWrapper>inputList, double eps, int minPts ){
        DBSCANClusterer<LocationWrapper> clusterer = new DBSCANClusterer<LocationWrapper>(eps,minPts);
        List<Cluster<LocationWrapper>> clusterResults = clusterer.cluster(inputList);
        System.out.println("creating dbscan cluster");
        for (int i=0; i<clusterResults.size(); i++) {
            System.out.println("Cluster " + i);
            for (LocationWrapper locationWrapper : clusterResults.get(i).getPoints()){
                System.out.println(locationWrapper.getLocation());
                System.out.println();
            }
        }
        System.out.println("end of dbscan cluster");
    }
}