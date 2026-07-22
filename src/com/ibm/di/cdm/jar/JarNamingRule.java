/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.jar;

import java.util.HashSet;

import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;

/**
 * The class for JAR Naming Rules.
 */
class JarNamingRule extends NamingRule {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            rule name.
	 * @param priority
	 *            rule priority.
	 */
	public JarNamingRule(String name, int priority) {
		identifiers = new HashSet<NamingRuleIdentifier>();
		this.name = name;
		this.priority = priority;
	}

	/**
	 * Adds a new identifier to the rule.
	 * 
	 * @param identifier
	 *            the new identifier.
	 */
	void addNamingIdentifier(NamingRuleIdentifier identifier) {
		identifiers.add(identifier);
	}

}
