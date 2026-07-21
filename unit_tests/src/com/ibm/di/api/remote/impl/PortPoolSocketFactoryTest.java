package com.ibm.di.api.remote.impl;

import static junit.framework.Assert.*;

import java.net.ServerSocket;
import java.util.HashSet;

import org.junit.*;

public class PortPoolSocketFactoryTest {

	@Before
	public void setUp() {
		PortPoolSocketFactory.parsePorts("9001,9002-9003");		
	}

	@Test
	public void test_Allocation_explicit_port_8001_This_may_fail_if_something_else_is_using_that_port() {

		PortPoolSocketFactory factory = new PortPoolSocketFactory(null);
		Exception error = null;
		ServerSocket serverSocket = null;
		try {
			serverSocket = factory.createServerSocket(8001);
		} catch (Exception e) {
			error = e;
		}
		assertNull(error);
		assertNotNull(serverSocket);
		assertEquals(serverSocket.getLocalPort(), 8001);
	}

	@Test
	public void test_At_least_one_port_in_the_range_9001_to_9003_is_available_If_this_fails_verify_that_something_is_using_those_ports() {

		PortPoolSocketFactory factory = new PortPoolSocketFactory(null);
		Exception error = null;
		ServerSocket serverSocket = null;
		try {
			serverSocket = factory.createServerSocket(0);
			serverSocket.close();
		} catch (Exception e) {
			error = e;
		}
		assertNull(error);
		assertNotNull(serverSocket);
	}

	@Test
	public void test_Port_Allocation_dynamic_3() {

		PortPoolSocketFactory factory = new PortPoolSocketFactory(null);
		Exception error = null;
		ServerSocket serverSocket = null;
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < 4; i++) {
			try {
				serverSocket = factory.createServerSocket(0);
			} catch (Exception e) {
				error = e;
				e.printStackTrace();
				break;
			}
			assertNotNull(serverSocket);
			int port = serverSocket.getLocalPort();
			assertTrue(port >= 9001);
			assertTrue(port <= 9003);
			assertTrue(!set.contains(port));
			set.add(port);
		}
		assertNotNull(error);
		assertTrue(error.getMessage().startsWith("CTGDKD507E")); // Out of ports
	}

}
