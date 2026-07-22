/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class SearchAttributeInfo {

	private String searchAttributeName = null;
	private String propertyName = null;
	private String columnName = null;
	private String referencePath = null;
	private String propertySetName = null;
	private String javaClassName = null;

	/**
	 * 
	 * @return String
	 */
	public String getJavaClassName() {
		return javaClassName;
	}

	/**
	 * 
	 * @param javaClassName
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}

	/**
	 * 
	 * @return String
	 */
	public String getPropertySetName() {
		return propertySetName;
	}

	/**
	 * 
	 * @param propertySetName
	 */
	public void setPropertySetName(String propertySetName) {
		this.propertySetName = propertySetName;
	}

	/**
	 * 
	 * @return String
	 */
	public String getSearchAttributeName() {
		return searchAttributeName;
	}
	
	/**
	 * 
	 * @param searchAttributeName
	 */
	public void setSearchAttributeName(String searchAttributeName) {
		this.searchAttributeName = searchAttributeName;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getPropertyName() {
		return propertyName;
	}
	
	/**
	 * 
	 * @param propertyName
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getReferencePath() {
		return referencePath;
	}
	
	/**
	 * 
	 * @param referencePath
	 */
	public void setReferencePath(String referencePath) {
		this.referencePath = referencePath;
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
	 * @param columnName
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}	
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("\n[SearchAttributeInfo: ");
		str.append("\nsearchAttributeName: " + searchAttributeName);
		str.append("\npropertyName: " + propertyName);
		str.append("\ncolumnName: " + columnName);
		str.append("\nreferencePath: " + referencePath);
		str.append("\npropertySetName: " + propertySetName);
		str.append("\n]");
		return str.toString();
	}
	
}
