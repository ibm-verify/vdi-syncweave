/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.impl.auth.NTLMEngine;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.HTTPParser;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * HTTPCLientConnector provides a client side HTTP connection where the user can
 * use it in Iterator, Lookup and AddOnly mode. A parser may be specified in
 * which case the contents sent/received on the HTTP connection is parsed.
 *
 * If no parser is specified then all data is conveyed through the entry object.
 * All HTTP headers from the server is entered as is prefixing their names with
 * "http.". The "body" attribute contains the result from the server and also
 * provides the data to be submitted to the server if no parser is specified.
 */
public class HTTPClientConnector extends Connector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "httpclientconnector";

	/**
	 * The connector name as reported in the log files
	 */
	private static final String myName = "IBM HTTP Client Connector";

	/**
	 * Supported modes
	 */
	public static final String[] CONNECTOR_MODES = { 
		ConnectorConfig.ADDONLY_MODE,
		ConnectorConfig.ITERATOR_MODE, 
		ConnectorConfig.LOOKUP_MODE,
		ConnectorConfig.CALL_REPLY_MODE };
	
	/**
	 * Version info
	 */
	public static final String VERSION_INFO = "2.3-di11.0.0.1 2021-06-23";

	/**
	 * The HTTP Parser instance to do our conversions.
	 */
	private HTTPParser parser;

	/**
	 * Set this to true when the user's parser has data to parse
	 */
	private boolean userParserInitialized;

	/**
	 * Set this to true when we have sent a http request
	 */
	private boolean isConnected;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The timeout value in seconds for establishing a connection to the server
	 * and for receiving response from the server.
	 */
	private int timeout = 0;

	/**
	 * Parameter Name: {@value #PARAM_TIMEOUT}
	 */
	public static final String PARAM_TIMEOUT = "timeout";
	
	public static final String AUTHORIZATION = "http.Authorization";
	public static final String AUTHENTICATE = "http.WWW-Authenticate";

	/**
	 * The socket we opened for communication with the HTTP server
	 */
	private Socket socket;
	
	/**
	 * The host name the socket is connected to
	 */
	private String lastHost;
	
	/**
	 * The host name in the request, generally the same as lastHost.
	 * Used when doing SSL tunneling.
	 */
	private String requestHost;

	/**
	 * The port the socket is connected to
	 */
	private int lastPort;

	/**
	 * The protocol used when connecting the socket
	 */
	private String lastProtocol;
	
	/**
	 * True if the last request we sent re-used a persistent connection
	 */
	private boolean isTryingPersistentConnection;
	
	private boolean isUsingTunnel;

	private String clientCertificateAlias;
	
	private SSLSocketFactory sslSocketFactory = null;

	/**
	 * Constructor
	 */
	public HTTPClientConnector() {
		setName(myName);
		setModes(CONNECTOR_MODES);
	}

	/**
	 * This function is called when the connector is no longer needed by the
	 * user (AssemblyLine or script).
	 */
	@Override
	public void terminate() throws Exception {
		if (parser != null)
			parser.closeParser();
		parser = null;

		closeSocket();
		
		super.terminate();
	}

	/**
	 * This function is called once after the connector configuration file has
	 * been provided by the caller. 
	 */
	@Override
	public void initialize(Object o) throws Exception {
		// Just in case someone has forgotten to call terminate()
		closeSocket();

		String str = getParam(PARAM_TIMEOUT);
		if (str != null && !str.isEmpty()) {
			timeout = Integer.parseInt(str) * 1000;
		}

		parser = new HTTPParser();
		parser.setUseProperties(false);
		parser.setClientMode(true);
		parser.setContext(this);
		parser.setDebug(debugMode());
		String charSet = getParam("characterSet");
		if (charSet == null || charSet.isEmpty())
			charSet = "ISO-8859-1";
		parser.setParam("characterSet", charSet);
		isConnected = false;
		userParserInitialized = false;
	}

	/**
	 * This function is called when the connector operates in Iterator mode
	 * inside an AssemblyLine.
	 */
	@Override
	public void selectEntries() throws Exception {
		String url = getParam("url");
		if (hasParser() && url != null && url.length() > 0) {
			closeSocket();
			sendRequest(url, null);
			isConnected = true;
		}
	}

	/**
	 * This function is called to retrieve the next Entry from the connector.
	 * When there are no more entries to retrieve the function should return a
	 * null value indicating a logical end of file.
	 *
	 * If the connector has no parser this will never happen since we always
	 * make a HTTP connection every time getNextEntry is called.
	 *
	 */
	@Override
	public Entry getNextEntry() throws Exception {

		if (userParserInitialized)
			return getParser().readEntry();

		if (isConnected)
			return readResponse(null);

		return sendRequestReadResponse(getParam("url"), null);
	}

	/**
	 * The putEntry function is called during AddOnly or Update operations.
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {

		Entry e = sendRequestReadResponse(getParam("url"), entry);
		
		if (entry != null && e != null)
			entry.merge(e, true);
	}

	/**
	 * Performs a query/reply operations.
	 *
	 * @param entry
	 *            The data used in outgoing call
	 * @return The entry returned by the HTTP server
	 */
	@Override
	public Entry queryReply(Entry entry) throws Exception {

		return sendRequestReadResponse(getParam("url"), entry);
	}

	/**
	 * The findEntry function is called during Lookup operations.
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {

		StringBuffer urlStr = new StringBuffer();

		if (search.getScriptFilter() != null) {
			String s = search.getScriptFilter();
			// Remove leading "url=", if any.
			Matcher m = Pattern.compile("^\\s*[Uu][Rr][Ll]\\s*=\\s*(.*)").matcher(s);
			urlStr.append(m.matches() ? m.group(1) : s);
		} else if (search.size() == 1 && "url".equals(search.getFirstCriteriaName())) {
			urlStr.append(search.getFirstCriteriaValue());
		} else {
			int size = search.getCriteria().size();

			urlStr.append(getParam("url"));
			for (int i = 0; i < size; i++) {
				urlStr.append((i == 0) ? "?" : "&");
				urlStr.append(URLEncoder.encode(search.getCriteria(i).name,
						"UTF-8"));
				Object value = search.getCriteria(i).value;
				if (value != null) {
					urlStr.append("=");
					urlStr.append(URLEncoder.encode(value.toString(), "UTF-8"));
				}
			}
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.HTTPCLIENT.FINDURL.INFO",
					urlStr.toString()));
		}

		return sendRequestReadResponse(urlStr.toString(), null);

	}

	/**
	 * This function sends a request to the HTTP server. If a parser is
	 * configured the provided entry is passed to the parser to build the
	 * http.body contents.
	 *
	 * @param strURL
	 *            The default URL to use if entry does not contain a http.entry
	 *            attr/prop
	 * @param entry
	 *            The entry containing headers and body to send to the server
	 * @throws Exception
	 *             if an error occurs
	 */
	private void sendRequest(String strURL, Entry entry) throws Exception {
		// It is not required to provide headers and data.
		boolean hasEntry; // If we were given an entry != null
		if (entry == null) {
			entry = new Entry();
			hasEntry = false;
		} else {
			hasEntry = true;
		}

		// Setup the URL. Use entry's http.url if provided.
		String reqURL = entry.getString("http.url");
		if (reqURL == null)
			reqURL = strURL;
		if (reqURL == null)
			throw new com.ibm.di.exceptions.MissingConfigurationException(
					"HTTPCLientConnector", "http.url");

		// Retrieve host and port from URL
		URL url = new URL(reqURL);
		String host = url.getHost();
		int port = url.getPort();

		// Do not re-use socket if we connect to a new host
		if (lastProtocol == null || ! lastProtocol.equals(url.getProtocol()) ||
			lastPort != port ||
			(lastHost == null ? host != null : ! lastHost.equals(host))) {
			closeSocket();
		}

		lastHost = host;
		lastPort = port;
		lastProtocol = url.getProtocol();
		
		requestHost = entry.getString("http.host");
		if (requestHost == null || requestHost.length() == 0)
			requestHost = lastHost;

		// Set defaults if not explicitly provided
		if (entry.getString("http.url") == null)
			entry.setAttribute("http.url", url.toString());

		// Set the request method
		if (entry.getString("http.method") == null && hasConfigValue("method"))
			entry.setAttribute("http.method", getParam("method"));

		if (! "NTLM".equals(getParam("authMethod"))) {
			// Set the username/password for basic authentication. The
			// HTTP parser will generate a Basic "Authorization" header.
			if (entry.getString("http.remote_user") == null && hasConfigValue("username"))
				entry.setAttribute("http.remote_user", getParam("username"));

			if (entry.getString("http.remote_pass") == null && hasConfigValue("password"))
				entry.setAttribute("http.remote_pass", getParam("password"), true);
		}

		if (hasConfigValue("inbody")) {
			entry.setAttribute("http.body", new File(getParam("inbody")));
		} else if (hasEntry && hasParser()) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.HTTPCLIENT.FILTERCONSTRUCT.INFO",
						getParam("parser")));
			}

			Entry pe = new Entry();
			for (String name : entry.getAttributeNames()) {
				if (name.startsWith("http."))
					continue;
				pe.setAttribute(name, entry.getAttribute(name));
//				if (debugMode()) {
//					debug(sResHash.getString("CONNECTOR.HTTPCLIENT.SETATTR.INFO", name));
//				}
			}

			if (shouldAddBody(pe, entry.getString("http.method"))) {
				StringWriter sw = new StringWriter();
				initParser(null, sw);
				getParser().writeEntry(pe);
				getParser().closeParser();
	
				entry.setAttribute("http.body", sw.toString());
			}
		}

		if (entry.getString("http.Content-Type") == null && hasConfigValue("contentType"))
			entry.setAttribute("http.Content-Type", getParam("contentType"));

		if (entry.getString(AUTHORIZATION) == null && hasConfigValue(AUTHORIZATION))
			entry.setAttribute(AUTHORIZATION, getParam(AUTHORIZATION));
		
		if (socket != null) {
			// Try to re-use the socket (persistent connection)
			try {
				isTryingPersistentConnection = true;
				if (debugMode()) {
					debug(sResHash.getString("HTTPCLIENT.USING.PERSISTENT", url));
				}
				setProxyParameters(entry);
				parser.writeEntry(entry);
				checkConnectionFlag(entry);
				return;
			} catch (IOException e) {
				closeSocket();
			}
		}
		
		isTryingPersistentConnection = false;

		// Should we use a proxy server ?
		String proxy = getParam("proxy");

		if (proxy != null && proxy.length() > 0) {
			int i = proxy.indexOf(':');
			if (i > 0) {
				try {
					port = Integer.parseInt(proxy.substring(i + 1));
					host = proxy.substring(0, i);
				} catch (Exception e) {
					logmsg(sResHash.getString("HTTPCLIENT.CANNOT.PARSE.PROXY", proxy));
				}
			} else {
				host = proxy;
			}

			if ("https".equalsIgnoreCase(lastProtocol)) {
				connectTunnelSocket(host, port);
			}
			setProxyParameters(entry);
		} 
	
		if (socket == null ) {
			connectSocket(host, port, lastProtocol);
		}
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.HTTPCLIENT.CONNECTTO.INFO", url));
		}

		// Initialize our local instance of the metamerge.HTTP parser
		parser.setInputStream(socket.getInputStream());
		parser.setOutputStream(socket.getOutputStream());

		parser.writeEntry(entry);
		checkConnectionFlag(entry);
	}

	private boolean shouldAddBody(Entry pe, String method) {
		if (method != null && ! "GET".equalsIgnoreCase(method) && ! "HEAD".equalsIgnoreCase(method))
			return true;
		return pe.size() > 0;
	}

	private void setProxyParameters(Entry entry) {
		if (isUsingTunnel)
			return;
		String proxy = getParam("proxy");

		if (proxy == null || proxy.length() == 0)
			return;
		// Signal that we are using a proxy
		entry.setProperty("http.proxy", "");

		// Set proxy username/password
		if (hasConfigValue("proxyUsername"))
			entry.setAttribute("http.proxy_user", getParam("proxyUsername"));

		if (hasConfigValue("proxyPassword"))
			entry.setAttribute("http.proxy_pass", getParam("proxyPassword"), true);	
	}
	
	private void connectTunnelSocket(String proxyHost, int proxyPort) throws Exception {

		connectSocket(proxyHost, proxyPort, "http");

		parser.setInputStream(socket.getInputStream());
		parser.setOutputStream(socket.getOutputStream());
	
		int port = lastPort;
		if (port == -1)
			port = 443;

		BufferedWriter out = parser.getWriter();
		out.write("CONNECT " + lastHost + ":" + port + " HTTP/1.1\r\n");

		// Write Host Header
		if (requestHost != null)
			out.write("Host: " + requestHost + "\r\n");

		// Set proxy username/password
		parser.sendProxyAuthorization(getParam("proxyUsername"), getParam("proxyPassword"), out);

		out.write("\r\n");
		out.flush();

		BufferedReader in = parser.getReader();
		String line = in.readLine();
		String response = null;
		if (line != null) {
			String[] parts = line.split(" ", 2);
			if (parts.length >= 2)
				response = parts[1];
		}
		in.readLine(); // Skip an empty line
		
		if (response != null && response.startsWith("2")) {
			socket = getSSLSocketFactory().createSocket(socket, lastHost, port, true);
			SystemFunctions.verifySSLProtocols(socket);
			if (debugMode()) {
				debug(sResHash.getString("HTTPCLIENT.USING.TUNNEL", new Object[] {proxyHost, lastHost + ":" + port}));
			}
			isUsingTunnel = true;
		} else {
			if (debugMode()) {
				debug(sResHash.getString("HTTPCLIENT.TUNNEL.FAILED", new Object[] {proxyHost, lastHost, response}));
			}
		}
	}

	/**
	 * Check if the entry contains a Connection: Close flag.
	 * If it does, mark the connection as not reusable.
	 * @param entry
	 */
	private void checkConnectionFlag(Entry entry) {
		if ("close".equalsIgnoreCase(entry.getString("http.Connection"))) {
			// set lastProtocol to null to signify that this connection
			// should not be re-used
			lastProtocol = null;
		}
	}

	private void connectSocket(String host, int port, String protocol) throws Exception {

		InetSocketAddress ia;

		if (protocol.equalsIgnoreCase("https")) {
			// Use default port if not specified
			if (port == -1)
				port = 443;

			// Before Java 8, we can either specify SNI or use timeout
			// when connecting, not both.
			if (timeout == 0) {
				// Default timeout, enable SNI (Server Name Indicator).
				socket = getSSLSocketFactory().createSocket(host, port);
			} else {
				socket = getSSLSocketFactory().createSocket();
				// connect the socket using timeout (only address, no server name)
				if (host != null)
					ia = new InetSocketAddress(host, port);
				else
					ia = new InetSocketAddress(InetAddress.getByName(null), port);
				socket.connect(ia, timeout);				
			}
			SystemFunctions.verifySSLProtocols(socket);

		} else if (protocol.equalsIgnoreCase("http")) {
			// Use default port if not specified
			if (port == -1)
				port = 80;

			socket = new Socket();
			// connect the socket using timeout
			if (host != null)
				ia = new InetSocketAddress(host, port);
			else
				ia = new InetSocketAddress(InetAddress.getByName(null), port);
			socket.connect(ia, timeout);
		} else {
			// We only do HTTP or HTTPS
			throw new Exception(sResHash.getString("CONNECTOR.HTTPCLIENT.BADPROTOCAL.EXCEP",
					protocol));
		}

		// set read timeout
		socket.setSoTimeout(timeout);

		// This will improve the speed when testing
		socket.setTcpNoDelay(true);
	}

	/**
	 * Close the previous socket if needed	
	 */
	private void closeSocket() {
		//defect 15117 - release the previous socket if already open
		if(socket != null) {
			try {
				socket.close();
				socket = null;
			} catch(IOException se) {
				socket = null;
			}
		}		
		isUsingTunnel = false;
	}
	
	/**
	 * This method reads the return entry from the http parser and checks for a
	 * configured parser in this connector. If a parser is defined then the
	 * http.body is sent to the parser for additional parsing.
	 *
	 * @param sentEntry
	 *            the Entry that has earlier been sent to the HTTP parser
	 * @return the Entry returned from the HTTP Parser, including all information from the attached parser
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry readResponse(Entry sentEntry) throws Exception {

		Entry e = parser.readEntry();
		if (e == null)
			return null;
		
		e = checkNTLM(e, sentEntry);
		
		checkConnectionFlag(e);
		
		Object body = e.getObject("http.body");
		if (body != null) {
			copyBodyToFile(body);

			if (hasParser()) {
				initParser(body, null);
				userParserInitialized = true;
				Entry parsedEntry = getParser().readEntry();
				if (parsedEntry != null)
					e.merge(parsedEntry);
			}

			e.setAttribute("http.body.response", body);
		}

		return e;
	}

	/**
	 * Implement NTLM authentication.
	 * Handshake:
	1: C  --> S   GET ...
    
    2: C <--  S   401 Unauthorized
                  WWW-Authenticate: NTLM
    
    3: C  --> S   GET ...
                  Authorization: NTLM <base64-encoded type-1-message>
    
    4: C <--  S   401 Unauthorized
                  WWW-Authenticate: NTLM <base64-encoded type-2-message>
    
    5: C  --> S   GET ...
                  Authorization: NTLM <base64-encoded type-3-message>
    
    6: C <--  S   200 Ok
	 * @param origResponse
	 * @param sentEntry
	 */
	private Entry checkNTLM(Entry origResponse, Entry sentEntry) {
		if (! "401".equals(origResponse.getString("http.responseCode"))
				|| lastProtocol == null || sentEntry == null)
			return origResponse;

		Attribute authenticate = origResponse.getAttribute(AUTHENTICATE);
		if (authenticate == null || !authenticate.hasValueIC("NTLM"))
				return origResponse;

		debug("Trying NTLM authentication");
		String user = sentEntry.getString("http.remote_user");
		if (user == null)
			user = getParam("username");
		if (user == null) {
			debug("NTLM: No username specified");
			return origResponse;
		}

		String pass = sentEntry.getString("http.remote_pass");
		if (pass == null)
			pass = getParam("password");
		if (pass == null)
			pass = "";

		String domain = getParam("NTLM.domain");
		int slash = user.indexOf('\\');
		if (domain == null && slash > 0) {
			domain = user.substring(0, slash);
			user = user.substring(slash + 1);
		}
		if (domain == null)
			domain = "";

		String host = getParam("NTLM.host");
		if (host == null)
			host = "";
		debug("NTLM user=" + user + ", domain=" + domain + ", host=" + host);

		try {
			Class<?> c = Class.forName("org.apache.http.impl.auth.NTLMEngineImpl");
			Constructor<?> constructor = c.getDeclaredConstructor();
			constructor.setAccessible(true);
			NTLMEngine engine = (NTLMEngine) constructor.newInstance();
			String auth1 = "NTLM " + engine.generateType1Msg(domain, host);
			debug("NTLM sending type1=" + auth1);
			sentEntry.setAttribute(AUTHORIZATION, auth1);
			parser.writeEntry(sentEntry);
			Entry response = parser.readEntry();
			if (response == null) {
				debug("NTLM no response");
				return origResponse;
			}
			String type2 = response.getString(AUTHENTICATE);
			if (type2 == null || type2.length() < 5) {
				debug("NTLM no type2 challenge returned");
				return response;
			}
			String auth3 = "NTLM " + engine.generateType3Msg(user, pass, domain, host, type2.substring(5));
			debug("NTLM sending type3=" + auth3);
			sentEntry.setAttribute(AUTHORIZATION, auth3);
			parser.writeEntry(sentEntry);
			return parser.readEntry();
		} catch (Exception ex) {
			logError("NTLM: Caught exception " + ex);
		}

		return origResponse;
	}

	/**
	 * Send a request and read the response
	 * @param url - url to use
	 * @param entry - contains info to send
	 * @return The Entry that was read
	 * @throws Exception
	 */
	private Entry sendRequestReadResponse(String url, Entry entry) throws Exception {

		sendRequest(url, entry);
		
		if (isTryingPersistentConnection) {
			try {
				Entry e = readResponse(entry);
				if ( e != null)
					return e;
			} catch (IOException ioe) {
				SystemFunctions.doNothing();
			}
			closeSocket();
			sendRequest(url, entry);
		}

		return readResponse(entry);	
	}
	
	/**
	 * copy the body to the file +++Tai
	 * @param body
	 */
	private void copyBodyToFile(Object body) {

		String outBodyFile = getParam("outbody"); // D934

		if (outBodyFile == null || outBodyFile.length() == 0)
			return;

		byte[] responseBytes;

		if (body instanceof byte[]) {
			// binary data
			responseBytes = (byte[]) body;
		} else {
			// textual data
			responseBytes = body.toString().getBytes();
		}

		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(outBodyFile);
			fileOut.write(responseBytes);
		} catch (IOException io) {
			logError(sResHash.getString(
					"CONNECTOR.HTTPCLIENT.READ.RESPONSE", io));
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException io) {
					debug(io.toString());
				}
			}
		}
	}
	
 	/**
	 * Version information.
	 *
	 * @return version information
	 */
	public String getVersion() {
		return VERSION_INFO;
	}
	
	public String getClientCertificateAlias() {
		return clientCertificateAlias;
	}

	/**
	 * Sets the Client Certificate that should be used when connection.
	 * Equivalent to setClientCertificateAlias(clientCertificateAlias, null, null, null)
	 * @param clientCertificateAlias - Name of the alias in the default keystore.
	 */
	public void setClientCertificateAlias(String clientCertificateAlias) {
		setClientCertificateAlias(clientCertificateAlias, null, null, null);
	}
	
	/**
	 * Sets the Client Certificate that should be used when connection.
	 * If the alias is null, resets behavior to default Java behavior.
	 * If the alias is an empty string, do not send any certificate at all.
	 * @param clientCertificateAlias - Name of the alias in the key store.
	 * @param keyStorePath - Path to key store. null means to use the javax.net.ssl.keyStore. 
	 * @param keyStorePass - Password for key store. null means javax.net.ssl.keyStorePassword.
	 * @param keyStoreType - The key store type. null means javax.net.ssl.keyStoreType or default.
	 */
	public void setClientCertificateAlias(String clientCertificateAlias,
			String keyStorePath, String keyStorePass, String keyStoreType) {
		// Reset sslSocketFactory to default;
		sslSocketFactory = ((SSLSocketFactory)SSLSocketFactory.getDefault());

		this.clientCertificateAlias = clientCertificateAlias;

		if (clientCertificateAlias == null) {
			return;
		}

		if (clientCertificateAlias.isEmpty()) {
			try {
				// initialize a KeyManagerFactory with a store containing no certificates.
				KeyStore empty = KeyStore.getInstance("jks");
				empty.load(null, null);
				KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmFactory.init(empty, null);

				// Create the new SSLSocketFactory
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(kmFactory.getKeyManagers(), null, null);
				sslSocketFactory = ctx.getSocketFactory();
				debug("No certificate will be used if client authentication is required");			
			} catch (Exception e) {
				logError("While creating KeyManager with no certificates: " + e.getMessage());
			}
			return;
		}

		if (keyStorePath == null)
			keyStorePath = System.getProperty("javax.net.ssl.keyStore");
		if (keyStorePass == null)
			keyStorePass = System.getProperty("javax.net.ssl.keyStorePassword");
		if (keyStorePath == null || keyStorePass == null) {
			logError("javax.net.ssl.keyStore or javax.net.ssl.keyStorePassword not specified, cannot read the alias " + clientCertificateAlias);
			return;
		}

		if (keyStoreType == null)
			keyStoreType = System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType());

		KeyStore keyStore;
		InputStream keyStoreStream = null;
		try {
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStoreStream = new FileInputStream(keyStorePath);
			keyStore.load(keyStoreStream, keyStorePass.toCharArray());
		} catch (Exception e) {
			logError("Unable to read keystore " + keyStorePath + ": " + e.getMessage());
			return;			
		} finally {
			if (keyStoreStream != null) {
				try {
					keyStoreStream.close();
				} catch (IOException e) {}
			}
		}

		// initialize a KeyManagerFactory with a store containing only the alias.
		KeyManagerFactory kmFactory;
		try {
			kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			Key key = keyStore.getKey(clientCertificateAlias, keyStorePass.toCharArray());
			if (key == null) {
				logError("Client certificate " + clientCertificateAlias + " not found in keystore");
				return;			
			}
			KeyStore temp = KeyStore.getInstance("jks");
			temp.load(null, keyStorePass.toCharArray());
			temp.setKeyEntry(clientCertificateAlias, key, keyStorePass.toCharArray(),
					keyStore.getCertificateChain(clientCertificateAlias));
			kmFactory.init(temp, keyStorePass.toCharArray());

			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmFactory.getKeyManagers(), null, null);

			// Finally creating the new SSLSocketFactory
			sslSocketFactory = ctx.getSocketFactory();
			debug("The certificate " + clientCertificateAlias + " will be used if client authentication is required");			

		} catch (Exception e) {
			logError("While creating temporary keystore for client certificate " + clientCertificateAlias + ": " + e.getMessage());
			return;			
		}
	}

	private SSLSocketFactory getSSLSocketFactory() throws Exception {
		if (sslSocketFactory == null)
			sslSocketFactory = ((SSLSocketFactory)SSLSocketFactory.getDefault());
		return sslSocketFactory;
	}
}
