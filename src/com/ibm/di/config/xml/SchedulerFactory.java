/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.BaseConfiguration;

public class SchedulerFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SCHEDULER_TAG = "Scheduler";

	@Override
	public void build(BaseConfiguration config, Element elem) throws Exception {
		setBaseName(config, elem);
		setParameters(elem, config, null);
	}

	@Override
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		getBaseName(config, elem);
		getParameters(elem, config);
	}
}
