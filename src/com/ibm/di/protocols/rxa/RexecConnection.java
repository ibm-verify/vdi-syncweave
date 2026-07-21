/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.tivoli.remoteaccess.REXECProtocol;
import com.ibm.tivoli.remoteaccess.RemoteAccessAuthException;
import com.ibm.tivoli.remoteaccess.RemoteAccess;
import java.net.ConnectException;

/**
 * This class encapsulates the RXA library's REXEC Connection related objects
 */
public class RexecConnection extends ConnectionImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Instance of the REXEC Protocol
	 */
	private REXECProtocol rexecProtocol;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "REXEC";

	/**
	 * To be used in logged messages
	 */
	protected Object[] args = { TYPE };

	/**
	 * RexecConnection Constructor
	 * 
	 * @param lp
	 *            LogProxy for logging
	 */
	public RexecConnection(LogProxy lp) {
		super(lp);
	}

	/**
	 * Begin a session with the target machine using the REXEC protocol
	 * 
	 * @return RemoteAccess The RXA connection object
	 * @throws RemoteConnectException
	 */
	public RemoteAccess beginSession() throws RemoteConnectException {
		lp.debug(MessageHelper.getMsgResource().getMessage(
				MsgIds.CREATING_CONNECTION,
				new Object[] { TYPE, hostName, userName }));
		rexecProtocol = new REXECProtocol(userName, password, hostName);
		// Set the port if a port has been provided
		if (port != 0) {
			rexecProtocol.setPortNumber(port);
		}
		if (initial_timeout != 0)
			rexecProtocol.setTimeout(initial_timeout);

		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_BEGIN, args));
			rexecProtocol.beginSession();
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_STARTED));
		} catch (RemoteAccessAuthException e) {
			rexecProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		} catch (ConnectException e) {
			rexecProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		}
		setRXAProtocol(rexecProtocol);
		return rexecProtocol;
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
