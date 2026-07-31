/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * Implements PropertyConfig
 */
public class PropertyConfigImpl extends BaseConfigurationImpl implements
		PropertyConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -2620929677558833640L;

	public PropertyConfigImpl() {
		super();
	}

	public PropertyConfigImpl(Object config) {
		super(config);
	}
}
