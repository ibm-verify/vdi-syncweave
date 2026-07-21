package com.ibm.di.parser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.io.FileInputStream;

import org.junit.After;
import org.junit.Test;

import com.ibm.di.entry.Entry;

/**
 * What is tested: <ul> 
 * <li>reading a standard delta IdML with CIs and Relationships</li> 
 * <li>reading MSS with CI's additional XML attributes</li>
 * <li>reading an IdML with a variety of operations (create, delete, modify)</li> 
 * <li>reading a refresh IdML</li> <li>reading an invalid refresh IdML causes an Exception</li> 
 * <li>reading an IdML with invalid operation causes an Exception</li> 
 * <li>reading an IdML with extension attributes</li>
 * <li>reading an IdML with custom namespace local names</li> 
 * <li>non-CDM CI attributes are skipped upon reading</li>
 * <li>reading IdML with many MSS elements causes an exception</li> 
 * </ul>
 * 
 */
public class IdMLParserTest {

	private static final String IDML_TYPE_ATTR = "$idmlType";
	private static final String OPERATION_ATTR = "$operation";
	private static final String CDM_VERSION = "2.4.19";

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static IdMLParser parser = new IdMLParser();

	@After
	public void closeIdMLParser() throws Exception {
		parser.closeParser();
	}

	@Test
	public void test_Read_Delta_IdML_Using_Create_Operation_With_Two_CIs_And_A_Relationship() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.operation.with.two.cis.and.a.relationship.xml"));
		parser.initInput();

		// first CI
		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"cdm:Signature", "cs_signature", //
				"cdm:Fqdn", "cs_fqdn" });

		// second CI
		entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.OperatingSystem", new String[] { "$id", "osid_1", //
				"cdm:OSName", "os_name" });

		// Relationship
		entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "RELATIONSHIP", "installedOn", new String[] { "source", "osid_1", //
				"target", "csid_1" });
	}

	@Test
	public void test_Read_Delta_IdML_Using_Create_Operation_With_Each_CI_Having_Additional_XML_Attributes() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.operation.each.ci.having.additional.attributes.xml"));
		parser.initInput();

		// read CI
		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("mss.sourceToken"), "mss_source_token");
		assertEquals(entry.getString("mss.sourceContactInfo"), "mss_source_contact_info");
		assertEquals(entry.getString("mss.superior"), "mss_sup_id");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"$superior", "sup_id", //
				"$sourceContactInfo", "source_contact_info", //
				"$sourceToken", "source_token", //
				"cdm:Signature", "cs_signature", //
				"cdm:Fqdn", "cs_fqdn" });
	}

	@Test
	public void test_Read_Delta_IdML_Using_Create_Delete_And_Modify_Operations() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.delete.modify.operations.with.one.ci.each.xml"));
		parser.initInput();

		// first CI
		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"cdm:Signature", "cs_signature_1", //
				"cdm:Fqdn", "cs_fqdn_1" });

		// second CI
		entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "delete");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_2", //
				"cdm:Signature", "cs_signature_2", //
				"cdm:Fqdn", "cs_fqdn_2" });

		// third CI
		entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "modify");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_3", //
				"cdm:Signature", "cs_signature_3", //
				"cdm:Fqdn", "cs_fqdn_3" });
	}

	@Test
	public void test_Read_Refresh_IdML_Using_Create_Operation() throws Exception {
		parser.setInputStream(new FileInputStream("./resources/IdML_Input/refresh.idml.using.create.operation.with.one.ci.xml"));
		parser.initInput();

		// first CI
		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString(IDML_TYPE_ATTR), "refresh");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"cdm:Signature", "cs_signature_1", //
				"cdm:Fqdn", "cs_fqdn_1" });
	}

	@Test
	public void test_Read_Refresh_IdML_Using_Delete_Operation() throws Exception {
		parser.setInputStream(new FileInputStream("./resources/IdML_Input/refresh.idml.using.delete.operation.with.one.ci.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNull(entry);
	}

	@Test
	public void test_Read_Refresh_IdML_Using_Modify_Operation() throws Exception {
		parser.setInputStream(new FileInputStream("./resources/IdML_Input/refresh.idml.using.modify.operation.with.one.ci.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNull(entry);
	}

	@Test
	public void test_Read_Invalid_IdML_Using_Invalid_Operation() throws Exception {
		parser.setInputStream(new FileInputStream("./resources/IdML_Input/invalid.idml.using.invalid.operation.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNull(entry);
	}

	@Test
	public void test_Read_MSS_And_CI_Data_Containing_Extention_Attributes() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.operation.with.extended.attributes.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString("mss.extattr:mssExt1"), "mssExtValue1");
		assertEquals(entry.getString("mss.extattr:mssExt2"), "mssExtValue2");

		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"cdm:Signature", "cs_signature_1", //
				"cdm:Fqdn", "cs_fqdn_1", //
				"cdm:extattr:ext1", "extValue1",//	
				"cdm:extattr:ext2", "extValue2",//
		});
	}

	@Test
	public void test_Read_IdML_Not_Using_Default_Local_Names_For_The_IdML_And_CDM_Namespaces() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.operation.with.custom.namespaces.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertEquals(entry.getString("$cdmVersion"), CDM_VERSION);
		assertMssData(entry);
		assertEquals(entry.getString("mss.extattr:mssExt1"), "mssExtValue1");
		assertEquals(entry.getString("mss.extattr:mssExt2"), "mssExtValue2");

		assertEquals(entry.getString(IDML_TYPE_ATTR), "delta");
		assertEquals(entry.getString(OPERATION_ATTR), "create");
		assertArtifact(entry, "CI", "sys.ComputerSystem", new String[] { "$id", "csid_1", //
				"cdm:Signature", "cs_signature_1", //
				"cdm:Fqdn", "cs_fqdn_1", //
				"cdm:extattr:ext1", "extValue1",//	
				"cdm:extattr:ext2", "extValue2",//
		});
	}

	@Test
	public void test_Non_CDM_CI_Attributes_Are_Not_Read() throws Exception {
		parser.setInputStream(new FileInputStream(
				"./resources/IdML_Input/delta.idml.using.create.operation.with.incorrect.ci.elements.xml"));
		parser.initInput();

		Entry entry = parser.readEntry();
		assertNotNull(entry);
		assertNull(entry.getString("cdm:nonCdmAttribute"));
		assertNull(entry.getString("mss.nonCdmAttribute"));
	}

	@Test
	public void test_Read_Incorrect_IdML_Not_Starting_With_The_MSS_Data() throws Exception {
		parser.setInputStream(new FileInputStream("./resources/IdML_Input/invalid.idml.with.two.mss.xml"));
		parser.initInput();
		boolean success = false;
		try {
			parser.readEntry();
		} catch (Exception ex) {
			success = true;
		}
		assertTrue(success);
	}

	private void assertMssData(Entry entry) {
		assertArtifact(entry, new String[] { "mss.id", "mss_id", //
				"mss.Hostname", "mss_hostname", //
				"mss.Label", "mss_label", //
				"mss.ManufacturerName", "mss_manufacturer", //
				"mss.MSSName", "ibm-cdm:///CDMMSS/mss_name", //
				"mss.ProductName", "mss_product" });
	}

	private void assertArtifact(Entry entry, String artifactType, String classType, String[] attributes) {
		assertEquals(entry.getString("$artifactType"), artifactType);
		assertEquals(entry.getString("$classType"), classType);
		assertArtifact(entry, attributes);
	}

	private void assertArtifact(Entry entry, String[] attributes) {
		for (int i = 0; i < attributes.length; i = i + 2) {
			assertEquals(entry.getString(attributes[i]), attributes[i + 1]);
		}
	}

}
