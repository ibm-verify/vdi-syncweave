/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.syslog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * Some Logging Utilities
 * 
 */
public class LogUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Root log directory
	 */
	public static final String ROOT_LOG_DIR = "system_logs/";

	/**
	 * Assembly Line log directory prefix
	 */
	public static final String AL_LOG_DIR_PREFIX = "AL_";

	/**
	 * Directory separator
	 */
    // ISDIDEV-567 : making DIR_SEP work on different platforms like linux, windows.
	private static final String DIR_SEP = File.separator;

	/**
	 * New line symbol
	 */
	private static final String STR_NEW_LINE = "\n";

	/**
	 * Assembly Line component type
	 */
	private static final int COMP_TYPE_AL = 0;

	/** Pattern that matches all invalid characters in a config ID */
	private static final Pattern CONFIG_ID_INVALIDATORS = Pattern.compile("[\\\\/\\*:\\?<\">\\|]");

	/**
	 * NLS property set for TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Searches the given Log object for the System Log Appender
	 * 
	 * @param aLog
	 * @return the System Log Appender, part of the given Log object, null if
	 *         not found
	 */
	public static SystemLogAppender getSystemLogAppender(Log aLog) {
		if (aLog == null) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString("SEVER.API.GETSYSTEMLOGAPPENDER.CALLED.WITH.NULL.LOG"));
			}
			return null;
		}

		return aLog.getSystemLog();
	}

	// *************************************************************************
	// reading log files
	// *************************************************************************

	/**
	 * Retrieves the content of the log file.
	 * 
	 * @param aLogFileName
	 *            name of log file.
	 * @return the content of the <i>aLogFileName</i> log file as a String
	 * @throws DIException
	 *             if an error occurs
	 */
	public static String getComponentLog(String aLogFileName) throws DIException {
		if (aLogFileName == null || aLogFileName.trim().length() == 0) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.GETCOMPONENTLOG.LOGFILENAME.PARAMETER.IS.NULL"));
		}

		StringBuffer log = null;
		File file = new File(aLogFileName);
		if (file.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				try {
					log = new StringBuffer("");
					String line;
					while ((line = in.readLine()) != null) {
						log.append(line);
						log.append(STR_NEW_LINE);
					}
				} finally {
					in.close();
				}
			} catch (Exception e) {
				APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.GETCOMPONENTLOG.ERROR.WHILE.READING.LOG.FILE",
						aLogFileName));
			}
		}

		if (log != null) {
			return log.toString();
		} else {
			return null;
		}
	}

	/**
	 * Retrieves the specified number of kilobytes from the end of the log file.
	 * 
	 * @param aLogFileName
	 *            name of the file
	 * @param aKilobytes
	 *            number of kilobytes
	 * @return the last aKilobytes of the content of aLogFileName log file as a
	 *         String
	 * @throws DIException
	 */
	public static String getComponentLogLastChunk(String aLogFileName, int aKilobytes) throws DIException {
		if (aLogFileName == null || aLogFileName.trim().length() == 0) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOGFILENAME.PARAMETER.IS.NULL"));
		}

		StringBuffer log = null;
		File file = new File(aLogFileName);
		if (file.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				try {
					long fileSize = file.length();
					long startSize = fileSize - aKilobytes * 1024L;
					if (startSize < 0) {
						startSize = 0;
					}
					long actuallySkipped = in.skip(startSize);
					if (actuallySkipped > 0) {
						in.readLine();
					}

					log = new StringBuffer("");
					String line;
					while ((line = in.readLine()) != null) {
						log.append(line);
						log.append(STR_NEW_LINE);
					}
				} finally {
					in.close();
				}
			} catch (Exception e) {
				APIEngine.logErrorAndThrowException(sResHash.getString(
						"SEVER.API.GETCOMPONENTLOGLASTCHUNK.ERROR.WHILE.READING.LOG.FILE", aLogFileName));
			}
		}

		if (log != null) {
			return log.toString();
		} else {
			return null;
		}

	}

	/**
	 * Retrieves the available component's log files names.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @return a String array containing all the available log files for the
	 *         components in the given Assembly Line
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static String[] getALLogFileNames(String aConfigId, String aALName) throws DIException {
		return getAvailableComponentLogFiles(AL_LOG_DIR_PREFIX, aConfigId, aALName);
	}

	/**
	 * Retrieves the specified number of available component's log files names.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @param iNumber
	 *            how many of the available log files you need
	 * @return a String array containing containing the first <i>number</i>
	 *         amount of the sorted by names according to the natural ordering
	 *         available log files for the components in the given Assembly Line
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static String[] getALLogFileNames(String aConfigId, String aALName, int iNumber) throws DIException {
		return getAvailableComponentLogFiles(AL_LOG_DIR_PREFIX, aConfigId, aALName, iNumber);
	}

	/**
	 * Retrieves the available component's log files names after the specified
	 * date.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @param dDate
	 *            the earliest Date for the logs needed
	 * @return a String array containing available log files for the components
	 *         in the given Assembly Line which are created after the given Date
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static String[] getALLogFileNames(String aConfigId, String aALName, Date dDate) throws DIException {
		return getAvailableComponentLogFiles(AL_LOG_DIR_PREFIX, aConfigId, aALName, dDate);
	}

	/**
	 * Retrieves the available component's log files names after the specified
	 * date.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @param startDate
	 *            the start Date for the logs needed
	 * @param endDate
	 *            the end Date for the logs needed
	 * @return a String array containing available log files for the components
	 *         in the given Assembly Line which are created after the given Date
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static String[] getALLogFileNames(String aConfigId, String aALName, Date startDate, Date endDate) throws DIException {
		return getAvailableComponentLogFiles(AL_LOG_DIR_PREFIX, aConfigId, aALName, startDate, endDate);
	}

	/**
	 * Retrieves the available log names, sorts them in ascending order
	 * according to the Natural Ordering and returns the last one.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @return The last log according to the Natural Ordering of the names
	 * @throws DIException
	 */
	public static String getALLastLogFileName(String aConfigId, String aALName) throws DIException {
		String logFileName = null;
		String[] files = getALLogFileNames(aConfigId, aALName);
		if (files != null && files.length > 0) {
			logFileName = files[files.length - 1];
		}
		return logFileName;
	}

	/**
	 * Retrieves the content of the AL log.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @param aLogFileName
	 *            the name of the log file
	 * @return The content of the Assembly Line log file as String
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static String getALLog(String aConfigId, String aALName, String aLogFileName) throws DIException {
		String logFileName = ROOT_LOG_DIR + aConfigId + DIR_SEP + AL_LOG_DIR_PREFIX + getCleanComponentName(aALName) + DIR_SEP
				+ aLogFileName;
		return getComponentLog(logFileName);
	}

	/**
	 * This method retrieves the specified number of kylobytes from the content
	 * of a AL's log file as String.
	 * 
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aALName
	 *            the Assembly Line name
	 * @param aLogFileName
	 *            the name of the log file
	 * @param aKilobytes
	 *            number of kilobytes.
	 * @return The last <i>aKilobytes</i> from the content of the Assembly Line
	 *         log file as String
	 * @throws DIException
	 */
	public static String getALLogLastChunk(String aConfigId, String aALName, String aLogFileName, int aKilobytes)
			throws DIException {
		String logFileName = ROOT_LOG_DIR + aConfigId + DIR_SEP + AL_LOG_DIR_PREFIX + getCleanComponentName(aALName) + DIR_SEP
				+ aLogFileName;
		return getComponentLogLastChunk(logFileName, aKilobytes);
	}

	/**
	 * Retrieves the available component's log files in ascending order.
	 * 
	 * @param aComponentTypeDir
	 *            the component type directory
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aComponentName
	 *            the component name
	 * @return a String array containing the sorted into ascending order
	 *         according to the natural ordering of the names of the available
	 *         log files for the given component
	 */
	public static String[] getAvailableComponentLogFiles(String aComponentTypeDir, String aConfigId, String aComponentName) {
		String[] logFiles = null;

		final String cleanCompName = getCleanComponentName(aComponentName);

		File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + cleanCompName);
		if (logDir.exists()) {
			logFiles = logDir.list(new FilenameFilter() {
				public boolean accept(File aDir, String aName) {
					if (!aName.startsWith(cleanCompName)) {
						return false;
					}
					if (!aName.endsWith(".log")) {
						return false;
					}
					return true;
				}
			});
		}

		if (logFiles != null) {
			Arrays.sort(logFiles);
		}

		return logFiles;
	}

	/**
	 * removes folder prefix (e.g. "AssemblyLines/")
	 * 
	 * @param aComponentName
	 *            name of the component
	 * @return the cleaned name.
	 */
	private static String getCleanComponentName(String aComponentName) {
		// removes folder prefix (e.g. "AssemblyLines/")
		String cleanName;
		if (aComponentName.indexOf("/") > -1) {
			cleanName = aComponentName.substring(aComponentName.lastIndexOf("/") + 1);
		} else {
			cleanName = aComponentName;
		}
		return cleanName;
	}

/**
	 * Replaces '/', ':', '\\', '*', '?', '"', '<', '>', '|' symbols with '_'
	 * from the given config id
	 * 
	 * @param aConfigId
	 *            configuration ID
	 * @return the modified config name.
	 */
	public static String getCleanConfigId(String aConfigId) {
		// a small performance optimization
		aConfigId = CONFIG_ID_INVALIDATORS.matcher(aConfigId).replaceAll("_");
		return aConfigId;
	}

	/**
	 * Cleans all logs(AL and EH) created after the given Date
	 * 
	 * @param aMinDate
	 *            after this date , the log are deleted
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static void cleanAllOldLogs(Date aMinDate) throws DIException {
		cleanAllOldALLogs(aMinDate);
	}

	/**
	 * Cleans all logs(AL and EH) and leaves only <i>aKeepNum</i> of them
	 * 
	 * @param aKeepNum
	 *            number of logs to keep.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static void cleanAllOldLogs(int aKeepNum) throws DIException {
		cleanAllOldALLogs(aKeepNum);
	}

	/**
	 * Cleans all AL logs created after the given Date
	 * 
	 * @param aMinDate
	 *            after this date , the log are deleted
	 * @throws DIException
	 *             if an error occurs.
	 */
	public static void cleanAllOldALLogs(Date aMinDate) throws DIException {
		deleteAllOldCompLogs(COMP_TYPE_AL, aMinDate);
	}

	/**
	 * Cleans all AL logs and leaves only <i>aKeepNum</i> of them
	 * 
	 * @param aKeepNum
	 *            number of logs to keep
	 * @throws DIException
	 *             if an error occurs
	 */
	public static void cleanAllOldALLogs(int aKeepNum) throws DIException {
		deleteAllOldCompLogs(COMP_TYPE_AL, aKeepNum);
	}

	/**
	 * Cleans AL logs, for a given config ID and Assembly Line name, created
	 * after the given Date
	 * 
	 * @param aConfigId
	 *            configuration ID
	 * @param aALName
	 *            name of the assembly line
	 * @param aMinDate
	 *            after this date , the log are deleted
	 * @return whether the operation was successful
	 * @throws DIException
	 */
	public static Boolean cleanOldALLogs(String aConfigId, String aALName, Date aMinDate) throws DIException {
		return deleteComponentOldLogs(AL_LOG_DIR_PREFIX, aConfigId, aALName, aMinDate);
	}

	/**
	 * Cleans AL logs, for a given config ID and Assembly Line name, created
	 * after the given Date
	 * 
	 * @param aConfigId
	 *            configuration ID
	 * @param aALName
	 *            name of the assembly line
	 * @param aMinDate
	 *            after this date , the log are deleted
	 * @param aMaxDate
	 *            before this date , the log are deleted
	 * @return whether the operation was successful
	 * @throws DIException
	 */
	public static Boolean cleanOldALLogs(String aConfigId, String aALName, Date aMinDate, Date aMaxDate) throws DIException {
		return deleteComponentOldLogs(AL_LOG_DIR_PREFIX, aConfigId, aALName, aMinDate, aMaxDate);
	}

	/**
	 * Cleans AL logs, for a given config ID and Assembly Line name, and leaves
	 * only <i>aKeepNum</i> of them
	 * 
	 * @param aConfigId
	 *            configuration ID
	 * @param aALName
	 *            name of the assembly line
	 * @param aKeepNum
	 *            number of logs to keep
	 * @return whether the operation was successful
	 * @throws DIException
	 */
	public static Boolean cleanOldALLogs(String aConfigId, String aALName, int aKeepNum) throws DIException {
		return deleteComponentOldLogs(AL_LOG_DIR_PREFIX, aConfigId, aALName, aKeepNum);
	}

	/**
	 * Deletes old logs and leaves at most <i>aKeepNum</i> of them
	 * 
	 * @param aComponentTypeDir
	 *            the directory prefix for the type of component
	 * @param aConfigId
	 *            the configuration ID
	 * @param aComponentName
	 *            the component name
	 * @param aKeepNum
	 *            how many logs should be left at most after the operation
	 *            completes
	 * @return whether the operation was successful
	 * @throws DIException
	 *             if an error occurs.
	 */
	private static Boolean deleteComponentOldLogs(String aComponentTypeDir, String aConfigId, String aComponentName, int aKeepNum)
			throws DIException {
		if (aComponentName == null || aComponentName.length() == 0) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.DELETECOMPONENTOLDLOGS.COMPONENT.NAME.PARAMETER.IS.NULL.1"));
		}

		Boolean componentLogsExist = Boolean.TRUE;
		String[] logFiles = getAvailableComponentLogFiles(aComponentTypeDir, aConfigId, aComponentName);
		if (logFiles != null && aKeepNum < logFiles.length) {
			File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + getCleanComponentName(aComponentName));
			String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
			for (int i = 0; i < logFiles.length - aKeepNum; i++) {
				File logFile = new File(logDirAbsPath + logFiles[i]);
				if (!logFile.delete())
					componentLogsExist = Boolean.FALSE;
			}
		}

		return componentLogsExist;
	}

	/**
	 * Deletes old logs created after a given Date
	 * 
	 * @param aComponentTypeDir
	 *            the directory prefix for the type of component
	 * @param aConfigId
	 *            the configuration ID
	 * @param aComponentName
	 *            the component name
	 * @param aMinDate
	 *            logs created after this Date will be deleted
	 * @return whether the operation was successful
	 * @throws DIException
	 *             if an error occurs.
	 */
	private static Boolean deleteComponentOldLogs(String aComponentTypeDir, String aConfigId, String aComponentName, Date aMinDate)
			throws DIException {

		return deleteComponentOldLogs(aComponentTypeDir, aConfigId, aComponentName, aMinDate, null);
	}

	/**
	 * Deletes old logs which satisfy the Date range. If the
	 * <code>aMaxDate</code> is <code>null</code> then the method functions gets
	 * the log files which are generated after the <code>aMinDate</code>
	 * 
	 * @param aComponentTypeDir
	 *            the directory prefix for the type of component
	 * @param aConfigId
	 *            the configuration ID
	 * @param aComponentName
	 *            the component name
	 * @param aMinDate
	 *            logs created after this Date will be deleted
	 * @param aMaxDate
	 *            logs created before this Date will be deleted if this param is
	 *            not null
	 * @return whether the operation was successful
	 * @throws DIException
	 *             if an error occurs.
	 */
	private static Boolean deleteComponentOldLogs(String aComponentTypeDir, String aConfigId, String aComponentName, Date aMinDate,
			Date aMaxDate) throws DIException {
		if (aComponentName == null || aComponentName.length() == 0) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.DELETECOMPONENTOLDLOGS.COMPONENT.NAME.PARAMETER.IS.NULL.2"));
		}
		if (aMinDate == null) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.DELETECOMPONENTOLDLOGS.MIN.DATE.PARAMETER.IS.NULL"));
		}

		Boolean componentLogsExist = Boolean.TRUE;
		String[] logFiles = getAvailableComponentLogFiles(aComponentTypeDir, aConfigId, aComponentName);
		if (logFiles != null) {
			File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + getCleanComponentName(aComponentName));
			String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
			for (int i = 0; i < logFiles.length; i++) {
				File logFile = new File(logDirAbsPath + logFiles[i]);
				Date logFileDate = new Date(logFile.lastModified());
				boolean isAfterMinDate = aMinDate.after(logFileDate);
				boolean isBeforeMaxDate = false;
				boolean isLogFileDeleted = false;
				if (aMaxDate != null) {
					isBeforeMaxDate = aMaxDate.before(logFileDate);
					if ((isAfterMinDate && isBeforeMaxDate)) {
						isLogFileDeleted = logFile.delete();
						componentLogsExist = Boolean.valueOf(isLogFileDeleted);
					}
				} else {
					if (isAfterMinDate) {
						isLogFileDeleted = logFile.delete();
						componentLogsExist = Boolean.valueOf(isLogFileDeleted);
					}
				}
			}
		}

		return componentLogsExist;
	}

	/**
	 * Deletes all logs for a given component type and after a given Date
	 * 
	 * @param aComponentType
	 *            should be 0 for AL component , otherwise an exception is
	 *            thrown
	 * @param aCriteria
	 *            after this date , the log are deleted
	 * @throws DIException
	 *             if an error occurs.
	 */
	private static void deleteAllOldCompLogs(int aComponentType, Date aCriteria) throws DIException {
		String compPref = null;
		if (aComponentType == COMP_TYPE_AL) {
			compPref = AL_LOG_DIR_PREFIX;
		} else {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.DELETEALLOLDCOMPLOGS.INVALID.COMPONENET.TYPE.1",
					Integer.toString(aComponentType)));
		}

		File rootLogDir = new File(ROOT_LOG_DIR);
		String[] configs = rootLogDir.list();
		if (configs != null) {
			for (int j = 0; j < configs.length; j++) {
				File logDir = new File(ROOT_LOG_DIR + configs[j]);
				String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
				String[] files = logDir.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						File file = new File(logDirAbsPath + files[i]);
						if (file.isDirectory() && files[i].startsWith(compPref) && aComponentType == COMP_TYPE_AL) {
							cleanOldALLogs(configs[j], files[i].substring(compPref.length()), aCriteria);
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes all logs for a given component type and leaves a specified amount
	 * of them
	 * 
	 * @param aComponentType
	 *            should be 0 for AL component type , othewise an exception is
	 *            thrown
	 * @param aCriteria
	 *            number of logs to keep
	 * @throws DIException
	 *             if an error occurs
	 */
	private static void deleteAllOldCompLogs(int aComponentType, int aCriteria) throws DIException {
		String compPref = null;
		if (aComponentType == COMP_TYPE_AL) {
			compPref = AL_LOG_DIR_PREFIX;
		} else {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.DELETEALLOLDCOMPLOGS.INVALID.COMPONENET.TYPE.2",
					Integer.toString(aComponentType)));
		}

		File rootLogDir = new File(ROOT_LOG_DIR);
		String[] configs = rootLogDir.list();
		if (configs != null) {
			for (int j = 0; j < configs.length; j++) {
				File logDir = new File(ROOT_LOG_DIR + configs[j]);
				String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
				String[] files = logDir.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						File file = new File(logDirAbsPath + files[i]);
						if (file.isDirectory() && files[i].startsWith(compPref) && aComponentType == COMP_TYPE_AL) {
							cleanOldALLogs(configs[j], files[i].substring(compPref.length()), aCriteria);
						}
					}
				}
			}
		}
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
	public static String cleanALLogs(String aConfigId, String aALName, String[] logsToBeDeleted) throws DIException {
		Boolean logsDeleted = null;
		StringBuffer logFilesNotDeleted = new StringBuffer();
		for (int iIndex = 0; iIndex < logsToBeDeleted.length; iIndex++) {
			String logToBeDeleted = logsToBeDeleted[iIndex];
			logsDeleted = deleteALLog(aConfigId, aALName, logToBeDeleted);
			// If the logsDeleted variable is null means that the log file is
			// not deleted for some
			// /reason. We add this log file name to the "logFilesNotDelted"
			// comma separated list.
			if (logsDeleted == null) {
				logFilesNotDeleted.append(logToBeDeleted);

				logFilesNotDeleted.append(",");
			}
		}

		return logFilesNotDeleted.toString();
	}

	/**
	 * Deletes the log file which are specified by "<code>logToBeDeleted</code>
	 * ".
	 * 
	 * @param configId
	 *            identification of the AssemblyLine's Config Instance.
	 * @param aALName
	 *            the name of the AssemblyLine whose logs will be cleaned up.
	 * @param logToBeDeleted
	 *            name of the log file to be deleted.
	 * @return a <code>Boolean</code> that holds <code>true</code> if the log
	 *         files are deleted successfully; else <code>null</code> if there
	 *         is an error deleting the log file.
	 * 
	 * @throws DIException
	 *             if an error occurs while deleting log files.
	 */
	private static Boolean deleteALLog(String configId, String aALName, String logToBeDeleted) throws DIException {
		String cleanCompName = getCleanComponentName(aALName);
		File logFile = new File(ROOT_LOG_DIR + configId + DIR_SEP + AL_LOG_DIR_PREFIX + cleanCompName + DIR_SEP + logToBeDeleted);
		boolean deleted = logFile.delete();
		return Boolean.valueOf(deleted);
	}

	/**
	 * Retrieves the specified number of available component's log files sorted
	 * in ascending order.
	 * 
	 * @param aComponentTypeDir
	 *            the component type directory
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aComponentName
	 *            the component name
	 * @param number
	 *            how many of the available log files you need
	 * @return a String array containing the first <i>number</i> amount of the
	 *         sorted into ascending order according to the natural ordering of
	 *         the names of the available log files for the given component
	 */
	public static String[] getAvailableComponentLogFiles(String aComponentTypeDir, String aConfigId, String aComponentName,
			int number) {
		String[] logFiles2 = null;
		String[] logFiles = new String[number];
		final String cleanCompName = getCleanComponentName(aComponentName);

		File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + cleanCompName);
		if (logDir.exists()) {
			logFiles2 = logDir.list(new FilenameFilter() {
				public boolean accept(File aDir, String aName) {
					if (!aName.startsWith(cleanCompName)) {
						return false;
					}
					if (!aName.endsWith(".log")) {
						return false;
					}
					return true;
				}
			});

		}

		if (logFiles2 != null) {
			Arrays.sort(logFiles2);

			// If number of available log files less then the specified number
			// return all log files.
			if (logFiles2.length < number) {
				return logFiles2;
			} else {
				System.arraycopy(logFiles2, 0, logFiles, 0, number);
			}
		}

		return logFiles;
	}

	/**
	 * Retrieves the available component's log files after the specified date
	 * sorted in ascending order.
	 * 
	 * @param aComponentTypeDir
	 *            the component type directory
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aComponentName
	 *            the component name
	 * @param dDate
	 *            the earliest Date for the logs needed
	 * @return a String array containing available log files for the given
	 *         component which are created after the given Date, sorted into
	 *         ascending order according to the natural ordering of their names
	 */
	public static String[] getAvailableComponentLogFiles(String aComponentTypeDir, String aConfigId, String aComponentName,
			Date dDate) {
		String[] logFiles = null;
		ArrayList<String> logFiles2 = new ArrayList<String>();
		logFiles = getAvailableComponentLogFiles(aComponentTypeDir, aConfigId, aComponentName);
		if (logFiles != null) {
			File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + getCleanComponentName(aComponentName));
			String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
			for (int i = 0; i < logFiles.length; i++) {
				File logFile = new File(logDirAbsPath + logFiles[i]);
				if (dDate.after(new Date(logFile.lastModified()))) {
					logFiles2.add(logFiles[i]);
				}
			}

		}
		String[] retArray = new String[logFiles2.size()];
		return (String[]) logFiles2.toArray(retArray);
	}

	/**
	 * Retrieves the available component's log files after the specified date
	 * sorted in ascending order.
	 * 
	 * @param aComponentTypeDir
	 *            the component type directory
	 * @param aConfigId
	 *            the ID of the configuration the Assembly Line belongs to
	 * @param aComponentName
	 *            the component name
	 * @param startDate
	 *            the start Date for the logs needed
	 * @param endDate
	 *            the end Date for the logs needed
	 * @return a String array containing available log files for the given
	 *         component which are created after the given Date, sorted into
	 *         ascending order according to the natural ordering of their names
	 */
	private static String[] getAvailableComponentLogFiles(String aComponentTypeDir, String aConfigId, String aComponentName,
			Date startDate, Date endDate) {
		String[] logFiles = null;
		ArrayList<String> logFiles2 = new ArrayList<String>();
		logFiles = getAvailableComponentLogFiles(aComponentTypeDir, aConfigId, aComponentName);
		if (logFiles != null) {
			File logDir = new File(ROOT_LOG_DIR + aConfigId + DIR_SEP + aComponentTypeDir + getCleanComponentName(aComponentName));
			String logDirAbsPath = logDir.getAbsolutePath() + DIR_SEP;
			for (int i = 0; i < logFiles.length; i++) {
				File logFile = new File(logDirAbsPath + logFiles[i]);
				if (startDate.before(new Date(logFile.lastModified())) && endDate.after(new Date(logFile.lastModified()))) {
					logFiles2.add(logFiles[i]);
				}
			}

		}
		String[] retArray = new String[logFiles2.size()];
		return (String[]) logFiles2.toArray(retArray);
	}
}
