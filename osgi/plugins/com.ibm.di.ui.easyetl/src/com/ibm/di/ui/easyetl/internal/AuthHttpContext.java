/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

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
import javax.servlet.http.Cookie;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 
ISDIDEV-552
PropertiesFile ->  
to update the property value in solution.prop file during runtime
*/
import com.ibm.di.util.PropertiesFile;

import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;

public class AuthHttpContext extends LocalApiAuthHttpContext {
	
	public final static String PROP_DASHBOARD_AUTH = "dashboard.auth";
	public final static String PROP_DASHBOARD_AUTH_WELCOME = "dashboard.auth.welcome.message";
	
	public final static String PROP_DASHBOARD_AUTH_LOCALHOST = "dashboard.auth.localhost";
	public final static String PROP_DASHBOARD_AUTH_REMOTE = "dashboard.auth.remote";
	
	public final static String PROP_DASHBOARD_AUTH_LDAP_URL = "dashboard.auth.ldap.url";
	public final static String PROP_DASHBOARD_AUTH_LDAP_GROUP = "dashboard.auth.ldap.group";
	public final static String PROP_DASHBOARD_AUTH_LDAP_GROUP_OLD = "dashboard.auth.ldap.url.group";
	public final static String PROP_DASHBOARD_AUTH_MAX_AGE = "dashboard.auth.max.age";
	
	
	public final static String authTypeNone = "none";
	public final static String authTypeNotAllowed = "deny";
	public final static String authTypeProperties = "properties";
	public final static String authTypeLdap = "ldap";

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(AuthHttpContext.class);
	
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
	
	private Dictionary<Object, Object> props;

	public AuthHttpContext(HttpContext defaultHttpContext, Dictionary<Object, Object> props) {
		super(defaultHttpContext, props);

		Object propVal = props.get(PROP_DASHBOARD_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal.toString() : null);
		
		// Copy the values into java.lang.System.Properties.
		propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
		if(propVal != null)
			System.setProperty(PROP_DASHBOARD_AUTH_LOCALHOST, propVal.toString());
		
		propVal = props.get(PROP_DASHBOARD_AUTH_REMOTE);
		if(propVal != null)
			System.setProperty(PROP_DASHBOARD_AUTH_REMOTE, propVal.toString());
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_URL);
		if(propVal != null)
			System.setProperty(PROP_DASHBOARD_AUTH_LDAP_URL, propVal.toString());
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if(propVal != null)
			System.setProperty(PROP_DASHBOARD_AUTH_LDAP_GROUP, propVal.toString());

		this.props = props;
	}
	
/**
 * Currently not used
 * @param props
 */
	public void refresh(Map<String, String> props) {
// This could accidentally turn off all authentication, meaning everybody has access.
//		String propVal = props.get(PROP_DASHBOARD_AUTH);
//		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal : null);
		
		String propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
		if(propVal != null)
			localhost = propVal;
		
		propVal = props.get(PROP_DASHBOARD_AUTH_REMOTE);
		if(propVal != null)
			remote = propVal;
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_URL);
		if(propVal != null)
			ldapUrl = propVal;
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if(propVal != null)
			ldapGroupUrl = propVal;
	}
	
	/**
	 * Refresh values from java.lang.System.Properties.
	 * This allows the values to be changed dynamically.
	 */
	public void refresh() {
		localhost = System.getProperty(PROP_DASHBOARD_AUTH_LOCALHOST, authTypeNone);
		remote = System.getProperty(PROP_DASHBOARD_AUTH_REMOTE, authTypeNotAllowed);
		ldapUrl = System.getProperty(PROP_DASHBOARD_AUTH_LDAP_URL);	
		ldapGroupUrl = System.getProperty(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if (ldapGroupUrl == null)
			ldapGroupUrl = System.getProperty(PROP_DASHBOARD_AUTH_LDAP_GROUP_OLD);
	}
	

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("X-Frame-Options","SAMEORIGIN"); //XFEHT-SDS-315 - clickjacking
		response.setHeader("Strict-Transport-Security", "max-age=31536000;includeSubDomains"); //XFEHT-SDS-309 - missing HSTS header
		if(!this.authEnabled) {
			return true;
		}

		HttpSession session = request.getSession(false);

		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/") || 
            pathInfo.equals("/dashboard") || pathInfo.equals("/logout.html")) {
			invalidateSession(response, session);
			response.sendRedirect("/dashboard/static/login.html");
			return true;			
		}

        if (pathInfo.startsWith("/login") || pathInfo.startsWith("/dojo") || 
            pathInfo.startsWith("/tdi") || pathInfo.startsWith("/tdinls")) {
            return true;
		}

        // ISDIDEV-552: Allow new files and endpoints for the change password scenario
        if (pathInfo.startsWith("/password") || 
            pathInfo.startsWith("/password.html") || 
            pathInfo.startsWith("/dashboard/static/password.html")) {
            return true;
        }
					
		/*
		 * Ensure that the session hasn't expired.
		 */
		if (session != null && sessionHasExpired(session)) {
			invalidateSession(response, session);
			response.sendRedirect("/dashboard/static/login.html");
			return true;
		}

		if (session != null && !session.isNew() && session.getAttribute(HttpContext.REMOTE_USER) != null) {
			/*
			 * to improve performance, automatically authenticate existing
			 * servlet sessions
			 */
			return true;
		}
		
		if (checkCredentials(request, response)) {
			setMaxAge(request.getSession());

			return true;
		} else {
			return false;
		}
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

    /*
    ISDIDEV-552 [ISDIPSIRT-5]
    Updates the password in the solution.properties file using the PropertiesFile wrapper.
    The new password takes effect immediately, allowing users to log in without restarting
    the SDI server or Content Engine (CE)
    */
    private void changeDefaultPassword(String newpassword, String propname) throws IOException {

        String solnDir = System.getenv("TDI_SOLDIR");
        String fullPath = solnDir + File.separator + "solution.properties";

        try {
            PropertiesFile propsFile = new PropertiesFile(fullPath, false);
            propsFile.setProperty(propname, newpassword);
            propsFile.store(fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
	
	private boolean checkCredentials(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		refresh();
		String method = isLocalHostRequest(request) ? localhost : remote;
		Exception error = null;

		if (method == null || authTypeNone.equals(method)) {
			return true;
		} 
        else if (authTypeNotAllowed.equals(method)) {
			error = new Exception("Connection from " + request.getRemoteHost() + " is not permitted");
			log.warn(error.getMessage());
		} 
        else {
			String user = request.getParameter("username");
			String pass = request.getParameter("password");
            /*
            ISDIDEV-552 [ISDIPSIRT-5]
            action      – When the action is "passwordchange" from the frontend,
                          triggers the password change flow for both in‑memory and file storage.
            newPassword – Value of new password received from the frontend (password change form)       
            */
            String action = request.getParameter("action");
            String newpassword = request.getParameter("newpassword");
			
			if (user == null || user.trim().length() == 0) {
				invalidateSession(response, request.getSession(false));
				response.sendRedirect("/dashboard/static/login.html");
				return false;
			} 
            else if(authTypeProperties.equals(method)) {
				String propname = "dashboard.auth.user." + user;
				if(props != null) {
					Object pwd = props.get(propname);
                    /* 
                    ISDIDEV-552 : 
                    Handles the first‑time login scenario 
                    where the admin user provides default credentials,
                    it redirects to change password page
                    */               
                    if ("admin".equals(user) && "admin".equals(pass) && "admin".equals(pwd)) {
                        invalidateSession(response, request.getSession(false));
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{ \"status\": \"forcePasswordChange\" }");
                        return false;
                    }

                    /*
                    * ISDIDEV-552:
                    * Handles the default password change in both the solution.properties file
                    * and in in‑memory storage (props object).
                    */
                    if ("passwordchange".equals(action)) {
                        changeDefaultPassword(newpassword, propname);
                        props.put(propname, newpassword);
                        return false;
                    }

                    // ISDIDEV-552 : ensures no session present before
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        invalidateSession(response, request.getSession(false));
                    }

					if(pwd != null && pwd.equals(pass)) {
                        // ISDIDEV-552 [ISDIPSIRT-5] checking password strength
                        if (!checkPasswordPolicy(pass)) {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{ \"status\": \"forcePasswordChange\" }");
                            return false;
                        }
						request.setAttribute(HttpContext.REMOTE_USER, user);
						request.setAttribute(HttpContext.AUTHENTICATION_TYPE, method);
						request.getSession().setAttribute(HttpContext.REMOTE_USER, user);
						return true;
					}
				}				
				error = new Exception("Bad username/password");
				
			} 
            else if (authTypeLdap.equals(method)) {
				try {
					String dn = user;
					InitialLdapContext ldap = null;
					
					if (pass == null || pass.length() == 0) {
						dn = null;
						error = new Exception("No password provided");
					} 
                    else if(user.matches(".*@.*")) {
						ldap = getLdapContext(null, null);
						SearchControls cons = new SearchControls();
						cons.setCountLimit(2);
						cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
						cons.setTimeLimit(10 * 1000);
						NamingEnumeration<SearchResult> result = ldap.search("", "(mail=" + user + ")", cons);
						if(result != null && result.hasMore()) {
							SearchResult next = result.nextElement();
							dn = next.getNameInNamespace();
						}
						if(result != null && result.hasMore()) {
							dn = null;
							error = new Exception("Email address is not unique");
						}
						ldap.close();
						ldap = null;
					}
					
					if(dn != null) {
						ldap = getLdapContext(dn, pass);
						ldap.close();
						checkMembership(dn);
						request.setAttribute(HttpContext.REMOTE_USER, user);
						request.setAttribute(HttpContext.AUTHENTICATION_TYPE, method);
						request.getSession().setAttribute(HttpContext.REMOTE_USER, user);
						return true;
					}
				} catch (Exception e) {
					log.error(user, e);
					error = e;
				}
			}
		}
	
		invalidateSession(response, request.getSession(false));
		
		if(error != null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(error.getLocalizedMessage());
			return false;
		}
		
		response.sendRedirect("/dashboard/static/login.html");
		return false;
	}

    /*
    * ISDIDEV-552 [ISDIPSIRT-5]:
    * checks the password policy -
    * Password must be at least 8 characters. 
    * It must contain one upper case, one lower case, one numerical, and 
    * one special character. 
    * The special character cannot be <, >, `, $, |, ;, or &.
    * If not redirect to password change page
    */
    private boolean checkPasswordPolicy(String password) {

        if (password == null || password.length() < 8) 
            return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String disallowed = "<>`$|;&";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#%^*()_+=-[]{}:\"\\,.?/".indexOf(c) >= 0) hasSpecial = true;

            if (disallowed.indexOf(c) >= 0) return false;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
	
	private void checkMembership(String dn) throws Exception {
		if(ldapGroupUrl == null || ldapGroupUrl.length() == 0)
			return;
		
		InitialLdapContext ldap = getGroupLdapContext();
		try {
			SearchControls cons = new SearchControls();
			cons.setSearchScope(SearchControls.OBJECT_SCOPE);
			cons.setTimeLimit(10 * 1000);
			NamingEnumeration<SearchResult> result = ldap.search(ldapGroupUrl, "objectClass=*", cons);
			if(result != null && result.hasMore()) {
				SearchResult next = result.nextElement();
				Attribute attr = next.getAttributes().get("uniquemember");
				if(attr == null)
					attr = next.getAttributes().get("member");
				if(attr == null)
					throw new Exception("Group does not have 'member' or 'uniquemember' attribute");
				for(int i = 0; i < attr.size(); i++) {
					String value = attr.get(i).toString();
					if(value.equalsIgnoreCase(dn))
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
		if(ldapGroupUrl.startsWith("ldap:"))
			ht.put(InitialLdapContext.PROVIDER_URL, ldapGroupUrl);
		else
			ht.put(InitialLdapContext.PROVIDER_URL, ldapUrl.substring(0, ldapUrl.lastIndexOf("/")));
		ht.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		return new InitialLdapContext(ht, null);
	}

	private InitialLdapContext getLdapContext(String user, String pass) throws NamingException {
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(InitialLdapContext.PROVIDER_URL, ldapUrl);
		ht.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		if(user != null) {
			ht.put(Context.SECURITY_AUTHENTICATION, "simple");
			ht.put(Context.SECURITY_PRINCIPAL, user);
			if(pass != null)
				ht.put(Context.SECURITY_CREDENTIALS, pass);
		}
		return new InitialLdapContext(ht, null);
	}

}
