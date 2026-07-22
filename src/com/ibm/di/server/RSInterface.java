/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// RSInterface.java
//
//
//
package com.ibm.di.server;

import javax.net.ServerSocketFactory;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;

/**
 * The top level Thread is an instance of this class, called the <i>main</i>
 * object. It has methods for manipulating AssemblyLines.
 */
public interface RSInterface {
	/**
	 * Returns the entire table or a sub-section of the configuration file.
	 * 
	 * @param name
	 *            Name of subsection or null
	 * 
	 * @return The entire table (name == null) or section in table
	 */
	public Object getConfiguration(String name);

	/**
	 * Returns the config object for this instance.
	 * 
	 * @return the configuration object of this instance.
	 */
	public MetamergeConfig getMetamergeConfig();

	/**
	 * Sets the given configuration to this instance.
	 * 
	 * @param config
	 *            The configuration object to assign to the instance.
	 */
	public void setMetamergeConfig(MetamergeConfig config);

	/**
	 * 
	 * Returns the "AssemblyLine" entry
	 * 
	 * @param name
	 *            The AssemblyLine name
	 * @return The configuration for the AssemblyLine
	 */
	public AssemblyLineConfig getTask(String name);

	/**
	 * Returns the "Connector" entry for name
	 * 
	 * @param name
	 *            The connector name
	 * @return The section from either the file configuration or the templates
	 *         configuration
	 */
	public ConnectorConfig getConnector(String name);

	/**
	 * Returns the the "Java Library" entry for name
	 * 
	 * @param name
	 *            The java library name
	 * @return The section for name
	 */
	public Object getLibrary(String name);

	/**
	 * Returns all "Libraries"
	 * 
	 * @return The entire list of Libraries
	 */
	public LibraryConfig getLibraries();

	/**
	 * Returns the the "Parser" entry for name
	 * 
	 * @param name
	 *            The parser name
	 * @return The section either the file configuration or the templates
	 *         configuration
	 */
	public ParserConfig getParser(String name);

	/**
	 * Returns the the "Script Library" entry for name
	 * 
	 * @param name
	 *            The script library name
	 * @return The section for name
	 */
	public ScriptConfig getScript(String name);

	/**
	 * Returns the the "Attribute Map" for a named connector
	 * 
	 * @param name
	 *            The connector name
	 * @return The attribute map section
	 */
	public AttributeMapConfig getAttributeMap(String name);

	/**
	 * Returns the "FunctionConfig" entry for name
	 * 
	 * @param name
	 *            The name of the function
	 * @return The function config object
	 * @throws Exception
	 *             if a lookup error occurs.
	 */
	public FunctionConfig getFunction(String name) throws Exception;

	/**
	 * Returns the value of a system property. The system properties include all
	 * Java system properties as well as TDI's own properties.
	 * 
	 * @param name
	 *            The system property name, or <code>null</code> if there is no
	 *            property with that name
	 * @return The value for the property
	 */
	public String getSysProp(String name);

	/**
	 * This method saves the current configuration to disk.
	 * 
	 * @throws Exception
	 *             if an error while persisting the configuration occurs.
	 */
	public void persistConfiguration() throws Exception;

	/**
	 * Writes a message to the system log file.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * main.logmsg(&quot;Conn object: &quot;);
	 * main.dumpEntry(conn);
	 * </pre>
	 * 
	 * @param msg
	 *            The message to be output.
	 */
	public void logmsg(String msg);

	/**
	 * Writes an error message to the system log file.
	 * 
	 * @param msg
	 *            The message to output
	 * @since 7.0
	 */
	public void logerror(String msg);

	/**
	 * This method logs a message with the specified level to the log.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * main.logmsg(&quot;INFO&quot;, &quot;Reading entry...&quot;);
	 * var entry = input.getConnector().getNextEntry();
	 * </pre>
	 * 
	 * @param level
	 *            Level of log. Legal values are FATAL, ERROR, WARN, INFO,
	 *            DEBUG. Unrecognized keyword means DEBUG.
	 * @param msg
	 *            The message
	 */
	public void logmsg(String level, String msg);

	/**
	 * Dumps the class and contents of an object to the log file. If this is an
	 * Entry, use the {@link #dumpEntry(Entry)} method instead.
	 * 
	 * @param o
	 *            The object to dump
	 * 
	 * @see #dumpEntry(Entry)
	 */
	public void dump(Object o);

	/**
	 * Dumps the contents of an Entry to the log file.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ctor = input.getConnector();
	 * 
	 * for (;;) {
	 * 	var entry = ctor.getNextEntry();
	 * 	if (entry != null) {
	 * 		main.logmsg(&quot;Read entry: &quot;);
	 * 		main.dumpEntry(entry);
	 * 	} else
	 * 		break;
	 * }
	 * </pre>
	 * 
	 * @param e
	 *            The Entry object to dump
	 * 
	 * @see com.ibm.di.entry.Entry
	 */
	public void dumpEntry(Entry e);

	/**
	 * Start a named AssemblyLine. See also the introduction to AssemblyLines.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var al = main.startAL(&quot;ALName&quot;);
	 * var tcb = al.getTCB();
	 * 
	 * main.logmsg(&quot;AL run mode: &quot; + tcb.getRunMode());
	 * main.logmsg(&quot;AL operation: &quot; + tcb.getALOperation());
	 * main.logmsg(&quot;AL settings: &quot;);
	 * main.dumpEntry(tcb.getALSettings());
	 * </pre>
	 * 
	 * @param assemblyLine
	 *            The name identifying the AssemblyLine to start
	 * @return The AssemblyLine Thread object
	 * @throws Exception
	 *             if <code>assemblyLine</code> is an unknown AssemblyLine or if
	 *             any of the connectors cannot be initialized
	 */

	public AssemblyLine startAL(String assemblyLine) throws Exception;

	/**
	 * Start a named AssemblyLine providing various objects.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var iwe = new com.ibm.di.entry.Entry();
	 * iwe.setAttribute(&quot;linenumber&quot;, &quot;1&quot;);
	 * iwe.setAttribute(&quot;&quot;, &quot;Some line with text&quot;);
	 * 
	 * var c = new com.ibm.di.connectors.FileConnector();
	 * 
	 * var al = main.startAL(&quot;ALName&quot;, iwe);
	 * var tcb = al.getTCB();
	 * 
	 * main.logmsg(&quot;AL run mode: &quot; + tcb.getRunMode());
	 * main.logmsg(&quot;AL connector 'debug' parameter: &quot; + tcb.getConnectorParameter(&quot;ConnectorName&quot;, &quot;debug&quot;));
	 * main.logmsg(&quot;AL initial work entry: &quot;);
	 * main.dumpEntry(tcb.getInitialWorkEntry());
	 * </pre>
	 * 
	 * @param assemblyLine
	 *            The name identifying the AssemblyLine to start
	 * @param io
	 *            This Object could either be
	 *            <ol>
	 *            <li> an Entry, used as the initial work entry </li> <li> a
	 *            Connector, used as a runtime-provided Connector </li> <li> a
	 *            Vector that could contain Entry, Connector(s), TCB or a Log
	 *            objects, used for configuring the AssemblyLine instance. </li>
	 *            <li> a TCB, that holds some special configuration fields read
	 *            by the AssemblyLine </li>
	 *            </ol>
	 * 
	 * @return The AssemblyLine Thread object
	 * @throws Exception
	 *             if <code>assemblyLine</code> is an unknown AssemblyLine or if
	 *             any of the connectors cannot be initialized
	 */
	public AssemblyLine startAL(String assemblyLine, Object io) throws Exception;

	/**
	 * Start named AssemblyLine providing an initial work entry and a connector
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var iwe = new com.ibm.di.entry.Entry();
	 * iwe.setAttribute(&quot;linenumber&quot;, &quot;1&quot;);
	 * iwe.setAttribute(&quot;line&quot;, &quot;Some line with text&quot;);
	 * 
	 * var c = main.getConnector(&quot;ConnectorName&quot;);
	 * var al = main.startAL(&quot;ALName&quot;, c, iwe);
	 * var tcb = al.getTCB();
	 * 
	 * main.logmsg(&quot;AL run mode: &quot; + tcb.getRunMode());
	 * main.logmsg(&quot;AL initial work entry: &quot;);
	 * main.dumpEntry(tcb.getInitialWorkEntry());
	 * </pre>
	 * 
	 * @param assemblyLine
	 *            The name identifying the AssemblyLine to start
	 * @param connector
	 *            The runtime-provided Connector
	 * @param work
	 *            The initial work entry
	 * @return The AssemblyLine Thread object
	 * @throws Exception
	 *             if <code>assemblyLine</code> is an unknown AssemblyLine or if
	 *             any of the connectors cannot be initialized
	 */

	public AssemblyLine startAL(String assemblyLine, Connector connector, Entry work) throws Exception;

	/**
	 * Restart the AssemblyLine given by the parameter.
	 * 
	 * @param assemblyLine
	 *            The name identifying the AssemblyLine to start
	 * @param checkpointID
	 *            The checkpoint identifier
	 * @return The AssemblyLine Thread object
	 * @throws Exception
	 *             if <code>assemblyLine</code> is an unknown AssemblyLine or if
	 *             any of the connectors cannot be re-initialized
	 */
	@Deprecated
	public AssemblyLine restartAL(String assemblyLine, String checkpointID) throws Exception;

	/**
	 * This method reloads the configuration file.
	 * 
	 * @throws Exception
	 *             if the operation fails.
	 */
	public void reload() throws Exception;

	/**
	 * This method returns the current configuration file path
	 * 
	 * @return The configuration file path as a string
	 */
	public String getConfigPath();

	/**
	 * This method sets the current configuration file path. This will be used
	 * when a persistConfiguration is requested.
	 * 
	 * @param path
	 *            The new configuration path
	 */
	public void setConfigPath(String path);

	/**
	 * Raise the shutdown request flag and set the exit code to 0. This method
	 * requests controlled shutdown of all assembly lines running on the server
	 * at the time of calling.
	 */
	public void shutdownServer();

	/**
	 * Raise the shutdown request flag and specify an exit code.
	 * 
	 * @param aExitCode
	 *            the code to return when the application exits.
	 */
	public void shutdownServer(int aExitCode);

	/**
	 * Return the null behavior string from the System.props
	 * 
	 * @return the null behavior string
	 * 
	 */
	public String getNullBehavior();

	/**
	 * Gets the null behavior value.
	 * 
	 * @return the null behavior value string or null if it have not been set
	 *         yet.
	 */
	public String getNullBehaviorValue();

	/**
	 * Return the null definition string from the System.props
	 * 
	 * @return the null Definition string
	 */
	public String getNullDefinition();

	/**
	 * Gets the null definition value.
	 * 
	 * @return the null definition value string or null if it have not been set
	 *         yet.
	 */
	public String getNullDefinitionValue();

	/**
	 * @return The name of this configuration instance.
	 * @since 7.0
	 */
	public String getName();

	/**
	 * @return The log object of this configuration instance. May be null.
	 * @since 7.0
	 */
	public Log getLog();

	/**
	 * Gets a Server Socket Factory for creating Server Sockets. The boolean
	 * parameter <code>useSSL</code> determines whether a SSL Server Socket
	 * Factory is returned or non-SSL one.<br>
	 * This method returns a new instance each time when it is called. The
	 * reason for this is that the implementation of the caller may be
	 * different. Sometimes SSL Factory may be needed while some other times
	 * NOT.<br>
	 * This method is for internal use only and you should not rely on it for
	 * any other purpose.
	 * 
	 * @param useSSL
	 *            Determines if SSL or non-SSL Server Socket Factory is returns
	 *            for use.
	 * 
	 * @return Server Socket Factory for obtaining Server Sockets.
	 * @since 7.1
	 */
	public ServerSocketFactory getServerSocketFactory(boolean useSSL);
}
