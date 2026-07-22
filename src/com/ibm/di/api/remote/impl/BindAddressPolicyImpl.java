/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * This class take responsibility for providing information about remote bind
 * addresses. This class is developed only for internal use and should not be
 * used for other purpose.
 * 
 * @since 7.1
 */
public class BindAddressPolicyImpl extends com.ibm.di.server.BindAddressPolicyImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Property name used to specify the remote bind address.
	 */
	public static final String PROP_API_REMOTE_BIND_ADDRESS = "api.remote.bind.address";

	/**
	 * String representing the remote bind address.
	 */
	private String remoteBindAddrStr;

	/**
	 * <code>java.net.InetAddress</code> object representing the remote bind
	 * address.
	 * <p>
	 * Note that <code>null</code> value means binding to all available network
	 * interfaces. If no value is specified in the property then the default
	 * bind address it taken.
	 * </p>
	 */
	protected InetAddress remoteBindINetAddr;

	/**
	 * Property file for obtaining the bind addresses from.
	 */
	private Properties prop;

	/**
	 * Determines if the caller is running on TDI Server side or not.
	 * <code>True</code> means running of TDI Server side.
	 */
	private boolean isTDIServerSide = false;

	/**
	 * Constructor. Accepts the Properties file that bind addresses will be
	 * retrieved from. Implicitly defines that the caller is running on TDI
	 * Server Client side.
	 * 
	 * @param propFile
	 *            Properties file that bind addresses will be obtained from.
	 */
	public BindAddressPolicyImpl(Properties propFile) {
		this(propFile, false);
	}

	/**
	 * Constructor. Accepts the Properties file that bind addresses will be
	 * retrieved from. The <code>tdiServerSide</code> parameter determines
	 * whether the caller is running on TDI Server side or not.
	 * 
	 * @param propFile
	 *            Properties file that bind addresses will be obtained from.
	 * @param tdiServerSide
	 *            Determines on which side the caller is running.<br>
	 *            <code>True</code> if caller is running on TDI Server API side<br>
	 *            <code>False</code> if caller is running on TDI Server Client
	 *            side.
	 */
	public BindAddressPolicyImpl(Properties propFile, boolean tdiServerSide) {
		super(propFile);
		prop = propFile;
		isTDIServerSide = tdiServerSide;
		initRemoteBindAddress();
	}

	/**
	 * Initialize the default bind address value. If the value is set to "*"
	 * then all network interfaces will be bound to. Missing or empty value
	 * means fall back to the default bind address.
	 */
	private void initRemoteBindAddress() {
		remoteBindAddrStr = prop.getProperty(PROP_API_REMOTE_BIND_ADDRESS);
		if (remoteBindAddrStr == null || remoteBindAddrStr.trim().length() == 0) {
			remoteBindINetAddr = super.getBindAddress();
		} else {
			if (remoteBindAddrStr.equals("*"))
				remoteBindINetAddr = null;
			else {
				try {
					remoteBindINetAddr = InetAddress.getByName(remoteBindAddrStr);
				} catch (UnknownHostException e) {
					remoteBindINetAddr = super.getBindAddress();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public InetAddress getBindAddress() {
		if (isTDIServerSide)
			return remoteBindINetAddr;
		else
			return super.getBindAddress();
	}
}
