/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.component;

import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.parser.ParserInterface;

/**
 * Representing a Parser integration component.
 * 
 * @since 7.2
 */
public interface ParserComponent extends IntegrationComponent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#getDefaultConfig()
	 */
	public ParserConfig getDefaultConfig();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.component.IntegrationComponent#newInstance()
	 */
	public ParserInterface newInstance() throws Throwable;

}
