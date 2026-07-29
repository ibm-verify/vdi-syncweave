/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.ArrayList;

import com.ibm.di.config.interfaces.HookConfig;

/**
 * The configuration for all the Hooks e.g. in a Connector.
 *
 */
public interface HooksConfig extends AttributeMapConfig {

	public HookConfig getHook(Object name);

	public void removeHook(Object name);

	public void setHook(HookConfig hook);

	/**
	 * Could this hook inherit data, if it did not have local data?
	 * @param name Name of Hook
	 * @return true if there is data that could be inherited
	 */
	public boolean couldInherit(String name);

	/**
	 * Returns a list of defined hooks for this configuration.
	 * @since 7.0
	 */
	public ArrayList<HookConfig> getActiveHooks();
	
	/**
	 * Returns null if hook is undefined and create=false.
	 * 
	 * @since 7.0
	 */
	public HookConfig getHook (Object name, boolean create);

}
