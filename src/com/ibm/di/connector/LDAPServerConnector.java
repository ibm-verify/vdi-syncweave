/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Locale;
import java.util.Vector;

import javax.net.ssl.SSLServerSocket;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.eventhandler.ldap.AbandonRequest;
import com.ibm.di.eventhandler.ldap.LDAPControl;
import com.ibm.di.eventhandler.ldap.LDAPMessage;
import com.ibm.di.eventhandler.ldap.LDAPResult;
import com.ibm.di.eventhandler.ldap.Request;
import com.ibm.di.eventhandler.ldap.SearchResultEntry;
import com.ibm.di.eventhandler.ldap.UnbindRequest;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * LDAPServerConnector is an LDAP server that returns a client connection from
 * the getNextClient method. The returned connector interface is initialized
 * with an LDAP client connection. Calls to the returned connector should be
 * getNextEntry() to retrieve the next client request (e.g. bind, search etc),
 * putEntry() to send search results to the LDAP client and finally replyEntry()
 * to send the status message that completes the client request.
 * <p>
 * The returned connector is not thread safe. Alls calls to putEntry and
 * replyEntry uses the previous getNextEntry request attributes in the data sent
 * to the client. Specifically, the LDAP message id and response operation code
 * is reused from the client request.
 */
public class LDAPServerConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ldapserverconnector";

	/**
	 * Configuration parameter name: {@value #PARAM_TCP_BACKLOG}
	 */
	public static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * Configuration parameter name: {@value #PARAM_SYSTEM_TCP_BACKLOG}
	 */
	public static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * {@link ServerSocket}
	 */
	private ServerSocket server;

	/**
	 * {@link Socket}
	 */
	private Socket socket;

	/**
	 * {@link LDAPMessage}
	 */
	private LDAPMessage msg;

	/**
	 * Binary attributes.
	 */
	private Vector<String> binattrs = new Vector<String>();

	/**
	 * Encoding
	 */
	private String charset = "UTF-8";

	/**
	 * {@link Request}
	 */
	private Request req;

	/**
	 * {@link LDAPServerConnector}
	 */
	private LDAPServerConnector serverConnector;

	/**
	 * Termination requested flag.
	 */
	private boolean terminationRequested = false;
	/**
	 * Indicates whether connector is currently waiting for a client connection.
	 */
	private boolean isAccepting = false;

	/**
	 * LDAP message queue
	 */
	private Vector<LDAPMessage> queue = new Vector<LDAPMessage>();
	/**
	 * Object used for access of the TMS messages
	 */
	private static final ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private boolean keepAlive;
	
	/**
	 * @return The resource object.
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * Default constructor.
	 */
	public LDAPServerConnector() {
		super();
		setModes(new String[] { ConnectorConfig.SERVER_MODE, });
	}

	/**
	 * Retrieves server connector.
	 * 
	 * @return the server connector if this connector is handling an LDAP client
	 *         session.
	 */
	public LDAPServerConnector getServerConnector() {
		return serverConnector;
	}

	/**
	 * Sets the server connector for this connector.
	 * 
	 * @param serverConnector
	 *            the server connector to set.
	 */
	public void setServerConnector(LDAPServerConnector serverConnector) {
		this.serverConnector = serverConnector;
	}

	/**
	 * Checks if the connector is waiting for a client connection.
	 * 
	 * @return true if this connector is currently waiting for a client
	 *         connection.
	 */
	public boolean isAccepting() {
		return isAccepting;
	}

	/**
	 * Checks if a termination request is sent.
	 * 
	 * @return true if this connector has the termination flag set.
	 */
	public boolean isTerminating() {
		return terminationRequested;
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
		if (isAccepting()) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAPSERVER.TERMINATERCV.INFO"));
			}
			// At this point we are the server
			terminationRequested = true;
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAPSERVER.DUMMY.INFO", new Object[] { server.getInetAddress(),
						"" + server.getLocalPort() }));
			}
			new Socket(server.getInetAddress(), server.getLocalPort());
		} else {
			// At this point we are a client sending a message to our server
			if (getServerConnector() == null) {
				throw new Exception(sResHash.getString("CONNECTOR.LDAPSERVER.NO.SERVER.CONNECTOR.AVAILABLE"));
			}

			if (getServerConnector().isTerminating()) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.LDAPSERVER.ALREADYTERM.INFO"));
				}
				return;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAPSERVER.SENDINGTERM.INFO"));
			}
			getServerConnector().terminateServer();
		}
	}

	/**
	 * Initialize the connector. To initialize this connector with an LDAP
	 * client session provide a java.net.Socket object for the obj parameter. In
	 * all other cases, the connector will initialize an LDAP server session.
	 * 
	 * @param obj
	 *            Null, Socket or ConnectorMode class
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object obj) throws Exception {

		if (socket != null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAPSERVER.CONNECTOR.ALREADY.INITIALIZED"));
			}
			return;
		}

		if (obj instanceof Socket) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.LDAPSERVER.USING.PROVIDE.JAVA.NET.SOCKET.OBJECT"));
			}
			socket = (Socket) obj;

			// Dramatically enhances speed when running over a LAN
			socket.setTcpNoDelay(true);

			if (Boolean.valueOf(getParam("ldapKeepAlive"))) {
				keepAlive = true;
			}
			socket.setKeepAlive(keepAlive);

			// Get binary attributes
			String bin = getParam("ldapBinaryAttributes");
			if (bin != null && bin.length() > 0) {
				StringTokenizer st = new StringTokenizer(bin, "\n");
				while (st.hasMoreTokens())
					binattrs.add(st.nextToken().toLowerCase(Locale.ENGLISH));
			}

			// Get charset
			String str = getParam("charset");
			if (str != null && str.length() > 0)
				charset = str;

			return;
		}

		// Get ldap listen port
		int port;

		String str = getParam("ldapPort");
		if (str == null || str.trim().length() == 0)
			str = "389";

		port = Integer.parseInt(str);

		int backlog = -1; // The maximum length of the queue.
		String strBacklog = getParam(PARAM_TCP_BACKLOG);
		if (strBacklog == null || strBacklog.trim().length() == 0) {
			strBacklog = System.getProperty(PARAM_SYSTEM_TCP_BACKLOG);
		}

		if (strBacklog != null && strBacklog.trim().length() > 0) {
			backlog = Integer.parseInt(strBacklog);
		}

		// Create server socket and wait for connections
		if (Boolean.valueOf(getParam("ldapUseSSL")).booleanValue()) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.LISTENING.FOR.LDAP.SSL.CONNECTIONS", Integer.valueOf(port)));
			server = getSSLServerSocket(port, backlog);
		} else {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.LISTENING.FOR.LDAP.CONNECTIONS", Integer.valueOf(port)));

			if (backlog > 0) {
				server = getRSInterface().getServerSocketFactory(false).createServerSocket(port, backlog);
			} else {
				server = getRSInterface().getServerSocketFactory(false).createServerSocket(port);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		super.terminate();

		if (socket != null) {
			socket.close();
			socket = null;
		}
		if (server != null) {
			server.close();
			server = null;
		}
	}

	/**
	 * Server mode - returns a new instance of the connector with a client
	 * connection. The new connector map {@link #getNextEntry()} to get requests
	 * from the client, {@link #putEntry(Entry)} to send a search result to the
	 * client, and finally {@link #replyEntry(Entry)} to send the LDAP result
	 * message.
	 * 
	 * @return an instance of this connector, that handles client sessions.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public ConnectorInterface getNextClient() throws Exception {
		if (server == null) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAPSERVER.NOTSERVER.SESSION.ERROR"));
		}

		if (terminationRequested) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.CONNTERMINATED.INFO"));
			return null;
		}

		isAccepting = true;

		Socket s = server.accept();

		isAccepting = false;

		if (terminationRequested) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.CONNTERMINATED.INFO"));
			s.close();
			return null;
		}

		LDAPServerConnector clientSession = new LDAPServerConnector();
		clientSession.setServerConnector(this);
		clientSession.setConfiguration(getConfiguration());
		clientSession.setName(getName());
		clientSession.setLog(getLog());
		if (keepAlive)
			clientSession.setKeepAlive(true);
		clientSession.initialize(s);
		return clientSession;
	}

	/**
	 * Returns the next Entry from the LDAP client.
	 * 
	 * @return - the next Entry, or null if client connection has been closed.
	 * @see #selectEntries()
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {
		if (socket == null) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAPSERVER.NOT.A.CLIENT.SESSION.USE.GETNEXTCLIENT"));
		}

		if (queue.size() > 0) {
			msg = queue.remove(0);
		} else {
			msg = getNextMessage(socket);
		}

		if (msg == null)
			return null;

		req = msg.getRequest();
		if (req == null) {
			throw new Exception(sResHash.getString("CONNECTOR.LDAPSERVER.INTERNAL.ERROR.LDAPMESSAGE"));
		}

		// Get request entry and add the LDAP message fields
		Entry e = msg.getEntry();
		e.setAttribute("ldap.operation", req.getCommandString());
		e.setAttribute("ldap.ipaddress", socket.getInetAddress().toString());
		e.setAttribute("ldap.messageid", "" + msg.getMessageID());

		// Debug
		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.BEGIN.REQUEST.PACKET.DUMP"));
			msg.dump(getLog());
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.END.REQUEST.PACKET.DUMP"));
		}

		return e;

	}

	/**
	 * Sends a search result entry to the LDAP client in response to a search
	 * request. The previously received client request must be a Search request
	 * or this method will most likely cause trouble for the LDAP client since
	 * the message id from the previous request is used in the search result
	 * packet.
	 * 
	 * @param entry
	 *            The entry. It must contain the $dn attribute plus other LDAP
	 *            attributes.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void putEntry(Entry entry) throws Exception {
		LDAPMessage sr = new LDAPMessage(msg.getMessageID());
		sr.setResponse(new SearchResultEntry(entry, charset));
		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.BEGIN.SEARCH.RESULT.PACKET.DUMP"));
			sr.dump(getLog());
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.END.SEARCH.RESULT.PACKET.DUMP"));
		}
		java.nio.ByteBuffer outbuf = sr.getBuffer();
		socket.getOutputStream().write(outbuf.array(), 0, outbuf.limit());
		socket.getOutputStream().flush();
	}

	/**
	 * Send an LDAP result message in response to the last request from the LDAP
	 * Client. The attributes used in the result packet are: ldap.status to set
	 * the status code, ldap.errormessage to provide additional info in case
	 * ldap.status is not zero, ldap.matcheddn to tell the client about the
	 * offending DN if any and finally ldap.referrals to refer the client to
	 * another LDAP server. The default value for all attributes is NULL and
	 * ZERO (e.g. OK) for the ldap.status attribute. You generally only have to
	 * set attribute values in case there is an error or you want to refer to
	 * another server.
	 * 
	 * @param conn
	 *            the entry, result of the Output Mapping
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void replyEntry(Entry conn) throws Exception {
		int status = 0;
		String matchedDN = null;
		String errorMessage = null;
		Object[] referrals = null;
		Object[] controls = null;

		// Send response packet or close the connection if request not
		// understood
		if (req instanceof UnbindRequest) {
			getLog().info(sResHash.getString("CONNECTOR.LDAPSERVER.CONNECTION.CLOSED"));
			return;
		}

		// Format response message
		Object obj = conn.getObject("ldap.status");
		if (obj != null) {
			if (obj instanceof Integer)
				status = ((Integer) obj).intValue();
			else
				status = Integer.parseInt(obj.toString());
		}

		obj = conn.getObject("ldap.matcheddn");
		if (obj != null)
			matchedDN = obj.toString();

		obj = conn.getObject("ldap.errormessage");
		if (obj != null)
			errorMessage = obj.toString();

		if (conn.getAttribute("ldap.referrals") != null)
			referrals = conn.getAttribute("ldap.referrals").getValues();

		if (conn.getAttribute("ldap.controls") != null) {
			Object[] cv = conn.getAttribute("ldap.controls").getValues();
			Vector<LDAPControl> ctls = new Vector<LDAPControl>();
			for (int i = 0; i < cv.length; i++) {
				LDAPControl c = (LDAPControl) cv[i];
				if (c.canHandleControl()) {
					c.setControlValueEntry(conn);
					ctls.add(c);
				}
			}
			controls = ctls.toArray();
		}

		sendResponse(socket, msg.getMessageID(), req.getResponseOp(), status, matchedDN, errorMessage, referrals, controls);

	}

	/**
	 * This method returns true if there is an abandon message in the queue with
	 * the specified message ID.
	 * 
	 * @param messageID
	 *            The LDAP Message ID
	 * @return true if there is an abandoned message with the provided id, false
	 *         otherwise.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public boolean isAbandoned(int messageID) throws Exception {
		LDAPMessage msg;
		if (socket == null)
			return false;

		// check queue of receives messages
		for (int i = 0; i < queue.size(); i++) {
			msg = queue.get(i);
			if (msg.getRequest() instanceof AbandonRequest) {
				Integer id = (Integer) msg.getEntry().getObject("ldap.abandon");
				if (id.intValue() == messageID) {
					queue.remove(i);
					return true;
				}
			}
		}

		// check incoming messages
		while (socket.getInputStream().available() > 0) {
			msg = getNextMessage(socket);
			if (msg == null)
				return false;

			if (msg.getRequest() instanceof AbandonRequest) {
				Integer id = (Integer) msg.getEntry().getObject("ldap.abandon");
				if (id.intValue() == messageID)
					return true;
			}

			// Add non processed messages to queue
			queue.add(msg);
		}

		return false;
	}

	/**
	 * Creates an instance of the {@link LDAPControl} class.
	 * 
	 * @param oid
	 *            the extended operation id
	 * @return the newly created object.
	 */
	public LDAPControl createControl(String oid) {
		return new LDAPControl(oid);
	}

	/**
	 * Sends LDAP message to the client
	 * 
	 * @param s
	 *            socket
	 * @param messageID
	 *            message id
	 * @param responseOp
	 *            response operation
	 * @param status
	 *            status of the massage
	 * @param matchedDN
	 *            matched distinguished name
	 * @param errorMessage
	 *            error message
	 * @param referrals
	 *            referrals
	 * @param controls
	 *            controls (array of LDAPControl objects)
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void sendResponse(Socket s, int messageID, int responseOp, int status, String matchedDN, String errorMessage,
			Object[] referrals, Object[] controls) throws Exception {
		// Create LDAP message with result sequence
		LDAPMessage rsp = new LDAPMessage(messageID);
		rsp.setResponse(new LDAPResult(responseOp, status, matchedDN, errorMessage, referrals));
		rsp.setControls(controls);

		// Debug dump
		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.BEGIN.RESPONSE.PACKET.DUMP"));
			rsp.dump(getLog());
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.END.RESPONSE.PACKET.DUMP"));
		}

		// Send to client
		java.nio.ByteBuffer outbuf = rsp.getBuffer();
		s.getOutputStream().write(outbuf.array(), 0, outbuf.limit());
		s.getOutputStream().flush();

	}

	/**
	 * Retrieves the next {@link LDAPMessage}
	 * 
	 * @param s
	 *            socket
	 * @return the next ldap message
	 * @throws Exception
	 */
	private LDAPMessage getNextMessage(Socket s) throws Exception {
		LDAPMessage p = null;

		try {
			p = new LDAPMessage();

			if (p.parse(s.getInputStream(), charset, binattrs))
				return p;
			else
				return null;
		} catch (IOException ioerror) {
			logmsg(sResHash.getString("CONNECTOR.LDAPSERVER.IOEXCEPTION.WHILE.READING.REQUEST", ioerror.toString()));
			return null;
		} catch (Exception error) {
			if (p != null && debugMode())
				p.dump(getLog());
			throw error;
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
	 * @return ServerSocket
	 * @throws IOException
	 */
	private ServerSocket getSSLServerSocket(int aPort, int aBacklog) throws IOException {

		SSLServerSocket sslServerSocket;
		if (aBacklog > 0) {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort, aBacklog);
		} else {
			sslServerSocket = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(aPort);
		}

		return sslServerSocket;
	}

	/**
	 * Version information.
	 * 
	 * @return the version information
	 */
	public String getVersion() {
		return "2.3-di11.0.0.1 2019-04-29";
	}
	
	public void setKeepAlive(boolean value) {
		keepAlive = value;
		if (socket != null)
			try {
				socket.setKeepAlive(value);
			} catch (SocketException e) {
			}
	}

}
