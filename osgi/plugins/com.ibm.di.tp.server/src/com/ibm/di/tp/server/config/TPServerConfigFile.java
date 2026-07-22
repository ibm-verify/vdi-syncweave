/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config;

import java.io.File;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.security.Crypto;
import com.ibm.di.security.CryptoFactory;
import com.ibm.di.server.StashFile;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.TPServerApplication;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPServerConfigFile {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	// plain config
	private final TPServerConfig config;

	private final File pathToXml;

	private final Crypto crypto;

	public TPServerConfigFile(File pathToXml) throws Exception {
		this.pathToXml = pathToXml;

		TPServerConfig encryptedConfig;
		if (pathToXml.exists()) {
			encryptedConfig = loadConfig();
		} else {
			// create an empty instance
			encryptedConfig = new TPServerConfig();
		}

		crypto = createCrypto(encryptedConfig);
		// do the storing only if there is something to encrypt.
		storeTPServerConfigIfChanged(encryptedConfig);

		// now decrypt the whole config.
		decryptConfig(encryptedConfig);
		config = encryptedConfig;
	}

	private void decryptConfig(TPServerConfig cfg) throws Exception {
		if (cfg != null && crypto != null) {
			for (TdiNodeConfig nc : cfg.getNodeConfigs().getTdiNodeConfigs()) {
				if (nc.getPassword() != null && nc.getPassword().isEncrypted()) {
					nc.getPassword().setValue(decryptValue(nc.getPassword().getValue()));
				}
			}
		}
	}

	private String decryptValue(String value) throws Exception {
		if (value != null) {
			byte[] encBytes = UserFunctions.base64Decode(value);
			byte[] plainBytes = crypto.decrypt(encBytes);
			return new String(plainBytes, "UTF-8");
		}
		return null;
	}

	private boolean encryptConfig(TPServerConfig cfg) throws Exception {
		// return true if the config was changed at all.
		boolean changed = false;
		if (cfg != null && crypto != null) {
			for (TdiNodeConfig nc : cfg.getNodeConfigs().getTdiNodeConfigs()) {
				if (nc.getPassword() != null && nc.getPassword().isProtect()) {
					nc.getPassword().setEncrypted(true);
					nc.getPassword().setValue(encryptValue(nc.getPassword().getValue()));
					changed = true;
				}
			}
		}

		return changed;
	}

	private String encryptValue(String value) throws Exception {
		if (value != null) {
			byte[] encBytes = crypto.encrypt(value.getBytes("UTF-8"));
			return UserFunctions.base64Encode(encBytes);
		}
		return null;
	}

	public TPServerConfig getTPServerConfig() {
		return config;
	}

	private TPServerConfig loadConfig() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TPServerConfig.class);
		Unmarshaller u = jc.createUnmarshaller();

		TPServerConfig cfg = (TPServerConfig) u.unmarshal(pathToXml);
		return cfg;
	}

	private static Crypto createCrypto(TPServerConfig cfg) throws Exception {
		Crypto result = null;
		boolean readFailed = false;
		if (cfg != null && cfg.getEncryptionConfig().getStash() != null) {
			Vector<String> readPasswords = null;
			try {
				readPasswords = StashFile.readPasswords(cfg.getEncryptionConfig().getStash());
			} catch (Exception ex) {
				// thrown in the case when we are running inside TDI.
				// the default crypto provider should be used.
				TPServerApplication.getLog().warn(
						ServerActivator.L10N.getString("TP.SERVER.CONFIG.ERROR.READING.STASH", ex.getLocalizedMessage()));
				readFailed = true;
			}

			if (readPasswords != null && readPasswords.size() > 0) {
				String keyStorePassword = readPasswords.get(0);
				String keyPassword = null;
				if (readPasswords.size() > 1) {
					keyPassword = readPasswords.get(1);
				} else {
					keyPassword = keyStorePassword;
				}
				result = CryptoFactory.createCrypto(cfg.getEncryptionConfig().getKeyStore(), keyStorePassword, cfg
						.getEncryptionConfig().getKeyStoreType(), cfg.getEncryptionConfig().getKeyAlias(), keyPassword, cfg
						.getEncryptionConfig().getTransformation(), null);
			}

		}

		if (result == null) {
			try {
				result = CryptoUtils.getDefaultCrypto();
				if (readFailed) {
					// the file could not be read because we have already
					// initialized a provider with that stash file.
					TPServerApplication.getLog().debug(
							ServerActivator.L10N.getString("TP.SERVER.CONFIG.STASH.FILE.AREADY.READ"));
				} else {
					TPServerApplication.getLog().debug(
							ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.ENCRYPTION.SETTINGS"));
				}
			} catch (Exception e) {
				TPServerApplication.getLog().warn(
						ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.DEFAULT.ENCRYPTION.SETTINGS"));
			} catch (NoClassDefFoundError err) {
				TPServerApplication.getLog().warn(
						ServerActivator.L10N.getString("TP.SERVER.CONFIG.MISSING.DEFAULT.ENCRYPTION.SETTINGS"));
			}
		}

		return result;
	}

	private void storeTPServerConfigIfChanged(TPServerConfig cfg) throws Exception {
		if (encryptConfig(cfg)) {
			storeTPServerConfigUnconditionly(cfg);
		}
	}

	private void storeTPServerConfigUnconditionly(TPServerConfig cfg) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(TPServerConfig.class);

		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(cfg, pathToXml);
	}

	public void store() throws Exception {
		storeTPServerConfigUnconditionly(config);
	}
}
