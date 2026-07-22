/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

import com.ibm.di.api.DIException;

/**
 * 
 * This listener listens for log events.
 * 
 */
public interface LogListener extends EventListener, Remote {

	/**
	 * Called right after the specified message is logged.
	 * 
	 * @param aMessage
	 *            the message text
	 * @throws DIException
	 *             If an error is encountered.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void messageLogged(String aMessage) throws DIException,
			RemoteException;

}
