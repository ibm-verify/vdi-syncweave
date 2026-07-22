/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.di.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.security.Crypto;
import com.ibm.di.security.CryptoFactory;
import com.ibm.di.server.StashFile;

/**
 * In-memory representation of a Java properties file that can contain protected
 * properties with encrypted values. This class reads and writes valid Java
 * properties. When writing the order of the properties and the user comments
 * are kept. Only the changed properties are stored in the output file.
 * 
 * @see PropertiesFile
 * @since 7.1
 */
public class JavaPropertiesFile extends BasePropertiesFile {

	/**
	 * Properties of the read properties file.
	 */
	private Properties props = new Properties();

	/**
	 * Constructs Java properties file without any encryption/decryption of
	 * properties.
	 * 
	 * @param path
	 *            path to properties files
	 * @throws Exception
	 */
	public JavaPropertiesFile(String path) throws Exception {
		load(path);
	}

	/**
	 * Constructs Java properties file, obtain keystore and key passwords from
	 * the stash file and encrypt/decrypt protected properties.
	 * 
	 * @param path
	 *            path to properties files
	 * @param keyStoreProp
	 *            keystore password property name
	 * @param keyStoreTypeProp
	 *            keystore type property name
	 * @param keyStoreAliasProp
	 *            alias property name
	 * @param transformationProp
	 *            transformation property name
	 * @param stashFileProp
	 *            stash file property name
	 * @throws Exception
	 */
	public JavaPropertiesFile(String path, String keyStoreProp, String keyStoreTypeProp, String keyStoreAliasProp,
			String transformationProp, String stashFileProp) throws Exception {

		// Load the file without decrypting anything
		load(path);

		// Initialize Crypto using the specified property names to read the
		// correct properties
		propertyCrypto = getCrypto(keyStoreProp, keyStoreTypeProp, keyStoreAliasProp, transformationProp, stashFileProp);

		// Load again this time encrypt/decrypt if needed
		loadProperties(path);

		// Update props object
		updateProperties();
	}

	/**
	 * This method loads keystore and key passwords from the stash file
	 * specified in the am_config.properties file. The am_config.proeprties file
	 * needs to be loaded first before invoking this method.
	 * 
	 * @return {@link Crypto} object used for encryption/decryption of protected
	 *         properties in the <code>am_config.properties</code> file;
	 *         <code>null</code> if not able to create Crypto object
	 * @throws Exception
	 *             if stash file is already read or can not create Crypto
	 *             object.
	 * @see CryptoFactory#createCrypto(String, String, String, String, String,
	 *      String, java.security.Provider)
	 */
	private Crypto getCrypto(String keyStoreProp, String keyStoreTypeProp, String keyStoreAliasProp, String transformationProp,
			String stashFileProp) throws Exception {
		Vector<String> stashFilePasswords = StashFile.readPasswords(getProperty(stashFileProp));

		// At least one password found in stash file
		if (stashFilePasswords != null && stashFilePasswords.size() > 0) {

			String keyStorePassword = stashFilePasswords.get(0);
			String keyPassword = null;
			if (stashFilePasswords.size() > 1) {
				keyPassword = stashFilePasswords.get(1);
			} else {
				// No key password found.
				keyPassword = keyStorePassword;
			}

			return CryptoFactory.createCrypto(getProperty(keyStoreProp), keyStorePassword, getProperty(keyStoreTypeProp),
					getProperty(keyStoreAliasProp), keyPassword, getProperty(transformationProp), null);
		}
		return null;
	}

	/**
	 * Load a properties file. Values of all protected properties are encrypted
	 * only if the Crypto module is already initialized.
	 * 
	 * @param path
	 *            the path of the properties file
	 * @param decrypt
	 *            specify whether to decrypt/encrypt protected properties'
	 *            values
	 * @throws Exception
	 *             error while reading the properties file, or error while
	 *             decrypting it
	 */
	private void load(String path) throws Exception {

		loadProperties(path);

		if (lines.isEmpty()) {
			loadLines(path);
		}
	}

	/**
	 * Load properties from the file.
	 * 
	 * @param path
	 *            file path
	 * @throws Exception
	 */
	private void loadProperties(String path) throws Exception {
		if (props.isEmpty()) {
			InputStream inProps = new FileInputStream(path);
			try {
				props.load(inProps);
			} finally {
				inProps.close();
			}
		}

		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			Property prop = new Property(key, value, new StringBuilder(key + "=" + value), propertyCrypto);

			// get the 'clean' key without protected prefix
			key = prop.getKey();
			properties.put(key, prop);

			if (isPropertyProtected(key)) {
				setPropertyEncrypted(key, true);
			}
		}
	}

	/**
	 * Load lines from the file.
	 * <p>
	 * Note: Multi-line property are concatenated into single element separated
	 * by the default line separator for the platform.
	 * 
	 * @param path
	 *            file path
	 * @throws Exception
	 */
	private void loadLines(String path) throws Exception {

		InputStreamReader isr = new InputStreamReader(new FileInputStream(path));
		BufferedReader br = new BufferedReader(isr);

		// line read from the file
		String normalLine = null;

		// line containing a completed key value pair
		StringBuilder logicalLine = new StringBuilder();

		// Indicate if we are reading multi-line property
		// Note: Both key and value may be on several lines
		boolean isMultiLine = false;

		try {
			while ((normalLine = br.readLine()) != null) {

				// multi-line found
				if (isNumContBackSlashOdd(normalLine)) {
					isMultiLine = true;
					logicalLine.append(normalLine + System.getProperty("line.separator"));
				} else {
					if (isMultiLine) {
						logicalLine.append(normalLine);
						isMultiLine = false;
					} else {
						logicalLine = new StringBuilder(normalLine);
					}
					lines.add(logicalLine);
					logicalLine = new StringBuilder();
				}
			}
		} finally {
			isr.close();
			br.close();
		}
	}

	/**
	 * @param line
	 *            line to check
	 * @return <code>true</code> if line ends with odd number of back slashes;
	 *         <code>false</code> otherwise
	 */
	private boolean isNumContBackSlashOdd(String line) {
		int num = 0;
		StringBuilder str = new StringBuilder(line);
		int i;

		// Make sure "\" are continuous starting from the end
		while ((i = str.lastIndexOf("\\")) != -1 && (i + 1 == str.length())) {
			// Remove last seen "\" character
			str.deleteCharAt(i);
			num++;
		}
		return (num % 2) != 0;
	}

	/**
	 * Update internal Properties object to contain not prefixed keys and
	 * decrypted values. This object than can be obtained using
	 * {@link #asProperties()} method.
	 */
	private void updateProperties() throws Exception {
		Enumeration<Object> e = props.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.startsWith(PROTECT_PREFIX)) {
				props.remove(key);
				key = key.substring(PROTECT_PREFIX.length());
				props.setProperty(key, getProperty(key));
			}
		}
	}

	/**
	 * Write the contents of this properties file to disk. The order of the
	 * properties order and all user comments are kept. Only the modified
	 * properties are overwritten in the file.
	 * 
	 * @param path
	 *            a file, whether the contents will be saved
	 * @throws Exception
	 *             error while writing the file or error while encrypting the
	 *             file
	 */
	public void store(String path) throws Exception {
		PrintWriter pw = new PrintWriter(path);
		Properties lineProp = new Properties();
		StringReader sr = null;
		String line = null;
		try {
			for (int i = 0; i < lines.size(); i++) {
				line = lines.get(i).toString().trim();

				// Parse not commented lines as Java properties
				// to find out exactly where to write the modified
				// properties
				if (!line.startsWith("#") && !line.startsWith("!")) {
					sr = new StringReader(lines.get(i).toString());
					lineProp.load(sr);

					Enumeration<Object> keys = lineProp.keys();
					while (keys.hasMoreElements()) {
						String key = (String) keys.nextElement();

						// Strip protect prefix
						if (key.startsWith(PROTECT_PREFIX)) {
							key = key.substring(PROTECT_PREFIX.length());
						}

						Property p = properties.get(key);
						if (p != null && p.isModified()) {
							line = p.getLine().toString();
						}
					}
					lineProp.clear();
				}
				pw.println(line);
			}
		} finally {
			pw.close();
		}
	}

	/**
	 * @return Properties object representing the contents of this properties
	 *         file
	 * @throws Exception
	 *             if not able to decrypt some property value
	 */
	public Properties asProperties() throws Exception {
		return props;
	}
}
