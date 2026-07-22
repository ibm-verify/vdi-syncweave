/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.TreeMap;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;

/**
 * Implements the Configuration for a single AssemblyLine operation.
  *
 */
public class OperationConfigImpl extends BaseConfigurationImpl implements
		OperationConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 2715909691453046036L;

	private SchemaConfig inputSchema;

	private SchemaConfig outputSchema;

	private AttributeMapConfig inputAttributeMap;

	private AttributeMapConfig outputAttributeMap;

	public OperationConfigImpl() {
		super();
	}

	public OperationConfigImpl(Object config) {
		super(config);
	}

	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		// Attribute maps
		if (inputAttributeMap == null) {
			inputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN, new TreeMap()));
		}
		inputAttributeMap.setParent(this);
		inputAttributeMap.init();
		inputAttributeMap.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.INPUT_MAP_NAME));

		if (outputAttributeMap == null) {
			outputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT, new TreeMap()));
		}
		outputAttributeMap.setParent(this);
		outputAttributeMap.init();
		outputAttributeMap.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.OUTPUT_MAP_NAME));

		// Input Schema
		if (inputSchema == null) {
			inputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.SCHEMA_INPUT, new TreeMap()));
		}
		inputSchema.setParent(this);
		inputSchema.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.INPUT_MAP_NAME));
		inputSchema.init();

		// Output Schema
		if (outputSchema == null) {
			outputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.SCHEMA_OUTPUT, new TreeMap()));
		}
		outputSchema.setParent(this);
		outputSchema.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.OUTPUT_MAP_NAME));
		outputSchema.init();

	}

	public SchemaConfig getSchema(boolean input) {
		return (input ? inputSchema : outputSchema);
	}

	public AttributeMapConfig getAttributeMap(boolean input) {
		return (input ? inputAttributeMap : outputAttributeMap);
	}

	public boolean isPublic() {
		return getBooleanParameter("public", true);
	}

	public void setPublic(boolean pub) {
		setBooleanParameter("public", pub);
	}

	public Object getClone() throws Exception {
		OperationConfig oc = new OperationConfigImpl(deepClone(null));
		oc.setName(getName());
		oc.init();
		oc.setParent(this); //TODO: This looks wrong, maybe only a temporary assignment?
		oc.setupInheritanceChain();
		oc.setModTS(getModTS());
		return oc;
	}

	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();

		inputSchema.setupInheritanceChain();
		outputSchema.setupInheritanceChain();
		inputAttributeMap.setupInheritanceChain();
		outputAttributeMap.setupInheritanceChain();
	}
}
