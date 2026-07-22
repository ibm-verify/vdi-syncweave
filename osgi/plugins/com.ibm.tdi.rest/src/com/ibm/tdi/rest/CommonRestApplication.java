/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.rest;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class CommonRestApplication extends ResourceConfig {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public CommonRestApplication() {
		// Register classes (per-request instances)
		register(AppInitializer.class);

		// Register singletons (shared instances between requests)
		register(new ALHandler());
		if(Boolean.getBoolean("com.ibm.tdi.rest.master.server")) {
			register(new MasterServer());
		}
	}
}
