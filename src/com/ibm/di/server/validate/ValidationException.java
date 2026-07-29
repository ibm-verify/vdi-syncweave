/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server.validate;

/**
 * Exception that show problem during validation.
 */
public class ValidationException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5322107684294652301L;

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 */
	public ValidationException() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            to be set.
	 * @param cause
	 *            for the exception.
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Set message of the exception.
	 * 
	 * @param message
	 *            to be set.
	 */
	public ValidationException(String message) {
		super(message);
	}

	/**
	 * Set exception that occurs the problem.
	 * 
	 * @param cause
	 *            for the exception.
	 */
	public ValidationException(Throwable cause) {
		super(cause);
	}

}