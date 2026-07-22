/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import com.ibm.di.server.ResourceHash;

/**
 * Wrapper class used to retrieve TMS messages for the TAM Connector. The
 * properties file used for the messages is named "tamconnector".
 */
public final class TMSMessageGetter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String MESSAGE_PROPERTIES_NAME = "tamconnector";

	private static ResourceHash bundle = ResourceHash
			.getHash(MESSAGE_PROPERTIES_NAME);

	/**
	 * Loads the ResourceBundle identified by resourceName.
	 * 
	 */
	private TMSMessageGetter() {
		super();
	}

	/**
	 * Get the globalized message for the given String identifier.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @param args
	 *            Substitutable arguments to be inserted into the result string.
	 * @return The message associated with the id. The args will be substitued
	 *         into the result string.
	 */
	public static String getMessage(String id, Object[] args) {
		return bundle.getString(id, args);
	}

	/**
	 * Get the globalized message for the given String identifier.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @param arg
	 *            Substitutable argument to be inserted into the result string.
	 * @return The message associated with the id. The args will be substitued
	 *         into the result string.
	 */
	public static String getMessage(String id, Object arg) {
		return bundle.getString(id, arg);
	}

	/**
	 * Get the globalized message for the given String identifier.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	public static String getMessage(String id) {
		return bundle.getString(id);
	}

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @param args
	 *            Substitutable arguments to be inserted into the result string.
	 * @return The message associated with the id. The args will be substitued
	 *         into the result string.
	 */
	public static String getMessage(TMSMsgId id, Object[] args) {
		return getMessage(id.toString(), args);
	}

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @param arg
	 *            Substitutable argument to be inserted into the result string.
	 * @return The message associated with the id. The args will be substitued
	 *         into the result string.
	 */
	public static String getMessage(TMSMsgId id, Object arg) {
		return getMessage(id.toString(), arg);
	}

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	public static String getMessage(TMSMsgId id) {
		return getMessage(id.toString());
	}
}
