/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Interface for the Log proxy class. This interface matches server.Log.
 */

public interface LogProxy {

	/**
	 * Close the output logger
	 */
	void close();

	/**
	 * Logs a debug message
	 * 
	 * @param res
	 *            Text of the message
	 */
	void debug(java.lang.String res);

	/**
	 * Logs a debug message
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void debug(java.lang.String res, java.lang.Object param);

	/**
	 * Logs a debug message
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void debug(java.lang.String res, java.lang.Object[] params);

	/**
	 * Logs a debug message
	 * 
	 * @param res
	 *            Text of the message
	 * @param param1
	 *            First parameter of the message
	 * @param param2
	 *            Second parameter of the message
	 */
	void debug(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/**
	 * This methods dumps an Object to the log file.
	 * 
	 * @param o
	 *            Object
	 */
	void dump(java.lang.Object o);

	/**
	 * Dumps a formatted message to the logfile from the contents of an Entry.
	 * 
	 * @param e
	 *            Entry
	 */
	void dumpEntry(Entry e);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 */
	void error(java.lang.String res);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void error(java.lang.String res, java.lang.Object[] params);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 * @param error
	 *            Throwable error
	 */
	void error(java.lang.String res, java.lang.Object[] params,
			java.lang.Throwable error);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 * @param error
	 *            Throwable error
	 */
	void error(java.lang.String res, java.lang.Object param,
			java.lang.Throwable error);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void error(java.lang.String res, java.lang.String param);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param param1
	 *            first parameters of the message
	 * @param param2
	 *            second parameters of the message
	 */
	void error(java.lang.String res, java.lang.String param1,
			java.lang.String param2);

	/**
	 * Logs an error message
	 * 
	 * @param res
	 *            Text of the message
	 * @param error
	 *            Throwable error
	 */
	void error(java.lang.String res, java.lang.Throwable error);

	/**
	 * Log a message with level fatal
	 * 
	 * @param res
	 *            Text of the message
	 */
	void fatal(java.lang.String res);

	/**
	 * Log a message with level fatal
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void fatal(java.lang.String res, java.lang.Object param);

	/**
	 * Log a message with level fatal
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void fatal(java.lang.String res, java.lang.Object[] params);

	/**
	 * Log a message with level fatal
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 * @param err
	 *            Throwable error
	 */
	void fatal(java.lang.String res, java.lang.Object param,
			java.lang.Throwable err);

	/**
	 * Log a message with level fatal
	 * 
	 * @param res
	 *            Text of the message
	 * @param err
	 *            Throwable error
	 */
	void fatal(java.lang.String res, java.lang.Throwable err);

	/**
	 * Logs a debug message if logging is enabled
	 * 
	 * @param res
	 *            Text of the message
	 */
	void fine(java.lang.String res);

	/**
	 * Logs a debug message if logging is enabled
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void fine(java.lang.String res, java.lang.Object param);

	/**
	 * Logs a debug message if logging is enabled
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void fine(java.lang.String res, java.lang.Object[] params);

	/**
	 * Logs a debug message if logging is enabled
	 * 
	 * @param res
	 *            Text of the message
	 * @param param1
	 *            first parameter of the message
	 * @param param2
	 *            second parameter of the message
	 */
	void fine(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/**
	 * Return the value of the debug parameter.
	 * 
	 * @return the value of the debug parameter
	 */
	boolean getDebug();

	/**
	 * Returns the prefix to be prepended to all messages.
	 * 
	 * @return prefix
	 */
	java.lang.String getPrefix();

	/**
	 * Return the NLS string given the resource.
	 * 
	 * @param resource
	 *            String
	 * @return NLS String
	 */
	java.lang.String getString(java.lang.String resource);

	/**
	 * Return the NLS string given the resource and a parameter.
	 * 
	 * @param resource
	 *            String
	 * @param param
	 *            parameter
	 * @return NLS String
	 */
	java.lang.String getString(java.lang.String resource, java.lang.Object param);

	/**
	 * Return the NLS string given the resource and an array of parameters.
	 * 
	 * @param resource
	 *            String
	 * @param params
	 *            Array of parameters
	 * @return NLS String
	 */
	java.lang.String getString(java.lang.String resource,
			java.lang.Object[] params);

	/**
	 * Return the NLS string given the resource and two parameters.
	 * 
	 * @param resource
	 *            String
	 * @param param1
	 *            first parameter
	 * @param param2
	 *            second parameter
	 * @return NLS String
	 */
	java.lang.String getString(java.lang.String resource,
			java.lang.Object param1, java.lang.Object param2);

	/**
	 * Log a message with level info
	 * 
	 * @param res
	 *            Text of the message
	 */
	void info(java.lang.String res);

	/**
	 * Log a message with level info
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void info(java.lang.String res, java.lang.Object param);

	/**
	 * Log a message with level info
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void info(java.lang.String res, java.lang.Object[] params);

	/**
	 * Log a message with level info
	 * 
	 * @param res
	 *            Text of the message
	 * @param param1
	 *            first parameter of the message
	 * @param param2
	 *            second parameter of the message
	 */
	void info(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/**
	 * Log a message with the specified level
	 * 
	 * @param level
	 *            the level of the message
	 * @param msg
	 *            Text of the message
	 */
	void log(java.lang.String level, java.lang.String msg);

	/**
	 * Log a message with debug level
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void logdebug(java.lang.String msg);

	/**
	 * Log a message with error level
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void logerror(java.lang.String msg);

	/**
	 * Log a message with error level
	 * 
	 * @param msg
	 *            Text of the message
	 * @param error
	 *            Throwable error
	 */
	void logerror(java.lang.String msg, java.lang.Throwable error);

	/**
	 * Log a message with fatal level
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void logfatal(java.lang.String msg);

	/**
	 * Logs a message to the output stream.
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void logfine(java.lang.String msg);

	/**
	 * Logs a message to the output stream.
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void loginfo(java.lang.String msg);

	/**
	 * Logs a message to the output stream.
	 * 
	 * @param msg
	 *            Text of the message
	 */
	void logwarn(java.lang.String msg);

	/**
	 * Sets debug parameter.
	 * 
	 * @param debug
	 *            true or false
	 */
	void setDebug(boolean debug);

	/**
	 * Sets a prefix to be prepended to all messages.
	 * 
	 * @param prefix
	 */
	void setPrefix(java.lang.String prefix);

	/**
	 * Log a message with level warning
	 * 
	 * @param res
	 *            Text of the message
	 */
	void warn(java.lang.String res);

	/**
	 * Log a message with level warning
	 * 
	 * @param res
	 *            Text of the message
	 * @param param
	 *            parameter of the message
	 */
	void warn(java.lang.String res, java.lang.Object param);

	/**
	 * Log a message with level warning
	 * 
	 * @param res
	 *            Text of the message
	 * @param params
	 *            parameters of the message
	 */
	void warn(java.lang.String res, java.lang.Object[] params);

	/**
	 * Log a message with level warning
	 * 
	 * @param res
	 *            Text of the message
	 * @param param1
	 *            first parameter of the message
	 * @param param2
	 *            second parameter of the message
	 */
	void warn(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/**
	 * Gets the server logger
	 * 
	 * @return the Log object
	 */
	Log getLog();

}
