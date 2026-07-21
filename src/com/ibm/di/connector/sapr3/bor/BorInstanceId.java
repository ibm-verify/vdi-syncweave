/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Representation of a BOR object key fields. Since BOR objects may have
 * mulitple keys, this class projects a name value pair representation.
 */
class BorInstanceId {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final Map keyValPairs;

	/**
	 * Create a new instance.
	 * 
	 * @param ele
	 *            The XML represenatation of BOR instance key information. The
	 *            XML must conform to the following: &ltsapBorObjIdentifier&gt
	 *            &ltkeyName_1&gtvalue_1&lt/keyName_1&gt
	 *            &ltkeyName_N&gtvalue_N&lt/keyName_N1&gt
	 *            &lt/sapBorObjIdentifier&gt
	 * @throws IllegalArgumentException
	 *             if <code>ele</code> is <code>null</code>.
	 */
	BorInstanceId(Element ele) {
		super();

		if (ele == null) {
			throw new IllegalArgumentException();
		}
		keyValPairs = Collections.synchronizedMap(new HashMap());
		initialize(ele);
	}

	private void initialize(Element ele) {
		NodeList nl = ele.getElementsByTagName("*");
		for (int i = 0; i < nl.getLength(); ++i) {
			Element keyEle = (Element) nl.item(i);
			String name = keyEle.getTagName();
			String val = XmlHelper.textValue(keyEle);
			addKey(name, val);
		}
	}

	/**
	 * Add a key value.
	 * 
	 * @param key
	 *            The key name.
	 * @param val
	 *            The value.
	 * @throws IllegalArgumentException
	 *             if either parameter is <code>null</code>.
	 */
	void addKey(String key, String val) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		if (val == null) {
			throw new IllegalArgumentException();
		}
		keyValPairs.put(key, val);
	}

	/**
	 * Get the value associated with the key.
	 * 
	 * @param key
	 *            The key name
	 * @return The value associated with the key, or <code>null</code> if key
	 *         does not map to a value.
	 * @throws IllegalArgumentException
	 *             if key is <code>null</code>.
	 */
	String getValue(String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		return ((String) keyValPairs.get(key));
	}

	/**
	 * Get the key names as a Set.
	 * 
	 * @return The Set of Key names. Names are Strings.
	 */
	Set keySet() {
		return keyValPairs.keySet();
	}

}
