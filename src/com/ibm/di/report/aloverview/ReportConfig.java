/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.server.ResourceHash;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class ReportConfig {

	public static final String MESSAGE_RESOURCE = "report";
	
	public static final String HTML_FRAGMENTS = "htmlFragments";
	public static final String IMAGES = "images";
	
	public static final String DOCUMENT_HEADER = "documentHeader";
	public static final String DOCUMENT_FOOTER = "documentFooter";
	public static final String TREE_NODE = "treeNode";
	public static final String ROOT_NODE = "rootNode";
	public static final String COMPONENT_NODE = "componentNode";
	public static final String DATA_TABLE = "dataTable";
	public static final String DATA_TABLE_EROW = "dataTableEvenRow";
	public static final String DATA_TABLE_OROW = "dataTableOddRow";
	public static final String DATA_TABLE_HCELL = "dataTableHeaderCell";
	public static final String DATA_TABLE_DCELL = "dataTableDataCell";
	public static final String RESOURCES = "resources";
	public static final String RESOURCE_LINK = "resource_link";	
	public static final String SCRIPT = "script";
	public static final String INFO_TABLE = "infoTable";
	public static final String INFO_TABLE_ROW = "infoTableRow";
	
	public static final String IMG_ASSEMBLY_LINE = "assemblyLine";
	public static final String IMG_SETTINGS = "settings";
	public static final String IMG_SCRIPT = "script";
	public static final String IMG_FOLDER = "folder";
	public static final String IMG_CONN_DELTA = "connectorDelta";
	public static final String IMG_CONN_ITERATOR = "connectorIterator";
	public static final String IMG_CONN_LOOKUP = "connectorLookup";
	public static final String IMG_CONN_SERVER = "connectorServer";
	public static final String IMG_CONN_UPDATE = "connectorUpdate";
	public static final String IMG_CONN_ADDONLY = "connectorAddOnly";
	public static final String IMG_CONN_CALLREPLY = "connectorCallReply";
	public static final String IMG_CONN_DELETE = "connectorDelete";
	public static final String IMG_CONNECTOR = "connector";
	public static final String IMG_PARSER = "parser";
	public static final String IMG_ATTR_MAP = "attrMap";
	public static final String IMG_DELTA = "delta";
	public static final String IMG_FUNCTION = "function";
	public static final String IMG_BRANCH = "branch";
	public static final String IMG_BRANCH_DISABLED = "branch_disabled";
	
	
	// Parameters in XML
	public static final String PARAM_INSTALL_DIR = "#{INSTALL_DIR}";
	public static final String PARAM_ASSEMBLY_LINE_NAME = "#{ASSEMBLY_LINE_NAME}";	
	public static final String PARAM_NODE_ID = "#{NODE_ID}";
	public static final String PARAM_NODE_IMG = "#{NODE_IMG}";
	public static final String PARAM_NODE_TITLE = "#{NODE_TITLE}";
	public static final String PARAM_NODE_CONTENT = "#{NODE_CONTENT}";
	public static final String PARAM_TABLE_CONTENT = "#{TABLE_CONTENT}";
	public static final String PARAM_PARAM_NAME = "#{PARAM_NAME}";
	public static final String PARAM_PARAM_VALUE = "#{PARAM_VALUE}";	
	public static final String PARAM_SCRIPT = "#{SCRIPT}";
	public static final String PARAM_ENABLED = "#{ENABLED}";
	public static final String PARAM_PARENT = "#{PARENT}";	
	public static final String PARAM_MODE = "#{MODE}";
	public static final String PARAM_TABLE_HEADER = "#{TABLE_HEADER}";
	public static final String PARAM_ROW_CONTENT = "#{ROW_CONTENT}";
	public static final String PARAM_RESOURCE_ID = "#{RESOURCE_ID}";
	public static final String PARAM_RESOURCE_NAME = "#{RESOURCE_NAME}";
	public static final String PARAM_TITLE = "#{TITLE}";
	public static final String PARAM_EXPAND_ALL = "#{EXPAND_ALL}";
	public static final String PARAM_HIDE_ALL = "#{HIDE_ALL}";
	public static final String PARAM_RESOURCES = "#{RESOURCES}";
	
	public static final String MSG_TITLE = "REPORT.ALOVERVIEW.TITLE";
	public static final String MSG_MSG_EXPAND_ALL = "REPORT.ALOVERVIEW.EXPAND_ALL";
	public static final String MSG_HIDE_ALL = "REPORT.ALOVERVIEW.HIDE_ALL";
	public static final String MSG_RESOURCES = "REPORT.ALOVERVIEW.RESOURCES";	
	public static final String MSG_SCRIPT = "REPORT.ALOVERVIEW.SCRIPT";
	public static final String MSG_CONFIGURATION = "REPORT.ALOVERVIEW.CONFIGURATION";
	public static final String MSG_OUTATTRMAP = "REPORT.ALOVERVIEW.OUTATTRMAP";
	public static final String MSG_INATTRMAP = "REPORT.ALOVERVIEW.INATTRMAP";
	public static final String MSG_LINK_CRITERIA = "REPORT.ALOVERVIEW.LINK_CRITERIA";
	public static final String MSG_RECONNECT = "REPORT.ALOVERVIEW.RECONNECT";
	public static final String MSG_DELTA = "REPORT.ALOVERVIEW.DELTA";
	public static final String MSG_PARSER = "REPORT.ALOVERVIEW.PARSER";
	public static final String MSG_HOOKS = "REPORT.ALOVERVIEW.HOOKS";
	public static final String MSG_EFC = "REPORT.ALOVERVIEW.EFC";
	public static final String MSG_DFC = "REPORT.ALOVERVIEW.DFC";
	public static final String MSG_PROXY_SETTINGS = "REPORT.ALOVERVIEW.PROXY_SETTINGS";
	public static final String MSG_SETTINGS = "REPORT.ALOVERVIEW.SETTINGS";
	public static final String MSG_PROPERTY_ENABLED = "REPORT.ALOVERVIEW.PROPERTY_ENABLED";
	public static final String MSG_PROPERTY_MODE = "REPORT.ALOVERVIEW.PROPERTY_MODE";
	public static final String MSG_PROPERTY_TYPE = "REPORT.ALOVERVIEW.PROPERTY_TYPE";
	public static final String MSG_PROPERTY_MATCH_ANY = "REPORT.ALOVERVIEW.PROPERTY_MATCH_ANY";
	public static final String MSG_OPERATIONS = "REPORT.ALOVERVIEW.OPERATIONS";
	
	/**
	 * 
	 * @param configNode
	 * @return ReportConfig
	 */
	public static ReportConfig getInstance(Node configNode) {
		ReportConfig reportConfig = new ReportConfig();
		
		NodeList childNodes = configNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node nextNode = childNodes.item(i);
			
			if (nextNode.getNodeName().equals(HTML_FRAGMENTS)) {
				reportConfig.extractHTMLFragments(nextNode);
			}
			
			if (nextNode.getNodeName().equals(IMAGES)) {
				reportConfig.extractImages(nextNode);
			}
		}
		
		return reportConfig;
	}
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	private Map<String, String> htmlFragments = new HashMap<String, String>();
	private Map<String, String> images = new HashMap<String, String>();
	
	private String tdiInstallPath = null;
	private String locale = "en";
	private ResourceHash resourceHash = null;
	
	/**
	 * 
	 */
	private ReportConfig() {
		this.resourceHash = ResourceHash.getHash(MESSAGE_RESOURCE);
	}
	
	/**
	 * 
	 * @param key
	 * @return String
	 */
	public String getMessage(String key) {
		return resourceHash.getString(key);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getTdiInstallPath() {
		return tdiInstallPath;
	}

	/**
	 * 
	 * @param tdiInstallPath
	 */
	public void setTdiInstallPath(String tdiInstallPath) {
		this.tdiInstallPath = tdiInstallPath;
	}

	/**
	 * 
	 * @return String
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * 
	 * @param locale
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}	
	
	/**
	 * 
	 * @param name
	 * @return StringBuffer
	 */
	public StringBuffer getHTMLFragment(String name) {
		String html = htmlFragments.get(name);
		return new StringBuffer(html);
	}

	/**
	 * 
	 * @param name
	 * @return StringBuffer
	 */
	public String getImageFile(String name) {
		return images.get(name);
	}	

	/**
	 * 
	 * @param node
	 */
	private void extractHTMLFragments(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node nextNode = childNodes.item(i);
			if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
				String value = nextNode.getFirstChild().getNodeValue();
				htmlFragments.put(nextNode.getNodeName(), value);
			}
		}
	}
	
	/**
	 * 
	 * @param node
	 */
	private void extractImages(Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++) {
			Node nextNode = childNodes.item(i);
			if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
				String imgFile = nextNode.getFirstChild().getNodeValue();
				images.put(nextNode.getNodeName(), imgFile);
			}
		}
	}	
	
}
