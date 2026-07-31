/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.di.connector.dpa.schema.ClassDefinition;
import com.ibm.di.connector.dpa.schema.ClassDefinitionFactory;
import com.ibm.di.connector.dpa.schema.ClassInstance;
import com.ibm.di.connector.dpa.schema.PropertyDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetDefinition;
import com.ibm.di.connector.dpa.schema.PropertySetInstance;
import com.ibm.di.connector.dpa.schema.ReferenceDefinition;
import com.ibm.di.connector.dpa.schema.ReferenceInstance;

/**
 * The internal representation of a deployed asset
 * 
 * @author yavor.gologanov
 *
 */
public class DPAsset {
	
	/**
	 * 
	 * @param classDefinition
	 * @param classdefFactory
	 * @return Map<String, SearchAttributeInfo>
	 */
	public static Map<String, SearchAttributeInfo> getAttributeInfoMap(ClassDefinition classDefinition, 
			ClassDefinitionFactory classdefFactory) {
		
		Map<String, SearchAttributeInfo> attrMap = new HashMap<String, SearchAttributeInfo>();
		
		ReferenceDefinition parent = classDefinition.getParent();
		if (parent != null) {
			String parentName = parent.getClassName();
			ClassDefinition parentDefinition = classdefFactory.getDefinition(parentName);
			addAttrInfoDefinitions(parentDefinition, 
					attrMap, 
					parent.getName(), 
					"",
					classdefFactory);			
		}	
			
		addAttrInfoDefinitions(classDefinition, attrMap, null, "", classdefFactory);	
		
		return attrMap;
	}	
	
	/**
	 * 
	 * @param classDefinition
	 * @param classdefFactory
	 * @return DPAsset
	 */
	public static DPAsset getAsset(ClassDefinition classDefinition, 
			ClassDefinitionFactory classdefFactory) {
		
		DPAsset root = new DPAsset("root");
		
		// Add properties from parent class
		ReferenceDefinition parent = classDefinition.getParent();
		if (parent != null) {
			ClassDefinition parentDefinition = classdefFactory.getDefinition(parent.getClassName());
			addClassDefinition(parentDefinition, root, classdefFactory);			
		}	
			
		addClassDefinition(classDefinition, root, classdefFactory);		
		
		return root;
	}
 
	/**
	 * 
	 * @param classInstance
	 * @return DPAsset
	 */
	public static DPAsset getAsset(ClassInstance classInstance) {
		
		DPAsset root = new DPAsset("root");
		
		ReferenceInstance parent = classInstance.getParent();
		if (parent != null) {
			addClassInstance(parent.getFirstClassInstance(), root);			
		}	
			
		addClassInstance(classInstance, root);		
		
		return root;
	}	
	
	/**
	 * 
	 * @param propertySet
	 * @param asset
	 */
	private static void addPropertyDefinitions(PropertySetDefinition propertySet, 
			DPAsset asset) {
		if ((propertySet == null) || (propertySet.getPropertyCount() == 0)) {
			return;
		}
		
		List<PropertyDefinition> propDefList = propertySet.getPropertyList();
		for (PropertyDefinition nextDef : propDefList) {
			asset.addAttribute(nextDef.getName(), nextDef.getJavaType());		
		}		
	}	
	
	/**
	 * 
	 * @param classInstance
	 * @param asset
	 */
	private static void addProperties(ClassInstance classInstance, DPAsset asset) {
		
		// Add additional properties
		if (classInstance.getAdditionalPropertiesCount() != 0) {

			Collection<PropertySetInstance> addProps = classInstance.getAdditionalProperties();
			for (PropertySetInstance nextPS : addProps) {
				Set<String> propertyNames = nextPS.getPropertyNames();
				if (propertyNames != null) {
					for (String nextPropName : propertyNames) {
						Object value = nextPS.getProperty(nextPropName);
						if (value != null) {
							asset.addAttribute(nextPropName, value);	
						}
					}	
				}
			}
		}
		
		// Add primary properties
		if (classInstance.getPropertiesCount() > 0) {
			Set<String> propertyNames = classInstance.getPropertyNames();
			for (String nextPropName : propertyNames) {
				Object value = classInstance.getProperty(nextPropName);
				if (value != null) {
					asset.addAttribute(nextPropName, value);	
				}
			}	
		}
	}		
	
	/**
	 * 
	 * @param classDefinition
	 * @param asset
	 * @param classdefFactory
	 */
	private static void addClassDefinition(ClassDefinition classDefinition, 
			DPAsset asset,
			ClassDefinitionFactory classdefFactory) {
		
		if (classDefinition.getAdditionalPropertyCount() > 0) {
			List<PropertySetDefinition> additionalProperties = classDefinition.getAdditionalProperties();
			for (PropertySetDefinition nextPropertySet : additionalProperties) {
				addPropertyDefinitions(nextPropertySet, asset);
			}
		}		
		
		PropertySetDefinition properties = classDefinition.getProperties();
		if (properties != null) {
			addPropertyDefinitions(properties, asset); 
		}
				
		if (classDefinition.getReferenceCount() == 0) {
			return;
		}
		
		List<ReferenceDefinition> references = classDefinition.getReferences();
		for (ReferenceDefinition nextRefDef : references) {
			ClassDefinition nextClassDef = classdefFactory.getDefinition(nextRefDef.getClassName());
			if (nextRefDef.isMultiple()) {
				DPAsset nextRefEntry = new DPAsset(nextRefDef.getName());
				DPAsset classEntry = new DPAsset(nextClassDef.getClassName());
				addClassDefinition(nextClassDef, classEntry, classdefFactory);
				nextRefEntry.addAsset(classEntry);
				asset.addNamedAsset(nextRefEntry.getName(), nextRefEntry);
			} else {					
				DPAsset nextRefEntry = new DPAsset(nextRefDef.getName());	
				addClassDefinition(nextClassDef, nextRefEntry, classdefFactory);
				asset.addNamedAsset(nextRefEntry.getName(), nextRefEntry);
			}
		}		
	}		

	/**
	 * 
	 * @param classInstance
	 * @param entry
	 */
	private static void addClassInstance(ClassInstance classInstance, 
			DPAsset asset) {
		
		addProperties(classInstance, asset);
		
		// Add references		
		if (classInstance.getReferenceCount() > 0) {
			Set<String> referenceNames = classInstance.getReferenceNames();
			for (String nextRefName : referenceNames) {
				ReferenceInstance refInstance = classInstance.getReference(nextRefName);
				List<ClassInstance> refClassInstances = refInstance.getClassInstances();
				
				if (refInstance.getDefinition().isMultiple()) {
					DPAsset nextRefEntry = new DPAsset(nextRefName);	
					for (ClassInstance nextInstance : refClassInstances) {				
						DPAsset nextClassEntry = new DPAsset(nextInstance.getClassName());	
						addClassInstance(nextInstance, nextClassEntry);
						nextRefEntry.addAsset(nextClassEntry);
					}				
					asset.addNamedAsset(nextRefEntry.getName(), nextRefEntry);
				} else if (refClassInstances.size() == 1) {
					ClassInstance nextInstance = refClassInstances.get(0);
					DPAsset nextRefEntry = new DPAsset(nextRefName);	
					addClassInstance(nextInstance, nextRefEntry);
					asset.addNamedAsset(nextRefEntry.getName(), nextRefEntry);
				}
			}	
		}	
	}			
	
	/**
	 * 
	 * @param classDefinition
	 * @param attr
	 * @param schema
	 */
	private static void addAttrInfoDefinitions(ClassDefinition classDefinition, 
			Map<String, SearchAttributeInfo> attrMap,
			String referencePath,
			String attrNamePrefix,
			ClassDefinitionFactory classdefFactory) {
		
		if (classDefinition.getAdditionalPropertyCount() > 0) {
			List<PropertySetDefinition> additionalProperties = classDefinition.getAdditionalProperties();
			for (PropertySetDefinition nextPropertySet : additionalProperties) {
				addAttrInfoDefinitions(nextPropertySet, attrMap, referencePath, attrNamePrefix);
			}
		}		
		
		PropertySetDefinition properties = classDefinition.getProperties();
		if (properties != null) {
			addAttrInfoDefinitions(properties, attrMap, referencePath, attrNamePrefix); 
		}
				
		if (classDefinition.getReferenceCount() == 0) {
			return;
		}
		
		List<ReferenceDefinition> references = classDefinition.getReferences();
		for (ReferenceDefinition nextRefDef : references) {
			ClassDefinition nextClassDef = classdefFactory.getDefinition(nextRefDef.getClassName());
			String newReferencePath = referencePath + ":" + nextRefDef.getName();
			String newAttrNamePrefix = attrNamePrefix + "." + nextRefDef.getName();
			addAttrInfoDefinitions(nextClassDef, attrMap, newReferencePath, newAttrNamePrefix, classdefFactory);
		}		
	}			
	
	/**
	 * 
	 * @param propertySet
	 * @param attrMap
	 * @param relationPath
	 * @param attrNamePrefix
	 */
	private static void addAttrInfoDefinitions(PropertySetDefinition propertySet, 
			Map<String, SearchAttributeInfo> attrMap,
			String referencePath,
			String attrNamePrefix) {
		
		if ((propertySet == null) || (propertySet.getPropertyCount() == 0)){
			return;
		}
		
		List<PropertyDefinition> propDefList = propertySet.getPropertyList();
		for (PropertyDefinition nextDef : propDefList) {
			SearchAttributeInfo attrInfo = new SearchAttributeInfo();
			String searchAttributeName = attrNamePrefix + nextDef.getName();
			attrInfo.setSearchAttributeName(searchAttributeName);
			attrInfo.setPropertyName(nextDef.getName());
			attrInfo.setReferencePath(referencePath);
			attrInfo.setPropertySetName(propertySet.getName());
			attrInfo.setColumnName(nextDef.getColumnName());
			attrInfo.setJavaClassName(nextDef.getJavaType());
			attrMap.put(attrInfo.getSearchAttributeName(), attrInfo);
		}		
	}	
	
	//-------------------------------------------------------------------------
	
	private String name = null;
	private Map<String, Object> attributes = new TreeMap<String, Object>();
	private Map<String, DPAsset> namedAssets = new TreeMap<String, DPAsset>();
	private List<DPAsset> assetList = new ArrayList<DPAsset>();

	/**
	 * 
	 * @param name
	 */
	public DPAsset(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getNamedAssetNames() {
		return namedAssets.keySet();
	}

	/**
	 * 
	 * @param name
	 * @return DPAsset
	 */
	public DPAsset getNamedAsset(String name) {
		return namedAssets.get(name);
	}	
	
	/**
	 * 
	 * @param name
	 * @param asset
	 */
	public void addNamedAsset(String name, DPAsset asset) {
		namedAssets.put(name, asset);
	}	
	
	/**
	 * 
	 * @return List<DPAsset>
	 */
	public List<DPAsset> getAssets() {
		return assetList;
	}	
	
	/**
	 * 
	 * @param asset
	 */
	public void addAsset(DPAsset asset) {
		assetList.add(asset);
	}	
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("\n[ DPA ENTRY: " + name);
		str.append("\nATTRIBUTES " + attributes);
		str.append("\nNAMED ASSETS " + namedAssets);
		str.append("\nASSETS " + assetList);
		str.append("\n]");
		return str.toString();
	}
	
}
