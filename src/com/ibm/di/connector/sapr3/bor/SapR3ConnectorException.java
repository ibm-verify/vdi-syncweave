/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * Base exception type for SAP R/3 Connector. IBM Tivoli Directory Integrator assembly lines should expect
 * to handle this type.
 */
public class SapR3ConnectorException extends Exception {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public SapR3ConnectorException(String msg) {
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
	public SapR3ConnectorException(Throwable root) {
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
	public SapR3ConnectorException(String msg, Throwable root) {
		super(msg, root);
	}

}
