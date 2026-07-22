/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// SecurityCrypto.java
//
//
//
package com.ibm.di.security;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.StringUtils;

import java.util.Locale;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class SecurityCrypto {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private Cipher cipher;

	private SecretKey key;

	private String transformation;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public SecurityCrypto(String keyPath) throws Exception {
		this(keyPath, null);
	}

	public SecurityCrypto(String keyPath, String cipherAlg) throws Exception {
		this(keyPath, cipherAlg, null);
	}

	/**
	 * Creates SecurityCrypto Object with the provided parameters. The object
	 * contains the key, cipher and algorithm used.
	 * 
	 * @param keyPath
	 *            this parameter contains the secret key.
	 * @param cipherAlg
	 *            this is the cipher algorithm which will be used to create the
	 *            cipher. If this parameter is null then the value of the
	 *            property "com.ibm.di.securityTransformation" will be used. If
	 *            the property has no value then a default value for the
	 *            algorithm will be used - DES/ECB/NoPadding.
	 * @param provider
	 *            this is the security provider which will be used to create the
	 *            cipher. If the parameter is null then the
	 *            "com.ibm.di.cryptoProviderName" property will be checked for
	 *            the name of an already registered security provider and if not
	 *            found then the cipher will be created without an explicit
	 *            security provider.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SecurityCrypto(String keyPath, String cipherAlg, Provider provider)
			throws Exception {
		transformation = cipherAlg;
		if (transformation == null || transformation.trim().length() == 0) {
			transformation = System
					.getProperty("com.ibm.di.securityTransformation");
		}
		boolean NIST = Boolean.getBoolean("com.ibm.di.server.NIST.on");

		if (transformation == null || transformation.trim().length() == 0)
		{
			if(NIST)
				transformation = "AES/ECB/NoPadding";
			else
				transformation = "DES/ECB/NoPadding";
		}

		// -- des/ecb/none is no longer valid with ibm jse 1.4.2
		if (transformation.equalsIgnoreCase("DES/ECB/None"))
			transformation = "DES/ECB/NoPadding";

		cipher = CryptoFactory.createCipher(transformation, provider);

		String algorithm = transformation;
		if (transformation.indexOf('/') != -1)
			algorithm = transformation
					.substring(0, transformation.indexOf('/'));

		// Force key lengths of 64 for DES and 128 for AES (or they choke)
		StringBuffer keyBuffer = new StringBuffer(keyPath);
		if (algorithm.toUpperCase(Locale.ENGLISH).startsWith("DES")) {
			while (keyBuffer.length() < 8)
				keyBuffer.append( " " );
		} else if (algorithm.toUpperCase(Locale.ENGLISH).startsWith("AES")) {
			while (keyBuffer.length() < 16)
				keyBuffer.append( " " );
		}
		byte[] keyData = keyBuffer.toString().getBytes("UTF-8");

		// Special handling for DES algorithm. Other algorithms might also need
		// this.
		// The cause for this is that SecrectKeySpec sometimes does not create a
		// useful key,
		// maybe due to some problem with IBM's JVM.
		if (algorithm.equalsIgnoreCase("DES")) {
			byte[] desKeyData = new byte[8];
			for (int i = 0; i < 8; i++)
				desKeyData[i] = keyData[i];

			DESKeySpec desKeySpec = new DESKeySpec(desKeyData);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			key = keyFactory.generateSecret(desKeySpec);
		} else if (algorithm.equalsIgnoreCase("AES")) {
			byte[] desKeyData = new byte[16];
			for (int i = 0; i < 16; i++)
				desKeyData[i] = keyData[i];

			key = new SecretKeySpec(keyData, algorithm);
		} else {
			key = new SecretKeySpec(keyData, algorithm);
		}
	}

	public String getDecrypted(String input) throws Exception {
		String decode;
		int pos;
		int size;

		pos = input.indexOf(" ");
		if (pos < 0) {
			throw new Exception(sResHash.getString("exception.count.sp.data"));
		}

		size = Integer.parseInt(input.substring(0, pos));
		decode = input.substring(pos + 1);
		byte[] data = new byte[(decode.length() / 2)];
		int index = 0;

		for (pos = 0; pos < data.length; pos++, index += 2) {
			data[pos] = StringUtils.fromHex(decode.substring(index, index + 2));
		}

		byte[] dec = decrypt(data);

		return new String(dec, 0, size);
	}

	public String getEncrypted(byte[] input) throws Exception {
		StringBuffer str = new StringBuffer(Integer.toString(input.length)
				+ " ");

		byte[] b = encrypt(input);

		for (int i = 0; i < b.length; i++) {
			str.append(StringUtils.toHex(b[i]));
		}
		str.append("\r\n");

		return str.toString();
	}

	public byte[] encrypt(byte[] input) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key);

		int size = input.length;
		int bs = cipher.getBlockSize();
		int pad = ((size % bs) == 0 ? 0 : bs - (size % bs));
		byte[] buffer = input;

		if (pad != 0) {
			buffer = new byte[size + pad];
			System.arraycopy(input, 0, buffer, 0, input.length);
			for (int i = 0; i < pad; i++)
				buffer[input.length + i] = ' ';
		}

		byte[] result = cipher.doFinal(buffer);
		return result;
	}

	public byte[] decrypt(byte[] input) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] result = cipher.doFinal(input);
		return result;
	}
}
