/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

/**
 * This is the base exception thrown by the TDI Server API, indicating that
 * something in the API work-flow went wrong.
 */
public class DIException extends Exception {

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -7033777031276603149L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default constructor with no cause message defined.
	 */
	public DIException() {
		super();
	}

	/**
	 * {@link DIException} constructor that creates an exception with predefined
	 * cause message.
	 * 
	 * @param aMessage
	 *            the cause message
	 */
	public DIException(String aMessage) {
		super(aMessage);
	}

	/**
	 * {@link DIException} constructor that creates an exception with predefined
	 * cause exception.
	 * 
	 * @param t
	 *            the cause exception
	 */
	public DIException(Throwable t) {
		super(t);
	}

	/**
	 * {@link DIException} constructor that creates an exception with predefined
	 * cause message and exception.
	 * 
	 * @param msg
	 *            the cause message
	 * @param t
	 *            the cause exception
	 */
	public DIException(String msg, Throwable t) {
		super(msg, t);
	}

}
