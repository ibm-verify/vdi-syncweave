/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.management.ObjectName;

import com.ibm.di.api.DIException;
import com.ibm.di.config.interfaces.MetamergeConfig;
import java.util.ArrayList;

/**
 * 
 * DIServerMBean interface that defines public methods exposed through JMX layer
 * for manipulating TDI Server.
 * 
 */
public interface DIServerMBean extends BaseAdminMBean {
	// Operations

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * 
	 * @param aConfigUrl
	 *            The URL where the configuration file is loaded from.
	 * @return ObjectName generated from the configuration ID.
	 * @throws DIException
	 *             if an error occurs on starting the new Config Instance.
	 */
	public ObjectName startConfigInstance(String aConfigUrl) throws DIException;

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * 
	 * @param aConfigUrl
	 *            The URL where the configuration file is loaded from.
	 * @param aKeepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code>
	 *            the Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param aPassword
	 *            Specify the password of the configuration when it is
	 *            password-protected; specify <code>null</code> when the
	 *            configuration is not password-protected.
	 * @return ObjectName generated from the configuration ID.
	 * @throws DIException
	 *             if an error occurs on starting the new Config Instance.
	 */
	public ObjectName startConfigInstance(String aConfigUrl,
			Boolean aKeepAlive, String aPassword) throws DIException;

	/**
	 * @deprecated Not for public use in the future.
	 * 
	 * Creates and starts a new Config Instance with an empty configuration.
	 * @param aConfigUrl
	 *            The URL of the new configuration file to be created.
	 * @throws DIException
	 *             if an error occurs while creating the new Config Instance.
	 */
	public ObjectName createNewConfigInstance(String aConfigUrl)
			throws DIException;

	/**
	 * @deprecated Not for public use in the future.
	 * 
	 * Creates and starts a new Config Instance with an empty configuration.
	 * @param aConfigUrl
	 *            The URL of the new configuration file to be created.
	 * @param aPassword
	 *            If this parameter is not <code>null</code>, the new
	 *            configuration will be protected with the given password.
	 * @throws DIException
	 *             if an error occurs while creating the new Config Instance.
	 */
	public ObjectName createNewConfigInstance(String aConfigUrl,
			String aPassword) throws DIException;

	/**
	 * Shuts down the TDI Server.
	 * 
	 * @throws DIException
	 *             if an error occurs while shutting down the server.
	 */
	public void shutDownServer() throws DIException;

	/**
	 * Shuts down the TDI Server with the specified exit code.
	 * 
	 * @param aExitCode
	 *            the exit code used to shut down TDI Server.
	 * @throws DIException
	 *             if an error occurs while shutting down the server.
	 */
	public void shutDownServer(Integer aExitCode) throws DIException;

	/**
	 * Checks if the SSL on the server is turned on.
	 * 
	 * @return true if SSL is enabled on server
	 * @throws DIException
	 *             if an error occurs while retrieving the information.
	 */
	public boolean isSSLon() throws DIException;

	// ConfigurationRegistry

	/**
	 * Administratively releases the lock of the specified configuration. This
	 * call can be only executed by users with the admin role.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @return true if the configuration lock has been release, false otherwise.
	 * @throws DIException
	 *             If an error occurs during releasing the lock.
	 */
	public boolean releaseConfigurationLock(String aRelativePath)
			throws DIException;

	/**
	 * Releases the lock on the specified configuration, thus aborting all
	 * changes being done. This call can only be executed from a user that has
	 * previously checked out the configuration and only if the configuration
	 * lock has not timed out.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @return true if the undo operation is successful, false otherwise.
	 * @throws DIException
	 *             If an error occurs during releasing the lock.
	 */
	public boolean undoCheckOut(String aRelativePath) throws DIException;

	/**
	 * Returns a list of the file names of all configurations in the specified
	 * folder. The configurations file paths returned are relative to the Server
	 * configuration codebase folder.
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list with the file names of all configurations in the specified
	 *         folder.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 */
	public ArrayList listConfigurations(String aRelativePath)
			throws DIException;

	/**
	 * Returns a list of the child folders of the specified folder.
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list of the child folders of the specified folder.
	 * @throws DIException
	 *             If an error occurs while retrieving child folder.
	 */
	public ArrayList listFolders(String aRelativePath) throws DIException;

	/**
	 * Returns a list of the file names of all configurations in the directory
	 * subtree of the Server configuration codebase folder. The configurations
	 * file paths returned are relative to the TDI Server configuration codebase
	 * folder.
	 * 
	 * @return A list of the file names of all configurations from the whole
	 *         configuration codebase directory subtree.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 */
	public ArrayList listAllConfigurations() throws DIException;

	/**
	 * Checks out the specified configuration. Returns the MetamergeConfig
	 * object representing the configuration and locks that configuration on the
	 * Server.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath)
			throws DIException;

	/**
	 * Checks out the specified password protected configuration. Returns the
	 * MetamergeConfig object representing the configuration and locks that
	 * configuration on the Server.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @param aPassword
	 *            Specify the password for password protected configurations.
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public MetamergeConfig checkOutConfiguration(String aRelativePath,
			String aPassword) throws DIException;

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @param aPassword
	 *            Specify the password for password protected configurations.
	 * @return The ConfigInstance Object Name for the temporary ConfigIsntance
	 *         started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public ObjectName checkOutConfigurationAndLoad(String aRelativePath,
			String aPassword) throws DIException;

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * 
	 * @return The ConfigInstance Object Name for the temporary ConfigIsntance
	 *         started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 */
	public ObjectName checkOutConfigurationAndLoad(String aRelativePath)
			throws DIException;

	/**
	 * Saves the specified configuration and releases the lock. If a temporary
	 * ConfigInstance has been started on check out, it will be stopped as well.
	 * 
	 * @param aConfiguration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param aRelativePath
	 *            The path of the configuration relative to the Server
	 *            configuration codebase folder.
	 * 
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration,
			String aRelativePath) throws DIException;

	/**
	 * Encrypts and saves the specified configuration and releases the lock. If
	 * a temporary Config Instance has been started on check out, it will be
	 * stopped as well.
	 * 
	 * @param aConfiguration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param aRelativePath
	 *            The path of the configuration relative to the Server
	 *            configuration codebase folder.
	 * @param aEncrypt
	 *            If set to true, the configuration will be encrypted on the
	 *            Server.
	 * 
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInConfiguration(MetamergeConfig aConfiguration,
			String aRelativePath, boolean aEncrypt) throws DIException;

	/**
	 * Checks in the specified configuration and leaves it checked out. The
	 * timeout for the lock on the configuration is reset.
	 * 
	 * @param aConfiguration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param aRelativePath
	 *            The path of the configuration relative to the Server
	 *            configuration codebase folder.
	 * 
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig aConfiguration,
			String aRelativePath) throws DIException;

	/**
	 * Creates a new empty configuration and immediately checks it out. If a
	 * configuration with the specified path already exists and the aOverwrite
	 * parameter is set to false the operation will fail and an Exception will
	 * be thrown.
	 * 
	 * @param aRelativePath
	 *            The path of the new configuration file relative to the Server
	 *            configuration codebase folder.
	 * @param aOverwrite
	 *            Specify whether to overwrite or not an already existing
	 *            configuration file.
	 * @return The MetamergeConfig object representing the newly created
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 */
	public MetamergeConfig createNewConfiguration(String aRelativePath,
			boolean aOverwrite) throws DIException;

	/**
	 * Creates a new empty configuration, immediately checks it out and loads a
	 * temporary Config Instance on the Server. If a configuration with the
	 * specified path already exists and the aOverwrite parameter is set to
	 * false the operation will fail and an Exception will be thrown.
	 * 
	 * @param aRelativePath
	 *            The path of the new configuration file relative to the Server
	 *            configuration codebase folder.
	 * @param aOverwrite
	 *            Specify whether to overwrite or not an already existing
	 *            configuration file.
	 * @return The ConfigInstance Object Name for the temporary ConfigIsntance
	 *         started on the Server.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 */
	public ObjectName createNewConfigurationAndLoad(String aRelativePath,
			boolean aOverwrite) throws DIException;

	/**
	 * Checks if the specified configuration is checked out on the Server.
	 * 
	 * @param aRelativePath
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder.
	 * @return true if the specified configuration is checked out, false
	 *         otherwise.
	 * @throws DIException
	 *             If an error occurs while checking the configuration.
	 */
	public boolean isConfigurationCheckedOut(String aRelativePath)
			throws DIException;

	/**
	 * Sends a custom, user defined notification to all registered listeners.
	 * 
	 * @param aType
	 *            Notification type, will be automatically prefixed with "user."
	 * @param aId
	 *            Notification ID, usually identifies the object this event
	 *            originated from.
	 * @param aData
	 *            Custom user data. Make sure the object passed is serializable
	 *            if you want to send this event notification in a remote
	 *            context.
	 * @throws DIException
	 *             If an error occurs while sending the notification.
	 */
	public void sendCustomNotification(String aType, String aId, Object aData)
			throws DIException;
}
