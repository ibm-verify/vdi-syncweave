/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * Exception type indicating error related to response parsing or processing.
 */
public class MalformedResponseException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	public MalformedResponseException() {
		super();
	}

	/**
	 * @param message
	 */
	public MalformedResponseException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MalformedResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public MalformedResponseException(Throwable cause) {
		super(cause);
	}

}
