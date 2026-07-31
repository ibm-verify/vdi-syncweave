/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import java.util.Set;

/**
 * Default implementation used for representing Naming Rule
 */
public class DefaultNamingRule extends NamingRule {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            of the rule.
	 * @param priority
	 *            of the rule.
	 * @param identifiers
	 *            set of NamingRuleIdentifier.
	 */
	DefaultNamingRule(String name, int priority, Set<NamingRuleIdentifier> identifiers) {
		this.name = name;
		this.priority = priority;
		this.identifiers = identifiers;
	}

}