/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.tivoli.remoteaccess.SSHProtocol;
import com.ibm.tivoli.remoteaccess.RemoteAccessAuthException;
import com.ibm.tivoli.remoteaccess.RemoteAccess;
import java.net.ConnectException;
import java.io.File;

/**
 * This class encapsulates the RXA library's SSH Connection related objects
 */
public class SSHConnection extends ConnectionImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Instance of the SSH protocol
	 */
	private SSHProtocol sshProtocol = null;

	/**
	 * Number of connection types
	 */
	private static final int numConnectionTypes = 2;

	/**
	 * Keystore type connection
	 */
	private static final int keystoreConnection = 1;

	/**
	 * Password type connection
	 */
	private static final int passwdConnection = 2;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "SSH";

	/**
	 * To be used in logged messages
	 */
	protected Object[] args = { TYPE };

	/**
	 * Stores if a connection is successful
	 */
	private boolean connectSuccess = false;

	/**
	 * The exception thrown when attempting the connection
	 */
	private Throwable exc = null;

	/**
	 * SSHConnection Constructor
	 * 
	 * @param log
	 *            LogProxy for logging
	 */
	public SSHConnection(LogProxy log) {
		super(log);
	}

	/**
	 * Begin a session with the target machine using the SSH protocol
	 * 
	 * @return RemoteAccess The RXA connection object
	 * @throws RemoteConnectException
	 */
	public RemoteAccess beginSession() throws RemoteConnectException {

		int i = 1;
		/*
		 * Iterate through the possible types of SSH connections to attempt to
		 * create a connection until a connection has been established or all
		 * methods have been exhausted
		 */
		while ((connectSuccess == false) && (i <= numConnectionTypes)) {
			try {
				sshProtocol = initializeProtocol(i);
				if (sshProtocol != null) {
					if (port != 0) {
						sshProtocol.setPortNumber(port);
					}
					if (initial_timeout != 0)
						sshProtocol.setTimeout(initial_timeout);

					lp.debug(MessageHelper.getMsgResource().getMessage(
							MsgIds.SESSION_BEGIN, args));
					sshProtocol.beginSession();
					lp.debug(MessageHelper.getMsgResource().getMessage(
							MsgIds.SESSION_STARTED));
				}
			} catch (RemoteAccessAuthException e) {
				connectSuccess = false;
				sshProtocol = null;
				exc = e;
			} catch (ConnectException e) {
				connectSuccess = false;
				sshProtocol = null;
				exc = e;
			}
			i++;
		}
		if ((i > numConnectionTypes) && (connectSuccess == false)
				&& (exc != null)) {
			// Attempted all three types of connecting and still unsuccessful
			throw ExceptionFactory.createRemoteConnectException(exc, lp);
		}
		setRXAProtocol(sshProtocol);
		return sshProtocol;
	}

	/**
	 * Attempt to initialize an SSH connection @param type Type of connection.
	 * (1) Using keystore & passphrase, (2) Using password @return SSHProtocol
	 * RXA connection object created @throws RemoteConnectException If
	 * connection unsuccessful
	 * 
	 * @param type
	 *            the type of connection to be used (keystore/password)
	 * @return the SSHProtocol initialized
	 * 
	 * @throws RemoteConnectException
	 */
	private SSHProtocol initializeProtocol(int type)
			throws RemoteConnectException {
		switch (type) {
		case keystoreConnection:
			if ((keystore != null) && (passphrase != null)) {
				/*
				 * Connect using the provided keystore and passphrase
				 */
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.SSH_CONN_KEYSTORE));
				sshProtocol = new SSHProtocol(new File(keystore), userName,
						passphrase, hostName);
				connectSuccess = true;
			}
			break;
		case passwdConnection:
			if (password != null) {
				/*
				 * Connect using the provided password for the remote user
				 */
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.CREATING_CONNECTION,
						new Object[] { TYPE, hostName, userName }));
				sshProtocol = new SSHProtocol(userName, password, hostName);
				connectSuccess = true;
			}
			break;
		default:
			break;

		}
		return sshProtocol;
	}

	/**
	 * Return this connection type
	 * 
	 * @return String The connection protocol used for this connection.
	 */
	public String getType() {
		return TYPE;
	}
}
