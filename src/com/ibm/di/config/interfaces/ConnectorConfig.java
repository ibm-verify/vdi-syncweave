/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a TDI Connector, e.g. used in an AssemblyLine.
 *
 */
public interface ConnectorConfig extends BaseConfiguration, OperationsConfig {

	/**
	 * The map name of the input schema
	 */
	public final static String SCHEMA_INPUT = "Input";

	/**
	 * The map name of the output schema
	 */
	public final static String SCHEMA_OUTPUT = "Output";

	// The following constants are used with the setMode(String) method
	// of the connector config.

	/**
	 * Constant for Iterator mode. Used with the {@link #setMode(String)}
	 * method.
	 */
	public final static String ITERATOR_MODE = "Iterator";

	/**
	 * Constant for AddOnly mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String ADDONLY_MODE = "AddOnly";

	/**
	 * Constant for Delete mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String DELETE_MODE = "Delete";

	/**
	 * Constant for Lookup mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String LOOKUP_MODE = "Lookup";

	/**
	 * Constant for Update mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String UPDATE_MODE = "Update";

	/**
	 * Constant for Script mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String SCRIPT_MODE = "Script";

	/**
	 * Constant for Call Reply mode. Used with the {@link #setMode(String)}
	 * method.
	 */
	public final static String CALL_REPLY_MODE = "CallReply";

	/**
	 * Constant for Server mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String SERVER_MODE = "Server";

	/**
	 * Constant for Reply mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String REPLY_MODE = "ReplyChannel";

	/**
	 * Constant for Branch mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String BRANCH_MODE = "Branch";

	/**
	 * Constant for Delta mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String DELTA_MODE = "Delta";

	/**
	 * Constant for Function mode. Used with the {@link #setMode(String)}
	 * method.
	 */
	public final static String FUNCTION_MODE = "Function";

	/**
	 * Constant for Mapping mode. Used with the {@link #setMode(String)} method.
	 */
	public final static String MAPPING_MODE = "Mapping";

	/**
	 * Input map name
	 */
	public final static String INPUT_MAP_NAME = "Input";

	/**
	 * Output map name
	 */
	public final static String OUTPUT_MAP_NAME = "Output";

	/**
	 * Connector state - {@value ConnectorConfig#ENABLED_STATE}
	 */
	public final static String ENABLED_STATE = "Enabled";

	/**
	 * Connector state - {@value ConnectorConfig#PASSIVE_STATE}
	 */
	public final static String PASSIVE_STATE = "Passive";

	/**
	 * Connector state - {@value ConnectorConfig#DISABLED_STATE}
	 */
	public final static String DISABLED_STATE = "Disabled";

	/**
	 * Delta behavior - {@value #DELTA_NORMAL}
	 */
	public final static int DELTA_NORMAL = 0;

	/**
	 * Delta behavior - {@value #DELTA_NO_DELETE}
	 */
	public final static int DELTA_NO_DELETE = 1;

	/**
	 * Server option - {@value #SERVER_OPTION_ENTRY}
	 */
	public final static String SERVER_OPTION_ENTRY = "entry";

	/**
	 * Server option - {@value #SERVER_OPTION_CONNECTOR}
	 */
	public final static String SERVER_OPTION_CONNECTOR = "connector";

	/**
	 * Component initialization option - {@value #COMP_INIT_DEFAULT}
	 */
	public final static int COMP_INIT_DEFAULT = 0;

	/**
	 * Component initialization option - {@value #COMP_INIT_USE}
	 */
	public final static int COMP_INIT_USE = 1;

	/**
	 * Component initialization option - {@value #COMP_INIT_MODIFIED}
	 */
	public final static int COMP_INIT_MODIFIED = 2;

	/**
	 * Component initialization option -
	 */
	public final static int COMP_INIT_EVERYTIME = 3;

	/**
	 * @return Connector mode; if it is not set return default value of
	 *         {@value ConnectorConfig#ADDONLY_MODE}
	 */
	public String getMode();

	/**
	 * Sets the mode attribute of the ConnectorConfig object
	 * 
	 * @param mode
	 *            The new mode value
	 */
	public void setMode(String mode);

	/**
	 * @param name
	 *            name of the schema
	 * @return a named Schema config
	 */
	public SchemaConfig getSchema(String name);

	/**
	 * @param input
	 *            input if <code>true</code> return Input schema else Output
	 *            schema
	 * @return input or output schema config
	 */
	public SchemaConfig getSchema(boolean input);

	/**
	 * Script connectors
	 * 
	 * @return The connectorScript value
	 */
	public String getConnectorScript();

	/**
	 * Sets the connectorScript attribute of the ConnectorConfig object
	 * 
	 * @param script
	 *            The new connectorScript value
	 */
	public void setConnectorScript(String script);

	/**
	 * Attribute Maps
	 * 
	 * @return The attributeMap value
	 */
	public AttributeMapConfig getAttributeMap();

	/**
	 * Sets the attributeMap attribute of the ConnectorConfig object
	 * 
	 * @param attributeMap
	 *            The new attributeMap value
	 */
	public void setAttributeMap(AttributeMapConfig attributeMap);

	/**
	 * Gets the attributeMap attribute of the ConnectorConfig object
	 * 
	 * @param input
	 *            a boolean value specifying whether the input or output
	 *            Attribute map to retrieve
	 * 
	 * @return The attributeMap value
	 */
	public AttributeMapConfig getAttributeMap(boolean input);

	/**
	 * Gets the attributeMap attribute of the ConnectorConfig object
	 * 
	 * @param name
	 *            the name of the Attribute map to retrieve
	 * 
	 * @return The attributeMap value
	 */
	public AttributeMapConfig getAttributeMap(Object name);

	/**
	 * Sets the attributeMap attribute of the ConnectorConfig object
	 * 
	 * @param attributeMap
	 *            The new attributeMap value
	 * @param input
	 *            The new attributeMap value
	 */
	public void setAttributeMap(AttributeMapConfig attributeMap, boolean input);

	/**
	 * Link Criteria
	 * 
	 * @return The linkCriteria value
	 */
	public LinkCriteriaConfig getLinkCriteria();

	/**
	 * Sets the linkCriteria attribute of the ConnectorConfig object
	 * 
	 * @param linkCriteria
	 *            The new linkCriteria value
	 */
	public void setLinkCriteria(LinkCriteriaConfig linkCriteria);

	/**
	 * Hooks
	 * 
	 * @return The hooks value
	 */
	public HooksConfig getHooks();

	/**
	 * Connection parameters
	 * 
	 * @return The connectionConfig value
	 */
	public RawConnectorConfig getConnectionConfig();

	/**
	 * Associated Parser configuration
	 * 
	 * @return The parserConfig value
	 */
	public ParserConfig getParserConfig();

	/**
	 * Sets the parserConfig attribute of the ConnectorConfig object
	 * 
	 * @param parser
	 *            The new parserConfig value
	 */
	public void setParserConfig(ParserConfig parser);

	/**
	 * State (typically used in the AssemblyLine)
	 * 
	 * @return The configured state
	 */
	public String getState();

	/**
	 * Sets the state attribute of the ConnectorConfig object
	 * 
	 * @param state
	 *            The new state
	 */
	public void setState(String state);

	/**
	 * Enabled (typically used in the AssemblyLine)
	 * 
	 * @return The enabled value
	 */
	public boolean getEnabled();

	/**
	 * Sets the enabled attribute of the ConnectorConfig object
	 * 
	 * @param enabled
	 *            The new enabled value
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Returns the compute-changes flag for Update mode connector.
	 * 
	 * @return The enabled value
	 */
	public boolean getComputeChanges();

	/**
	 * Sets the compute-changes flag for Update mode connector.
	 * 
	 * @param cc
	 *            The new enabled value
	 */
	public void setComputeChanges(boolean cc);

	/**
	 * Sets the Skip Lookup option to the Connector.
	 * 
	 * @param skipLookup
	 *            The new value of the Skip Lookup option.
	 */
	public void setSkipLookup(boolean skipLookup);

	/**
	 * Gets the Skip Lookup option from the Connector.
	 * 
	 * @return true if Skip Lookup is enabled.
	 */
	public boolean getSkipLookup();

	/**
	 * Checks whether the Connector supports Skip Lookup.
	 * 
	 * @return true if the Connector supports Skip Lookup.
	 */
	public boolean supportsSkipLookup();

	/**
	 * @return the delta configuration
	 */
	public DeltaConfig getDeltaConfig();

	/**
	 * @return the Connector Pool definition configuration
	 */
	public PoolDefConfig getPoolDefConfig();

	/**
	 * @return the Connector Pool instance configuration
	 */
	public PoolInstanceConfig getPoolInstanceConfig();

	/**
	 * @return the checkpoint configuration.
	 * @deprecated Checkpoint/restart is deprecated.
	 */
	public CheckpointConfig getCheckpointConfig();

	/**
	 * @return the checkpoint configuration.
	 */
	public SandboxConfig getSandboxConfig();

	/**
	 * @return true if the connector uses delta behavior
	 */
	public int getDeltaBehavior();

	/**
	 * Sets delta behavior.
	 * 
	 * @param behavior
	 */
	public void setDeltaBehavior(int behavior);

	/**
	 * @return the delta strict flag
	 */
	public boolean getDeltaStrict();

	/**
	 * Sets the delta strict flag.
	 * 
	 * @param strict
	 */
	public void setDeltaStrict(boolean strict);

	/**
	 * @return the server option (entry or connector) that determines whether
	 *         the connector returns a connector instance or an entry in server
	 *         mode.
	 */
	public String getServerOption();

	/**
	 * @return true if the connector requires a response in server or iterator
	 *         mode.
	 */
	public boolean getReplyRequired();

	/**
	 * @return true if the connector is an entry feed connector
	 */
	public boolean isEntryFeed();

	/**
	 * @return the name of the operation carrier
	 */
	public String getOperationCarrier();

	/**
	 * This method sets the name of the operation carrier
	 * 
	 * @param name
	 */
	public void setOperationCarrier(String name);

	/**
	 * @return whether the operation carrier is an entry attribute or entry
	 *         property
	 */
	public boolean getOperationCarrierIsProperty();

	/**
	 * This method sets whether the operation carrier is an entry attribute or
	 * entry property.
	 * 
	 * @param isproperty
	 */
	public void setOperationCarrierIsProperty(boolean isproperty);

	/**
	 * @return the reconnect config
	 */
	public ReconnectConfig getReconnectConfig();

	/**
	 * @return the initialization option for the component
	 */
	public int getInitializeOption();

	/**
	 * This method sets the the initialization option for the component
	 * 
	 * @param option
	 */
	public void setInitializeOption(int option);
		
	/**
	 * Sets the Read Limit or Loop Limit for the Connector
	 * 
	 * @param option A string representing the number.
	 * @since 7.1.1
	 */
	public void setLimitOption(String option);
	
	/**
	 * Returns the Read Limit or Loop Limit for the Connector
	 * @since 7.1.1
	 */
	public String getLimitOption();
	
	/**
	 * Returns the supported modes for this Connector.
	 * 
	 * @return the supported modes, as a comma separated String
	 * @since 7.2
	 */
	public String getSupportedModes();
	
	/**
	 * Sets the supported modes for this Connector
	 * @param modes Supported modes as a comma separated String.
	 * @since 7.2
	 */
	public void setSupportedModes(String modes);
}
