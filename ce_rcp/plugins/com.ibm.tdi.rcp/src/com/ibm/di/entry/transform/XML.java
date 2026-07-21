/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry.transform;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * This class is a temporary class to convert an Entry to/from XML. The new XML parser may
 * replace this class.
 *
 */
public class XML {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static Entry fromXML(String xml) throws Exception {
		return fromXML(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))));
	}
	
	public static Entry fromXML(Document document) throws Exception {
		document.normalizeDocument();
		Element root = document.getDocumentElement();
		return parseEntry(root);
	}
	
	private static Entry parseEntry(Element root) {
		Entry entry = new Entry();
		if(root.getAttribute("operation").length() > 0)
			entry.setOperation(root.getAttribute("operation"));
		parseValues(entry, root);
		return entry;
	}
	
	private static void parseProperty(Entry entry, Element node) {
		String name = node.getAttribute("name");
		ArrayList<Object> list = new ArrayList<Object>();
		parseValues(list, node);
		if(list.size() == 1)
			entry.setProperty(name, list.get(0));
		else
			entry.setProperty(name, list);
	}

	private static Attribute parseAttribute(Element node) {
		Attribute a = new Attribute(node.getAttribute("name"));
		if(node.getAttribute("operation").length() > 0) {
			a.setOperation(node.getAttribute("operation"));
		}
		parseValues(a, node);
		return a;
	}
	
	private static void parseValues(Object target, Element node) {
		NodeList children = node.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node item = children.item(i);
			Object value = null;
			if(item.getNodeName().equals("Attribute")) {
				value = parseAttribute((Element) item);
			} else if(item.getNodeName().equals("Entry")) {
				value = parseEntry((Element) item);
			} else if(item.getNodeName().equals("Property")) {
				if(target instanceof Entry)
					parseProperty((Entry)target, (Element)item);
			} else if(item.getNodeName().equals("value") && target instanceof Attribute) {
				if(item.getFirstChild() != null) {
					String val = item.getFirstChild().getNodeValue();
					if(val != null && target != null)
						((Attribute)target).addValue(item.getFirstChild().getNodeValue());
				}
			} else if(item instanceof org.w3c.dom.Text) {
				String str = item.getNodeValue().trim();
				if(str.length() > 0 || children.getLength() == 1)
					value = item.getNodeValue();
			}
			if(value == null)
				continue;
			
			if(target instanceof Attribute)
				((Attribute)target).addValue(value);
			else if (target instanceof List)
				((List)target).add(value);
			else if (target instanceof Entry && value instanceof Attribute)
				((Entry)target).setAttribute((Attribute)value);
		}
	}

	public static String toXML(Object entryOrAttribute) throws Exception {
		StringWriter sw = new StringWriter();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		
		Entry entry = (Entry) (entryOrAttribute instanceof Entry ? entryOrAttribute : null);
		Attribute attr = (Attribute) (entryOrAttribute instanceof Attribute ? entryOrAttribute : null);

		Element root = doc.createElement("Entry");
		doc.appendChild(root);
		
		if(entry != null) {
			addEntry(root, entry);
		} else {
			buildXMLAttribute(root, attr);
		}
			
		OutputFormat format = new OutputFormat("xml", "UTF-8", true);
		format.setOmitXMLDeclaration(true);
		XMLSerializer serial = new XMLSerializer(sw, format);
		serial.asDOMSerializer();
		serial.serialize(root);
		
		return sw.toString();
	}

	private static void buildXMLAttribute(Element root, Attribute attribute) {
		Element elem = createAttribute(root.getOwnerDocument(), attribute);
		root.appendChild(elem);
		addAttributeValues(elem, attribute);
	}
	
	private static void buildXMLProperty(Element root, String name, Object value) {
		Element elem = root.getOwnerDocument().createElement("Property");
		elem.setAttribute("name", name);
		root.appendChild(elem);
		addValues(elem, value, false);
	}
	
	private static void addEntry(Element elem, Entry entry) {
		Element e = null;
		if(elem.getNodeName().equals("Entry")) {
			e = elem;
		} else {
			e = elem.getOwnerDocument().createElement("Entry");
			elem.appendChild(e);
		}
		
		if(!entry.getOperation().equals(Entry.OP_GEN2))
			e.setAttribute("operation", entry.getOperation());
		
		String[] names = entry.getAttributeNames();
		for(int i = 0; i < names.length; i++)
			buildXMLAttribute(e, entry.getAttribute(names[i]));
		
		names = entry.getPropertyNames();
		for(int i = 0; i < names.length; i++)
			buildXMLProperty(e, names[i], entry.getProperty(names[i]));
	}

	private static void addValues(Element elem, Object value, boolean createElement) {
		if(value instanceof Attribute)
			buildXMLAttribute(elem, (Attribute) value);
		else if(value instanceof Entry)
			addEntry(elem, (Entry)value);
		else if(value != null)
			addStringValue(elem, value.toString(), createElement);
	}

	private static void addStringValue(Element e, String str, boolean createElement) {
		if(str == null)
			return;
		Element elem = e;
		if(createElement) {
			elem = e.getOwnerDocument().createElement("value");
			e.appendChild(elem);
		}
		
		if(str.indexOf("\r") != -1 || str.indexOf("\n") != -1)
			elem.appendChild(elem.getOwnerDocument().createCDATASection(str));
		else
			elem.appendChild(elem.getOwnerDocument().createTextNode(str));
	}

	private static void addAttributeValues(Element target, Attribute attribute) {
 		Object[] values = attribute.getValues();
		for(int i = 0; i < values.length; i++) {
			addValues(target, values[i], true);
		}
	}
	
	private static Element createAttribute(Document doc, Attribute attribute) {
		Element elem = doc.createElement("Attribute");
		elem.setAttribute("name", attribute.getName());
		if(!attribute.getOperation().equals("replace"))
			elem.setAttribute("operation", attribute.getOperation());
		return elem;
	}
}
