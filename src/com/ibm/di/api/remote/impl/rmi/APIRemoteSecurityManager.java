/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.rmi.RMISecurityManager;
import java.security.Permission;

/**
 * Wrapper API to expose the functionality available from
 * java.rmi.RMISecurityManager.
 */
public class APIRemoteSecurityManager extends RMISecurityManager {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * {@inheritDoc}
	 */
	public void checkPermission(Permission perm) {

	}

	/**
	 * {@inheritDoc}
	 */
	public void checkPermission(Permission perm, Object context) {

	}

}
