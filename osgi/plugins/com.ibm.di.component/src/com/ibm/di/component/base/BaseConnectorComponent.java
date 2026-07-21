/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component.base;

import javax.naming.NameNotFoundException;

import com.ibm.di.component.ConnectorComponent;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.connector.ConnectorInterface;

/**
 * The base connector component class
 * 
 * @since 7.2
 */
public class BaseConnectorComponent extends BaseIntegrationComponent implements ConnectorComponent {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.base.BaseIntegrationComponent#newInstance()
	 */
	public ConnectorInterface newInstance() throws Throwable {
		return (ConnectorInterface) super.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.base.BaseIntegrationComponent#getDefaultConfig()
	 */
	public ConnectorConfig getDefaultConfig() {
		return (ConnectorConfig) super.getDefaultConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.base.BaseIntegrationComponent#getDefaultConfig(com
	 * .ibm.di.config.interfaces.MetamergeConfig)
	 */
	protected BaseConfiguration getDefaultConfig(MetamergeConfig mc) throws Exception {
		String connId = (String) getProperty("component.name");
		try {
			return (BaseConfiguration) mc.lookup(MetamergeConfig.DEFAULT_CONNECTOR_FOLDER + "/" + connId);
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
