package com.olastore.listing.clustering.utils;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

/**
 * Created by gurramvinay on 10/5/15.
 */
public class ConfigReader {

	private Map configValues;

	public ConfigReader(String configFilePath) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		this.configValues = (Map) yaml.load(this.getClass().getClassLoader().getResourceAsStream(configFilePath));
	}

	public File readFile(String path) throws IOException {

		File file = new File("tmpFile");
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream(path), file);
		return file;
	}

	public Object readValue(String key) {
		return configValues.get(key);
	}

	public Map readAllValues() {
		return configValues;
	}
}
