package com.ibm.di.cvt71.api.remote;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.utils.ConfigUtils;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
@CVTComponent(name = "serverapi")
public class ConfigInstanceCVT {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static TDIServer tdi = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		tdi.setProperty("api.rest.on", "false");

		tdi.setProperty("api.remote.naming.port", "" + PortProbe.getAvailablePort());
		tdi.startServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tdi.close();
		tdi = null;
	}

	/**
	 * Verify that the TP Server does not start when api.rest.on=false.
	 */
	@CVTTest(name = "Undocumented_TC_01")
	@Test
	public void test_Test_Config_Instance_Stays_Alive_After_All_ALs_Stop_Executing() throws Exception {
		Session s = tdi.getServerAPISession();

		MetamergeConfig cfg = createEmptyAlConfig(s);

		ConfigInstance ci = s.startTempConfigInstance(ConfigUtils.serializeConfig(cfg), true, "runName", null);
		ci.startAssemblyLine("TestAssemblyLine", true);

		Thread.sleep(3000);

		ConfigInstance actCi = s.getConfigInstance("runName");
		assertThat(actCi, is(notNullValue()));
		assertThat(actCi.getConfigId(), is(ci.getConfigId()));
	}

	/**
	 * Verify that the TP Server does not start when api.rest.on=false.
	 */
	@CVTTest(name = "Undocumented_TC_02")
	@Test
	public void test_Test_Config_Instance_Could_Be_Stopped_And_Then_Started_Right_Away() throws Exception {
		Session s = tdi.getServerAPISession();
		MetamergeConfig mc = createEmptyAlConfig(s);

		String cfg = ConfigUtils.serializeConfig(mc);

		ConfigInstance ci1 = s.startTempConfigInstance(cfg, true, "runName", null);
		ci1.startAssemblyLine("TestAssemblyLine", true);
		ci1.stop(true);
		
		// this should 
		ConfigInstance ci2 = s.startTempConfigInstance(cfg, true, "runName", null);
		ci2.startAssemblyLine("TestAssemblyLine", true);
	}

	private MetamergeConfig createEmptyAlConfig(Session s) throws Exception {
		// -- create in memory config file
		MetamergeConfigXML mc = new MetamergeConfigXML();
		mc.initializeConfig();

		AssemblyLineConfig alc = (AssemblyLineConfig) mc.newInstanceOf(MetamergeConfig.ASSEMBLYLINE_FOLDER);
		alc.setName("TestAssemblyLine");
		mc.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);

		return mc;
	}
}
