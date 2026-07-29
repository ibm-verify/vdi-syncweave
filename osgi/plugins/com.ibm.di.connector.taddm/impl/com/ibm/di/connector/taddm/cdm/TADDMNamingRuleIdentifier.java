/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm;

import com.collation.platform.model.AttributeNotSetException;
import com.collation.platform.model.topology.core.Relationship;
import com.collation.platform.model.topology.meta.NamingRuleAttribute;
import com.collation.platform.model.topology.meta.ObjectAttribute;
import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.cdm.core.NamingRuleIdentifier;

/**
 * An adapter wrapping the TADDM Naming Rule identifiers so they can match TDI's
 * unified model.
 */
class TADDMNamingRuleIdentifier extends NamingRuleIdentifier {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructor.
	 * 
	 * @param attribute
	 *            the TADDM Naming attribute.
	 */
	public TADDMNamingRuleIdentifier(NamingRuleAttribute attribute) {
		try {
			ObjectAttribute attributeMeta = attribute.getAttributeMeta();
			isRequired = attribute.hasEnabled() && attribute.getEnabled();
			attributeName = getAttributeName(attributeMeta);
			relatedClass = getClassType(attributeMeta);
			relationshipType = getRelationshipType(attributeMeta);
			isRelationshipSource = isRelationshipSource(attributeMeta);
		} catch (AttributeNotSetException anse) {
			// no meta data for the attribute is available
		}
	}

	/**
	 * Gets the attribute name from the {@link ObjectAttribute}.
	 * 
	 * @param attributeMeta
	 *            the attribute meta-data.
	 * @return the attrubute's name.
	 * @throws AttributeNotSetException
	 *             if the 'name' attribute is not set.
	 */
	private String getAttributeName(ObjectAttribute attributeMeta) throws AttributeNotSetException {
		String name = null;
		if (attributeMeta.hasName()) {
			name = CDMUtils.toUpperCaseFirstLetter(attributeMeta.getName());
		}
		return name;
	}

	/**
	 * Gets the related item's type.
	 * 
	 * @param attributeMeta
	 *            the attribute meta-data.
	 * @return the other items's name.
	 * @throws AttributeNotSetException
	 *             if the 'type' attribute is not set.
	 */
	private String getClassType(ObjectAttribute attributeMeta) throws AttributeNotSetException {
		String type = null;
		if (attributeMeta.hasType()) {
			type = attributeMeta.getType();
			int index = type.lastIndexOf(CDMConstants.TADDM_CDM_NAMESPACE);
			if (index >= 0) {
				type = type.substring(index + CDMConstants.TADDM_CDM_NAMESPACE.length());
			}
		}
		return type;
	}

	/**
	 * Gets the type of the relationship represented by this attribute.
	 * 
	 * @param attributeMeta
	 *            the attribute meta-data.
	 * @return the relationship's type.
	 * @throws AttributeNotSetException
	 *             if this attribute is not set.
	 */
	private String getRelationshipType(ObjectAttribute attributeMeta) throws AttributeNotSetException {
		String relnType = null;
		if (attributeMeta.hasRelationshipType()) {
			relnType = getValidRelationship(attributeMeta.getRelationshipType());
		}
		return relnType;
	}

	/**
	 * Gets a CDM-style relationship name.
	 * 
	 * @param relationship
	 *            the provided relationship.
	 * @return a CDM-style relationship name
	 */
	private String getValidRelationship(String relationship) {
		if (isRelationship(relationship)) {
			int index = relationship.lastIndexOf('.');
			if (index >= 0) {
				relationship = relationship.substring(index + 1);
			}
		}
		return CDMUtils.toLowercaseFirstLetter(relationship);
	}

	/**
	 * Checks if the provided type is a valid TADDM relationship.
	 * 
	 * @param relationship
	 *            the relationship type.
	 * @return <code>true</code> if it is a relationship, otherwise
	 *         <code>false</code>.
	 */
	private boolean isRelationship(String relationship) {
		boolean isRelationship = false;
		if (relationship != null && relationship.startsWith(CDMConstants.TADDM_CDM_NAMESPACE)) {
			try {
				Class<?> relationshipClass = Class.forName(relationship);
				isRelationship = Relationship.class.isAssignableFrom(relationshipClass);
			} catch (ClassNotFoundException cnfe) {
				// ignore, unknown class type
				isRelationship = false;
			}
		}
		return isRelationship;
	}

	/**
	 * Checks if this attribute is the source of the Relationship.
	 * 
	 * @param attributeMeta
	 *            the attribute meta-data.
	 * @return <code>true</code> if it is the source, otherwise
	 *         <code>false</code>.
	 * @throws AttributeNotSetException
	 *             if this attribute is not set.
	 */
	private boolean isRelationshipSource(ObjectAttribute attributeMeta) throws AttributeNotSetException {
		return attributeMeta.hasReverseRelationship() && !attributeMeta.getReverseRelationship();
	}

}
