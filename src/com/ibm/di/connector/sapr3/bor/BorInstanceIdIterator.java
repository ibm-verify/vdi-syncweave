/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>
 * For a given BOR object type, iterates through all instance identifiers.
 * Instances of the class will be instantiated during
 * {@link Connector#selectEntries()} and cached for use in
 * {@link Connector#getNextEntry()}.
 * </p>
 * <p>
 * The type returned from {#link next()} will be {@link BorInstanceId}.
 * </p>
 * <p>
 * This iterator does <B>NOT</B> support <a
 * href="%j2se.api.doc.root%/java/util/Iterator.html#remove()">java.util.Iterator.remove()</a>
 * </p>
 * 
 * @see <a href="%j2se.api.doc.root%/java/util/Iterator.html">java.util.Iterator</a>
 */
final class BorInstanceIdIterator implements Iterator {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * The sapBorObjIndentifier element tag name expected to present in
	 * selectEnties RFC response.
	 */
	static final String SAP_BOR_ID_TAG_NAME = "sapBorObjIdentifier";

	final Iterator iter;

	/**
	 * Create a new iterator.
	 * 
	 * @param doc
	 *            The XML response from the select entries RFC call. The
	 *            iterator will init itself by retrieving all the Text and CDATA
	 *            Sections from all "keyfield" elements.
	 */
	BorInstanceIdIterator(Document doc) {
		super();
		Element root = doc.getDocumentElement();
		NodeList nl = root
				.getElementsByTagName(BorInstanceIdIterator.SAP_BOR_ID_TAG_NAME);
		List idList = new LinkedList();
		for (int i = 0; i < nl.getLength(); ++i) {
			Element e = (Element) nl.item(i);
			BorInstanceId borId = new BorInstanceId(e);
			idList.add(borId);
		}

		iter = idList.iterator();
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
