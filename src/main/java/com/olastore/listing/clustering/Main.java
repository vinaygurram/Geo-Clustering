package com.olastore.listing.clustering;

import com.olastore.listing.clustering.utils.ConfigReader;
import com.olastore.listing.clustering.algorithms.ClusterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main {

	public static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			long time_start = System.currentTimeMillis();

			if (args[0] == null || args[0].isEmpty()) {
				logger.error("Please provide valid environment variable");
				return;
			}

			if (args[1] == null || args[1].isEmpty()) {
				logger.error("Please provide valid city name");
				return;
			}

			ConfigReader esConfigReader = new ConfigReader("config/es.yaml");
			ConfigReader clustersConfigReader = new ConfigReader("config/clusters.yaml");

			ClusterBuilder clusterBuilder = new ClusterBuilder(args[0], esConfigReader, clustersConfigReader);
			clusterBuilder.createClusters(args[1].split(","));

			long time_end = System.currentTimeMillis();
			logger.info("Total time took to complete clusters is " + (time_end - time_start) + "ms");

		} catch (Exception e) {
			logger.error("Exception happen!", e);
		}
	}

}
