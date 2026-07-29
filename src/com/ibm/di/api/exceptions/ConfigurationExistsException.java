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
 * An exception object thrown when an error arise while manipulating
 * configuration files.
 */
public class ConfigurationExistsException extends DIException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 3575180918818451066L;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor that creates an exception with a predefined cause
	 * message.
	 */
	public ConfigurationExistsException() {
		super(sResHash.getString("SEVER.API.CONFIGURATION.ALREADY.EXISTS"));
	}

	/**
	 * A constructor for this object that gives the ability to set a custom
	 * cause message.
	 * 
	 * @param aMessage
	 *            the custom cause message.
	 */
	public ConfigurationExistsException(String aMessage) {
		super(aMessage);
	}
}
