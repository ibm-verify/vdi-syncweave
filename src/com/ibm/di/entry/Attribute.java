/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.TypeInfo;

import com.ibm.di.exceptions.DOMException;

/**
 * The Attribute class is used in conjunction with the Entry object to store
 * information about an attribute. The attribute has a name and a list of zero
 * or more values. Each value can be any type of Java object so you can add
 * values of any kind to the attribute.
 * <p>
 * This class has various methods which manipulates wrapped or unwrapped values.
 * In this context 'unwrap' means to get an Object value at specified position,
 * cast it to AttributeValue object, and call getValue() method on it. 'Wrapped'
 * value simply means that the object is used as it is - an AttributeValue
 * object.
 * <p>
 * Since IBM Tivoli Directory Integrator 7.0 an Attribute object could be
 * treated as an array of objects (Object[]), the following script illustrates
 * this:
 * 
 * <pre>
 * // let's say that the work entry have an Attribute called &quot;demo&quot; defined like this:
 * 
 * // create the Attribute;
 * work.demo = new com.ibm.di.entry.Attribute();
 * 
 * // add values to the attribute;  
 * work.demo[0] = &quot;val1&quot;;
 * work.demo[1] = new java.lang.Integer(&quot;5&quot;);
 * work.demo[2] = &quot;thirdValue&quot;;  
 *  
 * // From a performance point of view it is better to get a reference to the Attribute if we are going to use that object multiple times.
 * // This will not make the Script Engine look up the &quot;demo&quot; attribute each time from the work entry.
 * // The above lines &quot;work.demo[x]&quot; are not a good practice, this one is better:
 * var attr = work.demo;
 * 
 * // now the &quot;demo&quot; attribute have 3 values and we need to use each one of them. We could do that by using the [] notation just like we would do with an array.
 * var val1 = attr[0];
 * var val2 = attr[1];
 * var val3 = attr[2];
 * 
 * // or we could cycle through each of the Attribute's values like this:
 * 
 * for (val in attr.getValues())
 * 		main.logmsg(val);
 * 
 * // The result of this is that each value is being printed in the log. 
 * // something like this: 
 * // val1
 * // 5.0
 * // thirdValue
 * 
 * // Note that we use the getValues() method.
 * 
 * // If we have done this:
 * 
 * for (val in attr)
 * 		main.logmsg(val);
 * 
 * // Then the val variable would have a reference to the &quot;demo&quot; Attribute,
 * // thus the log would contain a single line that is the string representation of the &quot;demo&quot; Attribute.
 * // something like this:
 * // demo:val1|5.0|thirdValue
 * 
 * // This is because the toString() method of that Attribute is used implicitly.
 * </pre>
 * 
 */
public class Attribute extends NodeImpl implements Cloneable, Element {

	// (*) Static constants...
	/**
	 * CopyRight header.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 6675881744901860329L;

	/**
	 * Replace value
	 */
	public final static char ATTRIBUTE_REPLACE = 'r';

	/**
	 * Add value
	 */
	public final static char ATTRIBUTE_ADD = 'a';

	/**
	 * Delete value
	 */
	public final static char ATTRIBUTE_DELETE = 'd';

	/**
	 * Unchanged
	 */
	public final static char ATTRIBUTE_UNCHANGED = 'u';

	/**
	 * Values modified
	 */
	public final static char ATTRIBUTE_MOD = 'm';

	/**
	 * Array holding the string representation of the character fields: <br>
	 * [0] = {@link Attribute#ATTRIBUTE_REPLACE}<br>
	 * [1] = {@link Attribute#ATTRIBUTE_ADD}<br>
	 * [2] = {@link Attribute#ATTRIBUTE_DELETE}<br>
	 * [3] = {@link Attribute#ATTRIBUTE_UNCHANGED}<br>
	 * [4] = {@link Attribute#ATTRIBUTE_MOD}<br>
	 */
	public final static String[] OPER = { "replace", "add", "delete", "unchanged", "modify" };

	private static final String DEFAULT_NAME = "";
	// (*) End of Static constants...

	// (*) Start of TDI 611 fields
	/**
	 * This is the fully qualified name of the node, e.g. "<prefix>:<name>".
	 * This variable contains the escaped string before it has being normalized.
	 */
	private String name;

	private List<Object> values = new Vector<Object>();

	private char operation = ATTRIBUTE_REPLACE;

	private boolean protect;

	// (*) End of TDI 611 fields...

	/**
	 * If this field is different than null then this would mean that the
	 * Attribute is domEnabled.
	 */
	transient FieldsOptimizer field;

	/**
	 * this field will be set by the Entry when all of its first level children
	 * are deserialized.
	 */
	transient Entry doc;

	/**
	 * Initialize this attribute with no name, no values and the operation code
	 * set to ATTRIBUTE_REPLACE.
	 */
	public Attribute() {
		this(DEFAULT_NAME);
	}

	/**
	 * Initializes this attribute with no values and the operation code set to
	 * ATTRIBUTE_REPLACE, the name is set to <i>name</i>.
	 * 
	 * @param name
	 *            The attribute name
	 */
	public Attribute(String name) {
		rename(name, null);
	}

	/**
	 * Initializes this attribute with operation code set to ATTRIBUTE_REPLACE,
	 * the name is set to <i>name</i> and the value <i>value</i> is added to the
	 * list of values.
	 * 
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The attribute value
	 */
	public Attribute(String name, Object value) {
		rename(name, null);

		if (value instanceof AttributeValue) {
			value = getValidNode((Node) value);
		}

		values.add(value);
	}

	/**
	 * Initializes this attribute with operation code set to ATTRIBUTE_REPLACE,
	 * the name is set to the provided name and is marked to belong to the
	 * provided namespace URI.
	 * 
	 * @param qualifiedName
	 *            this is the raw name (escaped appropriately) to set to the
	 *            attribute the.
	 * @param namespaceURI
	 *            the namespace this Attribute belongs to
	 * @param protect
	 *            marks the Attribute as protected, by default is false
	 * @since 7.0
	 */
	public Attribute(String qualifiedName, String namespaceURI, boolean protect) {
		enableDOM();
		rename(qualifiedName, namespaceURI);

		this.protect = protect;
	}

	/**
	 * Returns a clone of this object. The cloning is shallow and does not clone
	 * attribute values. If for some reason the cloning does not succeed a
	 * constructor of the Attribute class will be explicitly called. Each
	 * descendant should check for this.<br />
	 * <br />
	 * 
	 * The child nodes of the receiver object are cloned completely. The clone
	 * process is always deep. <br />
	 * <br />
	 * 
	 * The returned clone has no parent and no document. They will be set when
	 * the object is set to an Entry or appended as a child of another
	 * Attribute.
	 * 
	 * @see #cloneNode(boolean)
	 * 
	 * @return the cloned attribute if the cloning process succeeds or a new
	 *         object - copy of this one.
	 */
	public Attribute clone() {
		try {
			// if this succeed then each member variable of the clone will hold
			// the same value/reference as the original object.
			Attribute clone = (Attribute) super.clone();
			fillInClone(clone, true);
			return clone;
		} catch (CloneNotSupportedException e) {
			// should never happen...
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method completes the cloning procedure by setting the required
	 * values to the new clone Attribute object.
	 * 
	 * @param clone
	 *            the Attribute to operate on.
	 * @param deep
	 *            whether the values of this Attribute will be cloned as well.
	 * @since 7.0
	 */
	private void fillInClone(Attribute clone, boolean deep) {
		if (deep) {
			clone.values = new Vector<Object>(this.values.size());
		} else {
			clone.values = new Vector<Object>(this.values);
		}

		if (isDOMEnabled()) {
			clone.field = new FieldsOptimizer(this.values.size());
			clone.field.localName = this.field.localName;
			clone.field.qName = this.field.qName;
			clone.field.prefix = this.field.prefix;
			clone.field.namespaceURI = this.field.namespaceURI;
			clone.field.valuesCount = this.field.valuesCount;

			if (field.propertyMap != null) {
				clone.field.propertyMap = field.propertyMap.cloneMap(clone);
			}
		}

		clone.doc = null;
		clone.parent = null;

		if (deep) {
			for (Object child : values) {
				if (child instanceof NodeImpl) {
					child = clone.getValidNode((Node) child);
				}
				clone.values.add(child);
			}
		}
	}

	/**
	 * Removes all values from this attribute and sets the operation to
	 * ATTRIBUTE_REPLACE.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector.getNextEntry();
	 * var attrlist = entry.getAttributeNames();
	 * 
	 * for (i = 0; i &lt; attrlist.length; i++) {
	 * 	var attr = entry.getAttribute(attrlist[i]);
	 * 	if (system.startsWithIC(attrlist[i], &quot;del&quot;)) {
	 * 		attr.clear();
	 * 	}
	 * }
	 * // print the results where all attributes with names starting 
	 * // with 'del' are cleared   
	 * task.dumpEntry(entry);
	 * </pre>
	 * 
	 * The Properties of the Attribute are not changed.
	 */
	public void clear() {

		if (isDOMEnabled()) {
			for (int i = 0; i < values.size();) {
				if (notElementChildOfThisNode(values.get(i))) {
					internalRemoveChild(i);
				} else {
					++i;
				}
			}
		} else {
			// in this case everything in the values list is a value, cannot be
			// a child.
			values.clear();
		}

		operation = ATTRIBUTE_REPLACE;
	}

	/**
	 * Checks if a value is contained in this Attribute.
	 * 
	 * @param value
	 *            The value to check for
	 * @return true if this Attribute contains the value, false if not
	 * @see #contains
	 */
	public boolean hasValue(Object value) {
		return contains(value);
	}

	/**
	 * Checks if a string value is contained in this attribute. The method
	 * converts values to their string representation before doing a
	 * case-insensitive comparison.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * if (conn.hasValueIC(&quot;this value&quot;)) {
	 * 	task.logmsg(&quot;It is there&quot;);
	 * }
	 * </pre>
	 * 
	 * @param value
	 *            The string value to check for
	 * @return true if attribute contains the value, false if not
	 */
	public boolean hasValueIC(String value) {
		if (value == null)
			return false;

		if (isDOMEnabled()) {
			Object val = null;
			for (int i = 0; i < values.size(); i++) {
				val = values.get(i);
				if (notElementChildOfThisNode(val)) {

					if (val instanceof AttributeValue) {
						val = ((AttributeValue) val).getValue();
					}

					if (val != null) {
						String str = val.toString();
						if (value.equalsIgnoreCase(str)) {
							return true;
						}
					}
				}
			}
		} else {
			for (int i = 0; i < values.size(); i++) {
				String val = getValue(i).toString();
				if (value.equalsIgnoreCase(val))
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a value is contained in this Attribute.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector().getNextEntry();
	 * var attrlist = entry.getAttributeNames();
	 * 
	 * for (i = 0; i &lt; attrlist.length; i++) {
	 * 	var attr = entry.getAttribute(attrlist[i]);
	 * 	if (attrlist[i].equals(&quot;name&quot;) &amp;&amp; attr.contains(&quot;John&quot;) &amp;&amp; attr.contains(&quot;Smith&quot;)) {
	 * 		task.logmsg(&quot;Another John Smith coming...&quot;);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param value
	 *            The value to check for
	 * @return true if this Attribute contains the value, false if not
	 */
	public boolean contains(Object value) {
		if (value instanceof AttributeValue) {
			value = ((AttributeValue) value).getValue();
		}

		if (isDOMEnabled()) {
			Object val = null;
			for (int i = 0; i < values.size(); i++) {
				val = values.get(i);
				if (notElementChildOfThisNode(val)) {
					if (val instanceof AttributeValue) {
						val = ((AttributeValue) val).getValue();
					}
					if (val != null && val.equals(value)) {
						return true;
					}
				}
			}
		} else {
			// We have to iterate ourselves since we need to unwrap
			// AttributeValue objects.
			for (int i = 0; i < values.size(); i++) {
				Object val = values.get(i);
				if (val instanceof AttributeValue)
					val = ((AttributeValue) val).getValue();
				if (val.equals(value))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns the first value, if any, as a String object.
	 * 
	 * @return The object as a String or null if attribute has no values
	 */
	public String getValue() {
		Object val = getValueAt(0);
		if (val == null)
			return null;

		return val.toString();
	}

	/**
	 * Returns the value at the position given by <i>index</i>. The value is
	 * unwrapped if it is an AttributeValue object.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *    var v = conn.getAttribute(&quot;attrname&quot;).getValue ( 0 );
	 *    task.logmsg( &quot;Java class of v is: &quot; + v.getClass().getName() );
	 *    task.logmsg( &quot;String representation: &quot; + v.toString() );
	 *    if ( v.getClass().getName() == &quot;java.util.Date&quot; ) {
	 *      task.logmsg(&quot;Date object: &quot; + v.getMonth();
	 *    }
	 * </pre>
	 * 
	 * @param index
	 *            The position
	 * @return The Object or null if index is out of range
	 * @see #size()
	 */
	public Object getValue(int index) {
		Object val = getValueAt(index);

		if (val instanceof AttributeValue) {
			val = ((AttributeValue) val).getValue();
		}

		return val;
	}

	/**
	 * Returns the object at the given index. This method unwraps the
	 * AttributeValue, if its operation code is
	 * {@link AttributeValue#AV_REPLACE}. In this case the actual value of the
	 * attribute is returned. If the AttributeValue's operation code is
	 * different from the above, the whole AttributeValue object is returned
	 * instead of the actual value. <b>Note:</b> if you want to access each
	 * value as an AttributeValue object (disregarding the operation code), use
	 * the DOM API.<br/>
	 * For example: <br/>
	 * Lets say you want to access the wrapped value of an attribute (an
	 * AttributeValue object). The name of the attribute is myAttr and the index
	 * of the value is 2. Use this script:
	 * 
	 * <pre>
	 * task.logmsg(&quot;Attribute: &quot; + myAttr);	//attribute
	 * var index = 2;	//value's index
	 * var nodeList = work.getAttribute(attr).getChildNodes();
	 * var internalIndex = 0;	
	 * var value = null;
	 * for(node in nodeList) {
	 * // The nodeList may contain Attribute objects. In order to get the correct 
	 * // index of the value we need to increase the internalIndex only when an 
	 * // AttributeValue is found. 
	 * 	if(!(node instanceof com.ibm.di.entry.Attribute) 
	 * 			&amp;&amp; internalIndex++ == index) {
	 * 		value = node;
	 * 		break;
	 * }
	 * task.logmsg(&quot;Value(&quot; + index + &quot;)= &quot; + value);
	 * 
	 * </pre>
	 * 
	 * @param index
	 *            the position
	 * @return The Object at the position, or null if index too high
	 */
	public Object getValueAV(int index) {
		Object value = getValueAt(index);
		if (value instanceof AttributeValue) {
			AttributeValue aValue = (AttributeValue) value;
			if (aValue.getOper() == AttributeValue.AV_REPLACE) {
				value = aValue.getValue();
			}
		}

		return value;
	}

	private Object getValueAt(int index) {
		Object val = null;

		if (index < values.size()) {
			if (isDOMEnabled()) {
				int internalIdx = 0;
				for (int i = 0; i < values.size(); i++) {
					val = values.get(i);

					// Attributes are not indexed
					if ((notElementChildOfThisNode(val)) && internalIdx++ == index) {
						break;
					} else {
						val = null;
					}
				}
			} else {
				val = values.get(index);
			}
		}

		return val;
	}

	/**
	 * Returns this attribute's values as an array of objects. This method
	 * unwraps AttributeValue objects. The array may be empty if attribute has
	 * no values (e.g. arr.length == 0).
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // get values of attribute of conn object;
	 * var vallist = conn.getAttribute(&quot;name&quot;).getValues();
	 * 
	 * // cycle through each of the Attribute's values and print them
	 * for (val in vallist)
	 * 	main.logmsg(val);
	 * 
	 * // or you could print them like this
	 * for (i = 0; i &lt; vallist.length; i++) {
	 * 	main.logmsg(vallist[i]);
	 * }
	 * </pre>
	 * 
	 * @return An array of objects.
	 */
	public Object[] getValues() {
		Object[] ret = new Object[size()];
		int retPos = 0;

		if (isDOMEnabled()) {
			Object val = null;
			for (int i = 0; i < values.size(); i++) {
				val = values.get(i);
				if (notElementChildOfThisNode(val)) {
					if (val instanceof AttributeValue) {
						ret[retPos++] = ((AttributeValue) val).getValue();
					} else {
						ret[retPos++] = val;
					}
				}
			}
		} else {
			for (Object val : values) {
				if (val instanceof AttributeValue) {
					ret[retPos++] = ((AttributeValue) val).getValue();
				} else {
					ret[retPos++] = val;
				}
			}
		}

		return ret;
	}

	/**
	 * Returns this Attribute's values as a Vector. This method unwraps
	 * AttributeValue objects.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // get values of attribute of conn object;
	 * var vallist = conn.getAttribute(&quot;name&quot;).getValuesVector();
	 * 
	 * // cycle through each of the Attribute's values and print them
	 * for (i = 0; i &lt; vallist.size(); i++) {
	 * 	main.logmsg(vallist.get(i));
	 * }
	 * </pre>
	 * 
	 * @see #getValues
	 * @return An array of objects.
	 */
	@SuppressWarnings("unchecked")
	public Vector getValuesVector() {
		Vector<Object> ret = new Vector<Object>(size());

		if (isDOMEnabled()) {
			Object val = null;
			for (int i = 0; i < values.size(); i++) {
				val = values.get(i);
				if (notElementChildOfThisNode(val)) {
					if (val instanceof AttributeValue) {
						ret.add(((AttributeValue) val).getValue());
					} else {
						ret.add(val);
					}
				}
			}
		} else {
			for (Object val : values) {
				if (val instanceof AttributeValue) {
					ret.add(((AttributeValue) val).getValue());
				} else {
					ret.add(val);
				}
			}
		}
		return ret;
	}

	/**
	 * Returns this Attribute's values as an array of objects. This method
	 * unwraps AttributeValue objects only if their operation code is
	 * {@link AttributeValue#AV_REPLACE}. Otherwise no unwrapping is performed
	 * and the AttributeValue objects are returned. The array may be empty if
	 * the attribute has no values (e.g. arr.length == 0).<b>Note: </b>if you
	 * need an array where all the values are wrapped (as AttributeValue
	 * objects) you must use the DOM API instead.<br/>
	 * Example:<br/>
	 * This script shows how to retrieve all the values of the attribute(myAttr)
	 * without unwrapping any of them.
	 * 
	 * <pre>
	 * task.logmsg(&quot;Attribute: &quot; + myAttr);	//attribute
	 * var nodeList = work.getAttribute(attr).getChildNodes();
	 * var array = new Array();
	 * var i = 0;
	 * for(node in nodeList) {
	 * 	if(!(node instanceof com.ibm.di.entry.Attribute))
	 * 		array[i++] = node;
	 * }
	 * //the 'array' holds AttributeValue objects only
	 * </pre>
	 * 
	 * @return An array of objects.
	 * @see #getValues()
	 */
	public Object[] getValuesAV() {
		Object[] ret = new Object[size()];
		int retPos = 0;
		if (isDOMEnabled()) {
			Object val = null;
			for (int i = 0; i < values.size(); i++) {

				val = values.get(i);
				if (notElementChildOfThisNode(val)) {
					if (val instanceof AttributeValue && ((AttributeValue) val).getOper() == AttributeValue.AV_REPLACE) {
						ret[retPos++] = ((AttributeValue) val).getValue();
					} else {
						ret[retPos++] = val;
					}
				}
			}
		} else {
			for (Object val : values) {
				if (val instanceof AttributeValue && ((AttributeValue) val).getOper() == AttributeValue.AV_REPLACE) {
					ret[retPos++] = ((AttributeValue) val).getValue();
				} else {
					ret[retPos++] = val;
				}
			}
		}

		return ret;
	}

	/**
	 * This method checks whether the nodeObj belongs to the current Document
	 * (entry) and if not it is cloned and connected to the entry. It is not
	 * automatically added to the values list! Will not clone if the node's
	 * document is null - assuming a new node and no cleaning up should be done.
	 * 
	 * If the nodeObj is an instance of NodeImpl then these rules are followed:<br />
	 * <br />
	 * <table border="1">
	 * <tr>
	 * <th>nodeObj.parent</th>
	 * <th>nodeObj.doc</th>
	 * <th>Action</th>
	 * <tr>
	 * <td>null</td>
	 * <td>null</td>
	 * <td>This is a NodeImpl object not attached anywhere. We should not wary
	 * about that and just attach it to the tree.</td>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>&lt;ref&gt;</td>
	 * <td rowspan="3">This node belongs to some other tree. Clone it and attach
	 * the clone to our tree. Note that if the doc references are identical we
	 * will skip clone and will think that this is just moving of a node in the
	 * current hierarchy.</td>
	 * </tr>
	 * <tr>
	 * <td>&lt;ref&gt;</td>
	 * <td>&lt;ref&gt;</td>
	 * </tr>
	 * <tr>
	 * <td>&lt;ref&gt;</td>
	 * <td>null</td>
	 * </tr>
	 * </table>
	 * 
	 * @param nodeObj
	 *            the node to check.
	 * @return the nodeObj if it is not a Node at all or it does not need to be
	 *         cloned, otherwise the cloned node.
	 * @throws DOMException
	 *             if the user tries to attach a parent node as a child.
	 */
	private NodeImpl getValidNode(Node nodeObj) {
		if (!(nodeObj instanceof NodeImpl)) {
			return null;
		}

		NodeImpl node = (NodeImpl) nodeObj;
		Document nodeDoc = node.getOwnerDocument();
		// this is the node that is about to be attached to this attribute's
		// tree.
		if (node.parent != null || nodeDoc != null) {

			if (nodeDoc == null && this.getOwnerDocument() == null) {
				// this is very rare case and it happens when we are cloning
				// an Attribute which is not attached to an Entry. In such
				// cases we assume that the user is working with the
				// attribute as a standard object not as part of a tree.
				node = (NodeImpl) node.cloneNode(true);
			} else if (nodeDoc != this.doc) {
				// the node is coming from a different source entry...
				// clone it and add it to the tree.
				node = (NodeImpl) node.cloneNode(true);
			} else if (isDOMEnabled()) {
				// the entry already is the document owning the node. check
				// for cyclic connection.
				checkForEndlessRecursion(node);

				// reposition the node in the tree.
				if (node.parent == null && nodeDoc != null) {
					// it was a top-level node...
					nodeDoc.removeChild(node);
				} else if (node.parent != null) {
					// was not a top-level node in this tree...
					node.parent.internalRemoveChildAndFixEntry(node);
				}
			}
		}

		node.connect(doc, this);

		return node;
	}

	/**
	 * Sets this Attribute's values using the List passed to it.
	 * 
	 * @param values
	 *            The new values list
	 */
	public void setValues(List<? extends Object> values) {
		clear();

		// check the Nodes belonging
		for (Object valObj : values) {
			addValue(valObj);
		}
	}

	/**
	 * Sets the Attribute's values to the array of objects provided by values.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // suppose work entry has attribute 'changenumbers' 
	 * // with int values we want to sort
	 * var attr = work.getAttribute(&quot;changenumbers&quot;);
	 * var vlist = attr.getValues();
	 * 
	 * // sort values of attribute 'changenumbers'
	 * java.util.Arrays.sort(vlist);
	 * 
	 * // update sorted values to entry attribute
	 * attr.setValues(vlist);
	 * </pre>
	 * 
	 * @param values
	 *            The new value array
	 */
	public void setValues(Object[] values) {
		clear();

		// check the Nodes belonging
		for (Object valObj : values) {
			addValue(valObj);
		}
	}

	/**
	 * Sets this Attribute's value at position 0 to the value specified. If the
	 * attribute has no values the value is inserted, otherwise the value at
	 * position 0 is replaced.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var entry = input.getConnector.getNextEntry();
	 * var attrlist = entry.getAttributeNames();
	 * 
	 * for (i = 0; i &lt; attrlist.length; i++) {
	 * 	var attr = entry.getAttribute(attrlist[i]);
	 * 	attr.setValue(&quot;firstValue&quot;)
	 * }
	 * </pre>
	 * 
	 * @param val
	 *            The new value
	 */
	public void setValue(Object val) {
		setValue(0, val);
	}

	/**
	 * Adds a value to the attribute's list of values.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // create the Attribute;
	 * var attr = new com.ibm.di.entry.Attribute();
	 * 
	 * // add values to the attribute;  
	 * attr.addValue(&quot;one&quot;);
	 * attr.addValue(2);
	 * attr.addValue('3');
	 * 
	 * // cycle through each of the Attribute's values and print them
	 * for (val in attr.getValues())
	 * 		main.logmsg(val);
	 * 
	 * // The result is: 
	 * // one
	 * // 2.0
	 * // 3
	 * </pre>
	 * 
	 * @param val
	 *            The new value
	 */
	public void addValue(Object val) {
		if (val instanceof AttributeValue) {
			val = getValidNode((AttributeValue) val);
		}

		if (isDOMEnabled()) {
			++field.valuesCount;
		}

		values.add(val);
	}

	/**
	 * Sets the attribute's value at a specific position to the value specified.
	 * If the position (index) is out of range an exception is thrown.
	 * 
	 * @param index
	 *            The position
	 * @param val
	 *            The value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the index was invalid.
	 */
	public void setValue(int index, Object val) {

		if (val instanceof AttributeValue) {
			val = getValidNode((AttributeValue) val);
		}

		if (isDOMEnabled()) {
			Object temp = null;
			int internalIdx = 0;
			boolean set = false;
			for (int i = 0; i < values.size(); i++) {
				if (notElementChildOfThisNode(values.get(i))) {
					if (internalIdx == index) {
						temp = values.remove(i);
						if (temp instanceof NodeImpl) {
							((NodeImpl) temp).disconnect();
						}

						values.add(i, val);
						set = true;
						break;
					} else {
						internalIdx++;
					}
				}
			}

			if (!set) {
				if (internalIdx == index) {
					values.add(val);
					++field.valuesCount;
				} else {
					throw new IndexOutOfBoundsException();
				}
			}
		} else {
			if (values.size() == index) {
				values.add(val);
			} else {
				values.set(index, val);
			}
		}
	}

	/**
	 * Removes all instances of a value from the attribute's list of values. Any
	 * Node values will be automatically detached from this parent and from the
	 * document they were previously attached to.
	 * 
	 * @param p1
	 *            The value to remove
	 * @return true if this Attribute contained the value, false if no change.
	 */
	public boolean removeValue(Object p1) {
		boolean wasThere = false;

		if (p1 instanceof AttributeValue) {
			p1 = ((AttributeValue) p1).getValue();
		}

		Object val = null;
		if (isDOMEnabled()) {
			for (int i = (values.size() - 1); i >= 0; i--) {
				val = values.get(i);

				if (val instanceof AttributeValue) {
					// will get detached in the next if
					val = ((AttributeValue) val).getValue();
				}

				if (val != null && val.equals(p1) && (notElementChildOfThisNode(val))) {
					internalRemoveChild(i);
					wasThere = true;
				}
			}
		} else {
			for (int i = (values.size() - 1); i >= 0; i--) {
				val = values.get(i);
				if (val instanceof AttributeValue)
					val = ((AttributeValue) val).getValue();
				if (val.equals(p1)) {
					values.remove(i);
					wasThere = true;
				}
			}
		}
		return wasThere;
	}

	/**
	 * Removes a value at a given index. Node values will be automatically
	 * detached from this parent and from the document they were attached to
	 * previously.
	 * 
	 * @param index
	 *            The index of the value to remove
	 * @return The value at the given index, or null if index is out of bounds
	 */
	public Object removeValueAt(int index) {
		Object result = null;

		if (isDOMEnabled()) {
			int internalIdx = 0;
			for (int i = 0; i < values.size(); i++) {
				if (notElementChildOfThisNode(values.get(i))) {
					if (internalIdx == index) {
						result = internalRemoveChild(i);
						if (result instanceof AttributeValue) {
							result = ((AttributeValue) result).getValue();
						}
						break;
					} else {
						internalIdx++;
					}
				}
			}
		} else {
			try {
				result = values.remove(index);
			} catch (ArrayIndexOutOfBoundsException ai) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Returns this Attribute's name without stripping it off the escape
	 * characters the name might has. To get the plain name without the escape
	 * characters use the {@link #getNodeName()} instead.
	 * 
	 * @return the Attribute's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets this Attribute's name.
	 * 
	 * @param name
	 *            The new name of this Attribute
	 */
	public void setName(String name) {
		rename(name, getNamespaceURI());
	}

	/**
	 * Returns the operation type of this Attribute.
	 * 
	 * @return The operation type as a char
	 */
	public char getOper() {
		return operation;
	}

	/**
	 * Returns the operation type of this Attribute as a String. The operation
	 * has only been set to a meaningful value if this Attribute is part of an
	 * Entry that comes from an Iterator with Delta enabled.
	 * 
	 * @return The operation type as a string. Possible values are "replace",
	 *         "add", "delete", "unchanged" or "modify"
	 */
	public String getOperation() {
		switch (operation) {
		case ATTRIBUTE_ADD:
			return OPER[1];
		case ATTRIBUTE_DELETE:
			return OPER[2];
		case ATTRIBUTE_UNCHANGED:
			return OPER[3];
		case ATTRIBUTE_MOD:
			return OPER[4];
		default:
			return OPER[0];
		}
	}

	/**
	 * Sets the operation type of this Attribute.
	 * 
	 * @param operation
	 *            The operation type as a char
	 */
	public void setOper(char operation) {
		this.operation = operation;
	}

	/**
	 * Sets the operation type of this Attribute.
	 * 
	 * @param operation
	 *            The operation type as a string. If this is <code>null</code>
	 *            or empty string the {@link #ATTRIBUTE_REPLACE} operation is
	 *            set.
	 */
	public void setOperation(String operation) {
		if (operation == null || operation.length() == 0) {
			setOper(ATTRIBUTE_REPLACE);
		} else {
			setOper(Character.toLowerCase(operation.charAt(0)));
		}
	}

	/**
	 * Returns the number of values contained in this Attribute.
	 * 
	 * @return The number of values
	 */
	public int size() {
		if (isDOMEnabled()) {
			return field.valuesCount;
		} else {
			return values.size();
		}
	}

	/**
	 * Returns the string representation of this attribute.
	 * 
	 * @return The name and values as a structured String
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder(256);
		internalToString(buf, 1, false);
		return buf.toString();
	}

	/**
	 * Returns the Delta string representation of this Attribute. This string
	 * representation also contains delta information, if present.
	 * 
	 * @return The name and values as a structured string
	 */
	public String toDeltaString() {
		StringBuilder buf = new StringBuilder(256);
		internalToString(buf, 1, true);
		return buf.toString();
	}

	/**
	 * Sets this Attribute's value at position 0 to the value specified. If the
	 * attribute has no values the value is inserted, otherwise the value at
	 * position 0 is replaced.
	 * 
	 * @param p1
	 *            The new value
	 * @param valueOper
	 *            The value operation code
	 * @see #setValue(Object)
	 */
	public void setValue(Object p1, int valueOper) {
		if (p1 instanceof AttributeValue) {
			((AttributeValue) p1).setOper(valueOper);
		} else {
			p1 = new AttributeValue(p1, valueOper);
		}

		setValue(p1);
	}

	/**
	 * Sets the attribute's value at a specific position to the value specified.
	 * If the position is out of range an exception is thrown.
	 * 
	 * @param position
	 *            The position
	 * @param p2
	 *            The value
	 * @param valueOper
	 *            The value operation code
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the position was invalid.
	 */
	public void setValue(int position, Object p2, int valueOper) {
		if (p2 instanceof AttributeValue) {
			((AttributeValue) p2).setOper(valueOper);
		} else {
			p2 = new AttributeValue(p2, valueOper);
		}

		setValue(position, p2);
	}

	/**
	 * Adds a value to this Attribute's list of values.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // suppose work entry has 'lowercasestring' attribute with some string values
	 * var attr = work.newAttribute(&quot;lowercasestring&quot;);
	 * 
	 * // add new values to the attribute
	 * attr.addValue(&quot;Mamut&quot;, com.ibm.di.entry.AttributeValue.AV_ADD);
	 * attr.addValue(&quot;APPC&quot;, com.ibm.di.entry.AttributeValue.AV_ADD);
	 * 
	 * main.logmsg(&quot;Before validation:&quot;);
	 * task.dumpEntry(work);
	 * 
	 * // find all newly added values and make them lowercase 
	 * for (i = 0; i &lt; attr.size(); i++) {
	 * 	if (attr.getValueOper(i) == com.ibm.di.entry.AttributeValue.AV_ADD) {
	 * 		var str = attr.getValue(i).toLowerCase();
	 * 		attr.setValue(i, str);
	 * 	}
	 * }
	 * 
	 * // print result
	 * main.logmsg(&quot;After validation:&quot;);
	 * task.dumpEntry(work);
	 * </pre>
	 * 
	 * @param p1
	 *            The new value
	 * @param valueOper
	 *            The value operation code
	 */
	public void addValue(Object p1, int valueOper) {
		if (p1 instanceof AttributeValue) {
			((AttributeValue) p1).setOper(valueOper);
		} else {
			p1 = new AttributeValue(p1, valueOper);
		}

		addValue(p1);
	}

	/**
	 * Adds the specified object as Attribute's value at the specified position.
	 * The object is inserted before any previous value at the specified
	 * location. If the location is equal to the size of this Vector, the object
	 * is added at the end.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // suppose work entry has attribute 'changenumbers' 
	 * // with int values in ascending order which we want to keep
	 * 
	 * var vlist = attr.getValues();
	 * 
	 * // value we want to insert into 'changenumbers' attribute
	 * var insertval = 5;
	 * 
	 * //insert value keeping ascending order of attribute's values
	 * for (i = 0; i &lt; vlist.length; i++) {
	 * 	main.logmsg(vlist[i]);
	 * 	if (insertval &gt;= vlist[i] &amp;&amp; insertval &lt;= vlist[i + 1]) {
	 * 		main.logmsg(&quot;Found place to insert &quot;);
	 * 		attr.addValue(i + 1, insertval);
	 * 		break;
	 * 	}
	 * }
	 * main.dumpEntry(work);
	 * </pre>
	 * 
	 * @param position
	 *            the index at which to insert the element
	 * @param val
	 *            the object to insert in this Vector
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                when <code>location < 0 || > size()</code>
	 */
	public void addValue(int position, Object val) {
		if (val instanceof AttributeValue) {
			val = getValidNode((Node) val);
		}

		if (isDOMEnabled()) {
			boolean add = false;
			int internalIdx = 0;
			for (int i = 0; i < values.size(); i++) {
				if (notElementChildOfThisNode(values.get(i))) {
					if (internalIdx == position) {
						values.add(i, val);
						add = true;
						break;
					} else {
						internalIdx++;
					}
				}
			}

			if (!add) {
				if (internalIdx == position) {
					values.add(val);
				} else {
					throw new ArrayIndexOutOfBoundsException();
				}
			}

			++field.valuesCount;
		} else {
			values.add(position, val);
		}
	}

	/**
	 * Adds the specified object into this Attribute at the specified location.
	 * The object is inserted before any previous value at the specified
	 * location. If the location is equal to the size of this Vector, the object
	 * is added at the end.
	 * 
	 * @param position
	 *            the index at which to insert the element
	 * @param p1
	 *            the object to insert in this Vector
	 * @param valueOper
	 *            The value operation code
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                when <code>location < 0 || > size()</code>
	 * @see #addValue(Object, int)
	 */
	public void addValue(int position, Object p1, int valueOper) {
		if (p1 instanceof AttributeValue) {
			((AttributeValue) p1).setOper(valueOper);
		} else {
			p1 = new AttributeValue(p1, valueOper);
		}
		addValue(position, p1);
	}

	/**
	 * Sets the value operation code. If the value is a non-AV object, a new
	 * AttributeValue object is created to hold the current value.
	 * 
	 * @param index
	 *            The value index
	 * @param valueOper
	 *            The value operation code
	 */
	public void setValueOper(int index, int valueOper) {
		Object obj = getValueAt(index);

		if (obj instanceof AttributeValue) {
			((AttributeValue) obj).setOper(valueOper);
		} else if (obj != null) {
			setValue(index, new AttributeValue(obj, valueOper));
		}
	}

	/**
	 * Returns the value operation code at a specified index as an integer.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // suppose work entry has 'lowercasestring' attribute with some values
	 * var attr = work.newAttribute(&quot;lowercasestring&quot;);
	 * 
	 * // add new values to the attribute
	 * attr.addValue(&quot;Mamut&quot;, com.ibm.di.entry.AttributeValue.AV_ADD);
	 * attr.addValue(&quot;APPC&quot;, com.ibm.di.entry.AttributeValue.AV_ADD);
	 * 
	 * main.logmsg(&quot;Before validation:&quot;);
	 * task.dumpEntry(work);
	 * 
	 * // find all newly added values and make them lowercase 
	 * for (i = 0; i &lt; attr.size(); i++) {
	 * 	if (attr.getValueOper(i) == com.ibm.di.entry.AttributeValue.AV_ADD) {
	 * 		var str = attr.getValue(i).toLowerCase();
	 * 		attr.setValue(i, str);
	 * 	}
	 * }
	 * 
	 * // print result
	 * main.logmsg(&quot;After validation:&quot;);
	 * task.dumpEntry(work);
	 * </pre>
	 * 
	 * @param index
	 *            The value index
	 * @return The value operation code ( -1 indicates no operation code )
	 */
	public int getValueOper(int index) {
		Object obj = getValueAt(index);

		if (obj instanceof AttributeValue) {
			return ((AttributeValue) obj).getOper();
		} else {
			return AttributeValue.AV_UNDEFINED;
		}
	}

	/**
	 * Sets the value operation code for a specified index. If the value is a
	 * non-AV object, a new AttributeValue object is created to hold the current
	 * value.
	 * 
	 * @param index
	 *            The new value
	 * @param valueOper
	 *            The string-version operation code
	 */
	public void setValueOperation(int index, String valueOper) {
		setValueOper(index, AttributeValue.stringToOper(valueOper));
	}

	/**
	 * Returns the operation code as a string (add, delete, unchanged) for a
	 * given index. If the value at the index is not a delta value (e.g.
	 * AttributeValue object) then a blank string is returned.
	 * 
	 * @param index
	 *            The index
	 */
	public String getValueOperation(int index) {
		Object obj = getValueAt(index);
		if (obj instanceof AttributeValue)
			return ((AttributeValue) obj).getOperation();
		else
			return "";
	}

	/**
	 * Sets the protected value of this Attribute
	 * 
	 * @param protect
	 *            - If true, try to protect the Attribute values by not dumping
	 *            them in log files
	 * @return this Attribute
	 * @deprecated use {@link #setProtected(boolean, boolean)} instead.
	 */
	@Deprecated
	public Attribute setProtected(boolean protect) {
		this.protect = protect;
		return this;
	}

	/**
	 * Marks this Attribute as protected and prevents its String values to be
	 * written when {@link #toString()} or {@link #toDeltaString()} are called.
	 * 
	 * @param protect
	 *            specify the state of the flag.
	 * @param deep
	 *            specify whether the provided state of the flag will be applied
	 *            for all the children in the sub-tree.
	 * @since 7.0
	 */
	public void setProtected(boolean protect, boolean deep) {
		this.protect = protect;

		if (deep) {
			for (int i = 0; i < values.size(); i++) {
				if (elementChildOfThisNode(values.get(i))) {
					((Attribute) values.get(i)).setProtected(protect, true);
				}
			}
		}
	}

	/**
	 * Returns the protected value of this Attribute
	 * 
	 * @return true if the values should not be dumped in log files
	 */
	public boolean getProtected() {
		return protect;
	}

	/**
	 * Add the values in another Attribute to this Attribute. All values are
	 * added, even if they already exist.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * // create one Attribute;
	 * var attr1 = new com.ibm.di.entry.Attribute();
	 * 
	 * // add values to the attribute;  
	 * attr1.addValue(&quot;one&quot;);
	 * attr1.addValue('2');
	 * attr1.addValue(3);
	 * 
	 * var attr2 = work.attr2.addValues(attr1);
	 * //the result is that attr2 now have 3 values: 'one'	'2'	'3.0'
	 * </pre>
	 * 
	 * @param attr
	 *            The Attribute from which values are collected
	 * @return this Attribute
	 */

	public Attribute addValues(Attribute attr) {
		if (attr == null)
			return this;

		Object[] vals = attr.getValuesAV();

		if (isDOMEnabled()) {
			for (int i = 0; i < vals.length; i++) {
				if (notElementChildOfThisNode(vals[i])) {
					addValue(vals[i]);
				}
			}
		} else {
			for (int i = 0; i < vals.length; i++) {
				values.add(vals[i]);
			}
		}

		if (attr.getProtected()) {
			setProtected(true, false);
		}

		return this;
	}

	public boolean isDOMEnabled() {
		if (field != null) {
			return true;
		} else if (getOwnerDocument() != null && this.doc.isDOMEnabled()) {
			field = new FieldsOptimizer(this.values.size());
			return true;
		}

		return false;
	}

	final void enableDOM() {
		if (field == null) {
			field = new FieldsOptimizer(this.values.size());
			if (getOwnerDocument() != null) {
				this.doc.enableDOM();
			}
		}
	}

	/**
	 * Rename the current node
	 * 
	 * @param qname
	 * @param namespaceURI
	 * @return true
	 */
	boolean rename(String name, String namespaceURI) {
		if (name != null) {
			if (!name.equals(this.name)) {

				if (getOwnerDocument() != null) {
					// before renaming make sure the entry index table is
					// cleaned.
					internalFixEntryWhenRemove(this);
				}

				internalSetNewName(name, namespaceURI);

				if (this.doc != null) {
					internalFixEntryWhenInsert(this);
				}
			} else {
				// probably want to change the namespace only...
				setNamespace(namespaceURI);
			}
		} else {
			internalSetNewName(DEFAULT_NAME, namespaceURI);
		}

		return true;
	}

	private void internalSetNewName(String name, String namespaceURI) {
		this.name = name;
		// if we are not domEnabled we don't need the cache fields.
		if (field != null) {
			this.field.qName = null;
			this.field.localName = null;
			this.field.prefix = null;
			setNamespace(namespaceURI);
		}
	}

	/**
	 * @param namespaceURI
	 */
	private void setNamespace(String namespaceURI) {
		if (field != null) {
			this.field.namespaceURI = namespaceURI == null || namespaceURI.trim().length() == 0 ? null : namespaceURI;
		}
	}
	
	/**
	 * Set the namespace URI 
	 * @param namespaceURI
	 */
	public void setNamespaceURI(String namespaceURI) {
		enableDOM();
		setNamespace(namespaceURI);
	}

	void parseName() {
		if (this.name != null) {
			this.field.qName = normalizeName(this.name);

			int colPos = this.field.qName.indexOf(':');

			if (colPos > 0) {
				this.field.prefix = this.field.qName.substring(0, colPos);
				this.field.localName = this.field.qName.substring(colPos + 1);
			} else {
				this.field.prefix = null;
				this.field.localName = this.field.qName;
			}
		}
	}

	/**
	 * Strips down the escape characters from the provided String.
	 * 
	 * @param name
	 *            the name to remove the escape characters from.
	 * @return the escaped String.
	 */
	public static final String normalizeName(String name) {
		String result = name;
		// normalize name
		if (name != null && name.indexOf(Entry.ESCAPE_CHAR) != -1) {
			StringBuilder builder = new StringBuilder(name);

			for (int i = 0; i < builder.length() - 1; i++) {
				if (builder.charAt(i) == Entry.ESCAPE_CHAR) {
					switch (builder.charAt(i + 1)) {
					case Entry.ESCAPE_CHAR:
					case Entry.PATH_SEPARATOR_CHAR:
						builder.deleteCharAt(i);
						break;
					}
					// more escape chars go here...
				}
			}

			result = builder.toString();
		}

		return result;
	}

	/**
	 * Scans for characters that need to be escaped and returns a string with
	 * those characters escaped.
	 * 
	 * @param name
	 *            the name to escape.
	 * @return the escaped string.
	 */
	public static final String escapeName(String name) {
		if (name == null) {
			return null;
		}

		if (name.indexOf(Entry.PATH_SEPARATOR_CHAR) == -1 && name.indexOf(Entry.ESCAPE_CHAR) == -1) {
			return name;
		}

		StringBuilder builder = new StringBuilder();

		char chr = (char) -1;

		for (int i = 0; i < name.length(); ++i) {
			switch ((chr = name.charAt(i))) {
			case Entry.PATH_SEPARATOR_CHAR:
			case Entry.ESCAPE_CHAR:
				builder.append(Entry.ESCAPE_CHAR);
				builder.append(chr);
				break;
			default:
				builder.append(chr);
			}
		}

		return builder.toString();
	}

	/**
	 * Checks whether the provided as parameter Attribute is already in the
	 * hierarchy tree.
	 * 
	 * @param obj
	 *            - expected an object of type Attribute
	 * @throws DOMException
	 *             if the provided object is the same as this object's parent or
	 *             the same as any of the parents of this object.
	 */
	private void checkForEndlessRecursion(Object obj) {
		if (elementChildOfThisNode(obj)) {
			for (Attribute a = this; a != null; a = a.getParentNode()) {
				if (obj == a) {
					throw new DOMException("HATTRIBUTE.INCORRECT.RECURSIVE.USAGE");
				}
			}
		}
	}

	/**
	 * Constructs the string representation of this object. This method is used
	 * by both {@link #toDeltaString()} and {@link #toString()} methods.
	 * 
	 * @param result
	 *            The {@link StringBuilder} object where the method will write
	 *            to
	 * @param indentLevel
	 *            the number of tab chars to place before each new line.
	 * @param showDelta
	 *            specify whether delta information about this Attribute should
	 *            be included in the final string.
	 * @since 7.0
	 */
	final void internalToString(StringBuilder result, int indentLevel, boolean showDelta) {

		if (isDOMEnabled()) {
			Entry.outputName(result, getNodeName());
		} else {
			Entry.outputName(result, getName());
		}

		boolean square = size() == values.size() && !hasAttributes();
		boolean empty = values.size() == 0 && !hasAttributes() && !showDelta;
		boolean singleVal = !empty && square && size() == 1;

		if (square) {
			if (!singleVal || showDelta) {
				result.append('[');
			}
		} else {
			result.append('{');
		}

		if (showDelta) {
			result.append('\n');
			indentOutput(result, indentLevel);
			Entry.outputName(result, "#type");
			Entry.outputValue(result, getOperation(), false);

			result.append('\n');
			indentOutput(result, indentLevel);
			Entry.outputName(result, "#count");
			Entry.outputValue(result, size(), false);
		}

		if (hasAttributes()) {
			for (Property prop : field.propertyMap) {
				result.append('\n');
				indentOutput(result, indentLevel);
				Entry.outputName(result, "@" + prop.getNodeName());
				Entry.outputValue(result, prop.getValue(), getProtected());
			}
		}

		for (Object val : values) {
			if (!singleVal || showDelta) {
				result.append('\n');
				indentOutput(result, indentLevel);
			}

			if (elementChildOfThisNode(val)) {
				((Attribute) val).internalToString(result, indentLevel + 1, showDelta); // do
				// we need delta info for second+ Attributes?
				result.append(',');
			} else {
				if (val instanceof NodeImpl && ((NodeImpl) val).getNodeType() == Node.CDATA_SECTION_NODE) {
					Entry.outputName(result, "#cdata");
					Entry.outputValue(result, ((NodeImpl) val).getNodeValue(), getProtected());
				} else if (val instanceof AttributeValue) {
					if (showDelta) {
						Entry.outputName(result, "#" + ((AttributeValue) val).getOperation());
					}
					Entry.outputValue(result, ((AttributeValue) val).getValue(), getProtected());
				} else if (val != null) {
					if (showDelta) {
						AttributeValue temp = new AttributeValue(val);
						Entry.outputName(result, "#" + temp.getOperation());
					}
					Entry.outputValue(result, val, getProtected());
				}
			}
		}

		if (!empty) {
			result.deleteCharAt(result.length() - 1);
			if (!singleVal || showDelta) {
				result.append('\n');
				indentOutput(result, --indentLevel);
			}
		}

		if (square) {
			if (!singleVal || showDelta) {
				result.append(']');
			}
		} else {
			result.append('}');
		}
	}

	/**
	 * Convenient method that will output a fixed number of tab chars to the
	 * result StringBuilder.
	 * 
	 * @param result
	 *            the place this method will write to
	 * @param indentLevel
	 *            the number of tabs to be output
	 * @since 7.0
	 */
	private void indentOutput(StringBuilder result, int indentLevel) {
		for (int i = 0; i < indentLevel; i++) {
			result.append('\t');
		}
	}

	final void setFullName(String fullName) {
		if (isDOMEnabled()) {
			this.field.fullName = fullName;
		}
	}

	final String getFullName() {
		return isDOMEnabled() ? field.fullName : null;
	}

	/**
	 * Merges the children/properties of another attribute to this attribute. If
	 * a value is not in the current list it is added. Each child Attribute from
	 * the foreign Attribute is accordingly merged with the child attributes of
	 * this Attribute.<br />
	 * <br />
	 * The Properties of the foreign attribute always replace this attribute's
	 * properties.
	 * 
	 * @param a
	 *            the Attribute to merge values from
	 * @param fixEntryNames
	 *            specifies whether the provided subtree should be reflected on
	 *            the entry.
	 * @since 7.0
	 */
	private void mergeHierarchy(Attribute a, String fullName, Set<Entry.KeyName> nameSet) {

		if (a == null) {
			return;
		}

		// holds the name of a child of the attribute we are merging with
		Entry.KeyName childKN = null;
		// holds the name of this attribute
		Entry.KeyName thisKN = null;
		boolean fixEntryIndex = false;

		fixEntryIndex = fullName != null && this.getOwnerDocument() != null;

		if (fixEntryIndex) {
			thisKN = new Entry.KeyName(fullName + Entry.PATH_SEPARATOR_CHAR);
		}

		if (fixEntryIndex && nameSet == null) {
			nameSet = new HashSet<Entry.KeyName>();

			for (Entry.KeyName key : this.doc.field.hData.keySet()) {
				if (key.getLCFullName().startsWith(thisKN.getLCFullName())) {
					nameSet.add(key);
				}
			}
		}

		// keys represent tag names of the elements and the value is the number
		// of the place that element was encountered on.
		Map<String, Integer> posMap = new HashMap<String, Integer>();

		for (Object aVal : a.values) {
			if (isElementNode(aVal)) {
				Attribute attr = (Attribute) aVal;
				String key = attr.getName();

				if (fixEntryIndex) {
					childKN = new Entry.KeyName(thisKN.getFullName() + attr.getName());
				}

				// holds the count of the current (aVal) attribute
				int pos = 0;
				Integer intPos = posMap.get(key);
				if (intPos == null) {
					posMap.put(key, 0);
				} else {
					pos = 1 + intPos;
					posMap.put(key, pos);
				}

				int myPos = 0;
				boolean set = false;
				for (Object thisVal : values) {
					if (isElementNode(thisVal)) {
						Attribute myAttr = (Attribute) thisVal;
						if (myAttr.getName().equals(key)) {
							if (myPos == pos) {
								myAttr.protect = attr.getProtected();

								if (fixEntryIndex) {
									myAttr.mergeHierarchy(attr, childKN.getFullName(), nameSet);
								} else {
									myAttr.mergeHierarchy(attr, null, null);
								}
								set = true;
								break;
							} else {
								myPos++;
							}
						}
					}
				}
				if (!set) {
					// met an Attribute that don't exist in this Attribute.
					Attribute newAttr = (Attribute) getValidNode(attr);
					values.add(newAttr);

					if (fixEntryIndex && nameSet.add(childKN)) {
						newAttr.setFullName(childKN.getFullName());
						this.doc.field.hData.put(childKN, newAttr);
						this.doc.addSubTree(newAttr, childKN.getFullName(), nameSet);
					}
				}
			} else if (!contains(aVal)) {
				addValue(aVal);
			}
		}

		if (a.hasAttributes()) {
			for (Property prop : a.field.propertyMap) {
				if (prop.getNamespaceURI() == null) {
					setAttributeNode((Attr) prop.cloneNode(true));
				} else {
					setAttributeNodeNS((Attr) prop.cloneNode(true));
				}
			}
		}
	}

	/**
	 * Converts an Attribute to a custom DOM implementation. If a Document is
	 * passed then the method will use that document as the container for the
	 * Attribute's data. If no Document is passed then the default Java
	 * implementation will be used. Note: only the children of this Attribute
	 * are considered (those of type {@link AttributeCDATA} and
	 * {@link AttributeText}), the rest are ignored.<br/>
	 * This method is useful when you need to easily convert to a DOM from a
	 * TDI's Entry/Attribute structure. The returned DOM could then be validated
	 * against a XMLSchema.<br>
	 * <br>
	 * <b>Note:</b> This call will convert the flat Attribute to a hierarchical
	 * one!
	 * 
	 * @param doc
	 *            - the document implementation to use
	 * @return the root element of the resultant DOM structure, representing
	 *         this Attribute.
	 * @throws ParserConfigurationException
	 */
	public Element toDOM(Document doc) throws ParserConfigurationException {
		enableDOM();
		if (doc == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			doc = dbf.newDocumentBuilder().newDocument();
		}

		// Create the element
		Element dst = null;
		if (getNamespaceURI() != null) {
			dst = doc.createElementNS(getNamespaceURI(), getNodeName());
		} else {
			dst = doc.createElement(getNodeName());
		}

		if (hasAttributes()) {
			// Copy attributes
			for (Property prop : field.propertyMap) {
				Attr attr = null;
				if (prop.getNamespaceURI() != null) {
					attr = doc.createAttributeNS(prop.getNamespaceURI(), prop.getNodeName());
				} else {
					attr = doc.createAttribute(prop.getNodeName());
				}

				attr.setNodeValue(prop.getValue());
				dst.setAttributeNode(attr);
			}
		}

		// Copy child nodes (elements, text, cdata, ...).
		NodeList children = getChildNodes();
		Node newNode = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			switch (child.getNodeType()) {

			case Node.ELEMENT_NODE: {
				newNode = ((Attribute) child).toDOM(doc);
				break;
			}
			case Node.CDATA_SECTION_NODE: {
				newNode = doc.createCDATASection(child.getTextContent());
				break;
			}
			case Node.TEXT_NODE: {
				newNode = doc.createTextNode(child.getTextContent());
				break;
			}
			default:
				// unknown node - skip this
			}

			if (newNode != null) {
				dst.appendChild(newNode);
				newNode = null;
			}
		}

		return dst;
	}

	private boolean notElementChildOfThisNode(Object node) {
		return !elementChildOfThisNode(node);
	}

	private boolean isElementNode(Object node) {
		return node instanceof Attribute;
	}

	private boolean elementChildOfThisNode(Object node) {
		/*
		 * Note in some rare cases an Attribute object could be added as regular
		 * values. In that cases we should neither connect the nodes as we
		 * usually do neither to disconnect them.
		 */
		return isElementNode(node) && ((Attribute) node).parent == this;
	}

	/**
	 * Merges the children/properties of another attribute to this attribute. If
	 * a value is not in the current list it is added. Each child Attribute from
	 * the foreign Attribute is accordingly merged with the child attributes of
	 * this Attribute.<br />
	 * <br />
	 * The Properties of the foreign attribute always replace this attribute's
	 * properties.
	 * 
	 * @param a
	 *            the Attribute to merge values from
	 * @since 7.0
	 */
	protected void merge(Attribute a) {
		if (isDOMEnabled()) {
			mergeHierarchy(a, getFullName(), null);
		} else {
			for (Object aVal : a.values) {
				if (!contains(aVal)) {
					addValue(aVal);
				}
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		// our "pseudo-constructor"
		// this will only "revive" the following fields:
		// * name
		// * operation
		// * protect
		// * values
		in.defaultReadObject();

		// see if we are hierarchical
		try {
			boolean domEnabled = in.readBoolean();

			if (domEnabled) {
				field = (FieldsOptimizer) in.readObject();

				// even if we are DOMenabled we don't serialize the parent
				// field... make sure it is properly resolved on
				// deserialization.
				for (Object val : values) {
					if (val instanceof NodeImpl) {
						((NodeImpl) val).parent = this;
					}
				}
			}
		} catch (IOException ioe) {
			// ok we are not hierarchical... we should stop right here
			return;
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {

		if (isDOMEnabled()) {
			// for pre-TDI 7.0 releases Attribute's values that were not wrapped
			// in an AttributeValue object were thought of as if been tagged as
			// AV_REPLACED values. So we need to unwrap those before output.
			AttributeValue av = null;
			for (int i = 0; i < values.size(); ++i) {
				if ((values.get(i) instanceof AttributeValue)
						&& (av = (AttributeValue) values.get(i)).getOper() == AttributeValue.AV_REPLACE) {
					values.set(i, av.getValue());
				}
			}
		}

		// this call outputs all the fields TDI 611 Entry recognizes... nothing
		// more. These fields include:
		// * name
		// * operation
		// * protect
		// * values
		out.defaultWriteObject();

		if (field != null) {
			// first we output the domEnabled flag
			out.writeBoolean(true);

			out.writeObject(field);

			// the rest of the fields used in a DOM enabled entry are only
			// cache/optimization objects which are created based on the objects
			// we serialized above. Don't need to serialize them because the
			// deserialization will be much slower than creating them on demand.
		} else {
			// only output the domEnabled flag
			out.writeBoolean(false);
		}
	}

	@Override
	void connect(Entry doc, Attribute parent) {
		if (parent == null && doc == null) {
			throw new IllegalArgumentException();
		}

		if (this.parent != parent) {
			setParent(parent);
		}

		this.doc = doc;

		if (this.doc != null) {
			// inform the new Entry about the change
			this.doc.invalidateNamesList();
		}
	}

	void disconnectSubtree() {
		this.doc = null;
		Object n = null;
		for (int i = 0; i < values.size(); i++) {
			if (elementChildOfThisNode(n = values.get(i))) {
				((Attribute) n).disconnectSubtree();
			}
		}
	}

	@Override
	void disconnect() {

		if (this.getOwnerDocument() != null) {
			// inform the new Entry about the change
			this.doc.invalidateNamesList();
		}

		if (this.doc != null && values.size() > size()) {
			disconnectSubtree();
			this.doc = null;
		}

		if (this.parent != null) {
			setParent(null);
		}
	}

	/**
	 * This is a convenient method that will gather all the {@link Text}
	 * children and will return them as a NodeList object.
	 * 
	 * @return NodeList object that hold all the children of this Attribute that
	 *         are of type Text
	 * @since 7.0
	 */
	public NodeList getTextSections() {
		List<NodeImpl> result = new ArrayList<NodeImpl>();
		Object child = null;
		for (int i = 0; i < values.size(); ++i) {
			child = values.get(i);
			if (child instanceof NodeImpl) {
				if (((NodeImpl) child).getNodeType() == Node.TEXT_NODE) {
					result.add((NodeImpl) child);
				}
			} else {
				// we tread unwrapped children as TEXT nodes!
				AttributeValue av = new AttributeValue(child);
				av.setParent(this);
				values.set(i, av);
				result.add(av);
			}
		}
		return new ImmutableNodeList<NodeImpl>(result, this);
	}

	/**
	 * This is a convenient method that will gather all the {@link CDATASection}
	 * children and will return them as a NodeList object.
	 * 
	 * @return NodeList object that hold all the children of this Attribute that
	 *         are of type CDATASection
	 * @since 7.0
	 */
	public NodeList getCDATASections() {
		List<NodeImpl> result = new ArrayList<NodeImpl>();

		for (Object child : values) {
			if (child instanceof NodeImpl && ((NodeImpl) child).getNodeType() == Node.CDATA_SECTION_NODE) {
				result.add((NodeImpl) child);
			} // else {
			// unwrapped values are treated as TEXT nodes...
			// }
		}
		return new ImmutableNodeList<NodeImpl>(result, this);
	}

	// DOM implementation starts here...

	public Entry getOwnerDocument() {
		// cache the result to avoid going to the Entry twice...
		return doc != null ? doc : (parent != null ? (doc = parent.getOwnerDocument()) : null);
	}

	/**
	 * This method clones the Attribute object receiving the call. Depending on
	 * the passed parameter the cloning will be either deep or not.<br />
	 * <br />
	 * 
	 * If the cloning is deep then the method {@link #clone()} is called. <br />
	 * <br />
	 * 
	 * If the cloning is not deep then a new Attribute object is returned with
	 * no parent and no document set. The object also has no children/values.
	 * The properties however are complete clones of the original objects.
	 * 
	 * @param deep
	 *            specifies whether a deep clone should be done or not.
	 * @return the cloned object.
	 * @since 7.0
	 */
	public Attribute cloneNode(boolean deep) {
		if (deep) {
			return clone();
		}

		Attribute clone = new Attribute();

		// this is usually done by the clone() method but in this case we need
		// to do it manually.
		clone.name = this.name;
		clone.operation = this.operation;
		clone.protect = this.protect;

		fillInClone(clone, false);
		return clone;
	}

	/**
	 * @return the local name of the Attribute/Node
	 * @since 7.0
	 */
	public String getLocalName() {
		enableDOM();
		if (this.field.localName == null) {
			parseName();
		}
		return this.field.localName;
	}

	/**
	 * @return the Node's type.
	 * @see Node#ELEMENT_NODE
	 * @since 7.0
	 */
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	/**
	 * This method will return the result of concatenation of all the
	 * AttributeText nodes this Attribute have. The different values are
	 * separated by space. An empty string is returned if no AttributeText
	 * objects are available.
	 * 
	 * @return the value of the Attribute.
	 * @since 7.0
	 */
	public String getNodeValue() {
		NodeList list = getTextSections();

		if (list.getLength() == 0) {
			return null;
		} else if (list.getLength() == 1) {
			return list.item(0).getNodeValue();
		}

		StringBuilder result = new StringBuilder();
		int i = 0;
		int max = list.getLength() - 1;
		for (; i < max; i++) {
			if (list.item(i).getNodeValue() != null) {
				result.append(list.item(i).getNodeValue());
				result.append(" ");
			}
		}

		if (list.item(i).getNodeValue() != null) {
			result.append(list.item(i).getNodeValue());
		}

		return result.toString();
	}

	/**
	 * This method will remove all the child nodes of type Text this Attribute
	 * might have. A new child node will be created of type Text and added to
	 * the children list.
	 * 
	 * @param nodeValue
	 *            the text of the text node that will be appended to the values
	 *            list.
	 * @throws DOMException
	 *             if an error occurs while removing Text nodes.
	 * @since 7.0
	 */
	public void setNodeValue(String nodeValue) {
		clear();

		// add it as first text child
		addValue(nodeValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespaceURI() {
		return isDOMEnabled() ? field.namespaceURI : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAttribute(String name) {
		Property prop = getAttributeNode(name);
		return prop == null ? "" : prop.getNodeValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAttributeNS(String namespaceURI, String localName) {
		Property prop = getAttributeNodeNS(namespaceURI, localName);
		return prop == null ? "" : prop.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public Property getAttributeNode(String name) {
		if (hasAttributes()) {
			return field.propertyMap.getNamedItem(name);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Property getAttributeNodeNS(String namespaceURI, String localName) {
		if (hasAttributes()) {
			return field.propertyMap.getNamedItemNS(namespaceURI, localName);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public NodeList getElementsByTagName(String tagname) {
		enableDOM();
		List<NodeImpl> attList = new ArrayList<NodeImpl>();
		internalGetElementsByTagName(tagname, attList);

		return new ImmutableNodeList<NodeImpl>(attList, null);
	}

	public void internalGetElementsByTagName(String tagname, List<NodeImpl> attList) {

		for (Object child : values) {
			if (elementChildOfThisNode(child)) {
				if (((Attribute) child).getNodeName().equals(tagname) || "*".equals(tagname)) {
					attList.add((NodeImpl) child);
				}

				((Attribute) child).internalGetElementsByTagName(tagname, attList);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		enableDOM();
		List<NodeImpl> attList = new ArrayList<NodeImpl>();
		internalGetElementsByTagNameNS(namespaceURI, localName, attList);

		return new ImmutableNodeList<NodeImpl>(attList, null);
	}

	public void internalGetElementsByTagNameNS(String namespaceURI, String localName, List<NodeImpl> attList) {

		for (Object child : values) {
			if (elementChildOfThisNode(child)) {
				if ((((Attribute) child).getLocalName().equals(localName) || "*".equals(localName))
						&& ((namespaceURI != null && namespaceURI.equals(((Attribute) child).getNamespaceURI())) || "*"
								.equals(namespaceURI))) {
					attList.add((NodeImpl) child);
				}

				((Attribute) child).internalGetElementsByTagNameNS(namespaceURI, localName, attList);

			}
		}
	}

	/**
	 * Not implemented!
	 * 
	 * @return null;
	 */
	public TypeInfo getSchemaTypeInfo() {
		return null;
	}

	/**
	 * @return the tag name of the Attribute e.g. "prefix:localName"
	 * 
	 */
	public String getTagName() {
		return getNodeName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasAttributes() {
		return field != null && field.propertyMap != null && field.propertyMap.size() > 0;
	}

	/**
	 * @return true if this Attribute has any Properties, false otherwise
	 * @since 7.0
	 */
	public boolean hasAttribute(String name) {
		return hasAttributes() && field.propertyMap.getNamedItem(name) != null;
	}

	/**
	 * Checks to see if a property with the specified localName and Attribute
	 * exists.
	 * 
	 * @param localName
	 *            the local name of the Property
	 * @param namespaceURI
	 *            the namespace of the Property
	 * @return true if this Attribute has such a Property, false otherwise.
	 * @since 7.0
	 */
	public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
		return hasAttributes() && field.propertyMap.getNamedItemNS(namespaceURI, localName) != null;
	}

	/**
	 * Removes the Property found by the specified name (e.g. "pref:prop" or
	 * just "prop" if the property has no prefix). If no such property is found
	 * then the call is ignored.
	 * 
	 * @param name
	 *            the name of the property to look for.
	 * @since 7.0
	 */
	public void removeAttribute(String name) {
		if (hasAttributes()) {
			getAttributes().removeNamedItem(name);
		}
	}

	/**
	 * Removes the Property found by the specified local name and namespace. If
	 * no such property is found then the call is ignored.
	 * 
	 * @param namespaceURI
	 *            the namespace the property to remove belongs to
	 * @param localName
	 *            the local name of the property to be removed
	 * 
	 * @since 7.0
	 */
	public void removeAttributeNS(String namespaceURI, String localName) {
		if (hasAttributes()) {
			getAttributes().removeNamedItemNS(namespaceURI, localName);
		}
	}

	/**
	 * Removes the specified Property from this Attribute node. If the provided
	 * Property has no parent (i.e. parent == null) then this method returns
	 * <code>null</code>. If the Property has a parent but it is different than
	 * this Attribute then a {@link DOMException} is thrown. <br>
	 * The method will then search the Property in the Attribute and if found it
	 * will be removed and returned as a result. If could not be found a null
	 * will be returned.
	 * 
	 * @param oldAttr
	 *            the property to remove from this attribute.
	 * @return either the removed property or null stating that something in the
	 *         removing process went wrong.
	 * @since 7.0
	 */
	public Attr removeAttributeNode(Attr oldAttr) {

		if (oldAttr == null || oldAttr.getParentNode() == null) {
			return null;
		}

		if (oldAttr.getParentNode() != this) {
			throw new DOMException("ATTRIBUTE.NOT.OWNED.ERROR", oldAttr.getNodeName());
		}

		if (hasAttributes() && field.propertyMap.remove(oldAttr)) {
			((Property) oldAttr).disconnect();
			return oldAttr;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String name, String value) {
		Property prop = (Property) getAttributeNode(name);

		if (prop == null) {
			prop = new Property(name, null, value);
			prop.setParent(this);
			((PropertyMap) getAttributes()).add(prop);
		} else {
			prop.setNodeValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
		int index = qualifiedName.indexOf(':');
		String localName = qualifiedName;

		if (index > 0) {
			localName = qualifiedName.substring(index + 1);
		}

		Property prop = (Property) getAttributeNodeNS(namespaceURI, localName);

		if (prop == null) {
			prop = new Property(qualifiedName, namespaceURI, value);
			prop.setParent(this);
			((PropertyMap) getAttributes()).add(prop);
		} else {
			prop.setNodeValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Attr setAttributeNode(Attr newAttr) {

		if (newAttr instanceof Property) {
			Property newProp = (Property) newAttr;

			if (newProp.getParentNode() != this) {
				if (newProp.getParentNode() != null) {
					newProp.getParentNode().removeAttributeNode(newProp);
				}
				newProp.setParent(this);
			}

			Property res = ((PropertyMap) getAttributes()).setNamedItem(newAttr);

			if (res != newAttr) {
				if (res != null) {
					res.disconnect();
				}
			} else {
				res = null;
			}
			return res;
		} else {
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "setAttributeNode", "Property", "newAttr" });
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Attr setAttributeNodeNS(Attr newAttr) {

		if (newAttr instanceof Property) {
			Property newProp = (Property) newAttr;

			if (newProp.getParentNode() != this) {
				if (newProp.getParentNode() != null) {
					newProp.getParentNode().removeAttributeNode(newProp);
				}
				newProp.setParent(this);
			}

			Property res = ((PropertyMap) getAttributes()).setNamedItemNS(newProp);

			if (res != newAttr) {
				if (res != null) {
					res.disconnect();
				}
			} else {
				res = null;
			}
			return res;
		} else {
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE",
					new String[] { "setAttributeNodeNS", "Property", "newAttr" });
		}
	}

	/**
	 * IDs are not supported!
	 * 
	 * @param name
	 * @param isId
	 * @throws DOMException
	 *             - never
	 */
	public void setIdAttribute(String name, boolean isId) {
	}

	/**
	 * IDs are not supported!
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param isId
	 * @throws DOMException
	 *             - never
	 */
	public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) {
	}

	/**
	 * IDs are not supported!
	 * 
	 * @param idAttr
	 * @param isId
	 * @throws DOMException
	 *             - never
	 */
	public void setIdAttributeNode(Attr idAttr, boolean isId) {
	}

	/**
	 * First calls super{@link #isEqualNode(Node)} and if it returns true then
	 * the following checks are done: <br />
	 * 
	 * <ul>
	 * <li>checks whether the <code>other</code> object is instance of the
	 * Attribute class</li>
	 * <li>compares each Properties both Attributes have</li>
	 * <li>compares the operations of both Attributes</li>
	 * </ul>
	 * 
	 * If at least one of the above checks do not pass then the two Attributes
	 * are considered different.
	 * 
	 * @param other
	 *            the node to compare this Attribute to
	 * @return true if both Attributes are the same based on the checks above,
	 *         false otherwise.
	 * @since 7.0
	 */
	@Override
	public boolean isEqualNode(Node other) {
		if (!super.isEqualNode(other)) {
			return false;
		}

		if (!(other instanceof Attribute)) {
			return false;
		}

		if (getAttributes().getLength() != other.getAttributes().getLength()) {
			return false;
		}

		for (int i = 0; field.propertyMap != null && i < field.propertyMap.getLength(); i++) {
			Property prop = field.propertyMap.item(i);
			Property otherProp = (Property) other.getAttributes().getNamedItemNS(prop.getNamespaceURI(), prop.getLocalName());

			if (other == null || !prop.isEqualNode(otherProp)) {
				return false;
			}
		}

		if (((Attribute) other).operation != operation) {
			return false;
		}

		return true;
	}

	/**
	 * Contains Attribute specific logic for looking up a NameSpace using the
	 * provided prefix.
	 * 
	 * @param prefix
	 *            the prefix mapped to the namespace.
	 * @return the namespace if found otherwise null.
	 * @since 7.0
	 */
	@Override
	protected String internalNSLookup(String prefix) {
		// let's check if the element is declaring prefixes
		for (int i = 0; field != null && field.propertyMap != null && i < field.propertyMap.getLength(); i++) {
			Property prop = field.propertyMap.item(i);

			if (prop.getNamespaceURI() != null && prop.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
				// prefix null means we are searching for the default NS
				if (prefix == null && prop.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
					// this property defines a default namespace
					return prop.getNodeValue();
				} else if (prop.getPrefix() != null && prop.getPrefix().equals(XMLConstants.XMLNS_ATTRIBUTE)
						&& prop.getLocalName().equals(prefix)) {
					// yes the element is assigning a namespace to the prefix we
					// are looking for so return the NS
					return prop.getNodeValue();
				}
			}
		}

		// nothing found so far? check the parent it must know something
		if (parent != null) {
			return parent.lookupNamespaceURI(prefix);
		}

		return null;
	}

	/**
	 * Searches for the first prefix that is matched to the provided
	 * namespaceURI.
	 * 
	 * @param namespaceURI
	 *            specifies the namespace to look for.
	 * @return the first prefix that is mapped to this namespaceURI or null if
	 *         not found.
	 * @since 7.0
	 */
	@Override
	protected String internalLookupPrefix(String namespaceURI) {
		// check for property defining a prefix
		for (int i = 0; field != null && field.propertyMap != null && i < field.propertyMap.getLength(); i++) {
			Property prop = field.propertyMap.item(i);

			if (prop.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
				if ((prop.getPrefix().equals(XMLConstants.XMLNS_ATTRIBUTE)) && prop.getValue().equals(namespaceURI)) {
					// we found the property
					return prop.getLocalName();
				}
				if (prop.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE) && prop.getValue().equals(namespaceURI)) {
					// this is the default NS
					return null;
				}
			}
		}

		if (parent != null) {
			return parent.lookupPrefix(namespaceURI);
		}

		return null;
	}

	/**
	 * Appends the new child to the end of the list of values. If that child is
	 * already in the tree it is just moved. If this child belongs to another
	 * entry it is first cloned and then appended.
	 * 
	 * @param newChild
	 *            the new child node to append.
	 * @return the new appended node (possibly clone).
	 * @since 7.0
	 */
	@Override
	public Node appendChild(Node newChild) {
		return insertBefore(newChild, null);
	}

	/**
	 * @return the property map of this Attribute.
	 * @since 7.0
	 */
	@Override
	public NamedNodeMap getAttributes() {
		enableDOM();

		if (field.propertyMap == null) {
			field.propertyMap = new PropertyMap();
		}
		return field.propertyMap;
	}

	/**
	 * This is the internal list of DOM children this Attribute have. Any
	 * changes made to it will reflect the Attribute as well.
	 * 
	 * @return the internal list of children nodes
	 */
	@Override
	public NodeList getChildNodes() {
		enableDOM();
		if (field.hiddenChildren == null) {
			field.hiddenChildren = new Attribute.ImmutableNodeList<Object>(values, this);
		}

		return field.hiddenChildren;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getFirstChild() {
		if (hasChildNodes()) {
			return getChildNodes().item(0);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getLastChild() {
		if (hasChildNodes()) {
			return getChildNodes().item(values.size() - 1);
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildNodes() {
		return getChildNodes().getLength() > 0;
	}

	/**
	 * Inserts the new child before the reference child. If the new child is
	 * already in the tree it is just moved. If this child belongs to another
	 * entry it is first cloned and then inserted. If the reference child is
	 * null then the new child is appended to the end.
	 * 
	 * @param newChild
	 *            the new child node to inserted.
	 * @param refChild
	 *            the reference child before which the new child will be
	 *            inserted.
	 * @return the new appended node (possibly clone).
	 * @since 7.0
	 */
	@Override
	public Node insertBefore(Node newChild, Node refChild) {

		if (!(newChild instanceof NodeImpl))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "insertBefore", "NodeImpl", "newChild" });

		if (refChild != null && !(refChild instanceof NodeImpl))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "insertBefore", "NodeImpl", "refChild" });

		if (refChild == newChild) {
			return newChild;
		}

		enableDOM();

		// check for repetition in the tree.
		if (((NodeImpl) newChild).getOwnerDocument() == getOwnerDocument() && ((NodeImpl) newChild).parent == this) {
			// make sure this is not added twice...
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == newChild) {
					((NodeImpl) values.get(i)).disconnect();
					values.remove(i--);
					if (!isElementNode(newChild)) {
						--field.valuesCount;
					}
				}
			}
		}

		newChild = getValidNode(newChild);

		newChild = internalInsertBefore((NodeImpl) newChild, refChild);

		internalFixEntryWhenInsert(newChild);

		return newChild;
	}

	/**
	 * Internal method that avoids all the redundant checks. Use this version if
	 * you are sure you cannot fall into any of the checks the public version of
	 * this method does.
	 * 
	 * @param newChild
	 *            the new child to insert
	 * @param refChild
	 *            the old child pointing where the new will be inserted, if null
	 *            just appends the new one.
	 * @return the new child after it has been cloned (if necessary).
	 * @since 7.0
	 */
	final Node internalInsertBefore(NodeImpl newChild, Node refChild) {
		boolean set = false;

		if (newChild.parent != this) {
			newChild.setParent(this);
		}

		NodeImpl result = newChild;

		if (refChild != null) {

			int oldNodePos = values.indexOf(refChild);
			if (oldNodePos > -1) {
				values.add(oldNodePos, newChild);
			} else {
				values.add(newChild);
			}

			set = true;
		}

		if (!set) {
			values.add(result);
		}

		if (isDOMEnabled() && !isElementNode(result)) {
			++field.valuesCount;
		}

		return result;
	}

	/**
	 * Removes the specified Node from this Attribute. The passed node could be
	 * any instance of the NodeImpl class.
	 * 
	 * @param oldChild
	 *            the node to remove.
	 * @return the removed node.
	 * @since 7.0
	 */
	@Override
	public Node removeChild(Node oldChild) {
		if (!(oldChild instanceof NodeImpl))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "removeChild", "NodeImpl", "oldChild" });

		if (oldChild instanceof Property) {
			removeAttributeNode((Attr) oldChild);
		} else if (((NodeImpl) oldChild).parent == this) {
			enableDOM();
			internalRemoveChildAndFixEntry(oldChild);
		}

		return oldChild;
	}

	final Node internalRemoveChild(Node oldChild) {
		values.remove(oldChild);
		// repeated call to connect/disconnect is not that painful.
		((NodeImpl) oldChild).disconnect();

		if (isDOMEnabled() && !isElementNode(oldChild)) {
			--field.valuesCount;
		}

		return oldChild;
	}

	final Object internalRemoveChild(int oldChildPos) {
		Object oldChild = values.remove(oldChildPos);

		if (oldChild instanceof NodeImpl) {
			// repeated call to connect/disconnect is not that painful.
			((NodeImpl) oldChild).disconnect();
		}

		if (field != null && !isElementNode(oldChild)) {
			--field.valuesCount;
		}

		return oldChild;
	}

	final boolean internalFixEntryWhenInsert(Node newChild) {
		boolean inserted = false;
		if (isElementNode(newChild) && ((Attribute) newChild).getOwnerDocument() != null) {

			Attribute nChild = (Attribute) newChild;
			Attribute parent = nChild.getParentNode();

			String fullName = parent != null ? (parent.getFullName() != null ?
			// the parent is indexed by the Entry so its children should also be
			// indexed.
			parent.getFullName() + Entry.PATH_SEPARATOR_STR + nChild.getName()

					// not indexed... we don't index the child.
					: null)

			// we are most likely renaming a node directly attached to
					// the Entry.
					: nChild.getName();

			if (fullName != null) {
				Entry.KeyName kn = new Entry.KeyName(fullName);

				// don't add this child if such child is already there.
				if (nChild.doc.internalGetAttribute(kn) == null) {
					nChild.doc.internalPutAttribute(kn, ((Attribute) newChild), false, true);
					inserted = true;
				}
			}
		}
		return inserted;
	}

	final boolean internalFixEntryWhenRemove(Object oldChild) {
		if (isDOMEnabled() && isElementNode(oldChild) && this.getOwnerDocument() != null) {

			if (((Attribute) oldChild).getFullName() != null) {
				// repeated call to connect/disconnect is not that painful.
				return this.doc.internalRemoveAttribute(new Entry.KeyName(((Attribute) oldChild).getFullName()), false, false) != null;
			} else {
				return this.doc.internalRemoveAttribute(new Entry.KeyName(((Attribute) oldChild).getName()), false, false) != null;
			}
		}
		return false;
	}

	final Node internalRemoveChildAndFixEntry(Node oldChild) {
		internalFixEntryWhenRemove(oldChild);
		return internalRemoveChild(oldChild);
	}

	final Object internalRemoveChildAndFixEntry(int oldChildPos) {
		Object oldChild = values.get(oldChildPos);

		internalFixEntryWhenRemove(oldChild);

		return internalRemoveChild(oldChildPos);
	}

	/**
	 * Replaces an existing node with a new node. If the existing node is not
	 * found null is returned.
	 * 
	 * @param newChild
	 *            the node to replace the old node with
	 * @param oldChild
	 *            then node that will be replaced
	 * @return the old node if found, null otherwise
	 * @since 7.0
	 */
	@Override
	public Node replaceChild(Node newChild, Node oldChild) {
		if (newChild instanceof Property || oldChild instanceof Property)
			return null;

		if (!(newChild instanceof NodeImpl))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "replaceChild", "NodeImpl", "newChild" });

		if (oldChild != null && !(oldChild instanceof NodeImpl))
			throw new DOMException("MISERVER.UNEXPECTED.PARAMETER.TYPE", new String[] { "replaceChild", "NodeImpl", "oldChild" });

		enableDOM();

		int pos = values.indexOf(oldChild);

		if (pos > -1) {
			// removing will take care of the valuesCount
			internalRemoveChildAndFixEntry(pos);

			newChild = getValidNode(newChild);
			values.add(pos, newChild);

			if (!isElementNode(newChild)) {
				++field.valuesCount;
			}

			return oldChild;
		}

		return null;
	}

	public void setPrefix(String prefix) throws DOMException {
		enableDOM();

		// calculate the new escaped localName
		String localName;
		int index = name.indexOf(":");
		if (index > 0) {
			localName = name.substring(index + 1);
		} else {
			localName = name;
		}

		// calculate the new escaped nodeName
		String newName;
		if (prefix != null && prefix.trim().length() > 0) {
			newName = prefix + ":" + localName;
		} else {
			newName = localName;
		}

		// set the new escaped name.
		setName(newName);
	}

	public String getPrefix() {
		enableDOM();
		if (field.prefix == null && field.localName == null) {
			parseName();
		}
		return field.prefix;
	}

	/**
	 * @return the node name. This name is different than the one returned by
	 *         the {@link #getName()} method, because it does not contain
	 *         escaped characters. For example if this method returns
	 *         <b>node.name</b> it will defer from the other name which would be
	 *         <b>node\.name</b>
	 */
	@Override
	public String getNodeName() {
		enableDOM();
		if (field.localName == null) {
			parseName();
		}
		return field.qName;
	}

	/**
	 * Wrapper class used to protect the internal list of values.
	 * 
	 * @param <N>
	 *            Could be anything... If not instance of Attribute or
	 *            AttributeValue it will be wrapped and returned.
	 */
	static final class ImmutableNodeList<N> implements NodeList {

		List<N> list;
		private Attribute parent;

		ImmutableNodeList(List<N> values, Attribute parent) {
			this.list = values;
			this.parent = parent;
		}

		public int getLength() {
			return list.size();
		}

		@SuppressWarnings("unchecked")
		public Node item(int i) {
			N item = list.get(i);

			if (!(item instanceof AttributeValue) && parent != null && parent.notElementChildOfThisNode(item)) {
				item = (N) new AttributeValue(item);
				((NodeImpl) item).setParent(parent);
				list.set(i, item);
			}

			return (Node) item;
		}
	}

	/**
	 * This is a class which optimizes the Attribute creation, this is done by
	 * telling the JVM to initialize only one field to null instead all the
	 * fields this class holds. This might have some performance optimization
	 * for the serialization of an Attribute object but that is not 100%
	 * confirmed.
	 */
	private final static class FieldsOptimizer implements Serializable {

		private static final long serialVersionUID = 4582101600919662114L;

		// (*) Start of TDI 70 fields...

		// (*)(*) fields that are manually serialized...
		private int valuesCount;

		/**
		 * Used to hold the path this attribute was indexed with. If this is
		 * null then the Entry has not indexed this Attribute.
		 */
		private String fullName;

		/**
		 * This private variable holds the namespaceURI of this element.
		 */
		private String namespaceURI;

		/**
		 * This private variable holds the map with the attributes of this
		 * element.
		 */
		private PropertyMap propertyMap;

		// (*)(*) end of manually serialized fields...

		// (*)(*) fields that are not serialized at all...
		/**
		 * This private variable holds the prefix of this element. This field is
		 * created on demand if it does not exist.
		 */
		private transient String prefix;

		/**
		 * The local name of this Attribute. This field is created on demand if
		 * it does not exist.
		 */
		private transient String localName;

		/**
		 * The qualified name of the Attribute (prefix:localName). This field is
		 * created on demand if it does not exist. This differs from the
		 * {@link Attribute#name} field because this one contains normalized
		 * representation of the name.
		 */
		private transient String qName;

		/**
		 * This is a wrapper of the values list of objects that makes sure each
		 * single element is wrapped by an AttributeValue instance for access
		 * through the DOM API. This field is created on demand if it does not
		 * exist.
		 */
		private transient NodeList hiddenChildren;

		// (*)(*) End of non-serialized fields...

		// (*) End of TDI 70 fields...

		public FieldsOptimizer(int valuesCount) {
			this.valuesCount = valuesCount;
		}
	}

}
