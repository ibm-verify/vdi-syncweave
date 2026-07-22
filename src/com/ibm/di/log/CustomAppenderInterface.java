/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import org.apache.log4j.Appender;

import com.ibm.di.config.interfaces.LogConfigItem;

public interface CustomAppenderInterface extends Appender {

	/**
	 * Initilizes the Custom Appender with its configuration parameters
	 * 
	 * @param aLogConfigItem
	 *            the Custom Appender configuration object.
	 */
	public void initialize(
			com.ibm.di.config.interfaces.LogConfigItem aLogConfigItem);

}
