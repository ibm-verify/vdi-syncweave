/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Properties;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.tivoli.remoteaccess.FileInfo;
import com.ibm.tivoli.remoteaccess.ProgramOutput;
import com.ibm.tivoli.remoteaccess.RemoteAccess;

/**
 * This class encapsulates the RXA library's Connection related objects
 */
public abstract class ConnectionImpl implements Connection {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Array List for the list of files
	 */
	private ArrayList<String> fileList = new ArrayList<String>();

	/*
	 * Configuration variables for the connection
	 */

	/**
	 * The hostname (address) of the target machine. This is a required
	 * parameter
	 */
	protected String hostName;

	/**
	 * The name of a user with Administrative privileges on the target machine
	 */
	protected String userName;

	/**
	 * The password for the user (specified as Remote User) on the target
	 * machine. This parameter may be optional in the case of SSH connections
	 * using a keystore, as well as for RSH connections
	 */
	protected byte[] password;

	/**
	 * The passphrase that protects your private key, in the keystore specified
	 * by the Keystore Path parameter above
	 */
	protected byte[] passphrase;

	/**
	 * The character encoding to use for AS400 command line arguments
	 */
	protected String cmdArgsCharEncode;

	/**
	 * Full path to the file containing the keystore
	 */
	protected String keystore;

	/**
	 * The desired CPU timeout period in milliseconds
	 */
	protected int timeout = 0;

	/**
	 * The port to use to connect to the target machine
	 */
	protected int port = 0;

	/**
	 * This parameter governs whether an SSL connection is enforced on the AS400
	 * (i5/OS) connection
	 */
	protected boolean as400SSL = false;

	/**
	 * This parameter defines an AS400 proxy server if so required
	 */
	protected String as400Proxy;

	/**
	 * An optional Remote CLFC parameter that defines the type of command
	 * execution to use for an AS400 (i5/OS) connection
	 */
	protected boolean as400RunProg = false;

	/**
	 * An optional Remote CLFC parameter that defines a timeout period for the
	 * initial connection to the target system. This has no effect on AS400
	 * targets
	 */
	protected int initial_timeout = 0;

	/**
	 * String for Command Return Code Parameter
	 */
	public static final String COMMAND_RETURN_CODE = "command.returnCode";

	/**
	 * String for Command Output Parameter
	 */
	public static final String COMMAND_STD_OUTPUT = "command.out";

	/**
	 * String for Command Error Parameter
	 */
	public static final String COMMAND_STD_ERROR = "command.error";

	/**
	 * String for Hostname parameter of Remote Machine (target)
	 */
	public static final String HOSTNAME = "hostname";

	/**
	 * String for the username paremeter to be used when connecting to target
	 */
	public static final String USERNAME = "user";

	/**
	 * String for the port parameter to be used when establishing connection
	 * with target
	 */
	public static final String PORT = "port";

	/**
	 * String for the User password parameter to authenticate to the target
	 */
	public static final String PASSWORD = "passwd";

	/**
	 * String for the file path on the target
	 */
	public static final String PATH = "path";

	/**
	 * String for the Source file path
	 */
	public static final String SOURCEPATH = "sourcefilepath";

	/**
	 * String for the Timeout duration parameter for executing the command (ms)
	 */
	public static final String TIMEOUT = "timeout";

	/**
	 * String for the keystore parameter to be used when connecting to the
	 * remote machine using the SSH protocol
	 */
	public static final String KEYSTORE = "keystore";

	/**
	 * String for the passphrase parameter for the keystore
	 */
	public static final String PASSPHRASE = "passphrase";

	/**
	 * String for the Enable or disable SSL parameter over an AS400 connection
	 */
	public static final String AS400_SSL = "as400SSL";

	/**
	 * String for the Proxy parameter to be used if required for AS400
	 * connection
	 */
	public static final String AS400_PROXY = "as400Proxy";

	/**
	 * String for the Secondary mechanism parameter for executing AS400 Programs
	 */
	public static final String AS400_RUNPROG = "as400RunProg";

	/**
	 * String for the Parameter to handle as text file
	 */
	public static final String HANDLE_TEXT_FILE = "handleAsTextFile";

	/**
	 * String for the Timeout parameter of the connection
	 */
	public static final String INITIAL_TIMEOUT = "connection_timeout";

	/**
	 * String for the Timeout parameter of the connection
	 */
	public static final String RECURSIVE_SEARCH = "recursiveList";

	/*
	 * The Attribute objects that will contain the results from executing a
	 * remote command. These Attributes will be included in the returned Entry
	 * object.
	 */

	/**
	 * The return code from the executed command indicating the success of the
	 * requested operation
	 */
	protected Attribute rtnCodeAttr = new Attribute(COMMAND_RETURN_CODE);

	/**
	 * The standard error from the executed command
	 */
	protected Attribute stdErrorAttr = new Attribute(COMMAND_STD_ERROR);

	/**
	 * The standard output from the executed command
	 */
	protected Attribute stdOutputAttr = new Attribute(COMMAND_STD_OUTPUT);

	/**
	 * The RemoteAccess object connecting to the target system
	 */
	private RemoteAccess ra;

	/**
	 * Used for logging
	 */
	protected LogProxy lp;

	/**
	 * Parent directory to the folder containing the stdin destination folder
	 */
	protected String parentDirToStdin;

	/**
	 * Path to random directory created
	 */
	protected String randomDir;

	/**
	 * Empty String
	 */
	protected static final String EMPTYSTRING = "";

	/**
	 * Boolean that decides if stdin and stdout files have to be handled as text
	 */
	private boolean isHandleAsTextFile = false;

	/**
	 * Boolean that decides sub-directories also needs to be looked into for
	 * list of files
	 */
	private boolean recursive = false;

	/**
	 * ConnectionImpl Constructor
	 * 
	 * @param log
	 *            LogProxy for logging
	 */
	public ConnectionImpl(LogProxy log) {
		super();
		parentDirToStdin = null;
		lp = log;
		// rxalp = new RXALoggerImpl(lp.getLog());
	}

	/**
	 * Initialize the properties of the connection
	 * 
	 * @param p
	 *            Properties object containing the configurable connection
	 *            attributes
	 */
	public void initializeProps(Properties p) {
		randomDir = "";
		cmdArgsCharEncode = null;
		hostName = p.getProperty(HOSTNAME);
		userName = p.getProperty(USERNAME);
		if (p.getProperty(PORT) != null) {
			Integer pt = new Integer(p.getProperty(PORT));
			port = pt.intValue();
		}
		if (p.getProperty(PASSWORD) != null) {
			password = (p.getProperty(PASSWORD)).getBytes();
		}
		if (p.getProperty(TIMEOUT) != null) {
			Integer time = new Integer(p.getProperty(TIMEOUT));
			timeout = time.intValue();
		}

		if (p.getProperty(KEYSTORE) != null) {
			keystore = p.getProperty(KEYSTORE);
		}
		if (p.getProperty(PASSPHRASE) != null) {
			passphrase = (p.getProperty(PASSPHRASE)).getBytes();
		} else {
			passphrase = EMPTYSTRING.getBytes();
		}
		if (p.getProperty(AS400_SSL) != null) {
			as400SSL = Boolean.valueOf(p.getProperty(AS400_SSL)).booleanValue();
		}
		as400Proxy = p.getProperty(AS400_RUNPROG);
		if (p.getProperty(AS400_SSL) != null) {
			as400RunProg = Boolean.valueOf(p.getProperty(AS400_RUNPROG))
					.booleanValue();
		}

		if ((p.getProperty(HANDLE_TEXT_FILE) != null)
				&& (p.getProperty(HANDLE_TEXT_FILE).trim().length() > 0)) {
			isHandleAsTextFile = Boolean.valueOf(
					p.getProperty(HANDLE_TEXT_FILE)).booleanValue();

		}

		if ((p.getProperty(INITIAL_TIMEOUT) != null)
				&& (p.getProperty(INITIAL_TIMEOUT).trim().length() > 0)) {
			initial_timeout = Integer.valueOf(p.getProperty(INITIAL_TIMEOUT))
					.intValue();

		}

		if (p.getProperty(RECURSIVE_SEARCH) != null) {
			recursive = Boolean.valueOf(p.getProperty(RECURSIVE_SEARCH))
					.booleanValue();
		}

	}

	/**
	 * Begin a session with the target machine
	 * 
	 * @return RemoteAccess The RXA connection object connecting to the target
	 * @throws RemoteConnectException
	 */
	public abstract RemoteAccess beginSession() throws RemoteConnectException;

	/**
	 * Return this connection type
	 * 
	 * @return String The connection protocol used for this connection. One of:
	 *         WIN, REXEC, RSH, SSH
	 */
	public abstract String getType();

	/**
	 * End the session with the target machine
	 */
	public void endSession() {
		if (ra != null) {
			ra.endSession();
		}
	}

	/**
	 * Transform the ProgramOutput into an Entry object containing the results
	 * 
	 * @param p
	 *            The ProgramOutput object containing the results from executing
	 *            the command
	 * @return Entry object containing three attributes: command.out,
	 *         command.error, command.returnCode
	 */
	private Entry getEntryResponse(ProgramOutput p) {
		ProgramOutput response = p;
		Entry rsp = new Entry();
		rtnCodeAttr.clear();
		stdOutputAttr.clear();
		stdErrorAttr.clear();
		rtnCodeAttr.addValue(Integer.valueOf((response.getReturnCode())));
		stdOutputAttr.addValue(response.getStdout());
		stdErrorAttr.addValue(response.getStderr());
		rsp.setAttribute(COMMAND_RETURN_CODE, rtnCodeAttr);
		rsp.setAttribute(COMMAND_STD_ERROR, stdErrorAttr);
		rsp.setAttribute(COMMAND_STD_OUTPUT, stdOutputAttr);
		return rsp;
	}

	/**
	 * Run the specified command on the target machine
	 * 
	 * @param c
	 *            String containing the command to be executed
	 * @return Entry containing the results from executing the command. It
	 *         contains three attributes: command.out, command.error and
	 *         command.returnCode.
	 * @throws GeneralCLFCException
	 */
	public Entry runCommand(String c) throws GeneralCLFCException {
		String cmd = c;
		Entry rsp = null;
		ProgramOutput po = null;
		try {
			if (ra != null) {
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.PRIOR_TO_EXECUTION, new Object[] { cmd }));
				if (ra instanceof com.ibm.tivoli.remoteaccess.WindowsProtocol) {
					 //use new API if this is only Windows protocol
					po = ((com.ibm.tivoli.remoteaccess.WindowsProtocol)ra).run(cmd, null, timeout, false, false);
				} else {
					po = ra.run(cmd, timeout);
				}
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.EXECUTION_COMPLETE, null));
			} else {
				lp.error(MessageHelper.getMsgResource().getMessage(
						MsgIds.REMOTE_CONNECT_ERROR, null));
			}
		} catch (ConnectException e) {
			throw ExceptionFactory.createGeneralCLFCException(e, lp);
		} catch (FileNotFoundException e) {
			throw ExceptionFactory.createGeneralCLFCException(e, lp);
		}
		if (po != null) {
			if (po.isTimeoutExpired()) {
				if (!(randomDir.equals(""))) {
					try {
						lp.debug(MessageHelper.getMsgResource().getMessage(
								MsgIds.REMOVE_DIR, new Object[] { randomDir }));
						ra.setCurrentDirectory(parentDirToStdin);
						ra.rm(randomDir, true, true);
					} catch (ConnectException ce) {
						throw ExceptionFactory.createGeneralCLFCException(ce,
								lp);
					} catch (FileNotFoundException fnfe) {
						throw ExceptionFactory.createGeneralCLFCException(fnfe,
								lp);
					} catch (IOException io) {
						throw ExceptionFactory.createGeneralCLFCException(io,
								lp);
					}
				}
				throw ExceptionFactory.createGeneralCLFCException(
						MsgIds.TIMEOUT, lp);
			}
			rsp = getEntryResponse(po);
		}
		return rsp;
	}

	/**
	 * Create a random directory on the target machine
	 * 
	 * @param p
	 *            The path to the parent directory where the random directory is
	 *            to be created
	 * @return String specifying the path to the random directory that was
	 *         created
	 * @throws GeneralCLFCException
	 * @throws RemoteConnectException
	 */
	public String createRandomDir(String p) throws RemoteConnectException,
			GeneralCLFCException {
		String path = p;
		try {
			if (path == null) {
				path = ra.getTempDir();
			}
			parentDirToStdin = path;
			randomDir = ra.mkRandomDirectory(path);
			return randomDir;
		} catch (ConnectException ce) {
			throw ExceptionFactory.createRemoteConnectException(ce, lp);
		} catch (FileNotFoundException f) {
			throw ExceptionFactory.createGeneralCLFCException(f, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}
	
	/**
	 * Create a directory on the target machine
	 * 
	 * @param dir
	 *            Path of the folder to be created
	 * @throws GeneralCLFCException
	 *             if the folder cannot be successfully created on the target
	 */
	public void createDir(String dir) throws GeneralCLFCException {
		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.CREATE_DIR, new Object[] { dir }));
			if(!ra.exists(dir))
			{
				ra.mkDirs(dir);
			}
			else
			{
				lp.loginfo("Directory '" + dir  + "' already exists");
			}
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fnfe) {
			throw ExceptionFactory.createGeneralCLFCException(fnfe, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}

	/**
	 * Remove a directory on the target machine
	 * 
	 * @param dir
	 *            The path to the folder to be deleted
	 * @throws GeneralCLFCException
	 *             if the folder cannot be successfully removed from the target
	 */
	public void removeDir(String dir) throws GeneralCLFCException {
		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.REMOVE_DIR, new Object[] { dir }));
			ra.setCurrentDirectory(parentDirToStdin);
			ra.rm(dir, true, true);
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fnfe) {
			throw ExceptionFactory.createGeneralCLFCException(fnfe, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}

	/**
	 * Transfer the specified file from the local machine to a specified
	 * destination on the remote target
	 * 
	 * @param localPath
	 *            Path to the source file on the local machine
	 * @param remotePath
	 *            Path to where the file is to be stored on the remote machine
	 * @throws GeneralCLFCException
	 *             if the transfer fails
	 */
	public void transferFile(String localPath, String remotePath)
			throws GeneralCLFCException {
		try {
			if (!isHandleAsTextFile) {
				ra.putFile(localPath, remotePath, timeout);
			} else {
				ra.setConversionCharset(ra.getRemoteCharset());
				ra.putTextFile(localPath, remotePath, timeout);
			}
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fnfe) {
			throw ExceptionFactory.createGeneralCLFCException(fnfe, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}

	/**
	 * Receives the specified file from the target machine to a specified
	 * destination on the local machine
	 * 
	 * @param localPath
	 *            Path to the source file on the remote machine
	 * @param remotePath
	 *            Path to where the file is to be stored on local machine
	 * @throws GeneralCLFCException
	 *             if the transfer fails
	 */
	public void receiveFile(String remotepath, String localpath)
			throws GeneralCLFCException {
		try {
			if (!isHandleAsTextFile) {
				ra.getFile(remotepath, localpath, timeout);
			} else {
				ra.setConversionCharset(ra.getRemoteCharset());
				ra.getTextFile(remotepath, localpath, timeout);
			}
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fnfe) {
			throw ExceptionFactory.createGeneralCLFCException(fnfe, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}

	/**
	 * Returns true/false to check a given file exists or not
	 * 
	 * @param path
	 *            path on the file
	 * @return true if the given file exists on the remote machine otherwise
	 *         false
	 * @throws GeneralCLFCException
	 */
	public boolean isExists(String path) throws GeneralCLFCException {
		try {
			return ra.exists(path);
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		}
	}

	/**
	 * Retrieves the list of files
	 * 
	 * @param path
	 *            path on the remote machine
	 * @throws GeneralCLFCException
	 */
	public ArrayList<String> list(String path) throws GeneralCLFCException {

		try {
			FileInfo[] file = ra.listFiles(path);
			for (FileInfo files : file) {
				if (!(files.getFileType() == FileInfo.isDirectory))
					fileList.add(path + "/" + files.getFilename());
				else if (recursive && !files.getFilename().startsWith("."))
					list(path + "/" + files.getFilename());
			}
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fne) {
			throw ExceptionFactory.createGeneralCLFCException(fne, lp);
		}
		return fileList;
	}

	/**
	 * Return the character that the target uses to separate path information
	 * 
	 * @return char separator
	 * @throws RemoteConnectException
	 */
	public char getOSSeparator() throws RemoteConnectException {
		char separator;
		try {
			separator = ra.getOS().getSeparator();
		} catch (ConnectException ce) {
			throw ExceptionFactory.createRemoteConnectException(ce, lp);
		}
		return separator;
	}

	/**
	 * Return the RXA Connection Object
	 * 
	 * @return RemoteAccess connection object accessing the target
	 */
	public RemoteAccess getRXAProtocol() {
		return ra;
	}

	/**
	 * Set the hostname of the target with which to connect
	 * 
	 * @param s
	 *            The hostname of the target
	 */
	public void setHost(String s) {
		hostName = s;
	}

	/**
	 * Get the hostname of the target for the connection
	 * 
	 * @return String hostname
	 */
	public String getHost() {
		return hostName;
	}

	/**
	 * Set the username to be used when connecting to the target
	 * 
	 * @param u
	 *            Username
	 */
	public void setUser(String u) {
		userName = u;
	}

	/**
	 * Get the username being used in the connection
	 * 
	 * @return String username
	 */
	public String getUser() {
		return userName;
	}

	/**
	 * Set the RXA connection object for this connection
	 * 
	 * @param remote
	 *            The protocol-specific connection object
	 */
	public void setRXAProtocol(RemoteAccess remote) {
		ra = remote;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCmdArgsCharEncode(String enc) {
		cmdArgsCharEncode = enc;
	}
}
