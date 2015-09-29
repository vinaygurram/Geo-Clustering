package clusters.create;

import clusters.create.objects.*;
import com.github.davidmoten.geo.GeoHash;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by gurramvinay on 6/26/15.
 * defining strategy to create the geo hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * distance matrix is computed for each geo hash
 */

public class ClusterStrategy {
    private DistanceMatrix distanceMatrix;

    public void createDistanceMatrix(Geopoint geoHash, List<String> points){
        List<String> ttpoints = new ArrayList<String>(points);
        this.distanceMatrix = new DistanceMatrix(geoHash,ttpoints);

    }

    //TODO
    //Need to re write the code
    public List<ClusterObj> createClusters(Geopoint geoHash,  List<String>points){

        if(points==null || points.size()==0) return new ArrayList<ClusterObj>();
        int clustersForCombination = 0;

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
                clustersForCombination++;
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }
        }

        if(clustersForCombination==0) return validClusters;
        clustersForCombination = 0;

        //Create clusters with 2 shops
        List<List<String>>clusters = get2CClusters(points);
        for(List<String> clusterObj : clusters){
            temp = checkValidCluster(geoHash,clusterObj);
            if(temp!=null){
                clustersForCombination++;
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }

        }


        if(clustersForCombination==0) return validClusters;
        clustersForCombination = 0;

        //create clusters with 3 shops
        clusters =get3CClusters(points);
        for(List<String> clusterObj : clusters){
            temp = checkValidCluster(geoHash,clusterObj);
            if(temp!=null){
                clustersForCombination++;
                temp.setGeoHash(encodedGeoHash);
                validClusters.add(temp);
            }

        }


        if(clustersForCombination==0) return validClusters;
        clustersForCombination = 0;

        //create clusters with 4 shops
        if(points.size()>3){
            clusters = get4CClusters(points);
            for(List<String> clusterObj : clusters){
                temp = checkValidCluster(geoHash,clusterObj);
                if(temp!=null){
                    clustersForCombination++;
                    temp.setGeoHash(encodedGeoHash);
                    validClusters.add(temp);
                }
            }


            if(clustersForCombination==0) return validClusters;
            clustersForCombination = 0;

            if(points.size()>4){

                //create clusters with 5 shops
                clusters = get5CClusters(points);
                for(List<String> clusterObj : clusters){
                    temp = checkValidCluster(geoHash,clusterObj);
                    if(temp!=null){
                        temp.setGeoHash(encodedGeoHash);
                        clustersForCombination++;
                        validClusters.add(temp);
                    }
                }


                if(clustersForCombination==0) return validClusters;

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
        this.distanceMatrix = null;
        return validClusters;
    }

    //Helper methods
    /**
     * computes rank for a given cluster
     * Rank r = 0.3 * (relFNVPinCluster/totalRelFNVinCluster) + 0.3 * (relNFNVPinCluster/totalRelNFNVinCluster)+ (0.35) * (ProductsInCluster/TotalProducts)
     * @return give rank
     * */
    public void setRankParameters(Set<String> relFNVSet, Set<String> relNFNVSet,ClusterObj clusterObj){

        //create cluster ID and Stores array
        String storeIdString = "";
        String clusterId = "";
        List<String> stores = clusterObj.getPoints();
        Collections.sort(stores);
        for(String s: stores){
            storeIdString += "\""+s+"\",";
            clusterId +="-"+s;
        }
        clusterId = clusterId.substring(1);
        storeIdString = storeIdString.substring(0,storeIdString.length()-1);

        //check if data is already computed
        if(GeoClustering.clusterRankMap.containsKey(clusterId)){
            clusterObj.setProductsCount(GeoClustering.clusterProductCoverage.get(clusterId));
            clusterObj.setSubCatCount(GeoClustering.clusterSubCatCoverage.get(clusterId));
            clusterObj.setRank(GeoClustering.clusterRankMap.get(clusterId));
            return;
        }

        //Get products set and sub cat count and product count
        Set<String> productsSet = new HashSet<>();
        int subCatCount = 0;
        try {

            String query = "{\"size\": 0,\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":[" +
                    "{\"terms\":{\"store_details.id\":["+storeIdString+"]}}," +
                    "{\"term\":{\"product_details.available\":true}}," +
                    "{\"term\":{\"product_details.status\":\"current\"}}]}}}}," +
                    "\"aggregations\":{\"unique_products\":{\"terms\":{\"field\":\"product_details.id\",\"size\":0}}," +
                    "\"sub_cat_count\":{\"cardinality\":{\"field\":\"product_details.sub_category_id\"}}}}";
            HttpClient httpClient = HttpClientBuilder.create().build();
            String listing_serach_api = (String)GeoClustering.yamlMap.get("es_search_api");
            listing_serach_api = listing_serach_api.replace(":index_name","listing_index_name");
            listing_serach_api = listing_serach_api.replace(":index_type","listing_index_type");
            HttpPost httpPost = new HttpPost(listing_serach_api);
            httpPost.setEntity(new StringEntity(query));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int status_code = httpResponse.getStatusLine().getStatusCode();
            if(status_code==200){
                JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                JSONObject esResult = result.getJSONObject("aggregations");
                JSONArray uniqueProdBuckets = esResult.getJSONObject("unique_products").getJSONArray("buckets");
                for(int i=0;i<uniqueProdBuckets.length();i++){
                    String productId = uniqueProdBuckets.getJSONObject(i).getString("key");
                    productsSet.add(productId);
                }
                subCatCount = esResult.getJSONObject("sub_cat_count").getInt("value");
            }
        }catch (Exception e){
            GeoClustering.logger.error(" Computing rank failed "+e.getMessage());
        }

        //store & set both product count and sub cat count
        GeoClustering.clusterProductCoverage.put(clusterId,productsSet.size());
        GeoClustering.clusterSubCatCoverage.put(clusterId,subCatCount);
        clusterObj.setProductsCount(productsSet.size());
        clusterObj.setSubCatCount(subCatCount);

        //Compute rel FNV & Non FNV products in cluster
        Set<String> intesection = new HashSet<String>(productsSet);
        intesection.retainAll(relFNVSet);
        int rFNVPCount = intesection.size();
        intesection = new HashSet<String>(productsSet);
        intesection.retainAll(relNFNVSet);
        int rNFNVPCount = intesection.size();

        //Compute rank
        double fncPCov = ((double) rFNVPCount)/((double) relFNVSet.size());
        double nfncPCov = ((double) rNFNVPCount)/((double) relNFNVSet.size());
        double pCov = ((double)productsSet.size())/ ((double)GeoClustering.maxProductCount);
        double rank = (GeoClustering.relFNCCoverageCoeff * fncPCov)+ (GeoClustering.relNFNCCoverageCoeff * nfncPCov)+ (GeoClustering.relProductCoverageCoeff* pCov);

        //store and set
        GeoClustering.clusterRankMap.put(clusterId,rank);
        clusterObj.setRank(rank);
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

    //For more than 3 shops
    public double getShortestDistanceForMultiPoints(Geopoint geohashPoint, List<String> points){
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
            GeoClustering.logger.error("shortest distance computation failed "+ e.getMessage());
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
        }else if(storeIdList.size()==2){
            shortDistance =getShortestDistanceFor2(geoHash, storeIdList);
        }else if(storeIdList.size()>=3){
            shortDistance = getShortestDistanceForMultiPoints(geoHash, storeIdList);
        }
        if(shortDistance>8) return null;

        boolean fnvCriteria = checkFnV(storeIdList);

        //Make the clusterObject
        ClusterObj clusterObjNew = new ClusterObj();
        for(String s: storeIdList){
            clusterObjNew.addPoint(s);
        }
        clusterObjNew.setDistance(shortDistance);
        setRankParameters(GeoClustering.fnvProdSet,GeoClustering.nfnvProdSet,clusterObjNew);

        //set cluster status offline/online
        clusterObjNew.setStatus(true);
        return clusterObjNew;
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