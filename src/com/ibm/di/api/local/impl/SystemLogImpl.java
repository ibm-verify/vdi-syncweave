/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.util.Date;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.SystemLog;
import com.ibm.di.api.syslog.LogUtils;

/**
 * This class implements various methods for getting system log information.
 */
public class SystemLogImpl implements SystemLog {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Represents local session.
	 */
	private SessionImpl mSession = null;

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "SystemLog";

	/**
	 * @param aSession
	 */
	public SystemLogImpl(SessionImpl aSession) {
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName)
			throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getAvailableComponentLogFiles(
				LogUtils.AL_LOG_DIR_PREFIX, aConfigId, aALName);
	}

	/**
	 * Returns the names of all available log files for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param iNumber
	 *            the number of the logs
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			int iNumber) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getAvailableComponentLogFiles(
				LogUtils.AL_LOG_DIR_PREFIX, aConfigId, aALName, iNumber);
	}

	/**
	 * Returns the names of all available log files for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param dDate
	 *            the date of the logs
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date dDate) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getAvailableComponentLogFiles(
				LogUtils.AL_LOG_DIR_PREFIX, aConfigId, aALName, dDate);

	}
	
	/**
	 * Returns the names of all available log files for a given AssemblyLine.
	 * 
	 * @param aConfigId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine.
	 * @param startDate
	 *            the start date of the logs
	 * @param endDate
	 *            the end date of the logs
	 * @return a</code> String</code> array, each of its elements specifying
	 *         the name of a log file.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log file
	 *             names.
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date startDate, Date endDate) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}
		
		return LogUtils.getALLogFileNames(aConfigId, aALName, startDate, endDate);

	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aConfigId, String aALName)
			throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		String logFileName = null;
		String[] files = getALLogFileNames(aConfigId, aALName);
		if (files != null && files.length > 0) {
			logFileName = files[files.length - 1];
		}
		return logFileName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aConfigId, String aALName, String aLogFileName)
			throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLog(aConfigId, aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aConfigId, String aALName,
			String aLogFileName, int aKilobytes) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLogLastChunk(aConfigId, aALName, aLogFileName,
				aKilobytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(Date aMinDate) throws DIException {
		String methodExtension = "cleanAllOldLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR, mSession.getServerInfo().getServerID(),
				authSuccessful, interfaceName, methodExtension, mSession
						.getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		cleanAllOldALLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(int aKeepNum) throws DIException {
		String methodExtension = "cleanAllOldLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR, mSession.getServerInfo().getServerID(),
				authSuccessful, interfaceName, methodExtension, mSession
						.getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		cleanAllOldALLogs(aKeepNum);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(Date aMinDate) throws DIException {
		String methodExtension = "cleanAllOldALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR, mSession.getServerInfo().getServerID(),
				authSuccessful, interfaceName, methodExtension, mSession
						.getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		LogUtils.cleanAllOldALLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(int aKeepNum) throws DIException {
		String methodExtension = "cleanAllOldALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR, mSession.getServerInfo().getServerID(),
				authSuccessful, interfaceName, methodExtension, mSession
						.getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		LogUtils.cleanAllOldALLogs(aKeepNum);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate) throws DIException {
		String methodExtension = "cleanOldALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR + aConfigId, aALName, authSuccessful,
				interfaceName, methodExtension, aALName, aConfigId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return LogUtils.cleanOldALLogs(aConfigId, aALName, aMinDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate, Date aMaxDate) throws DIException {
		String methodExtension = "cleanOldALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR + aConfigId, aALName, authSuccessful,
				interfaceName, methodExtension, aALName, aConfigId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return LogUtils.cleanOldALLogs(aConfigId, aALName, aMinDate, aMaxDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName, int aKeepNum)
			throws DIException {
		String methodExtension = "cleanOldALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR + aConfigId, aALName, authSuccessful,
				interfaceName, methodExtension, aALName, aConfigId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return LogUtils.cleanOldALLogs(aConfigId, aALName, aKeepNum);
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
	 *         files are deleted successfully; else comma separated names of the
	 *         log files which are not deleted.
	 * 
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 */
	public String cleanALLogs(String aConfigId, String aALName,
			String[] logsToBeDeleted) throws DIException {
		String methodExtension = "cleanALLogs";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(),
				LogUtils.ROOT_LOG_DIR + aConfigId, aALName, authSuccessful,
				interfaceName, methodExtension, aALName, aConfigId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return LogUtils.cleanALLogs(aConfigId, aALName, logsToBeDeleted);
	}

}
