/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.itregistry;

import com.ibm.tivoli.namereconciliation.guid.Guid;

/**
 * A wrapper class used to contain IT registry GUIDs. This class offers no
 * accessor methods as users are not expected to handle its contents on their
 * own. Instead they should use this wrapper class in all situations.
 * 
 * Also, instances of this class cannot be serialized, because storing of GUIDs
 * is not supported.
 */
public class ConfigurationItemId {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The value of the IT registry GUID.
	 */
	private Guid value;

	/**
	 * Creates a wrapper for the identification element.
	 * 
	 * @param value
	 *            string representation of the used GUID.
	 */
	public ConfigurationItemId(Guid value) {
		this.value = value;
	}

	/**
	 * Returns the actual ID/GUID.
	 * 
	 * @return an IT registry GUID.
	 */
	@SuppressWarnings("unused")
	private Guid getValue() {
		return value;
	}

}
