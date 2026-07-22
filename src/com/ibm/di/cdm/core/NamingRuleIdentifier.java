/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

/**
 * Class used for representing Naming Rule Identifiers (attributes taking part
 * in a naming rule).
 * 
 */
public abstract class NamingRuleIdentifier {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the attribute.
	 */
	protected String attributeName;

	/**
	 * The class type of the item which this attribute links to. Only relevant
	 * for implicit attributes.
	 */
	protected String relatedClass;

	/**
	 * The type of the relationship represented by this attribute. Only relevant
	 * for implicit attributes.
	 */
	protected String relationshipType;

	/**
	 * The role of this attribute in the relationship it represent. It can be
	 * either the source or the target. Only relevant for implicit attributes.
	 */
	protected boolean isRelationshipSource;

	/**
	 * Whether this attribute is needed by the rule. If <code>true</code> this
	 * attribute must be present for the rule to match. On the other hand, if
	 * <code>false</code>, this attribute must not be present.
	 */
	protected boolean isRequired;

	/**
	 * Returns the attribute name.
	 * 
	 * @return name.
	 */
	public final String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the class type of the item which this attribute links to.
	 * 
	 * @return related class type.
	 */
	public final String getRelatedClass() {
		return relatedClass;
	}

	/**
	 * Returns the type of the relationship represented by this attribute.
	 * 
	 * @return relationship type.
	 */
	public final String getRelationshipType() {
		return relationshipType;
	}

	/**
	 * Changes the attribute name.
	 * 
	 * @param name
	 *            new name.
	 */
	final void setAttributeName(String name) {
		attributeName = name;
	}

	/**
	 * Returns if this attribute represents the source of a relationship.
	 * 
	 * @return <code>true</code>if this attribute is implicit and source of the
	 *         relationship, otherwise <code>false</code>.
	 */
	public final boolean isRelationshipSource() {
		return isRelationshipSource;
	}

	/**
	 * Returns if this attribute is required.
	 * 
	 * @return <code>true</code>if this attribute required, otherwise
	 *         <code>false</code>.
	 */
	public final boolean isRequired() {
		return isRequired;
	}

	/**
	 * Returns whether this attribute is implicit or not.
	 * 
	 * @return <code>true</code>if this attribute is implicit, otherwise
	 *         <code>false</code>.
	 */
	public final boolean isImplicit() {
		return relationshipType != null && relatedClass != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("Attribute=" + getAttributeName() + ", Required=" + isRequired());
		if (isImplicit()) {
			builder.append(", Relationship=" + getRelationshipType());
			builder.append(", IsSource=" + isRelationshipSource());
			builder.append(", RelatedClass=" + getRelatedClass());
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
		boolean equal = false;
		if (obj instanceof NamingRuleIdentifier) {
			NamingRuleIdentifier other = (NamingRuleIdentifier) obj;
			if (isImplicit()) {
				equal = other.isImplicit() //
						&& getRelationshipType().equals(other.getRelationshipType()) //
						&& getRelatedClass().equals(other.getRelatedClass()) //
						&& (isRelationshipSource() == other.isRelationshipSource()) //
						&& (isRequired() == other.isRequired());
			} else {
				equal = !other.isImplicit() // 
						&& getAttributeName().equals(other.getAttributeName()) // 
						&& (isRequired() == other.isRequired());
			}
		}
		return equal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (isImplicit()) {
			return relationshipType.hashCode() ^ relatedClass.hashCode();
		}
		return attributeName.hashCode();
	}
}
