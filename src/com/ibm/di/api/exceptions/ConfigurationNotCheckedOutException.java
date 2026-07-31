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
 * An exception object thrown when an attempt is made to check in a
 * configuration that was not previously checked out.
 */
public class ConfigurationNotCheckedOutException extends DIException {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -4634830843134452015L;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor that creates an exception with a predefined cause
	 * message.
	 */
	public ConfigurationNotCheckedOutException() {
		super(sResHash
				.getString("SEVER.API.CONFIGURATION.IS.NOT.CHECKED.OUT.3"));
	}

	/**
	 * A constructor for this object that gives the ability to set a custom
	 * cause message.
	 * 
	 * @param aMessage
	 *            the custom cause message.
	 */
	public ConfigurationNotCheckedOutException(String aMessage) {
		super(aMessage);
	}
}
