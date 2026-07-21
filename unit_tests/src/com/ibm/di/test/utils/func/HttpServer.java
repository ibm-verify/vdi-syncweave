package com.ibm.di.test.utils.func;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.HTTPServerConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.test.utils.RSMock;
import com.ibm.di.test.utils.ServerModeConnectorDriver;

public class HttpServer {

	/**
	 * Creates HTTP request handler per TCP connection. Must be thread-safe.
	 * Each of the created handlers will be used on a different thread.
	 */
	public static interface HttpRequestHandlerFactory {
		HttpRequestHandler createHandler();
	}

	/**
	 * Handle HTTP requests for one TCP connection. If you set the "connection"
	 * header to "close" in the response, the server will close the TCP
	 * connection. If you throw it will not stop the server but will abandon the
	 * TCP connection.
	 */
	public static interface HttpRequestHandler {
		Entry handleRequest(Entry request) throws Exception;
	}

	private final ServerModeConnectorDriver httpConnectorDriver;

	public HttpServer(int tcpPort, boolean useSSL, HttpRequestHandlerFactory requestHandlerFactory) throws Exception {

		ConnectorInterface conn = new HTTPServerConnector();
		ConnectorConfig cc = new ConnectorConfigImpl();
		cc.init();
		cc.setState(ConnectorConfig.ENABLED_STATE);
		cc.setMode(ConnectorConfig.SERVER_MODE);
		cc.getConnectionConfig().setJavaClass(HTTPServerConnector.class.getName());
		cc.getConnectionConfig().setParameter("tcpPort", "" + tcpPort);
		cc.getConnectionConfig().setParameter("useSSL", "" + useSSL);
		conn.setConfiguration(cc);
		conn.setLog(new Log(""));
		conn.setRSInterface(new RSMock());

		ServerModeConnectorDriver.ClientHandler clientHandler = new HttpClientHandler(requestHandlerFactory);

		httpConnectorDriver = new ServerModeConnectorDriver(conn, clientHandler);
	}

	public void close() {
		httpConnectorDriver.close();
	}

	private static class HttpClientHandler implements ServerModeConnectorDriver.ClientHandler {

		private final HttpRequestHandlerFactory requestHandlerFactory;

		public HttpClientHandler(HttpRequestHandlerFactory requestHandlerFactory) {
			this.requestHandlerFactory = requestHandlerFactory;
		}

		public void handleClient(ConnectorInterface iteratorConnector) throws Exception {
			HttpRequestHandler requestHandler = requestHandlerFactory.createHandler();
			Entry request = iteratorConnector.getNextEntry();
			while (request != null) {

				Entry response = requestHandler.handleRequest(request);
				iteratorConnector.replyEntry(response);

				request = iteratorConnector.getNextEntry();
			}
		}
	}

}
