/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.ReconnectRuleConfig;

/**
 * This class implements reconnect rule configuration methods.
 */
public class ReconnectRuleConfigImpl extends BaseConfigurationImpl implements
		ReconnectRuleConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -3991188423937382066L;

	/** Exception class parameter. */
	public static final String EXCEPTION_CLASS_PARAM_NAME = "exceptionClass";

	/** Regular expression for exception messages parameter. */
	public static final String REGULAR_EXPRESSION_PARAM_NAME = "exceptionMessageRegExp";

	/** Reconnect action parameter. */
	public static final String ACTION_PARAM_NAME = "action";

	/**
	 * Create an empty reconnect rule configuration.
	 */
	public ReconnectRuleConfigImpl() {
		super();
	}

	/**
	 * Create a reconnect rule configuration from the specified raw
	 * configuration.
	 * 
	 * @param config
	 *            Raw configuration.
	 */
	public ReconnectRuleConfigImpl(Object config) {
		super(config);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAction() {
		return getStringParameter(ACTION_PARAM_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getExceptionClass() {
		return getStringParameter(EXCEPTION_CLASS_PARAM_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getExceptionMessageRegExp() {
		return getStringParameter(REGULAR_EXPRESSION_PARAM_NAME);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validate() throws Exception {

		// validate the exception class
		String exClass = getExceptionClass();
		if (exClass != null && exClass.trim().length() > 0) {
			try {
				Class.forName(exClass);
			} catch (ClassNotFoundException ex) {
				throw new Exception(
						super
								.getResHash()
								.getString(
										"MMCONFIG.RECONNECT.RULE.CONFIG.EXCEPTION.CLASS.NOT.FOUND",
										ex), ex);
			}
		}

		// validate the action
		String action = getAction();
		if ((action != null) && (action.trim().length() > 0)
				&& (!"error".equalsIgnoreCase(action))
				&& (!"reconnect".equalsIgnoreCase(action))) {
			throw new Exception(super.getResHash().getString(
					"MMCONFIG.RECONNECT.RULE.CONFIG.INVALID.ACTION", action));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getClone() throws Exception {

		ReconnectRuleConfig copy = new ReconnectRuleConfigImpl(deepClone(null));
		copy.setName(getName());
		copy.init();
		copy.setupInheritanceChain();
		copy.setModTS(getModTS());

		return copy;
	}

}
