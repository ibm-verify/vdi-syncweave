/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser.xml;

/**
 * 
 * The XML Parser don't need to track the namespaces for certain cases so we are
 * using this class to avoid doing any namespace tracking.
 * 
 * THIS CLASS IS FOR INTERNAL USAGE ONLY! MAY CHANGE IN THE FUTURE!
 * 
 * @since 7.0
 */
public class NullNamespacesTracker extends NamespacesTracker {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Creates a new instance.
	 */
	public NullNamespacesTracker() {
	}

	/**
	 * Does nothing
	 * 
	 * @param prefix
	 * @param namespaceURI
	 */
	@Override
	void addPrefix(String prefix, String namespaceURI) {
	}

	/**
	 * @param prefix
	 * @param namespaceURI
	 * 
	 * @return false
	 */
	@Override
	boolean contains(String prefix, String namespaceURI) {
		return false;
	}

	/**
	 * Does nothing
	 */
	@Override
	void decreaseLevel() {
	}

	/**
	 * @param prefix
	 * @return null
	 */
	@Override
	String getNamespace(String prefix) {
		return null;
	}

	/**
	 * Does nothing
	 */
	@Override
	void increaseLevel() {
	}

}
