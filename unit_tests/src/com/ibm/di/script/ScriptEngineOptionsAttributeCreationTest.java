
package com.ibm.di.script;

import static org.hamcrest.beans.HasProperty.*;
import static org.hamcrest.beans.HasPropertyWithValue.*;
import static org.hamcrest.beans.SamePropertyValuesAs.*;
import static org.hamcrest.collection.IsArray.*;
import static org.hamcrest.collection.IsArrayContaining.*;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.*;
import static org.hamcrest.collection.IsArrayContainingInOrder.*;
import static org.hamcrest.collection.IsArrayWithSize.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.collection.IsEmptyCollection.*;
import static org.hamcrest.collection.IsEmptyIterable.*;
import static org.hamcrest.collection.IsIn.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.*;
import static org.hamcrest.collection.IsIterableWithSize.*;
import static org.hamcrest.collection.IsMapContaining.*;
import static org.hamcrest.core.AllOf.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.DescribedAs.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsAnything.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsSame.*;
import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.hamcrest.object.HasToString.*;
import static org.hamcrest.object.IsCompatibleType.*;
import static org.hamcrest.object.IsEventFrom.*;
import static org.hamcrest.text.IsEmptyString.*;
import static org.hamcrest.text.IsEqualIgnoringCase.*;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.*;
import static org.hamcrest.text.StringContainsInOrder.*;
import static org.hamcrest.xml.HasXPath.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.jscript.IValue;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSObject;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSValue;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class ScriptEngineOptionsAttributeCreationTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final IValue VALUE_NULL = FBSNull.nullValue;

	private ScriptEngineOptions seo = new ScriptEngineOptions();

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr").size(), is(0));
		assertThat(e.getObject("attr"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr").size(), is(1));
		assertThat(e.getString("attr"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr").size(), is(1));
		assertThat(e.getString("attr"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr").size(), is(0));
		assertThat(e.getObject("pref:attr"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr").size(), is(1));
		assertThat(e.getString("pref:attr"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr").size(), is(1));
		assertThat(e.getString("pref:attr"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_NS_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr").size(), is(0));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getObject("attr"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_NS_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr").size(), is(1));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getString("attr"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Local_Name_NS_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr").size(), is(1));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getString("attr"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_NS_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr").size(), is(0));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getObject("pref:attr"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_NS_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr").size(), is(1));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getString("pref:attr"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Tag_Name_NS_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr").size(), is(1));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getString("pref:attr"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr.name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr"), is(nullValue()));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").size(), is(0));
		assertThat(e.getObject("attr.name"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr.name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr"), is(nullValue()));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").size(), is(1));
		assertThat(e.getString("attr.name"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr.name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("attr"), is(nullValue()));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").size(), is(1));
		assertThat(e.getString("attr.name"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr.pref:name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr"), is(nullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(0));
		assertThat(e.getObject("pref:attr.pref:name"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr.pref:name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr"), is(nullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("pref:attr.pref:name"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr.pref:name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(false));
		assertThat(e.getAttribute("pref:attr"), is(nullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("pref:attr.pref:name"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr.{ns}name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr"), is(notNullValue()));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr").size(), is(0));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr.name").size(), is(0));
		assertThat(e.getObject("attr.name"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr.{ns}name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr"), is(notNullValue()));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr").size(), is(0));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr.name").size(), is(1));
		assertThat(e.getString("attr.name"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr.{ns}name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("attr"), is(notNullValue()));
		assertThat(e.getAttribute("attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr").size(), is(0));
		assertThat(e.getAttribute("attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("attr.name").size(), is(1));
		assertThat(e.getString("attr.name"), is("value"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_Null_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr").size(), is(0));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(0));
		assertThat(e.getObject("pref:attr.pref:name"), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_String_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr").size(), is(0));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("pref:attr.pref:name"), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_Attribute_Value() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr").size(), is(0));
		assertThat(e.getAttribute("pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("pref:attr.pref:name"), is("value"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_And_Null_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "attr.name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").size(), is(0));
		assertThat(e.getObject("a.attr.name"), is(nullValue()));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_And_String_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "attr.name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").size(), is(1));
		assertThat(e.getString("a.attr.name"), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_And_Attribute_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "attr.name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").size(), is(1));
		assertThat(e.getString("a.attr.name"), is("value"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_Null_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "pref:attr.pref:name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(0));
		assertThat(e.getObject("a.pref:attr.pref:name"), is(nullValue()));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_String_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "pref:attr.pref:name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("a.pref:attr.pref:name"), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_And_Attribute_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "pref:attr.pref:name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("a.pref:attr.pref:name"), is("value"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_Null_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}attr.{ns}name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr").size(), is(0));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr.name").size(), is(0));
		assertThat(e.getObject("a.attr.name"), is(nullValue()));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_String_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}attr.{ns}name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr").size(), is(0));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr.name").size(), is(1));
		assertThat(e.getString("a.attr.name"), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Local_Names_NS_And_Attribute_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}attr.{ns}name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr").size(), is(0));
		assertThat(e.getAttribute("a.attr.name"), is(notNullValue()));
		assertThat(e.getAttribute("a.attr.name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.attr.name").size(), is(1));
		assertThat(e.getString("a.attr.name"), is("value"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_Null_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", VALUE_NULL);

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr").size(), is(0));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(0));
		assertThat(e.getObject("a.pref:attr.pref:name"), is(nullValue()));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_String_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr").size(), is(0));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("a.pref:attr.pref:name"), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Create_Attribute_With_Multiple_Tag_Names_NS_And_Attribute_Value() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", seo.wrapObject(new Attribute("name", "value")));

		assertThat(e.size(), is(1));
		assertThat(e.isDOMEnabled(), is(true));
		assertThat(e.getAttribute("a.pref:attr"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr").size(), is(0));
		assertThat(e.getAttribute("a.pref:attr.pref:name"), is(notNullValue()));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getPrefix(), is("pref"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").getNamespaceURI(), is("ns"));
		assertThat(e.getAttribute("a.pref:attr.pref:name").size(), is(1));
		assertThat(e.getString("a.pref:attr.pref:name"), is("value"));
	}
}
