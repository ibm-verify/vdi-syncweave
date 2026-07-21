/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.List;

import javax.naming.Name;

import com.ibm.di.config.interfaces.*;

/**
 * The configuration of a single Logger for e.g. an AssemblyLine
 */
public class LogConfigItemImpl extends BaseConfigurationImpl implements
		LogConfigItem {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 6299750464788808971L;

	private static final String INTERNAL_NAME = "%%NAME%%";
	
	public LogConfigItemImpl() {
		super();
	}

	public LogConfigItemImpl(Object data) {
		super(data);
	}

	/**
	 * Returns the log level for the LogConfig
	 */
	public String getLogLevel() {
		return getStringParameter(InternalSchema.LOG_CONFIG_LEVEL);
	}

	/**
	 * Sets the log level for the LogConfig
	 */
	public void setLogLevel(String level) {
		setStringParameter(InternalSchema.LOG_CONFIG_LEVEL, level);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		LogConfigItem clone = new LogConfigItemImpl(deepClone(null));
		clone.setName(getName());
		clone.init();
		clone.setMetamergeConfig(getMetamergeConfig());
		clone.setupInheritanceChain();
		clone.setModTS(getModTS());
		return clone;
	}
	
	public void setName(Name name) {
		if (name == null)
			return;
		super.setName(name);
		setParameter(INTERNAL_NAME, name);
	}

	public Name getName() {
		Name n = super.getName();
		if (n != null)
			return n;
		return (Name)getParameterRaw(INTERNAL_NAME);
	}
	
	public List<String> getKeys(int level) {
		List<String> keys = super.getKeys(level);
		keys.remove(INTERNAL_NAME);
		return keys;
	}
}
