/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLine;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements several methods to handle an AssemblyLine.
 */
public class AssemblyLineHandlerImpl extends APIRemoteObject implements com.ibm.di.api.remote.AssemblyLineHandler {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -3998184288819034641L;

	/**
	 * An Assembly Line instance
	 */
	private AssemblyLine mAssemblyLine = null;

	/**
	 * the local assembly line handler
	 */
	private transient com.ibm.di.api.local.AssemblyLineHandler mLocalHandler = null;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aAssemblyLine
	 *            the assembly line
	 * @param aLocalHandler
	 *            local assembly line handler
	 * @param aSession
	 *            the SessionImpl object
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private AssemblyLineHandlerImpl(AssemblyLine aAssemblyLine, com.ibm.di.api.local.AssemblyLineHandler aLocalHandler,
			SessionImpl aSession, RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {
		super(0, aClientSF, aServerSF);

		if (aAssemblyLine == null) {
			throw new DIException(sResHash.getString("SEVER.API.ASSEMBLYLINE.OBJECT.IS.NULL.1"));
		}
		if (aLocalHandler == null) {
			throw new DIException(sResHash.getString("SEVER.API.RAW.ASSEMBLYLINE.HANDLER.OBJECT"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.6"));
		}

		mAssemblyLine = aAssemblyLine;
		mLocalHandler = aLocalHandler;
		mSession = aSession;
	}

	// No explicit security checks are performed in the interface methods.
	// We assume that if someone has the necessary rights to obtain this object,
	// he is
	// allowed to execute all its methods.

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine getAssemblyLine() throws DIException, RemoteException {
		return mAssemblyLine;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle(Entry aEntry, boolean aProcessTCB) throws DIException, RemoteException {
		return mLocalHandler.executeCycle(aEntry, aProcessTCB);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle(Entry aEntry) throws DIException, RemoteException {
		return mLocalHandler.executeCycle(aEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry executeCycle() throws DIException, RemoteException {
		return mLocalHandler.executeCycle();
	}

	public Serializable eval(String script) throws DIException, RemoteException {
		return mLocalHandler.eval(script);
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws DIException, RemoteException {
		mLocalHandler.close();
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aAssemblyLine
	 *            the assembly line
	 * @param aLocalHandler
	 *            local assembly line handler
	 * @param aSession
	 *            the SessionImpl object
	 * @return AssemblyLineHandlerImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static AssemblyLineHandlerImpl createInstance(AssemblyLine aAssemblyLine,
			com.ibm.di.api.local.AssemblyLineHandler aLocalHandler, SessionImpl aSession) throws DIException, RemoteException {
		return new AssemblyLineHandlerImpl(aAssemblyLine, aLocalHandler, aSession, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

}
