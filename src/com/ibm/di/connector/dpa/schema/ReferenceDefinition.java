/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

/**
 * This class contains meta data about a class reference. 
 * A representation of the 'reference' element from dpaschema.xml.
 * 
 * @author yavor.gologanov
 *
 */
public class ReferenceDefinition {
	
	public static final String TYPE_AGGREGATION = "aggregation";
	public static final String TYPE_COMPOSITION = "composition";
	
	private String name = null;
	private String className = null;
	private int minOccurs = 0;
	private int maxOccurs = 0;
	private String type = TYPE_COMPOSITION;
	private boolean reversePrimaryKey = false;
	
	private String columnName = null;
	private String onProperty = null;
	private String joinTable = null;
	private String joinColumn = null;
	
	/**
	 * 
	 */
	protected ReferenceDefinition() {
		
	}	
	
	/**
	 * 
	 * @param joinTable
	 */
	protected void setJoinTable(String joinTable) {
		this.joinTable = joinTable;
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
	 * @param reversePrimaryKey
	 */
	protected void setReversePrimaryKey(boolean reversePrimaryKey) {
		this.reversePrimaryKey = reversePrimaryKey;
	}

	/**
	 * 
	 * @param type
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @param name
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param className
	 */
	protected void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * 
	 * @param minOccurs
	 */
	protected void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}
		
	/**
	 * 
	 * @param maxOccurs
	 */
	protected void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}	
	
	/**
	 * 
	 * @param columnName
	 */
	protected void setColumnName(String columnName) {
		this.columnName = columnName;
	}	
	
	/**
	 * 
	 * @param onProperty
	 */
	protected void setOnProperty(String onProperty) {
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
	 * @return String
	 */
	public String getClassName() {
		return className;
	}
		
	/**
	 * 
	 * @return int
	 */
	public int getMinOccurs() {
		return minOccurs;
	}
	
	/**
	 * 
	 * @return int
	 */
	public int getMaxOccurs() {
		return maxOccurs;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getColumnName() {
		return columnName;
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
	 * @return String
	 */
	public String getType() {
		return type;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isMultiple() {
		return ((maxOccurs < 0) || (maxOccurs > 1));
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isRequired() {
		return (minOccurs > 0);
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isReversePrimaryKey() {
		return reversePrimaryKey;
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getJoinTable() {
		return joinTable;
	}

	/**
	 * 
	 * @return String
	 */
	public String getJoinColumn() {
		return joinColumn;
	}

}
