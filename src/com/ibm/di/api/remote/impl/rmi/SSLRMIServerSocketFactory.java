/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.Principal;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;

import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.RS;

/**
 * 
 * Instances of this class are used by the RMI to obtain SSL server sockets for
 * RMI calls. This class implements RMIServerSocketFactory interface.
 * 
 */
public class SSLRMIServerSocketFactory implements RMIServerSocketFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * thread local variables
	 */
	private static final ThreadLocal<Socket> mSockets = new ThreadLocal<Socket>();

	/**
	 * Default backlog value. In general the default value is 50. However, using
	 * an invalid value causes the constructor to use its default one. This is
	 * safe in case this value is changed in the JVM.
	 */
	private static final int DEFAULT_BACKLOG = -1;

	/**
	 * The server socket factory for creating sockets
	 */
	private SSLServerSocketFactory mServerSocketFactory;

	/**
	 * The function which is used for assigning of hashcode. In this case its a
	 * static number.
	 */
	private int mHashCode = 101;

	/**
	 * Whether to turn SSL on or not. True by default.
	 */
	private boolean mSSLon = true;

	/**
	 * Whether remote SSL client authorization is on or not
	 */
	private boolean mNeedClientAuth = false;

	/**
	 * Retrieves the bind addresses.
	 */
	private BindAddressPolicy bindAddressPolicy = null;

	/**
	 * Creates SSLRMIServerSocketFactory object.
	 */
	public SSLRMIServerSocketFactory() {
		mSSLon = false;
	}

	/**
	 * Creates SSLRMIServerSocketFactory object.
	 * 
	 * @param bindAddrPol
	 *            Object for obtaining the bind addresses from.
	 */
	public SSLRMIServerSocketFactory(BindAddressPolicy bindAddrPol) {
		this();
		bindAddressPolicy = bindAddrPol;
	}

	/**
	 * Creates SSLRMIServerSocketFactory object with specified or not use of
	 * custom settings.
	 * 
	 * @param aUseCustomSecuritySettings
	 *            whether to use custom security settings or not
	 * @throws Exception
	 *             if the security protocol is not available in the default
	 *             provider package or any of the other provider packages that
	 *             were searched.
	 */

	public SSLRMIServerSocketFactory(boolean aUseCustomSecuritySettings) throws Exception {
		this(aUseCustomSecuritySettings, false);
	}

	/**
	 * Creates SSLRMIServerSocketFactory object with specified or not use of
	 * custom settings.
	 * 
	 * @param aUseCustomSecuritySettings
	 *            whether to use custom security settings or not
	 * @param bindAddrPol
	 *            Object for obtaining the bind addresses from.
	 * @throws Exception
	 *             if the security protocol is not available in the default
	 *             provider package or any of the other provider packages that
	 *             were searched.
	 */
	public SSLRMIServerSocketFactory(boolean aUseCustomSecuritySettings, BindAddressPolicy bindAddrPol) throws Exception {
		this(aUseCustomSecuritySettings, false, bindAddrPol);
	}

	/**
	 * Creates SSLRMIServerSocketFactory object with specified or not use of
	 * custom settings and stash.
	 * 
	 * @param aUseCustomSecuritySettings
	 *            whether to use custom security settings or not
	 * @param isTDIServerSide
	 *            Whether the code is executing on the side of the Directory
	 *            Integrator Server. <code>False</code> means that the code is
	 *            executing on a Server API client.
	 * @throws Exception
	 *             if the security protocol is not available in the default
	 *             provider package or any of the other provider packages that
	 *             were searched.
	 */

	public SSLRMIServerSocketFactory(boolean aUseCustomSecuritySettings, boolean isTDIServerSide) throws Exception {
		if (aUseCustomSecuritySettings) {
			if (isTDIServerSide) {
				mHashCode = 102;
			} else {
				mHashCode = 103;
			}
			SSLContext context = SSLRMIUtils.getSSLContext(isTDIServerSide);
			mServerSocketFactory = context.getServerSocketFactory();
		} else {
			mHashCode = 104;
			String proto = System.getProperty("com.ibm.di.SSLServerProtocols");
			if (proto == null)
				proto = System.getProperty("com.ibm.di.SSLProtocols");
			if (proto != null && proto.contains(","))
				proto = proto.split(",")[0].trim();
			else if (proto == null || proto.isEmpty())
				proto = "TLS";
			try {
				mServerSocketFactory = SSLContext.getInstance(proto.trim()).getServerSocketFactory();
			} catch (Exception e) {
				mServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			}
		}
		if (System.getProperty(Constants.PROP_API_REMOTE_SSL_CLIENT_AUTH_ON) != null) {
			mNeedClientAuth = Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_CLIENT_AUTH_ON);
		}
	}

	/**
	 * Creates SSLRMIServerSocketFactory object with specified or not use of
	 * custom settings and stash.
	 * 
	 * @param aUseCustomSecuritySettings
	 *            whether to use custom security settings or not
	 * @param isTDIServerSide
	 *            Whether the code is executing on the side of the Directory
	 *            Integrator Server. <code>False</code> means that the code is
	 *            executing on a Server API client.
	 * @param bindAddrPol
	 *            Object for obtaining the bind addresses from.
	 * @throws Exception
	 *             if the security protocol is not available in the default
	 *             provider package or any of the other provider packages that
	 *             were searched.
	 */
	public SSLRMIServerSocketFactory(boolean aUseCustomSecuritySettings, boolean isTDIServerSide, BindAddressPolicy bindAddrPol)
			throws Exception {
		this(aUseCustomSecuritySettings, isTDIServerSide);
		bindAddressPolicy = bindAddrPol;

	}

	/**
	 * Sets the local thread socket.
	 * 
	 * @param aSocket
	 *            Socket object
	 */
	protected static void setLocalThreadSocket(Socket aSocket) {
		mSockets.set(aSocket);
	}

	/**
	 * Gets the local thread principal.
	 * 
	 * @return subject distinguished name of the peer's own certificate.
	 * @throws Exception
	 *             if local thread socket or SSL socket session are null, or SSL
	 *             session certificate chain is empty.
	 */
	public static Principal getLocalThreadPrincipal() throws Exception {

		SSLSocket socket = (SSLSocket) mSockets.get();
		if (socket == null) {
			// msg "SERVER.API.REMOTE.RMI.NO.SSL.SOCKET"
			throw new Exception("No SSL socket is associated with the current thread.");
		}

		SSLSession session = socket.getSession();
		if (session == null) {
			// msg "SERVER.API.REMOTE.RMI.SSL.SOCKET.SESSION.NULL"
			throw new Exception("SSL socket session is NULL.");
		}

                return session.getPeerPrincipal();
        }

	/**
	 * Create a server socket on the specified port (port 0 indicates an
	 * anonymous port).
	 * 
	 * @param aPort
	 *            the port number
	 * @return the server socket on the specified port
	 * @throws IOException
	 */
	public ServerSocket createServerSocket(int aPort) throws IOException {
		InetAddress bindAddr = getBindAddress();
		if (!mSSLon) {
			return new ServerSocket(aPort, DEFAULT_BACKLOG, bindAddr);
		}
		SSLServerSocket socket = (SSLServerSocket) mServerSocketFactory.createServerSocket(aPort, DEFAULT_BACKLOG, bindAddr);
		if (mNeedClientAuth) {
			socket.setNeedClientAuth(true);
		}

		boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");

		if (NIST)
		{
			// Add the next line to enable all supported protocols
			socket.setEnabledProtocols(socket.getSupportedProtocols());

			ArrayList<String> ciphers = new ArrayList<String>();
			for (String cipher: socket.getSupportedCipherSuites()) {
				if (! cipher.contains("RC4"))
					ciphers.add(cipher);
			}
			socket.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
		}

		String s = System.getProperty("com.ibm.di.SSLServerProtocols");
		if (s != null) {
			socket.setEnabledProtocols(s.split(", *"));
		}
		
		return (new ServerSocketWrapper(socket));
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument
	 *         and they have same hash codes; <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		return (getClass() == obj.getClass() && mHashCode == ((SSLRMIServerSocketFactory) obj).mHashCode);
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return mHashCode;
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
