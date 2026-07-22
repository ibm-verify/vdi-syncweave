/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.icu.util.StringTokenizer;

/**
 * A class reading and writing Entries in a simple one-line-per-Entry format.
 * CSV is short for comma-separated values, and this format is popular for storing tabular data in text form.
 * The separator does not need to be a comma, it can be any single character.
 * 
 * 
 * @author Bjorn Stadheim
 * @see com.ibm.di.connector.Connector
 */

public class CSVParser extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "csvparser";

	/**
	 * List holding the columns.
	 */
	private List<String> columns;

	/**
	 * Character that separates columns
	 */
	private char colsep = ',';

	/**
	 * 
	 */
	private StringBuilder linebuf;

	/**
	 * The default value for this parameter is true. If Write Header is set, the
	 * first line output by the parser contains all the field names separated by
	 * the column separator.
	 */
	private boolean headersWritten;
	/**
	 * The default value for this parameter is false. 
	 * If the Write Bom is set to true a BOM element would element will be added to the file. 
	 */
	private boolean writeBOM;

	/**
	 * Number of current line.
	 */
	private int linenumber;

	/**
	 * Is quoting enabled.
	 */
	private boolean quoteEnabled = true;

	/**
	 * Quote all fields independently if they contain quote, separator or new
	 * line
	 * 
	 * @since 7.0
	 */
	private boolean quoteAll;

	/**
	 * Define a maximum number of bytes for a line. Line numbers of lines longer
	 * than this maximum number are logged.
	 */
	private int longlines;
	/**
	* This is used to determine what constitutes a line break (end of line) in the CSV file. 
	* If "prev" contains a CR, we have just read a CR, which means that a following LF can safely be ignored.
	* If "prev" contains something else, a LF is a line break, except when quoted.
	*/
	int prev = 0;

	/**
	 * Combine last fields in a new "Remainder" multi-valued attribute if all
	 * fields are more than the columns number
	 * 
	 * @since 7.0
	 */
	private boolean combineLastFields;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor;
	 */
	public CSVParser() {
		Trace.entrymid(this, "CSVParser");
		Trace.exitmid(this, "CSVParser");
	}

	/**
	 * Class constructor
	 * 
	 * @param in
	 *            : Input stream to read from;
	 * @param out
	 *            : Output stream to write to;
	 */
	public CSVParser(Reader in, Writer out) {

		Trace.entrymid(this, "CSVParser", in, out);
		setInputStream(in);
		setOutputStream(out);
		Trace.exitmid(this, "CSVParser");
	}

	/**
	 * This method is called by the hosting component (e.g. connector) to
	 * initialize the parser.
	 * 
	 * 
	 */
	public void initParser() {
		Trace.entrymin(this, "initParser");
		resetProperties();
		String p;

		p = getParam("csvColumnSeparator");
		if (p != null && p.length() > 0) {
			p = com.ibm.di.util.StringUtils.fromPrint(p);
			colsep = p.charAt(0);
		}
		if (debugMode()) {
			debug(sResHash.getString("PARSER.CSV.COLUMNSEP.INFO", "" + colsep));
		}

		p = getParam("csvCombineLastFields");
		if (p != null && p.equalsIgnoreCase("true")) {
			combineLastFields = true;
		}

		p = getParam("csvColumns");
		if (p != null && p.length() > 0) {
			columns = new ArrayList<String>();
			String cs;
			if (p.indexOf("\n") == -1)
				cs = "" + colsep;
			else
				cs = "\r\n";

			StringTokenizer st = new StringTokenizer(p, cs);
			while (st.hasMoreTokens()) {
				columns.add(st.nextToken());
			}

			if("true".equals(getParam("csvSortFields")))
				Collections.sort(columns);
			
			if (combineLastFields) {
				columns.add("Remainder");
			}
			
			logmsg(sResHash.getString("PARSER.CSV.COLUMNS.INFO", columns));
		} else {
			logmsg(sResHash.getString("PARSER.CSV.FIRSTCOLUMN.INFO"));
		}

		p = getParam("csvEnableQuoting");
		if (p != null && p.equalsIgnoreCase("false"))
			quoteEnabled = false;

		p = getParam("csvQuoteAllFields");
		if (p != null && p.equalsIgnoreCase("true")) {
			quoteAll = true;
		}

		p = getParam("csvWriteHeader");
		if (p != null && p.equalsIgnoreCase("false"))
			headersWritten = true;
		
		p = getParam("csvWriteBOM");
		if(p != null && p.equalsIgnoreCase("true"))
			writeBOM = true;

		// This parameter is just for backwards compatibility ?
		p = getParam("csvLogLongLines");
		if (p != null && p.length() > 0)
			longlines = Integer.parseInt(p);

		Trace.exitmin(this, "initParser");

	}

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception IOException
	 *                if error during reading next Entry occurs.
	 * 
	 */
	public Entry readEntry() throws IOException {
		Trace.entrymax(this, "readEntry");
		Entry e = new Entry();

		ArrayList<String> t = nextEntry();
		if (columns == null) {
			// Try semicolon
			String line = linebuf.toString();
			if (t == null) {
				if (line.indexOf(colsep) > 0) {
					t = new ArrayList<String>();
					StringTokenizer st = new StringTokenizer(line, "" + colsep);
					while (st.hasMoreTokens()) {
						String tok = st.nextToken();
						if (tok.startsWith("\"")) {
							tok = tok.substring(1);
							tok = tok.substring(0, tok.length() - 1);
						}
						t.add(tok);
					}
				}
				if ((t == null || t.size() == 0) && line.indexOf('\t') > 0) {
					t = new ArrayList<String>();
					StringTokenizer st = new StringTokenizer(line, "\t");
					while (st.hasMoreTokens()) {
						String tok = st.nextToken();
						if (tok.startsWith("\"")) {
							tok = tok.substring(1);
							tok = tok.substring(0, tok.length() - 1);
						}
						t.add(tok);
					}
					colsep = '\t';
				}
			}

		} else {
			if (t == null)
				return null;
		}

		if (columns == null) {
			columns = t;
			if (columns == null) {
				return null;
			}
			
			if (combineLastFields) {
				columns.add("Remainder");
			}
			return readEntry();
		}

		for (int i = 0; i < t.size(); i++) {
			String attrName = "";
			if ((i > columns.size() - 1)) {
				if (!combineLastFields) {
					break;
				} else {
					// If the number of fields is greater than the number of
					// columns they are combined in the "Remainder" multi-valued
					// attribute
					attrName = (String) columns.get(columns.size() - 1);
					e.getAttribute(attrName).addValue((String) t.get(i));
				}
			} else {
				attrName = (String) columns.get(i);
				if (attrName.length() > 0) {
					e.setAttribute(attrName, (String) t.get(i));
				}
			}

		}
		Trace.entrymax(this, "readEntry", e);
		return e;
	}

	/**
	 * The method retrieves the values for the next {@link Entry}
	 * 
	 * @return List holding the values for the Entry
	 * @throws IOException
	 */
	public ArrayList<String> nextEntry() throws IOException {
		Trace.entrymax(this, "nextEntry");
		ArrayList<String> arr = new ArrayList<String>();
		// String str = "";
		StringBuilder str = new StringBuilder();
		int ch = colsep;
		int lastchar;
		boolean quote = false;
		int bytecount = 0;

		linebuf = new StringBuilder();

		Reader r = getReader();

		while (true) {

			lastchar = ch;
			ch = r.read();
			if (ch == -1) {
				break;
			}

			bytecount++;

			linebuf.append((char) ch);

			if (quoteEnabled && ch == '"') {
    		prev = ch;
				if (quote) {
					ch = r.read();
					if (ch == -1)
						break;
					bytecount++;
					linebuf.append((char) ch);

					if (ch != '"')
						quote = false;

				} else if (lastchar == colsep || lastchar == '\n') {
					quote = true;
					continue;
				}
			}

			if (!quote) {
				if (ch == colsep) {
					arr.add(str.toString());
					str.setLength(0);
    			prev = ch;
					continue;
				}
    		/*code modified by L3 */
    		if (ch == '\r'){
    			linenumber++;
    			prev = ch;
    			break;
    		}  
				if (ch == '\n') {
    			if(prev != '\r') {
					linenumber++;
    				prev  = ch;
					break;
				}
    			else {
    				prev =ch;
     				continue;
			}
    		}    	
    	}
			str.append((char) ch);
		prev = ch;
		}

		if (ch != -1 || str.length() > 0 || arr.size() > 0)
			arr.add(str.toString());

		if (longlines > 0 && bytecount > longlines) {
			logmsg(sResHash.getString("PARSER.CSV.LONGLINE.INFO", new Object[] {
					"" + linenumber, "" + bytecount, linebuf.toString() }));
		}

		if (arr.size() < 1) {
			Trace.exitmax(this, "NULL");
			return null;
		} else {
			Trace.exitmax(this, "nextEntry", arr);
			return arr;
		}
	}

	/**
	 * Write an Entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception Exception
	 * 
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "writeEntry");

		if (columns == null) {
			columns = Arrays.asList(entry.getAttributeNames());
			logmsg(sResHash.getString("PARSER.CSV.NOCOLUMN.INFO", columns));
			if("true".equals(getParam("csvSortFields")))
				Collections.sort(columns);
		}

		linebuf = new StringBuilder();
		if (!headersWritten) {
			
			// If Write BOM is set add a BOM element
			if(writeBOM)
				writeBOM();
			
			for (int i = 0; i < columns.size(); i++) {

				if (linebuf.length() > 0)
					linebuf.append(colsep);
				linebuf.append(columns.get(i));
			}
			getWriter().write(linebuf.toString());
			getWriter().newLine();
			headersWritten = true;
			linebuf = new StringBuilder();
		}

		for (int i = 0; i < columns.size(); i++) {

			Attribute a = entry.getAttribute(columns.get(i));

			if (i > 0)
				linebuf.append(colsep);

			if (a == null) {
				continue;
			}

			String val = a.getValue();
			if (val != null) {
				if (quoteEnabled
						&& (val.indexOf("\"") != -1
								|| val.indexOf(colsep) != -1
								|| val.indexOf("\n") != -1
								|| val.indexOf("\r") != -1 || quoteAll))
					linebuf.append(quote(val));
				else
					linebuf.append(val);
			}

		}
		getWriter().write(linebuf.toString());
		getWriter().newLine();
		getWriter().flush();
		Trace.exitmax(this, "writeEntry");
	}

	/**
	 * Quotes the provided expression.
	 * 
	 * @param src
	 *            source String
	 * @return the quoted expression
	 */
	public String quote(String src) {
		Trace.entrymax(this, "quote");
		StringBuilder buf = new StringBuilder();
		buf.append('"');
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) == '"') {
				buf.append("\"\"");
			} else {
				buf.append(src.charAt(i));
			}
		}
		buf.append('"');
		Trace.exitmax(this, "quote", buf);
		return buf.toString();

	}

	/**
	 * Sets if header should be written
	 * 
	 * @param value If false, the headers need to be written. If true, the headers are already written, and should not be written again.
	 */
	public void setHeadersWritten(boolean value) {
		headersWritten = value;
	}

	/**
	 * Writes the BOM to output file. 
	 */
	  private void writeBOM()throws Exception{    
			if(getWriter()!= null){
					getWriter().write("\uFEFF");		
			}
		    writeBOM = false;    
	  }

	/**
	 * Version information.
	 * 
	 * @return version information.
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * Sets all properties to the default values.
	 */
	private void resetProperties() {
		columns = null;
		colsep = ',';
		linebuf = null;
		headersWritten = false;
		linenumber = 0;
		quoteEnabled = true;
		longlines = 0;
		combineLastFields = false;
		quoteAll = false;
		writeBOM = false;
		prev = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object schema) {
		if (columns != null) {
			Vector<Entry> result = new Vector<Entry>();

			for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
				Entry entry = new Entry();
				entry.addAttributeValue("name", columns.get(colIndex));
				entry.addAttributeValue("syntax", "java.lang.String");
				result.add(entry);
			}

			return result;
		}

		return null;
	}

	/**
	 * Returns the last line that was read or written.
	 * If nothing has been read or written yet, returns null.
	 * @return the last line that was read or written
	 * @since 7.2
	 */
	public String getLine() {
		if (linebuf != null)
			return linebuf.toString();
		else
			return null;
	}
}
