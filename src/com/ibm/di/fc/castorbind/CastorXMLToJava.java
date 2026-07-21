/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.ibm.icu.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This is the CastorXMLToJava class. The CastorXMLToJava Function Component
 * creates an Entry or a general Java object from an XML document.
 * CastorXMLToJava provides the option to get data from certain parts of the XML
 * tree when unmarshalling the XML document.
 */
public class CastorXMLToJava extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Parameter name.
	 */
	private static final String PARAM_MAPPING_FILE = "mappingFile";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_ATTRIBUTE_SPECS = "attributeSpecs";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_INPUT_TYPE = "inputXMLType";

	/**
	 * Input type 'String'.
	 */
	private static final String INPUT_TYPE_XML_STRING = "String";

	/**
	 * Input type 'DOMElement'.
	 */
	private static final String INPUT_TYPE_XML_DOM = "DOMElement";

	/**
	 * Attribute name.
	 */
	private static final String MAP_ATTRIBUTE_NAME = "Name";
	/**
	 * Attribute name.
	 */
	private static final String MAP_ATTRIBUTE_XPATH = "XPath";
	/**
	 * Attribute name.
	 */
	private static final String MAP_ATTRIBUTE_SPECIAL_TYPE = "SpecialType";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTR_XML_STRING = "xmlString";
	/**
	 * Attribute name.
	 */
	private static final String IN_ATTR_XML_DOM = "xmlDOMElement";

	/**
	 * Mapping file value
	 */
	private String mMappingFile = null;

	/**
	 * Input type value STRIN/DOM
	 */
	private String mInputType = null;

	/**
	 * Attribute Specs value.
	 */
	private List mAttributeSpecs = null;

	/**
	 * Component property.
	 */
	private static final String PROPERTIES_FILE = "castorxmltojava";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * In the initialize method the FC initializes its parameters.
	 * 
	 * @param obj
	 *            not used.
	 * @throws Exception
	 *             If the mapping file parameter is missing or the return type
	 *             is not valid.
	 */
	public void initialize(Object obj) throws Exception {
		mMappingFile = (String) getParam(PARAM_MAPPING_FILE);
		if (mMappingFile != null) {
			mMappingFile = mMappingFile.trim();
		}
		if (mMappingFile == null || mMappingFile.length() == 0) {
			throw new Exception(
					sResHash
							.getString("CASTOR.XML.TO.JAVA.FC.MAPPING.FILE.NOT.SPECIFIED"));
		}

		parseAttributeSpecs();

		mInputType = (String) getParam(PARAM_INPUT_TYPE);
		if (mInputType != null) {
			mInputType = mInputType.trim();
		}
		if (mInputType == null
				|| (!mInputType.equalsIgnoreCase(INPUT_TYPE_XML_DOM) && !mInputType
						.equalsIgnoreCase(INPUT_TYPE_XML_STRING))) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.IVALID.RETURN.TYPE", new Object[] {
							PARAM_INPUT_TYPE, mInputType }));
		}

		super.initialize(null);
	}

	/**
	 * The method parses attributes specs.
	 * 
	 * @throws Exception
	 *             if parsing is not successful.
	 */
	private void parseAttributeSpecs() throws Exception {
		String attributeSpecsStr = (String) getParam(PARAM_ATTRIBUTE_SPECS);
		if (attributeSpecsStr != null) {
			attributeSpecsStr = attributeSpecsStr.trim();
		}

		if (attributeSpecsStr == null) {
			mAttributeSpecs = null;
			return;
		}

		mAttributeSpecs = new Vector();
		StringTokenizer stAttr = new StringTokenizer(attributeSpecsStr, "\n\r");
		while (stAttr.hasMoreTokens()) {
			String attributeToken = stAttr.nextToken();
			StringTokenizer stItem = new StringTokenizer(attributeToken, ",");

			if (!stItem.hasMoreTokens()) {
				throw new Exception(sResHash.getString(
						"CASTOR.XML.TO.JAVA.FC.NO.MORE.TOKENS",
						attributeSpecsStr));
			}
			String attributeName = stItem.nextToken().trim();

			if (!stItem.hasMoreTokens()) {
				throw new Exception(sResHash.getString(
						"CASTOR.XML.TO.JAVA.FC.XPATH.NOT.SPECIFIED",
						attributeName));
			}
			String xPathQuery = stItem.nextToken().trim();

			String specialType = null;
			if (stItem.hasMoreTokens()) {
				specialType = stItem.nextToken().trim();
			}

			Hashtable map = new Hashtable();
			map.put(MAP_ATTRIBUTE_NAME, attributeName);
			map.put(MAP_ATTRIBUTE_XPATH, xPathQuery);
			if (specialType != null) {
				map.put(MAP_ATTRIBUTE_SPECIAL_TYPE, specialType);
			}
			mAttributeSpecs.add(map);
		}
	}

	/**
	 * This method retruns an XML document from a Java object or an Entry
	 * object.
	 * 
	 * When the method is passed an Entry object on input, it will return an
	 * Entry object. This mode of operation is called Entry mode. If the
	 * "inputXMLType" parameter specifies "DOMElement", the method will expect
	 * on input an Entry with an Attribute named "xmlDOMElement" with value of
	 * type "org.w3c.dom.Element". If the "inputXMLType" parameter specifies
	 * "String", an Entry with an Attribute named "xmlString" and value of type
	 * "java.lang.String" is expected on input. The output generated is an Entry
	 * whose Attributes are the unmarshalled XML elements as specified by the
	 * "attributeSpecs" parameter and the mapping file.
	 * 
	 * When the method is passed an object that is not an Entry on input (string
	 * or a DOM element) it returns the raw Java object as it is unmarshalled by
	 * Castor. This mode of operation is called non-Entry mode. If the
	 * "inputXMLType" parameter specifies "DOMElement", the method will expect
	 * on input a "org.w3c.dom.Element" object. If the "inputXMLType" parameter
	 * specifies "String", a "java.lang.String" object is expected on input.
	 * 
	 * @param obj
	 *            this is the input data passed to the FC.
	 * @return Returns an Entry or a Java object depending on the input data.
	 * @throws Exception
	 *             If initialization is not performed or if an Exception occur
	 *             during the marshaling process.
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		Object result = null;
		if (obj instanceof Entry) {
			result = unmarshal((Entry) obj, mMappingFile, mAttributeSpecs);
		} else {
			result = unmarshal(obj, mMappingFile);
		}

		return result;
	}

	/**
	 * This method is called when the CastorXMLToJava FC is passed an Entry
	 * object. The output generated is an Entry whose Attributes are the
	 * unmarshalled XML elements as specified by the "attributeSpecs" parameter
	 * and the mapping file.
	 * 
	 * @param aEntry
	 *            the input data as an Entry object.
	 * @param aMappingFile
	 *            the path to the Castor XML Mapping File that defines mapping
	 *            rules.
	 * @param aAttributeList
	 *            List of attributes which contains the attributes specified in
	 *            the Attribute Specification section of the FC.
	 * @return Returns an Entry object which contains the unmarshalled XML.
	 * @throws Exception
	 *             If an Exception occurs during the object unmarshaling.
	 */
	public Entry unmarshal(Entry aEntry, String aMappingFile,
			List aAttributeList) throws Exception {
		Entry entry = new Entry();

		// load the mapping file
		Mapping mapping = new Mapping(getClass().getClassLoader());
		try {
			mapping.loadMapping(aMappingFile);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.ERROR.LOADING.MAPPING.FILE", e
							.toString()));
		}

		// create and configure the unmarshaller
		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = new Unmarshaller(mapping);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.ERROR.CREATING.UNMARSHALLER", e
							.toString()));
		}
		unmarshaller.setIgnoreExtraElements(true);
		unmarshaller.setIgnoreExtraAttributes(true);

		Element xmlElement = null;
		if (mInputType.equalsIgnoreCase(INPUT_TYPE_XML_DOM)) {
			xmlElement = (Element) aEntry.getObject(IN_ATTR_XML_DOM);
		} else if (mInputType.equalsIgnoreCase(INPUT_TYPE_XML_STRING)) {
			String xmlString = (String) aEntry.getObject(IN_ATTR_XML_STRING);
			if (xmlString != null) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = null;
				try {
					builder = factory.newDocumentBuilder();
					InputSource inputSource = new InputSource(new StringReader(
							xmlString));
					Document doc = builder.parse(inputSource);
					xmlElement = doc.getDocumentElement();
				} catch (Exception e) {
					throw new Exception(sResHash.getString(
							"CASTOR.XML.TO.JAVA.FC.ERROR.CREATING.DOCUMENT", e
									.toString()));
				}
			}
		}
		if (xmlElement == null) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.NO.INPUT.DATA", mInputType));
		}

		if (aAttributeList != null && aAttributeList.size() > 0) {
			Iterator iterator = aAttributeList.iterator();
			while (iterator.hasNext()) {
				Map map = (Map) iterator.next();
				String attributeName = (String) map.get(MAP_ATTRIBUTE_NAME);
				String attributePath = (String) map.get(MAP_ATTRIBUTE_XPATH);
				String attributeSpecial = (String) map
						.get(MAP_ATTRIBUTE_SPECIAL_TYPE);
				if ((attributeSpecial != null)
						&& (!TypeWrapper.isSupported(attributeSpecial))) {
					throw new Exception(sResHash.getString(
							"CASTOR.XML.TO.JAVA.FC.ATTRIBUTE.NOT.SUPPORTED",
							new Object[] { attributeSpecial, attributeName }));
				}

				NodeList nodeList = null;
				try {
					nodeList = XPathAPI.selectNodeList(xmlElement,
							attributePath);
				} catch (TransformerException e) {
					throw new Exception(sResHash.getString(
							"CASTOR.XML.TO.JAVA.FC.UNABLE.TO.SELECT.ATTRIBUTE",
							new Object[] { attributePath, e.toString() }));
				}

				if (nodeList.getLength() == 0) {
					continue;
				}

				Attribute attribute = entry.newAttribute(attributeName);
				for (int i = 0; i < nodeList.getLength(); i++) {
					Object attributeValue = null;
					try {
						if (attributeSpecial == null) {
							attributeValue = unmarshaller.unmarshal(nodeList
									.item(i));
						} else {
							attributeValue = unmarshalWrapped(nodeList.item(i),
									attributeSpecial, unmarshaller);
						}
					} catch (Exception e) {
						throw new Exception(sResHash.getString(
								"CASTOR.XML.TO.JAVA.FC.ERROR.ON.UNMARSHALING",
								e.toString()));
					}
					if (attributeValue != null) {
						attribute.addValue(attributeValue);
					}
				}
			}
		} else {
			// todo: log that we are operating in auto mode
			NodeList nodeList = null;
			try {
				nodeList = XPathAPI.selectNodeList(xmlElement, "*");
			} catch (Exception e) {
				throw new Exception(sResHash.getString(
						"CASTOR.XML.TO.JAVA.FC.ERROR.SELECTING.XML", e
								.toString()));
			}

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Object attributeValue = null;
				try {
					node = checkForSupportedArray(node);
					if (TypeWrapper.isSupported(node.getNodeName())) {
						attributeValue = unmarshalWrapped(node, node
								.getNodeName(), unmarshaller);
					} else {
						attributeValue = unmarshaller.unmarshal(node);
					}
				} catch (Exception e) {
					throw new Exception(sResHash.getString(
							"CASTOR.XML.TO.JAVA.FC.ERROR.ON.UNMARSHALING", e
									.toString()));
				}

				if (attributeValue != null) {
					entry.addAttributeValue(node.getNodeName(), attributeValue);
				}
			}
		}
		return entry;
	}

	/**
	 * This method is called when the CastorXMLToJava FC is passed a
	 * java.lang.Object.
	 * 
	 * @param aInputXML
	 *            the input data as a java.lang.Object.
	 * @param aMappingFile
	 *            the path to the Castor XML Mapping File that defines mapping
	 *            rules.
	 * @return Returns the raw Java object as it is unmarshalled by Castor.
	 * @throws Exception
	 *             If an Exception occurs during the object unmarshaling.
	 */
	public Object unmarshal(Object aInputXML, String aMappingFile)
			throws Exception {
		Mapping mapping = new Mapping(getClass().getClassLoader());
		try {
			mapping.loadMapping(aMappingFile);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.ERROR.LOADING.MAPPING.FILE", e
							.toString()));
		}

		Unmarshaller unmarshaller = null;
		try {
			unmarshaller = new Unmarshaller(mapping);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.ERROR.CREATING.UNMARSHALLER", e
							.toString()));
		}
		unmarshaller.setIgnoreExtraElements(true);
		unmarshaller.setIgnoreExtraAttributes(true);

		Node xmlElement = null;
		try {
			if (mInputType.equalsIgnoreCase(INPUT_TYPE_XML_DOM)) {
				xmlElement = (Node) aInputXML;
			} else if (mInputType.equalsIgnoreCase(INPUT_TYPE_XML_STRING)) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = null;
				Document doc = null;
				try {
					builder = factory.newDocumentBuilder();
					InputSource inputSource = new InputSource(new StringReader(
							(String) aInputXML));
					doc = builder.parse(inputSource);
				} catch (Exception e) {
					throw new Exception(sResHash.getString(
							"CASTOR.XML.TO.JAVA.FC.ERROR.CREATING.DOCUMENT", e
									.toString()));
				}
				xmlElement = doc.getDocumentElement();
			}
		} catch (ClassCastException e) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.INVALID.INPUT.DATA", new Object[] {
							e.toString(), mInputType }));
		}
		if (xmlElement == null) {
			throw new Exception(sResHash.getString(
					"CASTOR.XML.TO.JAVA.FC.NO.INPUT.DATA", mInputType));
		}

		Object result = null;
		try {
			xmlElement = checkForSupportedArray(xmlElement);
			if (TypeWrapper.isSupported(xmlElement.getNodeName())) {
				result = unmarshalWrapped(xmlElement, xmlElement.getNodeName(),
						unmarshaller);
			} else {
				result = unmarshaller.unmarshal(xmlElement);
			}
		} catch (Exception e) {
			throw new Exception(sResHash
					.getString("CASTOR.XML.TO.JAVA.FC.ERROR.ON.UNMARSHALING", e
							.toString()));
		}

		return result;
	}

	/**
	 * The method creates a new {@link Document} from the specified {@link Node},
	 * renames and adds the node aNode to the end of the list of children of
	 * this node , unmarshals the {@link Element} created from the
	 * {@link Document} into a {@link TypeWrapper} object , extracts the real
	 * object and returns it.
	 * 
	 * @param aNode
	 *            {@link Node}
	 * @param aName
	 *            name for renaming the aNode
	 * @param aUnmar
	 *            {@link Unmarshaller}
	 * @return Object - the unmarshalled object
	 * @throws Exception
	 *             if an error occurs
	 */
	private Object unmarshalWrapped(Node aNode, String aName,
			Unmarshaller aUnmar) throws Exception {
		Document doc = aNode.getOwnerDocument();
		Element newElement = doc.createElement("typewrapper");
		newElement.appendChild(renameNode(aNode, aName, false));

		TypeWrapper wrapper = (TypeWrapper) aUnmar.unmarshal(newElement);

		return wrapper.getObject(aName);
	}

	/**
	 * Checks whether the name of the {@link Node} contains array , if so
	 * renames it with the array type supported by the castor.
	 * 
	 * @see StringArrayWrapper
	 * @see CharArrayWrapper
	 * @see ByteArrayWrapper
	 * @param aNode
	 *            the {@link Node} that has to be checked
	 * @return Node the argument if the node name is not array , or the renamed
	 *         node with the supported by the castor name.
	 */
	private Node checkForSupportedArray(Node aNode) {
		if (!aNode.getNodeName().equalsIgnoreCase("array")) {
			return aNode;
		}

		NamedNodeMap nodeMap = aNode.getAttributes();
		Node type = nodeMap.getNamedItem("xsi:type");
		boolean supportedType = false;
		String typeNodeName = null;

		if (type != null
				&& type.getNodeValue().equalsIgnoreCase(
						"java:[Ljava.lang.String;")) {
			supportedType = true;
			typeNodeName = TypeWrapper.TYPE_STRINGS;
		}

		if (type != null
				&& (type.getNodeValue().equalsIgnoreCase("java:[C") || type
						.getNodeValue().equalsIgnoreCase(
								"java:[Ljava.lang.Character;"))) {

			supportedType = true;
			typeNodeName = TypeWrapper.TYPE_CHARS;
		}

		if (type != null
				&& type.getNodeValue().equalsIgnoreCase(
						"java:[Ljava.lang.Byte;")) {
			supportedType = true;
			typeNodeName = TypeWrapper.TYPE_BYTES;
		}

		if (!supportedType) {
			return aNode;
		} else {
			return renameNode(aNode, typeNodeName, false);
		}
	}

	/**
	 * Replaces the existing {@link Node} with exact copy with different
	 * name.The method accomplishes renaming of the specified {@link Node}
	 * 
	 * @param aNode
	 *            {@link Node}
	 * @param aName
	 *            new name of the {@link Node}
	 * @param aCopyAttributes
	 *            must be <code>true</code> if the attributes should be copied
	 * @return the renamed Node
	 */
	private Node renameNode(Node aNode, String aName, boolean aCopyAttributes) {
		Element element = aNode.getOwnerDocument().createElement(aName);
		NodeList nodeList = aNode.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			element.appendChild(nodeList.item(i).cloneNode(true));
		}
		if (aCopyAttributes) {
			NamedNodeMap nnm = aNode.getAttributes();
			for (int i = 0; i < nnm.getLength(); i++) {
				element.setAttribute(nnm.item(i).getNodeName(), nnm.item(i)
						.getNodeValue());
			}
		}
		return element;
	}

	/**
	 * Version information.
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

}
