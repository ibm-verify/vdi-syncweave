/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.model.def.ModelObjectDefinition;
import com.ibm.di.connector.ccmdb.model.def.OMPRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.model.def.RelationRuleDefinition;
import com.ibm.di.connector.ccmdb.model.def.RelationRuleSet;

/**
 * This class defines methods for reading of definition objects.
 * 
 * @author yavor.gologanov
 *
 */
public class DefinitionProvider {

	private CCMDBActualCIProvider provider = null;
	
	private Map<String, List<PropertyDefinition>> propDefCache = 
		new HashMap<String, List<PropertyDefinition>>();

	private Map<String, ActualCIDefinition> configItemDefCache = 
		new WeakHashMap<String, ActualCIDefinition>();
	
	private Map<String, CIRelationDefinition> relationDefCache = 
		new WeakHashMap<String, CIRelationDefinition>();		
	
	/**
	 * 
	 * @param provider
	 */
	protected DefinitionProvider(CCMDBActualCIProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * 
	 * @param className
	 * @return ActualCIDefinition
	 * @throws SQLException
	 */
	public ActualCIDefinition getActualCIDefinition(String className) 
		throws SQLException {
	
		if (configItemDefCache.containsKey(className)) {
			return configItemDefCache.get(className);
		}
		ActualCIDefinition definition = null;
		
		ClassificationProvider classificationProvider = provider.getClassificationProvider();
		Classification classification = classificationProvider.getClasssification(className);
		if (classification == null) {
			provider.getContext().getLog().debug("Unsupported classification: " + className);
			classification = classificationProvider.getClasssification(ClassificationProvider.UNCLASSIFIED);
			definition = new ActualCIDefinition(classification);
			definition.setVisible(false);
		} else {		
			definition = new ActualCIDefinition(classification);
			if (className.equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
				loadBaseDefinition(definition);
				loadModelObjectAttributes(definition);
			} else {
				loadDefinition(definition);
			}
			configItemDefCache.put(className, definition);
		}
		return definition;
	}		
	
	/**
	 * 
	 * @param className
	 * @return CIRelationDefinition
	 * @throws SQLException
	 */
	public CIRelationDefinition getCIRelationDefinition(String className) 
		throws SQLException {
		
		if (relationDefCache.containsKey(className)) {
			return relationDefCache.get(className);
		}
		
		ClassificationProvider classificationProvider = provider.getClassificationProvider();
		Classification classification = classificationProvider.getClasssification(className);
		CIRelationDefinition definition = null;
		if (classification == null) {
			provider.getContext().getLog().debug("Unsupported classification: " + className);
			classification = classificationProvider.getClasssification(ClassificationProvider.UNCLASSIFIED_RELATION);
			definition = new CIRelationDefinition(classification);
			definition.setVisible(false);
		} else {		
			definition = new CIRelationDefinition(classification);
			loadDefinition(definition);
			relationDefCache.put(className, definition);
		}
		return definition;
	}				
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	public void loadSourceRelationRules(ActualCIDefinition definition) 
		throws SQLException {
			
		if (definition.getSourceRelationRules() != null) {
			return;
		}
		
		RelationRuleSet ruleSet = new RelationRuleSet();
		Classification classification = definition.getClassification();
		
		String sql = provider.getSQL(QuerySet.RELATIONRULES_SELECT_BY_SRC);		
		PreparedStatement statement = provider.getConnection().prepareStatement(sql);
		try {
			statement.setString(1, classification.getClassstructureId());	
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {					
					String relationName = resultSet.getString(1);
					String actciClassification = resultSet.getString(2);
					
					RelationRuleDefinition ruleDef = new RelationRuleDefinition(relationName);
					ruleDef.setSourceClassification(classification.getClassName());
					ruleDef.setTargetClassification(actciClassification);
					ruleSet.addRelationRule(ruleDef);
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}
		} finally {
			statement.close();
		}
		
		definition.setSourceRelationRules(ruleSet);
	}				
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	public void loadTargetRelationRules(ActualCIDefinition definition) 
		throws SQLException {
			
		if (definition.getTargetRelationRules() != null) {
			return;
		}
		
		RelationRuleSet ruleSet = new RelationRuleSet();
		Classification classification = definition.getClassification();
		
		String sql = provider.getSQL(QuerySet.RELATIONRULES_SELECT_BY_TRG);		
		PreparedStatement statement = provider.getConnection().prepareStatement(sql);
		try {
			statement.setString(1, classification.getClassstructureId());	
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {					
					String relationName = resultSet.getString(1);
					String actciClassification = resultSet.getString(2);
					
					RelationRuleDefinition ruleDef = new RelationRuleDefinition(relationName);
					ruleDef.setSourceClassification(actciClassification);
					ruleDef.setTargetClassification(classification.getClassName());
					ruleSet.addRelationRule(ruleDef);
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}
		} finally {
			statement.close();
		}
		
		definition.setTargetRelationRules(ruleSet);
	}	
		
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	public void loadRelationRules(CIRelationDefinition definition) 
		throws SQLException {

		if (definition.getRelationRules() != null) {
			return;
		}
		
		Classification classification = definition.getClassification();
		
		if (definition.getClassName().equals(CCMDBActualCIProvider.CLASS_CI_RELATION)) {
			List<RelationRuleDefinition> rules = new ArrayList<RelationRuleDefinition>();
			RelationRuleDefinition ruleDef = new RelationRuleDefinition(classification.getClassName());
			ruleDef.setSourceClassification(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
			ruleDef.setTargetClassification(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
			rules.add(ruleDef);
			definition.setRelationRules(rules);
			return;
		}
		
		List<RelationRuleDefinition> rules = new ArrayList<RelationRuleDefinition>();		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String sql = provider.getSQL(QuerySet.RELATIONRULES_SELECT_ST);
		try {
			statement = provider.getConnection().prepareStatement(sql);
			statement.setString(1, classification.getClassName());
			resultSet = statement.executeQuery();

			while (resultSet.next()) {
				String sourceClassification = resultSet.getString(1);
				String targetClassification = resultSet.getString(2);
				
				RelationRuleDefinition ruleDef = new RelationRuleDefinition(classification.getClassName());
				ruleDef.setSourceClassification(sourceClassification);
				ruleDef.setTargetClassification(targetClassification);
				rules.add(ruleDef);
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		
		definition.setRelationRules(rules);
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	private void loadBaseDefinition(ActualCIDefinition definition) 
		throws SQLException {

		List<PropertyDefinition> properties = getPropertyDefinitions(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
		for(PropertyDefinition propDef : properties) {
			PropertyDefinition propDefCopy = new PropertyDefinition(propDef.getName());
			propDefCopy.setJavaClassName(propDef.getJavaClassName());
			definition.addProperty(propDefCopy);
			
			if (propDef.getName().equals(CCMDBActualCISchema.ACTCI_ACTCIID)) {
				propDefCopy.setVisible(false);
			}
		}		
	}
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	private void loadDefinition(ActualCIDefinition definition) 
		throws SQLException {
		
		loadBaseDefinition(definition);
		
		// Set deleted actual CI properties
		ModelObjectDefinition actciDelDefinition = 
			new ModelObjectDefinition(CCMDBActualCIProvider.CLASSS_DELETED_ACTUAL_CI);
		List<PropertyDefinition> actcidelProperties 
			= getPropertyDefinitions(CCMDBActualCIProvider.CLASSS_DELETED_ACTUAL_CI);
		for(PropertyDefinition propDef : actcidelProperties) {
			PropertyDefinition propDefCopy = new PropertyDefinition(propDef.getName());
			propDefCopy.setJavaClassName(propDef.getJavaClassName());
			actciDelDefinition.addProperty(propDefCopy);
		}
		definition.setDeletedActualCI(actciDelDefinition);
		
		// Set OMP relation properties
		ModelObjectDefinition ompDefinition = 
			new ModelObjectDefinition(CCMDBActualCIProvider.CLASS_OMP);
		List<PropertyDefinition> ompProperties 
			= getPropertyDefinitions(CCMDBActualCIProvider.CLASS_OMP);
		for(PropertyDefinition propDef : ompProperties) {
			PropertyDefinition propDefCopy = new PropertyDefinition(propDef.getName());
			propDefCopy.setJavaClassName(propDef.getJavaClassName());
			ompDefinition.addProperty(propDefCopy);
			
			if (propDef.getName().equals(CCMDBActualCISchema.OMP_OMPID)) {
				propDefCopy.setVisible(false);
			}
		}
				
		OMPRelationDefinition ompRelationDefinition =
				new OMPRelationDefinition(CCMDBActualCIProvider.CLASS_OMPRELATION);
		List<PropertyDefinition> ompRelProperties 
			= getPropertyDefinitions(CCMDBActualCIProvider.CLASS_OMPRELATION);
		for(PropertyDefinition propDef : ompRelProperties) {
			PropertyDefinition propDefCopy = new PropertyDefinition(propDef.getName());
			propDefCopy.setJavaClassName(propDef.getJavaClassName());
			ompRelationDefinition.addProperty(propDefCopy);
			
			if (propDef.getName().equals(CCMDBActualCISchema.OMPCIRLN_OMPCIRLNID)) {
				propDefCopy.setVisible(false);
			}
		}
		ompRelationDefinition.setOmp(ompDefinition);
		definition.setOmpRelation(ompRelationDefinition);
		
		// Load attributes
		loadAttributes(definition);
	}	
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	private void loadDefinition(CIRelationDefinition definition) 
		throws SQLException {
		
		List<PropertyDefinition> properties = 
			getPropertyDefinitions(CCMDBActualCIProvider.CLASS_CI_RELATION);
		
		for(PropertyDefinition propDef : properties) {
			PropertyDefinition clone = new PropertyDefinition(propDef.getName());
			clone.setJavaClassName(propDef.getJavaClassName());
			definition.addProperty(clone);
			
			if (propDef.getName().equals(CCMDBActualCISchema.ACTCIRELATION_ACTCIRELATIONID)) {
				clone.setVisible(false);
			}
		}			
	}	
	
	/**
	 * 
	 * @param className
	 * @return List<PropertyDefinition>
	 * @throws SQLException
	 */
	private List<PropertyDefinition> getPropertyDefinitions(String className) 
		throws SQLException {
		
		if (!propDefCache.containsKey(className)) {
			loadClassProperties(className);
		}
		return propDefCache.get(className);
	}	
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	private void loadAttributes(ActualCIDefinition definition) 
		throws SQLException {
		
		Classification classification = definition.getClassification();
		
		String sql = provider.getSQL(QuerySet.CLASSSPEC_SELECT_BY_CLASSSTRUCTURE);
		PreparedStatement statement = provider.getConnection().prepareStatement(sql);			
		try {
			statement.setString(1, classification.getClassstructureId());
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					String name = resultSet.getString(1);
					String type = resultSet.getString(2);	
					String valueField = CCMDBActualCISchema.getAttrValueColumn(type);
					String javaType = CCMDBActualCISchema.getAttrJavaClass(type);
					
					AttributeDefinition attrDef = new AttributeDefinition(name);
					attrDef.setJavaClassName(javaType);
					attrDef.setValueField(valueField);
					definition.addAttribute(attrDef);
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}
		} finally {
			statement.close();
		}
	}		
	
	/**
	 * 
	 * @param definition
	 * @throws SQLException
	 */
	private void loadModelObjectAttributes(ActualCIDefinition definition) 
		throws SQLException {
		
		String sql = provider.getSQL(QuerySet.ASSETATTRID_SELECT_MODEL_OBJECT);
		PreparedStatement statement = provider.getConnection().prepareStatement(sql);			
		try {
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					String name = resultSet.getString(1);
					String type = resultSet.getString(2);	
					String valueField = CCMDBActualCISchema.getAttrValueColumn(type);
					String javaType = CCMDBActualCISchema.getAttrJavaClass(type);
					
					AttributeDefinition attrDef = new AttributeDefinition(name);
					attrDef.setJavaClassName(javaType);
					attrDef.setValueField(valueField);
					definition.addAttribute(attrDef);
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}
		} finally {
			statement.close();
		}
	}	
	
	/**
	 * 
	 * @param className
	 * @throws SQLException
	 */
	private void loadClassProperties(String className) throws SQLException {

		String queryName = null;
		if (className.equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
			queryName = QuerySet.ACTCI_SELECT_PROPERTIES_DEF;
		} else if (className.equals(CCMDBActualCIProvider.CLASSS_DELETED_ACTUAL_CI)) {
			queryName = QuerySet.CCIDELETEDACTCI_SELECT_PROPERTIES_DEF;
		} else if (className.equals(CCMDBActualCIProvider.CLASS_OMPRELATION)) {
			queryName = QuerySet.OMPCIRLN_SELECT_PROPERTIES_DEF;
		} else if (className.equals(CCMDBActualCIProvider.CLASS_OMP)) {
			queryName = QuerySet.OMP_SELECT_PROPERTIES_DEF;
		} else if (className.equals(CCMDBActualCIProvider.CLASS_CI_RELATION)) {
			queryName = QuerySet.ACTCIRELATION_SELECT_PROPERTIES_DEF;
		}
		
		if (queryName != null) {
			List<PropertyDefinition> propDefList = loadPropertyDefinitions(queryName);
			propDefCache.put(className, propDefList);
		}

	}	
	
	/**
	 * 
	 * @param queryName
	 * @return List<PropertyDefinition>
	 * @throws SQLException
	 */
	private List<PropertyDefinition> loadPropertyDefinitions(String queryName)
		throws SQLException {
		
		List<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();		
		String sqlQuery = provider.getSQL(queryName);	
		Statement statement = provider.getConnection().createStatement();		
		try {
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			try {
				if (resultSet.next()) {
					ResultSetMetaData metaData = resultSet.getMetaData();
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						String name = metaData.getColumnName(i);
						String type = metaData.getColumnClassName(i);
						PropertyDefinition propDef = new PropertyDefinition(name);
						propDef.setJavaClassName(type);
						properties.add(propDef);
					}
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}
		} finally {
			statement.close();
		}
		
		return properties;
	}	
		
}
