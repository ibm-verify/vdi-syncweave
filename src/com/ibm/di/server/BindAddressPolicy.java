/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.net.InetAddress;

/**
 * This interface provides basic methods for obtaining bind addresses.
 * 
 * @since 7.1
 */
public interface BindAddressPolicy {

	/**
	 * Provides the bind address to connect to. * means bind to all available
	 * network interfaces. Mind that only one IP address value should be
	 * provided for the related property. No host names are accepted - only IP
	 * addresses.
	 */
	public abstract InetAddress getBindAddress();
}
