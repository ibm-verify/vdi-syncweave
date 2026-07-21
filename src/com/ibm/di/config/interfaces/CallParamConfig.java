/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Specifies the basic methods for configuring a CallParamConfig object
 * @deprecated This interface is no longer used, we use TaskCallBlock instead.
 * @see com.ibm.di.server.TaskCallBlock 
 */
public interface CallParamConfig extends BaseConfiguration {

	/**
	 * Gets the targetAttributeName attribute of the CallParamConfig object
	 * 
	 * @return The targetAttributeName value
	 */
	public String getTargetAttributeName();

	/**
	 * Sets the targetAttributeName attribute of the CallParamConfig object
	 * 
	 * @param targetAttributeName
	 *            The new targetAttributeName value
	 */
	public void setTargetAttributeName(String targetAttributeName);

	/**
	 * Gets the syntax attribute of the CallParamConfig object
	 * 
	 * @return The defaultValue value
	 */
	public String getSyntax();

	/**
	 * Gets the syntax of the call parameter.
	 * 
	 * @param value
	 *            The new syntax, e.g. "java.lang.String".
	 */
	public void setSyntax(String value);

}
