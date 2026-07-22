/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * The base definition for an object from the data model. 
 * 
 * @author yavor.gologanov
 *
 */
public class ModelObjectDefinition extends AbstractDefinition {

	private String className = null;
	private Map<String, PropertyDefinition> properties = null;	
	
	/**
	 * 
	 * @param className
	 */
	public ModelObjectDefinition(String className) {
		this.className = className;
		super.setDisplayName(className);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getClassName() {
		return className;
	}	
	
	/**
	 * 
	 * @param property
	 */
	public void addProperty(PropertyDefinition property) {
		if (properties == null) {
			properties = new TreeMap<String, PropertyDefinition>();
		}
		properties.put(property.getName(), property);
	}
	
	/**
	 * 
	 * @param name
	 * @return PropertyDefinition
	 */
	public PropertyDefinition getProperty(String name) {
		if (properties != null) {
			return properties.get(name);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return Collection<PropertyDefinition>
	 */
	public Collection<PropertyDefinition> getProperties() {
		if (properties != null) {
			return properties.values();
		}
		
		return null;
	}	
	
}
