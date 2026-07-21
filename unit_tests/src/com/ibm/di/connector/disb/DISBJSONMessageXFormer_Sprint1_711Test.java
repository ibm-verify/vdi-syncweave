package com.ibm.di.connector.disb;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.junit.Test;

import com.ibm.di.connector.disb.DISBJSONMessageTransformer;
import com.ibm.di.connector.disb.model.ConfigurationItem;
import com.ibm.di.connector.disb.model.Create;
import com.ibm.di.connector.disb.model.Delete;
import com.ibm.di.connector.disb.model.Modify;
import com.ibm.di.connector.disb.model.OperationSet;
import com.ibm.di.connector.disb.model.Relationship;
import com.ibm.di.connector.disb.model.Refresh;
import com.ibm.json.java.OrderedJSONObject;

public class DISBJSONMessageXFormer_Sprint1_711Test {
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getInstanceOperationSet_CI_create()throws Exception{
		
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"NamingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}}";	
		System.out.println("####");
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		com.ibm.di.connector.disb.model.ConfigurationItem[] cis = opset.getCreate().getConfigurationItems();	
		HashMap<String, Object> attrs = cis[0].getAttributes();
		
		String key = "NamingContext";
		if (attrs.get(key) instanceof HashMap)
		{
			HashMap<String,Object> attr = (HashMap<String,Object>)attrs.get(key);
			assertTrue(attr.get("source").toString().equals("1234"));
			assertTrue(attr.get("target").toString().equals("3456"));
		}
		else
			assertTrue(false);		
		
		assertTrue(opset.getCreate().getTimeStamp().equals("2011-03-07T12:28:18Z"));
		assertTrue(opset.getOpId().equals("1"));		
	}

	
	@Test
	public void test_getInstanceOperationSet_CI_refresh()throws Exception{
		String jsonmsg = "{\"operationSet\": {\"opid\": \"single transaction\",\"refresh\": {\"timeStamp\": \"2011-02-16T14:59:50Z\",\"create\": {\"timeStamp\": \"2011-02-16T14:59:50Z\",\"modelObject\": {\"sys.ComputerSystem\": [{\"id\": \"sys.ComputerSystem-1\",\"sourceToken\": \"1\",\"SerialNumber\": \"00FFBABC13D8\",\"Manufacturer\": \"Intel\",\"Model\": \"Pentium 4\"},{\"id\": \"sys.ComputerSystem-2\",\"sourceToken\": \"2\",\"SerialNumber\": \"03ACB26791DE\",\"Manufacturer\": \"Intel\",\"Model\": \"Pentium DualCore\"},{\"id\": \"sys.ComputerSystem-3\",\"sourceToken\": \"3\",\"SerialNumber\": \"54DAFF327B63\",\"Manufacturer\": \"Intel\",\"Model\": \"Xeon Tigerton\"},{\"id\": \"sys.ComputerSystem-4\",\"sourceToken\": \"4\",\"SerialNumber\": \"750ABEAE54BD\",\"Manufacturer\": \"AMD\",\"Model\": \"lomino\"}],\"sys.windows.WindowsOperatingSystem\": [{\"id\": \"sys.windows.WindowsOperatingSystem-1\",\"sourceToken\": \"1\",\"Name\": \"Windows Server 2003 Enterprise Edition SP2\",\"OSVersion\": \"2003\",\"OSName\": \"Microsoft Windows Server 2003\",\"OsId\": \"1\",\"Label\": \"Windows Server 2003 Enterprise Edition SP2\"},{\"id\": \"sys.windows.WindowsOperatingSystem-2\",\"sourceToken\": \"2\",\"Name\": \"Windows Server 2003 Enterprise Edition SP2\",\"OSVersion\": \"2003\",\"OSName\": \"Microsoft Windows Server 2003\",\"OsId\": \"2\",\"Label\": \"Windows Server 2003 Enterprise Edition SP2\"}]},\"relationship\": {\"installedOn\": [{\"target\": \"sys.ComputerSystem-2\",\"source\": \"sys.windows.WindowsOperatingSystem-1\"},{\"target\": \"sys.ComputerSystem-1\",\"source\": \"sys.windows.WindowsOperatingSystem-2\"}]}}}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();	
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		Refresh refo = opset.getRefresh();
		ConfigurationItem[] cis = refo.getCreate().getConfigurationItems();
		
		assertTrue(opset.getOpId().equals("single transaction"));
		assertTrue(opset.getRefresh().getTimeStamp().equals("2011-02-16T14:59:50Z"));	
		assertTrue(opset.getRefresh().getCreate().getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		if(6 == cis.length)
		{
			if(cis[0].getClassNameType().equals(cis[1].getClassNameType().equals(cis[2].getClassNameType().equals(cis[3].getClassNameType()))))
				assertTrue(true);
			if(cis[0].getClassNameType().equals(cis[1].getClassNameType()))
				assertTrue(true);
		
			assertTrue(cis[0].getId().equals("sys.ComputerSystem-1"));
			assertTrue(cis[1].getId().equals("sys.ComputerSystem-2"));
			assertTrue(cis[2].getId().equals("sys.ComputerSystem-3"));
			assertTrue(cis[3].getId().equals("sys.ComputerSystem-4"));
			assertTrue(cis[4].getId().equals("sys.windows.WindowsOperatingSystem-1"));
			assertTrue(cis[5].getId().equals("sys.windows.WindowsOperatingSystem-2"));
			
			assertTrue(cis[0].getProperty("sourceToken").equals("1"));
			assertTrue(cis[1].getProperty("sourceToken").equals("2"));
			assertTrue(cis[2].getProperty("sourceToken").equals("3"));
			assertTrue(cis[3].getProperty("sourceToken").equals("4"));
			assertTrue(cis[4].getProperty("sourceToken").equals("1"));
			assertTrue(cis[5].getProperty("sourceToken").equals("2"));	
			
			assertTrue(cis[4].getProperty("OsId").equals("1"));
			assertTrue(cis[5].getProperty("OsId").equals("2"));
			
			if(cis[4].getProperty("Name").equals(cis[5].getProperty("Name")))
				if(cis[4].getProperty("OSVersion").equals(cis[5].getProperty("OSVersion")))
					if(cis[4].getProperty("OSName").equals(cis[5].getProperty("OSName")))
						if(cis[4].getProperty("Label").equals(cis[5].getProperty("Label")))
							assertTrue(true);
		}
	}

	/*
	 * This test case uses input from C:\\sandbox\\ibmdi_dev\\unit_tests\\resources\\DISBConnector_Input\\ci_msg2.txt
	 */
	@Test
	public void test_getInstanceOperationSet_CI_delete(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"delete\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"modelObject\":{\"sys.OperatingSystem\":[{\"id\":\"12345\",\"aliasGuids\":[\"DISGuid0001\"],\"ITMGuid001-sourceToken\":\"ITMToken1\",\"TADDMGuid0001-sourceToken\":\"TADDMToken1\"}]}}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			ConfigurationItem[] cis = opset.getDelete().getConfigurationItems();
			
			assertTrue(opset.getOpId().equals("1"));
			assertTrue(opset.getDelete().getTimeStamp().equals("2010-07-19T07:15:15Z"));
			
			assertTrue(cis[0].getClassNameType().equals("sys.OperatingSystem"));
			assertTrue(cis[0].getId().equals("12345"));
			assertTrue(cis[0].getProperty("ITMGuid001-sourceToken").equals("ITMToken1"));
			assertTrue(cis[0].getProperty("TADDMGuid0001-sourceToken").equals("TADDMToken1"));
			assertTrue(cis[0].getProperty("aliasGuids[1]").equals("DISGuid0001"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * This test case uses input from C:\\sandbox\\ibmdi_dev\\unit_tests\\resources\\DISBConnector_Input\\ci_msg3.txt
	 */
	@Test
	public void test_getInstanceOperationSet_CI_modify(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"modify\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"modelObject\":{\"sys.OperatingSystem\":[{\"id\":\"12345\",\"masterGuid\":\"DISGuid0001\",\"aliasGuids\":[\"DISGuid0001\",\"DISGuidXXX1\",\"DISGuidYYY1\"],\"aKey\":\"aValue\",\"sourceToken\":\"newSourceToken\",\"guid\":\"DISGuid001\",\"ITMGuid0001_sourceToken\":\"ITMToken1\",\"TADDMGuid0001_sourceToken\":\"TADDMToken1\",\"oldValues\":{\"sourceToken\":\"aSourceToken\"}}]}}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			ConfigurationItem[] cis = opset.getModify().getConfigurationItems();
			
			assertTrue(opset.getOpId().equals("1"));
			assertTrue(opset.getModify().getTimeStamp().equals("2010-07-19T07:15:15Z"));
			assertTrue(cis[0].getClassNameType().equals("sys.OperatingSystem"));
			assertTrue(cis[0].getId().equals("12345")); 
			assertTrue(cis[0].getProperty("guid").equals("DISGuid001"));
			assertTrue(cis[0].getProperty("aliasGuids[1]").equals("DISGuid0001"));
			assertTrue(cis[0].getProperty("aliasGuids[2]").equals("DISGuidXXX1"));
			assertTrue(cis[0].getProperty("aliasGuids[3]").equals("DISGuidYYY1"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		
	}
	
	/*
	 * This test case uses input from C:\\sandbox\ibmdi_dev\\unit_tests\\resources\\DISBConnector_Input\\relationship_msg1.txt
	 */
	@Test
	public void test_getInstanceOperationSet_relationship_create(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"reference\":{\"modelObject\":{\"sys.ComputerSystem\":[{\"id\":\"45678\",\"sourceToken\":\"ITM-CS-56789\"}],\"sys.OperatingSystem\":[{\"id\":\"12345\",\"sourceToken\":\"ITM-OS-12345\"}]}},\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"relationship\":{\"contains\":[{\"source\":\"45678\",\"target\":\"12345\"}]}},}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			ConfigurationItem[] cis = opset.getReference().getConfigurationItems();
			//Relationship[] rels = opset.getReference().getRelationships();
			Relationship[] rels = opset.getCreate().getRelationships();
			
			assertTrue(opset.getOpId().equals("1"));	
			if(cis.length == 2)
			{
				assertTrue(cis[0].getClassNameType().equals("sys.ComputerSystem"));
				assertTrue(cis[0].getId().equals("45678"));
				assertTrue(cis[0].getProperty("sourceToken").equals("ITM-CS-56789"));
				assertTrue(cis[1].getClassNameType().equals("sys.OperatingSystem"));
				assertTrue(cis[1].getId().equals("12345"));
				assertTrue(cis[1].getProperty("sourceToken").equals("ITM-OS-12345"));
			}
			assertTrue(opset.getCreate().getTimeStamp().equals("2010-07-19T07:15:15Z"));
			assertTrue(rels[0].getRelationShipType().equals("contains"));
			assertTrue(rels[0].getSource().equals("45678"));
			assertTrue(rels[0].getTarget().equals("12345"));
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}	
	}
	
	/*
	 * This test case uses input from C:\\sandbox\\ibmdi_dev\\unit_tests\\resources\\DISBConnector_Input\\relationship_msg2.txt
	 */
	@Test
	public void test_getInstanceOperationSet_relationship_create_tc2(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"reference\":{\"modelObject\":{\"sys.ComputerSystem\":[{\"id\":\"45678\",\"sourceToken\":\"ITM-CS-56789\"}]}},\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"modelObject\":{\"sys.OperatingSystem\":[{\"id\":\"12345\",\"sourceToken\":\"ITM-OS-34891\",\"OSName\":\"AIX\"}]},\"relationship\":{\"installedOn\":[{\"source\":\"45678\",\"target\":\"12345\"}]}}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			ConfigurationItem[] cis = opset.getReference().getConfigurationItems();
			Relationship[] rels = opset.getCreate().getRelationships();
			
			assertTrue(opset.getOpId().equals("1"));	
			
			assertTrue(cis[0].getClassNameType().equals("sys.ComputerSystem"));
			assertTrue(cis[0].getId().equals("45678"));
			assertTrue(cis[0].getProperty("sourceToken").equals("ITM-CS-56789"));
						
			assertTrue(opset.getCreate().getTimeStamp().equals("2010-07-19T07:15:15Z"));
			

			assertTrue(rels[0].getRelationShipType().equals("installedOn"));
			assertTrue(rels[0].getSource().equals("45678"));
			assertTrue(rels[0].getTarget().equals("12345"));
			
			cis = opset.getCreate().getConfigurationItems();
			
			assertTrue(cis[0].getClassNameType().equals("sys.OperatingSystem"));
			assertTrue(cis[0].getId().equals("12345"));
			assertTrue(cis[0].getProperty("sourceToken").equals("ITM-OS-34891"));
			assertTrue(cis[0].getProperty("OSName").equals("AIX"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}	
	}
	
	/*
	 * This test case uses input from C:\\sandbox\\ibmdi_dev\\unit_tests\\resources\\DISBConnector_Input\\relationship_msg5.txt
	 */
	@Test
	public void test_getInstanceOperationSet_relationship_delete(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"reference\":{\"modelObject\":{\"sys.OperatingSystem\":[{\"id\":\"12345\",\"masterGuid\":\"DISGuid0001\",\"aliasGuids\":[\"DISGuid0001\",\"DISGuidYYY1\"],\"MSSGuid1_sourceToken\":\"TADDMToken1\"}],\"app.SoftwareInstallation\":[{\"id\":\"678901\",\"masterGuid\":\"DISGuid0004\",\"aliasGuids\":[\"DISGuid0004\"]}]}},\"delete\":{\"timeStamp\":\"2010-07-19TO7:15:15Z\",\"relationship\":{\"installedOn\":[{\"source\":\"12345\",\"target\":\"678901\"}]}}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			ConfigurationItem[] cis = opset.getReference().getConfigurationItems();
			Relationship[] rels = opset.getDelete().getRelationships();
			
			assertTrue(opset.getOpId().equals("1"));	
			if(cis.length == 2)
			{
				assertTrue(cis[0].getClassNameType().equals("sys.OperatingSystem"));
				assertTrue(cis[0].getId().equals("12345"));
				assertTrue(cis[0].getProperty("masterGuid").equals("DISGuid0001"));
				assertTrue(cis[0].getProperty("MSSGuid1_sourceToken").equals("TADDMToken1"));
				assertTrue(cis[0].getProperty("aliasGuids[1]").equals("DISGuid0001"));
				assertTrue(cis[0].getProperty("aliasGuids[2]").equals("DISGuidYYY1"));

				assertTrue(cis[1].getClassNameType().equals("app.SoftwareInstallation"));
				assertTrue(cis[1].getId().equals("678901"));
				assertTrue(cis[1].getProperty("masterGuid").equals("DISGuid0004"));
				assertTrue(cis[1].getProperty("aliasGuids[1]").equals("DISGuid0004"));
			}
			
			assertTrue(opset.getDelete().getTimeStamp().equals("2010-07-19TO7:15:15Z"));
			assertTrue(rels[0].getRelationShipType().equals("installedOn"));
			assertTrue(rels[0].getSource().equals("12345"));
			assertTrue(rels[0].getTarget().equals("678901"));
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/*
	 * This test case is just to check we can hit exception case or not in getInstanceOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getInstanceOperationSet_create_tc2(){
		String jsonmsg = "{\"operationSet-\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"NamingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();	
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
	}
		
	@SuppressWarnings("unchecked")
	@Test
	public void test_getGuidOperationSet_split() throws Exception{
		String jsonmsg = "{\"sys.ComputerSystem\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"old\":[{\"MSSTADDMGuid_sourceToken\":\"TADDMToken\",\"Signature\":\"sig1\",\"Model\":\"T60\",\"Manufacturer\":\"IBM\",\"SerialNumber\":\"1111\",\"guids\":[\"1111\",\"2222\"]}],\"new\":[{\"MSSTADDMGuid_sourceToken\":\"TADDMToken\",\"Signature\":\"sig1\",\"guids\":[\"1111\"]},{\"MSSITMGuid_sourceToken\":\"ITMToken\",\"Signature\":\"sig2\",\"Model\":\"T60\",\"Manufacturer\":\"IBM\",\"SerialNumber\":\"1111\",\"guids\":[\"3333\",\"2222\"]}]}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		ConfigurationItem ci = msgXformer.getGuidConfigItem(jsonmsg);

		HashMap<String, Object> attrs = ci.getAttributes();
		String v = null;
		HashMap<String, Object> attr = null;
		
		assertTrue(ci.getClassNameType().equals("sys.ComputerSystem"));

		v = (String)attrs.get("timeStamp");
		assertTrue(v.equals("2010-07-19T07:15:15Z"));
		
		assertTrue(attrs.containsKey("old[1]"));
		attr = (HashMap<String,Object>)attrs.get("old[1]");
		
		assertTrue(attr.containsKey("MSSTADDMGuid_sourceToken"));
		v = (String)attr.get("MSSTADDMGuid_sourceToken");
		assertTrue(v.equals("TADDMToken"));
		
		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig1"));
		
		assertTrue(attr.containsKey("Model"));
		v = (String)attr.get("Model");
		assertTrue(v.equals("T60"));
		
		assertTrue(attr.containsKey("Manufacturer"));
		v = (String)attr.get("Manufacturer");
		assertTrue(v.equals("IBM"));
		
		assertTrue(attr.containsKey("SerialNumber"));
		v = (String)attr.get("SerialNumber");
		assertTrue(v.equals("1111"));
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("1111"));
		
		assertTrue(attr.containsKey("guids[2]"));
		v = (String)attr.get("guids[2]");
		assertTrue(v.equals("2222"));
		
		
		
		assertTrue(attrs.containsKey("new[1]"));
		attr = (HashMap<String,Object>)attrs.get("new[1]");
		
		assertTrue(attr.containsKey("MSSTADDMGuid_sourceToken"));
		v = (String)attr.get("MSSTADDMGuid_sourceToken");
		assertTrue(v.equals("TADDMToken"));
		
		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig1"));
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("1111"));
		
		
		assertTrue(attrs.containsKey("new[2]"));
		attr = (HashMap<String,Object>)attrs.get("new[2]");
		
		assertTrue(attr.containsKey("MSSITMGuid_sourceToken"));
		v = (String)attr.get("MSSITMGuid_sourceToken");
		assertTrue(v.equals("ITMToken"));
		
		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig2"));
		
		assertTrue(attr.containsKey("Model"));
		v = (String)attr.get("Model");
		assertTrue(v.equals("T60"));
		
		assertTrue(attr.containsKey("Manufacturer"));
		v = (String)attr.get("Manufacturer");
		assertTrue(v.equals("IBM"));
		
		assertTrue(attr.containsKey("SerialNumber"));
		v = (String)attr.get("SerialNumber");
		assertTrue(v.equals("1111"));		
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("3333"));
		
		assertTrue(attr.containsKey("guids[2]"));
		v = (String)attr.get("guids[2]");
		assertTrue(v.equals("2222"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getGuidOperationSet_merge() throws Exception{
		String jsonmsg = "{\"sys.ComputerSystem\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"old\":[{\"MSSTADDMGuid_sourceToken\":\"TADDMToken\",\"Signature\":\"sig1\",\"guids\":[\"1111\"]},{\"MSSTBSMGuid_sourceToken\":\"TBSMToken\",\"Signature\":\"sig1\",\"Model\":\"T60\",\"Manufacturer\":\"IBM\",\"SerialNumber\":\"1112\",\"guids\":[\"4444\"]}],\"new\":[{\"MSSTADDMGuid_sourceToken\":\"TADDMToken\",\"MSSTBSMGuid_sourceToken\":\"TBSMToken\",\"Signature\":\"sig1\",\"Model\":\"T60\",\"Manufacturer\":\"IBM\",\"SerialNumber\":\"1112\",\"guids\":[\"1111\",\"4444\"]}]}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		ConfigurationItem ci = msgXformer.getGuidConfigItem(jsonmsg);

		HashMap<String, Object> attrs = ci.getAttributes();
		String v = null;
		HashMap<String, Object> attr = null;
		
		assertTrue(ci.getClassNameType().equals("sys.ComputerSystem"));

		v = (String)attrs.get("timeStamp");
		assertTrue(v.equals("2010-07-19T07:15:15Z"));
		
		assertTrue(attrs.containsKey("old[1]"));
		attr = (HashMap<String,Object>)attrs.get("old[1]");
		
		assertTrue(attr.containsKey("MSSTADDMGuid_sourceToken"));
		v = (String)attr.get("MSSTADDMGuid_sourceToken");
		assertTrue(v.equals("TADDMToken"));

		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig1"));
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("1111"));
		
		assertTrue(attrs.containsKey("old[2]"));
		attr = (HashMap<String,Object>)attrs.get("old[2]");
		
		assertTrue(attr.containsKey("MSSTBSMGuid_sourceToken"));
		v = (String)attr.get("MSSTBSMGuid_sourceToken");
		assertTrue(v.equals("TBSMToken"));
		
		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig1"));
		
		assertTrue(attr.containsKey("Model"));
		v = (String)attr.get("Model");
		assertTrue(v.equals("T60"));
		
		assertTrue(attr.containsKey("Manufacturer"));
		v = (String)attr.get("Manufacturer");
		assertTrue(v.equals("IBM"));
		
		assertTrue(attr.containsKey("SerialNumber"));
		v = (String)attr.get("SerialNumber");
		assertTrue(v.equals("1112"));
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("4444"));
		
		assertTrue(attrs.containsKey("old[2]"));
		attr = (HashMap<String,Object>)attrs.get("new[1]");
		
		assertTrue(attr.containsKey("MSSTADDMGuid_sourceToken"));
		v = (String)attr.get("MSSTADDMGuid_sourceToken");
		assertTrue(v.equals("TADDMToken"));
		
		assertTrue(attr.containsKey("MSSTBSMGuid_sourceToken"));
		v = (String)attr.get("MSSTBSMGuid_sourceToken");
		assertTrue(v.equals("TBSMToken"));
		
		assertTrue(attr.containsKey("Signature"));
		v = (String)attr.get("Signature");
		assertTrue(v.equals("sig1"));
		
		assertTrue(attr.containsKey("Model"));
		v = (String)attr.get("Model");
		assertTrue(v.equals("T60"));
		
		assertTrue(attr.containsKey("Manufacturer"));
		v = (String)attr.get("Manufacturer");
		assertTrue(v.equals("IBM"));
		
		assertTrue(attr.containsKey("SerialNumber"));
		v = (String)attr.get("SerialNumber");
		assertTrue(v.equals("1112"));
		
		assertTrue(attr.containsKey("guids[1]"));
		v = (String)attr.get("guids[1]");
		assertTrue(v.equals("1111"));
		
		assertTrue(attr.containsKey("guids[2]"));
		v = (String)attr.get("guids[2]");
		assertTrue(v.equals("4444"));
	}
	
	/*
	 * This test case is just to test whether it return empty CI object or not
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getGuidOperationSet_split_tc2() throws Exception{
		String jsonmsg = "{}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		ConfigurationItem ci = msgXformer.getGuidConfigItem(jsonmsg);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test_getModelOperationSet_create()throws Exception{
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"Classes\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"parentClassname\":\"app/lotus/IMAPConfig\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\"},{\"classname\":\"complicted/classname\",\"classNamespace\":\"complictedns\",\"parentClassname\":\"net/Fqdn\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\",\"guid\":\"anOPtionalClassGuid\"}],\"Attributes\":[{\"name\":\"anAttribute\",\"className\":\"simple/classname\",\"guid\":\"anOptionalAttributeGuid\",\"classNamespace\":\"simplens\",\"dataType\":\"Boolean\",\"length\":\"anOptionalAttributeLength\",\"namespace\":\"an-att-namespace\",\"description\":\"A new sub attribute\"}],\"NamingRules\":[{\"name\":\"aNamingPolicy\",\"guid\":\"anOptionalNamingPolicyGuid\",\"namespace\":\"a-np-namespace\",\"NamingRule\":[{\"name\":\"aNamingRule\",\"guid\":\"anOptionalNamingRuleGuid\",\"priority\":\"0\",\"namespace\":\"a-nr-namespace\",\"Identifier\":[{\"keyword\":\"Parent\",\"namespace\":\"an-att-namespace1\",\"order\":\"0\",\"required\":\"true\",\"value\":\"parent\"},{\"keyword\":\"anAttribute\",\"namespace\":\"an-att-namespace\",\"order\":\"1\",\"relationship\":\"anOptionalIdentifierRelationship\",\"relationshipSource\":\"anOptionalIdentifierRelationshipSource\",\"relationshipTarget\":\"anOptionalIdentifierRelationshipTarget\",\"omitted\":\"anOptionalOmittedValue\",\"required\":\"true\",\"value\":\"aSampleNamingrule\"}]}]}],\"MappingPolicies\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"policy\":\"aNamingPolicy\",\"policyNameSpace\":\"a-np-namespace\"}],\"Relationships\":[{\"source\":{\"cardinality\":\"1\",\"classNamespace\":\"simplens\",\"classname\":\"simple/classname\"},\"target\":{\"cardinality\":\"*\",\"classNamespace\":\"complictedns\",\"classname\":\"complicted/classname\",\"role\":\"anOptionalTargetRole\"},\"namespace\":\"a-rel-namespace\",\"guid\":\"anOptionalRelationshipGuid\",\"type\":\"uses\"}]}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		ConfigurationItem[] cis = opset.getCreate().getConfigurationItems();
		
		assertTrue(opset.getOpId().equals("1"));
		assertTrue(opset.getCreate().getTimeStamp().equals("2010-07-19T07:15:15Z"));
		
		if(cis.length == 6)
		{
			if(cis[0].getClassNameType().equals(cis[1].getClassNameType().equals("Classes")))
				assertTrue(true);
			assertTrue(cis[2].getClassNameType().equals("Attributes"));
			assertTrue(cis[3].getClassNameType().equals("NamingRules"));
			assertTrue(cis[4].getClassNameType().equals("MappingPolicies"));
			assertTrue(cis[5].getClassNameType().equals("Relationships"));
			//unable to get guid of class from model topic message 
			//assertTrue(cis[2].getGuid().toString().equals("anOptionalAttributeGuid"));
			assertTrue(cis[0].getProperty("parentClassname").equals("app/lotus/IMAPConfig"));
			assertTrue(cis[0].getProperty("parentNamespace").equals("ibm-cdm:"));
			assertTrue(cis[0].getProperty("classname").equals("simple/classname"));
			assertTrue(cis[1].getProperty("classname").equals("complicted/classname"));
			assertTrue(cis[2].getProperty("className").equals("simple/classname"));
			assertTrue(cis[4].getProperty("classname").equals("simple/classname"));
		}
		else
			assertTrue(false);
		
		HashMap<String, Object> attrs = cis[3].getAttributes();
		String v = null;
		HashMap<String, Object> attr = null;
		HashMap<String, Object> attr1 = null;
		
		assertTrue(attrs.containsKey("name"));
		v = (String)attrs.get("name");
		assertTrue(v.equals("aNamingPolicy"));

		assertTrue(attrs.containsKey("guid"));
		v = (String)attrs.get("guid");
		assertTrue(v.equals("anOptionalNamingPolicyGuid"));
		
		assertTrue(attrs.containsKey("namespace"));
		v = (String)attrs.get("namespace");
		assertTrue(v.equals("a-np-namespace"));
		
		assertTrue(attrs.containsKey("NamingRule[1]"));
		attr = (HashMap<String,Object>)attrs.get("NamingRule[1]");
		
		assertTrue(attr.containsKey("name"));
		v = (String)attr.get("name");
		assertTrue(v.equals("aNamingRule"));
		
		assertTrue(attr.containsKey("guid"));
		v = (String)attr.get("guid");
		assertTrue(v.equals("anOptionalNamingRuleGuid"));
		
		assertTrue(attr.containsKey("priority"));
		v = (String)attr.get("priority");
		assertTrue(v.equals("0"));
		
		assertTrue(attr.containsKey("namespace"));
		v = (String)attr.get("namespace");
		assertTrue(v.equals("a-nr-namespace"));
		
		assertTrue(attr.containsKey("Identifier[1]"));
		attr1 = (HashMap<String,Object>)attr.get("Identifier[1]");
		
		assertTrue(attr1.containsKey("keyword"));
		v = (String)attr1.get("keyword");
		assertTrue(v.equals("Parent"));
		
		assertTrue(attr1.containsKey("namespace"));
		v = (String)attr1.get("namespace");
		assertTrue(v.equals("an-att-namespace1"));
		
		assertTrue(attr1.containsKey("order"));
		v = (String)attr1.get("order");
		assertTrue(v.equals("0"));
		
		assertTrue(attr1.containsKey("required"));
		v = (String)attr1.get("required");
		assertTrue(v.equals("true"));
		
		assertTrue(attr1.containsKey("value"));
		v = (String)attr1.get("value");
		assertTrue(v.equals("parent"));
		
		assertTrue(attr.containsKey("Identifier[2]"));
		attr1 = (HashMap<String,Object>)attr.get("Identifier[2]");
		
		assertTrue(attr1.containsKey("keyword"));
		v = (String)attr1.get("keyword");
		assertTrue(v.equals("anAttribute"));
		
		assertTrue(attr1.containsKey("namespace"));
		v = (String)attr1.get("namespace");
		assertTrue(v.equals("an-att-namespace"));
		
		assertTrue(attr1.containsKey("order"));
		v = (String)attr1.get("order");
		assertTrue(v.equals("1"));
		
		assertTrue(attr1.containsKey("relationship"));
		v = (String)attr1.get("relationship");
		assertTrue(v.equals("anOptionalIdentifierRelationship"));
		
		assertTrue(attr1.containsKey("relationshipSource"));
		v = (String)attr1.get("relationshipSource");
		assertTrue(v.equals("anOptionalIdentifierRelationshipSource"));
		
		assertTrue(attr1.containsKey("relationshipTarget"));
		v = (String)attr1.get("relationshipTarget");
		assertTrue(v.equals("anOptionalIdentifierRelationshipTarget"));
		
		assertTrue(attr1.containsKey("omitted"));
		v = (String)attr1.get("omitted");
		assertTrue(v.equals("anOptionalOmittedValue"));
		
		assertTrue(attr1.containsKey("required"));
		v = (String)attr1.get("required");
		assertTrue(v.equals("true"));
		
		assertTrue(attr1.containsKey("value"));
		v = (String)attr1.get("value");
		assertTrue(v.equals("aSampleNamingrule"));
		
		
		attrs = cis[5].getAttributes();
		assertTrue(attrs.containsKey("type"));
		assertTrue(attrs.containsKey("guid"));
		assertTrue(attrs.containsKey("namespace"));
		assertTrue(attrs.containsKey("source"));
		assertTrue(attrs.containsKey("target"));
		
		attr1 = (HashMap<String,Object>) attrs.get("source");
		
		v = (String) attr1.get("cardinality");
		assertTrue(v.equals("1"));
		
		v = (String) attr1.get("classNamespace");
		assertTrue(v.equals("simplens"));
		
		v = (String) attr1.get("classname");
		assertTrue(v.equals("simple/classname"));
		
		attr1 = (HashMap<String,Object>) attrs.get("target");
		
		v = (String) attr1.get("cardinality");
		assertTrue(v.equals("*"));
		
		v = (String) attr1.get("classNamespace");
		assertTrue(v.equals("complictedns"));
		
		v = (String) attr1.get("classname");
		assertTrue(v.equals("complicted/classname"));
		
		v = (String) attr1.get("role");
		assertTrue(v.equals("anOptionalTargetRole"));	
	}
	
	@Test
	public void test_getModelOperationSet_modify()throws Exception{
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"modify\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"Classes\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"parentClassname\":\"app/lotus/IMAPConfig\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\"},{\"classname\":\"complicted/classname\",\"classNamespace\":\"complictedns\",\"parentClassname\":\"net/Fqdn\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\",\"guid\":\"anOPtionalClassGuid\"}],\"Attributes\":[{\"name\":\"anAttribute\",\"className\":\"simple/classname\",\"guid\":\"anOptionalAttributeGuid\",\"classNamespace\":\"simplens\",\"dataType\":\"Boolean\",\"length\":\"anOptionalAttributeLength\",\"namespace\":\"an-att-namespace\",\"description\":\"A new sub attribute\"}],\"NamingRules\":[{\"name\":\"aNamingPolicy\",\"guid\":\"anOptionalNamingPolicyGuid\",\"namespace\":\"a-np-namespace\",\"NamingRule\":[{\"name\":\"aNamingRule\",\"guid\":\"anOptionalNamingRuleGuid\",\"priority\":\"0\",\"namespace\":\"a-nr-namespace\",\"Identifier\":[{\"keyword\":\"Parent\",\"namespace\":\"an-att-namespace1\",\"order\":\"0\",\"required\":\"true\",\"value\":\"parent\"},{\"keyword\":\"anAttribute\",\"namespace\":\"an-att-namespace\",\"order\":\"1\",\"relationship\":\"anOptionalIdentifierRelationship\",\"relationshipSource\":\"anOptionalIdentifierRelationshipSource\",\"relationshipTarget\":\"anOptionalIdentifierRelationshipTarget\",\"omitted\":\"anOptionalOmittedValue\",\"required\":\"true\",\"value\":\"aSampleNamingrule\"}]}]}],\"MappingPolicies\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"policy\":\"aNamingPolicy\",\"policyNameSpace\":\"a-np-namespace\"}],\"Relationships\":[{\"source\":{\"cardinality\":\"1\",\"classNamespace\":\"simplens\",\"classname\":\"simple/classname\"},\"target\":{\"cardinality\":\"*\",\"classNamespace\":\"complictedns\",\"classname\":\"complicted/classname\",\"role\":\"anOptionalTargetRole\"},\"namespace\":\"a-rel-namespace\",\"guid\":\"anOptionalRelationshipGuid\",\"type\":\"uses\"}]}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		Modify modobj = opset.getModify();
		ConfigurationItem[] cis = modobj.getConfigurationItems();
				
		assertTrue(opset.getOpId().equals("1"));
		assertTrue(opset.getModify().getTimeStamp().equals("2010-07-19T07:15:15Z"));
		assertTrue(cis.length == 6);
	}

	@Test
	public void test_getModelOperationSet_delete()throws Exception{
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"delete\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"Classes\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"parentClassname\":\"app/lotus/IMAPConfig\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\"},{\"classname\":\"complicted/classname\",\"classNamespace\":\"complictedns\",\"parentClassname\":\"net/Fqdn\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\",\"guid\":\"anOPtionalClassGuid\"}],\"Attributes\":[{\"name\":\"anAttribute\",\"className\":\"simple/classname\",\"guid\":\"anOptionalAttributeGuid\",\"classNamespace\":\"simplens\",\"dataType\":\"Boolean\",\"length\":\"anOptionalAttributeLength\",\"namespace\":\"an-att-namespace\",\"description\":\"A new sub attribute\"}],\"NamingRules\":[{\"name\":\"aNamingPolicy\",\"guid\":\"anOptionalNamingPolicyGuid\",\"namespace\":\"a-np-namespace\",\"NamingRule\":[{\"name\":\"aNamingRule\",\"guid\":\"anOptionalNamingRuleGuid\",\"priority\":\"0\",\"namespace\":\"a-nr-namespace\",\"Identifier\":[{\"keyword\":\"Parent\",\"namespace\":\"an-att-namespace1\",\"order\":\"0\",\"required\":\"true\",\"value\":\"parent\"},{\"keyword\":\"anAttribute\",\"namespace\":\"an-att-namespace\",\"order\":\"1\",\"relationship\":\"anOptionalIdentifierRelationship\",\"relationshipSource\":\"anOptionalIdentifierRelationshipSource\",\"relationshipTarget\":\"anOptionalIdentifierRelationshipTarget\",\"omitted\":\"anOptionalOmittedValue\",\"required\":\"true\",\"value\":\"aSampleNamingrule\"}]}]}],\"MappingPolicies\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"policy\":\"aNamingPolicy\",\"policyNameSpace\":\"a-np-namespace\"}],\"Relationships\":[{\"source\":{\"cardinality\":\"1\",\"classNamespace\":\"simplens\",\"classname\":\"simple/classname\"},\"target\":{\"cardinality\":\"*\",\"classNamespace\":\"complictedns\",\"classname\":\"complicted/classname\",\"role\":\"anOptionalTargetRole\"},\"namespace\":\"a-rel-namespace\",\"guid\":\"anOptionalRelationshipGuid\",\"type\":\"uses\"}]}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		Delete delobj = opset.getDelete();
		ConfigurationItem[] cis = delobj.getConfigurationItems();
				
		assertTrue(opset.getOpId().equals("1"));
		assertTrue(delobj.getTimeStamp().equals("2010-07-19T07:15:15Z"));
		assertTrue(cis.length == 6);
		
	}
	
	@Test
	public void test_getModelOperationSet_refresh()throws Exception{
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"refresh\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"Classes\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"parentClassname\":\"app/lotus/IMAPConfig\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\"},{\"classname\":\"complicted/classname\",\"classNamespace\":\"complictedns\",\"parentClassname\":\"net/Fqdn\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\",\"guid\":\"anOPtionalClassGuid\"}],\"Attributes\":[{\"name\":\"anAttribute\",\"className\":\"simple/classname\",\"guid\":\"anOptionalAttributeGuid\",\"classNamespace\":\"simplens\",\"dataType\":\"Boolean\",\"length\":\"anOptionalAttributeLength\",\"namespace\":\"an-att-namespace\",\"description\":\"A new sub attribute\"}],\"NamingRules\":[{\"name\":\"aNamingPolicy\",\"guid\":\"anOptionalNamingPolicyGuid\",\"namespace\":\"a-np-namespace\",\"NamingRule\":[{\"name\":\"aNamingRule\",\"guid\":\"anOptionalNamingRuleGuid\",\"priority\":\"0\",\"namespace\":\"a-nr-namespace\",\"Identifier\":[{\"keyword\":\"Parent\",\"namespace\":\"an-att-namespace1\",\"order\":\"0\",\"required\":\"true\",\"value\":\"parent\"},{\"keyword\":\"anAttribute\",\"namespace\":\"an-att-namespace\",\"order\":\"1\",\"relationship\":\"anOptionalIdentifierRelationship\",\"relationshipSource\":\"anOptionalIdentifierRelationshipSource\",\"relationshipTarget\":\"anOptionalIdentifierRelationshipTarget\",\"omitted\":\"anOptionalOmittedValue\",\"required\":\"true\",\"value\":\"aSampleNamingrule\"}]}]}],\"MappingPolicies\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"policy\":\"aNamingPolicy\",\"policyNameSpace\":\"a-np-namespace\"}],\"Relationships\":[{\"source\":{\"cardinality\":\"1\",\"classNamespace\":\"simplens\",\"classname\":\"simple/classname\"},\"target\":{\"cardinality\":\"*\",\"classNamespace\":\"complictedns\",\"classname\":\"complicted/classname\",\"role\":\"anOptionalTargetRole\"},\"namespace\":\"a-rel-namespace\",\"guid\":\"anOptionalRelationshipGuid\",\"type\":\"uses\"}]}}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		Create createobj = opset.getRefresh().getCreate();
		ConfigurationItem[] cis = createobj.getConfigurationItems();
		
		assertTrue(opset.getOpId().equals("1"));
		assertTrue(opset.getRefresh().getTimeStamp().equals("2010-07-19T07:15:15Z"));
		assertTrue(opset.getRefresh().getCreate().getTimeStamp().equals("2010-07-19T07:15:15Z"));
		assertTrue(cis.length == 6);
	}
	
	/*
	 * This test case is just to hit 
	 * if(operJsonObj.get(key) instanceof OrderedJSONObject) 
	 * in converJsonToModelOperation()
	 */
	@Test
	public void test_getModelOperationSet_create_tc2() throws Exception{
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"Classes\":[{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"parentClassname\":\"app/lotus/IMAPConfig\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\"},{\"classname\":\"complicted/classname\",\"classNamespace\":\"complictedns\",\"parentClassname\":\"net/Fqdn\",\"parentNamespace\":\"ibm-cdm:\",\"description\":\"A new sub class\",\"guid\":\"anOPtionalClassGuid\"}],\"Attributes\":[{\"name\":\"anAttribute\",\"className\":\"simple/classname\",\"guid\":\"anOptionalAttributeGuid\",\"classNamespace\":\"simplens\",\"dataType\":\"Boolean\",\"length\":\"anOptionalAttributeLength\",\"namespace\":\"an-att-namespace\",\"description\":\"A new sub attribute\"}],\"NamingRules\":[{\"name\":\"aNamingPolicy\",\"guid\":\"anOptionalNamingPolicyGuid\",\"namespace\":\"a-np-namespace\",\"NamingRule\":[{\"name\":\"aNamingRule\",\"guid\":\"anOptionalNamingRuleGuid\",\"priority\":\"0\",\"namespace\":\"a-nr-namespace\",\"Identifier\":[{\"keyword\":\"Parent\",\"namespace\":\"an-att-namespace1\",\"order\":\"0\",\"required\":\"true\",\"value\":\"parent\"},{\"keyword\":\"anAttribute\",\"namespace\":\"an-att-namespace\",\"order\":\"1\",\"relationship\":\"anOptionalIdentifierRelationship\",\"relationshipSource\":\"anOptionalIdentifierRelationshipSource\",\"relationshipTarget\":\"anOptionalIdentifierRelationshipTarget\",\"omitted\":\"anOptionalOmittedValue\",\"required\":\"true\",\"value\":\"aSampleNamingrule\"}]}]}],\"MappingPolicies\":{\"classname\":\"simple/classname\",\"classNamespace\":\"simplens\",\"policy\":\"aNamingPolicy\",\"policyNameSpace\":\"a-np-namespace\"},\"Relationships\":[{\"source\":{\"cardinality\":\"1\",\"classNamespace\":\"simplens\",\"classname\":\"simple/classname\"},\"target\":{\"cardinality\":\"*\",\"classNamespace\":\"complictedns\",\"classname\":\"complicted/classname\",\"role\":\"anOptionalTargetRole\"},\"namespace\":\"a-rel-namespace\",\"guid\":\"anOptionalRelationshipGuid\",\"type\":\"uses\"}]}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		OperationSet opset = msgXformer.getOperationSet(jsonmsg);
		
		ConfigurationItem[] cis = opset.getCreate().getConfigurationItems();
		
		assertTrue(opset.getOpId().equals("1"));
		assertTrue(opset.getCreate().getTimeStamp().equals("2010-07-19T07:15:15Z"));
		
		if(cis.length == 6)
		{
			assertTrue(cis[4].getClassNameType().equals("MappingPolicies"));
		}
		
	}
	
	/*
	 * This test case is just to hit opID Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_create_tc3(){
		String jsonmsg = "{\"operationSet\":{\"opid\":null,\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"NamingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true); 
			//e.printStackTrace();
		}
		
	}
	
	/*
	 * This test case is just to hit modify Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_modify_tc4(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"modify\":null}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
		
	}
	
	/*
	 * This test case is just to hit delete Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_delete_tc5(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"delete\":null}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
		
	}
	
	/*
	 * This test case is just to hit refresh Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_refresh_tc6(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"refresh\":null}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
		
	}
	

	/*
	 * This test case is just to hit create Exception inside getOperationSet() for refresh operation
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_refresh_tc7() throws Exception{
		
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"refresh\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"create\":null}}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		//OperationSet opset = msgXformer.getModelOperationSet(jsonmsg);
		
		// when create == null we are not throwing any exception for refresh operation
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		} 
	}
	
	/*
	 * This test case is just to hit create Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_create_tc8(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"create\":null}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
		
	}
	
	/*
	 * This test case is just to hit reference Exception inside getOperationSet()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_getOperationSet_reference_tc9(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"reference\":null}}";
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		
		try {
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
		
	} 
	
	/*
	 * This test case is just to hit exception in convertJsonToRelationShipArray()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_convertJsonToRelationShipArray_tc1(){
		String jsonmsg = "{\"operationSet\":{\"opid\":\"1\",\"reference\":{\"modelObject\":{\"sys.ComputerSystem\":[{\"id\":\"45678\",\"sourceToken\":\"ITM-CS-56789\"}],\"sys.OperatingSystem\":[{\"id\":\"12345\",\"sourceToken\":\"ITM-OS-12345\"}]}},\"create\":{\"timeStamp\":\"2010-07-19T07:15:15Z\",\"relationship\":{\"contains\":[{\"source\":null,\"target\":\"12345\"}]}},}}";
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try { 
			OperationSet opset = msgXformer.getOperationSet(jsonmsg);
			assertTrue(false);			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(true);
		}	
	}
	
	
	/*
	 * This test case is just to hit json parser exception in parseToJSONObject()
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_parseToJSONObject_jpe() {
		String jsonmsg = "{\"operationSet\":\"opid\":\"1\",\"create\":{\"timeStamp\":\"2011-03-07T12:28:18Z\",\"modelObject\":{\"ComputerSystem\":[{\"guid\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"PrimaryMACAddress\":\"001C25740059\",\"SystemBoardUUID\":\"UUID-0003\",\"NamingContext\":{\"source\":\"1234\",\"target\":\"3456\" }}],\"OperatingSystem\":[{\"guid\":\"D2C3F90AC23930E3B3F398012C68A210\",\"ManagedSystemName\":\"Win\",\"FQDN\":\"fqdn\" },{\"guid\":\"D2C3F90AC23930E3B3F398012C68A211\",\"ManagedSystemName\":\"Win2003\",\"FQDN\":\"fqdn\" }]},\"relationship\":{\"contains\":[{\"source\":\"923DE6F8DEF138F8AD0AFE3FF6E91E9B\",\"target\":\"D2C3F90AC23930E3B3F398012C68A210\"}]}}}}}";
		ByteArrayInputStream data = new ByteArrayInputStream(jsonmsg.getBytes());
		
		DISBJSONMessageTransformer msgXformer = new DISBJSONMessageTransformer();
		try {
			OrderedJSONObject obj = (OrderedJSONObject) msgXformer.parseToJSONObject(data);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
			
		}
	}
	
	/*
	 * as now we dont know whether DIS supports modify operation on Relationships, hence commenting out this testcase
	 */
//	@Test
	public void test_getInstanceOperationSet_relationship_modify(){	
	}
	
	/*
	 * as now we dont know whether DIS supports refresh operation on Relationships, hence commenting out this testcase
	 */
//	@Test
	public void test_getInstanceOperationSet_relationship_refresh(){
	}
}
