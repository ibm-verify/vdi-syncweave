/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A Configuration containing the parameters for a Connector.
 *
 */
public interface RawConnectorConfig extends BaseConfiguration {

	public final static int PARSER_REQUIRED = 0;

	public final static int PARSER_OPTIONAL = 1;

	public final static int PARSER_USELESS = 2;

	/**
	 * Returns the java class name for the implementing connector
	 */
	public String getJavaClass();

	/**
	 * Sets the java class name for the implementing connector
	 */
	public void setJavaClass(String javaClass);

	/**
	 * Returns the parser option flag for this raw connector
	 */
	public int getParserOption();

}
