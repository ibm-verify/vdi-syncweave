package com.ibm.di.test.utils.func;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.entry.Entry;

public class HttpReceiverTest {

	private static final int HTTP_PORT = 9999;

	private static final String HTTP_URL = "http://localhost:" + HTTP_PORT;
	
	private static int HTTP_CALL_TIMEOUT_MILLIS = 5000;

	private HttpReceiver receiver = null;
	private CloseableHttpClient httpClient = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		receiver = new HttpReceiver(HTTP_PORT);
		httpClient = createHttpClient();
	}

	@After
	public void tearDown() throws Exception {
		receiver.close();
		receiver = null;
		httpClient = null;
	}

	@Test
	public void test_http_get() throws Exception {
		HttpGet get = new HttpGet(HTTP_URL);
		CloseableHttpResponse response = httpClient.execute(get);
		response.close();
		
		Entry request = receiver.receive(HTTP_CALL_TIMEOUT_MILLIS);
		
		assertEquals("GET", request.getString("http.method"));
		assertNull(request.getString("http.body"));
	}
	
	@Test
	public void test_http_post() throws Exception {
		
		final String requestContent = "<person>john</person>";
		
		HttpPost post = new HttpPost(HTTP_URL);
		post.setEntity(new ByteArrayEntity(requestContent.getBytes("UTF-8")));

		CloseableHttpResponse response = httpClient.execute(post);
		response.close();
		
		Entry request = receiver.receive(HTTP_CALL_TIMEOUT_MILLIS);
		
		assertEquals("POST", request.getString("http.method"));
		assertEquals(requestContent, request.getString("http.body"));
	}
	
	private CloseableHttpClient createHttpClient() {
		final int timeout = 60000;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		return HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();
	}

}
