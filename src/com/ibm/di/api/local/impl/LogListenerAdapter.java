/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import com.ibm.icu.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Log;
import com.ibm.di.log.LogInterface;

/**
 * Adapt the local API's log listener interface to the log interface of the
 * Server.
 * 
 * @since 7.0
 */
public class LogListenerAdapter implements LogInterface {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * <p>
	 * Format for messages logged by an AssemblyLine.
	 * </p>
	 * <p>
	 * Previous versions used to format with org.apache.log4j.PatternLayout with
	 * format string "%d{ISO8601} %-5p [%c] - %m%n". Time-stamp format is like
	 * the one in org.apache.log4j.helpers.ISO8601DateFormat.
	 * </p>
	 * <p>
	 * First place-holder is for the current time. Second place-holder is for
	 * the message severity. Third place-holder is for the category of this
	 * logger. Fourth place-holder is for the raw message.
	 * </p>
	 */
	static final Format AL_LOG_MSG_FORMAT = new MessageFormat("{0,date,HH:mm:ss,SSS} {1} - {3}");

	/**
	 * <p>
	 * Format for messages logged by a ConfigInstance.
	 * </p>
	 * <p>
	 * The config instance log message format has one more field than
	 * {@link #AL_LOG_MSG_FORMAT}. That additional field is the log category.
	 * The log of a single AssemblyLine does not need log category because all
	 * messages are from the same category - the log category of the
	 * AssemblyLine. In comparison, a config instance log may contain messages
	 * from different AssemblyLines and also messages from the config instance
	 * itself. So there needs to be a way to denote where a particular message
	 * comes from.
	 * </p>
	 */
	static final Format CI_LOG_MSG_FORMAT = new MessageFormat("{0,date,HH:mm:ss,SSS} {1} [{2}] - {3}");

	/**
	 * Format for logged messages.
	 */
	private final Format formatter;

	/**
	 * The category of this logger.
	 */
	private String category = "";

	/**
	 * Log listener from the local Server API layer.
	 */
	private com.ibm.di.api.local.LogListener logListener = null;

	/**
	 * Log which contains the current object.
	 */
	private Log log;

	/**
	 * Lock to protect access to the {@link #log} field.
	 */
	private Object logLock = new Object();

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor
	 * 
	 * @param logListener
	 *            Log listener from the remote Server API layer.
	 * @param log
	 *            The Log, which contains this object. Necessary so that we can
	 *            unregister ourselves in case of error.
	 */
	public LogListenerAdapter(com.ibm.di.api.local.LogListener logListener, Log log, Format logMsgFormat) {
		this.logListener = logListener;
		this.log = log;
		this.formatter = logMsgFormat;
	}

	/**
	 * Send a message to the log listener.
	 * 
	 * @param message
	 *            Log message.
	 */
	private void messageLogged(String message) {
		try {
			if (logListener != null) {
				logListener.messageLogged(message);
			}
		} catch (DIException e) {
			APIEngine.logError(sResHash.getString("SEVER.API.EXCEPTION.CAUGHT.ON.MESSAGELOGGED", e.toString()));
		} catch (Throwable t) {
			/*
			 * Unrecoverable error - do not forward any more messages to the
			 * listener. If we try to unregister the logger from here directly,
			 * it will cause a deadlock, so spawn a thread to do it.
			 */
			logListener = null;
			final LogInterface logger = this;
			final String errorMsg = t.toString();
			new Thread(new Runnable() {
				public void run() {
					synchronized (logLock) {
						if (log != null) {
							log.removeLogger(logger);
						}
					}
					APIEngine.logWarn(sResHash.getString("SERVER.API.LOG.LISTENER.UNEXPECTED.ERROR", errorMsg));
				}
			}).start();
		}
	}

	/**
	 * Sets the containing Log. Can be invoked by different threads.
	 * 
	 * @param log
	 *            The containing Log.
	 */
	void setLog(Log log) {
		synchronized (logLock) {
			this.log = log;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAppender(LogConfigItem config, Map params) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String str) {
		log("DEBUG", str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String str) {
		log("ERROR", str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String str, Throwable error) {
		StringWriter sw =new StringWriter();                                   
		error.printStackTrace(new PrintWriter(sw));                            
		error(str + "\n" + sw.toString());  

	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(String str) {
		log("FATAL", str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(String str, Throwable error) {
		fatal(str + " " + error.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String str) {
		log("INFO ", str);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDebugEnabled() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(String level, String str) {
		String formattedStr = formatter.format(new Object[] { new Date(), level, category, str });
		messageLogged(formattedStr);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCategory(String category) throws Exception {
		this.category = category;
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String str) {
		log("WARN ", str);
	}
}
