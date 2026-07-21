/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

/**
 * Record a problem with the parameters passed to the SapR3RfcFCV3 class.
 */
public class SapR3FCParameterException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param message
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public SapR3FCParameterException(String message) {
		super(message);
	}

	/**
	 * Construct an exception of this type as result of another lower level
	 * exception. The message of this exception will be adopted from the root
	 * exception.
	 * 
	 * @param cause
	 *            The cause of the this exception.
	 */
	public SapR3FCParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct an exception of this type with a context message and lower
	 * level exception cause.
	 * 
	 * @param message
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 * @param cause
	 *            The cause of the this exception.
	 */
	public SapR3FCParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}
