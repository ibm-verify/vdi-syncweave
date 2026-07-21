/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;

/**
 * Contains all the parameters needed to load and instantiate a Function.
 *
 */
public class RawFunctionConfigImpl extends BaseConfigurationImpl implements
		RawFunctionConfig {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 8439049716964119461L;

	public RawFunctionConfigImpl() {
		super();
	}

	public RawFunctionConfigImpl(Object config) {
		super(config);
	}

	public String getJavaClass() {
		return getStringParameter("javaclass");
	}
	/**
	 * We override this method to change the inherited object if we inherit from
	 * a FunctionConfig.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof FunctionConfig)
			super.setInheritsFrom(((FunctionConfig) inheritFrom)
					.getFunctionConfig());
		else
			super.setInheritsFrom(inheritFrom);
	}
}
