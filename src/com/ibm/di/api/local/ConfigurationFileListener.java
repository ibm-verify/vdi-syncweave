/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import java.rmi.RemoteException;
import java.util.EventListener;

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
public interface ConfigurationFileListener extends EventListener {

	/**
	 * Called to deliver the {@link ConfigEvent} that had occurred.
	 * 
	 * @param evt
	 *            the event object
	 * @throws RemoteException
	 *             - on communication error.
	 */
	public void handleEvent(ConfigEvent evt);
}
