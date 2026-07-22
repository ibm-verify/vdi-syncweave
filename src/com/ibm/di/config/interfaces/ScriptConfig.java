/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a Script Component in an AssemblyLine
 *
 */
public interface ScriptConfig extends BaseConfiguration {

	public boolean getAutoInclude();

	public void setAutoInclude(boolean include);

	public String getIncludeFiles();

	public void setIncludeFiles(String files);

}
