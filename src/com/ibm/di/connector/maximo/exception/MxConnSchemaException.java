/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's schema exception. Thrown to indicate any sort of
 * schema problem in the connector.
 * 
 * @since 7.1
 */
public class MxConnSchemaException extends MxConnectorException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	/**
	 * Constructs a new {@link MxConnSchemaException} with the specified detail
	 * message and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnSchemaException(final String msg, final Object... msgArgs) {
		super(msg, msgArgs);
	}

	/**
	 * Constructs a new {@link MxConnSchemaException} with the specified detail
	 * message, cause, and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the cause
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnSchemaException(final String msg, final Throwable cause, final Object... msgArgs) {
		super(msg, cause, msgArgs);
	}
}
