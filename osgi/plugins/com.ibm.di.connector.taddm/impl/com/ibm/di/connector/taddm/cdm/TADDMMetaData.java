/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_CYCLE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_RELATIONSHIP_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_SOURCE_CI_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_TARGET_CI_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.EXTENDED_ATTRIBUTE_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.SPECIAL_ATTRIBUTE_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_CDM_NAMESPACE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_IMPLICIT_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_SOURCE_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_SOURCE_NAME;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_TARGET_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_TARGET_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.collation.platform.model.InterfaceIntrospector;
import com.collation.platform.model.ModelObject;
import com.collation.platform.model.topology.core.Relationship;
import com.collation.platform.model.topology.meta.ObjectAttribute;
import com.collation.platform.model.topology.meta.ObjectClass;
import com.collation.proxy.api.client.ApiException;
import com.collation.proxy.api.client.CMDBApi;
import com.ibm.cdb.api.server.ApiLookup;
import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.cdm.core.DefaultNamingRuleIdentifier;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;
import com.ibm.di.cdm.core.NamingRulesReader;
import com.ibm.di.connector.taddm.TADDMConnector;
import com.ibm.di.connector.taddm.cdm.query.CDM2TADDMQueryFilter;
import com.ibm.di.connector.taddm.cdm.query.DefaultQueryFilter;
import com.ibm.di.connector.taddm.cdm.query.QueryFilter;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This class is used for retrieving CDM meta-data from TADDM.
 */
public class TADDMMetaData extends MetaData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Whether the returned meta-data should be IdML-compatible or not.
	 */
	private boolean idmlMode;

	/**
	 * The TADDM API.
	 */
	private CMDBApi cmdbApi;

	/**
	 * A map between the CDM class types and the TADDM ones. For example:
	 * <p>
	 * <code>sys.ComputerSyste ->
	 * com.collation.platform.mode.topology.sys.ComputerSystem</code>
	 * </p>
	 */
	private Map<String, String> cdmToTADDMTypes;

	/**
	 * A map between the TADDM class types and the CDM ones. For example:
	 * <p>
	 * <code>com.collation.platform.mode.topology.sys.ComputerSystem ->
	 * sys.ComputerSyste</code>
	 * </p>
	 */
	private Map<String, String> taddmToCDMTypes;

	/**
	 * The Naming Rule reader used for this meta-data.
	 */
	private NamingRulesReader reader;

	/**
	 * A set of attributes which should not be returned. Most of them contain
	 * TADDM system information.
	 */
	private Set<String> excludeAttributes;

	/**
	 * The name of the class whose naming rules will be cached.
	 */
	private static String cachedClassType = null;

	/**
	 * The naming rules for a particular class used for future comparison to the
	 * same class.
	 */
	private static List<NamingRule> cachedNamingRules = null;

	/**
	 * 
	 */
	public TADDMMetaData(boolean idmlMode) {
		this(null, idmlMode);
	}

	/**
	 * Constructor.
	 * 
	 * @param cmdbApi
	 *            the TADDM API.
	 * @param idmlMode
	 *            whether IdML mode is on, or off.
	 */
	public TADDMMetaData(CMDBApi cmdbApi, boolean idmlMode) {
		this.idmlMode = idmlMode;
		this.cmdbApi = cmdbApi;
		this.reader = new TADDMNamingRulesReader(cmdbApi);

		cdmToTADDMTypes = new HashMap<String, String>();
		taddmToCDMTypes = new HashMap<String, String>();
		initNamingMaps();
		excludeAttributes = getExcludedAttributes();
	}

	/**
	 * Populates the CDM to TADDM and TADDM to CDM type maps.
	 */
	private void initNamingMaps() {
		String[] names = null;
		if (cmdbApi != null) {
			try {
				names = cmdbApi.getClassNames();
			} catch (ApiException ae) {
				// ignore
				names = null;
			}
		}

		if (names == null) {
			// Use the static class list provided by TADDM.
			// We do not use only it, because it does not provide some more
			// recent classes.
			names = ApiLookup.getNames();
		}
		for (int i = 0; i < names.length / 2; i++) {
			String fullName = names[i * 2 + 1];
			if (!isTADDMClass(fullName) || isEnum(fullName) || isCoreType(fullName)) {
				continue;
			}
			String cdmName = null;
			try {
				final String RELN_PREFIX = "com.collation.platform.model.topology.relation.";
				if (fullName.startsWith(RELN_PREFIX)) {
					cdmName = CDMUtils.toLowercaseFirstLetter(fullName.substring(RELN_PREFIX.length()));
				} else {
					cdmName = fullName.substring(TADDM_CDM_NAMESPACE.length());
				}
			} catch (Throwable e) {
				// ignore
			}
			if (cdmName != null) {
				cdmToTADDMTypes.put(cdmName, fullName);
				taddmToCDMTypes.put(fullName, cdmName);
			}
		}
	}

	/**
	 * At design type we return there class types because they are persistable
	 * and users can iterate them, if their names are explicitly provided.
	 * Furthermore, if the user has chosen a class like
	 * 'com.collation.platform.model.topology.core.Relationhip' he will be able
	 * to iterate all TADDM relationship types (including dependencies).
	 * 
	 * At runtime (when there is a CMDB API) we do not return these names
	 * because this leads to duplications when reading all TADDM class types (by
	 * leaving the 'Class Type' UI field blank). For example, in case of
	 * relationships, a single TransactionalDependency will be returned three
	 * times - once as a Relationship (super class), once as a Dependency (first
	 * level child) and once as a TransactionalDependency (second level child).
	 * 
	 * @param taddmClassType
	 *            the TADDM class type to be checked. A full TADDM name is
	 *            required.
	 * @return <b>true</b> if the class is from the core package and
	 *         <b>false</b> otherwise.
	 */
	private boolean isCoreType(String taddmClassType) {
		return taddmClassType.startsWith("com.collation.platform.model.topology.core");
	}

	/**
	 * Checks if provided class type is from enums package.
	 * 
	 * @param taddmClassType
	 *            the TADDM class type to be checked. A full TADDM name is
	 *            required.
	 * @return <b>true</b> if the class is from the enums package and
	 *         <b>false</b> otherwise.
	 */
	private boolean isEnum(String taddmClassType) {
		return taddmClassType.startsWith("com.collation.platform.model.topology.enums.");
	}

	/**
	 * Checks if provided class type is a TADDM class type.
	 * 
	 * @param classType
	 *            the class type to be checked. A full name is required.
	 * @return <b>true</b> if the class is a TADDM class type and <b>false</b>
	 *         otherwise.
	 */
	private boolean isTADDMClass(String classType) {
		return classType.startsWith(TADDM_CDM_NAMESPACE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector<Entry> getAttributes(String artifactType, String classType) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Note:</b> Not supported for TADDM. Throws an
	 * {@link UnsupportedOperationException}.
	 * </p>
	 */
	@Override
	public String getCdmVersion() throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getTypes(String artifactType) throws Exception {
		if ("CI".equals(artifactType)) {
			return getTypes(false);
		} else if ("RELATIONSHIP".equals(artifactType)) {
			return getTypes(true);
		}
		return null;
	}

	/**
	 * Gets the types of either Items or Relationships.
	 * 
	 * @param relationships
	 *            if <code>true</code> relationship type are returned, otherwise
	 *            <code>false</code>.
	 * @return a Map with the types.
	 */
	private Map<String, Object> getTypes(boolean relationships) {
		Map<String, Object> result = new TreeMap<String, Object>();
		for (Map.Entry<String, String> entry : taddmToCDMTypes.entrySet()) {
			try {
				Class<?> clazz = Class.forName(entry.getKey());
				if (ModelObject.class.isAssignableFrom(clazz) && isRelationship(clazz) == relationships) {
					if (idmlMode) {
						result.put(CDM_PREFIX + entry.getValue(), null);
					} else {
						result.put(entry.getValue(), null);
					}
				}
			} catch (ClassNotFoundException cnfe) { // ignore
				cnfe.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Checks if the provided Class is a TADDM relationship.
	 * 
	 * @param clazz
	 *            class to be checked.
	 * @return <code>true</code> if a relationship, otherwise <code>false</code>
	 *         .
	 */
	private boolean isRelationship(Class<?> clazz) {
		boolean isRelationship = false;
		if (clazz != null && isTADDMClass(clazz.getCanonicalName())) {
			isRelationship = Relationship.class.isAssignableFrom(clazz);
		}
		return isRelationship;
	}

	/**
	 * Checks if the provided Class is a TADDM relationship.
	 * 
	 * @param classType
	 *            the classType to be checked.
	 * @return <code>true</code> if a relationship, otherwise <code>false</code>
	 *         .
	 */
	public boolean isRelationship(String classType) {
		try {
			return isRelationship(Class.forName(getTADDMClassType(classType)));
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMSSClassName() {
		String className = super.getMSSClassName();
		if (idmlMode) {
			className = CDM_PREFIX + className;
		}
		return className;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtendedAttributeName(String attributeName) {
		if (idmlMode) {
			return super.getExtendedAttributeName(attributeName);
		}
		return EXTENDED_ATTRIBUTE_PREFIX + attributeName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NamingRule> getNamingRules(String classType) throws Exception {
		if (cachedNamingRules == null || cachedClassType == null || !cachedClassType.equals(classType)) {
			cachedClassType = classType;
			cachedNamingRules = reader.getAllNamingRules(classType);
		}
		return cachedNamingRules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesNamingRules(String classType, Set<String> attributes) {
		boolean result = true;
		if (idmlMode) {
			if (isRelationship(classType)) {
				result = attributes.contains(TADDM_SOURCE_NAME) && attributes.contains(TADDM_TARGET_NAME);
			} else {
				Set<String> formattedAttributres = new HashSet<String>();
				for (String attribute : attributes) {
					formattedAttributres.add(CDMUtils.toUpperCaseFirstLetter(attribute));
				}
				result = super.matchesNamingRules(classType, formattedAttributres);
			}
		}
		return result;
	}

	/**
	 * Creates a query filter to be used to convert IdML enabled queries to
	 * native TADDM ones.
	 * 
	 * @return the query filter.
	 */
	public QueryFilter createQueryFilter() {
		if (idmlMode) {
			return new CDM2TADDMQueryFilter(this);
		}
		return new DefaultQueryFilter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassType(String classType) {
		if (classType != null) {
			if (idmlMode) {
				classType = getCDMClassType(classType); // check for TADDM name
				classType = CDMUtils.escapeString(classType); // escape '.'
				classType = CDMUtils.addPrefix(classType); // add 'cdm:'
			} else {
				classType = CDMUtils.removePrefix(classType);
				classType = CDMUtils.removeEscapeChars(classType);
				classType = getTADDMClassType(classType);
			}
		}
		return classType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Attribute createExplicitAttribute(Node currentNode, String attributeName) {
		String name = attributeName;
		if (idmlMode) {
			name = CDM_PREFIX + CDMUtils.toUpperCaseFirstLetter(attributeName);
		}
		return super.createExplicitAttribute(currentNode, name);
	}

	/**
	 * Creates an implicit attribute using the provided information.
	 * 
	 * @param currentNode
	 *            the node where the attribute is to be added.
	 * @param classType
	 *            the type of the class owning the implicit attribute.
	 * @param attributeName
	 *            the name of the attribute that will be carted.
	 * @return the created attribute.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Attribute createImplicitAttributeByName(Node currentNode, String classType, String attributeName) throws Exception {
		Attribute attribute = null;
		classType = getTADDMClassType(classType);
		Class<?> clazz = Class.forName(classType);

		if (idmlMode) {
			if (isRelationship(clazz)) {
				String name = TADDM_SOURCE_NAME.equalsIgnoreCase(attributeName) ? CDM_SOURCE_CI_PREFIX : CDM_TARGET_CI_PREFIX;
				name += "ModelObject";
				attribute = super.createImplicitAttribute(currentNode, classType, name, null, null);
			} else {
				try {
					ObjectClass objectClass = cmdbApi.getMetaData(classType, true);
					if (objectClass.hasObjectAttributes()) {
						ObjectAttribute[] attrs = objectClass.getObjectAttributes();
						int i = 0;
						boolean isFound = false;
						while (i < attrs.length && !isFound) {
							ObjectAttribute attr = attrs[i++];
							if (!isImplicitAttribute(attr)) {
								continue;
							}

							String name = attr.getName();
							if (name.equals(attributeName)) {
								isFound = true;
								boolean isFrowardRelationship = !(attr.hasReverseRelationship() && attr.getReverseRelationship());
								attribute = this.createImplicitAttribute(currentNode, classType, attr.getRelationshipType(), attr
										.getType(), isFrowardRelationship, null);
							}
						}
					}
				} catch (ApiException ae) {
					throw new Exception(getMessage("TADDM.CONN.ERROR.GETTING.ATTRIBUTES", classType), ae);
				}
			}
		} else {
			if (isRelationship(clazz)) {
				String name = TADDM_SOURCE_NAME.equalsIgnoreCase(attributeName) ? TADDM_SOURCE_ATTRIBUTE : TADDM_TARGET_ATTRIBUTE;
				attribute = super.createImplicitAttribute(currentNode, classType, name, null, null);
			} else {
				attribute = super.createImplicitAttribute(currentNode, classType, TADDM_IMPLICIT_ATTRIBUTE, attributeName, null);
			}
		}
		return attribute;
	}

	/**
	 * Checks if this {@link ObjectAttribute} is an implicit attribute.
	 * 
	 * @param attr
	 *            the ObjectAttribute.
	 * @return <code>true</code> if it is an implicit attribute, otherwise
	 *         <code>false</code>.
	 */
	private boolean isImplicitAttribute(ObjectAttribute attr) {
		return attr.hasRelationshipType() && attr.hasName() && attr.hasType();
	}

	/**
	 * Checks if the provided string is the name of an implicit attribute.
	 * 
	 * @param name
	 *            the attribute name.
	 * @return <code>true</code> if it is an implicit attribute, otherwise
	 *         <code>false</code>.
	 */
	public boolean isImplicitAttribute(String name) {
		return (name.startsWith(SPECIAL_ATTRIBUTE_PREFIX) && !CDM_ID_SYSTEM_ATTRIBUTE.equals(name)
				&& !CDM_CLASSTYPE_SYSTEM_ATTRIBUTE.equals(name) && !CDM_CYCLE_SYSTEM_ATTRIBUTE.equals(name))
				|| name.startsWith(CDM_RELATIONSHIP_PREFIX)
				|| name.startsWith(CDM_SOURCE_CI_PREFIX)
				|| name.startsWith(CDM_TARGET_CI_PREFIX);
	}

	/**
	 * Returns the CDM class name corresponding to the provided string.
	 * 
	 * @param classType
	 *            the native class name.
	 * @return CDM class name.
	 */
	public String getCDMClassType(String classType) {
		String cdmClassType = classType;
		if (isTADDMClass(classType)) {
			cdmClassType = taddmToCDMTypes.get(classType);

			if (cdmClassType == null) {
				// Here we must throw an Exception, but the TADDM meta data does
				// not provided all CDM classes, so we try to handle the missing
				// ones ourselves.

				if (isRelationship(classType)) {
					cdmClassType = classType.substring(classType.lastIndexOf('.') + 1);
					cdmClassType = Character.toLowerCase(cdmClassType.charAt(0)) + cdmClassType.substring(1);
				} else {
					cdmClassType = classType.substring(TADDM_CDM_NAMESPACE.length());
				}
			}
		}
		return cdmClassType;
	}

	/**
	 * Creates an implicit attribute using the provided information. A check for
	 * existing duplicates is performed.
	 * 
	 * @param currentNode
	 *            the node where the attribute is to be added.
	 * @param classType
	 *            the type of the class owning the implicit attribute.
	 * @param relationshipName
	 *            the name of the relationship represented by this implicit
	 *            attribute.
	 * @param relatedClassType
	 *            the type of the other item which the implicit attribute points
	 *            to.
	 * @param isForward
	 *            the direction of the relationship represented by this implicit
	 *            attribute.
	 * @param relatedID
	 *            the ID of the related item. If provided the current hierarchy
	 *            will be checked for a duplicate and if such is found the
	 *            implicit attribute will not be added. To skip the duplicate
	 *            check set this parameter to <code>null</code>.
	 * @return the created attribute.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Attribute createImplicitAttribute(Node currentNode, String classType, String relationshipName, String relatedClassType,
			boolean isForward, String relatedID) throws Exception {
		if (idmlMode) {
			classType = getCDMClassType(classType);
			relationshipName = CDM_RELATIONSHIP_PREFIX + getCDMClassType(relationshipName);
			relatedClassType = getCDMClassType(relatedClassType);
			relatedClassType = (isForward ? CDM_TARGET_CI_PREFIX : CDM_SOURCE_CI_PREFIX) + relatedClassType;
		} else {
			relatedClassType = getTADDMImplicitAttributeName(classType, relationshipName, relatedClassType, isForward);
			relationshipName = TADDM_IMPLICIT_ATTRIBUTE;
		}
		return super.createImplicitAttribute(currentNode, classType, relationshipName, relatedClassType, relatedID);
	}

	/**
	 * Returns the TADDM class type. If a TADDM type is provided as input it is
	 * just returned. If CDM type is provided and not found an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @param cdmClassType
	 *            the class type.
	 * @return the TADDM class type.
	 */
	public String getTADDMClassType(String cdmClassType) {
		String taddmClassType = cdmClassType;
		final String TADDM_CDM_NAMESPACE_PREFIX = "com.collation.platform.model.";
		if (!cdmClassType.startsWith(TADDM_CDM_NAMESPACE_PREFIX)) {
			taddmClassType = cdmToTADDMTypes.get(cdmClassType);
			if (taddmClassType == null) {
				if (cdmClassType.contains(".")) {
					taddmClassType = TADDM_CDM_NAMESPACE + cdmClassType;
				} else if ("ModelObject".equals(cdmClassType)) {
					taddmClassType = TADDM_CDM_NAMESPACE_PREFIX + cdmClassType;
				} else if (Character.isLowerCase(cdmClassType.charAt(0))) {
					taddmClassType = TADDM_CDM_NAMESPACE + "relation." + CDMUtils.toUpperCaseFirstLetter(cdmClassType);
				} else {
					throw new IllegalArgumentException(getMessage("TADDM.CONN.UNKNOWN.CDM.CLASS.TYPE", cdmClassType));
				}
			}
		}
		return taddmClassType;
	}

	/**
	 * Returns the TADDM explicit attribute name. If the provided CDM attribute
	 * is not correctly named an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param cdmAttributeName
	 *            the class type.
	 * @return the TADDM class type.
	 */
	public String getTADDMExplicitAttributeName(String cdmAttributeName) {
		String resultName = cdmAttributeName;
		if ((Character.isUpperCase(cdmAttributeName.charAt(0)))
				&& (cdmAttributeName.length() < 2 || Character.isLowerCase(cdmAttributeName.charAt(1)))) {
			resultName = CDMUtils.toLowercaseFirstLetter(cdmAttributeName);
		}
		return resultName;
	}

	/**
	 * Returns the TADDM implicit attribute name corresponding to the provided
	 * data.
	 * 
	 * @param cdmClassType
	 *            the attribute's class type.
	 * @param cdmRelationshipType
	 *            the relationship type which this attribute represents.
	 * @param cdmRelatedClassType
	 *            the type of the item this attribute links to.
	 * @param isForward
	 *            whether the represented relationship is forward (from the
	 *            owning item to the related one) or backward.
	 * @return the implicit attribute's name.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String getTADDMImplicitAttributeName(String cdmClassType, String cdmRelationshipType, String cdmRelatedClassType,
			boolean isForward) throws Exception {
		String taddmClassType = getTADDMClassType(cdmClassType);
		String taddmRelationshipType = getTADDMClassType(cdmRelationshipType);
		String taddmRelatedClassType = getTADDMClassType(cdmRelatedClassType);
		String implicitAttribute = null;
		try {
			ObjectClass clazz = cmdbApi.getMetaData(taddmClassType, true);
			if (clazz.hasObjectAttributes()) {
				ObjectAttribute[] attrs = clazz.getObjectAttributes();
				int i = 0;
				boolean isFound = false;
				while (i < attrs.length && !isFound) {
					ObjectAttribute attr = attrs[i++];
					if (!attr.hasName() || !attr.hasRelationshipType() || !attr.hasReverseRelationship() || !attr.hasType()) {
						continue;
					}
					if (taddmRelationshipType.equalsIgnoreCase(attr.getRelationshipType()) //
							&& taddmRelatedClassType.equalsIgnoreCase(attr.getType()) //
							&& (isForward == !attr.getReverseRelationship())) {
						implicitAttribute = attr.getName();
						isFound = true;
					}
				}
			}
		} catch (ApiException ae) {
			throw new Exception(getMessage("TADDM.CONN.ERROR.GETTING.ATTRIBUTES", taddmClassType), ae);
		}
		if (implicitAttribute == null) {
			String source = cdmClassType;
			String target = cdmRelatedClassType;
			if (!isForward) {
				source = cdmRelatedClassType;
				target = cdmClassType;
			}
			throw new Exception(getMessage("TADDM.CONN.INVALID.IMPLICIT.ATTRIBUTE.NAME", cdmRelationshipType, source, target));
		}
		return implicitAttribute;
	}

	/**
	 * Returns the map of TADDM implicit attributes name and related node,
	 * corresponding to the provided data.
	 * 
	 * @param classType
	 *            the attribute's class type.
	 * @param node
	 *            related node
	 * @return map of implicit attribute's name and related node.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Map<String, List<Node>> getTADDMImplicitAttributes(String classType, Node node) throws Exception {
		Map<String, List<Node>> result = new HashMap<String, List<Node>>();
		String parent = node.getNodeName();
		if (isRelationship(classType)) {
			String name = null;
			if (TADDM_SOURCE_ATTRIBUTE.equalsIgnoreCase(parent) || parent.startsWith(CDM_SOURCE_CI_PREFIX)) {
				name = TADDM_SOURCE_NAME;
			} else if (TADDM_TARGET_ATTRIBUTE.equalsIgnoreCase(parent) || parent.startsWith(CDM_TARGET_CI_PREFIX)) {
				name = TADDM_TARGET_NAME;
			} else {
				throw new Exception(getMessage("TADDM.CONN.INVALID.ATTRIBUTE.NAME", parent, classType));
			}
			List<Node> relationList = new ArrayList<Node>();
			relationList.add(node);
			result.put(name, relationList);
		} else {
			NodeList children = node.getChildNodes();
			List<Node> childrenList = null;
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				String name = getTADDMImplicitAttributeName(classType, parent, child.getNodeName());
				if (result.get(name) == null) {
					childrenList = new ArrayList<Node>();
				} else {
					childrenList = result.get(name);
				}
				childrenList.add(child);
				result.put(name, childrenList);
			}
		}

		return result;
	}

	/**
	 * Gets the TADDM implicit attribute name corresponding to the specified
	 * structure. This method can be used for both native representations and
	 * IdML-compatible ones.
	 * 
	 * @param classType
	 *            the attribute's class type.
	 * @param parent
	 *            the parent-part of the implicit attribute.
	 * @param child
	 *            the child part of the implicit attribute.
	 * @return the implicit attribute's name.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private String getTADDMImplicitAttributeName(String classType, String parent, String child) throws Exception {
		if (idmlMode) {
			if (!parent.startsWith(CDM_RELATIONSHIP_PREFIX)) {
				throw new IllegalArgumentException(getMessage("TADDM.CONN.INVALID.IDML.ATTRIBUTE.NAME", parent));
			}
			if (!child.startsWith(CDM_SOURCE_CI_PREFIX) && !child.startsWith(CDM_TARGET_CI_PREFIX)) {
				throw new IllegalArgumentException(getMessage("TADDM.CONN.INVALID.IDML.ATTRIBUTE.NAME", child));
			}
			boolean isForward = child.startsWith(CDM_TARGET_CI_PREFIX);
			parent = CDMUtils.removePrefix(parent);
			child = CDMUtils.removePrefix(child);
			return getTADDMImplicitAttributeName(classType, parent, child, isForward);
		} else {
			if (!(TADDM_IMPLICIT_ATTRIBUTE.equals(parent) || TADDM_SOURCE_ATTRIBUTE.equals(parent) || TADDM_TARGET_ATTRIBUTE
					.equals(parent))) {
				throw new IllegalArgumentException(getMessage("TADDM.CONN.INVALID.ATTRIBUTE.NAME", parent, classType));
			}
			return child;
		}
	}

	/**
	 * Gets the reversed implicit attribute. Each implicit attribute represent a
	 * relationship to another Item. This method get the name of the implicit
	 * attribute used for the same relationship in the other item.
	 * 
	 * @param cdmClassName
	 *            the attribute's class type.
	 * @param implicitAttributeName
	 *            the implicit attribute's name.
	 * @return the opposite implicit attribute's name.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String getTADDMReversedImplicitAttributeName(String cdmClassName, String implicitAttributeName) throws Exception {
		String reverseImplicitAttribute = null;
		try {
			String taddmClassType = getTADDMClassType(cdmClassName);
			ObjectClass clazz = cmdbApi.getMetaData(taddmClassType, true);
			if (clazz.hasObjectAttributes()) {
				ObjectAttribute[] attrs = clazz.getObjectAttributes();
				int i = 0;
				boolean isFound = false;
				while (i < attrs.length && !isFound) {
					ObjectAttribute attr = attrs[i++];
					if (!attr.hasName() || !attr.hasRelationshipType() || !attr.hasReverseRelationship() || !attr.hasType()) {
						continue;
					}
					if (implicitAttributeName.equals(attr.getName())) {
						reverseImplicitAttribute = getTADDMImplicitAttributeName(attr.getType(), attr.getRelationshipType(),
								taddmClassType, attr.getReverseRelationship());
						isFound = true;
					}
				}
			}
		} catch (Exception e) {
			// ignore
			return null;
		}
		return reverseImplicitAttribute;
	}

	/**
	 * Returns the CDM class type of the provided Model Object.
	 * 
	 * @param modelObject
	 *            model object to be checked.
	 * @return the CDM format class type.
	 */
	public String getModelObjectClass(ModelObject modelObject) {
		return InterfaceIntrospector.getModelObjectInterface(modelObject).getCanonicalName();
	}

	/**
	 * Creates the excluded attributes list.
	 * 
	 * @return the list of excluded attributes.
	 */
	private Set<String> getExcludedAttributes() {
		Set<String> excludedAttrs = new HashSet<String>();
		String[] excludes = new String[] { "allAttributes", "class", "extendedAttributes", "bidiFlag", "bidiFormat", "cmdbSource",
				"namingAttributes" };
		excludedAttrs.addAll(Arrays.asList(excludes));
		return excludedAttrs;
	}

	/**
	 * Checks if the attribute is supported.
	 * 
	 * @param attributeName
	 *            the attribute to be checked
	 * @param classType
	 *            in case class type is relationship, supported attributes are
	 *            <b>source</b> and <b>target</b>.
	 * @return <b>true</b> if the attribute is supported, <b>false</b>
	 *         otherwise.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public boolean isSupportedAttribute(String attributeName, String classType) throws Exception {
		if (excludeAttributes.contains(attributeName)) {
			return false;
		}

		if (idmlMode && classType != null && isRelationship(classType)) {
			return TADDM_SOURCE_NAME.equalsIgnoreCase(attributeName) || TADDM_TARGET_NAME.equalsIgnoreCase(attributeName);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getIdentifierName(NamingRuleIdentifier identifier) {
		String identifierName = null;
		if (idmlMode) {
			identifierName = super.getIdentifierName(identifier);
		} else {
			identifierName = getTADDMExplicitAttributeName(identifier.getAttributeName());
			if (identifier.isImplicit()) {
				identifierName = TADDM_IMPLICIT_ATTRIBUTE + '.' + identifierName;
			}
		}
		return identifierName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NamingRuleIdentifier getNamingRuleIdentifier(String classType, String originalName) {
		NamingRuleIdentifier identifier = null;
		if (idmlMode) {
			identifier = super.getNamingRuleIdentifier(classType, originalName);
		} else if (isImplicitAttribute(originalName)) {
			String[] parts = originalName.split("\\.");
			String attributeName = null;
			if (parts.length >= 2) {
				attributeName = CDMUtils.toUpperCaseFirstLetter(parts[1]);
			}
			identifier = new DefaultNamingRuleIdentifier(attributeName);
		} else {
			identifier = new DefaultNamingRuleIdentifier(CDMUtils.toUpperCaseFirstLetter(originalName));
		}
		return identifier;
	}

	/**
	 * Gets a localized message using the provided key and adding the available
	 * values.
	 * 
	 * @param key
	 *            the message's key.
	 * @param values
	 *            the values to be added to the message.
	 * @return the formatted localized string.
	 */
	private String getMessage(String key, Object... values) {
		return TADDMConnector.L10N.getString(key, values);
	}

}