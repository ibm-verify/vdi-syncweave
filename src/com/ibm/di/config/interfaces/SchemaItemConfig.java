/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 *  Describes an item from the schema
 *  @see SchemaConfig
 */
public interface SchemaItemConfig extends BaseConfiguration {
	
	public final static String PRESENCE_REQUIRED = "Required";
	public final static String PRESENCE_OPTIONAL = "Optional";
	
	/**
	 * Gets the attributeName attribute of the SchemaItemConfig object
	 *
	 * @return The attributeName value
	 */
	public String getAttributeName();

	/**
	 * Sets the attributeName attribute of the SchemaItemConfig object
	 *
	 * @param name The new attributeName value
	 */
	public void setAttributeName(String name);

	/**
	 * Gets the java class used internally for the value
	 *
	 * @return The java class name
	 */
	public String getJavaClass();

	/**
	 * Sets the java class name of the SchemaItemConfig object
	 *
	 * @param className The new java class value
	 */
	public void setJavaClass(String className);

	/**
	 * Gets the externalSyntax attribute of the SchemaItemConfig object
	 *
	 * @return The externalSyntax value
	 */
	public String getExternalSyntax();

	/**
	 * Sets the externalSyntax attribute of the SchemaItemConfig object
	 *
	 * @param syntax The new externalSyntax value
	 */
	public void setExternalSyntax(String syntax);

	/**
	 * Gets the presence flag of the SchemaItemConfig object. The presence flag indicates to the user
	 * whether it is required, optional.
	 *
	 * @return The presence value
	 */
	public String getPresenceFlag();

	/**
	 * Sets the presence flag of the SchemaItemConfig object
	 *
	 * @param  presence  The new presence value
	 */
	public void setPresenceFlag(String presence);

	/**
	 * Returns the sample value
	 */
	public Object getSample();

	/**
	 * Sets the sample value
	 */
	public void setSample(Object sample);
	
	/**
	 * Returns the required setting for this schema item
	 * @return true if this item is required
	 */
	public boolean isRequired();
	
	/**
	 * Returns the child schema
	 *  
	 * @since 7.0
	 */
	public ContainerConfig getChildSchemaList();
	
	/**
	 * Returns the minimum occurrences for this item.
	 * 
	 * @since 7.0
	 */
	public int getMinOccurrences();
	
	/**
	 * Sets the minimum occurrence for this item.
	 * 
	 * @param min
	 * @since 7.0
	 */
	public void setMinOccurrences(int min);
	
	/**
	 * Returns the minimum occurrences for this item.
	 * 
	 * @since 7.0
	 */
	public int getMaxOccurrences();
	
	/**
	 * Sets the max occurence for this schema item. -1 is unbound.
	 * 
	 * @param max
	 * @since 7.0
	 */
	public void setMaxOccurrences(int max);
	
	/**
	 * Returns true if this item is a property (e.g XML Element attribute)
	 * 
	 * @since 7.0
	 */
	public boolean isProperty();
	
	/**
	 * Sets the property flag for this item.
	 * 
	 * @since 7.0
	 */
	public void setProperty(boolean property);
	
	/**
	 * Returns true if this item is repeatable (e.g. max > 1)
	 * 
	 * @since 7.0
	 */
	public boolean isRepeatable();

	/**
	 * Returns true if this item is a leaf node
	 * 
	 * @since 7.0
	 */
	public boolean isLeaf();
}

