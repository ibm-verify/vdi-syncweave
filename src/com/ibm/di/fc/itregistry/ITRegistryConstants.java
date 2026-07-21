/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.itregistry;

/**
 * This class contains various constants used by the IT registry Components
 * (e.g. attribute names etc.).
 */
public class ITRegistryConstants {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This attribute is used to override the default book name set in the
	 * Components configuration panel.
	 */
	public static final String BOOK_NAME_ATTR = "$itRegistryBookName";

	/**
	 * This attribute is used in attribute mapping in CallReply mode of IT
	 * registry Connector
	 */
	public static final String ATTR_CLASS_TYPE = "ClassType";

	/**
	 * This attribute represents the Guid of Management Software System. Init IT
	 * registry FC provides this value.
	 */
	public static final String ATTR_MSS_GUID = "$mssGuid";

	/**
	 * This represents the GUID of a Managed Element which is registered in the
	 * IT registry. In case of relationship its value is null.
	 */
	public static final String ATTR_GUID = "$guid";

	/**
	 * This attribute should be present in attribute map for adding/deleting a
	 * relationship.
	 */
	public static final String ATTR_RELATIONSHIP_TYPE = "RelationshipType";

	/**
	 * This attribute should be present for deleting a relationship. It is also
	 * returned when reading relationships.
	 */
	public static final String RELATIONSHIP_SOURCE_GUID_ATTR = "SourceGuid";

	/**
	 * This attribute should be present for deleting a relationship. It is also
	 * returned when reading relationships.
	 */
	public static final String RELATIONSHIP_TARGET_GUID_ATTR = "TargetGuid";

	/**
	 * This attribute should be present for adding a relationship.
	 */
	public static final String RELATIONSHIP_TARGET_ATTR = "Target";

	/**
	 * This attribute can be used for filtering the returned Relationships by
	 * the target type.
	 */
	public static final String RELATIONSHIP_TARGET_CLASS_ATTR = "TargetCass";

	/**
	 * This attribute should be present for adding a relationship.
	 */
	public static final String RELATIONSHIP_SOURCE_ATTR = "Source";

	/**
	 * This attribute can be used for filtering the returned Relationships by
	 * the source type.
	 */
	public static final String RELATIONSHIP_SOURCE_CLASS_ATTR = "SourceCass";

	/**
	 * The String which denotes the details about MSS for fetched CI
	 */
	public static final String MANAGEMENT_SOFTWARE_SYSTEM = "ManagementSoftwareSystem";

	/**
	 * The String which denotes the identifying attributes of fetched CI
	 */
	public static final String IDENTIFYING_ATTRIBUTE = "IdentifyingAttribute";

	/**
	 * The String for Guid
	 */
	public static final String ATTR_OUTPUT_GUID = "Guid";

	/**
	 * The special character which acts as package separator for CDM Class or
	 * Relationship.
	 */
	public static final String PACKAGE_SEPARATOR = ".";
	/**
	 * The String for SourceToken
	 */
	public static final String SOURCE_TOKEN = "SourceToken";
	
	/**
	 * The String for Alias Guid Array
	 */
	public static final String ALIAS_GUID = "$aliasGuid";
	
	/**
	 * The internal value of Return Guid for Master.
	 */
	public static final String MASTER = "MASTER";
	
	/**
	 * The internal value for Return Guid for Aliases.
	 */
	public static final String ALIASES = "ALIASES";
	
	/**
	 * The internal value for Return Guid for Master and Aliases.
	 */
	public static final String MASTER_AND_ALIASES ="BOTH";

}
