
package com.ibm.di.cvt71.fc;

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

@CVTComponent(name = PF1_FC_Improvement_Tests_CVT.COMPONENT_NAME)
public class PF1_FC_Improvement_Tests_CVT {
	public static final String COMPONENT_NAME = "DeltaFC";
	
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
		tdi.setProperty("com.ibm.di.store.database", TDI_SYS_STORE);
		tdi.setProperty("com.ibm.di.store.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		tdi.setProperty("com.ibm.di.store.jdbc.urlprefix", "jdbc:derby:");

		tdiInstallDir = tdi.getInstallDir();
		tdiSolutionDir = tdi.getSolutionDir();
		tcrh = new TestCaseResourceHandler(tdiInstallDir, tdiSolutionDir, COMPONENT_NAME);
		tcrh.initResource();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tdi.close();
		tdi = null;
		tcrh.restoreResources();
	}

	@Before
	public void cleanUpDeltaStore() throws Exception {
		File dsDir = new File(tdiSolutionDir, TDI_SYS_STORE);
		TestUtils.deleteDir(dsDir);
	}

	/**
	 * Verify that row locking manages concurrent access when multiple
	 * AssemblyLines want to use the same Delta Store to process data at the
	 * same time with "Read Committed"
	 * 
	 */
	@Test
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_01")
	public void test_Concurrency_Works_With_Row_Locking_ReadCommitted() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_01";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_01_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r",
				"CVT_PF1_Improvement_71_01_AL2,CVT_PF1_Improvement_71_01_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:5, Add:5");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
		TestUtils.checkFileNotContains(logFile, "SQLException");
	}

	/**
	 * Verify that row locking manages concurrent access when multiple
	 * AssemblyLines want to use the same Delta Store to process data at the
	 * same time with "Repeatable Read"
	 */
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_02")
	@Test
	public void test_Concurrency_Works_With_Row_Locking_RepeatableRead() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_02";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_02_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r",
				"CVT_PF1_Improvement_71_02_AL2,CVT_PF1_Improvement_71_02_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:5, Add:5");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
		TestUtils.checkFileNotContains(logFile, "SQLException");
	}

	/**
	 * Verify that row locking manages concurrent access when multiple
	 * AssemblyLines want to use the same Delta Store to process data at the
	 * same time with "Serializable"
	 */
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_03")
	@Test
	public void test_Concurrency_Works_With_Row_Locking_Serializable() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_03";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_03_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r",
				"CVT_PF1_Improvement_71_03_AL2,CVT_PF1_Improvement_71_03_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:5, Add:5");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
		TestUtils.checkFileNotContains(logFile, "SQLException");
	}

	/**
	 * Verify that row locking manages concurrent access when multiple
	 * AssemblyLines use the same Delta Store with "Serializable" and
	 * "On Connector Close"
	 */
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_04")
	//@Test This test cannot verify the objective reliably, and sometimes fails
	public void test_Concurrency_Works_With_Row_Locking_Serializable_and_OnConnectorClose() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_04";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_04_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r",
				"CVT_PF1_Improvement_71_04_AL2,CVT_PF1_Improvement_71_04_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:5, Add:5");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
		TestUtils.checkFileNotContains(logFile, "SQLException");
	}

	/**
	 * Verify that "Read Deleted" reads deleted entries with enabled Delta
	 */
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_08")
	@Test
	public void test_ReadDeleted_Reads_Deleted_Entries() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_08";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_08_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_08_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Operation: delete");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	/**
	 * Verify that "Remove Deleted" removes the deleted Entries when enabled
	 * Delta
	 * 
	 */
	@CVTTest(name = "CVT_PF1_FC_Improvement_Test_09")
	@Test
	public void test_RemoveDeleted_Removes_Deleted_Entries() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_09";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_09_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_09_AL2" };

		String[] args3 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_09_AL3" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:61, Add:10, Modify:10, Delete:29, CallReply:61, Nochange:11.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args3);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "AssemblyLine AssemblyLines/CVT_PF1_Improvement_71_09_AL3 terminated successfully.");
		TestUtils.checkFileNotContains(logFile, "Operation: delete");
	}

	/**
	 * Verify that "Return Unchanged" returns the unchanged Entries.
	 * 
	 */
	@CVTTest(name = "CVT_FN-CVT_PF1_FC_Improvement_Test_10")
	@Test
	public void test_ReturnUnchanged_Returns_Unchanged_Entries() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_10";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_10_AL1" };

		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_10_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "[Iterate_With_ReturnUnchanged] Get:31");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	/**
	 * Verify that when "Ignore changes for the following attributes" parameter
	 * is set all specified Attributes are ignored.
	 * 
	 */
	@CVTTest(name = "CVT_FN-CVT_PF1_FC_Improvement_Test_11")
	@Test
	public void test_IgnoreChangesForTheFollowingAttributes_Parameter_Ignores_All_Specified_Attributes() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_11";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_11_AL1" };
		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_11_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:50, CallReply:50, Nochange:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	/**
	 * Verify that when "Detect changes for the following attributes" parameter
	 * is set all specified Attributes are considered.
	 * 
	 */
	@CVTTest(name = "CVT_FN-CVT_PF1_FC_Improvement_Test_12")
	@Test
	public void test_DetectChangesForTheFollowingAttributes_Parameter_Considers_All_Specified_Attributes() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_12";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_12_AL1" };
		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_12_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:50, CallReply:50, Nochange:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}

	/**
	 * Verify that when "Ignore changes for the following attributes" parameter
	 * is set all specified Attributes are ignored.
	 * 
	 */
	@CVTTest(name = "CVT_FN-CVT_PF1_FC_Improvement_Test_13")
	@Test
	public void test_IgnoreChangesForTheFollowingAttributes_Parameter_Ignores_Specified_Attributes() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_13";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_13_AL1" };
		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_13_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:50, Modify:48,");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

	}

	/**
	 * Verify that when "Detect changes for the following attributes" parameter
	 * is set all specified Attributes are considered.
	 */
	@CVTTest(name = "CVT_FN-CVT_PF1_FC_Improvement_Test_14")
	@Test
	public void test_DetectChangesForTheFollowingAttributes_Parameter_Considers_Specified_Attributes() throws Exception {
		String tcName = "CVT_PF1_FC_Improvement_Test_14";

		String[] args1 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_14_AL1" };
		String[] args2 = { "-c", tcrh.getConfigurationXML(tcName), "-r", "CVT_PF1_Improvement_71_14_AL2" };

		tdi.startServer(args1);
		tdi.waitFor();
		File logFile = new File(tdiSolutionDir + "/logs/ibmdi.log");
		TestUtils.checkFileContains(logFile, "Total: Get:50, Add:50, CallReply:50.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");

		tdi.startServer(args2);
		tdi.waitFor();
		TestUtils.checkFileContains(logFile, "Total: Get:50, Modify:49, CallReply:50, Nochange:1.");
		TestUtils.checkFileContains(logFile, "Terminated successfully (0 errors).");
	}
}
