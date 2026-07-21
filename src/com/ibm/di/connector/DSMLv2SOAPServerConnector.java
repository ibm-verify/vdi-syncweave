/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.net.ssl.SSLServerSocket;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.RetryEntryException;
import com.ibm.di.parser.Dsmlv2Parser;
import com.ibm.di.parser.HTTPParser;
import com.ibm.di.server.ResourceHash;
import com.ibm.ldap.dsml.LdapResult;
import com.ibm.ldap.dsml.SearchResultEntry;

/**
 * The DSMLv2 SOAP Server Connector implements the DSMLv2 standard
 * (http://www.oasis-open.org/committees/dsml/docs/DSMLv2.doc). It listens for
 * DSMLv2 requests over HTTP. Once it receives the request, it parses the
 * request and sends the parsed request to the AssemblyLine workflow to process
 * it. The result is sent back to the client over HTTP. SOAP DSML binding is
 * supported by the Connector.
 */
public class DSMLv2SOAPServerConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dsmlv2serverconnector";

	/**
	 * Name of the connector.
	 */
	private static final String CONNECTOR_NAME = "DSML v2 SOAP Server Connector";

	/**
	 * A parameter name: {@value #PARAMETER_DSML_PORT}
	 */
	public static final String PARAMETER_DSML_PORT = "dsmlPort";

	/**
	 * A parameter name: {@value #PARAM_TCP_BACKLOG}
	 */
	public static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * A parameter name: {@value #PARAM_SYSTEM_TCP_BACKLOG}
	 */
	public static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * A parameter name: {@value #PARAMETER_USE_SSL}
	 */
	public static final String PARAMETER_USE_SSL = "useSSL";

	/**
	 * A parameter name: {@value #PARAM_REQUIRE_CLIENT_AUTH}
	 */
	public static final String PARAM_REQUIRE_CLIENT_AUTH = "needClientAuth";

	/**
	 * A parameter name: {@value #PARAMETER_BINARY_ATTRIBUTES}
	 */
	public static final String PARAMETER_BINARY_ATTRIBUTES = "binaryAttributes";

	/**
	 * A parameter name: {@value #PARAMETER_SOAPBINDING}
	 */
	public static final String PARAMETER_SOAPBINDING = "soapbinding";

	/**
	 * A parameter name: {@value #PARAMETER_HTTP_BASIC_AUTH}
	 */
	public static final String PARAMETER_HTTP_BASIC_AUTH = "httpAuth";

	/**
	 * A parameter name: {@value #PARAMETER_AUTH_REALM}
	 */
	public static final String PARAMETER_AUTH_REALM = "authRealm";

	/**
	 * A parameter name: {@value #HTTP_PARAM_PARAMETER_USE_CHUNKS}
	 */
	public static final String HTTP_PARAM_PARAMETER_USE_CHUNKS = "msgChunked";

	/**
	 * HTTP Error String: {@value #HTTP_BAD_REQUEST}
	 */
	public static final String HTTP_BAD_REQUEST = "400 Bad Request";

	/**
	 * HTTP Error String: {@value #HTTP_INTERNAL_SERVER_ERROR}
	 */
	public static final String HTTP_INTERNAL_SERVER_ERROR = "500 Internal Server Error";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_REMOTE_USER}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_USER = "http.remote_user";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_REMOTE_PASSWORD}
	 */
	public static final String ATTR_NAME_HTTP_REMOTE_PASSWORD = "http.remote_pass";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_BODY}
	 */
	public static final String ATTR_NAME_HTTP_BODY = "http.body";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_CONTENT_TYPE}
	 */
	public static final String ATTR_NAME_HTTP_CONTENT_TYPE = "http.Content-Type";

	/**
	 * An attribute name: {@value #ATTR_NAME_HTTP_CHARACTER_SET}
	 */
	public static final String ATTR_NAME_HTTP_CHARACTER_SET = "characterSet";

	/**
	 * Batch begin tag.
	 */
	private static final String BATCH_BEGIN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<batchResponse xmlns=\"urn:oasis:names:tc:DSML:2:0:core\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n";

	/**
	 * Batch end tag.
	 */
	private static final String BATCH_END = "</batchResponse>";

	/**
	 * SOAP end tag.
	 */
	private static final String SOAP_END = "</batchResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>\r\n";

	/**
	 * SOAP begin tag.
	 */
	private static final String SOAP_BEGIN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<SOAP-ENV:Body xmlns:dsml=\"urn:oasis:names:tc:DSML:2:0:core\">"
			+ "<batchResponse xmlns=\"urn:oasis:names:tc:DSML:2:0:core\" "
			+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";

	/**
	 * {@link Dsmlv2Parser} instance.
	 */
	private Dsmlv2Parser mDsmlv2Parser;

	/**
	 * {@link HTTPParser} instance.
	 */
	private HTTPParser mHttpParser;

	/**
	 * Server connector for the current connector.
	 */
	private DSMLv2SOAPServerConnector mServerConnector;

	/**
	 * {@link ServerSocket}
	 */
	private ServerSocket mServerSocket;

	/**
	 * Result of a search request.
	 */
	private Vector<Entry> mResultEntries;

	/**
	 * User defined binary attributes.
	 */
	private String mBinaryAttributes;

	/**
	 * User name.
	 */
	private String mUsername;

	/**
	 * Password.
	 */
	private String mPassword;

	/**
	 * Client {@link Socket}
	 */
	private Socket mClientSocket;

	/**
	 * {@link StringWriter} instance.
	 */
	private StringWriter mStringWriter;

	/**
	 * Termination request flag.
	 */
	private boolean mTerminationRequested = false;

	/**
	 * Request HTTP Basic authentication flag.
	 */
	private boolean mHttpBasicAuth = false;

	/**
	 * Indicates whether connector is currently waiting for a client connection.
	 */
	private boolean mIsAccepting = false;

	/**
	 * Use SSL to set up the connection flag.
	 */
	private boolean mUseSSL = false;

	/**
	 * Use DSML SOAP Binding flag.
	 */
	private boolean mSoapBinding = true;

	/**
	 * If <code>true</code>, the body of the message is transferred as a series
	 * of chunks
	 */
	private boolean mMsgChunked = false;

	/**
	 * Mandate client authentication using SSL
	 */
	private boolean mRequireClientAuth = false;

	/**
	 * Indicates that Batch is opened.
	 */
	private boolean mBatchOpened = false;

	/**
	 * Indicates whether the client authentication is rejected or not.
	 */
	private boolean mRejectClientAuth = false;

	/**
	 * Indicate successfulSoperation
	 */
	private boolean mInitializeSuccessfull = false;

	/**
	 * Port for incoming DSML requests.
	 */
	private int mPort;
	/**
	 * Object used for access of the TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * {@link DSMLv2ChunkedWriter} instance.
	 */
	private DSMLv2ChunkedWriter mChunkDsmlv2Writer;

	/**
	 * Default constructor. Instantiates the internally used DSMLv2 Parser and
	 * HTTP Parser.
	 */
	public DSMLv2SOAPServerConnector() {
		super();
		setName(CONNECTOR_NAME);
		setModes(new String[] { ConnectorConfig.SERVER_MODE, });

		mHttpParser = new HTTPParser();
		mHttpParser.setUseProperties(false);
		mHttpParser.setContext(this);

		mDsmlv2Parser = new Dsmlv2Parser();
		mDsmlv2Parser.setContext(this);
	}

	/**
	 * Initializes the connector. Parses all the configuration parameters.
	 *
	 * @param aObject
	 *            if this is an instance of {@link Socket} then that object is
	 *            used as a connection with the client.
	 * @throws Exception
	 *             if the url parameter is missing or the protocol used is
	 *             neither "http" nor "https".
	 */
	public void initialize(Object aObject) throws Exception {

		if (mClientSocket != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.ALREADYINIT.WARNING"));
			}
			return;
		}

		mHttpParser.setDebug(debugMode());
		mDsmlv2Parser.setDebug(debugMode());

		mMsgChunked = Boolean.valueOf(getParam(HTTP_PARAM_PARAMETER_USE_CHUNKS)).booleanValue();
		mSoapBinding = Boolean.valueOf(getParam(PARAMETER_SOAPBINDING)).booleanValue();
		mHttpBasicAuth = Boolean.valueOf(getParam(PARAMETER_HTTP_BASIC_AUTH)).booleanValue();

		mBinaryAttributes = getParam(PARAMETER_BINARY_ATTRIBUTES);

		if (debugMode()) {
			if (mSoapBinding) {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.USESOAPBINDING.INFO"));
			} else {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.NOTUSESOAPBINDING.INFO"));
			}
		}

		if (aObject instanceof Socket) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.USINGSOCKETFOR.DSMLCLIENT.INFO"));
			}
			mClientSocket = (Socket) aObject;
			mClientSocket.setTcpNoDelay(true);

			mDsmlv2Parser.setParam(Dsmlv2Parser.PARAMETER_MODE, "Server");
			mDsmlv2Parser.setParam(PARAMETER_SOAPBINDING, (mSoapBinding) ? "true" : "false");
			mDsmlv2Parser.setParam(Dsmlv2Parser.PARAMETER_BINARY_ATTRIBUTES, mBinaryAttributes);

			mHttpParser.setInputStream(mClientSocket.getInputStream());
			mHttpParser.setOutputStream(mClientSocket.getOutputStream());
			mHttpParser.setParam("characterSet", "UTF-8");
			mHttpParser.initParser();

			Entry requestEntry = mHttpParser.readEntry();
			if (requestEntry == null) {
				logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.NODSML.FROMCLIENT.INFO"));
				httpStatusResponse(HTTP_BAD_REQUEST);
				terminate();
				return;
			}

			if (mHttpBasicAuth) {
				try {
					if (!doAuthentication(mHttpParser, requestEntry)) {
						mHttpParser.httpForbidden();
						terminate();
						return;
					}
				} catch (Exception exception) {
					logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.EXCEPTION.WARNING", exception.toString()));
					terminate();
					return;
				}
			}

			byte[] bodyAsBytes = (byte[]) requestEntry.getObject("http.bodyAsBytes");
			if (bodyAsBytes == null ||bodyAsBytes.length == 0) {
				logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.NOMSGBODY.FROMCLIENT.WARNING"));
				httpStatusResponse(HTTP_BAD_REQUEST);
				terminate();
				return;
			}

			mStringWriter = new StringWriter();

			mDsmlv2Parser.setInputStream(new ByteArrayInputStream(bodyAsBytes));
			mDsmlv2Parser.setOutputStream(mStringWriter);

			try {
				mDsmlv2Parser.initParser();
			} catch (Exception ex) {
				myLog.logerror(sResHash.getString("CONNECTOR.DSMLSERVER.DSML.INITEXCEPTION.WARNING", ex.toString()), ex);
				httpStatusResponse(HTTP_INTERNAL_SERVER_ERROR);
				terminate();
				return;
			}

			mInitializeSuccessfull = true;
			return;
		}

		String strPort = getParam(PARAMETER_DSML_PORT);
		if (strPort == null || strPort.trim().length() == 0) {
			throw new Exception(sResHash.getString("CONNECTOR.DSMLSERVER.MISSINGREQUIRED.DSMLPORT.ERROR"));
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
			mRequireClientAuth = false;
			String strRequireClientAuth = getParam(PARAM_REQUIRE_CLIENT_AUTH);
			if (strRequireClientAuth != null) {
				mRequireClientAuth = Boolean.valueOf(strRequireClientAuth).booleanValue();
			}

			logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.LISTENING.DSMLSSL.INFO", Integer.valueOf(mPort)));
			mServerSocket = getSSLServerSocket(mPort, backlog, mRequireClientAuth);
		} else {
			logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.LISTENING.DSML.INFO", Integer.valueOf(mPort)));
			if (backlog > 0) {
				mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort, backlog);
			} else {
				mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort);
			}
		}
	}

	/**
	 * Retrieves the server connector.
	 *
	 * @return the server connector if this connector is handling an DSML client
	 *         session.
	 */
	public DSMLv2SOAPServerConnector getServerConnector() {
		return mServerConnector;
	}

	/**
	 * Sets the server connector for this connector.
	 *
	 * @param aServerConnector
	 *            the server connector.
	 */
	public void setServerConnector(DSMLv2SOAPServerConnector aServerConnector) {
		mServerConnector = aServerConnector;
	}

	/**
	 * Checks whether connector is waiting for a client connection
	 *
	 * @return true if this connector is currently waiting for a client
	 *         connection.
	 */
	public boolean isAccepting() {
		return mIsAccepting;
	}

	/**
	 * @return true if this connector has the termination flag set.
	 */
	public boolean isTerminating() {
		return mTerminationRequested;
	}

	/**
	 * Terminate the connector.
	 *
	 * @throws Exception
	 *             if a termination error occurs.
	 */
	public void terminate() throws Exception {

		super.terminate();

		if (mClientSocket != null) {
			mClientSocket.close();
			mClientSocket = null;
			logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.CONNCLOSED.INFO"));
		}

		if (mServerSocket != null) {
			mServerSocket.close();
			mServerSocket = null;
		}
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
			mTerminationRequested = true;
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.TERMINATE.SRVRECEIVED.INFO"));
				// At this point we are the server
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.CREATE.DUMMYCONN.INFO", new Object[] {
						mServerSocket.getInetAddress(), Integer.valueOf(mServerSocket.getLocalPort()) }));
			}
			try {
				new Socket(mServerSocket.getInetAddress(), mServerSocket.getLocalPort());
			} catch (IOException e) {
				logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.IOEXCEPTION.UNBLOCKINGSERVER.WARNING", e.toString()));
			}

		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.DSMLSERVER.SERVERTERM.INFO"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.DSMLSERVER.SENDING.TERMREQUEST.INFO"));
			}
			getServerConnector().terminateServer();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorInterface getNextClient() throws Exception {

		if (mServerSocket == null) {
			throw new Exception(sResHash.getString("CONNECTOR.DSMLSERVER.NOTSERVER.SESSION.ERROR"));
		}

		Socket clientSocket;

		if (!isTerminating()) {
			mIsAccepting = true;
			clientSocket = mServerSocket.accept();
			mIsAccepting = false;
		} else {
			logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.CONNTERMINATED.INFO"));
			return null;
		}

		if (isTerminating()) {
			logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.CONNTERMINATED.INFO"));
			clientSocket.close();
			clientSocket = null;
			return null;
		}

		DSMLv2SOAPServerConnector clientSession = new DSMLv2SOAPServerConnector();
		clientSession.setServerConnector(this);
		clientSession.setConfiguration(getConfiguration());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		try {
			clientSession.initialize(clientSocket);
		} catch (Exception e) {
			try {
				clientSocket.close();
			} catch (Exception e1) {
				clientSocket = null; // dummy statement
			}
		}
		if (!clientSession.mInitializeSuccessfull)
			throw new RetryEntryException("");

		return clientSession;
	}

	/**
	 * Gets the next DSML request, sent by the client.
	 *
	 * @return the client request as an Entry object.
	 * @throws Exception
	 *             if communication/parsing error occurs.
	 */
	public Entry getNextEntry() throws Exception {

		if (mRejectClientAuth || !mInitializeSuccessfull) {
			return null;
		}

		Entry requestEntry = mDsmlv2Parser.readEntry();

		if (requestEntry == null) {
			if (!mMsgChunked) {

				mDsmlv2Parser.closeParser();
				String batchResponse = mStringWriter.toString();

				Entry batchResponseEntry = new Entry();
				batchResponseEntry.setAttribute(ATTR_NAME_HTTP_CONTENT_TYPE, "text/xml");
				batchResponseEntry.setAttribute(ATTR_NAME_HTTP_BODY, batchResponse);

				mHttpParser.writeEntry(batchResponseEntry);
				mHttpParser.closeParser();
			} else {
				if (mSoapBinding) {
					writeChunk(SOAP_END);
				} else {
					writeChunk(BATCH_END);
				}
				finishChunk();
			}

			return null;
		} else {
			String requestOperation = requestEntry.getString(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION);

			if (!mMsgChunked) {
				if (requestOperation.equalsIgnoreCase(Dsmlv2Parser.OPERATION_SEARCH_REQUEST)) {
					mResultEntries = new Vector<Entry>();
				}
			} else {
				if (!mBatchOpened) {

					initChunk(mClientSocket.getOutputStream());
					if (mSoapBinding) {
						writeChunk(SOAP_BEGIN);
					} else {
						writeChunk(BATCH_BEGIN);
					}

					mBatchOpened = true;
				}

				if (requestOperation.equalsIgnoreCase(Dsmlv2Parser.OPERATION_SEARCH_REQUEST)) {

					String beginSearch = "<searchResponse>";
					String requestID = requestEntry.getString(Dsmlv2Parser.ATTR_NAME_DSML_REQUEST_ID);
					if (requestID != null && requestID.length() > 0) {
						beginSearch = "<searchResponse requestID=\"" + requestID + "\">";
					}

					writeChunk(beginSearch);
				}
			}
		}

		return requestEntry;
	}

	/**
	 * Sets an entry object in the set of results that will be send back to the
	 * client when the {@link #replyEntry(Entry)} method is called. If the
	 * connector is configured to transport the data as chunks then the entry
	 * will be serialized as a DSML message and will be send right to the
	 * client.
	 *
	 * @param aEntry
	 *            The entry object that will be sent as a part of the result to
	 *            the client.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void putEntry(Entry aEntry) throws Exception {

		if (!mMsgChunked) {
			mResultEntries.add(aEntry);
		} else {

			SearchResultEntry searchResultEntry = Dsmlv2Parser.getSearchResultEntry(aEntry);
			Element element = Dsmlv2Parser.dsmlMessageToElement(searchResultEntry);
			String chunk = mDsmlv2Parser.xmlNodeToString(element, true);
			writeChunk(chunk);
		}
	}

	/**
	 * If the connector is not configured for communicating with clients using
	 * chunks then this method writes the entries stored in the local result set
	 * as a whole DSML search response message back to the client. If chunking
	 * is used or the response is not a result of a search request then the
	 * provide entry is serialized and send right to the client.
	 *
	 * @param aResponseEntry
	 *            the entry that will be sent back to the client.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void replyEntry(Entry aResponseEntry) throws Exception {

		String requestOperation = aResponseEntry.getString(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION);
		String responseOperation = Dsmlv2Parser.getResponseOperation(requestOperation);

		aResponseEntry.setAttribute(Dsmlv2Parser.ATTR_NAME_DSML_OPERATION, responseOperation);

		if (!mMsgChunked) {
			if (responseOperation.equalsIgnoreCase(Dsmlv2Parser.OPERATION_SEARCH_RESPONSE)) {
				if (mResultEntries != null && mResultEntries.size() > 0) {
					Attribute resultEntries = aResponseEntry.newAttribute(Dsmlv2Parser.ATTR_NAME_ACUMULATOR);

					for (int i = 0; i < mResultEntries.size(); i++) {
						resultEntries.addValue(mResultEntries.elementAt(i));
					}
				}

				mResultEntries = null;
			}

			mDsmlv2Parser.writeEntry(aResponseEntry);
		} else {

			String chunk = null;

			if (responseOperation.equalsIgnoreCase(Dsmlv2Parser.OPERATION_SEARCH_RESPONSE)) {

				LdapResult ldapResult = Dsmlv2Parser.getSearchResultDoneFromEntry(aResponseEntry);
				Element element = Dsmlv2Parser.dsmlMessageToElement(ldapResult);
				chunk = mDsmlv2Parser.xmlNodeToString(element, true);
				chunk += "</searchResponse>";
			} else {
				mDsmlv2Parser.writeEntry(aResponseEntry);
				Node rootNode = mDsmlv2Parser.getSingleNode();
				chunk = mDsmlv2Parser.xmlNodeToString(rootNode, true);
			}

			writeChunk(chunk);
		}
	}

	/**
	 * Retrieves username.
	 *
	 * @return the authenticated username.
	 */
	public String getUserName() {
		return mUsername;
	}

	/**
	 * Retrieves password.
	 *
	 * @return the password the user was authenticated with.
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * Reject the client authentication, send the Forbidden page and terminate
	 * the connection.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void rejectClientAuthentication() throws Exception {
		if (mHttpBasicAuth && mInitializeSuccessfull) {
			mRejectClientAuth = true;
			mHttpParser.httpForbidden();
			terminate();
		}
	}

	/**
	 * Writes the status response
	 *
	 * @param aStatusCode
	 *            status code to be written
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private void httpStatusResponse(String aStatusCode) throws IOException {
		if (mHttpParser != null) {
			mHttpParser.getWriter().write("HTTP/1.1 " + aStatusCode + "\r\n");
			mHttpParser.getWriter().write("Content-Length: 0\r\n\r\n");
			mHttpParser.getWriter().flush();
		}
	}

	/**
	 * Verifies user name and password for the request entry.
	 *
	 * @param aHttpParser
	 *            {@link HTTPParser} instance.
	 * @param aEntry
	 *            {@link Entry} instance
	 * @return <code>true</code> if request is accepted , <code>false</code>
	 * @throws Exception
	 */
	private boolean doAuthentication(HTTPParser aHttpParser, Entry aEntry) throws Exception {

		boolean authResult = false;
		int retryCount = 0;

		do {
			mUsername = (String) aHttpParser.getProp(aEntry, ATTR_NAME_HTTP_REMOTE_USER);
			mPassword = (String) aHttpParser.getProp(aEntry, ATTR_NAME_HTTP_REMOTE_PASSWORD);

			if (mUsername == null || mUsername.length() == 0 || mPassword == null || mPassword.length() == 0) {

				if (retryCount >= 3) {
					logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.AUTHFAILED.WARNING"));
					authResult = false;
					break;
				}

				aHttpParser.httpAuthenticationRequest((String) getParam(PARAMETER_AUTH_REALM));
				retryCount++;
			} else {
				logmsg(sResHash.getString("CONNECTOR.DSMLSERVER.USERNAME.INFO", mUsername));
				authResult = true;
				break;
			}
		} while ((aEntry = aHttpParser.readEntry()) != null);

		return authResult;
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
	 * Provides methods for writing a chunk to a output stream.
	 */
	private static class DSMLv2ChunkedWriter extends Writer {

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
		 * Construct a DSMLv2ChunkedWriter with a default chunk size of 256
		 * bytes.
		 *
		 * @param stream
		 *            The stream that chunks will be written to
		 */
		public DSMLv2ChunkedWriter(OutputStream stream) {
			maxChunkSize = 256;
			init(stream);
		}

		/**
		 * Initialize DSMLv2ChunkedWriter
		 *
		 * @param stream
		 *            The stream to which chunks will be written
		 */
		private void init(OutputStream stream) {
			// This is the common function to be shared in both constructors....
			this.stream = stream;

			// The buffer for the chunk
			//
			buffer = new ByteArrayOutputStream();

			try {
				bufferWriter = new OutputStreamWriter(buffer, "UTF8");
				writer = new OutputStreamWriter(this.stream, "UTF8");
			} catch (UnsupportedEncodingException e) {
				bufferWriter = new OutputStreamWriter(buffer);
				writer = new OutputStreamWriter(this.stream);
			}
		}

		/**
		 * Write chunk message to writer
		 *
		 * @return 0
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
		 * @throws IOException
		 *             if an I/O error occurs.
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
			int remaining = length - offset;

			while (remaining > 0) {

				int numToWrite = (remaining > maxChunkSize) ? maxChunkSize : remaining;
				if ((buffer.size() + numToWrite) > maxChunkSize) {
					numToWrite = maxChunkSize - buffer.size();
				}

				bufferWriter.write(cbuf, offset, numToWrite);
				bufferWriter.flush();
				remaining -= numToWrite;
				offset += numToWrite;

				if (buffer.size() >= maxChunkSize) {
					writeChunk();
					buffer.reset();
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

		OutputStreamWriter chunkOutputWriter = new OutputStreamWriter(aOutputStream, "UTF8");

		chunkOutputWriter.write("HTTP/1.1 200 OK\r\n");
		chunkOutputWriter.write("Content-Type: text/xml\r\n");
		chunkOutputWriter.write("Transfer-Encoding: chunked\r\n");
		chunkOutputWriter.write("Connection: keep-alive\r\n");
		chunkOutputWriter.write("\r\n");
		chunkOutputWriter.flush();

		mChunkDsmlv2Writer = new DSMLv2ChunkedWriter(aOutputStream);
	}

	/**
	 * Writes a string.
	 *
	 * @param aChunk
	 *            String to be written
	 * @throws Exception
	 *             If an I/O error occurs
	 */

	private void writeChunk(String aChunk) throws Exception {
		mChunkDsmlv2Writer.write(aChunk);
	}

	/**
	 * Close the writer. Any remaining buffered data will be written as a chunk
	 * and a final zero-length chunk will be written to terminate the transfer.
	 *
	 * @throws Exception
	 *             if an I/O error occurs.
	 */
	private void finishChunk() throws Exception {
		mChunkDsmlv2Writer.close();
	}

	/**
	 * Version information.
	 *
	 * @return the version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

}
