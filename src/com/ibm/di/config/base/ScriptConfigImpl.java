/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ScriptConfig;

/**
 * Implements the configuration for a Script Component in an AssemblyLine
 *
 */
public class ScriptConfigImpl extends BaseConfigurationImpl implements
		ScriptConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7747686242551793890L;

	public ScriptConfigImpl() {

		super();

	}

	public ScriptConfigImpl(Object config) {

		super(config);

	}

	public boolean getAutoInclude() {

		return getBooleanParameter(InternalSchema.SC_AUTO_INCLUDE, false);

	}

	public void setAutoInclude(boolean include) {

		setBooleanParameter(InternalSchema.SC_AUTO_INCLUDE, include);

	}

	public String getIncludeFiles() {
		return getStringParameter(InternalSchema.SC_INCLUDE_FILES);
	}

	public void setIncludeFiles(String files) {
		setStringParameter(InternalSchema.SC_INCLUDE_FILES, files);
	}

	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.ENABLED, true);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		ScriptConfig cc = new ScriptConfigImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());
		cc.setupInheritanceChain();
		cc.setModTS(getModTS());
		return cc;
	}

	public boolean isParameterLocal(Object name) {
		if (hasParameter(name))
			return true;
		BaseConfiguration inh = getInheritsFrom();
		while (inh != null) {
			Object o = inh.getParameterRaw(name);
			if (o != null && o.toString().length() > 0)
				return false;
			inh = inh.getInheritsFrom();
		}
		return true;
	}
}
