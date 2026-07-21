/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cli;
/**
 * CLI Constant values
 * 
 */
public class CLIConstants {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public static final String AL_TYPE = "1";
	
	public static final String CONFIG_TYPE = "0";

	public static final String DELIMITER = " # ";

	public static final int ERROR = 4;

	public static final int WARN = 3;

	public static final int INFO = 2;

	public static final int DEBUG = 1;

	static boolean VERBOSE_MODE = false;
	/** Remote server hostname option */
	public static final String GEN_OPT_SRV_HOST = "-h";

	/** Remote server (rmi) port option */
	public static final String GEN_OPT_SRV_PORT = "-p";

	/** SSL key store option */
	public static final String GEN_OPT_KEYSTORE = "-K";

	/** SSL Keystore password option */
	public static final String GEN_OPT_KEY_PWD = "-P";

	/** SSL Trust Store option */
	public static final String GEN_OPT_TRUST_STORE = "-T";

	/** SSL Trust Store Password */
	public static final String GEN_OPT_TRUST_PWD = "-W";

	/** Verbose mode */
	public static final String GEN_OPT_VERBOSE = "-v";

	/** User id for custom auth option */
	public static final String GEN_OPT_USERID = "-u";

	/** User password for cust auth option */
	public static final String GEN_OPT_USERPWD = "-w";

	/**
	 * Option to specify the working directory which is the solution directory.
	 * In place to load the proper logging file.
	 */
	public static final String GEN_OPT_SOL_DIR = "-s";

	// "-op" switch
	public static final String OPERATION_SWITCH = "-op";

	/** Switch for which option to execute */
	// The available functionalities / operations
	/** Event operation */
	public static final String EVENT_NOTIFIC_OPER = "event";

	/** TDI Properties operation */
	public static final String PROP_OPER = "prop";

	/** Reload operation */
	public static final String RELOAD_OPER = "reload";

	/** Shutdown operation */
	public static final String SHUTDOWN_OPER = "shutdown";

	/** Server information operation */
	public static final String SRVINFO_OPER = "srvinfo";

	/** Viewing status of different components */
	public static final String STATUS_OPER = "status";

	/** Starting configs/ALs */
	public static final String START_OPER = "start";
	
	/** Debuging components of running ALs */
	public static final String DEBUG_OPER = "debug";
	
	/** Starting configs/ALs */
	public static final String QUERY_OPER = "queryop";

	/** Stopping configs/ALs */
	public static final String STOP_OPER = "stop";

	/** Option for viewing tombstones of stopped configs/ALs */
	public static final String TOMBSTONE_OPER = "tombstone";

	/** Option for deleting tombstones */
	public static final String DELETE_TOMBSTONE_OPER = "deletetombstone";
	
	/** Option for viewing config reports */
	public static final String REPORT_OPER = "report";

	public static final String HELP_OPTION = "-?";

	public static final String ALL = "all";

	// The environment variables
	public static final String ENV_SRV_HOST = "TDI_RSRV";

	public static final String ENV_SRV_PORT = "TDI_RPORT";

	// The default values
	public static final String DEFAULT_SERVER = "localhost";

	public static final String DEFAULT_PORT = "1099";
	public static final int RC_OK = 0;

	public static final int RC_FAIL = -1;
	public static final String ASSEMBLY_LINE_FOLDER_PREFIX = "AssemblyLines/";

	public static final String CONFIG_OPTION = "-c";

	public static final String ASSEMBLY_LINE_OPTION = "-r";

	public static final String AL_OP = "-alop";

	public static final String CONFIG_ENCRYPTED_OPTION = "-e";

	/** To start a assembly line in simulate mode */
	public static final String SIMULATE_MODE = "-s";
	// EVENT NOTIFICATION RELATED OPTIONS
	public static final String EVENT_NAME_OPTION = "-e";

	public static final String EVENT_SOURCE_OPTION = "-s";

	public static final String EVENT_DATA_OPTION = "-d";

	// EVENT NOTIFICATION RELATED DATASTRUCTURES
	public static final String DEFAULT_EVENT_SOURCE = "tdisrvctl";// Default
	// TOMBSTONE RELATED OPTIONS
	public static final String AGE_OPTION = "-age";

	public static final String TS_COMP_TYPE = "-ct";

	public static final String TS_EVENT_TYPE = "-et";

	public static final String TS_START_TIME = "-stime";

	public static final String TS_CREATE_TIME = "-ctime";

	public static final String TS_COMP_NAME = "-cn";

	public static final String TS_EXIT_CODE = "-ex";

	public static final String TS_ERR_DESC = "-desc";

	public static final String TS_STATS = "-stat";

	public static final String TS_USERMESSAGE = "-um";

	public static final String TS_GUID = "-guid";

	public static final String TS_CONFIG = "-cfg";
	// TDIp PROPERTIES RELATED OPTIONS
	public static final String LIST_PROP_STORES_OPTION = "-l";

	public static final String PROP_STORE_OPTION = "-o";

	public static final String GET_PROP_OPTION = "-g";

	public static final String SET_PROP_OPTION = "-s";

	public static final String DEL_PROP_OPTION = "-d";

	public static final String ENCRYPT_PROP_OPTION = "-e";
	
	public static final int LIST_STORE = 0;

	public static final int GET_VALUE = 1;

	public static final int SET_VALUE = 2;

	public static final int DEL_VALUE = 3;

	public static final String LIST_OPTION = "-l";

	public static final String RETCODE_OPTION = "-o";

	public static final String PROTECTED = "{encr}";

	public static final String LOAD_WITH_RUN_NAME = "-m";
	
	/**
	 * To start a temporary config instance
	 */
	public static final String CONFIG_TEMP_OPTION = "-t";

	public static final String PROP_FILE_OPTION = "-o";
	
	/**
	 * To listen for messages logged by an AssemblyLine.
	 */
	public static final String LISTEN_OPTION = "-listen";
	
	/**
	 * To execute an AssemblyLine synchronously.
	 */
	public static final String SYNC_OPTION = "-sync";
	
	/**
	 * Specifies AL components list.
	 */
	public static final String ALC_OPTION = "-alc";
	
	/**
	 * Set Debug enabled for specified AL components.
	 */
	public static final String ON_OPTION = "-on";

	/**
	 * Set Debug disabled for specified AL components.
	 */
	public static final String OFF_OPTION = "-off";

	/**
	 * Force a controlled stop or shutdown.
	 */
	public static final String FORCE_CONTROLLED_OPTION = "-f";

	/**
	 * This variable is used in the 'report' option to decide where the ":" in
	 * the report will get aligned. This is basically helpful in alignment of
	 * the report. For instance:<br>
	 * <--- DISPLAY_LENGTH ---><br>
	 * Name : Config.xml<br>
	 * Comment : This is a comment<br>
	 * <br>
	 * Since in diff langs the length of the labels "Name", "Comment", etc is
	 * not fixed, this value is used to decide how much padding to do at end of
	 * each word, so that the colons align them selves nicely. Formatting stuff
	 * basically. Could consider setting this value dynamically if on some
	 * locales the display length of 15 is also too less.
	 */
	public static final int DISPLAY_LENGTH = 15;

}
