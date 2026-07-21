package com.ibm.di.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;

public class AssemblyLineTest {
	
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private static String[] alHooks = {
			InternalSchema.AL_PROLOG_INIT,
			InternalSchema.AL_PROLOG,
			InternalSchema.AL_STARTCYCLE,
			InternalSchema.AL_EPILOG,
			InternalSchema.AL_EPILOG2,
		};
	
	/**
	 * This inner class is used to provide the AL with a runtime connector.
	 * 
	 * The tests does not require any specific connector so we provide
	 * a simple dummy connector that implements the required methods for the
	 * assemblyline component to function properly.
	 *
	 */
	private class TestConnector extends Connector {
		int counter = 1;
		public String getVersion() {
			return "JUnit Test Connector 1.0";
		}

		@Override
		public Entry getNextEntry() throws Exception {
			if(counter > 10)
				return null;
			Entry e = new Entry();
			e.setAttribute("Counter", "" + counter++);
			return e;
		}

		@Override
		public void putEntry(Entry entry) throws Exception {
//			System.out.println("PutEntry: " + (entry == null ? "[null]" : entry.toDeltaString()));
		}
		
	}
	
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
	 * Creates an AssemblyLine Configuration with a connector in iterator mode and a connector in Addonly mode. The assemblyline
	 * hooks are also created which sets a corresponding script variable to its own name (e.g. onsuccess = "onsuccess").
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
			
			// -- Add test connector as iterator
			ConnectorConfigImpl cc = new ConnectorConfigImpl();
			cc.init();
			cc.setMode(ConnectorConfigImpl.ITERATOR_MODE);
			cc.setName("IteratorConnector");
			alc.getEntryFeedComponents().addConfig(cc);
			
			// -- Add test connector as addonly
			cc = new ConnectorConfigImpl();
			cc.init();
			cc.setMode(ConnectorConfigImpl.ADDONLY_MODE);
			cc.setName("AddOnlyConnector");
			alc.getDataFlowComponents().addConfig(cc);
			
			// -- Make every hook set an attribute in the work entry
			for(String str : alHooks) {
				HookConfig hook = alc.getHook(str);
				hook.setScript(str + "= \"" + str + "\"\n");
				hook.setEnabled(true);
			}
				
			mc.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);
			
			return alc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method creates an AssemblyLine and provides a TestConnector runtime connector for the Iterator and Addonly connectors.
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
			
			TestConnector tc = new TestConnector();
			tc.setConfiguration(alc.getConnectorByName("IteratorConnector"));
			tcb.setRuntimeConnector("IteratorConnector", new TestConnector());
			
			tc = new TestConnector();
			tc.setConfiguration(alc.getConnectorByName("AddOnlyConnector"));
			tcb.setRuntimeConnector("AddOnlyConnector", new TestConnector());
			
			al = new AssemblyLine(new TestRS(), alc.getShortName(), tcb, null, alc);
			al.executeInitializeAL();
			return al;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Test
	public void test_AssemblyLine_Set_And_Get_Work() {
		AssemblyLine al = createTestAssemblyLine();
		Entry e = new Entry();
		al.setWork(e);
		assertEquals(e, al.getWork());
	}

	@Test
	public void test_AssemblyLine_Get_And_Set_Param() {
		AssemblyLine al = createTestAssemblyLine();
		al.setParam("name", "some value");
		assertEquals("some value", al.getParam("name"));
	}

	@Test
	public void testGetStats() {
		AssemblyLine al = createTestAssemblyLine();
		assertNotNull(al.getStats());
	}
	
	@Test
	public void testCheckScriptEngineNotNull() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_BEGINITER);
		assertNotNull(al.getScriptEngine());
	}

	@Test
	public void testGetConnector() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_BEGINITER);
		assertNotNull(al.getConnector("IteratorConnector"));
	}

	@Test
	public void testGetConnectorIndex() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_BEGINITER);
		int index = -1;
		try {
			index = al.getConnectorIndex("AddOnlyConnector");
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(1, index);
	}
	
	/**
	 * Verifies that the AL hooks are called by checking the script variables for each hook.
	 * The AL hooks set a s var to its own name in as in: startcycle = "startcycle".
	 * 
	 */
	@Test
	public void testCheck_AssemblyLine_Hooks() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG2);
		for(String str : alHooks) {
			Object val = null;
			try {
				val = al.getScriptEngine().eval(str);
			} catch (Exception e) {
				val = e.toString();
			}
			assertEquals(str, val);
		}
	}

	/**
	 * Verifies connectors parameters set via the TCB.
	 */
	@Test
	public void test_TCB_Set_Connector_Parameter() {
		AssemblyLineConfig alc = createTestAssemblyLineConfig();
		String value = "This value provided through the TCB";
		String retval = "TCB set parameter failed";
		String param = "tcb.parameter";
		try {
			TaskCallBlock tcb = new TaskCallBlock();
			tcb.setRunMode(AssemblyLine.RUNMODE_NODEBUG);
			
			TestConnector tc = new TestConnector();
			tc.setConfiguration(alc.getConnectorByName("IteratorConnector"));
			tcb.setRuntimeConnector("IteratorConnector", tc);
			
			tc = new TestConnector();
			tc.setConfiguration(alc.getConnectorByName("AddOnlyConnector"));
			tcb.setRuntimeConnector("AddOnlyConnector", tc);
			tcb.setConnectorParameter("IteratorConnector", param, value);
			
			AssemblyLine al = new AssemblyLine(new TestRS(), alc.getShortName(), tcb, null, alc);
			al.executeInitializeAL();
			al.executeMainLoop(ALState.MS_EPILOG);
			retval = (String) al.getConnector("IteratorConnector").getConnectorParam(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(value, retval);
	}
	
}
