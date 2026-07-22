/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.UserDataHandler;

import com.ibm.di.exceptions.DOMException;

/**
 * This class is just a place holder for all the methods that are not relevant
 * to the IBM Tivoli Directory Integrator's DOM implementation and that are not
 * implemented. This class is extended by the Entry class.
 * 
 * @since 7.0
 */
public abstract class DocImpl implements Document {

	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Comment createComment(String data) {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public DocumentFragment createDocumentFragment() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public EntityReference createEntityReference(String name)
			throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public ProcessingInstruction createProcessingInstruction(String target,
			String data) throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public DocumentType getDoctype() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getDocumentURI() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public DOMConfiguration getDomConfig() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Element getElementById(String elementId) {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public DOMImplementation getImplementation() {
		return null;
	}

	/**
	 * @return null
	 */
	public String getInputEncoding() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return false
	 */
	public boolean getStrictErrorChecking() {
		return false;
	}

	/**
	 * return null
	 */
	public String getXmlEncoding() {
		return null;
	}

	/**
	 * @return true
	 */
	public boolean getXmlStandalone() {
		return true;
	}

	/**
	 * @return "1.0"
	 */
	public String getXmlVersion() {
		return "1.0";
	}

	/**
	 * not implemented
	 */
	public void normalizeDocument() {
	}

	/**
	 * not implemented
	 */
	public void setStrictErrorChecking(boolean strictErrorChecking) {
	}

	/**
	 * Does nothig
	 */
	public void setXmlEncoding(String value) {
	}

	/**
	 * Does not change anything.
	 */
	public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
	}

	/**
	 * Does nothing.
	 */
	public void setXmlVersion(String xmlVersion) throws DOMException {
	}

	/**
	 * not implemented
	 * 
	 * @return 0
	 */
	public short compareDocumentPosition(Node other) throws DOMException {
		return 0;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public NamedNodeMap getAttributes() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getBaseURI() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Object getFeature(String feature, String version) {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getLocalName() {
		return getNodeName();
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getNamespaceURI() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Node getNextSibling() {
		return null;
	}

	/**
	 * @return "#document"
	 */
	public String getNodeName() {
		return "#document";
	}

	/**
	 * @return Node#DOCUMENT_NODE
	 */
	public short getNodeType() {
		return Node.DOCUMENT_NODE;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getNodeValue() throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Document getOwnerDocument() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Attribute getParentNode() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getPrefix() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Node getPreviousSibling() {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String getTextContent() throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Object getUserData(String key) {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public boolean hasAttributes() {
		return false;
	}

	/**
	 * not implemented
	 * 
	 * @return false
	 */
	public boolean isDefaultNamespace(String namespaceURI) {
		return false;
	}

	/**
	 * not implemented
	 * 
	 * @return false
	 */
	public boolean isSupported(String feature, String version) {
		return false;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String lookupNamespaceURI(String prefix) {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public String lookupPrefix(String namespaceURI) {
		return null;
	}

	/**
	 * not implemented
	 */
	public void setNodeValue(String nodeValue) throws DOMException {
	}

	/**
	 * not implemented
	 */
	public void setPrefix(String prefix) throws DOMException {
	}

	/**
	 * not implemented
	 */
	public void setTextContent(String textContent) throws DOMException {
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		return null;
	}

	/**
	 * not supported
	 * 
	 * @return null
	 */
	public Node importNode(Node importedNode, boolean deep) throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 */
	public void setDocumentURI(String documentURI) {
	}

	public boolean isSameNode(Node other) {
		return this == other;
	}

	public void normalize() {
	}
}
