package live.cluster.one;

import gridbase.ClusterObj;
import gridbase.ClusteringPoint;

import java.util.ArrayList;
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

    public static void main(String[] args){
        test tes = new test();
        List<String> teds = new ArrayList<String>();
        teds.add("1");
        teds.add("2");
        teds.add("3");
        teds.add("4");
        teds.add("5");


        String[] ssip = {"12"};
        ClusteringPoint cc = new ClusteringPoint("1",ssip,null);
        ClusteringPoint cd = new ClusteringPoint("2",ssip,null);
        ClusteringPoint ce = new ClusteringPoint("3",ssip,null);
        ClusteringPoint fd = new ClusteringPoint("4",ssip,null);
        List<ClusteringPoint > list = new ArrayList<ClusteringPoint>();
        list.add(cc);
        list.add(cd);
        list.add(ce);
        list.add(fd);
        List<ClusterObj> ssd = tes.getAll2Clusters(list);
        //teds.add("4");
        //teds.add("5");
        List<List<String>> aes = tes.getAllCombinations(teds);
//        List<List<String>> aes = tes.getAll2Strings(teds);
//        for(List<String> sd : aes){
//            for(String ss : sd){
//                System.out.print(" " + ss);
//            }
//            System.out.println();
//        }

    }
}
