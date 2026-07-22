/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

/**
 * ConnectorMethodException
 * 
 * Exception type for SAP R/3 User Registry Connector. Thrown by concrete impls
 * of {@link com.ibm.di.connector.sapr3.user.ConnectorMethod} to indicate method
 * execution exceptions.
 * 
 * 
 */
public class ConnectorMethodException extends UserRegistryConnectorException {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Construct exception with a context message.
	 * 
	 * @param msg
	 *            The message text. This text should ideally be retrieved from
	 *            an I18N message bundle.
	 */
	public ConnectorMethodException(String msg) {
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
	public ConnectorMethodException(Throwable root) {
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
	public ConnectorMethodException(String msg, Throwable root) {
		super(msg, root);
	}

}
