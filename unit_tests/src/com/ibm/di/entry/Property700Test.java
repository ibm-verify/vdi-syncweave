package com.ibm.di.entry;

import static junit.framework.Assert.*;

import org.junit.Test;

public class Property700Test {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Constructor_0() {
		Property prop = new Property("pref:name", "ns", "val");

		assertEquals(prop.getPrefix(), "pref");
		assertEquals(prop.getLocalName(), "name");
		assertEquals(prop.getNamespaceURI(), "ns");
		assertEquals(prop.getValue(), "val");
	}

	@Test
	public void test_Constructor_1() {
		Property prop = new Property("pref:name", "ns");

		assertEquals(prop.getPrefix(), "pref");
		assertEquals(prop.getLocalName(), "name");
		assertEquals(prop.getNamespaceURI(), "ns");
		assertEquals(prop.getValue(), "");
	}

	@Test
	public void test_Get_And_Set_Property_Value() {
		Property prop = new Property("pref:name", null);

		prop.setValue(null);
		assertEquals(prop.getValue(), "");
	}

	@Test
	public void test_Clone_Node() {
		Property prop = new Property("pref:name", "ns", "val");

		Property clone1 = (Property) prop.cloneNode(true);
		Property clone2 = (Property) prop.cloneNode(false);

		assertTrue(prop.isEqualNode(clone1));
		assertTrue(prop.isEqualNode(clone2));
		assertTrue(clone1.isEqualNode(clone2));
	}
}
