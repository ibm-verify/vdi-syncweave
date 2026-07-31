/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A configuration containing the parameters for a Function used in a Function Component.
 *
 */
public interface RawFunctionConfig extends BaseConfiguration {
	/**
	 * Returns the java class name for the implementing Function
	 */
	public String getJavaClass();
}
