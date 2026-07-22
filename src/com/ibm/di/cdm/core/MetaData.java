/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CYCLE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_RELATIONSHIP_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_SOURCE_CI_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_TARGET_CI_PREFIX;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.NameTokenizer;
import com.ibm.di.fc.idml.IdMLConstants;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;

/**
 * An abstract class that declares the routines to be used to retrieve the CDM
 * meta-data.
 */
public abstract class MetaData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The properties file containing messages.
	 */
	private static final String PROPERTIES_FILE = "openidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	protected static final ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Retrieves the attributes of a given artifact (CI or a Relationship).
	 * 
	 * @param artifactType
	 *            determines the type of artifact, which attributes we want.
	 *            Accepts either {@link IdMLConstants#ARTIFACT_CI} or
	 *            {@link IdMLConstants#ARTIFACT_RELATIONSHIP}.
	 * @param classType
	 *            the type of the artifact, which attributes we want.
	 * @return a Vector containing the CI/Relationship attributes.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public abstract Vector<Entry> getAttributes(String artifactType, String classType) throws Exception;

	/**
	 * Retrieves the version of the Common Data Model, which meta-data is
	 * extracted.
	 * 
	 * @return a string representing the CDM version. Its format is
	 *         '&ltversion&gt.&ltrelease&gt.&ltmodifier&gt'.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public abstract String getCdmVersion() throws Exception;

	/**
	 * Returns the CDM meta-data types artifacts (CIs or Relationships).
	 * 
	 * @param artifactType
	 *            determines the type of artifact - either
	 *            {@link IdMLConstants#ARTIFACT_CI} or
	 *            {@link IdMLConstants#ARTIFACT_RELATIONSHIP}.
	 * @return a Map containing the CDM type names and additional data for them
	 *         as an Object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public abstract Map<String, Object> getTypes(String artifactType) throws Exception;

	/**
	 * Returns the attributes supported by a Relationship. Each Relationship has
	 * two mandatory attributes - <b>source</b> and <b>target</b>.
	 * 
	 * @return a Vector containing the Relationship attributes.
	 */
	protected Vector<Entry> getRelationshipAttributes() {
		Vector<Entry> relationshipAttributes = new Vector<Entry>();
		// add 'source' attribute
		Entry source = new Entry();
		source.addAttributeValue("name", IdMLConstants.RELATIONSHIP_SOURCE_ATTR);
		source.addAttributeValue("syntax", String.class.getName());
		relationshipAttributes.add(source);

		// add 'target' attribute
		Entry target = new Entry();
		target.addAttributeValue("name", IdMLConstants.RELATIONSHIP_TARGET_ATTR);
		target.addAttributeValue("syntax", String.class.getName());
		relationshipAttributes.add(target);

		return relationshipAttributes;
	}

	/**
	 * Prefixes the provided string as an extended attribute.
	 * 
	 * @param attributeName
	 *            input name.
	 * @return prefixed name.
	 */
	public String getExtendedAttributeName(String attributeName) {
		String newAttributeName = attributeName;
		if (!attributeName.startsWith(CDMConstants.CDM_EXTENDED_ATTRIBUTE_PREFIX)) {
			newAttributeName = CDMConstants.CDM_EXTENDED_ATTRIBUTE_PREFIX + attributeName;
		}
		return newAttributeName;
	}

	/**
	 * Returns the correct class type.
	 * 
	 * @param classType
	 *            the native class name.
	 * @return CDM class name.
	 */
	public String getClassType(String classType) {
		return CDMUtils.addPrefix(classType);
	}

	/**
	 * Returns the name of the ManagementSoftwareSystem class type.
	 * 
	 * @return the MSS class type.
	 */
	public String getMSSClassName() {
		return "process.ManagementSoftwareSystem";
	}

	/**
	 * Creates an explicit attribute with the corresponding name and adds it to
	 * the provided node.
	 * 
	 * @param currentNode
	 *            the node where the attribute is to be added.
	 * @param name
	 *            the name of the new attribute.
	 * @return the created attribute.
	 */
	public Attribute createExplicitAttribute(Node currentNode, String name) {
		return createSimpleAttribute(currentNode, name);
	}

	/**
	 * Creates an implicit attribute using the provided information. A check for
	 * existing duplicates is performed.
	 * 
	 * @param currentNode
	 *            the node where the attribute is to be added.
	 * @param classType
	 *            the type of the class owning the implicit attribute.
	 * @param parentName
	 *            the name of the relationship represented by this implicit
	 *            attribute.
	 * @param childName
	 *            the type of the other item which the implicit attribute points
	 *            to.
	 * @param relatedID
	 *            the ID of the related item. If provided the current hierarchy
	 *            will be checked for a duplicate and if such is found the
	 *            implicit attribute will not be added. To skip the duplicate
	 *            check set this parameter to <code>null</code>.
	 * @return the created attribute.
	 */
	public Attribute createImplicitAttribute(Node currentNode, String classType, String parentName, String childName,
			String relatedID) {
		if (relatedID != null && isDuplicateRelationship(currentNode, parentName, childName, relatedID)) {
			return null;
		}

		Attribute result = null;
		if (childName == null) {
			result = createSimpleAttribute(currentNode, parentName);
		} else {
			result = createFoldedAttribute(currentNode, parentName, childName);
		}
		return result;
	}

	/**
	 * Checks if the provided node contains another implicit attribute matching
	 * these conditions.
	 * 
	 * @param currentNode
	 *            the checked node.
	 * @param parentName
	 *            the first part of the implicit attribute.
	 * @param childName
	 *            the second part of the implicit attribute.
	 * @param relatedID
	 *            the ID of the related item.
	 * @return <code>true</code> if a duplicate is found, otherwise
	 *         <code>false</code>.
	 */
	private boolean isDuplicateRelationship(Node currentNode, String parentName, String childName, String relatedID) {
		boolean isDuplicate = false;
		int i = 0;
		NodeList relnList = currentNode.getChildNodes();
		while (i < relnList.getLength() && !isDuplicate) {
			Node relnNode = relnList.item(i++);
			if (relnNode.getNodeName().equals(parentName)) {
				NodeList classList = relnNode.getChildNodes();
				int j = 0;
				while (j < classList.getLength() && !isDuplicate) {
					Node classNode = classList.item(j++);
					if (classNode.getNodeName().equals(childName)) {
						int k = 0;
						NodeList attrList = classNode.getChildNodes();
						while (k < attrList.getLength() && !isDuplicate) {
							Node attrNode = attrList.item(k++);
							String attrName = attrNode.getNodeName();
							if (CDM_ID_SYSTEM_ATTRIBUTE.equals(attrName) || CDM_CYCLE_SYSTEM_ATTRIBUTE.equals(attrName)) {
								try {
									isDuplicate = relatedID.equals(attrNode.getNodeValue());
								} catch (DOMException e) {
									SystemFunctions.doNothing();
									// ignore
								}
							}
						}
					}
				}
			}
		}
		return isDuplicate;
	}

	/**
	 * Creates an attribute which consists of two nodes. First, the provided
	 * node is checked if the parent-part is not present. If it exists,
	 * child-part is directly added to it. If it does not exist, both the
	 * parent-part and child-part are added.
	 * 
	 * @param currentNode
	 *            the node where this attribute is added.
	 * @param parentName
	 *            the parent part of the attribute.
	 * @param childName
	 *            the child-part of the attribute.
	 * @return the created attribute.
	 */
	private Attribute createFoldedAttribute(Node currentNode, String parentName, String childName) {
		Node parentNode = getChildNodeByName(currentNode, parentName);
		return createSimpleAttribute(parentNode, childName);
	}

	/**
	 * Creates a simple implicit attribute using the provided information.
	 * 
	 * @param currentNode
	 *            the node where this attribute is added.
	 * @param name
	 *            the name of the attribute to be created.
	 * @return the created attribute.
	 */
	private Attribute createSimpleAttribute(Node currentNode, String name) {
		Attribute attribute = new Attribute(name);
		currentNode.appendChild(attribute);
		return attribute;
	}

	/**
	 * Searches and returns the child of the provided node with the provided
	 * name. If none is found, a new child with this name is added and it is
	 * returned.
	 * 
	 * @param currentNode
	 *            the checked node.
	 * @param childName
	 *            the child node's name.
	 * @return the found/created node.
	 */
	private Node getChildNodeByName(Node currentNode, String childName) {
		int i = 0;
		NodeList list = currentNode.getChildNodes();
		Node parentNode = null;
		while (i < list.getLength() && parentNode == null) {
			Node node = list.item(i++);
			if (node.getNodeName().equals(childName)) {
				parentNode = node;
			}
		}
		if (parentNode == null) {
			parentNode = currentNode.appendChild(new Attribute(childName));
		}
		return parentNode;
	}

	/**
	 * Returns the CDM naming rules for the provided class.
	 * 
	 * @param classType
	 *            the class type of interest.
	 * @return a list of the available naming rules.
	 * @throws Exception
	 *             if a problem occurs when reading the rules.
	 */
	protected abstract List<NamingRule> getNamingRules(String classType) throws Exception;

	/**
	 * Returns true if the provided set of attributes matches any of the rules
	 * of that class.
	 * 
	 * @param classType
	 *            the class type of interest.
	 * @param attributes
	 *            a list of available attribute names.
	 * @return <code>true</code> if a matching rule is found, <code>false</code>
	 *         otherwise.
	 */
	public boolean matchesNamingRules(String classType, Set<String> attributes) {
		boolean matchesRule = false;
		try {
			List<NamingRule> rules = getNamingRules(classType);
			int i = 0;
			while (i < rules.size() && !matchesRule) {
				NamingRule currentRule = rules.get(i++);
				matchesRule = currentRule.matches(attributes);
			}
		} catch (Exception ex) {
			SystemFunctions.doNothing();
			// error retrieving naming rules
		}
		return matchesRule;
	}

	/**
	 * Compute delta of naming rules for the class type and provided attributes.
	 * 
	 * @param classType
	 *            the class whose rules will be processed.
	 * @param attributeNames
	 *            Set of attribute names.
	 * @return list of delta naming rules.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public List<NamingRule> getUnsatisfiedNamingRules(String classType, Set<String> attributeNames) throws Exception {
		classType = CDMUtils.removePrefix(classType);
		Set<NamingRuleIdentifier> identifiers = new HashSet<NamingRuleIdentifier>();
		for (String attribute : attributeNames) {
			identifiers.add(getNamingRuleIdentifier(classType, attribute));
		}

		List<NamingRule> namingRules = new ArrayList<NamingRule>();
		for (NamingRule namingRule : getNamingRules(classType)) {
			NamingRule resultRule = namingRule.intersect(identifiers);
			for (NamingRuleIdentifier identifier : resultRule.getIdentifiers()) {
				String name = getIdentifierName(identifier);
				identifier.setAttributeName(name);
			}
			namingRules.add(resultRule);
		}
		return namingRules;
	}

	/**
	 * Create NamingRuleIdentifier be provided class type and attribute name.
	 * 
	 * @param classType
	 *            the class whose attribute is processed.
	 * @param originalName
	 *            original attribute name.
	 * @return new NamingRuleIdentifier for provided attribute.
	 */
	protected NamingRuleIdentifier getNamingRuleIdentifier(String classType, String originalName) {
		NamingRuleIdentifier identifier = null;
		if (originalName.startsWith(CDMConstants.CDM_RELATIONSHIP_PREFIX)) {
			NameTokenizer tokenizer = new NameTokenizer();
			tokenizer.setEscapeChar('\\');
			tokenizer.setName(originalName);
			String relationshipType = CDMUtils.removePrefix(tokenizer.getNextToken('.'));
			String token = tokenizer.getNextToken('.');
			boolean isForward = token.startsWith(CDM_TARGET_CI_PREFIX);
			String relatedClass = CDMUtils.removePrefix(token);
			identifier = new DefaultNamingRuleIdentifier(null, relationshipType, relatedClass, isForward, true);
		} else {
			identifier = new DefaultNamingRuleIdentifier(CDMUtils.removePrefix(originalName));
		}
		return identifier;
	}

	/**
	 * Generate proper name for provided NamingRuleIdentifier.
	 * 
	 * @param identifier
	 *            whose name will be generate.
	 * @return a proper name
	 */
	protected String getIdentifierName(NamingRuleIdentifier identifier) {
		String identifierName = null;
		if (identifier.isImplicit()) {
			StringBuilder attributeName = new StringBuilder(CDM_RELATIONSHIP_PREFIX);
			attributeName.append(identifier.getRelationshipType());
			attributeName.append('.');
			attributeName.append(identifier.isRelationshipSource() ? CDM_TARGET_CI_PREFIX : CDM_SOURCE_CI_PREFIX);
			attributeName.append(CDMUtils.escapeString(identifier.getRelatedClass()));
			identifierName = attributeName.toString();
		} else {
			identifierName = CDMUtils.toUpperCaseFirstLetter(identifier.getAttributeName());
			identifierName = CDMUtils.addPrefix(identifierName);
		}
		return identifierName;
	}

}