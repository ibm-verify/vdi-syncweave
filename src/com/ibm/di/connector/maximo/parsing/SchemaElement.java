/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.parsing;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.di.connector.maximo.exception.MxConnTypeConvertionException;
import com.ibm.di.connector.maximo.util.typeconverter.BooleanConverter;
import com.ibm.di.connector.maximo.util.typeconverter.DoubleConverter;
import com.ibm.di.connector.maximo.util.typeconverter.IMxTypeConverter;
import com.ibm.di.connector.maximo.util.typeconverter.IntegerConverter;
import com.ibm.di.connector.maximo.util.typeconverter.LongConverter;
import com.ibm.di.connector.maximo.util.typeconverter.StringConverter;

/**
 * This class represents the structural elements that compose a {@link Schema
 * schema} object and contains all the metadata.
 * 
 * @since 7.1
 * @see Schema
 * @see SchemaConfiguration
 */
public final class SchemaElement {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String ATTR_SEPARATOR = "#";

	private static Map<String, IMxTypeConverter> classToConverter;

	private static final String EL_SEPARATOR = "@";

	private static Map<String, String> primitiveToWrapper;

	private final boolean attribute;

	private final List<SchemaElement> childrenList;

	private final Map<String, SchemaElement> childrenMap;

	private String className;

	private IMxTypeConverter converter;

	private final String name;

	private final SchemaElement parent;

	private Integer size;

	private final boolean uniqueKey;
	
	private boolean required;

	private final List<SchemaElement> uniqueKeyList;

	static {
		primitiveToWrapper = new HashMap<String, String>();
		primitiveToWrapper.put("boolean", Boolean.class.getName());
		primitiveToWrapper.put("int", Integer.class.getName());
		primitiveToWrapper.put("integer", Integer.class.getName());
		primitiveToWrapper.put("long", Long.class.getName());
		primitiveToWrapper.put("double", Double.class.getName());

		classToConverter = new HashMap<String, IMxTypeConverter>();
		classToConverter.put(Boolean.class.getName(), BooleanConverter.getInstance());
		classToConverter.put(Double.class.getName(), DoubleConverter.getInstance());
		classToConverter.put(Integer.class.getName(), IntegerConverter.getInstance());
		classToConverter.put(Long.class.getName(), LongConverter.getInstance());
	}

	private SchemaElement(final SchemaElement parent, final String name, final boolean attribure, final boolean uniqueKey) {

		this.childrenList = new LinkedList<SchemaElement>();
		this.uniqueKeyList = new LinkedList<SchemaElement>();
		this.childrenMap = new TreeMap<String, SchemaElement>();
		this.parent = parent;
		this.name = name;
		this.attribute = attribure;
		this.uniqueKey = uniqueKey;

		this.size = Integer.MAX_VALUE;
	}

	/**
	 * Builds a schema element object.
	 * 
	 * @param parent
	 *            parent schema element
	 * @param name
	 *            schema element's name
	 * @param attribute
	 *            <code>true</code> if this schema element represents an XSD
	 *            attribute, otherwise <code>false</code>
	 * @param uniqueKey
	 *            <code>true</code> if this element represents an unique key,
	 *            otherwise <code>false</code>
	 * @return a new schema element
	 */
	public static SchemaElement buildElement(final SchemaElement parent, final String name, final boolean attribute,
			final boolean uniqueKey) {
		return new SchemaElement(parent, name, attribute, uniqueKey);
	}

	/**
	 * Builds a root schema element object.
	 * 
	 * @param name
	 *            schema element's name
	 * @return a new root schema element
	 */
	public static SchemaElement buildRootElement(final String name) {

		return new SchemaElement(null, name, false, false);
	}

	/**
	 * Adds a child schema element.
	 * 
	 * @param child
	 *            child schema element
	 */
	public void addChild(final SchemaElement child) {

		if (!childrenMap.containsKey(child.getName())) {
			childrenMap.put(child.getName(), child);
			childrenList.add(child);

			if (child.isUniqueKey()) {
				uniqueKeyList.add(child);
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if this schema element contains a child element for
	 * the specified name.
	 * 
	 * @param name
	 *            name whose presence in this schema element is to be tested
	 * @return <tt>true</tt> if this schema element contains a child element for
	 *         the specified name, otherwise <tt>false</tt>
	 */
	public boolean containsChild(final String name) {

		return getChild(name) != null;
	}

	/**
	 * Dumps all the structure in a {@link StringBuilder} object.
	 * 
	 * @param b
	 *            {@link StringBuilder} where the structure will be dumped
	 * @return {@link StringBuilder} with the dumped structure
	 */
	public StringBuilder dumpElements(final StringBuilder b) {

		b.append(this.toString()).append(System.getProperty("line.separator"));

		for (final SchemaElement e : childrenList) {
			e.dumpElements(b);
		}

		return b;
	}

	/**
	 * Dumps all the structure in a {@link StringBuilder} object (not including
	 * attributes)
	 * 
	 * @param b
	 *            {@link StringBuilder} where the structure will be dumped
	 * @return {@link StringBuilder} with the dumped structure
	 */
	public StringBuilder dumpElements(final StringBuilder b, String tabs, boolean inclAttributes) {

		b.append(tabs + this.getName()).append(System.getProperty("line.separator"));

		for (final SchemaElement e : childrenList) {
			if ((e.isAttribute() && inclAttributes) || !e.isAttribute())
				e.dumpElements(b, tabs + "\t", inclAttributes);
		}

		return b;
	}

	/**
	 * Returns the child schema element for the specified name. Name is with the
	 * following format:
	 * <code>DocRoot@com.ibm.maximo@UpdateMXASSET@MXASSETSet@ASSET#relationship</code>
	 * , where <code>relationship</code> is an attribute name and the rest is
	 * the path.
	 * 
	 * @param name
	 *            name whose associated child is to be returned
	 * @return the child element for the specified name, or <tt>null</tt> if
	 *         this schema element contains no child for the name
	 * @see #containsChild(String)
	 */
	public SchemaElement getChild(final String name) {
		final String[] elements = name.split(ATTR_SEPARATOR);
		final String attrName;

		if (elements.length > 1) {
			attrName = elements[1];
		} else {
			attrName = null;
		}

		final String[] path = elements[0].split(EL_SEPARATOR);

		SchemaElement currentElement = this;
		for (int i = 0; i < path.length; i++) {
			if (!currentElement.childrenMap.containsKey(path[i])) {
				return null;
			}
			currentElement = currentElement.childrenMap.get(path[i]);
		}

		if (attrName != null) {
			currentElement = currentElement.childrenMap.get(attrName);
		}

		return currentElement;
	}

	/**
	 * Returns a list of all schema element children.
	 * 
	 * @return list of all schema element children
	 */
	public List<SchemaElement> getChildren() {
		return Collections.unmodifiableList(childrenList);
	}

	/**
	 * Returns this schema element's class name.
	 * 
	 * @return this schema element's class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the first child schema element.
	 * 
	 * @return the first child element, or <tt>null</tt> if this schema element
	 *         contains no child
	 */
	public SchemaElement getFirstChild() {
		return childrenList.get(0);
	}

	/**
	 * Returns this schema element's name.
	 * 
	 * @return this schema element's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parent element.
	 * 
	 * @return parent element
	 */
	public SchemaElement getParent() {
		return parent;
	}

	/**
	 * Returns a {@link String} object representing the path to this schema
	 * element starting from the root element.
	 * 
	 * @return a {@link String} object representing the path to this schema
	 *         element
	 * @see #getPathRelativeTo(SchemaElement)
	 * @see #getEntryPathRelativeTo(SchemaElement)
	 */
	public String getPath() {
		return getPathRelativeTo(null, new StringBuilder()).toString();
	}

	/**
	 * Returns a {@link String} object representing the path to this schema
	 * element in the hierarchical structure.
	 * <p>
	 * Below is a sample structure of schema elements.
	 * </p>
	 * 
	 * <pre>
	 * root (root)
	 * |
	 * +--child_A (root@child_A)
	 * |  |
	 * |  +--child_A1 (root@child_A@child_A1)
	 * |
	 * +--child_B (root@child_B)
	 * </pre>
	 * 
	 * <p>
	 * Between parentheses is the resulting path to the respective schema
	 * element starting from the root.
	 * </p>
	 * 
	 * @param e
	 *            element from which the path representation will be built
	 * @return a {@link String} object representing the path to this schema
	 *         element
	 */
	public String getPathRelativeTo(final SchemaElement e) {
		return getPathRelativeTo(e, new StringBuilder()).toString();
	}

	/**
	 * Returns a {@link String} object representing the path to this schema
	 * element in the context of hierarchical entry.
	 * <p>
	 * Below is a sample structure of schema elements.
	 * </p>
	 * 
	 * <pre>
	 * root (root)
	 * |
	 * +--child1 (root.child1)
	 * |  |
	 * |  +--child11 (root.child1.child11)
	 * |
	 * +--child2 (root.child2)
	 * </pre>
	 * 
	 * <p>
	 * Between parentheses is the resulting path to the respective element
	 * starting from the root.
	 * </p>
	 * 
	 * @param e
	 *            element from which the path representation will be built
	 * @return a {@link String} object representing the path to this schema
	 *         element
	 */
	public String getEntryPathRelativeTo(final SchemaElement e) {
		return getEntryPathRelativeTo(e, new StringBuilder()).toString();
	}

	/**
	 * Returns this schema element's maximum size.
	 * 
	 * @return this schema element's maximum size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * Returns a list of elements defined as unique key.
	 * 
	 * @return list of elements defined as unique key
	 */
	public List<SchemaElement> getUniqueKeyList() {
		if (isRoot()) {
			return new LinkedList<SchemaElement>(uniqueKeyList);
		}

		final List<SchemaElement> keys = parent.getUniqueKeyList();
		keys.addAll(uniqueKeyList);

		return keys;
	}

	/**
	 * Indicates if this schema element represents an XML attribute.
	 * 
	 * @return <code>true</code> if this schema element represents an XSD
	 *         attribute, otherwise <code>false</code>
	 */
	public boolean isAttribute() {
		return attribute;
	}

	/**
	 * Indicates if this element represents a MBO definition (i.e if at least
	 * one child is not an attribute).
	 * 
	 * @return <code>true</code> if this element represents a MBO definition,
	 *         otherwise <code>false</code>
	 */
	public boolean isMboDefinition() {
		for (final SchemaElement child : childrenList) {
			if (!child.isAttribute()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates if this element represents an unique key.
	 * 
	 * @return <code>true</code> if this element represents an unique key,
	 *         otherwise <code>false</code>
	 */
	public boolean isUniqueKey() {
		return uniqueKey;
	}

	/**
	 * Defines this schema element's class name. The class name is used in type
	 * conversion and validation.
	 * <p>
	 * Any class name is acceptable, but only these can be used in type
	 * conversion and validation:
	 * </p>
	 * <ul>
	 * <li>boolean <i>or</i> java.lang.Boolean</li>
	 * <li>int<i>or</i> integer <i>or</i> java.lang.Integer</li>
	 * <li>long <i>or</i> java.lang.Long</li>
	 * <li>double <i>or</i> java.lang.Double</li>
	 * <li>java.lang.String</li>
	 * </ul>
	 * <p>
	 * If the class name is different than those, it will be treated as
	 * <code>java.lang.String</code>.
	 * </p>
	 * 
	 * @param className
	 *            this schema element's class name
	 * @see #valueOf(String)
	 * @see #toString(Object, boolean)
	 */
	public void setClassName(final String className) {

		if (primitiveToWrapper.containsKey(className)) {
			this.className = primitiveToWrapper.get(className);
		} else {
			this.className = className;
		}

		if (classToConverter.containsKey(this.className)) {
			this.converter = classToConverter.get(this.className);
		} else {
			this.converter = StringConverter.getInstance();
		}
	}

	/**
	 * Defines this schema element's maximum size.
	 * 
	 * @param size
	 *            this schema element's maximum size
	 */
	public void setSize(final Integer size) {
		this.size = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(SchemaElement.class.getName());
		sb.append('{');
		sb.append("path=").append(getPath()).append("; ");
		sb.append("name=").append(name).append("; ");
		sb.append("attribute=").append(attribute).append("; ");
		sb.append("uniqueKey=").append(uniqueKey).append("; ");
		sb.append("className=").append(className).append("; ");
		sb.append("size=").append(size).append("; ");
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Returns a {@link String} object representing the specified <tt>value</tt>
	 * , according to this schema element's class name.
	 * 
	 * @param value
	 *            value to be converted
	 * @return {@link String} object representing the specified <tt>value</tt>,
	 *         or <code>null</code> if <tt>value</tt> is <code>null</code>
	 * @throws MxConnTypeConvertionException
	 *             if the specified <tt>value</tt> can not be converted
	 */
	public String toString(final Object value) throws MxConnTypeConvertionException {
		return converter.toString(value);
	}

	/**
	 * Converts the specified {@link String} <tt>value</tt>, according to this
	 * schema element's class name.
	 * 
	 * @param value
	 *            value to be converted
	 * @return value converted
	 * @throws MxConnTypeConvertionException
	 *             if the specified <tt>value</tt> can not be converted
	 */
	public Object valueOf(final String value) throws MxConnTypeConvertionException {
		return converter.valueOf(value);
	}

	private StringBuilder getPathRelativeTo(final SchemaElement e, final StringBuilder b) {
		if (!isRoot() && !parent.equals(e)) {
			parent.getPathRelativeTo(e, b);

			if (isAttribute()) {
				b.append(ATTR_SEPARATOR);
			} else {
				b.append(EL_SEPARATOR);
			}
		}

		b.append(name);
		return b;
	}

	private StringBuilder getEntryPathRelativeTo(final SchemaElement e, final StringBuilder b) {
		if (!isRoot() && !parent.equals(e)) {
			parent.getEntryPathRelativeTo(e, b);
			b.append('.');
		}
		b.append(name);
		return b;
	}

	private boolean isRoot() {
		return parent == null;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
