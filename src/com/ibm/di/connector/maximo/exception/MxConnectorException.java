/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's root exception. Thrown to indicate any sort of problem in
 * the connector.
 * 
 * @since 7.1
 */
public class MxConnectorException extends Exception {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private static final long serialVersionUID = 1;

	private final Object[] msgArgs;

	/**
	 * Constructs a new {@link MxConnectorException} with the specified detail
	 * message and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 * @see Exception#Exception(String)
	 */
	public MxConnectorException(final String msg, final Object... msgArgs) {
		super(msg);
		this.msgArgs = msgArgs;
	}

	/**
	 * Constructs a new {@link MxConnectorException} with the specified detail
	 * message, its arguments, and cause.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the cause
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 * @see Exception#Exception(String, Throwable)
	 */
	public MxConnectorException(final String msg, final Throwable cause, final Object... msgArgs) {
		super(msg, cause);
		this.msgArgs = msgArgs;
	}

	public final Object[] getMsgArgs() {
		return msgArgs;
	}
}
