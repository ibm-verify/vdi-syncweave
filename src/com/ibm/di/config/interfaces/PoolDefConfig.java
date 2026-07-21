/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;
/**
 * The configuration for a Connector Pool definition.
 *
 */
public interface PoolDefConfig extends BaseConfiguration {

	public boolean getPoolEnabled();

	public void setPoolEnabled(boolean aPoolEnabled);

	public int getMaxPoolSize();

	public void setMaxPoolSize(int aMaxPoolSize);

	public int getMinPoolSize();

	public void setMinPoolSize(int aMinPoolSize);

	public int getPurgeInterval();

	public void setPurgeInterval(int aPurgeInterval);

	public int getInitializeAttempts();

	public void setInitializeAttempts(int aAttempts);

	public int getInitializeSleepInterval();

	public void setInitializeSleepInterval(int aSleepInterval);

}
