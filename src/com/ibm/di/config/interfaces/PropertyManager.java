/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A PropertyManager manages the Property Stores.
 *
 */
public interface PropertyManager extends BaseConfiguration {

	/**
	 * Predefined property store names
	 */
	public final static String STDCOLL_JAVA = "Java-Properties";

	public final static String STDCOLL_GLOBAL = "Global-Properties";

	public final static String STDCOLL_SOLUTION = "Solution-Properties";

	public final static String STDCOLL_SYSTEM = "System-Properties";
	
	public static final String[] STDCOLL_PROPERTY_NAMES = new String[]{
			PropertyManager.STDCOLL_GLOBAL,	
			PropertyManager.STDCOLL_JAVA,	
			PropertyManager.STDCOLL_SOLUTION,	
			PropertyManager.STDCOLL_SYSTEM	
		};

	/**
	 * Method gets the container for the property store configurations.
	 * 
	 */
	public ContainerConfig getPropertyStores();

	/**
	 * Adds the property store config to the prop store collection.
	 * 
	 * @param psc
	 *            The property store config to add to the collection.
	 * @throws Exception
	 */
	public void addPropertyStore(PropertyStoreConfig psc) throws Exception;

	/**
	 * Returns the named PropertyStoreConfig.
	 * 
	 * @param name
	 */
	public PropertyStoreConfig getPropertyStore(String name);

	/**
	 * Returns the designated default property store.
	 * 
	 * @return The default prop store or NULL if none is designated
	 */
	public PropertyStoreConfig getDefaultPropertyStore();

	/**
	 * Returns the designated password property store.
	 * 
	 * @return The password prop store or NULL if none is designated
	 */
	public PropertyStoreConfig getPasswordPropertyStore();

	/**
	 * Sets the default property store
	 * 
	 * @param psc
	 *            The property store config
	 * @throws Exception
	 */
	public void setDefaultPropertyStore(PropertyStoreConfig psc)
			throws Exception;

	/**
	 * Sets the default password store
	 * 
	 * @param psc
	 *            The property store config
	 * @throws Exception
	 */
	public void setDefaultPasswordStore(PropertyStoreConfig psc)
			throws Exception;

	/**
	 * Adds a standard property store to the config. If one is already present
	 * this method does nothing.
	 * 
	 * @param store
	 *            The standard stor name
	 * @throws Exception
	 */
	public void addStdStore(String store) throws Exception;

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
	public boolean isStdStore(PropertyStoreConfig psc);
}
