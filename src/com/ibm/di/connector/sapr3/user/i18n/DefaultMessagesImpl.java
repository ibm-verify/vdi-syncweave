/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user.i18n;

import com.ibm.di.server.ResourceHash;
import java.util.MissingResourceException;

/**
 * The default implementation of {@link Messages}. This implementation is a
 * wrapper for java.util.ResourceBundle strings.
 * 
 */

public final class DefaultMessagesImpl implements Messages {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ResourceHash bundle = null;

	/**
	 * Loads the ResourceBundle identified by resourceName.
	 * 
	 * @param resourceName
	 *            A .properties or .class loadable resource bundle. See
	 *            java.util.ResourceBundle.
	 * @throws MissingResourceException
	 *             If unable to load bundle named by resoureName.
	 */
	public DefaultMessagesImpl(String resourceName)
			throws MissingResourceException {
		super();
		bundle = new ResourceHash(resourceName);
		if (bundle == null) {
			throw new MissingResourceException("Failed to load bundle", this
					.getClass().getName(), resourceName);
		}
	}

	/**
	 * Get the globalized message for the given id.
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
	 * Get the globalized message for the given id.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	public String getMessage(String id) {
		return bundle.getString(id);
	}
}
