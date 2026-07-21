package com.ibm.di.entry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.test.utils.OverriderClassLoader;

public class Entry611Test {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static URLClassLoader urlLoader = null;

	@BeforeClass
	public static void setupEnv() {
		try {
			urlLoader = new OverriderClassLoader(new URL[] { new File("lib/classes/611").toURI().toURL() }, Entry611Test.class
					.getClassLoader(), new String[] { "com.ibm.di.entry.Entry611Test", "com.ibm.di.entry.Entry" });
		} catch (MalformedURLException e) {
		}
	}

	@Test
	public void test_Entry() {
		Entry e = new Entry();
		assertNotNull(e);
		assertEquals("Initial Size", 0, e.size());
		assertEquals(0, e.getAttributeNames().length);
		assertEquals(0, e.getAttributeCollection().size());
		assertEquals("Initial Props Size", e.getPropertyNames().length, 0);
		assertEquals("Initial Oper", e.getOp(), Entry.OP_GEN);
		assertEquals("Initial Operation", e.getOperation(), Entry.OP_GEN2);
	}

	@Test
	public void test_Clone_Entry() {
		Entry src = new Entry();

		src.setAttribute("Level1", "val1");
		src.setAttribute("Level1.Level2", "val2");
		src.setAttribute("Level1.Level2.Level3", "val3");

		src.setProperty("Level1", "PropVal1");
		src.setProperty("Level1.Level2", "PropVal2");
		src.setProperty("Level1.Level2.Level3", "PropVal3");

		src.setOp(Entry.OP_MOD);

		Entry dst = src.clone(src);

		assertEquals("val1", dst.getAttribute("Level1").getValue());
		assertEquals("val2", dst.getAttribute("Level1.Level2").getValue());
		assertEquals("val3", dst.getAttribute("Level1.Level2.level3").getValue());

		assertEquals("PropVal1", dst.getProperty("Level1"));
		assertEquals("PropVal2", dst.getProperty("Level1.Level2"));
		assertEquals("PropVal3", dst.getProperty("Level1.Level2.level3"));

		assertEquals(Entry.OP_MOD, dst.getOp());

		String[] expected = { "Level1", "Level1.Level2", "Level1.Level2.Level3" };
		assertEqualValues(expected, dst.getAttributeNames());
		assertEqualValues(expected, dst.getAttributeCollection());
		assertEqualValues(expected, dst.getPropertyNames());
	}

	@Test
	public void test_Get_And_Set_Operation_Field() {
		Entry e = new Entry();

		e.setOp(Entry.OP_ADD);
		assertEquals(Entry.OP_ADD, e.getOp());

		e.setOp(Entry.OP_DEL);
		assertEquals(Entry.OP_DEL, e.getOp());

		e.setOp(Entry.OP_MOD);
		assertEquals(Entry.OP_MOD, e.getOp());

		e.setOp(Entry.OP_GEN);
		assertEquals(Entry.OP_GEN, e.getOp());
	}

	@Test
	public void test_Get_Entry_Operation() {
		Entry e = new Entry();

		e.setOp(Entry.OP_ADD);
		assertEquals(Entry.OP_ADD2, e.getOperation());

		e.setOp(Entry.OP_DEL);
		assertEquals(Entry.OP_DEL2, e.getOperation());

		e.setOp(Entry.OP_MOD);
		assertEquals(Entry.OP_MOD2, e.getOperation());

		e.setOp(Entry.OP_GEN);
		assertEquals(Entry.OP_GEN2, e.getOperation());
	}

	@Test
	public void test_Set_Entry_Operation() {
		Entry e = new Entry();

		e.setOperation(Entry.OP_ADD2);
		assertEquals(Entry.OP_ADD, e.getOp());

		e.setOperation(Entry.OP_DEL2);
		assertEquals(Entry.OP_DEL, e.getOp());

		e.setOperation(Entry.OP_MOD2);
		assertEquals(Entry.OP_MOD, e.getOp());

		e.setOperation(Entry.OP_GEN2);
		assertEquals(Entry.OP_GEN, e.getOp());

		e.setOperation("");
		assertEquals(Entry.OP_GEN, e.getOp());

		e.setOperation(null);
		assertEquals(Entry.OP_GEN, e.getOp());

		e.setOperation("testOp");
		assertEquals('t', e.getOp());
	}

	@Test
	public void test_Set_AttributeInterface_1() {
		Entry e = new Entry();

		e.setAttribute(new Attribute("test.name"));

		// check whether a new attribute is added.
		assertNotNull(e.getAttribute("test.name"));
		assertEquals(1, e.size());
		assertEquals("test.name", e.getAttributeNames()[0]);
	}

	@Test
	public void test_Set_AttributeInterface_2() {
		Entry e = new Entry();

		e.setAttribute(new Attribute("test.name"));

		// check if the old attribute is replaced.
		Attribute testAttr = e.getAttribute("test.name");

		e.setAttribute(new Attribute("test.name"));

		assertNotSame(testAttr, e.getAttribute("test.name"));
	}

	@Test
	public void test_Set_Attribute_Object_1() {
		Entry e = new Entry();

		e.setAttribute("my.test.name", "my.test.val");

		// check removing of attributes.
		e.setAttribute("my.test.name", null);

		assertNull(e.getAttribute("my.test.name"));
	}

	@Test
	public void test_Set_Attribute_Object_2() {
		Entry e = new Entry();

		// test the case when name and value are both Attributes
		e.setAttribute(new Attribute("my.test.name"), new Attribute("my.test.val"));

		assertNotNull(e.getAttribute("my.test.name"));
	}

	@Test
	public void test_Set_Attribute_Object_3() {
		Entry e = new Entry();

		e.setAttribute("MY.test.name", "my.test.val1");

		// check whether setAttribute is case insensitive...
		e.setAttribute("my.Test.name", "my.test.val2");

		assertNotNull(e.getAttribute("my.Test.name"));
		assertEquals("my.test.val2", e.getAttribute("my.Test.name").getValue());
		assertEquals("my.Test.name", e.getAttributeNames()[0]);
	}

	@Test
	public void test_Set_Attribute_Object_As_Protected_Or_Not() {
		Entry e = new Entry();

		e.setAttribute("MY.test.name", "my.test.val1", true);
		assertEquals(true, e.getAttribute("my.test.name").getProtected());

		e.setAttribute("MY.test.name", "my.test.val1", false);
		assertEquals(false, e.getAttribute("my.test.name").getProtected());
	}

	@Test
	public void test_Find_Or_Create_New_Attribute_1() {
		Entry e = new Entry();

		Attribute newAttr = e.newAttribute("Attr.Test.Name");

		assertEquals(0, newAttr.size());
		assertEquals(newAttr, e.newAttribute("attr.test.name"));
	}

	@Test
	public void test_Find_Or_Create_New_Attribute_2() {
		Entry e = new Entry();

		Attribute newAttr = e.newAttribute("Attr.Test.Name", Attribute.ATTRIBUTE_ADD);

		assertEquals(0, newAttr.size());
		assertEquals(Attribute.ATTRIBUTE_ADD, newAttr.getOper());
	}

	@Test
	public void test_Add_Attribute_Value_1() {
		Entry e = new Entry();

		// set new attribute
		e.addAttributeValue("my.attr.name", "val1");
		assertEquals(1, e.getAttribute("my.attr.name").size());
		assertEquals("val1", e.getAttribute("my.attr.name").getValue(0));

		// add a value to an existing attribute
		e.addAttributeValue("my.attr.name", "val2");
		assertEquals(2, e.getAttribute("my.attr.name").size());
		assertEquals("val1", e.getAttribute("my.attr.name").getValue(0));
		assertEquals("val2", e.getAttribute("my.attr.name").getValue(1));
	}

	@Test
	public void test_Add_Attribute_Value_2() {
		Entry e = new Entry();

		// set new attribute
		e.addAttributeValue("my.attr.name", "val1", AttributeValue.AV_ADD);
		assertEquals(1, e.getAttribute("my.attr.name").size());
		assertEquals("val1", e.getAttribute("my.attr.name").getValue(0));
		assertEquals(AttributeValue.AV_ADD, e.getAttribute("my.attr.name").getValueOper(0));

		// add a value to an existing attribute
		e.addAttributeValue("my.attr.name", "val2", AttributeValue.AV_DELETE);
		assertEquals(2, e.getAttribute("my.attr.name").size());
		assertEquals("val1", e.getAttribute("my.attr.name").getValue(0));
		assertEquals(AttributeValue.AV_ADD, e.getAttribute("my.attr.name").getValueOper(0));
		assertEquals("val2", e.getAttribute("my.attr.name").getValue(1));
		assertEquals(AttributeValue.AV_DELETE, e.getAttribute("my.attr.name").getValueOper(1));
	}

	@Test
	public void test_Merge_Attribute_Value_1() {
		Entry e = new Entry();

		e.mergeAttributeValue("my.Attr.name", new Attribute("zzz", "testValue1"));

		assertNotNull(e.getAttribute("my.Attr.name"));
		assertEquals("testValue1", e.getAttribute("my.Attr.name").getValue());

		e.mergeAttributeValue("my.Attr.name", new Attribute("attr", "testValue2"));

		assertNotNull(e.getAttribute("my.Attr.name"));
		assertEquals("testValue1", e.getAttribute("my.Attr.name").getValue(0));
		assertEquals("testValue2", e.getAttribute("my.Attr.name").getValue(1));
	}

	@Test
	public void test_Merge_Attribute_Value_2() {
		Entry e = new Entry();

		e.mergeAttributeValue(null, new Attribute("zzz"));

		assertEquals(0, e.size());
	}

	@Test
	public void test_Get_Object_1() {
		Entry e = new Entry();

		e.addAttributeValue("new.attr.name", "val1");
		e.addAttributeValue("new.attr.name", "val2");

		assertEquals("val1", e.getObject("new.Attr.name"));
		// make sure it always returns the same
		assertEquals("val1", e.getObject("new.Attr.name"));

		assertEquals("val1", e.getString("new.Attr.name"));
		// make sure it always returns the same
		assertEquals("val1", e.getString("new.Attr.name"));
	}

	@Test
	public void test_Get_Object_2() {
		Entry e = new Entry();

		e.newAttribute("new.attr.name");

		assertNull(e.getObject("new.attr.name"));
		assertNull(e.getString("new.attr.name"));

		assertNull(e.getObject("not.existing"));
		assertNull(e.getString("not.existing"));
	}

	@Test
	public void test_Get_Attribute_Object() {
		Entry e = new Entry();

		e.setAttribute("my.Attr.name", "val");

		assertEquals("val", e.getAttribute("my.Attr.name").getValue());
		assertEquals("val", e.getAttribute("my.attr.name").getValue());
		assertEquals("val", e.getAttribute("MY.ATTR.NAME").getValue());
		assertNull(e.getAttribute("NOT.existing"));
	}

	@Test
	public void test_Get_Attribute_Names_Collection() {
		Entry e = new Entry();

		e.setAttribute("Level1", "val1");
		e.newAttribute("Level1.Level2");
		e.setAttribute("Level1.Level2.Level3", "val3");

		assertEqualValues(new String[] { "Level1", "Level1.Level2", "Level1.Level2.Level3" }, e.getAttributeNames());
		assertEqualValues(new String[] { "Level1", "Level1.Level2", "Level1.Level2.Level3" }, e.getAttributeCollection());
	}

	@Test
	public void test_Remove_Attribute_Object() {
		Entry e = new Entry();

		e.setAttribute("my.attr.name", "val");
		e.setAttribute("my.attr2.name", "val");

		e.removeAttribute(new Attribute("my.attr.name"));
		assertNull(e.getAttribute("my.attr.name"));

		e.removeAttribute("my.ATTR2.name");
		assertNull(e.getAttribute("my.attr2.name"));
	}

	@Test
	public void test_Remove_All_Attributes() {
		Entry e = new Entry();

		e.setAttribute("my.attr.name", "val");
		e.setAttribute("my.attr2.name", "val");

		e.removeAllAttributes();

		assertNull(e.getAttribute("my.attr.name"));
		assertNull(e.getAttribute("my.attr2.name"));
		assertEquals(0, e.size());
	}

	@Test
	public void test_Get_Entry_Size() {
		Entry e = new Entry();

		e.setAttribute("my.attr.name", "val");
		e.setAttribute("my.attr2.name", "val");

		assertEquals(2, e.size());

		e.removeAttribute("my.attr.name");

		assertEquals(1, e.size());

		e.removeAttribute("my.attr2.name");

		assertEquals(0, e.size());
	}

	@Test
	public void test_Get_Entry_Property() {
		Entry e = new Entry();

		assertNull(e.getProperty(null));

		assertNull(e.getProperty("not.existing"));

		e.setProperty("My.Property.Name", "val");

		assertEquals("val", e.getProperty("my.property.name"));
		assertEquals("val", e.getProperty("My.Property.Name"));
	}

	@Test
	public void test_Set_Entry_Property() {
		Entry e = new Entry();

		e.setProperty("my.prop.name", "val");
		assertEquals(1, e.getPropertyNames().length);
		assertEquals("val", e.getProperty("my.prop.name"));

		e.setProperty("My.Prop.Name", null);
		assertNull(e.getProperty("my.prop.name"));
		assertEquals(0, e.getPropertyNames().length);
	}

	@Test
	public void test_Entry_Having_A_Property() {
		Entry e = new Entry();

		assertFalse(e.hasProperty("noSuch"));
		assertFalse(e.hasProperty(null));

		e.setProperty("my.prop.name", "val");

		assertTrue(e.hasProperty("my.prop.name"));
	}

	@Test
	public void test_Get_Property_Names() {
		Entry e = new Entry();

		e.setProperty("my.prop.1", "val1");
		e.setProperty("my.prop.2", "val2");
		e.setProperty("my.prop.3", "val3");

		assertEqualValues(new String[] { "my.prop.1", "my.prop.2", "my.prop.3" }, e.getPropertyNames());
	}

	@Test
	public void test_Merging_Entries_By_Keeping_Local_Attributes_In_Case_Of_Duplicate_Names() {
		Entry src = new Entry();

		src.setAttribute("my.attr.name", "val11");
		src.addAttributeValue("my.attr.name", "val12");
		src.setAttribute("my.attr2.name", "val21");

		src.setProperty("propName", "propVal");

		Entry dst = new Entry();

		dst.setAttribute("my.attr.name", "val11");
		dst.addAttributeValue("my.attr.name", "val13");

		dst.merge(src, true);

		assertEquals(3, dst.getAttribute("my.attr.name").size());
		assertEquals(2, dst.size());
		assertEqualValues(new String[] { "my.attr.name", "my.attr2.name" }, dst.getAttributeNames());
		assertEquals("val11", dst.getAttribute("my.attr.name").getValue(0));
		assertEquals("val13", dst.getAttribute("my.attr.name").getValue(1));
		assertEquals("val12", dst.getAttribute("my.attr.name").getValue(2));
		assertEquals("val21", dst.getAttribute("my.attr2.name").getValue());
		assertEquals("propVal", dst.getProperty("propName"));
	}

	@Test
	public void test_Merging_Entries_By_Overwriting_Local_Attributes_In_Case_Of_Duplicate_Names() {
		Entry src = new Entry();

		src.setAttribute("my.attr.name", "val11");
		src.addAttributeValue("my.attr.name", "val12");
		src.setAttribute("my.attr2.name", "val21");

		src.setProperty("propName", "propVal");

		Entry dst = new Entry();

		dst.setAttribute("my.attr.name", "val11");
		dst.addAttributeValue("my.attr.name", "val13");

		dst.merge(src, false);

		assertEquals(2, dst.getAttribute("my.attr.name").size());
		assertEquals(2, dst.size());
		assertEqualValues(new String[] { "my.attr.name", "my.attr2.name" }, dst.getAttributeNames());
		assertEquals("val11", dst.getAttribute("my.attr.name").getValue(0));
		assertEquals("val12", dst.getAttribute("my.attr.name").getValue(1));
		assertEquals("val21", dst.getAttribute("my.attr2.name").getValue());
		assertEquals("propVal", dst.getProperty("propName"));
	}

	public <T> void assertEqualValues(T[] expected, Collection<T> actual) {
		assertEquals("Size of array: ", expected.length, actual.size());

		int matches = 0;
		for (T val : actual) {
			for (T control : expected) {
				if (control.equals(val)) {
					++matches;
					break;
				}
			}
		}

		assertEquals("Found matches: ", expected.length, matches);
	}

	public <T> void assertEqualValues(T[] expected, T[] actual) {
		assertEquals("Size of array: ", expected.length, actual.length);

		int matches = 0;
		for (T val : actual) {
			for (T control : expected) {
				if (control.equals(val)) {
					++matches;
					break;
				}
			}
		}

		assertEquals("Found matches: ", expected.length, matches);
	}

	@Test
	public void test_Deserialize_70_Flat_Entry() throws IOException, InterruptedException, SecurityException,
			IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		// create the post-611 entry
		// +-->a.b.c = "c"
		// +---+-> "c2"
		// +-->z = "z"
		// +---+-> "z2"/ADD
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		Entry e = new Entry();
		e.setAttribute("a.b.c", "c");
		e.addAttributeValue("a.b.c", "c2");
		e.setAttribute("z", "z");
		e.addAttributeValue("z", "z2", AttributeValue.AV_ADD);
		e.setAttribute("p:a.b", "b");
		e.setAttribute("a.b.c.d", "d");
		e.setProperty("testProp", "testVal");

		// serialize the 70 entry
		oos.writeObject(e);
		oos.close();

		deserialize70EntryUsing611EntryClass(bos.toByteArray());
	}

	@Test
	public void test_Deserialize_70_Clone_Entry() throws IOException, InterruptedException, SecurityException,
			IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {

		// there was a defect when cloned entry is serialized it uses an
		// ArrayList instead of Vector.
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		// create the post-611 entry
		// +-->a.b.c = "c"
		// +---+-> "c2"
		// +-->z = "z"
		// +---+-> "z2"/ADD
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		Entry e = new Entry();
		e.setAttribute("a.b.c", "c");
		e.addAttributeValue("a.b.c", "c2");
		e.setAttribute("z", "z");
		e.addAttributeValue("z", "z2", AttributeValue.AV_ADD);
		e.setAttribute("p:a.b", "b");
		e.setAttribute("a.b.c.d", "d");
		e.setProperty("testProp", "testVal");

		e = e.clone();

		// serialize the 70 entry
		oos.writeObject(e);
		oos.close();

		deserialize70EntryUsing611EntryClass(bos.toByteArray());
	}

	/**
	 * @param string
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private void deserialize70EntryUsing611EntryClass(byte[] bytes) throws ClassNotFoundException, IllegalAccessException,
			InstantiationException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(urlLoader);

		try {
			// we need the Entry611Test to be reloaded and resolved using the
			// Entry class from TDI 6.1.1. This way the deserialization will use
			// the old Entry class.
			Class<?> entry611TestClass = urlLoader.loadClass("com.ibm.di.entry.Entry611Test");
			Object entry611TestInst = entry611TestClass.newInstance();
			Method method = entry611TestClass.getDeclaredMethod("test_Deserialize_70_Entry", new Class[] { byte[].class });
			method.setAccessible(true);
			method.invoke(entry611TestInst, new Object[] { bytes });
		} finally {
			Thread.currentThread().setContextClassLoader(ctxLoader);
		}
	}

	private static int test_Deserialize_70_Entry(byte[] bytes) {

		int result = 0;

		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			Entry e = (Entry) ois.readObject();
			ois.close();

			List<String> attrNames = new ArrayList<String>(4);
			attrNames.add("a.b.c");
			attrNames.add("z");
			attrNames.add("p:a.b");
			attrNames.add("a.b.c.d");

			Collection<String> set = e.getAttributeCollection();
			if (attrNames.size() != set.size()) {
				System.err.println("Entry.size (" + set.size() + " ) does not match expected size (" + attrNames.size() + ")");
				result = 1;
			}
			if (!attrNames.containsAll(set)) {
				System.err.println("The Entry names (" + set + ") are not the same as expected (" + attrNames + ")");
				result = 1;
			}
			if (e.getAttribute((Object) "p:A.B") == null) {
				System.err.println("Deserialized entry is not lowercase aware: p:A.B");
				result = 1;
			}
			if (e.getPropertyNames().length == 0) {
				System.err.println("The Entry property names count should be greater than 0.");
				result = 1;
			}
			if (!"testVal".equals(e.getProperty((Object) "testProp"))) {
				System.err.println("Missing expected property: testProp");
				result = 1;
			}
			if (!"testVal".equals(e.getProperty((Object) "tesTprop"))) {
				System.err.println("Deserialized entry is not lowercase aware: testProp");
				result = 1;
			}

			if (!"c".equals(e.getAttribute((Object) "a.b.c").getValue(0))) {
				System.err.println("Deserialized entry has no first value for attribute a.b.c");
				result = 1;
			}
			if (!"c2".equals(e.getAttribute((Object) "a.b.c").getValue(1))) {
				System.err.println("Deserialized entry has no second value for attribute a.b.c");
				result = 1;
			}
			if (AttributeValue.AV_ADD != e.getAttribute((Object) "z").getValueOper(1)) {
				System.err.println("Deserialized entry has no AttributeValue tag for attribute z");
				result = 1;
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			result = 1;
		} catch (IOException e1) {
			e1.printStackTrace();
			result = 1;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			result = 1;
		} catch (Throwable t) {
			t.printStackTrace();
			result = 1;
		}

		return result;
	}

	@Test
	public void test_Deserialize_70_Hierarchical_Entry() throws IOException, InterruptedException, SecurityException,
			IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException,
			NoSuchMethodException, InvocationTargetException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);

		// create the post-611 entry
		// +-->a.b.c = "c"
		// +---+-> "c2"
		// +-->z = "z"
		// +---+-> "z2"/ADD
		// +-->p:a.b = "b"
		// +-->a.b.c.d = "d"
		Entry e = new Entry();
		e.setAttribute("a.b.c", "c");
		e.addAttributeValue("a.b.c", "c2");
		e.setAttribute("z", "z");
		e.addAttributeValue("z", "z2", AttributeValue.AV_ADD);
		e.setAttribute("p:a.b", "b");
		e.setAttribute("a.b.c.d", "d");
		e.setProperty("testProp", "testVal");

		// make sure the serialized entry is hierarchical...
		e.enableDOM();

		// serialize the 70 entry
		oos.writeObject(e);
		oos.close();

		deserialize70EntryUsing611EntryClass(bos.toByteArray());
	}
}