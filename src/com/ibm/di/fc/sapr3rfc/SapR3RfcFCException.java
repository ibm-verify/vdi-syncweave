/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

/**
 * Exception type for SAP R/3 Function Component.
 */
public class SapR3RfcFCException extends Exception {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final SapR3RfcFCErrorCodes error;

	/**
	 * Construct exception with a context.
	 * 
	 * @param code
	 *            a code which can be used to recognise this error
	 * @param message
	 *            a detail message giving details on what has gone wrong
	 */
	public SapR3RfcFCException(SapR3RfcFCErrorCodes code, String message) {
		super(message);
		error = code;
	}

	/**
	 * Construct exception with a context.
	 * 
	 * @param code
	 *            a code which can be used to recognise this error
	 * @param message
	 *            a detail message giving details on what has gone wrong
	 * @param cause
	 *            the real cause of this exception
	 */
	public SapR3RfcFCException(SapR3RfcFCErrorCodes code, String message,
			Throwable cause) {
		super(message, cause);
		error = code;
	}

	/**
	 * Construct exception with a context message.
	 * 
	 * @param message
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public SapR3RfcFCException(String message) {
		super(message);
		error = null;
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
	public SapR3RfcFCException(String message, Throwable cause) {
		super(message, cause);
		error = null;
	}

	/**
	 * Construct an exception of this type as result of another lower level
	 * exception. The message of this exception will be adopted from the root
	 * exception.
	 * 
	 * @param cause
	 *            The cause of the this exception.
	 */
	public SapR3RfcFCException(Throwable cause) {
		super(cause);
		error = null;
	}

	/**
	 * Get the error code subtype.
	 * 
	 * @return SapR3RfcFCErrorCodes the corresponding error code for this
	 *         exception
	 */
	public final SapR3RfcFCErrorCodes getErrorCode() {
		return error;
	}
}
