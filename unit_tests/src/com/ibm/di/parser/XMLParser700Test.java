package com.ibm.di.parser;

import static junit.framework.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.test.utils.TestUtils;

public class XMLParser700Test {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Parsing_Of_Simple_XML_1() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry>" + "<telephoneNo>555-888-8888</telephoneNo>" + "<User>Jill Vox</User>"
				+ "</Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "ValueTag");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(2, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("telephoneNo");
		assertEquals(1, telephoneNo.size());
		assertEquals(1, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getValue());

		Attribute User = entry.getAttribute("User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Parsing_Of_Simple_XML_2() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry>" + "<telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag>" + "</telephoneNo>" + "<User>Jill Vox</User>" + "</Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "ValueTag");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(2, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("telephoneNo");
		assertEquals(2, telephoneNo.size());
		assertEquals(2, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getValue(0));
		assertEquals("555-999-9999", telephoneNo.getValue(1));

		Attribute User = entry.getAttribute("User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Parsing_Of_Simple_XML_3() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>" + "<ValueTag>555-999-9999</ValueTag>"
				+ "</telephoneNo>" + "<User>Jill Vox</User>" + "</Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(2, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("telephoneNo");
		assertEquals(0, telephoneNo.size());
		assertEquals(2, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getChildNodes().item(0).getNodeValue());
		assertEquals("555-999-9999", telephoneNo.getChildNodes().item(1).getNodeValue());

		Attribute User = entry.getAttribute("User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Parsing_Of_Simple_XML_4() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry xmlns=\"defaultNS\">" + "<telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag>" + "</telephoneNo>" + "<User>Jill Vox</User>" + "</Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "ValueTag");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(2, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("telephoneNo");
		assertEquals(2, telephoneNo.size());
		assertEquals(2, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getValue(0));
		assertEquals("555-999-9999", telephoneNo.getValue(1));

		Attribute User = entry.getAttribute("User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Parsing_Of_Advanced_XML_1() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "<User>Jill Vox</User></Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*/Entry");
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "ValueTag");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(1, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("Entry.telephoneNo");
		assertEquals(0, telephoneNo.size());
		assertEquals(2, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getChildNodes().item(0).getNodeValue());
		assertEquals("555-999-9999", telephoneNo.getChildNodes().item(1).getNodeValue());

		Attribute User = entry.getAttribute("Entry.User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Parsing_Of_Advanced_XML_2() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot><Entry>" + "<telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag>" + "</telephoneNo>" + "<User>Jill Vox</User>" + "</Entry></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*/Entry");
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "");

		parser.initParser();

		Entry entry = parser.readEntry();

		assertEquals(2, entry.size());
		assertEquals(1, entry.getChildNodes().getLength());
		assertEquals(true, entry.isDOMEnabled());

		Attribute telephoneNo = entry.getAttribute("Entry.telephoneNo");
		assertEquals(0, telephoneNo.size());
		assertEquals(2, telephoneNo.getChildNodes().getLength());
		assertEquals("555-888-8888", telephoneNo.getChildNodes().item(0).getNodeValue());
		assertEquals("555-999-9999", telephoneNo.getChildNodes().item(1).getNodeValue());

		Attribute User = entry.getAttribute("Entry.User");
		assertEquals(1, User.size());
		assertEquals(1, User.getChildNodes().getLength());
		assertEquals("Jill Vox", User.getValue());

		parser.closeParser();
	}

	@Test
	public void test_Serialization_Of_Simple_XML_1() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "ValueTag");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry();
		/*
		 * Enable DOM, so that the order of the attributes is deterministic and
		 * we can use string compare to verify the resulting XML text.
		 */
		e.enableDOM();
		e.setAttribute("User", "Jill Vox");
		e.setAttribute("telephoneNo", "555-888-8888");

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot><Entry><User>Jill Vox</User><telephoneNo>555-888-8888</telephoneNo></Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_Simple_XML_2() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "pref:ValueTag");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry();
		/*
		 * Enable DOM, so that the order of the attributes is deterministic and
		 * we can use string compare to verify the resulting XML text.
		 */
		e.enableDOM();
		e.setAttribute("User", "Jill Vox");
		e.setAttribute("telephoneNo", "555-888-8888");
		e.addAttributeValue("telephoneNo", "555-999-9999");;

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot><Entry><User>Jill Vox</User><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "</Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_Simple_XML_3() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry();

		e.newAttribute("telephoneNo").appendChild(new Attribute("ValueTag", "555-888-8888"));
		e.newAttribute("telephoneNo").appendChild(new Attribute("ValueTag", "555-999-9999"));
		e.setAttribute("User", "Jill Vox");

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot><Entry><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "<User>Jill Vox</User></Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_SimpleXML_4() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "DocRoot");
		parser.setParam("entry.tag", "Entry");
		parser.setParam("value.tag", "ValueTag");
		parser.setParam("static.decl", "<DocRoot xmlns=\"defaultNS\" />");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry(true);

		e.setAttribute("telephoneNo", "555-888-8888");
		e.addAttributeValue("telephoneNo", "555-999-9999");
		e.getAttribute("telephoneNo").setAttribute("xmlns", "defaultNS");

		e.setAttribute("User", "Jill Vox");
		e.getAttribute("User").setAttribute("xmlns", "defaultNS");

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot xmlns=\"defaultNS\"><Entry>" + "<telephoneNo><ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "<User>Jill Vox</User></Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_Advanced_XML_1() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "ValueTag");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry();

		e.newAttribute("Entry.telephoneNo").appendChild(new Attribute("ValueTag", "555-888-8888"));
		e.newAttribute("Entry.telephoneNo").appendChild(new Attribute("ValueTag", "555-999-9999"));
		e.setAttribute("Entry.User", "Jill Vox");

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot><Entry><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "<User>Jill Vox</User></Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_Advanced_XML_2() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "*");
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "false");

		parser.initParser();

		Entry e = new Entry();

		e.newAttribute("Entry.telephoneNo").appendChild(new Attribute("ValueTag", "555-888-8888"));
		e.newAttribute("Entry.telephoneNo").appendChild(new Attribute("ValueTag", "555-999-9999"));
		e.setAttribute("Entry.User", "Jill Vox");

		parser.writeEntry(e);
		parser.closeParser();

		String xml = out.toString();

		assertEquals("<DocRoot><Entry><telephoneNo>" + "<ValueTag>555-888-8888</ValueTag>"
				+ "<ValueTag>555-999-9999</ValueTag></telephoneNo>" + "<User>Jill Vox</User></Entry></DocRoot>", xml);
	}

	@Test
	public void test_Serialization_Of_Advanced_XML_When_Indention_Is_Turned_On() throws Exception {
		XMLParser2 parser = new XMLParser2();

		StringWriter out = new StringWriter();

		parser.setOutputStream(out);

		parser.setParam("characterSet", "UTF-8");
		parser.setParam("ns.map", "prefix=namespace");
		parser.setParam("xpath.expr", "root");
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "");
		parser.setParam("static.decl", "<root xmlns=\"myDefaultNS\" />");
		parser.setParam("omit.xml.decl.on.writing", "true");
		parser.setParam("indent.output", "true");
		parser.setParam("standalone.root", "false");

		parser.initParser();

		Entry e = new Entry(true);

		// pref1:Entry
		Attribute entryAttr = e.createElementNS("namespaceURI1", "pref1:Entry");
		e.appendChild(entryAttr);
		entryAttr.setAttribute("xmlns:pref1", "namespaceURI1");

		// Name
		Attribute temp = e.createElement("Name");
		temp.setValue("IBM Corporation");
		entryAttr.appendChild(temp);

		// Country
		Attribute countryAttr = e.createElement("Country");
		entryAttr.appendChild(countryAttr);

		// value
		temp = e.createElement("value");
		temp.setValue("USA");
		countryAttr.appendChild(temp);

		// value
		temp = e.createElement("value");
		temp.setValue("Norway");
		countryAttr.appendChild(temp);

		// value
		temp = e.createElement("value");
		temp.setValue("India");
		countryAttr.appendChild(temp);

		// value
		temp = e.createElement("value");
		temp.setValue("Bulgaria");
		countryAttr.appendChild(temp);

		parser.writeEntry(e);

		e = new Entry(true);

		// pref2:Entry
		entryAttr = e.createElementNS("namespaceURI2", "pref2:Entry");
		e.appendChild(entryAttr);
		entryAttr.setAttribute("xmlns:pref2", "namespaceURI2");

		// Name
		temp = e.createElement("Name");
		temp.setValue("Microsoft Corporation");
		entryAttr.appendChild(temp);

		// Country
		countryAttr = e.createElement("Country");
		entryAttr.appendChild(countryAttr);

		// value
		temp = e.createElement("value");
		temp.setValue("USA");
		countryAttr.appendChild(temp);

		// value
		temp = e.createElement("value");
		temp.setValue("India");
		countryAttr.appendChild(temp);

		parser.writeEntry(e);

		e = new Entry(true);

		// pref:Entry
		entryAttr = e.createElementNS("defaultNS", "pref:Entry");
		e.appendChild(entryAttr);
		entryAttr.setAttribute("xmlns:pref", "defaultNS");

		// Name
		temp = e.createElement("Name");
		temp.setValue("BigResearch");
		entryAttr.appendChild(temp);

		// Country
		countryAttr = e.createElement("Country");
		countryAttr.setValue("France");
		entryAttr.appendChild(countryAttr);

		parser.writeEntry(e);

		e = new Entry(true);

		// Entry
		entryAttr = e.createElementNS("defaultNS", "Entry");
		e.appendChild(entryAttr);
		entryAttr.setAttribute("xmlns", "defaultNS");

		// Country
		countryAttr = e.createElement("Country");
		entryAttr.appendChild(countryAttr);

		// value
		temp = e.createElement("value");
		temp.setValue("USA");
		countryAttr.appendChild(temp);

		// value
		temp = e.createElement("value");
		temp.setValue("UK");
		countryAttr.appendChild(temp);

		// Name
		temp = e.createElement("Name");
		temp.setValue("Google Corporation");
		entryAttr.appendChild(temp);

		parser.writeEntry(e);

		parser.closeParser();

		String xml = TestUtils.removeReturnCharacters(out.toString());

		String expected = "<root xmlns=\"myDefaultNS\">\n" + "\t<pref1:Entry xmlns:pref1=\"namespaceURI1\">\n"
				+ "\t\t<Name>IBM Corporation</Name>\n" + "\t\t<Country>\n" + "\t\t\t<value>USA</value>\n"
				+ "\t\t\t<value>Norway</value>\n" + "\t\t\t<value>India</value>\n" + "\t\t\t<value>Bulgaria</value>\n"
				+ "\t\t</Country>\n" + "\t</pref1:Entry>\n" + "\t<pref2:Entry xmlns:pref2=\"namespaceURI2\">\n"
				+ "\t\t<Name>Microsoft Corporation</Name>\n" + "\t\t<Country>\n" + "\t\t\t<value>USA</value>\n"
				+ "\t\t\t<value>India</value>\n" + "\t\t</Country>\n" + "\t</pref2:Entry>\n"
				+ "\t<pref:Entry xmlns:pref=\"defaultNS\">\n" + "\t\t<Name>BigResearch</Name>\n"
				+ "\t\t<Country>France</Country>\n" + "\t</pref:Entry>\n" + "\t<Entry xmlns=\"defaultNS\">\n" + "\t\t<Country>\n"
				+ "\t\t\t<value>USA</value>\n" + "\t\t\t<value>UK</value>\n" + "\t\t</Country>\n"
				+ "\t\t<Name>Google Corporation</Name>\n" + "\t</Entry>\n" + "</root>\n\n";

		assertEquals(expected, xml);
	}

	@Test
	public void test_Parsing_When_Default_Namespace_Is_Defined_On_Level_Higher_Than_The_Returned_Entry() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot xmlns=\"defaultNS\"><pref:SecondRoot xmlns:pref=\"namespaceURI\">"
				+ "<pref:Entry xmlns:firstPref=\"firstPrefNamespaceURI\" staticAttr=\"staticVal\">"
				+ "<firstPref:Name>IBM Corporation</firstPref:Name><firstPref:Country><pref:value>USA</pref:value>"
				+ "<pref:value>Norway</pref:value></firstPref:Country></pref:Entry></pref:SecondRoot></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("xpath.expr", "DocRoot/SecondRoot/*");

		parser.initParser();

		// the "defaultNS" namespace is defined on an element which is not
		// returned in the resultant entry, but the resultant entry is a child
		// of that element. The resultant entry has an attribute (staticAttr)
		// which belongs to the "defaultNS". Before fixed this call got the
		// parser broken with
		// javax.xml.stream.XMLStreamException: The namespace URI "defaultNS"
		// has not been bound to a prefix.
		Entry entry = parser.readEntry();

		assertNotNull(entry);

		parser.closeParser();
	}

	@Test
	public void test_Parsing_When_Prefixed_Namespace_Is_Defined_On_Level_Higher_Than_The_Returned_Entry() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<DocRoot xmlns=\"defaultNS\"><pref:SecondRoot xmlns:pref=\"namespaceURI\">"
				+ "<Entry xmlns:firstPref=\"firstPrefNamespaceURI\" pref:staticAttr=\"staticVal\">"
				+ "<firstPref:Name>IBM Corporation</firstPref:Name><firstPref:Country><pref:value>USA</pref:value>"
				+ "<pref:value>Norway</pref:value></firstPref:Country></Entry></pref:SecondRoot></DocRoot>";

		parser.setInputStream(xml);
		parser.setParam("xpath.expr", "DocRoot/SecondRoot/*");

		parser.initParser();

		// the "namespaceURI" namespace is defined on an element which is not
		// returned in the resultant entry, but the resultant entry is a child
		// of that element. The resultant entry has an attribute (staticAttr)
		// which belongs to the "namespaceURI". Before fixed this call got the
		// parser broken with
		// javax.xml.stream.XMLStreamException: The namespace URI "namespaceURI"
		// has not been bound to a prefix.
		Entry entry = parser.readEntry();

		assertNotNull(entry);

		parser.closeParser();
	}

	@Test
	public void test_Reading_Of_Empty_Element_Declaring_A_Namespace_That_Is_Redefined_In_Its_Sibling() throws Exception {
		XMLParser2 parser = new XMLParser2();

		String xml = "<PERSONGROUP xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ "    <PERSON>\r\n"
				+ "      <NAME xsi:nil=\"true\" />\r\n"
				+ "      <AGE xsi:nil=\"true\" />\r\n"
				+ "    </PERSON>\r\n" + "</PERSONGROUP>";

		parser.setInputStream(xml);
		parser.setParam("xpath.expr", "*/*");
		;
		parser.setParam("entry.tag", "");
		parser.setParam("value.tag", "*");
		parser.initParser();
		Entry entry = parser.readEntry();
		assertNotNull(entry);

		StringWriter out = new StringWriter();
		XMLParser2 writer = new XMLParser2();
		writer.setOutputStream(out);
		writer.setParam("xpath.expr", "*/*");
		writer.setParam("entry.tag", "");
		parser.setParam("value.tag", "*");
		writer.initParser();

		// work around
		// Attribute a = entry.getAttribute("PERSON");
		// a.setAttribute("xmlns:xsi",
		// "http://www.w3.org/2001/XMLSchema-instance");

		writer.writeEntry(entry);

		writer.closeParser();
		parser.closeParser();
	}
}