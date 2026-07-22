/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class AssemblyLineInfo {

	private MetamergeConfig metamergeConfig = null;
	private AssemblyLineConfig assemblyLineConfig = null;
	
	private List<TreeNode> assemblyLineNodes = new ArrayList<TreeNode>();
	private Map<String, TreeNode> resourceFolderNodes = null;
	private Map<String, TreeNode> resourceNodes = null;

	/**
	 * 
	 * @param assemblyLineConfig
	 * @param metamergeConfig
	 */
	public AssemblyLineInfo(AssemblyLineConfig assemblyLineConfig, 
			MetamergeConfig metamergeConfig) {
		this.assemblyLineConfig = assemblyLineConfig;
		this.metamergeConfig = metamergeConfig;
	}

	/**
	 * 
	 * @return AssemblyLineConfig
	 */
	public AssemblyLineConfig getAssemblyLineConfig() {
		return assemblyLineConfig;
	}
	
	/**
	 * 
	 * @return MetamergeConfig
	 */
	public MetamergeConfig getMetamergeConfig() {
		return metamergeConfig;
	}
	
	/**
	 * 
	 * @param node
	 */
	public void addAssemblyLineNode(TreeNode node) {
		if (node == null) {
			return;
		}
		
		assemblyLineNodes.add(node);
	}
	
	/**
	 * 
	 * @return List<TreeNode>
	 */
	public List<TreeNode> getAssemblyLineNodes() {
		return assemblyLineNodes;
	}
	
	/**
	 * 
	 * @param folderName
	 * @param resourceFolderNode
	 */
	public void addResourceFolderNode(String folderName, TreeNode resourceFolderNode) {
		
		if (resourceFolderNode == null) {
			return;
		}
		
		if (resourceFolderNodes == null) {
			resourceFolderNodes = new HashMap<String, TreeNode>();
		}
		resourceFolderNodes.put(folderName, resourceFolderNode);
	}	
	
	/**
	 * 
	 * @return Map<String, TreeNode>
	 */
	public Map<String, TreeNode> getResourceFolderNodes() {
		return resourceFolderNodes;
	}

	/**
	 * 
	 * @param folderName
	 * @return TreeNode
	 */
	public TreeNode getResourceFolderNode(String folderName) {
		if (resourceFolderNodes == null) {
			return null;
		}
		
		return resourceFolderNodes.get(folderName);
	}
	
	/**
	 * 
	 * @param resourceRef
	 * @param resourceNode
	 */
	public void addResourceNode(String resourceRef, TreeNode resourceNode) {
				
		if (resourceNode == null) {
			return;
		}		
		
		if (resourceNodes == null) {
			resourceNodes = new HashMap<String, TreeNode>();
		}
		resourceNodes.put(resourceRef, resourceNode);
	}
	
	/**
	 * 
	 * @param resourceRef
	 * @return TreeNode
	 */
	public TreeNode getResourceNode(String resourceRef) {
		if (resourceNodes == null) {
			return null;
		}
		
		return resourceNodes.get(resourceRef);
	}
	
}
