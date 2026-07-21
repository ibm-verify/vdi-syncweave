
package com.ibm.di.cvt71.store;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.utils.TestCaseResourceHandler;
import com.ibm.di.test.utils.TestUtils;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;

@CVTComponent(name = "DeltaStore")
public class PF1_DF_Improvement_Tests_CVT {
	
	public static final String TDI_SYS_STORE = "TDISysStore";
	private static TDIServer tdi = null;
	private static TestCaseResourceHandler tcrh = null;
	static File tdiInstallDir = null;
	static File tdiSolutionDir = null;
	static int tdiRMIPort;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		tdiRMIPort = PortProbe.getAvailablePort();

		// configure TDI server
		tdi.setProperty("api.on", "true");
		tdi.setProperty("api.remote.on", "true");
		tdi.setProperty("api.remote.naming.port", "" + tdiRMIPort);
		tdi.setProperty("api.remote.ssl.on", "false");
		
		// configure TDI server to start Derby in Embedded Mode
		tdi.setProperty("com.ibm.di.store.database", "TDISysStore");
		tdi.setProperty("com.ibm.di.store.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		tdi.setProperty("com.ibm.di.store.jdbc.urlprefix", "jdbc:derby:");

		tdiInstallDir = tdi.getInstallDir();
		tdiSolutionDir = tdi.getSolutionDir();

		tcrh = new TestCaseResourceHandler(tdiInstallDir, tdiSolutionDir, PF1_DF_Improvement_Tests_CVT.class);
		tcrh.initResource();
	}

	@Before
	public void cleanUpDeltaStore() throws Exception {
		File dsDir = new File(tdiSolutionDir, TDI_SYS_STORE);
		TestUtils.deleteDir(dsDir);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tdi.close();
		tdi = null;
		tcrh.restoreResources();
	}

	@CVTTest(name = "CVT_PF1_DF_Improvement_Test_01")
	@Test
	public void test_ReadDeleted_Reads_Deleted_Entries_Using_DeltaFC() throws Exception {
		String tcName = "CVT_PF1_DF_Improvement_Test_01";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_01_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_01_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Operation: delete");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	@CVTTest(name = "CVT_PF1_DF_Improvement_Test_02")
	@Test
	public void test_RemoveDeleted_Removes_Deleted_Entries_Using_DeltaFC() throws Exception {
		String tcName = "CVT_PF1_DF_Improvement_Test_02";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_02_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_02_AL2" };

		String[] args3 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_02_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:31, Add:10, Modify:10, Delete:29, Skip:11, Nochange:11.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args3);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:31.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	@CVTTest(name = "CVT_PF1_DF_Improvement_Test_03")
	@Test
	public void test_ReturnUnchanged_Returns_Unchanged_Entries_Using_DeltaFC() throws Exception {
		String tcName = "CVT_PF1_DF_Improvement_Test_03";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_03_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_03_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:31, Add:10, Modify:10, Nochange:11.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}
}
