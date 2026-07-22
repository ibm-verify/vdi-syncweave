/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import com.ibm.di.server.ResourceHash;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

/**
 * This factory creates objects, which can be used for encryption/decryption of
 * data. The purpose of this class is to shield other code from dealing with
 * keys and keystores and knowing concrete Crypto implementations.
 * 
 * @since 7.0
 */
public class CryptoFactory {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	/**
	 * <p>
	 * This system property specifies the Java class of a the default security
	 * provider that will be used for encryption/decryption. This provider will
	 * NOT be used for keystore management. The property is optional - if it is
	 * missing the default security provider list of the JRE will be used.
	 * </p>
	 * <p>
	 * This property must be interpreted by the application (e.g. the Server) on
	 * initialization. The specified provider class has to be instantiated and
	 * registered in the default security provider list of the JRE (e.g. using
	 * {@link java.security.Security#addProvider(Provider)}). Moreover the
	 * {@link #CRYPTO_PROVIDER_NAME_PROPERTY} needs to be set to the name of the
	 * provider, so that other security classes can take advantage of it via the
	 * {@link #createCipher(String, Provider)} method. Note that it is better
	 * for the large part of the code to access the provider by name from the
	 * default provider list rather than instantiate it every time because
	 * instantiation may be heavy-weight, may require security permissions or
	 * may require a specific class loader (e.g. the system class loader).
	 * </p>
	 */
	public static final String CRYPTO_PROVIDER_CLASS_PROPERTY = "com.ibm.di.cryptoProvider";

	/**
	 * <p>
	 * This system property sets the name of a registered Java security provider
	 * that will be used as the default provider for encryption/decryption. This
	 * provider will NOT be used for keystore management. The property is
	 * optional - if it is missing the default security provider list of the JRE
	 * will be used.
	 * </p>
	 * <p>
	 * This property is dependent on the value of the
	 * {@link #CRYPTO_PROVIDER_CLASS_PROPERTY} property. Whenever you set the
	 * {@link #CRYPTO_PROVIDER_CLASS_PROPERTY} property, you should update this
	 * property too.
	 * </p>
	 */
	public static final String CRYPTO_PROVIDER_NAME_PROPERTY = "com.ibm.di.cryptoProviderName";

	/**
	 * <p>
	 * Creates an object that can encrypt/decrypt data using the specified
	 * cryptography transformation.
	 * </p>
	 * 
	 * <p>
	 * The transformation can be either "RSA" or some secret key transformation,
	 * which a call to <code>javax.crypto.Cipher.getInstance</code> would
	 * accept. For example "AES/CBC/PKCS5Padding". The transformation must
	 * explicitly require a secret key. Password-based (PBE) transformations are
	 * not supported by this method.
	 * </p>
	 * 
	 * <p>
	 * If the transformation is set to "RSA", the specified keystore must
	 * contain a private key entry under the specified key alias. If the
	 * transformation involves a secret key cipher, the keystore must contain a
	 * secret key for that cipher under the key alias.
	 * </p>
	 * 
	 * <p>
	 * The returned object will be thread-safe.
	 * </p>
	 * 
	 * @param keyStorePath
	 *            a keystore file, which contains the key for
	 *            encryption/decryption
	 * @param keyStorePass
	 *            the password of the keystore file
	 * @param keyStoreType
	 *            the type of the keystore file
	 * @param keyAlias
	 *            the alias of the key
	 * @param keyPass
	 *            the password of the key
	 * @param transformation
	 *            the name of the cryptography transformation
	 * @param cryptoProvider
	 *            a Java security provider that will be used for
	 *            encryption/decryption; this provider will not be used for
	 *            reading keystores; the provider does not have to be registered
	 *            in the JRE; the parameter is optional - if it is set to null,
	 *            the value of the "com.ibm.di.cryptoProviderName" system
	 *            property will be used, if it is missing the default provider
	 *            list for the JRE will be used
	 * @return an object that implements the specified cryptography
	 *         transformation
	 * @throws Exception
	 *             If the transformation is "RSA" and the keystore does not
	 *             contain a public/private key pair under the specified alias
	 *             or if the keys are not suited for RSA. If the transformation
	 *             involves a secret key cipher and the keystore does not
	 *             contain a secret key under the specified alias.
	 */
	public static Crypto createCrypto(String keyStorePath, String keyStorePass,
			String keyStoreType, String keyAlias, String keyPass,
			String transformation, Provider cryptoProvider) throws Exception {

		try {
			return createCrypto(loadKeyStore(keyStorePath, keyStorePass, keyStoreType),
				keyAlias, keyPass, transformation, cryptoProvider);
		} catch (Exception e) {
			throw new Exception(resHash.getString("CryptoFactory.KeyStoreProblem", 
					new Object[]{keyStorePath, e.getMessage()}));
		}
		
	}
	
	/**
	 * <p>
	 * Creates an object that can encrypt/decrypt data using the specified
	 * cryptography transformation.
	 * </p>
	 * 
	 * <p>
	 * The transformation can be either "RSA" or some secret key transformation,
	 * which a call to <code>javax.crypto.Cipher.getInstance</code> would
	 * accept. For example "AES/CBC/PKCS5Padding". The transformation must
	 * explicitly require a secret key. Password-based (PBE) transformations are
	 * not supported by this method.
	 * </p>
	 * 
	 * <p>
	 * If the transformation is set to "RSA", the specified keystore must
	 * contain a private key entry under the specified key alias. If the
	 * transformation involves a secret key cipher, the keystore must contain a
	 * secret key for that cipher under the key alias.
	 * </p>
	 * 
	 * <p>
	 * The returned object will be thread-safe.
	 * </p>
	 * 
	 * @param keyStore
	 *            the KeyStore to use
	 * @param keyStorePath
	 *            path for the keystore file that was used to construct the KeyStore
	 * @param keyAlias
	 *            the alias of the key
	 * @param keyPass
	 *            the password of the key
	 * @param transformation
	 *            the name of the cryptography transformation
	 * @param cryptoProvider
	 *            a Java security provider that will be used for
	 *            encryption/decryption; this provider will not be used for
	 *            reading keystores; the provider does not have to be registered
	 *            in the JRE; the parameter is optional - if it is set to null,
	 *            the value of the "com.ibm.di.cryptoProviderName" system
	 *            property will be used, if it is missing the default provider
	 *            list for the JRE will be used
	 * @return an object that implements the specified cryptography
	 *         transformation
	 * @throws Exception
	 *             If the transformation is "RSA" and the keystore does not
	 *             contain a public/private key pair under the specified alias
	 *             or if the keys are not suited for RSA. If the transformation
	 *             involves a secret key cipher and the keystore does not
	 *             contain a secret key under the specified alias.
	 * @since 7.1
	 */
	public static Crypto createCrypto(KeyStore keyStore, String keyAlias, String keyPass,
			String transformation, Provider cryptoProvider) throws Exception {

		Crypto crypto = null;

		if ("RSA".equalsIgnoreCase(transformation)) {

			/*
			 * Verify that a public/private key pair exists under the specified
			 * alias.
			 */
			if (!keyStore.entryInstanceOf(keyAlias,
					KeyStore.PrivateKeyEntry.class)) {
				throw new Exception(resHash.getString(
						"CRYPTO.FACTORY.NO.PRIVATE.KEY.ENTRY", new Object[] {
								keyAlias, transformation }));
			}

			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) obtainEntry(
					keyStore, keyAlias, keyPass);

			PublicKey publicKey = privateKeyEntry.getCertificate()
					.getPublicKey();
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();

			/*
			 * Verify that the public/private keys are suited for the RSA
			 * algorithm.
			 */
			if (!(publicKey instanceof RSAPublicKey)
					|| !(privateKey instanceof RSAPrivateKey)) {
				throw new Exception(resHash.getString(
						"CRYPTO.FACTORY.NON.RSA.KEYS", new Object[] { keyAlias,
								privateKey.getAlgorithm(), transformation }));
			}

			crypto = new RSACrypto((RSAPublicKey) publicKey,
					(RSAPrivateKey) privateKey, cryptoProvider);

		} else { // This is a secret key transformation.

			// Verify that a secret key exists under the specified alias.
			if (!keyStore.entryInstanceOf(keyAlias,
					KeyStore.SecretKeyEntry.class)) {
				throw new Exception(resHash.getString(
						"CRYPTO.FACTORY.NO.SECRET.KEY.ENTRY", new Object[] {
								keyAlias, transformation }));
			}

			KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) obtainEntry(
					keyStore, keyAlias, keyPass);

			crypto = new SymmetricCipherCrypto(transformation, secretKeyEntry
					.getSecretKey(), cryptoProvider);
		}

		return crypto;
	}

	/**
	 * Loads a keystore into memory.
	 * 
	 * @param keyStorePath
	 *            keystore file path
	 * @param keyStorePass
	 *            keystore password
	 * @param keyStoreType
	 *            keystore type
	 * @return the keystore in-memory object
	 * @throws Exception
	 *             problem while reading the keystore
	 */
	public static KeyStore loadKeyStore(String keyStorePath,
			String keyStorePass, String keyStoreType) throws Exception {

		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		FileInputStream keyStoreInput = new FileInputStream(keyStorePath);
		try {
			keyStore.load(keyStoreInput, keyStorePass.toCharArray());
		} finally {
			// Ensure no file handles are left open
			if (keyStoreInput != null) {
				keyStoreInput.close();
			}
		}

		return keyStore;
	}

	/**
	 * Obtain an entry from a keystore. (Provides a user friendly message when
	 * the password for the entry is wrong.)
	 * 
	 * @param keyStore
	 *            initialized keystore object
	 * @param alias
	 *            the entry in the keystore to obtain
	 * @param pass
	 *            password for the entry
	 * @return the obtained entry
	 * @throws Exception
	 *             if the password for the entry is incorrect, or the entry is
	 *             missing
	 */
	public static KeyStore.Entry obtainEntry(KeyStore keyStore, String alias,
			String pass) throws Exception {

		KeyStore.Entry entry = null;
		try {
			entry = keyStore.getEntry(alias, new KeyStore.PasswordProtection(
					pass.toCharArray()));
		} catch (UnrecoverableEntryException ex) {

			/*
			 * According to the specs this exception is thrown when the password
			 * used to access the entry is incorrect. Explain that to the user.
			 */
			throw new Exception(resHash.getString(
					"CRYPTO.FACTORY.WRONG.ENTRY.PASSWORD", new Object[] {
							alias, ex }));
		}

		return entry;
	}
	
	/**
	 * A convenience method for instantiating a Cipher from a given security
	 * provider. If null is specified for the provider, the
	 * {@link #CRYPTO_PROVIDER_NAME_PROPERTY} property will be searched for the
	 * name of an already registered provider. If the property is empty or
	 * refers to a non-registered provider, the returned Cipher will rely on the
	 * list of security providers for the JRE.
	 * 
	 * @param transformation
	 *            a transformation valid for
	 *            <code>javax.crypto.Cipher.getInstance</code>
	 * @param cryptoProvider
	 *            Java security provider, which supports the specified
	 *            transformation
	 * @return the created Cipher
	 * @throws Exception
	 *             error, reported by the provider
	 */
	public static Cipher createCipher(String transformation,
			Provider cryptoProvider) throws Exception {

		Cipher cipher;
		if (cryptoProvider != null) {
			cipher = Cipher.getInstance(transformation, cryptoProvider);
		} else {
			String provName = System
					.getProperty(CRYPTO_PROVIDER_NAME_PROPERTY);
			if (provName != null) {
				provName = provName.trim();
			}
			if (provName != null && !"".equals(provName)) {
				cipher = Cipher.getInstance(transformation, provName);
			} else {
				cipher = Cipher.getInstance(transformation);
			}
		}

		return cipher;
	}
	
	/**
	 * 
	 * @param providerClassName
	 *            Fully qualified Java class name of the security provider.
	 * @return An instance of the provider.
	 * @throws Exception
	 *             If the provider class cannot be found of the provider cannot
	 *             be instantiated.
	 */
	public static Provider loadProvider(String providerClassName)
			throws Exception {
		/*
		 * The IBMJCEFIPS 1.2 provider of IBM Java 5 throws a
		 * NullPointerException on self test when accessed through the Eclipse
		 * OSGi bundle class loader (used by the CE), so use the system class
		 * loader instead.
		 */
		ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
		Class<?> provClass = sysLoader.loadClass(providerClassName);
		return (Provider) provClass.newInstance();
	}
}
