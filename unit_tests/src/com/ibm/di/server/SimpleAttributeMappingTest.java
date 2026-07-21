package com.ibm.di.server;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.config.base.AttributeMapConfigImpl;
import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;

/**
 * @since 7.1
 */
public class SimpleAttributeMappingTest {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String DEST_ATTR_NAME = "attr.name";

	private static final String SRC_ATTR_NAME1 = "attr.name.1";

	private static final String SRC_ATTR_NAME2 = "attr.name.2";

	private static final String SRC_ATTR_VAL1 = "value1";

	private static final String SRC_ATTR_VAL2 = "value2";

	private static final String SRC_ATTR_NULL_VAL = "n";

	private static final String SRC_NS = "ns";

	private Entry src;
	private Entry srcH;

	private Entry newEntry;
	private Entry newEntryH;

	private static AttributeMapping singleMap;

	private static AttributeMapping multiMap;

	@BeforeClass
	public static void setUpClass() throws Exception {
		singleMap = createAttrMapWithSingleAttrSource();
		multiMap = createAttrMapWithMultiAttrSource();
	}

	@Before
	public void setUp() {
		src = new Entry();
		src.setAttribute(SRC_ATTR_NAME1, SRC_ATTR_VAL1);
		src.setAttribute(SRC_ATTR_NAME2, SRC_ATTR_VAL2);

		newEntry = new Entry();

		srcH = new Entry(true);
		Attribute elem = new Attribute(SRC_ATTR_NAME1, SRC_NS, false);
		elem.setValue(SRC_ATTR_VAL1);
		srcH.setAttribute(elem);

		elem = new Attribute(SRC_ATTR_NAME2, SRC_NS, false);
		elem.setValue(SRC_ATTR_VAL2);
		srcH.setAttribute(elem);

		newEntryH = new Entry(true);
	}

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
		ami.setType(AttributeMapItem.SIMPLE_MAPPING);
		ami.setSimple(SRC_ATTR_NAME1);
		x.setAttributeMapItem(ami);
		x.setNullDefinition("EmptyAttribute");
		x.setNullBehavior("value");
		x.setNullBehaviorValue(SRC_ATTR_NULL_VAL);
		return x;
	}

	private static AttributeMapConfig createMultiValueMapConfig() throws Exception {
		AttributeMapConfig x = new AttributeMapConfigImpl();
		AttributeMapItem ami = new AttributeMapItemImpl();
		ami.setName(DEST_ATTR_NAME);
		ami.setType(AttributeMapItem.SIMPLE_MAPPING);
		ami.setSimple(SRC_ATTR_NAME1 + "\r\n" + SRC_ATTR_NAME2);
		x.setAttributeMapItem(ami);
		x.setNullDefinition("EmptyAttribute");
		x.setNullBehavior("value");
		x.setNullBehaviorValue(SRC_ATTR_NULL_VAL);
		return x;
	}

	private static AttributeMapping createAttrMapWithSingleAttrSource() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig amConfig = createSingleValueMapConfig();
		am.loadMap(amConfig);
		return am;
	}

	private static AttributeMapping createAttrMapWithSingleAttrSourceAndNoNullVal() throws Exception {
		AttributeMapping noNullValMap = createTestAttributeMapping();
		AttributeMapConfig cfg = createSingleValueMapConfig();
		cfg.setNullBehavior(null);
		noNullValMap.loadMap(cfg);
		return noNullValMap;
	}

	private static AttributeMapping createAttrMapWithMultiAttrSource() throws Exception {
		AttributeMapping am = createTestAttributeMapping();
		AttributeMapConfig amConfig = createMultiValueMapConfig();
		am.loadMap(amConfig);
		return am;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_No_Merging_And_A_Single_Value_From_Source() throws Exception {
		singleMap.mapEntry(src, newEntry);

		assertThat(newEntry.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME).size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_No_Merging_And_Multiple_Values_From_Source() throws Exception {
		multiMap.mapEntry(src, newEntry);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_With_Merging_And_A_Single_Value_From_Source() throws Exception {
		newEntry.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		singleMap.mapEntry(src, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_With_Merging_And_Multiple_Values_From_Source() throws Exception {
		newEntry.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		multiMap.mapEntry(src, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_No_Merging_And_A_Single_Value_From_Source() throws Exception {
		singleMap.mapEntry(src, newEntryH);

		assertThat(newEntryH.size(), is(1));
		assertThat(newEntryH.getAttribute(DEST_ATTR_NAME).size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_No_Merging_And_Multiple_Values_From_Source() throws Exception {
		multiMap.mapEntry(src, newEntryH);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_With_Merging_And_A_Single_Value_From_Source() throws Exception {
		newEntryH.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		singleMap.mapEntry(src, newEntryH, true);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_With_Merging_And_Multiple_Values_From_Source() throws Exception {
		newEntryH.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		multiMap.mapEntry(src, newEntryH, true);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_No_Merging_And_A_Single_Value_From_Source() throws Exception {
		singleMap.mapEntry(srcH, newEntry);

		assertThat(newEntry.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME).size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME).getNamespaceURI(), is(SRC_NS));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_No_Merging_And_Multiple_Values_From_Source() throws Exception {
		multiMap.mapEntry(srcH, newEntry);

		assertThat(newEntry.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME).getNamespaceURI(), is(SRC_NS));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_With_Merging_And_A_Single_Value_From_Source() throws Exception {
		newEntry.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		singleMap.mapEntry(srcH, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_With_Merging_And_Multiple_Values_From_Source() throws Exception {
		newEntry.setAttribute(DEST_ATTR_NAME, SRC_ATTR_VAL2);
		multiMap.mapEntry(srcH, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_VAL1, SRC_ATTR_VAL2, SRC_ATTR_VAL2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_No_Merging_And_A_Null_Value_From_Source() throws Exception {
		src.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(src, newEntry);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_No_Merging_And_A_Null_Value_From_Source() throws Exception {
		src.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(src, newEntryH);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_No_Merging_And_A_Null_Value_From_Source() throws Exception {
		srcH.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(srcH, newEntry);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_With_Merging_And_A_Null_Value_From_Source() throws Exception {
		src.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(src, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_With_Merging_And_A_Null_Value_From_Source() throws Exception {
		src.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(src, newEntryH, true);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_With_Merging_And_A_Null_Value_From_Source() throws Exception {
		srcH.getAttribute(SRC_ATTR_NAME1).clear();
		singleMap.mapEntry(srcH, newEntry, true);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_No_Merging_And_No_Value_From_Source_And_Empty_newEntry_And_NullValueBehavior()
			throws Exception {
		src.removeAllAttributes();
		singleMap.mapEntry(src, newEntry);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_No_Merging_And_No_Value_From_Source_And_Empty_newEntry_And_NullValueBehavior()
			throws Exception {
		src.removeAllAttributes();
		singleMap.mapEntry(src, newEntryH);

		assertThat(newEntryH.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_No_Merging_And_No_Value_From_Source_And_Empty_newEntry_And_NullValueBehavior()
			throws Exception {
		srcH.removeAllAttributes();
		singleMap.mapEntry(srcH, newEntry);

		assertThat(newEntry.size(), is(1));
		ArrayList<String> list = new ArrayList<String>(newEntry.getAttribute(DEST_ATTR_NAME).getValuesVector());
		assertThat(list, containsInAnyOrder(SRC_ATTR_NULL_VAL));
	}

	@Test
	public void test_Map_Of_Flat_Entry_To_Another_Flat_One_No_Merging_And_No_Value_From_Source_And_Non_Empty_newEntry_And_No_NullValueBehavior()
			throws Exception {
		src.removeAllAttributes();
		newEntry.setAttribute("some", "val");
		createAttrMapWithSingleAttrSourceAndNoNullVal().mapEntry(src, newEntry);

		assertThat(newEntry.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME), is(nullValue()));
	}

	@Test
	public void test_Map_Of_Flat_Entry_To_Hierarchical_One_No_Merging_And_No_Value_From_Source_And_Non_Empty_newEntry_And_No_NullValueBehavior()
			throws Exception {
		src.removeAllAttributes();
		newEntryH.setAttribute("some", "val");
		createAttrMapWithSingleAttrSourceAndNoNullVal().mapEntry(src, newEntryH);

		assertThat(newEntryH.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME), is(nullValue()));
	}

	@Test
	public void test_Map_Of_Hierarchical_Entry_To_Flat_One_No_Merging_And_No_Value_From_Source_And_Non_Empty_newEntry_And_No_NullValueBehavior()
			throws Exception {
		srcH.removeAllAttributes();
		newEntry.setAttribute("some", "val");
		createAttrMapWithSingleAttrSourceAndNoNullVal().mapEntry(srcH, newEntry);

		assertThat(newEntry.size(), is(1));
		assertThat(newEntry.getAttribute(DEST_ATTR_NAME), is(nullValue()));
	}
}
