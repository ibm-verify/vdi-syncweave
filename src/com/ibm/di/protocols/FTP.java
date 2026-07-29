/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This class implements the FTP functionality using a FTPClient object.
 */
public class FTP {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * An FTP client object.
	 */
	private FTPClient ftp = new FTPClient();

	/**
	 * This parameter holds the last exception that has occurred during the work
	 * of the FTP client.
	 */
	private Exception lastError;

	/**
	 * Constructor.
	 */
	public FTP() {
	}

	/**
	 * Connects to a given host on port 21.
	 * 
	 * @param host
	 *            the host of the FTP server.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean connect(String host) {
		return connect(host, 21);
	}

	/**
	 * Connects to a given host and port.
	 * 
	 * @param host
	 *            the host of the FTP server.
	 * @param port
	 *            the port to connect to.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean connect(String host, int port) {
		try {
			ftp.connect(host, port);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Logins the FTP client on the server with the given credentials.
	 * 
	 * @param username
	 *            the username used to login.
	 * @param password
	 *            the password used to login.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean login(String username, String password) {
		try {
			ftp.login(username, password);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Closes the connection to the FTP server.
	 * 
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean close() {
		try {
			ftp.disconnect();
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Sets the transfer mode to binary.
	 * 
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean setBinary() {
		try {
			ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Sets the transfer mode to ASCII.
	 * 
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean setAscii() {
		try {
			ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Gets a remote file from the FTP server.
	 * 
	 * @param remoteFile
	 *            a file to be copied.
	 * @param localFile
	 *            the name of the file used when saving it on the local machine.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean get(String remoteFile, String localFile) {
		try {
			FileOutputStream fos = new FileOutputStream(localFile);
			try {
				ftp.retrieveFile(remoteFile, fos);
			} finally {
				fos.close();
			}
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Puts a remote file on the FTP server.
	 * 
	 * @param remoteFile
	 *            the name used to save the file on the remote server.
	 * @param localFile
	 *            the file to be sent to the FTP server.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean put(String localFile, String remoteFile) {
		try {
			FileInputStream fis = new FileInputStream(localFile);
			try {
				ftp.storeFile(remoteFile, fis);
			} finally {
				fis.close();
			}
			return setLastErr(null);
		} catch (Exception err) {
			return setLastErr(err);
		}
	}

	/**
	 * Changes the current working directory on the FTP server.
	 * 
	 * @param path
	 *            the new directory.
	 * @return true if no error occurs, otherwise false.
	 */
	public boolean cd(String path) {
		try {
			ftp.changeWorkingDirectory(path);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Returns the FTP server response to a previously sent command.
	 * 
	 * @return the response returned to the FTP client.
	 */
	public String getResponse() {
		return ftp.getReplyString();
	}

	/**
	 * Lists the contents of the current working directory.
	 * 
	 * @return the directory contents.
	 */
	public Object[] dir() {
		try {
			Vector<Object> buf = new Vector<>();

            FTPFile[] files = ftp.listFiles(".");

            for (FTPFile file : files) {
                buf.add(file.getName());
            }

			return buf.toArray();
		} catch (Exception e) {
			setLastErr(e);
			return null;
		}
	}

	/**
	 * Sets the passed as parameter exception as the last exception of the FTP
	 * client so far.
	 * 
	 * @param e
	 *            an exception.
	 * @return true if the given exception is <code>null</code>, otherwise
	 *         false.
	 */
	public boolean setLastErr(Exception e) {
		lastError = e;
		return (e == null);
	}

	/**
	 * Returns the last exception occurred.
	 * 
	 * @return the last occurred exception.
	 */
	public Exception getLastError() {
		return lastError;
	}

}
