/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.auth;

import java.util.Enumeration;
import java.util.Properties;

public class WebSecuritySettings {

	public final static String PROP_DASHBOARD_AUTH = "dashboard.auth";
	public final static String PROP_DASHBOARD_AUTH_WELCOME = "dashboard.auth.welcome.message";
	
	public final static String PROP_DASHBOARD_AUTH_LOCALHOST = "dashboard.auth.localhost";
	public final static String PROP_DASHBOARD_AUTH_REMOTE = "dashboard.auth.remote";
	
	public final static String PROP_DASHBOARD_AUTH_LDAP_URL = "dashboard.auth.ldap.url";
	public final static String PROP_DASHBOARD_AUTH_LDAP_GROUP = "dashboard.auth.ldap.group";
	public final static String PROP_DASHBOARD_AUTH_LDAP_GROUP_OLD = "dashboard.auth.ldap.url.group";
	
	public final static String PROP_DASHBOARD_AUTH_LOGIN_URL = "dashboard.auth.login.url";
	public final static String PROP_DASHBOARD_AUTH_MAX_AGE = "dashboard.auth.max.age";
	
	public final static String authTypeNone = "none";
	public final static String authTypeNotAllowed = "deny";
	public final static String authTypeProperties = "properties";
	public final static String authTypeLdap = "ldap";
	
	private static Properties ctxConf = null;
	
	public static Properties getSharedProperties() {
		if (ctxConf == null) {
			ctxConf = new Properties();
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH, System.getProperty("dashboard.auth", "true"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH_REALM, System.getProperty("dashboard.auth.realm", "Security Verify Directory Integrator"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_ATTACH_SESSION, "true");
			
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH, "true"));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LOCALHOST, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LOCALHOST, WebSecuritySettings.authTypeProperties));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_MAX_AGE, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_MAX_AGE, "1800"));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_REMOTE, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_REMOTE, WebSecuritySettings.authTypeNotAllowed));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LDAP_URL, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LDAP_URL, "ldap://localhost:389"));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LDAP_GROUP, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LDAP_GROUP,
																					System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LDAP_GROUP_OLD, "")));
			ctxConf.setProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LOGIN_URL, System.getProperty(WebSecuritySettings.PROP_DASHBOARD_AUTH_LOGIN_URL, "/login.html"));
			
			for(Enumeration<Object> en = System.getProperties().keys(); en.hasMoreElements(); ) {
				String key = en.nextElement().toString();
				if(key.startsWith("dashboard.auth.user.")) {
					String value = System.getProperty(key);
					ctxConf.setProperty(key, value);
					System.setProperty(key, "");
				}
			}
		}
		return ctxConf;
		
	}
}
