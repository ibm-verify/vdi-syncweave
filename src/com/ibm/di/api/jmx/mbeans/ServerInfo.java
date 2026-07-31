/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;

/**
 * 
 * This class implements various methods for getting server information.
 * 
 */
public class ServerInfo extends BaseAdmin implements ServerInfoMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "ServerInfo";

	/**
	 * {@link com.ibm.di.api.local.ServerInfo}
	 */
	private com.ibm.di.api.local.ServerInfo mServerInfo;

	/**
	 * Class constructor.
	 * 
	 * @param aServerInfo
	 *            {@link com.ibm.di.api.local.ServerInfo}
	 */
	public ServerInfo(com.ibm.di.api.local.ServerInfo aServerInfo) {
		mServerInfo = aServerInfo;
	}

	// MBean interface

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() throws DIException {
		return getIPAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerVersion() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getServerVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIPAddress() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getIPAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHostName() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getHostName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOperatingSystem() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getOperatingSystem();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getServerBootTime() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getServerBootTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerID() throws DIException {
		// everyone is allowed to execute this method

		return mServerInfo.getServerID();
	}

	// Connectors information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable[] getInstalledConnectors() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledConnectors();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledConnectorsNames() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledConnectorsNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorDescription(String aConnectorName)
			throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getConnectorDescription(aConnectorName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorVersionInfo(String aConnectorName)
			throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getConnectorVersionInfo(aConnectorName);
	}

	// Parsers information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable[] getInstalledParsers() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledParsers();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledParsersNames() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledParsersNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserDescription(String aParserName) throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getParserDescription(aParserName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParserVersionInfo(String aParserName) throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getParserVersionInfo(aParserName);
	}

	// Function Components information

	/**
	 * {@inheritDoc}
	 */
	public Hashtable[] getInstalledFunctionComponents() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledFunctionComponents();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getInstalledFunctionComponentsNames() throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getInstalledFunctionComponentsNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentDescription(String aFunctionComponentName)
			throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo
				.getFunctionComponentDescription(aFunctionComponentName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFunctionComponentVersionInfo(String aFunctionComponentName)
			throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo
				.getFunctionComponentVersionInfo(aFunctionComponentName);
	}

	/**
	 * Verifies user ID.
	 * 
	 * @throws DIException
	 *             if the user isn't admin.
	 */
	private void checkIfUserIsAdmin() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Vector getPasswordParameterNames(String aJavaClassName)
			throws DIException {
		checkIfUserIsAdmin();

		return mServerInfo.getPasswordParameterNames(aJavaClassName);
	}

}
