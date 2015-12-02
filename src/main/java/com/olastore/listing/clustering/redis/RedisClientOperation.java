package com.olastore.listing.clustering.redis;

import java.util.List;
import java.util.Map;

public interface RedisClientOperation {
	public String get(String key);
	public List<String> mget(String key, String... field);
	
	public void set(String key, String data);
	public String mset(String key, Map<String, String> map);
	
	public void setOnlyNonExist(String key, String data);
	public void setWithExpireTime(String key, String data,int expireTime);
	
	
	public void lpush(String key, String... values);
	public String lpop(String key);
	
	public String rpop(String key);
	public void rpush(String key, String... values);

	public List<String> range(String key, int start, int end);
	
}