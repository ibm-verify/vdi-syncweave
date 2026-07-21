/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

/**
 * Store the error codes in a separate enumerated type class, ensuring that only
 * these error codes can be used when creating a specific SapR3RfcFCException.
 */
public final class SapR3RfcFCErrorCodes {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static int nextCode;

	private final int code;

	/* make sure everything is done in this class */
	private SapR3RfcFCErrorCodes() {
		this.code = nextCode++;
	}

	/**
	 * Get the type ID.
	 * 
	 * @return The ID.
	 */
	public int getCode() {
		return code;
	}

	/** unable to create XML DOM Document. */
	public static final SapR3RfcFCErrorCodes BAD_DOM_DOCUMENT = new SapR3RfcFCErrorCodes();

	/** problem establishing a connection to the SAP system. */
	public static final SapR3RfcFCErrorCodes CONNECTION_ESTABLISHMENT = new SapR3RfcFCErrorCodes();

	/** problem establishing a connection to the SAP system. */
	public static final SapR3RfcFCErrorCodes CONNECTION_DROPPED = new SapR3RfcFCErrorCodes();

	/** problem when disconnecting from the SAP system. */
	public static final SapR3RfcFCErrorCodes DISCONNECTION = new SapR3RfcFCErrorCodes();

	/** connection pool already exists. */
	public static final SapR3RfcFCErrorCodes CONNECTION_POOL_EXISTS = new SapR3RfcFCErrorCodes();

	/** DOM Document parsing error. */
	public static final SapR3RfcFCErrorCodes DOM_DOCUMENT_PARSER = new SapR3RfcFCErrorCodes();

	/** Problem parsing the DOM Document. */
	public static final SapR3RfcFCErrorCodes DOM_DOCUMENT_PARSING = new SapR3RfcFCErrorCodes();

	/** Problem reading the DOM Document. */
	public static final SapR3RfcFCErrorCodes DOM_DOCUMENT_READ = new SapR3RfcFCErrorCodes();

	/** Unable to execute the specified RFC. */
	public static final SapR3RfcFCErrorCodes RFC_FUNCTION_EXECUTION = new SapR3RfcFCErrorCodes();

	/** Unable to setup the remote function call paramters. */
	public static final SapR3RfcFCErrorCodes RFC_PARAM_PREPARE = new SapR3RfcFCErrorCodes();
}
