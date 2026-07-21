/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's type convertion exception. Thrown to indicate type
 * convertion problem in the connector.
 * 
 * @since 7.1
 */
public class MxConnTypeConvertionException extends MxConnSchemaException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	/**
	 * Constructs a new {@link MxConnTypeConvertionException} with the specified
	 * detail message and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnTypeConvertionException(final String msg, final Object... msgArgs) {
		super(msg, msgArgs);
	}

	/**
	 * Constructs a new {@link MxConnTypeConvertionException} with the specified
	 * detail message, cause, and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the cause
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnTypeConvertionException(final String msg, final Throwable cause, final Object... msgArgs) {
		super(msg, cause, msgArgs);
	}
}
