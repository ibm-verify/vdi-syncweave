/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Manages the Property Stores in a MetamergeConfig.
 *
 */
public class PropertyManagerImpl extends BaseConfigurationImpl implements
		PropertyManager, MetamergeFolder {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 4280805548502266432L;

	private final static String DEFAULT_STORE = "DefaultStore";

	private final static String PWD_STORE = "PasswordStore";

	private ContainerConfigImpl stores;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public PropertyManagerImpl() {
		super();
	}

	/**
	 * @param data
	 */
	public PropertyManagerImpl(Object data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.BaseConfiguration#init()
	 */
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		super.init();
		// Attribute maps
		if (stores == null) {
			stores = new ContainerConfigImpl(getParameter("container",
					new TreeMap()));
			stores.init();
			stores.setParent(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#addPropertyStore(com.ibm
	 * .di.config.interfaces.PropertyStoreConfig)
	 */
	public void addPropertyStore(PropertyStoreConfig psc) throws Exception {
		stores.addConfig(psc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#getDefaultPropertyStore()
	 */
	public PropertyStoreConfig getDefaultPropertyStore() {
		return getPropertyStore(getStringParameter(DEFAULT_STORE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#getPasswordPropertyStore()
	 */
	public PropertyStoreConfig getPasswordPropertyStore() {
		return getPropertyStore(getStringParameter(PWD_STORE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#getPropertyStore(java.lang
	 * .String)
	 */
	public PropertyStoreConfig getPropertyStore(String name) {
		if (name == null || name.equals(""))
			return null;
		else
			return (PropertyStoreConfig) stores.getConfig(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.PropertyManager#getPropertyStores()
	 */
	public ContainerConfig getPropertyStores() {
		return stores;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#setDefaultPasswordStore(
	 * com.ibm.di.config.interfaces.PropertyStoreConfig)
	 */
	public void setDefaultPasswordStore(PropertyStoreConfig psc)
			throws Exception {
		String value = (psc == null ? null : psc.getShortName());
		setParameter(PWD_STORE, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.config.interfaces.PropertyManager#setDefaultPropertyStore(
	 * com.ibm.di.config.interfaces.PropertyStoreConfig)
	 */
	public void setDefaultPropertyStore(PropertyStoreConfig psc)
			throws Exception {
		String value = (psc == null ? null : psc.getShortName());
		setParameter(DEFAULT_STORE, value);
	}

	/**
	 * Adds a standard property store to the config. If one is already present
	 * this method does nothing.
	 * 
	 * @param store
	 *            The standard store name
	 * @throws Exception
	 */
	public void addStdStore(String store) throws Exception {

		if (getPropertyStore(store) != null)
			return;

		PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
		psc.init();

		RawConnectorConfig rcc = psc.getConnectionConfig();
		rcc.setParent(psc);
		rcc.setParameter("collectionType", store);
		rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties");

		psc.setName(store);
		psc.setKeyAttribute("key");
		psc.setValueAttribute("value");
		psc.setInitialLoad(true);

		addPropertyStore(psc);

		rcc.setupInheritanceChain();
	}

	/**
	 * Method determines if the Property Store Config passed in is as standard
	 * property store.
	 * 
	 * @param psc
	 *            The Property Store Config to check on.
	 * 
	 * @return Returns true if the Property Store Config is a standard store.
	 *         Otherwise, false is returned.
	 */
	public boolean isStdStore(PropertyStoreConfig psc) {
		String str = psc.getShortName();
		return (PropertyManager.STDCOLL_GLOBAL.equals(str)
				|| PropertyManager.STDCOLL_JAVA.equals(str)
				|| PropertyManager.STDCOLL_SYSTEM.equals(str) || PropertyManager.STDCOLL_SOLUTION
				.equals(str));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.config.interfaces.BaseConfiguration#getClone()
	 */
	public Object getClone() throws Exception {
		PropertyManager cc = new PropertyManagerImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());
		cc.setupInheritanceChain();
		cc.setModTS(getModTS());
		return cc;
	}

	/**
	 * This method always throws an Exception, you are not allowed to create
	 * Folders here
	 */
	public MetamergeFolder createFolder(Object name) throws Exception {
		throw new Exception(
				sResHash
						.getString("MMCONFIG.METAMFOLDERIMPL.CANNOT.RECURSIVELY.CREATE.FOLDERS"));
	}

	public Enumeration<BaseConfiguration> list() throws Exception {
		if (stores == null)
			init();
		int n = stores.size();
		Hashtable<String, BaseConfiguration> h = new Hashtable<String, BaseConfiguration>(
				n);
		for (int i = 0; i < n; i++) {
			BaseConfiguration c = stores.getConfig(i);
			h.put(c.getShortName(), c);
		}
		return h.elements();
	}

	public String[] getNames() throws Exception {
		if (stores == null)
			init();
		int n = stores.size();
		String[] arr = new String[n];

		for (int i = 0; i < n; i++) {
			arr[i] = stores.getConfig(i).getShortName();
		}
		return arr;
	}

}
