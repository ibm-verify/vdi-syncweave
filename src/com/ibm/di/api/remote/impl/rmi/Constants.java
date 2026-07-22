/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

/**
 * 
 * A convenience class containing property names used for the RMI.
 * 
 */
public class Constants {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Specifies the keystore file containing the server certificate.
	 */
	public static final String PROP_SERVER_KEYSTORE = "api.keystore";

	/**
	 * Property that holds the password for the key store file.
	 */
	public static final String PROP_SERVER_KEYSTORE_PASSWORD = "api.keystore.password";

	/**
	 * Property that holds the alias of the used key.
	 */
	public static final String PROP_SERVER_KEY_ALIAS = "api.key.alias";

	/**
	 * Property that holds the password for the used key.
	 */
	public static final String PROP_SERVER_KEY_PASSWORD = "api.key.password";

	/**
	 * The type of the keystore file specified by {@link #PROP_SERVER_KEYSTORE}.
	 * If missing or empty will use the default keystore file for the JVM
	 * (usually "jks").
	 */
	public static final String PROP_SERVER_KEYSTORE_TYPE = "api.keystore.type";

	/**
	 * Specifies the keystore file containing the client certificate.
	 */
	public static final String PROP_API_CLIENT_KEYSTORE = "api.client.keystore";

	/**
	 * Specifies the password of the keystore file specified by
	 * <code>api.client.keystore</code>.
	 */
	public static final String PROP_API_CLIENT_KEYSTORE_PASS = "api.client.keystore.pass";

	/**
	 * The type of the keystore file specified by
	 * {@link #PROP_API_CLIENT_KEYSTORE}. If missing or empty will use the
	 * default keystore file for the JVM (usually "jks").
	 */
	public static final String PROP_API_CLIENT_KEYSTORE_TYPE = "api.client.keystore.type";

	/**
	 * The password of the private key stored in keystore file specified by
	 * api.client.keystore; if this property is missing, the password specified
	 * by <code>api.client.keystore.pass</code> is used instead.
	 */
	public static final String PROP_API_CLIENT_KEY_PASS = "api.client.key.pass";
	
	/**
	 * Specifies the truststore file for a Server API client.
	 */
	public static final String PROP_API_CLIENT_TRUSTSTORE = "api.client.truststore";

	/**
	 * Specifies the password of the truststore file specified by
	 * {@link #PROP_API_CLIENT_TRUSTSTORE}.
	 */
	public static final String PROP_API_CLIENT_TRUSTSTORE_PASS = "api.client.truststore.pass";

	/**
	 * The type of the truststore file specified by
	 * {@link #PROP_API_CLIENT_TRUSTSTORE}. If missing or empty will use the
	 * default keystore file for the JVM (usually "jks").
	 */
	public static final String PROP_API_CLIENT_TRUSTSTORE_TYPE = "api.client.truststore.type";

	/**
	 * Specifies the keystore file containing the TDI Server public certificate.
	 */
	public static final String PROP_API_TRUSTSTORE = "api.truststore";

	/**
	 * Specifies the password for the keystore file specified by api.truststore.
	 */
	public static final String PROP_API_TRUSTSTORE_PASS = "api.truststore.pass";

	/**
	 * The type of the keystore file specified by {@link #PROP_API_TRUSTSTORE}.
	 * If missing or empty will use the default keystore file for the JVM
	 * (usually "jks").
	 */
	public static final String PROP_API_TRUSTSTORE_TYPE = "api.truststore.type";

	/**
	 * When is set to <code>true</code>, then SSL is configured through the
	 * following TDI Server API-specific Java System properties:
	 * <code><li> api.client.keystore </li>
	 * <li> api.client.keystore.pass</li>
	 * <li> api.client.key.pass </li>
	 * <li> api.truststore </li>
	 * <li> api.truststore.pass </li></code>
	 * <p>
	 * <br>
	 * When is missing or when it is set to <code>false</code>, then for
	 * configuring the SSL channel are used the standard JSSE system properties
	 * like : <code><li> javax.net.ssl.keyStore </li>
	 * <li> javax.net.ssl.keyStorePassword</li>
	 * <li> javax.net.ssl.trustStore </li>
	 * <li> javax.net.ssl.trustStorePassword </li></code>
	 */
	public static final String PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES = "api.client.ssl.custom.properties.on";

	/**
	 * If set to true SSL with client and server authentication will be used on
	 * RMI connections of the Server API and its JMX layer; the Server API will
	 * use the Server certificate and private key (the one specified through the
	 * <code>com.ibm.di.server.keystore</code> and
	 * <code>com.ibm.di.server.key.alias</code> properties) for SSL
	 * connections. RMI clients need to trust that certificate.
	 * <p>
	 * <br>
	 * If set to false no SSL is used for client connections and no
	 * authentication and authorization is performed; connections are accepted
	 * from the local host and from hosts listed in the
	 * <code>api.remote.nonssl.hosts</code> property; if
	 * api.remote.nonssl.hosts is empty only connections from the local host are
	 * accepted.
	 */
	public static final String PROP_API_REMOTE_SSL_ON = "api.remote.ssl.on";

	/**
	 * When is set to <code>false</code>, SSL-based authentication cannot be
	 * used. When the property is not specified a value of <code>false</code>
	 * is assumed.
	 */
	public static final String PROP_API_REMOTE_SSL_CLIENT_AUTH_ON = "api.remote.ssl.client.auth.on";

	/**
	 * Status of the PKCS #11.
	 * <p>
	 * If <code>true &quot;IBMPKCS11Impl&quot;</code> provider will be used;
	 * otherwise <code>java.security.Provider</code> will be used.
	 */
	public static final String PROP_GET_PKCS11_STATUS = "com.ibm.di.server.pkcs11";

	/**
	 * PKCS #11 passowrd.
	 */
	public static final String PROP_PKCS11_PASS = "com.ibm.di.server.pkcs11.password";

	/**
	 * PKCS #11 library name.
	 */
	public static final String PROP_PKCS11_LIBRARY = "com.ibm.di.server.pkcs11.library";

	/**
	 * PKCS #11 slot number.
	 */
	public static final String PROP_PKCS11_SLOT = "com.ibm.di.server.pkcs11.slot";

	/**
	 * The configuration file for creating <code>&quot;IBMPKCS11Impl&quot;</code> provider
	 * using the PKCS #11 library name and slot number.
	 */
	public static final String PROP_PKCS11_CFG_PATH = "com.ibm.di.pkcs11cfg";

}
