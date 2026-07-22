/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;

public class GetSSLCertificate {
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static ResourceHash resolver = ResourceHash.getHash("miserver");
	
	public static String installCertificateFrom(String urlString, int defaultPort) {
		URL url;
		try {
			url = new URL(urlString);
		} catch (Exception e) {
			return resolver.getString("GetSSLCertificate.URL", urlString);
		}
		
		String host = url.getHost();
		int port = url.getPort();
		if (port < 0)
			port = defaultPort;
		
		// Load data from truststore
		String type = System.getProperty("javax.net.ssl.trustStoreType");
		if (type == null || type.length() == 0)
			type = KeyStore.getDefaultType();
		String trustStoreFile = System.getProperty("javax.net.ssl.trustStore");
		String pw = System.getProperty("javax.net.ssl.trustStorePassword");
		KeyStore trustStore;
		InputStream in = null;
		try {
			trustStore = KeyStore.getInstance(type);

			in = new FileInputStream(trustStoreFile);
			trustStore.load(in, pw.toCharArray());
		} catch (Exception e) {
			return resolver.getString("GetSSLCerticate.TrustStore", 
					new Object[] {trustStoreFile, e.toString()});
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e2) {}
		}

		// Setup trust manager
		MyTrustManager mtm;
		SSLContext context;
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			mtm = new MyTrustManager((X509TrustManager) tmf.getTrustManagers()[0]);
			try {
				String protocol = null;
				String sslProtocols = System.getProperty("com.ibm.di.SSLProtocols");
				if (sslProtocols != null && ! sslProtocols.trim().isEmpty())
					protocol = sslProtocols.split(",")[0].trim();
				if (protocol == null || protocol.isEmpty())
					protocol = "TLS";
				context = SSLContext.getInstance(protocol);
			} catch (Exception e) {
				context = SSLContext.getInstance("TLS");
			}
			context.init(null, new TrustManager[] {mtm}, null);
		} catch (Exception e) {
			return resolver.getString("GetSSLCerticate.SSLContext", e);
		}

		SSLSocket socket;
		try {
			socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
			socket.setSoTimeout(5000);
		} catch (Exception e) {
			return resolver.getString("GetSSLCerticate.Connect", 
					new Object[] {host, port, e.toString()});
		}

		SystemFunctions.verifySSLProtocols(socket);

		try {
			socket.startHandshake();
			socket.close();
			// No Errors
			return resolver.getString("GetSSLCerticate.Already.Trusted");
		} catch (SSLException sse) {
			// This is the case that will be handled by code below
			SystemFunctions.doNothing();
		} catch (Exception e) {
			return resolver.getString("GetSSLCerticate.Socket.Err", e);
		}

		if (mtm.chain == null || mtm.chain.length == 0)
			return resolver.getString("GetSSLCerticate.No.Cert");

		OutputStream out = null;
		String name;
		try {
			X509Certificate cert = mtm.chain[mtm.chain.length - 1];
			name = cert.getSubjectDN().getName() + "." + cert.getSerialNumber();
			trustStore.setCertificateEntry(name, cert);
			out = new FileOutputStream(trustStoreFile);
			trustStore.store(out, pw.toCharArray());
			out.close();
		} catch (Exception e) {
			return resolver.getString("GetSSLCerticate.Unable.Add", e);
		} finally {
			try {
			if (out != null)
				out.close();
			} catch (Exception e2) {
				return resolver.getString("GetSSLCerticate.Unable.Add", e2);			
			}
		}

		return resolver.getString("GetSSLCerticate.Added.Cert", name);
	}

	private static class MyTrustManager implements X509TrustManager {

		private X509TrustManager tm;
		public X509Certificate[] chain;

		MyTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			return tm.getAcceptedIssuers();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}
}
