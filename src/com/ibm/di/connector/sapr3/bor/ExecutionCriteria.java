/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * Represents parameters the filter what data instances are matched and returned
 * during a read operation.
 * 
 */
interface ExecutionCriteria {

	/**
	 * Gets the parameter value.
	 * 
	 * @param name
	 *            The name of the parameter
	 * @return The value of the parameter, or <code>null</code> if the name
	 *         has not been set.
	 */
	String getParam(String name);

	/**
	 * Set the value of the given parameter name. Overwrites exiting value if
	 * already set.
	 * 
	 * @param name
	 *            The name of the parameter to be set.
	 * @param value
	 *            The value.
	 */
	void setParam(String name, String value);

	/**
	 * Get the list of parameter names that have been set for this filter.
	 * 
	 * @return The list of names. The length will be zero if no names are
	 *         present.
	 */
	String[] getParamNames();
}
