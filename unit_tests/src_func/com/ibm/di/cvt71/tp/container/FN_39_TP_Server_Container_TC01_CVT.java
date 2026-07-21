package com.ibm.di.cvt71.tp.container;

import java.io.IOException;
import java.net.Socket;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.di.api.APIEngine;
import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.utils.func.PortProbe;
import com.ibm.di.test.utils.func.TDIServer;

@CVTComponent(name = "tpserver")
public class FN_39_TP_Server_Container_TC01_CVT {

	private static TDIServer tdi = null;

	private static int tpServerPort;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tdi = new TDIServer();
		tdi.setProperty(APIEngine.PROP_TP_SERVER_ON, "false");

		tpServerPort = PortProbe.getAvailablePort();
		tdi.setProperty("web.server.port", "" + tpServerPort);

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
	@CVTTest(name = "CVT_FN-39_TP_Server_Container_TC01")
	@Test(expected = IOException.class)
	public void test_tp_server_does_not_listen_when_disabled() throws Exception {
		new Socket("localhost", tpServerPort);
	}
}
