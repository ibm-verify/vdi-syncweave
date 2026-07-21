package com.ibm.di.server;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import com.ibm.di.server.BindAddressPolicy;

import org.junit.Test;

public class BindAddressPolicyImplTest {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String STAR = "*";
	public static final String VALID_IP_ADDRESS = "1.1.1.1";
	public static final String INVALID_IP_ADDRESS = "1.1.1.1,;4";

	@Test
	public void test_Default_Bind_Address_When_Not_Set() {
		Properties props = new Properties();
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Default_Bind_Adress_When_Empty() {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, "");
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Default_Bind_Address_When_Set_To_Star() {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, STAR);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Default_Bind_Address_When_Set_Invalid_Value() {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, INVALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props);

		assertNull(bindAddr.getBindAddress());
	}

	@Test
	public void test_Default_Bind_Address_When_Set_Valid_Value() throws UnknownHostException {
		Properties props = new Properties();
		props.setProperty(BindAddressPolicyImpl.PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS, VALID_IP_ADDRESS);
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(props);

		assertNotNull(bindAddr.getBindAddress());
		assertEquals(bindAddr.getBindAddress(), InetAddress.getByName(VALID_IP_ADDRESS));
	}

}
