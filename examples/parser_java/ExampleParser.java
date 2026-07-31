/*
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
package example_parser;

import java.io.IOException;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.ParserImpl;

/**
 * This is an example parser that reads or writes lines from files. Each line
 * is presented as an Attribute where the name of the attribute is set
 * in the configuration of the Parser in the Configuration Editor.
 */

public class ExampleParser extends ParserImpl {

	/**
	 * An example version of the parser.
	 */
	private static final String VERSION = "0";

	/**
	 * The name of the Attribute of the Entry.
	 */
	private String attrName;

	/**
	 * This method is called by the hosting component (e.g. connector) to
	 * initialize the parser. Here only the name of the Attribute needs to be
	 * initialized.
	 */
	public void initParser() throws IOException {
		String str = "";

		str = getParam("attributeName");
		if (str != null && str.trim().length() != 0) {
			attrName = str;
		} else {
			attrName = "AttributeName";
		}
	}

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception IOException
	 *                if error during reading next Entry occurs.
	 */
	public Entry readEntry() throws IOException {
		Entry e = new Entry();
		String value = getReader().readLine();

		if (value == null)
			return null;

		e.setAttribute(attrName, value);

		return e;
	}

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception Exception
	 */
	public void writeEntry(Entry entry) throws Exception {
		Attribute attr1 = entry.getAttribute(attrName);

		String str;
		str = attr1.getValue();

		getWriter().write(str);
		getWriter().newLine();
		getWriter().flush();
	}

	/**
	 * Flush any in-memory data to the current output stream. This method is
	 * called by some hosting component.
	 */
	public void flush() throws Exception {
		getWriter().flush();
	}

	/**
	 * Version information.
	 * 
	 * @return version information.
	 */
	public String getVersion() {
		return VERSION;
	}
}
