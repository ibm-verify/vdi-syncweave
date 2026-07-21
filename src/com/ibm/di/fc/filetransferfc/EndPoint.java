/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.filetransferfc;

import java.util.Properties;

import com.ibm.di.server.ResourceHash;

/**
 * This class represents a file transfer end point object configuration
 */
public class EndPoint {

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
	 * The property for FTP file transfer mode
	 */
	static final String FTP_TRANSFER_MODE = "ftpTransferMode";

	/**
	 * The property for Passive FTP file transfer mode
	 */
	static final String FTP_PASSIVE = "ftpPassive";

	/**
	 * The property for FTP Security option
	 */
	static final String FTP_SECURITY = "ftpSecurity";

	/**
	 * The property for FTP Explicit Mode SSL
	 */
	static final String FTP_EXPLICIT_SSL = "ftpExplicitModeSSL";

	/**
	 * Configuration properties of an end point
	 */
	public Properties props;

	/**
	 * String to signify Localhost
	 */
	private static final String LOCALHOST = "localhost";

	/**
	 * Constructor for an EndPoint object
	 * 
	 * @param isSrc
	 *            provide true if the end point is Source end point
	 */
	EndPoint() {
		props = new Properties();
	}

	/**
	 * Returns the protocol to connect to an end point
	 * 
	 * @return protocol to connect to an end point
	 */
	public String getProtocol() {
		return props.getProperty(FileTransferFC.PROTOCOL);
	}

	/**
	 * Sets the protocol to connect to an end point
	 * 
	 * @param p
	 *            protocol to connect to an end point
	 */
	public void setProtocol(String p) {
		props.setProperty(FileTransferFC.PROTOCOL, p);
	}

	/**
	 * Returns the hostname of an end point
	 * 
	 * @return hostname of an end point
	 */
	public String getHost() {
		String hostname = props.getProperty(FileTransferFC.HOSTNAME);
		if (hostname != null && hostname.length() != 0
				&& !hostname.equalsIgnoreCase(LOCALHOST)
				&& !hostname.equalsIgnoreCase(sResHash.getString(
				"FILE.TRANSFER.FC.LOCAL.IP")))
			return hostname;
		else
			return LOCALHOST;
	}

	/**
	 * Sets the hostname of an end point
	 * 
	 * @param host
	 *            hostname of an end point
	 */
	public void setHost(String host) {
		props.setProperty(FileTransferFC.HOSTNAME, host);
	}

	/**
	 * Returns the port number of an end point
	 * 
	 * @return port number of an end point
	 */
	public String getPort() {
		return props.getProperty(FileTransferFC.PORT);
	}

	/**
	 * Sets the port number of an end point
	 * 
	 * @param po
	 *            port number of an end point
	 */
	public void setPort(String po) {
		props.setProperty(FileTransferFC.PORT, po);
	}

	/**
	 * Returns the file path of an end point
	 * 
	 * @return file path of an end point
	 */
	public String getPath() {
		return props.getProperty(FileTransferFC.PATH);
	}

	/**
	 * Sets the path of an end point
	 * 
	 * @param path
	 *            path of an end point
	 */
	public void setPath(String path) {
		props.setProperty(FileTransferFC.PATH, path);
	}

	/**
	 * Returns the username of an end point
	 * 
	 * @return username of an end point
	 */
	public String getUser() {
		return props.getProperty(FileTransferFC.USERNAME);
	}

	/**
	 * Sets the username of an end point
	 * 
	 * @param user
	 *            username of an end point
	 */
	public void setUser(String user) {
		props.setProperty(FileTransferFC.USERNAME, user);
	}

	/**
	 * Returns the user password of an end point
	 * 
	 * @return user password of an end point
	 */
	public String getPassword() {
		return props.getProperty(FileTransferFC.PASSWORD);
	}

	/**
	 * Sets the user password of an end point
	 * 
	 * @param pass
	 *          user password of an end point
	 */
	public void setPassword(String pass) {
		props.setProperty(FileTransferFC.PASSWORD, pass);
	}

	/**
	 * Returns the keystore of an end point
	 * 
	 * @return keystore of an end point
	 */
	public String getKeystore() {
		return props.getProperty(FileTransferFC.KEYSTORE);
	}

	/**
	 * Sets the keystore of an end point
	 * 
	 * @param key
	 *        keystore of an end point
	 */
	public void setKeystore(String key) {
		props.setProperty(FileTransferFC.KEYSTORE, key);
	}

	/**
	 * Returns the passphrase of an end point
	 * 
	 * @return passphrase of an end point
	 */
	public String getPassphrase() {
		return props.getProperty(FileTransferFC.PASSPHRASE);
	}

	/**
	 * Sets the passphrase of an end point
	 * 
	 * @param passph
	 *            passphrase of an end point
	 */
	public void setPassphrase(String passph) {
		props.setProperty(FileTransferFC.PASSPHRASE, passph);
	}

	/**
	 * Returns the end point is Local/Remote
	 * 
	 * @return true if the end point is local
	 */
	public boolean isLocal() {
		boolean isLocal;
		String hostname = props.getProperty(FileTransferFC.HOSTNAME);
		if (null != hostname && hostname.length() != 0
				&& !hostname.equalsIgnoreCase(LOCALHOST)
				&& !hostname.equalsIgnoreCase(sResHash.getString(
				"FILE.TRANSFER.FC.LOCAL.IP")))
			
			isLocal = false;
		else
			isLocal = true;
		return isLocal;
	}
	
	/**
	 * Sets the path of the source file
	 * @param s
	 * 		Path of the source file
	 */
	public void setSourceFile(String s){
		props.setProperty(FileTransferFC.SOURCEPATH, s);
	}
}
