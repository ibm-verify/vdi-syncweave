/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import java.io.*;
import com.ibm.di.config.interfaces.*;
import com.ibm.di.security.*;
import com.ibm.di.server.ResourceHash;

/**
 * Implements the old External Properties used when running Tivoli Directory Integrator.
 * @deprecated
 *
 */
public class ExternalPropertiesImpl extends BaseConfigurationImpl implements
		ExternalPropertiesConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -5837658758525300221L;

	private BaseConfiguration extprops;

	private static final String[] KEYWORDS = {
			InternalSchema.EXTPROP_FILE_PATH, InternalSchema.EXTPROP_ENCRYPTED,
			InternalSchema.EXTPROP_QUERY_SIBLINGS,
			InternalSchema.EXTPROP_PASSWORD, InternalSchema.EXTPROP_CIPHER, };

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public ExternalPropertiesImpl() {
		super();
	}

	public ExternalPropertiesImpl(Object config) {
		super(config);
	}

	public boolean isDebugMode() {
		return MetamergeConfigImpl.logger.isDebugEnabled();
	}

	public void init() throws Exception {
		loadData();
	}

	public void configurationChanged(MetamergeConfigChange mcc) {
		performNotifyChange(mcc);
	}

	public void loadData() throws Exception {

		if (extprops != null)
			extprops.removeListener(this);

		extprops = new BaseConfigurationImpl();
		mergeData(getFilePath());
		// extprops.addListener ( this );
		extprops.setModified(false);
	}

	public void mergeData(String path) throws Exception {

		if (path == null || extprops == null)
			return;

		File f = new File(path);
		if (!f.exists())
			return;

		BufferedReader inp = null;

		try {
		if (EncryptedReader.isEncrypted(f)) {
			inp = new EncryptedReader(new FileInputStream(f));
			String pwd;
			if (getPassword() != null && getPassword().length() > 0)
				pwd = getPassword();
			else
				throw new Exception(sResHash.getString("MMCONFIG.EXTPROPSIMPL.NO.PASSWORD", path));

			((EncryptedReader) inp).setAlgorithm(getCipher());
			((EncryptedReader) inp).useKey(pwd);
			((EncryptedReader) inp).prefetch();
			setEncrypted(true);
		} else {
			inp = new BufferedReader(new FileReader(f));
		}

		String str;
		while ((str = inp.readLine()) != null) {
			str = str.trim();
			if (str.equals("") || str.startsWith("#")) {
				continue;
			} else if (str.indexOf(":") != -1) {
				String key = str.substring(0, str.indexOf(":"));
				if (extprops.getParameter(key) == null)
					extprops.setStringParameter(key, str.substring(str
							.indexOf(":") + 1));
			} else {
				MetamergeConfigImpl.logger.error(sResHash.getString(
						"MMCONFIG.EXTPROPSIMPL.UNKNOWN.PROPERTY.LINE", str));
			}
		}
		} finally {
			if (inp != null)
				inp.close();
		}
	}

	public void saveData() throws Exception {
		if (extprops == null)
			return;
		String path = getFilePath();
		if (path == null || path.length() == 0) {
			if (extprops.size() > 0) {
				throw new Exception(sResHash.getString(
						"MMCONFIG.EXTPROPSIMPL.NO.FILENAME.SPECIFIED",
						getName()));
			} else
				return;
		}

		BufferedWriter out = null;

		try {
		if (getEncrypted()) {
			out = new EncryptedWriter(new FileOutputStream(path));
			String pwd;
			if (getPassword() != null && getPassword().length() > 0)
				pwd = getPassword();
			else
				throw new Exception(sResHash.getString("MMCONFIG.EXTPROPSIMPL.NO.PASSWORD", path));

			((EncryptedWriter) out).setAlgorithm(getCipher());
			((EncryptedWriter) out).useKey(pwd);
		} else {
			out = new BufferedWriter(new FileWriter(path));
		}

		List<String> list = extprops.getKeys(BaseConfiguration.ONE_LEVEL);
		for (int i = 0; i < list.size(); i++) {
			out.write(list.get(i) + ":" + extprops.getParameter(list.get(i)));
			out.newLine();
		}
		} finally {
			if (out != null)
				out.close();
		}

		extprops.setModified(false);
	}

	public boolean getSaveNeeded() {
		if (extprops == null)
			return false;
		else
			return extprops.getModified();
	}

	public void setupInheritanceChain() {
	}

	public String getFilePath() {
		return getStringParameter(InternalSchema.EXTPROP_FILE_PATH);
	}

	public void setFilePath(String path) {

		if (path == null || path.trim().length() == 0) {
			removeParameter(InternalSchema.EXTPROP_FILE_PATH);
			try {
				loadData();
			} catch (Exception ignore) {
			}
		} else {
			setParameter(InternalSchema.EXTPROP_FILE_PATH, path);
		}

		if (extprops != null)
			extprops.setModified(true);
	}

	public String getPassword() {
		return getStringParameter(InternalSchema.EXTPROP_PASSWORD);
	}

	public void setPassword(String password) {

		if (password == null)
			removeParameter(InternalSchema.EXTPROP_PASSWORD);
		else
			setParameter(InternalSchema.EXTPROP_PASSWORD, password);

		if (extprops != null)
			extprops.setModified(true);
	}

	public boolean getEncrypted() {
		return getBooleanParameter(InternalSchema.EXTPROP_ENCRYPTED, false);
	}

	public void setEncrypted(boolean encrypted) {
		setBooleanParameter(InternalSchema.EXTPROP_ENCRYPTED, encrypted);
		if (extprops != null)
			extprops.setModified(true);
	}

	/**
	 * Returns the Cipher algorithm used when reading/writing an encrypted file
	 */
	public String getCipher() {
		return getStringParameter(InternalSchema.EXTPROP_CIPHER);
	}

	/**
	 * Sets the Cipher algorithm used when reading/writing an encrypted file
	 */
	public void setCipher(String cipher) {
		if (cipher == null)
			removeParameter(InternalSchema.EXTPROP_CIPHER);
		else
			setParameter(InternalSchema.EXTPROP_CIPHER, cipher);

		if (extprops != null)
			extprops.setModified(true);
	}

	public List<String> getKeys(int level) {
		if (extprops != null)
			return extprops.getKeys(level);
		else
			return new ArrayList<String>();
	}

	public Object getParameter(Object name) {

		if (name == null)
			return null;
		if (isDebugMode()) {
			MetamergeConfigImpl.logger.debug(sResHash.getString(
					"MMCONFIG.EXTPROPSIMPL.EXTPROP.GETPARAMETER", name));
		}
		for (int i = 0; i < KEYWORDS.length; i++) {
			if (KEYWORDS[i].equals("" + name)) {
				return super.getParameter(name);
			}
		}

		Object value = null;
		if (extprops != null)
			value = extprops.getParameter(name);

		return value;
	}

	public void setParameter(Object name, Object value) {

		if (isDebugMode()) {
			MetamergeConfigImpl.logger.debug(sResHash.getString(
					"MMCONFIG.EXTPROPSIMPL.EXTPROP.SETVALUE", new Object[] {
							name, value }));
		}

		for (int i = 0; i < KEYWORDS.length; i++) {
			if (KEYWORDS[i].equals("" + name)) {
				super.setParameter(name, value);
				return;
			}
		}

		if (extprops == null) {
			extprops = new BaseConfigurationImpl();
			// extprops.addListener ( this );
		}

		extprops.setParameter(name, value);
		notifyChange(this, InternalSchema.INHERITS_FROM,
				MetamergeConfigChange.MCC_SET);
	}

	public void removeParameter(Object name) {

		for (int i = 0; i < KEYWORDS.length; i++) {
			if (KEYWORDS[i].equals("" + name)) {
				super.removeParameter(name);
				return;
			}
		}
		if (extprops != null) {
			extprops.removeParameter(name);
			notifyChange(this, InternalSchema.INHERITS_FROM,
					MetamergeConfigChange.MCC_REMOVE);
		}
	}

	public Object getClone() throws Exception {
		ExternalPropertiesConfig epc = new ExternalPropertiesImpl(
				deepClone(null));
		epc.setMetamergeConfig(getMetamergeConfig());
		epc.init();
		epc.setupInheritanceChain();
		epc.setName(getName());
		epc.setModTS(getModTS());
		return epc;
	}

}
