/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.BufferedReader;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * The Line Reader Parser reads single lines of data. The line read is returned
 * in a single attribute. There is also an attribute named linenumber that
 * contains the line number, starting with 1.
 */
public class LineReader extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "linereaderparser";

	/**
	 * The reader object used for input.
	 */
	private BufferedReader reader;

	/**
	 * Contains the line number.
	 */
	private int linenumber;

	/**
	 * The attribute that contains or receives the line.
	 */
	private String attrName;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * This method is called by the hosting component (e.g. connector) to
	 * initialize the parser.
	 * 
	 * @exception Exception :
	 *                never
	 * 
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");

		reader = getReader();
		linenumber = 0;
		attrName = getParam("attributeName");
		if (attrName == null || attrName.length() == 0)
			attrName = "line";
		if (debugMode()) {
			debug(sResHash.getString("PARSER.LINEREADER.ATTRIBUTENAME.INFO",
					attrName));
		}

		Trace.exitmin(this, "initParser");
	}

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception Exception
	 *                if an I/O error occurs.
	 * 
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "writeEntry", entry);
		String str = entry.getString(attrName);

		if (str != null) {
			getWriter().write(str);
		}

		getWriter().newLine();
		getWriter().flush();
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception Exception
	 *                if error occurs.
	 * 
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		Entry e = new Entry();
		String str = reader.readLine();
		if (str == null)
			return null;

		linenumber++;
		e.setAttribute(attrName, str);
		e.setAttribute("linenumber", "" + linenumber);
		Trace.exitmax(this, "readEntry", e);
		return e;
	}

	/**
	 * Discover the schema for the Parser. For example, a XML Parser could
	 * return a representation of the XML Schema or the DTD referenced in a XML
	 * file.
	 * 
	 * @param source
	 *            The object on which to discover schema
	 * @return A Vector of com.ibm.di.entry.Entry objects describing each entity
	 * @throws Exception
	 *             If an I/O error occurs
	 * 
	 * @since 7.0
	 */
	public Object querySchema(Object source) throws Exception {
		Vector list = new Vector();
		Entry e;

		e = new Entry();
		e.addAttributeValue("name", attrName);
		e.addAttributeValue("syntax", "java.lang.String");
		list.add(e);

		e = new Entry();
		e.addAttributeValue("name", "linenumber");
		e.addAttributeValue("syntax", "java.lang.String");
		list.add(e);

		return list;
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I% 20%E%";
	}
}
