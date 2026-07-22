/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import java.util.Map;

import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.server.Trace;

import com.ibm.log.*;
import com.ibm.log.mgr.LogManager;
import com.ibm.log.mgr.PropertyFileDataStore;

/**
 * Implements LogInterface for com.ibm.log.Logger
 */

public class TDIJLog implements LogInterface {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private LogEventProducer myLogger;

	private LogManager mgr = null;

	/**
	 * Public constructor
	 */
	public TDIJLog() {
		myLogger = null;
	}

	/**
	 * Set the category.
	 * @param category The category
	 */
	public void setCategory( String category ) {

		
		try {
			mgr = LogManager.getManagerWithMergedDataStore(
					new PropertyFileDataStore(Trace.propFileName), false);
			myLogger = mgr.getLogger(category);
		} catch (Exception e) {
			// No manager available
		}
	}

	/**
	 * This sample code only understand the FileHandler. More code could be
	 * added for other Handlers
	 */
	public void addAppender(LogConfigItem config, Map<String,Object> map) throws Exception {
		if (config == null)
			return; // Throw Exception?

		String strHandler = config.getStringParameter("handler");
		if (strHandler == null)
			return; // Throw Exception?

		Handler handler = null;

		if (strHandler.equals("FileHandler")) {
			FileHandler fh = new FileHandler("", config.getStringParameter("File.File") );
			fh.setMaxFiles(1);
			fh.setAppending( config.getBooleanParameter("File.Append", false) );
			handler = fh;
		}

		// More code for other Handlers could be added here

		if (handler == null)
			return; // Throw Exception ?

		String formatter = config.getStringParameter("com.ibm.di.log.layout");
		if (formatter != null) {
			if (formatter.equals("CBE101")) {
				handler.setFormatter(new CBE101Formatter());
			} else if (formatter.equals("Enhanced")) {
				handler.setFormatter(new EnhancedFormatter());
			}
		}

		if ( myLogger == null )
			myLogger = new LevelLogger();
		myLogger.addLogEventListener(handler);
	}

	/**
	 * Log a message with level debug.
	 * 
	 * @param str
	 *            The string to be logged
	 */
	public void debug(String str) {
		log(Level.DEBUG_MIN, null, str);
	}

	/**
	 * Log a message with level info.
	 * 
	 * @param str
	 *            The string to be logged
	 */
	public void info(String str) {
		log(Level.INFO, null, str);
	}

	/**
	 * Log a message with level warning.
	 * 
	 * @param str
	 *            The string to be logged
	 */
	public void warn(String str) {
		log(Level.WARN, null, str);
	}

	/**
	 * Log a message with level error.
	 * 
	 * @param str
	 *            The string to be logged
	 */
	public void error(String str) {
		log(Level.ERROR, null, str);
	}

	/**
	 * Log a message with level error, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */
	public void error(String str, Throwable error) {
		log(Level.ERROR, error, str);
	}

	/**
	 * Log a message with level fatal.
	 * 
	 * @param str
	 *            The string to be logged
	 */
	public void fatal(String str) {
		log(Level.FATAL, null, str);
	}

	/**
	 * Log a message with level fatal, and an additional Throwable.
	 * 
	 * @param str
	 *            The string to be logged
	 * @param error
	 *            The Throwable to be logged
	 */
	public void fatal(String str, Throwable error) {
		log(Level.FATAL, error, str);
	}

	/**
	 * Log a message with the specified level.
	 * 
	 * @param level
	 *            The level to use when logging.
	 * @param str
	 *            The string to be logged
	 */
	public void log(String level, String str) {
		log(Level.getLevel(level), null, str);
	}

	/**
	 * Try to guess the class and method that issued the logging call
	 */
	private void log(Level level, Throwable err, String str) {
		if (myLogger == null)
			return;

		StackTraceElement[] stack = new Throwable().getStackTrace();
		String logClass = null;
		String method = null;
		for (int i = 0; i < stack.length; i++) {
			logClass = stack[i].getClassName();
			if ( ! "com.ibm.di.log.TDIJLog".equals(logClass) &&
				 ! "com.ibm.di.server.Log".equals(logClass) &&
				 ! "com.ibm.di.server.Log$InternalLogger".equals(logClass) &&
				 ! "java.lang.Throwable".equals(logClass) ) {
				if ( "com.ibm.di.connector.LogConnector".equals(logClass) )
					logClass = null;
				else
					method = stack[i].getMethodName();
				break;
			}
		}

		myLogger.log( new LogEvent(level, logClass, method, err, str,
						  null, null, null, null, null) );
			
	}

	/**
	 * Check if a debug message would be logged.
	 * 
	 * @return true if a debug message might be logged
	 */
	public boolean isDebugEnabled() {
		if (myLogger == null)
			return false;

		if ( myLogger instanceof LevelLoggerSupport )
			return ((LevelLoggerSupport)myLogger).isLoggable(Level.DEBUG_MIN);
		else
			return true;
	}

	/**
	 * Free up all resources this logger uses. The logger will not be called
	 * anymore.
	 */
	public void close() {
		if ( myLogger == null )
			return;
		LogEventListener[] list = myLogger.getLogEventListeners();
		for (int i = 0; i < list.length; i++) {
			list[i].close();
			myLogger.removeLogEventListener( list[i]);
		}
		if ( mgr != null )
			mgr.returnLogger( myLogger );
		myLogger = null;
	}

}
