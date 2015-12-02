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
    String redis_passwd = (String) redisConfig.readValue("redis_passwd_"+env);
    int redis_port = (int) redisConfig.readValue("redis_port_"+env);
    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setMaxTotal((int)redisConfig.readValue("redis_pool_maxActive"));
    jedisPoolConfig.setMaxIdle((int) redisConfig.readValue("redis_pool_maxIdle"));
    jedisPoolConfig.setTestOnBorrow((boolean)redisConfig.readValue("redis_pool_testOnBorrow"));
    jedisPoolConfig.setTestOnReturn((boolean)redisConfig.readValue("redis_pool_testOnReturn"));
    if(redis_passwd.isEmpty()) this.pool = new JedisPool(jedisPoolConfig,redis_host,redis_port);
    else this.pool = new JedisPool(jedisPoolConfig,redis_host,redis_port,2000,redis_passwd);
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
