/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.*;

/**
 * The configuration for an AttributeMap (used in components of the AssemblyLine).
 * 
 * @author bstadheim created 21.May 2002
 */
public interface AttributeMapConfig extends BaseConfiguration {

	/**
	 * Sets a attributeMapItem attribute of the AttributeMapConfig object
	 * 
	 * @param map
	 *            The new AttributeMapItem
	 */
	public void setAttributeMapItem(AttributeMapItem map);

	/**
	 * Removes a named attribute from this map.
	 * 
	 * @param attribute
	 *            The attribute name
	 */
	public void removeAttributeMapItem(Object attribute);

	/**
	 * Returns the AttributeMapItem for attribute
	 * 
	 * @param attribute
	 *            The attribute name
	 * @return The attributeMapItem value
	 */
	public AttributeMapItem getAttributeMapItem(Object attribute);

	/**
	 * Checks if a named attribute is local (not inherited or null) to this
	 * object.
	 * 
	 * @param attribute
	 *            The attribute name
	 * @return True if this object contains the attribute
	 */
	public boolean hasAttributeMapItem(Object attribute);

	/**
	 * Creates and adds a new attribute map item to this object.
	 * 
	 * @param name
	 *            The attribute name
	 * @return The newly created AttributeMapItem
	 * @exception Exception
	 *                Any errors encountered by the underlying driver
	 */
	public AttributeMapItem newAttributeMapItem(Object name) throws Exception;

	/**
	 * Returns a list of attribute names in this object.
	 * 
	 * @return The list of attribute names including inherited attributes
	 */
	public List<String> getAttributeNames();

	/**
	 * Changes the name of an existing attributemapitem
	 * 
	 * @param newName
	 *            The new name
	 * @param map
	 *            The existing AttributeMapItem
	 * @exception Exception
	 *                Any errors encountered by the underlying driver
	 */
	public void renameAttributeMapItem(Object newName, AttributeMapItem map)
			throws Exception;
}
