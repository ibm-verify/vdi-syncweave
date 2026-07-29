/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.base;

import java.util.List;
import java.util.Set;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.ModelObject;
import com.ibm.di.connector.ccmdb.model.OMPRelation;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.ModelObjectDefinition;
import com.ibm.di.connector.ccmdb.model.def.OMPRelationDefinition;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This class is responsible for creating of DI Entries from data model objects.
 * 
 * @author yavor.gologanov
 *
 */
public class CCMDBEntryFactory {

	private CCMDBMetaData metaData = null;
	
	/**
	 * 
	 * @param metaData
	 */
	public CCMDBEntryFactory(CCMDBMetaData metaData) {
		this.metaData = metaData;
	}	
	
	/**
	 * Creates a DI Entry based on a given configuration item.
	 * 
	 * @param configItem
	 * @return Entry
	 * @throws CCMDBException
	 */
	public Entry createEntry(ActualCI configItem) 
		throws CCMDBException {

		Entry entry = new Entry(true);
		
		ActualCIDefinition definition = metaData.getActualCIDefinition(configItem.getClassName());
		
		entry.setAttribute(CCMDBSchemaFactory.ATTR_CLASSIFICATION, definition.getClassName());
		
		EntryUtilities.addPropertiesToEntry(configItem, definition, entry);
		EntryUtilities.addAttributesToEntry(configItem, definition, entry);
		
		addRelations(configItem, entry);
		
		if (configItem.getDeletedActualCI() != null) {
			addDeletedCI(configItem, definition, entry);
		}
		
		if (configItem.getOmpRelation() != null) {
			addOMPRelation(configItem, definition, entry);
		}
		
		return entry;	

	}	
	
	/**
	 * Creates a DI Entry based on a given relation.
	 * 
	 * @param relation
	 * @return Entry
	 * @throws CCMDBException
	 */
	public Entry createEntry(CIRelation relation) 
		throws CCMDBException {

		Entry entry = new Entry(true);	
		
		CIRelationDefinition definition = metaData.getCIRelationDefinition(relation.getClassName());
				
		EntryUtilities.addPropertiesToEntry(relation, definition, entry);
		
		addRelatedActualCI(relation, entry);	
		
		return entry;
	}	
		
	//-------------------------------------------------------------------------
			
	/**
	 * 
	 * @param configItem
	 * @param attr
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addActualCI(ActualCI configItem, 
			Attribute attr, 
			Entry entry) throws CCMDBException {
		
		ActualCIDefinition definition = metaData.getActualCIDefinition(configItem.getClassName());
		EntryUtilities.addPropertiesToEntryAttribute(attr, configItem, definition, entry);
		EntryUtilities.addAttributesToEntryAttribute(attr, configItem, definition, entry);
	}	
	
	/**
	 * 
	 * @param configItem
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addRelations(ActualCI configItem, Entry entry) throws CCMDBException {

		Set<String> relationNames = configItem.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		for (String relationName : relationNames) {
			CIRelationDefinition relationDefinition = metaData.getCIRelationDefinition(relationName);
			String displayName = relationDefinition.getDisplayName();
			Attribute relationAttr = new Attribute(displayName);	
			
			List<CIRelation> sourceRelations = configItem.getSourceRelations(relationName);
			if (sourceRelations != null) {
				for (CIRelation nextRelation : sourceRelations) {
					Attribute relInstanceAttr = new Attribute(CCMDBSchemaFactory.ATTR_REL_INSTANCE);
					EntryUtilities.addPropertiesToEntryAttribute(relInstanceAttr, 
							nextRelation, relationDefinition, entry);
					addRelatedConfigItem(relInstanceAttr, nextRelation.getTarget(), 
							CCMDBSchemaFactory.ATTR_REL_TARGET,	entry);
					relationAttr.appendChild(relInstanceAttr);
				}
			}
			
			List<CIRelation> targetRelations = configItem.getTargetRelations(relationName);
			if (targetRelations != null) {
				for (CIRelation nextRelation : targetRelations) {
					Attribute relInstanceAttr = new Attribute(CCMDBSchemaFactory.ATTR_REL_INSTANCE);
					EntryUtilities.addPropertiesToEntryAttribute(relInstanceAttr, 
							nextRelation, relationDefinition, entry);
					addRelatedConfigItem(relInstanceAttr, nextRelation.getSource(), 
							CCMDBSchemaFactory.ATTR_REL_SOURCE,	entry);					
					relationAttr.appendChild(relInstanceAttr);
				}
			}			
			
			entry.appendChild(relationAttr);
		}
	}		
	
	/**
	 * 
	 * @param relation
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addRelatedActualCI(CIRelation relation, 
			Entry entry) throws CCMDBException {
		
		ActualCI source = relation.getSource();
		ActualCI target = relation.getTarget();
		
		if ((source == null) || (target == null)) {
			return;
		}
		
		Attribute sourcesAttr = new Attribute(CCMDBSchemaFactory.ATTR_REL_SOURCE);
		addActualCI(source, sourcesAttr, entry);
		entry.appendChild(sourcesAttr);

		Attribute targetsAttr = new Attribute(CCMDBSchemaFactory.ATTR_REL_TARGET);
		addActualCI(target, targetsAttr, entry);
		entry.appendChild(targetsAttr);
	}		
		
	/**
	 * 
	 * @param configItem
	 * @param definition
	 * @param entry
	 */
	private void addDeletedCI(ActualCI configItem, 
			ActualCIDefinition definition, 
			Entry entry) {
		
		ModelObject actcidel = configItem.getDeletedActualCI();	
		ModelObjectDefinition actcidelDef = definition.getDeletedActualCI();
	
		Attribute attribute = new Attribute(CCMDBSchemaFactory.ATTR_DELETED_CI);
		EntryUtilities.addPropertiesToEntryAttribute(attribute, actcidel, actcidelDef, entry);
		entry.appendChild(attribute);
	}	
	
	/**
	 * 
	 * @param configItem
	 * @param definition
	 * @param entry
	 */
	private void addOMPRelation(ActualCI configItem, 
			ActualCIDefinition definition, 
			Entry entry) {
		
		OMPRelation ompRelation = configItem.getOmpRelation();		
		ModelObject omp = ompRelation.getOmp();
		OMPRelationDefinition omprelDef = definition.getOmpRelation();
		ModelObjectDefinition ompDef = omprelDef.getOmp();
		
		Attribute ompRelAttr = new Attribute(CCMDBSchemaFactory.ATTR_OMP_RELATIONSHIP);				
		EntryUtilities.addPropertiesToEntryAttribute(ompRelAttr, ompRelation, omprelDef, entry);		
		
		Attribute ompAttr = new Attribute(CCMDBSchemaFactory.ATTR_OMP);
		EntryUtilities.addPropertiesToEntryAttribute(ompAttr, omp, ompDef, entry);
		
		ompRelAttr.appendChild(ompAttr);		
		entry.appendChild(ompRelAttr);	
	}			
	
	/**
	 * 
	 * @param attribute
	 * @param configItem
	 * @param attrName
	 * @param entry
	 */
	private void addRelatedConfigItem(Attribute relAttr, 
			ActualCI configItem, 
			String attrName,
			Entry entry) throws CCMDBException {
		
		Attribute ciAttr = new Attribute(attrName);
		ActualCIDefinition definition = metaData.getActualCIDefinition(configItem.getClassName());
		EntryUtilities.addPropertiesToEntryAttribute(ciAttr, configItem, definition, entry);
		EntryUtilities.addAttributesToEntryAttribute(ciAttr, configItem, definition, entry);
		relAttr.appendChild(ciAttr);
	}
	
}
