/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

/**
 * NT4UserMetaDataException is the custom exception class for throwing
 * exceptions on errors encountered by the Windows Users and Groups Connector.
 */
public class NT4UserMetaDataException extends Exception {
	/**
	 * Required for serialization
	 */
	private static final long serialVersionUID = -3253143411194257754L;
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default constructor.
	 */
	NT4UserMetaDataException() {
		super();
	}

	/**
	 * Constructs the exception object and initializes its error message with
	 * the text given.
	 * 
	 * @param aErrMsg
	 *            The error message that will be set to the exception object.
	 */
	NT4UserMetaDataException(String aErrMsg) {
		super(aErrMsg);
	}

}
