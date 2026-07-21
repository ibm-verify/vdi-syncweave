/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.SecurityRegistry;
import com.ibm.di.api.security.Identity;

/**
 * 
 * This interface provides information about various restrictions a user may
 * have. It lets you query what rights a user is granted and whether he/she is
 * authorized to execute a specific action.
 * 
 */
public class SecurityRegistryImpl implements SecurityRegistry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Represents the local session.
	 */
	private SessionImpl mSession = null;

	/**
	 * Class constructor.
	 * 
	 * @param aSession
	 *            {@link SessionImpl} instance
	 */
	public SecurityRegistryImpl(SessionImpl aSession) {
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userIsAdmin(String aUserId) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.isAdmin();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanReadConfig(String aUserId, String aConfigId)
			throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canReadConfig(aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteAL(String aUserId, String aConfigId,
			String aAssemblyLine) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canExecuteAL(aConfigId, aAssemblyLine);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteConfig(String aUserId, String aConfigId)
			throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canExecuteConfig(aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteConfigALs(String aUserId, String aConfigId)
			throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canExecuteConfigALs(aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteAll(String aUserId) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canExecuteAll();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanReadAll(String aUserId) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		Identity user = APIEngine.getIdentity(aUserId);
		if (user == null) {
			return false;
		}
		return user.canReadAll();
	}

}
