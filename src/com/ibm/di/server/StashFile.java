/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.security.SecurityCrypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.security.Provider;
import java.security.Security;

/**
 * This class implements a command line utility for creating and reading stash
 * files. The stash file stores two passwords in encrypted form. When the file
 * is being written the password are being enrypted and when the file is being
 * read the password are being decrypted by this utility. These two passwords
 * are passed as command line parameters to this utility. Utility usage:
 * 
 * StashFile <keyStorePassword> [<keyPassword> [<securityProviderClass>]]
 * 
 * Only the <keyStorePassword> password is required. The <keyPassword> and
 * <securityProviderClass> are optional. If only a single password is specified
 * at the command line, then only this single password is written to the stash
 * file. If two passwords and a security provider are passed then the provider
 * will be used for the cryptography (both passwords may be equal).
 */
public class StashFile {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The default stash file name.
	 */
	public static final String STASH_FILE_NAME = "idisrv.sth";

	private static final String CHAR_ENCODING = "ISO8859_1";

	private static final String ALG = "AES";

	private static final String ALG_KEY = "TDISecurityKey";

	private static final String PROPERTIES_FILE = "miserver";

	private static boolean mStashFileRead = false;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Creates a stash file with no key password.
	 * 
	 * @param aKeyStorePassword
	 *            the key store password
	 */
	public static void createStashFile(String aKeyStorePassword)
			throws Exception {
		createStashFile(aKeyStorePassword, null, null);
	}

	/**
	 * Creates a stash file with default name and writes the password parameters
	 * in it.
	 * 
	 * @param aKeyStorePassword
	 *            the key store password
	 * @param aKeyPassword
	 *            the key password
	 * @return the absolute file name of the stash file
	 * @throws Exception
	 *             if the key store password parameter is null or an empty
	 *             string.
	 */
	public static String createStashFile(String aKeyStorePassword,
			String aKeyPassword) throws Exception {
		return createStashFile(aKeyStorePassword, aKeyPassword, null);
	}

	public static String createStashFile(String aKeyStorePassword,
			String aKeyPassword, Provider provider) throws Exception {
		if (aKeyStorePassword == null || aKeyStorePassword.length() == 0) {
			String errorMessage = sResHash
					.getString("stash.file.null.or.empty.store.pass");
			throw new Exception(errorMessage);
		}

		File stashFile = new File(STASH_FILE_NAME);
		FileOutputStream outputStream = new FileOutputStream(stashFile);
		try {
			byte[] encValue = encrypt(aKeyStorePassword, provider);
			writeArray(outputStream, encValue);
			if (aKeyPassword != null && aKeyPassword.length() > 0) {
				encValue = encrypt(aKeyPassword, provider);
				writeArray(outputStream, encValue);
			}
		} finally {
			outputStream.close();
		}

		return stashFile.getAbsolutePath();
	}

	/**
	 * Reads the passwords from the default stash file. This method is used by
	 * the TDI Server (com.ibm.di.server.RS) to read the passwords it needs.
	 * 
	 * @return a vector containing the passwords
	 * @throws Exception
	 *             if the stash file has already been read
	 */
	public static Vector<String> readPasswords() throws Exception {
		return readPasswords(STASH_FILE_NAME);
	}
	
	/**
	 * Reads the passwords from a specified stash file.
	 * 
	 * @param aStashFile
	 *            the path of the stash file
	 * @return a vector containing the passwords
	 * @throws Exception
	 *             if the stash file has already been read
	 */
	public static Vector<String> readPasswords(String aStashFile) throws Exception {
		if (mStashFileRead) {
			String errorMessage = sResHash.getString("stash.file.already.read");
			throw new Exception(errorMessage);
		}
		Vector<String> passwords = readPasswordsFromFile(aStashFile);
		mStashFileRead = true;
		return passwords;
	}

	/**
	 * Reads the passwords from a specified stash file. This method will not
	 * complain that it has been called more than once.
	 * 
	 * <b>Note:</b> This method is for internal usage only. Any dependency from
	 * the end-user will not be supported. Changes to this class will happen
	 * without a warning.
	 * 
	 * @param aStashFile
	 *            the path of the stash file
	 * @return a vector containing the passwords
	 * @throws Exception
	 *             if the stash file has already been read
	 */
	public static Vector<String> readPasswordsFromFile(String aStashFile) throws Exception {

		// check if the stash file exists
		File stashFile = new File(aStashFile);
		if (!stashFile.exists()) {
			String errorMessage = sResHash.getString(
					"stash.file.not.found.sol.dir", aStashFile);
			throw new Exception(errorMessage);
		}

		// read data
		FileInputStream stashReader = new FileInputStream(stashFile);
		byte[] keyStorePassword = readArray(stashReader);
		byte[] keyPassword = readArray(stashReader);

		// decrypt and return data
		SecurityCrypto crypto = new SecurityCrypto(ALG_KEY, ALG);

		Vector<String> passwords = new Vector<String>();
		passwords.add(new String(crypto.decrypt(keyStorePassword), CHAR_ENCODING).trim());
		if (keyPassword != null) {
			passwords.add(new String(crypto.decrypt(keyPassword), CHAR_ENCODING).trim());
		}

		return passwords;
	}

	/**
	 * Reads byte array from an input stream.
	 * 
	 * @param aInputStream
	 *            the input stream the byte array is read from
	 * @return byte array read from the input stream
	 * @throws Exception
	 *             if the input stream parameter is null or if the size of the
	 *             data read does not matched the length read before the data
	 */
	private static byte[] readArray(InputStream aInputStream) throws Exception {
		if (aInputStream == null) {
			String errorMessage = sResHash
					.getString("stash.file.instream.null");
			throw new Exception(errorMessage);
		}

		int length = aInputStream.read();
		if (length == -1) {
			return null;
		}

		byte[] data = new byte[length];
		int bytesRead = aInputStream.read(data);
		if (bytesRead != length) {
			String errorMessage = sResHash
					.getString("stash.file.not.enough.data");
			throw new Exception(errorMessage);
		}

		return data;
	}

	/**
	 * Writes an array of bytes to an output stream.
	 * 
	 * @param aOutputStream
	 *            the output stream the array of bytes to be written to
	 * @param aByteArray
	 *            the array of bytes to be written to the output stream
	 * @throws Exception
	 *             if one of the parameters is null or if the byte array is
	 *             longer than 255 bytes
	 */
	private static void writeArray(OutputStream aOutputStream, byte[] aByteArray)
			throws Exception {
		if (aOutputStream == null) {
			String errorMessage = sResHash
					.getString("stash.file.outstream.null");
			throw new Exception(errorMessage);
		}
		if (aByteArray == null) {
			String errorMessage = sResHash
					.getString("stash.file.byte.array.null");
			throw new Exception(errorMessage);
		}
		if (aByteArray.length > 255) {
			String errorMessage = sResHash.getString(
					"stash.file.encrypt.data.too.long", Integer
							.toString(aByteArray.length));
			throw new Exception(errorMessage);
		}

		aOutputStream.write(aByteArray.length);
		aOutputStream.write(aByteArray);
	}

	/**
	 * Encrypts a string, using a preset algorithm.
	 * 
	 * @param aPlainStr
	 *            the string to be encrypted
	 * @return the bytes of the encrypted string
	 * @throws Exception
	 *             if the selected encryption algorithm or the selected
	 *             character encoding are not correct or there is a problem
	 *             during the encrytion.
	 */
	private static byte[] encrypt(String aPlainStr, Provider provider)
			throws Exception {
		SecurityCrypto crypto = null;
		if (provider != null) {
			crypto = new SecurityCrypto(ALG_KEY, ALG, provider);
		} else {
			crypto = new SecurityCrypto(ALG_KEY, ALG);
		}
		byte[] plainData = aPlainStr.getBytes(CHAR_ENCODING);
		return crypto.encrypt(plainData);
	}

	/**
	 * The main method of the command line utility class. Takes as arguments one
	 * or two passwords. The first one is the key store password and the second
	 * is the key password.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) throws Exception {
		if ((args.length < 1) || (args.length > 3) || args[0].equals("-?") || args[0].equals("-h") || args[0].equals("--help")) {
			showUsage();
			System.exit(-1);
		}

		try {
			String keyStorePassword = args[0];
			String keyPassword = null;
			Provider provider = null;
			if (args.length > 1) {
				keyPassword = args[1];
				if (args.length > 2) {
					if (args[2] == null) {
						System.out.println(sResHash
								.getString("stash.file.no.provider"));
						System.exit(-1);
					}
					provider = (Provider) Class.forName(args[2]).newInstance();
					Security.addProvider(provider);
				}
			}

			String stashFilePath = createStashFile(keyStorePassword,
					keyPassword, provider);
			String infoMessage = sResHash.getString("stash.file.created",
					stashFilePath);
			System.out.println(infoMessage);
		} catch (Exception e) {
			String errorMessage = sResHash.getString(
					"could.not.create.stash.file", e.getMessage());
			System.out.println(errorMessage);
			System.out.println();
			e.printStackTrace();
		}
	}

	/**
	 * Shows the usage of the commnad line utility.
	 */
	private static void showUsage() {
		String usage = sResHash.getString("stash.file.usage");
		System.out.println(usage);
		System.out.println();
	}

}
