/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

/**
 * Decode ABAP names into their true ABAP value. Typically done when processing
 * a request.
 */
/*
 * @modelguid {4F703C13-0BE2-4A91-A2C8-B989DA36BCDE}
 */
interface AbapIfrDecoder {

	/**
	 * Convert a XML Name to ABAP Name. Rules are: - characters from A-Z, a-z,
	 * or '_' need no mapping - character from 0-9 or '.' need no mapping - '_-'
	 * is mapped to '/' - '_--hex(c)' mapped to ASCII character c where hex(c)
	 * is the 2-character hexadecimal represenation of ASCII code (e.g., _--26
	 * is represented converted to &)
	 * 
	 * @param encodedAbapName
	 *            which is a form suitable for adding to XML
	 * @return the decoded ABAP name, that is the original form of the name.
	 */
	String decode(String encodedAbapName);
}
