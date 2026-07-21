
package com.ibm.di.cvt71.connectors;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;

@CVTComponent(name="conn_PES")
public class PF1_SS_Improvement_Tests_CVT {
	
	private static TDIServer tdi = null;
	static File tdiInstallDir = null;
	static File tdiSolutionDir = null;
	static int tdiRMIPort;
	
	public static final String TDI_CONFIG_FOLDER = "/TestCases/conn_SystemStore/";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		tdiRMIPort = PortProbe.getAvailablePort();

		// configure TDI server 
		tdi.setProperty("api.on", "true");
		tdi.setProperty("api.remote.on", "true");
		tdi.setProperty("api.remote.naming.port", "1099");
		tdi.setProperty("api.remote.ssl.on", "false");
		
		tdiInstallDir = tdi.getInstallDir();
		tdiSolutionDir = tdi.getSolutionDir();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tdi = null;
	}

	/**
	 * Verify that System Store Connector works without setting JDBC driver
	 * parameter.
	 * 
	 */
	@CVTTest(name = "CVT_PF1_SS_Improvement_Test_01")
	@Test
	public void test_SystemStoreConnector_Works_Without_Setting_JDBC_Driver_Parameter() throws Exception {
		String tcName = "CVT_PF1_SS_Improvement_Test_01";
		String[] args1 = {
			"-c",
			"\"" + tdiInstallDir + TDI_CONFIG_FOLDER + tcName + "/"
					+ tcName + ".xml\"", "-r",
			"CVT_PF1_SystemStoreImprovement_71_01_AL1" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "AssemblyLine AssemblyLines/CVT_PF1_SystemStoreImprovement_71_01_AL1 terminated successfully.");
	}

	/**
	 * Verify that the system Store Connector accesses the Entries in the System store with empty 'Create table statement' parameter
	 * 
	 */
	@CVTTest(name = "CVT_PF1_SS_Improvement_Test_02")
	@Test
	public void test_SystemStoreConnector_Works_Setting_JDBC_Driver_Parameter() throws Exception {
		String tcName = "CVT_PF1_SS_Improvement_Test_02";
		String[] args1 = {
				"-c",
				"\"" + tdiInstallDir + TDI_CONFIG_FOLDER + tcName + "/"
						+ tcName + ".xml\"", "-r",
				"CVT_PF1_SystemStoreImprovement_71_02_AL1" };
		
		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir+"/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile,"AssemblyLine AssemblyLines/CVT_PF1_SystemStoreImprovement_71_02_AL1 terminated successfully.");
	}

	
	/**
	 * Verify if system store connector works fine with the solid DB as system
	 * store.
	 * 
	 */
	@CVTTest(name = "CVT_PF1_SS_Improvement_Test_03")
	@Test
	public void test_SystemStoreConnector_Accesses_SysStore_With_Empty_CreateTableStatement_parameter() throws Exception {
		String tcName = "CVT_PF1_SS_Improvement_Test_03";
		String[] args1 = {
				"-c",
				"\"" + tdiInstallDir + TDI_CONFIG_FOLDER + tcName + "/"
						+ tcName + ".xml\"", "-r",
				"CVT_PF1_SystemStoreImprovement_71_03_AL1" };
		
		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "AssemblyLine AssemblyLines/CVT_PF1_SystemStoreImprovement_71_03_AL1 terminated successfully.");
	}

}
