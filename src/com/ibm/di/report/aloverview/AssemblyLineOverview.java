/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.Map;

import org.w3c.dom.Node;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.report.IReport;
import com.ibm.di.report.ReportException;
import com.ibm.di.report.ReportFactory;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class AssemblyLineOverview implements IReport {

	private ReportConfig config = null;
	
	/**
	 * 
	 */
	public void init(Node configNode, Map<String, String> properties) 
		throws ReportException {

		config = ReportConfig.getInstance(configNode);
		config.setTdiInstallPath(properties.get(ReportFactory.TDI_INSTALL_PATH));
		config.setLocale(properties.get(ReportFactory.TDI_LOCALE));			
	}

	/**
	 * 
	 */
	public String generate(BaseConfiguration baseConfiguration, 
			MetamergeConfig metamergeConfig) throws ReportException {
		
		AssemblyLineConfig assemblyLineConfig = (AssemblyLineConfig) baseConfiguration;
		AssemblyLineInfo alInfo = new AssemblyLineInfo(assemblyLineConfig, metamergeConfig);
		
		HTMLFactory htmlFactory = new HTMLFactory(config);
		ReportProcessor processor = new ReportProcessor(htmlFactory, config);
		
		String html = null;
		try {			
			processor.process(alInfo);	
			html = htmlFactory.formatDocument(alInfo);	
		} catch (Exception e) {
			throw new ReportException(e);
		}		

		return html;
	}

}
