/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for an AssemblyLine.
 * 
 * @author bstadheim created 21. May 2002
 */

public interface AssemblyLineConfig extends BaseConfiguration, OperationsConfig {

	/**
	 * The name of the input map
	 */
	public final static String INPUT_MAP_NAME = "Input";

	/**
	 * The name of the output map
	 */
	public final static String OUTPUT_MAP_NAME = "Output";

	/**
	 * Returns the number of connectors in the assemblyline
	 * 
	 * @return The connectorCount value
	 */
	public int getConnectorCount();

	/**
	 * Returns the connector attribute of the AssemblyLineConfig object
	 * 
	 * @param name
	 *            the name of the Connector for which to return the Connector
	 *            configuration object
	 * 
	 * @return The connector configuration object.
	 * 
	 * @throws Exception
	 *             if error occurs while retrieving the configuration object.
	 */
	public ConnectorConfig getConnectorByName(Object name) throws Exception;

	/**
	 * Returns the connector attribute of the AssemblyLineConfig object
	 * 
	 * @param position
	 *            Connector position
	 * @return The connector configuration object
	 * 
	 * @throws Exception
	 *             if error occurs while retrieving the configuration object.
	 */
	public ConnectorConfig getConnector(int position) throws Exception;

	/**
	 * Returns the number of components in the assemblyline
	 * 
	 * @return The componentCount value
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public int getComponentCount();

	/**
	 * Returns the components container
	 * 
	 * @return The componentCount value
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public ContainerConfig getComponents();

	/**
	 * Returns the component attribute of the AssemblyLineConfig object
	 * 
	 * @param name
	 *            The name of the component to get.
	 * @return The connector configuration object
	 * 
	 * @throws Exception
	 *             never
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public BaseConfiguration getComponentByName(String name) throws Exception;

	/**
	 * Returns the component attribute of the AssemblyLineConfig object
	 * 
	 * @param position
	 *            component position
	 * @return The connector configuration object
	 * @throws Exception
	 *             never
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public BaseConfiguration getComponent(int position) throws Exception;

	/**
	 * Removes a component from the assemblyline
	 * 
	 * @param position
	 *            component position
	 * @throws Exception
	 *             never
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public void removeComponent(int position) throws Exception;

	/**
	 * Removes a component from the assemblyline
	 * 
	 * @param component
	 *            component config
	 * 
	 * @throws Exception
	 *             if error removing the component occurs.
	 */
	public void removeComponent(BaseConfiguration component) throws Exception;

	/**
	 * Adds a connector configuration object to this assemblyline
	 * 
	 * @param connector
	 *            The component configuration object to add
	 * @param position
	 *            The position of the connector or -1 to add the connector to
	 *            the end
	 * @throws Exception
	 *             never
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public void insertComponent(BaseConfiguration connector, int position)
			throws Exception;

	/**
	 * Moves a connector one position up or down
	 * 
	 * @param position
	 *            Current connector position
	 * @param up
	 *            Up (true) or down (false)
	 * @return true if the operation succeeded
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public boolean moveComponent(int position, boolean up);

	/**
	 * Changes the local name of a connector (the name used in script engines
	 * etc )
	 * 
	 * @param name
	 *            The current name
	 * @param newName
	 *            .New local name
	 * @exception Exception
	 *                if the provided name is invalid or there is already a
	 *                component with the specified name.
	 */
	public void renameComponent(String name, String newName) throws Exception;

	/**
	 * Returns the AssemblyLine's HooksConfig
	 * 
	 * @return The hooks of this AssemblyLine
	 */
	public HooksConfig getHooks();

	/**
	 * Returns a specific HookConfig
	 * 
	 * @param name
	 *            The name of the hook
	 * @return The HookConfig with the given name
	 */
	public HookConfig getHook(String name);

	/**
	 * Returns the AssemblyLine Settings
	 * 
	 * @return The settings value
	 */
	public BaseConfiguration getSettings();

	/**
	 * Sets the AssemblyLine Settings of the AssemblyLineConfig
	 * 
	 * @param settings
	 *            The new settings value
	 */
	public void setSettings(BaseConfiguration settings);

	/**
	 * Returns the AssemblyLine's input parameters (ref Task Call Block)
	 * 
	 * @return The taskInputParameters value
	 * @deprecated Use getSchema(true)
	 */
	@Deprecated
	public CallConfig getTaskInputParameters();

	/**
	 * Returns the AssemblyLine's output parameters (ref Task Call Block)
	 * 
	 * @return The taskOutputParameters value
	 * @deprecated Use getSchema(false)
	 */
	@Deprecated
	public CallConfig getTaskOutputParameters();

	/**
	 * Returns the AssemblyLine's schema
	 * 
	 * @param input
	 *            if true, returns the input schema
	 * @return The schema object
	 */
	public SchemaConfig getSchema(boolean input);

	/**
	 * Returns the AssemblyLine's schema by name ("Input"/"Output")
	 * 
	 * @param name
	 *            the name of the schema config to retrieve
	 * @return The schema config object
	 */
	public SchemaConfig getSchema(String name);

	/**
	 * Returns the AssemblyLine's attribute map
	 * 
	 * @param input
	 *            if true, returns the input attribute map
	 * @return The attribute map object
	 */
	public AttributeMapConfig getAttributeMap(boolean input);

	/**
	 * Returns the AssemblyLine's attribute map by name ("Input"/"Output")
	 * 
	 * @param name
	 *            the name of the attribute map config to retrieve
	 * @return The attribute map config object
	 */
	public AttributeMapConfig getAttributeMap(String name);

	/**
	 * Returns the debug flag for the AssemblyLine (default false)
	 * 
	 * @return The debug value
	 */
	public boolean getDebug();

	/**
	 * Returns the debug flag for the AssemblyLine
	 * 
	 * @param defval
	 *            The default value
	 * @return The debug value
	 */
	public boolean getDebug(boolean defval);

	/**
	 * Sets the debug flag for the AssemblyLine
	 * 
	 * @param debug
	 *            The new debug value
	 */
	public void setDebug(boolean debug);

	/**
	 * Returns the automap flag for the AssemblyLine
	 * 
	 * @param connectorName
	 *            The name of an AssemblyLine Component
	 * @return true, if the AssemblyLine Component maps all the attributes
	 * @exception Exception
	 *                if the check does not succeed
	 */
	public boolean autoMapAllAttributes(Object connectorName) throws Exception;

	/**
	 * Returns the Checkpoint info object
	 * 
	 * @return The checkpointConfig value
	 */
	public CheckpointConfig getCheckpointConfig();

	/**
	 * Returns the Sandbox config object
	 * 
	 * @return the Sandbox config object
	 */
	public SandboxConfig getSandboxConfig();

	/**
	 * Returns the LogConfig object
	 * 
	 * @return The logConfig value
	 */
	public LogConfig getLogConfig();

	/**
	 * Returns the Thread options config in the AssemblyLine configuration
	 * 
	 * @return The threadOptions value
	 * 
	 * @throws Exception
	 *             if the options could not be retrieved.
	 */
	public PropertyConfig getThreadOptions() throws Exception;

	/**
	 * Returns the entry feed components container
	 * 
	 * @return The entry feed components value
	 */
	public ContainerConfig getEntryFeedComponents();

	/**
	 * Returns the data flow components container
	 * 
	 * @return The data flow components value
	 */
	public ContainerConfig getDataFlowComponents();

	/**
	 * Returns true if there is component (data flow or entry feed) named
	 * <i>name</i>
	 * 
	 * @param name
	 *            The name of the config
	 * @return True if it exists
	 */
	public boolean containsComponent(String name);

	/**
	 * Returns the component configuration
	 * 
	 * @param name
	 *            The name of the config
	 * @return Null if it does not exist else a BaseConfiguration subclass
	 */
	public BaseConfiguration getComponent(String name);

	/**
	 * This method adds a component configuration object to the correct
	 * component container
	 * 
	 * @param config
	 *            The configuration object
	 * @return The container to which it was added (either DataFlow or EntryFeed
	 *         container) or NULL if the config object was invalid.
	 */
	public ContainerConfig addComponent(BaseConfiguration config);

	/**
	 * This method removes a named component configuration object
	 * 
	 * @param name
	 *            The name of the component to remove.
	 */
	public void removeComponent(String name);

	/**
	 * Retrieves the Simulation Configuration which corresponds to the
	 * AssemblyLine.
	 * 
	 * @return SimulationConfig object which represents the Simulation
	 *         Configuration of the current AssemblyLine.
	 * @throws Exception
	 *             if an error occurs while initializing the simulation config
	 *             for the first time.
	 */
	public SimulationConfig getSimulationConfig() throws Exception;

}
