/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.easyetl.internal;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.ui.curi.ConnectorUtils;
import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;
import com.ibm.di.web.common.internal.auth.WebSecuritySettings;
import com.ibm.di.web.common.internal.jaxrs.JaxRsInitializableServlet;
import com.ibm.di.web.common.internal.jaxrs.JaxRsServlet;

public class AppActivator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// public static final L10N L10N = new L10N(FrameworkUtil.getBundle(AppActivator.class));

	private LocalApiAuthHttpContext localAuthCtx;

	private HttpService httpSrvc;

	private IServerAPIConnectionService apiSrvc;

	private static String curiUrlPrefix = System.getProperty("com.ibm.tdi.curi.url.prefix", "/ibm");
	
	@SuppressWarnings("unused")
	private synchronized void activate(ComponentContext cc) throws ServletException, NamespaceException {

		HttpContext ctx = getHttpContext();
		boolean enabled = Boolean.valueOf(System.getProperty("dashboard.on", "true"));
		if(enabled) {
			// create this servlet because it will automatically send an
			// initialization request, otherwise use the RestServlet instead.
			JaxRsInitializableServlet servlet = new JaxRsInitializableServlet(new EasyEtlApplication());

			// Set attribute before registration to avoid ServletConfig timing issue
			servlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);

			httpSrvc.registerResources("/dashboard/static", "/static", ctx);
			httpSrvc.registerServlet("/dashboard", servlet, null, ctx);

			JaxRsInitializableServlet curiservlet = new JaxRsInitializableServlet(new CuriApplication());
			ConnectorUtils.logdebug("Using '" + curiUrlPrefix + "' for CURI service (com.ibm.tdi.curi.url.prefix)");
			
			// Set attribute before registration to avoid ServletConfig timing issue
			curiservlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
			
			httpSrvc.registerServlet(curiUrlPrefix, curiservlet, null, null);
			
			/*
            ISDIDEV-552
            upon hitting "http://svdi:1098/" endpoint, the req falls back to Jetty server, 
            which responds with the default 404 page that exposes the Jetty version.
            To prevent this, we override the fallback behavior by adding a fallbackServlet
            that serves a custom error page and sets appropriate header values.
            */
			try {
                HttpServlet fallbackServlet = new HttpServlet() {

                    private static final long serialVersionUID = 1L;
                    private final String notFoundHtmlPage;

                    {
                        StringBuilder sb = new StringBuilder(512);
                        sb.append("<!DOCTYPE html>")
                          .append("<html><head><title>404 Not Found</title></head>")
                          .append("<body><h1>404 - Page Not Found</h1>")
                          .append("<p>The requested endpoint does not exist.</p>");

                        for (int i = 0; i < 10; i++) {
                            sb.append("<!-- padding -->");
                        }
                        sb.append("</body></html>");
                        notFoundHtmlPage = sb.toString();
                    }

                    @Override
                    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.setContentType("text/html; charset=UTF-8");
                        resp.setHeader("Server", "Integrator");
                        resp.getWriter().write(notFoundHtmlPage);
                    }
                };

                httpSrvc.registerServlet("/", fallbackServlet, null, null);

            } catch (NamespaceException e) {
                System.out.println("[Jetty] [ServerActivator] [easyetl] Could not register fallback 404 servlet at '/': " + e.getMessage());
            }
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
			localAuthCtx = new AuthHttpContext(httpSrvc.createDefaultHttpContext(), WebSecuritySettings.getSharedProperties());
		}
		return localAuthCtx;
	}

}
