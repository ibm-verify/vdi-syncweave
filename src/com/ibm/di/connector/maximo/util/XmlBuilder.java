/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple XML content generator.
 * <p>
 * This class contains simple methods to create XML content.
 * </p>
 * <p>
 * <b>Note:</b> no XML validation is made.
 * </p>
 * 
 * @since 7.1
 */
public final class XmlBuilder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final class TreeNode {

		private final Map<String, String> attributes;

		private boolean cdata;

		private final Map<String, LinkedList<TreeNode>> childNodes;

		private final String name;

		private String value;

		/**
		 * Constructs a {@link TreeNode}.
		 * 
		 * @param name
		 *            name of the node
		 */
		public TreeNode(final String name) {

			this.name = name;
			this.attributes = new TreeMap<String, String>();
			this.childNodes = new TreeMap<String, LinkedList<TreeNode>>();
		}

		/**
		 * Adds a new node to the tree.
		 * 
		 * @param nodeName
		 *            name of the child node
		 * @return the new child node
		 */
		public TreeNode addChild(final String nodeName) {

			return addChild(nodeName, null, false);
		}

		/**
		 * Adds a path of nodes in this tree.
		 * 
		 * @param path
		 *            array of strings which contains the names of the nodes
		 *            that make the path
		 * @return the last node of the path
		 */
		public TreeNode addPath(final String[] path) {

			return addPath(path, null, false);
		}

		/**
		 * Returns an unmodifiable copy of the attribute map of this node.
		 * 
		 * @return unmodifiable copy of the attribute map of this node
		 */
		@SuppressWarnings("unchecked")
		public Map<String, String> getAttributes() {

			if (attributes.isEmpty()) {
				return Collections.EMPTY_MAP;
			}
			return Collections.unmodifiableMap(attributes);
		}

		/**
		 * Returns an unmodifiable copy of the child nodes of this node.
		 * 
		 * @return an unmodifiable copy of the child nodes of this node
		 */
		@SuppressWarnings("unchecked")
		public List<TreeNode> getChildList() {

			if (childNodes.isEmpty()) {
				return Collections.EMPTY_LIST;
			}

			final LinkedList<TreeNode> result = new LinkedList<TreeNode>();
			for (final LinkedList<TreeNode> nodes : childNodes.values()) {
				result.addAll(nodes);
			}

			return result;
		}

		/**
		 * Returns the last node of the specified path.
		 * 
		 * @param path
		 *            array of strings that specifies the path to the node
		 * @return last node of the specified path
		 */
		public TreeNode getLast(final String[] path) {

			TreeNode currentNode = this;

			for (int i = 0; i < path.length; i++) {
				if (!currentNode.contains(path[i])) {
					return null;
				}
				currentNode = currentNode.getLast(path[i]);
			}

			return currentNode;
		}

		/**
		 * Returns the name of this node.
		 * 
		 * @return the name of this node
		 */
		public String getName() {

			return name;
		}

		/**
		 * Returns the value associated with this node.
		 * 
		 * @return the value associated with this node
		 */
		public String getValue() {

			return value;
		}

		/**
		 * Indicates if the value associated with this node should be inside a
		 * CDATA block.
		 * 
		 * @return <code>true</code> if the value associated with this node
		 *         should be inside a CDATA block, otherwise <code>false</code>
		 */
		public boolean isCdata() {

			return cdata;
		}

		/**
		 * Indicates if this node has no child nodes and node value associated.
		 * 
		 * @return <code>true</code> if this node has no child nodes and node
		 *         value associated, otherwise <code>false</code>
		 */
		public boolean isEmpty() {

			return isLeaf() && value == null;
		}

		/**
		 * Indicates if this node has no child nodes.
		 * 
		 * @return <code>true</code> if this node has no child nodes, otherwise
		 *         <code>false</code>
		 */
		public boolean isLeaf() {

			return childNodes.isEmpty();
		}

		/**
		 * Defines an attribute.
		 * 
		 * @param name
		 *            name of the attribute
		 * @param value
		 *            value of the attribute
		 */
		public void setAttribute(final String name, final String value) {

			attributes.put(name, value);
		}

		private TreeNode addChild(final String nodeName, final String nodeValue, final boolean cdata) {

			if (!childNodes.containsKey(nodeName)) {
				childNodes.put(nodeName, new LinkedList<TreeNode>());
			}

			final TreeNode newNode = new TreeNode(nodeName);
			newNode.setValue(nodeValue, cdata);
			childNodes.get(nodeName).addLast(newNode);
			return newNode;
		}

		private TreeNode addPath(final String[] path, final String value, final boolean cdata) {

			TreeNode currentNode = this;

			for (int i = 0; i < path.length - 1; i++) {
				if (currentNode.contains(path[i])) {
					currentNode = currentNode.getLast(path[i]);
				} else {
					currentNode = currentNode.addChild(path[i]);
				}
			}

			currentNode = currentNode.addChild(path[path.length - 1]);
			currentNode.setValue(value, cdata);
			return currentNode;
		}

		private boolean contains(final String nodeName) {

			return childNodes.containsKey(nodeName);
		}

		private TreeNode getLast(final String nodeName) {

			if (!contains(nodeName)) {
				return null;
			}

			return childNodes.get(nodeName).getLast();
		}

		private void setValue(final String value, final boolean cdata) {

			this.value = value;
			this.cdata = cdata;
		}
	}

	private static String extractAttribute(final String fullName) {

		final String[] elements = fullName.split("#");
		if (elements.length > 1) {
			return elements[1];
		}
		return null;
	}

	private static String[] extractTags(final String fullName) {

		final String[] elements = fullName.split("#");
		return elements[0].split("@");
	}

	private static void printAttributes(final StringBuilder sb, final TreeNode node) {

		final Map<String, String> attributes = node.getAttributes();

		for (final Map.Entry<String, String> e : attributes.entrySet()) {
			sb.append(' ');
			sb.append(e.getKey());
			sb.append('=');
			sb.append('"');
			sb.append(replaceWithEntityRefs(e.getValue()));
			sb.append('"');
		}
	}

	private static StringBuilder printNode(final StringBuilder sb, final TreeNode node) {

		sb.append('<');
		sb.append(node.getName());
		printAttributes(sb, node);

		if (node.isEmpty()) {
			return sb.append('/').append('>');
		}

		sb.append('>');

		if (node.isLeaf()) {
			if (node.isCdata()) {
				surroundWithCdata(sb, node.getValue());
			} else {
				sb.append(replaceWithEntityRefs(node.getValue()));
			}
		} else {
			final List<TreeNode> nodes = node.getChildList();
			for (final TreeNode child : nodes) {
				printNode(sb, child);
			}
		}

		sb.append('<').append('/').append(node.getName()).append('>');

		return sb;
	}

	private static String replaceWithEntityRefs(final String value) {

		return value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll(
				"\"", "&quot;");
	}

	private static void surroundWithCdata(final StringBuilder sb, final String value) {

		sb.append("<![CDATA[").append(value).append("]]>");
	}

	private final TreeNode root;

	/**
	 * Constructs a new {@link XmlBuilder}.
	 * 
	 * @param rootName
	 *            name of the root node
	 */
	public XmlBuilder(final String rootName) {

		this.root = new TreeNode(rootName);
	}

	/**
	 * Defines an XML attribute and its value.
	 * <p>
	 * The <code>fullName</code> parameter must comply the following syntax:
	 * </p>
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * &lt;root node&gt;@&lt;child node 1&gt;@&lt;child node 2&gt;@&lt;child node N&gt;#&lt;new attribute&gt;
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param fullName
	 *            the complete path from the root node until the attribute name
	 * @param value
	 *            the attribute's value
	 */
	public void setAttribute(final String fullName, final String value) {

		String attributeName = extractAttribute(fullName);
		String[] tagElements;

		if (attributeName == null) {
			attributeName = fullName;
			tagElements = new String[] {};
		} else {
			tagElements = extractTags(fullName);
		}

		TreeNode node;

		if (tagElements.length == 0) {
			node = root;
		} else {
			node = root.getLast(tagElements);
		}

		if (node == null) {
			node = root.addPath(tagElements);
		}

		node.setAttribute(attributeName, value);
	}

	/**
	 * Defines an XML tag and its content (without CDATA block).
	 * 
	 * @param fullName
	 *            the complete path from the root node until the tag name
	 * @param value
	 *            the tag's content
	 * @see #tag(String, String, boolean)
	 */
	public void tag(final String fullName, final String value) {

		tag(fullName, value, false);
	}

	/**
	 * Defines an XML tag and its content.
	 * <p>
	 * The <code>fullName</code> parameter must comply the following syntax:
	 * </p>
	 * 
	 * <pre>
	 * &lt;code&gt;
	 * &lt;root node&gt;@&lt;child node 1&gt;@&lt;child node 2&gt;@&lt;child node N&gt;@&lt;new tag&gt;
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param fullName
	 *            the complete path from the root node until the tag name
	 * @param value
	 *            the tag's content
	 * @param cdata
	 *            <code>true</code> if <code>value</code> must be enclosed by a
	 *            CDATA block, otherwise <code>false</code>
	 */
	public void tag(final String fullName, final String value, final boolean cdata) {

		final String[] tagElements = extractTags(fullName);

		root.addPath(tagElements, value, cdata);
	}

	/**
	 * Returns the XML content as {@link String}.
	 * 
	 * @return the XML content as {@link String}
	 */
	@Override
	public String toString() {

		return printNode(new StringBuilder(), root).toString();
	}
}
