/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;

/**
 * 
 * Represents a TombstoneManager instance. Provides various methods to deal with
 * Tombstones through remote session.
 * 
 */
public interface TombstoneManager extends Remote {

	/**
	 * Returns a single tombstone object uniquely identified by the specified
	 * GUID.
	 * 
	 * @param aGUID
	 *            Tombstone GUID.
	 * @return the Tombstone object.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone object.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone getTombstone(String aGUID) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID) throws DIException, RemoteException;

	/**
	 * Returns the recent n number of tombsones for a specified AssemblyLine.
	 * 
	 * @since 6.1.1
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The name of the AssmeblyLine's configuration.
	 * @param aRecentNumberOfTombstones
	 *            The recent n number of tombsones to be returned.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */

	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, int aRecentNumberOfTombstones)
			throws DIException, RemoteException;

	/**
	 * Returns all available tombstones for the specified AssemblyLine with
	 * timestamps in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The name of the AssmeblyLine's configuration.
	 * @param aStartTime
	 *            peroid start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, java.util.Date aStartTime,
			java.util.Date aEndTime) throws DIException, RemoteException;

	/**
	 * Returns all available tombstones for the specified Config Instance.
	 * 
	 * @param aConfigID
	 *            The configuration name.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID) throws DIException, RemoteException;

	/**
	 * Returns all available tombstones for the specified Config Instance with
	 * timestamps in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aConfigID
	 *            The configuration name.
	 * @param aStartTime
	 *            peroid start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID, java.util.Date aStartTime, java.util.Date aEndTime)
			throws DIException, RemoteException;

	/**
	 * Returns all available tombstones with timestamps in the interval
	 * specified by aStartTime and aEndTime.
	 * 
	 * @param aStartTime
	 *            peroid start time.
	 * @param aEndTime
	 *            period end time.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 *             if an error occurs while getting the Tombstone objects for
	 *             the specified interval.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Tombstone[] getTombstones(java.util.Date aStartTime, java.util.Date aEndTime) throws DIException, RemoteException;

	// Methods for clearing old tombstone records

	/**
	 * Deletes all tombstones that are older than the specified number of days.
	 * 
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteTombstones(int aDays) throws DIException, RemoteException;

	/**
	 * After this method is executed only the aMostRecentToKeep most recent
	 * tombstone records are kept and all other are deleted.
	 * 
	 * @param aMostResentToKeep
	 *            number of most recent tombstones to keep.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int keepMostRecentTombstones(int aMostResentToKeep) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, int aDays) throws DIException, RemoteException;

	/**
	 * Deletes all tombstones for the specified AssemblyLine that are older than
	 * the specified number of days.
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param olderThan
	 *            Date.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date olderThan) throws DIException, RemoteException;

	/**
	 * Deletes all tombstones for the specified AssemblyLine that are in the
	 * specified <code>Date</code> range
	 * 
	 * @param aAssemblyLineName
	 *            The AssemblyLine name.
	 * @param aConfigID
	 *            The AssemblyLine's configuration name.
	 * @param startDate
	 *            Date.
	 * @param endDate
	 *            Date.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date startDate, Date endDate) throws DIException,
			RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName, String aConfigID, int aMostResentToKeep) throws DIException,
			RemoteException;

	/**
	 * Deletes all tombstones for specified Config Instance.
	 * 
	 * @param aConfigID
	 *            Configuration name.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone records.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteCITombstones(String aConfigID) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int deleteCITombstones(String aConfigID, int aDays) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int keepMostRecentCITombstones(String aConfigID, int aMostResentToKeep) throws DIException, RemoteException;

	/**
	 * Deletes the tombstone with the specified GUID.
	 * 
	 * @param aGUID
	 *            Tombstone GUID.
	 * @return true only when the tombstone object with the specified GUID is
	 *         found and deleted.
	 * @throws DIException
	 *             if an error occurs while deleting Tombstone record.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean deleteTombstone(String aGUID) throws DIException, RemoteException;

	/**
	 * Obtains a list of IDs of configInstances for which a tombstone has been
	 * created.
	 * 
	 * @return the list of configInstance IDs.
	 * @throws DIException
	 *             if error occurs while obtaining the Config IDs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public List<String> getConfigInstanceIDs() throws DIException, RemoteException;

	/**
	 * Checks whether there are tombstone records for a configInstance with the
	 * specified ID.
	 * 
	 * @return true if the configInstance has tombstones, false otherwise
	 * @throws DIException
	 *             if error occurs while obtaining the Config IDs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean hasTombstones(String configInstanceId) throws DIException, RemoteException;

	/**
	 * Obtains a list of AssemblyLine Names of the AssemblyLines for which a
	 * tombstone has been created.
	 * 
	 * @return the list of configInstance IDs.
	 * @throws DIException
	 *             if error occurs while obtaining the AL names
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public List<String> getAssemblyLineNames(String configInstanceId) throws DIException, RemoteException;

	/**
	 * Checks whether there are tombstone records for an AssemblyLine with the
	 * specified name.
	 * 
	 * @return true if the AssemblyLine has tombstones, false otherwise
	 * @throws DIException
	 *             if error occurs while obtaining the Config IDs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean hasTombstones(String configInstanceId, String alName) throws DIException, RemoteException;
}