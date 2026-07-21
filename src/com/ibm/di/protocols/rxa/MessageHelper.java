/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * Compile time constants for message bundle keys.
 */
public final class MessageHelper {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file containing the message details
	 */
	static final String MESSAGE_PROPERTIES_NAME = "RemoteCLFCmsgs";

	/**
	 * Messages object containing the messages for the Remote Command Line
	 * Function Component
	 */
	private static final Messages MESSAGE_RESOURCE = new TMSMessagesImpl(
			MESSAGE_PROPERTIES_NAME);

	/**
	 * Get a reference to the loaded messages resources.
	 * 
	 * @return Messages The loaded messages
	 */
	public static Messages getMsgResource() {
		return MESSAGE_RESOURCE;
	}

}
