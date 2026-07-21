/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.plugin.pwstore.PasswordStoreAdapter;
import com.ibm.di.plugin.security.SecurityHelper;
import com.ibm.di.plugin.security.authentication.ProxyAuth;
import com.ibm.di.server.ResourceHash;

/**
 * This is the common Java Proxy that is responsible for reading the appropriate
 * configuration file and listening for incoming connections.
 */
public class Proxy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/** this is the default port the Proxy will listen to */
	public static final int DEFAULT_SERVER_PORT = 18001;

	/** the key name for the proxy's configuration file path */
	public static final String PROXY_CONFIG_FILE = "proxyConfigFile";

	/** the key name for the proxy's security folder */
	public static final String PROXY_AUTH_FOLDER = "passwordFilesDir";

	/** the key name for the proxy's password store class name */
	public static final String PROXY_SYNC_CLASS = "syncClass";

	/** the key name for the proxy's listening port */
	public static final String PROXY_PORT = "serverPort";

	/** the key name for the proxy's log file */
	public static final String PROXY_LOG_FILE = "javaLogFile";
	
	/** The key name for the log file rotation pattern */
	public static final String PROXY_LOG_ROTATION_PATTERN = "javaLogRotationPattern";
	
	/** The key name for the log encoding */
	public static final String LOG_ENCODING = "logEncoding";
	
	/** the key name for the proxy's debug flag */
	public static final String PROXY_DEBUG = "debug";

	/** the key name for the proxy's custom data */
	public static final String PROXY_CUSTOM_DATA = "customData";

	/**
	 * this array contains the keys which values are taken from the Java
	 * Property store, decrypted and put back with the same keys.
	 */
	private static final String[] PROPS_TO_DECRYPT = new String[] { "javax.net.ssl.trustStorePassword",
			"javax.net.ssl.keyStorePassword" };

	private boolean mStopProxy = false;

	/** this is the log object used by the proxy */
	private PWSyncLog log = null;

	/** the Proxy uses this object to synchronize critical parts of the code */
	private Object stopProxyLock = new Object();

	private int port = DEFAULT_SERVER_PORT;
	
	private PasswordStore pwstore = null;

	private List<ProxyCommandReceiver> commandReceivers = new ArrayList<ProxyCommandReceiver>();

	/** the proxy's log prefix */
	protected static final String PREFIX = "Proxy";

	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	/**
	 * Request from the Proxy to stop execution. This method will block to make
	 * sure a request does not come before the Proxy is completely initialized.
	 */
	public void requestProxyStop() {

		synchronized (stopProxyLock) {

			// ProxyStop already requested.
			if (mStopProxy)
				return;

			mStopProxy = true;

			try {
				new Socket(InetAddress.getByName(null), port).close();
				log.debug(PREFIX, resHash.getString("PWSYNC.PROXY.STOP.REQUESTED"));
			} catch (UnknownHostException e) {
				log.error(PREFIX, resHash.getString("PWSYNC.UNKNOWN.HOST"), e);
			} catch (IOException e) {
				log.debug(PREFIX, resHash.getString("PWSYNC.STOP.FAILED"));
			}
		}

		int count = 0;
		// wait for all other threads.
		while ((count = getReceiversCount()) > 1) {
			log.debug(PREFIX, resHash.getString("PWSYNC.WAITING.FOR.THREADS", (count - 1)));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.debug(PREFIX, resHash.getString("PWSYNC.WAITING.FOR.THREADS.INTERRUPTED"));
			}
		}
	}

	/**
	 * Check whether the Proxy is requested to stop.
	 * 
	 * @return true if the proxy was asked to stop, false otherwise.
	 */
	public boolean proxyStopRequested() {
		synchronized (stopProxyLock) {
			return mStopProxy;
		}
	}

	/**
	 * This is the entry point of the Java Proxy process when it is run by a
	 * plug-in other than the Domino plug-in.
	 * 
	 * @param args
	 *            no specific arguments are checked or expected.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws Exception {

		Proxy p = new Proxy();
		p.init();
		p.runProxy();
	}

	/**
	 * Reads the proxy configuration file given by a System Property with the
	 * name {@link Proxy#PROXY_CONFIG_FILE}
	 * 
	 * @throws FileNotFoundException
	 *             if the file could not be found.
	 * @throws IOException
	 *             if a read error occurs.
	 * @throws IllegalArgumentException
	 *             if the required System Property is missing or malformed.
	 */
	protected static void readProxyConfigurationFile() throws FileNotFoundException, IOException {
		String configFile = getProperty(PROXY_CONFIG_FILE);

		if (configFile == null) {
			System.out.println(resHash.getString("PWSYNC.MISSING.PROXY.CONFIG.PROP", PROXY_CONFIG_FILE));
			throw new FileNotFoundException(resHash.getString("PWSYNC.INCORRECT.PATH.TO.PROXY.CONFIG.FILE"));
		}

		File fConfigFile = new File(configFile);
		if (!fConfigFile.isFile()) {
			throw new IllegalArgumentException(resHash.getString("PWSYNC.INCORRECT.PATH.TO.PROXY.CONFIG.FILE"));
		}

		InputStream fileIs = new FileInputStream(configFile);
		try {
			System.getProperties().load(fileIs);
		} finally {
			fileIs.close();
		}

		System.getProperties().setProperty(PROXY_AUTH_FOLDER, fConfigFile.getCanonicalFile().getParent());
	}

	/**
	 * initializes the {@link Proxy} class.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void init() throws Exception {
		initializeProviders();
		readProxyConfigurationFile();
		initLog();
		validateAndPrepare();
	}
	
	private void initializeProviders() {
		String httpsHandler = System.getProperty("java.protocol.handler.pkgs", "");
		if (httpsHandler.trim().length() == 0) {
			System.setProperty("java.protocol.handler.pkgs", "com.ibm.net.ssl.www2.protocol");
		} else if (!httpsHandler.contains("com.ibm.net.ssl.www2.protocol")) {
			System.setProperty("java.protocol.handler.pkgs", httpsHandler + " | com.ibm.net.ssl.www2.protocol");
		}
		// End #11713
	}

	/**
	 * The main worker method. This method expects that the {@link #init()}
	 * method was called previously and it has succeeded.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void runProxy() throws Exception {
		try {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port, 20, InetAddress.getByName(null));
			} catch (IOException e) {
				log.error(PREFIX, resHash.getString("PWSYNC.COULD.NOT.OPEN.SERVER.SOCKET"), e);
				throw e;
			}

			String authFolder = getProperty(PROXY_AUTH_FOLDER);
			log.info(PREFIX, resHash.getString("PWSYNC.AUTH.FOLDER", authFolder));

			while (!proxyStopRequested()) {
				try {

					Socket socket = serverSocket.accept();

					if (proxyStopRequested()) {
						socket.close();
						break;
					}

					if (ProxyAuth.authenticate(socket, authFolder, log)) {
						log.info(PREFIX, resHash.getString("PWYSYNC.AUTH.SUCCESS"));
					} else {
						log.warn(PREFIX, resHash.getString("PWSYNC.AUTH.FAILURE"));
						socket.close();
						continue;
					}

					ProxyCommandReceiver receiver = new ProxyCommandReceiver(this, socket, pwstore, log);

					addReceiver(receiver);

					new Thread(receiver).start();
				} catch (IOException e) {
					log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
				}
			}

			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				log.error(PREFIX, resHash.getString("PWSYNC.IO.EXCEPTION"), e);
			}
			pwstore.terminate();

			log.info(PREFIX, resHash.getString("PWSYNC.PROXY.SHUTTING.DOWN"));
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("PWSYNC.EXCEPTION"), e);
		} finally {
			log.close();
		}
	}

	private void validateAndPrepare() throws Exception {

		// if we are here then the log is initialized correctly.
		if (getProperty(PROXY_SYNC_CLASS) == null) {
			throw new IllegalArgumentException(resHash.getString("PWSYNC.NO.SYNC.CLASS"));
		}

		// decrypt properties
		String val = null;
		for (String s : PROPS_TO_DECRYPT) {
			val = getProperty(s);
			val = SecurityHelper.getClearText(val);
			if (val != null)
				System.setProperty(s, val);
		}

		// create the custom password synchronizer
		try {
			Class<?> syncClass = Class.forName(getProperty(PROXY_SYNC_CLASS));

			Constructor<?> syncConstructor = syncClass.getConstructor((Class[]) null);

			Object inst = syncConstructor.newInstance((Object[]) null);

			// adapt objects implementing the deprecated interface
			if (inst instanceof PasswordStore) {
				pwstore = (PasswordStore) inst;
			} else if (inst instanceof IPasswordSynchronizer) {
				pwstore = new PasswordStoreAdapter((IPasswordSynchronizer) inst);
			} else {
				// return since we can not configure password store
				log.error(PREFIX, resHash.getString("PWSYNC.ERROR.INCORRECT.SYNC.CLASS"));
				return;
			}

			pwstore.initialize(log);

		} catch (Throwable t) {
			/*
			 * Catch Throwable here, because NoClassDefFoundError may occur if
			 * the class-path is incomplete.
			 */
			String msg = resHash.getString("PWSYNC.ERROR.IN.SYNC.CLASS");
			log.error(PREFIX, msg, t);
			throw new Exception(msg, t);
		}

		try {
			port = Integer.parseInt(getProperty(PROXY_PORT));
		} catch (NumberFormatException e) {
			log.warn(PREFIX, resHash.getString("PWSYNC.INCORRECT.PORT", new Object[] { PROXY_PORT,
					String.valueOf(DEFAULT_SERVER_PORT) }));
			port = DEFAULT_SERVER_PORT;
		}
	}

	private void initLog() throws UnsupportedEncodingException, FileNotFoundException {

		String logFileName = getProperty(PROXY_LOG_FILE);

		if (logFileName == null || "".equals(logFileName)) {
			// No logging will be available
			log = new PWSyncLog(null, null, false);
			return;
		}

		boolean debug = false;

		if ("true".equalsIgnoreCase(getProperty(PROXY_DEBUG)) || "1".equals(getProperty(PROXY_DEBUG))) {
			debug = true;
		}

		log = PWSyncLog.getLogForFile(logFileName, getProperty(LOG_ENCODING), debug, getProperty(PROXY_LOG_ROTATION_PATTERN));
	}

	void addReceiver(ProxyCommandReceiver rec) {
		synchronized (commandReceivers) {
			commandReceivers.add(rec);
		}
	}

	void removeReceiver(ProxyCommandReceiver rec) {
		synchronized (commandReceivers) {
			commandReceivers.remove(rec);
		}
	}

	int getReceiversCount() {
		synchronized (commandReceivers) {
			return commandReceivers.size();
		}
	}
	
	private static String getProperty(String propName) {
		String propValue = System.getProperty(propName);
		if (propValue != null) {
			propValue = propValue.trim();
		}
		return propValue;
	}
}
