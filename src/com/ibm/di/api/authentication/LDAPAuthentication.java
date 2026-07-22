/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.authentication;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.ibm.di.api.APIEngine;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class provides the capability of authentication against a LDAP server.
 */
public class LDAPAuthentication implements AuthenticationInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This constant holds the name of the class used as initial context.
	 */
	public static final String INITCTX = "com.sun.jndi.ldap.LdapCtxFactory";

	/**
	 * The name of the parameter used to specify the LDAP host name.
	 */
	public static final String PARAM_LDAP_HOST_NAME = "LDAPHostName";

	/**
	 * The name of the parameter used to specify the LDAP port.
	 */
	public static final String PARAM_LDAP_PORT = "LDAPPort";

	/**
	 * The name of the parameter used to specify the LDAP administrator's
	 * distinguished name.
	 */
	public static final String PARAM_LDAP_ADMIN_DN = "LDAPAdminDN";

	/**
	 * The name of the parameter used to specify the LDAP administrator's
	 * password.
	 */
	public static final String PARAM_LDAP_ADMIN_PWD = "LDAPAdminPassword";

	/**
	 * The name of the parameter used to specify the LDAP search base.
	 */
	public static final String PARAM_LDAP_SEARCH_BASE = "LDAPSearchBase";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_USERID_ATTRIBUTE = "LDAPUserIDAttribute";

	/**
	 * The name of the parameter used to specify that SSL is enabled or not.
	 */
	public static final String PARAM_LDAP_SSL_ENABLED = "LDAPSSLEnabled";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_GROUP_SUPPORT = "LDAPGroupSupport";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_MEMBERSHIP_ATTRIBUTE = "LDAPMemebershipAttribute";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_MEMBERSHIP_ATTRIBUTE_CONTENT = "LDAPMemebershipAttributeContent";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_GROUPNAME_ATTRIBUTE = "LDAPGroupNameAttribute";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_GROUP_SEARCHBASE = "LDAPGroupSearchBase";

	/**
	 * The name of a configuration parameter.
	 */
	public static final String PARAM_LDAP_BINARY_ATTRIBUTES = "LDAPBinaryAttrubutes";

	/**
	 * The name of the parameter used to specify the user name to authenticate.
	 */
	public static final String AUTH_MAP_LDAP_USERNAME = "LDAP_USERNAME";

	/**
	 * The name of the parameter used to specify the LDAP password used for the
	 * authentication.
	 */
	public static final String AUTH_MAP_LDAP_PASSWORD = "LDAP_PASSWORD";

	/**
	 * Semicolon delimiter.
	 */
	private static final String DELIMITER = ";";

	/**
	 * LDAP URL attribute.
	 */
	private String mLdapURL = null;

	/**
	 * Host name attribute.
	 */
	private String mHostName = null;

	/**
	 * LDAP port attribute.
	 */
	private String mLdapPort = null;

	/**
	 * Admin DN attribute.
	 */
	private String mAdminDN = null;

	/**
	 * Admin password attribute.
	 */
	private String mAdminPWD = null;

	/**
	 * LDAP search base attribute.
	 */
	private String mLdapSearchBase = null;

	/**
	 * User Id attribute.
	 */
	private String mLdapUserIDAttrib = null;

	/**
	 * SSL flag.
	 */
	private boolean mUseSSL = false;

	/**
	 * LDAP search context attribute.
	 */
	private InitialLdapContext mSearchCtx = null;

	/**
	 * Group support flag.
	 */
	private boolean ldapGroupSupport = false;

	/**
	 * LDAP membership attribute.
	 */
	private String ldapMembershipAttrib = null;

	/**
	 * LDAP membership attribute content.
	 */
	private String ldapMembershipAttribContent = null;

	/**
	 * LDAP search base for group attribute.
	 */
	private String ldapGroupSearchBase = null;

	/**
	 * LDAP group name attribute.
	 */
	private String ldapGroupNameAttrib = null;

	/**
	 * LDAP binary attributes.
	 */
	private String ldapBinaryAttribs = null;

	/**
	 * User groups attribute.
	 */
	private String userGroups = null;

	/**
	 * Logs error messages.
	 */
	private Logger mLogger = LogManager.getLogger("com.ibm.di.api.authentication");

	/**
	 * Initialized flag.
	 */
	private boolean mIsInitialized = false;

	/**
	 * Binary values flag.
	 */
	private boolean areValuesBinary = false;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Initializes the parameters used for the connection with the LDAP server
	 * using the provided as parameter configuration map.
	 * 
	 * @param configMap
	 *            the configuration held in a map.
	 * @throws Exception
	 *             if the initialization fails.
	 */
	public void initialize(Map<String, String> configMap) throws Exception {

		mHostName = getProperty(configMap, PARAM_LDAP_HOST_NAME);
		mLdapPort = getProperty(configMap, PARAM_LDAP_PORT);
		mAdminDN = getProperty(configMap, PARAM_LDAP_ADMIN_DN);
		mAdminPWD = getProperty(configMap, PARAM_LDAP_ADMIN_PWD);
		mLdapSearchBase = getProperty(configMap, PARAM_LDAP_SEARCH_BASE);
		mLdapUserIDAttrib = getProperty(configMap, PARAM_LDAP_USERID_ATTRIBUTE);
		mUseSSL = Boolean.valueOf(
				getProperty(configMap, PARAM_LDAP_SSL_ENABLED)).booleanValue();
		ldapGroupSupport = Boolean.valueOf(
				getProperty(configMap, PARAM_LDAP_GROUP_SUPPORT))
				.booleanValue();
		if (ldapGroupSupport) {
			ldapMembershipAttrib = getProperty(configMap,
					PARAM_LDAP_MEMBERSHIP_ATTRIBUTE);
			ldapMembershipAttribContent = getProperty(configMap,
					PARAM_LDAP_MEMBERSHIP_ATTRIBUTE_CONTENT);
			ldapGroupSearchBase = getProperty(configMap,
					PARAM_LDAP_GROUP_SEARCHBASE);
			ldapGroupNameAttrib = getProperty(configMap,
					PARAM_LDAP_GROUPNAME_ATTRIBUTE);
			ldapBinaryAttribs = getProperty(configMap,
					PARAM_LDAP_BINARY_ATTRIBUTES);
		}

		if (ldapBinaryAttribs != null) {
			StringTokenizer st = new StringTokenizer(ldapBinaryAttribs, " ");
			while (st.hasMoreTokens()) {
				String binaryAttribute = st.nextToken().trim();
				if (binaryAttribute
						.equalsIgnoreCase(ldapMembershipAttribContent)
						|| binaryAttribute
								.equalsIgnoreCase(ldapMembershipAttrib)) {
					areValuesBinary = true;
					break;
				}
			}
		}

		setupSearchContext();
	}

	/**
	 * This method authenticates the user using the entries in the provided map.
	 * 
	 * @see #AUTH_MAP_LDAP_USERNAME
	 * @see #AUTH_MAP_LDAP_PASSWORD
	 * 
	 * @param map
	 *            the map containing the credentials used for the
	 *            authentication.
	 * @throws Exception
	 *             if the authentication fails.
	 */
	public void authenticate(Map<String, String> map) throws Exception {
		authenticate(getProperty(map, AUTH_MAP_LDAP_USERNAME), getProperty(map,
				AUTH_MAP_LDAP_PASSWORD));
	}

	/**
	 * {@inheritDoc}
	 */
	public void authenticate(String aUserName, String aPassword)
			throws Exception {

		if (aUserName == null) {
			String funcmsg = sResHash.getString("SEVER.API.USERNAME.IS.NULL");
			logError(funcmsg);
			throw new Exception(funcmsg);
		}

		if (aPassword == null) {
			String funcmsg = sResHash.getString("SEVER.API.PASSWORD.IS.NULL");
			logError(funcmsg);
			throw new Exception(funcmsg);
		}

		setupSearchContext();

		String fullDN = aUserName;
		if (mLdapSearchBase != null && mLdapUserIDAttrib != null
				&& !UserFunctions.endsWithIC(aUserName, mLdapSearchBase)) {
			String ldapFilter = null;
			if (UserFunctions.startsWithIC(aUserName, mLdapUserIDAttrib)
					&& aUserName.indexOf("=") > -1) {
				ldapFilter = "(" + aUserName + ")";
			} else {
				ldapFilter = "(" + mLdapUserIDAttrib + "=" + aUserName + ")";
			}

			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString(
										"SEVER.API.PERFORMING.SEARCH.FOR.FULL.DN.FOR.WITH.FILTER",
										new Object[] { aUserName, ldapFilter }));
			}
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration<SearchResult> results = mSearchCtx.search(
					mLdapSearchBase, ldapFilter, constraints);
			if (results.hasMore()) {
				SearchResult searchResult = results.next();
				fullDN = makeFullDN(searchResult, mLdapSearchBase);
				if (results.hasMore()) {
					String funcmsg = sResHash.getString(
							"SEVER.API.MORE.THAN.ONE.SEARCH.RESULT.FOUND",
							aUserName);
					logError(funcmsg);
					throw new Exception(funcmsg);
				}
			} else {
				String funcmsg = sResHash.getString(
						"SEVER.API.FULL.DN.NOT.FOUND", aUserName);
				logError(funcmsg);
				throw new Exception(funcmsg);
			}
		}

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);
		env.put(Context.PROVIDER_URL, mLdapURL);
		env.put(Context.SECURITY_PRINCIPAL, fullDN);
		env.put(Context.SECURITY_CREDENTIALS, aPassword);
		env.put(Context.SECURITY_AUTHENTICATION, "Simple");

		if (mUseSSL) {
			env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket",
					"javax.net.ssl.SSLSocketFactory");
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash
						.getString("SEVER.API.USING.LDAP.SSL.CONNECTION.1"));
			}
		}

		try {
			InitialLdapContext authCtx = new InitialLdapContext(env, null);
			authCtx.close();
			if (ldapGroupSupport) {
				try {
					performGroupSearch(fullDN);
				} catch (NameNotFoundException e) {
					APIEngine
							.logInfo(sResHash
									.getString(
											"SEVER.API.LDAP.AUTHENTICATION.GROUP.USER.NOT.FOUND",
											fullDN));
				}
			}
		} catch (Exception e) {
			APIEngine.logError(sResHash.getString(
					"SEVER.API.AUTHENTICATION.FAILED.FOR.INITIALLDAPCONTEXT",
					new Object[] { aUserName, e.toString() }));
			throw e;
		}

		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.LDAP.AUTHENTICATION.SUCCEEDED.FOR.USERNAME",
					aUserName));
		}
	}

	/**
	 * Closes the initialized connection.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void close() throws Exception {
		if (mSearchCtx != null) {
			mSearchCtx.close();
		}

		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash
					.getString("SEVER.API.LDAP.AUTHENTICATION.MODULE.CLOSED"));
		}
	}

	/**
	 * Logs error message
	 * 
	 * @param aMessage
	 *            String
	 */
	private void logError(String aMessage) {
		if (mLogger != null) {
			mLogger.error(aMessage);
		}
	}

	/**
	 * Creates full DN.
	 * 
	 * @param sr
	 *            {@link SearchResult}
	 * @param aBaseDN
	 *            base distinguish name
	 * @return the DN
	 */
	private String makeFullDN(SearchResult sr, String aBaseDN) {

		String fullDN = null;
		String name = sr.getName();
		String base = aBaseDN;

		if (base == null || base.trim().length() == 0) {
			fullDN = name;
		} else if (name.length() == 0) {
			fullDN = base;
		} else if (name.startsWith("\"") && name.endsWith("\"")) {
			fullDN = name.substring(0, name.length() - 1) + "," + base + "\"";
		} else {
			fullDN = name + "," + base;
		}

		return fullDN;
	}

	/**
	 * Retrieves a value corresponding to the key name provided for the
	 * specified Map.
	 * 
	 * @param configMap
	 *            Map to look in.
	 * @param aParameterKey
	 *            key.
	 * @return the value corresponding to the key or <code>null</code> if
	 *         nothing found.
	 */
	private String getProperty(Map<String, String> configMap,
			String aParameterKey) {

		if (configMap == null || aParameterKey == null) {
			return null;
		}

		// Set keys = configMap.keySet();
		Iterator<Entry<String, String>> iter = configMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> mpEntry = iter.next();
			String key = mpEntry.getKey();
			if (key.equalsIgnoreCase(aParameterKey)) {
				String value = mpEntry.getValue();
				return value;
			}
		}

		return null;
	}

	/**
	 * Initializes a connection to the server and sets up a search context.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void setupSearchContext() throws Exception {
		if (mIsInitialized) {
			return;
		}
		if (mHostName == null) {
			String funcmsg = sResHash.getString(
					"SEVER.API.MISSING.PROPERTY.LDAP.HOST.NAME",
					PARAM_LDAP_HOST_NAME);
			logError(funcmsg);
			throw new Exception(funcmsg);
		}
		if (mLdapPort == null) {
			String funcmsg = sResHash.getString(
					"SEVER.API.MISSING.PROPERTY.LDAP.PORT", PARAM_LDAP_PORT);
			logError(funcmsg);
			throw new Exception(funcmsg);
		}

		if (!mHostName.startsWith("ldap://")) {
			mHostName = "ldap://" + mHostName;
		}

		mLdapURL = mHostName + ":" + mLdapPort;

		if (mLdapSearchBase == null && !ldapGroupSupport) {
			mIsInitialized = true;
			return;
		}

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, INITCTX);
		env.put(Context.PROVIDER_URL, mLdapURL);

		if (mAdminDN != null) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString("SEVER.API.USING.SIMPLE.AUTHENTICATION.FOR.SEARCH.CONTEXT"));
			}
			env.put(Context.SECURITY_PRINCIPAL, mAdminDN);
			env.put(Context.SECURITY_CREDENTIALS, mAdminPWD);
			env.put(Context.SECURITY_AUTHENTICATION, "Simple");
		} else {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash
						.getString("SEVER.API.USING.ANONYMOUS.AUTHENTICATION"));
			}
		}
		if (mUseSSL) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash
						.getString("SEVER.API.USING.LDAP.SSL.CONNECTION.2"));
			}
			env.put(Context.SECURITY_PROTOCOL, "ssl");
			env.put("java.naming.ldap.factory.socket",
					"javax.net.ssl.SSLSocketFactory");
		}
		if (areValuesBinary) {
			env.put("java.naming.ldap.attributes.binary", ldapBinaryAttribs
					.trim());
		}

		int count = 0;
		boolean serverAvailable = false;
		do {
			try {
				if (APIEngine.isDebugEnabled()) {
					APIEngine
							.logDebug(sResHash
									.getString("SEVER.API.TRYING.TO.CREATE.SEARCH.CONTEXT"));
				}
				mSearchCtx = new InitialLdapContext(env, null);
				if (APIEngine.isDebugEnabled()) {
					APIEngine.logDebug(sResHash
							.getString("SEVER.API.SEARCH.CONTEXT.CREATED"));
				}
				serverAvailable = true;
				break;
			} catch (Exception e) {
				APIEngine.logError(sResHash.getString(
						"SEVER.API.ERROR.WHILE.CREATING.SEARCH.CONTEXT", e
								.toString()));
				Thread.sleep(5000);
			}
			count++;
		} while (count < 3);

		if (!serverAvailable) {
			String funcmsg = sResHash
					.getString("SEVER.API.UNABLE.TO.CREATE.SEARCH.CONTEXT");
			logError(funcmsg);
			throw new Exception(funcmsg);
		}
		mIsInitialized = true;
	}

	/**
	 * The method searches all LDAP groups, which the user is member of and
	 * stores them in the userGroups object.
	 * 
	 * @param fullDN
	 *            String object containing the found DN of the authenticating
	 *            LDAP user
	 * @throws Exception
	 *             if an error occurs.
	 * @since 7.0
	 */
	private void performGroupSearch(String fullDN) throws Exception {
		String nullValue = findNullValue();
		if (nullValue != null) {
			String funcmsg = sResHash.getString(
					"SEVER.API.LDAP.AUTHENTICATION.GROUP.ATTRIBUTE.NULL",
					nullValue);
			logError(funcmsg);
			throw new Exception(funcmsg);
		}
		StringBuffer foundGroups = new StringBuffer();
		NamingEnumeration<?> attributeValues = null;
		String[] membershipAttribute = new String[] { ldapMembershipAttrib };
		Attributes atr = mSearchCtx.getAttributes(fullDN, membershipAttribute);
		Attribute groupsAttribute = atr.get(ldapMembershipAttrib);
		NamingEnumeration<?> groups = groupsAttribute.getAll();
		if (ldapMembershipAttribContent.equalsIgnoreCase(ldapGroupNameAttrib)) {
			while (groups.hasMore()) {
				foundGroups.append(DELIMITER + groups.next());
			}
		} else {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String returnedAtts[] = { ldapGroupNameAttrib };
			constraints.setReturningAttributes(returnedAtts);
			String ldapFilter = "(" + ldapMembershipAttribContent + "=";

			if (areValuesBinary) {
				if (ldapGroupSearchBase == null) {
					String funcmsg = sResHash
							.getString(
									"SEVER.API.LDAP.AUTHENTICATION.GROUP.NO.SEARCHBASE",
									PARAM_LDAP_GROUP_SEARCHBASE);
					logError(funcmsg);
					throw new Exception(funcmsg);
				}
				ldapFilter += "{0})";
				while (groups.hasMore()) {
					Object next = groups.next();
					NamingEnumeration<SearchResult> groupResult = mSearchCtx
							.search(ldapGroupSearchBase, ldapFilter,
									new Object[] { next }, constraints);
					if (groupResult.hasMore()) {
						SearchResult foundGroup = groupResult.next();
						attributeValues = foundGroup.getAttributes().get(
								ldapGroupNameAttrib).getAll();
						while (attributeValues.hasMore()) {
							foundGroups.append(DELIMITER
									+ attributeValues.next());
						}
					}
				}
			} else if (ldapGroupSearchBase != null
					&& !ldapMembershipAttribContent.equalsIgnoreCase("dn")) {
				while (groups.hasMore()) {
					ldapFilter += groups.next() + ")";
					NamingEnumeration<SearchResult> groupResult = mSearchCtx
							.search(ldapGroupSearchBase, ldapFilter,
									constraints);
					if (groupResult.hasMore()) {
						SearchResult foundGroup = groupResult.next();
						attributeValues = foundGroup.getAttributes().get(
								ldapGroupNameAttrib).getAll();
						while (attributeValues.hasMore()) {
							foundGroups.append(DELIMITER
									+ attributeValues.next());
						}
					}
				}
			} else {
				while (groups.hasMore()) {
					String[] groupnameAttribute = new String[] { ldapGroupNameAttrib };
					String next = (String) groups.next();
					try {
						Attributes registryAtr = mSearchCtx.getAttributes(next,
								groupnameAttribute);
						attributeValues = registryAtr.get(ldapGroupNameAttrib)
								.getAll();
						while (attributeValues.hasMore()) {
							foundGroups.append(DELIMITER
									+ attributeValues.next());
						}
					} catch (Exception e) {
						APIEngine
								.logInfo(sResHash
										.getString(
												"SEVER.API.LDAP.AUTHENTICATION.GROUP.BIND.UNSUCCESSFUL",
												next));
					}
				}
			}
		}

		userGroups = foundGroups.toString();
	}

	/**
	 * Convenience method, which checks whether some required "LDAP group
	 * support" property is not set.
	 * 
	 * @return The "LDAP group support"-related property, which should be set.
	 * @since 7.0
	 */
	private String findNullValue() {
		if (ldapMembershipAttrib == null)
			return APIEngine.PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE;
		if (ldapMembershipAttribContent == null)
			return APIEngine.PROP_API_CUSTOM_AUTH_LDAP_USERMEMBERSHIP_ATTRIBUTE_CONTENT;
		if (ldapGroupNameAttrib == null)
			return APIEngine.PROP_API_CUSTOM_AUTH_LDAP_GROUPNAME_ATTRIBUTE;
		return null;
	}

	/**
	 * @return String object containing the LDAP groups, which the
	 *         authenticating user is member of .
	 * @since 7.0
	 */
	public String getUserGroups() {
		return userGroups;
	}
}
