/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.loader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This is class is responsible for setting up any environment system
 * properties, as well as creating the class loaders hierarchy for loading all
 * TDI jars. This is the entry point for the class loading process along with
 * starting the TDI Server.
 * 
 * @since 7.1
 */
public class ServerLauncher {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the JAR file that contains all class loading-related classes.
	 */
	private static final String BOOT_JAR_NAME = "IDILoader.jar";

	/**
	 * TDI Server entry point. This is the class that should be called in order
	 * to run the TDI Server.
	 */
	private static final String SERVER_CLASS_NAME = "com.ibm.di.server.RS";

	/**
	 * Logs debug messages
	 */
	private static Logger logger;

	/**
	 * Formats strings for debugging.
	 */
	private static IDILoaderLogsFormatter msgRes;

	/**
	 * This is the main method that is called when this class is run.
	 * 
	 * @param args
	 *            the arguments that are passed to the
	 *            <code>com.ibm.di.server.RS</code>.
	 * @throws Exception
	 *             - if a value without corresponding parameter name is
	 *             encountered
	 */
	public static void main(String[] args) throws Exception {
		ClassLoader bootLoader = initClassLoader();
		Thread.currentThread().setContextClassLoader(bootLoader);
		startServer(bootLoader, args);
	}

	/**
	 * Initialize the TDI class loader that loads all TDI jars.
	 * 
	 * @return A new instance of ClassLoader which loads all TDI jars..
	 * @throws IOException
	 *             If any IO problems appear.
	 */
	public static ClassLoader initClassLoader() throws IOException {
		setEnvironmentDetailsAsSystemProperties();
		initializeLoggers();

		File tdiInstallDir = new File(System.getProperty("com.ibm.di.installdir"));
		logger.debug(msgRes.getString("IDILoader.jars.directory", tdiInstallDir.getAbsolutePath()));

		TDIJarsLocator urlLoc = new TDIJarsLocator(tdiInstallDir);
		ClassLoader bootLoader = createClassLoaderHierarchy(urlLoc);

		// previously the IDILoader was going through all the jars to search for
		// components definitions but since it is not dealing with class loading
		// any more we need to provide the jars externally.
		keepBackwardCompatibilityOfTheOldIDILoader(urlLoc);

		urlLoc = null;

		return bootLoader;
	}

	/**
	 * Creates the class loading hierarchy. The IDILoader is the bottom loader.
	 * Its parents are as follows:<br>
	 * Custom Jars Loader -> Patch Jars Loader -> Components Jars Loader ->
	 * Third Party Loader -> User Jars Loader.
	 * 
	 * @param urlLoc
	 *            jars locator class that provides methods for obtaining the
	 *            URLs of different TDI jars.
	 * @return the bottom class loader in the hierarchy.
	 */
	private static ClassLoader createClassLoaderHierarchy(TDIJarsLocator urlLoc) {
		URL[] allJars = new URL[urlLoc.getPatchesJars().length + urlLoc.getComponentsJars().length + urlLoc.getCustomJars().length
				+ urlLoc.getThirdPartyJars().length];

		System.arraycopy(urlLoc.getPatchesJars(), 0, allJars, 0, urlLoc.getPatchesJars().length);
		System.arraycopy(urlLoc.getComponentsJars(), 0, allJars, urlLoc.getPatchesJars().length, urlLoc.getComponentsJars().length);
		System.arraycopy(urlLoc.getCustomJars(), 0, allJars, urlLoc.getPatchesJars().length + urlLoc.getComponentsJars().length,
				urlLoc.getCustomJars().length);
		System.arraycopy(urlLoc.getThirdPartyJars(), 0, allJars, urlLoc.getPatchesJars().length + urlLoc.getComponentsJars().length
				+ urlLoc.getCustomJars().length, urlLoc.getThirdPartyJars().length);

		IDILoader bottomCL = new IDILoader(allJars, ServerLauncher.class.getClassLoader());

		// the bottom-most is the one loading the whole TDI
		return bottomCL;
	}

	/**
	 * Keep the backward compatibility of the old TDI Class loader. All TDI
	 * pre-7.0 components are collected here.
	 * 
	 * @param urlLoc
	 *            jars locator class that provides methods for obtaining the
	 *            URLs of different TDI jars.
	 */
	private static void keepBackwardCompatibilityOfTheOldIDILoader(TDIJarsLocator urlLoc) {
		IDILoader.scanForTdiComponents(urlLoc.getComponentsJars());
		IDILoader.scanForTdiComponents(urlLoc.getCustomJars());
		IDILoader.scanForTdiComponents(urlLoc.getPatchesJars());

		IDILoader.getInstance().addPackages(new File("packages"));
		IDILoader.getInstance().addPackages(new File(System.getProperty("com.ibm.di.installdir"), "packages"));
	}

	/**
	 * This method starts the TDI Server.
	 * 
	 * @param bootLoader
	 *            The loader that will load the
	 *            <code>com.ibm.di.server.RS</code> class.
	 * @param args
	 *            The arguments that are passed to the
	 *            <code>com.ibm.di.server.RS</code> class
	 */
	private static void startServer(ClassLoader bootLoader, String[] args) {
		try {
			logger.debug(msgRes.getString("IDILoader.run.load.initial.class", SERVER_CLASS_NAME));
			Class<?> serverClass = bootLoader.loadClass(SERVER_CLASS_NAME);

			logger.debug(msgRes.getString("IDILoader.run.get.main.method", SERVER_CLASS_NAME));
			Method serverMainMethod = serverClass.getMethod("main", String[].class);

			logger.debug(msgRes.getString("IDILoader.run.invoke.main.method", SERVER_CLASS_NAME));
			serverMainMethod.invoke(null, new Object[] { args });
		} catch (Exception ex) {
			logger.error(msgRes.getString("IDILoader.run.error"), ex);
		}
	}

	/**
	 * This method searches for the directory where TDI was installed. When this
	 * directory is found, a few Java system properties are set so that the
	 * location of the JARS and LIBS TDI folders are available when needed.
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private static void setEnvironmentDetailsAsSystemProperties() {
		String installDir = System.getProperty("com.ibm.di.installdir");
		if (installDir == null || installDir.trim().length() == 0) {
			installDir = getTdiInstallDir();
		}

		File installDirFile = new File(installDir);
		try {
			installDir = installDirFile.getCanonicalPath();
		} catch (Exception e) {
			installDir = installDirFile.getAbsolutePath();
		}
		System.setProperty("com.ibm.di.installdir", installDir);

		setLog4jConfigPath(installDirFile);

		if (System.getProperty("jlog.configuration") == null) {
			System.setProperty("jlog.configuration", installDir + "/etc/jlog.properties");
		}

		File libsDirFile = new File(installDir, "libs");

		System.setProperty("IDILoader.jars", "/" + installDirFile.getAbsolutePath());
		System.setProperty("IDILoader.libs", "/" + libsDirFile.getAbsolutePath());
	}

	/**
	 * If the System property log4j2.configurationFile is null, this probably means 
	 * that we are starting the Config Editor, and the property will be set accordingly.
	 * If the  property  begins with file:etc and points to a non-existing file,
	 * it will be changed to the install directory. 
	 * This works around a solution directory problem, we cannot use files in
	 * solution directory before they have been created.
	 * @param installDirFile 
	 */
	private static void setLog4jConfigPath(File installDirFile ) {
		String prop = System.getProperty("log4j2.configurationFile");
		try {
			if (prop == null) {
				System.setProperty("log4j2.configurationFile", installDirFile.toURI().toURL() + "/etc/ce-log4j2.xml");
				return;
			}
			
			// Handle both relative format (file:etc/...) and absolute format (file:/path/to/etc/...)
			if (prop.startsWith("file:") && !prop.startsWith("file:///")) {
				// Relative path format: file:etc/...
				if (prop.startsWith("file:etc")) {
					File f = new File(prop.substring(5));
					//ISDISUP-121
					if (!f.exists())
						System.setProperty("log4j2.configurationFile", installDirFile.toURI().toURL() + prop.substring(5));
					else{
						String solDir = System.getenv("TDI_SOLDIR");
						if (solDir.startsWith("."))
							solDir = System.getProperty("user.dir");
						File solDirFile = new File(solDir);
						System.setProperty("log4j2.configurationFile", solDirFile.toURI().toURL() + prop.substring(5));
					}
				}
			}
			// If it's already an absolute file:/// URI, leave it as is
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determines TDI install directory.
	 * 
	 * @return TDI install directory
	 */
	private static String getTdiInstallDir() {
		String resource = "/" + ServerLauncher.class.getCanonicalName().replace('.', '/') + ".class";

		URL bootJarUrl = ServerLauncher.class.getResource(resource);
		if (bootJarUrl == null) {
			System.err.println(msgRes.getString("IDILoader.no.install.dir"));
			System.exit(1);
		}

		String jarPath = bootJarUrl.getFile();
		jarPath = jarPath.substring(0, jarPath.length() - (resource.length() + 1));

		try {
			jarPath = URLDecoder.decode(jarPath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			e.printStackTrace();
			System.exit(1);
		}

		String installDir = jarPath.substring(5, jarPath.lastIndexOf(BOOT_JAR_NAME) - 1);

		File installDirFile = new File(installDir);
		try {
			installDir = installDirFile.getCanonicalPath();
		} catch (Exception e) {
			installDir = installDirFile.getAbsolutePath();
		}

		return installDir;
	}

	/**
	 * Initializes all logging-related objects.
	 */
	private static void initializeLoggers() {
		msgRes = new IDILoaderLogsFormatter();
		logger = LogManager.getLogger("com.ibm.di.loader.ServerLauncher");
	}

	/**
	 * Shutdown all servers.
	 */
	public static void shutdown() {
		try {
			Class<?> cls = IDILoader.getInstance().loadClass(SERVER_CLASS_NAME);
			Method m = cls.getMethod("shutdownAllServers", new Class[] { int.class, boolean.class, boolean.class });
			m.invoke(null, new Object[] { 0, true, true });

			// When ibmdiservice.exe shuts down the JVM shutdown hooks
			// are not invoked, so we need to manually execute the program
			// specified in the jvm.shutdown.hook property
			String externalApp = System.getProperty("jvm.shutdown.hook");
			if (externalApp != null && externalApp.trim().length() > 0) {
				Runtime.getRuntime().exec(externalApp);
			}

		} catch (Exception ignore) {
			ignore.printStackTrace();
			// There is no way an Exception is thrown because shutdownAllServers
			// method is present at the RS class and properly invoked
		}
	}

}
