/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.IOException;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * A class reading and writing dsEntries in a simple one-line-per-attribute
 * format.
 * 
 */

public class SimpleParser extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "simpleparser";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Default constructor.
	 */
	public SimpleParser() {
		Trace.entrymid(this, "SimpleParser");
		Trace.exitmid(this, "SimpleParser");
	}

	/**
	 * Reads the next entry from the input stream. If end of stream encountered
	 * a null value is returned.
	 * 
	 * @return the next Entry in the input stream.
	 * @exception IOException
	 * @see java.io.IOException
	 */

	public Entry readEntry() throws IOException {
		Trace.entrymax(this, "readEntry");
		int index;
		String str, key, value;
		Entry e = new Entry();

		while ((str = getReader().readLine()) != null) {

			if (_debug) {
				debug(sResHash.getString("PARSER.SIMPLE.READLINE.INFO", str));
			}

			if (str.compareTo(".") == 0)
				break;

			index = str.indexOf(":");
			if (index == -1) {
				throw new IOException(sResHash.getString(
						"PARSER.SIMPLE.READENTRY.ERROR", str));
			}

			key = str.substring(0, index);
			if (index >= str.length())
				value = "";
			else
				value = fromPrint(str.substring(index + 1));

			Attribute a = e.getAttribute(key);
			if (a != null)
				a.addValue(value);
			else
				e.setAttribute(key, value);
		}
		Trace.exitmax(this, "readEntry", e);
		if (e.size() < 1)
			return null;
		else
			return e;

	}

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception IOException
	 *                if an I/O error occurs.
	 * 
	 */
	public void writeEntry(Entry entry) throws IOException {
		Trace.entrymax(this, "writeEntry");
		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {

			Attribute a = entry.getAttribute(names[i]);

			for (int j = 0; j < a.size(); j++) {
				if (a.getValue(j) != null)
					writeString(a.getName() + ":"
							+ toPrint(a.getValue(j).toString())
							+ System.getProperty("line.separator"));
			}

		}
		getWriter().write("." + System.getProperty("line.separator"));
		getWriter().flush();
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Removes escaping backslashes from Strings.
	 * 
	 * @param str
	 *            String to manipulate.
	 * @return the String without escaping symbols.
	 */
	private String fromPrint(String str) {
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < str.length(); i++) {

			char ch = str.charAt(i);
			if ((ch == '\\') && (i + 1 < str.length())) {
				switch (str.charAt(i + 1)) {
				case 'n':
					ch = '\n';
					i++;
					break;
				case 'r':
					ch = '\r';
					i++;
					break;
				case '\\':
					i++;
					break;
				default:
					break;
				}
			}
			res.append(ch);
		}

		return res.toString();
	}

	/**
	 * Adds escaping backslashes to String.
	 * 
	 * @param str
	 *            String to manipulate.
	 * @return the String with escaping symbols.
	 */
	private String toPrint(String str) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char ch = (char) str.charAt(i);
			switch (ch) {
			case '\n':
				res.append("\\n");
				break;
			case '\r':
				res.append("\\r");
				break;
			case '\\':
				res.append("\\\\");
				break;
			default:
				res.append(ch);
			}
		}

		return res.toString();
	}

	/**
	 * Writes the specified string to the output stream and if debug is set
	 * writes it to the log
	 * 
	 * @param str
	 *            the String to be written
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private void writeString(String str) throws IOException {
		if (_debug) {
			debug(sResHash.getString("PARSER.SIMPLE.WRITESTRING.INFO", str));
		}
		getWriter().write(str);
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

}
