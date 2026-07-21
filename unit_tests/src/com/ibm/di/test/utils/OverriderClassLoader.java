package com.ibm.di.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This loader makes sure that any class (specified during construction) will be
 * reloaded from the provided classpath and resolved again. This will ensure
 * that the class loaded by the parent is overridden. Any other class not
 * specified for overriding will be first loaded from this loader's classpath
 * and if not found the control will be given to the parent loader to try to
 * find the class.
 */
public class OverriderClassLoader extends URLClassLoader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private List<String> overridesClasses;

	public OverriderClassLoader(URL[] urls, ClassLoader parent, String[] overridesClasses) {
		super(urls, parent);
		if (overridesClasses == null) {
			this.overridesClasses = new ArrayList<String>(0);
		} else {
			this.overridesClasses = Arrays.asList(overridesClasses);
		}
	}

	@Override
	public URL getResource(String name) {
		// let the child classloader be the first one searching for the
		// resource.
		URL resource = findResource(name);
		if (resource == null) {
			// check the parent's classpath
			ClassLoader parent = getParent();
			if (parent != null) {
				resource = parent.getResource(name);
			}
		}

		return resource;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		InputStream is = null;
		Class<?> clazz = null;

		clazz = findLoadedClass(name);
		if (clazz == null) {
			// see if the child has the class defined
			try {
				if (overridesClasses.contains(name)) {
					// we want these two classes to be loaded (and resolved) by
					// this
					// class loader and not by the parent.
					is = getResourceAsStream(name.replace('.', '/') + ".class");
				}

				if (is != null) {
					// ok so we are re-defining classes here...

					// read the class byte representation
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					int len = -1;
					byte[] temp = new byte[2048];
					try {
						while ((len = is.read(temp)) != -1) {
							out.write(temp, 0, len);
						}
					} catch (IOException e) {
						throw new ClassNotFoundException(e.getLocalizedMessage(), e);
					}
					// calling define creates a new Class instance with this
					// classloader set to the Class' field. The resolving of
					// the class will also be done using this class loader
					// Doing this will successfully redefine the Entry class
					// from 7.0 with the one from 6.1.1 for example.
					clazz = defineClass(name, out.toByteArray(), 0, out.size());
				} else {
					// we are not redefining... just load the classes from
					// this loader's
					clazz = findClass(name);
				}
			} catch (ClassNotFoundException e) {
				// no class... ask the parent...
				ClassLoader parent = getParent();
				if (parent != null) {
					clazz = parent.loadClass(name);
				}
			} finally {
				if (is != null) {
					try {
						is.close();
						is = null;
					} catch (IOException e) {
					}
				}
			}
		}

		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}
}
