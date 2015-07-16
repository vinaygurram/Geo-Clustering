package live.cluster.one;

import com.github.davidmoten.geo.GeoHash;
import gridbase.*;
import live.cluster.one.LObject.ClusterObjNew;

import java.util.ArrayList;
import java.util.Collections;
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


    public void createDistanceMatrix(Geopoint geoHash, List<String> points){
        List<String> ttpoints = new ArrayList<String>(points);
        this.distanceMatrix = new DistanceMatrix(geoHash,ttpoints);

    }


    public List<ClusterObj> createClusters(Geopoint geoHash, List<String> points){
//
//
//        createDistanceMatrix(geoHash,points);
//        List<ClusterObj> clusters = new ArrayList<ClusterObj>();
//        while(points.size()>2){
//            ClusterObj clusterObj = new ClusterObj();
//            //while(clusterObj.getPoints().size()<4 && clusterObj.getProductCount()<3000 &&clusterObj.getSub_cat().length<120){
//            while(clusterObj.getSub_cat().length<120 && points.size()>1 && clusterObj.getPoints().size()<5){
//                //C****//clusterObj = formCluster(geoHash,clusterObj,points);
//                clusterObj = null;
//            }
//            clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(), geoHash.getLocation().getLongitude()));
//            clusters.add(clusterObj);
//        }
//        ClusterObj clusterObj = new ClusterObj();
//        for(ClusteringPoint cp: points){
//            clusterObj.addPoint(cp);
//        }
//        clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(),geoHash.getLocation().getLongitude()));
//        clusters.add(clusterObj);
//        return clusters;
        return null;
    }

    public List<ClusterObjNew> createClusters1(Geopoint geoHash,  List<String>points){
        createDistanceMatrix(geoHash,points);
        List<ClusterObjNew> rclusters = new ArrayList<ClusterObjNew>();
        if(points.size()==0){
            return rclusters;
        }
        if(points.size()<3){
            ClusterObj clusterObj = new ClusterObj();
            if(points.size()==1){
//                clusterObj.addPoint(GeoCLusteringNew.clusterPoints.get(points.get(0)));
//                double dist = Geopoint.getDistance(geoHash.getLocation(),points.get(0).getLocation());
//                double rank = getFavourFactor_cov(clusterObj.getSub_cat().length,clusterObj.getProducts().length);
//                clusterObj.setDistance(dist);
//                //clusterObj.setRank(1.0);
//                clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(),geoHash.getLocation().getLongitude()));
//                rclusters.add(clusterObj);
                rclusters = new ArrayList<ClusterObjNew>();
                return rclusters;
            }
            if(points.size()==2){
//                clusterObj.addPoint(points.get(0));
//                double dist = getShortestDistance(points.get(1),geoHash,clusterObj.getPoints());
//                clusterObj.addPoint(points.get(1));
//                clusterObj.setDistance(dist);
//                clusterObj.setGeoHash(GeoHash.encodeHash(geoHash.getLocation().getLatitude(),geoHash.getLocation().getLongitude()));
//                double rank = getFavourFactor_cov(clusterObj.getSub_cat().length,clusterObj.getProducts().length);
//                //clusterObj.setRank(1.0);
//                rclusters.add(clusterObj);
                rclusters = new ArrayList<ClusterObjNew>();
                return rclusters;
            }
        }else {
            //create clusters with 3 shops
            List<List<String>> clusters =getAllClusters(points);
            List<ClusterObjNew> validClusters = new ArrayList<ClusterObjNew>();
            for(List<String> clusterObj : clusters){
                ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                if(temp!=null){
                    temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                    validClusters.add(temp);
                }

            }
            System.out.println();
            //create clusters with 2 shops if 3 shops fail
            if(validClusters.size()==0){
                clusters = getAll2Clusters(points);
                validClusters = new ArrayList<ClusterObjNew>();
                for(List<String> clusterObj : clusters){
                    ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                    if(temp!=null){
                        temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                        validClusters.add(temp);
                        System.out.println("s--1");
                    }

                }
            }
            rclusters = validClusters;
        }
        return rclusters;
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
            //double dd = getShortestDistance(p,geoHash,cluster.getPoints());
            double dd =1.0;
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

        if(pointList.size()<4) {
            ClusteringPoint[] clusteringPoints = new ClusteringPoint[pointList.size()];
            for(int i=0;i<clusteringPoints.length;i++){
               clusteringPoints[i] = pointList.get(i);
            }
            return clusteringPoints;
        }
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
     * Get Favoured Cluster in List of clusters
     * Fav Factor = (1/2)*(S/Smax) + (1/2) * (P/Pmax)
     * @return
     * */
    public double getFavourFactor_cov(int subCt, int products){
        double p = ((double)products)/17000;
        double s = ((double)subCt)/240;
        p =p/2;
        s =s/2;
        return  s+p;
    }


//    /**
//     * computes the shortest Custom distance between a point and a cluster
//     * distance d = Min(Di+Si)
//     * Di --->distance from geohash to point,
//     * Si--> shortest path co nnecting all the points in cluster and new point
//     * @return Distance with this particular point
//     * @param cp probable point that can be added to cluster
//     * @param geohashPoint geoHash for which cluster is being calculated
//     * @param points list of points that are already in cluster
//     * */
//    public double getShortestDistance(ClusteringPoint cp, ClusteringPoint geohashPoint, List<ClusteringPoint> points){
//
//
//        double smallestDistace = Double.MAX_VALUE;
//        points.add(cp);
//        for(ClusteringPoint tp : points){
//            double di = distanceBtPoints(tp,geohashPoint);
//            double si = getShortestDistanceWithPoint(tp,points);
//            double total = di+si;
//            if(total <smallestDistace) {
//                smallestDistace = total;
//            }
//        }
//        points.remove(cp);
//        return smallestDistace;
//    }

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
    public double getShortestDistance(String cp,String geohashPoint, List<String> points){


        double smallestDistace = Double.MAX_VALUE;
        points.add(cp);
        for(String tp : points){
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
     * Shortest Distance if store has only 2 points
     * */
    public double getShortestDistanceFor2(Geopoint geohashPoint, List<String> points){

        double dbp = distanceMatrix.getDistance(points.get(0),points.get(1));
        String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude());
        double dfg = distanceMatrix.getDistance(geoString,points.get(0));
        double dsg = distanceMatrix.getDistance(geoString,points.get(1));
        dfg = dfg + dbp;
        dsg = dsg+dbp;
        return dfg>dsg?dsg:dfg;
    }

    //For SLogic
    public double getShortestDistance1(Geopoint geohashPoint, List<String> points){
        double smallestDistace = Double.MAX_VALUE;
        String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude());
        for(String tp : points){
            double di = distanceBtPoints(tp,geoString);

            double si = getShortestDistanceWithPoint(tp,points);
            double total = di+si;
            if(total <smallestDistace) {
                smallestDistace = total;
            }
        }
        return smallestDistace;
    }

//
//    /**
//     * helper method
//     * @return the distance between two geo points
//     * */
//    public double distanceBtPoints(ClusteringPoint c1, ClusteringPoint c2){
//        return Geopoint.getDistance(c1.getLocation(), c2.getLocation());
//    }

//    /**
//     * computes the shortest path using all possible combinations
//     * @return Double shortest distance which connects all the distances
//     * */
//    public double getShortestDistanceWithPoint(ClusteringPoint cp2, List<ClusteringPoint> list){
//        try {
//            List<String> ss = new ArrayList<String>();
//            for(int i=0;i<list.size();i++){
//                if(list.get(i).getId().contentEquals(cp2.getId())) continue;
//                ss.add(list.get(i).getId());
//            }
//            String[] idsString = new String[ss.size()];
//            idsString = ss.toArray(idsString);
//            List<List<String>> permutations = permute(idsString);
//            double gDistance = Double.MAX_VALUE;
//            for(List<String> possiblity : permutations){
//
//                double tempDist = distanceMatrix.getDistance(cp2.getId(),possiblity.get(0));
//                for(int i=0;i<possiblity.size()-1;i++){
//                    tempDist+=distanceMatrix.getDistance(possiblity.get(i),possiblity.get(i+1));
//                }
//                if(tempDist < gDistance) gDistance = tempDist;
//            }
//            return  gDistance;
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return Double.MAX_VALUE;
//    }

    /**
     * helper method
     * @return the distance between two geo points
     * */
    public double distanceBtPoints(String c1, String c2){
        return distanceMatrix.getDistance(c1,c2);
    }


    /**
     * computes the shortest path using all possible combinations
     * @return Double shortest distance which connects all the distances
     * */
    public double getShortestDistanceWithPoint(String cp2, List<String> list){
        try {
            List<String> ss = new ArrayList<String>();
            for(int i=0;i<list.size();i++){
                if(list.get(i).contentEquals(cp2)) continue;
                ss.add(list.get(i));
            }
            String[] idsString = new String[ss.size()];
            idsString = ss.toArray(idsString);
            List<List<String>> permutations = permute(idsString);
            double gDistance = Double.MAX_VALUE;
            for(List<String> possiblity : permutations){

                double tempDist = distanceMatrix.getDistance(cp2,possiblity.get(0));
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
     * Create 3 possible combinations with everything
     **/

    public List<List<String>> getAllClusters(List<String> stringList){

        List<List<String>> totalList = new ArrayList<List<String>>();
        for(int i=0;i<stringList.size();i++){
            List<String> tempList = new ArrayList<String>();
            tempList.add(stringList.get(i));
            for(int j=i+1;j+1<stringList.size() ;j++){
                tempList.add(stringList.get(j));
                tempList.add(stringList.get(j+1));
                totalList.add(tempList);
                tempList = new ArrayList<String>();
                tempList.add(stringList.get(i));
            }
        }
        return totalList;

    }

//    public List<ClusterObj> getAll2Clusters(List<ClusteringPoint> stringList){
//
//        List<ClusterObj> totalList = new ArrayList<ClusterObj>();
//        for(int i=0;i<stringList.size();i++){
//            ClusterObj clusterObj = new ClusterObj();
//            clusterObj.addPoint(stringList.get(i));
//            for(int j=i+1;j<stringList.size() ;j++){
//                clusterObj.addPoint(stringList.get(j));
//                totalList.add(clusterObj);
//                clusterObj = new ClusterObj();
//                clusterObj.addPoint(stringList.get(i));
//            }
//        }
//        return totalList;
//    }

    public List<List<String>> getAll2Clusters(List<String> strings){
        List<List<String>> totalList = new ArrayList<List<String>>();
        for(int i=0;i<strings.size();i++){
            List<String> tempList = new ArrayList<String>();
            tempList.add(strings.get(i));
            for(int j=i+1;j<strings.size() ;j++){
                tempList.add(strings.get(j));
                totalList.add(tempList);
                tempList = new ArrayList<String>();
                tempList.add(strings.get(i));
            }
        }
        return totalList;

    }

//    /**
//     * Check if the 3 point cluster is valid
//     * */
//    public ClusterObj checkValidCluster_old(ClusteringPoint geoHash,ClusterObj clusterObj){
//        List<ClusteringPoint> points = clusterObj.getPoints();
//        double shortDistance = Double.MAX_VALUE ;
//        if(points.size()==3){
//            shortDistance = getShortestDistance1(geoHash, points);
//        }else if(points.size()==2){
//            shortDistance =getShortestDistanceFor2(geoHash, points);
//        }
//        if(shortDistance>10) return null;
//        if(clusterObj.getSub_cat().length<144) return null;
//        if(clusterObj.getProducts().length<4000) return null;
//        double rank = getFavourFactor_cov(clusterObj.getSub_cat().length, clusterObj.getProducts().length);
//        clusterObj.setDistance(shortDistance);
//        clusterObj.setRank(rank);
//        return clusterObj;
//    }


    /**
     * Check if the 3 point cluster is valid
     * */
    public ClusterObjNew checkValidCluster(Geopoint geoHash,List<String> stringList){
        double shortDistance = Double.MAX_VALUE ;
        if(stringList.size()==3){
            shortDistance = getShortestDistance1(geoHash, stringList);
        }else if(stringList.size()==2){
            shortDistance =getShortestDistanceFor2(geoHash, stringList);
        }
        if(shortDistance>8) return null;

        //merge those ids and get Cat and SubCat count
        int productCount = mergerProducts(stringList);
        int subCatCount = mergerSubCat(stringList);

        if(subCatCount<144) return null;
        if(productCount<4000) return null;
        double rank = getFavourFactor_cov(subCatCount,productCount);

        //Make the clusterObject now
        ClusterObjNew clusterObjNew = new ClusterObjNew();
        for(String s: stringList){
            clusterObjNew.addPoint(s);
        }
        clusterObjNew.setDistance(shortDistance);
        clusterObjNew.setRank(rank);
        clusterObjNew.setProductsCount(productCount);
        clusterObjNew.setSubCatCount(subCatCount);
        return clusterObjNew;
    }

    public int mergerProducts(List<String> idList){
        if(idList.size()==2) {
            //check if it already present else merge and update
            String hash = getHashForCHM(idList);
            if (GeoCLusteringNew.product2MergerMap.containsKey(hash)) {
                return GeoCLusteringNew.product2MergerMap.get(hash).length;
            } else {
                String[] merges = mergeStrings(GeoCLusteringNew.clusterPoints.get(idList.get(0)).getProducts(), GeoCLusteringNew.clusterPoints.get(idList.get(1)).getProducts());
                GeoCLusteringNew.product2MergerMap.put(hash, merges);
                return merges.length;
            }
        }
        if(idList.size()==3){
            String hash = getHashForCHM(idList);
            if(GeoCLusteringNew.product3MergerMap.containsKey(hash)){
                return GeoCLusteringNew.product3MergerMap.get(hash).length;
            }else {
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                String hash2 = getHashForCHM(tempList);
                String[] merge2 ={};

                if(GeoCLusteringNew.product2MergerMap.containsKey(hash2)){
                    merge2 = GeoCLusteringNew.product2MergerMap.get(hash2);
                }else {
                    merge2 = mergeStrings(GeoCLusteringNew.clusterPoints.get(idList.get(0)).getProducts(), GeoCLusteringNew.clusterPoints.get(idList.get(1)).getProducts());
                    GeoCLusteringNew.product2MergerMap.put(hash2, merge2);
                }
                String[] mergeF = mergeStrings(merge2,GeoCLusteringNew.clusterPoints.get(idList.get(2)).getProducts());
                GeoCLusteringNew.product3MergerMap.put(hash,mergeF);
                return mergeF.length;
            }
        }
        return 0;
    }

    public int mergerSubCat(List<String> idList){
        if(idList.size()==2) {
            //check if it already present else merge and update
            String hash = getHashForCHM(idList);
            if (GeoCLusteringNew.subCat2MergerMap.containsKey(hash)) {
                return GeoCLusteringNew.subCat2MergerMap.get(hash).length;
            } else {
                String[] merges = mergeStrings(GeoCLusteringNew.clusterPoints.get(idList.get(0)).getSubCat(), GeoCLusteringNew.clusterPoints.get(idList.get(1)).getSubCat());
                GeoCLusteringNew.subCat2MergerMap.put(hash, merges);
                return merges.length;
            }
        }
        if(idList.size()==3){
            String hash = getHashForCHM(idList);
            if(GeoCLusteringNew.subCat3MergerMap.containsKey(hash)){
                return GeoCLusteringNew.subCat3MergerMap.get(hash).length;
            }else {
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                String hash2 = getHashForCHM(tempList);
                String[] merge2 ={};

                if(GeoCLusteringNew.subCat2MergerMap.containsKey(hash2)){
                    merge2 = GeoCLusteringNew.subCat2MergerMap.get(hash2);
                }else {
                    merge2 = mergeStrings(GeoCLusteringNew.clusterPoints.get(idList.get(0)).getSubCat(), GeoCLusteringNew.clusterPoints.get(idList.get(1)).getSubCat());
                    GeoCLusteringNew.subCat2MergerMap.put(hash2, merge2);
                }
                String[] mergeF = mergeStrings(merge2,GeoCLusteringNew.clusterPoints.get(idList.get(2)).getSubCat());
                GeoCLusteringNew.subCat3MergerMap.put(hash,mergeF);
                return mergeF.length;
            }
        }
        return 0;
    }

    public String[] mergeStrings(String[] s1, String[] s2){

        if(s2==null){
            return s1;
        }
        if(s1==null){
            return s2;
        }
        List<String> shops = new ArrayList<String>(100);
        for(int i=0;i<s1.length;i++){
            shops.add(s1[i]);
        }

        for(String s: s2){

            boolean repeated = false;
            for(String d: s1){
                if(s.contentEquals(d)) repeated = true;
            }
            if(!repeated) shops.add(s);
        }
        String[] rStrings = new String[shops.size()];
        for(int i=0;i<shops.size();i++){
            rStrings[i] = shops.get(i);
        }
        return rStrings;
    }

    /**
     * Creates all possible String ids;
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


    //Helper function to create the hash
    public String getHashForCHM(List<String> strings){
        Collections.sort(strings);
        StringBuilder sb = new StringBuilder();
        for(String s : strings){
            sb.append("" +
                    "" +
                    "" +
                    "-");
            sb.append(s);
        }
        return sb.toString().substring(1);
    }
}