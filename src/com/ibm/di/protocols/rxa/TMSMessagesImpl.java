/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.di.server.ResourceHash;

/**
 * The default implementation of {@link Messages}. This implementation is a
 * wrapper for java.util.ResourceBundle strings.
 * 
 */
public final class TMSMessagesImpl implements Messages {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private ResourceHash bundle = null;

	/**
	 * Failure message
	 */
	private static final String FAILURE_MSG = "Failed to load bundle ";

	/**
	 * Loads the ResourceBundle identified by resourceName.
	 * 
	 * @param resourceName
	 *            A .properties or .class loadable resource bundle.
	 */
	public TMSMessagesImpl(String resourceName) {
		super();
		bundle = new ResourceHash(resourceName);
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
	public String getMessage(String id, Object[] args) {
		return bundle.getString(id, args);
	}

	/**
	 * Get the globalized message for the given String identifier.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	public String getMessage(String id) {
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
	public String getMessage(MessageID id, Object[] args) {
		return getMessage(id.toString(), args);
	}

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	public String getMessage(MessageID id) {
		return getMessage(id.toString());
	}
}
