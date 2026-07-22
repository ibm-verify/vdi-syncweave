/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.model;

import com.ibm.di.connector.ccmdb.provider.CCMDBActualCISchema;

/**
 *  This class is a representation of an actual CI attribute in CCMDB.
 * 
 * @author yavor.gologanov
 *
 */
public class ClassAttribute extends ModelObject {

	private Object value = null;
	
	/**
	 * 
	 * @param name
	 */
	public ClassAttribute(String name) {
		setProperty(CCMDBActualCISchema.ACTCISPEC_ASSETATTRID, name);
	}
	
	/**
	 * 
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getName() {
		return getStringProperty(CCMDBActualCISchema.ACTCISPEC_ASSETATTRID);
	}
	
	/**
	 * 
	 * @param otherAttribute
	 * @return boolean
	 */
	public boolean hasSameValue(ClassAttribute otherAttribute) {
		if (value == null) {
			return false;
		}
		
		if (otherAttribute.getValue() == null) {
			return false;
		}
		
		return value.equals(otherAttribute.getValue());
	}
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(super.toString());
		if (value != null) {
			str.append("\nvalue: " + value);
		}

		return str.toString();
	}
	
}
