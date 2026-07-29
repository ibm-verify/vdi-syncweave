/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * An instance of this class contains values for all properties 
 * defined by the corresponding PropertySetDefinition. 
 * 
 * @author yavor.gologanov
 *
 */
public class PropertySetInstance {

	private String name = null;
	private PropertySetDefinition definition = null;	
	private Map<String, Object> properties = null;
	
	/**
	 * 
	 * @param definition
	 */
	public PropertySetInstance(PropertySetDefinition definition) {
		this.definition = definition;
		this.name = definition.getName();
	}	
	
	/**
	 * 
	 * @return PropertySetDefinition
	 */
	public PropertySetDefinition getDefinition() {
		return definition;
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
	 * @return int
	 */
	public int getPropertyCount() {
		if (properties == null) {
			return 0;
		} 
		
		return properties.size();
	}
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getPropertyNames() {
		if (properties != null) {
			return properties.keySet();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param propertyName
	 * @return Object
	 */
	public Object getProperty(String propertyName) {
		if (properties != null) {
			return properties.get(propertyName);
		} else {
			return null;
		}
	}	
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addProperty(String name, Object value) {
		if (properties == null) {
			properties = new TreeMap<String, Object>();
		}
		this.properties.put(name, value);
	}	
			
}
