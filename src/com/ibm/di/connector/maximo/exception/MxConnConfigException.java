/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's configuration exception. Thrown to indicate configuration
 * parameter problems in the connector.
 * 
 * @since 7.1
 */
public class MxConnConfigException extends MxConnectorRuntimeException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private static final long serialVersionUID = 1;

	/**
	 * Constructs a new {@link MxConnConfigException} with the specified detail
	 * message and its arguments.
	 * 
	 * @param msg
	 *            the detail message
	 * @param msgArgs
	 *            arguments used to compound the specified message
	 */
	public MxConnConfigException(final String msg) {
		super(msg);
	}
}
