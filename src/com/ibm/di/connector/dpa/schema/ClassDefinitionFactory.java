/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.di.server.ResourceHash;

/**
 * This class is responsible for reading data schema from an input XML file and building 
 * the data definition model.
 * 
 * @author yavor.gologanov
 * 
 */
public class ClassDefinitionFactory {
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dpaconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);	

	public static final String PARENT_RELATION_NAME = "parent";

	protected static final String NODE_CLASS = "class";
	protected static final String ATTR_CLASS_NAME = "name";
	protected static final String ATTR_CLASS_TABLE = "table";

	protected static final String NODE_PROPERTIES = "properties";

	protected static final String NODE_ADDPROPERTIES = "additional_properties";
	protected static final String NODE_ADDPROPERTIES_NAME = "name";
	protected static final String NODE_ADDPROPERTIES_TABLE = "table";
	protected static final String NODE_ADDPROPERTIES_JOINCOLUMN = "joinColumn";
	protected static final String NODE_ADDPROPERTIES_ONPROP = "onProperty";

	protected static final String NODE_EXTENDS = "extends";
	protected static final String ATTR_EXTENDS_CLASS = "class";
	
	protected static final String NODE_PKDEF = "pkDefinition";
	protected static final String ATTR_PKDEF_TYPE = "type";
	protected static final String ATTR_PKDEF_VALUE = "value";

	protected static final String NODE_PROPERTY = "property";
	protected static final String ATTR_PROP_NAME = "name";
	protected static final String ATTR_PROP_COLUMN_NAME = "column";
	protected static final String ATTR_PROP_TYPE = "type";
	protected static final String ATTR_PROP_NATIVE_TYPE = "nativeType";
	protected static final String ATTR_PROP_REQUIRED = "required";
	protected static final String ATTR_PROP_UNIQUE = "unique";
	protected static final String ATTR_PROP_PRIMARY = "primary";

	protected static final String NODE_REFERENCES = "references";
	protected static final String NODE_REFERENCE = "reference";
	protected static final String ATTR_REF_NAME = "name";
	protected static final String ATTR_REF_CLASS = "class";
	protected static final String ATTR_REF_MAX = "maxOccurs";
	protected static final String ATTR_REF_MIN = "minOccurs";
	protected static final String ATTR_REF_TYPE = "type";
	protected static final String ATTR_REF_REVPK = "reversePK";

	protected static final String NODE_REF_KEY = "key";
	protected static final String ATTR_REF_KEY_COLNAME = "column";
	protected static final String ATTR_REF_KEY_ONPROPERTY = "onProperty";
	protected static final String ATTR_REF_KEY_JOINTABLE = "joinTable";
	protected static final String ATTR_REF_KEY_JOINCOLUMN = "joinColumn";

	// -------------------------------------------------------------------------

	private Map<String, ClassDefinition> defMap = new HashMap<String, ClassDefinition>();

	/**
	 * 
	 * @param configFile
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void init(URL configFile) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(configFile.openStream());
		parse(doc);
	}

	/**
	 * 
	 * @param className
	 * @return ClassDefinition
	 */
	public ClassDefinition getDefinition(String className) {

		ClassDefinition classdef = defMap.get(className);
		if (classdef == null) {
			throw new MissingClassDefinitionException(className);
		}

		return classdef;
	}
	
	// -------------------------------------------------------------------------

	/**
	 * 
	 * @param document
	 * @throws SAXException
	 */
	private void parse(Document document) throws SAXException {
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equals(NODE_CLASS)) {
				parseClassDefinition((Element) nextNode);
			}
		}
	}

	/**
	 * 
	 * @param classNode
	 * @throws SAXException
	 */
	private void parseClassDefinition(Element classNode) throws SAXException {

		ClassDefinition classDefinition = new ClassDefinition();

		String className = getStringAttribute(classNode, ATTR_CLASS_NAME, null);
		classDefinition.setClassName(className);

		String table = getStringAttribute(classNode, ATTR_CLASS_TABLE, null);
		classDefinition.setTable(table);

		Node pkdefNode = getFirstChild(classNode, NODE_PKDEF, false);
		if (pkdefNode != null) {
			String type = getStringAttribute(pkdefNode, ATTR_PKDEF_TYPE, null);
			String value = getStringAttribute(pkdefNode, ATTR_PKDEF_VALUE, null);
			UIDDefinition uidDefinition = new UIDDefinition();
			uidDefinition.setType(type);
			uidDefinition.setValue(value);
			classDefinition.setUidDefinition(uidDefinition);
		}
		
		Node extendsNode = getFirstChild(classNode, NODE_EXTENDS, false);
		if (extendsNode != null) {
			ReferenceDefinition parentDefinition = new ReferenceDefinition();
			parentDefinition.setName(PARENT_RELATION_NAME);

			String parentClassName = getStringAttribute(extendsNode, ATTR_EXTENDS_CLASS, null);
			parentDefinition.setClassName(parentClassName);
			parentDefinition.setMinOccurs(1);
			parentDefinition.setMaxOccurs(1);

			Node refkeyNode = getFirstChild(extendsNode, NODE_REF_KEY, true);
			String columnName = getStringAttribute(refkeyNode, ATTR_REF_KEY_COLNAME, null);
			parentDefinition.setColumnName(columnName);
			String onPropertyName = getStringAttribute(refkeyNode, ATTR_REF_KEY_ONPROPERTY, null);
			parentDefinition.setOnProperty(onPropertyName);
			String joinTable = getStringAttribute(refkeyNode, ATTR_REF_KEY_JOINTABLE, null);
			parentDefinition.setJoinTable(joinTable);
			String joinColumn = getStringAttribute(refkeyNode, ATTR_REF_KEY_JOINCOLUMN, null);
			parentDefinition.setJoinColumn(joinColumn);

			classDefinition.setParentDefinition(parentDefinition);
		}

		Node properties = getFirstChild(classNode, NODE_PROPERTIES, false);
		if (properties != null) {
			PropertySetDefinition propertySet = getPropertySet(properties);
			propertySet.setTable(classDefinition.getTable());
			classDefinition.setProperties(propertySet);
			propertySet.setJoinColumn(classDefinition.getPrimaryKey().getColumnName());
		}

		NodeList additionalProperties = classNode.getElementsByTagName(NODE_ADDPROPERTIES);
		if ((additionalProperties != null) && (additionalProperties.getLength() > 0)) {
			for (int i = 0; i < additionalProperties.getLength(); i++) {
				PropertySetDefinition propertySet = getPropertySet(additionalProperties.item(i));
				classDefinition.addAdditionalProperties(propertySet);
			}
		}

		Node referenceNode = getFirstChild(classNode, NODE_REFERENCES, false);
		if (referenceNode != null) {
			loadReferences(referenceNode, classDefinition);
		}

		defMap.put(classDefinition.getClassName(), classDefinition);
	}

	/**
	 * 
	 * @param propertiesNode
	 * @return PropertySetDefinition
	 * @throws SAXException
	 */
	private PropertySetDefinition getPropertySet(Node propertiesNode) throws SAXException {
		PropertySetDefinition propertySet = new PropertySetDefinition();

		String name = getStringAttribute(propertiesNode, NODE_ADDPROPERTIES_NAME, null);
		propertySet.setName(name);

		String table = getStringAttribute(propertiesNode, NODE_ADDPROPERTIES_TABLE, null);
		propertySet.setTable(table);

		String joinColumn = getStringAttribute(propertiesNode, NODE_ADDPROPERTIES_JOINCOLUMN, null);
		propertySet.setJoinColumn(joinColumn);

		String onProp = getStringAttribute(propertiesNode, NODE_ADDPROPERTIES_ONPROP, null);
		propertySet.setOnProperty(onProp);

		NodeList nodeList = propertiesNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);
			if (nextNode.getNodeName().equals(NODE_PROPERTY)) {
				PropertyDefinition propertyDefinition = new PropertyDefinition();

				String propertyName = getStringAttribute(nextNode, ATTR_PROP_NAME, null);
				propertyDefinition.setName(propertyName);

				String columnName = getStringAttribute(nextNode, ATTR_PROP_COLUMN_NAME, null);
				propertyDefinition.setColumnName(columnName);

				String type = getStringAttribute(nextNode, ATTR_PROP_TYPE, null);
				if ((type == null) || (!PropertyDefinition.isValidType(type))) {
					throw new SAXException(resHash.getString("DPA.CONN.DATA.TYPE.INVALID", new Object[] {type}));
				}
				propertyDefinition.setType(type);
				
				String nativeType = getStringAttribute(nextNode, ATTR_PROP_NATIVE_TYPE, null);
				if (nativeType != null) {
					propertyDefinition.setNativeType(nativeType);
				}
				propertyDefinition.setType(type);

				boolean required = getBooleanAttribute(nextNode, ATTR_PROP_REQUIRED, false);
				propertyDefinition.setRequired(required);

				boolean unique = getBooleanAttribute(nextNode, ATTR_PROP_UNIQUE, false);
				propertyDefinition.setUnique(unique);
				
				boolean primary = getBooleanAttribute(nextNode, ATTR_PROP_PRIMARY, false);
				propertyDefinition.setPrimary(primary);
				
				propertySet.addProperty(propertyDefinition);
			}
		}

		return propertySet;
	}

	/**
	 * 
	 * @param referencesNode
	 * @param classDefinition
	 * @throws SAXException
	 */
	private void loadReferences(Node referencesNode, ClassDefinition classDefinition) throws SAXException {
		NodeList nodeList = referencesNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nextNode = nodeList.item(i);

			if (nextNode.getNodeName().equals(NODE_REFERENCE)) {
				ReferenceDefinition referenceDefinition = new ReferenceDefinition();

				String name = getStringAttribute(nextNode, ATTR_REF_NAME, null);
				referenceDefinition.setName(name);

				String className = getStringAttribute(nextNode, ATTR_REF_CLASS, null);
				referenceDefinition.setClassName(className);

				String type = getStringAttribute(nextNode, ATTR_REF_TYPE, null);
				referenceDefinition.setType(type);
				
				int minOccurs = getIntAttribute(nextNode, ATTR_REF_MIN, 0);
				referenceDefinition.setMinOccurs(minOccurs);

				int maxOccurs = getIntAttribute(nextNode, ATTR_REF_MAX, 0);
				referenceDefinition.setMaxOccurs(maxOccurs);

				String reversePK = getStringAttribute(nextNode, ATTR_REF_REVPK, "false");
				referenceDefinition.setReversePrimaryKey("true".equalsIgnoreCase(reversePK));

				Node refkeyNode = getFirstChild(nextNode, NODE_REF_KEY, true);
				String columnName = getStringAttribute(refkeyNode, ATTR_REF_KEY_COLNAME, null);
				referenceDefinition.setColumnName(columnName);
				String onPropertyName = getStringAttribute(refkeyNode, ATTR_REF_KEY_ONPROPERTY, null);
				referenceDefinition.setOnProperty(onPropertyName);

				classDefinition.addReference(referenceDefinition);
			}
		}
	}

	/**
	 * 
	 * @param node
	 * @param attributeName
	 * @param defaultValue
	 * @return boolean
	 */
	private boolean getBooleanAttribute(Node node, String attributeName, boolean defaultValue) {
		NamedNodeMap attributes = node.getAttributes();
		Node attrNode = attributes.getNamedItem(attributeName);
		if (attrNode != null) {
			String value = attrNode.getNodeValue();
			if (value != null) {
				return value.equalsIgnoreCase("true");
			}
		}

		return defaultValue;
	}

	/**
	 * 
	 * @param node
	 * @param attributeName
	 * @param defaultValue
	 * @return String
	 */
	private String getStringAttribute(Node node, String attributeName, String defaultValue) {
		NamedNodeMap attributes = node.getAttributes();
		Node attrNode = attributes.getNamedItem(attributeName);
		if (attrNode != null) {
			return attrNode.getNodeValue();
		}

		return defaultValue;
	}

	/**
	 * 
	 * @param node
	 * @param attributeName
	 * @param defaultValue
	 * @return int
	 */
	private int getIntAttribute(Node node, String attributeName, int defaultValue) {
		NamedNodeMap attributes = node.getAttributes();
		Node attrNode = attributes.getNamedItem(attributeName);
		if (attrNode != null) {
			String value = attrNode.getNodeValue();
			if (value != null) {
				return Integer.parseInt(value);
			}
		}

		return defaultValue;
	}

	/**
	 * 
	 * @param node
	 * @param childNodeName
	 * @param required
	 * @return Node
	 * @throws SAXException
	 */
	private Node getFirstChild(Node node, String childNodeName, boolean required) throws SAXException {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node nextNode = childNodes.item(i);
			if (nextNode.getNodeName().equals(childNodeName)) {
				return nextNode;
			}
		}

		if (required) {
			throw new SAXException(resHash.getString("DPA.CONN.CHILD.MISSING", new Object[] {childNodeName, node.getNodeName()}));
		}

		return null;
	}

}
