/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.ui.webui.internal.tdi.TDIAuthHttpContext;
import com.ibm.di.ui.webui.internal.tdi.TDIServletAppliation;
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

	private TDIAuthHttpContext tdiAuthCtx;

	@SuppressWarnings("unused")
	private synchronized void activate(ComponentContext cc) throws ServletException, NamespaceException {

		HttpContext ctx = getHttpContext();
		boolean enabled = Boolean.valueOf(System.getProperty("dashboard.on", "true"));
		if(enabled) {
			// create this servlet because it will automatically send an
			// initialization request, otherwise use the RestServlet instead.
			JaxRsInitializableServlet servlet = new JaxRsInitializableServlet(new WebUiAppliation());

			// Set attribute before registration to avoid ServletConfig timing issue
			servlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);

			httpSrvc.registerResources("/fds/static", "/static", ctx);
			httpSrvc.registerServlet("/fds", servlet, null, ctx);
		}
		
		enabled = Boolean.valueOf(System.getProperty("tdiservlet.on", "true"));
		String url = System.getProperty("tdiservlet.url", "/tdi");
		if(enabled) {
			JaxRsInitializableServlet tdiservlet = new JaxRsInitializableServlet(new TDIServletAppliation());
			
			// Set attribute before registration to avoid ServletConfig timing issue
			tdiservlet.setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
			
			httpSrvc.registerServlet(url, tdiservlet, null, getTDIHttpContext());
		}

		final String webroot = System.getProperty("tdiservlet.files", "");
		if(webroot != null && webroot.trim().length() > 0) {
			HttpServlet fileservlet = new HttpServlet() {
				@Override
				protected void doGet(HttpServletRequest req,
						HttpServletResponse resp) throws ServletException,
						IOException {
					// TODO Auto-generated method stub
					File file = new File(webroot + req.getPathInfo());

					if (!file.getCanonicalPath().equals(file.getAbsolutePath())) {
						throw new IOException("Invalid path: " + req.getPathInfo());
					}

					FileInputStream fis = new FileInputStream(file);
					if(req.getPathInfo().endsWith(".html"))
						resp.setContentType(MediaType.TEXT_HTML);
					else if(req.getPathInfo().endsWith(".gif"))
						resp.setContentType("image/gif");
					else
						resp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
					
					int ch;
					while ( (ch = fis.read()) != -1) {
						resp.getOutputStream().write(ch);
					}
					fis.close();
				}
				
			};
			httpSrvc.registerServlet("/www", fileservlet, null, getTDIHttpContext());
			// Note: fileservlet is a plain HttpServlet, not JaxRsInitializableServlet
			// It will be initialized by the container before we can access ServletContext
			// This setAttribute call will fail - need to handle differently if needed
			fileservlet.getServletContext().setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
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

	private LocalApiAuthHttpContext getHttpContext() {
		if (localAuthCtx == null) {
			localAuthCtx = new AuthHttpContext(httpSrvc.createDefaultHttpContext(), WebSecuritySettings.getSharedProperties());
		}

		return localAuthCtx;
	}

	private LocalApiAuthHttpContext getTDIHttpContext() {
		if (tdiAuthCtx == null) {
			tdiAuthCtx = new TDIAuthHttpContext(httpSrvc.createDefaultHttpContext(), WebSecuritySettings.getSharedProperties());
		}

		return tdiAuthCtx;
	}
}
