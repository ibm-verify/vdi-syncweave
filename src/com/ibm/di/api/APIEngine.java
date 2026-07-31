/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.Policy;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.api.authentication.JAASAuthentication;
import com.ibm.di.api.authentication.LDAPAuthentication;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.api.local.Session;
import com.ibm.di.api.local.SessionFactory;
import com.ibm.di.api.remote.impl.rmi.APIRemoteSecurityManager;
import com.ibm.di.api.remote.impl.rmi.RMISocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIClientSocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.api.security.Identity;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.api.tm.TombstoneManager;
import com.ibm.di.api.tm.TombstoneManagerListener;
import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.osgi.OSGiContainerHandle;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * This is the core class of the Server API. The APIEngine class does all
 * initialization of the Server API. Both the local and the remote APIs are
 * initialized by this class. During the initialization the values of the
 * "api.*" properties in global.properties/solution.properties are used. This
 * class provides methods which give access to a local session needed for using
 * the local API.
 */
public class APIEngine {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "serverapiengine";

	// API property names
	/**
	 * Property name used to switch the TDI local API on and off.
	 */
	public static final String PROP_API_ON = "api.on";

	/**
	 * Property name used to specify the path to the user registry.
	 */
	public static final String PROP_API_USER_REGISTRY = "api.user.registry";

	/**
	 * Property name used to switch the user registry's encryption on and off.
	 */
	public static final String PROP_API_USER_REGISTRY_ENCRYPTION_ON = "api.user.registry.encryption.on";

	/**
	 * Property name used to switch the TDI remote API on and off.
	 */
	public static final String PROP_API_REMOTE_ON = "api.remote.on";

	/**
	 * Property name used to switch the TDI TP Server on and off.
	 */
	public static final String PROP_TP_SERVER_ON = "tp.server.on";

	/**
	 * Property name used to switch the TDI REST Server API on and off.
	 */
	public static final String PROP_REST_SERVER_ON = "api.rest.on";

	/**
	 * Property name used to set the port the remote server API will listen on.
	 */
	public static final String PROP_API_REMOTE_NAMING_PORT = "api.remote.naming.port";

	/**
	 * Property name used to switch the SSL for the remote API on and off.
	 */
	public static final String PROP_API_REMOTE_SSL_ON = "api.remote.ssl.on";

	/**
	 * Property name used to switch the client authentication on and off.
	 */
	public static final String PROP_API_REMOTE_SSL_CLIENT_AUTH_ON = "api.remote.ssl.client.auth.on";

	/**
	 * Property name used to specify the remote hosts that could use the remote
	 * API without SSL. The values could be separated by the characters " ", ","
	 * or ";"
	 */
	public static final String PROP_API_REMOTE_NONSSL_HOSTS = "api.remote.nonssl.hosts";

	/**
	 * Property name used to switch the local JMX interface on and off.
	 */
	public static final String PROP_API_JMX_ON = "api.jmx.on";

	/**
	 * Property name used to switch the remote JMX interface on and off.
	 */
	public static final String PROP_API_JMX_REMOTE_ON = "api.jmx.remote.on";

	/**
	 * Property name used to switch the TombStone manager on and off.
	 */
	public static final String PROP_TOMBSTONE_MANAGER_ON = "com.ibm.di.tm.on";

	/**
	 * Property name used to specify the script file path used for the custom
	 * authentication.
	 */
	public static final String PROP_API_CUSTOM_AUTH = "api.custom.authentication";

	/**
	 * Property name used to switch the custom method invocation on and off.
	 */
	public static final String PROP_API_CUSTOM_METHOD_INVOKE = "api.custom.method.invoke.on";

	/**
	 * Property name used to specify the class names that could be invoked
	 * through {@link Session#invokeCustom(String, String, Object[])} and
	 * {@link Session#invokeCustom(String, String, Object[], String[])} methods
	 * of the Server API. The names could be separated by the characters " ",
	 * "," or ";"
	 */
	public static final String PROP_API_CUSTOM_METHOD_INVOKE_ALLOWED_CLASSES = "api.custom.method.invoke.allowed.classes";

	/**
	 * Property name used to mark the LDAP authentication as critical or not. If
	 * its value is <code>true</code> then the error that occurred while
	 * authenticating will be thrown as exception, otherwise it will only be
	 * logged as an error and no exception will be thrown.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_CRITICAL = "api.custom.authentication.ldap.critical";

	/**
	 * Property name that holds the host name of the LDAP server.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_HOSTNAME = "api.custom.authentication.ldap.hostname";

	/**
	 * Property name that holds the port of the LDAP server.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_PORT = "api.custom.authentication.ldap.port";

	/**
	 * Property name used to switch the usage of SSL on and off.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_USE_SSL = "api.custom.authentication.ldap.ssl";

	/**
	 * Property name used to specify the LDAP directory location where user
	 * searches will be performed. When this property is not specified user
	 * searches will not be performed.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_SEARCHBASE = "api.custom.authentication.ldap.searchbase";

	/**
	 * Property name used to specify the LDAP Server administrator distinguished
	 * name that will be used for user searches. When this property is not
	 * specified anonymous bind will be used for user searches.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_ADMIN_DN = "api.custom.authentication.ldap.admindn";

	/**
	 * Property name used to specify the password for the LDAP Server
	 * administrator distinguished name
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_ADMIN_PASSWORD = "api.custom.authentication.ldap.adminpassword";

	/**
	 * Property name used to specify the user id attribute to be used in
	 * searches. When this property is not specified user searches will not be
	 * performed.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_USER_ATTRIBUTE = "api.custom.authentication.ldap.userattribute";

	/**
	 * Property name used to specify whether LDAP Group authentication is turned
	 * on. If it is set to 'true', the group membership of the authenticating
	 * user will be resolved and will be taken into account during
	 * authorization. If it is missing, the default value 'false' is used.
	 * 
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_GROUP_SUPPORT = "api.custom.authentication.ldap.groupsupport";

	/**
	 * Property name used to specify the name of the attribute of a user in LDAP
	 * that contains a list of the groups of which the user is a member. It is
	 * taken into account only if
	 * {@link #PROP_API_CUSTOM_AUTH_LDAP_GROUP_SUPPORT} is set to true.
	 * 
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE = "api.custom.authentication.ldap.usermembershipattribute";

	/**
	 * Property name used to specify how groups are named in the membership
	 * attribute of a user. For example, if the user's membership attribute
	 * contains values, which correspond to the 'objectSID' attributes of
	 * groups, set this property to 'objectSID'. If the user's membership
	 * attribute contains distinguished names of groups, then set this property
	 * to 'dn'. The property is required in case
	 * {@link #PROP_API_CUSTOM_AUTH_LDAP_GROUP_SUPPORT} is set to true.
	 * 
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE_CONTENT = "api.custom.authentication.ldap.usermembershipattributecontent";

	/**
	 * Property name used to specify the name of a group's attribute in LDAP
	 * which corresponds to the way the group is named in the TDI User Registry.
	 * For example, if LDAP groups are addressed in the TDI registry by their
	 * common name, then set this property to 'cn'. If the User Registry
	 * contains the distinguished names of the groups, then set this property to
	 * 'dn'.
	 * 
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_GROUPNAME_ATTRIBUTE = "api.custom.authentication.ldap.groupnameattribute";

	/**
	 * Property name used to specify the LDAP directory context, where groups
	 * will be searched. It is required only when LDAP group support is enabled.
	 * 
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_GROUP_SEARCHBASE = "api.custom.authentication.ldap.groupsearchbase";

	/**
	 * Property name used to specify a list of space-separated attribute names.
	 * These attributes have non-string syntax.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_BINARY_ATTRIBUTES = "api.custom.authentication.ldap.binaryattributes";

	/**
	 * Possible value of the property {@link #PROP_API_CUSTOM_AUTH} specifying
	 * that the build-in LDAP authentication mechanism must be used.
	 */
	public static final String PROP_API_CUSTOM_AUTH_LDAP_VALUE = "[ldap]";

	/**
	 * Possible value of the property {@link #PROP_API_CUSTOM_AUTH} specifying
	 * that the build-in JAAS authentication mechanism must be used.
	 */
	public static final String PROP_API_CUSTOM_AUTH_JAAS_VALUE = "[jaas]";

	/**
	 * Property name used to specify a list of Server notification types, which
	 * will be suppressed. Notifications of suppressed types will not be
	 * propagated by the notifications framework. The notification types in the
	 * list are separated by spaces. Wildcards may be included. Example:
	 * api.notification.suppress=di.al.* di.ci.start The above example will
	 * suppress all Assembly Line related notifications as well as notifications
	 * for starting a configuration instance. If the property is missing or is
	 * empty, no notifications will be suppressed.
	 * 
	 * @since 7.0
	 */
	public static final String PROP_API_NOTIFICATION_SUPPRESSED_TYPES = "api.notification.suppress";

	/**
	 * A constant containing the default port on which the RMI registry runs.
	 * This value will be used if the property with name
	 * {@link #PROP_API_REMOTE_NAMING_PORT} is not set.
	 */
	public static final int DEFAULT_REGISTRY_PORT = 1099;

	/**
	 * This constant contains the name of the used for looking up the remote
	 * object required for establishing a session with the remote TDI Server.
	 */
	public static final String REMOTE_SESSION_FACTORY_NAME = "SessionFactory";

	/**
	 * This is the prefix for auto generated configuration ids for temporary
	 * configuration instances.
	 * 
	 * @see Session#startTempConfigInstance(String, boolean, String, String)
	 */
	public static final String TEMP_CONFIG_ID_PREFIX = "temp_config_instance";

	/**
	 * Delimiter variable.
	 */
	private static final String PARAM_DELIMITER = " ,;";

	/**
	 * Logs debug messages.
	 */
	private static org.apache.logging.log4j.Logger mLogger = org.apache.logging.log4j.LogManager.getLogger("com.ibm.di.api");

	/**
	 * Engine initialized flag.
	 */
	private static boolean mEngineInitialized = false;

	/**
	 * Used to create local session.
	 */
	private static com.ibm.di.api.local.impl.SessionFactoryImpl mSessionFactoryLocal = null;

	/**
	 * Used to create remote session.
	 * 
	 */
	private static com.ibm.di.api.remote.impl.SessionFactoryImpl mSessionFactoryRemote = null;

	/**
	 * A tracker object, responsible for tracking the state of a configInstance,
	 * assemblyLines objects.
	 */
	private static ProcessRegistry mProcessRegistry = null;

	/**
	 * Parses the User Registry file and hold all the user identities with their
	 * corresponding permissions.
	 */
	private static com.ibm.di.api.security.Registry mSecurityRegistry = null;

	/**
	 * {@link EventNotifier} instance.
	 */
	private static EventNotifier mEventNotifier = null;

	/**
	 * Port number.
	 */
	private static int mPort = 0;

	/**
	 * SSL enabled flag. Default <code>true</code>.
	 */
	private static boolean mSSLon = true;

	/**
	 * SSL client authentication flag. Default <code>false</code>.
	 */
	private static boolean mSSLClientAuthOn = false;

	/**
	 * {@link RMIServerSocketFactory} instance
	 */
	private static RMIServerSocketFactory mServerSF = null;

	/**
	 * {@link RMIClientSocketFactory} instance
	 */
	private static RMIClientSocketFactory mClientSF = null;

	/**
	 * {@link Registry} is a remote interface to a simple remote object registry
	 * that provides methods for storing and retrieving remote object references
	 * bound with arbitrary string names.
	 */
	private static Registry mRegistry = null;

	/**
	 * Manages {@link Tombstone} objects.
	 */
	private static com.ibm.di.api.tm.TombstoneManager mTombstoneManager = null;

	/**
	 * Represents the repository used for manipulating configInstances.
	 */
	private static ConfigurationRegistry mConfigRegistry = null;

	/**
	 * Used to execute a custom, user-defined script that will take care for
	 * authentication of the users.
	 */
	private static APIAuthenticator mAuthenticator = null;

	/**
	 * Provides the capability of authentication against a LDAP server.
	 */
	private static LDAPAuthentication mLDAPAuthenticator = null;

	/**
	 * Provides the capability of authentication against a JAAS module.
	 */
	private static JAASAuthentication mJAASAuthenticator = null;

	/**
	 * Custom method invocation flag.
	 */
	private static boolean mMethodInvoke = false;

	/**
	 * List of classes which can be invoked through invokeCustom() methods of
	 * the Server API.
	 */
	private static String mMethodInvokeClasses = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	/**
	 * Bind Address that the remote Server API will be bound to. If
	 * <code>null</code> then the Server API will be exposed to all available
	 * network interfaces.
	 */
	private static BindAddressPolicy remoteBindAddr = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Returns the object logging TMS messages.
	 * 
	 * @return the static object that is responsible for transferring key
	 *         strings to localized messages.
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * Initializes the Server API. It creates and initialize the Security
	 * Registry, the local Session Factory, the Process Factory, the Config
	 * Registry, the Event Notifier. Many of the initializations depends on the
	 * global properties values. Depending on the configuration API
	 * Authenticator and Tombstone Manager are also created.
	 * 
	 * @throws DIException
	 *             in case an initialization error occurs.
	 */
	public static void initialize() throws DIException {
		mEngineInitialized = false;

		mConfigRegistry = new ConfigurationRegistry();
		mConfigRegistry.startAutoUnlock();

		/*
		 * Override configuration instance naming policy to provide support for
		 * some Server API specific features such as solution names and run
		 * names.
		 */
		RS.ConfigInstanceNamingPolicy apiNamingPolicy = new APIConfigInstanceNamingPolicy(mConfigRegistry);
		RS.setConfigInstanceNamingPolicy(apiNamingPolicy);

		mSecurityRegistry = new com.ibm.di.api.security.Registry();
		mSecurityRegistry.initialize(System.getProperty(PROP_API_USER_REGISTRY), apiNamingPolicy);

		mSessionFactoryLocal = new com.ibm.di.api.local.impl.SessionFactoryImpl();

		mProcessRegistry = new ProcessRegistry();

		String suppressedEventTypes = System.getProperty(PROP_API_NOTIFICATION_SUPPRESSED_TYPES, EventNotifier.MATCH_NONE_FILTER);
		mEventNotifier = new EventNotifier(false, suppressedEventTypes);

		if (System.getProperty(PROP_API_REMOTE_SSL_ON) != null) {
			mSSLon = Boolean.getBoolean(PROP_API_REMOTE_SSL_ON);
		}
		if (System.getProperty(PROP_API_REMOTE_SSL_CLIENT_AUTH_ON) != null) {
			mSSLClientAuthOn = Boolean.getBoolean(PROP_API_REMOTE_SSL_CLIENT_AUTH_ON);
		}
		String authType = System.getProperty(PROP_API_CUSTOM_AUTH);
		if (authType != null) {
			if (authType.equalsIgnoreCase(PROP_API_CUSTOM_AUTH_LDAP_VALUE)) {
				initLDAPAuth();
			} else if (authType.equalsIgnoreCase(PROP_API_CUSTOM_AUTH_JAAS_VALUE)) {
				initJAASAuth();
			} else {
				String mCustomAuthenticationScript = "";
				try {
					mCustomAuthenticationScript = new String(CryptoUtils.readFile(authType));
				} catch (java.io.IOException e) {
					logErrorAndThrowException(sResHash.getString("SEVER.API.UNABLE.TO.READ.CUSTOM.SCRIPT.FROM.FILE", authType), e);
				}
				mAuthenticator = new APIAuthenticator(true, mCustomAuthenticationScript);
			}

		} else {
			mAuthenticator = new APIAuthenticator(false, null);
		}

		String portStr = System.getProperty(PROP_API_REMOTE_NAMING_PORT);
		if (portStr == null || portStr.length() == 0) {
			mPort = DEFAULT_REGISTRY_PORT;
			logError(sResHash.getString("SERVER.API.WILL.USE.DEFAULT.VALUE.REMOTE.NAMING.PORT", String.valueOf(mPort)));
		} else {
			try {
				mPort = Integer.parseInt(portStr);
			} catch (NumberFormatException e) {
				mPort = 0;
			}
		}

		if (mPort <= 0 || mPort > 65535) {
			throw new DIException(sResHash.getString("SERVER.API.INVALID.VALUE.REMOTE.NAMING.PORT", portStr));
		}

		if (mPort < 1024) {
			logWarn(sResHash.getString("SERVER.API.RESERVED.PORT", String.valueOf(mPort)));
		}

		mEngineInitialized = true;

		if (Boolean.getBoolean(PROP_TOMBSTONE_MANAGER_ON)) {
			startTombstoneManager();
		}

		if (Boolean.getBoolean(PROP_API_JMX_ON)) {
			JMXAgent.initialize();
		}

		if (Boolean.getBoolean(PROP_API_REMOTE_ON) || Boolean.getBoolean(PROP_API_JMX_REMOTE_ON)) {
			initSocketFactories();
		}

		if (System.getProperty(PROP_API_CUSTOM_METHOD_INVOKE) != null) {
			mMethodInvoke = Boolean.getBoolean(PROP_API_CUSTOM_METHOD_INVOKE);
		}
		logInfo(sResHash.getString("SEVER.API.CUSTOM.INVOKE.ON.OR.OFF", Boolean.valueOf(mMethodInvoke)));

		if (mMethodInvoke && System.getProperty(PROP_API_CUSTOM_METHOD_INVOKE_ALLOWED_CLASSES) != null) {
			mMethodInvokeClasses = System.getProperty(PROP_API_CUSTOM_METHOD_INVOKE_ALLOWED_CLASSES);
			logInfo(sResHash.getString("SEVER.API.ALLOWED.CUSTOM.CLASSES", mMethodInvokeClasses));
		}
	}

	private static void initSocketFactories() throws DIException{
		remoteBindAddr = new com.ibm.di.api.remote.impl.BindAddressPolicyImpl(System.getProperties(), true);
		if (remoteBindAddr.getBindAddress() != null) {
			System.setProperty("java.rmi.server.hostname", remoteBindAddr.getBindAddress().getHostAddress());
		}

		if (isSSLon()) {
			try {
				mServerSF = new SSLRMIServerSocketFactory(true, true, remoteBindAddr);
				mClientSF = new SSLRMIClientSocketFactory(SSLRMIClientSocketFactory.SSL_PROPERTIES_CLIENT_DEFINED);
				if (isDebugEnabled()) {
					logDebug(sResHash.getString("SEVER.API.SOCKET.FACTORIES.CREATED.SUCCESSFULLY"));
				}
			} catch (Exception e) {
				logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.CREATE.RMI.CUSTOM.SOCKET.FACTORIES"), e);
			}
		} else {
			Vector<String> nonSSLhosts = new Vector<String>();

			// add localhost IP(s)
			try {
				nonSSLhosts.add(java.net.InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.GET.LOCALHOST.IP.ADDRESS"), e);
			}

			/*
			 * Add the remote bind address if in a multihomed system the
			 * local host equals another IP.
			 */
			if (remoteBindAddr.getBindAddress() != null
					&& !nonSSLhosts.contains((String) remoteBindAddr.getBindAddress().getHostAddress())) {
				nonSSLhosts.add(remoteBindAddr.getBindAddress().getHostAddress());
			}

			try {
				nonSSLhosts.add(InetAddress.getByName("localhost").getHostAddress());
			} catch (UnknownHostException e) {
				// should never happen
				throw new DIException(e.getLocalizedMessage());
			}

			if (System.getProperty(PROP_API_REMOTE_NONSSL_HOSTS) != null) {
				String hosts = System.getProperty(PROP_API_REMOTE_NONSSL_HOSTS);
				for (StringTokenizer st = new StringTokenizer(hosts, PARAM_DELIMITER); st.hasMoreTokens();) {
					String token = st.nextToken();
					nonSSLhosts.add(token);
				}
			}

			mServerSF = new RMISocketFactory(nonSSLhosts, remoteBindAddr);
			mClientSF = (RMIClientSocketFactory) mServerSF;
		}
	}

	/**
	 * Starts a Thread monitoring the localhost address and calls initializeRemote to register
	 * the SessionFactory object anew when the local IP address changes.
	 * @param time - Number of milliseconds to sleep between each poll.
	 */
	public static void startThreadDetectingIPChange(final long time) {

		new Thread("IPAddressChangeDetector") {
			@Override
			public void run() {
				String localAddress = null;
				try {
					localAddress = InetAddress.getLocalHost().getHostAddress();
				} catch (Exception e) {
					logInfo(getName() + "-> " + e.toString());
					return;
				}
				while (true) {
					try {
						Thread.sleep(time);
						String currAddress = InetAddress.getLocalHost().getHostAddress();
						if (!currAddress.equals(localAddress)) {
							logInfo(getName() + ": " + localAddress + " -> " + currAddress);
							if (mRegistry != null) {
								try {
									mRegistry.unbind(REMOTE_SESSION_FACTORY_NAME);
								} catch (Exception ex) {
									logInfo(getName() + ": " + ex);
								}
								mRegistry = null;
								System.setProperty("java.rmi.server.hostname", currAddress);
								try {
									initRMIRegistry().unbind(REMOTE_SESSION_FACTORY_NAME);
								} catch (Exception e2) {
									logInfo(getName() + ":> " + e2);											
								}
								initSocketFactories();
								initializeRemote();
							}
							localAddress = currAddress;
						}
					} catch (Exception e) {
						if (e instanceof InterruptedException)
							break;
						logError(getName(), e);
					}
				}
			}
		}.start();
	}

	
	/**
	 * Starts a TombstoneManager unless there is already one running.
	 */
	public synchronized static void startTombstoneManager() throws DIException {
		if (mTombstoneManager != null)
			return;
		TombstoneManager tm = new TombstoneManager();
		tm.startAutoCleaner();
		mTombstoneManager = tm;
	}

	/**
	 * Gets the local {@link SessionFactory} created during the initialization
	 * of the Local Server API. This method is used by TDI components in order
	 * to gain access to the Local Server API.
	 * 
	 * @return an instance object representing the local session factory for
	 *         creating local sessions to the TDI Server.
	 * @throws DIException
	 *             if the API Engine is not initialized or an error occurs while
	 *             obtaining the session factory.
	 */
	public static SessionFactory getLocalSessionFactory() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mSessionFactoryLocal;
	}

	/**
	 * Creates a local Session object using a local SessionFactory created
	 * during the initialization. This method is used by TDI components in order
	 * to gain access to the Local Server API.
	 * 
	 * @return an instance object representing the local session with the TDI
	 *         Server.
	 * @throws DIException
	 *             if the API Engine is not initialized or an error occurs while
	 *             obtaining the session.
	 */
	public static com.ibm.di.api.local.Session getLocalSession() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mSessionFactoryLocal.createSession();
	}

	/**
	 * Creates a local Session object using a local SessionFactory created
	 * during the initialization. This method is used when creation of the
	 * Session object requires authentication. The username and password
	 * parameters are used during the authentication process. This method is
	 * used by TDI components in order to gain access to the Local Server API.
	 * 
	 * @param aUserName
	 *            the username to use in the authentication process.
	 * @param aPassword
	 *            the password to use in the authentication process.
	 * 
	 * @return an instance object representing the local session with the TDI
	 *         Server.
	 * @throws DIException
	 *             if the API Engine is not initialized or an error occurs while
	 *             obtaining the session.
	 */
	public static com.ibm.di.api.local.Session getLocalSession(String aUserName, String aPassword) throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mSessionFactoryLocal.createSession(aUserName, aPassword);
	}

	/**
	 * Initializes the Server API for remote access. It initializes the RMI
	 * Registry and registers the Remote Session Factory that is used to access
	 * the Server API.
	 * 
	 * @throws DIException
	 *             if the {@link #initialize()} was not called yet or the call
	 *             did not succeed.<br>
	 *             Or other initialization/authentication error occurs.
	 * @throws RemoteException
	 *             if an error occurs.
	 */
	public static void initializeRemote() throws DIException, RemoteException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		try {
			mSessionFactoryRemote = com.ibm.di.api.remote.impl.SessionFactoryImpl.createInstance();
		} catch (RemoteException e) {
			throw new DIException(sResHash.getString("SEVER.API.COULD.NOT.CREATE.SESSIONFACTORY", e.toString()));
		}

		initRMIRegistry();

/*
 * In recent versions of Java the System.getSecurityManager() API has been
 * depreciated.  It should be safe enough to remove the SDI security manager
 * and use the default security manager.

		boolean removeSM = false;
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new APIRemoteSecurityManager());
			removeSM = true;
		}
 */

		try {
			mRegistry.bind(REMOTE_SESSION_FACTORY_NAME, mSessionFactoryRemote);
			logInfo(sResHash.getString("SEVER.API.REMOTE.SESSION.FACTORY.OBJECT.BOUND.TO.NAME"));
		} catch (Exception e) {
			logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.BIND.REMOTE.SESSION.FACTORY"), e);
		}

/*
		if (removeSM) {
			// For now we will leave the all permissive security manager.
			// This will permit TADDM's API to work properly without editing the
			// java.policy file.
			System.setSecurityManager(null);
			Policy.setPolicy(null);
		}
 */

		boolean isRemoteBindAddressSet = (remoteBindAddr.getBindAddress() != null) ? true : false;
		String initializedMsg = "";
		String consoleMessage = "";
		if (isSSLon()) {
			if (isSSLClientAuthenticationOn()) {
				initializedMsg = sResHash.getString("SEVER.API.REMOTE.API.ENGINE.SUCCESSFULLY.INITIALIZED.SSL.ON.CLIENT.AUTH.ON");
				if (isRemoteBindAddressSet) {
					consoleMessage = sResHash.getString(
							"SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.ADDRESS.SSL.ON.CLIENT.AUTH.ON", new Object[] {
									String.valueOf(mPort), remoteBindAddr.getBindAddress().getHostAddress() });
				} else {
					consoleMessage = sResHash.getString("SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.SSL.ON.CLIENT.AUTH.ON",
							new Object[] { String.valueOf(mPort) });
				}
			} else {
				initializedMsg = sResHash.getString("SEVER.API.REMOTE.API.ENGINE.SUCCESSFULLY.INITIALIZED.SSL.ON.CLIENT.AUTH.OFF");
				if (isRemoteBindAddressSet) {
					consoleMessage = sResHash.getString(
							"SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.ADDRESS.SSL.ON.CLIENT.AUTH.OFF", new Object[] {
									String.valueOf(mPort), remoteBindAddr.getBindAddress().getHostAddress() });
				} else {
					consoleMessage = sResHash.getString("SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.SSL.ON.CLIENT.AUTH.OFF",
							new Object[] { String.valueOf(mPort) });
				}
			}
		} else {
			initializedMsg = sResHash.getString("SEVER.API.REMOTE.API.ENGINE.SUCCESSFULLY.INITIALIZED.SSL.OFF.CLIENT.AUTH.OFF");
			if (isRemoteBindAddressSet) {
				consoleMessage = sResHash.getString(
						"SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.ADDRESS.SSL.OFF.CLIENT.AUTH.OFF", new Object[] {
								String.valueOf(mPort), remoteBindAddr.getBindAddress().getHostAddress() });
			} else {
				consoleMessage = sResHash.getString("SEVER.API.REMOTE.API.SUCCESSFULLY.STARTED.ON.PORT.SSL.OFF.CLIENT.AUTH.OFF",
						new Object[] { String.valueOf(mPort) });
			}
		}
		logInfo(initializedMsg);
		System.out.println(consoleMessage);
	}

	/**
	 * Initializes the TP Server for remote access. This initializes the OSGi
	 * Framework and the TP Service application that is used to provide a REST
	 * access to the TDI Connector components.
	 * 
	 * @throws DIException
	 *             if the {@link #initialize()} was not called yet or the call
	 *             did not succeed.<br>
	 *             Or other initialization/authentication error occurs.
	 * @since 7.1
	 */
	public static void initializeTPServer() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		OSGiContainerHandle osgi = OSGiContainerHandle.getHandle(true);
		try {
			osgi.startBundle("org.eclipse.equinox.http.jetty");
			osgi.startBundle("com.ibm.di.tp.server");
		} catch (Throwable t) {
			throw new DIException(t);
		}
	}

	/**
	 * Initializes the TP Server for remote access. This initializes the OSGi
	 * Framework and the TP Service application that is used to provide a REST
	 * access to the TDI Connector components.
	 * 
	 * @throws DIException
	 *             if the {@link #initialize()} was not called yet or the call
	 *             did not succeed.<br>
	 *             Or other initialization/authentication error occurs.
	 * @since 7.2
	 */
	public static void initializeRestServer() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		OSGiContainerHandle osgi = OSGiContainerHandle.getHandle(true);
		try {
			osgi.startBundle("org.eclipse.equinox.http.jetty");
			osgi.startBundle("com.ibm.di.api.rest");
			osgi.startBundle("com.ibm.di.api.bind");
			osgi.startBundle("com.ibm.di.config.bind");
		} catch (Throwable t) {
			throw new DIException(t);
		}
	}

	// process registry

	/**
	 * This is a notification method, that notifies the engine that the
	 * specified Config Instance has been started. The API Engine updates the
	 * Process Registry and sends a notification event. This method is for
	 * internal use by the Server API and should not be used elsewhere.
	 * 
	 * @param aConfigInstance
	 *            the started config instance object.
	 * 
	 * @throws DIException
	 *             if an error while broadcasting the notification occurs.
	 */
	public static void configInstanceStarted(RSInterface aConfigInstance) throws DIException {
		if (!mEngineInitialized) {
			return;
		}

		mProcessRegistry.configInstanceStarted(aConfigInstance);

		String configId = aConfigInstance.getName();

		DIEvent event = new DIEvent(DIEvent.EVT_CI_START, configId, null, configId);
		mEventNotifier.broadcastEvent(event);
	}

	/**
	 * This is a notification method, that notifies the engine that the
	 * specified Config Instance has been stopped. The API Engine updates the
	 * Process Registry and sends a notification event. This method is for
	 * internal use by the Server API and should not be used elsewhere.
	 * 
	 * @param aConfigInstance
	 *            the stoped config instance object.
	 * 
	 * @throws DIException
	 *             if an error while broadcasting the notification occurs.
	 */
	public static void configInstanceStopped(RSInterface aConfigInstance) throws DIException {
		if (!mEngineInitialized) {
			return;
		}

		mProcessRegistry.configInstanceStopped(aConfigInstance);

		String configId = aConfigInstance.getName();

		long started = ((RS) aConfigInstance).mmStarted;
		String guid = com.ibm.di.api.local.impl.ConfigInstanceImpl.genGUID((RS) aConfigInstance);
		boolean createTS = false;
		MetamergeConfig mc = aConfigInstance.getMetamergeConfig();

		try {
			TombstonesConfig tc = (TombstonesConfig) ((MetamergeConfigImpl) mc).lookupInFolder(
					MetamergeConfig.DEFAULT_SERVER_FOLDER, MetamergeConfig.DEFAULT_SERVER_TOMBSTONES);
			createTS = Boolean.valueOf((String) tc.getParameter(TombstoneManagerListener.CI_TS_PARAMETER)).booleanValue();
		} catch (Exception e) {
			logError(sResHash.getString("SEVER.API.UNABLE.TO.GET.TOMBSTONES.CONFIG", e.toString()));
		}

		DIEvent event = new CIEvent(DIEvent.EVT_CI_STOP, configId, null, configId, started, guid, createTS);
		mEventNotifier.broadcastEvent(event);
	}

	/**
	 * This is a notification method, that notifies the engine that the
	 * specified Assembly Line has been started. The API Engine updates the
	 * Process Registry and sends a notification event. This method is for
	 * internal use by the Server API and should not be used elsewhere.
	 * 
	 * @param aAssemblyLine
	 *            the started AssemblyLine instance.
	 */
	public static void assemblyLineStarted(AssemblyLine aAssemblyLine) throws DIException {
		if (!mEngineInitialized) {
			return;
		}

		try {
			mProcessRegistry.assemblyLineStarted(aAssemblyLine);

			String configId = getConfigId(aAssemblyLine.getParent());

			DIEvent event = new ALEvent(DIEvent.EVT_AL_START, aAssemblyLine.getName(), Integer.valueOf(aAssemblyLine.hashCode()),
					configId, aAssemblyLine.getStats());
			mEventNotifier.broadcastEvent(event);
		} catch (DIException ex) {
			logError(ex.toString(), ex);
		}
	}

	/**
	 * This is a notification method, that notifies the engine that the
	 * specified Assembly Line has been terminated. The API Engine updates the
	 * Process Registry and sends a notification event. This method is for
	 * internal use by the Server API and should not be used elsewhere.
	 * 
	 * @param aAssemblyLine
	 *            the stopped AssemblyLine instance.
	 */
	public static void assemblyLineTerminated(AssemblyLine aAssemblyLine) {
		if (!mEngineInitialized) {
			return;
		}

		try {
			mProcessRegistry.assemblyLineTerminated(aAssemblyLine);

			String configId = getConfigId(aAssemblyLine.getParent());

			com.ibm.di.server.TaskStatistics ts = aAssemblyLine.getStats();
			if (ts != null) {
				Exception err = ts.getError();
				if (err != null && err instanceof com.ibm.jscript.JavaScriptException) {
					ts.ex = new Exception(err.getMessage());
				}
			}
			String guid = com.ibm.di.api.local.impl.AssemblyLineImpl.genGUID(aAssemblyLine);
			String userMessage = aAssemblyLine.getTombstoneUserMessage();
			DIEvent event = new ALEvent(DIEvent.EVT_AL_STOP, aAssemblyLine.getName(), Integer.valueOf(aAssemblyLine.hashCode()),
					configId, ts, guid, userMessage);
			mEventNotifier.broadcastEvent(event);
		} catch (DIException ex) {
			logError(ex.toString(), ex);
		}
	}

	/**
	 * This is a notification method, that notifies the engine that server has
	 * been stopped. The only parameter specifies the time the server has been
	 * started. The API Engine sends a notification event. This method is for
	 * internal use by the Server API and should not be used elsewhere.
	 * 
	 * @param aServerStarted
	 *            the time the server was stopped.
	 * @throws DIException
	 *             if an error while broadcasting the notification occurs.
	 */
	public static void serverStopped(long aServerStarted) throws DIException {
		if (!mEngineInitialized) {
			return;
		}

		DIEvent event = new DIEvent(DIEvent.EVT_SRV_STOP, "Server", new Date(aServerStarted));
		mEventNotifier.broadcastEvent(event);
	}

	// accessor calls

	/**
	 * Returns a vector containing all configuration instances currently
	 * started.
	 * 
	 * @return the list with the running config instance objects.
	 * @throws DIException
	 *             if the API Engine is not initialized correctly or error
	 *             occurs while retrieving the list.
	 */
	public static Vector<RSInterface> getConfigInstances() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mProcessRegistry.getConfigInstances();
	}

	/**
	 * @return a list containing the IDs of all the configuration instances
	 *         currently started.
	 * 
	 * @throws DIException
	 *             if the API Engine is not initialized correctly or error
	 *             occurs while retrieving the list.
	 */
	public static List<String> getConfigInstanceIDs() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}
		return mProcessRegistry.getConfigInstanceIDs();
	}

	/**
	 * Retrieves config instance.
	 * 
	 * @param configId
	 *            configuration instance id
	 * @return configuration instance with the specified config id, or null if
	 *         there is no such instance
	 * @throws DIException
	 *             if the Server API is not initialized
	 * @since 7.0
	 */
	public static RSInterface getConfigInstance(String configId) throws DIException {

		Vector<RSInterface> rawConfigInstances = APIEngine.getConfigInstances();
		if (rawConfigInstances == null || configId == null) {
			return null;
		}

		RSInterface configInstance = null;
		for (int i = 0; i < rawConfigInstances.size(); i++) {
			RSInterface ci = (RSInterface) rawConfigInstances.get(i);
			String ciId = APIEngine.getConfigId(ci);
			if (configId.equals(ciId)) {
				configInstance = ci;
				break;
			}
		}

		return configInstance;
	}

	/**
	 * Returns a hashtable whose key elements are the Configuration Instances
	 * currently started, and the values are vectors containing all Assembly
	 * Lines currently started in the corresponding Configuration Instance.
	 * 
	 * @return the map between config instances and their AssemblyLines.
	 * 
	 * @throws DIException
	 *             if the Server API is not initialized, or an error occurs.
	 * 
	 */
	public static Hashtable<RSInterface, Vector<AssemblyLine>> getAssemblyLines() throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mProcessRegistry.getAssemblyLines();
	}

	/**
	 * Returns the Identity object from the Security Registry corresponding to
	 * the user id passes as parameter.
	 * 
	 * @param aUserId
	 *            the user identifier used when obtaining the Identity
	 * 
	 * @return the {@link Identity} object
	 * @throws DIException
	 *             if the API Engine is not initialized properly.
	 */
	public static Identity getIdentity(String aUserId) throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mSecurityRegistry.getIdentity(aUserId);
	}

	// Notifications

	/**
	 * Adds a new event listener.
	 * 
	 * @param aListener
	 *            this is the concrete object that implements the
	 *            {@link DIEventListener} interface.
	 * @param aTypeFilter
	 *            a composite filter that matches event type
	 * @param aIdFilter
	 *            an atomic filter that matches event id
	 * 
	 * @throws DIException
	 *             if the API Engine is not initialized properly, or other error
	 *             occurs.
	 * 
	 */
	public static void addEventListener(DIEventListener aListener, String aTypeFilter, String aIdFilter) throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		mEventNotifier.addEventListener(aListener, aTypeFilter, aIdFilter);
	}

	/**
	 * Removes existing event listener.
	 * 
	 * @param aListener
	 *            the listener to remove.
	 * 
	 * @return whether the listener was unregistered successfully
	 * @throws DIException
	 *             if the API Engine is not initialized properly, or other error
	 *             occurs.
	 */
	public static boolean removeEventListener(DIEventListener aListener) throws DIException {
		if (!mEngineInitialized) {
			throw new DIException(sResHash.getString("SEVER.API.SERVER.API.NOT.INITIALIZED"));
		}

		return mEventNotifier.removeEventListener(aListener);
	}

	// Logging methods

	/**
	 * Returns "true" if debug is enabled and "false" otherwise.
	 * 
	 * @return true if the debug is enabled, false otherwise.
	 */
	public static boolean isDebugEnabled() {
		return mLogger.isDebugEnabled();
	}

	/**
	 * Logs a message using the DEBUG log level.
	 * 
	 * @param aMessage
	 *            the message to log.
	 */
	public static void logDebug(String aMessage) {
		mLogger.debug(aMessage);
	}

	/**
	 * Logs a message using the INFO log level.
	 * 
	 * @param aMessage
	 *            the message to log.
	 */
	public static void logInfo(String aMessage) {
		mLogger.info(aMessage);
	}

	/**
	 * Logs a message using the ERROR log level.
	 * 
	 * @param aMessage
	 *            the message to log.
	 */
	public static void logError(String aMessage) {
		mLogger.error(aMessage);
	}

	/**
	 * Logs a message using the ERROR log level.
	 * 
	 * @param message
	 *            the message to log.
	 * 
	 * @param t
	 *            Error object.
	 */
	public static void logError(String message, Throwable t) {
		mLogger.error(message, t);
	}

	/**
	 * Logs a message using the WARN log level.
	 * 
	 * @param aMessage
	 *            the message to log.
	 */
	public static void logWarn(String aMessage) {
		mLogger.warn(aMessage);
	}

	/**
	 * Logs a message using the FATAL log level.
	 * 
	 * @param aMessage
	 *            the message to log.
	 */
	public static void logFatal(String aMessage) {
		mLogger.fatal(aMessage);
	}

	/**
	 * Logs a message using the ERROR log level and then throws a DIException
	 * using the same message.
	 * 
	 * @param aErrorMsg
	 *            the error message to output wit the exception.
	 * @throws DIException
	 *             with the specified error message.
	 */
	public static void logErrorAndThrowException(String aErrorMsg) throws DIException {
		logError(aErrorMsg);
		throw new DIException(aErrorMsg);
	}

	/**
	 * Logs a message composed from the message passes as parameter and the
	 * message in the {@link Throwable} object. The new message is logged using
	 * the ERROR log level and then a DIException is thrown containing that
	 * message.
	 * 
	 * @param aErrorMsg
	 *            the error message
	 * @param e
	 *            the Throwable object
	 * 
	 * @throws DIException
	 *             with the specified error message.
	 */
	public static void logErrorAndThrowException(String aErrorMsg, Throwable e) throws DIException {
		logError(aErrorMsg, e);
		throw new DIException(aErrorMsg + ": " + e.getMessage(), e);
	}

	/**
	 * Retrieves port number.
	 * 
	 * @return the port number used for the connection to the RMI Registry.
	 */
	public static int getNamingPort() {
		return mPort;
	}

	/**
	 * Checks if SSL is on.
	 * 
	 * @return <code>true</code> if SSL has been turned on. Otherwise returns
	 *         <code>false</code>.
	 */
	public static boolean isSSLon() {
		return mSSLon;
	}

	/**
	 * Checks if SSL client authentication is on.
	 * 
	 * @return <code>true</code> if SSL Client Authentication is on. Otherwise
	 *         returns <code>false</code>.
	 */
	public static boolean isSSLClientAuthenticationOn() {
		return mSSLClientAuthOn;
	}

	/**
	 * Retrieves server socket factory.
	 * 
	 * @return the server socket factory used for establishing remote connection
	 *         with the TDI Server over RMI. This will return null if the remote
	 *         api is turned off.
	 */
	public static RMIServerSocketFactory getServerSF() {
		return mServerSF;
	}

	/**
	 * Retrieves the client socket factory used for establishing remote
	 * connection.
	 * 
	 * @return the client socket factory used for establishing remote connection
	 *         with the TDI Server over RMI. This will return null if the remote
	 *         api is turned off.
	 */
	public static RMIClientSocketFactory getClientSF() {
		return mClientSF;
	}

	/**
	 * Creates and initializes the RMI Registry.
	 * 
	 * @return the RMI Registry in use.
	 * @throws DIException
	 *             if an error occurs.
	 * @throws RemoteException
	 *             if an error occurs.
	 */
	public static Registry initRMIRegistry() throws DIException, RemoteException {
		if (mRegistry != null) {
			return mRegistry;
		}
		/*
		 * The following lines check if there is already a remote registry on
		 * the localhost at the port used by TDI (1099 by default). If it's the
		 * case TDI will use this registry, if not TDI will create a new
		 * registry.
		 */
		try {
			mRegistry = LocateRegistry.getRegistry(mPort);
			mRegistry.list();
		} catch (RemoteException remoteEx) {
			if (isDebugEnabled()) {
				logDebug("LocateRegistry.getRegistry: " + remoteEx);
			}
			try {
				mRegistry = LocateRegistry.createRegistry(mPort);
				logInfo(sResHash.getString("SEVER.API.RMI.REGISTRY.STARTED.ON.PORT", String.valueOf(mPort)));
			} catch (Exception e) {
				logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.CREATE.RMI.REGISTRY"), e);
			}
		}

		logInfo(sResHash.getString("SEVER.API.RMI.REGISTRY.CONNECT.TO", mRegistry.toString()));
		return mRegistry;
	}

	/**
	 * Returns the {@link TombstoneManager}.
	 * 
	 * @return the {@link TombstoneManager} object.
	 */
	public static TombstoneManager getTombstoneManager() {
		return mTombstoneManager;
	}

	/**
	 * Retrieves config instance repository.
	 * 
	 * @return the {@link ConfigurationRegistry} object.
	 */
	public static ConfigurationRegistry getConfigurationRegistry() {
		return mConfigRegistry;
	}

	/**
	 * Retrieves the {@link APIAuthenticator} object.
	 * 
	 * @return the {@link APIAuthenticator} object.
	 */
	public static APIAuthenticator getAuthenticator() {
		return mAuthenticator;
	}

	/**
	 * Sends a custom notification event using the API Engine's Event Notifier.
	 * 
	 * @param aType
	 *            the type of the event that had occurred. <br>
	 *            Predefined constants: <br> {@link DIEvent#EVT_CI_START}<br>
	 *            {@link DIEvent#EVT_CI_STOP}<br> {@link DIEvent#EVT_CI_UPDATED}<br>
	 *            {@link DIEvent#EVT_AL_START}<br> {@link DIEvent#EVT_AL_STOP}<br>
	 *            {@link DIEvent#EVT_SRV_STOP}
	 * @param aId
	 *            the ID of the event <b>Note: </b>This ID should not to be
	 *            think of as a Unique Identifier used for distinguishing
	 *            different events occurring in the system.
	 * @param aData
	 *            the additional information this event carrier might contain.
	 * @throws DIException
	 */
	public static void sendCustomNotification(String aType, String aId, Object aData) throws DIException {
		String type = aType;
		if (type == null)
			type = DIEvent.EVT_USER_PREFIX;
		else if(!type.startsWith(DIEvent.EVT_USER_PREFIX))
			type = DIEvent.EVT_USER_PREFIX + type;

		DIEvent event = new DIEvent(type, aId, aData);
		mEventNotifier.broadcastEvent(event);
	}

	/**
	 * Initializes the LDAP Authenticator.
	 * 
	 * @throws DIException
	 *             if an error occurs.
	 */
	private static void initLDAPAuth() throws DIException {
		mLDAPAuthenticator = new LDAPAuthentication();

		String ldapHostName = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_HOSTNAME);
		String ldapPort = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_PORT);
		String ldapUseSSL = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_USE_SSL);
		String ldapSearchBase = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_SEARCHBASE);
		String ldapAdminDN = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_ADMIN_DN);
		String ldapAdminPassword = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_ADMIN_PASSWORD);
		String ldapUserAttrib = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_USER_ATTRIBUTE);
		String ldapGroupSupport = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_GROUP_SUPPORT);
		String ldapMembershipAttrib = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE);
		String ldapMembershipAttribContent = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE_CONTENT);
		String ldapGroupSerchBase = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_GROUP_SEARCHBASE);
		String ldapGroupNameAttrib = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_GROUPNAME_ATTRIBUTE);
		String ldapBinaryAttribs = System.getProperty(PROP_API_CUSTOM_AUTH_LDAP_BINARY_ATTRIBUTES);

		HashMap<String, String> configMap = new HashMap<String, String>();
		configMap.put(LDAPAuthentication.PARAM_LDAP_HOST_NAME, ldapHostName);
		configMap.put(LDAPAuthentication.PARAM_LDAP_PORT, ldapPort);
		configMap.put(LDAPAuthentication.PARAM_LDAP_SSL_ENABLED, ldapUseSSL);
		configMap.put(LDAPAuthentication.PARAM_LDAP_SEARCH_BASE, ldapSearchBase);
		configMap.put(LDAPAuthentication.PARAM_LDAP_ADMIN_DN, ldapAdminDN);
		configMap.put(LDAPAuthentication.PARAM_LDAP_ADMIN_PWD, ldapAdminPassword);
		configMap.put(LDAPAuthentication.PARAM_LDAP_USERID_ATTRIBUTE, ldapUserAttrib);
		configMap.put(LDAPAuthentication.PARAM_LDAP_GROUP_SUPPORT, ldapGroupSupport);
		configMap.put(LDAPAuthentication.PARAM_LDAP_MEMBERSHIP_ATTRIBUTE, ldapMembershipAttrib);
		configMap.put(LDAPAuthentication.PARAM_LDAP_MEMBERSHIP_ATTRIBUTE_CONTENT, ldapMembershipAttribContent);
		configMap.put(LDAPAuthentication.PARAM_LDAP_GROUP_SEARCHBASE, ldapGroupSerchBase);
		configMap.put(LDAPAuthentication.PARAM_LDAP_GROUPNAME_ATTRIBUTE, ldapGroupNameAttrib);
		configMap.put(LDAPAuthentication.PARAM_LDAP_BINARY_ATTRIBUTES, ldapBinaryAttribs);

		try {
			mLDAPAuthenticator.initialize(configMap);
		} catch (Exception e) {
			if (Boolean.getBoolean(PROP_API_CUSTOM_AUTH_LDAP_CRITICAL)) {
				logErrorAndThrowException(sResHash.getString("SEVER.API.LDAP.AUTHENTICATION.INITIALIZATION.ERROR.1"), e);
			} else {
				logError(sResHash.getString("SEVER.API.LDAP.AUTHENTICATION.INITIALIZATION.ERROR.2", e.toString()));
			}
		}
	}

	/**
	 * Retrieves LDAP authenticator.
	 * 
	 * @return the LDAP Authenticator, might be <code>null</code>.
	 */
	public static LDAPAuthentication getLDAPAuthenticator() {
		return mLDAPAuthenticator;
	}

	/**
	 * Checks if LDAP authentication is enabled.
	 * 
	 * @return <code>true</code> if LDAP Authentication is enabled,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isLDAPAuthenticationEnabled() {
		if (mLDAPAuthenticator != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If method invocation is enabled, then this method returns true.
	 * 
	 * @return true if method invocation is enabled and false otherwise.
	 */
	public static boolean getMethodInvokeEnabled() {
		return mMethodInvoke;
	}

	/**
	 * This methods returns a list of classes. These classes are the only
	 * classes which can be invoked through the
	 * {@link Session#invokeCustom(String, String, Object[])} and the
	 * {@link Session#invokeCustom(String, String, Object[], String[])} methods
	 * of the Server API.
	 * 
	 * @return list of classes which can be invoked through invokeCustom()
	 *         methods of the Server API.
	 */
	public static String getInvokeClassesAllowed() {
		if (mMethodInvokeClasses != null) {
			return mMethodInvokeClasses;
		}
		return null;
	}

	/**
	 * Obtain the configuration instance id for a running configuration
	 * instance.
	 * 
	 * @param configInstance
	 *            A running configuration instance.
	 * @return the configuration id, which corresponds to the specified
	 *         configuration instance
	 * 
	 * @since TDI 6.1.1
	 */
	public static String getConfigId(RSInterface configInstance) {
		return configInstance.getName();
	}

	/**
	 * Initialize the JAAS Authentication module.
	 * 
	 * @since 7.0
	 */
	private static void initJAASAuth() {
		mJAASAuthenticator = new JAASAuthentication();
	}

	/**
	 * Getter for the member variable mJAASAuthenticator
	 * 
	 * @return JAASAuthentication object
	 */
	public static JAASAuthentication getJAASAuthenticator() {
		return mJAASAuthenticator;
	}

	/**
	 * Checks if the member variable mJAASAuthenticator is initialized
	 * 
	 * @return true if JAAS Authentication is enabled
	 */
	public static boolean isJAASAuthenticationEnabled() {
		if (mJAASAuthenticator != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sends a notification event using the API Engine's Event Notifier.
	 * Initially introduced to send audit Notifications.
	 * 
	 * @param type
	 *            Type of the notification event.
	 * @param id
	 *            ID of the notification event.
	 * @param data
	 *            An Java object containing additional useful information. By
	 *            audit notification this object is of type com.ibm.di.Entry
	 *            containing as Attributes specific audit information.
	 * @param configInstanceId
	 *            The ConfigInstance ID, which the notification is bound to.
	 * @exception DIException
	 *                If an error occurs while the event is transmitted.
	 * @since 7.0
	 */
	public static void sendNotification(String type, String id, Object data, String configInstanceId) throws DIException {
		mEventNotifier.broadcastEvent(new DIEvent(type, id, data, configInstanceId));
	}

	/**
	 * Sends a notification event using the API Engine's Event Notifier.
	 * Initially introduced to send audit Notifications.
	 * 
	 * @param event
	 *            The event to publish
	 * @exception DIException
	 *                If an error occurs while the event is transmitted.
	 * @since 7.1.1
	 */
	public static void sendNotification(DIEvent event) throws DIException {
		mEventNotifier.broadcastEvent(event);
	}

	/**
	 * The policy of the Server API for assigning ids to configuration
	 * instances. If the instance has an explicit run name(<code>RS.CL_INTERNAL_CONFIG_NSTANCE_NAME</code>),
	 * then that name will be used as the configuration instance id. Otherwise,
	 * if a configuration file has a solution name, then that name will be used
	 * as the id of the configuration instance (note that run names have
	 * precedence over solution names). If the instance has no run name and its
	 * configuration file has no solution name, the generation of the instance
	 * name will be based upon the absolute name of the configuration file. Note
	 * that the Server API policy always ensures that the configuration ids do
	 * not contain any of the following symbols: '\' '/' ':' '*' '?' '"' '<'
	 * '>' '|' by replacing them with underscores. (This is done to avoid
	 * problems, when storing configuration-instance-specific information like
	 * System Logs on the file system).
	 * 
	 * @since 7.0
	 */
	private static class APIConfigInstanceNamingPolicy implements RS.ConfigInstanceNamingPolicy {

		/**
		 * The default policy, which will be decorated.
		 */
		private RS.ConfigInstanceNamingPolicy defaultPolicy = new RS.DefaultConfigInstanceNamingPolicy();

		/**
		 * The registry of configuration files of the Server API.
		 */
		private ConfigurationRegistry configRegistry;

		/**
		 * @param configRegistry
		 *            The registry of configuration files of the Server API. It
		 *            is used to resolve solution names.
		 */
		public APIConfigInstanceNamingPolicy(ConfigurationRegistry configRegistry) {

			this.configRegistry = configRegistry;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getConfigInstanceName(Map<String, Object> params) throws Exception {

			String instanceName = (String) params.get(RS.CL_INTERNAL_CONFIG_NSTANCE_NAME);

			if (instanceName != null) {

				/*
				 * The instance has been started with an explicit name - use
				 * that name to generate the config id
				 */

				instanceName = LogUtils.getCleanConfigId(instanceName);

			} else {

				// Assume that the default name is the file system path of the
				// configuration file
				String configPath = defaultPolicy.getConfigInstanceName(params);

				// a temporary config instance - no file associated
				String xmlConfig = (String) params.get(RS.CL_INTERNAL_CONFIG_AS_STRING);

				/*
				 * If a Solution Name is configured, use it as the config
				 * instance id.
				 */
				String solutionName = null;
				if (configPath != null) {
					solutionName = configRegistry.getSolutionName(new java.io.File(configPath));
				} else if (xmlConfig != null) {
					solutionName = ConfigurationRegistry.getSolutionNameFromMemory(xmlConfig);
				}

				if (solutionName != null && solutionName.length() > 0) {

					String cleanSolutionName = LogUtils.getCleanConfigId(solutionName);
					if (!solutionName.equals(cleanSolutionName)) {

						/*
						 * The Solution Name is not a valid file name, so it has
						 * been transformed. Warn the users about the
						 * transformation.
						 */
						APIEngine.logWarn(sResHash.getString("SEVER.API.TRANSFORMED.SOLUTION.NAME.TO.BE.CONFIG.ID", new Object[] {
								configPath, solutionName, cleanSolutionName }));
					}

					instanceName = cleanSolutionName;
				}

				/*
				 * No Solution Name was found, so calculate config instance id
				 * from the config file name by replacing slashes and colons
				 * with underscores.
				 */
				if (instanceName == null && configPath != null) {
					instanceName = LogUtils.getCleanConfigId(configPath);
				}

				if (instanceName == null && xmlConfig != null) {
					// auto-generate a config id for temp instance
					instanceName = TEMP_CONFIG_ID_PREFIX + System.currentTimeMillis();
				}
			}

			return instanceName;
		}
	}
}
