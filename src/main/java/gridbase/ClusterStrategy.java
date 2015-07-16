package gridbase;

import com.github.davidmoten.geo.GeoHash;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gurramvinay on 6/26/15.
 * defining strategy to create the goe hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * low level distance matraix is computer for each geo hash
 */

public class ClusterStrategy {
    private DistanceMatrix distanceMatrix;

    //Constructor
    ClusterStrategy(List<String> points,String geoHash){
 //       this.distanceMatrix = new DistanceMatrix(geoHash,points);
    }


    public List<ClusterObj> createClusters(ClusteringPoint geoHash, List<ClusteringPoint> points){

        List<ClusterObj> clusters = new ArrayList<ClusterObj>();
        while(points.size()>5){
            ClusterObj clusterObj = new ClusterObj();
            //while(clusterObj.getPoints().size()<4 && clusterObj.getProductCount()<3000 &&clusterObj.getSub_cat().length<120){
            while(clusterObj.getSub_cat().length<120){
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
            cluster.setDistance(Geopoint.getDistance(nearestPoint.getLocation(),geoHash.getLocation()));
            return cluster;
        }

        //Cluster has some points
        //find probable points
        ClusteringPoint[] tempPointList = findProbablePoints(cluster.getPoints(),points);

        //find best clustering point out of those using shortest distance
        //Choosing the best possbile point in the cluster
        ClusteringPoint goodClusteringPoint =null;
        double leastDistance =Double.MAX_VALUE;
        for(ClusteringPoint p : tempPointList){
            double dd = getShortestDistance(p,geoHash,cluster.getPoints());
            if(dd<leastDistance){
                leastDistance = dd;
                goodClusteringPoint = p;
            }
        }
        //Add it to Cluster
        cluster.setDistance(leastDistance);
        cluster.addPoint(goodClusteringPoint);
        points.remove(goodClusteringPoint);

        return cluster;
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
     * Fav Factor Logic
     * Fav Factor = Distance + Product Count (Will be changed later)
     * @return ClusterObj favoured cluster
     * @param clusterList list of clusters available
     * */
    public ClusterObj getFavouredCluster(List<ClusterObj> clusterList){
        double fav_facotr = Double.MIN_VALUE;
        ClusterObj rCluster =null;
        for(ClusterObj c : clusterList){
            double tempFav = (double)c.getProducts().length + c.getDistance();
            if(fav_facotr<tempFav) rCluster = c;
        }
        return  rCluster;
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
        return Geopoint.getDistance(c1.getLocation(),c2.getLocation());
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
