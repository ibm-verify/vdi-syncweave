/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * 
 * Exception type for SAP R/3 Connector.
 * 
 * 
 */
public class FunctionExecutionException extends SapR3ConnectorException {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public FunctionExecutionException(String msg) {
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
	public FunctionExecutionException(Throwable root) {
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
	public FunctionExecutionException(String msg, Throwable root) {
		super(msg, root);
	}

}
