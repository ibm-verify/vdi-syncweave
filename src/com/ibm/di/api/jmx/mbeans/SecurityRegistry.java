/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;

/**
 * This class provides information about various restrictions a user may have.
 * It lets you query what rights a user is granted and whether he/she is
 * authorized to execute a specific action.
 */
public class SecurityRegistry extends BaseAdmin implements
		SecurityRegistryMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "SecurityRegistry";

	/**
	 * Id of the MBean.
	 */
	public static final String MBEAN_ID = "SecurityRegistry";

	/**
	 * {@link com.ibm.di.api.local.SecurityRegistry}
	 */
	private com.ibm.di.api.local.SecurityRegistry mSecurityRegistry = null;

	/**
	 * Class constructor {@link com.ibm.di.api.local.SecurityRegistry}
	 * 
	 * @param aSecurityRegistry
	 */
	public SecurityRegistry(
			com.ibm.di.api.local.SecurityRegistry aSecurityRegistry) {
		mSecurityRegistry = aSecurityRegistry;
	}

	// MBean interface

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return MBEAN_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userIsAdmin(String aUserId) throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userIsAdmin(aUserId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanReadConfig(String aUserId, String aConfigId)
			throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanReadConfig(aUserId,
				aConfigId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanExecuteAL(String aUserId, String aConfigId,
			String aAssemblyLine) throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanExecuteAL(aUserId,
				aConfigId, aAssemblyLine));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanExecuteConfig(String aUserId, String aConfigId)
			throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanExecuteConfig(aUserId,
				aConfigId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanExecuteConfigALs(String aUserId, String aConfigId)
			throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanExecuteConfigALs(
				aUserId, aConfigId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanExecuteAll(String aUserId) throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanExecuteAll(aUserId));
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean userCanReadAll(String aUserId) throws DIException {
		checkIfUserIsAdmin();

		return Boolean.valueOf(mSecurityRegistry.userCanReadAll(aUserId));
	}

	/**
	 * Verifies user ID.
	 * 
	 * @throws DIException
	 *             if the user isn't admin.
	 */
	private void checkIfUserIsAdmin() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
	}

}
