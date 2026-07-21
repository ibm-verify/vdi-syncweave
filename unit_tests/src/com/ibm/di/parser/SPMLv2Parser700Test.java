package com.ibm.di.parser;

import static junit.framework.Assert.*;

import java.io.StringWriter;
import java.util.Collection;

import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

public class SPMLv2Parser700Test {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Read_Entry() throws Exception {

		SPMLv2Parser parser = new SPMLv2Parser();
		parser
				.setInputStream("<spmlsearch:searchRequest xmlns:spml='urn:oasis:names:tc:SPML:2:0' xmlns:spmlsearch='urn:oasis:names:tc:SPML:2:0:search' xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
						+ "<spmlsearch:query scope='oneLevel'>"
						+ "<spml:basePsoID ID='CN=group,CN=localhost' targetID='localhost' />"
						+ "<filter xmlns='urn:oasis:names:tc:DSML:2:0:core'>"
						+ "<and>"
						+ "<extensibleMatch name='systemFlags' dnAttributes='true'>"
						+ "<value>1asa</value>"
						+ "</extensibleMatch>"
						+ "<or>"
						+ "<greaterOrEqual name='roomnumber'>"
						+ "<value>3000</value>"
						+ "</greaterOrEqual>"
						+ "<not>"
						+ "<and>"
						+ "<lessOrEqual name='roomnumber'>"
						+ "<value>2000</value>"
						+ "</lessOrEqual>"
						+ "<not>"
						+ "<substrings name='bin'>"
						+ "<initial>YQBh</initial>"
						+ "</substrings>"
						+ "</not>"
						+ ""
						+ "<equalityMatch name='objectCategory'>"
						+ "<value>organizationalUnit</value>"
						+ "</equalityMatch>"
						+ "</and>"
						+ "</not>"
						+ "<approxMatch name='cn'>"
						+ "<value>ooo</value>"
						+ "</approxMatch>"
						+ "</or>"
						+ "<equalityMatch name='objectCategory'>"
						+ "<value>contact</value>"
						+ "</equalityMatch>"
						+ "<present name='objectclass' />"
						+ "</and>"
						+ "</filter>"
						+ "<attributes xmlns='urn:oasis:names:tc:DSML:2:0:core'>"
						+ "<attribute name = 'cn' />"
						+ "<attribute name = 'sn' />" + "</attributes>" + "</spmlsearch:query>" + "</spmlsearch:searchRequest>");
		parser.initParser();

		Entry e = parser.readEntry();
		parser.closeParser();

		assertEquals(22, e.size());

		Attribute spml = e.getAttribute("spml");
		assertEquals(5, spml.getChildNodes().getLength());
		assertEquals(0, spml.size());

		Attribute scope = e.getAttribute("spml.scope");
		assertEquals(1, scope.getChildNodes().getLength());
		assertEquals(1, scope.size());
		assertEquals("oneLevel", scope.getValue());

		Attribute containerID = e.getAttribute("spml.containerID");
		assertEquals(1, containerID.size());
		assertEquals(2, containerID.getChildNodes().getLength());
		assertEquals("CN=group,CN=localhost", containerID.getValue());
		assertEquals("localhost", ((Attribute) containerID.getLastChild()).getValue());

		Attribute filter = e.getAttribute("spml.filter");
		assertEquals(1, filter.getChildNodes().getLength());
		assertEquals(0, filter.size());

		Attribute and = (Attribute) filter.getFirstChild();
		assertEquals(4, and.getChildNodes().getLength());
		assertEquals(0, and.size());

		Collection<String> names = e.getAttributeCollection();

		assertEquals(22, names.size());

		assertTrue("spml.operation.type", names.contains("spml.operation.type"));
		assertEquals("Request", e.getAttribute("spml.operation.type").getValue());

		assertTrue("spml.operation", names.contains("spml.operation"));
		assertEquals("Search", e.getAttribute("spml.operation").getValue());

		assertTrue("spml.scope", names.contains("spml.scope"));
		assertEquals("oneLevel", e.getAttribute("spml.scope").getValue());

		assertTrue("spml.containerID", names.contains("spml.containerID"));
		assertEquals("CN=group,CN=localhost", e.getAttribute("spml.containerID").getValue());

		assertTrue("spml.containerID.targetID", names.contains("spml.containerID.targetID"));
		assertEquals("localhost", e.getAttribute("spml.containerID.targetID").getValue());

		assertTrue("spml.filter.and.extensibleMatch.name", names.contains("spml.filter.and.extensibleMatch.name"));
		assertEquals("systemFlags", e.getAttribute("spml.filter.and.extensibleMatch.name").getValue());

		assertTrue("spml.filter.and.extensibleMatch.value", names.contains("spml.filter.and.extensibleMatch.value"));
		assertEquals("1asa", e.getAttribute("spml.filter.and.extensibleMatch.value").getValue());

		assertTrue("spml.filter.and.extensibleMatch.dnAttributes", names.contains("spml.filter.and.extensibleMatch.dnAttributes"));
		assertEquals("false", e.getAttribute("spml.filter.and.extensibleMatch.dnAttributes").getValue());

		assertTrue("spml.filter.and.or.greaterOrEqual.name", names.contains("spml.filter.and.or.greaterOrEqual.name"));
		assertEquals("roomnumber", e.getAttribute("spml.filter.and.or.greaterOrEqual.name").getValue());

		assertTrue("spml.filter.and.or.greaterOrEqual.value", names.contains("spml.filter.and.or.greaterOrEqual.value"));
		assertEquals("3000", e.getAttribute("spml.filter.and.or.greaterOrEqual.value").getValue());

		assertTrue("spml.filter.and.or.not.and.lessOrEqual.name", names.contains("spml.filter.and.or.not.and.lessOrEqual.name"));
		assertEquals("roomnumber", e.getAttribute("spml.filter.and.or.not.and.lessOrEqual.name").getValue());

		assertTrue("spml.filter.and.or.not.and.lessOrEqual.value", names.contains("spml.filter.and.or.not.and.lessOrEqual.value"));
		assertEquals("2000", e.getAttribute("spml.filter.and.or.not.and.lessOrEqual.value").getValue());

		assertTrue("spml.filter.and.or.not.and.not.substrings.name", names
				.contains("spml.filter.and.or.not.and.not.substrings.name"));
		assertEquals("bin", e.getAttribute("spml.filter.and.or.not.and.not.substrings.name").getValue());

		assertTrue("spml.filter.and.or.not.and.not.substrings.initial", names
				.contains("spml.filter.and.or.not.and.not.substrings.initial"));
		assertEquals("YQBh", e.getAttribute("spml.filter.and.or.not.and.not.substrings.initial").getValue());

		assertTrue("spml.filter.and.or.not.and.equalityMatch.name", names.contains("spml.filter.and.or.not.and.equalityMatch.name"));
		assertEquals("objectCategory", e.getAttribute("spml.filter.and.or.not.and.equalityMatch.name").getValue());

		assertTrue("spml.filter.and.or.not.and.equalityMatch.value", names
				.contains("spml.filter.and.or.not.and.equalityMatch.value"));
		assertEquals("organizationalUnit", e.getAttribute("spml.filter.and.or.not.and.equalityMatch.value").getValue());

		assertTrue("spml.filter.and.or.approxMatch.name", names.contains("spml.filter.and.or.approxMatch.name"));
		assertEquals("cn", e.getAttribute("spml.filter.and.or.approxMatch.name").getValue());

		assertTrue("spml.filter.and.or.approxMatch.value", names.contains("spml.filter.and.or.approxMatch.value"));
		assertEquals("ooo", e.getAttribute("spml.filter.and.or.approxMatch.value").getValue());

		assertTrue("spml.filter.and.equalityMatch.name", names.contains("spml.filter.and.equalityMatch.name"));
		assertEquals("objectCategory", e.getAttribute("spml.filter.and.equalityMatch.name").getValue());

		assertTrue("spml.filter.and.equalityMatch.value", names.contains("spml.filter.and.equalityMatch.value"));
		assertEquals("contact", e.getAttribute("spml.filter.and.equalityMatch.value").getValue());

		assertTrue("spml.filter.and.present.name", names.contains("spml.filter.and.present.name"));
		assertEquals("objectclass", e.getAttribute("spml.filter.and.present.name").getValue());

		assertTrue("spml.attributeDescription", names.contains("spml.attributeDescription"));
		assertEquals("cn", e.getAttribute("spml.attributeDescription").getValue(0));
		assertEquals("sn", e.getAttribute("spml.attributeDescription").getValue(1));
	}

	@Test
	public void test_Write_Entry() throws Exception {
		StringWriter sw = new StringWriter();
		SPMLv2Parser parser = new SPMLv2Parser();
		parser.setOutputStream(sw);
		parser.initParser();
		Attribute value = null;
		Attribute name = null;

		Entry e = new Entry();

		Attribute spml = e.newAttribute("spml.operation.type");
		spml.setValue("Request");

		Attribute containerID = e.newAttribute("spml.containerID");
		containerID.setValue("CN=group,CN=localhost");

		Attribute targetID = e.newAttribute("spml.containerID.targetID");
		targetID.setValue("localhost");

		Attribute operation = e.newAttribute("spml.operation");
		operation.setValue("Search");

		Attribute scope = e.newAttribute("spml.scope");
		scope.setValue("oneLevel");

		Attribute filter = e.newAttribute("spml.filter");

		Attribute andChild1 = new Attribute("and");
		Attribute extMatch = new Attribute("extensibleMatch");
		value = new Attribute("value");
		value.setValue("1");
		name = new Attribute("name");
		name.setValue("systemFlags");
		Attribute attr = new Attribute("dnAttributes");
		attr.setValue("true");
		extMatch.appendChild(value);
		extMatch.appendChild(name);
		extMatch.appendChild(attr);
		andChild1.appendChild(extMatch);

		Attribute notChild1 = new Attribute("not");
		Attribute orChild = new Attribute("or");
		Attribute greater = new Attribute("greaterOrEqual");
		value = new Attribute("value");
		value.setValue("3000");
		greater.appendChild(value);
		name = new Attribute("name");
		name.setValue("roomnumber");
		greater.appendChild(name);
		orChild.appendChild(greater);

		Attribute andChild2 = new Attribute("and");
		Attribute less = new Attribute("lessOrEqual");
		value = new Attribute("value");
		value.setValue("2000");
		less.appendChild(value);
		name = new Attribute("name");
		name.setValue("roomnumber");
		less.appendChild(name);
		andChild2.appendChild(less);

		Attribute notChild2 = new Attribute("not");
		Attribute substrings = new Attribute("substrings");
		Attribute initial = new Attribute("initial");
		initial.setValue("YQBh");
		substrings.appendChild(initial);
		name = new Attribute("name");
		name.setValue("bin");
		substrings.appendChild(name);
		notChild2.appendChild(substrings);
		andChild2.appendChild(notChild2);

		Attribute equalityMatch = new Attribute("equalityMatch");
		value = new Attribute("value");
		value.setValue("organisationalUnit");
		equalityMatch.appendChild(value);
		name = new Attribute("name");
		name.setValue("objectCategory");
		equalityMatch.appendChild(name);
		andChild2.appendChild(equalityMatch);
		notChild1.appendChild(andChild2);
		orChild.appendChild(notChild1);

		Attribute approxMatch = new Attribute("approxMatch");
		value = new Attribute("value");
		value.setValue("ooo");
		approxMatch.appendChild(value);
		name = new Attribute("name");
		name.setValue("cn");
		approxMatch.appendChild(name);
		orChild.appendChild(approxMatch);
		andChild1.appendChild(orChild);

		Attribute equalityMatch2 = new Attribute("equalityMatch");
		value = new Attribute("value");
		value.setValue("contact");
		equalityMatch2.appendChild(value);
		name = new Attribute("name");
		name.setValue("objectCategory");
		equalityMatch2.appendChild(name);
		andChild1.appendChild(equalityMatch2);

		Attribute present = new Attribute("present");
		name = new Attribute("name");
		name.setValue("objectclass");
		present.appendChild(name);
		andChild1.appendChild(present);

		filter.appendChild(andChild1);

		e.newAttribute("spml.attributeDescription").addValue("cn");
		e.newAttribute("spml.attributeDescription").addValue("sn");

		parser.writeEntry(e);
		parser.closeParser();

		String result = "<spmlbatch:batchRequest xmlns:spmlbatch=\"urn:oasis:names:tc:SPML:2:0:batch\"><spmlsearch:searchRequest xmlns='urn:oasis:names:tc:SPML:2:0' xmlns:spmlsearch='urn:oasis:names:tc:SPML:2:0:search'>\n"
				+ "  <spmlsearch:query scope='oneLevel'>\n"
				+ "    <dsml:filter xmlns:dsml='urn:oasis:names:tc:DSML:2:0:core'>\n"
				+ "      <dsml:and>\n"
				+ "        <dsml:extensibleMatch name='systemFlags' dnAttributes='true'>\n"
				+ "          <dsml:value>1</dsml:value>\n"
				+ "        </dsml:extensibleMatch>\n"
				+ "        <dsml:or>\n"
				+ "          <dsml:greaterOrEqual name='roomnumber'>\n"
				+ "            <dsml:value>3000</dsml:value>\n"
				+ "          </dsml:greaterOrEqual>\n"
				+ "          <dsml:not>\n"
				+ "            <dsml:and>\n"
				+ "              <dsml:lessOrEqual name='roomnumber'>\n"
				+ "                <dsml:value>2000</dsml:value>\n"
				+ "              </dsml:lessOrEqual>\n"
				+ "              <dsml:not>\n"
				+ "                <dsml:substrings name='bin'>\n"
				+ "                  <dsml:initial>YQBh</dsml:initial>\n"
				+ "                </dsml:substrings>\n"
				+ "              </dsml:not>\n"
				+ "              <dsml:equalityMatch name='objectCategory'>\n"
				+ "                <dsml:value>organisationalUnit</dsml:value>\n"
				+ "              </dsml:equalityMatch>\n"
				+ "            </dsml:and>\n"
				+ "          </dsml:not>\n"
				+ "          <dsml:approxMatch name='cn'>\n"
				+ "            <dsml:value>ooo</dsml:value>\n"
				+ "          </dsml:approxMatch>\n"
				+ "        </dsml:or>\n"
				+ "        <dsml:equalityMatch name='objectCategory'>\n"
				+ "          <dsml:value>contact</dsml:value>\n"
				+ "        </dsml:equalityMatch>\n"
				+ "        <dsml:present name='objectclass'/>\n"
				+ "      </dsml:and>\n"
				+ "    </dsml:filter>\n"
				+ "    <dsml:attributes xmlns:dsml='urn:oasis:names:tc:DSML:2:0:core'>\n"
				+ "      <dsml:attribute name='cn'/>\n"
				+ "      <dsml:attribute name='sn'/>\n"
				+ "    </dsml:attributes>\n"
				+ "    <spmlsearch:basePsoID ID='CN=group,CN=localhost' targetID='localhost'/>\n"
				+ "  </spmlsearch:query>\n"
				+ "</spmlsearch:searchRequest>\n" + "</spmlbatch:batchRequest>";

		String ourResult = sw.toString();
		assertEquals(result, ourResult);
	}
}