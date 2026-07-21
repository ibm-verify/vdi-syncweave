package com.ibm.di.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;

/**
 * This class tests various aspects of the AssemblyLine Component.
 *
 * Version: %I%, %G%
 * 
 */
public class AssemblyLineComponentTest {

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

		@Override
		public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
			System.out.println("Delete entry: " + entry.toString());
		}

		@Override
		public Entry findEntry(SearchCriteria search) throws Exception {
			Entry e = new Entry();
			e.setAttribute("counter", "9999");
			e.setAttribute("key", "value");
			return e;
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
	 * Creates an AssemblyLine Configuration with a connector in iterator mode.
	 * 
	 * The iterator mode is also provided with a Link Criteria for the tests that require this.
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
			
			// -- Add test connector as addonly
			ConnectorConfigImpl cc = new ConnectorConfigImpl();
			cc.init();
			cc.setMode(ConnectorConfigImpl.ITERATOR_MODE);
			cc.setName("IteratorConnector");
			LinkCriteriaItem crit = cc.getLinkCriteria().newCriteria("1");
			crit.setAttribute("key");
			crit.setOper(LinkCriteriaItem.LC_EXACT);
			crit.setValue("$key");
			alc.getEntryFeedComponents().addConfig(cc);
			
			mc.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);
			
			return alc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method creates an AssemblyLine and provides a TestConnector runtime connector. In addition it is provided
	 * with an instance of the TestRS class so it has a proper parent RS object.
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
			
			al = new AssemblyLine(new TestRS(), alc.getShortName(), tcb, null, alc);
			al.executeInitializeAL();
			return al;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	@Test
	public void test_Last_Entry_Read() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG);
		assertNotNull(al.getConnector("IteratorConnector").getLastReadEntry());
	}

	@Test
	public void test_Last_Entry() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG);
		assertNotNull(al.getConnector("IteratorConnector").getLastEntry());
	}
	
	@Test
	public void test_End_Of_Data_Property() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG);
		assertEquals("true", al.getConnector("IteratorConnector").get(AssemblyLineComponent.END_OF_DATA));
	}
	
	@Test
	public void test_Delta_Mode_Fail() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG);
		AssemblyLineComponent conn = al.getConnector("IteratorConnector");
		Exception error = null;
		try {
			conn.delta(new Entry());
		} catch (Exception e) {
			error = e;
		}
		assertNotNull(error);
	}

	@Test
	public void test_Delta_Mode_Success() {
		AssemblyLine al = createTestAssemblyLine();
		al.executeMainLoop(ALState.MS_EPILOG);
		AssemblyLineComponent conn = al.getConnector("IteratorConnector");
		Exception error = null;
		try {
			Entry e = new Entry();
			e.setOp(Entry.OP_DEL);
			e.setAttribute("key", "value");
			conn.delta(e);
		} catch (Exception e) {
			e.printStackTrace();
			error = e;
		}
		assertNull(error);
	}
}
