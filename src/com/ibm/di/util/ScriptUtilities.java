/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import com.ibm.di.script.ScriptEngine;

public class ScriptUtilities {
	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static void includeScript(ScriptEngine se, String files)
			throws Exception {
		se.includeScript("includeScript", files);
	}

	/**
	 * Reads the contents of a text file.
	 * 
	 * @param path
	 *            the URL of the file
	 * @return the contents of the file
	 * @exception Exception
	 *                problem while reading the file
	 */

	public static String loadFile(String path) throws Exception {
		BufferedReader inp = null;
		StringBuffer buf = new StringBuffer();
		String line;

		java.net.URL u = new java.net.URL(path);
		java.net.URLConnection conn = u.openConnection();
		inp = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		if (inp == null) {
			inp = new BufferedReader(new FileReader(path));
		}

		while ((line = inp.readLine()) != null) {
			buf.append(line);
			buf.append("\n");
		}

		inp.close();

		return buf.toString();
	}

}
