/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;

import javax.naming.Binding;

import com.ibm.di.config.interfaces.*;
/**
 * Implements the configuration for all the Hooks e.g. in a Connector or AssemblyLine.
 *
 */
public class HooksConfigImpl extends AttributeMapConfigImpl implements
		HooksConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -9160883008989377612L;

	private Hashtable<String, HookConfig> items = new Hashtable<String, HookConfig>();

	public HooksConfigImpl() {
		super();
	}

	public HooksConfigImpl(Object config) {
		super(config);
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig)
			super.setInheritsFrom(((ConnectorConfig) inheritFrom).getHooks());
		else
			super.setInheritsFrom(inheritFrom);

		// set up inheritance for all hooks
		for (Enumeration<String> e = items.keys(); e.hasMoreElements();) {
			String name = e.nextElement();
			HookConfig hook = items.get(name);
			if ( hook.getInheritsFromRef() != null &&
					! hook.getInheritsFromRef().equals(BaseConfiguration.INHERIT_PARENT) )
				continue;
			hook.setInheritsFrom(getInheritsFrom());
		}
	}

	// Enable/Disable
	public synchronized  HookConfig getHook(Object o) {
		String name = o.toString();
		// Cached entry?
		HookConfig hook = items.get(name);
		if (hook != null) {
			return hook;
		}

		// Create hook object
		Object obj = getParameterRaw(name);
		if (obj == null) {
			obj = new TreeMap<String,Object>();
			setParameter(name, obj, false);
		} else if (!(obj instanceof TreeMap)) {
			// Something is wrong, the best we can do is probably to use an empty TreeMap
			obj = new TreeMap<String,Object>();
		}
		hook = new HookConfigImpl(obj);
		hook.setHookName(name);

		// set up inheritance
		setRefs(hook, name);
		
		return hook;
	}

	public synchronized void setHook(HookConfig hook) {
		String name = (String) hook.getHookName();
		setParameter(name, hook.getData(), false);
		setRefs(hook, name);
		
		notifyChange(this, name, MetamergeConfigChange.MCC_SET);
	}

	private void setRefs(HookConfig hook, String name) {
		if (hook.getInheritsFromRef() == null || 
				hook.getInheritsFromRef().equals(BaseConfiguration.INHERIT_PARENT) ) {
			// set up inheritance
			if (getInheritsFrom() instanceof HooksConfig && willUseInherited())
				hook.setInheritsFrom(((HooksConfig) getInheritsFrom()).getHook(name));
			else
				hook.setInheritsFrom(null);
			hook.setParent(this);
		} else {
			hook.setParent(this);
			try {
				hook.setupInheritanceChain();
			} catch (Exception e) {
				MetamergeConfigImpl.logger.error(e.getMessage(), e);
			}
		}

		items.put(name, hook);
	}
	
	public synchronized void removeHook(Object name) {
		items.remove(name);
		removeParameter(name);
	}

	/*
	 * Could this hook inherit data from some other place, if it did not have
	 * local data? @param name Name of Hook @return True if there is data that
	 * could be inherited
	 */
	public boolean couldInherit(String name) {
		return getHook(name).couldInherit();
	}

	/**
	 * flatten - combines all values from this object and its inherited objects
	 * into one single config object. After flattening, the object is a complete
	 * object with no inherited values except those from the excludedNS list.
	 * 
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {
		if (!super.flatten(excludedNS))
			return false;
		for (String key: getKeys(BaseConfiguration.RECURSIVE_SUBTREE))
			getHook(key).flatten(excludedNS);
		return true;
	}

	public List<Binding> search(String text, int options, int sizelimit,  List<Binding> results) {
		results = super.search(text, options, sizelimit, results);
		for (String key: getKeys(BaseConfiguration.RECURSIVE_SUBTREE))
			getHook(key).search(text, options, sizelimit, results);
		return results;
	}
	
	/**
	 * Returns a list of enabled hooks for this configuration.
	 * 
	 * @since 7.0
	 */
	public ArrayList<HookConfig> getActiveHooks() {
		List<String> list = getKeys(BaseConfiguration.RECURSIVE_SUBTREE);
		ArrayList<HookConfig> arr = new ArrayList<HookConfig>();
		for(String str : list ) {
			HookConfig hc = getHook(str);
			if(hc.getEnabled())
				arr.add(hc);
		}
		return arr;
	}

	/**
	 * Returns a hook or optionally creates it
	 * 
	 * @since 7.0
	 */
	public synchronized HookConfig getHook (Object name, boolean create) {
		if(items.containsKey(name) || create || getParameter(name) != null)
			return getHook(name);
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getChild(Object name) {
		return getHook(name, false);
	}
	
	@Override
	public synchronized List<String> getKeys(int level) {
		return super.getKeys(level);
	}
}
