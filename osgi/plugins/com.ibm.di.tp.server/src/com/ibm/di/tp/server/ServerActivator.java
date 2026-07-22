/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;
import com.ibm.di.schema.internal.SchemaProvider;
import com.ibm.di.schema.internal.SchemaRewriter;
import com.ibm.di.tp.server.util.SchemaRewriterAccessor;
import com.ibm.di.web.common.internal.auth.LocalApiAuthHttpContext;
import com.ibm.di.web.common.internal.jaxrs.JaxRsInitializableServlet;
import com.ibm.di.web.common.internal.wink.AtomServiceDocEnabler;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * This is the entry point of the Touchpoint Server OSGi bundle. It registers
 * the REST servlet in the OSGi HTTP Service. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ServerActivator implements SchemaProvider {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private LocalApiAuthHttpContext localAuthCtx;

	private HttpService httpSrvc;

	private IServerAPIConnectionService apiSrvc;

	private HttpServlet servlet;

	public static final L10N L10N = L10NFactory.getInstance(ServerActivator.class);

	private static Properties ctxConf;

	private static SchemaRewriterAccessor access = new SchemaRewriterAccessor();

	@SuppressWarnings("unused")
	private synchronized void activate(ComponentContext cc) throws ServletException, NamespaceException {
		HttpContext ctx = getHttpContext();

		ResourceConfig rc = ResourceConfig.forApplication(new TPServerApplication()).register(AtomServiceDocEnabler.class);

		servlet = new JaxRsInitializableServlet(rc) {
			private static final long serialVersionUID = -8157309898995145685L;

			@Override
			public void init(ServletConfig config) throws ServletException {
				// both httpSrvc and apiSrvc are must-haves in order to get
				// activated
				config.getServletContext().setAttribute(IServerAPIConnectionService.class.getCanonicalName(), apiSrvc);
				config.getServletContext().setAttribute(SchemaRewriterAccessor.class.getName(), access);
				super.init(config);
			}
		};

		String tpContextRoot = System.getProperty("tp.server.contextRoot", "/tp");
		httpSrvc.registerServlet(tpContextRoot, servlet, null, ctx);
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

	private LocalApiAuthHttpContext getHttpContext() {
		if (localAuthCtx == null) {
			localAuthCtx = new LocalApiAuthHttpContext(httpSrvc.createDefaultHttpContext(), getProperties());
		}

		return localAuthCtx;
	}

	private static synchronized Properties getProperties() {
		if (ctxConf == null) {
			ctxConf = new Properties();
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH, System.getProperty("tp.server.auth", "false"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_AUTH_REALM, System.getProperty("tp.server.auth.realm",
					"Security Verify Directory Integrator Touchpoint Server"));
			ctxConf.setProperty(LocalApiAuthHttpContext.PROP_ATTACH_SESSION, "false");
		}
		return ctxConf;
	}

	@SuppressWarnings("unused")
	private void bind(SchemaRewriter schema) {
		access.setSchemaRewriter(schema);
	}

	@SuppressWarnings("unused")
	private synchronized void unbind(SchemaRewriter schema) {
		access.setSchemaRewriter(null);
	}

	public String getContextDir() {
		return Constants.SCHEMA_CONTEXT_DIR;
	}

	public InputStream getSchema(String fileName) throws IOException {
		URL url = getResourceUrl(fileName);
		return url != null ? url.openStream() : null;
	}

	private URL getResourceUrl(String fileName) {
		if (fileName == null) {
			throw new NullPointerException();
		}
		final Class<?> clazz = ServerActivator.class;
		String schemaPath = "schema/" + fileName;
		URL schemaLocation = clazz.getResource(schemaPath);
		if (schemaLocation == null) {
			schemaLocation = clazz.getResource("/" + schemaPath);
		}
		return schemaLocation;
	}

	public boolean schemaFileExists(String fileName) {
		return getResourceUrl(fileName) != null;
	}
}
