package com.ibm.di.server;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.di.config.base.AttributeMapConfigImpl;
import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AttributeMapping.SingleAttributeMap;

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
public class AdvancedAttributeMappingTest {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DEST_ATTR_NAME = "attr.name";

	private static final String SRC_ATTR_NULL_VAL = "n";

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

	private static AttributeMapConfig createSingleValueMapConfig() throws Exception {
		AttributeMapConfig x = new AttributeMapConfigImpl();
		AttributeMapItem ami = new AttributeMapItemImpl();
		ami.setName(DEST_ATTR_NAME);
		ami.setType(AttributeMapItem.ADVANCED_MAPPING);
		ami.setScript("ret.value=obj;");
		x.setAttributeMapItem(ami);
		x.setNullDefinition("EmptyAttribute");
		x.setNullBehavior("value");
		x.setNullBehaviorValue(SRC_ATTR_NULL_VAL);
		return x;
	}

	private static AttributeMapping createSingleValueMapWithIdentityValueMapping() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());
		am.declareBean("ret", am);
		return am;
	}

	@Test
	public void test_mapAttribute_For_Null_Value() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		am.declareBean("obj", null);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		assertThat(((Attribute) val).getValue(), is(SRC_ATTR_NULL_VAL));
	}

	@Test
	public void test_mapAttribute_For_Attribute_Value_Which_Results_To_Null() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		Attribute a = new Attribute();
		am.declareBean("obj", a);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		assertThat(((Attribute) val).getValue(), is(SRC_ATTR_NULL_VAL));
	}

	@Test
	public void test_mapAttribute_For_Attribute_Value_Which_Does_Not_Result_To_Null() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		Attribute a = new Attribute("name", "value");
		am.declareBean("obj", a);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		assertThat((Attribute) val, is(sameInstance(a)));
	}

	@Test
	public void test_mapAttribute_For_TCB_Value() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		TaskCallBlock tcb = new TaskCallBlock();
		am.declareBean("obj", tcb);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		assertThat(((Attribute) val).getValue(0), is(sameInstance((Object) tcb)));
	}

	@Test
	public void test_mapAttribute_For_Entry_Value() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		Entry e = new Entry();
		am.declareBean("obj", e);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Entry.class)));
		assertThat((Entry) val, is(sameInstance(e)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_mapAttribute_For_List_Containing_No_Attributes_Not_Resulting_To_Null() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		List<String> l = new ArrayList<String>();
		l.add("str1");
		l.add("str2");
		am.declareBean("obj", l);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		ArrayList<Object> list = new ArrayList<Object>(((Attribute) val).getValuesVector());
		assertThat(list, contains(l.toArray()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_mapAttribute_For_List_Containing_Attributes_Not_Resulting_To_Null() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		List<Attribute> l = new ArrayList<Attribute>();
		l.add(new Attribute("attr1", "str1"));
		l.add(new Attribute("attr2", "str2"));
		am.declareBean("obj", l);

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		ArrayList<String> list = new ArrayList<String>(((Attribute) val).getValuesVector());
		assertThat(list, contains("str1", "str2"));
	}

	@Test
	public void test_mapAttribute_For_Regular_Value_Not_Resulting_To_Null() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		am.declareBean("obj", "v");

		Object val = am.mapAttribute(DEST_ATTR_NAME);
		assertThat(val, is(instanceOf(Attribute.class)));
		assertThat(((Attribute) val).getValue(), is("v"));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Null() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		assertThat(sam.isNull(null), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Not_Null_And_NVD_Is_Not_Specified() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		assertThat(sam.isNull(new Attribute()), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Empty_But_Flat_And_Dont_Belong_To_Entry() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		assertThat(sam.isNull(new Attribute()), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Empty_But_Flat_And_Belongs_To_Flat_Entry() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		Entry e = new Entry();
		assertThat(sam.isNull(e.newAttribute("a")), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Empty_And_Belongs_To_Hierarchical_Entry_And_Does_Not_Have_Children() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		Entry e = new Entry(true);
		assertThat(sam.isNull(e.newAttribute("a")), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Empty_And_Belongs_To_Hierarchical_Entry_But_Has_Children() throws Exception {
		AttributeMapping am = createSingleValueMapWithIdentityValueMapping();
		SingleAttributeMap sam = am.new SingleAttributeMap();
		Entry e = new Entry(true);
		e.newAttribute("a.b");
		assertThat(sam.isNull(e.newAttribute("a")), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Is_Not_Empty_And_NVD_Is_Set_To_Empty_Attribute() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("EmptyAttribute");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue("");
		assertThat(sam.isNull(a), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Multiple_Values_And_NVD_Is_Set_To_Empty_String() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("EmptyString");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();

		Attribute a = new Attribute();
		a.addValue("1");
		a.addValue("2");
		assertThat(sam.isNull(a), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Multiple_Values_And_NVD_Is_Set_To_Value() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("Value");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();

		Attribute a = new Attribute();
		a.addValue("1");
		a.addValue("2");
		assertThat(sam.isNull(a), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Null_Value_And_NVD_Is_Set_To_Empty_String() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("EmptyString");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue(null);
		assertThat(sam.isNull(a), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Empty_String_Value_And_NVD_Is_Set_To_Empty_String() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("EmptyString");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue("");
		assertThat(sam.isNull(a), is(true));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Object_Value_And_NVD_Is_Set_To_Empty_String() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("EmptyString");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue(new Object());
		assertThat(sam.isNull(a), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Has_Object_Value_And_NVD_Is_Set_To_Value() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("Value");
		x.setNullDefinitionValue("n");

		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue(new Object());
		assertThat(sam.isNull(a), is(false));
	}

	@Test
	public void test_isNull_When_Attribute_Has_String_Value_And_NVD_Is_Set_To_Value_And_Is_Ingoring_Case() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = new AttributeMapConfigImpl();
		x.setNullDefinition("Value");
		x.setNullDefinitionValue("nuLL");
		am.loadMap(x);

		SingleAttributeMap sam = am.new SingleAttributeMap();
		Attribute a = new Attribute();
		a.setValue("NuLl");
		assertThat(sam.isNull(a), is(true));
	}

	@Test
	public void test_mapEntry_Script_Returns_Flat_Entry_No_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Entry src = new Entry();
		src.setAttribute("a", "v1");
		src.setAttribute("a.b", "v2");

		am.declareBean("obj", src);
		Entry e = am.mapEntry(null, null, false);

		assertThat(e.getAttribute("a"), is(notNullValue()));
		assertThat(e.getAttribute("a").size(), is(1));
		assertThat(e.getAttribute("a").getValue(), is("v1"));
		assertThat(e.getAttribute("a.b"), is(notNullValue()));
		assertThat(e.getAttribute("a.b").size(), is(1));
		assertThat(e.getAttribute("a.b").getValue(), is("v2"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Hierarhical_Entry_No_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Entry src = new Entry(true);
		Attribute elem = src.createElementNS("ns1", "p1:a");
		elem.setValue("v1");
		src.appendChild(elem);

		elem = src.createElementNS("ns2", "p2:b");
		elem.setValue("v2");
		src.appendChild(elem);

		am.declareBean("obj", src);
		Entry e = am.mapEntry(null, null, false);

		assertThat(e.getAttribute("p1:a"), is(notNullValue()));
		assertThat(e.getAttribute("p1:a").size(), is(1));
		assertThat(e.getAttribute("p1:a").getValue(), is("v1"));
		assertThat(e.getAttribute("p1:a").getNamespaceURI(), is("ns1"));
		assertThat(e.getAttribute("p2:b"), is(notNullValue()));
		assertThat(e.getAttribute("p2:b").size(), is(1));
		assertThat(e.getAttribute("p2:b").getValue(), is("v2"));
		assertThat(e.getAttribute("p2:b").getNamespaceURI(), is("ns2"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Flat_Entry_With_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Entry src = new Entry();
		src.setAttribute("a", "v1");
		src.setAttribute("a.b", "v2");

		am.declareBean("obj", src);

		Entry e = new Entry();
		e.setAttribute("a", "v");
		e = am.mapEntry(null, e, true);

		assertThat(e.getAttribute("a"), is(notNullValue()));
		assertThat(e.getAttribute("a").size(), is(2));
		assertThat(e.getAttribute("a").getValue(0), is((Object) "v"));
		assertThat(e.getAttribute("a").getValue(1), is((Object) "v1"));
		assertThat(e.getAttribute("a.b"), is(notNullValue()));
		assertThat(e.getAttribute("a.b").size(), is(1));
		assertThat(e.getAttribute("a.b").getValue(), is("v2"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Hierarhical_Entry_With_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Entry src = new Entry(true);
		Attribute elem = src.createElementNS("ns1", "p1:a");
		elem.setValue("v1");
		src.appendChild(elem);

		elem = src.createElementNS("ns2", "p2:b");
		elem.setValue("v2");
		src.appendChild(elem);

		am.declareBean("obj", src);

		Entry e = new Entry();
		e.setAttribute("p1:a", "v");
		e = am.mapEntry(null, e, true);

		assertThat(e.getAttribute("p1:a"), is(notNullValue()));
		assertThat(e.getAttribute("p1:a").size(), is(2));
		assertThat(e.getAttribute("p1:a").getValue(0), is((Object) "v"));
		assertThat(e.getAttribute("p1:a").getValue(1), is((Object) "v1"));
		assertThat(e.getAttribute("p1:a").getNamespaceURI(), is(nullValue()));
		assertThat(e.getAttribute("p2:b"), is(notNullValue()));
		assertThat(e.getAttribute("p2:b").size(), is(1));
		assertThat(e.getAttribute("p2:b").getValue(), is("v2"));
		assertThat(e.getAttribute("p2:b").getNamespaceURI(), is(nullValue()));
	}

	@Test
	public void test_mapEntry_Script_Returns_Flat_Attribute_No_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Attribute a = new Attribute("a", "v1");

		am.declareBean("obj", a);
		Entry e = am.mapEntry(null, null, false);

		assertThat(e.getAttribute(DEST_ATTR_NAME), is(notNullValue()));
		assertThat(e.getAttribute(DEST_ATTR_NAME).size(), is(1));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(), is("v1"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Hierarhical_Attribute_No_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Attribute a = new Attribute("a", "ns1", false);
		a.setValue("v1");

		am.declareBean("obj", a);
		Entry e = am.mapEntry(null, null, false);

		assertThat(e.getAttribute(DEST_ATTR_NAME), is(notNullValue()));
		assertThat(e.getAttribute(DEST_ATTR_NAME).size(), is(1));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(), is("v1"));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getNamespaceURI(), is("ns1"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Flat_Attribute_With_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Attribute a = new Attribute("a", "v1");
		am.declareBean("obj", a);

		Entry e = new Entry();
		e.setAttribute(DEST_ATTR_NAME, "v");
		e = am.mapEntry(null, e, true);

		assertThat(e.getAttribute(DEST_ATTR_NAME), is(notNullValue()));
		assertThat(e.getAttribute(DEST_ATTR_NAME).size(), is(2));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(0), is((Object) "v"));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(1), is((Object) "v1"));
	}

	@Test
	public void test_mapEntry_Script_Returns_Hierarhical_Attribute_With_Merging_Enabled_To_Flat_Entry() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		am.loadMap(createSingleValueMapConfig());

		Attribute a = new Attribute("a", "ns1", false);
		a.setValue("v1");
		am.declareBean("obj", a);

		Entry e = new Entry();
		e.setAttribute(DEST_ATTR_NAME, "v");
		e = am.mapEntry(null, e, true);

		assertThat(e.getAttribute(DEST_ATTR_NAME), is(notNullValue()));
		assertThat(e.getAttribute(DEST_ATTR_NAME).size(), is(2));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(0), is((Object) "v"));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getValue(1), is((Object) "v1"));
		assertThat(e.getAttribute(DEST_ATTR_NAME).getNamespaceURI(), is(nullValue()));
	}

	@SuppressWarnings("serial")
	private static class MethodCalledDetectorEntry extends Entry {
		int removeCalledCounter = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.di.entry.Entry#removeAttribute(java.lang.String)
		 */
		@Override
		public void removeAttribute(String name) {
			removeCalledCounter++;
			super.removeAttribute(name);
		}
	}

	@Test
	public void test_mapEntry_Script_Returns_Null_Value_And_removeAttribute_Is_Not_Called() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = createSingleValueMapConfig();
		x.setNullDefinition(null);
		x.setNullBehavior(null);
		x.setNullBehaviorValue(null);
		am.loadMap(x);
		am.declareBean("obj", null);

		MethodCalledDetectorEntry e = new MethodCalledDetectorEntry();

		// must call removeAttribute() when not merging and when there is
		// something in the entry.
		am.mapEntry(null, e, false);
		assertThat(e.removeCalledCounter, is(0));
	}

	@Test
	public void test_mapEntry_Script_Returns_Null_Value_And_removeAttribute_Is_Not_Called_If_We_Are_Merging() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = createSingleValueMapConfig();
		x.setNullDefinition(null);
		x.setNullBehavior(null);
		x.setNullBehaviorValue(null);
		am.loadMap(x);
		am.declareBean("obj", null);

		MethodCalledDetectorEntry e = new MethodCalledDetectorEntry();

		// must call removeAttribute() when not merging and when there is
		// something in the entry.
		am.mapEntry(null, e, true);
		assertThat(e.removeCalledCounter, is(0));
	}

	@Test
	public void test_mapEntry_Script_Returns_Null_Value_And_removeAttribute_Is_Called_If_Entry_Is_Not_Empty() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig x = createSingleValueMapConfig();
		x.setNullDefinition(null);
		x.setNullBehavior(null);
		x.setNullBehaviorValue(null);
		am.loadMap(x);
		am.declareBean("obj", null);

		MethodCalledDetectorEntry e = new MethodCalledDetectorEntry();
		e.setAttribute("a", "v");

		// must call removeAttribute() when not merging and when there is
		// something in the entry.
		am.mapEntry(null, e, false);
		assertThat(e.removeCalledCounter, is(1));
	}
}
