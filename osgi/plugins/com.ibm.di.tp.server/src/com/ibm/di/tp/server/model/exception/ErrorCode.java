/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.exception;

/**
 * This enum contains the error codes as defined by the SCMP specification. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public enum ErrorCode {

	// --- Generic SCMP/APP/REST-related errors ---
	/**
	 * Default SCMP data error (couldn't be narrowed further) <br />
	 * details: (none)
	 */
	GENERIC_UNKNOWN(100000),

	/**
	 * Missing required atom:link <br />
	 * details: rel - relationship name of expected link
	 */
	GENERIC_MISSING_LINK(100001),

	/**
	 * Missing required scmp:data child element <br />
	 * details: qname - missing element qname
	 */
	GENERIC_MISSING_DATA(100002),

	/**
	 * Invalid atom:entry in POST/PUT operation (e.g. parse error) <br />
	 * details: (none)
	 */
	GENERIC_INVALID_ENTRY(100003),

	/**
	 * Invalid value for scmp:data child element <br />
	 * details: qname - invalid element qname; value - invalid supplied value
	 */
	GENERIC_INVALID_DATA(100004),

	/**
	 * Missing required atom:category <br />
	 * details: scheme - expected scheme name
	 */
	GENERIC_MISSING_CATEGORY(100005),

	/**
	 * Invalid atom:category value <br />
	 * details: scheme - invalid category scheme name; term - invalid supplied
	 * term
	 */
	GENERIC_INVALID_CATEGORY(100006),

	/**
	 * Too many atom:link for given relationship (e.g. multiple "to-group"
	 * links)<br />
	 * details: rel - relationship name of extra link
	 */
	GENERIC_TOO_MANY_LINKS(100007),

	/**
	 * Too many atom:category values for scheme (e.g. multiple "aspect"
	 * categories)<br />
	 * details: scheme - overpopulated scheme nam
	 */
	GENERIC_TOO_MANY_CATEGORIES(100008),

	// --- WSDL pass-by-value errors ---
	/**
	 * Default WSDL pass-by-value error (couldn't be narrowed further) <br />
	 * details: (none)
	 */
	WSDL_VAL_UNKNOWN(200000),

	/**
	 * POSTed WSDL archive contained multiple ports <br />
	 * details: (none)
	 */
	WSDL_VAL_MULTIPLE_PORTS(200001),

	/**
	 * POSTed WSDL archive contained zero ports <br />
	 * details: (none)
	 */
	WSDL_VAL_NO_PORTS(200002),

	/**
	 * Parse error in POSTed WSDL archive <br />
	 * details: file - file name (not archive path); namespace - file namespace;
	 * version - file version
	 */
	WSDL_VAL_PARSE(200003),

	/**
	 * Missing document in POSTed WSDL archive (neither in archive nor already
	 * in registry)<br />
	 * details: file - file name (not archive path); namespace - file namespace;
	 * version - file version
	 */
	WSDL_VAL_MISSING_DOCUMENT(200004),

	/**
	 * Unable to find file to build for WSDL archive (e.g. can no longer read
	 * imported XSD from registry)<br />
	 * details: file - file name (not archive path); namespace - file namespace;
	 * version - file version
	 */
	WSDL_VAL_MISSING_BUILD_FILE(200005),

	// --- WSDL pass-by-reference errors ---
	/**
	 * Default WSDL pass-by-reference error (couldn't be narrowed further) <br />
	 * details: (none)
	 */
	WSDL_REF_UNKNOWN(210000),

	/**
	 * Network exception reading WSDL from URL <br />
	 * details: url - URL that could not be read
	 */
	WSDL_REF_NETWORK(210001),

	/**
	 * Unexpected HTTP status code reading WSDL from supplied URL <br />
	 * details: url - URL that could not be read; status - received HTTP status
	 * code
	 */
	WSDL_REF_HTTP_STATUS(210002),

	/**
	 * WSDL parse error (including imports) <br />
	 * details: url - URL that could not be parsed
	 */
	WSDL_REF_PARSE(210003),

	/**
	 * Named WSDL port not found <br />
	 * details: port - missing WSDL port name
	 */
	WSDL_REF_PORT_NOT_FOUND(210004),

	// --- Federation server errors ---
	/**
	 * Default federation-specific error (couldn't be narrowed further) <br />
	 * details: (none)
	 */
	FEDERATION_UNKNOWN(300000),

	// --- Domain server errors ---
	/**
	 * Default domain-specific error (couldn't be narrowed further) <br />
	 * details: (none)
	 */
	DOMAIN_UNKNOWN(310000),

	// --- Registry server errors ---
	/**
	 * Default registry-specific error (couldn't be narrowed further) <br />
	 * details: file - file name that could not be deleted; used-by - file name
	 * that uses file that couldn't be deleted
	 */
	REGISTRY_UNKNOWN(320000),

	/**
	 * File in use and can't be deleted <br />
	 * details: (none)
	 */
	REGISTRY_CANNOT_DELETE(320001),

	// --- Connectivity server errors ---
	/**
	 * Default connectivity-provider-specific error (couldn't be narrowed
	 * further)<br />
	 * details: (none)
	 */
	CONNECTIVITY_UNKNOWN(330000);

	private final int code;

	private ErrorCode(int code) {
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
}
