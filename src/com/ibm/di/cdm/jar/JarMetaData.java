/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.jar;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.jar.JarEntry;

import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;
import com.ibm.di.cdm.core.NamingRulesReader;
import com.ibm.di.cdm.core.NamingRulesReaderFactory;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.util.ResourceLocator;
import com.ibm.dl.core.certification.Utils;

/**
 * This class is used to retrieve CDM meta-data from a jar file.
 */
public class JarMetaData extends MetaData {

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
	 * The name of the package (in the jar) containing the CDM class types.
	 */
	private static final String CLASS_PACKAGE = "com.ibm.dl.schema.cdm";

	/**
	 * The name of the package (in the jar) containing the CDM relationship
	 * types.
	 */
	private static final String RELATIONSHIP_PACKAGE = "com.ibm.dl.schema.cdm.relationships";

	/**
	 * The class file extension.
	 */
	private static final String CLASS_EXTENTION = ".class";

	/**
	 * The name of the class file (in the jar) containing the CDM version
	 * information.
	 */
	private static final String VERSION_CLASS = "com.ibm.dl.schema.cdm.Version";

	/**
	 * The name of the Version attribute in the {@link #VERSION_CLASS} class. It
	 * is used to retrieve the CDM meta-data version.
	 */
	private static final String VERSION_ATTRIBUTE_NAME = "VERSION";

	/**
	 * The Naming Rule reader used for this meta-data.
	 */
	private NamingRulesReader reader;

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

		Class<?> verionClass = Class.forName(VERSION_CLASS);
		Field[] fields = verionClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (isStringConstant(field)) {
				String fieldName = field.getName();
				if (fieldName.equals(VERSION_ATTRIBUTE_NAME)) {
					cdmVersion = (String) field.get(null);
				}
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
	 *             if a problem occurs.
	 */
	private Vector<Entry> getCiAttributes(String classType) throws Exception {
		final String attributePrefix = "ATTR_";
		final String sourceTokenSuffix = "SourceToken";

		Vector<Entry> classAttributes = new Vector<Entry>();

		// get the class type without the 'cdm:' prefix
		String unprefixedClassType = null;
		if (classType.startsWith(CDM_PREFIX)) {
			unprefixedClassType = classType.substring(CDM_PREFIX.length());
		} else {
			unprefixedClassType = classType;
		}
		String modelClassName = Utils.convertCdmNameToDLSchemaName(unprefixedClassType);
		try {
			Class<?> modelClass = Class.forName(modelClassName);
			Field[] fields = modelClass.getFields();
			for (Field field : fields) {
				if (isStringConstant(field)) {
					String fieldName = field.getName();
					if (fieldName.startsWith(attributePrefix) && !fieldName.endsWith(sourceTokenSuffix)) {
						Entry entry = new Entry();
						entry.addAttributeValue("name", field.get(null));
						entry.addAttributeValue("syntax", field.getType().getName());
						classAttributes.add(entry);
					}
				}

			}
		} catch (ClassNotFoundException cnfe) {
			throw new Exception(resHash.getString("CDM.META.DATA.NO.CLASS.FOUND", classType), cnfe);
		}

		// when retrieving the CDM meta-data from the JAR file the
		// cdm:SourceToken attribute will not be returned, so we added
		Entry sourceToken = new Entry();
		sourceToken.addAttributeValue("name", IdMLConstants.ARTIFACT_SOURCE_TOKEN_ATTR);
		sourceToken.addAttributeValue("syntax", String.class.getName());
		classAttributes.add(sourceToken);

		return classAttributes;
	}

	/**
	 * Returns the name of a Configuration Item type given its class name in the
	 * jar file.
	 * 
	 * @param modelClassName
	 *            the full name of the class in the jar file.
	 * @return the CDM name of the CI.
	 */
	private String getCiType(String modelClassName) {
		final String classNamePrefix = "IDML_";
		final String classNamesFilter = ".IDML_";
		int packageIndex = modelClassName.indexOf(CLASS_PACKAGE);
		int classNameIndex = modelClassName.lastIndexOf(classNamePrefix);
		int extentionIndex = modelClassName.lastIndexOf(CLASS_EXTENTION);

		String classType = null;
		if (packageIndex > -1 && classNameIndex > packageIndex && extentionIndex > classNameIndex
				&& modelClassName.contains(classNamesFilter)) {
			classType = modelClassName.substring(packageIndex + CLASS_PACKAGE.length() + 1, classNameIndex)
					+ modelClassName.substring(classNameIndex + classNamePrefix.length(), extentionIndex);
		}
		return classType;
	}

	/**
	 * Retrieves the Configuration Item CDM types.
	 * 
	 * @return a Map which keys are the CDM CI types, but with no additional
	 *         info.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Map<String, Object> getCiTypes() throws Exception {
		Map<String, Object> classTypes = new TreeMap<String, Object>();

		JarURLConnection jarConn = getConnectionToJar(CLASS_PACKAGE);
		if (jarConn != null) {
			JarEntry jarEntry;
			Enumeration<JarEntry> jarEntries = jarConn.getJarFile().entries();
			while (jarEntries.hasMoreElements()) {
				jarEntry = jarEntries.nextElement();
				String jarEntryName = jarEntry.getName();
				jarEntryName = jarEntryName.replace('/', '.');
				String classType = getCiType(jarEntryName);
				if (classType != null) {
					classTypes.put(CDM_PREFIX + classType, null);
				}
			}
		}
		return classTypes;
	}

	/**
	 * Gets a connection to the jar file containing the CDM meta-data
	 * definitions. This connection permits to iterate the jar an an archive and
	 * retrieve its class names.
	 * 
	 * @param resourceName
	 *            the name of a package in the jar file.
	 * @return a connection to the jar file containing the passes resource.
	 * @throws IOException
	 *             if there is a problem to connect to the jar file.
	 */
	private JarURLConnection getConnectionToJar(String resourceName) throws IOException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		if (classLoader != null) {
			String resourcePath = resourceName.replace('.', '/');
			URL resource = ResourceLocator.getResourceURL(resourcePath);

			if (resource != null) {
				return (JarURLConnection) resource.openConnection();
			}
		}
		return null;
	}

	/**
	 * Returns the name of a Relationship type given its class name in the jar
	 * file.
	 * 
	 * @param relationshipClassName
	 *            the full name of the class in the jar file.
	 * @return the CDM name of the Relationship.
	 * 
	 * @throws IllegalAccessException
	 *             if the needed fields of the Relationship's class are
	 *             unaccessible.
	 * @throws ClassNotFoundException
	 *             if the given class is not found.
	 */
	private String getRelationshipType(String relationshipClassName) throws IllegalAccessException, ClassNotFoundException {
		final String attributePrefix = "RELATIONSHIP_";
		String relationshipType = null;

		Class<?> relationshipClass = Class.forName(relationshipClassName);
		Field[] fields = relationshipClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (isStringConstant(field)) {
				String fieldName = field.getName();
				if (fieldName.startsWith(attributePrefix)) {
					relationshipType = (String) field.get(null);
				}
			}
		}
		return relationshipType;
	}

	/**
	 * Retrieves the Relationship CDM types.
	 * 
	 * @return a Map which keys are the CDM Relationship types, and no
	 *         additional info is provided.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Map<String, Object> getRelationshipTypes() throws Exception {
		Map<String, Object> relationshipTypes = new TreeMap<String, Object>();
		JarURLConnection jarConn = getConnectionToJar(RELATIONSHIP_PACKAGE);
		if (jarConn != null) {
			JarEntry jarEntry;
			Enumeration<JarEntry> jarEntries = jarConn.getJarFile().entries();
			while (jarEntries.hasMoreElements()) {
				jarEntry = jarEntries.nextElement();
				String jarEntryName = jarEntry.getName();
				jarEntryName = jarEntryName.replace('/', '.');
				if (!jarEntry.isDirectory() && jarEntryName.startsWith(RELATIONSHIP_PACKAGE)) {
					int extentionIndex = jarEntryName.lastIndexOf(CLASS_EXTENTION);
					String relationshipType = getRelationshipType(jarEntryName.substring(0, extentionIndex));
					if (relationshipType != null) {
						relationshipTypes.put(relationshipType, null);
					}
				}
			}
		}

		return relationshipTypes;
	}

	/**
	 * Using reflection determines if a given field is string constant.
	 * 
	 * @param field
	 *            the field to check.
	 * @return <b>true</b> if the passes field is a String constant, otherwise
	 *         <b>false</b>.
	 */
	private boolean isStringConstant(Field field) {
		boolean result = false;
		int mods = field.getModifiers();
		Class<?> fieldType = field.getType();
		if (Modifier.isStatic(mods) && Modifier.isFinal(mods) && fieldType.equals(String.class)) {
			result = true;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<NamingRule> getNamingRules(String classType) throws Exception {
		if (reader == null) {
			reader = NamingRulesReaderFactory.getJarNamingRulesReader();
		}
		return reader.getAllNamingRules(classType);
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