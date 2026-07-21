/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.base;

import java.util.Collection;

import com.ibm.di.connector.ccmdb.AbstractMetaData;
import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.ExecutionContext;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.model.def.ModelObjectDefinition;
import com.ibm.di.connector.ccmdb.model.def.OMPRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.connector.ccmdb.provider.ClassificationProvider;
import com.ibm.di.entry.Entry;

/**
 * An implementation of AbstractMetaData, that is designed to support native mode.
 *
 * @author yavor.gologanov
 *
 */
public class CCMDBMetaData extends AbstractMetaData {

	private CCMDBEntryFactory entryFactory = null;
	private CCMDBObjectFactory objectFactory = null;
	private CCMDBSchemaFactory schemaFactory = null;

	/**
	 *
	 */
	public void init(ExecutionContext ctx) throws CCMDBException {
		super.init(ctx);

		entryFactory = new CCMDBEntryFactory(this);
		objectFactory = new CCMDBObjectFactory(this);
		schemaFactory = new CCMDBSchemaFactory(this);
	}

	/**
	 *
	 */
	public Entry createSchema(ActualCIDefinition definition)
		throws CCMDBException {
		return schemaFactory.createSchema(definition);
	}

	/**
	 *
	 */
	public Entry createSchema(CIRelationDefinition definition)
		throws CCMDBException {
		return schemaFactory.createSchema(definition);
	}

	/**
	 *
	 */
	public Entry createEntry(ActualCI configItem) throws CCMDBException {
		return entryFactory.createEntry(configItem);
	}

	/**
	 *
	 */
	public Entry createEntry(CIRelation relation) throws CCMDBException {
		return entryFactory.createEntry(relation);
	}

	/**
	 *
	 */
	public ActualCI createActualCI(Entry entry) throws CCMDBException {
		return objectFactory.createConfigItem(entry);
	}

	/**
	 *
	 */
	public CIRelation createCIRelation(Entry entry) throws CCMDBException {
		return objectFactory.createRelation(entry);
	}

	/**
	 *
	 */
	public void applyMapping(ActualCIDefinition definition) {
		Classification classification = definition.getClassification();
		String className = classification.getClassName();
		definition.setDisplayName(className);

		applyDefaultMapping(definition);

		Collection<AttributeDefinition> attributes = definition.getAttributes();
		if (attributes != null) {
			for (AttributeDefinition attrDef : attributes) {
				String attrName = attrDef.getName();
				String displayName = attrName.substring(attrName.lastIndexOf("_") + 1);
				displayName = displayName.toLowerCase();
				attrDef.setDisplayName(displayName);
			}
		}

		if (classification.getClassName().equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI)) {
			return;
		}

		ModelObjectDefinition actciDelDef = definition.getDeletedActualCI();
		applyDefaultMapping(actciDelDef);

		OMPRelationDefinition ompRelDef = definition.getOmpRelation();
		applyDefaultMapping(ompRelDef);

		ModelObjectDefinition ompDef = ompRelDef.getOmp();
		applyDefaultMapping(ompDef);
	}

	/**
	 *
	 */
	public void applyMapping(CIRelationDefinition definition) {
		Classification classification = definition.getClassification();
		String className = classification.getClassName();
		String displayName = className.substring(className.lastIndexOf(".") + 1);
		displayName = displayName.toLowerCase();
		definition.setDisplayName(displayName);

		Collection<PropertyDefinition> properties = definition.getProperties();
		if (properties != null) {
			for (PropertyDefinition propDef : properties) {
				String propName = propDef.getName();
				String propDisplayName = propName.toLowerCase();
				propDef.setDisplayName(propDisplayName);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 *
	 * @param classstructureId
	 * @return Classification
	 */
	protected Classification getClassificationByClassstructure(String classstructureId) {
		ClassificationProvider classificationProvider = ctx.getDataProvider().getClassificationProvider();
		return classificationProvider.getClasssificationByClassstructure(classstructureId);
	}

	/**
	 *
	 * @param classType
	 * @return Classification
	 */
	protected Classification getClassificationByClassType(String classification) {
		ClassificationProvider classificationProvider = ctx.getDataProvider().getClassificationProvider();
		return classificationProvider.getClasssificationByClassType(classification);
	}

	/**
	 *
	 * @param definition
	 */
	private void applyDefaultMapping(ModelObjectDefinition definition) {
		Collection<PropertyDefinition> properties = definition.getProperties();
		if (properties != null) {
			for (PropertyDefinition propDef : properties) {
				String propName = propDef.getName();

				if (propName.equalsIgnoreCase(CCMDBActualCISchema.ACTCI_DESCRIPTION)) {
					propDef.setDisplayName("p" + propName.toLowerCase());
					continue;
				}

				String displayName = propName.toLowerCase();
				propDef.setDisplayName(displayName);
			}
		}
	}

	public CCMDBObjectFactory getObjectFactory() {
		return objectFactory;
	}

}
