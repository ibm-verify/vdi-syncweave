/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.exceptions.DOMException;

/**
 * The Entry class is a container for attributes and their values. The Entry
 * class is widely used in the IBM Tivoli Directory Integrator. In the scripting
 * environment you use the Entry and Attribute classes by accessing the object's
 * methods.
 * <p>
 * <b>JavaScript Examples</b>
 * 
 * <pre>
 * 
 * // List all attributes and their values
 * var conn = connector.getNextEntry();
 * var attrnames = conn.getAttributeNames();
 * for (i = 0; i &lt; attrnames.length; i++) {
 * 	var attr = conn.getAttribute(attrnames[i]);
 * 	task.logmsg(&quot;Attribute: name = &quot; + attr.getName() + &quot;, #values = &quot; + attr.size());
 * 	for (j = 0; j &lt; attr.size(); j++) {
 * 		task.logmsg(&quot;      Value &quot; + j + &quot;: &quot; + attr.getValue(j));
 * 	}
 * }
 * </pre>
 * 
 * The Entry could store date using two approaches:
 * <ol>
 * <li>Using the property map:<br>
 * <br>
 * <ul>
 * <li>The property map is a standard structure that uses key/value pair to
 * store user's data. <br>
 * </li>
 * <br>
 * <li>Both the key and the value objects could be of any kind since accepted
 * class type is Object.<br>
 * </li>
 * <br>
 * <li>The user could map his/her data (i.e. any subclass of the Object class
 * and the Object class itself) to a String key using the
 * {@link #setProperty(Object, Object)} method.<br>
 * <b>Note:</b> The key could be an instance of the Object type, but its String
 * representation (i.e. Object.{@link #toString()}) will be used as a key.<br>
 * </li>
 * <br>
 * <li>In order to access the mapped data the user should use the
 * {@link #getProperty(Object)} method.<br>
 * <b>Note:</b> The key Object should result in a unique String otherwise the
 * object that was already mapped will be replaced by the new data when the
 * {@link #setProperty(Object, Object)} is called! In order to distinguish and
 * retrieve data by the key name the {@link #equals(Object)} method of the key
 * is used.<br>
 * </li>
 * <br>
 * <li>To get a list of the keys used in the property map use the
 * {@link #getPropertyNames()} method.<br>
 * Starting from IBM Tivoli Directory Integrator 7.0 the properties could be
 * accessed using the .@ notation. <br>
 * <b>Note:</b> To use the .@ notation the properties names must be simple, this
 * means that the names should not contain any symbols that have their own
 * meaning in the script's interpretation. In case the name is a complex one use
 * the {@link #getProperty(Object)} method instead. <br>
 * </li>
 * </ul>
 * </li>
 * <br>
 * <li>Using the Attribute map:<br>
 * <br>
 * <ul>
 * <li>This is the most commonly used in IBM Tivoli Directory Integrator
 * key/value pair structure. Each key maps to a value which could be of type
 * Attribute or any user defined subclasses for example.<br>
 * <b>Note:</b> There is the limitation that the keys used for identifying the
 * Attribute objects must be unique strings. <br>
 * </li>
 * <br>
 * <li>Starting from IBM Tivoli Directory Integrator 7.0 additional wrapper
 * objects were introduced to handle XML like hierarchical data structures.<br>
 * For this purpose the Entry object now implements the
 * {@link org.w3c.dom.Document} interface, the {@link org.w3c.dom.Element}
 * interface is implemented by the {@link Attribute} class, the org.w3c.Attr
 * interface by the {@link Property} class, the {@link org.w3c.dom.Text} is
 * implemented by the {@link AttributeText} class and the
 * {@link org.w3c.dom.CDATASection} interface is implemented by the
 * {@link AttributeCDATA} class. <br>
 * Check out the JavaDoc for each of these new classes for more details.<br>
 * </li>
 * <br>
 * <li>Since IBM Tivoli Directory Integrator 7.0 it is possible to access
 * Entry's Attributes using the dot notation. <br>
 * <b>Note:</b> In order to be able to access the Attribute using the dot
 * notation the key name of the Attribute must be a simple one. This means that
 * no characters that are interpret as operators by the JavasSript engine could
 * be used. In the case where the name is a complex one use the old approach -
 * access the Attribute providing the name to the {@link #getAttribute(String)}
 * method.<br>
 * </li>
 * </ul>
 * </li>
 * </ol>
 * <b>Note:</b> The names of the attributes does not contain namespace
 * information and no such information is interpreted or expected by any of the
 * {@link #getAttribute(String)}, {@link #setAttribute(String, Object)},
 * {@link #getAttributeNames()} and {@link #getAttributeCollection()} methods or
 * any of the other methods using these ones. <br>
 * </p>
 * <p>
 * <b> Script Example </b>
 * 
 * <pre>
 * 
 *   // Access any property using the .@ notation
 *   var prop = work.@propertyName;
 *   // here the prop variable will have the value of the property with name propertyName
 *   
 *   // Note that properties with names &quot;1propName&quot;, &quot;prop#Name&quot;, &quot;prop!Name&quot; and so on, could not be accessed using that notation.
 *   // All of the following will result in an Exception being thrown by the Script Engine.
 *   var prop = work.@prop.Name;
 *   var prop = work.@prop!Name;
 *   // the names shouldn't start with a number either.
 *   var prop = work.@32443Prop;
 *   
 *   // use this approach to get the desired properties
 *   var prop = work.getProperty(&quot;prop.Name&quot;);
 *   prop = work.getProperty(&quot;prop!Name&quot;);
 *   prop = work.getProperty(&quot;32443Prop&quot;);
 *   
 *   // You could also create/replace properties like this
 *   work.@newPropertyName = new java.lang.Object();
 *   // If there were a property with the name newPropertyName then its value will be replaced.
 *      
 *   // Access any Attribute just refer to it using the local name it was mapped under
 *   var attr = work.attributeName;
 *   // here the attr variable will point to the Attribute object that was mapped to the key name &quot;attributeName&quot;.
 *   
 *   // Note that the name after the . should be a simple one in order to access it.
 *   // Names that have special characters like: attribute-Name or !attribute*Name or 342AttrName are not resolvable.
 *   // For those kind of names use the entry.getAttribute(&quot;complexName&quot;); approach.
 *    var attr = work.getAttriubte(&quot;@Complex346##.Name6&quot;);
 *    
 *   // You could create/replace Attributes like this:
 *   work.elementsLocalName = work.createElement(&quot;elementsLocalName&quot;);
 *   // This will create new Attribute with local name &quot;elementsLocalName&quot; and will map it under the key name &quot;elementsLocalName&quot;.
 *   
 *   // Note: if you specify a different name on the left then that name will override the one of the element in the right.
 *   work.otherName = work.createElement(&quot;elementLocalName&quot;);
 *   // You could think of this operation as if this was executed:
 *   work.setAttribute(&quot;otherName&quot;, work.createElement(&quot;elementsLocalName&quot;));
 *   // Here the name on the left is set on the Attribute on the right.
 *   
 *   // cycle through the Entry's attributes using the for/in loop
 *   for (var v in work) {
 *   main.logmsg(v.getName());
 *   }
 * </pre>
 * 
 * </p>
 */
public class Entry extends DocImpl implements Serializable, Cloneable {

	// (*) Start of Static constants...
	/**
	 * CopyRight header.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -5961424529378625729L;

	/**
	 * The Entry contains an entry which is supposed to be added
	 */
	public final static char OP_ADD = 'a';

	/**
	 * String representation of the {@link #OP_ADD} field.
	 */
	public final static String OP_ADD2 = "add";

	/**
	 * The Entry contains an entry which is supposed to be modified
	 */
	public final static char OP_MOD = 'm';

	/**
	 * String representation of the {@link #OP_MOD} field.
	 */
	public final static String OP_MOD2 = "modify";

	/**
	 * The Entry contains an entry which is supposed to be removed
	 */
	public final static char OP_DEL = 'd';

	/**
	 * String representation of the {@link #OP_DEL} field.
	 */
	public final static String OP_DEL2 = "delete";

	/**
	 * The Entry contains an entry which is unchanged
	 */
	public final static char OP_UNCHANGED = 'u';

	/**
	 * String representation of the {@link #OP_UNCHANGED} field.
	 */
	public final static String OP_UNCHANGED2 = "unchanged";

	/**
	 * The Entry contains an entry with no explicit knowledge of operation
	 */
	public final static char OP_GEN = 'g';

	/**
	 * String representation of the {@link #OP_GEN} field.
	 */
	public final static String OP_GEN2 = "generic";

	// (*)(*) Start of TDI 70 constants....
	/**
	 * This is the character used to separate the simple names in a composite
	 * name
	 */
	public final static char PATH_SEPARATOR_CHAR = '.';

	/**
	 * This is the string representing the {@link #PATH_SEPARATOR_CHAR} field.
	 */
	public final static String PATH_SEPARATOR_STR = ".";

	/**
	 * This is the character used to separate the simple names in a composite
	 * name
	 */
	public final static char ESCAPE_CHAR = '\\';

	// (*)(*) End of TDI 70 constants....

	// (*) End of Static constants...

	// (*) Start of TDI 611 fields...
	/**
	 * This is the map holding references to each Attribute in the flat tree.
	 * This structure maps escaped paths/names of the Attributes in the flat
	 * tree to the Attributes themselves.
	 */
	private Hashtable<String, Attribute> data;

	/**
	 * This structure maps lower case representation of full names to the real
	 * full names. This map is used for performance optimization when retrieving
	 * the attributes in the cases when the used name does not match the one
	 * used for key in the data map.
	 */
	private Hashtable<String, String> lowerCaseMap;

	/** holds the operation this entry is tagged with */
	private char operation;

	/** the properties this entry has. This field is lazily initialized. */
	private Hashtable<String, Object> properties;

	/**
	 * the case insensitive map holding the properties names. This field is
	 * lazily initialized.
	 */
	private Hashtable<String, String> lowercaseProps;

	// (*) End of TDI 611 fields...

	/** optimizes the creation of the Entry */
	transient FieldsOptimizer field;

	/**
	 * Construct a new generic flat Entry
	 */
	public Entry() {
		this(false);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param domEnabled
	 *            specifies whether the created entry will be hierarchical or a
	 *            flat one. If the entry is not required to be hierarchical use
	 *            false here and it will be made hierarchical on demand if a
	 *            method from the DOM API is called.
	 */
	public Entry(boolean domEnabled) {
		if (domEnabled) {
			field = new FieldsOptimizer();
		} else {
			data = new Hashtable<String, Attribute>();
			lowerCaseMap = new Hashtable<String, String>();
		}

		operation = OP_GEN;
	}

	/**
	 * Construct a new generic Entry, with data given by the provided Hashtable.
	 * This is used for internal purposes.
	 * 
	 * @param table
	 *            - a Hashtable representation of the
	 * @deprecated will be remove in a future release.
	 */
	@Deprecated
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Entry(Hashtable table) {
		data = (Hashtable) table.clone();
		lowerCaseMap = new Hashtable<String, String>();
		operation = OP_GEN;

		String key = null;
		for (Enumeration<String> e = data.keys(); e.hasMoreElements();) {
			key = e.nextElement();
			lowerCaseMap.put(key.toLowerCase(Locale.ENGLISH), key);
		}
	}

	/**
	 * Returns an Entry object which is a clone of the Entry parameter. The
	 * cloning is shallow which means that only first-level cloning of data
	 * values is performed.
	 * 
	 * @param entry
	 *            The Entry object to clone
	 * @return The cloned entry
	 * @deprecated use the {@link #clone()} method instead
	 */
	@Deprecated
	public Entry clone(Entry entry) {
		Entry e = new Entry(entry.data);
		e.setOp(entry.getOp());

		String[] list = entry.getPropertyNames();
		for (int i = 0; i < list.length; i++) {
			e.setProperty(list[i], entry.getProperty(list[i]));
		}

		return e;
	}

	/**
	 * Creates a shallow clone of the receiver. The Hierarchical Attributes
	 * contained in the data structures however are complete clones.
	 * 
	 * @return a clone of the receiver.
	 * 
	 * @since 7.0
	 */
	@Override
	public Entry clone() {
		try {
			Entry clone = (Entry) super.clone();
			fillInClone(clone, true);
			return clone;
		} catch (CloneNotSupportedException e) {
			// should not happen...
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Completes the empty clones by copying the private fields.
	 * 
	 * @param clone
	 *            the cloned entry object
	 * @param deep
	 *            specify how the method will handle the Attributes of the
	 *            source entry.
	 */
	private void fillInClone(Entry clone, boolean deep) {

		if (isDOMEnabled()) {
			clone.field = new FieldsOptimizer();
			clone.field.xPathObj = this.field.xPathObj;
			clone.field.isNamesListValid = this.field.isNamesListValid;
			clone.field.hiddenChildren = null;
			clone.field.toRemove = null;

			clone.field.tempKN = new KeyName();
			if (this.field.cachedNames != null) {
				clone.field.cachedNames = new HashSet<String>(this.field.cachedNames);
			}

			if (deep) {
				clone.field.hData = new HashMap<KeyName, Attribute>(this.field.hData.size() + 10);

				clone.field.children = new ArrayList<Attribute>(this.field.children.size());

				for (Attribute child : field.children) {
					clone.field.tempKN.setFullName(child.getName());
					clone.internalPutAttribute(clone.field.tempKN.clone(), child, true, true);
				}
			} else {
				clone.field.hData = new HashMap<KeyName, Attribute>(this.field.hData);
			}
		} else {
			clone.lowerCaseMap = new Hashtable<String, String>(this.lowerCaseMap);

			if (deep) {
				clone.data = new Hashtable<String, Attribute>(this.data.size());

				Attribute attr = null;
				for (Map.Entry<String, Attribute> child : this.data.entrySet()) {
					attr = child.getValue().clone();
					attr.doc = clone;
					clone.data.put(child.getKey(), attr);
				}
			} else {
				clone.data = new Hashtable<String, Attribute>(this.data);
			}
		}

		if (lowercaseProps != null) {
			// clone the properties
			clone.properties = new Hashtable<String, Object>(this.getProperties());
			clone.lowercaseProps = new Hashtable<String, String>(this.getLowercaseProps());
		}
	}

	/**
	 * Returns the operation field of this entry. The operation field signals
	 * what should be done with this Entry. See the OP_* codes for possible
	 * return values. The field is only set to a meaningful value if the Entry
	 * comes from an Iterator with delta enabled.
	 * 
	 * @return This Entry's operation code
	 */
	public char getOp() {
		return operation;
	}

	/**
	 * Sets the operation code for this Entry.
	 * 
	 * @param operation
	 *            The operation code
	 */
	public void setOp(char operation) {
		this.operation = operation;
	}

	/**
	 * Returns the operation field of this Entry. The operation field signals
	 * what should be done with this Entry. The method returns either <br>
	 * 
	 * <tt>OP_ADD2 = "add";</tt> The entry contains an entry which is supposed
	 * to be added<br>
	 * <tt>OP_MOD2 = "modify";</tt> The entry contains an entry which is
	 * supposed to be modified<br>
	 * <tt>OP_DEL2 = "delete";</tt> The entry contains an entry which is
	 * supposed to be removed<br>
	 * <tt>OP_UNCHANGED2 = "unchanged";</tt> The entry contains an entry which
	 * is unchanged<br>
	 * <tt>OP_GEN2 = "generic";</tt> The entry contains an entry with no
	 * explicit knowledge of operation<br>
	 * The field is only set to a meaningful value if the entry comes from an
	 * Iterator with delta enabled.
	 * 
	 * @return This Entry's operation code as a String
	 */
	public String getOperation() {

		switch (getOp()) {
		case 'a':
			return OP_ADD2;
		case 'm':
			return OP_MOD2;
		case 'd':
			return OP_DEL2;
		case 'u':
			return OP_UNCHANGED2;
		default:
			return OP_GEN2;
		}
	}

	/**
	 * Sets the operation code for this Entry.
	 * 
	 * @param operation
	 *            The operation code as a String
	 */
	public void setOperation(String operation) {
		if (operation == null || operation.length() == 0) {
			setOp(OP_GEN);
		} else {
			setOp(Character.toLowerCase(operation.charAt(0)));
		}
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes.
	 * 
	 * @deprecated Use setAttribute instead
	 * @param attr
	 *            An Attribute object
	 */
	public void set(AttributeInterface attr) {
		setAttribute(attr);
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes.
	 * 
	 * @param attr
	 *            An Attribute object
	 * @deprecated will be removed in future releases! Use
	 *             {@link #setAttribute(Attribute)} instead.
	 */
	@Deprecated
	public void setAttribute(AttributeInterface attr) {
		if (attr != null) {
			setAttribute(attr.getName(), attr);
		}
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes.
	 * <p>
	 * This will replace the attribute already mapped with the same key name (if
	 * any). <br>
	 * <br>
	 * Here are examples setting &quot;plain&quot; attributes.
	 * <p>
	 * <b>Example with &quot;plain&quot; attributes:</b>
	 * 
	 * <pre>
	 * ocAttr = system.newAttribute(&quot;objectClass&quot;);
	 * ocAttr.addValue(&quot;top&quot;);
	 * ocAttr.addValue(&quot;person&quot;);
	 * ocAttr.addValue(&quot;organizationalPerson&quot;);
	 * ocAttr.addValue(&quot;inetOrgPerson&quot;);
	 * 
	 * modentry.setAttribute(ocAttr);
	 * 
	 * </pre>
	 * 
	 * If there was an attribute named "objectClass" in the modentry then it
	 * would have been replaced by the newly created one.
	 * 
	 * @param attr
	 *            An Attribute object if this parameter is <code>null</code>
	 *            then the call is ignored.
	 * @since 7.0
	 */
	public void setAttribute(Attribute attr) {
		if (attr != null) {
			setAttribute(attr.getName(), attr);
		}
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The attribute value. If this parameter is <i>null</i>, then
	 *            the attribute is removed.
	 * @deprecated use {@link #setAttribute(String, Object)} instead.
	 */
	@Deprecated
	public void setAttribute(Object name, Object value) {

		if (name instanceof Attribute) {
			name = ((Attribute) name).getName();
		}

		if (name != null) {
			setAttribute(name.toString(), value);
		}
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes. <br>
	 * Note: This method will set/override the name of the Attribute to the
	 * name.toString() value in the cases when the second argument is an
	 * Attribute object.
	 * 
	 * @param name
	 *            The name of the Attribute to use as a key name for the
	 *            mapping. If <code>null</code> the call is ignored.
	 * @param value
	 *            The attribute value. If this parameter is <i>null</i>, then
	 *            the attribute is removed.
	 * 
	 * @see #setAttribute(Attribute)
	 * @since 7.0
	 */
	public void setAttribute(String name, Object value) {
		if (value == null) {
			removeAttribute(name);
			return;
		}

		if (name == null) {
			return;
		}

		if (!isDOMEnabled() && value instanceof Attribute && ((Attribute) value).isDOMEnabled()) {
			// assigning a hierarchical attribute to non-hierarchical entry. If
			// we have
			//
			// entry1["book.title"] = null; // create new attribute
			//
			// // standard mapping
			// entry2.setAttribute("book", entry1["book"]);
			//
			// then we won't be able to call entry2["book.title"]. Enabling DOM
			// on entry2 will allow us to do it.
			enableDOM();
		}

		if (isDOMEnabled()) {
			field.tempKN.setFullName(name);

			boolean addSubTree = true;
			boolean oldAttr = false;
			Attribute prev = internalGetAttribute(field.tempKN);
			Attribute leaf = null;

			if (value instanceof Attribute) {
				// in this corner case we are trying to set the received
				// Attribute's name possibly to a new one. If the Attribute is
				// coming from an entry (even this) it should get cloned first so we do
				// not have any hierarchy inconsistencies. Just clone it if it
				// belongs to an Entry.
				leaf = (Attribute) value;
				if (leaf.getOwnerDocument() != null) {
					leaf = leaf.cloneNode(true);
				}

				if (!field.tempKN.getName().equals(leaf.getName())) {
					leaf.setName(field.tempKN.getName());
				}
			} else {
				// don't create a new Attribute if such attribute already exists
				// in the tree.
				if (prev == null) {
					// no such attribute exists in the tree! Create a new one
					leaf = new Attribute(field.tempKN.getName(), value);
					addSubTree = false;
				} else {
					// there is an attribute in the tree... use that one.
					leaf = prev;
					oldAttr = true;
				}
			}

			Attribute parent = null;
			// check if such attribute already exists in the tree...
			if (prev != null) {
				if (oldAttr) {
					// just set the value
					leaf.setValue(value);

					if (!field.tempKN.getFullName().equals(leaf.getFullName())) {
						// this attribute is already in the data map but with
						// different name. Most probably the case of the letters
						// don't match, just change the name manually
						List<Attribute> pathToTop = new ArrayList<Attribute>();
						pathToTop.add(leaf);

						while ((leaf = leaf.getParentNode()) != null) {
							pathToTop.add(leaf);
						}

						int prevPos = 0;
						int dotPos = 0;
						Attribute curr = null;
						String tempName = null;
						for (int i = pathToTop.size() - 1; i >= 0; --i) {
							curr = pathToTop.get(i);
							dotPos = prevPos + curr.getName().length();
							tempName = field.tempKN.getFullName().substring(prevPos, dotPos);

							if (!curr.getName().equals(tempName)) {
								curr.rename(tempName, curr.getNamespaceURI());
							}

							prevPos = dotPos + 1;
						}
					}
				} else {
					// this case is called if a new Attribute should be
					// appended.
					parent = prev.getParentNode();
					if (parent != null) {
						leaf.merge((Attribute) prev);
						parent.replaceChild(leaf, prev);

						// replace the previous Attribute in the tree with the
						// leaf Attribute. The leaf attribute could not have
						// been cloned by replaceChild.
						internalPutAttribute(field.tempKN.clone(), leaf, false, addSubTree);
					} else {
						// in this case there is an existing Attribute as a
						// top-level child of this entry. Replace it...
						internalPutAttribute(field.tempKN.clone(), leaf, true, addSubTree);
					}
				}
			} else {
				// couldn't find a place to attach the Attribute so build the
				// parents' hierarchy first.
				parent = newAttribute(field.tempKN.getPrefix());
				if (parent != null) {
					// by now the parent should have been added to the data map
					// successfully. Just add the leaf.
					leaf = (Attribute) parent.internalInsertBefore(leaf, null);
					internalPutAttribute(field.tempKN.clone(), leaf, false, addSubTree);
				} else {
					// this Attribute should be attached on the entry level
					internalPutAttribute(field.tempKN.clone(), leaf, true, addSubTree);
				}
			}
		} else {
			String prev = (String) lowerCaseMap.put(name.toLowerCase(Locale.ENGLISH), name);

			if (prev != null && !prev.equals(name)) {
				data.remove(prev).doc = null;
			}

			Attribute newAttr;
			if (value instanceof Attribute) {
				newAttr = (Attribute) value;

				if (newAttr.getOwnerDocument() != null) {
					// in this corner case we are trying to set the received
					// Attribute's name possibly to a new one. If the Attribute
					// is coming from an entry it should get cloned first
					// so we do not have any hierarchy inconsistencies. Just
					// clone it if it belongs to an Entry.
					newAttr = newAttr.cloneNode(true);
				}

				if (!name.equals(newAttr.getName())) {
					// so far the leaf should be a standalone Attribute and
					// therefore setName() should be very cheap operation,
					// because no Entry will be modified.
					newAttr.setName(name);
				}
			} else {
				newAttr = new Attribute(name, value);
			}

			newAttr.doc = this;
			newAttr = data.put(name, newAttr);

			if (newAttr != null) {
				newAttr.doc = null;
			}
		}
	}

	/**
	 * Adds or replaces an attribute in this Entry's list of attributes.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The attribute value. If this parameter is <i>null</i>, then
	 *            the attribute is removed.
	 * @param protect
	 *            If this parameter is true, do not dump the Attribute values in
	 *            log files
	 * @deprecated use {@link #setAttribute(String, Object, boolean)} instead.
	 */
	@Deprecated
	public void setAttribute(Object name, Object value, boolean protect) {
		if (name instanceof Attribute) {
			name = ((Attribute) name).getName();
		}

		if (name != null) {
			setAttribute(name.toString(), value, protect);
		}
	}

	/**
	 * Calls {@link #setAttribute(String, Object)} but additionally uses the
	 * <code>protect</code> argument to set the attribute as protected or not.
	 * Note that the protection is set only on the attribute's level for a deep
	 * protection use:
	 * 
	 * <pre>
	 * work.getAttribute(name).setProtected(true, true);
	 * </pre>
	 * 
	 * @see #setAttribute(String, Object)
	 * 
	 * @param name
	 *            The name of the Attribute to use as a key name for the
	 *            mapping.
	 * @param value
	 *            The attribute value. If this parameter is <i>null</i>, then
	 *            the attribute is removed.
	 * @param protect
	 *            If this parameter is true, do not dump the Attribute's values
	 *            in the log files.
	 * @since 7.0
	 */
	public void setAttribute(String name, Object value, boolean protect) {
		if (value == null) {
			removeAttribute(name);
			return;
		}

		if (isDOMEnabled()) {
			setAttribute(name, value);
			Attribute newAttr = getAttribute(name);
			if (newAttr != null) {
				newAttr.setProtected(protect, false);
			}
		} else {
			String prev = (String) lowerCaseMap.put(((String) name).toLowerCase(Locale.ENGLISH), name);
			if (prev != null && !prev.equals(name)) {
				data.remove(prev).doc = null;
			}

			Attribute newAttr;
			if (value instanceof Attribute) {
				newAttr = (Attribute) value;
				newAttr.setName(name);
			} else {
				newAttr = new Attribute(name, value);
				newAttr.doc = this;
			}

			newAttr.doc = this;
			newAttr.setProtected(protect, false);
			newAttr = data.put(name, newAttr);
			if (newAttr != null) {
				newAttr.doc = null;
			}

		}
	}

	/**
	 * This method creates a new attribute with the specified name and values
	 * and add it to the entry. If the <code>values</code> parameter is an
	 * {@link Attribute} then that Attribute's values are added to the new
	 * Attribute. If the <code>values</code> parameter is a {@link List} then
	 * its elements are added as values to the new Attribute. If any of the
	 * elements are Attributes then the values of those attributes are also
	 * added to the new Attribute, the element attributes however are not.
	 * 
	 * @param name
	 *            The name of the Attribute to use as a key name for the
	 *            mapping.
	 * @param values
	 *            The attribute values. If this parameter is <i>null</i>, then
	 *            the attribute is removed.
	 * @see #setAttribute(Attribute)
	 * @since 7.0
	 */
	public void setAttributeValues(String name, Object values) {
		if (values == null) {
			removeAttribute(name);
			return;
		}

		if (name == null) {
			return;
		}

		Attribute leaf = newAttribute(name);

		if (leaf.size() > 0) {
			// there is already such attribute in the tree... use that one.
			leaf.clear();
		}

		// set the values to the leaf
		if (values instanceof Attribute) {
			leaf.addValues((Attribute) values);
		} else if (values instanceof List) {
			for (Object o : (List<?>) values) {
				if (o instanceof Attribute) {
					leaf.addValues((Attribute) o);
				} else {
					leaf.addValue(o);
				}
			}
		} else {
			leaf.addValue(values);
		}
	}

	/**
	 * This method is used to find/create an Attribute in the entry using the
	 * specified name. <br />
	 * <br />
	 * 
	 * This method accepts a name in the form "a.b.c.d" and tries to resolve
	 * this composite name in the receiver's tree. The composite name is broken
	 * down to pieces which tells the method how to navigate the hierarchy.
	 * 
	 * <br/>
	 * <br/>
	 * For example if we have the following hierarchy:
	 * 
	 * <pre>
	 * Entry
	 *  |---a
	 *      |---b
	 *      |   |---c
	 *      |
	 *      |---b
	 *          |---c
	 *              |---d
	 * 
	 * </pre>
	 * 
	 * Then the method will start from the root (the Entry) and will firstly
	 * search for an Attribute called <b>a</b>. If the Attribute on the entry
	 * level does not exists it is automatically created and added to the entry.
	 * Then a search for a child of <b>a</b> with the name <b>b</b> is made. If
	 * that child is not found then it is created as well. In our case the first
	 * <b>c</b> is found on the next step and then searched for a child named
	 * <b>d</b>. No such child is found and therefore it is created.
	 * 
	 * <br/>
	 * <br/>
	 * Once all the paths are resolved the method will return the last Attribute
	 * resolved/created. To navigate to the top you could use the
	 * {@link Attribute#getParentNode()}.
	 * 
	 * <br/>
	 * <br/>
	 * The method is able to work with single names e.g. "<b>a</b>" or
	 * "<b>pref:a</b>" which will be looked up in the entry and if not found
	 * will be automatically created. <br>
	 * <br>
	 * <b>Note:</b> The character "." is used as a separator. To escape it use
	 * "\.". To escape the escape char - "\" - use "\\".<br>
	 * <br>
	 * 
	 * @param name
	 *            the composite of names to use when creating/navigating the
	 *            Attributes chain.
	 * 
	 * @return a reference to the last Attribute retrieved/created in the
	 *         process of resolving the specified name.
	 * @since 7.0
	 */
	public Attribute newAttribute(String name) {

		if (name == null) {
			return null;
		}

		Attribute container = null;
		if (isDOMEnabled()) {

			KeyName thisKN = new KeyName(name);
			container = internalGetAttribute(thisKN);

			if (container == null) {

				Attribute parent = null;
				if (thisKN.getPrefix() != null) {
					parent = newAttribute(thisKN.getPrefix());
				}

				if (parent == null) {
					// create domEnabled attribute
					container = new Attribute(thisKN.getName(), null, false);
					// create a top-level Attribute
					internalPutAttribute(thisKN, container, true, false);
				} else {
					// create domEnabled attribute
					container = new Attribute(thisKN.getName(), parent.getNamespaceURI(), false);
					// create another child
					parent.internalInsertBefore(container, null);
					internalPutAttribute(thisKN, container, false, false);
				}
			}
		} else {
			container = getAttribute(name);
			if (container == null) {
				container = new Attribute(name);
				setAttribute(name, container);
			}
		}

		return container;
	}

	/**
	 * Returns an Attribute object from this entry's list of attributes. If the
	 * attribute does not exist, a new one is created with no values.
	 * 
	 * @param name
	 *            The attribute name to create/return
	 * @param oper
	 *            The new Attribute's operation code. Only used if creating a
	 *            new Attribute.
	 * @return The Attribute object
	 */
	public Attribute newAttribute(String name, char oper) {
		Attribute leaf = newAttribute(name);
		if (leaf != null) {
			leaf.setOper(oper);
		}
		return leaf;
	}

	/**
	 * Adds a value to an attribute. If the attribute does not exist it is
	 * created with the new value. If the attribute exists, the value is
	 * appended to the attribute's list of values. If the value Object is an
	 * Attribute, you may wish to use mergeAttributeValue instead, to get the
	 * values of the Attribute added instead of the Attribute itself as a value.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The value to append
	 * @deprecated use {@link #addAttributeValue(String, Object)} instead.
	 */
	@Deprecated
	public void addAttributeValue(Object name, Object value) {
		if (name != null) {
			addAttributeValue(name.toString(), value);
		}
	}

	/**
	 * Adds a value to an attribute. If the attribute does not exist it is
	 * created with the new value. If the attribute exists, the value is
	 * appended to the attribute's list of values. If the value Object is an
	 * Attribute, you may wish to use mergeAttributeValue instead, to get the
	 * values of the Attribute added instead of the Attribute itself as a value.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The value to append
	 * @since 7.0
	 */
	public void addAttributeValue(String name, Object value) {
		Attribute a = getAttribute(name);
		if (a != null) {
			a.addValue(value);
		} else {
			setAttribute(name, value);
		}
	}

	/**
	 * Adds a value to an attribute. If the attribute does not exist it is
	 * created with the new value. If the attribute exists, the value is
	 * appended to the attribute's list of values.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The value to append
	 * @param op
	 *            The attribute value operation, AV_UNCHANGED = 0, AV_ADD = 1,
	 *            AV_DELETE = 2
	 * @deprecated use {@link #addAttributeValue(String, Object, int)} instead.
	 */
	@Deprecated
	public void addAttributeValue(Object name, Object value, int op) {
		if (name != null) {
			addAttributeValue(name.toString(), value, op);
		}
	}

	/**
	 * Adds a value to an attribute. If the attribute does not exist it is
	 * created with the new value. If the attribute exists, the value is
	 * appended to the attribute's list of values.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The value to append
	 * @param op
	 *            The attribute value operation, AV_UNCHANGED = 0, AV_ADD = 1,
	 *            AV_DELETE = 2
	 * @since 7.0
	 */
	public void addAttributeValue(String name, Object value, int op) {
		Attribute a = getAttribute(name);

		if (a != null) {
			a.addValue(value, op);
		} else {
			if (value instanceof AttributeValue) {
				((AttributeValue) value).setOper(op);
			} else {
				value = new AttributeValue(value, op);
			}
			setAttribute(name, value);
		}
	}

	/**
	 * Merges the values in one attribute with the values from another
	 * attribute. All values from attr are added, even if they already exist.
	 * 
	 * @param name
	 *            The name of the attribute into which values are merged
	 * @param attr
	 *            The attribute (e.g. entry.getAttribute("xxx")) from which
	 *            values are collected
	 * @deprecated use {@link #mergeAttributeValue(String, Attribute)} instead.
	 */
	@Deprecated
	public void mergeAttributeValue(Object name, AttributeInterface attr) {
		if (name == null) {
			return;
		}

		if (attr instanceof Attribute) {
			mergeAttributeValue(name.toString(), (Attribute) attr);
		}
	}

	/**
	 * Merges the values in one attribute with the values from another
	 * attribute. All values from <code>attr</code> are added, even if they
	 * already exist. <br>
	 * If the given Attribute is marked as protected then the result Attribute
	 * will be protected as well.
	 * 
	 * @param name
	 *            The name of the attribute into which values are merged. If
	 *            this parameter is null the call is ignored.
	 * @param attr
	 *            The attribute (e.g. entry.getAttribute("xxx")) from which
	 *            values are collected.
	 * @since 7.0
	 */
	public void mergeAttributeValue(String name, Attribute attr) {
		if (name == null) {
			return;
		}

		if (attr != null) {
			newAttribute(name, attr.getOper()).addValues(attr);
		}
	}

	/**
	 * Merges in the values from an attribute. All values from attr are added,
	 * even if they already exist. The name of attr will be used to determine
	 * which Attribute should get the new values.
	 * 
	 * @param attr
	 *            The attribute (e.g. entry.getAttribute("xxx")) from which we
	 *            get the name and values
	 */
	public void mergeAttributeValue(Attribute attr) {
		if (attr != null) {
			mergeAttributeValue(attr.isDOMEnabled() && attr.getFullName() != null ? attr.getFullName() : attr.getName(), attr);
		}
	}

	/**
	 * Returns the Attribute object for a named attribute.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The Attribute object or <i>null</i> if the attribute does not
	 *         exist
	 * @deprecated use {@link #getAttribute(String)} instead.
	 */
	@Deprecated
	public Attribute get(Object p1) {
		return getAttribute(p1);
	}

	/**
	 * Returns the Attribute object for a named attribute.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The Attribute object or <i>null</i> if the attribute does not
	 *         exist
	 * @deprecated use {@link #getAttribute(String)} instead.
	 */
	@Deprecated
	public Attribute getAttribute(Object p1) {
		return p1 != null ? getAttribute(p1.toString()) : null;
	}

	/**
	 * Returns the Attribute object for a named attribute.
	 * 
	 * @param name
	 *            The attribute name
	 * @return The Attribute object or <i>null</i> if the attribute does not
	 *         exist
	 * @since 7.0
	 */
	public Attribute getAttribute(String name) {
		if (name == null) {
			return null;
		}

		Attribute a = null;
		if (isDOMEnabled()) {
			field.tempKN.setFullName(name);
			a = internalGetAttribute(field.tempKN);
		} else {
			a = data.get(name);

			if (a == null) {
				// look for an attribute with same name, different casing
				String realName = lowerCaseMap.get(name.toLowerCase(Locale.ENGLISH));
				if (realName != null) {
					a = data.get(realName);
				}
			}
		}
		return a;
	}

	/**
	 * Returns an array of strings containing attribute names in this entry.
	 * 
	 * @return The attribute names in this entry as an array
	 */
	public String[] getAttributeNames() {
		if (isDOMEnabled()) {
			Collection<String> list = getAttributeCollection();
			return list.toArray(new String[list.size()]);
		} else {
			String[] names = new String[data.size()];
			int counter = 0;

			for (Enumeration<String> e = data.keys(); e.hasMoreElements();) {
				names[counter++] = e.nextElement();
			}

			return names;
		}
	}

	/**
	 * Returns a java.util.Collection containing attribute names in this entry.
	 * 
	 * @return The attribute names in this entry as a java.util.Collection
	 *         object
	 */
	public Collection<String> getAttributeCollection() {
		if (isDOMEnabled()) {
			if (!field.isNamesListValid) {
				field.isNamesListValid = true;
				getCachedNames();

				Attribute value = null;
				for (Map.Entry<KeyName, Attribute> record : field.hData.entrySet()) {
					value = record.getValue();

					if (value.size() > 0 || value.getChildNodes().getLength() == 0) {
						field.cachedNames.add(record.getKey().getFullName());
					}
				}
			}
			return field.cachedNames;
		} else {
			List<String> c = new ArrayList<String>();
			for (Enumeration<String> e = data.keys(); e.hasMoreElements();) {
				c.add(e.nextElement());
			}
			return c;
		}
	}

	/**
	 * Returns the first value in an attribute as a String. If the attribute
	 * does not exist or the attribute does not have any values then <i>null</i>
	 * is returned.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The string value or null
	 * @deprecated use {@link #getString(String)} instead.
	 */
	@Deprecated
	public String getString(Object p1) {
		return p1 != null ? getString(p1.toString()) : null;
	}

	/**
	 * Returns the first value in an attribute as a String. If the attribute
	 * does not exist or the attribute does not have any values then <i>null</i>
	 * is returned.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The string value or null
	 * @since 7.0
	 */
	public String getString(String p1) {
		Attribute obj = getAttribute(p1);
		if (obj != null) {
			return obj.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Returns the first value in an attribute as an object. If the attribute
	 * does not exist or the attribute does not have any values then <i>null</i>
	 * is returned.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The object value or null
	 * @deprecated use {@link #getObject(String)} instead.
	 */
	@Deprecated
	public Object getObject(Object p1) {
		return p1 != null ? getObject(p1.toString()) : null;
	}

	/**
	 * Returns the first value in an attribute as an object. If the attribute
	 * does not exist or the attribute does not have any values then <i>null</i>
	 * is returned.
	 * 
	 * @param p1
	 *            The attribute name
	 * @return The object value or null
	 * @since 7.0
	 */
	public Object getObject(String p1) {
		Attribute obj = getAttribute(p1);
		if (obj != null) {
			return obj.getValue(0);
		} else {
			return null;
		}
	}

	/**
	 * Removes an attribute from this Entry's list of attribute.
	 * 
	 * @param p1
	 *            The name of the attribute to remove
	 * @deprecated use {@link #removeAttribute(String)} instead.
	 */
	@Deprecated
	public void removeAttribute(Object p1) {

		if (p1 instanceof Attribute) {
			removeAttribute(((Attribute) p1).getName());
		} else if (p1 != null) {
			removeAttribute(p1.toString());
		}
	}

	/**
	 * Removes an attribute from this Entry's list of attributes. <br>
	 * This method removes only the first attribute that could be accessed by
	 * the {@link #getAttribute(String)} method. If there is another attribute
	 * which is not mapped to this Entry it will not be removed by a subsequent
	 * call. To remove such Attributes use the provided DOM API.
	 * 
	 * <br>
	 * <br>
	 * <b>Note: </b> When this entry is using a DOM tree the removal of a value
	 * which is a parent of another attribute (e.g. calling
	 * <code>entry.removeAttribute("first.second");</code> when there is an
	 * attribute called "first.second.third") will not really remove that
	 * attribute from the tree. That attribute will only be cleared. This is
	 * done for optimization reasons. Once removed that attribute will not show
	 * in the array returned by {@link #getAttributeNames()} method. However
	 * that Attribute will still be accessible through the DOM API.
	 * 
	 * @param name
	 *            The name of the attribute to remove
	 */
	public void removeAttribute(String name) {
		if (name == null) {
			return;
		}

		if (isDOMEnabled()) {
			field.tempKN.setFullName(name);

			Attribute removed = internalGetAttribute(field.tempKN);

			if (removed != null) {
				// found the child to remove... now see how to do the removal...
				if (removed.getChildNodes().getLength() - removed.size() == 0) {
					// disconnect the child from the tree, it has no children

					Attribute parent = removed.getParentNode();
					if (parent != null) {

						// remove from the parent's list
						parent.internalRemoveChild(removed);

						// remove from the entry's map
						internalRemoveAttribute(field.tempKN, false, true);

						// advance to the top of the tree
						removed = parent;
						parent = removed.getParentNode();

						// cycle from bottom to top and remove all the empty
						// attributes
						while (removed.getChildNodes().getLength() == 0 && removed.size() == 0) {
							if (parent != null) {
								// remove from the parent's list
								parent.internalRemoveChild(removed);

								// tempKN is the parsed name of the attribute to
								// be
								// removed.
								field.tempKN.parseName(field.tempKN.getPrefix());

								// remove from the entry's map
								internalRemoveAttribute(field.tempKN, false, true);

								// advance to the top of the tree
								removed = parent;
								parent = removed.getParentNode();
							} else {
								// the Attribute is attached to this entry
								// directly...
								field.tempKN.setFullName(field.tempKN.getPrefix());
								internalRemoveAttribute(field.tempKN, true, true);
								// no way above the entry level...
								break;
							}
						}
					} else {
						// disconnect from the entry
						internalRemoveAttribute(field.tempKN, true, true);
					}
				} else if (removed.size() > 0) {
					// has children... just clear the values.
					removed.clear();
				}
			}
		} else {
			name = lowerCaseMap.remove(name.toLowerCase(Locale.ENGLISH));

			if (name != null) {
				Attribute attr = data.remove(name);
				if (attr != null) {
					attr.doc = null;
				}
			}
		}
	}

	/**
	 * Removes all attributes from this Entry.
	 * 
	 */
	public void removeAllAttributes() {
		if (isDOMEnabled()) {
			for (Attribute attr : field.hData.values()) {
				attr.setFullName(null);
			}

			for (Attribute child : field.children) {
				child.disconnect();
			}

			field.children.clear();
			// so far the child attributes should have been disconnected, so
			// clean
			// the structures that still have reference to the Attributes.
			field.hData.clear();
			if (field.cachedNames != null) {
				field.isNamesListValid = true;
				field.cachedNames.clear();
			}
		} else {
			data.clear();
			lowerCaseMap.clear();
		}
	}

	/**
	 * Returns the number of attributes present in this entry.
	 * 
	 * @return Number of attributes contained in this entry
	 */
	public int size() {
		if (isDOMEnabled()) {
			return getAttributeCollection().size();
		} else {
			return data.size();
		}
	}

	/**
	 * Returns a string representation of this entry.
	 * 
	 * @return All attribute names and values as a structured string
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(512);
		internalToString(buf, false);
		return buf.toString();
	}

	/**
	 * Returns a string representation of this entry, including delta
	 * information.
	 * 
	 * @return All attribute names and values as a structured string, and also
	 *         delta information if present.
	 */
	public String toDeltaString() {
		StringBuilder buf = new StringBuilder(512);
		internalToString(buf, true);
		return buf.toString();
	}

	/**
	 * Used to construct the string output by the {@link #toString()} and the
	 * {@link #toDeltaString()} methods.
	 * 
	 * @param result
	 *            the {@link StringBuilder} to write to
	 * @param showDelta
	 *            if this is true then delta information will be included other
	 *            wise no such information will be written.
	 * @since 7.0
	 */
	final void internalToString(StringBuilder result, boolean showDelta) {
		result.append('{');

		if (showDelta) {
			result.append('\n');
			result.append('\t');
			outputName(result, "#type");
			outputValue(result, getOperation(), false);

			result.append('\n');
			result.append('\t');
			outputName(result, "#count");
			outputValue(result, size(), false);
		}

		if (properties != null) {
			// output properties
			for (Map.Entry<String, Object> prop : properties.entrySet()) {
				result.append('\n');
				result.append('\t');
				Entry.outputName(result, "@" + prop.getKey());
				Entry.outputValue(result, prop.getValue(), false);
			}
		}

		int attrSize = 0;
		Collection<Attribute> children = null;
		if (isDOMEnabled()) {
			children = this.field.children;
			attrSize = this.field.children.size();
		} else {
			children = this.data.values();
			attrSize = this.data.size();
		}

		// output attributes
		for (Attribute child : children) {
			result.append('\n');
			result.append('\t');
			child.internalToString(result, 2, showDelta);
			result.append(',');
		}

		if (attrSize != 0 || (properties != null && properties.size() != 0) || showDelta) {
			result.deleteCharAt(result.length() - 1);
			result.append('\n');
		}
		result.append('}');
	}

	/**
	 * Used to output the name of the Attribute when doing the toString()
	 * routine.
	 * 
	 * @param result
	 *            the place to write into.
	 * @param name
	 *            the name of the attribute to output.
	 */
	static final void outputName(StringBuilder result, String name) {
		result.append("\"" + name + "\": ");
	}

	/**
	 * Used to output the value of the Attribute when doing the toString()
	 * routine.
	 * 
	 * @param result
	 *            the place to write into.
	 * @param value
	 *            the name of the attribute to output.
	 * @param protect
	 *            specifies whether the value should be dumped or not.
	 */
	static final void outputValue(StringBuilder result, Object value, boolean protect) {

		if (protect) {
			result.append("******");
		} else {
			if (value instanceof byte[]) {
				byte[] bytes = (byte[]) value;

				result.ensureCapacity(6 * bytes.length + 2);
				result.append('[');

				for (byte b : bytes) {
					result.append("0x" + Integer.toString(b, 16) + ", ");
				}

				result.delete(result.length() - 2, result.length());
				result.append(']');

			} else if (value instanceof Number || value instanceof Character || value instanceof Boolean) {
				result.append(value);
			} else if (value != null) {
				result.append("\"" + value.toString() + "\"");
			} else {
				result.append("null");
			}
		}

		result.append(',');
	}

	/**
	 * Returns a property value.
	 * 
	 * @param propertyName
	 *            The name of the property
	 * @return The property's value or null if no such property exists
	 * 
	 * @deprecated use {@link #getProperty(String)} instead.
	 */
	@Deprecated
	public Object getProperty(Object propertyName) {
		return propertyName != null ? getProperty(propertyName.toString()) : null;
	}

	/**
	 * Returns a property value.
	 * 
	 * @param propertyName
	 *            The name of the property
	 * @return The property's value or null if no such property exists
	 * @since 7.0
	 */
	public Object getProperty(String propertyName) {
		if (propertyName == null || properties == null) {
			return null;
		}

		Object a = properties.get(propertyName);

		if (a == null) {
			propertyName = lowercaseProps.get(propertyName.toLowerCase(Locale.ENGLISH));
			if (propertyName != null) {
				a = properties.get(propertyName);
			}
		}

		return a;
	}

	/**
	 * Sets/replaces a property.
	 * 
	 * @param propertyName
	 *            The name of the property
	 * @param propertyValue
	 *            The named property's value
	 * @deprecated use {@link #setProperty(String, Object)} insetad.
	 */
	@Deprecated
	public void setProperty(Object propertyName, Object propertyValue) {
		if (propertyName != null) {
			setProperty(propertyName.toString(), propertyValue);
		}
	}

	/**
	 * Sets/replaces a property.
	 * 
	 * @param propertyName
	 *            The name of the property
	 * @param propertyValue
	 *            The named property's value
	 * @since 7.0
	 */
	public void setProperty(String propertyName, Object propertyValue) {
		if (propertyValue == null) {
			propertyName = getLowercaseProps().remove(propertyName.toLowerCase(Locale.ENGLISH));
			if (propertyName != null) {
				getProperties().remove(propertyName);
			}
		} else {
			String realName = null;
			realName = getLowercaseProps().put(propertyName.toLowerCase(Locale.ENGLISH), propertyName);

			if (realName != null && !realName.equals(propertyName)) {
				getProperties().remove(realName);
			}

			getProperties().put(propertyName, propertyValue);
		}
	}

	/**
	 * Returns <i>true</i> if a property named by the propertyName parameter has
	 * a value.
	 * 
	 * @return True if such property exists
	 */
	public boolean hasProperty(String propertyName) {
		if (propertyName == null)
			return false;

		return lowercaseProps != null && lowercaseProps.containsKey(propertyName.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Returns a string array of the property names contained in this entry.
	 * 
	 * @return Array of strings with property names
	 */
	public String[] getPropertyNames() {
		String[] names = null;
		if (properties != null) {
			Set<String> keys = properties.keySet();
			names = keys.toArray(new String[keys.size()]);
		} else {
			names = new String[0];
		}

		return names;
	}

	/**
	 * Convenience method that calls <i>merge ( e, false )</i>.
	 * 
	 * @param e
	 *            The entry from which attributes are collected
	 */
	public void merge(Entry e) {
		merge(e, false);
	}

	/**
	 * Merges in the attributes and their values from another entry. After the
	 * operation this entry contains all the attributes combined. For attributes
	 * with the same name, the result will be the attributes from the other
	 * entry if <i>mergevalues</i> is false. Properties are copied from the
	 * <i>e</i> entry and overwrite any existing properties.
	 * <p>
	 * Example: This entry contains these attributes Name Values a 1 b 1, 2 The
	 * other entry contains these attributes Name Values b 3 c 4
	 * 
	 * After the merge, mergevalues=FALSE, this entry will contain Name Values a
	 * 1 b 3 c 4
	 * 
	 * After the merge, mergevalues=TRUE, this entry will contain Name Values a
	 * 1 b 1, 2, 3 c 4
	 * 
	 * @param e
	 *            The entry from which attributes are collected
	 * @param mergevalues
	 *            if false replace values, if true add values
	 */
	public void merge(Entry e, boolean mergevalues) {

		if (e.properties != null) {
			for (Map.Entry<String, Object> prop : e.properties.entrySet()) {
				setProperty(prop.getKey(), prop.getValue());
			}
		}

		Collection<Attribute> children = null;

		if (e.isDOMEnabled()) {
			children = e.field.children;
		} else {
			children = e.data.values();
		}

		for (Attribute child : children) {
			Attribute attr = mergevalues ? getAttribute(child.getName()) : null;
			if (attr != null)
				attr.merge(child);
			else
				setAttribute(child);
		}
	}

	/**
	 * Adds the child to the children ArrayList. The idea here is to keep the
	 * order of the Attributes as they were add to the Entry so we could cycle
	 * through the child elements of the Document. This method also adds the
	 * Attribute to the data map and fixes the doc/parent references of the
	 * Attribute as well. If the passed attribute is coming from another entry
	 * then it is cloned first and the clone is added to this entry.
	 * 
	 * This method is used internally.
	 * 
	 * @param kn
	 *            the name of the object to use. uses only the FullName field of
	 *            the KeyName class.
	 * 
	 * @param attr
	 *            the Attribute to add to the entry.
	 * @param addAsFirstLevelChild
	 *            specifies whether this Attribute will be added directly to
	 *            this Entry or somewhere down the hierarchy.
	 * 
	 * @param brandNewAttr
	 *            tells the method whether this an empty attribute or not. This
	 *            is done to avoid calling Attribute.getChildNodes() which
	 *            results in a new object createion.
	 * 
	 * @since 7.0
	 */
	final Attribute internalPutAttribute(KeyName kn, Attribute attr, boolean addAsFirstLevelChild, boolean addSubTree) {

		if (addAsFirstLevelChild) {
			// these checks are only done when attaching an attribute on the top
			// level. If an attribute is attached down the hierarchy its parent
			// is making the checks for its validity.
			if (attr.getOwnerDocument() == null) {
				// brand new attribute object... adopt it
				adoptNode(attr);
			}

			if (attr.getOwnerDocument() != this) {
				// this node is coming from other entry so just clone it and
				// add it
				attr = attr.cloneNode(true);

				// fix the document of the element and all its descendants also
				// change the parent of the element to null
				adoptNode(attr);
			}

			// add it to our lists
			field.children.add(attr);
		}

		attr.setFullName(kn.getFullName());
		Attribute result = field.hData.put(kn, attr);

		// notify the Entry that the tree has changed.
		invalidateNamesList();

		if (result != null && result != attr) {

			if (result.getChildNodes().getLength() > result.size()) {
				// remove its children which were also indexed...
				removeSubTree(result.getFullName());
			}

			// we already used the name for fast-removing of the sub-tree so
			// now
			// we could clear the field.
			result.setFullName(null);

			if (addAsFirstLevelChild) {
				// remove the child from
				field.children.remove(result);
			}

			result.disconnect();
		}

		if (addSubTree && attr.getChildNodes().getLength() > attr.size()) {
			// add the new Attribute's children...
			addSubTree(attr, kn.getFullName(), null);
		}

		return result;
	}

	/**
	 * Gets the attribute from the data map. Every KeyName has as hashCode the
	 * value that the lowercase representation of a normal name would have. This
	 * helps to compare integers instead of lowercase strings. After the
	 * hashCode is matched the KeyName.equals() is called. Look that one for
	 * details on how the search in the map is optimized and is case insensitive
	 * at the same time.
	 * 
	 * @param key
	 *            the name of the Attribute to look for. Uses only FullName and
	 *            lcFullName (if necessary) fields.
	 * @since 7.0
	 */
	final Attribute internalGetAttribute(KeyName kn) {
		return field.hData.get(kn);
	}

	/**
	 * Removes the child from the children ArrayList. This method is called when
	 * an Attribute is removed from the Entry. It is also removed from the
	 * children list. The references to the doc/parent of the Attribute are set
	 * to null.
	 * 
	 * This method should be used only when the DOM API is enabled, otherwise an
	 * unexpected error might occur.
	 * 
	 * @param kn
	 *            the name of the Attribute which will be removed. Uses only
	 *            FullName and lcFullName fields.
	 * @param removeFirstLevelChild
	 *            tells the method that the child to be removed should be
	 *            attached as a direct child of this Entry.
	 * @param disconnect
	 *            specifies whether this method should call the
	 *            {@link #disconnect()} method on the removed Attribute. This is
	 *            provided as optimization for the Attributes which are only
	 *            renamed by attaching and re-attaching to the entry.
	 * 
	 * @since 7.0
	 */
	final Attribute internalRemoveAttribute(KeyName kn, boolean removeFirstLevelChild, boolean disconnect) {
		Attribute attr = null;

		attr = field.hData.remove(kn);

		if (attr != null) {

			// notify the Entry that the tree has changed.
			invalidateNamesList();

			if (attr.getChildNodes().getLength() > attr.size()) {
				// remove its children which were also indexed...
				removeSubTree(attr.getFullName());
			}

			attr.setFullName(null);

			if (removeFirstLevelChild) {
				field.children.remove(attr);
			}

			if (disconnect) {
				attr.disconnect();
			}
		}

		return attr;
	}

	/**
	 * Traverses the children of the provided Attribute and add their names to
	 * the data map for faster access later.
	 * 
	 * <b>This should be called only if {@link #isDOMEnabled()} returns true,
	 * otherwise the result is unpredictable.</b>
	 * 
	 * @param parent
	 *            the Attribute which children will be scanned.
	 * @param fullName
	 *            the full name of the parent Attribute which it is mapped
	 *            under.
	 * @param set
	 *            that contains the names which will not be added if
	 *            encountered. If null an empty is auto-created.
	 */
	final void addSubTree(Attribute parent, String fullName, Set<KeyName> set) {
		if (set == null) {
			set = new HashSet<KeyName>();
		}

		fullName += PATH_SEPARATOR_STR;
		KeyName kn = null;
		NodeList children = parent.getChildNodes();
		Attribute node = null;
		for (int i = 0; i < children.getLength(); ++i) {

			if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
				node = (Attribute) children.item(i);
				kn = new KeyName(fullName + node.getName());

				if (set.add(kn)) {
					field.hData.put(kn, node);
					node.setFullName(kn.getFullName());
				}

				if (node.getChildNodes().getLength() > node.size()) {
					addSubTree(node, kn.getFullName(), set);
				}
			}
		}
	}

	/**
	 * Searches the data map for fullNames starting with the provided prefix.
	 * When found those names are removed from the data map. The mapped
	 * Attributes are set fullName to null.
	 * 
	 * <b>This should be called only if {@link #isDOMEnabled()} returns true,
	 * otherwise the result is unpredictable.</b>
	 * 
	 * @param fullName
	 *            the fullName.
	 */
	final void removeSubTree(String fullName) {
		if (field.toRemove == null) {
			field.toRemove = new ArrayList<KeyName>();
		}

		fullName += PATH_SEPARATOR_STR;

		for (KeyName key : field.hData.keySet()) {
			if (key.getFullName().startsWith(fullName)) {
				field.toRemove.add(key);
			}
		}

		for (KeyName key : field.toRemove) {
			field.hData.remove(key).setFullName(null);
		}

		field.toRemove.clear();
	}

	/**
	 * @return the lowercaseProps
	 * @since 7.0
	 */
	private Map<String, String> getLowercaseProps() {
		if (lowercaseProps == null) {
			lowercaseProps = new Hashtable<String, String>();
		}

		return lowercaseProps;
	}

	/**
	 * @return the lowercaseProps
	 * @since 7.0
	 */
	private Map<String, Object> getProperties() {
		if (properties == null) {
			properties = new Hashtable<String, Object>(1);
		}

		return properties;
	}

	private Collection<String> getCachedNames() {
		if (field.cachedNames == null) {
			field.cachedNames = new ArrayList<String>(1);
		}
		return field.cachedNames;
	}

	/**
	 * Raises the flag that means the tree have been updated to tell the Entry
	 * to clear its catches.
	 * 
	 * @since 7.0
	 */
	protected void invalidateNamesList() {
		if (isDOMEnabled() && field.isNamesListValid) {
			field.isNamesListValid = false;
			getCachedNames().clear();
		}
	}

	/**
	 * @return <code>true</code> if this entry is storing the information as DOM
	 *         tree. If the entry has a pre 7.0 flat structure this method
	 *         returns <code>false</code>.
	 */
	public boolean isDOMEnabled() {
		return field != null;
	}

	/**
	 * This method changes the entry from pre 7.0 flat structure to a
	 * hierarchical one. This method could only be called once, after that
	 * calling this method will have no impact on the entry's structure. This
	 * method is called implicitly when one of the tree/hierarchy related DOM
	 * APIs are called (e.g. {@link #getChildNodes()}.
	 */
	public void enableDOM() {
		if (!isDOMEnabled()) {
			field = new FieldsOptimizer(data.size());

			Attribute parent = null;
			Attribute current = null;
			KeyName key = null;
			Attribute existing = null;

			List<String> keys = java.util.Collections.list(lowerCaseMap.keys());
			java.util.Collections.sort(keys);
			String normalKey = null;

			for (String lowercaseKey : keys) {
				normalKey = lowerCaseMap.get(lowercaseKey);

				key = new KeyName(normalKey, lowercaseKey);
				current = data.get(normalKey);

				existing = internalGetAttribute(key);
				// Note: This check here should fail, since we have a sorting of
				// the keys but I will leave this check here for a little bit
				// longer.
				if (existing != null) {
					// in this case the entry had a couple of attributes which
					// one of them represented the parent of the other one...
					// but since the newAttribute call bellow might had already
					// created the parent we need to do an extra check.
					// e.g. We have: smpl.operation.type and smpl.operation
					// attributes... here the first one is processed first which
					// creates the spml.operation attribute.
					existing.addValues(current);
					if (current.isDOMEnabled()) {
						// don't forget to set the namespace...
						existing.rename(existing.getName(), current.getNamespaceURI());
					}
				} else {
					parent = newAttribute(key.getPrefix());

					// detach the child from the Entry to avoid any
					// complications while changing the Attribute's name.
					current.doc = null;
					current.setName(key.getName());

					if (parent == null) {
						internalPutAttribute(key, current, true, true);
					} else {
						parent.internalInsertBefore(current, null);
						internalPutAttribute(key, current, false, true);
					}
				}
			}

			// we don't need the old-style maps
			data = null;
			lowerCaseMap = null;
		}
	}

	/**
	 * Execute an XPath expression and get the result as an NodeList
	 * 
	 * @param xPath
	 *            the XPath expression
	 * 
	 * @exception XPathExpressionException
	 *                in case an invalid expression is passed
	 * 
	 * @return All the nodes found by the XPath engine.
	 * 
	 * @since 7.0
	 */
	public NodeList getNodeList(String xPath) throws XPathExpressionException {
		return (NodeList) getObject(xPath, XPathConstants.NODESET);
	}

	/**
	 * Execute an XPath expression and get the result as an Attribite *
	 * 
	 * @param xPath
	 *            the XPath expression
	 * 
	 * @exception XPathExpressionException
	 *                in case an invalid expression is passed
	 * @return the first Node out of all the nodes found
	 * 
	 * @since 7.0
	 */
	public Attribute getFirstAttribute(String xPath) throws XPathExpressionException {
		return (Attribute) getObject(xPath, XPathConstants.NODE);
	}

	/**
	 * Execute an XPath expression and get the result as an String *
	 * 
	 * @param xPath
	 *            the XPath expression
	 * 
	 * @exception XPathExpressionException
	 *                in case an invalid expression is passed
	 * @return The result as String
	 * 
	 * @since 7.0
	 */
	public String getStringValue(String xPath) throws XPathExpressionException {
		return (String) getObject(xPath, XPathConstants.STRING);
	}

	/**
	 * Execute an XPath expression and get the result as an Number *
	 * 
	 * @param xPath
	 *            the XPath expression
	 * 
	 * @exception XPathExpressionException
	 *                in case an invalid expression is passed
	 * 
	 * @return The result as Number
	 * 
	 * @since 7.0
	 */
	public Number getNumberValue(String xPath) throws XPathExpressionException {
		return (Number) getObject(xPath, XPathConstants.NUMBER);
	}

	/**
	 * Execute an XPath expression and get the result as an Boolean *
	 * 
	 * @param xPath
	 *            the XPath expression
	 * 
	 * @exception XPathExpressionException
	 *                in case an invalid expression is passed
	 * 
	 * @return The result as Boolean
	 * 
	 * @since 7.0
	 */
	public Boolean getBooleanValue(String xPath) throws XPathExpressionException {
		return (Boolean) getObject(xPath, XPathConstants.BOOLEAN);
	}

	/**
	 * executes the XPath expression and return the result as an Object
	 * 
	 * @param xPath
	 *            - the XPath expression
	 * @param returnType
	 *            - the type of the object that the XPath engine will return
	 * @return the found object
	 * @throws XPathExpressionException
	 *             in case an invalid expression is passed
	 * 
	 * @since 7.0
	 */
	private Object getObject(String xPath, QName returnType) throws XPathExpressionException {

		enableDOM();

		if (field.xPathObj == null) {
			field.xPathObj = XPathFactory.newInstance().newXPath();
		}

		return field.xPathObj.evaluate(xPath, this, returnType);
	}

	/**
	 * Entry/Attribute objects serialized in pre 7.0 release and then
	 * deserialized in a 7.0+ release are getting some of their member vars
	 * nullified. This code helps us deal with this when the object is
	 * deserialized.
	 * 
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @since 7.0
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		// our "pseudo-constructor"
		// this will only "revive" the following fields:
		// * data
		// * lowerCaseMap
		// * operation
		// * properties
		// * lowercaseProps
		in.defaultReadObject();

		// see if we are hierarchical
		try {

			boolean domEnabled = in.readBoolean();

			if (domEnabled) {
				field = (FieldsOptimizer) in.readObject();
				field.tempKN = new KeyName();
				field.isNamesListValid = true;
				field.cachedNames = Collections.list(data.keys());

				// make sure at least the top-most Attribute knows where it is
				// attached at (we don't serialize the Attribute's doc field for
				// performance reasons).
				for (Attribute attr : field.children) {
					attr.doc = this;
				}

				// we don't need the old-style maps any more...
				data = null;
				lowerCaseMap = null;

				return;
			}
		} catch (IOException ioe) {
			// pre 7.0 entry was received.
		}

		// even if we are flat entry coming from 6.1.1 in 7.0 we rely on the
		// doc field for the DOMenabled functionality.
		for (Map.Entry<String, Attribute> attr : data.entrySet()) {
			attr.getValue().doc = this;
		}
	}

	/**
	 * Serialize the entry with some custom logic.
	 * 
	 * @param out
	 *            the output stream to write to.
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {

		if (isDOMEnabled()) {
			if (field.isNamesListValid) {
				// convert the new data map to the old entry format
				lowerCaseMap = new Hashtable<String, String>(getCachedNames().size());
				data = new Hashtable<String, Attribute>(getCachedNames().size());
			} else {
				lowerCaseMap = new Hashtable<String, String>();
				data = new Hashtable<String, Attribute>();
			}

			for (Map.Entry<KeyName, Attribute> attr : field.hData.entrySet()) {
				if (attr.getValue().size() > 0 || attr.getValue().getChildNodes().getLength() == 0) {
					lowerCaseMap.put(attr.getKey().getLCFullName(), attr.getKey().getFullName());
					data.put(attr.getKey().getFullName(), attr.getValue());
				}
			}
		}

		// make sure we have properties structures created... pre-7.0 expects
		// these to be always created.
		getProperties();
		getLowercaseProps();

		// this call outputs all the fields TDI 611 Entry recognizes... nothing
		// more. These fields include:
		// * data
		// * lowerCaseMap
		// * operation
		// * properties
		// * lowercaseProps
		out.defaultWriteObject();

		if (isDOMEnabled()) {
			// first we output the domEnabled flag
			out.writeBoolean(true);

			out.writeObject(field);

			// the rest of the fields used in a DOM enabled entry are only
			// cache/optimization objects which are created based on the objects
			// we serialized above. Don't need to serialize them because the
			// deserialization will be much slower than creating them on the
			// other side.
		} else {
			// only output the domEnabled flag
			out.writeBoolean(false);
		}
	}

	/**
	 * Returns a XML representation of this Entry object. Note: if the Entry
	 * contains multiple attributes then the resultant XML will be multi-rooted.
	 * 
	 * @return the XML String.
	 * @throws Exception if an error occurs 
	 * @since 7.0
	 */
	public String toXML() throws Exception {

		StringWriter out = new StringWriter();
			Class<?> xmlParserClass = Class.forName("com.ibm.di.parser.xml.XMLParser2");
			Object xmlParser = xmlParserClass.newInstance();
			xmlParserClass.getMethod("setOutputStream", Writer.class).invoke(xmlParser, out);

			xmlParserClass.getMethod("setParam", String.class, String.class).invoke(xmlParser, "omit.xml.decl.on.writing", "true");
			xmlParserClass.getMethod("setParam", String.class, String.class).invoke(xmlParser, "indent.output", "true");

			xmlParserClass.getMethod("initParser").invoke(xmlParser);
			xmlParserClass.getMethod("writeEntry", Entry.class).invoke(xmlParser, this);
			xmlParserClass.getMethod("closeParser").invoke(xmlParser);

		return out.toString();
	}

	/**
	 * Returns an Entry object constructed from a XML String.
	 * 
	 * @param string The XML String used to construct the new Entry
	 * @param xPath Sets the simple XPath expression used to find entries.
	 * @param entryTag The name of the element which wraps each Entry.
	 * @throws Exception if an error occurs during parsing
	 * @return the new Entry.
	 * @since 7.2
	 */
	public static Entry fromXML(String string, String xPath, String entryTag) throws Exception {

			Class<?> parserClass = Class.forName("com.ibm.di.parser.xml.XMLParser2");
			Object parser = parserClass.newInstance();

			if (xPath != null)
				parserClass.getMethod("setParam", String.class, String.class).invoke(parser, "xpath.expr", xPath);
			if (entryTag != null)
				parserClass.getMethod("setParam", String.class, String.class).invoke(parser, "entry.tag", entryTag);
			parserClass.getMethod("setParam", String.class, String.class).invoke(parser, "omit.xml.decl.on.reading", "true");

			parserClass.getMethod("setInputStream", Reader.class).invoke(parser, new StringReader(string));

			parserClass.getMethod("initParser").invoke(parser);
			Entry e = (Entry) parserClass.getMethod("readEntry").invoke(parser);
			parserClass.getMethod("closeParser").invoke(parser);
			return e;

	}

	/**
	 * Returns a JSON representation of this Entry object. 
	 * 
	 * @return the JSON String.
	 * @throws Exception if an error occur
	 * @since 7.2
	 */
	public String toJSON() throws Exception {

		StringWriter out = new StringWriter();
			Class<?> parserClass = Class.forName("com.ibm.di.parser.JSONParser");
			Object parser = parserClass.newInstance();
			parserClass.getMethod("setOutputStream", Writer.class).invoke(parser, out);

			parserClass.getMethod("initParser").invoke(parser);
			parserClass.getMethod("writeEntry", Entry.class).invoke(parser, this);
			parserClass.getMethod("closeParser").invoke(parser);

		return out.toString();
	}

	/**
	 * Returns an Entry constructed from a JSON string
	 * 
	 * @param string The JSON string used to set values in the new Entry.
	 * @return The new Entry
	 * @throws Exception if an error occurs during parsing
	 * @since 7.2
	 */
	public static Entry fromJSON(String string) throws Exception {

			Class<?> parserClass = Class.forName("com.ibm.di.parser.JSONParser");
			Object parser = parserClass.newInstance();
			parserClass.getMethod("setInputStream", Reader.class).invoke(parser, new StringReader(string));

			parserClass.getMethod("initParser").invoke(parser);
			Entry e = (Entry) parserClass.getMethod("readEntry").invoke(parser);
			parserClass.getMethod("closeParser").invoke(parser);
			return e;
	}

	/**
	 * fullName = prefix + "." + name
	 * 
	 * @since 7.0
	 */
	final static class KeyName implements Cloneable, Serializable {

		private static final long serialVersionUID = 1958264649610037783L;

		// escaped strings
		private String fullName;
		private String lcFullName;
		private String prefix;
		private String name;

		/**
		 * This is a prediction of what the hashCode of the lower case name
		 * would look like. It is used to avoid the expensive
		 * String.toLowerCase() call when possible.
		 */
		private int lcHashCode;

		public KeyName() {
		}

		public KeyName(String fullName) {
			setFullName(fullName);
		}

		public KeyName(String fullName, String lcFullName) {
			this.fullName = fullName;
			this.lcFullName = lcFullName;
			this.lcHashCode = lcFullName.hashCode();
		}

		/**
		 * If you are sure you are going to need either the prefix or the name
		 * fields parse the FullName with this method, otherwise set the full
		 * name with {@link #setFullName(String)}. This method will reset all
		 * the internal fields. <br>
		 * <br>
		 * fullName = prefix + "." + name
		 * 
		 * @param aName
		 *            the full name to parse.
		 */
		void parseName(String aName) {
			// we have realized that the prefix and name will be needed so
			// parse them as well.
			setFullName(aName);

			if (aName != null) {

				// do this in case the aName is not a composite one.
				name = aName;

				int end = aName.length() - 1;
				while ((end = aName.lastIndexOf(PATH_SEPARATOR_CHAR, --end)) > 0) {
					if (aName.charAt(end - 1) != ESCAPE_CHAR) {
						prefix = aName.substring(0, end);
						name = aName.substring(end + 1);
						break;
					} else {
						int current = end - 2;
						while (current >= 0 && aName.charAt(current) == ESCAPE_CHAR) {
							--current;
						}

						if ((end - 1 - current) % 2 == 0) {
							// escaped backslash!
							prefix = aName.substring(0, end);
							name = aName.substring(end + 1);
							break;
						}
						end = current;
					}
				}
			}
		}

		/**
		 * @return the lower case String representing the fullName field. This
		 *         field is created on demand.
		 */
		String getLCFullName() {
			if (lcFullName == null && fullName != null) {
				lcFullName = fullName.toLowerCase(Locale.ENGLISH);
			}

			return lcFullName;
		}

		/**
		 * @return the fullName field.
		 */
		String getFullName() {
			return fullName;
		}

		/**
		 * Sets the fullName field to the provided value. The rest of the fields
		 * are reset.
		 * 
		 * @param fullName
		 *            the fullName to set.
		 */
		void setFullName(String fullName) {
			if (this.fullName == null || !this.fullName.equals(fullName)) {
				// we fall into this case when the getName() or getPrefix() are
				// called.
				this.fullName = fullName;
				this.prefix = null;
				this.name = null;
				lcHashCode = 0;

				boolean keyIsLC = true;
				if (fullName != null) {
					int codePoint = 0;
					/*
					 * Calculate the lower case name's hash code the same way
					 * the call name.toLowerCase().hashCode() would.
					 */
					for (int j = 0; j < fullName.length(); ++j) {
						codePoint = fullName.charAt(j);

						if ('A' <= codePoint && codePoint <= 'Z') {
							lcHashCode = 31 * lcHashCode + (codePoint | 0x20);
							keyIsLC = false;
						} else if (codePoint < 128) {
							lcHashCode = 31 * lcHashCode + codePoint;
						} else {
							lcHashCode = 31 * lcHashCode + Character.toLowerCase(codePoint);
							keyIsLC = false;
						}
					}
				}

				// this will skip the toLowerCase() call in the cases when
				// the user is using a lowerCase string.
				if (keyIsLC) {
					this.lcFullName = fullName;
				} else {
					this.lcFullName = null;
				}
			}
		}

		/**
		 * @return the prefix or null if the fullName field does not have a
		 *         prefix.
		 */
		String getPrefix() {
			if (prefix == null && name == null && fullName != null) {
				parseName(fullName);
			}
			return prefix;
		}

		/**
		 * @return the name or null if the provided fullName is null.
		 */
		String getName() {
			if (name == null && fullName != null) {
				parseName(fullName);
			}
			return name;
		}

		/**
		 * @return the hash code of the lower-case representation of the
		 *         provided full name.
		 */
		@Override
		public int hashCode() {
			return lcHashCode;
		}

		/**
		 * @return true if the fullName is equal to the provided fullName or the
		 *         lower case representations match.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof KeyName) {

				return
				// String.toLowerCase() is an expensive call so lets see if the
				// normal names are identical first.
				(fullName.hashCode() == ((KeyName) obj).fullName.hashCode() && fullName.hashCode() != 0 && fullName
						.equals(((KeyName) obj).fullName))
						// ok so either the hashCodes don't match or the
						// regionMatch returned false.
						// Our last resort is the lower case regionMatch
						|| getLCFullName().equals(((KeyName) obj).getLCFullName());
			}
			return false;
		}

		/**
		 * @return a clone of this object.
		 */
		@Override
		protected KeyName clone() {
			try {
				return (KeyName) super.clone();
			} catch (CloneNotSupportedException cnse) {
				return null;
			}
		}

		/**
		 * @return the string representation.
		 */
		@Override
		public String toString() {
			return fullName;
		}
	}

	// DOM implementation starts here...

	/**
	 * 
	 * This method adopts the provided as parameter Node by setting its parent
	 * to null and setting the document reference to this entry object. When
	 * this method is called the Node is not being added to the Entry, it will
	 * only point to the Entry as its Document. The node will be detached by its
	 * parent before this method completes.
	 * 
	 * Does not make the Entry hierarchical.
	 * 
	 * @param source
	 *            the Node which parents to change.
	 * @return The same Node but with changed parent and document references.
	 * @exception DOMException
	 *                in case the Node is not an instance of the NodeImpl class.
	 * 
	 * @since 7.0
	 */
	public Node adoptNode(Node source) throws DOMException {
		if (!(source instanceof NodeImpl)) {
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "adoptNode", "NodeImpl", "source" });
		}

		((NodeImpl) source).connect(this, null);

		return source;
	}

	/**
	 * This method creates a new Property object that represents an element's
	 * attribute in terms of DOM concepts. The namespace of that property will
	 * be set to null.
	 * <p>
	 * This method implements org.w3c.dom.Document's createAttribute method and
	 * only creates a new Property but not set it on an Attribute object. This
	 * means that until you call {@link Attribute#setAttributeNode(Attr)} with
	 * the newly created Property object it will not be visible trough
	 * Attribute's methods like getAttribute(String), getAttributes(), etc.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = system.newEntry();
	 * var hattr1 = entry.createElement(&quot;mother&quot;);
	 * entry.appendChild(hattr1);
	 * 
	 * var attr = entry.createAttribute(&quot;name&quot;);
	 * var attr1 = entry.createAttribute(&quot;age&quot;);
	 * var attr2 = entry.createAttribute(&quot;work&quot;);
	 * 
	 * attr1.setValue(&quot;33&quot;);
	 * attr2.setValue(&quot;IBM Corporation&quot;);
	 * attr.setValue(&quot;Marina&quot;);
	 * 
	 * hattr1.setAttributeNode(attr1);
	 * hattr1.setAttributeNode(attr2);
	 * 
	 * //if we call hattr1.toString(); attribute 'name' will not be displayed
	 * </pre>
	 * 
	 * Does not make the Entry hierarchical.
	 * 
	 * @param name
	 *            - the name of the new Property
	 * @return the new Property object
	 * @exception DOMException
	 *                - in case an error occurs while creating the Property
	 * @see #createAttributeNS(String, String)
	 * 
	 * @since 7.0
	 */
	public Property createAttribute(String name) throws DOMException {
		return new Property(name, null);
	}

	/**
	 * This method creates new Property object that represents an element's
	 * attribute in terms of XML concepts. The created Property object has the
	 * following attributes: nodeName: <code>qualifiedName</code>, namespaceURI:
	 * <code>namespaceURI</code> and nodeValue: <code>null</code>.
	 * <p>
	 * This method implements org.w3c.dom.Document's createAttributeNS method
	 * and only creates a new Property but not set it on Attribute object. This
	 * means that until you call {@link Attribute#setAttributeNode(Attr)} with
	 * the newly created Property object it will not be visible trough the
	 * Attribute's methods like getAttribute(String), getAttributes(), etc.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 
	 * var entry = system.newEntry();
	 * var hattr1 = entry.createElement(&quot;mother&quot;);
	 * var hattr2 = entry.createElement(&quot;son&quot;);
	 * entry.appendChild(hattr1);
	 * entry.appendChild(hattr2);
	 * 
	 * var attr = entry.createAttribute(&quot;general&quot;, &quot;name&quot;);
	 * var attr1 = entry.createAttribute(&quot;personal&quot;, &quot;age&quot;);
	 * var attr2 = entry.createAttributeNS(&quot;occupation&quot;, &quot;work&quot;);
	 * 
	 * attr.setValue(&quot;Marina&quot;);
	 * attr1.setValue(&quot;33&quot;);
	 * attr2.setValue(&quot;IBM Corporation&quot;);
	 * 
	 * hattr1.setAttributeNode(attr1);
	 * hattr1.setAttributeNode(attr2);
	 * 
	 * //if we call hattr1.toString() attribute 'name' will not be displayed
	 * 
	 * </pre>
	 * 
	 * Does not make the Entry hierarchical.
	 * 
	 * @param namespaceURI
	 *            - the namespace this Property belongs to
	 * @param qualifiedName
	 *            - the name of the new Property, this could be in the format
	 *            prefix:localName
	 * 
	 * @return the new Property object
	 * 
	 * @exception DOMException
	 *                - in case an error occurs while creating the Property
	 * 
	 * @since 7.0
	 */
	public Property createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
		return new Property(qualifiedName, namespaceURI);
	}

	/**
	 * This method creates new Attribute object that represents an element in
	 * terms of XML concepts. The namespace of this Attribute will be set to
	 * null.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 
	 * var entry = system.newEntry();
	 * entry.setAttribute(&quot;type&quot;, &quot;family&quot;);
	 * 
	 * var hattr1 = entry.createElement(&quot;mother&quot;);
	 * var hattr2 = entry.createElement(&quot;daughter&quot;);
	 * var hattr3 = entry.createElement(&quot;son&quot;);
	 * 
	 * hattr1.appendChild(hattr2);
	 * hattr1.appendChild(hattr3);
	 * 
	 * entry.appendChild(hattr1);
	 * task.dumpEntry(entry);
	 * 
	 * </pre>
	 * 
	 * Does not make the Entry hierarchical.
	 * 
	 * @param tagName
	 *            - the name this Attribute will have, any special characters
	 *            will be escaped prior to creating the Attribute.
	 * @return the new Attribute object
	 * @exception DOMException
	 *                - in case an error occurs while creating the Attribute
	 * 
	 * @since 7.0
	 */
	public Attribute createElement(String tagName) throws DOMException {
		return new Attribute(Attribute.escapeName(tagName));
	}

	/**
	 * This method creates new Attribute object that represents an element in
	 * terms of XML concepts.
	 * <p>
	 * Here is an example of how to create elements with specified namespace,
	 * add them to some entry as children and print the results.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 
	 * var entry = system.newEntry();
	 * 
	 * var hattr1 = entry.createElementNS(&quot;adult&quot;,
	 * &quot;mother&quot;); var hattr2 =
	 * entry.createElementNS(&quot;youth&quot;, &quot;daughter&quot;); var
	 * hattr3 = entry.createElementNS(&quot;youth&quot;, &quot;son&quot;);
	 * 
	 * entry.appendChild(hattr1); entry.appendChild(hattr2);
	 * entry.appendChild(hattr3);
	 * 
	 * var list = entry.getChildNodes();
	 * 
	 * for (i = 0; i &lt; list.getLength(); i++) { task.logmsg(&quot;\tName:
	 * &quot; + list.item(i).getNodeName()); task.logmsg(&quot;\tNS: &quot; +
	 * list.item(i).getNamespaceURI()); }
	 * 
	 * </pre>
	 * 
	 * Does not make the Entry hierarchical.
	 * 
	 * @param namespaceURI
	 *            - the namespace this element will belong to
	 * @param qualifiedName
	 *            - the name of the new Attribute, this could be in the format
	 *            prefix:localName. Any special characters will be escaped prior
	 *            to creating the Attribute.
	 * @return the new Attribute object
	 * @exception DOMException
	 *                - in case an error occurs while creating the Attribute
	 * 
	 * @since 7.0
	 */
	public Attribute createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return new Attribute(Attribute.escapeName(qualifiedName), namespaceURI, false);
	}

	/**
	 * Creates new AttributeCDATA object that represents a CDATASection in terms
	 * of XML concepts. Does not make the Entry hierarchical.
	 * 
	 * @param data
	 *            - the data this AttributeCDATA will contain
	 * @return the new AttributeCDATA object
	 * @exception DOMException
	 *                - in case an error occurs while creating the
	 *                AttributeCDATA
	 * 
	 * @since 7.0
	 */
	public CDATASection createCDATASection(String data) throws DOMException {
		return new AttributeValue(data, AttributeValue.AV_UNDEFINED, false);
	}

	/**
	 * Creates new AttributeText object that represents a Text section in terms
	 * of XML concepts. Does not make the Entry hierarchical.
	 * 
	 * @param data
	 *            - the data this AttributeText will contain
	 * @return the new AttributeText object
	 * 
	 * @since 7.0
	 */
	public Text createTextNode(String data) {
		return new AttributeValue(data, AttributeValue.AV_UNDEFINED, true);
	}

	/**
	 * @return The first (by the order the attributes were entered) Attribute in
	 *         the entry (if any, otherwise null)
	 * 
	 * @since 7.0
	 */
	public Attribute getDocumentElement() {
		return getFirstChild();
	}

	/**
	 * Recursively searches for children with the specified tag name.
	 * 
	 * @param tagname
	 *            - the search criteria
	 * @return NodeList object which holds all the matching children
	 * 
	 * @since 7.0
	 */
	public NodeList getElementsByTagName(String tagname) {

		enableDOM();

		List<NodeImpl> attList = new ArrayList<NodeImpl>();

		for (Attribute child : field.children) {
			if (child.getTagName().equals(tagname) || "*".equals(tagname)) {
				attList.add(child);
			}
			child.internalGetElementsByTagName(tagname, attList);
		}

		return new Attribute.ImmutableNodeList<NodeImpl>(attList, null);
	}

	/**
	 * Recursively searches for children that belong to the specified
	 * namespaceURI and have the specific localName.
	 * 
	 * @param namespaceURI
	 *            - the namespace the child should belong to
	 * @param localName
	 *            - the name the child should have
	 * @return NodeList object which holds all the matching children
	 * 
	 * @since 7.0
	 */
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {

		enableDOM();

		List<NodeImpl> attList = new ArrayList<NodeImpl>();

		for (Attribute child : field.children) {
			if ((child.getLocalName().equals(localName) || "*".equals(localName))
					&& (child.getNamespaceURI().equals(namespaceURI) || "*".equals(namespaceURI))) {
				attList.add(child);
			}
			child.internalGetElementsByTagNameNS(namespaceURI, localName, attList);
		}

		return new Attribute.ImmutableNodeList<NodeImpl>(attList, null);
	}

	/**
	 * Renames the node. If the passed Attribute is already added to this entry
	 * a check for another Attribute with the specified qualifiedName will be
	 * made. If such an Attribute is found it will be replace by the renamed
	 * Attribute. Note that the renamed Attribute will be accessible from the
	 * entry by the localName passed as parameter of this method.<br />
	 * 
	 * If a Property is passed then a check for an existing Property is done. If
	 * one is found then it is replaced otherwise the property is just renamed.
	 * The namespaceURI parameter is not considered when passing a Property. To
	 * change the namespace of this Property you must use the qualifiedName and
	 * set the appropriate prefix. Note that the prefix must be already defined
	 * either implicitly on an Attribute level or explicitly as another
	 * Property.
	 * 
	 * @param n
	 *            - this methods accepts objects of type Attribute or type
	 *            Property
	 * @param namespaceURI
	 *            - the new namespace that should be set
	 * @param qualifiedName
	 *            - the new name this node should have, this name is the one
	 *            that later will be returned by {@link Node#getNodeName()}
	 * @return the same node with changed namespace and qualifiedName
	 * @throws DOMException
	 *             if the n object is not of type Attribute or Property. If the
	 *             n object is a Property but the specified prefix has not been
	 *             declared previously.
	 * 
	 * @since 7.0
	 */
	public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {

		enableDOM();

		if (qualifiedName == null) {
			return null;
		} else {
			qualifiedName = Attribute.escapeName(qualifiedName);
		}

		Node result = null;

		if (n instanceof Attribute) {

			// remember the position of the Attribute
			if (n.getParentNode() == null) {

				field.tempKN.setFullName(qualifiedName);
				// this node is directly attached to an entry. Make sure we
				// preserve the uniqueness of the keys in the data map.

				Attribute namedAttr = internalGetAttribute(field.tempKN);

				if (namedAttr != n) {
					internalRemoveAttribute(field.tempKN, true, true);
				}
				// else {
				// oops seems like the user is trying to rename the
				// same attribute... probably only the namespaceURI needs to be
				// updated.
				// }
			}

			// the rename should update the Entry's data map appropriately.
			((Attribute) n).rename(qualifiedName, namespaceURI);

			result = n;
		} else if (n instanceof Property) {

			Attribute attr = (Attribute) n.getParentNode();

			// don't remove if the nodeName is not being changed.
			if (!n.getNodeName().equals(qualifiedName) && attr != null) {
				// replace an existing property.
				attr.removeAttribute(qualifiedName);
			}

			// renaming it will not require any additional steps
			((Property) n).rename(qualifiedName, namespaceURI);

			result = n;
			// Everything went fine just return the same node
		} else {
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE",
					new String[] { "renameNode", "Attribute or Property", "n" });
		}

		return result;
	}

	/**
	 * This will add the provided Attribute in the entry using the localName of
	 * that Attribute for a key name. If that key name already exists in the map
	 * then an exception will be thrown. <br>
	 * 
	 * Use the {@link #setAttribute(String, Object)} method instead if you want
	 * to force a replacement of the existing Attribute.
	 * 
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 
	 * var entry = system.newEntry();
	 * entry.setAttribute(&quot;type&quot;, &quot;family&quot;);
	 * 
	 * var hattr1 = entry.createElement(&quot;mother&quot;);
	 * var hattr2 = entry.createElement(&quot;daughter&quot;);
	 * var hattr3 = entry.createElement(&quot;son&quot;);
	 * 
	 * hattr1.appendChild(hattr1);
	 * hattr1.appendChild(hattr2);
	 * hattr1.appendChild(hattr3);
	 * 
	 * entry.appendChild(hattr1);
	 * 
	 * task.dumpEntry(entry);
	 * 
	 * </pre>
	 * 
	 * @param newChild
	 *            - Object that is an instance of the Attribute class. This
	 *            object will be added in the Entry if its local name does not
	 *            already exist in the key names set.
	 * 
	 * @return the appended Attribute
	 * 
	 * @exception DOMException
	 *                - in case the newChild parameter is not an instance of the
	 *                Attribute class or if an Attribute is already mapped with
	 *                that key name.
	 * 
	 * @since 7.0
	 */
	public Attribute appendChild(Node newChild) throws DOMException {
		enableDOM();
		if (!(newChild instanceof Attribute)) {
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "appendChild", "Attribute", "newChild" });
		}

		field.tempKN.setFullName(((Attribute) newChild).getName());
		if (field.hData.containsKey(field.tempKN)) {
			throw new DOMException("ENTRY.NAME.ALREADY.BOUND", newChild.getNodeName());
		}

		return insertBefore(newChild, null);
	}

	/**
	 * Returns list of all children of the specified Node;
	 * <p>
	 * Here is an example of how to print entry's all children name and NS.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 
	 * var list = entry.getChildNodes();
	 * 
	 * for (i = 0; i &lt; list.getLength(); i++) {
	 * 	task.logmsg(&quot;\tName: &quot; + list.item(i).getNodeName());
	 * 	task.logmsg(&quot;\tNS: &quot; + list.item(i).getNamespaceURI());
	 * }
	 * 
	 * task.dumpEntry(entry);
	 * 
	 * </pre>
	 * 
	 * @return an immutable NodeList object. Adding an element to the list will
	 *         not make the element a child of the current Document.
	 * 
	 * @since 7.0
	 */
	public NodeList getChildNodes() {
		// wrap the children list to protect it from modification and to
		// optimize access to children length and elements.
		enableDOM();
		if (field.hiddenChildren == null) {
			field.hiddenChildren = new Attribute.ImmutableNodeList<Attribute>(field.children, null);
		}

		return field.hiddenChildren;
	}

	/**
	 * Retrieves the first child, that is, the first Attribute object inserted
	 * in the Entry
	 * 
	 * @return the first child.
	 * 
	 * @since 7.0
	 */
	public Attribute getFirstChild() {
		if (hasChildNodes()) {
			return field.children.get(0);
		}
		return null;
	}

	/**
	 * Retrieves the last child, that is, the last Attribute object inserted in
	 * the Entry
	 * 
	 * @return the last child
	 * 
	 * @since 7.0
	 */
	public Attribute getLastChild() {
		if (hasChildNodes()) {
			return field.children.get(field.children.size() - 1);
		}
		return null;
	}

	/**
	 * @return true if the Entry has Attribute children, false otherwise
	 * 
	 * @since 7.0
	 */
	public boolean hasChildNodes() {
		enableDOM();
		return field.children.size() > 0;
	}

	/**
	 * If the <code>refChild</code> is not presented in the entry structure or
	 * is null then the <code>newChild</code> will be appended to the end. This
	 * method will replace any existing Attribute that are already mapped under
	 * a name which is equal to <code>newChild</code>'s localName.
	 * 
	 * @param newChild
	 *            the Attribute that is going to be inserted
	 * @param refChild
	 *            the Attribute which position will shift down
	 * @return the Attribute which was just added before the
	 *         <code>refChild</code>
	 * @exception DOMException
	 *                if the <code>newChild</code> and the <code>refChild</code>
	 *                are not Attribute instances
	 * 
	 * @since 7.0
	 */
	public Attribute insertBefore(Node newChild, Node refChild) throws DOMException {
		enableDOM();

		if (!(newChild instanceof Attribute))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "insertBefore", "Attribute", "newChild" });

		if (refChild != null && !(refChild instanceof Attribute))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "insertBefore", "Attribute", "refChild" });

		if (refChild != null && newChild.getNodeName().equals(refChild.getNodeName())) {
			throw new DOMException("ENTRY.NAME.ALREADY.BOUND", newChild.getNodeName());
		} else {
			setAttribute(((Attribute) newChild).getName(), newChild);

			if (refChild != null) {
				// the newChild is on the last position, so just take it out
				field.children.remove(newChild);
				// and insert it where it should be
				int oldNodePos = field.children.indexOf(refChild);
				if (oldNodePos > -1) {
					field.children.add(oldNodePos, (Attribute) newChild);
				} else {
					field.children.add((Attribute) newChild);
				}
			}
		}

		return (Attribute) newChild;
	}

	/**
	 * This method will remove the provided Attribute from the Entry.<br>
	 * 
	 * @see #removeAttribute(boolean, String)
	 * 
	 * @param oldChild
	 *            - the Attribute that should be removed from the entry object
	 * @return - the same Attribute passed as parameter if it was found in the
	 *         Entry or null if that Attribute could not be found.
	 * @exception DOMException
	 *                if the oldChild is not an instance of Attribute
	 * 
	 * @since 7.0
	 */
	public Attribute removeChild(Node oldChild) throws DOMException {
		enableDOM();

		if (!(oldChild instanceof Attribute))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "removeChild", "Attribute", "oldChild" });

		if (((Attribute) oldChild).getOwnerDocument() == this && ((Attribute) oldChild).parent == null) {

			boolean owned = field.children.contains(oldChild);

			if (owned) {
				removeAttribute(((Attribute) oldChild).getName());
				return (Attribute) oldChild;
			}
		}

		return null;
	}

	/**
	 * This method will search the entry structure for the oldChild and will
	 * replace it with the newChild.
	 * 
	 * @param newChild
	 *            the Attribute that is going to be added
	 * @param oldChild
	 *            the Attribute which will be removed
	 * 
	 * @return the oldChild that was successfully removed or null if that child
	 *         was not found.
	 * 
	 * @exception DOMException
	 *                if both of the parameters are not instances of the
	 *                Attribute class
	 * 
	 * @since 7.0
	 */
	public Attribute replaceChild(Node newChild, Node oldChild) throws DOMException {

		enableDOM();

		if (!(newChild instanceof Attribute))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "replaceChild", "Attribute", "newChild" });

		if (!(oldChild instanceof Attribute))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "replaceChild", "Attribute", "oldChild" });

		if (field.children.contains(oldChild)) {
			insertBefore(newChild, oldChild);
			return removeChild(oldChild);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEqualNode(Node other) {
		if (!(other instanceof Entry))
			return false;

		Entry entry = (Entry) other;

		if (entry.operation != operation)
			return false;

		if (entry.getProperties().size() != getProperties().size())
			return false;

		for (Map.Entry<String, Object> prop : entry.properties.entrySet()) {
			Object value1 = prop.getValue();
			Object value2 = properties.get(prop.getKey());

			if (value1 != value2 || (value1 != null && !value1.equals(value2))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Clones this entry object
	 * 
	 * @param deep
	 *            if true a complete clone of the tree will be created (the
	 *            values are not cloned!). If false is specified the Attributes
	 *            of this entry are not cloned.
	 * @return the clone object.
	 */
	public Entry cloneNode(boolean deep) {
		if (deep) {
			return clone();
		}

		Entry clone = new Entry();
		clone.operation = this.operation;

		fillInClone(clone, deep);
		return clone;
	}

	/**
	 * This is a class which optimizes the Entry creation, this is done by
	 * telling the JVM to initialize only one field to null instead all the
	 * fields this class holds. This might have some performance optimization
	 * for the serialization of an Entry object but that is not 100% confirmed.
	 */
	final static class FieldsOptimizer implements Serializable {

		private static final long serialVersionUID = -8040153657067486846L;

		// (*) Start of 70 fields...

		// (*)(*) fields that are serialized manually...

		/**
		 * This is the map holding a reference to each Attribute (Node) in the
		 * hierarchical tree. When the Entry is flat this map is
		 * <code>null</code>. This map is automatically created when the
		 * {@link #enableDOM()} is called.
		 */
		Map<KeyName, Attribute> hData;

		/**
		 * Keeps the order of Attributes in the Entry. Make sure the access to
		 * this object is guarded by {@link #isDOMEnabled()} or
		 * {@link #enableDOM()}, otherwise a NPE will be thrown.
		 */
		private List<Attribute> children;

		// (*)(*) End of manually serialized fields...

		// (*)(*) fields that are not serialized at all...
		/**
		 * The {@link XPath} object used to seek throughout the XML Document.
		 * This object is initialized on demand.
		 */
		private transient XPath xPathObj;

		// (*)(*)(*) optimization fields...
		/**
		 * This field holds the anonymous object created to protect the children
		 * list. This field is assigned only once, on demand.
		 */
		private transient NodeList hiddenChildren;

		/**
		 * This field holds a list of all the names to the leafs of this tree.
		 * This field is cleared when a Node in the tree is added or removed.
		 */
		private transient Collection<String> cachedNames;
		private transient boolean isNamesListValid;

		/**
		 * we don't need to create a new List every time we have to hold the
		 * sub-tree list of nodes.
		 */
		private transient List<KeyName> toRemove;

		/**
		 * most of the time we only need a single Info holder so just cache that
		 */
		private transient KeyName tempKN;

		// (*)(*)(*) End of optimization fields...

		// (*)(*) End of non-serialized fields...

		// (*) End of TDI 70 fields...

		public FieldsOptimizer() {
			this.tempKN = new KeyName();
			this.hData = new HashMap<KeyName, Attribute>();
			this.children = new ArrayList<Attribute>();
		}

		public FieldsOptimizer(int initialSize) {
			this.tempKN = new KeyName();
			this.hData = new HashMap<KeyName, Attribute>(initialSize + 10);
			this.children = new ArrayList<Attribute>(initialSize);
		}
	}
}
