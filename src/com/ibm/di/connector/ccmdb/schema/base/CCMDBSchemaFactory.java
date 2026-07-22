/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.base;

import java.util.Collection;
import java.util.List;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.ModelObjectDefinition;
import com.ibm.di.connector.ccmdb.model.def.OMPRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.model.def.RelationRuleDefinition;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This class is responsible for creating of native schema entries 
 * from data model definition objects.
 * 
 * @author yavor.gologanov
 *
 */
public class CCMDBSchemaFactory {

	public static final String ATTR_OMP_RELATIONSHIP = "OMPRelationship";
	public static final String ATTR_OMP = "OMP";
	public static final String ATTR_DELETED_CI = "deletedActualCI";
	public static final String ATTR_REL_INSTANCE = "relation";
	public static final String ATTR_REL_SOURCE = "source";
	public static final String ATTR_REL_TARGET = "target";	
	public static final String ATTR_CLASSIFICATION = "classification";
	
	private CCMDBMetaData metaData = null;
	
	/**
	 * 
	 * @param metaData
	 */
	public CCMDBSchemaFactory(CCMDBMetaData metaData) {
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
		
		schema.setAttribute(ATTR_CLASSIFICATION, String.class.getCanonicalName());
		
		EntryUtilities.addPropertiesToSchema(definition, schema);
		EntryUtilities.addAttributesToSchema(definition.getAttributes(), schema);		
		
		addRelationDefinitions(definition, schema);	
		if (definition.getDeletedActualCI() != null) {
			addDeletedCIDefinition(definition, schema);		
		}
		
		if (definition.getOmpRelation() != null) {
			addOMPRelationDefinition(definition, schema);		
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
		
		EntryUtilities.addPropertiesToSchema(definition, schema);
		addRelatedActualCIDefinitions(schema);	
		
		return schema;
	}	
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param definition
	 * @param schema
	 * @throws CCMDBException
	 */
	private void addRelationDefinitions(ActualCIDefinition definition, 
			Entry schema) throws CCMDBException {
		
		Collection<String> relationNames = definition.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		for (String relationName : relationNames) {
			CIRelationDefinition relationDefinition = metaData.getCIRelationDefinition(relationName);
			Collection<PropertyDefinition> properties = relationDefinition.getProperties();
			if (properties == null) {
				continue;
			}
			
			List<RelationRuleDefinition> srcRules = definition.getSourceRelationRules(relationName);
			List<RelationRuleDefinition> trgRules = definition.getTargetRelationRules(relationName);
			
			String displayName = relationDefinition.getDisplayName();
			Attribute relationAttr = new Attribute(displayName);				
			
			if ((srcRules != null) && (srcRules.size() > 0)) {
				Attribute relationInstanceAttr = new Attribute(ATTR_REL_INSTANCE);
				EntryUtilities.addPropertiesToSchemaAttribute(relationInstanceAttr, relationDefinition, schema);
				addRelatedActualCIDefinition(relationInstanceAttr, ATTR_REL_TARGET, schema);
				relationAttr.appendChild(relationInstanceAttr);
			}
			
			if ((trgRules != null) && (trgRules.size() > 0)) {
				Attribute relationInstanceAttr = new Attribute(ATTR_REL_INSTANCE);
				EntryUtilities.addPropertiesToSchemaAttribute(relationInstanceAttr, relationDefinition, schema);
				addRelatedActualCIDefinition(relationInstanceAttr, ATTR_REL_SOURCE, schema);
				relationAttr.appendChild(relationInstanceAttr);
			}
			
			schema.appendChild(relationAttr);
		}
	}	
	
	/**
	 * 
	 * @param definition
	 * @param schema
	 */
	private void addDeletedCIDefinition(ActualCIDefinition definition, Entry schema) {		
		Attribute attribute = new Attribute(ATTR_DELETED_CI);
		ModelObjectDefinition actciDelDefinition = definition.getDeletedActualCI();
		EntryUtilities.addPropertiesToSchemaAttribute(attribute, actciDelDefinition, schema);
		schema.appendChild(attribute);
	}	
	
	/**
	 * 
	 * @param definition
	 * @param schema
	 */
	private void addOMPRelationDefinition(ActualCIDefinition definition, Entry schema) {		
		Attribute attribute = new Attribute(ATTR_OMP_RELATIONSHIP);	
		OMPRelationDefinition ompRelDefinition = definition.getOmpRelation();
		EntryUtilities.addPropertiesToSchemaAttribute(attribute, ompRelDefinition, schema);
		
		Attribute ompAttr = new Attribute(ATTR_OMP);
		ModelObjectDefinition ompDefinition = ompRelDefinition.getOmp();
		EntryUtilities.addPropertiesToSchemaAttribute(ompAttr, ompDefinition, schema);
		
		attribute.appendChild(ompAttr);		
		schema.appendChild(attribute);
	}

	/**
	 * 
	 * @param schema
	 * @throws CCMDBException
	 */
	private void addRelatedActualCIDefinitions(Entry schema) 
		throws CCMDBException {
		
		ActualCIDefinition definition = metaData.getActualCIDefinition();
		
		Attribute sourcesAttr = new Attribute(ATTR_REL_SOURCE);
		EntryUtilities.addPropertiesToSchemaAttribute(sourcesAttr, definition, schema);
		schema.appendChild(sourcesAttr);

		Attribute targetsAttr = new Attribute(ATTR_REL_TARGET);
		EntryUtilities.addPropertiesToSchemaAttribute(targetsAttr, definition, schema);
		schema.appendChild(targetsAttr);
	}		
	
	/**
	 * 
	 * @param relation
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addRelatedActualCIDefinition(Attribute relationInstanceAttr, 
			String attrName,
			Entry schema) throws CCMDBException {
		
		ActualCIDefinition definition = metaData.getActualCIDefinition();
		
		Attribute ciAttr = new Attribute(attrName);
		EntryUtilities.addPropertiesToSchemaAttribute(ciAttr, definition, schema);
		relationInstanceAttr.appendChild(ciAttr);
	}		
	
}
