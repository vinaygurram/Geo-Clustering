package clusters.create;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.jongo.*;


/**
 * Created by gurramvinay on 8/22/15.
 */
public class MongoJClient {
    private static final String MONGO_HOST = "localhost";
    private static final String MONGO_DB = "geokit";
    public static final String MONGO_COLLECTION = "localities";
    private static MongoClient mongoClient;

    private MongoJClient(){
    }

    private static MongoClient getMongoClient(){
        try {
            if(mongoClient==null){
                mongoClient = new MongoClient(MONGO_HOST);
            }
            return mongoClient;
        }catch (Exception e){
            System.out.println("MONGO DB CONNECTION IS NOT MADE");
            e.printStackTrace();
        }
        return null;
    }

    public static Jongo getJongoClietn(){
        try {
            DB geo = getMongoClient().getDB(MONGO_DB);
            return new Jongo(geo);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void close(){
        mongoClient.close();
    }
}
