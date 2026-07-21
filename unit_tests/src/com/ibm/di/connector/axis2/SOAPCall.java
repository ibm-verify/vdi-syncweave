package com.ibm.di.connector.axis2;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SOAPCall {
	
	private String body = null;
	private int responseCode = 0;
	
	public static SOAPCall call(String httpURL, String requestBody, String requestContentType) throws Exception {
		Map<String, String> requestHeaders = new HashMap<String, String> ();
		requestHeaders.put("content-type", requestContentType);
		return new SOAPCall(httpURL, requestBody, requestHeaders, null, null);
	}
	
	public static SOAPCall call(String httpURL, String requestBody, String requestContentType, String username, String password) throws Exception {
		Map<String, String> requestHeaders = new HashMap<String, String> ();
		requestHeaders.put("content-type", requestContentType);
		return new SOAPCall(httpURL, requestBody, requestHeaders, username, password);
	}
	
	public SOAPCall(String httpURL, String requestBody, Map<String, String> requestHeaders, String username, String password) throws Exception {
		getHTTPResponse_ApacheHttpClient31(httpURL, requestBody, requestHeaders, username, password);
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public String getBody() {
		return body;
	}
	
	/*
	 * Migrated to Apache HttpClient 4.x
	 */
	private void getHTTPResponse_ApacheHttpClient31(String httpURL, String requestBody, Map<String, String> requestHeaders, String username, String password) throws Exception {
		
		final int timeoutMillis = 60000;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeoutMillis)
				.setSocketTimeout(timeoutMillis)
				.build();
		
		CloseableHttpClient httpClient;
		if (username != null) {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					AuthScope.ANY,
					new UsernamePasswordCredentials(username, password));
			httpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.setDefaultRequestConfig(requestConfig)
					.build();
		} else {
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.build();
		}
		
		try {
			HttpPost post = new HttpPost(httpURL);
			for (Map.Entry<String, String> e : requestHeaders.entrySet()) {
				post.addHeader(e.getKey(), e.getValue());
			}
			post.setEntity(new ByteArrayEntity(requestBody.getBytes("UTF-8")));
			
			CloseableHttpResponse response = httpClient.execute(post);
			try {
				responseCode = response.getStatusLine().getStatusCode();
				body = EntityUtils.toString(response.getEntity(), "UTF-8");
			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
	}
	
}
