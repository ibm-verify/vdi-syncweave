/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Base64;

import com.ibm.di.connector.maximo.core.MxConnConfiguration;
import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnConfigException;
import com.ibm.di.connector.maximo.exception.MxConnHttpException;
import com.ibm.di.connector.maximo.exception.MxConnIOException;
import com.ibm.di.connector.maximo.exception.MxConnTimeoutException;
import com.ibm.di.server.Log;
import com.ibm.di.util.StringUtils;

/**
 * HTTP client used for request/response communication.
 * <p>
 * This HTTP client will try to communicate with one of the HTTP servers
 * provided by target URL list.
 * </p>
 * 
 * @since 7.1
 */
public final class HttpClient {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String UTF8_CHARSET = "UTF-8";

	private boolean authRequired = false;

	private String password;

	private final List<String> targetUrlList = new LinkedList<String>();

	private int timeout;

	private String userId;

	private boolean xmlCharValidationEnabled = false;

	private Log logger;

	public HttpClient(Log log) {
		super();
		logger = log;
	}

	private static HttpURLConnection buildConnection(final String targetURL) throws IOException {
		final URL url = new URL(targetURL);
		return (HttpURLConnection) url.openConnection();
	}

	private static String encode(final String value) throws UnsupportedEncodingException {
		return Base64.getEncoder().encodeToString(value.getBytes(UTF8_CHARSET));
	}

	private static boolean isValidXmlChar(final int c) {

		if ((c >= '\u0020' && c <= '\uD7FF') || (c >= '\uE000' && c <= '\uFFFD') || (c == '\t' || c == '\n' || c == '\r')) {
			return true;
		}
		return false;
	}

	/**
	 * Requests the resource specified by the target URL list as an input
	 * stream.
	 * 
	 * @return input stream to the requested resource
	 * @throws MxConnConfigException
	 *             if no target URL list is defined
	 * @throws MxConnIOException
	 *             if any communication problem occurs
	 * @see #setTargetUrlList(List)
	 * @see #setAuthenticationRequired(boolean)
	 * @see #setTimeout(int)
	 * @since 1.4.0
	 */
	public InputStream getAsInputStream() throws MxConnConfigException, MxConnIOException {
		try {
			final String response = post(null);
			return new ByteArrayInputStream(response.getBytes(UTF8_CHARSET));
		} catch (final UnsupportedEncodingException e) {
			throw new MxConnIOException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.UNSUPPORTED.ENCODING", UTF8_CHARSET), null,
					e, UTF8_CHARSET);
		}
	}

	/**
	 * Requests the resource specified by the target URL list. If no message is
	 * provided, the request method used is GET, otherwise the request method is
	 * POST.
	 * 
	 * @param msg
	 *            message to be posted
	 * @return response returned
	 * @throws MxConnConfigException
	 *             if no target URL list is defined
	 * @throws MxConnIOException
	 *             if any communication problem occurs
	 * @see #setTargetUrlList(List)
	 * @see #setAuthenticationRequired(boolean)
	 * @see #setTimeout(int)
	 */
	public String post(final String msg) throws MxConnConfigException, MxConnIOException {

		checkParams();

		String response = null;
		for (int i = 0; i < targetUrlList.size(); i++) {
			try {
				logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.TARGET.URL", targetUrlList.get(i)));
				response = post(targetUrlList.get(i), msg);
				break;
			} catch (final MxConnIOException e) {
				// if we have tried the last URL, throw exception
				if (i == targetUrlList.size() - 1) {
					throw e;
				}
				logger.warn(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.SEND.REQUEST", e));
			}
		}

		return response;
	}

	/**
	 * Indicates if the authentication header containing the user's credentials
	 * should be sent.
	 * 
	 * @param isRequired
	 *            <code>true</code> if the authentication header should be sent,
	 *            otherwise <code>false</code>
	 * @see #setUserId(String)
	 * @see #setPassword(String)
	 */
	public void setAuthenticationRequired(final boolean isRequired) {
		authRequired = isRequired;
	}

	/**
	 * Defines the user's password.
	 * 
	 * @param pass
	 *            user's password
	 */
	public void setPassword(final String pass) {
		password = pass;
	}

	/**
	 * Defines the list of HTTP server's target URLs to which resources will be
	 * requested.
	 * 
	 * @param targetUrlList
	 *            list of HTTP server's target URLs
	 */
	public void setTargetUrlList(final List<String> targetUrlList) {
		this.targetUrlList.clear();
		this.targetUrlList.addAll(targetUrlList);
	}

	public List<String> getTargetUrlList() {
		return targetUrlList;
	}

	/**
	 * Defines the timeout, in milliseconds. This is used when communicating
	 * with the HTTP server. If the timeout expires before the connection can be
	 * established or before there is data available for read, a
	 * {@link MxConnTimeoutException} is raised. A timeout of zero is
	 * interpreted as infinite timeout.
	 * 
	 * @param timeout
	 */
	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Defines the user's identification.
	 * 
	 * @param userId
	 *            user's identification
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Defines if the response message should be validated against invalid XML
	 * characters. If enabled, all invalid characters will be removed from the
	 * request/response message.
	 * 
	 * @param enabled
	 *            <code>true</code> if the message should be validated against
	 *            invalid XML characters, <code>false</code> otherwise
	 */
	public void setXmlCharValidationEnabled(final boolean enabled) {
		xmlCharValidationEnabled = enabled;
	}

	private void checkParams() {
		if (targetUrlList.isEmpty()) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.TARGET.URL.LIST.IS.EMPTY"));
		}

		if (authRequired) {
			checkParam(MxConnConfiguration.PARAM_USER_ID, userId);
			checkParam(MxConnConfiguration.PARAM_PASSWORD, password);
		}

		if (timeout < 0) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.INVALID.TIMEOUT.VALUE"));
		}
	}

	private void checkParam(final String paramName, final String paramValue) {
		if (StringUtils.isBlank(paramValue)) {
			throw new MxConnConfigException(SimpleTpaeIFConnector.getResHash()
					.getString("MXCONN.HTTPCLIENT.PARAM.NOT.DEFINED", paramName));
		}
	}

	private void configureGet(final HttpURLConnection conn) throws ProtocolException {
		conn.setDoInput(true);
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
	}

	private void configurePost(final HttpURLConnection conn, final String msg) throws ProtocolException {
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Length", String.valueOf(msg.length()));
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
	}

	private String post(final String url, final String msg) throws MxConnIOException {
		try {
			final HttpURLConnection conn = buildConnection(url);

			if (msg == null) {
				configureGet(conn);
			} else {
				configurePost(conn, msg);
			}

			if (authRequired) {
				setAuthenticationHeader(conn);
			}

			if (msg != null) {
				send(conn, msg);
			}

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new MxConnHttpException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.HTTP.RESPONSE.CODE.NOT.OK"), url, conn
						.getResponseCode(), conn.getResponseMessage(), receiveError(conn));
			}

			return receive(conn);
		} catch (final SocketTimeoutException e) {
			throw new MxConnTimeoutException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.SOCKET.TIMEOUT"), url, timeout, e);
		} catch (final IOException e) {
			throw new MxConnIOException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.SEND.HTTP.REQUEST"), url, e, url);
		}
	}

	private String receive(final HttpURLConnection conn) throws IOException {
		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.RECEIVING.MSG"));

		final InputStreamReader in = new InputStreamReader(conn.getInputStream(), UTF8_CHARSET);
		final StringWriter response = new StringWriter();

		try {
			if (xmlCharValidationEnabled) {
				logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.XML.VALIDATION.ENABLED"));
			}
			for (int i = in.read(); i != -1; i = in.read()) {
				if (!xmlCharValidationEnabled || isValidXmlChar((char) i)) {
					response.append((char) i);
				}
			} 
		} finally {
			in.close();
		}

		return response.toString();
	}

	private String receiveError(final HttpURLConnection conn) throws IOException {

		final InputStream es = conn.getErrorStream();
		if (es == null) {
			return "";
		}

		final String lineSep = System.getProperty("line.separator");
		final String encodingCharset = getEncoding(conn.getContentType());
		final StringBuilder response = new StringBuilder();
		final InputStreamReader inr = new InputStreamReader(es, encodingCharset);

		try {
			for (int i = inr.read(); i != -1; i = inr.read()) {
				response.append((char) i);
			}
		} catch (final IOException e) {
			logger.warn(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.READ.ERROR.MESSAGE", e));
		} finally {
			es.close();
			inr.close();
		}

		return response.toString().replace(lineSep, "");
	}

	private void send(final HttpURLConnection conn, final String msg) throws IOException {

		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.SENDING.MESSAGE", msg));

		String message = msg;
		final OutputStream out = conn.getOutputStream();
		if (xmlCharValidationEnabled) {
			logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.XML.VALIDATION.ENABLED"));

			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < msg.length(); i++) {
				if (isValidXmlChar(msg.charAt(i))) {
					sb.append(msg.charAt(i));
				}
			}
			message = sb.toString();
			logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.XML.MESSAGE.AFTER.VALIDATION", message));
		}
		out.write(message.getBytes(UTF8_CHARSET));
		out.flush();
	}

	private void setAuthenticationHeader(final HttpURLConnection conn) throws UnsupportedEncodingException {
		String encoded = encode(userId + ":" + password);
		conn.setRequestProperty("Authorization", "Basic " + encoded);
		conn.setRequestProperty("MAXAUTH", encoded);
		conn.setRequestProperty("content-type", "application/xml");
		conn.setRequestProperty("Accept", "application/xml");
	}

	/**
	 * Split HTTP encoding part and return just encoding charset.
	 * <p>
	 * Example: <code>&quot;text/html;charset=ISO-8859-1&quot;</code> will
	 * return <code>&quot;ISO-8859-1&quot;</code>.
	 * 
	 * @param httpEncoding
	 *            HTTP encoding part
	 * @return just encoding charset
	 */
	private String getEncoding(String httpEncoding) {
		String[] entries = null;
		String result = UTF8_CHARSET;

		// Splits entry into two pieces and get just the value,after "="
		if (httpEncoding != null) {
			entries = httpEncoding.split("=");
			if (entries.length >= 2) {
				result = entries[1].trim();
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(HttpClient.class.getName());
		sb.append('{');
		sb.append(MxConnConfiguration.PARAM_AUTHENTICATION_REQUIRED + "=").append(authRequired).append("; ");
		sb.append(MxConnConfiguration.PARAM_USER_ID + "=").append(userId).append("; ");
		sb.append(MxConnConfiguration.PARAM_PASSWORD + "=").append(password).append("; ");
		sb.append("targetUrlList=").append(targetUrlList).append("; ");
		sb.append(MxConnConfiguration.PARAM_TIMEOUT + "=").append(timeout);
		sb.append('}');
		return sb.toString();
	}
}
