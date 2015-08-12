package clusters.updates.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

public class Subscriber extends JedisPubSub {

    private Logger logger = LoggerFactory.getLogger(Subscriber.class);

    @Override
    public void onMessage(String channel,  String message){
        //making channels
        if(channel.contentEquals("inventory_add")){

            logger.info("add ");
            logger.info("message received "+ message);
        }
        if(channel.contentEquals("inventory_update")){
            logger.info("update ");
            logger.info("message received "+ message);

        }
        if(channel.contentEquals("inventory_delete")){
            logger.info("delete ");
            logger.info("message received "+ message);

        }

    }
    @Override
        public void onPMessage(String pattern, String channel, String message) {
        }
    @Override
        public void onSubscribe(String channel, int subscribedChannels) {
    }
    @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
    }
    @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }
    @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
    }
     }
