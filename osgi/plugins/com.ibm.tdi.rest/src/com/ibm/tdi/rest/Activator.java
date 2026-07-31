/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import javax.servlet.ServletException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.web.common.internal.auth.LdapAuthHttpContext;
import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;
import com.ibm.di.web.common.internal.auth.WebSecuritySettings;
import com.ibm.di.web.common.internal.jaxrs.JaxRsInitializableServlet;

public class Activator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// public static final L10N L10N = new L10N(FrameworkUtil.getBundle(AppActivator.class));

	private LocalApiAuthHttpContext localAuthCtx;

	private HttpService httpSrvc;

	private IServerAPIConnectionService apiSrvc;

//	private ClientToMaster client;

	@SuppressWarnings("unused")
	private synchronized void activate(ComponentContext cc) throws ServletException, NamespaceException {
		boolean enabled = Boolean.valueOf(System.getProperty("sdirest.on", "true"));
		String url = System.getProperty("sdirest.url", "/sdi");
		if(enabled) {
			System.out.println("com.ibm.tdi.rest started on " + url);
			JaxRsInitializableServlet tdiservlet = new JaxRsInitializableServlet(new CommonRestApplication());
			// Set the API service before registering - servlet will use it after init
			tdiservlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
			httpSrvc.registerServlet(url, tdiservlet, null, getHttpContext());
			startDialHomeServer();
		}
	}

	/**
	 * This method starts a timer that periodically posts a status entry to a URL.
	 */
	private void startDialHomeServer() {
		String url = System.getProperty("com.ibm.tdi.rest.client.url");
		if(url != null && url.length() > 0) {
//			this.client = new ClientToMaster(url);
//			this.client.activate();
		}

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
	}

	@SuppressWarnings("unused")
	private synchronized void unbind(IServerAPIConnectionService srvc) {
		getHttpContext().setServerAPIConnectionService(null);
	}

	@SuppressWarnings("unused")
	private synchronized void bindServiceLocatorGenerator(Object generator) {
		// This method exists solely to create a dependency on ServiceLocatorGenerator
		// The activator won't start until SPI Fly has registered this service
	}

	@SuppressWarnings("unused")
	private synchronized void unbindServiceLocatorGenerator(Object generator) {
		// No-op
	}

	private LocalApiAuthHttpContext getHttpContext() {
		if (localAuthCtx == null) {
			localAuthCtx = new LdapAuthHttpContext(httpSrvc.createDefaultHttpContext(), WebSecuritySettings.getSharedProperties());
		}
		return localAuthCtx;
	}

}
