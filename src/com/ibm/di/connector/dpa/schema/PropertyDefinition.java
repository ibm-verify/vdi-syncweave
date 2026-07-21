/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.sql.Timestamp;

/**
 * This class contains meta data about a class property. 
 * A representation of the 'property' element from dpaschema.xml.
 * 
 * @author yavor.gologanov
 *
 */
public class PropertyDefinition {

	public static final String STRING = "string";
	public static final String INT = "int";
	public static final String DOUBLE = "double";
	public static final String TIMESTAMP = "timestamp";
	
	/**
	 * 
	 * @param type
	 * @return boolean
	 */
	public static boolean isValidType(String type) {
		return (STRING.equalsIgnoreCase(type) 
				|| INT.equalsIgnoreCase(type)
				|| TIMESTAMP.equalsIgnoreCase(type)
				|| DOUBLE.equalsIgnoreCase(type));
	}
	
	//-------------------------------------------------------------------------
	
	private String name = null;
	private String columnName = null;
	private String type = null;
	private String nativeType = null;
	private boolean required = false;
	private boolean unique = false;
	private boolean primary = false;
	
	/**
	 * 
	 */
	protected PropertyDefinition() {
		
	}
	
	/**
	 * 
	 * @param primary
	 */
	protected void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * 
	 * @param unique
	 */
	protected void setUnique(boolean unique) {
		this.unique = unique;
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
	 * @param columnName
	 */
	protected void setColumnName(String columnName) {
		this.columnName = columnName;
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
	 * @return String
	 */
	public String getNativeType() {
		return nativeType;
	}

	/**
	 * 
	 * @param nativeType
	 */
	public void setNativeType(String nativeType) {
		this.nativeType = nativeType;
	}	
	
	/**
	 * 
	 * @param required
	 */
	protected void setRequired(boolean required) {
		this.required = required;
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
	public String getColumnName() {
		return columnName;
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
	public boolean isRequired() {
		return required;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isUnique() {
		return unique;
	}	
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean isPrimary() {
		return primary;
	}	
	
	/**
	 * 
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof PropertyDefinition)) {
			return false;
		}
				
		PropertyDefinition opd = (PropertyDefinition) o;
		return name.equals(opd.getName());
	}

	/**
	 * 
	 */
	public int hashCode() {
		return name.hashCode();
	}	
	
	/**
	 * 
	 * @return String
	 */
	public String getJavaType() {
		if (STRING.equalsIgnoreCase(type)) {
			return String.class.getCanonicalName();
		} else if (INT.equalsIgnoreCase(type)) {
			return Integer.class.getCanonicalName();
		} else if (TIMESTAMP.equalsIgnoreCase(type)) {
			return Timestamp.class.getCanonicalName();
		} else if (DOUBLE.equalsIgnoreCase(type)) {
			return Double.class.getCanonicalName();
		}
		
		return Object.class.getCanonicalName();
	}
	
}
