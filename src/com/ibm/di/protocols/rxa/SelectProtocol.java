/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import java.net.ConnectException;
import java.util.Properties;

import com.ibm.tivoli.remoteaccess.RemoteAccess;

/**
 * Connect to the target machine using any protocol that is available.
 */
public class SelectProtocol {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Connection to remote target
	 */
	private RemoteAccess rem = null;

	/**
	 * Connection object
	 */
	private Connection connection = null;

	/*
	 * Configuration parameters
	 */

	/**
	 * The hostname (address) of the target machine
	 */
	protected String hostName;

	/**
	 * The name of a user
	 */
	protected String userName;

	/**
	 * The password for the user
	 */
	protected byte[] password;

	/**
	 * The passphrase that protects your private key
	 */
	protected byte[] passphrase;

	/**
	 * Full path to the file containing the keystore
	 */
	protected String keystore;

	/**
	 * Path to the Source file
	 */
	protected String sourcefile = null;

	/**
	 * The port to use to connect to the target machine
	 */
	protected int port = 0;

	/**
	 * Properties to be used for connection initialization
	 */
	private Properties props;

	/**
	 * Log proxy to be used for logging
	 */
	private LogProxy lp;

	/**
	 * Position number in connections list
	 */
	private int posn = 0;

	/**
	 * List of connections
	 */
	private Connection[] plist = null;

	/**
	 * List of all available protocols which can make successful connection
	 */
	private String[] protocolList;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "ANY";

	/**
	 * Construct a SelectProtocol object with the provided properties
	 * 
	 * @param p
	 *            Properties object containing the configured attribute values
	 * @param log
	 *            LogProxy object to be used for logging
	 */
	public SelectProtocol(Properties p, LogProxy log) {
		lp = log;
		props = p;
		hostName = p.getProperty(ConnectionImpl.HOSTNAME);
		userName = p.getProperty(ConnectionImpl.USERNAME);
		if (p.getProperty(ConnectionImpl.PORT) != null) {
			port = Integer.parseInt(p.getProperty(ConnectionImpl.PORT));
		}
		if (p.getProperty(ConnectionImpl.PASSWORD) != null) {
			password = (p.getProperty(ConnectionImpl.PASSWORD)).getBytes();
		}
		if (p.getProperty(ConnectionImpl.KEYSTORE) != null) {
			keystore = p.getProperty(ConnectionImpl.KEYSTORE);
		}
		if (p.getProperty(ConnectionImpl.PASSPHRASE) != null) {
			passphrase = (p.getProperty(ConnectionImpl.PASSPHRASE)).getBytes();
		}

		if (p.getProperty(ConnectionImpl.SOURCEPATH) != null) {
			sourcefile = p.getProperty(ConnectionImpl.SOURCEPATH);
		}
	}

	/**
	 * Find a suitable protocol that is available on the target machine and
	 * create a connection
	 * 
	 * @return Connection RXA Connection object
	 * @throws RemoteConnectException
	 *             If a connection cannot be established
	 */
	public Connection findProtocol() throws RemoteConnectException {

		return this.findProtocol(false);
	}

	/**
	 * Find all suitable protocols that is available on the target machine if
	 * checkAll is true; otherwise stops at first suitable protocol
	 * 
	 * @param checkAll
	 *            checks if all suitable protocols needs to be found or only
	 *            first succcessful protocol
	 * @return Connection RXA Connection object
	 * @throws RemoteConnectException
	 */
	public Connection findProtocol(boolean checkAll)
			throws RemoteConnectException {
		/*
		 * The list of protocols to attempt.
		 */
		plist = checkPropsProvided();
		protocolList = new String[plist.length];
		lp.debug(MessageHelper.getMsgResource().getMessage(
				MsgIds.FINDING_PROTOCOL));
		/*
		 * Self implemented selectProtocol method Attempt to establish a
		 * connection using the possible protocols given the provided attributes
		 */
		int i = 0;
		while ((posn < plist.length)) {
			connection = plist[posn];
			connection.initializeProps(props);
			try {
				rem = connection.beginSession();

				if (rem != null) {
					String p = connection.getType();
					if (p != null)
						protocolList[i++] = p;
					if (!checkAll) {

						if (null != sourcefile) {
							try {
								if (rem.exists(sourcefile)) {
									/*
									 * A suitable protocol was found and a
									 * connection was established
									 */
									lp.debug(MessageHelper.getMsgResource()
											.getMessage(
													MsgIds.PROTOCOL_CHOSEN,
													new Object[] { connection
															.getType() }));

									return connection;
								}
							} catch (ConnectException e) {
								rem = null;
								connection = null;
							}
						} else {

							/*
							 * A suitable protocol was found and a connection
							 * was established
							 */
							lp.debug(MessageHelper.getMsgResource().getMessage(
									MsgIds.PROTOCOL_CHOSEN,
									new Object[] { connection.getType() }));

							return connection;
						}
					}
				}
			} catch (RemoteConnectException ce) {
				rem = null;
				connection = null;
			}
			posn++;
		}

		/*
		 * Check if a connection was created
		 */
		if (rem == null) {
			/*
			 * No connection could be established
			 */
			throw ExceptionFactory.createRemoteConnectException(
					MsgIds.NO_SUITABLE_PROTOCOL, lp);
		}

		return connection;
	}

	/**
	 * Finds all available protocols that can make a successful connection to
	 * target machine using given connection parameters
	 * 
	 * @return List of All suitable protocols using which a successful
	 *         connection can be made
	 */

	public String[] getAllProtocols() {
		try {
			findProtocol(true);
		} catch (RemoteConnectException e) {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.REMOTE_CONNECT_ERROR));
		}
		
		String[] list = protocolList;

		return list;
	}

	/**
	 * Check what properties have been configured and determine what possible
	 * protocols can be attempted with the supplied properties. @return
	 * Connection[] Array of possible connection objects
	 * 
	 * @return the Connection array
	 */
	private Connection[] checkPropsProvided() {
		Connection[] availableProtocols = null;

		if (props.containsKey(ConnectionImpl.PASSWORD)) {
			/*
			 * The password has been provided along with the username and
			 * hostname. Therefore, sufficient parameters have been provided to
			 * attempt all the connection types.
			 */
			availableProtocols = new Connection[5];
			Connection ssh = new SSHConnection(lp);
			Connection rsh = new RSHConnection(lp);
			Connection win = new WinConnection(lp);
			Connection rexec = new RexecConnection(lp);
			Connection as400 = new AS400Connection(lp);
			availableProtocols[0] = ssh;
			availableProtocols[1] = rsh;
			availableProtocols[2] = rexec;
			availableProtocols[3] = win;
			availableProtocols[4] = as400;
		} else {
			/*
			 * If no password has been provided then it restricts the possible
			 * protocols to SSH and RSH.
			 */
			if (props.containsKey(ConnectionImpl.KEYSTORE)) {
				/*
				 * If a keystore has been provided then an SSH connection should
				 * be attempted along with an RSH connection.
				 */
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.EITHER_SSH_OR_RSH));
				availableProtocols = new Connection[2];
				Connection ssh = new SSHConnection(lp);
				Connection rsh = new RSHConnection(lp);
				availableProtocols[0] = ssh;
				availableProtocols[1] = rsh;
			} else {
				/*
				 * Only an RSH connection is possible since no password or
				 * keystore has been provided.
				 */
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.RSH_ONLY));
				availableProtocols = new Connection[1];
				Connection rsh = new RSHConnection(lp);
				availableProtocols[0] = rsh;
			}
		}
		return availableProtocols;
	}
}
