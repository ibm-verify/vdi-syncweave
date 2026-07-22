/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * The configuration for a single item in an AttributeMap.
 * @see AttributeMapConfig
 * 
 * @author bstadheim created 21. May 2002
 */
public interface AttributeMapItem extends BaseConfiguration {

	/**
	 * Simple mapping keyword
	 */
	public final static String SIMPLE_MAPPING = "simple";

	/**
	 * Advanced mapping keyword
	 */
	public final static String ADVANCED_MAPPING = "advanced";

	/**
	 * Substitution mapping keyword
	 */
	public final static String SUBSTITUTION_MAPPING = "substitution";

	/**
	 * Returns true if this attribute map item is enabled
	 * 
	 * @return The if enabled, false if disabled
	 */
	public boolean getEnabled();

	/**
	 * Sets the enabled attribute of the AttributeMapItem object
	 * 
	 * @param enabled
	 *            The new enabled value
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Gets the script attribute of the AttributeMapItem object
	 * 
	 * @return The script value
	 */
	public String getScript();

	/**
	 * Sets the script attribute of the AttributeMapItem object
	 * 
	 * @param script
	 *            The new script value
	 */
	public void setScript(String script);

	/**
	 * Gets the modify attribute of the AttributeMapItem object
	 * 
	 * @return The modify value
	 */
	public boolean getModify();

	/**
	 * Sets the modify attribute of the AttributeMapItem object
	 * 
	 * @param modify
	 *            The new modify value
	 */
	public void setModify(boolean modify);

	/**
	 * Gets the add attribute of the AttributeMapItem object
	 * 
	 * @return The add value
	 */
	public boolean getAdd();

	/**
	 * Sets the add attribute of the AttributeMapItem object
	 * 
	 * @param add
	 *            The new add value
	 */
	public void setAdd(boolean add);

	/**
	 * Gets the simple attribute of the AttributeMapItem object
	 * 
	 * @return The simple value
	 */
	public String getSimple();

	/**
	 * Sets the simple attribute of the AttributeMapItem object
	 * 
	 * @param attribute
	 *            The new simple value
	 */
	public void setSimple(String attribute);

	/**
	 * Sets the type attribute of the AttributeMapItem object
	 * 
	 * @param type
	 *            The new type value
	 */
	public void setType(String type);

	/**
	 * Gets the type attribute of the AttributeMapItem object
	 * 
	 * @return The type value
	 */
	public String getType();

	/**
	 * Returns true if this AttributeMapItem is a simple attribute map
	 * 
	 * @return The simple value
	 */
	public boolean isSimple();

	/**
	 * Returns true if this AttributeMapItem is an advanced attribute map
	 * (mapped by a script)
	 * 
	 * @return The advanced value
	 */
	public boolean isAdvanced();

	/**
	 * Returns true if this attribute map item is subject to property expansion
	 * 
	 * @return The if enabled, false if disabled
	 */
	public boolean isSubstitution();

	/**
	 * Sets the substitution template attribute of the AttributeMapItem object
	 * 
	 * @param template
	 *            The substitution template
	 */
	public void setSubstitution(String template);

	/**
	 * Gets the substitution template attribute of the AttributeMapItem object
	 * 
	 * @return The substitution template
	 */
	public String getSubstitution();

	/**
	 * Returns a list of child attribute map items.
	 * 
	 * @return List of child AttributeMapItem items
	 * @since 7.0
	 */
	public List getChildAttributeMaps();
	
}
