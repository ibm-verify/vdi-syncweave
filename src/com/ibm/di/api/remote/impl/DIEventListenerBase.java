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
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.impl.BindAddressPolicyImpl;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.api.remote.impl.rmi.SSLRMIClientSocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements EventListener for Server API events.
 */
public class DIEventListenerBase extends APIRemoteObject implements DIEventListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 4143805973736693970L;

	/**
	 * DIEventListener object
	 */
	private transient DIEventListener mListener = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aListener
	 *            the DIEventListener object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private DIEventListenerBase(DIEventListener aListener) throws DIException, RemoteException {
		super();

		if (aListener == null) {
			throw new DIException(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.7"));
		}

		mListener = aListener;
	}

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aListener
	 *            the DIEventListener object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private DIEventListenerBase(DIEventListener aListener, RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF)
			throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (aListener == null) {
			throw new DIException(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.8"));
		}

		mListener = aListener;
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(DIEvent aEvent) throws DIException, RemoteException {
		mListener.handleEvent(aEvent);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the DIEventListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @return DIEventListenerBase object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static DIEventListenerBase createInstance(DIEventListener aListener, boolean aSSLon) throws Exception, RemoteException {
		return createInstance(aListener, aSSLon, Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES));
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the DIEventListener object
	 * @return DIEventListenerBase object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static DIEventListenerBase createInstance(DIEventListener aListener) throws Exception, RemoteException {
		return createInstance(aListener, false, false);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the DIEventListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @param aUseCustomProperties
	 *            if <code>true</code> custom user properties are used.
	 * @return DIEventListenerBase object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static DIEventListenerBase createInstance(DIEventListener aListener, boolean aSSLon, boolean aUseCustomProperties)
			throws Exception, RemoteException {
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(System.getProperties());
		if (aSSLon) {
			return new DIEventListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(aUseCustomProperties,
					bindAddr));
		} else {
			return new DIEventListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(bindAddr));
		}
	}
}
