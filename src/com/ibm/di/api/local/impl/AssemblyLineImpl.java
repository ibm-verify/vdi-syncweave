/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLineListener;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.syslog.LogUtils;
import com.ibm.di.api.syslog.SystemLogAppender;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.log.LogInterface;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

/**
 * Implements an AssemblyLine instance.
 */
public class AssemblyLineImpl implements com.ibm.di.api.local.AssemblyLine {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * {@link com.ibm.di.server.AssemblyLine} instance
	 */
	private com.ibm.di.server.AssemblyLine mAssemblyLine = null;

	/**
	 * {@link ConfigInstanceImpl} instance
	 */
	private ConfigInstanceImpl mConfigInstance = null;

	/**
	 * Represents the local session.
	 */
	private SessionImpl mSession = null;

	/**
	 * Writes logs to system files.
	 */
	private SystemLogAppender mSysLog = null;

	/**
	 * Global unique ID.
	 */
	private String mGUID = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Represents the name of the corresponding interface. It is used as part of
	 * the mechanism to filter authorization audit notifications.
	 */
	private final static String interfaceName = "AssemblyLine";

	/**
	 * Class constructor.
	 * 
	 * @param aAssemblyLine
	 *            {@link com.ibm.di.server.AssemblyLine} instance
	 * @param aConfigInstance
	 *            {@link ConfigInstanceImpl} instance
	 * @param aSession
	 *            {@link SessionImpl} instance
	 * @throws DIException
	 *             if any of the parameter is <code>null</code>
	 */
	public AssemblyLineImpl(com.ibm.di.server.AssemblyLine aAssemblyLine, ConfigInstanceImpl aConfigInstance, SessionImpl aSession)
			throws DIException {
		if (aAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.RAW.ASSEMBLYLINE.OBJECT.IS.NULL"));
		}
		if (aConfigInstance == null) {
			throw new DIException(sResHash.getString("SEVER.API.CONFIG.INSTANCE.OBJECT.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL"));
		}

		mAssemblyLine = aAssemblyLine;
		mConfigInstance = aConfigInstance;
		mSession = aSession;
		try {
			mSysLog = LogUtils.getSystemLogAppender(aAssemblyLine.getLog());
		} catch (Exception e) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString("SEVER.API.ERROR.WHILE.OBTAINING.SYSTEMLOG.FOR.AL", new Object[] {
						aAssemblyLine.getName(), String.valueOf(aAssemblyLine.hashCode()) }));
			}
			mSysLog = null;
		}

		mGUID = genGUID(aAssemblyLine);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance getConfigInstance() throws DIException {
		// everyone is allowed to execute this method

		return mConfigInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getUniqueCode() throws DIException {
		return mAssemblyLine.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLineConfig getConfig() throws DIException {
		if (!mSession.getIdentity().canReadConfig(mConfigInstance.getConfigId())) {
			throw new AuthorizationException();
		}

		AssemblyLineConfig config = null;
		try {
			config = mAssemblyLine.getConfigClone();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.GET.ASSEMBLYLINE.CONFIGURATION"), e);
		}
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehavior() throws DIException {
		// everyone is allowed to execute this method

		BaseConfiguration baseConfig = getConfig().getSettings();
		if (baseConfig == null) {
			return null;
		}

		return baseConfig.getNullBehavior();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehaviorValue() throws DIException {
		// everyone is allowed to execute this method

		BaseConfiguration baseConfig = getConfig().getSettings();
		if (baseConfig == null) {
			return null;
		}

		return baseConfig.getNullBehaviorValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public TaskStatistics getStatistics() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getStats();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isActive() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.isAlive();
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getResult() throws DIException {
		// everyone is allowed to execute this method

		return mAssemblyLine.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(mConfigInstance.getConfigId(), mAssemblyLine.getName());
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfiguration().getPath(),
				getName(), authSuccessful, interfaceName, methodExtension, getName(), mConfigInstance.getConfigId());
		if (!authSuccessful) {
			throw new AuthorizationException();
		}

		mAssemblyLine.shutdown();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(boolean sync) throws DIException {
		String methodExtension = "stop";
		boolean authSuccessful = mSession.getIdentity().canExecuteAL(mConfigInstance.getConfigId(), mAssemblyLine.getName());
		APIAuditor.sendSessionAuditData(mSession.getIdentity().getUserId(), mConfigInstance.getConfiguration().getPath(),
				getName(), authSuccessful, interfaceName, methodExtension, getName(), mConfigInstance.getConfigId());
		if (!authSuccessful) {
			throw new AuthorizationException();
		}
		try {
			mAssemblyLine.shutdown(sync);
		} catch (AbortALException aae) {
			// Cannot happen.
			SystemFunctions.doNothing();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogFilePath() throws DIException {
		// everyone is allowed to execute this method

		if (mSysLog == null) {
			return null;
		}
		return mSysLog.getFile();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogFileName() throws DIException {
		// everyone is allowed to execute this method

		if (mSysLog == null && mAssemblyLine != null) {
			mSysLog = mAssemblyLine.getLog().getSystemLog();
		}
		return mSysLog == null ? null : mSysLog.getFileName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLog() throws DIException {
		if (!mSession.getIdentity().canExecuteAL(mConfigInstance.getConfigId(), mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}

		String log = null;
		if (mSysLog != null) {
			String logFileName = mSysLog.getFile();
			log = LogUtils.getComponentLog(logFileName);
		}
		return log;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogLastChunk(int aLastKilobytes) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(mConfigInstance.getConfigId(), mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}

		String log = null;
		if (mSysLog != null) {
			String logFileName = mSysLog.getFile();
			log = LogUtils.getComponentLogLastChunk(logFileName, aLastKilobytes);
		}
		return log;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGlobalUniqueID() {
		return mGUID;
	}

	/**
	 * Generates unique GUID for a specified assembly line.
	 * 
	 * @param aAssemblyLine
	 *            the assembly line name
	 * @return generated GUID as String object
	 */
	public static String genGUID(com.ibm.di.server.AssemblyLine aAssemblyLine) {
		String hash = Integer.toString(aAssemblyLine.hashCode());
		String start = Long.toString(aAssemblyLine.getStats().getStart());
		String guid = hash + start;

		return guid;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSimulating() throws DIException {
		// everyone can do this
		return mAssemblyLine.isSimulating();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSimulating(boolean simulate) throws DIException {
		if (!mSession.getIdentity().canExecuteAL(mConfigInstance.getConfigId(), mAssemblyLine.getName())) {
			throw new AuthorizationException();
		}

		mAssemblyLine.setSimulating(simulate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(AssemblyLineListener listener, boolean getLogs, boolean getEntryOnEachCycle) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.12"));
		}

		LogInterface logger = null;
		Log log = mAssemblyLine.getLog();
		if (getLogs && log != null) {
			logger = new LogListenerAdapter(listener, log, LogListenerAdapter.AL_LOG_MSG_FORMAT);
			log.addLogger(logger);
		}

		/*
		 * keep the event listener in pair with the logger, so that we can
		 * remove the logger when we remove the listener
		 */
		AssemblyLineListenerAdapter listenerAdapter = new AssemblyLineListenerAdapter(listener, logger, getEntryOnEachCycle);
		mAssemblyLine.addListener(listenerAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(AssemblyLineListener listener) throws DIException {

		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.12"));
		}

		AssemblyLineListenerAdapter adapter = new AssemblyLineListenerAdapter(listener);
		adapter = (AssemblyLineListenerAdapter) mAssemblyLine.removeListener(adapter);

		if (adapter == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.NOT.REGISTERED"));
		}

		// also unregister the associated logger, if any
		LogInterface logger = adapter.getLogger();
		Log log = mAssemblyLine.getLog();
		if (logger != null && log != null) {
			log.removeLogger(logger);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void attachDebugger(int port, String host, boolean onerror) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		try {
			mAssemblyLine.enableDebug(port, host, onerror, true);
		} catch (Exception ex) {
			throw new DIException(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void detachDebugger(Object msg) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		mAssemblyLine.disableDebug(msg, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getComponentDebugMode(String componentName) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		try {
			return mAssemblyLine.getComponentDebugMode(componentName);
		} catch (Exception ex) {
			throw new DIException(ex.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setComponentDebugMode(String componentName, boolean debug) throws DIException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}
		try {
			mAssemblyLine.setComponentDebugMode(componentName, debug);
		} catch (Exception ex) {
			throw new DIException(ex.getMessage());
		}
	}

}
