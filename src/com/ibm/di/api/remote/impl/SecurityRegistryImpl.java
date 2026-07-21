/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.SecurityRegistry;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class provides information about various restrictions a user may have It
 * lets you query what rights a user is granted and whether he/she is authorized
 * to execute a specific action.
 * 
 */
public class SecurityRegistryImpl extends APIRemoteObject implements
		SecurityRegistry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -7240460021387101436L;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * local security registry
	 */
	private transient com.ibm.di.api.local.SecurityRegistry mLocalSecurityRegistry = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalSecurityRegistry
	 *            local security registry
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private SecurityRegistryImpl(
			com.ibm.di.api.local.SecurityRegistry aLocalSecurityRegistry,
			SessionImpl aSession, RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalSecurityRegistry == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.SECURITY.REGISTRY.OBJECT"));
		}
		if (aSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.SESSION.OBJECT.IS.NULL.10"));
		}

		mLocalSecurityRegistry = aLocalSecurityRegistry;
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userIsAdmin(String aUserId) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userIsAdmin(aUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanReadConfig(String aUserId, String aConfigId)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanReadConfig(aUserId, aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteAL(String aUserId, String aConfigId,
			String aAssemblyLine) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanExecuteAL(aUserId, aConfigId,
				aAssemblyLine);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteConfig(String aUserId, String aConfigId)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanExecuteConfig(aUserId, aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteConfigALs(String aUserId, String aConfigId)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanExecuteConfigALs(aUserId,
				aConfigId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanExecuteAll(String aUserId) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanExecuteAll(aUserId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userCanReadAll(String aUserId) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		return mLocalSecurityRegistry.userCanReadAll(aUserId);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aLocalSecurityRegistry
	 *            local sequrity registry
	 * @param aSession
	 *            the SessionImpl object
	 * @return SecurityRegistryImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static SecurityRegistryImpl createInstance(
			com.ibm.di.api.local.SecurityRegistry aLocalSecurityRegistry,
			SessionImpl aSession) throws DIException, RemoteException {
		return new SecurityRegistryImpl(aLocalSecurityRegistry, aSession,
				APIEngine.getClientSF(), APIEngine.getServerSF());
	}

}
