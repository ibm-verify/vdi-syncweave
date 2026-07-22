/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.ibm.di.function.SystemFunctions;

public class RestServerLogger {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private URLConnection conn;
	private BufferedReader buf;

	public RestServerLogger() {
	}

	public RestServerLogger(Object url) throws Exception {
		conn = new URL(url.toString()).openConnection();
		conn.setReadTimeout(3000);
		buf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	}

	public String getNextMessage() throws Exception {
		return buf.readLine();
	}

	public void close() {
		if (buf != null) {
			try {
				buf.close();
			} catch (IOException e) {
				//Ignore
				SystemFunctions.doNothing();
			}
			buf = null;
		}
	}
}
