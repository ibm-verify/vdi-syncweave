/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.Provider;

/**
 * Utility class that holds common functionality for the RMI SSL socket
 * factories.
 * 
 * @since 7.0
 */
public class SSLRMIUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Prepare an SSL context. The context is initialized using the settings
	 * specified as Java system properties. For a reference of available
	 * properties refer to {@link com.ibm.di.api.remote.impl.rmi.Constants}.
	 * 
	 * @param isTDIServerSide
	 *            Whether the code is executing on the side of the Directory
	 *            Integrator Server. <code>False</code> means that the code is
	 *            executing on a Server API client.
	 * @return The created SSL context.
	 * @throws Exception
	 *             If an error occurs while preparing the SSL context. For
	 *             example a keystore password is invalid.
	 */
	static SSLContext getSSLContext(boolean isTDIServerSide) throws Exception {
		boolean pkcsStatus = Boolean
				.getBoolean(Constants.PROP_GET_PKCS11_STATUS);
		if (pkcsStatus) {
			return getSSLContextUsingHardwareKeys();
		} else {
			return getSSLContextUsingSoftwareKeys(isTDIServerSide);
		}
	}

	/**
	 * Prepare an SSL context using keys from keystores located on the file
	 * system. The context is initialized using the settings specified as Java
	 * system properties. For a reference of available properties refer to
	 * {@link com.ibm.di.api.remote.impl.rmi.Constants}.
	 * 
	 * @param isTDIServerSide
	 *            Whether the code is executing on the side of the Directory
	 *            Integrator Server. <code>False</code> means that the code is
	 *            executing on a Server API client.
	 * @return The created SSL context.
	 * @throws Exception
	 *             If an error occurs while preparing the SSL context. For
	 *             example a keystore file is missing.
	 */
	private static SSLContext getSSLContextUsingSoftwareKeys(
			boolean isTDIServerSide) throws Exception {

		String keyStorePath;
		String keyStorePass;
		String keyStoreType;
		String keyPass;
		String trustStorePath;
		String trustStorePass;
		String trustStoreType;

		/*
		 * Different set of system properties are used depending on the side, on
		 * which the code is executing.
		 */
		if (isTDIServerSide) {

			keyStorePath = getRequiredProperty(Constants.PROP_SERVER_KEYSTORE);
			keyStorePass = getRequiredProperty(Constants.PROP_SERVER_KEYSTORE_PASSWORD);
			keyStoreType = System
					.getProperty(Constants.PROP_SERVER_KEYSTORE_TYPE);

			keyPass = System.getProperty(Constants.PROP_SERVER_KEY_PASSWORD);

			trustStorePath = getRequiredProperty(Constants.PROP_API_TRUSTSTORE);
			trustStorePass = getRequiredProperty(Constants.PROP_API_TRUSTSTORE_PASS);
			trustStoreType = System
					.getProperty(Constants.PROP_API_TRUSTSTORE_TYPE);
		} else {

			keyStorePath = getRequiredProperty(Constants.PROP_API_CLIENT_KEYSTORE);
			keyStorePass = getRequiredProperty(Constants.PROP_API_CLIENT_KEYSTORE_PASS);
			keyStoreType = System.getProperty(Constants.PROP_API_CLIENT_KEYSTORE_TYPE);

			keyPass = System.getProperty(Constants.PROP_API_CLIENT_KEY_PASS);

			trustStorePath = System.getProperty(Constants.PROP_API_CLIENT_TRUSTSTORE);
			if (trustStorePath == null) {
				trustStorePath = getRequiredProperty(Constants.PROP_API_TRUSTSTORE);
			}
			trustStorePass = System.getProperty(Constants.PROP_API_CLIENT_TRUSTSTORE_PASS);
			if (trustStorePass == null) {
				trustStorePass = getRequiredProperty(Constants.PROP_API_TRUSTSTORE_PASS);
			}
			trustStoreType = System.getProperty(Constants.PROP_API_CLIENT_TRUSTSTORE_TYPE);
			if (trustStoreType == null) {
				trustStoreType = System.getProperty(Constants.PROP_API_TRUSTSTORE_TYPE);
			}
		}

		if (keyStoreType == null || keyStoreType.trim().length() == 0) {
			keyStoreType = KeyStore.getDefaultType();
		}
		if (trustStoreType == null || trustStoreType.trim().length() == 0) {
			trustStoreType = KeyStore.getDefaultType();
		}
		if (keyPass == null || keyPass.length() == 0) {
			keyPass = keyStorePass;
		}

		return getSSLContext(keyStorePath, keyStorePass, keyStoreType, keyPass,
				trustStorePath, trustStorePass, trustStoreType);
	}

	/**
	 * Prepare an SSL context.
	 * 
	 * @param keyStorePath
	 *            The path of the keystore file that holds the private key of
	 *            the SSL server side.
	 * @param keyStorePass
	 *            The password of the keystore.
	 * @param keyStoreType
	 *            The type of the keystore.
	 * @param keyPass
	 *            The password of the private key.
	 * @param trustStorePath
	 *            The path of the truststore file that holds certificates of
	 *            trusted parties.
	 * @param trustStorePass
	 *            The password of the truststore.
	 * @param trustStoreType
	 *            The type of the truststore.
	 * @return The created SSL context.
	 * @throws Exception
	 *             If an error occurs while preparing the SSL context. For
	 *             example a keystore file is missing.
	 */
	private static SSLContext getSSLContext(String keyStorePath,
			String keyStorePass, String keyStoreType, String keyPass,
			String trustStorePath, String trustStorePass, String trustStoreType)
			throws Exception {

		SSLContext ctx = null;
		
		boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");

		String proto = null;
		if (NIST)
		{
			proto = "TLSv1.2";
		} else {
			proto = System.getProperty("com.ibm.di.SSLProtocols");
			if (proto != null)
				proto = proto.split(",")[0].trim();
			else
				proto = "TLS";
		}
		try {
			ctx = SSLContext.getInstance(proto);
		} catch (Exception e) {
			ctx = SSLContext.getInstance("TLS");
		}

		String providerClass = System.getProperty("com.ibm.di.sslProvider");
		Provider provider = null;
		if (providerClass != null && providerClass.trim().length() > 0) {
			provider = (Provider) Class.forName(providerClass).newInstance();
		}

		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		InputStream keyStoreStream = null;
		try {
			keyStoreStream = new FileInputStream(keyStorePath);
			keyStore.load(keyStoreStream, keyStorePass.toCharArray());
		} finally {
			if (keyStoreStream != null) {
				keyStoreStream.close();
			}
		}

		KeyManagerFactory kmFactory;
		if (provider != null) {
			kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory
					.getDefaultAlgorithm(), provider);
		} else {
			kmFactory = KeyManagerFactory.getInstance(KeyManagerFactory
					.getDefaultAlgorithm());
		}
		kmFactory.init(tempStore(keyStore, keyPass), keyPass.toCharArray());

		KeyStore trustStore = KeyStore.getInstance(trustStoreType);
		InputStream trustStoreStream = null;
		try {
			trustStoreStream = new FileInputStream(trustStorePath);
			trustStore.load(trustStoreStream, trustStorePass.toCharArray());
		} finally {
			if (trustStoreStream != null) {
				trustStoreStream.close();
			}
		}

		TrustManagerFactory tmFactory;
		if (provider != null) {
			tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory
					.getDefaultAlgorithm(), provider);
		} else {
			tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory
					.getDefaultAlgorithm());
		}
		tmFactory.init(trustStore);

		ctx
				.init(kmFactory.getKeyManagers(), tmFactory.getTrustManagers(),
						null);

		return ctx;
	}

	/**
	 * Create a temporary keystore containing only the server alias.
	 * @param keyStore
	 * @return
	 */
	private static KeyStore tempStore(KeyStore keyStore, String pw) {
		String alias = System.getProperty(Constants.PROP_SERVER_KEY_ALIAS);
		if (alias == null || alias.isEmpty())
			return keyStore;

		try {
			Key key = keyStore.getKey(alias, pw.toCharArray());
			if (key == null)
				return keyStore;

			KeyStore temp = KeyStore.getInstance("jks");
			temp.load(null, pw.toCharArray());
			temp.setKeyEntry(alias, key, pw.toCharArray(), keyStore.getCertificateChain(alias));
			return temp;
		} catch (Exception e) {
			return keyStore;
		}
	}

	/**
	 * Prepare an SSL context using keys from keystores located on a hardware
	 * device, which can be accessed via a PKCS#11 native API. The context is
	 * initialized using the settings specified as Java system properties. For a
	 * reference of available properties refer to
	 * {@link com.ibm.di.api.remote.impl.rmi.Constants}.
	 * 
	 * 
	 * @return The created SSL context.
	 * @throws Exception
	 *             If an error occurs while preparing the SSL context.
	 */
	private static SSLContext getSSLContextUsingHardwareKeys() throws Exception {

		SSLContext ctx = null;

		String cfgPath = System.getProperty(Constants.PROP_PKCS11_CFG_PATH);

		/*
		 * Add the PKCS-11 driver.  The guidance is to use SunPKCS-11
		 * driver: https://docs.oracle.com/javase/9/security/pkcs11-reference-guide1.htm#JSSEC-GUID-30E98B63-4910-40A1-A6DD-663EAF466991
		 */

		Provider provider = Security.getProvider("SunPKCS11");
		provider = provider.configure(cfgPath);
		Security.addProvider(provider); 

		// Work out the password for the card.
		String keypass = System.getProperty(Constants.PROP_PKCS11_PASS);
		char[] passphrase = keypass.toCharArray();

		// Get a keystore of type PKCS11 . The name of keystore
		// is not relevant for a PKCS11 keystore.
		KeyStore ks = KeyStore.getInstance("PKCS11");
		ks.load(null, passphrase);

		// Create a KeyManagerFactory that implements the X.509 key
		// management algorithm.
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

		// already logged into card. Password not required.
		kmf.init(ks, null);

		// Initialize the SSLContext with the KeyManagerFactory and the
		// default
		// TrustManager. Because there is no client authentication, no
		// trusted
		// certificates are required. SSL_TLS will allow the server to
		// handshake
		// using SSLv3 or TLSv1 protocol and will accept a v2
		// hello.
		ctx = SSLContext.getInstance("TLS");
		ctx.init(kmf.getKeyManagers(), null, null);

		return ctx;
	}

	/**
	 * Retrieves a required Java system property.
	 * 
	 * @param propName
	 *            The name of the property.
	 * @return The value of the property.
	 * @throws Exception
	 *             If the property is missing or empty.
	 */
	private static String getRequiredProperty(String propName) throws Exception {
		String value = System.getProperty(propName, "").trim();
		if (value.length() == 0) {
			// msg "SERVER.API.REMOTE.RMI.MISSING.PROPERTY"
			throw new Exception("Required property is missing: " + propName);
		}
		return value;
	}

}
