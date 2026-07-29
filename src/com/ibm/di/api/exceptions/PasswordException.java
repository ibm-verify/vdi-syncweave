/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.exceptions;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.server.ResourceHash;

/**
 * The class PasswordException represents the Exception class used when a
 * password protected configuration is opened remotely with a wrong or missing
 * password.
 */
public class PasswordException extends DIException {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -4403048499957050841L;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default Constructor of PasswordException
	 */
	public PasswordException() {
		super(sResHash
				.getString("SERVER.API.MISSING.PASSWORD.FOR.CONFIGURATION"));
	}

	/**
	 * Constructor with parameter of PasswordException
	 * 
	 * @param aMessage
	 *            The error message
	 */
	public PasswordException(String aMessage) {
		super(aMessage);
	}
}
