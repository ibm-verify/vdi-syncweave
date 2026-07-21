/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.schema.internal.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.ibm.di.schema.internal.SchemaProvider;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class SchemaServer extends HttpServlet {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 4932902775748042798L;

	static final String ROOT_CONTEXT = "/schema";

	private Map<String, SchemaProvider> providers = Collections.synchronizedMap(new HashMap<String, SchemaProvider>());

	private HttpService httpSrvc;

	@SuppressWarnings("unused")
	private synchronized void activate() throws ServletException, NamespaceException {
		httpSrvc.registerServlet(ROOT_CONTEXT, this, null, null);
	}

	@SuppressWarnings("unused")
	private void bind(SchemaProvider sp) {
		String contextDir = sp.getContextDir();
		if (contextDir.length() > 1) {
			if (contextDir.startsWith("/") || contextDir.endsWith("/")) {
				throw new IllegalStateException(contextDir);
			} else if (contextDir.indexOf('/') > -1) {
				throw new IllegalStateException(contextDir);
			}
		} else if (contextDir.length() == 0) {
			throw new IllegalStateException("contextDir empty");
		}

		providers.put(sp.getContextDir(), sp);
	}

	@SuppressWarnings("unused")
	private void unbind(SchemaProvider sp) {
		providers.remove(sp.getContextDir());
	}

	@SuppressWarnings("unused")
	private synchronized void bind(HttpService srvc) {
		httpSrvc = srvc;
	}

	@SuppressWarnings("unused")
	private synchronized void unbind(HttpService srvc) {
		httpSrvc = null;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		URI baseUri;
		String contextDir, filePath;
		try {
			baseUri = new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), ROOT_CONTEXT, null, null);
			URI contextDirAndFile = baseUri.relativize(new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), req
					.getRequestURI(), null, null));
			int firstSep = contextDirAndFile.getPath().indexOf('/');
			contextDir = contextDirAndFile.getPath().substring(0, firstSep);
			filePath = contextDirAndFile.getPath().substring(firstSep + 1);
			baseUri = URI.create(baseUri.toString() + "/" + contextDir).normalize();
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}

		SchemaRemoteView schemaView = null;
		SchemaProvider prov = providers.get(contextDir);
		if (prov != null) {
			InputStream schema = prov.getSchema(filePath);
			if (schema != null) {
				try {
					schemaView = new SchemaRemoteView(schema);
				} catch (Exception e) {
					throw new ServletException(e);
				}
			}
		}

		if (schemaView == null) {
			resp.setStatus(404);
		} else {
			try {
				resp.getWriter().write(schemaView.getRemoteSchemaAsString(baseUri.toString()));
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}
}
