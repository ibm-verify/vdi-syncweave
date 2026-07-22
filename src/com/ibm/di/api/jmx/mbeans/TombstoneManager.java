/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.jmx.JMXAgent;

/**
 * Represents a TombstoneManager instance. Provides various methods to deal with
 * Tombstones.
 */
public class TombstoneManager extends BaseAdmin implements
		TombstoneManagerMBean {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "TombstoneManager";

	/**
	 * Id of the MBean.
	 */
	public static final String MBEAN_ID = "TombstoneManager";

	/**
	 * {@link com.ibm.di.api.local.TombstoneManager} instance.
	 */
	private com.ibm.di.api.local.TombstoneManager mTombstoneManager = null;

	/**
	 * Class constructor.
	 * 
	 * @param aTombstoneManager
	 *            {@link com.ibm.di.api.local.TombstoneManager} instance.
	 * @throws DIException
	 */
	public TombstoneManager(
			com.ibm.di.api.local.TombstoneManager aTombstoneManager)
			throws DIException {
		mTombstoneManager = aTombstoneManager;
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
	public Tombstone getTombstone(String aGUID) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getTombstone(aGUID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName,
				aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID, int aRecentNumberOfTombstones) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName,
				aConfigID, aRecentNumberOfTombstones);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID, Date aStartTime, Date aEndTime)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getAssemblyLineTombstones(aAssemblyLineName,
				aConfigID, aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getConfigInstanceTombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID,
			Date aStartTime, Date aEndTime) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getConfigInstanceTombstones(aConfigID,
				aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public Tombstone[] getTombstones(Date aStartTime, Date aEndTime)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.getTombstones(aStartTime, aEndTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteTombstones(int aDays) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteTombstones(aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentTombstones(int aMostResentToKeep)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentTombstones(aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName,
				aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			int aDays) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName,
				aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			Date olderThan) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName,
				aConfigID, olderThan);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			Date startDate, Date endDate) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteALTombstones(aAssemblyLineName,
				aConfigID, startDate, endDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName,
			String aConfigID, int aMostResentToKeep) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentALTombstones(aAssemblyLineName,
				aConfigID, aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteCITombstones(aConfigID);
	}

	/**
	 * {@inheritDoc}
	 */
	public int deleteCITombstones(String aConfigID, int aDays)
			throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteCITombstones(aConfigID, aDays);
	}

	/**
	 * {@inheritDoc}
	 */
	public int keepMostRecentCITombstones(String aConfigID,
			int aMostResentToKeep) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.keepMostRecentCITombstones(aConfigID,
				aMostResentToKeep);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteTombstone(String aGUID) throws DIException {

		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mTombstoneManager.deleteTombstone(aGUID);
	}

}
