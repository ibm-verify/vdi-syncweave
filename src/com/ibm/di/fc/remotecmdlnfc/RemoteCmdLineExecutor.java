/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.remotecmdlnfc;

import java.util.Properties;

import com.ibm.di.entry.Entry;
import com.ibm.di.protocols.rxa.AS400Connection;
import com.ibm.di.protocols.rxa.Connection;
import com.ibm.di.protocols.rxa.ExceptionFactory;
import com.ibm.di.protocols.rxa.GeneralCLFCException;
import com.ibm.di.protocols.rxa.LogProxy;
import com.ibm.di.protocols.rxa.MessageHelper;
import com.ibm.di.protocols.rxa.MsgIds;
import com.ibm.di.protocols.rxa.ParamException;
import com.ibm.di.protocols.rxa.RSHConnection;
import com.ibm.di.protocols.rxa.RemoteConnectException;
import com.ibm.di.protocols.rxa.RexecConnection;
import com.ibm.di.protocols.rxa.SSHConnection;
import com.ibm.di.protocols.rxa.SelectProtocol;
import com.ibm.di.protocols.rxa.WinConnection;
import com.ibm.tivoli.remoteaccess.RemoteAccess;

/**
 * The CmdLineExecutor that connects to and executes commands on a remote
 * machine
 */
public class RemoteCmdLineExecutor implements CmdLineExecutor {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Configuration properties for the initialization of the connection
	 */
	private Properties rxaProps;

	/**
	 * Logger to be used
	 */
	private LogProxy logproxy;

	/**
	 * The connection object used to connect to the remote machine.
	 */
	private Connection connect = null;

	/**
	 * Constructor for the RemoteCmdLineExecutor
	 * 
	 * @param p
	 *            The properties for the executor connection
	 * @param lp
	 *            The LogProxy for logging
	 */
	public RemoteCmdLineExecutor(Properties p, LogProxy lp) {
		logproxy = lp;
		rxaProps = p;
	}

	/**
	 * Create a connection with the target machine
	 * 
	 * @return boolean value representing the success of the connection
	 * @throws RemoteConnectException
	 * @throws ParamException
	 */
	public boolean prepareConnection() throws RemoteConnectException,
			ParamException {
		if (!checkRequiredProps()) {
			throw ExceptionFactory.createParamException(
					MsgIds.MISSING_PARAMS, logproxy);
		}
		return initRXAClientConnection();
	}

	/**
	 * Execute the command on the target machine.
	 * 
	 * @param cmdToExecute
	 *            The command to be executed
	 * @return Entry object containing three output attributes: command.out,
	 *         command.error and command.returnCode.
	 * @throws RemoteConnectException
	 * @throws GeneralCLFCException
	 */
	public Entry executeCommand(String cmdToExecute)
			throws RemoteConnectException, GeneralCLFCException {
		Entry response = null;
		if (connect != null) {
			response = connect.runCommand(cmdToExecute);
		} else {
			logproxy.error(MessageHelper.getMsgResource().getMessage(
					MsgIds.REMOTE_CONNECT_ERROR));
		}
		return response;
	}

	/**
	 * Execute the command on the target machine.
	 * 
	 * @param cmdToExecute
	 *            The command to be executed
	 * @param args
	 *            The command arguments as a String Array
	 * @param argDelimiter
	 *            The command argument delimiter
	 * @return Entry object containing three output attributes: command.out,
	 *         command.error and command.returnCode.
	 * @throws RemoteConnectException
	 * @throws GeneralCLFCException
	 */
	public Entry executeCommand(String cmdToExecute, String[] args,
			String argDelimiter) throws RemoteConnectException,
			GeneralCLFCException {
		Entry response = null;

		if (connect != null) {
			// currently only the AS400 connection allows arguments to be passed
			// seperately
			if (connect.getType() == AS400Connection.TYPE) {
				response = ((AS400Connection) connect).runAS400Program(
						cmdToExecute, args, argDelimiter);
			} else {
				StringBuffer command = new StringBuffer();
				command.append(cmdToExecute);
				if (null != args && args.length > 0) {
					for (int i = 0; i < args.length; i++) {
						if (null != args[i] && args[i].length() > 0) {
							command.append(argDelimiter);
							command.append(args[i]);
						}
					}
				}
				response = connect.runCommand(command.toString());
			}
		} else {
			logproxy.error(MessageHelper.getMsgResource().getMessage(
					MsgIds.REMOTE_CONNECT_ERROR));
		}
		return response;
	}

	/**
	 * Create a random directory on the target machine. Can be used to store
	 * temporary files.
	 * 
	 * @param path
	 *            The path to the directory under which the random directory is
	 *            to be created
	 * @return String Representing the complete path to the random directory
	 *         that was created
	 * @throws GeneralCLFCException
	 *             If random directory creation fails
	 */
	public String getRandomDir(String path) throws GeneralCLFCException {
		return connect.createRandomDir(path);
	}

	/**
	 * Transfer file localStdin to remoteStdin.
	 * 
	 * @param local
	 *            Path to standard input source file on local machine
	 * @param remote
	 *            Path to standard input destination file on target machine
	 * @throws GeneralCLFCException
	 *             If file transfer is unsuccessful
	 */
	public void transferFile(String local, String remote)
			throws GeneralCLFCException {
		connect.transferFile(local, remote);
	}

	/**
	 * Remove a file/directory from the target
	 * 
	 * @param dir
	 *            Path to the file/folder to be removed on target machine
	 * @throws GeneralCLFCException
	 *             If delete operation is unsuccessful
	 */
	public void removeDir(String dir) throws GeneralCLFCException {
		connect.removeDir(dir);
	}

	/**
	 * Return the correct path separator for the target system.
	 * 
	 * @return char path separator on target machine
	 * @throws RemoteConnectException
	 *             If problems encountered obtaining the OS Separator.
	 */
	public char getOSSeparator() throws RemoteConnectException {
		return connect.getOSSeparator();
	}

	/**
	 * Close the connection to the target
	 */
	public void close() {
		connect.endSession();
	}

	/**
	 * Initialize the connection with the target machine using the specified
	 * configuration properties @return boolean representing the success of the
	 * connection @throws RemoteConnectException
	 * 
	 * @return true if connection is initialized
	 * 
	 * @throws RemoteConnectException
	 * @throws ParamException
	 */
	private boolean initRXAClientConnection() throws RemoteConnectException,
			ParamException {
		if ((rxaProps.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
				.equalsIgnoreCase(SelectProtocol.TYPE)) {
			// use protocol selector
			SelectProtocol sp = new SelectProtocol(rxaProps, logproxy);
			connect = sp.findProtocol();
		} else {
			if ((rxaProps.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equalsIgnoreCase(RexecConnection.TYPE)) {
				// create REXEC connection
				connect = new RexecConnection(logproxy);
			} else if ((rxaProps
					.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equalsIgnoreCase(RSHConnection.TYPE)) {
				// create RSH connection
				connect = new RSHConnection(logproxy);
			} else if ((rxaProps
					.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equalsIgnoreCase(SSHConnection.TYPE)) {
				// create SSH connection
				connect = new SSHConnection(logproxy);
			} else if ((rxaProps
					.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equalsIgnoreCase(WinConnection.TYPE)) {
				// create Windows connection
				connect = new WinConnection(logproxy);
			} else if ((rxaProps
					.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equalsIgnoreCase(AS400Connection.TYPE)) {
				// create Windows connection
				connect = new AS400Connection(logproxy);
			} else {
				throw ExceptionFactory.createParamException(
						MsgIds.INVALIDCONNTYPE, logproxy);
			}
			connect.initializeProps(rxaProps);
			connect.beginSession();
		}
		if (connect.getRXAProtocol() == null) {
			logproxy.error(MessageHelper.getMsgResource().getMessage(
					MsgIds.CONNECTION_UNSUCCESSFUL));
		}
		return (connect.getRXAProtocol() != null);
	}

	/**
	 * Ensure that the required properties have been provided for the selected
	 * connection type
	 * 
	 * @return boolean value representing whether all required properties have
	 *         been configured
	 */
	private boolean checkRequiredProps() {
		/*
		 * The hostname and a username must always be provided
		 */
		if ((rxaProps.containsKey(RemoteCmdLineFC.PARAM_CONFIG_HOST))
				&& (rxaProps.containsKey(RemoteCmdLineFC.PARAM_CONFIG_USER))) {
			/*
			 * In order to create the REXEC or Windows connection using the
			 * provided hostname and username a password must also be provided.
			 * On the other hand, RSH connections do not require a password. For
			 * SSH connections a password may be provided or the user may
			 * provide a keystore & passphrase or elect to use a keypair.
			 */
			if ((rxaProps.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equals(SSHConnection.TYPE)) {
				/*
				 * For SSH connections, either a password or keystore must be
				 * provided.
				 */
				if ((rxaProps.containsKey(RemoteCmdLineFC.PARAM_CONFIG_PASSWD))
						|| (rxaProps
								.containsKey(RemoteCmdLineFC.PARAM_CONFIG_KEYSTORE))) {
					return true;
				}
			}
			/*
			 * For RSH connections only the hostname and username are required
			 */
			else if (((rxaProps
					.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
					.equals(RSHConnection.TYPE))
					|| ((rxaProps
							.getProperty(RemoteCmdLineFC.PARAM_CONFIG_CONNTYPE))
							.equals(SelectProtocol.TYPE))) {
				return true;
			} else {
				/*
				 * For AS400, REXEC and Windows connections, a password must be
				 * provided.
				 */
				if (rxaProps.containsKey(RemoteCmdLineFC.PARAM_CONFIG_PASSWD)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Constructs a single String that represents the command and all its
	 * arguments seperated with the specified delimiter.
	 * 
	 * @param cmdToExecute
	 *            The command to be executed
	 * @param args
	 *            The command arguments as a String Array
	 * @param argDelimiter
	 *            The command argument delimiter Return the connection object
	 *            accessing the target machine
	 * @return RemoteAccess RXA Connection object
	 */
	public String buildSingleCommand(String cmdToExecute, String[] args,
			String argDelimiter) {
		if (null == args || args.length < 1) {
			return cmdToExecute;
		} else {
			StringBuffer appendedArgs = new StringBuffer(cmdToExecute);
			for (int i = 0; i < args.length; i++) {
				appendedArgs.append(argDelimiter + args[i]);
			}
			return appendedArgs.toString();
		}
	}

	/**
	 * Return the connection object accessing the target machine
	 * 
	 * @return RemoteAccess RXA Connection object
	 */
	public RemoteAccess getRXAProtocol() {
		return connect.getRXAProtocol();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExecutorCmdArgsEncoding(String enc) {
		connect.setCmdArgsCharEncode(enc);
	}
}
