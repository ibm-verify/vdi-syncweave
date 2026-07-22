/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a single Hook e.g. in a Connector.
 */
public interface HookConfig extends BaseConfiguration {

	/**
	 * {@inheritDoc}
	 */
	public boolean getEnabled();

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @return name of the hook
	 */
	public Object getHookName();

	/**
	 * Sets the name of the hook.
	 * 
	 * @param name
	 */
	public void setHookName(Object name);

	/**
	 * Could this hook inherit data from some other place, if it did not have
	 * local data?
	 * 
	 * @return true if there is data that could be inherited
	 * @since 6.2
	 */
	public boolean couldInherit();
}
