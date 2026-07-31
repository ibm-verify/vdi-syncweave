/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.security.spec.AlgorithmParameterSpec;
import java.security.Provider;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;

/**
 * Secret key encryption/decryption.
 * 
 * This class can work with secret key JCE transformations like the one that
 * <code>javax.crypto.Cipher.getInstance</code> accepts. It supports block
 * ciphers (e.g. AES) in various feedback modes (ECB, CBC, CFB, ...) as well as
 * stream ciphers (e.g. RC4).
 * 
 * Objects of this class are thread-safe.
 * 
 * @since 7.0
 */
public class SymmetricCipherCrypto implements Crypto {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the cryptography transformation, which will be used for
	 * encryption/decryption.
	 */
	private final String transformation;

	/**
	 * The secret key, which will be used for encryption/decryption.
	 */
	private final SecretKey secretKey;

	/**
	 * A Java security provider, which will be used to perform the
	 * transformation. If null, no explicit provider will be used, which means
	 * that the default provider list for the JRE will kick in. The provider
	 * does not need to be registered in the JRE.
	 */
	private final Provider cryptoProvider;

	/**
	 * The size of the initialization vector. If the feedback mode of the
	 * transformation does not use an IV, this field will be zero. This field
	 * will be zero for stream ciphers, because they do not employ a feedback
	 * mode. This field is derived of the transformation and the secret key. It
	 * is cached in the object state for better performance.
	 */
	private final int ivSize;

	/**
	 * Initializes the object with the specified parameters. Calculates the size
	 * of the initialization vector needed for the transformation. Accepts an
	 * optional Java security provider, which will be used for encryption. If
	 * the provider is set to null, the implementation will rely on the provider
	 * list configured for the JRE.
	 * 
	 * @param transformation
	 *            the name of a secret key transformation
	 * @param secretKey
	 *            a secret key, suitable for the cipher of the transformation
	 * @param cryptoProvider
	 *            a Java security provider
	 * @throws Exception
	 *             error by the underlying JCE provider
	 */
	public SymmetricCipherCrypto(String transformation, SecretKey secretKey,
			Provider cryptoProvider) throws Exception {

		this.transformation = transformation;
		this.secretKey = secretKey;
		this.cryptoProvider = cryptoProvider;

		/*
		 * Calculate the size of the initialization vector (IV) needed for the
		 * feedback mode of the transformation and cache it in the object's
		 * state.
		 */
		Cipher cipher = CryptoFactory.createCipher(transformation,
				cryptoProvider);
		initCipherForEncryption(cipher, secretKey);
		byte[] ivBytes = cipher.getIV();

		this.ivSize = (ivBytes != null) ? ivBytes.length : 0;

	}

	/**
	 * Encrypt data. If the feedback mode of the transformation requires an
	 * initialization vector (IV), a random one will be created. This makes the
	 * IV non-predictable. The encrypted data is prefixed with the IV (if
	 * required) as plaintext (the IV does not need to be kept secret).
	 */
	public byte[] encrypt(byte[] data) throws Exception {

		Cipher cipher = CryptoFactory.createCipher(transformation,
				cryptoProvider);

		initCipherForEncryption(cipher, secretKey);

		byte[] ivBytes = cipher.getIV();
		byte[] encryptedPayload = cipher.doFinal(data);

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		if (ivBytes != null) {
			result.write(ivBytes); // write the IV first
		}
		result.write(encryptedPayload);

		return result.toByteArray();
	}

	/**
	 * Decrypt data. If the transformation requires an initialization vector
	 * (IV), the IV used for encryption is assumed to be located in the
	 * beginning of the input buffer.
	 */
	public byte[] decrypt(byte[] encryptedData) throws Exception {

		Cipher cipher = CryptoFactory.createCipher(transformation,
				cryptoProvider);

		if (ivSize > 0) { // The feedback mode requires an IV

			AlgorithmParameterSpec param = null;

			if (transformation.startsWith("RC2")) {

				// RC2 requires specific handling

				int keySize = secretKey.getEncoded().length * 8;

				/*
				 * The IV of RC2 is always 8 bytes long, so the IV size is not
				 * specified:
				 */
				param = new RC2ParameterSpec(keySize, encryptedData);
			} else {
				param = new IvParameterSpec(encryptedData, 0, ivSize);
			}

			cipher.init(Cipher.DECRYPT_MODE, secretKey, param);

		} else { // The feedback mode does not require an IV.
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
		}

		// The encrypted payload comes after the IV in the buffer.
		return cipher.doFinal(encryptedData, ivSize, encryptedData.length
				- ivSize);
	}

	/**
	 * Initialize a Cipher object for encryption.
	 * 
	 * @param cipher
	 *            the cipher object, which will be initialized
	 * @param secretKey
	 *            a secret key, that the cipher will use for encryption
	 * @throws Exception
	 *             problem reported by the JCE framework
	 */
	private static void initCipherForEncryption(Cipher cipher,
			SecretKey secretKey) throws Exception {

    	    /*
	     * javax.crypto.Cipher.init will automatically generate a new random
	     * IV, if the feedback mode requires it.
	     */
	    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	}

}
