/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

/**
 * Encode ABAP names, e.g. RFC parameter names, according IFR standard.
 */

interface AbapIfrEncoder {

	/**
	 * Convert a ABAP Name into XML Name. Rules are: - characters from A-Z, a-z,
	 * or '_' need no mapping - character from 0-9 or '.' need no mapping - '/'
	 * is mapped to '_-' - any other ASCII character c is mapped to '_--hex(c)'
	 * where hex(c) is the 2-character hexadecimal represenation of ASCII code
	 * (e.g., & is represented as _--26)
	 * 
	 * @param plainAbapName -
	 *            this is the unencoded ABAP name
	 * @return the corresponding ABAP name which has parameters mapped
	 */
	String encode(String plainAbapName);
}
