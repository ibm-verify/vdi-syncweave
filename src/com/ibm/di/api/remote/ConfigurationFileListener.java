/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.RemoteException;

import com.ibm.di.api.ConfigEvent;

/**
 * This is a base EventListener for receiving Server API Configuration File
 * events.
 * <p>
 * In order to listen for events an implementation of this class should be
 * provided by the user. Then reference to this implementation could be
 * added/removed with some of these methods: <code>
 * {@link Session#addEventListener(ConfigurationFileListener)},
 * {@link Session#removeEventListener(ConfigurationFileListener)}
 * 
 * @since 7.2
 */
public interface ConfigurationFileListener extends RemoteListener {

	/**
	 * Called to deliver the {@link ConfigEvent} that had occurred.
	 * 
	 * @param evt
	 *            the event object
	 * @throws RemoteException
	 *             - on communication error.
	 */
	public void handleEvent(ConfigEvent evt) throws RemoteException;
}
