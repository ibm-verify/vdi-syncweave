/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.ValidatorConfig;

/**
 * An implementation of {@link ValidatorConfig} that can be used in the form of
 * the Component.
 */
public class ValidatorConfigImpl extends BaseConfigurationImpl implements ValidatorConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 3577621338216299283L;

	/**
	 * {@inheritDoc}
	 */
	public String getValidatorClass() {
		return (String) getParameter(VALIDATOR_CLASS);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValidatorClass(String clazz) {
		setParameter(VALIDATOR_CLASS, clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType() {

		return (String) getParameter(VALIDATOR_TYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(String type) {
		setParameter(VALIDATOR_TYPE, type);
	}

}