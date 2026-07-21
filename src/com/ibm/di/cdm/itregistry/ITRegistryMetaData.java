/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.itregistry;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;
import com.ibm.di.cdm.jar.JarNamingRulesReader;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.tivoli.dataintegration.DataIntegrationServices;
import com.ibm.tivoli.dataintegration.common.IConstants;
import com.ibm.tivoli.namereconciliation.common.NrsApiException;

/**
 * This class is used to retrieve CDM meta-data from an IT registry.
 */
public class ITRegistryMetaData extends MetaData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Naming Context Prefix
	 */
	private static final String NAMING_CONTEXT_PREFIX = "nc:";

	/**
	 * The JDBC URL used for connecting to the IT registry.
	 */
	private String jdbcUrl;

	/**
	 * The user name used for connecting to the IT registry.
	 */
	private String dbUsername;

	/**
	 * The password used for connecting to the IT registry.
	 */
	private String dbPassword;

	/**
	 * Constructor.
	 * 
	 * @param jdbcUrl
	 *            the JDBC URL used for connecting to the IT registry.
	 * @param jdbcDriver
	 *            the JDBC driver for connecting to the IT registry.
	 * @param dbUsername
	 *            the user name used for connecting to the IT registry.
	 * @param dbPassword
	 *            the password used for connecting to the IT registry.
	 * @throws Exception
	 *             if the JDBC driver is unavailable.
	 */
	public ITRegistryMetaData(String jdbcUrl, String jdbcDriver, String dbUsername, String dbPassword) throws Exception {
		this.jdbcUrl = jdbcUrl;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;

		Class.forName(jdbcDriver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector<Entry> getAttributes(String artifactType, String classType) throws Exception {
		Vector<Entry> result = null;
		if (IdMLConstants.ARTIFACT_CI.equalsIgnoreCase(artifactType)) {
			result = getCiAttributes(classType);
		} else if (IdMLConstants.ARTIFACT_RELATIONSHIP.equalsIgnoreCase(artifactType)) {
			result = getRelationshipAttributes();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCdmVersion() throws Exception {
		String cdmVersion = null;
		Connection readConnection = null;
		DataIntegrationServices service = new DataIntegrationServices();
		try {
			readConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
			service.init(readConnection, null, null, null, null);
			cdmVersion = service.getModelLevel();
		} finally {
			if (readConnection != null && !readConnection.isClosed()) {
				readConnection.commit();
				readConnection.close();
			}
		}
		return cdmVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getTypes(String artifactType) throws Exception {
		Map<String, Object> result = null;
		if (IdMLConstants.ARTIFACT_CI.equalsIgnoreCase(artifactType)) {
			result = getCiTypes();
		} else if (IdMLConstants.ARTIFACT_RELATIONSHIP.equalsIgnoreCase(artifactType)) {
			result = getRelationshipTypes();
		}
		return result;
	}

	/**
	 * Retrieves the CDM attributes supported for a given CI type.
	 * 
	 * @param classType
	 *            the type of CI which attributes we need.
	 * @return a Vector of CDM attributes available for the given CI type.
	 * 
	 * @throws Exception
	 *             if a problem with the IT registry occurs.
	 */
	private Vector<Entry> getCiAttributes(String classType) throws Exception {
		final String attributeNameKey = "Name";
		final String attributeTypeKey = "Type";

		// get the class type without the 'cdm:' prefix
		String unprefixedClassType = null;
		if (classType.startsWith(CDM_PREFIX)) {
			unprefixedClassType = classType.substring(CDM_PREFIX.length());
		} else {
			unprefixedClassType = classType;
		}

		// get the short name of the CI class
		String shortClassType = null;
		int shortNameIndex = unprefixedClassType.lastIndexOf('.');
		if (shortNameIndex > -1) {
			shortClassType = unprefixedClassType.substring(shortNameIndex + 1);
		} else {
			shortClassType = unprefixedClassType;
		}

		Vector<Entry> classAttributes = new Vector<Entry>();
		// connect to the IT registry
		Connection readConnection = null;
		DataIntegrationServices service = new DataIntegrationServices();
		try {
			readConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
			service.init(readConnection, null, null, null, null);

			HashMap<?, ?>[] attributes = service.getAttributeTypes(IConstants.TYPE_CLASS_TYPE, shortClassType);
			for (HashMap<?, ?> outerMap : attributes) {
				for (java.util.Map.Entry<?, ?> key : outerMap.entrySet()) {
					HashMap<?, ?>[] maps = (HashMap[]) key.getValue();
					for (HashMap<?, ?> innerMap : maps) {
						Entry entry = new Entry();
						entry.addAttributeValue("name", CDM_PREFIX + innerMap.get(attributeNameKey));
						entry.addAttributeValue("syntax", innerMap.get(attributeTypeKey));
						classAttributes.add(entry);
					}
				}
			}
		} catch (NrsApiException nae) {
			throw new Exception(resHash.getString("CDM.META.DATA.NO.CLASS.FOUND", classType), nae);
		} finally {
			if (readConnection != null && !readConnection.isClosed()) {
				readConnection.commit();
				readConnection.close();
			}
		}

		return classAttributes;
	}

	/**
	 * Retrieves the Configuration Item CDM types.
	 * 
	 * @return a Map which keys are the available CDM CI types, and no
	 *         additional data is provided.
	 * @throws Exception
	 *             if a problem with the IT registry occurs.
	 */
	private Map<String, Object> getCiTypes() throws Exception {
		final String classNameKey = "LongName";
		Map<String, Object> ciTypes = new TreeMap<String, Object>();

		// connect to the IT registry
		Connection readConnection = null;
		DataIntegrationServices service = new DataIntegrationServices();
		try {
			readConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
			service.init(readConnection, null, null, null, null);

			HashMap<?, ?>[] types = service.getClassTypes((String) null);
			for (HashMap<?, ?> classTypeMap : types) {
				String classType = (String) classTypeMap.get(classNameKey);
				classType = CDM_PREFIX + classType.replace('/', '.');
				ciTypes.put(classType, null);
			}
		} finally {
			if (readConnection != null && !readConnection.isClosed()) {
				readConnection.commit();
				readConnection.close();
			}
		}

		return ciTypes;
	}

	/**
	 * Retrieves the Relationship CDM types.
	 * 
	 * @return a Map which keys are the CDM Relationship types and the
	 *         corresponding source and class types are stored in the Object
	 *         value.
	 * @throws Exception
	 *             if a problem with the IT registry occurs.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getRelationshipTypes() throws Exception {
		final String relationshipTypeKey = "RelationshipType";
		final String sourceClassString = "SourceClass";
		final String targetClassString = "TargetClass";

		Map<String, Object> relationshipTypes = new TreeMap<String, Object>();

		// connect to the IT registry
		Connection readConnection = null;
		DataIntegrationServices service = new DataIntegrationServices();
		try {
			readConnection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
			service.init(readConnection, null, null, null, null);

			HashMap<?, ?>[] types = service.getValidRelationships(null, null, null);
			for (HashMap<?, ?> relationshipTypeMap : types) {
				String relationshipTypeName = (String) relationshipTypeMap.get(relationshipTypeKey);
				relationshipTypeName = CDM_PREFIX + relationshipTypeName;

				Object holder = relationshipTypes.get(relationshipTypeName);
				if (holder == null) {
					ArrayList<TreeSet<Object>> endpointClasses = new ArrayList<TreeSet<Object>>(2);
					endpointClasses.add(new TreeSet<Object>());
					endpointClasses.add(new TreeSet<Object>());
					endpointClasses.get(0).add(relationshipTypeMap.get(sourceClassString));
					endpointClasses.get(1).add(relationshipTypeMap.get(targetClassString));
					relationshipTypes.put(relationshipTypeName, endpointClasses);
				} else {
					if (holder instanceof ArrayList<?>) {
						((ArrayList<TreeSet<Object>>) holder).get(0).add(relationshipTypeMap.get(sourceClassString));
						((ArrayList<TreeSet>) holder).get(1).add(relationshipTypeMap.get(targetClassString));
					}
				}
			}
		} finally {
			if (readConnection != null && !readConnection.isClosed()) {
				readConnection.commit();
				readConnection.close();
			}
		}
		return relationshipTypes;
	}

	/**
	 * FIXME: <p><b>Important:</b> Once the DIS team starts supporting Naming
	 * Rule-s via the {@link DataIntegrationServices} API this method should be
	 * extended to return their IT registry adapter classes.</p>
	 * 
	 * @see JarNamingRulesReader
	 * @see TADDMNamingRulesReader
	 */
	@Override
	protected List<NamingRule> getNamingRules(String classType) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getIdentifierName(NamingRuleIdentifier identifier) {
		String identifierName = CDMUtils.toUpperCaseFirstLetter(identifier.getAttributeName());
		if (identifier.isImplicit()) {
			identifierName = NAMING_CONTEXT_PREFIX + identifierName;
		} else {
			identifierName = super.getIdentifierName(identifier);
		}
		return identifierName;
	}
}