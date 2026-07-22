/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

/**
 * This class contains various constants used by the IdML Components (e.g.
 * attribute names, configuration constants, etc.).
 */
public class IdMLConstants {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This attribute contains the id used when adding a CI to the IdML. The
	 * 'id' is a unique (at a given IdML file) identifier given to each created
	 * CI. It is used for creating the IdML Relationships.
	 */
	public static final String ID_ATTR = "$id";

	/**
	 * This attribute contains the CI's superior ID.
	 */
	public static final String SUPERIOR_ID_ATTR = "$superior";

	/**
	 * This attribute contains the CI's source contact info.
	 */
	public static final String SOURCE_CONTACT_INFO_ATTR = "$sourceContactInfo";

	/**
	 * This attribute contains the sourceToken attribute. The 'sourceToken'
	 * attribute is an unique identifier in the whole environment of the author
	 * of the IdML file (not only in the file itself). It can optionally be
	 * passed when creating a CI.
	 */
	public static final String ARTIFACT_SOURCE_TOKEN_ATTR = "cdm:SourceToken";

	/**
	 * One of the artifact types supported by the IdML Components. It denotes an
	 * IdML Configuration Item.
	 */
	public static final String ARTIFACT_CI = "CI";

	/**
	 * One of the artifact types supported by the IdML Components. It denotes an
	 * IdML Relationship.
	 */
	public static final String ARTIFACT_RELATIONSHIP = "RELATIONSHIP";

	/**
	 * This attribute is returned by the Close IdML FC and contains either the
	 * full path to the generated IdML file or its contents (if the in-memory
	 * option is used).
	 */
	public static final String BOOK_ATTR = "$idmlBook";

	/**
	 * This properties file provides default values for the parameters needed to
	 * connect to an IT registry.
	 */
	public static final String IT_REGISTRY_PROPERTIES_FILE = "etc/it_registry.properties";

	/**
	 * A prefix used by the properties in {@link #IT_REGISTRY_PROPERTIES_FILE}.
	 */
	public static final String IT_REGISTRY_PREFIX = "it_registry.";

	/**
	 * This attribute is used to override the default book name set in the
	 * Components configuration panel.
	 */
	public static final String BOOK_NAME_ATTR = "$idmlBookName";

	/**
	 * One of the mandatory attributes required when creating an IdML
	 * Relationship. It denotes the 'source' of the relationship.
	 */
	public static final String RELATIONSHIP_SOURCE_ATTR = "source";

	/**
	 * One of the mandatory attributes required when creating an IdML
	 * Relationship. It denotes the 'target' of the relationship.
	 */
	public static final String RELATIONSHIP_TARGET_ATTR = "target";

	/**
	 * The CDM namespace used in IdML files.
	 */
	public static final String CDM_NAMESPACE = "http://www.ibm.com/xmlns/swg/cdm";

	/**
	 * The IdML namespace used in IdML files.
	 */
	public static final String IDML_NAMESPACE = "http://www.ibm.com/xmlns/swg/idml";

	/**
	 * This enumeration contains the operations supported by the IDML schema.
	 * 
	 */
	public enum Operations {

		/**
		 * Create operation.
		 */
		CREATE,

		/**
		 * Modify operation.
		 */
		MODIFY,

		/**
		 * Delete operation.
		 */
		DELETE;

		/**
		 * This attribute is used to specify the operation of the created
		 * CI/Relationships. It accepts the above values.
		 */
		public static final String PARAM_NAME = "$operation";
	}

	/**
	 * A mask displayed instead of the actual password for the IT registry
	 * database.
	 */
	public static final String PASSWORD_MASK = "******";

}
