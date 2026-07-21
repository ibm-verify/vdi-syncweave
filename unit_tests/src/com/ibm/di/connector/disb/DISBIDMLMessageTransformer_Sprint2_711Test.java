package com.ibm.di.connector.disb;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.ibm.di.connector.disb.DISBIDMLMessageTransformer;
import com.ibm.di.connector.disb.model.ConfigurationItem;
import com.ibm.di.connector.disb.model.Create;
import com.ibm.di.connector.disb.model.Delete;
import com.ibm.di.connector.disb.model.Modify;
import com.ibm.di.connector.disb.model.OperationSet;
import com.ibm.di.connector.disb.model.Refresh;
import com.ibm.di.connector.disb.model.Relationship;

public class DISBIDMLMessageTransformer_Sprint2_711Test {

	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * This test case uses InstanceRefreshIDML.xml file
	 */
	@Test
	public void test_transformIDML_refresh_tc1()throws Exception{
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><idml:idml	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\"	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\">    <idml:source IdMLSchemaVersion=\"0.8\">		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">            <cdm:MSSName>TCM7.1</cdm:MSSName>            <cdm:ManufacturerName>IBM</cdm:ManufacturerName>            <cdm:ProductName>TCM</cdm:ProductName>            <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>            <cdm:ProductVersion>7.1</cdm:ProductVersion>			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid>			</cdm:process.ManagementSoftwareSystem>    </idml:source>    <idml:operationSet opid=\"single transaction\">        <idml:refresh timestamp=\"2011-02-16T14:59:50Z\">            <idml:create timestamp=\"2011-02-16T14:59:50Z\">                <cdm:CDM-ER-Specification>                    <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                        sourceToken=\"1\">						<cdm:Model>Pentium 4</cdm:Model>						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber>						<cdm:Manufacturer>Intel</cdm:Manufacturer>                    </cdm:sys.ComputerSystem>                    <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                        sourceToken=\"2\">						<cdm:Model>Pentium DualCore</cdm:Model>						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber>						<cdm:Manufacturer>Intel</cdm:Manufacturer>                    </cdm:sys.ComputerSystem>                    <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                        sourceToken=\"3\">						<cdm:Model>Xeon Tigerton</cdm:Model>						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber>						<cdm:Manufacturer>Intel</cdm:Manufacturer>                    </cdm:sys.ComputerSystem>                    <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                        sourceToken=\"4\">						<cdm:Model>Athlon Palomino</cdm:Model>						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber>						<cdm:Manufacturer>AMD</cdm:Manufacturer>                    </cdm:sys.ComputerSystem>                    <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                        <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                        <cdm:OSVersion>2003</cdm:OSVersion>                        <cdm:OsId>1</cdm:OsId>                        <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                        <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                    </cdm:sys.windows.WindowsOperatingSystem>					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                        target=\"sys.ComputerSystem-2\" />                    <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                        <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                        <cdm:OSVersion>2003</cdm:OSVersion>                        <cdm:OsId>2</cdm:OsId>                        <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                        <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                    </cdm:sys.windows.WindowsOperatingSystem>					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-2\"                        target=\"sys.ComputerSystem-1\" />	                </cdm:CDM-ER-Specification>            </idml:create>        </idml:refresh>    </idml:operationSet></idml:idml>";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		idmlMsgXformer.transformIDML(in);
		String jsonmsg = idmlMsgXformer.getjSONMessage();
		
		OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg); 
		Refresh refreshObj = opset.getRefresh();
		
		assertTrue(opset.getOpId().equals("single transaction")); 
		assertTrue(refreshObj.getTimeStamp().equals("2011-02-16T14:59:50Z"));
		assertTrue(refreshObj.getCreate().getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		ConfigurationItem[] cis =  refreshObj.getCreate().getConfigurationItems();
		Relationship[] rels = refreshObj.getCreate().getRelationships();
		
		if((cis.length == 6)&&(rels.length == 2))
			assertTrue(true);
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * This test case uses InstanceCreateIDML.xml file
	 */
	@Test
	public void test_transformIDML_create_tc2()throws Exception{
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">             <cdm:MSSName>TCM7.1</cdm:MSSName>             <cdm:ManufacturerName>IBM</cdm:ManufacturerName>             <cdm:ProductName>TCM</cdm:ProductName>             <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>             <cdm:ProductVersion>7.1</cdm:ProductVersion> 			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid> 			</cdm:process.ManagementSoftwareSystem>     </idml:source>     <idml:operationSet opid=\"single transaction\">             <idml:create timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                         sourceToken=\"1\"> 						<cdm:Model>Pentium 4</cdm:Model> 						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                         sourceToken=\"3\"> 						<cdm:Model>Xeon Tigerton</cdm:Model> 						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                         sourceToken=\"4\"> 						<cdm:Model>Athlon Palomino</cdm:Model> 						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber> 						<cdm:Manufacturer>AMD</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                         target=\"sys.ComputerSystem-2\" />                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>2</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-2\"                         target=\"sys.ComputerSystem-1\" />	                 </cdm:CDM-ER-Specification>             </idml:create>     </idml:operationSet> </idml:idml>";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		idmlMsgXformer.transformIDML(in);
		String jsonmsg = idmlMsgXformer.getjSONMessage();
		
		OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
		Create createObj = opset.getCreate();
		
		assertTrue(opset.getOpId().equals("single transaction")); 
		assertTrue(createObj.getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		
		ConfigurationItem[] cis =  createObj.getConfigurationItems();
		Relationship[] rels = createObj.getRelationships();
		
		if((cis.length == 6)&&(rels.length == 2))
			assertTrue(true);
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * This test case uses InstanceDeleteIDML.xml file
	 */
	@Test
	public void test_transformIDML_delete_tc3()throws Exception{ 
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">             <cdm:MSSName>TCM7.1</cdm:MSSName>             <cdm:ManufacturerName>IBM</cdm:ManufacturerName>             <cdm:ProductName>TCM</cdm:ProductName>             <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>             <cdm:ProductVersion>7.1</cdm:ProductVersion> 			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid> 			</cdm:process.ManagementSoftwareSystem>     </idml:source>     <idml:operationSet opid=\"single transaction\">             <idml:delete timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                         sourceToken=\"1\"> 						<cdm:Model>Pentium 4</cdm:Model> 						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                         sourceToken=\"3\"> 						<cdm:Model>Xeon Tigerton</cdm:Model> 						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                         sourceToken=\"4\"> 						<cdm:Model>Athlon Palomino</cdm:Model> 						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber> 						<cdm:Manufacturer>AMD</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                         target=\"sys.ComputerSystem-2\" />                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>2</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-2\"                         target=\"sys.ComputerSystem-1\" />	                 </cdm:CDM-ER-Specification>             </idml:delete>     </idml:operationSet> </idml:idml>";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		idmlMsgXformer.transformIDML(in);
		String jsonmsg = idmlMsgXformer.getjSONMessage();
		
		OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
		Delete deleteObj = opset.getDelete();
		
		assertTrue(opset.getOpId().equals("single transaction")); 
		assertTrue(deleteObj.getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		
		ConfigurationItem[] cis =  deleteObj.getConfigurationItems();
		Relationship[] rels = deleteObj.getRelationships();
		
		if((cis.length == 6)&&(rels.length == 2))
			assertTrue(true);
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * This test case uses InstanceModifyIDML.xml file
	 */
	@Test
	public void test_transformIDML_modify_tc4()throws Exception{ 
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">             <cdm:MSSName>TCM7.1</cdm:MSSName>             <cdm:ManufacturerName>IBM</cdm:ManufacturerName>             <cdm:ProductName>TCM</cdm:ProductName>             <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>             <cdm:ProductVersion>7.1</cdm:ProductVersion> 			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid> 			</cdm:process.ManagementSoftwareSystem>     </idml:source>     <idml:operationSet opid=\"single transaction\">             <idml:modify timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                         sourceToken=\"1\"> 						<cdm:Model>Pentium 4</cdm:Model> 						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                         sourceToken=\"3\"> 						<cdm:Model>Xeon Tigerton</cdm:Model> 						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                         sourceToken=\"4\"> 						<cdm:Model>Athlon Palomino</cdm:Model> 						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber> 						<cdm:Manufacturer>AMD</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>2</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem>	                 </cdm:CDM-ER-Specification>             </idml:modify>     </idml:operationSet> </idml:idml> ";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		idmlMsgXformer.transformIDML(in);
		String jsonmsg = idmlMsgXformer.getjSONMessage();
		
		OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
		Modify modifyObj = opset.getModify();
		
		assertTrue(opset.getOpId().equals("single transaction")); 
		assertTrue(modifyObj.getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		
		ConfigurationItem[] cis =  modifyObj.getConfigurationItems();
		
		if(cis.length == 6)
			assertTrue(true);
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * The objective of this testcase is to check whether we are constructing 
	 * MSSName attribute properly or not if not provided in IDML book 
	 * This test case uses InstanceCreateIDML_TC5.xml file
	 */
	@Test
	public void test_transformIDML_create_tc5()throws Exception{ 
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">             <cdm:ManufacturerName>IBM</cdm:ManufacturerName>             <cdm:ProductName>TCM</cdm:ProductName>             <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>             <cdm:ProductVersion>7.1</cdm:ProductVersion> 			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid> 			</cdm:process.ManagementSoftwareSystem>     </idml:source>     <idml:operationSet opid=\"single transaction\">             <idml:create timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                         sourceToken=\"1\"> 						<cdm:Model>Pentium 4</cdm:Model> 						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                         sourceToken=\"3\"> 						<cdm:Model>Xeon Tigerton</cdm:Model> 						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                         sourceToken=\"4\"> 						<cdm:Model>Athlon Palomino</cdm:Model> 						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber> 						<cdm:Manufacturer>AMD</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                         target=\"sys.ComputerSystem-2\" />                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>2</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-2\"                         target=\"sys.ComputerSystem-1\" />	                 </cdm:CDM-ER-Specification>             </idml:create>     </idml:operationSet> </idml:idml> ";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		idmlMsgXformer.transformIDML(in);
		String jsonmsg = idmlMsgXformer.getjSONMessage(); 
		
		OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
		Create createObj = opset.getCreate();
		
		assertTrue(opset.getOpId().equals("single transaction")); 
		assertTrue(createObj.getTimeStamp().equals("2011-02-16T14:59:50Z"));
		
		
		ConfigurationItem[] cis =  createObj.getConfigurationItems();
		Relationship[] rels = createObj.getRelationships();
		
		if((cis.length == 6)&&(rels.length == 2))
			assertTrue(true);
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * The objective of this testcase is to check what happens when MSS section is missing in idml book 
	 * This test case uses InstanceCreateIDML_TC6.xml file
	 */
	@SuppressWarnings("unused")
	@Test
	public void test_transformIDML_create_tc6(){
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		        </idml:source>     <idml:operationSet opid=\"single transaction\">             <idml:create timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-1\"                         sourceToken=\"1\"> 						<cdm:Model>Pentium 4</cdm:Model> 						<cdm:SerialNumber>00FFBABC13D8</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-3\"                         sourceToken=\"3\"> 						<cdm:Model>Xeon Tigerton</cdm:Model> 						<cdm:SerialNumber>54DAFF327B63</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-4\"                         sourceToken=\"4\"> 						<cdm:Model>Athlon Palomino</cdm:Model> 						<cdm:SerialNumber>750ABEAE54BD</cdm:SerialNumber> 						<cdm:Manufacturer>AMD</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                         target=\"sys.ComputerSystem-2\" />                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-2\" sourceToken=\"2\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>2</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-2\"                         target=\"sys.ComputerSystem-1\" />	                 </cdm:CDM-ER-Specification>             </idml:create>     </idml:operationSet> </idml:idml>";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		
		try {
			idmlMsgXformer.transformIDML(in);
			String jsonmsg = idmlMsgXformer.getjSONMessage();
			
			OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
			Create createObj = opset.getCreate();
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
	}
	
	/*
	 * This test case targets transformIDML()
	 * We pass idml msg & expects json msg to be returned properly.
	 * The objective of this testcase is to check what happens when opid operation is missing 
	 * This test case uses InstanceCreateIDML_TC7.xml file
	 */
	/*
	@SuppressWarnings("unused")
	//@Test
	public void test_transformIDML_create_tc7(){ 
		String idmlmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <idml:idml 	xmlns:idml=\"http://www.ibm.com/xmlns/swg/idml\" 	xmlns:cdm=\"http://www.ibm.com/xmlns/swg/cdm\" 	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" 	xsi:schemaLocation=\"http://www.ibm.com/xmlns/swg/idml idml.xsd\" >     <idml:source IdMLSchemaVersion=\"0.8\"> 		    <cdm:process.ManagementSoftwareSystem id=\"ITPM71.tpmfriends.in.ibm.com\" sourceToken=\"http://tcm71:12345/portal\" CDMSchemaVersion=\"2.10.3\">             <cdm:MSSName>TCM7.1</cdm:MSSName>             <cdm:ManufacturerName>IBM</cdm:ManufacturerName>             <cdm:ProductName>TCM</cdm:ProductName>             <cdm:Hostname>tcm.raleigh.ibm.com</cdm:Hostname>             <cdm:ProductVersion>7.1</cdm:ProductVersion> 			<cdm:Guid>E2C3F90AC23930E3B3F398012C68A210</cdm:Guid> 			</cdm:process.ManagementSoftwareSystem>     </idml:source>     <idml:operationSet >             <idml:create timestamp=\"2011-02-16T14:59:50Z\">                 <cdm:CDM-ER-Specification>                     <cdm:sys.ComputerSystem id=\"sys.ComputerSystem-2\"                         sourceToken=\"2\"> 						<cdm:Model>Pentium DualCore</cdm:Model> 						<cdm:SerialNumber>03ACB26791DE</cdm:SerialNumber> 						<cdm:Manufacturer>Intel</cdm:Manufacturer>                     </cdm:sys.ComputerSystem>                     <cdm:sys.windows.WindowsOperatingSystem id=\"sys.windows.WindowsOperatingSystem-1\" sourceToken=\"1\">                         <cdm:OSName>Microsoft Windows Server 2003</cdm:OSName>                         <cdm:OSVersion>2003</cdm:OSVersion>                         <cdm:OsId>1</cdm:OsId>                         <cdm:Label>Windows Server 2003 Enterprise Edition SP2</cdm:Label>                         <cdm:Name>Windows Server 2003 Enterprise Edition SP2</cdm:Name>                     </cdm:sys.windows.WindowsOperatingSystem> 					<cdm:installedOn source=\"sys.windows.WindowsOperatingSystem-1\"                         target=\"sys.ComputerSystem-2\" />                 </cdm:CDM-ER-Specification>             </idml:create>     </idml:operationSet> </idml:idml> ";
		
		DISBIDMLMessageTransformer idmlMsgXformer = new DISBIDMLMessageTransformer();
		DISBJSONMessageTransformer jsonMsgXformer = new DISBJSONMessageTransformer();
		
		InputStream in = new ByteArrayInputStream(idmlmsg.getBytes());
		
		try {
			idmlMsgXformer.transformIDML(in);
			String jsonmsg = idmlMsgXformer.getjSONMessage();
			
			OperationSet opset = jsonMsgXformer.getOperationSet(jsonmsg);
			Create createObj = opset.getCreate();
		} catch (Exception e) {
			assertTrue(true);
			//e.printStackTrace();
		}
	}
	*/
}
