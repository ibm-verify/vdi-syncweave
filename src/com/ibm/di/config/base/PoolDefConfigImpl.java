/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;

/**
 * Implements the configuration for a Connector Pool definition.
 *
 */
public class PoolDefConfigImpl extends BaseConfigurationImpl implements
		PoolDefConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -1252371938517765606L;

	public PoolDefConfigImpl() {
		super();
	}

	public PoolDefConfigImpl(Object aConfig) {
		super(aConfig);
	}

	public boolean getPoolEnabled() {
		return getBooleanParameter(InternalSchema.CONNECTOR_POOL_DEF_ENABLED,
				false);
	}

	public void setPoolEnabled(boolean aPoolEnabled) {
		setBooleanParameter(InternalSchema.CONNECTOR_POOL_DEF_ENABLED,
				aPoolEnabled);
	}

	public int getMaxPoolSize() {
		return getIntegerParameter(InternalSchema.CONNECTOR_POOL_DEF_MAX_SIZE,
				0);
	}

	public void setMaxPoolSize(int aMaxPoolSize) {
		setIntegerParameter(InternalSchema.CONNECTOR_POOL_DEF_MAX_SIZE,
				aMaxPoolSize);
	}

	public int getMinPoolSize() {
		return getIntegerParameter(InternalSchema.CONNECTOR_POOL_DEF_MIN_SIZE,
				0);
	}

	public void setMinPoolSize(int aMinPoolSize) {
		setIntegerParameter(InternalSchema.CONNECTOR_POOL_DEF_MIN_SIZE,
				aMinPoolSize);
	}

	public int getPurgeInterval() {
		return getIntegerParameter(
				InternalSchema.CONNECTOR_POOL_DEF_PURGE_INTERVAL, 0);
	}

	public void setPurgeInterval(int aPurgeInterval) {
		setIntegerParameter(InternalSchema.CONNECTOR_POOL_DEF_PURGE_INTERVAL,
				aPurgeInterval);
	}

	public int getInitializeAttempts() {
		return getIntegerParameter(
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_ATTEMPTS, 1);
	}

	public void setInitializeAttempts(int aAttempts) {
		setIntegerParameter(
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_ATTEMPTS,
				aAttempts);
	}

	public int getInitializeSleepInterval() {
		return getIntegerParameter(
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_SLEEP_INTERVAL, 0);
	}

	public void setInitializeSleepInterval(int aSleepInterval) {
		setIntegerParameter(
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_SLEEP_INTERVAL,
				aSleepInterval);
	}

	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig) {
			super.setInheritsFrom(((ConnectorConfig) inheritFrom)
					.getPoolDefConfig());
		} else {
			super.setInheritsFrom(inheritFrom);
		}
	}

}
