/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;

/**
 * 
 * Represents a TombstoneManager instance. Provides various methods to deal with
 * Tombstones.
 * 
 */
public interface TombstoneManagerMBean extends BaseAdminMBean {

	/**
	 * Returns a single tombstone object uniquely identified by the specified
	 * GUID.
	 * 
	 * @param aGUID
	 *            Tombstone GUID.
	 * @return the Tombstone object.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone object.
	 */
	public Tombstone getTombstone(String aGUID) throws DIException;

	/**
	 * Returns all available tombstones for the specified AssemblyLine.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The name of the AssmeblyLine's configuration.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects.
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID) throws DIException;

	/**
	 * Returns the recent n number of tombstones for a specified AssemblyLine.
	 * 
	 * @since 6.1.1
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The name of the AssmeblyLine's configuration.
	 * @param aRecentNumberOfTombstones
	 *            The recent n number of tombstones to be fetched.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID, int aRecentNumberOfTombstones) throws DIException;

	/**
	 * Returns all available tombstones for the specified AssemblyLine with
	 * timestamps in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The name of the AssmeblyLine's configuration.
	 * @param aStartTime
	 *            period start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified AssemblyLine.
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName,
			String aConfigID, java.util.Date aStartTime, java.util.Date aEndTime)
			throws DIException;

	/**
	 * Returns all available tombstones for the specified Config Instance.
	 * 
	 * @param aConfigID
	 *            The configuration name.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified Config Instance.
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID)
			throws DIException;

	/**
	 * Returns all available tombstones for the specified Config Instance with
	 * timestamps in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aConfigID
	 *            The configuration name.
	 * @param aStartTime
	 *            period start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified Config Instance.
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID,
			java.util.Date aStartTime, java.util.Date aEndTime)
			throws DIException;

	/**
	 * Returns all available tombstones with timestamps in the interval
	 * specified by aStartTime and aEndTime.
	 * 
	 * @param aStartTime
	 *            period start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified interval.
	 */
	public Tombstone[] getTombstones(java.util.Date aStartTime,
			java.util.Date aEndTime) throws DIException;

	// Methods for clearing old tombstone records

	/**
	 * Deletes all tombstones that are older than the specified number of days.
	 * 
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteTombstones(int aDays) throws DIException;

	/**
	 * After this method is executed only the aMostRecentToKeep most recent
	 * tombstone records are kept and all other are deleted.
	 * 
	 * @param aMostResentToKeep
	 *            number of most recent tombstones to keep.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int keepMostRecentTombstones(int aMostResentToKeep)
			throws DIException;

	/**
	 * Deletes all tombstones for specified AssemblyLine.
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID)
			throws DIException;

	/**
	 * Deletes all tombstones for the specified AssemblyLine that are older than
	 * the specified number of days.
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			int aDays) throws DIException;

	/**
	 * Deletes all tombstones for the specified AssemblyLine that are older than
	 * the specified date.
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param olderThan
	 *            Date
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			Date olderThan) throws DIException;
	
	/**
	 * Deletes all tombstones for the specified AssemblyLine that are in the specified
	 * <code>Date</code> range
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date           
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID,
			Date startDate, Date endDate) throws DIException;

	/**
	 * After this method is executed only the aMostRecentToKeep most recent
	 * tombstone records for the specified AssemblyLine are kept and all other
	 * are deleted.
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param aMostResentToKeep
	 *            Number of most recent tombstones to keep.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName,
			String aConfigID, int aMostResentToKeep) throws DIException;

	/**
	 * Deletes all tombstones for specified Config Instance.
	 * 
	 * @param aConfigID
	 *            Configuration name.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteCITombstones(String aConfigID) throws DIException;

	/**
	 * Deletes all tombstones for the specified Config Instance that are older
	 * than the specified number of days.
	 * 
	 * @param aConfigID
	 *            Configuration name.
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int deleteCITombstones(String aConfigID, int aDays)
			throws DIException;

	/**
	 * After this method is executed only the aMostRecentToKeep most recent
	 * tombstone records for the specified Config Instance are kept and all
	 * other are deleted.
	 * 
	 * @param aConfigID
	 *            Configuration name.
	 * @param aMostResentToKeep
	 *            Number of most recent tombstones to keep.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 */
	public int keepMostRecentCITombstones(String aConfigID,
			int aMostResentToKeep) throws DIException;

	/**
	 * Deletes the tombstone with the specified GUID.
	 * 
	 * @param aGUID
	 *            Tombstone GUID.
	 * @return true only when the tombstone object with the specified GUID is
	 *         found and deleted.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone record.
	 */
	public boolean deleteTombstone(String aGUID) throws DIException;

}
