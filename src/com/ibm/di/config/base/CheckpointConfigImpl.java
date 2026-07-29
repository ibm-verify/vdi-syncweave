/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.CheckpointConfig;

/**
 * Checkpoint/restart has been deprecated.
 * @deprecated
 */
public class CheckpointConfigImpl extends BaseConfigurationImpl implements
		CheckpointConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -8342369881523468483L;

	/**
	 * Constructors
	 */
	public CheckpointConfigImpl() {
		super();
	}

	/**
	 * Constructor with one parameter.
	 * 
	 * @param data
	 *            TreeMap of attribute/value pairs
	 */
	public CheckpointConfigImpl(Object data) {
		super(data);
	}

	/**
	 * Returns the checkpoint identifier.
	 * 
	 * @return Checkpoint identifier
	 */
	public String getIdentifier() {
		return getStringParameter(InternalSchema.CHECKPOINT_IDENTIFIER);
	}

	/**
	 * Sets the checkpoint identifier.
	 * 
	 * @param id
	 *            The checkpoint identifier
	 */
	public void setIdentifier(String id) {
		setStringParameter(InternalSchema.CHECKPOINT_IDENTIFIER, id);
	}

	/**
	 * Returns true if the feature is enabled.
	 */

	/**
	 * {@inheritDoc}
	 */
	public boolean getWorkEnabled() {
		// uses inherited getEnabled()
		return getBooleanParameter(InternalSchema.CHECKPOINT_WORK, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getRestartInfoEnabled() {
		return getBooleanParameter(InternalSchema.CHECKPOINT_RSI, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setWorkEnabled(boolean enable) {
		// uses inherited setEnabled()
		setBooleanParameter(InternalSchema.CHECKPOINT_WORK, enable);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRestartInfoEnabled(boolean enable) {
		setBooleanParameter(InternalSchema.CHECKPOINT_RSI, enable);
	}

}
