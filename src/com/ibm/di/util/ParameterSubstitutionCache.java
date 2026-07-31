/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.*;

public class ParameterSubstitutionCache {

	private HashMap<String,Object> map = new HashMap<String,Object>();

	private HashMap<String,ParameterSubstitution> expressions = new HashMap<String,ParameterSubstitution>();

	/**
	 * Returns true if str contains expressions
	 */
	public static boolean isExpression(String str) {
		// currently we simply let the first char decide
		if (str == null)
			return false;
		else if (str.length() == 0)
			return false;
		else
			return str.startsWith("{");
	}

	/**
	 * Remove the enclosing curly braces from an expression. We only do this if
	 * there is a nested {{}} present.
	 * 
	 * @param str
	 * @return The string without the braces
	 */
	public static String stripExpression(String str) {
		boolean cb = false;
		int n = str.length();
		if (n < 4 || str.charAt(0) != '{' || str.charAt(n - 1) != '}')
			return str;

		for (int i = 0; i < n; i++) {
			switch (str.charAt(i)) {
			case '{':
				if (cb)
					return str.substring(1, n - 1);
				else
					cb = true;
				break;
			case '}':
				cb = false;
				break;
			}
		}
		return str;
	}

	public ParameterSubstitutionCache() {
	}

	public String substitute(String str, String objectName, Object object)
			throws Exception {
		if (str == null)
			return str;
		ParameterSubstitution ps = expressions.get(str);
		if (ps == null) {
			ps = new ParameterSubstitution(stripExpression(str), map);
			expressions.put(str, ps);
		}

		if (objectName != null) {
			if (object != null)
				map.put(objectName, object);
			else
				map.remove(objectName);
		}
		return ps.substitute(map);
	}

	public void put(String objectName, Object object) {
		if (object != null)
			map.put(objectName, object);
		else
			map.remove(objectName);
	}

}
