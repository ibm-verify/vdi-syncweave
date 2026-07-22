/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import com.ibm.di.server.ResourceHash;

/**
 * This is the type of exception that is thrown when the authenticated user does
 * not have the required authority to perform the specific operation.
 */
public class AuthorizationException extends DIException {

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -1504306062628342605L;

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * The default constructor for this object.
	 */
	public AuthorizationException() {
		super(
				sResHash
						.getString("SEVER.API.NOT.AUTHORIZED.TO.PERFORM.THIS.OPERATION"));
	}

	/**
	 * This is the constructor that provides a way to construct the object with
	 * a specific error message.
	 * 
	 * @param aMessage
	 *            the message to be used in the exception.
	 */
	public AuthorizationException(String aMessage) {
		super(aMessage);
	}
}
