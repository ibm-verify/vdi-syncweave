/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// HTTPUtils.java
//
//
//
package com.ibm.di.util;

import java.net.*;

public class HTTPUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static String lastError = null;

	public static String getLastError() {
		if (lastError != null)
			return lastError;
		else
			return "";
	}

	public static String requestOK() {
		return ("HTTP/1.1 200 OK\r\n");
	}

	public static String fileNotFound() {
		return ("HTTP/1.1 404 OK\r\n");
	}

	public static String authenticationRequest() {
		String str = ("HTTP/1.1 401 Forbidden\r\n");
		str += "WWW-Authenticate: Basic realm=\"IBM-Directory-Integrator\"\r\n";
		return str;
	}

	public static URLConnection openURL(String url) {
		try {
			URL u = new URL(url);
			URLConnection conn = u.openConnection();
			return conn;
		} catch (Exception e) {
			lastError = e.toString();
			return null;
		}
	}
}
