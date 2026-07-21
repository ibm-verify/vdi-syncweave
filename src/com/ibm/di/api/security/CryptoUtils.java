/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.security;

import com.ibm.di.api.APIEngine;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.security.CryptoFactory;
import com.ibm.di.security.Crypto;
import com.ibm.di.util.ParamUtils;
import com.ibm.di.util.PropertiesFile;
import java.security.KeyStore;
import java.security.Provider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;

/**
 * This class represents the Server's cryptographic module. It is initialized by
 * the Server at startup.
 */
public class CryptoUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Encrypt User Registry. Encrypted files are written as binary and not
	 * prefixed by marker signature.
	 */
	public static final String MODE_ENCRYPT = "encrypt";

	/**
	 * Decrypt User Registry.
	 */
	public static final String MODE_DECRYPT = "decrypt";

	/**
	 * Encrypt a TDI configuration file. Encrypted files are written as binary
	 * and prefixed by marker signature.
	 */
	public static final String MODE_CENCRYPT = "encrypt_config";

	/**
	 * Decrypt a TDI configuration file.
	 */
	public static final String MODE_CDECRYPT = "decrypt_config";

	/**
	 * Encrypt a TDI properties file. The file is not encrypted as a whole -
	 * only values of protected properties are encrypted. Both the input and the
	 * output of the operation are text files which use the default encoding for
	 * the platform.
	 */
	public static final String MODE_PENCRYPT = "encrypt_props";

	/**
	 * Decrypt a TDI properties file. The file is not decrypted as a whole -
	 * only encrypted property values are decrypted. Both the input and the
	 * output of the operation are text files which use the default encoding for
	 * the platform.
	 */
	public static final String MODE_PDECRYPT = "decrypt_props";

	/**
	 * System property that specifies the path to the keystore which hosts the
	 * Server encryption key. The password for that keystore is located in the
	 * Server Stash File.
	 */
	public static final String ENCRYPTION_PROP_SERVER_KEYSTORE = "com.ibm.di.server.encryption.keystore";

	/**
	 * System property that specifies the type of the keystore which hosts the
	 * Server encryption key.
	 */
	public static final String ENCRYPTION_PROP_SERVER_KEYSTORE_TYPE = "com.ibm.di.server.encryption.keystoretype";

	/**
	 * System property that specifies the cryptographic transformation used by
	 * the Server for encryption. Can be either "RSA" or some secret key
	 * transformation, which a call to
	 * <code>javax.crypto.Cipher.getInstance</code> would accept. For example
	 * "AES/CBC/PKCS5Padding". The transformation must explicitly require a
	 * secret key. Password-based (PBE) transformations are not supported.
	 */
	public static final String ENCRYPTION_PROP_SERVER_TRANSFORMATION = "com.ibm.di.server.encryption.transformation";

	/**
	 * System property that specifies the alias of the Server encryption key.
	 */
	public static final String ENCRYPTION_PROP_SERVER_KEY_ALIAS = "com.ibm.di.server.encryption.key.alias";

	/**
	 * A marker string, whose UTF-8 binary representation is used to prefix
	 * encrypted configurations.
	 */
	private final static String SERVER_ENCRYPTED_SIGNATURE = "{PKI ENCRYPTED}\n";

	/**
	 * Whether this cryptographic module has been initialized.
	 */
	private static boolean isInitialized = false;

	/**
	 * This is the object, which performs the actual encryption/decryption. It
	 * is thread-safe.
	 */
	private static Crypto defaultCrypto = null;

	/**
	 * This is the default KeyStore that contains the server certificate.
	 */
	private static KeyStore defaultKeyStore = null;

	/**
	 * The default password for Certificates
	 */
	private static String keyPass = null;
	
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash resHash = APIEngine.getResHash();

	/**
	 * Initialize this module. This method must be called before using any of
	 * the other methods.
	 * 
	 * @param keyStorePass
	 *            a password for the keystore that hosts the Server encryption
	 *            key
	 * @param keyPass
	 *            a password for the key inside the keystore
	 * @throws Exception
	 *             the module is already initialized; a required system property
	 *             is missing; the key cannot be retrieved; the encryption
	 *             transformation is not supported; the key is not suitable for
	 *             the encryption transformation
	 */
	public static void init(String keyStorePass, String keyPass)
			throws Exception {

		if (isInitialized) {
			throw new Exception(resHash
					.getString("SEVER.API.SERVER.CRYPTO.ALREADY.INITIALIZED"));
		}

		String keyStorePath = ParamUtils
				.getRequiredProperty(ENCRYPTION_PROP_SERVER_KEYSTORE);
		String keyStoreType = ParamUtils
				.getRequiredProperty(ENCRYPTION_PROP_SERVER_KEYSTORE_TYPE);
		String keyAlias = ParamUtils
				.getRequiredProperty(ENCRYPTION_PROP_SERVER_KEY_ALIAS);
		String transformation = ParamUtils
				.getRequiredProperty(ENCRYPTION_PROP_SERVER_TRANSFORMATION);

		// Stash away info that might be needed later.
		defaultKeyStore = CryptoFactory.loadKeyStore(keyStorePath, keyStorePass, keyStoreType);
		CryptoUtils.keyPass = keyPass;
		
		// Create a thread-safe Crypto.
		try {
			defaultCrypto = CryptoFactory.createCrypto(defaultKeyStore,
				keyAlias, keyPass, transformation, null);
		} catch (Exception e) {
			ResourceHash rh = ResourceHash.getHash("miserver");
			throw new Exception(rh.getString("CryptoFactory.KeyStoreProblem", 
					new Object[]{keyStorePath, e.getMessage()}));			
		}

		isInitialized = true;

	}

	/**
	 * The entry-point of the cryptoutils command-line tool. Invoke with no
	 * arguments to print a brief usage manual.
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws Exception
	 *             operation error
	 */
	public static void main(String[] args) throws Exception {

		if (args.length == 0){
			showUsage();
			System.exit(-1);
		}

        Map<?, ?> params = null;
		String inputFileName = null;
		String outputFileName = null;
		String mode = null;
		String keyStorePath = null;
		String keyStorePass = null;
		String keyAlias = null;
        
		try{
			params = ParamUtils.parseCommandLine(args);
			// Get the mandatory parameters
			inputFileName = ParamUtils.getRequiredParam(params, "input");
			outputFileName = ParamUtils.getRequiredParam(params, "output");
			mode = ParamUtils.getRequiredParam(params, "mode");
			keyStorePath = ParamUtils.getRequiredParam(params, "keystore");
			keyStorePass = ParamUtils.getRequiredParam(params, "storepass");
			keyAlias = ParamUtils.getRequiredParam(params, "alias");
		} catch (Exception e){
			showUsage();
			System.exit(-1);
		}

		// Get the optional parameters
		String keyPass = ParamUtils.getOptionalParam(params, "keypass",
				keyStorePass);
		String transformation = ParamUtils.getOptionalParam(params,
				"transformation", "RSA");
		String keyStoreType = ParamUtils.getOptionalParam(params, "storetype",
				KeyStore.getDefaultType());
		String cryptoProviderClass = ParamUtils.getOptionalParam(params,
				"cryptoproviderclass", null);

		// Validate the parameters
		boolean isValidMode = MODE_ENCRYPT.equalsIgnoreCase(mode)
				|| MODE_DECRYPT.equalsIgnoreCase(mode)
				|| MODE_CENCRYPT.equalsIgnoreCase(mode)
				|| MODE_CDECRYPT.equalsIgnoreCase(mode)
				|| MODE_PENCRYPT.equalsIgnoreCase(mode)
				|| MODE_PDECRYPT.equalsIgnoreCase(mode);
		if (!isValidMode) {
			throw new Exception(resHash.getString(
					"SEVER.API.ILLEGAL.VALUE.FOR.MODE.PARAMETER", mode));
		}
		if (!new File(inputFileName).exists()) {
			throw new Exception(resHash.getString(
					"SEVER.API.INPUT.FILE.DOES.NOT.EXIST", inputFileName));
		}

		Provider provider = null;
		if (cryptoProviderClass != null
				&& cryptoProviderClass.trim().length() > 0) {
			provider = CryptoFactory.loadProvider(cryptoProviderClass);
		}
		
		Crypto crypto = CryptoFactory.createCrypto(keyStorePath, keyStorePass,
				keyStoreType, keyAlias, keyPass, transformation, provider);

		boolean isEncryptMode = MODE_ENCRYPT.equalsIgnoreCase(mode)
				|| MODE_CENCRYPT.equalsIgnoreCase(mode)
				|| MODE_PENCRYPT.equalsIgnoreCase(mode);

		boolean workOnPropertiesFile = mode.equalsIgnoreCase(MODE_PENCRYPT)
				|| mode.equalsIgnoreCase(MODE_PDECRYPT);

		if (workOnPropertiesFile) {

			transformPropertiesFile(inputFileName, outputFileName, crypto,
					isEncryptMode);

		} else {

			boolean expectSignatureWhenReading = MODE_CDECRYPT
					.equalsIgnoreCase(mode);
			boolean useSignatureWhenWriting = MODE_CENCRYPT
					.equalsIgnoreCase(mode);

			byte[] source = readFile(inputFileName, expectSignatureWhenReading);
			byte[] result = (isEncryptMode) ? crypto.encrypt(source) : crypto
					.decrypt(source);

			writeFile(outputFileName, result, useSignatureWhenWriting);
		}

	}

	/**
	 * Print a brief usage manual.
	 */
	private static void showUsage() {
		System.out.println(resHash.getString("SEVER.API.CRYPTO.UTILS.USAGE",
				new Object[] { MODE_ENCRYPT, MODE_DECRYPT, MODE_CENCRYPT,
						MODE_CDECRYPT, MODE_PENCRYPT, MODE_PDECRYPT }));
	}

	/**
	 * Read a whole file as binary.
	 * 
	 * @param fileName
	 *            file to read
	 * @return file contents
	 * @throws IOException
	 *             error while reading the file
	 */
	public static byte[] readFile(String fileName) throws IOException {
		return readFile(fileName, false);
	}

	/**
	 * Read a whole file as binary. Can verify that the file is prefixed with an
	 * encryption marker (signature). If signature verification is performed,
	 * the returned data does not include the signature bytes.
	 * 
	 * @param fileName
	 *            file to read
	 * @param useSignature
	 *            whether to verify encryption signature
	 * @return file contents
	 * @throws IOException
	 *             the encryption signature is invalid or an error occurred
	 *             while reading the file
	 */
	private static byte[] readFile(String fileName, boolean useSignature)
			throws IOException {
		
		byte[] byteArray = null;
		FileInputStream inputStream = new FileInputStream(fileName);
		
		try {
			int size = inputStream.available();
			byteArray = new byte[size];
			if (size > 0) {
				inputStream.read(byteArray);
			}
			if (useSignature) {
				// Verify signature
				byte[] signature = SERVER_ENCRYPTED_SIGNATURE.getBytes("UTF-8");
				for (int i = 0; i < signature.length; i++) {
					if (byteArray[i] != signature[i]) {
						throw new IOException(
								resHash
										.getString("SEVER.API.CRYPTO.UTILS.WRONG.SIGNATURE"));
					}
				}
				byte[] configArray = new byte[size - signature.length];
				for (int i = 0; i < configArray.length; i++) {
					configArray[i] = byteArray[i + signature.length];
				}
				byteArray = configArray;
			}
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		return byteArray;
	}

	/**
	 * Write a file to disk. The file can be prefixed with an encryption marker
	 * (signature), to signal that it is encrypted.
	 * 
	 * @param fileName
	 *            file name
	 * @param data
	 *            file contents
	 * @param useSignature
	 *            whether to prefix the file with an encryption signature
	 * @throws IOException
	 *             error while writing the file
	 */
	private static void writeFile(String fileName, byte[] data,
			boolean useSignature) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if (useSignature) {
			outputStream.write(SERVER_ENCRYPTED_SIGNATURE.getBytes("UTF-8"));
		}
		outputStream.write(data);
		
		FileOutputStream fos = new FileOutputStream(fileName);
		try {
			outputStream.writeTo(fos);
		} finally {
			fos.close();
		}
	}

	/**
	 * Encrypt/decrypt a TDI properties file. The file is not transformed as a
	 * whole, but rather only the values of protected properties are
	 * encrypted/decrypted.
	 * 
	 * @param inputFileName
	 *            input properties file
	 * @param outputFileName
	 *            output properties file
	 * @param crypto
	 *            a cryptographic object used for encryption/decryption
	 * @param encrypt
	 *            if set to true, the values of all protected properties will be
	 *            encrypted; if set to false, the values of all protected
	 *            properties will be decrypted
	 * @throws Exception
	 *             I/O or cryptography-related error
	 * @since 7.0
	 */
	private static void transformPropertiesFile(String inputFileName,
			String outputFileName, Crypto crypto, boolean encrypt)
			throws Exception {

		PropertiesFile propsFile = new PropertiesFile(crypto, inputFileName,
				false);

		Iterator<?> it = propsFile.keys();
		while (it.hasNext()) {
			String key = (String) it.next();

			if (propsFile.isPropertyProtected(key)) {
				propsFile.setPropertyEncrypted(key, encrypt);
			}
		}

		propsFile.store(outputFileName, null, null);
	}

	/**
	 * Decrypt User Registry contents with the Server encryption key.
	 * 
	 * @param data
	 *            User Registry contents
	 * @return decrypted data
	 * @throws Exception
	 *             this module is not initialized or some cryptographic error
	 *             occurred
	 */
	public static byte[] decryptSecurityRegistry(byte[] data) throws Exception {

		verifyInitialized();

		return decryptWithServerKey(data);
	}

	/**
	 * Encrypt data with the Server encryption key.
	 * 
	 * @param data
	 *            data to encrypt
	 * @return encrypted data
	 * @throws Exception
	 *             this module is not initialized or some cryptographic error
	 *             occurred
	 */
	public static byte[] encryptWithServerKey(byte[] data) throws Exception {

		verifyInitialized();

		return defaultCrypto.encrypt(data);
	}

	/**
	 * Decrypt data with the Server encryption key.
	 * 
	 * @param data
	 *            encrypted data
	 * @return decrypted data
	 * @throws Exception
	 *             this module is not initialized or some cryptographic error
	 *             occurred
	 */
	public static byte[] decryptWithServerKey(byte[] data) throws Exception {

		verifyInitialized();

		return defaultCrypto.decrypt(data);
	}

	/**
	 * Retrieve an object representation of the Server's encryption/decryption
	 * functionality. The returned object is thread-safe.
	 * 
	 * @return Server's cryptographic object
	 * @throws Exception
	 *             this module is not initialized
	 */
	public static Crypto getDefaultCrypto() throws Exception {

		verifyInitialized();

		return defaultCrypto;
	}

	/**
	 * Create a Crypto object using a specified Certificate.
	 * 
	 * @param keyAlias The alias for the Certificate
	 * @param transformation The Crypto algorithm/transformation to use
	 * @return a cryptographic object
	 * @throws Exception
	 *             this module is not initialized
	 * @since 7.1
	 */
	public static Crypto getCrypto(String keyAlias, String transformation) throws Exception {

		verifyInitialized();

		String keyStorePath = ParamUtils.getRequiredProperty(ENCRYPTION_PROP_SERVER_KEYSTORE);
		if (transformation == null || transformation.length() == 0)
			transformation = ParamUtils.getRequiredProperty(ENCRYPTION_PROP_SERVER_TRANSFORMATION);

		// Create a thread-safe Crypto.
		try {
			return CryptoFactory.createCrypto(defaultKeyStore,
				keyAlias, keyPass, transformation, null);
		} catch (Exception e) {
			ResourceHash rh = ResourceHash.getHash("miserver");
			throw new Exception(rh.getString("CryptoFactory.KeyStoreProblem", 
				new Object[]{keyStorePath, e.getMessage()}));			
		}
	}

	/**
	 * Returns a list of the server Certificate aliases.
	 * An empty string is the first element in the array, for convenience.
	 * @return a list of the server Certificate aliases
	 * @throws Exception if this module is not initialized
	 * @since 7.1
	 */
	public static String[] getKeyStoreAliases() throws Exception {
		verifyInitialized();
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("");
		Enumeration<String> e = defaultKeyStore.aliases();
		while (e.hasMoreElements()) {
			list.add(e.nextElement());
		}
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Verifies that this module is initialized.
	 * 
	 * @throws Exception
	 *             if the module is not initialized
	 */
	private static void verifyInitialized() throws Exception {

		if (!isInitialized) {
			throw new Exception(
					resHash
							.getString("SEVER.API.CRYPTO.UTILS.SERVER.CRYPTO.NOT.INITIALIZED"));
		}
	}

}
