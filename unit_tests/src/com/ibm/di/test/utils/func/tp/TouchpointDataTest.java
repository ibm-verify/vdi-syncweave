package com.ibm.di.test.utils.func.tp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Property;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.util.DOMUtils;

public class TouchpointDataTest {
	
	@Test
	public void test_entryToXml_xmlToEntry() throws Exception {
		
		Entry e = new Entry();
		
		final String NS_EXAMPLE = "http://www.example.com"; 
		Attribute book = e.createElementNS(NS_EXAMPLE, "e:book");

		Attribute title = e.createElementNS(NS_EXAMPLE, "e:title");
		title.addValue("My Book");
		title.setAttributeNS("http://www.ibm.com", "i:style", "bold");
		
		Attribute date = e.createElementNS(NS_EXAMPLE, "e:date");
		date.addValue(new java.util.Date());
		
		book.appendChild(e.createCDATASection("mycdata"));
		book.appendChild(title);
		book.appendChild(e.createTextNode("mytext"));
		book.appendChild(date);
		book.appendChild(e.createTextNode("myothertext"));
		
		e.appendChild(book);
		
		Document doc = DOMUtils.getDOMParser().newDocument();
		
		Element entryElem = TouchpointData.entryToXml(doc, e);
		
		Entry ee = TouchpointData.xmlToEntry(entryElem);
		
		assertEqualEntries(e, ee);
	}
	
	@Test
	public void test_flat_attribute_with_no_values() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.newAttribute("myattr");
		
		test_attribute(attr);
	}
	
	@Test
	public void test_flat_attribute_with_single_value() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.newAttribute("myattr");
		attr.addValue(new java.util.Date());
		
		test_attribute(attr);
	}
	
	@Test
	public void test_flat_attribute_with_three_values() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.newAttribute("myattr");
		attr.addValue(new java.util.Date());
		attr.addValue("myvalue");
		attr.addValue(BigInteger.TEN);
		
		test_attribute(attr);
	}
	
	@Test
	public void test_flat_attribute_with_dot_in_name() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.newAttribute("http.body");
		attr.addValue(new java.util.Date());
		
		test_attribute(attr);
	}
	
	@Test
	public void test_flat_attribute_with_dollar_sign_in_name() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.newAttribute("$dn");
		attr.addValue("cn=root");
		
		test_attribute(attr);
	}
	
	@Test
	public void test_attribute_with_property() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.createElementNS("nsuri", "t:parent");
		attr.setAttributeNS("propnsuri", "x:prop", "v");
		
		test_attribute(attr);
	}
	
	@Test
	public void test_attribute_with_child_with_no_values() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.createElementNS("nsuri", "t:parent");
		Attribute child = entry.createElementNS("nsuriother", "t:child");
		attr.appendChild(child);
		
		test_attribute(attr);
	}
	
	@Test
	public void test_attribute_with_values_with_child_with_no_values() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.createElementNS("nsuri", "t:parent");
		Attribute child = entry.createElementNS("nsuriother", "t:child");
		attr.addValue(BigInteger.TEN);
		attr.appendChild(child);
		
		test_attribute(attr);
	}
	
	@Test
	public void test_attribute_with_values_with_children_with_values() throws Exception {
		
		Entry entry = new Entry();
		Attribute attr = entry.createElementNS("nsuri", "t:parent");
		
		Attribute childone = entry.createElementNS("urione", "t:childone");
		childone.addValue(new java.util.Date());
		
		Attribute childtwo = entry.createElementNS("uritwo", "tt:childtwo");
		childtwo.addValue(new Object());
		
		attr.addValue(BigInteger.ZERO);
		attr.appendChild(childone);
		attr.addValue(BigInteger.ONE);
		attr.appendChild(childtwo);
		attr.addValue(BigInteger.TEN);
		
		test_attribute(attr);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_name_is_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.newAttribute("myattr");
		Attribute attrOther = entry.newAttribute("myattr2");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_namespace_is_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns1", "myattr");
		Attribute attrOther = entry.createElementNS("ns2", "myattr");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_value_count_is_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.addValue("one");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.addValue("one");
		attrOther.addValue("two");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_values_are_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.addValue("one");
		attr.addValue("2");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.addValue("one");
		attrOther.addValue("two");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_property_namespaces_are_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.setAttributeNS("ns", "prop", "1");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.setAttribute("prop", "1");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_property_names_are_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.setAttribute("prop1", "1");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.setAttribute("prop2", "1");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_property_values_are_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.setAttribute("prop", "1");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.setAttribute("prop", "2");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_assertEqualAttributes_fail_when_property_count_is_different() throws Exception {
		
		Entry entry = new Entry();
		
		Attribute attr = entry.createElementNS("ns", "myattr");
		attr.setAttribute("prop", "1");
		attr.setAttribute("prop2", "1");
		
		Attribute attrOther = entry.createElementNS("ns", "myattr");
		attrOther.setAttribute("prop", "2");
		
		test_assertEqualAttributes_fail(attr, attrOther);
	}
	
	@Test
	public void test_propertyToXml_simple_property() throws Exception {
		
		final String name = "myprop";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_with_namespace_and_no_prefix() throws Exception {
		
		final String name = "myprop";
		final String namespaceURI = "myns";
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_with_namespace_and_prefix() throws Exception {
		
		final String name = "myprefix:myprop";
		final String namespaceURI = "myns";
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_value_with_non_letter_characters() throws Exception {
		
		final String name = "myprefix:myprop";
		final String namespaceURI = "myns";
		final String value = "\n\t\t\r\r$\r$\t@&^*() < > \r\n {}-+_=!##\n\t";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_value_is_empty_string() throws Exception {
		
		final String name = "myprop";
		final String namespaceURI = "myns";
		final String value = "";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_name_starting_with_whitespace() throws Exception {
		
		final String name = "\r\n  \tmyprop";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_name_ending_with_whitespace() throws Exception {
		
		final String name = "myprop \t \r\n";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_name_starting_with_dollar_sign() throws Exception {
		
		final String name = "$myprop";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_name_starting_with_num_sign() throws Exception {
		
		final String name = "#myprop";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	@Test
	public void test_propertyToXml_property_name_starting_with_ampersand() throws Exception {
		
		final String name = "&myprop";
		final String namespaceURI = null;
		final String value = "myvalue";
		
		test_propertyToXml(name, namespaceURI, value);
	}
	
	private void test_assertEqualAttributes_fail(Attribute attr, Attribute attrOther) throws Exception {
		boolean complained = false;
		try {
			assertEqualAttributes(attr, attrOther);
		} catch (AssertionError expected) {
			complained = true;
		}
		assertTrue(complained);
	}
	
	private void test_attribute(Attribute attr) throws Exception {
		
		Document doc = DOMUtils.getDOMParser().newDocument();
		Entry entry = new Entry();
		
		Element attrElem = TouchpointData.attributeToXml(doc, attr);
		
		String xml = DOMUtils.elementToString(attrElem);
		
		Element otherAttrElem = DOMUtils.parseString(xml);
		Attribute otherAttr = TouchpointData.xmlToAttribute(entry, otherAttrElem);
		
		assertEqualAttributes(attr, otherAttr);
	}
	
	private void test_propertyToXml(String name, String namespaceURI, String value) throws Exception {
		
		Document doc = DOMUtils.getDOMParser().newDocument();
		
		Property prop = new Property(name, namespaceURI, value);
		
		Element elem = TouchpointData.propertyToXml(doc, prop);
		
		// DOM implementation must be comfortable with our format
		String xml = DOMUtils.elementToString(elem);
		DOMUtils.parseString(xml);
		
		assertEquals("property", elem.getLocalName());
		assertEquals(TouchpointData.NS_TP, elem.getNamespaceURI());
		assertEquals(name, elem.getAttribute("name"));
		
		if (namespaceURI != null) {
			assertEquals(namespaceURI, elem.getAttribute("namespaceURI"));
		} else {
			assertTrue(elem.getAttribute("namespaceURI") == null || elem.getAttribute("namespaceURI").length() == 0);
		}
		
		assertEquals(value, elem.getTextContent());
	}
	
	private static void assertEqualEntries(Entry expected, Entry actual) {
		
		// check properties
		String[] expectedPropertyNames = expected.getPropertyNames();
		String[] actualPropertyNames = actual.getPropertyNames();
		TestUtils.compareLists(expectedPropertyNames, actualPropertyNames);
		for (String propName : expectedPropertyNames) {
			assertEquals(expected.getProperty(propName), actual.getProperty(propName));
		}
		
		// check attributes
		NodeList nodeListExpected = expected.getChildNodes();
		NodeList nodeListActual = actual.getChildNodes();
		
		assertEquals(nodeListExpected.getLength(), nodeListActual.getLength());
		
		for (int i = 0; i < nodeListExpected.getLength(); ++i) {
			Attribute attrExpected = (Attribute) nodeListExpected.item(i);
			Attribute attrActual = (Attribute) nodeListActual.item(i);
			assertEqualAttributes(attrExpected, attrActual);
		}
	}
	
	private static void assertEqualAttributes(Attribute expected, Attribute actual) {
		
		// check the name
		assertEquals(expected.getLocalName(), actual.getLocalName());
		assertEquals(expected.getNamespaceURI(), actual.getNamespaceURI());
		
		// check properties
		NamedNodeMap expectedProps = expected.getAttributes();
		NamedNodeMap actualProps = actual.getAttributes();
		assertEquals(expectedProps.getLength(), actualProps.getLength());
		for (int i = 0; i < expectedProps.getLength(); ++i) {
			Property propExpected = (Property) expectedProps.item(i);
			Property propActual;
			if (propExpected.getNamespaceURI() != null && propExpected.getNamespaceURI().length() > 0) {
				propActual = (Property) actualProps.getNamedItemNS(propExpected.getNamespaceURI(), propExpected.getLocalName());
			} else {
				propActual = (Property) actualProps.getNamedItem(propExpected.getNodeName());
			}
			assertEqualProperties(propExpected, propActual);
		}
		
		// check child attributes and values
		NodeList nodeListExpected = expected.getChildNodes();
		NodeList nodeListActual = actual.getChildNodes();
		
		assertEquals(nodeListExpected.getLength(), nodeListActual.getLength());
		
		for (int i = 0; i < nodeListExpected.getLength(); ++i) {
			
			Node nodeExpected = nodeListExpected.item(i);
			Node nodeActual = nodeListActual.item(i);
			
			if (Node.ELEMENT_NODE == nodeExpected.getNodeType()) {
				assertEquals(nodeExpected.getNodeType(), nodeActual.getNodeType());
				assertEqualAttributes((Attribute) nodeExpected, (Attribute) nodeActual);
			} else {
				assertEquals(nodeExpected.getTextContent(), nodeActual.getTextContent());
			}
		}
	}
	
	private static void assertEqualProperties(Property expected, Property actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		assertEquals(expected.getLocalName(), actual.getLocalName());
		assertEquals(expected.getNamespaceURI(), actual.getNamespaceURI());
		assertEquals(expected.getValue(), actual.getValue());
	}
	
	

}
