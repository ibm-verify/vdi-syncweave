/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2011, 2011
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
package com.ibm.di.log;

import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;

import com.ibm.di.server.ResourceHash;

/**
 * This class extends the {@link DailyRollingFileAppender} and adds an
 * additional parameter to its configuration – 'Number of files'. By specifying
 * this parameter the user can limit the number of the created backup log files.
 */
public class FixedDailyRollingFileAppender extends DailyRollingFileAppender {
	/**
	 * Number of backup log files.
	 */
	private int fileCount = 0;

	/**
	 * Path to the log file.
	 */
	private String filePath = null;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "fixeddailyroller";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Constructor.
	 */
	public FixedDailyRollingFileAppender() {
		super();
		
	}

	@Override
	public void activateOptions() {
		if (filePath == null)
			filePath = getFile();
		super.activateOptions();
	}

	/**
	 * Close the underlying Writer. We override this method in order to
	 * implement the special behavior of the FixedDailyRollingFileAppender -
	 * limiting the number of backup log files.
	 */
	@Override
	protected void closeWriter() {
		File logFile = new File(filePath);

		// List all the files in the folder of the log file
		File[] allFiles = logFile.getParentFile().listFiles();
		TreeMap<Long, File> logFiles = new TreeMap<Long, File>();

		for (File currentFile : allFiles) {

			// Get files whose names start with the log filename (except it).
			if (currentFile.getName().startsWith(logFile.getName())
					&& currentFile.getName().length() == 
						(getDatePattern().length() + logFile.getName().length())
					&& !currentFile.getName().equals(logFile.getName()))

				// Put the lastModified attribute and the file in
				// TreeMap object which sorts by the key.
				logFiles.put(currentFile.lastModified(), currentFile);
		}
		
		// Determine the number of the files we need to delete
		int numToDelFiles = logFiles.size() - fileCount;
		int numDelFiles = 0;

		// Delete first numToDelFiles number of log files
		for (Iterator<Long> i = logFiles.keySet().iterator(); i.hasNext()
				&& numDelFiles < numToDelFiles;) {
			Long key = i.next();

			if (logFiles.get(key).exists() && !logFiles.get(key).delete()) {
				Logger
						.getRootLogger()
						.error(
								sResHash
										.getString(
												"MISERVER.FIXEDDAILYROLLINGFILEAPPENDER.CANNOT.DELETE.FILE",
												logFiles.get(key).getName()));
				System.out.println("Could not delete file!");
			}
			numDelFiles++;
		}
		allFiles = null;
		logFiles = null;

		super.closeWriter();
	}

	/**
	 * Sets number of backup log files.
	 * <p>
	 * This method is called by the
	 * {@link TDILog4j#addAppender(com.ibm.di.config.interfaces.LogConfigItem, java.util.Map)}
	 * method. It calls the 'set+param_name' named methods for the every
	 * parameter for every added file appender.
	 * <p>
	 * So if your new parameter added to the tdi.xml file is called 'FileCount'
	 * then you should implement 'setFileCount' method in order to get the
	 * actual value typed by the user in the UI and use it in your internal
	 * logic.
	 * 
	 * @param count
	 *            number of files
	 */
	public void setFileCount(int count) {
		if (count < 0)
			throw new NumberFormatException();
		fileCount = count;
	}
}
