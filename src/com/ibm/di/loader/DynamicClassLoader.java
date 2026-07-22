/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This is a URL Class Loader that dynamically loads TDI Jars files and
 * resources from the URLs specified. The difference with the pre-TDI 7.1 class
 * loader is that this loader first lets the child find the specified class and
 * then refer to its parent.
 * 
 * @since 7.1
 */
public class DynamicClassLoader extends URLClassLoader {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Logs debug messages
	 */
	private Logger logger;

	/**
	 * Formats strings for debugging.
	 */
	private IDILoaderLogsFormatter msgRes;

	/**
	 * Constructor
	 * 
	 * @param urls
	 *            the URLs from which to load classes and resources
	 */
	public DynamicClassLoader(URL[] urls) {
		super(urls);
		initializeLoggers();
	}

	/**
	 * Constructor
	 * 
	 * @param urls
	 *            the URLs from which to load classes and resources
	 * @param parent
	 *            The parent class loader.
	 */
	public DynamicClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		initializeLoggers();
	}

	/**
	 * Constructor
	 * 
	 * @param urls
	 *            the URLs from which to load classes and resources
	 * @param parent
	 *            The parent class loader.
	 * @param factory
	 *            the URLStreamHandlerFactory to use when creating URLs
	 */
	public DynamicClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
		initializeLoggers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		Class<?> clazz = findLoadedClass(name);

		if (clazz == null) {
			try {
				clazz = findClass(name);
			} catch (ClassNotFoundException cnfe) {
				try {
					if (getParent() != null) {
						clazz = getParent().loadClass(name);
					}
				} catch (NoClassDefFoundError e) {
					e.printStackTrace();
				}
			}
		}

		if (resolve) {
			resolveClass(clazz);
		}

		return clazz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getResource(String name) {
		logger.debug(msgRes.getString("IDILoader.findResource", name));
		URL resource = findResource(name);

		if (resource == null && getParent() != null) {
			resource = getParent().getResource(name);
		}

		if (resource != null) {
			logger.debug(msgRes.getString("IDILoader.findResource.jar.URL", resource.toString()));
		} else {
			logger.debug(msgRes.getString("IDILoader.findResource.resource.not.found", name));
		}

		return resource;
	}

	/**
	 * Search for Jar files in the specified directory. Sub-directories are
	 * considered, too.
	 * 
	 * @param searchDir
	 *            directory to search at.
	 * @param bufferList
	 *            List to update with URLs of the Jars
	 */
	protected static void scanDirectoryForJarsRecursively(File searchDir, List<URL> bufferList) {
		File[] children = searchDir.listFiles();

		if (children != null) {
			try {
				for (File child : children) {
					if (child.isFile() && child.canRead()) {
						String childName = child.getName().toLowerCase();
						if (childName.endsWith(".jar") || childName.endsWith(".zip")) {
							bufferList.add(child.toURI().toURL());
						}
					} else if (child.isDirectory()) {
						bufferList.add(child.toURI().toURL());
						scanDirectoryForJarsRecursively(child, bufferList);
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes all logging-related objects.
	 */
	private void initializeLoggers() {
		msgRes = new IDILoaderLogsFormatter();
		logger = LogManager.getLogger("com.ibm.di.loader.DynamicClassLoader");
	}
}
