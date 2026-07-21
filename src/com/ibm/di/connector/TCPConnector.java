/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * The TCP Connector is a transport Connector using TCP sockets for transport.
 * You can use the TCP Connector in Iterator and AddOnly mode only.
 */
public class TCPConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the component
	 */
	private static final String PROPERTIES_FILE = "tcpconnector";

	/**
	 * name of backlog parameter
	 */
	public static final String PARAM_TCP_BACKLOG = "backlog";

	/**
	 * name of the backlog in the global properties file
	 */
	public static final String PARAM_SYSTEM_TCP_BACKLOG = "com.ibm.di.tcp.backlog";

	/**
	 * input reader
	 */
	private BufferedReader in;

	/**
	 * output writter
	 */
	private BufferedWriter out;

	/**
	 * socket to the server
	 */
	private ServerSocket serverSocket;

	/**
	 * tcp port number
	 */
	private Integer tcpPort;

	/**
	 * name of the host
	 */
	private String tcpHost;

	/**
	 * the socket obejct
	 */
	private Socket socket;

	/**
	 * Is connector in server mode
	 */
	private Boolean serverMode;

	/**
	 * name of the component
	 */
	private static final String myName = "ArchiTech TCP Connector";

	/**
	 * Resource hash object for accessing TMS messages
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Class constructor
	 */
	public TCPConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.ADDONLY_MODE, });
	}

	/**
	 * default implementation
	 * 
	 * @throws Exception
	 *             never
	 */
	public void selectEntries() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {

		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (Exception ignore) {
		}

		try {
			if (socket != null)
				socket.close();
		} catch (Exception ignore) {
		}

		serverSocket = null;
		socket = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object o) throws Exception {
		socket = null;
		serverSocket = null;
		tcpPort = null;
		tcpHost = null;

		if (o != null && o instanceof Socket) {
			socket = (Socket) o;
		} else {
			if (getParam("tcpPort") == null) {
				throw new com.ibm.di.exceptions.MissingConfigurationException(sResHash
						.getString("CONNECTOR.TCP.MISSING.TCPPORT.EXCEPTION"), "tcpPort");
			}

			tcpPort = new Integer(getParam("tcpPort"));

			serverMode = getBoolean("serverMode");
			if (serverMode != null && serverMode.booleanValue()) {

				int backlog = -1; // The maximum length of the queue.
				String strBacklog = getParam(PARAM_TCP_BACKLOG);
				if (strBacklog == null || strBacklog.trim().length() == 0) {
					strBacklog = System.getProperty(PARAM_SYSTEM_TCP_BACKLOG);
				}

				if (strBacklog != null && strBacklog.trim().length() > 0) {
					backlog = Integer.parseInt(strBacklog);
				}

				if (Boolean.valueOf(getParam("tcpUseSSL")).booleanValue()) {
					logmsg(sResHash.getString("CONNECTOR.TCP.LISTENSSL.INFO", tcpPort));
					serverSocket = getSSLServerSocket(tcpPort.intValue(), backlog);
				} else {
					logmsg(sResHash.getString("CONNECTOR.TCP.LISTENNORMAL.INFO", tcpPort));
					if (backlog > 0) {
						serverSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(tcpPort.intValue(),
								backlog);
					} else {
						serverSocket = getRSInterface().getServerSocketFactory(false).createServerSocket(tcpPort.intValue());
					}
				}
				socket = null;

			} else {
				tcpHost = getParam("tcpHost");
				if (tcpHost == null) {
					throw new com.ibm.di.exceptions.MissingConfigurationException(sResHash
							.getString("CONNECTOR.TCP.MISSING.TCPHOST.EXCEPTION"), "tcpHost");
				}
				if (Boolean.valueOf(getParam("tcpUseSSL")).booleanValue()) {
					socket = getSSLSocket(tcpHost, tcpPort.intValue());
					logmsg(sResHash.getString("CONNECTOR.TCP.CLIENTLISTENSSL.INFO", new Object[] { tcpHost, tcpPort }));
				} else {
					socket = new Socket(tcpHost, tcpPort.intValue());
					logmsg(sResHash.getString("CONNECTOR.TCP.CLIENTLISTENNORMAL.INFO", new Object[] { tcpHost, tcpPort }));
				}
			}
		}

	}

	/**
	 * Returns the next Entry from the connector. If we are in Server mode,
	 * accept a new Connection. If there is a Parser connected to this
	 * Connector, the Parser is used to read the next Entry. The Entry will have
	 * three special Properties:
	 * <UL>
	 * <LI>socket - The socket we are reading from
	 * <LI>inp - a BufferedReader using the socket
	 * <LI>out - a BufferedWriter using the socket
	 * </UL>
	 * 
	 * @return - the next Entry, or null if no more data
	 * @throws Exception
	 *             if an error occurs
	 * @see #selectEntries()
	 */
	public Entry getNextEntry() throws Exception {
		if (serverSocket != null) {
			if (socket != null)
				socket.close();
			socket = null;

			logmsg(sResHash.getString("CONNECTOR.TCP.WAITING.INFO", tcpPort));
			SocketThread socketThread = new SocketThread(serverSocket);
			try {
				socketThread.start();
				socketThread.join();
				socket = socketThread.getSocket();
			} catch (InterruptedException ioe) {
				logmsg(sResHash.getString("CONNECTOR.TCP.INTERRUPTED.WAITING"));
				serverSocket.close();
				throw ioe;
			}
			if (socket == null)
				return null;
			in = null;
			out = null;
			socketThread = null;
			logmsg(sResHash.getString("CONNECTOR.TCP.CONNECT.INFO", socket.getInetAddress().toString()));
		}
		setProperty("socket", socket);
		Entry entry;
		if (in == null && socket != null)
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		if (out == null && socket != null)
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		if (hasParser()) {
			initParser(in, null);
			entry = getParser().readEntry();
			if (entry == null)
				return entry;
		} else {
			entry = new Entry();
		}

		/**
		 * Set entry properties so that receiver can work with TCP streams.
		 */
		entry.setProperty("socket", socket);
		entry.setProperty("inp", in);
		entry.setProperty("out", out);

		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry entry) throws Exception {
		if (hasParser()) {
			if (out == null && socket != null)
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			// if (getParam("parser") != null &&
			// getParam("parser").toString().length() > 0) {
			initParser(null, out);
			getParser().writeEntry(entry);
		} else {
			socket.getOutputStream().write(entry.toString().getBytes());
		}
	}

	/**
	 * Returns a reader object for the socket
	 * 
	 * @return the input reader
	 * @throws Exception
	 *             if an error occurs
	 * 
	 */
	public Reader getReader() throws Exception {
		if (in == null && socket != null) {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		return in;
	}

	/**
	 * Returns a writer object for the socket
	 * 
	 * @return the output writer
	 * @throws Exception
	 *             if an error occurs
	 * 
	 */
	public Writer getWriter() throws Exception {
		if (out == null && socket != null) {
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		return out;
	}

	/**
	 * Creates SSL server socket
	 * 
	 * @param port
	 *            port number
	 * @param aBacklog
	 *            number of queued connections
	 * @return the created server socket
	 * @throws IOException
	 *             if an error occurs
	 */
	private ServerSocket getSSLServerSocket(int port, int aBacklog) throws IOException {

		SSLServerSocket sslServer;
		if (aBacklog > 0) {
			sslServer = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(port, aBacklog);
		} else {
			sslServer = (SSLServerSocket) getRSInterface().getServerSocketFactory(true).createServerSocket(port);
		}

		if (Boolean.valueOf(getParam("tcpNeedSSLClientAuth")).booleanValue()) {
			sslServer.setNeedClientAuth(true);
		}

		return sslServer;
	}

	/**
	 * Returns client SSL socket
	 * 
	 * @param host
	 *            host name
	 * @param port
	 *            port number
	 * @return the socket object
	 * @throws IOException
	 */
	private Socket getSSLSocket(String host, int port) throws IOException {
		SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
		return socket;
	}

	/**
	 * Returns the Socket we are reading from or writing to
	 * 
	 * @return the Socket we are reading from or writing to
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Returns the Certificates of the peer. If this is not an SSL Session,
	 * return null, otherwise return the same as
	 * 
	 * <pre>
	 * getSocket().getSession().getPeerCertificates();
	 * </pre>
	 * 
	 * @return an ordered array of peer certificates, with the peer's own
	 *         certificate first followed by any certificate authorities.
	 * @throws SSLPeerUnverifiedException
	 *             if the peer's identity has not been verified
	 */
	public java.security.cert.Certificate[] getCertificates() throws SSLPeerUnverifiedException {
		if (!(socket instanceof SSLSocket))
			return null;
		SSLSession sess = ((SSLSocket) socket).getSession();
		if (sess == null)
			return null;

		return sess.getPeerCertificates();
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * Inner class for handling socket connections
	 */
	private static class SocketThread extends Thread {
		/**
		 * server socket
		 */
		private ServerSocket serverSocket;
		/**
		 * client socket
		 */
		private Socket socket = null;

		/**
		 * Class constructor
		 * 
		 * @param s
		 *            server socket
		 */
		SocketThread(ServerSocket s) {
			serverSocket = s;
		}

		/**
		 * Accepts client connection
		 */
		public void run() {
			try {
				socket = serverSocket.accept();
			} catch (Exception ioe) {
				socket = null;
			}
		}

		/**
		 * Retrieves client socket
		 * 
		 * @return the client socket
		 */
		Socket getSocket() {
			return socket;
		}
	}

}
