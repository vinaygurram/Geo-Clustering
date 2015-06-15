import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Singleton Class
 * To get ESClient
 * Created by gurramvinay on 6/12/15.
 */
public class ESClient {

    private ESClient(){};
    private static final String HOSTNAME = "http://es.qa.olahack.in/";
    private static final String CLUSTER_NAME = "elasticsearch";
    private static final int PORT = 9300;

    private static Client esClient;

    public static Client getESClient(){
       if(esClient==null){
           Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name",CLUSTER_NAME).build();
           esClient = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(HOSTNAME,PORT));
        }
        return esClient;
    }
}
