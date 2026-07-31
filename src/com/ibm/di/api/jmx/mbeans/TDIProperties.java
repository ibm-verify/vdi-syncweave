/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.rmi.RemoteException;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * Wrapper API to expose the functionality available from
 * com.ibm.di.config.interfaces.TDIProperties.
 * 
 */
public class TDIProperties extends BaseAdmin implements TDIPropertiesMBean {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "TDIProperties";

	/**
	 * {@link com.ibm.di.api.local.TDIProperties} instance.
	 */
	private com.ibm.di.api.local.TDIProperties mLocalTDIProperties = null;

	/**
	 * ID.
	 */
	private String mId = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Class constructor.
	 * 
	 * @param aLocalTDIProperties
	 *            {@link com.ibm.di.api.local.TDIProperties} instance.
	 * @param aId
	 *            ID.
	 * @throws DIException
	 */
	public TDIProperties(
			com.ibm.di.api.local.TDIProperties aLocalTDIProperties, String aId)
			throws DIException {
		mLocalTDIProperties = aLocalTDIProperties;
		mId = aId;
	}

	// MBean interface

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return mId;
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
	public List getPropertyStoreNames() throws Exception, RemoteException {
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
	 * Generates object name for specified TDI-P.
	 * 
	 * @param aUniqueCode
	 *            unique code used for building the TDI-P MBean id.
	 * @return the generated object name
	 * 
	 * @throws DIException
	 *             if error occurs while creating TDI-P JMX object name.
	 */
	public static ObjectName genObjectName(String aUniqueCode)
			throws DIException {
		String keyProperties = "type=" + MBEAN_TYPE + ",id=" + aUniqueCode;
		ObjectName objectName = null;
		try {
			objectName = new ObjectName(JMXAgent.MBEAN_SERVER_DOMAIN + ":"
					+ keyProperties);
		} catch (MalformedObjectNameException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.CREATE.TDIPROPERTIES.JMX.OBJECT.NAME"),
							e);
		}
		return objectName;
	}
}
