/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.osgi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.ibm.di.api.remote.impl.BindAddressPolicyImpl;
import com.ibm.di.api.remote.impl.rmi.Constants;
import com.ibm.di.loader.DynamicClassLoader;
import com.ibm.di.server.BindAddressPolicy;
import com.ibm.di.util.FileUtils;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class is used to start the OSGi Framework. The class expects that the
 * OSGi Framework is located in the "osgi" folder under the TDI install dir. The
 * configuration area will be based off of the TdI install dir and the instance
 * area will based off of the Solution Directory to keep the configuration
 * independent for each instance. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class OSGiLauncher {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** White-Space delimiter characters */
	private static final String WS_DELIM = " \t\n\r\f";

	/** OSGi Framework Bundle name without the versioning part */
	private static final String FRAMEWORK_BUNDLE_NAME = "org.eclipse.osgi";

	/** The class used to start the Equinox OSGi Framework */
	private static final String STARTER = "org.eclipse.core.runtime.adaptor.EclipseStarter";

	/** keyword for marking that the particular property should be nullified */
	private static final String NULL_IDENTIFIER = "@null";

	/** The key to the property specifying the OSGi Framework */
	private static final String OSGI_FRAMEWORK = "osgi.framework";

	/** The key to the property specifying the workspace place */
	private static final String OSGI_INSTANCE_AREA = "osgi.instance.area";

	/** The key to the property specifying the configuration place */
	private static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";

	/** The key to the property specifying the osgi installation place */
	private static final String OSGI_INSTALL_AREA = "osgi.install.area";

	/** The file to look for that contains the launching properties */
	private static final String LAUNCH_INI = "launch.ini";

	/** The file to look for that contains the command line parameters */
	private static final String CONSOLE_INI = "console.ini";

	/** The key to the property specifying the location where TDI is installed */
	private static final String TDI_INSTALL_DIR = "com.ibm.di.installdir";

	/** The sub-directory where the framework is located */
	private static final String OSGI = "osgi";

	private static final Logger logger = LogManager.getLogger(OSGiLauncher.class);

	/** this holds the "<tdi_install_dir>/osgi" location */
	private File platformDirectory;

	/** this is the OSGi Framework ClassLoader which we started it with. */
	private ClassLoader frameworkClassLoader;

	/** the commandline parameters */
	private String[] args;

	// instances populated after container startup
	private Object systemBundleContext;

	/**
	 * Creates an instance for starting up the OSGi Framework.
	 */
	OSGiLauncher() {
		platformDirectory = new File(System.getProperty(TDI_INSTALL_DIR), OSGI);
		init();
	}

	/**
	 * Initializes the instance by reading the configuration properties and the
	 * launch parameters. This parameter reads the configuration files and set
	 * the parameters as System.Properties
	 */
	private synchronized void init() {
		readLaunchProperties();
		interpretLocationProperties();
		interpretHttpProperties();
		args = buildCommandLineArguments();
	}

	/**
	 * Starts the OSGi Framework. The class must be initialized before the
	 * calling this method.
	 */
	public synchronized void start() {
		if (frameworkClassLoader != null) {
			logger.warn("Framework is already started");
			return;
		}

		// store it in case the FWK switch the context loader.
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			final URL osgiURLArray[] = { new URL((String) System.getProperty(OSGI_FRAMEWORK)) };
			frameworkClassLoader = AccessController.doPrivileged(new PrivilegedAction<DynamicClassLoader>() {
				public DynamicClassLoader run() {
					return new DynamicClassLoader(osgiURLArray, getClass().getClassLoader());
				}
			});

			Class<?> clazz = frameworkClassLoader.loadClass(STARTER);

			// start the framework.
			Method setInitialProperties = clazz.getMethod("setInitialProperties", new Class<?>[] { java.util.Map.class });
			setInitialProperties.invoke(null, new Object[] { System.getProperties() });
			Method runMethod = clazz.getMethod("startup", new Class[] { java.lang.String[].class, java.lang.Runnable.class });
			systemBundleContext = runMethod.invoke(null, new Object[] { args, null });
		} catch (InvocationTargetException ite) {
			Throwable t = ite.getTargetException();
			if (t == null) {
				t = ite;
			}
			logger.error("Error while starting Framework", t);
			throw new RuntimeException(t);
		} catch (Exception e) {
			logger.error("Error while starting Framework", e);
			throw new RuntimeException(e);
		} finally {
			// return the context loader we entered this method with.
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	/**
	 * @return the frameworkClassLoader
	 */
	public ClassLoader getFrameworkClassLoader() {
		return frameworkClassLoader;
	}

	/**
	 * @return the systemBundleContext
	 */
	public Object getSystemBundleContext() {
		return systemBundleContext;
	}

	/**
	 * Reads the file specified by the {@link #LAUNCH_INI} constant and sets its
	 * properties as System properties. This method overrides any existing
	 * properties. It also makes sure to search for the file in the solution
	 * directory first.
	 */
	private void readLaunchProperties() {
		Properties launchProperties = null;
		// make sure we consider the solution directory also
		File launchIniFile = new File(OSGI, LAUNCH_INI);

		if (!launchIniFile.exists()) {
			launchIniFile = new File(platformDirectory, LAUNCH_INI);
		}

		// load the file
		launchProperties = loadProperties(launchIniFile);

		String key = null;
		String value = null;
		for (Map.Entry<Object, Object> entry : launchProperties.entrySet()) {
			key = (String) entry.getKey();
			value = (String) entry.getValue();

			if (key.endsWith("*")) {
				if (value.equals(NULL_IDENTIFIER)) {
					// clear the props.
					String prefix = key.substring(0, key.length() - 1);
					for (Object propertyName : System.getProperties().keySet()) {
						if (((String) propertyName).startsWith(prefix)) {
							System.clearProperty((String) propertyName);
						}
					}
				}
			} else if (value.equals(NULL_IDENTIFIER)) {
				System.clearProperty(key);
			} else {
				System.setProperty(key, value);
			}
		}
	}

	/**
	 * Interprets the launcher properties and initializes the missing ones. The
	 * following properties are checked and set if missing:
	 * {@link #OSGI_INSTALL_AREA}, {@link #OSGI_CONFIGURATION_AREA},
	 * {@link #OSGI_INSTALL_AREA} and {@link #OSGI_FRAMEWORK}. This method
	 * searches for file which name starts with the value of
	 * {@link #FRAMEWORK_BUNDLE_NAME} and if multiple found will get the one
	 * with the highest version specified in the name itself.
	 * 
	 * @throws RuntimeException
	 *             if unable to set the default values of any of the
	 *             configuration parameters.
	 */
	@SuppressWarnings("deprecation")
	private void interpretLocationProperties() {
		try {
			// make sure the "osgi.install.area" is set
			if (System.getProperty(OSGI_INSTALL_AREA) == null) {
				// the default place is "<install_dir>/osgi"

				// Note: we are using toURL here instead of .toURI().toURL()
				// because the EcliseStarter has some problems with converting
				// URIs to URLs. The problem is visible when we set the config
				// area to be in a place which path contains spaces (e.g.
				// c:\program files\tdi). The created config area folder will
				// contain the %20 for each space (e.g. c:\program%20files\tdi).
				// Using toURL() we get a URL like representation but without
				// encoding the characters.
				System.setProperty(OSGI_INSTALL_AREA, platformDirectory.toURL().toExternalForm());
			}

			// make sure the "osgi.configuration.area" is set
			if (System.getProperty(OSGI_CONFIGURATION_AREA) == null) {
				// the default place is "<sol_dir>/osgi/configuration"
				File configurationDirectory = new File(OSGI, "configuration");
				if (!configurationDirectory.exists() && !configurationDirectory.mkdirs()) {
					throw new RuntimeException(new FileNotFoundException(configurationDirectory.getAbsolutePath()));
				}
				System.setProperty(OSGI_CONFIGURATION_AREA, configurationDirectory.toURL().toExternalForm());
			}

			// make sure the "osgi.instance.area" is set
			if (System.getProperty(OSGI_INSTANCE_AREA) == null) {
				// the default place is "<sol_dir>/osgi/workspace"
				File workspaceDirectory = new File(OSGI, "workspace");
				if (!workspaceDirectory.exists() && !workspaceDirectory.mkdirs()) {
					throw new RuntimeException(new FileNotFoundException(workspaceDirectory.getAbsolutePath()));
				}
				System.setProperty(OSGI_INSTANCE_AREA, workspaceDirectory.toURL().toExternalForm());
			}

			// make sure the "osgi.framework" is set
			if (System.getProperty(OSGI_FRAMEWORK) == null) {
				File pluginsPath = new File(platformDirectory, "plugins");
				String fwkPath = searchFor(FRAMEWORK_BUNDLE_NAME, pluginsPath);
				if (fwkPath == null) {
					throw new RuntimeException(new FileNotFoundException(pluginsPath + "/" + FRAMEWORK_BUNDLE_NAME + ".jar"));
				}
				System.setProperty(OSGI_FRAMEWORK, new File(fwkPath).toURL().toExternalForm());
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private void interpretHttpProperties() {
		boolean sslOn = Boolean.getBoolean("web.server.ssl.on");
		// before we start the http server we need to map the config properties
		// so the HTTP/HTTPs ports are there.
		if (sslOn) {
			// -- default is on so we must explicitly disable it (or it tries port 80 and messes up)
			System.setProperty("org.eclipse.equinox.http.jetty.http.enabled", "false");
			System.setProperty("org.eclipse.equinox.http.jetty.https.enabled", "true");
			transferProperty("web.server.port", "org.eclipse.equinox.http.jetty.https.port");
		} else {
			System.setProperty("org.eclipse.equinox.http.jetty.http.enabled", "true");
			transferProperty("web.server.port", "org.eclipse.equinox.http.jetty.http.port");
		}

		BindAddressPolicy bap = new BindAddressPolicyImpl(System.getProperties(), true);
		if (bap.getBindAddress() == null) {
			if (sslOn) {
				System.getProperties().remove("org.eclipse.equinox.http.jetty.https.host");
			} else {
				System.getProperties().remove("org.eclipse.equinox.http.jetty.http.host");
			}
		} else {
			if (sslOn) {
				System.setProperty("org.eclipse.equinox.http.jetty.https.host", bap.getBindAddress().getHostAddress());
			} else {
				System.setProperty("org.eclipse.equinox.http.jetty.http.host", bap.getBindAddress().getHostAddress());
			}
		}

		// Map the properties from the Server API SSL settings
		if (sslOn) {
			transferProperty(Constants.PROP_SERVER_KEYSTORE, "org.eclipse.equinox.http.jetty.ssl.keystore", "");
			String keyStorePass = transferProperty(Constants.PROP_SERVER_KEYSTORE_PASSWORD,
					"org.eclipse.equinox.http.jetty.ssl.password", "");
			transferProperty(Constants.PROP_SERVER_KEY_PASSWORD, "org.eclipse.equinox.http.jetty.ssl.keypassword", keyStorePass);
			transferProperty(Constants.PROP_API_CLIENT_KEYSTORE_TYPE, "org.eclipse.equinox.http.jetty.ssl.keystoretype", "jks");
			//transferProperty("api.remote.ssl.client.auth.on", "org.eclipse.equinox.http.jetty.ssl.needclientauth");
			transferProperty("web.server.ssl.client.auth.on", "org.eclipse.equinox.http.jetty.ssl.needclientauth");
		}

		transferProperty("web.server.session.timeout", "org.eclipse.equinox.http.jetty.context.sessioninactiveinterval");

		// We need session termination notification. Make sure Jetty is
		// customized appropriately.
		System.setProperty("org.eclipse.equinox.http.jetty.customizer.class",
				"com.ibm.di.http.jetty.listener.internal.impl.HttpSessionCleanupEnabler");
	}

	private static String transferProperty(String src, String dst) {
		return transferProperty(src, dst, null);
	}

	private static String transferProperty(String src, String dst, String defVal) {
		String propVal = System.getProperty(src);
		if (propVal == null || (propVal = propVal.trim()).length() == 0) {
			propVal = defVal;
		}

		if (propVal != null) {
			System.setProperty(dst, propVal);
		}
		return propVal;
	}

	/**
	 * Parses the file specified by the {@link #CONSOLE_INI} constant and sets
	 * each line as a single commandline parameter. It also makes sure to search
	 * for the file in the solution directory first.
	 * 
	 * @return
	 */
	private String[] buildCommandLineArguments() {
		List<String> args = new ArrayList<String>();

		File consoleIni = new File(OSGI, CONSOLE_INI);

		if (!consoleIni.exists()) {
			consoleIni = new File(platformDirectory, CONSOLE_INI);
		}

		String commandLine = null;
		try {
			commandLine = FileUtils.loadFile(consoleIni);
		} catch (Exception e) {
			commandLine = "";
		}

		String arg = null;
		for (StringTokenizer tokenizer = new StringTokenizer(commandLine, WS_DELIM); tokenizer.hasMoreTokens(); args.add(arg)) {
			arg = tokenizer.nextToken(WS_DELIM);
			if (arg.startsWith("\"")) {
				String remainingArg = tokenizer.nextToken("\"");
				arg = arg.substring(1) + remainingArg;

			} else if (arg.startsWith("'")) {
				String remainingArg = tokenizer.nextToken("'");
				arg = arg.substring(1) + remainingArg;
			}
		}

		return args.toArray(new String[args.size()]);
	}

	private Properties loadProperties(File file) {
		URL resource = null;
		try {
			resource = file.toURI().toURL();
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
		}

		return loadProperties(resource);
	}

	protected Properties loadProperties(URL resource) {
		Properties result = new Properties();

		InputStream in = null;
		try {
			if (resource != null) {
				in = resource.openStream();
				result.load(in);
			}
		} catch (IOException ioexception) {
			logger.error(ioexception.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException ioexception1) {
					logger.error(ioexception1.getMessage());
				}
		}

		return result;
	}

	/**
	 * Searches for the specified file in the provided location.
	 * 
	 * @param target
	 *            the file name to look for.
	 * @param location
	 *            the location where to do the search.
	 * @return the absolute path to the file starting with the specified name or
	 *         <code>null</code> if not found.
	 */
	private String searchFor(final String target, File location) {

		File candidates[] = location.listFiles(new FileFilter() {
			public boolean accept(File candidate) {
				/*
				 * either looks like "org.eclipse.osgi_3.5.1.R35x_v20090827.jar"
				 * or like "org.eclipse.osgi.jar"
				 */
				return candidate.getName().startsWith(target + "_") || candidate.getName().equals(target + ".jar");
			}
		});

		String theFile = null;
		if (candidates != null) {
			String arrays[] = new String[candidates.length];
			for (int i = 0; i < arrays.length; i++) {
				arrays[i] = candidates[i].getName();
			}

			int result = findMax(arrays);
			if (result > -1) {
				theFile = candidates[result].getAbsolutePath().replace('\\', '/') + (candidates[result].isDirectory() ? "/" : "");
			}
		}
		return theFile;
	}

	/**
	 * Scans the provided array and finds the highest version from the files in
	 * the array.
	 * 
	 * @param candidates
	 *            the files
	 * @return the position of the file which has the maximum version number.
	 */
	private int findMax(String candidates[]) {
		int result = 0;
		Version max = null;
		for (int i = 0; i < candidates.length; i++) {
			String name = candidates[i];
			String version = "";
			int index = name.indexOf('_');
			if (index != -1) {
				version = name.substring(index + 1);

				Version current = getVersionElements(version);
				if (max == null) {
					result = i;
					max = current;
				} else if (max.compareTo(current) < 0) {
					result = i;
					max = current;
				}
			}
		}

		return result;
	}

	/**
	 * Parses a string of values to get a {@link Version} of the form "x.y.z.id"
	 * where x, y and z are numbers and id is a string.
	 * 
	 * @param version
	 *            the version as string.
	 * @return the version corresponding to the provided string
	 */
	private Version getVersionElements(String version) {
		if (version.endsWith(".jar")) {
			version = version.substring(0, version.length() - 4);
		}

		int[] v = new int[3];
		String id = "";

		StringTokenizer t = new StringTokenizer(version, ".");

		for (int i = 0; t.hasMoreTokens() && i < 4; i++) {
			String token = t.nextToken();
			if (i < 3) {
				try {
					v[i] = Integer.parseInt(token);
				} catch (NumberFormatException e) {
					v[i] = 0;
				}
			} else {
				id = token;
			}
		}

		return new Version(v[0], v[1], v[2], id);
	}

	private static final class Version implements Comparable<Version> {
		private final int v1;
		private final int v2;
		private final int v3;
		private final String id;

		public Version(int v1, int v2, int v3, String id) {
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			return o instanceof Version && compareTo((Version) o) == 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return v1 * v2 * v3 * id.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Version o) {
			int result = 0;

			if (v1 == o.v1) {
				if (v2 == o.v2) {
					if (v3 == o.v3) {
						result = id.compareTo(o.id);
					} else if (v3 < o.v3) {
						result = -1;
					} else {
						result = 1;
					}
				} else if (v3 < o.v3) {
					result = -1;
				} else {
					result = 1;
				}
			} else if (v1 < o.v1) {
				result = -1;
			} else {
				result = 1;
			}

			return result;
		}
	}
}
