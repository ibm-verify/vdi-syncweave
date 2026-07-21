/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import java.util.HashSet;
import java.util.Set;

import com.ibm.di.function.SystemFunctions;

/**
 * Class used for representing Naming Rules.
 * 
 */
public abstract class NamingRule implements Comparable<NamingRule> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default priority set to naming rules if no other can be found.
	 */
	public static final int UNDEFINED_PRIORITY = -1;

	/**
	 * Default name set to naming rules if no other can be found.
	 */
	public static final String UNDEFINED_NAME = "undefined";

	/**
	 * The rule's priority.
	 */
	protected int priority = UNDEFINED_PRIORITY;

	/**
	 * The rule's name.
	 */
	protected String name = UNDEFINED_NAME;

	/**
	 * The identifiers of this naming rule.
	 */
	protected Set<NamingRuleIdentifier> identifiers;

	/**
	 * Returns the identifiers of the Naming Rule.
	 * 
	 * @return rule's identifiers.
	 */
	public final Set<NamingRuleIdentifier> getIdentifiers() {
		return identifiers;
	}

	/**
	 * Returns the name of the Naming Rule.
	 * 
	 * @return rule's name.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the priority of the Naming Rule.
	 * 
	 * @return rule's priority.
	 */
	public final int getPriority() {
		return priority;
	}

	/**
	 * Checks whether this naming rule has any Naming Identifiers.
	 * 
	 * @return <code>true</code> if the rule is empty, otherwise
	 *         <code>false</code>.
	 */
	public final boolean isEmpty() {
		return identifiers.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(NamingRule other) {
		return getPriority() - other.getPriority();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("Naming rule: " + getName() + ", Priority=" + getPriority());
		for (NamingRuleIdentifier nri : getIdentifiers()) {
			builder.append(nri.toString());
		}
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof NamingRule) {
			NamingRule other = (NamingRule) obj;
			return getName().equals(other.getName()) //
					&& getIdentifiers().equals(other.getIdentifiers()) //
					&& (getPriority() == other.getPriority()) //
					&& (getIdentifiers().size() == other.getIdentifiers().size());

		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return name.hashCode() ^ priority;
	}

	/**
	 * Checks if the provided attributes match this naming rule.
	 * 
	 * @param attributes
	 *            the attributes to be checked.
	 * @return <code>true</code> if the attributes match this rule, otherwise
	 *         <code>false</code>.
	 */
	public boolean matches(Set<String> attributes) {
		Set<NamingRuleIdentifier> identifiers = getIdentifiers();
		boolean matches = true;
		for (NamingRuleIdentifier identifier : identifiers) {
			matches &= identifier.isRequired() == attributes.contains(identifier.getAttributeName());
			if (!matches) {
				break;
			}
		}
		return matches;
	}

	/**
	 * Checks if the provided attributes match this naming rule.
	 * 
	 * @param attributes
	 *            the attributes to be checked.
	 * @return NaminRule that contains unsatisfied NamingRuleIdentifiers.
	 */
	public NamingRule intersect(Set<NamingRuleIdentifier> attributes) {
		Set<NamingRuleIdentifier> namingIdentifiers = new HashSet<NamingRuleIdentifier>();
		try {
			for (NamingRuleIdentifier identifier : identifiers) {
				boolean matched = false;
				for (NamingRuleIdentifier currentAttribute : attributes) {
					matched = false;
					if (currentAttribute.isImplicit()) {
						matched = currentAttribute.getRelationshipType().equals(identifier.getRelationshipType()) //
								&& currentAttribute.getRelatedClass().equals(identifier.getRelatedClass()) //
								&& (currentAttribute.isRelationshipSource() == identifier.isRelationshipSource());
					} else {
						matched = currentAttribute.getAttributeName().equals(identifier.getAttributeName());
					}
					if (matched) {
						break;
					}
				}
				if (matched != identifier.isRequired()) {
					namingIdentifiers.add(new DefaultNamingRuleIdentifier(identifier.getAttributeName(), identifier
							.getRelationshipType(), identifier.getRelatedClass(), identifier.isRelationshipSource(), identifier
							.isRequired()));
				}
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return new DefaultNamingRule(name, priority, namingIdentifiers);
	}

}
