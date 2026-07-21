/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

/**
 * This class contains meta data about the primary key of a class from the model. 
 * A representation of the 'pkDefinition' element from dpaschema.xml.
 * 
 * @author yavor.gologanov
 *
 */
public class UIDDefinition {

	public static final String TYPE_SQL = "sql";
	public static final String TYPE_INHERIT = "inherit";
	
	private String type = null;
	private String value = null;
	
	/**
	 * 
	 */
	protected UIDDefinition() {
		
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
	 * @param type String
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * 
	 * @param value String
	 */
	public void setValue(String value) {
		this.value = value;
	}	
	
}
