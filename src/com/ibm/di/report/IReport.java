/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report;

import java.util.Map;

import org.w3c.dom.Node;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * 
 * @author yavor.gologanov
 *
 */
public interface IReport {
	
	/**
	 * 
	 * @param configNode
	 * @param properties
	 * @throws ReportException
	 */
	public void init(Node configNode, Map<String, String> properties) 
		throws ReportException;
	
	/**
	 * 
	 * @param baseConfiguration
	 * @param metamergeConfig
	 * @return
	 * @throws ReportException
	 */
	public String generate(BaseConfiguration baseConfiguration, 
			MetamergeConfig metamergeConfig) 
		throws ReportException;
	
}
