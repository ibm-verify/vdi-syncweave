/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import com.ibm.tivoli.remoteaccess.LocalWindowsProtocol;
import com.ibm.tivoli.remoteaccess.WindowsProtocol;
import com.ibm.tivoli.remoteaccess.RemoteAccessAuthException;
import com.ibm.tivoli.remoteaccess.RemoteAccess;
import java.net.ConnectException;

/**
 * This class encapsulates the RXA library's Win Connection related objects
 */
public class WinConnection extends ConnectionImpl {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Instance of Windows protocol
	 */
	private WindowsProtocol winProtocol;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "WIN";

	/**
	 * To be used in logged messages
	 */
	protected Object[] args = { TYPE };

	/**
	 * WinConnection Constructor
	 * 
	 * @param log
	 *            LogProxy for logging
	 */
	public WinConnection(LogProxy log) {
		super(log);
	}

	/**
	 * Begin a session with the target machine using the WIN protocol
	 * 
	 * @return RemoteAccess The RXA connection object
	 * @throws RemoteConnectException
	 */
	public RemoteAccess beginSession() throws RemoteConnectException {
		if(hostName == null || userName ==null){
			winProtocol = new LocalWindowsProtocol();
		}
		lp.debug(MessageHelper.getMsgResource().getMessage(
				MsgIds.CREATING_CONNECTION,
				new Object[] { TYPE, hostName, userName }));
		winProtocol = new WindowsProtocol(userName, password, hostName);
		if (port != 0) {
			lp.info(MessageHelper.getMsgResource().getMessage(
					MsgIds.WIN_NO_PORT_ALLOWED));
		}

		if (initial_timeout != 0)
			winProtocol.setTimeout(initial_timeout);
		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_BEGIN, args));
			winProtocol.beginSession();
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_STARTED));
		} catch (RemoteAccessAuthException e) {
			winProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		} catch (ConnectException e) {
			winProtocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		}
		setRXAProtocol(winProtocol);
		return winProtocol;
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
