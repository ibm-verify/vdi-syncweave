/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.jar;

import com.ibm.di.cdm.core.NamingRuleIdentifier;

/**
 * The class for JAR Naming Rule Identifiers.
 */
class JarNamingRuleIdentifier extends NamingRuleIdentifier {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor for implicit attributes.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param isRequired
	 *            whether this attribute is required.
	 */
	public JarNamingRuleIdentifier(String attributeName, boolean isRequired) {
		this.attributeName = attributeName;
		this.isRequired = isRequired;
	}

	/**
	 * Constructor for explicit attributes.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param isRequired
	 *            whether this attribute is required.
	 * @param relationshipType
	 *            the relationship type represented by this implicit attribute.
	 * @param relatedClass
	 *            the type of the related item.
	 * @param isRelationshipSource
	 *            whether this attribute is the relationship's source.
	 */
	public JarNamingRuleIdentifier(String attributeName, boolean isRequired, String relationshipType,
			String relatedClass, boolean isRelationshipSource) {
		this(attributeName, isRequired);
		this.relationshipType = relationshipType;
		this.relatedClass = relatedClass;
		this.isRelationshipSource = isRelationshipSource;
	}

}