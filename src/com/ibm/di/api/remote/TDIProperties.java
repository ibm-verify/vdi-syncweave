/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;

/**
 * 
 * Remote Server API interface to TDIProperties. Wrapper API to expose the
 * functionality available from com.ibm.di.config.interfaces.TDIProperties
 * 
 * @see com.ibm.di.config.interfaces.TDIProperties
 * 
 */
public interface TDIProperties extends Remote {

	/**
	 * Does a commit on all property stores.
	 * 
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void commit() throws RemoteException, Exception;

	/**
	 * Gets the property value from the property store chosen by TDIProperties.
	 * 
	 * @param key
	 *            The property name
	 * @return The property value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Object getProperty(String key) throws RemoteException, Exception;

	/**
	 * Returns the property value from the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property value
	 * @return The property value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Object getProperty(String propertyStoreName, String key)
			throws RemoteException, Exception;

	/**
	 * Returns an array containing all the property keys in the named property
	 * store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @return The propertyStoreKeys value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String[] getPropertyStoreKeys(String propertyStoreName)
			throws RemoteException, Exception;

	/**
	 * Sets the property in the property store chosen by TDIProperties.
	 * 
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @return the <code>TDIPropertyStore</code> to which the key/value pair
	 *         was written
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TDIPropertyStore setProperty(String key, Object value)
			throws RemoteException, Exception;

	/**
	 * Sets the property in the property store chosen by TDIProperties.
	 * 
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @param protect
	 *            True if value should be protected (driver dependent)
	 * @return the <code>TDIPropertyStore</code> to which the key/value pair
	 *         was written
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TDIPropertyStore setProperty(String key, Object value,
			boolean protect) throws RemoteException, Exception;

	/**
	 * Sets the property in the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void setProperty(String propertyStoreName, String key, Object value)
			throws RemoteException, Exception;

	/**
	 * Sets the property in the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @param protect
	 *            True if value should be protected (driver dependent)
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void setProperty(String propertyStoreName, String key, Object value,
			boolean protect) throws RemoteException, Exception;

	/**
	 * Removes a property in the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property to delete
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void removeProperty(String propertyStoreName, String key)
			throws RemoteException, Exception;

	/**
	 * Adds a property store to the end of the list of TDI-P's list of property
	 * stores.
	 * 
	 * @param config
	 *            The property store configuration
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void addPropertyStore(PropertyStoreConfig config)
			throws RemoteException, Exception;

	/**
	 * Inserts a connector interface at the given index. See addPropertyStore()
	 * for a description of parameters.
	 * 
	 * @param config
	 *            The property store configuration
	 * @param atIndex
	 *            The position where the new connector is placed (-1 = END, 0 =
	 *            First)
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void insertPropertyStore(PropertyStoreConfig config, int atIndex)
			throws RemoteException, Exception;

	/**
	 * Removes a property store from TDI-P. The connector interface is closed
	 * and then removed.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void removePropertyStore(String propertyStoreName)
			throws RemoteException, Exception;

	/**
	 * Returns a list of property store names in use by TDI-P.
	 * 
	 * @return The propertyStoreNames value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public List<String> getPropertyStoreNames() throws Exception, RemoteException;

	/**
	 * Returns the default property store.
	 * 
	 * @return the default property store
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TDIPropertyStore getDefaultStore() throws Exception, RemoteException;

	/**
	 * Sets the default property store.
	 * 
	 * @param defaultStore
	 *            the new default property store
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void setDefaultStore(TDIPropertyStore defaultStore)
			throws Exception, RemoteException;

	/**
	 * Returns the password store.
	 * 
	 * @return the password store
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TDIPropertyStore getPasswordStore() throws Exception,
			RemoteException;

	/**
	 * Sets the password store.
	 * 
	 * @param passwordStore
	 *            the new password store
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void setPasswordStore(TDIPropertyStore passwordStore)
			throws Exception, RemoteException;

	/**
	 * Returns the named property store.
	 * 
	 * @param name
	 *            the name of the property store
	 * @return the <code>TDIPropertyStore</code> with specified name
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TDIPropertyStore getPropertyStore(String name) throws Exception,
			RemoteException;

	/**
	 * Trims the key from a given string. For example
	 * <code>trimKey(&quot;greeting:Hello, world!&quot;)</code> will return
	 * the following string: <code>&quot;Hello, world!&quot;</code>.
	 * 
	 * @param key
	 *            a string representing key:value pair
	 * @return string containing only the value
	 * @throws Exception
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String trimKey(String key) throws Exception, RemoteException;

	/**
	 * Returns whether specified property is encrypted or not.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property value
	 * @return <code>true</code> if this property is encrypted;
	 *         <code>false</code> otherwise
	 * @throws Exception
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean isPropertyEncrypted(String propertyStoreName, String key)
			throws Exception, RemoteException;

}
