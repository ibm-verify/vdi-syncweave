/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains common meta data about a set of related properties. 
 * A representation of  'properties' element from dpaschema.xml.
 * 
 * @author yavor.gologanov
 *
 */
public class PropertySetDefinition {

	private String name = null;
	private String table = null;
	private String joinColumn = null;
	private String onProperty = null;
	private List<PropertyDefinition> propertyList = null;
	
	/**
	 * 
	 */
	protected PropertySetDefinition() {
		
	}	
	
	/**
	 * 
	 * @param table
	 */
	protected void setTable(String table) {
		this.table = table;
	}	
	
	/**
	 * 
	 * @param joinColumn
	 */
	protected void setJoinColumn(String joinColumn) {
		this.joinColumn = joinColumn;
	}	
	
	
	/**
	 * 
	 * @param property
	 */
	protected void addProperty(PropertyDefinition property) {
		if (propertyList == null) {
			propertyList = new ArrayList<PropertyDefinition>();
		}
		this.propertyList.add(property);
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getJoinColumn() {
		return joinColumn;
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getPropertyCount() {
		if (propertyList == null) {
			return 0;
		}
		
		return propertyList.size();
	}
	
	/**
	 * 
	 * @return List<PropertyDefinition>
	 */
	public List<PropertyDefinition> getPropertyList() {
		return propertyList;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getOnProperty() {
		return onProperty;
	}

	/**
	 * 
	 * @param onProperty
	 */
	public void setOnProperty(String onProperty) {
		this.onProperty = onProperty;
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
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}	
	
}
