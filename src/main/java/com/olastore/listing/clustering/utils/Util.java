package com.olastore.listing.clustering.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Util {
	public static Map setListingIndexNameForCity(Map map, String key, String city) {
		String indexName = (String) map.get(key);
		indexName = indexName + "_" + city;
		map.put(key, indexName);
		return map;
	}

	public static Map updateListingIndexNameForCity(Map map, String key, String oldCity, String newCity) {
		String indexName = (String) map.get(key);
		indexName = indexName.replace(oldCity,newCity);
		map.put(key, indexName);
		return map;
	}

	public static Map setClusterIndexes(Map map, String geo_index_key, String cluster_index_key) {
		DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
		Date today = Calendar.getInstance().getTime();
		String dateString = dateFormat.format(today);
		String indexName = (String) map.get(geo_index_key);
		indexName = indexName + "_" + dateString;
		map.put(geo_index_key, indexName);
		indexName = (String) map.get(cluster_index_key);
		indexName = indexName + "_" + dateString;
		map.put(cluster_index_key, indexName);
		return map;
	}

	public static String getIndexCreatedNdaysBack(String indexNameForToday,int days){
		DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		String dateString = dateFormat.format(today);
		calendar.add(Calendar.DATE,-days);
		Date yesterday = calendar.getTime();
		String yesDateString =  dateFormat.format(yesterday);
		indexNameForToday = indexNameForToday.replace(dateString,yesDateString);
		return indexNameForToday;
	}
}
