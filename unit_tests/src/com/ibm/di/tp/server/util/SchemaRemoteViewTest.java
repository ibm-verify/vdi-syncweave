package com.ibm.di.tp.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.di.schema.internal.server.SchemaRemoteView;
import com.ibm.di.util.DOMUtils;

public class SchemaRemoteViewTest {

	private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

	private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

	private static final String XSD_WITH_INCLUDES = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <schema xmlns=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.ibm.com/xmlns/prod/scmp\"> <include schemaLocation=\"base.xsd\" /> <include schemaLocation=\"propertysheet.xsd\" /> <element name=\"test\" type=\"string\" /> </schema>";

	private URI baseUri = URI.create("http://www.text.org/mypath/");
	private SchemaRemoteView x;

	@Before
	public void setUp() throws Exception {
		x = new SchemaRemoteView(new ByteArrayInputStream(XSD_WITH_INCLUDES.getBytes("UTF-8")));
	}

	@After
	public void tearDown() {
		baseUri = null;
		x = null;
	}

	@Test
	public void test_getRemoteSchemaLocation_returns_uri_which_starts_with_the_baseuri() throws Exception {
		String remoteSchemaLocation = SchemaRemoteView.getRemoteSchemaLocation("test.xsd", baseUri.toString());
		assertTrue(remoteSchemaLocation.startsWith(baseUri.toString()));
	}

	@Test
	public void test_getRemoteSchemaLocation_returns_uri_which_ends_with_the_schema_file_name() throws Exception {
		final String schemaFileName = "test.xsd";
		String remoteSchemaLocation = SchemaRemoteView.getRemoteSchemaLocation(schemaFileName, baseUri.toString());
		assertTrue(remoteSchemaLocation.endsWith(schemaFileName));
	}

	@Test
	public void test_getRemoteSchema_updates_all_include_elements() throws Exception {
		String schemaString = x.getRemoteSchemaAsString(baseUri.toString());

		Element schema = DOMUtils.parseString(schemaString);

		List<Element> includes = DOMUtils.getAllElementsWithName(schema, "include", XSD_NS);

		assertEquals(2, includes.size());
		for (Element e : includes) {
			String schemaLocation = e.getAttribute("schemaLocation");
			assertNotNull(schemaLocation);
			assertTrue(schemaLocation.startsWith(baseUri.toString()));
		}
	}

	@Test
	public void test_getRemoteXsiSchemaLocation() throws Exception {

		final String xsiSchemaLocation = "http://www.hostone.com/1\none.xsd \n http://www.hosttwo.org\t two.xsd \n";
		final List<String> expectedTokens = Arrays.asList("http://www.hostone.com/1", SchemaRemoteView.getRemoteSchemaLocation(
				"one.xsd", baseUri.toString()), "http://www.hosttwo.org", SchemaRemoteView.getRemoteSchemaLocation("two.xsd",
				baseUri.toString()));

		String remoteXsiSchemaLocation = SchemaRemoteView.getRemoteXsiSchemaLocation(xsiSchemaLocation, baseUri.toString());

		List<String> remoteXsiSchemaLocationTokens = removeEmptyTokens(Arrays.asList(remoteXsiSchemaLocation.split("\\s")));

		assertEquals(expectedTokens, remoteXsiSchemaLocationTokens);
	}

	@Test
	public void test_updateXsiSchemaLocation() throws Exception {

		final String xsiSchemaLocation = "http://www.ibm.com/xmlns/prod/scmp scmp.xsd";
		final String expectedXsiSchemaLocation = SchemaRemoteView.getRemoteXsiSchemaLocation(xsiSchemaLocation, baseUri.toString());

		Document doc = DOMUtils.getDOMParser().newDocument();
		Element e = doc.createElementNS("http://www.host1234.net", "yyy:elem");
		e.setAttributeNS(XSI_NS, "isx:schemaLocation", xsiSchemaLocation);

		SchemaRemoteView.updateXsiSchemaLocation(e, baseUri.toString());

		assertEquals("http://www.host1234.net", e.getNamespaceURI());
		assertEquals("elem", e.getLocalName());

		assertEquals(expectedXsiSchemaLocation, e.getAttributeNS(XSI_NS, "schemaLocation"));
	}

	private static List<String> removeEmptyTokens(List<String> list) {
		List<String> result = new ArrayList<String>();
		for (String s : list) {
			if (!"".equals(s)) {
				result.add(s);
			}
		}
		return result;
	}

}
