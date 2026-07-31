/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import javax.crypto.Cipher;
import java.security.Provider;

/**
 * RSA encryption/decryption of data of any length.
 * 
 * The pieces of data on which RSA can be normally operate are limited in size by the 
 * size of the RSA keys.
 * To workaround that limitation this class implements a custom scheme, which uses
 * RSA as a block cipher - the plaintext is divided into equally-sized blocks and 
 * each of them is RSA encrypted.
 * This approach allows encryption/decyption over data of any length.
 *
 * @since 7.0
 */
public class RSACrypto implements Crypto {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The transformation name.
	 */
	private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

	/**
	 * An adjustment to compensate for the space overhead of the padding.
	 * (Typically RSA is combined with some version of PKCS#1 Padding.)
	 */
	private static final int BLOCK_SIZE_REDUCTION = 11;

	/**
	 * A public key, used for encryption.
	 */
	private RSAPublicKey publicKey;

	/**
	 * A private key, used for decryption.
	 */
	private RSAPrivateKey privateKey;

	/**
	 * A Java security provider, which will be used to perform encryption/decryption.
	 * If null, no explicit provider will be used, which means that the default provider list for the JRE will kick in.
	 * The provider does not need to be registered in the JRE.
	 */
	private Provider cryptoProvider;

	/**
	 * Initialize the object with the specified parameters.
	 * Accepts an optional Java security provider, which will be used for encryption.
	 * If the provider is set to null, the implementation will rely on the provider list configured for the JRE.
	 * 
	 * @param publicKey a RSA public key
	 * @param privateKey a RSA private key
	 * @param cryptoProvider a Java security provider
	 */
	public RSACrypto(RSAPublicKey publicKey, RSAPrivateKey privateKey,
			Provider cryptoProvider) {

		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.cryptoProvider = cryptoProvider;
	}

	/**
	 * Encrypt data.
	 * The public key is used for encryption (opposite of signing where the private key is used).
	 * This way the encrypted data can be decrypted only using the private key.
	 * A security feature of the PKCS#1 padding, which is predominantly used with RSA, is that encryption
	 * produces a different ciphertext each time, despite the input plaintext stays the same.
	 * Of course, all of these ciphertexts will decrypt to the same plaintext.
	 */
	public byte[] encrypt(byte[] data) throws Exception {

		Cipher cipher = CryptoFactory.createCipher(ALGORITHM, cryptoProvider);

		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		int sourceLength = data.length;
		int outputSize = getOutputSize(publicKey);
		int blockSize = outputSize - BLOCK_SIZE_REDUCTION;
		int targetIndex = 0;
		int sourceIndex = 0;
		int chunks = sourceLength / blockSize;
		if (sourceLength % blockSize != 0) {
			chunks++;
		}
		byte[] encryptedData = new byte[chunks * outputSize];

		while (sourceIndex < sourceLength) {
			if ((sourceIndex + blockSize) > sourceLength) {
				blockSize = sourceLength - sourceIndex;
			}
			cipher.doFinal(data, sourceIndex, blockSize, encryptedData,
					targetIndex);

			sourceIndex += blockSize;
			targetIndex += outputSize;
		}

		return encryptedData;
	}

	/**
	 * Decrypt data.
	 * Decryption is done using the private key.
	 */
	public byte[] decrypt(byte[] encryptedData) throws Exception {

		Cipher cipher = CryptoFactory.createCipher(ALGORITHM, cryptoProvider);

		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		int outputSize = getOutputSize(privateKey);
		int size = encryptedData.length;
		int targetIndex = 0;
		int sourceIndex = 0;
		byte[] result = new byte[size];

		while (sourceIndex < size) {
			int bytesDecrypted = cipher.doFinal(encryptedData, sourceIndex,
					outputSize, result, targetIndex);
			targetIndex += bytesDecrypted;
			sourceIndex += outputSize;
		}

		return truncByteArray(result, targetIndex);
	}

	/**
	 * Get the first 'size' bytes of a byte array.
	 * 
	 * @param byteArray the source byte array
	 * @param size how many bytes to get
	 * @return an array with the first 'size' bytes of the source array
	 */
	private static byte[] truncByteArray(byte[] byteArray, int size) {
		byte[] result = new byte[size];
		System.arraycopy(byteArray, 0, result, 0, size);
		return result;
	}

	/**
	 * Get a close upper limit of the size of data an RSA key can work on.
	 * The return value is actually the modulus of the RSA key measured in bytes and rounded up.
	 * Generally a RSA key cannot operate on a piece of data, which represents a larger number
	 * than modulus of the RSA key.
	 * 
	 * @param key an RSA key
	 * @return an upper limit of the size of data, the RSA key can work on; in bytes
	 */
	private static int getOutputSize(RSAKey key) {

		BigInteger modulus = key.getModulus();
		int bitLength = modulus.bitLength();
		int outputSize = bitLength / 8;
		if (bitLength % 8 != 0) {
			outputSize++;
		}

		return outputSize;
	}

}
