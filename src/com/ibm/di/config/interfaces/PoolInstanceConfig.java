/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Configuration used by a Connector using a Connector Pool.
 * @see PoolInstanceConfig
 *
 */
public interface PoolInstanceConfig extends BaseConfiguration {

	public static final int EXHAUSTED_POOL_WAIT = 0;

	public static final int EXHAUSTED_POOL_FAIL = 1;

	public static final String[] EXH_POOL_NAMES = { "Wait", "Fail" };

	public boolean getPoolEnabled();

	public void setPoolEnabled(boolean aPoolEnabled);

	public int getExhaustedPoolBehavior();

	public void setExhaustedPoolBehavior(int aBehaviorOnExhaustedPool)
			throws Exception;

}
