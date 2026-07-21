/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonFactory;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Property;

/**
 * This class implements the JsonFactory interface used by the IBM JavaScript Engine's JSON features. It is used both to create objects
 * when reading JSON code, and also used when generating JSON code. This class maps between Tivoli Directory Integrator's Entry/Attribute
 * model and the JSON model.
 * <p>
 * Parsing - When parsing JSON code the parser calls the createXXX methods and setProperty to generate an Entry from JSON data. Conversely, when
 * generating JSON data from an Entry, the methods in use are the isXXX methods (e.g. isArray, isObject) and also the getProperty method, iterateArrayValues,
 * and iterateObjectProperties.
 */
public class JsonTdiFactory implements JsonFactory {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final JsonTdiFactory instance = new JsonTdiFactory();

	/**
	 * When iterating through attributes with children, we need a way to know which of several attributes with the same
	 * name we should choose. This variable remembers which attribute is the next one to look at.
	 */
	private Object iteratorValue = null;
	
	public Object createNull() {
		return null;
	}

	public Object createString(String value) {
		return value;
	}

	public Object createNumber(double value) {
		return Double.valueOf(value);
	}

	public Object createBoolean(boolean value) {
		return Boolean.valueOf(value);
	}

	public Object createObject(Object parent, String propertyName) {
		if(parent == null) {
			// -- Top level is always an Entry objet
			return new Entry();

		} else  {
			// -- Child objects are always Attribute objects
			Attribute attr = new Attribute(Attribute.escapeName(propertyName));
			return attr;
		}
	}

	public Object createArray(Object parent, String propertyName, List<Object> values) {
		return values;
	}

	@SuppressWarnings("unchecked")
	public void setProperty(Object parent, String propertyName, Object value) {

		// Escape special characters, e.g dot or backslash.
		String eName = Attribute.escapeName(propertyName);

		if(parent instanceof Entry) {
			if (propertyName.startsWith("@") && ! (value instanceof Attribute) && ! (value instanceof List) && propertyName.length() > 1) {
				((Entry)parent).setProperty(propertyName.substring(1), value);
				return;
			}

			Attribute attribute = ((Entry)parent).getAttribute(eName);
			if (attribute == null && value instanceof Attribute) {
				Attribute a = (Attribute) value;
				if (eName.equals(a.getName())) {
					((Entry)parent).setAttribute(eName, a);
					return;
				}
			}

			if(attribute == null)
				attribute =((Entry)parent).newAttribute(eName);

			if(value instanceof List) {
				setValues(attribute, (List<Object>)value);
			} else if (value instanceof Attribute) {
				attribute.appendChild((Attribute)value);
			} else {
				attribute.addValue(value);
			}

		} else if (parent instanceof Attribute) {
			Attribute attribute = (Attribute) parent;
			if (propertyName.startsWith("@") && ! (value instanceof Attribute) && ! (value instanceof List) && propertyName.length() > 1) {
				attribute.setAttribute(propertyName.substring(1), value.toString());
				return;
			}

			if(value instanceof List) {
				Attribute child = new Attribute(eName);
				attribute.appendChild(child);
				setValues(child, (List<Object>)value);
			} else {
				Attribute child = null;
				if(value instanceof Attribute && eName.equals(((Attribute)value).getName())) {
					child = (Attribute) value;
				} else {
					child = new Attribute(eName, value);
				}
				attribute.appendChild(child);
			}
		}
	}

	private void setValues(Attribute attribute, List<Object> values) {
		for(Object obj : values) {
			if(obj instanceof Attribute) {
				Attribute newAttr = (Attribute)obj;
				if (newAttr.hasAttributes() && newAttr.size() == 0) {
					NamedNodeMap map = newAttr.getAttributes();
					// Will only be one value, but make a loop anyway.
					for (int i = 0; i < map.getLength(); i++) {
						Node node = map.item(i);
						if (node.getNamespaceURI() == null) {
							attribute.setAttributeNode((Attr) node.cloneNode(true));
						} else {
							attribute.setAttributeNodeNS((Attr) node.cloneNode(true));
						}
					}
				} else if(newAttr != attribute && newAttr != attribute.getParentNode()) {
					attribute.appendChild(newAttr);
				}
			} else {
				attribute.addValue(obj);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Object getProperty(Object parent, String propertyName) throws JsonException {

		Object tmp = iteratorValue;
		iteratorValue = null;
		if (tmp instanceof Attribute) {
			Attribute attr = (Attribute) tmp;

			if(attr.size() == 0)
				return attr.isDOMEnabled() ? attr : null;

			if (attr.isDOMEnabled() && attr.hasAttributes()) {
				List<Object> values = new ArrayList<Object>();
				NamedNodeMap nodes = attr.getAttributes();
				for (int i = 0; i < nodes.getLength(); i++) {
					values.add(nodes.item(i));
				}
				values.addAll(attr.getValuesVector());
				return values;
			}

			if(attr.size() == 1)
				return attr.getValue(0);
			else
				return attr.getValuesVector();				
		}
		return tmp;
	}

	public boolean isNull(Object value) throws JsonException {
		return value==null;
	}

	public boolean isString(Object value) throws JsonException {
		if (value instanceof String)
			return true;
		
// Let all unknown values be strings, to avoid exceptions...
		if (value instanceof Entry ||
			value instanceof Attribute ||
			value instanceof Property ||
			isNumber(value) || 
			isBoolean(value) || 
			isArray(value))
			return false;
			
		return true;
	}

	public String getString(Object value) throws JsonException {
		return value.toString();
	}

	public boolean isNumber(Object value) throws JsonException {
		return value instanceof Number;
	}

	public double getNumber(Object value) throws JsonException {
		return ((Number)value).doubleValue();
	}

	public boolean isBoolean(Object value) throws JsonException {
		return value instanceof Boolean;
	}

	public boolean getBoolean(Object value) throws JsonException {
		return ((Boolean)value).booleanValue();
	}

	public boolean isObject(Object value) throws JsonException {
		if (value instanceof Entry)
			return true;
		else if (value instanceof Attribute)
			return isAttributeAnObject((Attribute)value);
		else if (value instanceof Property)
			return true;

		return false;
	}

	/**
	 * Returns true if the Attribute is an object. To be an object, the attribute must contain more than one child
	 * attribute or at least one child attribute and one ordinary value (e.g. non attribute child). Furthermore, the child
	 * attributes must have different names to constitute an object.
	 * 
	 * @param value
	 * @return
	 */
	private boolean isAttributeAnObject(Attribute attr) {
		if (!attr.isDOMEnabled())
			return false;
		// -- an attribute is only an object if it contains Attributes with different names
		NodeList nodes = attr.getChildNodes();
		String name = attr.getNodeName();
		for(int i = 0; i < nodes.getLength(); i++) {
			Object obj = nodes.item(i);
			if(obj instanceof Attribute) {
				if (! name.equals(((Attribute)obj).getNodeName())) {
					// -- if the attribute has a different name from it's parent it is an object
					return true;
				}
			} else {
				// -- if we have non-attribute values it's not an object but an array
				return false;
			}
		}
		// If no values, only attributes, we can consider this an object.
		if (attr.hasAttributes() && attr.size() == 0 && nodes.getLength() == 0)
			return true;
		// No children, or all the children have same name as parent means Array
		return false;
	}

	public Iterator<String> iterateObjectProperties(Object object) throws JsonException {
		List<String> keys = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		
		if(object instanceof Entry) {
			Entry entry = (Entry) object;

			if(entry.isDOMEnabled()) {
				NodeList list = entry.getChildNodes();
				for(int i = 0; i < list.getLength(); i++) {
					Attribute attr = (Attribute) list.item(i);
					keys.add(attr.getNodeName());
					values.add(attr);
				}
			} else {
				for(String str : entry.getAttributeNames()) {
					keys.add(str);
					values.add(entry.getAttribute(str));
				}
			}

			for(String str : entry.getPropertyNames()) {
				keys.add("@" + str);
				values.add(entry.getProperty(str));
			}

		} else if (object instanceof Attribute) {
			Attribute attr = (Attribute) object;
			if(attr.isDOMEnabled()) {
				NodeList list = attr.getChildNodes();
				for(int i = 0; i < list.getLength(); i++) {
					Object obj = list.item(i);
					if(obj instanceof Attribute) {
						Attribute a = (Attribute) list.item(i);
						keys.add(a.getNodeName());
						values.add(a);
					}
				}
				if (attr.hasAttributes()) {
					NamedNodeMap nodes = attr.getAttributes();
					for (int i = 0; i < nodes.getLength(); i++) {
						String s = nodes.item(i).getNodeName();
						if (s != null) {
							keys.add("@" + s);
							values.add(nodes.item(i).getNodeValue());
						}
					}
				}
			}
		} else if (object instanceof Property) {
			Property p = (Property) object;
			keys.add("@" + p.getNodeName());
			values.add(p.getNodeValue());
		}
		return new PropertiesIterator(keys, values);
	}

	public boolean isArray(Object value) throws JsonException {
		if (value instanceof List)
			return true;
		else if (value instanceof Attribute)
			return !isAttributeAnObject((Attribute) value);
		else if (value instanceof byte[])
			return true;
		else
			return false;
	}

	@SuppressWarnings("unchecked")
	public Iterator<Object> iterateArrayValues(Object array) throws JsonException {
		List<Object> list;

		if(array instanceof List) {
			list = (List<Object>)array;
		} else if (array instanceof byte[]){
			byte[] b = (byte[])array;
			list = new ArrayList<Object>(b.length);
			for (int i = 0; i < b.length; i++)
				list.add(Byte.valueOf(b[i]));
		} else {
			list = new ArrayList<Object>();
			Attribute attr = (Attribute)array;
			if (attr.isDOMEnabled() && attr.hasAttributes()) {
				NamedNodeMap properties = attr.getAttributes();
				for (int i = 0; i < properties.getLength(); i++) {
					list.add(properties.item(i));
				}
			}
			NodeList nodes = attr.getChildNodes();
			for(int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n instanceof Attribute && 
						attr.getNodeName().equals(((Attribute)n).getNodeName()) &&
						((Attribute)n).getFirstChild() instanceof AttributeValue) {
					for (Object o: ((Attribute)n).getValues()) {
						list.add(o);
					}
				} else if (n instanceof AttributeValue) {
					list.add(((AttributeValue)n).getValue());
				} else {
					list.add(n);
				}
			}
		}

		return list.iterator();
	}
	
	/**
	 * Private class to iterate through Attribute values.
	 * Saves the next iteratorValue for getProperty().
	 * This is needed to be able to locate the correct Attribute when several Attributes have the same name.
	 *
	 */
	private class PropertiesIterator implements Iterator<String> {

		private Iterator<String> keyIterator;
		private Iterator<Object> valueIterator;
		
		public PropertiesIterator(List<String> keys, List<Object> values) {
			keyIterator = keys.iterator();
			valueIterator = values.iterator();
		}
		
		public boolean hasNext() {
			return keyIterator.hasNext() && valueIterator.hasNext();
		}

		public String next() {
			iteratorValue = valueIterator.next();
			return keyIterator.next();
		}

		public void remove() {
			// Not supported
		}
	}
}
