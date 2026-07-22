/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

/**
 * The definition for a class property. 
 * 
 * @author yavor.gologanov
 *
 */
public class PropertyDefinition extends AbstractDefinition {

	private String name = null;
	private String javaClassName = null;
		
	/**
	 * 
	 * @param name
	 */
	public PropertyDefinition(String name) {
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
	 */
	public String getDisplayName() {
		if (super.getDisplayName() == null) {
			return name;
		}
		else {
			return super.getDisplayName();
		}
	}
	
}
