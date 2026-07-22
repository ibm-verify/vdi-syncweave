/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.ConfigurationRegistry;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.ConfigurationFileListener;
import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.api.local.LogListener;
import com.ibm.di.api.local.SecurityRegistry;
import com.ibm.di.api.local.ServerInfo;
import com.ibm.di.api.local.SystemLog;
import com.ibm.di.api.local.SystemQueue;
import com.ibm.di.api.local.TombstoneManager;
import com.ibm.di.api.security.Identity;
import com.ibm.di.api.security.LocalIdentity;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class represents local Session and implements various methods which
 * could be used with the started TDI Server.
 */
public class SessionImpl implements com.ibm.di.api.local.Session {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Authenticated user's identity.
	 */
	private Identity mIdentity = null;

	/**
	 * Repository for manipulating configInstances.
	 */
	private ConfigurationRegistry mConfigRegistry = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "Session";

	/**
	 * A UserFunctions instance
	 */
	private UserFunctions userFunctions = new UserFunctions();
	
	/**
	 * Default constructor.
	 */
	public SessionImpl() {
		mIdentity = new LocalIdentity();
		mConfigRegistry = APIEngine.getConfigurationRegistry();

		String methodExtension = "createSession";

		try {
			APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), true, interfaceName,
					methodExtension, getServerInfo().getServerID(), null);
		} catch (DIException e) {
			sResHash.getString("SEVER.API.AUDIT.SEND.NOTIFICATION");
		}
	}

	/**
	 * Creates a SessionImpl by specified user id.
	 * 
	 * @param aUserId
	 *            the user id.
	 * @throws DIException
	 *             if an error occurs while creating a SessionImpl
	 */
	public SessionImpl(String aUserId) throws DIException {
		mIdentity = APIEngine.getIdentity(aUserId);
		String methodExtension = "createSession";
		if (mIdentity == null) {
			UserFunctions uf = new UserFunctions();
			String funcmsg = sResHash.getString("SEVER.API.USER.WITH.ID.NOT.AUTHORIZED", uf.splitString(aUserId, ";")[0]);
			APIEngine.logError(funcmsg);
			APIAuditor.sendSessionAuditData(aUserId, null, getServerInfo().getServerID(), false, interfaceName, methodExtension,
					getServerInfo().getServerID(), null);
			throw new AuthorizationException(funcmsg);
		}
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), true, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);
		mConfigRegistry = APIEngine.getConfigurationRegistry();
	}

	// -------------------------
	// Session interface methods
	// -------------------------

	/**
	 * {@inheritDoc}
	 */
	public ServerInfo getServerInfo() throws DIException {
		// everyone is allowed to execute this method

		return new ServerInfoImpl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance[] getConfigInstances() throws DIException {
		if (!(mIdentity.canReadAll() || mIdentity.canExecuteAll())) {
			throw new AuthorizationException();
		}

		Vector<RSInterface> rawConfigInstances = APIEngine.getConfigInstances();
		if (rawConfigInstances == null) {
			return null;
		}

		ConfigInstanceImpl[] configInstance = new ConfigInstanceImpl[rawConfigInstances.size()];
		for (int i = 0; i < rawConfigInstances.size(); i++) {
			ConfigInstanceImpl ci = new ConfigInstanceImpl((RS) rawConfigInstances.get(i), this);
			configInstance[i] = ci;
		}
		return configInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getConfigInstancesIDs() throws DIException {
		if (!(mIdentity.canReadAll() || mIdentity.canExecuteAll())) {
			throw new AuthorizationException();
		}

		return APIEngine.getConfigInstanceIDs();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance getConfigInstance(String aConfigId) throws DIException {
		if (!(mIdentity.canReadConfig(aConfigId) || mIdentity.canExecuteConfig(aConfigId))) {
			throw new AuthorizationException();
		}

		if (aConfigId == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONFIG.ID.IS.NULL"));
		}

		RS configInstance = (RS) APIEngine.getConfigInstance(aConfigId);

		if (configInstance == null) {
			return null;
		} else {
			return new ConfigInstanceImpl(configInstance, this);
		}
	}

	// access to running processes in all Server Config Instances

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine[] getAssemblyLines() throws DIException {
		if (!mIdentity.canExecuteAll()) {
			throw new AuthorizationException();
		}

		Hashtable<RSInterface, Vector<com.ibm.di.server.AssemblyLine>> rawAssembylLines = APIEngine.getAssemblyLines();
		if (rawAssembylLines == null) {
			return null;
		}

		ArrayList<AssemblyLineImpl> assemblyLines = new ArrayList<AssemblyLineImpl>();
		Enumeration<RSInterface> configInstances = rawAssembylLines.keys();
		while (configInstances.hasMoreElements()) {
			RS rawConfigInstance = (RS) configInstances.nextElement();
			Vector<com.ibm.di.server.AssemblyLine> configInstanceALs = rawAssembylLines.get(rawConfigInstance);
			if (configInstanceALs != null && configInstanceALs.size() > 0) {
				ConfigInstanceImpl configInstance = new ConfigInstanceImpl(rawConfigInstance, this);
				for (int i = 0; i < configInstanceALs.size(); i++) {
					AssemblyLineImpl al = new AssemblyLineImpl(configInstanceALs.get(i), configInstance, this);
					assemblyLines.add(al);
				}
			}
		}

		return assemblyLines.toArray(new AssemblyLine[assemblyLines.size()]);
	}

	// Operations

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configToken) throws DIException {
		return startConfigInstance(configToken, true, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configToken, boolean aKeepAlive, String aPassword) throws DIException {

		return startConfigInstance(configToken, aKeepAlive, aPassword, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps) throws DIException {

		return startConfigInstance(configPathOrSolutionName, keepAlive, password, runName, overrideProps, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigInstance(String aConfigUrl) throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.SUPPORTED.SERVER.API.CALL.CREATENEWCONFIGINSTANCE.1"));
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigInstance(String aConfigUrl, String aPassword) throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.SUPPORTED.SERVER.API.CALL.CREATENEWCONFIGINSTANCE.2"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer() throws DIException {
		String methodExtension = "shutDownServer";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath(),
				getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		Vector<RSInterface> configInstances = APIEngine.getConfigInstances();
		if (configInstances.size() > 0) {
			for (int i = 0; i < configInstances.size(); i++) {
				((RS) configInstances.get(i)).shutdownServer();
			}
		}
		RS.gRS.shutdownServer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer(int aExitCode) throws DIException {
		String methodExtension = "shutDownServer";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath(),
				getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		Vector<RSInterface> configInstances = APIEngine.getConfigInstances();

		if (configInstances.size() > 0) {
			for (int i = 0; i < configInstances.size(); i++) {
				((RS) configInstances.get(i)).shutdownServer();
			}
		}
		RS.gRS.shutdownServer(aExitCode);
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer(int aExitCode, boolean sync) throws DIException {
		String methodExtension = "shutDownServer";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath(),
				getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		RS.shutdownAllServers(aExitCode, true, sync);
	}

	// Security Registry

	/**
	 * {@inheritDoc}
	 */
	public SecurityRegistry getSecurityRegistry() throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return new SecurityRegistryImpl(this);
	}

	// System Log

	/**
	 * {@inheritDoc}
	 */
	public SystemLog getSystemLog() throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return new SystemLogImpl(this);
	}

	// Notifications

	/**
	 * {@inheritDoc}
	 */
	public void addEventListener(DIEventListener aListener, String aTypeFilter, String aIdFilter) throws DIException {
		APIEngine.addEventListener(aListener, aTypeFilter, aIdFilter);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeEventListener(DIEventListener aListener) throws DIException {
		return APIEngine.removeEventListener(aListener);
	}

	public void addEventListener(ConfigurationFileListener listener) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		mConfigRegistry.addListener(listener);
	}

	public boolean removeEventListener(ConfigurationFileListener listener) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}
		return mConfigRegistry.removeListener(listener) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TombstoneManager getTombstoneManager() throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		if (APIEngine.getTombstoneManager() == null) {
			return null;
		}

		return new TombstoneManagerImpl(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSSLon() throws DIException {
		return APIEngine.isSSLon();
	}

	// ---------------------
	// non-interface methods
	// ---------------------

	/**
	 * @return the {@link Identity} attribute of this object
	 * @throws DIException
	 *             if an error occurs.
	 */
	public Identity getIdentity() throws DIException {
		return mIdentity;
	}

	// ConfigurationRegistry

	/**
	 * {@inheritDoc}
	 */
	public boolean releaseConfigurationLock(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.releaseConfigurationLock(aRelativePath, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean undoCheckOut(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.undoCheckOut(aRelativePath, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<String> listConfigurations(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.listConfigurations(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<String> listFolders(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.listFolders(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<String> listAllConfigurations() throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.listAllConfigurations();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.checkOutConfiguration(aRelativePath, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath, String aPassword) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.checkOutConfiguration(aRelativePath, aPassword, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.checkOutConfigurationAndLoad(aRelativePath, mIdentity, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String aRelativePath, String aPassword) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.checkOutConfigurationAndLoad(aRelativePath, aPassword, mIdentity, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration, String aRelativePath) throws DIException {
		String methodExtension = "checkInConfiguration";

		String configName = aConfiguration.getShortName();
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + aRelativePath, configName,
				authSuccessful, interfaceName, methodExtension, configName, null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		mConfigRegistry.checkInConfiguration(aConfiguration, aRelativePath, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration, String aRelativePath, boolean aEncrypt) throws DIException {
		String methodExtension = "checkInConfiguration";
		String configName = aConfiguration.getShortName();
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + aRelativePath, configName,
				authSuccessful, interfaceName, methodExtension, configName, null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		mConfigRegistry.checkInConfiguration(aConfiguration, aRelativePath, mIdentity, aEncrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig aConfiguration, String aRelativePath) throws DIException {
		String methodExtension = "checkInAndLeaveCheckedOut";
		String configName = aConfiguration.getShortName();
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + aRelativePath, configName,
				authSuccessful, interfaceName, methodExtension, configName, null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		mConfigRegistry.checkInAndLeaveCheckedOut(aConfiguration, aRelativePath, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String relativePathOrSolutionName, boolean encrypt)
			throws DIException {
		String methodExtension = "checkInAndLeaveCheckedOut";
		String configName = configuration.getShortName();
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + relativePathOrSolutionName,
				configName, authSuccessful, interfaceName, methodExtension, configName, null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		mConfigRegistry.checkInAndLeaveCheckedOut(configuration, relativePathOrSolutionName, mIdentity, encrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig createNewConfiguration(String aRelativePath, boolean aOverwrite) throws DIException {
		String methodExtension = "createNewConfiguration";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + aRelativePath,
				getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.createNewConfiguration(aRelativePath, aOverwrite, mIdentity);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigurationAndLoad(String aRelativePath, boolean aOverwrite) throws DIException {
		String methodExtension = "createNewConfigurationAndLoad";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), mConfigRegistry.getConfigFolderPath() + aRelativePath,
				getServerInfo().getServerID(), authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.createNewConfigurationAndLoad(aRelativePath, aOverwrite, mIdentity, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConfigurationCheckedOut(String aRelativePath) throws DIException {
		if (!mIdentity.isAdmin()) {
			throw new AuthorizationException();
		}

		return mConfigRegistry.isConfigurationCheckedOut(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendCustomNotification(String aType, String aId, Object aData) throws DIException {
		APIEngine.sendCustomNotification(aType, aId, aData);
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemQueue getSystemQueue() throws DIException {
		if (!mIdentity.canExecuteAll()) {
			throw new AuthorizationException();
		}
		return SystemQueueImpl.newInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigFolderPath() {
		return mConfigRegistry.getConfigFolderPath();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParams) throws DIException {
		String methodExtension = "invokeCustom";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), aCustomClassName, aMethodName, authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		checkClass(aCustomClassName);

		Class<?>[] methodParams = null;
		if (aParams != null) {
			methodParams = new Class[aParams.length];
			for (int i = 0; i < aParams.length; i++) {
				if (aParams[i] == null) {
					throw new DIException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.ILLEGAL.USE.WITH.NULL.PARAM"));
				} else {
					methodParams[i] = aParams[i].getClass();
				}
			}
		}
		Object result = null;
		try {
			Class<?> customClass = Class.forName(aCustomClassName);
			Method method = customClass.getMethod(aMethodName, methodParams);
			result = method.invoke(null, aParams);
		} catch (ClassNotFoundException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.CLASS.NOT.FOUND"), exc);
		} catch (NoSuchMethodException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.NOT.FOUND"), exc);
		} catch (InvocationTargetException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.THROW.EXCEPTION"), exc);
		} catch (IllegalAccessException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.ILLEGAL.ACCESS"), exc);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParamsValue, String[] aParamsClass)
			throws DIException {
		String methodExtension = "invokeCustom";
		boolean authSuccessful = mIdentity.isAdmin();
		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), aCustomClassName, aMethodName, authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		checkClass(aCustomClassName);

		Object result = null;
		try {
			Class<?> customClass = Class.forName(aCustomClassName);
			Class<?>[] methodParams = null;
			if (aParamsClass != null) {
				methodParams = new Class[aParamsClass.length];
				for (int i = 0; i < methodParams.length; i++) {
					methodParams[i] = Class.forName(aParamsClass[i]);
				}
			}
			Method method = customClass.getMethod(aMethodName, methodParams);
			result = method.invoke(null, aParamsValue);
		} catch (ClassNotFoundException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.CLASS.NOT.FOUND"), exc);
		} catch (NoSuchMethodException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.NOT.FOUND"), exc);
		} catch (InvocationTargetException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.THROW.EXCEPTION"), exc);
		} catch (IllegalAccessException exc) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.ILLEGAL.ACCESS"), exc);
		}
		return result;
	}

	/**
	 * Verifies which classes are allowed to be invoked in the session
	 * 
	 * @param aCustomClassName
	 *            the name of the class to be checked
	 * @throws DIException
	 *             if invocation is not allowed.
	 */
	private void checkClass(String aCustomClassName) throws DIException {
		if (!APIEngine.getMethodInvokeEnabled()) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.NOT.TURNED.ON"));
		}
		String allowedClasses = APIEngine.getInvokeClassesAllowed();
		StringTokenizer classes = null;
		if (allowedClasses != null) {
			classes = new StringTokenizer(allowedClasses, " ,;");
		}
		if (classes == null || classes.countTokens() == 0) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.NO.CLASSES.ALLOWED"));
		}
		boolean classAllowed = false;
		while (classes.hasMoreTokens()) {
			String token = classes.nextToken();
			if (token.equals(aCustomClassName)) {
				classAllowed = true;
				break;
			}
		}
		if (!classAllowed) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.INVOKE.CUSTOM.METHOD.CLASS.NOT.ALLOWED", aCustomClassName));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteConfiguration(String relativePathOrSolutionName) throws DIException {

		String methodExtension = "deleteConfiguration";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), relativePathOrSolutionName, getServerInfo().getServerID(),
				authSuccessful, interfaceName, methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		mConfigRegistry.deleteConfiguration(relativePathOrSolutionName, mIdentity);
	}

	/**
	 * Prepare configuration instance startup parameters.
	 * 
	 * @param keepAlive
	 *            Whether to keep the instance running if it is the last thread
	 *            in the Server.
	 * @param password
	 *            A password for the configuration.
	 * @param runName
	 *            The run name (if not null) is used as the configuration
	 *            instance id.
	 * @param overrideProps
	 *            Override property store settings of the configuration. Can be
	 *            null.
	 * 
	 * @return The startup parameters.
	 * @throws DIException
	 *             If the run name is invalid.
	 */
	private Hashtable<String, Object> prepareStartupParams(boolean keepAlive, String password, String runName, String overrideProps)
			throws DIException {

		Hashtable<String, Object> params = new Hashtable<String, Object>();

		if (password != null) {
			params.put(RS.CL_PASSWORD, password);
		}
		if (keepAlive) {
			params.put(RS.CL_NO_TERMINATE, "");
		}
		if (runName != null) {
			runName = runName.trim();
			if (runName.length() > 0) {

				if (!runName.equals(LogUtils.getCleanConfigId(runName))) {

					// the run name is not suited for a config instance id
					APIEngine.logErrorAndThrowException(sResHash.getString("SERVER.API.INVALID.RUN.NAME", runName));
				}

				params.put(RS.CL_INTERNAL_CONFIG_NSTANCE_NAME, runName);
			}
		}
		if (overrideProps != null) {
			overrideProps = overrideProps.trim();
			if (overrideProps.length() > 0) {
				params.put(RS.CL_EXT_PROP_FILE, overrideProps);
			}
		}

		return params;
	}

	/**
	 * Start a configuration instance using the specified startup parameters.
	 * 
	 * @param params
	 *            Startup parameters.
	 * @param logListener
	 *            Listener for logged messages.
	 * @return A handle to the started configuration instance.
	 * @throws DIException
	 *             The instance cannot be started.
	 */
	private ConfigInstance startConfigInstance(Hashtable<String, Object> params, LogListener logListener) throws DIException {

		ConfigInstanceLogger ciLogger = null;
		if (logListener != null) {
			ciLogger = new ConfigInstanceLogger(logListener);
			params.put(RS.CL_INTERNAL_ADD_LISTENER, ciLogger);
		}

		RS rawConfigInstance = null;
		try {
			rawConfigInstance = RS.startServer(null, params);

			final long timeout = 500000; // changed from 50000 by L3 defect
			// 14135
			boolean initDone = rawConfigInstance.waitForInitializationToComplete(timeout);
			if (!initDone) {
				throw new TimeoutException();
			}

			Throwable rsError = rawConfigInstance.getExitError();
			if (rsError != null) {
				throw new Exception(rsError);
			}

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.CONFIG.INSTANCE.1"), e);
		}

		return new ConfigInstanceImpl(rawConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps)
			throws DIException {

		return startTempConfigInstance(xmlConfig, keepAlive, runName, overrideProps, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps, LogListener logListener) throws DIException {

		String methodExtension = "startConfigInstance";
		boolean authSuccessful = mIdentity.isAdmin();

		// Obtain a file path from the received token.
		String configUrl = APIEngine.getConfigurationRegistry().getConfigFilePath(configPathOrSolutionName);

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), configUrl, getServerInfo().getServerID(), authSuccessful,
				interfaceName, methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		Hashtable<String, Object> params = prepareStartupParams(keepAlive, password, runName, overrideProps);
		params.put(RS.CL_CONFIG, configUrl);

		return startConfigInstance(params, logListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps,
			LogListener logListener) throws DIException {

		String methodExtension = "startTempConfigInstance";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		Hashtable<String, Object> params = prepareStartupParams(keepAlive, null, runName, overrideProps);
		params.put(RS.CL_INTERNAL_CONFIG_AS_STRING, xmlConfig);
		params.put(RS.CL_NO_AUTOSTART, "");

		return startConfigInstance(params, logListener);
	}

	public void startTombstoneManager() throws DIException {
		APIEngine.startTombstoneManager();
	}

	public Object getPersistentObject(String key) throws DIException {
		String methodExtension = "getPersistentObject";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		try {
			return userFunctions.getPersistentObject(key);
		} catch (Throwable e) {
			throw new DIException(e);
		}
	}

	public Object setPersistentObject(String key, Object value) throws DIException {
		String methodExtension = "setPersistentObject";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		try {
			return userFunctions.setPersistentObject(key, value);
		} catch (Throwable e) {
			throw new DIException(e);
		}
	}

	public Object deletePersistentObject(String key) throws DIException {
		String methodExtension = "deletePersistentObject";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		try {
			return userFunctions.deletePersistentObject(key);
		} catch (Throwable e) {
			throw new DIException(e);
		}
	}

	public String getJavaProperty(String prop) throws DIException {
		String methodExtension = "getJavaProperty";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		try {
			return userFunctions.getJavaProperty(prop);
		} catch (Throwable e) {
			throw new DIException(e);
		}
	}

	/**
	 * Sets the value of a Java System property.
	 * 
	 * @param prop
	 *            The property name
	 * @param value
	 *            The property value
	 */
	public void setJavaProperty(String prop, String value) throws DIException {
		String methodExtension = "setJavaProperty";
		boolean authSuccessful = mIdentity.isAdmin();

		APIAuditor.sendSessionAuditData(mIdentity.getUserId(), null, getServerInfo().getServerID(), authSuccessful, interfaceName,
				methodExtension, getServerInfo().getServerID(), null);

		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		try {
			userFunctions.setJavaProperty(prop, value);
		} catch (Throwable e) {
			throw new DIException(e);
		}
	}
}
