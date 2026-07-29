/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

import com.ibm.di.exceptions.DOMException;

/**
 * Basic implementation of the {@link Node} interface. Each descendant, except
 * for those that implement the {@link Document} interface, should follow the
 * general contract:
 * <ul>
 * <li>doc = null
 * <ul>
 * <li>parent = null -> the Node has neither a parent nor a Document. This might
 * be a brand new Node not yet attached to an Element.</li>
 * <li>parent != null -> the Node has a parent but its parent or the top-most
 * parent is not attached to a Document.</li>
 * </ul>
 * </li>
 * <li>doc != null
 * <ul>
 * <li>parent = null -> this is an Element node attached directly to the
 * Document</li>
 * <li>parent != null -> this is a node which up the hierarchy has a parent
 * attached to a Document</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @since 7.0
 */
public abstract class NodeImpl implements Node, Serializable {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * version ID
	 */
	private static final long serialVersionUID = -5367559036450244166L;

	// *************************************************************************
	// Static Declaration

	// *************************************************************************
	// Member Declaration

	/**
	 * This private variable holds a reference to the parent of this element.
	 */
	protected transient Attribute parent;

	/**
	 * This private variable holds a reference to the document of this element.
	 */
	// protected Entry doc;
	public Node appendChild(Node newChild) throws DOMException {
		return null;
	}

	/**
	 * Does nothing!
	 * 
	 * @param other
	 * @throws DOMException
	 *             - never
	 * 
	 * @return 0
	 */
	public short compareDocumentPosition(Node other) throws DOMException {
		return 0;
	}

	/**
	 * @return PropertyMap object, that contains all the properties of this
	 *         Attribute.
	 */
	public NamedNodeMap getAttributes() {
		return null;
	}

	/**
	 * Does Nothing!
	 * 
	 * @return null;
	 */
	public String getBaseURI() {
		return null;
	}

	/**
	 * Does Nothing!
	 * 
	 * @param version
	 * @param feature
	 * 
	 * 
	 * @return Boolean.FALSE
	 */
	public Object getFeature(String feature, String version) {
		return Boolean.FALSE;
	}

	public String getNodeName() {
		return getPrefix() == null ? getLocalName() : getPrefix() + ":"
				+ getLocalName();
	}

	public Document getOwnerDocument() {
		return parent != null ? parent.getOwnerDocument() : null;
	}

	public Attribute getParentNode() {
		return parent;
	}

	public String getTextContent() throws DOMException {
		return getNodeValue();
	}

	/**
	 * Does nothing!
	 * 
	 * @param key
	 * 
	 * @return null
	 */
	public Object getUserData(String key) {
		return null;
	}

	public boolean hasAttributes() {
		return false;
	}

	public boolean hasChildNodes() {
		return false;
	}

	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		return null;
	}

	public boolean isDefaultNamespace(String namespaceURI) {
		// no prefix? then our element's namespace should be the default one
		if (this.getPrefix() == null) {
			if (namespaceURI == null) {
				return (this.getNamespaceURI() == null);
			}
			return namespaceURI.equals(this.getNamespaceURI());
		}

		// do node specific search.
		return namespaceURI != null
				&& namespaceURI.equals(internalNSLookup(null));
	}

	protected String internalNSLookup(String prefix) {
		return null;
	}

	public boolean isEqualNode(Node other) {
		if (this == other)
			return true;

		if (!(other instanceof NodeImpl))
			return false;

		if (!getLocalName().equals(other.getLocalName()))
			return false;

		if (!(getNamespaceURI() == other.getNamespaceURI() || (getNamespaceURI() != null && getNamespaceURI()
				.equals(other.getNamespaceURI())))) {
			return false;
		}

		if (!(getPrefix() == other.getPrefix() || (getPrefix() != null && getPrefix()
				.equals(other.getPrefix())))) {
			return false;
		}

		if (hasAttributes() != other.hasAttributes())
			return false;

		return true;
	}

	public boolean isSameNode(Node other) {
		return this == other;
	}

	/**
	 * Does nothing!
	 * 
	 * @param feature
	 * @param version
	 * @return false
	 */
	public boolean isSupported(String feature, String version) {
		return false;
	}

	public String lookupNamespaceURI(String prefix) {
		// start from this element
		if (// looking for the default NS of this element?
		(this.getPrefix() == null && prefix == null) ||
		// looking for the element's NS?
				(this.getPrefix() != null && this.getPrefix().equals(prefix))) {

			return this.getNamespaceURI();
		}

		// some nodes could search elsewhere
		return internalNSLookup(prefix);
	}

	public String lookupPrefix(String namespaceURI) {
		if (namespaceURI != null && namespaceURI.trim().length() > 0) {
			// start from this element
			if (this.getNamespaceURI().equals(namespaceURI)) {
				// OK the namespace is the same as the element's namespace so
				// return the element's prefix
				return getPrefix();
			}
			return internalLookupPrefix(namespaceURI);
		}
		return null;
	}

	protected String internalLookupPrefix(String namespaceURI) {
		return null;
	}

	/**
	 * Not Supported!
	 */
	public void normalize() {
	}

	public Node removeChild(Node oldChild) throws DOMException {
		return null;
	}

	public Node replaceChild(Node newChild, Node oldChild) {
		return null;
	}

	public void setTextContent(String textContent) {
		setNodeValue(textContent);
	}

	/**
	 * Does nothing!
	 * 
	 * @param key
	 * @param data
	 * @param handler
	 * 
	 * @return null
	 */
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	public NodeList getChildNodes() {
		return null;
	}

	public Node getFirstChild() {
		return null;
	}

	public Node getLastChild() {
		return null;
	}

	public Node getNextSibling() {
		if (parent != null) {
			boolean found = false;
			for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
				if (!found && parent.getChildNodes().item(i) == this) {
					found = true;
				} else if (found) {
					return parent.getChildNodes().item(i);
				}
			}
		} else if (getOwnerDocument() != null) {
			boolean found = false;
			for (int i = 0; i < getOwnerDocument().getChildNodes().getLength(); i++) {
				if (!found
						&& getOwnerDocument().getChildNodes().item(i) == this) {
					found = true;
				} else if (found) {
					return getOwnerDocument().getChildNodes().item(i);
				}
			}
		}

		return null;
	}

	public Node getPreviousSibling() {
		if (parent != null) {
			boolean found = false;
			for (int i = (parent.getChildNodes().getLength() - 1); i >= 0; i--) {
				if (!found && parent.getChildNodes().item(i) == this) {
					found = true;
				} else if (found) {
					return parent.getChildNodes().item(i);
				}
			}
		} else if (getOwnerDocument() != null) {
			boolean found = false;
			for (int i = getOwnerDocument().getChildNodes().getLength() - 1; i >= 0; i--) {
				if (!found
						&& getOwnerDocument().getChildNodes().item(i) == this) {
					found = true;
				} else if (found) {
					return getOwnerDocument().getChildNodes().item(i);
				}
			}
		}

		return null;
	}

	/**
	 * Sets the parent of this node. The node is removed from its parent node.
	 * 
	 * @param parent
	 * @param removeFromChildren
	 */
	void setParent(Attribute parent) {
		if (this == parent) {
			throw new DOMException("HATTRIBUTE.INCORRECT.RECUSIVE.USAGE");
		}

		this.parent = parent;
	}

	void connect(Entry doc, Attribute parent) {
		if (parent == null && doc == null) {
			throw new IllegalArgumentException();
		}

		if (this.parent != parent) {
			setParent(parent);
		}
	}

	void disconnect() {
		if (this.parent != null) {
			setParent(null);
		}
	}
}
