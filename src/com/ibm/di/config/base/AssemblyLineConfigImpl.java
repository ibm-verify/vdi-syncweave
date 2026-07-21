/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.CallConfig;
import com.ibm.di.config.interfaces.CheckpointConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.config.interfaces.SandboxConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SimulationConfig;

/**
 * The implementation class for the configuration of an AssemblyLine.
 */
@SuppressWarnings("deprecation")
public class AssemblyLineConfigImpl extends BaseConfigurationImpl implements
		AssemblyLineConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 2715909691453046036L;

	/**
	 * Entry feed container config.
	 */
	private ContainerConfig entryFeed;

	/**
	 * Data Flow container config.
	 */
	private ContainerConfig dataFlow;

	/**
	 * Operations container config.
	 */
	private ContainerConfig operations;

	/**
	 * List of connectors.
	 */
	private Vector<String> connectorList;

	/**
	 * Settings config.
	 */
	private BaseConfiguration settings;

	/**
	 * Checkpoint config.
	 */
	private CheckpointConfig checkpoint;

	/**
	 * Sandbox config.
	 */
	private SandboxConfig sandbox;

	/**
	 * Log config.
	 */
	private LogConfig logger;

	/**
	 * Thread options config.
	 */
	private PropertyConfig threadOptions;

	/**
	 * Hooks config.
	 */
	private HooksConfig hooks;

	/**
	 * Input attribute map config.
	 */
	private AttributeMapConfig inputAttributeMap;

	/**
	 * Output attribute map config.
	 */
	private AttributeMapConfig outputAttributeMap;

	/**
	 * Input schema config.
	 */
	private SchemaConfig inputSchema;

	/**
	 * Output schema config.
	 */
	private SchemaConfig outputSchema;

	/**
	 * Initial parameters.
	 */
	private SchemaConfig initParams;

	/**
	 * Simulation config.
	 */
	private SimulationConfig simulation = null;

	/**
	 * Constructor for the AssemblyLineConfigImpl object.
	 */
	public AssemblyLineConfigImpl() {
		super();
	}

	/**
	 * Constructor providing a TreeMap of attribute/value pairs.
	 *
	 * @param config
	 *            the config object containing initial data
	 */
	public AssemblyLineConfigImpl(Object config) {
		super(config);
	}

	/**
	 * Preload all connectors
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() throws Exception {

		// EntryFeed Components - no need to load from internal schema since
		// this feature only exist in the XML driver version
		if (entryFeed == null) {
			entryFeed = new ContainerConfigImpl(getParameter(
					InternalSchema.AL_EH + "EF", new TreeMap<String, Object>()));
			entryFeed.setName("EntryFeedContainer");
			entryFeed.setParent(this);
			entryFeed.init();
			setChild("EntryFeed", entryFeed);
		}

		// DataFlow Components - no need to load from internal schema since this
		// feature only exist in the XML driver version
		if (dataFlow == null) {
			dataFlow = new ContainerConfigImpl(getParameter(
					InternalSchema.AL_EH + "DF", new TreeMap<String, Object>()));
			dataFlow.setName("DataFlowContainer");
			dataFlow.setParent(this);
			dataFlow.init();
			setChild("DataFlow", dataFlow);
		}

		// Operations - no need to load from internal schema since this feature
		// only exist in the XML driver version
		if (operations == null) {
			operations = new ContainerConfigImpl(getParameter(
					InternalSchema.AL_OPERATIONS, new TreeMap<String, Object>()));
			operations.setName("Operations");
			operations.setParent(this);
			operations.init();
			setChild("Operations", operations);
		}

		// initParams
		if (initParams == null) {
			initParams = new SchemaConfigImpl(getParameter(
					InternalSchema.AL_INIT_PARAMS, new TreeMap<String, Object>()));
			initParams.setName("AssemblyLineInitParams");
			initParams.setParent(this);
			initParams.init();
			setChild("AssemblyLineInitParams", initParams);
		}

		// Old style connector list
		if (connectorList == null) {
			connectorList = (Vector) getParameter(
					InternalSchema.AL_CONNECTOR_LIST, new Vector<String>());
			// connectors = new TreeMap();
			TreeMap<String,Object> tmp = (TreeMap) getParameter(
					InternalSchema.AL_COMPONENT_LIST, new TreeMap<String,Object>());
			for (String name:connectorList) {
				TreeMap<String,Object> cfg = (TreeMap) tmp.get(name);

				if (cfg == null) {
					throw new Exception(getResHash().getString(
						"MMCONFIG.ASSEMBLYLINECONFIGIMPL.COMPONENT.CONFIG.NOT.FOUND",
						new Object[] { name, getName() }));
				}

				ConnectorConfig cc;
				if (cfg.get(InternalSchema.FUNCTION_CONFIG) != null) {
					cc = new FunctionConfigImpl(cfg);
				} else {
					cc = new ConnectorConfigImpl(cfg);
				}
				try {
					cc.setName(MetamergeConfigFactory.parseName(name));
				} catch (InvalidNameException ine) {
					System.out.println(getResHash().getString(
							"MMCONFIG.ASSEMBLYLINECONFIGIMPL.PARSE.NAME.ERROR",
							new Object[] { name, ine }));
				}
				cc.setParent(this);
				cc.init();
				cc.setupInheritanceChain();
				// System.out.println("Migrating connector: " + cc.getName());
				addComponent(cc);
				// connectors.put ( name, cc );
			}
		}

		// EH Thread options
		if (threadOptions == null) {
			threadOptions = new PropertyConfigImpl(getParameter(
					InternalSchema.AL_THREADOPTIONS, new TreeMap<String,Object>()));
			threadOptions.setParent(this);
			threadOptions.init();
			threadOptions.setName("ThreadOptions");
			setChild(threadOptions.getShortName(), threadOptions);
		}

		// Attribute maps
		if (inputAttributeMap == null) {
			inputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN, new TreeMap<String,Object>()));
		}
		inputAttributeMap.setParent(this);
		inputAttributeMap.init();
		inputAttributeMap.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.INPUT_MAP_NAME));
		setChild("InputAttributeMap", inputAttributeMap);

		if (outputAttributeMap == null) {
			outputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT, new TreeMap<String,Object>()));
		}
		outputAttributeMap.setParent(this);
		outputAttributeMap.init();
		outputAttributeMap.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.OUTPUT_MAP_NAME));
		setChild("OutputAttributeMap", outputAttributeMap);

		// Input Schema
		if (inputSchema == null) {
			inputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.SCHEMA_INPUT, new TreeMap<String,Object>()));
		}
		inputSchema.setParent(this);
		inputSchema.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.INPUT_MAP_NAME));
		inputSchema.init();
		setChild("InputSchema", inputSchema);

		// Output Schema
		if (outputSchema == null) {
			outputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.SCHEMA_OUTPUT, new TreeMap<String,Object>()));
		}
		outputSchema.setParent(this);
		outputSchema.setName(MetamergeConfigFactory
				.parseName(AssemblyLineConfig.OUTPUT_MAP_NAME));
		outputSchema.init();
		setChild("OutputSchema", outputSchema);

		// Hooks
		if (hooks == null)
			hooks = new HooksConfigImpl(getParameter(
					InternalSchema.CONNECTOR_HOOKS, new TreeMap<String,Object>()));
		hooks.setParent(this);
		hooks.init();
		setChild("Hooks", hooks);
		migrateHooks();

		// Simulation
		if (simulation == null) {
			initSimulationConfig();
		}
	}

	/**
	 * Migrates all assembly line hooks.
	 */
	private void migrateHooks() {
		migrateHook(InternalSchema.AL_PROLOG);
		migrateHook(InternalSchema.AL_PROLOG_INIT);
		migrateHook(InternalSchema.AL_EPILOG);
		migrateHook(InternalSchema.AL_EPILOG2);
		migrateHook(InternalSchema.AL_SHUTDOWN);
		migrateHook(InternalSchema.AL_STARTCYCLE);
		migrateHook(InternalSchema.AL_ONSUCCESS);
		migrateHook(InternalSchema.AL_ONFAILURE);
	}

	/**
	 * Migrate a hook.
	 *
	 * @param name
	 *            name of the hook
	 */
	private void migrateHook(String name) {
		String old = getStringParameter(name);

		if (old != null && old.length() > 0) {
			HookConfig h = hooks.getHook(name);
			h.setScript(old);
			h.setEnabled(true);
			removeParameter(name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getConnectorCount() {
		int count = 0;
		List<BaseConfiguration> list = getEntryFeedComponents().getConfigurations(null);
		getDataFlowComponents().getConfigurations(list);
		for (BaseConfiguration bc:list) {
			if (bc instanceof ConnectorConfig)
				count++;
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorConfig getConnector(int position) {
		int count = 0;
		ConnectorConfig cc = null;
		List<BaseConfiguration> list = getEntryFeedComponents().getConfigurations(null);
		getDataFlowComponents().getConfigurations(list);
		for (BaseConfiguration bc:list) {
			if (bc instanceof ConnectorConfig) {
				cc = (ConnectorConfig) bc;
				if (count == position)
					break;
				else
					count++;
			}
		}
		return cc;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorConfig getConnectorByName(Object connectorName) {
		if (connectorName == null) {
			return null;
		} else {
			return (ConnectorConfig) getComponent(connectorName.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public HookConfig getHook(String name) {
		return hooks.getHook(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public HooksConfig getHooks() {
		return hooks;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getSettings() {
		if (settings == null) {
			settings = new BaseConfigurationImpl(getParameter(
					InternalSchema.AL_SETTINGS, new TreeMap<String,Object>()));
		}
		settings.setParent(this);
		return settings;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSettings(BaseConfiguration settings) {
		this.settings = settings;
		setParameter(InternalSchema.AL_SETTINGS, settings.getData());
	}

	/**
	 * Call Parameters
	 *
	 * @return The taskInputParameters value
	 * @deprecated Use getSchema(true)
	 */
	@Deprecated
	public CallConfig getTaskInputParameters() {
		CallConfig cc = new CallConfigImpl(getSchema(true).getData());
		cc.setParent(this);
		return cc;
	}

	/**
	 * Output Parameters
	 *
	 * @return The taskOutputParameters value
	 * @deprecated Use getSchema(false)
	 */
	@Deprecated
	public CallConfig getTaskOutputParameters() {
		CallConfig cc = new CallConfigImpl(getSchema(false).getData());
		cc.setParent(this);
		return cc;
	}

	/**
	 * {@inheritDoc}
	 * @deprecated
	 */
	public CheckpointConfig getCheckpointConfig() {
		if (checkpoint != null) {
			return checkpoint;
		}

		checkpoint = new CheckpointConfigImpl(getParameter(
				InternalSchema.AL_CHECKPOINT, new TreeMap<String,Object>()));

		checkpoint.setParent(this);
		return checkpoint;
	}

	/**
	 * {@inheritDoc}
	 */
	public SandboxConfig getSandboxConfig() {
		if (sandbox != null) {
			return sandbox;
		}

		sandbox = new SandboxConfigImpl(getParameter(
				InternalSchema.CONNECTOR_SANDBOX_CONFIG, new TreeMap<String,Object>()));
		sandbox.setParent(this);
		return sandbox;
	}

	/**
	 * {@inheritDoc}
	 */
	public LogConfig getLogConfig() {
		if (logger != null) {
			return logger;
		}

		logger = new LogConfigImpl(getParameter(InternalSchema.LOG_CONFIG,
				new TreeMap<String,Object>()));
		logger.setParent(this);
		return logger;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDebug() {
		return getDebug(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDebug(boolean defval) {
		return getSettings().getBooleanParameter(InternalSchema.DEBUG, defval);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDebug(boolean debug) {
		getSettings().setBooleanParameter(InternalSchema.DEBUG, debug);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean autoMapAllAttributes(Object connectorName) throws Exception {
		BaseConfiguration bc = getComponent(connectorName.toString());
		if (!(bc instanceof ConnectorConfig))
			return false;

		ConnectorConfig cc = (ConnectorConfig) bc;

		List<String> am = cc.getAttributeMap(true).getAttributeNames();

		if (am.size() == 0
				&& getSettings().getBooleanParameter(
						InternalSchema.AL_AUTOMAP_ATTRIBUTES, false))
			return true;

		if (am.size() == 1 && "*".equals(am.get(0)))
			return true;

		return false;
	}

	/**
	 * Return self clone
	 *
	 * @return The clone value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Object getClone() throws Exception {
		AssemblyLineConfig alc = new AssemblyLineConfigImpl(deepClone(null));
		alc.setName(getName());
		alc.init();
		alc.setMetamergeConfig(getMetamergeConfig());
		alc.setupInheritanceChain();
		alc.setModTS(getModTS());
		return alc;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyConfig getThreadOptions() {
		return threadOptions;
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getSchema(boolean input) {
		if (input) {
			return inputSchema;
		} else {
			return outputSchema;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getSchema(String name) {
		return getSchema(name.equals(AssemblyLineConfig.INPUT_MAP_NAME));
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap(boolean input) {
		if (input) {
			return inputAttributeMap;
		} else {
			return outputAttributeMap;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap(String name) {
		return getAttributeMap(name.equals(AssemblyLineConfig.INPUT_MAP_NAME));
	}

	/**
	 * @return <code>null</code>
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public ContainerConfig getComponents() {
		return null;
	}

	/**
	 * @return <code>0</code>
	 * @deprecated
	 */
	@Deprecated
	public int getComponentCount() {
		return 0;
	}

	/**
	 * @param name
	 *
	 * @return <code>null</code>
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public BaseConfiguration getComponentByName(String name) {
		return null;
	}

	/**
	 * @param position
	 *
	 * @return <code>null</code>
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public BaseConfiguration getComponent(int position) {
		return null;
	}

	/**
	 * Does nothing.
	 *
	 * @param position
	 *
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public void removeComponent(int position) {
	}

	/**
	 * Does nothing.
	 *
	 * @param connector
	 * @param position
	 *
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public void insertComponent(BaseConfiguration connector, int position) {
	}

	/**
	 * @param position
	 * @param up
	 *
	 * @return false
	 * @deprecated in TDI 7.0
	 */
	@Deprecated
	public boolean moveComponent(int position, boolean up) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void renameComponent(String oldName, String newName)
			throws Exception {
		BaseConfiguration bc = getComponent(oldName);
		if (bc == null) {
			return;
		}

		if (getComponent(newName) != null) {
			throw new javax.naming.NameAlreadyBoundException(newName);
		}

		bc.setName(MetamergeConfigFactory.parseName(newName));
		notifyChange(this, newName, MetamergeConfigChange.MCC_SET, bc);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig getEntryFeedComponents() {
		return entryFeed;
	}

	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig getDataFlowComponents() {
		return dataFlow;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsComponent(String name) {
		if (getDataFlowComponents().containsConfig(name, true)) {
			return true;
		} else {
			return getEntryFeedComponents().containsConfig(name, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getComponent(String name) {
		BaseConfiguration bc = getEntryFeedComponents().getConfig(name, true);
		if (bc == null) {
			bc = getDataFlowComponents().getConfig(name, true);
		}

		return bc;
	}

	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig addComponent(BaseConfiguration config) {
		ContainerConfig cc = null;
		if (config instanceof ConnectorConfig
				&& ((ConnectorConfig) config).isEntryFeed()) {
			cc = getEntryFeedComponents();
		} else {
			cc = getDataFlowComponents();
		}

		if (cc != null) {
			cc.addConfig(config);
		}

		return cc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeComponent(String name) {
		BaseConfiguration c;
		if ((c = getDataFlowComponents().getConfig(name, true)) != null) {
			getDataFlowComponents().removeConfig(c.getShortName(), true);
		} else if ((c = getEntryFeedComponents().getConfig(name, true)) != null) {
			getEntryFeedComponents().removeConfig(c.getShortName(), true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeComponent(BaseConfiguration component) {
		removeComponent(component.getShortName());
	}

	/**
	 * This method returns a list of supported operations
	 *
	 * @return {@link ContainerConfig} object
	 */
	public ContainerConfig getOperations() {
		return operations;
	}

	/**
	 * This method returns the config for a given operation.
	 *
	 * @param name
	 * @return {@link OperationConfig} object
	 */
	public OperationConfig getOperation(String name) {
		return (OperationConfig) getOperations().getConfig(name);
	}

	/**
	 * This method creates a new operation object.
	 *
	 * @param name
	 * @return {@link OperationConfig} object
	 * @throws Exception
	 */
	public OperationConfig createOperation(String name) throws Exception {
		if (getOperations().containsConfig(name, true))
			throw new NameAlreadyBoundException(name);

		OperationConfig bc = new OperationConfigImpl();
		bc.setName(name);
		bc.setParent(this);
		bc.init();
		getOperations().addConfig(bc);
		return getOperation(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getPublishedInitParams() {
		return initParams;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPublishedInitParams(SchemaConfig schema) throws Exception {
		initParams = schema;
		initParams.setName("AssemblyLineInitParams");
		initParams.setParent(this);
		initParams.init();
		setParameter(InternalSchema.AL_INIT_PARAMS, initParams, false);
		setChild("AssemblyLineInitParams", initParams);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();
		for (String childName:getChildNames())
			getChild(childName).setupInheritanceChain();
	}

	/**
	 * {@inheritDoc}
	 */
	public SimulationConfig getSimulationConfig() throws Exception {
		if (simulation != null) {
			return simulation;
		}

		initSimulationConfig();

		return simulation;
	}

	/**
	 * Initializes the Simulation config.
	 *
	 * @throws Exception
	 *             if the initialization fails
	 */
	public void initSimulationConfig() throws Exception {
		simulation = new SimulationConfigImpl(getParameter(
				InternalSchema.AL_SIMULATE_CONFIG, new TreeMap<String,Object>()));
		simulation.setParent(this);
		simulation.init();
		setChild("Simulation", simulation);
	}
}
