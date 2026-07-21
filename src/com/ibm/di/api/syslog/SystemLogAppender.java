/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.syslog;

import java.io.File;
import java.util.Date;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.jmx.mbeans.AssemblyLine;
import com.ibm.di.server.RSInterface;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * This class extend the {@link FileAppender} class and defines API for writing
 * logs to system files.
 */
public class SystemLogAppender {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Pattern for date conversion.
	 */
	private static final String DATE_TIME_PATTERN = "_yyyy_MM_dd__HH_mm_ss_SSS";

	/**
	 * Formats the date.
	 */
	private static final SimpleDateFormat mDateFormatter = new SimpleDateFormat(
			DATE_TIME_PATTERN);

	/**
	 * Specified component name.
	 */
	private String mComponentName = null;

	/**
	 * Specified file name.
	 */
	private String mFileName = null;

	/**
	 * Absolute file name for log
	 */
	private String absoluteFileName;

	/**
	 * Configuration ID.
	 */
	private String mConfigId = null;
	
	/**
	 * Maximum generations of log files.
	 */
	private int maxGenerations;

	/**
	 * Default constructor
	 * 
	 * @throws Exception:
	 *             never
	 */
	public SystemLogAppender() throws Exception {
	}

	/**
	 * Sets the name of the component.
	 * 
	 * @param aComponentName
	 *            String , {@link AssemblyLine}
	 * @throws Exception :
	 *             never.
	 */
	public void setComponentName(String aComponentName) throws Exception {
		mComponentName = getCleanComponentName(aComponentName);
	}

	/**
	 * Retrieves component name.
	 * @return String. The component name.
	 * @throws Exception :
	 *             never.
	 */
	public String getComponentName() throws Exception {
		return mComponentName;
	}

	/**
	 * Sets the configuration id.
	 * 
	 * @param aConfigId
	 *            String
	 * @throws Exception :
	 *             never.
	 */
	public void setConfigId(String aConfigId) throws Exception {
		mConfigId = aConfigId;
	}

	/**
	 * Sets the configuration id using a configInstance.
	 * 
	 * @param configInstance
	 *            object implementing {@link RSInterface}.
	 * @throws Exception
	 *             if configuration cannot be set.
	 */
	public void setConfigInstance(Object configInstance) throws Exception {
		mConfigId = APIEngine.getConfigId((RSInterface) configInstance);
	}

	/**
	 * Retrieves configuration ID. 
	 * @return the configuration id.
	 * @throws Exception :
	 *             never
	 */
	public String getConfigId() throws Exception {
		return mConfigId;
	}

	/**
	 * Retrieves file name.
	 * @return String, the file name.
	 */
	public String getFileName() {
		return mFileName;
	}

	/**
	 * Sets max generations of log files
	 * @param number
	 */
	public void setMaxGenerations(String number) {
		if (number == null || number.length() == 0)
			return;
		try {
			maxGenerations = Integer.parseInt(number);
		} catch (NumberFormatException nfe) {
			maxGenerations = 0;
		}
	}

	/**
	 * Creates a simple and absolute file name with the name of the component
	 * and current date in the root directory specified by the {@link LogUtils}
	 * class.
	 * 
	 * @throws Exception
	 *             if the component type has not been assigned.
	 */
	public void generateFileName() throws Exception {
		// construct simple file name
		mFileName = mComponentName + mDateFormatter.format(new Date()) + ".log";

		// construct absolute file name
		String componentLogDir = LogUtils.ROOT_LOG_DIR + mConfigId + "/"
				+ LogUtils.AL_LOG_DIR_PREFIX;

		File file = new File(componentLogDir + mComponentName, mFileName);

		absoluteFileName = file.getAbsolutePath();
	}

	/**
	 * Returns absolute path for the log file.
	 * @return
	 */
	public String getFile() {
		return absoluteFileName;
	}

	/**
	 * Removes folder prefix (e.g. "AssemblyLines/")
	 * 
	 * @param aComponentName
	 *            String
	 * @return the clean component name.
	 */
	private static String getCleanComponentName(String aComponentName) {
		// remove folder prefix (e.g. "AssemblyLines/")
		String cleanName;
		if (aComponentName.indexOf("/") > -1) {
			cleanName = aComponentName.substring(aComponentName
					.lastIndexOf("/") + 1);
		} else {
			cleanName = aComponentName;
		}
		return cleanName;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}
}
