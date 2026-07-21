/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible for locating TDI Jars URLs. Jars are grouped in
 * four categories:
 * <p>
 * 1. Third Party Jars<br>
 * 2. Components Jars<br>
 * 3. Patch Jars<br>
 * 4. Custom Jars
 * </p>
 * 
 * @since 7.1
 */
public class TDIJarsLocator {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Array of URLs to all third party Jars. These Jars are located at
	 * <code>TDI_install_dir/jars/3rdparty</code>
	 */
	private final URL[] thirdPartyJars;

	/**
	 * Array of URLs to all components Jars. These Jars are located at
	 * <p>
	 * <code>TDI_install_dir/jars/common</code><br>
	 * <code>TDI_install_dir/jars/connectors</code><br>
	 * <code>TDI_install_dir/jars/parsers</code><br>
	 * <code>TDI_install_dir/jars/functions</code><br>
	 * <code>TDI_install_dir/jars/plugins</code>
	 * </p>
	 */
	private final URL[] componentsJars;

	/**
	 * Array of URLs to all patches Jars. These Jars are located at
	 * <code>TDI_install_dir/jars/patches</code>
	 */
	private final URL[] patchesJars;

	/**
	 * Array of URLs to all custom Jars.
	 */
	private final URL[] customJars;

	/**
	 * Constructor.
	 * 
	 * @param tdiInstallDir
	 *            File Object to TDI install directory.
	 * @throws IOException
	 *             If some IO problems with files occur.
	 */
	public TDIJarsLocator(File tdiInstallDir) throws IOException {
		List<URL> thirdPartyJars = new LinkedList<URL>();
		List<URL> componentsJars = new LinkedList<URL>();
		List<URL> patchesJars = new LinkedList<URL>();
		List<URL> customJars = new LinkedList<URL>();

		// the jars directory
		File jarsDir = new File(tdiInstallDir, "jars");

		// the jars/3rdparty directory
		File thirdPartyDir = new File(jarsDir.getCanonicalPath(), "3rdparty");

		// the jars/<tdiComponents> directories
		File[] componentsDirs = { new File(jarsDir, "common"), new File(jarsDir, "connectors"), new File(jarsDir, "functions"),
				new File(jarsDir, "parsers"), new File(jarsDir, "plugins") };

		// the jars/patches directory
		File patchDir = new File(jarsDir, "patches");

		// all of the above directories
		List<File> excludeDirs = new ArrayList<File>(2 + componentsDirs.length);
		excludeDirs.add(thirdPartyDir);
		excludeDirs.add(patchDir);
		for (File cd : componentsDirs) {
			excludeDirs.add(cd);
		}

		// gather the URLs
		scanDirectoryForJarsRecursively(thirdPartyDir, thirdPartyJars, null);
		for (File componentDir : componentsDirs) {
			scanDirectoryForJarsRecursively(componentDir, componentsJars, null);
		}
		scanDirectoryForJarsRecursively(patchDir, patchesJars, null);
		scanDirectoryForJarsRecursively(jarsDir, customJars, excludeDirs);

		this.thirdPartyJars = toArray(thirdPartyJars);
		this.componentsJars = toArray(componentsJars);
		this.patchesJars = toArray(patchesJars);
		this.customJars = toArray(customJars);
	}

	/**
	 * Transform a list of URLs to an Array of URLs.
	 * 
	 * @param lst
	 *            List of URLs to take values from.
	 * @return Array of URLs corresponding to the values in the input List.
	 */
	private URL[] toArray(List<URL> lst) {
		URL[] result = new URL[lst.size()];
		return lst.toArray(result);
	}

	/**
	 * Scan a specified directory for Jars. All Jar file in the specified
	 * directory are scanned as well as those in the sub-directories.
	 * 
	 * @param searchDir
	 *            Directory to search at.
	 * @param bufferList
	 *            List of URLs that will be updated with the URLs of the newly
	 *            founded Jars files.
	 * @param excludeDirs
	 *            Directories to exclude while searching.
	 */
	private static void scanDirectoryForJarsRecursively(File searchDir, List<URL> bufferList, List<File> excludeDirs) {
		File[] children = searchDir.listFiles();

		if (children != null) {
			try {
				for (File child : children) {
					if (child.isFile() && child.canRead()) {
						String childName = child.getName().toLowerCase();
						if (childName.endsWith(".jar") || childName.endsWith(".zip")) {
							bufferList.add(child.toURI().toURL());
						}
					} else if (child.isDirectory() && !isOneOf(child, excludeDirs)) {
						bufferList.add(child.toURI().toURL());
						scanDirectoryForJarsRecursively(child, bufferList, null);
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 *Determine of the specified child is in the list of the specified exclude
	 * directories. If so, the file in the exclFude directories is removed.
	 * 
	 * @param child
	 *            File that will be checked if it is in the exclude directories
	 *            list.
	 * @param excludeDirs
	 *            Exclude directories list.
	 * @return <code>True</code> if the child is in the exclude directories list<br>
	 *         <code>False</code> if the child is not found in the exclude
	 *         directories list.
	 */
	private static boolean isOneOf(File child, List<File> excludeDirs) {
		if (excludeDirs != null) {
			for (int i = 0; i < excludeDirs.size(); i++) {
				if (child.equals(excludeDirs.get(i))) {
					excludeDirs.remove(i);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the URLs to all third party Jars.
	 * 
	 * @return Array of URLs to all third party Jars
	 */
	public URL[] getThirdPartyJars() {
		return thirdPartyJars;
	}

	/**
	 * Get the URLs of all component Jars.
	 * 
	 * @return Array of URLs to all component Jars
	 */
	public URL[] getComponentsJars() {
		return componentsJars;
	}

	/**
	 * Get the URLs to all patched Jars
	 * 
	 * @return Array of URLs to all patched JarsF
	 */
	public URL[] getPatchesJars() {
		return patchesJars;
	}

	/**
	 * Get the URLs to all custom Jars
	 * 
	 * @return Array of URLs to all custom Jars
	 */
	public URL[] getCustomJars() {
		return customJars;
	}
}
