/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

import com.ibm.di.server.Log;

/**
 * Factory for TDI SLF4J Loggers.
 * 
 * @since 7.1
 */
public class TDILoggerFactory implements ILoggerFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * Cache created loggers. Access to this cache must be thread-safe.
	 */
	private Map<String, Logger> loggerCache = new HashMap<String, Logger>();

	/**
	 * {@inheritDoc}
	 */
	public synchronized Logger getLogger(String name) {
		Logger logger = loggerCache.get(name);
		if (logger == null) {
			Log tdiLog = new Log(name);
			logger = new TDILogger(tdiLog, name);
			loggerCache.put(name, logger);
		}
		return logger;
	}
}
