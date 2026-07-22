/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.*;
/**
 * This interface describes one section in a {@link FormConfig} 
 * @since 7.0
 */
public interface FormSection extends BaseConfiguration {

	/**
	 * Return the names of all FormSections or FormItems in the FormSection.
	 * Note: Modifying the Vector will modify the list in this FormSection.
	 */
	public Vector<String> getNames();

	/**
	 * Set the names of all FormSections or FormItems in the FormSection
	 * @param names The new list of names
	 */
	public void setNames( Vector<String> names );

	/**
	 * Get the title of this FormSection, or null if no title
	 */
	public String getTitle();

	/**
	 * Set the title of this FormSection.
	 * The new title should be a key to be looked up in the translation file
	 * @param title The new title
	 */
	public void setTitle( String title );

	/**
	 * Get the description of this FormSection, or null if no description
	 */
	public String getDescription();

	/**
	 * Set the description of this FormSection
	 * The new description should be a key to be looked up in the translation file
	 * @param description The new description
	 */
	public void setDescription( String description );

	/**
	 * Return true if this section is initially expanded
	 */
	public boolean initiallyExpanded();

	/**
	 * Set whether this section is initially expanded
	 * @param value true if the section is initially expanded
	 */
	public void setInitiallyExpanded(boolean value);

	/**
	 * Get a FormItem defined by this FormSection, or the enclosing FormConfig.
	 * May return null if the name is not defined.
	 * This may override or modify a FormItem defined in the FormConfig.
	 * @param name The name of the FormItem
	 */
	public FormItemConfig getFormItem(String name);

	/**
	 * Add a FormItem to this FormSection.
	 * This may override or modify a FormItem defined in the FormConfig.
	 * @param item The new FormItem
	 */
	public void addFormItem(FormItemConfig item);

	/**
	 * Returns a Collection of all locally defined FormItemConfigs.
	 * For internal use.
	 */
	public Collection<FormItemConfig> getFormItems();

	/**
	 * Set the FormConfig this FormSection is part of.
	 * @param form The FormConfig
	 */
	public void setForm(FormConfig form);
}
