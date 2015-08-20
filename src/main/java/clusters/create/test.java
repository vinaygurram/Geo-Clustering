package clusters.create;


import clusters.create.LObject.ClusteringPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gurramvinay on 7/10/15.
 */
public class test {
    public List<List<String>> getAllCombinations(List<String> stringList){

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

    public List<ClusterObj> getAllClusters(List<ClusteringPoint> stringList){

        List<ClusterObj> totalList = new ArrayList<ClusterObj>();
        for(int i=0;i<stringList.size();i++){
            ClusterObj clusterObj = new ClusterObj();
            clusterObj.addPoint(stringList.get(i));
            //List<ClusteringPoint> tempList = new ArrayList<ClusteringPoint>();
            //tempList.add(stringList.get(i));
            for(int j=i+1;j+1<stringList.size() ;j++){
                clusterObj.addPoint(stringList.get(j));
                clusterObj.addPoint(stringList.get(j+1));
                totalList.add(clusterObj);
                clusterObj = new ClusterObj();
                clusterObj.addPoint(stringList.get(i));
            }
        }
        return totalList;
    }
    public List<ClusterObj> getAll2Clusters(List<ClusteringPoint> stringList){

        List<ClusterObj> totalList = new ArrayList<ClusterObj>();
        for(int i=0;i<stringList.size();i++){
            ClusterObj clusterObj = new ClusterObj();
            clusterObj.addPoint(stringList.get(i));
            for(int j=i+1;j<stringList.size() ;j++){
                clusterObj.addPoint(stringList.get(j));
                totalList.add(clusterObj);
                clusterObj = new ClusterObj();
                clusterObj.addPoint(stringList.get(i));
            }
        }
        return totalList;
    }

    public List<List<String>> getAll2Strings(List<String> strings){
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

    public String getHashForCHM(List<String> strings){
        Collections.sort(strings);
        StringBuilder sb = new StringBuilder();
        for(String s : strings){
            sb.append("-");
            sb.append(s);
        }
        return sb.toString().substring(1);

    }

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

    public static void main(String[] args){
        test tes = new test();
        List<String> teds = new ArrayList<String>();
        teds.add("1");
        teds.add("2");
        teds.add("3");
        teds.add("4");
        teds.add("5");
        teds.add("6");
        teds.add("7");
        teds.add("8");
        List<List<String>> stringList = tes.get6CClusters(teds);
        System.out.println(stringList);


    }
}
