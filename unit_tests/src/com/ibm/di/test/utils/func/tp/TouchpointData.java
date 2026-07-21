package com.ibm.di.test.utils.func.tp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Property;
import com.ibm.di.util.DOMUtils;

public class TouchpointData {
	
	public static final String NS_TP = "http://www.ibm.com/xmlns/prod/tdi/71/tp";
	
	private final String xml;
	private final List<Entry> entries;
	
	public TouchpointData(String xml) throws Exception {
		this.xml = xml;
		this.entries = deserialize(xml);
	}
	
	public TouchpointData(List<Entry> entries) throws Exception {
		this.entries = entries;
		this.xml = serialize(entries);
	}
	
	public TouchpointData(Entry entry) throws Exception {
		List<Entry> es = new ArrayList<Entry>();
		es.add(entry);
		this.entries = es;
		this.xml = serialize(entries);
	}
	
	public String getXML() {
		return xml;
	}
	
	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}
	
	private static List<Entry> deserialize(String xml) throws Exception {
		
		if (xml == null || xml.length() == 0) {
			return new ArrayList<Entry>();
		}
		
		Element dataElem = DOMUtils.parseString(xml);

		if (!"data".equals(dataElem.getLocalName())) {
			throw new RuntimeException("Incorrect touchpoint data syntax. Unexpected name of the root element: "+dataElem.getLocalName());
		}
		
		if (!NS_TP.equals(dataElem.getNamespaceURI())) {
			throw new RuntimeException("Incorrect touchpoint data syntax. Unexpected namespace of the root element: "+dataElem.getNamespaceURI());
		}
		
		List<Entry> entries = new ArrayList<Entry>();
		NodeList nodeList = dataElem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Element entryElem = (Element) node;
				Entry entry = xmlToEntry(entryElem);
				entries.add(entry);
			}
		}
		
		return entries;
	}
	
	private static String serialize(List<Entry> entries) throws Exception {
		
		Document doc = DOMUtils.getDOMParser().newDocument();
		
		Element dataElem = doc.createElementNS(NS_TP, "tp:data");
		
		for (Entry entry : entries) {
			Element entryElem = entryToXml(doc, entry);
			dataElem.appendChild(entryElem);
		}
		
		return DOMUtils.elementToString(dataElem);
	}

	public static Element entryToXml(Document doc, Entry entry) throws Exception {
		
		Element entryElem = doc.createElementNS(NS_TP, "tp:entry");
		
		// add the properties
		for (String propName : entry.getPropertyNames()) {
			Object propValueObj = entry.getProperty(propName); 
			String propValue = null;
			if (propValueObj != null) {
				propValue = propValueObj.toString();
			}
			Element propElem = propertyToXml(doc, propName, null, propValue);
			entryElem.appendChild(propElem);
		}
		
		// add the attributes
		NodeList nodeList = entry.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Attribute attr = (Attribute) node;
				Element attrElem = attributeToXml(doc, attr);
				entryElem.appendChild(attrElem);
			} else {
				throw new IllegalArgumentException("Unexpected node type: " + node.getNodeType());
			}
		}
		
		return entryElem;
	}

	public static Entry xmlToEntry(Element entryElem) throws Exception {
		
		final boolean domEnabled = true;
		Entry entry = new Entry(domEnabled);
		
		if (!"entry".equals(entryElem.getLocalName())) {
			throw new IllegalArgumentException("Incorrect entry syntax. Unexpected element name: " + entryElem.getLocalName());
		}

		if (!NS_TP.equals(entryElem.getNamespaceURI())) {
			throw new IllegalArgumentException("Incorrect entry syntax. Unexpected namespace URI: " + entryElem.getNamespaceURI());
		}
		
		NodeList nodeList = entryElem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Element elem = (Element) node;
				if ("attribute".equals(elem.getLocalName())) {
					Attribute attr = xmlToAttribute(entry, elem);
					entry.setAttribute(attr);
				} else if ("property".equals(elem.getLocalName())) {
					Property prop = xmlToProperty(entry, elem);
					entry.setProperty(prop.getNodeName(), prop.getValue());
				} else {
					throw new IllegalArgumentException("Unexpected element: "+elem.getLocalName());
				}
			}
		}
		
		return entry;
	}

	public static Element attributeToXml(Document doc, Element attr) throws Exception {
		
		Element attrElem = doc.createElementNS(NS_TP, "tp:attribute");
		
		// set name
		attrElem.setAttribute("name", attr.getNodeName());
		if (attr.getNamespaceURI() != null && attr.getNamespaceURI().length() > 0) {
			attrElem.setAttribute("namespaceURI", attr.getNamespaceURI());
		}
		
		// add properties
		NamedNodeMap propMap = attr.getAttributes();
		for (int i = 0; i < propMap.getLength(); ++i) {
			Attr prop = (Attr) propMap.item(i);
			Element propElem = propertyToXml(doc, prop);
			attrElem.appendChild(propElem);
		}
		
		// add attributes and values
		NodeList nodeList = attr.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				// child attribute
				Element childAttr = (Element) node;
				Element childAttrElem = attributeToXml(doc, childAttr);
				attrElem.appendChild(childAttrElem);
			} else if (Node.CDATA_SECTION_NODE == node.getNodeType() || Node.TEXT_NODE == node.getNodeType()) {
				// value
				 Element valueElem = valueToXml(doc, node.getTextContent());
				 attrElem.appendChild(valueElem);
			} else {
				throw new IllegalArgumentException("Unexpected node type: "+node.getNodeType());
			}
		}
		
		return attrElem;
	}

	public static Attribute xmlToAttribute(Entry entry, Element attrElem) throws Exception {
		
		if (!"attribute".equals(attrElem.getLocalName())) {
			throw new IllegalArgumentException("Incorrect attribute syntax. Unexpected element name: " + attrElem.getLocalName());
		}

		if (!NS_TP.equals(attrElem.getNamespaceURI())) {
			throw new IllegalArgumentException("Incorrect attribute syntax. Unexpected namespace URI: " + attrElem.getNamespaceURI());
		}
		
		String name = attrElem.getAttribute("name");
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Incorrect attribute syntax. Missing attribute name.");
		}
		String namespaceURI = attrElem.getAttribute("namespaceURI");
		
		Attribute attr;
		if (namespaceURI != null && namespaceURI.length() > 0) {
			attr = entry.createElementNS(namespaceURI, name);
		} else {
			attr = entry.createElement(name);
		}
		
		NodeList nodeList = attrElem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				Element elem = (Element) node;
				if ("attribute".equals(elem.getLocalName())) {
					// child attribute
					Attribute childAttr = xmlToAttribute(entry, elem);
					attr.appendChild(childAttr);
				} else if ("property".equals(elem.getLocalName())) {
					// property
					Property prop = xmlToProperty(entry, elem);
					if (prop.getNamespaceURI() != null && prop.getNamespaceURI().length() > 0) {
						attr.setAttributeNodeNS(prop);
					} else {
						attr.setAttributeNode(prop);
					}
				} else if ("value".equals(elem.getLocalName())) {
					// value
					String value = xmlToValue(elem);
					Text valueNode = entry.createTextNode(value);
					attr.appendChild(valueNode);
				} else {
					throw new IllegalArgumentException("Unexpected element: "+elem.getLocalName());
				}
			}
		}
		
		return attr;
	}
	
	public static Element valueToXml(Document doc, String value) throws Exception {
		
		Element elem = doc.createElementNS(NS_TP, "tp:value");
		
		CDATASection data = doc.createCDATASection(value);
		elem.appendChild(data);
		
		return elem;
	}
	
	public static String xmlToValue(Element valueElem) throws Exception {
		
		if (!"value".equals(valueElem.getLocalName())) {
			throw new IllegalArgumentException("Incorrect value syntax. Unexpected element name: " + valueElem.getLocalName());
		}

		if (!NS_TP.equals(valueElem.getNamespaceURI())) {
			throw new IllegalArgumentException("Incorrect value syntax. Unexpected namespace URI: " + valueElem.getNamespaceURI());
		}
		
		return valueElem.getTextContent();
	}
	
	public static Element propertyToXml(Document doc, String propName, String namespaceURI, String propValue) throws Exception {
		
		Element elem = doc.createElementNS(NS_TP, "tp:property");
		
		if (propName == null || propName.length() == 0) {
			throw new IllegalArgumentException("Property name must not be empty.");
		}

		elem.setAttribute("name", propName);

		if (namespaceURI != null && namespaceURI.length() > 0) {
			elem.setAttribute("namespaceURI", namespaceURI);
		}

		if (propValue != null) {
			CDATASection data = doc.createCDATASection(propValue);
			elem.appendChild(data);
		}

		return elem;
		
	}

	public static Element propertyToXml(Document doc, Attr prop) throws Exception {
		return propertyToXml(doc, prop.getNodeName(), prop.getNamespaceURI(), prop.getValue());
	}

	public static Property xmlToProperty(Entry entry, Element elem) throws Exception {

		if (!"property".equals(elem.getLocalName())) {
			throw new IllegalArgumentException("Incorrect property syntax. Unexpected element name: " + elem.getLocalName());
		}

		if (!NS_TP.equals(elem.getNamespaceURI())) {
			throw new IllegalArgumentException("Incorrect property syntax. Unexpected namespace URI: " + elem.getNamespaceURI());
		}

		String name = elem.getAttribute("name");
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Incorrect property syntax. Missing name.");
		}

		String namespaceURI = elem.getAttribute("namespaceURI");
		if (namespaceURI.length() == 0) {
			namespaceURI = null;
		}

		String value = elem.getTextContent();

		Property prop;
		if (namespaceURI != null && namespaceURI.length() > 0) {
			prop = entry.createAttributeNS(namespaceURI, name);
		} else {
			prop = entry.createAttribute(namespaceURI);
		}

		if (value != null) {
			prop.setValue(value);
		}

		return prop;
	}
	
}
