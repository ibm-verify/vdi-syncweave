/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration of a single Logger for e.g. an AssemblyLine
 */
public interface LogConfigItem extends BaseConfiguration {

	/**
	 * Returns the log level for the LogConfig
	 */
	public String getLogLevel();

	/**
	 * Sets the log level for the LogConfig
	 */
	public void setLogLevel(String level);

}
