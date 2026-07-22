/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.ibm.di.api.APIEngine;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * The MetamergeConfigFactory class provides a number of static methods for use
 * by applications to obtain MetamergeConfig objects. Each MetamergeConfig
 * object is registered in the global namespace with a unique name. This
 * namespace is used by MetamergeConfig objects when they refer to object in
 * other configurations. The resolving of names is also provided by this class.
 * Typically, a MetamergeConfig object will always call the isNameLocal() method
 * to determine whether a name is handled by itself or another MetamergeConfig
 * object. In the latter case a subsequent call to the lookup() method will
 * cause this class to resolve, and potentially load, the MetamergeConfig object
 * that is able to access the named object.
 * 
 * <h3>Getting MetamergeConfig Objects</h3> An application should use either
 * getFileInstance(Object) or getInstance(Hashtable) to obtain a MetamergeConfig
 * object. The first variant is a convenience method that calls
 * getInstance(Hashtable) by setting key/value pairs in the hashtable. The
 * following keywords are reserved:
 * <p>
 * <table>
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>javax.naming.Context.PROVIDER_URL</td>
 * <td>The URL the driver uses to locate the configuration store</td>
 * </tr>
 * <tr>
 * <td>MetamergeConfigFactory.MC_DRIVER</td>
 * <td>The class name for the MetamergeConfig implementation</td>
 * </tr>
 * <tr>
 * <td>MetamergeConfigFactory.MC_CREATE</td>
 * <td>Specifies wether the driver should create the endpoint if it does not
 * exist</td>
 * </tr>
 * <tr>
 * <td>MetamergeConfigFactory.MC_PARSER</td>
 * <td>The class name for the parser (if the driver needs one)</td>
 * </tr>
 * <tr>
 * <td>MetamergeConfigFactory.MC_DEBUG</td>
 * <td>true/false to set the debug flag in the driver</td>
 * </tr>
 * </table>
 * <p>
 * <h3>Names</h3>
 * All applications and MetamergeConfig implementations should use the
 * parseName() method to convert a name to a javax.naming.Name instance. The
 * name syntax used by MetamergeConfigFactory and MetamergeConfig objects are
 * words separated by a slash (/). An example would be <i>AssemblyLines/MyAL</i>
 * which would point to an object called <i>MyAL</i> in a folder named
 * <i>AssemblyLines</i>. To facilitate references to objects in other
 * configuration stores a namespace ID can also be part of a name. If the first
 * component in a Name ends with a colon (:) it is considered to be a namespace
 * reference. The following name <i>system:/Connectors/ibmdi.LDAP</i> refers to
 * <i>/Connectors/ibmdi.LDAP</i> in the MetamergeConfig object registered as
 * <i>system</i> in this class. The words <i>system</i> and <i>internal</i> are
 * reserved for use by IBM DI.
 * 
 * <h3>Resolving Names</h3>
 * When a MetamergeConfig object calls MetamergeConfigFactory.lookup() with a
 * non-local reference it will first search the global namespace for the named
 * MetamergeConfig object. If no such object exists, this class will look in the
 * caller MetamergeConfig's namespace folder for a namespace that matches the
 * requested name. If a namespace reference is found in the caller's namespace
 * folder, it is loaded and registered in the global namespace. The name
 * registered in the global namespace is the File/URL for the loaded
 * MetamergeConfig and not the requested namespace itself.
 * <p>
 * For example, if A calls lookup using <i>ext:Properties</i> as name and A also
 * has a namespace (e.g. external reference) named <i>ext</i> defined, this
 * class will load the MetamergeConfig as defined by A's local namespace
 * definition. Assuming A's namespace definition looks like this: <i>ext --->
 * d:/mm/ext.cfg</i> then that file will be loaded and registered in the global
 * namespace as <i>d:/mm/ext.cfg</i>. The local namespace reference is only
 * valid for the calling MetamergeConfig object but the MetamergeConfig object
 * it refers to may be used by other MetamergeConfig objects as well. For
 * example, B might have a similar configuration but instead of <i>ext</i> it
 * may refer to <i>d:/mm/ext.cfg</i> as <i>library</i>. When B then calls lookup
 * with <i>library:Properties</i> this class will then find out that the file
 * (d:/mm/ext.cfg) is already loaded and that instance is reused for B as well.
 * Thus, there are local namespace references and global namespace references.
 * Local namespace references are valid only in the MetamergeConfig object they
 * are defined whereas global namespace references (like system) are globally
 * available.
 */
public class MetamergeConfigFactory {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Reserved namespace ID for installed components etc ...
	 */
	public final static String SYSTEM_NAMESPACE = "system";

	/**
	 * Reserved namespace ID for standard forms etc ...
	 */
	public final static String STDFORMS_NAMESPACE = SYSTEM_NAMESPACE;

	/**
	 * Reserved namespace ID for adapters
	 */
	public final static String ADAPTERS_NAMESPACE = "adapter";

	/**
	 * Driver parameter - If driver needs a parser this parameter specifies the
	 * class to use
	 */
	public final static String MC_PARSER = "com.ibm.di.config.interfaces.fsctx.parser";

	/**
	 * Driver parameter - Specify "true" or "false" to set the debug mode option
	 * for the driver
	 */
	public final static String MC_DEBUG = "com.ibm.di.config.interfaces.fsctx.debug";

	/**
	 * Driver parameter - Specify "true" or "false" to tell the driver whether a
	 * configuration URL should be created
	 */
	public final static String MC_CREATE = "com.ibm.di.config.interfaces.createFile";

	/**
	 * MetamergeConfigFactory parameter - Specifies the driver class name to
	 * use.
	 */
	public final static String MC_DRIVER = "com.ibm.di.config.interfaces.driver";

	/**
	 * MetamergeConfigFactory parameter - Specifies the URL
	 */
	public final static String MC_URL = Context.PROVIDER_URL;

	/**
	 * Force use of server public key encryption by setting this prop to "true".
	 * Force no encryption by setting the prop to "false". If this prop is
	 * neither "true" nor "false", the default behavior is to use encryption
	 * only when the server is running in secure mode.
	 */
	public final static String MC_ENCRYPT = "com.ibm.di.config.interfaces.serverencryption";

	/**
	 * The namespace, which a given MetamergeConfig object represents. If this
	 * parameter is not specified, the URL will be used as namespace.
	 */
	public final static String MC_NAMESPACE = "com.ibm.di.config.interfaces.namespace";

	/**
	 * Used to avoid creating default folders. For internal use.
	 */
	public final static String MC_NO_DEFAULT_FOLDERS = "com.ibm.di.config.no.default.folders";

	/**
	 * Used to override the location returned by MetamergeConfig.getDirectory()
	 */
	public final static String MC_CONFIG_DIRECTORY = "com.ibm.di.config.config.directory";

	/**
	 * This is the default driver class if none is discovered/specified.
	 */
	public final static String DEFAULT_DRIVER = "com.ibm.di.config.base.MetamergeConfigImpl";

	private static final String PROPERTIES_FILE = "miconfig";

	/**
	 * This is the Log object used by this class and other configuration
	 * drivers.
	 */
	public final static Log logger = new Log(PROPERTIES_FILE,
			"com.ibm.di.config.interfaces.MetamergeConfigFactory");

	/**
	 * This hashtable holds the registered namespaces.
	 */
	private static Map<String, MetamergeConfig> namespace = new ConcurrentHashMap<String, MetamergeConfig>();

	/**
	 * This hashtable holds the registered namespaces that are pending unload
	 * (when all refs to them are removed).
	 */
	private static Map<String, MetamergeConfig> unloadPending = new ConcurrentHashMap<String, MetamergeConfig>();

	/**
	 * This object holds the javax.naming specification for names
	 */
	private static Properties namesyntax = new Properties();

	/**
	 * This object holds the javax.naming specification for simple (flat) names
	 */
	private static Properties emptySyntax = new Properties();

	/**
	 * This object holds a list of filename/driver mappings for discovery of
	 * driver.
	 */
	private static Map<String, String> driverMap = new ConcurrentHashMap<String, String>();

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * Gets the javax.naming NameSyntax properties used in Metamerge
	 * configuration drivers.
	 * 
	 * @return The nameSyntax value
	 */
	public static Properties getNameSyntax() {
		return namesyntax;
	}

	/**
	 * Returns an instance of a MetamergeConfig driver using path as parameter
	 * 
	 * @param path
	 *            input file/parh/url ....
	 * 
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig getFileInstance(Object path) throws Exception {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.PROVIDER_URL, path);
		return getInstance(env);
	}

	/**
	 * Returns an instance of a MetamergeConfig driver using env as parameter
	 * 
	 * @param env
	 *            Table of parameters
	 * 
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig getInstance(Hashtable env) throws Exception {
		Object url = env.get(Context.PROVIDER_URL);
		String driver = (String) env.get(MC_DRIVER);
		String path = null;

		Object namespace = env.get(MC_NAMESPACE);
		if (namespace == null) {
			namespace = url;
		}

		// If already loaded then return current one
		if (namespace != null && getNamespace(namespace) != null) {
			return getNamespace(namespace);
		}

		// If driver not specified then try to figure out which one to use
		if (driver == null) {
			if (url != null && url.toString().equals("")) {
				url = null;
			}

			if (url instanceof String) {
				try {
					URL tmp = new URL(url.toString());
					url = tmp;
					// handled by URL test below
				} catch (MalformedURLException mue) {
				}
			}

			if (url instanceof String) {
				try {
					File file = new File(url.toString());
					path = file.getName();
					url = file;
				} catch (Exception error) {
				}
			}

			// Is is a URL?
			if (url instanceof URL) {
				if (((URL) url).getProtocol().equalsIgnoreCase("file")) {
					path = ((URL) url).getFile();
					if (path != null)
						driver = getDriverFromFile(new File(path), (String) env
								.get(javax.naming.Context.SECURITY_CREDENTIALS));
				} else {
					driver = driverMap.get(((URL) url).getProtocol());
				}
			}

			if (url instanceof File) {
				driver = getDriverFromFile((File) url, (String) env
						.get(javax.naming.Context.SECURITY_CREDENTIALS));
			}
		}

		if (driver == null) {
			throw new Exception(sResHash.getString(
					"MICONFIG.METAMCONFIGFACT.NO.CONFIG.DRIVER", url));
		}

		// Create MetamergeConfig instance
		Class[] params = new Class[] { Hashtable.class };
		Object[] envp = new Object[] { env };

		try {
			MetamergeConfig mc = (MetamergeConfig) Class.forName(driver)
					.getConstructor(params).newInstance(envp);
			registerNamespace(mc.toString(), mc);
			return mc;
		} catch (java.lang.reflect.InvocationTargetException ite) {
			Throwable t = ite.getTargetException();
			if (t instanceof Exception)
				throw (Exception) t;
			else
				throw ite;
		}
	}

	/**
	 * Returns the driver associated with the extension to path or the default
	 * driver if none found.
	 * 
	 * @param f
	 *            the file
	 * @param password
	 *            the pass for the file
	 * 
	 * @return The driver class name
	 */
	private static String getDriverFromFile(File f, String password)
			throws Exception {
		String path = f.getName();
		int index = path.lastIndexOf(".");
		String driver = null;
		if (index >= 0) {
			driver = driverMap.get(path.substring(index).toLowerCase(
					Locale.ENGLISH));
		} else {
			driver = driverMap.get(path);
		}

		if (driver != null) {
			return driver;
		}

		BufferedReader inp;

		if (com.ibm.di.security.EncryptedReader.isEncrypted(f)) {
			if (password == null || password.length() == 0) {
				throw new com.ibm.di.exceptions.PasswordException(
						sResHash
								.getString("MICONFIG.METAMCONFIGFACT.MISSING.PASSWORD.FOR.ENCRYPTED.FILE"));
			}

			com.ibm.di.security.SecurityCrypto key = new com.ibm.di.security.SecurityCrypto(
					password);
			com.ibm.di.security.EncryptedReader input = new com.ibm.di.security.EncryptedReader(
					new FileInputStream(f));
			input.setKey(key);
			input.prefetch();
			inp = input;
		} else {
			inp = new BufferedReader(new FileReader(f));
		}

		String line = inp.readLine();
		inp.close();

		if (line == null)
			return null;

		if (line.startsWith("["))
			return "com.ibm.di.config.base.MetamergeConfigImpl";

		if (line.startsWith("<?xml"))
			return "com.ibm.di.config.xml.MetamergeConfigXML";

		return DEFAULT_DRIVER;
	}

	/**
	 * Gets an instance of the driver for old-style config files.
	 * 
	 * @param path
	 *            CFG file/url name
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig getCFGFileInstance(Object path)
			throws Exception {
		return getCFGFileInstance(path, null);
	}

	/**
	 * Gets an instance of the driver for old-style config files
	 * 
	 * @param path
	 *            CFG file/url
	 * @param password
	 *            Password to access path
	 * 
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig getCFGFileInstance(Object path,
			String password) throws Exception {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		ht.put(Context.PROVIDER_URL, path);
		ht.put(MC_DEBUG, "false");
		if (password != null) {
			ht.put(Context.SECURITY_CREDENTIALS, password);
		}

		return new MetamergeConfigImpl(ht);
	}

	/**
	 * Gets an combined instance for a Vector of files.
	 * 
	 * @param paths
	 *            A Vector containing Strings, each representing an URL.
	 * 
	 * @return The combined MetamergeConfig object
	 */
	public static MetamergeConfig createSysInstance(Vector<String> paths) {

		MetamergeConfigXML ret;
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		ht.put(MC_NO_DEFAULT_FOLDERS, "");
		ht.put(MC_ENCRYPT, "false");
		try {
			ret = new MetamergeConfigXML(ht);
			// Better create a few folders
			ret.createFolder(MetamergeConfig.DEFAULT_ATTRIBUTEMAP_FOLDER);
			ret.createFolder(MetamergeConfig.DEFAULT_SCRIPT_FOLDER);
			ret.setModTSEnabled(false);
		} catch (Exception e) {
			logger.error("error.loading.system.templates", e);
			return null;
		}

		for (String path : paths) {
			ht = new Hashtable<String, Object>();
			try {
				ht.put(MC_DEBUG, "false");
				ht.put(MC_NO_DEFAULT_FOLDERS, "");
				ht.put(MC_ENCRYPT, "false");

				String driver;
				if (path.endsWith(".xml")) {
					driver = "com.ibm.di.config.xml.MetamergeConfigXML";
					ht.put(Context.PROVIDER_URL, new URL(path));
				} else {
					driver = DEFAULT_DRIVER;
					ht.put(Context.PROVIDER_URL, new URL(path).openStream());
				}

				Class[] params = new Class[] { Hashtable.class };
				Object[] envp = new Object[] { ht };

				MetamergeConfig add = (MetamergeConfig) Class.forName(driver)
						.getConstructor(params).newInstance(envp);
				add.setDriverParameter(Context.PROVIDER_URL, null);
				copy(add, ret, null, true);
			} catch (Exception e) {
				Throwable t = e.getCause();
				logger.error("CONFIGFACTORY.PROBLEM.PARSING", path, t != null ? t : e);
			}
		}

		return ret;
	}

	/**
	 * Returns the MetamergeConfig associated with the provided namespace.
	 * 
	 * @param name
	 *            The namespace name
	 * 
	 * @return The MetamergeConfig object or null if name was not found
	 */
	public static MetamergeConfig getNamespace(Object name) {
		if (logger.isDebugEnabled()) {
			logger.debug("MICONFIG.METAMCONFIGFACT.GETNAMESPACE", name, Boolean
					.valueOf(namespace.containsKey(name.toString())));
		}
		return namespace.get(name.toString());
	}

	/**
	 * Returns the MetamergeConfig associated with the provided namespace.
	 * 
	 * @param name
	 *            The namespace name
	 */
	public static void removeNamespace(Object name) {
		if (logger.isDebugEnabled()) {
			logger.debug("MICONFIG.METAMCONFIGFACT.REMOVENAMESPACE", name,
					Boolean.valueOf(namespace.containsKey(name.toString())));
		}
		MetamergeConfig mc = namespace.get(name.toString());
		if (mc != null) {
			namespaceChanged(name.toString(), mc, false);
			namespace.remove(name.toString());
		}
	}

	/**
	 * Returns the MetamergeConfig associated with the provided namespace. If
	 * not found, searches the referent's namespace table for a match.
	 * 
	 * @param name
	 *            The namespace name
	 * @return The MetamergeConfig object or null if name was not found
	 */
	public static MetamergeConfig getLocalNamespace(MetamergeConfig referent,
			Object name) {
		MetamergeConfig mc = namespace.get(name.toString());
		if (mc != null)
			return mc;

		try {
			MetamergeFolder nsfolder = (MetamergeFolder) referent
					.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);
			String[] names = nsfolder.getNames();
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(name.toString())) {
					return loadNamespace(referent.getNamespace(names[i]));
				}
			}
		} catch (Exception ignore) {
			ignore.printStackTrace(System.err);
		}

		return null;
	}

	/**
	 * Returns the namespace to which a configuration object belongs or null if
	 * the object does not belong to a registered namespace.
	 * 
	 * @param config
	 *            Configuration object obtained from a MetamergeConfig object
	 * @return The namespace (a String) or null if not found
	 */
	public static Object getNamespaceFor(BaseConfiguration config) {
		MetamergeConfig mc = config.getMetamergeConfig();
		if (mc == null) {
			return null;
		}

		for (java.util.Map.Entry<String, MetamergeConfig> mapEntry : namespace
				.entrySet()) {
			if (mapEntry.getValue() == mc) {
				return mapEntry.getKey();
			}
		}

		return null;
	}

	/**
	 * Returns the local namespace for a component. This method searches the
	 * referent object that owns the configuration object by looking at the
	 * referent's local namespace definitions. The returned namespace is valid
	 * only in the context of the referent.
	 * 
	 * @param referent
	 *            The MetamergeConfig object to search for local namespaces
	 * @param config
	 *            The configuration object
	 * @return The local namespace value
	 */
	public static Object getLocalNamespaceFor(MetamergeConfig referent,
			BaseConfiguration config) {

		// Return null if config object is local to referent
		if (referent != null && referent == config.getMetamergeConfig()) {
			return null;
		}

		Object ns = getNamespaceFor(config);
		if (ns == null) {
			return null;
		}

		if (referent == null) {
			return ns;
		}

		try {
			MetamergeFolder nsfolder = (MetamergeFolder) referent
					.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);
			String[] names = nsfolder.getNames();
			String namedURL = ns.toString();

			for (int i = 0; i < names.length; i++) {
				NamespaceConfig nc = referent.getNamespace(names[i]);
				String url = nc.getURL();

				if (namedURL.equals(url)
						|| namedURL.equals(new File(url).getAbsolutePath()))
					return nc.getShortName();
			}
		} catch (Exception ignore) {
		}

		return ns;
	}

	/**
	 * Returns a list of registered namespaces.
	 * 
	 * @return The namespace values
	 */
	public static Object[] getNamespaces() {
		return namespace.keySet().toArray();
	}

	/**
	 * Returns true if the name is local to the given MetamergeConfig. If not,
	 * the name refers to another MetamergeConfig which can be obtained through
	 * the lookup() method.
	 * 
	 * @param mc
	 *            A MetamergeConfig object
	 * @param name
	 *            The name of the MetamergeConfig object
	 * @return true if name is local to mc
	 * @exception Exception
	 */
	public static boolean isNameLocal(MetamergeConfig mc, Object name)
			throws Exception {
		// Make sure we have a Name object
		Name n = parseName(name);

		if (n.size() > 0) {
			return !n.get(0).endsWith(":");
		} else {
			return true;
		}
	}

	/**
	 * This method parses a name which can either be an instance of
	 * javax.naming.Name or any object implementing the toString() method. In
	 * case of a Name, the method returns a clone of name. In case of a string,
	 * the string is parsed according to the internal configuration of names. If
	 * name is null, a new javax.naming.Name object is returned.
	 * 
	 * @param name
	 *            null, javax.namgin.Name or an object having a toString()
	 *            method
	 * @return A Name instance representing the name parameter
	 * @exception InvalidNameException
	 */
	public static Name parseName(Object name) throws InvalidNameException {
		if (name instanceof Name) {
			return (Name) ((Name) name).clone();
		}

		if (name == null) {
			return new CompoundName("", namesyntax);
		}

		CompoundName cn = new CompoundName(name.toString(), namesyntax);
		if (cn.size() > 0 && cn.get(0).equals("")) {
			cn.remove(0);
		}
		return cn;
	}

	/**
	 * Creates a simple (flat) name.
	 * A CompoundName using the String is returned, with no parsing.
	 * 
	 * @param name
	 *            The name
	 * @return A Name instance representing the name parameter
	 * @exception InvalidNameException
	 * @since 7.2
	 */
	public static Name simpleName(String name) throws InvalidNameException {
		return new CompoundName(name == null ? "" : name, emptySyntax);
	}

	/**
	 * Registers a MetamergeConfig object in the global namespace.
	 * 
	 * @param name
	 *            Unique name
	 * @param mc
	 *            MetamergeConfig object
	 */
	public static void registerNamespace(Object name, MetamergeConfig mc) {
		if (logger.isDebugEnabled()) {
			logger.debug("MICONFIG.METAMCONFIGFACT.REGISTERNAMESPACE.NAME",
					name);
		}
		namespace.put(name.toString(), mc);
		namespaceChanged(name.toString(), mc, true);
	}

	/**
	 * Puts a MetamergeConfig object onto the unloadPending stack and calls
	 * garbageCollect.
	 * 
	 * @param ns
	 */
	public static synchronized void unregisterNamespace(Object ns) {
		if (ns == null)
			return;

		Object name = ns.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("MICONFIG.METAMCONFIGFACT.UNREGISTERNAMESPACE.NAME",
					name);
		}
		if (getNamespace(name) != null) {
			if (logger.isDebugEnabled()) {
				logger
						.debug(
								"MICONFIG.METAMCONFIGFACT.UNREGISTERNAMESPACE.ADD.TO.PENDING",
								name);
			}
			unloadPending.put(name.toString(), getNamespace(name));
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"MICONFIG.METAMCONFIGFACT.NOT.IN.GLOBAL.NAMESPACE",
						name);
			}
		}

		// Call garbageCollect as long as items were removed
		boolean collectedAnything;
		do {
			collectedAnything = garbageCollect();
		} while (collectedAnything);
	}

	/**
	 * Removes the MetamergeConfig objects from the global namespace if there
	 * are no more references to it.
	 */
	public static synchronized boolean garbageCollect() {
		if (logger.isDebugEnabled()) {
			logger.debug("MICONFIG.METAMCONFIGFACT.GARBAGECOLLECT");
		}
		boolean didRemove = false;
		for (String namedURL : unloadPending.keySet()) {
			logger.info("MICONFIG.METAMCONFIGFACT.NAMEDURL", namedURL);
			if (canRemoveNamespace(namedURL)) {
				if (logger.isDebugEnabled()) {
					logger
							.debug(
									"MICONFIG.METAMCONFIGFACT.GARBAGECOLLECT.REMOVE.NAMESPACE",
									namedURL);
				}
				removeNamespace(namedURL);
				unloadPending.remove(namedURL);
				didRemove = true;
			}
		}
		return didRemove;
	}

	public static boolean canRemoveNamespace(String namedURL) {

		for (String ns : namespace.keySet()) {
			if (logger.isDebugEnabled()) {
				logger.debug("MICONFIG.METAMCONFIGFACT.CANREMOVENAMESPACE", ns);
			}
			if (ns.equals(namedURL))
				continue;

			if (logger.isDebugEnabled()) {
				logger
						.debug(
								"MICONFIG.METAMCONFIGFACT.CANREMOVENAMESPACE.CHECK.INCLUDES",
								ns);
			}
			MetamergeConfig referent = getNamespace(ns);
			try {
				MetamergeFolder nsfolder = referent
						.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);

				for (String name : nsfolder.getNames()) {
					NamespaceConfig nc = referent.getNamespace(name);
					String url = nc.getURL();
					if (namedURL.equals(url)
							|| namedURL.equals(new File(url).getAbsolutePath())) {
						if (logger.isDebugEnabled()) {
							logger.debug("MICONFIG.METAMCONFIGFACT.CANREMOVENAMESPACE.NAMEDURL",
											namedURL, ns);
						}
						return false;
					}
				}
			} catch (Exception ignore) {
			}
		}
		return true;
	}

	/**
	 * Returns a MetamergeConfig object for the provided url. If the Url needs
	 * to be loaded it also registers the url/config in the global namespace
	 * table.
	 * 
	 * @param url
	 *            URL to load/locate
	 * 
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig loadNamespace(String url) throws Exception {

		// Lookup URL in our static table
		MetamergeConfig ns = getNamespace(url);
		if (ns != null) {
			return ns;
		}

		// At this point we can try to create a MetamergeConfig instance to
		// server
		// the URL. However, we only do this if we have enough information in
		// the
		// NamespaceConfig record retrieved from the caller
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.PROVIDER_URL, url);
		ns = getInstance(env);
		registerNamespace(url, ns);
		return ns;
	}

	/**
	 * Returns a MetamergeConfig object for the provided NamespaceConfig. If
	 * loading is needed, also register the url/config in the global namespace
	 * table.
	 * 
	 * @param nc
	 *            NamespaceConfig to load/locate
	 * 
	 * @return MetamergeConfig object
	 * @exception Exception
	 */
	public static MetamergeConfig loadNamespace(NamespaceConfig nc)
			throws Exception {

		if (nc == null)
			return null;

		String url = nc.getURL();
		if (url == null || url.length() == 0)
			return null;

		// Lookup URL in our static table
		MetamergeConfig ns = getNamespace(url);
		if (ns != null) {
			return ns;
		}

		// At this point we can try to create a MetamergeConfig instance to
		// server
		// the URL. However, we only do this if we have enough information in
		// the
		// NamespaceConfig record retrieved from the caller
		Hashtable<String, Object> env = new Hashtable<String, Object>();

		env.put(Context.PROVIDER_URL, url);

		Object o = nc.getParameter(Context.SECURITY_CREDENTIALS);
		if (o != null)
			env.put(Context.SECURITY_CREDENTIALS, o);

		o = nc.getDriver();
		if (o != null)
			env.put(MC_DRIVER, o);

		env.put(MC_CREATE, "false");

		ns = getInstance(env);
		registerNamespace(url, ns);
		return ns;
	}

	/**
	 * Resolves a namespace reference into a Metamergeconfig object. This method
	 * will try to create a new instance of a MetamergeConfig driver for any
	 * unresolved URLs.
	 * 
	 * @param mc
	 *            The MetamergeConfig object requesting name
	 * @param name
	 *            The name to resolve
	 * 
	 * @return The MetamergeConfig object owning name
	 * @exception Exception
	 */
	public static MetamergeConfig resolve(MetamergeConfig mc, Name name)
			throws Exception {

		// Verify that we really have a namespace reference
		String str = name.get(0);
		if (!str.endsWith(":")) {
			throw new Exception(sResHash.getString(
					"MICONFIG.METAMCONFIGFACT.NOT.A.NAMESPACE.REFERENCE", name));
		}

		// remove trailing colon
		str = str.substring(0, str.length() - 1);

		// Try simple name
		MetamergeConfig ns = getNamespace(str);
		if (ns != null) {
			return ns;
		}

		// Cannot proceed without a MetamergeConfig
		if (mc == null) {
			throw new Exception(sResHash.getString(
					"MICONFIG.METAMCONFIGFACT.UNDEFINED.NAMESPACE.REFERENCE",
					str));
		}

		// Get caller's namespace definition
		try {
			NamespaceConfig nc = mc.getNamespace(str);
			if (nc == null) {
				throw new Exception(sResHash.getString(
						"MICONFIG.METAMCONFIGFACT.UNDEFINED.NAMESPACE.REFERENCE2",
						new Object[] { str, mc }));
			}
	
			return loadNamespace(nc);
		} catch(Exception err) {
			// -- Auto load APIEngine registered configs
			if(loadRegisteredSolution(str))
				return getNamespace(str);
			
			throw err;
		}

	}
	
	/**
	 * Checks the APIEngine's 
	 */
	public static boolean loadRegisteredSolution(String ns) {
		MetamergeConfig mc = null;
		try {
			if(APIEngine.getConfigurationRegistry() != null) {
				// If we have a matching registered solution identifier try
				// to load that and register it using the solution identifier.
				String path = APIEngine.getConfigurationRegistry().getConfigFilePath(ns);
				if(path != null) {
					Hashtable<String, Object> env = new Hashtable<String, Object>();
					env.put(Context.PROVIDER_URL, path);
					mc = getInstance(env);
					registerNamespace(ns, mc);
				}
			}
		} catch(Exception err) {
			SystemFunctions.doNothing();
		}
		return mc != null;
	}

	/**
	 * Performs a lookup in the global namespace for name. The name parameter is
	 * resolved using resolve and the local part of name is then searched for in
	 * the metamergeconfig returned by resolve. This method is used by
	 * MetamergeConfig drivers when it encounters a name that is not local to
	 * itself.
	 * 
	 * @param mc
	 *            Calling MetamergeConfig
	 * @param name
	 *            The name to lookup
	 * 
	 * @return The configuration object returned by the resolved MetamergeConfig
	 *         object
	 * @exception Exception
	 */
	public static Object lookup(MetamergeConfig mc, Object name)
			throws Exception {
		// Make sure we have a Name object
		Name n = parseName(name);

		// Resolve name to MetamergeConfig instance
		MetamergeConfig nc = resolve(mc, n);

		// Remove namespace prefix and pass on the rest of the name
		return nc.lookup(n.getSuffix(1));
	}

	/**
	 * Recursively copy input configuration to another MetamergeConfig object
	 * 
	 * @param input
	 *            Object to copy
	 * @param dest
	 *            MetamergeConfig object to receive copy
	 * @param destName
	 *            Name of copied object in dest config
	 * 
	 * @exception Exception
	 */
	public static void copy(BaseConfiguration input, MetamergeConfig dest,
			Name destName) throws Exception {
		copy(input, dest, destName, false);
	}

	/**
	 * Recursively copy input configuration to another MetamergeConfig object
	 * 
	 * @param input
	 *            Object to copy
	 * @param dest
	 *            MetamergeConfig object to receive copy
	 * @param destName
	 *            Name of copied object in dest config
	 * @param overwrite
	 *            flag indicating whether to overwrite in case of copying folder
	 * @exception Exception
	 */
	public static void copy(BaseConfiguration input, MetamergeConfig dest,
			Name destName, boolean overwrite) throws Exception {
		if (destName == null) {
			destName = parseName("");
		}

		BaseConfiguration cc;
		if (input instanceof MetamergeConfig) {
			cc = input;
		} else {
			cc = (BaseConfiguration) input.getClone();
			cc.setMetamergeConfig(dest);
			cc.setName(destName);
		}

		if (input instanceof MetamergeFolder
				&& !(input instanceof PropertyManager)) {
			copyFolder((MetamergeFolder) cc, dest, destName, overwrite);
		} else {
			if (overwrite)
				dest.rebind(destName, cc);
			else
				dest.bind(destName, cc);
		}
	}

	/**
	 * Recursively copies a folder to another MetamergeConfig object. Existing
	 * objects are overwritten.
	 * 
	 * @param folder
	 *            The folder to copy
	 * @param dest
	 *            The config that receives the copy
	 * @param destName
	 *            The name the copy gets
	 * @exception Exception
	 */
	public static void copyFolder(MetamergeFolder folder, MetamergeConfig dest,
			Name destName) throws Exception {
		copyFolder(folder, dest, destName, true);
	}

	/**
	 * Recursively copies a folder to another MetamergeConfig object. Existing
	 * objects are overwritten.
	 * 
	 * @param folder
	 *            The folder to copy
	 * @param dest
	 *            The config that receives the copy
	 * @param destName
	 *            The name the copy gets
	 * @param overwrite
	 *            flag indicating whether to overwrite
	 * 
	 * @exception Exception
	 */
	public static void copyFolder(MetamergeFolder folder, MetamergeConfig dest,
			Name destName, boolean overwrite) throws Exception {

		if (destName.size() > 0) {
			// Make sure the folder exists
			try {
				dest.lookup(destName);
			} catch (Throwable nnfe) {
				dest.createFolder(destName);
			}
		}

		Enumeration<Binding> enum1 = folder.list();
		while (enum1.hasMoreElements()) {
			Binding b = enum1.nextElement();
			if (!(b.getObject() instanceof BaseConfiguration))
				continue;

			BaseConfiguration c = (BaseConfiguration) b.getObject();

			Name name = (Name) destName.clone();
			name.add(c.getShortName());

			if (!isStandardObject(name) || c instanceof InstanceConfig
					|| c instanceof LogConfig || c instanceof PropertyManager
					|| c instanceof SolutionInterface) {
				// vishakha - for cut/copy/paste if dont overwite
				if (!overwrite) {
					try {
						if (dest.lookup(c.getName()) != null)
							continue;
					} catch (NameNotFoundException e) {
					} catch (NamingException e2) {
					}
				}
				// end
				// Make sure we don't overwrite PropertyStores with null data
				if (c instanceof PropertyManager) {
					ContainerConfig stores = ((PropertyManager) c)
							.getPropertyStores();
					if (stores == null || stores.size() == 0)
						continue;
				}
				if (c instanceof InstanceConfig) {
					ContainerConfig items = ((InstanceConfig) c)
							.getStartupItems();
					if (items == null || items.size() == 0)
						continue;
				}
				if (c instanceof LogConfig) {
					List items = ((LogConfig) c).getItems();
					if (items == null || items.size() == 0)
						continue;
				}
				if (c instanceof SolutionInterface) {
					SolutionInterface sol = (SolutionInterface) c;
					ContainerConfig c1 = sol.getExposedAssemblyLines();
					ContainerConfig c2 = sol.getExposedProperties();
					if ((c1 == null || c1.size() == 0)
							&& (c2 == null || c2.size() == 0)
							&& (sol.getHealthAssemblyLine() == null)
							&& (sol.getInstanceID() == null))
						continue;
				}

				c = (BaseConfiguration) c.getClone();
				c.setMetamergeConfig(dest);
				c.setName(name);
				dest.rebind(name, c);
			} else if (!(c instanceof MetamergeFolder)) {
				try {
					copyObject(c, (BaseConfiguration) dest.lookup(c.getName()));
				} catch (NameNotFoundException nn) {
				}
			}

			if (c instanceof MetamergeFolder && !(c instanceof PropertyManager)) {
				copyFolder((MetamergeFolder) c, dest, name, overwrite);
			}
		}
	}

	/**
	 * Recursively copies an object to another MetamergeConfig object. Existing
	 * objects are overwritten.
	 * 
	 * @param source
	 *            The object to copy
	 * @param dest
	 *            The config that receives the copy
	 */
	public static void copyObject(BaseConfiguration source,
			BaseConfiguration dest) {

		// Special case for external properties
		if (source instanceof ExternalPropertiesConfig) {
			((ExternalPropertiesConfig) dest)
					.setFilePath(((ExternalPropertiesConfig) source)
							.getFilePath());
			((ExternalPropertiesConfig) dest)
					.setEncrypted(((ExternalPropertiesConfig) source)
							.getEncrypted());
			try {
				((ExternalPropertiesConfig) dest).loadData();
			} catch (Exception ignore) {
			}
			return;
		}

		for (String key : source.getKeys(BaseConfiguration.ONE_LEVEL)) {
			dest.setParameter(key, source.getParameter(key));
		}

		for (String key : source.getKeys(BaseConfiguration.SUBTREE)) {
			dest.setParameter(key, source.getParameter(key));
		}
	}

	/**
	 * Convenience method to log a message to the system log.
	 * 
	 * @param msg
	 *            The message to log
	 */
	public static void logmsg(String msg) {
		logger.info(msg);
	}

	/**
	 * Initialization of namesyntax and drivermap.
	 */
	static {
		namesyntax.put("jndi.syntax.direction", "left_to_right");
		namesyntax.put("jndi.syntax.separator", "/");
		namesyntax.put("jndi.syntax.ignorecase", "true");
		namesyntax.put("jndi.syntax.escape", "\\");
		namesyntax.put("jndi.syntax.trimblanks", "true");
		namesyntax.put("jndi.syntax.separator.ava", ",");
		namesyntax.put("jndi.syntax.separator.typeval", "=");

		driverMap.put(".cfg", "com.ibm.di.config.base.MetamergeConfigImpl");
		driverMap.put(".xml", "com.ibm.di.config.xml.MetamergeConfigXML");
		driverMap.put("ldap",
				"com.ibm.di.config.interfaces.jndi.MetamergeConfigJNDI");
	}

	/**
	 * Verify that inheritance chain does not loop.
	 */
	public static void verifyInheritanceChain(BaseConfiguration config,
			Object inheritFrom) throws Exception {
		if (inheritFrom == null)
			return;

		Name source = config.getName();
		if (source == null)
			return;

		Name inherit = parseName(inheritFrom);

		Vector<Name> v = new Vector<Name>();

		if (source.equals(inherit)) {
			v.add(inherit);
			throw new InheritanceLoopException(source, v);
		}

		// Parent/None cannot create loop since child entries cannot be
		// inherited from)
		if (inheritFrom.equals(BaseConfiguration.INHERIT_NONE)
				|| inheritFrom.equals(BaseConfiguration.INHERIT_PARENT))
			return;

		// Standalone config file has only one object
		if (config.getMetamergeConfig() == null)
			return;

		BaseConfiguration bc;
		try {
			bc = (BaseConfiguration) config.getMetamergeConfig().lookup(
					inherit.toString());
		} catch (NameNotFoundException nfe) {
			return;
		} catch (FileNotFoundException fnfe) {
			logger.error(fnfe.toString(), fnfe);
			return;
		}

		while (bc != null) {
			v.add(parseName(bc.getName()));
			if (config == bc)
				throw new InheritanceLoopException(source, v);

			bc = bc.getInheritsFrom();
		}
	}

	/**
	 * Returns true if the object name represents a standard object in the
	 * configuration space.
	 */
	public static boolean isStandardObject(Object objname) {
		if (objname == null)
			return false;

		try {
			Name name = parseName(objname);
			if (name.size() <= 1)
				return true;
			if (name.size() == 2) {
				String folder = name.get(0);
				String def = name.get(1);

				if ((MetamergeConfig.DEFAULT_EXTPROP_FOLDER.equals(folder))
						&& (MetamergeConfig.DEFAULT_EXTPROP_NAME.equals(def))) {
					return true;
				}

				if ((MetamergeConfig.DEFAULT_SERVER_FOLDER.equals(folder))
						&& (MetamergeConfig.DEFAULT_SERVER_LOG.equals(def)
								|| MetamergeConfig.DEFAULT_SERVER_TOMBSTONES
										.equals(def)
								|| MetamergeConfig.DEFAULT_SOLUTION_INTERFACE
										.equals(def) || MetamergeConfig.DEFAULT_SERVER_AUTOSTART
								.equals(def))) {
					return true;
				}
			}
		} catch (Exception ignore) {
		}

		return false;
	}

	/**
	 * Adds a list of packages to the packages namespace. Returns the packageid
	 * or exception object for each package.
	 * 
	 * @param packages
	 *            List of absolute paths
	 */
	public static List<String> addPackages(List<String> packages) {
		ArrayList<String> result = new ArrayList<String>();
		for (String name : packages) {
			try {
				result.add(addPackage(name));
			} catch (Exception err) {
				result.add(err.toString());
			}
		}
		return result;
	}

	/**
	 * Removes a package from the in-memory list of packages. Also, the
	 * auto-generated adapters are removed from the adapters namespace.
	 * 
	 * @param mc
	 *            The package to remove
	 * @return true the package was removed, false if the config is not a
	 *         package
	 */
	public static boolean removePackage(MetamergeConfig mc) throws Exception {
		if (!mc.getBooleanParameter("%%package%%", false))
			return false;

		String id = ((ContainerConfig) mc.lookup("Package")).getConfig("Info")
				.getStringParameter("packageid");
		removeNamespace(id);

		MetamergeConfig ads = getNamespace(ADAPTERS_NAMESPACE);
		if (ads == null)
			return true;

		String[] names = ads.getDefaultFolder(MetamergeConfig.CONNECTOR_FOLDER)
				.getNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(id + "."))
				ads.unbind("/" + MetamergeConfig.DEFAULT_CONNECTOR_FOLDER + "/"
						+ names[i]);
		}

		return true;
	}

	/**
	 * Adds a package to the packages namespace. Returns the package ID on
	 * success.
	 * 
	 * @param path
	 *            The absolute path of the file
	 */
	public static String addPackage(String path) throws Exception {
		MetamergeConfig mc = null;
		try {
			mc = getFileInstance(path);
			ContainerConfig cc = null;
			if ((cc = (ContainerConfig) mc.lookup("Package")) == null) {
				return "[not a packaged config file]";
			}

			String pkg = cc.getConfig("Info").getStringParameter("packageid");
			if (pkg == null) {
				throw new Exception(
						sResHash
								.getString("MICONFIG.METAMCONFIGFACT.PACKAGE.HAS.NO.INFO.PACKAGEID.NODE"));
			}

			mc.setBooleanParameter("%%package%%", true);
			if (getNamespace(pkg) != null)
				return "[duplicate package id: " + pkg + " in file " + path
						+ "]";

			registerNamespace(pkg, mc);

			addAdapters(pkg, mc);

			logger.info("MICONFIG.METAMCONFIGFACT.REGISTERED.PACKAGE", pkg);

			return pkg;

		} catch (javax.naming.NameNotFoundException ignore) {
			return "[not a packaged config file]";
		} finally {
			unregisterNamespace(mc);
		}
	}

	private static void addAdapters(String id, MetamergeConfig mc) {
		try {
			MetamergeConfig ads = getNamespace(ADAPTERS_NAMESPACE);
			if (ads == null) {
				ads = new MetamergeConfigXML();
				ads.initializeConfig();
				registerNamespace(ADAPTERS_NAMESPACE, ads);
			}
			String[] names = mc.getDefaultFolder(
					MetamergeConfig.ASSEMBLYLINE_FOLDER).getNames();
			for (int i = 0; i < names.length; i++) {
				ConnectorConfig cc = (ConnectorConfig) ads
						.newInstanceOf(MetamergeConfig.CONNECTOR_FOLDER);
				cc.setName("/Connectors/" + id + "." + names[i]);
				cc
						.setInheritsFromRef("system:/Connectors/ibmdi.AssemblyLineConnector");
				cc.getConnectionConfig().setStringParameter("assemblyLine",
						id + ":/AssemblyLines/" + names[i]);
				cc.getConnectionConfig().setInheritsFromRef(
						BaseConfiguration.INHERIT_PARENT);
				cc.getSchema(ConnectorConfig.SCHEMA_INPUT).setInheritsFromRef(
						BaseConfiguration.INHERIT_PARENT);
				cc.getSchema(ConnectorConfig.SCHEMA_OUTPUT).setInheritsFromRef(
						BaseConfiguration.INHERIT_PARENT);
				cc.setupInheritanceChain();
				ads.rebind(cc.getName(), cc);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * Returns a list of packaged config files
	 */
	public static List<MetamergeConfig> getPackages() throws Exception {
		List<MetamergeConfig> result = new ArrayList<MetamergeConfig>();
		for (MetamergeConfig mc : namespace.values()) {
			if (mc.getBooleanParameter("%%package%%", false))
				result.add(mc);
		}
		if (getNamespace(ADAPTERS_NAMESPACE) != null)
			result.add(getNamespace(ADAPTERS_NAMESPACE));
		return result;
	}

	/**
	 * This method does nothing.
	 * @deprecated
	 */
	public static void addNamespaceListener(ActionListener listener) {
	}

	/**
	 * This method does nothing.
	 * @deprecated
	 */
	public static void removeNamespaceListener(ActionListener listener) {
	}

	/**
	 * This method does nothing.
	 * @deprecated
	 */
	public static void namespaceChanged(String ns,
			MetamergeConfig mc, boolean added) {
	}
	
	/**
	 * Enable/disable the use of configuration listeners in the JVM. This is an
	 * optimization to turn off listeners which the CE uses, but the Server does
	 * not need.
	 * 
	 * @param value
	 *            False to disable configuration listeners.
	 * @since 7.0
	 */
	public static void setUseConfigListeners(boolean value) {
		BaseConfigurationImpl.setUseConfigListeners(value);
	}

	/**
	 * @return Whether configuration listeners are enabled or disabled in this
	 *         JVM.
	 * @see #setUseConfigListeners(boolean)
	 * @since 7.0
	 */
	public static boolean getUseConfigListeners() {
		return BaseConfigurationImpl.getUseConfigListeners();
	}

}
