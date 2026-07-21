/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class CharacterDataImpl extends NodeImpl implements
		CharacterData {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Serialization Version ID
	 */
	private static final long serialVersionUID = -283349571852699498L;

	/**
	 * Gathers all the CharacterData (Text or CDATA) objects that are siblings
	 * to this node and return the text data separated by the "\r\n" string.
	 * 
	 * @return the gathered data of all the Text or CDATA objects on the current
	 *         level. If this node does not belong to a document and has no
	 *         parent then its own text data is returned.
	 */
	public String getWholeText() {

		if (parent != null) {
			StringBuilder result = new StringBuilder();
			NodeList children = parent.getChildNodes();

			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == this.getNodeType()) {
					// put CRLF char for separation
					result.append(children.item(i).getNodeValue() + "\r\n");
				}
			}

			// at least this.data will be inside the result
			return result.toString();
		}

		// if we can not find owner then return the data that this holds.
		return getData();
	}

	/**
	 * @param moreData
	 *            - the data that will be appended to the current data
	 */
	public void appendData(String moreData) throws DOMException {
		String thisData = getData();
		setData(thisData == null ? moreData : thisData + moreData);

	}

	/**
	 * @param offset
	 *            - the position to start deleting from.
	 * @param count
	 *            - the number of characters to delete.
	 */
	public void deleteData(int offset, int count) throws DOMException {
		String thisData = getData();
		if (thisData != null) {
			setData(thisData.substring(0, offset)
					+ thisData.substring(offset + count));
		}
	}

	/**
	 * @return the length of the text data of this CharacterData object, if the
	 *         data is null then 0 is returned;
	 */
	public int getLength() {
		String thisData = getData();
		return thisData != null ? thisData.length() : 0;
	}

	/**
	 * @param offset
	 *            - the offset from the start at which to begin the inserting.
	 * @param data
	 *            - the data to be inserted.
	 */
	public void insertData(int offset, String data) throws DOMException {
		String thisData = getData();
		if (data != null) {
			setData(thisData.substring(0, offset) + data
					+ thisData.substring(offset));
		}
	}

	/**
	 * @param offset
	 *            - the offset from the start at which to begin the replacing.
	 * @param count
	 *            - the number of characters to be removed.z
	 * @param data
	 *            - the data to be inserted.
	 */
	public void replaceData(int offset, int count, String data)
			throws DOMException {
		String thisData = getData();
		if (data != null) {
			setData(thisData.substring(0, offset) + data
					+ thisData.substring(offset + count));
		}
	}

	/**
	 * @param offset
	 *            - the offset from the start at which to begin retrieving.
	 * @param count
	 *            - the number of chars to return
	 * @return return the part of the data identified by the parameters, or null
	 *         if this CharacterData object has no data.
	 */
	public String substringData(int offset, int count) throws DOMException {
		String thisData = getData();
		return thisData != null ? thisData.substring(offset, offset + count)
				: null;
	}

	public String getNodeValue() throws DOMException {
		return getData();
	}

	public void setNodeValue(String nodeValue) throws DOMException {
		setData(nodeValue);
	}

	/**
	 * not implemented
	 * 
	 * @return false
	 */
	public boolean isElementContentWhitespace() {
		return false;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Text replaceWholeText(String arg0) throws DOMException {
		return null;
	}

	/**
	 * not implemented
	 * 
	 * @return null
	 */
	public Text splitText(int arg0) throws DOMException {
		return null;
	}

	@Override
	public String toString() {
		return getNodeValue();
	}
}
