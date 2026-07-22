/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.List;
import java.util.TreeMap;

import javax.naming.NameAlreadyBoundException;

import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements the TDI Connector configuration.
 */
public class ConnectorConfigImpl extends BaseConfigurationImpl implements
		ConnectorConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 4093376456212230000L;

	/**
	 * Input AttributeMap configuration.
	 */
	private AttributeMapConfig inputAttributeMap;

	/**
	 * Output AttributeMap configuration.
	 */
	private AttributeMapConfig outputAttributeMap;

	/**
	 * LinkCriteria configuration.
	 */
	private LinkCriteriaConfig linkCriteria;

	/**
	 * RawConnector configuration.
	 */
	private RawConnectorConfig connectionConfig;

	/**
	 * Input Schema configuration.
	 */
	private SchemaConfig inputSchema;

	/**
	 * Output Schema configuration.
	 */
	private SchemaConfig outputSchema;

	/**
	 * Hooks configuration.
	 */
	private HooksConfig hooks;

	/**
	 * Parser configuration.
	 */
	protected ParserConfig parserConfig;

	/**
	 * Hooks configuration.
	 */
	private DeltaConfig deltaConfig;

	/**
	 * Checkpoint configuration.
	 */
	private CheckpointConfig checkpoint;

	/**
	 * Sandbox configuration.
	 */
	private SandboxConfig sandbox;

	/**
	 * Hooks configuration.
	 */
	private ContainerConfig operations;

	/**
	 * Pool configuration.
	 */
	private PoolDefConfig poolDefConfig;

	/**
	 * PoolInstance configuration.
	 */
	private PoolInstanceConfig poolInstanceConfig;

	/**
	 * Reconnect configuration.
	 */
	private ReconnectConfig reconnectConfig;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * Non-arg constructor
	 */
	public ConnectorConfigImpl() {
		super();
	}

	/**
	 * Constructor with one parameter.
	 * 
	 * @param config
	 */
	public ConnectorConfigImpl(Object config) {
		super(config);
	}

	/**
	 * Add initial values for all configuration types.
	 */
	private void updateChildNames() {
		setChild("InputMap", inputAttributeMap);
		setChild("OutputMap", outputAttributeMap);
		setChild("LinkCriteria", linkCriteria);
		setChild("Hooks", hooks);
		setChild("InputSchema", inputSchema);
		setChild("OutputSchema", outputSchema);
		setChild("Connection", connectionConfig);
		setChild("Parser", parserConfig);
		setChild("DeltaSettings", deltaConfig);
		setChild("Checkpoint", checkpoint);
		setChild("Sandbox", sandbox);
		setChild("Operations", operations);
	}

	/**
	 * This method initializes the ConnectorConfig object, adds children to it
	 * and initializes them too.
	 * 
	 * @throws Exception
	 *             if could not parse or initialize some configuration
	 */
	public void init() throws Exception {

		// Attribute maps
		if (inputAttributeMap == null) {
			inputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN, new TreeMap()));
			inputAttributeMap
					.setName(MetamergeConfigFactory.parseName("Input"));
		}
		inputAttributeMap.setParent(this);
		inputAttributeMap.init();

		if (outputAttributeMap == null) {
			outputAttributeMap = new AttributeMapConfigImpl(getParameter(
					InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT, new TreeMap()));
			outputAttributeMap.setName(MetamergeConfigFactory
					.parseName("Output"));
		}
		outputAttributeMap.setParent(this);
		outputAttributeMap.init();

		// Link Criteria
		if (linkCriteria == null)
			linkCriteria = new LinkCriteriaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_LINK_CONFIG, new TreeMap()));
		linkCriteria.setParent(this);
		linkCriteria.init();

		// Hooks
		if (hooks == null)
			hooks = new HooksConfigImpl(getParameter(
					InternalSchema.CONNECTOR_HOOKS, new TreeMap()));
		hooks.setParent(this);
		hooks.init();

		// Input Schema
		if (inputSchema == null) {
			inputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_SCHEMA_INPUT, new TreeMap()));
			inputSchema.setName(MetamergeConfigFactory
					.parseName(ConnectorConfig.SCHEMA_INPUT));
		}
		inputSchema.setParent(this);
		inputSchema.init();

		// Output Schema
		if (outputSchema == null) {
			outputSchema = new SchemaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_SCHEMA_OUTPUT, new TreeMap()));
			outputSchema.setName(MetamergeConfigFactory
					.parseName(ConnectorConfig.SCHEMA_OUTPUT));
		}
		outputSchema.setParent(this);
		outputSchema.init();

		// Raw connector config
		if (connectionConfig == null)
			connectionConfig = new RawConnectorConfigImpl(getParameter(
					InternalSchema.CONNECTOR_CONNECTOR_CONFIG, new TreeMap()));
		connectionConfig.setParent(this);
		connectionConfig.init();

		// Parser config
		if (parserConfig == null)
			parserConfig = new ParserConfigImpl(getParameter(
					InternalSchema.CONNECTOR_PARSER_CONFIG, new TreeMap()));
		parserConfig.setParent(this);
		parserConfig.init();

		// Delta config
		if (deltaConfig == null)
			deltaConfig = new DeltaConfigImpl(getParameter(
					InternalSchema.CONNECTOR_DELTA_CONFIG, new TreeMap()));
		deltaConfig.setParent(this);
		deltaConfig.init();

		// Checkpoint config
		if (checkpoint == null)
			checkpoint = new CheckpointConfigImpl(getParameter(
					InternalSchema.CONNECTOR_CHECKPOINT_CONFIG, new TreeMap()));
		checkpoint.setParent(this);
		checkpoint.init();

		// Checkpoint config
		if (sandbox == null)
			sandbox = new SandboxConfigImpl(getParameter(
					InternalSchema.CONNECTOR_SANDBOX_CONFIG, new TreeMap()));
		sandbox.setParent(this);
		sandbox.init();

		// Operations
		if (operations == null) {
			operations = new ContainerConfigImpl(getParameter(
					InternalSchema.AL_OPERATIONS, new TreeMap()));
			operations.setName("Operations");
		}
		operations.setParent(this);
		operations.init();

		// Connector Pool Definition
		if (poolDefConfig == null) {
			poolDefConfig = new PoolDefConfigImpl(getParameter(
					InternalSchema.CONNECTOR_POOL_DEF_CONFIG, new TreeMap()));
		}
		if (poolDefConfig.getInheritsFromRef() == null)
			poolDefConfig.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		poolDefConfig.setParent(this);
		poolDefConfig.init();

		// Connector Pool Instance
		if (poolInstanceConfig == null) {
			poolInstanceConfig = new PoolInstanceConfigImpl(getParameter(
					InternalSchema.CONNECTOR_POOL_INSTANCE_CONFIG,
					new TreeMap()));
		}
		poolInstanceConfig.setParent(this);
		poolInstanceConfig.init();

		// Reconnect config
		if (reconnectConfig == null)
			reconnectConfig = new ReconnectConfigImpl(getParameter(
					InternalSchema.CONNECTOR_RECONNECT_CONFIG, new TreeMap()));
		if (reconnectConfig.getInheritsFromRef() == null)
			reconnectConfig
					.setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		reconnectConfig.setParent(this);
		reconnectConfig.init();

		updateChildNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setupInheritanceChain() throws Exception {

		// First check if we inherit internally
		String inheritFrom = getStringParameter(InternalSchema.INHERITS_FROM);
		if (BaseConfiguration.INHERIT_NONE.equals(inheritFrom)) {
			setInheritsFrom(null);
		} else if (inheritFrom != null && inheritFrom.indexOf("/") == -1) {
			if (inheritFrom
					.equals(com.ibm.di.server.ServerConstants.VIRTUAL_CONNECTOR_NAME))
				return;

			if (inheritFrom.startsWith("@"))
				inheritFrom = inheritFrom.substring(1);

			AssemblyLineConfig al = null;
			BaseConfiguration bc = getParent();
			while (bc != null) {
				if (bc instanceof AssemblyLineConfig) {
					al = (AssemblyLineConfig) bc;
					break;
				}
				bc = bc.getParent();
			}

			if (al != null)
				setInheritsFrom(al.getConnectorByName(inheritFrom));

		} else {
			// Call super class to set our own inherits from
			super.setupInheritanceChain();
		}

		// Call child objects to set their inherits from
		connectionConfig.setupInheritanceChain();
		parserConfig.setupInheritanceChain();
		inputSchema.setupInheritanceChain();
		outputSchema.setupInheritanceChain();
		inputAttributeMap.setupInheritanceChain();
		outputAttributeMap.setupInheritanceChain();
		hooks.setupInheritanceChain();
		linkCriteria.setupInheritanceChain();
		deltaConfig.setupInheritanceChain();
		checkpoint.setupInheritanceChain();
		poolDefConfig.setupInheritanceChain();
		reconnectConfig.setupInheritanceChain();

		if (getInheritsFrom() instanceof ConnectorConfig)
			operations.setInheritsFrom(((ConnectorConfig) getInheritsFrom())
					.getOperations());

		/*
		 * Ensure the Parser's schema is merged into the Connector's schema,
		 * only if the Connector can use a Parser.
		 */
		if (connectionConfig.getParserOption() != RawConnectorConfig.PARSER_USELESS) {
			((SchemaConfigImpl) inputSchema).attachSchema(parserConfig
					.getSchema(true));
			((SchemaConfigImpl) outputSchema).attachSchema(parserConfig
					.getSchema(false));
		} else {
			((SchemaConfigImpl) inputSchema).detachSchema(parserConfig
					.getSchema(true));
			((SchemaConfigImpl) outputSchema).detachSchema(parserConfig
					.getSchema(false));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateInheritsFrom(String ref) throws Exception {
		if (ref != null && ref.startsWith("@")) {
			super.setInheritsFromRef(ref);
			if (connectionConfig != null && INHERIT_PARENT.equals(connectionConfig.getInheritsFromRef())) {
				connectionConfig.setParameter(InternalSchema.CONNECTOR_CONNECTOR_PARSEROPTION, "Useless");
				connectionConfig.setJavaClass(ref);
			}
			setupInheritanceChain();
		} else {
			if (connectionConfig != null && INHERIT_PARENT.equals(connectionConfig.getInheritsFromRef())) {
				connectionConfig.removeParameter(InternalSchema.CONNECTOR_CONNECTOR_PARSEROPTION);
				connectionConfig.removeParameter(InternalSchema.CONNECTOR_CONNECTOR_JAVACLASS);
			}

			if (ref == null) {
				ref = "";
			}

			super.updateInheritsFrom(ref);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInheritsFromRef(String ref) {
		super.setInheritsFromRef(ref);
		if (connectionConfig != null) {
			MetamergeConfigFactory.logmsg(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.SET.INHERITS.FROM",
					new Object[] { ref }));
			try {
				setupInheritanceChain();
			} catch (Exception error) {
				error.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMode() {
		return (String) getParameter(InternalSchema.CONNECTOR_MODE,
				ConnectorConfig.ADDONLY_MODE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMode(String mode) {
		setStringParameter(InternalSchema.CONNECTOR_MODE, mode);
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getSchema(String name) {
		if ("Input".equalsIgnoreCase(name)) {
			return inputSchema;
		} else if ("Output".equalsIgnoreCase(name)) {
			return outputSchema;
		} else {
			SchemaConfig customSchema = new SchemaConfigImpl(getParameter(name,
					new TreeMap()));

			return customSchema;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getSchema(boolean input) {
		if (input)
			return inputSchema;
		else
			return outputSchema;
	}

	/**
	 * {@inheritDoc}
	 */
	public DeltaConfig getDeltaConfig() {
		return deltaConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolDefConfig getPoolDefConfig() {
		return poolDefConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public PoolInstanceConfig getPoolInstanceConfig() {
		return poolInstanceConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getConnectorScript() {
		return getScript();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConnectorScript(String script) {
		setScript(script);
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap(boolean input) {
		if (input)
			return inputAttributeMap;
		else
			return outputAttributeMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap(Object name) {
		// If null then determine based on connector's mode
		if (name == null)
			return getAttributeMap();

		//
		if (name.equals(ConnectorConfig.INPUT_MAP_NAME))
			return inputAttributeMap;
		else
			return outputAttributeMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap() {
		if (getMode().equals(ConnectorConfig.UPDATE_MODE)
				|| getMode().equals(ConnectorConfig.ADDONLY_MODE))
			return getAttributeMap(false);
		else
			return getAttributeMap(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttributeMap(AttributeMapConfig attributeMap) {
		if (getMode().equals(ConnectorConfig.UPDATE_MODE)
				|| getMode().equals(ConnectorConfig.ADDONLY_MODE))
			setAttributeMap(attributeMap, false);
		else
			setAttributeMap(attributeMap, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttributeMap(AttributeMapConfig attributeMap, boolean input) {
		if (input) {
			inputAttributeMap = attributeMap;
			setParameter(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_IN,
					attributeMap.getData());
			setChild("InputMap", attributeMap);
		} else {
			outputAttributeMap = attributeMap;
			setParameter(InternalSchema.CONNECTOR_ATTRIBUTE_MAP_OUT,
					attributeMap.getData());
			setChild("OutputMap", attributeMap);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public LinkCriteriaConfig getLinkCriteria() {
		return linkCriteria;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLinkCriteria(LinkCriteriaConfig linkCriteria) {
		this.linkCriteria = linkCriteria;
		setParameter(InternalSchema.CONNECTOR_LINK_CRITERIA, linkCriteria
				.getData());
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
	public RawConnectorConfig getConnectionConfig() {
		return connectionConfig;
	}

	/**
	 * Sets the connectionConfig attribute of the connector config.
	 * 
	 * @param rcc
	 *            new RawConnectorConfig object to set
	 */
	public void setConnectionConfig(RawConnectorConfig rcc) {
		connectionConfig = rcc;
		setParameter(InternalSchema.CONNECTOR_CONNECTOR_CONFIG, rcc.getData(),
				false);
		connectionConfig.setParent(this);
		try {
			connectionConfig.init();
			connectionConfig.setupInheritanceChain();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * @return associated Parser configuration
	 */
	public ParserConfig getParserConfig() {
		return parserConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParserConfig(ParserConfig parser) {
		parserConfig = parser;
		setParameter(InternalSchema.CONNECTOR_PARSER_CONFIG, parser.getData(),
				false);
		parserConfig.setParent(this);
		try {
			parserConfig.init();
			parserConfig.setupInheritanceChain();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getState() {
		return (String) getParameter(InternalSchema.CONNECTOR_STATE,
				ConnectorConfig.ENABLED_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setState(String state) {
		setStringParameter(InternalSchema.CONNECTOR_STATE, state);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getEnabled() {
		return getState().equals(ConnectorConfig.ENABLED_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		if (enabled)
			setStringParameter(InternalSchema.CONNECTOR_STATE,
					ConnectorConfig.ENABLED_STATE);
		else if (ConnectorConfig.SCRIPT_MODE.equals(getMode()))
			setStringParameter(InternalSchema.CONNECTOR_STATE,
					ConnectorConfig.DISABLED_STATE);
		else
			setStringParameter(InternalSchema.CONNECTOR_STATE,
					ConnectorConfig.PASSIVE_STATE);
	}

	/**
	 * Returns the compute-changes flag for Update mode connector.
	 * 
	 * @return The enabled value
	 */
	public boolean getComputeChanges() {
		return getBooleanParameter(InternalSchema.CONNECTOR_COMPUTE_CHANGES,
				true);
	}

	/**
	 * Sets the compute-changes flag for Update mode connector.
	 * 
	 * @param cc
	 *            The compute-change flag to set.
	 */
	public void setComputeChanges(boolean cc) {
		setBooleanParameter(InternalSchema.CONNECTOR_COMPUTE_CHANGES, cc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSkipLookup(boolean skipLookup) {
		setBooleanParameter(InternalSchema.CONNECTOR_SKIP_LOOKUP, skipLookup);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getSkipLookup() {
		return getBooleanParameter(InternalSchema.CONNECTOR_SKIP_LOOKUP, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportsSkipLookup() {
		return connectionConfig.getStringParameter("supportsSkipLookup") != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CheckpointConfig getCheckpointConfig() {
		return checkpoint;
	}

	/**
	 * {@inheritDoc}
	 */
	public SandboxConfig getSandboxConfig() {
		return sandbox;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDeltaBehavior() {
		return getIntegerParameter(InternalSchema.DELTA_BEHAVIOR,
				ConnectorConfig.DELTA_NORMAL);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDeltaBehavior(int behavior) {
		setIntegerParameter(InternalSchema.DELTA_BEHAVIOR, behavior);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDeltaStrict() {
		return getBooleanParameter(InternalSchema.DELTA_STRICT, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDeltaStrict(boolean strict) {
		setBooleanParameter(InternalSchema.DELTA_STRICT, strict);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerOption() {
		return connectionConfig
				.getStringParameter(InternalSchema.CONNECTOR_SERVER_OPTION);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getReplyRequired() {
		return connectionConfig.getBooleanParameter(
				InternalSchema.CONNECTOR_SERVER_REPLY, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEntryFeed() {
		return (ConnectorConfig.ITERATOR_MODE.equals(getMode()) || ConnectorConfig.SERVER_MODE
				.equals(getMode()));
	}

	/**
	 * {@inheritDoc}
	 */
	public ContainerConfig getOperations() {
		return operations;
	}

	/**
	 * {@inheritDoc}
	 */
	public OperationConfig getOperation(String name) {
		return (OperationConfig) getOperations().getConfig(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public OperationConfig createOperation(String name) throws Exception {
		if (getOperations().containsConfig(name, true))
			throw new NameAlreadyBoundException(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.NAMEBOUND.EXCEPTION",
					new Object[] { name }));

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
	public String getOperationCarrier() {
		return getStringParameter(InternalSchema.CONNECTOR_CONNECTOR_OPCARRIER);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOperationCarrier(String name) {
		setStringParameter(InternalSchema.CONNECTOR_CONNECTOR_OPCARRIER, name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getOperationCarrierIsProperty() {
		return getBooleanParameter(
				InternalSchema.CONNECTOR_CONNECTOR_OPCARRIERPROP, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOperationCarrierIsProperty(boolean isproperty) {
		setBooleanParameter(InternalSchema.CONNECTOR_CONNECTOR_OPCARRIERPROP,
				isproperty);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {
		inputAttributeMap.flatten(excludedNS);
		outputAttributeMap.flatten(excludedNS);
		linkCriteria.flatten(excludedNS);
		connectionConfig.flatten(excludedNS);
		inputSchema.flatten(excludedNS);
		outputSchema.flatten(excludedNS);
		hooks.flatten(excludedNS);
		parserConfig.flatten(excludedNS);
		deltaConfig.flatten(excludedNS);
		checkpoint.flatten(excludedNS);
		sandbox.flatten(excludedNS);
		operations.flatten(excludedNS);
		poolDefConfig.flatten(excludedNS);
		poolInstanceConfig.flatten(excludedNS);

		return super.flatten(excludedNS);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getReferences(List<String> list) {
		List<String> refs = super.getReferences(list);
		inputAttributeMap.getReferences(refs);
		outputAttributeMap.getReferences(refs);
		linkCriteria.getReferences(refs);
		connectionConfig.getReferences(refs);
		inputSchema.getReferences(refs);
		outputSchema.getReferences(refs);
		hooks.getReferences(refs);
		parserConfig.getReferences(refs);
		deltaConfig.getReferences(refs);
		checkpoint.getReferences(refs);
		sandbox.getReferences(refs);
		operations.getReferences(refs);
		poolDefConfig.getReferences(refs);
		poolInstanceConfig.getReferences(refs);
		return refs;
	}

	/**
	 * {@inheritDoc}
	 */
	public ReconnectConfig getReconnectConfig() {
		return reconnectConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitializeOption() {
		return getIntegerParameter(InternalSchema.CONNECTOR_INIT_OPTION,
				COMP_INIT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInitializeOption(int option) {
		setIntegerParameter(InternalSchema.CONNECTOR_INIT_OPTION, option);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getClone() throws Exception {
		ConnectorConfig cc = new ConnectorConfigImpl(deepClone(null));
		cc.setName(getName());
		cc.init();
		cc.setMetamergeConfig(getMetamergeConfig());

		// TODO; is it correct to set up inheritance here? I feel no, but we assume that it will be done
		cc.setParent(getParent()); // Temporary to set up inheritance. UGLY, but we need the parent for special cases.
		cc.setupInheritanceChain();
		cc.setParent(null); // set back to null.
        
		cc.setModTS(getModTS());
		return cc;
	}

	/**
	 * {@inheritDoc}
	 */
	public SchemaConfig getPublishedInitParams() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPublishedInitParams(SchemaConfig schema) {
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLimitOption() {
		return getStringParameter(InternalSchema.CONNECTOR_LOOKUP_LIMIT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLimitOption(String option) {
		if (option == null || option.length() == 0)
			removeParameter(InternalSchema.CONNECTOR_LOOKUP_LIMIT);
		else
			setParameter(InternalSchema.CONNECTOR_LOOKUP_LIMIT, option);
	}

	public String getSupportedModes() {
		return getStringParameter(InternalSchema.CONNECTOR_SUPPORTED_MODES);
	}

	public void setSupportedModes(String modes) {
		setStringParameter(InternalSchema.CONNECTOR_SUPPORTED_MODES, modes);
	}
}
