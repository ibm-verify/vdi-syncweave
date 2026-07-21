package com.ibm.di.parser;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.test.utils.TestUtils;

public class LDIFParser700Test {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static LDIFParser parser = null;
	
	@Test
	public void test_Writing_Entry_With_Add_operation() throws IOException {
		StringWriter sw = new StringWriter();
		parser.setOutputStream(sw);
		
		Entry e = new Entry();
		
		Attribute dn = e.newAttribute("$dn");
		dn.setValue("cn=Angelina Smith,ou=Development Team,o=IBM,c=US");

		Attribute cn = e.newAttribute("cn");
		cn.addValue("Angelina Smith");
		cn.addValue("AngelinaS");

		Attribute sn = e.newAttribute("sn");
		sn.addValue("Smith");
		
		Attribute phone = e.newAttribute("telephoneNumber");
		phone.setValue("69332");

		Attribute mail = e.newAttribute("mail");
		mail.setValue("asmith@us.IBM.com");

		Attribute objClass = e.newAttribute("objectClass");
		objClass.addValue("organizationalPerson");
		objClass.addValue("person");
		objClass.addValue("top");
		
		Attribute photo = e.newAttribute("photo");
		photo.setValue("\\\\$HOME_DIR/empdir/photog/angie.jpg");
		
		e.setOperation("add");
		
		parser.writeEntry(e);
		
		String result ="version: 1\n\n"
			+ "dn: cn=Angelina Smith,ou=Development Team,o=IBM,c=US\n"
			+ "changetype: add\n"
			+ "objectClass: organizationalPerson\n"
			+ "objectClass: person\n" + "objectClass: top\n"
			+ "mail: asmith@us.IBM.com\n"
			+ "photo: \\$HOME_DIR/empdir/photog/angie.jpg\n"
			+ "sn: Smith\n"
			+ "telephoneNumber: 69332\n"
			+ "cn: Angelina Smith\n"
			+ "cn: AngelinaS\n\n";
	
		String ourResult = sw.toString().replace("\\\\", "\\");
		assertEquals(result, TestUtils.removeReturnCharacters(ourResult));
	}
	
	@Test
	public void test_Writing_Entry_With_Delete_operation() throws IOException {
		StringWriter sw = new StringWriter();
		parser.setOutputStream(sw);
		
		Entry e = new Entry();
		Attribute dn = e.newAttribute("$dn");
		dn.setValue("cn=Suzzie Smith,ou=Development Team,o=IBM,c=US");
		e.setOperation("delete");
		parser.writeEntry(e);
		
		String result = "dn: cn=Suzzie Smith,ou=Development Team,o=IBM,c=US\n"
			+ "changetype: delete\n\n";
			
		String ourResult = sw.toString().replace("\\\\", "\\");
		assertEquals(result, TestUtils.removeReturnCharacters(ourResult));
	}
	
	@Test
	public void test_Writing_Entry_With_Modify_operation() throws IOException {
		StringWriter sw = new StringWriter();
		parser.setOutputStream(sw);
		
		Entry e=new Entry();
		Attribute dn = e.newAttribute("$dn");
		dn.addValue("cn=Barbara Willson,ou=Sales,o=IBM,c=US");

		Attribute phone = e.newAttribute("work-phone");
		phone.addValue("650/506-7000");
		phone.addValue("650/506-7001");
		phone.setOperation("modify");
		phone.setValueOperation(0, "add");
		phone.setValueOperation(1, "add");

		Attribute fax = e.newAttribute("home-fax");
		fax.setOperation("delete");
		
		phone=null;
		phone = e.newAttribute("home-phone");
		phone.setValue("415/697-8899");
		
		e.setOperation("modify");
		parser.writeEntry(e);
		
		String result = "dn: cn=Barbara Willson,ou=Sales,o=IBM,c=US\n"
			+ "changetype: modify\n"
			+ "replace: home-phone\n"
			+ "home-phone: 415/697-8899\n"+ "-\n"
			+ "delete: home-fax\n" + "-\n"
			+ "add: work-phone\n"
			+ "work-phone: 650/506-7000\n"
			+ "work-phone: 650/506-7001\n" + "-\n\n";
	
		String ourResult = sw.toString().replace("\\\\", "\\");
		assertEquals(result, TestUtils.removeReturnCharacters(ourResult));
	}
	
	@Test
	public void test_Writing_Entry_With_ModRDN_operation() throws IOException {
		StringWriter sw = new StringWriter();
		parser.setOutputStream(sw);
		
		Entry e=new Entry();
		Attribute dn = e.newAttribute("$dn");
		dn.setValue("cn=Sally Jones,ou=HR,o=IBM,c=US");

		Attribute newRDN = e.newAttribute("newrdn");
		newRDN.addValue("Sally Jones-Sampson");
		newRDN.setOperation("replace");
		
		Attribute oldRDN = e.newAttribute("deleteoldrdn");
		oldRDN.addValue("1");
		oldRDN.setOperation("replace");
		
		e.setOperation("modrdn");
		
		parser.writeEntry(e);
		
		String result ="dn: cn=Sally Jones,ou=HR,o=IBM,c=US\n"
			+ "changetype: modrdn\n"
			+ "newrdn: Sally Jones-Sampson\n"
			+ "deleteoldrdn: 1\n\n";
	
		String ourResult = sw.toString().replace("\\\\", "\\");
		assertEquals(result, TestUtils.removeReturnCharacters(ourResult));
	}
	
	@Test
	public void test_Writing_Entry_With_ModDN_operation() throws IOException {
		StringWriter sw = new StringWriter();
		parser.setOutputStream(sw);
		
		Entry e=new Entry();
		
		Attribute dn = e.newAttribute("$dn");
		dn.setValue("cn=Molly Weak,ou=HR,o=IBM,c=US");

		Attribute newRDN = e.newAttribute("newrdn");
		newRDN.setValue("Molly Weak");
		
		Attribute newsuperior = e.newAttribute("newsuperior");
		newsuperior.setValue("ou=expeople,o=IBM,c=US");
					
		Attribute oldDN = e.newAttribute("deleteoldrdn");
		oldDN.setValue("0");
		
		e.setOperation("modrdn");
		
		parser.writeEntry(e);
		
		String result = "dn: cn=Molly Weak,ou=HR,o=IBM,c=US\n"
			+ "changetype: modrdn\n"
			+ "newrdn: Molly Weak\n"
			+ "newsuperior: ou=expeople,o=IBM,c=US\n"
			+ "deleteoldrdn: 0\n\n";
	
		String ourResult = sw.toString().replace("\\\\", "\\");
		assertEquals(result, TestUtils.removeReturnCharacters(ourResult));
	}

	@BeforeClass
	public static void initParser() throws Exception {
		parser = new LDIFParser();
		parser.setInputStream("version: 1\\n\\n"
						+ "# add person\n"
						+ "dn: cn=Angelina Smith,ou=Development Team,o=IBM,c=US\n"
						+ "changetype: add\n"
						+ "cn: Angelina Smith\n"
						+ "cn: AngelinaS\n"
						+ "sn: Smith\n"
						+ "mail: asmith@us.IBM.com\n"
						+ "telephoneNumber: 69332\n"
						+ "photo: \\$HOME_DIR/empdir/photog/angie.jpg\n"
						+ "objectClass: organizationalPerson\n"
						+ "objectClass: person\n"
						+ "objectClass: top\n\n"
						+ "# delete person\n"
						+ "dn: cn=Suzzie Smith,ou=Development Team,o=IBM,c=US\n"
						+ "changetype: delete\n\n" 
						+ "# update person\n"
						+ "dn: cn=Barbara Willson,ou=Sales,o=IBM,c=US\n"
						+ "changetype: modify\n"
						+ "add: work-phone\n"
						+ "work-phone: 650/506-7000\n"
						+ "work-phone: 650/506-7001\n" + "-\n"
						+ "delete: home-fax\n" + "-\n"
						+ "replace: home-phone\n"
						+ "home-phone: 415/697-8899\n\n" 
						+ "# modify RDN\n"
						+ "dn: cn=Sally Jones,ou=HR,o=IBM,c=US\n"
						+ "changetype: modrdn\n"
						+ "newrdn: Sally Jones-Sampson\n"
						+ "deleteoldrdn: 1\n\n"
						+ "# modify DN\n"
						+ "dn: cn=Molly Weak,ou=HR,o=IBM,c=US\n"
						+ "changetype: moddn\n"
						+ "newrdn: Molly Weak\n"
						+ "newsuperior: ou=expeople,o=IBM,c=US\n"
						+ "deleteoldrdn: 0\n");
		parser.initParser();
	}
	
	@Test
	public void test_Reading_Entry_With_Add_operation() throws IOException {
		Entry e = parser.readEntry();
		assertEquals("add", e.getOperation());
		assertEquals(7, e.getAttributeNames().length);
		
		Attribute dn = e.getAttribute("$dn");
		assertEquals("$dn", dn.getName());
		assertEquals("cn=Angelina Smith,ou=Development Team,o=IBM,c=US", dn.getValue());
		assertEquals(1, dn.size());
		
		Attribute sn = e.getAttribute("sn");
		assertEquals("sn", sn.getName());
		assertEquals("Smith", sn.getValue(0));
		assertEquals(1, sn.size());
		
		Attribute cn = e.getAttribute("cn");
		assertEquals("cn", cn.getName());
		assertEquals("Angelina Smith", cn.getValue(0));
		assertEquals("AngelinaS", cn.getValue(1));
		assertEquals(2, cn.size());
		
		Attribute mail = e.getAttribute("mail");
		assertEquals("mail", mail.getName());
		assertEquals("asmith@us.IBM.com", mail.getValue());
		assertEquals(1, mail.size());
		
		Attribute photo = e.getAttribute("photo");
		assertEquals("photo", photo.getName());
		assertEquals("\\$HOME_DIR/empdir/photog/angie.jpg", photo.getValue());
		assertEquals(1, photo.size());
		
		Attribute phone = e.getAttribute("telephoneNumber");
		assertEquals("telephoneNumber", phone.getName());
		assertEquals("69332", phone.getValue());
		assertEquals(1, phone.size());
		
		Attribute objClass = e.getAttribute("objectClass");
		assertEquals("objectClass", objClass.getName());
		assertEquals("organizationalPerson", objClass.getValue(0));
		assertEquals("person", objClass.getValue(1));
		assertEquals("top", objClass.getValue(2));
		assertEquals(3, objClass.size());
	}
	
	@Test
	public void test_Reading_Entry_With_Delete_operation() throws IOException{
		Entry e = parser.readEntry();
		assertEquals("delete", e.getOperation());
		assertEquals(1, e.getAttributeNames().length);
		
		Attribute dn = e.getAttribute("$dn");
		assertEquals("$dn", dn.getName());
		assertEquals("cn=Suzzie Smith,ou=Development Team,o=IBM,c=US", dn.getValue());
		assertEquals(1, dn.size());	
	}
	
	@Test
	public void test_Reading_Entry_With_Modify_operation() throws IOException {
		Entry e = parser.readEntry();
		assertEquals("modify", e.getOperation());
		assertEquals(4, e.getAttributeNames().length);
		
		Attribute dn = e.getAttribute("$dn");
		assertEquals("$dn", dn.getName());
		assertEquals("cn=Barbara Willson,ou=Sales,o=IBM,c=US", dn.getValue());
		assertEquals(1, dn.size());
		
		Attribute phone = e.getAttribute("work-phone");
		assertEquals("modify", phone.getOperation());
		assertEquals("add", phone.getValueOperation(0));
		assertEquals("add", phone.getValueOperation(1));
		assertEquals("650/506-7000", phone.getValue(0));
		assertEquals("650/506-7001", phone.getValue(1));
		assertEquals(2, phone.size());
		
		Attribute fax = e.getAttribute("home-fax");
		assertEquals("delete", fax.getOperation());
		assertEquals("home-fax", fax.getName());
		assertEquals(null, fax.getValue());
		assertEquals(0, fax.size());
		
		phone = null;
		phone = e.getAttribute("home-phone");
		assertEquals("replace", phone.getOperation());
		assertEquals("415/697-8899", phone.getValue());
		assertEquals(1, phone.size());
	}
	
	@Test
	public void test_Reading_Entry_With_ModRDN_operation() throws IOException {
		Entry e = parser.readEntry();
		assertEquals("modify", e.getOperation());
		assertEquals(3, e.getAttributeNames().length);
		
		Attribute dn = e.getAttribute("$dn");
		assertEquals("$dn", dn.getName());
		assertEquals("cn=Sally Jones,ou=HR,o=IBM,c=US", dn.getValue());
		assertEquals(1, dn.size());
		
		Attribute newRDN = e.getAttribute("newrdn");
		assertEquals("newrdn", newRDN.getName());
		assertEquals("Sally Jones-Sampson", newRDN.getValue());
		assertEquals(1, newRDN.size());
		
		Attribute delOldRDN = e.getAttribute("deleteoldrdn");
		assertEquals("deleteoldrdn", delOldRDN.getName());
		assertEquals("1", delOldRDN.getValue());
		assertEquals(1, delOldRDN.size());
	}
	
	@Test
	public void test_Reading_Entry_With_ModDN_operation() throws IOException {
		Entry e = parser.readEntry();
		assertEquals("modify", e.getOperation());
		assertEquals(4, e.getAttributeNames().length);
		
		Attribute dn = e.getAttribute("$dn");
		assertEquals("$dn", dn.getName());
		assertEquals("cn=Molly Weak,ou=HR,o=IBM,c=US", dn.getValue());
		assertEquals(1, dn.size());

		Attribute newsuperior = e.getAttribute("newsuperior");
		assertEquals("newsuperior", newsuperior.getName());
		assertEquals("ou=expeople,o=IBM,c=US", newsuperior.getValue());
		assertEquals(1, newsuperior.size());
		
		Attribute newRDN = e.getAttribute("newrdn");
		assertEquals("newrdn", newRDN.getName());
		assertEquals("Molly Weak", newRDN.getValue());
		assertEquals(1, newRDN.size());
		
		Attribute delOldRDN = e.getAttribute("deleteoldrdn");
		assertEquals("deleteoldrdn", delOldRDN.getName());
		assertEquals("0", delOldRDN.getValue());
		assertEquals(1, delOldRDN.size());
	}
}
