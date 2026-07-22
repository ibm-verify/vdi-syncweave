/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import java.util.ArrayList;
import java.util.Properties;

import com.ibm.di.entry.Entry;
import com.ibm.tivoli.remoteaccess.RemoteAccess;

/**
 * Represents a connection using the Remote Execution and Access Library
 */
public interface Connection {

	/**
	 * Return the name of the target machine
	 * 
	 * @return String hostname
	 */
	String getHost();

	/**
	 * Set the hostname of the target
	 * 
	 * @param h
	 *            Hostname of target machine
	 */
	void setHost(String h);

	/**
	 * Return the username being used to connect to the target
	 * 
	 * @return Username
	 */
	String getUser();

	/**
	 * Set the username being used to connect to the target
	 * 
	 * @param u
	 *            The name of the user
	 */
	void setUser(String u);

	/**
	 * Initialize the connection properties
	 * 
	 * @param p
	 *            The configurable attributes for the connection
	 */
	void initializeProps(Properties p);

	/**
	 * Begin a session with the target machine
	 * 
	 * @return RemoteAccess RXA connection object that is using a particular
	 *         protocol to connect to the target system
	 * @throws RemoteConnectException
	 *             Thrown if starting the session is unsuccessful
	 */
	RemoteAccess beginSession() throws RemoteConnectException;

	/**
	 * Execute the command on the target
	 * 
	 * @param cmd
	 *            The command to be executed
	 * @return Entry object containing the results from the command execution as
	 *         three attributes: command.out, command.error and
	 *         command.returnCode.
	 * @throws GeneralCLFCException
	 *             If errors are encountered when attempting to run the command
	 */
	Entry runCommand(String cmd) throws GeneralCLFCException;

	/**
	 * Return the character that the target uses to separate path information
	 * 
	 * @return char separator
	 * @throws RemoteConnectException
	 */
	char getOSSeparator() throws RemoteConnectException;

	/**
	 * Create a directory on the target machine
	 * 
	 * @param dir
	 *            The path to the folder to be created
	 * @throws GeneralCLFCException
	 *             if the folder cannot be successfully created on the target
	 */
	public void createDir(String dir) throws GeneralCLFCException;
	
	/**
	 * Remove a directory on the target machine
	 * 
	 * @param dir
	 *            The path to the folder to be deleted
	 * @throws GeneralCLFCException
	 *             if the folder cannot be successfully removed from the target
	 */
	void removeDir(String dir) throws GeneralCLFCException;

	/**
	 * Transfer the specified file from the local machine to a specified
	 * destination on the remote target
	 * 
	 * @param local
	 *            Path to the source file on the local machine
	 * @param remote
	 *            Path to where the file is to be stored on the remote machine
	 * @throws GeneralCLFCException
	 *             if the transfer fails
	 */
	void transferFile(String local, String remote) throws GeneralCLFCException;

	/**
	 * Receives the specified file from the specified target machine to local
	 * machine
	 * 
	 * @param remote
	 *            Path to the source file on the remote machine
	 * @param local
	 *            Path to where the file is to be stored on the local machine
	 * 
	 * @return result of the file transfer operation
	 * 
	 * @throws GeneralCLFCException
	 *             if the transfer fails
	 */
	void receiveFile(String remote, String local)
			throws GeneralCLFCException;
	
	/**
	 * Returns true/false to check a given file exists or not
	 * 
	 * @param path
	 * 			path of the file on the remote machine
	 * @throws GeneralCLFCException 
	 */
	public boolean isExists(String path) throws GeneralCLFCException;
	
	/**
	 * Retrieves the list of files
	 * 
	 * @param path
	 * 			path on the remote machine
	 * @throws GeneralCLFCException 
	 */
	public ArrayList<String> list(String path) throws GeneralCLFCException;

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
	String createRandomDir(String p) throws GeneralCLFCException,
			RemoteConnectException;

	/**
	 * End the session with the target machine
	 */
	void endSession();

	/**
	 * Return the internal RXA connection object
	 * 
	 * @return RemoteAccess The RXA connection object that is connected to the
	 *         target system
	 */
	RemoteAccess getRXAProtocol();

	/**
	 * Return the connection protocol used for this connection
	 * 
	 * @return String type of connection
	 */
	String getType();

	/**
	 * Sets encoding for the command arguments before executing the actual
	 * command
	 * 
	 * @param enc
	 *            encoding to be used
	 */
	void setCmdArgsCharEncode(String enc);

}
