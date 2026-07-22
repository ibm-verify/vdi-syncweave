/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;
/**
 * The configuration for a Parser
 */
public interface ParserConfig extends BaseConfiguration {

	public String getJavaClass();

	/**
	 * @param name
	 *            the name of the schema - either "Input" or "Output"
	 * @return Parser's schema
	 * @since 7.0
	 */
	public SchemaConfig getSchema(String name);

	/**
	 * @param input
	 *            if true will return the input schema, otherwise will return
	 *            the output schema
	 * @return Parsers' schema
	 * @since 7.0
	 */
	public SchemaConfig getSchema(boolean input);
}
