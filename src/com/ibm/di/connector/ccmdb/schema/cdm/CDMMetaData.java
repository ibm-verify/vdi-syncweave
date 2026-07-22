/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.cdm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.MetaDataFactory;
import com.ibm.di.connector.ccmdb.AbstractMetaData;
import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.ExecutionContext;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCIProvider;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.connector.ccmdb.provider.ClassificationProvider;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;

/**
 * An implementation of AbstractMetaData, designed to support IdML mode.
 * 
 * @author yavor.gologanov
 *
 */
public class CDMMetaData extends AbstractMetaData {

	public static final String ATTR_NAME = "name";
	public static final String ATTR_SYNTAX = "syntax";	
	public static final String ATTR_SIZE = "size";
	public static final String ATTR_TYPE = "type";
	
	/**
	 * 
	 * @param classification
	 * @return
	 * @throws Exception
	 */
	public static String getCDMType(String classification) 
		throws Exception {

		MetaData metaData = MetaDataFactory.getJarMetaData();
		
		if (classification.startsWith("RELATION.")) {
			String searchName = classification.substring(classification.lastIndexOf(".") + 1).toLowerCase();
			searchName = CDMConstants.CDM_PREFIX + searchName;
			Set<String> cdmRelations = metaData.getTypes(IdMLConstants.ARTIFACT_RELATIONSHIP).keySet();
						
			Iterator<String> relationsIt = cdmRelations.iterator();
			while (relationsIt.hasNext()) {
				String nextRelationName = relationsIt.next();
				if (nextRelationName.equalsIgnoreCase(searchName)) {
					String name = nextRelationName.substring(nextRelationName.indexOf(":") + 1);
					return name;
				}
			}		
		} else {
			
			String searchName = CDMConstants.CDM_PREFIX + classification.toLowerCase();
			Map<String, Object> cdmTypes = metaData.getTypes(IdMLConstants.ARTIFACT_CI);			
			Iterator<String> cdmTypesIt = cdmTypes.keySet().iterator();
			while (cdmTypesIt.hasNext()) {
				String nextTypeName = cdmTypesIt.next();
				if (nextTypeName.equalsIgnoreCase(searchName)) {
					String name = nextTypeName.substring(nextTypeName.indexOf(":") + 1);
					return name;
				}
			}
			return "core.ManagedElement";
		}
		
		return null;
	}	
	
	
	/**
	 * 
	 * @param cdmType
	 * @return
	 * @throws Exception
	 */
	public static String getClassification(String cdmType) {
		if (cdmType == null) {
			return null;
		}
		return cdmType.toUpperCase();
	}	
		
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	private MetaData cdmMetaData = null;
	private Set<String> cdmRelations = null;
	
	private CDMEntryFactory entryFactory = null;
	private CDMObjectFactory objectFactory = null;
	private CDMSchemaFactory schemaFactory = null;
		
	private Map<String, Classification> classMapping = new HashMap<String, Classification>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 */
	public void init(ExecutionContext ctx) throws CCMDBException {
		super.init(ctx);
		
		try {
			cdmMetaData = MetaDataFactory.getJarMetaData();
			entryFactory = new CDMEntryFactory(this);
			objectFactory = new CDMObjectFactory(this);
			schemaFactory = new CDMSchemaFactory(this);
		} catch (Exception e) {
			throw new CCMDBException(e);
		}
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
		initClassMapping();
		return objectFactory.createConfigItem(entry);
	}

	/**
	 * 
	 */
	public CIRelation createCIRelation(Entry entry) throws CCMDBException {
		initClassMapping();
		return objectFactory.createRelation(entry);
	}		
	
	/**
	 * 
	 */
	public void applyMapping(ActualCIDefinition definition) 
		throws CCMDBException {
		try {
			String cdmClass = null;
			Classification classification = definition.getClassification();
			boolean isRootClass = classification.getClassName().equals(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
			
			if (isRootClass) {
				cdmClass = "core.ManagedElement";		
			} else {
				cdmClass = getCDMClass(classification.getClassName());	
				
			}
			
			if (cdmClass == null) {
				definition.setVisible(false);
				return;
			} 
			
			definition.setDisplayName(cdmClass);
			
			Map<String, String> cdmAttributes = null;
			if (isRootClass) {
				cdmAttributes = getCDMAttributeMap(IdMLConstants.ARTIFACT_CI, "sys.ComputerSystem");
			} else {
				cdmAttributes = getCDMAttributeMap(IdMLConstants.ARTIFACT_CI, cdmClass);
			}			
			
			String prefix = CDMConstants.CDM_PREFIX;
			prefix = prefix.substring(0, prefix.indexOf(":"));
			
			// add properties mapping
			Collection<PropertyDefinition> properties = definition.getProperties();
			if (properties != null) {
				for (PropertyDefinition propDef : properties) {
					String name = propDef.getName();
					if (name.equalsIgnoreCase(CCMDBActualCISchema.ACTCI_DESCRIPTION)) {
						propDef.setVisible(false);
						propDef.setDisplayName("p" + propDef.getName());
						continue;
					}
					
					String searchName = name.toLowerCase();
					if (cdmAttributes.containsKey(searchName)) {
						String cdmAttrName = cdmAttributes.get(searchName);
						String displayName = cdmAttrName.substring(cdmAttrName.indexOf(":") + 1);
						propDef.setDisplayName(displayName);
						propDef.setDisplayPrefix(prefix);
					} else {
						propDef.setVisible(false);
					}
				} 
			}	

			// add attributes mapping
			Collection<AttributeDefinition> attributes = definition.getAttributes();
			if (attributes != null) {
				for (AttributeDefinition attrDef : attributes) {
					String name = attrDef.getName();
					String searchName = name.substring(name.lastIndexOf("_") + 1).toLowerCase();
					if (cdmAttributes.containsKey(searchName)) {
						String cdmAttrName = cdmAttributes.get(searchName);
						String displayName = cdmAttrName.substring(cdmAttrName.indexOf(":") + 1);
						attrDef.setDisplayName(displayName);						
						attrDef.setDisplayPrefix(prefix);
					}  else {
						attrDef.setVisible(false);
					}
				} 
			}	

		} catch (Exception e) {
			throw new CCMDBException(e);
		}
	}	
	
	/**
	 * 
	 */
	public void applyMapping(CIRelationDefinition definition) throws CCMDBException {
		try {
			if (cdmRelations == null) {
				cdmRelations = cdmMetaData.getTypes(IdMLConstants.ARTIFACT_RELATIONSHIP).keySet();
			}
			
			boolean isRootClass = definition.getClassName().equals(CCMDBActualCIProvider.CLASS_CI_RELATION);
			
			if (isRootClass) {
				definition.setDisplayName("relation");
			} else {
				String name = definition.getClassification().getClassName();
				String searchName = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
				String cdmName = getCDMRelationName(searchName, cdmRelations);
				if (cdmName == null) {
					definition.setVisible(false);
					return;
				}
				definition.setDisplayName(cdmName);
			}
			
			Collection<PropertyDefinition> properties = definition.getProperties();
			if (properties != null) {
				for (PropertyDefinition propDef : properties) {
					propDef.setVisible(false);
				}
			}
		} catch (Exception e) {
			throw new CCMDBException(e);
		}		
	}		
	
	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param cdmName
	 * @return Classification
	 * @throws CCMDBException
	 */
	protected Classification getClassificationByCDMName(String cdmName) throws CCMDBException {
		return classMapping.get(cdmName);
	}	
	
	/**
	 * 
	 * @throws CCMDBException
	 */
	private void initClassMapping() throws CCMDBException {
		if (!classMapping.isEmpty()) {
			return;
		}
		
		ClassificationProvider classificationProvider = ctx.getDataProvider().getClassificationProvider();
		classMapping = new HashMap<String, Classification>();
		try {
			List<Classification> classifications = 
				classificationProvider.getClassifications(CCMDBActualCIProvider.CLASS_ACTUAL_CI);
			
			for (Classification classification : classifications) {
				String cdmClass = getCDMClass(classification.getClassName());
				if (cdmClass != null) {
					classMapping.put(cdmClass, classification);
				}
			}
			
			classifications = 
				classificationProvider.getClassifications(CCMDBActualCIProvider.CLASS_CI_RELATION);
			
			for (Classification classification : classifications) {
				if (cdmRelations == null) {
					cdmRelations = cdmMetaData.getTypes(IdMLConstants.ARTIFACT_RELATIONSHIP).keySet();
				}
				
				String name = classification.getClassName();
				String searchName = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
				String cdmName = getCDMRelationName(searchName, cdmRelations);
				if (cdmName != null) {
					classMapping.put(cdmName, classification);
				}
			}
		} catch (Exception e) {
			throw new CCMDBException(e);
		}
	}
	
	/**
	 * 
	 * @param artifactType
	 * @param className
	 * @return Map<String, String>
	 */
	private Map<String, String> getCDMAttributeMap(String artifactType, String className) 
		throws Exception {
		Vector<Entry> cdmAttributes = cdmMetaData.getAttributes(IdMLConstants.ARTIFACT_CI, className);
		Map<String, String> result = new HashMap<String, String>();
		Iterator<Entry> cdmAttributesIt = cdmAttributes.iterator();
		while (cdmAttributesIt.hasNext()) {
			Entry nextAttrEntry = cdmAttributesIt.next();
			String nextAttrName = (String) nextAttrEntry.getAttribute("name").getValue();
			String key = nextAttrName.substring(CDMConstants.CDM_PREFIX.length());
			result.put(key.toLowerCase(), nextAttrName);
		}
		return result;
	}
	
	/**
	 * 
	 * @param classType
	 * @return String
	 * @throws Exception
	 */
	private String getCDMClass(String classType) 
		throws Exception {
		
		if (classType == null) {
			return null;
		}
		
		String searchName = CDMConstants.CDM_PREFIX + classType.toLowerCase();
		Map<String, Object> cdmTypes = cdmMetaData.getTypes(IdMLConstants.ARTIFACT_CI);
		
		Iterator<String> cdmTypesIt = cdmTypes.keySet().iterator();
		while (cdmTypesIt.hasNext()) {
			String nextTypeName = cdmTypesIt.next();
			if (nextTypeName.equalsIgnoreCase(searchName)) {
				String name = nextTypeName.substring(nextTypeName.indexOf(":") + 1);
				return name;
			}
		}

		return null;
	}		
	
	/**
	 * 
	 * @param relName
	 * @param cdmRelations
	 * @return String
	 * @throws Exception
	 */
	private String getCDMRelationName(String relName, Set<String> cdmRelations) 
		throws Exception {
		
		String searchName = CDMConstants.CDM_PREFIX + relName;
		
		Iterator<String> relationsIt = cdmRelations.iterator();
		while (relationsIt.hasNext()) {
			String nextRelationName = relationsIt.next();
			if (nextRelationName.equalsIgnoreCase(searchName)) {
				String name = nextRelationName.substring(nextRelationName.indexOf(":") + 1);
				return name;
			}
		}

		return null;
	}		
	
}
