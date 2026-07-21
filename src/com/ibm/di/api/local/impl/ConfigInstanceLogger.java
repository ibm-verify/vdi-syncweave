/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ConfigInstanceListener;
import com.ibm.di.server.Log;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.local.LogListener;
import com.ibm.di.log.LogInterface;

/**
 * <p>
 * Adapts a log listener from the Server API layer to a collection of loggers in
 * the Server layer. This collection comprises one logger for the config
 * instance and one logger for each of the AssemblyLine instance of this config
 * instance.
 * </p>
 * <p>
 * Must be registered as a listener of the config instance.
 * </p>
 * 
 * @since 7.0
 */
class ConfigInstanceLogger implements ConfigInstanceListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Map log object to the logger which we have put into that log.
	 */
	private Map<Log, LogInterface> loggersMap = new HashMap<Log, LogInterface>();

	/**
	 * Server API log listener.
	 */
	private LogListener logListener;

	/**
	 * Constructor. The object must be registered as a listener of a config
	 * instance so that it can intercept logged messages.
	 * 
	 * @param logListener
	 *            Server API log listener, which will receive the messages
	 *            logged by the config instance and its AssemblyLines.
	 */
	public ConfigInstanceLogger(LogListener logListener) {
		this.logListener = logListener;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineStarted(AssemblyLine assemblyLine) {
		attachToLog(assemblyLine.getLog());
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineStopped(AssemblyLine assemblyLine) {
		detachFromLog(assemblyLine.getLog());
	}

	/**
	 * {@inheritDoc}
	 */
	public void configInstanceStarted(RSInterface configInstance) {
		attachToLog(configInstance.getLog());
	}

	/**
	 * {@inheritDoc}
	 */
	public void configInstanceStopped(RSInterface configInstance) {
		detachFromLog(configInstance.getLog());
	}

	/**
	 * Remove all associated loggers from the Server layer.
	 */
	public void close() {
		synchronized (loggersMap) {
			for (Map.Entry<Log, LogInterface> e : loggersMap.entrySet()) {
				e.getKey().removeLogger(e.getValue());
			}
			loggersMap.clear();
		}
	}

	/**
	 * @param parentLog
	 *            The parent log, which will contain the created logger.
	 * @return Server layer logger, which redirects messages to
	 *         {@link #logListener}
	 */
	private LogInterface createLogger(Log parentLog) {
		LogInterface logger = new LogListenerAdapter(logListener, parentLog, LogListenerAdapter.CI_LOG_MSG_FORMAT);
		try {
			logger.setCategory(parentLog.getCategory());
		} catch (Exception ex) {
			APIEngine.logError(ex.toString(), ex);
		}
		return logger;
	}

	/**
	 * Create and attach logger to the specified log.
	 * 
	 * @param log
	 *            Log object.
	 */
	private void attachToLog(Log log) {
		if (log == null) {
			return;
		}
		LogInterface logger = createLogger(log);
		log.addLogger(logger);
		synchronized (loggersMap) {
			loggersMap.put(log, logger);
		}
	}

	/**
	 * Remove the attached logger from the specified log.
	 * 
	 * @param log
	 *            Log object.
	 */
	private void detachFromLog(Log log) {
		if (log == null) {
			return;
		}
		LogInterface logger;
		synchronized (loggersMap) {
			logger = loggersMap.remove(log);
		}
		if (logger != null) {
			log.removeLogger(logger);
		}
	}

	// Note: Local API is pushing local listeners and we need to be able to
	// recognize the same objects without persisting maps for loggers of
	// different layers. Here is why we are making sure the adapters are
	// identical if the local listeners they adapt from are also identical.
	@Override
	public int hashCode() {
		return logListener.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ConfigInstanceLogger && logListener.equals(((ConfigInstanceLogger) o).logListener);
	}
}
