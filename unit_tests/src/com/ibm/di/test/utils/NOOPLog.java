
package com.ibm.di.test.utils;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

public class NOOPLog extends Log {
	
	public NOOPLog() {
		super("NOOP Log");
	}
	
	@Override
	public void logfine(String msg) {
	}

	@Override
	public void logdebug(String msg) {
	}

	@Override
	public void loginfo(String msg) {
	}

	@Override
	public void logwarn(String msg) {
	}

	@Override
	public void logerror(String msg) {
	}

	@Override
	public void logerror(String msg, Throwable error) {
	}

	@Override
	public void logfatal(String msg) {
	}

	@Override
	public void fine(String res) {
	}

	@Override
	public void fine(String res, Object param) {
	}

	@Override
	public void fine(String res, Object param1, Object param2) {
	}

	@Override
	public void fine(String res, Object[] params) {
	}

	@Override
	public void debug(String res) {
	}

	@Override
	public void debug(String res, Object param) {
	}

	@Override
	public void debug(String res, Object param1, Object param2) {
	}

	@Override
	public void debug(String res, Object[] params) {
	}

	@Override
	public void info(String res) {
	}

	@Override
	public void info(String res, Object param) {
	}

	@Override
	public void info(String res, Object param1, Object param2) {
	}

	@Override
	public void info(String res, Object[] params) {
	}

	@Override
	public void warn(String res) {
	}

	@Override
	public void warn(String res, Object param) {
	}

	@Override
	public void warn(String res, Object param1, Object param2) {
	}

	@Override
	public void warn(String res, Object[] params) {
	}

	@Override
	public void error(String res) {
	}

	@Override
	public void error(String res, Throwable error) {
	}

	@Override
	public void error(String res, String param) {
	}

	@Override
	public void error(String res, Object param, Throwable error) {
	}

	@Override
	public void error(String res, String param1, String param2) {
	}

	@Override
	public void error(String res, Object[] params) {
	}

	@Override
	public void error(String res, Object[] params, Throwable error) {
	}

	@Override
	public void fatal(String res) {
	}

	@Override
	public void fatal(String res, Throwable err) {
	}

	@Override
	public void fatal(String res, Object param) {
	}

	@Override
	public void fatal(String res, Object param, Throwable err) {
	}

	@Override
	public void fatal(String res, Object[] params) {
	}

	@Override
	public void log(String level, String msg) {
	}

	@Override
	public void dumpEntry(Entry e) {
	}

	@Override
	public void dump(Object o) {
	}
}
