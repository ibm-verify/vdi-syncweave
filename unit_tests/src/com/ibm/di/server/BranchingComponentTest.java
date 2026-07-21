package com.ibm.di.server;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.ibm.di.config.base.BranchConditionImpl;
import com.ibm.di.config.base.BranchingConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;

/**
 * This class tests various aspects of the Branching Component.
 *
 * Version: %I%, %G%
 * 
 */
public class BranchingComponentTest {

	/**
	 * This is an instance of the TDI Server that overrides some methods
	 * to avoid the operational dependencies of the server to be required. 
	 *
	 */
	private class TestRS extends RS {
		private Log log = new Log("NOOP Log");
		@Override
		public Log getLog() {
			return log;
		}
		@Override
		public void logerror(String msg, Throwable error) {
			System.out.println(msg);
			if(error != null)
				error.printStackTrace();
		}
		@Override
		public void logerror(String msg) {
			System.out.println(msg);
		}
		@Override
		public void logmsg(String level, String msg) {
			System.out.println(level + ": " + msg);
		}
		@Override
		public void logmsg(String msg) {
			System.out.println(msg);
		}
	}
	
	/**
	 * Creates an AssemblyLine Configuration with no contents. The auto-map all attributes is set
	 * to avoid having an attribute map required.
	 *  
	 */
	private static AssemblyLineConfig createTestAssemblyLineConfig() {
		try {
			// -- No jlog
			System.getProperties().remove("jlog.configuration");
			
			// -- create in memory config file
			MetamergeConfigXML mc = new MetamergeConfigXML();
			mc.initializeConfig();
			
			// -- create assemblyline
			AssemblyLineConfig alc = (AssemblyLineConfig) mc.newInstanceOf(MetamergeConfig.ASSEMBLYLINE_FOLDER);
			alc.setName("TestAssemblyLine");
			alc.getSettings().setBooleanParameter("automapattributes", true);
			
			mc.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);
			
			return alc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method creates an AssemblyLine and sets the run mode to RUNMODE_NODEBUG to avoid certain parts of the
	 * assemblyline to execute. 
	 * 
	 * In addition it is provided with an instance of the TestRS class so it has a proper parent RS object.
	 * 
	 */
	private AssemblyLine createTestAssemblyLine() {
		AssemblyLineConfig alc = createTestAssemblyLineConfig();
		AssemblyLine al;
		try {
			TaskCallBlock tcb = new TaskCallBlock();
			tcb.setRunMode(AssemblyLine.RUNMODE_NODEBUG);
			al = new AssemblyLine(new TestRS(), alc.getShortName(), tcb, null, alc);
			al.executeInitializeAL();
			return al;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method creates a BranchingComponent with the provided parameters as its only conditional statement. The branch
	 * component is provided an instance of an AssemblyLine so we can execute the branch component methods.
	 * 
	 * @param leftHand
	 * @param oper
	 * @param rightHand
	 * @param caseSensitive
	 * @return
	 */
	private BranchingComponent createBranchingComponent(String leftHand, String oper, String rightHand, boolean caseSensitive) {
		BranchingConfigImpl bcc = new BranchingConfigImpl();
		bcc.init();
		bcc.getConditions().addConfig(createBranchCondition(leftHand, oper, rightHand));
		AssemblyLine al = createTestAssemblyLine();
		
		try {
			return new BranchingComponent(al, bcc.getShortName(), bcc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method creates a BranchCondition object with the provided parameters.
	 * 
	 * @param leftHand
	 * @param oper
	 * @param rightHand
	 * @return
	 */
	private BaseConfiguration createBranchCondition(String leftHand, String oper, String rightHand) {
		BranchConditionImpl bci = new BranchConditionImpl();
		try {
			bci.init();
		} catch (Exception e) {
		}
		bci.setLeftHand(leftHand);
		bci.setOperator(oper);
		bci.setRightHand(rightHand);
		return bci;
	}
	
	/**
	 * This method creates a work entry with a few select attributes. This entry is used in the various
	 * branching component tests.
	 * 
	 * @return
	 */
	private Entry getWork() {
		Entry work = new Entry();
		work.setAttribute("cn", "john doe");
		work.setAttribute("number", "8");
		work.newAttribute("empty");
		return work;
	}
	
	/**
	 * This method tests a branch condition. A new branch component is created with a dummy assemblyline and the parameters
	 * provided is used to create the branch condition being tested. The result of the branch test is returned (true, false).
	 * 
	 * @param leftHand
	 * @param oper
	 * @param rightHand
	 * @param caseSensitive
	 * @return
	 */
	private String testCondition(String leftHand, String oper, String rightHand, boolean caseSensitive) {
		BranchingComponent bc = createBranchingComponent(leftHand, oper, rightHand, caseSensitive);
		try {
			return ""+bc.willExecute(getWork());
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}
	
	@Test
	public void test_BranchingComponent_Contains_Condition() {
		String result = testCondition("cn", BranchCondition.BRANCH_CONTAINS, "hn do", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_Equals_Condition() {
		String result = testCondition("cn", BranchCondition.BRANCH_EQUALS, "john doe", false);
		assertEquals("true", result);
	}

	@Test
	public void test_BranchingComponent_EndsWith_Condition() {
		String result = testCondition("cn", BranchCondition.BRANCH_ENDS_WITH, "doe", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_StartsWith_Condition() {
		String result = testCondition("cn", BranchCondition.BRANCH_STARTS_WITH, "john", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_HasValue_Condition() {
		String result = testCondition("cn", BranchCondition.BRANCH_HAS_VALUE, "", false);
		assertEquals("true", result);
		
		result = testCondition("unknown", BranchCondition.BRANCH_HAS_VALUE, "", false);
		assertEquals("false", result);
		
		result = testCondition("empty", BranchCondition.BRANCH_HAS_VALUE, "", false);
		assertEquals("false", result);
	}

	@Test
	public void test_BranchingComponent_LessThan_Condition() {
		String result = testCondition("number", BranchCondition.BRANCH_LT, "9", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_LessThanEquals_Condition() {
		String result = testCondition("number", BranchCondition.BRANCH_LTE, "8", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_GreaterThanEquals_Condition() {
		String result = testCondition("number", BranchCondition.BRANCH_GTE, "8", false);
		assertEquals("true", result);
	}
	
	@Test
	public void test_BranchingComponent_Exists_Condition() {
		String result = testCondition("number", BranchCondition.BRANCH_EXISTS, "", false);
		assertEquals("true", result);
		result = testCondition("unknown", BranchCondition.BRANCH_EXISTS, "", false);
		assertEquals("false", result);
	}
	
}
