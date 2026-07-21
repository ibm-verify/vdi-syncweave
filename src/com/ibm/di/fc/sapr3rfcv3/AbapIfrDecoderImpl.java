/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Decodes XML name into ABAP name.
 * 
 * @see com.ibm.di.fc.com.ibm.di.fc.sapr3fc.AbapIfrDecoderImpl#decode(java.lang.String) for
 *      details.
 * @see {http://ifr.sap.com/home/Documents/ABAP_Serialization.htm} for spec.
 * 
 */
final class AbapIfrDecoderImpl implements AbapIfrDecoder {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String HEX_ENCODED_SEQUENCE_PREFIX = "_--";

	private final static String SLASH_PLAIN = "/";

	private final static String SLASH_ENCODED = "_-";

	public AbapIfrDecoderImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.AbapIfrDecoder#decode(java.lang.String)
	 *      Convert a XML Name to ABAP Name. Rules are: - characters from A-Z,
	 *      a-z, or '_' need no mapping - character from 0-9 or '.' need no
	 *      mapping - '_-' is mapped to '/' - '_--hex(c)' mapped to ASCII
	 *      character c where hex(c) is the 2-character hexadecimal
	 *      represenation of ASCII code (e.g., _--26 is represented converted to &)
	 */
	public String decode(String encodedAbapName) {
		Pattern pattern;
		String decodedName = "";

		if (encodedAbapName == null) {
			// Coding error
			throw new IllegalArgumentException();
		}

		if (encodedAbapName.equals("")) {
			return decodedName;
		}

		pattern = Pattern.compile(HEX_ENCODED_SEQUENCE_PREFIX
				.concat("[0-9A-Fa-f][0-9A-Fa-f]"));
		Matcher matcher = pattern.matcher(encodedAbapName);

		int startpos = 0;
		int endpos = 0;

		while (matcher.find()) {
			if (matcher.start() > 0) {
				endpos = matcher.start();
			}

			// Get the first part of the string
			if (endpos - startpos > 0) {
				decodedName = decodedName.concat(encodedAbapName.substring(
						startpos, endpos)
						.replaceAll(SLASH_ENCODED, SLASH_PLAIN));
			}

			// convert the _--nn into ascii equivalent
			char c = (char) Integer.valueOf(
					matcher.group().substring(
							HEX_ENCODED_SEQUENCE_PREFIX.length()), 16)
					.intValue();
			decodedName += c;

			// set start postion to next location
			startpos = matcher.end();
		}

		// No matches were found, return the original
		if (startpos == 0) {
			decodedName = encodedAbapName.substring(startpos).replaceAll(
					SLASH_ENCODED, SLASH_PLAIN);
		} else if (startpos != encodedAbapName.length()) {
			// we need to get the last matching token
			decodedName = decodedName
					.concat(encodedAbapName.substring(startpos).replaceAll(
							SLASH_ENCODED, SLASH_PLAIN));
		}

		return decodedName;
	}
}
