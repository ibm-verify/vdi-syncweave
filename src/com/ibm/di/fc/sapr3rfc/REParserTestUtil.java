/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;

/**
 * 
 * A generic utility to parse some arbitrary text.
 */
class REParserTestUtil implements Iterator {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Matcher matcher;

	private int startpos;

	private int endpos;

	private Pattern pattern;

	private String token;

	private CharSequence text;

	private boolean wantDelimiter;

	/**
	 * Setup the pattern that we wish to use for this RE.
	 * 
	 * @param pattern
	 */
	public REParserTestUtil(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	/**
	 * Have we matched anything else? Get the string if we have.
	 * 
	 * @return
	 */
	public boolean hasNext() {
		if (matcher.find()) {
			if (matcher.start() > 0) {
				endpos = matcher.start();
			}

			// Get the first part of the string
			if (wantDelimiter && endpos - startpos > 0) {
				token = text.toString().substring(startpos, endpos);
			} else {
				// get the token
				token = matcher.group();
			}

			// set start position to next location
			startpos = matcher.end();

			return true;
		}

		// // No matches were found, return the original
		// if (startpos == 0)
		// {
		// token = text.toString().substring(startpos);
		// }
		// else if (startpos != text.length())
		// {
		// // we need to get the last matching token
		// token = text.toString().substring(startpos);
		// }

		return false;
	}

	/**
	 * Search for the specified character sequence within the pattern defined.
	 * 
	 * @param match
	 * @param wantDelimiter
	 */
	public void search(CharSequence text, boolean wantDelimiter) {
		matcher = pattern.matcher(text);
		startpos = 0;
		endpos = 0;
		this.text = text;
		this.wantDelimiter = wantDelimiter;
	}

	public void search(CharSequence text) {
		search(text, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		String msg = LogMessageHelper.getMsgResource().getMessage(
				LogMessageHelper.SAPR3_RFCFC_0014);
		throw new RuntimeException(msg);
	}

}
