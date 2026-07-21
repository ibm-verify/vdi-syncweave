/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * Represents message strings that can be substituted with runtime context
 * information.
 */
public interface Messages {

	/**
	 * Interface for a message ID
	 * 
	 */
	public interface MessageID {
	};

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
	String getMessage(String id, Object[] args);

	/**
	 * Get the globalized message for the given String identifier.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	String getMessage(String id);

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested message.
	 * @param args
	 *            Substitutable arguments to be inserted into the result string.
	 * @return The message associated with the id. The args will be substitued
	 *         into the result string.
	 */
	String getMessage(MessageID id, Object[] args);

	/**
	 * Get the globalized message for the given MessageID.
	 * 
	 * @param id
	 *            Identifier of the requested string.
	 * @return The message associated with the id.
	 */
	String getMessage(MessageID id);
}
