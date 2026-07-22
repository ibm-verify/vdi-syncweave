/*
 * IBM Confidential
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

import static junit.framework.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import example_parser.ExampleParser;

public class ExampleParserTest {

	@Test
	public void testRead() throws Exception {
		ExampleParser parser = new ExampleParser();

		String input = "This is Line 1\n" + "This is Line2";

		parser.setInputStream(input);
		parser.setParam("attributeName", "Lines");

		parser.initParser();

		Entry entry = parser.readEntry();
		parser.closeParser();

		assertEquals(1, entry.size());
		assertEquals("This is Line 1", entry.getString("Lines"));
	}

	@Test
	public void testWrite() throws Exception {
		ExampleParser parser = new ExampleParser();

		StringWriter output = new StringWriter();

		parser.setOutputStream(output);
		parser.setParam("attributeName", "Lines");
		parser.initParser();

		Entry entry = new Entry();
		Attribute attribute = new Attribute("Lines", "This is Line 1");
		entry.setAttribute(attribute);
		
		parser.writeEntry(entry);
	
		assertEquals(1, entry.size());
		assertEquals("This is Line 1", output.toString().trim());
	}
}
