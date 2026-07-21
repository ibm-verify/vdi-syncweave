/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * This class binds the LoggerFactory of SLF4J with the TDI logging
 * implementation.
 * 
 * @since 7.1
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * This field is required by the SLF4J API.
	 */
	public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

	/**
	 * This method is required by the SLF4J API.
	 */
	public static final StaticLoggerBinder getSingleton() {
		return SINGLETON;
	}

	/**
	 * Declare the version of the SLF4J API this implementation is compiled
	 * against. The value of this field is usually modified with each release.
	 */
	// to avoid constant folding by the compiler, this field must *not* be final
	public static String REQUESTED_API_VERSION = "1.7.36"; // !final

	private static final String loggerFactoryClassStr = TDILoggerFactory.class.getName();

	private final ILoggerFactory loggerFactory = new TDILoggerFactory();

	private StaticLoggerBinder() {
	}

	/**
	 * {@inheritDoc}
	 */
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLoggerFactoryClassStr() {
		return loggerFactoryClassStr;
	}
}
