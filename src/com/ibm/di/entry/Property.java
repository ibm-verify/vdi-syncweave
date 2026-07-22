/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import com.ibm.di.exceptions.DOMException;

/**
 * 
 * This class represents the attribute in terms of XML concepts.
 * 
 * @since 7.0
 * 
 */
public class Property extends NodeImpl implements Attr {

	// *************************************************************************
	// Static Declaration

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * version ID
	 */
	private static final long serialVersionUID = -636082772847685789L;

	public static final String DEFAULT_VALUE = "";

	// *************************************************************************
	// Member Declaration

	private String localName;

	private String value = DEFAULT_VALUE;

	private String prefix;

	private String namespaceURI;

	// *************************************************************************
	// Constructor Declaration

	public Property(String name, String namespaceURI, String value) {
		this(name, namespaceURI);

		this.parent = null;

		if (value != null) {
			this.value = value;
		}
	}

	public Property(String qualifiedName, String namespaceURI) {

		if (qualifiedName == null || qualifiedName.length() == 0)
			throw new DOMException("PROPERTY.MISSING.NAME");

		rename(qualifiedName, namespaceURI);
	}

	// *************************************************************************
	// Attr Interface Implementation
	/**
	 * @return the fully qualified name of the Property
	 */
	public String getName() {

		return getNodeName();
	}

	/**
	 * @return the owner element
	 */
	public Attribute getOwnerElement() {
		return parent;
	}

	/**
	 * @return null
	 */
	public TypeInfo getSchemaTypeInfo() {

		return null;
	}

	/**
	 * @return true
	 */
	public boolean getSpecified() {

		return true;
	}

	/**
	 * @return the value of the property
	 */
	public String getValue() {

		return value;
	}

	/**
	 * @return false
	 */
	public boolean isId() {
		return false;
	}

	/**
	 * Sets the value of the Property.
	 */
	public void setValue(String value) throws DOMException {
		this.value = value == null ? DEFAULT_VALUE : value;
	}

	/**
	 * Not applicable for properties. Does nothing.
	 * 
	 * @return null;
	 */
	public Node appendChild(Node arg0) throws DOMException {
		return null;
	}

	/**
	 * @return a new Property object that is a copy of this Property but does
	 *         not belong to any Entry or Attribute.
	 */
	public Node cloneNode(boolean arg0) {
		return new Property(getNodeName(), namespaceURI, value);
	}

	/**
	 * Not applicable for properties Does nothing.
	 * 
	 * @return 0;
	 */
	public short compareDocumentPosition(Node arg0) throws DOMException {
		return 0;
	}

	/**
	 * @return the local name of the Property.
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * @return Node.ATTRIBUTE_NODE
	 */
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}

	/**
	 * @return the value of this Property
	 */
	public String getNodeValue() throws DOMException {
		return value;
	}

	/**
	 * @return true if the other node, passed as parameter, is an object of type
	 *         Property, has the same qualified name, same namespace URI and the
	 *         same value, otherwise returns false.
	 */
	public boolean isEqualNode(Node other) {
		if (!super.isEqualNode(other))
			return false;

		if (!(other instanceof Property))
			return false;

		if (value.length() != other.getNodeValue().length()
				|| !value.equals(other.getNodeValue()))
			return false;

		return true;
	}

	/**
	 * same as {@link #setValue(String)}
	 */
	public void setNodeValue(String aValue) throws DOMException {
		setValue(aValue);
	}

	/**
	 * This method sets the given prefix to the Property. If the prefix is
	 * "xmlns" then it is considered that the Property defines a prefix to
	 * namespace in terms of XML concepts, thus the namespace is set to be
	 * "http://www.w3.org/2000/xmlns/". The same namespace is automatically set
	 * if the prefix is either null or an empty string and the name of the
	 * Property is "xmlns".
	 * 
	 * @param prefix
	 *            - the prefix that should be set to this Property
	 */
	public void setPrefix(String prefix) throws DOMException {

		prefix = (prefix == null || prefix.trim().length() == 0) ? null
				: prefix;

		rename(prefix == null ? localName : prefix + ":" + localName, null);
	}

	// *************************************************************************
	// Non-Inherited Methods Implementation

	/**
	 * renames the Property
	 * 
	 * @param qname
	 * @param namespaceURI
	 * @return the same object with different identifiers
	 */
	boolean rename(String qname, String namespaceURI) {
		int colPos = qname.indexOf(':');
		String xmlns = null;

		if (colPos > 0) {
			// ok we have a prefix so we should have a namespace
			prefix = qname.substring(0, colPos);
			localName = qname.substring(colPos + 1);
			xmlns = prefix;
		} else {
			// no prefix...
			prefix = null;
			localName = qname;
			xmlns = qname;
		}

		if (xmlns.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
			// defining namespace
			this.namespaceURI = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else if (prefix != null && prefix.equals(XMLConstants.XML_NS_PREFIX)) {
			this.namespaceURI = XMLConstants.XML_NS_URI;
		} else {
			this.namespaceURI = namespaceURI == null
					|| namespaceURI.trim().length() == 0 ? null : namespaceURI;
		}

		// if this.namespaceURI is null then neither the prefix nor the name is
		// "xmlns"
		if (namespaceURI != null && this.namespaceURI == null)
			this.namespaceURI = namespaceURI;

		if (this.namespaceURI == null && prefix != null) {
			// if not specifying a ns then try to lookup it from the parent if
			// it is not declared then an exception is thrown
			this.namespaceURI = lookupNamespaceURI(prefix);
		}

		return true;
	}

	protected String internalNSLookup(String prefix) {
		return parent != null ? parent.lookupNamespaceURI(prefix) : null;
	}

	@Override
	protected String internalLookupPrefix(String namespaceURI) {
		return parent != null ? parent.lookupPrefix(namespaceURI) : null;
	}

	/**
	 * @return the value of this Property
	 */
	public String toString() {
		return getValue();
	}

	@Override
	public Document getOwnerDocument() {
		return parent != null ? parent.getOwnerDocument() : null;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public String getPrefix() {
		return prefix;
	}
}
