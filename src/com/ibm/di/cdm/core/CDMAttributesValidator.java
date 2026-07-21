/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.server.validate.ValidationException;
import com.ibm.di.server.validate.ValidationIssue;
import com.ibm.di.server.validate.Validator;

/**
 * Validate the output map of a CDM-related Component to verify mapped
 * attributes against CDM naming rules.
 */
public abstract class CDMAttributesValidator implements Validator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The configuration that contains information for validation.
	 */
	private BaseConfiguration config = null;

	/**
	 * {@inheritDoc}
	 */
	public void initialize(BaseConfiguration config) {
		this.config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ValidationIssue> validate() throws ValidationException {
		if (!(config instanceof ConnectorConfig)) {
			return new ArrayList<ValidationIssue>();
		}
		ConnectorConfig connConfig = (ConnectorConfig) config;

		List<NamingRule> namingRules = getUnsatisfiedNamingRules(connConfig);
		return processNamingRules(namingRules);
	}

	/**
	 * Compute unsatisfied naming rules.
	 * 
	 * @param config
	 *            the base configuration used to compute the difference.
	 * @return list of unsatisfied naming rules.
	 * @throws ValidationException
	 *             if validation error occurs.
	 */
	protected abstract List<NamingRule> getUnsatisfiedNamingRules(BaseConfiguration config) throws ValidationException;

	/**
	 * Return set of enabled attributes in the map.
	 * 
	 * @param mapConfig
	 *            the map configuration.
	 * @return set of enabled attributes.
	 */
	protected Set<String> getEnabledAttributes(AttributeMapConfig mapConfig) {
		Set<String> attributes = new HashSet<String>();
		List<String> attributesNames = mapConfig.getAttributeNames();
		for (String name : attributesNames) {
			if (mapConfig.getAttributeMapItem(name).getEnabled()) {
				attributes.add(name);
			}
		}
		return attributes;
	}

	/**
	 * Create list of validation issues.
	 * 
	 * @param namingRules
	 *            list of unsatisfied naming rules.
	 * @return list of validation issues.
	 */
	private List<ValidationIssue> processNamingRules(List<NamingRule> namingRules) {
		List<ValidationIssue> issues = new ArrayList<ValidationIssue>();
		int severity = getSeverityLevel(namingRules);

		for (NamingRule rule : namingRules) {
			String message = null;

			if (rule.getIdentifiers().size() > 0) {
				// not satisfied
				message = MessageUtils.getUnsatisfiedMessage(rule);
			} else {
				// all satisfied
				message = MessageUtils.getSatisfiedMessage(rule);
			}

			ValidationIssue issue = new ValidationIssue(severity, "", ((ConnectorConfig) config).getAttributeMap(), message);
			issues.add(issue);
		}
		return issues;
	}

	/**
	 * Determinate the severity level.
	 * 
	 * @param namingRules
	 *            unsatisfied naming rules.
	 * @return severity level.
	 */
	private int getSeverityLevel(List<NamingRule> namingRules) {
		int severity = VALIDATION_ERROR;
		for (NamingRule rule : namingRules) {
			if (rule.getIdentifiers().size() == 0) {
				severity = VALIDATION_INFO;
				break;
			}
		}
		return severity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {
	}

}