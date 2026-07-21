/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Vector;

import com.ibm.di.server.BindAddressPolicy;

/**
 * A wrapper class of RMIServerSocketFactory class. Class used by the RMI to
 * obtain server and client sockets for RMI calls. This Factory is running only
 * on TDI Server side.
 * 
 */
public class RMISocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -3200652858929712303L;

	/**
	 * thread local variables
	 */
	private transient static final ThreadLocal<Socket> mSockets = new ThreadLocal<Socket>();

	/**
	 * Vector object of provided hosts
	 */
	private transient static Vector<String> mNonSSLHosts = null;

	/**
	 * Default backlog value. Setting invalid value of -1 will cause the JVM to
	 * use its default backlog value when creating ServerSockets.
	 */
	private static final int DEFAULT_BACKLOG = -1;

	/**
	 * Retrieves the bind addresses.
	 */
	private transient BindAddressPolicy bindAddressPolicy = null;

	/**
	 * Constructor with two parameters.
	 * 
	 * @param aHosts
	 *            Vector object of provided hosts
	 * @param bindAddrPol
	 *            Object where bind addresses are obtained from
	 * 
	 */
	public RMISocketFactory(Vector<String> aHosts, BindAddressPolicy bindAddrPol) {
		this(aHosts);
		bindAddressPolicy = bindAddrPol;
	}

	/**
	 * Constructor with two parameters.
	 * 
	 * @param aHosts
	 *            Vector object of provided hosts
	 * @param bindAddrPol
	 *            Object where bind addresses are obtained from
	 * 
	 */
	public RMISocketFactory(Vector<String> aHosts) {
		mNonSSLHosts = aHosts;
	}

	/**
	 * Create a server socket on the specified port. The Bind Address is set
	 * implicitly.
	 * 
	 * @param aPort
	 *            the port. port 0 indicates an anonymous port
	 * @return the server socket on the specified port
	 * @throws IOException
	 *             if I/O error occurs during server socket creation
	 */
	public ServerSocket createServerSocket(int aPort) throws IOException {
		InetAddress bindAddr = getBindAddress();
		ServerSocket serverSocket = new ServerSocket(aPort, DEFAULT_BACKLOG, bindAddr);
		return new ServerSocketWrapper(serverSocket);
	}

	/**
	 * Create a client socket connected to the specified host and port.
	 * 
	 * @param host
	 *            the host name
	 * @param port
	 *            the port number
	 * @return a socket connected to the specified host and port
	 * @throws IOException
	 *             if I/O error occurs during socket creation
	 */
	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host, port);
	}

	/**
	 * Sets the current thread's copy of this thread-local variable to the
	 * specified value. Many applications will have no need for this
	 * functionality, relying solely on the initialValue method to set the
	 * values of thread-locals.
	 * 
	 * @param aSocket
	 *            the value to be stored in the current threads' copy of this
	 *            thread-local.
	 */
	protected static void setLocalThreadSocket(Socket aSocket) {
		mSockets.set(aSocket);
	}

	/**
	 * Verifies access of the socket associated with the current thread to this
	 * <code>RMISocketFactory</code>.
	 * 
	 * @return <code>true</code> if the socket has access, otherwise returns
	 *         <code>false</code>
	 * @throws Exception
	 *             if no <code>Socket</code> is associated with the current
	 *             thread
	 */
	public static boolean allowConnection() throws Exception {
		Socket socket = mSockets.get();
		if (socket == null) {
			throw new Exception("No Socket is associated with the current thread.");
		}

		// verify access
		InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
		String ip = sa.getAddress().getHostAddress();

		for (int i = 0; i < mNonSSLHosts.size(); i++) {
			if (ip.equalsIgnoreCase((String) mNonSSLHosts.get(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the IP address to bind the ServerSocket to. Note that
	 * <code>null</code> may be returned. This will cause the ServerSocket to
	 * bind to all available network interfaces.
	 * 
	 * @return IP address to bind to.
	 */
	private InetAddress getBindAddress() {
		if (bindAddressPolicy != null) {
			return bindAddressPolicy.getBindAddress();
		}
		return null;
	}
}
