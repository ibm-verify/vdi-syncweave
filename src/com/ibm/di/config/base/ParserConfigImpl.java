/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;

import com.ibm.di.config.interfaces.*;

/**
 * Implements the configuration for a Parser
 */
public class ParserConfigImpl extends BaseConfigurationImpl implements
		ParserConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 5497221494799800099L;
	
	/**
	 * Parser's input schema.
	 */
	private SchemaConfig inputSchema;
	
	/**
	 * Parser's output schema.
	 */
	private SchemaConfig outputSchema;
	

	public ParserConfigImpl() {
		super();
	}

	public ParserConfigImpl(Object config) {
		super(config);
	}

	public String getJavaClass() {
		return getStringParameter(InternalSchema.PARSER_JAVACLASS);
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig)
			super.setInheritsFrom(((ConnectorConfig) inheritFrom)
					.getParserConfig());
		else
			super.setInheritsFrom(inheritFrom);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		ParserConfig pc = new ParserConfigImpl(deepClone(null));
		pc.setName(getName());
		pc.init();
		pc.setMetamergeConfig(getMetamergeConfig());
		pc.setupInheritanceChain();
		pc.setModTS(getModTS());
		return pc;
	}
	
	public boolean flatten(List<String> excludedNS) throws Exception {
		
		inputSchema.flatten(excludedNS);
		outputSchema.flatten(excludedNS);

		return super.flatten(excludedNS);
	}
	
	public SchemaConfig getSchema(String name) {
		return getSchema("Input".equalsIgnoreCase(name));
	}

	public SchemaConfig getSchema(boolean input) {
		if (input)
			return inputSchema;
		else
			return outputSchema;
	}
	
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		
		super.init();
		
		if (inputSchema == null) {
			inputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_SCHEMA_INPUT, new TreeMap()));
			inputSchema.setName(MetamergeConfigFactory
					.parseName(ConnectorConfig.SCHEMA_INPUT));
		}
		inputSchema.setParent(this);
		inputSchema.init();
		setChild("InputSchema", inputSchema);
		
		inputSchema.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		
		
		if (outputSchema == null) {
			outputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_SCHEMA_OUTPUT, new TreeMap()));
			outputSchema.setName(MetamergeConfigFactory
					.parseName(ConnectorConfig.SCHEMA_OUTPUT));
		}
		outputSchema.setParent(this);
		outputSchema.init();
		setChild("OutputSchema", outputSchema);
		
		outputSchema.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
	}
	
	public void setupInheritanceChain() throws Exception {
		
		super.setupInheritanceChain();
		inputSchema.setupInheritanceChain();
		outputSchema.setupInheritanceChain();
	}
	
}
