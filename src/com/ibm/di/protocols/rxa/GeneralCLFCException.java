/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * Exception type for the Remote Command Line Function Component
 */
public class GeneralCLFCException extends Exception {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The error code for this exception
	 */
	private final transient MsgIds error;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param c
	 *            The RemoteCLFCErrorCode for this exception
	 * @param msg
	 *            The message text.
	 */
	public GeneralCLFCException(MsgIds c, String msg) {
		super(msg);
		error = c;
	}

	/**
	 * Construct an exception of this type with a context message and lower
	 * level exception cause.
	 * 
	 * @param c
	 *            The RemoteCLFCErrorCode for this exception
	 * @param msg
	 *            The message text.
	 * @param root
	 *            The cause of the this exception.
	 */
	public GeneralCLFCException(MsgIds c, String msg, Throwable root) {
		super(msg, root);
		error = c;
	}

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text.
	 */
	public GeneralCLFCException(String msg) {
		super(msg);
		error = null;
	}

	/**
	 * Construct an exception of this type as result of another lower level
	 * exception. The message of this exception will be adopted from the root
	 * exception.
	 * 
	 * @param root
	 *            The cause of the this exception.
	 */
	public GeneralCLFCException(Throwable root) {
		super(root);
		error = null;
	}

	/**
	 * Construct an exception of this type with a context message and lower
	 * level exception cause.
	 * 
	 * @param msg
	 *            The message text.
	 * @param root
	 *            The cause of the this exception.
	 */
	public GeneralCLFCException(String msg, Throwable root) {
		super(msg, root);
		error = null;
	}

	/**
	 * Get the error code for this exception
	 * 
	 * @return RemoteCLFCErrorCode error
	 */
	public MsgIds getCode() {
		return error;
	}

}
