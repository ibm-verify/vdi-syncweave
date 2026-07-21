/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import java.net.*;
import java.io.*;

import java.security.*;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.BindAddressPolicyImpl;
import com.ibm.di.server.ServerSocketFactoryEX;

// Tai5.2 : import com.sun.net.ssl.*;

public class SSL {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Socket getClientSocket(String host, int port) throws Exception {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		return factory.createSocket(host, port);
	}

	public static Socket getClientSocketAuth(String keystore, String keypass, String host, int port) throws Exception {
		// set up a key manager so that we can do client authentication
		// if asked by server
		SSLSocketFactory factory = null;
		SSLContext ctx;
		KeyManagerFactory kmf;
		KeyStore ks;

		char[] passphrase = keypass.toCharArray();
		
		boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");

		if(NIST)
			ctx = SSLContext.getInstance("TLSv1.2");
		else
			ctx = SSLContext.getInstance("TLS");

		// Setup key manager
		kmf = KeyManagerFactory.getInstance("SunX509");
		ks = KeyStore.getInstance("JKS");
		InputStream ksStream = null;
		try {
			ksStream = new FileInputStream(keystore);
			ks.load(ksStream, passphrase);
		} finally {
			if (ksStream != null) {
				ksStream.close();
			}
		}
		kmf.init(ks, passphrase);

		// Setup trust manager
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		// Initialize SSL context
		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		// Connect and return socket
		factory = ctx.getSocketFactory();
		return factory.createSocket(host, port);
	}

	public static ServerSocket getServerSocket(int port) throws Exception {
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(System.getProperties());
		ServerSocketFactory factory = new ServerSocketFactoryEX(bindAddr, true);
		return factory.createServerSocket(port);
	}

}
