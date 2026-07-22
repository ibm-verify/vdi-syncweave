/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class provides simple encode decode of strings
 * 
 * @author Jerry Borrelli
 */
public class SecurityHelper {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String CHAR_ENCODING = "ISO8859_1";

	private static final int IV_LENGTH = 16;
	private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static byte[] KEY = "HuP0&^Nlk)0-+aq=^GtIUyh&90$3':h6".getBytes();

	/**
	 * Method converts a binary string to asci. Every byte will be replaced by
	 * two asci characters.
	 * 
	 * @param binary
	 *            A String representing the value to be converted to ASCI.
	 * 
	 * @return java.lang.String ecoded string
	 */
	public static String convertToASCI(String binary) {
		// First, get the String into bytes.
		byte[] buffer = null;
		try {
			buffer = binary.getBytes(CHAR_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		int readBytes = buffer.length;

		// Create a new StringBuffer.
		StringBuffer hexData = new StringBuffer();

		for (int i = 0; i < readBytes; i++) {
			// Get the Hex value.
			String num = Integer.toHexString(0xff & buffer[i]);

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
	 * Method converts a asci string to binary. Assumes every byte is really a
	 * converted binary String. See convertToASCI.
	 * 
	 * @param asci
	 *            A String representing the value to be converted to binary.
	 * 
	 * @return java.lang.String ecoded string
	 */
	public static String convertToBinary(String asci) {
		// Get the String length.
		int len = asci.length() / 2;
		int digit = 0;

		// Use a byte array.
		byte[] hexData = new byte[len];

		// Convert the to character to bytes.
		for (int i = 0; i < len; i++) {
			// Convert the item back to its binary value
			// and append to the string buffer.
			digit = Integer.parseInt(asci.substring(2 * i, (2 * i) + 2), 16);
			hexData[i] = (byte) digit;
		}

		try {
			return new String(hexData, CHAR_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Decode input string using a MessageDigest.
	 * 
	 * @param stringIn
	 *            A String representing an encoded value
	 * 
	 * @return java.lang.String decoded string
	 */
	public static String decode(String stringIn) {
		String decodedStr = null;

		try {
			byte stringInBytes[] = stringIn.getBytes(CHAR_ENCODING);

			if (stringInBytes.length > IV_LENGTH) {
				/*
				 * Break the obfuscated data into the IV and CipherText 
				 * components.
				 */

				byte[] iv = new byte[IV_LENGTH];

				System.arraycopy(stringInBytes, 0, iv, 0, IV_LENGTH);
                    
				byte[] cipherText = new byte[stringInBytes.length - IV_LENGTH];

				System.arraycopy(stringInBytes, IV_LENGTH, cipherText, 0, stringInBytes.length - IV_LENGTH);

				/*
				 * Create the IV, Cipher and key specification.
				 */

				IvParameterSpec ivSpec = new IvParameterSpec(iv);
                    
				Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

				SecretKeySpec kspec = new SecretKeySpec(KEY, "AES");

				/*
				 * Initialise the cipher and decrypt the text.
				 */

				cipher.init(Cipher.DECRYPT_MODE, kspec, ivSpec);                

				decodedStr = new String(cipher.doFinal(cipherText));
			}
		}
		catch(Exception e) {
			decodedStr = null;
		}

		/*
		 * If we failed to decode the string we want to fallback to the
		 * legacy method of decoding the string.
		 */

		if (decodedStr == null) {
			try {
				MessageDigest localMessageDigest = null;
				byte[] localIdBytes = null;
				byte stringInBytes[] = stringIn.getBytes(CHAR_ENCODING);

				localMessageDigest = MessageDigest.getInstance("MD5");
				localIdBytes = localMessageDigest.digest(("LDAPTIMSecurityKey")
						.getBytes(CHAR_ENCODING));
				if (localIdBytes != null) {
					byte[] dencrypted = new byte[stringInBytes.length];
					int len = localIdBytes.length;
					for (int i = 0; i < stringInBytes.length; i++) {
						dencrypted[i] = (byte) (localIdBytes[i % len] ^ stringInBytes[i]); // xor
					}

					decodedStr = new String(dencrypted, CHAR_ENCODING);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return decodedStr;
	}

	/**
	 * Encode input string using a MessageDigest.
	 * 
	 * @param stringIn
	 *            A String representing the value to be encoded.
	 * 
	 * @return java.lang.String ecoded string
	 */
	public static String encode(String stringIn) {
		String encodedStr = null;

		try {
			byte[] encrypted = null;

	   		Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
	 		SecretKeySpec kspec = new SecretKeySpec(KEY, "AES");

			cipher.init(Cipher.ENCRYPT_MODE, kspec);

	   		byte[] passwdBytes = stringIn.getBytes(CHAR_ENCODING);

			byte[] enc = cipher.doFinal(passwdBytes);
			byte[] iv  = cipher.getIV();

			if (enc != null && iv != null)
			{
				encrypted = new byte[enc.length + iv.length];

				System.arraycopy(iv, 0, encrypted, 0, iv.length);
				System.arraycopy(enc, 0, encrypted, iv.length, enc.length);
			}

			encodedStr = new String(encrypted, CHAR_ENCODING);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return encodedStr;
	}

	/**
	 * Decode input string using a MessageDigest
	 * 
	 * @param stringIn
	 *            a String representing an encoded value.
	 * @return java.lang.String decoded string or null if the stringIn parameter
	 *         is null.
	 */
	public static String getClearText(String stringIn) {
		if (stringIn == null) {
			return null;
		}

		String binaryData = null;
		try {
			binaryData = convertToBinary(stringIn);
		} catch (NumberFormatException x) {
			binaryData = stringIn;
		}
		String clearText = decode(binaryData);
		return clearText;
	}
}
