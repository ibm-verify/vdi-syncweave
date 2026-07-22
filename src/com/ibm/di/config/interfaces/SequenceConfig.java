/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

public interface SequenceConfig extends ContainerConfig {

	/**
	 * Returns the LogConfig object
	 * 
	 * @return The logConfig value
	 */
	public LogConfig getLogConfig();

}
