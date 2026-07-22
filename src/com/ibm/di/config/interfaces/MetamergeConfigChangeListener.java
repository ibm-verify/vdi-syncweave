/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;
/**
 * The methods that must be implemented by a class listening to changes
 * in the configuration.
 * @see MetamergeConfigChange
 *
 */
public interface MetamergeConfigChangeListener {

	public void configurationChanged(MetamergeConfigChange changeEvent);

}
