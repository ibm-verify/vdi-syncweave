/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.*;  

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
	
	public final static String PROP_DASHBOARD_AUTH_LOGIN_URL = "dashboard.auth.login.url";
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
	
	/**
	 * Login/logout urls
	 */
	private String loginUrl = "/login.html";
	private String logoutUrl = "/logout.html";
    // ISDIDEV-552 : new url for change password page
	private String passwordUrl = "/password.html";

	static int cnt;
	static boolean user_locked = false;
	static Date start_time, end_time;
	/**
	 * 
	 */
	private boolean debug = Boolean.getBoolean("dashboard.auth.debug");
	
	private Dictionary<Object, Object> props;

	public AuthHttpContext(HttpContext defaultHttpContext, Dictionary<Object, Object> props) {
		super(defaultHttpContext, props);

		Object propVal = props.get(PROP_DASHBOARD_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal.toString() : null);
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
		if(propVal != null)
			localhost = propVal.toString();
		
		propVal = props.get(PROP_DASHBOARD_AUTH_REMOTE);
		if(propVal != null)
			remote = propVal.toString();
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_URL);
		if(propVal != null)
			ldapUrl = propVal.toString();
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LDAP_GROUP);
		if(propVal != null)
			ldapGroupUrl = propVal.toString();
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LOGIN_URL);
		if(propVal != null)
			loginUrl = propVal.toString();

		this.props = props;
	}
	
	public void refresh(Map<String, String> props) {
		String propVal = props.get(PROP_DASHBOARD_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal : null);
		
		propVal = props.get(PROP_DASHBOARD_AUTH_LOCALHOST);
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
	 * @return null
	 */
	@Override
	public URL getResource(String name) {
		if(name != null && name.indexOf("//") != -1) {
			name = name.replaceAll("//", "/");
		}
		return super.getResource(name);
	}

	

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		Exception error = null;
		response.setHeader("X-Frame-Options","SAMEORIGIN"); //XFEHT-SDS-315 - clickjacking
		response.setHeader("Strict-Transport-Security", "max-age=31536000;includeSubDomains"); //XFEHT-SDS-309 -missing HSTS header
		
		response.setHeader("Set-Cookie", "SameSite=Strict;HttpOnly=true;");	
		response.setHeader("Server", "unknown");

		if(!this.authEnabled) {
			return true;
		}

		HttpSession session = request.getSession(false);

		String pathInfo = request.getPathInfo();

		if (pathInfo != null) {

			if (pathInfo.equals(logoutUrl)) {
				invalidateSession(response, session);
				sendRedirectLogin(request, response);
				return true;
			}

			if (pathInfo.startsWith(loginUrl) || pathInfo.startsWith("/dojo") || 
                pathInfo.startsWith("/tdi/styles.css") || pathInfo.startsWith("/ibmjs") || 
                pathInfo.startsWith("/dijit") || pathInfo.startsWith("/idx") || 
                pathInfo.startsWith("/codemirror") || pathInfo.startsWith("/djconfig") || 
                pathInfo.startsWith("/tdinls") || pathInfo.startsWith("/tdi/NlsMixin") || 
                pathInfo.startsWith("/images")|| pathInfo.startsWith("/login") || 
                pathInfo.startsWith("/tdi/Main") || pathInfo.contains("tdi/nls")
				) 
            {
				return true;
			}

            // ISDIDEV-552: Allow new files and endpoints for the change password scenario
            if (pathInfo.startsWith(passwordUrl) || pathInfo.startsWith("/password") ) {
                return true;
			}
			
		}
	
		/*
		 * Ensure that the session hasn't expired.
		 */
		if (session != null && sessionHasExpired(session)) {
			invalidateSession(response, session);
			sendRedirectLogin(request, response);
			return true;
		}

		if (session != null) {
			/*
			 * to improve performance, automatically authenticate existing
			 * servlet sessions
			 */
			return true;
		}

		if(user_locked){ //check lockout time
			end_time = new Date();			
			long difference_In_Time = end_time.getTime() - start_time.getTime();
			long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
			
			log.info("User is locked out. Time elapsed = "+difference_In_Minutes);
			if(difference_In_Minutes < 30){
				log.info("Account is locked out. Please try again after 30 minutes.");
				error = new Exception("Account is locked out. Please try again after 30 minutes.");

				invalidateSession(response, request.getSession(false));
				if(error != null) {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.getWriter().write(error.getLocalizedMessage());
					return false;
				}
				return false;
			}				
			else{
				user_locked = false;
				cnt = 0;
				log.info("User may try login now.");
			}
		}
		if(isLocalHostRequest(request)) {			
			if(this.localhost == null || authTypeNone.equals(this.localhost))
				return true;
			else{
				if (!user_locked)
					return checkCredentials(request, response, session, this.localhost);
				else 
					return true;
			}
		} else {
			if(this.remote == null || authTypeNone.equals(this.remote))
				return true;
			else{
				if(!user_locked)
					return checkCredentials(request, response, session, this.remote);
				else 
					return true;
			}
		}
		
	}
	
	private void sendRedirectLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String path = request.getServletPath();

		invalidateSession(response, request.getSession(false));

		if(!path.endsWith("/static"))
			path = path + "/static";
		if(debug) {
			System.out.println("FDS: redirect: from=" + request.getRequestURL() + ", to=" + path + " + " + loginUrl);
		}
		if(!user_locked){
			response.sendRedirect(path + loginUrl);
			}
		else{
			String p = request.getRequestURL().toString();
			if(p != null){
				String p1 = p.substring(0, p.indexOf("static")+6);
				response.sendRedirect(p1);
			}			
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
            System.err.println("Error updating properties file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

	}
	
	private boolean checkCredentials(HttpServletRequest request, HttpServletResponse response, HttpSession session, String method) throws IOException {

		boolean rc = checkCredentials(request, response, method);

		if (rc) {
			setMaxAge(request.getSession());
		}

		return rc;
	}

	private boolean checkCredentials(HttpServletRequest request, HttpServletResponse response, String method) throws IOException {
		
		Exception error = null;

		if (!authTypeNotAllowed.equals(method)) {
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
			
			if (user != null && authTypeProperties.equals(method)) {
				String propname = "dashboard.auth.user." + user;

				if (props != null) {
					Object pwd = props.get(propname);
                    /* 
                    ISDIDEV-552 : 
                    Handles the first‑time login scenario 
                    where the admin user provides default credentials,
                    it redirects to change password page
                    */    
                    if ("admin".equals(user) && "admin".equals(pass) && "admin".equals(pwd)) {
                        invalidateSession(response, request.getSession(false));
                        String path = request.getServletPath();
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{ \"status\": \"forcePasswordChange\" }");
                        return false;
                    }

                    if ("changePassword".equals(action)) {
                        changeDefaultPassword(newpassword, propname);
                        props.put(propname, newpassword);
                        // terminating session if any present
                        HttpSession session = request.getSession(false);
                        invalidateSession(response, request.getSession(false));
                        return false;
                    }

					if (pwd != null && pwd.equals(pass)) {
                        // ISDIDEV-552 [ISDIPSIRT-5] checking password strength
                        if (!checkPasswordPolicy(pass)) {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{ \"status\": \"forcePasswordChange\" }");
                            return false;
                        }
						request.setAttribute(HttpContext.REMOTE_USER, user);
						request.setAttribute(HttpContext.AUTHENTICATION_TYPE, method);
						cnt = 0;
						user_locked = false;						

						return true;
					}
					else{ //login failure	
						if( (pwd != null) && (cnt < 6) ){
							//login failure attempts not reached, admin user name is correct, increment counter
							log.info("User name is correct but password is incorrect.", cnt++);	
							error = new Exception("Incorrect password.");
						}
						if ( (pwd != null) && (cnt >=6) ){
							user_locked = true;
							//login failure attempts reached, admin user name is correct, lockout user
							log.info("Account locked out. You can try login after 30 minutes.");
							start_time = new Date();
							log.info(start_time.toString());
							error = new Exception("Account locked out. You can try login after 30 minutes.");
							sendRedirectLogin(request, response);
							return false;
						}									
					}//end of else login failure
				}
				error = new Exception("Bad username/password");			
				
			} else if (user != null && authTypeLdap.equals(method)) {
				try {
					String dn = user;
					InitialLdapContext ldap = null;
					
					if(user.matches(".*@.*")) {
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
						return true;
					}
				} catch (Exception e) {
					log.error(user, e);
					error = e;
				}
			}
		} else if (authTypeNotAllowed.equals(method) ){
			error = new Exception("Connection from " + request.getRemoteHost() + " is not permitted");
			log.warn(error.getMessage());
		}

		invalidateSession(response, request.getSession(false));
		
		if(error != null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(error.getLocalizedMessage());
			return false;
		}
		
		sendRedirectLogin(request, response);
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
