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
public class TreeNode {

	private String type = ReportConfig.TREE_NODE;
	private String title = null;
	private String id = null;
	private String image = ReportConfig.IMG_FOLDER;
	
	private String userComments = null;
	private String parentRefName = null;
	
	private Map<String, String> properties = new TreeMap<String, String>();	
	private List<StringBuffer> contentItems = new ArrayList<StringBuffer>();	
	private List<TreeNode> childNodes = new ArrayList<TreeNode>();
	
	/**
	 * 
	 * @param title
	 * @param type
	 */
	protected TreeNode(String title, String type) {
		this.title = title;
		this.type = type;

	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addProperty(String name, String value) {
		properties.put(name, value);
	}	
	
	/**
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getProperties() {
		return properties;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 
	 * @return String
	 */
	public String getNodeId() {
		if (id == null) {
			return title.toLowerCase();
		}
		return id;
	}
	
	/**
	 * 
	 * @param prefix
	 * @return String
	 */
	public String getUniqueId(String prefix) {
		String uid = prefix + '_' + getNodeId();	
		uid = uid.replace(' ', '_');
		return uid;
	}
	
	/**
	 * 
	 * @param id
	 */
	protected void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @param image
	 */
	protected void setImage(String image) {
		this.image = image;
	}	

	/**
	 * 
	 * @param userComments
	 */
	protected void setUserComments(String userComments) {
		this.userComments = userComments;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getUserComments() {
		return userComments;
	}

	/**
	 * 
	 * @param parentRefName
	 */
	protected void setParentRefName(String parentRefName) {
		this.parentRefName = parentRefName;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getParentRefName() {
		return parentRefName;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getImage() {
		return image;
	}	
		
	/**
	 * 
	 * @param content
	 */
	public void addContentItem(StringBuffer content) {
		contentItems.add(content);
	}	
	
	/**
	 * 
	 * @return List<StringBuffer>
	 */
	public List<StringBuffer> getContentItems() {
		return contentItems;
	}
	
	/**
	 * 
	 * @param node
	 */
	public void addNode(TreeNode node) {
		if (node == null) {
			return;
		}
		
		if (!node.isEmpty()) {
			childNodes.add(node);
		}
	}	

	/**
	 * 
	 * @return List<TreeNode>
	 */
	public List<TreeNode> getChildNodes() {
		return childNodes;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
	
		return (properties.isEmpty() && childNodes.isEmpty() && 
				contentItems.isEmpty() && (parentRefName == null));
	}
	
}
