/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.filetransferfc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.protocols.rxa.LogProxy;
import com.ibm.di.protocols.rxa.LogProxyImpl;
import com.ibm.di.protocols.rxa.ParamException;
import com.ibm.di.protocols.rxa.RemoteConnectException;
import com.ibm.di.protocols.rxa.TDIRXALogAdapter;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.tivoli.remoteaccess.BaseProtocol;
import com.ibm.tivoli.remoteaccess.log.Level;
import com.ibm.tivoli.remoteaccess.log.Logger;

/**
 * <p>
 * TDI File Transfer Function Component.
 * </p>
 * 
 * <p>
 * This function component (FC) provides the ability to transfer a specified
 * file to a target machine.
 * </p>
 * 
 * <p>
 * This FC establishes connections and transfers file to target machines using
 * the IBM Remote Execution and Access toolkit or existing FTPClient APIs
 * (com.ibm.di.protocols.FTPClient). To use this function component you must
 * have the File Transfer FC (with its included jar files) installed correctly
 * on your local machine. The target machine you wish to connect and transfer
 * file to must have at least one of the following connection protocols
 * configured and running:
 * <ul>
 * <li>FTP
 * <li>RSH
 * <li>REXEC
 * <li>SSH
 * <li>A windows connection protocol such as SMB,CIFS,DCE-RPC
 * </ul>
 * </p>
 * 
 * <p>
 * Configuration is accomplished by setting logon parameters for client
 * connections to the specified machine (target) where the file needs to be
 * transferred. See {@link #initialize} for more details on how to initialize.
 * </p>
 * 
 * <p>
 * {@link #initialize} must be the first operation called in this class.<br>
 * {@link #perform} can then be called one or more times.<br>
 * {@link #terminate} must be called to allow connection cleanup before the
 * class is destroyed.
 * </p>
 */

public class FileTransferFC extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties
	 */
	private static final String PROPERTIES_FILE = "filetransferfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * The parameter for Source Connection Protocol
	 */
	public static final String PARAM_CONFIG_SOURCE_PROTOCOL = "sourceProtocol";

	/**
	 * The parameter for Source File Path
	 */
	public static final String PARAM_CONFIG_SOURCE_PATH = "sourcePath";

	/**
	 * The parameter for Source Hostname
	 */
	public static final String PARAM_CONFIG_SOURCE_HOST = "sourceHostname";

	/**
	 * The parameter for Source Port
	 */
	public static final String PARAM_CONFIG_SOURCE_PORT = "sourcePort";

	/**
	 * The parameter for the Source Username
	 */
	private static final String PARAM_CONFIG_SOURCE_USERNAME = "sourceUsername";

	/**
	 * The parameter for the Source Password
	 */
	private static final String PARAM_CONFIG_SOURCE_PASSWORD = "sourcePassword";

	/**
	 * The parameter for the Source Keystore
	 */
	private static final String PARAM_CONFIG_SOURCE_KEYSTORE = "sourceKeystore";

	/**
	 * The parameter for the Source Passphrase
	 */
	private static final String PARAM_CONFIG_SOURCE_PASSPHRASE = "sourcePassphrase";

	/**
	 * The parameter for Target Connection Protocol
	 */
	public static final String PARAM_CONFIG_TARGET_PROTOCOL = "targetProtocol";

	/**
	 * The parameter for Target File Path
	 */
	public static final String PARAM_CONFIG_TARGET_PATH = "targetPath";

	/**
	 * The parameter for Target Hostname
	 */
	public static final String PARAM_CONFIG_TARGET_HOST = "targetHostname";

	/**
	 * The parameter for Target Port
	 */
	public static final String PARAM_CONFIG_TARGET_PORT = "targetPort";

	/**
	 * The parameter for the Target Username
	 */
	private static final String PARAM_CONFIG_TARGET_USERNAME = "targetUsername";

	/**
	 * The parameter for the Target Password
	 */
	private static final String PARAM_CONFIG_TARGET_PASSWORD = "targetPassword";

	/**
	 * The parameter for the Target Keystore
	 */
	private static final String PARAM_CONFIG_TARGET_KEYSTORE = "targetKeystore";

	/**
	 * The parameter for the Target Passphrase
	 */
	private static final String PARAM_CONFIG_TARGET_PASSPHRASE = "targetPassphrase";

	/**
	 * The parameter for creating Target File Path
	 */
	private static final String PARAM_CONFIG_CREATE_TARGET_PATH = "createTargetPath";
	/**
	 * The parameter for the temporary file path
	 */
	private static final String PARAM_CONFIG_TEMP_DIRECTORY = "tempDirectory";

	/**
	 * The parameter for the temporary file path
	 */
	private static final String PARAM_CONFIG_DELETE_TEMP = "deleteTempFile";

	/**
	 * The parameter for the temporary file path
	 */
	private static final String PARAM_CONFIG_RECURSIVE_SEARCH = "recursiveList";

	/**
	 * The parameter for FTP file transfer mode
	 */
	private static final String PARAM_CONFIG_FTP_TRANSFER_MODE = "ftpTransferMode";

	/**
	 * The parameter for Passive FTP file transfer mode
	 */
	private static final String PARAM_CONFIG_FTP_PASSIVE = "ftpPassive";

	/**
	 * The parameter for Secutiry type and level for FTP file transfer
	 */
	private static final String PARAM_CONFIG_FTP_SECURITY = "ftpSecurity";

	/**
	 * The parameter for explicit SSL mode for FTP file transfer
	 */
	private static final String PARAM_CONFIG_FTP_EXPLICIT_SSL = "ftpExplicitModeSSL";

	/**
	 * The parameter for file transfer operation timeout for RXA supported
	 * protocols
	 */
	private static final String PARAM_CONFIG_TIME_OUT = "timeout";

	/**
	 * The parameter to handle as Text file
	 */
	private static final String PARAM_CONFIG_HANDLE_TEXT_FILE = "handleAsTextFile";

	/**
	 * The parameter for Enable or disable SSL over an AS400 connection
	 */
	private static final String PARAM_CONFIG_AS400_SSL = "as400SSL";

	/**
	 * The parameter for the Proxy to be used if required for AS400 connection
	 */
	private static final String PARAM_CONFIG_AS400_PROXY = "as400Proxy";

	/**
	 * The parameter for Passive FTP file transfer mode for source
	 */
	private static final String PARAM_CONFIG_FTP_PASSIVE_SRC = "ftpPassive_src";

	/**
	 * The parameter for Secutiry type and level for FTP file transfer for source
	 */
	private static final String PARAM_CONFIG_FTP_SECURITY_SRC = "ftpSecurity_src";

	/**
	 * The parameter for explicit SSL mode for FTP file transfer for source
	 */
	private static final String PARAM_CONFIG_FTP_EXPLICIT_SSL_SRC = "ftpExplicitModeSSL_src";

	/**
	 * The parameter for file transfer operation timeout for RXA supported
	 * protocols for source
	 */
	private static final String PARAM_CONFIG_TIME_OUT_SRC = "timeout_src";

	/**
	 * The parameter to handle as Text file for source
	 */
	private static final String PARAM_CONFIG_HANDLE_TEXT_FILE_SRC = "handleAsTextFile_src";

	/**
	 * The parameter for Enable or disable SSL over an AS400 connection for source
	 */
	private static final String PARAM_CONFIG_AS400_SSL_SRC = "as400SSL_src";

	/**
	 * The parameter for the Proxy to be used if required for AS400 connection for source
	 */
	private static final String PARAM_CONFIG_AS400_PROXY_SRC = "as400Proxy_src";
	
	/**
	 * The parameter for Passive FTP file transfer mode for target
	 */
	private static final String PARAM_CONFIG_FTP_PASSIVE_TGT = "ftpPassive_tgt";

	/**
	 * The parameter for Secutiry type and level for FTP file transfer for target
	 */
	private static final String PARAM_CONFIG_FTP_SECURITY_TGT = "ftpSecurity_tgt";

	/**
	 * The parameter for explicit SSL mode for FTP file transfer for target
	 */
	private static final String PARAM_CONFIG_FTP_EXPLICIT_SSL_TGT = "ftpExplicitModeSSL_tgt";

	/**
	 * The parameter for file transfer operation timeout for RXA supported
	 * protocols for target
	 */
	private static final String PARAM_CONFIG_TIME_OUT_TGT = "timeout_tgt";

	/**
	 * The parameter to handle as Text file for target
	 */
	private static final String PARAM_CONFIG_HANDLE_TEXT_FILE_TGT = "handleAsTextFile_tgt";

	/**
	 * The parameter for Enable or disable SSL over an AS400 connection for target
	 */
	private static final String PARAM_CONFIG_AS400_SSL_TGT = "as400SSL_tgt";

	/**
	 * The parameter for the Proxy to be used if required for AS400 connection for target
	 */
	private static final String PARAM_CONFIG_AS400_PROXY_TGT = "as400Proxy_tgt";

	/**
	 * The property key for connection protocol
	 */
	public static final String PROTOCOL = "connectionProtocol";

	/**
	 * The property key for the Username
	 */
	public static final String USERNAME = "user";

	/**
	 * The property for the Password
	 */
	public static final String PASSWORD = "passwd";

	/**
	 * The property for the Keystore
	 */
	public static final String KEYSTORE = "keystore";

	/**
	 * The property for the Passphrase
	 */
	public static final String PASSPHRASE = "passphrase";

	/**
	 * The property for the Hostname
	 */
	public static final String HOSTNAME = "hostname";

	/**
	 * The property for the Port number
	 */
	public static final String PORT = "port";

	/**
	 * The property for the path
	 */
	public static final String PATH = "path";

	/**
	 * The property for the Source file path
	 */
	public static final String SOURCEPATH = "sourcefilepath";

	/**
	 * The property for Recursive search of the files
	 */
	public static final String RECURSIVE_SEARCH = "recursiveList";

	/**
	 * The parameter for file transfer operation timeout for RXA supported
	 * protocols
	 */
	public static final String TIME_OUT = "timeout";

	/**
	 * The parameter to handle as Text file
	 */
	public static final String HANDLE_TEXT_FILE = "handleAsTextFile";

	/**
	 * The parameter for Enable or disable SSL over an AS400 connection
	 */
	public static final String AS400_SSL = "as400SSL";

	/**
	 * The parameter for the Proxy to be used if required for AS400 connection
	 */
	public static final String AS400_PROXY = "as400Proxy";

	/**
	 * The string for password
	 */
	private static final String PASSWD_STRING = "*******";

	/**
	 * This array stores the names of the TDI GUI parameters that can be
	 * configured
	 */
	protected final String[] PARAM_CONFIG_OPTIONS = { PARAM_CONFIG_SOURCE_PATH,
			PARAM_CONFIG_SOURCE_HOST, PARAM_CONFIG_SOURCE_USERNAME,
			PARAM_CONFIG_SOURCE_PASSWORD, PARAM_CONFIG_SOURCE_KEYSTORE,
			PARAM_CONFIG_SOURCE_PASSPHRASE, PARAM_CONFIG_TARGET_PATH,
			PARAM_CONFIG_TARGET_HOST, PARAM_CONFIG_TARGET_USERNAME,
			PARAM_CONFIG_TARGET_PASSWORD, PARAM_CONFIG_TARGET_KEYSTORE,
			PARAM_CONFIG_TARGET_PASSPHRASE, PARAM_CONFIG_SOURCE_PORT,
			PARAM_CONFIG_TARGET_PORT, PARAM_CONFIG_FTP_TRANSFER_MODE,
			PARAM_CONFIG_FTP_SECURITY_SRC, PARAM_CONFIG_AS400_PROXY_SRC,
			PARAM_CONFIG_FTP_PASSIVE_SRC, PARAM_CONFIG_FTP_EXPLICIT_SSL_SRC,
			PARAM_CONFIG_TIME_OUT_SRC, PARAM_CONFIG_HANDLE_TEXT_FILE_SRC,
			PARAM_CONFIG_AS400_SSL_SRC, PARAM_CONFIG_FTP_SECURITY_TGT, PARAM_CONFIG_AS400_PROXY_TGT,
			PARAM_CONFIG_FTP_PASSIVE_TGT, PARAM_CONFIG_FTP_EXPLICIT_SSL_TGT,
			PARAM_CONFIG_TIME_OUT_TGT, PARAM_CONFIG_HANDLE_TEXT_FILE_TGT,
			PARAM_CONFIG_AS400_SSL_TGT, PARAM_CONFIG_TEMP_DIRECTORY,
			PARAM_CONFIG_DELETE_TEMP, PARAM_CONFIG_RECURSIVE_SEARCH,
			PARAM_CONFIG_CREATE_TARGET_PATH };

	/**
	 * Absolute path of the temporary directory on the local computer
	 */
	private String temp;

	/**
	 * The operator object that connects to the appropriate machine to transfer
	 * the file.
	 */
	protected FileTransferOperator operator = null;

	/**
	 * The operator object that connects to source machine to transfer the file.
	 */
	protected FileTransferOperator sourceOperator = null;

	/**
	 * The operator object that connects to target machine to transfer the file.
	 */
	protected FileTransferOperator targetOperator = null;

	/**
	 * String for Local to Local file transfer
	 */
	public static final String LOCAL_TO_LOCAL = "localtolocal";

	/**
	 * String for Local to Remote file transfer
	 */
	public static final String LOCAL_TO_REMOTE = "localtoremote";

	/**
	 * String for Remote to Local file transfer
	 */
	public static final String REMOTE_TO_LOCAL = "remotetolocal";

	/**
	 * String for Remote to Remote file transfer
	 */
	public static final String REMOTE_TO_REMOTE = "remotetoremote";

	/**
	 * Possible file transfer direction from source to target
	 */
	private enum Direction {
		LOCAL_TO_LOCAL, LOCAL_TO_REMOTE, REMOTE_TO_LOCAL, REMOTE_TO_REMOTE
	}

	/**
	 * Transfer Direction
	 */
	public Direction direction;

	/**
	 * The name of the entry attribute containing value of source protocol to be
	 * used.This attribute should be contained in the TDI Entry object passed to
	 * the FC perform method.
	 */
	private static final String IN_PARAM_SOURCE_PROTOCOL = "source.protocol";

	/**
	 * The name of the entry attribute containing path of the source file which
	 * needs to be transfered. This attribute should be contained in the TDI
	 * Entry object passed to the FC perform method.
	 */
	private static final String IN_PARAM_SOURCE_PATH = "source.path";

	/**
	 * The name of the entry attribute containing host name of the machine where
	 * source file resides. This attribute should be contained in the TDI Entry
	 * object passed to the FC perform method.
	 */
	private static final String IN_PARAM_SOURCE_HOST = "source.hostname";

	/**
	 * The name of the entry attribute containing port which needs to be used to
	 * connect to the machine where source file resides. This attribute should
	 * be contained in the TDI Entry object passed to the FC perform method.
	 */
	private static final String IN_PARAM_SOURCE_PORT = "source.port";

	/**
	 * The name of the entry attribute containing user name which needs to be
	 * used to connect to the machine where source file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_SOURCE_USER = "source.user";

	/**
	 * The name of the entry attribute containing user password which needs to
	 * be used to connect to the machine where source file resides. This
	 * attribute should be contained in the TDI Entry object passed to the FC
	 * perform method.
	 */
	private static final String IN_PARAM_SOURCE_PASSWORD = "source.password";

	/**
	 * The name of the entry attribute containing keystore which needs to be
	 * used to connect to the machine where source file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_SOURCE_KEYSTORE = "source.keystore";

	/**
	 * The name of the entry attribute containing passphrase which needs to be
	 * used to connect to the machine where source file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_SOURCE_PASSPHRASE = "source.passphrase";

	/**
	 * The name of the entry attribute containing value of target protocol to be
	 * used.This attribute should be contained in the TDI Entry object passed to
	 * the FC perform method.
	 */
	private static final String IN_PARAM_TARGET_PROTOCOL = "target.protocol";

	/**
	 * The name of the entry attribute containing path of the target file which
	 * needs to be transfered. This attribute should be contained in the TDI
	 * Entry object passed to the FC perform method.
	 */
	private static final String IN_PARAM_TARGET_PATH = "target.path";

	/**
	 * The name of the entry attribute containing host name of the machine where
	 * target file resides. This attribute should be contained in the TDI Entry
	 * object passed to the FC perform method.
	 */
	private static final String IN_PARAM_TARGET_HOST = "target.hostname";

	/**
	 * The name of the entry attribute containing port which needs to be used to
	 * connect to the machine where target file resides. This attribute should
	 * be contained in the TDI Entry object passed to the FC perform method.
	 */
	private static final String IN_PARAM_TARGET_PORT = "target.port";

	/**
	 * The name of the entry attribute containing user name which needs to be
	 * used to connect to the machine where target file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_TARGET_USER = "target.user";

	/**
	 * The name of the entry attribute containing user password which needs to
	 * be used to connect to the machine where target file resides. This
	 * attribute should be contained in the TDI Entry object passed to the FC
	 * perform method.
	 */
	private static final String IN_PARAM_TARGET_PASSWORD = "target.password";

	/**
	 * The name of the entry attribute containing keystore which needs to be
	 * used to connect to the machine where target file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_TARGET_KEYSTORE = "target.keystore";

	/**
	 * The name of the entry attribute containing passphrase which needs to be
	 * used to connect to the machine where target file resides. This attribute
	 * should be contained in the TDI Entry object passed to the FC perform
	 * method.
	 */
	private static final String IN_PARAM_TARGET_PASSPHRASE = "target.passphrase";

	/**
	 * The attribute to specify the temp directory in the system where this FC runs. 
	 * This value overrides the one in the UI.
	 */
	private static final String TEMP_DIR_VAR = "$tempDirPath";
	
	/**
	 * This array stores all entry attribute names which can be set through
	 * Input entry
	 */
	private static final String[] IN_PARAM_OPTIONS = {
			IN_PARAM_SOURCE_PROTOCOL, IN_PARAM_SOURCE_PATH,
			IN_PARAM_SOURCE_HOST, IN_PARAM_SOURCE_PORT, IN_PARAM_SOURCE_USER,
			IN_PARAM_SOURCE_PASSWORD, IN_PARAM_SOURCE_KEYSTORE,
			IN_PARAM_SOURCE_PASSPHRASE, IN_PARAM_TARGET_PROTOCOL,
			IN_PARAM_TARGET_PATH, IN_PARAM_TARGET_HOST, IN_PARAM_TARGET_PORT,
			IN_PARAM_TARGET_USER, IN_PARAM_TARGET_PASSWORD,
			IN_PARAM_TARGET_KEYSTORE, IN_PARAM_TARGET_PASSPHRASE, TEMP_DIR_VAR};

	/**
	 * The name of the Output entry attribute containing path of the temporary
	 * file created during Remote to Remote file transfer operation
	 */
	private static final String OUT_PARAM_TEMP_FILE_PATH = "$tempFilePath";
	/**
	 * Keeps track of whether the FC has been initialized or not.
	 */
	private boolean initialized;

	/**
	 * Checks whether connection to remote Source machine need to be
	 * re-established or not
	 */
	private boolean sourceReconnect = false;

	/**
	 * Checks whether connection to remote Target machine need to be
	 * re-established or not
	 */
	private boolean targetReconnect = false;

	/**
	 * The way to perform RXA connection related logging
	 */
	protected LogProxy logProxy = null;
	
	/**
	 * The way to perform RXA logging
	 */
	protected TDIRXALogAdapter RXAlogger;

	/**
	 * Source end point configuration object
	 */
	private EndPoint source = null;

	/**
	 * Target end point configuration object
	 */
	private EndPoint target = null;
	
	private String tempDirPath = null;
	
	/** Enable or disable RXA internal logging */
	public static final String PARAM_CONFIG_RXA_LOG = "enableRXALog";

	/**
	 * File Transfer FC constructor
	 */
	public FileTransferFC() {
		super();
		// Initialize the required variables.
		initialized = false;

	}

	/**
	 * The FC receives the information about connection parameters from
	 * configuration panel or from its Output Map and transfers a file from
	 * given Source to Target
	 * 
	 * @param arg0
	 *            the work entry passed to the FC.
	 * @return an Entry object containing $tempFilePath attribute with the
	 *         status of file transfer operation
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object arg0) throws Exception {

		Entry response = new Entry();

		if (arg0 instanceof Entry) {
			Entry request = (Entry) arg0;
			if (request.size() != 0) {
				processInputEntryAttr(request);
			}

			if (source.getPath() == null || source.getPath().length() == 0) {
				throw new Exception(sResHash.getString(
						"FILE.TRANSFER.FC.PARAMETER.NOT.PROVIDED",
						PARAM_CONFIG_SOURCE_PATH));
			}

			if (target.getPath() == null || target.getPath().length() == 0) {
				throw new Exception(sResHash.getString(
						"FILE.TRANSFER.FC.PARAMETER.NOT.PROVIDED",
						PARAM_CONFIG_TARGET_PATH));
			}

			setDirection();

			// Connecting to required target machine and performing file
			// transfer operation based on the actual file transfer
			// direction
			transferFile();
			if (temp != null && temp.length() != 0) {
				response.setAttribute(OUT_PARAM_TEMP_FILE_PATH, temp);
				if (source.props.getProperty(PARAM_CONFIG_DELETE_TEMP) != null
						&& Boolean.valueOf(
								source.props
										.getProperty(PARAM_CONFIG_DELETE_TEMP))
								.booleanValue()) {
					File t = new File(temp);
					if (!t.delete())
						printDebugMessage(
								"FILE.TRANSFER.FC.TEMP.FILE.DELETE.FAILED",
								new Object[] { t.getAbsoluteFile() });
				}

			}
		} else
			throw new Exception(
					sResHash.getString("FILE.TRANSFER.FC.EXPECTS.ENTRY"));

		return response;
	}

	/**
	 * Process the attributes contained within the provided Entry object. If
	 * valid attribute(s) have been provided containing data of the expected
	 * type then store them in the appropriate variables to be used when
	 * performing the file transfer operation. This will wipe over few values
	 * that were configured on the GUI. An appropriate error will be logged if
	 * it occurs.
	 * 
	 * @param request
	 *            Entry to be processed
	 */
	private void processInputEntryAttr(Entry request) {

		/*
		 * Get all provided entry attributes.
		 */
		int numParam = IN_PARAM_OPTIONS.length;

		for (int i = 0; i < numParam; i++) {

			String cmdEntry = request.getString(IN_PARAM_OPTIONS[i]);
			if (cmdEntry != null) {

				// Source
				if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_PROTOCOL) {
					if (source.getProtocol() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_PROTOCOL,
										cmdEntry });
					source.setProtocol(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_PATH) {
					if (source.getPath() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_PATH,
										cmdEntry });
					source.setSourceFile(cmdEntry);
					source.setPath(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_HOST) {
					if (source.getHost() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_HOST,
										cmdEntry });
					source.setHost(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_PORT) {
					if (source.getPort() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_PORT,
										cmdEntry });
					source.setPort(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_USER) {
					if (source.getUser() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_USERNAME,
										cmdEntry });
					source.setUser(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_PASSWORD) {
					if (source.getPassword() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_PASSWORD,
										PASSWD_STRING });
					source.setPassword(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_KEYSTORE) {
					if (source.getKeystore() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_KEYSTORE,
										cmdEntry });
					source.setKeystore(cmdEntry);
					sourceReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_SOURCE_PASSPHRASE) {
					if (source.getPassphrase() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_SOURCE_PASSPHRASE,
										PASSWD_STRING });
					source.setPassphrase(cmdEntry);
					sourceReconnect = true;
				}

				// Target

				else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_PROTOCOL) {
					if (target.getProtocol() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_PROTOCOL,
										cmdEntry });
					target.setProtocol(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_PATH) {
					if (target.getPath() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_PATH,
										cmdEntry });
					target.setPath(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_HOST) {
					if (target.getHost() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_HOST,
										cmdEntry });
					target.setHost(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_PORT) {
					if (target.getPort() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_PORT,
										cmdEntry });
					target.setPort(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_USER) {
					if (target.getUser() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_USERNAME,
										cmdEntry });
					target.setUser(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_PASSWORD) {
					if (target.getPassword() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_PATH,
										PASSWD_STRING });
					target.setPassword(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_KEYSTORE) {
					if (target.getKeystore() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_KEYSTORE,
										cmdEntry });
					target.setKeystore(cmdEntry);
					targetReconnect = true;
				} else if (IN_PARAM_OPTIONS[i] == IN_PARAM_TARGET_PASSPHRASE) {
					if (target.getPassphrase() != null)
						printDebugMessage(
								"FILE.TRANSFER.FC.PARAMETER.OVERRIDDEN",
								new Object[] { PARAM_CONFIG_TARGET_USERNAME,
										PASSWD_STRING });
					target.setPassphrase(cmdEntry);
					targetReconnect = true;
				}
				else if (IN_PARAM_OPTIONS[i] == TEMP_DIR_VAR) {
					tempDirPath = cmdEntry;
				}
			}
		}
	}

	/**
	 * This function is called once after the components configuration file has
	 * been provided by the caller.
	 * 
	 * @param o
	 *            The custom log object from TDI.
	 * 
	 * @throws Exception
	 *             If super class initialize fails.
	 */
	public void initialize(Object o) throws Exception {

		super.initialize(null);

		if (null != o && (o instanceof Log)) {
			logProxy = new LogProxyImpl((Log) o);
		} else {
			logProxy = new LogProxyImpl(this.getLog());
		}
		BaseProtocol.setLogger(getRXACompatableLogger());
		RXAlogger.text(Level.INFO, this, "initialize",
				"Starting RXA Internal Logging.");
		BaseProtocol.startLogging();
		initConfigProperties();

		setDirection();

		try {
			initConnection();
		} catch (Exception e) {
			printDebugMessage(
					"FILE.TRANSFER.FC.CONFIG.EDITOR.CONNECTION.FAILED", null);
		}

	}
	
	
	
	/**
	 * Gets an RXA compatible logger
	 *
	 * @return the logger
	 */
	protected Logger getRXACompatableLogger() {
		try {
			/*
			 * // Example of Getting the jre native logger
			 * java.util.logging.Logger javaLogger = java.util.logging.Logger
			 * .getLogger(loggerName);
			 * javaLogger.setLevel(java.util.logging.Level.FINEST);
			 * javaLogger.setUseParentHandlers(false); // create logging dir
			 * File logDir = new File("./logs"); if (!logDir.exists()) { boolean
			 * created = logDir.mkdir(); if (!created || !logDir.canWrite()) {
			 * throw new IOException("Can't write to log dir: " +
			 * logDir.getAbsolutePath() + " Logging disabled."); } } // set-up
			 * handlers for the trace and message logs java.util.logging.Handler
			 * msgHndlr = new java.util.logging.FileHandler(
			 * "./logs/rxa_message.log", 1000000, 1, true);
			 * msgHndlr.setLevel(java.util.logging.Level.INFO);
			 * msgHndlr.setFormatter(new SimpleFormatter());
			 * java.util.logging.Handler trcHndlr = new
			 * java.util.logging.FileHandler( "./logs/rxa_trace.log", 10000000,
			 * 1, true); trcHndlr.setLevel(java.util.logging.Level.FINEST);
			 * trcHndlr.setFormatter(new SimpleFormatter());
			 * javaLogger.addHandler(msgHndlr); javaLogger.addHandler(trcHndlr);
			 * // Configure RXA logging using the remoteaccess JreLogAdapter
			 * Logger RXAlogger = JreLogAdapter.getLogger(loggerName);
			 */
			if (null == RXAlogger) {
				RXAlogger = new TDIRXALogAdapter(logProxy.getLog());
				RXAlogger.text(Level.INFO, this, "getRXACompatableLogger",
						"TDIRXALogAdaptor Created.");
			}
			Boolean loggingWidget = Boolean
					.valueOf((String) getParam(PARAM_CONFIG_RXA_LOG));
			if (loggingWidget != null) {
				RXAlogger.setLogEnabled(loggingWidget.booleanValue());
			} else {
				RXAlogger.setLogEnabled(false);
			}
			return RXAlogger;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Initializes connection to target machine(s)
	 * 
	 * @throws Exception
	 * @throws ParamException
	 * @throws RemoteConnectException
	 */
	public void initConnection() throws RemoteConnectException, ParamException,
			Exception {

		switch (direction) {
		case REMOTE_TO_LOCAL:
			// Try to connect to remote Source machine
			sourceOperator = initializeConnection(source);
			break;

		case REMOTE_TO_REMOTE:
			// Try to connect to remote Source machine
			sourceOperator = initializeConnection(source);
			// Try to connect to remote Target machine
			targetOperator = initializeConnection(target);
			break;

		case LOCAL_TO_REMOTE:
			// Try to connect to remote Target machine
			targetOperator = initializeConnection(target);
			break;
		}

	}

	/**
	 * Transfers the file from Source to Target Location
	 * 
	 * @throws Exception
	 *             Throws an exception if the connection to remote machine is
	 *             not successful
	 */
	private void transferFile() throws Exception {

		String dest = null;
		switch (direction) {
		case REMOTE_TO_LOCAL:
			// Before initializing connection attempt to create target
			// folder if needed
			createTargetDir(target.getPath());

			// In case of Remote to Local file transfer we receive the
			// file from remote to local computer
			if (sourceReconnect)
				sourceOperator = initializeConnection(source);

			dest = appendFileName(target.getPath(), source.getPath());
			sourceOperator.receiveFile(source.getPath(), dest);
			break;

		case REMOTE_TO_REMOTE:

			temp = createTempFile();

			if (sourceReconnect)
				sourceOperator = initializeConnection(source);

			// Before receiving file from Remote to Local machine attempt to
			// re-connect to end target machine
			if (targetReconnect)
				targetOperator = initializeConnection(target);

			// Before initializing connection attempt to create target
			// folder if needed
			createTargetDir(target.getPath());

			// In case of Remote to Remote file transfer first of all we
			// get the file to temporary directory of the local computer
			sourceOperator.receiveFile(source.getPath(), temp);

			// Now we transfer file from temporary directory to final
			// target computer
			dest = appendFileName(target.getPath(), source.getPath());
			targetOperator.transferFile(temp, dest);
			break;

		case LOCAL_TO_REMOTE:
			if (targetReconnect)
				targetOperator = initializeConnection(target);
			
			if (!initialized)
			{
				throw new Exception(sResHash.getString(
						"FILE.TRANSFER.FC.INITIALIZE.CONNECTION.FAILED",
						new Object[] { target.getHost() }));
			}

			// Before initializing connection attempt to create target
			// folder if needed
			createTargetDir(target.getPath());
			// Now we transfer file from temporary directory to final
			// target computer
			dest = appendFileName(target.getPath(), source.getPath());
			targetOperator.transferFile(source.getPath(), dest);

			break;

		case LOCAL_TO_LOCAL:
			// Attempt to create target folder if needed
			createTargetDir(target.getPath());

			copyLocalFile(source.getPath(),
					appendFileName(target.getPath(), source.getPath()));

		}
	}

	/**
	 * Creates the destination directory if doesn't already exists
	 * 
	 * @param targetDir
	 *            directory which needs to be created
	 * @throws Exception
	 */
	private void createTargetDir(String targetDir) throws Exception {
		if (source.props.getProperty(PARAM_CONFIG_CREATE_TARGET_PATH) != null
				&& Boolean.valueOf(
						source.props
								.getProperty(PARAM_CONFIG_CREATE_TARGET_PATH))
						.booleanValue()) {
			switch (direction) {
			case REMOTE_TO_LOCAL:
			case LOCAL_TO_LOCAL:
				File dir = new File(targetDir);
				if (!dir.isDirectory() && !dir.mkdirs())
						throw new Exception(sResHash.getString(
								"FILE.TRANSFER.FC.UNABLE.TO.CREATE.DIRECTORY",
								new Object[] { dir.getAbsolutePath() }));
				break;

			case REMOTE_TO_REMOTE:
			case LOCAL_TO_REMOTE:
				targetOperator.createDirectory(targetDir);
				break;
			}

		}
	}

	/**
	 * Copies a file on Local computer from one location to another location
	 * 
	 * @param source
	 *            Absolute path of the source file
	 * @param target
	 *            Absolute path of the target file
	 * @throws Exception
	 *             Exception if Source and Target file paths are same
	 */
	private void copyLocalFile(String source, String target) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		File f1 = new File(source);
		File f2 = new File(target);

		if (f1.getAbsolutePath().equalsIgnoreCase(f2.getAbsolutePath()))
			throw new Exception(sResHash.getString(
					"FILE.TRANSFER.FC.FILE.PATH.IDENTICAL",
					new Object[] { target }));

		try {
			in = new FileInputStream(f1);
		} catch (FileNotFoundException fnf) {
			throw new Exception(sResHash.getString(
					"FILE.TRANSFER.FC.SOURCE.FILE.NOT.FOUND",
					new Object[] { source }));
		}
		try {
			out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (FileNotFoundException fnf) {
			throw new Exception(sResHash.getString(
					"FILE.TRANSFER.FC.UNABLE.WRITE.TARGET.FILE",
					new Object[] { target }));
		} finally {
			try {
				if (null != in)
					in.close();
			} finally {
				if (null != out)
					out.close();
			}
		}

	}

	/**
	 * Gets the version of this FC.
	 * 
	 * @return version string
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Initialize the configuration parameters provided for the FC on the TDI
	 * GUI.
	 * 
	 * @throws Exception
	 */
	private void initConfigProperties() throws Exception {

		// Create instances of the Source and Target End Points
		source = new EndPoint();
		target = new EndPoint();

		String connectionProtocol = (String) getParam(PARAM_CONFIG_SOURCE_PROTOCOL);
		if (connectionProtocol.length() == 0) {
			// Set the Source connection protocol to "ANY"
			source.setProtocol("ANY");
			printDebugMessage(
					"FILE.TRANSFER.FC.CONNECTION.PROTOCOL.INITIALIZED",
					new Object[] { PARAM_CONFIG_SOURCE_PROTOCOL, "ANY" });
		} else {
			source.setProtocol(connectionProtocol);
			printDebugMessage("FILE.TRANSFER.FC.PARAMETER.INITIALIZED",
					new Object[] { PARAM_CONFIG_SOURCE_PROTOCOL,
							connectionProtocol });
		}

		connectionProtocol = (String) getParam(PARAM_CONFIG_TARGET_PROTOCOL);
		if (connectionProtocol.length() == 0) {
			// Set the Source connection protocol to "ANY"
			target.setProtocol("ANY");
			printDebugMessage(
					"FILE.TRANSFER.FC.CONNECTION.PROTOCOL.INITIALIZED",
					new Object[] { PARAM_CONFIG_TARGET_PROTOCOL, "ANY" });
		} else {
			target.setProtocol(connectionProtocol);
			printDebugMessage("FILE.TRANSFER.FC.PARAMETER.INITIALIZED",
					new Object[] { PARAM_CONFIG_TARGET_PROTOCOL,
							connectionProtocol });
		}

		/*
		 * Get all provided Config parameters.
		 */
		int numParam = PARAM_CONFIG_OPTIONS.length;

		for (int i = 0; i < numParam; i++) {
			String paramVal = (String) getParam(PARAM_CONFIG_OPTIONS[i]);
			if (null != paramVal && paramVal.length() != 0) {

				// Source
				if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PATH) {
					source.setPath(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_HOST) {
					source.setHost(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PORT) {
					source.setPort(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_USERNAME) {
					source.setUser(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PASSWORD) {
					source.setPassword(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_KEYSTORE) {
					source.setKeystore(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PASSPHRASE) {
					source.setPassphrase(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PORT) {
					source.setPort(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_SECURITY_SRC) {
					source.props.setProperty(PARAM_CONFIG_FTP_SECURITY, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_AS400_PROXY_SRC) {
					source.props.setProperty(PARAM_CONFIG_AS400_PROXY, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_PASSIVE_SRC) {
					source.props.setProperty(PARAM_CONFIG_FTP_PASSIVE, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_EXPLICIT_SSL_SRC) {
					source.props.setProperty(PARAM_CONFIG_FTP_EXPLICIT_SSL, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TIME_OUT_SRC) {
					source.props.setProperty(PARAM_CONFIG_TIME_OUT, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_HANDLE_TEXT_FILE_SRC) {
					source.props.setProperty(PARAM_CONFIG_HANDLE_TEXT_FILE, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_AS400_SSL_SRC) {
					source.props.setProperty(PARAM_CONFIG_AS400_SSL, paramVal);
					
					
					// Target
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PATH) {
					target.setPath(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_HOST) {
					target.setHost(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PORT) {
					target.setPort(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_USERNAME) {
					target.setUser(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PASSWORD) {
					target.setPassword(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_KEYSTORE) {
					target.setKeystore(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PASSPHRASE) {
					target.setPassphrase(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PORT) {
					target.setPort(paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_SECURITY_TGT) {
					target.props.setProperty(PARAM_CONFIG_FTP_SECURITY, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_AS400_PROXY_TGT) {
					target.props.setProperty(PARAM_CONFIG_AS400_PROXY, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_PASSIVE_TGT) {
					target.props.setProperty(PARAM_CONFIG_FTP_PASSIVE, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_FTP_EXPLICIT_SSL_TGT) {
					target.props.setProperty(PARAM_CONFIG_FTP_EXPLICIT_SSL, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TIME_OUT_TGT) {
					target.props.setProperty(PARAM_CONFIG_TIME_OUT, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_HANDLE_TEXT_FILE_TGT) {
					target.props.setProperty(PARAM_CONFIG_HANDLE_TEXT_FILE, paramVal);
				} else if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_AS400_SSL_TGT) {
					target.props.setProperty(PARAM_CONFIG_AS400_SSL, paramVal);
				}

				// Other Advanced Options
				else {
					source.props.setProperty(PARAM_CONFIG_OPTIONS[i], paramVal);
					target.props.setProperty(PARAM_CONFIG_OPTIONS[i], paramVal);
				}

				if (PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PASSWORD
						|| PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_SOURCE_PASSPHRASE
						|| PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PASSWORD
						|| PARAM_CONFIG_OPTIONS[i] == PARAM_CONFIG_TARGET_PASSPHRASE)
					printDebugMessage("FILE.TRANSFER.FC.PARAMETER.INITIALIZED",
							new Object[] { PARAM_CONFIG_OPTIONS[i],
									PASSWD_STRING });
				else
					printDebugMessage("FILE.TRANSFER.FC.PARAMETER.INITIALIZED",
							new Object[] { PARAM_CONFIG_OPTIONS[i], paramVal });
			}
		}
	}

	/**
	 * Sets the direction of the final file transfer operation
	 * 
	 * @throws Exception
	 */
	private void setDirection() throws Exception {

		if (source.isLocal() && target.isLocal()) {
			direction = Direction.LOCAL_TO_LOCAL;
		} else if (source.isLocal() && !target.isLocal()) {
			direction = Direction.LOCAL_TO_REMOTE;
		} else if (!source.isLocal() && target.isLocal()) {
			direction = Direction.REMOTE_TO_LOCAL;
		} else if (!source.isLocal() && !target.isLocal()) {
			direction = Direction.REMOTE_TO_REMOTE;
		}
		if (direction != null)
			printDebugMessage("FILE.TRANSFER.FC.TRANSFER.DIRECTION",
					new Object[] { direction });
		else
			throw new Exception(
					sResHash.getString("FILE.TRANSFER.FC.UNKNOWN.TRANSFER.DIRECTION"));
	}

	/**
	 * Prints a debug message if debug mode for the Components is enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	public void printDebugMessage(String msgKey, Object[] params) {
		if (params == null || params.length == 0) {
			debug(sResHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(sResHash.getString(msgKey, params[0]));
		} else {
			debug(sResHash.getString(msgKey, params));
		}
	}

	/**
	 * Checks the connection to a given target computer
	 * 
	 * @param target
	 *            Configuration of the target end point object
	 * 
	 * @return Returns the Object of FileTransferOperator
	 * 
	 * @throws RemoteConnectException
	 * 
	 * @throws ParamException
	 * 
	 * @throws Exception
	 * 
	 */
	private FileTransferOperator initializeConnection(EndPoint tgt)
	{

		printDebugMessage("FILE.TRANSFER.FC.INITIALIZING.CONNECTION",
				new Object[] { tgt.getHost() });

		operator = new FileTransferOperator(tgt.props, logger);
		try
		{
			initialized = operator.prepareConnection();
			printDebugMessage("FILE.TRANSFER.FC.CONNECTION.SUCCESSFUL",
					new Object[] { tgt.getHost() });
			return operator;
		}
		catch (Exception e)
		{
			Error err =  new Error(sResHash.getString(
					"FILE.TRANSFER.FC.INITIALIZE.CONNECTION.FAILED",
					new Object[] { tgt.getHost() }));
			err.initCause(e);
			throw err;
		}
	}

	/**
	 * Creates a temporary File under user specified temporary location if
	 * specified, otherwise system's temporary directory
	 * 
	 * @return path string of the temporary directory
	 * @throws IOException
	 */
	private String createTempFile() throws IOException {
		File temp;
		if(tempDirPath != null)
		{
			temp = File.createTempFile(
					"temp",
					null,
					new File(tempDirPath));
		}
		else if (source.props.getProperty(PARAM_CONFIG_TEMP_DIRECTORY) != null) {
			temp = File.createTempFile(
					"temp",
					null,
					new File(source.props
							.getProperty(PARAM_CONFIG_TEMP_DIRECTORY)));
		} else
			temp = File.createTempFile("temp", null, null);
		return temp.getAbsolutePath();
	}

	/**
	 * This function is called when the connector is no longer needed by the
	 * user in the Assembly Line or script. Always calls the superclass
	 * terminate method which will take care of releasing resources, closing
	 * parsers etc.
	 * 
	 * @throws Exception
	 */
	public void terminate() throws Exception {
		if (operator != null)
			operator.close();
		if (sourceOperator != null)
			sourceOperator.close();
		if (targetOperator != null)
			targetOperator.close();
		super.terminate();
	}

	/**
	 * This function makes a list of protocols which can be used to make a
	 * successful connection to the specified machine with the specified
	 * connection parameters
	 * 
	 * @param isSource
	 *            Checks if protocols need to be retrieved for Source end point
	 * 
	 * @return List of protocols which can be used to make a successful
	 *         connection to the specified machine with the specified connection
	 *         parameters
	 * 
	 * 
	 */
	public Vector<String> getProtocols(boolean isSource) {

		if (isSource)
			operator = new FileTransferOperator(source.props, getLog());
		else
			operator = new FileTransferOperator(target.props, getLog());

		return operator.getProtocolList();
	}

	/**
	 * Appends the file name to the directory path
	 * 
	 * @param directory
	 *            path of the directory to which file name needs to be appended
	 * 
	 * @param filepath
	 *            Path of the file
	 * 
	 * @return file path
	 */
	private String appendFileName(String directory, String file) {

		// Changed the logic to retrieve the filename by parsing the given
		// String, instead of using File.getName() API, because it was causing
		// issues while executing AL on a Unix platform.
		file = file.replace("\\", "/");
		String[] arr = file.split("/");
		String dir = directory.trim().replace("\\", "/");
		if("/".equals(dir))
		{
			return "/" + arr[arr.length - 1];
		}
		else if(dir.endsWith("/"))
		{
			return dir + arr[arr.length - 1];
		}
		return dir + "/" + arr[arr.length - 1];
	}

	/**
	 * Receives the file from Source to Local machine
	 * 
	 * @throws Exception
	 * @throws ParamException
	 * @throws RemoteConnectException
	 */
	public void getFile() throws RemoteConnectException, ParamException,
			Exception {
		sourceOperator = initializeConnection(source);
		sourceOperator.receiveFile(source.getPath(), target.getPath());
	}

	/**
	 * Send the file from Local to Target machine
	 * 
	 * @throws Exception
	 * @throws ParamException
	 * @throws RemoteConnectException
	 */
	public void putFile() throws RemoteConnectException, ParamException,
			Exception {
		targetOperator = initializeConnection(target);
		String dest = appendFileName(target.getPath(), source.getPath());
		targetOperator.transferFile(source.getPath(), dest);
	}

	/**
	 * Retrieves the list of Files in the Source path
	 * 
	 * @throws Exception
	 * @throws ParamException
	 * @throws RemoteConnectException
	 */
	public String[] listSource() throws RemoteConnectException, ParamException,
			Exception {
		sourceOperator = initializeConnection(source);
		return sourceOperator.list(source.getPath());
	}

	/**
	 * Retrieves the list of Files in the Target path
	 * 
	 * @throws Exception
	 * @throws ParamException
	 * @throws RemoteConnectException
	 */
	public String[] listTarget() throws RemoteConnectException, ParamException,
			Exception {
		targetOperator = initializeConnection(target);
		return targetOperator.list(target.getPath());
	}
}
