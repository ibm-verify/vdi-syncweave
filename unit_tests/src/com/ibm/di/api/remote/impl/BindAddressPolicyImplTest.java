
package com.ibm.di.api.remote.impl;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.junit.Test;

import com.ibm.di.server.BindAddressPolicy;

public class BindAddressPolicyImplTest {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String STAR = "*";
	public static final String VALID_IP_ADDRESS = "1.1.1.1";
	public static final String VALID_IP_ADDRESS_2 = "2.2.2.2";
	public static final String INVALID_IP_ADDRESS = "1.1.1.1,;4";

	@Test
	public void test_Remote_Bind_Address_When_Not_Set_And_Default_Bind_Address_Set_Valid_And_On_TDI_Client_Side()
			throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, false);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

	@Test
	public void test_Remote_Bind_Address_When_Not_Set_And_Default_Bind_Address_Set_Valid_And_On_TDI_Server_Side()
			throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

	@Test
	public void test_Remote_Bind_Address_When_Not_Set_And_Default_Bind_Address_Set_Invalid_And_On_TDI_Client_Side()
			throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, INVALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, false);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Remote_Bind_Address_When_Not_Set_And_Default_Bind_Address_Set_Invalid_And_On_TDI_Server_Side()
			throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, INVALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Remote_Bind_Address_When_Not_Set_And_Default_Bind_Address_Not_Set() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, INVALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Remote_Bind_Address_When_Set_Invalid() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		props.setProperty(BindAddressPolicyImpl.PROP_API_REMOTE_BIND_ADDRESS, INVALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

	@Test
	public void test_Remote_Bind_Address_When_Set_Valid_On_TDI_Server_Side() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_API_REMOTE_BIND_ADDRESS, VALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

	@Test
	public void test_Remote_Bind_Address_When_Set_Valid_On_TDI_Client_Side() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		props.setProperty(BindAddressPolicyImpl.PROP_API_REMOTE_BIND_ADDRESS, VALID_IP_ADDRESS_2);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, false);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

	@Test
	public void test_Remote_Bind_Address_When_Set_Star_On_TDI_Server_Side() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_API_REMOTE_BIND_ADDRESS, STAR);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, true);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Remote_Bind_Address_When_Set_Star_On_TDI_Client_Side() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		props.setProperty(BindAddressPolicyImpl.PROP_API_REMOTE_BIND_ADDRESS, STAR);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props, false);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}
}
