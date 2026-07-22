/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// StringUtils.java
//
//
//
package com.ibm.di.util;

import java.io.*;
import java.util.Vector;

import com.ibm.di.util.Base64OutputStream;
import com.ibm.di.server.ResourceHash;

import com.ibm.icu.util.StringTokenizer;

public class StringUtils {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public Exception lastError;

	static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public Exception getLastError() {
		return lastError;
	}

	public static String toHex(byte b) {
		StringBuilder str = new StringBuilder();
		appendHex(b, str);
		return str.toString();
	}

	public static void appendHex(byte b, StringBuilder buffer) {
		char ch = (char) b;
		buffer.append(HEX[((ch >> 4) & 15)]);
		buffer.append(HEX[(ch & 15)]);
	}

	public static void appendHex(char c, StringBuilder buffer) {
		appendHex((byte) (c >> 8), buffer);
		appendHex((byte) (c & 255), buffer);
	}

	public static String toHex(char c) {
		StringBuilder str = new StringBuilder();
		appendHex(c, str);
		return str.toString();
	}

	public static String toHex(String str) {
		StringBuffer res = new StringBuffer();
		byte[] data = str.getBytes();

		for (int i = 0; i < data.length; i++) {
			if (res.length() > 0)
				res.append(" ");
			res.append(toHex(data[i]));
		}
		return res.toString();
	}

	public static byte nibble(char ch) throws NumberFormatException {
		if (ch >= '0' && ch <= '9')
			return (byte) (ch - '0');

		if (ch >= 'A' && ch <= 'F')
			return (byte) ((ch - 'A') + 10);

		if (ch >= 'a' && ch <= 'f')
			return (byte) ((ch - 'a') + 10);

		throw new NumberFormatException(sResHash.getString("nibble.symbol", new StringBuffer(ch)));
	}

	public static byte fromHex(String hex) throws NumberFormatException {
		byte c1 = nibble(hex.charAt(0));
		byte c2 = nibble(hex.charAt(1));
		return (byte) ((c1 << 4) + c2);
	}

	public static String toPrint(String str) {
		return ResourceHash.escapeSpecialChars(str);
	}

	public static String fromPrint(String str) {
		return ResourceHash.substituteSpecialSequences(str);
	}

	public static Vector<String> splitstring(String str, String key) {
		StringTokenizer st = new StringTokenizer(str, key);
		Vector<String> v = new Vector<String>();
		while (st.hasMoreTokens())
			v.add(st.nextToken());

		return v;
	}

	public static String[] splitstringArr(String str, String key) {
		Vector<String> v = splitstring(str, key);
		if (v.size() < 1)
			return null;

		return v.toArray(new String[v.size()]);
	}

	public String toBase64(String source) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Base64OutputStream b = new Base64OutputStream(bos);
			byte[] buf = source.getBytes();
			for (int i = 0; i < buf.length; i++) {
				b.write(buf[i]);
				b.write(0);
			}
			// b.write (source.getBytes());
			b.flush();
			return bos.toString();
		} catch (Exception e) {
			lastError = e;
			return null;
		}
	}

	/**
	 * Convert a string to ASCII. Non-ASCII characters will be escaped using the
	 * Unicode escape Java notation: "\\uxxx".
	 * 
	 * @param s
	 *            A string, which may include non ASCII characters.
	 * @return A string that contains only ASCII characters.
	 */
	public static String toASCII(String s) {
		if (isISOlatin1(s))
			return s;

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c <= 0xff) {
				buffer.append(c);
			} else {
				buffer.append("\\u");
				appendHex(c, buffer);
			}
		}
		return buffer.toString();
	}

	/**
	 * Return true if all characters are in the ISO latin1 character set.
	 * 
	 * @param s
	 *            The String to check
	 * @return <code>true</code> if all characters are in the ISO latin1 character set.
	 */
	private static boolean isISOlatin1(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) > 0xff)
				return false;
		}
		return true;
	}

	/**
	 * Calculates the number of non-empty tokens in a string.
	 * 
	 * @param s
	 * @param delim
	 * @return
	 * @see #splitString(String, char)
	 */
	public static int splitStringTokenCount(String s, char delim) {

		int tokenCount = 0;

		int beg = 0;

		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == delim) {
				if (beg < i) {
					// token is not empty
					++tokenCount;
				}
				beg = i + 1;
			}
		}

		if (beg < s.length()) {
			++tokenCount;
		}

		return tokenCount;
	}

	/**
	 * Split string into non-empty tokens using the specified delimeter. This
	 * routine is supposed to be faster than
	 * {@link java.lang.String#split(String)}.
	 * 
	 * @param s
	 *            String to be split into tokens.
	 * @param delim
	 *            Delimeter character.
	 * @return An array of tokens. Will never be null.
	 * @see #splitStringTokenCount(String, char)
	 */
	public static String[] splitString(String s, char delim) {

		int tokenCount = splitStringTokenCount(s, delim);
		int top = 0;

		String[] tokens = new String[tokenCount];

		int beg = 0;

		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == delim) {
				if (beg < i) {
					// token is not empty
					String t = s.substring(beg, i);
					tokens[top++] = t;
				}
				beg = i + 1;
			}
		}

		if (beg < s.length()) {
			String t = s.substring(beg);
			tokens[top++] = t;
		}

		return tokens;
	}

	public static boolean isBlank(final String str) {
		if (str == null) {
			return true;
		}
		return str.trim().isEmpty();
	}
}
