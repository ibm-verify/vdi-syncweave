/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.List;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.TDIProperties;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * Wrapper API to expose the functionality available from
 * com.ibm.di.config.interfaces.TDIProperties.
 * 
 */
public class TDIPropertiesImpl extends APIRemoteObject implements
		TDIProperties {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -5762036032687102858L;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * the local TDIProperties
	 */
	private transient com.ibm.di.api.local.TDIProperties mLocalTDIProperties = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalTDIProperties
	 *            the local TDIProperties
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private TDIPropertiesImpl(
			com.ibm.di.api.local.TDIProperties aLocalTDIProperties,
			SessionImpl aSession, RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {

		super(0, aClientSF, aServerSF);

		if (aLocalTDIProperties == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.TDIPROPERTIES.IS.NULL.1"));
		}
		if (aSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.SESSION.OBJECT.IS.NULL.13"));
		}

		mLocalTDIProperties = aLocalTDIProperties;
		mSession = aSession;

	}

	/**
	 * {@inheritDoc}
	 */
	public void commit() throws RemoteException, Exception {
		mLocalTDIProperties.commit();

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String key) throws RemoteException, Exception {
		return mLocalTDIProperties.getProperty(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore setProperty(String key, Object value)
			throws RemoteException, Exception {
		return mLocalTDIProperties.setProperty(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore setProperty(String key, Object value,
			boolean protect) throws RemoteException, Exception {
		return mLocalTDIProperties.setProperty(key, value, protect);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(String propertyStoreName, String key)
			throws RemoteException, Exception {
		mLocalTDIProperties.removeProperty(propertyStoreName, key);

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String propertyStoreName, String key)
			throws RemoteException, Exception {
		return mLocalTDIProperties.getProperty(propertyStoreName, key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(String propertyStoreName, String key, Object value)
			throws RemoteException, Exception {
		mLocalTDIProperties.setProperty(propertyStoreName, key, value);

	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getPropertyStoreKeys(String propertyStoreName)
			throws RemoteException, Exception {
		return mLocalTDIProperties.getPropertyStoreKeys(propertyStoreName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPropertyStore(PropertyStoreConfig config)
			throws RemoteException, Exception {
		mLocalTDIProperties.addPropertyStore(config);

	}

	/**
	 * {@inheritDoc}
	 */
	public void insertPropertyStore(PropertyStoreConfig config, int atIndex)
			throws RemoteException, Exception {
		mLocalTDIProperties.insertPropertyStore(config, atIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePropertyStore(String propertyStoreName)
			throws RemoteException, Exception {
		mLocalTDIProperties.removePropertyStore(propertyStoreName);

	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getPropertyStoreNames() throws Exception, RemoteException {
		return mLocalTDIProperties.getPropertyStoreNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getDefaultStore() throws Exception, RemoteException {
		return mLocalTDIProperties.getDefaultStore();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefaultStore(TDIPropertyStore defaultStore)
			throws Exception, RemoteException {
		mLocalTDIProperties.setDefaultStore(defaultStore);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getPasswordStore() throws Exception,
			RemoteException {
		return mLocalTDIProperties.getPasswordStore();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPasswordStore(TDIPropertyStore passwordStore)
			throws Exception, RemoteException {
		mLocalTDIProperties.setPasswordStore(passwordStore);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getPropertyStore(String name) throws Exception,
			RemoteException {
		return mLocalTDIProperties.getPropertyStore(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String trimKey(String key) throws Exception, RemoteException {
		return mLocalTDIProperties.trimKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(String propertyStoreName, String key, Object value,
			boolean protect) throws RemoteException, Exception {
		mLocalTDIProperties.setProperty(propertyStoreName, key, value, protect);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPropertyEncrypted(String propertyStoreName, String key)
			throws Exception, RemoteException {
		return mLocalTDIProperties.isPropertyEncrypted(propertyStoreName, key);
	}

	/**
	 * Creates TDIPropertiesImpl instance.
	 * 
	 * @param localTDIP
	 *            the local TDIProperties
	 * @param aSession
	 *            the SessionImpl object
	 * @return TDIPropertiesImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static TDIPropertiesImpl createInstance(
			com.ibm.di.api.local.TDIProperties localTDIP, SessionImpl aSession)
			throws DIException, RemoteException {
		return new TDIPropertiesImpl(localTDIP, aSession, APIEngine
				.getClientSF(), APIEngine.getServerSF());
	}

}
