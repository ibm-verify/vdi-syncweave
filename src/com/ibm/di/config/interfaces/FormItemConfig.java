/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * One item in a {@link FormConfig} 
 *
 */
public interface FormItemConfig extends BaseConfiguration {

	/**
	 * Get the syntax of this FormItem
	 */
	public String getSyntax();

	/**
	 * Set the syntax of this FormItem
	 * 
	 * @param str
	 *            The new syntax
	 */
	public void setSyntax(String str);

	/**
	 * Get the label for this FormItem
	 */
	public String getLabel();

	/**
	 * Set the label for this FormItem
	 * 
	 * @param str
	 *            The new label
	 */
	public void setLabel(String str);

	/**
	 * Get the ToolTip for this FormItem
	 */
	public String getToolTip();

	/**
	 * Set the ToolTip for this FormItem.
	 * 
	 * @param str
	 *            The new ToolTip
	 */
	public void setToolTip(String str);

	/**
	 * Get the default value for this FormItem. null means no default.
	 */
	public String getDefaultValue();

	/**
	 * Set the default value for this FormItem.
	 * 
	 * @param str
	 *            The new default value
	 */
	public void setDefaultValue(String str);

	/**
	 * Get the possible values for this FormItem.
	 */
	public List<String> getValues();

	/**
	 * Set the possible values for this FormItem.
	 * 
	 * @param values
	 *            The new values
	 */
	public void setValues(Vector values);

	/**
	 * Get the localized values for this FormItem. The values are the same
	 * length, and have the same order as getValues(). This is only useful for a
	 * droplist
	 * 
	 * @since 7.0
	 */
	public List<String> getLocalizedValues();

	/**
	 * Set the localized values for this FormItem.
	 * 
	 * @param map
	 *            maps from real values to localized Values.
	 * @since 7.0
	 */
	public void setLocalizedValues(Map map);

	/**
	 * Is this FormItem indexBased?
	 * 
	 * @since 7.0
	 */
	public boolean isIndexBased();

	/**
	 * Set whether this FormItem should be indexbased.
	 * 
	 * @param value
	 *            True if this FormItem should be indexbased.
	 */
	public void setIndexBased(boolean value);

	/**
	 * Get script to be executed for a button in this FormItem
	 */
	public String getScript();

	/**
	 * Get label for a button in this FormItem
	 */
	public String getScriptLabel();

	/**
	 * Get tooltip for a button in this FormItem
	 */
	public String getScriptToolTip();

	/**
	 * Get script to be executed for a 2nd button in this FormItem
	 * 
	 * @since 7.0
	 */
	public String getScript2();

	/**
	 * Get label for a 2nd button in this FormItem
	 * 
	 * @since 7.0
	 */
	public String getScriptLabel2();

	/**
	 * Get tooltip for a 2nd button in this FormItem
	 * 
	 * @since 7.0
	 */
	public String getScriptToolTip2();

	/**
	 * Is this FormItem readOnly?
	 * 
	 * @since 7.0
	 */
	public boolean isReadOnly();

	/**
	 * Return true if this FormItem should not be protected. That means that the
	 * value should be stored in cleartext in the config file, even if the
	 * syntax is "Password". The default value is false, that is, protect the
	 * value of passwords.
	 * 
	 * @since 7.0
	 */
	public boolean getDontProtect();

	/**
	 * Return true if this FormItem is an expression
	 * 
	 * @since 7.0
	 */
	public boolean isExpression();

	/**
	 * Return the name of the Component class that should be used for this
	 * formitem. Used if syntax = "COMPONENT"
	 * 
	 * @since 7.0
	 */
	public String getComponentClass();

	/**
	 * Return the name of the method to set/get this FormItem. set/get is
	 * supposed to be prefixed to the String to get the method.
	 * 
	 * @since 7.0
	 */
	public String getReflect();

	/**
	 * Return the minimun value for this FormItem. default is Integer.MIN_VALUE;
	 * 
	 * @since 7.0
	 */
	public int getMinValue();

	/**
	 * Return the maximun value for this FormItem. default is Integer.MAX_VALUE;
	 * 
	 * @since 7.0
	 */
	public int getMaxValue();

	/**
	 * Return the size of this FormItem, as number of characters If nothing
	 * specified, return 0.
	 * 
	 * @since 7.0
	 */
	public int getSize();

	/**
	 * Return true if this is a help item
	 * 
	 * @since 7.0
	 */
	public boolean isHelp();

	/**
	 * Return true if this FormItem is a required parameter
	 * 
	 * @since 7.0
	 * 
	 */
	public boolean isRequired();

	/**
	 * Set whether this FormItem is a required parameter
	 * 
	 * @param value
	 *            True if this FormItem is a required parameter
	 */
	public void setRequired(boolean value);

	/**
	 * Return the lead-in text, to be used in addition to the label
	 * 
	 * @since 7.0
	 */
	public String getLeadText();

	/**
	 * Set the lead-in text, to be used in addition to the label
	 * 
	 * @since 7.0
	 * @param text
	 *            The new lead-in text
	 */
	public void setLeadText(String text);

	/**
	 * Generic get
	 * 
	 * @since 7.0
	 */
	public Object get(String name);

	/**
	 * Set the FormConfig this FormItem is part of.
	 * 
	 * @param form
	 *            The FormConfig
	 */
	public void setForm(FormConfig form);

	/**
	 * Returns the FormConfig this FormItem is part of.
	 * @since 7.1.1
	 * 
	 */
	public FormConfig getForm();

	/**
	 * Returns whether this FormItem is valid for a particular mode.
	 * 
	 * @param mode
	 *            The mode
	 * @return true if the FormItem is valid for the mode.
	 * @since 7.0
	 */
	public boolean isValidForMode(String mode);
}
