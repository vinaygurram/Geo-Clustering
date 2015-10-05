package com.olastore.listing.clustering;

import com.olastore.listing.clustering.Util.ConfigReader;
import com.olastore.listing.clustering.algorithms.GeoClustering;
import com.olastore.listing.clustering.clients.ESClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by gurramvinay on 10/5/15.
 */
public class Main {

  public static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    try {
      long time_start =System.currentTimeMillis();

      ConfigReader configReader = new ConfigReader();
      Map esConfig = configReader.readConfig("src/main/resources/config/es.yaml");
      Map clustersConfig = configReader.readConfig("src/main/resources/config/clusters.yaml");

      GeoClustering geoClustering = new GeoClustering(args[0],esConfig,clustersConfig);
      geoClustering.createClusters(args[1]);

      long time_end = System.currentTimeMillis();
      logger.info("Total time took to complete clusters is "+ (time_end-time_start) + "ms");

    }catch ( Exception e){
      logger.error("Exception happen!",e);
    }
  }

}
