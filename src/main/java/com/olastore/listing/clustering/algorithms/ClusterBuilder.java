package com.olastore.listing.clustering.algorithms;

import com.olastore.listing.clustering.clients.ESClient;
import com.olastore.listing.clustering.utils.ConfigReader;
import com.olastore.listing.clustering.utils.GeoHashUtil;
import com.olastore.listing.clustering.lib.models.ClusterPoint;
import com.olastore.listing.clustering.utils.Util;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.FileEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main class to create clusters. It will have all the utility Objects.
 */
public class ClusterBuilder {

	public static ESClient esClient;
	private Map esConfig;
	private Map clustersConfig;

	public static ConcurrentHashMap<String, ClusterPoint> clusterPoints = new ConcurrentHashMap<>();
	public static List<String> pushedClusters = Collections.synchronizedList(new ArrayList<String>());
	public static ConcurrentHashMap<String, Double> clusterRankMap = new ConcurrentHashMap<>();
	public static Set<String> popularProdSet = new HashSet<>();
	public static AtomicInteger bulkDocCount = new AtomicInteger(0);
	public static StringBuilder bulkDoc = new StringBuilder();
	public static Logger logger = LoggerFactory.getLogger(ClusterBuilder.class);
	public static ConcurrentHashMap<String, String> storeStatusMap = new ConcurrentHashMap<>();

	public ClusterBuilder(String env, ConfigReader esConfigReader, ConfigReader clustersConfigReader) {
		String esHostKey = "es_host_" + env;
		this.esConfig = esConfigReader.readAllValues();
		this.clustersConfig = clustersConfigReader.readAllValues();
		this.esClient = new ESClient((String) esConfig.get(esHostKey));
	}

	public void reinitializeClusteringIndices() {
		try {
			String indexName = esConfig.get("geo_hash_index_name") + "," + esConfig.get("clusters_index_name");
			esClient.deleteIndex(indexName);
			File file = new File("tmpFile");
			FileUtils.copyInputStreamToFile(
					getClass().getClassLoader().getResourceAsStream((String) esConfig.get("geo_mappings_file_path")),
					file);
			JSONObject create_geo_response = esClient.createIndex((String) esConfig.get("geo_hash_index_name"),
					new FileEntity(file));
			logger.info("Creating geo mappings {}", create_geo_response.toString());

			FileUtils.copyInputStreamToFile(getClass().getClassLoader()
					.getResourceAsStream((String) esConfig.get("cluster_mappings_file_path")), file);
			JSONObject create_cluster_response = esClient.createIndex((String) esConfig.get("clusters_index_name"),
					new FileEntity(file));
			logger.info("Creating cluster mappings {}", create_cluster_response.toString());
			file.delete();
		} catch (Exception e) {
			logger.error("Exception in create/delete indices {}", e);
		}
	}

	private Set<String> initializePopularProductSet() {

		Set<String> productIdSet = new HashSet<>();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			File file = new File("tmpFile");
			FileUtils.copyInputStreamToFile(getClass().getClassLoader()
					.getResourceAsStream((String) clustersConfig.get("popular_products_file_path")), file);
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				productIdSet.add(line);
			}
			file.delete();
		} catch (Exception e) {
			logger.error("Exception happened!{}", e);
		} finally {
			try {
				if (fileReader != null)
					fileReader.close();
				if (bufferedReader != null)
					bufferedReader.close();
			} catch (Exception e) {
				logger.error("Exception happened!{}", e);
			}
		}
		return productIdSet;
	}

	public void createClusters(String city) throws Exception {

		// generate popular products
		popularProdSet = initializePopularProductSet();
		if (popularProdSet.size() == 0) {
			logger.error("Popular products is zero. Stopping now");
			return;
		}
		logger.info("Popular items reading completed. Total number of popular products are " + popularProdSet.size());

		esConfig = com.olastore.listing.clustering.utils.Util.setListingIndexNameForCity(esConfig, "listing_index_name",
				city);
		esConfig = com.olastore.listing.clustering.utils.Util.setClusterIndexes(esConfig, "geo_hash_index_name",
				"clusters_index_name");
		// reinitializeClusteringIndices();

		GeoHashUtil geoHashUtil = new GeoHashUtil();
		List<String> geoHashList = geoHashUtil.getGeoHashesForArea(city, this.clustersConfig);

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (String geoHash : geoHashList) {
			executorService.submit(new ClusterWorker(geoHash, esConfig, clustersConfig));
		}
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.DAYS);
		if (!bulkDoc.toString().isEmpty()) {
			JSONObject result = esClient.pushToESBulk("", "", bulkDoc.toString());
			logger.info("Response from ES for  is " + result);
		}
	}

}
