
package com.ibm.di.test.http;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.http.Header;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ibm.di.test.http.HttpClientContext;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class FuncTestHttpClientContext extends HttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private CloseableHttpClient client;

	private String httpRootUri;
	
	private CredentialsProvider credentialsProvider;

	public FuncTestHttpClientContext() {
		this.client = HttpClients.createDefault();
	}
	
	/**
	 * Set credentials provider for HTTP authentication.
	 * This will recreate the HTTP client with the new credentials.
	 *
	 * @param credentialsProvider the credentials provider to use
	 */
	public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
		// Recreate client with credentials
		if (credentialsProvider != null) {
			this.client = HttpClientBuilder.create()
					.setDefaultCredentialsProvider(credentialsProvider)
					.build();
		} else {
			this.client = HttpClients.createDefault();
		}
	}
	
	/**
	 * Get the current credentials provider.
	 *
	 * @return the credentials provider or null if not set
	 */
	public CredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.test.tp.TPClientContext#getHttpRootURI()
	 */
	@Override
	public String getHttpRootURI() {
		return httpRootUri;
	}

	/**
	 * @param httpRootUri
	 *            the httpRootUri to set
	 */
	public void setHttpRootUri(String httpRootUri) {
		this.httpRootUri = httpRootUri;
	}

	/**
	 * @return the client
	 */
	public CloseableHttpClient getClient() {
		return client;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.test.tp.TPClientContext#invoke(org.springframework.mock.web
	 * .MockHttpServletRequest)
	 */
	@Override
	public MockHttpServletResponse invoke(MockHttpServletRequest request) throws Exception {
		HttpRequestBase method = toHttpMethod(request);
		CloseableHttpResponse response = client.execute(method);
		try {
			return fromHttpMethod(response);
		} finally {
			response.close();
		}
	}

	/**
	 * @param request
	 * @return
	 */
	private static HttpRequestBase toHttpMethod(MockHttpServletRequest req) {
		HttpRequestBase method = createHttpMethod(req);
		setRequestHeaders(req, method);
		return method;
	}

	private static void setRequestHeaders(MockHttpServletRequest from, HttpRequestBase to) {
		Enumeration<?> hNames = from.getHeaderNames();
		String hName = null;
		while (hNames.hasMoreElements()) {
			hName = (String) hNames.nextElement();
			to.setHeader(hName, from.getHeader(hName));
		}
	}

	/**
	 * @param req
	 * @return
	 */
	private static HttpRequestBase createHttpMethod(MockHttpServletRequest req) {
		HttpRequestBase method = null;

		if (javax.ws.rs.HttpMethod.GET.equals(req.getMethod())) {
			method = new HttpGet(req.getRequestURI());
		} else if (javax.ws.rs.HttpMethod.POST.equals(req.getMethod())) {
			HttpPost post = new HttpPost(req.getRequestURI());
			InputStreamEntity entity = new InputStreamEntity(req.getInputStream(), req.getContentLength());
			if (req.getContentType() != null) {
				entity.setContentType(req.getContentType());
			}
			post.setEntity(entity);
			method = post;
		} else if (javax.ws.rs.HttpMethod.PUT.equals(req.getMethod())) {
			HttpPut put = new HttpPut(req.getRequestURI());
			InputStreamEntity entity = new InputStreamEntity(req.getInputStream(), req.getContentLength());
			if (req.getContentType() != null) {
				entity.setContentType(req.getContentType());
			}
			put.setEntity(entity);
			method = put;
		} else if (javax.ws.rs.HttpMethod.DELETE.equals(req.getMethod())) {
			method = new HttpDelete(req.getRequestURI());
		} else if (javax.ws.rs.HttpMethod.HEAD.equals(req.getMethod())) {
			method = new HttpHead(req.getRequestURI());
		} else {
			throw new IllegalArgumentException("Unexpected HTTP Method!");
		}

		return method;
	}

	/**
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private static MockHttpServletResponse fromHttpMethod(CloseableHttpResponse response) throws IOException {
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		mockResponse.setStatus(response.getStatusLine().getStatusCode());
		if (response.getEntity() != null) {
			byte[] content = EntityUtils.toByteArray(response.getEntity());
			mockResponse.getOutputStream().write(content);
		}
		setResponseHeaders(response, mockResponse);

		return mockResponse;
	}

	private static void setResponseHeaders(CloseableHttpResponse from, MockHttpServletResponse to) {
		for (Header h : from.getAllHeaders()) {
			to.setHeader(h.getName(), h.getValue());
		}
	}

}
