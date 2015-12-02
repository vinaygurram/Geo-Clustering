package com.olastore.listing.clustering.algorithms;

import com.github.davidmoten.geo.GeoHash;
import com.olastore.listing.clustering.utils.DistanceMatrix;
import com.olastore.listing.clustering.lib.models.Geopoint;
import com.olastore.listing.clustering.lib.models.ClusterDefinition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * defining strategy to create the geo hash based cluster. Covers point
 * selection criteria, Sample Space for Cluster, Max Distance Coverage,Cluster
 * Selection Criteria distance matrix is computed for each geo hash
 */

public class ClusterStrategy {

	private static Logger LOG = LoggerFactory.getLogger(ClusterStrategy.class);

	private DistanceMatrix distanceMatrix;
	private Map esConfig;
	private Map clusterConfig;
	private final int max_stores;
	private final int max_radius;

	public ClusterStrategy(int max_stores, int max_radius) {
		this.max_radius = max_radius;
		this.max_stores = max_stores;
	}

	public void createDistanceMatrix(Geopoint geoHash, List<String> points) {
		List<String> ttpoints = new ArrayList<String>(points);
		this.distanceMatrix = new DistanceMatrix(geoHash, ttpoints, clusterConfig);
	}

	public List<ClusterDefinition> createClusters(Geopoint geoHash, List<String> points, Map esConfig,
																								Map clusterConfig) throws Exception {
		this.esConfig = esConfig;
		this.clusterConfig = clusterConfig;
		if (points == null || points.size() == 0)
			return new ArrayList<ClusterDefinition>();
		int clustersForCombination = 0;

		createDistanceMatrix(geoHash, points);
		List<ClusterDefinition> validClusters = new ArrayList<ClusterDefinition>();
		Set<String> subSetCombination = new HashSet<>();
		String encodedGeoHash = GeoHash.encodeHash(geoHash.getLatitude(), geoHash.getLongitude(),
				(Integer) clusterConfig.get("clusters_geo_precision"));
		ClusterDefinition temp;
		Set<List<String>> clusters;


		for (int i = max_stores; i > 0; i--) {
			if (points.size() >= i) {
				clusters = getAllCombinations(points, i);
				for (List<String> clusterObj : clusters) {
					temp = checkValidCluster(geoHash, clusterObj, subSetCombination);
					if (temp != null) {
						clustersForCombination++;
						temp.setGeoHash(encodedGeoHash);
						validClusters.add(temp);
					}
				}
			}
		}
		this.distanceMatrix = null;
		return validClusters;
	}

	// Helper methods
	/**
	 * computes rank for a given cluster Rank r =
	 * (popularProductsInCluster/totalPopularProducts)
	 */
	public void setRankParameters(Set<String> popularProductsSet, ClusterDefinition clusterObj) {

		if (clusterObj.getPoints().size() == 0)
			return;
		String storeIdString = "";
		String clusterId = "";
		List<String> stores = clusterObj.getPoints();
		Collections.sort(stores);
		for (String s : stores) {
			storeIdString += "\"" + s + "\",";
			clusterId += "-" + s;
		}
		clusterId = clusterId.substring(1);
		storeIdString = storeIdString.substring(0, storeIdString.length() - 1);

		if (ClusterBuilder.clusterRankMap.containsKey(clusterId)) {
			clusterObj.setRank(ClusterBuilder.clusterRankMap.get(clusterId));
			return;
		}

		Set<String> productsSet = new HashSet<>();
		int subCatCount = 0;
		try {

			String query = "{\"size\": 0,\"query\":{\"filtered\":{\"filter\":{\"bool\":{\"must\":["
					+ "{\"terms\":{\"store_details.id\":[" + storeIdString + "]}},"
					+ "{\"term\":{\"product_details.available\":true}},"
					+ "{\"term\":{\"product_details.status\":\"current\"}}]}}}},"
					+ "\"aggregations\":{\"unique_products\":{\"terms\":{\"field\":\"product_details.id\",\"size\":0}},"
					+ "\"sub_cat_count\":{\"cardinality\":{\"field\":\"product_details.sub_category_id\"}}}}";
			JSONObject result = ClusterBuilder.esClient.searchES((String) esConfig.get("listing_index_name"),
					(String) esConfig.get("listing_index_type"), query);
			JSONObject esResult = result.getJSONObject("aggregations");
			JSONArray uniqueProdBuckets = esResult.getJSONObject("unique_products").getJSONArray("buckets");
			for (int i = 0; i < uniqueProdBuckets.length(); i++) {
				String productId = uniqueProdBuckets.getJSONObject(i).getString("key");
				productsSet.add(productId);
			}
		} catch (Exception e) {
			LOG.error("Found error while setting ranks {}", e);
		}
		Set<String> intesection = new HashSet<String>(productsSet);
		intesection.retainAll(popularProductsSet);
		int popular_products_count = intesection.size();
		double rank = ((double) popular_products_count / (double) popularProductsSet.size());

		ClusterBuilder.clusterRankMap.put(clusterId, rank);
		clusterObj.setRank(rank);
	}

	/**
	 * Shortest Distance if store has only 2 points
	 */
	public double getShortestDistanceFor2(Geopoint geohashPoint, List<String> points) {

		double dbp = distanceMatrix.getDistance(points.get(0), points.get(1));
		String geoString = GeoHash.encodeHash(geohashPoint.getLatitude(), geohashPoint.getLongitude(),
				(Integer) clusterConfig.get("clusters_geo_precision"));
		double dfg = distanceMatrix.getDistance(geoString, points.get(0));
		double dsg = distanceMatrix.getDistance(geoString, points.get(1));
		dfg = dfg + dbp;
		dsg = dsg + dbp;
		return dfg > dsg ? dsg : dfg;
	}

	public double getShortestDistanceForMultiPoints(Geopoint geohashPoint, List<String> points) {
		double smallestDistace = Double.MAX_VALUE;
		String geoString = GeoHash.encodeHash(geohashPoint.getLatitude(), geohashPoint.getLongitude(),
				(Integer) clusterConfig.get("clusters_geo_precision"));
		for (String tp : points) {
			double di = distanceBtPoints(tp, geoString);

			double si = getShortestDistanceWithPoint(tp, points);
			double total = di + si;
			if (total < smallestDistace) {
				smallestDistace = total;
			}
		}
		return smallestDistace;
	}

	/**
	 * helper method
	 *
	 * @return the distance between two geo points
	 */
	public double distanceBtPoints(String c1, String c2) {
		return distanceMatrix.getDistance(c1, c2);
	}

	/**
	 * computes the shortest path using all possible combinations
	 *
	 * @return Double shortest distance which connects all the distances
	 */
	public double getShortestDistanceWithPoint(String cp2, List<String> list) {
		try {
			List<String> ss = new ArrayList<String>();
			for (String s : list) {
				if (s.contentEquals(cp2))
					continue;
				ss.add(s);
			}
			String[] idsString = new String[ss.size()];
			idsString = ss.toArray(idsString);
			List<List<String>> permutations = permute(idsString);
			double gDistance = Double.MAX_VALUE;
			for (List<String> possiblity : permutations) {

				double tempDist = distanceMatrix.getDistance(cp2, possiblity.get(0));
				for (int i = 0; i < possiblity.size() - 1; i++) {
					tempDist += distanceMatrix.getDistance(possiblity.get(i), possiblity.get(i + 1));
				}
				if (tempDist < gDistance)
					gDistance = tempDist;
			}
			return gDistance;
		} catch (Exception e) {
			LOG.error("Error in calculating distance {}", e);

		}
		return Double.MAX_VALUE;
	}

	public Set<List<String>> getAllCombinations(List<String> inputArray, int combinationSize) {
		if (combinationSize > inputArray.size())
			return new HashSet<List<String>>();
		Set<List<String>> total = new HashSet<>();
		combinationUtil(0, 0, inputArray.size(), combinationSize, inputArray, new ArrayList<String>(), total);
		return total;
	}

	public void combinationUtil(int index, int start, int end, int combinationSize, List<String> inputArray,
															List<String> thisCombination, Set<List<String>> totalCombinations) {
		if (index == combinationSize) {
			totalCombinations.add(new ArrayList<>(thisCombination));
			return;
		}
		for (int i = start; i < end && end + i + 1 >= combinationSize - index; i++) {
			thisCombination.add(inputArray.get(i));
			combinationUtil(index + 1, i + 1, end, combinationSize, inputArray, thisCombination, totalCombinations);
			thisCombination.remove(inputArray.get(i));
		}
	}

	public Set<String> getAllSubSets(List<String> storeIdList) {
		Set<String> subSets = new HashSet<String>();
		for (int i = 1; i < storeIdList.size(); i++) {
			Set<List<String>> allCombinations = getAllCombinations(storeIdList, i);
			for (List<String> tempList : allCombinations) {
				Collections.sort(tempList);
				StringBuilder sb = new StringBuilder();
				for (String s : tempList) {
					sb.append("-");
					sb.append(s);
				}
				String hash = sb.toString().substring(1);
				subSets.add(hash);
			}
		}
		return subSets;
	}

	/**
	 * Check if the point cluster is valid
	 */
	public ClusterDefinition checkValidCluster(Geopoint geoHash, List<String> storeIdList,
																						 Set<String> subCombinationsSet) {

		Collections.sort(storeIdList);
		StringBuilder sb = new StringBuilder();
		for (String s : storeIdList) {
			sb.append("-");
			sb.append(s);
		}
		String hash = sb.toString().substring(1);
		if (subCombinationsSet.contains(hash))
			return null;
		double shortDistance = Double.MAX_VALUE;
		if (storeIdList.size() == 1) {
			shortDistance = Geopoint.getDistance(geoHash,
					ClusterBuilder.clusterPoints.get(storeIdList.get(0)).getLocation());
		} else if (storeIdList.size() == 2) {
			shortDistance = getShortestDistanceFor2(geoHash, storeIdList);
		} else if (storeIdList.size() >= 3) {
			shortDistance = getShortestDistanceForMultiPoints(geoHash, storeIdList);
		}
		if (shortDistance > max_radius)
			return null;

		ClusterDefinition clusterDefinition = new ClusterDefinition();
		for (String s : storeIdList) {
			String thisStoreStatus = ClusterBuilder.storeStatusMap.get(s);
			if (thisStoreStatus.contentEquals("active")) {
				clusterDefinition.addPoint(s);
			}
		}
		clusterDefinition.setId(hash);
		clusterDefinition.setDistance(shortDistance);
		setRankParameters(ClusterBuilder.popularProdSet, clusterDefinition);
		Set<String> subSets = getAllSubSets(storeIdList);
		subCombinationsSet.addAll(subSets);
		return clusterDefinition;
	}

	/**
	 * Creates all possible String ids;
	 */
	public List<List<String>> permute(String[] ids) {
		List<List<String>> permutations = new ArrayList<List<String>>();
		permutations.add(new ArrayList<String>());
		for (int i = 0; i < ids.length; i++) {
			List<List<String>> current = new ArrayList<List<String>>();
			for (List<String> permutation : permutations) {
				for (int j = 0, n = permutation.size() + 1; j < n; j++) {
					List<String> temp = new ArrayList<String>(permutation);
					temp.add(j, ids[i]);
					current.add(temp);
				}
			}
			permutations = new ArrayList<>(current);
		}
		return permutations;
	}

}