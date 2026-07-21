package com.ibm.di.entry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import com.ibm.di.test.framework.perf.RepeatConstants;
import com.ibm.di.test.utils.TestUtils;

/**
 * @author kaloyan.kolev
 * 
 */
public class EntryExtPerf {

	private static byte[] serializedFlatEntry = null;
	private static byte[] serializedHierarchicalEntry = null;
	
	private static Entry hEntry = null;
	private static Entry fEntry = null;
	
	byte[] serializedFlat611Entry = new byte[] { -84, -19, 0, 5, 115, 114, 0, 22, 99, 111, 109, 46, 105, 98, 109, 46, 100, 105, 46, 101, 110,
			116, 114, 121, 46, 69, 110, 116, 114, 121, -83, 68, -61, -7, -69, 22, 55, 63, 2, 0, 5, 67, 0, 9, 111, 112, 101,
			114, 97, 116, 105, 111, 110, 76, 0, 4, 100, 97, 116, 97, 116, 0, 21, 76, 106, 97, 118, 97, 47, 117, 116, 105, 108,
			47, 72, 97, 115, 104, 116, 97, 98, 108, 101, 59, 76, 0, 12, 108, 111, 119, 101, 114, 67, 97, 115, 101, 77, 97, 112,
			113, 0, 126, 0, 1, 76, 0, 14, 108, 111, 119, 101, 114, 99, 97, 115, 101, 80, 114, 111, 112, 115, 113, 0, 126, 0, 1,
			76, 0, 10, 112, 114, 111, 112, 101, 114, 116, 105, 101, 115, 113, 0, 126, 0, 1, 120, 112, 0, 103, 115, 114, 0, 19,
			106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 72, 97, 115, 104, 116, 97, 98, 108, 101, 19, -69, 15, 37, 33, 74,
			-28, -72, 3, 0, 2, 70, 0, 10, 108, 111, 97, 100, 70, 97, 99, 116, 111, 114, 73, 0, 9, 116, 104, 114, 101, 115, 104,
			111, 108, 100, 120, 112, 63, 64, 0, 0, 0, 0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 4, 116, 0, 5, 97, 46, 98, 46, 99,
			115, 114, 0, 26, 99, 111, 109, 46, 105, 98, 109, 46, 100, 105, 46, 101, 110, 116, 114, 121, 46, 65, 116, 116, 114,
			105, 98, 117, 116, 101, 92, -91, 127, 19, -97, 99, -21, -23, 2, 0, 4, 67, 0, 9, 111, 112, 101, 114, 97, 116, 105,
			111, 110, 90, 0, 7, 112, 114, 111, 116, 101, 99, 116, 76, 0, 4, 110, 97, 109, 101, 116, 0, 18, 76, 106, 97, 118,
			97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 76, 0, 6, 118, 97, 108, 117, 101, 115, 116, 0, 18,
			76, 106, 97, 118, 97, 47, 117, 116, 105, 108, 47, 86, 101, 99, 116, 111, 114, 59, 120, 112, 0, 114, 0, 113, 0, 126,
			0, 5, 115, 114, 0, 16, 106, 97, 118, 97, 46, 117, 116, 105, 108, 46, 86, 101, 99, 116, 111, 114, -39, -105, 125,
			91, -128, 59, -81, 1, 3, 0, 3, 73, 0, 17, 99, 97, 112, 97, 99, 105, 116, 121, 73, 110, 99, 114, 101, 109, 101, 110,
			116, 73, 0, 12, 101, 108, 101, 109, 101, 110, 116, 67, 111, 117, 110, 116, 91, 0, 11, 101, 108, 101, 109, 101, 110,
			116, 68, 97, 116, 97, 116, 0, 19, 91, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116,
			59, 120, 112, 0, 0, 0, 0, 0, 0, 0, 1, 117, 114, 0, 19, 91, 76, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 79, 98,
			106, 101, 99, 116, 59, -112, -50, 88, -97, 16, 115, 41, 108, 2, 0, 0, 120, 112, 0, 0, 0, 10, 116, 0, 1, 99, 112,
			112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 5, 112, 58, 97, 46, 98, 115, 113, 0, 126, 0, 6, 0, 114, 0,
			113, 0, 126, 0, 16, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0, 0, 0, 10, 116, 0,
			1, 98, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 7, 97, 46, 98, 46, 99, 46, 100, 115, 113, 0, 126,
			0, 6, 0, 114, 0, 113, 0, 126, 0, 21, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0,
			0, 0, 10, 116, 0, 1, 100, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 116, 0, 1, 122, 115, 113, 0, 126, 0, 6,
			0, 114, 0, 113, 0, 126, 0, 26, 115, 113, 0, 126, 0, 10, 0, 0, 0, 0, 0, 0, 0, 1, 117, 113, 0, 126, 0, 13, 0, 0, 0,
			10, 116, 0, 1, 122, 112, 112, 112, 112, 112, 112, 112, 112, 112, 120, 120, 115, 113, 0, 126, 0, 3, 63, 64, 0, 0, 0,
			0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 4, 113, 0, 126, 0, 5, 113, 0, 126, 0, 5, 113, 0, 126, 0, 16, 113, 0, 126, 0,
			16, 113, 0, 126, 0, 21, 113, 0, 126, 0, 21, 113, 0, 126, 0, 26, 113, 0, 126, 0, 26, 120, 115, 113, 0, 126, 0, 3,
			63, 64, 0, 0, 0, 0, 0, 8, 119, 8, 0, 0, 0, 11, 0, 0, 0, 0, 120, 115, 113, 0, 126, 0, 3, 63, 64, 0, 0, 0, 0, 0, 8,
			119, 8, 0, 0, 0, 11, 0, 0, 0, 0, 120 };

	@BeforeClass
	public static void initEntry() {
		hEntry = TestUtils.createHierarchicalEntry(7, "");
		fEntry = TestUtils.createFlatEntry(5, "");

		serializedFlatEntry = TestUtils.serializeObject(fEntry);
		serializedHierarchicalEntry = TestUtils.serializeObject(hEntry);
	}
	
	@Test
	public void test_Flat_getAttribute() { // 4,5 min
		Entry entry = new Entry();
		entry.setAttribute("attr1", "val1");
		entry.setAttribute("attr2", "val2");
		entry.setAttribute("attr3", "val3");
		entry.setAttribute("attr4", "val4");

		for (long i = 0; i < RepeatConstants.get500m(); ++i) {
			entry.getAttribute("att1");
			entry.getAttribute("attr2");
			entry.getAttribute("attr3");
			entry.getAttribute("attr4");
			entry.getAttribute("attr5");
		}
	}

	@Test
	public void test_Flat_getAttribute_dottedNames() { // 4,2 min
		Entry entry = new Entry();
		entry.setAttribute("attr.1", "val1");
		entry.setAttribute("attr.2", "val2");
		entry.setAttribute("attr.3", "val3");
		entry.setAttribute("attr.4", "val4");

		for (long i = 0; i < RepeatConstants.get750m(); ++i) {
			entry.getAttribute("attr.1");
			entry.getAttribute("attr.2");
			entry.getAttribute("attr.3");
			entry.getAttribute("attr.4");
			entry.getAttribute("attr.5");
		}
	}

	@Test
	public void test_Flat_setAttribute_Values_As_String() { // 4,9 min
		for (long i = 0; i < RepeatConstants.get250m(); ++i) {
			Entry entry = new Entry();
			entry.setAttribute("attr1", "val1");
			entry.setAttribute("attr2", "val2");
			entry.setAttribute("attr3", "val3");
			entry.setAttribute("attr4", "val4");
		}
	}
	
	@Test
	public void test_Flat_setAttribute_Values_As_List() { // 2,6 min
		List<String> vals = new ArrayList<String>(3);
		vals.add("val1");
		vals.add("val2");
		vals.add("val3");
		
		for (long i = 0; i < RepeatConstants.get250m(); ++i) {
			Entry entry = new Entry();
			entry.setAttributeValues("attr", vals);
		}
	}
	
	@Test
	public void test_Flat_setAttribute_Values_As_Attribute() { // 3 min
		Attribute temp = new Attribute();
		temp.addValue("val1");
		temp.addValue("val2");
		temp.addValue("val3");
		
		for (long i = 0; i < RepeatConstants.get250m(); ++i) {
			Entry entry = new Entry();
			entry.setAttribute("attr", temp);
		}
	}

	@Test
	public void test_Flat_addAttributeValue() throws Exception { // 4,5 min
		for (long i = 0; i < RepeatConstants.get50m(); ++i) {
			Entry e = TestUtils.createHierarchicalEntry(2, "");
			
			e.addAttributeValue("attr1", "val");
			e.addAttributeValue("attr2", "val", AttributeValue.AV_ADD);
			
			e.addAttributeValue("attr3", "val");
			e.addAttributeValue("attr4", "val", AttributeValue.AV_REPLACE);
			
			e.addAttributeValue("attr5", new Attribute());
			e.addAttributeValue("attr6", new Attribute(), AttributeValue.AV_ADD);
		}
	}
	
	
	@Test
	public void test_Flat_setAttribute_dottedNames() { // 3,7 min
		for (long i = 0; i < RepeatConstants.get150m(); ++i) {
			Entry entry = new Entry();
			entry.setAttribute("attr.1", "val1");
			entry.setAttribute("attr.2", "val2");
			entry.setAttribute("attr.3", "val3");
			entry.setAttribute("attr.4", "val4");
		}
	}

	@Test
	public void test_Flat_newAttribute_Values_As_String() { // 4 min
		for (long i = 0; i < RepeatConstants.get150m(); ++i) {
			Entry entry = new Entry();
			entry.newAttribute("attr1");
			entry.newAttribute("attr2");
			entry.newAttribute("attr3");
			
			entry.newAttribute("attr1");
			entry.newAttribute("attr3");
			entry.newAttribute("attr2");
		}
	}
	
	@Test
	public void test_Flat_removeAttribute() { // 3,9 min
		Entry entry = new Entry();
		for (long i = 0; i < RepeatConstants.get500m(); ++i) {
			entry.setAttribute("attr","val");
			entry.removeAttribute("attr");			
		}
	}
	
	
	@Test
	public void test_Flat_getAttributeNames() { // 3,5 min
		for (long i = 0; i < RepeatConstants.get1g(); ++i) {
			fEntry.getAttributeNames();
		}
	}
	
	@Test
	public void test_Hierachical_getAttributeNames() { // 4 min
		for (long i = 0; i < RepeatConstants.get1m(); ++i) {
			hEntry.getAttributeNames();
		}
	}
	
	@Test
	public void test_Hierachical_toString() { // 2,73 min
		for (long i = 0; i < RepeatConstants.get7m(); ++i) {
			hEntry.toString();
		}
	}
	
	@Test
	public void test_Hierachical_toDeltaString() { // 4 min
		for (long i = 0; i < RepeatConstants.get5m(); ++i) {
			hEntry.toDeltaString();
		}
	}

	@Test
	public void test_Flat_toString() { // 4 min
		for (long i = 0; i < RepeatConstants.get50m(); ++i) {
			fEntry.toString();
		}
	}
	
	@Test
	public void test_Flat_toDeltaString() { // 3,15 min
		for (long i = 0; i < RepeatConstants.get25m(); ++i) {
			fEntry.toDeltaString();
		}
	}
	
	@Test
	public void test_Flat_getAttributeNames_dottedNames() { // 3 min
		Entry entry = new Entry();
		entry.setAttribute("attr.1", "val1");
		entry.setAttribute("attr.2", "val2");
		entry.setAttribute("attr.3", "val3");
		entry.setAttribute("attr.4", "val4");

		for (long i = 0; i < RepeatConstants.get1g(); ++i) {
			entry.getAttributeNames();
		}
	}

	@Test
	public void test_Hierarchical_clone() { // 3,5 min
		for (long i = 0; i < RepeatConstants.get5m(); ++i) {
			Entry e = hEntry.clone();
		}
	}
	
	@Test
	public void test_Flat_clone() { // 3,8 min
		for (long i = 0; i < RepeatConstants.get25m(); ++i) {
			Entry e = fEntry.clone();
		}
	}
	
	@Test
	public void test_Flat_enableDOM() { // 3,7 min
		for (long i = 0; i < RepeatConstants.get100m(); ++i) {
			Entry entry = new Entry();
			entry.setAttribute("attr1", "val1");
			entry.setAttribute("attr.2", "val2");
			entry.enableDOM();
		}
	}
	
	@Test
	public void test_Hierarchical_appendChild() throws Exception { // 4,2 min
		for (long i = 0; i < RepeatConstants.get15m(); ++i) {
			Entry e = new Entry();
			Attribute attr1 = e.appendChild(new Attribute("attr1"));
			Attribute attr2 = e.appendChild(new Attribute("attr2"));
			Attribute attr3 = e.appendChild(new Attribute("attr3"));

			Node subattr11 = attr1.appendChild(new Attribute("subattr11"));
			Node subattr12 = attr1.appendChild(new Attribute("subattr12"));
			Node subattr13 = attr1.appendChild(new Attribute("subattr13"));

			Node ssattr = subattr11.appendChild(new Attribute("ssattr"));
			subattr11.appendChild(new Attribute("ssattr"));
			subattr11.appendChild(new Attribute("ssattr"));
			subattr12.appendChild(new Attribute("ssattr121"));
			subattr12.appendChild(new Attribute("ssattr122"));
			subattr12.appendChild(new Attribute("ssattr123"));
			subattr13.appendChild(new Attribute("ssattr131"));
			subattr13.appendChild(new Attribute("ssattr132"));
			subattr13.appendChild(new Attribute("ssattr133"));
			ssattr.appendChild(new Attribute("sssattr1"));
			ssattr.appendChild(new Attribute("sssattr2"));
			ssattr.appendChild(new Attribute("sssattr3"));

			attr2.appendChild(new Attribute("subattr21"));
			attr2.appendChild(new Attribute("subattr22"));
			attr2.appendChild(new Attribute("subattr23"));
			attr3.appendChild(new Attribute("subattr31"));
			attr3.appendChild(new Attribute("subattr32"));
			attr3.appendChild(new Attribute("subattr33"));
		}
	}
	
	@Test
	public void test_Flat_removeChild() { // 3,8 min
		Entry entry = new Entry();
		for (long i = 0; i < RepeatConstants.get300m(); ++i) {
			Node n = entry.appendChild(new Attribute("attr"));
			entry.removeChild(n);			
		}
	}
	
	@Test
	public void test_Hierarchical_createElement() throws Exception { // 3 min
		Entry e = new Entry();
		
		for (long i = 0; i < RepeatConstants.get300m(); ++i){
			e.createElement("attr1");
			e.createElement("attr2");
			e.createElement("attr3");
			e.createElement("attr4");
		}
	}
	
	@Test
	public void test_Hierarchical_createElementNS() throws Exception { // 3 min
		Entry e = new Entry();
		
		for (long i = 0; i < RepeatConstants.get250m(); ++i){
			e.createElementNS("ns1", "attr1");
			e.createElementNS("ns1", "attr2");
			e.createElementNS("ns2", "attr1");
			e.createElementNS("ns2", "attr2");
		}
	}
	
	@Test
	public void test_Hierarchical_merge() throws Exception { // 4,2 min
		for (long i = 0; i < RepeatConstants.get5m(); ++i){
			Entry e1 = TestUtils.createHierarchicalEntry(4, "_");
			Entry e2 = TestUtils.createHierarchicalEntry(4, "*");
			e1.merge(e2);
			e2.merge(e1);
		}
	}
	
	@Test
	public void test_Flat_merge() throws Exception { // 3,35 min
		for (long i = 0; i < RepeatConstants.get15m(); ++i){
			Entry e1 = TestUtils.createFlatEntry(4, "_");
			Entry e2 = TestUtils.createFlatEntry(4, "*");
			e1.merge(e2);
			e2.merge(e1);
		}
	}
	
	@Test
	public void test_Flat_writeObject() throws IOException { // 3,33 min
		for (long i = 0; i < RepeatConstants.get10m(); ++i) {
			ObjectOutputStream objOut = new ObjectOutputStream(new NOOPOutputStream());
			objOut.writeObject(fEntry);
			objOut.flush();
			objOut.close();
		}
	}
	
	@Test
	public void test_Flat_readObject() throws IOException, ClassNotFoundException {
		// 2,7 min
		for (long i = 0; i < RepeatConstants.get2m(); ++i) {
			ByteArrayInputStream bis = new ByteArrayInputStream(serializedFlatEntry);
			bis.reset();
			ObjectInputStream objInp = new ObjectInputStream(bis);
			Entry entry = (Entry) objInp.readObject();
			objInp.close();
		}
	}
	
	@Test
	public void test_Hierarchical_readObject() throws IOException, ClassNotFoundException {
		// 3,6 min
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedHierarchicalEntry);
		for (long i = 0; i < RepeatConstants.get500k(); ++i) {
			ObjectInputStream objInp = new ObjectInputStream(bis);
			Entry entry = (Entry) objInp.readObject();
			bis.reset();
			objInp.close();
		}
		bis.close();
	}
	
	@Test
	public void test_Hierarchical_writeObject() throws IOException {
		// 3,2 min
		for (long i = 0; i < RepeatConstants.get1m(); ++i) {
			ObjectOutputStream objOut = new ObjectOutputStream(new NOOPOutputStream());
			objOut.writeObject(hEntry);
			objOut.flush();
			objOut.close();
		}
	}
	
	@Test
	public void test_Flat_611_readObject() throws Exception { // 2,7 min
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedFlat611Entry);
		for (long i = 0; i < RepeatConstants.get2m(); ++i) {
			ObjectInputStream objInp = new ObjectInputStream(bis);
			Entry e = (Entry) objInp.readObject();
			bis.reset();
			objInp.close();
		}
		bis.close();
	}

	@Test
	public void test_Hirarchical_renameNode() throws Exception { // 3,4 min
		Entry e = new Entry();
		Attribute a = e.appendChild(e.createElement("attr1"));
		
		for (long i = 0; i < RepeatConstants.get300m(); ++i) {
			if(e.getAttribute("attr1")!= null)
				e.renameNode(a, "ns1", "attr2");
			else
				e.renameNode(a, "ns2", "attr1");
		}
		
	}
	
	@Test
	public void test_Flat_setProperty() throws Exception { // 2,7 min
		for(long i = 0; i<RepeatConstants.get100m(); i++) {
			Entry e = new Entry();
			e.setProperty("prop1", "property1");
			e.setProperty("prop2", "property2");
			e.setProperty("prop3", "property3");
			e.setProperty("prop4", "property4");
		}
	}
	
	@Test
	public void test_Flat_getProperty() throws Exception { // 3,8 min
		for(long i = 0; i<RepeatConstants.get2g(); i++) {
			fEntry.getProperty("prop1");
		}
	}
	
	public static class NOOPOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
		}
	}
}
