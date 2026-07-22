/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.connection;

import com.ibm.di.api.DIException;

/**
 * This service provides the means to obtain a reference to an
 * {@link IServerAPIConnection} instance.
 * 
 * It allows access to the Local Server API if one is available in the current
 * JVM. The Local API will be exposed through the remote interfaces shipped by
 * Tivoli Directory Integrator. This allows clients to use only one interface
 * for communication with Tivoli Directory Integrator but make use of the local
 * API when needed.
 * 
 * @since 7.2
 */
public interface IServerAPIConnectionService {

	/**
	 * Create a connection to a remote Tivoli Directory Integrator server.
	 * 
	 * @param host
	 *            the hostname of the remote server.
	 * @param port
	 *            the port on which the RMI API is listening on.
	 * @return the connection instance.
	 * @throws DIException
	 *             if unable to obtain an instance of IServerAPIConnection
	 */
	public IServerAPIConnection getConnection(String host, int port) throws DIException;

	/**
	 * Creates a connection to the Tivoli Directory Integrator server running in
	 * this JVM.
	 * 
	 * @return the connection instance.
	 * @throws DIException
	 *             if unable to obtain an instance of IServerAPIConnection
	 */
	public IServerAPIConnection getConnection() throws DIException;

	/**
	 * Check if the specified connection is local.
	 * 
	 * @param conn
	 *            the connection previously created by this instance.
	 * @return true if the connection is local, false otherwise.
	 */
	public boolean isConnectionLocal(IServerAPIConnection conn);
}
