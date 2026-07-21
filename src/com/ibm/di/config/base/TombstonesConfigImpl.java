/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.TombstonesConfig;

/**
 * Implements {@link TombstonesConfig}
 *
 */
public class TombstonesConfigImpl extends BaseConfigurationImpl implements
		TombstonesConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -3260102686391332434L;

	public TombstonesConfigImpl() {
		super();
	}

	public TombstonesConfigImpl(Object tm) {
		super(tm);
	}

	public void init() {
	}
}
