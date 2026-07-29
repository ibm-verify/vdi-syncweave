/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * The URL Connector is a transport Connector that requires a Parser to operate.
 * The Connector opens a stream specified by a URL. <b>Note:</b> When forced
 * through a firewall that enforces a proxy server, the URL Connector does not
 * work. The URL Connector needs to have the right proxy server set. This
 * Connector supports AddOnly and Iterator modes. <br>
 * The Connector, in principle, can handle secure communications using the SSL
 * protocol, but it may require driver-specific configuration steps in order to
 * set up SSL support.
 */
public class URLConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the properties file
	 */
	private static final String PROPERTIES_FILE = "urlconnector";

	/**
	 * URL
	 */
	private URL url;

	/**
	 * connection object
	 */
	private URLConnection conn;

	/**
	 * name of the component
	 */
	private static final String myName = "URL Connector";

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class constructor
	 */
	public URLConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE, });
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		url = null;
		conn = null;
		super.terminate();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object o) throws Exception {
		Reader is = null;
		Writer os = null;

		String strURL = getParam("url");
		if (strURL == null) {
			strURL = getParam("puturl");
		}

		if (strURL == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.URL.MISSING.EXCEP"));
		}

		url = new URL(strURL);
		conn = url.openConnection();

		conn.setDoInput(true);
		conn.setDoOutput(true);

		// NOTE: these two lines are extremely important to be in this order
		// if you change their order this will cause the connector to break

		os = new OutputStreamWriter(conn.getOutputStream());
		is = new InputStreamReader(conn.getInputStream());

		initParser(is, os);

	}

	/**
	 * default implementation
	 * 
	 * @throws Exception
	 *             never
	 * 
	 */
	public void selectEntries() throws Exception {
	}

	/**
	 * reads next entry using the provided parser
	 * 
	 * @return the next entry
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry getNextEntry() throws Exception {
		return getParser().readEntry();
	}

	/**
	 * writes next entry using the provided parser
	 * 
	 * @param entry
	 *            the entry to be written
	 * @throws Exception
	 *             if an error occurs
	 */
	public void putEntry(Entry entry) throws Exception {
		getParser().writeEntry(entry);
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I% 20%E%";
	}

}
