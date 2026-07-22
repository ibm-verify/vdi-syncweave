/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.security;

/**
 * Implementations of this interface encrypt/decrypt binary data.
 * 
 * @since 7.0
 */
public interface Crypto {

	/**
	 * Encrypt data. There is no guarantee that on different invocations the
	 * method will produce the same ciphertext on the same plaintext.
	 * 
	 * @param data
	 *            plaintext
	 * @return ciphertext
	 * @throws Exception
	 *             problem with encryption
	 */
	byte[] encrypt(byte[] data) throws Exception;

	/**
	 * Decrypt data.
	 * 
	 * @param encryptedData
	 *            ciphertext
	 * @return plaintext
	 * @throws Exception
	 *             problem with decryption
	 */
	byte[] decrypt(byte[] encryptedData) throws Exception;

}
