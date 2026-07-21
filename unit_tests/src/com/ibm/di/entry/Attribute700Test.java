package com.ibm.di.entry;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.AllOf.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.DescribedAs.*;
import static org.hamcrest.core.IsAnything.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsSame.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.exceptions.DOMException;
import com.ibm.di.test.utils.TestUtils;

public class Attribute700Test {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Default_Constructor() {
		Attribute attr = new Attribute();
		assertThat(attr, is(notNullValue()));
		assertThat(attr.isDOMEnabled(), is(false));

		// this should enable dom
		assertThat(attr.getLocalName(), is(notNullValue()));
		assertThat(attr.getNodeName(), is(notNullValue()));
		assertThat(attr.getName(), is(notNullValue()));
		assertThat(attr.getTagName(), is(notNullValue()));
		assertThat(attr.getPrefix(), is(nullValue()));
		assertThat(attr.getNamespaceURI(), is(nullValue()));
		assertThat(attr.isDOMEnabled(), is(true));
	}

	@Test
	public void test_Constructor_With_Providing_Prefixed_Name_No_Value_And_No_NS() {
		Attribute attr = new Attribute("pref:name");

		assertThat(attr, is(notNullValue()));
		assertThat(attr.getOwnerDocument(), is(nullValue()));
		assertThat(attr.parent, is(nullValue()));
		assertThat(attr.getName(), is(equalTo("pref:name")));
		assertThat(attr.size(), is(0));
		assertThat(attr.isDOMEnabled(), is(false));

		// this should enable dom
		assertThat(attr.getPrefix(), is(equalTo("pref")));
		assertThat(attr.getLocalName(), is(equalTo("name")));
		assertThat(attr.getNodeName(), is(equalTo("pref:name")));
		assertThat(attr.getTagName(), is(equalTo("pref:name")));
		assertThat(attr.getNamespaceURI(), is(nullValue()));
		assertThat(attr.isDOMEnabled(), is(true));
	}

	@Test
	public void test_Constructor_With_Providing_Prefixed_Name_Value_And_No_NS() {
		Attribute attr = new Attribute("pref:name", "val");

		assertThat(attr, is(notNullValue()));
		assertThat(attr.getOwnerDocument(), is(nullValue()));
		assertThat(attr.parent, is(nullValue()));
		assertThat(attr.getName(), is(equalTo("pref:name")));
		assertThat(attr.size(), is(1));
		assertThat(attr.getValue(), is("val"));
		assertThat(attr.isDOMEnabled(), is(false));

		// this should enable dom
		assertThat(attr.getPrefix(), is(equalTo("pref")));
		assertThat(attr.getLocalName(), is(equalTo("name")));
		assertThat(attr.getNodeName(), is(equalTo("pref:name")));
		assertThat(attr.getTagName(), is(equalTo("pref:name")));
		assertThat(attr.getNamespaceURI(), is(nullValue()));
		assertThat(attr.isDOMEnabled(), is(true));
	}

	@Test
	public void test_Constructor_With_Providing_Prefixed_Name_NS_And_No_Value() {
		Attribute attr = new Attribute("pref:name", "ns", false);

		assertThat(attr, is(notNullValue()));

		// this constructor always crates a dom enabled entry
		assertThat(attr.isDOMEnabled(), is(true));

		assertThat(attr.getOwnerDocument(), is(nullValue()));
		assertThat(attr.parent, is(nullValue()));
		assertThat(attr.getName(), is(equalTo("pref:name")));
		assertThat(attr.size(), is(0));
		assertThat(attr.getPrefix(), is(equalTo("pref")));
		assertThat(attr.getLocalName(), is(equalTo("name")));
		assertThat(attr.getNodeName(), is(equalTo("pref:name")));
		assertThat(attr.getTagName(), is(equalTo("pref:name")));
		assertThat(attr.getNamespaceURI(), is(equalTo("ns")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_No_Prefix_And_Provide_Valid_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("test");

		assertThat(attr.getPrefix(), is(nullValue()));

		attr.setPrefix("val");

		assertThat(attr.getPrefix(), is(equalTo("val")));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("val:test")));
		assertThat(attr.getTagName(), is(equalTo("val:test")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_No_Prefix_And_Provide_Null_For_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("test");

		assertThat(attr.getPrefix(), is(nullValue()));

		attr.setPrefix(null);

		assertThat(attr.getPrefix(), is(nullValue()));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("test")));
		assertThat(attr.getTagName(), is(equalTo("test")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_No_Prefix_And_Provide_Empty_String_For_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("test");

		assertThat(attr.getPrefix(), is(nullValue()));

		attr.setPrefix("");

		assertThat(attr.getPrefix(), is(nullValue()));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("test")));
		assertThat(attr.getTagName(), is(equalTo("test")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_Prefix_And_Provide_Valid_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("pref:test");

		assertThat(attr.getPrefix(), is(equalTo("pref")));

		attr.setPrefix("val");

		assertThat(attr.getPrefix(), is(equalTo("val")));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("val:test")));
		assertThat(attr.getTagName(), is(equalTo("val:test")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_Prefix_And_Provide_Null_For_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("pref:test");

		assertThat(attr.getPrefix(), is(equalTo("pref")));

		attr.setPrefix(null);

		assertThat(attr.getPrefix(), is(nullValue()));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("test")));
		assertThat(attr.getTagName(), is(equalTo("test")));
	}

	@Test
	public void test_setPrefix_On_Attribute_With_Prefix_And_Provide_Empty_String_For_Value() {
		Entry e = new Entry();
		Attribute attr = e.newAttribute("pref:test");

		assertThat(attr.getPrefix(), is(equalTo("pref")));

		attr.setPrefix("");

		assertThat(attr.getPrefix(), is(nullValue()));
		assertThat(attr.getLocalName(), is(equalTo("test")));
		assertThat(attr.getNodeName(), is(equalTo("test")));
		assertThat(attr.getTagName(), is(equalTo("test")));
	}

	@Test
	public void test_setName_On_Standalone_Attribute_Which_Has_Default_Name_Without_Providing_Prefix() {
		Attribute a = new Attribute();
		a.setName("name");

		assertThat(a.getName(), is("name"));
		assertThat(a.getPrefix(), is(nullValue()));
		assertThat(a.getLocalName(), is("name"));
		assertThat(a.getNodeName(), is("name"));
		assertThat(a.getTagName(), is("name"));
	}

	@Test
	public void test_setName_On_Standalone_Attribute_Which_Has_Default_Name_And_Provide_A_Prefix() {
		Attribute a = new Attribute();
		a.setName("pref:name");

		assertThat(a.getName(), is("pref:name"));
		assertThat(a.getPrefix(), is("pref"));
		assertThat(a.getLocalName(), is("name"));
		assertThat(a.getNodeName(), is("pref:name"));
		assertThat(a.getTagName(), is("pref:name"));
	}

	@Test
	public void test_setName_On_Standalone_Attribute_Which_Has_Default_Name_Without_Providing_Prefix_But_Setting_Escaped_Name() {
		Attribute a = new Attribute();
		a.setName("\\.name\\.");

		assertThat(a.getName(), is("\\.name\\."));
		assertThat(a.getPrefix(), is(nullValue()));
		assertThat(a.getLocalName(), is(".name."));
		assertThat(a.getNodeName(), is(".name."));
		assertThat(a.getTagName(), is(".name."));
	}

	@Test
	public void test_setName_On_Standalone_Attribute_Which_Has_Default_Name_And_Provide_A_Prefix_But_Setting_Escaped_Name() {
		Attribute a = new Attribute();
		a.setName("\\.pref\\.:\\.name\\.");

		assertThat(a.getName(), is("\\.pref\\.:\\.name\\."));
		assertThat(a.getPrefix(), is(".pref."));
		assertThat(a.getLocalName(), is(".name."));
		assertThat(a.getNodeName(), is(".pref.:.name."));
		assertThat(a.getTagName(), is(".pref.:.name."));
	}

	@Test
	public void test_getFullName_Should_Return_Null_When_DOM_Is_Not_Enabled() throws Exception {
		Attribute attr = new Attribute("name");
		assertThat(attr.getFullName(), is(nullValue()));
	}

	@Test
	public void test_getFullName_Should_Return_The_Path_To_The_Top_When_DOM_Enabled() throws Exception {
		Entry e = new Entry(true);
		Attribute attr = e.newAttribute("top.attr.name");
		assertThat(attr.getFullName(), is("top.attr.name"));
	}

	@Test
	public void test_getFullName_Should_Return_The_Path_To_The_Top_When_DOM_Not_Enabled() throws Exception {
		Entry e = new Entry(false);
		Attribute attr = e.newAttribute("top.attr.name");
		assertThat(attr.getFullName(), is(nullValue()));
	}

	@Test
	public void test_FullName_Should_Only_Be_Set_When_DOM_Enabled() throws Exception {
		Attribute attr = new Attribute("name");
		assertThat(attr.isDOMEnabled(), is(false));
		assertThat(attr.getFullName(), is(nullValue()));

		attr.setFullName("top.attr.name");
		assertThat(attr.isDOMEnabled(), is(false));
		assertThat(attr.getFullName(), is(nullValue()));

		attr.enableDOM();
		attr.setFullName("top.attr.name");
		assertThat(attr.isDOMEnabled(), is(true));
		assertThat(attr.getFullName(), is("top.attr.name"));
	}

	@Test
	public void test_Deep_Clone() {
		Attribute attr = new Attribute("pref:name", "ns", true);
		attr.appendChild(new Attribute("pref:name1", "ns", false));
		attr.setAttribute("attr", "val");

		assertThat(attr.size(), is(equalTo(0)));
		assertThat(attr.getLocalName(), is(equalTo("name")));
		assertThat(attr.getPrefix(), is(equalTo("pref")));
		assertThat(attr.getName(), is(equalTo("pref:name")));
		assertThat(attr.getNamespaceURI(), is(equalTo("ns")));
		assertThat(attr.getProtected(), is(equalTo(true)));
		assertThat(attr.getAttributes().getLength(), is(equalTo(1)));

		Attribute clone = attr.clone();

		// check parents
		assertThat(clone.size(), is(equalTo(attr.size())));
		assertThat(clone.getLocalName(), is(equalTo(attr.getLocalName())));
		assertThat(clone.getPrefix(), is(equalTo(attr.getPrefix())));
		assertThat(clone.getName(), is(equalTo(attr.getName())));
		assertThat(clone.getNamespaceURI(), is(equalTo(attr.getNamespaceURI())));
		assertThat(clone.getOper(), is(equalTo(attr.getOper())));
		assertThat(clone.getOperation(), is(equalTo(attr.getOperation())));
		assertThat(clone.getProtected(), is(equalTo(attr.getProtected())));
		assertThat(clone.getAttributes().getLength(), is(equalTo(attr.getAttributes().getLength())));

		// check children
		attr = (Attribute) attr.getFirstChild();
		clone = (Attribute) clone.getFirstChild();

		assertThat(clone.size(), is(equalTo(attr.size())));
		assertThat(clone.getLocalName(), is(equalTo(attr.getLocalName())));
		assertThat(clone.getPrefix(), is(equalTo(attr.getPrefix())));
		assertThat(clone.getName(), is(equalTo(attr.getName())));
		assertThat(clone.getNamespaceURI(), is(equalTo(attr.getNamespaceURI())));
		assertThat(clone.getParentNode(), is(not(equalTo(attr.getParentNode()))));
		assertThat(clone.getOper(), is(equalTo(attr.getOper())));
		assertThat(clone.getOperation(), is(equalTo(attr.getOperation())));
		assertThat(clone.getProtected(), is(equalTo(attr.getProtected())));
		assertThat(clone.getAttributes().getLength(), is(equalTo(attr.getAttributes().getLength())));
	}

	@Test
	public void test_Shallow_Clone() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.setValue(new Attribute("pref:name1", "ns", false));
		attr.setAttribute("attr", "val");

		Attribute clone = attr.cloneNode(false);
		assertEquals(1, clone.getChildNodes().getLength());
		assertEquals("pref:name", clone.getNodeName());
		assertEquals("ns", clone.getNamespaceURI());
		assertEquals(1, clone.getAttributes().getLength());
		assertEquals(1, clone.getChildNodes().getLength());
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void test_addValueIntObject_When_DOM_Disabled_And_Position_Is_Negative() throws Exception {
		Attribute attr = new Attribute();
		attr.addValue(-1, "val");
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void test_addValueIntObject_When_DOM_Enabled_And_Position_Is_Negative() throws Exception {
		Attribute attr = new Attribute();
		attr.enableDOM();
		attr.addValue(-1, "val");
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void test_addValueIntObject_When_DOM_Disabled_And_Position_Is_Greater_Than_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.addValue(attr.size() + 1, "val");
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void test_addValueIntObject_When_DOM_Enabled_And_Position_Is_Greater_Than_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.enableDOM();
		attr.addValue(attr.size() + 1, "val");
	}

	@Test
	public void test_addValueIntObject_When_DOM_Disabled_And_Position_Is_Equal_To_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue(attr.size(), "val2");

		assertThat(attr.size(), is(2));
		assertThat(attr.getValue(0).toString(), is("val1"));
		assertThat(attr.getValue(1).toString(), is("val2"));
	}

	@Test
	public void test_addValueIntObject_When_DOM_Enabled_And_Position_Is_Equal_To_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.enableDOM();
		attr.addValue("val1");
		attr.addValue(attr.size(), "val2");

		assertThat(attr.size(), is(2));
		assertThat(attr.getValue(0).toString(), is("val1"));
		assertThat(attr.getValue(1).toString(), is("val2"));
	}

	@Test
	public void test_addValueIntObject_When_DOM_Disabled_And_Position_Is_Between_0_And_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue(1, "val3");

		assertThat(attr.size(), is(3));
		assertThat(attr.getValue(0).toString(), is("val1"));
		assertThat(attr.getValue(1).toString(), is("val3"));
		assertThat(attr.getValue(2).toString(), is("val2"));
	}

	@Test
	public void test_addValueIntObject_When_DOM_Enabled_And_Position_Is_Between_0_And_Size() throws Exception {
		Attribute attr = new Attribute();
		attr.enableDOM();
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue(1, "val3");

		assertThat(attr.size(), is(3));
		assertThat(attr.getValue(0).toString(), is("val1"));
		assertThat(attr.getValue(1).toString(), is("val3"));
		assertThat(attr.getValue(2).toString(), is("val2"));
	}

	@Test
	public void test_addValueIntObjectInt_When_The_Attribute_Is_Flat() {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue(1, "val3", AttributeValue.AV_DELETE);
		attr.addValue(3, new AttributeValue("val4"), AttributeValue.AV_DELETE);

		assertThat(attr.size(), is(4));
		assertThat(attr.getValue(0), is(instanceOf(String.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat(attr.getValue(2), is(instanceOf(String.class)));
		assertThat((String) attr.getValue(2), is("val2"));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_DELETE));
		assertThat(attr.getValueAV(3), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(3), is(AttributeValue.AV_DELETE));
	}

	@Test
	public void test_addValueObjectInt() {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val2", Attribute.ATTRIBUTE_DELETE);
		attr.addValue("val3", Attribute.ATTRIBUTE_REPLACE);
		attr.addValue(new AttributeValue("val4"), AttributeValue.AV_UNCHANGED);

		assertThat(attr.size(), is(4));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_DELETE));

		// returns undefined because that is how it has been in 6.1.1 and that
		// is how it is after introducing AV_REPLACE in 7.0
		assertThat(attr.getValueOper(2), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(3), is(AttributeValue.AV_UNCHANGED));
	}

	@Test
	public void test_addValues_No_NPE_Is_Thrown() throws Exception {
		new Attribute().addValues(null);
	}

	@Test
	public void test_addValues_When_The_Receiver_Is_Not_Dom_Enabled_And_The_Parameter_Is_Not_Dom_Enabled() throws Exception {
		Attribute dst = new Attribute();
		Attribute src = new Attribute();

		dst.addValue("val");

		src.addValue("srcVal1");
		src.addValue("srcVal2", AttributeValue.AV_DELETE);

		dst.addValues(src);

		assertThat(dst.size(), is(3));
		assertThat((String) dst.getValue(0), is("val"));
		assertThat((String) dst.getValue(1), is("srcVal1"));
		assertThat((String) dst.getValue(2), is("srcVal2"));
		assertThat(dst.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueOper(2), is(AttributeValue.AV_DELETE));
		assertThat(dst.getChildNodes().getLength(), is(3 + 0)); // 3 values and
		// 0 element
		// children
	}

	@Test
	public void test_addValues_When_The_Receiver_Is_Dom_Enabled_And_The_Parameter_Is_Not_Dom_Enabled() throws Exception {
		Attribute dst = new Attribute();
		Attribute src = new Attribute();

		dst.addValue("val");
		dst.appendChild(new Attribute());

		src.addValue("srcVal1");
		src.addValue("srcVal2", AttributeValue.AV_DELETE);

		dst.addValues(src);

		assertThat(dst.size(), is(3));
		assertThat((String) dst.getValue(0), is("val"));
		assertThat((String) dst.getValue(1), is("srcVal1"));
		assertThat((String) dst.getValue(2), is("srcVal2"));
		assertThat(dst.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueOper(2), is(AttributeValue.AV_DELETE));
		assertThat(dst.getChildNodes().getLength(), is(3 + 1)); // 3 values and
		// 1 element
		// child
	}

	@Test
	public void test_addValues_When_The_Receiver_Is_Not_Dom_Enabled_And_The_Parameter_Is_Dom_Enabled() throws Exception {
		Attribute dst = new Attribute();
		Attribute src = new Attribute();

		dst.addValue("val");

		src.addValue("srcVal1");
		src.appendChild(new Attribute()); // this one will not be copied over
		src.addValue("srcVal2", AttributeValue.AV_DELETE);

		dst.addValues(src);

		assertThat(dst.size(), is(3));
		assertThat((String) dst.getValue(0), is("val"));
		assertThat((String) dst.getValue(1), is("srcVal1"));
		assertThat((String) dst.getValue(2), is("srcVal2"));
		assertThat(dst.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueOper(2), is(AttributeValue.AV_DELETE));
		assertThat(dst.getChildNodes().getLength(), is(3 + 0)); // 3 values and
		// 1 element
		// child
	}

	@Test
	public void test_addValues_When_The_Receiver_Is_Dom_Enabled_And_The_Parameter_Is_Dom_Enabled() throws Exception {
		Attribute dst = new Attribute();
		Attribute src = new Attribute();

		dst.addValue("val");
		dst.appendChild(new Attribute());

		src.addValue("srcVal1");
		src.appendChild(new Attribute()); // this one will not be copied over
		src.addValue("srcVal2", AttributeValue.AV_DELETE);

		dst.addValues(src);

		assertThat(dst.size(), is(3));
		assertThat((String) dst.getValue(0), is("val"));
		assertThat((String) dst.getValue(1), is("srcVal1"));
		assertThat((String) dst.getValue(2), is("srcVal2"));
		assertThat(dst.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueOper(2), is(AttributeValue.AV_DELETE));
		assertThat(dst.getChildNodes().getLength(), is(3 + 1)); // 3 values and
		// 0 element
		// children
	}

	@Test
	public void test_clear_For_Attribute_Not_DOM_Enabled() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("a");
		attr.addValue("a1");
		attr.addValue("a2");
		attr.addValue("a3");

		assertThat(attr.size(), is(4));

		attr.clear();

		assertThat(attr.size(), is(0));
	}

	@Test
	public void test_clear_For_Attribute_Which_Is_DOM_Enabled() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("a");
		attr.addValue("a1");
		attr.addValue("a2");
		attr.addValue("a3");

		// add some children to enable dom
		attr.appendChild(new Attribute());
		attr.appendChild(new Attribute());

		// make sure attributes are not cleared too
		attr.setAttribute("attr1", "val1");
		attr.setAttribute("attr2", "val2");

		assertThat(attr.size(), is(4));

		attr.clear();

		assertThat(attr.size(), is(0));
		assertThat(attr.getChildNodes().getLength(), is(2));
		assertThat(attr.getAttributes().getLength(), is(2));
	}

	@Test
	public void test_hasValue_Of_DOM_Enabled_Attribute() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertTrue(attr.hasValue("val1"));
		assertTrue(attr.hasValue("val2"));
		assertTrue(attr.hasValue(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertFalse(attr.hasValue("VAL1"));
	}

	@Test
	public void test_hasValue_Of_DOM_Disabled_Attribute() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertTrue(attr.hasValue("val1"));
		assertTrue(attr.hasValue("val2"));
		assertTrue(attr.hasValue(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertFalse(attr.hasValue("VAL1"));
	}

	@Test
	public void test_contains_Of_DOM_Enabled_Attribute() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertTrue(attr.contains("val1"));
		assertTrue(attr.contains("val2"));
		assertTrue(attr.contains(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertFalse(attr.contains("VAL1"));
	}

	@Test
	public void test_contains_Of_DOM_Disabled_Attribute() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertTrue(attr.contains("val1"));
		assertTrue(attr.contains("val2"));
		assertTrue(attr.contains(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertFalse(attr.contains("VAL1"));
	}

	@Test
	public void test_hasValueIC_Of_DOM_Enabled_Attribute() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		assertTrue(attr.hasValueIC("val1"));
		assertTrue(attr.hasValueIC("val2"));
		assertTrue(attr.hasValueIC("VAL1"));
		assertTrue(attr.hasValueIC("val3"));
		assertTrue(attr.hasValueIC("vaL3"));
	}

	@Test
	public void test_hasValueIC_Of_DOM_Disabled_Attribute() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		assertTrue(attr.hasValueIC("val1"));
		assertTrue(attr.hasValueIC("val2"));
		assertTrue(attr.hasValueIC("VAL1"));
		assertTrue(attr.hasValueIC("val3"));
		assertTrue(attr.hasValueIC("vaL3"));
	}

	@Test
	public void test_Get_Attribute_Value() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		assertEquals(attr.getValue(0), "val1");
		assertEquals(attr.getValue(1), "val2");
		assertEquals(attr.getValue(2), "val3");
	}

	@Test
	public void test_protected_On_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("pref:name", "ns", false);

		assertThat(attr.getProtected(), is(false));

		attr.setProtected(true);

		assertThat(attr.getProtected(), is(true));

		attr.setProtected(false, false);

		assertThat(attr.getProtected(), is(false));

		attr.setProtected(true, false);

		assertThat(attr.getProtected(), is(true));

		attr.appendChild(new Attribute());
		assertThat(((Attribute) attr.getFirstChild()).getProtected(), is(false));

		attr.setProtected(true, true);

		assertThat(attr.getProtected(), is(true));
		assertThat(((Attribute) attr.getFirstChild()).getProtected(), is(true));
	}

	@Test
	public void test_protected_On_DOM_Disabled_Attribute() throws Exception {
		Attribute attr = new Attribute("pref:name");

		assertThat(attr.getProtected(), is(false));

		attr.setProtected(true);

		assertThat(attr.getProtected(), is(true));

		attr.setProtected(false, false);

		assertThat(attr.getProtected(), is(false));

		attr.setProtected(true, false);

		assertThat(attr.getProtected(), is(true));
	}

	@Test
	public void test_getValue_With_String() throws Exception {
		String val = "val";

		assertThat(new Attribute("name", val).getValue(), is(sameInstance(val)));
	}

	@Test
	public void test_getValue_With_Null() throws Exception {
		assertThat(new Attribute("name").getValue(), is(nullValue()));
	}

	@Test
	public void test_getValue_With_Object() throws Exception {
		Integer i = new Integer(256);
		assertThat(new Attribute("name", i).getValue(), is(equalTo("256")));
	}

	@Test
	public void test_getValueOperation() throws Exception {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val1", AttributeValue.AV_ADD);
		attr.addValue("val1", 333); // undefined operation.

		assertThat(attr.getValueOperation(0), is(""));
		assertThat(attr.getValueOperation(1), is("add"));
		assertThat(attr.getValueOperation(2), is(""));
	}

	@Test
	public void test_getValues_Should_Unwrap_Attributes_Values_When_DOM_Enabled() {
		Attribute attr = new Attribute("simpleAttrName", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		for (Object val : attr.getValues()) {
			assertThat(val, is(not(instanceOf(AttributeValue.class))));
		}
	}

	@Test
	public void test_getValues_Should_Unwrap_Attributes_Values_When_Not_DOM_Enabled() {
		Attribute attr = new Attribute("simpleAttrName");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		for (Object val : attr.getValues()) {
			assertThat(val, is(not(instanceOf(AttributeValue.class))));
		}
	}

	@Test
	public void test_getValuesVector_Should_Unwrap_Attributes_Values_When_Not_DOM_Enabled() {
		Attribute attr = new Attribute("simpleAttrName");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		for (Object val : attr.getValuesVector()) {
			assertThat(val, is(not(instanceOf(AttributeValue.class))));
		}
	}

	@Test
	public void test_getValuesVector_Should_Unwrap_Attributes_Values_When_DOM_Enabled() {
		Attribute attr = new Attribute("simpleAttrName", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		for (Object val : attr.getValuesVector()) {
			assertThat(val, is(not(instanceOf(AttributeValue.class))));
		}
	}

	@Test
	public void test_getValuesAV_Should_Unwrap_Attributes_Values_When_Not_DOM_Enabled_And_Value_Is_Tagged_As_Replace() {
		Attribute attr = new Attribute("simpleAttrName");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_REPLACE);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		Object[] vals = attr.getValuesAV();

		assertThat(vals[0], is(instanceOf(String.class)));
		assertThat(vals[1], is(instanceOf(String.class)));
		assertThat(vals[2], is(instanceOf(AttributeValue.class)));
	}

	@Test
	public void test_getValuesAV_Should_Unwrap_Attributes_Values_When_DOM_Enabled_And_Value_Is_Tagged_As_Replace() {
		Attribute attr = new Attribute("simpleAttrName", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_REPLACE);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		Object[] vals = attr.getValuesAV();

		assertThat(vals[0], is(instanceOf(String.class)));
		assertThat(vals[1], is(instanceOf(String.class)));
		assertThat(vals[2], is(instanceOf(AttributeValue.class)));
	}

	@Test
	public void test_getValueAV_When_DOM_Enabled() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat((String) attr.getValueAV(0), is("val1"));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
	}

	@Test
	public void test_getValueAV_When_DOM_Not_Enabled() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat((String) attr.getValueAV(0), is("val1"));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
	}

	@Test
	public void test_merge_Flat_Attribute_With_Another_Flat_Attribute() throws Exception {
		Attribute dst = new Attribute("dst");
		Attribute src = new Attribute("src");

		dst.addValue("val1");
		dst.addValue("val2", AttributeValue.AV_UNCHANGED);

		src.addValue("val1"); // exists in dst so should not get copied over
		src.addValue("val3");
		// in 611 this tag has the meaning that the value is not tagged
		src.addValue("val4", AttributeValue.AV_REPLACE);

		assertThat(dst.isDOMEnabled(), is(not(true)));
		assertThat(src.isDOMEnabled(), is(not(true)));

		dst.merge(src);

		// make sure both attributes stay flat
		assertThat(dst.isDOMEnabled(), is(not(true)));
		assertThat(src.isDOMEnabled(), is(not(true)));

		assertThat(dst.size(), is(4));
		assertThat(dst.getValueAV(0), is(instanceOf(String.class)));
		assertThat(dst.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueAV(2), is(instanceOf(String.class)));
		// the value will be unwrapped
		assertThat(dst.getValueAV(3), is(instanceOf(String.class)));
		assertThat((String) dst.getValue(0), is("val1"));
		assertThat((String) dst.getValue(1), is("val2"));
		assertThat((String) dst.getValue(2), is("val3"));
		assertThat((String) dst.getValue(3), is("val4"));
	}

	@Test
	public void test_merge_Flat_Attribute_With_A_DOM_Enabled_Attribute() throws Exception {
		Attribute dst = new Attribute("dst");
		Attribute src = new Attribute("src", "ns", false);

		dst.addValue("val1");
		dst.addValue("val2", AttributeValue.AV_UNCHANGED);

		src.addValue("val1"); // exists in dst so should not get copied over
		src.addValue("val3");
		// in 611 this tag has the meaning that the value is not tagged
		src.addValue("val4", AttributeValue.AV_REPLACE);

		assertThat(dst.isDOMEnabled(), is(not(true)));

		dst.merge(src);

		// Make sure the dst attribute stays flat
		assertThat(dst.isDOMEnabled(), is(not(true)));

		assertThat(dst.size(), is(4));
		assertThat(dst.getValueAV(0), is(instanceOf(String.class)));
		assertThat(dst.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueAV(2), is(instanceOf(String.class)));
		// the value will be unwrapped
		assertThat(dst.getValueAV(3), is(instanceOf(String.class)));
		assertThat((String) dst.getValue(0), is("val1"));
		assertThat((String) dst.getValue(1), is("val2"));
		assertThat((String) dst.getValue(2), is("val3"));
		assertThat((String) dst.getValue(3), is("val4"));
	}

	@Test
	public void test_merge_DOM_Enabled_Attribute_With_A_Flat_Attribute() throws Exception {
		Attribute dst = new Attribute("dst", "ns", false);
		Attribute src = new Attribute("src");

		dst.addValue("val1");
		dst.appendChild(new Attribute("child1"));
		dst.appendChild(new Attribute("child2"));
		dst.getChildNodes().item(1).appendChild(new AttributeValue("val1.1", AttributeValue.AV_ADD));
		dst.getChildNodes().item(1).appendChild(new AttributeValue("val1.2", AttributeValue.AV_ADD));
		((Attribute) dst.getChildNodes().item(1)).addValue("val3");
		dst.getLastChild().appendChild(new Attribute("child2.1"));
		dst.getLastChild().appendChild(new AttributeValue("val2.1", AttributeValue.AV_ADD));
		dst.getLastChild().appendChild(new AttributeValue("val2.2", AttributeValue.AV_ADD));
		dst.getLastChild().getFirstChild().appendChild(new AttributeValue("val2.1.1", AttributeValue.AV_ADD));
		dst.addValue("val2", AttributeValue.AV_UNCHANGED);

		// dst:________________________ // src:________________________ //
		// ..+--"val1"_________________ // ..+--"val1"_________________ //
		// ..+--child1:________________ // ..+--"val3"_________________ //
		// ..|....+--"val1.1"__________ // ..+--"val4"_________________ //
		// ..|....+--"val1.2"__________ // ____________________________ //
		// ..|....+--"val3"____________ // ____________________________ //
		// ..+--child2:________________ // ____________________________ //
		// ..|....+--child2.1:_________ // ____________________________ //
		// ..|....|....+--"val2.1.1"___ // ____________________________ //
		// ..|....+--"val2.1"__________ // ____________________________ //
		// ..|....+--"val2.2"__________ // ____________________________ //
		// ..+--"val2"_________________ // ____________________________ //

		src.addValue("val1"); // exists in dst so should not get copied over
		src.addValue("val3");
		// in 611 this tag has the meaning that the value is not tagged
		src.addValue("val4", AttributeValue.AV_REPLACE);

		assertThat(src.isDOMEnabled(), is(not(true)));

		// merge the hierarchy
		dst.merge(src);

		// here is how it should look
		// dst:________________________ //
		// ..+--"val1"_________________ //
		// ..+--child1:________________ //
		// ..|....+--"val1.1"__________ //
		// ..|....+--"val1.2"__________ //
		// ..|....+--"val3"____________ //
		// ..+--child2:________________ //
		// ..|....+--child2.1:_________ //
		// ..|....|....+--"val2.1.1"___ //
		// ..|....+--"val2.1"__________ //
		// ..|....+--"val2.2"__________ //
		// ..+--"val2"_________________ //
		// ..+--"val3"_________________ //
		// ..+--"val4"_________________ //

		// make sure the flat entry stays flat...
		assertThat(src.isDOMEnabled(), is(not(true)));

		// check the number of direct value/attributeValue children
		assertThat(dst.size(), is(4));
		// check the number of all children
		assertThat(dst.getChildNodes().getLength(), is(6));

		// check the first level children
		assertThat(dst.getValueAV(0), is(instanceOf(String.class)));
		assertThat(dst.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueAV(2), is(instanceOf(String.class)));
		// the value will be unwrapped
		assertThat(dst.getValueAV(3), is(instanceOf(String.class)));
		assertThat((String) dst.getValue(0), is("val1"));
		assertThat((String) dst.getValue(1), is("val2"));
		assertThat((String) dst.getValue(2), is("val3"));
		assertThat((String) dst.getValue(3), is("val4"));

		// validate child1 structure
		assertThat(dst.getChildNodes().item(1), is(instanceOf(Attribute.class)));
		Attribute child1 = (Attribute) dst.getChildNodes().item(1);
		// check size
		assertThat(child1.size(), is(3));
		assertThat(child1.getChildNodes().getLength(), is(3));
		// check children
		assertThat(child1.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(child1.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(child1.getValueAV(2), is(instanceOf(String.class)));
		assertThat((String) child1.getValue(0), is("val1.1"));
		assertThat((String) child1.getValue(1), is("val1.2"));
		assertThat((String) child1.getValue(2), is("val3"));

		// validate child2 structure
		assertThat(dst.getChildNodes().item(2), is(instanceOf(Attribute.class)));
		Attribute child2 = (Attribute) dst.getChildNodes().item(2);
		// check size
		assertThat(child2.size(), is(2));
		assertThat(child2.getChildNodes().getLength(), is(3));
		// check children
		assertThat(child2.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(child2.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat((String) child2.getValue(0), is("val2.1"));
		assertThat((String) child2.getValue(1), is("val2.2"));

		// validate child2.1 structure
		assertThat(child2.getFirstChild(), is(instanceOf(Attribute.class)));
		Attribute child21 = (Attribute) child2.getFirstChild();
		// check size
		assertThat(child21.size(), is(1));
		assertThat(child21.getChildNodes().getLength(), is(1));
		// check children
		assertThat(child21.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat((String) child21.getValue(0), is("val2.1.1"));
	}

	@Test
	public void test_merge_DOM_Enabled_Attribute_With_Another_DOM_Enabled_Attribute() throws Exception {
		Attribute dst = new Attribute("dst", "ns", false);
		Attribute src = new Attribute("src", "na", false);

		dst.addValue("val1");
		dst.appendChild(new Attribute("child1"));
		dst.appendChild(new Attribute("child2"));
		dst.getChildNodes().item(1).appendChild(new AttributeValue("val1.1", AttributeValue.AV_ADD));
		dst.getChildNodes().item(1).appendChild(new AttributeValue("val1.2", AttributeValue.AV_ADD));
		((Attribute) dst.getChildNodes().item(1)).addValue("val3");
		dst.getLastChild().appendChild(new Attribute("child2.1"));
		dst.getLastChild().appendChild(new AttributeValue("val2.1", AttributeValue.AV_ADD));
		dst.getLastChild().appendChild(new AttributeValue("val2.2", AttributeValue.AV_ADD));
		dst.getLastChild().getFirstChild().appendChild(new AttributeValue("val2.1.1", AttributeValue.AV_ADD));
		dst.addValue("val2", AttributeValue.AV_UNCHANGED);

		// dst:________________________ // src:________________________ //
		// ..+--"val1"_________________ // ..+--"val1"_________________ //
		// ..+--child1:________________ // ..+--child1:________________ //
		// ..|....+--"val1.1"__________ // ..|....+--"val1.2"__________ //
		// ..|....+--"val1.2"__________ // ..|....+--"val1.3"__________ //
		// ..|....+--"val3"____________ // ..|....+--"val3"____________ //
		// ..|_________________________ // ..|....+--"val5"____________ //
		// ..+--child2:________________ // ..+--child3:________________ //
		// ..|....+--child2.1:_________ // ..|....+--"val6"____________ //
		// ..|....|....+--"val2.1.1"___ // ..|....+--child3.1:_________ //
		// ..|....+--"val2.1"__________ // ..|....|....+--"val7"_______ //
		// ..|....+--"val2.2"__________ // ..|....+--"val3.1"__________ //
		// ..+--"val2"_________________ // ..+--"val3"_________________ //
		// ____________________________ // ..+--"val4"_________________ //

		src.addValue("val1"); // exists in dst so should not get copied over
		src.appendChild(new Attribute("child1"));
		src.getChildNodes().item(1).appendChild(new AttributeValue("val1.2", AttributeValue.AV_UNCHANGED));
		src.getChildNodes().item(1).appendChild(new AttributeValue("val1.3", AttributeValue.AV_UNCHANGED));
		((Attribute) src.getChildNodes().item(1)).addValue("val3");
		((Attribute) src.getChildNodes().item(1)).addValue("val5");
		src.appendChild(new Attribute("child3"));
		((Attribute) src.getLastChild()).addValue("val6");
		src.getLastChild().appendChild(new Attribute("child3.1"));
		((Attribute) src.getLastChild().getChildNodes().item(1)).addValue("val7");
		src.getLastChild().appendChild(new AttributeValue("val3.1", AttributeValue.AV_UNCHANGED));
		src.addValue("val3");
		// in 611 this tag has the meaning that the value is not tagged
		src.addValue("val4", AttributeValue.AV_REPLACE);

		// merge the hierarchy
		dst.merge(src);

		// here is how it should look
		// dst:________________________ //
		// ..+--"val1"_________________ //
		// ..+--child1:________________ //
		// ..|....+--"val1.1"__________ //
		// ..|....+--"val1.2"__________ //
		// ..|....+--"val3"____________ //
		// ..|....+--"val1.3"__________ //
		// ..|....+--"val5"____________ //
		// ..+--child2:________________ //
		// ..|....+--child2.1:_________ //
		// ..|....|....+--"val2.1.1"___ //
		// ..|....+--"val2.1"__________ //
		// ..|....+--"val2.2"__________ //
		// ..+--"val2"_________________ //
		// ..+--child3:________________ //
		// ..|....+--"val6"____________ //
		// ..|....+--child3.1:_________ //
		// ..|....|....+--"val7"_______ //
		// ..|....+--"val3.1"__________ //
		// ..+--"val3"_________________ //
		// ..+--"val4"_________________ //

		// check the number of direct value/attributeValue children
		assertThat(dst.size(), is(4));
		// check the number of all children
		assertThat(dst.getChildNodes().getLength(), is(7));

		// check the first level children
		assertThat(dst.getValueAV(0), is(instanceOf(String.class)));
		assertThat(dst.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(dst.getValueAV(2), is(instanceOf(String.class)));
		// the value will be unwrapped
		assertThat(dst.getValueAV(3), is(instanceOf(String.class)));
		assertThat((String) dst.getValue(0), is("val1"));
		assertThat((String) dst.getValue(1), is("val2"));
		assertThat((String) dst.getValue(2), is("val3"));
		assertThat((String) dst.getValue(3), is("val4"));

		// validate child1 structure
		assertThat(dst.getChildNodes().item(1), is(instanceOf(Attribute.class)));
		Attribute child1 = (Attribute) dst.getChildNodes().item(1);
		// check size
		assertThat(child1.size(), is(5));
		assertThat(child1.getChildNodes().getLength(), is(5));
		// check children
		assertThat(child1.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(child1.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(child1.getValueAV(2), is(instanceOf(String.class)));
		assertThat(child1.getValueAV(3), is(instanceOf(AttributeValue.class)));
		assertThat(child1.getValueAV(4), is(instanceOf(String.class)));
		assertThat((String) child1.getValue(0), is("val1.1"));
		assertThat((String) child1.getValue(1), is("val1.2"));
		assertThat((String) child1.getValue(2), is("val3"));
		assertThat((String) child1.getValue(3), is("val1.3"));
		assertThat((String) child1.getValue(4), is("val5"));

		// validate child2 structure
		assertThat(dst.getChildNodes().item(2), is(instanceOf(Attribute.class)));
		Attribute child2 = (Attribute) dst.getChildNodes().item(2);
		// check size
		assertThat(child2.size(), is(2));
		assertThat(child2.getChildNodes().getLength(), is(3));
		// check children
		assertThat(child2.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(child2.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat((String) child2.getValue(0), is("val2.1"));
		assertThat((String) child2.getValue(1), is("val2.2"));

		// validate child2.1 structure
		assertThat(child2.getFirstChild(), is(instanceOf(Attribute.class)));
		Attribute child21 = (Attribute) child2.getFirstChild();
		// check size
		assertThat(child21.size(), is(1));
		assertThat(child21.getChildNodes().getLength(), is(1));
		// check children
		assertThat(child21.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat((String) child21.getValue(0), is("val2.1.1"));

		// validate child3 structure
		assertThat(dst.getChildNodes().item(4), is(instanceOf(Attribute.class)));
		Attribute child3 = (Attribute) dst.getChildNodes().item(4);
		// check size
		assertThat(child3.size(), is(2));
		assertThat(child3.getChildNodes().getLength(), is(3));
		assertThat(child3.getValueAV(0), is(instanceOf(String.class)));
		assertThat(child3.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat((String) child3.getValue(0), is("val6"));
		assertThat((String) child3.getValue(1), is("val3.1"));

		// validate child3.1 structure
		assertThat(child3.getChildNodes().item(1), is(instanceOf(Attribute.class)));
		Attribute child31 = (Attribute) child3.getChildNodes().item(1);
		// check size
		assertThat(child31.size(), is(1));
		assertThat(child31.getChildNodes().getLength(), is(1));
		// check children
		assertThat(child31.getValueAV(0), is(instanceOf(String.class)));
		assertThat((String) child31.getValue(0), is("val7"));
	}

	@Test
	public void test_merge_Of_DOM_Enabled_Attribute_With_Another_DOM_Enabled_Attribute_Which_Has_Properties_Set() throws Exception {
		Attribute dst = new Attribute("dst", "ns", false);
		Attribute src = new Attribute("src", "na", false);

		src.setAttribute("attr1", "val1");
		src.setAttributeNS("ns", "pref:name", "val2");

		src.appendChild(new Attribute("pref:name1"));
		((Element) src.getFirstChild()).setAttribute("attr2", "val3");

		dst.merge(src);

		assertThat(src.getAttributes().getLength(), is(2));
		assertThat(dst.getAttributes().getLength(), is(2));
		assertThat(dst.getAttribute("attr1"), is("val1"));
		assertThat(dst.getAttributeNS("ns", "name"), is("val2"));
		assertThat(dst.getFirstChild().getAttributes().getLength(), is(1));
		assertThat(((Element) dst.getFirstChild()).getAttribute("attr2"), is("val3"));
	}

	@Test
	public void test_removeValue_Of_DOM_Enabled_Attribute() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.appendChild(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));
		NodeImpl temp = (NodeImpl) attr.getFirstChild();

		assertNotNull(temp.parent);

		assertTrue(attr.removeValue(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertTrue(attr.removeValue("val2"));

		assertNotNull(attr.removeChild(temp));
		assertFalse(attr.removeValue(null));

		assertNull(temp.parent);
		assertNull(temp.getOwnerDocument());
	}

	@Test
	public void test_removeValue_Of_DOM_Disabled_Attribute() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertTrue(attr.removeValue(new AttributeValue("val1", AttributeValue.AV_ADD)));
		assertTrue(attr.removeValue("val2"));

		assertFalse(attr.removeValue(null));
	}

	@Test
	public void test_removeAt_When_DOM_Enabled() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);
		attr.appendChild(new AttributeValue("val3", AttributeValue.AV_UNCHANGED, true));

		NodeImpl temp = (NodeImpl) attr.getLastChild();

		assertNotNull(temp.parent);

		assertNotNull(attr.removeValueAt(1));
		assertNotNull(attr.removeChild(temp));

		assertNotNull(attr.removeValueAt(0));
		assertNull(attr.removeValueAt(0));

		assertNull(temp.parent);
		assertNull(temp.getOwnerDocument());
	}

	@Test
	public void test_removeAt_When_DOM_Disabled() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_ADD);

		assertNotNull(attr.removeValueAt(1));

		assertNotNull(attr.removeValueAt(0));
		assertNull(attr.removeValueAt(0));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void test_setValueIntObject_For_DOM_Enabled_Attribute_With_Negative_Position() {
		new Attribute("pref:name", "ns", false).setValue(-1, "abcd");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void test_setValueIntObject_For_DOM_Disabled_Attribute_With_Negative_Position() {
		new Attribute("pref:name").setValue(-1, "abcd");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void test_setValueIntObject_For_DOM_Enabled_Attribute_With_Position_Greather_Than_Size() {
		new Attribute("pref:name", "ns", false).setValue(1, "abcd");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void test_setValueIntObject_For_DOM_Disabled_Attribute_With_Position_Greather_Than_Size() {
		new Attribute("pref:name").setValue(1, "abcd");
	}

	@Test
	public void test_setValueIntObject_For_DOM_Enabled_Attribute_With_Equal_To_Size_Postion() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.setValue(attr.size(), "val2");

		assertThat(attr.size(), is(2));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
	}

	@Test
	public void test_setValueIntObject_For_DOM_Disabled_Attribute_With_Equal_To_Size_Postion() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.setValue(attr.size(), "val2");

		assertThat(attr.size(), is(2));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
	}

	@Test
	public void test_setValueIntObject_For_DOM_Enabled_Attribute_With_Correct_Postion() {
		Attribute attr = new Attribute("pref:name", "ns", false);
		attr.addValue("val1");
		attr.appendChild(new Attribute("child1"));
		attr.addValue("val2");
		attr.addValue("val3");
		attr.setValue(1, "a");
		attr.setValue(3, new AttributeValue("b", AttributeValue.AV_ADD));

		assertThat(attr.size(), is(4));
		assertThat(attr.getChildNodes().getLength(), is(5));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(3), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("a"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat((String) attr.getValue(3), is("b"));
	}

	@Test
	public void test_setValueIntObject_For_DOM_Disabled_Attribute_With_Correct_Postion() {
		Attribute attr = new Attribute("pref:name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");
		attr.setValue(1, "a");
		attr.setValue(3, new AttributeValue("b", AttributeValue.AV_ADD));

		assertThat(attr.size(), is(4));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(3), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("a"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat((String) attr.getValue(3), is("b"));
	}

	@Test
	public void test_setValueIntObjectInt_When_The_Attribute_Is_Flat() {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val2");
		attr.setValue(1, "val3", AttributeValue.AV_DELETE);

		assertThat(attr.size(), is(2));
		assertThat(attr.getValue(0), is(instanceOf(String.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_DELETE));
	}

	@Test
	public void test_setValueIntObjectInt_With_AV_When_The_Attribute_Is_Flat() {
		Attribute attr = new Attribute();
		attr.addValue("val1");
		attr.addValue("val2");
		attr.setValue(1, new AttributeValue("val3"), AttributeValue.AV_DELETE);

		assertThat(attr.size(), is(2));
		assertThat(attr.getValue(0), is(instanceOf(String.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_DELETE));
	}

	@Test
	public void test_setValueObject_Of_Empty_Attribute() throws Exception {
		Attribute attr = new Attribute("name");

		assertThat(attr.size(), is(0));

		attr.setValue("val");

		assertThat(attr.size(), is(1));
		assertThat(attr.getValue(), is("val"));
	}

	@Test
	public void test_setValueObject_To_Replace_First_Value() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");

		assertThat(attr.size(), is(3));

		attr.setValue("val");

		assertThat(attr.size(), is(3));
		assertThat((String) attr.getValue(0), is("val"));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
	}

	@Test
	public void test_setValueObjectInt_Of_Empty_Attribute() throws Exception {
		Attribute attr = new Attribute("name");

		assertThat(attr.size(), is(0));

		attr.setValue("val", AttributeValue.AV_ADD);

		assertThat(attr.size(), is(1));
		assertThat(attr.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_ADD));
		assertThat(attr.getValue(), is("val"));
	}

	@Test
	public void test_setValueObjectInt_To_Replace_First_Value() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");

		assertThat(attr.size(), is(3));

		attr.setValue("val", AttributeValue.AV_ADD);

		assertThat(attr.size(), is(3));
		assertThat((String) attr.getValue(0), is("val"));
		assertThat(attr.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_ADD));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
	}

	@Test
	public void test_setValueObjectInt_To_Replace_First_Value_With_AV() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");

		assertThat(attr.size(), is(3));

		attr.setValue(new AttributeValue("val"), AttributeValue.AV_ADD);

		assertThat(attr.size(), is(3));
		assertThat((String) attr.getValue(0), is("val"));
		assertThat(attr.getValueAV(0), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_ADD));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
	}

	@Test
	public void test_Equality_Of_Identical_Nodes() {
		Attribute attr1 = new Attribute("pref:name", "ns", false);
		attr1.setAttribute("prop", "val");
		Attribute attr2 = new Attribute("pref:name", "ns", false);
		attr2.setAttribute("prop", "val");
		attr2.addValue("val");

		assertTrue(attr1.isEqualNode(attr2));
	}

	@Test
	public void test_setValueOper_For_Flat_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue(new AttributeValue("val3"));

		attr.setValueOper(1, AttributeValue.AV_ADD);
		attr.setValueOper(2, AttributeValue.AV_ADD);

		assertThat(attr.size(), is(3));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_ADD));
		assertThat(attr.getValueOper(2), is(AttributeValue.AV_ADD));
	}

	@Test
	public void test_setValueOper_For_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("name", "ns", false);
		attr.addValue("val1");
		attr.appendChild(new Attribute("child1"));
		attr.addValue("val2");
		attr.addValue(new AttributeValue("val3"));

		attr.setValueOper(1, AttributeValue.AV_ADD);
		attr.setValueOper(2, AttributeValue.AV_ADD);

		assertThat(attr.size(), is(3));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_ADD));
		assertThat(attr.getValueOper(2), is(AttributeValue.AV_ADD));
	}

	@Test
	public void test_setValueOperation_For_Flat_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue(new AttributeValue("val3"));

		attr.setValueOperation(1, "add");
		attr.setValueOperation(2, "add");

		assertThat(attr.size(), is(3));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_ADD));
		assertThat(attr.getValueOper(2), is(AttributeValue.AV_ADD));
	}

	@Test
	public void test_setValueOperation_For_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("name", "ns", false);
		attr.addValue("val1");
		attr.appendChild(new Attribute("child1"));
		attr.addValue("val2");
		attr.addValue(new AttributeValue("val3"));

		attr.setValueOperation(1, "add");
		attr.setValueOperation(2, "add");

		assertThat(attr.size(), is(3));
		assertThat(attr.getValueAV(0), is(instanceOf(String.class)));
		assertThat(attr.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getValueAV(2), is(instanceOf(AttributeValue.class)));
		assertThat((String) attr.getValue(0), is("val1"));
		assertThat((String) attr.getValue(1), is("val2"));
		assertThat((String) attr.getValue(2), is("val3"));
		assertThat(attr.getValueOper(0), is(AttributeValue.AV_UNDEFINED));
		assertThat(attr.getValueOper(1), is(AttributeValue.AV_ADD));
		assertThat(attr.getValueOper(2), is(AttributeValue.AV_ADD));
	}

	@Test
	public void test_setValuesList_For_Flat_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");

		List<String> vals = new ArrayList<String>();
		vals.add("a");
		vals.add("b");
		vals.add("c");

		attr.setValues(vals);
		assertThat(attr.size(), is(equalTo(vals.size())));
		assertThat((String) attr.getValue(0), is("a"));
		assertThat((String) attr.getValue(1), is("b"));
		assertThat((String) attr.getValue(2), is("c"));
	}

	@Test
	public void test_setValuesList_For_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("name", "ns", true);
		attr.addValue("val1");
		attr.appendChild(new Attribute("child1"));
		attr.addValue("val2");
		attr.appendChild(new Attribute("child2"));

		List<String> vals = new ArrayList<String>();
		vals.add("a");
		vals.add("b");
		vals.add("c");

		attr.setValues(vals);
		assertThat(attr.size(), is(equalTo(vals.size())));
		assertThat(attr.getChildNodes().getLength(), is(vals.size() + 2));
		assertThat((String) attr.getValue(0), is("a"));
		assertThat((String) attr.getValue(1), is("b"));
		assertThat((String) attr.getValue(2), is("c"));
	}

	@Test
	public void test_Get_Elements_By_Tag_Name() {
		Attribute attr = new Attribute("pref:a", "ns", false);
		Attribute root = attr;

		attr.appendChild(new Attribute("pref:b", "ns", false));
		attr = (Attribute) attr.getFirstChild();
		attr.addValue("val1");

		attr.appendChild(new Attribute("pref:c", "ns", false));
		attr = (Attribute) attr.getLastChild();
		attr.addValue("val2");

		attr.appendChild(new Attribute("pref:b", "ns", false));
		attr = (Attribute) attr.getLastChild();
		attr.addValue("val3");

		NodeList elems = root.getElementsByTagName("pref:b");
		assertEquals(elems.getLength(), 2);

		elems = root.getElementsByTagName("*");
		assertEquals(elems.getLength(), 3);
	}

	@Test
	public void test_setValuesObjects_For_Flat_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");

		String[] vals = { "a", "b", "c" };

		attr.setValues(vals);
		assertThat(attr.size(), is(equalTo(vals.length)));
		assertThat((String) attr.getValue(0), is("a"));
		assertThat((String) attr.getValue(1), is("b"));
		assertThat((String) attr.getValue(2), is("c"));
	}

	@Test
	public void test_setValuesObjects_For_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("name", "ns", true);
		attr.addValue("val1");
		attr.appendChild(new Attribute("child1"));
		attr.addValue("val2");
		attr.appendChild(new Attribute("child2"));

		String[] vals = { "a", "b", "c" };

		attr.setValues(vals);
		assertThat(attr.size(), is(equalTo(vals.length)));
		assertThat(attr.getChildNodes().getLength(), is(vals.length + 2));
		assertThat((String) attr.getValue(0), is("a"));
		assertThat((String) attr.getValue(1), is("b"));
		assertThat((String) attr.getValue(2), is("c"));
	}

	@Test(expected = DOMException.class)
	public void test_appendChild_With_Invalid_Param() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").appendChild(doc.createElement("test"));
	}

	@Test(expected = DOMException.class)
	public void test_insertBefore_With_Invalid_First_Param() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").insertBefore(doc.createElement("test"), null);
	}

	@Test(expected = DOMException.class)
	public void test_insertBefore_With_Invalid_Second_Param() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").insertBefore(new Attribute("name1"), doc.createElement("test"));
	}

	@Test(expected = DOMException.class)
	public void test_removeChild_With_Invalid_Element() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").removeChild(doc.createElement("test"));
	}

	@Test(expected = DOMException.class)
	public void test_removeChild_With_Invalid_Attr() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").removeChild(doc.createAttribute("test"));
	}

	@Test
	public void test_insertBefore_With_Same_Object_For_Both_Parms() throws Exception {
		Attribute attr = new Attribute("NAME");
		Attribute newAttr = new Attribute("new");

		Attribute res = (Attribute) attr.insertBefore(newAttr, newAttr);

		assertThat(attr.getChildNodes().getLength(), is(0));
		assertThat(res, is(sameInstance(newAttr)));
	}

	@Test
	public void test_insertBefore_Try_To_Add_The_Same_Child_Twice() throws Exception {
		Attribute parent = new Attribute("name");
		Attribute child = new Attribute("child");
		Attribute ref = new Attribute("ref");

		parent.appendChild(ref);
		parent.appendChild(child);
		parent.appendChild(new Attribute("child2"));

		parent.insertBefore(child, ref);

		assertThat(parent.size(), is(0));
		assertThat(parent.getChildNodes().getLength(), is(3));
		assertThat(parent.getFirstChild(), is(sameInstance((Node) child)));
		assertThat(parent.getLastChild().getPreviousSibling(), is(not(sameInstance((Node) child))));
	}

	@Test
	public void test_insertBefore_Try_To_Add_Children_Of_Another_Attribute() throws Exception {

		Attribute otherAttr = new Attribute("other");

		Attribute parent = new Attribute("name");
		Attribute child = new Attribute("child");
		Attribute ref = new Attribute("ref");

		otherAttr.appendChild(child);

		parent.appendChild(ref);
		parent.appendChild(new Attribute("child2"));

		parent.insertBefore(child, ref);

		assertThat(parent.size(), is(0));
		assertThat(parent.getChildNodes().getLength(), is(3));
		assertThat(parent.getFirstChild(), is(not(sameInstance((Node) child))));
	}

	@Test
	public void test_insertBefore_Try_To_Add_Child_Having_Others_Attribute_Child_As_Reference() throws Exception {
		Attribute otherAttr = new Attribute("other");

		Attribute parent = new Attribute("name");
		Attribute child = new Attribute("child");
		Attribute ref = new Attribute("ref");

		otherAttr.appendChild(ref);

		parent.appendChild(new Attribute("child2"));

		parent.insertBefore(child, ref);

		assertThat(parent.size(), is(0));
		assertThat(parent.getChildNodes().getLength(), is(2));
		assertThat(parent.getLastChild(), is(sameInstance((Node) child)));
	}

	@Test
	public void test_removeChild_For_Property() throws Exception {
		Attribute attr = new Attribute("name");
		attr.setAttribute("attr", "val");

		assertThat(attr.getAttributes().getLength(), is(1));

		attr.removeChild(attr.getAttributeNode("attr"));

		assertThat(attr.getAttributes().getLength(), is(0));
	}

	@Test
	public void test_getChildNodes_Enables_DOM() throws Exception {
		Attribute attr = new Attribute("name");

		assertThat(attr.isDOMEnabled(), is(not(true)));

		attr.getChildNodes();
		assertThat(attr.isDOMEnabled(), is(true));
	}

	@Test
	public void test_getChildNodes_Shuld_Not_Return_Null() throws Exception {
		assertThat(new Attribute("name").getChildNodes(), is(not(nullValue())));
	}

	@Test
	public void test_getFirstChild_Returns_Null_When_Attribute_Is_Empty() throws Exception {
		assertThat(new Attribute("name").getFirstChild(), is(nullValue()));
	}

	@Test
	public void test_getLastChild_Returns_Null_When_Attribute_Is_Empty() throws Exception {
		assertThat(new Attribute("name").getLastChild(), is(nullValue()));
	}

	@Test
	public void test_replaceChild_Returns_Null_When_Parameters_Are_Properties() throws Exception {
		assertThat(new Attribute("name").replaceChild(new Property("name", "ns", "val"), new Attribute("zzz")), is(nullValue()));
		assertThat(new Attribute("name").replaceChild(new Attribute("zzz"), new Property("name", "ns", "val")), is(nullValue()));
	}

	@Test(expected = DOMException.class)
	public void test_replaceChild_With_Foreign_Node_As_First_Param() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").replaceChild(doc.createElement("test"), new Attribute("zzz"));
	}

	@Test(expected = DOMException.class)
	public void test_replaceChild_With_Foreign_Node_As_Second_Param() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		new Attribute("name").replaceChild(new Attribute("zzz"), doc.createElement("test"));
	}

	@Test
	public void test_replaceChild_With_Null_As_The_Old_Child() throws Exception {
		Attribute attr = new Attribute("name");

		attr.replaceChild(new Attribute("child"), null);

		assertThat(attr.getChildNodes().getLength(), is(0));
		assertThat(attr.size(), is(0));
	}

	@Test
	public void test_replaceChild_With_An_Attribute_As_The_Old_Child_And_Attribute_As_New() throws Exception {
		Attribute attr = new Attribute("name");
		attr.appendChild(new Attribute("value"));

		attr.replaceChild(new Attribute("child"), attr.getFirstChild());

		assertThat(attr.getChildNodes().getLength(), is(1));
		assertThat(attr.size(), is(0));

		assertThat(attr.getFirstChild().getNodeName(), is("child"));
	}

	@Test
	public void test_replaceChild_With_An_Attribute_As_The_Old_Child_And_AttributeValue_As_New() throws Exception {
		Attribute attr = new Attribute("name");
		attr.appendChild(new Attribute("value"));

		attr.replaceChild(new AttributeValue("child"), attr.getFirstChild());

		assertThat(attr.getChildNodes().getLength(), is(1));
		assertThat(attr.size(), is(1));

		assertThat(attr.getFirstChild().getNodeValue(), is("child"));
	}

	@Test
	public void test_replaceChild_With_An_AttributeValue_As_The_Old_Child_And_Attribute_As_New() throws Exception {
		Attribute attr = new Attribute("name");
		attr.appendChild(new AttributeValue("value"));

		attr.replaceChild(new Attribute("child"), attr.getFirstChild());

		assertThat(attr.getChildNodes().getLength(), is(1));
		assertThat(attr.size(), is(0));

		assertThat(attr.getFirstChild().getNodeName(), is("child"));
	}

	@Test
	public void test_replaceChild_With_An_AttributeValue_As_The_Old_Child_And_AttributeValue_As_New() throws Exception {
		Attribute attr = new Attribute("name");
		attr.appendChild(new AttributeValue("value"));

		attr.replaceChild(new AttributeValue("child"), attr.getFirstChild());

		assertThat(attr.getChildNodes().getLength(), is(1));
		assertThat(attr.size(), is(1));
		assertThat(attr.getFirstChild().getNodeValue(), is("child"));
	}

	@Test
	public void test_hasChildNodes_Returns_False_On_Empty_Flat_Attribute() throws Exception {
		assertThat(new Attribute("name").hasChildNodes(), is(false));
	}

	@Test
	public void test_hasChildNodes_Returns_False_On_DOM_Enabled_Attribute() throws Exception {
		assertThat(new Attribute("name", "ns", false).hasChildNodes(), is(false));
	}

	@Test
	public void test_hasChildNodes_Returns_True_On_Empty_Flat_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");

		assertThat(attr.hasChildNodes(), is(true));
	}

	@Test
	public void test_hasChildNodes_Returns_True_On_DOM_Enabled_Attribute() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val3");
		attr.appendChild(new Attribute("child1"));
		attr.appendChild(new Attribute("child2"));

		assertThat(attr.hasChildNodes(), is(true));
	}

	@Test
	public void test_getCDATASections_Retrieves_Only_Explicitly_Tagged_Values() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue(new AttributeValue("val2", AttributeValue.AV_UNDEFINED, false));
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNDEFINED, true));

		NodeList cdatas = attr.getCDATASections();

		assertThat(cdatas.getLength(), is(1));
		assertThat(cdatas.item(0).getNodeValue(), is(equalTo("val2")));
	}

	@Test
	public void test_getTextSections_Retrieves_All_Non_CDATA_Tagged_Values() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue(new AttributeValue("val2", AttributeValue.AV_UNDEFINED, false));
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_UNDEFINED, true));

		NodeList cdatas = attr.getTextSections();

		assertThat(cdatas.getLength(), is(2));
		assertThat(cdatas.item(0).getNodeValue(), is(equalTo("val1")));
		assertThat(cdatas.item(1).getNodeValue(), is(equalTo("val3")));
	}

	@Test
	public void test_Get_Elements_By_Tag_Name_And_Namespace() {
		Attribute attr = new Attribute("pref:a", "ns", false);
		Attribute root = attr;

		attr.appendChild(new Attribute("pref:b", "ns", false));
		attr = (Attribute) attr.getFirstChild();
		attr.addValue("val1");

		attr.appendChild(new Attribute("pref:c", "ns1", false));
		attr = (Attribute) attr.getLastChild();
		attr.addValue("val2");

		attr.appendChild(new Attribute("pref:b", "ns1", false));
		attr = (Attribute) attr.getLastChild();
		attr.addValue("val3");

		NodeList elems = root.getElementsByTagNameNS("ns", "b");
		assertEquals(elems.getLength(), 1);

		elems = root.getElementsByTagNameNS("*", "b");
		assertEquals(2, elems.getLength());

		elems = root.getElementsByTagNameNS("ns1", "*");
		assertEquals(elems.getLength(), 2);

		elems = root.getElementsByTagNameNS("*", "*");
		assertEquals(elems.getLength(), 3);
	}

	@Test
	public void test_Remove_Attribute_Node() {
		Attribute attr = new Attribute();
		attr.setAttribute("attr", "val");

		assertNull(attr.removeAttributeNode(null));
	}

	@Test(expected = DOMException.class)
	public void test_Remove_Attribute_Node_1() {
		Attribute attr = new Attribute();
		Attribute other = new Attribute();

		Property prop = new Property("name", null, "value");

		other.setAttributeNode(prop);
		attr.removeAttributeNode(prop);
	}

	@Test
	public void test_Remove_Attribute_Node_2() {
		Attribute attr = new Attribute();
		attr.setAttribute("attr", "val");
		Property prop = (Property) attr.removeAttributeNode(attr.getAttributeNode("attr"));

		assertNotNull(prop);
		assertEquals(0, attr.getAttributes().getLength());
		assertNull(prop.getParentNode());
	}

	@Test
	public void test_Insert_Child_Node_Before_Another_Attribute_Node() {
		Attribute attr = new Attribute();
		attr.appendChild(new Attribute("a"));
		attr.appendChild(new Attribute("c"));

		Attribute b = new Attribute("b");

		attr.insertBefore(b, attr.getLastChild());

		assertEquals(attr.getChildNodes().getLength(), 3);
		assertEquals(attr.getChildNodes().item(1), b);
	}

	@Test
	public void test_Replace_Node() {
		Attribute attr = new Attribute();
		attr.appendChild(new Attribute("a"));
		attr.appendChild(new Attribute("c"));

		Attribute b = new Attribute("b");

		Attribute replaced = (Attribute) attr.replaceChild(b, attr.getLastChild());

		assertEquals(attr.getChildNodes().getLength(), 2);
		assertNotNull(replaced);
		assertEquals(attr.getChildNodes().item(1), b);
	}

	@Test
	public void test_Cloning_Of_Unattached_To_Entry_Attribute() {
		// this defect is about the missing child values (Attributes) of the
		// clone of another attribute which has not been attached to an entry
		// (i.e. doc=null)
		Attribute a = new Attribute("a");

		a.appendChild(new Attribute("b1"));
		((Attribute) a.getFirstChild()).setValue("v1");

		a.appendChild(new Attribute("b2"));
		((Attribute) a.getFirstChild()).setValue("v2");

		a.appendChild(new Attribute("b3"));
		((Attribute) a.getFirstChild()).setValue("v3");

		a.appendChild(new Attribute("b4"));
		((Attribute) a.getFirstChild()).setValue("v4");

		assertEquals(4, a.getChildNodes().getLength());
		Attribute clone = a.clone();
		assertEquals(4, clone.getChildNodes().getLength());
		assertTrue("Only 2 children left in the clone", a.getChildNodes().getLength() == 4);
	}

	@Test
	public void test_Escape_Attribute_Names_1() {
		Entry e = new Entry();

		e.setAttribute("a\\.b", "val");

		assertEquals(1, e.size());
		assertNotNull(e.getAttribute("a\\.b"));

		e.newAttribute("pref:composite\\.name.child1").setValue("val1");
		e.newAttribute("pref:composite\\.name.child2").setValue("val2");

		Collection<String> names = e.getAttributeCollection();

		assertEquals(3, names.size());
		assertTrue(names.contains("pref:composite\\.name.child1"));
		assertTrue(names.contains("pref:composite\\.name.child2"));
		assertTrue(names.contains("a\\.b"));
	}

	@Test
	public void test_Escape_Attribute_Names_2() {
		Entry e = new Entry();
		e.enableDOM();

		e.setAttribute("a\\.b.c\\.d", "val");

		assertEquals(1, e.size());
		assertNotNull(e.getAttribute("a\\.b"));
		assertNotNull(e.getAttribute("a\\.b.c\\.d"));
		assertEquals("c.d", e.getAttribute("a\\.b.c\\.d").getNodeName());
		assertEquals("val", e.getAttribute("a\\.b.c\\.d").getValue());

		Collection<String> names = e.getAttributeCollection();

		assertEquals(1, names.size());
		assertTrue(names.contains("a\\.b.c\\.d"));
	}

	@Test
	public void test_Escape_Attribute_Names_3() {
		assertEquals("child\\.name\\.test", Attribute.escapeName("child.name.test"));
	}

	@Test
	public void test_Remove_Escape_Characters() {
		assertEquals("child.name.test", Attribute.normalizeName("child\\.name\\.test"));
	}

	@Test
	public void test_getAttribute() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttribute("attr", "val1");
		attr.setAttribute("pref:attr", "val2");

		assertThat(attr.getAttribute("attr"), is("val1"));
		assertThat(attr.getAttribute("pref:attr"), is("val2"));
	}

	@Test
	public void test_getAttributeNS() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNS("ns", "name", "val1");

		assertThat(attr.getAttributeNS("ns", "name"), is("val1"));

		attr.setAttributeNS("ns", "pref:name", "val2");

		assertThat(attr.getAttributes().getLength(), is(1));
		assertThat(attr.getAttributeNS("ns", "name"), is("val2"));
		assertThat(attr.getAttributeNS("ns", "pref:name"), is(""));
	}

	@Test
	public void test_getAttributeNode() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNode(new Property("attr", null, "val1"));
		attr.setAttributeNode(new Property("pref:attr", null, "val2"));

		assertThat(attr.getAttributeNode("attr").getValue(), is("val1"));
		assertThat(attr.getAttributeNode("pref:attr").getValue(), is("val2"));
		assertThat(attr.getAttributeNode("attr").getNamespaceURI(), is(nullValue()));
		assertThat(attr.getAttributeNode("pref:attr").getNamespaceURI(), is(nullValue()));
	}

	@Test
	public void test_getAttributeNodeNS() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNodeNS(new Property("name", "ns", "val1"));

		assertThat(attr.getAttributeNodeNS("ns", "name").getValue(), is("val1"));

		attr.setAttributeNodeNS(new Property("pref:name", "ns", "val2"));

		assertThat(attr.getAttributes().getLength(), is(1));
		assertThat(attr.getAttributeNodeNS("ns", "name").getValue(), is("val2"));
		assertThat(attr.getAttributeNodeNS("ns", "pref:name"), is(nullValue()));
		assertThat(attr.getAttributeNodeNS("ns", "name").getPrefix(), is("pref"));
	}

	@Test
	public void test_hasAttribute() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttribute("attr", "val1");
		attr.setAttribute("pref:attr", "val2");

		assertThat(attr.hasAttribute("attr"), is(true));
		assertThat(attr.hasAttribute("pref:attr"), is(true));
	}

	@Test
	public void test_hasAttributeNS() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNS("ns", "name", "val1");

		assertThat(attr.hasAttributeNS("ns", "name"), is(true));

		attr.setAttributeNS("ns", "pref:name", "val2");

		assertThat(attr.getAttributes().getLength(), is(1));
		assertThat(attr.hasAttributeNS("ns", "name"), is(true));
	}

	@Test
	public void test_hasAttributes_Returns_True() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttribute("attr", "val1");
		attr.setAttribute("pref:attr", "val2");
		attr.setAttributeNS("ns", "name", "val1");
		attr.setAttributeNS("ns", "pref:name", "val2");

		assertThat(attr.getAttributes().getLength(), is(3));
		assertThat(attr.hasAttributes(), is(true));
	}

	@Test
	public void test_hasAttributes_Returns_False() throws Exception {
		assertThat(new Attribute("name").hasAttributes(), is(false));
	}

	@Test
	public void test_removeAttribute() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttribute("attr", "val1");
		attr.setAttribute("pref:attr", "val2");

		assertThat(attr.getAttributes().getLength(), is(2));
		assertThat(attr.getAttribute("attr"), is("val1"));
		assertThat(attr.getAttribute("pref:attr"), is("val2"));

		attr.removeAttribute("attr");
		attr.removeAttribute("pref:attr");
		assertThat(attr.getAttribute("attr"), is(""));
		assertThat(attr.getAttribute("pref:attr"), is(""));
		assertThat(attr.getAttributes().getLength(), is(0));
	}

	@Test
	public void test_removeAttributeNS() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNS("ns", "name", "val1");

		assertThat(attr.getAttributeNS("ns", "name"), is("val1"));
		attr.removeAttributeNS("ns", "name");
		assertThat(attr.getAttributeNS("ns", "name"), is(""));

		attr.setAttributeNS("ns", "pref:name", "val2");

		assertThat(attr.getAttributes().getLength(), is(1));
		assertThat(attr.getAttributeNS("ns", "name"), is("val2"));
		assertThat(attr.getAttributeNS("ns", "pref:name"), is(""));

		attr.removeAttributeNS("ns", "name");

		assertThat(attr.getAttributes().getLength(), is(0));
		assertThat(attr.getAttributeNS("ns", "name"), is(""));
	}

	@Test
	public void test_removeAttributeNode() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setAttributeNode(new Property("attr", null, "val1"));
		attr.setAttributeNode(new Property("pref:attr", null, "val2"));

		assertThat(attr.getAttributeNode("attr").getValue(), is("val1"));
		assertThat(attr.getAttributeNode("pref:attr").getValue(), is("val2"));
		assertThat(attr.getAttributeNode("attr").getNamespaceURI(), is(nullValue()));
		assertThat(attr.getAttributeNode("pref:attr").getNamespaceURI(), is(nullValue()));

		attr.removeAttributeNode((Attr) attr.getAttributes().item(0));
		attr.removeAttributeNode((Attr) attr.getAttributes().item(0));

		assertThat(attr.getAttributeNode("attr"), is(nullValue()));
		assertThat(attr.getAttributeNode("pref:attr"), is(nullValue()));
	}

	@Test
	public void test_Serialization_Of_Flat_Attribute() throws ClassNotFoundException {
		Attribute attr = new Attribute("name");

		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_DELETE);
		attr.addValue("val3", AttributeValue.AV_REPLACE);

		attr.setOper(Attribute.ATTRIBUTE_DELETE);

		byte[] bytes = TestUtils.serializeObject(attr);
		Attribute des = (Attribute) TestUtils.deserializeObject(bytes);

		assertThat(des, is(not(nullValue())));
		assertThat(des.getName(), is("name"));
		assertThat(des.size(), is(3));
		assertThat(des.getValueAV(0), is(instanceOf(String.class)));
		assertThat((String) des.getValue(0), is("val1"));
		assertThat(des.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat((String) des.getValue(1), is("val2"));
		// for backward compatibility we are always serializing AttributeValues
		// tagged as replace as regular objects.
		assertThat(des.getValueAV(2), is(instanceOf(String.class)));
		assertThat((String) des.getValue(2), is("val3"));
		assertThat(des.getOper(), is(Attribute.ATTRIBUTE_DELETE));
	}

	@Test
	public void test_Serialization_Of_DOM_Enabled_Attribute() throws ClassNotFoundException {
		Attribute attr = new Attribute("pref:name", "ns", false);

		attr.addValue("val1");
		attr.addValue("val2", AttributeValue.AV_DELETE);
		attr.addValue("val3", AttributeValue.AV_REPLACE);
		attr.appendChild(new Attribute("pref:child"));

		attr.setOper(Attribute.ATTRIBUTE_DELETE);

		attr.setAttribute("pref:attr", "val1");
		attr.setAttributeNS("ns", "attr1", "val2");

		byte[] bytes = TestUtils.serializeObject(attr);
		Attribute des = (Attribute) TestUtils.deserializeObject(bytes);

		assertThat(des, is(not(nullValue())));
		assertThat(des.getName(), is("pref:name"));
		assertThat(des.getNodeName(), is("pref:name"));
		assertThat(des.getLocalName(), is("name"));
		assertThat(des.getPrefix(), is("pref"));
		assertThat(des.getNamespaceURI(), is("ns"));

		assertThat(des.size(), is(3));
		assertThat(des.getValueAV(0), is(instanceOf(String.class)));
		assertThat((String) des.getValue(0), is("val1"));
		assertThat(des.getValueAV(1), is(instanceOf(AttributeValue.class)));
		assertThat((String) des.getValue(1), is("val2"));
		// for backward compatibility we are always serializing AttributeValues
		// tagged as replace as regular objects.
		assertThat(des.getValueAV(2), is(instanceOf(String.class)));
		assertThat((String) des.getValue(2), is("val3"));
		assertThat(des.getChildNodes().getLength(), is(4));
		assertThat(des.getLastChild(), is(instanceOf(Attribute.class)));
		assertThat(des.getLastChild().getNodeName(), is("pref:child"));

		assertThat(des.getOper(), is(Attribute.ATTRIBUTE_DELETE));

		assertThat(des.getAttribute("pref:attr"), is("val1"));
		assertThat(des.getAttributeNS("ns", "attr1"), is("val2"));
	}

	@Test
	public void test_getNodeType_Returns_Node_ELEMENT_NODE_Value() throws Exception {
		assertThat(new Attribute().getNodeType(), is(equalTo(Node.ELEMENT_NODE)));
	}

	@Test
	public void test_getNodeValue_When_The_Node_Has_No_Text_Children() throws Exception {
		assertThat(new Attribute("name").getNodeValue(), is(nullValue()));
	}

	@Test
	public void test_getNodeValue_When_The_Node_Has_A_Single_Text_Children() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");

		assertThat(attr.getNodeValue(), is("val1"));
	}

	@Test
	public void test_getNodeValue_When_The_Node_Has_Multiple_Text_Children() throws Exception {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.addValue("val3");

		assertThat(attr.getNodeValue(), is("val1 val2 val3"));
	}

	@Test
	public void test_setNodeValue_Replaces_Any_Existing_Values_But_Not_Element_Child() throws Exception {
		Attribute attr = new Attribute("name");

		attr.addValue("val1");
		attr.addValue("val2");
		attr.appendChild(new Attribute("child"));
		attr.addValue("val3");

		attr.setNodeValue("val");

		assertThat(attr.size(), is(1));
		assertThat(attr.getChildNodes().getLength(), is(2));
		assertThat(attr.getFirstChild(), is(instanceOf(Attribute.class)));
		assertThat(attr.getLastChild(), is(instanceOf(AttributeValue.class)));
		assertThat(attr.getLastChild().getNodeValue(), is("val"));
	}

	@Test
	public void test_setOper() throws Exception {
		Attribute attr = new Attribute("name");
		attr.setOper(Attribute.ATTRIBUTE_MOD);
		assertThat(attr.getOper(), is(Attribute.ATTRIBUTE_MOD));
	}

	@Test
	public void test_setOperation_With_Null() throws Exception {
		Attribute attr = new Attribute("name");

		attr.setOper(Attribute.ATTRIBUTE_ADD);
		assertThat(attr.getOper(), is(Attribute.ATTRIBUTE_ADD));

		attr.setOperation(null);
		assertThat(attr.getOper(), is(Attribute.ATTRIBUTE_REPLACE));

		attr.setOper(Attribute.ATTRIBUTE_ADD);
		assertThat(attr.getOper(), is(Attribute.ATTRIBUTE_ADD));

		attr.setOperation("");
		assertThat(attr.getOper(), is(Attribute.ATTRIBUTE_REPLACE));
	}

	@Test
	public void test_getOperation_With_Valid_Value() throws Exception {
		Attribute add = new Attribute("add");
		Attribute mod = new Attribute("mod");
		Attribute del = new Attribute("del");
		Attribute unchanged = new Attribute("unchanged");
		Attribute repl = new Attribute("repl");

		add.setOper(Attribute.ATTRIBUTE_ADD);
		mod.setOper(Attribute.ATTRIBUTE_MOD);
		del.setOper(Attribute.ATTRIBUTE_DELETE);
		unchanged.setOper(Attribute.ATTRIBUTE_UNCHANGED);
		repl.setOper(Attribute.ATTRIBUTE_REPLACE);

		assertThat(add.getOperation(), is(Attribute.OPER[1]));
		assertThat(mod.getOperation(), is(Attribute.OPER[4]));
		assertThat(del.getOperation(), is(Attribute.OPER[2]));
		assertThat(unchanged.getOperation(), is(Attribute.OPER[3]));
		assertThat(repl.getOperation(), is(Attribute.OPER[0]));
	}

	@Test
	public void test_setOperation_With_Unknown_Value() throws Exception {
		Attribute attr = new Attribute("clear");

		attr.setOperation("clear");

		// replace is the default type when the set value is unknown.
		assertThat(attr.getOperation(), is("replace"));
	}

	@Test
	public void test_getSchemaTypeInfo_Should_Return_Null() throws Exception {
		assertThat(new Attribute("name").getSchemaTypeInfo(), is(nullValue()));
	}

	@Test
	public void test_toDOM_Converts_A_Flat_Attribute() throws ParserConfigurationException {
		Attribute attr = new Attribute("name");
		attr.addValue("val1");
		attr.addValue(new AttributeValue("val2"));
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));

		Element attrAsDom = attr.toDOM(null);

		assertThat(attrAsDom.getChildNodes().getLength(), is(3));
		assertThat(attrAsDom.getChildNodes().item(0).getNodeValue(), is("val1"));
		assertThat(attrAsDom.getChildNodes().item(1).getNodeValue(), is("val2"));
		assertThat(attrAsDom.getChildNodes().item(2).getNodeValue(), is("val3"));
		assertThat(attrAsDom.getChildNodes().item(0).getNodeType(), is(Node.TEXT_NODE));
		assertThat(attrAsDom.getChildNodes().item(1).getNodeType(), is(Node.TEXT_NODE));
		assertThat(attrAsDom.getChildNodes().item(2).getNodeType(), is(Node.CDATA_SECTION_NODE));
	}

	@Test
	public void test_toDOM_Converts_A_DOM_Enabled_Attribute() throws ParserConfigurationException {
		Attribute attr = new Attribute("name", "ns", false);
		attr.addValue("val1");
		attr.addValue(new AttributeValue("val2"));
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));

		Attribute child1 = new Attribute("child1", "ns", false);
		child1.addValue("val1");
		child1.addValue(new AttributeValue("val2"));
		child1.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));
		attr.appendChild(child1);

		Attribute child2 = new Attribute("child2", "ns", false);
		child2.addValue("val1");
		child2.addValue(new AttributeValue("val2"));
		child2.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));
		attr.appendChild(child2);

		attr.setAttribute("pref:attr1", "val1");
		attr.setAttributeNS("ns", "pref:attr2", "val2");
		child1.setAttribute("pref:attr1", "val1");
		child1.setAttributeNS("ns", "pref:attr2", "val2");
		child2.setAttribute("pref:attr1", "val1");
		child2.setAttributeNS("ns", "pref:attr2", "val2");

		Element attrAsDom = attr.toDOM(null);

		Element current = attrAsDom;
		assertThat(current.getChildNodes().getLength(), is(5));
		validateNodeStructure(current);

		current = (Element) attrAsDom.getChildNodes().item(3);
		assertThat(current.getChildNodes().getLength(), is(3));
		validateNodeStructure(current);

		current = (Element) attrAsDom.getChildNodes().item(4);
		assertThat(current.getChildNodes().getLength(), is(3));
		validateNodeStructure(current);
	}

	private static final void validateNodeStructure(Element current) {
		assertThat(current.getChildNodes().item(0).getNodeValue(), is("val1"));
		assertThat(current.getChildNodes().item(1).getNodeValue(), is("val2"));
		assertThat(current.getChildNodes().item(2).getNodeValue(), is("val3"));
		assertThat(current.getChildNodes().item(0).getNodeType(), is(Node.TEXT_NODE));
		assertThat(current.getChildNodes().item(1).getNodeType(), is(Node.TEXT_NODE));
		assertThat(current.getChildNodes().item(2).getNodeType(), is(Node.CDATA_SECTION_NODE));
		assertThat(current.getAttributes().getLength(), is(2));
		assertThat(current.getAttributes().getNamedItem("pref:attr1").getNodeValue(), is("val1"));
		assertThat(current.getAttributes().getNamedItemNS("ns", "attr2").getNodeValue(), is("val2"));
	}

	@Test
	public void test_toString_And_toDeltaString_Returns_The_Same_String_For_The_Same_Attribute_Structures() throws Exception {
		Attribute attr = new Attribute("name", "ns", false);
		attr.addValue("val1");
		attr.addValue(new AttributeValue("val2"));
		attr.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));

		Attribute child1 = new Attribute("child1", "ns", false);
		child1.addValue("val1");
		child1.addValue(new AttributeValue("val2"));
		child1.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));
		attr.appendChild(child1);

		Attribute child2 = new Attribute("child2", "ns", false);
		child2.addValue("val1");
		child2.addValue(new AttributeValue("val2"));
		child2.addValue(new AttributeValue("val3", AttributeValue.AV_REPLACE, false));
		attr.appendChild(child2);

		attr.setAttribute("pref:attr1", "val1");
		attr.setAttributeNS("ns", "pref:attr2", "val2");
		child1.setAttribute("pref:attr1", "val1");
		child1.setAttributeNS("ns", "pref:attr2", "val2");
		child2.setAttribute("pref:attr1", "val1");
		child2.setAttributeNS("ns", "pref:attr2", "val2");

		Attribute clone = attr.clone();
		assertThat(attr.toString(), is(not(nullValue())));
		assertThat(attr.toDeltaString(), is(not(nullValue())));
		assertThat(clone.toString(), is(equalTo(attr.toString())));
		assertThat(clone.toDeltaString(), is(equalTo(attr.toDeltaString())));
	}
}
