/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.tivoli.remoteaccess.RSHProtocol;
import com.ibm.tivoli.remoteaccess.RemoteAccessAuthException;
import com.ibm.tivoli.remoteaccess.RemoteAccess;
import java.net.ConnectException;

/**
 * This class encapsulates the RXA library's RSH Connection related objects
 */
public class RSHConnection extends ConnectionImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Instance of the RSH Protocol
	 */
	private RSHProtocol rshProtocol;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "RSH";

	/**
	 * To be used in logged messages
	 */
	protected Object[] args = { TYPE };

	/**
	 * RSHConnection Constructor
	 * 
	 * @param log
	 *            LogProxy for logging
	 */
	public RSHConnection(LogProxy log) {
		super(log);
	}

	/**
	 * Begin a session with the target machine using the RSH protocol
	 * 
	 * @return RemoteAccess The RXA connection object
	 * @throws RemoteConnectException
	 */
	public RemoteAccess beginSession() throws RemoteConnectException {
		lp.debug(MessageHelper.getMsgResource().getMessage(
				MsgIds.CREATE_CONN_NO_PASSWD, args));
		rshProtocol = new RSHProtocol(userName, hostName);
		if (port != 0) {
			rshProtocol.setPortNumber(port);
		}
		if (initial_timeout != 0)
			rshProtocol.setTimeout(initial_timeout);
		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_BEGIN, args));
			rshProtocol.beginSession();
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_STARTED));
		} catch (RemoteAccessAuthException e) {
			rshProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		} catch (ConnectException e) {
			rshProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		}
		setRXAProtocol(rshProtocol);
		return rshProtocol;
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
