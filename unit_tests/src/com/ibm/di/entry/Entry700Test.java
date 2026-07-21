package com.ibm.di.entry;

import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.di.exceptions.DOMException;

public class Entry700Test extends Entry611Test {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String hierarchy;
	private String[] prefixes;
	private String[] localNames;

	@Before
	public void setUp() {
		hierarchy = "p1:a.p2:b.c.d.p3:e.f.g.h";
		prefixes = new String[] { "p1", "p2", null, null, "p3", null, null, null };
		localNames = new String[] { "a", "b", "c", "d", "e", "f", "g", "h" };
	}

	@Test
	public void test_Constructor() {
		Entry e = new Entry();
		assertNotNull(e);
		assertEquals("Initial Size", e.size(), 0);
		assertEquals("Initial Props Size", e.getPropertyNames().length, 0);
		assertEquals("Initial Oper", e.getOp(), Entry.OP_GEN);
		assertEquals("Initial Operation", e.getOperation(), Entry.OP_GEN2);
	}

	@Test
	public void test_Entry_Cloning() {
		Entry e1 = new Entry();
		e1.setProperty("p1", "val1");
		e1.newAttribute(hierarchy);
		assertEquals(e1.size(), 1);

		Entry e2 = new Entry();
		e2.setProperty("p2", "val2");
		e2.newAttribute(hierarchy, Attribute.ATTRIBUTE_ADD);
		assertEquals(1, e2.size());

		assertTrue(e1.equals(e1));
		assertTrue(e1.isEqualNode(e1));
		assertFalse(e1.equals(e2));
		assertFalse(e1.isEqualNode(e2));

		// start cloning...
		// deep
		Entry d1 = e1.clone();
		Entry d2 = e2.clone();

		// shallow
		Entry s1 = e1.cloneNode(false);
		Entry s2 = e2.cloneNode(false);

		// check depth...
		int depth = 0;
		depth = getLevelDepth(d1.getFirstChild());
		assertEquals(8, depth);

		depth = getLevelDepth(d2.getFirstChild());
		assertEquals(8, depth);

		depth = getLevelDepth(s1.getFirstChild());
		assertEquals(8, depth);

		depth = getLevelDepth(s2.getFirstChild());
		assertEquals(8, depth);

		assertNotNull(d1);
		assertNotNull(d2);
		assertNotNull(s1);
		assertNotNull(s2);

		assertFalse(e1.isSameNode(d1));
		assertFalse(s1.isSameNode(d1));
		assertFalse(e1.isSameNode(s1));

		assertFalse(e2.isSameNode(d2));
		assertFalse(s2.isSameNode(d2));
		assertFalse(e2.isSameNode(s2));

		assertTrue(e1.isEqualNode(d1));
		assertTrue(e1.isEqualNode(s1));
		assertTrue(s1.isEqualNode(d1));

		assertTrue(e2.isEqualNode(s2));
		assertTrue(e2.isEqualNode(d2));
		assertTrue(s2.isEqualNode(d2));

		assertFalse(e1.equals(e2));
		assertFalse(e1.equals(d2));
		assertFalse(e1.equals(s2));

		assertFalse(d1.equals(e2));
		assertFalse(d1.equals(d2));
		assertFalse(d1.equals(s2));

		assertFalse(s1.equals(e2));
		assertFalse(s1.equals(d2));
		assertFalse(s1.equals(s2));

		assertFalse(e1.isEqualNode(e2));
		assertFalse(e1.isEqualNode(d2));
		assertFalse(e1.isEqualNode(s2));

		assertFalse(d1.isEqualNode(e2));
		assertFalse(d1.isEqualNode(d2));
		assertFalse(d1.isEqualNode(s2));

		assertFalse(s1.isEqualNode(e2));
		assertFalse(s1.isEqualNode(d2));
		assertFalse(s1.isEqualNode(s2));
	}

	private int getLevelDepth(Attribute root) {
		int level = 0;
		while (root != null) {
			root = (Attribute) root.getFirstChild();
			level++;
		}

		return level;
	}

	@Test
	public void test_Find_Or_Create_New_Attribute() {
		Entry e1 = new Entry();
		e1.newAttribute(hierarchy);

		assertEquals(e1.size(), 1);

		Attribute current = e1.getFirstChild();
		assertEquals(localNames[0], current.getLocalName());
		current = (Attribute) current.getFirstChild();

		for (int i = 1; i < localNames.length; i++) {

			if (i != localNames.length - 1) {
				assertEquals(current.getChildNodes().getLength(), 1);
			}

			assertEquals(prefixes[i], current.getPrefix());
			assertEquals(localNames[i], current.getLocalName());

			current = (Attribute) current.getFirstChild();
		}
	}

	@Test
	public void test_Get_And_Set_Attribute() {
		Entry e = new Entry();

		// creating new attribute...
		e.setAttribute("a", "val1");

		assertNotNull(e.getAttribute("a"));
		assertNotNull(e.getAttribute("A"));
		assertTrue(e.getAttributeNames().length == 1);
		assertTrue(e.getAttributeCollection().size() == 1);
		assertTrue(e.getChildNodes().getLength() == 1);
		assertEquals(e.getAttribute("a").getValue(), "val1");
		assertEquals(e.getAttribute("A").getValue(), "val1");

		// replacing the new attribute...
		e.setAttribute("a", "val2");

		assertEquals(e.getAttribute("a").getValue(), "val2");
		assertEquals(e.getAttribute("A").getValue(), "val2");
		assertEquals(1, e.getChildNodes().getLength());

		// replacing the new attribute...
		e.setAttribute("A", "val3");

		assertEquals(e.getAttribute("a").getValue(), "val3");
		assertEquals(e.getAttribute("A").getValue(), "val3");
		assertEquals(1, e.getChildNodes().getLength());

		// removing the new attribute...
		e.setAttribute("a", null);

		assertNull(e.getAttribute("a"));
		assertNull(e.getAttribute("A"));
		assertEquals(0, e.getChildNodes().getLength());

		// creating a new attribute... replacing the old one... and changing the
		// name of the new to the name of the old one...
		e.setAttribute("a", new Attribute("sss"));

		assertEquals(e.getAttribute("a").getValue(), null);
		assertEquals(e.getAttribute("A").getValue(), null);
		assertNotSame("sss", e.getAttribute("a").getName());
		assertEquals(1, e.getChildNodes().getLength());

		// adding a new attribute...
		e.setAttribute(new Attribute("sss"));

		assertNotNull(e.getAttribute("sss"));
		assertEquals("sss", e.getAttribute("sss").getName());
		assertEquals(2, e.getChildNodes().getLength());

		// clear the entry
		e.removeAllAttributes();
		assertEquals(0, e.getChildNodes().getLength());
		assertEquals(0, e.getAttributeNames().length);
		assertEquals(0, e.getAttributeCollection().size());

		// creating new protected attribute...
		e.setAttribute("a", "val1", true);

		assertNotNull(e.getAttribute("a"));
		assertNotNull(e.getAttribute("A"));
		assertEquals(1, e.getAttributeNames().length);
		assertEquals(1, e.getAttributeCollection().size());
		assertEquals(1, e.getChildNodes().getLength());
		assertEquals("val1", e.getAttribute("a").getValue());
		assertEquals("val1", e.getAttribute("A").getValue(), "val1");
		assertEquals(true, e.getAttribute("a").getProtected());

		// replacing the new attribute...
		e.setAttribute("a", "val2", false);

		assertEquals("val2", e.getAttribute("a").getValue());
		assertEquals("val2", e.getAttribute("A").getValue());
		assertEquals(false, e.getAttribute("a").getProtected());
		assertEquals(1, e.getChildNodes().getLength());

		// replacing the new attribute...
		e.setAttribute("A", "val3", true);

		assertEquals(e.getAttribute("a").getValue(), "val3");
		assertEquals(e.getAttribute("A").getValue(), "val3");
		assertEquals(e.getAttribute("a").getProtected(), true);
		assertTrue(e.getChildNodes().getLength() == 1);

		// setAttributeValues... testing...

		// remove the last attribute...
		e.setAttributeValues("a", null);

		assertEquals(e.getChildNodes().getLength(), 0);
		assertTrue(e.getAttributeNames().length == 0);
		assertTrue(e.getAttributeCollection().size() == 0);

		// test with attributes...
		Attribute attr = new Attribute("b");
		attr.addValue(new Attribute("c1"));
		attr.addValue(new Attribute("c2"));

		e.setAttributeValues("a", attr);

		assertEquals(1, e.getAttributeNames().length);
		assertEquals(1, e.getAttributeCollection().size());
		assertEquals(1, e.getChildNodes().getLength());

		assertEquals("a", e.getFirstChild().getLocalName());
		assertEquals(2, e.getFirstChild().size());

		// add some list values.
		List<Object> vals = new ArrayList<Object>(8);
		vals.add(new Object());
		vals.add(new Object());
		vals.add(new Object());
		vals.add(new Object());
		vals.add(new Object());

		e.setAttributeValues("x", vals);

		assertTrue(e.getAttributeNames().length == 2);
		assertTrue(e.size() == 2);
		assertEquals(e.getChildNodes().getLength(), 2);

		assertEquals("x", e.getAttribute("x").getLocalName());
		assertEquals(5, e.getAttribute("x").size());
		assertEquals(5, e.getAttribute("x").getChildNodes().getLength());

		vals.clear();
		attr = new Attribute("o");
		attr.addValue(new Object());
		attr.addValue(new Object());
		vals.add(attr);

		attr = new Attribute("p");
		attr.addValue(new Object());
		attr.addValue(new Object());
		vals.add(attr);

		attr = new Attribute("q");
		attr.addValue(new Object());
		attr.addValue(new Object());
		vals.add(attr);

		vals.add(new Object());
		vals.add(new Object());

		assertEquals(5, e.getAttribute("x").size());
		assertEquals(5, e.getAttribute("x").getChildNodes().getLength());

		e.setAttributeValues("x", vals);

		assertEquals(8, e.getAttribute("x").size());
		assertEquals(8, e.getAttribute("x").getChildNodes().getLength());

		// addAttributeValue testing...

		e.addAttributeValue("x", "string");
		e.addAttributeValue("x", new Attribute("attr"));

		assertEquals(10, e.getAttribute("x").size());
		Assert.assertEquals(10, e.getAttribute("x").getChildNodes().getLength());

		assertNull(e.getAttribute("y"));

		e.addAttributeValue("y", "string1");

		assertNotNull(e.getAttribute("y"));
		assertEquals(1, e.getAttribute("y").size());

		e.addAttributeValue("y", "string2");

		assertEquals(2, e.getAttribute("y").size());

		assertNull(e.getAttribute("z"));

		e.addAttributeValue("z", "string1", AttributeValue.AV_UNDEFINED);

		assertNotNull(e.getAttribute("z"));
		assertEquals(1, e.getAttribute("z").size());
		assertEquals(((AttributeValue) e.getAttribute("z").getValueAV(0)).getOper(), AttributeValue.AV_UNDEFINED);

		e.addAttributeValue("z", "string2", AttributeValue.AV_DELETE);

		assertEquals(2, e.getAttribute("z").size());
		assertEquals(((AttributeValue) e.getAttribute("z").getValueAV(1)).getOper(), AttributeValue.AV_DELETE);
	}

	@Test
	public void test_Remove_Attribute() {
		Entry e = new Entry();

		e.setAttribute("a", "val");

		assertNotNull(e.getAttribute("a"));
		assertTrue(e.getChildNodes().getLength() == 1);

		e.removeAttribute("a");

		assertNull(e.getAttribute("a"));
		assertTrue(e.getChildNodes().getLength() == 0);

		e.setAttribute("a", "val");

		assertNotNull(e.getAttribute("A"));
		assertTrue(e.getChildNodes().getLength() == 1);

		e.removeAttribute("A");

		assertNull(e.getAttribute("a"));
		assertTrue(e.getChildNodes().getLength() == 0);

		e.newAttribute("a");
		e.newAttribute("b");
		e.newAttribute("c.d.e");
		e.newAttribute("zzz");

		assertEquals(4, e.size());
		assertEquals(4, e.getChildNodes().getLength());

		e.removeAllAttributes();

		assertEquals(0, e.size());
		assertEquals(0, e.getChildNodes().getLength());
	}

	@Test
	public void test_Remove_Attribute_Extra() {
		Entry e = new Entry();

		e.setAttribute("a.b.c", "c");
		e.setAttribute("a.b", "b");
		e.setAttribute("a.b.c.d", "d");

		e.removeAttribute("a.b.c.d");
		assertNotNull(e.getAttribute("a.b.c"));

		e.removeAttribute("a.b");
		assertNull(e.getAttribute("a.b"));

		e.removeAttribute("a.b.c");
		assertEquals(0, e.size());
	}

	@Test
	public void test_Properties() {
		Entry e = new Entry();

		e.setProperty("a", "b");

		assertTrue(e.hasProperty("a"));
		assertTrue(e.getPropertyNames().length == 1);
		assertEquals(e.getProperty("a"), "b");

		e.setProperty("a", "c");

		assertTrue(e.hasProperty("a"));
		assertTrue(e.getPropertyNames().length == 1);
		assertEquals(e.getProperty("a"), "c");

		e.setProperty("a", null);

		assertFalse(e.hasProperty("a"));
		assertTrue(e.getPropertyNames().length == 0);
		assertNull(e.getProperty("a"));
	}

	@Test
	public void test_Merge_Attributes() {
		Entry dest = new Entry();

		dest.setProperty("prop1", "val1");
		dest.setProperty("prop2", "val3");
		dest.enableDOM();

		Attribute temp = dest.newAttribute("a.b.c.d");

		// d
		temp.setValue("val");

		// c
		temp = temp.getParentNode();
		temp.addValue("val1");

		// a
		temp = temp.getParentNode().getParentNode();
		temp.addValue(0, "val7");

		temp.appendChild(new Attribute("p:b"));
		((Attribute) temp.getLastChild()).setValue("val3");

		Entry src = new Entry();
		src.enableDOM();

		src.setProperty("prop2", "val2");

		// c
		temp = src.newAttribute("a.b.c");

		temp.addValue("val1");
		temp.addValue("val2");

		// b
		temp = temp.getParentNode();
		temp.addValue("val4");

		// a
		temp = temp.getParentNode();
		temp.appendChild(new Attribute("p:b"));
		temp.addValue("val6");

		// p:b
		temp = (Attribute) temp.getLastChild().getPreviousSibling();
		temp.appendChild(new Attribute("c"));

		// c
		temp = (Attribute) temp.getLastChild();
		temp.addValue("val5");

		// override values...
		Entry z = src.clone();
		z.merge(dest);

		assertEquals(4, z.size());
		assertEquals(2, z.getPropertyNames().length);
		assertEquals(z.getProperty("prop2"), "val3");

		// a
		temp = z.getFirstChild();
		assertNotNull(temp);
		assertEquals(3, temp.getChildNodes().getLength());
		assertEquals("a", temp.getNodeName());
		assertEquals("val7", temp.getValue(0));

		// b
		temp = (Attribute) temp.getFirstChild();
		assertNotNull(temp);
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals("b", temp.getNodeName());

		// p:b
		temp = (Attribute) temp.getParentNode().getLastChild();
		assertNotNull(temp);
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals("p:b", temp.getNodeName());
		assertEquals("val3", temp.getValue());

		// c
		temp = (Attribute) temp.getParentNode().getFirstChild().getFirstChild();
		assertNotNull(temp);
		assertEquals(2, temp.getChildNodes().getLength());
		assertEquals("c", temp.getNodeName());
		assertEquals(1, temp.size());
		assertEquals("val1", temp.getValue(0));

		// d
		temp = (Attribute) temp.getFirstChild();
		assertNotNull(temp);
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals("d", temp.getNodeName());
		assertEquals("val", temp.getValue());

		// merge values...
		dest.merge(src, true);

		assertEquals(6, dest.size());
		assertEquals(1, dest.getChildNodes().getLength());
		assertEquals(2, dest.getPropertyNames().length);
		assertEquals("val2", dest.getProperty("prop2"));

		// a
		temp = dest.getFirstChild();
		assertNotNull(temp);
		assertEquals(temp.getNodeName(), "a");
		assertEquals(4, temp.getChildNodes().getLength());
		assertEquals(2, temp.size());
		assertEquals(temp.getValue(0), "val7");
		assertEquals(temp.getValue(1), "val6");

		// b
		temp = (Attribute) temp.getFirstChild();
		assertNotNull(temp);
		assertEquals("b", temp.getNodeName());
		assertEquals(2, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals("val4", temp.getValue());

		// c
		temp = (Attribute) temp.getFirstChild();
		assertNotNull(temp);
		assertEquals(temp.getNodeName(), "c");
		assertEquals(3, temp.getChildNodes().getLength());
		assertEquals(2, temp.size());
		assertEquals(temp.getValue(0), "val1");
		assertEquals(temp.getValue(1), "val2");

		// d
		temp = (Attribute) temp.getFirstChild();
		assertNotNull(temp);
		assertEquals(temp.getNodeName(), "d");
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals(temp.getValue(), "val");

		// p:b
		temp = (Attribute) temp.getParentNode().getParentNode().getParentNode().getChildNodes().item(2);
		assertNotNull(temp);
		assertEquals(temp.getNodeName(), "p:b");
		assertEquals(2, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals(temp.getValue(), "val3");

		// c
		temp = (Attribute) temp.getLastChild();
		assertNotNull(temp);
		assertEquals(temp.getNodeName(), "c");
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals(1, temp.size());
		assertEquals(temp.getValue(0), "val5");
	}

	@Test
	public void test_Adopt_Node() {
		// adoptNode...

		Entry e = new Entry();

		Attribute attr = new Attribute("name");

		assertNull(attr.getOwnerDocument());
		assertNull(attr.getParentNode());

		e.adoptNode(attr);

		assertEquals(attr.getOwnerDocument(), e);
		assertNull(attr.getParentNode());
	}

	@Test
	public void test_Rename_Node() {
		Entry e = new Entry();

		e.setAttribute(new Attribute("p:a"));
		e.setAttribute(new Attribute("p:b"));

		assertEquals(e.size(), 2);

		e.renameNode(e.getAttribute("p:a"), null, "p:b");
		assertEquals(1, e.size());

		// c
		Attribute temp = e.newAttribute("a.b.c");
		e.renameNode(temp, "ns", "zzz:c");
		assertEquals(e.size(), 2);
		assertEquals(temp.getNodeName(), "zzz:c");

		e.renameNode(e.getAttribute("a.b"), null, "p:b");
		assertEquals(2, e.size());
		assertNotNull(e.getAttribute("a.p:b.zzz:c"));
		assertNull(e.getAttribute("a.b"));

		temp.setAttribute("my:prop", "val");
		assertEquals(temp.getAttributes().getLength(), 1);

		e.renameNode(temp.getAttributeNode("my:prop"), null, "zzz:prop");

		assertEquals(temp.getAttributes().getLength(), 1);
		assertNotNull(temp.getAttributeNode("zzz:prop"));
		assertNull(temp.getAttributeNode("my:prop"));
	}

	@Test
	public void test_Defect_0() {
		Entry e = new Entry();
		e.newAttribute("x.y.z");

		e.setAttribute("newAttr", e.getFirstChild());
		assertEquals(2, e.size());
	}

	@Test
	public void test_Using_Prefixes() {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = null;
		try {
			doc = dbf.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		Element attr = doc.createElementNS("ns", "pref1:name");
		attr.setPrefix("pref1");
		assertEquals(attr.getNamespaceURI(), "ns");

		Entry e = new Entry();
		attr = e.createElementNS("ns", "pref1:name");
		attr.setPrefix("pref1");
		assertEquals(attr.getNamespaceURI(), "ns");
	}

	@Test
	public void test_Schema_Validation() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Element xsd = null;
		Element elem = null;
		try {
			elem = dbf.newDocumentBuilder().parse(
					new InputSource(new StringReader("<?xml version='1.0' encoding='UTF-8'?>"
							+ "<com:findPersonResponse xmlns:com='http://www.example.org/complex_wrapped_doc/'>" + "<com:person>"
							+ "<com:firstname>Gosho</com:firstname>" + "<com:lastname>Goshev</com:lastname>"
							+ "<com:age>47</com:age>" + "<com:email>gosho.goshev@gmail.com</com:email>"
							+ "<com:email>goshog@yahoo.com</com:email>" + "<com:email>ggoshev@us.ibm.com</com:email>"
							+ "</com:person>" + "</com:findPersonResponse>"))).getDocumentElement();
			xsd = dbf
					.newDocumentBuilder()
					.parse(
							new InputSource(
									new StringReader(
											"<?xml version='1.0' encoding='UTF-8'?>"
													+ "<xsd:schema xmlns:tns='http://www.example.org/complex_wrapped_doc/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' targetNamespace='http://www.example.org/complex_wrapped_doc/'>"
													+ "<xsd:element name='findPerson'>" + "<xsd:complexType>" + "<xsd:sequence>"
													+ "<xsd:element ref='tns:name' />" + "</xsd:sequence>" + "</xsd:complexType>"
													+ "</xsd:element>" + "<xsd:element name='name' type='xsd:string' />"
													+ "<xsd:element name='findPersonResponse'>" + "<xsd:complexType>"
													+ "<xsd:sequence>" + "<xsd:element ref='tns:person' />" + "</xsd:sequence>"
													+ "</xsd:complexType>" + "</xsd:element>" + "<xsd:element name='person'>"
													+ "<xsd:complexType>" + "<xsd:sequence>"
													+ "<xsd:element ref='tns:firstname' />" + "<xsd:element ref='tns:lastname' />"
													+ "<xsd:element ref='tns:age' />"
													+ "<xsd:element ref='tns:email' minOccurs='0' maxOccurs='unbounded' />"
													+ "</xsd:sequence>" + "</xsd:complexType>" + "</xsd:element>"
													+ "<xsd:element name='firstname' type='xsd:string' />"
													+ "<xsd:element name='lastname' type='xsd:string' />"
													+ "<xsd:element name='age' type='xsd:int' />"
													+ "<xsd:element name='email' type='xsd:string' />" + "</xsd:schema>")))
					.getDocumentElement();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Entry elemDoc = new Entry();
		elem = convertDOM(elem, elemDoc);
		elemDoc.appendChild(elem);

		Entry xsdDoc = new Entry();
		xsd = convertDOM(xsd, xsdDoc);
		xsdDoc.appendChild(xsd);

		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try {
			schema = factory.newSchema(new DOMSource(xsd));
		} catch (SAXException e2) {
			e2.printStackTrace();
		}
		Validator validator = schema.newValidator();

		try {
			validator.validate(new DOMSource(elem));
		} catch (IOException ex) {
			throw new RuntimeException("Unexpected IO problem: " + ex, ex);
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Convert an XML element between two DOM implementations.
	 * 
	 * @param src
	 *            An XML element of the source implementation.
	 * @param doc
	 *            A document of the target implementation.
	 * @return An XML element of the target implementation.
	 */
	public static Element convertDOM(Element src, Document doc) {

		Element dst;
		if (src.getNamespaceURI() != null) {
			dst = doc.createElementNS(src.getNamespaceURI(), src.getNodeName());
		} else {
			dst = doc.createElement(src.getLocalName());
		}

		NamedNodeMap attrMap = src.getAttributes();
		for (int i = 0; i < attrMap.getLength(); ++i) {
			Attr attr = (Attr) attrMap.item(i);

			if (attr.getNamespaceURI() != null) {

				String qualifiedName = attr.getLocalName();
				if (attr.getPrefix() != null && attr.getPrefix().length() > 0) {
					qualifiedName = attr.getPrefix() + ":" + qualifiedName;
				}

				dst.setAttributeNS(attr.getNamespaceURI(), qualifiedName, attr.getValue());
			} else {
				dst.setAttribute(attr.getLocalName(), attr.getValue());
			}
		}

		NodeList nodes = src.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node child = nodes.item(i);

			Node copyChild = null;
			int nodeType = child.getNodeType();
			switch (nodeType) {
			case Node.CDATA_SECTION_NODE:
				CDATASection cdata = (CDATASection) child;
				copyChild = doc.createCDATASection(cdata.getData());
				break;
			case Node.ELEMENT_NODE:
				copyChild = convertDOM((Element) child, doc);
				break;
			case Node.TEXT_NODE:
				Text text = (Text) child;
				copyChild = doc.createTextNode(text.getData());
				break;
			default:
				// unrecognized node - skip it
			}

			if (copyChild != null) {
				dst.appendChild(copyChild);
			}
		}

		return dst;
	}

	@Test
	public void test_Set_Hierarchical_Attribute_1() {
		Entry e = new Entry();

		e.setAttribute("p1:a.b.p2:c.d", new Attribute());
		assertEquals(1, e.size());

		Attribute temp = e.getFirstChild();
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals("p1:a", temp.getNodeName());

		temp = (Attribute) temp.getFirstChild();
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals("b", temp.getNodeName());

		temp = (Attribute) temp.getFirstChild();
		assertEquals(1, temp.getChildNodes().getLength());
		assertEquals("p2:c", temp.getNodeName());

		temp = (Attribute) temp.getFirstChild();
		assertEquals(0, temp.getChildNodes().getLength());
		assertEquals("d", temp.getNodeName());
	}

	@Test
	public void test_Set_Hierarchical_Attribute_2() {
		Entry e = new Entry();
		e.enableDOM();
		Attribute temp = null;

		Attribute attr = e.newAttribute("p1:a.b.p2:c.d");

		// p2:c
		temp = attr.getParentNode();

		temp.appendChild(new Attribute("d"));
		temp.appendChild(new Attribute("d"));

		assertEquals(3, temp.getChildNodes().getLength());

		// this call will replace the first d Attribute
		e.setAttribute("p1:a.b.p2:c.d", "test");

		assertEquals(3, temp.getChildNodes().getLength());
		assertEquals(((Attribute) temp.getFirstChild()).getValue(), "test");
	}

	@Test
	public void test_Set_Attribute_Partial_Tree() {
		Entry e = new Entry();
		Attribute temp = null;

		// b
		temp = e.newAttribute("p1:a.b");

		e.setAttribute("p1:a.b.p2:c.d", "test");

		assertEquals(1, temp.getChildNodes().getLength());

		// p2:c
		temp = (Attribute) temp.getFirstChild();
		assertEquals(1, temp.getChildNodes().getLength());

		assertEquals(((Attribute) temp.getFirstChild()).getValue(), "test");
	}

	@Test
	public void test_Set_Attribute_Entry_Level() {
		Entry e = new Entry();

		e.setAttribute("first", "test");

		assertEquals(1, e.size());
		assertEquals("first", e.getFirstChild().getNodeName());
	}

	@Test
	public void test_Set_Attribute_Values_As_Attribute() {
		Entry e = new Entry();
		Attribute temp = new Attribute();

		temp.addValue("val1");
		temp.addValue("val2");
		temp.addValue("val3");

		e.setAttributeValues("a.b.p:c", temp);

		assertEquals(3, ((Attribute) e.getFirstChild().getFirstChild().getFirstChild()).size());
	}

	@Test
	public void test_Set_Attribute_Values_As_List() {
		Entry e = new Entry();

		List<String> vals = new ArrayList<String>(3);

		vals.add("val1");
		vals.add("val2");
		vals.add("val3");

		e.setAttributeValues("a.b.p:c", vals);

		assertEquals(3, ((Attribute) e.getFirstChild().getFirstChild().getFirstChild()).size());
	}

	@Test
	public void test_Set_Attribute_Values_As_Object() {
		Entry e = new Entry();

		e.setAttributeValues("a.b.p2:c", "test");

		assertEquals("p2:c", e.getFirstChild().getFirstChild().getFirstChild().getNodeName());
		assertEquals(((Attribute) e.getFirstChild().getFirstChild().getFirstChild()).getValue(), "test");
	}

	@Test
	public void test_Defect_1() {
		// looks like when moving first level getChildNodes() between entries
		// the
		// source attribute's name is changed.
		Entry src = new Entry();
		Entry dst = new Entry();

		src.setAttribute("test", "value");
		dst.setAttribute("zzz", src.getAttribute("test"));

		assertEquals(1, src.size());
		assertEquals("test", src.getAttribute("test").getName());
		assertEquals("value", src.getString("test"));
		assertEquals(1, dst.size());
		assertEquals("value", dst.getString("zzz"));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void test_Old_Cloning() {
		Entry src = new Entry();

		src.setAttribute("attr1", "attrVal1");
		src.setAttribute("attr2", "attrVal2");
		src.setAttribute("attr3", "attrVal3");
		src.setAttribute("attr4", "attrVal4");

		Entry clone = src.clone(src);

		assertEquals(4, clone.size());
	}

	@Test
	public void test_Clone_Defect() {
		Entry e = new Entry();
		e.setAttribute("a", "1");
		e.setAttribute("b", "2");
		assertEquals(2, e.size());

		Entry ec = e.clone();
		assertEquals(2, e.size());
		assertEquals(2, ec.size());
	}

	@Test
	public void test_Deserialize_611_Entry() throws IOException, ClassNotFoundException {

		// serialized entry from 611
		// +-->a.b.c = "c"
		// +-->z = "z"
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		byte[] entry = new byte[] { -84, -19, 0, 5, 115, 114, 0, 22, 99, 111, 109, 46, 105, 98, 109, 46, 100, 105, 46, 101, 110,
				116, 114, 121, 46, 69, 110, 116, 114, 121, -83, 68, -61, -7, -69, 22, 55, 63, 2, 0, 5, 67, 0, 9, 111, 112, 101,
				114, 97, 116, 105, 111, 110, 76, 0, 4, 100, 97, 116, 97, 116, 0, 21, 76, 106, 97, 118, 97, 47, 117, 116, 105, 108,
				47, 72, 97, 115, 104, 116, 97, 98, 108, 101, 59, 76, 0, 12, 108, 111, 119, 101, 114, 67, 97, 115, 101, 77, 97, 112,
				113, 0, 126, 0, 1, 76, 0, 14, 108, 111, 119, 101, 114, 99, 97, 115, 101, 80, 114, 111, 112, 115, 113, 0, 126, 0, 1,
				76, 0, 10, 112, 114, 111, 112, 101, 114, 116, 105, 101, 115, 113, 0, 126, 0, 1, 120, 112, 0, 103, 115, 114, 0, 19,
				106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 116, 97, 98, 108, 101, 19, -69, 15, 37, 33, 74,
				-28, -72, 3, 0, 2, 70, 0, 10, 108, 111, 97, 100, 70, 97, 99, 116, 111, 114, 73, 0, 9, 116, 104, 114, 101, 115, 104,
				111, 108, 100, 120, 112, 63, 64, 0, 0, 0, 0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 4, 116, 0, 5, 97, 46, 98, 46, 99,
				115, 114, 0, 26, 99, 111, 109, 46, 105, 98, 109, 46, 100, 105, 46, 101, 110, 116, 114, 121, 46, 65, 116, 116, 114,
				105, 98, 117, 116, 101, 92, -91, 127, 19, -97, 99, -21, -23, 2, 0, 4, 67, 0, 9, 111, 112, 101, 114, 97, 116, 105,
				111, 110, 90, 0, 7, 112, 114, 111, 116, 101, 99, 116, 76, 0, 4, 110, 97, 109, 101, 116, 0, 18, 76, 106, 97, 118,
				97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 76, 0, 6, 118, 97, 108, 117, 101, 115, 116, 0, 18,
				76, 106, 97, 118, 97, 47, 117, 116, 105, 108, 47, 86, 101, 99, 116, 111, 114, 59, 120, 112, 0, 114, 0, 113, 0, 126,
				0, 5, 115, 114, 0, 16, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 86, 101, 99, 116, 111, 114, -39, -105, 125,
				91, -128, 59, -81, 1, 3, 0, 3, 73, 0, 17, 99, 97, 112, 97, 99, 105, 116, 121, 73, 110, 99, 114, 101, 109, 101, 110,
				116, 73, 0, 12, 101, 108, 101, 109, 101, 110, 116, 67, 111, 117, 110, 116, 91, 0, 11, 101, 108, 101, 109, 101, 110,
				116, 68, 97, 116, 97, 116, 0, 19, 91, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116,
				59, 120, 112, 0, 0, 0, 0, 0, 0, 0, 1, 117, 114, 0, 19, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 79, 98,
				106, 101, 99, 116, 59, -112, -50, 88, -97, 16, 115, 41, 108, 2, 0, 0, 120, 112, 0, 0, 0, 10, 116, 0, 1, 99, 112,
				112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 5, 112, 58, 97, 46, 98, 115, 113, 0, 126, 0, 6, 0, 114, 0,
				113, 0, 126, 0, 16, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0, 0, 0, 10, 116, 0,
				1, 98, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 7, 97, 46, 98, 46, 99, 46, 100, 115, 113, 0, 126,
				0, 6, 0, 114, 0, 113, 0, 126, 0, 21, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0,
				0, 0, 10, 116, 0, 1, 100, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 1, 122, 115, 113, 0, 126, 0, 6,
				0, 114, 0, 113, 0, 126, 0, 26, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0, 0, 0,
				10, 116, 0, 1, 122, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 120, 115, 113, 0, 126, 0, 3, 63, 64, 0, 0, 0,
				0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 4, 113, 0, 126, 0, 5, 113, 0, 126, 0, 5, 113, 0, 126, 0, 16, 113, 0, 126, 0,
				16, 113, 0, 126, 0, 21, 113, 0, 126, 0, 21, 113, 0, 126, 0, 26, 113, 0, 126, 0, 26, 120, 115, 113, 0, 126, 0, 3,
				63, 64, 0, 0, 0, 0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 0, 120, 115, 113, 0, 126, 0, 3, 63, 64, 0, 0, 0, 0, 0, 8,
				119, 8, 0, 0, 0, 11, 0, 0, 0, 0, 120 };

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(entry));

		Entry e = (Entry) ois.readObject();

		assertEquals(4, e.size());

		// see if the serialized entry could be deserialized and accessed as a
		// flat structure.
		assertEquals(1, e.getAttribute("a.b.c").size());
		assertNotNull(e.getAttribute("z"));
		assertEquals(1, e.getAttribute("p:a.b").size());
		assertEquals(1, e.getAttribute("a.b.c.d").size());

		List<String> attrNames = new ArrayList<String>(4);
		attrNames.add("a.b.c");
		attrNames.add("z");
		attrNames.add("p:a.b");
		attrNames.add("a.b.c.d");

		Collection<String> set = e.getAttributeCollection();
		assertEquals(attrNames.size(), set.size());
		assertTrue(attrNames.containsAll(set));

		// now make the deserialized entry hierarchical and check again
		e.enableDOM();

		assertNotNull(e.getAttribute("p:a"));
		assertEquals(1, e.getAttribute("a.b.c").size());
		assertNotNull(e.getAttribute("z"));
		assertEquals(1, e.getAttribute("p:a.b").size());
		assertEquals(1, e.getAttribute("a.b.c.d").size());
		assertNotNull(e.getAttribute("a"));
		assertEquals(1, e.getAttribute("a").getChildNodes().getLength());
		assertEquals(1, e.getAttribute("a.b").getChildNodes().getLength());
		assertEquals(0, e.getAttribute("a.b").size());
		assertEquals(2, e.getAttribute("a.b.c").getChildNodes().getLength());
		assertEquals(1, e.getAttribute("a.b.c.d").getChildNodes().getLength());
		assertEquals(1, e.getAttribute("p:a").getChildNodes().getLength());
		assertEquals(1, e.getAttribute("p:a.b").getChildNodes().getLength());
		assertEquals(0, e.getPropertyNames().length);

		attrNames = new ArrayList<String>(4);
		attrNames.add("a.b.c");
		attrNames.add("z");
		attrNames.add("p:a.b");
		attrNames.add("a.b.c.d");

		set = e.getAttributeCollection();
		assertEquals(attrNames.size(), set.size());
		assertTrue(attrNames.containsAll(set));
	}

	@Test
	public void test_Serialize_And_Deserializing_70_FlatEntry() throws IOException, ClassNotFoundException {
		// create the Entry
		// +-->a.b.c = "c"
		// +-->z = "z"
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		Entry e = new Entry();
		e.setAttribute("a.b.c", "c");
		e.setAttribute("z", "z");
		e.setAttribute("p:a.b", "b");
		e.setAttribute("a.b.c.d", "d");
		e.setProperty("testProp", "testVal");

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(bytes);

		oos.writeObject(e);
		oos.close();

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));

		Entry de = (Entry) ois.readObject();
		ois.close();

		assertEquals(4, de.size());
		assertEquals(false, de.isDOMEnabled());
		assertEquals(1, de.getAttribute("p:a.b").size());
		assertEquals("b", de.getString("p:a.b"));
		assertEquals(1, de.getAttribute("z").size());
		assertEquals("z", de.getString("z"));
		assertEquals(1, de.getAttribute("a.b.c").size());
		assertEquals("c", de.getString("a.b.c"));
		assertEquals(1, de.getAttribute("a.b.c.d").size());
		assertEquals("d", de.getString("a.b.c.d"));
		assertEquals("testVal", de.getProperty("testProp"));

		List<String> attrNames = new ArrayList<String>(4);
		attrNames.add("a.b.c");
		attrNames.add("z");
		attrNames.add("p:a.b");
		attrNames.add("a.b.c.d");

		Collection<String> set = de.getAttributeCollection();
		assertEquals(attrNames.size(), set.size());
		assertTrue(attrNames.containsAll(set));
	}

	@Test
	public void test_Serialize_And_Deserializing_70_Hierarchical_Entry() throws IOException, ClassNotFoundException {
		// create the post-611 entry
		// +-->a.b.c = "c"
		// +-->z = "z"
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		Entry e = new Entry();
		e.enableDOM();
		e.setAttribute("a.b.c", "c");
		e.setAttribute("z", "z");
		// the a.b attribute will have 2 getChildNodes() c and only one of them
		// will be indexed. This will show whether when deserialized the Entry
		// will get the hierarchical data map instead of the flat one.
		e.getAttribute("a.b").appendChild(e.createElement("c"));
		e.setAttribute("p:a.b", "b");
		e.setAttribute("a.b.c.d", "d");
		e.setProperty("testProp", "testVal");

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(bytes);

		oos.writeObject(e);
		oos.close();

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));

		Entry de = (Entry) ois.readObject();
		ois.close();

		assertEquals(true, de.isDOMEnabled());
		assertEquals(4, de.size());
		assertEquals(3, de.getChildNodes().getLength());

		assertNotNull(de.getAttribute("p:a"));
		assertNotNull(de.getAttribute("z"));
		assertEquals(1, de.getAttribute("z").size());
		assertEquals("z", de.getString("z"));
		assertNotNull(de.getAttribute("a"));
		assertEquals(1, de.getAttribute("a").getChildNodes().getLength());
		assertEquals(2, de.getAttribute("a.b").getChildNodes().getLength());
		assertEquals(0, de.getAttribute("a.b").size());
		assertEquals(2, de.getAttribute("a.b.c").getChildNodes().getLength());
		assertEquals(1, de.getAttribute("a.b.c").size());
		assertEquals("c", de.getString("a.b.c"));
		assertEquals(1, de.getAttribute("a.b.c.d").getChildNodes().getLength());
		assertEquals(1, de.getAttribute("a.b.c.d").size());
		assertEquals("d", de.getString("a.b.c.d"));
		assertEquals(1, de.getAttribute("p:a").getChildNodes().getLength());
		assertEquals(1, de.getAttribute("p:a.b").getChildNodes().getLength());
		assertEquals(1, de.getAttribute("p:a.b").size());
		assertEquals("b", de.getString("p:a.b"));
		assertEquals("testVal", de.getProperty("testProp"));

		List<String> attrNames = new ArrayList<String>(4);
		attrNames.add("a.b.c");
		attrNames.add("z");
		attrNames.add("p:a.b");
		attrNames.add("a.b.c.d");

		Collection<String> set = de.getAttributeCollection();
		assertEquals(attrNames.size(), set.size());
		assertTrue(attrNames.containsAll(set));
	}

	@Test
	public void test_getAttributeNames_For_Flat_Entry() throws Exception {
		Entry e = new Entry();

		e.setAttribute("attr1", "val1");
		e.setAttribute("attr2", "val2");
		e.setAttribute("attr3", "val3");
		e.setAttribute("attr4", "val4");

		assertThat(e.getAttributeNames().length, is(4));
		assertThat(e.getAttributeNames(), is(arrayContainingInAnyOrder("attr1", "attr2", "attr3", "attr4")));
	}

	@Test
	public void test_getAttributeNames_For_DOM_Entry() throws Exception {
		Entry e = new Entry(true);

		e.setAttribute("Attr1.child", "val1");
		e.setAttribute("attr2.Child", "val2");
		e.setAttribute("Attr3", "val3");
		e.setAttribute("attr4", "val4");

		assertThat(e.getAttributeNames().length, is(4));
		assertThat(e.getAttributeNames(), is(arrayContainingInAnyOrder("Attr1.child", "attr2.Child", "Attr3", "attr4")));
	}

	@Test
	public void test_KeyName_parseName_With_Escaped_Name() throws Exception {
		Entry.KeyName kn = new Entry.KeyName();

		kn.parseName("prefix1.prefix2.name1\\.name2\\\\.pref:name");

		assertThat(kn.getPrefix(), is(equalTo("prefix1.prefix2.name1\\.name2\\\\")));
		assertThat(kn.getName(), is(equalTo("pref:name")));
	}

	@Test(expected = DOMException.class)
	public void test_adoptNode_Throwing_Exception_When_Node_Is_From_Foreign_DOM_Implementation() throws Exception {
		Entry e = new Entry(true);
		e.adoptNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("test"));
	}

	@Test
	public void test_createCDATASection() throws Exception {
		assertThat(new Entry().createCDATASection("data"), is(not(nullValue())));
	}

	@Test
	public void test_enableDOM_When_The_Child_Path_Has_Been_Created_And_Then_A_Parent_Replaces_A_Node_In_The_Hierarchy_To_The_Child()
			throws Exception {
		Entry e = new Entry();
		e.setAttribute("a.b.c.d", "Dval");
		e.setAttribute("a.b.c", "Cval");
		e.setAttribute("a.b", "Bval");
		e.setAttribute("a", "Aval");

		Attribute a = e.getAttribute("a");
		Attribute b = e.getAttribute("a.b");
		Attribute c = e.getAttribute("a.b.c");
		Attribute d = e.getAttribute("a.b.c.d");

		e.enableDOM();

		assertThat(e.getAttribute("a"), is(sameInstance(a)));
		assertThat(e.getAttribute("a.b"), is(sameInstance(b)));
		assertThat(e.getAttribute("a.b.c"), is(sameInstance(c)));
		assertThat(e.getAttribute("a.b.c.d"), is(sameInstance(d)));
	}

	@Test
	public void test_getAttribute_With_Null_Argument() throws Exception {
		assertThat(new Entry().getAttribute(null), is(nullValue()));
	}

	@Test
	public void test_getElementsByTagName_Returns_Non_Null_Result_Even_For_Empty_Entry() throws Exception {
		assertThat(new Entry(true).getElementsByTagName(""), is(notNullValue()));
	}

	@Test
	public void test_getElementsByTagName_Flattens_The_Hierarchy_When_Searching_With_WildCard() throws Exception {
		Entry e = new Entry(true);
		e.appendChild(e.createElement("attr1"));
		e.appendChild(e.createElement("attr2"));
		e.appendChild(e.createElement("attr3"));

		Attribute n = e.getFirstChild();
		n.appendChild(e.createElement("child11"));
		n.appendChild(e.createElement("child12"));
		n.appendChild(e.createElement("child13"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElement("child21"));
		n.appendChild(e.createElement("child22"));
		n.appendChild(e.createElement("child23"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElement("child31"));
		n.appendChild(e.createElement("child32"));
		n.appendChild(e.createElement("child33"));

		NodeList result = e.getElementsByTagName("*");
		List<String> names = new ArrayList<String>(result.getLength());

		for (int i = 0; i < result.getLength(); i++) {
			names.add(result.item(i).getNodeName());
		}

		assertThat(result.getLength(), is(12));
		assertThat(names, containsInAnyOrder("attr1", "attr2", "attr3", "child11", "child12", "child13", "child21", "child22",
				"child23", "child31", "child32", "child33"));
	}

	@Test
	public void test_getElementsByTagName_For_Looking_Up_Children_With_The_Specified_Name() throws Exception {
		Entry e = new Entry(true);
		e.appendChild(e.createElement("attr1"));
		e.appendChild(e.createElement("attr2"));
		e.appendChild(e.createElement("attr3"));

		Attribute n = e.getFirstChild();
		n.appendChild(e.createElement("child1"));
		n.appendChild(e.createElement("child2"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElement("child1"));
		n.appendChild(e.createElement("child2"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElement("child1"));
		n.appendChild(e.createElement("child2"));

		NodeList result = e.getElementsByTagName("child2");

		assertThat(result.getLength(), is(3));
		assertThat(result.item(0).getNodeName(), is("child2"));
		assertThat(result.item(1).getNodeName(), is("child2"));
		assertThat(result.item(2).getNodeName(), is("child2"));
	}

	@Test
	public void test_getElementsByTagNameNS_Returns_Non_Null_Result_Even_For_Empty_Entry() throws Exception {
		assertThat(new Entry(true).getElementsByTagNameNS("", ""), is(notNullValue()));
	}

	@Test
	public void test_getElementsByTagNameNS_Flattens_The_Hierarchy_When_Searching_With_WildCard_For_Local_Name() throws Exception {
		Entry e = new Entry(true);
		e.appendChild(e.createElementNS("ns", "attr1"));
		e.appendChild(e.createElementNS("ns", "attr2"));
		e.appendChild(e.createElementNS("ns", "attr3"));

		Attribute n = e.getFirstChild();
		n.appendChild(e.createElementNS("ns", "child11"));
		n.appendChild(e.createElementNS("ns", "child12"));
		n.appendChild(e.createElementNS("ns", "child13"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElementNS("ns", "child21"));
		n.appendChild(e.createElementNS("ns", "child22"));
		n.appendChild(e.createElementNS("ns", "child23"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElementNS("ns", "child31"));
		n.appendChild(e.createElementNS("ns", "child32"));
		n.appendChild(e.createElementNS("ns", "child33"));

		NodeList result = e.getElementsByTagNameNS("ns", "*");
		List<String> names = new ArrayList<String>(result.getLength());

		for (int i = 0; i < result.getLength(); i++) {
			names.add(result.item(i).getNodeName());
		}

		assertThat(result.getLength(), is(12));
		assertThat(names, containsInAnyOrder("attr1", "attr2", "attr3", "child11", "child12", "child13", "child21", "child22",
				"child23", "child31", "child32", "child33"));
	}

	@Test
	public void test_getElementsByTagNameNS_Flattens_The_Hierarchy_When_Searching_With_WildCard_For_Namespace() throws Exception {
		Entry e = new Entry(true);
		e.appendChild(e.createElementNS("ns1", "p1:child"));
		e.appendChild(e.createElementNS("ns2", "p2:child"));
		e.appendChild(e.createElementNS("ns3", "p3:child"));

		Attribute n = e.getFirstChild();
		n.appendChild(e.createElementNS("ns1", "child"));
		n.appendChild(e.createElementNS("ns1", "child"));
		n.appendChild(e.createElementNS("ns1", "child"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElementNS("ns2", "child"));
		n.appendChild(e.createElementNS("ns2", "child"));
		n.appendChild(e.createElementNS("ns2", "child"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElementNS("ns3", "child"));
		n.appendChild(e.createElementNS("ns3", "child"));
		n.appendChild(e.createElementNS("ns3", "child"));

		NodeList result = e.getElementsByTagNameNS("*", "child");
		List<String> names = new ArrayList<String>(result.getLength());

		for (int i = 0; i < result.getLength(); i++) {
			names.add(result.item(i).getLocalName());
		}

		assertThat(result.getLength(), is(12));
		for (String name : names) {
			assertThat(name, is("child"));
		}
	}

	@Test
	public void test_getElementsByTagNameNS_For_Looking_Up_Children_With_The_Specified_Local_Name_And_Namespace() throws Exception {
		Entry e = new Entry(true);
		e.appendChild(e.createElementNS("ns1", "p1:child"));
		e.appendChild(e.createElementNS("ns2", "p2:child"));

		Attribute n = e.getFirstChild();
		n.appendChild(e.createElementNS("ns1", "child"));
		n.appendChild(e.createElementNS("ns1", "child"));
		n.appendChild(e.createElementNS("ns1", "child"));

		n = (Attribute) n.getNextSibling();
		n.appendChild(e.createElementNS("ns2", "child"));
		n.appendChild(e.createElementNS("ns2", "child"));
		n.appendChild(e.createElementNS("ns2", "child"));

		NodeList result = e.getElementsByTagNameNS("ns2", "child");

		assertThat(result.getLength(), is(4));
		assertThat(result.item(0).getNodeName(), is("p2:child"));
		assertThat(result.item(1).getNodeName(), is("child"));
		assertThat(result.item(2).getNodeName(), is("child"));
		assertThat(result.item(3).getNodeName(), is("child"));
	}

	@Test
	public void test_mergeAttributeValue_For_Flat_Attribute() throws Exception {
		Entry e = new Entry(true);

		e.setAttribute("attr.name", "val1");

		Attribute attr = new Attribute("attr.name");
		attr.addValue("val1");
		attr.addValue("val2");

		e.mergeAttributeValue(attr);

		assertThat(e.getAttribute("attr.name").size(), is(3));
	}

	@Test
	public void test_mergeAttributeValue_For_DOM_Enabled_Attribute() throws Exception {
		Entry e = new Entry();

		e.setAttribute("attr.name", "val1");

		Attribute attr = new Attribute("attr.name", "ns", true);
		attr.addValue("val1");
		attr.addValue("val2");

		e.mergeAttributeValue(attr);

		assertThat(e.getAttribute("attr.name").size(), is(3));
		assertThat((String) e.getAttribute("attr.name").getValue(0), is("val1"));
		assertThat((String) e.getAttribute("attr.name").getValue(1), is("val1"));
		assertThat((String) e.getAttribute("attr.name").getValue(2), is("val2"));
	}

	@Test
	public void test_removeAttribute_With_Values_And_Children() throws Exception {
		Entry e = new Entry(true);

		e.setAttribute("a", "val");
		e.setAttribute("a.b", "val");

		e.removeAttribute("a");

		// will clear "a" but will leave it.
		assertThat(e.getAttribute("a"), is(notNullValue()));
		assertThat(e.getAttribute("a").size(), is(0));
		assertThat(e.getAttribute("a.b"), is(notNullValue()));
		assertThat(e.getAttribute("a.b").size(), is(1));
		assertThat(e.getAttribute("a.b").getValue(), is("val"));
	}

	@Test
	public void test_removeAttribute_And_Its_Parents_All_The_Way_To_The_Top() throws Exception {
		Entry e = new Entry(true);
		e.setAttribute("a.b.c.d", "val");

		e.removeAttribute("a.b.c.d");

		assertThat(e.getAttribute("a"), is(nullValue()));
		assertThat(e.getAttribute("a.b"), is(nullValue()));
		assertThat(e.getAttribute("a.b.c"), is(nullValue()));
		assertThat(e.getAttribute("a.b.c.d"), is(nullValue()));
	}

	@Test
	public void test_removeAttribute_And_Its_Parents_But_Not_All_The_Way_To_The_Top() throws Exception {
		Entry e = new Entry(true);
		e.setAttribute("a.b.c.d", "val");
		e.setAttribute("a.b", "val");

		e.removeAttribute("a.b.c.d");

		assertThat(e.getAttribute("a"), is(notNullValue()));
		assertThat(e.getAttribute("a.b"), is(notNullValue()));
		assertThat(e.getAttribute("a.b").getValue(), is("val"));
		assertThat(e.getAttribute("a.b.c"), is(nullValue()));
		assertThat(e.getAttribute("a.b.c.d"), is(nullValue()));
	}

	@Test
	public void test_toString_And_toDeltaString_Produce_The_Same_Output_For_Identical_DOM_Enabled_Entries() throws Exception {
		Entry e = new Entry(true);
		e.setAttribute("a.b.c.d", "val");
		e.setAttribute("a.b", "val");
		e.setAttribute("pref:x.pref:y.pref:z", "val");

		e.setProperty("prop1", "val1");
		e.setProperty("prop2", "val2");

		Entry clone = e.clone();

		String eStr = e.toString();
		String cloneStr = clone.toString();

		assertThat(eStr, is(notNullValue()));
		assertThat(cloneStr, is(notNullValue()));
		assertThat(eStr, is(equalTo(cloneStr)));

		eStr = e.toDeltaString();
		cloneStr = clone.toDeltaString();

		assertThat(eStr, is(notNullValue()));
		assertThat(cloneStr, is(notNullValue()));
		assertThat(eStr, is(equalTo(cloneStr)));
	}

	@Test
	public void test_toString_And_toDeltaString_Produce_The_Same_Output_For_Identical_Flat_Entries() throws Exception {
		Entry e = new Entry(false);
		e.setAttribute("a.b.c.d", "val");
		e.setAttribute("a.b", "val");
		e.setAttribute("pref:x.pref:y.pref:z", "val");

		e.setProperty("prop1", "val1");
		e.setProperty("prop2", "val2");

		Entry clone = e.clone();

		String eStr = e.toString();
		String cloneStr = clone.toString();

		assertThat(e.isDOMEnabled(), is(not(true)));
		assertThat(clone.isDOMEnabled(), is(not(true)));

		assertThat(eStr, is(notNullValue()));
		assertThat(cloneStr, is(notNullValue()));

		eStr = e.toDeltaString();
		cloneStr = clone.toDeltaString();

		assertThat(eStr, is(notNullValue()));
		assertThat(cloneStr, is(notNullValue()));
	}

	@Test
	public void test_clone_Make_Sure_The_Cloned_Attributes_Belong_To_The_Cloned_Entry_And_Not_To_The_Source_One() throws Exception {
		Entry e = new Entry(false);
		e.setAttribute("a.b.c.d", "val");
		e.setAttribute("a.b", "val");
		e.setAttribute("pref:x.pref:y.pref:z", "val");

		Entry clone = e.clone();

		assertThat(clone.getAttribute("a.b.c.d").getOwnerDocument(), is(not(sameInstance(e))));
		assertThat(clone.getAttribute("a.b").getOwnerDocument(), is(sameInstance(clone)));
	}

	@Test
	public void test_clone_Make_Sure_The_Cloned_Attribute_Has_The_Same_Type_Of_Values_As_The_Source_Attribute_When_The_Value_Has_Been_Set_On_An_Already_Existing_Attribute()
			throws Exception {
		Entry e = new Entry(true);
		e.newAttribute("a.b.c");
		e.setAttribute("a.b", "val");

		Entry c = e.clone();

		// the problem was that when the c is created the getChildNodes is
		// called on attribute "a.b" which resulted in wrapping the simple
		// string "val" in an AV object which changed the value of the delta
		// string. The fix included a check in the internalToString to verify
		// that simple values should be written out as AV's with replace
		// operation.
		assertEquals(e.toDeltaString(), c.toDeltaString());
	}
}
