/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
import java.util.*;

import javax.naming.InvalidNameException;

/**
 * Implementation of one section in a {@link FormConfigImpl} 
 * @since 7.0
 */
public class FormSectionImpl extends BaseConfigurationImpl implements FormSection {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -8761349695805705054L;

	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String INITIALLY_EXPANDED = "initiallyExpanded";

	private Vector<String> names = null;

	private Hashtable<String,FormItemConfig> items = new Hashtable<String,FormItemConfig>();

	private TreeMap<String, TreeMap<?, ?>> formItems; // Used to save data for cloning

	private FormConfig form = null;

	public FormSectionImpl() {
		super();
		init();
	}

	public FormSectionImpl(Object obj) {
		super(obj);
		init();
	}

	@SuppressWarnings("unchecked")
	public void init() {
		if (names == null)
			names = (Vector<String>) getParameter("parameterlist", new Vector<String>());
		if (formItems == null)
			formItems = (TreeMap<String, TreeMap<?, ?>>) getParameter("formitemlist", new TreeMap<String, TreeMap<?, ?>>());
	}

	public Vector<String> getNames() {
		return names;
	}

	public void setNames( Vector<String> names ) {
		this.names = names;
	}

	public String getTitle() {
		return translate(getStringParameter(TITLE));
	}

	public void setTitle( String title ) {
		setParameter(TITLE, title);
	}

	public String getDescription() {
		return translate(getStringParameter(DESCRIPTION));
	}

	public void setDescription( String description ) {
		setParameter(DESCRIPTION, description);
	}

	public boolean initiallyExpanded() {
		return getBooleanParameter(INITIALLY_EXPANDED, true);
	}

	public void setInitiallyExpanded(boolean value) {
		setBooleanParameter(INITIALLY_EXPANDED, value);
	}

	public FormItemConfig getFormItem(String name) {
		// Check local cache
		FormItemConfig item = items.get(name);
		if (item != null)
			return item;

		// Get data for FormItem, if this is a clone
		Object obj = formItems.get(name);
		if (obj == null)
			return null;
		
		// Create instance
		item = new FormItemConfigImpl(obj);
		try {
			item.setName(MetamergeConfigFactory.parseName(name));
		} catch (InvalidNameException ine) {
			return null;
		}
		addFormItem(item);

		return item;

	}

	public void addFormItem(FormItemConfig item) {

		String name = item.getShortName();
		item.setParent(this);
		item.setForm(form);
		if ( getParent() instanceof FormConfig )
			item.setInheritsFrom(((FormConfig)getParent()).getFormItem(name));

		// Put in cache
		items.put(name, item);
		
		 //save away for cloning purposes
		formItems.put(name, item.getData());
	}

	public Collection<FormItemConfig> getFormItems() {
		return items.values();
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		FormSection form = new FormSectionImpl(deepClone(null));
		form.setName(getName());
		form.init();
		for (Enumeration<FormItemConfig> e = items.elements(); e.hasMoreElements(); ) {
			form.addFormItem( (FormItemConfig) e.nextElement().getClone() );
		}
		form.setMetamergeConfig(getMetamergeConfig());
		form.setupInheritanceChain();
		form.setModTS(getModTS());
		return form;
	}

	public void setForm(FormConfig form) {
		this.form = form;
		for (Enumeration<FormItemConfig> e = items.elements(); e.hasMoreElements(); ) {
			e.nextElement().setForm(form);
		}
	}

	/**
	 * Translate a String using the form's translate method 
	 */
	private String translate( String str ) {
		if ( form == null )
			return str;
		return form.translate( str );
	}
}
