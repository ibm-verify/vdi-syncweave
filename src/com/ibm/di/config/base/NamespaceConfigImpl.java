/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * Implements a configuration object describing another configuration that is included in this MetamergeConfig.
 *
 */
public class NamespaceConfigImpl extends BaseConfigurationImpl implements
		NamespaceConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 986964857890827079L;

	public NamespaceConfigImpl() {
		super();
	}

	public NamespaceConfigImpl(Object config) {
		super(config);
	}

	public String getURL() {
		return getStringParameter(InternalSchema.NAMESPACE_URL);
	}

	public void setURL(String url) {
		setStringParameter(InternalSchema.NAMESPACE_URL, url);
	}

	public String getDriver() {
		return getStringParameter(InternalSchema.NAMESPACE_DRIVER);
	}

	public void setDriver(String driver) {
		setStringParameter(InternalSchema.NAMESPACE_DRIVER, driver);
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		NamespaceConfig cc = new NamespaceConfigImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());
		cc.setupInheritanceChain();
		cc.setModTS(getModTS());
		return cc;
	}

}
