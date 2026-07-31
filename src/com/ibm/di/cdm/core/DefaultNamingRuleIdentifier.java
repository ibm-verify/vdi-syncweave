/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

/**
 * Default implementation used for representing Naming Rule Identifiers
 * (attributes taking part in a naming rule).
 * 
 */
public class DefaultNamingRuleIdentifier extends NamingRuleIdentifier {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 * 
	 * @param attributeName
	 *            the name of the identifier
	 */
	public DefaultNamingRuleIdentifier(String attributeName) {
		this.attributeName = attributeName;
		this.isRequired = true;
	}

	/**
	 * Constructor.
	 * 
	 * @param attributeName
	 *            the name of the identifier.
	 * @param relationshipType
	 *            the relationship type.
	 * @param relatedClass
	 *            the related class.
	 * @param isRelationshipSource
	 *            shows if the identifier is source.
	 * @param isRequired
	 *            shows if the identifier is required.
	 */
	public DefaultNamingRuleIdentifier(String attributeName, String relationshipType, String relatedClass,
			boolean isRelationshipSource, boolean isRequired) {
		this.attributeName = attributeName;
		this.relationshipType = relationshipType;
		this.relatedClass = relatedClass;
		this.isRelationshipSource = isRelationshipSource;
		this.isRequired = isRequired;
	}

}