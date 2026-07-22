/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Date;
import java.util.List;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.remote.TombstoneManager;
import com.ibm.di.server.ResourceHash;

/**
 * Represents a TombstoneManager instance. Provides various methods to deal with
 * Tombstones through remote session.
 */
public class TombstoneManagerImpl extends APIRemoteObject implements TombstoneManager {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 921645136006676861L;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * the local TombstoneManager
	 */
	private transient com.ibm.di.api.local.TombstoneManager mLocalTombstoneManager = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalTombstoneManager
	 *            the local TombstoneManager
	 * @param aSession
	 *            the session object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private TombstoneManagerImpl(com.ibm.di.api.local.TombstoneManager aLocalTombstoneManager, SessionImpl aSession,
			RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalTombstoneManager == null) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.TOMBSTONEMANAGER.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.14"));
		}

		mLocalTombstoneManager = aLocalTombstoneManager;
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone getTombstone(String aGUID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getTombstone(aGUID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, int aRecentNumberOfTombstones)
			throws DIException, RemoteException {
		return mLocalTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID, aRecentNumberOfTombstones);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, Date aStartTime, Date aEndTime)
			throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID, aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getConfigInstanceTombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID, Date aStartTime, Date aEndTime) throws DIException,
			RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getConfigInstanceTombstones(aConfigID, aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getTombstones(Date aStartTime, Date aEndTime) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.getTombstones(aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteTombstones(int aDays) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteTombstones(aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentTombstones(int aMostResentToKeep) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.keepMostRecentTombstones(aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, int aDays) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date olderThan) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, olderThan);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date startDate, Date endDate) throws DIException,
			RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, startDate, endDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName, String aConfigID, int aMostResentToKeep) throws DIException,
			RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.keepMostRecentALTombstones(aAssemblyLineName, aConfigID, aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteCITombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID, int aDays) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteCITombstones(aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentCITombstones(String aConfigID, int aMostResentToKeep) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.keepMostRecentCITombstones(aConfigID, aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteTombstone(String aGUID) throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return mLocalTombstoneManager.deleteTombstone(aGUID);
	}

	/**
	 * Creates TombstoneManagerImpl instance.
	 * 
	 * @param aLocalTombstoneManager
	 *            the local TombstoneManager
	 * @param aSession
	 *            the session object
	 * @return TombstoneManagerImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static TombstoneManagerImpl createInstance(com.ibm.di.api.local.TombstoneManager aLocalTombstoneManager,
			SessionImpl aSession) throws DIException, RemoteException {

		return new TombstoneManagerImpl(aLocalTombstoneManager, aSession, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

	public List<String> getConfigInstanceIDs() throws DIException, RemoteException {
		return mLocalTombstoneManager.getConfigInstanceIDs();
	}

	public List<String> getAssemblyLineNames(String configInstanceId) throws DIException, RemoteException {
		return mLocalTombstoneManager.getAssemblyLineNames(configInstanceId);
	}

	public boolean hasTombstones(String configInstanceId) throws DIException {
		return mLocalTombstoneManager.hasTombstones(configInstanceId);
	}

	public boolean hasTombstones(String configInstanceId, String alName) throws DIException {
		return mLocalTombstoneManager.hasTombstones(configInstanceId, alName);
	}
}
