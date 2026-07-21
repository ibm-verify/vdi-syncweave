/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Date;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.SystemLog;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class implements various methods for getting system log information.
 * 
 */
public class SystemLogImpl extends APIRemoteObject implements SystemLog {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -6692402920501689550L;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * local system log
	 */
	private transient com.ibm.di.api.local.SystemLog mLocalSystemLog = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalSystemLog
	 *            local system log
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private SystemLogImpl(com.ibm.di.api.local.SystemLog aLocalSystemLog,
			SessionImpl aSession, RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalSystemLog == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.SYSTEM.LOG.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.SESSION.OBJECT.IS.NULL.12"));
		}

		mLocalSystemLog = aLocalSystemLog;
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLogFileNames(aConfigId, aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aConfigId, String aALName)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLastLogFileName(aConfigId, aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aConfigId, String aALName, String aLogFileName)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLog(aConfigId, aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			int iNumber) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLogFileNames(aConfigId, aALName, iNumber);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date dDate) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLogFileNames(aConfigId, aALName, dDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date startDate, Date endDate) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLogFileNames(aConfigId, aALName, startDate, endDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aConfigId, String aALName,
			String aLogFileName, int aKilobytes) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.getALLogLastChunk(aConfigId, aALName,
				aLogFileName, aKilobytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(Date aMinDate) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		mLocalSystemLog.cleanAllOldLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(int aKeepNum) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		mLocalSystemLog.cleanAllOldLogs(aKeepNum);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(Date aMinDate) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		mLocalSystemLog.cleanAllOldALLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(int aKeepNum) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		mLocalSystemLog.cleanAllOldALLogs(aKeepNum);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.cleanOldALLogs(aConfigId, aALName, aMinDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate, Date aMaxDate) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.cleanOldALLogs(aConfigId, aALName, aMinDate, aMaxDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName, int aKeepNum)
			throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalSystemLog.cleanOldALLogs(aConfigId, aALName, aKeepNum);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aLocalSystemLog
	 *            local system log
	 * @param aSession
	 *            the SessionImpl object
	 * @return SystemLogImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static SystemLogImpl createInstance(
			com.ibm.di.api.local.SystemLog aLocalSystemLog, SessionImpl aSession)
			throws DIException, RemoteException {
		return new SystemLogImpl(aLocalSystemLog, aSession, APIEngine
				.getClientSF(), APIEngine.getServerSF());
	}

	/**
	 * Deletes all the logs which are specified in "
	 * <code>logsToBeDeleted</code>" array.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param logsToBeDeleted
	 *            name of the log files which are to be deleted.
	 * @return a <code>String</code> that holds <code>null</code> if the log
	 *         files are deleted successfully; else comma seperated names of the
	 *         log files which are not deleted.
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String cleanALLogs(String aConfigId, String aALName,
			String[] logsToBeDeleted) throws DIException, RemoteException {
		return mLocalSystemLog.cleanALLogs(aConfigId, aALName, logsToBeDeleted);
	}

}
