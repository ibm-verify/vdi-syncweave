/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

/**
 * 
 * Exception type for SapAdapter.
 * 
 */
public class SapAdapterException extends Exception {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public SapAdapterException(String msg) {
		super(msg);
	}

	/**
	 * Construct an exception of this type as result of another lower level
	 * exception. The message of this exception will be adopted from the root
	 * exception.
	 * 
	 * @param root
	 *            The cause of the this exception.
	 */
	public SapAdapterException(Throwable root) {
		super(root);
	}

	/**
	 * Construct an exception of this type with a context message and lower
	 * level exception cause.
	 * 
	 * @param msg
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 * @param root
	 *            The cause of the this exception.
	 */
	public SapAdapterException(String msg, Throwable root) {
		super(msg, root);
	}
}
