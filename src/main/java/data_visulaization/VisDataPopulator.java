package data_visulaization;

import clusters.create.objects.BoundingBox;
import clusters.create.objects.Geopoint;
import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by gurramvinay on 8/27/15.
 */
public class VisDataPopulator {


    private static int GEO_PRECISION = 7;

    private static double BLR_TOP_LEFT_LAT = 13.11091;
    private static double BLR_TOP_LEFT_LON = 77.46253;
    private static double BLR_BOT_RIGHT_LAT = 12.81581;
    private static double BLR_BOT_RIGHT_LON = 77.79075;

    private static final String ES_DATA_VIS_API= "http://localhost:9200/visual_index/geo";

    private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision) {
        Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
                box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
        System.out.println(boxCoverage.toString());
        return boxCoverage.getHashes();
    }

    private BoundingBox getBangaloreBox() {
        Geopoint topleft = new Geopoint(BLR_TOP_LEFT_LAT, BLR_TOP_LEFT_LON);
        Geopoint botright = new Geopoint(BLR_BOT_RIGHT_LAT, BLR_BOT_RIGHT_LON);
        return new BoundingBox(topleft, botright);
    }


    public List<String> getBlrGeoHashes() {
        BoundingBox bbox = getBangaloreBox();
        Set<String> hashes = getGeoHashOfBoundingBox(bbox, GEO_PRECISION);
        Iterator<String> iterator = hashes.iterator();
        int valitGeoHashCount = 0;
        List<String> geohashList = new ArrayList<String>();
        while (iterator.hasNext()) {
            String thisHash = iterator.next();
            geohashList.add(thisHash);
            //String zone = getZoneFromMongo(thisHash);
            //if(!(zone.isEmpty() || zone.contentEquals(""))){
            //    geohashList.add(thisHash);
            //    valitGeoHashCount++;
            //}
        }
        // MongoJClient.close();
        System.out.println("total number of hashes " + geohashList.size());
        return geohashList;
    }


    private void pushData(List<String> geoHashes){
        for(String geoHash: geoHashes){
            LatLong latLong = GeoHash.decodeHash(geoHash);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",geoHash);
            jsonObject.put("location",latLong.getLat()+","+latLong.getLon());
            jsonObject.put("total_products",1000);


            try {
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(ES_DATA_VIS_API+"/"+geoHash);
                httpPost.setEntity(new StringEntity(jsonObject.toString()));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                System.out.println(httpResponse.getStatusLine().getStatusCode());
            }catch (Exception e){
                e.printStackTrace();
            }




        }

    }

    public static void main(String[] args) {
        VisDataPopulator visDataPopulator = new VisDataPopulator();
        visDataPopulator.pushData(visDataPopulator.getBlrGeoHashes());
    }
}
