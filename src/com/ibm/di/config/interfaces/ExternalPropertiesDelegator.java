/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ibm.di.server.ResourceHash;

/**
 * The old way of accessing External Properties.
 * @deprecated Use {@link TDIProperties} instead.
 * @see TDIProperties
 *
 */
public class ExternalPropertiesDelegator extends
		com.ibm.di.config.base.BaseConfigurationImpl implements
		ExternalPropertiesConfig, MetamergeConfigChangeListener, Comparator {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 7725187425731381660L;

	private final static ResourceHash sResHash = MetamergeConfigFactory
			.getResHash();

	private MetamergeConfig mc;

	private String msg = sResHash
			.getString("MICONFIG.EXTPROPSDELEGATOR.USE.A.NAMED.EXTERNALPROPERTIES");

	private TDIProperties tdiProperties;

	public ExternalPropertiesDelegator(MetamergeConfig mc) {
		this.mc = mc;
	}

	/**
	 * This metod does nothing
	 * 
	 * @deprecated This method is not used anymore, and does nothing
	 */
	@Deprecated
	public void configurationChanged(MetamergeConfigChange changeEvent) {
	}

	/**
	 * Gets the saveNeeded flag of the ExternalPropertiesConfig object
	 * 
	 * @return The saveNeeded value
	 */
	public boolean getSaveNeeded() {
		return false;
	}

	/**
	 * This method reads the external properties into memory for subseqent use.
	 * This is typically done automatically by the hosting MetamergeConfig
	 * object when this object is requested.
	 * 
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void loadData() throws Exception {
		if (tdiProperties == null)
			getEPObjects();
		if (tdiProperties == null)
			return;

		TDIPropertyStore store = tdiProperties.getDefaultStore();
		if (store != null)
			store.reconnect();
	}

	/**
	 * This method writes back the data to the external file.
	 * 
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void saveData() throws Exception {
		if (tdiProperties == null)
			getEPObjects();
		if (tdiProperties == null)
			return;

		TDIPropertyStore store = tdiProperties.getDefaultStore();
		if (store != null)
			store.terminate();
	}

	/**
	 * This method returns the combined set of property names in all extprop
	 * objects
	 */
	public List<String> getKeys(int level) {
		getEPObjects();
		ArrayList<String> list = new ArrayList<String>();
		for (String name: tdiProperties.getPropertyStoreNames()) {
			try {
				Iterator<String> iter = tdiProperties
						.getPropertyStoreKeys(name);
				while (iter != null && iter.hasNext()) {
					list.add(name + ":" + iter.next());
				}
			} catch (Exception e) {
				e.printStackTrace();
				list.add("ERROR: " + name + "; " + e);
			}
		}

		return list;
	}

	public Object getParameter(Object name) {

		if (name == null)
			return null;

		getEPObjects();

		try {
			String pname = name.toString();
			int index = pname.indexOf(":");
			if (index != -1)
				return tdiProperties.getProperty(pname.substring(0, index),
						pname.substring(index + 1));
			else
				return tdiProperties.getProperty(pname);
		} catch (Exception e) {
			e.printStackTrace();
			return e;
		}
	}

	/**
	 * Returns null
	 * 
	 * @deprecated Always returns null
	 */
	@Deprecated
	public ExternalPropertiesConfig getContainerForKey(Object name) {
		return null;
	}

	/**
	 * Returns null
	 * 
	 * @deprecated Always returns null
	 */
	@Deprecated
	public ExternalPropertiesConfig getDefaultExternalPropertyObject() {
		return null;
	}

	public void setParameter(Object name, Object value) {
		getEPObjects();
		try {
			tdiProperties.setProperty(name.toString(), value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeParameter(Object name) {
		getEPObjects();
		try {
			tdiProperties.setProperty(name.toString(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the extern property config named <i>name </>
	 */
	public TDIPropertyStore getNamedXP(String name) {
		return tdiProperties.getPropertyStore(name);
	}

	public TDIProperties getEPObjects() {
		try {
			tdiProperties = mc.getTDIProperties();
			return tdiProperties;
		} catch (Exception error) {
			error.printStackTrace();
		}
		return null;
	}

	public int compare(Object o1, Object o2) {
		String sa = ((BaseConfiguration) o1).getShortName();
		String sb = ((BaseConfiguration) o2).getShortName();
		// if
		// (UserPreferences.getDefaultInstance().getBoolean("com.ibm.di.admin.sortFoldersIgnoreCase",false))
		// return sa.compareToIgnoreCase(sb);
		// else
		return sa.compareTo(sb);

	}

	// / **********************************************************************
	// ////
	// / The following methods cannot be delegated
	// /

	public Object getClone() {
		return null;
	}

	/**
	 * Gets the filePath attribute of the ExternalPropertiesConfig object
	 * 
	 * @return The filePath value
	 */
	public String getFilePath() {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return null;
		return rcc.getStringParameter("collection");
	}

	/**
	 * Sets the filePath attribute of the ExternalPropertiesConfig object
	 * 
	 * @param path
	 *            The new filePath value
	 */
	public void setFilePath(String path) {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return;
		rcc.setStringParameter("collection", path);
	}

	/**
	 * Returns the password used when opening an encrypted file. This method
	 * always returns null for security reasons.
	 * 
	 */
	public String getPassword() {
		return null;
	}

	/**
	 * Sets the password used when opening an encrypted file
	 */
	public void setPassword(String password) {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return;
		rcc.setStringParameter("secret", password);
	}

	/**
	 * Returns the Cipher algorithm used when reading/writing an encrypted file
	 */
	public String getCipher() {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return null;
		return rcc.getStringParameter("cipher");
	}

	/**
	 * Sets the Cipher algorithm used when reading/writing an encrypted file
	 */
	public void setCipher(String cipher) {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return;
		rcc.setStringParameter("cipher", cipher);
	}

	/**
	 * Gets the encrypted flag of the ExternalPropertiesConfig object
	 * 
	 * @return The encrypted value
	 */
	public boolean getEncrypted() {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return false;
		return rcc.getBooleanParameter("encryption", false);
	}

	/**
	 * Sets the encrypted flag of the ExternalPropertiesConfig object
	 * 
	 * @param encrypted
	 *            The new encrypted value
	 */
	public void setEncrypted(boolean encrypted) {
		RawConnectorConfig rcc = getDefaultConnectionConfig();
		if (rcc == null)
			return;
		rcc.setBooleanParameter("encryption", encrypted);
	}

	/**
	 * This method merges the contents of a file into the current list of
	 * properties.
	 * 
	 * @deprecated Use a named ExternalProperties object instead of this call
	 * @param path
	 *            The file path to load properties from
	 * @throws Exception
	 *             Always, use a named ExternalProperties object instead of this
	 *             call
	 */
	@Deprecated
	public void mergeData(String path) throws Exception {
		throw new Exception(msg);
	}

	private RawConnectorConfig getDefaultConnectionConfig() {
		if (tdiProperties == null)
			getEPObjects();
		if (tdiProperties == null)
			return null;

		TDIPropertyStore store = tdiProperties.getDefaultStore();
		if (store == null) {
			try {
				PropertyManager pm = (PropertyManager) mc
						.lookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
				PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
				psc.setName("Default");
				psc.setMetamergeConfig(mc);
				psc.init();
				RawConnectorConfig rcc = psc.getConnectionConfig();
				rcc.setParent(psc);
				rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties");
				rcc.setParameter("collectionType", "User-Defined");
				rcc.setupInheritanceChain();
				psc.setKeyAttribute("key");
				psc.setValueAttribute("value");
				psc.setInitialLoad(true);
				pm.getPropertyStores().addConfig(psc);
				pm.setDefaultPropertyStore(psc);
				return rcc;
			} catch (Exception e) {
				return null;
			}
		}

		RawConnectorConfig rcc = store.getConnectionConfig();
		if (rcc == null)
			return null;
		String className = rcc.getJavaClass();
		if (!"com.ibm.di.connector.PropertiesConnector".equals(className))
			return null;
		String ct = rcc.getStringParameter("collectionType");
		if (!"User-Defined".equals(ct))
			return null;
		return rcc;
	}
}
