/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.tivoli.namereconciliation.guid.Guid;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class ConfigurationItem {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@SuppressWarnings("rawtypes")
	private HashMap attributes;
	private String classNameType;
	private String id;
	private Guid guid;

	/**
	 * @return the guid
	 */
	public Guid getGuid() {
		return guid;
	}

	/**
	 * @param guid
	 *            the guid to set
	 */
	public void setGuid(Guid guid) {
		this.guid = guid;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the attributes
	 */
	@SuppressWarnings("rawtypes")
	public HashMap getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(HashMap<String, Object> attributes) {
		this.attributes = attributes;
		id = (String) attributes.get("id");
	}

	/**
	 * @return the classNameType
	 */
	public String getClassNameType() {
		return classNameType;
	}

	/**
	 * @param classNameType
	 *            the classNameType to set
	 */
	public void setClassNameType(String classNameType) {
		this.classNameType = classNameType;
	}

	@SuppressWarnings("rawtypes")
	public String toString() {
		StringBuilder ciStringBuilder = new StringBuilder();
		ciStringBuilder.append("ClassNameType = " + classNameType + " ");
		Iterator it = null;
		if (attributes != null) {
			it = attributes.entrySet().iterator();
			hashMapPrinter(it, ciStringBuilder);
		}
		if (guid != null) {
			ciStringBuilder.append("Guid = " + guid.toString());
		}
		return ciStringBuilder.toString();
	}

	@SuppressWarnings("rawtypes")
	private void hashMapPrinter(Iterator it, StringBuilder ciStringBuilder) {
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ciStringBuilder.append(pairs.getKey() + " = " + pairs.getValue() + ",");
		}
	}

	public String getProperty(String prop) {
		String value = null;
		if (attributes != null) {
			value = (String) attributes.get(prop);
		}
		return value;
	}

}
