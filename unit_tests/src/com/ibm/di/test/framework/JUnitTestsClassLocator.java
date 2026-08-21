/**
 * 
 */
package com.ibm.di.test.framework;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author kaloyan.kolev
 * 
 */
public class JUnitTestsClassLocator {

	enum Postfixes {
		CVT, ExtPerf, IntPerf, Test, Unknown;
		
		public String toString() {
			return super.toString() + ".class";
		}
	}

	private static final String SEARCH_PACKAGE_AS_DIR = "com/ibm/di";
	private static final String SEARCH_PACKAGE = SEARCH_PACKAGE_AS_DIR.replace('/', '.');

	private Class<?>[] classesCache;
	private Postfixes cachedClassesPostfix = Postfixes.Unknown;

	public JUnitTestsClassLocator() {
	}

	public Class<?>[] getExtPerfClasses() {
		return getClassesWithPostfix(Postfixes.ExtPerf);
	}
	
	public Class<?>[] getIntPerfClasses() {
		return getClassesWithPostfix(Postfixes.IntPerf);
	}
	
	public Class<?>[] getTestClasses() {
		return getClassesWithPostfix(Postfixes.Test);
	}
	
	public Class<?>[] getCVTClasses() {
		return getClassesWithPostfix(Postfixes.CVT);
	}
	
	private Class<?>[] getClassesWithPostfix(Postfixes postfix) {
		if (cachedClassesPostfix != postfix) {
			classesCache = findJUnitClasses(postfix.toString());
			cachedClassesPostfix = postfix;
		}
		return classesCache;
	}

	private Class<?>[] findJUnitClasses(String postfix) {
		List<Class<?>> classes = new LinkedList<Class<?>>();
		URL[] classPath = null;
		if (JUnitTestsClassLocator.class.getClassLoader() instanceof URLClassLoader) {
			classPath = ((URLClassLoader) JUnitTestsClassLocator.class.getClassLoader()).getURLs();
			for (URL cp : classPath) {
				if (cp.toExternalForm().endsWith("/")) {
					try {
						File f = new File(cp.toURI());
						File p = null;
						if (f.exists()) {
							p = new File(f, SEARCH_PACKAGE_AS_DIR);
						}

						if (p != null) {
							collectClassesFromDirectory(p, SEARCH_PACKAGE, classes, postfix);
						}
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				} else if (cp.toExternalForm().toLowerCase().endsWith(".jar")) {
					JarFile jar = null;
					try {
						jar = new JarFile(new File(cp.toURI()));
						collectClassesFromZip(jar, classes, postfix);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					} finally {
						if (jar != null) {
							try {
								jar.close();
							} catch (IOException e) {
							}
						}
					}
				} else if (cp.toExternalForm().toLowerCase().endsWith(".zip")) {
					ZipFile zip = null;
					try {
						zip = new ZipFile(new File(cp.toURI()));
						collectClassesFromZip(zip, classes, postfix);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					} finally {
						if (zip != null) {
							try {
								zip.close();
							} catch (IOException e) {
							}
						}
					}
				}
			}
		}

		Class<?>[] result = new Class[classes.size()];

		return classes.toArray(result);
	}

	/**
	 * Returns true if the class can be run as a JUnit test (i.e. it is a
	 * concrete class, not an interface or annotation).
	 */
	private static boolean isRunnableTestClass(Class<?> clazz) {
		return !clazz.isInterface() && !clazz.isAnnotation()
				&& !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
	}

	private void collectClassesFromDirectory(File dir, String currentPack, List<Class<?>> classes, String postfix) {
		File[] children = dir.listFiles();

		if (children != null) {
			for (File child : children) {
				if (child.isFile() && child.getName().endsWith(postfix)) {
					try {
						String className = currentPack + "." + removeClassSuffix(child.getName());
						Class<?> clazz = JUnitTestsClassLocator.class.getClassLoader().loadClass(className);
						if (isRunnableTestClass(clazz)) {
							classes.add(clazz);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				} else if (child.isDirectory()) {
					collectClassesFromDirectory(child, currentPack + "." + child.getName(), classes, postfix);
				}
			}
		}
	}

	private void collectClassesFromZip(ZipFile zip, List<Class<?>> classes, String postfix) {
		Enumeration<? extends ZipEntry> entries = zip.entries();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.startsWith(SEARCH_PACKAGE_AS_DIR) && name.endsWith(postfix)) {
				try {
					Class<?> clazz = JUnitTestsClassLocator.class.getClassLoader().loadClass(
							name.substring(0, name.length() - 6).replace('/', '.'));
					if (isRunnableTestClass(clazz)) {
						classes.add(clazz);
					}
				} catch (ClassNotFoundException e) {
					// ignore...
					e.printStackTrace();
				} catch (java.lang.UnsatisfiedLinkError e) {
					// ignore...
					e.printStackTrace();
				} catch (java.lang.NoClassDefFoundError e) {
					// ignore...
					e.printStackTrace();
				}
			}
		}
	}
	
	private String removeClassSuffix(String fileName) {
		int suffixStart = fileName.lastIndexOf(".class");
		if (suffixStart != -1) {
			return fileName.substring(0, suffixStart);
		} else {
			return fileName;
		}
	}
	
}
