/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This class was used for the configuration of the Connector schema. 
 * @deprecated We used SchemaConfig for all schemas now.
 * @see SchemaConfig
 */
public interface ConnectorSchemaConfig extends BaseConfiguration {

	/**
	 * Gets the List of Items names.
	 * 
	 * @return The item value
	 */
	public java.util.List getItemNames();

	/**
	 * @param name
	 *            the name of the schema item
	 * @param create
	 *            a boolean value specifying if the item should be created if it
	 *            does not exist
	 * 
	 * @return The item value
	 */
	public ConnectorSchemaItemConfig getItem(Object name, boolean create);

	/**
	 * Gets the item attribute of the ConnectorSchemaConfig object
	 * 
	 * @param name
	 *            the name of the schema item
	 * 
	 * @return The item value
	 */
	public ConnectorSchemaItemConfig getItem(Object name);

	/**
	 * Deletes the specified schema item.
	 * 
	 * @param name
	 *            the name of the schema item to delete.
	 */
	public void removeItem(Object name);

	/**
	 * Sets the item attribute of the ConnectorSchemaConfig object
	 * 
	 * @param name
	 *            The new item value
	 * @param item
	 *            The new item value
	 */
	public void setItem(Object name, ConnectorSchemaItemConfig item);

	/**
	 * Creates a new schema item.
	 * 
	 * @param name
	 *            the name of the new schema item
	 * 
	 * @return the config object for the new item
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public ConnectorSchemaItemConfig newItem(Object name) throws Exception;
}
