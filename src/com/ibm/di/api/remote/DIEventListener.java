/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.EventListener;

/**
 * 
 * This is a base EventListener for Server API events.
 * 
 */
public interface DIEventListener extends RemoteListener {

	/**
	 * Handles a specified event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void handleEvent(DIEvent aEvent) throws DIException, RemoteException;

}
