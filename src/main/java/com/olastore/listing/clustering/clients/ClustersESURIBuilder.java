package com.olastore.listing.clustering.clients;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * @author gurramvinay
 */
public class ClustersESURIBuilder {

	public static final String INDEX_NAME = "index_name";
	public static final String INDEX_TYPE = "index_type";
	private String ES_HOST;
	private String schme = "http";

	private enum endPoints {
		SEARCH("_search"), BULK("_bulk"),ALIASES("_aliases");
		protected final String name;

		private endPoints(String name) {
			this.name = name;
		}

		public boolean equalsName(String otherName) {
			return (otherName != null) && name.equals(otherName);
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	protected ClustersESURIBuilder(String esHOST) {
		this.ES_HOST = esHOST;
	}

	protected ClustersESURIBuilder(String esHOST, String schme) {
		this.ES_HOST = esHOST;
		this.schme = schme;
	}

	public URI getESSearchEndPoint(HashMap<String, String> searchLevels) throws URISyntaxException {
		StringBuilder path = new StringBuilder();
		if (searchLevels.containsKey(INDEX_NAME)) {
			path.append("/").append(searchLevels.get(INDEX_NAME));
		}
		if (searchLevels.containsKey(INDEX_TYPE)) {
			path.append("/").append(searchLevels.get(INDEX_TYPE));
		}
		path.append("/").append(endPoints.SEARCH);
		return new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();
	}

	public URI getBulkEndPoint(HashMap<String, String> bulkLevels) throws URISyntaxException {
		StringBuilder path = new StringBuilder();
		if (bulkLevels.containsKey(INDEX_NAME)) {
			path.append("/").append(bulkLevels.get(INDEX_NAME));
		}
		if (bulkLevels.containsKey(INDEX_TYPE)) {
			path.append("/").append(bulkLevels.get(INDEX_TYPE));
		}
		path.append("/").append(endPoints.BULK);
		return new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();
	}

	public URI getBulkEndPoint() throws URISyntaxException {
		return getBulkEndPoint(new HashMap<String, String>());
	}

	public URI getDocEndPoint(HashMap<String, String> docMap) throws URISyntaxException {
		StringBuilder path = new StringBuilder();
		path.append("/").append(docMap.get(INDEX_NAME));
		path.append("/").append(docMap.get(INDEX_TYPE));
		path.append("/").append(docMap.get("id"));
		return new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();

	}

	public URI getIndexURI(String indexName) throws URISyntaxException {
		return new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath("/" + indexName).build();
	}

	public URI getAliasesEndPoint() throws URISyntaxException {
		StringBuilder path = new StringBuilder();
		path.append("/").append(endPoints.ALIASES);
		return new URIBuilder().setScheme(schme).setHost(ES_HOST).setPath(path.toString()).build();
	}
}
