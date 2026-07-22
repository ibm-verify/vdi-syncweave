/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component.base;

import javax.naming.NameNotFoundException;

import com.ibm.di.component.ParserComponent;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.parser.ParserInterface;

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
public class BaseParserComponent extends BaseIntegrationComponent implements ParserComponent {
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
	public ParserConfig getDefaultConfig() {
		return (ParserConfig) super.getDefaultConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.base.BaseIntegrationComponent#newInstance()
	 */
	public ParserInterface newInstance() throws Throwable {
		return (ParserInterface) super.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.component.base.BaseIntegrationComponent#getDefaultConfig(com
	 * .ibm.di.config.interfaces.MetamergeConfig)
	 */
	protected BaseConfiguration getDefaultConfig(MetamergeConfig mc) throws Exception {
		String parserId = (String) getProperty("component.name");
		try {
			return (BaseConfiguration) mc.lookup(MetamergeConfig.DEFAULT_PARSER_FOLDER + "/" + parserId);
		} catch (NameNotFoundException e) {
			return null;
		}
	}
}
