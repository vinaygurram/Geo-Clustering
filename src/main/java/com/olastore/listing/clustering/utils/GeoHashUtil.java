package com.olastore.listing.clustering.utils;

import com.github.davidmoten.geo.Coverage;
import com.github.davidmoten.geo.GeoHash;
import com.olastore.listing.clustering.algorithms.ClusterBuilder;
import com.olastore.listing.clustering.lib.models.BoundingBox;
import com.olastore.listing.clustering.lib.models.Geopoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by gurramvinay on 10/5/15.
 */
public class GeoHashUtil {
	private static final Logger logger = LoggerFactory.getLogger(GeoHashUtil.class);

	private Set<String> getGeoHashOfBoundingBox(BoundingBox box, int precision) {
		Coverage boxCoverage = GeoHash.coverBoundingBox(box.getTopLeft().getLatitude(), box.getTopLeft().getLongitude(),
				box.getBotRight().getLatitude(), box.getBotRight().getLongitude(), precision);
		return boxCoverage.getHashes();
	}

	private BoundingBox getBBox(String city, Map clustersConfig) {
		Geopoint topleft = new Geopoint((Double) ((HashMap) clustersConfig.get(city + "_bbox_top_left")).get("lat"),
				(Double) ((HashMap) clustersConfig.get(city + "_bbox_top_left")).get("lon"));
		Geopoint botright = new Geopoint((Double) ((HashMap) clustersConfig.get(city + "_bbox_bot_right")).get("lat"),
				(Double) ((HashMap) clustersConfig.get(city + "_bbox_bot_right")).get("lon"));
		return new BoundingBox(topleft, botright);
	}

	public List<String> getGeoHashesForArea(String city, Map clustersConfig) {
		BoundingBox bbox = getBBox(city, clustersConfig);
		Set<String> hashes = getGeoHashOfBoundingBox(bbox, (Integer) clustersConfig.get("clusters_geo_precision"));
		Iterator<String> iterator = hashes.iterator();
		List<String> geohashList = new ArrayList<>();
		while (iterator.hasNext()) {
			String thisHash = iterator.next();
			geohashList.add(thisHash);
		}
		logger.info("Total number of hashes ", geohashList.size());
		return geohashList;
	}
}
