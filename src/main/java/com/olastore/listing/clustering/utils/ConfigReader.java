package com.olastore.listing.clustering.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by gurramvinay on 10/5/15.
 */
public class ConfigReader {

  public Map readConfig(String configFilePath) throws FileNotFoundException {
    Yaml yaml = new Yaml();
    return  (Map) yaml.load(new FileInputStream(new File(configFilePath)));
  }
}
