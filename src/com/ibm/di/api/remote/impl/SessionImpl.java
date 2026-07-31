/*
 * Copyright contributors to the SyncWeave project
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
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.ConfigurationFileListener;
import com.ibm.di.api.remote.DIEventListener;
import com.ibm.di.api.remote.LogListener;
import com.ibm.di.api.remote.SecurityRegistry;
import com.ibm.di.api.remote.ServerInfo;
import com.ibm.di.api.remote.SystemLog;
import com.ibm.di.api.remote.SystemQueue;
import com.ibm.di.api.remote.TombstoneManager;
import com.ibm.di.api.security.Identity;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * This class implements methods for managing remote Session.
 */
public class SessionImpl extends APIRemoteObject implements com.ibm.di.api.remote.Session {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 1177408831370310096L;

	/**
	 * the identity attribute
	 */
	private transient Identity mIdentity = null;

	/**
	 * api local session object
	 */
	private transient com.ibm.di.api.local.impl.SessionImpl mLocalSession = null;

	/**
	 * Constructor.
	 * 
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if an error occurs while creating a SessionImpl
	 * @throws RemoteException
	 */
	public SessionImpl(RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		mLocalSession = new com.ibm.di.api.local.impl.SessionImpl();
		mIdentity = mLocalSession.getIdentity();
	}

	/**
	 * Constructor.
	 * 
	 * @param aUserId
	 *            the user id
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if an error occurs while creating a SessionImpl
	 * @throws RemoteException
	 */
	public SessionImpl(String aUserId, RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {
		super(0, aClientSF, aServerSF);

		mLocalSession = new com.ibm.di.api.local.impl.SessionImpl(aUserId);
		mIdentity = mLocalSession.getIdentity();
	}

	// -------------------------
	// Session interface methods
	// -------------------------

	/**
	 * {@inheritDoc}
	 */
	public ServerInfo getServerInfo() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ServerInfo localServerInfo = mLocalSession.getServerInfo();
		return ServerInfoImpl.createInstance(localServerInfo, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance[] getConfigInstances() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance[] localInstances = mLocalSession.getConfigInstances();
		com.ibm.di.api.remote.ConfigInstance[] remoteInstances = new com.ibm.di.api.remote.ConfigInstance[localInstances.length];
		for (int i = 0; i < localInstances.length; i++) {
			remoteInstances[i] = ConfigInstanceImpl.createInstance(localInstances[i], this);
		}

		return remoteInstances;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getConfigInstancesIDs() throws DIException, RemoteException {
		return mLocalSession.getConfigInstancesIDs();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance getConfigInstance(String aConfigId) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.getConfigInstance(aConfigId);

		if (localConfigInstance != null) {
			return ConfigInstanceImpl.createInstance(localConfigInstance, this);
		} else {
			return null;
		}
	}

	// access to running processes in all Server Config Instances

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine[] getAssemblyLines() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.AssemblyLine[] localALs = mLocalSession.getAssemblyLines();
		com.ibm.di.api.remote.AssemblyLine[] remoteALs = new com.ibm.di.api.remote.AssemblyLine[localALs.length];
		for (int i = 0; i < localALs.length; i++) {
			remoteALs[i] = AssemblyLineImpl.createInstance(localALs[i], this);
		}
		return remoteALs;
	}

	// Operations

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String aConfigUrl) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.startConfigInstance(aConfigUrl);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String aConfigUrl, boolean aKeepAlive, String aPassword) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.startConfigInstance(aConfigUrl, aKeepAlive,
				aPassword);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps) throws DIException, RemoteException {
		return startConfigInstance(configPathOrSolutionName, keepAlive, password, runName, overrideProps, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigInstance(String aConfigUrl) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.createNewConfigInstance(aConfigUrl);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigInstance(String aConfigUrl, String aPassword) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.createNewConfigInstance(aConfigUrl, aPassword);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalSession.shutDownServer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer(int aExitCode) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalSession.shutDownServer(aExitCode);
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer(int aExitCode, boolean sync) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalSession.shutDownServer(aExitCode, sync);
	}

	// Security Registry

	/**
	 * {@inheritDoc}
	 */
	public SecurityRegistry getSecurityRegistry() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.SecurityRegistry localSecurityRegistry = mLocalSession.getSecurityRegistry();
		return SecurityRegistryImpl.createInstance(localSecurityRegistry, this);
	}

	// System Log

	/**
	 * {@inheritDoc}
	 */
	public SystemLog getSystemLog() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.SystemLog localSystemLog = mLocalSession.getSystemLog();
		return SystemLogImpl.createInstance(localSystemLog, this);
	}

	// TombstoneManager

	/**
	 * {@inheritDoc}
	 */
	public TombstoneManager getTombstoneManager() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		com.ibm.di.api.local.TombstoneManager localTombstoneManager = mLocalSession.getTombstoneManager();
		return TombstoneManagerImpl.createInstance(localTombstoneManager, this);
	}

	// Notifications

	/**
	 * {@inheritDoc}
	 */
	public void addEventListener(DIEventListener aListener, String aTypeFilter, String aIdFilter) throws DIException,
			RemoteException {
		// security check is delegated to the local implementation
		com.ibm.di.api.local.DIEventListener adapter = RemoteListenerAdapter.create(aListener,
				com.ibm.di.api.local.DIEventListener.class);
		mLocalSession.addEventListener(adapter, aTypeFilter, aIdFilter);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeEventListener(DIEventListener aListener) throws DIException, RemoteException {
		// security check is delegated to the local implementation
		com.ibm.di.api.local.DIEventListener adapter = RemoteListenerAdapter.create(aListener,
				com.ibm.di.api.local.DIEventListener.class);
		return mLocalSession.removeEventListener(adapter);
	}

	public void addEventListener(ConfigurationFileListener listener) throws DIException, RemoteException {
		com.ibm.di.api.local.ConfigurationFileListener adapter = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.ConfigurationFileListener.class);
		mLocalSession.addEventListener(adapter);
	}

	public boolean removeEventListener(ConfigurationFileListener listener) throws DIException, RemoteException {
		com.ibm.di.api.local.ConfigurationFileListener adapter = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.ConfigurationFileListener.class);
		return mLocalSession.removeEventListener(adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSSLon() throws DIException, RemoteException {
		return mLocalSession.isSSLon();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean releaseConfigurationLock(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.releaseConfigurationLock(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean undoCheckOut(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.undoCheckOut(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listConfigurations(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.listConfigurations(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listFolders(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.listFolders(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listAllConfigurations() throws DIException, RemoteException {
		return mLocalSession.listAllConfigurations();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.checkOutConfiguration(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath, String aPassword) throws DIException, RemoteException {
		return mLocalSession.checkOutConfiguration(aRelativePath, aPassword);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String aRelativePath, String aPassword) throws DIException, RemoteException {
		return ConfigInstanceImpl.createInstance(mLocalSession.checkOutConfigurationAndLoad(aRelativePath, aPassword), this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String aRelativePath) throws DIException, RemoteException {
		return ConfigInstanceImpl.createInstance(mLocalSession.checkOutConfigurationAndLoad(aRelativePath), this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration, String aRelativePath) throws DIException, RemoteException {
		mLocalSession.checkInConfiguration(aConfiguration, aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration, String aRelativePath, boolean aEncrypt) throws DIException,
			RemoteException {
		mLocalSession.checkInConfiguration(aConfiguration, aRelativePath, aEncrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig aConfiguration, String aRelativePath) throws DIException, RemoteException {
		mLocalSession.checkInAndLeaveCheckedOut(aConfiguration, aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String relativePathOrSolutionName, boolean encrypt)
			throws DIException, RemoteException {
		mLocalSession.checkInAndLeaveCheckedOut(configuration, relativePathOrSolutionName, encrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig createNewConfiguration(String aRelativePath, boolean aOverwrite) throws DIException, RemoteException {
		return mLocalSession.createNewConfiguration(aRelativePath, aOverwrite);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance createNewConfigurationAndLoad(String aRelativePath, boolean aOverwrite) throws DIException,
			RemoteException {
		return ConfigInstanceImpl.createInstance(mLocalSession.createNewConfigurationAndLoad(aRelativePath, aOverwrite), this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConfigurationCheckedOut(String aRelativePath) throws DIException, RemoteException {
		return mLocalSession.isConfigurationCheckedOut(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendCustomNotification(String aType, String aId, Object aData) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalSession.sendCustomNotification(aType, aId, aData);
	}

	/**
	 * {@inheritDoc}
	 */
	public SystemQueue getSystemQueue() throws DIException, RemoteException {

		// security check is delegated to the local implementation

		return SystemQueueImpl.newInstance(mLocalSession.getSystemQueue());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigFolderPath() {
		return mLocalSession.getConfigFolderPath();
	}

	// ---------------------
	// non-interface methods
	// ---------------------

	/**
	 * Returns the Identity;
	 * 
	 * @return Identity object
	 */
	public Identity getIdentity() {
		return mIdentity;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParams) throws DIException, RemoteException {
		return mLocalSession.invokeCustom(aCustomClassName, aMethodName, aParams);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParamsValue, String[] aParamsClass)
			throws DIException, RemoteException {
		return mLocalSession.invokeCustom(aCustomClassName, aMethodName, aParamsValue, aParamsClass);
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
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		throw new NotSerializableException();
	}

	/**
	 * @see #readObject(ObjectInputStream)
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteConfiguration(String relativePathOrSolutionName) throws DIException, RemoteException {
		mLocalSession.deleteConfiguration(relativePathOrSolutionName);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps)
			throws DIException, RemoteException {
		return startTempConfigInstance(xmlConfig, keepAlive, runName, overrideProps, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps, LogListener logListener) throws DIException, RemoteException {

		com.ibm.di.api.local.LogListener localListener = null;

		if (logListener != null) {
			localListener = RemoteListenerAdapter.create(logListener, com.ibm.di.api.local.LogListener.class);
		}

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.startConfigInstance(configPathOrSolutionName,
				keepAlive, password, runName, overrideProps, localListener);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps,
			LogListener logListener) throws DIException, RemoteException {

		com.ibm.di.api.local.LogListener localListener = null;

		if (logListener != null) {
			localListener = RemoteListenerAdapter.create(logListener, com.ibm.di.api.local.LogListener.class);
		}

		com.ibm.di.api.local.ConfigInstance localConfigInstance = mLocalSession.startTempConfigInstance(xmlConfig, keepAlive,
				runName, overrideProps, localListener);

		return ConfigInstanceImpl.createInstance(localConfigInstance, this);
	}

	public void startTombstoneManager() throws DIException, RemoteException {
		mLocalSession.startTombstoneManager();
	}

	public Object getPersistentObject(String key) throws DIException, RemoteException {
		return mLocalSession.getPersistentObject(key);
	}

	public Object setPersistentObject(String key, Object value) throws DIException, RemoteException {
		return mLocalSession.setPersistentObject(key, value);
	}

	public Object deletePersistentObject(String key) throws DIException, RemoteException {
		return mLocalSession.deletePersistentObject(key);
	}

	public String getJavaProperty(String prop) throws DIException, RemoteException {
		return mLocalSession.getJavaProperty(prop);
	}

	public void setJavaProperty(String prop, String value) throws DIException, RemoteException {
		mLocalSession.setJavaProperty(prop, value);
	}
}
