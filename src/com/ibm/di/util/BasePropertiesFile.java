/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.di.function.UserFunctions;
import com.ibm.di.security.Crypto;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * In-memory representation of a properties file. It may contain comments and
 * empty lines as well as property definitions. When writing the order of the
 * properties and the user comments are kept.
 * <p>
 * A property in a properties file can be protected or non-protected. Protected
 * properties normally have their keys prefixed with
 * BasePropertiesFiles.PROTECT_PREFIX. Protected properties may have their
 * values encrypted. If a property value is encrypted, it will be prefixed with
 * BasePropertiesFiles.PROTECT_VAL_PREFIX. If a property's value is encrypted,
 * the property is considered protected no matter if it is actually marked as
 * protected.
 * 
 * @see PropertiesFile
 * @since 7.1
 */
public abstract class BasePropertiesFile {
	// Log object 
	private static Log logger;

	/**
	 * A prefix for the keys of protected properties. A protected property may
	 * or may not have an encrypted value. However, if a property's value is
	 * encrypted, the property must be protected.
	 */
	public static final String PROTECT_PREFIX = "{protect}-";

	/**
	 * A prefix for encrypted property values.
	 */
	public static final String PROTECT_VAL_PREFIX = "{encr}";

	/**
	 * All lines of the properties file - including comments, empty lines,
	 * include directives and property definitions. The contents of referenced
	 * property files are not contained in this list. Each line is a
	 * StringBuilder object, so that a line can be modified, without modifying
	 * the list.
	 */
	protected List<StringBuilder> lines = new ArrayList<StringBuilder>();

	/**
	 * <p>
	 * The property definitions of the properties file the mapping is: property
	 * key -> BasePropertiesFile.Property object. The key is not marked with a
	 * protect prefix.
	 * </p>
	 * <p>
	 * Only the last encountered definition of a property is kept. If for
	 * example a properties file contains two equal properties, then the
	 * definition from the last property will be kept.
	 * </p>
	 */
	protected Map<String, Property> properties = new TreeMap<String, Property>();

	/**
	 * Whether any of the lines in this properties file has been modified.
	 */
	protected boolean modified = false;

	/**
	 * Object to encrypt/decrypt the values of protected properties.
	 */
	protected Crypto propertyCrypto;

	protected static final ResourceHash resHash = ResourceHash.getHash("miserver");

	/**
	 * A property from a properties file. If provided the actual read line from
	 * the file is also saved and updated in case of change to property key or
	 * value.
	 */
	protected static class Property {

		/**
		 * The raw key of the property - potentially marked with a protect
		 * prefix.
		 */
		private String rawKey;

		/**
		 * The raw value of the property - potentially encrypted and marked with
		 * an encryption prefix.
		 */
		private String rawValue;

		/**
		 * The corresponding line of the property in the properties file.
		 */
		private StringBuilder line;

		/**
		 * A cryptographic object used to encrypt/decrypt values of protected
		 * properties.
		 */
		private Crypto crypto;

		/**
		 * Flag indicating whether the property has been modified.
		 */
		private boolean modified = false;

		/**
		 * Construct a property.
		 * 
		 * @param rawKey
		 *            the raw key of the property
		 * @param rawValue
		 *            the raw value of the property.
		 * @param line
		 *            the corresponding line of the property
		 * @param crypto
		 *            cryptographic object for encryption/decryption of the
		 *            property's value
		 */
		public Property(String rawKey, String rawValue, StringBuilder line, Crypto crypto) {
			this.rawKey = rawKey;
			this.rawValue = rawValue;
			this.line = line;
			this.crypto = crypto;
		}

		/**
		 * @return the key of the property with no marker protect prefix
		 */
		public String getKey() {
			String key = rawKey;

			if (isPropertyProtected(rawKey)) {
				key = key.substring(PROTECT_PREFIX.length());
			}
			return key;
		}

		/**
		 * @return the value of the property as plain text (decrypted if
		 *         necessary)
		 * @throws Exception
		 *             error during decryption
		 */
		public String getValue() throws Exception {
			return decryptPropertyValue(rawValue, crypto);
		}

		/**
		 * @return whether the property's value is encrypted; implies that the
		 *         property is protected
		 */
		public boolean isEncrypted() {
			return isPropertyValueEncrypted(rawValue);
		}

		/**
		 * @return whether the property is protected
		 */
		public boolean isProtected() {
			return isPropertyProtected(rawKey) || isEncrypted();
		}

		/**
		 * @return whether the property was modified
		 */
		public boolean isModified() {
			return modified;
		}

		/**
		 * Encrypt/decrypt the property's value.
		 * 
		 * @param encrypt
		 *            whether the property's value will be encrypted
		 * @throws Exception
		 *             encryption/decryption error
		 */
		public void setEncrypted(boolean encrypt) throws Exception {

			if (isEncrypted() == encrypt) {
				// no need to change anything
				return;
			}

			modified = true;
			if (encrypt) {

				// encrypted value implies that the property is protected
				setProtected(true);
				rawValue = encryptPropertyValue(rawValue, crypto);
			} else {
				rawValue = decryptPropertyValue(rawValue, crypto);
			}
			updatePropertyLine();
		}

		/**
		 * Mark the property as protected/non-protected. The operation will
		 * involve decryption if an encrypted property is transformed to
		 * non-protected.
		 * 
		 * @param protect
		 *            whether the property will be marked as protected
		 * @throws Exception
		 *             decryption error
		 */
		public void setProtected(boolean protect) throws Exception {
			if (isProtected() == protect) {
				// no need to change anything
				return;
			}

			modified = true;

			if (protect) {
				rawKey = PROTECT_PREFIX + rawKey;
			} else {

				// not protected implies that the property value is not
				// encrypted
				setEncrypted(false);
				rawKey = rawKey.substring(PROTECT_PREFIX.length());
			}

			updatePropertyLine();
		}

		/**
		 * @return whether the property originates from this properties file or
		 *         from some of the properties file that it references
		 */
		public boolean isOwnProperty() {
			/*
			 * Own properties have a corresponding line in the properties file,
			 * properties from included files do not.
			 */
			return line != null;
		}

		/**
		 * @return the corresponding line for this property in the main
		 *         properties file
		 */
		public StringBuilder getLine() {
			return line;
		}

		/**
		 * Associate a new line object to this property.
		 * 
		 * @param line
		 *            a line from the main properties file
		 */
		public void setLine(StringBuilder line) {

			this.line = line;
			updatePropertyLine();
			modified = true;
		}

		/**
		 * Update the line of the properties file that corresponds to the
		 * property.
		 */
		private void updatePropertyLine() {
			if (line != null) {
				line.setLength(0);
				line.append(rawKey + "=" + rawValue);
			}
		}

		/**
		 * Encrypt a property value. The returned value is marked with an
		 * encryption prefix. If the value is already encrypted or the
		 * cryptographic object is null, the value is returned as it is.
		 * 
		 * @param value
		 *            a property value
		 * @param crypto
		 *            a cryptographic object, used for encryption
		 * @return the encrypted value
		 * @throws Exception
		 *             an encryption error
		 */
		private static String encryptPropertyValue(String value, Crypto crypto) throws Exception {
			if (isPropertyValueEncrypted(value)) {
				// The value is already encrypted.
				return value;
			}

			if (crypto == null) {
				return value;
			}

			if (value == null) {
				return value;
			}

			byte[] bytes = value.getBytes();
			byte[] encryptedBytes = crypto.encrypt(bytes);
			String encryptedPayload = UserFunctions.base64Encode(encryptedBytes);

			return PROTECT_VAL_PREFIX + encryptedPayload;
		}

		/**
		 * Decrypt a property value. If the value is not encrypted or the
		 * cryptographic object is null, the value is returned as it is.
		 * 
		 * @param value
		 *            a property value
		 * @param crypto
		 *            a cryptographic object, used for decryption
		 * @return the decrypted value
		 * @throws Exception
		 *             a decryption error
		 */
		private static String decryptPropertyValue(String value, Crypto crypto) throws Exception {
			if (!isPropertyValueEncrypted(value)) {
				return value;
			}

			if (crypto == null) {
				return value;
			}

			if (value == null) {
				return value;
			}

			String encryptedPayload = value.substring(PROTECT_VAL_PREFIX.length());
			byte[] decryptedBytes;
			try {				
				byte[] encryptedBytes = UserFunctions.base64Decode(encryptedPayload);
				decryptedBytes = crypto.decrypt(encryptedBytes);
				return new String(decryptedBytes);
			} catch (Exception e) {
				if(logger!=null){
					logger.debug("ERROR.DECRYPT.PROTECTED.PROPERTY",e.getMessage());
				}
				return encryptedPayload;
			}			
		}

		/**
		 * @param value
		 *            property value
		 * @return whether the property value is encrypted
		 */
		private static boolean isPropertyValueEncrypted(String value) {
			boolean result = false;

			if (value != null) {
				result = value.startsWith(PROTECT_VAL_PREFIX);
			}

			return result;
		}

		/**
		 * @param key
		 *            property key
		 * @return whether the property is protected
		 */
		private static boolean isPropertyProtected(String key) {
			boolean result = false;

			if (key != null) {
				result = key.startsWith(PROTECT_PREFIX);
			}

			return result;
		}

	}

	/**
	 * Create an empty object.
	 */
	public BasePropertiesFile() {
		super();
	}

	/**
	 * Create an empty object with crypto module.
	 * 
	 * @param propertyCrypto
	 *            object used to encrypt/decrypt values of protected properties
	 */
	public BasePropertiesFile(Crypto propertyCrypto) {
		this.propertyCrypto = propertyCrypto;
	}

	/**
	 * @return the keys of all properties; removing properties through this
	 *         iterator will result in an undefined behavior
	 */
	public Iterator<String> keys() {
		return properties.keySet().iterator();
	}

	/**
	 * Return the property value as plain text. The value will be decrypted if
	 * necessary. If the property is not found, the method returns null.
	 * 
	 * @param key
	 *            the property key
	 * @return the property value
	 * @throws Exception
	 *             decryption error
	 */
	public String getProperty(String key) throws Exception {
		Property p = properties.get(key);
		if (p != null) {
			return p.getValue();
		}

		return null;
	}

	/**
	 * Set a property. Either override an existing property or add a new one. If
	 * overriding a protected existing property, the property will stay
	 * protected. If overriding an encrypted existing property, the property
	 * will stay encrypted. If the specified value is encrypted, the property
	 * will be considered encrypted (and protected).
	 * 
	 * @param key
	 *            a property key; can be marked as protected
	 * @param value
	 *            a property value; can be encrypted
	 * @throws Exception
	 *             encryption error (if setting a non-encrypted value to an
	 *             encrypted property)
	 */
	public void setProperty(String key, String value) throws Exception {
		StringBuilder line = null;
		boolean keepProtected = false;
		boolean keepEncrypted = false;

		Property newp = new Property(key, value, null, propertyCrypto);

		Property p = properties.get(newp.getKey());
		if (p != null) {

			// will replace existing
			line = p.getLine();
			keepProtected = p.isProtected();
			keepEncrypted = p.isEncrypted();
		} else {

			// will add new
			line = new StringBuilder(key + "=" + value);
			lines.add(line);
		}

		// add or replace
		newp.setLine(line);
		properties.put(newp.getKey(), newp);

		if (keepProtected) {
			newp.setProtected(true);
		}

		if (keepEncrypted) {
			newp.setEncrypted(true);
		}

		modified = true;
	}

	/**
	 * Remove a property from this properties file.
	 * 
	 * @param key
	 *            property key
	 */
	public void removeProperty(String key) {
		Property p = properties.get(key);
		if (p != null) {
			lines.remove(p.getLine());
			properties.remove(p.getKey());
			modified = true;
		}
	}

	/**
	 * @param key
	 *            a property key
	 * @return whether the property is marked as protected
	 */
	public boolean isPropertyProtected(String key) {
		Property p = properties.get(key);
		if (p != null) {
			return p.isProtected();
		}

		return false;
	}

	/**
	 * @param key
	 *            a property key
	 * @return whether the property value is encrypted
	 */
	public boolean isPropertyEncrypted(String key) {
		Property p = properties.get(key);
		if (p != null) {
			return p.isEncrypted();
		}

		return false;
	}

	/**
	 * Change the protected status of a property. Not protected implies that the
	 * property is not encrypted, so the property value will be decrypted if the
	 * property is transformed from protected to not protected.
	 * 
	 * @param key
	 *            a property key
	 * @param protect
	 *            whether the property will be protected
	 * @throws Exception
	 *             decryption error
	 */
	public void setPropertyProtected(String key, boolean protect) throws Exception {
		Property p = properties.get(key);
		if (p != null) {

			boolean oldProtect = p.isProtected();
			p.setProtected(protect);

			// count as modifications only actual changes to own properties
			if (oldProtect != protect && p.isOwnProperty()) {
				modified = true;
			}
		}
	}

	/**
	 * Change the encrypted status of a property's value. Encrypted implies that
	 * the property is protected, so the property will automatically become
	 * protected if it is encrypted.
	 * 
	 * @param key
	 *            a property key
	 * @param encrypt
	 *            whether the property value will be encrypted
	 * @throws Exception
	 *             encryption/decryption error
	 */
	public void setPropertyEncrypted(String key, boolean encrypt) throws Exception {
		Property p = properties.get(key);
		if (p != null) {
			boolean oldEncrypt = p.isEncrypted();
			p.setEncrypted(encrypt);

			// count as modifications only actual changes to own properties
			if (oldEncrypt != encrypt && p.isOwnProperty()) {
				modified = true;
			}
		}
	}

	/**
	 * Determine whether the contents of this file has been modified, e.g. the
	 * status of a property has been changed.
	 * 
	 * @return whether the in-memory contents of the properties file are
	 *         modified
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Write the contents of this properties file to disk.
	 * 
	 * @param path
	 *            a file, whether the contents will be saved
	 * @throws Exception
	 *             error while writing the file
	 */
	public abstract void store(String path) throws Exception;
	
	/**
	 * Return the property value as plain text. The value will be decrypted if
	 * necessary. If the property is not found, the method returns null.
	 * 
	 * @param key
	 * @param log
	 * @return the property value
	 * @throws Exception
	 */
	public String getProperty(String key, Log log)throws Exception {
		setLogger(log);
		try {
			return getProperty(key);
		} finally {
			setLogger(null);		
		}
	}
	
	private static void setLogger(Log log) {
		logger = log;
	}
}
