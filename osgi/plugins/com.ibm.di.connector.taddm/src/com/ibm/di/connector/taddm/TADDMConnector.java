/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.SearchCriteria;

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
public class TADDMConnector extends Connector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final L10N L10N = L10NFactory.getInstance(TADDMConnector.class, "taddmcdconnector");

	/**
	 * The name of the TADDM SDK parameter from the Connector's configuration
	 * panel.
	 */
	private static final String PARAM_TADDM_SDK = "taddmSDK";

	/**
	 * The java property required for using the TADDM Java SDK.
	 */
	private static final String COLLATION_HOME_JAVA_PROPERTY = "com.collation.home";

	private ClassLoader ctxLoader;

	protected TADDMClassLoader loader;

	protected Connector worker;
	
	protected Collection jarFiles;

	/**
	 * 
	 */
	public TADDMConnector() {
		super();
		setName("TADDM Connector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE, ConnectorConfig.DELETE_MODE,
				ConnectorConfig.ADDONLY_MODE, ConnectorConfig.UPDATE_MODE, ConnectorConfig.DELTA_MODE });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#initialize(java.lang.Object)
	 */
	@Override
	public void initialize(Object o) throws Exception {

		if (worker == null) {
			worker = createWorker((BaseConfiguration) getConfiguration());
		}

		setContextLoader();

		// we set the collation home so that TADDM's SDK can function
		// properly
		System.setProperty(COLLATION_HOME_JAVA_PROPERTY, getStringParameter(PARAM_TADDM_SDK));

		Class<?> propsClass = loader.loadClass("com.collation.platform.util.Props");
		Method homeMethod = propsClass.getMethod("getHome", (Class[]) null);
		debug(L10N.getString("TADDM.CONN.COLLATION.HOME.SET", homeMethod.invoke(null, (Object[]) null)));
		redirectTADDMLogging();

		worker.initialize(o);
		unsetContextLoader();
	}

	private void setContextLoader() {
		if (loader != null) {
			ctxLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	private void unsetContextLoader() {
		if (loader != null) {
			Thread.currentThread().setContextClassLoader(ctxLoader);
		}
	}

	/**
	 * @param configuration
	 * @return
	 * @throws Exception
	 */
	private Connector createWorker(BaseConfiguration config) throws Exception {
		if (worker == null) {

			if (config instanceof RawConnectorConfig == false) {
				config = ((ConnectorConfig) config).getConnectionConfig();
			}

			String taddmSDK = config.getStringParameter(PARAM_TADDM_SDK);
			worker = createWorker(taddmSDK);
		}
		return worker;
	}

	private Connector createWorker(String taddmSDK) throws Exception, FileNotFoundException, MalformedURLException,
			URISyntaxException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		if (worker != null) {
			return worker;
		}

		if (taddmSDK != null && taddmSDK.length() > 0) {
			debug(L10N.getString("TADDM.CONN.PARAMETER.INITIALIZED", PARAM_TADDM_SDK, taddmSDK));
		} else {
			throw new Exception(L10N.getString("TADDM.CONN.PARAMETER.NOT.PROVIDED", PARAM_TADDM_SDK));
		}

		URL[] taddmJars = getSDKLibraries(taddmSDK);
		loader = new TADDMClassLoader(taddmJars, TADDMConnector.class.getClassLoader());
		Class<?> taddmClass = loader.loadClass(getConnectorClass());
		worker = (Connector) taddmClass.newInstance();
		return worker;
	}

	/**
	 * @return
	 */
	protected String getConnectorClass() {
		return "com.ibm.di.connector.taddm.TADDMWorkerConnector";
	}

	/**
	 * @param taddmSDK
	 * @return
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 */
	private URL[] getSDKLibraries(String taddmSDK) throws FileNotFoundException, MalformedURLException {
		if (taddmSDK.startsWith("file:")) {
			taddmSDK = taddmSDK.substring(5);
		}

		File sdk = new File(taddmSDK);
		if (sdk.isDirectory()) {
			File lib = getLibDirectory(sdk);

			File clientApi = getJarFile("taddm-api-client.jar");
			File platformModel = getJarFile("platform-model.jar");

			Bundle thisBundle = FrameworkUtil.getBundle(TADDMConnector.class);

			if (thisBundle != null) {
				URL implBin = thisBundle.getResource("ibin");
				if (implBin == null) {
					implBin = thisBundle.getResource("/ibin");
				}

				try {
					implBin = FileLocator.resolve(implBin);
					// when there is a space this returns unencoded path
					// in that case converting to URI will let us know we need
					// to handle it properly
					implBin.toURI();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					String zipProtocol = "zip".equals(implBin.getProtocol()) ? "zip:"
							: ("jar".equals(implBin.getProtocol()) ? "jar:" : null);
					String zipSpec = null;
					if (zipProtocol != null) {
						String urlPath = implBin.getPath();
						// strip off zip/jar specifics
						int zipSpecStart = urlPath.indexOf('!');
						zipSpec = urlPath.substring(zipSpecStart);
						urlPath = urlPath.substring(0, zipSpecStart);
						implBin = new URL(urlPath);
					} // else - directory maybe?

					if ("file".equals(implBin.getProtocol())) {
						File f = new File(implBin.getPath());
						implBin = f.toURI().toURL();
					} // else - don't know how to encode anything else... maybe
					// with splitting by '/' and invoking URLEncoder on
					// every path element... don't need it for now

					if (zipProtocol != null) {
						implBin = new URL(zipProtocol + implBin.toString() + zipSpec);
					}
				}

				return new URL[] { implBin, clientApi.toURI().toURL(), platformModel.toURI().toURL() };
			} else {
				return new URL[] { clientApi.toURI().toURL(), platformModel.toURI().toURL() };
			}
		} else {
			throw new FileNotFoundException(taddmSDK);
		}
	}

	protected File getJarFile(String jarFileName)
	{
		if (jarFiles == null || jarFiles.size() == 0)
		{
			return null;
		}
		for (Iterator iterator = jarFiles.iterator(); iterator.hasNext();)
		{
			File file = (File) iterator.next();
			if (jarFileName.equals(file.getName()))
			{
				return file;
			}
		}
		return null;
	}

	private File getLibDirectory(File sdk) throws FileNotFoundException {
		// try taddm 7.2
		File lib = new File(sdk, "lib");
		if (!lib.isDirectory()) {
			// try taddm 7.1.2
			lib = new File(sdk, "clientlib");

			if (!lib.isDirectory()) {
				// try tdi reserved name
				lib = new File(sdk, "tdilib");

				if (!lib.isDirectory()) {
					throw new FileNotFoundException(sdk.toString() + "/(lib | clientlib | tdilib)");
				}
			}
		}
		loadJarFiles(sdk);
		return lib;
	}

	protected void loadJarFiles(File sdk)
	{
		jarFiles = getFileListing(sdk, new FileFilter()
		{
			public boolean accept(File file)
			{
				if(file.getName().endsWith(".jar"))
				{
					return true;
				}
				else if(file.isDirectory())
				{
					return true;
				}
				return false;
			}
		});
	}
	
	private List<File> getFileListing(File dir, FileFilter filter)
	{
		List<File> result = new ArrayList<File>();
		File[] files = dir.listFiles(filter);
		for (int i = 0; i < files.length; i++)
		{
			if (!files[i].isFile())
			{
				List<File> deeperList = getFileListing(files[i], filter);
				result.addAll(deeperList);
			}
			else
			{
				result.add(files[i]);
			}
		}
		return result;
	}

	/**
	 * <p>
	 * <b>Important:</b> This method may have to be revisited for the future
	 * versions of TADDM.
	 * </p>
	 * 
	 * <p>
	 * By default when TADDM has a configured log, it removes all other
	 * appenders of the root logger before setting up its own. This causes the
	 * TDI log to be hijacked by TADDM.
	 * </p>
	 * 
	 * This method uses low-level knowledge of the TADDM Log4jFactory class and
	 * prevents it from hijacking the TDI log. This is done it several steps:
	 * <ol>
	 * <li>The 'configured_' class variable is set to TRUE, thus preventing the
	 * removal of all existing appenders.</li>
	 * <li>If no TADDM log file is provided, a dummy value is set to prevent
	 * TADDM from logging in the console.</li>
	 * </ol>
	 */
	private void redirectTADDMLogging() {
		FileInputStream fis = null;
		File propsFile = null;
		try {
			// Mark the TADDM logger as configured. This will prevent TADDM from
			// clearing all appenders, including the TDI.
//			Class<?> log4jFactoryClass = loader.loadClass("com.collation.platform.logger.Log4jFactory");
//			Field configured = log4jFactoryClass.getDeclaredField("configured_");
//			configured.setAccessible(true);
//			configured.set(null, Boolean.TRUE);
//			Field hierarchy = log4jFactoryClass.getDeclaredField("hierarchy_");
//			hierarchy.setAccessible(true);
//
//			Class<?> loggerClass = loader.loadClass("org.apache.log4j.Logger");
//			Method rootLoggerMethod = loggerClass.getMethod("getRootLogger", (Class[]) null);
//			Object rootLogger = rootLoggerMethod.invoke(null, (Object[]) null);
//
//			Class<?> hierarchyClass = loader.loadClass("org.apache.log4j.Hierarchy");
//			Constructor<?> hconst = hierarchyClass.getConstructor(loggerClass);
//
//			hierarchy.set(null, hconst.newInstance(rootLogger));

			// provide a default log file, if none is available in
			// collation.properties
			String logFile = System.getProperty("com.collation.LogFile");
			if (logFile == null) {
				System.setProperty("com.collation.LogFile", "log/api-client.log");
			}

			// reload the collation.properties file
			if (System.getProperty("com.collation.api.port") == null &&
					System.getProperty("com.collation.version") == null) {
				propsFile = new File(System.getProperty(COLLATION_HOME_JAVA_PROPERTY), "etc/collation.properties");
				fis = new FileInputStream(propsFile);
				System.getProperties().load(fis);
			}
		} catch (Exception ex) {
			// unable to reset logging or to reload the props file afterwards
			if (propsFile != null)
				debug("Unable to reload props file '" + propsFile.getAbsolutePath() + "': " + ex);
			else
				debug("Unable to reset logging: " + ex);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				//ignore
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.server.VersionInfoInterface#getVersion()
	 */
	public String getVersion() {
		return worker.getVersion();
	}

	private void checkInitialized() {
		if (worker == null) {
			throw new RuntimeException("Initialize the connector first");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#addFindEntry(java.lang.Object)
	 */
	@Override
	public boolean addFindEntry(Object entry) {
		checkInitialized();
		setContextLoader();
		try {
			return worker.addFindEntry(entry);
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#clearFindEntries()
	 */
	@Override
	public void clearFindEntries() {
		checkInitialized();
		setContextLoader();
		try {
			worker.clearFindEntries();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#debug(java.lang.String)
	 */
	@Override
	public void debug(String msg) {
		if (worker != null) {
			worker.debug(msg);
		} else {
			super.debug(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#debugMode()
	 */
	@Override
	public boolean debugMode() {
		return worker != null ? worker.debugMode() : super.debugMode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#deleteEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.deleteEntry(entry, search);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#findEntry(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public Entry findEntry(Object arg0, Object arg1) {
		checkInitialized();
		setContextLoader();
		try {
			return worker.findEntry(arg0, arg1);
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#findEntry(com.ibm.di.server.SearchCriteria
	 * )
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.findEntry(search);
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getBoolean(java.lang.Object)
	 */
	@Override
	public Boolean getBoolean(Object p1) {
		return worker != null ? worker.getBoolean(p1) : super.getBoolean(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getConfiguration()
	 */
	@Override
	public Object getConfiguration() {
		return worker != null ? worker.getConfiguration() : super.getConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getContext()
	 */
	@Override
	public Object getContext() {
		return worker != null ? worker.getContext() : super.getContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getFindEntryCount()
	 */
	@Override
	public int getFindEntryCount() {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getFindEntryCount();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getFirstFindEntry()
	 */
	@Override
	public Entry getFirstFindEntry() throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getFirstFindEntry();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getLog()
	 */
	@Override
	public Log getLog() {
		return worker != null ? worker.getLog() : super.getLog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getMaxDuplicateEntries()
	 */
	@Override
	public int getMaxDuplicateEntries() {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getMaxDuplicateEntries();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getModes()
	 */
	@Override
	public Vector<String> getModes() {
		return worker != null ? worker.getModes() : super.getModes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#getModes(com.ibm.di.config.interfaces.
	 * ConnectorConfig)
	 */
	@Override
	public Vector<String> getModes(ConnectorConfig config) {
		return worker != null ? worker.getModes(config) : super.getModes(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getName()
	 */
	@Override
	public String getName() {
		return worker != null ? worker.getName() : super.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextClient()
	 */
	@Override
	public ConnectorInterface getNextClient() throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getNextClient();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextEntry()
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getNextEntry();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getNextFindEntry()
	 */
	@Override
	public Entry getNextFindEntry() throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.getNextFindEntry();
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getParam(java.lang.String)
	 */
	@Override
	public String getParam(String param) {
		return worker != null ? worker.getParam(param) : super.getParam(param);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getParser()
	 */
	@Override
	public ParserInterface getParser() {
		return worker != null ? worker.getParser() : super.getParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getProperty(java.lang.Object)
	 */
	@Override
	public Object getProperty(Object p1) {
		return worker != null ? worker.getProperty(p1) : super.getProperty(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getPushbackEntry()
	 */
	@Override
	public Entry getPushbackEntry() {
		checkInitialized();
		return worker.getPushbackEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getRawConnectorConfiguration()
	 */
	@Override
	public BaseConfiguration getRawConnectorConfiguration() {
		return worker != null ? worker.getRawConnectorConfiguration() : super.getRawConnectorConfiguration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getRSInterface()
	 */
	@Override
	public RSInterface getRSInterface() {
		return worker != null ? worker.getRSInterface() : super.getRSInterface();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#getUI()
	 */
	@Override
	public JComponent getUI() {
		checkInitialized();
		return worker.getUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#hasConfigValue(java.lang.Object)
	 */
	@Override
	public boolean hasConfigValue(Object p1) {
		return worker != null ? worker.hasConfigValue(p1) : super.hasConfigValue(p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return worker != null ? worker.equals(o) : super.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return worker != null ? worker.hashCode() : super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {

		Method method = null;
		try {
			method = worker.getClass().getMethod("clone", (Class[]) null);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		if (method != null) {
			boolean access = method.isAccessible();
			try {
				method.setAccessible(true);
				return method.invoke(worker, (Object[]) null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} finally {
				method.setAccessible(access);
			}
		}
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#hasParser()
	 */
	@Override
	public boolean hasParser() {
		checkInitialized();
		return worker.hasParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#initParser(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void initParser(Object is, Object os) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.initParser(is, os);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isDeltaSupported()
	 */
	@Override
	public boolean isDeltaSupported() {
		checkInitialized();
		return worker.isDeltaSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isExceptionFatal(java.lang.Exception)
	 */
	@Override
	public boolean isExceptionFatal(Exception e) {
		checkInitialized();
		return worker.isExceptionFatal(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#isIOException(java.lang.Throwable)
	 */
	@Override
	public boolean isIOException(Throwable e) {
		checkInitialized();
		return worker.isIOException(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#logError(java.lang.String)
	 */
	@Override
	public void logError(String msg) {
		if (worker != null) {
			worker.logError(msg);
		} else {
			super.logError(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#logmsg(java.lang.String)
	 */
	@Override
	public void logmsg(String msg) {
		if (worker != null) {
			worker.logmsg(msg);
		} else {
			super.logmsg(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#modEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.modEntry(entry, search);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#modEntry(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria, com.ibm.di.entry.Entry)
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.modEntry(entry, search, old);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#pushback(com.ibm.di.entry.Entry)
	 */
	@Override
	public void pushback(Entry e) {
		checkInitialized();
		setContextLoader();
		worker.pushback(e);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#putEntry(com.ibm.di.entry.Entry)
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.putEntry(entry);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#queryOperations(com.ibm.di.config.interfaces
	 * .ConnectorConfig)
	 */
	@Override
	public void queryOperations(ConnectorConfig config) throws Exception {
		checkInitialized();
		worker.queryOperations(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#queryReply(com.ibm.di.entry.Entry)
	 */
	@Override
	public Entry queryReply(Entry entry) throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.queryReply(entry);
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#querySchema(java.lang.Object)
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		checkInitialized();
		setContextLoader();
		try {
			return worker.querySchema(source);
		} finally {
			unsetContextLoader();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#queryTables()
	 */
	@Override
	public Vector<String> queryTables() throws Exception {
		checkInitialized();
		return worker.queryTables();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#reconnect()
	 */
	@Override
	public void reconnect() throws Exception {
		checkInitialized();
		worker.reconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#reconnect(java.lang.Object)
	 */
	@Override
	public void reconnect(Object o) throws Exception {
		checkInitialized();
		worker.reconnect(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#registerScriptBeans(com.ibm.di.script.
	 * ScriptEngine)
	 */
	@Override
	public void registerScriptBeans(ScriptEngine se) throws Exception {
		worker.registerScriptBeans(se);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#replyEntry(com.ibm.di.entry.Entry)
	 */
	@Override
	public void replyEntry(Entry entry) throws Exception {
		checkInitialized();
		setContextLoader();
		worker.replyEntry(entry);
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#selectEntries()
	 */
	@Override
	public void selectEntries() throws Exception {
		checkInitialized();
		setContextLoader();
		worker.selectEntries();
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setConfiguration(java.lang.Object)
	 */
	@Override
	public void setConfiguration(Object config) {
		if (worker == null) {
			try {
				worker = createWorker((BaseConfiguration) config);
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new RuntimeException(e);
				}
			}
		}
		worker.setConfiguration(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setContext(java.lang.Object)
	 */
	@Override
	public void setContext(Object aContext) {
		super.setContext(aContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setCurrent(com.ibm.di.entry.Entry,
	 * com.ibm.di.server.SearchCriteria)
	 */
	@Override
	public void setCurrent(Entry entry, SearchCriteria search) {
		checkInitialized();
		worker.setCurrent(entry, search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setDebugMode(boolean)
	 */
	@Override
	public void setDebugMode(boolean debug) {
		checkInitialized();
		worker.setDebugMode(debug);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setLog(com.ibm.di.server.Log)
	 */
	@Override
	public void setLog(Log logger) {
		super.setLog(logger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setMaxDuplicateEntries(int)
	 */
	@Override
	public void setMaxDuplicateEntries(int mde) {
		checkInitialized();
		worker.setMaxDuplicateEntries(mde);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setModes(java.lang.String[])
	 */
	@Override
	public void setModes(String[] modes) {
		super.setModes(modes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setModes(java.util.Vector)
	 */
	@Override
	public void setModes(Vector<String> modes) {
		super.setModes(modes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setParam(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setParam(String param, String value) {
		super.setParam(param, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#setParser(com.ibm.di.parser.ParserInterface
	 * )
	 */
	@Override
	public void setParser(ParserInterface parser) throws IOException {
		super.setParser(parser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#setProperty(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void setProperty(Object p1, Object p2) {
		super.setProperty(p1, p2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.connector.Connector#setRSInterface(com.ibm.di.server.RSInterface
	 * )
	 */
	@Override
	public void setRSInterface(RSInterface rsi) {
		super.setRSInterface(rsi);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#terminate()
	 */
	@Override
	public void terminate() throws Exception {
		checkInitialized();
		setContextLoader();
		worker.terminate();
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.Connector#terminateServer()
	 */
	@Override
	public void terminateServer() throws Exception {
		checkInitialized();
		setContextLoader();
		worker.terminateServer();
		unsetContextLoader();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (worker != null) {
			return worker.toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * Retrieves a String value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the String parameter.
	 * @return the String value of the parameter.
	 */
	protected String getStringParameter(String parameterName) {
		String parameter = getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}

	public MetaData getNoConnectionMetadata(boolean idmlMode) throws Exception {
		Class<?> tmd = loader.loadClass("com.ibm.di.connector.taddm.cdm.TADDMMetaData");
		return (MetaData) tmd.getConstructor(new Class[] { boolean.class }).newInstance(idmlMode);
	}

	public MetaData getMetaData() {
		checkInitialized();
		try {
			return (MetaData) worker.getClass().getMethod("getMetaData", (Class[]) null).invoke(worker, (Object[]) null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String createWhereClause(SearchCriteria searchCriteria) {
		try {
			return (String) loader.loadClass("com.ibm.di.connector.taddm.cdm.query.TADDMQueryBuilder").getMethod(
					"createWhereClause", new Class[] { SearchCriteria.class }).invoke(null, new Object[] { searchCriteria });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Map<String, Object>> getMSS() throws Exception{
		checkInitialized();
		return (Map<String, Map<String, Object>>) worker.getClass().getMethod("getMSS", (Class[]) null).invoke(worker, (Object[])null);
	}
}
