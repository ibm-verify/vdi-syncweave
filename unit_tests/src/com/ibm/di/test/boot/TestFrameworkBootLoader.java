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

		// Add spring-core/spring-web/spring-webmvc BEFORE the recursive jars/
		// scan so that the correct Spring version takes priority over the older
		// one bundled inside activemq-all.jar (which lacks MimeType copy-constructor).
		// spring-core must come first since spring-web's MediaType extends MimeType.
		try {
			for (String jar : new String[] { "spring-core.jar", "spring-web.jar", "spring-webmvc.jar" }) {
				File f = new File(tdiInstallDir, "jars/" + jar);
				if (f.exists()) {
					urls.add(f.toURI().toURL());
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// get the jars from the tdi's "jars" folder; exclude:
		//   jsr311-api.jar    - JAX-RS 1.x, conflicts with JAX-RS 2.x (jersey)
		//   jaxb-impl-2.3.0.1 - uses sun.misc.Unsafe.defineClass removed in Java 11+;
		//                       replaced by jaxb-runtime-2.3.6.jar staged separately
		//   spring-core/web/webmvc - already added above with priority ordering
		File tdiJarsDir = new File(tdiInstallDir, "jars");
		collectJars(tdiJarsDir, urls, new String[] { "jsr311-api", "jaxb-impl", "spring-core", "spring-web", "spring-webmvc" });

		// get the jars from the plugins' "jars" folder
		File plnsJarsDir = new File(tdiInstallDir, "pwd_plugins/jars");
		collectJars(plnsJarsDir, urls, null);

		/*
		 * get the plugins from the TP server as simple jars; exclude the
		 * Equinox and OSGi jars, and Wink JAX-RS jars (added back after Jersey
		 * so that Jersey's RuntimeDelegate takes priority over Wink's 1.x one).
		 */
		File tpJarsDir = new File(tdiInstallDir, "osgi/plugins");
		collectJars(tpJarsDir, urls, new String[] { "org.eclipse.equinox", "org.eclipse.osgi", "org.eclipse.update",
				"ibm-wink-jaxrs", "org.apache.wink.common" });

		// Add Wink common jar AFTER Jersey so Jersey's RuntimeDelegate wins;
		// the jar is still needed for TP server type dependencies (CollectionCategories, AtomText).
		try {
			File winkCommon = new File(tdiInstallDir, "osgi/plugins/org.apache.wink.common.jar");
			if (winkCommon.exists()) {
				urls.add(winkCommon.toURI().toURL());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

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
