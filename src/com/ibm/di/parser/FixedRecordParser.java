/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Fixed Parser reads and writes fixed length text records.
 */
public class FixedRecordParser extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "fixedparser";

	/**
	 * Record description
	 */
	private Vector cols;

	/**
	 * Trim leading/trailing spaces for input fields
	 */
	private boolean trim;

	/**
	 * Padding character value.
	 */
	private char paddingCharacter;

	/**
	 * End position to read/write.
	 */
	private int maxEnd;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Represents a column
	 */
	private class FixedRecordParserCol {
		/**
		 * Name of the column
		 */
		public String name;

		/**
		 * Start position
		 */
		public int start;

		/**
		 * End position.
		 */
		public int end;

		/**
		 * Total size.
		 */
		public int size;

		/**
		 * Class constructor
		 * 
		 * @param p1
		 *            name
		 * @param p2
		 *            start position
		 * @param p3
		 *            total size
		 */
		public FixedRecordParserCol(String p1, String p2, String p3) {
			name = p1;
			start = Integer.parseInt(p2.trim()) - 1; // Strings use 0 based
			// indexing
			size = Integer.parseInt(p3.trim());
			end = start + size;
			if (end > maxEnd)
				maxEnd = end;
		}
	}

	/**
	 * This method is called by the hosting component (e.g. connector) to
	 * initialize the parser.
	 */
	public void initParser() {

		Trace.entrymin(this, "initParser");
		resetProperties();
		cols = new Vector();
		maxEnd = 0;
		String str = getParam("fixedCols");
		StringTokenizer st = new StringTokenizer(str, System
				.getProperty("line.separator")); // "\r\n"
		while (st.hasMoreTokens()) {
			String tok = st.nextToken().trim();
			if (tok.length() < 1)
				continue;
			StringTokenizer st2 = new StringTokenizer(tok, ",");
			cols.add(new FixedRecordParserCol(st2.nextToken(), st2.nextToken(),
					st2.nextToken()));
		}

		trim = Boolean.valueOf(getParam("trimValues")).booleanValue();

		str = getParam("paddingCharacter");
		if (str == null)
			paddingCharacter = ' ';
		else
			paddingCharacter = str.charAt(0);
		if (debugMode()) {
			debug(sResHash.getString("PARSER.FIXED.PADDING.INFO", ""
					+ paddingCharacter));
		}
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception Exception
	 *                if an I/O error occurs.
	 * 
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, "readEntry");
		String str = getReader().readLine();
		if (str == null)
			return null;

		Entry e = new Entry();

		for (int i = 0; i < cols.size(); i++) {
			FixedRecordParserCol fx = (FixedRecordParserCol) cols.get(i);
			String fld;

			if (fx.start >= str.length())
				fld = "";
			else if (fx.end > str.length())
				fld = str.substring(fx.start);
			else
				fld = str.substring(fx.start, fx.end);

			if (trim)
				fld = fld.trim();

			e.addAttributeValue(fx.name, fld);
		}
		Trace.exitmax(this, "readEntry", e);
		return e;
	}

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param e
	 *            The entry to write
	 * @exception Exception
	 *                if an I/O error occurs.
	 * 
	 */
	public void writeEntry(Entry e) throws Exception {

		Trace.entrymax(this, "writeEntry", e);

		StringBuffer out = new StringBuffer(maxEnd);

		for (int i = 0; i < maxEnd; i++)
			out.append(paddingCharacter);

		for (int i = 0; i < cols.size(); i++) {

			FixedRecordParserCol fx = (FixedRecordParserCol) cols.get(i);
			String val = e.getString(fx.name);

			if (val == null)
				continue;

			if (trim)
				val = val.trim();

			if (val.length() > fx.size)
				val = val.substring(0, fx.size);

			int end = fx.end;
			if (fx.start + val.length() < end)
				end = fx.start + val.length();

			out.replace(fx.start, end, val);
		}

		getWriter()
				.write(out.toString() + System.getProperty("line.separator"));
		getWriter().flush();
		Trace.exitmax(this, "writeEntry");
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

		for (Iterator iter = cols.iterator(); iter.hasNext();) {
			FixedRecordParserCol frc = (FixedRecordParserCol) iter.next();
			Entry e = new Entry();
			e.addAttributeValue("name", frc.name);
			e.addAttributeValue("syntax", "java.lang.String");
			list.add(e);
		}
		return list;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets Parser's parameters.
	 */
	private void resetProperties() {
		cols = null;
		trim = false;
		paddingCharacter = '\0';
		maxEnd = 0;
	}
}
