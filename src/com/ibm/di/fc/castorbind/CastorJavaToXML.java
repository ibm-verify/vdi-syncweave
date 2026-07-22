/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This is the CastorJavaToXML class. This is the main class of the
 * CastorJavaToXML function component. The CastorJavaToXML Function Component
 * creates an XML document from a Java object or an Entry object.
 */
public class CastorJavaToXML extends Function {
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
	private static final String PARAM_USE_ATTRIBUTE_NAMES = "useAttributeNames";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_ROOT_ELEMENT_NAME = "rootElementName";
	/**
	 * Parameter name.
	 */
	private static final String PARAM_RETURN_XML_TYPE = "returnXMLType";

	/**
	 * Default value for the root element - {@link Entry}
	 */
	private static final String DEFAULT_ROOT_ELEMENT = "Entry";

	/**
	 * Attribute name.
	 */
	private static final String OUT_ATTR_XML_STRING = "xmlString";
	/**
	 * Attribute name.
	 */
	private static final String OUT_ATTR_XML_DOM = "xmlDOMElement";
	/**
	 * Parameter name for return type.
	 */
	private static final String RETURN_XML_STRING = "String";
	/**
	 * Parameter name for return type.
	 */
	private static final String RETURN_XML_DOM = "DOMElement";

	/**
	 * Mapping file value.
	 */
	private String mMappingFile = null;

	/**
	 * if this flag is set to true, the names of the Attributes are used as XML
	 * element names, otherwise the XML elements are named as specified in the
	 * Mapping File.
	 */
	private boolean mUseAttributeNames = false;

	/**
	 * The value of the root element of the generated XML.
	 */
	private String mRootElementName = null;

	/**
	 * value of return element DOM/String.
	 */
	private String mReturnXMLType = null;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "castorjavatoxml";

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
							.getString("CASTOR.JAVA.TO.XML.FC.MAPPING.FILE.NOT.SPECIFIED"));
		}

		mUseAttributeNames = Boolean.valueOf(
				(String) getParam(PARAM_USE_ATTRIBUTE_NAMES)).booleanValue();

		mRootElementName = (String) getParam(PARAM_ROOT_ELEMENT_NAME);
		if (mRootElementName != null) {
			mRootElementName = mRootElementName.trim();
		}
		if (mRootElementName == null || mRootElementName.length() == 0) {
			mRootElementName = DEFAULT_ROOT_ELEMENT;
		}

		mReturnXMLType = (String) getParam(PARAM_RETURN_XML_TYPE);
		if (mReturnXMLType != null) {
			mReturnXMLType = mReturnXMLType.trim();
		}
		if (mReturnXMLType == null
				|| (!mReturnXMLType.equalsIgnoreCase(RETURN_XML_DOM) && !mReturnXMLType
						.equalsIgnoreCase(RETURN_XML_STRING))) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.IVALID.RETURN.TYPE", new Object[] {
							PARAM_RETURN_XML_TYPE, mReturnXMLType }));
		}

		super.initialize(null);
	}

	/**
	 * The CastorJavaToXML Function Component can operate both with Entry
	 * objects and with custom Java objects.
	 * 
	 * When the Function Component is passed an Entry object on input, it will
	 * return an Entry object. This mode of operation is called Entry mode. In
	 * Entry mode each Attribute of the Entry passed on input is marshaled and
	 * placed under the root of the resulting XML element. If the
	 * "returnXMLType" parameter is set to "DOMElement" the resulting Entry
	 * contains one attribute named "xmlDOMElement" and its value is the
	 * marshaled XML element as an "org.w3c.dom.Element" object. If the
	 * "returnXMLType" parameter is set to "String" the resulting Entry contains
	 * one attribute named "xmlString" and its value is the marshaled XML
	 * element as a "java.lang.String" object.
	 * 
	 * When passed a Java object which is not an Entry object, the Function
	 * Component will serialize the object passed using Castor serialization and
	 * this will be the result XML. This mode of operation is called non-Entry
	 * mode. If the "returnXMLType" parameter is set to "DOMElement" the
	 * resulting XML element is returned as a "org.w3c.dom.Element" object. If
	 * the "returnXMLType" parameter is set to "String" the XML element is
	 * returned as a "java.lang.String" object.
	 * 
	 * @param obj
	 *            this is the input object passed to the FC.
	 * @return Returns DOMElement or String object depending on the
	 *         returnXMLType specified by the FC.
	 * @throws Exception
	 *             If initialization is not performed or if an Exception occur
	 *             during the marshaling process.
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		Object result = null;
		if (obj instanceof Entry) {
			result = marshal((Entry) obj, mMappingFile, mRootElementName,
					mUseAttributeNames);
		} else {
			result = marshal(obj, mMappingFile);
		}

		return result;
	}

	/**
	 * This method is called when the CastorJavaToXML FC is passed an Entry
	 * object. Each Attribute of the Entry passed on input is marshaled and
	 * placed under the root of the resulting XML element.
	 * 
	 * @param aEntry
	 *            the input data as an Entry object.
	 * @param aMappingFile
	 *            the path to the Castor XML Mapping File that defines mapping
	 *            rules.
	 * @param aRootElementName
	 *            This parameter specifies the name of the root element of the
	 *            generated XML.
	 * @param aUseAttrNames
	 *            When set to "true" the names of the Entry Attributes are used
	 *            as XML element names, otherwise the XML elements are named as
	 *            specified in the mapping file.
	 * @return Returns DOMElement or String object depending on the
	 *         returnXMLType specified by the FC.
	 * @throws Exception
	 *             If an Exception occurs during the object marshaling.
	 */
	public Entry marshal(Entry aEntry, String aMappingFile,
			String aRootElementName, boolean aUseAttrNames) throws Exception {
		Mapping mapping = new Mapping(getClass().getClassLoader());
		try {
			mapping.loadMapping(aMappingFile);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.COULD.NOT.LOAD.FILE", e.toString()));
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.COULD.NOT.CREATE.BUILDER", e
							.toString()));
		}

		Document doc = builder.newDocument();
		String rootElementName;
		if (aRootElementName != null) {
			rootElementName = aRootElementName;
		} else {
			rootElementName = DEFAULT_ROOT_ELEMENT;
		}
		Element root = null;
		try {
			root = doc.createElement(rootElementName);
			doc.appendChild(root);
		} catch (DOMException e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.INVALID.ROOT.ELEMENT", e.toString()));
		}

		try {
			String[] attributeNames = aEntry.getAttributeNames();
			for (int i = 0; i < attributeNames.length; i++) {
				Attribute attribute = aEntry.get(attributeNames[i]);
				Object[] values = attribute.getValues();
				for (int j = 0; j < values.length; j++) {
					Element element = doc.createElement("any");
					Marshaller marshaller = new Marshaller(element);
					marshaller.setMapping(mapping);
					marshaller.marshal(values[j]);
					if (aUseAttrNames) {
						Node oldNode = element.getFirstChild();
						root.appendChild(renameNode(oldNode, attributeNames[i],
								true));
					} else {
						root.appendChild(element.getFirstChild());
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.ERROR.ON.MARSHALING1", e.toString()));
		}

		Entry result = new Entry();
		if (mReturnXMLType.equalsIgnoreCase(RETURN_XML_DOM)) {
			result.addAttributeValue(OUT_ATTR_XML_DOM, root);
		} else if (mReturnXMLType.equalsIgnoreCase(RETURN_XML_STRING)) {
			StringWriter sw = new StringWriter();
			try {
				Transformer trans = TransformerFactory.newInstance()
						.newTransformer();
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.transform(new DOMSource(root), new StreamResult(sw));
			} catch (Exception e) {
				throw new Exception(sResHash.getString(
						"CASTOR.JAVA.TO.XML.FC.ERROR.TRANSFORMING.DOM1", e
								.toString()));
			}

			result.addAttributeValue(OUT_ATTR_XML_STRING, sw.toString());
		}

		return result;
	}

	/**
	 * This method is called when the CastorJavaToXML FC is passed a Java object
	 * which is not an Entry object, the metho will serialize the object passed
	 * using Castor serialization and this will be the result XML.
	 * 
	 * @param aObject
	 *            the input data as a java.lang.Object.
	 * @param aMappingFile
	 *            the path to the Castor XML Mapping File that defines mapping
	 *            rules.
	 * @return Returns DOMElement or String object depending on the
	 *         returnXMLType specified by the FC.
	 * @throws Exception
	 *             If an Exception occurs during the object marshaling.
	 */
	public Object marshal(Object aObject, String aMappingFile) throws Exception {
		Mapping mapping = new Mapping(getClass().getClassLoader());

		try {
			mapping.loadMapping(aMappingFile);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.ERROR.LOADING.MAPPING.FILE", e
							.toString()));
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.ERROR.CREATING.DOCUMENT.BUILDER", e
							.toString()));
		}

		Document doc = null;
		Element root = null;
		try {
			doc = builder.newDocument();
			if (mRootElementName != null) {
				root = doc.createElement(mRootElementName);
			} else {
				root = doc.createElement(DEFAULT_ROOT_ELEMENT);
			}

			Marshaller marshaller = new Marshaller(root);
			marshaller.setMapping(mapping);
			marshaller.marshal(aObject);
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CASTOR.JAVA.TO.XML.FC.ERROR.ON.MARSHALING2", e.toString()));
		}

		if (mReturnXMLType.equalsIgnoreCase(RETURN_XML_DOM)) {
			return root;
		} else if (mReturnXMLType.equalsIgnoreCase(RETURN_XML_STRING)) {
			StringWriter sw = new StringWriter();
			try {
				Transformer trans = TransformerFactory.newInstance()
						.newTransformer();
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.transform(new DOMSource(root.getFirstChild()),
						new StreamResult(sw));
			} catch (Exception e) {
				throw new Exception(sResHash.getString(
						"CASTOR.JAVA.TO.XML.FC.ERROR.TRANSFORMING.DOM2", e
								.toString()));
			}

			return sw.toString();
		} else {
			throw new Exception(sResHash
					.getString("CASTOR.JAVA.TO.XML.FC.INVALID.RETURN.TYPE",
							mReturnXMLType));
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
		return "2.0-di7.1.1 %I% 20%E%";
	}

}
