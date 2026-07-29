/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

import java.util.Collection;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.ccmdb.model.ActualCI;
import com.ibm.di.connector.ccmdb.model.ClassAttribute;
import com.ibm.di.connector.ccmdb.model.ModelObject;
import com.ibm.di.connector.ccmdb.model.def.ActualCIDefinition;
import com.ibm.di.connector.ccmdb.model.def.AttributeDefinition;
import com.ibm.di.connector.ccmdb.model.def.ModelObjectDefinition;
import com.ibm.di.connector.ccmdb.model.def.PropertyDefinition;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * An utilities class that contains several static methods for manipulating Entry objects.
 * 
 * @author yavor.gologanov
 *
 */
public class EntryUtilities {

	/**
	 * Adds property definitions as attributes to a DI Entry.
	 * 
	 * @param definition 
	 *				the ModelObjectDefinition instance whose properties to be added.
	 * @param schema 
	 * 				the DI Entry to be updated
	 */
	public static void addPropertiesToSchema(ModelObjectDefinition definition, Entry schema) {		
		Collection<PropertyDefinition> properties = definition.getProperties();
		if (properties == null) {
			return;
		}
		
		for (PropertyDefinition propDef : properties) {
			if (!propDef.isVisible()) {
				continue;
			}
			schema.setAttribute(propDef.getDisplayName(), propDef.getJavaClassName());	
			if (propDef.getDisplayPrefix() != null) {
				schema.getAttribute(propDef.getDisplayName()).setPrefix(propDef.getDisplayPrefix());
			}
		}		
	}	
	
	/**
	 * Adds property definitions as child attributes to an Attribute.
	 * 
	 * @param attribute 
	 * 				the attribute to be updated
	 * @param definition 
	 * 				the ModelObjectDefinition instance whose properties to be added.
	 * @param schema 
	 * 				parent DI Entry object
	 */
	public static void addPropertiesToSchemaAttribute(Attribute attribute, 
		ModelObjectDefinition definition, 
		Entry schema) {
		
		Collection<PropertyDefinition> properties = definition.getProperties();
		if (properties == null) {
			return;
		}
		
		for (PropertyDefinition propDef : properties) {
			
			if (!propDef.isVisible()) { 
				continue;
			}
			
			Node nextPropNode = new Attribute(propDef.getDisplayName());
			if (propDef.getDisplayPrefix() != null) {
				nextPropNode.setPrefix(propDef.getDisplayPrefix());
			}
			
			nextPropNode.setNodeValue(propDef.getJavaClassName());
			attribute.appendChild(nextPropNode);
		}
	}		
	
	/**
	 * Adds class attribute definitions as attributes to a DI Entry.
	 * 
	 * @param attributes 
	 * 				class attribute definitions to be added.
	 * @param schema 
	 * 				the DI Entry to be updated
	 */
	public static void addAttributesToSchema(Collection<AttributeDefinition> attributes, 
			Entry schema) {
		
		if (attributes == null) {
			return;
		}
		
		for (AttributeDefinition attrDef : attributes) {
			if (!attrDef.isVisible()) {
				continue;
			}
			
			schema.setAttribute(attrDef.getDisplayName(), attrDef.getJavaClassName());	
			if (attrDef.getDisplayPrefix() != null) {
				schema.getAttribute(attrDef.getDisplayName()).setPrefix(attrDef.getDisplayPrefix());
			}
		}		
	}			
	
	/**
	 *  Adds class attribute definitions as attributes to a DI Entry attribute.   
	 *     
	 * @param attribute 
	 * 				the DI Entry attribute to be updated
	 * @param attributes 
	 * 				class attribute definitions to be added
	 * @param schema 
	 * 				parent DI Entry object
	 */
	public static void addAttributesToSchemaAttribute(Attribute attribute, 
		Collection<AttributeDefinition> attributes, 
		Entry schema) {
		
		if (attributes == null) {
			return;
		}
		
		for (AttributeDefinition attrDef : attributes) {
			
			if (!attrDef.isVisible()) {
				continue;
			}
			
			Node nextAttrNode = new Attribute(attrDef.getDisplayName());
			if (attrDef.getDisplayPrefix() != null) {
				nextAttrNode.setPrefix(attrDef.getDisplayPrefix());
			}
			
			nextAttrNode.setNodeValue(attrDef.getJavaClassName());
			attribute.appendChild(nextAttrNode);
		}
	}	
	
	/**
	 * Adds object properties as attributes to a DI Entry.
	 * 
	 * @param object 
	 * 				the model object whose properties to be added.
	 * @param definition 
	 * 				the object definition.
	 * @param entry 
	 * 				the DI Entry to be updated.
	 */
	public static void addPropertiesToEntry(ModelObject object, 
			ModelObjectDefinition definition, 
			Entry entry) {
		
		Collection<PropertyDefinition> propDefList = definition.getProperties();
		if (propDefList == null) {
			return;
		}
		
		for (PropertyDefinition propDef : propDefList) {
			if (!propDef.isVisible()) {
				continue;
			}
			
			Object value = object.getProperty(propDef.getName());
			if (value != null) {
				entry.setAttribute(propDef.getDisplayName(), value);
				if (propDef.getDisplayPrefix() != null) {
					entry.getAttribute(propDef.getDisplayName()).setPrefix(propDef.getDisplayPrefix());
				}
			}
		}		
	}	
	
	/**
	 * Adds object attributes as attributes to a DI Entry.
	 * 
	 * @param configItem 
	 * 				the configuration item whose properties to be added.
	 * @param definition 
	 * 				the object definition.
	 * @param entry 
	 * 				the DI Entry to be updated.
	 */
	public static void addAttributesToEntry(ActualCI configItem, 
			ActualCIDefinition definition, 
			Entry entry) {
		
		Collection<AttributeDefinition> attrDefList = definition.getAttributes();
		if (attrDefList == null) {
			return;
		}
		
		for (AttributeDefinition attrDef : attrDefList) {
			if (!attrDef.isVisible()) {
				continue;
			}
			
			Object value = configItem.getAttributeValue(attrDef.getName());
			if (value != null) {
				entry.setAttribute(attrDef.getDisplayName(), value);	
				if (attrDef.getDisplayPrefix() != null) {
					entry.getAttribute(attrDef.getDisplayName()).setPrefix(attrDef.getDisplayPrefix());
				}
			}
		}		
	}		
	
	/**
	 * Adds object attributes as child attributes to a DI Entry attribute.
	 * 
	 * @param attribute 
	 * 				the Entry attribute to be updated
	 * @param configItem 
	 * 				the object whose attributes to be added
	 * @param definition 
	 * 				the object definition
	 * @param entry 
	 * 				parent DI Entry
	 */
	public static void addAttributesToEntryAttribute(Attribute attribute, 
			ActualCI configItem, 
			ActualCIDefinition definition,
			Entry entry) {
		
		Collection<AttributeDefinition> attrDefList = definition.getAttributes();
		if (attrDefList == null) {
			return;
		}
		
		for (AttributeDefinition attrDef : attrDefList) {	
			if (!attrDef.isVisible()) {
				continue;
			}
			
			Object value = configItem.getAttributeValue(attrDef.getName());
			if (value != null) {
				Node nextAttrNode = new Attribute(attrDef.getDisplayName());
				if (attrDef.getDisplayPrefix() != null) {
					nextAttrNode.setPrefix(attrDef.getDisplayPrefix());
				}
				nextAttrNode.setNodeValue(value.toString());
				attribute.appendChild(nextAttrNode);
			}
		}
	}			
	
	/**
	 * Adds object attributes as child attributes to a DI Entry attribute.
	 * 
	 * @param attribute 
	 * 				the Entry attribute to be updated
	 * @param object 
	 * 				the object whose properties to be added.
	 * @param definition 
	 * 				the object definition
	 * @param entry 
	 * 				parent DI Entry
	 */
	public static void addPropertiesToEntryAttribute(Attribute attribute, 
			ModelObject object, 
			ModelObjectDefinition definition,
			Entry entry) {
		
		Collection<PropertyDefinition> propDefList = definition.getProperties();
		if (propDefList == null) {
			return;
		}
		
		for (PropertyDefinition propDef : propDefList) {		
			if (!propDef.isVisible()) {
				continue;
			}
			
			Object value = object.getProperty(propDef.getName());
			if (value != null) {
				Node nextPropNode = new Attribute(propDef.getDisplayName());
				if (propDef.getDisplayPrefix() != null) {
					nextPropNode.setPrefix(propDef.getDisplayPrefix());
				}				
				
				nextPropNode.setNodeValue(value.toString());
				attribute.appendChild(nextPropNode);
			}
		}
	}	
	
	/**
	 * Extract property values form a NodeList and add them to a ModelObject.
	 * 
	 * @param object 
	 * 				the ModeObject instance to be updated
	 * @param definition 
	 * 				the object definition
	 * @param attrNodeList 
	 * 				a NodeList that contains property values
	 */
	public static void addPropertiesToObject(ModelObject object, 
			ModelObjectDefinition definition, 
			NodeList attrNodeList) {
		
		Collection<PropertyDefinition> propDefList = definition.getProperties();
		if (propDefList == null) {
			return;
		}
		
		for (PropertyDefinition propDef : propDefList) {
			String displayName = propDef.getDisplayName();
			for (int i=0; i<attrNodeList.getLength(); i++) {
				Node nextNode = attrNodeList.item(i);
				if (nextNode.getLocalName().equals(displayName)) {
					object.setProperty(propDef.getName(), nextNode.getNodeValue());
				}
			}			
		}		
	}	
	
	/**
	 * Extract attribute values form a NodeList and add them to a ModelObject.
	 * 
	 * @param configItem 
	 * 				the configuration item to be updated 
	 * @param definition 
	 * 				the object definition
	 * @param attrNodeList 
	 * 				a NodeList that contains property values
	 */
	public static void addAttributesToObject(ActualCI configItem, 
			ActualCIDefinition definition, 
			NodeList attrNodeList) {
		
		Collection<AttributeDefinition> attrDefList = definition.getAttributes();
		if (attrDefList == null) {
			return;
		}
		
		for (AttributeDefinition attrDef : attrDefList) {
			String displayName = attrDef.getDisplayName();
			for (int i=0; i<attrNodeList.getLength(); i++) {
				Node nextNode = attrNodeList.item(i);
				if (nextNode.getLocalName().equals(displayName)) {
					ClassAttribute ccmdbAttribute = new ClassAttribute(attrDef.getName());
					Attribute attr = (Attribute) nextNode;
					
					ccmdbAttribute.setValue(attrDef.formatValue(attr.getValue()));
					configItem.addAttribute(ccmdbAttribute);
					break;
				}
			}			
		}		
	}	
	
}
