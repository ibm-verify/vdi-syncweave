package com.ibm.di.test.http;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
public abstract class HttpClientContext {

	public abstract MockHttpServletResponse invoke(MockHttpServletRequest request) throws Exception;

	/**
	 * @return the root of the TP Server HTTP Server. E.g.
	 *         http://&lt;tp_server&gt;:&lt;tp_port&gt;/tp/ or
	 *         http://localhost/tp
	 */
	public abstract String getHttpRootURI();

	/**
	 * Construct a mock request to be used in tests.
	 * 
	 * @param method
	 *            HTTP method
	 * @param requestURI
	 *            request URI
	 * @param acceptHeader
	 *            request Accept header
	 * @return new mock request
	 */
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader) {
		return constructMockRequest(method, requestURI, acceptHeader, null);
	}

	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, ServletContext ctx) {
		MockHttpServletRequest mockRequest = new MockHttpServletRequestWrapper(ctx, method, requestURI) {

			public String getPathTranslated() {
				return null; // prevent Spring to resolve the file on the
				// filesystem which fails
			}

		};

		mockRequest.addHeader("Accept", acceptHeader);
		return mockRequest;
	}

	/**
	 * Construct a mock request to be used in tests.
	 * 
	 * @param method
	 *            HTTP method
	 * @param requestURI
	 *            request URI
	 * @param acceptHeader
	 *            request Accept header
	 * @param contentType
	 *            request Content Type
	 * @param content
	 *            request content
	 * @return new mock request
	 */
	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, String contentType,
			byte[] content) {
		return constructMockRequest(method, requestURI, acceptHeader, contentType, content, null);
	}

	public MockHttpServletRequest constructMockRequest(String method, String requestURI, String acceptHeader, String contentType,
			byte[] content, ServletContext ctx) {
		MockHttpServletRequest mockRequest = constructMockRequest(method, requestURI, acceptHeader, ctx);
		mockRequest.setContentType(contentType);
		mockRequest.setContent(content);

		return mockRequest;
	}

	public static class MockHttpServletRequestWrapper extends MockHttpServletRequest {

		private ServletInputStream inputStream = null;

		public MockHttpServletRequestWrapper() {
			super();
		}

		public MockHttpServletRequestWrapper(ServletContext ctx) {
			super(ctx);
		}

		public MockHttpServletRequestWrapper(ServletContext ctx, String method, String requestURI) {
			super(ctx, method, requestURI);
		}

		public MockHttpServletRequestWrapper(String method, String requestURI) {
			super(method, requestURI);
		}

		@Override
		public ServletInputStream getInputStream() {
			if (inputStream != null) {
				return inputStream;
			}
			inputStream = super.getInputStream();
			return inputStream;
		}

		@Override
		public void setContentType(String contentType) {
			if (getCharacterEncoding() != null && !contentType.contains("charset=")) {
				contentType += ";charset=" + getCharacterEncoding();
			}
			addHeader("Content-Type", contentType);
		}

		@Override
		public void setContent(byte[] content) {
			super.setContent(content);
			if (content != null) {
				addHeader("Content-Length", String.valueOf(content.length));
			}
		}
		
		/* (non-Javadoc)
		 * @see org.springframework.mock.web.MockHttpServletRequest#getRequestURI()
		 */
		@Override
		public String getRequestURI() {
			String val = super.getRequestURI();
			return val;
		}
	}
}
