/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's root runtime exception. Thrown to indicate any sort of
 * problem in the connector.
 * 
 * @since 7.1
 */
public class MxConnectorRuntimeException extends RuntimeException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	/**
	 * Constructs a new {@link MxConnectorRuntimeException} with the specified
	 * detail message and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @see Exception#Exception(String)
	 */
	public MxConnectorRuntimeException(final String msg) {
		super(msg);
	}

	/**
	 * Constructs a new {@link MxConnectorRuntimeException} with the specified
	 * detail message, its arguments, and cause.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the cause
	 * @see Exception#Exception(String, Throwable)
	 */
	public MxConnectorRuntimeException(final String msg, final Throwable cause) {
		super(msg, cause);
	}
}
