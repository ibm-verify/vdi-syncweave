/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Implements the configuration used by a Connector using a Connector Pool.
 *
 */
public class PoolInstanceConfigImpl extends BaseConfigurationImpl implements
		PoolInstanceConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 5594919717769030291L;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public PoolInstanceConfigImpl() {
		super();
	}

	public PoolInstanceConfigImpl(Object aConfig) {
		super(aConfig);
	}

	public boolean getPoolEnabled() {
		return getBooleanParameter(
				InternalSchema.CONNECTOR_POOL_INSTANCE_ENABLED, false);
	}

	public void setPoolEnabled(boolean aPoolEnabled) {
		setBooleanParameter(InternalSchema.CONNECTOR_POOL_INSTANCE_ENABLED,
				aPoolEnabled);
	}

	public int getExhaustedPoolBehavior() {
		int exhaustedPoolBehavior = getIntegerParameter(
				InternalSchema.CONNECTOR_POOL_INSTANCE_EXHAUSTED_BEHAVIOR,
				PoolInstanceConfig.EXHAUSTED_POOL_WAIT);

		return exhaustedPoolBehavior;
	}

	public void setExhaustedPoolBehavior(int aBehaviorOnExhaustedPool)
			throws Exception {
		if ((aBehaviorOnExhaustedPool != PoolInstanceConfig.EXHAUSTED_POOL_WAIT)
				&& (aBehaviorOnExhaustedPool != PoolInstanceConfig.EXHAUSTED_POOL_FAIL)) {

			throw new Exception(
					sResHash
							.getString(
									"MMCONFIG.POOLINSTANCECONFIGIMPL.INVALID.BEHAVIOR.ON.EXHAUSTED.POOL",
									Integer.valueOf(aBehaviorOnExhaustedPool)));
		}

		setIntegerParameter(
				InternalSchema.CONNECTOR_POOL_INSTANCE_EXHAUSTED_BEHAVIOR,
				aBehaviorOnExhaustedPool);
	}
}
