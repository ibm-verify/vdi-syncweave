/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

public class MissingAttributeException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String missingAttributeName;

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            The message
	 * @param name
	 *            The name of the missing attribute
	 */
	public MissingAttributeException(String msg, String name) {
		super(msg);
		this.missingAttributeName = name;
	}

	/**
	 * Return the name of the missing Attribute
	 * 
	 * @return The name of the missing Attribute
	 */
	public String getAttributeName() {
		return missingAttributeName;
	}
}
