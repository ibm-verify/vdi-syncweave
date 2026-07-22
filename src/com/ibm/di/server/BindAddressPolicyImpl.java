/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * This class takes responsibility for providing information about default. This
 * class is for internal use only and you should not rely on it for other
 * purpose.
 * 
 * @since 7.1
 */
public class BindAddressPolicyImpl implements BindAddressPolicy {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Property name used to specify the default bind address.
	 */
	public static final String PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS = "com.ibm.di.default.bind.address";

	/**
	 * String representing the default bind address.
	 */
	private String defaultBindAddrStr;

	/**
	 * <code>java.net.InetAddress</code> object representing the default bind
	 * address.
	 * <p>
	 * Note that <code>null</code> value means binding to all available network
	 * interfaces.
	 * </p>
	 */
	private InetAddress defaultBindINetAddr;

	/**
	 * Property file for obtaining the bind addresses from.
	 */
	private Properties prop;

	/**
	 * Constructor
	 * 
	 * @param propFile
	 *            Properties file that bind addresses will be obtained from.
	 */
	public BindAddressPolicyImpl(Properties propFile) {
		prop = propFile;
		initDefaultBindAddress();
	}

	/**
	 * Initialize the default bind address value. If the value is set to "*"
	 * then all network interfaces will be bound to. Missing or empty value is
	 * treated just like "*". In case that the IP address could not be
	 * determined the default bind address will be set to "*".
	 */
	private void initDefaultBindAddress() {
		// null value is allowed
		defaultBindAddrStr = prop.getProperty(PROP_COM_IBM_DI_DEFAULT_BIND_ADDRESS);
		if (defaultBindAddrStr == null || defaultBindAddrStr.trim().length() == 0 || defaultBindAddrStr.equals("*")) {
			defaultBindINetAddr = null; // bind to all network interfaces
		} else {
			try {
				defaultBindINetAddr = InetAddress.getByName(defaultBindAddrStr);
			} catch (UnknownHostException e) {
				defaultBindINetAddr = null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public InetAddress getBindAddress() {
		return defaultBindINetAddr;
	}
}
