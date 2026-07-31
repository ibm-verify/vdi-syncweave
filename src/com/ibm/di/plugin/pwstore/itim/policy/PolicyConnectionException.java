/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * Exception type indicating error related to password policy connection.
 */
public class PolicyConnectionException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	public PolicyConnectionException() {
		super();
	}

	/**
	 * @param message
	 */
	public PolicyConnectionException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PolicyConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public PolicyConnectionException(Throwable cause) {
		super(cause);
	}

}
