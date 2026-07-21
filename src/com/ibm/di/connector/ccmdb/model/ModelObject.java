/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is a base class for all classes from the data model.
 * 
 * @author yavor.gologanov
 *
 */
public class ModelObject {

	private Map<String, Object> properties = null;

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, Object value) {
		if (properties == null) {
			properties = new TreeMap<String, Object>();
		}
		
		this.properties.put(name, value);
	}
	
	/**
	 * 
	 * @return Map<String, Object>
	 */
	protected Map<String, Object> getProperties() {
		return properties;
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getPropertyCount() {
		if (properties != null) {
			return properties.size();
		}
		
		return 0;
	}
	
	/**
	 * 
	 * @return Set<String>
	 */
	public Set<String> getPropertyNames() {
		if (properties != null) {
			return properties.keySet();
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param name
	 * @return Object
	 */
	public Object getProperty(String name) {

		if (properties != null) {
			return properties.get(name);
		}
		
		return null;
	}	
	
	/**
	 * 
	 * @param name
	 * @return String
	 */
	public String getStringProperty(String name) {

		if (properties != null) {
			return (String) properties.get(name);
		}
		
		return null;
	}		
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		if (properties != null) {
			Set<String> propertyNames = properties.keySet();
			for (String nextPropertyName : propertyNames) {
				str.append("\n");
				str.append(nextPropertyName);
				str.append(" : ");
				str.append(properties.get(nextPropertyName));
			}
		} else {
			str.append("EMPTY");
		}
		return str.toString();
	}
	
}
