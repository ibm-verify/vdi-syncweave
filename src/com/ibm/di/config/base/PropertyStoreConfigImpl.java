/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import com.ibm.di.config.interfaces.*;

/**
 * Implements the configuration for a single Property Store.
 *
 */
public class PropertyStoreConfigImpl extends BaseConfigurationImpl implements
		PropertyStoreConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -2620929677558833640L;

	private RawConnectorConfig connectionConfig;

	private ParserConfig parserConfig;

	private transient String displayString = null;

	public PropertyStoreConfigImpl() {
		super();
	}

	public PropertyStoreConfigImpl(Object config) {
		super(config);
	}

	@SuppressWarnings("rawtypes")
	public void init() throws Exception {
		// Raw connector config
		if (connectionConfig == null)
			connectionConfig = new RawConnectorConfigImpl(getParameter(
					InternalSchema.CONNECTOR_CONNECTOR_CONFIG, new TreeMap()));
		connectionConfig.init();
		connectionConfig.setParent(this);

		// Parser config
		if (parserConfig == null)
			parserConfig = new ParserConfigImpl(getParameter(
					InternalSchema.CONNECTOR_PARSER_CONFIG, new TreeMap()));
		parserConfig.init();
		parserConfig.setParent(this);
	}

	/**
	 * Connector driver parameters
	 */
	public RawConnectorConfig getConnectionConfig() {
		return connectionConfig;
	}

	/**
	 * Associated Parser configuration
	 */
	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	/**
	 * Property name filters
	 */
	public String getNameFilters() {
		return getStringParameter("nameFilters");
	}

	/**
	 * Property name filters
	 */
	public void setNameFilters(String filters) {
		setStringParameter("nameFilters", filters);
	}

	/**
	 * Returns true if store is read-only
	 */
	public boolean getReadOnly() {
		return getBooleanParameter("readOnly", false);
	}

	/**
	 * Returns true if store is read-only
	 */
	public void setReadOnly(boolean readonly) {
		setBooleanParameter("readOnly", readonly);
	}

	/**
	 * Returns the attribute name used as key in the connector
	 */
	public String getKeyAttribute() {
		return getStringParameter("keyattr");
	}

	/**
	 * Sets the attribute name to use as property key in the connector
	 */
	public void setKeyAttribute(String attrname) {
		setStringParameter("keyattr", attrname);
	}

	/**
	 * Returns the attribute name used as value in the connector
	 */
	public String getValueAttribute() {
		return getStringParameter("valueattr");
	}

	/**
	 * Sets the attribute name to use for the value in the connector
	 */
	public void setValueAttribute(String attrname) {
		setStringParameter("valueattr", attrname);
	}

	/**
	 * Returns the cache timeout for in-memory properties (0 == never cache)
	 */
	public int getCacheTimeout() {
		return getIntegerParameter("cacheTimeout", 0);
	}

	/**
	 * Sets the timeout in seconds before a property is considered stale.
	 */
	public void setCacheTimeout(int timeout) {
		setIntegerParameter("cacheTimeout", timeout);
	}

	/**
	 * Returns true if the property data store is read into memory on creation
	 */
	public boolean getInitialLoad() {
		return getBooleanParameter("initialLoad", false);
	}

	/**
	 * Sets the initial load flag (true to load data source into memory)
	 */
	public void setInitialLoad(boolean load) {
		setBooleanParameter("initialLoad", load);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		PropertyStoreConfig pc = new PropertyStoreConfigImpl(deepClone(null));
		pc.setName(getName());
		pc.init();
		pc.setMetamergeConfig(getMetamergeConfig());
		pc.setupInheritanceChain();
		pc.setModTS(getModTS());
		return pc;
	}

	/**
	 * Used by the Config Editor to set the name that should be displayed for
	 * this PropertyStoreConfig
	 */
	public void setDisplayString(String name) {
		displayString = name;
	}

	public String toString() {
		if (displayString != null)
			return displayString;
		// return "PropertyStoreConfig (" + getShortName() + ")";
		return getShortName();
	}
}
