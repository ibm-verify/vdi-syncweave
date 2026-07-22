/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// HTTPFunctions.java
//
//
//
package com.ibm.di.function;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Locale;

import javax.mail.internet.*;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.*;

import com.ibm.icu.util.StringTokenizer;

public class HTTPFunctions {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Exception lastError;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	static public class httpDataStream extends BufferedReader {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		private int size;

		private BufferedReader stream;

		public httpDataStream(BufferedReader stream, int size) {
			super(stream);
			this.stream = stream;
			this.size = size;
		}

		public int available() {
			return size;
		}

		public void mark(int readlimit) {
		}

		public boolean markSupported() {
			return false;
		}

		public int read() throws IOException {
			// System.out.println ( "dataStream: read one byte " + size);
			if (size > 0) {
				size--;
				return stream.read();
			}
			return -1;
		}

		public void close() {
			size = 0;
		}

		public int read(char[] buf) throws IOException {
			// System.out.println ( "read byte buffer");
			return read(buf, 0, buf.length);
		}

		public int read(char[] buf, int off, int len) throws IOException {
			int rc = 0;

			// System.out.println ( "dataStream: off=" + off + ", len=" + len +
			// ", size=" + size);

			if (size < 1)
				return -1;

			if (len > size)
				rc = stream.read(buf, off, size);
			else
				rc = stream.read(buf, off, len);

			// System.out.println ( "Read " + rc + " bytes from stream");

			size -= rc;
			// System.out.println ( "dataStream: rc=" + rc + ", size=" + size);

			return rc;
		}

		public String readLine() throws IOException {
			// System.out.println ( "dataStream: readLine");
			// System.out.println ( "dataStream: size=" + size);

			if (size < 1)
				return null;

			StringBuffer buf = new StringBuffer();
			int i;

			while ((i = read()) != -1) {
				switch (i) {
				case 10:
					return buf.toString();
				case 13:
					break;
				default:
					buf.append((char) i);
					break;
				}
				// System.out.println ( "Line is now: " + buf.toString());
			}

			return buf.toString();
		}

	}

	static public class httpRequest {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		public String method;

		public String path;

		public Hashtable parameters;

		public Hashtable headers;

		public BufferedReader dataStream;

		public httpRequest() {
			parameters = new Hashtable();
			headers = new Hashtable();
			method = "";
			path = "";
		}

	}

	public httpRequest parseRequest(Socket socket) {
		lastError = null;

		try {
			return parseRequestX(socket);
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	private httpRequest parseRequestX(Socket socket) throws Exception {
		String str;
		String s;
		String val;
		int length = 0;
		int ix;
		boolean urlEncoded = false;
		httpRequest e = new httpRequest();

		BufferedReader in = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));

		str = in.readLine();

		StringTokenizer st = new StringTokenizer(str, " ?&");

		// GET / POST
		s = st.nextToken();
		e.method = s.toUpperCase(Locale.ENGLISH);

		// Base URL
		s = st.nextToken();
		e.path = s;

		while (st.hasMoreTokens()) {
			s = st.nextToken();
			if (s.indexOf("=") != -1) {
				ix = s.indexOf("=");
				val = mapValue(s.substring(ix + 1));
				e.parameters.put(s.substring(0, ix), val);
			} else {
				e.parameters.put(s, "");
			}
		}

		// HTTP/1.1 sends headers + blank
		if (s != null && s.compareToIgnoreCase("http/1.1") != 0) {
			return e;
		}

		while ((str = in.readLine()) != null) {
			ix = str.indexOf(":");
			if (ix > 0) {
				s = mapValue(str);
				ix = s.indexOf(":");
				val = s.substring(ix + 1);
				s = s.substring(0, ix);
				e.headers.put(s, val.trim());
				if (s.toLowerCase(Locale.ENGLISH).startsWith("content-length")) {
					length = Integer.parseInt(val.trim());
				}
				if ((s.toLowerCase(Locale.ENGLISH).startsWith("content-type"))
						&& (val.indexOf("x-www-form-urlencoded") != -1)) {
					urlEncoded = true;
				}
			}

			if (str.length() < 1)
				break;
		}

		e.dataStream = new httpDataStream(in, length);

		// User may have used POST instead of GET
		if (urlEncoded) {
			while ((str = e.dataStream.readLine()) != null) {
				st = new StringTokenizer(str, "&");
				while (st.hasMoreTokens()) {
					s = st.nextToken();
					s = mapValue(s);
					if (s.indexOf("=") != -1) {
						ix = s.indexOf("=");
						val = s.substring(ix + 1);
						e.parameters.put(s.substring(0, ix), val.trim());
					} else {
						e.parameters.put(s, "");
					}
				}
			}
		}

		return e;
	}

	private String mapValue(String str) {
		int i = 0;
		StringBuffer ns = new StringBuffer("");
		char ch;

		while (i < str.length()) {
			ch = str.charAt(i);
			switch (ch) {
			case '%':
				i++;
				char x = (char) StringUtils.fromHex(str.substring(i, i + 2));
				ns.append((char) StringUtils.fromHex(str.substring(i, i + 2)));
				i += 2;
				break;
			case '+':
				ns.append(" ");
				i++;
				break;
			default:
				ns.append(ch);
				i++;
				break;
			}
		}

		return ns.toString();
	}

	public URLConnection openURL(String url) {
		try {
			URL u = new URL(url);
			URLConnection conn = u.openConnection();
			return conn;
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}
}
