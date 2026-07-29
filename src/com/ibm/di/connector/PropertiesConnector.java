/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.security.Crypto;
import com.ibm.di.security.EncryptedReader;
import com.ibm.di.security.EncryptedWriter;
import com.ibm.di.security.SecurityCrypto;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.util.FileUtils;
import com.ibm.di.util.PropertiesFile;

/**
 * Properties Connector operates on a file or URL.
 */
public class PropertiesConnector extends Connector implements
		ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "propertiesconnector";

	/**
	 * Component name.
	 */
	private final static String myName = "IBM Properties Connector";

	/**
	 * Connector parameter name: {@value #PARAM_COLLECTION}
	 */
	public final static String PARAM_COLLECTION = "collection";

	/**
	 * Connector parameter name: {@value #PARAM_COLLECTION_TYPE}
	 */
	public final static String PARAM_COLLECTION_TYPE = "collectionType";

	/**
	 * Connector parameter name: {@value #PARAM_ENCRYPTION}
	 */
	public final static String PARAM_ENCRYPTION = "encryption";

	/**
	 * Connector parameter name: {@value #PARAM_CIPHER}
	 */
	public final static String PARAM_CIPHER = "cipher";

	/**
	 * Connector parameter name: {@value #PARAM_PASSWORD}
	 */
	public final static String PARAM_PASSWORD = "secret";

	/**
	 * Connector parameter name: {@value #PARAM_AUTOREWRITE}
	 */
	public final static String PARAM_AUTOREWRITE = "autorewrite";

	/**
	 * Connector parameter name: {@value #PARAM_CREATE_FILE}
	 */
	public final static String PARAM_CREATE_FILE = "createCollection";

	/**
	 * Certificate parameter name
	 */
	private final static String PARAM_KEY_ALIAS = "keyAlias";

	/**
	 * Prefix used by encrypted property values
	 */
	public final static String PROTECT_PREFIX = "{protect}-";

	/**
	 * Prefix used in property value to indicate encrypted data
	 */
	public final static String PROTECT_VAL_PREFIX = "{encr}";

	/**
	 * Properties name
	 */
	private final static String PROPS_HEADER = "##{PropertiesConnector} ";

	/**
	 * Server
	 */
	private final static String SERVER = "SERVER";

	/**
	 * Supported connector modes
	 */
	public final static String[] SUPPORTED_MODES = {
			ConnectorConfig.ADDONLY_MODE, ConnectorConfig.ITERATOR_MODE,
			ConnectorConfig.UPDATE_MODE, ConnectorConfig.LOOKUP_MODE,
			ConnectorConfig.DELETE_MODE, };

	/**
	 * In-memory data
	 */
	protected Map<String, Object> map;

	/**
	 * A flag that marks a modified collection.
	 */
	protected boolean modified;

	/**
	 * A flag that marks a collection of Java Properties.
	 */
	protected boolean isJavaProperties;

	/**
	 * The {@link PropertyStore} object.
	 */
	protected PropertyStore systemStore;

	/**
	 * The {@link PropertiesFile} object.
	 */
	protected PropertiesFile propsFile;

	/**
	 *
	 * The {@link Crypto} object used for decryption of the entire properties file.
	 *
	 */
	protected Crypto propsFileCrypto;

	/**
	 * Iterator over the in-memory/sysStore/File properties' keys.
	 */
	protected Iterator<String> mapIterator;

	/**
	 * Iterator over the Java Properties' keys.
	 */
	protected Iterator<Object> jpIterator;

	/**
	 * Helper object.
	 */
	protected UserFunctions uf = new UserFunctions();
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Set to true if we should ignore errors while reading a file
	 */
	private boolean ignoreReadErrors = false;

	private boolean isInitialized;
	
	/**
	 * Constructor
	 */
	public PropertiesConnector() {
		super();
		Trace.entrymid(this, "PropertiesConnector");
		setName(myName);
		setModes(SUPPORTED_MODES);
		Trace.exitmid(this, "PropertiesConnector");
	}

	/**
	 * Initialize the connector. For file/url collections the contents is loaded
	 * at this point
	 *
	 * @param o
	 *            This parameter is ignored by this connector.
	 * @exception Exception
	 *                Any exception thrown by java.io/java.net classes when
	 *                loading a file/url.
	 */
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);
		isInitialized = true;

		String ct = getParam(PARAM_COLLECTION_TYPE);
		if (ct == null || ct.equals(""))
			ct = PropertyManager.STDCOLL_JAVA;

		modified = false;

		ignoreReadErrors = (o instanceof Boolean) ? (Boolean) o : false;

		map = new java.util.Hashtable<String, Object>();

		if (ct.equals(PropertyManager.STDCOLL_JAVA)) {
			isJavaProperties = true;
			PropertyConfig pc = (PropertyConfig) ((ConnectorConfig) getConfiguration())
					.getMetamergeConfig().lookup(
							MetamergeConfig.DEFAULT_PROPERTY_FOLDER);
			for (String key: pc.getKeys(BaseConfiguration.ONE_LEVEL))
				map.put(key, pc.getParameter(key));

		} else if (ct.equals(PropertyManager.STDCOLL_SYSTEM)) {
			systemStore = StoreFactory.getDefaultPropertyStore();

		} else if (ct.equals(PropertyManager.STDCOLL_GLOBAL)
				|| ct.equals(PropertyManager.STDCOLL_SOLUTION)) {
			String installdir = System.getProperty("com.ibm.di.installdir");
			File path;

			if (ct.equals(PropertyManager.STDCOLL_GLOBAL)) {
				path = new File(installdir + File.separator + "etc"
						+ File.separator + "global.properties");
			} else {
				path = new File("solution.properties");
			}

			if (path.exists()) {
				loadProperties(path.getAbsolutePath());
			} else {
				logmsg(sResHash.getString("CONNECTOR.PROPERTIES.NOPATH.INFO",
						path.getAbsolutePath()));
			}

		} else {
			loadProperties(getParam(PARAM_COLLECTION));

			if (modified && getBoolean(PARAM_AUTOREWRITE) != null
					&& getBoolean(PARAM_AUTOREWRITE).booleanValue()) {
				saveProperties(getParam(PARAM_COLLECTION));
				modified = false;
			}

		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		if (modified)
			saveProperties(getParam(PARAM_COLLECTION));
	}

	/**
	 * Helper class used for encryption/decryption of properties.
	 */
	private static class SigningPasswordCrypto implements Crypto {

		/**
		 * password used in the encryption.
		 */
		private String password;

		/**
		 * specifies the type of encryption to use.
		 */
		private String cipher;

		/**
		 * Constructor of the helper class
		 *
		 * @param password
		 *            the password used in the encryption.
		 * @param cipher
		 *            specifies the type of encryption to use.
		 */
		public SigningPasswordCrypto(String password, String cipher) {
			this.password = password;
			this.cipher = cipher;
		}

		/**
		 * @param encryptedData
		 *            the data to decrypt as byte array
		 *
		 * @return the decrypted data
		 * @throws Exception
		 *             if an error occurs.
		 */
		public byte[] decrypt(byte[] encryptedData) throws Exception {

			ByteArrayInputStream input = new ByteArrayInputStream(encryptedData);
			EncryptedReader ec = new EncryptedReader(input);
			ec.setKey(new SecurityCrypto(password, cipher));

			return FileUtils.readInputStream(ec.getInputStream());
		}

		/**
		 * @param data
		 *            the data to encrypt as byte array
		 *
		 * @return the encrypted data
		 * @throws Exception
		 *             if an error occurs.
		 */
		public byte[] encrypt(byte[] data) throws Exception {

			ByteArrayOutputStream result = new ByteArrayOutputStream();

			EncryptedWriter ew = new EncryptedWriter(result);
			ew.setKey(new SecurityCrypto(password, cipher));
			ew.getOutputStream().write(data);
			ew.close();

			return result.toByteArray();
		}

	}

	/**
	 * Loads the data from path into a buffer where decryption is applied before
	 * parsing the properties into the map object.
	 *
	 * @param path
	 *            The filename or URL from which to read props
	 *
	 * @exception Exception
	 *                if an error occurs while loading properties
	 */
	protected void loadProperties(String path) throws Exception {

		// Crypto to use for encrypting/decrypting individual properties
		Crypto propertyCrypto = CryptoUtils.getDefaultCrypto();
		String cipher = getParam(PARAM_CIPHER);
		String keyAlias = getParam(PARAM_KEY_ALIAS);
		if (keyAlias != null && keyAlias.length() > 0) {
			if (SERVER.equalsIgnoreCase(cipher))
				cipher = null;
			try {
				propertyCrypto = CryptoUtils.getCrypto(keyAlias, cipher);
			} catch (Exception e) {
				String msg = sResHash.getString("cannot.create.crypto",
							new Object[]{keyAlias, path});
				if (myLog != null)
					myLog.logerror(msg, e);
				else
					TDIProperties.logger.logerror(msg, e);
				propertyCrypto = null; // Use null to mark that we cannot decrypt properties
			}
		}

		boolean encrypted = false;
		if (getBoolean(PARAM_ENCRYPTION) != null) {
			encrypted = getBoolean(PARAM_ENCRYPTION).booleanValue();
		}

		File file = new File(path);
		if (file.isFile() && !encrypted )
			encrypted = EncryptedReader.isEncrypted(file);

		propsFileCrypto = getFileCrypto(encrypted, path);

		try {
			propsFile = new PropertiesFile(propertyCrypto,
					path, true, propsFileCrypto, PROPS_HEADER);
		} catch (FileNotFoundException fnf) {
			if (getBoolean(PARAM_CREATE_FILE).booleanValue()) {

				propsFile = new PropertiesFile(propertyCrypto);

				//log a warning
				ConnectorConfig bc = (ConnectorConfig) getConfiguration();
				String name = (bc instanceof BaseConfigurationImpl
						? ((BaseConfigurationImpl) bc).getLongName()
						: bc.getShortName());
				String msg = sResHash.getString("CONNECTOR.FNF", 
						new Object[] { path, name });
				if (myLog != null) {
					myLog.logwarn(msg);
				} else {
					TDIProperties.logger.logwarn(msg);
				}
				
				return;
			} else {
				throw fnf;
			}
		} catch (Exception err) {
			if (ignoreReadErrors) {
				propsFile = new PropertiesFile(propertyCrypto);
				return;
			}
			throw err;
		}

		// Ensure all protected properties are encrypted
		Iterator<String> it = propsFile.keys();
		while (it.hasNext()) {

			String key = it.next();
			if (propsFile.isPropertyProtected(key)
					&& !propsFile.isPropertyEncrypted(key)) {

				propsFile.setPropertyEncrypted(key, true);
				modified = true;
			}
		}

	}

	private Crypto getFileCrypto(boolean encrypted, String path) throws Exception {
		if (!encrypted)
			return null;

		String cipher = getParam(PARAM_CIPHER);
		String keyAlias = getParam(PARAM_KEY_ALIAS);
		if (keyAlias != null && keyAlias.length() > 0) {
			if (SERVER.equalsIgnoreCase(cipher))
				cipher = null;
			try {
				return CryptoUtils.getCrypto(keyAlias, cipher);
			} catch (Exception e) {
				String msg = sResHash.getString("cannot.create.crypto", new Object[]{keyAlias, path});
				throw new Exception(msg, e);
			}
		}

		if (SERVER.equalsIgnoreCase(cipher))
			return CryptoUtils.getDefaultCrypto();

		String pwd = getParam(PARAM_PASSWORD);
		if (pwd == null || pwd.length() == 0) {
			throw new Exception(sResHash.getString("no.password.specified", path));
		}

		return new SigningPasswordCrypto(pwd, cipher);
	}

	/**
	 * Sets the provided property in the
	 * JavaPropertiesMap/SystemStore/PropertyFile.
	 *
	 * @param key
	 *            the key name to use.
	 * @param value
	 *            the value to set.
	 * @param encr
	 *            specify whether the value should be encrypted.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void setProperty(String key, Object value, boolean encr)
			throws Exception {
		setProperty(key, value, Boolean.valueOf(encr));
	}

	/**
	 * Sets the provided property in the
	 * JavaPropertiesMap/SystemStore/PropertyFile.
	 *
	 * @param key
	 *            the key name to use.
	 * @param value
	 *            the value to set.
	 * @param encr
	 *            If not null, specify whether the value should be encrypted.
	 *            If null, keep old encryption status if possible.
	 * @throws Exception
	 *             if an error occurs.
	 * @since 7.0
	 */
	public void setProperty(String key, Object value, Boolean encr)
	throws Exception {
		if (!isInitialized)
			initialize(null);

		String nstr = value == null ? "" : value.toString();

		if (encr != null && encr && !nstr.startsWith(PROTECT_VAL_PREFIX)) {
			byte[] cryptVal = CryptoUtils.encryptWithServerKey(nstr
					.getBytes("UTF-8"));
			nstr = PROTECT_VAL_PREFIX + UserFunctions.base64Encode(cryptVal);
		}

		if (isJavaProperties) {

			map.put(key, nstr);

			// Update System.setProperty as well
			if (value != null)
				System.setProperty(key, value.toString());
			else
				System.setProperty(key, "");
		}

		if (systemStore != null) {
			systemStore.setProperty(key, value == null ? null : nstr);
		}

		if (propsFile != null) {
			propsFile.setProperty(key, value == null ? "" : value.toString());
			if (encr != null) {
				propsFile.setPropertyEncrypted(key, encr);
				if (!encr)
					propsFile.setPropertyProtected(key, encr);
			}
		}

		modified = true;
	}

	/**
	 * Saves the properties in the appropriate store. If the store is configured
	 * to be the SystemStore then this call is ignored.
	 *
	 * @param path
	 *            this is the path to the file in which the properties will be
	 *            saved. This parameter is ignored if the connector is
	 *            configured to store properties in other than a File.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void saveProperties(String path) throws Exception {
		if (isJavaProperties) {
			PropertyConfig pc = (PropertyConfig) ((ConnectorConfig) getConfiguration())
					.getMetamergeConfig().lookup(
							MetamergeConfig.DEFAULT_PROPERTY_FOLDER);
			for (Map.Entry<String, Object> entry: map.entrySet()) {
				pc.setParameter(entry.getKey(), entry.getValue());
			}
		} else if (systemStore == null) {
			String ct = getParam(PARAM_COLLECTION_TYPE);
			File p;
			/* code commented by L3
			 * if (ct.equals(PropertyManager.STDCOLL_GLOBAL)
					|| ct.equals(PropertyManager.STDCOLL_SOLUTION)) {*/
			if (PropertyManager.STDCOLL_GLOBAL.equals(ct)
                    || PropertyManager.STDCOLL_SOLUTION.equals(ct)) {
				String installdir = System.getProperty("com.ibm.di.installdir");
				if (ct.equals(PropertyManager.STDCOLL_GLOBAL))
					p = new File(installdir + File.separator + "etc"
							+ File.separator + "global.properties");
				else
					p = new File("solution.properties");
			} else {
				p = new File(path);
			}

			StringBuilder header = new StringBuilder();
			header.append(PROPS_HEADER);
			header.append("savedBy=");
			header.append(System.getProperty("user.name"));
			header.append(", saveDate=");
			header.append(new java.util.Date());
			String keyAlias = getParam(PARAM_KEY_ALIAS);
			if (keyAlias != null && keyAlias.length() > 0) {
				header.append(System.getProperty("line.separator"));
				header.append(PROPS_HEADER);
				header.append("encryptionKey=");
				header.append(keyAlias);
			}
			Boolean encrypt = getBoolean(PARAM_ENCRYPTION);
			propsFile.store(p.getAbsolutePath(), header.toString(),
					getFileCrypto(encrypt != null ? encrypt.booleanValue() : false, p.getAbsolutePath()));
		}
	}

	/**
	 * Initializes the helper iterators objects used for iterating over the keys
	 * of the configured property store.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");

		if (isJavaProperties) {
			mapIterator = map.keySet().iterator();
			jpIterator = System.getProperties().keySet().iterator();
		}

		if (systemStore != null) {
			mapIterator = systemStore.keys().iterator();
		}

		if (propsFile != null) {
			mapIterator = propsFile.keys();
		}

		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * Iterates over the set of keys and returns an entry for each property.
	 *
	 * @return an entry that represents the key/value pair in the specified
	 *         store, or null if the store is exhausted.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymax(this, "getNextEntry");
		String key = null;

		if (mapIterator != null && mapIterator.hasNext()) {
			key = mapIterator.next();
		} else if (jpIterator != null) {
			// Only return javaprops not in the JavaProperties config
			while (jpIterator.hasNext()) {
				key = jpIterator.next().toString();
				if (!map.containsKey(key))
					break;
				key = null;
			}
		}

		if (key == null)
			return null;

		Trace.exitmax(this, "getNextEntry");
		return findEntry(new SearchCriteria("key", SearchCriteria.EXACT, key));
	}

	/**
	 * Looks for a key name in the configured store. The key name is provided
	 * using the first value of a {@link SearchCriteria} object.
	 *
	 * @param search
	 *            the object used to find the specific property. Note: only the
	 *            first criteria is used, the rest (if any) are ignored.
	 * @return an entry that represents the key/value pair in the specified
	 *         store, or null if a property with the specified key name could
	 *         not be found.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		String key = search.getFirstCriteriaValue();
		Object value = null;
		Entry e = new Entry();

		if (isJavaProperties) {

					value = map.get(key);

					if (value == null)
					{
						value = System.getProperty(key);
							// Sometimes the previous line returns null, while the next line finds something. Quite mysterious...
						if (value == null)
							 value = System.getProperties().get(key);
						e.setAttribute("javaprop", Boolean.TRUE);
					}
				}

		if (systemStore != null) {
			value = systemStore.getProperty(key);
		}

		if (propsFile != null) {
			value = propsFile.getProperty(key);
			e.setAttribute("protect", propsFile.isPropertyProtected(key));
		}

		if (value == null)
			return null;

		e.setAttribute("key", key);
		e.setAttribute("value", value);
		return e;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {
		Vector<Entry> v = new Vector<Entry>();
		Entry e = new Entry();
		e.setAttribute("name", "value");
		v.add(e);
		e = new Entry();
		e.setAttribute("name", "key");
		v.add(e);
		return v;
	}

	/**
	 * Adds a key/value pair in the specified property store.
	 *
	 * @param entry
	 *            the entry containing the attributes "key" and "value".
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymax(this, "putEntry", entry);

		String key = entry.getString("key");
		Object value = entry.getObject("value");
		if (entry.getObject("protect") instanceof Boolean)
			setProperty(key, value, (Boolean)entry.getObject("protect"));
		else
			setProperty(key, value, null);

		Trace.exitmax(this, "putEntry");
	}

	/**
	 * Modifies an existing entry. The new entry data is given by the <i>entry</i>
	 * parameter and the search criteria specifies which entry to modify.
	 *
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 *
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		String key = search.getFirstCriteriaValue();
		if (key == null || key.equals("")) {
			throw new com.ibm.di.exceptions.UnsupportedOperation(sResHash
					.getString("CONNECTOR.SCRIPT.MODENTRY.EXCEPTION"));
		}

		Object val = entry.getObject("value");
		if (val == null) {
			val = "";
		}

		if (entry.getObject("protect") instanceof Boolean)
			setProperty(key, val, (Boolean)entry.getObject("protect"));
		else
			setProperty(key, val, null);
	}

	/**
	 * Deletes the specified key/value pair from the configured property store.
	 *
	 * @param entry -
	 *            ignored.
	 * @param search
	 *            the {@link SearchCriteria} object which first criteria object
	 *            is used to find the property to delete.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws Exception {
		String key = search.getFirstCriteriaValue();

		if (isJavaProperties) {

			Object old = map.get(key);
			if (old != null) {
				map.remove(old);
				modified = true;
			}

			String sysOld = System.getProperty(key);
			if (sysOld != null && sysOld.length() > 0) {
				System.setProperty(key, "");
			}
		}

		if (systemStore != null) {
			systemStore.removeProperty(key);
			modified = true;
		}

		if ((propsFile != null) && (propsFile.getProperty(key) != null)) {
			propsFile.removeProperty(key);
			modified = true;
		}

	}

	/**
	 * Set the modified flag, to make sure that the values are saved even if no
	 * change has been made
	 */
	public void setModified() {
		modified = true;
	}

	/**
	 * Return version information
	 *
	 * @return The version value
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
