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
import java.rmi.server.Unreferenced;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

/**
 * Implements an AssemblyLine instance.
 */
public class AssemblyLineImpl extends APIRemoteObject 
	implements com.ibm.di.api.remote.AssemblyLine, Unreferenced {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = 43495849633090489L;

	/**
	 * local assembly line
	 */
	private transient com.ibm.di.api.local.AssemblyLine mLocalAssemblyLine = null;

	/**
	 * configuration instance of the assembly line
	 */
	private transient ConfigInstanceImpl mConfigInstance = null;

	/**
	 * api session object
	 */
	private transient SessionImpl mSession = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aLocalAssemblyLine
	 *            local assembly line
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            the client socket factory
	 * @param aServerSF
	 *            the server socket factory
	 * @throws DIException
	 *             if error occurs while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private AssemblyLineImpl(com.ibm.di.api.local.AssemblyLine aLocalAssemblyLine, SessionImpl aSession,
			RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.ASSEMBLYLINE.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.7"));
		}

		mLocalAssemblyLine = aLocalAssemblyLine;
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigInstance getConfigInstance() throws DIException, RemoteException {
		// everyone is allowed to execute this method

		if (mConfigInstance == null) {
			mConfigInstance = ConfigInstanceImpl.createInstance(mLocalAssemblyLine.getConfigInstance(), mSession);
		}
		return mConfigInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getUniqueCode() throws DIException, RemoteException {
		return mLocalAssemblyLine.getUniqueCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLineConfig getConfig() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getConfig();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehavior() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getNullBehavior();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehaviorValue() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getNullBehaviorValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public TaskStatistics getStatistics() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getStatistics();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isActive() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getResult() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalAssemblyLine.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop(boolean sync) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		mLocalAssemblyLine.stop(sync);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogFilePath() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getSystemLogFilePath();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogFileName() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getSystemLogFileName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLog() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getSystemLog();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSystemLogLastChunk(int aLastKilobytes) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalAssemblyLine.getSystemLogLastChunk(aLastKilobytes);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aLocalAssemblyLine
	 *            local assembly line
	 * @param aSession
	 *            the SessionImpl object
	 * @return AssemblyLineImpl object
	 * @throws DIException
	 *             if error occurs while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static AssemblyLineImpl createInstance(com.ibm.di.api.local.AssemblyLine aLocalAssemblyLine, SessionImpl aSession)
			throws DIException, RemoteException {
		return new AssemblyLineImpl(aLocalAssemblyLine, aSession, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGlobalUniqueID() throws DIException, RemoteException {
		return mLocalAssemblyLine.getGlobalUniqueID();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSimulating() throws DIException, RemoteException {

		return mLocalAssemblyLine.isSimulating();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSimulating(boolean simulate) throws DIException, RemoteException {

		mLocalAssemblyLine.setSimulating(simulate);

	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(AssemblyLineListener listener, boolean getLogs, boolean getEntryOnEachCycle) throws DIException,
			RemoteException {
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.14"));
		}
		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.AssemblyLineListener.class);
		mLocalAssemblyLine.addListener(localListener, getLogs, getEntryOnEachCycle);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(AssemblyLineListener listener) throws DIException, RemoteException {
		if (listener == null) {
			throw new DIException(sResHash.getString("SERVER.API.LISTENER.OBJECT.IS.NULL.14"));
		}
		com.ibm.di.api.local.AssemblyLineListener localListener = RemoteListenerAdapter.create(listener,
				com.ibm.di.api.local.AssemblyLineListener.class);
		mLocalAssemblyLine.removeListener(localListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void attachDebugger(int port, String host, boolean onerror) throws DIException, RemoteException {
		mLocalAssemblyLine.attachDebugger(port, host, onerror);
	}

	/**
	 * {@inheritDoc}
	 */
	public void detachDebugger(Object msg) throws DIException, RemoteException {
		mLocalAssemblyLine.detachDebugger(msg);
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

	/**
	 * {@inheritDoc}
	 */
	public boolean getComponentDebugMode(String componentName) throws DIException, RemoteException {
		return mLocalAssemblyLine.getComponentDebugMode(componentName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setComponentDebugMode(String componentName, boolean debug) throws DIException, RemoteException {
		mLocalAssemblyLine.setComponentDebugMode(componentName, debug);
	}

	public void unreferenced() {
		mLocalAssemblyLine = null;		
	}
}
