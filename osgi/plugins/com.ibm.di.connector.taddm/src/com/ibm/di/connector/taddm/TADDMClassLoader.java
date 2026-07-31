/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class TADDMClassLoader extends ClassLoader {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final Map<ClassPath, ClassLoader> loaders = new HashMap<ClassPath, ClassLoader>();

	private final ClassLoader delegate;

	/**
	 * @param parentLoader
	 * @throws URISyntaxException
	 *             thrown if any of the specified URLs are not complying with
	 *             the URI syntax
	 */
	public TADDMClassLoader(final URL[] taddmJars, final ClassLoader parentLoader) throws URISyntaxException {
		if (taddmJars == null || taddmJars.length == 0) {
			throw new IllegalArgumentException("taddmJars");
		}

		ClassPath cp = new ClassPath(taddmJars);
		synchronized (loaders) {
			ClassLoader ldr = loaders.get(cp);
			if (ldr == null) {
				ldr = AccessController.doPrivileged(new PrivilegedAction<ChildFirstLoader>() {
					public ChildFirstLoader run() {
						return new ChildFirstLoader(taddmJars, parentLoader);
					}
				});
				loaders.put(cp, ldr);
			}
			delegate = ldr;
		}
	}

	private static class ClassPath {

		private final String[] classpath;

		private final int hashcode;

		public ClassPath(URL[] taddmJars) throws URISyntaxException {
			String[] normalizedURIs = new String[taddmJars.length];
			for (int i = 0; i < taddmJars.length; i++) {
				normalizedURIs[i] = taddmJars[i].toURI().normalize().toString();
			}

			Arrays.sort(normalizedURIs, new Comparator<String>() {
				public int compare(String u1, String u2) {
					String u1Str = u1;
					String u2Str = u2;
					return u1Str.compareTo(u2Str);
				}
			});
			classpath = normalizedURIs;

			int hc = 0;
			for (String s : classpath) {
				hc += s.hashCode();
			}
			hashcode = hc;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return hashcode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof ClassPath && classpath.length == ((ClassPath) o).classpath.length) {
				for (int i = 0; i < classpath.length; i++) {
					if (!classpath[i].equals(((ClassPath) o).classpath[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return delegate.loadClass(className);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#getResource(java.lang.String)
	 */
	@Override
	public URL getResource(String resName) {
		return delegate.getResource(resName);
	}

	private static class ChildFirstLoader extends URLClassLoader {

		public ChildFirstLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
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
					if (getParent() != null) {
						clazz = getParent().loadClass(name);
					}
				}
			}

			if (resolve) {
				resolveClass(clazz);
			}

			return clazz;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.net.URLClassLoader#findClass(java.lang.String)
		 */
		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			// Classes that are loaded by parent classloader and are also
			// present on this classspace are problematic. Here is why we need
			// to skip them and rely on parent classes only. We know for sure
			// that the IDILoader is containing DOM interfaces (and probably an
			// implementation), and also the Entry model is implementing those
			// interfaces. In order to avoid conflicts while working with
			// Entries just skip loading of the DOM interfaces in the child
			// class (the implementation is ok, though).
			if (name.startsWith("org.w3c.dom.") && name.indexOf('.', "org.w3c.dom.".length()) == -1) {
				throw new ClassNotFoundException(name);
			}
			if (name.startsWith("org.apache.log4j"))
				throw new ClassNotFoundException(name);
			if (name.startsWith("org.apache.logging.log4j"))
				throw new ClassNotFoundException(name);

			return super.findClass(name);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public URL getResource(String name) {
			URL resource = findResource(name);

			if (resource == null && getParent() != null) {
				resource = getParent().getResource(name);
			}
			return resource;
		}
	}
}
