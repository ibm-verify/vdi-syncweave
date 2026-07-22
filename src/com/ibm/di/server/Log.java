/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Log.java
package com.ibm.di.server;

import java.io.*;
import java.util.*;

import com.ibm.di.api.syslog.SystemLogAppender;
import com.ibm.di.entry.*;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.log.LogInterface;
import com.ibm.di.log.TDILog4j;

import java.text.MessageFormat;

/**
 * The Log object is used to log messages to logs. It keeps a list of different
 * Loggers, all implementing LogInterface, and asks all of them to log.
 */
public class Log implements Serializable {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The debug mode flag for the log.
	 */
	public boolean debug = false;

	/**
	 * Collection of loggers.
	 */
	private transient InternalLogger loggers = new InternalLogger();

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private transient ResourceHash res;

	/**
	 * Prefix for the messages.
	 */
	private String prefix = "";

	private SystemLogAppender systemLogAppender;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private final static String PROPERTIESFILE = "miserver";

	/**
	 * Category parameter for the loggers.
	 */
	private String category = null;

	/**
	 * The parent log.
	 */
	private Log parentLog = null;

	/**
	 * Categories.
	 */
	private static Properties categories;

	/**
	 * File of the categories. From it are loaded the categories properties.
	 */
	private final static String CATEGORIESFILE = "logging.categories";

	/**
	 * A property.
	 */
	private final static String LOG4J_INTERFACE = "com.ibm.di.log.TDILog4j";

	/**
	 * A property.
	 */
	private final static String LOGGING_ENABLED = "com.ibm.di.logging.enabled";

	/**
	 * Parameter specifying whether logging is enabled.
	 */
	private static Boolean loggingEnabled = null;
	
	/**
	 * Lock to synchronize access to {@link #loggingEnabled}.
	 */
	private static Object loggingEnabledLock = new Object();

	/**
	 * Unique number used in serialization.
	 */
	private static final long serialVersionUID = 42L;

	static {
		categories = new Properties();
		// Initialize the categories
		if (new File(CATEGORIESFILE).exists()) {
			try {
				FileInputStream fis = new FileInputStream(CATEGORIESFILE);
				try {
					categories.load(fis);
				} finally {
					fis.close();
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * A constructor which takes only a resource name.
	 * 
	 * @param resourceName
	 *            Name used both as translation resource name and the category
	 *            name
	 */
	public Log(String resourceName) {
		res = ResourceHash.getHash(resourceName);
		try {
			add(resourceName);
		} catch (Exception e) {
			// Nowhere to log the problem
		}
		setLoggingEnabled();
	}

	/**
	 * A constructor with both resourceName and category name
	 * 
	 * @param resourceName
	 *            Name used for locating translation resource
	 * @param category
	 *            The category name for the loggers
	 */
	public Log(String resourceName, String category) {
		res = ResourceHash.getHash(resourceName);
		try {
			add(category);
		} catch (Exception e) {
			// Nowhere to log the problem
		}
		setLoggingEnabled();
	}

	/**
	 * A constructor taking another Log as a parameter. All messages are logged
	 * to that Log's internal list of loggers.
	 * 
	 * @param parentLog
	 *            The other Log
	 */
	public Log(Log parentLog) {
		res = parentLog.res;
		category = parentLog.category;
		this.parentLog = parentLog;
		loggers.setParentLog(parentLog);
	}

	/**
	 * A constructor with both resourceName and an InternalLogger
	 * 
	 * @param resourceName
	 *            Name used for locating translation resource
	 * @param loggers
	 *            Used as the list of loggers
	 * @deprecated
	 */
	@Deprecated
	public Log(String resourceName, InternalLogger loggers) {
		res = ResourceHash.getHash(resourceName);
		category = resourceName;
		this.loggers = loggers;
	}

	/**
	 * Look up the correct LogInterface using the given name, and add a Logger
	 * of that type
	 * 
	 * @param name
	 *            the name of a LogInterface
	 * @throws Exception
	 *             if problem occurs
	 */
	private void add(String name) throws Exception {
		if (name != null)
			category = name;
		String s = (name == null ? "" : name);
		String className = categories.getProperty(s);
		while (className == null && s.indexOf('.') > 0) {
			s = s.substring(0, s.lastIndexOf('.'));
			className = categories.getProperty(s);
		}
		if (className == null)
			className = categories.getProperty("*");
		LogInterface l = getClassLogger(className);
		if (category != null)
			l.setCategory(category);
	}

	/**
	 * Returns a Vector containing all org.apache.log4j.Logger objects used by
	 * this Log.
	 * 
	 * @return a Vector containing all Logger objects used by this Log.
	 */
	public Vector<org.apache.logging.log4j.Logger> getLoggers() {
		Vector<org.apache.logging.log4j.Logger> ret;
		if (parentLog != null)
			ret = parentLog.getLoggers();
		else
			ret = new Vector<org.apache.logging.log4j.Logger>();

		for (Object log:loggers) {
			if (log instanceof TDILog4j)
				ret.add(((TDILog4j) log).myLogger);
		}
		return ret;
	}

	/**
	 * Returns the SystemLogAppender if any, in the Log.
	 * @return
	 */
	public SystemLogAppender getSystemLog() {
		return systemLogAppender;
	}

	public void setSystemLogAppender(SystemLogAppender tmp) {
		systemLogAppender = tmp;
	}

	/**
	 * Add a LogInterface to internal list of loggers.
	 * 
	 * @param logger
	 *            The new LogInterface object.
	 */
	public void addLogger(LogInterface logger) {
		synchronized (loggers) {
			loggers.addLogger(logger);
		}
	}

	/**
	 * Remove a LogInterface from the internal list of loggers.
	 * 
	 * @param logger
	 *            The LogInterface object.
	 */
	public void removeLogger(LogInterface logger) {
		synchronized (loggers) {
			loggers.removeLogger(logger);
		}
	}

	/**
	 * Returns a TDILog4j Logger connected to this Log object.
	 * 
	 * @return the TDILog4j Logger
	 */
	public TDILog4j getTDILog4j() {
		synchronized (loggers) {
			for (int i = 0; i < loggers.size(); i++)
				if (loggers.get(i) instanceof TDILog4j)
					return (TDILog4j) loggers.get(i);

			TDILog4j ret = new TDILog4j();
			addLogger(ret);
			if (category != null)
				ret.setCategory(category);
			return ret;
		}
	}

	/**
	 * Returns a LogInterface with the given class Name. An instance is created
	 * and added to the internal list of loggers.
	 * 
	 * @param className
	 *            The class name
	 * @return a LogInterface with the given class Name.
	 * @throws Exception
	 *             if problem occurs
	 */
	public LogInterface getClassLogger(String className) throws Exception {
		if (className == null || className.length() == 0)
			className = LOG4J_INTERFACE;

		LogInterface ret = (LogInterface) Class.forName(className)
				.newInstance();
		addLogger(ret);
		return ret;
	}

	/**
	 * Sets debug parameter
	 * 
	 * @param debug
	 *            true if debug level should be output as info
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Return the value of the debug parameter
	 * 
	 * @return the <code>boolean</code> value
	 */
	public boolean getDebug() {
		return debug;
	}

	/**
	 * Checks whether debug is enabled.
	 * 
	 * @return <b>true</b> if the debug mode flag is true or any of the kept
	 *         loggers has enabled debug, otherwise returns <b>false</b>.
	 */
	public boolean isDebugEnabled() {
		return debug || loggers.isDebugEnabled();
	}

	/**
	 * Checks the com.ibm.di.logging.enabled property to see if logging is
	 * enabled
	 */
	public static void setLoggingEnabled() {
		synchronized (loggingEnabledLock) {
			if (loggingEnabled == null && System.getProperty(LOGGING_ENABLED) != null)
				loggingEnabled = Boolean.valueOf(System.getProperty(LOGGING_ENABLED));
		}
	}

	/**
	 * Disables or enables TDI logging. All loggers are affected by this
	 * setting.
	 * 
	 * @param enabled
	 *            if <code>true</code> all loggers are enabled, otherwise they
	 *            are disabled
	 */
	public static void setLoggingEnabled(boolean enabled) {
		synchronized (loggingEnabledLock) {
			loggingEnabled = Boolean.valueOf(enabled);
		}
	}

	/**
	 * Returns whether TDI logging is active or disabled.
	 * 
	 * @return <code>true</code> if logging is enabled, otherwise
	 *         <code>false</code>
	 */
	public static boolean isLoggingEnabled() {
		synchronized (loggingEnabledLock) {
			return loggingEnabled == null || loggingEnabled.booleanValue();
		}
	}

	/**
	 * Sets the category for this Log
	 * 
	 * @param category
	 *            The category
	 * @since 7.0
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Returns the category for this Log
	 * 
	 * @return The category
	 * @since 7.0
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets a prefix to be prepended to all messages
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Returns the prefix to be prepended to all messages
	 * 
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Return the NLS string given the resource.
	 * 
	 * @param resource
	 *            the given resource
	 * @return a NLS string
	 */

	public String getString(String resource) {
		return prefix + res.getString(resource);
	}

	/**
	 * Return the NLS string given the resource and a parameter.
	 * 
	 * @param resource
	 *            the given resource
	 * @param param
	 *            a parameter
	 * @return a NLS strings
	 */
	public String getString(String resource, Object param) {
		return prefix
				+ MessageFormat.format(res.getString(resource),
						new Object[] { param });
	}

	/**
	 * Return the NLS string given the resource and two parameters
	 * 
	 * @param resource
	 *            the given resource
	 * @param param1
	 *            a parameter
	 * @param param2
	 *            a parameter
	 * @return a NLS strings
	 */

	public String getString(String resource, Object param1, Object param2) {
		return prefix
				+ MessageFormat.format(res.getString(resource), new Object[] {
						param1, param2 });
	}

	/**
	 * Return the NLS string given the resource and an array of parameters
	 * 
	 * @param resource
	 *            the given resource
	 * @param params
	 *            an array of parameters
	 * @return a NLS strings
	 */
	public String getString(String resource, Object[] params) {
		return prefix + MessageFormat.format(res.getString(resource), params);
	}

	/**
	 * Close the output logger
	 */
	public void close() {
		synchronized (loggers) {
			loggers.close();
		}
	}

	/**
	 * Logs a message to the output stream.
	 * 
	 * @param msg
	 *            The message to log.
	 */
	public void logfine(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (loggers.isDebugEnabled())
				loggers.debug(prefix + msg);
		}
	}

	/**
	 * Log a message with info level if the debug mode flag is <code>true</code>,
	 * otherwise an debug level message is logged. This is done for all used
	 * loggers.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void logdebug(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (debug)
				loggers.info(prefix + msg);
			else if (loggers.isDebugEnabled())
				loggers.debug(prefix + msg);
		}
	}

	/**
	 * Log a message with info level. This is done for all used loggers.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void loginfo(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.info(prefix + msg);
		}
	}

	/**
	 * Log a message with warning level. This is done for all used loggers.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void logwarn(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.warn(prefix + msg);
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void logerror(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(prefix + msg);
		}
	}

	/**
	 * Log a message with error level with an additional Throwable object. This
	 * is done for all used loggers.
	 * 
	 * @param msg
	 *            the message to log
	 * @param error
	 *            the Throwable to be logged
	 */
	public void logerror(String msg, Throwable error) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(prefix + msg, error);
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers.
	 * 
	 * @param msg
	 *            the message to log
	 */
	public void logfatal(String msg) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(prefix + msg);
		}
	}

	/**
	 * Log a message with debug level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource given.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void fine(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (loggers.isDebugEnabled())
				loggers.debug(getString(res));
		}
	}

	/**
	 * Log a message with debug level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void fine(String res, Object param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (loggers.isDebugEnabled())
				loggers.debug(getString(res, param));
		}
	}

	/**
	 * Log a message with debug level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes two
	 * additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 */
	public void fine(String res, Object param1, Object param2) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (loggers.isDebugEnabled())
				loggers.debug(getString(res, param1, param2));
		}
	}

	/**
	 * Log a message with debug level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes array with
	 * parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 */
	public void fine(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (loggers.isDebugEnabled())
				loggers.debug(getString(res, params));
		}
	}

	/**
	 * Log a message with debug level if the debug mode flag is
	 * <code>true</code>, otherwise an info level message is logged. This is
	 * done for all used loggers. The message is a NLS string formed using the
	 * resource given.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void debug(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (debug)
				loggers.info(getString(res));
			else if (loggers.isDebugEnabled())
				loggers.debug(getString(res));
		}
	}

	/**
	 * Log a message with debug level if the debug mode flag is
	 * <code>true</code>, otherwise an info level message is logged. This is
	 * done for all used loggers. The message is a NLS string formed using the
	 * resource and includes a parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void debug(String res, Object param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (debug)
				loggers.info(getString(res, param));
			else if (loggers.isDebugEnabled())
				loggers.debug(getString(res, param));
		}
	}

	/**
	 * Log a message with debug level if the debug mode flag is
	 * <code>true</code>, otherwise an info level message is logged. This is
	 * done for all used loggers. The message is a NLS string formed using the
	 * resource and includes two additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 */
	public void debug(String res, Object param1, Object param2) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (debug)
				loggers.info(getString(res, param1, param2));
			else if (loggers.isDebugEnabled())
				loggers.debug(getString(res, param1, param2));
		}
	}

	/**
	 * Log a message with debug level if the debug mode flag is
	 * <code>true</code>, otherwise an info level message is logged. This is
	 * done for all used loggers. The message is a NLS string formed using the
	 * resource and includes array with parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 */
	public void debug(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			if (debug)
				loggers.info(getString(res, params));
			else if (loggers.isDebugEnabled())
				loggers.debug(getString(res, params));
		}
	}

	/**
	 * Log a message with info level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void info(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.info(getString(res));
		}
	}

	/**
	 * Log a message with info level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void info(String res, Object param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.info(getString(res, param));
		}
	}

	/**
	 * Log a message with info level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes two
	 * additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 */
	public void info(String res, Object param1, Object param2) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.info(getString(res, param1, param2));
		}
	}

	/**
	 * Log a message with info level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes array with
	 * parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 */
	public void info(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.info(getString(res, params));
		}
	}

	/**
	 * Log a message with warning level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void warn(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.warn(getString(res));
		}
	}

	/**
	 * Log a message with warning level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void warn(String res, Object param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.warn(getString(res, param));
		}
	}

	/**
	 * Log a message with warning level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes two
	 * additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 */
	public void warn(String res, Object param1, Object param2) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.warn(getString(res, param1, param2));
		}
	}

	/**
	 * Log a message with warning level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes array with
	 * parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 */
	public void warn(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.warn(getString(res, params));
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void error(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res));
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * Throwable object.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param error
	 *            the Throwable to be logged
	 */
	public void error(String res, Throwable error) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res), error);
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void error(String res, String param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res, param));
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter and a Throwable object.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 * @param error
	 *            the Throwable to be logged
	 */
	public void error(String res, Object param, Throwable error) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res, param), error);
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes two
	 * additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 */
	public void error(String res, String param1, String param2) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res, param1, param2));
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes array with
	 * parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 */
	public void error(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res, params));
		}
	}

	/**
	 * Log a message with error level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes array with
	 * parameters and a Throwable object.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            an array with parameters for the message
	 * @param error
	 *            the Throwable to be logged
	 */
	public void error(String res, Object[] params, Throwable error) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.error(getString(res, params), error);
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource given.
	 * 
	 * @param res
	 *            the resource used for the message
	 */
	public void fatal(String res) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(getString(res));
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * Throwable object.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param err
	 *            the Throwable to be logged
	 */
	public void fatal(String res, Throwable err) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(getString(res), err);
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 */
	public void fatal(String res, Object param) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(getString(res, param));
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes a
	 * parameter and a Throwable object.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 * @param err
	 *            the Throwable to be logged
	 */
	public void fatal(String res, Object param, Throwable err) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(getString(res, param), err);
		}
	}

	/**
	 * Log a message with fatal level. This is done for all used loggers. The
	 * message is a NLS string formed using the resource and includes the given
	 * parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            parameters for the message
	 */
	public void fatal(String res, Object[] params) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			loggers.fatal(getString(res, params));
		}
	}

	/**
	 * Log a message with the specified level. This is done for all used
	 * loggers.
	 * 
	 * @param level
	 *            The level to use when logging
	 * @param msg
	 *            The string to be logged
	 */
	public void log(String level, String msg) {
		if (!isLoggingEnabled())
			return;
		if (level == null)
			level = "INFO";
		synchronized (loggers) {
			loggers.log(level, msg);
		}
	}

	/**
	 * Throws a new exception with a custom message. The message is a NLS string
	 * formed using the given resource.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @throws Exception
	 *             the thrown exception
	 */
	public void exception(String res) throws Exception {
		throw new Exception(getString(res));
	}

	/**
	 * Throws a new exception with a custom message. The message is a NLS string
	 * formed using the given resource and an additional parameter.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param
	 *            a parameter for the message
	 * @throws Exception
	 *             the thrown exception
	 */
	public void exception(String res, Object param) throws Exception {
		throw new Exception(getString(res, param));
	}

	/**
	 * Throws a new exception with a custom message. The message is a NLS string
	 * formed using the given resource and two additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param param1
	 *            a parameter for the message
	 * @param param2
	 *            a parameter for the message
	 * @throws Exception
	 *             the thrown exception
	 */
	public void exception(String res, Object param1, Object param2)
			throws Exception {
		throw new Exception(getString(res, param1, param2));
	}

	/**
	 * Throws a new exception with a custom message. The message is a NLS string
	 * formed using the given resource and an array of additional parameters.
	 * 
	 * @param res
	 *            the resource used for the message
	 * @param params
	 *            parameters for the message
	 * @throws Exception
	 *             the thrown exception
	 */
	public void exception(String res, Object[] params) throws Exception {
		throw new Exception(getString(res, params));
	}

	/**
	 * Dumps a formatted message to the logfile from the contents of an Entry
	 * 
	 * @param e
	 *            The entry to dump
	 * @see #dump
	 */
	public void dumpEntry(Entry e) {
		if (!isLoggingEnabled())
			return;
		synchronized (loggers) {
			String savePrefix = prefix;
			ResourceHash saveRes = res;
			res = ResourceHash.getHash(PROPERTIESFILE);

			try {
				loggers.info(getString("Entry.dump"));
				prefix = "";
				if (e == null) {
					loggers.info("\tnull");
				} else if (e.isDOMEnabled()){
					for (String s:e.toString().split("\n"))
						loggers.info(s);
				} else {
					loggers.info("\t"
							+ getString("Operation", e.getOperation()));
					loggers.info("\t" + getString("Attributes"));
					String[] names = e.getAttributeNames();
					for (int i = 0; i < names.length; i++) {
						Attribute a = e.getAttribute(names[i]);
						StringBuffer str = new StringBuffer("\t\t");
						str.append(names[i]);
						if (!names[i].equalsIgnoreCase(a.getName())) {
							str.append("[");
							str.append(a.getName());
							str.append("]");
						}
						switch (a.getOper()) {
						case Attribute.ATTRIBUTE_REPLACE:
							str.append(" (");
							str.append(getString("replace"));
							str.append(")");
							break;
						case Attribute.ATTRIBUTE_ADD:
							str.append(" (");
							str.append(getString("add"));
							str.append(")");
							break;
						case Attribute.ATTRIBUTE_DELETE:
							str.append(" (");
							str.append(getString("delete"));
							str.append(")");
							break;
						}
						str.append(":");
						for (int j = 0; j < a.size(); j++) {
							Object val = a.getValue(j);
							if (a.getProtected()) {
								str.append("\t'*****'");
							} else if (val instanceof byte[]) {
								str.append("\t(");
								str.append(UserFunctions
										.encodeToHexstring((byte[]) val));
								str.append(")");
							} else {
								str.append("\t'");
								str.append(val);
								str.append("'");
							}
						}
						loggers.info(str.toString());
					}

					names = e.getPropertyNames();
					if (names.length > 0) {
						loggers.info("\t" + getString("Properties"));
						for (int i = 0; i < names.length; i++) {
							loggers.info("\t\t" + names[i] + ":\t'"
									+ e.getProperty(names[i]) + "'");
						}
					}
				}
			} finally {
				prefix = savePrefix;
				loggers.info(getString("Entry.dump.end"));
				res = saveRes;
			}
		}
	}

	/**
	 * This methods dumps an Object to the log file.
	 * 
	 * @param o
	 *            The entry to dump
	 * @see #dumpEntry
	 */
	public void dump(Object o) {

		if (!isLoggingEnabled())
			return;

		if (o instanceof Entry) {
			dumpEntry((Entry) o);
			return;
		}

		synchronized (loggers) {
			String savePrefix = prefix;
			ResourceHash saveRes = res;
			res = ResourceHash.getHash(PROPERTIESFILE);
			try {
				loggers.info(getString("Object.dump"));
				prefix = "";
				if (o == null) {
					loggers.info("\tnull");
				} else {
					loggers.info("\t" + o.getClass().getName());
					loggers.info("\t" + o.toString());
				}
			} catch (Exception ex) {
				loggers.error("\t", ex);
			} finally {
				prefix = savePrefix;
				loggers.info(getString("Object.dump.end"));
				res = saveRes;
			}
		}
	}

	/**
	 * This method is override so that the Log object will be ignored during
	 * serialization.
	 * 
	 * @param out
	 *            the output stream
	 * @throws IOException
	 *             if there is problem during serialization
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	}

	/**
	 * This method is override so that the Log object will be ignored during
	 * deserialization.
	 * 
	 * @param in
	 *            the input stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
	}

	/**
	 * A collection of loggers.
	 */
	public static class InternalLogger extends Vector<LogInterface> {

		private static final long serialVersionUID = 1344138240644654421L;

		/**
		 * The parent log.
		 */
		private Log parentLog = null;

		/**
		 * Sets the parent log.
		 * 
		 * @param parent
		 *            the new value for the parent log
		 */
		void setParentLog(Log parent) {
			parentLog = parent;
		}

		/**
		 * Checks whether the logger object and this InternalLogger are equal.
		 * 
		 * @param logger
		 *            the object to compare with this object
		 * @return <code>true</code> if they are equal, otherwise
		 *         <code>false</code>
		 */
		public boolean equals(Object logger) {
			return super.equals(logger);
		}

		/**
		 * Returns an integer hash code. Any two objects which answer
		 * <code>true</code> when passed to <code>equals</code> must answer
		 * the same value for this method.
		 * 
		 * @return an integer hash code
		 */
		public int hashCode() {
			return super.hashCode();
		}

		/**
		 * Adds the logger object to the collection of loggers.
		 * 
		 * @param logger
		 *            the object to add
		 */
		public void addLogger(LogInterface logger) {
			if (!contains(logger))
				add(logger);
		}

		/**
		 * Removes a logger from the collection.
		 * 
		 * @param logger
		 *            the object we want to remove
		 */
		public void removeLogger(LogInterface logger) {
			super.remove(logger);
		}

		/**
		 * Logs a message with level debug for each logger in the collection.
		 * 
		 * @param str
		 *            the string to be logged
		 */
		public void debug(String str) {
			if (parentLog != null)
				parentLog.debug(str);
			for (int i = 0; i < size(); i++)
				get(i).debug(str);
		}

		/**
		 * Log a message with level info for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 */
		public void info(String str) {
			if (parentLog != null)
				parentLog.info(str);
			for (int i = 0; i < size(); i++)
				get(i).info(str);
		}

		/**
		 * Log a message with level warning for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 */
		public void warn(String str) {
			if (parentLog != null)
				parentLog.warn(str);
			for (int i = 0; i < size(); i++)
				get(i).warn(str);
		}

		/**
		 * Log a message with level error for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 */
		public void error(String str) {
			if (parentLog != null)
				parentLog.error(str);
			for (int i = 0; i < size(); i++)
				get(i).error(str);
		}

		/**
		 * Log a message with level error, and an additional Throwable. This is
		 * done for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 * @param error
		 *            The Throwable to be logged
		 */
		public void error(String str, Throwable error) {
			if (parentLog != null)
				parentLog.error(str, error);
			for (int i = 0; i < size(); i++)
				get(i).error(str, error);
		}

		/**
		 * Log a message with level fatal for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 */
		public void fatal(String str) {
			if (parentLog != null)
				parentLog.fatal(str);
			for (int i = 0; i < size(); i++)
				get(i).fatal(str);
		}

		/**
		 * Log a message with level fatal, and an additional Throwable. This is
		 * done for each logger in the collection.
		 * 
		 * @param str
		 *            The string to be logged
		 * @param error
		 *            The Throwable to be logged
		 */
		public void fatal(String str, Throwable error) {
			if (parentLog != null)
				parentLog.fatal(str, error);
			for (int i = 0; i < size(); i++)
				get(i).fatal(str, error);
		}

		/**
		 * Log a message with the specified level for each logger in the
		 * collection.
		 * 
		 * @param level
		 *            The level to use when logging
		 * @param str
		 *            The string to be logged
		 */
		public void log(String level, String str) {
			if (parentLog != null)
				parentLog.log(level, str);
			for (int i = 0; i < size(); i++)
				get(i).log(level, str);
		}

		/**
		 * Check if a debug message would be logged by any of the loggers.
		 * 
		 * @return true if a debug message might be logged.
		 */
		public boolean isDebugEnabled() {
			if (parentLog != null && parentLog.isDebugEnabled())
				return true;

			for (int i = 0; i < size(); i++) {
				if (get(i).isDebugEnabled())
					return true;
			}
			return false;
		}

		/**
		 * @deprecated
		 * 
		 * @return a boolean value
		 */
		@Deprecated
		public boolean isEnabledForAll() {
			return isDebugEnabled();
		}

		/**
		 * Free up all resources the loggers use and remove them from the
		 * collection. They will not be called anymore.
		 */
		public void close() {			
			for (int i = 0; i < size(); i++) {				
				get(i).close();
			}
			clear();
		}
	}
}
