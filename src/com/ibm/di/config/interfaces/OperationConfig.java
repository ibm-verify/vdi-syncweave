/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The Configuration for a single AssemblyLine operation.
 *
 */
public interface OperationConfig extends BaseConfiguration {

	/**
	 * This constant has been deprecated.
	 * @deprecated
	 */
	public final static String INIT_OPERATION = "$initialize";

	/**
	 * Returns the input or output schema
	 * 
	 * @param input
	 *            if true returns the input schema otherwise the output schema
	 *            is returned
	 */
	public SchemaConfig getSchema(boolean input);

	/**
	 * Returns the input or output attribute map
	 * 
	 * @param input
	 *            if true returns the input map otherwise the output map is
	 *            returned
	 */
	public AttributeMapConfig getAttributeMap(boolean input);

	/**
	 * Returns the public flag of the operation
	 * @deprecated This flag is not used anymore
	 */
	public boolean isPublic();

	/**
	 * Sets the public flag of the operation
	 * @deprecated This flag is not used anymore
	 */
	public void setPublic(boolean pub);
}
