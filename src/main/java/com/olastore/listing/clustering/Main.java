package com.olastore.listing.clustering;

import com.olastore.listing.clustering.utils.ConfigReader;
import com.olastore.listing.clustering.algorithms.ClusterBuilder;
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
      long time_start = System.currentTimeMillis();

      if(args[0] == null || args[0].isEmpty()){
        logger.error("Please provide valid environment variable");
        return;
      }

      if(args[1] == null || args[1].isEmpty()){
        logger.error("Please provide valid city name");
        return;
      }

      ConfigReader configReader = new ConfigReader();
      Map esConfig = configReader.readConfig("src/main/resources/config/es.yaml");
      Map clustersConfig = configReader.readConfig("src/main/resources/config/clusters.yaml");

      ClusterBuilder clusterBuilder = new ClusterBuilder(args[0],esConfig,clustersConfig);
      clusterBuilder.createClusters(args[1]);

      long time_end = System.currentTimeMillis();
      logger.info("Total time took to complete clusters is "+ (time_end-time_start) + "ms");

    }catch ( Exception e){
      logger.error("Exception happen!",e);
    }
  }

}
