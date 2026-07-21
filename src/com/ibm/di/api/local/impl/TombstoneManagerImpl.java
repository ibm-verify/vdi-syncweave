/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.util.Date;
import java.util.List;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.local.TombstoneManager;

/**
 * Represents a TombstoneManager instance. Provides various methods to deal with
 * Tombstones.
 */
public class TombstoneManagerImpl implements TombstoneManager {

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
	 * Manages {@link Tombstone} objects.
	 */
	private com.ibm.di.api.tm.TombstoneManager mTombstoneManager = null;

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "TombstoneManager";

	/**
	 * 
	 * @param aSession
	 */
	public TombstoneManagerImpl(SessionImpl aSession) {
		mSession = aSession;
		mTombstoneManager = APIEngine.getTombstoneManager();
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone getTombstone(String aGUID) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getTombstone(aGUID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, int aRecentNumberOfTombstones)
			throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID, aRecentNumberOfTombstones);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, Date aStartTime, Date aEndTime)
			throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName, aConfigID, aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getConfigInstanceTombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID, Date aStartTime, Date aEndTime) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getConfigInstanceTombstones(aConfigID, aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getTombstones(Date aStartTime, Date aEndTime) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getTombstones(aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteTombstones(int aDays) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteTombstones(aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentTombstones(int aMostResentToKeep) throws DIException {
		String methodExtension = "keepMostRecentTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME, mSession
				.getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, mSession.getServerInfo()
				.getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentTombstones(aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID) throws DIException {
		String methodExtension = "deleteALTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aAssemblyLineName, authSuccessful, interfaceName, methodExtension, aAssemblyLineName, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, int aDays) throws DIException {
		String methodExtension = "deleteALTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aAssemblyLineName, authSuccessful, interfaceName, methodExtension, aAssemblyLineName, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date olderThan) throws DIException {
		String methodExtension = "deleteALTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aAssemblyLineName, authSuccessful, interfaceName, methodExtension, aAssemblyLineName, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, olderThan);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date startDate, Date endDate) throws DIException {
		String methodExtension = "deleteALTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aAssemblyLineName, authSuccessful, interfaceName, methodExtension, aAssemblyLineName, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName, aConfigID, startDate, endDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName, String aConfigID, int aMostResentToKeep) throws DIException {
		String methodExtension = "keepMostRecentALTombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aAssemblyLineName, authSuccessful, interfaceName, methodExtension, aAssemblyLineName, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentALTombstones(aAssemblyLineName, aConfigID, aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID) throws DIException {
		String methodExtension = "deleteCITombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aConfigID, authSuccessful, interfaceName, methodExtension, aConfigID, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteCITombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID, int aDays) throws DIException {
		String methodExtension = "deleteCITombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aConfigID, authSuccessful, interfaceName, methodExtension, aConfigID, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteCITombstones(aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentCITombstones(String aConfigID, int aMostResentToKeep) throws DIException {
		String methodExtension = "keepMostRecentCITombstones";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME,
				aConfigID, authSuccessful, interfaceName, methodExtension, aConfigID, aConfigID);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentCITombstones(aConfigID, aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteTombstone(String aGUID) throws DIException {
		String methodExtension = "deleteTombstone";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), com.ibm.di.api.tm.TombstoneManager.TABLE_NAME, aGUID,
				authSuccessful, interfaceName, methodExtension, aGUID, null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteTombstone(aGUID);
	}

	public List<String> getConfigInstanceIDs() throws DIException {
		return mTombstoneManager.getConfigInstanceIDs();
	}

	public List<String> getAssemblyLineNames(String configInstanceId) throws DIException {
		return mTombstoneManager.getAssemblyLineNames(configInstanceId);
	}

	public boolean hasTombstones(String configInstanceId) throws DIException {
		return mTombstoneManager.hasTombstones(configInstanceId);
	}

	public boolean hasTombstones(String configInstanceId, String alName) throws DIException {
		return mTombstoneManager.hasTombstones(configInstanceId, alName);
	}
}
