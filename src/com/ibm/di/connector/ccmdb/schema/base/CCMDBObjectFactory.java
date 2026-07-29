/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.schema.base;

import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.ccmdb.CCMDBException;
import com.ibm.di.connector.ccmdb.EntryUtilities;
import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.CIRelation;
import com.ibm.di.connector.ccmdb.model.RelationSet;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.CIRelationDefinition;
import com.ibm.di.connector.ccmdb.model.def.Classification;
import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This class is responsible for creating of data model objects from DI Entries.
 *
 * @author yavor.gologanov
 *
 */
public class CCMDBObjectFactory {
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ccmdbconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	private static final String CLASSTYPE_ATTR = "CLASSIFICATION";

	private CCMDBMetaData metaData = null;

	/**
	 *
	 * @param metaData
	 */
	public CCMDBObjectFactory(CCMDBMetaData metaData) {
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
		ActualCI configItem = createConfigItem(nodeList,false);

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

		NodeList nodeList = entry.getChildNodes();
		CIRelation relation = createRelation(nodeList);

		return relation;
	}

	//-------------------------------------------------------------------------

	/**
	 *
	 * @param nodeList
	 * @return ActualCI
	 * @throws CCMDBException
	 */
	public  ActualCI createConfigItem(NodeList nodeList,boolean isRelationship)
		throws CCMDBException {

		// Note: this method is used for creating source and target CIs of a relationship also

		String classstructureId = getActualCIClassstructure(nodeList);

		if(isRelationship && classstructureId == null)
		{
			throw new CCMDBException("The attribute 'classstructureId' is required for source/target to add a relationship");
		}

		Classification classification = metaData.getClassificationByClassstructure(classstructureId);

		if(!isRelationship)
		{
			// Check if classstructureid and the selected classType is compatible
			String selectedClassType = metaData.getSelectedClassification();
			if(selectedClassType == null)
			{
				selectedClassType = "";
			}
			if(classification != null)
			{
				if(!selectedClassType.equalsIgnoreCase(classification.getClassName()))
				{
					throw new CCMDBException("ClasstructureId '" + classstructureId + "' incompatible with the selected class type '"  + selectedClassType + "'");
				}
			}
			else if(classification == null)
			{
				// if classification name is provided, check if it is the same as the selected class type
				String classificationID = getActualCIClassification(nodeList);
				if(classificationID == null) // the user has not provided both classstructureid and classification
				{
					classificationID = selectedClassType;
				}
				else if(!selectedClassType.equalsIgnoreCase(classificationID))
				{
					throw new CCMDBException("The classification '" + classificationID + "' is incompatible with the selected class type '"  + selectedClassType + "'");
				}
				classification = metaData.getClassificationByClassType(classificationID);
				if(classification == null)
				{
					throw new CCMDBException(resHash.getString("CCMDB.CONN.NO.CLASS.TYPE.FOUND", new Object[] {classificationID}));
				}
			}
		}

		ActualCI configItem = new ActualCI();
		configItem.setClassification(classification);
		ActualCIDefinition definition = metaData.getActualCIDefinition(configItem.getClassName(), true, true);

		EntryUtilities.addPropertiesToObject(configItem, definition, nodeList);
		EntryUtilities.addAttributesToObject(configItem, definition, nodeList);

		extractRelations(configItem, definition, nodeList);

		if (configItem.getDeletedActualCI() != null) {
			extractActciDeletedCI(configItem, definition, nodeList);
		}

		if (configItem.getOmpRelation() != null) {
			extractActciOMPRelation(configItem, definition, nodeList);
		}
		return configItem;
	}

	/**
	 *
	 * @param nodeList
	 * @return CIRelation
	 * @throws CCMDBException
	 */
	private CIRelation createRelation(NodeList nodeList)
		throws CCMDBException {

		String classification = getCIRelationName(nodeList);

		String selectedClassification = metaData.getSelectedClassification();
		 if(classification == null){
		   	classification = selectedClassification;
		  }
		 else if(!classification.equals(selectedClassification)){
		   	throw new CCMDBException("relationnum attribute - "  + classification + " does not match the selected relationship class type - "
		   			+ selectedClassification);
		  }

		CIRelationDefinition definition = metaData.getCIRelationDefinition(classification, true);
		//
		CIRelation relation = new CIRelation();
		relation.setClassName(definition.getClassName());
		EntryUtilities.addPropertiesToObject(relation, definition, nodeList);

		// Get details about source and target CI.
		// Here we will get enough details needed to determine if the source or target CI exists.
		// We need GUID to determine the existence of a CI
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			String nodeName = nextNode.getNodeName();
			boolean isSourceNode = nodeName.equals(CCMDBSchemaFactory.ATTR_REL_SOURCE);
			boolean isTargetNode = nodeName.equals(CCMDBSchemaFactory.ATTR_REL_TARGET);
			if (isSourceNode|| isTargetNode) {
				NodeList sChildNodeList = nextNode.getChildNodes();
				ActualCI ci = new ActualCI();
				for (int k=0; k<sChildNodeList.getLength(); k++) {
					Node nNode = sChildNodeList.item(k);
					if(nNode.getLocalName().toLowerCase().equalsIgnoreCase(CCMDBActualCISchema.ACTCI_GUID)) {
						ci.setProperty(CCMDBActualCISchema.ACTCI_GUID, nNode.getNodeValue());
						break;
					}
				}
				if(isSourceNode){
					relation.setSource(ci);
				}
				else if(isTargetNode){
					relation.setTarget(ci);
				}
			}
				// Added - Done
		}

		//

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
			if (!nextNode.getNodeName().equals(CCMDBSchemaFactory.ATTR_REL_INSTANCE)) {
				continue;
			}

			NodeList relNodeList = nextNode.getChildNodes();
			CIRelation relation = extractRelation(definition, relNodeList);
			if (relation.getSourceGuid().equals(configItem.getGuid())) {
				configItem.getSourceRelations().addRelation(relation);
				relation.setSource(configItem);
			} else if (relation.getTargetGuid().equals(configItem.getGuid())) {
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
	private CIRelation extractRelation(CIRelationDefinition definition, NodeList nodeList)
		throws CCMDBException {

		CIRelation relation = new CIRelation();
		relation.setClassName(definition.getClassName());
		EntryUtilities.addPropertiesToObject(relation, definition, nodeList);

		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equals(CCMDBSchemaFactory.ATTR_REL_SOURCE)) {
				ActualCI source = createConfigItem(nextNode.getChildNodes(), true);
				relation.setSource(source);
			} else if (nextNode.getNodeName().equals(CCMDBSchemaFactory.ATTR_REL_TARGET)) {
				ActualCI target = createConfigItem(nextNode.getChildNodes(), true);
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
	 */
	private void extractActciDeletedCI(ActualCI configItem,
			ActualCIDefinition definition,
			NodeList nodeList) {
		//throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @param configItem
	 * @param definition
	 * @param nodeList
	 */
	private void extractActciOMPRelation(ActualCI configItem,
			ActualCIDefinition definition,
			NodeList nodeList) {
		//throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @param nodeList
	 * @return String
	 */
	private String getActualCIClassstructure(NodeList nodeList) {
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equalsIgnoreCase(CCMDBActualCISchema.ACTCI_CLASSSTRUCTUREID)) {
				return nextNode.getNodeValue();
			}
		}
		return null;
	}

	/**
	 *
	 * @param nodeList
	 * @return String
	 */
	private String getActualCIClassification(NodeList nodeList) {
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equalsIgnoreCase(CLASSTYPE_ATTR)) {
				return nextNode.getNodeValue();
			}
		}
		return null;
	}

	/**
	 *
	 * @param nodeList
	 * @return String
	 */
	private String getCIRelationName(NodeList nodeList) {
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equalsIgnoreCase(CCMDBActualCISchema.ACTCIRELATION_RELATIONNUM)) {
				return nextNode.getNodeValue();
			}
		}
		return null;
	}

}
