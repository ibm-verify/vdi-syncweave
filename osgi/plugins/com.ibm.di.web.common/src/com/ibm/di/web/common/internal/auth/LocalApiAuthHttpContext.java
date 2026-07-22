/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Dictionary;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.api.DIException;
import com.ibm.di.api.connection.IServerAPIConnection;
import com.ibm.di.api.connection.IServerAPIConnectionService;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;

/**
 * Context for the OSGi HTTP Service which supports HTTP basic authentication
 * and authenticates clients against the local Server API.
 * <p>
 * Configuration is performed in a couple of ways:
 * <ul>
 * <li>Default - When created using the default constructor (e.g. by registering
 * it using the "org.eclipse.equinox.http.registry.httpcontexts" extension
 * point) configuration is taken from System Properties</li>
 * <li>Custom - When created using the overloaded constructor (e.g. by
 * registering it using the OSGi {@link HttpService} method) configuration is
 * taken from the passed in properties</li>
 * </ul>
 * <p>
 * If configuration enables BASIC authentication (i.e. property
 * {@link #PROP_AUTH} has been set to "true"), a successfully authenticated user
 * will have an {@link HttpSession} with the following attributes set:
 * <ul>
 * <li>{@link IServerAPIConnection} - the connection to the TDI API</li>
 * <li> {@link Session} - the session established during authentication against
 * the TDI API</li>
 * </ul>
 * 
 * 
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class LocalApiAuthHttpContext implements HttpContext {

	public static final L10N L10N = L10NFactory.getInstance(LocalApiAuthHttpContext.class);

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(LocalApiAuthHttpContext.class);

	/**
	 * HTTP authentication response header (see
	 * http://tools.ietf.org/html/rfc2617#section-3.2.1).
	 */
	private static final String HTTP_HEADER_AUTHENTICATE = "WWW-Authenticate";

	/**
	 * HTTP authentication request header (see
	 * http://tools.ietf.org/html/rfc2617#section-3.2.2).
	 */
	private static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

	/**
	 * Basic authentication scheme (see
	 * http://tools.ietf.org/html/rfc2617#section-2).
	 */
	private static final String AUTH_BASIC = "Basic";

	/**
	 * Default authentication realm.
	 */
	public static final String DEFAULT_AUTH_REALM = "Security Verify Directory Integrator";

	/**
	 * Configuration property which specifies the authentication realm. If
	 * missing the default value of {@link #DEFAULT_AUTH_REALM} will be used.
	 */
	public static final String PROP_AUTH_REALM = "com.ibm.di.http.context.auth.realm";

	/**
	 * Configuration property specifying whether to enable/disable Basic
	 * Authentication. If value results to "false" this context's security check
	 * will always succeed.
	 */
	public static final String PROP_AUTH = "com.ibm.di.http.context.auth";

	/**
	 * A key which boolean value specifies whether the newly created
	 * {@link Session} created during authentication will be attached to the
	 * {@link HttpSession} object.
	 */
	public static final String PROP_ATTACH_SESSION = "com.ibm.di.http.context.attach.session";

	/**
	 * Authentication enabled/disabled.
	 */
	private final boolean authEnabled;

	/**
	 * Authentication realm.
	 */
	private final String authRealm;

	/**
	 * Default HTTP context.
	 */
	private final HttpContext defaultHttpContext;

	private final boolean attachSession;

	private IServerAPIConnectionService apiSrvc;

	/**
	 * The maximum age of a session, in seconds.
	 */

	private Integer maxAge = 3 * 60;

	private final static String maxAgeProperty = "sdi.maxAge";
	public final static String PROP_DASHBOARD_AUTH_MAX_AGE = "dashboard.auth.max.age";

	/**
	 * Creates a default context which configuration will be taken from the
	 * System Properties.
	 */
	public LocalApiAuthHttpContext() {
		this(null, System.getProperties());
	}

	/**
	 * Creates a default context which configuration will be taken from the
	 * properties.
	 * 
	 * @param props
	 *            - the configuration of this context
	 * 
	 */
	public LocalApiAuthHttpContext(Dictionary<Object, Object> props) {
		this(null, props);
	}

	/**
	 * @param defaultHttpContext
	 *            Default HTTP context. The current context will delegate to the
	 *            default context when need to resolve mime types or resources,
	 *            but will not delegate security check. May be <code>null</code>
	 *            .
	 * @param props
	 *            Configuration properties (e.g. {@link #PROP_AUTH_REALM} ).
	 * 
	 */
	public LocalApiAuthHttpContext(HttpContext defaultHttpContext, Dictionary<Object, Object> props) {
		this.defaultHttpContext = defaultHttpContext;

		Object propVal = props.get(PROP_AUTH);
		this.authEnabled = Boolean.parseBoolean(propVal != null ? propVal.toString() : null);

		propVal = props.get(PROP_AUTH_REALM);
		this.authRealm = propVal == null || ((String) (propVal = propVal.toString().trim())).length() == 0 ? DEFAULT_AUTH_REALM
				: (String) propVal;

		propVal = props.get(PROP_ATTACH_SESSION);
		this.attachSession = Boolean.parseBoolean(propVal != null ? propVal.toString() : null);

		try {
			propVal = props.get(PROP_DASHBOARD_AUTH_MAX_AGE);
			if(propVal != null) {
				maxAge = Integer.valueOf(propVal.toString());
			}			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMimeType(String name) {
		return defaultHttpContext != null ? defaultHttpContext.getMimeType(name) : null;
	}

	/**
	 * @return null
	 */
	public URL getResource(String name) {
		return defaultHttpContext != null ? defaultHttpContext.getResource(name) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!authEnabled) {
			return true;
		}

		HttpSession session = request.getSession(false);

		if (session != null && !session.isNew() && !sessionHasExpired(session)) {
			/*
			 * to improve performance, automatically authenticate existing
			 * servlet sessions
			 */
			return true;
		}

		String username = null;
		boolean result;
		try {
			final String[] credentials = parseCredentials(request);
			username = credentials[0];
			final String password = credentials[1];
			result = authenticate(session, username, password);
		} catch (Exception e) {
			result = false;
		}

		if (result) {
			request.setAttribute(HttpContext.REMOTE_USER, username);
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, "BASIC");
		} else {
			invalidateSession(response, session);

			askForCredentials(response);
		}

		return result;
	}

	/**
	 * Parse authentication credentials out of an HTTP request.
	 * 
	 * @param request
	 *            Servlet request object.
	 * @return An array with first element user name and second element
	 *         password.
	 * @throws IllegalArgumentException
	 *             The authentication data is invalid.
	 * @throws UnsupportedOperationException
	 *             The authentication mechanism is not supported.
	 * @throws UnsupportedEncodingException
	 *             The authentication data cannot be decoded.
	 */
	protected String[] parseCredentials(HttpServletRequest request) throws IllegalArgumentException, UnsupportedOperationException,
			UnsupportedEncodingException {

		String authHeader = request.getHeader(HTTP_HEADER_AUTHORIZATION);
		if (authHeader == null || authHeader.length() == 0) {
			throw new IllegalArgumentException();
		}

		authHeader = authHeader.trim();
		int spaceIndex = authHeader.indexOf(' ');
		if (spaceIndex < 0) {
			throw new IllegalArgumentException(L10N.getString("TP.SERVER.HTTP.CONTEXT.MISSING.AUTHORIZATION.HEADER"));
		}

		String authScheme = authHeader.substring(0, spaceIndex);
		String authData = authHeader.substring(spaceIndex).trim();
		// verify the authentication scheme
		if (!AUTH_BASIC.equalsIgnoreCase(authScheme)) {
			throw new UnsupportedOperationException(L10N.getString("TP.SERVER.HTTP.CONTEXT.UNSUPPORTED.AUTH.SCHEME"));
		}

		// base64-decode and split the credentials token
		byte[] authBytes = UserFunctions.base64Decode(authData);
		authData = new String(authBytes, "UTF-8");
		int colonIndex = authData.indexOf(':');
		if (colonIndex < 0) {
			throw new IllegalArgumentException(L10N.getString("TP.SERVER.HTTP.CONTEXT.AUTH.HEADER.INVALID.SYNTAX"));
		}

		String username = authData.substring(0, colonIndex);
		String password = authData.substring(colonIndex + 1);

		return new String[] { username, password };
	}

	/**
	 * Send an HTTP response requesting authentication from the client.
	 * 
	 * @param response
	 *            Servlet response object.
	 * @throws IOException
	 *             Error reported by the servlet API.
	 */
	protected void askForCredentials(HttpServletResponse response) throws IOException {

		/*
		 * We no longer set the www-authenticate header because if this is set
		 * we lose the ability to perform session timeouts.  We shouldn't be
		 * using this anyway because the Web applications all use forms based
		 * authentication and not BA.
		 */

//		response.setHeader(HTTP_HEADER_AUTHENTICATE, AUTH_BASIC + " realm=\"" + authRealm + "\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	/**
	 * Authenticate a user against the local Server API.
	 * 
	 * @param session
	 *            The session object to attach any data on successful
	 *            authentication.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
	 * @return Whether the user is successfully authenticated.
	 */
	protected boolean authenticate(HttpSession session, String username, String password) {
		boolean result = false;

		IServerAPIConnectionService connSrvc;
		synchronized (this) {
			connSrvc = apiSrvc;
		}

		if (connSrvc == null) {
			throw new RuntimeException(new ServiceUnavailableException(IServerAPIConnectionService.class.getCanonicalName()));
		}

		try {
			IServerAPIConnection localConn = connSrvc.getConnection();
			SessionFactory sf = localConn.getSessionFactory();
			Session sess = sf.createSession(username, password);

			if (attachSession) {
				session.setAttribute(IServerAPIConnection.class.getCanonicalName(), localConn);
				session.setAttribute(Session.class.getCanonicalName(), sess);
			}
			result = true;
		} catch (DIException de) {
			log.info(L10N.getString("TP.SERVER.HTTP.CONTEXT.AUTH.FAILED.FOR.USER", new Object[] { username, de.getMessage() }), de);
		} catch (RemoteException e) {
			log.info(L10N.getString("TP.SERVER.HTTP.CONTEXT.AUTH.FAILED.FOR.USER", new Object[] { username, e.getMessage() }), e);
		} catch (NotBoundException e) {
			log.info(L10N.getString("TP.SERVER.HTTP.CONTEXT.AUTH.FAILED.FOR.USER", new Object[] { username, e.getMessage() }), e);
		}
		return result;
	}

	/**
	 * Wires this instance with the {@link IServerAPIConnectionService} that
	 * will be used for establishing local connections. This operation is thread
	 * safe.
	 * 
	 * @param srvc
	 */
	public synchronized void setServerAPIConnectionService(IServerAPIConnectionService srvc) {
		apiSrvc = srvc;
	}

	/**
	 * Clear the maxAge property from the session.
	 *
	 * @param session 
	 *          The session.
	 */

	protected void clearMaxAge(HttpSession session) {
		session.removeAttribute(maxAgeProperty);
	}

	/**
	 * Set the maximum age for the session.
	 *
	 * @param session 
	 *          The session.
	 */

	protected void setMaxAge(HttpSession session) {
		session.setAttribute(maxAgeProperty, 
				new Long(java.time.Instant.now().getEpochSecond() + maxAge));
	}

	/**
	 * Determine if the session has expired or not.
	 *
	 * @param session 
	 *          The session.
	 */

	protected boolean sessionHasExpired(HttpSession session) {
		boolean expired   = false;
		Object  maxAgeObj = session.getAttribute(maxAgeProperty);

		if (maxAgeObj == null) {
			setMaxAge(session);
		} else {
			expired = ((Long)maxAgeObj) < java.time.Instant.now().getEpochSecond();
		}

		return expired;
	}

	/**
	 * Clear the session cookie in the response.
	 *
	 * @param response
	 *        The response handle.
	 */

	protected void clearSessionCookie(HttpServletResponse response) {
		// Clear the session cookie.
		Cookie cookie = new Cookie("SDISessionID", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	/**
	 * Invalidate the session and clear the session cookie.
	 *
	 * @param response
	 *        The response handle.
	 * @param session
	 *        The session.
	 */

	protected void invalidateSession(HttpServletResponse response, HttpSession session) {
		if (session != null) {
			clearSessionCookie(response);

			session.setAttribute(maxAgeProperty, new Long(0));
		}
	}

}
