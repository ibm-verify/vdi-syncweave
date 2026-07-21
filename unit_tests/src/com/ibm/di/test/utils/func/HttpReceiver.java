package com.ibm.di.test.utils.func;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.ibm.di.entry.Entry;

/**
 * <p>
 * A very simple HTTP server which stores incoming requests and answers each the
 * same way.
 * </p>
 * 
 * <b>Example:</b>
 * 
 * <pre>
 * HttpReceiver receiver = new HttpReceiver(80);
 * 
 * Entry request = receiver.receive();
 * 
 * System.out.println(&quot;request method: &quot; + request.getString(&quot;http.method&quot;));
 * System.out.println(&quot;request content: &quot; + request.getString(&quot;http.body&quot;));
 * 
 * receiver.close();
 * </pre>
 */
public class HttpReceiver {
	
	private HttpServer httpServer;
	private BlockingQueue<Entry> requests = new LinkedBlockingQueue<Entry>();

	public HttpReceiver(int port) throws Exception {
		final boolean useSSL = false;
		this.httpServer = new HttpServer(port, useSSL, new HttpRequestHandlerFactoryImpl(requests));
	}

	public Entry receive() throws Exception {
		return requests.take();
	}

	public Entry receive(long milliseconds) throws Exception {
		return requests.poll(milliseconds, TimeUnit.MILLISECONDS);
	}
	
	public void close() {
		httpServer.close();
	}
	
	private static class HttpRequestHandlerFactoryImpl implements HttpServer.HttpRequestHandlerFactory {
		
		private BlockingQueue<Entry> requests;
		
		public HttpRequestHandlerFactoryImpl(BlockingQueue<Entry> requests) {
			this.requests = requests;
		}
		
		public HttpServer.HttpRequestHandler createHandler() {
			return new HttpRequestHandlerImpl(requests);
		}
	}
	
	private static class HttpRequestHandlerImpl implements HttpServer.HttpRequestHandler {
		
		private final BlockingQueue<Entry> requests;
		
		public HttpRequestHandlerImpl(BlockingQueue<Entry> requests) {
			this.requests = requests;
		}

		public Entry handleRequest(Entry request) throws Exception {
			requests.put(request);
			Entry response = new Entry();
			response.setAttribute("http.status", "200 OK");
			return response;
		}
	}
	
}
