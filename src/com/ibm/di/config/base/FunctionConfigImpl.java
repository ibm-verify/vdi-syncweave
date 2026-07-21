/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.TreeMap;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;

/**
 * The configuration for a TDI Function Component, e.g. used in an AssemblyLine.
 *
 */
public class FunctionConfigImpl extends ConnectorConfigImpl implements
		FunctionConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 5778585850194005910L;

	private RawFunctionConfig functionConfig;

	public FunctionConfigImpl() throws Exception {
		super();
	}

	public FunctionConfigImpl(Object data) throws Exception {
		super(data);
	}

	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		super.init();

		// Raw function config
		if (functionConfig == null)
			functionConfig = new RawFunctionConfigImpl(getParameter(
					InternalSchema.FUNCTION_CONFIG, new TreeMap()));
		functionConfig.setParent(this);
		functionConfig.init();

		// Parser config
		if (parserConfig == null)
			parserConfig = new ParserConfigImpl(getParameter(
					InternalSchema.CONNECTOR_PARSER_CONFIG, new TreeMap()));
		parserConfig.setParent(this);
		parserConfig.init();

		// ConnectorConfigImpl will do the flattening itself but we need
		// the extra function config flattened also (done by
		// BaseConfigurationImpl)
		setChild("Adapter", functionConfig);
		setChild("Parser", parserConfig);

		setParentInherit(getSchema(ConnectorConfig.SCHEMA_INPUT));
		setParentInherit(getSchema(ConnectorConfig.SCHEMA_OUTPUT));
		setParentInherit(getAttributeMap(true));
		setParentInherit(getAttributeMap(false));
		setParentInherit(parserConfig);
	}

	private void setParentInherit(BaseConfiguration bc) {
		if (bc == null)
			return;
		if (bc.getInheritsFromRef() == null)
			bc.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
	}

	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();
		if (getInheritsFrom() instanceof FunctionConfig)
			functionConfig.setInheritsFrom(((FunctionConfig) getInheritsFrom())
					.getFunctionConfig());
		else
			functionConfig.setInheritsFrom(null);
		parserConfig.setupInheritanceChain();
	}

	/**
	 * Returns the implementing java class
	 * 
	 * @return The java class
	 */
	public String getJavaClass() {
		return functionConfig.getJavaClass();
	}

	/**
	 * Returns the mode which is always call-reply
	 * 
	 * @return The mode
	 */
	public String getMode() {
		return ConnectorConfig.FUNCTION_MODE;
	}

	/**
	 * Function specific configuration
	 */
	public RawFunctionConfig getFunctionConfig() {
		return functionConfig;
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		FunctionConfig cc = new FunctionConfigImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());
		cc.setupInheritanceChain();
		cc.setModTS(getModTS());
		return cc;
	}

}
