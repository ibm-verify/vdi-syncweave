/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A configuration object describing another configuration that is included in this configuration.
 *
 */
public interface NamespaceConfig extends BaseConfiguration {

	// The provider URL (e.g. file, URL )
	public String getURL();

	public void setURL(String url);

	// The MetamergeConfig driver class name
	public String getDriver();

	public void setDriver(String driver);

	// Any other parameter is accessed by the driver using getParameter since
	// each driver may require different parameters

}
