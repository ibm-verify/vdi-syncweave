/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class is responsible for loading classes from the jar files that are
 * placed in the jars directory
 */
public class IDILoader extends URLClassLoader {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Vector of all installed configs.
	 */
	private static Vector<String> installedConfigs = new Vector<String>();

	/**
	 * Logs debug messages.
	 */
	private Logger logger = LogManager.getLogger(IDILoader.class);

	/**
	 * Names of all the folders in the Jars Directory of TDI.
	 */
	private String[] defaultNames = { "patches", "common", "connectors", "functions", "parsers", "plugins" };

	/**
	 * Used to track where the custom classes has been loaded from
	 */
	private Map<String, Class<?>> classFileCache = new HashMap<String, Class<?>>();

	/**
	 * This the list of installed packages. It contains a list of
	 * MetamergeConfig objects.
	 */
	private static List<String> packages = new ArrayList<String>();

	/**
	 * This is the first (and only) instance that is created
	 */
	private static IDILoader instance = null;

	/**
	 * Formats strings for debugging messages
	 */
	private static IDILoaderLogsFormatter msgRes;

	/**
	 * Default constructor for the IDILoader object
	 */
	public IDILoader() {
		this(null, IDILoader.class.getClassLoader());
	}

	/**
	 * General purpose constructor for the IDILoader object
	 * 
	 * @param parent
	 *            The parent class loader.
	 */
	IDILoader(URL[] urls, ClassLoader parent) {
		super(urls == null ? new URL[0] : urls, parent);
		if (instance == null) {
			instance = this;
		}
		msgRes = new IDILoaderLogsFormatter();
	}

	/**
	 * Provides the IDILoader with the jars so it can go through each one of
	 * them and index the .xml and .ini files for further requests by the
	 * server.
	 */
	static void scanForTdiComponents(URL[] tdiClassPath) {
		String[] preferredNames = createPreferredNames();

		for (URL url : tdiClassPath) {
			String file = url.toExternalForm();
			if (!file.endsWith("/")) {
				getInstance().addInstalledComponent(url, preferredNames);
			}
		}
	}

	/**
	 * Create an array with Locale specific tdi.xml and idi.inf Strings.
	 * @return
	 */
	private static String[] createPreferredNames() {

		ArrayList<String> prefNames = new ArrayList<String>();

		addToList(prefNames, "tdi", ".xml" );
		addToList(prefNames, "idi", ".inf" );

		return prefNames.toArray(new String[prefNames.size()]);
	}
	
	private static void addToList(List<String> list, String prefix, String suffix) {
		Locale locale = Locale.getDefault();

		if (locale != null) {
			String lang = locale.getLanguage();
			String country = locale.getCountry();
			if (!"".equals(lang)) {
				if (!"".equals(country)) {
					list.add(prefix + "_" + lang + "_" + country + suffix);
				}
				list.add( prefix + "_" + lang + ".xml");
			}
		}
		list.add(prefix + suffix);	
	}
	

	/**
	 * Returns a Vector with all the idi.inf files.
	 * 
	 * @return Vector containing Strings, each String is the URL for an idi.inf
	 *         file.
	 * @since 7.0
	 */
	public static Vector<String> getAllSysConfigs() {
		return installedConfigs;
	}

	/**
	 * Returns the time a class was modified
	 * 
	 * @param className
	 *            the name of the class
	 * @return The modification time as a String or null if not found
	 */
	public static String getModificationDate(String className) {
		long date = -1;
		String result = "";

		String path = getInstance().getPathForClass(className);

		if (path != null) {
			if (path.endsWith(".jar") || path.endsWith(".zip")) {
				ZipFile zf = null;
				try {
					zf = new ZipFile(path);
					date = zf.getEntry(className.replace(".", "/") + ".class").getTime();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (zf != null) {
						try {
							zf.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			} else {
				// plain file
				File f = new File(path);
				date = f.lastModified();
			}
		}

		if (date != -1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			result = formatter.format(new Date(date));
		}

		return result;
	}

	/**
	 * Provide a list of all installed packages.
	 * 
	 * @return the list of installed packages
	 */
	public static List<String> getInstalledPackages() {
		return packages;
	}

	/**
	 * Provides a path to a specified class. This is the Jar file where the
	 * specified class is located.
	 * 
	 * @param clsname
	 *            the name of the class which path to get
	 * @return the path from which a class is/will be loaded.
	 * 
	 */
	public String getPathForClass(String clsname) {
		clsname = clsname.replace('.', '/') + ".class";
		URL url = getResourceURL(clsname);

		String result = null;

		if (url != null) {
			if ("jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol())) {
				// now the url should look like a
				// jar:file:/path/to/archive.jar!/path/to/file.class
				String j = url.getFile();
				result = j.substring(0, j.lastIndexOf(clsname, j.length() - clsname.length()) - 2
				/*
				 * "!/" are used to separate the jar file and the class file.
				 */);
			} else if ("file".equals(url.getProtocol())) {
				// now the url should look like a
				// file:/class/path/to/package/of/file.class
				String f = url.toString();
				result = f.substring(0, f.lastIndexOf(clsname, f.length() - clsname.length()));
			}

			if (result != null && result.startsWith("file:")) {
				result = result.substring(5 /* "file:".length() */);
			}

			if (result != null) {
				try {
					result = URLDecoder.decode(result, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else {
			// try some of the files
			for (Entry<String, Class<?>> entry : classFileCache.entrySet()) {
				if (entry.getValue().getCanonicalName().equals(clsname)) {
					result = entry.getKey();
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Add all files in the given path to our internal list of jar files
	 * 
	 * @param path
	 *            The path of the file(s) to be added
	 */
	public void addFiles(String path) {

		logger.debug(msgRes.getString("IDILoader.addFiles", path));

		File f = new File(path);
		String absolutePath = f.getAbsolutePath();

		if (!f.exists()) {
			logger.debug(msgRes.getString("IDILoader.addFiles.path.not.exist", path));
			return;
		}

		if (!f.isDirectory()) {
			if (path.toLowerCase(Locale.ENGLISH).endsWith(".jar") || path.toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
				try {
					addURL(f.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				addInstalledComponent(absolutePath);
				addInstalledXMLComponent(absolutePath);
				// }
			} else if (path.toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
				packages.add(absolutePath);
			} else {
				logger.debug(msgRes.getString("IDILoader.addFiles.path.not.jar", path));
			}
			return;
		}

		String[] str = f.list();

		if (str == null) {
			logger.info(msgRes.getString("IDILoader.addFiles.no.files.found", absolutePath));
			return;
		}

		logger.debug(msgRes.getString("IDILoader.addFiles.adding.files", absolutePath));

		Vector<String> list = new Vector<String>(Arrays.asList(str));
		for (int i = 0; i < defaultNames.length; i++) {
			if (list.remove(defaultNames[i]))
				addFiles(absolutePath + File.separator + defaultNames[i]);
		}

		for (int i = 0; i < list.size(); i++)
			addFiles(absolutePath + File.separator + list.get(i));
	}

	/**
	 * Add all files in the given path to our internal list of packages
	 * 
	 * @param path
	 *            The path of the file(s) to be added
	 */
	public void addPackages(File path) {

		logger.debug(msgRes.getString("IDILoader.addPackages", path));

		if (!path.exists()) {
			logger.debug(msgRes.getString("IDILoader.addPackages.path.not.exist", path));
			return;
		}

		if (!path.isDirectory()) {
			if (path.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
				packages.add(path.getAbsolutePath());
			}
			return;
		}

		File[] str = path.listFiles();

		if (str == null) {
			logger.info(msgRes.getString("IDILoader.addPackages.no.files.found", path.getAbsolutePath()));
			return;
		}

		logger.debug(msgRes.getString("IDILoader.addPackages.adding.files", path.getAbsolutePath()));
		for (int i = 0; i < str.length; i++)
			addPackages(str[i]);
	}

	/**
	 * Locate idi.inf in a jar file, and add it to the installedConfigs
	 * 
	 * @param path
	 *            The absolute path of the jar file
	 */
	public void addInstalledComponent(String path) {

		JarFile jar = null;
		try {
			jar = new JarFile(path);
		} catch (IOException e) {
			logger.info(msgRes.getString("IDILoader.addInstalledComponent.exception", new Object[] { path, e.toString() }));
			return;
		}

		if (logger.isDebugEnabled())
			logger.debug(msgRes.getString("IDILoader.addInstalledComponent.looking", path ));
		
		try {
			ZipEntry entry = null;
			Locale locale = Locale.getDefault();

			if (locale != null) {
				String lang = locale.getLanguage();
				String country = locale.getCountry();
				entry = jar.getEntry("idi_" + lang + "_" + country + ".inf");
				if (entry == null)
					entry = jar.getEntry("idi_" + lang + ".inf");
			}
			if (entry == null)
				entry = jar.getEntry("idi.inf");
			if (entry != null) {
				if (logger.isDebugEnabled())
					logger.debug(msgRes.getString("IDILoader.addInstalledComponent.found", 
							new Object[] {entry.getName(), path } ));			
			    installedConfigs.add("jar:file:///" + path + "!/" + entry.getName());
			}

		} catch (Exception error) {
			logger.info(msgRes.getString("IDILoader.addInstalledComponent.exception", new Object[] { path, error.toString() }));
		} finally {
			try {
				jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Locate tdi.xml in a jar file, and add it to the sysconfigs Vector
	 * 
	 * @param path
	 *            The absolute path of the jar file
	 */
	public void addInstalledXMLComponent(String path) {

		JarFile jar = null;
		try {
			jar = new JarFile(path);
		} catch (IOException e) {
			logger.warn(msgRes.getString("IDILoader.addInstalledComponent.exception", new Object[] { path, e.toString() }));
			return;
		}

		try {
			ZipEntry entry = null;
			Locale locale = Locale.getDefault();

			if (locale != null) {
				String lang = locale.getLanguage();
				String country = locale.getCountry();
				entry = jar.getEntry("tdi_" + lang + "_" + country + ".xml");
				if (entry == null)
					entry = jar.getEntry("tdi_" + lang + ".xml");
			}
			if (entry == null)
				entry = jar.getEntry("tdi.xml");

			if (entry != null) {
				if (logger.isDebugEnabled())
					logger.debug(msgRes.getString("IDILoader.addInstalledComponent.found", 
							new Object[] {entry.getName(), path } ));			
				installedConfigs.add("jar:file:///" + path + "!/" + entry.getName());
			}
		} catch (Exception error) {
			logger.info(msgRes.getString("IDILoader.addInstalledComponent.exception", new Object[] { path, error.toString() }));
		} finally {
			try {
				jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Locate tdi.xml or idi.inf in a jar file, and add it to the installedConfigs.
	 * 
	 * @param path
	 *            URL of the jar file
	 * @param preferredNames the ordered set of names to search for.
	 */
	private void addInstalledComponent(URL path, String[] preferredNames) {
		ZipInputStream jar = getZipInputStreamByURL(path);
		if (jar == null) {
			return;
		}

		if (logger.isDebugEnabled())
			logger.debug(msgRes.getString("IDILoader.addInstalledComponent.looking", path ));
		
		try {
			String entryName = getZipEntry(jar, preferredNames);

			if (entryName != null) {
				if (logger.isDebugEnabled())
					logger.debug(msgRes.getString("IDILoader.addInstalledComponent.found", new Object[] {entryName, path } ));
				
				installedConfigs.add("jar:file://" + path.getPath() + "!/" + entryName);
			}
		} catch (Exception error) {
			logger.info(msgRes.getString("IDILoader.addInstalledComponent.exception", new Object[] { path, error.toString() }));
		} finally {
			try {
				jar.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Provides a ZipInputStream by a specified URL of a Jar File.
	 * 
	 * @param path
	 *            URL of the Jar file
	 * @return ZipInputStream of the Jar file
	 */
	private ZipInputStream getZipInputStreamByURL(URL path) {
		ZipInputStream jar = null;
		try {
			InputStream is = path.openStream();
			jar = new ZipInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jar;
	}

	/**
	 * Finds one of the names in the zip stream. The names are provided as an
	 * ordered set of non-null strings.
	 * 
	 * @param jis
	 *            ZipInputStream in which to search the entry
	 * @param prefNames
	 *            the ordered set of names to search for.
	 * @return the found name. The search completes after the most preferable
	 *         name is found or the stream is exhausted. In case that the entry
	 *         is not found <code>null</code> is returned
	 * @throws IOException
	 *             if IO problems occur
	 */
	private String getZipEntry(ZipInputStream zis, String[] prefNames) throws IOException {
		ZipEntry entry = null;

		int prefPos = prefNames.length;

		while (prefPos > 0 && (entry = zis.getNextEntry()) != null) {
			String name = entry.getName();
			for (int i = 0; i < prefPos; i++) {
				if (name.equals(prefNames[i]))
					prefPos = i;
			}
		}

		return prefPos >= prefNames.length ? null : prefNames[prefPos];
	}

	/**
	 * Provide an instance of IDILoader.
	 * 
	 * @return the first instance of IDILoader that was created.
	 * 
	 * @since 7.0
	 */
	public static IDILoader getInstance() {
		if (instance == null) {
			/*
			 * FindBugs advises: [DP] Classloaders should only be created inside
			 * doPrivileged block [DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED]
			 * This code creates a classloader, which requires a security
			 * manager. If this code will be granted security permissions, but
			 * might be invoked by code that does not have security permissions,
			 * then the classloader creation needs to occur inside a
			 * doPrivileged block.
			 */
			instance = AccessController.doPrivileged(new PrivilegedAction<IDILoader>() {
				public IDILoader run() {
					return new IDILoader();
				}
			});
		}
		return instance;
	}

	/**
	 * Load a Class from a .class file Try to avoid duplicate defining of
	 * classes by keeping a local cache, which maps from fileName to class.
	 * 
	 * @param fileName
	 *            - The name of the .class file
	 * @return - The loaded class
	 * @throws Exception
	 *             if an internal error occurs.
	 */
	public Class<?> loadClassFromFile(String fileName) throws Exception {
		fileName = new File(fileName).getCanonicalPath();
		Class<?> c = classFileCache.get(fileName);
		if (c != null)
			return c;

		FileInputStream fis = new FileInputStream(fileName);
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];

			int rc;
			while ((rc = fis.read(b)) != -1)
				bos.write(b, 0, rc);

			b = bos.toByteArray();
			c = defineClass(null, b, 0, b.length);
		} finally {
			fis.close();
		}
		classFileCache.put(fileName, c);
		return c;
	}

	/**
	 * Locate all classes in a jar file, and define the classes found.
	 * 
	 * @param path
	 *            The absolute path of the jar file
	 */
	public Class<?> loadClassFromFile(String path, String className) {
		// if this class loader has already loaded that class don't define it
		// again
		Class<?> result = findLoadedClass(className);

		if (result == null) {
			JarFile jf = null;
			InputStream is = null;
			try {
				jf = new JarFile(path);

				ZipEntry entry = jf.getEntry(className + ".class");
				if (entry != null) {
					is = jf.getInputStream(entry);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int rc;
					while ((rc = is.read(b)) != -1) {
						bos.write(b, 0, rc);
					}

					byte[] bytes = bos.toByteArray();
					result = defineClass(null, bytes, 0, bytes.length);
				}
			} catch (Exception error) {
				logger.info(msgRes.getString("IDILoader.cacheJarContents.error", new Object[] { path, error }));
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (jf != null) {
					try {
						jf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return result;
	}

	/**
	 * Finds a resource URL by its name. The lookup is made by using
	 * {@link #getResourceFromClassLoaderChain(String)} method. If the name does
	 * not start with a leading slash and the resource was not found the first
	 * time another try will be made by prefixing the name with a forward slash.
	 * 
	 * If the resource exists and an URL is obtained it is then resolved using
	 * the {@link #resolveLocalURL(URL)} method.
	 * 
	 * Note we cannot use the ResourceLocator class here because it is not on the classpath of the IDILoader!
	 * 
	 * @param resourceName
	 *            the name to look for.
	 * @return the URL of the resource.
	 */
	private URL getResourceURL(String resourceName) {
		URL url = getResourceFromClassLoaderChain(resourceName);

		if (url == null && !resourceName.startsWith("/")) {
			// when inside OSGi context the resource should be an absolute URI
			url = getResourceFromClassLoaderChain("/" + resourceName);
		}

		if (url != null && !"file".equals(url.getProtocol()) && !"jar".equals(url.getProtocol())
				&& !"zip".equals(url.getProtocol())) {
			url = resolveLocalURL(url);
		}

		return url;
	}

	/**
	 * This method looks for a resource using the ClassLoaders chain. The first
	 * classloader that is asked is the context loader, then the loader that has
	 * loaded the ResourceLocator class and finally the system loader.
	 * 
	 * @param resource
	 *            the name of the resource to look for.
	 * @return the url to the resource or <code>null</code> if not found.
	 */
	private URL getResourceFromClassLoaderChain(String resource) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL result = null;
		if (cl != null) {
			result = cl.getResource(resource);
		}

		if (result == null && cl != this) {
			cl = this;
			result = cl.getResource(resource);
		}

		if (result == null) {
			cl = ClassLoader.getSystemClassLoader();
			result = cl.getResource(resource);
		}

		return result;
	}

	/**
	 * If we are inside an OSGi context the URL will not be a file but a
	 * bundleresource. Use this method to convert that URL to a file. If the url
	 * is already a File or the OSGi context is not present this method will not
	 * perform anything and the provided URL will be returned. The access to the
	 * eclipse code is done through reflection so it is safe to include this
	 * code in non-osgi context.
	 * 
	 * @param resource
	 *            the URL to convert
	 * @return a connection to the jar file containing the passes resource.
	 * @throws IOException
	 *             if there is a problem to connect to the jar file.
	 */
	private URL resolveLocalURL(URL resource) {
		if (resource != null) {
			try {
				// convert an eclipse resource URL to a normal URL
				Class<?> fileLocatorClass = Class.forName("org.eclipse.core.runtime.FileLocator");
				Method getUrlMethod = fileLocatorClass.getMethod("resolve", new Class[] { URL.class });
				resource = (URL) getUrlMethod.invoke(null, new Object[] { resource });
			} catch (ClassNotFoundException cnfe) {
				return resource;
			} catch (NoClassDefFoundError e) {
				return resource;
			} catch (SecurityException e) {
				return resource;
			} catch (NoSuchMethodException e) {
				return resource;
			} catch (IllegalArgumentException e) {
				return resource;
			} catch (IllegalAccessException e) {
				return resource;
			} catch (InvocationTargetException e) {
				return resource;
			}
		}
		return resource;
	}
}
