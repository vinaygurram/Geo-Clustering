package live.cluster.one;

import com.github.davidmoten.geo.GeoHash;
import gridbase.ClusterObj;
import gridbase.ClusteringPoint;
import gridbase.DistanceMatrix;
import gridbase.Geopoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gurramvinay on 6/26/15.
 * defining strategy to create the goe hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * low level distance matraix is computer for each geo hash
 */

public class ClusterStrategyNew {
    private DistanceMatrix distanceMatrix;
    List<ClusteringPoint> points;
    ClusteringPoint geoHash;

    //Constructor
    ClusterStrategyNew(List<ClusteringPoint> points, ClusteringPoint geoHash){
        List<ClusteringPoint> ttpoints = new ArrayList<ClusteringPoint>(points);
        ttpoints.add(geoHash);
        this.distanceMatrix = new DistanceMatrix(ttpoints);
        this.geoHash = geoHash;
        this.points = points;
    }


    public List<ClusterObj> createClusters(ClusteringPoint geoHash, List<ClusteringPoint> points){

        List<ClusterObj> clusters = new ArrayList<ClusterObj>();
        while(points.size()>2){
            ClusterObj clusterObj = new ClusterObj();
            //while(clusterObj.getPoints().size()<4 && clusterObj.getProductCount()<3000 &&clusterObj.getSub_cat().length<120){
            while(clusterObj.getSub_cat().length<120 && points.size()>0){
                clusterObj = formCluster(geoHash,clusterObj,points);
            }
            clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(),geoHash.getLocation().getLongitude()));
            clusters.add(clusterObj);
        }
        ClusterObj clusterObj = new ClusterObj();
        for(ClusteringPoint cp: points){
            clusterObj.addPoint(cp);
        }
        clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(),geoHash.getLocation().getLongitude()));
        clusters.add(clusterObj);
        return clusters;
    }

    public ClusterObj formCluster(ClusteringPoint geoHash, ClusterObj cluster, List<ClusteringPoint> points){

        //Consider the case of only geoHash point
        if(cluster.getPoints().size()==0){
            ClusteringPoint nearestPoint = getDNearestPoint(geoHash,points);
            points.remove(nearestPoint);
            cluster.addPoint(nearestPoint);
            double distance = Geopoint.getDistance(nearestPoint.getLocation(), geoHash.getLocation());
            cluster.setDistance(distance);
            double rank = getFavourFactor(distance,nearestPoint.getProducts().length);
            cluster.setRank(rank);
            return cluster;
        }

        //Cluster has some points
        //find probable points
        ClusteringPoint[] tempPointList = findProbablePoints(cluster.getPoints(),points);

        //find best clustering point out of those using shortest distance
        //Choosing the best possible point in the cluster
        ClusteringPoint goodClusteringPoint =null;
        double bestDistance= Double.MAX_VALUE;
        List<String> bestProductCoverage = new ArrayList<String>();
        ClusteringPoint bestPoint = null;

        double fav = Double.MIN_VALUE;

        for(ClusteringPoint p : tempPointList){
            double dd = getShortestDistance(p,geoHash,cluster.getPoints());
            List<String> productsCoverage = getProductCoverage(p,cluster);
            double temp = getFavourFactor(dd,productsCoverage.size());
            if(temp>fav){
                fav = temp;
                bestProductCoverage = productsCoverage;
                bestDistance = dd;
                bestPoint = p;
            }
        }
        //Add it to Cluster
        //Doint it multiple times but let it go
        cluster.setDistance(bestDistance);
        cluster.addPoint(bestPoint);
        cluster.setRank(fav);
        points.remove(bestPoint);
        String[] pps = new String[bestProductCoverage.size()];
        pps = bestProductCoverage.toArray(pps);
        cluster.setProducts(pps);

        return cluster;
    }
     /**
      * Get product coverage with this point
      * */
    public List<String> getProductCoverage(ClusteringPoint clusteringPoint, ClusterObj clusterObj){
        String[] clusterProducts = clusterObj.getProducts();
        String[] pointProducts = clusteringPoint.getProducts();
        List<String> finalProducts = new ArrayList<String>();
        for(int i=0;i<clusterProducts.length;i++){
            finalProducts.add(clusterProducts[i]);
        }
        for(int i=0;i<pointProducts.length;i++){
            if(!finalProducts.contains(pointProducts[i])){
                finalProducts.add(pointProducts[i]);
            }
        }
        return finalProducts;
    }

    /**
     * Get nearest point according to distance
     * */
    public ClusteringPoint getDNearestPoint(ClusteringPoint p, List<ClusteringPoint> points){
        HashMap<String,Double>ddNode = distanceMatrix.getNodeDistanceMatrix(p.getId());
        double distanceN = Double.MAX_VALUE;
        ClusteringPoint cp = null;
        for(ClusteringPoint cd : points){
            double tempDistance = distanceMatrix.getDistance(cd.getId(),p.getId());
            if (tempDistance<distanceN) cp = cd;
        }
        return  cp;
    }




    //Helper methods

    /**
     * @return  3 points that are near to cluster
     * */
    //Need to optimize the way of finding the 3 largest
    //the number is very small (5) so better to same complexity for heap/selection/raw
    public ClusteringPoint[] findProbablePoints(List<ClusteringPoint> clusterPoints, List<ClusteringPoint> pointList){

        ClusteringPoint[] largest = new ClusteringPoint[3];

        ClusteringPoint laregestF = null;
        double distance = Double.MAX_VALUE;
        for(ClusteringPoint point :pointList){
            double tempD = getDistanceFromCluster(point,clusterPoints);
            if(distance>tempD){
                distance = tempD;
                laregestF = point;
            }
        }
        ClusteringPoint largestS=null;
        distance = Double.MAX_VALUE;
        for(ClusteringPoint point :pointList){
            if(point.getId()==laregestF.getId()) continue;
            double tempD = getDistanceFromCluster(point,clusterPoints);
            if(distance>tempD){
                distance = tempD;
                largestS = point;
            }
        }
        ClusteringPoint largestT=null;
        distance = Double.MAX_VALUE;
        for(ClusteringPoint point :pointList){
            if(point.getId()==laregestF.getId() || point.getId()==largestS.getId()) continue;
            double tempD = getDistanceFromCluster(point,clusterPoints);
            if(distance>tempD){
                distance = tempD;
                largestT = point;
            }
        }
        largest[0] = laregestF;
        largest[1] =largestS;
        largest[2] = largestT;
        return largest;
    }


    /**
     * Get avg distance from existing cluster
     * */
    public double getDistanceFromCluster(ClusteringPoint point, List<ClusteringPoint> pointList){
        double ddd = 0d;
        for(ClusteringPoint dd: pointList){
            ddd += Geopoint.getDistance(point.getLocation(),dd.getLocation());
        }
        return (ddd/pointList.size());
    }

    /**
     * Get Favoured Cluster in List of clusters
     * Fav Factor Logic D--> Distance, P ---> Product Count
     * Dmax = 15; Product Count Pmax = 17000
     * Fav Factor = (1/2)*(Dmax/D) + (1/2) * (P/Pmax)
     * @return
     * */
    public double getFavourFactor(double distance, int products){
        double d = 15000/distance;
        d = d/2;
        double p = ((double)products)/17000;
        p =p/2;
        return  d+p;
    }


    /**
     * computes the shortest Custom distance between a point and a cluster
     * distance d = Min(Di+Si)
     * Di --->distance from geohash to point,
     * Si--> shortest path co nnecting all the points in cluster and new point
     * @return Distance with this particular point
     * @param cp probable point that can be added to cluster
     * @param geohashPoint geoHash for which cluster is being calculated
     * @param points list of points that are already in cluster
     * */
    public double getShortestDistance(ClusteringPoint cp, ClusteringPoint geohashPoint, List<ClusteringPoint> points){


        double smallestDistace = Double.MAX_VALUE;
        points.add(cp);
        for(ClusteringPoint tp : points){
            double di = distanceBtPoints(tp,geohashPoint);
            double si = getShortestDistanceWithPoint(tp,points);
            double total = di+si;
            if(total <smallestDistace) {
                smallestDistace = total;
            }
        }
        points.remove(cp);
        return smallestDistace;
    }



    /**
     * helper method
     * @return the distance between two geo points
     * */
    public double distanceBtPoints(ClusteringPoint c1, ClusteringPoint c2){
        return Geopoint.getDistance(c1.getLocation(), c2.getLocation());
    }

    /**
     * computes the shortest path using all possible combinations
     * @return Double shortest distance which connects all the distances
     * */
    public double getShortestDistanceWithPoint(ClusteringPoint cp2, List<ClusteringPoint> list){

        try {
            String[] idsString = new String[list.size()];
            for(int i=0;i<list.size();i++){
                idsString[i] = list.get(i).getId();
            }
            List<List<String>> permutations = permute(idsString);
            double gDistance = Double.MAX_VALUE;
            for(List<String> possiblity : permutations){

                double tempDist = distanceMatrix.getDistance(cp2.getId(),possiblity.get(0));
                for(int i=0;i<possiblity.size()-1;i++){
                    tempDist+=distanceMatrix.getDistance(possiblity.get(i),possiblity.get(i+1));
                }
                if(tempDist < gDistance) gDistance = tempDist;
            }
            return  gDistance;


        }catch (Exception e){
            e.printStackTrace();
        }

        return Double.MAX_VALUE;


    }

    /**
     * Creates all possible String ids;
     *
     * */
    public List<List<String>> permute(String[] ids) {
        List<List<String>> permutations = new ArrayList<List<String>>();
        //empty list to continue the loop
        permutations.add(new ArrayList<String>());

        for ( int i = 0; i < ids.length; i++ ) {
            // create a temporary container to hold the new permutations
            // while we iterate over the old ones
            List<List<String>> current = new ArrayList<List<String>>();
            for ( List<String> permutation : permutations ) {
                for ( int j = 0, n = permutation.size() + 1; j < n; j++ ) {
                    List<String> temp = new ArrayList<String>(permutation);
                    temp.add(j, ids[i]);
                    current.add(temp);
                }
            }
            permutations = new ArrayList<List<String>>(current);
        }
        return permutations;
    }
}
