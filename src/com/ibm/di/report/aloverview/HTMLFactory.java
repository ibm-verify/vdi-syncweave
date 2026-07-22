/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class HTMLFactory {
	
	private ReportConfig reportConfig = null;
	
	/**
	 * 
	 * @param reportConfig
	 */
	public HTMLFactory(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}
	
	/**
	 * 
	 * @param assemblyLineInfo
	 * @return String
	 */
	public String formatDocument(AssemblyLineInfo assemblyLineInfo) {
		StringBuffer html = new StringBuffer();
		
		String assemblyLineName = assemblyLineInfo.getAssemblyLineConfig().getShortName();
		
		// add document header
		StringBuffer documentHeader = reportConfig.getHTMLFragment(ReportConfig.DOCUMENT_HEADER);
		replaceAll(documentHeader, ReportConfig.PARAM_INSTALL_DIR, reportConfig.getTdiInstallPath());
		replace(documentHeader, ReportConfig.PARAM_ASSEMBLY_LINE_NAME, assemblyLineName);
		
		String title = reportConfig.getMessage(ReportConfig.MSG_TITLE);
		replaceAll(documentHeader, ReportConfig.PARAM_TITLE, title);
		
		String expAll = reportConfig.getMessage(ReportConfig.MSG_MSG_EXPAND_ALL);
		replace(documentHeader, ReportConfig.PARAM_EXPAND_ALL, expAll);
		
		String hideAll = reportConfig.getMessage(ReportConfig.MSG_HIDE_ALL);
		replace(documentHeader, ReportConfig.PARAM_HIDE_ALL, hideAll);
		
		html.append(documentHeader);
		
		// add document content
		for (TreeNode node : assemblyLineInfo.getAssemblyLineNodes()) {
			html.append(toHTML(node, assemblyLineInfo, assemblyLineName));
		}
		
		Map<String, TreeNode> resourceFolderNodes = assemblyLineInfo.getResourceFolderNodes();
		if ((resourceFolderNodes != null) && (!resourceFolderNodes.isEmpty())) {
			StringBuffer resourcesHtml = reportConfig.getHTMLFragment(ReportConfig.RESOURCES);
			String resourcesMsg = reportConfig.getMessage(ReportConfig.MSG_RESOURCES);
			replace(resourcesHtml, ReportConfig.PARAM_RESOURCES, resourcesMsg);
			html.append(resourcesHtml);	
			
			for (Map.Entry<String, TreeNode>  folder : resourceFolderNodes.entrySet()) {
				html.append(toHTML(folder.getValue(), assemblyLineInfo, ReportProcessor.RESOURCES_PREFIX));
			}
		}
		
		// add document footer
		html.append(reportConfig.getHTMLFragment(ReportConfig.DOCUMENT_FOOTER));		
		return html.toString();
	}	
	
	/**
	 * 
	 * @param node
	 * @param content
	 * @param nodeId
	 * @return String
	 */
	public String formatNode(TreeNode node, StringBuffer content, String nodeId) {
		
		StringBuffer div = reportConfig.getHTMLFragment(node.getType());		
		replaceAll(div, ReportConfig.PARAM_NODE_ID, nodeId);
		
		String image = reportConfig.getImageFile(node.getImage());
		replace(div, ReportConfig.PARAM_NODE_IMG, image);
		replace(div, ReportConfig.PARAM_NODE_TITLE, node.getTitle());
		replace(div, ReportConfig.PARAM_NODE_CONTENT, content.toString());
		return div.toString();
	}		
	
	/**
	 * 
	 * @param table
	 * @return StringBuffer
	 */
	public StringBuffer getDataTable(DataTable table) {
		
		if ((table == null) || table.isEmpty()) {
			return null;
		}
		
		StringBuffer headerCells = new StringBuffer();	
		for (String header : table.getHeaders()) {
			StringBuffer cell = reportConfig.getHTMLFragment(ReportConfig.DATA_TABLE_HCELL);
			replace(cell, ReportConfig.PARAM_PARAM_NAME, header);
			headerCells.append(cell);
		}
		
		int rownum = 2;
		StringBuffer rowsBuffer = new StringBuffer();			
		for (List<String> row : table.getRows()) {
			StringBuffer dataCells = new StringBuffer();			
			for (String value : row) {
				StringBuffer cellBuffer = reportConfig.getHTMLFragment(ReportConfig.DATA_TABLE_DCELL);
				replace(cellBuffer, ReportConfig.PARAM_PARAM_VALUE, value);
				dataCells.append(cellBuffer);
			}
			
			StringBuffer rowBuffer = null;
			if (rownum%2 == 0) {
				rowBuffer = reportConfig.getHTMLFragment(ReportConfig.DATA_TABLE_EROW);
			} else {
				rowBuffer = reportConfig.getHTMLFragment(ReportConfig.DATA_TABLE_OROW);
			}
			replace(rowBuffer, ReportConfig.PARAM_ROW_CONTENT, dataCells.toString());
			rowsBuffer.append(rowBuffer).append("\n");
			rownum++;
		}
		
		StringBuffer tableBuffer = reportConfig.getHTMLFragment(ReportConfig.DATA_TABLE);
		replace(tableBuffer, ReportConfig.PARAM_TABLE_HEADER, headerCells.toString());
		replace(tableBuffer, ReportConfig.PARAM_TABLE_CONTENT, rowsBuffer.toString());
		return tableBuffer;		
	}	
		
	/**
	 * 
	 * @param paramMap
	 * @return StringBuffer
	 */
	public StringBuffer getDataTable(Map<String, String> paramMap) {
		
		if (paramMap == null) {
			return null;
		}
		
		List<String> headers = new ArrayList<String>();
		headers.add("Parameter");
		headers.add("Value");
		DataTable table = new DataTable(headers);

		for (Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
			List<String> row = new ArrayList<String>();
			row.add(paramEntry.getKey());
			row.add(paramEntry.getValue());
			table.addRow(row);
		}
		
		return getDataTable(table);
	}		
	
	/**
	 * 
	 * @param params
	 * @return StringBuffer
	 */
	public StringBuffer getInfoTable(Map<String, String> params) {
		
		if ((params == null) || (params.isEmpty())) {
			return null;
		}
		
		StringBuffer rows = new StringBuffer();	
		for (Map.Entry<String, String> param : params.entrySet()) {
			StringBuffer row = reportConfig.getHTMLFragment(ReportConfig.INFO_TABLE_ROW);
			replace(row, ReportConfig.PARAM_PARAM_NAME, param.getKey());
			replace(row, ReportConfig.PARAM_PARAM_VALUE, param.getValue());
			rows.append(row).append("\n");
		}
		
		StringBuffer table = reportConfig.getHTMLFragment(ReportConfig.INFO_TABLE);
		replace(table, ReportConfig.PARAM_TABLE_CONTENT, rows.toString());
		return table;	
	}		
	
	/**
	 * 
	 * @param scriptText
	 * @return StringBuffer
	 */
	public StringBuffer getScript(String scriptText) {			
		
		StringBuffer script = reportConfig.getHTMLFragment(ReportConfig.SCRIPT);
		if (scriptText == null) {
			scriptText = "";
		}
		
		replace(script, ReportConfig.PARAM_SCRIPT, scriptText);
		return script;
	}		
	
	/**
	 * 
	 * @param resourceId
	 * @param resourceName
	 * @return StringBuffer
	 */
	public StringBuffer getResoureLink(String resourceId, String resourceName) {			
		
		StringBuffer link = reportConfig.getHTMLFragment(ReportConfig.RESOURCE_LINK);
		replaceAll(link, ReportConfig.PARAM_RESOURCE_ID, resourceId);
		replace(link, ReportConfig.PARAM_RESOURCE_NAME, resourceName);
		return link;
	}	
		
	/**
	 * 
	 * @param node
	 * @param assemblyLineInfo
	 * @param treePath
	 * @return String
	 */
	private String toHTML(TreeNode node, AssemblyLineInfo assemblyLineInfo, String treePath) {
		
		StringBuffer nodeContent = new StringBuffer();
		Map<String, String> properties = new TreeMap<String, String>();
		String parentRef = node.getParentRefName();
		if (parentRef != null)  {			
			TreeNode resourceNode = assemblyLineInfo.getResourceNode(parentRef);
			if (resourceNode != null) {
				int index = parentRef.lastIndexOf("/");
				String folderName = parentRef.substring(1, index);	
				String resourceId = ReportProcessor.RESOURCES_PREFIX + "_" + folderName;
				
				StringBuffer resourceLink = getResoureLink(resourceId.toLowerCase(), parentRef);
				properties.put("Inherits From", resourceLink.toString());				
			} else {
				properties.put("Inherits From", node.getParentRefName());
			}
		}
		if ((node.getUserComments() != null) ){
			properties.put("User Comment", node.getUserComments());
		}
		properties.putAll(node.getProperties());
		StringBuffer propertiesTable = getInfoTable(properties);
		if (propertiesTable != null) {
			nodeContent.append(propertiesTable);
		}
		
		for (StringBuffer item : node.getContentItems()) {
			nodeContent.append(item);
		}
				
		String nodeId = node.getUniqueId(treePath);	
		for (TreeNode childNode : node.getChildNodes()) {
			nodeContent.append(toHTML(childNode, assemblyLineInfo, nodeId));
		}
		
		String html = formatNode(node, nodeContent, nodeId);		
		return html;
	}		
	
	/**
	 * 
	 * @param source
	 * @param text
	 * @param replacement
	 */
	private void replaceAll(StringBuffer source, String text, String replacement) {
		int pos = -1;
		while ((pos = source.indexOf(text, pos)) > 0) {
			source.replace(pos, pos + text.length(), replacement);
		}	
	}
	
	/**
	 * 
	 * @param source
	 * @param text
	 * @param replacement
	 */
	private void replace(StringBuffer source, String text, String replacement) {
		int pos = source.indexOf(text, -1);
		if (pos > 0) {
			source.replace(pos, pos + text.length(), replacement);
		}	
	}
	
}
