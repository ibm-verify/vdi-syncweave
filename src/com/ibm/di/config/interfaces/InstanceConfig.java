/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Configuration for the list of AssemblyLines that will
 * automatically be started when the server is started.
 *
 */

public interface InstanceConfig extends BaseConfiguration {

	/**
	 * This property is used to identify the name of an autostart item. The
	 * startup items container has a BaseConfiguration object for each startup
	 * item. The parameter AUTOSTART_NAME names the full path to the
	 * Assemblyline that should be started when the config
	 * instance starts.
	 */
	public final static String AUTOSTART_NAME = "Name";

	/**
	 * Returns the instance identifier for this configuration.
	 */
	public String getInstanceID();

	/**
	 * Returns the instance identifier for this configuration.
	 */
	public void setInstanceID(String id);

	/**
	 * Returns the "list" of AssemblyLines that should be auto-started
	 */
	public ContainerConfig getStartupItems();

}
