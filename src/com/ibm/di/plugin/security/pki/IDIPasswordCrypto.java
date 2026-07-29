/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.security.pki;

/**
 * Requirement: Usage of this class requires the installation of
 *        the IBMJCE provider on the running machine and the CLASSPATH
 *        points to the classes. Use of JKS file to store certificate containing
 *        the public key for encryption, or private key for decryption.
 *
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

import com.ibm.di.server.ResourceHash;

public class IDIPasswordCrypto {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	private static final String sAlgo = "RSA";

	private static final String sStoreType = "JKS";

	private static final String EMPTY_STRING = "ibmdiEmptyString";

	private static final String UTF8_ENCODING = "UTF-8";

	private static Provider provider;

	private static Cipher cipher;

	private static KeyStore keyStore;
	
	private static String keyStorePath = "";

	/**
	 * getCipher: Obtain Cipher object
	 * 
	 * @return Cipher
	 * @throws Exception
	 *             when unable to add provider
	 * 
	 */
	private static Cipher getCipher() throws java.lang.Exception {
		if (cipher == null) {

			try {
				cipher = Cipher.getInstance(sAlgo);
			} catch (Exception ex) {
				throw new java.lang.Exception(resHash.getString(
						"PWCRYPTO.CIPHER.INIT.FAILED", new Object[] { sAlgo,
								"", ex }), ex);
			}
		}
		return cipher;
	}

	/**
	 * getKeyStore: Obtain if not set, and return keystore
	 * 
	 * @param path
	 *            String representing file path to jks file
	 * @param password
	 *            String representing password for jks file as specified by path
	 * @return KeyStore
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	private static KeyStore getKeyStore(String path, String password)
			throws java.lang.Exception {
		// return if present, otherwise load keystore
		// from the specified provider.

		if (keyStore == null || !keyStorePath.equals(path)) {
			loadKeyStore(path, password);
			keyStorePath = path;
		}

		return keyStore;
	}

	/**
	 * loadKeyStore: Sets keyStore attributef
	 * 
	 * @param path
	 *            String representing file path to jks file
	 * @param password
	 *            String representing password for jks file as specified by path
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	private static synchronized void loadKeyStore(String path, String password)
			throws java.lang.Exception {
		// Generates a keystore object for the specified keystore type
		// from the specified provider.

		File ff = null;

		// given a path, create a stream and load keystore file
		try {
			keyStore = KeyStore.getInstance(sStoreType);
			ff = new File(path);
			if (!ff.exists()) {
				throw new java.lang.Exception(resHash.getString(
						"PWCRYPTO.KEY.STORE.FILE.NOT.FOUND", path));
			}

			InputStream streamIn = null;
			try {
				streamIn = new FileInputStream(ff);
				keyStore.load(streamIn, password.toCharArray());
			} finally {
				if (streamIn != null) {
					streamIn.close();
				}
			}

		} catch (Exception ex) {
			keyStore = null;
			throw new java.lang.Exception(resHash
					.getString("PWCRYPTO.KEY.STORE.OPEN.FAILED", new Object[] {
							path, ex }), ex);
		}

	}

	/**
	 * getCertificate: Obtain certificate from keystore
	 * 
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return Certificate
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	private static Certificate getCertificate(String ksPath, String ksPassword,
			String certificateAlias) throws java.lang.Exception {
		Certificate cert = null;

		try {
			KeyStore ks = getKeyStore(ksPath, ksPassword);
			cert = ks.getCertificate(certificateAlias);
			if (cert == null) {
				// ks.getCertificate will return null if and not throw exc.
				// most likely the wrong alias was specified
				throw new java.lang.Exception(resHash.getString(
						"PWCRYPTO.CERT.NOT.FOUND", new Object[] {
								certificateAlias, ksPath }));
			}

		} catch (Exception e) {
			throw new java.lang.Exception(resHash.getString(
					"PWCRYPTO.CERT.RETRIEVAL.FAILED", new Object[] {
							certificateAlias, ksPath, e }), e);
		}

		return cert;
	}

	/**
	 * getPrivateKey: Obtain private Key from keystore
	 * 
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return Key
	 * @throws Exception
	 *             when underlying function fails
	 */
	private static Key getPrivateKey(String ksPath, String ksPassword,
			String certificateAlias, String certificatePassword)
			throws java.lang.Exception {
		Key privatekey = null;
		try {

			KeyStore ks = getKeyStore(ksPath, ksPassword);
			privatekey = ks.getKey(certificateAlias, certificatePassword
					.toCharArray());

		} catch (Exception e) {
			throw new java.lang.Exception(resHash.getString(
					"PWCRYPTO.PRIVATE.KEY.RETRIEVAL.FAILED", new Object[] {
							certificateAlias, ksPath, e }), e);
		}
		return privatekey;
	}

	/**
	 * getPublicKey: Obtain public Key from keystore
	 * 
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return Key
	 * @throws Exception
	 *             when underlying function fails
	 */
	private static PublicKey getPublicKey(String ksPath, String ksPassword,
			String certificateAlias) throws java.lang.Exception {
		PublicKey pubKey = null;

		try {
			Certificate cert = getCertificate(ksPath, ksPassword,
					certificateAlias);

			pubKey = cert.getPublicKey();

		} catch (Exception ex) {
			throw new java.lang.Exception(resHash.getString(
					"PWCRYPTO.PUBLIC.KEY.RETRIEVAL.FAILED", new Object[] {
							certificateAlias, ksPath, ex }), ex);
		}

		return pubKey;
	}

	/**
	 * encrypt: Obtain encrypted (and ascii-encoded) value for plaintext
	 * specified, null strings are not processed and will be returned as null.
	 * 
	 * @param plainText
	 *            String representing value to be encrypted
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return String representing encrypted format, null is returned if a null
	 *         is passed in.
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	public static String encrypt(String plainText, String ksPath,
			String ksPassword, String certificateAlias)
			throws java.lang.Exception {

		String returnStr = null; // to be returned
		byte[] plainBytes = null;

		// only encode/ecrypt non-null
		if (notNull(plainText)) {

			if (emptyString(plainText)) {
				plainBytes = (EMPTY_STRING).getBytes(UTF8_ENCODING);
			} else {
				plainBytes = plainText.getBytes(UTF8_ENCODING);
				// setup for call
			}
			// obtain pks encrypted format
			byte[] encryptedBytes = encrypt(plainBytes, ksPath, ksPassword,
					certificateAlias);
			returnStr = convertToASCI(encryptedBytes);
		} else {
			returnStr = null;
		}

		return returnStr;
	}

	/**
	 * decrypt: Obtain plain ascii text for encrypted ciphertext specified. Null
	 * strings are not processed and will be returned as received. Empty strings
	 * will be encoded/encrypted.
	 * 
	 * @param cipherText
	 *            String representing value to be decrypted
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @param certificatePassword
	 *            String representing password for the certificate's private key
	 * @return String representing the decrypted format of the received string.
	 *         Null is returned when a null is received.
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	public static String decrypt(String cipherText, String ksPath,
			String ksPassword, String certificateAlias,
			String certificatePassword) throws java.lang.Exception {

		String returnString = null;
		if (notNull(cipherText)) {

			byte[] cipherBytes;

			cipherBytes = convertToBinary(cipherText);

			byte[] plainBytes = null;
			plainBytes = decrypt(cipherBytes, ksPath, ksPassword,
					certificateAlias, certificatePassword);

			returnString = new String(plainBytes, UTF8_ENCODING);
			if (returnString.equalsIgnoreCase(EMPTY_STRING)) {
				returnString = ""; // empty string
			}
		} else {
			returnString = null; // return as is
		}
		return returnString;
	}

	/**
	 * decrypt: Obtain plain ascii text for encrypted ciphertext specified. This
	 * method uses the same password for the keystore file and for accessing the
	 * private key. Null strings are not processed and will be returned as
	 * received. Empty strings will be encoded/encrypted.
	 * 
	 * @param cipherText
	 *            String representing value to be decrypted
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return String representing the decrypted format of the received string.
	 *         Null is returned when a null is received.
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	public static String decrypt(String cipherText, String ksPath,
			String ksPassword, String certificateAlias)
			throws java.lang.Exception {
		return decrypt(cipherText, ksPath, ksPassword, certificateAlias,
				ksPassword);
	}

	/**
	 * encrypt: Obtain encrypted (and ascii-encoded) value for plaintext
	 * specified
	 * 
	 * @param plainText
	 *            byte[] representing value to be encrypted
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return Key
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	private static byte[] encrypt(byte[] plainText, String ksPath,
			String ksPassword, String certificateAlias)
			throws java.lang.Exception {

		PublicKey pubKey = getPublicKey(ksPath, ksPassword, certificateAlias);

		synchronized (IDIPasswordCrypto.class) {
			getCipher();
			try {
				cipher.init(Cipher.ENCRYPT_MODE, pubKey);
				return cipher.doFinal(plainText);
			} catch (Exception ex) {
				cipher = null; // Work around a bug, no way to clear error condition.
				throw new Exception(resHash.getString("PWCRYPTO.ENCRYPT.FAILED", ex), ex);
			}
		}
	}

	/**
	 * decrypt: Obtain plain ascii text for encrypted ciphertext specified
	 * 
	 * @param cipherText
	 *            byte[] representing value to be decrypted
	 * @param ksPath
	 *            String representing file path to jks file
	 * @param ksPassword
	 *            String representing password for jks file as specified by path
	 * @param certificateAlias
	 *            String naming the alias of certificate in keystore file
	 * @return Key
	 * @throws Exception
	 *             when underlying funtion fails
	 */
	private static byte[] decrypt(byte[] cipherText, String ksPath,
			String ksPassword, String certificateAlias,
			String certificatePassword) throws java.lang.Exception {

		Key prvKey = getPrivateKey(ksPath, ksPassword, certificateAlias, certificatePassword);

		synchronized (IDIPasswordCrypto.class) {
			getCipher();
			try {
				cipher.init(Cipher.DECRYPT_MODE, prvKey);
				return cipher.doFinal(cipherText);
			} catch (Exception ex) {
				cipher = null; // Work around a bug, no way to clear error condition.
				throw new Exception(resHash.getString("PWCRYPTO.DECRYPT.FAILED", ex), ex);
			}
		}
	}

	/**
	 * convertToASCI: converts a binary string to asci. Every byte will be
	 * replaced by two asci characters. (see convertToBinary)
	 * 
	 * @param binary
	 *            a String representing the value to be converted to ascii
	 * @return java.lang.String encoded string which is twice the length of
	 *         input byte array
	 */
	private static String convertToASCI(byte[] binary) {
		// First, get the String into bytes.
		// byte[] buffer = binary.getBytes();
		byte[] buffer = binary;
		int readBytes = buffer.length;

		// Create a new StringBuffer.
		StringBuffer hexData = new StringBuffer();

		for (int i = 0; i < readBytes; i++) {
			// Get the Hex value.
			String num = Integer.toHexString((0xff & buffer[i]));

			// Make sure it is two character encoded.
			if (num.length() < 2) {
				num = "0" + num;
			}

			hexData.append(num);
		}

		// return the new String.
		return hexData.toString();
	}

	/**
	 * convertToBinary: converts a asci string to binary. Assumes every byte is
	 * really a converted binary String. Returned value is one-half length of
	 * input string (See convertToASCI).
	 * 
	 * @param asci
	 *            a String representing the value to be converted to binary
	 * @return byte[] binary representation of ascii string.
	 */
	private static byte[] convertToBinary(String asci) {
		// Get the String length.
		int len = asci.length() / 2;
		int digit = 0;

		// Use a byte array.
		byte[] hexData = new byte[len];

		// Convert the to character to bytes.
		int asciInx = 0;
		for (int i = 0; i < len; i++) {
			// Convert the item back to its binary value
			// and append to the string buffer.
			asciInx = 2 * i;
			digit = Integer.parseInt(asci.substring(asciInx, asciInx + 2), 16);
			hexData[i] = (byte) digit;
			/*
			 * System.out.println( "toBinary: " + "\tASCII["+i+"]:" +
			 * "\tparseInt Value: " + hexData[i]);
			 */
		}

		return hexData;
	}

	/*
	 * check if string value is null @param testString String to be tested
	 * @return boolean false if input is null
	 * 
	 */
	private static boolean notNull(String testString) {

		return (testString != null);

	}

	/*
	 * check if string value is not null and is 0-length @param testString
	 * String to be tested @return boolean true if input 0 length
	 * 
	 */
	private static boolean emptyString(String testString) {
		if ((testString != null) && (testString.length() == 0)) {
			return true; // parm failure
		} else {
			return false;
		}

	}

}
