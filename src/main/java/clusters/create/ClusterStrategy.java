package clusters.create;

import clusters.create.objects.*;
import com.github.davidmoten.geo.GeoHash;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gurramvinay on 6/26/15.
 * defining strategy to create the geo hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * low level distance matraix is computer for each geo hash
 */

public class ClusterStrategy {
    private DistanceMatrix distanceMatrix;
    public String ES_LISTING_SEARCH_END_POINT = "http://localhost:9200/listing/list/_search";

    public void createDistanceMatrix(Geopoint geoHash, List<String> points){
        List<String> ttpoints = new ArrayList<String>(points);
        this.distanceMatrix = new DistanceMatrix(geoHash,ttpoints);

    }

    //TODO
    //Need to re write the code
    public List<ClusterObj> createClusters(Geopoint geoHash,  List<String>points){

        if(points==null || points.size()==0) return new ArrayList<ClusterObj>();

        createDistanceMatrix(geoHash, points);
        List<ClusterObj> validClusters = new ArrayList<ClusterObj>();
        String encodedGeoHash = GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude(),7);
        ClusterObj temp;
        //Create clusters with 1 shops
        for(String s: points){

            List<String> thisList = new ArrayList<String>();
            thisList.add(s);
            temp = checkValidCluster(geoHash,thisList);
            if(temp!=null){
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }
        }

        //Create clusters with 2 shops
        List<List<String>>clusters = get2CClusters(points);
        for(List<String> clusterObj : clusters){
            temp = checkValidCluster(geoHash,clusterObj);
            if(temp!=null){
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }

        }
        //create clusters with 3 shops
        clusters =get3CClusters(points);
        for(List<String> clusterObj : clusters){
            temp = checkValidCluster(geoHash,clusterObj);
            if(temp!=null){
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }

        }
        //create clusters with 4 shops
        if(points.size()>3){
            clusters = get4CClusters(points);
            for(List<String> clusterObj : clusters){
                temp = checkValidCluster(geoHash,clusterObj);
                if(temp!=null){
                    temp.setGeoHash(encodedGeoHash);
                    validClusters.add(temp);
                }
            }
            if(points.size()>4){

                //create clusters with 5 shops
                clusters = get5CClusters(points);
                for(List<String> clusterObj : clusters){
                    temp = checkValidCluster(geoHash,clusterObj);
                    if(temp!=null){
                        temp.setGeoHash(encodedGeoHash);
                        validClusters.add(temp);
                    }
                }

                if(points.size()>5){
                    //create clusters with 6 shops
                    clusters = get6CClusters(points);
                    for(List<String> clusterObj : clusters){
                        temp = checkValidCluster(geoHash,clusterObj);
                        if(temp!=null){
                            temp.setGeoHash(encodedGeoHash);
                            validClusters.add(temp);
                        }
                    }
                }
            }
        }
        return validClusters;
    }

    //Helper methods

    /**
     * Get Favoured Cluster in List of clusters
     * Fav Factor = (1/2)*(S/Smax) + (1/2) * (P/Pmax)
     * @return give rank
     * */
    public double getFavourFactor_cov(int subCt, int products){
        double p = ((double)products)/17000;
        double s = ((double)subCt)/240;
        p =p/2;
        s =s/2;
        return  s+p;
    }

    /**
     * Shortest Distance if store has only 2 points
     * */
    public double getShortestDistanceFor2(Geopoint geohashPoint, List<String> points){

        double dbp = distanceMatrix.getDistance(points.get(0),points.get(1));
        String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude(),7);
        double dfg = distanceMatrix.getDistance(geoString,points.get(0));
        double dsg = distanceMatrix.getDistance(geoString,points.get(1));
        dfg = dfg + dbp;
        dsg = dsg+dbp;
        return dfg>dsg?dsg:dfg;
    }

    //For SLogic
    public double getShortestDistance1(Geopoint geohashPoint, List<String> points){
        double smallestDistace = Double.MAX_VALUE;
        String geoString  = GeoHash.encodeHash(geohashPoint.getLatitude(),geohashPoint.getLongitude(),7);
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
            for(String s: list){
               if(s.contentEquals(cp2)) continue;
                ss.add(s);
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

    public List<List<String>> get3CClusters(List<String> stringList){

        if(stringList.size()<3) return new ArrayList<List<String>>();

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


    /**
     *Get all 4 possible combinations
     *
     * */
    public List<List<String>> get4CClusters(List<String> idList){

        if(idList.size()<4) return new ArrayList<List<String>>();

        List<List<String>> totalList = new ArrayList<List<String>>();
        for(int i=0;i<idList.size();i++){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(i));
            for(int j=i+1;j+2<idList.size();j++){
                tempList.add(idList.get(j));
                tempList.add(idList.get(j+1));
                tempList.add(idList.get(j+2));
                totalList.add(tempList);
                tempList = new ArrayList<String>();
                tempList.add(idList.get(i));
            }
        }
        return totalList;
    }

    /**
     * Get all 5 possible combinations
     * */
    public List<List<String>> get5CClusters(List<String> idList){

        if(idList.size()<5) return new ArrayList<List<String>>();

        List<List<String>> totalList = new ArrayList<List<String>>();
        for(int i=0;i<idList.size();i++){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(i));
            for(int j=i+1;j+3<idList.size();j++){
                tempList.add(idList.get(j));
                tempList.add(idList.get(j+1));
                tempList.add(idList.get(j+2));
                tempList.add(idList.get(j+3));
                totalList.add(tempList);
                tempList = new ArrayList<String>();
                tempList.add(idList.get(i));
            }
        }
        return totalList;
    }


    /**
     * Get all possible 6 combinations
     * */

    public List<List<String>> get6CClusters(List<String> idList){

        if(idList.size()<6) return new ArrayList<List<String>>();

        List<List<String>> totalList = new ArrayList<List<String>>();
        for(int i=0;i<idList.size();i++){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(i));
            for(int j=i+1;j+4<idList.size();j++){
                tempList.add(idList.get(j));
                tempList.add(idList.get(j+1));
                tempList.add(idList.get(j+2));
                tempList.add(idList.get(j+3));
                tempList.add(idList.get(j+4));
                totalList.add(tempList);
                tempList = new ArrayList<String>();
                tempList.add(idList.get(i));
            }
        }
        return totalList;
    }


    /**
     * Get all 2 possible combinations
     * */
    public List<List<String>> get2CClusters(List<String> strings){

        if(strings.size()<2) return new ArrayList<List<String>>();
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

    /**
     * Check if the point cluster is valid
     * */
    public ClusterObj checkValidCluster(Geopoint geoHash,List<String> storeIdList){
        double shortDistance = Double.MAX_VALUE ;

        if(storeIdList.size()==1){
            shortDistance = Geopoint.getDistance(geoHash, GeoClustering.clusterPoints.get(storeIdList.get(0)).getLocation());
        }
        if(storeIdList.size()>=3){
            shortDistance = getShortestDistance1(geoHash, storeIdList);
        }else if(storeIdList.size()==2){
            shortDistance =getShortestDistanceFor2(geoHash, storeIdList);
        }
        if(shortDistance>7) return null;

        //merge those ids and get Cat and SubCat count


        boolean fnvCriteria = checkFnV(storeIdList);
        //if(!fnvCriteria) return null;

        //if(subCatCount<144) return null;
        //if(productCount<4000) return null;
        //double rank = getFavourFactor_cov(subCatCount,productCount);

        //Make the clusterObject now
        ClusterObj clusterObjNew = new ClusterObj();
        setProductAndSubCatCoverage(storeIdList,clusterObjNew);
        for(String s: storeIdList){
            clusterObjNew.addPoint(s);
        }
        clusterObjNew.setDistance(shortDistance);
        clusterObjNew.setRank(1);

        //set cluster status offline/online
        clusterObjNew.setStatus(true);
        return clusterObjNew;
    }


    private void setProductAndSubCatCoverage(List<String> storeIds,ClusterObj clusterObjNew){

        String hash = getHashForCHM(storeIds);
        if(GeoClustering.clusterProductCoverage.containsKey(hash)){
            clusterObjNew.setProductsCount(GeoClustering.clusterProductCoverage.get(hash));
            clusterObjNew.setSubCatCount(GeoClustering.clusterSubCatCoverage.get(hash));
        }
        try {
            String storeIdString = "";
            for(String s: storeIds){
                storeIdString += "\""+s+"\",";
            }
            storeIdString = storeIdString.substring(0,storeIdString.length()-1);
            String query = "{\"size\":0,\"query\":{\"terms\":{\"store_details.id\":["+storeIdString+"]}}," +
                    "\"aggregations\":{\"product_count\":{\"cardinality\":{\"field\":\"product_details.id\"}}," +
                    "\"sub_cat_count\":{\"cardinality\":{\"field\":\"product_details.sub_category_id\"}}}}";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(ES_LISTING_SEARCH_END_POINT);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            System.out.println("status is "+ httpResponse.getStatusLine().toString());
            int status_code = httpResponse.getStatusLine().getStatusCode();
            if(status_code==200){
                JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                JSONObject aggr  = result.getJSONObject("aggregations");
                int pCount = aggr.getJSONObject("product_count").getInt("value");
                int subCatCount = aggr.getJSONObject("sub_cat_count").getInt("value");
                GeoClustering.clusterProductCoverage.put(hash,pCount);
                GeoClustering.clusterSubCatCoverage.put(hash,subCatCount);
                clusterObjNew.setProductsCount(pCount);
                clusterObjNew.setSubCatCount(subCatCount);
            }else {
                System.out.println("something wrong in calculating product and sub cat count");
                clusterObjNew.setProductsCount(0);
                clusterObjNew.setSubCatCount(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Check for FnV
     * */
     public boolean checkFnV(List<String> idsist){
         boolean rValue = false;
         for(String s: idsist){
             if(GeoClustering.clusterPoints.get(s).isFnv()){
                 rValue = true;
             }
         }
         return rValue;
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
            sb.append("-");
            sb.append(s);
        }
        return sb.toString().substring(1);
    }

}