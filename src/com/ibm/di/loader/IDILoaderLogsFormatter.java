/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.loader;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class provides basic functionality for formating and obtaining strings
 * when logging. This class is nly for internal use.
 * 
 * @since 7.1
 */
public class IDILoaderLogsFormatter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This is the resource bundle with TMS messages for the IDILoader.
	 */
	private ResourceBundle resBundle;

	/**
	 * Constructor
	 */
	public IDILoaderLogsFormatter() {
		try {
			resBundle = ResourceBundle.getBundle("idiloadertms");
		} catch (MissingResourceException mre) {
			resBundle = null;
		}
	}

	/**
	 * Return the NLS string given the resource
	 */
	public String getString(String resource) {
		if (resBundle == null) {
			return resource;
		}

		String theString = resBundle.getString(resource);
		if (theString == null) {
			theString = resource; // if the string cannot be found, return the
			// string ID instead
		}
		return theString;
	}

	/**
	 * Return the NLS string given the resource and a parameter
	 */
	public String getString(String resource, Object param) {
		return MessageFormat.format(getString(resource), new Object[] { param });// perform
		// variable
		// substitution
	}

	/**
	 * Returns the NLS string value for the passed "resource" (key) and replaces
	 * the placeholders {0},{1},etc by the corresponding
	 * params[0],params[1],etc.
	 * 
	 * @param resource
	 *            The key whose value is to be retrieved.
	 * @param params
	 *            An array of strings which will replace placeholders
	 * @return The value with placeholders replaced.
	 */
	public String getString(String resource, Object[] params) {
		// perform variable substitution
		return MessageFormat.format(getString(resource), params);
	}
}
