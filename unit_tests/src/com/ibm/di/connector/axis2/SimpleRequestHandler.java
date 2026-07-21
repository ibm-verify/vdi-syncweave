package com.ibm.di.connector.axis2;

import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;

public class SimpleRequestHandler implements ServerModeConnectorTestDriver.RequestHandler {

	private volatile Entry requestEntry = null;
	private Entry responseEntry = null;

	public SimpleRequestHandler(Entry responseEntry) {
		this.responseEntry = responseEntry;
	}

	public void handleRequest(ConnectorInterface iteratorConnector) throws Exception {
		requestEntry = iteratorConnector.getNextEntry();
		iteratorConnector.replyEntry(responseEntry);
	}

	public Entry getRequestEntry() {
		return requestEntry;
	}
}
