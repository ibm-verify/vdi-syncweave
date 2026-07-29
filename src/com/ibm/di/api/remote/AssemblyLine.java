/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ibm.di.api.DIException;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskStatistics;

/**
 * Represents an AssemblyLine instance.
 */
public interface AssemblyLine extends Remote {

	/**
	 * Returns the configuration instance of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var mAssemblyLine = remoteSession.getAssemblyLines()[0];
	 * 
	 * if (mAssemblyLine != null) {
	 * 	var mConfigInstance = mAssemblyLine.getConfigInstance();
	 * 
	 * 	var configId = mConfigInstance.getConfigId();
	 * 
	 * 	var aAssemblyLineName = &quot;AssemblyLine&quot;;
	 * 
	 * 	var assemblyLine = mConfigInstance.startAssemblyLine(aAssemblyLineName);
	 * 
	 * 	var objectName = com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(assemblyLine.getName(), assemblyLine.getUniqueCode());
	 * } else {
	 * 	main.logmsg(&quot;No Remote Assembly Lines Running&quot;);
	 * }
	 * </pre>
	 * 
	 * @return Returns ConfigInstance object representing the configuration
	 *         instance of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the configuration
	 *             instance information.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public ConfigInstance getConfigInstance() throws DIException, RemoteException;

	/**
	 * Returns the name of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var mAssemblyLine = remoteSession.getAssemblyLines()[0];
	 * 
	 * if (mAssemblyLine != null) {
	 * 	var mConfigInstance = mAssemblyLine.getConfigInstance();
	 * 
	 * 	var configId = mConfigInstance.getConfigId();
	 * 
	 * 	var aAssemblyLineName = &quot;AssemblyLine&quot;;
	 * 
	 * 	var assemblyLine = mConfigInstance.startAssemblyLine(aAssemblyLineName);
	 * 
	 * 	var objectName = com.ibm.di.api.jmx.mbeans.AssemblyLine.genObjectName(assemblyLine.getName(), assemblyLine.getUniqueCode());
	 * } else {
	 * 	main.logmsg(&quot;No Remote Assembly Lines Running&quot;);
	 * }
	 * </pre>
	 * 
	 * @return String object representing the AssemblyLine's name.
	 * @throws DIException
	 *             if an error occurs while retrieving the name of the
	 *             AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getName() throws DIException, RemoteException;

	/**
	 * Returns the unique code of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var remALListener = new com.ibm.di.admin.remote.RemoteALListener(remoteSession, false);
	 * var runningAL = session.getAssemblyLines()[0];
	 * remALListener.setCode(runningAL.getUniqueCode());
	 * </pre>
	 * 
	 * @return int value representing the unique code of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the unique code of the
	 *             AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public int getUniqueCode() throws DIException, RemoteException;

	// AssemblyLine configuration

	/**
	 * Returns configuration information about the AssemblyLine.
	 * 
	 * @return AssemblyLineConfig representing the configuration information of
	 *         the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the configuration
	 *             information of the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public AssemblyLineConfig getConfig() throws DIException, RemoteException;

	/**
	 * Gets the nullBehavior attribute of the AssemblyLine object
	 * 
	 * @return String object representing the nullBehavior attribute value or
	 *         null if no setting values are available for the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the nullBehavior attribute.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getNullBehavior() throws DIException, RemoteException;

	/**
	 * Gets the nullBehaviorValue attribute of the AssemblyLine object.
	 * 
	 * @return String object representing the nullBehaviorValue attribute value
	 *         or null if no setting values are available for the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the nullBehaviorValue
	 *             attribute.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getNullBehaviorValue() throws DIException, RemoteException;

	// AssemblyLine statistics

	/**
	 * This method returns the TaskStatistics object for this AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var runningALs = null;
	 * var stats = null;
	 * var result = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * if (runningConfigs != null) {
	 * 	for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 		runningALs = runningConfigs[i].getAssemblyLines();
	 * 
	 * 		for (var j = 0; j &lt; runningALs.length; j++) {
	 * 			if (runningALs[j].isActive()) {
	 * 				stats = runningALs[j].getStatistics().toString();
	 * 				main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 
	 * 				result = runningALs[j].getResult();
	 * 
	 * 				var attr = new com.ibm.di.entry.Attribute(&quot;LogURL&quot;, &quot;/log/&quot; + runningALs[j].getGlobalUniqueID());
	 * 
	 * 				runningALs[j].stop();
	 * 			}
	 * 		}
	 * 	}
	 * } else {
	 * 	main.logmsg(&quot;No remote config instances available&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The accumulated TaskStatistics object.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine statistics.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public TaskStatistics getStatistics() throws DIException, RemoteException;

	// Operations

	/**
	 * Checks if the AssemblyLine is active.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var runningALs = null;
	 * var stats = null;
	 * var result = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * if (runningConfigs != null) {
	 * 	for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 		runningALs = runningConfigs[i].getAssemblyLines();
	 * 
	 * 		for (var j = 0; j &lt; runningALs.length; j++) {
	 * 			if (runningALs[j].isActive()) {
	 * 				stats = runningALs[j].getStatistics().toString();
	 * 				main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 
	 * 				result = runningALs[j].getResult();
	 * 
	 * 				var attr = new com.ibm.di.entry.Attribute(&quot;LogURL&quot;, &quot;/log/&quot; + runningALs[j].getGlobalUniqueID());
	 * 
	 * 				runningALs[j].stop();
	 * 			}
	 * 		}
	 * 	}
	 * } else {
	 * 	main.logmsg(&quot;No remote config instances available&quot;);
	 * }
	 * </pre>
	 * 
	 * @return true if the AssemblyLine's thread is alive, false otherwise.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine state.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean isActive() throws DIException, RemoteException;

	/**
	 * This method returns the result entry object. This object is a copy of the
	 * working entry as it were when the AssemblyLine finished processing the
	 * connectors.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var runningALs = null;
	 * var stats = null;
	 * var result = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * if (runningConfigs != null) {
	 * 	for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 		runningALs = runningConfigs[i].getAssemblyLines();
	 * 
	 * 		for (var j = 0; j &lt; runningALs.length; j++) {
	 * 			if (runningALs[j].isActive()) {
	 * 				stats = runningALs[j].getStatistics().toString();
	 * 				main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 
	 * 				result = runningALs[j].getResult();
	 * 
	 * 				var attr = new com.ibm.di.entry.Attribute(&quot;LogURL&quot;, &quot;/log/&quot; + runningALs[j].getGlobalUniqueID());
	 * 
	 * 				runningALs[j].stop();
	 * 			}
	 * 		}
	 * 	}
	 * } else {
	 * 	main.logmsg(&quot;No remote config instances available&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The last "work" entry object.
	 * @throws DIException
	 *             if an error occurs while getting the result Entry.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Entry getResult() throws DIException, RemoteException;

	/**
	 * Stops the execution of the AssemblyLine.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var runningALs = null;
	 * var stats = null;
	 * var result = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * if (runningConfigs != null) {
	 * 	for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 		runningALs = runningConfigs[i].getAssemblyLines();
	 * 
	 * 		for (var j = 0; j &lt; runningALs.length; j++) {
	 * 			if (runningALs[j].isActive()) {
	 * 				stats = runningALs[j].getStatistics().toString();
	 * 				main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 
	 * 				result = runningALs[j].getResult();
	 * 
	 * 				var attr = new com.ibm.di.entry.Attribute(&quot;LogURL&quot;, &quot;/log/&quot; + runningALs[j].getGlobalUniqueID());
	 * 
	 * 				runningALs[j].stop();
	 * 			}
	 * 		}
	 * 	}
	 * } else {
	 * 	main.logmsg(&quot;No remote config instances available&quot;);
	 * }
	 * </pre>
	 * 
	 * @throws DIException
	 *             if an error occurs while stopping the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void stop() throws DIException, RemoteException;

	/**
	 * Stops the execution of the AssemblyLine, and waits for it to stop.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var stats = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 	var runningALs = runningConfigs[i].getAssemblyLines();
	 * 	for (var j = 0; j &lt; runningALs.length; j++) {
	 * 		if (runningALs[j].isActive()) {
	 * 			main.logmsg(&quot;Stopping [&quot; + j + &quot;]: &quot; + runningALs[j].getName());
	 * 
	 * 			runningALs[j].stop(false);
	 * 
	 * 			stats = runningALs[j].getStatistics().toString();
	 * 			main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 		}
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param sync
	 *            If true, create a new Thread on the server to do the waiting.
	 * @throws DIException
	 *             if an error occurs while stopping the AssemblyLine.
	 * @since 7.1
	 */
	public void stop(boolean sync) throws DIException, RemoteException;

	// System Log

	/**
	 * Returns the fully-qualified path of the log file of the AssemblyLine.
	 * 
	 * @return the fully-qualified log file path.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine log file
	 *             path.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getSystemLogFilePath() throws DIException, RemoteException;

	/**
	 * Returns the name of the log file of the AssemblyLine (not prefixed by
	 * folders path).
	 * 
	 * @return the log file name.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine log file
	 *             name.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getSystemLogFileName() throws DIException, RemoteException;

	/**
	 * Retrieves the current AssemblyLine's system log.
	 * 
	 * @return the log generated by the AssemblyLine so far.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getSystemLog() throws DIException, RemoteException;

	/**
	 * Retrieves the last chunk from the current AssemblyLine's system log.
	 * 
	 * @param aLastKilobytes
	 *            specifies in kilobytes the size of the log's last chunk that
	 *            will be read.
	 * 
	 * @return the last chunk of the AssemblyLine's log, generated so far.
	 * @throws DIException
	 *             if an error occurs while obtaining AssemblyLine's log.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getSystemLogLastChunk(int aLastKilobytes) throws DIException, RemoteException;

	/**
	 * Returns AssemblyLine GUID. The GUID is a string value that is unique for
	 * each component ever created by a particular TDI Server.
	 * <p>
	 * <b>Example</b>
	 * </p>
	 * 
	 * <pre>
	 * var runningALs = null;
	 * var stats = null;
	 * var result = null;
	 * var sessionFactory = java.rmi.Naming.lookup(&quot;rmi://127.0.0.1:1099/SessionFactory&quot;);
	 * var remoteSession = sessionFactory.createSession();
	 * var runningConfigs = remoteSession.getConfigInstances();
	 * if (runningConfigs != null) {
	 * 	for (var i = 0; i &lt; runningConfigs.length; i++) {
	 * 		runningALs = runningConfigs[i].getAssemblyLines();
	 * 
	 * 		for (var j = 0; j &lt; runningALs.length; j++) {
	 * 			if (runningALs[j].isActive()) {
	 * 				stats = runningALs[j].getStatistics().toString();
	 * 				main.logmsg(&quot;statistics[&quot; + j + &quot;] = &quot; + stats);
	 * 
	 * 				result = runningALs[j].getResult();
	 * 
	 * 				var attr = new com.ibm.di.entry.Attribute(&quot;LogURL&quot;, &quot;/log/&quot; + runningALs[j].getGlobalUniqueID());
	 * 
	 * 				runningALs[j].stop();
	 * 			}
	 * 		}
	 * 	}
	 * } else {
	 * 	main.logmsg(&quot;No remote config instances available&quot;);
	 * }
	 * </pre>
	 * 
	 * @return The AssemblyLine GUID value.
	 * @throws DIException
	 *             if an error occurs while obtaining the AssemblyLine GUID
	 *             value.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public String getGlobalUniqueID() throws DIException, RemoteException;

	/**
	 * Check weather the AssemblyLine is simulating or not
	 * 
	 * @return true if the AssemblyLine is simulating, false if it is not.
	 * 
	 * @throws DIException
	 *             if an error occurs while obtaining the simulation state
	 * @throws RemoteException
	 *             if connection error occurs.
	 * 
	 */
	public boolean isSimulating() throws DIException, RemoteException;

	/**
	 * Changes the way the AssemblyLine treats the target systems it is
	 * connectig/interacting to/with. Turning the simulation on will make the
	 * AssemblyLine use the SimulationConfig child of the AssemblyLineConfig in
	 * order to properly handle sensitive data.
	 * 
	 * @param simulate
	 *            true switches the simulation on, false switches it off
	 * 
	 * @throws DIException
	 *             if an error occurs while changing the simulation state
	 * @throws RemoteException
	 *             if connection error occurs.
	 */
	public void setSimulating(boolean simulate) throws DIException, RemoteException;

	/**
	 * Register a listener for AssemblyLine events. You must be admin to execute
	 * this method. Beware that there is no automatic removal of orphaned
	 * listeners.
	 * 
	 * @param listener
	 *            Listener for AssemblyLine events.
	 * @param getLogs
	 *            If true, the listener will receive logged messages.
	 * @param getEntryOnEachCycle
	 *            If true the listener will receive the current entry at each
	 *            AssemblyLine cycle's end.
	 * @throws DIException
	 *             if the listener cannot be registered.
	 * @throws RemoteException
	 *             if connection error occurs.
	 * @see #removeListener(AssemblyLineListener)
	 * @since 7.0
	 */
	public void addListener(AssemblyLineListener listener, boolean getLogs, boolean getEntryOnEachCycle) throws DIException,
			RemoteException;

	/**
	 * <p>
	 * Unregister a listener for AssemblyLine events. You must be admin to
	 * execute this method. Listeners are associated with the API object, not
	 * with the AssemblyLine in the Server, so you can remove only listeners
	 * added with the {@link #addListener(AssemblyLineListener, boolean)} method
	 * on the same object.
	 * </p>
	 * <p>
	 * Beware that the listener may get notified one more time after it was
	 * unregistered. The only way to ensure this does not happen is to
	 * unregister the listener in one of its callback methods.
	 * </p>
	 * 
	 * @param listener
	 *            The listener that needs to be removed.
	 * @throws DIException
	 *             if the listener cannot be unregistered.
	 * @throws RemoteException
	 *             if connection error occurs.
	 * @see #addListener(AssemblyLineListener, boolean)
	 * @since 7.0
	 */
	public void removeListener(AssemblyLineListener listener) throws DIException, RemoteException;

	/**
	 * Attach a debugger to the AssemblyLine. You must be admin to execute this
	 * method.
	 * 
	 * @param port
	 *            Port of the debugger.
	 * @param host
	 *            Host of the debugger.
	 * @param onerror
	 *            If true breakpoints are disabled except when there is an
	 *            error.
	 * @throws DIException
	 *             If the AssemblyLine is already being debugged or the
	 *             AssemblyLine cannot connect to the debugger.
	 * @throws RemoteException
	 *             If connection error occurs.
	 * @since 7.0
	 */
	public void attachDebugger(int port, String host, boolean onerror) throws DIException, RemoteException;

	/**
	 * Detach the current debugger from the AssemblyLine. You must be admin to
	 * execute this method.
	 * 
	 * @param msg
	 *            This object will be sent to the debugger before the detaching
	 *            occurs. Must be serializable. If null, nothing will be sent.
	 * @throws DIException
	 *             If an error occurs while detaching the debugger.
	 * @throws RemoteException
	 *             If connection error occurs.
	 * @since 7.0
	 */
	public void detachDebugger(Object msg) throws DIException, RemoteException;

	/**
	 * Query the debug mode setting of the specified component (Connector or
	 * Function Component). You must be admin to execute this method. A
	 * component will log debug level messages only if it is in debug mode. You
	 * can find out the names of the AssemblyLine components from the
	 * configuration - {@link #getConfig()}.
	 * 
	 * @param componentName
	 *            The name of a Connector or a Function Component from this
	 *            AssemblyLine. Must be spelled exactly as it appears in the
	 *            configuration.
	 * @return The debug mode of the component.
	 * @throws DIException
	 *             If an error occurs while querying the debug mode.
	 * @throws RemoteException
	 *             If connection error occurs.
	 * @since 7.0
	 */
	public boolean getComponentDebugMode(String componentName) throws DIException, RemoteException;

	/**
	 * Modify the debug mode setting of the specified component (Connector or
	 * Function Component). You must be admin to execute this method.
	 * Modifications are done on the fly and are not persisted in the
	 * configuration - {@link #getConfig()}. If you run the same AssemblyLine
	 * again it will use the settings from the configuration.
	 * 
	 * @param componentName
	 *            The name of a Connector or a Function Component from this
	 *            AssemblyLine.
	 * @param debug
	 *            The new debug mode of the component.
	 * @throws DIException
	 *             If an error occurs while setting the debug mode.
	 * @throws RemoteException
	 *             If connection error occurs.
	 * @see #getComponentDebugMode(String)
	 * @since 7.0
	 */
	public void setComponentDebugMode(String componentName, boolean debug) throws DIException, RemoteException;

}
