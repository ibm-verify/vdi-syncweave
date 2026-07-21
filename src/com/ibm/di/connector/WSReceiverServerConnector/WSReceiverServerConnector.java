/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.WSReceiverServerConnector;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocket;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.ibm.di.connector.WSReceiverServerConnector.BufferedNonBlockingInputStream;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.ResourceHash;

/**
 * The Web Service Receiver Server Connector is part of the IBM Tivoli Directory
 * Integrator Web Services suite. This Connector is basically an HTTP Server
 * specialized for servicing SOAP requests over HTTP. It operates in Server mode
 * only.
 * 
 * This version is compatible with Axis2.
 */
public class WSReceiverServerConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "wsreceiverserverconnector";

	/**
	 * Parameter name.The port number the service is running (listening) on
	 */
	protected static final String PARAM_TCP_PORT = "tcpPort";

	/**
	 * Parameter name.Backlog. The maximum queue length for incoming
	 * connections. If a connection request arrives when the queue is full, the
	 * connection will be refused.
	 */
	protected static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * System backlog.
	 */
	protected static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * Parameter name.Parameter name.Specifies the type of the SOAP Response
	 * message output from the AssemblyLine
	 */
	protected static final String PARAM_RETURN_XML_TYPE = "returnXMLType";

	/**
	 * Parameter name.Specifies the type of the SOAP Request message input to
	 * the AssemblyLine
	 */
	protected static final String PARAM_INPUT_TYPE = "inputType";

	/**
	 * Parameter name.Enable/Disable SSL.If checked the server will only accept
	 * SSL (https) connections
	 */
	protected static final String PARAM_USE_SSL = "useSSL";

	/**
	 * Parameter name.Is client authentication required.If checked clients will
	 * be required to provide SSL certificates for client authentication
	 */
	protected static final String PARAM_REQUIRE_CLIENT_AUTH = "requireClientAuth";

	/**
	 * Parameter name.Specifies whether the $operation attribute of the op-entry
	 * is set to the name of the web service operation.
	 */
	protected static final String PARAM_TAG_OPENTRY = "tagOpEntry";

	/**
	 * The port number the service is running (listening) on.
	 */
	protected int mPort = -1;

	/**
	 * Specifies the type of the SOAP Request message input to the AssemblyLine
	 */
	protected String mInputXMLType = null;

	/**
	 * Specifies the type of the SOAP Response message output from the
	 * AssemblyLine
	 */
	protected String mReturnXMLType = null;

	/**
	 * If checked the server will only accept SSL (https) connections
	 */
	protected boolean mUseSSL = false;

	/**
	 * If checked clients will be required to provide SSL certificates for
	 * client authentication
	 */
	protected boolean mRequireClientAuth = false;

	/**
	 * Specifies whether the $operation attribute of the op-entry is set to the
	 * name of the web service operation.
	 */
	protected boolean mTagOpEntry = false;

	/**
	 * Client socket
	 * 
	 * @see Socket
	 */
	protected Socket mSocket = null;

	/**
	 * Server socket.
	 * 
	 * @see ServerSocket
	 */
	protected ServerSocket mServerSocket = null;

	/**
	 * Server Connector
	 */
	protected WSReceiverServerConnector mServerConnector;

	/**
	 * Charset constant.
	 */
	protected String CHARSET = "charset";

	/**
	 * Is termination requested.
	 */
	private boolean terminationRequested = false;

	/**
	 * Is the server accepting the next client.
	 */
	private boolean isAccepting = false;

	/**
	 * SOAP response - attribute name.
	 */
	public static final String ATTR_SOAP_RESPONSE = "soapResponse";

	/**
	 * Host - attribute name.
	 */
	public static final String ATTR_HOST = "host";

	/**
	 * Requested resource - attribute name.
	 */
	public static final String ATTR_REQUESTED_RESOURCE = "requestedResource";

	/**
	 * SOAP request - attribute name.
	 */
	public static final String ATTR_SOAP_REQUEST = "soapRequest";

	/**
	 * SOAP action - attribute name.
	 */
	public static final String ATTR_SOAP_ACTION = "soapAction";

	/**
	 * WSDL requested - attribute name.
	 */
	public static final String ATTR_WSDL_REQUESTED = "wsdlRequested";

	/**
	 * Response content type - attribute name.
	 */
	public static final String ATTR_RESPONSE_CONTENT_TYPE = "responseContentType";

	/**
	 * Public constant representing XML String format.
	 */
	public static final String XML_STRING = "String";
	/**
	 * Public constant representing XML DOM format.
	 */
	public static final String XML_DOM = "DOMElement";

	/**
	 * Is authentication already read.
	 */
	private boolean authRead = false;

	/**
	 * Is authentication requirement set. Default false
	 */
	private boolean authRequired = false;

	/**
	 * Buffer size - 4096.
	 */
	private static final int BUFSIZE = 4096;

	/**
	 * Holds the version of the HTTP protocol represented as byte array.
	 */
	private static byte HTTP[];

	/**
	 * The server successful respond converted in byte array.
	 */
	private static byte OK[];

	/**
	 * The byte array representing the CRLFCRLF sequence, used for separating
	 * the headers.
	 */
	private static byte SEPARATOR[];
	static {
		try {
			HTTP = "HTTP/1.0 ".getBytes(WebServiceClient.ENCODING_LATIN_1);
			// In Axis2, we need to provide our own "OK" message
			OK = "200 OK".getBytes(WebServiceClient.ENCODING_LATIN_1);
			SEPARATOR = "\r\n\r\n".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * WSDL suffix
	 */
	public static final String WSDL_ID = "?WSDL";

	/**
	 * Content type - "text/xml"
	 */
	public static final String CONTENT_TYPE_XML = "text/xml";
	/**
	 * Content type - "text/html"
	 */
	public static final String CONTENT_TYPE_HTML = "text/html";
	/**
	 * Boolean value represented as String.
	 */
	protected static final String TRUE = "true";

	/**
	 * Boolean value represented as String.
	 */
	protected static final String FALSE = "false";

	/**
	 * Next entry is not available. Default false
	 */
	protected boolean mNoNextEntry = false;

	/**
	 * {@link SAXParserFactory}
	 */
	protected SAXParserFactory mSAXParserFactory = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * Default constructor
	 */
	public WSReceiverServerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.SERVER_MODE });
	}

	/**
	 * Initialize the connector. The connector may be passed a parameter of any
	 * kind by the user. Assigns parameters specified by the user to the
	 * connector
	 * 
	 * @param obj
	 *            User provided parameter
	 * @throws Exception
	 *             if the initialization of this connector fails.
	 */
	public void initialize(Object obj) throws Exception {
		if (mSocket != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.EXITINIT.INFO"));
			}
			return;
		}

		if (obj instanceof Socket) {

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.USESOCKET.INFO"));
			}

			mSocket = (Socket) obj;

			mInputXMLType = (String) getParam(PARAM_INPUT_TYPE);

			if (mInputXMLType != null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.INPUTTYPE.INFO", mInputXMLType));
				}
				mInputXMLType = mInputXMLType.trim();
			}

			if (mInputXMLType == null || (!mInputXMLType.equalsIgnoreCase(XML_DOM) && !mInputXMLType.equalsIgnoreCase(XML_STRING))) {
				throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.INVALID.PARAMETER.VALUE", mInputXMLType));
			}

			mReturnXMLType = (String) getParam(PARAM_RETURN_XML_TYPE);

			if (mReturnXMLType != null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.RETURNXMLTYPE.INFO", mReturnXMLType));
				}
				mReturnXMLType = mReturnXMLType.trim();
			}
			if (mReturnXMLType == null
					|| (!mReturnXMLType.equalsIgnoreCase(XML_DOM) && !mReturnXMLType.equalsIgnoreCase(XML_STRING))) {
				throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.INVALID.RETURNXMLTYPE", mReturnXMLType));
			}

			authRequired = Boolean.valueOf((String) getParam("useBasicAuth")).booleanValue();
		} else {
			String port = (String) getParam(PARAM_TCP_PORT);

			if (port == null) {
				throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.MISSING.TCPPORT"));
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.TCPPORT.INFO", port));
				}
			}
			mPort = Integer.parseInt(port);

			String useSSL = (String) getParam(PARAM_USE_SSL);

			if (useSSL == null) {
				throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.MISSING.USESSL"));
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.USESSL.INFO", useSSL));
				}
			}

			mUseSSL = Boolean.valueOf(useSSL).booleanValue();

			String requireClientAuth = (String) getParam(PARAM_REQUIRE_CLIENT_AUTH);

			if (requireClientAuth == null) {
				// Setting a default value for old IBM Tivoli Directory
				// Integrator 6.0 Configurations
				// without this parameter
				requireClientAuth = "false";
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.SET.DEFAULT.REQUIRECLIENTAUTH", requireClientAuth));
				}
			}
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.REQUIRECLIENTAUTH.INFO", requireClientAuth));
			}

			mRequireClientAuth = Boolean.valueOf(requireClientAuth).booleanValue();

			int backlog = -1; // The maximum length of the queue.
			String strBacklog = getParam(PARAM_TCP_BACKLOG);
			if (strBacklog == null || strBacklog.trim().length() == 0) {
				strBacklog = System.getProperty(PARAM_SYSTEM_TCP_BACKLOG);
			}

			if (strBacklog != null && strBacklog.trim().length() > 0) {
				backlog = Integer.parseInt(strBacklog);
			}

			if (mUseSSL) {
				mServerSocket = getSSLServerSocket(mPort, backlog, mRequireClientAuth);
			} else {
				if (backlog > 0) {
					mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort, backlog);
				} else {
					mServerSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(mPort);
				}
			}

		}

		String tagOperation = (String) getParam(PARAM_TAG_OPENTRY);
		if (tagOperation != null) {
			mTagOpEntry = Boolean.valueOf(tagOperation).booleanValue();
		} else {
			mTagOpEntry = false;
		}

		if (mTagOpEntry) {
			mSAXParserFactory = SAXParserFactory.newInstance();
		}
	}

	/**
	 * The method creates and returns {@link ServerSocket} for the specified
	 * port , the maximum queue length for connections (backlog) and sets if it
	 * is required authentication for the connection.
	 * 
	 * @param aPort
	 *            int
	 * @param aBacklog
	 *            int
	 * @param aRequireClientAuth
	 *            boolean
	 * @return ServerSocket
	 * @throws IOException
	 */
	private ServerSocket getSSLServerSocket(int aPort, int aBacklog, boolean aRequireClientAuth) throws IOException {

		SSLServerSocket sslServerSocket;
		if (aBacklog > 0) {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort, aBacklog);
		} else {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort);
		}

		sslServerSocket.setNeedClientAuth(aRequireClientAuth);

		return sslServerSocket;
	}

	/**
	 * Server mode - return a clone of self that handles the next client
	 * instance when running in server mode. The returned connector may be used
	 * in its own thread to handle a "client" request so if the returned
	 * instance is returned more than once it must be thread safe.
	 * 
	 * @return the clone of itself
	 * @throws Exception
	 *             : if the server socket is not specified.
	 * 
	 */
	public ConnectorInterface getNextClient() throws Exception {
		if (mServerSocket == null) {
			throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.NOTSERVERSESSION.EXCEP"));
		}

		if (terminationRequested) {
			logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.TERMINATE1.INFO"));
			return null;
		}

		isAccepting = true;

		mSocket = mServerSocket.accept();

		isAccepting = false;

		if (terminationRequested) {
			logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.TERMINATE2.INFO"));
			mSocket.close();
			return null;
		}

		// Dramatically enhances speed when running over a LAN
		mSocket.setTcpNoDelay(true);

		WSReceiverServerConnector clientSession = new WSReceiverServerConnector();
		clientSession.setServerConnector(this);
		clientSession.setConfiguration(getConfiguration());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		clientSession.initialize(mSocket);
		return clientSession;
	}

	/**
	 * @return WSReceiverServerConnector
	 */
	public WSReceiverServerConnector getServerConnector() {
		return mServerConnector;
	}

	/**
	 * Sets the {@link WSReceiverServerConnector}
	 * 
	 * @param aServerConnector
	 *            WSReceiverServerConnector
	 */
	public void setServerConnector(WSReceiverServerConnector aServerConnector) {
		mServerConnector = aServerConnector;
	}

	/**
	 * Interrupts and shuts down the Connector if it runs in Server Mode. Does
	 * not have effect if the Connector is not running in Server Mode.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void terminateServer() throws Exception {
		if (isAccepting()) {

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.TERMINATERCV.INFO"));
			}

			// At this point we are the server
			terminationRequested = true;

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.DUMMY.INFO", new Object[] { mServerSocket.getInetAddress(),
						"" + mServerSocket.getLocalPort() }));
			}

			new Socket(mServerSocket.getInetAddress(), mServerSocket.getLocalPort());
		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector() == null) {
				throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.NOSRVCONNECTOR.EXCEP"));
			}

			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.WSRECSERVER.ALREADYTERM.INFO"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.SENDINGTERM.INFO"));
			}

			getServerConnector().terminateServer();
		}
	}

	/**
	 * Terminate the connector. This function closes all connection and releases
	 * all resources used by the connector.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
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
	 * Returns the next Entry from the connector. The entry is populated with
	 * attributes and values from the next entry in the input set.
	 * 
	 * @return the next Entry, or null if no more data
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry getNextEntry() throws Exception {
		if (mSocket == null) {
			throw new Exception(sResHash.getString("CONNECTOR.WSRECSERVER.NOTCLIENTSESSION.EXCEP"));
		}

		if (mNoNextEntry) {
			return null;
		}

		byte buf[] = new byte[BUFSIZE];

		StringBuffer soapAction = new StringBuffer();
		StringBuffer httpRequest = new StringBuffer();
		StringBuffer fileName = new StringBuffer();
		StringBuffer cookie = new StringBuffer();
		StringBuffer cookie2 = new StringBuffer();
		StringBuffer authInfo = new StringBuffer();
		StringBuffer contentType = new StringBuffer();
		StringBuffer contentLocation = new StringBuffer();
		StringBuffer host = new StringBuffer();

		BufferedNonBlockingInputStream is = new BufferedNonBlockingInputStream();

		is.setInputStream(mSocket.getInputStream());
		// parse all headers into hashtable
		int contentLength = HTTPHeaders.parseHeaders(is, buf, contentType, contentLocation, soapAction, httpRequest, fileName,
				cookie, cookie2, authInfo, host);
		is.setContentLength(contentLength);

		// Print the HTTP header info if we are in debug mode.
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPCONTENTINFO.START"));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPCONTENTLENGTH", new Object[] { "Content-Length",
					"" + contentLength }));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPCONTENTTYPE", new Object[] { "Content-Type",
					contentType }));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPCONTENTLOC", new Object[] { "Content-Location",
					contentLocation }));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPSOAPACTION", new Object[] { "SOAPAction",
					soapAction }));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPHOSTNAME", new Object[] { "Host", host }));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPCONTENTINFO.END"));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPMETHOD", httpRequest));
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPRESOURCE", fileName));
		}

		Entry entry = new Entry();
		if (fileName.toString().toUpperCase().endsWith(WSDL_ID)) {
			logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.PROCESSWSDLREQ.INFO"));
			entry.setAttribute(ATTR_WSDL_REQUESTED, TRUE);
		} else {
			logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.PROCESSSOAPREQ.INFO"));

			String soapRequest = WebServiceClient.readHttpContent(is, contentType.toString(), contentLength);

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.WSRECSERVER.PROCESSEDSOAPREQ.INFO", soapRequest));
			}

			// Extract only the body content to prevent recursive nesting
			soapRequest = extractSoapBodyContent(soapRequest);

			if (debugMode()) {
				debug("Extracted SOAP body content: " + soapRequest);
			}

			entry.setAttribute(ATTR_WSDL_REQUESTED, FALSE);
			entry.setAttribute(ATTR_HOST, host);
			entry.setAttribute(ATTR_REQUESTED_RESOURCE, fileName);

			Object soapRequestObject = soapRequest;
			if (mInputXMLType.equalsIgnoreCase(XML_DOM)) {
				soapRequestObject = WebServiceClient.getAsDOM(soapRequest);
			}
			entry.setAttribute(ATTR_SOAP_REQUEST, soapRequestObject);
			entry.setAttribute(ATTR_SOAP_ACTION, soapAction.toString());

			if (mTagOpEntry) {
				String operationName = null;

				SAXParser parser = mSAXParserFactory.newSAXParser();
				try {
					parser.parse(new ByteArrayInputStream(soapRequest.getBytes("UTF-8")), new OperationHandler());
				} catch (SAXException e) {
					if (e instanceof OperationNameFoundException) {
						operationName = e.getMessage();
					}
				}

				if (operationName != null) {
					Object context = getContext();
					if (context != null && context instanceof AssemblyLine) {
						AssemblyLine al = (AssemblyLine) context;
						Entry opEntry = al.getOpEntry();
						opEntry.setAttribute(AssemblyLine.OPENTRY_OPERATION, operationName);
					} else {
						logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.OPATTR.EXCEP"));
					}
				} else {
					logmsg(sResHash.getString("CONNECTOR.WSRECSERVER.OPNAME.EXCEP"));
				}
			}
		}

		if (authRequired && !authRead) {
			String str = "";
			int ix;

			if (authInfo != null && authInfo.length() > 0) {
				str = UserFunctions.base64Decode(authInfo.toString(), "iso-8859-1");
				ix = str.indexOf(':');
			} else if (fileName.toString().toUpperCase().endsWith(WSDL_ID)) {
				ix = -1;
			} else {
				httpAuthenticationRequest(getParam("authRealm"));
				StringBuffer header = getHeaders();
				str = getAuthorization(header);
				ix = (str == null) ? -1 : str.indexOf(':');
			}

			if (ix > -1) {
				entry.setAttribute("http.username", str.substring(0, ix));
				entry.setAttribute("http.password", str.substring(ix + 1));
			} else {
				entry.setAttribute("http.username", "");
				entry.setAttribute("http.password", "");
			}
			authRead = true;
		}

		mNoNextEntry = true;
		return entry;
	}

	/**
	 * Send a reply to the connector.
	 * 
	 * @param conn
	 *            Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	public void replyEntry(Entry conn) throws Exception {
		OutputStream out = mSocket.getOutputStream();
		if (authRequired) {
			String auth = "false";
			if (conn.getAttribute("http.credentialsValid") != null) {
				auth = conn.getString("http.credentialsValid");
			}

			boolean authorized = Boolean.valueOf(auth).booleanValue();
			// if user is not authorized, send "Not Authorized" message and exit
			// replyEntry method
			if (!authorized) {
				byte[] httpForbidden = "HTTP/1.1 401 Forbidden".getBytes(WebServiceClient.ENCODING_LATIN_1);
				byte[] negativeResponse = "Not Authorized".getBytes(WebServiceClient.ENCODING_UTF8);

				out.write(httpForbidden);
				out.write(("\r\n" + "Content-Length" + ": " + negativeResponse.length)
						.getBytes(WebServiceClient.ENCODING_LATIN_1));
				out.write(SEPARATOR);

				out.write(negativeResponse);
				out.flush();
				mSocket.close();
				return;
			}
		}

		byte[] status = OK;
		Attribute soapResponseAttr = conn.getAttribute(ATTR_SOAP_RESPONSE);
		String soapResponse = "";
		if (soapResponseAttr != null) {
			Object soapResponseObject = soapResponseAttr.getValue(0);
			if (mReturnXMLType.equalsIgnoreCase(XML_DOM)) {
				soapResponse = WebServiceClient.getAsString((org.w3c.dom.Node) soapResponseObject);
			} else {
				soapResponse = (String) soapResponseObject;
			}
		} else {
			soapResponse = "This web service returned nothing.";
		}

		String response = soapResponse;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.HTTPSTATUSCODE.INFO", new String(status)));
		}

		Attribute attrResponseContentType = conn.getAttribute(ATTR_RESPONSE_CONTENT_TYPE);
		String responseContentType = CONTENT_TYPE_XML;
		if (attrResponseContentType != null) {
			Object objResponseContentType = attrResponseContentType.getValue(0);
			if (objResponseContentType instanceof String) {
				responseContentType = (String) objResponseContentType;
			}
		}

		if (responseContentType.indexOf(CHARSET) == -1) {
			if (responseContentType.indexOf(';') == -1) {
				responseContentType += ";";
			}
			responseContentType += CHARSET + "=" + WebServiceClient.ENCODING_UTF8;
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.CONTENTTYPE.INFO", responseContentType));
		}

		out.write(HTTP);
		out.write(status);

		out.write(("\r\n" + "Content-Type" + ": " + responseContentType)
				.getBytes(WebServiceClient.ENCODING_LATIN_1));

		byte[] responseBytes = response.getBytes(WebServiceClient.ENCODING_UTF8);
		int responseContentLength = responseBytes.length;
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.CONTENTLENGTH.INFO", "" + responseContentLength));
		}
		out.write(("\r\n" + "Content-Length" + ": " + responseContentLength)
				.getBytes(WebServiceClient.ENCODING_LATIN_1));
		out.write(SEPARATOR);
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.WSRECSERVER.RESPONSE.INFO", response));
		}
		out.write(responseBytes);
		out.flush();

		mSocket.close();
	}

	/**
	 * @return true if the server is accepting connection, false otherwise.
	 */
	public boolean isAccepting() {
		return isAccepting;
	}

	/**
	 * @return true if a termination request is sent , false otherwise.
	 */
	public boolean isTerminating() {
		return terminationRequested;
	}

	/**
	 * @return version information.
	 */
	public String getVersion() {
		return "2.2-di11.0.0.1 %I%, 20%E%";
	}

	/**
	 * This method is supposed to be used in scripts for reading WSDL files.
	 * This method is not used by this Connector. It is a convenience method
	 * only.
	 *
	 * @param aFileName
	 *            String
	 * @return String
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public String readFile(String aFileName) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(aFileName);
		int fileLen = fileInputStream.available();
		byte[] fileByteArray = new byte[fileLen];
		fileInputStream.read(fileByteArray, 0, fileLen);
		fileInputStream.close();
		return new String(fileByteArray);
	}
	
	/**
		* Extracts the SOAP Body content from a complete SOAP envelope.
		* If the input is already just body content (no envelope), returns it as-is.
		* This prevents recursive nesting when the client sends a complete SOAP envelope.
		*
		* @param soapMessage The complete SOAP message
		* @return The body content without envelope wrapper
		* @throws Exception if parsing fails
		*/
	private String extractSoapBodyContent(String soapMessage) throws Exception {
		if (soapMessage == null || soapMessage.trim().isEmpty()) {
			return soapMessage;
		}
		
		// Check if this is a complete SOAP envelope
		if (!soapMessage.contains("<soapenv:Envelope") &&
			!soapMessage.contains("<soap:Envelope") &&
			!soapMessage.contains("<env:Envelope") &&
			!soapMessage.contains("<SOAP-ENV:Envelope")) {
			// Not a SOAP envelope, return as-is
			return soapMessage;
		}
		
		try {
			// Parse the SOAP message
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(soapMessage.getBytes("UTF-8")));
			
			// Find the Body element (namespace-aware)
			NodeList bodyNodes = doc.getElementsByTagNameNS("*", "Body");
			if (bodyNodes.getLength() == 0) {
				// No Body element found, return original
				if (debugMode()) {
					debug("No SOAP Body element found, returning original message");
				}
				return soapMessage;
			}
			
			Element bodyElement = (Element) bodyNodes.item(0);
			
			// Extract the body content (children of Body element)
			StringBuilder bodyContent = new StringBuilder();
			NodeList children = bodyElement.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					bodyContent.append(WebServiceClient.getAsString(child));
				}
			}
			
			String result = bodyContent.toString();
			if (debugMode()) {
				debug("Successfully extracted SOAP body content");
			}
			return result;
		} catch (Exception e) {
			// If extraction fails, log and return original
			if (debugMode()) {
				debug("Failed to extract SOAP body content: " + e.getMessage());
			}
			logmsg("Warning: Could not extract SOAP body content, using original message: " + e.getMessage());
			return soapMessage;
		}
	}
	
	/**
		* Sends HTTP authentication request to the client.
	 *
	 * @param realm
	 *            String
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void httpAuthenticationRequest(String realm) throws IOException {
		OutputStream out = mSocket.getOutputStream();
		String authHeader = "HTTP/1.1 401 Authorization Required\r\n" + "WWW-Authenticate: Basic realm=\"" + realm + "\"\r\n"
				+ "Content-Type: text/html\r\n" + "Content-Length: 0\r\n\r\n";
		out.write(authHeader.getBytes());
		out.flush();
	}

	/**
	 * Returns the HTTP headers.
	 *
	 * @return StringBuffer
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private StringBuffer getHeaders() throws IOException {
		InputStream in = mSocket.getInputStream();
		StringBuffer header = new StringBuffer();
		int c;
		boolean lastCR = false;
		boolean lastLF = false;
		while ((c = in.read()) != -1) {
			if (c == '\r') {
				if (lastCR && lastLF) {
					break;
				}
				lastCR = true;
				header.append((char) c);
			} else if (c == '\n') {
				if (lastCR && lastLF) {
					break;
				}
				lastLF = true;
				header.append((char) c);
			} else {
				lastCR = false;
				lastLF = false;
				header.append((char) c);
			}
		}
		return header;
	}

	/**
	 * Returns the authorization string from the HTTP headers.
	 *
	 * @param header
	 *            StringBuffer
	 * @return String
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported
	 */
	private String getAuthorization(StringBuffer header) throws java.io.UnsupportedEncodingException {
		String str = header.toString();
		int ix = str.indexOf("Authorization: Basic ");
		if (ix == -1) {
			return null;
		}
		ix += 21;
		int ix2 = str.indexOf("\r\n", ix);
		if (ix2 == -1) {
			return null;
		}
		String auth = str.substring(ix, ix2);
		return UserFunctions.base64Decode(auth, "iso-8859-1");
	}
}
