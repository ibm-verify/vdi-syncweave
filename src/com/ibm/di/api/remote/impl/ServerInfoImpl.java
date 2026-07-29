/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.model.descriptor.ComponentDescriptor;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements various methods for getting server information.
 */
public class ServerInfoImpl extends APIRemoteObject implements com.ibm.di.api.remote.ServerInfo {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	private static final long serialVersionUID = -2687798353167357701L;

	/**
	 * api session object
	 */
	private SessionImpl mSession = null;

	/**
	 * local server info
	 */
	private transient com.ibm.di.api.local.ServerInfo mLocalServerInfo = null;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * 
	 * @param aLocalServerInfo
	 *            local server info
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
	private ServerInfoImpl(com.ibm.di.api.local.ServerInfo aLocalServerInfo, SessionImpl aSession,
			RMIClientSocketFactory aClientSF, RMIServerSocketFactory aServerSF) throws DIException, RemoteException {
		super(0, aClientSF, aServerSF);

		if (aLocalServerInfo == null) {
			throw new DIException(sResHash.getString("SEVER.API.LOCAL.SERVER.INFO.OBJECT.IS.NULL"));
		}
		if (aSession == null) {
			throw new DIException(sResHash.getString("SEVER.API.SESSION.OBJECT.IS.NULL.11"));
		}

		mLocalServerInfo = aLocalServerInfo;
		mSession = aSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerVersion() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getServerVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIPAddress() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getIPAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHostName() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getHostName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOperatingSystem() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getOperatingSystem();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getServerBootTime() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getServerBootTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerID() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getServerID();
	}

	// Connectors information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledConnectors() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledConnectors();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledConnectorsNames() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledConnectorsNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorDescription(String aConnectorName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getConnectorDescription(aConnectorName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorVersionInfo(String aConnectorName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getConnectorVersionInfo(aConnectorName);
	}

	// Parsers information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledParsers() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledParsers();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledParsersNames() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledParsersNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserDescription(String aParserName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getParserDescription(aParserName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserVersionInfo(String aParserName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getParserVersionInfo(aParserName);
	}

	// Function Components information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable<?, ?>[] getInstalledFunctionComponents() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledFunctionComponents();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledFunctionComponentsNames() throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getInstalledFunctionComponentsNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentDescription(String aFunctionComponentName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getFunctionComponentDescription(aFunctionComponentName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentVersionInfo(String aFunctionComponentName) throws DIException, RemoteException {
		// security check is delegated to the local implementation

		return mLocalServerInfo.getFunctionComponentVersionInfo(aFunctionComponentName);
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @param aLocalServerInfo
	 *            local server info
	 * @param aSession
	 *            the SessionImpl object
	 * @return ServerInfoImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static ServerInfoImpl createInstance(com.ibm.di.api.local.ServerInfo aLocalServerInfo, SessionImpl aSession)
			throws DIException, RemoteException {
		return new ServerInfoImpl(aLocalServerInfo, aSession, APIEngine.getClientSF(), APIEngine.getServerSF());
	}

	/**
	 * {@inheritDoc}
	 */
	public Vector<String> getPasswordParameterNames(String aJavaClassName) throws DIException, RemoteException {
		return mLocalServerInfo.getPasswordParameterNames(aJavaClassName);
	}

	/**
	 * {@inheritDoc}
	 */
	public ComponentDescriptor getInstalledComponentDescriptor(String componentName) throws DIException, RemoteException {
		return mLocalServerInfo.getInstalledComponentDescriptor(componentName);
	}
}
