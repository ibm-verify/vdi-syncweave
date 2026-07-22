/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.filetransferfc;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.protocols.rxa.AS400Connection;
import com.ibm.di.protocols.rxa.Connection;
import com.ibm.di.protocols.rxa.LogProxy;
import com.ibm.di.protocols.rxa.LogProxyImpl;
import com.ibm.di.protocols.rxa.ParamException;
import com.ibm.di.protocols.rxa.RSHConnection;
import com.ibm.di.protocols.rxa.RemoteConnectException;
import com.ibm.di.protocols.rxa.RexecConnection;
import com.ibm.di.protocols.rxa.SSHConnection;
import com.ibm.di.protocols.rxa.SelectProtocol;
import com.ibm.di.protocols.rxa.WinConnection;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * The File Transfer Operator that connects and Transfer Files to a target
 * machine
 */
public class FileTransferOperator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The properties file containing messages.
	 */
	private static final String PROPERTIES_FILE = "filetransferfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * Configuration properties for the initialization of the connection
	 */
	private Properties Props;

	/**
	 * Logger to be used for Non RXA connection related logging
	 */
	private Log logger;

	/**
	 * The way to perform RXA connection related logging
	 */
	private LogProxy logProxy;

	/**
	 * The RXA connection object used to connect to the target machine.
	 */
	private Connection connect = null;

	/**
	 * The FTP connection object used to connect to the target machine.
	 */
	private FTPConnection ftpconnect = null;

	/**
	 * Constructor for the FileTransferAgent
	 * 
	 * @param lp
	 *            The LogProxy for logging
	 * @param p
	 *            The configuration properties for the initialization of the
	 *            connection
	 * 
	 */
	public FileTransferOperator(Properties p, Log log) {
		logProxy = new LogProxyImpl(log);
		logger = log;
		Props = p;
	}

	/**
	 * Creates a connection with the target machine
	 * 
	 * @return boolean value representing the success of the connection
	 * @throws RemoteConnectException
	 * @throws ParamException
	 */
	public boolean prepareConnection() throws RemoteConnectException,
			ParamException, Exception {
		if (!checkRequiredProps()) {
			throw new Exception(
					resHash.getString("FILE.TRANSFER.FC.MISSING.PARAMS"));
		}

		if (Props.getProperty(FileTransferFC.PROTOCOL).equals(
				SelectProtocol.TYPE)) {
			try {
				return initRXAClientConnection();
			} catch (Exception e) {
				return initFTPClientConnection();
			}
		} else if (Props.getProperty(FileTransferFC.PROTOCOL).equals(
				FTPConnection.TYPE)) {
			return initFTPClientConnection();
		} else {
			return initRXAClientConnection();
		}

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
		 * The Hostname and a Username must always be provided
		 */
		if ((Props.containsKey(FileTransferFC.HOSTNAME))
				&& (Props.containsKey(FileTransferFC.USERNAME))) {

			/*
			 * For SSH connections a password may be provided or the user may
			 * provide a keystore & passphrase or elect to use a keypair.
			 * 
			 * On the other hand, RSH connections do not require a password.
			 * 
			 * In order to create the FTP/REXEC/Windows/AS400 connection using
			 * the provided hostname and username a password must also be
			 * provided.
			 */
			if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equals(SSHConnection.TYPE)) {
				/*
				 * For SSH connections, either a password or keystore must be
				 * provided.
				 */
				if ((Props.containsKey(FileTransferFC.PASSWORD))
						|| (Props.containsKey(FileTransferFC.KEYSTORE))) {
					return true;
				}
			}
			/*
			 * RSH connections do not require a password
			 */
			else if (((Props.getProperty(FileTransferFC.PROTOCOL))
					.equals(RSHConnection.TYPE))
					|| ((Props.getProperty(FileTransferFC.PROTOCOL))
							.equals(SelectProtocol.TYPE))) {
				return true;
			} else {
				/*
				 * For FTP, AS400, REXEC and Windows connections, a password
				 * must be provided.
				 */
				if (Props.containsKey(FileTransferFC.PASSWORD)) {

					return true;
				}
			}
		}
		return false;
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
			ParamException, Exception {
		if ((Props.getProperty(FileTransferFC.PROTOCOL))
				.equalsIgnoreCase(SelectProtocol.TYPE)) {
			// use protocol selector
			SelectProtocol sp = new SelectProtocol(Props, logProxy);
			connect = sp.findProtocol();
		} else {
			if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equalsIgnoreCase(RexecConnection.TYPE)) {
				// create REXEC connection
				connect = new RexecConnection(logProxy);
			} else if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equalsIgnoreCase(RSHConnection.TYPE)) {
				// create RSH connection
				connect = new RSHConnection(logProxy);
			} else if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equalsIgnoreCase(SSHConnection.TYPE)) {
				// create SSH connection
				connect = new SSHConnection(logProxy);
			} else if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equalsIgnoreCase(WinConnection.TYPE)) {
				// create Windows connection
				connect = new WinConnection(logProxy);
			} else if ((Props.getProperty(FileTransferFC.PROTOCOL))
					.equalsIgnoreCase(AS400Connection.TYPE)) {
				// create AS400 connection
				connect = new AS400Connection(logProxy);
			} else {
				throw new Exception(
						resHash.getString("FILE.TRANSFER.FC.INVALID.CONN.TYPE"));
			}
			connect.initializeProps(Props);
			connect.beginSession();
		}

		// Setting ftpconnect to null when successful connection could be
		// established using any of the RXA protocol.
		if (connect.getRXAProtocol() != null)
			ftpconnect = null;
		return (connect.getRXAProtocol() != null);
	}

	/**
	 * Initialize the FTP connection with the target machine using the specified
	 * configuration properties
	 * 
	 * @return boolean representing the success/failure of the connection
	 * @throws Exception
	 * 
	 */
	private boolean initFTPClientConnection() throws Exception {

		try {
			ftpconnect = new FTPConnection();
			ftpconnect.initializeProps(Props);
			ftpconnect.beginSession();
		} catch (Exception e) {
			throw new Exception(
					resHash.getString("FILE.TRANSFER.FC.CONNECTION.UNSUCCESSFUL"));
		}

		return (ftpconnect != null && ftpconnect.getFTPProtocol() != null);
	}

	/**
	 * Transfers file from source to target
	 * 
	 * @param source
	 *            Path to source file
	 * @param target
	 *            Path to destination file on target machine
	 * @throws Exception
	 *             If file transfer is unsuccessful
	 */
	public void transferFile(String source, String target) throws Exception {
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null) {
			ftpconnect.transferFile(source, target);
		} else
			connect.transferFile(source, target);
	}

	/**
	 * Receives a file from target to source
	 * 
	 * @param source
	 *            Path to source file
	 * @param target
	 *            Path to destination file on local machine
	 * @throws Exception
	 *             If file transfer is unsuccessful
	 */
	public void receiveFile(String source, String target) throws Exception {
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null) {
			ftpconnect.receiveFile(source, target);
		} else
			connect.receiveFile(source, target);
	}

	/**
	 * Create a directory on the target machine
	 * 
	 * @param dir
	 *            Path of the folder to be created
	 * 
	 * @throws Exception
	 *             If the directory creation is not successful.
	 */
	public void createDirectory(String dir) throws Exception {
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null) {
			ftpconnect.createDir(dir);
		} else
			connect.createDir(dir);
	}

	/**
	 * Checks the given file exists or not on remote machine
	 * 
	 * @param filePath
	 *            Path of the file
	 * @return true if the given file exists on remote machine otherwise false
	 * @throws Exception
	 */
	public boolean isExists(String filePath) throws Exception {
		boolean isExist = false;
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null)
			isExist = ftpconnect.isExists(filePath);
		else if (connect != null && connect.getRXAProtocol() != null)
			isExist = connect.isExists(filePath);
		return isExist;
	}

	/**
	 * Checks the given file exists or not on remote machine
	 * 
	 * @param path
	 *            Path of the file
	 * @return true if the given file exists on remote machine otherwise false
	 * @throws Exception
	 */
	public String[] list(String path) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null)
			list = ftpconnect.list(path);
		else if (connect != null && connect.getRXAProtocol() != null)
			list = connect.list(path);

		String[] finalList = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			finalList[i] = list.get(i);
		return finalList;
	}

	/**
	 * Closes the connection to the target
	 */
	public void close() {
		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null)
			ftpconnect.endSession();
		else if (connect != null && connect.getRXAProtocol() != null)
			connect.endSession();
	}

	/**
	 * Iterates through all the protocols and tries to make a connection using
	 * each of them with the specified configuration
	 * 
	 * @return String array of the protocols through which a successful
	 *         connection could be made.
	 */

	public Vector<String> getProtocolList() {

		// Use RXA protocol selector
		SelectProtocol sp = new SelectProtocol(Props, logProxy);
		String[] rxaProtocolList = sp.getAllProtocols();

		ftpconnect = new FTPConnection();
		try {
			ftpconnect.initializeProps(Props);
			ftpconnect.beginSession();
		} catch (Exception e) {
			ftpconnect.endSession();
		}

		Vector<String> v = new Vector<String>();

		if (ftpconnect != null && ftpconnect.getFTPProtocol() != null) {
			if (rxaProtocolList != null && rxaProtocolList.length != 0) {
				for (int i = 0; i < rxaProtocolList.length + 1; i++) {
					if (i != rxaProtocolList.length) {
						if (rxaProtocolList[i] != null)
							v.add(rxaProtocolList[i]);
					} else
						v.add(ftpconnect.getType());

				}
			} else {
				v.add(ftpconnect.getType());
			}

		} else if (rxaProtocolList != null && rxaProtocolList.length != 0) {
			for (int i = 0; i < rxaProtocolList.length; i++) {
				if (rxaProtocolList[i] != null)
					v.add(rxaProtocolList[i]);
			}
		}

		close();
		return v;

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
			String s = resHash.getString(msgKey);
			logger.debug(s);
		} else if (params.length == 1) {
			logger.debug(resHash.getString(msgKey, params[0]));
		} else {
			logger.debug(resHash.getString(msgKey, params));
		}
	}
}
