package com.olastore.listing.clustering.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

public class RedisClientOperationImpl implements RedisClientOperation {

    private static final Logger logger = LoggerFactory.getLogger(RedisClientOperationImpl.class);
    private static Jedis jedisResource ;
    private final String geoHashKey;
    private final String geoHashCityKey;

    public RedisClientOperationImpl(Jedis jedis, Map redisConf) {
        this.jedisResource = jedis;
        this.geoHashCityKey = (String) redisConf.get("geo_hash_city_key");
        this.geoHashKey = (String) redisConf.get("geo_hash_key");
    }

    public String mset(String key, Map<String, String> map) {
        try {
            return jedisResource.hmset(key, map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public List<String> mget(String key, String... field) {
        return jedisResource.hmget(key, field);
    }

    public void set(String key, String data) {
        jedisResource.set(key, data);
    }

    public void setOnlyNonExist(String key, String data) {
        jedisResource.setnx(key, data);
    }

    public void setWithExpireTime(String key, String data, int sec) {
        jedisResource.setex(key, sec, data);
    }

    public String get(String key) {
        return jedisResource.get(key);
    }

    public void lpush(String key, String... values) {
        jedisResource.lpush(key, values);
    }

    public List<String> range(String key, int start, int end) {
        return jedisResource.lrange(key, start, end);
    }

    public String lpop(String key) {
        return jedisResource.lpop(key);
    }

    public String rpop(String key) {
        return jedisResource.rpop(key);
    }

    public void rpush(String key, String... values) {
        jedisResource.rpush(key, values);
    }

    public String getParamsForGeoHash(String geohash) {
        return get(geoHashKey+geohash);
    }

    public String getParamsForCity(String city) {
        return get(geoHashCityKey+city);
    }

}