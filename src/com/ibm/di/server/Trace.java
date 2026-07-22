/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Trace.java
package com.ibm.di.server;

import java.io.IOException;
import java.io.Serializable;

import com.ibm.log.Level;
import com.ibm.log.PDLogger;
import com.ibm.log.mgr.LogManager;
import com.ibm.log.mgr.PropertyFileDataStore;
import com.ibm.log.util.LogException;

/**
 * This class uses a <b>PDLogger</b> to log messages when AssemblyLines and
 * their elements pass through different stages (thus creating a trace of their
 * work).
 * 
 */
public class Trace implements Serializable {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file.
	 */
	public static final String propFileName = System
			.getProperty("jlog.configuration");

	/**
	 * The logger used for logging messages.
	 */
	private static PDLogger logger = null;

	static {
		if (propFileName != null) {
			try {
				LogManager logMgr = LogManager.getManagerWithMergedDataStore(
						new PropertyFileDataStore(propFileName), true);
				logger = (PDLogger) logMgr.getLogger(System.getProperty(
						"jlog.logger", "jlog.logger"));
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (LogException le) {
				le.printStackTrace();
			}
		}
	}

	/**
	 * Unique ID used for serialization.
	 */
	private static final long serialVersionUID = 42L;

	/**
	 * Constructor for the Trace object.
	 */
	public Trace() {
		super();
	}

	/**
	 * Logs a debug message with medium level.
	 * 
	 * @param loggingClass
	 *            a String representing the class that logs the message.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void entrymid(String loggingClass, String loggingMethod) {
		if (logger != null)
			logger.entry(Level.DEBUG_MID, loggingClass, loggingMethod);
	}

	/**
	 * Logs a debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void entrymid(Object This, String loggingMethod) {
		if (logger != null)
			logger.entry(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 */
	public static void entrymid(Object This, String loggingMethod, Object param1) {
		if (logger != null)
			logger.entry(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, param1);
	}

	/**
	 * Logs a debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 * @param param2
	 *            an additional parameter that will be logged.
	 */
	public static void entrymid(Object This, String loggingMethod,
			Object param1, Object param2) {
		if (logger != null)
			logger.entry(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, param1, param2);
	}

	/**
	 * Logs a debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param params
	 *            an array of additional parameters.
	 */
	public static void entrymid(Object This, String loggingMethod,
			Object[] params) {
		if (logger != null)
			logger.entry(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, params);
	}

	/**
	 * Logs a debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void entrymin(Object This, String loggingMethod) {
		if (logger != null)
			logger.entry(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 */
	public static void entrymin(Object This, String loggingMethod, Object param1) {
		if (logger != null)
			logger.entry(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod, param1);
	}

	/**
	 * Logs a debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 * @param param2
	 *            an additional parameter that will be logged.
	 */
	public static void entrymin(Object This, String loggingMethod,
			Object param1, Object param2) {
		if (logger != null)
			logger.entry(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod, param1, param2);
	}

	/**
	 * Logs a debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param params
	 *            an array of additional parameters.
	 */
	public static void entrymin(Object This, String loggingMethod,
			Object[] params) {
		if (logger != null)
			logger.entry(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod, params);
	}

	/**
	 * Logs a debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void entrymax(Object This, String loggingMethod) {
		if (logger != null)
			logger.entry(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 */
	public static void entrymax(Object This, String loggingMethod, Object param1) {
		if (logger != null)
			logger.entry(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, param1);
	}

	/**
	 * Logs a debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param param1
	 *            an additional parameter that will be logged.
	 * @param param2
	 *            an additional parameter that will be logged.
	 */
	public static void entrymax(Object This, String loggingMethod,
			Object param1, Object param2) {
		if (logger != null)
			logger.entry(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, param1, param2);
	}

	/**
	 * Logs a debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param params
	 *            an array of additional parameters.
	 */
	public static void entrymax(Object This, String loggingMethod,
			Object[] params) {
		if (logger != null)
			logger.entry(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, params);
	}

	/**
	 * Logs a exit debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void exitmin(Object This, String loggingMethod) {
		if (logger != null)
			logger.exit(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a exit debug message with minimum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned value.
	 */
	public static void exitmin(Object This, String loggingMethod,
			Object retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MIN, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void exitmid(Object This, String loggingMethod) {
		if (logger != null)
			logger.exit(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a exit debug message with medium level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned value.
	 */
	public static void exitmid(Object This, String loggingMethod,
			Object retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 */
	public static void exitmax(Object This, String loggingMethod) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned value.
	 */
	public static void exitmax(Object This, String loggingMethod,
			Object retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>int</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, int retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>boolean</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod,
			boolean retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>float</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, float retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>double</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod,
			double retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>char</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, char retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>long</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, long retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>byte</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, byte retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs a exit debug message with maximum level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param retValue
	 *            the returned <b>short</b> value.
	 */
	public static void exitmax(Object This, String loggingMethod, short retValue) {
		if (logger != null)
			logger.exit(Level.DEBUG_MAX, This.getClass().getName(),
					loggingMethod, retValue);
	}

	/**
	 * Logs an exception with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param throwable
	 *            a reference to the occurd exception.
	 * @param text
	 *            additional information, leave it a <code>""</code> if not
	 *            needed.
	 */
	public static void exception(Object This, String loggingMethod,
			Throwable throwable, String text) {
		if (logger != null)
			logger.exception(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, throwable, text);
	}

	/**
	 * Logs an exception with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param throwable
	 *            a reference to the occurd exception.
	 */
	public static void exception(Object This, String loggingMethod,
			Throwable throwable) {
		if (logger != null)
			logger.exception(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, throwable);
	}

	/**
	 * Logs an text message with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param text
	 *            additional information, leave it a <code>""</code> if not
	 *            needed.
	 */
	public static void text(Object This, String loggingMethod, String text) {
		if (logger != null)
			logger.text(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, text);
	}

	/**
	 * Logs an text message with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param text
	 *            additional information, leave it a <code>""</code> if not
	 *            needed.
	 * @param insert1
	 *            an object that will be added to the message.
	 */
	public static void text(Object This, String loggingMethod, String text,
			Object insert1) {
		if (logger != null)
			logger.text(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, text, insert1);
	}

	/**
	 * Logs an text message with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param text
	 *            additional information, leave it a <code>""</code> if not
	 *            needed.
	 * @param insert1
	 *            an object that will be added to the message.
	 * @param insert2
	 *            an object that will be added to the message.
	 */
	public static void text(Object This, String loggingMethod, String text,
			Object insert1, Object insert2) {
		if (logger != null)
			logger.text(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, text, insert1, insert2);
	}

	/**
	 * Logs an text message with medium debug level.
	 * 
	 * @param This
	 *            a reference to the logging class.
	 * @param loggingMethod
	 *            the logging method.
	 * @param text
	 *            additional information, leave it a <code>""</code> if not
	 *            needed.
	 * @param inserts
	 *            an array of objects that will be added to the message.
	 */
	public static void text(Object This, String loggingMethod, String text,
			Object[] inserts) {
		if (logger != null)
			logger.text(Level.DEBUG_MID, This.getClass().getName(),
					loggingMethod, text, inserts);
	}

}
