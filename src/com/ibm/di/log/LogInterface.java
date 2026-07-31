/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Log.java
package com.ibm.di.log;

import java.util.Map;

import com.ibm.di.config.interfaces.LogConfigItem;

/**
 * Defines an Interface to new Loggers. Any Logger we use must adhere to this
 * interface. The Implementation must provide a public constructor with no
 * arguments. After construction either the setCategory() or the addAppender()
 * method will be called.
 */

public interface LogInterface {

	public final static String TYPE = "type";
	public final static String NAME = "name";
	public final static String CONFIG_INSTANCE = "configInstance";
	public final static String TIME = "time";

	/**
	 * Set the category for this Logger. This method specifies a category, to
	 * allow a category based configuration.
	 * 
	 * @param category
	 *            The category to use.
	 */
	public void setCategory(String category) throws Exception;

	/**
	 * Add an Appender to the Logger using the given config. Appender is the
	 * log4j name, java.util.logging would call it a Handler. May
	 * throw an Exception if the config does not make sense.<br/> The params
	 * Map may contain these keys to help set up the Appender:
	 * <ul>
	 * <li>TYPE: "AssemblyLine" or ""
	 * <li>NAME: A String with the name of component
	 * <li>CONFIG_INSTANCE: a RSInterface
	 * <li>TIME: a String with the time in milliseconds
	 * </ul>
	 * 
	 * @param config
	 *            The LogConfigItem.
	 * @param params
	 *            Extra information that may be useful/
	 */
	public void addAppender(LogConfigItem config, Map<String,Object> params) throws Exception;

	/**
	 * Log a message with level debug.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void debug(String str);

	/**
	 * Log a message with level info.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void info(String str);

	/**
	 * Log a message with level warning.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void warn(String str);

	/**
	 * Log a message with level error.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void error(String str);

	/**
	 * Log a message with level error, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */

	public void error(String str, Throwable error);

	/**
	 * Log a message with level fatal.
	 * 
	 * @param str
	 *            The string to be logged
	 */

	public void fatal(String str);

	/**
	 * Log a message with level fatal, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */

	public void fatal(String str, Throwable error);

	/**
	 * Log a message with the specified level.
	 * 
	 * @param level
	 *            The level to use when logging.
	 * @param str
	 *            The string to be logged
	 */

	public void log(String level, String str);

	/**
	 * Check if a debug message would be logged.
	 * 
	 * @return true if a debug message might be logged
	 */
	public boolean isDebugEnabled();

	/**
	 * Free up all resources this logger uses. The logger will not be called
	 * anymore.
	 */
	public void close();
}
