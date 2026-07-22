/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Describes how to do record and playback for an AssemblyLine
 *
 */
public interface SandboxConfig extends BaseConfiguration {

	/**
	 * Returns the sandbox identifier
	 */
	public String getIdentifier();

	/**
	 * Sets the sandbox identifier
	 */
	public void setIdentifier(String identifier);

	/**
	 * Returns the Record enabled flag
	 */
	public boolean getRecordEnabled();

	/**
	 * Sets the Record enabled flag
	 */
	public void setRecordEnabled(boolean enabled);

	/**
	 * Returns the Playback enabled flag
	 */
	public boolean getPlaybackEnabled();

	/**
	 * Sets the Record enabled flag
	 */
	public void setPlaybackEnabled(boolean enabled);

}
