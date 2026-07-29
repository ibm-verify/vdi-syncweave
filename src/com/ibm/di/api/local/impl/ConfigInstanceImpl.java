/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.ConfigurationRegistry;
import com.ibm.di.api.DIException;
import com.ibm.di.api.exceptions.PasswordException;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.AssemblyLineHandler;
import com.ibm.di.api.local.AssemblyLineListener;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.LogListener;
import com.ibm.di.api.local.Sequence;
import com.ibm.di.api.local.TDIProperties;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ConfigInstanceListener;
import com.ibm.di.server.ConnectorPool;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskCallBlock;

/**
 * Represents a configuration instance and implements various methods for
 * manipulating the configuration.
 */
public class ConfigInstanceImpl implements ConfigInstance {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Forks off new threads to run an AssemblyLine and EcentHandlers.
	 */
	private RS mConfigInstance = null;

	/**
	 * Represents the local session
	 */
	private SessionImpl mSession = null;

	/**
	 * ID of the config instance
	 */
	private String mConfigInstanceId;

	/**
	 * Global unique ID.
	 */
	private String mGUID = null;

	/**
	 * Load timeout property.
	 */
	private static final String PROPERTY_CONFIG_LOAD_TIMEOUT = "api.config.load.timeout";

	/**
	 * Default config load timeout - 2;
	 */
	private static final int DEFAULT_CONFIG_LOAD_TIMEOUT = 2;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "ConfigInstance";

	/**
	 * 
	 * @param aConfigInstance
	 * @param aSession
	 * @throws DIException
	 */
	public ConfigInstanceImpl(RS aConfigInstance, SessionImpl aSession) throws DIException {
		if (aConfigInstance == null) {
			throw new DIException(sResHash.getString("SEVER.API.RAW.CONFIG.INSTANCE.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL"));
		}

		mConfigInstance = aConfigInstance;
		mSession = aSession;

		mConfigInstanceId = APIEngine.getConfigId(aConfigInstance);

		if (null == mConfigInstanceId || !aConfigInstance.isAlive()) {

			Throwable e = aConfigInstance.getExitError();
			if (e instanceof com.ibm.di.exceptions.PasswordException) {
				APIEngine.logError(sResHash.getString("SERVER.API.MISSING.PASSWORD.FOR.CONFIGURATION"));
				throw new PasswordException(sResHash.getString("SERVER.API.MISSING.PASSWORD.FOR.CONFIGURATION"));
			}
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.RAW.CONFIG.INSTANCE.IS.NOT.RUNNING"));
		}

		mGUID = genGUID(aConfigInstance);

	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigId() throws DIException {
		// everyone is allowed to execute this method

		return getConfigurationId();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig getConfiguration() throws DIException {
		if (!mSession.getIdentity().canReadConfig(getConfigurationId())) {
			throw new AuthorizationException();
		}

		MetamergeConfig mc = getMetamergeConfig();
		// The config instance must be cloned to ensure local readonly access

		return mc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfiguration(MetamergeConfig aConfiguration) throws DIException {
		String methodExtension = "setConfiguration";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), aConfiguration
				.getShortName(), authSuccessful, interfaceName, methodExtension, aConfiguration.getShortName(), mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		if (!APIEngine.getConfigurationRegistry().userCanSetConfiguration(getConfigId(), mSession.getIdentity())) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.USER.CANT.SET.CONFIGURATION.FOR", new Object[] {
					mSession.getIdentity().getUserId(), getConfigId() }));
		}

		mConfigInstance.setMetamergeConfig(aConfiguration);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated not supported.
	 */
	public void saveConfiguration() throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.A.SUPPORTED.CALL.1"));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated not supported.
	 */
	public void saveConfiguration(boolean aEncrypt) throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.A.SUPPORTED.CALL.2"));
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties() throws DIException {
		if (!mSession.getIdentity().canReadConfig(getConfigurationId())) {
			throw new AuthorizationException();
		}

		MetamergeConfig mc = getMetamergeConfig();
		ExternalPropertiesConfig epc = null;
		try {
			// Use ExternalPropertiesConfig getClone() when the MetamergeConfig
			// is also
			// cloned to ensure readonly local access
			epc = mc.getExternalProperties(MetamergeConfig.DEFAULT_EXTPROP_NAME);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.OBTAIN.EXTERNAL.PROPERTIES.1"), e);
		}
		return epc;
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties(String aKey) throws DIException {
		if (!mSession.getIdentity().canReadConfig(getConfigurationId())) {
			throw new AuthorizationException();
		}

		MetamergeConfig mc = getMetamergeConfig();
		ExternalPropertiesConfig epc = null;
		try {
			// Use ExternalPropertiesConfig getClone() when the MetamergeConfig
			// is also
			// cloned to ensure readonly local access
			epc = mc.getExternalProperties(aKey);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.OBTAIN.EXTERNAL.PROPERTIES.2"), e);
		}
		return epc;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExternalPropertiesKeys() throws DIException {
		if (!mSession.getIdentity().canReadConfig(getConfigurationId())) {
			throw new AuthorizationException();
		}

		MetamergeConfig mc = getMetamergeConfig();
		String[] keys = null;
		try {
			keys = ((MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_EXTPROP_FOLDER)).getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.OBTAIN.EXTERNAL.PROPERTIES.KEYS"), e);
		}
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExternalProperties(ExternalPropertiesConfig aExPropConfig) throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.A.SUPPORTED.CALL.3"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExternalProperties(String aKey, ExternalPropertiesConfig aExPropConfig) throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.A.SUPPORTED.CALL.4"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveExternalProperties() throws DIException {
		throw new DIException(sResHash.getString("SEVER.API.NOT.A.SUPPORTED.CALL.5"));
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAssemblyLinesNames() throws DIException {
		// deprecated
		return getAssemblyLineNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAssemblyLineNames() throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteConfigALs(
				getConfigurationId()))) {
			throw new AuthorizationException();
		}

		MetamergeFolder alFolder = getAssemblyLinesFolder();

		String[] alNames = null;
		try {
			alNames = alFolder.getNames();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.ASSEMBLYLINES.NAMES"), e);
		}
		return alNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getAssemblyLineUniqueCodes() throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteConfigALs(
				getConfigurationId()))) {
			throw new AuthorizationException();
		}

		int[] ids = null;
		Hashtable<RSInterface, Vector<com.ibm.di.server.AssemblyLine>> als = APIEngine.getAssemblyLines();
		if (als != null) {
			Vector<com.ibm.di.server.AssemblyLine> alsv = als.get(mConfigInstance);
			if (alsv != null && alsv.size() > 0) {
				ids = new int[alsv.size()];
				for (int i = 0; i < alsv.size(); i++) {
					com.ibm.di.server.AssemblyLine al = (com.ibm.di.server.AssemblyLine) alsv.get(i);
					ids[i] = al.hashCode();
				}
			}
		}

		if (ids == null) {
			ids = new int[0];
		}

		return ids;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine getAssemblyLineByUniqueCode(int alId) throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteConfigALs(
				getConfigurationId()))) {
			throw new AuthorizationException();
		}

		Hashtable<RSInterface, Vector<com.ibm.di.server.AssemblyLine>> als = APIEngine.getAssemblyLines();
		if (als != null) {
			Vector<com.ibm.di.server.AssemblyLine> alsv = als.get(mConfigInstance);
			if (alsv != null) {
				for (int i = 0; i < alsv.size(); i++) {
					com.ibm.di.server.AssemblyLine al = (com.ibm.di.server.AssemblyLine) alsv.get(i);
					if (al.hashCode() == alId) {
						return new AssemblyLineImpl(al, this, mSession);
					}
				}
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineInputParameters(String aAssemblyLineName) throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteAL(
				getConfigurationId(), aAssemblyLineName))) {
			throw new AuthorizationException();
		}

		AssemblyLineConfig alCfg = getAssemblyLineCfg(aAssemblyLineName);
		Object schema = null;
		try {
			schema = alCfg.getSchema(true);
			// should clone for readonly local access
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.OBTAIN.ASSEMBLYLINE.INPUT.SCHEMA"), e);
		}
		return (SchemaConfig) schema;
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineOutputParameters(String aAssemblyLineName) throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteAL(
				getConfigurationId(), aAssemblyLineName))) {
			throw new AuthorizationException();
		}

		AssemblyLineConfig alCfg = getAssemblyLineCfg(aAssemblyLineName);
		Object schema = null;
		try {
			schema = alCfg.getSchema(false);
			// should clone for readonly local access
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.OBTAIN.ASSEMBLYLINE.OUTPUT.SCHEMA"), e);
		}
		return (SchemaConfig) schema;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine[] getAssemblyLines() throws DIException {
		if (!(mSession.getIdentity().canReadConfig(getConfigurationId()) || mSession.getIdentity().canExecuteConfigALs(
				getConfigurationId()))) {
			throw new AuthorizationException();
		}

		AssemblyLine[] result = new AssemblyLine[0];
		Hashtable<RSInterface, Vector<com.ibm.di.server.AssemblyLine>> als = APIEngine.getAssemblyLines();
		if (als == null) {
			return result;
		}

		Vector<com.ibm.di.server.AssemblyLine> alsv = als.get(mConfigInstance);
		if (alsv == null || alsv.size() == 0) {
			return result;
		} else {
			result = new AssemblyLine[alsv.size()];
			for (int i = 0; i < alsv.size(); i++) {
				result[i] = new AssemblyLineImpl((com.ibm.di.server.AssemblyLine) alsv.get(i), this, mSession);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName) throws DIException {
		return startAssemblyLine(aAssemblyLineName, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, boolean aSync) throws DIException {
		String methodExtension = "startAssemblyLine";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), aAssemblyLineName);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), aAssemblyLineName,
				authSuccessful, interfaceName, methodExtension, aAssemblyLineName, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.1",
					aAssemblyLineName));
		}

		com.ibm.di.server.AssemblyLine rawAl = null;
		try {
			rawAl = mConfigInstance.startAL(aAssemblyLineName);
			if (aSync == true) {
				rawAl.join();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE", aAssemblyLineName), e);
		}

		updateLockTime();
		return new AssemblyLineImpl(rawAl, this, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData) throws DIException {
		return startAssemblyLine(aAssemblyLineName, aInputData, null, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, boolean aSync) throws DIException {
		return startAssemblyLine(aAssemblyLineName, aInputData, null, false, aSync);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs) throws DIException {
		return startAssemblyLine(aAssemblyLineName, aInputData, aListener, aGetLogs, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs, boolean aSync) throws DIException {
		return startAssemblyLine(aAssemblyLineName, aInputData, aListener, aGetLogs, aSync, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs, boolean aSync, boolean aGetEntryOnEachCycle) throws DIException {
		String methodExtension = "startAssemblyLine";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), aAssemblyLineName);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), aAssemblyLineName,
				authSuccessful, interfaceName, methodExtension, aAssemblyLineName, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.2",
					aAssemblyLineName));
		}

		TaskCallBlock tcb = new TaskCallBlock();
		/*
		 * If inputData is a TCB already use it, otherwise create it.
		 */
		if (aInputData != null) {
			if (aInputData instanceof TaskCallBlock) {
				tcb = (TaskCallBlock) aInputData;
			} else {
				tcb.setInitialWorkEntry(aInputData);
			}
		}
		Vector<Object> alParams = new Vector<Object>();
		alParams.add(tcb);
		AssemblyLineListenerAdapter listenerAdapter = null;
		LogListenerAdapter logAppender = null;
		if (aListener != null) {
			if (aGetLogs) {
				logAppender = new LogListenerAdapter(aListener, null, LogListenerAdapter.AL_LOG_MSG_FORMAT);
			}
			listenerAdapter = new AssemblyLineListenerAdapter(aListener, logAppender, aGetEntryOnEachCycle);
			alParams.add(listenerAdapter);
		}
		com.ibm.di.server.AssemblyLine rawAl = null;
		try {
			if (logAppender != null) {
				rawAl = mConfigInstance.startAL(aAssemblyLineName, alParams, logAppender);
				/*
				 * now we have a log - give it to the adapter, so that it can
				 * unregister itself if something goes wrong
				 */
				logAppender.setLog(rawAl.getLog());
			} else {
				rawAl = mConfigInstance.startAL(aAssemblyLineName, alParams);
			}
			if (aSync == true) {
				rawAl.join();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE.1", aAssemblyLineName),
					e);
		}

		updateLockTime();

		return new AssemblyLineImpl(rawAl, this, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLineHandler startAssemblyLineManual(String aAssemblyLineName, Entry aInputData) throws DIException {
		String methodExtension = "startAssemblyLineManual";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), aAssemblyLineName);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), aAssemblyLineName,
				authSuccessful, interfaceName, methodExtension, aAssemblyLineName, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.3",
					aAssemblyLineName));
		}

		TaskCallBlock tcb = new TaskCallBlock();
		// If inputData is a TCB already use it, otherwise create it.
		if (aInputData != null) {
			if (aInputData instanceof TaskCallBlock) {
				tcb = (TaskCallBlock) aInputData;
			} else {
				tcb.setInitialWorkEntry(aInputData);
			}
		}

		tcb.setProperty(com.ibm.di.server.AssemblyLine.TCB_RUNMODE_PROPNAME, Integer
				.valueOf(com.ibm.di.server.AssemblyLine.RUNMODE_I_MANUAL));

		com.ibm.di.server.AssemblyLine rawAl = null;
		try {
			rawAl = mConfigInstance.startAL(aAssemblyLineName, tcb);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE.2", aAssemblyLineName),
					e);
		}

		AssemblyLineImpl assemblyLine = new AssemblyLineImpl(rawAl, this, mSession);
		return new AssemblyLineHandlerImpl(assemblyLine, rawAl, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, TaskCallBlock aTcb) throws DIException {
		return startAssemblyLine(aAssemblyLineName, aTcb, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, TaskCallBlock aTcb, boolean aSync) throws DIException {
		String methodExtension = "startAssemblyLine";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), aAssemblyLineName);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), aAssemblyLineName,
				authSuccessful, interfaceName, methodExtension, aAssemblyLineName, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.4",
					aAssemblyLineName));
		}
		com.ibm.di.server.AssemblyLine rawAl = null;
		try {
			rawAl = mConfigInstance.startAL(aAssemblyLineName, aTcb);
			if (aSync) {
				rawAl.join();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE.3", aAssemblyLineName),
					e);
		}

		updateLockTime();
		return new AssemblyLineImpl(rawAl, this, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload() throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		try {
			mConfigInstance.reload();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RELOAD.CONFIG.INSTANCE"), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), mConfigInstanceId,
				authSuccessful, interfaceName, methodExtension, mConfigInstanceId, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		mConfigInstance.shutdownServer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(boolean sync) throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = mSession.getIdentity().isAdmin();
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), mConfigInstanceId,
				authSuccessful, interfaceName, methodExtension, mConfigInstanceId, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		mConfigInstance.shutdownServer(0, sync);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aALName) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(getConfigurationId(), aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLogFileNames(getConfigurationId(), aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aALName) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(getConfigurationId(), aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLastLogFileName(getConfigurationId(), aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aALName, String aLogFileName) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(getConfigurationId(), aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLog(getConfigurationId(), aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aALName, String aLogFileName, int aKilobytes) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(getConfigurationId(), aALName)) {
			throw new AuthorizationException();
		}

		return LogUtils.getALLogLastChunk(getConfigurationId(), aALName, aLogFileName, aKilobytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getInstanceBootTime() throws DIException {
		return new Date(((RS) mConfigInstance).mmStarted);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGlobalUniqueID() {
		return mGUID;
	}

	// Connector Pool calls

	/**
	 * {@inheritDoc}
	 */
	public String[] getConnectorPoolNames() throws DIException {
		if (!(mSession.getIdentity().canExecuteConfigALs(getConfigurationId()) || mSession.getIdentity().canReadConfig(
				getConfigurationId()))) {
			throw new AuthorizationException();
		}

		return mConfigInstance.getConnectorPoolNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolSize(String aConnectorPoolName) throws DIException {
		if (!mSession.getIdentity().canExecuteConfigALs(getConfigurationId())) {
			throw new AuthorizationException();
		}

		ConnectorPool connPool = mConfigInstance.getConnectorPool(aConnectorPoolName);
		if (connPool == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONNECTOR.POOL.DOES.NOT.EXIST.1", aConnectorPoolName));
		}

		return connPool.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolFreeNum(String aConnectorPoolName) throws DIException {
		if (!mSession.getIdentity().canExecuteConfigALs(getConfigurationId())) {
			throw new AuthorizationException();
		}

		ConnectorPool connPool = mConfigInstance.getConnectorPool(aConnectorPoolName);
		if (connPool == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONNECTOR.POOL.DOES.NOT.EXIST.2", aConnectorPoolName));
		}

		return connPool.getFreeConnectorsNum();
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolDefConfig getConnectorPoolConfig(String aConnectorPoolName) throws DIException {
		if (!mSession.getIdentity().canReadConfig(getConfigurationId())) {
			throw new AuthorizationException();
		}

		ConnectorPool connPool = mConfigInstance.getConnectorPool(aConnectorPoolName);
		if (connPool == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONNECTOR.POOL.DOES.NOT.EXIST.3", aConnectorPoolName));
		}

		return connPool.getPoolConfig();
	}

	/**
	 * {@inheritDoc}
	 */
	public int purgeConnectorPool(String aConnectorPoolName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		ConnectorPool connPool = mConfigInstance.getConnectorPool(aConnectorPoolName);
		if (connPool == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONNECTOR.POOL.DOES.NOT.EXIST.4", aConnectorPoolName));
		}

		connPool.purge();
		return connPool.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIProperties getTDIProperties() throws Exception {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig mc = getMetamergeConfig();
		com.ibm.di.config.interfaces.TDIProperties localTDIP = mc.getTDIProperties();
		TDIPropertiesImpl tdipImpl = new TDIPropertiesImpl(localTDIP, mSession);
		return tdipImpl;
	}

	// ***************************************
	// PRIVATE METHODS
	// ***************************************

	/**
	 * Retrieves the ID of the configuration.
	 * 
	 * @return ID of the config instance.
	 * @throws DIException
	 *             : never
	 */
	private String getConfigurationId() throws DIException {
		return mConfigInstanceId;
	}

	/**
	 * Retrieves the {@link MetamergeConfig}.
	 * 
	 * @return MetamergeConfig
	 * @throws DIException
	 *             if an error occurs.
	 */
	private MetamergeConfig getMetamergeConfig() throws DIException {
		MetamergeConfig config = null;

		try {
			config = (MetamergeConfig) mConfigInstance.getMetamergeConfig();
			if (config == null) {
				// Get timeout interval specified
				String configTimeout = System.getProperty(PROPERTY_CONFIG_LOAD_TIMEOUT);
				int timeOut = 0;
				if (configTimeout != null && configTimeout.trim().length() > 0) {
					try {
						timeOut = Integer.parseInt(configTimeout);
					} catch (NumberFormatException e) {
						APIEngine.logError(sResHash.getString("SERVER.API.INVALID.VALUE.SPECIFIED.FOR.CONFIG.TIMEOUT",
								configTimeout));
						timeOut = DEFAULT_CONFIG_LOAD_TIMEOUT;
					}
				} else {
					timeOut = DEFAULT_CONFIG_LOAD_TIMEOUT;
				}

				long maxTimeout = 1000L * 60 * timeOut;
				long startWaitTime = System.currentTimeMillis();
				long currentTime = System.currentTimeMillis();

				while (config == null && ((currentTime - startWaitTime) < maxTimeout)) {
					Thread.sleep(600);
					config = (MetamergeConfig) mConfigInstance.getMetamergeConfig();
					currentTime = System.currentTimeMillis();
				}
				if (config == null)
					APIEngine.logErrorAndThrowException(sResHash.getString("SERVER.API.CONFIG.LOAD.TIMEOUT.EXCEEDED"));
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.UNABLE.TO.OBTAIN.CONFIG.OBJECT"), e);

		}
		return config;
	}

	/**
	 * Retrieves Assembly line's folder
	 * 
	 * @return MetamergeFolder
	 * @throws DIException
	 *             if an error occurs.
	 */
	private MetamergeFolder getAssemblyLinesFolder() throws DIException {
		MetamergeConfig cfg = getMetamergeConfig();
		MetamergeFolder alFolder = null;
		try {
			alFolder = cfg.getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.RETRIEVE.ASSEMBLYLINES.FOLDER"), e);
		}

		if (alFolder == null) {
			throw new DIException(sResHash.getString("SEVER.API.ASSEMBLYLINES.FOLDER.OBJECT.IS.NULL"));
		}

		return alFolder;
	}

	/**
	 * Retrieves Assembly line config.
	 * 
	 * @param aALName
	 *            name of the AssemblyLine
	 * @return AssemblyLineConfig
	 * @throws DIException
	 */
	private AssemblyLineConfig getAssemblyLineCfg(String aALName) throws DIException {
		MetamergeConfig cfg = getConfiguration();

		AssemblyLineConfig alCfg = null;
		try {
			alCfg = cfg.getAssemblyLine(aALName);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString(
					"SEVER.API.GETASSEMBLYLINECFG.COULD.NOT.RETRIEVE.CONFIGURATION.FOR.ASSEMBLYLINE", aALName));
		}

		if (alCfg == null) {
			throw new DIException(sResHash.getString("SEVER.API.GETASSEMBLYLINECFG.ASSEMBLYLINE", aALName));
		}

		return alCfg;
	}

	/**
	 * This method is for internal Server API usage.
	 * 
	 * @param aConfigInstance
	 * @return the globally unique identifier string
	 */
	public static String genGUID(com.ibm.di.server.RS aConfigInstance) {

		String hash = Integer.toString(aConfigInstance.hashCode());
		String start = Long.toString(aConfigInstance.mmStarted);
		String guid = hash + start;

		return guid;
	}

	/**
	 * Updates the time the configuration is checked out.
	 */
	private void updateLockTime() {
		if (mConfigInstanceId.startsWith(ConfigurationRegistry.CM_PREFIX)) {
			try {
				APIEngine.getConfigurationRegistry().updateLockTime(mConfigInstance.getConfigPath());
			} catch (DIException e) {
				APIEngine.logError(sResHash.getString("SEVER.API.ERROR.UPDATING.CONFIGURATION.LOCK.TIME", e.toString()));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigPath() {
		// everyone is allowed to execute this method
		return mConfigInstance.getCommandLineConfigId();
	}

	public String getConfigurationFile() throws DIException {
		String cp = getConfigPath();
		return cp != null && !"<stdin>".equals(cp) ? APIEngine.getConfigurationRegistry().getConfigToken(new File(cp)) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addLogListener(LogListener listener) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.13"));
		}
		ConfigInstanceLogger logger = new ConfigInstanceLogger(listener);
		logger.configInstanceStarted(mConfigInstance);
		mConfigInstance.addListener(logger);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLogListener(LogListener listener) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.13"));
		}
		ConfigInstanceLogger logger = new ConfigInstanceLogger(listener);
		ConfigInstanceListener actLogger = mConfigInstance.removeListener(logger);
		if (actLogger == null || !(actLogger instanceof ConfigInstanceLogger)) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.NOT.REGISTERED.2"));
		}
		((ConfigInstanceLogger) actLogger).close();
	}

	public Sequence startSequence(String name) throws DIException {
		return startSequence(name, null, false);
	}

	public Sequence startSequence(String name, TaskCallBlock tcb, boolean sync)
			throws DIException {
		String methodExtension = "startSequence";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), name);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), name,
				authSuccessful, interfaceName, methodExtension, name, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.1",
					name));
		}

		com.ibm.di.server.Sequence seq = null;
		try {
			seq = mConfigInstance.startSequence(name, tcb);
			if (sync) {
				seq.join();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE", name), e);
		}

		updateLockTime();
		return new SequenceImpl(seq, this, mSession);
	}

	public Sequence startSequence(String name, TaskCallBlock tcb,
			AssemblyLineListener listener) throws DIException {
		String methodExtension = "startSequence";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(getConfigurationId(), name);
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfigPath(), name,
				authSuccessful, interfaceName, methodExtension, name, mConfigInstanceId);
		if (!authSuccessful) {
			throw new AuthorizationException(sResHash.getString("SEVER.API.NOT.AUTHORIZED.TO.EXECUTE.ASSEMBLYLINE.1",
					name));
		}

		Vector<Object> params = new Vector<Object>();
		if (tcb != null) {
			params.add(tcb);
		}
		LogListenerAdapter logAppender = null;
		if (listener != null) {
			logAppender = new LogListenerAdapter(listener, null, LogListenerAdapter.AL_LOG_MSG_FORMAT);
			params.add(new AssemblyLineListenerAdapter(listener, logAppender, false));
		}

		com.ibm.di.server.Sequence seq = null;
		try {
			seq = mConfigInstance.startSequence(name, params, logAppender);
			if (logAppender != null)
				logAppender.setLog(seq.getLog());
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.START.ASSEMBLYLINE", name), e);
		}

		updateLockTime();
		return new SequenceImpl(seq, this, mSession);
	}

	public Map<String, Object> getSchedulerInfo(String name) {
		return mConfigInstance.getSchedulerInfo(name);
	}

	public List<Map<String, Object>> getSchedulersInfo() {
		return mConfigInstance.getSchedulersInfo();
	}
}
