package com.olastore.listing.clustering.clients;

import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;


public class ESClient {

	ClustersHttpClientFactory clustersHttpClientFactory;
	ClustersESURIBuilder clustersESURIBuilder;

	public ESClient(String esHost) {
		clustersHttpClientFactory = new ClustersHttpClientFactory();
		clustersESURIBuilder = new ClustersESURIBuilder(esHost);
	}

	public static Logger LOG = Logger.getLogger(ESClient.class);

	public JSONObject searchES(String indexName, String indexType, String query) throws URISyntaxException {
		StringEntity stringEntity = new StringEntity(query, Charset.defaultCharset());
		HashMap<String, String> map = new HashMap<>();
		if (!indexName.contentEquals("")) {
			map.put("index_name", indexName);
			if (!indexType.contentEquals(""))
				map.put("index_type", indexType);
		}
		URI searchUri = clustersESURIBuilder.getESSearchEndPoint(map);
		return clustersHttpClientFactory.getHttpClient().executePost(searchUri, stringEntity);
	}

	public JSONObject getESDoc(String indexName, String indexType, String docID) throws URISyntaxException {
		HashMap<String, String> map = new HashMap<>();
		map.put("index_name", indexName);
		map.put("index_type", indexType);
		map.put("id", docID);
		URI docUri = clustersESURIBuilder.getDocEndPoint(map);
		return clustersHttpClientFactory.getHttpClient().executeGet(docUri);
	}

	public JSONObject pushToES(String indexName, String indexType, String id, String data) throws URISyntaxException {
		StringEntity entity = new StringEntity(data, Charset.defaultCharset());
		HashMap<String, String> map = new HashMap<>();
		map.put("index_name", indexName);
		map.put("index_type", indexType);
		map.put("id", id);
		URI docUri = clustersESURIBuilder.getDocEndPoint(map);
		return clustersHttpClientFactory.getHttpClient().executePost(docUri, entity);
	}

	public JSONObject pushToESBulk(String indexName, String indexType, String data) throws URISyntaxException {
		StringEntity entity = new StringEntity(data, Charset.defaultCharset());
		HashMap<String, String> map = new HashMap<>();
		if (!indexName.contentEquals("")) {
			map.put("index_name", indexName);
			if (!indexType.contentEquals(""))
				map.put("index_type", indexType);
		}
		URI bulkURI = clustersESURIBuilder.getBulkEndPoint(map);
		return clustersHttpClientFactory.getHttpClient().executePost(bulkURI, entity);

	}

	public JSONObject createIndex(String indexName, FileEntity fileEntity) throws URISyntaxException {
		URI indexUri = clustersESURIBuilder.getIndexURI(indexName);
		return clustersHttpClientFactory.getHttpClient().executePut(indexUri, fileEntity);
	}

	public void deleteIndex(String indexName) throws URISyntaxException {
		URI indexUri = clustersESURIBuilder.getIndexURI(indexName);
		clustersHttpClientFactory.getHttpClient().executeDelete(indexUri);
	}

}
