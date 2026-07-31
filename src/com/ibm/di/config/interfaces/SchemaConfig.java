/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * Describes a Schema, the information about which which fields are available
 * or needed when reading from or writing to e.g. a Connector.
 *
 */
public interface SchemaConfig extends BaseConfiguration {

	/**
	 * Gets a List of all of the names.
	 * 
	 * @return The item names in a List.
	 */
	public List<String> getItemNames();

	/**
	 * Gets the item attribute of the SchemaConfig object
	 * 
	 * @param name
	 *            The name of the schema item object
	 * @return The item value
	 */
	public SchemaItemConfig getItem(Object name);

	/**
	 * Removes and item from the schema
	 * 
	 * @param name
	 *            The name of the item to remove
	 */
	public void removeItem(Object name);

	/**
	 * Sets the item attribute of the SchemaConfig object
	 * 
	 * @param name
	 *            The new item value
	 * @param item
	 *            The new item value
	 */
	public void setItem(Object name, SchemaItemConfig item);

	/**
	 * Constructs a new SchemaItemConfig object
	 * 
	 * @param name
	 *            The name of the new Object
	 * @return The new constructed object
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public SchemaItemConfig newItem(Object name) throws Exception;
}
