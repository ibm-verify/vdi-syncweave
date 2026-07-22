/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;

/**
 * 
 * This class implements various methods for getting system log information.
 * 
 */
public class SystemLog extends BaseAdmin implements SystemLogMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "SystemLog";

	/**
	 * Id of the MBean.
	 */
	public static final String MBEAN_ID = "SystemLog";

	/**
	 * Logs system information.
	 */
	private com.ibm.di.api.local.SystemLog mSystemLog = null;

	/**
	 * 
	 * @param aSystemLog
	 *            {@link com.ibm.di.api.local.SystemLog}
	 */
	public SystemLog(com.ibm.di.api.local.SystemLog aSystemLog) {
		mSystemLog = aSystemLog;
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
	public String[] getALLogFileNames(String aConfigId, String aALName)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLogFileNames(aConfigId, aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName, int n)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLogFileNames(aConfigId, aALName, n);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date dDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLogFileNames(aConfigId, aALName, dDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aConfigId, String aALName,
			Date startDate, Date endDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLogFileNames(aConfigId, aALName, startDate, endDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aConfigId, String aALName)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLastLogFileName(aConfigId, aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aConfigId, String aALName, String aLogFileName)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLog(aConfigId, aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aConfigId, String aALName,
			String aLogFileName, Integer aKilobytes) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null
				&& !JMXAgent.getSecRegistry().userCanExecuteAL(userId,
						aConfigId, aALName)) {
			throw new AuthorizationException();
		}

		return mSystemLog.getALLogLastChunk(aConfigId, aALName, aLogFileName,
				aKilobytes.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(Date aMinDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mSystemLog.cleanAllOldLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldLogs(Integer aKeepNum) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mSystemLog.cleanAllOldLogs(aKeepNum.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(Date aMinDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mSystemLog.cleanAllOldALLogs(aMinDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanAllOldALLogs(Integer aKeepNum) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mSystemLog.cleanAllOldALLogs(aKeepNum.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mSystemLog.cleanOldALLogs(aConfigId, aALName, aMinDate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Date aMinDate, Date aMaxDate) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mSystemLog.cleanOldALLogs(aConfigId, aALName, aMinDate, aMaxDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean cleanOldALLogs(String aConfigId, String aALName,
			Integer aKeepNum) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mSystemLog.cleanOldALLogs(aConfigId, aALName, aKeepNum
				.intValue());
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
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
		return mSystemLog.cleanALLogs(aConfigId, aALName, logsToBeDeleted);
	}

}
