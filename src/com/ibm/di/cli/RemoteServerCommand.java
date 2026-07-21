/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.api.remote.TDIProperties;
import com.ibm.di.api.remote.impl.AssemblyLineListenerBase;
import com.ibm.di.api.remote.impl.DIEventListenerBase;
import com.ibm.di.api.remote.impl.LogListenerBase;
import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.security.Crypto;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.StashFile;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.util.FileUtils;
import com.ibm.di.util.PropertiesFile;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.StringTokenizer;

/**
 * Command line utility to view status/ tombstones / start / stop / reload of
 * remote TDI server components.
 *
 */
public class RemoteServerCommand extends CLIConstants {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** Hashtable to store the General Options passed. */
	private static Hashtable<String, String> m_GeneralOptions = null;

	/**
	 * Resource bundle (Locale specific) Filename: tdisrvctl.properties.
	 * Generated from TMS XML file tdisrvctl.xml
	 */
	private static ResourceHash resHash = ResourceHash.getHash("tdisrvctl");

	/** Keeps track of number of arguments processed */
	private static int m_ArgumentsProcessed = -1;

	// The general options

	/** Holds the session with the connected TDI Server */
	private static Session m_Session;

	private static String m_ServerHost = null;

	private static String m_ServerPort = null;

	private static String RUNNING = resHash.getString("RUNNING");

	private static String STOPPED = resHash.getString("STOPPED");

	private static String NOT_FOUND = resHash.getString("NOT_FOUND");

	/** Holds the user specified config list */
	private static List<String> m_configList = null;

	/** Holds the user specified assembly line list */
	private static List<String> m_assemblyLineList = null;

	/** Holds the user specified component list */
	private static List<String> m_componentList = null;

	/** Holds the user specified assembly line operation name */
	private static String aloperation = null;

	/** chk if operations needs to be executed* */
	private static boolean execAlOp = false;

	/** chk if operations needs to be executed* */
	private static TaskCallBlock userTCB = null;

	private static TaskCallBlock simulateMode = null;

	/** Indicates that the user chose to operate on ALL configs */
	private static boolean b_AllConfigs = true;

	/** Indicates that the user chose to operate on ALL ALs */
	private static boolean b_AllAssembly = true;

	/** Indicates that the user chose to operate on ALL components */
	private static boolean b_AllComponents = true;

	/** Indicates that the user chose to run the assembly line is simulate mode */
	private static boolean m_SimulateMode = false;

	// The config decryption password. (Support for old config password
	// encryption)
	private static String m_Config_Password = null;

	// source

	private static String m_EventName = null;

	private static String m_EventSource = DEFAULT_EVENT_SOURCE;

	private static String m_EventData = null;

	// DATASTRUCTURE TO HOLD TOMBSTONE SPECIFIC OPTIONS
	/** Holds the tombstone parameters passed by the user */
	private static Hashtable<String,String> m_Tomb_Params = null;

	/** Holds the tombstone attributes requested by the user */
	private static List<String> m_Tomb_Col_Options = null;

	// TDIp Properties related data structures
	private static String m_Config = null;

	private static String m_PropStoreName = null;

	private static String m_PropKey = null;

	private static String m_PropVal = null;

	private static boolean m_bProtect = false;

	private static boolean m_bAllKeys = false;

	private static int m_Prop_Operation = -1;// Will indicate the type of

	// Prop operation (List, Get,
	// Set, Del).

	private static Logger logger = null;

	private static boolean b_SHOW_HELP = false;

	private static boolean bIllegalCommandUsage = false;

	private static String configRunName = null;

	private static String propFileNames = null;

	private static boolean startWithRunname = false;

	/**
	 * Indicates that the user has specified the config instance to be started
	 * as temporary
	 */
	private static boolean startTemp = false;

	/**
	 * Whether the user wants to listen for messages logged by an AssemblyLine.
	 */
	private static boolean listen = false;

	/**
	 * Whether to execute an AssemblyLine synchronously (wait for the
	 * AssemblyLine to complete before the command exits).
	 */
	private static boolean sync = false;

	/**
	 * Indicates whether the Debug mode will be enabled or disabled for the
	 * specified components.
	 */
	private static boolean isDebugOn = false;

	/**
	 * This will hold the user choice. If user specified Configs then it will
	 * contain "C". If user specified AssemblyLines then it will also contain "A".
	 *
	 * Example. User specified "Configs and Assembly lines".. then this string
	 * will be "CA".
	 *
	 * @see #parseCAE(String[], String)
	 * @see #viewStatus()
	 */
	private static String m_userExecutionChoice = "";

	private final static String SOLUTION_PROPERTIES = "solution.properties";
	
	/**
	 * If the user specified -f to stop or shutdown, this will be set to true.
	 * @see #parseCAE(String[], String)
	 */
	private static boolean controlledStop = false;

	public static void main(String args[]) {
		int retCode = RC_FAIL;
		try {
			retCode = serverControlCommand(args);
		} catch (Exception ex) {
			log(ERROR, ex);
		}
		if (retCode != RC_OK && bIllegalCommandUsage == false) {
			// The command did not finish successfully. Print WARNING message.
			// Don't print this message if there was an ILLEGAL COMMAND USAGE
			message(resHash.getString("COMMAND_ERROR_occurred"));
		}
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.RETURN.CODE", "" + retCode));
		}

		System.exit(retCode);
	}

	/**
	 * The central method which carries out the command line parsing and
	 * decision making on which methods to execute.
	 *
	 * @param args
	 *            The command line params, passed as such by main.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>.
	 */
	private static int serverControlCommand(String[] args) {
		int retCode = RC_FAIL;

		logger = Logger.getLogger("com.ibm.di.cli.tdisrvctl");

		readDefaultProperties();
		
		try {
			m_GeneralOptions = new Hashtable<String, String>();

			// Parse the general options
			if (args.length > 0)
				parseGeneralOptions(args);
			else
				b_SHOW_HELP = true;

			if (b_SHOW_HELP) {
				message(getCommandUsage(null));
				return RC_OK;
			}

			if (m_GeneralOptions.get(GEN_OPT_VERBOSE) != null) {
				VERBOSE_MODE = true;
			}

			// Now the currentPointer is at the -op. Lets take it one step ahead
			// and decide the
			// operation.
			m_ArgumentsProcessed++;
			if (m_ArgumentsProcessed >= args.length) {
				throw new IllegalCommandUsageException(resHash.getString("OPT_VAL_UNSPECIFIED", "-op"));
			}

			if (args[m_ArgumentsProcessed].equals(RELOAD_OPER)) {
				retCode = execReload(args);
			} else if (args[m_ArgumentsProcessed].equals(SHUTDOWN_OPER)) {
				retCode = execShutdown(args);
			} else if (args[m_ArgumentsProcessed].equals(SRVINFO_OPER)) {
				retCode = execServerInfo(args);
			} else if (args[m_ArgumentsProcessed].equals(STATUS_OPER)) {
				retCode = execStatus(args);
			} else if (args[m_ArgumentsProcessed].equals(START_OPER)) {
				retCode = execStart(args);
			} else if (args[m_ArgumentsProcessed].equals(DEBUG_OPER)) {
				retCode = execDebug(args);
			} else if (args[m_ArgumentsProcessed].equals(QUERY_OPER)) {
				retCode = execQueryOps(args);
			} else if (args[m_ArgumentsProcessed].equals(STOP_OPER)) {
				retCode = execStop(args);
			} else if (args[m_ArgumentsProcessed].equals(TOMBSTONE_OPER)) {
				retCode = execTombStone(args);
			} else if (args[m_ArgumentsProcessed].equals(DELETE_TOMBSTONE_OPER)) {
				retCode = execDeleteTombStone(args);
			} else if (args[m_ArgumentsProcessed].equals(REPORT_OPER)) {
				retCode = execReport(args);
			} else if (args[m_ArgumentsProcessed].equals(EVENT_NOTIFIC_OPER)) {
				retCode = execEventNotification(args);
			} else if (args[m_ArgumentsProcessed].equals(PROP_OPER)) {
				retCode = execPropertyOperation(args);
			} else // UNKNOWN OPERATION
			{
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_OPTION_TYPE", OPERATION_SWITCH));
			}
		}// Catch illegal command usage exception
		catch (IllegalCommandUsageException ic) {
			bIllegalCommandUsage = true;
			retCode = RC_FAIL;
			log(ERROR, ic);
			if (ic.getErrorMessage() != null)
				message(ic.getErrorMessage());

			message(getCommandUsage(ic.getHelpOption()));
		}

		return retCode;
	}

	/**
	 * Read default properties from current folder.
	 * Need to read twice, since properties cannot be decrypted until after
	 * the crypto system has been set up, which requires some properties.
	 * Will only read if the stash file and solution.properties are present.
	 */
	private static void readDefaultProperties() {
		File stash = new File("idisrv.sth");
		File solProps = new File(SOLUTION_PROPERTIES);
		if (! (stash.exists() && solProps.exists()))
			return;

		readProps(false);

		// read keystore passwords
		Vector<String> stashFilePasswords = null;
		ResourceHash rsMsg = ResourceHash.getHash("miserver");
		try {
			stashFilePasswords = StashFile.readPasswords();
		} catch (Exception e) {
			logger.warn(rsMsg.getString("cannot.read.stash.file", e.toString()));
			return;
		}

		if (stashFilePasswords == null || stashFilePasswords.size() == 0) {
			logger.warn(rsMsg.getString("no.password.found.in.stash"));
			return;
		}

		String keyStorePassword = stashFilePasswords.get(0);
		String keyPassword = null;
		if (stashFilePasswords.size() > 1) {
			keyPassword = stashFilePasswords.get(1);
		} else {
			keyPassword = keyStorePassword;
		}

		try {
			com.ibm.di.api.security.CryptoUtils.init(keyStorePassword, keyPassword);
		} catch (Exception e) {
			logger.warn(rsMsg.getString("cannot.setup.server.keystore", e.toString()));
			return;
		}

		readProps(true);
	}
	
	private static void readProps(boolean decrypt) {
		try {
			Crypto crypto = null;
			if (decrypt)
				crypto = CryptoUtils.getDefaultCrypto();
			
			PropertiesFile propsFile = new PropertiesFile(crypto, SOLUTION_PROPERTIES, true);

			Iterator<String> it = propsFile.keys();
			while (it.hasNext()) {

				String key = it.next();
				String value = null;
				if (decrypt || !propsFile.isPropertyEncrypted(key))
					value = propsFile.getProperty(key);
				if (value != null && System.getProperty(key) == null)
					System.setProperty(key, value);
			}
		} catch (Throwable e) {
			logger.error("readDefaultProperties", e);
		}
	}

	/**
	 * This method deletes a specific tombstone. The tombstone entry to be
	 * deleted is identified by the GUID passed by the user. This functionality
	 * is supported for 7.0 servers and above.
	 *
	 * @since 7.0
	 * @param args
	 *            String
	 * @return int
	 */
	private static int execDeleteTombStone(String[] args) throws IllegalCommandUsageException {
		boolean status = false;
		int retcode = RC_OK;
		m_ArgumentsProcessed++;
		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) { // Coluld be help -?
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(DELETE_TOMBSTONE_OPER));
				return RC_OK;
			}

		} else if (noOfArgumentsRemaining == 2) {// -guid <Guide number
			// option>
			if (connect() == RC_FAIL) {
				return RC_FAIL;
			}

			String version = getVersion();
			if (version == null) {
				return RC_FAIL;
			} else {
				if (version.startsWith("6.0") || version.startsWith("6.1") ) {
					message(resHash.getString("DELETE_TOMBSTONE_UNSUPPORTED", m_ServerHost));
					return RC_FAIL;
				} else if (args[m_ArgumentsProcessed].equals(TS_GUID)) {
					// -guid option
					m_ArgumentsProcessed++;// get the next argument.
					String guid = args[m_ArgumentsProcessed];
					guid = guid.trim();
					status = deleteTombStones(guid); // pass the value of
					// guid.
					if (status) {
						retcode = RC_OK;
						message(resHash.getString("TOMBSTONE_DELETED", guid));
					} else {
						retcode = RC_FAIL;
					}
				}
			}
		} else {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), DELETE_TOMBSTONE_OPER);
		}
		return retcode;
	}

	/**
	 * Deletes the tombstone with the appropriate guid passed.
	 *
	 * @param guid
	 * @return true only when the tombstone object with the specified GUID is
	 *         found and deleted.
	 *
	 */
	private static boolean deleteTombStones(String guid) {
		boolean status = false;
		com.ibm.di.api.remote.TombstoneManager tombManager = null;
		try {
			tombManager = m_Session.getTombstoneManager();
			status = tombManager.deleteTombstone(guid);
		} catch (RemoteException ex) {
			log(ERROR, resHash.getString("TOMB_MANAGER_UNAVAIL"), ex);
			message(resHash.getString("TOMB_MANAGER_UNAVAIL"));
			return false;
		} catch (DIException ex) {
			log(ERROR, resHash.getString("TOMB_MANAGER_UNAVAIL"), ex);
			message(resHash.getString("TOMB_MANAGER_UNAVAIL"));
			return false;
		}
		return status;
	}

	/**
	 * Returns the version of the server.
	 *
	 * @return the version of the server
	 */
	private static String getVersion() {
		String version = null;
		try {
			version = m_Session.getServerInfo().getServerVersion();
			version = version.substring(0, version.indexOf('-'));
			version = version.trim();
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.DETERMINE.SERVER.VERSION"), ex);
		}
		return version;
	}

	/**
	 * Operation execution for viewing Tombstone entries. This will be supported
	 * only if the remote server connecting to is 6.1 and above.
	 * <p>
	 * Note: The <code>m_ArgumentsProcessed</code> should be set to the point
	 * from where to continue reading the rest of the paramters.
	 *
	 * @param args
	 *            The command line arguments passed.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int execTombStone(String[] args) throws IllegalCommandUsageException {
		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) {
			// Must be "-?" else ERROR
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(TOMBSTONE_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), TOMBSTONE_OPER);
			}
		} else if (noOfArgumentsRemaining <= 0) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), TOMBSTONE_OPER);
		} else {
			// Parse the arguments.
			parseTombstone(args);

			// Check if the TOMBSTONE feature is supported.
			// Should be 6.1 or above

			if (connect() == RC_FAIL)
				return RC_FAIL;
			try {
				if (VERBOSE_MODE) {
					message(resHash.getString("REMOTESERVERCOMMAND.WHETHER.TOMBSTONE.IS.SUPPORTED"));
				}

				String version = m_Session.getServerInfo().getServerVersion();
				version = version.substring(0, version.indexOf('-'));
				version = version.trim();

				if (VERBOSE_MODE) {
					message(resHash.getString("REMOTESERVERCOMMAND.SERVER.VERSION", version));
				}

				if (version.startsWith("6.0")) {
					message(resHash.getString("TOMBSTONE_UNSUPPORTED", m_ServerHost));
					return RC_FAIL;
				}
			} catch (Exception ex) {
				log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.DETERMINE.SERVER.VERSION"), ex);
				return RC_FAIL;
			}

			// All argments have been parsed. Now execute the view Tombstone
			// operation.
			// The parsed options will be in the m_Tomb_Params hashtable
			// and the m_Tomb_Col_Options list. If there was some
			// error while parsing then the parseTombtone() method
			// would already have thrown an appropriate exception and
			// hence we will not reach here.
			retCode = viewTombStones();
		}
		return retCode;
	}

	/**
	 * This method will print the tombstones for the user specified
	 * config/al/eh. This method expects the session to be ALREADY established,
	 * and also the tombstone options to be parsed.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int viewTombStones() {
		int retCode = RC_OK;
		com.ibm.di.api.remote.TombstoneManager tombManager = null;
		try {
			tombManager = m_Session.getTombstoneManager();
		} catch (Exception ex) {
			log(ERROR, resHash.getString("TOMB_MANAGER_UNAVAIL"), ex);
			message(resHash.getString("TOMB_MANAGER_UNAVAIL"));
			return RC_FAIL;
		}

		String days = m_Tomb_Params.get(AGE_OPTION);
		if (days == null) {
			days = "1"; // dafault value of 1 day if not specified.
		}

		int noOfDays = 1;
		try {
			noOfDays = new Integer(days).intValue();
		} catch (NumberFormatException nfe) {
			log(ERROR, "", nfe);
			// ignore. (will never happen - already checked while parsing)
		}

		Date endDate = new Date(); // today
		Calendar rightNow = Calendar.getInstance();
		rightNow.add(Calendar.DATE, (noOfDays * -1)); // subtract the
		// specified number of
		// days
		Date startDate = rightNow.getTime();
		com.ibm.di.api.Tombstone[] tombstones = null;

		// Config is mandatory

		String origConfig = m_Tomb_Params.get(CONFIG_OPTION);
		String configName = prefixIfRelativePath(origConfig);

		String configID = convertURLtoID(configName);

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.START.DATE", startDate.toString()));
			message(resHash.getString("REMOTESERVERCOMMAND.END.DATE", endDate.toString()));
			message(resHash.getString("REMOTESERVERCOMMAND.CONFIG.ID", configID));
		}

		try {
			// If only CONFIG is mentioned, then obtain tombstones for the
			// specified config.
			if (m_Tomb_Params.containsKey(CONFIG_OPTION) && !m_Tomb_Params.containsKey(ASSEMBLY_LINE_OPTION)) {
				tombstones = tombManager.getConfigInstanceTombstones(configID, startDate, endDate);
				// handle FN-13 Soln name requirement
				if (null == tombstones || tombstones.length == 0)
					tombstones = tombManager.getConfigInstanceTombstones(origConfig, startDate, endDate);
			}
			// For assembly line
			else if (m_Tomb_Params.containsKey(ASSEMBLY_LINE_OPTION)) {
				String alName = m_Tomb_Params.get(ASSEMBLY_LINE_OPTION);
				alName = ASSEMBLY_LINE_FOLDER_PREFIX + alName; // the API wants
				// "AssemblyLines/"
				// prefixed in
				// name
				tombstones = tombManager.getAssemblyLineTombstones(alName, configID, startDate, endDate);
				// handle FN-13 Soln name requirement
				if (null == tombstones || tombstones.length == 0)
					tombstones = tombManager.getAssemblyLineTombstones(alName, origConfig, startDate, endDate);
			}

			boolean b_AllTombColumns = false;
			if (m_Tomb_Params.containsKey(ALL) || m_Tomb_Col_Options.size() <= 0) {
				b_AllTombColumns = true;
			}

			// Now display tombstones

			String configuration = null; // Config name
			String compType = null; // Component type
			String compName = null; // Component name
			String eventType = null; // Event type
			String exitCode = null; // Exit Code
			String errDesc = null; // Error Desc
			String guid = null; // Guid;
			String startTime = null; // Start Time
			String createTime = null; // Create Time
			String stats = null; // Stats
			String userMsg = null; // User message

			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.NUMBER.OF.TOMBSTONES.FOUND", "" + tombstones.length));
			}

			StringBuffer displayBuffer = null;
			if (tombstones != null) {
				for (int i = 0; i < tombstones.length; i++) {

					if (b_AllTombColumns) {
						// Show all
						configuration = "" + tombstones[i].getConfiguration();
						compType = "" + tombstones[i].getComponentTypeID();
						compName = tombstones[i].getComponentName();
						guid = "" + tombstones[i].getGUID();
						eventType = "" + tombstones[i].getEventTypeID();
						exitCode = "" + tombstones[i].getExitCode();
						startTime = tombstones[i].getStartTime().toString();
						createTime = tombstones[i].getTombstoneCreateTime().toString();
						errDesc = "" + tombstones[i].getErrorDescription();
						userMsg = tombstones[i].getUserMessage();
						if (tombstones[i].getComponentTypeID() == 1) // ASSEMBLY
							// LINES
							// ONLY
							stats = tombstones[i].getStatistics().toString();
						else
							stats = "---";

						message("");
						message(compType + DELIMITER + compName + DELIMITER + configuration + DELIMITER + guid + DELIMITER + eventType
								+ DELIMITER + exitCode + DELIMITER + startTime + DELIMITER + createTime + DELIMITER + errDesc
								+ DELIMITER + userMsg + DELIMITER + stats);
					} else { // Show only those which the user asked for

						displayBuffer = new StringBuffer();
						for (String attribute: m_Tomb_Col_Options) {
							if (attribute.equals(TS_COMP_TYPE))
								displayBuffer.append("" + tombstones[i].getComponentTypeID());
							else if (attribute.equals(TS_COMP_NAME))
								displayBuffer.append("" + tombstones[i].getComponentName());
							else if (attribute.equals(TS_CONFIG))
								displayBuffer.append("" + tombstones[i].getConfiguration());
							else if (attribute.equals(TS_GUID))
								displayBuffer.append("" + tombstones[i].getGUID());
							else if (attribute.equals(TS_EVENT_TYPE))
								displayBuffer.append("" + tombstones[i].getEventTypeID());
							else if (attribute.equals(TS_EXIT_CODE))
								displayBuffer.append("" + tombstones[i].getExitCode());
							else if (attribute.equals(TS_START_TIME))
								displayBuffer.append("" + tombstones[i].getStartTime());
							else if (attribute.equals(TS_CREATE_TIME))
								displayBuffer.append("" + tombstones[i].getTombstoneCreateTime());
							else if (attribute.equals(TS_ERR_DESC))
								displayBuffer.append("" + tombstones[i].getErrorDescription());
							else if (attribute.equals(TS_STATS)) {
								if (tombstones[i].getComponentTypeID() == 1) // ASSEMBLY
									// LINES
									// ONLY
									displayBuffer.append("" + tombstones[i].getStatistics().toString());
								else
									displayBuffer.append("---");
							} else if (attribute.equals(TS_USERMESSAGE))
								displayBuffer.append("" + tombstones[i].getUserMessage());

							displayBuffer.append(DELIMITER);
						}
						message(displayBuffer.toString());
					}
				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.OBTAIN.TOMBSTONES"), ex);
			return RC_FAIL;
		}
		return retCode;
	}

	/**
	 *
	 * Converts URL to ID.
	 *
	 * @param configName
	 *
	 * @return the config id.
	 *
	 * @see com.ibm.di.api.syslog.LogUtils#getCleanConfigId(String)
	 */
	private static String convertURLtoID(String configName) {
		// This call may be changed to some different server API later.
		String configId = com.ibm.di.api.syslog.LogUtils.getCleanConfigId(configName);

		return configId;
	}

	/**
	 * Parses the command line paramters for Tombstone option. The parameters
	 * (config,al,eh,age) are all put in the hashtable
	 * <code>m_Tomb_Params</code> and the Tombstone attributes which the user
	 * wishes to see are put in <code>m_Tomb_Col_Options</code>.
	 * <p>
	 * The method expects the
	 * <code>m_ArgumentsProcessed<code> variable to be set to the point
	 * from where it will parse the remaining arguments.
	 *
	 * @param args
	 *            Command Line paramters
	 *
	 * @throws IllegalCommandUsageException
	 *             When the command options are incorrectly used.
	 */
	private static void parseTombstone(String[] args) throws IllegalCommandUsageException {

		m_Tomb_Params = new Hashtable<String,String>();
		m_Tomb_Col_Options = new ArrayList<String>();
		String temp = null;

		for (int i = m_ArgumentsProcessed; i < args.length; i++) {
			// CONFIG OPTION
			if (args[i].equals(CONFIG_OPTION)) {
				i++;
				if (i != args.length) {
					temp = args[i];
					if (temp.indexOf(",") > 0) {
						throw new IllegalCommandUsageException(resHash.getString("ONE_CONFIG_ALLOWED"), TOMBSTONE_OPER);
					}

					m_Tomb_Params.put(CONFIG_OPTION, temp);
				} else // No VALUE for option !
				{
					throw new IllegalCommandUsageException(resHash.getString("OPT_VAL_UNSPECIFIED", args[i - 1]), TOMBSTONE_OPER);
				}
			}
			// ASSEMBLY LINE OPTION
			else if (args[i].equals(ASSEMBLY_LINE_OPTION)) {
				i++;
				if (i != args.length) {
					temp = args[i];
					if (temp.indexOf(",") > 0) {
						throw new IllegalCommandUsageException(resHash.getString("ONE_AL_ALLOWED"), TOMBSTONE_OPER);
					}

					m_Tomb_Params.put(ASSEMBLY_LINE_OPTION, temp);
				} else // No VALUE for option !
				{
					throw new IllegalCommandUsageException(resHash.getString("OPT_VAL_UNSPECIFIED", args[i - 1]), TOMBSTONE_OPER);
				}

			}

			// AGE OPTION
			else if (args[i].equals(AGE_OPTION)) {
				i++;
				if (i != args.length) {
					temp = args[i];
					int age = -1;
					try {
						age = Integer.valueOf(temp);
					} catch (NumberFormatException nfe) {
						log(ERROR, resHash.getString("REMOTESERVERCOMMAND.INVALID.AGE", temp));
					}

					if (age <= 0) {
						throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_AGE_VAL"), TOMBSTONE_OPER);
					}

					m_Tomb_Params.put(AGE_OPTION, "" + age);
				} else // No VALUE for option !
				{
					throw new IllegalCommandUsageException(resHash.getString("OPT_VAL_UNSPECIFIED", args[i - 1]), TOMBSTONE_OPER);
				}
			}
			// TOMBSTONE SPECIFIC OPTIONS
			else if (args[i].equals(TS_COMP_NAME) || args[i].equals(TS_COMP_TYPE) || args[i].equals(TS_CONFIG)
					|| args[i].equals(TS_CREATE_TIME) || args[i].equals(TS_ERR_DESC) || args[i].equals(TS_EVENT_TYPE)
					|| args[i].equals(TS_EXIT_CODE) || args[i].equals(TS_USERMESSAGE) || args[i].equals(TS_START_TIME)
					|| args[i].equals(TS_STATS) || args[i].equals(TS_GUID))

			{
				// Since the order these options were specified must be
				// "remembered", therefore we
				// will save these options in an ArrayList instead of a hash
				// table.
				// Plus these options don't have any "value" associated with
				// them...
				// so all the more better.
				m_Tomb_Col_Options.add(args[i]);
			}
			// SHOW ALL [This has to be the last option, and no more parsing
			// will be done after this]
			else if (args[i].equals(ALL)) // This means user view ALL
			// tombstone columns.
			{
				// Check if user specified some "specific" columns, and also
				// said SHOW ALL (which is weird!)
				if (m_Tomb_Col_Options.size() > 0)
					throw new IllegalCommandUsageException(resHash.getString("CONFLICTING_TOMB_OPT"), TOMBSTONE_OPER);

				m_Tomb_Params.put(ALL, "true");
				i++;

				if (i != args.length) // Should be the last option
					throw new IllegalCommandUsageException(resHash.getString("ALL_OPT_LAST"), TOMBSTONE_OPER);

				break; // end of command processing.

			} else // DID NOT MATCH ANY !?? Some weird option specified : )
			{
				throw new IllegalCommandUsageException(resHash.getString("UNKNOWN_OPT", args[i]), TOMBSTONE_OPER);
			}
		}

		// Check if config has been specified (it is mandatory)

		if (!m_Tomb_Params.containsKey(CONFIG_OPTION)) {
			throw new IllegalCommandUsageException(resHash.getString("OPT_MANDATORY", CONFIG_OPTION), TOMBSTONE_OPER);
		}

	}

	/**
	 * Operation execution for Shutdown of server.
	 * <p>
	 * Note: The <code>m_ArgumentsProcessed</code> should be set to the point
	 * from where to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The command line arguments passed.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int execShutdown(String[] args) throws IllegalCommandUsageException {

		int returnCodeShutdown = 0;
		controlledStop = false;

		// Get the operation specific options
		for (m_ArgumentsProcessed++; m_ArgumentsProcessed < args.length; m_ArgumentsProcessed++) {
			// Help option
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				// Contains "?", therefore print usage of this option
				message(getCommandUsage(SHUTDOWN_OPER));
				return RC_OK;
			}

			// Return code option
			if (args[m_ArgumentsProcessed].equals(RETCODE_OPTION) &&
					m_ArgumentsProcessed < (args.length - 1)) {
				m_ArgumentsProcessed++;
				try {
					returnCodeShutdown = Integer.valueOf(args[m_ArgumentsProcessed]);
				} catch (Exception ex) {
					log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.PROCESSING.SHUTDOWN"), ex);
					throw new IllegalCommandUsageException(null, SHUTDOWN_OPER);
				}
				continue;
			}

			// Controlled stop option
			if (args[m_ArgumentsProcessed].equals(FORCE_CONTROLLED_OPTION)) {
				controlledStop = true;
				continue;
			}
			throw new IllegalCommandUsageException(null, SHUTDOWN_OPER);
		}

		return shutdownServer(returnCodeShutdown, controlledStop);
	}

	/**
	 * Shutdown the server.
	 * @param returnCode The return code the server should use when shutting down
	 * @param controlledStop If true, try to let AssemblyLines do a controlled stop.
	 * @return RC_OK if success, otherwise RC_FAIL.
	 */
	private static int shutdownServer(int returnCode, boolean controlledStop) {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.SHUTDOWNSERVER.2", "" + returnCode));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;

		try {
			if (controlledStop)
				m_Session.shutDownServer(returnCode, false);
			else
				m_Session.shutDownServer(returnCode);
		} catch (Exception e) {
			log(ERROR, e);
			return RC_FAIL;
		}

		message(resHash.getString("SRV_SHUTDOWN", m_ServerHost + ":" + m_ServerPort));
		return RC_OK;
	}

	/**
	 * Reload CONFIGS option.
	 *
	 * @param args
	 *            The command line parameters.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int execReload(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;

		m_ArgumentsProcessed++;

		// Get the operation specific options
		int remainingArgs = (args.length - m_ArgumentsProcessed);
		switch (remainingArgs) {
		case 1: // If only one argument remaining then it MUST be "-?"
			// else
			// ERROR.
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(RELOAD_OPER));
				retCode = RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), RELOAD_OPER);
			}
			break;

		case 2: // Should be -c and list_of_configs
			if (args[m_ArgumentsProcessed].equals(CONFIG_OPTION)) {
				List<String> listOfConfigs = tokenizeToList(args[m_ArgumentsProcessed + 1], ",");
				retCode = reloadConfigs(listOfConfigs);
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), RELOAD_OPER);
			}
			break;

		default: // Incorrect command usage
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), RELOAD_OPER);

		}

		return retCode;
	}

	/**
	 * Send Custom event notification option.
	 *
	 * @param args
	 *            The command line arguments
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 * @throws IllegalCommandUsageException
	 */
	private static int execEventNotification(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		// Check if the EVENT NOTIFICATION feature is supported.
		// Should be 6.1 or above

		if (connect() == RC_FAIL)
			return RC_FAIL;
		try {
			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.WHETHER.REMOTE.CLIENT.IS.SUPPORTED"));
			}

			String version = m_Session.getServerInfo().getServerVersion();
			version = version.substring(0, version.indexOf('-'));
			version = version.trim();

			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.SERVER.VERSION.2", version));
			}

			if (version.startsWith("6.0")) {
				message(resHash.getString("REMOTE_CLIENT_NOTIFICATION_UNSUPPORTED", m_ServerHost));
				return RC_FAIL;
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.DETERMINE.SERVER.VERSION.2"), ex);
			return RC_FAIL;
		}

		// Get the operation specific options
		int remainingArgs = (args.length - m_ArgumentsProcessed);
		switch (remainingArgs) {
		case 0:
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);

		case 1: // If only one argument remaining then it must be "-?"
			// else
			// ERROR
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(EVENT_NOTIFIC_OPER));
				retCode = RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);
			}
			break;

		default:
			parseEventOptions(args); // Extracts the "event_name",
			// "source",
			// and "data"
			retCode = sendEventNotification();
			// fire the event
		}
		return retCode;
	}

	/**
	 * Manage config property via TDIp.
	 *
	 * @param args
	 *            The command line arguments
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 * @throws IllegalCommandUsageException
	 *
	 */
	private static int execPropertyOperation(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		// Check if the TDI-p Property feature is supported.
		// Should be 6.1 or above

		if (connect() == RC_FAIL)
			return RC_FAIL;
		try {
			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.WHETHER.TDIP.PROPERTIES.IS.SUPPORTED"));
			}
			String version = m_Session.getServerInfo().getServerVersion();
			version = version.substring(0, version.indexOf('-'));
			version = version.trim();

			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.SERVER.VERSION.3", version));
			}

			if (version.startsWith("6.0")) {
				message(resHash.getString("TDIP_PROPERTIES_UNSUPPORTED", m_ServerHost));
				return RC_FAIL;
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("TDIP_PROPERTIES_UNSUPPORTED"), ex);
			return RC_FAIL;
		}

		// Get the operation specific options
		int remainingArgs = (args.length - m_ArgumentsProcessed);
		switch (remainingArgs) {
		case 0:
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);

		case 1: // If only one argument remaining then it must be "-? "
			// else
			// ERROR
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(PROP_OPER));
				retCode = RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
			}
			break;

		default:
			parseConfigPropertiesOptions(args);
			if (m_Prop_Operation == LIST_STORE) {
				retCode = displayPropertyStoreList();
			} else if (m_Prop_Operation == GET_VALUE) {
				retCode = displayPropertyValues();
			} else if (m_Prop_Operation == SET_VALUE) {
				retCode = setTDIPropertyValue();
			} else if (m_Prop_Operation == DEL_VALUE) {
				retCode = deleteTDIProperty();
			}

			// fire the event
		}
		return retCode;
	}

	/**
	 * Displays a list of property stores for the config <code>m_Config</code>.
	 *
	 * @see #parseConfigPropertiesOptions(String[])
	 */

	private static int displayPropertyStoreList() {
		int retCode = RC_OK;
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.DISPLAYPROPERTYSTORELIST"));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;
		String origConfig = m_Config;
		m_Config = prefixIfRelativePath(m_Config);
		try {
			ConfigInstance configInstance = m_Session.getConfigInstance(convertURLtoID(m_Config));
			// MetamergeConfig mc = configInstance.getConfiguration();
			// TDIProperties tdip = mc.getTDIProperties();
			if (null == configInstance)// Check if the user has passed a soln
				// name.
				configInstance = m_Session.getConfigInstance(origConfig);
			if (null != configInstance) {
				TDIProperties tdip = configInstance.getTDIProperties();
				List<String> listOfStores = tdip.getPropertyStoreNames();
				for (String store: listOfStores) {
					message(store);
				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.OBTAIN.LIST.OF.PROPERTY.STORES", m_Config), ex);
			return RC_FAIL;
		}

		return retCode;
	}

	/**
	 * Checks to see if the store name passed is valid. If not, then prints an
	 * appropriate message and returns <code>false</code>.
	 *
	 * @param tdip
	 *            The TDIProperties object which will be queried for existing
	 *            store names.
	 * @param storeName
	 *            The store Name to check.
	 */
	private static boolean doesPropertyStoreExist(TDIProperties tdip, String storeName) {
		try {
			List<String> listOfStores = tdip.getPropertyStoreNames();
			for (String store: listOfStores) {
				if (store.equals(storeName)) {
					return true;
				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.OBTAIN.LIST.OF.PROPERTY.STORES.2", m_Config), ex);
			return false;
		}
		message(resHash.getString("PROP_STORE_NOT_FOUND", storeName));
		return false; // not found
	}

	/**
	 * Displays the list of configured property store names.
	 *
	 * @return <code>RC_OK</code> if all is ok, otherwise <code>RC_FAIL</code>
	 */
	private static int displayPropertyValues() {
		int retCode = RC_OK;
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.DISPLAYPROPERTYVALUES"));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;

		try {
			String origConfig = m_Config;
			m_Config = prefixIfRelativePath(m_Config);
			ConfigInstance configInstance = m_Session.getConfigInstance(convertURLtoID(m_Config));
			if (null == configInstance)
				configInstance = m_Session.getConfigInstance(origConfig);
			// Check if the user has passed a soln name.
			if (null != configInstance) {
				TDIProperties tdip = configInstance.getTDIProperties();
				if (m_bAllKeys) { // If user chose to get all keys

					if (m_PropStoreName != null) { 	// User specified a prop store
						if (doesPropertyStoreExist(tdip, m_PropStoreName) == false)
							return RC_FAIL;
						displayProperties(tdip, m_PropStoreName);
					} else { // User did not specify a property store.
						// Get list of all property stores, and show their keys.
						List<String> storeList = tdip.getPropertyStoreNames();
						for (String store:storeList) {
							displayProperties(tdip, store);
						}
					}
				} else { // User passed a specific property name
					Object val = null;
					if (m_PropStoreName != null) { // User specified a prop store
						if (doesPropertyStoreExist(tdip, m_PropStoreName) == false)
							return RC_FAIL;

						val = tdip.getProperty(m_PropStoreName, m_PropKey);
						if (tdip.isPropertyEncrypted(m_PropStoreName, m_PropKey)) {
							val = PROTECTED + val;
						}
					} else {
						val = tdip.getProperty(m_PropKey);
						if (tdip.isPropertyEncrypted(null, m_PropKey)) {
							val = PROTECTED + val;
						}

					}

					if (val != null) {
						message(val.toString());
					} else {
						message(resHash.getString("PROP_KEY_NOT_FOUND", m_PropKey));
					}

				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.TRYING.TO.OBTAIN.PROPERTY.FOR.CONFIG", m_Config), ex);
			return RC_FAIL;
		}
		return retCode;
	}

	private static void displayProperties(TDIProperties tdip, String store) throws Exception {
		message("");
		message("--- " + store + " ---");
		message("");
		String[] list = tdip.getPropertyStoreKeys(store);
		if (list.length <= 0) {
			message(resHash.getString("NONE"));
			return;
		}
		for (String key:list) {
			Object val = tdip.getProperty(store, key);
			if (tdip.isPropertyEncrypted(store, key)) {
				message(key + "=" + PROTECTED + val);
			} else {
				message(key + "=" + val);
			}
		}
	}

	private static int setTDIPropertyValue() {
		int retCode = RC_OK;
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.SETTDIPROPERTYVALUE"));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;

		try {
			String origConfig = m_Config;
			m_Config = prefixIfRelativePath(m_Config);
			ConfigInstance configInstance = m_Session.getConfigInstance(convertURLtoID(m_Config));
			if (null == configInstance)
				configInstance = m_Session.getConfigInstance(origConfig);
			// Check if the user has passed a soln name.
			if (null != configInstance) {
				TDIProperties tdip = configInstance.getTDIProperties();
				if (m_PropStoreName != null) { // Prop store specified
					if (doesPropertyStoreExist(tdip, m_PropStoreName) == false)
						return RC_FAIL;
					if (m_bProtect == true) {
						tdip.setProperty(m_PropStoreName, m_PropKey, m_PropVal, m_bProtect);
					} else {
						tdip.setProperty(m_PropStoreName, m_PropKey, m_PropVal);
					}
				} else { // Store NOT specified
					tdip.setProperty(m_PropKey, m_PropVal, m_bProtect);
				}
				tdip.commit();
				message(resHash.getString("PROPERTY_SET_SUCCESS", m_PropKey));
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.TRYING.TO.SET.PROPERTY.FOR.CONFIG", m_Config), ex);
			return RC_FAIL;
		}

		return retCode;
	}

	private static int deleteTDIProperty() {
		int retCode = RC_OK;
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.DELETETDIPROPERTY"));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;

		try {
			String origConfig = m_Config;
			m_Config = prefixIfRelativePath(m_Config);
			ConfigInstance configInstance = m_Session.getConfigInstance(convertURLtoID(m_Config));
			if (null == configInstance) {
				// Check if the user has passed a soln name.
				configInstance = m_Session.getConfigInstance(origConfig);
			}

			if (null != configInstance) {
				TDIProperties tdip = configInstance.getTDIProperties();
				if (m_PropStoreName != null) { // Prop store specified
					if (doesPropertyStoreExist(tdip, m_PropStoreName) == false)
						return RC_FAIL;

					tdip.removeProperty(m_PropStoreName, m_PropKey);
					tdip.commit();
					message(resHash.getString("PROPERTY_DELETE_SUCCESS", m_PropKey));
				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.TRYING.TO.DELETE.PROPERTY", new String[] { m_PropKey,
					m_Config }), ex);
			return RC_FAIL;
		}

		return retCode;
	}

	/**
	 * REPORT option.
	 *
	 * @param args
	 *            The command line paramters.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int execReport(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;

		m_ArgumentsProcessed++;

		// Get the operation specific options
		int remainingArgs = (args.length - m_ArgumentsProcessed);
		switch (remainingArgs) {
		case 1: // If only one argument remaining then it MUST be "-?"
			// or "-l"
			// else ERROR.
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(REPORT_OPER));
				retCode = RC_OK;
			} else if (args[m_ArgumentsProcessed].equals(LIST_OPTION)) {
				retCode = displayConfigList();
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), REPORT_OPER);
			}
			break;

		case 2: // Should be -c and config_name
			if (args[m_ArgumentsProcessed].equals(CONFIG_OPTION)) {
				String configName = args[m_ArgumentsProcessed + 1];
				retCode = showReport(configName);
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), REPORT_OPER);
			}
			break;

		default: // Incorrect command usage
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), REPORT_OPER);

		}

		return retCode;
	}

	private static int queryOps(String cfgName, String alName) {

		AssemblyLineConfig alc = null;

		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}
		String origConfig = cfgName;
		cfgName = convertURLtoID(prefixIfRelativePath(cfgName));
		message("");
		message(resHash.getString("CONNECTED_SRV", m_ServerHost + ":" + m_ServerPort));

		if (m_ServerHost == null)
			m_ServerHost = "localhost";

		if (m_ServerPort == null)
			m_ServerPort = "1099";

		try {
			ConfigInstance ci = m_Session.getConfigInstance(cfgName);

			if (ci == null) {
				// Check if the user has passed a soln name.
				ci = m_Session.getConfigInstance(origConfig);

				if (null == ci) {
					message(resHash.getString("CONFIG_NOT_LOADED", origConfig));
					log(ERROR, resHash.getString("CONFIG_NOT_LOADED", origConfig));
					return RC_FAIL;
				}
			}
			try {
				alc = (AssemblyLineConfig) ci.getConfiguration().lookup(ASSEMBLY_LINE_FOLDER_PREFIX + alName);
			} catch (Exception exx) {
				log(ERROR, "", exx);
			}

			if (alc == null) {
				message(resHash.getString("AL_NOT_FOUND_FOR_OPERATION", alName));
				return RC_FAIL;
			}

			ContainerConfig cc = alc.getOperations();
			if (cc.size() <= 0) {
				message(resHash.getString("NO_OPERATIONS_AVAILABLE_INFORMATION"));
				return RC_OK;
			}

			String displayString = "";
			String opName = "";
			for (int i = 0; i < cc.size(); i++) {
				opName = cc.getConfig(i).getShortName(); // operation name
				// get exposed attributes for input
				displayString = opName + ": " + getOperationAttributes(alc, opName);
				message(displayString);
			}

		} catch (Exception e) {
			log(ERROR, "", e);
			return RC_FAIL;
		}

		return RC_OK;
	}

	/**
	 * Operation "queryop" for AssemblyLines. Note: The
	 * <code>m_ArgumentsProcessed</code> should be set to the point from where
	 * to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The Command Line parameters.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>
	 */
	private static int execQueryOps(String[] args) throws IllegalCommandUsageException { // created
																							// by
																							// skarthik.
		String cfg = "";
		String al = "";
		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) { // Must be "-?" else ERROR

			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(QUERY_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), STOP_OPER);
			}
		} else if (noOfArgumentsRemaining != 4) {
			// display help. This will indicate how the options need to be
			// specified.
			message(getCommandUsage(QUERY_OPER));
			return RC_OK;
		} else {
			boolean foundC = false;
			boolean foundR = false;
			// parse the arguments.
			for (int i = 0; i < 2; i++) {
				if (args[m_ArgumentsProcessed].equals(CONFIG_OPTION) && !foundC) {
					cfg = args[++m_ArgumentsProcessed];
					foundC = true;
				} else if (args[m_ArgumentsProcessed].equals(ASSEMBLY_LINE_OPTION) && !foundR) {
					foundR = true;
					al = args[++m_ArgumentsProcessed];
				} else {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), STOP_OPER);
				}
				m_ArgumentsProcessed++;
			}
			retCode = queryOps(cfg, al);
		}

		return retCode;
	}

	/**
	 * Operation "stop" for Configs / Assembly Lines. Note: The
	 * <code>m_ArgumentsProcessed</code> should be set to the point from where
	 * to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The Command Line parameters.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>
	 */
	private static int execStop(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) // Must be "-?" else ERROR
		{
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(STOP_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), STOP_OPER);
			}
		} else if (noOfArgumentsRemaining <= 0) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), STOP_OPER);
		} else {
			// Parse the arguments.
			parseCAE(args, STOP_OPER);

			// All arguments have been parsed. Now execute the STOP operation.
			retCode = stopComponents();
		}

		return retCode;
	}

	/**
	 * Operation "start" for Configs / Assembly Lines. Note: The
	 * <code>m_ArgumentsProcessed</code> should be set to the point from where
	 * to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The Command Line parameters.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>
	 */
	private static int execStart(String[] args) throws IllegalCommandUsageException {
		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) { // Must be "-?" else ERROR
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(START_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), START_OPER);
			}
		} else if (noOfArgumentsRemaining <= 0) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), START_OPER);
		} else {
			// Parse the arguments.
			parseCAE(args, START_OPER);

			// All argments have been parsed. Now execute the START operation.
			retCode = startComponents();
		}

		return retCode;
	}

	/**
	 * Operation "status" for Configs / Assembly Lines. Note: The
	 * <code>m_ArgumentsProcessed</code> should be set to the point from where
	 * to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The Command Line parameters.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>
	 */
	private static int execStatus(String[] args) throws IllegalCommandUsageException {
		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;
		if (noOfArgumentsRemaining == 1) // Must be "-?" else ERROR
		{
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(STATUS_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), STATUS_OPER);
			}
		} else if (noOfArgumentsRemaining <= 0) {
			// Assume that since no other options have been passed - take this to be:
			// tdisrvctl -op status -c all -r all
			m_userExecutionChoice = "CAE";
			b_AllConfigs = true;
			b_AllAssembly = true;
			retCode = viewStatus();

		} else {
			// Parse the arguments.
			parseCAE(args, STATUS_OPER);

			// All arguments have been parsed. Now execute the STATUS operation.
			retCode = viewStatus();
		}

		return retCode;
	}

	/**
	 *
	 * This method will parse the remaining arguments for obtaining the user
	 * specified CONFIGS and ASSEMBLIES. This method is used for parsing the
	 * options for the "status" "start", "stop" and "debug" operations.
	 * <p>
	 * The method expects the
	 * <code>m_ArgumentsProcessed<code> variable to be set to the point
	 * from where it will parse the remaining arguments.
	 * <p>
	 * This method will set the user passed configs into <code>m_configList</code>.
	 * <p>
	 * This method will set the user passed assembly lines into
	 * <code>m_assemblyLineList</code>.
	 * <p>
	 * If user chose to specify ALL for any of the above options then the
	 * appropriate boolean value will be filled into <code>b_AllConfigs</code>
	 * and <code>b_AllAssembly</code>.
	 * <p>
	 * Also, based on user choice, it will set the
	 * <code>m_userExecutionChoice</code> to hold "C" or "A".
	 *
	 * @param args
	 *            The command line arguments.
	 *
	 * @param operation
	 *            The type of operation for which the parsing is to be done.
	 *            Helps in doing operation specific steps where necessary. The
	 *            type of operations are: <code>START_OPER</code>,
	 *            <code>STATUS_OPER</code> and <code></code>.
	 *
	 * @throws IllegalCommandUsageException
	 *
	 * @see RemoteServerCommand#viewStatus()
	 * @see #START_OPER
	 * @see #STATUS_OPER
	 * @see #STOP_OPER
	 * @see #DEBUG_OPER
	 */
	private static void parseCAE(String args[], String operation) throws IllegalCommandUsageException {

		userTCB = new TaskCallBlock();
		for (int current = m_ArgumentsProcessed; current < args.length; current++) {

			if (args[current].equals(CONFIG_OPTION)) // If config option
			{
				current++;
				m_userExecutionChoice += "C"; // User specified "configs"
				if (args[current].equals(ASSEMBLY_LINE_OPTION)) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}
				if (args[current].equals(ALL)) {
					// Ignore the rest of the arguments.
					b_AllConfigs = true;
				} else // Parse the arguments
				{
					b_AllConfigs = false;
					m_configList = tokenizeToList(args[current], ",");
				}

				continue; // continue with next argument.
			}

			if (args[current].equals(ASSEMBLY_LINE_OPTION)) {
				// User specified "assembly lines"
				m_userExecutionChoice += "A";
				current++;

				if (args[current].equals(CONFIG_OPTION)) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}

				if (args[current].equals(ALL)) {
					b_AllAssembly = true;
				} else {
					b_AllAssembly = false;
					m_assemblyLineList = tokenizeToList(args[current], ",");
				}

				// Check if AL_OP related option specified (start only)
				try {
					if (operation.equals(START_OPER) && (current != args.length - 1) && (args[current + 1].equals(AL_OP))) {
						// Not more than config or one AL is allowed
						if ((b_AllConfigs == true) || (m_configList.size() > 1) || (b_AllAssembly == true)
								|| (m_assemblyLineList.size() > 1)) {
							throw new IllegalCommandUsageException(resHash.getString("CONFIG_ALL_NOT_ALLOWED", ALL), operation);
						}
						// we have not parsed the alop param
						current = current + 2;
						if (current >= args.length) {
							// No operation found
							throw new IllegalCommandUsageException(resHash.getString("CONFIG_ALL_NOT_ALLOWED", ALL), operation);
						}

						aloperation = args[current];
						execAlOp = true;
						userTCB.setALOperation(aloperation);
						// Get operation parameters/attributes
						String attrList = null;
						try {
							++current;
							if (args[current].equals("-f")) { // File specified
								try {
									current++;
									String filename = args[current];
									if (aloperation.equalsIgnoreCase("$initialize")) {
										userTCB.setOperationInitParams(getAttributeEntryFromFile(filename));
									} else {
										userTCB.setInitialWorkEntry(getAttributeEntryFromFile(filename));
									}
								} catch (Exception ie) {
									throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
								}
							} else { // Not specified file
								attrList = args[current];
								if (null != attrList) {
									if (!attrList.startsWith("{")) {
										throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"),
												operation);
									}
									if (aloperation.equalsIgnoreCase("$initialize")) {
										userTCB.setOperationInitParams(getAttributeEntry(attrList));
									} else {
										userTCB.setInitialWorkEntry(getAttributeEntry(attrList));
									}
								}
							}
						} catch (ArrayIndexOutOfBoundsException ignore) {
							throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
						}
					}
				} catch (ArrayIndexOutOfBoundsException aie) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}

				continue; // Continue with next argument
			}
			if (args[current].equals(SIMULATE_MODE)) {
				m_SimulateMode = true;
				current++;
				try {
					simulateMode = new TaskCallBlock();
					simulateMode.setProperty(com.ibm.di.server.AssemblyLine.TCB_SIMULATE_MODE, Boolean.TRUE);
				} catch (Exception ex) {
					log(ERROR, ex);
				}
				continue;
			}

			// Parse for config using run name

			if (args[current].equals(LOAD_WITH_RUN_NAME)) {
				current++;
				if (args[current].contains("-") || m_configList.size() > 1) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}

				configRunName = args[current];
				startWithRunname = true;
				continue;
			}

			// Parse for list of property file names to overrride
			if (args[current].equals(PROP_FILE_OPTION)) {
				current++;
				if (args[current].contains("-")) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}

				propFileNames = args[current];
				// System.out.println(propFileNames);
				continue;
			}

			// User specified a password for config decryption
			if (args[current].equals(CONFIG_ENCRYPTED_OPTION) && operation.equals(START_OPER)) {
				current++;
				if (args[current].equals(ASSEMBLY_LINE_OPTION) || args[current].equals(CONFIG_OPTION)) {
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
				}
				m_Config_Password = args[current];
				if (m_Config_Password != null && m_Config_Password.trim().length() == 0) {
					m_Config_Password = null;
				}
				continue;
			}

			// User specified config to be run as temp
			if (args[current].equals(CONFIG_TEMP_OPTION) && operation.equals(START_OPER)) {
				startTemp = true;
				continue;
			}

			// User specified the controlled stop
			if (args[current].equals(FORCE_CONTROLLED_OPTION) && operation.equals(STOP_OPER)) {
				controlledStop = true;
				continue;
			}

			// Listen for messages logged by the AL
			if (args[current].equals(LISTEN_OPTION)) {
				listen = true;
				continue;
			}

			// Execute synchronously
			if (args[current].equals(SYNC_OPTION)) {
				sync = true;
				continue;
			}

			// Debug AL components
			if (operation.equals(DEBUG_OPER)) {
				if (args[current].equals(ALC_OPTION) && (args.length - 1 >= current + 1)) {
					b_AllComponents = false;
					current++;
					m_componentList = tokenizeToList(args[current], ",");
					continue;
				} else if (args[current].equals(ON_OPTION)) {
					isDebugOn = true;
					continue;
				} else if (args[current].equals(OFF_OPTION)) {
					isDebugOn = false;
					continue;
				}
			}

			// Else some illegal option encountered. Show error.
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), operation);
		}

		// SPECIFIC TESTS FOR START AND STOP OPERATIONS
		if (operation.equals(START_OPER) || operation.equals(STOP_OPER)) {
			// Check that CONFIGURATIONS have been specified.
			// This is mandatory.
			verifySingleConfig(operation);
			if (startTemp && m_Config_Password != null) {
				throw new IllegalCommandUsageException(resHash.getString("TEMP_PASS_NOT_ALLOWED"), operation);
			}
		}

		if (operation.equals(QUERY_OPER)) {
			verifySingleConfig(operation);
		}

		if (listen) {
			verifyOperationForOption(operation, LISTEN_OPTION, new String[] { START_OPER, STATUS_OPER });
			verifySingleConfig(operation);
			if (m_userExecutionChoice.indexOf('A') > 0 && (m_assemblyLineList != null)) {
				verifySingleAL(operation);
			}
		}

		if (sync) {
			verifyOperationForOption(operation, SYNC_OPTION, new String[] { START_OPER });
			verifySingleConfig(operation);
			if (m_userExecutionChoice.indexOf('A') > 0 && (m_assemblyLineList != null)) {
				verifySingleAL(operation);
			}
		}

		if (operation.equals(DEBUG_OPER)) {
			verifySingleConfig(operation);

			if (m_userExecutionChoice.indexOf('A') > 0) {
				verifySingleAL(operation);
			} else {
				throw new IllegalCommandUsageException(resHash.getString("OPT_MANDATORY", ASSEMBLY_LINE_OPTION), operation);
			}
		}

	}

	/**
	 * For the given file - gets the attributes and constructs the entry object
	 *
	 * @param filename
	 *            the path to the file
	 * @return an Entry object
	 */
	private static Entry getAttributeEntryFromFile(String filename) throws Exception {
		Entry initWorkEntry = new Entry();
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(new File(filename)));
		} catch (Exception ex) {
			log(ERROR, "Unable to get file:" + filename, ex);
			throw ex;
		}
		String line = null;
		String[] token;
		String attr, val = null;
		try {
			while ((line = r.readLine()) != null) {
				//token = line.split(":");
				token = line.split(":",2);
				attr = token[0];
				val = token[1];
				initWorkEntry.setAttribute(attr, val);
			}
		} catch (Exception ex) {
			log(ERROR, "Unable to parse attributes in file", ex);
			throw ex;
		} finally {
			if (r != null) {
				r.close();
				r = null;
			}
		}

		return initWorkEntry;
	}

	private static Entry getAttributeEntry(String args) throws IllegalCommandUsageException {
		// Remove "{" and "}"
		int lastIndex = args.indexOf("}");
		args = args.substring(1, lastIndex); // Ignore startinmg {

		String[] attrList = args.split(";");

		int len = attrList.length;
		if (attrList.length == 0) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), START_OPER);

		}
		Entry initWorkEntry = new Entry();
		String temp = null;
		String[] val = null;
		try {
			for (int i = 0; i < len; i++) {
				temp = attrList[i];
				//val = temp.split(":");
				val = temp.split(":", 2);
				String attrName = null;
				String attrVal = null;
				if (val.length > 0) {
					attrName = val[0].trim();
					attrVal = val[1].trim();
					initWorkEntry.setAttribute(attrName, attrVal);
				}
			}

		} catch (Exception ex) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), START_OPER);
		}

		return initWorkEntry;
	}

	/**
	 * Parses the command line options for custom event notification option. It
	 * puts the appropriate values in <code>m_EventName</code>,
	 * <code>m_EventSource</code>, and <code>m_EventData</code>.
	 *
	 * @param args
	 *            The params passed from command line
	 *
	 * @throws IllegalCommandUsageException
	 *             if an unknown, incorrect option is encountered.
	 */
	private static void parseEventOptions(String args[]) throws IllegalCommandUsageException {

		for (int current = m_ArgumentsProcessed; current < args.length; current++) {
			if (current == args.length - 1) {
				// This means that the current pointer is on the LAST argument.
				// There is NO WAY that there can be only ONE argument left and
				// not yet
				// parsed. Every argument for this option occurs in pairs of
				// two.
				// -e <value>, -s <value>, -d <value>. Hence this case should
				// not come.
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);
			}
			if (args[current].equals(EVENT_NAME_OPTION)) // If event name
			{
				current++;
				if (m_EventName == null)
					m_EventName = args[current];
				else
					// mentioned more than once
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);

				continue; // continue with next argument.
			} else if (args[current].equals(EVENT_SOURCE_OPTION)) {
				current++;
				if (DEFAULT_EVENT_SOURCE.equals(m_EventSource))
					m_EventSource = args[current];
				else
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);

				continue; // continue with next argument.
			} else if (args[current].equals(EVENT_DATA_OPTION)) {
				current++;
				if (m_EventData == null)
					m_EventData = args[current];
				else
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);

				continue; // continue with next argument.
			}
			// Else some illegal option encountered. Show error.
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);
		}

		if (m_EventName == null) { // This is a mandatory option
			message(resHash.getString("OPT_MANDATORY", EVENT_NAME_OPTION));
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), EVENT_NOTIFIC_OPER);
		}
	}

	/**
	 * Parses the command line options for "prop" option.
	 * <p>
	 * <code>m_Prop_Operation</code> contains the OPERATION type (LIST, GET,
	 * SET, DEL)<br/>
	 * <code>m_bProtect</code> contains the flag for encrypt true or not.
	 * <code>m_Config</code> contains the config name to work with [Mandatory]
	 * else exception is thrown. <code>m_PropStoreName</code> contains the store
	 * to work with (if -o option specified) <code>m_PropKey</code> contains the
	 * Property Key name (if specified). <code>m_PropVal</code> contains the
	 * Property Value (if specified).
	 *
	 * @param args
	 *            The command line arguments.
	 * @throws IllegalCommandUsageException
	 *             If any of the parsing checks fail.
	 */

	private static void parseConfigPropertiesOptions(String args[]) throws IllegalCommandUsageException {

		for (int current = m_ArgumentsProcessed; current < args.length; current++) {
			// LIST PROP STORES OPTION
			if (args[current].equals(LIST_PROP_STORES_OPTION)) {
				// Set operation to LIST (if not set to something else already)
				if (m_Prop_Operation == -1)
					m_Prop_Operation = LIST_STORE;
				else {
					message(resHash.getString("PROP_OPTIONS_MUTUALLY_EXCLUSIVE"));
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
				}
				continue; // continue with next argument.
			}
			// ENCRYPT PROPERTY OPTION
			if (args[current].equals(ENCRYPT_PROP_OPTION)) {
				m_bProtect = true;
				continue; // continue with next argument.
			}

			// CONFIG NAME OPTION (mandatory)
			if (args[current].equals(CONFIG_OPTION) && (current < (args.length - 1))) // If
																						// event
																						// name
			{
				current++;
				if (m_Config == null)
					m_Config = args[current];
				else
					// mentioned more than once
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);

				continue; // continue with next argument.
			}

			// SET PROPERTY STORE OPTION
			if (args[current].equals(PROP_STORE_OPTION)) {
				current++;
				if (m_PropStoreName == null)
					m_PropStoreName = args[current];
				else
					// mentioned more than once.
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);

				continue; // continue with next argument.
			}

			// GET A PROPERTY VALUE OPTION
			if (args[current].equals(GET_PROP_OPTION) && (current < (args.length - 1))) {
				current++;
				// Set operation to GET
				if (m_Prop_Operation == -1)
					m_Prop_Operation = GET_VALUE;
				else {
					message(resHash.getString("PROP_OPTIONS_MUTUALLY_EXCLUSIVE"));
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
				}
				// Store the KEY to GET
				if (args[current].equals(ALL)) {
					m_bAllKeys = true;
				} else {
					m_PropKey = args[current];
					m_bAllKeys = false;
				}

				continue; // continue with next argument.
			}

			// SET A PROPERTY VALUE OPTION
			if (args[current].equals(SET_PROP_OPTION) && (current < (args.length - 1))) {
				current++;
				// Set operation to SET
				if (m_Prop_Operation == -1)
					m_Prop_Operation = SET_VALUE;
				else {
					message(resHash.getString("PROP_OPTIONS_MUTUALLY_EXCLUSIVE"));
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
				}
				// Store the KEY and VALUE to SET
				String keyAndVal = args[current];
				if (keyAndVal.indexOf('=') > 0) {
					m_PropKey = (keyAndVal.substring(0, keyAndVal.indexOf('='))).trim();
					m_PropVal = (keyAndVal.substring(keyAndVal.indexOf('=') + 1)).trim();
				} else { // Incorrect KEY=VALUE is the format
					log(ERROR, resHash.getString("REMOTESERVERCOMMAND.THE.KEY.VALUE.FORMAT.SHOULD.HAVE.BEEN.SPECIFIED", keyAndVal));
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
				}
				continue; // continue with next argument.
			}

			// DELETE A PROPERTY VALUE OPTION
			if (args[current].equals(DEL_PROP_OPTION) && (current < (args.length - 1))) {
				current++;
				// Set operation to DEL
				if (m_Prop_Operation == -1)
					m_Prop_Operation = DEL_VALUE;
				else {
					message(resHash.getString("PROP_OPTIONS_MUTUALLY_EXCLUSIVE"));
					throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
				}
				// Store the KEY to DEL
				m_PropKey = args[current];
				continue; // continue with next argument.
			}

			// Else some illegal option encountered. Show error.
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
		}

		if (m_Config == null) {
			message(resHash.getString("OPT_MANDATORY", CONFIG_OPTION));
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
		}
		if (m_PropStoreName == null && m_Prop_Operation == DEL_VALUE) {
			message(resHash.getString("PROP_STORE_REQD"));
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), PROP_OPER);
		}
	}

	/**
	 * Operation Execution for Server Information. Note: The
	 * <code>m_ArgumentsProcessed</code> should be set to the point from where
	 * to continue reading the rest of the paramters.
	 *
	 * @param args
	 *            The command line arguments.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int execServerInfo(String[] args) throws IllegalCommandUsageException {

		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		// Get the operation specific option
		if (m_ArgumentsProcessed >= args.length) {
			// OK ! Get the server information
			retCode = viewServerInformation();
		} else if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) // Contains
		// "-?"
		// therefore
		// print
		// usage of
		// this
		// option
		{
			message(getCommandUsage(SRVINFO_OPER));
			retCode = RC_OK;
		} else {
			throw new IllegalCommandUsageException(null, SRVINFO_OPER);
		}
		return retCode;

	}

	/**
	 * Connects to the remote server and obtains its server information.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int viewServerInformation() {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.VIEWSERVERINFORMATION"));
		}

		if (connect() == RC_FAIL)
			return RC_FAIL;

		try {
			com.ibm.di.api.remote.ServerInfo serverInfo = m_Session.getServerInfo();
			if (serverInfo != null) {
				String serverIP = serverInfo.getIPAddress();
				String bootTime = serverInfo.getServerBootTime().toString();
				String version = serverInfo.getServerVersion();
				String operatingSystem = serverInfo.getOperatingSystem();
				String serverReloadTime = bootTime; // Currently there is no API
				// for this. Taken code as
				// such from AMC2.

				String displayString = resHash.getString("TDI_SERVER_INFO", new String[] { serverIP, m_ServerPort, m_ServerHost,
						version, bootTime, serverReloadTime, operatingSystem });
				message(displayString);
			} else {
				log(ERROR, resHash.getString("REMOTESERVERCOMMAND.SERVERINFO.IS.NULL"));
				return RC_FAIL;
			}
		} catch (Exception e) {
			log(ERROR, e);
			return RC_FAIL;
		}
		return RC_OK;
	}

	/**
	 * Sends the user specified event.
	 * <p>
	 * The <code>m_EventName</code> MUST have the event name.<br>
	 * The <code>m_EventSource</code> should have the event source or can be
	 * <code>null</code>.<br>
	 * The <code>m_EventData</code> should have the data the user wishes to pass
	 * or can be <code>null</code>.<br>
	 *
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 * @see #parseEventOptions(String[])
	 * @see #execEventNotification(String[])
	 */

	private static int sendEventNotification() {
		int retCode = RC_OK;

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.SENDEVENTNOTIFICATION"));
		}
		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}

		try {
			m_Session.sendCustomNotification(m_EventName, m_EventSource, m_EventData);
			message(resHash.getString("EVENT_NOTIFICATION_SUCCESSFUL", m_EventName));

		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.SENDINGCUSTOMNOTIFICATION", new String[] { m_EventName,
					m_EventSource, m_EventData }), ex);
			retCode = RC_FAIL;
		}
		return retCode;
	}

	/**
	 * Displays the actual status of the configs/ALs.
	 * <p>
	 * This method makes use of the following notable member variables:
	 * <p>
	 * <code>m_configList</code><br>
	 * The list of configs whose status is to be shown. This will be ignored if
	 * <code>allConfigs</code> is <code>true</code>.
	 * <p>
	 * <code>m_assemblyLineList</code><br>
	 * The list of assembly lines whose status is to be shown. This will be
	 * ignored if <code>allAssembly</code> is <code>true</code>.
	 * <p>
	 * <code>b_allConfigs</code><br>
	 * <code>true</code> indicates that all configs should be listed.
	 * <p>
	 * <code>b_allAssembly</code><br>
	 * <code>true</code> indicates that all assembly lines should be listed.
	 * <p>
	 * <code>m_userExecutionChoice</code><br>
	 * This variable is used to decide what all componets are to be listed.This
	 * is the OVERRIDING variable.<br>
	 * If this variable contains "C" means, configs should be listed. <br>
	 * If this variable contains "A" means, assembly lines should be listed.<br>
	 * <br>
	 *
	 *
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 * @see #parseCAE(String[], String)
	 *
	 */
	private static int viewStatus() {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.VIEWSTATUS"));
		}

		int retCode = RC_OK;

		List<String> userSpecifiedConfigs_running = new ArrayList<String>();
		// Will hold the user specified configs which are found to be running.

		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}

		List<String> userSpecifiedConfigList = new ArrayList<String>();
		// Store original config names to check for solution name.
		// Check for relative paths of configs
		if (!b_AllConfigs) {
			for (int i = 0; i < m_configList.size(); i++) {
				// For each user specified config
				userSpecifiedConfigList.add(m_configList.get(i));
				m_configList.set(i, prefixIfRelativePath(m_configList.get(i)));

			}
		}

		if (m_userExecutionChoice.indexOf('C') >= 0) {
			Hashtable<String,String> runningConfigTable = getRunningConfig();

			message("");
			message(resHash.getString("CONFIG_HEADING"));
			boolean found = false;

			if (b_AllConfigs) {
				// DISPLAY RUNNING CONFIGS
				java.util.Enumeration<String> enum1 = runningConfigTable.keys();
				String configName = null;
				String tempConfigID = null;
				String loadedConfigID = null;
				while (enum1.hasMoreElements()) {
					found = true;
					configName = enum1.nextElement();
					// D8241 Show Solution Name if solution name is defined.
					// Otherwise show file path.
					tempConfigID = convertURLtoID(configName);
					loadedConfigID = runningConfigTable.get(configName);
					if (tempConfigID.equals(loadedConfigID)) {
						// Config ID is same as URL.
						// Therefore no solution defined.
						message("\n" + CONFIG_TYPE + DELIMITER + configName + DELIMITER + RUNNING);
					} else {
						// Solution Name defined.
						message("\n" + CONFIG_TYPE + DELIMITER + loadedConfigID + DELIMITER + RUNNING);
					}
				}

				if (!found) {
					message("\n" + resHash.getString("NONE"));
				}

			} else // Display information of specific configs.
			{
				ConfigInstance ci = null;

				// For each user specified config
				for (int i = 0; i < m_configList.size(); i++) {
					try {
						if (runningConfigTable.containsKey(m_configList.get(i))) {
							message("\n" + CONFIG_TYPE + DELIMITER + m_configList.get(i) + DELIMITER + RUNNING);
							userSpecifiedConfigs_running.add(m_configList.get(i));

							ci = m_Session.getConfigInstance(runningConfigTable.get(m_configList.get(i)));
						} else if (runningConfigTable.containsValue(userSpecifiedConfigList.get(i))) { // Maybe
							// user specified solution name
							message("\n" + CONFIG_TYPE + DELIMITER + userSpecifiedConfigList.get(i) + DELIMITER + RUNNING);
							ci = m_Session.getConfigInstance(userSpecifiedConfigList.get(i));

							// From solution name get the file url
							userSpecifiedConfigs_running.add(ci.getConfiguration().toString());

							// For now, the last else covers the
							// stoppedConfigTable.containsKey(m_configList.get(i)
							// ) case and everything else. In the future, we may
							// add this else if in when there is something to do
							// for the other cases.
						} else {
							message("\n" + CONFIG_TYPE + DELIMITER + m_configList.get(i) + DELIMITER + STOPPED);
						}
						if (listen && m_userExecutionChoice.indexOf('A') < 0) {
							listenConfigInstance(ci, m_Session.isSSLon());
						}
					} catch (Exception ex) {
						log(ERROR, ex);
					}
				}
			}

		}

		if (m_userExecutionChoice.indexOf('A') >= 0) {
			message("");
			message(resHash.getString("AL_HEADING"));

			if (b_AllAssembly) {
				if (m_userExecutionChoice.indexOf('C') >= 0) {
					// If user has specified configs also
					if (b_AllConfigs) {
						// If user has mentioned details of ALL configs
						// Then just pick up the running ones, since the stopped
						// configs cannot have running assembly lines :)
						retCode = viewAllALStatus(null, null);
					} else {
						// Take the user specified configs.
						retCode = viewAllALStatus(userSpecifiedConfigs_running, null);
					}
				} else {
					// Get all assembly from SESSION (user has not
					// specified any configs)
					retCode = viewAllALStatus(null, null);
				}
			} else // User specified assembly lines
			{
				if (m_userExecutionChoice.indexOf('C') >= 0 && !b_AllConfigs) {
					// If user has specified configs also, then just get those
					// assembly lines which are in the configs.
					retCode = viewAllALStatus(userSpecifiedConfigs_running, m_assemblyLineList);
				} else { // Get specified assembly name from all configs
					retCode = viewAllALStatus(null, m_assemblyLineList);
				}
			}
		}

		return retCode;
	}

	/**
	 * Starts the user specified configs/ALs.
	 * <p>
	 * This method makes use of the following notable member variables:
	 * <p>
	 * <code>m_configList</code><br>
	 * The configs which should be started. This is mandatory. Only ONE config
	 * is taken (the first one in the list).
	 * <p>
	 * <code>m_assemblyLineList</code><br>
	 * The list of assembly lines to be started. This will be ignored if
	 * <code>b_AllAssembly</code> is <code>true</code>.
	 * <p>
	 * <code>b_AllAssembly</code><br>
	 * <code>true</code> indicates that all assembly lines should be started.
	 * <p>
	 * <code>m_userExecutionChoice</code><br>
	 * This variable is used to decide what all componets are to be started.This
	 * is the OVERRIDING variable.<br>
	 * If this variable contains "C" means, configs should be started
	 * (MANDATORY). <br>
	 * If this variable contains "A" means, assembly lines should be started.<br>
	 * <br>
	 *
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 * @see #parseCAE(String[], String)
	 *
	 */
	private static int startComponents() {

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.STARTCOMPONENTS"));
		}

		int retCode = RC_OK;

		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}
		message("");
		message(resHash.getString("CONNECTED_SRV", m_ServerHost + ":" + m_ServerPort));

		// try starting the user specified configuration.
		ConfigInstance configInstance = null;
		if (m_configList != null && m_configList.size() >= 1) {
			String origConfig = m_configList.get(0);
			String configuration = prefixIfRelativePath(origConfig);

			try {
				configInstance = startConfigInstance(origConfig, configuration);
			} catch (Exception ex) {
				log(ERROR, resHash.getString("CONFIG_NOT_START", configuration), ex);
				message(resHash.getString("CONFIG_NOT_START", configuration));
				return RC_FAIL;
			}
		} else {
			// No config specified
			return RC_FAIL;
		}

		// try starting the user specified assembly lines
		if (m_userExecutionChoice.indexOf('A') > 0) {
			retCode = startAssemblyLines(configInstance);
		}

		return retCode;
	}

	/**
	 * Starts the user specified config.
	 *
	 * @param origConfig
	 *            the config instance spesified by the user
	 * @param configuration
	 *            if the <code>origConfig</code> is not absolute this is: <li>
	 *            remote config folder path</li> <li>same as
	 *            <code>origConfig</code> if -t option is specified</li>
	 * @return ConfigInstance object representing the started or already running
	 *         config
	 * @throws Exception
	 */
	private static ConfigInstance startConfigInstance(String origConfig, String configuration) throws Exception {

		ConfigInstance configInstance = null;
		String xmlConfig = null;

		if (startTemp) {
			xmlConfig = FileUtils.loadFile(configuration);
		} else if (!startWithRunname) {
			Hashtable<String,String> runningConfigs = getRunningConfig();

			// If solname is defined it is returned as object in the key/
			// object pair in hashtable. Check if hash table contains this value
			boolean isSolnName = runningConfigs.containsValue(origConfig);
			String configId = null;

			if (isSolnName) {
				configId = origConfig;
			} else {
				configId = runningConfigs.get(configuration);
			}

			// Config is already running
			if (configId != null) {
				configInstance = m_Session.getConfigInstance(configId);
				message(resHash.getString("CONFIG_ALREADY_RUNNING", configInstance.getConfiguration().toString()));
				return configInstance;
			}
		}

		if (listen && m_userExecutionChoice.indexOf('A') < 0) {
			CIListenerSetup l = null;
			try {
				l = new CIListenerSetup(m_Session.isSSLon());

				message(resHash.getString("LISTENING.CI"));
				if (startTemp) {
					configInstance = m_Session.startTempConfigInstance(xmlConfig, true, configRunName, propFileNames,
							l.getListener());

					// Show the config ID
					message(resHash.getString("CONFIG_STARTED", configInstance.getConfigId()));
				} else {
					configInstance = m_Session.startConfigInstance(configuration, true, m_Config_Password, configRunName,
							propFileNames, l.getListener());

					// Show the config path
					message(resHash.getString("CONFIG_STARTED", configInstance.getConfigPath()));
				}
				l.listen(configInstance.getConfigId());
			} finally {
				if (l != null) {
					if (configInstance != null) {
						configInstance.removeLogListener(l.getListener());
					}
					l.close();
				}
			}
		} else {
			if (startTemp) {
				configInstance = m_Session.startTempConfigInstance(xmlConfig, true, configRunName, propFileNames);
			} else {
				configInstance = m_Session
						.startConfigInstance(configuration, true, m_Config_Password, configRunName, propFileNames);

				// Show the config path
				message(resHash.getString("CONFIG_STARTED", configInstance.getConfigPath()));
			}
		}
		return configInstance;
	}

	/**
	 * Stops the user specified configs/ALs.
	 * <p>
	 * Note that this method follows the following algorithm:<br>
	 * 1. If the user has specified ONLY -c option, that is no ALs, then just
	 * stop the specified configs and exit.<br>
	 * 2. If the user has specified EITHER the ALs to stop, then based on the
	 * config specified, obtain a handle to the specified ALs and stop them. In
	 * this case the method will <b>NOT</b> stop the config.<br>
	 * 3. "-c" option is mandatory.
	 * <p>
	 * This method makes use of the following notable member variables:
	 * <p>
	 * <code>m_configList</code><br>
	 * This is mandatory. Only ONE config is taken (the first one in the list).
	 * <p>
	 * <code>m_assemblyLineList</code><br>
	 * The list of assembly lines to be stopped. This will be ignored if
	 * <code>b_AllAssembly</code> is <code>true</code>.
	 * <p>
	 * <code>b_AllAssembly</code><br>
	 * <code>true</code> indicates that all assembly lines should be stopped.
	 * <p>
	 * <code>m_userExecutionChoice</code><br>
	 * This variable is used to decide what all components are to be
	 * stopped.This is the OVERRIDING variable.<br>
	 * If this variable contains <b>ONLY</b> "C" means, configs should be
	 * stopped (MANDATORY). <br>
	 * If this variable contains "A" means, assembly lines should be stopped and
	 * configs will NOT be stopped by the command.<br>
	 *
	 * 2006/12/04 This method has been modified for defect 8051 - support FN-13
	 * on CLI
	 *
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 * @see #parseCAE(String[], String)
	 *
	 */
	private static int stopComponents() {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.STOPCOMPONENTS"));
		}

		int retCode = RC_OK;

		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}
		message("");
		message(resHash.getString("CONNECTED_SRV", m_ServerHost + ":" + m_ServerPort));

		// Obtain the config HANDLE
		ConfigInstance configInstance = null;
		String configName = null;
		boolean isSolnName = false;
		if (m_configList != null && m_configList.size() >= 1) {
			configName = m_configList.get(0);
			String configuration = prefixIfRelativePath(configName);
			try {
				Hashtable<String,String> runningConfigs = getRunningConfig();
				String configId = runningConfigs.get(configuration);
				if (configId == null) {
					// Code added for support of FN-13 on CLI
					/*
					 * since the getRunningConfigs()- key/value pair . Value
					 * contains the Soln name so check if this is the case
					 */
					isSolnName = runningConfigs.containsValue(configName);

					if (isSolnName)
						configId = configName;
					else {
						// This config is already stopped ! CANNOT Continue.
						message(resHash.getString("CONFIG_NOT_RUNNING", configuration));
						return RC_FAIL;
					}
				}

				configInstance = m_Session.getConfigInstance(configId);
				if (m_userExecutionChoice.equals("C")) // ONLY CONFIG
				{
					// Then stop the config and return HAPPILY :)
					try {
						if (controlledStop)
							configInstance.stop(false);
						else
							configInstance.stop();
						message(resHash.getString("CONFIG_STOPPED", configuration));
						return RC_OK;
					} catch (Exception ex) {
						message(resHash.getString("STOP_CONFIG_ERR", configuration));
						log(ERROR, resHash.getString("STOP_CONFIG_ERR"), ex);
						return RC_FAIL;
					}
				}
			} catch (Exception ex) {
				log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.GET.CONFIG.INFO.STOP.THE.CONFIG"), ex);
				return RC_FAIL;
			}

		} else // No configs specified !
		{
			return RC_FAIL;
		}

		// try stopping the user specified assembly lines

		if (m_userExecutionChoice.indexOf('A') > 0) {

			try {
				// Get the list of running ALs.
				AssemblyLine[] runningALs = configInstance.getAssemblyLines();
				List<String> runningAL_list = new ArrayList<String>();
				for (int i = 0; i < runningALs.length; i++) {
					String name = runningALs[i].getName();
					if (name != null && name.startsWith(ASSEMBLY_LINE_FOLDER_PREFIX))
						name = name.substring(ASSEMBLY_LINE_FOLDER_PREFIX.length());
					runningAL_list.add(name);
				}

				// Get list of all assemblies in this config.
				String[] listOfALs = configInstance.getAssemblyLineNames();
				AssemblyLine assemblyLine = null;

				// Then stop the assembly lines.
				String alName = null;
				if (b_AllAssembly) // User wishes to stop all running assembly
				// lines.
				{
					for (int i = 0; i < runningALs.length; i++) {
						assemblyLine = runningALs[i];
						alName = runningAL_list.get(i);
						try {
							if (controlledStop)
								assemblyLine.stop(false);
							else
								assemblyLine.stop();
							message(resHash.getString("AL_STOPPED", alName));
						} catch (Exception ex) {
							message(resHash.getString("ERR_STOP_AL", alName));
							retCode = RC_FAIL;
							log(ERROR, resHash.getString("ERR_STOP_AL", alName), ex);
						}

					}
				} else {
					// user has specified a list of AssemblyLines to stop.

					for (int i = 0; i < m_assemblyLineList.size(); i++) {
						alName = m_assemblyLineList.get(i);
						int index = runningAL_list.indexOf(alName);
						if (index < 0) // if not running
						{
							// Check if this is a valid assembly line name.
							if (contains(listOfALs, alName)) // Valid
							{
								message(resHash.getString("AL_ALREADY_STOPPED", alName));
								retCode = RC_FAIL;
							} else // Not found. Invalid assembly line name.
							{
								message(resHash.getString("AL_NOT_FOUND", alName));
								retCode = RC_FAIL;
							}
						} else // It is a running AL.
						{
							assemblyLine = runningALs[index];
							try {
								if (controlledStop)
									assemblyLine.stop(false);
								else
									assemblyLine.stop();
								message(resHash.getString("AL_STOPPED", alName));
							} catch (Exception ex) {
								message(resHash.getString("ERR_STOP_AL", alName));
								retCode = RC_FAIL;
								log(ERROR, resHash.getString("ERR_STOP_AL", alName), ex);
							}
						}
					}
				}
			} catch (Exception ex) {
				log(ERROR, resHash.getString("STOP_ALS_ERR"), ex);
				message(resHash.getString("STOP_ALS_ERR"));
				return RC_FAIL;
			}
		}

		return retCode;
	}

	/**
	 * Checks if the <code>valToSearch</code> exists in the <code>list</code>.
	 *
	 * @param list
	 *            The list to search in.
	 * @param valToSearch
	 *            The value to search in the list.
	 * @return <code>true</code> if found, otherwise <code>false</code>.
	 */
	private static boolean contains(String[] list, String valToSearch) {
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(valToSearch))
				return true;
		}
		return false;
	}

	/**
	 * Will show the status of all ALs. If the <code>listOfConfigs</code> is
	 * <code>null</code> then it will imply that get AL info for all the
	 * configs. If a list of Configs is passed, then it will obtain the AL info
	 * for only those configs. Same is the case with <code>listofALs</code>.
	 * Ifit is <code>null</code> then it will imply that the user wants
	 * information for all ALs, whereas if <code>listOfAls</code> is defined,
	 * then user will be shown status for only the ones mentioned in the
	 * listOfALs.
	 *
	 * @param listOfConfigs
	 *            The list of configs to which the ALs should be limited.
	 * @param listOfALs
	 *            The list if ALs to which the output must be limited.
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 */
	private static int viewAllALStatus(List<String> listOfConfigs, List<String> listOfALs) {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.VIEWALLALSTATUS"));
		}

		int retCode = RC_OK;

		// Get a list of running configs

		try {
			ConfigInstance[] runningConfigs = m_Session.getConfigInstances();
			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.NO.OF.RUNNING.CONFIGS", "" + runningConfigs.length));
			}

			if (listOfConfigs != null) {
				runningConfigs = removeConfigFromList(runningConfigs, listOfConfigs);
			}
			// Now we only have those configs which the user requested (or all
			// configs if user requested for all).
			// For each config obtain the status of all its ALs

			Hashtable<String,String> alStatusTable = new Hashtable<String,String>();
			Hashtable<String, AssemblyLine> alTable = new Hashtable<String, AssemblyLine>();
			int cut = ASSEMBLY_LINE_FOLDER_PREFIX.length();
			AssemblyLine[] runningALs = null;
			String shortName = null;
			String stats = null;
			String configID = null;
			final String seperator = "#0#";
			String key = null;

			for (int i = 0; i < runningConfigs.length; i++) {
				// Get all running ALs details for this config
				runningALs = runningConfigs[i].getAssemblyLines();
				configID = runningConfigs[i].getConfigId();
				for (int j = 0; j < runningALs.length; j++) {
					shortName = runningALs[j].getName().substring(cut);
					stats = runningALs[j].getStatistics().toString();
					key = configID + seperator + shortName;
					if (!alStatusTable.containsKey(key)) { // Store AL status
						// in hashtable
						alStatusTable.put(key, AL_TYPE + DELIMITER + shortName + DELIMITER + RUNNING + DELIMITER + stats);
					}
					if (!alTable.containsKey(key)) {
						alTable.put(key, runningALs[j]);
					}
				}

				// Get all stopped ALs details for this config
				//MetamergeConfig meta = runningConfigs[i].getConfiguration();
				//MetamergeFolder metaFolder = meta.getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER);

				//String[] assemblyLineNames = metaFolder.getNames();

				String[] assemblyLineNames = runningConfigs[i].getAssemblyLineNames();

				for (int j = 0; j < assemblyLineNames.length; j++) {
					key = configID + seperator + assemblyLineNames[j];
					// If not in hashtable - then it must be a stopped AL
					if (!alStatusTable.containsKey(key)) { // Store AL status
						// in hashtable
						alStatusTable.put(key, AL_TYPE + DELIMITER + assemblyLineNames[j] + DELIMITER + STOPPED);
					}
				}
			}

			// Now for all user specified configs we have the ConfigID_ALs with
			// their display status in
			// hashtable. Now check if user specified to see status for specific
			// ALs or not.
			// If yes, then show status of only those from hashtable, if no,
			// then show status
			// of all ALs in hash table.

			if (listOfALs == null) // Means show for all ALs
			{
				Vector<String> v = new Vector<String>(alStatusTable.keySet());
				Collections.sort(v); // gets sorted by CONFIG_ID+ALs (so all
				// ALs of a config are shown together)
				for (int i = 0; i < v.size(); i++) {
					message("\n" + alStatusTable.get(v.elementAt(i)));
				}

			} else // Show status for specific ALs only.
			{
				// From the hash table obtain those keys which contain this AL
				// name (there
				// can be more than one - since same ALs may be present in more
				// than one config.
				Vector<String> v_ConfigAL = new Vector<String>(alStatusTable.keySet());
				Vector<String> v_OnlyAL = new Vector<String>();
				for (String temp: v_ConfigAL) {
					// Remove the config name
					temp = temp.substring(temp.indexOf(seperator) + seperator.length());
					v_OnlyAL.add(temp);
				}

				String status = null;
				for (int i = 0; i < listOfALs.size(); i++) {
					if (v_OnlyAL.contains(listOfALs.get(i))) {
						Vector<Integer> matchedIndexes = getMatchingIndexes(v_OnlyAL, listOfALs.get(i));
						for (int k = 0; k < matchedIndexes.size(); k++) {
							// for each matched index
							int index = matchedIndexes.elementAt(k);
							status = alStatusTable.get(v_ConfigAL.get(index));
							message("\n" + status);

							if (listen) {
								AssemblyLine al = alTable.get(v_ConfigAL.get(index));
								listenAssemblyLine(al, m_Session.isSSLon());
							}
						}
					} else {
						status = AL_TYPE + DELIMITER + listOfALs.get(i) + DELIMITER + NOT_FOUND;
						message("\n" + status);
					}

				}
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.GET.DETAILS.FOR.ASSEMBLY.LINES"), ex);
			retCode = RC_FAIL;
		}

		return retCode;
	}

	/**
	 * From the given list, it will find out the indexes which have the matching
	 * element and return all thoses indexes in a vector.
	 *
	 * @param list
	 *            The list to search in.
	 * @param stringToSearch
	 *            The string to search in the list.
	 * @return A vector containing the matching indexes.
	 */
	private static Vector<Integer> getMatchingIndexes(Vector<String> list, String stringToSearch) {
		Vector<Integer> v = new Vector<Integer>();
		for (int i = 0; i < list.size(); i++) {
			if (list.elementAt(i).equals(stringToSearch))
				v.add(i);
		}
		return v;
	}

	/**
	 * From the masterList, it removes those configs whose name is not in the
	 * passed configsToKeep.
	 *
	 * @param masterList
	 * @param configsToKeep
	 *
	 * @return The list of ConfigInstances which are to be kept.
	 */

	private static ConfigInstance[] removeConfigFromList(ConfigInstance[] masterList, List<String> configsToKeep) throws Exception {
		int numberOfConfigsFound = 0;
		for (int i = 0; i < masterList.length; i++) {
			MetamergeConfig metaConfig = masterList[i].getConfiguration();
			if (!configsToKeep.contains(metaConfig.toString())) {
				// Nullify the current config instance object
				masterList[i] = null;
			} else {
				numberOfConfigsFound++;
			}
		}

		ConfigInstance[] tempInstance = new ConfigInstance[numberOfConfigsFound];
		int count = 0;
		for (int i = 0; i < masterList.length; i++) {
			if (masterList[i] != null) {
				tempInstance[count] = masterList[i];
				count++;
			}
		}
		return tempInstance;
	}

	/**
	 * Returns a hashtable with KEYS as the name of configs which are running,
	 * and the value as their respective config ids.
	 * <p>
	 * Note: The session must already be establised.
	 *
	 * @return hashtable containing running configs info.
	 */

	private static Hashtable<String, String> getRunningConfig() {
		Hashtable<String, String> runningConfigTable = new Hashtable<String, String>();
		try {
			ConfigInstance[] runningConfigs = m_Session.getConfigInstances();

			MetamergeConfig mConfig = null;

			String vers = getVersion();
			for (int i = 0; i < runningConfigs.length; i++) {
				if (vers.startsWith("6.0") || vers.startsWith("6.1")) {
					mConfig = runningConfigs[i].getConfiguration();
					runningConfigTable.put(mConfig.toString(), runningConfigs[i].getConfigId());
				} else {
					if (runningConfigs[i].getConfigPath() != null) {
						runningConfigTable.put(runningConfigs[i].getConfigPath(), runningConfigs[i].getConfigId());
					} else {
						runningConfigTable.put("temp_config" + i, runningConfigs[i].getConfigId());
					}

				}
			}

		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.GET.LIST.OF.ALL.RUNNING.CONFIGS"), ex);
		}

		return runningConfigTable;
	}

	/**
	 * Connects to the specified remote server.
	 *
	 * @return <code>RC_OK</code> if successfully able to execute the task,
	 *         otherwise <code>RC_FAIL</code>
	 */
	private static int connect() {

		m_ServerHost = m_GeneralOptions.get(GEN_OPT_SRV_HOST);

		if (m_ServerHost == null) {
			// Try taking from ENVIRONMENT
			m_ServerHost = System.getProperty(ENV_SRV_HOST);
			if (m_ServerHost == null)
				m_ServerHost = DEFAULT_SERVER;
		}

		m_ServerPort = m_GeneralOptions.get(GEN_OPT_SRV_PORT);
		if (m_ServerPort == null) {
			// Try taking from environment
			m_ServerPort = System.getProperty(ENV_SRV_PORT);
			if (m_ServerPort == null) {
				m_ServerPort = DEFAULT_PORT;
			}
		}

		// IF SSL DETAILS PASSED
		// Trust store
		String trustStore = m_GeneralOptions.get(GEN_OPT_TRUST_STORE);
		if (trustStore != null) {
			System.setProperty("javax.net.ssl.trustStore", trustStore);
		}
		String trustPassword = m_GeneralOptions.get(GEN_OPT_TRUST_PWD);
		if (trustPassword != null) {
			System.setProperty("javax.net.ssl.trustStorePassword", trustPassword);
			trustPassword = null;
		}

		if (System.getProperty("javax.net.ssl.trustStoreType") == null)
			System.setProperty("javax.net.ssl.trustStoreType", "jks");

		// Key store
		String keystore = m_GeneralOptions.get(GEN_OPT_KEYSTORE);
		if (keystore != null) {
			System.setProperty("javax.net.ssl.keyStore", keystore);
		}
		String keyPassword = m_GeneralOptions.get(GEN_OPT_KEY_PWD);
		if (keyPassword != null) {
			System.setProperty("javax.net.ssl.keyStorePassword", keyPassword);
		}

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.ATTEMPTING.TO.CONNECT.TO.TDI.SERVER", new String[] { m_ServerHost,
					m_ServerPort }));
		}

		try {
			SessionFactory sf = (SessionFactory) Naming.lookup("rmi://" + m_ServerHost + ":" + m_ServerPort + "/SessionFactory");
			if (sf != null) {
				String username = m_GeneralOptions.get(GEN_OPT_USERID);
				String password = m_GeneralOptions.get(GEN_OPT_USERPWD);
				if (username != null && password != null) {
					// Custom authentication.
					if (VERBOSE_MODE) {
						message(resHash.getString("REMOTESERVERCOMMAND.USING.CUSTOM.AUTHENTICATION", username));
					}
					m_Session = sf.createSession(username, password);
				} else {
					m_Session = sf.createSession();
				}

				return RC_OK;
			}
		} catch (Exception e) {
			message(resHash.getString("SRV_CONNECT_ERR", m_ServerHost + ":" + m_ServerPort));
			log(ERROR, e);
		}

		return RC_FAIL;
	}

	/**
	 * Generates a config report for the <code>configURL</code> passed. The
	 * passed <code>configURL</code> must be a loaded config on the server. If
	 * not, then this method will return an error.
	 *
	 * 2006/12/04 This method has been modified for defect 8051 - support FN-13
	 * on CLI
	 *
	 * @param configURL
	 * @return <code>RC_OK</code> if no error occured, otherwise
	 *         <code>RC_FAIL</code>.
	 */
	private static int showReport(String configURL) throws IllegalCommandUsageException {

		int retCode = RC_OK;

		if (configURL.indexOf(",") >= 0) // Only a single config is allowed.
		{
			throw new IllegalCommandUsageException(resHash.getString("ONE_CONFIG_ALLOWED"), REPORT_OPER);
		}

		// Connect to server
		if (connect() == RC_FAIL)
			return RC_FAIL;
		ConfigInstance configInstance = null;
		MetamergeConfig metamergeConfig = null;

		// Get the config instance
		try {
			configInstance = getConfigInstance(configURL, m_Session);
			if (configInstance != null) {
				metamergeConfig = configInstance.getConfiguration();
			} else {
				return RC_FAIL;
			}
		} catch (Exception ex) {
			log(ERROR, ex.getMessage(), ex);
			message(ex.getMessage());
			return RC_FAIL;
		}

		// Tracks if any error occured during config report generation
		boolean errorOccured = false;

		message("");
		message(resHash.getString("CONFIG_REPORT_HEADING"));
		message("");
		String tempStr = prepareString(resHash.getString("CONFIG_LABEL")) + configURL;
		message(tempStr); // Displays -> Config : <configName>

		// get Assembly line details
		String[] assemblyLinesListing = null;
		String comment = null;
		boolean debugMode = false;
		String debugText = null;
		ConnectorConfig conConfig = null;
		ParserConfig parserConfig = null;
		int connectorCount = 0;
		String enabledStatus = null;
		String parser = null;
		String template = null;

		try {
			assemblyLinesListing = configInstance.getAssemblyLineNames();
			AssemblyLine runningAL = null;
			AssemblyLineConfig alConfig = null;
			if (assemblyLinesListing != null && assemblyLinesListing.length > 0) {

				message("");
				message(resHash.getString("AL_HEADING"));
				message("");
				// Display the list of assembly lines
				for (int i = 0; i < assemblyLinesListing.length; i++) {
					alConfig = metamergeConfig.getAssemblyLine(assemblyLinesListing[i]);
					comment = alConfig.getUserComment();
					if (comment == null || comment.trim().length() <= 0)
						comment = resHash.getString("NONE");

					tempStr = prepareString(resHash.getString("NAME_LABEL")) + assemblyLinesListing[i];
					message(tempStr); // Display AL name
					tempStr = prepareString(resHash.getString("COMMENT_LABEL")) + comment;
					message(tempStr); // Display AL comment
					message("");
				}

				// Details of each assembly line
				for (int i = 0; i < assemblyLinesListing.length; i++) {
					String ALName = assemblyLinesListing[i];
					runningAL = getRunningAssemblyLine(configInstance, ALName);
					alConfig = metamergeConfig.getAssemblyLine(ALName);

					message("");
					message(resHash.getString("AL_COMP_HEADING", assemblyLinesListing[i]));
					connectorCount = alConfig.getConnectorCount();

					if (connectorCount == 0) {
						message("");
						message(" " + resHash.getString("NONE"));
					}

					for (int row = 0; row < connectorCount; row++) {
						// Display info for each connector and FC
						conConfig = alConfig.getConnector(row);
						parserConfig = conConfig.getParserConfig();
						if (parserConfig == null || parserConfig.getInheritsFromRef() == null)
							parser = resHash.getString("NONE");
						else {
							parser = parserConfig.getInheritsFromRef();
						}
						if (conConfig.getEnabled())
							enabledStatus = resHash.getString("ENABLED");
						else {
							if (conConfig.getState().equalsIgnoreCase("Passive")) {
								enabledStatus = resHash.getString("PASSIVE");
							} else {
								enabledStatus = resHash.getString("DISABLED");
							}
						}

						if (conConfig.getInheritsFromRef() == null)
							template = resHash.getString("NONE");
						else
							template = conConfig.getInheritsFromRef();

						comment = conConfig.getUserComment();

						if (comment == null || comment.trim().length() <= 0)
							comment = resHash.getString("NONE");

						if (runningAL != null) {
							debugMode = runningAL.getComponentDebugMode(conConfig.getName().toString());
						} else {
							debugMode = getConfigDebugMode(conConfig);
						}

						debugText = debugMode ? resHash.getString("ENABLED") : resHash.getString("DISABLED");

						message("");
						tempStr = prepareString(resHash.getString("NAME_LABEL")) + conConfig.getName();
						message(tempStr); // Display Connector name

						tempStr = prepareString(resHash.getString("MODE_LABEL")) + conConfig.getMode();
						message(tempStr); // Display Connector mode

						tempStr = prepareString(resHash.getString("STATE_LABEL")) + enabledStatus;
						message(tempStr); // Display status (enabled /
						// disabled / passive)

						tempStr = prepareString(resHash.getString("DEBUG_LABEL")) + debugText;
						message(tempStr); // Display value of Debug mode

						tempStr = prepareString(resHash.getString("TEMPLATE_LABEL")) + template;
						message(tempStr); // Display inherited from template

						tempStr = prepareString(resHash.getString("PARSER_LABEL")) + parser;
						message(tempStr); // Display parser connected to this
						// connector

						tempStr = prepareString(resHash.getString("COMMENT_LABEL")) + comment;
						message(tempStr); // Display "description" for the
						// connector

					}
				}
			}
		} catch (RemoteException ex) {
			errorOccured = true;
			log(ERROR, ex.toString(), ex);
		} catch (Exception ex) {
			errorOccured = true;
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.GETTING.ASSEMBLY.LINE.DETAILS"), ex);
		}

		// Connector Library
		MetamergeFolder folder = null;
		String[] components = null;
		try {
			folder = metamergeConfig.getDefaultFolder(MetamergeConfig.CONNECTOR_FOLDER);
			components = folder.getNames();
			message("");
			message(resHash.getString("CONN_LIBRARY_HEADING"));
			if (components.length <= 0) {
				message(resHash.getString("NONE"));
			}
			for (int i = 0; i < components.length; i++) {
				conConfig = metamergeConfig.getConnector(components[i]);
				parserConfig = conConfig.getParserConfig();
				if (parserConfig == null || parserConfig.getInheritsFromRef() == null)
					parser = resHash.getString("NONE");
				else {
					parser = parserConfig.getInheritsFromRef();
				}
				if (conConfig.getEnabled())
					enabledStatus = resHash.getString("ENABLED");
				else {
					if (conConfig.getState().equalsIgnoreCase("Passive")) {
						enabledStatus = resHash.getString("PASSIVE");
					} else {
						enabledStatus = resHash.getString("DISABLED");
					}
				}

				if (conConfig.getInheritsFromRef() == null)
					template = resHash.getString("NONE");
				else
					template = conConfig.getInheritsFromRef();

				comment = conConfig.getUserComment();
				if (comment == null || comment.trim().length() <= 0)
					comment = resHash.getString("NONE");

				message("");
				tempStr = prepareString(resHash.getString("NAME_LABEL")) + conConfig.getName();
				message(tempStr); // Display Connector name

				tempStr = prepareString(resHash.getString("MODE_LABEL")) + conConfig.getMode();
				message(tempStr); // Display Connector mode

				tempStr = prepareString(resHash.getString("STATE_LABEL")) + enabledStatus;
				message(tempStr); // Display status (enabled / disabled /
				// passive)

				tempStr = prepareString(resHash.getString("TEMPLATE_LABEL")) + template;
				message(tempStr); // Display inherited from template

				tempStr = prepareString(resHash.getString("PARSER_LABEL")) + parser;
				message(tempStr); // Display parser connected to this
				// connector

				tempStr = prepareString(resHash.getString("COMMENT_LABEL")) + comment;
				message(tempStr); // Display "description" for the connector

			}

		} catch (Exception ex) {
			errorOccured = true;
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.GETTING.CONNECTOR.LIBRARY.DETAILS"), ex);
		}

		// Parser library

		try {
			folder = metamergeConfig.getDefaultFolder(MetamergeConfig.PARSER_FOLDER);
			components = folder.getNames();
			message("");
			message(resHash.getString("PARSER_LIBRARY_HEADING"));
			if (components.length <= 0) {
				message("");
				message(" " + resHash.getString("NONE"));
			}
			for (int i = 0; i < components.length; i++) {
				parserConfig = metamergeConfig.getParser(components[i]);

				if (parserConfig == null || parserConfig.getInheritsFromRef() == null)
					template = resHash.getString("NONE");
				else
					template = parserConfig.getInheritsFromRef();

				comment = parserConfig.getUserComment();
				if (comment == null || comment.trim().length() <= 0)
					comment = resHash.getString("NONE");

				message("");

				tempStr = prepareString(resHash.getString("NAME_LABEL")) + parserConfig.getName();
				message(tempStr); // Display PARSER name

				tempStr = prepareString(resHash.getString("TEMPLATE_LABEL")) + template;
				message(tempStr); // Display inherited from template

				tempStr = prepareString(resHash.getString("COMMENT_LABEL")) + comment;
				message(tempStr); // Display "description" for the parser

			}

		} catch (Exception ex) {
			errorOccured = true;
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.GETTING.PARSER.LIBRARY.DETAILS"), ex);
		}

		// Scripts

		ScriptConfig scriptConfig = null;
		try {
			folder = metamergeConfig.getDefaultFolder(MetamergeConfig.SCRIPT_FOLDER);
			components = folder.getNames();

			message("");
			message(resHash.getString("SCRIPT_LIBRARY_HEADING"));

			if (components.length <= 0) {
				message("");
				message(" " + resHash.getString("NONE"));
			}

			String autoIncludeStr = null;
			for (int i = 0; i < components.length; i++) {
				scriptConfig = metamergeConfig.getScript(components[i]);
				if (scriptConfig.getAutoInclude())
					autoIncludeStr = resHash.getString("SCRIPT_INCLUDE_AUTOMATICALLY");
				else
					autoIncludeStr = resHash.getString("SCRIPT_INCLUDE_MANUAL");

				message("");
				tempStr = prepareString(resHash.getString("NAME_LABEL")) + scriptConfig.getName();
				message(tempStr); // Display script name

				tempStr = prepareString(resHash.getString("SCRIPT_INCLUDE_LABEL")) + autoIncludeStr;
				message(tempStr); // Display script include mode (auto /
				// manual)

			}

		} catch (Exception ex) {
			errorOccured = true;
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.GETTING.SCRIPT.LIBRARY.DETAILS"), ex);
		}

		// Functions library
		FunctionConfig functionConfig = null;
		try {
			folder = metamergeConfig.getDefaultFolder(MetamergeConfig.FUNCTION_FOLDER);
			components = folder.getNames();

			message("");
			message(resHash.getString("FUNCTION_LIBRARY_HEADING"));

			if (components.length <= 0) {
				message("");
				message(" " + resHash.getString("NONE"));
			}

			for (int i = 0; i < components.length; i++) {
				functionConfig = metamergeConfig.getFunction(components[i]);

				if (functionConfig.getInheritsFromRef() == null)
					template = resHash.getString("NONE");
				else
					template = functionConfig.getInheritsFromRef();

				comment = functionConfig.getUserComment();
				if (comment == null || comment.trim().length() <= 0)
					comment = resHash.getString("NONE");

				message("");

				tempStr = prepareString(resHash.getString("NAME_LABEL")) + functionConfig.getName();
				message(tempStr); // Display FC name

				tempStr = prepareString(resHash.getString("TEMPLATE_LABEL")) + template;
				message(tempStr); // Display inherited from template

				tempStr = prepareString(resHash.getString("COMMENT_LABEL")) + comment;
				message(tempStr); // Display "description" for the FC

			}

		} catch (Exception ex) {
			errorOccured = true;
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.GETTING.FUNCTION.LIBRARY.DETAILS"), ex);
		}

		message("");
		message("------------------------------------------------------");
		message(resHash.getString("REPORT_GENERATED_DATE_TIME", new Date().toString()));
		message("------------------------------------------------------");

		if (errorOccured) // If some error occured.
		{
			retCode = RC_FAIL;
		}

		return retCode;
	}

	/**
	 * Displays the list of configs in the "config" folder of the remote server
	 * install directory.
	 *
	 * @return <code>RC_OK</code> if no error occured, otherwise
	 *         <code>RC_FAIL</code>.
	 *
	 * @throws IllegalCommandUsageException
	 */
	private static int displayConfigList() throws IllegalCommandUsageException {
		if (connect() == RC_FAIL)
			return RC_FAIL;

		// Check if the remote server version is 6.1 or above.
		// If not then show error and return - since the config folder
		// feature has been added in 6.1
		try {
			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.WHETHER.CONFIG.FOLDER.FEATURE.IS.SUPPORTED"));
			}

			String version = m_Session.getServerInfo().getServerVersion();
			version = version.substring(0, version.indexOf('-'));
			version = version.trim();

			if (VERBOSE_MODE) {
				message(resHash.getString("REMOTESERVERCOMMAND.SERVER.VERSION.4", version));
			}

			if (version.startsWith("6.0")) {
				message(resHash.getString("CONFIG_FOLDER_UNSUPPORTED", m_ServerHost));
				return RC_FAIL;
			}
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.DETERMINE.SERVER.VERSION.3"), ex);
			return RC_FAIL;
		}

		// Obtain the list of configs in the config folder
		ArrayList listOfAllConfigs = null;
		try {
			listOfAllConfigs = m_Session.listAllConfigurations();
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.UNABLE.TO.GET.LIST.OF.ALL.CONFIGS.2"), ex);
			return RC_FAIL;
		}

		if (listOfAllConfigs.size() == 0)
			message(resHash.getString("CONFIG_FOLDER_EMPTY", m_ServerHost + ":" + m_ServerPort));
		else {
			message("");
			message(resHash.getString("LIST_CONFIG_HEADING"));
			message("");
			for (int i = 0; i < listOfAllConfigs.size(); i++) {
				message(listOfAllConfigs.get(i).toString());
			}
			message("");
			message("---------------");
			message(resHash.getString("TOTAL_LABEL") + ": " + listOfAllConfigs.size());
			message("---------------");
		}
		return RC_OK;
	}

	private static int reloadConfigs(List<String> configList) {

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.RELOADCONFIGS"));
		}

		int retCode = RC_FAIL;

		if (connect() == RC_FAIL)
			return RC_FAIL;

		boolean errorOccured = false;

		// Obtain a list of running configs
		ConfigInstance[] configRunning = null;
		try {
			configRunning = m_Session.getConfigInstances();
		} catch (Exception ex) {
			log(ERROR, resHash.getString("REMOTESERVERCOMMAND.ERROR.WHILE.RETRIEVING.LIST.OF.LOADED.CONFIGS"), ex);
			errorOccured = true;
		}

		boolean found = false;
		String user_ConfigID = null;
		String origConfig = null;
		if (configRunning != null && configRunning.length > 0) {
			for (int i = 0; i < configList.size(); i++) // for each user
			// specified config
			{
				try {
					// Search for the appropriate running config instance.
					found = false;
					origConfig = configList.get(i);
					user_ConfigID = convertURLtoID(prefixIfRelativePath(origConfig));
					for (int j = 0; j < configRunning.length; j++) {
						if ((configRunning[j].getConfigId()).equals(user_ConfigID)) { // Found
																						// a
																						// match.
							// RELOAD it.
							configRunning[j].reload();
							found = true;
							message(resHash.getString("CONFIG_RELOADED", origConfig));
						} else if (configRunning[j].getConfigId().equals(origConfig)) {
							// this might be config with soln name
							configRunning[j].reload();
							found = true;
							message(resHash.getString("CONFIG_RELOADED", origConfig));
						}
					}
					if (!found) // Could not find it in the running list !
					{
						String funcmsg = resHash.getString("CONFIG_RELOAD_STOPPED", configList.get(i));
						log(ERROR, funcmsg);
						errorOccured = true;
						message(funcmsg);
					}
				} catch (Exception ex) // Exception while trying to reload.
				{
					log(ERROR, resHash.getString("CONFIG_RELOAD_ERR", m_ServerHost + ":" + m_ServerPort), ex);
					errorOccured = true;
				}
			}
		}

		if (errorOccured) // If some error occured.
		{
			retCode = RC_FAIL;
			message(resHash.getString("CONFIG_RELOAD_ERR", m_ServerHost + ":" + m_ServerPort));
		} else {
			retCode = RC_OK;
			message(resHash.getString("ALL_CONFIGS_RELOADED", m_ServerHost + ":" + m_ServerPort));
		}

		return retCode;
	}

	/**
	 * Parses the general options. After parsing, the general options and their
	 * corresponding values are set in the hashtable
	 * <code>m_GeneralOptions</code>. The number of arguments that have been
	 * processed and done with are set in <code>m_ArgumentsProcessed</code>.
	 * This is done so that the code which wishes to parse the later part of the
	 * code knows from which index of the array to continue from.
	 * <p>
	 * The parsing continues until it encounters the
	 * <code>OPERATION_SWITCH</code> option. Then it breaks out.
	 *
	 * @param args
	 *            The arguments passed to the program.
	 *
	 * @throws IllegalCommandUsageException
	 *             This exception is thrown whenever the method encounters an
	 *             option that it does not recognize, or an option that is
	 *             passed TWICE, or a parameter that was expected but not found
	 *             in the correct position, or the <code>OPERATION_SWITCH</code>
	 *             is NOT found in <code>args</code>.
	 *
	 * @see #m_GeneralOptions
	 * @see #m_ArgumentsProcessed
	 * @see #OPERATION_SWITCH
	 *
	 */

	private static void parseGeneralOptions(String args[]) throws IllegalCommandUsageException {
		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.PARSING.GENERAL.OPTIONS"));
		}
		String currentArgument;
		boolean operationSwitchFound = false; // Flag to indicate whether the
		// OPERATION_SWITCH was found.

		for (int i = 0; i < args.length; i++) {
			currentArgument = args[i];
			if (currentArgument.equals(GEN_OPT_SRV_HOST) || // Server host
					currentArgument.equals(GEN_OPT_SRV_PORT) || // Server port
					currentArgument.equals(GEN_OPT_USERID) || // Custom auth
					// user id
					currentArgument.equals(GEN_OPT_USERPWD) || // Custom auth
					// user pwd
					currentArgument.equals(GEN_OPT_KEYSTORE) || // Keystore
					currentArgument.equals(GEN_OPT_KEY_PWD) || // Key Password
					currentArgument.equals(GEN_OPT_TRUST_STORE) || // Trust
					// Store
					currentArgument.equals(GEN_OPT_TRUST_PWD) || // Trust
					// Password
					currentArgument.equals(GEN_OPT_SOL_DIR) // The location of
			// the solution dir.
			)

			{
				putInGeneralOptionsTable(currentArgument, args, i);
				i++;
			} else if (currentArgument.equals(GEN_OPT_VERBOSE)) // VERBOSE MODE
			{
				if (m_GeneralOptions.containsKey(GEN_OPT_VERBOSE)) {
					throw new IllegalCommandUsageException(resHash.getString("OPT_OCCUR_TWICE", GEN_OPT_VERBOSE));
				}
				m_GeneralOptions.put(currentArgument, "true");
				VERBOSE_MODE = true;
			} else if (currentArgument.equals(HELP_OPTION)) {
				b_SHOW_HELP = true;
				break;
			} else if (currentArgument.equals(OPERATION_SWITCH)) // Break
			// out. End
			// of
			// General
			// options.
			{
				m_ArgumentsProcessed = i; // Set to the location of
				// OPERATION_SWITCH and break out.
				operationSwitchFound = true;
				break;
			} else { // UNKNOWN GENERAL OPTION.
				throw new IllegalCommandUsageException(resHash.getString("UNKNOWN_OPT", currentArgument));
			}
		}

		if (operationSwitchFound == false && b_SHOW_HELP == false) {
			throw new IllegalCommandUsageException(resHash.getString("OP_SWITCH_ABSENT", OPERATION_SWITCH));
		}

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.GENERAL.OPTIONS", m_GeneralOptions.toString()));
		}
	}

	private static void putInGeneralOptionsTable(String generalOption, String args[], int currentCounter)
			throws IllegalCommandUsageException {
		if (m_GeneralOptions.containsKey(generalOption)) {
			throw new IllegalCommandUsageException(resHash.getString("OPT_OCCUR_TWICE", generalOption));
		}
		if (checkIfNextArgIsValue(args, currentCounter) == false) {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"));
		}
		String val = args[currentCounter + 1];
		m_GeneralOptions.put(generalOption, val);
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
			log(WARN, resHash.getString("REMOTESERVERCOMMAND.ALREADY.REACHED.THE.END.OF.ARGUMENT.LIST"));
			return false;
		}

		// Check if it is one of the options
		String nextValue = args[currentCounter + 1];

		if (nextValue.equals(GEN_OPT_KEY_PWD) || nextValue.equals(GEN_OPT_KEYSTORE) || nextValue.equals(GEN_OPT_SRV_HOST)
				|| nextValue.equals(GEN_OPT_SRV_PORT) || nextValue.equals(GEN_OPT_TRUST_PWD)
				|| nextValue.equals(GEN_OPT_TRUST_STORE) || nextValue.equals(GEN_OPT_USERID) || nextValue.equals(GEN_OPT_USERPWD)
				|| nextValue.equals(GEN_OPT_VERBOSE) || nextValue.equals(OPERATION_SWITCH)) {
			return false;
		}

		return true;
	}

	/**
	 * Based on the passed delimter, seperates the string into tokens and puts
	 * into an arraylist.Also trims() each of the strings for whitespaces. Will
	 * ignore duplicate tokens, and log the ignored tokens in the log file.
	 *
	 * @param string
	 *            The string to be tokenized.
	 * @param delimiter
	 *            The delimter.
	 * @return ArrayList of tokens.
	 */
	private static List<String> tokenizeToList(String string, String delimiter) {
		List<String> list = new ArrayList<String>();

		StringTokenizer st = new StringTokenizer(string, ",");

		String token = null;
		while (st.hasMoreTokens()) {
			token = st.nextToken().trim();
			if (list.contains(token)) { // D4557 Ignoring duplicates
				log(WARN, resHash.getString("REMOTESERVERCOMMAND.DUPLICATE.ARGUMENT", token));
			} else {
				list.add(token);
			}
		}

		return list;
	}

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

	private static void log(int level, String message) {
		log(level, message, null);
	}

	private static void log(int level, Exception e) {
		log(level, null, e);
	}

	private static void log(int level, String message, Exception e) {
		// If verbose mode is on, then print everything on console.
		if (VERBOSE_MODE) {
			if (message != null)
				message(message);
			if (e != null)
				message(e.toString());
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

	private static String getStackTrace(Throwable e) {
				StackTraceElement[] stElements = e.getStackTrace();
				StringBuffer trace = new StringBuffer();
				trace.append(e.getMessage());
				for (int i = 0; i < stElements.length; i++) {
					trace.append("\n\t").append(stElements[i].toString());
				}
				if (e.getCause() != null) {
					//trace.append("\n Caused by:");
					trace.append("\n Caused by: " + e.getCause().toString());
					trace.append(getStackTrace(e.getCause()));
				}
				return trace.toString();
	}

	/**
	 * Debugging method to print the arguments passed to the program.
	 *
	 * @param arguments
	 *            String array containing the arguments.
	 */
	private static void printArguments(String arguments[]) {
		for (int i = 0; i < arguments.length; i++) {
			logger.debug(resHash.getString("ARGUMENT_PASSED", new String[] { i + "", arguments[i] }));
		}
	}

	/**
	 * This method will print the command usage. The general option usage will
	 * be printed if the parameter "option" is not passed. If the paramter
	 * "option" has a particular value, then the help for that particular option
	 * will be printed.
	 *
	 * @param option
	 *            The -op "option" value whose help to be generated.
	 *
	 * @return The help usage String.
	 *
	 * @see #SHUTDOWN_OPER
	 * @see #SRVINFO_OPER
	 * @see #START_OPER
	 * @see #STATUS_OPER
	 * @see #STOP_OPER
	 * @see #TOMBSTONE_OPER
	 * @see #RELOAD_OPER
	 */
	private static String getCommandUsage(String option) {
		if (option == null) {
			return resHash.getString("GEN_CMD_USAGE");
		}
		if (option.equals(RELOAD_OPER)) {
			return resHash.getString("RELOAD_USAGE");
		}
		if (option.equals(SHUTDOWN_OPER)) {
			return resHash.getString("SHUTDOWN_USAGE");
		}
		if (option.equals(SRVINFO_OPER)) {
			return resHash.getString("SRVINFO_USAGE");
		}
		if (option.equals(STATUS_OPER)) {
			return resHash.getString("STATUS_USAGE");
		}
		if (option.equals(START_OPER)) {
			return resHash.getString("START_USAGE");
		}
		if (option.equals(DEBUG_OPER)) {
			return resHash.getString("DEBUG_USAGE");
		}
		if (option.equals(QUERY_OPER)) {
			return resHash.getString("QUERY_USAGE");
		}
		if (option.equals(STOP_OPER)) {
			return resHash.getString("STOP_USAGE");
		}
		if (option.equals(TOMBSTONE_OPER)) {
			return resHash.getString("TOMBSTONE_USAGE");
		}
		if (option.equals(DELETE_TOMBSTONE_OPER)) {
			return resHash.getString("DELETE_TOMBSTONE_USAGE");
		}
		if (option.equals(REPORT_OPER)) {
			return resHash.getString("REPORT_USAGE");
		}
		if (option.equals(EVENT_NOTIFIC_OPER)) {
			return resHash.getString("EVENT_NOTIFICATION_USAGE");
		}
		if (option.equals(PROP_OPER)) {
			return resHash.getString("CONFIG_PROPERTIES_USAGE");
		}
		return resHash.getString("GEN_CMD_USAGE");
	}

	/**
	 * Prepare string to be displayed in a visually aligned manner. This method
	 * pads <code>originalString</code> with spaces in the end, based on the
	 * <code>DISPLAY_LENGTH</code> value and formats it for the "report"
	 * operation.
	 * <p>
	 * This method will always return a string in the following format: <br>
	 * &quot; <code>originalString</code>[spaces]: &quot; <br>
	 * Where length of the spaces is ALWAYS equal to <code>DISPLAY_LENGTH -
	 * originalString.length</code>.
	 *
	 * @param originalString
	 *            The string to pad
	 *
	 * @return The string padded with requisite number of spaces.
	 *
	 */
	private static String prepareString(String originalString) {
		int padToRight = DISPLAY_LENGTH - originalString.length();
		if (padToRight > 0) {
			StringBuffer buff = new StringBuffer(" " + originalString);

			for (int i = 0; i < padToRight; i++) {
				buff.append(" ");
			}
			buff.append(": ");
			return buff.toString();
		}

		return originalString;
	}

	/**
	 * Prefixes the remote config folder path if the configPath is NOT absolute.
	 * <p>
	 * Note: If the -t option is specified we do not need to prefix the
	 * configPath because we are starting only Configs located on the client
	 * machine.
	 *
	 * @param configPath
	 * @return either the path passed as parameter or a path the is equal to
	 *         prefix + configPath
	 */
	private static String prefixIfRelativePath(String configPath) { // D5080
		String prefix = "";
		// No need to prefix. Will look in the current folder.
		if (startTemp) {
			return configPath;
		}
		try {
			prefix = m_Session.getConfigFolderPath();
			if (prefix == null || prefix.trim().length() <= 0) {
				// Remote server does not have a config folder.
				return configPath;
			}
			prefix = prefix.trim();
			char lastChar = prefix.charAt(prefix.length() - 1);
			char firstChar = prefix.charAt(0);
			if (lastChar != '\\' || lastChar != '/') {
				if (firstChar == '/' && prefix.indexOf(':') < 0) { // Unix
					prefix = prefix + "/";
				} else { // Windows
					prefix = prefix + "\\";
				}
			}
		} catch (Exception ex) {
			return configPath;
		}
		configPath = configPath.trim();
		if (configPath.charAt(1) != ':' && configPath.charAt(0) != '/') {
			// if not absolute path
			return prefix + configPath;
		}
		return configPath;
	}

	/**
	 * For the given ALConfig, gets the specified operations input attributes
	 *
	 * @param alConfig
	 *            The ALConfig to query
	 * @param operation
	 *            The operation whose parameters to find
	 * @return String Returns the attributes in the format {attr1;attr2}
	 */
	private static String getOperationAttributes(AssemblyLineConfig alConfig, String operation) {
		StringBuffer toReturn = new StringBuffer(" - ");
		OperationConfig oc = alConfig.getOperation(operation);
		if (oc != null) {
			SchemaConfig alschema = oc.getSchema(true);
			if (alschema != null) {
				List<String> names = alschema.getItemNames();
				if (names != null && names.size() > 0) { // Normal attributes
					// defined for an operation
					toReturn = new StringBuffer("{");
					for (int i = 0; i < names.size(); i++) {
						SchemaItemConfig item = alschema.getItem(names.get(i));
						toReturn.append(item.getAttributeName());
						toReturn.append(";");
					}
				} else { // Get from AttrMap (Need to understand when does
					// this code execute)
					// For $initialize code it definitely goes here
					names = oc.getAttributeMap(true).getAttributeNames();
					if (names != null && names.size() > 0) {
						toReturn = new StringBuffer("{");
						for (int i = 0; i < names.size(); i++) {
							toReturn.append(names.get(i));
							toReturn.append(";");
						}
					}
				}
			}
		}

		if (toReturn.indexOf(";") > 0) {
			return toReturn.substring(0, toReturn.length() - 1) + "}";
		}
		return toReturn.toString();
	}

	/**
	 * Listen for the messages logged by the AssemblyLine.
	 *
	 * @param al
	 *            Handle to an AssemblyLine.
	 *
	 * @param isSSLon
	 *            Whether SSL is used for the Server API session.
	 * @throws Exception
	 *             if listening for messages fails.
	 */
	private static void listenAssemblyLine(AssemblyLine al, boolean isSSLon) throws Exception {

		ALListenerSetup l = null;
		try {
			l = new ALListenerSetup(isSSLon);
			message("\n" + resHash.getString("LISTENING.AL"));
			al.addListener(l.getListener(), true, false);
			l.listen();
		} finally {
			if (l != null) {
				if (al != null) {
					al.removeListener(l.getListener());
				}
				l.close();
			}
		}
	}

	/**
	 * Listen for the messages logged by the ConfigInstance.
	 *
	 * @param ci
	 *            Handle to an ConfigInstance.
	 * @param isSSLon
	 *            Whether SSL is used for the Server API session.
	 * @throws Exception
	 *             if listening for messages fails.
	 */
	private static void listenConfigInstance(ConfigInstance ci, boolean isSSLon) throws Exception {
		CIListenerSetup l = null;
		try {
			l = new CIListenerSetup(isSSLon);
			message("\n" + resHash.getString("LISTENING.CI"));
			ci.addLogListener(l.getListener());
			l.listen(ci.getConfigId());
		} finally {
			if (l != null) {
				if (ci != null) {
					ci.removeLogListener(l.getListener());
				}
				l.close();
			}
		}
	}

	/**
	 * Verify that a single configuration is specified.
	 *
	 * @param operation
	 *            The name of the operation, specified by the user (the value of
	 *            the '-op' option).
	 * @throws IllegalCommandUsageException
	 *             If the specified configuration is not exactly one.
	 */
	private static void verifySingleConfig(String operation) throws IllegalCommandUsageException {
		if (m_userExecutionChoice.indexOf('C') < 0 || (m_configList == null)) {
			throw new IllegalCommandUsageException(resHash.getString("OPT_MANDATORY", CONFIG_OPTION), operation);
		}

		if (b_AllConfigs == true) {
			throw new IllegalCommandUsageException(resHash.getString("CONFIG_ALL_NOT_ALLOWED", ALL), operation);
		}

		if (m_configList.size() > 1) {
			throw new IllegalCommandUsageException(resHash.getString("ONE_CONFIG_ALLOWED"), operation);
		}
	}

	/**
	 * Verify that a single AssemblyLine is specified.
	 *
	 * @param operation
	 *            The name of the operation, specified by the user (the value of
	 *            the '-op' option).
	 * @throws IllegalCommandUsageException
	 *             If the specified AssemblyLine is not exactly one.
	 */
	private static void verifySingleAL(String operation) throws IllegalCommandUsageException {

		if (b_AllAssembly == true) {
			throw new IllegalCommandUsageException(resHash.getString("ASSEMBLY_LINE_ALL_NOT_ALLOWED", ALL), operation);
		}

		if (m_assemblyLineList != null && m_assemblyLineList.size() > 1) {
			throw new IllegalCommandUsageException(resHash.getString("ONE_AL_ALLOWED"), operation);
		}
	}

	/**
	 * Verify that the specified operation is supported for the specified
	 * option.
	 *
	 * @param operation
	 *            The name of the operation, specified by the user (the value of
	 *            the '-op' option).
	 * @param option
	 *            The name of an option, e.g. {@link CLIConstants#CONFIG_OPTION}
	 *            .
	 * @param allowedOperations
	 *            The names of operations, which support the option.
	 * @throws IllegalCommandUsageException
	 *             If the operation is not compatible with the option.
	 */
	private static void verifyOperationForOption(String operation, String option, String[] allowedOperations)
			throws IllegalCommandUsageException {

		boolean operationAllowed = false;
		for (String allowedOp : allowedOperations) {
			if (allowedOp.equals(operation)) {
				operationAllowed = true;
				break;
			}
		}

		if (!operationAllowed) {
			throw new IllegalCommandUsageException(resHash.getString("OPTION_NOT_ALLOWED_FOR_OPERATION", new Object[] { option,
					operation }), operation);
		}
	}

	/**
	 * Listener for messages logged by an AssemblyLine.
	 *
	 * @since 7.0
	 */
	private static class ALListener implements AssemblyLineListener {

		private boolean alFinished = false;

		/**
		 * {@inheritDoc}
		 */
		public void assemblyLineCycleDone(Entry entry) throws DIException, RemoteException {
		}

		/**
		 * {@inheritDoc}
		 */
		public synchronized void assemblyLineFinished() throws DIException, RemoteException {
			alFinished = true;
			notify();
		}

		/**
		 * {@inheritDoc}
		 */
		public void messageLogged(String msg) throws DIException, RemoteException {
			message(msg);
		}

		synchronized void waitForALToFinish() throws InterruptedException {
			while (!alFinished) {
				wait();
			}
		}
	}

	/**
	 * Listener for messages logged by Config Instance.
	 *
	 * @since 7.0
	 */
	private static class CIListener implements LogListener {

		/**
		 * Flag set when the ConfigInstance is stopped.
		 */
		private boolean ciClosed = false;

		/**
		 * Holds the wrapped event listener.
		 */
		DIEventListener baseDIListener;

		/**
		 * Class implementing the DIEventListener used to register event
		 * listener.
		 */
		private class DIListener implements DIEventListener {
			/**
			 * {@inheritDoc}
			 */
			public void handleEvent(DIEvent aEvent) throws DIException, RemoteException {
				notifyForCIClosed();
			}
		}

		/**
		 * Notifies the waitForCIToClose() method to stop waiting.
		 */
		synchronized void notifyForCIClosed() {
			ciClosed = true;
			notify();
		}

		/**
		 * {@inheritDoc}
		 */
		public void messageLogged(String msg) throws DIException, RemoteException {
			message(msg);
		}

		/**
		 * Waits untill the ConfigInstance is closed.
		 *
		 * @throws InterruptedException
		 *             if the wait is interrupted
		 */
		synchronized void waitForCIToClose() throws InterruptedException {
			while (!ciClosed) {
				wait();
			}
		}

		/**
		 * Unregisters DIEventListener with the current session and unexport the
		 * remote object from the RMI runtime.
		 *
		 * @throws DIException
		 *             if an error occurs while unregistering the listener.
		 * @throws RemoteException
		 *             if a communication-related exception occurs.
		 */
		public void close() throws DIException, RemoteException {
			m_Session.removeEventListener(baseDIListener);
			if (baseDIListener != null) {
				UnicastRemoteObject.unexportObject(baseDIListener, true);
			}
		}

		/**
		 * Registers an event listener with the current session. The listener
		 * will listen for {@link DIEvent#EVT_CI_STOP} event of a specified by
		 * <code>configId</code> ConfigInstance.
		 *
		 * @param configId
		 *            the config instance ID
		 * @throws Exception
		 *             if Runtime or Security exception occurs
		 */
		public void addDIListener(String configId) throws Exception {
			boolean isSSLon = m_Session.isSSLon();
			DIListener diListener = new DIListener();

			baseDIListener = DIEventListenerBase.createInstance(diListener, isSSLon);
			m_Session.addEventListener(baseDIListener, DIEvent.EVT_CI_STOP, configId);
		}
	}

	/**
	 * Utility class for working with AssemblyLine listener.
	 *
	 * @since 7.0
	 */
	private static class ALListenerSetup {

		private ALListener localListener;
		private AssemblyLineListener rmiExportedListener;

		/**
		 * Prepare the listener.
		 *
		 * @param isSSLon
		 *            Whether SSL is used for the Server API session.
		 *
		 * @throws Exception
		 *             If the listener cannot be exported in RMI.
		 */
		ALListenerSetup(boolean isSSLon) throws Exception {
			localListener = new ALListener();
			rmiExportedListener = AssemblyLineListenerBase.createInstance(localListener, isSSLon);
		}

		/**
		 * @return AssemblyLine listener as an RMI callback.
		 */
		AssemblyLineListener getListener() {
			return rmiExportedListener;
		}

		/**
		 * Wait for the AssemblyLine, in which the listener is registered, to
		 * finish. You must register the listener obtained by
		 * {@link #getListener()} in an AssemblyLine before calling this method.
		 * Otherwise it will hang forever.
		 *
		 * @throws InterruptedException
		 *             If the wait is interrupted.
		 */
		void listen() throws InterruptedException {
			localListener.waitForALToFinish();
		}

		/**
		 * Cleanup the listener.
		 */
		void close() {
			try {
				if (rmiExportedListener != null) {
					UnicastRemoteObject.unexportObject(rmiExportedListener, true);
				}
			} catch (Exception ex) {
				log(WARN, ex.getMessage(), ex);
			}
			/*
			 * No need to worry about unexporting the RMI server object created
			 * for the listener, because all RMI threads will die together with
			 * the whole JVM.
			 */
		}
	}

	/**
	 * Utility class for working with ConfigInstance listener.
	 *
	 * @since 7.0
	 */
	private static class CIListenerSetup {

		private CIListener localListener;
		private LogListener rmiExportedListener;

		/**
		 * Prepare the listener.
		 *
		 * @param isSSLon
		 *            Whether SSL is used for the current Server API session.
		 *
		 * @throws Exception
		 *             If the listener cannot be exported in RMI.
		 */
		CIListenerSetup(boolean isSSLon) throws Exception {
			localListener = new CIListener();
			rmiExportedListener = LogListenerBase.createInstance(localListener, isSSLon);
		}

		/**
		 * @return Log listener as an RMI callback.
		 */
		LogListener getListener() {
			return rmiExportedListener;
		}

		/**
		 * Wait for the ConfigInstance, in which the listener is registered, to
		 * close. You must register the listener obtained by
		 * {@link #getListener()} in an ConfigInstance before calling this
		 * method. Otherwise it will hang forever.
		 *
		 * @param configId
		 *            the config instance ID
		 * @throws Exception
		 *
		 * @throws Exception
		 *             <li>If the wait is interrupted.</li> <li>If a
		 *             communication-related exception occurs.</li> <li>If could
		 *             not complete the operation successfully.</li>
		 */
		void listen(String configId) throws Exception {
			// Start listener for a particular ConfigInstance stop event
			localListener.addDIListener(configId);
			localListener.waitForCIToClose();
		}

		/**
		 * Cleanup the listener.
		 */
		void close() {
			try {
				localListener.close();
				if (rmiExportedListener != null) {
					UnicastRemoteObject.unexportObject(rmiExportedListener, true);
				}
			} catch (Exception ex) {
				log(WARN, ex.getMessage(), ex);
			}
			/*
			 * No need to worry about unexporting the RMI server object created
			 * for the listener, because all RMI threads will die together with
			 * the whole JVM.
			 */
		}
	}

	/**
	 * Start AssemblyLines according to the command-line options.
	 *
	 * @param configInstance
	 *            The configuration instance associated with the AssemblyLine.
	 * @return {@link CLIConstants#RC_OK} or {@link CLIConstants#RC_FAIL}
	 */
	private static int startAssemblyLines(ConfigInstance configInstance) {

		int retCode = RC_OK;
		try {
			// First show user the list of assembly lines which are already
			// running.
			AssemblyLine[] runningALs = configInstance.getAssemblyLines();
			List<String> runningAL_list = new ArrayList<String>();
			int cut = ASSEMBLY_LINE_FOLDER_PREFIX.length();
			String shortname;

			for (int i = 0; i < runningALs.length; i++) {
				shortname = runningALs[i].getName().substring(cut);
				runningAL_list.add(shortname);
				message(resHash.getString("AL_ALREADY_RUNNING", shortname));
			}

			// Get list of all assemblies in this config.
			String[] listOfALs = configInstance.getAssemblyLineNames();

			List<String> alNamesToStart;
			if (b_AllAssembly) {
				// user wishes to start all assembly lines
				alNamesToStart = Arrays.asList(listOfALs);
			} else {
				// user has specified a list of assembly lines to start
				alNamesToStart = m_assemblyLineList;
			}

			// do not start those that are already running
			alNamesToStart.removeAll(Arrays.asList(runningALs));

			for (String assemblyName : alNamesToStart) {

				// Check if this is a valid assembly line name.
				if (contains(listOfALs, assemblyName)) {

					TaskCallBlock tcb = null;
					if (execAlOp && userTCB != null) {
						tcb = userTCB;
					} else if (m_SimulateMode) {
						tcb = simulateMode;
					}

					if (listen) {
						executeAssemblyLineWithListener(configInstance, assemblyName, tcb, m_Session.isSSLon());
					} else if (sync) {
						boolean alSuccess = executeAssemblyLine(configInstance, assemblyName, tcb);
						if (!alSuccess) {
							retCode = RC_FAIL;
						}
					} else {
						startAssemblyLine(configInstance, assemblyName, tcb);
					}

				} else {
					// Not found. Invalid assembly line name.
					message(resHash.getString("AL_NOT_FOUND", assemblyName));
					retCode = RC_FAIL;
				}
			}

		} catch (Exception ex) {
			log(ERROR, resHash.getString("ALS_NOT_STARTED", m_ServerHost + ":" + m_ServerPort), ex);
			message(resHash.getString("ALS_NOT_STARTED", m_ServerHost + ":" + m_ServerPort));
			retCode = RC_FAIL;
		}

		return retCode;
	}

	/**
	 * Execute an AssemblyLine synchronously and receive its logged messages.
	 *
	 * @param ci
	 *            Configuration instance, from which is the AssemblyLine.
	 * @param alName
	 *            AssemblyLine name.
	 * @param tcb
	 *            Task call block to pass to the AssemblyLine when starting.
	 * @param isSSLon
	 *            Whether SSL is used for the Server API session.
	 * @throws Exception
	 *             If the AssemblyLine cannot be started or the listening for
	 *             messages fails.
	 */
	private static void executeAssemblyLineWithListener(ConfigInstance ci, String alName, TaskCallBlock tcb, boolean isSSLon)
			throws Exception {

		AssemblyLine al = null;
		ALListenerSetup l = null;
		try {
			l = new ALListenerSetup(isSSLon);
			al = ci.startAssemblyLine(alName, tcb, l.getListener(), true);

			message(resHash.getString("AL_STARTED", alName));
			message(resHash.getString("LISTENING.AL"));

			l.listen();
		} finally {
			if (l != null) {
//				if (al != null) { -listen throws a error because of this code 
//					al.removeListener(l.getListener());
//				}
				l.close();
			}
		}
	}

	/**
	 * Start an AssemblyLine.
	 *
	 * @param ci
	 *            Configuration instance, from which is the AssemblyLine.
	 * @param alName
	 *            AssemblyLine name.
	 * @param tcb
	 *            Task call block to pass to the AssemblyLine when starting.
	 * @throws Exception
	 *             If the AssemblyLine cannot be started or the listening for
	 *             messages fails.
	 */
	private static void startAssemblyLine(ConfigInstance ci, String alName, TaskCallBlock tcb) throws Exception {

		if (tcb != null) {
			ci.startAssemblyLine(alName, tcb);
		} else {
			ci.startAssemblyLine(alName);
		}
		message(resHash.getString("AL_STARTED", alName));
	}

	/**
	 * Execute an AssemblyLine synchronously.
	 *
	 * @param ci
	 *            Configuration instance, from which is the AssemblyLine.
	 * @param alName
	 *            AssemblyLine name.
	 * @param tcb
	 *            Task call block to pass to the AssemblyLine when starting.
	 * @return Whether the AssemblyLine completed successfully or failed with an
	 *         exception.
	 * @throws Exception
	 *             If the AssemblyLine cannot be started.
	 */
	private static boolean executeAssemblyLine(ConfigInstance ci, String alName, TaskCallBlock tcb) throws Exception {

		AssemblyLine al;
		if (tcb != null) {
			al = ci.startAssemblyLine(alName, tcb, true);
		} else {
			al = ci.startAssemblyLine(alName, true);
		}

		TaskStatistics stats = al.getStatistics();
		if (stats.getError() == null) {
			message(resHash.getString("AL.COMPLETE", new Object[] {alName, stats}));

			try {
				Entry e = al.getResult();
				message(resHash.getString("AL.LAST.ENTRY", e));
			} catch (Exception ex) {
				message(resHash.getString("AL.LAST.ENTRY.ERROR", ex));
			}
		} else {
			message(resHash.getString("AL.ERROR", new Object[] {alName, stats.getError(), stats}));
		}

		return stats.getError() == null;
	}

	/**
	 * Operation "debug" for AssemblyLine components.
	 * <p>
	 * Note: The <code>m_ArgumentsProcessed</code> should be set to the point
	 * from where to continue reading the rest of the parameters.
	 *
	 * @param args
	 *            The Command Line parameters.
	 *
	 * @return <code>RC_OK</code> if there were no errors, otherwise
	 *         <code>RC_FAIL</code>
	 */
	private static int execDebug(String[] args) throws IllegalCommandUsageException {
		int retCode = RC_FAIL;
		m_ArgumentsProcessed++;

		int noOfArgumentsRemaining = args.length - m_ArgumentsProcessed;

		if (noOfArgumentsRemaining == 1) { // Must be "-?" else ERROR
			if (args[m_ArgumentsProcessed].equals(HELP_OPTION)) {
				message(getCommandUsage(DEBUG_OPER));
				return RC_OK;
			} else {
				throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), DEBUG_OPER);
			}
		} else if (noOfArgumentsRemaining == 5 || noOfArgumentsRemaining == 7) {
			// Must be "-c config -r al_name -on/off" or
			// "-c config -r al_name -alc al_comp -on/off" else ERROR
			// Parse the arguments.
			parseCAE(args, DEBUG_OPER);

			// All argments have been parsed. Now execute the DEBUG operation.
			retCode = debugComponents();
		} else {
			throw new IllegalCommandUsageException(resHash.getString("ILLEGAL_COMMAND_USAGE"), DEBUG_OPER);
		}
		return retCode;
	}

	/**
	 * Sets Debug mode for user specified components of a runnig AssemblyLine.
	 * <p>
	 * This method makes use of the following notable member variables:
	 * <p>
	 * <code>isDebugOn</code><br>
	 * <code>true</code> indicates that the Debug mode should be enabled.
	 * <p>
	 * <code>m_componentList</code><br>
	 * The list of components to be affected by the debug operation. This will
	 * be ignored if <code>b_AllComponents</code> is <code>true</code>.
	 * <p>
	 * <code>b_AllComponents</code><br>
	 * <code>true</code> indicates that all components should be affected by the
	 * debug operation.
	 * <p>
	 *
	 * @return <code>RC_OK</code> if all is OK, else <code>RC_FAIL</code>.
	 *
	 * @see #parseCAE(String[], String)
	 *
	 */
	private static int debugComponents() {

		if (VERBOSE_MODE) {
			message(resHash.getString("REMOTESERVERCOMMAND.IN.DEBUGCOMPONENTS"));
		}

		if (connect() == RC_FAIL) {
			return RC_FAIL;
		}
		message("");
		message(resHash.getString("CONNECTED_SRV", m_ServerHost + ":" + m_ServerPort));

		// Get the config instance
		try {
			String origUrl = m_configList.get(0);
			ConfigInstance configInstance = getConfigInstance(origUrl, m_Session);

			String alName = m_assemblyLineList.get(0);
			AssemblyLine al = getRunningAssemblyLine(configInstance, alName);

			if (al == null) {
				throw new Exception(resHash.getString("ASSEMBLY_LINE_NOT_RUNNING", alName));
			}

			AssemblyLineConfig alc = al.getConfig();
			int count = alc.getConnectorCount();
			String compName = null;

			if (b_AllComponents) {
				// iterate trough all components
				for (int i = 0; i < count; i++) {
					compName = alc.getConnector(i).getShortName();
					al.setComponentDebugMode(compName, isDebugOn);
				}
			} else if (m_componentList != null) {
				verifyComponentsExist(alc, m_componentList);
				for (int i = 0; i < m_componentList.size(); i++) {
					compName = m_componentList.get(i);
					al.setComponentDebugMode(compName, isDebugOn);
				}
			}
		} catch (Exception ex) {
			log(ERROR, ex.getMessage(), ex);
			message(ex.getMessage());
			return RC_FAIL;
		}
		return RC_OK;
	}

	/**
	 * Verifies that all components in <code>m_componentList</code> exist in the
	 * specified AssemblyLine.
	 *
	 * @param alc
	 *            configuration of the specified AssemblyLine
	 * @throws Exception
	 *             if not all components exist in the assembly line
	 */
	private static void verifyComponentsExist(AssemblyLineConfig alc, List<String> compList) throws Exception {
		BaseConfiguration bc = null;
		String compName = null;

		for (int i = 0; i < compList.size(); i++) {
			compName = compList.get(i);
			bc = alc.getComponent(compName);
			if (bc == null) {
				// such component does not exist
				String errorMsg = resHash.getString("COMPONENT_COULD_NOT_SET_DEBUG_MODE", compName);
				throw new Exception(errorMsg);
			}
		}
	}

	/**
	 * @param configURL
	 *            Config specified by the user
	 * @return ConfigInstance object if the config is running; <code>null</code>
	 *         otherwise;
	 * @throws Exception
	 *             if could not load specified Config
	 */
	private static ConfigInstance getConfigInstance(String configURL, Session session) throws Exception {

		ConfigInstance configInstance = null;
		String origUrl = configURL;
		configURL = prefixIfRelativePath(configURL);
		String configID = convertURLtoID(configURL);

		try {
			configInstance = session.getConfigInstance(configID);

			if (configInstance == null) {

				// Check if the Solution Name has been passed .
				configInstance = session.getConfigInstance(origUrl);

				if (configInstance == null) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			String errorMsg = resHash.getString("CONFIG_NOT_LOADED_ERR",
					new String[] { origUrl, m_ServerHost + ":" + m_ServerPort });
			throw new Exception(errorMsg);
		}
		return configInstance;
	}

	/**
	 * If the <code>ALName</code> assembly line from the
	 * <code>configInstance</code> is running its AssemblyLine object is
	 * returned; otherwise null is returned.
	 *
	 * @param ALName
	 *            name of the AssemblyLine
	 * @return AssemblyLine object for a running assembly line
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLines or
	 *             retrieving the name of the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private static AssemblyLine getRunningAssemblyLine(ConfigInstance configInstance, String ALName) throws DIException,
			RemoteException {
		AssemblyLine[] runningALs = m_Session.getAssemblyLines();
		String runningALName = null;

		for (int i = 0; i < runningALs.length; i++) {
			runningALName = runningALs[i].getName();

			if (runningALName.lastIndexOf("/") != -1) {
				runningALName = runningALName.substring(runningALName.lastIndexOf("/") + 1);
			}

			if (runningALName.equals(ALName)
					&& runningALs[i].getConfigInstance().getConfigId().equals(configInstance.getConfigId())) {
				return runningALs[i];
			}
		}
		return null;
	}

	/**
	 * This method returns the Debug parameter for a specified connector or
	 * function component.
	 *
	 * @param cc
	 *            ConnectorConfig object
	 * @return the value of the Debug parameter as specified in the
	 *         configuration of the component
	 */
	private static boolean getConfigDebugMode(ConnectorConfig cc) {
		if (cc instanceof FunctionConfig) {
			FunctionConfig fc = (FunctionConfig) cc;
			return fc.getFunctionConfig().getDebug(false);
		} else {
			return cc.getConnectionConfig().getDebug(false);
		}
	}
}
