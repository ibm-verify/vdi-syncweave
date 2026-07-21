/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import com.ibm.di.config.interfaces.*;

/**
 * The old way of passing parameters on a call to an AssemblyLine.
 * The current way is to use a TaskCallBlock.
 * @see com.ibm.di.server.TaskCallBlock
 * @deprecated
 */
public class CallConfigImpl extends BaseConfigurationImpl implements CallConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -4697458497835329096L;

	/**
	 * A cache with call parameters.
	 */
	private Hashtable cache = new Hashtable();

	/**
	 * Default Constructor.
	 */
	public CallConfigImpl() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            TreeMap of attribute/value pairs
	 */
	public CallConfigImpl(Object config) {
		super(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getCallParameters() {
		return getKeys(BaseConfiguration.SUBTREE);
	}

	/**
	 * {@inheritDoc}
	 */
	public CallParamConfig getCallParameter(Object name) {
		CallParamConfig cp = (CallParamConfig) cache.get(name.toString());
		if (cp == null) {
			Object obj = getParameter(name);
			if (obj == null)
				return null;
			cp = new CallParamConfigImpl(getParameter(name));
			cp.setParent(this);
			cache.put(name.toString(), cp);
		}

		try {
			cp.setName(MetamergeConfigFactory.parseName(name));
			return cp;
		} catch (Exception error) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCallParameter(CallParamConfig param) {
		setParameter(param.getShortName(), param.getData());
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCallParameter(CallParamConfig param) {
		removeParameter(param.getShortName());
	}

	/**
	 * {@inheritDoc}
	 */
	public CallParamConfig newCallParameter(Object name) throws Exception {

		if (getParameter(name) != null)
			throw new javax.naming.NameAlreadyBoundException(name.toString());

		CallParamConfig cp = new CallParamConfigImpl(getParameter(name,
				new TreeMap()));
		cp.setParent(this);
		cp.setName(MetamergeConfigFactory.parseName(name));
		cache.put(name.toString(), cp);
		return cp;
	}
}
