/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's timeout exception. Thrown to indicate the timeout
 * expires before the connection can be estabilished or before there is data
 * available for read.
 * 
 * @since 7.1
 */
public class MxConnTimeoutException extends MxConnIOException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	private final int timeout;

	/**
	 * Constructs a new {@link MxConnTimeoutException}.
	 * 
	 * @param targetUrl
	 *            target URL
	 * @param timeout
	 *            timeout, in milliseconds
	 * @param cause
	 *            the cause
	 */
	public MxConnTimeoutException(final String msg, final String targetUrl, final int timeout, final Throwable cause) {
		super(msg, targetUrl, cause, timeout);
		this.timeout = timeout;
	}

	/**
	 * Returns the timeout, in milliseconds.
	 * 
	 * @return timeout, in milliseconds
	 */
	public int getTimeout() {
		return timeout;
	}
}
