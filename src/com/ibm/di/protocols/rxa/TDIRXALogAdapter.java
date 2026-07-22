/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.di.server.Log;
import com.ibm.tivoli.remoteaccess.log.Level;
import com.ibm.tivoli.remoteaccess.log.LoggingAdapter;

/**
 * This class encapsulates the RXA internal logger
 */
public class TDIRXALogAdapter extends LoggingAdapter {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Log object to be used for logging
	 */
	private Log log;

	/**
	 * String for maximum level of debugging
	 */
	public static final String DEBUG_MAX = "debug_max";

	/**
	 * String for middle level of debugging
	 */
	public static final String DEBUG_MID = "debug_mid";

	/**
	 * String for minimum level of debugging
	 */
	public static final String DEBUG_MIN = "debug_min";

	/**
	 * String for info
	 */
	public static final String INFO = "info";

	/**
	 * String for warn
	 */
	public static final String WARN = "warn";

	/**
	 * String for error
	 */
	public static final String ERROR = "debug";

	/**
	 * Specifies if log is enabled
	 */
	public boolean logEnabled = false;

	/**
	 * @param log
	 *            Reference to TDI Logger.
	 */
	public TDIRXALogAdapter(Log log) {
		super();
		this.log = log;
		logEnabled = false;
	}

	/**
	 * Hidden so log object will always be initialized.
	 */
	private TDIRXALogAdapter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see com.ibm.tivoli.remoteaccess.log.Logger#text(com.ibm.tivoli.remoteaccess.log.Level,
	 *      java.lang.Object, java.lang.String, java.lang.String,
	 *      java.lang.Object[])
	 */
	public void text(Level level, Object loggingClass, String loggingMethod,
			String text, Object[] inserts) {
		if (!isLogEnabled()) {
			return;
		}
		if(log == null || MessageHelper.getMsgResource() == null)
		{
			return;
		}
		if (null != inserts) {
			for (int i = 0; i < inserts.length; i++) {
				String INSERT_MARKER = "{" + i + "}";
				if (inserts[i] != null && inserts[i].toString().length() > 0) {
					int start = text.indexOf(INSERT_MARKER);
					if (text.indexOf(INSERT_MARKER) != -1) {
						String begin = text.substring(0, start);
						String end = text.substring(start
								+ INSERT_MARKER.length());
						text = begin + inserts[i].toString() + end;
					}
				}
			}
		}
		if (level.equals(Level.DEBUG_MAX) || level.equals(Level.DEBUG_MID)
				|| level.equals(Level.DEBUG_MIN)) {
			log.logdebug(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_DEBUG_MSG,
					new Object[] { loggingMethod, text }));
		} else if (level.equals(Level.INFO)) {
			log.loginfo(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_INFO_MSG,
					new Object[] { loggingMethod, text }));
		} else if (level.equals(Level.WARN)) {
			log.logwarn(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_WARN_MSG,
					new Object[] { loggingMethod, text }));
		} else if (level.equals(Level.ERROR)) {
			log.logerror(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_ERROR_MSG,
					new Object[] { loggingMethod, text }));
		}
	}

	/**
	 * @see com.ibm.tivoli.remoteaccess.log.Logger#exception(com.ibm.tivoli.remoteaccess.log.Level,
	 *      java.lang.Object, java.lang.String, java.lang.Throwable,
	 *      java.lang.String)
	 */
	public void exception(Level level, Object loggingClass,
			String loggingMethod, Throwable throwable, String text) {
		if (!isLogEnabled()) {
			return;
		}
		if(log == null || MessageHelper.getMsgResource() == null)
		{
			return;
		}
		String msg = loggingClass.getClass().getName() + "." + loggingMethod
				+ "(): Throwable Message: " + throwable.getMessage() + " :"
				+ text;
		if (level.equals(Level.DEBUG_MAX) || level.equals(Level.DEBUG_MID)
				|| level.equals(Level.DEBUG_MIN)) {
			log.logdebug(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_DEBUG_MSG,
					new Object[] { loggingMethod, msg }));
		} else if (level.equals(Level.INFO)) {
			log.loginfo(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_INFO_MSG,
					new Object[] { loggingMethod, msg }));
		} else if (level.equals(Level.WARN)) {
			log.logwarn(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_WARN_MSG,
					new Object[] { loggingMethod, msg }));
		} else if (level.equals(Level.ERROR)) {
			log.logerror(MessageHelper.getMsgResource().getMessage(
					MsgIds.RXA_ERROR_MSG,
					new Object[] { loggingMethod, msg }));
		}
	}

	/**
	 * @return Returns the log.
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * @param log
	 *            The log to set.
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	/**
	 * @see com.ibm.tivoli.remoteaccess.log.Logger#isLoggable(com.ibm.tivoli.remoteaccess.log.Level)
	 */
	public boolean isLoggable(Level level) {
		if (level.equals(Level.DEBUG_MAX) || level.equals(Level.DEBUG_MID)
				|| level.equals(Level.DEBUG_MIN)) {
			if (log.getDebug())
				return true;
			else
				return false;
		} else if (level.equals(Level.INFO) || level.equals(Level.WARN)
				|| level.equals(Level.ERROR)) {
			return true;
		} else
			return false;
	}

	/**
	 * @return Returns the logEnabled.
	 */
	public boolean isLogEnabled() {
		return logEnabled;
	}

	/**
	 * @param logEnabled
	 *            The logEnabled to set.
	 */
	public void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	/**
	 * @param logEnabled
	 *            The logEnabled to set.
	 */
	public void setLogEnabled(String logEnabled) {
		if (logEnabled.equalsIgnoreCase("true")) {
			this.logEnabled = true;
		} else {
			this.logEnabled = false;
		}
	}
}
