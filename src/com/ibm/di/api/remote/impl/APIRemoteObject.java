/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class extending UnicastRemoteObject.
 * The purpose is to be able to specify the port number
 * used when creating ServerSockets.
 * @since 7.1
 *
 */

public class APIRemoteObject extends UnicastRemoteObject {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * For serialization purposes.
	 */
	private static final long serialVersionUID = 8943980897096922855L;
	
	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public APIRemoteObject() throws RemoteException {
		this(0, null, null);
	}

	/**
	 * Constructor specifying port number.
	 * @param port The port number.
	 * @throws RemoteException
	 */
	public APIRemoteObject(int port) throws RemoteException {
		this(port, null, null);
	}

	/**
	 * Constructor specifying port number and factories.
	 * @param port  The port number
	 * @param csf Client Socket Factory.
	 * @param ssf Server Socket Factory.
	 * @throws RemoteException
	 */
	public APIRemoteObject(int port, RMIClientSocketFactory csf,
			RMIServerSocketFactory ssf) throws RemoteException {
		super(port, csf, new PortPoolSocketFactory(ssf));
	}
}
