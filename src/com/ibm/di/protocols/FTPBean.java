/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// FTPBean.java
//
//
//
package com.ibm.di.protocols;

/**
 * This class helps to expose the functionality of the FTP Client through the
 * scripting environment.
 * 
 */
public class FTPBean {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The FTP client used for data and command transferring with the FTP
	 * server.
	 */
	private FTPClient ftp;

	/**
	 * Whether the files must be transferred as binary data or ASCII text.
	 */
	private boolean binary = false;

	/**
	 * This parameter holds the last exception that has occurred during the work
	 * of the FTP client.
	 */
	private Exception lastError;

	/**
	 * Determines whether to use detailed logging or not.
	 */
	private boolean debug = false;

	/**
	 * Constructor creating the FTP client object.
	 */
	public FTPBean() {
		ftp = new FTPClient();
	}

	/**
	 * Connects to the specified FTP server using the given credentials to
	 * login. No SSL security is used for the data and control channels created.
	 * 
	 * @param host
	 *            the host of the FTP server.
	 * @param username
	 *            the username used to login.
	 * @param password
	 *            the password used to login.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean connect(String host, String username, String password) {
		return connect(host, username, password, false, false);
	}

	/**
	 * Connects to the specified FTP server using the given credentials to
	 * login. The usage of SSL on the data and control channels is specified by
	 * the last two parameters.
	 * 
	 * @param host
	 *            the host of the FTP server.
	 * @param username
	 *            the username used to login.
	 * @param password
	 *            the password used to login.
	 * @param useSSLonCommandChannel
	 *            whether to use SSL on the control channel.
	 * @param useSSLonDataChannel
	 *            whether to use SSL on the data channel.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean connect(String host, String username, String password,
			boolean useSSLonCommandChannel, boolean useSSLonDataChannel) {
		try {
			if (ftp != null) {
				ftp.disconnect();
			}
			ftp = new FTPClient();
			if (getDebug())
				ftp.setDebug(true);
			int defaultPort = 21;
			if (useSSLonCommandChannel) {
				defaultPort = 990;
			}
			ftp.connect(host, defaultPort, useSSLonCommandChannel, useSSLonDataChannel);
			ftp.login(username, password);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Closes the connection of the FTP client to the server.
	 * 
	 * @return true if no exception occurred, otherwise false.
	 */
	public boolean close() {
		try {
			ftp.disconnect();
			ftp = null;
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Sets binary transfer mode.
	 */
	public void setBinary() {
		binary = true;
	}

	/**
	 * Sets ASCII transfer mode.
	 */
	public void setAscii() {
		binary = false;
	}

	/**
	 * Gets a file from the FTP server.
	 * 
	 * @param remoteFile
	 *            the file to be retrieved from the server.
	 * @param localFile
	 *            the name used to save the file on the local machine.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean get(String remoteFile, String localFile) {
		try {
			ftp.getFile(remoteFile, localFile, binary);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Puts a local file on the FTP server.
	 * 
	 * @param localFile
	 *            the local file to be sent to the server.
	 * @param remoteFile
	 *            the name used for saving the file on the server.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean put(String localFile, String remoteFile) {
		try {
			ftp.putFile(localFile, remoteFile, binary);
			return setLastErr(null);
		} catch (Exception err) {
			return setLastErr(err);
		}
	}

	/**
	 * Changes the working directory.
	 * 
	 * @param path
	 *            the new working directory.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean cd(String path) {
		try {
			ftp.cwd(path);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Returns an object representing the contents of the current working
	 * directory on the FTP server.
	 * 
	 * @return the current working directory contents.
	 */
	public Object dir() {
		try {
			setLastErr(null);
			return new DirectoryContents(ftp.dir());
		} catch (Exception e) {
			setLastErr(e);
			return null;
		}
	}

	/**
	 * Deletes the given file/directory from the FTP server.
	 * 
	 * @param path
	 *            the path of the file/directory to be removed.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean remove(String path) {
		try {
			ftp.deleteFile(path);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
		}
	}

	/**
	 * Renames the given file/directory on the FTP server.
	 * 
	 * @param fromPath
	 *            old name.
	 * @param toPath
	 *            new name.
	 * @return true if no exception occurs, otherwise false.
	 */
	public boolean rename(String fromPath, String toPath) {
		try {
			ftp.rename(fromPath, toPath);
			return setLastErr(null);
		} catch (Exception e) {
			return setLastErr(e);
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

	/**
	 * Sets the debug level of the FTP client.
	 * 
	 * @param debug
	 *            if true detailed logging will be used, otherwise no.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
		if (ftp != null)
			ftp.setDebug(debug);
	}

	/**
	 * Gets the debug level that has been set.
	 * 
	 * @return true if detailed logging is used, otherwise false.
	 */
	public boolean getDebug() {
		return debug;
	}

	/**
	 * This class is a container in which the contents of a directory can be
	 * saved.
	 * 
	 */
	public static class DirectoryContents {
		/**
		 * Copyright information.
		 */
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * A list of the contents.
		 */
		private String[] list;

		/**
		 * A index used for iterating the list.
		 */
		private int index;

		/**
		 * Constructor.
		 * 
		 * @param list
		 *            a list of values used for initialization.
		 */
		public DirectoryContents(String[] list) {
			this.list = list;
			this.index = -1;
			if (list.length > 0 && list[0].startsWith("total"))
				this.index++;
		}

		/**
		 * Moves the index to the next position.
		 * 
		 * @return true if the end of the list has not been reached yet,
		 *         otherwise false.
		 */
		public boolean next() {
			index++;
			return (index < list.length);
		}

		/**
		 * Returns the name of the file/directory the index is currently
		 * pointing at.
		 * 
		 * @return the file/directory name.
		 */
		public String getName() {
			// lrwxrwxrwx 1 root root 22 Mar 14 2002 intranet -> /v
			String str = list[index];
			String filename = null;
			int counter = 0;
			boolean skip = false;
			boolean justSawSpace = true;
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == ' ' || str.charAt(i) == '\t') {
					if (!skip) {
						counter++;
						skip = true;
					}
					justSawSpace = true;
				} else {
					if (justSawSpace && ! str.substring(i).startsWith("->"))
						filename = str.substring(i);
					if (counter == 8)
						break;
					justSawSpace = false;
					skip = false;
				}
			}

			return filename;

		}

		/**
		 * Returns the type of the element (a file, directory, etc.).
		 * 
		 * @return the type.
		 */
		public int getType() {
			switch (list[index].charAt(0)) {
			case 'd':
				return 1;
			case '-':
				return 2;
			case 'l':
				return 3;
			default:
				return 4;
			}
		}

		/**
		 * Returns the symbolic representation of the element's type.
		 * 
		 * @return a string determining the element's type.
		 */
		public String getTypeString() {
			return list[index].substring(0, 1);
		}
	}

}
