package com.olastore.listing.clustering.redis;

import com.olastore.listing.clustering.utils.ConfigReader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileNotFoundException;

/**
 * Created by gurramvinay on 10/11/15.
 */
public class RedisClient {
  private static JedisPool pool;

  public RedisClient(String env) throws FileNotFoundException {
    ConfigReader redisConfig = new ConfigReader("config/redis.yaml");
    String redis_host = (String) redisConfig.readValue("redis_host_"+env);
    int redis_port = (int) redisConfig.readValue("redis_port"+env);
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    this.pool = new JedisPool(redis_host,redis_port);
  }


  public void connectionDestroy() {
    pool.destroy();
  }

  public Jedis getResource() {
    return pool.getResource();
  }

  public void returunReource(Jedis resource) {
    pool.returnResourceObject(resource);
  }

}
