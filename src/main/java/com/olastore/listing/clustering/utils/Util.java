package com.olastore.listing.clustering.utils;

import java.util.Map;

/**
 * Created by gurramvinay on 10/8/15.
 */
public class Util {
  public static Map setListingIndexNameForCity(Map map,String key, String city){
    String indexName = (String)map.get(key);
    indexName = indexName+"_"+city;
    map.put(key,indexName);
    return map;
  }
}
