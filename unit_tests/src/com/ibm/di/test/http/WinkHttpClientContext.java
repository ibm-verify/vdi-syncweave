package com.ibm.di.test.http;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.ibm.di.web.common.internal.jaxrs.JaxRsInitializableServlet;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class WinkHttpClientContext extends HttpClientContext {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** this is the uri that the wink code will generate */
	public static final String CONTEXT_ROOT_USED_BY_WINK = "http://localhost:80";

	protected HttpServlet servlet;
	protected MockServletConfig servletConfig = new MockServletConfig();

	private Class<? extends Application> app;

	private Class<? extends ResourceConfig> dep;

	public WinkHttpClientContext() {
	}

	public WinkHttpClientContext(Class<? extends Application> app, Class<? extends ResourceConfig> dep) {
		this.app = app;
		this.dep = dep;
	}

	public HttpServlet getServlet() {
		return new JaxRsInitializableServlet(getApplicationClass());
	}

	@Before
	public void setUp() throws Exception {
		servlet = getServlet();
		if (isAutoInit()) {
			initContext();
		}
	}

	protected void preInitHook() {
	}

	protected void postInitHook() {
	}

	/**
	 * Passes the test to the servlet instance simulating AS behaviour.
	 * 
	 * @param request
	 *            the filled request
	 * @return a new response as filled by the servlet
	 * @throws IOException
	 * @throws ServletException
	 */
	public MockHttpServletResponse invoke(MockHttpServletRequest request) throws ServletException, IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();

		preInvokeHook();
		servlet.service(request, response);
		postInvokeHook();

		return response;
	}

	protected void preInvokeHook() {
	}

	protected void postInvokeHook() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.WinkTestBase#getApplicationClassName()
	 */
	protected Class<? extends Application> getApplicationClass() {
		return app;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.WinkTestBase#getApplicationClassName()
	 */
	protected Class<? extends ResourceConfig> getDeploymentConfigurationClass() {
		return dep;
	}

	protected boolean isAutoInit() {
		return true;
	}

	protected void initContext() throws ServletException {
		preInitHook();
		// if (getApplicationClass() != null) {
		// servletConfig.addInitParameter("javax.ws.rs.Application",
		// getApplicationClass().getCanonicalName());
		// }
		servlet.init(servletConfig);
		postInitHook();
	}

	private static String getUriPath(String requestURI) {
		URI reqUri = URI.create(requestURI);
		if (reqUri.isAbsolute()) {
			requestURI = reqUri.getRawPath();
		}
		return requestURI;
	}

	@Override
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader) {
		// make sure the URL is relative so the wink code can work.
		return super.constructMockRequest(method, getUriPath(requestURI), acceptHeader, servletConfig.getServletContext());
	}

	@Override
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, ServletContext ctx) {
		// make sure the URL is relative so the wink code can work.
		return super.constructMockRequest(method, getUriPath(requestURI), acceptHeader, ctx);
	}

	@Override
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, String contentType,
			byte[] content) {
		return super.constructMockRequest(method, getUriPath(requestURI), acceptHeader, contentType, content, servletConfig
				.getServletContext());
	}

	@Override
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, String contentType,
			byte[] content, ServletContext ctx) {
		return super.constructMockRequest(method, getUriPath(requestURI), acceptHeader, contentType, content, ctx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.test.http.HttpClientContext#getHttpRootURI()
	 */
	@Override
	public String getHttpRootURI() {
		return "/";
	}
}
