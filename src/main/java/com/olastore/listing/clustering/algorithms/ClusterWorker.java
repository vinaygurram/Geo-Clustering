package com.olastore.listing.clustering.algorithms;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.NewRelicApiImplementation;
import com.olastore.listing.clustering.lib.models.Geopoint;
import com.olastore.listing.clustering.lib.models.ClusterDefinition;
import com.olastore.listing.clustering.lib.models.ClusterPoint;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Class creates clusters for a geo hash and push the clusters into ES.
 */
public class ClusterWorker implements Callable<String> {
	String geohash;
	Map esConfig;
	Map clustersConfig;
	int max_shops;
	int max_radius;

	ClusterWorker(String geohash, Map esConfig, Map clustersConfig,int max_radius, int max_shops) {
		this.geohash = geohash;
		this.clustersConfig = clustersConfig;
		this.esConfig = esConfig;
		this.max_radius = max_radius;
		this.max_shops = max_shops;

	}

	private static final Logger logger = LoggerFactory.getLogger(ClusterWorker.class);

	/**
	 * Calls listing index to find stores within 6kms Calls stores index to get
	 * lat,long values for stores
	 */
	public List<String> getClusetringPointsForGeoHash(String geohash) {
		List<String> reShops = new ArrayList<String>();
		try {
			int cluster_radius = (Integer) clustersConfig.get("clusters_radius");
			String query = "{\"size\":\"100\",\"query\":{\"filtered\":{\"filter\":{\"geo_distance\":{\"location\":\""
					+ geohash + "\",\"distance\":\"" + cluster_radius + "km\"}}}}}";
			JSONObject jsonObject = ClusterBuilder.esClient.searchES((String) esConfig.get("stores_index_name"),
					(String) esConfig.get("stores_index_type"), query);
			JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");
			for (int i = 0; i < hits.length(); i++) {
				JSONObject thisStoreObject = hits.getJSONObject(i);
				String id = thisStoreObject.getJSONObject("_source").getJSONObject("store_details").getString("id");
				double lat = thisStoreObject.getJSONObject("_source").getJSONObject("store_details")
						.getJSONObject("location").getDouble("lat");
				double lng = thisStoreObject.getJSONObject("_source").getJSONObject("store_details")
						.getJSONObject("location").getDouble("lon");
				reShops.add(id);
				ClusterBuilder.storeStatusMap.put(id, thisStoreObject.getJSONObject("_source")
						.getJSONObject("store_details").getString("store_state"));
				ClusterPoint clusterPoint = new ClusterPoint(id, new Geopoint(lat, lng));
				ClusterBuilder.clusterPoints.put(id, clusterPoint);
			}

		} catch (JSONException e) {
			logger.error("Json exception ", e);
		} catch (Exception e) {
			logger.error("Exception ", e);
		}
		return reShops;
	}

	@Override
	public String call() {
		try {
			List<String> points = getClusetringPointsForGeoHash(geohash);
			if (points.size() == 0)
				return "DONE for " + geohash + "-- no shops within the raidus";
			LatLong gll = GeoHash.decodeHash(geohash);
			Geopoint geopoint = new Geopoint(gll.getLat(), gll.getLon());
			List<ClusterDefinition> clusterDefinitionList = null;
			clusterDefinitionList = new ClusterStrategy(max_shops,max_radius).createClusters(geopoint, points, esConfig, clustersConfig);
			logger.info("Geohash : " + geohash + ", Number of shops : " + points.size() + ", Number of Clusters : "
					+ clusterDefinitionList.size());
			if (clusterDefinitionList.size() > 0) {
				pushClusters(clusterDefinitionList);
			}
		} catch (Exception e) {
			logger.error("Exception ", e);
		}
		NewRelic.incrementCounter("Custom/geohash");
		return "DONE for " + geohash;
	}

	public void pushClusters(List<ClusterDefinition> clusterDefinitions) {
		try {
			JSONObject geoDoc = new JSONObject();
			geoDoc.put("id", clusterDefinitions.get(0).getGeoHash());
			geoDoc.put("clusters_count", clusterDefinitions.size());
			JSONArray clusters = new JSONArray();
			geoDoc.put("clusters", clusters);

			Collections.sort(clusterDefinitions, new Comparator<ClusterDefinition>() {
				@Override
				public int compare(ClusterDefinition o1, ClusterDefinition o2) {
					double diff = o2.getRank() - o1.getRank();
					if (diff > 0)
						return 1;
					if (diff < 0)
						return -1;
					if (diff == 0) {
						double distanceDiff = o1.getDistance() - o2.getDistance();
						if (distanceDiff > 0)
							return 1;
						if (distanceDiff < 0)
							return -1;
						if (distanceDiff == 0)
							return 0;
					}
					;
					return 1;
				}
			});

			int count = 0;
			for (ClusterDefinition clusterDefinition : clusterDefinitions) {
				count++;
				if (count > 50)
					break;
				List<String> stringList = clusterDefinition.getPoints();
				Collections.sort(stringList);
				String clusterId = clusterDefinition.getId();
				clusters.put(clusterDefinition.getId());

				if (!ClusterBuilder.pushedClusters.contains(clusterId)) {
					ClusterBuilder.esClient.pushToES((String) esConfig.get("clusters_index_name"),
							(String) esConfig.get("clusters_index_type"), clusterId, clusterDefinition.toString());
					ClusterBuilder.pushedClusters.add(clusterId);
				}
			}
			String thisDocAsString = "{\"index\" : {\"_index\" : \"" + (String) esConfig.get("geo_hash_index_name")
					+ "\",\"_type\" : \"" + (String) esConfig.get("geo_hash_index_type") + "\",\"_id\":\""
					+ clusterDefinitions.get(0).getGeoHash() + "\" }}\n" + geoDoc.toString() + "\n";
			String maxString = "";

			synchronized (ClusterBuilder.bulkDoc) {
				ClusterBuilder.bulkDoc.append(thisDocAsString);
				ClusterBuilder.bulkDocCount.incrementAndGet();
				if (ClusterBuilder.bulkDocCount.get() > 100) {
					maxString = ClusterBuilder.bulkDoc.toString();
					ClusterBuilder.bulkDoc = new StringBuilder();
					ClusterBuilder.bulkDocCount = new AtomicInteger(0);
				}
			}

			if (!maxString.isEmpty()) {
				ClusterBuilder.esClient.pushToESBulk("", "", maxString);
			}

		} catch (Exception e) {
			logger.error("Something went wrong while pushing clusters ", e);
		}
	}
}
