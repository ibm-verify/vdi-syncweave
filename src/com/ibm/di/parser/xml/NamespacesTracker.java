/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

import java.util.ArrayList;

/**
 * 
 * This class is used for tracking the declared prefixes in the XML. It is used
 * as a stack which increases its size each time the XML Parser2 enters in a
 * child element. It decrease its size each time the XML Parser2 exits the child
 * element and gets back to the child parent context.
 * 
 * THIS CLASS IS FOR INTERNAL USAGE ONLY! MAY CHANGE IN THE FUTURE!
 * 
 * @since 7.0
 */
public class NamespacesTracker {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Holds the information for the declared prefix/namespace values.
	 */
	private ArrayList<ArrayList<String[]>> holder;

	/**
	 * Default constructor.
	 */
	NamespacesTracker() {
		holder = new ArrayList<ArrayList<String[]>>();
		holder.add(new ArrayList<String[]>(0));
	}

	/**
	 * Pushes another holder in the stack
	 */
	void increaseLevel() {
		holder.add(new ArrayList<String[]>(0));
	}

	/**
	 * Pops the head of the stack
	 */
	void decreaseLevel() {
		if (holder.size() > 0) {
			holder.remove(holder.size() - 1);
		}
	}

	/**
	 * Check if the prefix:namespace pair is defined for the current context
	 * 
	 * @param prefix
	 *            : String, the prefix
	 * @param namespaceURI
	 *            : String , the namespace
	 * @return true , if the prefix is defined and it is mapped to the same NS
	 */
	boolean contains(String prefix, String namespaceURI) {
		for (int i = holder.size(); --i >= 0;) {

			ArrayList<String[]> currLevelList = holder.get(i);
			for (String[] map : currLevelList) {

				if ((map[0] != null && prefix != null && map[0].equals(prefix))
						|| map[0] == prefix)
					// check NS
					if ((map[1] != null && namespaceURI != null && map[1]
							.equals(namespaceURI))
							|| map[1] == namespaceURI) {
						// the prefix is defined and it is mapped to the same NS
						return true;
					} else {
						// defined but points to different NS
						return false;
					}
			}
		}
		return false;
	}

	/**
	 * Adds the prefix:namespace pair on the top of the stack. this means that
	 * the namespace is declared for that element's level
	 * 
	 * @param prefix
	 *            String , the prefix
	 * @param namespaceURI
	 *            , String the namespace
	 */
	void addPrefix(String prefix, String namespaceURI) {
		if (holder.size() > 0) {
			holder.get(holder.size() - 1).add(
					new String[] { prefix, namespaceURI });
		}
	}

	/**
	 * Gets the closest namespace declared with the specified prefix
	 * 
	 * @param prefix
	 *            String , the prefix.
	 * @return namespace.
	 */
	String getNamespace(String prefix) {
		for (int i = holder.size(); --i >= 0;) {
			ArrayList<String[]> currLevelList = holder.get(i);

			for (String[] map : currLevelList) {

				if (((map[0] != null && prefix != null && map[0].equals(prefix)) || map[0] == prefix)) {
					return map[1];
				}
			}
		}
		return null;
	}
}
