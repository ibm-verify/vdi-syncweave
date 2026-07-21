/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import org.apache.log4j.Logger;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.MessageFormat;

/**
 * Implements the messaging and logging functions required by the update
 * installer
 */
public class UpdateInstallerMsgs {

	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Name of the XML filename with the Update Installer messages
	 */
	private static final String TMSFILE = "updateinstaller";

	/**
	 * This is the resource bundle with TMS messages for the IDILoader.
	 */
	private static ResourceHash resBundle = ResourceHash.getHash(TMSFILE);

	/**
	 * This is the logger used for logging msgs and the levels
	 */
	private static Logger logger;

	/**
	 * FATAL log level
	 */
	public static final int FATAL = 32;
	/**
	 * ERROR log level
	 */
	public static final int ERROR = 16;
	/**
	 * WARN log level
	 */
	public static final int WARN = 8;
	/**
	 * INFO log level
	 */
	public static final int INFO = 4;
	/**
	 * DEBUG log level
	 */
	public static final int DEBUG = 2;
	/**
	 * TRACE log level
	 */
	public static final int TRACE = 1;

	/**
	 * Default class constructor for the UpdateInstallerMsgs object
	 */
	public UpdateInstallerMsgs() {
	}

	/**
	 * Return the NLS string given the resource
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @return The NLS string representing the specified resource
	 */
	public static String getString(String resource) {
		String theString = resBundle.getString(resource);
		if (theString == null) {
			theString = resource; // if the string cannot be found, return the
									// string ID instead
		}
		return theString;
	}

	/**
	 * Return the NLS string given the resource and a parameter
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into a variable in the resource
	 *            string
	 * @return The NLS string representing the specified resource with the
	 *         placeholder replaced
	 */
	public static String getString(String resource, Object param) {
		return MessageFormat
				.format(getString(resource), new Object[] { param });// perform
		// variable
		// substitution
	}

	/**
	 * Return the NLS string given the resource and 2 parameters
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into the 1st variable in the
	 *            resource string
	 * @param param2
	 *            A parameter to substitute into the 2nd variable in the
	 *            resource string
	 * @return The NLS string representing the specified resource with
	 *         placeholders replaced
	 */
	public static String getString(String resource, Object param, Object param2) {
		return MessageFormat.format(getString(resource), new Object[] { param,
				param2 });
	}

	/**
	 * Return the NLS string given the resource and 3 parameters
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into the 1st variable in the
	 *            resource string
	 * @param param2
	 *            A parameter to substitute into the 2nd variable in the
	 *            resource string
	 * @param param3
	 *            A parameter to substitute into the 3rd variable in the
	 *            resource string
	 * @return The NLS string representing the specified resource with
	 *         placeholders replaced
	 */
	public static String getString(String resource, Object param,
			Object param2, Object param3) {
		return MessageFormat.format(getString(resource), new Object[] { param,
				param2, param3 });
	}

	/**
	 * Return the NLS string given the resource and 4 parameters
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into the 1st variable in the
	 *            resource string
	 * @param param2
	 *            A parameter to substitute into the 2nd variable in the
	 *            resource string
	 * @param param3
	 *            A parameter to substitute into the 3rd variable in the
	 *            resource string
	 * @param param4
	 *            A parameter to substitute into the 4th variable in the
	 *            resource string
	 * @return The NLS string representing the specified resource with
	 *         placeholders replaced
	 */
	public static String getString(String resource, Object param,
			Object param2, Object param3, Object param4) {
		return MessageFormat.format(getString(resource), new Object[] { param,
				param2, param3, param4 });
	}

	/**
	 * Return the NLS string given the resource and 5 parameters
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into the 1st variable in the
	 *            resource string
	 * @param param2
	 *            A parameter to substitute into the 2nd variable in the
	 *            resource string
	 * @param param3
	 *            A parameter to substitute into the 3rd variable in the
	 *            resource string
	 * @param param4
	 *            A parameter to substitute into the 4th variable in the
	 *            resource string
	 * @param param5
	 *            A parameter to substitute into the 5th variable in the
	 *            resource string
	 * @return The NLS string representing the specified resource with
	 *         placeholders replaced
	 */
	public static String getString(String resource, Object param,
			Object param2, Object param3, Object param4, Object param5) {
		return MessageFormat.format(getString(resource), new Object[] { param,
				param2, param3, param4, param5 });
	}

	/**
	 * Return the NLS string given the resource and 6 parameters
	 * 
	 * @param resource
	 *            The TMS file resource to be looked up from the resource bundle
	 * @param param
	 *            A parameter to substitute into the 1st variable in the
	 *            resource string
	 * @param param2
	 *            A parameter to substitute into the 2nd variable in the
	 *            resource string
	 * @param param3
	 *            A parameter to substitute into the 3rd variable in the
	 *            resource string
	 * @param param4
	 *            A parameter to substitute into the 4th variable in the
	 *            resource string
	 * @param param5
	 *            A parameter to substitute into the 5th variable in the
	 *            resource string
	 * @param param6
	 *            A parameter to substitute into the 6th variable in the
	 *            resource string
	 * @return The NLS string representing the specified resource with
	 *         placeholders replaced
	 */
	public static String getString(String resource, Object param,
			Object param2, Object param3, Object param4, Object param5,
			Object param6) {
		return MessageFormat.format(getString(resource), new Object[] { param,
				param2, param3, param4, param5, param6 });
	}

	/**
	 * Returns the NLS string value for the passed "resource" (key) and replaces
	 * the placeholders {0},{1},etc by the corresponding params[0], params[1],
	 * etc.
	 * 
	 * @param resource
	 *            The key whose value is to be retrieved.
	 * @param params
	 *            An array of strings which will replace placeholders
	 * @return The value with placeholders replaced.
	 */
	public static String getString(String resource, Object[] params) {
		return MessageFormat.format(getString(resource), params);
	}

	/**
	 * Logs the specified message with the specified level using the log4j
	 * utility.
	 * 
	 * @param msg
	 *            The message to be logged
	 * @param level
	 *            The level at which to log this message
	 */
	public static void log(String msg, int level) {
		if (logger == null)
			logger = Logger.getLogger("UpdateInstaller.UpdateInstallerMsgs");

		switch (level) {
		case TRACE:
			logger.trace(msg);
			break;
		case DEBUG:
			logger.debug(msg);
			break;
		case INFO:
			logger.info(msg);
			break;
		case WARN:
			logger.warn(msg);
			break;
		case FATAL:
			logger.fatal(msg);
			break;
		case ERROR:
		default:
			logger.error(msg);
			break;
		}
	}

}
