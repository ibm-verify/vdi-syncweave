/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.filetransferfc;

import java.util.ArrayList;
import java.util.Properties;

import com.ibm.di.protocols.FTPClient;
import com.ibm.di.server.ResourceHash;

/**
 * This class encapsulates the FTPClient APIs Connection related objects
 */
public class FTPConnection {

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
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Array List for the list of files
	 */
	private ArrayList<String> fileList = new ArrayList<String>(); 
	/*
	 * Configuration variables for the connection
	 */

	/**
	 * The hostname (address) of the target machine.
	 */
	private String hostName;

	/**
	 * The name of a user with Administrative privileges on the target machine
	 */
	private String userName;

	/**
	 * The password for the user (specified as Remote User) on the target
	 * machine.
	 */
	private String password;

	/**
	 * Checks if the desired mode of file transfer is binary
	 */
	private boolean isBinary;
	
	/**
	 * Checks if the passive FTP mode needs to be used
	 */
	private boolean ftpPassive;
	
	/**
	 * The FTP Security option
	 */
	private String ftpSecurity;
	
	/**
	 * Checks if the SSL to be used on Control channel
	 */
	private boolean useSSLonControlChannel;
	
	/**
	 * Checks if the SSL to be used on Data channel
	 */
	private boolean	useSSLonDataChannel;
	
	/**
	 * Checks if Recursive search need to be performed
	 */
	private boolean recursive = false;
	
	/**
	 * Checks if Explicit FTP SSL to be used
	 */
	private boolean ftpExplicitModeSSL;

	/**
	 * The port to use to connect to the target machine
	 */
	private String port;
	
	/**
	 * The FTPClient object connecting to the target system
	 */
	private FTPClient ftp;
	
	
	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "FTP";

	/**
	 * Initialize the properties of the connection
	 * 
	 * @param p
	 *            Properties object containing the configurable connection
	 *            attributes
	 * @throws Exception
	 */
	public void initializeProps(Properties p) throws Exception {

		if (p.getProperty(FileTransferFC.HOSTNAME) != null)
			hostName = p.getProperty(FileTransferFC.HOSTNAME);
		else
			throw new Exception(resHash.getString("FILE.TRANSFER.FC.FTP.MISSING.PARAMS", FileTransferFC.HOSTNAME));

		if (p.getProperty(FileTransferFC.PASSWORD) != null) {
			password = p.getProperty(FileTransferFC.PASSWORD);
		} else
			throw new Exception(resHash.getString("FILE.TRANSFER.FC.FTP.MISSING.PARAMS", FileTransferFC.PASSWORD));

		if (p.getProperty(FileTransferFC.USERNAME) != null)
			userName = p.getProperty(FileTransferFC.USERNAME);
		else
			throw new Exception(resHash.getString("FILE.TRANSFER.FC.FTP.MISSING.PARAMS", FileTransferFC.USERNAME));

		ftpSecurity = p.getProperty(EndPoint.FTP_SECURITY);
		ftpExplicitModeSSL = Boolean.valueOf(p.getProperty(EndPoint.FTP_EXPLICIT_SSL))
		.booleanValue();		
		
		if (ftpSecurity.equals("Use SSL on control channel")) {
			useSSLonControlChannel = true;
			useSSLonDataChannel = false;
		} else if (ftpSecurity.equals("Use SSL on control and data channels")) {
			useSSLonControlChannel = true;
			useSSLonDataChannel = true;
		} else {
			useSSLonControlChannel = false;
			useSSLonDataChannel = false;
		}

		
		port = p.getProperty(FileTransferFC.PORT);
		
		if ((p.getProperty(EndPoint.FTP_TRANSFER_MODE) != null)) {
			if (p.getProperty(EndPoint.FTP_TRANSFER_MODE)
					.equalsIgnoreCase("ASCII"))
				isBinary = false;
			else
				isBinary = true;
		}
		
		ftpPassive = Boolean.valueOf(p.getProperty(EndPoint.FTP_PASSIVE))
		.booleanValue();
		
		recursive = Boolean.valueOf(p.getProperty(FileTransferFC.RECURSIVE_SEARCH))
		.booleanValue();
	}

	/**
	 * Begin a session with the target machine
	 * 
	 * @return FTPClient the FTP connection object connecting to the target
	 * @throws Exception
	 */
	public FTPClient beginSession() throws Exception {
		ftp = new FTPClient();
		
		try{
			if (port != null) {
				ftp.connect(hostName, Integer.parseInt(port),
						useSSLonControlChannel, useSSLonDataChannel, ftpExplicitModeSSL);
			} else {
				int defaultPort = 21;
				if (useSSLonControlChannel && !ftpExplicitModeSSL) {
					defaultPort = 990;
				}
				ftp.connect(hostName, defaultPort, useSSLonControlChannel,
						useSSLonDataChannel, ftpExplicitModeSSL);
			}
			
			ftp.login(userName, password);
			ftp.setUsePassive(ftpPassive);	
		} catch (Exception e){
			ftp = null;
			throw new Exception("FILE.TRANSFER.FC.CONNECTION.UNSUCCESSFUL");			
		}
		
		return ftp;
	}

	/**
	 * Return this connection type
	 * 
	 * @return String The connection protocol used for this connection.
	 */
	public String getType() {
		return TYPE;
	}

	/**
	 * End the session with the target machine
	 */
	public void endSession() {
		if (ftp != null) {
			ftp.disconnect();
		}
	}

	/**
	 * Transfers the specified file to target location
	 * 
	 * @param source
	 *            Path to the source file
	 * @param target
	 *            Path to where the file is to be stored on the target
	 * @throws Exception
	 */
	public void transferFile(String source, String target) throws Exception {
		ftp.putFile(source, target, isBinary);
	}

	/**
	 * Receive the specified file from target location to local machine
	 * 
	 * @param source
	 *            Path of the source file
	 * @param target
	 *            Path to where the file is to be stored on the local machine
	 * @throws Exception
	 */
	public void receiveFile(String source, String target) throws Exception {
		ftp.getFile(source, target, isBinary);
	}
	
	/**
	 * Transfers the specified file to target location
	 * 
	 * @param source
	 *            Path to the file
	 * @return true if the file exists on the remote machine otherwise false
	 * @throws Exception
	 */
	public boolean isExists(String source) throws Exception {
		try
		{
			if (ftp.list(source, false) != null)
				return true;
			else
				return false;
			}
		catch(Exception e)
		{
			if(e.getMessage().indexOf("Directory not found") > -1)
			{
				return false;
			}
			else
			{
				throw e;
			}
		}
	}
	
	/**
	 * Create a directory on the target machine
	 * 
	 * @param dir
	 *            Path of the folder to be created
	 *            
	 * @throws Exception
	 * 			  if the folder cannot be successfully created on the target
	 */
	public void createDir(String dir) throws Exception {
		if(!isExists(dir))
		{
			ftp.mkdir(dir);
		}
	}
	
	/**
	 * Retrieves the list of files
	 * 
	 * @param path
	 *            path on the remote machine
	 * @throws Exception 
	 */
	public ArrayList<String> list(String path) throws Exception {
		String[] file = ftp.dir(path, true);
		for(String files: file)
		{
			String[] fileName = files.split("\\s");
			if(!files.startsWith("d")){
				fileList.add(path + "/" + fileName[fileName.length-1]);
			}else if(recursive){
				list(path + "/" + fileName[fileName.length-1]);
			}
		}
		return fileList;
	}
	
//	/**
//	 * Retrieves the list of files
//	 * 
//	 * @param path
//	 *            path on the remote machine
//	 * @throws Exception 
//	 */
//	public String[] list(String path) throws Exception {
//		String[] file = ftp.dir(path, true);
//		for(String files: file)
//		String[] fileList = new String[file.length];
//		for (int i = 0; i < file.length; i++) {
//			if(!file[i].startsWith("d")){
//				String[] fileName = file[i].split("\\s");
//				fileList[i] = path + "/" + fileName[fileName.length-1];
//			}else{
//				return list(file[i]);
//			}
//		}
//		return fileList;
//	}

	/**
	 * Return the FTP Connection Object
	 * 
	 * @return FTP connection object accessing the target
	 */
	public FTPClient getFTPProtocol() {
		return ftp;
	}

}