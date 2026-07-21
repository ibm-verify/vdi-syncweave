/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

/**
 * 
 * @author yavor.gologanov
 *
 */
public interface ConnectorLog {
	
	/**
	 * Log a message to the connector's log. The message is prefixed by the
	 * connector's name.
	 * 
	 * @param msg
	 *            The message to write to the log
	 */
	public void logmsg(String msg);

	/**
	 * Log a debug message to the connector's log
	 * 
	 * @param msg
	 *            The message to write to the log
	 */
	public void debug(String msg);

	/**
	 * Log an error message to the connector's log
	 * 
	 * @param msg
	 *            The message to write to the log
	 * @since 7.0
	 */
	public void logError(String msg);

	/**
	 *  Log an exception message to the connector's log
	 * 
	 * @param e
	 * 			 The exception to write to the log
	 */
	public void logError(Exception e); 
	
}
