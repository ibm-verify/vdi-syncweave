/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report.aloverview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SimulationConfig;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class ReportProcessor {

	public static final String RESOURCES_PREFIX = "resources";
	
	public static final String NODE_CONFIGURATION = "config";
	public static final String NODE_OUTATTRMAP = "outmap";
	public static final String NODE_INATTRMAP = "inmap";
	public static final String NODE_LINK_CRITERIA = "lc";
	public static final String NODE_RECONNECT = "rc";
	public static final String NODE_DELTA = "delta";
	public static final String NODE_PARSER = "parser";
	public static final String NODE_HOOKS = "hooks";
	public static final String NODE_EFC = "efc";
	public static final String NODE_DFC = "dfc";
	public static final String NODE_PROXY_SETTINGS = "proxy";
	public static final String NODE_SETTINGS = "settings";
	public static final String NODE_OPERATIONS = "operations";
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------	
	
	private HTMLFactory htmlFactory = null;
	private ReportConfig reportConfig = null;
	private List<String> references = null;

	/**
	 * 
	 * @param htmlFactory
	 * @param reportConfig
	 */
	public ReportProcessor(HTMLFactory htmlFactory, ReportConfig reportConfig) {
		this.htmlFactory = htmlFactory;
		this.reportConfig = reportConfig;
	}
	
	/**
	 * 
	 * @return HTMLFactory
	 */
	public HTMLFactory getHtmlFactory() {
		return htmlFactory;
	}
	
	
	/**
	 * 
	 * @param assemblyLineInfo
	 * @throws Exception
	 */
	public synchronized void process(AssemblyLineInfo assemblyLineInfo) 
		throws Exception {
		
		references = new ArrayList<String>();
		AssemblyLineConfig config = assemblyLineInfo.getAssemblyLineConfig();
		
		// Add settings
		FormConfig alSettingsForm = ReportUtil.getFormConfig("AL Settings");
		TreeNode settingsNode = getRawConfigurationNode(config.getSettings(), 
				getMsg(ReportConfig.MSG_SETTINGS), 
				ReportConfig.ROOT_NODE, 
				NODE_SETTINGS,
				alSettingsForm);
		assemblyLineInfo.addAssemblyLineNode(settingsNode);
		
		// Add proxy settings
		FormConfig globalForm = ReportUtil.getFormConfig("__GLOBAL__");
		SimulationConfig simSettings = config.getSimulationConfig();
		if (simSettings != null) {
			TreeNode proxySettingsNode = getRawConfigurationNode(simSettings.getProxySettings(), 
					getMsg(ReportConfig.MSG_PROXY_SETTINGS),
					ReportConfig.ROOT_NODE, 
					NODE_PROXY_SETTINGS,
					globalForm);
			assemblyLineInfo.addAssemblyLineNode(proxySettingsNode);
		}
		
		// Add hooks
		TreeNode hooksNode = getHooksNode(config.getHooks(), ReportConfig.ROOT_NODE);
		assemblyLineInfo.addAssemblyLineNode(hooksNode);
		
		// Add containers
		ContainerConfig containerConfig = config.getEntryFeedComponents();
		TreeNode containerNode = getContainerNode(containerConfig, 
				getMsg(ReportConfig.MSG_EFC),
				NODE_EFC);
		assemblyLineInfo.addAssemblyLineNode(containerNode);

		
		containerConfig = config.getDataFlowComponents();
		containerNode = getContainerNode(containerConfig, 
				getMsg(ReportConfig.MSG_DFC),
				NODE_DFC);
		assemblyLineInfo.addAssemblyLineNode(containerNode);
		
		TreeNode operationsNode = getOperationsNode(config);
		assemblyLineInfo.addAssemblyLineNode(operationsNode);
		
		if (references.size() > 0) {
			loadResources(assemblyLineInfo);
		}
		
		references = null;
	}

	/**
	 * 
	 * @param title
	 * @param type
	 * @param configuration
	 */
	protected TreeNode createNode(String title, String type, BaseConfiguration configuration) {
		TreeNode node = new TreeNode(title, type);
		
		String parent = configuration.getInheritsFromRef();
		if ((parent != null) && (!parent.equals(BaseConfiguration.INHERIT_PARENT))) {
			node.setParentRefName(parent);
			
			if (parent.startsWith("/")) {
				references.add(parent);
			}
		}
		node.setUserComments(configuration.getUserComment());	
		return node;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param key
	 * @return String
	 */
	private String getMsg(String key) {
		return reportConfig.getMessage(key);
	}
	
	/**
	 * 
	 * @param title
	 * @return TreeNode
	 */
	private TreeNode createResourceNode(String title) {
		TreeNode node = new TreeNode(title, ReportConfig.ROOT_NODE);
		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getResourceNode(BaseConfiguration config) 
		throws Exception {
		
		if (config == null) {
			return null;
		}
		
		if (config instanceof AttributeMapConfig) {
			AttributeMapConfig attrMapConfig = (AttributeMapConfig) config;
			DataTable table = ReportUtil.getAttributeMapData(attrMapConfig);		
			StringBuffer attrTable = htmlFactory.getDataTable(table);
			if (attrTable == null) {
				return null;
			}
			
			TreeNode node = createNode(config.getShortName(), ReportConfig.COMPONENT_NODE, config);
			node.setImage(ReportConfig.IMG_ATTR_MAP);
			node.addContentItem(attrTable);	  
			return node;
		}		
		
		if (config instanceof ParserConfig) {
			ParserConfig parserConfig = (ParserConfig) config;
			FormConfig form = ReportUtil.getFormConfig(parserConfig.getJavaClass());
			Map<String, String> parameters = ReportUtil.getRawConfiguration(parserConfig, form);
			StringBuffer parametersTable = htmlFactory.getDataTable(parameters);
			if (parametersTable == null) {
				return null;
			}	
			
			TreeNode node = createNode(config.getShortName(), ReportConfig.COMPONENT_NODE, config);
			node.setImage(ReportConfig.IMG_PARSER);
			node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
					ReportUtil.format(config.getEnabled()));		
			node.addContentItem(parametersTable);
			return node;
		}
		
		if (config instanceof FunctionConfig) {
			FunctionConfig functionConfig = (FunctionConfig) config;
			return getFunctionNode(functionConfig); 	    			
		}
		
		if (config instanceof ConnectorConfig) {
			ConnectorConfig connectorConfig = (ConnectorConfig) config;
			return getConnectorNode(connectorConfig); 	    			
		}
		
		if (config instanceof ScriptConfig) {
			ScriptConfig scriptConfig = (ScriptConfig) config;
			return getScriptNode(scriptConfig, ReportConfig.COMPONENT_NODE); 	    			
		}
		
		return null;
	}		
	
	/**
	 * 
	 * @param assemblyLineInfo
	 * @throws Exception
	 */
	private void loadResources(AssemblyLineInfo assemblyLineInfo) 
		throws Exception {
		
		for (String resourceRef : references) {
			
			int index = resourceRef.lastIndexOf("/");
			String folderName = resourceRef.substring(1, index);			
			
			TreeNode folderNode = assemblyLineInfo.getResourceFolderNode(folderName);
			if (folderNode == null) {
				folderNode = createResourceNode(folderName);
				assemblyLineInfo.addResourceFolderNode(folderName, folderNode);
			}
			
			BaseConfiguration config = (BaseConfiguration) 
				assemblyLineInfo.getMetamergeConfig().lookup(resourceRef);
			TreeNode resourceNode = getResourceNode(config);
			folderNode.addNode(resourceNode);
			assemblyLineInfo.addResourceNode(resourceRef, resourceNode);
		}
	}	
	
	/**
	 * 
	 * @param config
	 * @param type
	 * @return TreeNode
	 */
	private TreeNode getScriptNode(ScriptConfig config, String type) {
		TreeNode node = createNode(config.getShortName(), type, config);
		node.setImage(ReportConfig.IMG_SCRIPT);		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
		
		String script = config.getScript();
		script = script.replace("&", "&amp;");
		script = script.replace("<", "&lt;");
		StringBuffer scriptBox = htmlFactory.getScript(script);
		node.addContentItem(scriptBox);
		return node;
	}
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 */
	private TreeNode getHookNode(HookConfig config) {
		TreeNode node = createNode(config.getShortName(), ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_SCRIPT);		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
		
		String script = config.getScript();
		StringBuffer scriptBox = htmlFactory.getScript(script);
		node.addContentItem(scriptBox);
		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @param nodeType
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getHooksNode(HooksConfig config, String nodeType) 
		throws Exception {
		
		if (config == null) {
			return null;
		}
		
		List<String> hookNames = config.getKeys(BaseConfiguration.SUBTREE);
		if ((hookNames == null) || (hookNames.isEmpty())) {
			return null;
		}
		
		TreeNode hooksNode = createNode(getMsg(ReportConfig.MSG_HOOKS), nodeType, config);
		hooksNode.setImage(ReportConfig.IMG_SCRIPT);
		hooksNode.setId(NODE_HOOKS);
		
		Iterator<String> hookNamesIt = hookNames.iterator();
		while (hookNamesIt.hasNext()) {
			Object nextHookName = hookNamesIt.next();			
			HookConfig hookConfig = config.getHook(nextHookName);
			TreeNode hookNode = getHookNode(hookConfig);
			hooksNode.addNode(hookNode);
		}		
		
		return hooksNode;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getOperationsNode(AssemblyLineConfig config) 
		throws Exception {
		
		ContainerConfig operationsConfig = config.getOperations();		
		if (operationsConfig == null) {
			return null;
		}
				
		TreeNode operationsNode = createNode(getMsg(ReportConfig.MSG_OPERATIONS), 
				ReportConfig.ROOT_NODE,
				operationsConfig);
		operationsNode.setImage(ReportConfig.IMG_FOLDER);
		operationsNode.setId(NODE_OPERATIONS);
		
		for (int i = 0; i < operationsConfig.size(); i++) {
			OperationConfig oc = config.getOperation(operationsConfig.getConfig(i).getShortName());
			TreeNode operationNode = getOperationNode(oc);
			operationsNode.addNode(operationNode);
		}
				
		return operationsNode;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 */
	private TreeNode getOperationNode(OperationConfig config) {
		TreeNode node = createNode(config.getShortName(), ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_FOLDER);		
		
		AttributeMapConfig inputAttrMap = config.getAttributeMap(true);
		TreeNode inputAttrMapNode = getInputAttributeMapNode(inputAttrMap);
		node.addNode(inputAttrMapNode);
		
		AttributeMapConfig outputAttrMap = config.getAttributeMap(false);
		TreeNode outputAttrMapNode = getOutputAttributeMapNode(outputAttrMap);
		node.addNode(outputAttrMapNode);

		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getParserNode(ParserConfig config) throws Exception {		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_PARSER), 
				ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_PARSER);
		node.setId(NODE_PARSER);		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
	
		FormConfig form = ReportUtil.getFormConfig(config.getJavaClass());
		
		Map<String, String> parameters = ReportUtil.getRawConfiguration(config, form);
		StringBuffer parametersTable = htmlFactory.getDataTable(parameters);
		if (parametersTable == null) {
			return null;
		}
		
		node.addContentItem(parametersTable);
		return node;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getDeltaConfigNode(DeltaConfig config) throws Exception {
		if (config == null) {
			return null;
		}
		
		if (!config.getEnabled()) {
			return null;
		}
		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_DELTA), ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_DELTA);
		node.setId(NODE_DELTA);
		
		Map<String, String> parameters = ReportUtil.getDeltaInfoMap(config);		
		StringBuffer parametersTable = htmlFactory.getInfoTable(parameters);
		if (parametersTable == null) {
			return null;
		}
		node.addContentItem(parametersTable);
		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getReconnectNode(ReconnectConfig config) throws Exception {
		if (config == null) {
			return null;
		}
		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_RECONNECT), 
				ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_SETTINGS);
		node.setId(NODE_RECONNECT);		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
		
		FormConfig form = ReportUtil.getFormConfig("ConnectorReconnect");		
		Map<String, String> parameters = ReportUtil.getRawConfiguration(config, form);
		StringBuffer parametersTable = htmlFactory.getDataTable(parameters);
		if (parametersTable == null) {
			return null;
		}
		
		node.addContentItem(parametersTable);
		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getLinkCriteriaNode(LinkCriteriaConfig config) throws Exception {
		if (config == null) {
			return null;
		}
		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_LINK_CRITERIA), 
				ReportConfig.TREE_NODE, config);		
		node.setImage(ReportConfig.IMG_SETTINGS);
		node.setId(NODE_LINK_CRITERIA);
		
		Map<String, String> configMap = ReportUtil.getLinkCriteriaInfoMap(config);
    	StringBuffer configTable = htmlFactory.getInfoTable(configMap);
		if (configTable != null) {
			node.addContentItem(configTable);
		}		
		
		DataTable dataTable = ReportUtil.getLinkCriteriaData(config);	
		StringBuffer paramTable = htmlFactory.getDataTable(dataTable);
		if (paramTable != null) {
			node.addContentItem(paramTable);
		}
			
		if ((configTable == null) && (paramTable == null)) {
			return null;
		}
		return node;
	}	
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 */
	private TreeNode getInputAttributeMapNode(AttributeMapConfig config) {
		if (config == null) {
			return null;
		}
		
		DataTable table = ReportUtil.getAttributeMapData(config);		
		StringBuffer attrTable = htmlFactory.getDataTable(table);
		if (attrTable == null) {
			return null;
		}
		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_INATTRMAP), 
				ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_ATTR_MAP);
		node.setId(NODE_INATTRMAP);
		node.addContentItem(attrTable);
		return node;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 */
	private TreeNode getOutputAttributeMapNode(AttributeMapConfig config) {
		if (config == null) {
			return null;
		}
		
		DataTable table = ReportUtil.getAttributeMapData(config);		
		StringBuffer attrTable = htmlFactory.getDataTable(table);
		if (attrTable == null) {
			return null;
		}
		
		TreeNode node = createNode(getMsg(ReportConfig.MSG_OUTATTRMAP), 
				ReportConfig.TREE_NODE, config);
		node.setImage(ReportConfig.IMG_ATTR_MAP);
		node.setId(NODE_OUTATTRMAP);
		node.addContentItem(attrTable);
		return node;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getComponentNode(BaseConfiguration config) 
		throws Exception {
		
		if (config instanceof FunctionConfig) {
			FunctionConfig functionConfig = (FunctionConfig) config;
			return getFunctionNode(functionConfig); 	    			
		}
		
		if (config instanceof ConnectorConfig) {
			ConnectorConfig connectorConfig = (ConnectorConfig) config;
			return getConnectorNode(connectorConfig); 	    			
		}
		
		if (config instanceof BranchingConfig) {
			BranchingConfig branchingConfig = (BranchingConfig) config;
			return getBranchNode(branchingConfig); 	    			
		}
		
		if (config instanceof ScriptConfig) {
			ScriptConfig scriptConfig = (ScriptConfig) config;
			return getScriptNode(scriptConfig, ReportConfig.COMPONENT_NODE); 	    			
		}
		
		return null;
	}		
	

	/**
	 * 
	 * @param config
	 * @param containerName
	 * @param nodeId
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getContainerNode(ContainerConfig config, 
			String containerName, String nodeId) throws Exception {
		
		if (config == null) {
			return null;
		}
		
		List<String> componentNames = config.getChildNames();
		if ((componentNames == null) || (componentNames.isEmpty())){
			return null;
		}
		
		TreeNode node = createNode(containerName, ReportConfig.ROOT_NODE, config);
		node.setImage(ReportConfig.IMG_FOLDER);
		node.setId(nodeId);
			
		for (String name: componentNames) {
			node.addNode(getComponentNode(config.getConfig(name)));
		}
		
		return node;
	}		
		
	/**
	 * 
	 * @param config
	 * @param nodeName
	 * @param nodeType
	 * @param nodeId
	 * @param form
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getRawConfigurationNode(BaseConfiguration config, 
			String nodeName, 
			String nodeType, 
			String nodeId,
			FormConfig form) throws Exception {
		
		if (config == null) {
			return null;
		}
		
		TreeNode node = createNode(nodeName, nodeType, config);
		node.setId(nodeId);
		node.setImage(ReportConfig.IMG_SETTINGS);
		
		Map<String, String> parameters = ReportUtil.getRawConfiguration(config, form);
		StringBuffer parametersTable = htmlFactory.getDataTable(parameters);
		if (parametersTable == null) {
			return null;
		}
		node.addContentItem(parametersTable);	
		return node;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getConnectorNode(ConnectorConfig config) 
		throws Exception {
		
		TreeNode node = createNode(config.getShortName(),	ReportConfig.COMPONENT_NODE, config);	
		node.setImage(ReportUtil.getConnectorImage(config.getMode()));
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_MODE), 
				config.getMode());
		
		String javaClass = config.getConnectionConfig().getJavaClass();
		FormConfig form = ReportUtil.getFormConfig(javaClass);
		
		// Add configuration
		RawConnectorConfig rawConfig = config.getConnectionConfig();
		TreeNode configNode = getRawConfigurationNode(rawConfig, 
				getMsg(ReportConfig.MSG_CONFIGURATION), 
				ReportConfig.TREE_NODE, 
				NODE_CONFIGURATION,
				form);
		node.addNode(configNode);
		
		ParserConfig parserConfig = config.getParserConfig();
		if (parserConfig != null) {
			TreeNode parserNode = getParserNode(parserConfig);
			node.addNode(parserNode);
		}
		
		AttributeMapConfig inputAttrMap = config.getAttributeMap(true);
		TreeNode inputAttrMapNode = getInputAttributeMapNode(inputAttrMap);
		node.addNode(inputAttrMapNode);
		
		AttributeMapConfig outputAttrMap = config.getAttributeMap(false);
		TreeNode outputAttrMapNode = getOutputAttributeMapNode(outputAttrMap);
		node.addNode(outputAttrMapNode);
		
		TreeNode deltaNode = getDeltaConfigNode(config.getDeltaConfig());
		node.addNode(deltaNode);
		
		TreeNode reconnectNode = getReconnectNode(config.getReconnectConfig());
		node.addNode(reconnectNode);
		
		TreeNode linkCriteriaNode = getLinkCriteriaNode(config.getLinkCriteria());
		node.addNode(linkCriteriaNode);
		
		TreeNode hooksNode = getHooksNode(config.getHooks(), ReportConfig.TREE_NODE);
		node.addNode(hooksNode);
		
		return node;
	}		
	
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getFunctionNode(FunctionConfig config) 
		throws Exception {
		
		TreeNode node = createNode(config.getShortName(), ReportConfig.COMPONENT_NODE, config);	
		node.setImage(ReportConfig.IMG_FUNCTION);
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));
		
		String javaClass = config.getJavaClass();
		FormConfig form = ReportUtil.getFormConfig(javaClass);
		
		// Add configuration
		RawFunctionConfig rawConfig = config.getFunctionConfig();
		
		TreeNode configNode = getRawConfigurationNode(rawConfig, 
				getMsg(ReportConfig.MSG_CONFIGURATION), 
				ReportConfig.TREE_NODE, 
				NODE_CONFIGURATION,
				form);
		node.addNode(configNode);
		
		String script = rawConfig.getScript();
		if (script != null) {
			TreeNode scriptNode = new TreeNode(getMsg(ReportConfig.MSG_SCRIPT), 
					ReportConfig.TREE_NODE);
			scriptNode.setImage(ReportConfig.IMG_SCRIPT);		
			StringBuffer scriptBox = htmlFactory.getScript(script);
			scriptNode.addContentItem(scriptBox);
			node.addNode(scriptNode);
		}
		
		ParserConfig parserConfig = config.getParserConfig();
		if (parserConfig != null) {
			TreeNode parserNode = getParserNode(parserConfig);
			node.addNode(parserNode);
		}
		
		AttributeMapConfig inputAttrMap = config.getAttributeMap(true);
		TreeNode inputAttrMapNode = getInputAttributeMapNode(inputAttrMap);
		node.addNode(inputAttrMapNode);
		
		AttributeMapConfig outputAttrMap = config.getAttributeMap(false);
		TreeNode outputAttrMapNode = getOutputAttributeMapNode(outputAttrMap);
		node.addNode(outputAttrMapNode);
				
		TreeNode hooksNode = getHooksNode(config.getHooks(), ReportConfig.TREE_NODE);
		node.addNode(hooksNode);
		
		return node;
	}		
		
	/**
	 * 
	 * @param config
	 * @return TreeNode
	 * @throws Exception
	 */
	private TreeNode getBranchNode(BranchingConfig config) 
		throws Exception {
		
		TreeNode node = createNode(config.getShortName(), ReportConfig.COMPONENT_NODE, config);
		if(config.getEnabled())
			node.setImage(ReportConfig.IMG_BRANCH);
		else
			node.setImage(ReportConfig.IMG_BRANCH_DISABLED);		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_ENABLED), 
				ReportUtil.format(config.getEnabled()));		
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_TYPE), 
				ReportUtil.format(config.getBranchType()));
		node.addProperty(getMsg(ReportConfig.MSG_PROPERTY_MATCH_ANY), 
				ReportUtil.format(config.getMatchAny()));
		
		String script = config.getScript();
		if (script != null) {
			StringBuffer scriptBox = htmlFactory.getScript(script);
			node.addContentItem(scriptBox);
		}
		
		ContainerConfig conditions = config.getConditions();
		if (conditions != null) {
			DataTable dataTable = ReportUtil.getBranchConditionsData(conditions);
			StringBuffer conditionsTable = htmlFactory.getDataTable(dataTable);
			if (conditionsTable != null) {
				node.addContentItem(conditionsTable);	
			}
		}
		
		for (int i = 0; i < config.size(); i++) {
			node.addNode(getComponentNode(config.getConfig(i)));
		}
		
		return node;
	}		
	
}
