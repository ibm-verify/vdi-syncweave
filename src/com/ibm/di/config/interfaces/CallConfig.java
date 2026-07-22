/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * An object through which calls are performed.
 * @deprecated This interface is no longer used, we use TaskCallBlock instead.
 * @see com.ibm.di.server.TaskCallBlock 
 */
public interface CallConfig extends BaseConfiguration {

	/**
	 * Gets the callParameters attribute of the CallConfig object
	 * 
	 * @return The callParameters value
	 */
	public List getCallParameters();

	/**
	 * Gets a named CallParamConfig object
	 * 
	 * @param name
	 *            The name of the CallParamConfig object
	 * 
	 * @return The callParameter value
	 */
	public CallParamConfig getCallParameter(Object name);

	/**
	 * Sets a name CallParamConfig object
	 * 
	 * @param param
	 *            The new callParameter value
	 */
	public void setCallParameter(CallParamConfig param);

	/**
	 * Removes a CallParamConfig object from this config.
	 * 
	 * @param param
	 *            The CallParamConfig object to remove
	 */
	public void removeCallParameter(CallParamConfig param);

	/**
	 * Creates a new CallParamConfig and adds it to this configuration.
	 * 
	 * @param name
	 *            Call param name
	 * 
	 * @return The new CallParamConfig object
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public CallParamConfig newCallParameter(Object name) throws Exception;
}
