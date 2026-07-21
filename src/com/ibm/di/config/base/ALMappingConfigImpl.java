/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;

/**
 * This is the implementation class for the configuration of an AssemblyLine Attribute Map Component.
 */
public class ALMappingConfigImpl extends ConnectorConfigImpl implements
		ALMappingConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 2712493657450710788L;

	/**
	 * Constructor
	 * 
	 * @throws Exception
	 */
	public ALMappingConfigImpl() throws Exception {
		super();
	}

	/**
	 * Constructor providing a TreeMap of attribute/value pairs.
	 * 
	 * @param data
	 *            initial data for attributes
	 * 
	 * @throws Exception
	 */
	public ALMappingConfigImpl(Object data) throws Exception {
		super(data);
	}

	/**
	 * Returns the mode of this config.
	 * 
	 * @return String constant
	 */
	public String getMode() {
		return ConnectorConfig.MAPPING_MODE;
	}

	/**
	 * Set the enabled state for a connector.
	 * 
	 * @param enabled
	 *            <code>true</code> if the connector is enabled;
	 *            <code>false</code> if the connector is disabled.
	 */
	public void setEnabled(boolean enabled) {
		setStringParameter(InternalSchema.CONNECTOR_STATE,
				enabled ? ConnectorConfig.ENABLED_STATE
						: ConnectorConfig.DISABLED_STATE);
	}

	/**
	 * Override ConnectorConfig to ensure we always get the input map.
	 * 
	 * @return {@link AttributeMapConfig} object
	 */
	public AttributeMapConfig getAttributeMap() {
		return super.getAttributeMap(true);
	}

	/**
	 * Override ConnectorConfig to ensure we always get the input map
	 * 
	 * @param name
	 * @return d
	 */
	public AttributeMapConfig getAttributeMap(Object name) {
		return super.getAttributeMap(true);
	}

	/**
	 * Override ConnectorConfig to ensure we always get the input map.
	 * 
	 * @param input
	 *            <code>true</code> to get the input map; <code>false</code>
	 *            to get the output map.
	 * @return {@link AttributeMapConfig} object
	 */
	public AttributeMapConfig getAttributeMap(boolean input) {
		return super.getAttributeMap(true);
	}

	/**
	 * @return <code>true</code> if the connector requires a response in
	 *         Server or Iterator mode.
	 */
	public boolean getReplyRequired() {
		return false;
	}

	/**
	 * @return <code>true<code> if the connector is an entry feed connector.
	 */
	public boolean isEntryFeed() {
		return false;
	}

	/**
	 * This method returns self clone.
	 * 
	 * @return {@link ALMappingConfigImpl} object
	 * @throws Exception
	 */
	public Object getClone() throws Exception {
		ALMappingConfig cc = new ALMappingConfigImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());
		cc.setupInheritanceChain();
		cc.setModTS(getModTS());
		return cc;
	}
}
