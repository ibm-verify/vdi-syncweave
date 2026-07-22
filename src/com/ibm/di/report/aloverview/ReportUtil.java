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

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.BranchingFactory;
import com.ibm.di.config.xml.ConnectorFactory;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class ReportUtil {
	
	/**
	 * 
	 * @param mode
	 * @return String
	 */
	public static String getConnectorImage(String mode) {
		
		if (ConnectorConfig.ITERATOR_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_ITERATOR;
		}
		if (ConnectorConfig.DELTA_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_DELTA;
		}
		if (ConnectorConfig.LOOKUP_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_LOOKUP;
		}
		if (ConnectorConfig.SERVER_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_SERVER;
		}
		if (ConnectorConfig.UPDATE_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_UPDATE;
		}
		if (ConnectorConfig.ADDONLY_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_ADDONLY;
		}
		if (ConnectorConfig.CALL_REPLY_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_CALLREPLY;
		}
		if (ConnectorConfig.DELETE_MODE.equals(mode)) {
			return ReportConfig.IMG_CONN_DELETE;
		}
		
		return ReportConfig.IMG_CONNECTOR;
	}	
	
	/**
	 * 
	 * @param configuration
	 * @param form
	 * @return Map<String, String>
	 */
	public static Map<String, String> getRawConfiguration(BaseConfiguration configuration, 
			FormConfig form) {

		Map<String, String> parameters = new TreeMap<String, String>();
		
		List<String> paramNames = configuration.getKeys(BaseConfiguration.ONE_LEVEL); 
		if ((paramNames == null) || (paramNames.isEmpty())) {
			return null;
		}			
		
		for (String paramName : paramNames) {
			if (paramName.equals(InternalSchema.INHERITS_FROM)) {
				continue;
			}
			if (paramName.equals(InternalSchema.USER_COMMENT)) {
				continue;
			}
			if (paramName.equals(InternalSchema.DEBUG)) {
				continue;
			}
			if (paramName.equals(InternalSchema.SCRIPT)) {
				continue;
			}
			
			Object value = configuration.getParameterRaw(paramName);
			
			if (form != null) {
				FormItemConfig fic = form.getFormItem(paramName);
				if (fic != null) {
					paramName = fic.getLabel();
					if ("password".equalsIgnoreCase(fic.getSyntax())) {							
						value = "*****";
					}
				}
			} 
			parameters.put(paramName, format(value));
		} 
		return parameters;
	}
	
	/**
	 * 
	 * @param deltaConfig
	 * @return Map<String, String>
	 */
	public static Map<String, String> getDeltaInfoMap(DeltaConfig deltaConfig) 
		throws Exception {
		
		List<String> attrNames = new ArrayList<String>();
		attrNames.add(InternalSchema.ENABLED);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_DB);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_ITER_DELETED);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_WHEN_TO_COMMIT);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_DRIVER);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_LEVEL);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_ROW_LOCKING);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_CHANGE_DETECTION_MODE);
		attrNames.add(InternalSchema.CONNECTOR_DELTA_ATTRIBUTE_LIST);

		FormConfig form = ReportUtil.getFormConfig("com.ibm.di.fc.DeltaFC");
		
		Map<String, String> infoMap = new TreeMap<String, String>();
		for (String paramName : attrNames) {
			Object paramValue = deltaConfig.getParameterRaw(paramName);
			if (paramValue != null) {
				if (form != null) {
					FormItemConfig fic = form.getFormItem(paramName);
					if (fic != null) {
						paramName = fic.getLabel();
					}
				} 		
				infoMap.put(paramName, format(paramValue));
			}
		}
		
		return infoMap;
	}	
	
	/**
	 * 
	 * @param linkCriteriaConfig
	 * @return Map<String, String>
	 * @throws Exception
	 */
	public static Map<String, String> getLinkCriteriaInfoMap(LinkCriteriaConfig linkCriteriaConfig) 
		throws Exception {
		List<String> attrNames = new ArrayList<String>();
		attrNames.add(InternalSchema.CONNECTOR_LINK_OR);
		attrNames.add(InternalSchema.CONNECTOR_LINK_MODE);
		attrNames.add(InternalSchema.CONNECTOR_ADVANCED_LINK_CRITERIA);
		
		FormConfig form = ReportUtil.getFormConfig("Link Criteria");
		
		Map<String, String> infoMap = new TreeMap<String, String>();
		for (String paramName : attrNames) {
			Object paramValue = linkCriteriaConfig.getParameterRaw(paramName);
			if (paramValue != null) {
				if (form != null) {
					FormItemConfig fic = form.getFormItem(paramName);
					if (fic != null) {
						paramName = fic.getLabel();
					}
				} 				
				infoMap.put(paramName, format(paramValue));
			}
		}
				
		return infoMap;
	}	
	
	/**
	 * 
	 * @param linkCriteriaConfig
	 * @return DataTable
	 */
	public static DataTable getLinkCriteriaData(LinkCriteriaConfig linkCriteriaConfig) {
		
		List<String> itemHeaders = new ArrayList<String>();
		itemHeaders.add(ConnectorFactory.LCI_KEY);
		itemHeaders.add(ConnectorFactory.LCI_ATTRIBUTE);
		itemHeaders.add(ConnectorFactory.LCI_OPERAND);
		itemHeaders.add(ConnectorFactory.LCI_VALUE);
		
		DataTable table = new DataTable(itemHeaders);
		List<String> list = linkCriteriaConfig.getCriteriaNames();
		for (int i = 0; i < list.size(); i++) {
			if (!linkCriteriaConfig.isCriteriaLocal(list.get(i))) {
				continue;
			}
			List<String> item = new ArrayList<String>();
			
			LinkCriteriaItem lci = linkCriteriaConfig.getCriteria(list.get(i));
			item.add(lci.getShortName());
			item.add(format(lci.getParameterRaw(InternalSchema.LC_ATTRIBUTE)));
			item.add(format(lci.getParameterRaw(InternalSchema.LC_OPERATOR)));
			item.add(format(lci.getParameterRaw(InternalSchema.LC_VALUE)));
			table.addRow(item);
		}
		
		return table;
	}	
	
	/**
	 * 
	 * @param config
	 * @return DataTable
	 */
	public static DataTable getBranchConditionsData(ContainerConfig config) 
		throws Exception {
		
		FormConfig form = ReportUtil.getFormConfig("BranchCondition");
		
		List<String> itemHeaders = new ArrayList<String>();
		itemHeaders.add(BranchingFactory.LEFT_HAND_TAG);
		itemHeaders.add(BranchingFactory.OPERATOR_TAG);
		itemHeaders.add(BranchingFactory.RIGHT_HAND_TAG);
		itemHeaders.add(BranchingFactory.NEGATE_TAG);
		itemHeaders.add(BranchingFactory.CASESENSITIVE_TAG);
		itemHeaders.add(BranchingFactory.MATCH_ANY_TAG);
		
		List<String> itemHeaderNames = new ArrayList<String>();
		for (String header : itemHeaders) {
			if (form != null) {
				FormItemConfig fic = form.getFormItem(header);
				if (fic != null) {
					itemHeaderNames.add(fic.getLabel());
				} else {
					itemHeaderNames.add(header);
				}
			} 		
		}
		
		DataTable table = new DataTable(itemHeaderNames);	
		for (int i = 0; i < config.size(); i++) {
			BaseConfiguration nextConfig = config.getConfig(i);
			if (nextConfig instanceof BranchCondition) {
				BranchCondition bc = (BranchCondition) nextConfig;
				
				List<String> item = new ArrayList<String>();
				item.add(format(bc.getParameterRaw(InternalSchema.BRANCH_CONDITION_LEFT)));
				item.add(format(bc.getParameterRaw(InternalSchema.BRANCH_CONDITION_OPER)));
				item.add(format(bc.getParameterRaw(InternalSchema.BRANCH_CONDITION_RIGHT)));
				item.add(format(bc.getParameterRaw(InternalSchema.BRANCH_CONDITION_NEGATE)));
				item.add(format(bc.getParameterRaw(InternalSchema.BRANCH_CONDITION_CASE_SENSITIVE)));
				item.add(format(bc.getMatchAny()));
				table.addRow(item);
			}
		}		
	
		return table;
	}	
	
	/**
	 * 
	 * @param attrMap
	 * @return DataTable
	 */
	public static DataTable getAttributeMapData(AttributeMapConfig attrMap) {
		
		List<String> itemHeaders = new ArrayList<String>();
		itemHeaders.add(ConnectorFactory.AMI_NAME);
		itemHeaders.add(ConnectorFactory.AMI_TYPE);
		itemHeaders.add(ConnectorFactory.AMI_ENABLED);
		itemHeaders.add(ConnectorFactory.AMI_ADD);
		itemHeaders.add(ConnectorFactory.AMI_MODIFY);
		itemHeaders.add(ConnectorFactory.AMI_SCRIPT);
		itemHeaders.add(ConnectorFactory.AMI_SIMPLE);
		itemHeaders.add(ConnectorFactory.AMI_SUBST_TEMPLATE);
		
		DataTable table = new DataTable(itemHeaders);
		List<String> list = attrMap.getKeys(BaseConfiguration.SUBTREE);
		for (int i = 0; i < list.size(); i++) {
			AttributeMapItem ami = attrMap.getAttributeMapItem(list.get(i));
			if (ami.size() == 0) {
				continue;
			}
			
			List<String> item = new ArrayList<String>();

			item.add(list.get(i));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_TYPE)));
			item.add(format(ami.getParameterRaw(InternalSchema.ENABLED)));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_ADD)));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_MODIFY)));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_SCRIPT)));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_SIMPLE)));
			item.add(format(ami.getParameterRaw(InternalSchema.AMI_SUBSTITUTION)));
			table.addRow(item);
		}
		
		return table;
	}		
	
	/**
	 * 
	 * @param paramValue
	 * @return String
	 */
	public static String format(Object paramValue) {
		if (paramValue == null) {
			return "&nbsp;";
		}
		
		return String.valueOf(paramValue);
	}
	
	/**
	 * 
	 * @param formName
	 * @return FormConfig
	 * @throws Exception
	 */
	public static FormConfig getFormConfig(String formName) throws Exception {
		if (formName == null) {
			return null;
		}
		
		return (FormConfig) MetamergeConfigFactory.lookup(null, "system:/Forms/" + formName);
	}
	
}
