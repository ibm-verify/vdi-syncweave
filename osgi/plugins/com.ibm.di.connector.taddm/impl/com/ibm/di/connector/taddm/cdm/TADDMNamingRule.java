/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm;

import java.util.HashSet;

import com.collation.platform.model.AttributeNotSetException;
import com.collation.platform.model.topology.meta.NamingRuleAttribute;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;

/**
 * An adapter used for converting TADDM Naming Rules to TDI's model..
 */
class TADDMNamingRule extends NamingRule {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            the TADDM style naming rule.
	 */
	public TADDMNamingRule(com.collation.platform.model.topology.meta.NamingRule rule) {
		identifiers = new HashSet<NamingRuleIdentifier>();
		try {
			if (rule.hasNamingRuleAttributes()) {
				NamingRuleAttribute[] attributes = rule.getNamingRuleAttributes();
				for (NamingRuleAttribute attribute : attributes) {
					identifiers.add(new TADDMNamingRuleIdentifier(attribute));
				}
			}
		} catch (AttributeNotSetException anse) {
			// no naming attributes are present
		}
		try {
			if (rule.hasRulePriority()) {
				priority = rule.getRulePriority();
			}
		} catch (AttributeNotSetException anse) {
			// priority attribute not defined
			priority = UNDEFINED_PRIORITY;
		}
	}

}
