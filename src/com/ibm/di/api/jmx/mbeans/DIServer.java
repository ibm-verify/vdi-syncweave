/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.ArrayList;

import javax.management.ObjectName;

import com.ibm.di.api.AuthorizationException;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * This class implements methods exposed through JMX layer for manipulating TDI
 * Server.
 */
public class DIServer extends BaseAdmin implements DIServerMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "DIServer";

	/**
	 * Represents the local session
	 */
	private com.ibm.di.api.local.Session mLocalSession = null;

	/**
	 * Class constructor
	 * 
	 * @param aSession
	 *            com.ibm.di.api.local.Session
	 */
	public DIServer(com.ibm.di.api.local.Session aSession) {
		mLocalSession = aSession;
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
		return mLocalSession.getServerInfo().getHostName();
	}

	// Operations

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startConfigInstance(String aConfigUrl) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.startConfigInstance(aConfigUrl);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName startConfigInstance(String aConfigUrl,
			Boolean aKeepAlive, String aPassword) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.startConfigInstance(aConfigUrl, aKeepAlive.booleanValue(),
						aPassword);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public ObjectName createNewConfigInstance(String aConfigUrl)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.createNewConfigInstance(aConfigUrl);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public ObjectName createNewConfigInstance(String aConfigUrl,
			String aPassword) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.createNewConfigInstance(aConfigUrl, aPassword);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mLocalSession.shutDownServer();
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutDownServer(Integer aExitCode) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mLocalSession.shutDownServer(aExitCode.intValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSSLon() throws DIException {
		return mLocalSession.isSSLon();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean releaseConfigurationLock(String aRelativePath)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.releaseConfigurationLock(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean undoCheckOut(String aRelativePath) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.undoCheckOut(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listConfigurations(String aRelativePath)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.listConfigurations(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listFolders(String aRelativePath) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.listFolders(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList listAllConfigurations() throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.listAllConfigurations();
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.checkOutConfiguration(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath,
			String aPassword) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.checkOutConfiguration(aRelativePath, aPassword);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration,
			String aRelativePath) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mLocalSession.checkInConfiguration(aConfiguration, aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration,
			String aRelativePath, boolean aEncrypt) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mLocalSession.checkInConfiguration(aConfiguration, aRelativePath,
				aEncrypt);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig aConfiguration,
			String aRelativePath) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		mLocalSession.checkInAndLeaveCheckedOut(aConfiguration, aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig createNewConfiguration(String aRelativePath,
			boolean aOverwrite) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.createNewConfiguration(aRelativePath, aOverwrite);
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName checkOutConfigurationAndLoad(String aRelativePath,
			String aPassword) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.checkOutConfigurationAndLoad(aRelativePath, aPassword);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName checkOutConfigurationAndLoad(String aRelativePath)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.checkOutConfigurationAndLoad(aRelativePath);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public ObjectName createNewConfigurationAndLoad(String aRelativePath,
			boolean aOverwrite) throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.createNewConfigurationAndLoad(aRelativePath, aOverwrite);
		return ConfigInstance.genObjectName(ci.getConfigId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConfigurationCheckedOut(String aRelativePath)
			throws DIException {
		String userId = getCurrentUserId();
		if (userId != null && !JMXAgent.getSecRegistry().userIsAdmin(userId)) {
			throw new AuthorizationException();
		}

		return mLocalSession.isConfigurationCheckedOut(aRelativePath);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendCustomNotification(String aType, String aId, Object aData)
			throws DIException {
		// everyone is allowed to execute this method

		mLocalSession.sendCustomNotification(aType, aId, aData);
	}
}
