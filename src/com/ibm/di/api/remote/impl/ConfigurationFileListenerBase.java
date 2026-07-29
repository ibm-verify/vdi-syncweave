/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.ConfigEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ConfigurationFileListener;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.api.remote.impl.rmi.SSLRMIClientSocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements EventListener for Server API Configuration File events.
 */
public class ConfigurationFileListenerBase extends APIRemoteObject implements ConfigurationFileListener {
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
	 * ConfigurationFileListener object
	 */
	private transient ConfigurationFileListener listener = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param listener
	 *            the ConfigurationFileListener object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private ConfigurationFileListenerBase(ConfigurationFileListener listener) throws DIException, RemoteException {
		super();

		if (listener == null) {
			throw new DIException(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.7"));
		}

		this.listener = listener;
	}

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param listener
	 *            the ConfigurationFileListener object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private ConfigurationFileListenerBase(ConfigurationFileListener listener, RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (listener == null) {
			throw new DIException(sResHash.getString("SEVER.API.LISTENER.OBJECT.IS.NULL.8"));
		}

		this.listener = listener;
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(ConfigEvent event) throws RemoteException {
		listener.handleEvent(event);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the ConfigurationFileListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @return new ConfigurationFileListener object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static ConfigurationFileListener createInstance(ConfigurationFileListener aListener, boolean aSSLon) throws Exception,
			RemoteException {
		return createInstance(aListener, aSSLon, Boolean.getBoolean(Constants.PROP_API_REMOTE_SSL_CUSTOM_PROPERTIES));
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the ConfigurationFileListener object
	 * @return new ConfigurationFileListenerBase object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static ConfigurationFileListener createInstance(ConfigurationFileListener aListener) throws Exception, RemoteException {
		return createInstance(aListener, false, false);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aListener
	 *            the ConfigurationFileListener object
	 * @param aSSLon
	 *            if <code>true</code> SSL is used
	 * @param aUseCustomProperties
	 *            if <code>true</code> custom user properties are used.
	 * @return new ConfigurationFileListenerBase object
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static ConfigurationFileListener createInstance(ConfigurationFileListener aListener, boolean aSSLon,
			boolean aUseCustomProperties) throws Exception, RemoteException {
		BindAddressPolicy bindAddr = new BindAddressPolicyImpl(System.getProperties());
		if (aSSLon) {
			return new ConfigurationFileListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(aUseCustomProperties,
					bindAddr));
		} else {
			return new ConfigurationFileListenerBase(aListener, new SSLRMIClientSocketFactory(
					SSLRMIClientSocketFactory.SSL_PROPERTIES_SERVER_DEFINED), new SSLRMIServerSocketFactory(bindAddr));
		}
	}
}
