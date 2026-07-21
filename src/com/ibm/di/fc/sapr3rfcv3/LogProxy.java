/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.ibm.di.entry.Entry;

/**
 * Interface for the Log proxy class. This interface matches server.Log. It
 * would have been preferrable to have used one that existed but there appears
 * to be no interface to the server.Log class.
 * 
 */
interface LogProxy {

	void close();

	void debug(java.lang.String res);

	void debug(java.lang.String res, java.lang.Object param);

	void debug(java.lang.String res, java.lang.Object[] params);

	void debug(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/** This methods dumps an Object to the log file. */
	void dump(java.lang.Object o);

	/** Dumps a formatted message to the logfile from the contents of an Entry. */
	void dumpEntry(Entry e);

	void error(java.lang.String res);

	void error(java.lang.String res, java.lang.Object[] params);

	void error(java.lang.String res, java.lang.Object[] params,
			java.lang.Throwable error);

	void error(java.lang.String res, java.lang.Object param,
			java.lang.Throwable error);

	void error(java.lang.String res, java.lang.String param);

	void error(java.lang.String res, java.lang.String param1,
			java.lang.String param2);

	void error(java.lang.String res, java.lang.Throwable error);

	// void exception(java.lang.String res);
	//            
	// void exception(java.lang.String res, java.lang.Object param);
	//            
	// void exception(java.lang.String res, java.lang.Object[] params);
	//            
	// void exception(java.lang.String res, java.lang.Object param1,
	// java.lang.Object param2);
	//            
	void fatal(java.lang.String res);

	void fatal(java.lang.String res, java.lang.Object param);

	void fatal(java.lang.String res, java.lang.Object[] params);

	void fatal(java.lang.String res, java.lang.Object param,
			java.lang.Throwable err);

	void fatal(java.lang.String res, java.lang.Throwable err);

	void fine(java.lang.String res);

	void fine(java.lang.String res, java.lang.Object param);

	void fine(java.lang.String res, java.lang.Object[] params);

	void fine(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	/** Return the value of the debug parameter. */
	boolean getDebug();

	/** Returns the prefix to be prepended to all messages. */
	java.lang.String getPrefix();

	/** Return the NLS string given the resource. */
	java.lang.String getString(java.lang.String resource);

	/** Return the NLS string given the resource and a parameter. */
	java.lang.String getString(java.lang.String resource, java.lang.Object param);

	/** Return the NLS string given the resource and an array of parameters. */
	java.lang.String getString(java.lang.String resource,
			java.lang.Object[] params);

	/** Return the NLS string given the resource and two parameters. */
	java.lang.String getString(java.lang.String resource,
			java.lang.Object param1, java.lang.Object param2);

	void info(java.lang.String res);

	void info(java.lang.String res, java.lang.Object param);

	void info(java.lang.String res, java.lang.Object[] params);

	void info(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

	void log(java.lang.String level, java.lang.String msg);

	void logdebug(java.lang.String msg);

	void logerror(java.lang.String msg);

	void logerror(java.lang.String msg, java.lang.Throwable error);

	void logfatal(java.lang.String msg);

	/** Logs a message to the output stream. */
	void logfine(java.lang.String msg);

	void loginfo(java.lang.String msg);

	void logwarn(java.lang.String msg);

	/** Sets debug parameter. */
	void setDebug(boolean debug);

	// void setLogger(org.apache.log4j.Logger logger);

	/** Sets a prefix to be prepended to all messages. */
	void setPrefix(java.lang.String prefix);

	void warn(java.lang.String res);

	void warn(java.lang.String res, java.lang.Object param);

	void warn(java.lang.String res, java.lang.Object[] params);

	void warn(java.lang.String res, java.lang.Object param1,
			java.lang.Object param2);

}
