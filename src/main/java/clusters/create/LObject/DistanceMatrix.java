package clusters.create.LObject;

import clusters.create.GeoCLusteringNew;
import com.github.davidmoten.geo.GeoHash;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
/**
 * Created by gurramvinay on 8/10/15.
 */
public class DistanceMatrix {

    private final HashMap<String, Double> dMatrix = new HashMap<String, Double>();
    private final String CCATTERM = "##";

    public DistanceMatrix(Geopoint geoHash, List<String> points){
        computeDistancMatrix(geoHash,points);
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

    synchronized private void computeDistancMatrix(Geopoint geoHashString,List<String> clusteringPoints){
        for(String cp: clusteringPoints){
            //geoHash Calculation
            dMatrix.put(getHash(GeoHash.encodeHash(geoHashString.getLatitude(),geoHashString.getLongitude(),7),cp,true),Geopoint.getDistance(geoHashString, GeoCLusteringNew.clusterPoints.get(cp).getLocation()));

            for(String tp: clusteringPoints){
                if(!cp.contentEquals(tp)){
                    String hashId = getHash(tp,cp,true);
                    String hashId2 = getHash(tp,cp,false);
                    if(dMatrix.containsKey(hashId) || dMatrix.containsKey(hashId2)){
                    }else {
                        dMatrix.put(hashId,Geopoint.getDistance(GeoCLusteringNew.clusterPoints.get(cp).getLocation(),GeoCLusteringNew.clusterPoints.get(tp).getLocation()));
                    }
                }
            }
        }
    }


    public void getGoogleDistanceBetweenPoints(Geopoint g1, Geopoint g2) {

        try {
            String API= "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "key=AIzaSyDonh7jhpT3yEUXTNCj_o1a_Fo5NSPpdUA&" +
                    "origins="+g1.getLatitude()+","+g1.getLongitude()+"&" +
                    "destinations="+g1.getLatitude()+","+g2.getLongitude()+"&mode=driving";
            URL url = new URL(API);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            String result = IOUtils.toString(urlConnection.getInputStream());



        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
