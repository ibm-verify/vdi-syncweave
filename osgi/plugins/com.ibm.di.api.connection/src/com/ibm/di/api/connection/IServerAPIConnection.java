/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.EventListener;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.RemoteListener;
import com.ibm.di.api.remote.SessionFactory;

/**
 * This class is representing a connection to a Tivoli Directory Integrator
 * Server. It abstracts away the details of obtaining a {@link SessionFactory}
 * and exporting {@link RemoteListener}s.
 * 
 * @since 7.2
 */
public interface IServerAPIConnection {

	/**
	 * @return the contained SessionFactory for this instance.
	 * @throws NotBoundException
	 *             if the SessionFactory is not bound to the remote registry.
	 * @throws RemoteException
	 *             if an error occurs while communicating with the remote
	 *             server.
	 * @throws DIException
	 */
	public SessionFactory getSessionFactory() throws RemoteException, NotBoundException, DIException;

	/**
	 * This method prepares the user's implementation of the
	 * {@link EventListener} to be accessed by the remote server.
	 * 
	 * @param listener
	 *            the listener to export
	 * @param useSSL
	 *            specifies whether SSL should be used
	 * @param aUseCustomProperties
	 *            specifies whether to use the custom SSL properties and those
	 *            defined by the JVM.
	 * @return the exported listener
	 * @throws DIException
	 *             if an error occurs while exporting the listener
	 * @throws RemoteException
	 *             if an error occurs while communicating with the remote
	 *             server.
	 */
	public <L extends RemoteListener> L export(L listener, boolean useSSL, boolean aUseCustomProperties) throws DIException,
			RemoteException;
}
