/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.cdm;

import java.sql.Timestamp;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.TypeNotFoundException;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.RelationSet;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.entry.Entry;

/**
 * This class is responsible for creating data model objects from DI Entries.
 * 
 * 
 * @author yavor.gologanov
 *
 */
public class CDMObjectFactory {

	private CDMMetaData metaData = null;
	
	/**
	 * 
	 * @param metaData
	 */
	public CDMObjectFactory(CDMMetaData metaData) {
		this.metaData = metaData;
	}
	
	/**
	 * Creates an instance of ActualCI based on the data from a given DI Entry.
	 * 
	 * @param entry
	 * @return ActualCI
	 * @throws CCMDBException
	 */
	public ActualCI createConfigItem(Entry entry) 
		throws CCMDBException {
	
		NodeList nodeList = entry.getChildNodes();
		if (nodeList.getLength() == 0) {
			String msg = "Inconsistent entry:" + entry;
			throw new CCMDBException(msg);
		}
		
		ActualCI configItem = createConfigItem(nodeList);
		
		return configItem;	

	}	
	
	/**
	 * Creates an instance of CIRelation based on the data from a given DI Entry.
	 * 
	 * @param entry
	 * @return CIRelation
	 * @throws CCMDBException
	 */
	public CIRelation createRelation(Entry entry) 
		throws CCMDBException {
		
		CIRelation relation = null;

		NodeList nodeList = entry.getChildNodes();
		if (nodeList.getLength() == 1) {
			relation = createRelation(nodeList.item(0));
		}

		return relation;	
	}
	
	//-------------------------------------------------------------------------	
	
	/**
	 * 
	 * @param nodeList
	 * @return ActualCI
	 * @throws CCMDBException
	 */
	private ActualCI createConfigItem(NodeList nodeList) 
		throws CCMDBException {

		ActualCI configItem = new ActualCI();
		String className = addProperties(nodeList, configItem);
		
		Classification classification = metaData.getClassificationByCDMName(className);
		if (classification == null) {
			throw new TypeNotFoundException(className);
		}
		configItem.setClassification(classification);
					
		ActualCIDefinition definition = metaData.getActualCIDefinition(configItem.getClassName(), true, true);
		EntryUtilities.addAttributesToObject(configItem, definition, nodeList);
		extractRelations(configItem, definition, nodeList);

		//if (configItem.getOmpRelation() != null) {
		//	extractActciOMPRelation(configItem, definition, nodeList);
		//}
		return configItem;	
	}		
	
	/**
	 * 
	 * @param nodeList
	 * @return CIRelation
	 * @throws CCMDBException
	 */
	private CIRelation createRelation(Node node) 
		throws CCMDBException {

		String cdmName = node.getLocalName();		
		Classification classification = metaData.getClassificationByCDMName(cdmName);
		if (classification == null) {
			throw new TypeNotFoundException(cdmName);
		}
		CIRelationDefinition definition = metaData.getCIRelationDefinition(classification.getClassName(), true);

		CIRelation relation = new CIRelation();	
		relation.setClassName(definition.getClassName());
		
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_SWAPPED, 0);
		
		NodeList nodeList = node.getChildNodes();
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			String prefix = nextNode.getPrefix() + ":";
			if (prefix.equals(CDMConstants.CDM_SOURCE_CI_PREFIX)) {
				ActualCI source = createConfigItem(nextNode.getChildNodes());
				relation.setSource(source);
			} else if (prefix.equals(CDMConstants.CDM_TARGET_CI_PREFIX)) {
				ActualCI target = createConfigItem(nextNode.getChildNodes());
				relation.setTarget(target);
			} 
		}
		return relation;	
	}	
	
	/**
	 * 
	 * @param configItem
	 * @param definition
	 * @param nodeList
	 * @throws CCMDBException
	 */
	private void extractRelations(ActualCI configItem, 
			ActualCIDefinition definition, 
			NodeList nodeList) throws CCMDBException {
	
		Set<String> relationNames = definition.getRelationNames();
		if (relationNames == null) {
			return;
		}
		
		configItem.setSourceRelations(new RelationSet());
		configItem.setTargetRelations(new RelationSet());
		for (String relationName : relationNames) {
			CIRelationDefinition relationDefinition = metaData.getCIRelationDefinition(relationName, true);
			String displayName = relationDefinition.getDisplayName();
			for (int i=0; i<nodeList.getLength(); i++) {
				Node nextNode = nodeList.item(i);
				if (nextNode.getLocalName().equals(displayName)) {
					extractRelationInstances(relationDefinition, configItem, nextNode);
					break;					
				}
			}	
		}	
	}	
	
	/**
	 * 
	 * @param definition
	 * @param configItem
	 * @param relationNode
	 * @throws CCMDBException
	 */
	private void extractRelationInstances(CIRelationDefinition definition,
			ActualCI configItem, 
			Node relationNode) throws CCMDBException {
	
		NodeList nodeList = relationNode.getChildNodes();
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			CIRelation relation = extractRelation(definition, nextNode);
			if (relation.getTarget() != null) {
				configItem.getSourceRelations().addRelation(relation);	
				relation.setSource(configItem);
			} else if (relation.getSource() != null) {
				configItem.getTargetRelations().addRelation(relation);
				relation.setTarget(configItem);
			}
		}
	}	
		
	/**
	 * 
	 * @param definition
	 * @param nodeList
	 * @return CIRelation
	 * @throws CCMDBException
	 */
	private CIRelation extractRelation(CIRelationDefinition definition, Node node)
		throws CCMDBException {

		CIRelation relation = new CIRelation();	
		relation.setClassName(definition.getClassName());
		if (node.getPrefix() == null) {
			String msg = "Source or target item expected!";	
			throw new CCMDBException(msg);
		}		
		
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_RELATIONNUM, definition.getClassName());
		relation.setProperty(CCMDBActualCISchema.ACTCIRELATION_SWAPPED, 0);
		
		String prefix = node.getPrefix() + ":";
		if (prefix.equals(CDMConstants.CDM_SOURCE_CI_PREFIX)) {
			ActualCI source = createConfigItem(node.getChildNodes());
			relation.setSource(source);
		} else if (prefix.equals(CDMConstants.CDM_TARGET_CI_PREFIX)) {
			ActualCI target = createConfigItem(node.getChildNodes());
			relation.setTarget(target);
		} 
		return relation;
	}
	
	/**
	 * 
	 * @param nodeList
	 * @param configItem
	 * @return String
	 */
	private String addProperties(NodeList nodeList, ActualCI configItem) {

		configItem.setProperty(CCMDBActualCISchema.ACTCI_LANGCODE, "EN");
		configItem.setProperty(CCMDBActualCISchema.ACTCI_LASTSCANDT, new Timestamp(System.currentTimeMillis()));
		configItem.setProperty(CCMDBActualCISchema.ACTCI_HASLD, 0);
		
		String className = null;
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equalsIgnoreCase(CDMSchemaFactory.ATTR_CLASS_TYPE_NAME)) {
				className = nextNode.getNodeValue();
			} else if (nextNode.getNodeName().equalsIgnoreCase(CDMSchemaFactory.ATTR_GUID_NAME)) {
				String guid = nextNode.getNodeValue();
				configItem.setProperty(CCMDBActualCISchema.ACTCI_GUID, guid);
			} 
		}	
		return className;
	}		
	
}
