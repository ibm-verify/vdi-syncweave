/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.di.connector.maximo.core.SimpleTpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnXmlParsingException;

/**
 * This class consists exclusively of static methods that operate on or return
 * XML content and DOM elements.
 * 
 * @since 7.1
 */
public final class Dom {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

	/**
	 * Returns a {@link Map map} containing the child nodes found in the given
	 * XML node.
	 * 
	 * @param rootNode
	 *            XML node whose child nodes are to be returned
	 * @return {@link Map map} containing the child nodes found in the given XML
	 *         node
	 */
	public static Map<String, String> getAttributes(final Node rootNode) {
		return getAttributes(rootNode, null);
	}

	/**
	 * Searches the specified XML node for child nodes with the specified names.
	 * 
	 * @param rootNode
	 *            XML node whose child nodes are to be returned
	 * @param names
	 *            names of the attributes to be found
	 * @return a {@link Map map} containing the child nodes found and its
	 *         respective values
	 */
	public static Map<String, String> getAttributes(final Node rootNode, final Set<String> names) {

		final HashMap<String, String> result = new HashMap<String, String>();
		final NodeList nodes = rootNode.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				final String nodeName = node.getNodeName();
				if (names == null || names.contains(nodeName)) {
					result.put(nodeName, getTextValue(node));
				}
			}
		}

		return result;
	}

	/**
	 * Returns the value of the specified attribute.
	 * 
	 * @param node
	 *            node containing the specified attribute
	 * @param attributeName
	 *            attribute's name
	 * @return value of the specified attribute, or <code>null</code> if the
	 *         attribute does not exist
	 */
	public static String getAttributeValue(final Node node, final String attributeName) {

		final NamedNodeMap attributes = node.getAttributes();

		if (attributes != null) {
			final Node attribute = attributes.getNamedItem(attributeName);
			if (attribute != null) {
				return attribute.getNodeValue();
			}
		}

		return null;
	}

	/**
	 * Returns all elements specified by <tt>elementName</tt>.
	 * 
	 * @param elementName
	 *            name of the element to be returned
	 * @param document
	 *            DOM document
	 * @return a list of all elements found with the specified name s
	 */
	public static NodeList getElements(final String elementName, final Document document) {
		return document.getElementsByTagName(elementName);
	}

	/**
	 * Parses the XML content and returns all elements specified by
	 * <tt>elementName</tt>.
	 * 
	 * @param elementName
	 *            name of the element to be returned
	 * @param xml
	 *            XML content to be parsed
	 * @return a list of all elements found with the specified name
	 * @throws MxConnXmlParsingException
	 *             if it is not possible to parse the XML content
	 */
	public static NodeList getElements(final String elementName, final String xml) throws MxConnXmlParsingException {
		return getElements(elementName, parse(xml));
	}

	/**
	 * Returns the text value of the specified XML node.
	 * 
	 * @param node
	 *            XML node
	 * @return text value of the specified XML node
	 */
	public static String getTextValue(final Node node) {

		final Node text = node.getFirstChild();
		if (text == null || text.getNodeType() != Node.TEXT_NODE) {
			return "";
		}
		return text.getNodeValue();
	}

	/**
	 * Indicates if the specified XML node has <code>null</code> value. NOTE:
	 * Not used.
	 * 
	 * @param node
	 *            XML node
	 * @return <code>true</code> if the specified XML node has <code>null</code>
	 *         value, otherwise <code>false</code>
	 */
	public static boolean isNull(final Node node) {

		if (node.getAttributes() != null) {
			final Node attribute = node.getAttributes().getNamedItemNS(XSI_NS, "nil");
			if (attribute != null && "true".equalsIgnoreCase(getTextValue(attribute))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parses the XML content.
	 * 
	 * @param xml
	 *            XML content to be parsed
	 * @return a DOM document
	 * @throws MxConnXmlParsingException
	 *             if it is not possible to parse the XML content
	 */
	public static Document parse(final String xml) throws MxConnXmlParsingException {
		try {
			final InputSource inpSource = new InputSource(new StringReader(xml));
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			final DocumentBuilder builder = factory.newDocumentBuilder();

			return builder.parse(inpSource);
		} catch (final Exception e) {
			throw new MxConnXmlParsingException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.PARSE.XML"), xml, e);
		}
	}
}
