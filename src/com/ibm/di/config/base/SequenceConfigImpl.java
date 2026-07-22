/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.TreeMap;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.SequenceConfig;

public class SequenceConfigImpl extends ContainerConfigImpl implements SequenceConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -325297100808939672L;

	/**
	 * Log config.
	 */
	private LogConfig logger;

	/**
	 * {@inheritDoc}
	 */
	public LogConfig getLogConfig() {
		if (logger != null) {
			return logger;
		}

		logger = new LogConfigImpl(getParameter(InternalSchema.LOG_CONFIG,
				new TreeMap<String,Object>()));
		logger.setParent(this);
		return logger;
	}

	@Override
	public Object getClone() throws Exception {
		SequenceConfigImpl cc = new SequenceConfigImpl();
		cc.setName(getName());
		cc.init();
		cc.logger = (LogConfig) logger.getClone();
		cc.logger.setParent(cc);
		for(BaseConfiguration obj : items) {
			cc.addConfig((BaseConfiguration) obj.getClone());
		}
		cc.setModTS(getModTS());
		return cc;
	}
}
