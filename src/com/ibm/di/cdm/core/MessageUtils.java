/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import com.ibm.di.server.ResourceHash;

/**
 * This class provides utility methods for composing TADDM validation messages.
 */
public class MessageUtils {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource hash.
	 */
	private static final ResourceHash resHash = ResourceHash.getHash("miserver");

	/**
	 * Create comma separated list of attributes.
	 * 
	 * @param rule
	 *            the rule that in not satisfied
	 * @return list of attributes.
	 */
	private static String getAttributeList(NamingRule rule) {
		StringBuilder attributesToAdd = new StringBuilder();
		StringBuilder attributesToRemove = new StringBuilder();
		for (NamingRuleIdentifier identifier : rule.getIdentifiers()) {
			if (identifier.isRequired()) {
				attributesToAdd.append("+");
				attributesToAdd.append(identifier.getAttributeName());
				attributesToAdd.append(", ");
			} else {
				attributesToRemove.append("-");
				attributesToRemove.append(identifier.getAttributeName());
				attributesToRemove.append(", ");
			}
		}
		String attributes = attributesToAdd.toString() + attributesToRemove.toString();
		if (attributes.length() > 1) {
			attributes = attributes.substring(0, attributes.length() - 2);
		}
		return attributes;
	}

	/**
	 * Return message when naming rule is not satisfied.
	 * 
	 * @param rule
	 *            the rule that is not satisfied.
	 * @return Unsatisfied message.
	 */
	public static String getUnsatisfiedMessage(NamingRule rule) {
		String message = null;
		String attributes = getAttributeList(rule);
		if ("undefined".equals(rule.getName())) {
			message = resHash.getString("CDM.VERIFY.RULE.ATTRIBUTES", new Object[] { rule.getPriority(), attributes });
		} else {
			message = resHash.getString("CDM.VERIFY.RULE.WITH.NAME.ATTRIBUTES", new Object[] { rule.getPriority(),
					rule.getName(), attributes });
		}
		return message;
	}

	/**
	 * Return message when naming rule is satisfied.
	 * 
	 * @param rule
	 *            the rule that is satisfied.
	 * @return Satisfied message.
	 */
	public static String getSatisfiedMessage(NamingRule rule) {
		String message = null;
		if ("undefined".equals(rule.getName())) {
			message = resHash.getString("CDM.VERIFY.RULE.SATISFIED", rule.getPriority());
		} else {
			message = resHash.getString("CDM.VERIFY.RULE.WITH.NAME.SATISFIED", new Object[] { rule.getPriority(),
					rule.getName() });
		}
		return message;
	}
}