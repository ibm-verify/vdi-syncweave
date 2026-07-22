/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameNotFoundException;

import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * This class provides access to the property stores defined for a specific
 * configuration instance.
 */
public class TDIProperties implements Serializable {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -3361471837888677277L;

	/**
	 * This is the logger object used by this class and other configuration
	 * drivers.
	 */
	public final static Log logger = new Log("com.ibm.di.TDIProperties");

	public final static String KEY_ATTRIBUTE = "key";

	public final static String VALUE_ATTRIBUTE = "value";

	public final static String PROTECT_ATTRIBUTE = "protect";

	protected List<TDIPropertyStore> stores = new ArrayList<TDIPropertyStore>();

	private TDIPropertyStore defaultStore;

	private TDIPropertyStore passwordStore;

	protected PropertyManager pm;

	private static boolean debug = false;

	private final static ResourceHash sResHash = MetamergeConfigFactory
			.getResHash();

	private final static Object stdMutex = new Object();

	private static TDIPropertyStore globalStore = null;

	private static TDIPropertyStore javaStore = null;

	private static TDIPropertyStore systemStore = null;

	private static TDIPropertyStore solutionStore = null;

	public TDIProperties() {
	}

	/**
	 * TDIProperties constructor to create instance for a specified Config.
	 * 
	 * @param mc
	 * 
	 * @throws Exception
	 */
	public TDIProperties(MetamergeConfig mc) throws Exception {
		// Defect # 11367
		this(mc,true);		
	}
	
	public TDIProperties(MetamergeConfig mc , boolean initStores) throws  Exception{
		//New method for Defect # 11367
		try {
			pm = (PropertyManager) mc
					.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		} catch (NameNotFoundException nnfe) {
			pm = new PropertyManagerImpl();
			pm.init();
		}
		if(initStores){
			initStores();
		}
		
			// pm.addListener(new DefaultConfigChangeListener() {
			// public void configurationChanged(MetamergeConfigChange changeEvent) {
			// if (DEFAULT_STORE.equals(changeEvent.getKey())) {
			// setDefaultStore();
			// } else if (PWD_STORE.equals(changeEvent.getKey())) {
			// setPasswordStore();
			// } else if (COMP_LIST.equals(changeEvent.getKey())) {
			// removeAllStores();
			// addAllStores();
			// setPasswordStore();
			// setDefaultStore();
			// }
			// }
			// });
	}
	
	/**
	 * Returns the MetamergeConfig to which this object belongs.
	 * 
	 * @return
	 */
	public MetamergeConfig getMetamergeConfig() {
		if(pm != null)
			return pm.getMetamergeConfig();
		else
			return null;
	}
	
	public void initStores(){
		// New method for Defect # 11367
		addAllStores();
		setDefaultStore();
		setPasswordStore();
	}

	public void debug(String msg) {
		if (logger != null)
			logger.logdebug(msg);
	}

	public void warn(String msg) {
		if (logger != null)
			logger.logwarn(msg);
	}

	public void logerror(String msg) {
		if (logger != null)
			logger.logerror(msg);
	}

	private void addAllStores() {
		ContainerConfig cc = pm.getPropertyStores();
		for (int i = 0, n = cc.size(); i < n; i++) {
			try {
				addPropertyStore((PropertyStoreConfig) cc.getConfig(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void setDefaultStore() {
		// Default store
		if (pm.getDefaultPropertyStore() != null)
			setDefaultStore(getPropertyStore(pm.getDefaultPropertyStore()
					.getShortName()));
	}

	private void setPasswordStore() {
		// Password store
		if (pm.getPasswordPropertyStore() != null)
			setPasswordStore(getPropertyStore(pm.getPasswordPropertyStore()
					.getShortName()));
	}

	/**
	 * Does a commit on all property stores
	 */
	public void commit() throws Exception {
		for (TDIPropertyStore s : stores) {
			s.commit();
		}
	}

	/**
	 * Gets the property value from the property store chosen by TDIProperties.
	 * 
	 * @param key
	 *            The property name
	 * @return The property value
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public Object getProperty(String key) throws Exception {
		List<TDIPropertyStore> list = selectStore(key, true);
		String realKey = trimKey(key);

		if (debug && list.size() > 0) {
			debug(sResHash.getString("MICONFIG.TDIPROPERTIES.GETPROPERTY",
					realKey));
		}

		for (TDIPropertyStore s: list) {
			if (s == null)
				continue;
			if (debug) {
				debug(sResHash.getString("MICONFIG.TDIPROPERTIES.CHECK.STORE",
						s.getName()));
			}
			try {
				Object value = s.getProperty(realKey);
				if (value != null) {
					if (debug) {
						debug(sResHash.getString(
								"MICONFIG.TDIPROPERTIES.FOUND.IN.STORE", s
										.getName()));
					}
					return value;
				}
			} catch (Exception e) {
				if (debug) {
					debug(e.getLocalizedMessage());
				}
			}
		}

		logerror(sResHash.getString(
				"MICONFIG.TDIPROPERTIES.NO.AVAILABLE.PROPERTY.STORES.TO.READ.KEY",
				key));

		return null;
	}

	/**
	 * Sets the property in the property store chosen by TDIProperties. The
	 * returned TDIPropertyStore is the store to which the key/value pair was
	 * written.
	 * 
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public TDIPropertyStore setProperty(String key, Object value)
			throws Exception {
		return setProperty(key, value, false);
	}

	/**
	 * Sets the property in the property store chosen by TDIProperties. The
	 * returned TDIPropertyStore is the store to which the key/value pair was
	 * written.
	 * 
	 * @param key
	 *            The property name
	 * @param value
	 *            The new property value
	 * @param protect
	 *            True if value should be protected (driver dependent)
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public TDIPropertyStore setProperty(String key, Object value,
			boolean protect) throws Exception {
		List<TDIPropertyStore> list = selectStore(key, true);
		if (list.size() == 0) {
			throw new Exception(sResHash.getString(
				"MICONFIG.TDIPROPERTIES.NO.AVAILABLE.PROPERTY.STORES.TO.WRITE.KEY",
				key));
		}

		String realKey = trimKey(key);

		// Check if one of the stores already has the key and use that if
		// possible
		for (TDIPropertyStore s : list) {
			Object oldval = s.getProperty(realKey);
			if (oldval != null || list.size() == 1) {
				s.setProperty(realKey, value, protect);
				return s;
			}
		}

		// Use default store if no stores had the value before
		if (defaultStore != null)
			defaultStore.setProperty(realKey, value, protect);
		else {
			throw new Exception(sResHash.getString(
					"MICONFIG.TDIPROPERTIES.NO.DEFAULT.STORE.TO.ACCEPT",
					realKey));
		}

		return defaultStore;
	}

	/**
	 * Removes a property in the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property to delete
	 */
	public void removeProperty(String propertyStoreName, String key)
			throws Exception {
		for (TDIPropertyStore s: stores) {
			if (propertyStoreName.equals(s.getName())) {
				s.removeProperty(key);
				return;
			}
		}
	}

	/**
	 * Returns the property value from the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property value
	 * @return The property value
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public Object getProperty(String propertyStoreName, String key) throws Exception {
		TDIProperties props = selectTDIProperties(key, this);
		TDIPropertyStore store = props.getPropertyStore(propertyStoreName);
		if (store == null) {
			logerror(sResHash.getString("MICONFIG.TDIPROPERTIES.NO.PROPERTY.STORE.KEY", new Object[] { propertyStoreName, key }));
			return null;
		}

		Object value = store.getProperty(key);
		if (value == null)
			warn(sResHash.getString("MICONFIG.TDIPROPERTIES.PROPERTY.STORE.NO.KEY", new Object[] { propertyStoreName, key }));

		return value;
	}

	/**
	 * Sets a property in the named property store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @param key
	 *            The property value
	 * @param value
	 *            The new property value
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public void setProperty(String propertyStoreName, String key, Object value)
			throws Exception {
		for (TDIPropertyStore s: stores) {
			if (propertyStoreName.equals(s.getName())) {
				s.setProperty(key, value);
				return;
			}
		}
	}

	/**
	 * Returns the Entry object for a key - this is the same call as
	 * getProperty(key) only the Entry object is returned for the property.
	 */
	public Entry getPropertyEntry(String key) throws Exception {
		List<TDIPropertyStore> list = selectStore(key, true);

		if (list.size() == 0) {
			throw new Exception(sResHash.getString(
				"MICONFIG.TDIPROPERTIES.NO.AVAILABLE.PROPERTY.STORES.TO.READ.KEY",
				key));
		}

		String realKey = trimKey(key);

		for (TDIPropertyStore s : list) {
			Entry value = s.getPropertyEntry(realKey);
			if (value != null) {
				return value;
			}
		}

		return null;
	}

	/**
	 * Returns an Iterator for all the property keys in the named property
	 * store.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * @return The propertyStoreKeys value
	 * @exception Exception
	 *                Runtime or security exception
	 */
	@SuppressWarnings("unchecked")
	public Iterator<String> getPropertyStoreKeys(String propertyStoreName)
			throws Exception {
		for (TDIPropertyStore s: stores) {
			if (propertyStoreName.equals(s.getName())) {
				return s.keys();
			}
		}
		return null;		
	}

	/**
	 * Adds a property store to the end of the list of TDI-P's list of property
	 * stores.
	 * 
	 * @param config
	 *            The property store configuration
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public void addPropertyStore(PropertyStoreConfig config) throws Exception {
		stores.add(createStore(config));
	}

	/**
	 * Inserts a connector interface at the given index. See addPropertyStore()
	 * for a description of parameters.
	 * 
	 * @param config
	 *            The property store configuration
	 * @param atIndex
	 *            The position where the new connector is placed (-1 = END, 0 =
	 *            First)
	 * 
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public void insertPropertyStore(PropertyStoreConfig config, int atIndex)
			throws Exception {
		stores.add(atIndex, createStore(config));
	}

	/**
	 * Removes a property store from TDI-P. The connector interface is closed
	 * and then removed.
	 * 
	 * @param propertyStoreName
	 *            The name of the property store
	 * 
	 * @exception Exception
	 *                Runtime or security exception
	 */
	public void removePropertyStore(String propertyStoreName) throws Exception {
		int index = indexOf(propertyStoreName);
		if (index == -1)
			return;

		stores.remove(index).terminate();
	}

	/**
	 * Returns a list of property store names in use by TDI-P.
	 * 
	 * @return The propertyStoreNames value
	 */
	public List<String> getPropertyStoreNames() {
		List<String> list = new ArrayList<String>();
		for (TDIPropertyStore s:stores)
			list.add(s.getName());
		return list;
	}

	/**
	 * Returns the index for a named property store
	 * 
	 * @param name
	 *            Name of property store
	 * 
	 * @return The position of the property store
	 */
	public int indexOf(String name) {
		for (int i = 0; i < stores.size(); i++) {
			if (name.equals(stores.get(i).getName())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns an initialized TDIPropertyStore object
	 * 
	 * @param psc
	 *            The property store configuration
	 * @return The TDIPropertyStore object
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	private TDIPropertyStore createStore(PropertyStoreConfig psc)
			throws Exception {
		if (pm.isStdStore(psc))
			return createStdStore(psc);
		else
			return new TDIPropertyStore(psc, this);
	}

	/**
	 * Returns an initialized TDIPropertyStore object for a standard store
	 * 
	 * @param psc
	 *            The property store configuration
	 * @return The TDIPropertyStore object
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	private TDIPropertyStore createStdStore(PropertyStoreConfig psc)
			throws Exception {
		synchronized (stdMutex) {
			String str = psc.getShortName();
			if (PropertyManager.STDCOLL_GLOBAL.equals(str)) {
				if (globalStore == null)
					globalStore = new TDIPropertyStore(psc, null);
				return globalStore;
			}
			if (PropertyManager.STDCOLL_JAVA.equals(str)) {
				if (javaStore == null)
					javaStore = new TDIPropertyStore(psc, null);
				return javaStore;
			}
			if (PropertyManager.STDCOLL_SYSTEM.equals(str)) {
				if (systemStore == null)
					systemStore = new TDIPropertyStore(psc, null);
				return systemStore;
			}
			if (PropertyManager.STDCOLL_SOLUTION.equals(str)) {
				if (solutionStore == null)
					solutionStore = new TDIPropertyStore(psc, null);
				return solutionStore;
			}
		}
		return new TDIPropertyStore(psc, this);
	}

	/**
	 * Returns the preferred store for a property key
	 */
	protected List<TDIPropertyStore> selectStore(String key, boolean read) {

		List<TDIPropertyStore> list = new ArrayList<TDIPropertyStore>();
		if (key == null)
			return list;

		TDIProperties props = selectTDIProperties(key, this);
		
		int index = key.lastIndexOf(":");
		if (index != -1) {
			// StoreName:PropName or property.StoreName:PropName
			String store = key.substring(0, index);
			if (store.startsWith("property."))
				store = store.substring(9);
			
			TDIPropertyStore s = props.getPropertyStore(store);
			if (s != null)
				list.add(s);

			return list;
		}

		// Check stores if they accept
		for(String str : props.getPropertyStoreNames()) {
			TDIPropertyStore s = props.getPropertyStore(str);
			int listIndex = list.size();
			if (s == props.getDefaultStore())
				listIndex = 0;
			if (read && s.canRead() && s.accepts(key))
				list.add(listIndex, s);
			else if (!read && s.canWrite() && s.accepts(key))
				list.add(listIndex, s);
		}

		// Add default store if list is empty
		if (props.getPropertyStoreNames().size() == 0 && props.getDefaultStore() != null)
			list.add(props.getDefaultStore());

		return list;
	}

	public TDIPropertyStore getDefaultStore() {
		return defaultStore;
	}

	public void setDefaultStore(TDIPropertyStore defaultStore) {
		this.defaultStore = defaultStore;
	}

	public TDIPropertyStore getPasswordStore() {
		return passwordStore;
	}

	public void setPasswordStore(TDIPropertyStore passwordStore) {
		this.passwordStore = passwordStore;
	}

	public TDIPropertyStore getPropertyStore(String name) {
		int index = indexOf(name);
		if (index == -1) {
			// double-check with prop manager
			PropertyStoreConfig psc = pm.getPropertyStore(name);
			if (psc != null) {
				try {
					addPropertyStore(psc);
				} catch (Exception err) {
				}
			}
			index = indexOf(name);
		}

		if (index == -1)
			return null;
		else
			return stores.get(index);
	}

	public String trimKey(String key) {
		String str = key;
		// -- remove property store name
		int index = str.lastIndexOf(":");
		if (index != -1) {
			str = str.substring(index + 1);
		}
		
		// -- remove external config reference
		index = str.lastIndexOf("@");
		if (index != -1) {
			str = str.substring(0, index);
		}
		return str;
	}

	/**
	 * Returns the TDIProperties object associated with the key. If the key has a ref to
	 * another project/config we return that. Otherwise the defaultProps is returned.
	 * (store:property@referencedMC)
	 * 
	 * @param key
	 * @param defaultProps
	 * @return
	 */
	protected TDIProperties selectTDIProperties(String key, TDIProperties defaultProps) {
		int at = key.lastIndexOf("@");
		if(at != -1) {
			String ref = key.substring(at+1);
			try {
				NamespaceConfig ns = (NamespaceConfig)getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER + "/" + ref);
				MetamergeConfig mc = MetamergeConfigFactory.loadNamespace(ns);
				if(mc != null)
					return mc.getTDIProperties();
			} catch (Exception err) {
				if(logger != null)
					logger.logerror(key, err);
				else
					err.printStackTrace();
			}
		}
		return defaultProps;
	}

}
