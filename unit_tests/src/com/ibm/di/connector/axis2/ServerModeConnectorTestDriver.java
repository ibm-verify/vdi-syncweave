package com.ibm.di.connector.axis2;

import com.ibm.di.connector.ConnectorInterface;

/*
 * Test driver for a Server mode Connector. Accepts a single client and terminates.
 * The request itself is handled by the specified handler.
 */
public class ServerModeConnectorTestDriver implements Runnable {

	interface RequestHandler {
		void handleRequest(ConnectorInterface iteratorConnector) throws Exception;
	}

	private ConnectorInterface serverConnector;
	private RequestHandler requestHandler;
	private volatile Exception error = null;

	public ServerModeConnectorTestDriver(ConnectorInterface serverConnector, RequestHandler requestHandler) {
		this.serverConnector = serverConnector;
		this.requestHandler = requestHandler;
	}
	
	public void initialize() throws Exception {
		serverConnector.initialize(null);
	}

	public void run() {
		try {
			ConnectorInterface iteratorConnector = serverConnector.getNextClient();
			requestHandler.handleRequest(iteratorConnector);
		} catch (Exception ex) {
			error = ex;
		}
	}
	
	public void close() throws Exception {
		serverConnector.terminateServer();
		serverConnector.terminate();
	}

	public Exception getExitError() {
		return error;
	}
}
