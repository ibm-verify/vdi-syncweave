/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.cdm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.RelationRuleDefinition;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This class is responsible for creating of IdML compatible schema entries 
 * from data model definition objects.
 * 
 * @author yavor.gologanov
 *
 */
public class CDMSchemaFactory {

	public static final String ATTR_CLASS_TYPE_NAME = "@ClassType";
	public static final String ATTR_GUID_NAME = "@Guid";	
	public static final String ATTR_CLASSIFICATION = "@Classification";
	
	public static final String DISPLAY_NAME = "DisplayName";
	
	public static final String ATTR_MSS = "mss";	
	public static final String MSS_CLASS = "process.ManagementSoftwareSystem";
	public static final String MSS_PRODUCT_VERSION = "ProductVersion";
	public static final String MSS_GUID = "Guid";
	public static final String MSS_LMT = "LastModifiedTime";
	public static final String MSS_DISPLAY_NAME = "DisplayName";
	public static final String MSS_LMBY = "LastModifiedBy";
	public static final String MSS_NAME = "MSSName";
	public static final String MSS_PRODUCT_NAME = "ProductName";	
	
	
	private CDMMetaData metaData = null;
	
	/**
	 * 
	 * @param metaData
	 */
	public CDMSchemaFactory(CDMMetaData metaData) {
		this.metaData = metaData;
	}	
	
	/**
	 * Creates a DI Entry from a given instance of ActualCIDefinition.
	 * 
	 * @param definition
	 * @return Entry
	 * @throws CCMDBException
	 */
	public Entry createSchema(ActualCIDefinition definition) 
		throws CCMDBException {
	
		Entry schema = new Entry(true);
		
		if (definition.isVisible()) {
			schema.setAttribute(ATTR_CLASS_TYPE_NAME, String.class.getCanonicalName());
			schema.setAttribute(ATTR_GUID_NAME, String.class.getCanonicalName());
			schema.setAttribute(ATTR_CLASSIFICATION, String.class.getCanonicalName());
		
			EntryUtilities.addPropertiesToSchema(definition, schema);
			EntryUtilities.addAttributesToSchema(definition.getAttributes(), schema);	
			
			addRelationDefinitions(definition, schema);	
		
			if (definition.getOmpRelation() != null) {
				Attribute mssAttr = createMSSAttribute(schema);
				schema.appendChild(mssAttr);
			}
		} 
		return schema;
	}
	
	/**
	 * Creates a DI Entry from a given instance of CIRelationDefinition.
	 * 
	 * @param definition
	 * @return Entry
	 * @throws CCMDBException
	 */
	public Entry createSchema(CIRelationDefinition definition) 
		throws CCMDBException {
		
		Entry schema = new Entry(true);	
		
		if (definition.isVisible()) {
			Attribute relationAttr = new Attribute(definition.getDisplayName());
			addRelationRulesDefinitions(definition, relationAttr, schema);
		
			schema.setAttribute(definition.getDisplayName(), relationAttr);
		}
		return schema;
	}	
		
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param itemDefinition
	 * @throws CCMDBException
	 */
	private void addRelationDefinitions(ActualCIDefinition itemDefinition, 
			Entry schema) throws CCMDBException {
		
		Set<String> relationNames = itemDefinition.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		for (String relationName : relationNames) {
			
			List<RelationRuleDefinition> srcRules = itemDefinition.getSourceRelationRules(relationName);
			List<RelationRuleDefinition> trgRules = itemDefinition.getTargetRelationRules(relationName);
			
			if (((srcRules != null) && (srcRules.size() > 0))
					|| ((trgRules != null) && (trgRules.size() > 0))) {
			
				CIRelationDefinition relDefinition = metaData.getCIRelationDefinition(relationName);
				String attrName = CDMConstants.CDM_RELATIONSHIP_PREFIX + relDefinition.getDisplayName();
				Attribute relationAttr = new Attribute(attrName);	
			
				if ((srcRules != null) && (srcRules.size() > 0)) {
					for (RelationRuleDefinition ruleDef : srcRules) {
						String targetClassification = ruleDef.getTargetClassification();
						ActualCIDefinition actciDefinition = 
							metaData.getActualCIDefinition(targetClassification);
						Attribute targetAttr = createRSTAttribute(schema, 
								CDMConstants.CDM_TARGET_CI_PREFIX, 
								actciDefinition);
						relationAttr.appendChild(targetAttr);
					}
				}
			
				if ((trgRules != null) && (trgRules.size() > 0)) {
					for (RelationRuleDefinition ruleDef : trgRules) {
						String sourceClassification = ruleDef.getSourceClassification();
						ActualCIDefinition actciDefinition = 
							metaData.getActualCIDefinition(sourceClassification);
						Attribute sourceAttr = createRSTAttribute(schema, 
								CDMConstants.CDM_SOURCE_CI_PREFIX, 
								actciDefinition);
						relationAttr.appendChild(sourceAttr);
					}
				}
			
				schema.appendChild(relationAttr);
			}
		}
	}	
	
	/**
	 * 
	 * @param relationDefinition
	 * @param relationAttr
	 * @param schema
	 * @throws CCMDBException
	 */
	private void addRelationRulesDefinitions(CIRelationDefinition relationDefinition, 
			Attribute relationAttr,
			Entry schema) throws CCMDBException {

		List<RelationRuleDefinition> relationRules = relationDefinition.getRelationRules();
		if (relationRules == null) {
			return;
		}
		
		Set<String> sourceItemClassificatios = new HashSet<String>();
		Set<String> targetItemClassificatios = new HashSet<String>();
		
		for (RelationRuleDefinition ruleDef : relationRules) {
			sourceItemClassificatios.add(ruleDef.getSourceClassification());
			targetItemClassificatios.add(ruleDef.getTargetClassification());
		}
		
		if (sourceItemClassificatios.size() > 0) {
			for (String actciClassification : sourceItemClassificatios) {
				ActualCIDefinition actciDef = 
					metaData.getActualCIDefinition(actciClassification);
				if (actciDef.isVisible()) {
					Attribute srcAttr = createRSTAttribute(schema, 	
							CDMConstants.CDM_SOURCE_CI_PREFIX, 
							actciDef);
					relationAttr.appendChild(srcAttr);
				}
			}
		}
		
		if (targetItemClassificatios.size() > 0) {
			for (String actciClassification : targetItemClassificatios) {
				ActualCIDefinition actciDef = 
					metaData.getActualCIDefinition(actciClassification);
				if (actciDef.isVisible()) {
					Attribute trgAttr = createRSTAttribute(schema, 	
							CDMConstants.CDM_TARGET_CI_PREFIX, 
							actciDef);
					relationAttr.appendChild(trgAttr);
				}
			}
		}		
	}		
	
	/**
	 * 
	 * @param schema
	 * @return Attribute
	 */
	private static Attribute createMSSAttribute(Entry schema) {
		Attribute mssAttr = new Attribute(ATTR_MSS);
		String prefix = CDMConstants.CDM_PREFIX;
		Attribute mssAttrValue = new Attribute(prefix + MSS_CLASS);
		mssAttr.appendChild(mssAttrValue);
		
		Attribute productVersion = new Attribute(prefix + MSS_PRODUCT_VERSION);
		productVersion.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(productVersion);
		
		Attribute guid = new Attribute(prefix + MSS_GUID);
		guid.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(guid);		
		
		Attribute lmt = new Attribute(prefix + MSS_LMT);
		lmt.setValue(Integer.class.getCanonicalName());		
		mssAttrValue.appendChild(lmt);

		Attribute displayName = new Attribute(prefix + MSS_DISPLAY_NAME);
		displayName.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(displayName);
		
		Attribute lmb = new Attribute(prefix + MSS_LMBY);
		lmb.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(lmb);
		
		Attribute mssName = new Attribute(prefix + MSS_NAME);
		mssName.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(mssName);		
		
		Attribute productName = new Attribute(prefix + MSS_PRODUCT_NAME);
		productName.setValue(String.class.getCanonicalName());		
		mssAttrValue.appendChild(productName);	

		return mssAttr;
	}				

	/**
	 * 
	 * @param schema
	 * @param prefix
	 * @param definition
	 * @return Attribute
	 */
	private static Attribute createRSTAttribute(Entry schema, 
			String prefix, 
			ActualCIDefinition definition)  {		
		
		String attrName = prefix + definition.getDisplayName();
		Attribute targetAttr = new Attribute(attrName);

		Attribute classTypeAttr = new Attribute(ATTR_CLASS_TYPE_NAME);
		classTypeAttr.setValue(String.class.getCanonicalName());		
		targetAttr.appendChild(classTypeAttr);
		
		Attribute guidAttr = new Attribute(ATTR_GUID_NAME);
		guidAttr.setValue(String.class.getCanonicalName());		
		targetAttr.appendChild(guidAttr);
		
		EntryUtilities.addPropertiesToSchemaAttribute(targetAttr, definition, schema);
		EntryUtilities.addAttributesToSchemaAttribute(targetAttr, definition.getAttributes(), schema);
		
		return targetAttr;
	}	
	
}
