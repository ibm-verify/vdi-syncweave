/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Proxy for the Logging framework.
 * 
 */
public final class LogProxyImpl implements LogProxy {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Server Log object used for the logging
	 */
	private Log log;

	/**
	 * Empty string
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * Constructor of the class
	 */
	public LogProxyImpl() {
		super();
	}

	/**
	 * Constructor with a Log object given
	 * 
	 * @param log
	 *            the server Log object
	 */
	public LogProxyImpl(Log log) {
		super();
		setLog(log);
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() {
		if (getLog() != null) {
			getLog().close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(java.lang.String res) {
		if (getLog() != null) {
			getLog().debug(res);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().debug(res, param);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().debug(res, params);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().debug(res, param1, param2);
		}

	}

	/**
	 * This methods dumps an Object to the log file.
	 * 
	 * @param o
	 *            Object to be dumped
	 */
	public void dump(java.lang.Object o) {
		if (getLog() != null) {
			getLog().dump(o);
		}
	}

	/**
	 * Dumps a formatted message to the logfile from the contents of an Entry.
	 * 
	 * @param e
	 *            Entry to be dumped
	 */
	public void dumpEntry(Entry e) {
		if (getLog() != null) {
			getLog().dumpEntry(e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res) {
		if (getLog() != null) {
			getLog().error(res);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().error(res, params);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.Object[] params,
			java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, params, error);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.Object param,
			java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, param, error);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.String param) {
		if (getLog() != null) {
			getLog().error(res, param);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.String param1,
			java.lang.String param2) {
		if (getLog() != null) {
			getLog().error(res, param1, param2);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void error(java.lang.String res, java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, error);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(java.lang.String res) {
		if (getLog() != null) {
			getLog().fatal(res);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().fatal(res, param);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().fatal(res, params);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(java.lang.String res, java.lang.Object param,
			java.lang.Throwable err) {
		if (getLog() != null) {
			getLog().fatal(res, param, err);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fatal(java.lang.String res, java.lang.Throwable err) {
		if (getLog() != null) {
			getLog().fatal(res, err);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fine(java.lang.String res) {
		if (getLog() != null) {
			getLog().fine(res);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fine(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().fine(res, param);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fine(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().fine(res, params);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void fine(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().fine(res, param1, param2);
		}

	}

	/**
	 * Return the value of the debug parameter.
	 * 
	 * @return true or false
	 */
	public boolean getDebug() {
		if (getLog() != null) {
			return getLog().getDebug();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public java.lang.String getPrefix() {
		return ExceptionFactory.FC_PREFIX;
	}

	/**
	 * {@inheritDoc}
	 */
	public java.lang.String getString(java.lang.String resource) {
		if (getLog() != null) {
			return getLog().getString(resource);
		}

		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object param) {
		if (getLog() != null) {
			return getLog().getString(resource, param);
		}

		return EMPTY_STRING;
	}

	/**
	 * {@inheritDoc}
	 */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object[] params) {
		if (getLog() != null) {
			return getLog().getString(resource, params);
		}

		return EMPTY_STRING;

	}

	/**
	 * {@inheritDoc}
	 */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object param1, java.lang.Object param2) {
		if (getLog() != null) {
			return getLog().getString(resource, param1, param2);
		}

		return EMPTY_STRING;

	}

	/**
	 * {@inheritDoc}
	 */
	public void info(java.lang.String res) {
		if (getLog() != null) {
			getLog().info(res);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().info(res, param);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().info(res, params);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().info(res, param1, param2);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void log(java.lang.String level, java.lang.String msg) {
		if (getLog() != null) {
			getLog().log(level, msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logdebug(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logdebug(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logerror(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logerror(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logerror(java.lang.String msg, java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().logerror(msg, error);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logfatal(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logfatal(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logfine(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logfine(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void loginfo(java.lang.String msg) {
		if (getLog() != null) {
			getLog().loginfo(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logwarn(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logwarn(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDebug(boolean debug) {
		if (getLog() != null) {
			getLog().setDebug(debug);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrefix(java.lang.String prefix) {
		if (getLog() != null) {
			getLog().setPrefix(prefix);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(java.lang.String res) {
		if (getLog() != null) {
			getLog().warn(res);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().warn(res, param);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().warn(res, params);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().warn(res, param1, param2);
		}

	}

	/**
	 * retrieves the log file that we have.
	 * 
	 * @return Log
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * Set the log object we are proxy for.
	 * 
	 * @param log
	 */
	public void setLog(com.ibm.di.server.Log log) {
		this.log = log;
	}
}
