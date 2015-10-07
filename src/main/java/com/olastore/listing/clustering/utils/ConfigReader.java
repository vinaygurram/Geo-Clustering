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

  private Map configValues;

  public ConfigReader(String configFilePath) throws FileNotFoundException {
    Yaml yaml = new Yaml();
    this.configValues = (Map) yaml.load(new FileInputStream(new File(configFilePath)));
  }

  public Object readValue(String key) {
    return configValues.get(key);
  }

  public Map readAllValues() {
    return configValues;
  }
}
