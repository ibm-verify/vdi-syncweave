/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * The old way of describing a parameter of a call to an AssemblyLine.
 * The current way is to use a TaskCallBlock.
 * @deprecated
 */
public class CallParamConfigImpl extends BaseConfigurationImpl implements
		CallParamConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 5788021154714741767L;

	/**
	 * Default Constructor.
	 */
	public CallParamConfigImpl() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            TreeMap of attribute/value pairs
	 */
	public CallParamConfigImpl(Object config) {
		super(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTargetAttributeName() {
		String str = getStringParameter(InternalSchema.TCB_ATTRIBUTE_TARGET);
		if (str == null)
			return getShortName();
		else
			return str;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetAttributeName(String targetAttributeName) {
		setStringParameter(InternalSchema.TCB_ATTRIBUTE_TARGET,
				targetAttributeName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSyntax(String value) {
		setParameter(InternalSchema.TCB_ATTRIBUTE_SYNTAX, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSyntax() {
		String str = getStringParameter(InternalSchema.TCB_ATTRIBUTE_SYNTAX);
		if (str == null)
			str = getStringParameter(InternalSchema.SCHEMA_EXTERNAL_SYNTAX);
		return str;
	}
}
