/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This interface is used with the SolutionInterface to define exposed
 * properties.
 * 
 */
public interface ExposedProperty extends BaseConfiguration {

	/**
	 * Property name used to denote that all properties in a storename are not
	 * exposed
	 */
	public final static String DENY_ALL = "-";

	/**
	 * Property name used to denote that all properties in a storename are
	 * exposed
	 */
	public final static String PERMIT_ALL = "*";

	/**
	 * Returns the category for this property
	 * 
	 * @return the category
	 */
	public String getCategory();

	/**
	 * Returns the property name
	 * 
	 * @return property name
	 */
	public String getPropertyName();

	/**
	 * Returns the store name. A property may be exposed without a store name in
	 * which case it applies to the first store name with that property.
	 * 
	 * @return store name or null if not defined
	 */
	public String getStoreName();

	/**
	 * Optional label used by UI applications (use getUserComment() for
	 * tooltips)
	 * 
	 * @return tooltip
	 */
	public String getLabel();

	/**
	 * Changes the category for this property
	 * 
	 * @param category
	 */
	public void setCategory(String category);

	/**
	 * Sets the name of the exposed property
	 * 
	 * @param propertyName
	 *            The exposed property name
	 */
	public void setPropertyName(String propertyName);

	/**
	 * Sets the store name to which the exposed property applies. Can be null to
	 * denote the first property store with this name.
	 * 
	 * @param storeName
	 *            The name of the property store or null for any property store
	 */
	public void setStoreName(String storeName);

	/**
	 * Sets the label for this exposed property
	 * 
	 * @param label
	 *            Text used by UI applications
	 */
	public void setLabel(String label);

}
