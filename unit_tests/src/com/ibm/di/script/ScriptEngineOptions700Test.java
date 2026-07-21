package com.ibm.di.script;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.NodeList;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.test.utils.TestUtils;

public class ScriptEngineOptions700Test {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static ScriptEngine se;
	private static Entry ret;

	@BeforeClass
	public static void setUpScriptEngine() throws Exception {
		se = new ScriptEngine(null);
		ret = new Entry();
		se.declareBean("ret", ret);
	}

	@After
	public void clearTestEnv() {
		se.undeclareBean("work");
		TestUtils.clearEntry(ret);
	}

	@Test
	public void test_Get_Property_Entry() throws Exception {
		Entry work = new Entry(true);

		work.setProperty("testProp", "propVal");
		work.newAttribute("prefix:attrName.prefix:attrValue");
		work.newAttribute("attrName.attrVal");

		se.declareBean("work", work);

		se.interpret("ret.setAttribute(\"testProp\", work.@testProp);" + "ret.setAttribute(\"localName\", work.attrName);"
				+ "ret.setAttribute(\"singleVal\", work[\"prefix:attrName\"]);" + "ret.setAttribute(\"nullVal\", work.nullAttr);");

		assertEquals(3, ret.size());
		assertEquals("propVal", ret.getAttribute("testProp").getValue());
		assertEquals(work.getAttribute("prefix:attrName").getValue(), ret.getAttribute("singleVal").getValue());
		assertEquals(null, ret.getAttribute("nullAttr"));

	}

	@Test
	public void test_Get_Property_Attribute() throws Exception {
		Entry work = new Entry();

		Attribute attr = work.newAttribute("workAttr");
		attr.setAttribute("propName", "propValue");
		attr.setAttribute("pref:propN", "propV");
		attr.addValue("val1");
		attr.addValue("val2");
		attr.appendChild(work.createElement("child1"));
		attr.appendChild(work.createElement("pref:child2"));

		se.declareBean("work", work);

		se.interpret("ret.setAttribute(\"propName\", work.workAttr.@propName);"
				+ "ret.setAttribute(\"propN\", work.workAttr[\"@pref:propN\"]);"
				+ "ret.setAttribute(\"attrVal1\", work.workAttr[0]);" + "ret.setAttribute(\"attrVal2\", work.workAttr[1]);"
				+ "work.workAttr.child1.setValue(\"childVal1\");" + "work.workAttr.child2.setValue(\"childVal2\");"
				+ "ret.setAttribute(\"child1\", work.workAttr.child1);"
				+ "ret.setAttribute(\"child2\", work.workAttr[\"pref:child2\"]);");

		assertEquals(6, ret.size());
		assertEquals("propValue", ret.getAttribute("propName").getValue());
		assertEquals("propV", ret.getAttribute("propN").getValue());
		assertEquals("val1", ret.getAttribute("attrVal1").getValue());
		assertEquals("val2", ret.getAttribute("attrVal2").getValue());
		assertEquals("childVal1", ret.getAttribute("child1").getValue());
		assertEquals("childVal2", ret.getAttribute("child2").getValue());
	}

	@Test
	public void test_Get_Property_NodeList() throws Exception {
		Entry work = new Entry();

		Attribute attr = work.newAttribute("workAttr");
		attr.appendChild(work.createElement("child1"));
		attr.getFirstChild().appendChild(work.createElement("child12"));
		attr.getFirstChild().appendChild(work.createElement("pref:child12"));
		attr.appendChild(work.createElement("child1"));
		attr.getLastChild().appendChild(work.createElement("child12"));
		attr.appendChild(work.createElement("pref:child2"));
		attr.getLastChild().appendChild(work.createElement("pref:child22"));
		attr.appendChild(work.createElement("pref:child2"));
		attr.getLastChild().appendChild(work.createElement("pref:child22"));
		attr.getLastChild().appendChild(work.createElement("child22"));

		se.declareBean("work", work);
		se.declareBean("ret", ret);

		se.interpret("ret.setAttribute(\"child1Size\", work.workAttr.child1.child12);"
				+ "work.workAttr.child1[\"pref:child12\"].setValue(\"singleVal\");"
				+ "ret.setAttribute(\"pref:child1Size\", work.workAttr.child1[\"pref:child12\"]);"
				+ "ret.setAttribute(\"child2Size\", work.workAttr[\"pref:child2\"].child22);"
				+ "ret.setAttribute(\"pref:child2Size\", work.workAttr.child2[\"pref:child22\"]);");

		assertEquals(4, ret.size());
		assertEquals(3, ((NodeList) ret.getAttribute("child1Size").getValue(0)).getLength());
		assertEquals("singleVal", ret.getAttribute("pref:child1Size").getValue());
		assertEquals(3, ((NodeList) ret.getAttribute("child2Size").getValue(0)).getLength());
		assertEquals(2, ((NodeList) ret.getAttribute("pref:child2Size").getValue(0)).getLength());
	}

	@Test
	public void test_Get_Property_List_Index() throws Exception {
		List<String> list = new ArrayList<String>();

		list.add("value1");
		list.add("value2");

		se.declareBean("list", list);
		se.interpret("ret.setAttribute(\"value1\", list[0]);\n" + "ret.setAttribute(\"value2\", list[1]);\n");

		assertEquals(2, ret.size());
		assertEquals("value1", ret.getString("value1"));
		assertEquals("value2", ret.getString("value2"));
	}

	@Test(expected = java.lang.IndexOutOfBoundsException.class)
	public void test_Get_Property_List_Exception() throws Exception {
		List<String> list = new ArrayList<String>();

		list.add("value1");
		list.add("value2");

		se.declareBean("list", list);

		se.interpret("ret.setAttribute(\"value3\", list[2]);\n");
	}

	@Test
	public void test_Get_Property_Entry_Property() throws Exception {
		Entry work = new Entry();

		work.setProperty("prop", "val");

		se.declareBean("work", work);

		se.interpret("ret.setAttribute(\"prop\", work.@prop);\n");

		assertEquals(1, ret.size());
		assertEquals("val", ret.getAttribute("prop").getValue());
	}

	@Test
	public void test_Put_Property_Entry() throws Exception {
		se.interpret("	ret.@testProp=\"testPropVal\";				" + //
				"		ret[\"local.Name1\"]=\"test1\";				" + //
				"		ret[\"pref:local\\\\.Name2\"]=\"test2\";	" + //
				"		ret[\"{ns1}local.Name3\"]=\"test3\";		" + //
				"		ret[\"{ns2}pref:local\\\\.Name4\"]=\"test4\";");

		assertEquals(3, ret.size());
		assertEquals("testPropVal", ret.getProperty("testProp"));
		assertEquals(null, ret.getObject("local.Name1"));
		assertEquals("test2", ret.getObject("pref:local\\.Name2"));
		assertEquals("test3", ret.getObject("local.Name3"));
		assertEquals("ns1", ret.getAttribute("local.Name3").getNamespaceURI());
		assertEquals("test4", ret.getObject("pref:local\\.Name4"));
		assertEquals("ns2", ret.getAttribute("pref:local\\.Name4").getNamespaceURI());

		// check that the values are replaced...

		se.interpret("	ret.@testProp=\".testPropVal\";				" + //
				"		ret[\"local.Name1\"]=\".test1\";			" + //
				"		ret[\"pref:local\\\\.Name2\"]=\".test2\";	" + //
				"		ret[\"{ns1}local.Name3\"]=\".test3\";		" + // 
				"		ret[\"{ns2}pref:local\\\\.Name4\"]=\".test4\";	");

		assertEquals(4, ret.size());
		assertEquals(".testPropVal", ret.getProperty("testProp"));
		assertEquals(".test1", ret.getObject("local.Name1"));
		assertEquals(".test2", ret.getObject("pref:local\\.Name2"));
		assertEquals(".test3", ret.getObject("local.Name3"));
		assertEquals("ns1", ret.getAttribute("local.Name3").getNamespaceURI());
		assertEquals(".test4", ret.getObject("pref:local\\.Name4"));
		assertEquals("ns2", ret.getAttribute("pref:local\\.Name4").getNamespaceURI());

	}

	@Test
	public void test_Put_Property_Attribute() throws Exception {
		se.interpret("	ret.newAttribute(\"attr\");" //
				+ "		ret.newAttribute(\"list\");" //
				// test array like behavior
				+ "		ret.list[0] = \"val0\";" //
				+ "		ret.list[1] = \"xxx\";" //
				+ "		ret.list[2] = \"val2\";" //
				+ "		ret.list[1] = \"val1\";"

				// test properties handling
				+ "		ret.attr[\"@local.Name1\"]=\"propTest1\";" //
				+ "		ret.attr[\"@pref:local.Name2\"]=\"propTest2\";" //
				+ "		ret.attr[\"@{ns1}local.Name3\"]=\"propTest3\";" //
				+ "		ret.attr[\"@{ns2}pref:local.Name4\"]=\"propTest4\";"

				// test child handling
				+ "		ret.attr[\"local\\\\.Name1\"]=\"test1\";" //
				+ "		ret.attr[\"pref:local.Name2\"]=\"test2\";" //
				+ "		ret.attr[\"{ns1}local\\\\.Name3\"]=\"test3\";" //
				+ "		ret.attr[\"{ns2}pref:local.Name4\"]=\"test4\";");

		assertEquals(4, ret.size());
		assertEquals(3, ret.getAttribute("list").size());
		assertEquals("val0", ret.getAttribute("list").getValue(0));
		assertEquals("val1", ret.getAttribute("list").getValue(1));
		assertEquals("val2", ret.getAttribute("list").getValue(2));

		assertEquals("propTest1", ret.getAttribute("attr").getAttribute("local.Name1"));
		assertEquals("propTest2", ret.getAttribute("attr").getAttribute("pref:local.Name2"));
		assertEquals("propTest3", ret.getAttribute("attr").getAttributeNS("ns1", "local.Name3"));
		assertEquals("propTest4", ret.getAttribute("attr").getAttributeNS("ns2", "local.Name4"));

		assertEquals("test1", ret.getObject("attr.local\\.Name1"));
		assertEquals("test2", ret.getObject("attr.pref:local.Name2"));
		assertEquals("test3", ret.getObject("attr.local\\.Name3"));
		assertEquals("ns1", ret.getAttribute("attr.local\\.Name3").getNamespaceURI());

		// check that the values are replaced...

		se.interpret("ret.attr[\"@local.Name1\"]=\".propTest1\";" + "ret.attr[\"@pref:local.Name2\"]=\".propTest2\";"
				+ "ret.attr[\"@{ns1}local.Name3\"]=\".propTest3\";" + "ret.attr[\"@{ns2}pref:local.Name4\"]=\".propTest4\";"

				+ "ret.attr[\"local\\\\.Name1\"]=\".test1\";" + "ret.attr[\"pref:local.Name2\"]=\".test2\";"
				+ "ret.attr[\"{ns1}local\\\\.Name3\"]=\".test3\";" + "ret.attr[\"{ns2}pref:local.Name4\"]=\".test4\";");

		assertEquals(4, ret.size());
		assertEquals(".propTest1", ret.getAttribute("attr").getAttribute("local.Name1"));
		assertEquals(".propTest2", ret.getAttribute("attr").getAttribute("pref:local.Name2"));
		assertEquals(".propTest3", ret.getAttribute("attr").getAttributeNS("ns1", "local.Name3"));
		assertEquals(".propTest4", ret.getAttribute("attr").getAttributeNS("ns2", "local.Name4"));

		assertEquals(".test1", ret.getObject("attr.local\\.Name1"));
		assertEquals(".test2", ret.getObject("attr.pref:local.Name2"));
		assertEquals(".test3", ret.getObject("attr.local\\.Name3"));
		assertEquals("ns1", ret.getAttribute("attr.local\\.Name3").getNamespaceURI());
	}

	@Test
	public void test_Get_Property_Overriding_Method_Name() throws Exception {
		Entry work = new Entry(true);

		work.setProperty("prop", "val");
		work.newAttribute("getProperty");

		se.declareBean("work", work);

		se.interpret("ret.setAttribute(\"testProp\", work.getProperty(\"prop\"));");

		assertEquals(1, ret.size());
		assertEquals("val", ret.getAttribute("testProp").getValue());
	}

	@Test
	public void test_Assignment_Of_Attribute_Must_Not_Use_toString_Method_But_getValue_One() throws Exception {
		Entry work = new Entry(true);

		work.setAttribute("attr", "attrValue");

		se.declareBean("work", work);

		se.interpret("ret[\"testAssign\"] = work[\"attr\"];");

		assertEquals(1, ret.size());
		assertEquals("attrValue", ret.getAttribute("testAssign").getValue());
	}
}
