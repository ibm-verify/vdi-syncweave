package com.ibm.di.server;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.w3c.dom.Element;

import com.ibm.di.config.base.AttributeMapConfigImpl;
import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;

public class AttributeMapping700Test {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static ScriptEngine createTestScriptEngine() throws Exception {
		final String name = "Test ScriptEngine";
		return new ScriptEngine(name);
	}

	private static Log createNOOPLog() {
		return new Log("NOOP Log");
	}

	private static AttributeMapping createTestAttributeMapping() throws Exception {
		final String name = "Test AttributeMapping";
		final TaskInterface context = null;
		final Log log = createNOOPLog();
		final ScriptEngine se = createTestScriptEngine();
		return new AttributeMapping(name, context, log, se);
	}

	private static AttributeMapConfig createMapStarConfig() throws Exception {
		AttributeMapConfig x = new AttributeMapConfigImpl();
		AttributeMapItem ami = new AttributeMapItemImpl();
		ami.setName("*");
		ami.setType(AttributeMapItem.SIMPLE_MAPPING);
		x.setAttributeMapItem(ami);
		return x;
	}
	
	private static AttributeMapping createMapStarAttrMap() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig amConfig = createMapStarConfig();
		am.loadMap(amConfig);
		return am;
	}

	@Test
	public void test_Map_Star_Attribute_With_Namespace_And_Child_From_Hierarchical_Entry_To_Flat_Entry() throws Exception {

		final String namespaceURI = "www.example.com";
		final String qualifiedName = "com:findPerson";
		final String localName = "findPerson";

		final String childNamespaceURI = "www.example2.com";
		final String childQualifiedName = "com:name";

		// add an attribute with namespace to an entry
		Entry he = new Entry();
		Element attr = he.createElementNS(namespaceURI, qualifiedName);
		Element attr2 = he.createElementNS(childNamespaceURI, childQualifiedName);
		attr.appendChild(attr2);
		he.appendChild(attr);

		// a newly-created empty entry must not be DOM enabled
		Entry e = new Entry();

		Attribute a;

		// test the setup before mapping
		assertFalse(e.isDOMEnabled());
		assertTrue(he.isDOMEnabled());
		a = he.getAttribute(qualifiedName);
		assertNotNull(a);
		assertEquals(qualifiedName, a.getName());
		assertEquals(localName, a.getLocalName());
		assertEquals(namespaceURI, a.getNamespaceURI());

		createMapStarAttrMap().mapEntry(he, e);

		a = e.getAttribute(qualifiedName);
		assertNotNull(a);
		assertEquals(qualifiedName, a.getName());
		assertEquals(localName, a.getLocalName());
		assertEquals(namespaceURI, a.getNamespaceURI());
	}

	@Test
	public void test_Map_Star_Attribute_With_Dotted_Name_From_Hierarchical_Entry_To_Flat_Entry() throws Exception {

		final String attrName = "http.body";
		final String attrValue = "test";

		Entry he = new Entry();
		he.enableDOM();
		he.setAttribute(attrName, attrValue);

		Entry e = new Entry();
		assertFalse(e.isDOMEnabled());

		createMapStarAttrMap().mapEntry(he, e);

		assertAttributeValues(e.getAttribute(attrName), attrValue);
	}
	
	@Test
	public void test_Map_Star_Attribute_With_Namespace_From_Hierarchical_Entry_To_Flat_Entry() throws Exception {

		final String attrLocalName = "a";
		final String attrValue = "myvalue";
		final String attrPrefix = "pr";
		final String attrNSURI = "http://www.example.com";
		final String attrNodeName = attrPrefix+":"+attrLocalName;

		Entry he = new Entry();
		Attribute a = he.createElementNS(attrNSURI, attrNodeName);
		a.addValue(attrValue);
		he.appendChild(a);

		Entry e = new Entry();
		assertFalse(e.isDOMEnabled());

		createMapStarAttrMap().mapEntry(he, e);

		assertTrue(e.isDOMEnabled());
		Attribute ma = e.getAttribute(attrNodeName); 
		assertAttributeValues(ma, attrValue);
		assertEquals(attrLocalName, ma.getLocalName());
		assertEquals(attrPrefix, ma.getPrefix());
		assertEquals(attrNSURI, ma.getNamespaceURI());
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Does_Not_Enable_DOM() throws Exception {
		Entry src = new Entry();
		Entry dst = new Entry();
		
		// both entries are not hierarchical before the mapping
		assertFalse(src.isDOMEnabled());
		assertFalse(dst.isDOMEnabled());

		createMapStarAttrMap().mapEntry(src, dst);
		
		// both entries are still not hierarchical after the mapping
		assertFalse(src.isDOMEnabled());
		assertFalse(dst.isDOMEnabled());
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Add_Attribute() throws Exception {

		final String attrName = "a";
		final String attrValue = "myvalue";
		
		Entry src = new Entry();
		Attribute a = src.newAttribute(attrName);
		a.addValue(attrValue);

		Entry dst = new Entry();

		createMapStarAttrMap().mapEntry(src, dst);

		// the attribute should be copied to the destination
		assertAttributeValues(dst.getAttribute(attrName), attrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Add_Attribute_Preserves_Does_Not_Modify_The_Source_Entry() throws Exception {

		final String attrName = "a";
		final String attrValue = "myvalue";
		
		Entry src = new Entry();
		Attribute a = src.newAttribute(attrName);
		a.addValue(attrValue);

		Entry dst = new Entry();

		createMapStarAttrMap().mapEntry(src, dst);
		
		// the attribute in the source must be preserved
		assertAttributeValues(src.getAttribute(attrName), attrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Replace_Attribute() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);

		final boolean mergeValues = false;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);

		// the attribute should be updated in the destination
		assertAttributeValues(dst.getAttribute(attrName), attrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Replace_Attribute_Does_Not_Modify_The_Source_Entry() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);

		final boolean mergeValues = false;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);
		
		// the attribute in the source must be preserved
		assertAttributeValues(src.getAttribute(attrName), attrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Merge_Attribute_Values() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);

		final boolean mergeValues = true;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);

		// the attribute should be updated in the destination
		assertAttributeValues(dst.getAttribute(attrName), attrOldValue, attrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Merge_Attribute_Values_Does_Not_Modify_The_Source_Entry() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);

		final boolean mergeValues = true;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);
		
		// the attribute in the source must be preserved
		assertAttributeValues(src.getAttribute(attrName), attrValue);
	}

	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Add_Attribute_Preserves_Existing_Attributes_In_The_Destination() throws Exception {

		final String attrName = "a";
		final String attrValue = "myvalue";
		
		final String existingAttrName = "b";
		final String existingAttrValue = "myothervalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(existingAttrName, existingAttrValue);

		createMapStarAttrMap().mapEntry(src, dst);

		// existing attributes should be preserved in the destination
		assertAttributeValues(dst.getAttribute(existingAttrName), existingAttrValue);
	}
	
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Replace_Attribute_Preserves_Existing_Attributes_In_The_Destination() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		final String existingAttrName = "b";
		final String existingAttrValue = "myothervalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);
		dst.setAttribute(existingAttrName, existingAttrValue);

		final boolean mergeValues = false;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);

		// existing attributes should be preserved in the destination
		assertAttributeValues(dst.getAttribute(existingAttrName), existingAttrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Merge_Attribute_Values_Preserves_Existing_Attributes_In_The_Destination() throws Exception {

		final String attrName = "a";
		final String attrValue = "newvalue";
		final String attrOldValue = "oldvalue";
		
		final String existingAttrName = "b";
		final String existingAttrValue = "myothervalue";
		
		Entry src = new Entry();
		src.setAttribute(attrName, attrValue);

		Entry dst = new Entry();
		dst.setAttribute(attrName, attrOldValue);
		dst.setAttribute(existingAttrName, existingAttrValue);

		final boolean mergeValues = true;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);

		// existing attributes should be preserved in the destination
		assertAttributeValues(dst.getAttribute(existingAttrName), existingAttrValue);
	}
	
	@Test
	public void test_Map_Star_From_Flat_Entry_To_Flat_Entry_Preserves_Existing_Attributes_In_The_Destination_When_Source_Is_Empty() throws Exception {

		final String existingAttrName = "b";
		final String existingAttrValue = "myothervalue";
		
		Entry src = new Entry();

		Entry dst = new Entry();
		dst.setAttribute(existingAttrName, existingAttrValue);

		final boolean mergeValues = false;
		createMapStarAttrMap().mapEntry(src, dst, mergeValues);
		
		// existing attributes should be preserved in the destination
		assertAttributeValues(dst.getAttribute(existingAttrName), existingAttrValue);
	}
	
	
	private void assertAttributeValues(Attribute a, Object... expectedValues) {
		assertNotNull(a);
		assertEquals(a.size(), expectedValues.length);
		for (int i = 0; i < expectedValues.length; ++i) {
			assertEquals(expectedValues[i], a.getValue(i));
		}
	}
	
}
