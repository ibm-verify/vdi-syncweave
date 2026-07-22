/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * Simple TCP Server that accepts TCP connections. Supports SSL and client
 * certificates.
 */
public class TCPServerConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "tcpserverconnector";

	/**
	 * Parameter name in the configuration for tcpPort.
	 */
	private static final String PARAM_TCP_PORT = "tcp.port";

	/**
	 * Parameter name in the configuration for backlog.
	 */
	private static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * Java property parameter name for backlog.
	 */
	private static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * Parameter name in the configuration for useSSL.
	 */
	private static final String PARAM_USE_SSL = "useSSL";

	/**
	 * Parameter name in the configuration for requireClientAuth.
	 */
	private static final String PARAM_REQUIRE_CLIENT_AUTH = "requireClientAuth";

	/**
	 * Backlog to be used if the "backlog" parameter is not set or is empty
	 */
	@SuppressWarnings("unused")
	private static final int BACKLOG_DEFAULT = 50;

	/**
	 * ServerSocket for this Connector listening for client connections.
	 */
	private ServerSocket mServerSocket;

	/**
	 * Socket created after serverSocket.accept().
	 */
	private Socket mSocket;

	/**
	 * New TCPServerConnector object created for every connected client.
	 */
	private TCPServerConnector mServerConnector;

	/**
	 * If true, the Connector has started terminating or has already been
	 * terminated.
	 */
	private boolean mTerminationRequested;

	/**
	 * If true the Connector is accepting client requests.
	 */
	private boolean mIsAccepting;
	/**
	 * ResourceHash used for access of the TMS messages
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor.
	 */
	public TCPServerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.SERVER_MODE, ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Retrieves server connector.
	 * 
	 * @return The server Connector if this Connector is handling a TCP client
	 *         session.
	 */
	public TCPServerConnector getServerConnector() {
		return mServerConnector;
	}

	/**
	 * Sets the server Connector for this Connector.
	 * 
	 * @param aServerConnector
	 *            The serverConnector.
	 */
	public void setServerConnector(TCPServerConnector aServerConnector) {
		this.mServerConnector = aServerConnector;
	}

	/**
	 * Checks whether this Connector is currently waiting for a client
	 * connection.
	 * 
	 * @return true if this Connector is currently waiting for a client
	 *         connection.
	 */
	public boolean isAccepting() {
		return mIsAccepting;
	}

	/**
	 * Checks if a termination is requested.
	 * 
	 * @return true if this Connector has the termination flag set.
	 */
	public boolean isTerminating() {
		return mTerminationRequested;
	}

	/**
	 * Initialize the Connector. To initialize this Connector with a TCP client
	 * session provide a java.net.Socket object for the obj parameter. In all
	 * other cases, the Connector will initialize a TCP server session.
	 * 
	 * @param aObj
	 *            Null, Socket or ConnectorMode class
	 */
	@Override
	public void initialize(Object aObj) throws Exception {

		mTerminationRequested = false;

		String strPort = getParam(PARAM_TCP_PORT);
		if (strPort == null || strPort.trim().length() == 0) {
			throw new Exception(sResHash.getString("CONNECTOR.TCPSRV.MISSING.TCPPORT.EXCEP"));
		}
		int port = Integer.parseInt(strPort); // Get tcp listening port

		int backlog = -1; // The maximum length of the queue.
		String strBacklog = getParam(PARAM_TCP_BACKLOG);
		if (strBacklog == null || strBacklog.trim().length() == 0) {
			strBacklog = System.getProperty(PARAM_SYSTEM_TCP_BACKLOG);
		}

		if (strBacklog != null && strBacklog.trim().length() > 0) {
			backlog = Integer.parseInt(strBacklog);
		}

		String strUseSSL = getParam(PARAM_USE_SSL);
		boolean useSSL = false;
		if (strUseSSL != null) {
			useSSL = Boolean.valueOf(strUseSSL).booleanValue();
		}

		if (mSocket != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.TCPSRV.INITALREADY.INFO"));
			}
			return;
		}

		if (aObj instanceof Socket) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.TCPSRV.USESOCKET.INFO"));
			}
			mSocket = (Socket) aObj;

			// Dramatically enhances speed when running over a LAN
			mSocket.setTcpNoDelay(true);
			return;
		}

		// Create server socket and wait for connections
		if (useSSL) {
			String strRequireClientAuth = getParam(PARAM_REQUIRE_CLIENT_AUTH);
			boolean requireClientAuth = false;
			if (strRequireClientAuth != null) {
				requireClientAuth = Boolean.valueOf(strRequireClientAuth).booleanValue();
			}

			mServerSocket = getSSLServerSocket(port, backlog, requireClientAuth);
			logmsg(sResHash.getString("CONNECTOR.TCPSRV.LISTENSSL.INFO", new Object[] { "" + port, "" + backlog,
					"" + requireClientAuth }));
		} else {
			if (backlog > 0) {
				mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(port, backlog);
			} else {
				mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(port);
			}

			logmsg(sResHash.getString("CONNECTOR.TCPSRV.LISTEN.INFO", new Object[] { "" + port, "" + backlog }));
		}
	}

	/**
	 * Gets the SSL server socket for this connector.
	 * 
	 * @param aPort
	 *            The port to listen to
	 * @param aBacklog
	 *            How many connections are queued
	 * @param aNeedClientAuth
	 *            true if clients must authenticate themselves.
	 * @return the SSL server socket.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private ServerSocket getSSLServerSocket(int aPort, int aBacklog, boolean aNeedClientAuth) throws IOException {

		SSLServerSocket sslServerSocket;
		if (aBacklog > 0) {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort, aBacklog);
		} else {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort);
		}

		sslServerSocket.setNeedClientAuth(aNeedClientAuth);

		return sslServerSocket;
	}

	/**
	 * Server mode - returns a new instance of the Connector for each client
	 * connection.
	 */
	@Override
	public ConnectorInterface getNextClient() throws Exception {
		Socket socket;

		if (!isTerminating()) {
			mIsAccepting = true;
			socket = mServerSocket.accept();
			mIsAccepting = false;
		} else {
			logmsg(sResHash.getString("CONNECTOR.TCPSRV.TERMINATE1.INFO"));
			return null;
		}

		if (isTerminating()) {
			logmsg(sResHash.getString("CONNECTOR.TCPSRV.TERMINATE2.INFO"));
			socket.close();
			socket = null;
			return null;
		}

		logmsg(sResHash.getString("CONNECTOR.TCPSRV.CONNECT.INFO", socket.getInetAddress()));
		TCPServerConnector clientSession = new TCPServerConnector();
		clientSession.setServerConnector(this);
		clientSession.setConfiguration(getConfiguration());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		clientSession.initialize(socket);
		return clientSession;
	}

	/**
	 * Returns the next Entry from the TCP client.
	 * 
	 * @return - the next Entry, or null if the client connection has been
	 *         closed.
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		if (mSocket == null && mServerSocket != null && !mTerminationRequested) {
			mIsAccepting = true;
			mSocket = mServerSocket.accept();
			mIsAccepting = false;
			// Dramatically enhances speed when running over a LAN
			mSocket.setTcpNoDelay(true);
		}
		if (mSocket == null || mTerminationRequested)
			return null;

		Entry entry = new Entry();
		addAttributes(entry);
		return entry;
	}

	/**
	 * Adds all needed attributes to the Entry
	 * 
	 * @param aEntry
	 *            The entry.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void addAttributes(Entry aEntry) throws Exception {
		aEntry.setAttribute("tcp.originator", this);
		aEntry.setAttribute("event.originator", this);
		aEntry.setAttribute("tcp.inputstream", mSocket.getInputStream());
		aEntry.setAttribute("event.inputstream", mSocket.getInputStream());
		aEntry.setAttribute("tcp.outputstream", mSocket.getOutputStream());
		aEntry.setAttribute("event.outputstream", mSocket.getOutputStream());

		aEntry.setAttribute("tcp.remoteIP", mSocket.getInetAddress().getHostAddress());
		aEntry.setAttribute("tcp.remotePort", "" + mSocket.getPort());
		aEntry.setAttribute("tcp.remoteHost", mSocket.getInetAddress().getHostName());
		aEntry.setAttribute("tcp.localIP", mSocket.getLocalAddress().getHostAddress());
		aEntry.setAttribute("tcp.localPort", "" + mSocket.getLocalPort());
		aEntry.setAttribute("tcp.localHost", mSocket.getLocalAddress().getHostName());
		aEntry.setAttribute("tcp.socket", mSocket);
	}

	/**
	 * Flushes the output stream to the client.
	 * 
	 * @param aEntry
	 *            This parameter is ignored
	 */
	@Override
	public void putEntry(Entry aEntry) throws Exception {
		mSocket.getOutputStream().flush();
	}

	/**
	 * Flushes the output stream to the client and closes the connection.
	 * 
	 * @param aEntry
	 *            This parameter is ignored
	 */
	@Override
	public void replyEntry(Entry aEntry) throws Exception {
		putEntry(aEntry);
		mSocket.close();
		mSocket = null;
		mTerminationRequested = true;
	}

	/**
	 * This method tries to terminate the server by setting the termination flag
	 * for the Connector returned by getServerConnector and immediately
	 * connecting to its port.
	 */
	@Override
	public void terminateServer() throws Exception {
		if (getServerConnector() == null) {
			mTerminationRequested = true;
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.TCPSRV.TERMINATERCV.INFO"));
				// At this point we are the server
				debug(sResHash.getString("CONNECTOR.TCPSRV.DUMMY.INFO", new Object[] { mServerSocket.getInetAddress(),
						"" + mServerSocket.getLocalPort() }));
			}
			try {
				new Socket(mServerSocket.getInetAddress(), mServerSocket.getLocalPort());
			} catch (IOException e) {
				logmsg(sResHash.getString("CONNECTOR.TCPSRV.MISSING.TCPPORT.WARN", e.toString()));
			}

		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.TCPSRV.ALREADYTERM.INFO"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.TCPSRV.SENDINGTERM.INFO"));
			}
			getServerConnector().terminateServer();
		}
	}

	/**
	 * Terminate the connector.
	 */
	@Override
	public void terminate() throws Exception {
		super.terminate();

		if (mSocket != null) {
			mSocket.close();
			mSocket = null;
		}
		if (mServerSocket != null) {
			mServerSocket.close();
			mServerSocket = null;
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
