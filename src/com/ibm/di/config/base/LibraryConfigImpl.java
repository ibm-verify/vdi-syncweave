/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * Implements {@link LibraryConfig}
 * 
 */
public class LibraryConfigImpl extends BaseConfigurationImpl implements
		LibraryConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -6737181973806281819L;

	public LibraryConfigImpl() {
		super();
	}

	public LibraryConfigImpl(Object config) {
		super(config);
	}
}
