package live.cluster.one;

import com.github.davidmoten.geo.GeoHash;
import gridbase.*;
import live.cluster.one.LObject.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static live.cluster.one.GeoCLusteringNew.*;

/**
 * Created by gurramvinay on 6/26/15.
 * defining strategy to create the goe hash based cluster.
 * Covers point selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster Selection Criteria
 * low level distance matraix is computer for each geo hash
 */

public class ClusterStrategyNew {
    private DistanceMatrix distanceMatrix;
    private int productCount;
    private int subCatCount;


    public void createDistanceMatrix(Geopoint geoHash, List<String> points){
        List<String> ttpoints = new ArrayList<String>(points);
        this.distanceMatrix = new DistanceMatrix(geoHash,ttpoints);

    }

    //TODO
    //Need to re write the code
    public List<ClusterObjNew> createClusters1(Geopoint geoHash,  List<String>points){

        if(points==null || points.size()==0) return new ArrayList<ClusterObjNew>();

        createDistanceMatrix(geoHash, points);
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
            List<ClusterObjNew> validClusters = new ArrayList<ClusterObjNew>();

            //Create clusters with 1 shops
            for(String s: points){

                List<String> thisList = new ArrayList<String>();
                thisList.add(s);
                ClusterObjNew temp = checkValidCluster(geoHash,thisList);
                if(temp!=null){
                    temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                    validClusters.add(temp);
                }
            }

            //create clusters with 3 shops
            List<List<String>> clusters =get3CClusters(points);
            for(List<String> clusterObj : clusters){
                ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                if(temp!=null){
                    temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                    validClusters.add(temp);
                }

            }
            //create clusters with 4 shops
            if(points.size()>3){
                clusters = get4CClusters(points);
                for(List<String> clusterObj : clusters){
                    ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                    if(temp!=null){
                        temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                        validClusters.add(temp);
                    }
                }
                if(points.size()>4){

                    //create clusters with 5 shops
                    clusters = get5CClusters(points);
                    for(List<String> clusterObj : clusters){
                        ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                        if(temp!=null){
                            temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                            validClusters.add(temp);
                        }
                    }

                    if(points.size()>5){
                        //create clusters with 6 shops
                        clusters = get6CClusters(points);
                        for(List<String> clusterObj : clusters){
                            ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                            if(temp!=null){
                                temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                                validClusters.add(temp);
                            }
                        }
                    }
                }
            }
            clusters = get2CClusters(points);
            for(List<String> clusterObj : clusters){
                ClusterObjNew temp = checkValidCluster(geoHash,clusterObj);
                if(temp!=null){
                    temp.setGeoHash(GeoHash.encodeHash(geoHash.getLatitude(),geoHash.getLongitude()));
                    validClusters.add(temp);
                }

            }
            rclusters = validClusters;
            if(rclusters.size()!=0){
                this.productCount= getProductCoverage(points);
                this.subCatCount= getSubCatCoverage(points);
            }
        }
        return rclusters;
    }


    public int getProductCoverage(List<String> idList){

        String hash = getHashForCHM(idList);
        if(geoProductCoverage.containsKey(hash)){
            return geoProductCoverage.get(hash);
        }else {
            //compute and return
            String[] merge;
            merge  = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
            for(int i=2;i<idList.size();i++){
                merge  = mergeStrings(merge, clusterPoints.get(idList.get(i)).getProducts());
            }
            geoProductCoverage.put(hash,merge.length);
            return merge.length;
        }
    }

    public int getSubCatCoverage(List<String> idList){
        String hash = getHashForCHM(idList);
        if(geoSubCatCoverage.containsKey(hash)){
           return geoSubCatCoverage.get(hash);
        }else{
            String[] merge;
            merge = mergeStrings(clusterPoints.get(idList.get(0)).getSubCat(), clusterPoints.get(idList.get(1)).getSubCat());
            for(int i=2;i<idList.size();i++){
                merge = mergeStrings(merge, clusterPoints.get(idList.get(i)).getSubCat());
            }
            geoSubCatCoverage.put(hash,merge.length);
            return merge.length;
        }
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

        if(idList.size()<4) return null;

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

        if(idList.size()<5) return null;

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

        if(idList.size()<6) return null;

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
    public ClusterObjNew checkValidCluster(Geopoint geoHash,List<String> stringList){
        double shortDistance = Double.MAX_VALUE ;

        if(stringList.size()==1){
            shortDistance = Geopoint.getDistance(geoHash,clusterPoints.get(stringList.get(0)).getLocation());
        }
        if(stringList.size()>=3){
            shortDistance = getShortestDistance1(geoHash, stringList);
        }else if(stringList.size()==2){
            shortDistance =getShortestDistanceFor2(geoHash, stringList);
        }
        if(shortDistance>6) return null;

        //merge those ids and get Cat and SubCat count
        int productCount = mergerProducts(stringList);
        int subCatCount = mergerSubCat(stringList);

        boolean fnvCriteria = checkFnV(stringList);
        if(!fnvCriteria) return null;

        //if(subCatCount<144) return null;
        //if(productCount<4000) return null;
        //double rank = getFavourFactor_cov(subCatCount,productCount);

        //Make the clusterObject now
        ClusterObjNew clusterObjNew = new ClusterObjNew();
        for(String s: stringList){
            clusterObjNew.addPoint(s);
        }

        CatalogTree catalogTree = createOrMergeCatalogTree(null,stringList);
        clusterObjNew.setCatalogTree(catalogTree);
        clusterObjNew.setDistance(shortDistance);
        //clusterObjNew.setRank(rank);
        clusterObjNew.setRank(1);
        clusterObjNew.setProductsCount(productCount);
        clusterObjNew.setSubCatCount(subCatCount);


        //set cluster status offline/online
        boolean status = getClusterStatus(clusterObjNew);
        clusterObjNew.setStatus(status);
        return clusterObjNew;
    }


    /**
     * Check for FnV
     * */
     public boolean checkFnV(List<String> idsist){
         boolean rValue = false;
         for(String s: idsist){
             if(clusterPoints.get(s).isFnv()){
                 rValue = true;
             }
         }
         return rValue;
     }

    /**
     * return cluster status
     * true if product coverage is >80% and Subcat Coverage is >80%
     * */
    public boolean getClusterStatus(ClusterObjNew clusterObjNew){
        int pCount = clusterObjNew.getProductsCount();
        int sbCount = clusterObjNew.getSubCatCount();
        int lpCount = ((Double)((0.8)* (this.productCount))).intValue();
        int lsCount = ((Double)((0.8)* (this.subCatCount))).intValue();
        return !(pCount < lpCount || sbCount < lsCount);
    }

    //Create or merge catalogTree
    /**
     * Need to optimize
     * */
    public CatalogTree createOrMergeCatalogTree(CatalogTree catalogTree,List<String> idList){
        //check if it already stored
        String hash = getHashForCHM(idList);
        if(catalogTreeMap.containsKey(hash)){
            return  catalogTreeMap.get(hash);
        }

        //get productList
        List<String> productList = new ArrayList<String>();
        String[]tempList = new String[0];
        if(idList.size()==1){
            tempList = clusterPoints.get(idList.get(0)).getProducts();
        }
        if(idList.size()==2){
            tempList = product2MergerMap.get(hash);
        }
        if(idList.size()==3) {
            tempList = product3MergerMap.get(hash);
        }
        if(idList.size()>3){
            tempList = productMultiMergerMap.get(hash);
        }
        Collections.addAll(productList, tempList);

        //create catalog tree
        if(catalogTree==null) catalogTree = new CatalogTree();

        //Making tree 4k loop
        for (String ss: productList){
            List<String> catList =  map.get(ss);
            if(catList==null){
                continue;
            }

            //Create Super Category
            List<SuperCategory>superCategoryList = catalogTree.getSuperCategories();
            boolean is_super_cat_exists = false;
            for(SuperCategory superCategory : superCategoryList){

                //Check if super category exists
                if(superCategory.getSup_cat_id().contentEquals(catList.get(0))){
                    is_super_cat_exists = true;

                    //Handle categories
                    boolean is_cat_exists = false;
                    List<Category> categoriesList=superCategory.getCatList();
                    for(Category category : categoriesList){
                       if(category.getCat_id().contentEquals(catList.get(2))){
                           is_cat_exists = true;

                           //check for case where sub cat does not exist
                           if(catList.size()>2 &&!catList.get(2).isEmpty()) {
                               //Handle sub categories
                               boolean is_sub_cat_exists = false;
                               List<SubCategory> subCategoryList = category.getSubCatList();
                               for(SubCategory subCategory : subCategoryList){
                                   if(subCategory.getSub_cat_id().contentEquals(catList.get(2))){
                                       is_sub_cat_exists = true;
                                       subCategory.addProduct(ss);
                                   }
                               }
                               //Add Sub category
                               if(!is_sub_cat_exists){
                                   SubCategory subCategory = new SubCategory();
                                   subCategory.setSub_cat_id(catList.get(2));
                                   subCategory.addProduct(ss);
                                   category.addSubCategory(subCategory);
                               }

                           }else {
                               category.addProduct(ss);
                           }
                       }
                    }
                    //add category if it does not exist
                    if(!is_cat_exists){
                        Category category = new Category();
                        category.setCat_id(catList.get(1));
                        //some products might not have sub categories
                        if(catList.size()>2 &&!catList.get(2).isEmpty()){
                            SubCategory subCategory = new SubCategory();
                            subCategory.setSub_cat_id(catList.get(2));
                            subCategory.addProduct(ss);
                            category.addSubCategory(subCategory);
                        }else {
                            category.addProduct(ss);
                        }
                        superCategory.addCategory(category);
                    }

                }
            }
            if(!is_super_cat_exists){
                SuperCategory superCategory = new SuperCategory();
                superCategory.setSup_cat_id(catList.get(0));
                Category category = new Category();
                category.setCat_id(catList.get(1));
                //some products might not have sub categories
                if(catList.size()>2 &&!catList.get(2).isEmpty()){
                    SubCategory subCategory = new SubCategory();
                    subCategory.setSub_cat_id(catList.get(2));
                    subCategory.addProduct(ss);
                    category.addSubCategory(subCategory);
                }else {
                    category.addProduct(ss);
                }
                superCategory.addCategory(category);
                catalogTree.getSuperCategories().add(superCategory);
            }
        }
        catalogTreeMap.put(hash,catalogTree);
        return catalogTree;
    }


    /**
     * Merge products with given ids
     * lot of code duplicity
     * divide them into multiple function of case of 2 and case of 3
     * */
    public int mergerProducts(List<String> idList){
        if(idList.size()==1){
            return clusterPoints.get(idList.get(0)).getProducts().length;
        }
        if(idList.size()==2) {
            //check if it already present else merge and update
            String hash = getHashForCHM(idList);
            if (product2MergerMap.containsKey(hash)) {
                return product2MergerMap.get(hash).length;
            } else {
                String[] merges = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
                product2MergerMap.put(hash, merges);
                return merges.length;
            }
        }
        if(idList.size()==3){
            String hash = getHashForCHM(idList);
            if(product3MergerMap.containsKey(hash)){
                return product3MergerMap.get(hash).length;
            }else {
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                String hash2 = getHashForCHM(tempList);
                String[] merge2;

                if(product2MergerMap.containsKey(hash2)){
                    merge2 = product2MergerMap.get(hash2);
                }else {
                    merge2 = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
                    product2MergerMap.put(hash2, merge2);
                }
                String[] mergeF = mergeStrings(merge2, clusterPoints.get(idList.get(2)).getProducts());
                product3MergerMap.put(hash,mergeF);
                return mergeF.length;
            }
        }
        if(idList.size()==4){
            String hash = getHashForCHM(idList);
            if(productMultiMergerMap.containsKey(hash)){
                return productMultiMergerMap.get(hash).length;
            }else{
                //atleast 4 stores are present
                //check for 2,2 pairs
                String[] merge1 ;
                String[] merge2 ;
                String[] merge3 ;
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                String tempHash = getHashForCHM(tempList);
                if(product2MergerMap.containsKey(tempHash)){
                   merge1 =  product2MergerMap.get(tempHash);
                }else {
                    merge1 = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
                    product2MergerMap.put(tempHash,merge1);
                }
                tempList = new ArrayList<String>();
                tempList.add(idList.get(2));
                tempList.add(idList.get(3));
                tempHash = getHashForCHM(tempList);
                if(product2MergerMap.containsKey(tempHash)){
                    merge2 =  product2MergerMap.get(tempHash);
                }else {
                    merge2 = mergeStrings(clusterPoints.get(idList.get(2)).getProducts(), clusterPoints.get(idList.get(3)).getProducts());
                    product2MergerMap.put(tempHash,merge2);
                }
                merge3  = mergeStrings(merge1,merge2);
                productMultiMergerMap.put(hash,merge3);
                return merge3.length;
            }
        }
        if(idList.size()==5){
            String hash = getHashForCHM(idList);
            if(productMultiMergerMap.containsKey(hash)){
                return productMultiMergerMap.get(hash).length;
            }else{
                //check for 2,3 pairs
                String[] merge1 ;
                String[] merge2 ;
                //Get merged products for 0,1
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                String tempHash = getHashForCHM(tempList);
                if(product2MergerMap.containsKey(tempHash)){
                    merge1 = product2MergerMap.get(tempHash);
                }else {
                    merge1 = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
                    product2MergerMap.put(tempHash,merge1);
                }

                //get merged products for 2,3,4
                tempList = new ArrayList<String>();
                tempList.add(idList.get(2));
                tempList.add(idList.get(3));
                tempList.add(idList.get(4));
                tempHash = getHashForCHM(tempList);
                if(product3MergerMap.containsKey(tempHash)){
                    merge2 = product3MergerMap.get(tempHash);
                }else {
                    //check for if 2 is already done
                    tempList= new ArrayList<String>();
                    tempList.add(idList.get(2));
                    tempList.add(idList.get(3));
                    String dTempHash = getHashForCHM(tempList);
                    if(product2MergerMap.containsKey(dTempHash)){
                        merge2 = product2MergerMap.get(dTempHash);
                    }else {
                        merge2 = mergeStrings(clusterPoints.get(idList.get(2)).getProducts(), clusterPoints.get(idList.get(3)).getProducts());
                        product2MergerMap.put(dTempHash,merge2);
                    }
                    merge2 = mergeStrings(merge2, clusterPoints.get(idList.get(4)).getProducts());
                    product3MergerMap.put(tempHash,merge2);
                }

                //merge all 5 things
                merge1 = mergeStrings(merge1,merge2);
                productMultiMergerMap.put(hash,merge1);
                return  merge1.length;
            }
        }

        if(idList.size()==6){
            String hash = getHashForCHM(idList);
            if(productMultiMergerMap.containsKey(hash)){
                return productMultiMergerMap.get(hash).length;
            }else{
                //check for 3,3 pairs
                String[] merge1 ;
                String[] merge2 ;

                //0,1,2 pair
                List<String> tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                tempList.add(idList.get(2));
                String tempHash = getHashForCHM(tempList);
                if(product3MergerMap.containsKey(tempHash)){
                    merge1 = product3MergerMap.get(tempHash);
                }else {
                    tempList = new ArrayList<String>();
                    tempList.add(idList.get(0));
                    tempList.add(idList.get(1));
                    String dTempHash = getHashForCHM(tempList);
                    if(product2MergerMap.containsKey(dTempHash)){
                        merge1 = product2MergerMap.get(dTempHash);
                    }else {
                        merge1 = mergeStrings(clusterPoints.get(idList.get(0)).getProducts(), clusterPoints.get(idList.get(1)).getProducts());
                        product2MergerMap.put(dTempHash,merge1);
                    }
                    merge1 = mergeStrings(merge1, clusterPoints.get(idList.get(2)).getProducts());
                    product3MergerMap.put(tempHash,merge1);
                }

                //3,4,5 pair
                tempList = new ArrayList<String>();
                tempList.add(idList.get(3));
                tempList.add(idList.get(4));
                tempList.add(idList.get(5));
                tempHash = getHashForCHM(tempList);
                if(product3MergerMap.containsKey(tempHash)){
                    merge2 = product3MergerMap.get(tempHash);
                }else {
                    tempList = new ArrayList<String>();
                    tempList.add(idList.get(3));
                    tempList.add(idList.get(4));
                    String dTempHash = getHashForCHM(tempList);
                    if(product2MergerMap.containsKey(dTempHash)){
                        merge2 = product2MergerMap.get(dTempHash);
                    }else {
                        merge2 = mergeStrings(clusterPoints.get(idList.get(3)).getProducts(), clusterPoints.get(idList.get(4)).getProducts());
                        product2MergerMap.put(dTempHash,merge2);
                    }
                    merge2 = mergeStrings(merge2, clusterPoints.get(idList.get(2)).getProducts());
                    product3MergerMap.put(tempHash,merge2);
                }
                //merge all 6
                merge1 = mergeStrings(merge1,merge2);
                productMultiMergerMap.put(hash,merge1);
                return  merge1.length;
            }
        }
        return 0;
    }

    public int mergerSubCat(List<String> idList){
        if(idList.size()==1){
            return clusterPoints.get(idList.get(0)).getSubCat().length;
        }
        if(idList.size()==2) {
           return merge2SubCat(idList).length;
        }
        if(idList.size()==3){
            return merge3SubCat(idList).length;
        }
        if(idList.size()==4){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(0));
            tempList.add(idList.get(1));
            tempList.add(idList.get(2));
            tempList.add(idList.get(3));
            String hash = getHashForCHM(tempList);
            if(subCatMultiMergerMap.containsKey(hash)){
                return subCatMultiMergerMap.get(hash).length;
            }else {
                String[] merge1;
                String[] merge2;

                tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                merge1 = merge2SubCat(tempList);

                tempList = new ArrayList<String>();
                tempList.add(idList.get(2));
                tempList.add(idList.get(3));
                merge2 = merge2SubCat(tempList);

                merge1 = mergeStrings(merge1,merge2);
                subCatMultiMergerMap.put(hash, merge1);
                return merge1.length;
            }
        }
        if(idList.size()==5){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(0));
            tempList.add(idList.get(1));
            tempList.add(idList.get(2));
            tempList.add(idList.get(3));
            tempList.add(idList.get(4));
            String hash = getHashForCHM(tempList);
            if(subCatMultiMergerMap.containsKey(hash)){
                return subCatMultiMergerMap.get(hash).length;
            }else {
                String[] merge1;
                String[] merge2;

                tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                tempList.add(idList.get(2));
                merge1 = merge3SubCat(tempList);

                tempList = new ArrayList<String>();
                tempList.add(idList.get(3));
                tempList.add(idList.get(4));
                merge2 = merge2SubCat(tempList);

                merge1 = mergeStrings(merge1,merge2);
                subCatMultiMergerMap.put(hash,merge1);
                return merge1.length;
            }
        }

        if(idList.size()==6){
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(0));
            tempList.add(idList.get(1));
            tempList.add(idList.get(2));
            tempList.add(idList.get(3));
            tempList.add(idList.get(4));
            tempList.add(idList.get(5));
            String hash = getHashForCHM(tempList);
            if(subCatMultiMergerMap.containsKey(hash)){
                return subCatMultiMergerMap.get(hash).length;
            }else {
                String[] merge1;
                String[] merge2;

                tempList = new ArrayList<String>();
                tempList.add(idList.get(0));
                tempList.add(idList.get(1));
                tempList.add(idList.get(2));
                merge1 = merge3SubCat(tempList);

                tempList = new ArrayList<String>();
                tempList.add(idList.get(3));
                tempList.add(idList.get(4));
                tempList.add(idList.get(5));
                merge2 = merge3SubCat(tempList);

                merge1 = mergeStrings(merge1,merge2);
                subCatMultiMergerMap.put(hash,merge1);
                return merge1.length;
            }
        }

        return 0;
    }

    public String[] merge2SubCat(List<String> idList){
        String hash = getHashForCHM(idList);
        if (subCat2MergerMap.containsKey(hash)) {
            return subCat2MergerMap.get(hash);
        } else {
            String[] merges = mergeStrings(clusterPoints.get(idList.get(0)).getSubCat(), clusterPoints.get(idList.get(1)).getSubCat());
            subCat2MergerMap.put(hash, merges);
            return merges;
        }
    }

    public String[] merge3SubCat(List<String> idList){
        String hash = getHashForCHM(idList);
        if(subCat3MergerMap.containsKey(hash)){
            return subCat3MergerMap.get(hash);
        }else {
            List<String> tempList = new ArrayList<String>();
            tempList.add(idList.get(0));
            tempList.add(idList.get(1));
            String hash2 = getHashForCHM(tempList);
            String[] merge2 ;

            if(subCat2MergerMap.containsKey(hash2)){
                merge2 = subCat2MergerMap.get(hash2);
            }else {
                merge2 = mergeStrings(clusterPoints.get(idList.get(0)).getSubCat(), clusterPoints.get(idList.get(1)).getSubCat());
                subCat2MergerMap.put(hash2, merge2);
            }
            String[] mergeF = mergeStrings(merge2, clusterPoints.get(idList.get(2)).getSubCat());
            subCat3MergerMap.put(hash,mergeF);
            return mergeF;
        }
    }

    public String[] mergeStrings(String[] s1, String[] s2){

        if(s2==null){
            return s1;
        }
        if(s1==null){
            return s2;
        }
        List<String> shops = new ArrayList<String>(100);
        Collections.addAll(shops,s1);
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
            sb.append("-");
            sb.append(s);
        }
        return sb.toString().substring(1);
    }


    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getSubCatCount() {
        return subCatCount;
    }

    public void setSubCatCount(int subCatCount) {
        this.subCatCount = subCatCount;
    }
}