/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model.def;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;

/**
 * The definition of a class attribute. 
 * 
 * @author yavor.gologanov
 *
 */
public class AttributeDefinition extends PropertyDefinition {

	private String description = null;
	private String valueField = null;

	/**
	 * 
	 * @param name
	 */
	public AttributeDefinition(String name) {
		super(name);
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * 
	 * @return Object
	 */
	public Object formatValue(String value) {
		if (valueField.equals(CCMDBActualCISchema.ATTR_NUM_VALUE)) {
			return Double.valueOf(value);
		}
		return value;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getValueField() {
		return valueField;
	}

	/**
	 * 
	 * @param valueField
	 */
	public void setValueField(String valueField) {
		this.valueField = valueField;
	}
	
}
