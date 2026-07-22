/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import java.util.Map.Entry;

import javax.naming.*;
import com.ibm.di.config.interfaces.*;
/**
 * Implements the configuration for a Link Criteria in a Connector.
 *
 */
public class LinkCriteriaConfigImpl extends BaseConfigurationImpl implements
		LinkCriteriaConfig, MetamergeConfigChangeListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -9206856536172011821L;

	private Hashtable<String,LinkCriteriaItem> cache = new Hashtable<String,LinkCriteriaItem>();

	private BaseConfiguration children;

	// private Vector criteria;
	// private LinkCriteriaConfig inherit;

	public LinkCriteriaConfigImpl() {
		super();
		children = new BaseConfigurationImpl(getParameter(
				InternalSchema.CONNECTOR_LINK_CRITERIA, new TreeMap<String,Object>()));
		children.setParent(this);
	}

	public LinkCriteriaConfigImpl(Object config) {
		super(config);
		children = new BaseConfigurationImpl(getParameter(
				InternalSchema.CONNECTOR_LINK_CRITERIA, new TreeMap<String,Object>()));
		children.setParent(this);
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig)
			inheritFrom = ((ConnectorConfig) inheritFrom).getLinkCriteria();

		if (inheritFrom instanceof LinkCriteriaConfig) {
			LinkCriteriaConfig lcc = (LinkCriteriaConfig) inheritFrom;
			children.setInheritsFrom(lcc.getCriteria());
			for (Entry<String, LinkCriteriaItem> entry: cache.entrySet()) {
				if (lcc.getCriteria().getParameter(entry.getKey()) != null)
					entry.getValue().setInheritsFrom(lcc.getCriteria(entry.getKey()));
				else
					entry.getValue().setInheritsFrom(null);	
			}
			lcc.getCriteria().addListener(this);
		} else {
			children.setInheritsFrom(null);
			for (Entry<String, LinkCriteriaItem> item:cache.entrySet()) {
				item.getValue().setInheritsFrom(null);
			}
		}

		super.setInheritsFrom(inheritFrom);
	}

	public void configurationChanged(MetamergeConfigChange mcc) {
		performNotifyChange(mcc);
	}

	public BaseConfiguration getCriteria() {
		return children;
	}

	public List<String> getCriteriaNames() {
		return children.getKeys(BaseConfiguration.RECURSIVE_SUBTREE);
	}

	public LinkCriteriaItem getCriteria(Object name) {
		LinkCriteriaItem lci = cache.get(name.toString());
		if (lci != null)
			return lci;

		// Get data for attribute map item
		Object obj = children.getParameter(name.toString());
		if (obj == null)
			return null;

		// Create instance
		if (children.hasParameter(name)) {
			lci = new LinkCriteriaItemImpl(obj);
		} else {
			lci = new LinkCriteriaItemImpl();
			children.setParameter(name, lci.getData());
		}

		try {
			lci.setName(MetamergeConfigFactory.parseName(name));
		} catch (InvalidNameException ine) {
			return null;
		}

		if (getInheritsFrom() instanceof LinkCriteriaConfig && 
				((LinkCriteriaConfig) getInheritsFrom()).getCriteria().getParameter(name)!= null ) {
			lci.setInheritsFrom(((LinkCriteriaConfig) getInheritsFrom()).getCriteria(name));
		}

		// Put in cache
		cache.put(name.toString(), lci);
		lci.setParent(this);
		return lci;
	}

	public void setCriteria(LinkCriteriaItem item) {
		String name = item.getShortName();
		children.setParameter(name, item.getData());
		if (getInheritsFrom() instanceof LinkCriteriaConfig && 
				((LinkCriteriaConfig) getInheritsFrom()).getCriteria().getParameter(name)!= null ) {
			item.setInheritsFrom(((LinkCriteriaConfig) getInheritsFrom()).getCriteria(name));
		} else {
			item.setInheritsFrom(null);
		}

		cache.put(name, item);
		item.setParent(this);
	}

	public void removeCriteria(Object attribute) {
		if (attribute instanceof LinkCriteriaItem)
			attribute = ((LinkCriteriaItem) attribute).getShortName();
		children.removeParameter(attribute);
		cache.remove(attribute);
	}

	public LinkCriteriaItem newCriteria(Object name) throws Exception {

		String key;

		if (name == null) {
			long ts = new Date().getTime();
			while (children.getParameter(Long.toHexString(ts)) != null)
				ts++;
			key = Long.toHexString(ts);
		} else {
			key = name.toString();
		}

		if (children.hasParameter(key))
			throw new javax.naming.NameAlreadyBoundException(key);

		LinkCriteriaItem lci = new LinkCriteriaItemImpl();
		lci.setName(MetamergeConfigFactory.parseName(key));
		lci.setParent(this);
		children.setParameter(key, lci.getData());
		cache.put(key, lci);
		return lci;
	}

	public boolean isCriteriaLocal(Object name) {
		return children.isParameterLocal(name);
	}

	public String getAdvancedLinkCriteria() {
		return getStringParameter(InternalSchema.CONNECTOR_ADVANCED_LINK_CRITERIA);
	}

	public void setAdvancedLinkCriteria(String script) {
		setStringParameter(InternalSchema.CONNECTOR_ADVANCED_LINK_CRITERIA,
				script);
	}

	public boolean getAdvancedLinkMode() {
		return getBooleanParameter(InternalSchema.CONNECTOR_LINK_MODE, false);
	}

	public void setAdvancedLinkMode(boolean advanced) {
		setBooleanParameter(InternalSchema.CONNECTOR_LINK_MODE, advanced);
	}

	public boolean getMatchAny() {
		return getBooleanParameter(InternalSchema.CONNECTOR_LINK_OR, false);
	}

	public void setMatchAny(boolean value) {
		setBooleanParameter(InternalSchema.CONNECTOR_LINK_OR, value);
	}

	public boolean flatten(List<String> excludedNS) throws Exception {
		if (!super.flatten(excludedNS))
			return false;

		List<String> list = getCriteriaNames();
		for (int i = 0; i < list.size(); i++)
			getCriteria(list.get(i)).flatten(excludedNS);

		return true;
	}

	public List<Binding> search(String text, int options, int sizelimit, List<Binding> results) {
		List<String> list = getCriteriaNames();
		for (int i = 0; i < list.size(); i++)
			getCriteria(list.get(i)).search(text, options, sizelimit, results);
		return results;
	}

	public String toString() {
		String str = "LinkCriteria@" + hashCode();
		if (getParent() != null) {
			str += " in " + getParent().getName();
		}
		return str;
	}

}
