/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.ParamUtils;
import com.ibm.di.util.StringUtils;
import java.security.Provider;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Map;
import java.util.Enumeration;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * This a command line tool for managing secret keys. The tool is needed because
 * the standard utilities 'keytool' and 'ikeyman' in IBM JRE 5.0 can only work
 * with public/private keys but not with secret keys.
 * 
 * The tool can generate and delete secret keys. It can also import a secret key
 * from one keystore to another.
 * 
 * @since 7.0
 */
public class SecretKeyTool {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Command-line switch, which triggers generation of a secret key.
	 */
	private static final String GEN_SECRET_KEY_SWITCH = "genseckey";

	/**
	 * Command-line switch, which triggers deletion of a secret key.
	 */
	private static final String DEL_SECRET_KEY_SWITCH = "delete";

	/**
	 * Command-line switch, which triggers imorting e secret key from one
	 * keystore to another.
	 */
	private static final String IMPORT_SECRET_KEY_SWITCH = "importkey";

	/**
	 * Command-line switch, which triggers listing the contents of a keystore.
	 */
	private static final String LIST_KEYSTORE_SWITCH = "list";

	/**
	 * Command-line switch, which triggers printing information about a secret
	 * key.
	 */
	private static final String PRINT_SECRET_KEY_SWITCH = "printseckey";

	/**
	 * A set of key-value parameter pairs, which describe a single user request.
	 * Normally user requests are parsed from the command line.
	 */
	private Map params;

	/**
	 * Call with no arguments to print a brief usage manual. The following
	 * operations are provided:
	 * 
	 * "-genseckey" : Generates a secret key for the specified algorithm of the
	 * specified size. The generated key is stored in the specified keystore
	 * under the specified alias. If the keystore does not exist, it is created.
	 * If the keystore already contains an entry under that alias, that entry is
	 * overridden.
	 * 
	 * "-delete" : deletes an existing secret key from a keystore
	 * 
	 * "-importkey" : Imports a secret key from one keystore to another. If the
	 * destination keystore does not exist, it is created. If the destination
	 * keystore already contains an entry under the specified alias, that entry
	 * is overridden.
	 * 
	 * "-list" : Lists the entries of a keystore. For each entry it is displayed
	 * whether the entry is a certificate entry, a private key entry or a secret
	 * key entry.
	 * 
	 * "-printseckey" : Prints information about a secret key from a keystore.
	 * The displayed information includes the algorithm of the key. The size of
	 * the key in bits and the actual raw key data.
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws Exception
	 *             incorrect syntax or an error encountered during the tool
	 *             operation
	 */
	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			printUsage();
		} else {

			Map params = ParamUtils.parseCommandLine(args);
			try {
				new SecretKeyTool(params).run();
			} catch (Exception ex) {
				throw new Exception(resHash.getString(
						"SECRET.KEY.TOOL.OPERATION.FAILED", ex));
			}
		}
	}

	/**
	 * Prints a brief usage manual on the standard output.
	 */
	private static void printUsage() {
		System.out.println(resHash.getString("SECRET.KEY.TOOL.USAGE"));
	}

	/**
	 * Creates a new instance of the tool to service a given user request. The
	 * request is passed as a set of parameter name-value pairs.
	 * 
	 * @param params
	 *            parameters, which describe the user request
	 */
	private SecretKeyTool(Map params) {

		this.params = params;
	}

	/**
	 * Runs the tool on the configured parameters.
	 * 
	 * @throws Exception
	 *             parameter mismatch or an error encountered during the tool
	 *             operation
	 */
	private void run() throws Exception {

		if (params.get(GEN_SECRET_KEY_SWITCH) != null) {
			generateAndStoreSecretKey();
		} else if (params.get(DEL_SECRET_KEY_SWITCH) != null) {
			deleteKey();
		} else if (params.get(IMPORT_SECRET_KEY_SWITCH) != null) {
			importKey();
		} else if (params.get(LIST_KEYSTORE_SWITCH) != null) {
			listKeyStore();
		} else if (params.get(PRINT_SECRET_KEY_SWITCH) != null) {
			printSecretKey();
		} else {
			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.COULD.NOT.DETERMINE.OPERATION",
					new Object[] { GEN_SECRET_KEY_SWITCH,
							DEL_SECRET_KEY_SWITCH, IMPORT_SECRET_KEY_SWITCH }));
		}

	}

	/**
	 * Handles the "-genseckey" command-line option.
	 * 
	 * @throws Exception
	 *             error encountered while performing the operation
	 */
	private void generateAndStoreSecretKey() throws Exception {

		final String keyAlias = ParamUtils.getRequiredParam(params, "alias");
		final String keyAlgorithm = ParamUtils.getRequiredParam(params,
				"keyalg");
		final int keySize = Integer.parseInt(ParamUtils.getRequiredParam(
				params, "keysize"));
		final String keyStorePath = ParamUtils.getRequiredParam(params,
				"keystore");
		final String keyStorePass = ParamUtils.getRequiredParam(params,
				"storepass");
		final String keyStoreType = ParamUtils.getRequiredParam(params,
				"storetype");
		final String keyPass = ParamUtils.getOptionalParam(params, "keypass",
				keyStorePass);
		final String keyGenProviderClass = ParamUtils.getOptionalParam(params,
				"keygenproviderclass", null);

		SecretKey secretKey = generateSecretKey(keyAlgorithm, keySize,
				keyGenProviderClass);

		KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePass,
				keyStoreType, false); // the keystore is not required to exist

		keyStore.setKeyEntry(keyAlias, secretKey, keyPass.toCharArray(), null);

		saveKeyStore(keyStore, keyStorePath, keyStorePass);

	}

	/**
	 * Handles the "-delete" command-line option.
	 * 
	 * @throws Exception
	 *             error encountered while performing the operation
	 */
	private void deleteKey() throws Exception {

		final String keyAlias = ParamUtils.getRequiredParam(params, "alias");
		final String keyStorePath = ParamUtils.getRequiredParam(params,
				"keystore");
		final String keyStorePass = ParamUtils.getRequiredParam(params,
				"storepass");
		final String keyStoreType = ParamUtils.getRequiredParam(params,
				"storetype");

		KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePass,
				keyStoreType, true); // the keystore must exist

		if (!keyStore.containsAlias(keyAlias)) {
			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.NO.ENTRY.UNDER.ALIAS", new Object[] {
							keyAlias, keyStorePath }));
		}

		keyStore.deleteEntry(keyAlias);

		saveKeyStore(keyStore, keyStorePath, keyStorePass);
	}

	/**
	 * Handes the "-importkey" command-line option.
	 * 
	 * @throws Exception
	 *             error encountered while performing the operation
	 */
	private void importKey() throws Exception {

		final String srcKeyAlias = ParamUtils.getRequiredParam(params,
				"srcalias");
		final String srcKeyStorePath = ParamUtils.getRequiredParam(params,
				"srckeystore");
		final String srcKeyStorePass = ParamUtils.getRequiredParam(params,
				"srcstorepass");
		final String srcKeyStoreType = ParamUtils.getRequiredParam(params,
				"srcstoretype");
		final String srcKeyPass = ParamUtils.getOptionalParam(params,
				"srckeypass", srcKeyStorePass);

		final String destKeyAlias = ParamUtils.getRequiredParam(params,
				"destalias");
		final String destKeyStorePath = ParamUtils.getRequiredParam(params,
				"destkeystore");
		final String destKeyStorePass = ParamUtils.getRequiredParam(params,
				"deststorepass");
		final String destKeyStoreType = ParamUtils.getRequiredParam(params,
				"deststoretype");
		final String destKeyPass = ParamUtils.getOptionalParam(params,
				"destkeypass", destKeyStorePass);

		// The source keystore must exist.
		KeyStore srcKeyStore = loadKeyStore(srcKeyStorePath, srcKeyStorePass,
				srcKeyStoreType, true);

		KeyStore.Entry entry = CryptoFactory.obtainEntry(srcKeyStore,
				srcKeyAlias, srcKeyPass);

		if (entry == null) {
			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.NO.ENTRY.UNDER.ALIAS.2", new Object[] {
							srcKeyAlias, srcKeyStorePath }));
		}

		// The destination keystore is not required to exist.
		KeyStore destKeyStore = loadKeyStore(destKeyStorePath,
				destKeyStorePass, destKeyStoreType, false);

		destKeyStore.setEntry(destKeyAlias, entry,
				new KeyStore.PasswordProtection(destKeyPass.toCharArray()));

		saveKeyStore(destKeyStore, destKeyStorePath, destKeyStorePass);
	}

	/**
	 * Handles the "-list" command-line option.
	 * 
	 * @throws Exception
	 *             error encountered while performing the operation
	 */
	private void listKeyStore() throws Exception {

		final String keyStorePath = ParamUtils.getRequiredParam(params,
				"keystore");
		final String keyStorePass = ParamUtils.getRequiredParam(params,
				"storepass");
		final String keyStoreType = ParamUtils.getRequiredParam(params,
				"storetype");

		// The source keystore must exist.
		KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePass,
				keyStoreType, true);

		Enumeration aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = (String) aliases.nextElement();

			if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
				System.out.println(resHash.getString(
						"SECRET.KEY.TOOL.PRIVATE.KEY.ENTRY", alias));
			} else if (keyStore.entryInstanceOf(alias,
					KeyStore.TrustedCertificateEntry.class)) {
				System.out.println(resHash.getString(
						"SECRET.KEY.TOOL.CERT.ENTRY", alias));
			} else if (keyStore.entryInstanceOf(alias,
					KeyStore.SecretKeyEntry.class)) {
				System.out.println(resHash.getString(
						"SECRET.KEY.TOOL.SECRET.KEY.ENTRY", alias));
			} // else unknown type - skip it silently
		}
	}

	/**
	 * Handles the "-printseckey" command-line option.
	 * 
	 * @throws Exception
	 *             error encountered while performing the operation
	 */
	private void printSecretKey() throws Exception {

		final String keyAlias = ParamUtils.getRequiredParam(params, "alias");
		final String keyStorePath = ParamUtils.getRequiredParam(params,
				"keystore");
		final String keyStorePass = ParamUtils.getRequiredParam(params,
				"storepass");
		final String keyStoreType = ParamUtils.getRequiredParam(params,
				"storetype");
		final String keyPass = ParamUtils.getOptionalParam(params, "keypass",
				keyStorePass);

		// The source keystore must exist.
		KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePass,
				keyStoreType, true);

		if (!keyStore.entryInstanceOf(keyAlias, KeyStore.SecretKeyEntry.class)) {
			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.SECRET.KEY.NOT.FOUND", new Object[] {
							keyAlias, keyStorePath }));
		}

		KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) CryptoFactory
				.obtainEntry(keyStore, keyAlias, keyPass);
		SecretKey secretKey = entry.getSecretKey();

		/*
		 * According to the specs SecretKey.getEncoding always returns the raw
		 * bytes of the secret key
		 */
		byte[] rawKey = secretKey.getEncoded();
		int keySizeInBits = rawKey.length * 8;

		StringBuffer rawKeyStr = new StringBuffer();
		for (int i = 0; i < rawKey.length; ++i) {
			if (i > 0) {
				rawKeyStr.append(":");
			}
			rawKeyStr.append(StringUtils.toHex(rawKey[i]));
		}

		System.out.println(resHash.getString("SECRET.KEY.TOOL.KEY.INFORMATION",
				new Object[] { secretKey.getAlgorithm(),
						Integer.valueOf(keySizeInBits), rawKeyStr }));
	}

	/**
	 * Generates a secret key.
	 * 
	 * @param algorithm
	 *            the algorithm of the key
	 * @param keySize
	 *            the size of the key
	 * @param providerClass
	 *            Java security provider, which will be used for key generation
	 * @return the generated secret key
	 * @throws Exception
	 *             error while generating the key
	 */
	private static SecretKey generateSecretKey(String algorithm, int keySize,
			String providerClass) throws Exception {

		SecretKey secretKey = null;

		try {
			KeyGenerator keyGen = null;
			if (providerClass != null) {

				Provider provider = (Provider) Class.forName(providerClass)
						.newInstance();

				// The provider does not have to be reigstered
				keyGen = KeyGenerator.getInstance(algorithm, provider);
			} else {
				keyGen = KeyGenerator.getInstance(algorithm);
			}

			keyGen.init(keySize);
			secretKey = keyGen.generateKey();
		} catch (Exception ex) {

			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.KEY.GENERATION.FAILED", new Object[] {
							algorithm, Integer.valueOf(keySize), ex }));
		}

		return secretKey;
	}

	/**
	 * Utility method, which loads a keystore file into memory. Whether the
	 * existence of the keystore file is required depends on the 'fileMustExist'
	 * flag.
	 * 
	 * @param path
	 *            path to the keystore file
	 * @param pass
	 *            keystore password
	 * @param type
	 *            keystore type
	 * @param fileMustExist
	 *            whether the specified keystore file is supposed to exist
	 * @return the keystore in-memory object
	 * @throws Exception
	 *             error while loading the keystore
	 */
	private static KeyStore loadKeyStore(String path, String pass, String type,
			boolean fileMustExist) throws Exception {

		KeyStore keyStore = null;

		/*
		 * Passing a null stream on KeyStore.load means that an empty keystore
		 * will be created in memory.
		 */
		FileInputStream keyStoreInput = null;

		try {
			keyStore = KeyStore.getInstance(type);

			if (fileMustExist || new File(path).exists()) {
				keyStoreInput = new FileInputStream(path);
			}

			keyStore.load(keyStoreInput, pass.toCharArray());

		} catch (Exception ex) {

			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.CANNOT.LOAD.KEYSTORE", new Object[] {
							path, type, ex }));

		} finally {
			if (keyStoreInput != null) {
				keyStoreInput.close();
			}
		}

		return keyStore;
	}

	/**
	 * Stores a in-memory keystore on the file system.
	 * 
	 * @param keyStore
	 *            a in-memory keystore
	 * @param path
	 *            where the keystore will be stored on the file system
	 * @param pass
	 *            password to protect the keystore file
	 * @throws Exception
	 */
	private static void saveKeyStore(KeyStore keyStore, String path, String pass)
			throws Exception {

		FileOutputStream keyStoreOutput = null;

		try {

			keyStoreOutput = new FileOutputStream(path);
			keyStore.store(keyStoreOutput, pass.toCharArray());

		} catch (Exception ex) {

			throw new Exception(resHash.getString(
					"SECRET.KEY.TOOL.CANNOT.WRITE.KEYSTORE", new Object[] {
							path, keyStore.getType(), ex }));

		} finally {
			if (keyStoreOutput != null) {
				keyStoreOutput.close();
			}
		}
	}

}
