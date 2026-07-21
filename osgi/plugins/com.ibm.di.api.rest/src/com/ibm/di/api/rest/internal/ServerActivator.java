/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal;

import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.rest.internal.listener.ListenerFactory;
import com.ibm.di.api.rest.internal.registry.ListenerRegistry;
import com.ibm.di.api.rest.internal.registry.UserDataRegistry;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.http.jetty.listener.internal.HttpSessionListenerProvider;
import com.ibm.di.web.common.internal.auth.LdapAuthHttpContext;
import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;
import com.ibm.di.web.common.internal.auth.WebSecuritySettings;
import com.ibm.di.web.common.internal.jaxrs.JaxRsServlet;

/**
 * This is the entry point of the ReST Server API OSGi bundle. It registers the
 * REST servlet in the OSGi HTTP Service and wires expected components through
 * the {@link ServletContext}. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class ServerActivator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger(ServerActivator.class);

	private LocalApiAuthHttpContext localAuthCtx;

	private HttpService httpSrvc;

	private IServerAPIConnectionService apiSrvc;

	private HttpServlet servlet;

	private static Properties ctxConf;

	private ServiceRegistration sessionCleanerReg;
	
	private static boolean useLdapAuth = true;
	
	private LdapAuthHttpContext ldapAuthCtx;

	@SuppressWarnings("unused")
	private synchronized void activate(ComponentContext cc) throws ServletException, NamespaceException, DIException {
		HttpContext ctx = getHttpContext();

		servlet = new JaxRsServlet(new RestApplication());
		
		// Set attributes before registration - servlet will apply them after init
		if (servlet instanceof JaxRsServlet) {
			JaxRsServlet jaxrsServlet = (JaxRsServlet) servlet;
			jaxrsServlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
			jaxrsServlet.setAttribute(UserDataRegistry.class.getName(), new UserDataRegistry());
			jaxrsServlet.setAttribute(ListenerRegistry.class.getName(), new ListenerRegistry());
			
			// Set callback to complete registrations after servlet initialization
			final JaxRsServlet finalServlet = jaxrsServlet;
			final ComponentContext finalCc = cc;
			jaxrsServlet.setPostInitCallback(new Runnable() {
				public void run() {
					try {
						ServletContext servletContext = finalServlet.getServletContext();
						sessionCleanerReg = finalCc.getBundleContext().registerService(
							HttpSessionListenerProvider.class.getCanonicalName(),
							new SessionCleaner(servletContext), null);
						servletContext.setAttribute(ListenerFactory.class.getName(),
							new ListenerFactory(EnvUtils.getServerApiConnection(servletContext), servletContext));
						servletContext.setAttribute(ExecutorService.class.getName(), Executors.newCachedThreadPool());
					} catch (DIException e) {
						log.error("Failed to complete servlet initialization", e);
						throw new RuntimeException("Failed to initialize REST API servlet", e);
					}
				}
			});
		}

		String restContextRoot = System.getProperty("api.rest.contextRoot", "/rest");
		httpSrvc.registerServlet(restContextRoot, servlet, null, ctx);

		log.info(AppConstants.L10N.getString("REST.API.SERVER.ACTIVE"));
	}

	@SuppressWarnings("unused")
	private synchronized void bind(HttpService srvc) throws ServletException, NamespaceException {
		httpSrvc = srvc;
	}

	@SuppressWarnings("unused")
	private synchronized void bind(IServerAPIConnectionService srvc) {
		apiSrvc = srvc;
		getHttpContext().setServerAPIConnectionService(srvc);
	}

	@SuppressWarnings("unused")
	private synchronized void unbind(HttpService srvc) {
		httpSrvc = null;
		servlet = null;
	}

	@SuppressWarnings("unused")
	private synchronized void unbind(IServerAPIConnectionService srvc) {
		getHttpContext().setServerAPIConnectionService(null);
	}

	@SuppressWarnings("unused")
	private synchronized void deactivate(ComponentContext cc) throws ServletException, NamespaceException {
		sessionCleanerReg.unregister();
		sessionCleanerReg = null;
		log.info(AppConstants.L10N.getString("REST.API.SERVER.INACTIVE"));
	}

	private LocalApiAuthHttpContext getHttpContext() {
		if(useLdapAuth) {
			if(ldapAuthCtx == null) {
				ldapAuthCtx = new LdapAuthHttpContext(httpSrvc.createDefaultHttpContext(), WebSecuritySettings.getSharedProperties());
			}
			return ldapAuthCtx;
		} else {
			if (localAuthCtx == null) {
				localAuthCtx = new LocalApiAuthHttpContext(httpSrvc.createDefaultHttpContext(), getProperties());
			}
			return localAuthCtx;
		}
	}

	private static synchronized Properties getProperties() {
		if (ctxConf == null) {
			ctxConf = new Properties();
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH, System.getProperty("api.rest.auth", "false"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH_REALM, System.getProperty("api.rest.auth.realm",
					"Security Verify Directory Integrator REST Server API"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_ATTACH_SESSION, "true");
		}
		return ctxConf;
	}
	
	/*
	private static synchronized Properties getDashboardProperties() {
		if (ctxConf == null) {
			ctxConf = new Properties();
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH, System.getProperty("dashboard.auth", "true"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH_REALM, System.getProperty("api.rest.auth.realm",
					"Security Verify Directory Integrator ReST Server API"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_ATTACH_SESSION, "true");
			
			ctxConf.setProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH, System.getProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH, "true"));
			ctxConf.setProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST, System.getProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LOCALHOST, LdapAuthHttpContext.authTypeNone));
			ctxConf.setProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE, System.getProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_REMOTE, LdapAuthHttpContext.authTypeNotAllowed));
			ctxConf.setProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL, System.getProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_URL, "ldap://localhost:389"));
			ctxConf.setProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP, System.getProperty(LdapAuthHttpContext.PROP_DASHBOARD_AUTH_LDAP_GROUP, ""));
			
			for(Enumeration<Object> en = System.getProperties().keys(); en.hasMoreElements(); ) {
				String key = en.nextElement().toString();
				if(key.startsWith("dashboard.auth.user.")) {
					ctxConf.setProperty(key, System.getProperty(key));
					System.setProperty(key, "");
				}
			}
		}
		return ctxConf;
	}
	*/
}
