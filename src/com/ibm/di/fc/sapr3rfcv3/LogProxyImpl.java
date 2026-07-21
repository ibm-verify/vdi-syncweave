/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Proxy for the Logging framework.
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class LogProxyImpl implements LogProxy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Log log;

	public LogProxyImpl() {
		super();

	}

	public LogProxyImpl(Log log) {
		super();
		setLog(log);
	}

	public void close() {
		if (getLog() != null) {
			getLog().close();
		}
	}

	public void debug(java.lang.String res) {
		if (getLog() != null) {
			getLog().debug(res);
		}
	}

	public void debug(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().debug(res, param);
		}
	}

	public void debug(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().debug(res, params);
		}

	}

	public void debug(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().debug(res, param1, param2);
		}

	}

	/** This methods dumps an Object to the log file. */
	public void dump(java.lang.Object o) {
		if (getLog() != null) {
			getLog().dump(o);
		}
	}

	/** Dumps a formatted message to the logfile from the contents of an Entry. */
	public void dumpEntry(Entry e) {
		if (getLog() != null) {
			getLog().dumpEntry(e);
		}

	}

	public void error(java.lang.String res) {
		if (getLog() != null) {
			getLog().error(res);
		}

	}

	public void error(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().error(res, params);
		}
	}

	public void error(java.lang.String res, java.lang.Object[] params,
			java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, params, error);
		}

	}

	public void error(java.lang.String res, java.lang.Object param,
			java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, param, error);
		}
	}

	public void error(java.lang.String res, java.lang.String param) {
		if (getLog() != null) {
			getLog().error(res, param);
		}

	}

	public void error(java.lang.String res, java.lang.String param1,
			java.lang.String param2) {
		if (getLog() != null) {
			getLog().error(res, param1, param2);
		}

	}

	public void error(java.lang.String res, java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().error(res, error);
		}

	}

	public void fatal(java.lang.String res) {
		if (getLog() != null) {
			getLog().fatal(res);
		}

	}

	public void fatal(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().fatal(res, param);
		}
	}

	public void fatal(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().fatal(res, params);
		}

	}

	public void fatal(java.lang.String res, java.lang.Object param,
			java.lang.Throwable err) {
		if (getLog() != null) {
			getLog().fatal(res, param, err);
		}

	}

	public void fatal(java.lang.String res, java.lang.Throwable err) {
		if (getLog() != null) {
			getLog().fatal(res, err);
		}

	}

	public void fine(java.lang.String res) {
		if (getLog() != null) {
			getLog().fatal(res);
		}

	}

	public void fine(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().fine(res, param);
		}

	}

	public void fine(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().fine(res, params);
		}

	}

	public void fine(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().fine(res, param1, param2);
		}

	}

	/** Return the value of the debug parameter. */
	public boolean getDebug() {
		if (getLog() != null) {
			return getLog().getDebug();
		}
		return false;
	}

	/** Returns the prefix to be prepended to all messages. */
	public java.lang.String getPrefix() {
		if (getLog() != null) {
			return getLog().getPrefix();
		}
		return "";
	}

	/** Return the NLS string given the resource. */
	public java.lang.String getString(java.lang.String resource) {
		if (getLog() != null) {
			return getLog().getString(resource);
		}

		return "";
	}

	/** Return the NLS string given the resource and a parameter. */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object param) {
		if (getLog() != null) {
			return getLog().getString(resource, param);
		}

		return "";
	}

	/** Return the NLS string given the resource and an array of parameters. */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object[] params) {
		if (getLog() != null) {
			return getLog().getString(resource, params);
		}

		return "";

	}

	/** Return the NLS string given the resource and two parameters. */
	public java.lang.String getString(java.lang.String resource,
			java.lang.Object param1, java.lang.Object param2) {
		if (getLog() != null) {
			return getLog().getString(resource, param1, param2);
		}

		return "";

	}

	public void info(java.lang.String res) {
		if (getLog() != null) {
			getLog().info(res);
		}
	}

	public void info(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().info(res, param);
		}
	}

	public void info(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().info(res, params);
		}
	}

	public void info(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().info(res, param1, param2);
		}

	}

	public void log(java.lang.String level, java.lang.String msg) {
		if (getLog() != null) {
			getLog().log(level, msg);
		}
	}

	public void logdebug(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logdebug(msg);
		}
	}

	public void logerror(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logerror(msg);
		}
	}

	public void logerror(java.lang.String msg, java.lang.Throwable error) {
		if (getLog() != null) {
			getLog().logerror(msg, error);
		}
	}

	public void logfatal(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logfatal(msg);
		}
	}

	/** Logs a message to the output stream. */
	public void logfine(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logfine(msg);
		}
	}

	public void loginfo(java.lang.String msg) {
		if (getLog() != null) {
			getLog().loginfo(msg);
		}
	}

	public void logwarn(java.lang.String msg) {
		if (getLog() != null) {
			getLog().logwarn(msg);
		}
	}

	/** Sets debug parameter. */
	public void setDebug(boolean debug) {
		if (getLog() != null) {
			getLog().setDebug(debug);
		}
	}

	/*
	 * public void setLogger(org.apache.log4j.Logger logger) { if (getLog() !=
	 * null) { getLog().setLogger(logger); } }
	 */

	/** Sets a prefix to be prepended to all messages. */
	public void setPrefix(java.lang.String prefix) {
		if (getLog() != null) {
			getLog().setPrefix(prefix);
		}

	}

	public void warn(java.lang.String res) {
		if (getLog() != null) {
			getLog().warn(res);
		}

	}

	public void warn(java.lang.String res, java.lang.Object param) {
		if (getLog() != null) {
			getLog().warn(res, param);
		}
	}

	public void warn(java.lang.String res, java.lang.Object[] params) {
		if (getLog() != null) {
			getLog().warn(res, params);
		}

	}

	public void warn(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2) {
		if (getLog() != null) {
			getLog().warn(res, param1, param2);
		}

	}

	/**
	 * retrievs the log file that we have.
	 * 
	 * @return Log
	 */
	private Log getLog() {
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
