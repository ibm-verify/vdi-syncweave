/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import javax.naming.Name;

import com.ibm.di.config.interfaces.ExposedProperty;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;

/**
 * Implementation of ExposedProperty interface.
 * 
 * Note: This class overrides the getName() method to reflect the current
 * property and store values as defined by getPropertyName() and getStoreName()
 * to create a unique name.
 * 
 * 1. has property and store names --> "property.store" 2. has property but not
 * store name -> "property" 3. has store but not property -> "store"
 * 
 * @see com.ibm.di.config.interfaces.ExposedProperty
 */
public class ExposedPropertyImpl extends BaseConfigurationImpl implements
		ExposedProperty {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructors
	 */
	public ExposedPropertyImpl() {
		super();
	}

	public ExposedPropertyImpl(Object data) {
		super(data);
	}

	/**
	 * Overridden to dynamically generate name from store and property.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.BaseConfigurationImpl#getShortName()
	 */
	public String getShortName() {
		String name = getPropertyName();
		if (name == null)
			return null;
		String store = getStoreName();
		if (store != null)
			name += ":" + getStoreName();
		return name;
	}

	/**
	 * Overridden to dynamically generate name from store and property.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.BaseConfigurationImpl#getName()
	 */
	public Name getName() {
		try {
			return MetamergeConfigFactory.parseName(getShortName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#getCategory()
	 */
	public String getCategory() {
		return getStringParameter("category");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#getPropertyName()
	 */
	public String getPropertyName() {
		return getStringParameter("propertyName");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#getStoreName()
	 */
	public String getStoreName() {
		return getStringParameter("storeName");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#getLabel(java.lang.String)
	 */
	public String getLabel() {
		return getStringParameter("label");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#setCategory(java.lang.String)
	 */
	public void setCategory(String category) {
		setStringParameter("category", category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#setPropertyName(java.lang.String)
	 */
	public void setPropertyName(String propertyName) {
		setStringParameter("propertyName", propertyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#setStoreName(java.lang.String)
	 */
	public void setStoreName(String storeName) {
		setStringParameter("storeName", storeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.base.ExposedProperty#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		setStringParameter("label", label);
	}
}
