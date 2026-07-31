/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.WSReceiverServerConnector;

import java.io.IOException;
import java.util.Locale;

import com.ibm.di.connector.WSReceiverServerConnector.BufferedNonBlockingInputStream;

import com.ibm.di.fc.webservice.axis2.WebServiceClient;
import com.ibm.di.server.ResourceHash;

/**
 * HTTPHeaders is an object that aides in the creation of a response header.
 * This version is compatible with Axis2.
 */
public class HTTPHeaders {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component name.
	 */
	private static final String PROPERTIES_FILE = "wsreceiverserverconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * MIME header for content length.Byte array.
	 */
	private static byte lenHeader[];
	/**
	 * Length of the MIME header content length array.
	 */
	private static int lenLen;
	static {
		try {
			lenHeader = "content-length: "
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
			lenLen = lenHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for content type. Byte array.
	 */
	private static byte typeHeader[];

	/**
	 * Length of the MIME header content type array.
	 */
	private static int typeLen;
	static {
		try {
			typeHeader = ("Content-Type"
					.toLowerCase(Locale.ENGLISH) + ": ")
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
			typeLen = typeHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for content location. Byte array.
	 */
	private static byte locationHeader[];

	/**
	 * Length of the MIME header location array.
	 */
	private static int locationLen;
	static {
		try {
			locationHeader = ("Content-Location"
					.toLowerCase(Locale.ENGLISH) + ": ")
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
			locationLen = locationHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for SOAP action. Byte array.
	 */
	private static byte actionHeader[];

	/**
	 * Length of the MIME headed SOAP action array.
	 */
	private static int actionLen;
	static {
		try {
			actionHeader = "soapaction: "
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
			actionLen = actionHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for cookie.
	 */
	private static byte cookieHeader[];
	static {
		try {
			cookieHeader = "cookie: ".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for cookie2. Byte array.
	 */
	private static byte cookie2Header[];
	static {
		try {
			cookie2Header = "cookie2: "
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * HTTP header for authentication. Byte array.
	 */
	private static byte authHeader[];

	/**
	 * Length of the HTTP header for authentication array
	 */
	private static int authLen;
	static {
		try {
			authHeader = "authorization: "
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
			authLen = authHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Host header. Byte array
	 */
	private static byte hostHeader[];

	/**
	 * Length of the host header array.
	 */
	private static int hostLen;
	static {
		try {
			hostHeader = "Host: ".getBytes(WebServiceClient.ENCODING_LATIN_1);
			hostLen = hostHeader.length;
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for GET. Byte array.
	 */
	private static byte getHeader[];
	static {
		try {
			getHeader = "GET".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MIME header for POST. Byte array
	 */
	private static byte postHeader[];
	static {
		try {
			postHeader = "POST".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Header ender. Byte array
	 */
	private static byte headerEnder[];
	static {
		try {
			headerEnder = ": ".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Basic authentication. Byte array
	 */
	private static byte basicAuth[];
	static {
		try {
			basicAuth = "basic ".getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Standard MIME headers for XML payload
	 */
	private static byte XML_MIME_STUFF[];
	static {
		try {
			XML_MIME_STUFF = ("\r\nContent-Type: text/xml; charset=utf-8\r\n"
					+ "Content-Length: ")
					.getBytes(WebServiceClient.ENCODING_LATIN_1);
		} catch (java.io.UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ASCII character mapping to lower case
	 */
	private static final byte[] toLower = new byte[256];

	static {
		for (int i = 0; i < 256; i++) {
			toLower[i] = (byte) i;
		}

		for (int lc = 'a'; lc <= 'z'; lc++) {
			toLower[lc + 'A' - 'a'] = (byte) lc;
		}
	}

	/**
	 * The method read information from the
	 * {@link BufferedNonBlockingInputStream} and writes it in the byte array.
	 *
	 * @param is
	 *            {@link BufferedNonBlockingInputStream}
	 * @param b
	 *            byte array
	 * @param off
	 *            offset, position to start writing in the array
	 * @param len
	 *            length , amount for reading
	 * @return int , the amount , that has been read.
	 * @throws IOException
	 *             if error occurs during reading.
	 */
	private static int readLine(BufferedNonBlockingInputStream is, byte[] b,
	 	int off, int len) throws java.io.IOException {
		int count = 0, c;

		while ((c = is.read()) != -1) {
			if (c != '\n' && c != '\r') {
				b[off++] = (byte) c;
				count++;
			}
			if (count == len)
				break;
			if ('\n' == c) {
				int peek = is.peek(); // If the next line begins with tab or
				// space then this is a continuation.
				if (peek != ' ' && peek != '\t')
					break;
			}
		}
		return count > 0 ? count : -1;
	}

	/**
	 * The method accepts two byte array and returns true if they match. Note:
	 * The first array is translated in lower cases and the second one should be
	 * in lower cases
	 * 
	 * @param buf
	 *            byte array
	 * @param target
	 *            byte array
	 * @return boolean
	 */
	public static boolean matches(byte[] buf, byte[] target) {
		for (int i = 0; i < target.length; i++) {
			if (toLower[buf[i]] != target[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The method accepts two byte array and returns true if they match. Note:
	 * Case sensitive
	 * 
	 * @param buf
	 *            byte array
	 * @param target
	 *            byte array
	 * @return boolean
	 */
	public static boolean matchesCase(byte[] buf, byte[] target) {
		for (int i = 0; i < target.length; i++) {
			if (buf[i] != target[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The method accepts two byte array and returns true if the part of the
	 * first one starting from position bufIdx matches the second one. Note: The
	 * first array is translated in lower cases and the second one should be in
	 * lower cases
	 * 
	 * @param buf
	 *            byte array
	 * @param bufIdx
	 *            int , the position , where the comparison begins
	 * @param target
	 *            byte array
	 * @return boolean
	 */
	public static boolean matches(byte[] buf, int bufIdx, byte[] target) {
		for (int i = 0; i < target.length; i++) {
			if (toLower[buf[bufIdx + i]] != target[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parses headers from the given stream.
	 * 
	 * @param is
	 *            {@link BufferedNonBlockingInputStream}
	 * @param buf
	 *            byte array
	 * @param contentType
	 *            {@link StringBuffer}
	 * @param contentLocation
	 *            {@link StringBuffer}
	 * @param soapAction
	 *            {@link StringBuffer}
	 * @param httpRequest
	 *            {@link StringBuffer}
	 * @param fileName
	 *            {@link StringBuffer}
	 * @param cookie
	 *            {@link StringBuffer}
	 * @param cookie2
	 *            {@link StringBuffer}
	 * @param authInfo
	 *            {@link StringBuffer}
	 * @param host
	 *            {@link StringBuffer}
	 * @return int , content length
	 * @throws IOException
	 */
	public static int parseHeaders(BufferedNonBlockingInputStream is,
	 	byte buf[], StringBuffer contentType, StringBuffer contentLocation,
			StringBuffer soapAction, StringBuffer httpRequest,
			StringBuffer fileName, StringBuffer cookie, StringBuffer cookie2,
			StringBuffer authInfo, StringBuffer host)
			throws java.io.IOException {
		int n;
		int len = 0;

		n = readLine(is, buf, 0, buf.length);
		if (n < 0) {
			// nothing!
			throw new java.io.IOException(sResHash
					.getString("CONNECTOR.WSRECSERVER.UNEXPECTEDEOS00"));
		}

		httpRequest.delete(0, httpRequest.length());
		fileName.delete(0, fileName.length());
		contentType.delete(0, contentType.length());
		contentLocation.delete(0, contentLocation.length());

		if (buf[0] == getHeader[0]) {
			httpRequest.append("GET");
			for (int i = 0; i < n - 5; i++) {
				char c = (char) (buf[i + 5] & 0x7f);
				if (c == ' ')
					break;
				fileName.append(c);
			}
			return 0;
		} else if (buf[0] == postHeader[0]) {
			httpRequest.append("POST");
			for (int i = 0; i < n - 6; i++) {
				char c = (char) (buf[i + 6] & 0x7f);
				if (c == ' ')
					break;
				fileName.append(c);
			}
		} else {
			throw new java.io.IOException(sResHash
					.getString("CONNECTOR.WSRECSERVER.BADREQUEST00"));
		}

		while ((n = readLine(is, buf, 0, buf.length)) > 0) {

			if ((n <= 2) && (buf[0] == '\n' || buf[0] == '\r') && (len > 0))
				break;

			int endHeaderIndex = 0;
			while (endHeaderIndex < n
					&& toLower[buf[endHeaderIndex]] != headerEnder[0]) {
				endHeaderIndex++;
			}
			endHeaderIndex += 2;
			int i = endHeaderIndex - 1;

			if (endHeaderIndex == lenLen && matches(buf, lenHeader)) {

				while ((++i < n) && (buf[i] >= '0') && (buf[i] <= '9')) {
					len = (len * 10) + (buf[i] - '0');
				}

			} else if (endHeaderIndex == actionLen
					&& matches(buf, actionHeader)) {

				soapAction.delete(0, soapAction.length());
				/*
				 * i++; while ((++i < n) && (buf[i] != '"')) {
				 * soapAction.append((char) (buf[i] & 0x7f)); }
				 */

				while (++i < n) {
					if (buf[i] != '"') {
						soapAction.append((char) (buf[i] & 0x7f));
					}
				}

			} else if (endHeaderIndex == authLen && matches(buf, authHeader)) {
				if (matches(buf, endHeaderIndex, basicAuth)) {
					i += basicAuth.length;
					while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
						if (buf[i] == ' ')
							continue;
						authInfo.append((char) (buf[i] & 0x7f));
					}
				} else {
					throw new java.io.IOException(sResHash
							.getString("CONNECTOR.WSRECSERVER.BADAUTH00"));
				}
			} else if (endHeaderIndex == locationLen
					&& matches(buf, locationHeader)) {
				while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
					if (buf[i] == ' ')
						continue;
					contentLocation.append((char) (buf[i] & 0x7f));
				}
			} else if (endHeaderIndex == typeLen && matches(buf, typeHeader)) {
				while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
					if (buf[i] == ' ')
						continue;
					contentType.append((char) (buf[i] & 0x7f));
				}
			} else if (endHeaderIndex == hostLen
					&& matchesCase(buf, hostHeader)) {
				while (++i < n && (buf[i] != '\r') && (buf[i] != '\n')) {
					if (buf[i] == ' ')
						continue;
					host.append((char) (buf[i] & 0x7f));
				}
			}
		}
		return len;
	}
}
