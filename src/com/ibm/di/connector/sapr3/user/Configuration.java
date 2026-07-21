/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.io.File;

import com.ibm.di.server.Log;

/**
 * Represents the configuration name value pairs.
 * 
 */
interface Configuration {

	/**
	 * Get the value of the named parameter.
	 * 
	 * @param paramName
	 *            The name of the configuration parameter.
	 * @return The value of the parameter, or <code>null</code> if the
	 *         paramters does not exist or has not been set.
	 * @throws IllegalArgumentException
	 *             if paramName is null.
	 * @throws IllegalStateException
	 *             if {@link #validate} has not been called.
	 */
	String getParamAsString(String paramName);

	/**
	 * Get the value of the named parameter as a files.
	 * 
	 * @param paramName
	 *            The name of the configuration parameter.
	 * @return The value of the parameter, or <code>null</code> if the
	 *         paramters does not exist or has not been set.
	 * @throws IllegalArgumentException
	 *             if paramName is null.
	 * @throws IllegalStateException
	 *             if {@link #validate} has not been called.
	 */
	File getParamAsFile(String paramName);

	/**
	 * Get the value of the named parameter as a list of files.
	 * 
	 * @param paramName
	 *            The name of the configuration parameter.
	 * @return The mulitple values of the parameter, or <code>null</code> if
	 *         the paramters does not exist or has not been set.
	 * @throws IllegalArgumentException
	 *             if paramName is null.
	 * @throws IllegalStateException
	 *             if {@link #validate} has not been called.
	 */
	File[] getParamAsFileArray(String paramName);

	/**
	 * Get the IBM Tivoli Directory Integrator Logger.
	 * 
	 * @return the log instance.
	 * @throws IllegalStateException
	 *             if {@link #validate} has not been called.
	 */
	Log getLog();

	/**
	 * Get the raw IBM Tivoli Directory Integrator Config object.
	 * 
	 * @return ConnectorConfig instance.
	 */
	Object getRawConfig();
}
