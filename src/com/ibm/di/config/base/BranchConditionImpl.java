/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;

/**
 * Implementation of the configuration for a single condition in a BranchingConfig.
 */
public class BranchConditionImpl extends BaseConfigurationImpl implements
		BranchCondition {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -4091773233583817912L;

	/**
	 * Default Constructor
	 */
	public BranchConditionImpl() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 *            TreeMap of attribute/value pairs
	 */
	public BranchConditionImpl(Object data) {
		super(data);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLeftHand() {
		return getStringParameter(InternalSchema.BRANCH_CONDITION_LEFT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLeftHand(String str) {
		setStringParameter(InternalSchema.BRANCH_CONDITION_LEFT, str);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOperator() {
		return getStringParameter(InternalSchema.BRANCH_CONDITION_OPER);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOperator(String str) {
		setStringParameter(InternalSchema.BRANCH_CONDITION_OPER, str);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRightHand() {
		return getStringParameter(InternalSchema.BRANCH_CONDITION_RIGHT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRightHand(String str) {
		setStringParameter(InternalSchema.BRANCH_CONDITION_RIGHT, str);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getCaseSensitive() {
		return getBooleanParameter(
				InternalSchema.BRANCH_CONDITION_CASE_SENSITIVE, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCaseSensitive(boolean cs) {
		setBooleanParameter(InternalSchema.BRANCH_CONDITION_CASE_SENSITIVE, cs);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getNegate() {
		return getBooleanParameter(InternalSchema.BRANCH_CONDITION_NEGATE,
				false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNegate(boolean negate) {
		setBooleanParameter(InternalSchema.BRANCH_CONDITION_NEGATE, negate);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getMatchAny() {
		return getBooleanParameter("MatchAny", false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMatchAny(boolean matchAny) {
		setBooleanParameter("MatchAny", matchAny);
	}

}
