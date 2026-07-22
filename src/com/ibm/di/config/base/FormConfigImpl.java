/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.InvalidNameException;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.FormSection;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ValidatorConfig;

/**
 * Implementation of the configuration of a Form that can be displayed by the
 * Configuration Editor
 */
public class FormConfigImpl extends BaseConfigurationImpl implements FormConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -8761349695805705052L;

	private static final String FORM_TITLE = "title";
	private static final String FORM_USER_CODE = "uiclass";
	private static final String FORM_TABLIST = "tablist";
	private static final String FORM_SCRIPT = "formscript";

	private Hashtable<String, FormItemConfig> items = new Hashtable<String, FormItemConfig>();

	private Hashtable<String, FormSection> sections = new Hashtable<String, FormSection>();

	private TreeMap<String, TreeMap<?, ?>> formItems;

	private Vector<String> formOrder;

	private Vector<String> formSections;

	private String translationFileName;

	/**
	 * List of all available validators in the configuration.
	 */
	private List<ValidatorConfig> validators = null;

	private transient List<ResourceBundle> resbundles;

	private String locale;

	private transient ClassLoader resCL;

	public FormConfigImpl() {
		super();
		init();
	}

	public FormConfigImpl(Object obj) {
		super(obj);
		init();
	}

	@SuppressWarnings("unchecked")
	public void init() {
		if (formItems == null)
			formItems = (TreeMap<String, TreeMap<?, ?>>) getParameter("parameter", new TreeMap<String, TreeMap<?, ?>>());

		if (formOrder == null)
			formOrder = (Vector<String>) getParameter("parameterlist", new Vector<String>());

		if (formSections == null)
			formSections = (Vector<String>) getParameter("sections", new Vector<String>());

		if (validators == null) {
			validators = (List<ValidatorConfig>) getParameter("validators", new ArrayList<ValidatorConfig>());
		}
	}

	public List<String> getFormItemNames() {
		return formOrder;
	}

	public Iterator<String> getLocalFormItemNames() {
		return formItems.keySet().iterator();
	}

	public FormItemConfig getFormItem(String name) {
		// Check local cache
		FormItemConfig item = (FormItemConfig) items.get(name);
		if (item != null)
			return item;

		// Get data for attribute map item
		Object obj = formItems.get(name);
		if (obj == null) {
			// If we don't have and inherit from another formconfig try that one
			if (getInheritsFrom() == null && getInheritsFromRef() != null) {
				try {
					setupInheritanceChain();
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
			if (getInheritsFrom() instanceof FormConfig) {
				item = ((FormConfig) getInheritsFrom()).getFormItem(name);
				if (item != null)
					return item;
			}
			return null;
		}

		// Create instance
		item = new FormItemConfigImpl(obj);
		try {
			item.setName(MetamergeConfigFactory.parseName(name));
		} catch (InvalidNameException ine) {
		}
		item.setParent(this);
		item.setForm(this);

		// Put in cache
		items.put(name, item);

		return item;
	}

	public FormItemConfig newFormItem(String name) {

		if (!formOrder.contains(name))
			formOrder.add(name);

		// Check local cache
		FormItemConfig item = (FormItemConfig) items.get(name);
		if (item != null)
			return item;

		// Create instance
		item = new FormItemConfigImpl();
		try {
			item.setName(MetamergeConfigFactory.parseName(name));
		} catch (InvalidNameException ine) {
		}

		addFormItem(item);
		notifyChange(this, "newFormItem", MetamergeConfigChange.MCC_SET);
		return item;
	}

	public void addFormItem(FormItemConfig item) {

		String name = item.getShortName();
		item.setParent(this);
		item.setForm(this);

		// Put in cache
		items.put(name, item);

		formItems.put(name, item.getData());
	}

	public void addSection(FormSection section) {

		section.setParent(this);
		section.setForm(this);

		// Put in cache
		sections.put(section.getShortName(), section);

		setParameter(section.getShortName(), section.getData());
	}

	public void removeFormItem(String name) {
		formOrder.remove(name);
	}

	public void renameFormItem(String oldName, String newName) {
		int index = formOrder.indexOf(oldName);
		formOrder.remove(oldName);
		if (index >= 0)
			formOrder.add(index, newName);
		else
			formOrder.add(newName);

		FormItemConfig item = (FormItemConfig) items.remove(oldName);
		if (item != null)
			items.put(newName, item);

		TreeMap<?, ?> o = formItems.remove(oldName);
		if (o != null)
			formItems.put(newName, o);
	}

	public List<String> getSectionNames() {
		return formSections;
	}

	public FormSection getSection(String name) {
		// Check local cache
		FormSection item = (FormSection) sections.get(name);
		if (item != null)
			return item;

		// Get data for attribute map item
		Object obj = getParameter(name);
		if (!(obj instanceof TreeMap))
			return null;

		// Create instance
		item = new FormSectionImpl(obj);
		try {
			item.setName(MetamergeConfigFactory.parseName(name));
		} catch (InvalidNameException ine) {
		}
		item.setParent(this);
		item.setForm(this);

		// Put in cache
		sections.put(name, item);

		return item;
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		FormConfig form = new FormConfigImpl(deepClone(null));
		form.setName(getName());
		form.setMetamergeConfig(getMetamergeConfig());
		form.setTranslationFile(getTranslationFile());
		form.setTranslationClassLoader(getTranslationClassLoader());
		form.init();
		form.setupInheritanceChain();
		form.setModTS(getModTS());
		return form;
	}

	public String getFormEventHandler() {
		/*
		 * Formevents2 and Formevents3 are used when in the idi_base.inf file
		 * there are more than one script lines. The reason that we have more
		 * than one script lines is that CMVC does not allow checking in files
		 * with line(s) longer than 2048 symbols. Example:
		 * autogen/components/eventhandlers/DSMLv2EventHandler/idi_base.inf
		 */
		String param = getStringParameter(InternalSchema.FORM_EVENT_HANDLER);
		if (param == null)
			return null;
		StringBuilder ret = new StringBuilder(param);
		param = getStringParameter(InternalSchema.FORM_EVENT_HANDLER + "2");
		if (param != null)
			ret.append(param);
		param = getStringParameter(InternalSchema.FORM_EVENT_HANDLER + "3");
		if (param != null)
			ret.append(param);
		return ret.toString();
	}

	public String getFormScript() {
		return getStringParameter(FORM_SCRIPT);
	}

	public String getTitle() {
		String title = getStringParameter(FORM_TITLE);
		if (title == null)
			title = "";
		return translate(title);
	}

	public String getUIClass() {
		return getStringParameter(FORM_USER_CODE);
	}

	public int getWidth(int w) {
		return getIntegerParameter(InternalSchema.FORM_WIDTH, w);
	}

	public int getHeight(int h) {
		return getIntegerParameter(InternalSchema.FORM_HEIGTH, h);
	}

	public boolean getUseHyperLabel() {
		return getBooleanParameter(InternalSchema.FORM_USEHYPERLABELS, true);
	}

	public boolean getUseTabs() {
		return hasParameter(FORM_TABLIST);
	}

	public List<String> getTabNames() {
		Vector<String> v = new Vector<String>();
		String tabs = getStringParameter(FORM_TABLIST);
		if (tabs != null) {
			StringTokenizer st = new StringTokenizer(tabs, ",");
			while (st.hasMoreTokens())
				v.add(st.nextToken());
		}
		return v;
	}

	public String getTabTitle(String name) {
		return translate(getStringParameter(name + ".title"));
	}

	public String getTabToolTip(String name) {
		return translate(getStringParameter(name + ".tooltip"));
	}

	public String getTranslationFile() {
		return translationFileName;
	}

	public synchronized void setTranslationFile(String name) {
		translationFileName = name;
		resbundles = null;
	}

	public synchronized String translate(String str) {
		if (str == null)
			return str;

		if (resbundles == null && translationFileName != null) {
			String[] files = translationFileName.split(",");
			resbundles = new ArrayList<ResourceBundle>();
			for(int i = 0; i < files.length; i++) {
				ResourceBundle res = null;
				try {
					if (locale == null) {
						if (resCL != null) {
							res = ResourceBundle.getBundle(files[i], Locale.getDefault(), resCL);
						} else {
							res = ResourceBundle.getBundle(files[i]);
						}
					} else {
						if (resCL != null) {
							res = ResourceBundle.getBundle(files[i], getLocale(), resCL);
						} else {
							res = ResourceBundle.getBundle(files[i], getLocale());
						}
					}
				} catch (Exception e) {
					MetamergeConfigImpl.logger.error("FormConfigImpl.no.translation.found", files[i] + "_" + getLocale());
				}
				if(res != null)
					resbundles.add(res);
			}
		}
		
		if (resbundles == null)
			return str;

		for(ResourceBundle localRes: resbundles) {
			try {
				return localRes.getString(str);
			} catch (MissingResourceException mre) {
				try {
					return localRes.getString(str.trim());
				} catch (MissingResourceException mre2) {
					continue;
				} catch(NullPointerException npe) {
					continue;
				}
			}
		}
		
		return str;
	}
	
	private Locale getLocale() {
		String locale = this.locale;
		if(locale != null) {
			int index = locale.indexOf("_");
			if(index != -1) {
				return new Locale(locale.substring(0, index), locale.substring(index+1));
			} else {
				return new Locale(locale);
			}
		}
		return Locale.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.FormConfig#getTranslationClassLoader()
	 */
	public ClassLoader getTranslationClassLoader() {
		return resCL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.FormConfig#setTranslationClassLoader(java
	 * .lang.ClassLoader)
	 */
	public void setTranslationClassLoader(ClassLoader ldr) {
		resCL = ldr;
	}

	/**
	 * Sets the preferred Locale to use when translating strings
	 */
	public synchronized void setTranslationLocale(String locale) {
		this.locale = locale;
		// -- clear fields to resolve the ResourceBundle again
		resbundles = null;
	}

	/**
	 * Returns Locale identifier (ISO lang code) to use when translating
	 * resources.
	 */
	public String getTranslationLocale() {
		return locale;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ValidatorConfig> getValidators() {
		return validators;
	}

}