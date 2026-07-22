/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.util.Iterator;
import java.util.List;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.TDIProperties;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * Wrapper API to expose the functionality available from
 * com.ibm.di.config.interfaces.TDIProperties.
 * 
 * @author administrator
 */
public class TDIPropertiesImpl implements TDIProperties {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Variable for providing access to the property stores defined for a
	 * specific configuration instance.
	 */
	private com.ibm.di.config.interfaces.TDIProperties mTDIProperties = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor
	 * 
	 * @param aTDIProperties
	 * @param aSession
	 * @throws DIException
	 */
	public TDIPropertiesImpl(
			com.ibm.di.config.interfaces.TDIProperties aTDIProperties,
			SessionImpl aSession) throws DIException {

		if (aTDIProperties == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.TDIPROPERTIES.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.SESSION.OBJECT.IS.NULL.5"));
		}

		mTDIProperties = aTDIProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	public void commit() throws Exception {
		mTDIProperties.commit();

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String key) throws Exception {
		return mTDIProperties.getProperty(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore setProperty(String key, Object value)
			throws Exception {
		return mTDIProperties.setProperty(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore setProperty(String key, Object value,
			boolean protect) throws Exception {
		return mTDIProperties.setProperty(key, value, protect);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(String propertyStoreName, String key)
			throws Exception {
		mTDIProperties.removeProperty(propertyStoreName, key);

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getProperty(String propertyStoreName, String key)
			throws Exception {
		return mTDIProperties.getProperty(propertyStoreName, key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(String propertyStoreName, String key, Object value)
			throws Exception {
		mTDIProperties.setProperty(propertyStoreName, key, value);

	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getPropertyStoreKeys(String propertyStoreName)
			throws Exception {
		Iterator iterator_store = mTDIProperties
				.getPropertyStoreKeys(propertyStoreName);
		java.util.ArrayList list = new java.util.ArrayList();
		Object key = null;
		while (iterator_store.hasNext()) {
			key = iterator_store.next();
			list.add(key);
		}
		String[] list_str = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			list_str[i] = (String) list.get(i);
		}
		return list_str;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPropertyStore(PropertyStoreConfig config) throws Exception {
		mTDIProperties.addPropertyStore(config);

	}

	/**
	 * {@inheritDoc}
	 */
	public void insertPropertyStore(PropertyStoreConfig config, int atIndex)
			throws Exception {
		mTDIProperties.insertPropertyStore(config, atIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePropertyStore(String propertyStoreName) throws Exception {
		mTDIProperties.removePropertyStore(propertyStoreName);

	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getPropertyStoreNames() throws Exception {
		return mTDIProperties.getPropertyStoreNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getDefaultStore() throws Exception {
		return mTDIProperties.getDefaultStore();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefaultStore(TDIPropertyStore defaultStore) throws Exception {
		mTDIProperties.setDefaultStore(defaultStore);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getPasswordStore() throws Exception {
		return mTDIProperties.getPasswordStore();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPasswordStore(TDIPropertyStore passwordStore)
			throws Exception {
		mTDIProperties.setPasswordStore(passwordStore);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIPropertyStore getPropertyStore(String name) throws Exception {
		return mTDIProperties.getPropertyStore(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public String trimKey(String key) throws Exception {
		return mTDIProperties.trimKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(String propertyStoreName, String key, Object value,
			boolean protect) throws Exception {
		// Get a reference to the property store
		TDIPropertyStore store = mTDIProperties
				.getPropertyStore(propertyStoreName);
		// Set the property into that particular property store
		if (store != null) {
			store.setProperty(key, value, protect);
		} else {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.TDIPROPERTYSTORE.NOT.FOUND"));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPropertyEncrypted(String propertyStoreName, String key)
			throws Exception {
		Entry entry = null;
		TDIPropertyStore store = null;

		if (propertyStoreName == null) {// Store name not passed
			List propStores = mTDIProperties.getPropertyStoreNames();
			String storename = null;
			for (int i = 0; i < propStores.size(); i++) { // Search for the
				// first store that
				// contains the key
				storename = (String) propStores.get(i);
				store = mTDIProperties.getPropertyStore(storename);
				entry = store.getPropertyEntry(key);
				if (entry == null) {
					continue;
				} else { // Found a store that contains the key
					break;
				}
			}
		} else { // Store name is passed
			store = mTDIProperties.getPropertyStore(propertyStoreName);
			if (store != null) {
				entry = store.getPropertyEntry(key);
			} else {
				throw new DIException(
						sResHash
								.getString("SEVER.API.LOCAL.TDIPROPERTYSTORE.NOT.FOUND"));
			}
		}

		if (entry == null) { // Property does not exist, so its safe to
			// assume it as default un-encrypted
			return false;
		} else {
			Attribute attr = entry.getAttribute("protect");
			String b = null;
			if (attr != null) {
				b = ((String) attr.getValue());
				if (b.equalsIgnoreCase("true")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
}
