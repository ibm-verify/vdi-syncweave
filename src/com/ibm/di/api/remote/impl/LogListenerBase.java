/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.impl.BindAddressPolicyImpl;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.api.remote.impl.rmi.SSLRMIClientSocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.ResourceHash;

/**
 * This utility class allows remote clients to register listeners in the Server
 * API. Remote listeners must be RMI server objects.
 * 
 * @since 7.0
 */
public class LogListenerBase extends APIRemoteObject implements LogListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -5045556238257990569L;

	/**
	 * The listener object in the local JVM.
	 */
	private transient LogListener localListener = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param localListener
	 *            a POJO listener object in the local JVM
	 * @param clientSF
	 *            client socket factory used for communication with this RMI
	 *            server object
	 * @param serverSF
	 *            server socket factory used for communication with this RMI
	 *            server object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private LogListenerBase(LogListener localListener, RMIClientSocketFactory clientSF, RMIServerSocketFactory serverSF)
			throws Exception, RemoteException {
		super(0, clientSF, serverSF);

		if (localListener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.11"));
		}

		this.localListener = localListener;
	}

	/**
	 * Creates and exports an RMI server object, which can be used as a remote
	 * call-back. The created server object delegates calls to the specified
	 * POJO.
	 * 
	 * @param localListener
	 *            a POJO listener object in the local JVM
	 * @param useSSL
	 *            if <code>true</code> SSL is used
	 * @return the RMI server object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static LogListenerBase createInstance(LogListener localListener, boolean useSSL) throws Exception, RemoteException {
		return createInstance(localListener, useSSL, Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES));
	}

	/**
	 * Creates and exports an RMI server object, which can be used as a remote
	 * call-back. The created server object delegates calls to the specified
	 * POJO.
	 * 
	 * @param localListener
	 *            a POJO listener object in the local JVM.
	 * @return the RMI server object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static LogListenerBase createInstance(LogListener localListener) throws Exception, RemoteException {
		return createInstance(localListener, false, false);
	}

	/**
	 * Creates and exports an RMI server object, which can be used as a remote
	 * call-back. The created server object delegates calls to the specified
	 * POJO.
	 * 
	 * @param localListener
	 *            a POJO listener object in the local JVM
	 * @param useSSL
	 *            if <code>true</code> SSL is used
	 * @param useCustomProperties
	 *            if <code>true</code> custom SSL properties are used
	 *            ('api.client.*'), otherwise the default JSSE properties are
	 *            used ('javax.net.ssl.*')
	 * @return the RMI server object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static LogListenerBase createInstance(LogListener localListener, boolean useSSL, boolean useCustomProperties)
			throws Exception, RemoteException {
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(System.getProperties());
		if (useSSL) {
			return new LogListenerBase(localListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(useCustomProperties,
					bindAddr));
		} else {
			return new LogListenerBase(localListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(bindAddr));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void messageLogged(String message) throws DIException, RemoteException {
		localListener.messageLogged(message);
	}
}
