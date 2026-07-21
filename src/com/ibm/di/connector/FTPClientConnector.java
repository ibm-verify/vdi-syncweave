/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.net.Socket;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.protocols.FTPClient;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * The FTP Client Connector is a transport Connector that requires a Parser to
 * operate. The Connector reads or writes a data stream that can either be a
 * file or a directory listing. Think of the FTP Client Connector as a remote
 * read/write facility, not something you use to transfer files.
 *
 * This Connector supports FTP Passive Mode, as per RFC959. Passive Mode
 * reverses who initiates the data connection in a file transfer. Normally the
 * server initiates a data connection to the client (after a command from the
 * client), whereas passive mode enables the client to initiate the data
 * connection. This makes it easier to transfer files when the client is behind
 * a firewall.
 *
 */
public class FTPClientConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file
	 */
	private static final String PROPERTIES_FILE = "ftpclientconnector";

	/**
	 * The local instance of the ftp connection that is used to read and write
	 * data from the remote host
	 */
	private FTPClient ftp;

	/**
	 * ASCII or binary image file transfer type
	 */
	private boolean binary;

	/**
	 * socket object for I/O transfer
	 */
	private Socket socket;

	/**
	 * Name of the component
	 */
	private static final String myName = "FTP Connector";

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static final ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * True if we are reading from the socket and have not seen any End of Data indicator
	 */
	private boolean readingAndNotSeenEOD;

	/**
	 * Default constructor
	 */
	public FTPClientConnector() {
		Trace.entrymid(this, "FTPClientConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE });
		Trace.exitmid(this, "FTPClientConnector");
	}

	/**
	 * Closes connection to remote host.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void terminate() throws Exception {
		Trace.entrymin(this, "terminate");
		// Must close in/out data streams before closing connection to FTP
		// server
		logmsg(sResHash.getString("CONNECTOR.FTPCLIENT.TERMINATING", getName()));

		Exception err = null;

		try {
			ftp.checkComplete();
		} catch (Exception b) {
			err = b;
		}

		super.terminate();

		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (Exception ignore) {
				logmsg(sResHash.getString(
						"CONNECTOR.FTPCLIENT.SOCKETCLOSE.WARNING", ignore
								.toString()));
			}
		}

		if (ftp != null) {
			ftp.disconnect();
			ftp = null;
		}

		Trace.exitmin(this, "terminate");

		if (err != null && ! readingAndNotSeenEOD)
			throw err;

	}

	/**
	 * Creates connection for the login to the remote host and opens the socket.
	 *
	 * @param o
	 *            ignored
	 * @throws Exception
	 *             if an error occurs.
	 *
	 */
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);
		String ftpHost = getParam("ftpServer");
		String ftpPort = getParam("ftpPort");
		String ftpUser = getParam("ftpUser");
		String ftpPass = getParam("ftpPass");
		String ftpMode = getParam("ftpTransferMode");
		String ftpSecurity = getParam("ftpSecurity");
		Boolean ftpExplicitModeSSL = getBoolean("ftpExplicitModeSSL");
		if(ftpExplicitModeSSL == null)
			ftpExplicitModeSSL = Boolean.FALSE;

		boolean useSSLonControlChannel;
		boolean useSSLonDataChannel;
		if (ftpSecurity.equals("Use SSL on control channel")) {
			useSSLonControlChannel = true;
			useSSLonDataChannel = false;
		} else if (ftpSecurity.equals("Use SSL on control and data channels")) {
			useSSLonControlChannel = true;
			useSSLonDataChannel = true;
		} else {
			useSSLonControlChannel = false;
			useSSLonDataChannel = false;
		}

		if (ftpMode == null || ftpMode.equalsIgnoreCase("ASCII"))
			binary = false;
		else
			binary = true;

		if (ftpHost == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.FTPCLIENT.CONNECTIONOPEN.INFO"));
		}

		ftp = new FTPClient(getLog());

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.FTPCLIENT.CONNECTIONOPEN.INFO",
					ftpHost + ":" + (ftpPort == null ? "21" : ftpPort)));
		}

		if (debugMode())
			ftp.setDebug(true);

		if (ftpPort != null) {
			ftp.connect(ftpHost, Integer.parseInt(ftpPort),
					useSSLonControlChannel, useSSLonDataChannel, ftpExplicitModeSSL);
		} else {
			int defaultPort = 21;
			if (useSSLonControlChannel) {
				defaultPort = 990;
			}
			ftp.connect(ftpHost, defaultPort, useSSLonControlChannel,
					useSSLonDataChannel, ftpExplicitModeSSL);
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.FTPCLIENT.CONNECTIONUSER.INFO",
					ftpUser));
		}

		ftp.login(ftpUser, ftpPass);

		ftp.setUsePassive(Boolean.valueOf(getParam("ftpPassive"))
				.booleanValue());

		String oper = getParam("ftpOperation");
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.FTPCLIENT.SELECTENTRIES.INFO",
					new Object[] { oper, getParam("ftpPath") }));
		}

		if (oper.equalsIgnoreCase("put")) {
			socket = ftp.putFile(getParam("ftpPath"), binary);
			initParser(null, socket.getOutputStream());
			readingAndNotSeenEOD = false;
		}
		if (oper.equalsIgnoreCase("get")) {
			socket = ftp.getFile(getParam("ftpPath"), binary);
			initParser(socket.getInputStream(), null);
			readingAndNotSeenEOD = true;
		}
		if (oper.equalsIgnoreCase("list")) {
			socket = ftp.list(getParam("ftpPath"), binary);
			initParser(socket.getInputStream(), null);
			readingAndNotSeenEOD = true;
		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * Default implementation.
	 *
	 * @throws Exception
	 *             never
	 */
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * Reads an entry , by calling the provided parser's readEntry() method.
	 *
	 * @return the read entry.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymax(this, "getNextEntry");
		Entry e = getParser().readEntry();
		if (e == null)
	        readingAndNotSeenEOD = false;
		Trace.exitmax(this, "getNextEntry");
		return e;
	}

	/**
	 * Writes an entry , by calling the provided parser's writeEntry() method.
	 *
	 * @param entry
	 *            the entry to be written.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "putEntry", entry);
		getParser().writeEntry(entry);
		Trace.exitmin(this, "putEntry");
	}

	/**
	 * @throws Exception
	 *             if an error occurs.
	 * @deprecated Use #Connector.reconnect(Object) instead
	 */
	@Deprecated
	public void reconnect() throws Exception {
		Trace.entrymin(this, "reconnect");
		terminate();
		initialize(this);
		if (((ConnectorConfig) getConfiguration()).getMode().equals(
				ConnectorConfig.ITERATOR_MODE)) {
			selectEntries();
		}
		Trace.exitmin(this, "reconnect");
	}

	/**
	 * Returns version information.
	 *
	 * @return version info.
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

}
