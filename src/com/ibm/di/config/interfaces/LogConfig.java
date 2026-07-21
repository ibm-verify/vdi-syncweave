/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * The Logging Configuration for e.g. an AssemblyLine.
 */
public interface LogConfig extends BaseConfiguration {

	/**
	 * Adds an item to the config array
	 */
	public void addItem(LogConfigItem item);

	/**
	 * Removes an item from the config array
	 */
	public void removeItem(int index);

	/**
	 * Returns a specific item from the config array
	 */
	public LogConfigItem getItem(int index);

	/**
	 * Returns a new item which is added to the internal list
	 */
	public LogConfigItem newItem();

	/**
	 * Returns the list of log config objects
	 */
	public List<LogConfigItem> getItems();

}
