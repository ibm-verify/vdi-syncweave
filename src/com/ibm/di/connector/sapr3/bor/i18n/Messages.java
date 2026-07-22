/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor.i18n;

/**
 * Represents an I18N message strings that can be substituted with runtime
 * context information.
 */

public interface Messages {

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
	String getMessage(String id, Object[] args);

	/**
	 * Get the globalized message for the given id.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	String getMessage(String id);
}
