/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.Unreferenced;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.Sequence;
import com.ibm.di.api.remote.TDIProperties;
import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.FileConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskCallBlock;

/**
 * This class implements methods exposed through Server API remote session.
 */
public class ConfigInstanceImpl extends APIRemoteObject 
	implements com.ibm.di.api.remote.ConfigInstance, Unreferenced {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 5732937135284552922L;

	/**
	 * api session object
	 */
	private transient SessionImpl mSession = null;

	/**
	 * configuration instance of the assembly line
	 */
	private transient com.ibm.di.api.local.ConfigInstance mLocalConfigInstance = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalConfigInstance
	 *            local config instance
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if error occurred while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private ConfigInstanceImpl(com.ibm.di.api.local.ConfigInstance aLocalConfigInstance, SessionImpl aSession,
			RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalConfigInstance == null) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.CONFIG.INSTANCE.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.8"));
		}

		mSession = aSession;
		mLocalConfigInstance = aLocalConfigInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigId() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getConfigId();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig getConfiguration() throws DIException, RemoteException {
		// security check is delegated to the local implementation
		MetamergeConfig mc = mLocalConfigInstance.getConfiguration();
		try {
			if (mc != null) {
				mc.instantiateAllObjects();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.METAMERGECONFIGINSTANTIATEALLOBJECTS.FAILED"), e);
		}
		return mc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfiguration(MetamergeConfig aConfiguration) throws DIException, RemoteException {
		if (!mSession.getIdentity().isAdmin()) {
			throw new AuthorizationException();
		}

		MetamergeConfig oldConfig = mLocalConfigInstance.getConfiguration();
		FileConfig fileConfig = ((MetamergeConfigImpl) oldConfig).getFileConfig();
		if (fileConfig != null) {
			((MetamergeConfigImpl) aConfiguration).setFileConfig(fileConfig);
		}
		mLocalConfigInstance.setConfiguration(aConfiguration);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAssemblyLinesNames() throws DIException, RemoteException {
		// deprecated call
		return mLocalConfigInstance.getAssemblyLinesNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getAssemblyLineUniqueCodes() throws DIException, RemoteException {
		return mLocalConfigInstance.getAssemblyLineUniqueCodes();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine getAssemblyLineByUniqueCode(int alId) throws DIException, RemoteException {
		com.ibm.di.api.local.AssemblyLine al = mLocalConfigInstance.getAssemblyLineByUniqueCode(alId);
		return al != null ? AssemblyLineImpl.createInstance(al, mSession) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAssemblyLineNames() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getAssemblyLineNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineInputParameters(String aAssemblyLineName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getAssemblyLineInputParameters(aAssemblyLineName);
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getAssemblyLineOutputParameters(String aAssemblyLineName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getAssemblyLineOutputParameters(aAssemblyLineName);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine[] getAssemblyLines() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine[] localALs = mLocalConfigInstance.getAssemblyLines();

		AssemblyLine[] remoteALs = new AssemblyLine[localALs.length];
		for (int i = 0; i < localALs.length; i++) {
			remoteALs[i] = AssemblyLineImpl.createInstance(localALs[i], mSession);
		}
		return remoteALs;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, boolean aSync) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aSync);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aInputData);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, boolean aSync) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aInputData,
				aSync);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(aListener,
				com.ibm.di.api.local.AssemblyLineListener.class);

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aInputData,
				localListener, aGetLogs);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs, boolean aSync) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(aListener,
				com.ibm.di.api.local.AssemblyLineListener.class);

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aInputData,
				localListener, aGetLogs, aSync);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, Entry aInputData, AssemblyLineListener aListener,
			boolean aGetLogs, boolean aSync, boolean aGetEntryOnEachCycle) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(aListener,
				com.ibm.di.api.local.AssemblyLineListener.class);

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aInputData,
				localListener, aGetLogs, aSync, aGetEntryOnEachCycle);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLineHandler startAssemblyLineManual(String aAssemblyLineName, Entry aInputData) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLineHandler localHandler = mLocalConfigInstance.startAssemblyLineManual(aAssemblyLineName,
				aInputData);

		AssemblyLineImpl assemblyLine = AssemblyLineImpl.createInstance(localHandler.getAssemblyLine(), mSession);

		return AssemblyLineHandlerImpl.createInstance(assemblyLine, localHandler, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, TaskCallBlock aTcb) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance.startAssemblyLine(aAssemblyLineName, aTcb);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAssemblyLine(String aAssemblyLineName, TaskCallBlock aTcb, boolean aSync) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine localAssemblyLine = mLocalConfigInstance
				.startAssemblyLine(aAssemblyLineName, aTcb, aSync);

		return AssemblyLineImpl.createInstance(localAssemblyLine, mSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.reload();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		if (mLocalConfigInstance != null)
			mLocalConfigInstance.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(boolean sync) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		if (mLocalConfigInstance != null)
			mLocalConfigInstance.stop(sync);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getALLogFileNames(String aALName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getALLogFileNames(aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLastLogFileName(String aALName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getALLastLogFileName(aALName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLog(String aALName, String aLogFileName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getALLog(aALName, aLogFileName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getALLogLastChunk(String aALName, String aLogFileName, int aKilobytes) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getALLogLastChunk(aALName, aLogFileName, aKilobytes);
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getInstanceBootTime() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getInstanceBootTime();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated not supported.
	 */
	public void saveConfiguration() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.saveConfiguration();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated not supported.
	 */
	public void saveConfiguration(boolean aEncrypt) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.saveConfiguration(aEncrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getExternalProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	public ExternalPropertiesConfig getExternalProperties(String aKey) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getExternalProperties(aKey);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getExternalPropertiesKeys() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getExternalPropertiesKeys();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExternalProperties(ExternalPropertiesConfig aExPropConfig) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.setExternalProperties(aExPropConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExternalProperties(String aKey, ExternalPropertiesConfig aExPropConfig) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.setExternalProperties(aKey, aExPropConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveExternalProperties() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalConfigInstance.saveExternalProperties();
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aLocalConfigInstance
	 *            local config instance
	 * @param aSession
	 *            the SessionImpl object
	 * @return ConfigInstanceImpl object
	 * @throws DIException
	 *             if error occurred while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static ConfigInstanceImpl createInstance(com.ibm.di.api.local.ConfigInstance aLocalConfigInstance, SessionImpl aSession)
			throws DIException, RemoteException {
		return new ConfigInstanceImpl(aLocalConfigInstance, aSession, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGlobalUniqueID() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getGlobalUniqueID();
	}

	// Connector Pool calls

	/**
	 * {@inheritDoc}
	 */
	public String[] getConnectorPoolNames() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getConnectorPoolNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolSize(String aConnectorPoolName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getConnectorPoolSize(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorPoolFreeNum(String aConnectorPoolName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getConnectorPoolFreeNum(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolDefConfig getConnectorPoolConfig(String aConnectorPoolName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.getConnectorPoolConfig(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public int purgeConnectorPool(String aConnectorPoolName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalConfigInstance.purgeConnectorPool(aConnectorPoolName);
	}

	/**
	 * {@inheritDoc}
	 */
	public TDIProperties getTDIProperties() throws Exception, RemoteException {
		com.ibm.di.api.local.TDIProperties localTDIP = mLocalConfigInstance.getTDIProperties();
		TDIProperties remoteTDIP = TDIPropertiesImpl.createInstance(localTDIP, mSession);
		return remoteTDIP;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigPath() throws RemoteException {
		return mLocalConfigInstance.getConfigPath();
	}

	public String getConfigurationFile() throws DIException, RemoteException {
		return mLocalConfigInstance.getConfigurationFile();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addLogListener(LogListener listener) throws DIException, RemoteException {
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.15"));
		}
		com.ibm.di.api.local.LogListener localListener = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.LogListener.class);
		mLocalConfigInstance.addLogListener(localListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLogListener(LogListener listener) throws DIException, RemoteException {
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.15"));
		}

		mLocalConfigInstance.removeLogListener(RemoteListenerAdapter.create(listener, com.ibm.di.api.local.LogListener.class));
	}

	/**
	 * The content of this object must not be serialized. This method throws
	 * always. The base class <code>java.rmi.server.UnicastRemoteObject</code>
	 * implements <code>java.io.Serializable</code>, so the current class
	 * inherits that interface. However the when serializing a
	 * UnicastRemoteObject, the RMI framework writes the stub to the
	 * serialization stream and not the server object itself. As a result the
	 * serialization procedure could never reach the current derived class. and
	 * So <code>readObject</code> and <code>writeObject</code> should never get
	 * called during serialization/de-serialization.
	 * 
	 * @param in
	 *            serialization stream
	 * @throws IOException
	 *             NotSerializableException
	 * @since 7.0
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		throw new NotSerializableException();
	}

	/**
	 * @see #readObject(ObjectInputStream)
	 * @since 7.0
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException();
	}

	public Sequence startSequence(String name) throws DIException, RemoteException {
	
		com.ibm.di.api.local.Sequence sequence = mLocalConfigInstance.startSequence(name);

		return SequenceImpl.createInstance(sequence, mSession);
	}

	public Sequence startSequence(String name, TaskCallBlock tcb, boolean sync)
			throws DIException, RemoteException  {

		com.ibm.di.api.local.Sequence sequence = mLocalConfigInstance.startSequence(name, tcb, sync);

		return SequenceImpl.createInstance(sequence, mSession);
	}

	public Sequence startSequence(String name, TaskCallBlock tcb,
			AssemblyLineListener listener) throws DIException, RemoteException  {

		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.AssemblyLineListener.class);

		com.ibm.di.api.local.Sequence sequence = mLocalConfigInstance.startSequence(name, tcb, localListener);

		return SequenceImpl.createInstance(sequence, mSession);
	}

	public Map<String, Object> getSchedulerInfo(String name)
			throws RemoteException {
		return mLocalConfigInstance.getSchedulerInfo(name);
	}

	public List<Map<String, Object>> getSchedulersInfo() throws RemoteException {
		return mLocalConfigInstance.getSchedulersInfo();
	}
	
	public void unreferenced() {
		mLocalConfigInstance = null;
	}
}
