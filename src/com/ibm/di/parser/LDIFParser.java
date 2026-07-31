/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Locale;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 *
 * The LDIF Parser reads and writes LDIF style data. The LDIF Parser is usually
 * to do file exchange with an LDAP directory.
 *
 */
public class LDIFParser extends ParserImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "ldifparser";

	/**
	 * This is the look-ahead line (continuation lines)
	 */
	private String lastLine;

	/**
	 * Used when writing.
	 */
	private boolean needVersionNumber = true;

	/**
	 * If true, do not write change records
	 */
	private boolean onlyDescriptive = false;

	/**
	 * This is the attribute name used by Integrator internally to denote the
	 * distinguished name.
	 */
	private String dnAttributeName = "$dn";

	/**
	 * The default binary Attributes
	 */
	private String binaryAttributes = "photo personalSignature audio jpegPhoto javaSerializedData thumbnailPhoto thumbnailLogo userPassword userCertificate authorityRevocationList certificateRevocationList crossCertificatePair x500UniqueIdentifier objectGUID objectSid ";

	/**
	 * Hashtable containing the names of all binary attributes (in lowercase)
	 */
	private Hashtable<String, String> binaryAttr;

	/**
	 * The character encoding used to decode base64 encoded characters
	 */
	private String characterSet;

	/**
	 * Array used to decode base64 encoding
	 */
	private int[] decode = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58,
			59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
			22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
			46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

	/**
	 * Array used for base64 encoding
	 */
	private char[] encode;

	private boolean supportLanguageTag;
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Creates LDIF Parser.
	 */
	public LDIFParser() {
		Trace.entrymid(this, "LDIFParser");
		Trace.exitmid(this, "LDIFParser");
	}

	/**
	 * This function is called after the connector has initialized the parser
	 * with input/output streams.
	 */
	public void initParser() {

		Trace.entrymin(this, "initParser");
		resetProperties();
		if (getParam("ldifDNAttributeName") != null) {
			dnAttributeName = getParam("ldifDNAttributeName");
		}
		if (getParam("ldifVersion") != null && getParam("ldifVersion").equalsIgnoreCase("false")) {
			needVersionNumber = false;
		}

		characterSet = getParam("characterSet");
		// use UTF-8 encoding if nothing else specified, so that NLS data can be
		// processed
		if (characterSet == null || characterSet.trim().length() == 0) {
			characterSet = "UTF-8";
		}

		// Add binary attributes
		String str = getParam("ldifBinaryAttributes");
		if (str != null) {
			binaryAttributes += str;
		}

		binaryAttr = new Hashtable<String, String>();
		StringTokenizer st = new StringTokenizer(binaryAttributes, " ,\r\n");
		while (st.hasMoreTokens()) {
			binaryAttr.put(st.nextToken().toLowerCase(Locale.ENGLISH), "");
		}

		onlyDescriptive = Boolean.valueOf(getParam("ldifOnlyDescriptive"));

		supportLanguageTag = Boolean.valueOf(getParam("supportLanguageTag"));

		// Initialize encode array
		encode = new char[64];
		for (int i = 0; i < 128; i++) {
			if (decode[i] >= 0)
				encode[decode[i]] = (char) i;
		}
		Trace.exitmin(this, "initParser");
	}

	/**
	 * Reads LDIF files into entries. This method parses LDIF attributes to
	 * entry attributes and maps &quot;dn&quot; key to internal name specified
	 * by the <code>ldifDNAttributeName</code> parameter. This is the main entry
	 * point for reading entries.
	 *
	 * @return entry
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public Entry readEntry() throws IOException {

		Trace.entrymax(this, "readEntry");
		int index;
		String str;
		String key = null;
		String value;
		Entry e = new Entry();
		char lastOper = Attribute.ATTRIBUTE_REPLACE;
		char prevOper = Attribute.ATTRIBUTE_MOD;
		boolean attributeDeleteFlag = false;

		while (true) {
			str = getLine();
			// End of change or end of entry?
			if (str == null || str.equals("") || str.equals("-")) {
				// Make sure we get empty attributes as well
				if (key != null && !key.equalsIgnoreCase("changetype") && e.getAttribute(key) == null) {
					e.newAttribute(key).setOper(lastOper);
				} else if (key != null && attributeDeleteFlag) {

					e.removeAttribute(key);
					e.newAttribute(key, Attribute.ATTRIBUTE_REPLACE);

				}

				if (str == null || str.equals(""))
					break;
				else
					continue;
			}
			attributeDeleteFlag = false;
			// A special case when you read LDIF data off a Netscape changelog
			// entry
			if (str.length() > 0 && str.charAt(0) == 0) {
				break;
			}

			index = str.indexOf(":");
			if (index == -1) {
				throw new IOException(sResHash.getString("PARSER.LDIF.READENTRY.ERROR", str));
			}

			key = str.substring(0, index);
			if (!supportLanguageTag && key.indexOf(";") != -1) {
				logmsg(sResHash.getString("PARSER.LDIF.IGNORE.LANGTAG", key));
				key = key.substring(0, key.indexOf(";"));
			}

			// Map "dn" to internal name
			if (key.equalsIgnoreCase("dn")) {
				key = dnAttributeName;
			}

			if (index >= str.length()) {
				value = "";
			} else {
				value = str.substring(index + 1).trim();
			}

			byte[] bvalue = null;
			if (value.startsWith(":")) {
				bvalue = getBytes(value);
				if (binaryAttr.get(key.toLowerCase(Locale.ENGLISH)) == null) {
					if (characterSet == null) {
						value = new String(bvalue);
					} else {
						value = new String(bvalue, characterSet);
					}
					bvalue = null;
				}
			}

			if (key.equalsIgnoreCase("replace") || key.equalsIgnoreCase("delete") || key.equalsIgnoreCase("add")) {
				Attribute old = e.getAttribute(value);
				if (old != null) {
					prevOper = old.getOper();
					if (key.equalsIgnoreCase("replace")) {
						old.clear();
					}
				} else {
					prevOper = Attribute.ATTRIBUTE_MOD;
				}
			}

			// For incremental LDIF files we store the last
			// operation in the attribute's operand.

			if (key.equalsIgnoreCase("replace")) {
				lastOper = Attribute.ATTRIBUTE_REPLACE;
				key = value;
				continue;
			}

			if (key.equalsIgnoreCase("delete")) {
				lastOper = Attribute.ATTRIBUTE_DELETE;
				key = value;
				attributeDeleteFlag = true;
				continue;
			}

			if (key.equalsIgnoreCase("add")) {
				lastOper = Attribute.ATTRIBUTE_ADD;
				key = value;
				continue;
			}

			// Set the entry operation from the changetype attribute
			// (Incremental LDIF only)
			//
			if (key.equalsIgnoreCase("changetype")) {
				if ("modify".equalsIgnoreCase(value) || "modrdn".equalsIgnoreCase(value) || "moddn".equalsIgnoreCase(value)) {
					e.setOp(Entry.OP_MOD);
				} else if ("add".equalsIgnoreCase(value)) {
					e.setOp(Entry.OP_ADD);
				} else if ("delete".equalsIgnoreCase(value)) {
					e.setOp(Entry.OP_DEL);
				} else {
					throw new IOException(sResHash.getString("PARSER.LDIF.UNKNOWN.LDIF.CHANGETYPE", value));
				}
				continue;
			}

			// Get attribute or create a new one
			Attribute a = e.newAttribute(key);

			// Add value and operation code to the attribute
			Object val;
			if (bvalue != null) {
				val = bvalue;
			} else {
				val = value;
			}

			switch (lastOper) {
			case Attribute.ATTRIBUTE_ADD:
				if (prevOper == Attribute.ATTRIBUTE_REPLACE) {
					a.addValue(val);
				} else if (prevOper == Attribute.ATTRIBUTE_DELETE && a.size() == 0) {
					a.setOper(Attribute.ATTRIBUTE_REPLACE);
					prevOper = Attribute.ATTRIBUTE_REPLACE;
					a.addValue(val);
				} else {
					a.setOper(Attribute.ATTRIBUTE_MOD);
					a.addValue(val, AttributeValue.AV_ADD);
				}
				break;
			case Attribute.ATTRIBUTE_DELETE:
				if (prevOper == Attribute.ATTRIBUTE_REPLACE) {
					a.removeValue(val);
				} else {
					a.setOper(Attribute.ATTRIBUTE_MOD);
					a.addValue(val, AttributeValue.AV_DELETE);
				}
				break;
			default: // Generic and Replace
				a.setOper(Attribute.ATTRIBUTE_REPLACE);
				a.addValue(val);
				break;
			}

		} // end while

		Trace.exitmax(this, "readEntry", e);
		if (e.size() < 1) {
			return null;
		} else {
			return e;
		}

	}

	/**
	 * Writes entry in LDIF format to the current output writer. Depending on
	 * whether <code>ldifVersion</code> parameter is checked the ldif version
	 * number is also written .
	 *
	 * @param entry
	 *            the entry to be written.
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void writeEntry(Entry entry) throws IOException {

		Trace.entrymax(this, "writeEntry", entry);

		if (needVersionNumber) {
			writeString("version: 1");
			getWriter().newLine();
			getWriter().newLine(); // add a separator (gwb 597)

			needVersionNumber = false;
		}

		if (onlyDescriptive || entry.getOp() == Entry.OP_GEN)
			writeDescriptiveRecord(entry);
		else
			writeChangeRecord(entry);

		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Writes an LDIF change record to the writer.
	 *
	 * @param entry
	 *            the entry to be written.
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private void writeChangeRecord(Entry entry) throws IOException {
		if (entry.getOp() == Entry.OP_UNCHANGED)
			return;

		writeString("dn:" + toPrint(entry.getString(dnAttributeName)));
		getWriter().newLine();
		Attribute newrdn = null;

		// Write changetype
		switch (entry.getOp()) {
		case Entry.OP_ADD:
			writeString("changetype: add");
			getWriter().newLine();
			break;
		case Entry.OP_MOD:
			// newrdn attribute is required for moddn and modrdn operations
			newrdn = entry.getAttribute("newrdn");
			if (newrdn != null) {
				writeString("changetype: modrdn");
			} else {
				writeString("changetype: modify");
			}
			getWriter().newLine();
			break;
		case Entry.OP_DEL:
			writeString("changetype: delete");
			getWriter().newLine();
			getWriter().newLine();
			getWriter().flush();
			return;
		}

		// For a valid moddn and modrdn operations the
		// newrdn attribute MUST be right after the changetype attribute
		if (newrdn != null) {
			writeAttributeAndValue(newrdn, 0);
		}

		// Write attributes and values
		for (String name : entry.getAttributeNames()) {

			if (name.compareTo(dnAttributeName) == 0) {
				continue;
			}

			// skip newrdn attribute if we already write it
			if (newrdn != null && name.compareTo("newrdn") == 0) {
				continue;
			}

			Attribute a = entry.getAttribute(name);

			if (a.getOper() == Attribute.ATTRIBUTE_DELETE) {
				writeString("delete: " + name);
				getWriter().newLine();
				writeString("-");
				getWriter().newLine();

			} else if (a.getOper() == Attribute.ATTRIBUTE_MOD) {

				int[] addValues = new int[a.size()];
				int addV = 0;
				int[] delValues = new int[a.size()];
				int delV = 0;

				// Sort add/delete values into separate arrays
				for (int j = 0; j < a.size(); j++) {
					if (a.getValueOper(j) == AttributeValue.AV_DELETE) {
						delValues[delV++] = j;
					} else if (a.getValueOper(j) != AttributeValue.AV_UNCHANGED) {
						addValues[addV++] = j;
					}
				}

				// Write add values
				if (addV > 0) {
					writeString("add: " + name);
					getWriter().newLine();
					for (int j = 0; j < addV; j++) {
						writeAttributeAndValue(a, addValues[j]);
					}
					writeString("-");
					getWriter().newLine();
				}

				// Write deleted values
				if (delV > 0) {
					writeString("delete: " + name);
					getWriter().newLine();
					for (int j = 0; j < delV; j++) {
						writeAttributeAndValue(a, delValues[j]);
					}
					writeString("-");
					getWriter().newLine();
				}
			} else if (a.getOper() == Attribute.ATTRIBUTE_UNCHANGED && entry.getOp() == Entry.OP_MOD) {
				// Nothing to do in this case...
				continue;
			} else {
				if (entry.getOp() == Entry.OP_MOD && !isModRDNAttribute(a)) {
					// Don't add 'replace: ' for the newrdn, deleteoldrdn
					// and newsuperior attributes of the moddn and modrdn
					// operations.
					if (a.getOper() == Attribute.ATTRIBUTE_ADD) {
						writeString("add: " + name);
						getWriter().newLine();
					} else {
						writeString("replace: " + name);
						getWriter().newLine();
					}
				}
				for (int j = 0; j < a.size(); j++) {
					writeAttributeAndValue(a, j);
				}
				if (entry.getOp() == Entry.OP_MOD && !isModRDNAttribute(a)) {
					writeString("-");
					getWriter().newLine();
				}
			}

		}
		getWriter().newLine();
		getWriter().flush();
	}

	/**
	 * @param a
	 *            Attribute object
	 * @return <code>true</code> if <code>a</code> is one of the special modrdn
	 *         attributes: newrdn, deleteoldrdn or newsuperior;
	 *         <code>false</code> otherwise
	 */
	private boolean isModRDNAttribute(Attribute a) {
		return (a.getName().equalsIgnoreCase("newrdn") || a.getName().equalsIgnoreCase("deleteoldrdn") || a.getName()
				.equalsIgnoreCase("newsuperior"));
	}

	/**
	 * Writes an LDIF descriptive record to the writer.
	 *
	 * @param entry
	 *            the entry to be written.
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	private void writeDescriptiveRecord(Entry entry) throws IOException {
		if (entry.getOp() == Entry.OP_DEL)
			return;

		writeString("dn:" + toPrint(entry.getString(dnAttributeName)));
		getWriter().newLine();

		// Write attributes and values
		for (String name : entry.getAttributeNames()) {

			if (name.compareTo(dnAttributeName) == 0)
				continue;

			Attribute a = entry.getAttribute(name);

			if (a.getOper() == Attribute.ATTRIBUTE_DELETE)
				continue;

			for (int i = 0; i < a.size(); i++) {
				if (a.getValueOper(i) != AttributeValue.AV_DELETE)
					writeAttributeAndValue(a, i);
			}
		}
		getWriter().newLine();
		getWriter().flush();
	}

	/**
	 * Writes Attribute name and value.
	 *
	 * @param a
	 *            {@link Attribute}
	 * @param index
	 *            index of the value
	 * @throws IOException
	 */
	private void writeAttributeAndValue(Attribute a, int index) throws IOException {
		Trace.entrymax(this, "writeAttributeAndValue", a);
		String str;
		if (a.getValue(index) instanceof byte[]) {
			str = ": " + encodeBytes((byte[]) a.getValue(index));
		} else {
			str = toPrint(a.getValue(index).toString());
		}
		writeString(a.getName() + ":" + str);
		getWriter().newLine();
		Trace.exitmax(this, "writeAttributeAndValue");
	}

	/**
	 * Retrieves next line.
	 *
	 * @return the read line.
	 * @throws IOException
	 *             if error during reading occurs.
	 */
	private String getLine() throws IOException {
		Trace.entrymax(this, "getLine");
		String accum;
		/*
		 * The StringBuilder will be used only if the line is continued - that
		 * is several lines are to be considered as one (by the ldif specs each
		 * line that is a continuation of the previous line starts with a single
		 * space).
		 */
		StringBuilder accumbuf = null;
		// Use look-ahead line?
		if ((lastLine != null) && (lastLine.length() > 0) /* gwb 1737 */) {
			accum = lastLine;
		} else {
			do {
				accum = readOneLine(getReader());
			} while (accum != null && (accum.startsWith("version:") || accum.startsWith("#") || accum.length() == 0));
		}

		// End of file?
		if (accum == null) {
			return null;
		}

		// Handle multiple blank lines gwb 1737
		if (lastLine != null && (lastLine.length() == 0) && (accum.length() > 0)) {
			lastLine = accum; // next record after blanks
			return ""; // force break
		}

		// Concatenate multiple lines (saving next entry's first line in
		// lastLine)
		while ((lastLine = readOneLine(getReader())) != null) {
			if (lastLine.startsWith(" ") || lastLine.startsWith("\t")) {
				if (accumbuf == null) {
					/*
					 * Create a buffer only if we need it. This is an
					 * optimization, because the creation of a StringBuilder is
					 * an overhead and most of the lines will not be continued.
					 */
					accumbuf = new StringBuilder(accum);
				}
				accumbuf.append(lastLine.substring(1));
			} else if (lastLine.startsWith("#")) {
				// ignore comment
				continue;
			} else {
				break;
			}
		}

		String result;
		if (accumbuf != null) {
			// a continued line is accumulated in the buffer
			result = accumbuf.toString().trim();
		} else {
			result = accum.trim();
		}
		Trace.exitmax(this, "writeAttributeAndValue", result);
		return result;
	}
	
	/**
	 * Returns next line from getReader().
	 * LDIF is quite special, and only LF or CR LF will terminate a line, but not a single CR.
	 * @return
	 */
	private String readOneLine(BufferedReader r) throws IOException {
		StringBuilder str = new StringBuilder();
		int i;
		while (true) {
			i = r.read();
			if (i < 0) {
				if (str.length() == 0)
					return null;
				else
					return str.toString();
			}
			if (i == '\n') {
				if (str.length() > 0 && str.charAt(str.length()-1) == '\r')
					return str.substring(0, str.length() - 1);
				return str.toString();
			}
			str.append((char)i);
		}
	}

	/**
	 * Accepts a string and returns it encoded version.
	 *
	 * @param str
	 *            {@link String}
	 * @return the encoded version
	 */
	private String toPrint(String str) {

		if (str == null) {
			return "";
		}

		boolean mustEncode = (str.startsWith(":") || str.startsWith("<") || str.startsWith(" ") || str.endsWith(" "));

		int i = 0;
		while (i < str.length() && !mustEncode) {
			int ch = (int) str.charAt(i);
			mustEncode = (ch == 10 || ch == 13 || ch == 0 || ch > 127);
			i++;
		}

		try {
			if (mustEncode) {
				return ": " + encodeBytes(characterSet == null ? str.getBytes() : str.getBytes(characterSet));
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("PARSER.LDIF.WHILE.ENCODING.ERROR", new Object[] { str, e.getMessage() }));
		}
		return " " + str;
	}

	/**
	 * Accepts a byte array and encodes it to a string.
	 *
	 * @param b
	 *            byte array
	 * @return String
	 */
	private String encodeBytes(byte[] b) {
		Trace.entrymax(this, "encodeBytes");
		StringWriter w = new StringWriter();
		int res = 0;
		int i = 0;

		while (i < b.length) {
			int ch = b[i] & 0xff;
			switch (i % 3) {
			case 0:
				w.write(encode[ch >> 2]);
				res = (ch & 3) << 4;
				break;
			case 1:
				w.write(encode[res | (ch >> 4)]);
				res = (ch & 0xf) << 2;
				break;
			case 2:
				w.write(encode[res | (ch >> 6)]);
				w.write(encode[ch & 0x3f]);
			}
			i++;
		}

		i %= 3;
		if (i != 0) {
			w.write(encode[res]);
			if (i == 1) {
				w.write("==");
			} else {
				w.write("=");
			}
		}
		Trace.exitmax(this, "encodeBytes", w);
		return w.toString();
	}

	/**
	 * Retrieves the decoded byte array from the provided string
	 *
	 * @param str
	 *            String
	 * @return byte array
	 */
	private byte[] getBytes(String str) {
		Trace.entrymax(this, "getBytes", str);
		ByteArrayOutputStream w = new ByteArrayOutputStream();

		int mode = 0;
		int res = 0;

		for (int i = 1; i < str.length(); i++) {
			int ch = (int) str.charAt(i);
			if (ch > 0 && ch < 128) {
				ch = decode[ch];
			} else {
				continue;
			}

			if (ch < 0) {
				continue;
			}

			switch (mode) {
			case 0:
				res = ch << 2;
				break;
			case 1:
				w.write(res | (ch >> 4));
				res = (ch << 4) & 0xff;
				break;
			case 2:
				w.write(res | (ch >> 2));
				res = (ch << 6) & 0xff;
				break;
			case 3:
				w.write(res | ch);
				break;
			}
			mode = (mode + 1) % 4;
		}
		Trace.exitmax(this, "encodeBytes");
		return w.toByteArray();
	}

	/**
	 * Write a String
	 *
	 * @param str
	 *            String to be written.
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void writeString(String str) throws IOException {
		getWriter().write(str);
	}

	/**
	 * Version information.
	 *
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * Provided to test/debug function usage: passed argument of classpath to
	 * LDIF file will dump entries.
	 *
	 * @param argv
	 *            String array
	 */
	public static void main(String argv[]) {

		String ldifPath = argv[0];
		InputStream is = null;
		System.out.println(sResHash.getString("PARSER.LDIF.LDIF.FILE.TO.BE.TESTED", ldifPath));
		try {
			is = new FileInputStream(new File(ldifPath));
		} catch (FileNotFoundException e) {
			System.out.println(sResHash.getString("PARSER.LDIF.FILE.NOT.FOUND", ldifPath));
		}

		LDIFParser lp = new LDIFParser();
		lp.setInputStream(is);
		try {
			Entry ent = lp.readEntry();
			while (ent != null) {
				System.out.println(sResHash.getString("PARSER.LDIF.ENTRY", ent.toString()));
				ent = lp.readEntry();
			}
		} catch (java.io.IOException e) {
			System.out.println(sResHash.getString("PARSER.LDIF.ERROR.READING.LINE.FROM.FILE", ldifPath));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Resets Parser's parameters.
	 */
	private void resetProperties() {
		lastLine = null;
		needVersionNumber = true;
		dnAttributeName = "$dn";
		binaryAttributes = "photo personalSignature audio jpegPhoto javaSerializedData thumbnailPhoto thumbnailLogo userPassword userCertificate authorityRevocationList certificateRevocationList crossCertificatePair x500UniqueIdentifier objectGUID objectSid ";
		binaryAttr = null;
		characterSet = null;
		encode = null;
	}

}
