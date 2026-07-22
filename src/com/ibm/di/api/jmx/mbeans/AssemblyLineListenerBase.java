/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.impl.APIRemoteObject;
import com.ibm.di.api.remote.impl.BindAddressPolicyImpl;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.api.remote.impl.rmi.SSLRMIClientSocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.ResourceHash;

/**
 * This class is used to provide a bridge between custom defined listener Java
 * class and the Server API notification mechanism. The custom defined listener
 * Java class is not available on the Server side so it should be wrapped in
 * AssemblyLineListenerBase class.
 */
public class AssemblyLineListenerBase extends APIRemoteObject implements AssemblyLineListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID for deserialization.
	 */
	private static final long serialVersionUID = 2420397426677995275L;

	/**
	 * {@link AssemblyLineListener} instance.
	 */
	private AssemblyLineListener mListener = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Default constructor.
	 * 
	 * @param aListener
	 *            AssemblyLineListener instance.
	 * @throws Exception
	 *             if parameter is null.
	 */
	private AssemblyLineListenerBase(AssemblyLineListener aListener) throws Exception {
		super();

		if (aListener == null) {
			throw new Exception(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.1"));
		}

		mListener = aListener;
	}

	/**
	 * Class constructor.
	 * 
	 * @param aListener
	 *            {@link AssemblyLineListener}
	 * @param aClientSF
	 *            the client-side socket factory for making calls to the remote
	 *            object
	 * @param aServerSF
	 *            the server-side socket factory for receiving remote calls
	 * @throws Exception
	 *             if listener parameter is null.
	 */
	private AssemblyLineListenerBase(AssemblyLineListener aListener, RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws Exception {
		super(0, aClientSF, aServerSF);

		if (aListener == null) {
			throw new Exception(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.2"));
		}

		mListener = aListener;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineCycleDone(Entry aEntry) throws DIException, RemoteException {
		mListener.assemblyLineCycleDone(aEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineFinished() throws DIException, RemoteException {
		mListener.assemblyLineFinished();
	}

	/**
	 * {@inheritDoc}
	 */
	public void messageLogged(String aMessage) throws DIException, RemoteException {
		mListener.messageLogged(aMessage);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            AssemblyLineListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @return AssemblyLineListenerBase object
	 * @throws Exception
	 *             if error occurred while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static AssemblyLineListenerBase createInstance(AssemblyLineListener aListener, boolean aSSLon) throws Exception {
		return createInstance(aListener, aSSLon, Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES));
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            AssemblyLineListener object
	 * @return AssemblyLineListenerBase object
	 * @throws Exception
	 *             if error occurred while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static AssemblyLineListenerBase createInstance(AssemblyLineListener aListener) throws Exception {
		return createInstance(aListener, false, false);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            AssemblyLineListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @param aUseCustomProperties
	 *            if <code>true</code> user custom settings is used
	 * @return AssemblyLineListenerBase object
	 * @throws Exception
	 *             if error occurred while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static AssemblyLineListenerBase createInstance(AssemblyLineListener aListener, boolean aSSLon,
			boolean aUseCustomProperties) throws Exception {
		BindAddressPolicy bindaddr = new BindAddressPolicyImpl(System.getProperties());
		if (aSSLon) {
			return new AssemblyLineListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(aUseCustomProperties,
					bindaddr));
		} else {
			return new AssemblyLineListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(bindaddr));
		}
	}

}
