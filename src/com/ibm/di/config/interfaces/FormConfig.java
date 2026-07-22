/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.Iterator;
import java.util.List;

/**
 * A Form that can be displayed by the Configuration Editor
 * 
 */
public interface FormConfig extends BaseConfiguration {

	/**
	 * Returns a list of the names of all the FormItems in this FormConfig. Some
	 * names may refer to global FormItems.
	 */
	public List<String> getFormItemNames();

	/**
	 * Returns an Iterator over names of FormItems defined in this FormConfig.
	 */
	public Iterator<String> getLocalFormItemNames();

	/**
	 * Get a FormItem defined by this FormConfig. May return null if the name is
	 * not defined in this FormConfig.
	 * 
	 * @param name
	 *            The name of the FormItem
	 */
	public FormItemConfig getFormItem(String name);

	/**
	 * Create a new FormItem with the given name
	 * 
	 * @param name
	 *            The name of the FormItem
	 */
	public FormItemConfig newFormItem(String name);

	/**
	 * Remove the FormItem with the given name
	 * 
	 * @param name
	 *            The name of the FormItem
	 */
	public void removeFormItem(String name);

	/**
	 * Rename a FormItem.
	 * 
	 * @param oldName
	 *            The old name of the FormItem
	 * @param newName
	 *            The new name of the FormItem
	 */
	public void renameFormItem(String oldName, String newName);

	/**
	 * Add a FormItem definition to this FormConfig
	 */
	public void addFormItem(FormItemConfig item);

	/**
	 * Get a list of the names of all Sections used by this FormConfig, if
	 * defined. The names would typically be FormSection names. A FormSection
	 * also contains names, which could either be FormSection names of FormItem
	 * names. FormSection names would refer to FormSections in this FormConfig,
	 * but FormItem names may refer to global FormItems.
	 */
	public List<String> getSectionNames();

	/**
	 * Get the FormSection with the given name.
	 * 
	 * @param name
	 *            The name of the FormSection
	 */
	public FormSection getSection(String name);

	/**
	 * Add a FormSection definition to this FormConfig
	 */
	public void addSection(FormSection section);

	/**
	 * /** Get the script that handles events in this FormConfig. This script is
	 * executed at once when the Form is displayed. Could be null.
	 */
	public String getFormEventHandler();

	/**
	 * Get the script that will be executed every time a button is pressed. Much
	 * like getFormEventHandler(), but this can be executed 0 or more times.
	 * Could be null.
	 */
	public String getFormScript();

	/**
	 * Get the title of this FormConfig.
	 */
	public String getTitle();

	/**
	 * Get the name of a UI class that will be used to display the form. Most of
	 * the other parameters will be ignored. This could be null, indicating no
	 * UI class is defined.
	 */
	public String getUIClass();

	/**
	 * Get the preferred width of this Form.
	 * 
	 * @param w
	 *            Use this width if no width defined
	 */
	public int getWidth(int w);

	/**
	 * Get the preferred height of this Form.
	 * 
	 * @param h
	 *            Use this height if no height defined.
	 */
	public int getHeight(int h);

	/**
	 * Return true if this FormConfig uses hyperlabels
	 */
	public boolean getUseHyperLabel();

	/**
	 * Return true if this form uses tabs.
	 */
	public boolean getUseTabs();

	/**
	 * Get names of tabs. These names refer to other FormConfig objects.
	 */
	public List<String> getTabNames();

	/**
	 * Get Title for one tab.
	 * 
	 * @param name
	 *            Name of the tab
	 */
	public String getTabTitle(String name);

	/**
	 * Get Tooltip for one tab.
	 * 
	 * @param name
	 *            Name of the tab
	 */
	public String getTabToolTip(String name);

	/**
	 * Get the name of the translation file. For internal use, to translate
	 * labels and so on.
	 */
	public String getTranslationFile();

	/**
	 * Set the name of the translation file. For internal use with
	 * ResourceBundle.
	 */
	public void setTranslationFile(String name);

	/**
	 * Translate a String.
	 * 
	 * @param str
	 *            The string to be translated
	 * @return The translated String, or the original String if no translation
	 *         was found.
	 */
	public String translate(String str);

	/**
	 * Sets the preferred Locale to use when translating strings. Setting this
	 * to null resets the FormConfig back to using the default locale; otherwise
	 * it overrides the default locale.
	 */
	public void setTranslationLocale(String locale);

	/**
	 * Returns Locale identifier (ISO lang code) to use when translating
	 * resources.
	 */
	public String getTranslationLocale();

	/**
	 * Sets the preferred {@link ClassLoader} able to resolve the translation
	 * file as a resource.
	 */
	public void setTranslationClassLoader(ClassLoader ldr);

	/**
	 * @return the {@link ClassLoader} used to resolve the translation file as a
	 *         resource. If not specified <code>null</code> is returned.
	 */
	public ClassLoader getTranslationClassLoader();

	/**
	 * Return list of all available validators in the configuration. In case of
	 * missing validators an empty list is returned.
	 * 
	 * @return list of validators configuration.
	 */
	public List<ValidatorConfig> getValidators();
}
