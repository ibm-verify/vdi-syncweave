/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.Context;

import com.ibm.di.api.ConfigEvent.Type;
import com.ibm.di.api.exceptions.ConfigurationExistsException;
import com.ibm.di.api.exceptions.ConfigurationNotCheckedOutException;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.ConfigurationFileListener;
import com.ibm.di.api.local.Session;
import com.ibm.di.api.local.impl.SessionImpl;
import com.ibm.di.api.security.Identity;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.Listenable;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ThreadSafeListenableImpl;
import com.ibm.di.server.ThreadSafeListenableImpl.Visitor;

/**
 * This class represents the repository used for manipulating configInstances.
 */
public final class ConfigurationRegistry implements Listenable<ConfigurationFileListener> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Property name which value is used as the configurations root directory.
	 * Only the configuration files placed in this directory can be edited
	 * through the Server API.
	 */
	public static final String PROPERTY_ROOT_PATH = "api.config.folder";

	/**
	 * Property name used to specify the timeout in minutes for configuration
	 * locks. A value of 0 means no timeout.
	 */
	public static final String PROPERTY_LOCK_TIMEOUT = "api.config.lock.timeout";

	/**
	 * Key name - identity.
	 */
	private static final String KEY_IDENTITY = "identity";

	/**
	 * Key name - timestamp.
	 */
	private static final String KEY_CHECKOUT_TIMESTAMP = "timestamp";

	/**
	 * Key name - load.
	 */
	private static final String KEY_LOAD = "load";

	/**
	 * This is the prefix used when creating a temporary config instances.
	 */
	public static final String CM_PREFIX = "tmp_cr_";

	/**
	 * Lock timeout in minutes- 0;
	 */
	private static final int LOCK_TIMEOUT_MINUTES = 0;

	/**
	 * Time interval for auto unlock - 3 seconds.
	 */
	private static final long AUTO_UNLOCK_SLEEP_INTERVAL = 30000L;

	/**
	 * Root path.
	 */
	private String mRootPath = null;

	/**
	 * Canonical root path.
	 */
	private String mRootPathCanonical = null;

	/**
	 * Registry map. <br>
	 * <b>keys</b> - Configuration relative paths <br>
	 * <b>values </b>- Maps with Identity and Checkout time
	 */
	private Map<String, Map<String, Object>> mRegistry = new TreeMap<String, Map<String, Object>>();

	/**
	 * Used to lock the {@link #mRegistry} object.
	 */
	private Object mRegistryLock = new Object();

	/**
	 * The variable holds the timeout information in minutes.
	 */
	private int mTimeout = 0;

	/**
	 * {@link AutoUnlock} instance.
	 */
	private AutoUnlock mAutoUnlock = null;

	/**
	 * Object that is used to lock solution names mappings.
	 */
	private Object configFileAndSolutionNameMappingsLock = new Object();

	/**
	 * Solution Names cache - stores mappings between configuration files
	 * canonical paths and their solution names. If a configuration file has no
	 * solution name, it appears in the cache with a null solution name.
	 * 
	 * The cache consists of two maps, which allow null values (TreeMap). The
	 * entries in the maps are not independent, so the maps are to be accessed
	 * as one synchronized entity (one lock for both).
	 */
	private Map<String, String> configFileToSolutionNameMap = new TreeMap<String, String>();
	
	/**
	 * Solution Names cache - stores mappings between configuration files
	 * canonical paths and their solution names. If a configuration file has no
	 * solution name, it appears in the cache with a null solution name.
	 * 
	 * The cache consists of two maps, which allow null values (TreeMap). The
	 * entries in the maps are not independent, so the maps are to be accessed
	 * as one synchronized entity (one lock for both).
	 */
	private Map<String, String> solutionNameToConfigFileMap = new TreeMap<String, String>();
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Holds up the collection of listeners and also delivers the notifications.
	 */
	private ThreadSafeListenableImpl<ConfigurationFileListener> eventSource = new ThreadSafeListenableImpl<ConfigurationFileListener>();

	/**
	 * Solutions with active schedules should be auto started. We keep a list here for use by RS.java and others.
	 */
	private List<File> activeSchedulesConfigs = new ArrayList<File>();
	
	/**
	 * Default constructor used for creation and initialization of the
	 * configuration registry.
	 * 
	 * @throws DIException
	 *             if an error occurs.
	 */
	public ConfigurationRegistry() throws DIException {

		String rootPath = System.getProperty(PROPERTY_ROOT_PATH);
		if (rootPath == null || rootPath.trim().length() == 0) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.MISSING.CONFIGURATION.PROPERTY"));
		} else {
			mRootPath = rootPath;
			if (!mRootPath.endsWith("/")) {
				mRootPath = mRootPath + "/";
			}
			File file = new File(mRootPath);
			mRootPathCanonical = getCanonicalPath(file);
		}

		parseSolutionNamesOfAllConfigurations();
		String timeoutStr = System.getProperty(PROPERTY_LOCK_TIMEOUT);
		if (timeoutStr != null && timeoutStr.trim().length() > 0) {
			try {
				mTimeout = Integer.parseInt(timeoutStr);
			} catch (NumberFormatException e) {
				APIEngine.logError(sResHash.getString("SEVER.API.INVALID.VALUE.SPECIFIED.FOR.LOCK.TIMEOUT", new Object[] {
						timeoutStr, e.toString() }));
				mTimeout = LOCK_TIMEOUT_MINUTES;
				APIEngine.logError(sResHash.getString("SEVER.API.WILL.USE.DEFAULT.VALUE.FOR.LOCK.TIMEOUT", new Object[] { String
						.valueOf(mTimeout) }));
			}
		} else {
			mTimeout = LOCK_TIMEOUT_MINUTES;
			APIEngine.logError(sResHash.getString("SEVER.API.WILL.USE.DEFAULT.VALUE.FOR.LOCK.TIMEOUT", new Object[] { String
					.valueOf(mTimeout) }));
		}

	}

	/**
	 * Starts a new thread that on a given interval unlock configurations
	 * checked out for a long time
	 */
	public void startAutoUnlock() {
		if (mTimeout > 0) {
			mAutoUnlock = new AutoUnlock();
			mAutoUnlock.start();
		}
	}

	/**
	 * This method is used to release the lock of a configuration file.
	 * 
	 * @param configToken
	 *            a configuration file path relative to the configuration
	 *            codebase folder or a Solution Name.
	 * @param mIdentity
	 *            the user {@link Identity} object with enough authorities to
	 *            unlock the configuration
	 * @return true if the file was previously locked, false otherwise.
	 * 
	 * @throws DIException
	 *             if an error occurs.
	 */
	public boolean releaseConfigurationLock(String configToken, Identity identity) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		// local SessionImpl will check for Admin rights
		if (!isConfigurationCheckedOut(relativePath)) {
			return false;
		}
		removeFromRegistry(relativePath);

		notifyConfigurationListeners(new ConfigEvent(Type.UNLOCK, relativePath, identity.getUserId()));

		return true;
	}

	/**
	 * This method is used to release the lock of a previously checked out
	 * configuration file.
	 * 
	 * @param configToken
	 *            a configuration file path relative to the configuration
	 *            codebase folder or a Solution Name.
	 * @param identity
	 *            the user {@link Identity} object used to verify the user that
	 *            have checked out the configuration.
	 * 
	 * @return true if the file was previously locked, false otherwise.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public boolean undoCheckOut(String configToken, Identity identity) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		if (!isConfigurationCheckedOut(relativePath, identity)) {
			return false;
		}
		removeFromRegistry(relativePath);

		notifyConfigurationListeners(new ConfigEvent(Type.UNLOCK, relativePath, identity.getUserId()));

		return true;
	}

	/**
	 * Returns a list of all configurations in the specified folder. If a
	 * configuration has a Solution Name, this name appears in the list,
	 * otherwise in the list appears the file path of the configuration. The
	 * configurations file paths returned are relative to the Server
	 * configuration codebase folder. The returned list is based on information,
	 * gathered by the Server on startup. If a new configuration file is added
	 * in the configuration codebase folder when the Server is already running,
	 * that configuration will not be listed by the method.
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list of all configurations in the specified folder.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 */
	public ArrayList<String> listConfigurations(String aRelativePath) throws DIException {

		verifyRelativePath(aRelativePath);
		ArrayList<String> configArray = new ArrayList<String>();
		String folderPath = aRelativePath;
		if (folderPath == null) {
			folderPath = mRootPath;
		} else {
			folderPath = mRootPath + aRelativePath;
		}

		File rootDir = new File(folderPath);
		if (rootDir.isDirectory()) {

			String rootDirCanonicalPath = getCanonicalPath(rootDir);

			synchronized (configFileAndSolutionNameMappingsLock) {
				Iterator<String> configIter = configFileToSolutionNameMap.keySet().iterator();
				while (configIter.hasNext()) {
					File configFile = new File((String) configIter.next());
					String configFileParentCanonicalPath = getCanonicalPath(configFile.getParentFile());

					if (rootDirCanonicalPath.equals(configFileParentCanonicalPath) && getRelativePath(configFile) != null) {
						configArray.add(getConfigToken(configFile));
					}
				}
			}
		} else if (rootDir.isFile()) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.INVALID.RELATIVE.FOLDER.PATH", folderPath));
		}

		return configArray;
	}

	/**
	 * Returns a list of the child folders of the specified folder.
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list of the child folders of the specified folder.
	 * @throws DIException
	 *             If an error occurs while retrieving child folder.
	 */
	public ArrayList<String> listFolders(String aRelativePath) throws DIException {

		verifyRelativePath(aRelativePath);
		ArrayList<String> folderNames = new ArrayList<String>();
		String folderPath = aRelativePath;
		if (folderPath == null) {
			folderPath = mRootPath;
		} else {
			folderPath = mRootPath + aRelativePath;
		}

		File rootDir = new File(folderPath);
		if (rootDir.isDirectory()) {
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					folderNames.add(getRelativePath(file));
				}
			}
		} else if (rootDir.isFile()) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.INVALID.RELATIVE.FOLDER.PATH.2", folderPath));
		}

		return folderNames;
	}

	/**
	 * Returns a list all configurations in the directory subtree of the Server
	 * configuration codebase folder. If a configuration has a Solution Name,
	 * this name appears in the list, otherwise in the list appears the file
	 * path of the configuration. The configurations file paths returned are
	 * relative to the TDI Server configuration codebase folder. The returned
	 * list is based on information, gathered by the Server on startup. If a new
	 * configuration file is added in the configuration codebase folder when the
	 * Server is already running, that configuration will not be listed by the
	 * method.
	 * 
	 * @return A list of all configurations from the whole configuration
	 *         codebase directory subtree.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 */
	public ArrayList<String> listAllConfigurations() throws DIException {

		ArrayList<String> configArray = new ArrayList<String>();

		synchronized (configFileAndSolutionNameMappingsLock) {
			Iterator<String> configIter = configFileToSolutionNameMap.keySet().iterator();
			while (configIter.hasNext()) {
				File configFile = new File((String) configIter.next());
				if(configFile.exists())
					configArray.add(getConfigToken(configFile));
			}
		}

		return configArray;
	}

	/**
	 * Checks out the specified configuration. Returns the MetamergeConfig
	 * object representing the configuration and locks that configuration on the
	 * Server.
	 * 
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public MetamergeConfig checkOutConfiguration(String configToken, Identity identity) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		return checkOutConfiguration(relativePath, null, identity);
	}

	/**
	 * Checks out the specified password protected configuration. Returns the
	 * MetamergeConfig object representing the configuration and locks that
	 * configuration on the Server.
	 * 
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param password
	 *            Specify the password for password protected configurations.
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public MetamergeConfig checkOutConfiguration(String configToken, String password, Identity identity) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		verifyRelativePath(relativePath);
		if (isConfigurationCheckedOut(relativePath)) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.CONFIGURATION.ALREADY.CHECKED.OUT", relativePath));
		}

		MetamergeConfig mc = null;

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(javax.naming.Context.PROVIDER_URL, mRootPath + relativePath);
		env.put(MetamergeConfigFactory.MC_CREATE, "false");

		if (password != null) {
			env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
			APIEngine.logDebug(sResHash.getString("SERVER.API.PASSWORD.PROTECTED.CONFIGURATION.OPEN", relativePath));
		}

		try {
			mc = MetamergeConfigFactory.getInstance(env);
		} catch (com.ibm.di.exceptions.PasswordException pe) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.CONFIGURATION.IS.PASSWORD.PROTECTED", relativePath),
					pe);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.WHILE.LOADING.CONFIGURATION", relativePath), e);
		}

		// add to registry
		addToRegistry(relativePath, identity, false);

		notifyConfigurationListeners(new ConfigEvent(Type.CHECK_OUT, relativePath, identity.getUserId()));

		return mc;
	}

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @param session
	 *            this is the reference to the {@link Session} object used to
	 *            start the confiInstance object.
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String configToken, Identity identity, SessionImpl session)
			throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		return checkOutConfigurationAndLoad(relativePath, null, identity, session);
	}

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param password
	 *            Specify the password for password protected configurations.
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @param session
	 *            this is the reference to the {@link Session} object used to
	 *            start the confiInstance object.
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String configToken, String password, Identity identity, SessionImpl session)
			throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		verifyRelativePath(relativePath);
		if (isConfigurationCheckedOut(relativePath)) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.CONFIGURATION.ALREADY.CHECKED.OUT.2", relativePath));
		}

		String configPath = mRootPath + relativePath;
		String runName = getRunName(relativePath);
		ConfigInstance ci = session.startConfigInstance(configPath, true, password, runName, null);

		// add to registry
		addToRegistry(relativePath, identity, true);

		notifyConfigurationListeners(new ConfigEvent(Type.CHECK_OUT, relativePath, identity.getUserId()));

		return ci;
	}

	/**
	 * Saves the specified configuration and releases the lock. If a temporary
	 * ConfigInstance has been started on check out, it will be stopped as well.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInConfiguration(MetamergeConfig configuration, String configToken, Identity identity) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		checkInConfiguration(configuration, relativePath, identity, false);
	}

	/**
	 * Encrypts and saves the specified configuration and releases the lock. If
	 * a temporary Config Instance has been started on check out, it will be
	 * stopped as well.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @param encrypt
	 *            If set to true, the configuration will be encrypted on the
	 *            Server.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInConfiguration(MetamergeConfig configuration, String configToken, Identity identity, boolean encrypt)
			throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		if (!isConfigurationCheckedOut(relativePath, identity)) {
			APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATION.IS.NOT.CHECKED.OUT", relativePath));
			throw new ConfigurationNotCheckedOutException();
		}
		saveConfiguration(configuration, relativePath, encrypt);
		removeFromRegistry(relativePath);

		notifyConfigurationListeners(new ConfigEvent(Type.CHECK_IN, relativePath, identity.getUserId()));
	}

	/**
	 * Checks in the specified configuration and leaves it checked out. The
	 * timeout for the lock on the configuration is reset.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @param encrypt
	 *            If set to true, the configuration will be encrypted on the
	 *            Server.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String configToken, Identity identity) throws DIException {
		checkInAndLeaveCheckedOut(configuration, configToken, identity, false);
	}

	/**
	 * Checks in the specified configuration and leaves it checked out. The
	 * timeout for the lock on the configuration is reset.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String configToken, Identity identity, boolean encrypt)
			throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		if (!isConfigurationCheckedOut(relativePath, identity)) {
			APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATION.IS.NOT.CHECKED.OUT.2", relativePath));
			throw new ConfigurationNotCheckedOutException();
		}

		saveConfiguration(configuration, relativePath, encrypt);

		synchronized (mRegistryLock) {
			Map<String, Object> configInfo = mRegistry.get(relativePath);
			if (((Boolean) configInfo.get(KEY_LOAD)).booleanValue()) {
				reloadConfigInstance(relativePath);
			}
		}

		updateLockTime(relativePath);

		notifyConfigurationListeners(new ConfigEvent(Type.CHECK_IN_LOCKED, relativePath, identity.getUserId()));
	}

	/**
	 * Creates a new empty configuration and immediately checks it out. If a
	 * configuration with the specified path already exists and the aOverwrite
	 * parameter is set to false the operation will fail and an Exception will
	 * be thrown.
	 * 
	 * @param aRelativePath
	 *            The path of the new configuration file relative to the Server
	 *            configuration codebase folder.
	 * @param aOverwrite
	 *            Specify whether to overwrite or not an already exising
	 *            configuration file.
	 * @param aIdentity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @return The MetamergeConfig object representing the newly created
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 */
	public MetamergeConfig createNewConfiguration(final String aRelativePath, boolean aOverwrite, final Identity aIdentity)
			throws DIException {

		MetamergeConfig mc = createNewConfigFile(aRelativePath, aOverwrite);

		addToRegistry(aRelativePath, aIdentity, false);

		notifyConfigurationListeners(new ConfigEvent(Type.CREATE, aRelativePath, aIdentity.getUserId()));

		return mc;
	}

	/**
	 * Creates a new empty configuration, immediately checks it out and loads a
	 * temporary Config Instance on the Server. If a configuration with the
	 * specified path already exists and the aOverwrite parameter is set to
	 * false the operation will fail and an Exception will be thrown.
	 * 
	 * @param aRelativePath
	 *            The path of the new configuration file relative to the Server
	 *            configuration codebase folder.
	 * @param aOverwrite
	 *            Specify whether to overwrite or not an already existing
	 *            configuration file.
	 * @param aIdentity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @param aSession
	 *            the {@link Session} object used to start the configInstance.
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 */
	public ConfigInstance createNewConfigurationAndLoad(String aRelativePath, boolean aOverwrite, Identity aIdentity,
			SessionImpl aSession) throws DIException {

		createNewConfigFile(aRelativePath, aOverwrite);

		ConfigInstance ci = checkOutConfigurationAndLoad(aRelativePath, aIdentity, aSession);

		notifyConfigurationListeners(new ConfigEvent(Type.CREATE_LOCKED, aRelativePath, aIdentity.getUserId()));

		return ci;
	}

	/**
	 * Returns the value of api.config.folder property.
	 * 
	 * @return The canonical path of the config folder. If api.config.folder is
	 *         NOT defined then return an empty string.
	 */
	public String getConfigFolderPath() {
		if (System.getProperty(PROPERTY_ROOT_PATH) != null) {
			if (mRootPathCanonical != null)
				return mRootPathCanonical;
			else
				return mRootPath;
		}
		// Property not defined.
		return "";
	}

	// ********************************************************************
	// Private methods
	// ********************************************************************

	/**
	 * Creates a new empty configuration and immediately checks it out.
	 * 
	 * @param aRelativePath
	 *            The path of the new configuration file relative to the Server
	 *            configuration codebase folder.
	 * @param aOverwrite
	 *            Specify whether to overwrite or not an already exising
	 *            configuration file.
	 * @return MetamergeConfig instance
	 * @throws DIException
	 *             If a configuration with the specified path already exists and
	 *             the aOverwrite parameter is set to false the operation will
	 *             fail and an Exception will be thrown.
	 */
	private MetamergeConfig createNewConfigFile(String aRelativePath, boolean aOverwrite) throws DIException {

		verifyRelativePath(aRelativePath);
		File file = new File(mRootPath + aRelativePath);
		if (!aOverwrite && file.exists()) {
			APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATION.FILE.ALREADY.EXISTS", aRelativePath));
			throw new ConfigurationExistsException();
		}

		if (isConfigurationCheckedOut(aRelativePath)) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.CONFIGURATION.ALREADY.CHECKED.OUT.3", aRelativePath));
		}

		String parent = file.getParent();
		if (parent != null) {
			File parentFolder = new File(parent);
			// Make an attempt to create the parent directory if it does not
			// exist.
			if (!parentFolder.exists())
				// We will not try to walk up the tree to create any parent
				// directories
				// that do not exist and will ignore the output of trying to
				// create the
				// parent directory since the error may be the fact we don't
				// have
				// access to see the parent directory.
				parentFolder.mkdirs();
		}

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
		env.put(Context.PROVIDER_URL, mRootPath + aRelativePath);
		env.put(MetamergeConfigFactory.MC_CREATE, "true");

		MetamergeConfig mc = null;
		try {
			mc = MetamergeConfigFactory.getInstance(env);
			mc.commitChanges(null);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.LOAD.CONFIGURATION", aRelativePath), e);
		}

		return mc;
	}

	/**
	 * Recursively constructs a list of all files in a specified directory. The
	 * list contains only files (no directories) as java.io.File objects.
	 * 
	 * @param aRootDirName
	 *            Path to a root directory.
	 * @return The list of files inside the root directory.
	 * @throws DIException
	 *             If the specified path does not point to a directory.
	 */
	private List<File> recurseDirs(String aRootDirName) throws DIException {

		List<File> allFiles = new ArrayList<File>();

		File rootDir = new File(aRootDirName);
		if (rootDir.isDirectory()) {
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					allFiles.add(file);
				} else if (file.isDirectory()) {
					List<File> children = recurseDirs(file.getPath());
					allFiles.addAll(children);
				}
			}
		} else {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.RECURSEDIRS.INVALID.FOLDER.PATH.ROOT.DIR.NAME",
					aRootDirName));
		}

		return allFiles;
	}

	/**
	 * Checks if the specified configuration is checked out on the Server.
	 * 
	 * @param configToken
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return true if the specified configuration is checked out, false
	 *         otherwise.
	 * @throws DIException
	 *             If an error occurs while checking the configuration.
	 */
	public boolean isConfigurationCheckedOut(String configToken) throws DIException {

		String relativePath = getRelativeConfigFilePath(configToken);

		synchronized (mRegistryLock) {
			if (mRegistry.containsKey(relativePath)) {
				return true;
			} else {
				Iterator<String> iter = mRegistry.keySet().iterator();
				while (iter.hasNext()) {
					String regPath = iter.next();
					regPath = regPath.replace(File.separatorChar, '_');
					if (regPath.equalsIgnoreCase(relativePath)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	/**
	 * Checks whether the configuration has been checked out.
	 * 
	 * @param aRelativePath
	 *            The configuration file path, relative to the configuration
	 *            codebase folder
	 * @param aIdentity
	 *            the {@link Identity} object used to verify the user's
	 *            authorities.
	 * @return <code>true</code> if configurations has been checked out.
	 * @throws DIException
	 */
	private boolean isConfigurationCheckedOut(String aRelativePath, Identity aIdentity) throws DIException {

		String userId = null;

		synchronized (mRegistryLock) {
			Map<String, Object> configLockInfo = mRegistry.get(aRelativePath);
			if (configLockInfo == null) {
				Iterator<String> iter = mRegistry.keySet().iterator();
				while (iter.hasNext()) {
					String regPath = iter.next();
					String regPathTmp = regPath.replace(File.separatorChar, '_');
					regPathTmp = regPathTmp.replace('/', '_');
					if (regPathTmp.equalsIgnoreCase(aRelativePath)) {
						configLockInfo = mRegistry.get(regPath);
						break;
					}
				}
				if (configLockInfo == null) {
					return false;
				}
			}
			userId = (String) configLockInfo.get(KEY_IDENTITY);
		}

		if (userId == null) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.INTERNAL.ERROR.NULL.USER.ID.IN.CONFIGURATION.REGISTRY", aRelativePath));
		}

		return true;
	}

	/**
	 * Adds the relative path , the {@link Identity} object and the value of the
	 * load parameter to the registry.
	 * 
	 * @param aRelativePath
	 *            the relative path.
	 * @param aIdentity
	 *            {@link Identity} instance
	 * @param aLoad
	 *            <code>true</code> loading , <code>false</code> writing
	 * @throws DIException
	 *             if an error occurs.
	 */
	private void addToRegistry(String aRelativePath, Identity aIdentity, boolean aLoad) throws DIException {

		Map<String, Object> configInfo = new TreeMap<String, Object>();
		String userId = aIdentity.getUserId();
		configInfo.put(KEY_IDENTITY, userId);
		configInfo.put(KEY_CHECKOUT_TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
		configInfo.put(KEY_LOAD, Boolean.valueOf(aLoad));

		synchronized (mRegistryLock) {
			mRegistry.put(aRelativePath, configInfo);
		}
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATION.LOCKED.BY", new Object[] { aRelativePath,
					aIdentity.getUserId() }));
		}
	}

	/**
	 * Removes an entry with the specified relative path from the registry.
	 * 
	 * @param aRelativePath
	 *            the relative path.
	 * @throws DIException
	 *             if an error occurs.
	 */
	private void removeFromRegistry(String aRelativePath) throws DIException {

		synchronized (mRegistryLock) {
			Map<String, Object> configInfo = mRegistry.get(aRelativePath);
			if (((Boolean) configInfo.get(KEY_LOAD)).booleanValue()) {
				stopConfigInstance(aRelativePath);
			}
			mRegistry.remove(aRelativePath);
		}
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATION.LOCK.RELEASED", aRelativePath));
		}

	}

	/**
	 * Updates the time the configuration is checked out. The configuration is
	 * specified by the <code>path</code> parameter. This time is set to the
	 * current time as if the configuration was just checked out.
	 * 
	 * @param path
	 *            the configuration path.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public void updateLockTime(String path) throws DIException {

		path = getRelativeConfigFilePath(path);

		Map<String, Object> configInfo = null;
		synchronized (mRegistryLock) {
			configInfo = mRegistry.get(path);
			if (configInfo != null) {
				configInfo.put(KEY_CHECKOUT_TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
			}
		}
		if ((configInfo != null) && (APIEngine.isDebugEnabled())) {
			APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATION.LOCK.TIME.RESET", path));
		}
	}

	/**
	 * Saves the configuration to the provided relative path.
	 * 
	 * @param aConfiguration
	 *            {@link MetamergeConfig} instance.
	 * @param aRelativePath
	 *            relative path of the configuration.
	 * @param aEncrypt
	 *            is file encrypted.
	 * @throws DIException
	 */
	private void saveConfiguration(MetamergeConfig aConfiguration, String aRelativePath, boolean aEncrypt) throws DIException {

		/*
		 * Update the Solution Names cache. This must happen before the
		 * confguration is serialized since a detection of duplicate Solution
		 * Names is possible. In a case of a duplicated Solution Name, the
		 * configuration must not be saved.
		 */
		String solutionName = null;
		if (null != aConfiguration.getSolutionInterface()) {
			solutionName = aConfiguration.getSolutionInterface().getInstanceID();
			if (solutionName != null && solutionName.trim().length() == 0) {
				solutionName = null;
			}
		}
		File configFile = new File(mRootPath + aRelativePath);
		updateSolutionNamesCache(configFile, solutionName, checkHasActiveSchedules(aConfiguration));

		try {
			aConfiguration.setDriverParameter(Context.PROVIDER_URL, mRootPath + aRelativePath);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(
					sResHash.getString("SEVER.API.COULD.NOT.SET.CONFIGURATION.PATH.FOR", aRelativePath), e);
		}

		if (aConfiguration.isCommittable()) {
			try {
				if (aEncrypt) {
					aConfiguration.setDriverParameter(MetamergeConfigFactory.MC_ENCRYPT, "true");
				}
				aConfiguration.commitChanges(null);
				if(solutionName != null)
					MetamergeConfigFactory.removeNamespace(solutionName);
				else
					MetamergeConfigFactory.removeNamespace(configFile.getAbsolutePath());
			} catch (Exception e) {
				APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.SAVE.CONFIGURATION", aRelativePath), e);
			}
			String configId = com.ibm.di.api.syslog.LogUtils.getCleanConfigId(mRootPath + aRelativePath);
			APIEngine.sendNotification(DIEvent.EVT_CI_UPDATED, configId, null, solutionName == null ? configId : solutionName);
		} else {
			throw new DIException(sResHash.getString("SEVER.API.CONFIGURATION.IS.NOT.COMMITTABLE", aRelativePath));
		}
	}

	/**
	 * Raises shutdown request and thus stops the configuration instance.
	 * 
	 * @param aRelativePath
	 *            The configuration file path, relative to the configuration
	 *            codebase folder
	 * @throws DIException
	 *             if an error occurs.
	 */
	private void stopConfigInstance(String aRelativePath) throws DIException {

		String configId = getRunName(aRelativePath);
		RSInterface configInstance = APIEngine.getConfigInstance(configId);

		if (configInstance != null) {
			configInstance.shutdownServer();
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.CONFIGINSTANCE.STOPPED", aRelativePath));
			}
		} else {
			APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.UNABLE.TO.STOP", aRelativePath));
		}
	}

	/**
	 * This method reloads the configuration file.
	 * 
	 * @param aRelativePath
	 *            The configuration file path , relative to the configuration
	 *            codebase folder.
	 * @throws DIException
	 *             if an error occurs.
	 */
	private void reloadConfigInstance(String aRelativePath) throws DIException {

		String configId = getRunName(aRelativePath);
		RSInterface configInstance = APIEngine.getConfigInstance(configId);

		if (configInstance != null) {
			try {
				configInstance.reload();
				if (APIEngine.isDebugEnabled()) {
					APIEngine
							.logDebug(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.CONFIGINSTANCE.RELOADED", aRelativePath));
				}
			} catch (Exception e) {
				APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.UNABLE.TO.RELOAD", new Object[] {
						aRelativePath, e.toString() }));
			}
		} else {
			APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.UNABLE.TO.RELOAD.2", aRelativePath));
		}
	}

	/**
	 * 
	 * Check that the specified user can set the configuration.
	 * 
	 * @param configId
	 *            the configuration id used to identify the configInstance
	 * @param identity
	 *            the {@link Identity} object used to verify the user's rights.
	 * @return true if the user have the necessary rights for the operation,
	 *         false otherwise.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public boolean userCanSetConfiguration(String configId, Identity identity) throws DIException {

		boolean ret = false;

		if (configId != null && configId.startsWith(CM_PREFIX)) {

			RSInterface configInstance = APIEngine.getConfigInstance(configId);

			if (configInstance != null) {

				String configPath = configInstance.getConfigPath();
				String relativePath = getRelativePath(new File(configPath));

				ret = isConfigurationCheckedOut(relativePath, identity);
			}
		}

		return ret;
	}

	/**
	 * This class is used to create new threads , which unlock configurations
	 * checked out for a long time.
	 */
	private class AutoUnlock extends Thread {

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			try {
				while (true) {
					try {
						Thread.sleep(AUTO_UNLOCK_SLEEP_INTERVAL);
					} catch (InterruptedException e) {
						if (APIEngine.isDebugEnabled()) {
							APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.AUTO.UNLOCK.THREAD.INTERUPTED",
									e.toString()));
						}
					}
					unlock();
				}
			} catch (Exception e) {
				APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.AUTO.UNLOCK.FATAL.ERROR", e.toString()));
			}
		}
	}

	/**
	 * Unlocks configurations checked out for a long time.
	 */
	private void unlock() {

		long timeout = 1000L * 60 * mTimeout;
		long currentTime = System.currentTimeMillis();

		synchronized (mRegistryLock) {
			Iterator<String> keys = mRegistry.keySet().iterator();
			while (keys.hasNext()) {
				String configId = (String) keys.next();
				Map<String, Object> configInfo = mRegistry.get(configId);
				long checkOutTime = ((Long) configInfo.get(KEY_CHECKOUT_TIMESTAMP)).longValue();
				if ((checkOutTime + timeout) < currentTime) {
					if (((Boolean) configInfo.get(KEY_LOAD)).booleanValue()) {
						try {
							stopConfigInstance(configId);
						} catch (DIException e) {
							APIEngine.logError(sResHash.getString("SEVER.API.CONFIGURATIONREGISTRY.ERROR.STOPPING.CONFIGURATION",
									new Object[] { configId, e.toString() }));
						}
					}
					keys.remove();
					if (APIEngine.isDebugEnabled()) {
						APIEngine.logDebug(sResHash
								.getString("SEVER.API.CONFIGURATIONREGISTRY.AUTO.UNLOCK.CONFIGURATION", configId));
					}
				}
			}
		}
	}

	/**
	 * Verifies whether the provided path is relative .
	 * 
	 * @param aRelativePath
	 *            path to be checked.
	 * @throws DIException
	 *             if the path is not valid.
	 */
	private void verifyRelativePath(String aRelativePath) throws DIException {

		File file = new File(aRelativePath);
		if (file.isAbsolute()) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ABSOLUTE.PATH.PROVIDED.EXPECTING.RELATIVE",
					aRelativePath));
		}

		String relative = getCanonicalPath(new File(mRootPath + aRelativePath));

		if (!relative.startsWith(mRootPathCanonical)) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.PATH.PROVIDED.IS.NOT.ALLOWED", aRelativePath));
		}
	}

	/**
	 * Reads the Solution Names of all configurations inside the configuration
	 * codebase folder and updates the Solution Names cache.
	 * 
	 * @throws DIException
	 *             If the configuration codebase folder cannot be listed or
	 *             duplication of Solution Names is detected or failed retrieval
	 *             of the canonical path of a file or Solution Name coincides
	 *             with the path of an existing file.
	 * @since 6.1.1
	 */
	private void parseSolutionNamesOfAllConfigurations() throws DIException {

		Iterator<File> configIter = listAllFilesInConfigsFolder().iterator();
		while (configIter.hasNext()) {
			File configFile = configIter.next();
			
			//
			// Skip dot files
			//
			if(configFile.getName().startsWith(".")) {
				continue;
			}
			if (!UserFunctions.endsWithIC(configFile.getName(), ".xml")) {
			    continue;
			}
			
			String solutionName = readSolutionName(configFile);
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString("SEVER.API.CONFIGURATION.REGISTRY.PARSED.FILE.FOR.SOLUTION.NAME",
						new Object[] { configFile.getAbsolutePath(), solutionName }));
			}
		}
	}

	/**
	 * Searches for a Solution Name inside a TDI XML configuration file and
	 * updates the Solution Names cache.
	 * 
	 * @param configFile
	 *            A TDI XML configuration file.
	 * @return The Solution Name of the TDI configuration (if any); null if the
	 *         file is not a valid TDI configuration or has no Solution Name.
	 * @exception DIException
	 *                : Failed retrieval of the canonical path of a file or
	 *                duplicate Solution Name or Solution Name coincides with
	 *                the path of an existing file.
	 * 
	 * @since 6.1.1
	 */
	private String readSolutionName(File configFile) throws DIException {

		String solutionName = null;
		boolean isValidTDIConfig = false;
		boolean hasActiveSchedules = false;

		// .cfg files are assumed to be valid old-style TDI configs.

		if (UserFunctions.endsWithIC(getCanonicalPath(configFile), ".cfg")) {

			solutionName = null; // old-style TDI configurations have no
			// Solution Names
			isValidTDIConfig = true;

		} else {

			SolutionNameFinder solutionNameFinder = new SolutionNameFinder(configFile);
			solutionName = solutionNameFinder.getSolutionName();
			if (solutionName != null && solutionName.trim().length() == 0) {
				solutionName = null;
			}
			isValidTDIConfig = solutionNameFinder.isValidTDIConfig();
			hasActiveSchedules = solutionNameFinder.hasActiveSchedules();
		}

		/*
		 * Update the Solution Names cache only for valid TDI configurations
		 * that are located inside the configurations code-base folder.
		 */
		if (isValidTDIConfig && getRelativePath(configFile) != null) {
			updateSolutionNamesCache(configFile, solutionName, hasActiveSchedules);
		}

		return solutionName;
	}

	/**
	 * This class extracts a Solution Name from a TDI configuration XML file.
	 * 
	 * @since 6.1.1
	 */
	private static class SolutionNameFinder {

		/**
		 * Name of the solution.
		 */
		private String solutionName = null;

		/**
		 * Flag for valid TDI XML configuration.
		 */
		private boolean isValidTDIXMLConfig = false;
		
		/**
		 * Flag for active schedule(s)
		 */
		private boolean hasActiveSchedules = false;

		/**
		 * Constructs the {@link SolutionNameFinder} object responsible for
		 * extracting the solution name out of the XML config file.
		 * 
		 * @param configFile
		 *            the config file containing the solution name.
		 */
		public SolutionNameFinder(File configFile) {

			try {
				solutionName = readSolutionNameFromFile(configFile);
				isValidTDIXMLConfig = true;
			} catch (com.ibm.di.exceptions.PasswordException pe) {
				APIEngine.logDebug(sResHash
						.getString("SERVER.API.PASSWORD.PROTECTED.CONFIGURATION.DETECTED", configFile.toString()));
				isValidTDIXMLConfig = true;
			} catch (Exception e) {
					APIEngine.logDebug(sResHash.getString("SEVER.API.ERROR.WHILE.LOADING.CONFIGURATION", configFile.toString()
							+ " " + e));
					return;
			}

			try {
				// at this point the config should be registered in the namespace by readSolutionNameFromFile
				hasActiveSchedules = checkHasActiveSchedules(MetamergeConfigFactory.getNamespace(configFile.getCanonicalPath()));
			} catch (Exception e) {
				hasActiveSchedules = false;
			}
		}

		/**
		 * @return true if the parsed XML config file is valid one, false
		 *         otherwise.
		 */
		public boolean isValidTDIConfig() {
			return isValidTDIXMLConfig;
		}

		/**
		 * @return the solution name found in the XML config file,
		 *         <code>null</code> if a solution name is not set.
		 */
		public String getSolutionName() {
			return solutionName;
		}
		
		/**
		 * @return the active schedules flag
		 */
		public boolean hasActiveSchedules() {
			return hasActiveSchedules;
		}

		/**
		 * Retrieve the Solution Name of a configuration file.
		 * 
		 * @param configFile
		 *            A configuration file.
		 * @return The Solution Name if any or null otherwise.
		 * @throws Exception
		 *             Parsing error.
		 */
		public static String readSolutionNameFromFile(File configFile) throws Exception {

			MetamergeConfig mc = null;
			Hashtable<String, String> env = new Hashtable<String, String>();

			env.put(javax.naming.Context.PROVIDER_URL, configFile.getCanonicalPath());
			env.put(MetamergeConfigFactory.MC_CREATE, "false");
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			mc = MetamergeConfigFactory.getInstance(env);

			String solutionName = null;
			if (mc.getSolutionInterface() != null) {
				solutionName = mc.getSolutionInterface().getInstanceID();
			}
			
			return solutionName;
		}

		/**
		 * Retrieve the Solution Name of a configuration.
		 * 
		 * @param xmlConfig
		 *            A configuration as a XML string.
		 * @return The Solution Name if any or null otherwise.
		 * @throws Exception
		 *             Parsing error.
		 */
		public static String readSolutionNameFromMemory(String xmlConfig) throws Exception {

			MetamergeConfig mc = null;
			Hashtable<String, Object> env = new Hashtable<String, Object>();

			env.put(javax.naming.Context.PROVIDER_URL, new ByteArrayInputStream(xmlConfig.getBytes()));
			env.put(MetamergeConfigFactory.MC_CREATE, "false");
			env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
			env.put(MetamergeConfigFactory.MC_ENCRYPT, "false");
			mc = MetamergeConfigFactory.getInstance(env);

			String solutionName = null;
			if (mc.getSolutionInterface() != null) {
				solutionName = mc.getSolutionInterface().getInstanceID();
			}

			return solutionName;
		}

	}

	/**
	 * Retrieves the Solution Name of a TDI configuration file in the
	 * configuration codebase folder. The Solution Name is looked up in the
	 * internal Solution Names cache. If the specified configuration file cannot
	 * be found in the cache, it is parsed for its Solution Name. If the
	 * Solution Name is a duplicate, an exception is thrown, otherwise the
	 * Solution Names cache is updated.
	 * 
	 * @param configFile
	 *            A TDI configuration file in the configuration codebase folder.
	 * @return The Solution Name of the specified configuration file.
	 * @throws DIException
	 *             if detected a duplication of Solution Names or failed
	 *             retrieval of the canonical path of a file.
	 */
	public String getSolutionName(File configFile) throws DIException {

		String solutionName = null;

		String configFileCanonical = getCanonicalPath(configFile);

		synchronized (configFileAndSolutionNameMappingsLock) {

			if (configFileToSolutionNameMap.containsKey(configFileCanonical)) {

				solutionName = (String) configFileToSolutionNameMap.get(configFileCanonical);
			} else {

				// This file has not been parsed before - parse it now.
				solutionName = readSolutionName(configFile);
			}
		}

		return solutionName;
	}

	/**
	 * Resolves a token to a configuration file path. The token can be either a
	 * Solution Name of a configuration file in the configuration codebase
	 * folder or a configuration file path. The method never returns null as
	 * long as the token is not null. If the token is not a path of an existing
	 * file and is not a Solution Name of an existing file, the method assumes
	 * the token is a path of a non-existing file and returns it as it is.
	 * 
	 * @param token
	 *            A configuration file path or a Solution Name.
	 * @return The configuration file path, which corresponds to the provided
	 *         token.
	 * @exception DIException
	 *                if detected a duplication of Solution Names or failed
	 *                retrieval of the canonical path of a file or Solution Name
	 *                coincides with the path of an existing file.
	 * 
	 * @since 6.1.1
	 */
	public String getConfigFilePath(String token) throws DIException {

		// so far the api expects to see an absolute path.. check for that
		File configFile = new File(token);
		boolean tokenIsFileName = configFile.isFile();

		if (!tokenIsFileName && !configFile.isAbsolute()) {
			// recheck the token with rootPath
			configFile = new File(getConfigFolderPath(), token);
			tokenIsFileName = configFile.isFile();

			if (tokenIsFileName) {
				// rewrite token to the absolute path.
				token = configFile.getPath();
			}
		}

		String solutionName = !tokenIsFileName
		// amc sends: rootPath/solutionName, so don't bother if we know its
		// an existing file.
		? getSolutionNameFromAMC(token)
				: null;

		if (solutionName == null && !tokenIsFileName) {
			/*
			 * If the token is not pointing to an existing file, then interpret
			 * it as a Solution Name.
			 */
			solutionName = token;
		}

		String result = null;
		if (solutionName != null) {
			synchronized (configFileAndSolutionNameMappingsLock) {
				result = solutionNameToConfigFileMap.get(solutionName);
			}
		} else if (tokenIsFileName) {
			/*
			 * Prefetch the solution name of the configuration if not read yet,
			 * and check for duplication.
			 */
			getSolutionName(configFile);
			result = token;
		}

		if (result == null) {
			// Finally interpret the token as the path of a non-existing file.
			result = token;
		}

		return result;
	}

	/**
	 * Detects and normalized Solution Names received from AMC/CLI. AMC/CLI
	 * prefixes Solution Names with the path of the configuration codebase
	 * folder, so this method strips the path of the configuration codebase
	 * folder and returns just the Solution Name. If the specified token is not
	 * a Solution Name, which comes from AMC/CLI, then null is returned. The
	 * filter to recognize Solution Names from AMC/CLI is to match all of the
	 * following three conditions: - the token is an absolute file name; - no
	 * file exists under the absolute name, specified by the token; - the token
	 * starts with the path of the configuration codebase folder. The method
	 * does not check if the Solution Name actually exists.
	 * 
	 * @param token
	 *            A configuration file path or a Solution Name.
	 * 
	 * @return Solution Name, if the token comes from AMC/CLI and contains a
	 *         Solution Name, or otherwise - null.
	 */
	private String getSolutionNameFromAMC(String token) {

		String result = token;

		File filePathForToken = new File(token);
		String configFolderPath = getConfigFolderPath();

		boolean isSolutionNameFromAMC = filePathForToken.isAbsolute() && !filePathForToken.isFile()
				&& token.startsWith(configFolderPath);

		if (isSolutionNameFromAMC) {
			String solutionName = token.substring(configFolderPath.length());
			if (solutionName.startsWith("/") || solutionName.startsWith("\\")) {
				solutionName = solutionName.substring(1);
			}
			result = solutionName;

			APIEngine.logWarn(sResHash
					.getString("SERVER.API.DETECTED.SOLUTION.NAME.FROM.AMC", new Object[] { solutionName, token }));
		}

		return result;
	}

	/**
	 * Resolves a token to a relative configuration file path. The token can be
	 * either a Solution Name of a configuration file in the configuration
	 * codebase folder or a configuration file path, relative to the
	 * configuration codebase folder. The method never returns null as long as
	 * the token is not null. If the token is not a the path of an existing file
	 * and is not a Solution Name of an existing file, the method assumes the
	 * token is a path of a non-existing file and returns it as it is.
	 * 
	 * @param token
	 *            A configuration file path relative to the configuration
	 *            codebase folder or a Solution Name.
	 * @return The configuration file path, which corresponds to the provided
	 *         token, relative to the configuration codebase folder.
	 * @throws DIException
	 *             Failed retrieval of the canonical path of a file.
	 */
	private String getRelativeConfigFilePath(String token) throws DIException {

		// First try interpreting the token as a config path.
		String result = token;

		File configFile = new File(mRootPath + token);
		if (!configFile.exists()) {

			// If the token is not pointing to an existing file, then interpret
			// it as a Solution Name.
			synchronized (configFileAndSolutionNameMappingsLock) {
				String filePath = solutionNameToConfigFileMap.get(token);
				if (null != filePath) {
					result = getRelativePath(new File(filePath));
				}
			}
		}

		if (null == result) {
			// Finally interpret the token as the path of a non-existing file.
			result = token;
		}

		return result;
	}

	/**
	 * Recursively lists all files inside the the configuration codebase folder.
	 * The list contains java.io.File objects.
	 * 
	 * @return A list of all files inside the configuration codebase folder.
	 * @throws DIException
	 *             If the configuration codebase folder cannot be listed.
	 */
	private List<File> listAllFilesInConfigsFolder() throws DIException {

		List<File> configs = null;
		File rootDir = new File(mRootPath);
		if (rootDir.exists()) {
			configs = recurseDirs(mRootPath);
		}

		if (null == configs) {
			configs = new ArrayList<File>();
		}

		return configs;
	}

	/**
	 * If the specified config file has a Solution Name, then that name is
	 * returned, otherwise the path of the configuration, relative to the
	 * configuration codebase folder is returned. The returned token is never
	 * null for configuration files located in the configurations codebase
	 * folder.
	 * 
	 * @param configFile
	 *            A TDI configuration file in the configuration codebase folder.
	 * @return A token (relative path or a Solution Name), which identifies the
	 *         configuration.
	 * @exception DIException
	 *                if an error occurs.
	 */
	public String getConfigToken(File configFile) throws DIException {
		String token = getSolutionName(configFile);
		if (null == token) {
			token = getRelativePath(configFile);
		}

		return token;
	}

	/**
	 * Returns the path of the specified file, relative to the configuration
	 * codebase folder. The specified file must be inside the configuration
	 * codebase folder, otherwise null is returned. This means that you could
	 * use this method to test if a file is located inside the configurations
	 * codebase folder.
	 * 
	 * @param file
	 *            A file inside the configuration codebase folder.
	 * @return The relative path of the file.
	 * @throws DIException
	 *             Failed retrieval of the canonical path of a file.
	 */
	private String getRelativePath(File file) throws DIException {
		String result = null;

		String canonicalPath = getCanonicalPath(file);
		if (canonicalPath.startsWith(mRootPathCanonical)) {
			result = canonicalPath.substring(mRootPathCanonical.length());
			// Strip leading slash if any.
			if (result.startsWith("/") || result.startsWith("\\")) {
				result = result.substring(1);
			}
		}

		return result;
	}

	/**
	 * Updates the Solution Names cache with the specified mapping. If the
	 * specified config file has already been mapped, the old mapping is
	 * removed.
	 * 
	 * @param configFile
	 *            A TDI configuration file.
	 * @param solutionName
	 *            The solution name of the configuration file; null if the
	 *            configuration file has no Solution Name
	 * @throws DIException
	 *             Failed retrieval of the canonical path of a file or duplicate
	 *             Solution Name or Solution Name coincides with the path of an
	 *             existing file.
	 */
	private void updateSolutionNamesCache(File configFile, String solutionName, boolean hasActiveSchedules) throws DIException {

		String configFileCanonical = getCanonicalPath(configFile);

		synchronized (configFileAndSolutionNameMappingsLock) {

			if (null != solutionName) {

				// Check for duplicate Solution Names.
				String configFileWithTheSameSolutionName = solutionNameToConfigFileMap.get(solutionName);
				if (null != configFileWithTheSameSolutionName && !configFileCanonical.equals(configFileWithTheSameSolutionName)) {

					// There is another config file, which has the same Solution
					// Name.
					throw new DIException(sResHash.getString("SEVER.API.DUPLICATE.SOLUTION.NAME", new Object[] { solutionName,
							configFileWithTheSameSolutionName }));
				}

				verifySolutionNameNotSameAsAnotherFileName(configFile, solutionName);
			}

			// Clean-up old Solution Name Map entry (if any).
			String oldSolutionName = configFileToSolutionNameMap.get(configFileCanonical);
			if (null != oldSolutionName) {
				solutionNameToConfigFileMap.remove(oldSolutionName);
			}

			// Update the values.
			if (null != solutionName) {
				solutionNameToConfigFileMap.put(solutionName, configFileCanonical);
			}
			configFileToSolutionNameMap.put(configFileCanonical, solutionName);
		}
		
		synchronized(activeSchedulesConfigs) {
			int index = activeSchedulesConfigs.indexOf(configFile);
			if(hasActiveSchedules && index == -1)
				activeSchedulesConfigs.add(configFile);
			else if (!hasActiveSchedules)
				activeSchedulesConfigs.remove(configFile);
		}
	}

	/**
	 * Analog of java.io.File.getCanonicalPath , which throws DIException
	 * instead of IOException.
	 * 
	 * @param file
	 *            A file.
	 * @return The canonical path of the file.
	 * @throws DIException
	 *             Retrieving the canonical path failed.
	 * @see java.io.File#getCanonicalPath()
	 */
	private String getCanonicalPath(File file) throws DIException {
		String canonicalPath = null;

		try {
			canonicalPath = file.getCanonicalPath();
		} catch (java.io.IOException e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.CANT.GET.CANONICAL.PATH", new Object[] {
					file.getPath(), e }));
		}

		return canonicalPath;
	}

	/**
	 * Retrieves run name of the configuration file.
	 * 
	 * @param relativePath
	 *            A configuration file path relative to the configuration
	 *            codebase folder
	 * @return The corresponding run name of the configuration instance, when
	 *         the file is checked out and loaded.
	 * @since 7.0
	 */
	private String getRunName(String relativePath) {

		// Ensure the run name will not get rejected.
		return LogUtils.getCleanConfigId(CM_PREFIX + mRootPath + relativePath);
	}

	/**
	 * Delete a file from the configuration codebase folder.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param identity
	 *            the user idenity
	 * @throws DIException
	 *             The file is currently checked-out or deletion failed (e.g.
	 *             the file does not exist).
	 * @since 7.0
	 */
	public void deleteConfiguration(String relativePathOrSolutionName, Identity identity) throws DIException {

		if (isConfigurationCheckedOut(relativePathOrSolutionName)) {
			throw new DIException(sResHash.getString("SERVER.API.CANNOT.DELETE.CHECKED.OUT.FILE"));
		}

		String relativePath = getRelativeConfigFilePath(relativePathOrSolutionName);
		verifyRelativePath(relativePath);

		File file = new File(mRootPath + relativePath);

		synchronized (configFileAndSolutionNameMappingsLock) {

			boolean deleted = file.delete();

			if (!deleted) {
				throw new DIException(sResHash.getString("SERVER.API.CANNOT.DELETE.FILE", file.getAbsolutePath()));
			}

			/*
			 * clear the information about the file (if any) from the solution
			 * name cache
			 */
			String canonicalPath = getCanonicalPath(file);
			String solutionName = configFileToSolutionNameMap.get(canonicalPath);
			if (solutionName != null) {
				solutionNameToConfigFileMap.remove(solutionName);
			}
			configFileToSolutionNameMap.remove(canonicalPath);
		}

		notifyConfigurationListeners(new ConfigEvent(Type.DELETE, relativePath, identity.getUserId()));
	}

	/**
	 * Verify that the specified Solution Name is not the same as the file name
	 * of an existing file. Note that it is allowed to have a configuration file
	 * whose Solution Name is the same as its file name. The reason behind this
	 * check is to avoid ambiguity because some methods of the Server API accept
	 * both Solution Names and file names.
	 * 
	 * @param configFile
	 *            A TDI configuration file.
	 * @param solutionName
	 *            The Solution Name of the configuration file.
	 * @throws DIException
	 *             The Solution Name is the same as the file name of another
	 *             file.
	 * @since 7.0
	 */
	private void verifySolutionNameNotSameAsAnotherFileName(File configFile, String solutionName) throws DIException {

		String canonicalPath = getCanonicalPath(configFile);

		if (solutionName == null || solutionName.trim().length() == 0) {
			return;
		}

		/*
		 * Check both in the working folder of TDI and in the configurations
		 * codebase folder. The reason is that the 'startConfigInstance' methods
		 * accept Solution Names prefixed with the path of the codebase folder
		 * (this is how AMC gives them), stand-alone Solution Names and paths
		 * relative to the working folder.
		 */
		for (String otherFileName : new String[] { solutionName, mRootPath + solutionName }) {

			File otherFile = new File(otherFileName);

			if (otherFile.exists() && otherFile.isFile() && !getCanonicalPath(otherFile).equals(canonicalPath)) {

				throw new DIException(sResHash.getString("SERVER.API.SOLUTION.NAME.COINCIDES.WITH.EXISTING.FILE", new Object[] {
						solutionName, canonicalPath, otherFile.getAbsolutePath() }));
			}
		}
	}

	/**
	 * Get the Solution Name of a configuration.
	 * 
	 * @param xmlConfig
	 *            The configuration as a XML string.
	 * @return The Solution Name or null if there is none.
	 * @throws DIException
	 *             Error while parsing the configuration.
	 * @since 7.0
	 */
	static String getSolutionNameFromMemory(String xmlConfig) throws DIException {
		String solutionName;
		try {
			solutionName = SolutionNameFinder.readSolutionNameFromMemory(xmlConfig);
		} catch (Exception ex) {
			throw new DIException(sResHash.getString("SERVER.API.CANNOT.PARSE.CONFIG.FOR.SOLUTION.NAME", ex));
		}
		return solutionName;
	}

	public void addListener(ConfigurationFileListener listener) {
		eventSource.addListener(listener);
	}

	public ConfigurationFileListener removeListener(ConfigurationFileListener listener) {
		return eventSource.removeListener(listener);
	}

	private void notifyConfigurationListeners(final ConfigEvent ce) {
		eventSource.visitListeners(new Visitor<ConfigurationFileListener>() {
			public void visit(ConfigurationFileListener listener) {
				listener.handleEvent(ce);
			}
		});
	}
	
	public List<File> getConfigsWithActiveSchedules() {
		return this.activeSchedulesConfigs;
	}
	
	private static boolean checkHasActiveSchedules(MetamergeConfig mc) {
		// 
		// -- Check if solution has any enabled schedules with a valid assemblyline to run
		//
		boolean hasActiveSchedules = false;
		try {
			MetamergeFolder folder = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER);
			Enumeration<?> list = folder.list();
			while (list != null && list.hasMoreElements()) {
				Object o = list.nextElement();
				if(o instanceof Binding)
					o = ((Binding) o).getObject();
				if (o instanceof SchedulerConfig && ((SchedulerConfig) o).getEnabled()) {
					String scheduled = ((SchedulerConfig)o).getScheduledName();
					try {
						if (scheduled.contains( MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/"))
							mc.getSequence(scheduled);
						else
							mc.getAssemblyLine(scheduled);
						hasActiveSchedules = true;
						break;			
					} catch (Exception e) {
						APIEngine.logError(mc.toString() + ":" + ((SchedulerConfig)o).getShortName(), e);
					}
				}
			}
		} catch (Exception e) {
			return hasActiveSchedules;
		}
		return hasActiveSchedules;
	}
}
