/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapAuthHttpContext extends LocalApiAuthHttpContext {

	public final static String PROP_DASHBOARD_AUTH = "dashboard.auth";
	public final static String PROP_DASHBOARD_AUTH_WELCOME = "dashboard.auth.welcome.message";

	public final static String PROP_DASHBOARD_AUTH_LOCALHOST = "dashboard.auth.localhost";
	public final static String PROP_DASHBOARD_AUTH_REMOTE = "dashboard.auth.remote";

	public final static String PROP_DASHBOARD_AUTH_LDAP_URL = "dashboard.auth.ldap.url";
	public final static String PROP_DASHBOARD_AUTH_LDAP_GROUP = "dashboard.auth.ldap.group";

	public final static String authTypeNone = "none";
	public final static String authTypeNotAllowed = "deny";
	public final static String authTypeProperties = "properties";
	public final static String authTypeLdap = "ldap";

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory
			.getLogger(LdapAuthHttpContext.class);

	/**
	 * Auth option for localhost connections
	 */
	private String localhost = null;

	/**
	 * Auth option for remote connections
	 */
	private String remote = null;

	/**
	 * Auth enabled
	 */
	private boolean authEnabled;

	/**
	 * LDAP Params
	 */
	private String ldapUrl;

	/**
	 * LDAP Group URL
	 */
	private String ldapGroupUrl;

	private static Dictionary<Object, Object> props;

	public LdapAuthHttpContext(HttpContext defaultHttpContext,
			Dictionary<Object, Object> props) {
		super(defaultHttpContext, props);

		Object propVal = props.get(PROP_DASHBOARD_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal
				.toString() : null);

		propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
		if (propVal != null)
			localhost = propVal.toString();

		propVal = props.get(PROP_DASHBOARD_AUTH_REMOTE);
		if (propVal != null)
			remote = propVal.toString();

		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_URL);
		if (propVal != null)
			ldapUrl = propVal.toString();

		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if (propVal != null)
			ldapGroupUrl = propVal.toString();

		LdapAuthHttpContext.props = props;
	}

	public void refresh(Map<String, String> props) {
		String propVal = props.get(PROP_DASHBOARD_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal
				: null);

		propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
		if (propVal != null)
			localhost = propVal;

		propVal = props.get(PROP_DASHBOARD_AUTH_REMOTE);
		if (propVal != null)
			remote = propVal;

		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_URL);
		if (propVal != null)
			ldapUrl = propVal;

		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if (propVal != null)
			ldapGroupUrl = propVal;
	}
	
	public static Properties getProperties() {
		return WebSecuritySettings.getSharedProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		if (!this.authEnabled) {
			return true;
		}

		HttpSession session = request.getSession(false);

		if (session != null && !session.isNew() && !sessionHasExpired(session)) {
			/*
			 * to improve performance, automatically authenticate existing
			 * servlet sessions
			 */
			return true;
		}

		String username = null;
		boolean result;
		try {
			final String[] credentials = parseCredentials(request);
			username = credentials[0];
			final String password = credentials[1];
			result = authenticate(request, response, username, password);
		} catch (Exception e) {
			result = false;
		}

		if (result) {
			request.setAttribute(HttpContext.REMOTE_USER, username);
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, "BASIC");
		} else {
			invalidateSession(response, session);

			askForCredentials(response);
		}

		return result;
	}

	protected boolean authenticate(HttpServletRequest request,
			HttpServletResponse response, String username, String password) {
		try {
			if (isLocalHostRequest(request)) {
				if (this.localhost == null
						|| authTypeNone.equals(this.localhost))
					return true;
				else if (this.localhost != null)
					return checkCredentials(username, password, this.localhost);
			} else {
				if (this.remote == null || authTypeNone.equals(this.remote))
					return true;
				else
					return checkCredentials(username, password, this.remote);
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private boolean isLocalHostRequest(HttpServletRequest request) {
		try {
			InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
			if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
				return true;

			// Check if the address is defined on any interface
			try {
				return NetworkInterface.getByInetAddress(addr) != null;
			} catch (Exception e) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean checkCredentials(String username, String password,
			String method) throws Exception {

		if (authTypeNotAllowed.equals(method)) {
			return false;
			
		} else if (username != null && authTypeProperties.equals(method)) {
			// -- Properties authentication
			String propname = "dashboard.auth.user." + username;
			if (props != null) {
				Object pwd = props.get(propname);
				if (pwd != null && pwd.equals(password)) {
					return true;
				}
			}
			return false;

		} else if (username != null && authTypeLdap.equals(method)) {
			// -- LDAP authentication
			try {
				String dn = username;
				InitialLdapContext ldap = null;

				if (username.matches(".*@.*")) {
					/*
					 * Do a lookup using the mail attribute for a match We set
					 * the limit to 2 entries so we can detect if the result is
					 * unique
					 */
					ldap = getLdapContext(null, null);
					SearchControls cons = new SearchControls();
					cons.setCountLimit(2);
					cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
					cons.setTimeLimit(10 * 1000);
					NamingEnumeration<SearchResult> result = ldap.search("",
							"(mail=" + username + ")", cons);
					if (result != null && result.hasMore()) {
						SearchResult next = result.nextElement();
						dn = next.getNameInNamespace();
					}
					// -- got one entry, now check if there is another
					if (result != null && result.hasMore()) {
						dn = null;
					}
					ldap.close();
					ldap = null;
				}

				if (dn != null) {
					ldap = getLdapContext(dn, password);
					ldap.close();
					checkMembership(dn);
					return true;
				}
			} catch (Exception e) {
				log.error(username, e);
				return false;
			}
		}

		return false;
	}

	private void checkMembership(String dn) throws Exception {
		if (ldapGroupUrl == null || ldapGroupUrl.length() == 0)
			return;

		InitialLdapContext ldap = getGroupLdapContext();
		try {
			SearchControls cons = new SearchControls();
			cons.setSearchScope(SearchControls.OBJECT_SCOPE);
			cons.setTimeLimit(10 * 1000);
			NamingEnumeration<SearchResult> result = ldap.search(ldapGroupUrl,
					"objectClass=*", cons);
			if (result != null && result.hasMore()) {
				SearchResult next = result.nextElement();
				Attribute attr = next.getAttributes().get("uniquemember");
				if (attr == null)
					attr = next.getAttributes().get("member");
				if (attr == null)
					throw new Exception(
							"Group does not have 'member' or 'uniquemember' attribute");
				for (int i = 0; i < attr.size(); i++) {
					String value = attr.get(i).toString();
					if (value.equalsIgnoreCase(dn))
						return;
				}
				throw new Exception(dn + " is not a member of " + ldapGroupUrl);
			}
		} finally {
			ldap.close();
		}
		throw new Exception(ldapGroupUrl + " does not exist");
	}

	private InitialLdapContext getGroupLdapContext() throws NamingException {
		Hashtable<String, String> ht = new Hashtable<String, String>();
		if (ldapGroupUrl.startsWith("ldap:"))
			ht.put(InitialLdapContext.PROVIDER_URL, ldapGroupUrl);
		else
			ht.put(InitialLdapContext.PROVIDER_URL, ldapUrl.substring(0,
					ldapUrl.lastIndexOf("/")));
		ht.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		return new InitialLdapContext(ht, null);
	}

	private InitialLdapContext getLdapContext(String user, String pass)
			throws NamingException {
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(InitialLdapContext.PROVIDER_URL, ldapUrl);
		ht.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		if (user != null) {
			ht.put(Context.SECURITY_AUTHENTICATION, "simple");
			ht.put(Context.SECURITY_PRINCIPAL, user);
			if (pass != null)
				ht.put(Context.SECURITY_CREDENTIALS, pass);
		}
		return new InitialLdapContext(ht, null);
	}

}
