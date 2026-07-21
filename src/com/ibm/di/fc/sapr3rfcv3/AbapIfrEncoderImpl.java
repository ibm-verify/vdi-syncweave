/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Encodes BAP name into a XML name equivalent.
 * 
 * @see com.ibm.di.fc.com.ibm.di.fc.sapr3fc.AbapIfrEncoderImpl#encode(java.lang.String) for
 *      details.
 * @see {http://ifr.sap.com/home/Documents/ABAP_Serialization.htm} for spec.
 * 
 * @modelguid {1A37B348-60F3-4D06-AC1F-D11CAD91400B}
 */
final class AbapIfrEncoderImpl implements AbapIfrEncoder {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String HEX_ENCODED_SEQUENCE_PREFIX = "_--";

	private final static String SLASH_PLAIN = "/";

	private final static String SLASH_ENCODED = "_-";

	public AbapIfrEncoderImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.AbapIfrEncoder#encode(java.lang.String)
	 *      Convert a ABAP Name into XML Name. Rules are: - characters from A-Z,
	 *      a-z, or '_' need no mapping - character from 0-9 or '.' need no
	 *      mapping - '/' is mapped to '_-' - any other ASCII character c is
	 *      mapped to '_--hex(c)' where hex(c) is the 2-character hexadecimal
	 *      represenation of ASCII code (e.g., & is represented as _--26)
	 */
	/* @modelguid {8B4CB2AC-DBEA-41A9-9F64-1B7498781727} */
	public String encode(String plainAbapName) {
		String encodedName = "";

		if (plainAbapName == null) {
			throw new IllegalArgumentException();
		}

		if (plainAbapName.equals("")) {
			return encodedName;
		}

		/*
		 * Convert any character that is not in the set [A-Za-z_0-9./]
		 */
		Pattern pattern = Pattern.compile("[^A-Za-z_0-9\\./]+");
		Matcher matcher = pattern.matcher(plainAbapName);

		int startpos = 0;
		int endpos = 0;

		while (matcher.find()) {
			if (matcher.start() > 0) {
				endpos = matcher.start();
			}

			// Get the first part of the string
			if (endpos - startpos > 0) {

				encodedName = encodedName.concat(plainAbapName.substring(
						startpos, endpos));
			}

			// convert the ascii char into _--xx equivalent
			// toCharArray()
			char[] chars = matcher.group().toCharArray();
			for (int j = 0; j < chars.length; j++) {
				encodedName = encodedName.concat(HEX_ENCODED_SEQUENCE_PREFIX
						.concat(Integer.toHexString(chars[j])));
			}

			// set start postion to next location
			startpos = matcher.end();
		}

		// No matches were found, return the original
		if (startpos == 0) {
			encodedName = plainAbapName;
		} else if (startpos != plainAbapName.length()) {
			// we need to get the last matching token
			encodedName = encodedName.concat(plainAbapName.substring(startpos));

		}

		// Convert any '/' to '_-'
		return encodedName.replaceAll(SLASH_PLAIN, SLASH_ENCODED);
	}

}
