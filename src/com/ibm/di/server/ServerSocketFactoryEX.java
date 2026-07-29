/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.ibm.di.function.SystemFunctions;

/**
 * This class provides methods for getting both
 * <code>java.net.ServerSocket</code> and
 * <code>javax.net.ssl.SSLServerSocket</code> objects. This class is for
 * internal use only and you should not rely on it for other purpose.
 * 
 * @since 7.1
 */
public class ServerSocketFactoryEX extends ServerSocketFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default backlog value. In general the default value is 50. However, using
	 * an invalid value causes the constructor to use its default one. This is
	 * safe in case this value is changed in the JVM.
	 */
	private static final int DEFAULT_BACKLOG = -1;

	/**
	 * Object for retrieving the bind addresses.
	 */
	private BindAddressPolicy bindAddrPolicy;

	/**
	 * ServerSocketFactory object for creating non-SSL ServerSockets
	 */
	private ServerSocketFactory serverSF;

	/**
	 * SSLServerSocketFactory object for creating SSL ServerSockets
	 */
	private SSLServerSocketFactory sslServerSF;

	/**
	 * Determine if SSL or non-SSL factory
	 */
	private boolean useSSL;

	/**
	 * Constructor. Takes the <code>BindAddressPolicy</code> object where bind
	 * addresses will be obtained from. The <code>isSSLon</code> parameter
	 * determines if this class wraps a
	 * <code>javax.net.ssl.SSLServerSocketFactory</code> or
	 * <code>javax.net.ServerSocketFactory</code>. If set to <code>True</code>
	 * when some of the create methods is called the SSLServerSocket is
	 * returned.
	 * 
	 * @param aBindAddrPolicy
	 *            Object where bind addresses are obtained from.
	 * @param isSSLon
	 *            Determines if SSL or non-SSL Server Socket Factory is wrapped.
	 */
	public ServerSocketFactoryEX(BindAddressPolicy aBindAddrPolicy, boolean isSSLon) {
		bindAddrPolicy = aBindAddrPolicy;
		useSSL = isSSLon;

		serverSF = ServerSocketFactory.getDefault();
		sslServerSF = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	}

	/**
	 * Creates Server Socket on the specified port.
	 * 
	 * @param port
	 *            The port on which the Server Socket is created
	 */
	public ServerSocket createServerSocket(int port) throws IOException {
		return createServerSocket(port, DEFAULT_BACKLOG);
	}

	/**
	 * Creates Server Socket on the specified port and backlog
	 * 
	 * @param port
	 *            The port on which the Server Socket is created
	 * @param backlog
	 *            how many connections are queued
	 */
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return createServerSocket(port, backlog, bindAddrPolicy.getBindAddress());
	}

	/**
	 * Creates Server Socket on the specified port, backlog and InetAddress.
	 * 
	 * @param port
	 *            - the port to listen to
	 * @param backlog
	 *            - how many connections are queued
	 * @param ifAddress
	 *            - the network interface address to use
	 */
	public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
		if (useSSL) {
			boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");
			SSLServerSocket socket = (SSLServerSocket)sslServerSF.createServerSocket(port, backlog, ifAddress);

			if(NIST)
			{
				socket.setEnabledProtocols(socket.getSupportedProtocols());
				ArrayList<String> ciphers = new ArrayList<String>();
				for (String cipher: socket.getSupportedCipherSuites()) {
					if (! cipher.contains("RC4"))
						ciphers.add(cipher);
				}
				socket.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));

			}
			
			SystemFunctions.verifySSLProtocols(socket);
			
           	return socket;

			//return (SSLServerSocket) sslServerSF.createServerSocket(port, backlog, ifAddress);
		} else {
			return serverSF.createServerSocket(port, backlog, ifAddress);
		}
	}
}
