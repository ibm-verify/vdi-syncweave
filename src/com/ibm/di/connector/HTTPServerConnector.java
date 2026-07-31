/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.RetryEntryException;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.HTTPParser;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * This connector provides HTTP server like functionality and ability to
 * receive/handle client requests sent over the HTTP protocol.
 */
public class HTTPServerConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "httpserverconnector";

	/**
	 * Name of the connector .
	 */
	private static final String CONNECTOR_NAME = "HTTP server connector";

	/**
	 * Parameter Name: {@value #PARAMETER_TCP_PORT}
	 */
	public static final String PARAMETER_TCP_PORT = "tcpPort";

	/**
	 * Parameter Name: {@value #PARAM_TCP_BACKLOG}
	 */
	public static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * Parameter Name: {@value #PARAM_SYSTEM_TCP_BACKLOG}
	 */
	public static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * Parameter Name: {@value #PARAMETER_HEADERS_AS_PROPS}
	 */
	public static final String PARAMETER_HEADERS_AS_PROPS = "headersAsProperties";

	/**
	 * Parameter Name: {@value #PARAMETER_TCP_DATA_AS_PROPS}
	 */
	public static final String PARAMETER_TCP_DATA_AS_PROPS = "tcpDataAsProperties";

	/**
	 * Parameter Name: {@value #PARAMETER_AUTH_CONN}
	 */
	public static final String PARAMETER_AUTH_CONN = "authConnector";

	/**
	 * Parameter Name: {@value #PARAMETER_HTTP_BASIC_AUTH}
	 */
	public static final String PARAMETER_HTTP_BASIC_AUTH = "httpAuth";

	/**
	 * Parameter Name: {@value #PARAMETER_AUTH_REALM}
	 */
	public static final String PARAMETER_AUTH_REALM = "authRealm";

	/**
	 * Parameter Name: {@value #PARAMETER_USE_SSL}
	 */
	public static final String PARAMETER_USE_SSL = "useSSL";

	/**
	 * Parameter Name: {@value #PARAMETER_REQUIRE_CLIENT_AUTH}
	 */
	public static final String PARAMETER_REQUIRE_CLIENT_AUTH = "needClientAuth";

	/**
	 * Parameter Name: {@value #PARAMETER_CONTENT_TYPE}
	 */
	public static final String PARAMETER_CONTENT_TYPE = "contentType";

	/**
	 * Parameter Name: {@value #PARAMETER_USE_CHUNKS}
	 */
	public static final String PARAMETER_USE_CHUNKS = "msgChunked";
	
	/**
	 * Parameter Name: {@value #PARAMETER_CONNECTION_TIMEOUT}. Connection
	 * timeout in seconds.
	 */
	public static final String PARAMETER_IDLE_CONNECTION_TIMEOUT = "idleConnectionTimeout";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_REMOTE_USER}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_USER = "http.remote_user";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_REMOTE_PASSWORD}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_PASSWORD = "http.remote_pass";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_BODY}
	 */
	public static final String ATTR_NAME_HTTP_BODY = "http.body";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_CONTENT_TYPE}
	 */
	public static final String ATTR_NAME_HTTP_CONTENT_TYPE = "http.Content-Type";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_CHARACTER_SET}
	 */
	public static final String ATTR_NAME_HTTP_CHARACTER_SET = "characterSet";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_CONNECTION}
	 */
	public static final String ATTR_NAME_HTTP_CONNECTION = "http.connection";

	/**
	 * An {@link Entry} attribute name: {@value #ATTR_NAME_HTTP_AUTH_ENTRY}
	 */
	public static final String ATTR_NAME_HTTP_AUTH_ENTRY = "auth.entry";

	/**
	 * Error String: {@value #HTTP_BAD_REQUEST}
	 */
	public static final String HTTP_BAD_REQUEST = "400 Bad Request";

    /**
     * The Connector version.
     */
    public static final String VERSION_INFO = "2.1-di7.1.1 %I%, 20%E%";

    /**
     * Possible Connector modes.
     */
    public static final String[] CONNECTOR_MODES = {
            ConnectorConfig.SERVER_MODE, 
            ConnectorConfig.ITERATOR_MODE,
            };

	/**
	 * Server connector for the current connector.
	 */
	private HTTPServerConnector mServerConnector;

	/**
	 * Authentication connector.
	 */
	private ConnectorInterface mAuthConnector = null;

	/**
	 * {@link ServerSocket} instance.
	 */
	private ServerSocket mServerSocket;

	/**
	 * {@link HTTPParser} instance.
	 */
	private HTTPParser mHttpParser;

	/**
	 * Client {@link Socket}
	 */
	private Socket mClientSocket;

	/**
	 * Should the request remain alive.
	 */
	private boolean mKeepAlive = true;

	/**
	 * Termination request flag.
	 */
	private AtomicBoolean mTerminationRequested = new AtomicBoolean(false);

	/**
	 * Indicates whether connector is currently waiting for a client connection.
	 */
	private boolean mIsAccepting = false;

	/**
	 * Use SSL to set up the connection flag.
	 */
	private boolean mUseSSL = false;

	/**
	 * Request HTTP Basic authentication flag.
	 */
	private boolean mHttpBasicAuth = false;

	/**
	 * 
	 */
	private boolean mUseProps = true;

	/**
	 * Reads/Writes TCP Data as Entry Properties, else reads/writes them as
	 * Entry Attributes
	 */
	private boolean mTcpDataAsProps = true;

	/**
	 * If <code>true</code> , the http body of the message is transferred as a
	 * series of chunks
	 */
	private boolean mMsgChunked = false;

	/**
	 * Client request rejected flag.
	 */
	private boolean mRejectClientAuth = false;

	/**
	 * Specifies the content-type HTTP header to use when sending data.
	 */
	private String mContentType;

	/**
	 * User name.
	 */
	private String mUsername;

	/**
	 * Password
	 */
	private String mPassword;

	/**
	 * Port for incoming HTTP requests.
	 */
	private int mPort;

	/**
	 * Indicates first request.
	 */
	private boolean mFirstRequest = true;

	/**
	 * {@link Entry} object used for the communication with the remote client.
	 */
	private Entry mRequestEntry = null;

	/**
	 * Automatic chunking flag.
	 */
	private boolean autoChunking = false;

	/**
	 * {@link HTTPChunkedWriter} instance.
	 */
	private HTTPChunkedWriter mHttpChunkWriter;
	
	/**
	 * Connection timeout in seconds. This timeout affects how long the server
	 * will tolerate idle persistent connections (reusing the same TCP
	 * connection for multiple HTTP requests). If the server receives no request
	 * within the specified timeout, it will declare the TCP connection dead and
	 * will close it. Since the default socket time out value varies for 
	 * different browsers, the value set here is the maximum with respect
	 * to browsers. 
	 */
	private int mIdleConnectionTimeout = 120;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Constructs this object, sets the supported modes and initializes the
	 * internally used {@link HTTPParser}.
	 */
	public HTTPServerConnector() {
		setName(CONNECTOR_NAME);
		setModes(CONNECTOR_MODES);

		mHttpParser = new HTTPParser();
		mHttpParser.setClientMode(false);
		mHttpParser.setContext(this);
	}

	/**
	 * Parsers all the configuration parameters of this connector and prepares
	 * for starting the HTTP Server. If a {@link Socket} is provided as a
	 * parameter then that socket will be used for communication with the client
	 * on the other end of the socket.
	 * 
	 * @param aObject
	 *            recognizes only an object of type {@link Socket}
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object aObject) throws Exception {

		mTerminationRequested.set(false);

		if (mClientSocket != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.INITALREADY.INFO"));
			}
			return;
		}

		mHttpParser.setDebug(debugMode());

		mMsgChunked = Boolean.valueOf(getParam(PARAMETER_USE_CHUNKS)).booleanValue();
		mHttpBasicAuth = Boolean.valueOf(getParam(PARAMETER_HTTP_BASIC_AUTH)).booleanValue();
		mUseProps = Boolean.valueOf(getParam(PARAMETER_HEADERS_AS_PROPS)).booleanValue();
		mTcpDataAsProps = Boolean.valueOf(getParam(PARAMETER_TCP_DATA_AS_PROPS)).booleanValue();

		mContentType = getParam(PARAMETER_CONTENT_TYPE);
		if (mContentType == null || mContentType.length() == 0) {
			mContentType = "text/html";
		}
		
		String strConnectionTimeout = getParam(PARAMETER_IDLE_CONNECTION_TIMEOUT);
		if (strConnectionTimeout != null && strConnectionTimeout.trim().length() > 0) {
			mIdleConnectionTimeout = Integer.parseInt(strConnectionTimeout);
		} else {
			mIdleConnectionTimeout = 120;
		}

		if (aObject instanceof Socket) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.PROVIDEDSOCKET.INFO"));
			}
			setSocket((Socket) aObject);
		} else {
			if (mAuthConnector == null) {
				try {
					String authConnectorName = (String) getParam(PARAMETER_AUTH_CONN);
					if (authConnectorName != null && !authConnectorName.equalsIgnoreCase("(none)")) {
						mAuthConnector = SystemFunctions.loadConnector(authConnectorName);
						mAuthConnector.initialize(null);
					}
				} catch (Exception ex) {
					logmsg(sResHash.getString("CONNECTOR.HTTPSRV.UNABLE.TO.LOAD.AUTHENTICATOR.CONNECTOR", new Object[] {
							getParam(PARAMETER_AUTH_CONN), ex.toString() }));
				}
			}

			String strPort = getParam(PARAMETER_TCP_PORT);
			if (strPort == null || strPort.trim().length() == 0) {
				throw new com.ibm.di.exceptions.MissingConfigurationException(CONNECTOR_NAME, PARAMETER_TCP_PORT);
			}

			mPort = Integer.parseInt(strPort);

			int backlog = -1; // The maximum length of the queue.
			String strBacklog = getParam(PARAM_TCP_BACKLOG);
			if (strBacklog == null || strBacklog.trim().length() == 0) {
				strBacklog = System.getProperty(PARAM_SYSTEM_TCP_BACKLOG);
			}

			if (strBacklog != null && strBacklog.trim().length() > 0) {
				backlog = Integer.parseInt(strBacklog);
			}

			mUseSSL = Boolean.valueOf(getParam(PARAMETER_USE_SSL)).booleanValue();
			if (mUseSSL == true) {
				boolean requireClientAuth = false;
				String strRequireClientAuth = getParam(PARAMETER_REQUIRE_CLIENT_AUTH);
				if (strRequireClientAuth != null) {
					requireClientAuth = Boolean.valueOf(strRequireClientAuth).booleanValue();
				}

				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.LISTENING.FOR.HTTP.SSL.CONNECTIONS.ON.PORT", Integer.valueOf(mPort)));

				mServerSocket = getSSLServerSocket(mPort, backlog, requireClientAuth);
			} else {
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.LISTENING.FOR.HTTP.CONNECTIONS.ON.PORT", Integer.valueOf(mPort)));

				if (backlog > 0) {
					mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort, backlog);
				} else {
					mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort);
				}
			}
		}
	}

	/**
	 * Sets the {@link Socket} object used for the communication with the remote
	 * client.
	 * 
	 * @param aSocket
	 *            the socket to set.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void setSocket(Socket aSocket) throws Exception {
		mClientSocket = aSocket;
		mClientSocket.setTcpNoDelay(true);
		mClientSocket.setSoTimeout(mIdleConnectionTimeout*1000);

		String charSet = getParam(ATTR_NAME_HTTP_CHARACTER_SET);
		if (charSet == null || charSet.isEmpty())
			charSet = "UTF-8";
		mHttpParser.setParam(ATTR_NAME_HTTP_CHARACTER_SET, charSet);
		mHttpParser.setInputStream(mClientSocket.getInputStream());
		mHttpParser.setOutputStream(mClientSocket.getOutputStream());
		mHttpParser.setUseProperties(mUseProps);

		HookConfig h = ((ConnectorConfig)getConfiguration()).getHooks().getHook("after_getnextclient", false);
		if (h != null && h.getEnabled())
			mRequestEntry = doAuthentication();
		else
			mRequestEntry = null;

		mKeepAlive = true;
		mFirstRequest = true;
	}

	/**
	 * This method blocks until a client is connected. After a connection is
	 * established a new instance of this object is created, initialized and
	 * returned.
	 * 
	 * @return a new instance of {@link HTTPServerConnector} responsible for
	 *         handling the communication with the new client.
	 * @throws Exception
	 *             if this connector was not initialized properly or other type
	 *             of error occurs.
	 */
	public ConnectorInterface getNextClient() throws Exception {

		if (mServerSocket == null) {
			throw new Exception(sResHash.getString("CONNECTOR.HTTPSRV.NOTSERVER.SESSION.ERROR"));
		}

		Socket clientSocket;

		if (!isTerminating()) {
			mIsAccepting = true;
			clientSocket = mServerSocket.accept();
			mIsAccepting = false;
		} else {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.CONNECTOR.TERMINATED.BY.EXTERNAL"));
			return null;
		}

		if (isTerminating()) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.CONNECTOR.TERMINATED.BY.EXTERNAL"));
			clientSocket.close();
			clientSocket = null;
			return null;
		}

		HTTPServerConnector clientSession = new HTTPServerConnector();
		try {// Try catch added by L3 defect # 10953
			clientSession.setServerConnector(this);
			clientSession.setConfiguration(getConfiguration());
			clientSession.setRSInterface(getRSInterface());
			clientSession.setAuthConnector(mAuthConnector);
			clientSession.setName(getName());
			clientSession.setLog(getLog());
			clientSession.initialize(clientSocket);
		} catch (SSLException ssle) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.IGNORE.SSL.EXCEPTION"));
			}
			try {
				clientSocket.close();
			} catch (IOException ignore) {
				clientSocket = null;
			}
			throw new RetryEntryException("");

		} catch (SocketException se) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.IGNORE.SSL.EXCEPTION"));
			}
			try {
				clientSocket.close();
			} catch (IOException ignore) {
				clientSocket = null;
			}
			throw new RetryEntryException("");

		} catch (NoSuchElementException nsee) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.IGNORE.NO.SUCH.ELEMENT.EXCEPTION"));
			}
			try {
				clientSocket.close();
			} catch (IOException ignore) {
				clientSocket = null;
			}
			throw new RetryEntryException("");
		}

		return clientSession;
	}

	/**
	 * Sets the authentication connector for this connector.
	 * 
	 * @param authConn
	 *            the connector to set.
	 */
	private void setAuthConnector(ConnectorInterface authConn) {
		mAuthConnector = authConn;
	}

	/**
	 * Retrieves server connector.
	 * 
	 * @return the server connector if this connector is handling an HTTP client
	 *         session, or <code>null</code> if a connection has not been
	 *         established yet.
	 */
	public HTTPServerConnector getServerConnector() {
		return mServerConnector;
	}

	/**
	 * Sets the server connector for this connector.
	 * 
	 * @param aServerConnector
	 *            the server connector that created this instance.
	 */
	public void setServerConnector(HTTPServerConnector aServerConnector) {
		this.mServerConnector = aServerConnector;
	}

	/**
	 * Checks whether the connector is waiting for a client connection.
	 * 
	 * @return true if this connector is currently waiting for a client
	 *         connection.
	 */
	public boolean isAccepting() {
		return mIsAccepting;
	}

	/**
	 * Checks whether a termination request is sent.
	 * 
	 * @return true if this connector has the termination flag set.
	 */
	public boolean isTerminating() {
		return mTerminationRequested.get();
	}

	/**
	 * This method tries to terminate the server by setting the termination flag
	 * for the connector returned by getServerConnector and immediately
	 * connecting to its port (which should fail).
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void terminateServer() throws Exception {
		if (getServerConnector() == null) {
			mTerminationRequested.set(true);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.TERMINATE.SERVER.REQUEST.RECEIVED"));
				// At this point we are the server
				debug(sResHash.getString("CONNECTOR.HTTPSRV.CREATE.DUMMYCONN.INFO", new Object[] { mServerSocket.getInetAddress(),
						Integer.valueOf(mServerSocket.getLocalPort()) }));
			}
			try {
				new Socket(mServerSocket.getInetAddress(), mServerSocket.getLocalPort());
			} catch (IOException e) {
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.IOEXCEPTION.ON.UNBLOCKING.CONNECTOR.SERVER.SOCKET", e.toString()));
			}

		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.HTTPSRV.SERVER.IS.ALREADY.TERMINATING"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.SENDING.TERMINATION.REQUEST"));
			}
			getServerConnector().terminateServer();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {

		closeClientSocket();
		
		if (mServerSocket != null) {
			mServerSocket.close();
			mServerSocket = null;
		}

		mHttpParser.closeParser();
	}

	private void closeClientSocket() {
		if (mClientSocket != null) {
			try {
				mClientSocket.close();
			} catch (IOException ignore) {
				SystemFunctions.doNothing();
			}
			mClientSocket = null;
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.CLIENT.CONNECTION.CLOSED"));
		}	
		mTerminationRequested.set(true);
	}
	
	/**
	 * This method is called when this connector is used in Iterator mode. This
	 * method will block until a client connects on the opened port. If the
	 * authentication fails the method returns <code>null</code>. If
	 * authentication is successful the HTTP request is parsed to an
	 * {@link Entry} object and returned.
	 * 
	 * @return the parsed HTTP request as an {@link Entry} object.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {

		if (mClientSocket == null && !isTerminating()) {
			mIsAccepting = true;
			setSocket(mServerSocket.accept());
			mIsAccepting = false;
		}

		if (mRejectClientAuth) {
			return null;
		}

		if (!mKeepAlive || isTerminating()) {
			terminate();
			return null;
		}

		Entry requestEntry = null;

		try {
			if (mFirstRequest) {
				requestEntry = mRequestEntry != null ? mRequestEntry : doAuthentication();
				mFirstRequest = false;
			} else {
				requestEntry = mHttpParser.readEntry();
			}
			if (requestEntry == null) {
				return null;
			}
		} catch (java.net.SocketTimeoutException ste) {
			debug(sResHash.getString("CONNECTOR.HTTPSRV.GETNEXTENTRY.SOCKET.EXCEPTION", ste.toString()));
			return null;
		} catch (java.net.SocketException se) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.GETNEXTENTRY.SOCKET.EXCEPTION", se.toString()));
			return null;
		} catch (SSLException ssle) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.IGNORE.SSL.EXCEPTION"));
			return null;
		} catch (NoSuchElementException nsee) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.IGNORE.NO.SUCH.ELEMENT.EXCEPTION"));
			return null;
		}

		// Check "Connection: close" header
		String connection = (String) mHttpParser.getProp(requestEntry, ATTR_NAME_HTTP_CONNECTION);
		if (connection != null) {
			mKeepAlive = (connection.toLowerCase(Locale.ENGLISH).indexOf("close") == -1);
		} else {
			// default to persistent connection as recommended by HTTP 1.1
			mKeepAlive = true;
		}

		// Parse http.body?
		Object bodyBuffer = null;
		if (mUseProps) {
			bodyBuffer = requestEntry.getProperty(ATTR_NAME_HTTP_BODY);
		} else {
			Attribute bodyAttribute = requestEntry.getAttribute(ATTR_NAME_HTTP_BODY);
			if (bodyAttribute != null) {
				bodyBuffer = bodyAttribute.getValue(0);
			}
		}

		if (bodyBuffer != null) {
			if (hasParser()) {
				if (bodyBuffer instanceof byte[])
					initParser(bodyBuffer, null);
				else
					initParser(bodyBuffer.toString(), null);
				Entry parsedEntry = getParser().readEntry();
				if (parsedEntry != null)
					requestEntry.merge(parsedEntry);
			} else {
				if (bodyBuffer instanceof byte[])
					requestEntry.setAttribute(ATTR_NAME_HTTP_BODY, bodyBuffer);
				else
					requestEntry.setAttribute(ATTR_NAME_HTTP_BODY, bodyBuffer.toString());

			}
		}

		addTCPProperties(requestEntry, mClientSocket);

		return requestEntry;
	}

	/**
	 * This method provides chunking capabilities to the HTTP server. This
	 * method is used from a scripts and is not directly called by the TDI
	 * Server. If the chunking is turned off the usage of this method will turn
	 * it on.
	 * 
	 * @param aEntry
	 *            the entry to send as a chunk.
	 * @throws Exception
	 *             if a communication error is raised.
	 */
	public void putEntry(Entry aEntry) throws Exception {

		// -- One could argue that an empty entry would send the final chunk (0
		// size) to the client.
		// -- But, this should be done by the TDI developer by calling reply on
		// the component to
		// -- force out the final chunk.
		if (aEntry.size() == 0) {
			return;
		}

		//
		// When you call putEntry (or add on the component) we enable chunking.
		//
		if (mHttpChunkWriter == null) {
			initChunk(mClientSocket.getOutputStream());
		}

		String chunk = null;

		if (hasParser()) {
			chunk = parseEntry(aEntry);
		} else {
			chunk = aEntry.getString(ATTR_NAME_HTTP_BODY);
		}

		if (chunk != null && chunk.length() > 0) {
			mHttpChunkWriter.write(chunk);
			mHttpChunkWriter.flush();
		}
	}

	/**
	 * This method sends the provided entry to the client as an HTTP response.
	 * If chunking is enabled then the provided entry will be sent as the last
	 * chunk.
	 * 
	 * @param aEntry
	 *            the entry to send to the client.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void replyEntry(Entry aEntry) throws Exception {

		//
		// We keep the mMsgChunked parameter to force chunked transfer encoding.
		// Other than that,
		// this has little effect compared to not using chunked transfer
		// encoding.
		//
		if (mMsgChunked || mHttpChunkWriter != null) {
			putEntry(aEntry);
			// Close will send the final zero-length chunk
			mHttpChunkWriter.close();
			mHttpChunkWriter = null;
		} else {
			if (hasParser()) {
				String body = parseEntry(aEntry);
				mHttpParser.setProp(aEntry, ATTR_NAME_HTTP_BODY, body);
			}

			Object contentType = null;
			if (mUseProps) {
				contentType = aEntry.getProperty(ATTR_NAME_HTTP_CONTENT_TYPE);
			} else {
				Attribute contentTypeAttribute = aEntry.getAttribute(ATTR_NAME_HTTP_CONTENT_TYPE);
				if (contentTypeAttribute != null) {
					contentType = contentTypeAttribute.getValue();
				}
			}

			if (contentType == null || contentType.toString().length() == 0) {
				mHttpParser.setProp(aEntry, ATTR_NAME_HTTP_CONTENT_TYPE, mContentType);
			}

			try {
				mHttpParser.writeEntry(aEntry);
				mHttpParser.getOutputStream().flush();
			} catch (java.net.SocketException se) {
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.CONNECTION.CLOSED.BY.CLIENT"));
				closeClientSocket();
				mKeepAlive = false;
			}
		}
		
		// check for connection close header in the response
		String connection = aEntry.getString(ATTR_NAME_HTTP_CONNECTION); 
		if (connection != null && connection.indexOf("close") != -1) {
			mKeepAlive = false;
		}
	}

	/**
	 * Returns a server socket bound to the specified port, and uses the
	 * specified connection backlog.
	 * 
	 * @param aPort
	 *            the port to listen to
	 * @param aBacklog
	 *            how many connections are queued
	 * @param aNeedClientAuth
	 *            is authentication required
	 * @return the ServerSocket
	 * @throws IOException
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
	 * Retrieves username.
	 * 
	 * @return the username used for the authentication.
	 */
	public String getUserName() {
		return mUsername;
	}

	/**
	 * Retrieves password.
	 * 
	 * @return the password the user has authenticated with.
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * Prints the Forbidden page and closes the connection. This method does not
	 * have effect if the basic authentication is not enabled for this
	 * connector.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void rejectClientAuthentication() throws Exception {
		if (mHttpBasicAuth) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.REJECT.CLIENT.AUTHENTICATION"));
			mRejectClientAuth = true;
			mHttpParser.httpForbidden();
			terminate();
		}
	}

	/**
	 * Parses the entry's attribute's names and values into {@link String}.
	 * 
	 * @param aEntry
	 *            entry to parse
	 * @return the String representation
	 * @throws Exception
	 */
	private String parseEntry(Entry aEntry) throws Exception {
		Entry parsedEntry = new Entry();
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.HTTPSRV.CONSTRUCTING.FILTERED.ENTRY.FOR.PARSER", getParam("parser")));
		}

		String[] names = aEntry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith("http.") || names[i].startsWith("tcp.")) {
				continue;
			}

			parsedEntry.setAttribute(names[i], aEntry.getAttribute(names[i]));
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPSRV.ATTRIBUTES", names[i]));
			}
		}

		StringWriter sw = new StringWriter();
		initParser(null, sw);
		getParser().writeEntry(parsedEntry);
		getParser().closeParser();

		return sw.toString();
	}

	/**
	 * This method adds the tcp properties to the provided entry.
	 * 
	 * @param aEntry
	 *            Entry to set properties on
	 * @param aSocket
	 *            socket holding the properties.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void addTCPProperties(Entry aEntry, Socket aSocket) throws Exception {

		setProperty(aEntry, "tcp.inputstream", aSocket.getInputStream(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.outputstream", aSocket.getOutputStream(), mTcpDataAsProps);

		setProperty(aEntry, "tcp.remoteIP", aSocket.getInetAddress().getHostAddress(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.remotePort", "" + aSocket.getPort(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.remoteHost", aSocket.getInetAddress().getHostName(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.localIP", aSocket.getLocalAddress().getHostAddress(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.localPort", "" + aSocket.getLocalPort(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.localHost", aSocket.getLocalAddress().getHostName(), mTcpDataAsProps);
		setProperty(aEntry, "tcp.socket", aSocket, mTcpDataAsProps);
	}

	/**
	 * This method adds a property to the provided entry if the
	 * <code>aTcpDataAsProps</code> is set to true, if it is set to false then
	 * the provided property is added as an attribute.
	 * 
	 * @param aEntry
	 *            the entry which the property will be set on.
	 * @param aProperty
	 *            the property name.
	 * @param aValue
	 *            the property value.
	 * @param aTcpDataAsProps
	 *            tells whether the property will be set as an Entry property or
	 *            as Entry Attribute.
	 */
	public void setProperty(Entry aEntry, String aProperty, Object aValue, boolean aTcpDataAsProps) {
		if (aTcpDataAsProps) {
			aEntry.setProperty(aProperty, aValue);
		} else {
			aEntry.setAttribute(aProperty, aValue);
		}
	}

	/**
	 * The method does the connector authentication.
	 * 
	 * @param aHttpParser
	 *            to read requests from
	 * @param aAuthConnector
	 *            authentication connector
	 * @param aAuthEntry
	 *            request {@link Entry}
	 * @return The authenticated Entry if successful, null if not successful
	 * @throws Exception
	 */
	private Entry doConnectorAuth(HTTPParser aHttpParser, ConnectorInterface aAuthConnector, Entry aAuthEntry) throws Exception {

		int retryCount = 0;
		Entry authRecord = null;

		do {
			if (aAuthConnector != null && authRecord == null) {

				mUsername = (String) aHttpParser.getProp(aAuthEntry, ATTR_NAME_HTTP_REMOTE_USER);

				if (mUsername != null) {
					logmsg(sResHash.getString("CONNECTOR.HTTPSRV.AUTHENTICATE.USER", mUsername));

					mPassword = (String) aHttpParser.getProp(aAuthEntry, ATTR_NAME_HTTP_REMOTE_PASSWORD);

					ConnectorConfig connectorConfig = (ConnectorConfig) aAuthConnector.getConfiguration();
					LinkCriteriaConfig linkCriteriaConfig = connectorConfig.getLinkCriteria();

					SearchCriteria searchCriteria = new SearchCriteria();
					java.util.List<?> criteriaNames = linkCriteriaConfig.getCriteriaNames();
					for (int i = 0; i < criteriaNames.size(); i++) {
						LinkCriteriaItem lcItem = linkCriteriaConfig.getCriteria(criteriaNames.get(i));
						searchCriteria.addTemplate((String) lcItem.getAttribute(), lcItem.getMatch(), (String) lcItem.getValue());
					}

					if (linkCriteriaConfig.getMatchAny()) {
						searchCriteria.setType(SearchCriteria.SEARCH_OR);
					}

					Entry searchEntry = new Entry();
					searchEntry.setAttribute("username", mUsername);
					searchEntry.setAttribute("password", mPassword);

					searchCriteria.buildCriteria(searchEntry);

					try {
						authRecord = aAuthConnector.findEntry(searchCriteria);
					} catch (Exception authException) {
						logmsg(sResHash.getString("CONNECTOR.HTTPSRV.AUTHENTICATION.ERROR", authException.toString()));
					}
				}

				if (authRecord == null) {

					if (retryCount >= 3) {
						logmsg(sResHash.getString("CONNECTOR.HTTPSRV.AUTHENTICATION.FAILED.THREE.TIMES"));
						break;
					}

					aHttpParser.httpAuthenticationRequest((String) getParam(PARAMETER_AUTH_REALM));
					retryCount++;
					continue;
				}
			}

			if (authRecord != null) {
				aAuthEntry.setProperty(ATTR_NAME_HTTP_AUTH_ENTRY, authRecord);
				addTCPProperties(aAuthEntry, mClientSocket);
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.AUTHENTICATION.SUCCESSFUL"));
				return aAuthEntry;
			}
		} while ((aAuthEntry = aHttpParser.readEntry()) != null);

		return null;
	}

	/**
	 * Extracts user name and password from the request.
	 * 
	 * @param aHttpParser
	 *            HTTP parser
	 * @param aEntry
	 *            request {@link Entry} to read from
	 * @return the Entry with the user name if successful, <code>null</code> otherwise.o
	 * @throws Exception
	 */
	private Entry getAuthParams(HTTPParser aHttpParser, Entry aEntry) throws Exception {

		int retryCount = 0;

		do {
			mUsername = (String) aHttpParser.getProp(aEntry, ATTR_NAME_HTTP_REMOTE_USER);
			mPassword = (String) aHttpParser.getProp(aEntry, ATTR_NAME_HTTP_REMOTE_PASSWORD);

			if (mUsername == null || mUsername.length() == 0 || mPassword == null || mPassword.length() == 0) {

				if (retryCount >= 3) {
					logmsg(sResHash.getString("CONNECTOR.HTTPSRV.USERNAME.OR.PASSWORD.NOT.PROVIDED"));
					break;
				}

				aHttpParser.httpAuthenticationRequest((String) getParam(PARAMETER_AUTH_REALM));
				retryCount++;
			} else {
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.USERNAME", mUsername));
				return aEntry;
			}
		} while ((aEntry = aHttpParser.readEntry()) != null);

		return null;
	}

	/**
	 * Verifies the identity of the user if necessary retrieves the next
	 * request.
	 * 
	 * @return the request {@link Entry} from the parser
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Entry doAuthentication() throws Exception {

		Entry requestEntry = mHttpParser.readEntry();

		if (requestEntry == null) {
			logmsg(sResHash.getString("CONNECTOR.HTTPSRV.NO.HTTP.REQUEST.AVAILABLE.FROM.CLIENT"));
			terminate();
			return null;
		}

		if (mHttpBasicAuth) {

			try {
				if (mAuthConnector != null) {
					requestEntry = doConnectorAuth(mHttpParser, mAuthConnector, requestEntry);
				} else {
					requestEntry = getAuthParams(mHttpParser, requestEntry);
				}

				if (requestEntry == null) {
					mHttpParser.httpForbidden();
					terminate();
					return null;
				}
			} catch (Exception authException) {
				logmsg(sResHash.getString("CONNECTOR.HTTPSRV.AUTHENTICATION.EXEPTION", authException.toString()));
				mHttpParser.httpForbidden();
				terminate();
				return null;
			}
		}

		return requestEntry;
	}

	/**
	 * Provides methods for writing a chunk to a output stream.
	 */
	private static class HTTPChunkedWriter extends Writer {

		/**
		 * The buffer for the chunk
		 */
		private ByteArrayOutputStream buffer;

		/**
		 * Writer to the buffer.
		 */
		private OutputStreamWriter bufferWriter;

		/**
		 * The stream that ultimately gets the data
		 */
		private OutputStream stream;

		/**
		 * Writer to the stream.
		 */
		private OutputStreamWriter writer;

		/**
		 * The size limit for a chunk
		 */
		private int maxChunkSize;

		/**
		 * Construct a HTTPChunkedWriter with a default chunk size of 256 bytes
		 * 
		 * @param stream
		 *            The stream that chunks will be written to
		 */
		public HTTPChunkedWriter(OutputStream stream) {
			this.maxChunkSize = -1;
			this.init(stream);
		}

		/**
		 * Initialize HTTPChunkedWriter
		 * 
		 * @param stream
		 *            The stream to which chunks will be written
		 */
		private void init(OutputStream stream) {
			// This is the common function to be shared in both constructors....
			this.stream = stream;

			// The buffer for the chunk
			//
			this.buffer = new ByteArrayOutputStream();

			try {
				this.bufferWriter = new OutputStreamWriter(buffer, "UTF-8");
				this.writer = new OutputStreamWriter(this.stream, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				this.bufferWriter = new OutputStreamWriter(buffer);
				this.writer = new OutputStreamWriter(this.stream);
			}
		}

		/**
		 * Write chunk message to writer
		 * 
		 * @return '0'
		 * @throws IOException
		 *             if an I/O error occurs.
		 */
		private int writeChunk() throws IOException {
			if (buffer.size() > 0) {
				writer.write(Integer.toString(buffer.size(), 16) + "\r\n");
				writer.flush();
				buffer.writeTo(stream);
				writer.write("\r\n");
				writer.flush();
			}

			return 0;
		}

		/**
		 * Flush the writer. This will write any data in the internal buffer to
		 * the underlying stream as a single chunk.
		 * 
		 * @see java.io.Writer#flush()
		 */
		public void flush() throws IOException {
			writer.flush();
			bufferWriter.flush();

			writeChunk();
			buffer.reset();
		}

		/**
		 * Close the writer. Any remaining buffered data will be written as a
		 * chunk and a final zero-length chunk will be written to terminate the
		 * transfer.
		 * 
		 * @see java.io.Writer#close()
		 */
		public void close() throws IOException {
			flush();

			// The final chunk has a length of 0
			//
			writer.write("0\r\n\r\n");
			writer.flush();
		}

		/**
		 * Write a data buffer to the writer. As data is written, multiple
		 * chunks may be passed off to the underlying data stream, depending on
		 * the size of this write and the maximum size specified for the chunk.
		 * 
		 * @param cbuf
		 *            The buffer from which data wil be written
		 * @param offset
		 *            Thos offset within the buffer
		 * @param length
		 *            The number of bytes to write
		 * @see java.io.Writer#write(char[], int, int)
		 */
		public void write(char[] cbuf, int offset, int length) throws IOException {
			int remaining = length;

			if (this.maxChunkSize == -1) {
				bufferWriter.write(cbuf, offset, length);
			} else {
				while (remaining > 0) {
					int count = (remaining > this.maxChunkSize) ? this.maxChunkSize : remaining;
					if ((buffer.size() + count) > this.maxChunkSize) {
						count -= buffer.size();
					}

					bufferWriter.write(cbuf, offset, count);
					remaining -= count;
					offset += count;

					if (buffer.size() >= this.maxChunkSize) {
						writeChunk();
					}
				}
			}
		}
	}

	/**
	 * Initializes the chunk writer.
	 * 
	 * @param aOutputStream
	 *            output stream to write to.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void initChunk(OutputStream aOutputStream) throws Exception {

		OutputStreamWriter chunkOutputWriter = new OutputStreamWriter(aOutputStream, "UTF-8");

		String contentType = "Content-Type: " + mContentType + "\r\n";

		chunkOutputWriter.write("HTTP/1.1 200 OK\r\n");
		chunkOutputWriter.write("Transfer-Encoding: chunked\r\n");
		chunkOutputWriter.write(contentType);
		chunkOutputWriter.write("Connection: keep-alive\r\n");
		chunkOutputWriter.write("\r\n");
		chunkOutputWriter.flush();

		mHttpChunkWriter = new HTTPChunkedWriter(aOutputStream);
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * This method lets the user dynamically configure chunked output mode. It
	 * has no effect if chunking already is enabled through the configuration.
	 * Setting this to "true" will cause the putEntry method to automatically
	 * initialize the chunked writer for output.
	 * 
	 * @param enabled
	 *            The new value for automatic chunking.
	 */
	public void setAutoChunking(boolean enabled) {
		this.autoChunking = enabled;
	}

	/**
	 * Returns whether automatic chunking is enabled or not.
	 * 
	 * @return True if automatic chunking is enabled.
	 */
	public boolean isAutoChunking() {
		return autoChunking;
	}

	/**
	 * Returns true if response is chunked.
	 * 
	 * @return True if response should be chunked.
	 */
	public boolean isChunked() {
		return mMsgChunked;
	}
	
	/**
	 * Returns true if the client socket is closed
	 * @return
	 */
	public boolean isConnectionClosed() {
		return mClientSocket == null;
	}
}
