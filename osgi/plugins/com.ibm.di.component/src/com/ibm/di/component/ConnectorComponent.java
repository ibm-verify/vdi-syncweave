/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.ConnectorInterface;

/**
 * Representing a Connector integration component.
 * 
 * @since 7.2
 */
public interface ConnectorComponent extends IntegrationComponent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getDefaultConfig()
	 */
	public ConnectorConfig getDefaultConfig();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#newInstance()
	 */
	public ConnectorInterface newInstance() throws Throwable;

}
