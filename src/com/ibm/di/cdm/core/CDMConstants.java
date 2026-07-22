/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

/**
 * A set of constants used by the CDM-aware Components.
 */
public interface CDMConstants {
	/**
	 * Copyright.
	 */
	static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The package which acts as a root of all CDM classes in the JAR metadata.
	 */
	public static final String JAR_CDM_NAMEPACE = "com.ibm.dl.schema.cdm.";

	/**
	 * The package which acts as a root of all CDM classes in the TADDM metadata
	 * JAR.
	 */
	public static final String TADDM_CDM_NAMESPACE = "com.collation.platform.model.topology.";

	/**
	 * The name of the domain attribute used by TADDM Connector.
	 */
	public static final String TADDM_DOMAIN_ATTRIBUTE = "$domain";

	/**
	 * The name of the attribute containing MSS information.
	 */
	public static final String TADDM_MSS_ATTRIBUTE = "$mss";

	/**
	 * The name of the attribute which wraps all implicit attributes in TADDM's
	 * native model.
	 */
	public static final String TADDM_IMPLICIT_ATTRIBUTE = "$implicit";

	/**
	 * The name of the attribute which wraps all source attributes in TADDM's
	 * IdML model.
	 */
	public static final String TADDM_SOURCE_ATTRIBUTE = "$source";

	/**
	 * The name of the attribute which wraps all target attributes in TADDM's
	 * IdML model.
	 */
	public static final String TADDM_TARGET_ATTRIBUTE = "$target";

	/**
	 * The name of the TADDM source attribute.
	 */
	public static final String TADDM_SOURCE_NAME = "source";

	/**
	 * The name of the TADDM target attribute.
	 */
	public static final String TADDM_TARGET_NAME = "target";

	/**
	 * The prefix used by all explicitF attributes and classes in IdML mode.
	 */
	public static final String CDM_PREFIX = "cdm:";

	/**
	 * The prefix used by all implicit attributes in IdML mode.
	 */
	public static final String CDM_RELATIONSHIP_PREFIX = "cdm-rel:";

	/**
	 * The prefix used by the "related class" part of all implicit attributes in
	 * IdML mode. If signifies that the related class arcs as a SOURCE of the
	 * relationship.
	 */
	public static final String CDM_SOURCE_CI_PREFIX = "cdm-src:";

	/**
	 * The prefix used by the "related class" part of all implicit attributes in
	 * IdML mode. If signifies that the related class arcs as a TARGET of the
	 * relationship.
	 */
	public static final String CDM_TARGET_CI_PREFIX = "cdm-trg:";

	/**
	 * The prefix used for extended attributes in IdML mode.
	 */
	public static final String CDM_EXTENDED_ATTRIBUTE_PREFIX = "cdm-ext:";

	/**
	 * The prefix used for extended attributes in native mode.
	 */
	public static final String EXTENDED_ATTRIBUTE_PREFIX = "ext:";

	/**
	 * A system property added to each CDM item. It contains the class type of
	 * that item.
	 */
	public static final String CDM_CLASSTYPE_SYSTEM_ATTRIBUTE = "$classType";

	/**
	 * A system property added to each CDM item. It contains the item's ID
	 * (specific identifier for that system). For example, in TADDM it contains
	 * the item's GUID, for IdML books - its local ID attribute. This property
	 * is only applicable when reading CDM data.
	 */
	public static final String CDM_ID_SYSTEM_ATTRIBUTE = "$id";

	/**
	 * This system property is added only when the iterated CI is cyclic. An
	 * item is cyclic if it exists on more than one location on some path in the
	 * hierarchical CDM model. Through it, we avoid recursion.
	 */
	public static final String CDM_CYCLE_SYSTEM_ATTRIBUTE = "$cycle";

	/**
	 * This prefix is used for item attributes with special meaning in the
	 * hierarchical CDM model. It guarantees, that we will not have collisions
	 * with normal item attributes.
	 */
	public static final String SPECIAL_ATTRIBUTE_PREFIX = "$";
}
