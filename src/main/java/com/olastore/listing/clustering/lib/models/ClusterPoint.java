package com.olastore.listing.clustering.lib.models;

/**
 * Generic Object used to from the cluster. Independent of other schema. 
 */
public class ClusterPoint {

	private Geopoint location;
	private String id;
	private boolean isFnv = false;

	public ClusterPoint(String id, Geopoint location) {
		this.id = id;
		this.location = location;
	}

	public Geopoint getLocation() {
		return location;
	}

	public void setLocation(Geopoint location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {

		this.id = id;
	}

	public boolean isFnv() {
		return isFnv;
	}

	public void setIsFnv(boolean isFnv) {
		this.isFnv = isFnv;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(id).append(" ").append(location).append(";;;").toString();
	}
}
