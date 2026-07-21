/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component.base;

import javax.naming.NameNotFoundException;

import com.ibm.di.component.FunctionComponent;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.fc.FunctionInterface;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class BaseFunctionComponent extends BaseIntegrationComponent implements FunctionComponent {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.base.BaseIntegrationComponent#getDefaultConfig()
	 */
	public FunctionConfig getDefaultConfig() {
		return (FunctionConfig) super.getDefaultConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.base.BaseIntegrationComponent#newInstance()
	 */
	public FunctionInterface newInstance() throws Throwable {
		return (FunctionInterface) super.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.base.BaseIntegrationComponent#getDefaultConfig(com
	 * .ibm.di.config.interfaces.MetamergeConfig)
	 */
	protected BaseConfiguration getDefaultConfig(MetamergeConfig mc) throws Exception {
		String funcId = (String) getProperty("component.name");
		try {
			return (BaseConfiguration) mc.lookup(MetamergeConfig.DEFAULT_FUNCTION_FOLDER + "/" + funcId);
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
