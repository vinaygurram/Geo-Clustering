package clusters.updates.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
public class RedisPubSub
{
    public static final String inventoryAddChannel= "inventory_add";
    public static final String inventoryUpdateChannel= "inventory_update";
    public static final String inventoryDeleteChannel= "inventory_delete";

    public static void main(String[] args) throws Exception
    {      
        JedisPool jedispool = new JedisPool("localhost");
        final Jedis subscriberJedis = jedispool.getResource();
 
        final Subscriber subscriber = new Subscriber();
        new Thread(new Runnable(){
            public void run()
            {
                try
                {
                    System.out.println("Subscribing to " +inventoryAddChannel);
                    subscriberJedis.subscribe(subscriber,inventoryAddChannel,inventoryUpdateChannel,inventoryDeleteChannel);
                    System.out.println("Subscription ended.");
                }
                catch (Exception e)
                {
                    System.out.println("Subscribing failed."+e);
                }
            }
        }).start();
        jedispool.returnResource(subscriberJedis);
    }
}
