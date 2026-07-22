/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.dpa.provider.DeployedAssetsSchema;
import com.ibm.di.connector.dpa.provider.SQLQuery;
import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassDefinitionFactory;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceInstance;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.util.ResourceLocator;

/**
 * The implementation of DefaultDPAMetaData designed to work with DPA data schema.
 * 
 * @author yavor.gologanov
 *
 */
public class DefaultDPAMetaData extends AbstractMetaData {
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	

	private static final String SCHEMA_CONFIG = "dpaschema.xml";
	
	//-------------------------------------------------------------------------
	
	private ClassDefinitionFactory classdefFactory = null;
	
	/**
	 * 
	 */
	public void init(String connectorMode) throws DPAException {
		super.init(connectorMode);
		
		this.classdefFactory = new ClassDefinitionFactory();
		URL configFile = ResourceLocator.getResourceURL(SCHEMA_CONFIG);
		try {
			this.classdefFactory.init(configFile);
		} catch (Exception e) {
			log.logError(e);		
			throw new DPAException(e);
		}
		
	}
	
	/**
	 * 
	 */
	public ClassInstance createClassInstance(Entry entry) throws DPAException {
		
		NodeList nodeList = entry.getChildNodes();
		
		String className = getClassName(nodeList);
		if (className == null) {
			throw new DPAException(resHash.getString("DPA.CONN.CLASS.NAME.MISSING", new Object[] {entry.toString()}));
		}
			
		String asesetClass = DeployedAssetsSchema.getAssetClass(className);
		ClassDefinition classDefinition = classdefFactory.getDefinition(asesetClass);
		ClassInstance classInstance = getInstance(classDefinition, nodeList);
		return classInstance;
	}

	/**
	 * 
	 */
	public Entry createEntry(ClassInstance classInstance) {

		DPAsset asset= DPAsset.getAsset(classInstance);
		Entry entry = new Entry(true);	
		transform(entry, asset);
		return entry;
	}

	/**
	 * 
	 */
	public Entry createSchema(String className) {
		
		ClassDefinition classDefinition = classdefFactory.getDefinition(className);
		DPAsset asset= DPAsset.getAsset(classDefinition, classdefFactory);
		Entry schema = new Entry(true);	
		transform(schema, asset);		
		return schema;
	}

	/**
	 * 
	 */
	public SQLQuery createSearchQuery(SearchCriteria criteria, String assetClass) {
	
		ClassDefinition classDefinition = classdefFactory.getDefinition(assetClass);
		SearchQueryBuilder searchQueryBuilder = SearchQueryBuilder.createQueryBuilder(classDefinition, classdefFactory);
		SQLQuery query = searchQueryBuilder.buildQuery(criteria, assetClass);
        log.debug(resHash.getString("DPA.CONN.DEBUG.CREATE.QUERY", new Object[] {query.toString()}));		
		return query;
	}

	/**
	 * 
	 */
	public ClassDefinitionFactory getClassDefinitionFactory() {
		return classdefFactory;
	}	
	

	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param entry
	 * @param asset
	 */
	private void transform(Entry entry, DPAsset asset) {
		Map<String, Object> attributes = asset.getAttributes();
		for (Map.Entry<String, Object> attribute : attributes.entrySet()){
			entry.setAttribute(attribute.getKey(), attribute.getValue());
		}
		
		Set<String> entryNames = asset.getNamedAssetNames();
		for (String nextEntryName : entryNames) {
			DPAsset nextAsset = asset.getNamedAsset(nextEntryName);
			Attribute nextAttr = new Attribute(nextEntryName);
			addAsset(entry, nextAsset, nextAttr);
			entry.appendChild(nextAttr);
		}		
	}
	
	/**
	 * 
	 * @param entry
	 * @param asset
	 * @param attr
	 */
	private void addAsset(Entry entry, DPAsset asset, Attribute attr) {
		Map<String, Object> attributes = asset.getAttributes();
		for (Map.Entry<String,Object> attribute : attributes.entrySet()) {
			if (attribute.getValue() != null) {
				Node nextAttrNode = new Attribute(attribute.getKey());
				nextAttrNode.setNodeValue(attribute.getValue().toString());
				attr.appendChild(nextAttrNode);
			}
		}
		
		Set<String> assetNames = asset.getNamedAssetNames();
		for (String nextAssetName : assetNames) {
			DPAsset nextAsset = asset.getNamedAsset(nextAssetName);
			Attribute nextEntryAttr = new Attribute(nextAssetName);
			addAsset(entry, nextAsset, nextEntryAttr);
			attr.appendChild(nextEntryAttr);
		}
		
		List<DPAsset> assets = asset.getAssets();
		if ((assets != null) && (assets.size() > 0)) {
			for (DPAsset nextAsset : assets) {
				Attribute nextEntryAttr = new Attribute(nextAsset.getName());
				addAsset(entry, nextAsset, nextEntryAttr);
				attr.appendChild(nextEntryAttr);
			}
		}
	}	
	
	/**
	 * 
	 * @param classDefinition
	 * @param nodeList
	 * @return ClassInstance
	 */
	private ClassInstance getInstance(ClassDefinition classDefinition, 
			NodeList nodeList) {
		
		ClassInstance classInstance = new ClassInstance(classDefinition);
		
		if (classDefinition.getParent() != null) {
			String parentClass = classDefinition.getParent().getClassName();
			ClassDefinition parentDefinition = classdefFactory.getDefinition(parentClass);
			ClassInstance parentInstance = getInstance(parentDefinition, nodeList);
			ReferenceInstance parentReference = new ReferenceInstance(classDefinition.getParent());
			parentReference.addClassInstance(parentInstance);
			classInstance.setParent(parentReference);
		}			
		
		Map<String, Node> namedNodeMap = getNamedNodeMap(nodeList);	
		loadProperties(classInstance, namedNodeMap);
		loadRelations(classInstance, namedNodeMap);
		return classInstance;			
	}	
	
	/**
	 * 
	 * @param classInstance
	 * @param namedNodeMap
	 */
	private void loadProperties(ClassInstance classInstance, Map<String, Node> namedNodeMap) {

		ClassDefinition classsdef = classInstance.getDefinition();
		PropertySetDefinition properties = classsdef.getProperties();
		if ((properties == null) || (properties.getPropertyList() == null)) {
			return; 
		}
							
		List<PropertyDefinition> propDefList = properties.getPropertyList();
		for (PropertyDefinition nextPropDef : propDefList) {
			Node node = namedNodeMap.get(nextPropDef.getName());
			if (node != null) {
				Object propValue = node.getNodeValue();
				classInstance.setProperty(nextPropDef.getName(), propValue);
			}
		}

	}
	
	/**
	 * 
	 * @param classInstance
	 * @param namedNodeMap
	 */
	private void loadRelations(ClassInstance classInstance, Map<String, Node> namedNodeMap) {
	
		ClassDefinition classsdef = classInstance.getDefinition();		
		if (classsdef.getReferenceCount() == 0) {
			return; 
		}
	
		List<ReferenceDefinition> references = classsdef.getReferences();
		for (ReferenceDefinition nextRefDef : references) {
			Node node = namedNodeMap.get(nextRefDef.getName());
			if (node == null) {
				continue;
			}
			
			String refClassName = nextRefDef.getClassName();
			ClassDefinition refClassDdef = classdefFactory.getDefinition(refClassName);
			if (nextRefDef.isMultiple()) {
				ReferenceInstance refInst = new ReferenceInstance(nextRefDef);
				NodeList childNodes = node.getChildNodes();
				for (int i=0; i<childNodes.getLength(); i++) {
					Node nextChild = childNodes.item(i);
					if (refClassName.equalsIgnoreCase(nextChild.getNodeName())) {
						ClassInstance refClassInst = getInstance(refClassDdef, nextChild.getChildNodes());
						refInst.addClassInstance(refClassInst);
					}
				}
				classInstance.addReference(refInst);
			} else {
				ClassInstance refClassInst = getInstance(refClassDdef, node.getChildNodes());
				if (refClassInst != null) {
					ReferenceInstance refInst = new ReferenceInstance(nextRefDef);
					refInst.addClassInstance(refClassInst);
					classInstance.addReference(refInst);
				}
			}
		}
	}	
	
	/**
	 * 
	 * @param nodeList
	 * @return Map<String, Node>
	 */
	private Map<String, Node> getNamedNodeMap(NodeList nodeList) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			nodeMap.put(nextNode.getNodeName(), nextNode);
		}		
		return nodeMap;
	}
	
	/**
	 * 
	 * @param nodeList
	 * @return String
	 */
	private String getClassName(NodeList nodeList) {
		for (int i=0; i<nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equals(DeployedAssetsSchema.CLASS_PROPERTY_NAME)) {
				return nextNode.getNodeValue();
			}
		}		
		return null;
	}
	
}
