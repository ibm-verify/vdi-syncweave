/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.SchedulerConfig;

/**
 *Implements a SchedulerConfig.
 *
 */
public class SchedulerConfigImpl extends BaseConfigurationImpl implements
		SchedulerConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7224398293287296262L;

	
	public SchedulerConfigImpl() {
		super();
	}

	public SchedulerConfigImpl(Object o) {
		super(o);
	}

	public String getScheduledName() {
		return getStringParameter(InternalSchema.SCHEDULER_SCHEDULED_NAME);
	}

	public String getStartTimes() {
		return getStringParameter(InternalSchema.SCHEDULER_START_TIMES);
	}

	public int getType() {
		return getIntegerParameter(InternalSchema.SCHEDULER_TYPE, TIMER);
	}

	public void setScheduledName(String name) {
		setParameter(InternalSchema.SCHEDULER_SCHEDULED_NAME, name);
	}

	public void setStartTimes(String times) {
		setParameter(InternalSchema.SCHEDULER_START_TIMES, times);
	}

	public void setType(int type) {
		setParameter(InternalSchema.SCHEDULER_TYPE, type);
	}

    /**
     * Return self clone
     */
    public Object getClone() throws Exception {
            SchedulerConfig sc = new SchedulerConfigImpl(deepClone(null));
            sc.setName(getName());
            sc.init();
            sc.setMetamergeConfig(getMetamergeConfig());
            sc.setupInheritanceChain();
            sc.setModTS(getModTS());
            return sc;
    }

    /**
     * change default for enabled to be true.
     */
	@Override
	public boolean getEnabled() {
		// TODO Auto-generated method stub
		return getBooleanParameter(InternalSchema.ENABLED, true);
	}
}
