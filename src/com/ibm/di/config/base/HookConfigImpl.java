/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.*;

/**
 * This is the implementation class for the configuration of a single Hook.
 */
public class HookConfigImpl extends BaseConfigurationImpl implements HookConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -1300997546910640256L;

	/**
	 * Constructor
	 */
	public HookConfigImpl() {
		super();
	}

	/**
	 * Constructor providing a TreeMap of attribute/value pairs.
	 * 
	 * @param config
	 *            initial config
	 */
	public HookConfigImpl(Object config) {
		super(config);
	}

	/**
	 * Constructor providing hook name and script. Creates enabled hook.
	 * 
	 * @param name
	 * @param script
	 */
	public HookConfigImpl(Object name, String script) {
		super();
		setHookName(name);
		setScript(script);
		setEnabled(true);
	}

	/**
	 * @return Hook name
	 */
	public Object getHookName() {
		return getParameter(InternalSchema.HC_NAME);
	}

	/**
	 * Sets hook name.
	 * 
	 * @param name
	 */
	public void setHookName(Object name) {
		setParameter(InternalSchema.HC_NAME, name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.HC_ENABLED, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		setBooleanParameter(InternalSchema.HC_ENABLED, enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getScript() {
		return getStringParameter(InternalSchema.HC_SCRIPT);
	}

	/**
	 * Sets the hook script.
	 * @param script
	 */
	public void setScript(String script) {
		setParameter(InternalSchema.HC_SCRIPT, script);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean couldInherit() {

		if (!(getInheritsFrom() instanceof HookConfig))
			return false;

		HookConfig inh = (HookConfig) getInheritsFrom();

		return inh.size() > 1 || inh.couldInherit();

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {

		boolean didFlatten = false;

		BaseConfiguration inheritFrom = getInheritsFrom();

		while (inheritFrom != null) {

			didFlatten = true;

			// Copy all simple values from inherited object
			List<String> simple = inheritFrom
					.getKeys(BaseConfiguration.ONE_LEVEL);
			for (int i = 0; i < simple.size(); i++) {
				String param = simple.get(i);
				if (!hasParameter(param))
					setParameter(param, inheritFrom.getParameter(param));
			}

			// Set inheritFrom to the next in the chain
			inheritFrom = inheritFrom.getInheritsFrom();
		}

		return didFlatten;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean detachFromParent() {
		if (getParent() instanceof HooksConfig) {
			((HooksConfig) getParent()).removeHook(getHookName());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean reattachToParent(int position) {
		if (getParent() instanceof HooksConfig) {
			((HooksConfig) getParent()).setHook(this);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof HooksConfig)
			super.setInheritsFrom(((HooksConfig) inheritFrom)
					.getHook(getHookName()));
		else
			super.setInheritsFrom(inheritFrom);
	}

 	@Override
	public void setupInheritanceChain() throws Exception {
		String inheritFrom = getInheritsFromRef();
		if (inheritFrom != null && !inheritFrom.equals(BaseConfiguration.INHERIT_PARENT)) {
			super.setupInheritanceChain();
		} else if (getParent() != null) {
			// set up inheritance from parent
			if (getParent().getInheritsFrom() instanceof HooksConfig && willUseInherited())
				setInheritsFrom(((HooksConfig) getParent().getInheritsFrom()).getHook(getHookName()));
			else
				setInheritsFrom(null);
		}
	}

	public String getShortName() {
		String str = super.getShortName();
		if(str != null)
			return str;
		else
			return ""+getHookName();
	}
}
