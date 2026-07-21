/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * (C) Copyright IBM Corporation. 2009, 2011
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner
 * @history
 */
package com.ibm.di.tp.server.smash.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import zero.core.context.GlobalContext;

/**
 * This class adapts the request data in the sMash {@link GlobalContext} to the
 * Servlet API {@link ServletRequest}. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SmashRequestAdapter implements HttpServletRequest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	private final HttpServlet servlet;
	private final Cookie[] cookies;
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	private final ServletInputStream sis = new InputStreamDelegate((InputStream) GlobalContext.zget("/request/input"));

	private String charEnc;
	private BufferedReader reader;

	public SmashRequestAdapter(HttpServlet servlet) {
		this.servlet = servlet;

		// get the cookies of this request
		Map<String, List<zero.core.cookie.Cookie>> zcookieMap = GlobalContext.zget("/request/cookies/in");
		if (zcookieMap != null && zcookieMap.size() > 0) {
			List<Cookie> cList = new LinkedList<Cookie>();
			for (Map.Entry<String, List<zero.core.cookie.Cookie>> zentry : zcookieMap.entrySet()) {
				if (zentry.getValue() != null) {
					for (zero.core.cookie.Cookie zcookie : zentry.getValue()) {
						Cookie cookie = new Cookie(zcookie.getName(), zcookie.getValue());
						if (zcookie.getDomain() != null) {
							cookie.setDomain(zcookie.getDomain());
						}
						cookie.setSecure(zcookie.getSecure());
						if (zcookie.getPath() != null) {
							cookie.setPath(zcookie.getPath());
						}
						if (zcookie.getComment() != null) {
							cookie.setComment(zcookie.getComment());
						}
						cookie.setMaxAge(zcookie.getMaxAge());
						cookie.setVersion(zcookie.getVersion());
						cList.add(cookie);
					}
				}
			}
			cookies = cList.toArray(new Cookie[cList.size()]);
		} else {
			cookies = new Cookie[0];
		}

		// get the character encoding of the request
		String type = getHeader("Content-Type");
		if (type != null) {
			int start = type.indexOf("charset");
			if (start > -1) {
				start += "charset=".length();
				if (start < type.length()) {
					int end = type.indexOf(";");
					end = end == -1 ? type.length() : end;
					charEnc = type.substring(start, end);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		servlet.getServletContext().log("HttpServletRequest.getAuthType() is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		return servlet.getServletContext().getContextPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return cookies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String name) {
		String zHeader = GlobalContext.zget("/request/headers/in/" + name);
		if (zHeader != null) {
			try {
				return Long.parseLong(zHeader);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(nfe);
			}
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {
		return GlobalContext.zget("/request/headers/in/" + name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getHeaderNames() {
		return Collections.enumeration(GlobalContext.zlist("/request/headers/in", false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getHeaders(String name) {
		String header = getHeader(name);
		List<String> result = new LinkedList<String>();

		if (header != null && header.equals(name)) {
			String[] vals = COMMA_PATTERN.split(header);
			Collections.addAll(result, vals);
		}

		return Collections.enumeration(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String name) {
		String zHeader = GlobalContext.zget("/request/headers/in/" + name);
		if (zHeader != null) {
			try {
				return Integer.parseInt(zHeader);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(nfe);
			}
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return GlobalContext.zget("/request/method");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		return GlobalContext.zget("/event/pathInfo");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		servlet.getServletContext().log("HttpServletRequest.getPathTranslated() is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		return GlobalContext.zget("/request/queryString");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		return GlobalContext.zget("/request/subject/remoteUser");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		String uri = GlobalContext.zget("/request/uri");
		String qs = null;
		return uri.substring(0, uri.length() - ((qs = getQueryString()) != null ? qs.length() + 1 : 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		StringBuffer sb = new StringBuffer();
		sb.append((String) GlobalContext.zget("/request/scheme"));
		sb.append("://");
		sb.append((String) GlobalContext.zget("/request/serverName"));
		sb.append(":");
		sb.append((String) GlobalContext.zget("/request/serverPort"));
		sb.append(getRequestURI());

		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return GlobalContext.zget("/user/zsessionid");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		servlet.getServletContext().log("HttpServletRequest.getSession(" + create + ") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		servlet.getServletContext().log("HttpServletRequest.getUserPrincipal() is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		servlet.getServletContext().log("HttpServletRequest.isRequestedSessionIdFromCookie() is not implemented!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		servlet.getServletContext().log("HttpServletRequest.isRequestedSessionIdFromURL() is not implemented!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		servlet.getServletContext().log("HttpServletRequest.isRequestedSessionIdFromUrl() is deprecated!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		servlet.getServletContext().log("HttpServletRequest.isRequestedSessionIdValid() is not implemented!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String role) {
		servlet.getServletContext().log("HttpServletRequest.isUserInRole(\"" + role + "\") is not implemented!");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return charEnc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		return getIntHeader("Content-Length");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		return getHeader("Content-Type");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		return sis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName((String) GlobalContext.zget("/request/serverName"));
		} catch (UnknownHostException e) {
			try {
				addr = InetAddress.getByName(null);
			} catch (UnknownHostException e1) {
			}
		}

		return addr != null ? addr.getHostAddress() : InetAddress.getLocalHost().getHostAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName((String) GlobalContext.zget("/request/serverName"));
		} catch (UnknownHostException e) {
			try {
				addr = InetAddress.getByName(null);
			} catch (UnknownHostException e1) {
			}
		}

		return addr != null ? addr.getHostName() : "localhost";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		return GlobalContext.zget("/request/serverPort");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		String country = GlobalContext.zget("/request/locales#0/country");
		String language = GlobalContext.zget("/request/locales#0/language");

		Locale result = null;
		if (country != null && language != null) {
			result = new Locale(language, country);
		} else if (language != null) {
			result = new Locale(language);
		} else {
			result = Locale.getDefault();
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getLocales() {
		List<Locale> locs = new LinkedList<Locale>();

		List<Map<String, String>> zlocs = GlobalContext.zget("/request/locales");

		for (Map<String, String> zloc : zlocs) {
			String country = zloc.get("country");
			String language = zloc.get("language");

			if (country != null && language != null) {
				locs.add(new Locale(language, country));
			} else if (language != null) {
				locs.add(new Locale(language));
			}
		}

		return Collections.enumeration(locs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		List<String> vals = GlobalContext.zget("/request/params/" + name, null);
		return vals != null ? vals.get(0) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	@SuppressWarnings("unchecked")
	public Map getParameterMap() {
		Enumeration en = getParameterNames();
		Map<String, String[]> map = new HashMap<String, String[]>();

		String param = null;
		while (en.hasMoreElements()) {
			param = (String) en.nextElement();
			map.put(param, getParameterValues(param));
		}

		return Collections.unmodifiableMap(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	@SuppressWarnings("unchecked")
	public Enumeration getParameterNames() {
		List<String> params = GlobalContext.zlist("/request/params", false);
		return params != null ? Collections.enumeration(params) : Collections.enumeration(Collections.emptyList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		List<String> vals = GlobalContext.zget("/request/params/" + name, null);
		return vals != null ? vals.toArray(new String[vals.size()]) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		return GlobalContext.zget("/request/protocol");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		if (reader == null) {
			String enc = getCharacterEncoding();
			reader = new BufferedReader(enc == null ? new InputStreamReader(sis) : new InputStreamReader(sis, enc));
		}
		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String path) {
		servlet.getServletContext().log("HttpServletRequest.getRealPath(\"" + path + "\") is deprecated!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		return GlobalContext.zget("/request/remoteAddress");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		return GlobalContext.zget("/request/remoteHost");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		return GlobalContext.zget("/request/remotePort");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String path) {
		servlet.getServletContext().log("HttpServletRequest.getRequestDispatcher(\"" + path + "\") is not implemented!");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		return GlobalContext.zget("/request/scheme");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		return GlobalContext.zget("/request/serverName");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return GlobalContext.zget("/request/serverPort");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		return "https".equalsIgnoreCase(getScheme());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		charEnc = env;
	}

	private static final class InputStreamDelegate extends ServletInputStream {

		private final InputStream delegate;

		public InputStreamDelegate(InputStream delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return delegate.read(b, off, len);
		}
	}
}
