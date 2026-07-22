/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a TDI Function Component, e.g. used in an AssemblyLine.
 *
 */
public interface FunctionConfig extends ConnectorConfig {

	/**
	 * Returns the implementing java class
	 * 
	 * @return The java class
	 */
	public String getJavaClass();

	/**
	 * Function specific configuration
	 */
	public RawFunctionConfig getFunctionConfig();

}
