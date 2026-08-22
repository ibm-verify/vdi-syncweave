package com.ibm.di.test.utils.func.tp;

import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.ibm.di.web.common.atom.AtomEntry;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Element;

import com.ibm.di.test.http.HttpClientContext;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.utils.atom.AtomUtils;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.config.ObjectFactory;
import com.ibm.di.tp.server.model.config.StatusData;
import com.ibm.di.tp.server.util.SCMPUtils;

public class ProviderTouchpoint extends Touchpoint {

	/**
	 * Connect and read timeout.
	 */
	private static final int HTTP_TIMEOUT_MILLIS = 60000;

	private static final String HTTP_SIZELIMIT_HEADER = "X-TDI-TP-SizeLimit";

	private final CloseableHttpClient httpClient;

	private boolean reqInAvailable = true;
	private final URL reqIn;

	public ProviderTouchpoint(HttpClientContext ctx, String instanceEntryUrl) throws Exception {
		super(ctx, instanceEntryUrl);

		reqIn = findRequestInUri();
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(HTTP_TIMEOUT_MILLIS)
				.setSocketTimeout(HTTP_TIMEOUT_MILLIS)
				.build();
		
		this.httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private URL findRequestInUri() throws Exception {
		String res = app.getResourceUrlFromEntry(getInstanceEntry(), Constants.REL_STATUS);
		MockHttpServletResponse resp = ctx.invoke(ctx.constructMockRequest(javax.ws.rs.HttpMethod.GET, res, MediaType.WILDCARD));
		TpAppHelper.checkSuccess(resp);

		AtomEntry statusEntry = AtomUtils.deserializeEntry(resp.getContentAsString());

		Element dataElem = SCMPUtils.getDataElement(statusEntry.getAny());
		StatusData data = ObjectFactory.createStatusData(dataElem);

		return (reqInAvailable = data.getTouchpointStatus().getRequestIn() != null) ? new URL(data.getTouchpointStatus()
				.getRequestIn()) : null;
	}

	public URL getRequestInUrl() throws Exception {
		if (!reqInAvailable) {
			throw new UnsupportedOperationException();
		}

		return reqIn;
	}

	public ProviderTouchpointResponse get() throws Exception {
		return doGet(reqIn);
	}

	public ProviderTouchpointResponse get(int sizeLimit) throws Exception {
		return doGet(reqIn, HTTP_SIZELIMIT_HEADER + "=" + sizeLimit);
	}

	public ProviderTouchpointResponse get(String query) throws Exception {
		return doGet(getQueryUrl(query));
	}

	public ProviderTouchpointResponse get(String query, int sizeLimit) throws Exception {
		return doGet(getQueryUrl(query), HTTP_SIZELIMIT_HEADER + "=" + sizeLimit);
	}

	public ProviderTouchpointResponse post(TouchpointData data) throws Exception {
		HttpPost post = new HttpPost(reqIn.toString());
		post.setEntity(new ByteArrayEntity(data.getXML().getBytes("UTF-8")));
		return doHttp(post);
	}

	public ProviderTouchpointResponse put(String query, TouchpointData data) throws Exception {
		HttpPut put = new HttpPut(getQueryUrl(query).toString());
		put.setEntity(new ByteArrayEntity(data.getXML().getBytes("UTF-8")));
		return doHttp(put);
	}

	public ProviderTouchpointResponse delete(String query) throws Exception {
		HttpDelete delete = new HttpDelete(getQueryUrl(query).toString());
		return doHttp(delete);
	}

	private URL getQueryUrl(String query) throws Exception {
		if (!query.startsWith("?")) {
			query = "?" + query;
		}
		return new URL(reqIn.toString() + query);
	}

	private ProviderTouchpointResponse doGet(URL url) throws Exception {
		return doGet(url, "");
	}

	private ProviderTouchpointResponse doGet(URL url, String headers) throws Exception {

		HttpGet get = new HttpGet(url.toString());

		// headers are specified in java.util.Properties format
		Properties props = new Properties();
		props.load(new StringReader(headers));
		for (String p : props.stringPropertyNames()) {
			get.setHeader(p, props.getProperty(p));
		}

		return doHttp(get);
	}

	private ProviderTouchpointResponse doHttp(HttpRequestBase method) throws Exception {
		method.setHeader("Connection", "keep-alive");
		CloseableHttpResponse response = httpClient.execute(method);
		try {
			int responseCode = response.getStatusLine().getStatusCode();
			byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());
			return new ProviderTouchpointResponse(responseCode, responseBytes);
		} finally {
			response.close();
		}
	}

}
