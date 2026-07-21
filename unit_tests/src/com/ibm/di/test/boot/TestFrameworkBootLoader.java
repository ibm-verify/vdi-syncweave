package com.ibm.di.test.boot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author kaloyan.kolev
 */
public class TestFrameworkBootLoader extends URLClassLoader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Create a {@link ClassLoader} which first searches its internal classpath
	 * before giving the control to its parent classloader. This is required for
	 * the cases when the shipped jars are duplicate with the ones from the JRE.
	 * 
	 * @param urls
	 * @param parent
	 */
	public TestFrameworkBootLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		String tdiInstallDir = System.getProperty("com.ibm.di.installdir");

		if (tdiInstallDir == null || tdiInstallDir.trim().length() == 0) {
			throw new IllegalArgumentException("Missing Java Property: com.ibm.di.installdir");
		}

		String testRunner = System.getProperty("com.ibm.di.test.runner");

		if (testRunner == null || testRunner.trim().length() == 0) {
			throw new IllegalArgumentException("Missing Java Property: com.ibm.di.test.runner");
		}

		URL[] classPath = getTdiClassPath(new File(tdiInstallDir).getCanonicalPath());
		URLClassLoader testLoader = new TestFrameworkBootLoader(classPath, TestFrameworkBootLoader.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(testLoader);

		Class<?> runnerClass = testLoader.loadClass(testRunner);
		Method mainMethod = runnerClass.getMethod("main", String[].class);
		mainMethod.invoke(null, new Object[] { args });
	}

	private static final URL[] getTdiClassPath(String tdiInstallDir) {
		List<URL> urls = new LinkedList<URL>();

		// add resources to the classpath
		try {
			urls.add(new File("resources").toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// add the idiloader
		try {
			urls.add(new File(tdiInstallDir, "IDILoader.jar").toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// get the jars from the tdi's "jars" folder
		File tdiJarsDir = new File(tdiInstallDir, "jars");
		collectJars(tdiJarsDir, urls, null);

		// get the jars from the plugins' "jars" folder
		File plnsJarsDir = new File(tdiInstallDir, "pwd_plugins/jars");
		collectJars(plnsJarsDir, urls, null);

		/*
		 * get the plugins from the TP server as simple jars; exclude the
		 * Equinox and OSGi jars
		 */
		File tpJarsDir = new File(tdiInstallDir, "osgi/plugins");
		collectJars(tpJarsDir, urls, new String[] { "org.eclipse.equinox", "org.eclipse.osgi", "org.eclipse.update" });

		// get the jars from the unit_tests' "jars" folder
		File utJarsDir = new File("lib");
		collectJars(utJarsDir, urls, new String[] { "classes" });

		URL[] result = new URL[urls.size()];
		return urls.toArray(result);
	}

	private static final void collectJars(File container, List<URL> urls, String[] ignoreNames) {
		File[] children = container.listFiles();
		try {
			if (children != null) {
				for (File child : children) {
					if (child.isDirectory()) {
						urls.add(child.toURI().toURL());
						collectJars(child, urls, ignoreNames);
					} else {
						String name = child.getName().toLowerCase();
						if ((name.endsWith(".jar") || name.endsWith(".zip")) && !nameContainsString(name, ignoreNames)) {
							urls.add(child.toURI().toURL());
						}
					}
				}
			}
		} catch (MalformedURLException ue) {
			// this should never occur because we know we are dealing with
			// existing files and we are not creating the uri manually.
			ue.printStackTrace();
		}
	}

	private static final boolean nameContainsString(String name, String[] ignoreNames) {
		if (ignoreNames != null) {
			for (String ignoreName : ignoreNames) {
				if (name.indexOf(ignoreName) != -1) {
					return true;
				}
			}
		}

		return false;
	}
}
