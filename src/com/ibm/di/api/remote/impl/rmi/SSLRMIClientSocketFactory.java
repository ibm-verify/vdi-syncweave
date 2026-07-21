/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.ibm.di.function.SystemFunctions;

/**
 * 
 * Instances of this class are used by the RMI to obtain SSL client sockets for
 * RMI calls. This class implements RMIClientSocketFactory class.
 * 
 */
public class SSLRMIClientSocketFactory implements RMIClientSocketFactory,
		Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 5083017546031420384L;

	/**
	 * Whether to check for the use of user custom security settings
	 */
	public static final int SSL_PROPERTIES_CLIENT_DEFINED = 100;

	/**
	 * if a server is defined and no SSL connection needed, a socket is created
	 * without additional initialization
	 */
	public static final int SSL_PROPERTIES_SERVER_DEFINED = 101;

	/**
	 * set to SSL_PROPERTIES_CLIENT_DEFINED or SSL_PROPERTIES_SERVER_DEFINED
	 */
	private int mSSLProperties = SSL_PROPERTIES_CLIENT_DEFINED;

	/**
	 * The SSL socket factory used to create sockets
	 */
	private transient SSLSocketFactory mSocketFactory = null;

	/**
	 * Creates SSLRMIClientSocketFactory
	 * 
	 * @param aSSLProperties
	 *            Client socket factory SSL properties
	 */
	public SSLRMIClientSocketFactory(int aSSLProperties) {
		mSSLProperties = aSSLProperties;
	}

	/**
	 * Creates a client socket connected to the specified host and port if
	 * <code>SSLSocketFactory</code> is not initialized
	 * 
	 * @param host
	 *            the host name
	 * @param port
	 *            the port name
	 * @return a socket connected to the specified host and port.
	 * @throws IOException
	 *             if an I/O error occurs during socket creation
	 */
	public Socket createSocket(String host, int port) throws IOException {

		// Determine whether we are executing on the TDI Server side
		boolean isTDIServerSide = mSSLProperties == SSL_PROPERTIES_SERVER_DEFINED;

		if (isTDIServerSide
				&& (System.getProperty(Constants.PROP_API_REMOTE_SSL_ON) != null)
				&& (!Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_ON))) {
			return new Socket(host, port);
		}

		if (mSocketFactory == null) {
			mSocketFactory = createSSLSocketFactory(isTDIServerSide);
		}

		boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");
		Socket ret = mSocketFactory.createSocket(host, port);

		if (NIST)
		{
			if (ret instanceof SSLSocket) {
				SSLSocket sock = (SSLSocket) ret;
				sock.setEnabledProtocols(sock.getSupportedProtocols());

				List<String> ciphers = new ArrayList<String>();
				for (String cipher: sock.getSupportedCipherSuites()) {
					if (! cipher.contains("RC4"))
						ciphers.add(cipher);
				}
				sock.setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
			}
		}
		SystemFunctions.verifySSLProtocols(ret);
		return ret;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj
	 *         argument and they have same SSL properties; <code>false</code>
	 *         otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		return (getClass() == obj.getClass() && mSSLProperties == ((SSLRMIClientSocketFactory) obj).mSSLProperties);
	}

	/**
	 * Returns a hash code value for the object.
	 * <p>
	 * For <code>SSLRMIClientSocketFactory</code> class the hash code is
	 * considered to be equal to the value the value of SSL properties.
	 * 
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return mSSLProperties;
	}

	/**
	 * Create an SSL socket factory based on the settings specified as Java
	 * system properties. For a reference of available properties refer to
	 * {@link com.ibm.di.api.remote.impl.rmi.Constants}.
	 * 
	 * @param isTDIServerSide
	 *            Whether the code is executing on the side of the Directory
	 *            Integrator Server. <code>False</code> means that the code is
	 *            executing on a Server API client.
	 * @return The created SSL socket factory.
	 * @throws IOException
	 *             If an error occurs while creating the socket factory, e.g. if
	 *             a keystore file is missing.
	 */
	private static SSLSocketFactory createSSLSocketFactory(
			boolean isTDIServerSide) throws IOException {
		boolean useCustomSecuritySettings = true;

		if (!isTDIServerSide) {
			useCustomSecuritySettings = Boolean
					.getBoolean(Constants.PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES);
		}

		SSLSocketFactory sslSocketFactory = null;

		if (useCustomSecuritySettings) {
			SSLContext context = null;
			try {
				context = SSLRMIUtils.getSSLContext(isTDIServerSide);
			} catch (Exception e) {
				// msg "SERVER.API.REMOTE.RMI.UNABLE.TO.CREATE.SOCKET"
				throw new IOException("Unable to create socket: " + e);
			}
			sslSocketFactory = context.getSocketFactory();
		} else {
			String proto = System.getProperty("com.ibm.di.SSLProtocols");
			if (proto != null && proto.contains(","))
				proto = proto.split(",")[0].trim();
			else if (proto == null || proto.isEmpty())
				proto = "TLS";
			try {
				sslSocketFactory = SSLContext.getInstance(proto.trim()).getSocketFactory();
			} catch (Exception e) {
				sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
		}
		return sslSocketFactory;
	}

}
