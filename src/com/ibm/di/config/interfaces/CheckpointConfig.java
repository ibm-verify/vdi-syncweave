/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Used for configuring a Checkpoint configuration.
 * @deprecated This interface is no longer used, Checkpoint/restart is deprecated.
*/
public interface CheckpointConfig extends BaseConfiguration {

	/**
	 * Returns the checkpoint identifier.
	 * 
	 * @return Checkpoint identifier
	 */
	public String getIdentifier();

	/**
	 * Sets the checkpoint identifier.
	 * 
	 * @param id
	 *            The checkpoint identifier
	 */
	public void setIdentifier(String id);

	/**
	 * @return <code>true</code> if the feature is enabled; <code>false</code>
	 *         otherwise.
	 */
	public boolean getWorkEnabled();

	/**
	 * @return <code>true</code> if the restart info is enabled;
	 *         <code>false</code> otherwise.
	 */
	public boolean getRestartInfoEnabled();

	/**
	 * Sets the feature on/off.
	 * 
	 * @param enable
	 *            new value
	 */
	public void setWorkEnabled(boolean enable);

	/**
	 * Sets the restart info on/off.
	 * 
	 * @param enable
	 *            new value
	 */
	public void setRestartInfoEnabled(boolean enable);

}
