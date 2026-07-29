/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.script;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JSContext;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.JavaAccessObject;
import com.ibm.jscript.types.JavaWrapperObject;

/**
 * This class configures the IBM JS script engine.
 * 
 * Since 2.2
 * The isDebugAllowed is now final static so setDebugAllowed
 * were removed to comply with the new JSContext interface.
 * 
 * Since 2.0 ScriptEngineOptions is no longer just an options class. It also
 * provides the isDebugAllowed() method that is different for each instance of
 * the engine. Although this could be a static hastable with reference to the
 * current thread, the overhead of allocating a new JSContext is fairly small
 * since we don't use the script caching features of the JSContext.
 */
public class ScriptEngineOptions extends JSContext {

	// These variables are used for debugging
	private boolean debugAllowed = false;

	private List<TDIDebugListener> listeners = new ArrayList<TDIDebugListener>();

	/**
	 * Script engine context to be used when debugging is turned off. Caching is
	 * enabled to speed up parsing of scripts. There is a single instance for
	 * the whole JVM, so that caching has greater performance effect.
	 */
	private static JSContext nonDebugContext = new ScriptEngineOptions(true, false);

	private static Set<String> noClass = new HashSet<String>();

	/**
	 * Returns a context with no debugging and cache disabled
	 * 
	 * @return a context.
	 */
	public static JSContext get() {
		return get(false);
	}

	/**
	 * Returns a context with debugging enabled and cache disabled
	 * 
	 * @return a context.
	 */
	public static JSContext get(boolean debug) {
		if (debug) {
			return new ScriptEngineOptions(true, debug);
		} else {
			return nonDebugContext;
		}
	}

	public ScriptEngineOptions() {
		super(true);
	}

	//
	// TDI Custom debugger extensions
	//
	public ScriptEngineOptions(boolean useCache, boolean debug) {
		super(useCache);
		this.debugAllowed = debug;
	}

	public void addDebugListener(TDIDebugListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeDebugListener(TDIDebugListener listener) {
		listeners.remove(listener);
	}

	public boolean isDebug() {
		return debugAllowed;
	}

	/**
	 * Called by ASTDebug
	 * 
	 * @param statement
	 *            The statement about to be executed
	 * @param context
	 *            The context in which the statement executes
	 */
	public void debugStatement(ASTNode statement, IExecutionContext context) throws JavaScriptException {
		if (listeners.size() == 0)
			return;
		List <TDIDebugListener> listenerCopy = new ArrayList<TDIDebugListener>(listeners);
		for (TDIDebugListener l : listenerCopy)
			l.debugStatement(statement, context);
	}

	public interface TDIDebugListener {
		public void debugStatement(ASTNode statement, IExecutionContext context) throws JavaScriptException;
	}

	// Language extensions ///////////////////////////////////////////////////
	/** @return true */
	public boolean hasStringLengthAsMethod() {
		return true;
	}

	/** @return true */
	public boolean hasStringExtendedMethods() {
		return true;
	}

	/** @return true */
	public boolean hasGlobalObjectExtensions() {
		return true;
	}

	/** @return false */
	public boolean hasJUnitExtensions() {
		return false;
	}

	/** @return true */
	public boolean hasObjectPrototypeExtensions() {
		return true;
	}

	/** @return true */
	public boolean hasMathExtensions() {
		return true;
	}

	/** @return false */
	public boolean hasListOperator() {
		return false;
	}

	/** @return true */
	public boolean hasRhinoExtensions() {
		return true;
	}

	/** @return true */
	public boolean hasJavaBeanAccess() {
		return true;
	}

	/** @return true */
	public boolean autoConvertJavaArgsToString() {
		return true;
	}

	/** @return true */
	public boolean ignoreJavaCallAmbiguities() {
		return true;
	}

	// Debugger //////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */
	// String & Array addons
	public IValue getProperty(Object object, String propertyName) throws InterpretException {

		IValue returnVal = null;
		/*
		 * From Entry object the user will be able to access the properties of
		 * the entry using the .@propertyName notation and the attributes
		 * referring to them by name (i.e. .attName)
		 */
		if (object instanceof Entry) {

			Entry entry = (Entry) object;

			// check for properties
			if (propertyName.startsWith("@")) {
				Object property = entry.getProperty(propertyName.substring(1));
				returnVal = FBSUtility.wrap(this, property);
			} else {
				Object result = null;

				/*
				 * Use the DOM API only if DOM is already enabled. We don't want
				 * to enable DOM just to search an attribute.
				 */
				if (entry.isDOMEnabled()) {
					ElementQName eName = new ElementQName(propertyName);
					List<Element> dest = getElementsByTagName(entry, eName.get(), null);

					result = getResultFromList(dest);
					if (result != null && eName.getChildrenString() != null) {
						// recursive call to resolve the next element in the
						// provided path.
						result = getProperty(result, eName.getChildrenString());
					}
				} else {
					result = entry.getAttribute(propertyName);
				}
				
				if (result == null && propertyName.equalsIgnoreCase("operation"))
					result = entry.getOperation();

				returnVal = FBSUtility.wrap(this, result);
			}
		}
		/*
		 * Make objects from type Attribute access their child attributes using
		 * dot (.) notation e.g. a.b.c would return all attributes c that are
		 * children of b which are children of a. Also allow access to the
		 * properties of an attribute using @ notation e.g. a.@b will return a
		 * String object that represents the property of an attribute
		 */
		else if (object instanceof Attribute) {

			Attribute a = (Attribute) object;

			// check for properties
			if (propertyName.startsWith("@")) {
				returnVal = FBSUtility.wrap(this, getAttributeNode(a, getQName(propertyName.substring(1))));
			}

			if (returnVal == null) {
				// dealing with this as array?
				try {
					int index = Integer.parseInt(propertyName);
					returnVal = FBSUtility.wrap(this, a.getValue(index));
				} catch (NumberFormatException nfe) {
					// it is not an element request
				}
			}

			if (returnVal == null) {
				ElementQName eName = new ElementQName(propertyName);
				List<Element> dest = getElementsByTagName(a, eName.get(), null);

				Object result = getResultFromList(dest);
				if (result != null && eName.getChildrenString() != null) {
					// recursive call to resolve the next element in the
					// provided path.
					result = getProperty(result, eName.getChildrenString());
				}

				returnVal = FBSUtility.wrap(this, result);
			}
		}
		/*
		 * Provide ability for searching the NodeList using dot (.) notation.
		 * Using the .@ notation the user will be able to retrieve all the
		 * properties defined for all the Attributes in that list.
		 */
		else if (object instanceof NodeList) {

			NodeList src = (NodeList) object;

			// dealing with this as array?
			try {
				int index = Integer.parseInt(propertyName);
				returnVal = FBSUtility.wrap(this, src.item(index));
			} catch (NumberFormatException nfe) {
				// it is not an element request
			}

			if (returnVal == null) {
				if (propertyName.startsWith("@")) {
					List<Node> dest = new ArrayList<Node>();

					Attr property = null;
					QName name = getQName(propertyName.substring(1));

					for (int i = 0; i < src.getLength(); i++) {

						if (src.item(i) instanceof Element) {
							property = getAttributeNode((Element) src.item(i), name);

							if (property != null) {
								dest.add(property);
							}
						}
					}

					Object result = null;
					// found a lot of elements? return them all as array
					if (dest.size() > 1) {
						result = new ImmutableNodeList(dest);

					} // Only single element found? return just it, the
					// Attribute
					else if (dest.size() == 1) {
						// return the Attribute Java Object
						result = dest.get(0);
					}

					returnVal = FBSUtility.wrap(this, result);

				} else {
					// temp arrayList to hold all the elements found
					List<Element> dest = new ArrayList<Element>();
					ElementQName eName = new ElementQName(propertyName);

					for (int i = 0; i < src.getLength(); i++) {
						getElementsByTagName(src.item(i), eName.get(), dest);
					}

					Object result = getResultFromList(dest);
					if (result != null && eName.getChildrenString() != null) {
						// recursive call to resolve the next element in the
						// provided path.
						result = getProperty(result, eName.getChildrenString());
					}

					returnVal = FBSUtility.wrap(this, result);
				}
			}
		}

		if (returnVal == null) {
			returnVal = super.getProperty(object, propertyName);
		}

		// this tells the calling method that we do not have custom properties
		// defined for this type of object
		return returnVal;
	}

	private Object getResultFromList(List<Element> e) {
		Object result = null;
		// found a lot of elements? return them all as array
		if (e.size() > 1) {
			// return the new array
			result = new ImmutableNodeList(e);

			// Only single element found? return just it, the
			// Attribute
		} else if (e.size() == 1) {
			// return the Attribute Java Object
			result = e.get(0);
		}

		return result;
	}

	/**
	 * Parses the first element name of a complex path consisting of multiple
	 * elements names. The parsed {@link QName} can be further obtained by the
	 * {@link #get()} method and the remaining element names could be obtained
	 * using {@link #getChildrenString()};
	 * 
	 * @since 7.1
	 */
	private static class ElementQName {
		private QName qName;
		private String childrenStr;

		public ElementQName(String path) {
			int nameEndPos = getFirstNameEndPosition(path);
			qName = getQName(path.substring(0, nameEndPos));
			if (++nameEndPos < path.length()) {
				childrenStr = path.substring(nameEndPos, path.length());
			}
		}

		/**
		 * Finds the first unescaped element separator (i.e. ".") and returns
		 * its position. If the string is something like:
		 * "{http://namespace.host.com/}pref\\.name:local\\.name.second.child"
		 * Then the end position will be just before "second.child".
		 * 
		 * @param path
		 * @return
		 */
		private int getFirstNameEndPosition(String path) {
			int endPos = 0;
			int nsEnd = 0;
			if ('{' == path.charAt(0)) {
				nsEnd = path.indexOf('}') + 1;
			}

			int escCount = 0;
			loop: for (endPos = nsEnd; endPos < path.length(); endPos++) {
				switch (path.charAt(endPos)) {
				case '\\':
					escCount++;
					break;
				case '.':
					if (escCount % 2 == 0) {
						break loop;
					} else {
						escCount = 0;
					}
					break;
				default:
					escCount = 0;
					break;
				}
			}

			if (endPos - nsEnd == 0) {
				// missing local name
				throw new IllegalArgumentException(path);
			}

			return endPos;
		}

		public QName get() {
			return qName;
		}

		public String getChildrenString() {
			return childrenStr;
		}
	}

	private static QName getQName(String s) {

		String ns = XMLConstants.NULL_NS_URI;
		String localPart = s;
		String prefix = XMLConstants.DEFAULT_NS_PREFIX;

		int idx = -1;

		if (s.indexOf('{') == 0 && (idx = s.indexOf('}')) != -1) {
			ns = s.substring(1, idx);
			localPart = s.substring(idx + 1);
			s = localPart;
		}

		if ((idx = s.indexOf(':')) != -1) {
			prefix = s.substring(0, idx);
			localPart = s.substring(idx + 1);
		}

		return new QName(ns, localPart, prefix);
	}

	private List<Element> getElementsByTagName(Node parent, QName name, List<Element> list) {

		NodeList children = null;
		if (parent != null) {
			children = parent.getChildNodes();
		}

		if (children != null) {
			// !!!intentionally comparing by reference!!!
			boolean hasPrefix = name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX;
			boolean hasNS = name.getNamespaceURI() != XMLConstants.NULL_NS_URI;

			String tagName = hasNS ? Attribute.normalizeName(name.getLocalPart()) : Attribute.normalizeName(getTagName(name));

			if (list == null) {
				list = new ArrayList<Element>(children.getLength());
			}

			Node child = null;

			for (int i = 0; i < children.getLength(); i++) {
				child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					if (hasNS) {
						if (name.getNamespaceURI().equalsIgnoreCase(child.getNamespaceURI()) && tagName.equalsIgnoreCase(child.getLocalName())) {
							list.add((Element) child);
						}
					} else if (hasPrefix) {
						if (tagName.equalsIgnoreCase(child.getNodeName())) {
							list.add((Element) child);
						}
					} else if (tagName.equalsIgnoreCase(child.getLocalName())) {
						list.add((Element) child);
					}
				}
			}
		}
		return list;
	}

	private Attr getAttributeNode(Element parent, QName name) {
		if (name.getNamespaceURI() != XMLConstants.NULL_NS_URI) {
			return parent.getAttributeNodeNS(name.getNamespaceURI(), name.getLocalPart());
		} else if (name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX) {
			return parent.getAttributeNode(name.getPrefix() + ":" + name.getLocalPart());
		} else {
			return parent.getAttributeNode(name.getLocalPart());
		}
	}

	@Override
	public BeanInfo getBeanInfo(Class<?> clazz) throws IntrospectionException {
		if (clazz == Entry.class || clazz == Attribute.class || clazz == NodeList.class)
			return new SimpleBeanInfo();
		return super.getBeanInfo(clazz);
	}

	private Attribute newAttribute(Node parent, QName name) {

		NodeList children = parent.getChildNodes();

		// !!!intentionally comparing by reference!!!
		boolean hasPrefix = name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX;
		boolean hasNS = name.getNamespaceURI() != XMLConstants.NULL_NS_URI;

		String escapedTagName = null;
		String tagName = hasNS ? Attribute.normalizeName(name.getLocalPart()) : Attribute
				.normalizeName(escapedTagName = getTagName(name));

		Node child = null;

		// find the named child element
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (hasNS) {
					if (name.getNamespaceURI().equalsIgnoreCase(child.getNamespaceURI()) && tagName.equalsIgnoreCase(child.getLocalName())) {
						break;
					}
				} else if (hasPrefix) {
					if (tagName.equalsIgnoreCase(child.getNodeName())) {
						break;
					}
				} else if (tagName.equalsIgnoreCase(child.getLocalName())) {
					break;
				}
			}
			child = null;
		}

		// none found... create it then
		if (child == null) {
			if (escapedTagName == null) {
				escapedTagName = getTagName(name);
			}

			if (hasNS) {
				child = new Attribute(escapedTagName, name.getNamespaceURI(), false);
			} else {
				child = new Attribute(escapedTagName);
			}

			if (parent instanceof Entry) {
				((Entry) parent).setAttribute((Attribute) child);
			} else {
				parent.appendChild(child);
			}
		}

		return (Attribute) child;
	}

	private String getTagName(QName name) {
		return name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX ? name.getPrefix() + ':' + name.getLocalPart() : name
				.getLocalPart();
	}

	private void newProperty(Element parent, QName name, String value) {

		if (name.getNamespaceURI() != XMLConstants.NULL_NS_URI) {
			parent.setAttributeNS(name.getNamespaceURI(), name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX ? name.getPrefix()
					+ ':' + name.getLocalPart() : name.getLocalPart(), value);
		} else if (name.getPrefix() != XMLConstants.DEFAULT_NS_PREFIX) {
			if (parent instanceof Attribute && XMLConstants.XMLNS_ATTRIBUTE.equals(name.getPrefix())) {
				Attribute a = (Attribute) parent;
				String nodeName = a.getNodeName();
				int i = nodeName.indexOf(':');
				if (i > 0 && nodeName.substring(0, i).equalsIgnoreCase(name.getLocalPart())) {
					a.setNamespaceURI(value);
				}
			}
			parent.setAttribute(name.getPrefix() + ":" + name.getLocalPart(), value);
		} else {
			parent.setAttribute(name.getLocalPart(), value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean putProperty(Object object, String propertyName, IValue value) throws InterpretException {

		boolean result = false;

		/*
		 * This bit of code allows the user to set properties (e.g.
		 * entry.@propName = "propValue") or attributes (e.g. entry.attName =
		 * "attValue") using the specific notation.
		 */
		if (object instanceof Entry) {
			Entry entry = (Entry) object;

			if (propertyName.startsWith("@")) {
				entry.setProperty(propertyName.substring(1), value.toJavaObject());
				result = true;
			} else {
				ElementQName eName = new ElementQName(propertyName);
				Attribute attrChild = null;

				if (entry.isDOMEnabled() || haveNamespace(eName)) {
					attrChild = newAttribute(entry, eName.get());
					if (eName.getChildrenString() != null) {
						// recursive call to set the value on the children
						result = putProperty(attrChild, eName.getChildrenString(), value);
					} else {
						setAttributeValues(attrChild, value.toJavaObject());
						result = true;
					}
				} else {
					attrChild = entry.newAttribute(propertyName);
					setAttributeValues(attrChild, value.toJavaObject());
					result = true;
				}
			}
		}
		/*
		 * This bit of code gives the user the ability to set Attribute's values
		 * using the [] notation. Also the user will be able to set Attribute's
		 * Properties using the .@ notation
		 */
		else if (object instanceof Attribute) {
			Attribute attr = (Attribute) object;

			try {
				int index = Integer.parseInt(propertyName);
				attr.setValue(index, value.toJavaObject());
				result = true;
			} catch (NumberFormatException nfe) {
				// the propertyName was not an int. Just ignore
			}

			if (!result) {
				if (propertyName.startsWith("@")) {
					newProperty(attr, lookupNamespaceFromParent(attr, getQName(propertyName.substring(1))),
							value.toJavaObject() != null ? value.toJavaObject().toString() : null);
					result = true;
				} else {
					ElementQName eName = new ElementQName(propertyName);
					Attribute attrChild = newAttribute(attr, lookupNamespaceFromParent(attr, eName.get()));

					if (eName.getChildrenString() != null) {
						// recursive call to set the value on the children
						result = putProperty(attrChild, eName.getChildrenString(), value);
					} else {
						setAttributeValues(attrChild, value.toJavaObject());
						result = true;
					}
				}
			}
		}
		/*
		 * This section will enable the user to successfully execute the
		 * following statement: conn.a.b.c[0] = "value". When the conn.a.b.c
		 * results in a NodeList we add a value to the Attribute from the list
		 * referred by the index.
		 */
		else if (object instanceof NodeList) {

			NodeList list = (NodeList) object;

			int index = Integer.parseInt(propertyName);
			if (list.item(index) instanceof Attribute) {
				setAttributeValues((Attribute) list.item(index), value.toJavaObject());
				result = true;
			}
		}

		if (!result) {
			result = super.putProperty(object, propertyName, value);
		}

		return result;
	}

	private QName lookupNamespaceFromParent(Element parent, QName qName) {
		if (qName.getNamespaceURI() == XMLConstants.NULL_NS_URI) {
			// does not have an explicit NS, try to resolve it
			String pref = qName.getPrefix();

			if (pref == XMLConstants.DEFAULT_NS_PREFIX) {
				pref = null;
			}

			String ns = parent.lookupNamespaceURI(pref);
			if (ns != null) {
				qName = new QName(ns, qName.getLocalPart(), qName.getPrefix());
			}
		}

		return qName;
	}

	/**
	 * @param eName
	 * @return
	 */
	private boolean haveNamespace(ElementQName eName) {
		return eName.get().getNamespaceURI() != XMLConstants.NULL_NS_URI
				|| (eName.getChildrenString() != null && haveNamespace(new ElementQName(eName.getChildrenString())));
	}

	@SuppressWarnings("unchecked")
	private void setAttributeValues(Attribute attr, Object value) {

		attr.clear();
		if (value instanceof Attribute) {
			Attribute val = (Attribute) value;
			if (val.isDOMEnabled()) {
				NodeList children = val.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					attr.appendChild(children.item(i));
				}
			} else {
				for (Object obj : val.getValues()) {
					attr.addValue(obj);
				}
			}
		} else if (value instanceof Vector) {
			for (Object o: (Vector<Object>)value ) {
				attr.addValue(o);
			}
		} else if (value != null) {
			
			attr.addValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public JavaWrapperObject wrapObject(Object o) {

		if (o instanceof Entry) {
			return new EntryWrapper(this, (Entry) o);
		} else if (o instanceof Attribute) {
			return new AttributeWrapper(this, (Attribute) o);
		} else if (o instanceof NodeList) {
			return new NodeListWrapper(this, (NodeList) o);
		}

		return null;
	}

	/**
	 * Overrides the default loadClass, to remember strings that are not classes.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Class loadClass(String className) throws ClassNotFoundException {
		if (isNoClass(className))
			throw new ClassNotFoundException(className);
		try {
			return super.loadClass(className);
		} catch (ClassNotFoundException e) {
			setNoClass(className);
			throw e;
		}
	}
	
	private static boolean isNoClass(String name) {
		synchronized (noClass) {
			return noClass.contains(name);
		}
	}
	
	private static void setNoClass(String name) {
		synchronized (noClass) {
			noClass.add(name);
		}
	}
	
	/**
	 * Clear the set containing strings that are not classes.
	 * This method is useful to call after adding new jars to the class loader.
	 */
	public static void clearNoClassSet() {
		synchronized (noClass) {		
			noClass.clear();
		}
	}

	
	@Override
	public int getSourceTabSize() {
		// Use 1 to be compatible with eclipse counting
		return 1;
	}

	/**
	 * 
	 * This wrapper class just allow the for in loop to cycle over
	 * HAttrbiteList's values.
	 * 
	 * @author kaloyan.kolev
	 */
	private static class NodeListWrapper extends JavaAccessObject {

		private NodeList list = null;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            the java script context object
		 * @param list
		 *            the list to wrap
		 */
		public NodeListWrapper(JSContext context, NodeList list) {
			super(context, list.getClass(), list);
			this.list = list;
		}

		/**
		 * {@inheritDoc}
		 */
		public IValues getValues() {

			return new IValues() {

				private int index = 0;

				public boolean hasNext() {
					return list == null ? false : index < list.getLength();
				}

				public FBSValue next() {
					try {
						return FBSUtility.wrap(getJSContext(), list.item(index++));
					} catch (InterpretException ie) {
						throw new RuntimeException(ie);
					}
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			// not sure if the object is put in a hashtable but as
			// suggested by the Static Code Analysis tool this class should have
			// an implementation.
			return 2;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {

			if (!(o instanceof NodeListWrapper)) {
				return false;
			} else {
				return super.equals(o);
			}
		}
	}

	private static class EntryWrapper extends JavaAccessObject {

		private Entry entry = null;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            the java script context object
		 * @param entry
		 *            the entry to wrap
		 */
		public EntryWrapper(JSContext context, Entry entry) {
			super(context, entry.getClass(), entry);
			this.entry = entry;
		}

		/**
		 * {@inheritDoc}
		 */
		public IValues getValues() {
			return new IValues() {

				private Iterator<String> attributeNameIterator = entry.getAttributeCollection().iterator();

				public boolean hasNext() {
					return attributeNameIterator.hasNext();
				}

				public FBSValue next() {
					String nextAttributeName = attributeNameIterator.next();
					Attribute a = entry.getAttribute(nextAttributeName);
					try {
						return FBSUtility.wrap(getJSContext(), a);
					} catch (InterpretException ie) {
						throw new RuntimeException(ie);
					}
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			// not sure if the object is put in a hashtable but as
			// suggested by the Static Code Analysis tool this class should have
			// an implementation.
			return 1;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {

			if (!(o instanceof EntryWrapper)) {
				return false;
			} else {
				return super.equals(o);
			}
		}
	}

	private static class AttributeWrapper extends JavaAccessObject {

		private Attribute attr;

		public AttributeWrapper(JSContext jsContext, Attribute attr) {
			super(jsContext, attr.getClass(), attr);
			this.attr = attr;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.jscript.types.JavaWrapperObject#toFBSPrimitive()
		 */
		@Override
		public FBSValue toFBSPrimitive() throws InterpretException {
			Object o;
			if (attr.size() == 1)
				o = attr.getValue(0);
			else if (attr.size() == 0)
				o = hasChildNodes() ? attr.toString() : "";
			else
				o = Arrays.toString(attr.getValues());

			if (o == null)
				o = "";

			return FBSUtility.wrap(getJSContext(), o);
		}

		private boolean hasChildNodes() {
			if (!attr.isDOMEnabled())
				return false;
			return attr.hasChildNodes();
		}

		@Override
		public boolean isString() {
			return true;
		}

		@Override
		public String stringValue() {
			if (attr.size() == 1)
				return attr.getValue();
			return Arrays.toString(attr.getValues());
		}

		@Override
		public IValues getValues() {

			return new IValues() {

				private NodeList children = attr.isDOMEnabled() ? attr.getChildNodes() : null;
				private int index = 0;

				public boolean hasNext() {
					if (children != null) {
						return index < children.getLength();
					} else {
						return index < attr.size();
					}
				}

				public FBSValue next() {
					Object value;
					if (children != null) {
						value = children.item(index);
					} else {
						value = attr.getValueAV(index);
					}
					FBSValue result;
					try {
						result = FBSUtility.wrap(getJSContext(), value);
					} catch (InterpretException ie) {
						throw new RuntimeException(ie);
					}
					++index;
					return result;
				}
			};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			// not sure if the object is put in a hashtable but as
			// suggested by the Static Code Analysis tool this class should have
			// an implementation.
			return 4;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object o) {

			if (!(o instanceof AttributeWrapper)) {
				return false;
			} else {
				return super.equals(o);
			}
		}
	}

	static final class ImmutableNodeList implements NodeList {

		List<? extends Node> list;

		ImmutableNodeList(List<? extends Node> values) {
			this.list = values;
		}

		public int getLength() {
			return list.size();
		}

		public Node item(int i) {
			return list.get(i);
		}
	}
}
