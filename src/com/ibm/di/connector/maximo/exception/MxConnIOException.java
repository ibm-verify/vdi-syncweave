/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's IO exception. Thrown to indicate any sort of
 * communication problem in the connector.
 * 
 * @since 7.1
 */
public class MxConnIOException extends MxConnectorException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	private final String targetUrl;

	/**
	 * Constructs a new {@link MxConnIOException} with the specified detail
	 * message, target URL and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param targetUrl
	 *            target URL that causes the exception
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnIOException(final String msg, final String targetUrl, final Object... msgArgs) {
		super(msg, msgArgs);
		this.targetUrl = targetUrl;
	}

	/**
	 * Constructs a new {@link MxConnIOException} with the specified detail
	 * message, target URL and cause.
	 * 
	 * @param msg
	 *            the detail message
	 * @param targetUrl
	 *            target URL that causes the exception
	 * @param cause
	 *            the cause
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnIOException(final String msg, final String targetUrl, final Throwable cause, final Object... msgArgs) {
		super(msg, cause, msgArgs);
		this.targetUrl = targetUrl;
	}

	/**
	 * Returns the target URL that causes the exception.
	 * 
	 * @return target URL that causes the exception
	 */
	public final String getTargetUrl() {
		return targetUrl;
	}
}
