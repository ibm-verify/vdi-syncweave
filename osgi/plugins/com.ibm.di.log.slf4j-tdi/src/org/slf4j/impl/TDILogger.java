/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.FormattingTuple;

import com.ibm.di.server.Log;

/**
 * SLF4J Logger. It is an adapter which delegates to a TDI log object.
 * 
 * @since 7.1
 */
public class TDILogger extends MarkerIgnoringBase {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for serialization.
	 */
	private static final long serialVersionUID = -558413730709922429L;

	/**
	 * TDI log object.
	 */
	private Log log;

	/**
	 * @param log
	 *            TDI log object.
	 * @param name
	 *            Logger name.
	 */
	public TDILogger(Log log, String name) {
		this.log = log;
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isTraceEnabled() {
		// no trace support right now
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void trace(String msg) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void trace(String format, Object param1) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void trace(String format, Object param1, Object param2) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void trace(String format, Object[] argArray) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void trace(String msg, Throwable t) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String msg) {
		log.logdebug(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String format, Object param1) {
		FormattingTuple msgStr = MessageFormatter.format(format, param1);
		log.logdebug(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String format, Object param1, Object param2) {
		FormattingTuple msgStr = MessageFormatter.format(format, param1, param2);
		log.logdebug(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String format, Object[] argArray) {
		FormattingTuple msgStr = MessageFormatter.arrayFormat(format, argArray);
		log.logdebug(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled()) {
			// no other way to log Throwable at the moment, so use error
			log.logerror(msg, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInfoEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String msg) {
		log.loginfo(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String format, Object arg) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg);
		log.loginfo(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String format, Object arg1, Object arg2) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg1, arg2);
		log.loginfo(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String format, Object[] argArray) {
		FormattingTuple msgStr = MessageFormatter.arrayFormat(format, argArray);
		log.loginfo(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(String msg, Throwable t) {
		if (isInfoEnabled()) {
			// no other way to log Throwable at the moment, so use error
			log.logerror(msg, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isWarnEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String msg) {
		log.logwarn(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String format, Object arg) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg);
		log.logwarn(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String format, Object arg1, Object arg2) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg1, arg2);
		log.logwarn(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String format, Object[] argArray) {
		FormattingTuple msgStr = MessageFormatter.arrayFormat(format, argArray);
		log.logwarn(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled()) {
			// no other way to log Throwable at the moment, so use error
			log.logerror(msg, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isErrorEnabled() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String msg) {
		log.logerror(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String format, Object arg) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg);
		log.logerror(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String format, Object arg1, Object arg2) {
		FormattingTuple msgStr = MessageFormatter.format(format, arg1, arg2);
		log.logerror(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String format, Object[] argArray) {
		FormattingTuple msgStr = MessageFormatter.arrayFormat(format, argArray);
		log.logerror(msgStr.getMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(String msg, Throwable t) {
		log.logerror(msg, t);
	}

}
