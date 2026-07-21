/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.migamc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

public class MigrateAmcProperties {

	private static final String AMC_PROPERTIES_UPDATED = "amc.properties.updated";

	private static final String AMC_PROPERTIES_REMOVED = "amc.properties.removed";

	private static final String PROPERTY_DESC = ".comment";

	private static final String PROPERTY_DELIMITER = ",";

	private static final String AMC_PROPERTIES_ADDED = "amc.properties.added";

	private static final String MIGRATION_XML_PROPS_FILE = "migrateamcprops.xml";

	/**
	 * Holds the copyright for the program
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constant signifying to log a error message.
	 */
	private static final int ERROR = 4;

	/**
	 * Constant signifying to log a warning message.
	 */
	private static final int WARN = 3;

	/**
	 * Constant signifying to log a info message.
	 */
	private static final int INFO = 2;

	/**
	 * Constant signifying to log a debug message.
	 */
	private static final int DEBUG = 1;

	/**
	 * Constant specifiying if the program is going to run in verbose mode.
	 */
	private static boolean VERBOSE_MODE = false;

	/**
	 * The log4j logger for the command.
	 */
	private static Logger logger = null;

	/**
	 * Saves whether an illegal parameter was passed into the command.
	 */
	private static boolean bIllegalCommandUsage = false;

	/**
	 * Resource bundle (Locale specific) Filename: migrategblprops.properties.
	 * Generated from TMS XML file migrategblprops.xml
	 */
	private static ResourceHash resHash = ResourceHash
			.getHash("migrateamcprops");

	/**
	 * Constant specifies the command completed successfully.
	 */
	private static final int RC_OK = 0;

	/**
	 * Constant specifies the command failed.
	 */
	private static final int RC_FAIL = -1;

	/**
	 * Hashtable to store the Options passed to the program.
	 */
	private static Hashtable<String, String> generalOptions = null;

	/**
	 * Tracks the of number of arguments processed.
	 */
	private static int argumentsProcessed = -1;

	/**
	 * Tracks if the usage statement should be printed.
	 */
	private static boolean b_SHOW_HELP = false;

	// The options..
	/**
	 * The option for the name of the file to migrate
	 */
	private static final String GEN_OPT_FILE_MIG = "-f";

	/**
	 * The option for the name of the backup name to give the file if a backup
	 * is to be done.
	 */
	private static final String GEN_OPT_FILE_BACKUP = "-b";

	/**
	 * Verbose mode
	 */
	private static final String GEN_OPT_VERBOSE = "-v";

	/**
	 * Option to print the usage statement.
	 */
	private static final String HELP_OPTION = "-?";

	/**
	 * The hidden option for the location of TDI. Passed in by the calling
	 * wrapper script.
	 */
	private static final String GEN_OPT_TDI_INSTALL = "-i";

	/**
	 * The hidden option for the location of JRE used by the tool. Passed in by
	 * the calling wrapper script.
	 */
	private static final String GEN_OPT_JRE_INSTALL = "-j";

	/**
	 * Option to specify a new file name to migrate the file to.
	 */
	private static final String GEN_OPT_NEW_FILE = "-n";

	/**
	 * Holds the end of line character that will be used for new lines added to
	 * the properties file.
	 */
	private static String eol = "";

	/**
	 * Holds the file separator for the os the program is running on.
	 */
	private static String fs = System.getProperty("file.separator");

	/**
	 * Holds the install directory of TDI. Basically, the value for the -i
	 * option.
	 */
	private static String targetDir = "";

	/**
	 * Holds the install directory of JRE used by TDI or the specific tool.
	 * Basically, the value for the -j option.
	 */
	private static String targetDirJre = "";

	/**
	 * This is the Properties object which caches all the instructions for
	 * migrating the properties file
	 */
	private static Properties propsMigrateAMC = null;

	/**
	 * Used to send messages out to user. Based on passed paramters, this may
	 * send the messages to the console (default) or to a file.
	 * 
	 * @param string
	 *            The message to be sent. Should be localized.
	 */
	private static void message(String string) {
		System.out.println(string);
	}

	/**
	 * Logs a message.
	 * 
	 * @param level
	 *            The log level to log at. Valid constants are DEBUG, ERROR,
	 *            WARN, and INFO.
	 * @param message
	 *            The message to be logged.
	 */
	private static void log(int level, String message) {
		log(level, message, null);
	}

	/**
	 * Logs an exception
	 * 
	 * @param level
	 *            The log level to log at. Valid constants are DEBUG, ERROR,
	 *            WARN, and INFO.
	 * @param e
	 *            The exception to be logged.
	 */
	private static void log(int level, Exception e) {
		log(level, null, e);
	}

	/**
	 * Logs a a message and an exception.
	 * 
	 * @param level
	 *            The log level to log at. Valid constants are DEBUG, ERROR,
	 *            WARN, and INFO.
	 * @param message
	 *            The message to be logged.
	 * @param e
	 *            The exception to be logged.
	 */
	private static void log(int level, String message, Exception e) {
		// If verbose mode is on, then print everything on console.

		if (VERBOSE_MODE || level == ERROR || level == WARN) {
			if (message != null)
				System.out.println(message);
			if (e != null)
				System.out.println(e.toString());
		}

		switch (level) {
		case ERROR:
			if (message != null)
				logger.error(message);
			if (e != null)
				logger.error(getStackTrace(e));
			break;
		case WARN:
			if (message != null)
				logger.warn(message);
			if (e != null)
				logger.warn(getStackTrace(e));
			break;
		case INFO:
			if (message != null)
				logger.info(message);
			if (e != null)
				logger.info(getStackTrace(e));
			break;
		case DEBUG:
			if (message != null)
				logger.debug(message);
			if (e != null)
				logger.debug(getStackTrace(e));
			break;
		}
	}

	private static String getStackTrace(Exception e) {
		StackTraceElement[] stElements = e.getStackTrace();
		StringBuffer trace = new StringBuffer();
		String tmp = e.getMessage();
		trace.append(tmp);
		for (int i = 0; i < stElements.length; i++) {
			trace.append("\n\t").append(stElements[i].toString());
		}
		return trace.toString();
	}

	/**
	 * Parses the general options. After parsing, the general options and their
	 * corresponding values are set in the hashtable <code>generalOptions</code>
	 * . The number of arguments that have been processed and done with are set
	 * in <code>argumentsProcessed</code>. This is done so that the code which
	 * wishes to parse the later part of the code knows from which index of the
	 * array to continue from.
	 * 
	 * @param args
	 *            The arguments passed to the program.
	 * 
	 * @throws IllegalCommandUsageException
	 *             This exception is thrown whenever the method encounters an
	 *             option that it does not recognize, or an option that is
	 *             passed TWICE, or a parameter that was expected but not found
	 *             in the correct position.
	 * 
	 * @see #generalOptions
	 * @see #argumentsProcessed
	 * 
	 */
	private static void parseGeneralOptions(String args[])
			throws IllegalCommandUsageException {

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.PARSING.OPTIONS"));
		}

		String currentArgument;
		// Flag to indicate whether the OPERATION_SWITCH was found.
		boolean operationSwitchFound = false;
		// Flag to indicate whether the GEN_OPT_TDI_INSTALL was found.
		boolean operationTDIInstallFound = false;
		// Flag to indicate whether the GEN_OPT_JRE_INSTALL was found.
		boolean operationJREFound = false;

		// Loop through the arguments
		for (int i = 0; i < args.length; i++) {
			currentArgument = args[i];

			// see if its an option recognized
			if (currentArgument.equals(GEN_OPT_FILE_MIG) || // File to migrate
					currentArgument.equals(GEN_OPT_FILE_BACKUP) || // Name to
					// give the
					// backup
					// file it
					// its going
					// to be
					// backed
					// up.
					currentArgument.equals(GEN_OPT_JRE_INSTALL) || // The
					// location
					// of the
					// JRE that
					// is being
					// used.
					currentArgument.equals(GEN_OPT_TDI_INSTALL) || // The
					// location
					// of the
					// JRE
					// install.
					currentArgument.equals(GEN_OPT_NEW_FILE) // The location
			// to migrate
			// the file to.
			) {
				putInGeneralOptionsTable(currentArgument, args, i);
				i++;
				argumentsProcessed++;
				// if this was the required parameter, make sure we remember.
				if (currentArgument.equals(GEN_OPT_FILE_MIG)) {
					operationSwitchFound = true;
				} else if (currentArgument.equals(GEN_OPT_TDI_INSTALL)) {
					operationTDIInstallFound = true;
				} else if (currentArgument.equals(GEN_OPT_JRE_INSTALL)) {
					operationJREFound = true;
				}
			} else if (currentArgument.equals(GEN_OPT_VERBOSE)) // VERBOSE MODE
			{
				if (generalOptions.containsKey(GEN_OPT_VERBOSE)) {
					throw new IllegalCommandUsageException(resHash.getString(
							"OPT_OCCUR_TWICE", GEN_OPT_VERBOSE));
				}
				generalOptions.put(currentArgument, "true");
				VERBOSE_MODE = true;
			} else if (currentArgument.equals(HELP_OPTION)) {
				b_SHOW_HELP = true;
				break;
			} else { // UNKNOWN GENERAL OPTION.
				throw new IllegalCommandUsageException(resHash.getString(
						"UNKNOWN_OPT", currentArgument));
			}
		}

		// We have an error if a required parameter is missing.
		if (b_SHOW_HELP == false) {
			if (operationSwitchFound == false) {
				throw new IllegalCommandUsageException(resHash.getString(
						"FILE_OPTION_ABSENT", GEN_OPT_FILE_MIG));
			} else if (operationTDIInstallFound == false) {
				throw new IllegalCommandUsageException(resHash.getString(
						"FILE_OPTION_ABSENT", GEN_OPT_TDI_INSTALL));
			} else if (operationJREFound == false) {
				throw new IllegalCommandUsageException(resHash.getString(
						"FILE_OPTION_ABSENT", GEN_OPT_JRE_INSTALL));
			}
		}

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.GENERAL.OPTIONS", generalOptions
					.toString()));
		}
	}

	/**
	 * Method puts the option in the internal arguments table.
	 * 
	 * @param args
	 *            The arguments passed to the program.
	 * 
	 * @throws IllegalCommandUsageException
	 *             This exception is thrown whenever the method encounters an
	 *             option that it does not recognize, or an option that is
	 *             passed TWICE, or a parameter that was expected but not found
	 *             in the correct position.
	 * 
	 * @see #generalOptions
	 * @see #argumentsProcessed
	 * 
	 */
	private static void putInGeneralOptionsTable(String generalOption,
			String args[], int currentCounter)
			throws IllegalCommandUsageException {
		if (generalOptions.containsKey(generalOption)) {
			throw new IllegalCommandUsageException(resHash.getString(
					"OPT_OCCUR_TWICE", generalOption));
		}

		// ensure the next argument is a value.
		if (checkIfNextArgIsValue(args, currentCounter) == false) {
			String tmpArg = args[currentCounter];
			if (tmpArg.equals(GEN_OPT_FILE_MIG)
					|| tmpArg.equals(GEN_OPT_FILE_BACKUP)
					|| tmpArg.equals(GEN_OPT_TDI_INSTALL)
					|| tmpArg.equals(GEN_OPT_JRE_INSTALL)
					|| tmpArg.equals(GEN_OPT_NEW_FILE)) {
				log(ERROR, resHash.getString("OPT_VAL_UNSPECIFIED", tmpArg));
			}
			throw new IllegalCommandUsageException(resHash
					.getString("ILLEGAL_COMMAND_USAGE"));
		}

		String val = args[currentCounter + 1];
		generalOptions.put(generalOption, val);
	}

	/**
	 * Check's if the next argument is a value and not another option.
	 * 
	 * @param args
	 *            An array of arguments.
	 * @param currentCounter
	 *            To decide which is the NEXT index.
	 * 
	 * @return If the next is a "value" then returns true. If the next is an
	 *         "option" then returns false. If the next is null, then also
	 *         returns a false.
	 */
	private static boolean checkIfNextArgIsValue(String args[],
			int currentCounter) {
		if (currentCounter >= ((args.length) - 1)) {
			log(
					WARN,
					resHash
							.getString("COMMAND.ALREADY.REACHED.THE.END.OF.ARGUMENT.LIST"));
			return false;
		}

		// Check if it is one of the options
		String nextValue = args[currentCounter + 1];

		if (nextValue.equals(GEN_OPT_FILE_MIG)
				|| nextValue.equals(GEN_OPT_FILE_BACKUP)
				|| nextValue.equals(GEN_OPT_VERBOSE)
				|| nextValue.equals(HELP_OPTION)
				|| nextValue.equals(GEN_OPT_TDI_INSTALL)
				|| nextValue.equals(GEN_OPT_JRE_INSTALL)
				|| nextValue.equals(GEN_OPT_NEW_FILE)
				|| nextValue.startsWith("-")) {
			return false;
		}

		return true;
	}

	/**
	 * This method is responsible for initializing the
	 * 
	 * @param pathOfPropertiesXMLFile
	 */
	private static void initializeMigration(String pathOfPropertiesXMLFile) {
		InputStream in = null;
		log(INFO, resHash.getString(
				"INITIALIZING_MIGRATION_LOADING_PROPERTIES",
				pathOfPropertiesXMLFile));
		try {
			in = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(pathOfPropertiesXMLFile);
			initializeMigration(in);
		} catch (Exception e) {
			log(ERROR, resHash.getString("ERROR_WHILE_INITIALIZING_MIGRATION",
					e.getMessage()), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log(WARN, e.getMessage());
				}
			}
		}

	}

	private static void initializeMigration(InputStream in) {
		try {
			propsMigrateAMC = new Properties();
			propsMigrateAMC.loadFromXML(in);
		} catch (IOException e) {
			log(ERROR, resHash.getString("ERROR_WHILE_INITIALIZING_MIGRATION",
					e.getMessage()), e);
		}

	}

	/**
	 * This method is used for adding the new properties which are added in the
	 * latest release.
	 * 
	 * @param strbuf
	 */
	private static void performAdditions(StringBuffer strbuf) {
		log(INFO, resHash.getString("ADDING_NEW_PROPERTIES"));
		String newProperties = propsMigrateAMC
				.getProperty(AMC_PROPERTIES_ADDED);
		List<String> lstNewProperties = tokenizeProperties(newProperties);
		if (lstNewProperties != null) {
			for (int iIndex = 0; iIndex < lstNewProperties.size(); iIndex++) {
				String propName = lstNewProperties.get(iIndex);
				String propValue = propsMigrateAMC.getProperty(propName);
				String propdesc = propsMigrateAMC.getProperty(propName
						+ PROPERTY_DESC);
				if (strbuf.indexOf(propName) == -1) {
					if (propdesc != null) {
						strbuf.append(propdesc);
						strbuf.append(eol);
					}
					strbuf.append(propName);
					strbuf.append("=");
					strbuf.append(propValue);
					strbuf.append(eol);
				}
			}

		} else {
			log(WARN, resHash.getString("NO_NEW_PROPERTIES_ADDED", "AMC 7.1"));
		}
		log(INFO, resHash.getString("COMPLETED_ADDING_NEW_PROPERTIES"));
	}

	/**
	 * The method is a generic method used by all the perform methods for
	 * tokenizing the list of properties which are to be considered for the
	 * operation.
	 * 
	 * @param newProperties
	 * @return a list of properties names that are going to be migrated.
	 */
	private static List<String> tokenizeProperties(String newProperties) {
		List<String> lstProperties = null;
		if (newProperties != null && newProperties.trim().length() > 0) {
			lstProperties = new Vector<String>();
			StringTokenizer strtok = new StringTokenizer(newProperties,
					PROPERTY_DELIMITER);
			while (strtok.hasMoreElements()) {
				String sToken = strtok.nextToken();
				lstProperties.add(sToken);
			}
		}
		return lstProperties;
	}

	/**
	 * The modifications which can be done to the properties file can be of two
	 * types namely. 1/ Deleting/Commenting the properties which are no longer
	 * valid. 2/ Updating the value of a particular property
	 * 
	 * @param strbuf
	 */
	private static void performModifications(StringBuffer strbuf) {
		log(INFO, resHash.getString("STARTED_WITH_MODIFICATION"));
		commentProperties(strbuf);
		updateProperties(strbuf);
		log(INFO, resHash.getString("COMPLETED_WITH_MODIFICATION"));
	}

	private static void performUpdateText(StringBuffer strbuf) {
		/* When updating am_config.properties, you always want to use the "/" */
		String targetDirUpdateText = targetDir.replace('\\', '/');
		int changeIndex = strbuf.indexOf("$change$");
		while (changeIndex != -1) {
			strbuf.replace(changeIndex, changeIndex + "$change$".length(),
					targetDirUpdateText);
			changeIndex = strbuf.indexOf("$change$", targetDirUpdateText
					.length());
		}
	}

	/**
	 * The method updates the properties which are modified in the new release.
	 * The method comments the old property value and writes the updated
	 * property value immediately below the property which is updated.
	 * 
	 * @param strbuf
	 */
	private static void updateProperties(StringBuffer strbuf) {
		log(INFO, resHash.getString("UPDATING_PROPERTIES"));
		String propsUpdated = propsMigrateAMC
				.getProperty(AMC_PROPERTIES_UPDATED);
		List<String> lstPropertiesUpdated = tokenizeProperties(propsUpdated);
		if (lstPropertiesUpdated != null) {
			for (int iIndex = 0; iIndex < lstPropertiesUpdated.size(); iIndex++) {
				String propName = lstPropertiesUpdated.get(iIndex);
				String propValue = propsMigrateAMC.getProperty(propName);
				int startIndex = strbuf.indexOf(propName);

				if (strbuf.charAt(startIndex - 1) != '#') {
					/*
					 * not commented out so insert a comment at the beginning of
					 * the line
					 */
					strbuf.insert(startIndex, '#');
				} else {
					// If the '#' is present then we decrement the startIndex by
					// one so as to
					// consider the '#' char as well while tokenizing the
					// string.
					startIndex = startIndex - 1;
				}
				String line = strbuf.substring(startIndex);
				int equalsIndex = line.indexOf("=");
				int eolIndex = line.indexOf(eol);
				String oldvalue = line.substring(equalsIndex + 1, eolIndex);
				// Computing the newStartIndex Since we have added a # in front
				// of the old property
				// we have to consider its length, then the property name
				// length, then the equal to sign
				// length, followed by the oldvalues length and then finally the
				// eol length. Once all these
				// lengths are added then we get the offset which we need to add
				// to the startIndex of the
				// property in order to reach to a line exactly below the old
				// property.
				int newStartIndex = "#".length() + propName.length()
						+ "=".length() + oldvalue.length() + eol.length();
				newStartIndex = startIndex + newStartIndex;
				strbuf.insert(newStartIndex, propName + "=" + propValue + eol);
			}
		} else {
			log(INFO, resHash.getString("NO_PROPERTIES_TO_UPDATE"));
		}
	}

	/**
	 * The method comments the properties which are no longer required in the
	 * current release. The method does not physically delete the properties
	 * from the migrated file, however it adds a comment before these
	 * properties.
	 * 
	 * @param strbuf
	 */
	private static void commentProperties(StringBuffer strbuf) {
		log(INFO, resHash.getString("COMMENTING_UNWANTED_PROPERTIES"));
		String propsRemoved = propsMigrateAMC
				.getProperty(AMC_PROPERTIES_REMOVED);
		List<String> lstPropertiesRemoved = tokenizeProperties(propsRemoved);
		if (lstPropertiesRemoved != null) {
			log(INFO, resHash.getString(
					"NUMBER_OF_OLD_PROPERTIES_TO_BE_REMOVED", Integer
							.toString(lstPropertiesRemoved.size())));
			for (int iIndex = 0; iIndex < lstPropertiesRemoved.size(); iIndex++) {
				String propName = lstPropertiesRemoved.get(iIndex);
				int startIndex = strbuf.indexOf(propName + "=");

				/*
				 * We iterate through the entire of the String buffer to locate
				 * all the instances of the property name and comment the same
				 * in the properties file.
				 */
				while (startIndex != -1) {
					if (strbuf.charAt(startIndex - 1) != '#') {
						/*
						 * not commented out so insert a comment at the
						 * beginning of the line
						 */
						strbuf.insert(startIndex, '#');
					}
					startIndex = strbuf.indexOf(propName, startIndex
							+ propName.length());
				}
			}

		} else {
			log(INFO, resHash.getString("NO_OLD_PROPERTIES_TO_BE_REMOVED",
					"AMC 7.1"));
		}

	}

	/**
	 * Migrates the input file to the TDI 7.1 level. It assumes the input file
	 * amc.properties file. The input file (and optional backup file) should
	 * have been set in the parseGeneralOptions method which must be called
	 * before calling this method.
	 * 
	 * @return 1 is returned if the file is successful. -1 is returned if any
	 *         error occurs while trying to migrate the file including if the
	 *         file cannot be backed up to the specified location or if the file
	 *         to migrate cannot be written to.
	 * 
	 */
	public static int migrateFile() {
		int retCode = RC_FAIL;

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.START.MIGRATION"));
		}

		try {
			initializeMigration(MIGRATION_XML_PROPS_FILE);
			// First, get the needed parameters that we have already parsed and
			// validated.
			targetDir = generalOptions.get(GEN_OPT_TDI_INSTALL);
			targetDirJre = generalOptions.get(GEN_OPT_JRE_INSTALL);

			// Replace the proper strings in the JRE path.
			targetDirJre = targetDirJre.replace('/', fs.toCharArray()[0]);

			// Get the input file.
			String inputFile = generalOptions.get(GEN_OPT_FILE_MIG);

			// See if the user requested a backup of the file to a particular
			// location. If not, just back it up
			// to the current location with <name>.backup appended.
			String backupFile = generalOptions.get(GEN_OPT_FILE_BACKUP);
			if (backupFile == null) {
				backupFile = inputFile + ".backup";
			}

			// See if the user requested the migrated file to be sent to a new
			// location.
			String newFile = generalOptions.get(GEN_OPT_NEW_FILE);
			if (newFile == null) {
				newFile = inputFile;
			}

			// First, rename/backup the file.
			File tmpBackup1 = new File(inputFile);
			File tmpBackup2 = new File(backupFile);
			// But, exit out if we have specified a file that does not exist or
			// is not a file or cannot be read or written to.
			if (!tmpBackup1.exists() || !tmpBackup1.canRead()) {
				throw new Exception(resHash.getString("FILE_NOREAD_NOEXIST",
						inputFile));
			} else if (!tmpBackup1.isFile()) {
				throw new Exception(resHash.getString("NOT_A_FILE", inputFile));
			} else if (!tmpBackup1.canWrite()) {
				throw new Exception(resHash
						.getString("FILE_NOWRITE", inputFile));
			}

			// Do not continue if the backup file is the same as the input file.
			if (tmpBackup1.equals(tmpBackup2)) {
				throw new Exception(resHash.getString("FILES_ARE_SAME",
						inputFile));
			}
			// Check to ensure that the new file is not the backup file.
			if (!inputFile.equals(newFile)) {
				File tmpBackup3 = new File(newFile);
				if (tmpBackup3.equals(tmpBackup2)) {
					throw new Exception(resHash.getString("FILES_ARE_SAME2",
							newFile));
				}
			}
			// Always delete the backup file before starting...
			if (tmpBackup2.exists()) {
				boolean isDeleted = tmpBackup2.delete();
				if (isDeleted)
					logger.info(resHash.getString("FILE_DELETED", tmpBackup2
							.getAbsolutePath()));
				else
					logger.info(resHash.getString("FAILED_TO_DELETE_FILE",
							tmpBackup2.getAbsolutePath()));
			}

			boolean rename = tmpBackup1.renameTo(new File(backupFile));
			if (!rename) {
				throw new Exception(resHash.getString("UNABLE_BACKUP_FILE",
						new String[] { inputFile, backupFile }));
			}

			// Read the entire backup file into a buffer so that the proper
			// changes
			// can be made.
			FileInputStream fin = new FileInputStream(backupFile);
			InputStreamReader inStreamReader = new InputStreamReader(fin);
			StringBuffer globalPropNewBuf = new StringBuffer();
			String globalPropNewString = "";
			int c;
			while ((c = inStreamReader.read()) != -1) {
				globalPropNewBuf.append((char) c);
			}
			inStreamReader.close();
			fin.close();

			/* Determine what the end of line character is */
			if (globalPropNewBuf.indexOf("\r\n") != -1)
				eol = "\r\n";

			else if (globalPropNewBuf.indexOf("\n\r") != -1)
				eol = "\r\n";

			else
				eol = "\n";

			// Peform the additions to am_config.properties.v61 to bring it up
			// to v611 level.
			performAdditions(globalPropNewBuf);

			// Perform the modifications to am_config.properties.v61 to bring it
			// up to the v611 level.
			performModifications(globalPropNewBuf);

			// Perform the update text actions on am_config.properties.v61 to
			// bring it up to the v611 level.
			performUpdateText(globalPropNewBuf);

			// Write out the modified buffer to the original file. Its now
			// migrated.
			globalPropNewString = globalPropNewBuf.toString();
			FileOutputStream fout = new FileOutputStream(newFile);
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(fout));
			out.write(globalPropNewString);
			out.close();
			fout.close();

			// Do all of the work. The it passes.
			retCode = RC_OK;
		} catch (Exception e) {
			// Log the exception
			log(ERROR, e);
		}
		log(INFO, resHash.getString("EXIT_CODE_OF_MIGRATION", Integer
				.toString(retCode)));
		return retCode;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int retCode = RC_FAIL;

		// Initialize the logging...
		logger = Logger.getLogger("com.ibm.di.migamc.tdimigamc");

		// Initialize the needed globals.
		generalOptions = new Hashtable<String, String>();

		try {
			// First, parse the arguments...
			parseGeneralOptions(args);
			retCode = RC_OK;
			// Then try to migrate the file.
			if (!b_SHOW_HELP) {
				retCode = migrateFile();
			}
		} catch (Exception ex) {
			log(ERROR, ex);
		}

		if (retCode != RC_OK && bIllegalCommandUsage == false) {
			// The command did not finish successfully. Print WARNING message.
			// Don't print this message if there was an ILLEGAL COMMAND USAGE
			message(resHash.getString("COMMAND_ERROR_occurred"));
		}

		// Pring the usage if specified.
		if (b_SHOW_HELP) {
			message(resHash.getString("QUERY_USAGE"));
		}

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.RETURN.CODE", "" + retCode));
		}

		// Exit with the specified return code.
		System.exit(retCode);

	}

}
