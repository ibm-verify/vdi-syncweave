/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Sequence;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

public class SequenceImpl extends APIRemoteObject implements Sequence {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625951535327607718L;

	private transient com.ibm.di.api.local.Sequence localSequence;
	
	private transient ConfigInstanceImpl configInstance;

	/**
	 * api session object
	 */
	private transient SessionImpl session = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param localSequence
	 *            local Sequence
	 * @param session
	 *            the SessionImpl object
	 * @param clientSF
	 *            the client socket factory
	 * @param serverSF
	 *            the server socket factory
	 * @throws DIException
	 *             if error occurs while creating instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private SequenceImpl(com.ibm.di.api.local.Sequence localSequence, SessionImpl session,
			RMIClientSocketFactory clientSF, RMIServerSocketFactory serverSF) throws DIException, RemoteException {
		super(0, clientSF, serverSF);

		if (localSequence == null) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.ASSEMBLYLINE.IS.NULL"));
		}
		if (session == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.7"));
		}

		this.localSequence = localSequence;
		this.session = session;
	}

	public static SequenceImpl createInstance (com.ibm.di.api.local.Sequence sequence, SessionImpl session)
			throws DIException, RemoteException {
		return new SequenceImpl(sequence, session, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

	public ConfigInstance getConfigInstance() throws DIException, RemoteException {
		if (configInstance == null) {
			configInstance = ConfigInstanceImpl.createInstance(localSequence.getConfigInstance(), session);
		}
		return configInstance;
	}

	public String getName() throws DIException, RemoteException {
			return localSequence.getName();
	}

	public Entry getResult() throws DIException, RemoteException {
		return localSequence.getResult();
	}

	public TaskStatistics getStatistics() throws DIException, RemoteException {
		return localSequence.getStatistics();
	}

	public int getUniqueCode() throws DIException, RemoteException {
		return localSequence.getUniqueCode();
	}

	public boolean isActive() throws DIException, RemoteException {
		return localSequence.isActive();
	}

	public void stop() throws DIException, RemoteException {
		localSequence.stop();
	}

	public void stop(boolean sync) throws DIException, RemoteException {
		localSequence.stop(sync);
	}

}
