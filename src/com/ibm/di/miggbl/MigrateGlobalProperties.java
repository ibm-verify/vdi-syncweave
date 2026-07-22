/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.miggbl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.StashFile;
import com.ibm.di.util.FileUtils;
import com.ibm.icu.util.StringTokenizer;

/**
 * The MigrateGlobalProperties class is a small program that will migrate any
 * global.properties (or solutions.properties) file from 6.0, 6.1, 6.1.1, 7.0, 7.1 & 7.1.1
 * to TDI 7.2. See the main method for information of valid parameters that can
 * be passed into the command. The program relies on the icu4j library for
 * globalization. It relies on log4j for logging.
 */
public class MigrateGlobalProperties {

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
	private static final ResourceHash resHash = ResourceHash.getHash("migrategblprops");

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
	 * Tracks if we are applying maintenance.
	 */
	private static boolean MAINTENANCE_MODE = false;

	/**
	 * Maintenance mode
	 */
	private static final String GEN_OPT_MAINTENANCE = "-m";

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
	 * Option to specify the working directory which is the solution directory.
	 * In place to load the proper logging file.
	 */
	private static final String GEN_OPT_SOL_DIR = "-s";

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
	 * Holds the operating system the program is running on.
	 */
	private static String os = System.getProperty("os.name");

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
	 * Holds a true string. If its set to true then we have Cloudscape 10
	 * migrations line to change.
	 */
	private static boolean tdiMigrateDerby = false;

	/**
	 * Holds the Derby Database location if it can be found.
	 */
	private static String derbyDatabaseLocation = "";

	/**
	 * Holds Touchpoint Server Port if it found.
	 */
	private static String tpServerPort = "";

	/**
	 * Used to send messages out to user. Based on passed parameters, this may
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
		StringBuilder trace = new StringBuilder();
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
	private static void parseGeneralOptions(String args[]) throws IllegalCommandUsageException {

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
			if (currentArgument.equals(GEN_OPT_FILE_MIG) || // File to
					// migrate
					currentArgument.equals(GEN_OPT_FILE_BACKUP) || // Name
					// to
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
					currentArgument.equals(GEN_OPT_NEW_FILE) || // The location
					// to migrate
					// the file to.
					currentArgument.equals(GEN_OPT_SOL_DIR) // The location of
			// the solution dir.
			) {
				putInGeneralOptionsTable(currentArgument, args, i);
				i++;
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
					throw new IllegalCommandUsageException(resHash.getString("OPT_OCCUR_TWICE", GEN_OPT_VERBOSE));
				}
				generalOptions.put(currentArgument, "true");
				VERBOSE_MODE = true;
			} else if (currentArgument.equals(HELP_OPTION)) {
				b_SHOW_HELP = true;
				break;
			} else if (currentArgument.equals(GEN_OPT_MAINTENANCE)) {
				generalOptions.put(currentArgument, "true");
				MAINTENANCE_MODE = true;
			} else { // UNKNOWN GENERAL OPTION.
				throw new IllegalCommandUsageException(resHash.getString("UNKNOWN_OPT", currentArgument));
			}
		}

		// We have an error if a required parameter is missing.
		if (b_SHOW_HELP == false) {
			if (operationSwitchFound == false) {
				throw new IllegalCommandUsageException(resHash.getString("FILE_OPTION_ABSENT", GEN_OPT_FILE_MIG));
			} else if (operationTDIInstallFound == false) {
				throw new IllegalCommandUsageException(resHash.getString("FILE_OPTION_ABSENT", GEN_OPT_TDI_INSTALL));
			} else if (operationJREFound == false) {
				throw new IllegalCommandUsageException(resHash.getString("FILE_OPTION_ABSENT", GEN_OPT_JRE_INSTALL));
			}
		}

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.GENERAL.OPTIONS", generalOptions.toString()));
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
	private static void putInGeneralOptionsTable(String generalOption, String args[], int currentCounter)
			throws IllegalCommandUsageException {
		if (generalOptions.containsKey(generalOption)) {
			throw new IllegalCommandUsageException(resHash.getString("OPT_OCCUR_TWICE", generalOption));
		}

		// ensure the next argument is a value.
		if (checkIfNextArgIsValue(args, currentCounter) == false) {
			String tmpArg = args[currentCounter];
			if (tmpArg.equals(GEN_OPT_FILE_MIG) || tmpArg.equals(GEN_OPT_FILE_BACKUP) || tmpArg.equals(GEN_OPT_TDI_INSTALL)
					|| tmpArg.equals(GEN_OPT_JRE_INSTALL) || tmpArg.equals(GEN_OPT_NEW_FILE)) {
				log(ERROR, resHash.getString("OPT_VAL_UNSPECIFIED", tmpArg));
			}
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"));
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
	private static boolean checkIfNextArgIsValue(String args[], int currentCounter) {
		if (currentCounter >= ((args.length) - 1)) {
			log(WARN, resHash.getString("COMMAND.ALREADY.REACHED.THE.END.OF.ARGUMENT.LIST"));
			return false;
		}

		// Check if it is one of the options
		String nextValue = args[currentCounter + 1];

		if (nextValue.equals(GEN_OPT_FILE_MIG) || nextValue.equals(GEN_OPT_FILE_BACKUP) || nextValue.equals(GEN_OPT_VERBOSE)
				|| nextValue.equals(HELP_OPTION) || nextValue.equals(GEN_OPT_TDI_INSTALL) || nextValue.equals(GEN_OPT_JRE_INSTALL)
				|| nextValue.equals(GEN_OPT_NEW_FILE) || nextValue.startsWith("-")) {
			return false;
		}

		return true;
	}

	private static void PerformDeletions(StringBuilder strbuf) {
		String v60DeleteSnippetOne = "## To have the ScriptEngine to precompile javascript/jscript code set this prop to true";

		String v61DeleteSnippetTwoA = "## --------------";
		String v61DeleteSnippetTwoB = "## AMC properties";
		String v61DeleteSnippetTwoC = "amc.ssl.on="; /*
													 * could be true or false
													 */

		/* Delete Snippet One */
		/* find out if snippet one is set to true or false */
		int snippetStartIndex = strbuf.indexOf(v60DeleteSnippetOne);
		int snippetEndIndex;

		if (snippetStartIndex != -1) {
			snippetEndIndex = strbuf.indexOf("##", snippetStartIndex + 5);
			// random starting point, just want to get to the next ##'s

			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		/*
		 * Delete Snippet Two Find the AMC line, back up to the beginning of the
		 * previous comment then go forward to the last line
		 */
		snippetStartIndex = strbuf.indexOf(v61DeleteSnippetTwoB) - v61DeleteSnippetTwoA.length() - 2 * eol.length();

		if (snippetStartIndex != -1) {
			snippetStartIndex = strbuf.indexOf("##", snippetStartIndex); // find
			// the
			// real
			// start
			// of
			// the
			// line
			snippetEndIndex = strbuf.indexOf(v61DeleteSnippetTwoC); // find the
			// end
			snippetEndIndex = strbuf.indexOf("##", snippetEndIndex); // find the
			// real
			// end
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// TDI 7.0 deletions

		// Delete server authentication example
		String v70DeleteSnippet1begin = "## example" + eol + "## javax.net.ssl.trustStore=";
		String v70DeleteSnippet1end = "## javax.net.ssl.trustStoreType=jks" + eol;

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet1begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(v70DeleteSnippet1end)
					+ v70DeleteSnippet1end.length();
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete client authentication example
		String v70DeleteSnippet2begin = "## example" + eol + "## javax.net.ssl.keyStore=";
		String v70DeleteSnippet2end = "## javax.net.ssl.keyStoreType=jks" + eol;

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet2begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(v70DeleteSnippet2end)
					+ v70DeleteSnippet2end.length();
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete ACT related properties

		String v70DeleteSnippet3begin = "## -----------------------------------------------" + eol
				+ "## Active Correlation Technology engine settings";
		String v70DeleteSnippet3end = ".acts" + eol;

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet3begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(v70DeleteSnippet3end)
					+ v70DeleteSnippet3end.length();
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete the JVM related property
		String v70DeleteSnippet4begin = "# Location of directory where the JRE SDI will use is installed";
		String v70DeleteSnippet4begin2 = "com.ibm.di.jvmdir=";

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet4begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}
		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet4begin2);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete the javascript related property
		String v70DeleteSnippet5begin = "## Regex library selection (java or jakarta). Using jakarta requires the Jakarta regex library (not included)";
		String v70DeleteSnippet5begin2 = "com.ibm.di.scriptengine.regex=";

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet5begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}
		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet5begin2);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete another javascript engine related property
		String v70DeleteSnippet6begin = "## Custom class for ibmjs options to let us choose regex library";
		String v70DeleteSnippet6begin2 = "ibmjs.options=";

		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet6begin);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}
		snippetStartIndex = strbuf.indexOf(v70DeleteSnippet6begin2);
		if (snippetStartIndex != -1) {
			snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}

		// Delete the Checkpoint store related properties
		String[] v70checkpoints = new String[] {
		// -- first segment
				"#com.ibm.di.store.create.checkpoint.store",
				// -- second segment
				"#com.ibm.di.store.create.checkpoint.store=CREATE TABLESPACE",
				// -- third segment
				"com.ibm.di.store.create.checkpoint.store", };

		for (String str : v70checkpoints) {
			snippetStartIndex = strbuf.indexOf(str);
			if (snippetStartIndex != -1) {
				snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
				strbuf.delete(snippetStartIndex, snippetEndIndex);
			}
		}

		// Delete the miadmin library related properties
		String[] v70miadminlib = new String[] {
		// -- first segment
				"##" + eol + "## Modify the line below to specify the location of the Library Resources.",
				// -- second segment
				"## The default value is <HOMEDIR>/tdilibrary",
				// -- third segment
				"# com.ibm.di.admin.library.dir=",
				// -- fourth segment
				"com.ibm.di.admin.library.dir=" };

		for (String str : v70miadminlib) {
			snippetStartIndex = strbuf.indexOf(str);
			if (snippetStartIndex != -1) {
				snippetEndIndex = snippetStartIndex + strbuf.substring(snippetStartIndex).indexOf(eol);
				strbuf.delete(snippetStartIndex, snippetEndIndex);
			}
		}

		// End of TDI 7.0 deletions
		// Delete the javascript related property

		tpServerPort = getPropertyValue(strbuf, "tp.server.port");
		
		snippetStartIndex = strbuf.indexOf("tp.server.port");
		if (snippetStartIndex != -1) {
			snippetEndIndex = strbuf.indexOf(eol, snippetStartIndex);
			strbuf.delete(snippetStartIndex, snippetEndIndex);
		}
	}

	private static void performAdditions(StringBuilder strbuf) {

		/* Snippet 1 no longer added since it was removed in 7.0 */

		/* Snippet 2 no longer added since it was removed in 7.0 */

		/* Snippet 3 */
		String v60AddSnippet31 = "# Set a customized SQL statement for creation of the Tombstone Manager table. Keep the same table and field names."
				+ eol;
		String v60AddSnippet32 = "#com.ibm.di.store.create.tombstones=CREATE TABLE IDI_TOMBSTONE ( ID INT GENERATED ALWAYS AS IDENTITY, COMPONENT_TYPE_ID INT, EVENT_TYPE_ID INT, START_TIME TIMESTAMP, CREATED_ON TIMESTAMP, COMPONENT_NAME VARCHAR(1024), CONFIGURATION VARCHAR(1024), EXIT_CODE INT, ERROR_DESCR VARCHAR(1024), STATS LONG VARCHAR FOR BIT DATA, GUID VARCHAR(1024) NOT NULL, USER_MESSAGE VARCHAR(1024), UNIQUE (ID, GUID))"
				+ eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet31) == -1) {

			// logUtil.writeToLog("Adding snippet 3");
			strbuf.append(v60AddSnippet31);
			strbuf.append(v60AddSnippet32);

			strbuf.append(eol);
		}

		/* Snippet 4 */
		String v60AddSnippet41 = "# the ibmsnap_commitseq column name used by the RDBMS changelog connector" + eol;
		String v60AddSnippet42 = "com.ibm.di.conn.rdbmschlog.cdcolname=ibmsnap_commitseq" + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet41) == -1) {
			// logUtil.writeToLog("Adding snippet 4");
			strbuf.append(v60AddSnippet41);
			strbuf.append(v60AddSnippet42);

			strbuf.append(eol);
		}

		/* Snippet 6 */
		/* This snippet must be added below the line: api.remote.ssl.on=true] */
		String v60AddSnippet61 = "api.remote.ssl.on="; // could be true or false
		String v60AddSnippet62 = "api.remote.naming.port";
		String v60AddSnippet63 = "api.remote.ssl.client.auth.on=true";

		/* Need to insert this string */
		if (strbuf.indexOf(v60AddSnippet61) == -1) {
			// logUtil.writeToLog("Adding snippet 6");
			int startIndex = strbuf.indexOf(v60AddSnippet62);
			strbuf.insert(startIndex, v60AddSnippet63);

			strbuf.append(eol);
		}

		/* Snippet 7 */
		String v60AddSnippet71 = "## Specifies a list of IP addresses to accept non SSL connections from (host names are not accepted)."
				+ eol;
		String v60AddSnippet72 = "## Use space, comma or semicolon as delimiter between IP addresses. This property is only taken into account"
				+ eol;
		String v60AddSnippet73 = "## when api.remote.ssl.on is set to false." + eol;
		String v60AddSnippet74 = "## api.remote.nonssl.hosts=" + eol;

		String v60AddSnippet75 = "api.remote.nonssl.hosts=";

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet75) == -1) {
			// logUtil.writeToLog("Adding snippet 7");
			strbuf.append(v60AddSnippet71);
			strbuf.append(v60AddSnippet72);
			strbuf.append(v60AddSnippet73);
			strbuf.append(v60AddSnippet74);

			strbuf.append(eol);
		}

		/* Snippet 8 */
		String v60AddSnippet81 = "## The configuration files placed in this folder can be edited through the Server API." + eol;
		String v60AddSnippet82 = "## Configuration files placed in other folders cannot be edited through the Server API." + eol;
		String v60AddSnippet83 = "api.config.folder=$change$/configs" + eol + eol;

		/* Needs to have a blank line between these lines */
		String v60AddSnippet84 = "## Timeout in minutes for configuration locks. A value of 0 means no timeout." + eol;
		String v60AddSnippet85 = "api.config.lock.timeout=0" + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet81) == -1) {
			// logUtil.writeToLog("Adding snippet 8");
			strbuf.append(v60AddSnippet81);
			strbuf.append(v60AddSnippet82);
			strbuf.append(v60AddSnippet83);
			strbuf.append(v60AddSnippet84);
			strbuf.append(v60AddSnippet85);

			strbuf.append(eol);
		}

		/* Snippet 9 */
		String v60AddSnippet91 = "## Specifies if the Server API methods for custom method invocation (Session.invokeCustom(...)) are allowed to be used."
				+ eol;
		String v60AddSnippet92 = "## When api.custom.method.invoke.on is set to false and the Server API methods for custom method invocation are used,"
				+ eol;
		String v60AddSnippet93 = "## then an exception will be thrown." + eol;
		String v60AddSnippet94 = "## Only classes listed in api.custom.method.invoke.allowed.classes are allowed to be directly invoked."
				+ eol;
		String v60AddSnippet95 = "## The default value is false." + eol;
		String v60AddSnippet96 = "api.custom.method.invoke.on=false" + eol + eol;

		/* Need a blank line here */
		String v60AddSnippet97 = "## Specifies the list of classes which can be directly invoked by the Server API methods for custom"
				+ eol;
		String v60AddSnippet98 = "## method invocation (Session.invokeCustom(...))." + eol;
		String v60AddSnippet99 = "## This property is only taken into account if api.custom.method.invoke.on is set to true." + eol;
		String v60AddSnippet910 = "## The classes in this list must be separated by a space, a comma or a semicolon." + eol;
		String v60AddSnippet911 = "## Example:" + eol;
		String v60AddSnippet912 = "## api.custom.method.invoke.allowed.classes=com.ibm.MyClass,com.ibm.MyOtherClass" + eol;
		String v60AddSnippet913 = "## In the above example only methods from the com.ibm.MyClass and com.ibm.MyOtherClass classes are"
				+ eol;
		String v60AddSnippet914 = "## allowed to be directly invoked." + eol;
		String v60AddSnippet915 = "api.custom.method.invoke.allowed.classes=" + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet91) == -1) {

			// logUtil.writeToLog("Adding snippet 9");
			strbuf.append(v60AddSnippet91);
			strbuf.append(v60AddSnippet92);
			strbuf.append(v60AddSnippet93);
			strbuf.append(v60AddSnippet94);
			strbuf.append(v60AddSnippet95);
			strbuf.append(v60AddSnippet96);
			strbuf.append(v60AddSnippet97);
			strbuf.append(v60AddSnippet98);
			strbuf.append(v60AddSnippet99);
			strbuf.append(v60AddSnippet910);
			strbuf.append(v60AddSnippet911);
			strbuf.append(v60AddSnippet912);
			strbuf.append(v60AddSnippet913);
			strbuf.append(v60AddSnippet914);
			strbuf.append(v60AddSnippet915);

			strbuf.append(eol);

		}

		/* Snippet 10 */
		String v60AddSnippet101 = "## api.custom.authentication points to a JavaScript text file that contains custom authentication code.";
		String v60AddSnippet102 = "## For example: api.custom.authentication=ldap_auth.js.";
		String v60AddSnippet103 = "## To enable the built-in LDAP Authentication mechanism, set this property to \"[ldap]\".";
		String v60AddSnippet104 = "## For example: api.custom.authentication=[ldap]";
		String v60AddSnippet105 = "##api.custom.authentication=[ldap]";

		/* Need a blank line */
		String v60AddSnippet106 = "## LDAP Authnetication properties";
		String v60AddSnippet107 = "## ---------------------";

		/* Need a blank line */
		String v60AddSnippet108 = "## If this parameter is set to \"true\" and the LDAP Authnetication initialization fails, the whole Server API will not be started.";
		String v60AddSnippet109 = "## If this parameter is missing or is set to \"false\" any LDAP Authentication initialization errors will be logged and the Server API will be started.";
		String v60AddSnippet1010 = "api.custom.authentication.ldap.critical=false";

		/* Need a blank line */
		String v60AddSnippet1011 = "## LDAP Server hostname.";
		String v60AddSnippet1012 = "api.custom.authentication.ldap.hostname=";

		/* Need a blank line */
		String v60AddSnippet1013 = "## LDAP server port number. For example, 389 for non-SSL or 636 for SSL.";
		String v60AddSnippet1014 = "api.custom.authentication.ldap.port=";

		/* Need a blank line */
		String v60AddSnippet1015 = "## Specifies whether SSL is used to communicate with the LDAP Server.";
		String v60AddSnippet1016 = "## When set to \"true\" SSL will be used, otherwise SSL will not be used.";
		String v60AddSnippet1017 = "api.custom.authentication.ldap.ssl=";

		/* Need a blank line */
		String v60AddSnippet1018 = "## Specifies the LDAP directory location where user searches will be preformed.";
		String v60AddSnippet1019 = "## When this property is not specified user searches will not be performed.";
		String v60AddSnippet1020 = "api.custom.authentication.ldap.searchbase=";

		/* Need a blank line */
		String v60AddSnippet1021 = "## Specifies the user id attribute to be used in searches.";
		String v60AddSnippet1022 = "## When this property is not specified user searches will not be performed.";
		String v60AddSnippet1023 = "api.custom.authentication.ldap.userattribute=";

		/* Need a blank line */
		String v60AddSnippet1024 = "## Specifies an LDAP Server administrator distinguished name that will be used for user searches.";
		String v60AddSnippet1025 = "## When this property is not specified anonymous bind will be used for user searches.";
		String v60AddSnippet1026 = "api.custom.authentication.ldap.admindn=";

		/* Need a blank line */
		String v60AddSnippet1027 = "## Password for the LDAP Server administrator distinguished name.";
		String v60AddSnippet1028 = "{protect}-api.custom.authentication.ldap.adminpassword=";

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet101) == -1) {
			// logUtil.writeToLog("Adding snippet 10");
			strbuf.append(v60AddSnippet101 + eol);
			strbuf.append(v60AddSnippet102 + eol);
			strbuf.append(v60AddSnippet103 + eol);
			strbuf.append(v60AddSnippet104 + eol);
			strbuf.append(v60AddSnippet105 + eol + eol);
			strbuf.append(v60AddSnippet106 + eol);
			strbuf.append(v60AddSnippet107 + eol + eol);
			strbuf.append(v60AddSnippet108 + eol);
			strbuf.append(v60AddSnippet109 + eol);
			strbuf.append(v60AddSnippet1010 + eol + eol);
			strbuf.append(v60AddSnippet1011 + eol);
			strbuf.append(v60AddSnippet1012 + eol + eol);
			strbuf.append(v60AddSnippet1013 + eol);
			strbuf.append(v60AddSnippet1014 + eol + eol);
			strbuf.append(v60AddSnippet1015 + eol);
			strbuf.append(v60AddSnippet1016 + eol);
			strbuf.append(v60AddSnippet1017 + eol + eol);
			strbuf.append(v60AddSnippet1018 + eol);
			strbuf.append(v60AddSnippet1019 + eol);
			strbuf.append(v60AddSnippet1020 + eol + eol);
			strbuf.append(v60AddSnippet1021 + eol);
			strbuf.append(v60AddSnippet1022 + eol);
			strbuf.append(v60AddSnippet1023 + eol + eol);
			strbuf.append(v60AddSnippet1024 + eol);
			strbuf.append(v60AddSnippet1025 + eol);
			strbuf.append(v60AddSnippet1026 + eol + eol);
			strbuf.append(v60AddSnippet1027 + eol);
			strbuf.append(v60AddSnippet1028 + eol + eol);

			strbuf.append(eol);

		}

		/* Snippet 11 */

		String v60AddSnippet111 = "## Tombstone Manager properties" + eol;
		String v60AddSnippet112 = "## ---------------------" + eol;
		String v60AddSnippet113 = "com.ibm.di.tm.on=false" + eol;
		String v60AddSnippet114 = "com.ibm.di.tm.autodel.age=0" + eol;
		String v60AddSnippet115 = "com.ibm.di.tm.autodel.records.trigger.on=10000" + eol;
		String v60AddSnippet116 = "com.ibm.di.tm.autodel.records.max=5000" + eol;
		String v60AddSnippet117 = "com.ibm.di.tm.create.all=false" + eol + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet111) == -1) {
			// logUtil.writeToLog("Adding snippet 11");
			strbuf.append(v60AddSnippet111);
			strbuf.append(v60AddSnippet112);
			strbuf.append(v60AddSnippet113);
			strbuf.append(v60AddSnippet114);
			strbuf.append(v60AddSnippet115);
			strbuf.append(v60AddSnippet116);
			strbuf.append(v60AddSnippet117);

			strbuf.append(eol);

		}

		/* Snippet 12 */
		String v60AddSnippet121 = "## Properties for Windows IPv6 communications." + eol;
		String v60AddSnippet122 = "## Uncomment these properties for Windows IPv6 communication only." + eol;
		String v60AddSnippet123 = "## These properties will not affect IPv4 communication or IPv6 communication on Unices." + eol;
		String v60AddSnippet124 = "#java.net.preferIPv4Stack=false" + eol;
		String v60AddSnippet125 = "#java.net.preferIPv6Addresses=true\n" + eol;

		String v60AddSnippet126 = "java.net.preferIPv4Stack";
		String v60AddSnippet127 = "java.net.preferIPv6Addresses";

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet126) == -1) {
			// logUtil.writeToLog("Adding snippet 12");
			strbuf.append(v60AddSnippet121);
			strbuf.append(v60AddSnippet122);
			strbuf.append(v60AddSnippet123);
			strbuf.append(v60AddSnippet124);
		}

		if (strbuf.indexOf(v60AddSnippet127) == -1) {
			strbuf.append(v60AddSnippet125);
		}

		strbuf.append(eol);

		/* Snippet 13 */
		String v60AddSnippet131 = "## --------------------------------------------" + eol;
		String v60AddSnippet132 = "## Performance settings" + eol;
		String v60AddSnippet133 = "## --------------------------------------------" + eol;
		String v60AddSnippet134 = "##" + eol;
		String v60AddSnippet135 = "## Enable/Disable performance logging" + eol;
		String v60AddSnippet136 = "com.ibm.di.server.perfStats=false" + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet132) == -1) {
			// logUtil.writeToLog("Adding snippet 13");
			strbuf.append(v60AddSnippet131);
			strbuf.append(v60AddSnippet132);
			strbuf.append(v60AddSnippet133);
			strbuf.append(v60AddSnippet134);
			strbuf.append(v60AddSnippet135);
			strbuf.append(v60AddSnippet136);

			strbuf.append(eol);

		}

		/* Snippet 14 */
		String v60AddSnippet141 = "### ------------------------------------------" + eol;
		String v60AddSnippet142 = "### Used by Config Report" + eol;
		String v60AddSnippet143 = "###-------------------------------------------" + eol;
		String v60AddSnippet144 = "### set this is you want to override the local language for Config Reports" + eol;
		String v60AddSnippet145 = "# com.ibm.di.admin.configreport.translation=en" + eol + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet142) == -1) {
			// logUtil.writeToLog("Adding snippet 14");
			strbuf.append(v60AddSnippet141);
			strbuf.append(v60AddSnippet142);
			strbuf.append(v60AddSnippet143);
			strbuf.append(v60AddSnippet144);
			strbuf.append(v60AddSnippet145);

			strbuf.append(eol);

		}

		/* Snippet 15 */
		String v60AddSnippet151 = "##----------------------" + eol;
		String v60AddSnippet152 = "## System Queue settings" + eol;
		String v60AddSnippet153 = "##----------------------" + eol;
		String v60AddSnippet154 = "## If set to \"true\" the System Queue is initialized on startup and can be used;" + eol;
		String v60AddSnippet155 = "## otherwise the System Queue is not initialized and cannot be used." + eol;
		String v60AddSnippet156 = "systemqueue.on=true" + eol + eol;

		/* Need a blank line */
		String v60AddSnippet157 = "## Specifies the fully qualified name of the class that will be used as a JMS Driver." + eol;
		String v60AddSnippet158 = "# systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.IBMMQ" + eol;
		String v60AddSnippet159 = "# systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.JMSScriptDriver" + eol;
		String v60AddSnippet1510 = "systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.IBMMQe";

		/* Need a blank line */
		String v60AddSnippet1515 = "### MQ JMS driver initialization properties" + eol;
		String v60AddSnippet1516 = "# systemqueue.jmsdriver.param.mq.java.naming.provider.url=<host:port>" + eol;
		String v60AddSnippet1517 = "# systemqueue.jmsdriver.param.mq.channel=<channel_name>" + eol;
		String v60AddSnippet1518 = "# systemqueue.jmsdriver.param.mq.queue.manager=<queuemanger_name>" + eol;
		String v60AddSnippet1519 = "# systemqueue.jmsdriver.param.mq.sslCipher=<cipherSuite_name>" + eol;
		String v60AddSnippet1520 = "# systemqueue.jmsdriver.param.mq.sslUseFlag=false" + eol + eol;

		/* Need a blank line */
		String v60AddSnippet1521 = "### JMS Javascript driver initialization properties" + eol;
		String v60AddSnippet1522 = "## Specifies the location of the script file" + eol;
		String v60AddSnippet1523 = "# systemqueue.jmsdriver.param.js.jsfile=driver.js" + eol + eol;

		/* Need a blank line */
		String v60AddSnippet1524 = "## This is the place to put any JMS provider specific properties needed by a JMS Driver," + eol;
		String v60AddSnippet1525 = "## which connects to a 3rd party JMS system." + eol;
		String v60AddSnippet1526 = "## All JMS Driver properties should begin with the 'systemqueue.jmsdriver.param.' prefix."
				+ eol;
		String v60AddSnippet1527 = "## All properties having this prefix are passes to the JMS Driver on initialization after"
				+ eol;
		String v60AddSnippet1528 = "## removing the 'systemqueue.jmsdriver.param.' prefix from the property name." + eol;
		String v60AddSnippet1529 = "# systemqueue.jmsdriver.param.user.param1=value1" + eol;
		String v60AddSnippet1530 = "# systemqueue.jmsdriver.param.user.param2=value2" + eol;
		String v60AddSnippet1531 = "# ..." + eol + eol;

		/* Need a blank line */
		String v60AddSnippet1532 = "## Credentials used for authenticating to the target JMS system" + eol;
		String v60AddSnippet1533 = "# {protect}-systemqueue.auth.username=<username>" + eol;
		String v60AddSnippet1534 = "# {protect}-systemqueue.auth.password=<password>" + eol;

		/* Not found so lets append to the bottom */
		if (strbuf.indexOf(v60AddSnippet152) == -1) {
			// logUtil.writeToLog("Adding snippet 15");
			strbuf.append(v60AddSnippet151);
			strbuf.append(v60AddSnippet152);
			strbuf.append(v60AddSnippet153);
			strbuf.append(v60AddSnippet154);
			strbuf.append(v60AddSnippet155);
			strbuf.append(v60AddSnippet156);
			strbuf.append(v60AddSnippet157);
			strbuf.append(v60AddSnippet158);
			strbuf.append(v60AddSnippet159);
			strbuf.append(eol);
			strbuf.append(v60AddSnippet1515);
			strbuf.append(v60AddSnippet1516);
			strbuf.append(v60AddSnippet1517);
			strbuf.append(v60AddSnippet1518);
			strbuf.append(v60AddSnippet1519);
			strbuf.append(v60AddSnippet1520);
			strbuf.append(v60AddSnippet1521);
			strbuf.append(v60AddSnippet1522);
			strbuf.append(v60AddSnippet1523);
			strbuf.append(v60AddSnippet1524);
			strbuf.append(v60AddSnippet1525);
			strbuf.append(v60AddSnippet1526);
			strbuf.append(v60AddSnippet1527);
			strbuf.append(v60AddSnippet1528);
			strbuf.append(v60AddSnippet1529);
			strbuf.append(v60AddSnippet1530);
			strbuf.append(v60AddSnippet1531);
			strbuf.append(v60AddSnippet1532);
			strbuf.append(v60AddSnippet1533);
			strbuf.append(v60AddSnippet1534);

			strbuf.append(eol);

		}

		String v70FIPSModeProps1 = "## Enabling/Disabling FIPS Mode in SDI" + eol;

		String v70FIPSModeProps2 = "##------------------------------------" + eol;

		String v70FIPSModeProps3 = "## If the below property is set to true then SDI will be enforced to run in FIPS Compliant Mode."
				+ eol;

		String v70FIPSModeProps4 = "## The default value is false, i.e. SDI will not run in FIPS Mode by default." + eol;

		String v70FIPSModeProps5 = "com.ibm.di.server.fipsmode.on=";

		if (strbuf.indexOf(v70FIPSModeProps5) == -1) {
			strbuf.append(v70FIPSModeProps1);
			strbuf.append(v70FIPSModeProps2);
			strbuf.append(v70FIPSModeProps3);
			strbuf.append(v70FIPSModeProps4);
			strbuf.append(v70FIPSModeProps5 + "false");

			strbuf.append(eol);
		}

		String v70PKCS11Props1 = "##PKCS11 options" + eol;
		String v70PKCS11Props2 = "##Set the value of following properties to use PKCS11 enabled devices to store SDI servers private key / certificate."
				+ eol;
		String v70PKCS11Props3 = "com.ibm.di.pkcs11cfg=" + eol;
		String v70PKCS11Props4 = "com.ibm.di.server.pkcs11=" + eol;
		String v70PKCS11Props5 = "com.ibm.di.server.pkcs11.library=" + eol;
		String v70PKCS11Props6 = "com.ibm.di.server.pkcs11.slot=" + eol;
		String v70PKCS11Props7 = "{protect}-com.ibm.di.server.pkcs11.password=" + eol;

		if (strbuf.indexOf(v70PKCS11Props1) == -1) {
			strbuf.append(v70PKCS11Props2);
			strbuf.append(v70PKCS11Props3 + "false");
			strbuf.append(v70PKCS11Props4 + "etc" + File.separator + "pkcs11.cfg");
			strbuf.append(v70PKCS11Props5);
			strbuf.append(v70PKCS11Props6);
			strbuf.append(v70PKCS11Props7);

			strbuf.append(eol);
		}

		String v70SecurityProviderOld = "com.metamerge.securityTransformation";
		String v70SecurityProviderNew = "com.ibm.di.securityTransformation";
		int oldProviderIndex = strbuf.indexOf(v70SecurityProviderOld);
		if (oldProviderIndex != -1) {
			strbuf.replace(oldProviderIndex, oldProviderIndex + v70SecurityProviderOld.length(), v70SecurityProviderNew);
		}

		String v611Snippet2Predecessor = "## To enable the built-in LDAP Authentication mechanism, set this property to \"[ldap]\"."
				+ eol;
		String v70AddSnippet2 = "## To enable the built-in JAAS Authentication mechanism, set this property to \"[jaas]\"." + eol;
		int startIndexPredecessor = strbuf.indexOf(v611Snippet2Predecessor);
		int startIndexSnippet = strbuf.indexOf(v70AddSnippet2);
		if (startIndexPredecessor != -1 && startIndexSnippet == -1) {
			startIndexPredecessor = startIndexPredecessor + v611Snippet2Predecessor.length();
			strbuf.insert(startIndexPredecessor, v70AddSnippet2);
		}

		String v70AddSnippet3Comment = "## JAAS Authnetication properties" + eol + "## ---------------------" + eol;
		String v70AddSnippet3Prop = "java.security.auth.login.config";
		if (strbuf.indexOf(v70AddSnippet3Prop) == -1) {
			strbuf.append(v70AddSnippet3Comment);
			strbuf.append(v70AddSnippet3Prop + "=" + eol + eol);
		}

		String v70ServerIDProps1 = "## Specify the unique ID for the SDI Server" + eol;
		String v70ServerIDProps2 = "## ----------------------------------------" + eol;
		String v70ServerIDProps3 = "## This property helps a client connecting to the SDI server to identify different servers"
				+ eol;
		String v70ServerIDProps4 = "## running on the same IP and the same port in different time. (Default is DEFAULT_ID)" + eol;
		String v70ServerIDProps5 = "com.ibm.di.server.id=";

		if (strbuf.indexOf(v70ServerIDProps5) == -1) {
			strbuf.append(v70ServerIDProps1);
			strbuf.append(v70ServerIDProps2);
			strbuf.append(v70ServerIDProps3);
			strbuf.append(v70ServerIDProps4);
			strbuf.append(v70ServerIDProps5 + "DEFAULT_ID");

			strbuf.append(eol);
		}

		String v70SymmetricCipherSupportProps1 = "com.ibm.di.server.encryption.keystoretype";
		if (strbuf.indexOf(v70SymmetricCipherSupportProps1) == -1) {
			strbuf.append(v70SymmetricCipherSupportProps1 + "=jks" + eol);
		}
		String v70SymmetricCipherSupportProps2 = "com.ibm.di.server.encryption.transformation";
		if (strbuf.indexOf(v70SymmetricCipherSupportProps2) == -1) {
			strbuf.append(v70SymmetricCipherSupportProps2 + "=RSA" + eol);
		}

		// Add properties related to SR-5

		String oldKeyStorelocation = "com.ibm.di.server.keystore=";
		String oldKeyStoreAlias = "com.ibm.di.server.key.alias=";

		String keyStoreloc = getPropertyValue(strbuf, oldKeyStorelocation);
		String keyStoreAlias = getPropertyValue(strbuf, oldKeyStoreAlias);
		// Passwords are to be read from idisrv.sth file
		// For now assume they are the same as the api.truststore.
		// String keystorePwd = getPropertyValue(strbuf,
		// "api.truststore.pass=");
		Vector<String> vecPasswords = null;
		String keystorePwd = null;
		String keyPwd = null;
		try {
			vecPasswords = StashFile.readPasswords();
		} catch (Exception e) {
			log(ERROR, resHash.getString("CANNOT_READ_STASH_FILE", e.getMessage()), e);
		}

		if (vecPasswords != null && vecPasswords.size() > 0) {
			keystorePwd = vecPasswords.get(0);
			if (vecPasswords.size() > 1)
				keyPwd = vecPasswords.get(1);
			else
				keyPwd = keystorePwd;
		}

		else
			log(ERROR, resHash.getString("NO_PASSWORD_FOUND_IN_STASH"));

		String v70AddSnippet4Comment1 = "## Encryption properties added in SDI 7.0" + eol;
		String v70AddSnippet4Props1 = "com.ibm.di.server.encryption.keystore";
		String v70AddSnippet4Props2 = "com.ibm.di.server.encryption.key.alias";

		String v70AddSnippet4Comment2 = "## Added property for server api keystore password and key password" + eol;
		String v70AddSnippet4Props3 = "api.keystore.password=";
		String v70AddSnippet4Props4 = "api.key.password=";
		int startIndex = 0;
		int eolIndex = 0;

		if (strbuf.indexOf(v70AddSnippet4Props1) == -1) {

			// Insert new encryption properties after the
			// "com.ibm.di.server.securemode" property
			startIndex = strbuf.indexOf("com.ibm.di.server.securemode");
			if (startIndex != -1) {
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length();

				String line = eol + v70AddSnippet4Comment1;
				strbuf.insert(startIndex, line);
				startIndex += line.length();
				line = v70AddSnippet4Props1 + "=" + keyStoreloc + eol;
				strbuf.insert(startIndex, line);
				startIndex += line.length();
				line = v70AddSnippet4Props2 + "=" + keyStoreAlias + eol;
				strbuf.insert(startIndex, line);
				startIndex += line.length();
				strbuf.insert(startIndex, eol);
			} else {
				// Property not found append to the end of file
				strbuf.append(eol);
				strbuf.append(v70AddSnippet4Comment1);
				strbuf.append(v70AddSnippet4Props1 + "=" + keyStoreloc + eol);
				strbuf.append(v70AddSnippet4Props2 + "=" + keyStoreAlias + eol);
				strbuf.append(eol);
			}
			// Add comment related to adding new keystore pwd property for
			// ssl keystore after the ssl alias property
			startIndex = strbuf.indexOf("com.ibm.di.server.key.alias");
			if (startIndex != -1) {
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length();

				strbuf.insert(startIndex, eol + v70AddSnippet4Comment2);
				startIndex = startIndex + (eol + v70AddSnippet4Comment2).length();
				strbuf.insert(startIndex, v70AddSnippet4Props3 + keystorePwd + eol);
				startIndex = startIndex + (v70AddSnippet4Props3 + keystorePwd + eol).length();
				strbuf.insert(startIndex, v70AddSnippet4Props4 + keyPwd + eol);
			} else {
				// Property not found append to bottom of file
				strbuf.append(eol);
				strbuf.append(v70AddSnippet4Comment2);
				strbuf.append(v70AddSnippet4Props3 + keystorePwd + eol);
				strbuf.append(v70AddSnippet4Props4 + keyPwd + eol);
			}

		}

		// Add property for Server API config load timeout.
		String v70AddSnippet5Comment = "## Timeout in minutes for loading configuration" + eol;
		String v70AddSnippet5Props = "api.config.load.timeout=2";

		if (strbuf.indexOf(v70AddSnippet5Props) == -1) {

			startIndex = strbuf.indexOf("api.config.lock.timeout");
			if (startIndex != -1) {
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length();
				strbuf.insert(startIndex, eol + v70AddSnippet5Comment);
				startIndex = startIndex + (eol + v70AddSnippet5Comment).length();
				strbuf.insert(startIndex, v70AddSnippet5Props + eol);
			} else {
				strbuf.append(eol);
				strbuf.append(v70AddSnippet5Comment);
				strbuf.append(v70AddSnippet5Props);
				strbuf.append(eol);

			}
		}

		// Add property for turning off logging
		String v70AddLoggingPropertyComment = "## ---------------------------------------------" + eol + "## Logging settings"
				+ eol + "## ---------------------------------------------" + eol + eol
				+ "## When false, all log calls made through the SDI Log class will be discarded." + eol;
		String v70AddLoggingProperty = "com.ibm.di.logging.enabled";
		String v70AddLoggingPropertyValue = "=true" + eol;

		if (strbuf.indexOf(v70AddLoggingProperty) == -1) {
			strbuf.append(eol);
			strbuf.append(v70AddLoggingPropertyComment);
			strbuf.append(v70AddLoggingProperty);
			strbuf.append(v70AddLoggingPropertyValue);
		}

		// Add property to set platform for IBM JS Engine
		String v70IBMJSPlatformPropertyComment = "## ---------------------------------------------" + eol
				+ "## IBM JavaScript Engine settings" + eol + "## ---------------------------------------------" + eol + eol
				+ "## Set the type of platform - required by the IBM JS Engine when caching is used." + eol;
		String v70IBMJSPlatformProperty = "com.ibm.commons.platform";
		String v70IBMJSPlatformPropertyValue = "=com.ibm.commons.platform.GenericPlatform" + eol;

		if (strbuf.indexOf(v70IBMJSPlatformProperty) == -1) {
			strbuf.append(eol);
			strbuf.append(v70IBMJSPlatformPropertyComment);
			strbuf.append(v70IBMJSPlatformProperty);
			strbuf.append(v70IBMJSPlatformPropertyValue);
		}

		String v70NotificationsSuppressionProperty = "api.notification.suppress";
		String v70NotificationsSuppression = "## Specifies a list of Server notification types, which will be suppressed. " + eol
				+ "## Notifications of suppressed types will not be propagated by the notifications framework." + eol
				+ "## The notification types in the list are separated by spaces. Wildcards may be included." + eol + "## Example:"
				+ eol + "## api.notification.suppress=di.al.* di.ci.start" + eol
				+ "## The above example will suppress all Assembly Line related notifications as well as" + eol
				+ "## notifications for starting a configuration instance." + eol
				+ "## If the property is missing or is empty, no notifications will be suppressed." + eol
				+ v70NotificationsSuppressionProperty + "=di.server.api.authenticate di.server.api.authorize.*" + eol;
		if (strbuf.indexOf(v70NotificationsSuppressionProperty) == -1) {

			/*
			 * Insert the property after the
			 * "api.custom.method.invoke.allowed.classes" property to maintain
			 * logical ordering. To shield against commented occurrences of the
			 * property, search for it at the beginning of a new line.
			 */
			int predecessorStart = strbuf.indexOf(eol + "api.custom.method.invoke.allowed.classes");
			if (predecessorStart != -1) {
				// skip the beginning of the new line
				predecessorStart += eol.length();
				// see where ends the line of the predecessor and insert the new
				// property there
				int predecessorEnd = strbuf.indexOf(eol, predecessorStart) + eol.length();
				strbuf.insert(predecessorEnd, eol + v70NotificationsSuppression);

			} else {
				// add it at the end
				strbuf.append(eol);
				strbuf.append(v70NotificationsSuppression);
			}
		}

		String v70APIAuditProperty = "api.audit.on";
		String v70APIAudit = v70APIAuditProperty + "=false" + eol;
		if (strbuf.indexOf(v70APIAuditProperty) == -1) {

			/*
			 * Insert the property after the "api.on" property to maintain
			 * logical ordering. To shield against commented occurrences of the
			 * property, search for it at the beginning of a new line.
			 */
			int predecessorStart = strbuf.indexOf(eol + "api.on");
			if (predecessorStart != -1) {
				// skip the beginning of the new line
				predecessorStart += eol.length();
				// see where ends the line of the predecessor and insert the new
				// property there
				int predecessorEnd = strbuf.indexOf(eol, predecessorStart) + eol.length();
				strbuf.insert(predecessorEnd, v70APIAudit);

			} else {
				// add it at the end
				strbuf.append(eol);
				strbuf.append(v70APIAudit);
			}
		}

		String v70LDAPGroupSupportProperty = "api.custom.authentication.ldap.groupsupport";
		String v70LDAPGroupSupport = "## This property specifies whether LDAP Group authentication is turned on."
				+ eol
				+ "## If it is set to 'true', the group membership of the authenticating users will be resolved and will be taken into account during authorization."
				+ eol + "## If it is missing, the default value 'false' is used." + eol + v70LDAPGroupSupportProperty + "=false"
				+ eol;
		String v70LDAPGroupMembershipProperty = "api.custom.authentication.ldap.usermembershipattribute";
		String v70LDAPGroupMembership = "## Specifies the name of the attribute of a user in LDAP that contains a list of the groups of which the user is a member."
				+ eol
				+ "## It is taken int oaccount only if 'api.custom.authentication.ldap.groupsupport' is set to true. "
				+ eol
				+ v70LDAPGroupMembershipProperty + "=" + eol;
		String v70LDAPGroupMembershipContentProperty = "api.custom.authentication.ldap.usermembershipattributecontent";
		String v70LDAPGroupMembershipContent = "## Specifies how groups are named in the membership attribute of a user."
				+ eol
				+ "## For example, if the user's membership attribute contains values, which correspond to the 'objectSID' attributes of groups, set this property to 'objectSID'."
				+ eol
				+ "## If the user's membership attribute contains distinguished names of groups, then set this property to 'dn'."
				+ eol + "## The property is required in case 'api.custom.authentication.ldap.groupsupport' is set to true. " + eol
				+ v70LDAPGroupMembershipContentProperty + "=" + eol;
		String v70LDAPGroupNameProperty = "api.custom.authentication.ldap.groupnameattribute";
		String v70LDAPGroupName = "## Specifies the name of a group's attribute in LDAP which corresponds to the way the group is named in the SDI User Registry."
				+ eol
				+ "## For example, if LDAP groups are addressed in the SDI registry by their common name, then set this property to 'cn'."
				+ eol
				+ "## If the User Registry contains the distinguished names of the groups, then set this property to 'dn'."
				+ eol + v70LDAPGroupNameProperty + "=" + eol;
		String v70LDAPGroupSearchbaseProperty = "api.custom.authentication.ldap.groupsearchbase";
		String v70LDAPGroupSearchbase = "## Represents the LDAP directory context, where groups will be searched." + eol
				+ "## It is required only when LDAP group support is enabled." + eol + v70LDAPGroupSearchbaseProperty + "=" + eol;
		String v70LDAPGroupBinaryProperty = "api.custom.authentication.ldap.binaryattributes";
		String v70LDAPGroupBinary = "## Optional property, which represents a list of space-separated attribute names. Specifies attributes which have non-string syntax."
				+ eol + "## " + v70LDAPGroupBinaryProperty + "=" + eol;

		String v70LDAPGroups = v70LDAPGroupSupport + eol + v70LDAPGroupMembership + eol + v70LDAPGroupMembershipContent + eol
				+ v70LDAPGroupName + eol + v70LDAPGroupSearchbase + eol + v70LDAPGroupBinary;

		if (strbuf.indexOf(v70LDAPGroupSupportProperty) == -1) {
			/*
			 * Insert the property after the
			 * "api.custom.authentication.ldap.adminpassword" property to
			 * maintain logical ordering. To shield against commented
			 * occurrences of the property, search for it at the beginning of a
			 * new line.
			 */
			int predecessorStart = strbuf.indexOf(eol + "{protect}-api.custom.authentication.ldap.adminpassword");
			if (predecessorStart != -1) {
				// skip the beginning of the new line
				predecessorStart += eol.length();
				// see where ends the line of the predecessor and insert the new
				// property there
				int predecessorEnd = strbuf.indexOf(eol, predecessorStart) + eol.length();
				strbuf.insert(predecessorEnd, eol + v70LDAPGroups);

			} else {
				// add it at the end
				strbuf.append(eol);
				strbuf.append(v70LDAPGroups);
			}
		}

		String v71ServerPorts = "api.remote.server.ports";
		if (strbuf.indexOf(v71ServerPorts) == -1) {
			String value = eol + "# " + v71ServerPorts + "=8700-8900";
			// Try to insert at the correct point
			int point = strbuf.indexOf(v60AddSnippet62);
			if (point > 0)
				point = strbuf.indexOf(eol, point);
			if (point > 0) {
				strbuf.insert(point, value);
			} else {
				strbuf.append(value);
				strbuf.append(eol);
			}
		}

		// ## Touchpoint Server properties
		// tp.server.on=false
		// tp.server.config=etc/tp.xml
		// tp.server.auth=false
		// tp.server.auth.realm=Tivoli Directory Integrator Touchpoint Server
		String v71AddTPServerComment = eol + "## Touchpoint Server settings" + eol;
		String v71AddTPServerProperties = "tp.server.on=false" + eol + "tp.server.config=etc/tp.xml" + eol + "tp.server.auth=false"
				+ eol + "tp.server.auth.realm=Security Verify Directory Integrator Touchpoint Server";

		if (strbuf.indexOf("tp.server.on") == -1) {
			strbuf.append(v71AddTPServerComment);
			strbuf.append(v71AddTPServerProperties);
			strbuf.append(eol);
		}

		String v71BindAddressesComments = eol
				+ "##The properties determine the default bind address and the remote bind address for the Server API."
				+ eol
				+ "## * means bind to all network interfaces. The Remote Bind Address overrides the Default one."
				+ eol
				+ "## Only one IP address should be set. No hostnames are accepted."
				+ eol
				+ "## Mind that the java.rmi.server.hostname property is set implicitly to equal the Remote Bind Address property when used."
				+ eol + "##This will cause the client stubs to create sockets on the specified Remote Bind Address." + eol;
		String v71DefaultBindAddress = "com.ibm.di.default.bind.address";
		String v71RemoteBindAddress = "api.remote.bind.address";

		if (strbuf.indexOf(v71DefaultBindAddress) == -1) {
			String v71DefaultBindAddressProp = "#" + v71DefaultBindAddress + "=*" + eol;
			strbuf.append(v71BindAddressesComments);
			strbuf.append(v71DefaultBindAddressProp);
			strbuf.append(eol);
		}
		if (strbuf.indexOf(v71RemoteBindAddress) == -1) {
			String v71RemoteBindAddressProp = "#" + v71RemoteBindAddress + "=*" + eol;
			strbuf.append(v71RemoteBindAddressProp);
		}

		// Custom Server API client properties
		String v71ServerAPIClientComment = "##" + eol + "## Server API client properties" + eol + "##" + eol;
		if (strbuf.indexOf("api.client.ssl.custom.properties.on") == -1) {

			strbuf.append(eol);
			strbuf.append(eol);
			strbuf.append(eol);
			strbuf.append(v71ServerAPIClientComment);

			strbuf.append("api.client.ssl.custom.properties.on=true" + eol);

			String jsseKeyStore = getPropertyValue(strbuf, "javax.net.ssl.keyStore=");
			if (jsseKeyStore != null && jsseKeyStore.trim().length() > 0) {
				strbuf.append("api.client.keystore=" + getPropertyValue(strbuf, "javax.net.ssl.keyStore=") + eol);
				strbuf.append("{protect}-api.client.keystore.pass=" + getPropertyValue(strbuf, "javax.net.ssl.keyStorePassword=")
						+ eol);
				strbuf.append("api.client.keystore.type=" + getPropertyValue(strbuf, "javax.net.ssl.keyStoreType=") + eol);
				strbuf.append("{protect}-api.client.key.pass=" + getPropertyValue(strbuf, "javax.net.ssl.keyStorePassword=") + eol);
			} else {
				strbuf.append("api.client.keystore=serverapi/testadmin.jks" + eol);
				strbuf.append("{protect}-api.client.keystore.pass=administrator" + eol);
				strbuf.append("api.client.keystore.type=jks" + eol);
				strbuf.append("{protect}-api.client.key.pass=administrator" + eol);
			}

			String jsseTrustStore = getPropertyValue(strbuf, "javax.net.ssl.trustStore=");
			if (jsseTrustStore != null && jsseTrustStore.trim().length() > 0) {
				strbuf.append("api.client.truststore=" + getPropertyValue(strbuf, "javax.net.ssl.trustStore=") + eol);
				strbuf.append("{protect}-api.client.truststore.pass="
						+ getPropertyValue(strbuf, "javax.net.ssl.trustStorePassword=") + eol);
				strbuf.append("api.client.truststore.type=" + getPropertyValue(strbuf, "javax.net.ssl.trustStoreType=") + eol);
			} else {
				strbuf.append("api.client.truststore=serverapi/testadmin.jks" + eol);
				strbuf.append("{protect}-api.client.truststore.pass=administrator" + eol);
				strbuf.append("api.client.truststore.type=jks" + eol);
			}
		}

		// Checks if MQe configuration already exists.
		boolean mqeConfigurationExists = strbuf.indexOf(v60AddSnippet1510) != -1;

		// ActiveMQ as System Queue properties
		String v72ActiveMQSystemQueueDriverName = "systemqueue.jmsdriver.name";
		String v72ActiveMQSystemQueueDriverValue = "com.ibm.di.systemqueue.driver.ActiveMQ";
		String v72ActiveMQSystemQueueDriverProp = v72ActiveMQSystemQueueDriverName + "=" + v72ActiveMQSystemQueueDriverValue;
		if (strbuf.indexOf(v72ActiveMQSystemQueueDriverProp) == -1) {
			addProperty(strbuf, v72ActiveMQSystemQueueDriverName, v72ActiveMQSystemQueueDriverValue, null, v60AddSnippet159,
					mqeConfigurationExists);
		}

		// ActiveMQ Driver properties
		String v72ActiveMQDriverBrokerName = "systemqueue.jmsdriver.param.jms.broker";
		String v72ActiveMQDriverBrokerValue = "vm://localhost?brokerConfig=xbean:etc/activemq.xml";
		String v72ActiveMQDriverBrokerProp = v72ActiveMQDriverBrokerName + "=" + v72ActiveMQDriverBrokerValue;
		String v72ActiveMQDriverComments = "### ActiveMQ driver initialization properties" + eol //
				+ "## Specifies the location of the ActiveMQ initialization file." + eol //
				+ "## This file is used to initialize ActiveMQ on SDI server startup.";
		if (strbuf.indexOf(v72ActiveMQDriverBrokerProp) == -1) {
			addProperty(strbuf, v72ActiveMQDriverBrokerName, v72ActiveMQDriverBrokerValue, v72ActiveMQDriverComments,
					v60AddSnippet1523, mqeConfigurationExists);
		}

		
		// ## Web container
		// web.server.port=1098
		// web.server.ssl.on=false
		// web.server.ssl.client.auth.on=false
		// # web.server.session.timeout=300
		String v72AddWebServerComment = eol + "## Web Container Settings" + eol;
		//Assuming we have captured tpServerPort in PerformDeletions()
		//String tpServerPort = getPropertyValue(strbuf, "tp.server.port");
		if ((tpServerPort  == null) || (tpServerPort.isEmpty()) || (tpServerPort.equals(""))) {
			tpServerPort = "1098";
		}

		//Refer RTC Task#43375
		String v72AddWebServerProperties = "web.server.port=" + tpServerPort + eol + "web.server.ssl.on=true" + eol
				+ "web.server.ssl.client.auth.on=false" + eol + "# web.server.session.timeout=300";

		if (strbuf.indexOf("web.server.port") == -1) {
			strbuf.append(v72AddWebServerComment);
			strbuf.append(v72AddWebServerProperties);
			strbuf.append(eol);
		}

		// ## REST API
		// ## ----------------------
		// api.rest.on=true
		// api.rest.auth=false
		// api.rest.auth.realm=Tivoli Directory Integrator REST API
		//
		// api.rest.jmsdriver.name=com.ibm.di.systemqueue.driver.ActiveMQ
		// api.rest.jmsdriver.queue.sender.persistance=false
		// api.rest.jmsdriver.queue.sender.timeToLive=60000
		// api.rest.jmsdriver.param.jms.broker=vm://localhost?brokerConfig=xbean:etc/activemq.xml
		// # api.rest.jmsdriver.auth.username
		// # api.rest.jmsdriver.auth.password
		String v72AddRestComment = eol + "## REST API" + eol + "## ----------------------" + eol;
		String v72AddRestProperties = "api.rest.on=true" + eol + "api.rest.auth=false" + eol
				+ "api.rest.auth.realm=Security Verify Directory Integrator REST API" + eol + eol
				+ "api.rest.jmsdriver.name=com.ibm.di.systemqueue.driver.ActiveMQ" + eol
				+ "api.rest.jmsdriver.queue.sender.persistance=false" + eol + "api.rest.jmsdriver.queue.sender.timeToLive=60000"
				+ eol + "api.rest.jmsdriver.param.jms.broker=vm://localhost?brokerConfig=xbean:etc/activemq.xml" + eol
				+ "# api.rest.jmsdriver.auth.username" + eol + "# api.rest.jmsdriver.auth.password";

		if (strbuf.indexOf("api.rest.on") == -1) {
			strbuf.append(v72AddRestComment);
			strbuf.append(v72AddRestProperties);
			strbuf.append(eol);
		}
		
		//## Dashboard properties
		//##
		//dashboard.on=true
		//dashboard.templates.folder=dashboard/templates

		//## Dashboard authentication properties
		//##
		//## The values for localhost and remotehost can be:
		//##	none: No authentication is required
		//##	deny: All connections denied
		//##	ldap: Authentication is done by logging into an LDAP server and optionally validating group membership
		//##
		//## dashboard.ldap.url
		//##   Specify the LDAP host port and optionally a search base (ldap://<host>:<port>[/<search-base>])
		//##
		//## dashboard.ldap.url.group
		//##   Specify the LDAP host port and optionally a search base (ldap://<host>:<port>[/<search-base>])
		//##
		//dashboard.auth=true
		//dashboard.auth.localhost=none
		//dashboard.auth.remote=deny
		//# dashboard.auth.ldap.url=ldap://localhost:389/ou=users,ou=system
		//# dashboard.auth.ldap.url.group=ldap://localhost:389/cn=group1,ou=groups,ou=system
		
		String v711AddDashboardComments1 = eol + "## Dashboard properties"
				+ eol + "## " + eol;
		String v711AddDashboardProperties1 = "dashboard.on=true" + eol
				+ "dashboard.templates.folder=dashboard/templates" + eol + eol;
		String v711AddDashboardComments2 = "## Dashboard authentication properties"
				+ eol
				+ "##"
				+ eol
				+ "## The values for localhost and remotehost can be:"
				+ eol
				+ "##	none: No authentication is required"
				+ eol
				+ "##	deny: All connections denied"
				+ eol
				+ "##	ldap: Authentication is done by logging into an LDAP server and optionally validating group membership"
				+ eol
				+ "##	properties: Authentication is done using dashboard.auth.user.[username]=[password] properties"
				+ eol
				+ "##"
				+ eol
				+ "## dashboard.ldap.url"
				+ eol
				+ "##   Specify the LDAP host port and optionally a search base (ldap://<host>:<port>[/<search-base>])"
				+ eol
				+ "##"
				+ eol
				+ "## dashboard.ldap.url.group"
				+ eol
				+ "##   Specify the LDAP host port and optionally a search base (ldap://<host>:<port>[/<search-base>])"
				+ eol + "##" + eol;
		String v711AddDashboardProperties2 = "dashboard.auth=true" + eol
				+ "dashboard.auth.localhost=properties" + eol
				+ "dashboard.auth.remote=deny" + eol;
		String v711AddDashboardProperties3 = "# dashboard.auth.ldap.url=ldap://localhost:389/ou=users,ou=system"
				+ eol
				+ "# dashboard.auth.ldap.url.group=ldap://localhost:389/cn=group1,ou=groups,ou=system"
				+ eol;
		String v72AddDashboardProperties4 = "# Default FDS username/password"
				+ eol
				+ "{protect}-dashboard.auth.user.admin=admin"
				+ eol;

		if (strbuf.indexOf("dashboard.on") == -1) {
			strbuf.append(v711AddDashboardComments1);
			strbuf.append(v711AddDashboardProperties1);
			strbuf.append(v711AddDashboardComments2);
			strbuf.append(v711AddDashboardProperties2);
			strbuf.append(v711AddDashboardProperties3);
			strbuf.append(v72AddDashboardProperties4);
			strbuf.append(eol);
		}
		
		//api.keystore.type=jks property need to be added - might be newly started from 711 onwards
		String v711apiKeystoreTypeProperty = eol + "api.keystore.type=jks" + eol;
		if (strbuf.indexOf("api.keystore.type") == -1) {
			strbuf.append(v711apiKeystoreTypeProperty);
			strbuf.append(eol);
		}
		
		//SDI72 - Add NIST properties if not present
		String v72nistComments = "## ----------------------------------" 
				+ eol 
				+ "## Enabling/Disabling NIST Mode in SDI"
				+ eol
				+ "##------------------------------------"
				+ eol 
				+ "## If the below property is set to true then SDI will be enforced to run in NIST Compliant Mode."
				+ eol
				+ "## The default value is false, i.e. SDI will not run in NIST Mode by default.";
		String v72nistProperty = "com.ibm.di.server.NIST.on=";
		String v72nistPropertyValue = "false";
		if(strbuf.indexOf(v72nistProperty) == -1){
			strbuf.append(eol + v72nistComments + eol + v72nistProperty + v72nistPropertyValue + eol);
		}
		
	}

	/* Performs the Derby specific changes */
	private static void performDerbyChanges(StringBuilder strbuf) {
		int startIndex;
		int eolIndex;
		boolean alreadyCloudScape10 = false;

		/* Snippet 2: [Regarding Derby - Embedded Mode] */

		/*
		 * This code was ported from code written to migrate a V6.0
		 * global.properties file ' to a V6.1 file. Assumptions were made that
		 * are not true if migrating from V6.1 or V6.1.1 to say V7.0 So first
		 * determine if the global.properties file is already using Derby 10 If
		 * its not, then do the the V6.0 stuff.
		 */
		startIndex = strbuf.indexOf("Cloudscape 10");
		if (startIndex != -1)
			alreadyCloudScape10 = true;

		/*
		 * If Derby is being migrated then If the embedded properties are
		 * commented out Add the new properties as comments Else if the embedded
		 * properties are not commented out Comment out the original properties
		 * Add the new properties
		 * 
		 * Else if Derby is not being migrated Add the new properties as
		 * comments
		 */

		/* -> TDI 60 */
		String v60ModSnippet22 = "com.ibm.di.store.database=CloudScape";
		String v60ModSnippet23 = "com.ibm.di.store.jdbc.driver=com.ibm.db2j.jdbc.DB2jDriver";
		String v60ModSnippet24 = "com.ibm.di.store.jdbc.urlprefix=jdbc:db2j:";

		/* -> TDI 61 */
		String v61ModSnippet21 = "com.ibm.di.store.database=TDISysStore" + eol; // Name
		// of
		// the
		// migrated
		// Derby
		// system
		// store]
		String v61ModSnippet22 = "com.ibm.di.store.jdbc.driver=org.apache.derby.jdbc.EmbeddedDriver" + eol;
		String v61ModSnippet23 = "com.ibm.di.store.jdbc.urlprefix=jdbc:derby:" + eol;

		/* If Derby is being migrated */
		if (tdiMigrateDerby) {
			/* If the embedded properties are currently commented out */
			startIndex = strbuf.indexOf(v60ModSnippet22);
			if (strbuf.substring(startIndex - 1, startIndex).equalsIgnoreCase("#")) {
				/*
				 * Insert the new properties after the current ones (as
				 * comments)
				 */
				startIndex = strbuf.indexOf(v60ModSnippet24);
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length();

				// logUtil.writeToLog("Adding new embedded properties as
				// comments");
				strbuf.insert(startIndex, "#" + v61ModSnippet21);

				startIndex = startIndex + v61ModSnippet21.length() + "#".length();
				strbuf.insert(startIndex, "#" + v61ModSnippet22);

				startIndex = startIndex + v61ModSnippet22.length() + "#".length();
				strbuf.insert(startIndex, "#" + v61ModSnippet23);
			}
			/* else comment out the current properties and add the new ones */
			else {
				// logUtil.writeToLog("Commenting out current properties and
				// adding the new ones");
				strbuf.insert(startIndex, "#");

				startIndex = strbuf.indexOf(v60ModSnippet23);
				strbuf.insert(startIndex, "#");

				startIndex = strbuf.indexOf(v60ModSnippet24);
				strbuf.insert(startIndex, "#");

				startIndex = startIndex + v60ModSnippet24.length() + 1 + eol.length(); // +1
				// for
				// the
				// added
				// #
				strbuf.insert(startIndex, v61ModSnippet21);

				startIndex = startIndex + v61ModSnippet21.length();
				strbuf.insert(startIndex, v61ModSnippet22);

				startIndex = startIndex + v61ModSnippet22.length();
				strbuf.insert(startIndex, v61ModSnippet23);
			}
		}
		/*
		 * Else Derby is not being updated and we are not already at Cloudscape
		 * 10, so just add the new properties as comments
		 */
		else if (!alreadyCloudScape10) {
			/*
			 * Insert the new properties after the current ones (as comments)
			 */

			startIndex = strbuf.indexOf(v60ModSnippet24);
			eolIndex = strbuf.indexOf(eol, startIndex);
			startIndex = eolIndex + eol.length();

			// logUtil.writeToLog("Adding new embedded properties as
			// comments");

			strbuf.insert(startIndex, "#" + v61ModSnippet21);

			startIndex = startIndex + v61ModSnippet21.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet22);

			startIndex = startIndex + v61ModSnippet22.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet23);
		}

		/* Snippet 3: [Regarding Derby - Network Mode] */

		// logUtil.writeToLog("Modifying Snippet 3");
		/* -> TDI 60 */
		String v60ModSnippet30 = "## Location of the database (networked mode)";
		String v60ModSnippet31 = "com.ibm.di.store.database=";
		String v60ModSnippet32 = "com.ibm.di.store.jdbc.driver=";
		String v60ModSnippet33 = "com.ibm.di.store.jdbc.urlprefix=";
		String v60ModSnippet34 = "com.ibm.di.store.port=";

		/* -> TDI 61 */
		String v61ModSnippet31 = "com.ibm.di.store.database=jdbc:derby://localhost:1527/$change$\\TDISysStore;create=true" + eol;
		String v61ModSnippet32 = "com.ibm.di.store.jdbc.driver=org.apache.derby.jdbc.ClientDriver" + eol;
		String v61ModSnippet33 = "com.ibm.di.store.jdbc.urlprefix=jdbc:derby://localhost:1527/" + eol;
		String v61ModSnippet34 = "com.ibm.di.store.hostname=localhost" + eol;

		/*
		 * If Derby is being migrated then If the network properties are
		 * commented out Add the new properties as comments Else if the network
		 * properties are not commented out Comment out the original properties
		 * Add the new properties
		 * 
		 * Else if Derby is not being migrated Add the new properties as
		 * comments
		 */

		startIndex = strbuf.indexOf(v60ModSnippet30);
		startIndex = strbuf.indexOf(v60ModSnippet31, startIndex);

		/* If Derby is being updated */
		if (tdiMigrateDerby) {

			/* If the network properties are commented out */
			if (strbuf.substring(startIndex - 1, startIndex).equalsIgnoreCase("#")) {

				// logUtil.writeToLog("Adding new network properties as
				// comments");

				/* Add the new properties as comments */
				eolIndex = strbuf.indexOf(eol, startIndex);

				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, "#" + v61ModSnippet31);

				startIndex = strbuf.indexOf(v60ModSnippet32, startIndex);
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, "#" + v61ModSnippet32);

				startIndex = strbuf.indexOf(v60ModSnippet33, startIndex);
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, "#" + v61ModSnippet33);

				/*
				 * this string needs to go before the v60 string, so back up to
				 * be in front of the #
				 */
				startIndex = strbuf.indexOf(v60ModSnippet34);
				strbuf.insert(startIndex - 1, "#" + v61ModSnippet34);
			}

			/* Else comment out the old property and add the new property */
			else {
				// logUtil.writeToLog("Commenting out current network properties
				// and adding the new ones");

				strbuf.insert(startIndex, "#");

				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, v61ModSnippet31);

				startIndex = strbuf.indexOf(v60ModSnippet32, startIndex);
				strbuf.insert(startIndex, "#");

				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, v61ModSnippet32);

				startIndex = strbuf.indexOf(v60ModSnippet33, startIndex);
				strbuf.insert(startIndex, "#");

				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = eolIndex + eol.length(); // skip over the end of
				// line .... should be
				// at the next line
				strbuf.insert(startIndex, v61ModSnippet33);

				/* This is a new string so there's nothing to replace */
				startIndex = strbuf.indexOf(v60ModSnippet34);
				strbuf.insert(startIndex, v61ModSnippet34);
			}
		}

		/*
		 * Else Derby is not being updated and we are not already at CloudScape
		 * 10, so just add the new properties as comments
		 */
		else if (!alreadyCloudScape10) {
			/*
			 * Insert the new properties after the current ones (as comments)
			 */

			// logUtil.writeToLog("Adding new network properties as
			// comments");
			// Add the new properties as comments //
			eolIndex = strbuf.indexOf(eol, startIndex);

			startIndex = eolIndex + eol.length();
			strbuf.insert(startIndex, "#" + v61ModSnippet31);

			startIndex = strbuf.indexOf(v60ModSnippet32, startIndex);
			eolIndex = strbuf.indexOf(eol, startIndex);
			startIndex = eolIndex + eol.length(); // skip over the end of
			// line
			// .... should be at the
			// next line
			strbuf.insert(startIndex, "#" + v61ModSnippet32);

			startIndex = strbuf.indexOf(v60ModSnippet33, startIndex);
			eolIndex = strbuf.indexOf(eol, startIndex);
			startIndex = eolIndex + eol.length(); // skip over the end of
			// line
			// .... should be at the
			// next line
			strbuf.insert(startIndex, "#" + v61ModSnippet33);

			startIndex = strbuf.indexOf(v60ModSnippet34);
			/*
			 * If com.ibm.di.store.port= is commented out, make sure we insert
			 * before the #
			 */
			if (strbuf.substring(startIndex - 1, startIndex).equalsIgnoreCase("#"))
				strbuf.insert(startIndex - 1, "#" + v61ModSnippet34);
			else
				strbuf.insert(startIndex, "#" + v61ModSnippet34);
		}

		/*
		 * Snippet 4: [Regarding Derby - SQL Statements have changed. The
		 * Datatype from "long varbinary" has become "BLOB"] In V6.11 the
		 * "ALTER" strings were added
		 */

		/*
		 * If Derby is being migrated then Comment out the original properties
		 * Add the new properties
		 * 
		 * Else if Derby is not being migrated Add the new properties as
		 * comments
		 */

		// logUtil.writeToLog("Modifying Snippet 4");
		/* -> TDI 60 */
		String v60ModSnippet42 = "com.ibm.di.store.create.delta.systable=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int)";
		String v60ModSnippet43 = "com.ibm.di.store.create.delta.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY long varbinary )";
		String v60ModSnippet44 = "com.ibm.di.store.create.property.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY long varbinary )";
		String v60ModSnippet46 = "com.ibm.di.store.create.sandbox.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY long varbinary )";

		/* -> TDI 6.1.1 */
		String v611ModSnippet42b = "; ALTER TABLE {0} ADD CONSTRAINT IDI_CS_{UNIQUE} Primary Key (ID)";
		String v611ModSnippet43b = "; ALTER TABLE {0} ADD CONSTRAINT IDI_DS_{UNIQUE} Primary Key (ID)";
		String v611ModSnippet44b = "; ALTER TABLE {0} ADD CONSTRAINT IDI_PS_{UNIQUE} Primary Key (ID)";
		String v611ModSnippet45b = "; ALTER TABLE {0} ADD CONSTRAINT IDI_CR_{UNIQUE} Primary Key (ID)";

		/* -> TDI 61 */
		String v61ModSnippet41 = eol + eol + "# create statements for system store tables (CloudScape 10)" + eol;
		String v61ModSnippet42 = "com.ibm.di.store.create.delta.systable=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int)"
				+ v611ModSnippet42b + eol;
		String v61ModSnippet43 = "com.ibm.di.store.create.delta.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB )"
				+ v611ModSnippet43b + eol;
		String v61ModSnippet44 = "com.ibm.di.store.create.property.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )"
				+ v611ModSnippet44b + eol;
		String v61ModSnippet46 = "com.ibm.di.store.create.sandbox.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )"
				+ eol;
		String v61ModSnippet47 = "com.ibm.di.store.create.recal.conops=CREATE TABLE {0} (METHOD varchar(VARCHAR_LENGTH), RESULT BLOB, ERROR BLOB)"
				+ eol;

		/* -> 611 */
		String v611ModSnippet41 = "# create statements for system store tables (CloudScape 10)";
		String v611ModSnippet42 = "com.ibm.di.store.create.delta.systable=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, VERSION int)";
		String v611ModSnippet43 = "com.ibm.di.store.create.delta.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, SEQUENCEID int, ENTRY BLOB )";
		String v611ModSnippet44 = "com.ibm.di.store.create.property.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ENTRY BLOB )";
		String v611ModSnippet45 = "com.ibm.di.store.create.checkpoint.store=CREATE TABLE {0} (ID VARCHAR(VARCHAR_LENGTH) NOT NULL, ALSTATE BLOB, ENTRY BLOB, TCB BLOB )";

		/* If Derby is being migrated */
		if (tdiMigrateDerby) {

			// logUtil.writeToLog("Commenting out SQL properties and adding
			// the
			// new ones");
			startIndex = strbuf.indexOf(v60ModSnippet42);
			strbuf.insert(startIndex, "#");

			startIndex = strbuf.indexOf(v60ModSnippet43);
			strbuf.insert(startIndex, "#");

			startIndex = strbuf.indexOf(v60ModSnippet44);
			strbuf.insert(startIndex, "#");

			// -- no more checkpoints in 7.0
			// startIndex = strbuf.indexOf(v60ModSnippet45);
			// strbuf.insert(startIndex, "#");

			startIndex = strbuf.indexOf(v60ModSnippet46);
			strbuf.insert(startIndex, "#");

			startIndex = startIndex + v60ModSnippet46.length() + 1 + eol.length(); // +1
			// for
			// #
			strbuf.insert(startIndex, v61ModSnippet41);

			startIndex = startIndex + v61ModSnippet41.length();
			strbuf.insert(startIndex, v61ModSnippet42);

			startIndex = startIndex + v61ModSnippet42.length();
			strbuf.insert(startIndex, v61ModSnippet43);

			startIndex = startIndex + v61ModSnippet43.length();
			strbuf.insert(startIndex, v61ModSnippet44);

			// -- no more checkpoints in 7.0
			// startIndex = startIndex + v61ModSnippet44.length();
			// strbuf.insert(startIndex, v61ModSnippet45);

			startIndex = startIndex + v61ModSnippet44.length();
			strbuf.insert(startIndex, v61ModSnippet46);

			startIndex = startIndex + v61ModSnippet46.length();
			strbuf.insert(startIndex, v61ModSnippet47);
		}

		/*
		 * If we are not already at Cloudscape 10 just add the Cloudscape 10 SQL
		 * statements as comments
		 */
		else if (!alreadyCloudScape10) {
			// logUtil.writeToLog("Adding Cloudscape 10 SQL properties as
			// comments");

			startIndex = strbuf.indexOf(v60ModSnippet46);
			eolIndex = strbuf.indexOf(eol, startIndex);
			startIndex = eolIndex + eol.length();

			strbuf.insert(startIndex, "#" + v61ModSnippet41);

			startIndex = startIndex + v61ModSnippet41.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet42);

			startIndex = startIndex + v61ModSnippet42.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet43);

			startIndex = startIndex + v61ModSnippet43.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet44);

			// -- no more checkpoints in 7.0
			// startIndex = startIndex + v61ModSnippet44.length() +
			// "#".length();
			// strbuf.insert(startIndex, "#" + v61ModSnippet45);

			startIndex = startIndex + v61ModSnippet44.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet46);

			startIndex = startIndex + v61ModSnippet46.length() + "#".length();
			strbuf.insert(startIndex, "#" + v61ModSnippet47);
		}

		/*
		 * Else we are already at Cloudscape 10. We need to insert the new
		 * strings
		 */
		else {

			/* Start by finding the Cloudscape 10 lines */
			int cloudScape10start = strbuf.indexOf(v611ModSnippet41);

			startIndex = strbuf.indexOf(v611ModSnippet42, cloudScape10start);
			if (startIndex >= 0 && strbuf.charAt(startIndex + v611ModSnippet42.length()) != ';')
				strbuf.insert(startIndex + v611ModSnippet42.length(), v611ModSnippet42b);

			startIndex = strbuf.indexOf(v611ModSnippet43, cloudScape10start);
			if (startIndex >= 0 && strbuf.charAt(startIndex + v611ModSnippet43.length()) != ';')
				strbuf.insert(startIndex + v611ModSnippet43.length(), v611ModSnippet43b);

			startIndex = strbuf.indexOf(v611ModSnippet44, cloudScape10start);
			if (startIndex >= 0 && strbuf.charAt(startIndex + v611ModSnippet44.length()) != ';')
				strbuf.insert(startIndex + v611ModSnippet44.length(), v611ModSnippet44b);

			startIndex = strbuf.indexOf(v611ModSnippet45, cloudScape10start);
			if (startIndex >= 0 && strbuf.charAt(startIndex + v611ModSnippet45.length()) != ';')
				strbuf.insert(startIndex + v611ModSnippet45.length(), v611ModSnippet45b);
		}

		String v70ModIncorrectProperty = "derby.database.defaultAccessMode";
		startIndex = strbuf.indexOf(v70ModIncorrectProperty);
		if (startIndex != -1) {
			/*
			 * Edit the property to the right value
			 * 'derby.database.defaultConnectionMode'.
			 */
			String wrongWord = "Access";
			String rightWord = "Connection";
			startIndex = strbuf.indexOf(wrongWord);
			strbuf.delete(startIndex, startIndex + wrongWord.length());
			strbuf.insert(startIndex, rightWord);
		}

		// Derby authentication properties
		String v70DerbyAuthComment = "#" + eol + "## Derby (Cloudscape) properties required for enabling authentication" + eol
				+ "#" + eol;
		String v70StartNetworkServer = "derby.drda.startNetworkServer";
		String v70StartNetworkServerValue = "true";
		String v70RequireAuth = "derby.connection.requireAuthentication";
		String v70RequireAuthValue = "true";
		String v70AuthProvider = "derby.authentication.provider";
		String v70AuthProviderValue = "BUILTIN";
		String v70ConnectionMode = "derby.database.defaultConnectionMode";
		String v70ConnectionModeValue = "fullAccess";

		addDerbyAuthProperty(strbuf, v70StartNetworkServer, v70StartNetworkServerValue, v70DerbyAuthComment);
		addDerbyAuthProperty(strbuf, v70RequireAuth, v70RequireAuthValue, "");
		addDerbyAuthProperty(strbuf, v70AuthProvider, v70AuthProviderValue, "");
		addDerbyAuthProperty(strbuf, v70ConnectionMode, v70ConnectionModeValue, "");
	}

	/**
	 * This method adds the Derby authentication properties used by TDI.
	 * 
	 * @param builder
	 *            the builder where modifications are made
	 * @param name
	 *            property key
	 * @param value
	 *            property value
	 * @param comment
	 *            a comment clarifying the property (if available)
	 */
	private static void addDerbyAuthProperty(StringBuilder builder, String name, String value, String comment) {
		int startIndex = builder.indexOf(name);
		if (startIndex == -1) {
			addProperty(builder, name, value, comment, startIndex, false);
		}
	}

	/**
	 * 
	 * @param builder
	 *            the builder where modifications are made
	 * @param name
	 *            property key
	 * @param value
	 *            property value
	 * @param comment
	 *            a comment clarifying the property (if available)
	 * @param insertPoint
	 *            point where new property will be added (after EOL from this
	 *            insert point). Negative value means at the end of the file.
	 * @param addCommented
	 *            add new property commented (start with #)
	 */
	private static void addProperty(StringBuilder builder, String name, String value, String comment, int insertPoint,
			boolean addCommented) {
		if (value == null) {
			value = "";
		}
		if (comment == null) {
			comment = "";
		} else {
			comment = eol + comment + eol;
		}

		String property = name + "=" + value;
		if (addCommented) {
			property = "# " + property;
		}
		property = eol + comment + property;

		// position at the next EOL
		if (insertPoint >= 0) {
			insertPoint = builder.indexOf(eol, insertPoint);
		}

		if (insertPoint >= 0) {
			builder.insert(insertPoint, property);
		} else {
			builder.append(property);
			builder.append(eol);
		}
	}

	/**
	 * 
	 * @param builder
	 *            the builder where modifications are made
	 * @param name
	 *            property key
	 * @param value
	 *            property value
	 * @param comment
	 *            a comment clarifying the property (if available)
	 * @param keyAfter
	 *            new property will be added after this key String (on new
	 *            line). If this key does not exist, the new property will be
	 *            add at the end of the file.
	 * @param addCommented
	 *            add new property commented (start with #)
	 */
	private static void addProperty(StringBuilder builder, String name, String value, String comment, String keyAfter,
			boolean addCommented) {
		int insertPoint = builder.indexOf(keyAfter);
		addProperty(builder, name, value, comment, insertPoint, addCommented);
	}

	private static void performModification(StringBuilder strbuf) {
		int startIndex;
		int endIndex;

		/* Snippet 1 */

		/* -> TDI 60 */
		String v60ModSnippet11 = "## Modify the line below to add your own directory containing jar/zip files. This directory will be searched recursively";
		String v60ModSnippet12 = "## by the TDILoader for class files and resources. Only files with a \"zip\" or \"jar\" extension are searched.";
		String v60ModSnippet13 = "##";

		/* -> TDI 61: Only the comment changes */
		String v61ModSnippet11 = "## Modify the line below to add your own jar/zip files.";
		String v61ModSnippet12 = "## The property may specify several directories or jar files, separated by the Java Property \"path.separator\",";
		String v61ModSnippet13 = "## which is \":\" on Linux and \";\" on Windows" + eol;
		String v61ModSnippet14 = "## Directories will be searched recursively by the TDILoader for jar files containing classes and resources."
				+ eol;
		String v61ModSnippet15 = "## Only files with a \".zip\" or \".jar\" extension are searched." + eol;

		// logUtil.writeToLog("Modifying Snippet 1");

		startIndex = strbuf.indexOf(v60ModSnippet11);
		if (startIndex != -1) {
			endIndex = startIndex + v60ModSnippet11.length();
			strbuf.replace(startIndex, endIndex, v61ModSnippet11);

			startIndex = strbuf.indexOf(v60ModSnippet12);
			endIndex = startIndex + v60ModSnippet12.length();
			strbuf.replace(startIndex, endIndex, v61ModSnippet12);

			startIndex = strbuf.indexOf(v60ModSnippet13, endIndex);
			strbuf.insert(startIndex, v61ModSnippet13);
			endIndex = startIndex + v61ModSnippet13.length();

			startIndex = strbuf.indexOf(v60ModSnippet13, endIndex);
			strbuf.insert(startIndex, v61ModSnippet14);
			endIndex = startIndex + v61ModSnippet14.length();

			startIndex = strbuf.indexOf(v60ModSnippet13, endIndex);
			strbuf.insert(startIndex, v61ModSnippet15);
		}

		/*
		 * Now we need to change all the $change$ to _TARGETDIR and all
		 * $jvmRoot$ to _TARGETDIR_JRE This is rather kludgy. It works here
		 * because PerformModifications is called after deletions and additions
		 * There is no $jvmRoot$ or $change$ being done here, they are being
		 * done in the other methods, but since this is called last, we can put
		 * it here and it will be done for all the changes made.
		 */
		// logUtil.writeToLog("modifyng the $change$'s");
		int changeIndex = strbuf.indexOf("$change$");
		while (changeIndex != -1) {
			strbuf.replace(changeIndex, changeIndex + "$change$".length(), targetDir);
			changeIndex = strbuf.indexOf("$change$", targetDir.length());
		}

		// logUtil.writeToLog("modifyng the $jvmRoot$'s");
		int jvmIndex = strbuf.indexOf("$jvmRoot$");
		while (jvmIndex != -1) {
			strbuf.replace(jvmIndex, jvmIndex + "$jvmRoot$".length(), targetDirJre);
			jvmIndex = strbuf.indexOf("$jvmRoot$", targetDirJre.length());
		}

		// UPDATED IN TDI 7.0
		String v611ModSnippet1 = "api.remote.on=false";
		String v70ModSnippet1 = "api.remote.on=true";
		startIndex = strbuf.indexOf(v611ModSnippet1);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v611ModSnippet1.length(), v70ModSnippet1);
		}

		String v611ModSnippet2 = "javax.net.ssl.trustStore=" + eol + "{protect}-javax.net.ssl.trustStorePassword=" + eol
				+ "javax.net.ssl.trustStoreType=" + eol;
		String v70ModSnippet2 = "javax.net.ssl.trustStore=serverapi" + File.separator + "testadmin.jks" + eol
				+ "{protect}-javax.net.ssl.trustStorePassword=administrator" + eol + "javax.net.ssl.trustStoreType=jks" + eol;

		startIndex = strbuf.indexOf(v611ModSnippet2);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v611ModSnippet2.length(), v70ModSnippet2);
		}

		String v611ModSnippet3 = "javax.net.ssl.keyStore=" + eol + "{protect}-javax.net.ssl.keyStorePassword=" + eol
				+ "javax.net.ssl.keyStoreType=" + eol;
		String v70ModSnippet3 = "javax.net.ssl.keyStore=serverapi" + File.separator + "testadmin.jks" + eol
				+ "{protect}-javax.net.ssl.keyStorePassword=administrator" + eol + "javax.net.ssl.keyStoreType=jks" + eol;

		startIndex = strbuf.indexOf(v611ModSnippet3);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v611ModSnippet3.length(), v70ModSnippet3);
		}
		// Add modifications in TDI 7.0 related to renaming of
		// com.ibm.di.server.keystore and com.ibm.di.server.alias

		String v611ModSnippet4 = "com.ibm.di.server.keystore=";
		String v611ModSnippet5 = "com.ibm.di.server.key.alias=";

		String v70ModSnippet4 = "api.keystore=";
		String v70ModSnippet5 = "api.key.alias=";

		startIndex = strbuf.indexOf(v611ModSnippet4);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v611ModSnippet4.length(), v70ModSnippet4);
		}

		startIndex = strbuf.indexOf(v611ModSnippet5);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v611ModSnippet5.length(), v70ModSnippet5);
		}

		// Add modifications in TDI 7.0 related so that the
		// api.config.folder is a relative path from now on.

		String v611ModSnippet5start = "api.config.folder=";
		String v70ModSnippet6 = "api.config.folder=configs";

		startIndex = strbuf.indexOf(v611ModSnippet5start);
		if (startIndex != -1) {
			endIndex = startIndex + strbuf.substring(startIndex).indexOf(eol);
			strbuf.replace(startIndex, endIndex, v70ModSnippet6);
		}

		String SysQStr = "systemqueue.on=";
		String SysQStrNew = "systemqueue.on=true" + eol;

		startIndex = strbuf.indexOf(SysQStr);
		if (startIndex != -1) {
			endIndex = startIndex + strbuf.substring(startIndex).indexOf(eol);

			// only change to relative path if the systemqueue.on is false
			if (!Boolean.parseBoolean(strbuf.substring(startIndex + SysQStr.length(), endIndex).trim())) {

				strbuf.replace(startIndex, endIndex, SysQStrNew);

				String iniFile = "systemqueue.jmsdriver.param.mqe.file.ini=";
				String iniFileNew = "systemqueue.jmsdriver.param.mqe.file.ini=" + "MQePWStore/pwstore_server.ini" + eol;

				startIndex = strbuf.indexOf(iniFile);
				if (startIndex != -1) {
					endIndex = startIndex + strbuf.substring(startIndex).indexOf(eol);

					strbuf.replace(startIndex, endIndex, iniFileNew);
				}

			}
		}

		// A few specific errors found in the 7.0 support stream. Apply this to
		// be safe.
		String v70BadHelp = "## http://publib.boulder.ibm.com/infocenter/ieduasst/tivv1r0/index.jsp?topic=/com.ibm.iea.tdi/tdi/TDIv70_Task.html";
		String v70GoodHelp = "## http://publib.boulder.ibm.com/infocenter/tiv2help/index.jsp?toc=/com.ibm.IBMDI.doc_7.0/toc.xml";

		startIndex = strbuf.indexOf(v70BadHelp);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v70BadHelp.length(), v70GoodHelp);
		}

		String v70BadHelpHost = "com.ibm.di.helpHost=publib.boulder.ibm.com/infocenter/ieduasst/tivv1r0/index.jsp?topic=";
		String v70GoodHelpHost = "com.ibm.di.helpHost=publib.boulder.ibm.com/infocenter/tivihelp/v2r1/index.jsp?topic=";

		startIndex = strbuf.indexOf(v70BadHelpHost);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v70BadHelpHost.length(), v70GoodHelpHost);
		}
	
		// SDI 72
		String v72BadHelpHost = v70GoodHelpHost;
		String v72GoodHelpHost = "com.ibm.di.helpHost=www.ibm.com/docs/en/SSCQGF_10.0.0";

		startIndex = strbuf.indexOf(v72BadHelpHost);
		if (startIndex != -1) {
			strbuf.replace(startIndex, startIndex + v72BadHelpHost.length(), v72GoodHelpHost);
		}

		//SDI 72 modifications - dashboard
		String v71dashboardProperty = "dashboard.on=";
		if(strbuf.indexOf(v71dashboardProperty) != -1){
			//Insert newly introduced dashboard property in SDI v72		
			String v71dashboardAuthLdapUrlGroup = "dashboard.auth.ldap.url.group=";
			String v72dashboardAuthAdminUserComments = "# Default FDS username/password";
			String v72dashboardAuthAdminUser = "{protect}-dashboard.auth.user.admin=";
			String v72dashboardAuthAdminUserValue = "admin";		
			
			if(strbuf.indexOf(v72dashboardAuthAdminUser) == -1){
				startIndex = strbuf.indexOf(eol, strbuf.indexOf(v71dashboardAuthLdapUrlGroup)) + eol.length();
				strbuf.insert(startIndex, v72dashboardAuthAdminUserComments + eol
						+ v72dashboardAuthAdminUser
						+ v72dashboardAuthAdminUserValue + eol);
			}				
			
			//Insert newly added dashboard comments in SDI v72
			String v72dashboardComments1 = "##	ldap: Authentication is done by logging into an LDAP server and optionally validating group membership";
			String v72dashboardComments2 = "##	properties: Authentication is done using dashboard.auth.user.[username]=[password] properties"; 
			
			startIndex =  strbuf.indexOf(eol, strbuf.indexOf(v72dashboardComments1)) + eol.length();
			if(strbuf.indexOf(v72dashboardComments2) == -1){
				strbuf.insert(startIndex, v72dashboardComments2);
			}
		}

		
		applyMaintenance(strbuf);
	}

	private static void applyMaintenance(StringBuilder strbuf) {

		// fill in when issues come up in maintainance for TDI 7.1

	}

	private static int findIndexOfProperty(StringBuilder strbuf, String property) {
		int startIndex;
		int eolIndex;
		String eol;

		if (strbuf.indexOf("\r\n") != -1)
			eol = "\r\n";

		else if (strbuf.indexOf("\n\r") != -1)
			eol = "\r\n";

		else
			eol = "\n";

		// Find the uncommented property field.
		startIndex = strbuf.indexOf(property);

		while (startIndex != -1) {
			// The property is not commented out.
			if (!strbuf.substring(startIndex - 1, startIndex).equalsIgnoreCase("#")) {
				return (startIndex);
			} else {
				eolIndex = strbuf.indexOf(eol, startIndex);
				startIndex = strbuf.indexOf(property, eolIndex);
			}
		}

		return (startIndex);
	}

	/**
	 * Determine the file has old derby information and needs to be migrated.
	 * 
	 * @param globalPropNewBuf
	 *            The string buffer to look in that represents the file. This is
	 *            the file content that should be search through and should be a
	 *            global/solution.properties file.
	 * 
	 * @return True if the Derby information should be migrated. Otherwise,
	 *         false is returned.
	 */
	public static boolean mustMigrateDerby(StringBuilder globalPropNewBuf) {

		/*
		 * If the com.ibm.di.store.jdbc.driver property is set to
		 * com.ibm.db2j.jdbc.DB2jDriver then its Embedded mode and the system
		 * store database is Derby and we need to migrate it.
		 */
		int driverIndex = findIndexOfProperty(globalPropNewBuf, "com.ibm.di.store.jdbc.driver=");
		int startIndex = globalPropNewBuf.indexOf("com.ibm.db2j.jdbc.DB2jDriver", driverIndex);

		/*
		 * TDI V60 is in embedded mode The value of the system store property is
		 * "com.ibm.db2j.jdbc.DB2jDriver" we are in embedded mode the database
		 * value is the value of the property
		 */
		if (startIndex != -1) {
			/*
			 * Now find the location of the Derby DB Unfortunately no telling
			 * where in the file the com.ibm.di.store.database property is, so
			 * we need to find the uncommented out one
			 */
			startIndex = findIndexOfProperty(globalPropNewBuf, "com.ibm.di.store.database=");
			startIndex = startIndex + "com.ibm.di.store.database=".length();
			int eolIndex = globalPropNewBuf.indexOf(eol, startIndex);

			derbyDatabaseLocation = globalPropNewBuf.substring(startIndex, eolIndex);

			/* Store the database location in absolute form */

			// For Windows if the second character is a not a ":"
			if (os.startsWith("Win")) {

				if (derbyDatabaseLocation.indexOf(":\\") != 1) {
					derbyDatabaseLocation = targetDir + fs + derbyDatabaseLocation;
				}
			}
			// For Unix the install directory must begin with a file
			// separator
			// to be absolute
			else {
				if (!derbyDatabaseLocation.startsWith(fs)) {
					derbyDatabaseLocation = targetDir + fs + derbyDatabaseLocation;
				}
			}

			File dataBasePath = new File(derbyDatabaseLocation);

			if (dataBasePath.isDirectory()) {
				return true;
			} else {
				return false;
			}
		}
		/*
		 * TDI v60 maybe configured for Network Mode or not using Derby at all
		 */
		else {
			startIndex = globalPropNewBuf.indexOf("com.ibm.db2.jcc.DB2Driver", driverIndex);

			if (startIndex != -1) {
				/*
				 * The value of the system store property is
				 * "com.ibm.db2j.jcc.DB2jDriver" we are in network mode the
				 * database value is harder to get Need to find the substring
				 * that is enclosed in quotes tokenize this base on ";" for each
				 * token, search for a valid directory, once you find one this
				 * is the value
				 */
				startIndex = findIndexOfProperty(globalPropNewBuf, "com.ibm.di.store.database=");
				startIndex = startIndex + "com.ibm.di.store.database=".length();
				int eolIndex = globalPropNewBuf.indexOf(eol, startIndex);
				String systemStoreLocation = globalPropNewBuf.substring(startIndex, eolIndex);

				int systemStoreIndex = systemStoreLocation.indexOf("\"");
				String systemStoreString = null;
				if (systemStoreIndex != -1) {
					systemStoreString = systemStoreLocation.substring(systemStoreIndex);
					// lets strip off the double quotes (should be at the
					// beginning and end.
					systemStoreString = systemStoreString.substring(1, systemStoreString.length() - 1);
				} else {
					// in case no quotes are used
					int doubleslashIndex = systemStoreLocation.indexOf("//");
					if (doubleslashIndex != -1) {
						systemStoreIndex = systemStoreLocation.indexOf("/", doubleslashIndex) + doubleslashIndex + 3;
						systemStoreString = systemStoreLocation.substring(systemStoreIndex);
						// lets strip off the double quotes (should be at the
						// beginning and end.
						systemStoreString = systemStoreString.substring(1, systemStoreString.length() - 1);
					} else {
						// systemStoreLocation contains the value, if no quotes
						// and url are used
						systemStoreString = systemStoreLocation;
					}
				}

				StringTokenizer systemStoreTokens = new StringTokenizer(systemStoreString, ";");
				while (systemStoreTokens.hasMoreTokens()) {
					String derbyDatabaseLocationTMP = systemStoreTokens.nextToken();
					File dataBasePath = new File(derbyDatabaseLocationTMP);

					if (dataBasePath.isDirectory()) {
						derbyDatabaseLocation = dataBasePath.toString();
						return true;
					}
				}

				// Database was not found, so return false;
				return false;
			}

			else {
				return false;
			}
		}
	}

	/**
	 * Migrates the input file to the TDI 7.2 level. It assumes the input file a
	 * global.properties (solution.properties file). The input file (and
	 * optional backup file) should have been set in the parseGeneralOptions
	 * method which must be called before calling this method.
	 * 
	 * @return 1 is returned if the file is successful. -1 is returned if any
	 *         error occurs while trying to migrate the file including if the
	 *         file cannot be backed up to the specified location or if the file
	 *         to migrate cannot be written to.
	 */
	public static int migrateFile() {
		int retCode = RC_FAIL;

		if (VERBOSE_MODE) {
			message(resHash.getString("COMMAND.START.MIGRATION"));
		}

		try {
			// First, get the needed parameters that we have already parsed
			// and
			// validated.
			targetDir = generalOptions.get(GEN_OPT_TDI_INSTALL);
			targetDirJre = generalOptions.get(GEN_OPT_JRE_INSTALL);

			// Replace the proper strings in the JRE path.
			targetDirJre = targetDirJre.replace('/', fs.toCharArray()[0]);

			// Get the input file.
			String inputFile = generalOptions.get(GEN_OPT_FILE_MIG);

			// See if the user requested a backup of the file to a
			// particular
			// location. If not, just back it up
			// to the current location with <name>.backup appended.
			String backupFile = generalOptions.get(GEN_OPT_FILE_BACKUP);
			if (backupFile == null) {
				backupFile = inputFile + ".backup";
			}

			// See if the user requested the migrated file to be sent to a
			// new
			// location.
			String newFile = generalOptions.get(GEN_OPT_NEW_FILE);
			if (newFile == null) {
				newFile = inputFile;
			}

			// First, rename/backup the file.
			File tmpBackup1 = new File(inputFile);
			File tmpBackup2 = new File(backupFile);
			// But, exit out if we have specified a file that does not exist
			// or
			// is not a file or cannot be read or written to.
			if (!tmpBackup1.exists() || !tmpBackup1.canRead()) {
				throw new Exception(resHash.getString("FILE_NOREAD_NOEXIST", inputFile));
			} else if (!tmpBackup1.isFile()) {
				throw new Exception(resHash.getString("NOT_A_FILE", inputFile));
			} else if (!tmpBackup1.canWrite()) {
				throw new Exception(resHash.getString("FILE_NOWRITE", inputFile));
			}

			// Do not continue if the backup file is the same as the input
			// file.
			if (tmpBackup1.equals(tmpBackup2)) {
				throw new Exception(resHash.getString("FILES_ARE_SAME", inputFile));
			}
			// Check to ensure that the new file is not the backup file.
			if (!inputFile.equals(newFile)) {
				File tmpBackup3 = new File(newFile);
				if (tmpBackup3.equals(tmpBackup2)) {
					throw new Exception(resHash.getString("FILES_ARE_SAME2", newFile));
				}
			}
			// Always delete the backup file before starting...
			if (tmpBackup2.exists()) {
				FileUtils.delete(tmpBackup2);
			}

			boolean rename = tmpBackup1.renameTo(new File(backupFile));
			if (!rename) {
				throw new Exception(resHash.getString("UNABLE_BACKUP_FILE", new String[] { inputFile, backupFile }));
			}

			// Read the entire backup file into a buffer so that the proper
			// changes can be made.
			FileInputStream fin = new FileInputStream(backupFile);
			InputStreamReader inStreamReader = new InputStreamReader(fin);
			StringBuilder globalPropNewBuf = new StringBuilder();

			char[] buff = new char[2048];
			int c;

			while ((c = inStreamReader.read(buff)) != -1) {
				globalPropNewBuf.append(buff, 0, c);
			}
			inStreamReader.close();
			buff = null;

			/* Determine what the end of line character is */
			if (globalPropNewBuf.indexOf("\r\n") != -1)
				eol = "\r\n";

			else if (globalPropNewBuf.indexOf("\n\r") != -1)
				eol = "\r\n";

			else
				eol = "\n";

			// Determine if we need to migrate the Derby information in
			// the
			// file.
			tdiMigrateDerby = mustMigrateDerby(globalPropNewBuf);

			if (MAINTENANCE_MODE)
				applyMaintenance(globalPropNewBuf);
			else {
				// Perform the deletions.
				log(INFO, resHash.getString("PERF.DELETIONS.INFO"));
				PerformDeletions(globalPropNewBuf);

				// Perform the additions.
				log(INFO, resHash.getString("PERF.ADDITIONS.INFO"));
				performAdditions(globalPropNewBuf);

				log(INFO, resHash.getString("PERF.DERBY.INFO"));
				// Perform the Derby changes.
				performDerbyChanges(globalPropNewBuf);

				log(INFO, resHash.getString("PERF.MODS.INFO"));
				// Peform the modifications.
				performModification(globalPropNewBuf);
			}

			// Write out the modified buffer to the original file. Its now
			// migrated.

			String globalPropNewString = globalPropNewBuf.toString();
			FileOutputStream fout = new FileOutputStream(newFile);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fout));
			out.write(globalPropNewString);
			out.close();

			// Do all of the work. The it passes.
			retCode = RC_OK;
		} catch (Exception e) {
			// Log the exception
			log(ERROR, e);
		}

		return retCode;
	}

	/**
	 * The main method of the Migrate Global Properties Command. The command
	 * exits with a 0 if it completes successfully. If the command failes it
	 * exits with a -1.
	 * 
	 * @param args
	 *            The arguments passed into the command. Valid arguments are:
	 * 
	 */
	public static void main(String args[]) {
		int retCode = RC_FAIL;

		// Initialize the logging...
		logger = Logger.getLogger("com.ibm.di.miggbl.tdimiggbl");

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
			// The command did not finish successfully. Print WARNING
			// message.
			// Don't print this message if there was an ILLEGAL COMMAND
			// USAGE
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

	/**
	 * Get the current value of property from the existing global.properties
	 * file. Returns empty string if property not present in file
	 * 
	 * @param strbuf
	 * @param prop
	 * @return the property value or empty string if property not found
	 */

	private static String getPropertyValue(StringBuilder strbuf, String prop) {

		String propertyValue = "";
		int startIndex = findIndexOfProperty(strbuf, prop);
		if (startIndex != -1) {
			startIndex = startIndex + prop.length();
			int eolIndex = strbuf.indexOf(eol, startIndex);
			propertyValue = strbuf.substring(startIndex, eolIndex);
			propertyValue = propertyValue.trim();
			if (propertyValue.charAt(0) == '=') {
				propertyValue = propertyValue.substring(1).trim();
			}
		}
		return propertyValue;
	}
}
