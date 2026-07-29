/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InvalidNameException;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.DefaultConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;

/**
 * The implementation class for the configuration of an AttributeMap
 */
public class AttributeMapConfigImpl extends BaseConfigurationImpl implements
		AttributeMapConfig, MetamergeConfigChangeListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -2619015538178665684L;

	/**
	 * All attribute map items.
	 */
	private Map<String, AttributeMapItem> items = new TreeMap<String, AttributeMapItem>();

	/**
	 * Constructor
	 */
	public AttributeMapConfigImpl() {
		super();
	}

	/**
	 * Constructor providing a TreeMap of attribute/value pairs.
	 * 
	 * @param config
	 *            initial config
	 */
	public AttributeMapConfigImpl(Object config) {
		super(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();
		init();
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector. Also we need to set up inheritance for all the attribute map
	 * items.
	 * 
	 * @param inheritFrom
	 *            if this is {@link ConnectorConfig} object we take his
	 *            AttributeMap to inherit from else standard behavior.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig) {
			AttributeMapConfig amc = (AttributeMapConfig) ((ConnectorConfig) inheritFrom)
					.getAttributeMap(getShortName());
			super.setInheritsFrom(amc);
		} else {
			super.setInheritsFrom(inheritFrom);
		}

		synchronized(items) {
			// set up inheritance for all attribute map items
			for (Map.Entry<String, AttributeMapItem> e: items.entrySet()) {
				setItemInheritance(e.getValue(), e.getKey());
			}
		}
	}

	/**
	 * Set up inheritance.
	 * 
	 * @param ami
	 * @param name
	 */
	private void setItemInheritance(AttributeMapItem ami, String name) {
		String amiRef = ami.getInheritsFromRef();
		if (amiRef != null && !amiRef.equals(BaseConfiguration.INHERIT_PARENT)) {
			try {
				ami.setupInheritanceChain();
			} catch (Exception e) {
				MetamergeConfigImpl.logger.error(e.getMessage(), e);
			}
			return;
		}
		AttributeMapConfig inh = null;
		if (getInheritsFrom() instanceof AttributeMapConfig)
			inh = (AttributeMapConfig) getInheritsFrom();

		if (inh != null && willUseInherited() && inh.hasAttributeMapItem(name))
			ami.setInheritsFrom(inh.getAttributeMapItem(name));
		else
			ami.setInheritsFrom(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void configurationChanged(MetamergeConfigChange mcc) {
		if (mcc.getSource() instanceof AttributeMapConfig &&
				(mcc.getOperation() == MetamergeConfigChange.MCC_REMOVE ||
				 mcc.getOperation() == MetamergeConfigChange.END_CHANGES)) {
			batchChange = true; // this is not real changes...
			synchronized(items) {
				Iterator<Map.Entry<String,AttributeMapItem>> it = items.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String,AttributeMapItem> entry = it.next();
					if (hasParameter(entry.getKey()))
						setItemInheritance(entry.getValue(), entry.getKey());
					else
						it.remove();
				}
			}
			batchChange = false;
		}
		notifyChange(mcc);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapItem newAttributeMapItem(Object name) throws Exception {

		if (hasAttributeMapItem(name))
			throw new javax.naming.NameAlreadyBoundException(name.toString());

		MetamergeConfigFactory.parseName(name); // throw Exception if illegal
		// name
		AttributeMapItem ami = getAttributeMapItem(name);
		if (ami != null)
			notifyChange(this, ami.getShortName(),
					MetamergeConfigChange.MCC_ADD, ami);

		return ami;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttributeMapItem(AttributeMapItem map) {
		String name = map.getShortName();
		synchronized (this) {
			setParameter(name, map.getData(), false);
		}
		notifyChange(this, name, MetamergeConfigChange.MCC_SET);
		synchronized (items) {
			items.put(name, map);
		}

		map.setParent(this);
		setItemInheritance(map, name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAttributeMapItem(Object attribute) {
		Object name = attribute;
		if (attribute instanceof AttributeMapItem)
			name = ((AttributeMapItem) attribute).getShortName();

		synchronized (items) {
			items.remove(name);
		}
		removeParameter(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void renameAttributeMapItem(Object newName, AttributeMapItem map)
			throws Exception {

		if ((hasAttributeMapItem(newName))
				&& (map != getAttributeMapItem(newName))) {
			throw new javax.naming.NameAlreadyBoundException(newName.toString());
		}

		Object oldName = map.getShortName();
		map.flatten(new ArrayList<String>());
		removeAttributeMapItem(oldName);
		map.setName(MetamergeConfigFactory.simpleName(newName.toString()));
		setAttributeMapItem(map);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapItem getAttributeMapItem(Object attrName) {
		String attribute = attrName.toString();

		// Check local cache
		AttributeMapItem ami;
		synchronized (items) {
			ami = items.get(attribute);

			if (ami != null)
				return ami;

			// The name may have a different casing but still be in items
			for (Map.Entry<String, AttributeMapItem> e:items.entrySet()) {
				if (e.getKey().equalsIgnoreCase(attribute))
					return e.getValue();
			}
			
			// Get data for attribute map item
			Object obj = getParameterRaw(attribute);
			if (obj == null) {
				// Try to make sure we use correct casing if this is a local parameter.
				for (Iterator<String> i = getDataIterator(); i != null && i.hasNext();) {
					String key = i.next();
					if (attribute.equalsIgnoreCase(key)) {
						obj = getParameter(key);
						if (obj instanceof TreeMap) {
							attribute = key;
							break;
						} else {
							obj = null;
						}
					}
				}
			}

			// Get data for attribute map item
			if (obj == null)
				obj = new TreeMap<String, Object>();

			// Create instance
			ami = new AttributeMapItemImpl(obj);

			try {
				ami.setName(MetamergeConfigFactory.simpleName(attribute));
			} catch (InvalidNameException ine) {
				return null;
			}

			batchChange = true;
			ami.setParent(this);
			setItemInheritance(ami, attribute);
			batchChange = false;

			// Put in cache
			items.put(attribute, ami);
		}

		// If we inherit this attribute then we receive a clone of the
		// data and we need to add it to our local store when it changes.
		if (!hasParameter(attribute)) {
			final AttributeMapItem map = ami;
			map.addListener(new DefaultConfigChangeListener() {
				public void configurationChanged(MetamergeConfigChange mcc) {
					if (mcc.getSource() == map
							&& !"setInheritsFrom".equals(mcc.getUserObject())) {
						setAttributeMapItem(map);
						map.removeListener(this);
					}
				}
			});
		}

		return ami;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasAttributeMapItem(Object attribute) {
		for (Iterator<String> i = getDataIterator(); i != null && i.hasNext();) {
			String key = i.next();
			if (!key.equalsIgnoreCase(attribute.toString()))
				continue;
			Object o = getData().get(key);
			if (o instanceof TreeMap && ((TreeMap) o).size() > 1)
				return true;
		}

		// Check inherited object
		if (getInheritsFrom() instanceof AttributeMapConfig)
			return ((AttributeMapConfig) getInheritsFrom())
					.hasAttributeMapItem(attribute);

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getAttributeNames() {
		ArrayList<String> list = new ArrayList<String>();

		synchronized (this) {
			for (Iterator<String> i = getDataIterator(); i != null && i.hasNext();) {
				String key = i.next();
				Object o = getParameterRaw(key);
				if (o instanceof TreeMap && ((TreeMap) o).size() > 0)
					list.add(key);
			}
		}
		
		// Retrieve from inherited object
		if (getInheritsFrom() instanceof AttributeMapConfig) {
			List<String> plist = ((AttributeMapConfig) getInheritsFrom())
					.getAttributeNames();
			for (String key : plist) {
				if (!containsIC(list, key))
					list.add(key);
			}
		}

		return list;
	}

	/**
	 * Checks whether the provided String <code>s</code> is contained in the
	 * list of strings. The comparison ignores the case of the strings.
	 * 
	 * @param list
	 *            the list of strings to compare against.
	 * @param s
	 *            the string to search for.
	 * @return true if the provided string (or variation of some kind) is
	 *         contained in the list.
	 */
	private boolean containsIC(List<String> list, String s) {
		String str = s.toLowerCase(Locale.ENGLISH);
		for (String item:list)
			if (str.equals(item.toLowerCase(Locale.ENGLISH)))
				return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {
		if (!willFlatten(excludedNS))
			return false;

		for (String attr : getAttributeNames()) {
			getAttributeMapItem(attr).flatten(excludedNS);
		}

		return super.flatten(excludedNS);
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getChild(Object name) {
		if (hasAttributeMapItem(name))
			return getAttributeMapItem(name);
		else
			return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getChildNames() {
		return getAttributeNames();
	}
	
}
