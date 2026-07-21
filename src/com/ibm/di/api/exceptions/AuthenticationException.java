/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.exceptions;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.server.ResourceHash;

/**
 * An exception object thrown when the authentication of a user against a
 * specific system fails.
 */
public class AuthenticationException extends DIException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 5252257385808266515L;

	/**
	 * the description of the error the object was created with.
	 */
	private String mErrorDescription = null;

	/**
	 * the code of the error the object was created with.
	 */
	private Integer mErrorCode = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor of the exception that creates the exception with a
	 * predefined cause message.
	 */
	public AuthenticationException() {
		super(sResHash.getString("SEVER.API.USER.NOT.AUTHENTICATED"));
	}

	/**
	 * This constructor creates an instance of this type with a specific error
	 * attributes.
	 * 
	 * @param aMessage
	 *            the custom cause message
	 * @param aErrorDescription
	 *            the description of the occurred error.
	 * @param aErrorCode
	 *            a numeric value used as an error code.
	 */
	public AuthenticationException(String aMessage, String aErrorDescription,
			Object aErrorCode) {
		super(aMessage);
		mErrorDescription = aErrorDescription;
		if (aErrorCode != null) {
			try {
				mErrorCode = new Integer(aErrorCode.toString());
			} catch (NumberFormatException e) {
				APIEngine
						.logError(sResHash
								.getString(
										"SEVER.API.AUTHENTICATIONEXCEPTION.UNABLE.TO.PARSE.ERRORCODE",
										aErrorCode));
				mErrorCode = null;
			}
		}
	}

	/**
	 * Retrieves error description.
	 * 
	 * @return the description of the error the object was created with.
	 */
	public String getErrorDescription() {
		return mErrorDescription;
	}

	/**
	 * Retrieves error code.
	 * 
	 * @return the code of the error the object was created with.
	 */
	public Integer getErrorCode() {
		return mErrorCode;
	}

	/**
	 * Converts the exception to string.
	 * 
	 * @return the string representation of the thrown exception.
	 */
	public String toString() {
		String exception = super.toString();
		exception = exception + " , ErrorDescription = " + mErrorDescription
				+ " , ErrorCode = " + mErrorCode;
		return exception;
	}
}
