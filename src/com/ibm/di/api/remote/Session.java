/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.api.DIException;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * 
 * This is the remote Session interface which provides various methods which
 * could be used with the started TDI Server through remote session.
 * 
 */
public interface Session extends Remote {

	/**
	 * Retrieves the Server information.
	 * 
	 * @return ServerInfo object representing the information gathered from the
	 *         Server.
	 * @throws DIException
	 *             if an error occurs while retrieving the Server information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ServerInfo getServerInfo() throws DIException, RemoteException;

	/**
	 * Returns all currently started configuration instances.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var ci = session.getConfigInstances();
	 * for (i=0; i&lt;ci.length; i++) {
	 * task.logmsg(&quot;Config instance: &quot; + ci[i]);
	 * 
	 * </pre>
	 * 
	 * @return ConfigInstance array each value representing currently started
	 *         configuration instance.
	 * @throws DIException
	 *             if an error occurs while retrieving the information about the
	 *             currently started configuration instances.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance[] getConfigInstances() throws DIException, RemoteException;

	/**
	 * Returns the IDs of all currently started configuration instances.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * 	var session = (new com.ibm.di.api.local.impl.SessionFactoryImpl).createSession();
	 * 	var ciIds = session.getConfigInstancesIDs();
	 * 	for (i=0; i&lt;ciIds.length; i++) {
	 * 	task.logmsg(&quot;Config instance: &quot; + ciIds[i]);
	 * 	
	 * 	}
	 * </pre>
	 * 
	 * @return a List of the IDs of the all the active configuration instances.
	 *         If none, then an empty list is returned.
	 * @throws DIException
	 *             if an error occurs while retrieving the information about the
	 *             currently started configuration instances.
	 */
	public List<String> getConfigInstancesIDs() throws DIException, RemoteException;

	/**
	 * Returns configuration instance corresponding to a specific configuration
	 * ID.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var cID = &quot;testconfig.xml&quot;;
	 * var ci = session.getConfigInstance(cID);
	 * //do something with ci
	 * 
	 * </pre>
	 * 
	 * @param aConfigId
	 *            the ID of the wanted configuration.
	 * @return ConfigInstance object corresponding to the specified
	 *         configuration ID or null if the configuration ID is not found in
	 *         the list of currently started configurations.
	 * @throws DIException
	 *             if an error occurs while retrieving the information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance getConfigInstance(String aConfigId) throws DIException, RemoteException;

	// access to running processes in all Server Config Instances

	/**
	 * Returns started AssemblyLines corresponding to the currently started
	 * configurations.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var assemblyLines = session.getAssemblyLines();
	 * 
	 * for (i = 0; i &lt; assemblyLines.length; i++) {
	 * 	task.logmsg(&quot;AL name: &quot; + assemblyLines[i].getName());
	 * 
	 * 	// do someting with assemblyLines[i]
	 * }
	 * </pre>
	 * 
	 * @return AssemblyLine array each value corresponding to a started
	 *         AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the information about the
	 *             AssemblyLines.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public AssemblyLine[] getAssemblyLines() throws DIException, RemoteException;

	// Operations

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * 
	 * session.startConfigInstance(&quot;testconfig.xml&quot;);
	 * </pre>
	 * 
	 * @param configPathOrSolutionName
	 *            The URL where the configuration file is loaded from or the
	 *            Solution Name of the configuration file. Only configuration
	 *            files located in the configuration codebase folder can be
	 *            referenced by Solution Name.
	 * @return ConfigInstance object representing currently started
	 *         configuration instance.
	 * @throws DIException
	 *             if an error occurs on starting the new Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var pass = &quot;Difficult password&quot;;
	 * session.startConfigInstance(&quot;testconfig.xml&quot;, false, pass);
	 * </pre>
	 * 
	 * @param configPathOrSolutionName
	 *            The URL where the configuration file is loaded from or the
	 *            Solution Name of the configuration file.
	 * @param keepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code> the
	 *            Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param password
	 *            Specify the password of the configuration when it is
	 *            password-protected; specify <code>null</code> when the
	 *            configuration is not password-protected.
	 * @return ConfigInstance object representing currently started
	 *         configuration instance.
	 * @throws DIException
	 *             if an error occurs on starting the new Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password)
			throws DIException, RemoteException;

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var pass = &quot;Difficult password&quot;;
	 * session.startConfigInstance(&quot;testconfig.xml&quot;, false, pass, &quot;myrunname&quot;, &quot;mystore=mynewstore.properties&quot;);
	 * </pre>
	 * 
	 * @param configPathOrSolutionName
	 *            The URL where the configuration file is loaded from or the
	 *            Solution Name of the configuration file. Only configuration
	 *            files located in the configuration codebase folder can be
	 *            referenced by Solution Name.
	 * @param keepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code> the
	 *            Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param password
	 *            Specify the password of the configuration when it is
	 *            password-protected; specify <code>null</code> when the
	 *            configuration is not password-protected.
	 * @param runName
	 *            A name which will be used as the configuration instance id. It
	 *            must not coincide with any of the ids of running configuration
	 *            instances. It must be a valid file name on the file system, on
	 *            which TDI is running, because the configuration instance id
	 *            (which is the same as the run name) is used when storing
	 *            certain configuration-instance-specific information, such as
	 *            the System Logs. To avoid file system problems, TDI forbids
	 *            the following symbols to appear inside a run name: '\' '/' ':'
	 *            '*' '?' '"' '<' '>' '|'. The run name is an optional parameter
	 *            that can be null.
	 * @param overrideProps
	 *            Use to redirect property stores to load their contents from
	 *            files different from the ones specified in the configuration
	 *            file. Property stores and their corresponding files are
	 *            specified as key-value pairs separated with spaces, e.g.:
	 *            "mystore1=file1.properties mystore2=file2.properties". You
	 *            cannot override a property store from an included
	 *            configuration file, because its property stores are not
	 *            directly accessible in the including file. This is an optional
	 *            parameter that can be null.
	 * @return ConfigInstance object representing currently started
	 *         configuration instance.
	 * @throws DIException
	 *             If an error occurs on starting the new Config Instance or if
	 *             the run name contains any of the following symbols: '\' '/'
	 *             ':' '*' '"' '<' '>' '|'.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 * @since 7.0
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps) throws DIException, RemoteException;

	/**
	 * Starts a new Config Instance on the Server with the configuration given.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var pass = &quot;Difficult password&quot;;
	 * session.startConfigInstance(&quot;testconfig.xml&quot;, false, pass, &quot;myrunname&quot;, &quot;mystore=mynewstore.properties&quot;, null);
	 * </pre>
	 * 
	 * @param configPathOrSolutionName
	 *            The URL where the configuration file is loaded from or the
	 *            Solution Name of the configuration file. Only configuration
	 *            files located in the configuration codebase folder can be
	 *            referenced by Solution Name.
	 * @param keepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code> the
	 *            Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param password
	 *            Specify the password of the configuration when it is
	 *            password-protected; specify <code>null</code> when the
	 *            configuration is not password-protected.
	 * @param runName
	 *            A name which will be used as the configuration instance id. It
	 *            must not coincide with any of the ids of running configuration
	 *            instances. It must be a valid file name on the file system, on
	 *            which TDI is running, because the configuration instance id
	 *            (which is the same as the run name) is used when storing
	 *            certain configuration-instance-specific information, such as
	 *            the System Logs. To avoid file system problems, TDI forbids
	 *            the following symbols to appear inside a run name: '\' '/' ':'
	 *            '*' '?' '"' '<' '>' '|'. The run name is an optional parameter
	 *            that can be null.
	 * @param overrideProps
	 *            Use to redirect property stores to load their contents from
	 *            files different from the ones specified in the configuration
	 *            file. Property stores and their corresponding files are
	 *            specified as key-value pairs separated with spaces, e.g.:
	 *            "mystore1=file1.properties mystore2=file2.properties". You
	 *            cannot override a property store from an included
	 *            configuration file, because its property stores are not
	 *            directly accessible in the including file. This is an optional
	 *            parameter that can be null.
	 * @param logListener
	 *            Listener for messages logged by the configuration instance.
	 * @return ConfigInstance object representing currently started
	 *         configuration instance.
	 * @throws DIException
	 *             If an error occurs on starting the new Config Instance or if
	 *             the run name contains any of the following symbols: '\' '/'
	 *             ':' '*' '"' '<' '>' '|'.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 * @since 7.0
	 */
	public ConfigInstance startConfigInstance(String configPathOrSolutionName, boolean keepAlive, String password, String runName,
			String overrideProps, LogListener logListener) throws DIException, RemoteException;

	/**
	 * @deprecated Not supported.
	 * 
	 *             Creates and starts a new Config Instance with an empty
	 *             configuration.
	 * @param aConfigUrl
	 *            The URL of the new configuration file to be created.
	 * @throws DIException
	 *             if an error occurs while creating the new Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	@Deprecated
	public ConfigInstance createNewConfigInstance(String aConfigUrl) throws DIException, RemoteException;

	/**
	 * @deprecated Not supported.
	 * 
	 *             Creates and starts a new Config Instance with an empty
	 *             configuration.
	 * @param aConfigUrl
	 *            The URL of the new configuration file to be created.
	 * @param aPassword
	 *            If this parameter is not <code>null</code>, the new
	 *            configuration will be protected with the given password.
	 * @throws DIException
	 *             if an error occurs while creating the new Config Instance.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	@Deprecated
	public ConfigInstance createNewConfigInstance(String aConfigUrl, String aPassword) throws DIException, RemoteException;

	/**
	 * Shuts down the TDI server.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * 
	 * session.shutDownServer();
	 * </pre>
	 * 
	 * @throws DIException
	 *             if an error occurs while shutting down the server.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void shutDownServer() throws DIException, RemoteException;

	/**
	 * Shuts down the TDI Server with the specified exit code.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * 
	 * session.shutDownServer(2);
	 * </pre>
	 * 
	 * @param aExitCode
	 *            the exit code used to shut down TDI Server.
	 * @throws DIException
	 *             if an error occurs while shutting down the server.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void shutDownServer(int aExitCode) throws DIException, RemoteException;

	/**
	 * Shuts down the TDI Server with the specified exit code, after stopping
	 * all AssemblyLines and waiting a while for them to finish.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * 
	 * session.shutDownServer(0, false);
	 * </pre>
	 * 
	 * @param aExitCode
	 *            the exit code used to shut down TDI Server.
	 * @param sync
	 *            If true, do the waiting in separate Threads on the server.
	 * @throws DIException
	 *             if an error occurs while shutting down the server.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 * @since 7.1
	 */
	public void shutDownServer(int aExitCode, boolean sync) throws DIException, RemoteException;

	// Security Registry

	/**
	 * Returns information about the restrictions the current user has.
	 * 
	 * @return SecurityRegistry object which represents the restrictions to the
	 *         current user.
	 * @throws DIException
	 *             if an error occurs while retrieving security information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public SecurityRegistry getSecurityRegistry() throws DIException, RemoteException;

	// System Log

	/**
	 * Returns information about the System logging.
	 * 
	 * @return SystemLog object containing log information.
	 * @throws DIException
	 *             if an error occurs while retrieving the System logging
	 *             information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public SystemLog getSystemLog() throws DIException, RemoteException;

	// Notifications

	/**
	 * Registers an Event Listener with the Session.
	 * 
	 * @param aListener
	 *            The Event Listener to register with the Session.
	 * @param aTypeFilter
	 *            A filter for the type of events.
	 * @param aIdFilter
	 *            A filter for the id of events.
	 * @throws DIException
	 *             if an error occurs while registering the listener.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void addEventListener(DIEventListener aListener, String aTypeFilter, String aIdFilter) throws DIException,
			RemoteException;

	/**
	 * Unregisters an Event Listener with the Session.
	 * 
	 * @param aListener
	 *            The Event Listener to unregister.
	 * @return true if the EventListener is successfully unregistered, false
	 *         otherwise.
	 * @throws DIException
	 *             if an error occurs while unregistering the listener.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean removeEventListener(DIEventListener aListener) throws DIException, RemoteException;

	/**
	 * Registers a Event Listener to monitor for Configuration File changes.
	 * 
	 * @param listener
	 *            the listener to register
	 * @throws DIException
	 *             if an error occurs while registering the listener.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void addEventListener(ConfigurationFileListener listener) throws DIException, RemoteException;

	/**
	 * Unregisters an Event Listener with the Session.
	 * 
	 * @param listener
	 *            the listener to unregister.
	 * @return true if the listener is successfully unregistered, false
	 *         otherwise.
	 * @throws DIException
	 *             if an error occurs while unregistering the listener.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean removeEventListener(ConfigurationFileListener listener) throws DIException, RemoteException;

	/**
	 * Returns the TombstoneManager object. Tombstones can be queried and
	 * cleared through this object.
	 * 
	 * @return The TombstoneManager object or null if TombstoneManeger is
	 *         switched off.
	 * @throws DIException
	 *             if an error occurs while getting the TombstoneManager.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TombstoneManager getTombstoneManager() throws DIException, RemoteException;

	/**
	 * Checks if current session is over SSL.
	 * 
	 * @return true if current session is over SSL.
	 * @throws DIException
	 *             if an error occurs while retrieving the information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean isSSLon() throws DIException, RemoteException;

	// ConfigurationRegistry

	/**
	 * Administratively releases the lock of the specified configuration. This
	 * call can be only executed by users with the admin role.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return true if the configuration lock has been release, false otherwise.
	 * @throws DIException
	 *             If an error occurs during releasing the lock.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean releaseConfigurationLock(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Releases the lock on the specified configuration, thus aborting all
	 * changes being done. This call can only be executed from a user that has
	 * previously checked out the configuration and only if the configuration
	 * lock has not timed out.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return true if the undo operation is successful, false otherwise.
	 * @throws DIException
	 *             If an error occurs during releasing the lock.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean undoCheckOut(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Returns a list of all configurations in the specified folder. If a
	 * configuration has a Solution Name, this name appears in the list,
	 * otherwise in the list appears the file path of the configuration. The
	 * configurations file paths returned are relative to the Server
	 * configuration codebase folder. The returned list is based on information,
	 * gathered by the Server on startup. If a new configuration file is added
	 * in the configuration codebase folder when the Server is already running,
	 * that configuration will not be listed by the method.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *  	var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 *  	var session = sf.createSession();
	 *  
	 * 	for(alcfg in session.listConfigurations(&quot;test_folder&quot;)){
	 * 	task.logmsg(&quot;getting name&quot;);
	 * 	task.logmsg(&quot;Conf: &quot; + alcfg);
	 * 	}
	 * </pre>
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list of all configurations in the specified folder.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ArrayList listConfigurations(String aRelativePath) throws DIException, RemoteException;

	/**
	 * Returns a list of the child folders of the specified folder.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *  	var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 *  	var session = sf.createSession();
	 * 
	 * 	for(p in session.listFolders(&quot;test_folder&quot;)){
	 * 	task.logmsg(&quot;getting name&quot;);
	 * 	task.logmsg(&quot;Folder: &quot; + p);
	 * 	}
	 * </pre>
	 * 
	 * @param aRelativePath
	 *            A folder relative to the Server configuration codebase folder.
	 * @return A list of the child folders of the specified folder.
	 * @throws DIException
	 *             If an error occurs while retrieving child folder.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ArrayList listFolders(String aRelativePath) throws DIException, RemoteException;

	/**
	 * Returns a list all configurations in the directory subtree of the Server
	 * configuration codebase folder. If a configuration has a Solution Name,
	 * this name appears in the list, otherwise in the list appears the file
	 * path of the configuration. The configurations file paths returned are
	 * relative to the TDI Server configuration codebase folder. The returned
	 * list is based on information, gathered by the Server on startup. If a new
	 * configuration file is added in the configuration codebase folder when the
	 * Server is already running, that configuration will not be listed by the
	 * method.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *  	var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 *  	var session = sf.createSession();
	 * 
	 * 	for(alcfg in session.listAllConfigurations()){
	 * 		task.logmsg(&quot;getting name:&quot;);
	 * 		task.logmsg(&quot;Conf: &quot; + alcfg);
	 * 	}
	 * </pre>
	 * 
	 * @return A list of all configurations from the whole configuration
	 *         codebase directory subtree.
	 * @throws DIException
	 *             If an error occurs while retrieving configurations.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ArrayList listAllConfigurations() throws DIException, RemoteException;

	/**
	 * Checks out the specified configuration. Returns the MetamergeConfig
	 * object representing the configuration and locks that configuration on the
	 * Server.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public MetamergeConfig checkOutConfiguration(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Checks out the specified password protected configuration. Returns the
	 * MetamergeConfig object representing the configuration and locks that
	 * configuration on the Server.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param password
	 *            Specify the password for password protected configurations.
	 * @return The MetamergeConfig object representing the specified
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public MetamergeConfig checkOutConfiguration(String relativePathOrSolutionName, String password) throws DIException,
			RemoteException;

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Checks out the specified configuration and starts a temporary Config
	 * Instance on the Server.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param password
	 *            Specify the password for password protected configurations.
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while checking out the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance checkOutConfigurationAndLoad(String relativePathOrSolutionName, String password) throws DIException,
			RemoteException;

	/**
	 * Saves the specified configuration and releases the lock. If a temporary
	 * ConfigInstance has been started on check out, it will be stopped as well.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void checkInConfiguration(MetamergeConfig configuration, String relativePathOrSolutionName) throws DIException,
			RemoteException;

	/**
	 * Checks in the specified configuration and leaves it checked out. The
	 * timeout for the lock on the configuration is reset.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String relativePathOrSolutionName) throws DIException,
			RemoteException;

	/**
	 * Encrypts and saves the specified configuration and releases the lock. If
	 * a temporary Config Instance has been started on check out, it will be
	 * stopped as well.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param encrypt
	 *            If set to true, the configuration will be encrypted on the
	 *            Server.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void checkInConfiguration(MetamergeConfig configuration, String relativePathOrSolutionName, boolean encrypt)
			throws DIException, RemoteException;

	/**
	 * Checks in the specified configuration and leaves it checked out. The
	 * timeout for the lock on the configuration is reset.
	 * 
	 * @param configuration
	 *            The MetamergeConfig object representing the configuration to
	 *            be checked in.
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @param encrypt
	 *            If set to true, the configuration will be encrypted on the
	 *            Server.
	 * @throws DIException
	 *             If an error occurs while checking in the configuration.
	 */
	public void checkInAndLeaveCheckedOut(MetamergeConfig configuration, String relativePathOrSolutionName, boolean encrypt)
			throws DIException, RemoteException;

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
	 *            Specify whether to overwrite or not an already exising
	 *            configuration file.
	 * @return The MetamergeConfig object representing the newly created
	 *         configuration.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public MetamergeConfig createNewConfiguration(String aRelativePath, boolean aOverwrite) throws DIException, RemoteException;

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
	 * @return The ConfigInstance object representing the temporary
	 *         ConfigIsntance started on the Server.
	 * @throws DIException
	 *             If an error occurs while creating the new configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance createNewConfigurationAndLoad(String aRelativePath, boolean aOverwrite) throws DIException,
			RemoteException;

	/**
	 * Checks if the specified configuration is checked out on the Server.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @return true if the specified configuration is checked out, false
	 *         otherwise.
	 * @throws DIException
	 *             If an error occurs while checking the configuration.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean isConfigurationCheckedOut(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Sends a custom, user defined notification to all registered listeners.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var t = work.getAttribute(&quot;event.type&quot;);
	 * var id = work.getAttribute(&quot;event.id&quot;);
	 * var data = work.getAttribute(&quot;event.userData&quot;);
	 * session.sendCustomNotification(t, id, data);
	 * </pre>
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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void sendCustomNotification(String aType, String aId, Object aData) throws DIException, RemoteException;

	/**
	 * Gets the remote Server API SystemQueue representation object
	 * 
	 * @return the remote Server API SystemQueue representation object
	 * @throws DIException
	 *             If the System Queue is turned off or the System Queue cannot
	 *             be initialized
	 * @throws RemoteException
	 *             If the Server API RMI connection fails
	 */
	public SystemQueue getSystemQueue() throws DIException, RemoteException;

	/**
	 * Gets the value of the api.config.folder property in the remote server as
	 * a complete path. If not set, then returns an empty string.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var p = session.getConfigFolderPath();
	 * var f = new java.io.File(p);
	 * if (f.exists())
	 * 	task.logmsg(&quot;Folder path: &quot; + p);
	 * </pre>
	 * 
	 * @return The path of the config folder on the remote machine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getConfigFolderPath() throws RemoteException;

	/**
	 * Invokes the specified method from the specified class. The method must be
	 * static. This method cannot invoke methods with null parameter values. All
	 * parameter values in the parameter object array MUST be non-null. If any
	 * value is null an Exception is thrown. If a user wants to invoke a method
	 * with a null parameter, he/she must use the other
	 * <CODE>invokeCustom</CODE> method. The usage of this method can be turned
	 * on/off. There is a property in the global.properties file called
	 * api.custom.method.invoke.on. If this property is set to true then this
	 * method can be invoked, otherwise an exception is thrown if this method is
	 * invoked. There is a restriction on the classes which can be directly
	 * invoked. The allowed classes are described in another property in the
	 * global.properties file called api.custom.method.invoke.allowed.classes.
	 * Only classes listed in this property can be directly invoked by this
	 * method. If a user tries to invoke a class which is not in this list then
	 * an exception is thrown.
	 * <p>
	 * <b>Example:</b> Suppose the following class is packaged in a jar file,
	 * which is then placed in the 'jars' folder of TDI:<br>
	 * <code>
	 * public class MyClass {<br> 
	 * public static Integer multiply(Integer a , Integer b){  <br>
	 * return new Integer(a.intValue() * b.intValue()); <br>
	 *  }<br>
	 * }<br></code> Suppose the global.properties TDI configuration file
	 * contains the following lines:<br>
	 * 
	 * <code>api.custom.method.invoke.on=true</code><br>
	 * <code>api.custom.method.invoke.allowed.classes=MyClass</code><br>
	 * Now we can do the following:
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var result = session.invokeCustom(&quot;MyClass&quot;, &quot;multiply&quot;, new Array(3, 5));
	 * </pre>
	 * 
	 * @param aCustomClassName
	 *            The class containing the method to be invoked.
	 * @param aMethodName
	 *            The name of the method to be invoked.
	 * @param aParams
	 *            Array of parameters used by the invoked method.
	 * @return the result of dispatching the method represented with parameters
	 *         aParams
	 * @throws DIException
	 *             If an error occurs while invoking the method.
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParams) throws DIException, RemoteException;

	/**
	 * Invokes the specified method from the specified class. The method must be
	 * static. This method is used when the user wants to invoke the specified
	 * method with a null parameter value. The main difference between these two
	 * <CODE>invokeCustom</CODE> methods is that when using this method the user
	 * MUST specify the parameters' type. The usage of this method can be turned
	 * on/off. There is a property in the global.properties file called
	 * api.custom.method.invoke.on. If this property is set to true then this
	 * method can be invoked, otherwise an exception is thrown if this method is
	 * invoked. There is a restriction on the classes which can be directly
	 * invoked. The allowed classes are described in another property in the
	 * global.properties file called api.custom.method.invoke.allowed.classes.
	 * Only classes listed in this property can be directly invoked by this
	 * method. If the user tries to invoke a class which is not in this list
	 * then an exception is thrown.
	 * <p>
	 * <b>Example:</b> Suppose the following class is packaged in a jar file,
	 * which is then placed in the 'jars' folder of TDI:<br>
	 * <code>
	 * public class MyClass {<br> 
	 * public static Integer multiply(Integer a , Integer b){  <br>
	 * return new Integer(a.intValue() * b.intValue()); <br>
	 *  }<br>
	 * }<br></code> Suppose the global.properties TDI configuration file
	 * contains the following lines:<br>
	 * 
	 * <code>api.custom.method.invoke.on=true</code><br>
	 * <code>api.custom.method.invoke.allowed.classes=MyClass</code><br>
	 * Now we can do the following:
	 * 
	 * <pre>
	 * var sf = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var session = sf.createSession();
	 * var result = session.invokeCustom(&quot;MyClass&quot;, &quot;multiply&quot;, new Array(3, 5), new Array(&quot;java.lang.Integer&quot;, &quot;java.lang.Integer&quot;));
	 * </pre>
	 * 
	 * @param aCustomClassName
	 *            The class containing the method to be invoked.
	 * @param aMethodName
	 *            The name of the method to be invoked.
	 * @param aParamsValue
	 *            Array of parameters used by the invoked method.
	 * @param aParamsClass
	 *            The type of the parameters used by the invoked method.
	 * @return the result of dispatching the method represented with parameters'
	 *         types aParamsClass.
	 * @throws DIException
	 *             If an error occurs while invoking the method.
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 */
	public Object invokeCustom(String aCustomClassName, String aMethodName, Object[] aParamsValue, String[] aParamsClass)
			throws DIException, RemoteException;

	/**
	 * Delete a file from the configuration codebase folder. You must be admin
	 * to execute this method.
	 * 
	 * @param relativePathOrSolutionName
	 *            The path to the configuration relative to the Server
	 *            configuration codebase folder or the Solution Name of the
	 *            configuration (the configuration must be inside the
	 *            configuration codebase folder).
	 * @throws DIException
	 *             The file is currently checked-out or deletion failed (e.g.
	 *             the file does not exist).
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 * @since 7.0
	 */
	public void deleteConfiguration(String relativePathOrSolutionName) throws DIException, RemoteException;

	/**
	 * Start a configuration instance which has no associated configuration
	 * file. You must be admin to execute this method.
	 * 
	 * @param xmlConfig
	 *            A configuration as a XML string.
	 * @param keepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code> the
	 *            Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param runName
	 *            A name which will be used as the configuration instance id. It
	 *            must not coincide with any of the id's of running
	 *            configuration instances. It must not contain forward or back
	 *            slashes or colons. This is an optional parameter that can be
	 *            null.
	 * @param overrideProps
	 *            Use to redirect property stores to load their contents from
	 *            files different from the ones specified in the configuration.
	 *            Property stores and their corresponding files are specified as
	 *            key-value pairs separated with spaces, e.g.:
	 *            "mystore1=file1.properties mystore2=file2.properties". You
	 *            cannot override a property store from an included
	 *            configuration, because its property stores are not directly
	 *            accessible. This is an optional parameter that can be null.
	 * @return A handle to the started configuration instance.
	 * @throws DIException
	 *             If an error occurs on starting the new Config Instance or if
	 *             the run name is not a valid file name.
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 * @since 7.0
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps)
			throws DIException, RemoteException;

	/**
	 * Start a configuration instance which has no associated configuration
	 * file. You must be admin to execute this method.
	 * 
	 * @param xmlConfig
	 *            A configuration as a XML string.
	 * @param keepAlive
	 *            When <code>true</code> the Config Instance will stay alive
	 *            even when no threads are running, when <code>false</code> the
	 *            Config Instance will automatically terminate when its last
	 *            thread terminates.
	 * @param runName
	 *            A name which will be used as the configuration instance id. It
	 *            must not coincide with any of the id's of running
	 *            configuration instances. It must not contain forward or back
	 *            slashes or colons. This is an optional parameter that can be
	 *            null.
	 * @param overrideProps
	 *            Use to redirect property stores to load their contents from
	 *            files different from the ones specified in the configuration.
	 *            Property stores and their corresponding files are specified as
	 *            key-value pairs separated with spaces, e.g.:
	 *            "mystore1=file1.properties mystore2=file2.properties". You
	 *            cannot override a property store from an included
	 *            configuration, because its property stores are not directly
	 *            accessible. This is an optional parameter that can be null.
	 * @param logListener
	 *            Listener for messages logged by this configuration instance.
	 * @return A handle to the started configuration instance.
	 * @throws DIException
	 *             If an error occurs on starting the new Config Instance or if
	 *             the run name is not a valid file name.
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 * @since 7.0
	 */
	public ConfigInstance startTempConfigInstance(String xmlConfig, boolean keepAlive, String runName, String overrideProps,
			LogListener logListener) throws DIException, RemoteException;

	/**
	 * Starts the TombstoneManager.
	 * @throws DIException
	 * @throws RemoteException
	 *             If a communication-related exception occurs.
	 */
	public void startTombstoneManager() throws DIException, RemoteException;

	/**
	 * This method retrieves a named object from the default system property store.
	 * 
	 * @param key
	 *            The unique key
	 * @return Object
	 * @throws Exception
	 */
	public Object getPersistentObject(String key) throws DIException, RemoteException;

	/**
	 * This method stores a named object in the default system property store.
	 * 
	 * @param key
	 *            The unique key
	 * @param value
	 *            The object to store (must be java serializable)
	 * @return The old object if any
	 * @throws Exception
	 */
	public Object setPersistentObject(String key, Object value) throws DIException, RemoteException;

	/**
	 * This method deletes a named object in the default system property store.
	 * 
	 * @param key
	 *            The unique key
	 * @return The old object if any
	 * @throws Exception
	 */
	public Object deletePersistentObject(String key) throws DIException, RemoteException;

	/**
	 * Returns the value of a Java System property.
	 * 
	 * @param prop
	 *            The property name
	 * @return The property value or null if no such property exists
	 */
	public String getJavaProperty(String prop) throws DIException, RemoteException;

	/**
	 * Sets the value of a Java System property.
	 * 
	 * @param prop
	 *            The property name
	 * @param value
	 *            The property value
	 */
	public void setJavaProperty(String prop, String value) throws DIException, RemoteException;
}