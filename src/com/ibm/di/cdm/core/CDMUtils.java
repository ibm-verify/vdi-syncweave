/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.util.Locale;

import org.w3c.dom.Node;

import com.ibm.di.entry.NameTokenizer;

/**
 * This class contains several utility methods used by the CDM Components.
 * 
 */
public class CDMUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Capitalizes the first letter in the provided string.
	 * 
	 * @param name
	 *            input string.
	 * @return the resulting capitalized string.
	 */
	public static String toUpperCaseFirstLetter(String name) {
		String newName = name;
		if (name != null && (name.length() > 0) && Character.isLowerCase(name.charAt(0))) {
			newName = name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
		}
		return newName;
	}

	/**
	 * Makes the first letter of the provided string lower.
	 * 
	 * @param name
	 *            input string.
	 * @return the input string with lower case first letter.
	 */
	public static String toLowercaseFirstLetter(String name) {
		String newName = name;
		if (name != null && (name.length() > 0) && Character.isUpperCase(name.charAt(0))) {
			newName = name.substring(0, 1).toLowerCase(Locale.ENGLISH) //
					+ name.substring(1);
		}
		return newName;
	}

	/**
	 * Escapes the provided string using the provided parameters.
	 * 
	 * @param string
	 *            input string.
	 * @param separatorChar
	 *            the separator char which must be escaped.
	 * @param escapeChar
	 *            the char used for escaping.
	 * @return the resulting string.
	 */
	public static String escapeString(String string, String separatorChar, char escapeChar) {
		StringBuilder builder = new StringBuilder(string);
		int position = -1;
		while ((position = builder.indexOf(separatorChar, ++position)) >= 0) {
			if (position - 1 < 0 || builder.charAt(position - 1) != escapeChar) {
				builder.insert(position, '\\');
			} else {
				int current = position - 2;
				while (current >= 0 && builder.charAt(current) == escapeChar) {
					--current;
				}

				if ((position - 1 - current) % 2 == 0) {
					builder.insert(position, '\\');
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Escapes the '.' characters in the provided string placing a '\' before
	 * them.
	 * 
	 * @param string
	 *            input string.
	 * @return resulting string.
	 */
	public static String escapeString(String string) {
		return escapeString(string, ".", '\\');
	}

	/**
	 * Removes any CDM prefix present in the provided string.
	 * 
	 * @param prefixedString
	 *            input string.
	 * @return the resulting string.
	 */
	public static String removePrefix(String prefixedString) {
		int index = prefixedString.indexOf(':');
		return prefixedString.substring(index + 1);
	}

	/**
	 * Adds a CDM prefix to the provided string, if its does not have it.
	 * 
	 * @param string
	 *            input string.
	 * @return the resulting string.
	 */
	public static String addPrefix(String string) {
		if (!string.startsWith(CDM_PREFIX)) {
			string = CDM_PREFIX + string;
		}
		return string;
	}

	/**
	 * Removes the '\' escape characters before each '.' in the input string.
	 * 
	 * @param escapedString
	 *            the escaped input string.
	 * @return the resulting string.
	 */
	public static String removeEscapeChars(String escapedString) {
		NameTokenizer nameTokenizer = new NameTokenizer();
		nameTokenizer.setName(escapedString);
		StringBuilder result = new StringBuilder(escapedString.length());
		String token = nameTokenizer.getNextToken('.');
		while (token != null) {
			result.append(token);
			token = nameTokenizer.getNextToken('.');
			if (token != null) {
				result.append('.');
			}
		}

		return result.toString();
	}
	
	/**
	 * Returns the full path from the hierarchy's root to the specified node.
	 * The '.' char is used for separating each node name from.
	 * 
	 * @param attr
	 *            the node which path we want.
	 * @return the full path from the root.
	 */
	public static String getAttributePath(Node attr) {
		String path = null;
		if (attr != null) {
			path = attr.getNodeName();
			Node parent = attr.getParentNode();
			if (parent != null) {
				path = getAttributePath(parent) + "." + path;
			}
		}
		return path;
	}
}
