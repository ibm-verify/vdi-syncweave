/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Specifies the basic methods for work with the operations, provided by the
 * AssemblyLine or AssemblyLine object
 * 
 */
public interface OperationsConfig extends BaseConfiguration {
	/**
	 * This method returns a list of supported operations
	 */
	public ContainerConfig getOperations();

	/**
	 * This method returns the config for a given operation
	 */
	public OperationConfig getOperation(String name);

	/**
	 * This method creates a new operation object.
	 * 
	 * @throws Exception
	 */
	public OperationConfig createOperation(String name) throws Exception;

	/**
	 * Returns the Published Initialization Parameter Schema for the
	 * AssemblyLine
	 * 
	 * @since 6.1.1
	 */
	public SchemaConfig getPublishedInitParams();

	/**
	 * Sets the Published Initialization Parameter Schema for the AssemblyLine
	 * 
	 * @param schema
	 *            The new schema
	 * @throws Exception
	 * @since 6.1.1
	 */
	public void setPublishedInitParams(SchemaConfig schema) throws Exception;
}
