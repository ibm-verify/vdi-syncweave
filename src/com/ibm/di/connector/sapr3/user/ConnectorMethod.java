/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.util.List;

/**
 * ConnectorMethod
 * 
 * Representation of an IBM Tivoli Directory Integrator Connector method. Concrete impls will perform the
 * real work for the Connector method, e.g. putEntry().
 * 
 */
interface ConnectorMethod {

	/**
	 * getConfig
	 * 
	 * Obtain a reference to the config name value pairs.
	 * 
	 * @return The config
	 * 
	 */
	Configuration getConfig();

	/**
	 * setConfig
	 * 
	 * Set the configuration reference.
	 * 
	 * @param config
	 *            The configuration
	 * @throws IllegalArgumentException
	 *             if config is <code>null</code>.
	 */
	void setConfig(Configuration config);

	/**
	 * Determine if execution detected any R/3 ABAP application level errors.
	 * 
	 * @return <code>true</code> if ABAP errors occurred during execution,
	 *         <code>false</code> otherwise.
	 */
	boolean hasAbapErrors();

	/**
	 * Determine if execution detected any R/3 ABAP application level warnings.
	 * 
	 * @return <code>true</code> if ABAP warnings occurred during execution,
	 *         <code>false</code> otherwise.
	 */
	boolean hasAbapWarnings();

	/**
	 * Get all error messages returned during the execution of the method.
	 * 
	 * @return A list of AbapErrorInfo messages that occured as a result of
	 *         executing R/3 ABAP code. The length will be zero if no errors
	 *         occurred.
	 */
	List getAbapErrors();

	/**
	 * Get all warning messages returned during the execution of the method.
	 * 
	 * @return A list of AbapErrorInfo messages that occured as a result of
	 *         executing R/3 ABAP code. The length will be zero if no errors
	 *         occurred.
	 */
	List getAbapWarnings();
}
