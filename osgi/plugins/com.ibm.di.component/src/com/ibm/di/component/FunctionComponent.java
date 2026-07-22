/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component;

import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.fc.FunctionInterface;

/**
 * Representing a Function integration component.
 * 
 * @since 7.2
 */
public interface FunctionComponent extends IntegrationComponent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getDefaultConfig()
	 */
	public FunctionConfig getDefaultConfig();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#newInstance()
	 */
	public FunctionInterface newInstance() throws Throwable;
}
