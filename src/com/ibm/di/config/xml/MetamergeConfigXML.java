/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.ConfigCache;
import com.ibm.di.config.base.ConfigStatistics;
import com.ibm.di.config.base.FileNamespace;
import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.base.RemoteConfigURL;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.security.EncryptedReader;
import com.ibm.di.security.EncryptedWriter;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Version;
import com.ibm.di.util.FileUtils;

/**
 * MetamergeConfigXML is a class that provides support for TDI XML configuration
 * files. It can create new configurations, load and store existing ones. This
 * class provides access to the structure of a TDI configuration as well to its
 * current status.
 */

public class MetamergeConfigXML extends MetamergeConfigImpl {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -4403169711579029765L;

	public final static String SERVER_ENCRYPTED_SIGNATURE = "{PKI ENCRYPTED}\n";

	// XML Document Tag
	public final static String METAMERGE_CONFIG_TAG = "MetamergeConfig";

	public final static String METAMERGE_VERSION_TAG = "version";

	public final static String METAMERGE_CREATED_TAG = "created";

	public final static String METAMERGE_CREATEDBY_TAG = "createdBy";

	public final static String METAMERGE_MODIFIED_TAG = "modified";

	public final static String METAMERGE_MODIFIEDBY_TAG = "modifiedBy";

	public final static String METAMERGE_VERSION_ID = "7.1.1";

	public final static String IDI_VERSION = "IDIversion";

	public final static String IDI_CREATED = "Created by SDI";

	// XML Document & Root element
	private Document document;

	private Element rootElement;

	private ConfigCache cache;

	/**
	 * These are objects that are represented by Proxies which are using the
	 * {@link LazyConfig} invocation handler. A config will not expand until a
	 * particular information is not requested out of it. This instance will
	 * attempt to expand all configs on serialization in order to properly write
	 * the final XML.
	 */
	private transient Map<Name, LazyConfigProtector> lazyConfigs;

	// Needs to save
	private ConfigStatistics stats;

	private boolean alreadySaved;

	private final static ResourceHash sResHash = BaseConfigurationImpl.getResHash();

	public MetamergeConfigXML() {
		super();
	}

	public MetamergeConfigXML(Hashtable<String, Object> env) throws Exception {
		super(env);
	}

	public String getConfigVersion() {
		String s = rootElement.getAttribute(METAMERGE_VERSION_TAG);
		if (s == null)
			return "0";
		else
			return s;
	}

	public boolean isOldVersion() {
		return METAMERGE_VERSION_ID.compareTo(getConfigVersion()) > 0;
	}

	public void logmsg(String msg) {
		Factories.logmsg(msg);
	}

	public void debug(String msg) {
		Factories.debug(msg);
	}

	/**
	 * 
	 * @return true if debug is enabled. Otherwise, false is returned.
	 */
	public boolean isDebugMode() {
		return Factories.isDebugMode();
	}

	/**
	 * Initializes the XML configuration, including the reading and parsing of
	 * the XML file.
	 */
	public synchronized void initializeConfig() throws Exception {
		stats = new ConfigStatistics();

		cache = new ConfigCache();

		// Setup default folders class map
		initializeClassMap();

		// Add XML element - Java Class mapping
		classMap.put(ConnectorFactory.CONNECTOR_TAG, "com.ibm.di.config.base.ConnectorConfigImpl");
		classMap.put(AssemblyLineFactory.ASSEMBLYLINE_TAG, "com.ibm.di.config.base.AssemblyLineConfigImpl");
		classMap.put(ParserFactory.PARSER_TAG, "com.ibm.di.config.base.ParserConfigImpl");
		classMap.put(ScriptFactory.SCRIPT_TAG, "com.ibm.di.config.base.ScriptConfigImpl");
		classMap.put(LibraryFactory.LIBRARY_TAG, "com.ibm.di.config.base.BaseConfigurationImpl");
		classMap.put(NamespaceFactory.NAMESPACE_TAG, "com.ibm.di.config.base.NamespaceConfigImpl");
		classMap.put(ExtPropFactory.EXTPROP_TAG, "com.ibm.di.config.base.ExternalPropertiesImpl");
		classMap.put(LoggingFactory.LOGGING_TAG, "com.ibm.di.config.base.LogConfigImpl");
		classMap.put(FunctionFactory.FUNCTION_TAG, "com.ibm.di.config.base.FunctionConfigImpl");
		classMap.put(ContainerFactory.CONTAINER_TAG, "com.ibm.di.config.base.ContainerConfigImpl");
		classMap.put(ALMappingFactory.ALMAPPING_TAG, "com.ibm.di.config.base.ALMappingConfigImpl");
		classMap.put(TombstonesFactory.TOMBSTONES_TAG, "com.ibm.di.config.base.TombstonesConfigImpl");
		classMap.put(PropertyStoreFactory.PROPERTY_STORE_TAG, "com.ibm.di.config.base.PropertyManagerImpl");
		classMap.put(SchedulerFactory.SCHEDULER_TAG, "com.ibm.di.config.base.SchedulerConfigImpl");

		// XML Doc builder
		DocumentBuilder db = getXMLDocumentBuilder();

		// Get input source
		InputStream is = getInputStream(getDriverParameter(Context.PROVIDER_URL));
		if (is == null) {
			createNewDocument(db);
			return;
		}
		try {
			// If we have keystore params then we have to decrypt it first,
			// but not if we are reading a component definiton file.
			if (env.get(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS) == null && useEncryption()) {
				is = decryptConfiguration(is);
			}

			document = parseXMLConfig(is, db);
		} finally {
			is.close();
			is = null;
		}

		rootElement = document.getDocumentElement();
		if (!rootElement.getNodeName().equals(METAMERGE_CONFIG_TAG))
			throw new Exception(sResHash.getString("improper.root.tag", new Object[] { toString(), METAMERGE_CONFIG_TAG }));

		int cmp = METAMERGE_VERSION_ID.compareTo(getConfigVersion());
		if (cmp < 0)
			throw new Exception(sResHash.getString("new.version.fatal", new Object[] { toString(), METAMERGE_VERSION_ID }));
		if (cmp > 0)
			Factories.logger.warn(sResHash.getString("old.version.warning", new Object[] { toString(), METAMERGE_VERSION_ID,
					getConfigVersion() }));

		// 1.0 to 1.1 conversion
		convertOldNamesAndFolders();

		// Create default folders
		if (env.get(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS) == null)
			addDefaultFolders();

		// 1.1 to 1.2 conversion
		if ("1.2".compareTo(getConfigVersion()) > 0)
			convertExternalProperties();

		setModified(false);
	}

	/**
	 * Obtain the configuration from the specified Context.PROVIDER_URL
	 * parameter.
	 * 
	 * @param path
	 *            The value of the Context.PROVIDER_URL initialization
	 *            parameter.
	 * @return The configuration stream. Null, if the configuration does not
	 *         exist and is supposed to be created.
	 * @throws Exception
	 *             If the configuration cannot be accessed, e.g. if the file is
	 *             missing or is not readable.
	 * @throws PasswordException
	 *             If the configuration is encrypted using a password and the
	 *             password is not provided.
	 */
	private InputStream getInputStream(Object path) throws Exception {

		InputStream is;

		if (path == null) {
			is = null;
		} else if (path instanceof File || path instanceof String || path instanceof RemoteConfigURL) {
			File ff;
			if (path instanceof File) {
				ff = (File) path;
			} else if (path instanceof RemoteConfigURL) {
				logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.PATH.IS.A.REMOTECONFIGURL"));
				String filePath = ((RemoteConfigURL) path).toString();
				ff = new File(filePath);
			} else {
				ff = new File((String) path);
			}

			if (!ff.exists() && "false".equals(getDriverParameter(MetamergeConfigFactory.MC_CREATE))) {
				throw new FileNotFoundException(sResHash.getString("MMCONFIG.METAMCONFIGXML.NO.SUCH.FILE", path.toString()));
			}

			path = ff.getCanonicalPath();
			setDriverParameter(Context.PROVIDER_URL, path);
			setName(MetamergeConfigFactory.parseName(path));

			if ((!ff.exists() || ff.length() == 0) && "false".equals(getDriverParameter(MetamergeConfigFactory.MC_CREATE))) {
				logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.READ.FILE", ff.getPath()));
				throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.READ.FILE", ff.getPath()));
			}

			if (!ff.exists() || ff.length() == 0 || "true".equals(getDriverParameter(MetamergeConfigFactory.MC_CREATE))) {
				logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.CREATE.NEW.FILE", path));
				is = null;
			} else {

				// Check for encrypted file
				if (EncryptedReader.isEncrypted(ff)) {
					if (useEncryption()) {
						throw new com.ibm.di.exceptions.PasswordException(sResHash
								.getString("MMCONFIG.METAMCONFIGXML.CANNOT.LOAD.CLIENT.ENCRYPTED.FILE"));
					} else if (!hasPassword()) {
						throw new com.ibm.di.exceptions.PasswordException(sResHash
								.getString("MMCONFIG.METAMCONFIGXML.MISSING.PASSWORD.FOR.ENCRYPTED.FILE"));
					}
					logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.READ.CURRENT.FILE.ENCRYPTED"));
				} else {
					// The user gave a password, and none is needed.
					// Maybe better to let this fail later on, rather than
					// fixing it now.
					// env.remove(javax.naming.Context.SECURITY_CREDENTIALS);
					logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.READ.CURRENT.FILE"));
				}

				is = new FileInputStream(ff);
			}

		} else if (path instanceof InputStream) {
			logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.READ.INPUT.STREAM"));
			is = (InputStream) path;
			// Remove the InputStream, it cannot be serialized...
			setDriverParameter(Context.PROVIDER_URL, null);

		} else if (path instanceof byte[]) {
			logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.READ.BYTE.ARRAY"));
			is = new ByteArrayInputStream((byte[]) path);

		} else if (path instanceof URL) {
			logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.READ.URL"));
			is = ((URL) path).openStream();
			setName(MetamergeConfigFactory.parseName(path));

		} else {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.UNKNOWN.PROVIDER.URL", path));
		}

		return is;
	}

	/**
	 * @param encryptedStream
	 *            Encrypted configuration.
	 * @return The decrypted configuration.
	 * @throws Exception
	 *             If the configuration is not encrypted. If decryption fails or
	 *             if an I/O error occurs while reading the specified
	 *             configuration.
	 */
	private InputStream decryptConfiguration(InputStream encryptedStream) throws Exception {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] byteArray = new byte[2048];
		int rc;

		// Verify signature
		byte[] signature = SERVER_ENCRYPTED_SIGNATURE.getBytes("UTF-8");
		for (int i = 0; i < signature.length; i++) {
			if (encryptedStream.read() != signature[i]) {
				throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CONFIG.FILE.IS.NOT.PKI.ENCRYPTED"));
			}
		}

		// Read rest of PKI encrypted data
		while ((rc = encryptedStream.read(byteArray)) != -1) {
			bos.write(byteArray, 0, rc);
		}

		InputStream decryptedStream;
		try {
			byte[] decrypted = CryptoUtils.decryptWithServerKey(bos.toByteArray());
			decryptedStream = new ByteArrayInputStream(decrypted);
		} catch (Exception error) {
			error.printStackTrace();
			throw error;
		}
		bos.close();
		bos = null;

		return decryptedStream;
	}

	/**
	 * Parses the XML document from the input stream and converts it into a DOM
	 * object.
	 * 
	 * @param is
	 *            The input stream the XML document will be read from.
	 * @param db
	 *            Document builder used to convert the XML document into DOM
	 *            objects
	 * @return Document object representing the XML document from the input
	 *         stream
	 */
	private Document parseXMLConfig(InputStream is, DocumentBuilder db) throws Exception {
		if (hasPassword()) {
			EncryptedReader input = new EncryptedReader(is);
			input.useKey(getPassword());
			return db.parse(new InputSource(new InputStreamReader(input.getInputStream(), "UTF-8")));
		} else {
			return db.parse(is);
		}
	}

	/**
	 * Creates a new DOM document with an empty root element.
	 * 
	 * @param db
	 *            Document builder used to create the DOM objects.
	 */
	private void createNewDocument(DocumentBuilder db) throws Exception {
		document = db.newDocument();
		rootElement = document.createElement(METAMERGE_CONFIG_TAG);

		rootElement.setAttribute(METAMERGE_VERSION_TAG, METAMERGE_VERSION_ID);
		rootElement.setAttribute(METAMERGE_CREATED_TAG, (new java.util.Date()).toString());
		rootElement.setAttribute(METAMERGE_CREATEDBY_TAG, System.getProperty("user.name"));

		document.appendChild(rootElement);

		if (env.get(MetamergeConfigFactory.MC_NO_DEFAULT_FOLDERS) == null)
			addDefaultFolders();
	}

	/**
	 * Called by super's lookup
	 */
	protected synchronized Object internalLookup(Object namex) throws Exception {

		Name name = MetamergeConfigFactory.parseName(namex);

		// Cached entry?
		Object obj = cache.getObject(name);
		if (obj != null) {
			if (isDebugMode()) {
				debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.FOUND.CACHED.OBJECT", name));
			}
			// TODO: Check if this is really necessary!
			// ((BaseConfiguration)obj).setupInheritanceChain();
			return obj;
		}

		// External file attribute map?
		if (name.toString().startsWith("file:")) {
			String s = name.toString().substring(5);
			if (s.endsWith(FileNamespace.EXTERNAL_ATTRIBUTE_MAP_EXTENSION)) {
				BaseConfiguration bc = FileNamespace.createMap(s, this);
				if (bc != null) {
					bc.setModified(false);
					cache.addObject(name, bc);
					return bc;
				}
			}
		}

		// Local reference?
		if (!MetamergeConfigFactory.isNameLocal(this, name))
			return MetamergeConfigFactory.lookup(this, name);

		// Lazy config object?
		if (getLazyConfigsMap(false) != null) {
			LazyConfigProtector cfg = getLazyConfigsMap(false).get(name);
			if (cfg != null) {
				return cfg.getConfig();
			}
		}

		// Locate named element in XML document
		Element elem = findByName(getRootElement(), name);

		// Disable setting timestamps while we are parsing the new object
		boolean save = isModTSEnabled();
		setModTSEnabled(false);

		// Call factories to produce a java object
		BaseConfiguration base = Factories.getImpl(elem.getNodeName());
		base.setName(name);
		base.setMetamergeConfig(this);
		cache.addObject(base.getName(), base); // Save in cache

		Factories.getFactory(elem.getNodeName()).parse(base, elem);

		// Initialize object
		base.init();
		base.setupInheritanceChain();
		base.setModified(false);

		setModTSEnabled(save);

		// Statistics
		stats.lookup();

		return base;
	}

	/**
	 * This method returns an enumeration with the names in the given folder. If
	 * the name does does not name a folder an exception is thrown.
	 */
	public Enumeration<Binding> list() throws Exception {
		return list("");
	}

	public Enumeration<Binding> list(Object name) throws Exception {
		// Locate named element
		Name n = MetamergeConfigFactory.parseName(name);
		Element elem = findByName(getRootElement(), n);
		return new ElementEnumeration(elem, n);
	}

	/**
	 * This method creates a folder
	 */
	public MetamergeFolder createFolder(Object name) throws Exception {
		Name folderName = MetamergeConfigFactory.parseName(name);

		if (folderName.size() == 0) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.SINGLE.NAME.COMPONENT.REQUIRED"));
		} else if (folderName.size() != 1) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.RECURSIVELY.CREATE.FOLDERS"));
		}

		MetamergeFolder folder = (MetamergeFolder) Factories.getImpl(FolderFactory.FOLDER_TAG);
		folder.setName(folderName);
		folder.setMetamergeConfig(this);
		bind(folderName, folder);
		return folder;
	}

	/**
	 * 
	 * Create a PropertyStoreConfig with the Properties connector for each
	 * external property config.
	 */
	private void convertExternalProperties() throws Exception {
		PropertyManager pm = (PropertyManager) internalLookup(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);
		MetamergeFolder xp = (MetamergeFolder) internalLookup(MetamergeConfig.DEFAULT_EXTPROP_FOLDER);
		String[] names = xp.getNames();

		for (int i = 0; i < names.length; i++) {
			ExternalPropertiesConfig epc = (ExternalPropertiesConfig) internalLookup(MetamergeConfig.DEFAULT_EXTPROP_FOLDER + "/"
					+ names[i]);
			if (epc.getFilePath() == null || epc.getFilePath().length() == 0)
				continue;

			PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
			psc.init();
			psc.setMetamergeConfig(this);

			RawConnectorConfig rcc = psc.getConnectionConfig();
			rcc.setParameter("collection", epc.getFilePath());
			rcc.setParameter("collectionType", "User-Defined");
			rcc.setBooleanParameter("encryption", epc.getEncrypted());
			rcc.setParameter("cipher", epc.getCipher());
			if (epc.getPassword() == null || epc.getPassword().equals(""))
				rcc.setParameter("password", "BlowFish32");
			else
				rcc.setParameter("password", epc.getPassword());
			rcc.setBooleanParameter("autorewrite", false);

			rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties");
			rcc.setParent(psc);
			rcc.setupInheritanceChain();

			psc.setName(epc.getShortName());
			psc.setKeyAttribute("key");
			psc.setValueAttribute("value");
			psc.setInitialLoad(true);

			pm.addPropertyStore(psc);

			// removeElement(epc.getName());
		}

		removeElement(MetamergeConfig.DEFAULT_EXTPROP_FOLDER);

		try {
			pm.setDefaultPropertyStore(pm.getPropertyStore(" Default"));
			return;
		} catch (Exception err) {
		}

		try {
			pm.setDefaultPropertyStore(pm.getPropertyStore("Default"));
			return;
		} catch (Exception err) {
		}

		try {
			pm.setDefaultPropertyStore(pm.getPropertyStore("_Default"));
			return;
		} catch (Exception err) {
		}
	}

	private void convertOldNamesAndFolders() throws Exception {

		// Server --> Config
		Element elem = findOneByName(getRootElement(), "Server");
		if (elem != null) {
			elem.setAttribute(Factories.NAME_ATTRIBUTE, MetamergeConfig.DEFAULT_SERVER_FOLDER);
		}

		// ExtProp --> ExtProp/_Default
		elem = findOneByName(getRootElement(), MetamergeConfig.DEFAULT_EXTPROP_FOLDER);
		if (elem != null && !("Folder".equals(elem.getTagName()))) {

			Element parent = getRootElement();

			// Remove old style ext prop
			parent.removeChild(elem);

			// Create new folder and add ext prop to it
			Element folder = parent.getOwnerDocument().createElement(FolderFactory.FOLDER_TAG);
			folder.setAttribute(Factories.NAME_ATTRIBUTE, MetamergeConfig.DEFAULT_EXTPROP_FOLDER);
			parent.appendChild(folder);
			elem.setAttribute(Factories.NAME_ATTRIBUTE, MetamergeConfig.DEFAULT_EXTPROP_NAME);
			folder.appendChild(elem);
		}

		// Logging/server --> Server/Logging

		Element old = findOneByName(getRootElement(), "Logging");
		if (old == null)
			return; // No Logging folder

		if (findOneByName(getRootElement(), MetamergeConfig.DEFAULT_SERVER_FOLDER) == null) {
			// No server folder. Rename this folder to be server.
			old.setAttribute(Factories.NAME_ATTRIBUTE, MetamergeConfig.DEFAULT_SERVER_FOLDER);
			// Rename the server node to Logging
			Element oldLog = findOneByName(old, "server");
			if (oldLog != null && findOneByName(old, "Logging") == null)
				oldLog.setAttribute(Factories.NAME_ATTRIBUTE, "Logging");
		} else {
			// Both Logging and Server folder. This should not happen, but just
			// remove one of them, e.g. the Logging folder
			getRootElement().removeChild(old);
		}
	}

	/**
	 * This method traverses the document tree searching for an element with an
	 * attribute (name) equal to name.
	 */
	public Element findByName(Element root, Object name) throws Exception {
		Name st = null;
		Element elem = root;

		st = toName(name);

		if (st.size() == 0)
			return root;

		for (int i = 0; i < st.size(); i++) {
			// System.out.println ( " next name: " + st.get(i));
			if (st.get(i).equals(""))
				continue;
			elem = findOneByName(elem, st.get(i));
			if (elem == null)
				break;
		}
		if (elem == null)
			throw new javax.naming.NameNotFoundException(name.toString());

		// System.out.println ( " findByName found: " + elem.getTagName() );
		return elem;
	}

	private Name toName(Object name) throws InvalidNameException {
		Name st;
		// Make sure we have a correctly parsed name
		if (name instanceof Name)
			st = (Name) name;
		else
			st = MetamergeConfigFactory.parseName(name.toString());
		return st;
	}

	/**
	 * This method searches the children of an element for an element with an
	 * attribute (name) equal to name.
	 */
	public Element findOneByName(Element root, String name) {
		Node child = root.getFirstChild();

		while (child != null) {

			if (child.getNodeType() != Node.ELEMENT_NODE) {
				child = child.getNextSibling();
				continue;
			}

			if ((!((Element) child).hasAttribute(Factories.NAME_ATTRIBUTE)) && (child.getNodeName().equals(name))) {
				return (Element) child;
			}

			if (((Element) child).getAttribute(Factories.NAME_ATTRIBUTE).equals(name))
				return (Element) child;

			child = child.getNextSibling();
		}
		return null;
	}

	/**
	 * Returns the root element of the XML document.
	 */
	public Element getRootElement() {
		return rootElement;
	}

	/**
	 * Returns the XML document object.
	 */
	public Document getDocument() {
		return document;
	}

	// Add operation
	public void bind(Object name, Object obj) throws Exception {
		internalBind(name, obj, false);
		stats.bind();
	}

	// Add/Replace operation
	public void rebind(Object name, Object obj) throws Exception {
		internalBind(name, obj, true);
		stats.rebind();
	}

	// Rename operation
	public synchronized void rename(Object name, Object newname) throws Exception {
		BaseConfiguration curr = (BaseConfiguration) internalLookup(name);
		Name old = MetamergeConfigFactory.parseName(name);
		Name nn = MetamergeConfigFactory.parseName(newname);
		if (old.size() < 2) {
			throw new Exception(sResHash.getString("MMCONFIG.RENAME.STANDARDFOLDERERROR"));
		}
		if (nn.size() != 1) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.MOVE.NOT.SUPPORTED"));
		}

		if (nn.size() == 1)
			nn.addAll(0, old.getPrefix(old.size() - 1));

		try {
			internalLookup(nn);
			throw new NameAlreadyBoundException(nn.toString());
		} catch (NameNotFoundException nfe) {
			// could be a lazy one
			Map<Name, LazyConfigProtector> map = getLazyConfigsMap(false);
			if (map != null && map.get(nn) != null) {
				throw new javax.naming.NameAlreadyBoundException(nn.toString());
			}
		}

		// remove the old one
		Map<Name, LazyConfigProtector> map = getLazyConfigsMap(false);
		if (isLazyConfig(curr) && map != null && map.remove(old) != null) {
			if (((LazyConfig) Proxy.getInvocationHandler(curr)).isLoaded()) {
				// bind it as a normal one if it has been expanded
				curr.setName(nn);
				curr.setModified(true);
				internalBind(nn, curr, false);
			} else {
				// it's still an empty shell keep it as lazy one
				map.put(nn, new LazyConfigProtector(curr));
				((LazyConfig) Proxy.getInvocationHandler(curr)).setName(nn);
				((LazyConfig) Proxy.getInvocationHandler(curr)).setModified(true);
			}
		} else {
			Element elem = findByName(getRootElement(), old);
			if (elem == null || elem.getParentNode() == null) {
				throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.INTERNAL.ERROR.XML.ELEMENT.NOT.FOUND"));
			} else
				elem.setAttribute(Factories.NAME_ATTRIBUTE, newname.toString());

			curr.setName(nn);
			curr.setModified(true);
			cache.removeObject(old);
			cache.addObject(nn, curr);
		}

		notifyChange(this, old, MetamergeConfigChange.MCC_MODIFY, nn);
	}

	// Delete operation
	public void unbind(Object name) throws Exception {
		if (MetamergeConfigFactory.isStandardObject(name)) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.REMOVE.STANDARD.OBJECT"));
		} else
			removeElement(name);
	}

	protected synchronized void removeElement(Object name) throws Exception {
		// check lazyConfigs first
		Map<Name, LazyConfigProtector> map = getLazyConfigsMap(false);
		if (!(map != null && map.remove(toName(name)) != null)) {

			// if here then it wasn't a lazy config, try a normal find
			Element elem = findByName(getRootElement(), name);
			if (elem == null)
				return; // cannot happen

			if (elem.getParentNode() == null) {
				throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.REMOVE.TOP.LEVEL.ELEMENT"));
			}

			cache.removeObject(name);
			elem.getParentNode().removeChild(elem);
		}

		notifyChange(this, name, MetamergeConfigChange.MCC_DELETE);
		stats.unbind();
	}

	private synchronized void internalBind(Object objname, Object obj, boolean isRebind) throws Exception {
		Name name = MetamergeConfigFactory.parseName(objname);
		Name parent = name.getPrefix(name.size() - 1);

		Element current = null;
		boolean oldLazy = false;
		// Existing can only be there when we rebind
		try {
			current = findByName(getRootElement(), name);
			if (!isRebind)
				throw new javax.naming.NameAlreadyBoundException(name.toString());
		} catch (NameNotFoundException nnfe) {
			// could be a lazy one
			Map<Name, LazyConfigProtector> map = getLazyConfigsMap(false);
			if (map != null && (oldLazy = (map.get(name) != null)) && !isRebind) {
				throw new javax.naming.NameAlreadyBoundException(name.toString());
			}
		}

		boolean normalBind = !(isLazyConfig(obj) && !((LazyConfig) Proxy.getInvocationHandler(obj)).isLoaded());

		if (normalBind) {
			// Transform BaseConfig to XML Element tree
			Element newElement = transformObject(name, (BaseConfiguration) obj);

			Element parentNode;
			// Need parentNode in any case
			if (isDebugMode()) {
				debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.INTERNALBIND.OBJNAME", new Object[] { objname,
						Integer.valueOf(parent.size()), parent }));
			}
			parentNode = findByName(getRootElement(), parent);

			// Add/Replace
			if (current != null) {
				if (obj instanceof MetamergeFolder && !(obj instanceof PropertyManager)
						&& !MetamergeConfig.DEFAULT_NAMESPACE_FOLDER.equals(objname))
					moveChildNodes(current, newElement);
				parentNode.replaceChild(newElement, current);
			} else {
				parentNode.appendChild(newElement);
			}

			// old one could be lazy
			if (oldLazy) {
				getLazyConfigsMap(false).remove(name);
			}

			// Clear modified flag
			((BaseConfiguration) obj).setModified(false);
			// Cache it
			cache.addObject(name, (BaseConfiguration) obj);
		} else {
			if (current != null) {
				Element parentNode = findByName(getRootElement(), parent);
				parentNode.removeChild(current);
			}
			getLazyConfigsMap(true).put(name, new LazyConfigProtector((BaseConfiguration) obj));
			((LazyConfig) Proxy.getInvocationHandler(obj)).setModified(true);
		}

		// Send notification (must be sent AFTER the bind or objects could
		// lookup it up before we cache it)
		if (current != null || oldLazy)
			notifyChange(this, name, MetamergeConfigChange.MCC_MODIFY);
		else
			notifyChange(this, name, MetamergeConfigChange.MCC_ADD);
	}

	private boolean isLazyConfig(Object obj) {
		return Proxy.isProxyClass(obj.getClass()) && Proxy.getInvocationHandler(obj) instanceof LazyConfig;
	}

	/**
	 * Moves an element's child nodes to another element.
	 */
	private void moveChildNodes(Element source, Element dest) throws Exception {
		while (source.hasChildNodes()) {
			dest.appendChild(source.getFirstChild());
		}
	}

	/**
	 * Creates an Element with child nodes representing the base config object.
	 */
	private Element transformObject(Name name, BaseConfiguration obj) throws Exception {
		// Create new Element for config object
		String tag = Factories.getClassTag(obj);
		if (tag == null) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.NO.XML.FACTORY.FOR.OBJECT", new Object[] { name,
					obj.getClass().getName() }));
		}
		if (isDebugMode()) {
			debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.TRANSFORMOBJECT.NAME", new Object[] { name, obj.getClass().getName(),
					tag }));
		}
		Element elem = getDocument().createElement(tag);
		BaseConfiguration inherit = obj.getInheritsFrom();
		try {
			obj.setInheritsFrom(null);
			Factories.getFactory(tag).build(obj, elem);
		} finally {
			obj.setInheritsFrom(inherit);
		}
		return elem;
	}

	/**
	 * Save XML tree to output stream.
	 */
	public synchronized void commitChanges(Object output, boolean isSave) throws Exception {
		logmsg(sResHash.getString("MMCONFIG.METAMCONFIGXML.METAMERGEXML.COMMIT.CHANGES"));

		// Verify that password is not given when we run in secure mode
		if (useEncryption() && hasPassword()) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.SAVING.CONFIGURATION.IN.SERVER"));
		}

		if (isOldVersion()) {
			if (output != null && commitOldVersionChanges(output))
				return;
			if (output == null && !alreadySaved && commitOldVersionChanges())
				return;
		}

		// Update underlying document with entries from dirty cache
		if (isDebugMode()) {
			debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.COMMIT.CHANGES.PROCESS.DIRTY.LIST"));
		}

		for (String name : cache.getDirtyList()) {
			if (isDebugMode()) {
				debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.COMMIT.CHANGES.NEXT.DIRTY.NAME", name));
			}
			BaseConfiguration config = cache.getObject(name);
			rebind(name, config);
			if (!isSave)
				config.setModified(true);
		}

		// expand lazy configs
		Map<Name, LazyConfigProtector> map = getLazyConfigsMap(false);
		if (map != null) {
			for (Entry<Name, LazyConfigProtector> lazy : map.entrySet()) {
				try {
					((LazyConfig) Proxy.getInvocationHandler(lazy.getValue().getConfig())).loadConfig();
					internalBind(lazy.getKey(), lazy.getValue().getConfig(), false);
				} catch (Throwable e) {
					if (e instanceof Exception) {
						throw (Exception) e;
					} else {
						throw new Exception(e);
					}
				}
			}
		}
		// clean up lazy map as all configs have been expanded.
		lazyConfigs = null;

		// Make sure we have the correct version
		getRootElement().setAttribute(METAMERGE_VERSION_TAG, METAMERGE_VERSION_ID);

		if (isSave) {
			if (isDebugMode()) {
				debug(sResHash.getString("MMCONFIG.METAMCONFIGXML.COMMIT.CHANGES.UPDATE.MODIFIED.FLAGS"));
			}
			// Update last modified tag
			getRootElement().setAttribute(METAMERGE_MODIFIED_TAG, (new java.util.Date()).toString());
			getRootElement().setAttribute(METAMERGE_MODIFIEDBY_TAG, System.getProperty("user.name"));
			getRootElement().setAttribute(IDI_VERSION, IDI_CREATED + Version.version());
		}
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		// String password = (String) getDriverParameter(
		// javax.naming.Context.SECURITY_CREDENTIALS );
		String encoding = System.getProperty(RS.PROP_CONFIG_ENCODING);
		String password = getPassword();
		boolean hasPassword = (password != null && password.length() > 0);
		EncryptedWriter out = null;
		OutputStream outStream = null;
		StreamResult streamResult = null;

		try {
			// Encrypt via server PKI?
			if (useEncryption()) {
				outStream = new ByteArrayOutputStream();
				streamResult = new StreamResult(outStream);
				t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			} else {
				outStream = getOutputStream(output);
				if (hasPassword) {
					Writer writer = new OutputStreamWriter(outStream, "UTF-8");
					out = new EncryptedWriter(writer, outStream);
					out.useKey(password);
					streamResult = new StreamResult(out.getOutputStream());
					t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				} else {
					if (encoding != null && encoding.length() > 0) {
						t.setOutputProperty(OutputKeys.ENCODING, encoding);
					}
					streamResult = new StreamResult(outStream);
				}
			}

			t.transform(new DOMSource(getRootElement()), streamResult);

			if (useEncryption()) {
				try {
					byte[] data = ((ByteArrayOutputStream) outStream).toByteArray();
					byte[] encrypted = CryptoUtils.encryptWithServerKey(data);
					outStream = getOutputStream(output);
					outStream.write(SERVER_ENCRYPTED_SIGNATURE.getBytes("UTF-8"));
					outStream.write(encrypted);
				} catch (Exception error) {
					error.printStackTrace();
					throw error;
				}
			}

		} finally {
			if (out != null) {
				out.close();
			}
			if (outStream != null) {
				outStream.close();
			}
		}

		if (isSave)
			setModified(false);
		stats.reset();
	}

	private boolean commitOldVersionChanges() throws Exception {
		File f;
		Object output = getDriverParameter(Context.PROVIDER_URL);
		if (output instanceof File)
			f = (File) output;
		else if (output instanceof String)
			f = new File((String) output);
		else
			return false;

		// Rename old version (use dot file to avoid TDI picking up this backup file)
		File parentDir = f.getAbsoluteFile().getParentFile();
		File bak = new File(parentDir, "." + f.getName() + "." + getConfigVersion());

		if (!f.renameTo(bak))
			return false;
		Factories.logger.warn(sResHash.getString("old.version.saved", bak.getAbsolutePath()));

		Hashtable<String, Object> newenv = new Hashtable<String, Object>(env);
		newenv.put(MetamergeConfigFactory.MC_CREATE, "true");

		MetamergeConfigXML dest = new MetamergeConfigXML(newenv);

		try {
			MetamergeConfigFactory.copy(this, dest, null, true);
			dest.commitChanges(null, true);
		} catch (Exception e) {
			if (!f.exists() && bak.exists()) {
				Factories.logger.warn(sResHash.getString("MMCONFIG.METAMCONFIGXML.RESTORING.ABSOLUTEPATH", new Object[] {
						bak.getAbsolutePath(), f.getAbsolutePath() }));
				FileUtils.renameTo(bak, f);
			}
			throw e;
		}

		setNoBackupOfOldVersion();
		return true;
	}

	private boolean commitOldVersionChanges(Object output) throws Exception {

		Hashtable<String, Object> newenv = new Hashtable<String, Object>(env);
		newenv.put(MetamergeConfigFactory.MC_CREATE, "true");

		MetamergeConfigXML dest = new MetamergeConfigXML(newenv);

		MetamergeConfigFactory.copy(this, dest, null, true);
		dest.commitChanges(output, true);
		return true;
	}

	public void setOutput(Object output) throws Exception {
		setDriverParameter(Context.PROVIDER_URL, output);
	}

	public boolean isCommittable() {
		Object output = null;

		try {
			output = getDriverParameter(Context.PROVIDER_URL);
		} catch (Exception ignore) {
		}

		if (output instanceof RemoteConfigURL) {
			return true; // remote config
		}
		if (output instanceof String)
			output = new File((String) output);

		if (output instanceof File) {

			File out = (File) output;
			if (out.isDirectory())
				return false;
			else if (out.exists())
				return out.canWrite();

			File dir = out.getParentFile();
			if (dir == null)
				return true;
			else
				return dir.canWrite();

		} else if (output instanceof OutputStream || output instanceof URLConnection) {
			return true;
		}

		return false;
	}

	public boolean isReadOnly() {
		return !isCommittable();
	}

	/**
	 * XML Doc needs to be saved if we have dirty objects in our cache or if
	 * there has been any successful bind/rebind/unbind calls.
	 */
	public synchronized boolean getModified() {
		return (cache.getDirtyList().size() > 0 || stats.getModCount() > 0);
	}

	public BaseConfiguration newInstanceOf(Object typeName) throws Exception {
		String cls = classMap.get(typeName);
		// System.out.println ( "newInstanceOf: " + typeName + ": " + cls );
		if (cls == null)
			cls = DEFAULT_FOLDER_IMPL;

		Object obj = Class.forName(cls).newInstance();
		((BaseConfiguration) obj).setMetamergeConfig(this);
		((BaseConfiguration) obj).init();
		// ((BaseConfiguration)obj).setupInheritanceChain ();
		((BaseConfiguration) obj).setModified(false);
		return (BaseConfiguration) obj;
	}

	/**
	 * Returns the output stream to which the XML document is written. Called by
	 * commitChanges.
	 */
	public OutputStream getOutputStream(Object out) throws Exception {
		Object output;
		if (out != null)
			output = out;
		else
			output = getDriverParameter(Context.PROVIDER_URL);

		if (output instanceof File)
			return new FileOutputStream((File) output);
		else if (output instanceof String)
			return new FileOutputStream(new File(output.toString()));
		else if (output instanceof OutputStream)
			return (OutputStream) output;
		else if (output instanceof URLConnection)
			return ((URLConnection) output).getOutputStream();
		else if (output == null) {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.CANNOT.SAVE.NO.OUTPUT.SPECIFIED"));
		} else {
			throw new Exception(sResHash.getString("MMCONFIG.METAMCONFIGXML.UNKNOWN.OUTPUT.OBJECT.CLASS", output.getClass()
					.getName()));
		}
	}

	/**
	 * Class used by list() method to return a list of names/objects at a
	 * specific branch.
	 */
	private class ElementEnumeration implements Enumeration<Binding> {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		static final long serialVersionUID = -4403169711579029765L;

		private Node child;

		private Name base;

		private Iterator<Entry<Name, LazyConfigProtector>> lazyEnum;

		public ElementEnumeration(Element root, Name base) {
			this.base = (Name) base.clone();
			this.child = null;
			if (root.hasChildNodes()) {
				setNextChild(root.getFirstChild());
			}

			Map<Name, LazyConfigProtector> cfg = getLazyConfigsMap(false);
			if (cfg != null) {
				Set<Entry<Name, LazyConfigProtector>> filtered = null;
				for (Entry<Name, LazyConfigProtector> e : cfg.entrySet()) {
					if (e.getKey().startsWith(base)) {
						if (filtered == null) {
							filtered = new HashSet<Entry<Name, LazyConfigProtector>>();
						}
						filtered.add(e);
					}
				}
				if (filtered != null) {
					lazyEnum = filtered.iterator();
				}
			}
		}

		public void setNextChild(Node nextChild) {
			child = nextChild;
			if (child == null)
				return;
			while (child != null && child.getNodeType() != Node.ELEMENT_NODE)
				child = child.getNextSibling();
		}

		public boolean hasMoreElements() {
			return (child != null) || (lazyEnum != null && lazyEnum.hasNext());
		}

		public Binding nextElement() {
			Binding bnd = null;
			if (child != null) {
				String key = "";
				try {
					Name name;
					key = ((Element) child).getAttribute(Factories.NAME_ATTRIBUTE);
					if (key == null || key.equals(""))
						key = child.getNodeName();

					if (base != null)
						name = MetamergeConfigFactory.parseName(base.clone()).add(key);
					else
						name = MetamergeConfigFactory.parseName(key);

					BaseConfiguration b = (BaseConfiguration) lookup(name);
					bnd = new Binding(name.get(name.size() - 1), b);

				} catch (Exception err) {
					err.printStackTrace();
					Factories.logger.error(err.toString());
					bnd = new Binding(key, "ERROR: " + err.toString());
				}
				setNextChild(child.getNextSibling());
			} else if (lazyEnum != null && lazyEnum.hasNext()) {
				Entry<Name, LazyConfigProtector> e = lazyEnum.next();
				bnd = new Binding(e.getKey().get(e.getKey().size() - 1), e.getValue().getConfig());
			}
			return bnd;
		}
	}

	public boolean isRemote() {
		Object output = null;
		try {
			output = getDriverParameter(Context.PROVIDER_URL);
		} catch (Exception ignore) {
		}
		if (output instanceof RemoteConfigURL)/* Remote config */
			return true;
		else
			return false;
	}

	public String getShortName() {
		try {
			if (isRemote())
				return ((RemoteConfigURL) this.getDriverParameter(Context.PROVIDER_URL)).toString();
		} catch (Exception ignore) {
		}
		return super.getShortName();
	}

	protected ConfigCache getCache() {
		return cache;
	}

	public String getDirectory() {
		String dir = getRootElement().getAttribute(MetamergeConfigFactory.MC_CONFIG_DIRECTORY);
		if (dir != null && dir.length() > 0)
			return dir;
		else
			return super.getDirectory();
	}

	/**
	 * Sets a flag to prevent an old version of the file to be used as backup.
	 * @since 7.2
	 */
	public synchronized void setNoBackupOfOldVersion() {
		//TODO: use this method in 
		// osgi/plugins/com.ibm.di.ui.easyetl/src/com/ibm/di/ui/easyetl/internal/templates/TemplatesHandler.java
		//         private void _createNewTemplate(@Context HttpServletRequest req, InMultiPart inMP) throws Exception {
		// just before the mc.commitChanges(null)
		alreadySaved = true;
	}

	/**
	 * We keep only one DocumentBuilderFactory, for efficiency.
	 * 
	 * @since 7.1.1
	 */
	private static DocumentBuilderFactory myDocumentBuilderFactory;

	private static DocumentBuilder getXMLDocumentBuilder() throws Exception {
		if (myDocumentBuilderFactory == null)
			myDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = myDocumentBuilderFactory.newDocumentBuilder();
		db.setErrorHandler(new DefaultHandler());
		return db;
	}

	private Map<Name, LazyConfigProtector> getLazyConfigsMap(boolean autoCreate) {
		if (lazyConfigs == null && autoCreate) {
			lazyConfigs = new HashMap<Name, LazyConfigProtector>();
		}
		return lazyConfigs;
	}

	public static abstract class LazyConfig implements InvocationHandler {

		private Name fullName;

		private boolean modified;

		public LazyConfig(Name fullName) {
			this.fullName = fullName;
		}

		public void setModified(boolean val) {
			modified = val;
		}

		public void setName(Object name) throws InvalidNameException {
			if (name == null)
				return;
			fullName = MetamergeConfigFactory.parseName(name);
		}

		public Name getName() {
			return fullName;
		}

		public boolean getModified() {
			return modified;
		}

		public abstract boolean isLoaded();

		public abstract void loadConfig() throws Throwable;
	}

	/**
	 * Keeps the Lazy Config off the hands of the HashMap and thus delays its
	 * load.
	 * 
	 * @since 7.1.1
	 */
	private static class LazyConfigProtector {
		private final BaseConfiguration bc;

		public LazyConfigProtector(BaseConfiguration bc) {
			this.bc = bc;
		}

		public BaseConfiguration getConfig() {
			return bc;
		}
	}
}
