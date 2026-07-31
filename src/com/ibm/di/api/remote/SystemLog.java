/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import com.ibm.di.api.DIException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Date;

/**
 * 
 * This interface provides various methods for getting system log information.
 * 
 */
public interface SystemLog extends Remote {

	// AssemblyLines system log

	/**
	 * Returns the names of all available log files for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName)
			throws DIException, RemoteException;

	/**
	 * Returns the names of first 'n' log files for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param iNumber
	 *            the max number of log files to be returned. If iNumber exceeds
	 *            the number of available log files then all the log files names
	 *            are returned.
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			int iNumber) throws DIException, RemoteException;

	/**
	 * Returns the names of all available log files prior to the specified
	 * 'date' for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param dDate
	 *            all the log files prior to this date will e listed.
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date dDate) throws DIException, RemoteException;
	
	/**
	 * Returns the names of all available log files prior to the specified
	 * 'date' for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param startDate
	 *            all the log files after this date will be listed.
	 * @param endDate
	 *            all the log files before this date will be listed.           
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date startDate, Date endDate) throws DIException, RemoteException;

	/**
	 * Returns the name of the log file created on the last run of a given
	 * AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @return the name of the log file created on the last AssemblyLine's run.
	 * @throws DIException
	 *             if an error occurs while obtaining the log file name.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getALLastLogFileName(String aConfigId, String aALName)
			throws DIException, RemoteException;

	/**
	 * Given an AssemblyLine identification, and a log file name, retrieves the
	 * log of this AssemblyLine, stored in the specified file.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param aLogFileName
	 *            the name of the log file; no file path should be specified -
	 *            just the file name.
	 * @return the specified log of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getALLog(String aConfigId, String aALName, String aLogFileName)
			throws DIException, RemoteException;

	/**
	 * Retrieves the last chunk from a specified AssemblyLine's log file.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param aLogFileName
	 *            the name of the log file; no file path should be specified -
	 *            just the file name.
	 * @param aKilobytes
	 *            specifies in kilobytes the size of the log's last chunk that
	 *            will be read.
	 * @return the last chunk of the specified AssemblyLine's log.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getALLogLastChunk(String aConfigId, String aALName,
			String aLogFileName, int aKilobytes) throws DIException,
			RemoteException;

	// Log Cleanup

	/**
	 * Deletes all log files older than the specified date.
	 * 
	 * @param aMinDate
	 *            only log files that were last modified before this date will
	 *            be deleted.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void cleanAllOldLogs(Date aMinDate) throws DIException,
			RemoteException;

	/**
	 * Deletes all log files except those generated on the "
	 * <code>aKeepNum</code>" latest runs of all components.
	 * 
	 * @param aKeepNum
	 *            specifies the number of the latest log files that should not
	 *            be deleted; If for example, <code>aKeepNum == 5</code>, for
	 *            each component only the 5 latest log files will not be
	 *            deleted.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void cleanAllOldLogs(int aKeepNum) throws DIException,
			RemoteException;

	/**
	 * Deletes all AssemblyLines' log files older than the specified date.
	 * 
	 * @param aMinDate
	 *            only log files that were last modified before this date will
	 *            be deleted.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void cleanAllOldALLogs(Date aMinDate) throws DIException,
			RemoteException;

	/**
	 * Deletes all AssemblyLines' log files except those generated on the "
	 * <code>aKeepNum</code>" latest runs of all AssemblyLines.
	 * 
	 * @param aKeepNum
	 *            specifies the number of the latest log files that should not
	 *            be deleted; If for example, <code>aKeepNum == 5</code>, for
	 *            each AssemblyLine only the 5 latest log files will not be
	 *            deleted.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void cleanAllOldALLogs(int aKeepNum) throws DIException,
			RemoteException;

	/**
	 * Deletes those log files of the specified AssemblyLine, that are older
	 * than the specified date.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param aMinDate
	 *            only log files that were last modified after this date will
	 *            be deleted.
	 * @return a <code>Boolean</code> that holds <code>true</code> if the
	 *         log files were deleted successfully; and <code>null</code> if
	 *         there is no log folder for the specified AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate) throws DIException, RemoteException;
	
	/**
	 * Deletes those log files of the specified AssemblyLine, that are older
	 * than the specified date.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param aMinDate
	 *            only log files that were last modified after this date will
	 *            be deleted.
	 * @param aMaxDate
	 *            only log files that were last modified before this date will
	 *            be deleted.            
	 * @return a <code>Boolean</code> that holds <code>true</code> if the
	 *         log files were deleted successfully; and <code>null</code> if
	 *         there is no log folder for the specified AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate, Date aMaxDate) throws DIException, RemoteException;

	/**
	 * Deletes all log files of the specified AssemblyLine except those
	 * generated on the "<code>aKeepNum</code>" latest AssemblyLine runs.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param aKeepNum
	 *            specifies the number of the latest log files that should not
	 *            be deleted; If for example, <code>aKeepNum == 5</code> only
	 *            the 5 latest AssemblyLine's log files will not be deleted.
	 * @return a <code>Boolean</code> that holds <code>true</code> if the
	 *         log files were deleted successfully; and <code>null</code> if
	 *         there is no log folder for the specified AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName, int aKeepNum)
			throws DIException, RemoteException;

	/**
	 * Deletes all the logs which are specified in "
	 * <code>logsToBeDeleted</code>" array.
	 * 
	 * @param aConfigid
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param logsToBeDeleted
	 *            name of the log files which are to be deleted.
	 * @return a <code>String</code> that holds <code>null</code> if the log
	 *         files are deleted successfully; else comma separated names of the
	 *         log files which are not deleted.
	 * 
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String cleanALLogs(String aConfigid, String aALName,
			String[] logsToBeDeleted) throws DIException, RemoteException;

}
