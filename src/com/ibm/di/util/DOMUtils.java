/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class DOMUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Document doc;
	static {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			doc = dbf.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			// should not happen
		}
	}

	public static final Element getElementByName(Element parent, String localName, String ns) {
		if (parent != null) {
			NodeList children = parent.getChildNodes();

			Node n = null;
			for (int i = 0; i < children.getLength(); i++) {
				n = children.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE && n.getNamespaceURI().equals(ns) && n.getLocalName().equals(localName)) {
					return (Element) n;
				}
			}
		}
		return null;
	}

	public static final Element getElementByName(List<Element> children, String localName, String ns) {
		if (children != null) {
			Node n = null;
			for (int i = 0; i < children.size(); i++) {
				n = children.get(i);
				if (n.getNodeType() == Node.ELEMENT_NODE && n.getNamespaceURI().equals(ns) && n.getLocalName().equals(localName)) {
					return (Element) n;
				}
			}
		}
		return null;
	}

	public static final List<Element> getAllElementsWithName(Element parent, String localName, String ns) {

		List<Element> result = new ArrayList<Element>();

		if (parent != null) {
			NodeList children = parent.getChildNodes();
			for (int i = 0; i < children.getLength(); ++i) {
				Node n = children.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE && localName.equals(n.getLocalName()) && ns != null
						&& ns.equals(n.getNamespaceURI())) {
					result.add((Element) n);
				}
			}
		}

		return result;
	}

	public static final Element getElementWithAttribute(List<Element> elements, String attrName, String attrNS, String attrValue) {
		for (Element e : elements) {
			if (attrValue.equals(e.getAttributeNS(attrNS, attrName))) {
				return e;
			}
		}
		return null;
	}

	public static Element parseString(String str) throws Exception {
		return getDOMParser().parse(new ByteArrayInputStream(str.getBytes("UTF-8"))).getDocumentElement();
	}

	public static String elementToString(Element element) throws Exception {
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		StringWriter stringWriter = new StringWriter();
		t.transform(new DOMSource(element), new StreamResult(stringWriter));
		return stringWriter.toString();
	}

	public static DocumentBuilder getDOMParser() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		return factory.newDocumentBuilder();
	}
}
