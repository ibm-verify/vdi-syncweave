/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A utility class that implements MetamergeConfigChangeListener, and does nothing
 * when an event is received.
 *
 */
public class DefaultConfigChangeListener implements
		MetamergeConfigChangeListener {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public void configurationChanged(MetamergeConfigChange mcc) {
	}
}
