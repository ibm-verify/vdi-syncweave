/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import com.ibm.di.config.interfaces.*;

/**
 * Implements the Logging Configuration for e.g. an AssemblyLine.
 */
public class LogConfigImpl extends BaseConfigurationImpl implements LogConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 3371411072185625170L;

	private List<LogConfigItem> items = new ArrayList<LogConfigItem>();

	private Vector<TreeMap<String,Object>> data;

	public LogConfigImpl() {
		super();
		init();
	}

	public LogConfigImpl(Object tm) {
		super(tm);
		init();
	}

	@SuppressWarnings("unchecked")
	public void init() {
		if (data == null) {
			data = (Vector) getParameter(InternalSchema.LOG_CONFIG,
					new Vector<TreeMap<String,Object>>());
			for (int i = 0; i < data.size(); i++) {
				LogConfigItem lci = new LogConfigItemImpl(data.get(i));
				items.add(lci);
				lci.setParent(this);
				if (lci.getInheritsFromRef() != null ) {
					try {
						lci.setupInheritanceChain();
					} catch (Exception e) {
						MetamergeConfigImpl.logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Adds an item to the config array
	 */
	@SuppressWarnings("unchecked")
	public void addItem(LogConfigItem item) {
		if (item.getInheritsFromRef() != null) {
			try {
				item.setupInheritanceChain();
			} catch (Exception e) {
				MetamergeConfigImpl.logger.error(e.getMessage(), e);
			}
		}
		items.add(item);
		item.setParent(this);
		notifyChange(this, "" + items.size(), MetamergeConfigChange.MCC_SET);
		data.add(item.getData());
	}

	/**
	 * Removes an item from the config array
	 */
	public void removeItem(int index) {
		items.remove(index);
		data.removeElementAt(index);
		notifyChange(this, "" + index, MetamergeConfigChange.MCC_REMOVE);
	}

	/**
	 * Returns a specific item from the config array
	 */
	public LogConfigItem getItem(int index) {
		return (LogConfigItem) items.get(index);
	}

	/**
	 * Returns a new item which is added to the internal list
	 */
	public LogConfigItem newItem() {
		LogConfigItem item = new LogConfigItemImpl();
		addItem(item);
		return item;
	}

	/**
	 * Returns a list of config objects
	 */
	public List<LogConfigItem> getItems() {
		return items;
	}

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		LogConfig lc = new LogConfigImpl(deepClone(null));
		lc.setName(getName());
		for (int i = 0; i < data.size(); i++) {
			lc.getItem(i).setName(getItem(i).getName());
		}

		lc.setMetamergeConfig(getMetamergeConfig());

		for (int i = 0; i < data.size(); i++) {
			LogConfigItem lci = lc.getItem(i);
			if (lci.getInheritsFromRef() != null )
				lci.setupInheritanceChain();
		}
		lc.setModTS(getModTS());

		return lc;
	}
}
