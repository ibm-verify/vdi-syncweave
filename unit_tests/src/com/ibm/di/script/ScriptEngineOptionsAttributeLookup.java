
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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
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
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.jscript.types.FBSString;

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
public class ScriptEngineOptionsAttributeLookup {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private ScriptEngineOptions seo = new ScriptEngineOptions();

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Local_Name() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(false));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Tag_Name() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "pref:attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(false));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));

		assertThat(seo.getProperty(e, "attr").toJavaObject(), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Local_Name_NS1() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Local_Name_NS2() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Tag_Name_NS1() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Tag_Name_NS2() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "pref:attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Tag_Name_NS3() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Tag_Name_NS4() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}pref:attr").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Local_Name() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "attr.name", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(false));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Tag_Name() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "pref:attr.pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "pref:attr.pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(false));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));

		assertThat(seo.getProperty(e, "attr").toJavaObject(), is(nullValue()));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Local_Name_NS1() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr.{ns}name", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Local_Name_NS2() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}attr.{ns}name", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}attr.{ns}name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Tag_Name_NS1() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Tag_Name_NS2() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "pref:attr.pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Tag_Name_NS3() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}attr.{ns}name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Able_To_Find_Attribute_With_Multiple_Tag_Name_NS4() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "{ns}pref:attr.{ns}pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		Attribute a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Local_Names() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "attr.name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names1() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "pref:attr.pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names2() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "pref:attr.pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.pref:attr.pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Local_Names_NS1() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}attr.{ns}name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Local_Names_NS2() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}attr.{ns}name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.{ns}attr.{ns}name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names_NS1() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.attr.name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names_NS2() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.pref:attr.pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names_NS3() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.{ns}attr.{ns}name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Attribute_Able_To_Find_Attribute_With_Multiple_Tag_Names_NS4() throws Exception {
		Attribute a = new Attribute("a");
		Entry e = new Entry();
		e.setAttribute(a);
		seo.putProperty(a, "{ns}pref:attr.{ns}pref:name", FBSString.get("val"));

		Object o = seo.getProperty(e, "a.{ns}pref:attr.{ns}pref:name").toJavaObject();
		assertThat(o, is(instanceOf(Attribute.class)));
		assertThat(e.isDOMEnabled(), is(true));

		a = (Attribute) o;
		assertThat(a.getValue(), is("val"));
	}

	@Test
	public void test_Entry_Does_Not_Create_A_Second_Attribute_Because_It_Is_Unable_To_Find_The_First_One() throws Exception {
		Entry e = new Entry();
		seo.putProperty(e, "{http://www.example.com/library/}ns:book.{http://www.example.com/library/}ns2:title", FBSString
				.get("title"));

		seo.putProperty(e, "book.author", FBSString.get("author"));
		assertThat(e.getChildNodes().getLength(), is(1));
	}
}
