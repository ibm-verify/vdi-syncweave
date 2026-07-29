/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.user;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Iterates all user names stored in R/3. Instances will be instantiated during
 * {@link Connector#selectEntries()} and cached for use in
 * {@link Connector#getNextEntry()}.
 * 
 * This iterator does <B>NOT</B> support <a
 * href="%j2se.api.doc.root%/java/util/Iterator.html#remove()">java.util.Iterator.remove()</a>
 * 
 * @see <a href="%j2se.api.doc.root%/java/util/Iterator.html">java.util.Iterator</a>
 */
final class R3UsernameIterator implements Iterator {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * The sapUserName element tag name expected to present in selectEnties RFC
	 * response.
	 */
	static final String SAP_USER_NAME_TAG_NAME = "sapUserName";

	final Iterator iter;

	/**
	 * Create a new iterator.
	 * 
	 * @param doc
	 *            The XML response from the select entries RFC call. The
	 *            iterator will init itself by retrieving all the Text and CDATA
	 *            Sections from all "sapUserName" elements.
	 */
	R3UsernameIterator(Document doc) {
		super();
		Element root = doc.getDocumentElement();
		NodeList nl = root
				.getElementsByTagName(R3UsernameIterator.SAP_USER_NAME_TAG_NAME);
		List usernamesList = new LinkedList();
		for (int i = 0; i < nl.getLength(); ++i) {
			Element e = (Element) nl.item(i);
			NodeList children = e.getChildNodes();
			StringBuffer userName = new StringBuffer();
			for (int j = 0; j < children.getLength(); ++j) {
				Node n = children.item(j);
				if ((n.getNodeType() == Node.TEXT_NODE)
						|| (n.getNodeType() == Node.CDATA_SECTION_NODE)) {
					userName.append(n.getNodeValue());
				}
			}
			usernamesList.add(userName.toString());
		}

		iter = usernamesList.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return iter.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return iter.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
