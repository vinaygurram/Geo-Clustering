package gridbase;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by gurramvinay on 6/30/15.
 */
public class DistanceMatrix {

    private final HashMap<String, Double> dMatrix = new HashMap<String, Double>();
    private final String CCATTERM = "##";

    public DistanceMatrix(List<ClusteringPoint> points){
        computeDistancMatrix(points);
    }

    private String getHash(String id1, String id2,boolean order) {
        if(order) return new StringBuilder().append(id1).append(CCATTERM).append(id2).toString();
        else return new StringBuilder().append(id2).append(CCATTERM).append(id1).toString();
    }

    public HashMap<String,Double> getNodeDistanceMatrix(String id){
        HashMap<String, Double> ndMap = new HashMap<String, Double>();
        Set<String> keySet = dMatrix.keySet();
        for(String key : keySet){
            String[] keys = key.split("##");
            if(keys[0].contentEquals(id)){
                ndMap.put(keys[1],dMatrix.get(key));
            }else if(keys[1].contentEquals(id)){
                ndMap.put(keys[0],dMatrix.get(key));
            }
        }
        return ndMap;
    }


    public double getDistance(String id1, String id2){
        String hash1 = getHash(id1,id2,true);
        if(this.dMatrix.containsKey(hash1)) return dMatrix.get(hash1);
        hash1 = getHash(id1,id2,false);
        if(this.dMatrix.containsKey(hash1)) return dMatrix.get(hash1);
        return 0d;
    }

    private void computeDistancMatrix(List<ClusteringPoint> clusteringPoints){
        for(ClusteringPoint cp: clusteringPoints){
           for(ClusteringPoint tp: clusteringPoints){
               if(cp.getId()!=tp.getId()){
                   String hashId = getHash(tp.getId(),cp.getId(),true);
                   String hashId2 = getHash(tp.getId(),cp.getId(),false);
                   if(dMatrix.containsKey(hashId) || dMatrix.containsKey(hashId2)){
                   }else {
                       dMatrix.put(hashId,Geopoint.getDistance(cp.getLocation(),tp.getLocation()));
                   }
               }
           }
        }
    }

}
