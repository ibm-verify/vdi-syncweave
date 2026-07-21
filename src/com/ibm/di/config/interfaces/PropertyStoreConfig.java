/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a single Property Store.
 *
 */
public interface PropertyStoreConfig extends BaseConfiguration {

	/**
	 * Connection parameters
	 * 
	 * @return The connectionConfig value
	 */
	public RawConnectorConfig getConnectionConfig();

	/**
	 * Associated Parser configuration
	 * 
	 * @return The parserConfig value
	 */
	public ParserConfig getParserConfig();

	/**
	 * Property name filters
	 */
	public String getNameFilters();

	/**
	 * Property name filters
	 */
	public void setNameFilters(String filters);

	/**
	 * Returns true if store is read-only
	 */
	public boolean getReadOnly();

	/**
	 * Returns true if store is read-only
	 */
	public void setReadOnly(boolean readonly);

	/**
	 * Returns the attribute name used as key in the connector
	 */
	public String getKeyAttribute();

	/**
	 * Sets the attribute name to use as property key in the connector
	 */
	public void setKeyAttribute(String attrname);

	/**
	 * Returns the attribute name used as value in the connector
	 */
	public String getValueAttribute();

	/**
	 * Sets the attribute name to use for the value in the connector
	 */
	public void setValueAttribute(String attrname);

	/**
	 * Returns the cache timeout for in-memory properties (0 == never cache)
	 */
	public int getCacheTimeout();

	/**
	 * Sets the timeout in seconds before a property is considered stale.
	 */
	public void setCacheTimeout(int timeout);

	/**
	 * Returns true if the property data store is read into memory on creation
	 */
	public boolean getInitialLoad();

	/**
	 * Sets the initial load flag (true to load data source into memory)
	 */
	public void setInitialLoad(boolean load);
}
