/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.cdm;

import java.util.List;
import java.util.Set;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.ModelObject;
import com.ibm.di.connector.ccmdb.model.OMPRelation;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 *  This class is responsible for creating of DI Entries from data model objects.
 * 
 * @author yavor.gologanov
 *
 */
public class CDMEntryFactory {
	
	private CDMMetaData metaData = null;
	
	/**
	 * 
	 * @param metaData
	 */
	public CDMEntryFactory(CDMMetaData metaData) {
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
		if ((definition == null) || (!definition.isVisible())) {
			definition = metaData.getActualCIDefinition();
		}
		
		entry.setAttribute(CDMSchemaFactory.ATTR_CLASS_TYPE_NAME, definition.getDisplayName());
		entry.setAttribute(CDMSchemaFactory.ATTR_GUID_NAME, configItem.getGuid());
		entry.setAttribute(CDMSchemaFactory.ATTR_CLASSIFICATION, definition.getClassification());
		
		EntryUtilities.addPropertiesToEntry(configItem, definition, entry);
		EntryUtilities.addAttributesToEntry(configItem, definition, entry);		
		addRelations(configItem, entry);	
		
		OMPRelation ompRelation = configItem.getOmpRelation();
		if (ompRelation != null) {
			Attribute mssAttr = createMSSData(entry, configItem.getOmpRelation());
			entry.appendChild(mssAttr);
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
		String attrName = CDMConstants.CDM_RELATIONSHIP_PREFIX + definition.getDisplayName();
		Attribute relationAttr = new Attribute(attrName);
		addRelatedActualCI(relation, relationAttr, entry);		
		entry.appendChild(relationAttr);
		
		return entry;
	}	
	

	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param configItem
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addRelations(ActualCI configItem, 
			Entry entry) throws CCMDBException {
		
		Set<String> relationNames = configItem.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		for (String relationName : relationNames) {
			CIRelationDefinition relationDefinition = metaData.getCIRelationDefinition(relationName);
			String attrName = CDMConstants.CDM_RELATIONSHIP_PREFIX + relationDefinition.getDisplayName();
			Attribute relationAttr = new Attribute(attrName);	
			
			List<CIRelation> sourceRelations = configItem.getSourceRelations(relationName);
			if (sourceRelations != null) {
				for (CIRelation nextRelation : sourceRelations) {
					ActualCI target = nextRelation.getTarget();
					Attribute targetAttr = createRSTAttribute(entry, 
							CDMConstants.CDM_TARGET_CI_PREFIX, 
							target);
					relationAttr.appendChild(targetAttr);
				}
			}
			
			List<CIRelation> targetRelations = configItem.getTargetRelations(relationName);
			if (targetRelations != null) {
				for (CIRelation nextRelation : targetRelations) {
					ActualCI source = nextRelation.getSource();
					Attribute sourceAttr = createRSTAttribute(entry, 
							CDMConstants.CDM_SOURCE_CI_PREFIX, 
							source);
					relationAttr.appendChild(sourceAttr);
				}
			}			
			
			entry.appendChild(relationAttr);
		}		
	}		
	
	/**
	 * 
	 * @param relation
	 * @param relationAttr
	 * @param entry
	 * @throws CCMDBException
	 */
	private void addRelatedActualCI(CIRelation relation, 
			Attribute relationAttr,
			Entry entry) throws CCMDBException {
		
		ActualCI source = relation.getSource();
		ActualCI target = relation.getTarget();
		
		if ((source == null) || (target == null)) {
			return;
		}
		
		Attribute sourcesAttr = createRSTAttribute(entry, 
				CDMConstants.CDM_SOURCE_CI_PREFIX, 
				source);
		relationAttr.appendChild(sourcesAttr);

		Attribute targetsAttr = createRSTAttribute(entry, 
				CDMConstants.CDM_TARGET_CI_PREFIX, 
				target);
		relationAttr.appendChild(targetsAttr);
	}	
	
	/**
	 * 
	 * @param entry
	 * @param ompRel
	 * @return Attribute
	 */
	private Attribute createMSSData(Entry entry, OMPRelation ompRel) {
		
		ModelObject omp = ompRel.getOmp();
		
		Attribute mssAttr = new Attribute(CDMSchemaFactory.ATTR_MSS);
		String prefix = CDMConstants.CDM_PREFIX;
		Attribute mssAttrValue = new Attribute(prefix + CDMSchemaFactory.MSS_CLASS);
		mssAttr.appendChild(mssAttrValue);
		
		Attribute productVersion = new Attribute(prefix + CDMSchemaFactory.MSS_PRODUCT_VERSION);
		productVersion.setValue(omp.getProperty(CCMDBActualCISchema.OMP_VERSION));		
		mssAttrValue.appendChild(productVersion);
		
		Attribute guid = new Attribute(prefix + CDMSchemaFactory.MSS_GUID);
		guid.setValue(omp.getProperty(CCMDBActualCISchema.OMP_OMPGUID));		
		mssAttrValue.appendChild(guid);		
		
		Attribute lmt = new Attribute(prefix + CDMSchemaFactory.MSS_LMT);
		lmt.setValue(omp.getProperty(CCMDBActualCISchema.OMP_CHANGEDATE));		
		mssAttrValue.appendChild(lmt);

		Attribute displayName = new Attribute(prefix + CDMSchemaFactory.MSS_DISPLAY_NAME);
		displayName.setValue(omp.getProperty(CCMDBActualCISchema.OMP_DISPLAYLABEL));		
		mssAttrValue.appendChild(displayName);
		
		Attribute lmb = new Attribute(prefix + CDMSchemaFactory.MSS_LMBY);
		lmb.setValue(omp.getProperty(CCMDBActualCISchema.OMP_CHANGEBY));		
		mssAttrValue.appendChild(lmb);
		
		Attribute mssName = new Attribute(prefix + CDMSchemaFactory.MSS_NAME);
		mssName.setValue(omp.getProperty(CCMDBActualCISchema.OMP_NAME));		
		mssAttrValue.appendChild(mssName);		
		
		Attribute productName = new Attribute(prefix + CDMSchemaFactory.MSS_PRODUCT_NAME);
		productName.setValue(omp.getProperty(CCMDBActualCISchema.OMP_PRODUCTNAME));		
		mssAttrValue.appendChild(productName);	

		return mssAttr;
	}		

	
	/**
	 * 
	 * @param entry
	 * @param prefix
	 * @param configItem
	 * @return
	 * @throws CCMDBException
	 */
	private Attribute createRSTAttribute(Entry entry, 
			String prefix, 
			ActualCI configItem) throws CCMDBException {	
		
		ActualCIDefinition actciDef = metaData.getActualCIDefinition(configItem.getClassName());
		
		String attrName = prefix + actciDef.getDisplayName();
		Attribute attr = new Attribute(attrName);

		Attribute classTypeAttr = new Attribute(CDMSchemaFactory.ATTR_CLASS_TYPE_NAME);
		classTypeAttr.setValue(actciDef.getDisplayName());		
		attr.appendChild(classTypeAttr);
		
		Attribute guidAttr = new Attribute(CDMSchemaFactory.ATTR_GUID_NAME);
		guidAttr.setValue(configItem.getGuid());		
		attr.appendChild(guidAttr);

		Attribute classificationAttr = new Attribute(CDMSchemaFactory.ATTR_CLASSIFICATION);
		classificationAttr.setValue(actciDef.getClassification());		
		attr.appendChild(classificationAttr);
		
		EntryUtilities.addPropertiesToEntryAttribute(attr, configItem, actciDef, entry);
		EntryUtilities.addAttributesToEntryAttribute(attr, configItem, actciDef, entry);
		
		return attr;
	}		
	
}
