/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * Exception type indicating the password policy subsystem failed to
 * initialization, e.g. open network connection.
 */
public class PolicyInitializationException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * Default exception.
	 */
	public PolicyInitializationException() {
		super();
	}

	/**
	 * Exception with message.
	 * 
	 * @param message
	 */
	public PolicyInitializationException(String message) {
		super(message);
	}

	/**
	 * Exception with message and root cause reference.
	 * 
	 * @param message
	 * @param cause
	 */
	public PolicyInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Exception with root cause reference only.
	 * 
	 * @param cause
	 */
	public PolicyInitializationException(Throwable cause) {
		super(cause);
	}

}
