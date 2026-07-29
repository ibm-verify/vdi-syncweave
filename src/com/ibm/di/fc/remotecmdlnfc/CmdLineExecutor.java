/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.remotecmdlnfc;

import com.ibm.di.entry.Entry;
import com.ibm.di.protocols.rxa.GeneralCLFCException;

/**
 * The object that connects to the target machine, executes the commands and
 * returns the results.
 */
public interface CmdLineExecutor {

	/**
	 * Execute the command on the target machine.
	 * 
	 * @param cmd
	 *            The command to be executed
	 * @return Entry object containing three output attributes: command.out,
	 *         command.error and command.returnCode.
	 * @throws GeneralCLFCException
	 *             When an exception occurs trying to execute the command
	 *             remotely.
	 */
	Entry executeCommand(String cmd) throws GeneralCLFCException;

	/**
	 * Execute the command on the target machine.
	 * 
	 * @param cmd
	 *            The command to be executed
	 * @param args
	 *            The command arguments as a String Array
	 * @param argDelimiter
	 *            The command argument delimiter
	 * @return Entry object containing three output attributes: command.out,
	 *         command.error and command.returnCode.
	 * @throws GeneralCLFCException
	 *             When an exception occurs trying to execute the command
	 *             remotely.
	 */
	Entry executeCommand(String cmd, String[] args, String argDelimiter)
			throws GeneralCLFCException;

	/**
	 * Create a connection with the target machine
	 * 
	 * @return boolean value representing the success of the connection
	 * @throws GeneralCLFCException
	 */
	boolean prepareConnection() throws GeneralCLFCException;

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
	String getRandomDir(String path) throws GeneralCLFCException;

	/**
	 * Transfer file localStdin to remoteStdin.
	 * 
	 * @param localStdin
	 *            Path to standard input source file on local machine
	 * @param remoteStdin
	 *            Path to standard input destination file on target machine
	 * @throws GeneralCLFCException
	 *             If file transfer is unsuccessful
	 */
	void transferFile(String localStdin, String remoteStdin)
			throws GeneralCLFCException;

	/**
	 * Remove a file/directory from the target
	 * 
	 * @param dirPath
	 *            Path to the file/folder to be removed on target machine
	 * @throws GeneralCLFCException
	 *             If delete operation is unsuccessful
	 */
	void removeDir(String dirPath) throws GeneralCLFCException;

	/**
	 * Return the correct path separator for the target system.
	 * 
	 * @return char path separator on target machine
	 * @throws GeneralCLFCException
	 *             If problems encountered obtaining the OS Separator.
	 */
	char getOSSeparator() throws GeneralCLFCException;

	/**
	 * Close the connection to the target
	 */
	void close();

	/**
	 * Sets encoding for the command arguments before executing the actual
	 * command
	 * 
	 * @param enc
	 *            encoding to be used
	 */
	void setExecutorCmdArgsEncoding(String enc);
}
