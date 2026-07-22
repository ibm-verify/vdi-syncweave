/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class ReportFactory {

	public static final String REPORT_CLASS = "reportClass";
	public static final String REPORT_CONFIG = "reportConfig";
	
	public static final String TDI_INSTALL_PATH = "TDIInstallPath";
	public static final String TDI_LOCALE = "TDILocale";
	
	/**
	 * 
	 * @param properties
	 * @return
	 */
	public static ReportFactory newInstance(Map<String, String> properties) {
		ReportFactory instance = new ReportFactory();
		if (properties != null) {
			instance.properties.putAll(properties);
		}
		return instance;
	}
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	private Map<String, String> properties = new HashMap<String, String>();
	
	/**
	 * 
	 */
	private ReportFactory() {
		
	}
	
	/**
	 * 
	 * @param configFile
	 * @return
	 * @throws ReportException
	 */
	public IReport createReport(String configFile) throws ReportException {
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(configFile));
			
			String className = null;
			Node configNode = null;
			
			NodeList childNodes = doc.getDocumentElement().getChildNodes();
			for (int i=0; i<childNodes.getLength(); i++) {
				Node nextNode = childNodes.item(i);
				
				if (nextNode.getNodeName().equals(REPORT_CLASS)) {
					className = nextNode.getFirstChild().getNodeValue();
				}
			
				if (nextNode.getNodeName().equals(REPORT_CONFIG)) {
					configNode = nextNode;
				}
			}
			
			IReport report = (IReport) Class.forName(className).newInstance();
			report.init(configNode, properties);
			return report;
		} catch (Exception e) {
			throw new ReportException(e);
		} 
	}
	
}
