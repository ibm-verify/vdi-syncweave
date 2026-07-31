/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.store.StoreFactory;

/**
 * Read/write {@link ConnectorConfig} and children elements in XML format.
 */
public class ConnectorFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// XML Tags
	public final static String CONNECTOR_TAG = "Connector";

	public final static String MODE = "ConnectorMode";

	public final static String STATE = "ConnectorState";

	public final static String SCRIPT = "ConnectorScript";

	public final static String INIT_OPTION = "InitializeOption";

	public final static String COMPUTE_CHANGES = "ComputeChanges";
	
	public final static String SKIP_LOOKUP = "SkipLookup";

	public final static String LINK_CRITERIA = "LinkCriteria";

	public final static String LINK_CRITERIA_ITEM = "LinkCriteriaItem";

	public final static String LINK_CRITERIA_MODE = "AdvancedLinkMode";

	public final static String LINK_CRITERIA_OR = "MatchAny";

	public final static String LINK_CRITERIA_SCRIPT = "Script";

	public final static String LCI_ATTRIBUTE = "Attribute";

	public final static String LCI_OPERAND = "Operator";

	public final static String LCI_VALUE = "Value";

	public final static String LCI_KEY = "Key";

	public final static String ATTRIBUTE_MAP = "AttributeMap";

	public final static String ATTRIBUTE_MAP_ITEM = "AttributeMapItem";

	public final static String AMI_NAME = "Name";

	public final static String AMI_TYPE = "Type";

	public final static String AMI_SCRIPT = "Script";

	public final static String AMI_ADD = "Add";

	public final static String AMI_MODIFY = "Modify";

	public final static String AMI_SIMPLE = "Simple";

	public final static String AMI_ENABLED = "Enabled";

	public final static String AMI_SUBST_TEMPLATE = "SubstitutionTemplate";

	public final static String CONFIGURATION = "Configuration";

	public final static String HOOKS = "Hooks";

	public final static String HOOK = "Hook";

	public final static String DELTA_SETTINGS = "DeltaSettings";

	public final static String DELTA_ENABLED = "Enabled";

	public final static String DELTA_ATTRIBUTE = "UniqueAttribute";

	public final static String DELTA_FILEPATH = "FilePath";

	public final static String DELTA_READ_DELETED = "ReadDeleted";

	public final static String DELTA_REMOVE_DELETED = "RemoveDeleted";

	public final static String DELTA_RETURN_UNCHANGED = "ReturnUnchanged";

	public final static String DELTA_WHEN_TO_COMMIT = "WhenToCommit";

	public final static String DELTA_ROW_LOCKING = "RowLocking";

	public final static String DELTA_ATTRIBUTE_LIST = "AttributeList";
	
	public final static String DELTA_CHANGE_DETECTION_MODE = "ChangeDetectionMode";
	
	public final static String DELTA_DRIVER = "Driver";

	public final static String DELTA_LEVEL = "Level";

	public final static String DELTA_BEHAVIOR = "DeltaBehavior";

	public final static String DELTA_STRICT = "DeltaStrict";

	public final static String DELTA_FAST_ALGORITHM = "DeltaFastAlgorithm";

	public final static String DELTA_DUPLICATE_KEYS = "AllowDuplicateDeltaKeys";

	public final static String POOL_DEF = "PoolDefinition";

	public final static String POOL_DEF_ENABLED = "Enabled";

	public final static String POOL_DEF_MIN_SIZE = "MinPoolSize";

	public final static String POOL_DEF_MAX_SIZE = "MaxPoolSize";

	public final static String POOL_DEF_PURGE_INTERVAL = "PurgeInterval";

	public final static String POOL_DEF_INITIALIZE_ATTEMPTS = "InitializeAttempts";

	public final static String POOL_DEF_INITIALIZE_SLEEP_INTERVAL = "InitializeSleepInterval";

	public final static String POOL_INSTANCE = "PoolInstance";

	public final static String POOL_INSTANCE_ENABLED = "Enabled";

	public final static String POOL_INSTANCE_EXHAUSTED_BEHAVIOR = "ExhaustedPoolBehavior";

	public final static String OPS_CARRIER = "OperationCarrier";

	public final static String OPS_CARRIER_PROP = "OperationCarrierIsProperty";

	public final static String LOOKUP_LIMIT = "LookupLimit";

	public final static String SUPPORTED_MODES = "SupportedModes";

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		Element e;
		String str;

		ConnectorConfig cc = (ConnectorConfig) config;
		cc.init();

		// Set name and initialize
		getBaseName(cc, elem);

		// Connector mode
		if ((str = getNodeTextByName(elem, MODE)) != null)
			cc.setMode(str);

		// Connector state
		if ((str = getNodeTextByName(elem, STATE)) != null)
			cc.setState(str);

		// Add Parser settings
		if ((e = getSingleElement(elem, ParserFactory.PARSER_TAG)) != null)
			getFactory(ParserFactory.PARSER_TAG).parse(cc.getParserConfig(), e);

		// Add Raw Connector
		if ((e = getSingleElement(elem, CONFIGURATION)) != null) {
			getBaseName(cc.getConnectionConfig(), e);
			getParameters(e, cc.getConnectionConfig());
		}

		// Script
		str = getNodeTextByName(elem, SCRIPT);
		if (str != null && str.length() > 0)
			cc.setScript(str);

		// Compute Changes
		str = getNodeTextByName(elem, COMPUTE_CHANGES);
		if (str != null && str.length() > 0)
			cc.setComputeChanges(Boolean.valueOf(str).booleanValue());
		// Skip Lookup
		str = getNodeTextByName(elem, SKIP_LOOKUP);
		if (str != null && str.length() > 0)
			cc.setSkipLookup(Boolean.valueOf(str).booleanValue());

		// Use Delta
		str = getNodeTextByName(elem, DELTA_BEHAVIOR);
		if (str != null && str.length() > 0)
			cc.setDeltaBehavior(Integer.valueOf(str).intValue());

		str = getNodeTextByName(elem, DELTA_STRICT);
		if (str != null && str.length() > 0)
			cc.setDeltaStrict(Boolean.valueOf(str).booleanValue());

		// Attribute Maps
		getAttributeMaps(elem, cc);

		// Delta settings
		getDeltaSettings(getSingleElement(elem, DELTA_SETTINGS), cc
				.getDeltaConfig());

		// Schema
		getSchemas(elem, cc);

		// Hooks
		if ((e = getSingleElement(elem, HookFactory.HOOK_TAG)) != null)
			getFactory(HookFactory.HOOK_TAG).parse(cc.getHooks(), e);

		// Link Criteria
		if ((e = getSingleElement(elem, LINK_CRITERIA)) != null)
			getLinkCriteria(cc.getLinkCriteria(), e);

		// Checkpoint config
		if ((e = getSingleElement(elem, CheckpointFactory.CHECKPOINT_TAG)) != null)
			getFactory(CheckpointFactory.CHECKPOINT_TAG).parse(
					cc.getCheckpointConfig(), e);

		// Sandbox config
		if ((e = getSingleElement(elem, SandboxFactory.SANDBOX_TAG)) != null)
			getFactory(SandboxFactory.SANDBOX_TAG).parse(cc.getSandboxConfig(),
					e);

		// Reconnect config
		if ((e = getSingleElement(elem, ReconnectFactory.RECONNECT_TAG)) != null) {
			getFactory(ReconnectFactory.RECONNECT_TAG).parse(
					cc.getReconnectConfig(), e);
		}

		// Operations
		if ((e = getSingleElement(elem, AssemblyLineFactory.OPERATIONS_TAG)) != null)
			((AssemblyLineFactory) getFactory(AssemblyLineFactory.ASSEMBLYLINE_TAG))
					.getOperations(e, cc);

		str = getNodeTextByName(elem, OPS_CARRIER);
		if (str != null && str.length() > 0)
			cc.setOperationCarrier(str);

		str = getNodeTextByName(elem, OPS_CARRIER_PROP);
		if (str != null && str.length() > 0)
			cc.setOperationCarrierIsProperty(Boolean.valueOf(str)
					.booleanValue());

		// Pool Definition
		getPoolDefConfig(getSingleElement(elem, POOL_DEF), cc
				.getPoolDefConfig());

		// Pool Instance
		getPoolInstanceConfig(getSingleElement(elem, POOL_INSTANCE), cc
				.getPoolInstanceConfig());

		// Init Option
		str = getNodeTextByName(elem, INIT_OPTION);
		if (str != null && str.length() > 0)
			cc.setInitializeOption(Integer.parseInt(str));

		if (!MetamergeConfigXML.METAMERGE_VERSION_ID.equals(cc
				.getMetamergeConfig().getConfigVersion())) {
			try {
				migrateConnectorConfig(cc);
			} catch (Throwable err) {
				logger.error("migrateConnectorConfig", err);
			}
		}
		
		// Lookup Limit
		str = getNodeTextByName(elem, LOOKUP_LIMIT);
		if (str != null && str.length() > 0)
			cc.setLimitOption(str);

		// Supported Modes
		str = getNodeTextByName(elem, SUPPORTED_MODES);
		if (str != null && str.length() > 0)
			cc.setSupportedModes(str);
	}

	public void getLinkCriteria(LinkCriteriaConfig lcc, Element elem)
			throws Exception {
		String str;

		getBaseName(lcc, elem);

		if ((str = getNodeTextByName(elem, LINK_CRITERIA_OR)) != null)
			lcc.setMatchAny(Boolean.valueOf(str).booleanValue());

		if ((str = getNodeTextByName(elem, LINK_CRITERIA_MODE)) != null)
			lcc.setAdvancedLinkMode(Boolean.valueOf(str).booleanValue());

		if ((str = getNodeTextByName(elem, LINK_CRITERIA_SCRIPT)) != null)
			lcc.setAdvancedLinkCriteria(str);

		getLinkCriteriaItems(lcc, elem);
	}

	public void getLinkCriteriaItems(LinkCriteriaConfig lcc, Element elem)
			throws Exception {
		String str;
		NodeList list = elem.getElementsByTagName(LINK_CRITERIA_ITEM);
		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			if ((str = getNodeTextByName(node, LCI_KEY)) == null)
				continue;
			LinkCriteriaItem lci = lcc.newCriteria(str);
			if ((str = getNodeTextByName(node, LCI_ATTRIBUTE)) != null)
				lci.setAttribute(str);
			if ((str = getNodeTextByName(node, LCI_OPERAND)) != null)
				lci.setOper(str);
			if ((str = getNodeTextByName(node, LCI_VALUE)) != null)
				lci.setValue(str);
			if ((str = getNodeTextByName(node, AMI_ENABLED)) != null && str.length() > 0)
				lci.setEnabled(Boolean.valueOf(str));
			lcc.setCriteria(lci);
		}
	}

	public void getSchemas(Element elem, ConnectorConfig config)
			throws Exception {
		NodeList list = elem.getElementsByTagName(SchemaFactory.SCHEMA_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			// String version =
			//((MetamergeConfigXML)config.getMetamergeConfig()).getConfigVersion
			// ();
			String name = e.getAttribute(NAME_ATTRIBUTE);
			if (name.equals("")) {
				logmsg(sResHash.getString("MMCONFIG.CONNFACTORY.XML.MIGRATING"));
				// Migrate 1.0 to 1.1
				((SchemaFactory) getFactory(SchemaFactory.SCHEMA_TAG)).migrate(
						config, e);
			} else {
				getFactory(SchemaFactory.SCHEMA_TAG).parse(
						config.getSchema(name), e);
			}
		}
	}

	public void getAttributeMaps(Element elem, ConnectorConfig config)
			throws Exception {
		NodeList list = elem.getElementsByTagName(ATTRIBUTE_MAP);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute(NAME_ATTRIBUTE);
			AttributeMapConfig amc = config.getAttributeMap(name);
			getAttributeMap(e, amc);
		}
		getNullBehavior(elem, config);
	}

	public void getAttributeMapItem(Element elem, AttributeMapConfig amc)
			throws Exception {
		String str = getNodeTextByName(elem, AMI_NAME);
		getAttributeMapItem(elem, amc.getAttributeMapItem(str));
	}

	public void getAttributeMap(Element elem, AttributeMapConfig amc)
			throws Exception {
		// Set name and inherit from
		getBaseName(amc, elem);

		// Get all AttributeMapItem elements
		NodeList list = elem.getElementsByTagName(ATTRIBUTE_MAP_ITEM);
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getParentNode() == elem) {
				String str = getNodeTextByName((Element) list.item(i), AMI_NAME);
				getAttributeMapItem((Element) list.item(i), amc
						.getAttributeMapItem(str));
			}
		}

		getNullBehavior(elem, amc);
	}

	public void getAttributeMapItem(Element elem, AttributeMapItem ami)
			throws Exception {
		String str;

		// Enabled
		str = getNodeTextByName(elem, AMI_ENABLED);
		if (str != null && str.length() > 0)
			ami.setEnabled(Boolean.valueOf(str).booleanValue());

		// Add
		str = getNodeTextByName(elem, AMI_ADD);
		if (str != null && str.length() > 0)
			ami.setAdd(Boolean.valueOf(str).booleanValue());

		// Modify
		str = getNodeTextByName(elem, AMI_MODIFY);
		if (str != null && str.length() > 0)
			ami.setModify(Boolean.valueOf(str).booleanValue());

		// Simple
		str = getNodeTextByName(elem, AMI_SIMPLE);
		if (str != null && str.length() > 0)
			ami.setSimple(str);

		// Script
		str = getNodeTextByName(elem, AMI_SCRIPT);
		if (str != null && str.length() > 0)
			ami.setScript(str);

		// Substitution
		str = getNodeTextByName(elem, AMI_SUBST_TEMPLATE);
		if (str != null && str.length() > 0)
			ami.setSubstitution(str);

		// Type
		str = getNodeTextByName(elem, AMI_TYPE);
		if (str != null && str.length() > 0)
			ami.setType(str);

		getNullBehavior(elem, ami);

		getInheritsFrom(ami, elem);
		
		// Get all child AttributeMapItem elements
		NodeList list = elem.getElementsByTagName(ATTRIBUTE_MAP_ITEM);
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getParentNode() != elem)
				continue;

			AttributeMapItem child = new AttributeMapItemImpl();
			child.setParent(ami);
			child.init();
			ami.getChildAttributeMaps().add(child);
			getAttributeMapItem((Element) list.item(i), child);
		}

	}

	public void getDeltaSettings(Element elem, DeltaConfig dc) throws Exception {
		if (elem == null)
			return;

		String str;
		getBaseName(dc, elem);

		if ((str = getNodeTextByName(elem, DELTA_ENABLED)) != null)
			dc.setParameter(InternalSchema.ENABLED, str);

		if ((str = getNodeTextByName(elem, DELTA_ATTRIBUTE)) != null)
			dc.setUniqueAttribute(str);

		if ((str = getNodeTextByName(elem, DELTA_FILEPATH)) != null)
			dc.setDeltaDB(str);

		if ((str = getNodeTextByName(elem, DELTA_READ_DELETED)) != null)
			dc.setParameter( InternalSchema.CONNECTOR_DELTA_ITER_DELETED, str);

		if ((str = getNodeTextByName(elem, DELTA_REMOVE_DELETED)) != null)
			dc.setParameter(InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED, str);

		if ((str = getNodeTextByName(elem, DELTA_RETURN_UNCHANGED)) != null)
			dc.setParameter(InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED, str);

		if ((str = getNodeTextByName(elem, DELTA_FAST_ALGORITHM)) != null)
			dc.setParameter( InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM, str);

		if ((str = getNodeTextByName(elem, DELTA_DUPLICATE_KEYS)) != null)
			dc.setParameter(InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS, str);

		if ((str = getNodeTextByName(elem, DELTA_WHEN_TO_COMMIT)) != null)
			dc.setWhenToCommit(str);

		if ((str = getNodeTextByName(elem, DELTA_DRIVER)) != null)
			dc.setDriver(str);

		if ((str = getNodeTextByName(elem, DELTA_LEVEL)) != null)
			dc.setParameter(InternalSchema.CONNECTOR_DELTA_LEVEL, str);

		if ((str = getNodeTextByName(elem, DELTA_ROW_LOCKING)) != null) {
			dc.setRowLocking(str);
		}

		if ((str = getNodeTextByName(elem, DELTA_CHANGE_DETECTION_MODE)) != null) {
			dc.setChangeDetectionMode(str);
		}

		if ((str = getNodeTextByName(elem, DELTA_ATTRIBUTE_LIST)) != null) {
			dc.setAttributeList(str);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {

		ConnectorConfig cc = (ConnectorConfig) config;
		Element e = elem;

		// Name & Inherit
		setBaseName(cc, e);

		// Mode
		setSingleElement(e, MODE, cc.getMode());

		// State
		setSingleElement(e, STATE, cc.getState());

		// Raw Connector
		Element rc = elem.getOwnerDocument().createElement(CONFIGURATION);
		elem.appendChild(rc);
		setBaseName(cc.getConnectionConfig(), rc);
		setParameters(rc, cc.getConnectionConfig(), null);

		// Script
		setSingleElement(e, SCRIPT, cc, InternalSchema.SCRIPT);

		// Compute Changes
		setSingleElement(e, COMPUTE_CHANGES, cc,
				InternalSchema.CONNECTOR_COMPUTE_CHANGES);

		// Skip Lookup
		setSingleElement(e, SKIP_LOOKUP, cc,
				InternalSchema.CONNECTOR_SKIP_LOOKUP);

		// Use Delta
		setSingleElement(e, DELTA_BEHAVIOR, cc, InternalSchema.DELTA_BEHAVIOR);
		setSingleElement(e, DELTA_STRICT, cc, InternalSchema.DELTA_STRICT);

		// Parser
		Element parserElement = e.getOwnerDocument().createElement(
				ParserFactory.PARSER_TAG);
		e.appendChild(parserElement);
		getFactory(ParserFactory.PARSER_TAG).build(cc.getParserConfig(),
				parserElement);

		// Attribute maps
		setAttributeMaps(e, cc);

		// Delta Settings
		setDeltaSettings(e, cc.getDeltaConfig());

		// Schema
		setSchemas(e, cc);

		// Link Criteria
		setLinkCriteria(cc.getLinkCriteria(), e);

		// Hooks
		getFactory(HookFactory.HOOK_TAG).build(cc.getHooks(), e);

		// Checkpoint config
		Element cpe = elem.getOwnerDocument().createElement(
				CheckpointFactory.CHECKPOINT_TAG);
		elem.appendChild(cpe);
		getFactory(CheckpointFactory.CHECKPOINT_TAG).build(
				cc.getCheckpointConfig(), cpe);

		// Sandbox config
		cpe = elem.getOwnerDocument().createElement(SandboxFactory.SANDBOX_TAG);
		elem.appendChild(cpe);
		getFactory(SandboxFactory.SANDBOX_TAG)
				.build(cc.getSandboxConfig(), cpe);

		// Reconnect config
		rc = elem.getOwnerDocument().createElement(
				ReconnectFactory.RECONNECT_TAG);
		elem.appendChild(rc);
		getFactory(ReconnectFactory.RECONNECT_TAG).build(
				cc.getReconnectConfig(), rc);

		// AL Operations
		cpe = elem.getOwnerDocument().createElement(
				AssemblyLineFactory.OPERATIONS_TAG);
		elem.appendChild(cpe);
		((AssemblyLineFactory) getFactory(AssemblyLineFactory.ASSEMBLYLINE_TAG))
				.setOperations(cpe, cc);

		setSingleElement(e, OPS_CARRIER_PROP, cc,
				InternalSchema.CONNECTOR_CONNECTOR_OPCARRIERPROP);
		setSingleElement(e, OPS_CARRIER, cc,
				InternalSchema.CONNECTOR_CONNECTOR_OPCARRIER);

		// Pool Definition
		setPoolDefConfig(e, cc.getPoolDefConfig());

		// Pool Instance
		setPoolInstanceConfig(e, cc.getPoolInstanceConfig());

		// Initialize Option
		setSingleElement(e, INIT_OPTION, cc, InternalSchema.CONNECTOR_INIT_OPTION);

		// Lookup Limit or Read Limit
		setSingleElement(e, LOOKUP_LIMIT, cc, InternalSchema.CONNECTOR_LOOKUP_LIMIT);

		// Supported modes
		setSingleElement(e, SUPPORTED_MODES, cc, InternalSchema.CONNECTOR_SUPPORTED_MODES);
	}

	public void setSchemas(Element elem, ConnectorConfig cc) throws Exception {
		getFactory(SchemaFactory.SCHEMA_TAG).build(cc.getSchema("Input"), elem);
		getFactory(SchemaFactory.SCHEMA_TAG)
				.build(cc.getSchema("Output"), elem);
	}

	public void setAttributeMaps(Element elem, ConnectorConfig cc)
			throws Exception {
		setAttributeMap(cc.getAttributeMap(true), elem);
		setAttributeMap(cc.getAttributeMap(false), elem);
		setNullBehavior(cc, elem);
	}

	public void setAttributeMap(AttributeMapConfig amc, Element elem)
			throws Exception {

		Element e = elem.getOwnerDocument().createElement(ATTRIBUTE_MAP);
		elem.appendChild(e);
		setBaseName(amc, e);

		setAttributeMapItems(amc, e);

		setNullBehavior(amc, e);
	}

	public void setAttributeMapItems(AttributeMapConfig amc, Element e)
			throws Exception {

		List<String> list = amc.getKeys(BaseConfiguration.SUBTREE);
		for (int i = 0; i < list.size(); i++) {
			AttributeMapItem ami = amc.getAttributeMapItem(list.get(i));
			if (ami.size() == 0)
				continue;

			Element p = e.getOwnerDocument().createElement(ATTRIBUTE_MAP_ITEM);
			setSingleElement(p, AMI_NAME, "" + list.get(i));
			setSingleElement(p, AMI_TYPE, ami, InternalSchema.AMI_TYPE);
			setSingleElement(p, AMI_ENABLED, ami, InternalSchema.ENABLED);
			setSingleElement(p, AMI_ADD, ami, InternalSchema.AMI_ADD);
			setSingleElement(p, AMI_MODIFY, ami, InternalSchema.AMI_MODIFY);
			setSingleElement(p, AMI_SCRIPT, ami, InternalSchema.AMI_SCRIPT);
			setSingleElement(p, AMI_SIMPLE, ami, InternalSchema.AMI_SIMPLE);
			setSingleElement(p, AMI_SUBST_TEMPLATE, ami,
					InternalSchema.AMI_SUBSTITUTION);

			setNullBehavior(ami, p);

			// 7.0: Attribute map items can inherit
			setInheritsFrom(ami, p);

			e.appendChild(p);
		}
	}

	public void setLinkCriteria(LinkCriteriaConfig lcc, Element elem)
			throws Exception {

		Element e = elem.getOwnerDocument().createElement(LINK_CRITERIA);
		elem.appendChild(e);

		setBaseName(lcc, e);

		// Match Any (OR rather than AND)
		setSingleElement(e, LINK_CRITERIA_OR, lcc,
				InternalSchema.CONNECTOR_LINK_OR);

		// Advanced link mode (true / false)
		setSingleElement(e, LINK_CRITERIA_MODE, lcc,
				InternalSchema.CONNECTOR_LINK_MODE);

		// Advanced link script
		setSingleElement(e, LINK_CRITERIA_SCRIPT, lcc,
				InternalSchema.CONNECTOR_ADVANCED_LINK_CRITERIA);

		// The rest of the pack
		setLinkCriteriaItems(lcc, e);
	}

	public void setLinkCriteriaItems(LinkCriteriaConfig lcc, Element elem)
			throws Exception {

		List<String> list = lcc.getCriteriaNames();
		for (int i = 0; i < list.size(); i++) {
			if (!lcc.isCriteriaLocal(list.get(i)))
				continue;

			LinkCriteriaItem lci = lcc.getCriteria(list.get(i));
			if (lci.getData().size() == 0)
				continue;

			Element e = elem.getOwnerDocument().createElement(LINK_CRITERIA_ITEM);
			elem.appendChild(e);

			setSingleElement(e, LCI_KEY, lci.getShortName());
			setSingleElement(e, LCI_ATTRIBUTE, lci, InternalSchema.LC_ATTRIBUTE);
			setSingleElement(e, LCI_OPERAND, lci, InternalSchema.LC_OPERATOR);
			setSingleElement(e, LCI_VALUE, lci, InternalSchema.LC_VALUE);
			setSingleElement(e, AMI_ENABLED, lci, InternalSchema.ENABLED);
		}

	}

	public void setDeltaSettings(Element elem, DeltaConfig dc) throws Exception {
		Element e = elem.getOwnerDocument().createElement(DELTA_SETTINGS);
		elem.appendChild(e);

		setBaseName(dc, e);

		setSingleElement(e, DELTA_ENABLED, dc, InternalSchema.ENABLED);
		setSingleElement(e, DELTA_ATTRIBUTE, dc,
				InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR);
		setSingleElement(e, DELTA_FILEPATH, dc,
				InternalSchema.CONNECTOR_DELTA_DB);
		setSingleElement(e, DELTA_READ_DELETED, dc,
				InternalSchema.CONNECTOR_DELTA_ITER_DELETED);
		setSingleElement(e, DELTA_REMOVE_DELETED, dc,
				InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED);
		setSingleElement(e, DELTA_RETURN_UNCHANGED, dc,
				InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED);
		setSingleElement(e, DELTA_FAST_ALGORITHM, dc,
				InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM);
		setSingleElement(e, DELTA_DUPLICATE_KEYS, dc,
				InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS);
		setSingleElement(e, DELTA_WHEN_TO_COMMIT, dc,
				InternalSchema.CONNECTOR_DELTA_WHEN_TO_COMMIT);
		setSingleElement(e, DELTA_DRIVER, dc,
				InternalSchema.CONNECTOR_DELTA_DRIVER);
		setSingleElement(e, DELTA_LEVEL, dc,
				InternalSchema.CONNECTOR_DELTA_LEVEL);
		setSingleElement(e, DELTA_ROW_LOCKING, dc,
				InternalSchema.CONNECTOR_DELTA_ROW_LOCKING);
		setSingleElement(e, DELTA_CHANGE_DETECTION_MODE, dc,
				InternalSchema.CONNECTOR_DELTA_CHANGE_DETECTION_MODE);
		setSingleElement(e, DELTA_ATTRIBUTE_LIST, dc,
				InternalSchema.CONNECTOR_DELTA_ATTRIBUTE_LIST);
					
	}

	public void getPoolDefConfig(Element elem, PoolDefConfig poolConfig)
			throws Exception {
		if (elem == null)
			return;

		String str;
		getBaseName(poolConfig, elem);

		if ((str = getNodeTextByName(elem, POOL_DEF_ENABLED)) != null)
			poolConfig.setPoolEnabled(Boolean.valueOf(str).booleanValue());

		if ((str = getNodeTextByName(elem, POOL_DEF_MIN_SIZE)) != null)
			poolConfig.setParameter(InternalSchema.CONNECTOR_POOL_DEF_MIN_SIZE, str);

		if ((str = getNodeTextByName(elem, POOL_DEF_MAX_SIZE)) != null)
			poolConfig.setParameter(InternalSchema.CONNECTOR_POOL_DEF_MAX_SIZE, str);

		if ((str = getNodeTextByName(elem, POOL_DEF_PURGE_INTERVAL)) != null)
			poolConfig.setParameter(InternalSchema.CONNECTOR_POOL_DEF_PURGE_INTERVAL, str);

		if ((str = getNodeTextByName(elem, POOL_DEF_INITIALIZE_ATTEMPTS)) != null)
			poolConfig.setParameter(InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_ATTEMPTS, str);

		if ((str = getNodeTextByName(elem, POOL_DEF_INITIALIZE_SLEEP_INTERVAL)) != null)
			poolConfig.setParameter(InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_SLEEP_INTERVAL, str);
	}

	public void setPoolDefConfig(Element elem, PoolDefConfig poolConfig)
			throws Exception {
		Element e = elem.getOwnerDocument().createElement(POOL_DEF);
		elem.appendChild(e);

		setBaseName(poolConfig, e);

		setSingleElement(e, POOL_DEF_ENABLED, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_ENABLED);
		setSingleElement(e, POOL_DEF_MIN_SIZE, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_MIN_SIZE);
		setSingleElement(e, POOL_DEF_MAX_SIZE, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_MAX_SIZE);
		setSingleElement(e, POOL_DEF_PURGE_INTERVAL, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_PURGE_INTERVAL);
		setSingleElement(e, POOL_DEF_INITIALIZE_ATTEMPTS, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_ATTEMPTS);
		setSingleElement(e, POOL_DEF_INITIALIZE_SLEEP_INTERVAL, poolConfig,
				InternalSchema.CONNECTOR_POOL_DEF_INITIALIZE_SLEEP_INTERVAL);
	}

	public void getPoolInstanceConfig(Element elem,
			PoolInstanceConfig poolConfig) throws Exception {
		if (elem == null) {
			return;
		}

		String str;
		getBaseName(poolConfig, elem);

		if ((str = getNodeTextByName(elem, POOL_INSTANCE_ENABLED)) != null) {
			poolConfig.setPoolEnabled(Boolean.valueOf(str).booleanValue());
		}

		if ((str = getNodeTextByName(elem, POOL_INSTANCE_EXHAUSTED_BEHAVIOR)) != null) {
			int exhBehavior = PoolInstanceConfig.EXHAUSTED_POOL_WAIT;
			for (int i = 0; i < PoolInstanceConfig.EXH_POOL_NAMES.length; i++) {
				if (PoolInstanceConfig.EXH_POOL_NAMES[i].equalsIgnoreCase(str)) {
					exhBehavior = i;
					break;
				}
			}
			poolConfig.setExhaustedPoolBehavior(exhBehavior);
		}
	}

	public void setPoolInstanceConfig(Element elem,
			PoolInstanceConfig poolConfig) throws Exception {
		Element e = elem.getOwnerDocument().createElement(POOL_INSTANCE);
		elem.appendChild(e);

		setBaseName(poolConfig, e);

		setSingleElement(e, POOL_INSTANCE_ENABLED, poolConfig,
				InternalSchema.CONNECTOR_POOL_INSTANCE_ENABLED);

		if (poolConfig
				.hasParameter(InternalSchema.CONNECTOR_POOL_INSTANCE_EXHAUSTED_BEHAVIOR)) {
			int exhBehavior = poolConfig.getExhaustedPoolBehavior();
			if (exhBehavior < 0
					|| exhBehavior >= PoolInstanceConfig.EXH_POOL_NAMES.length) {
				exhBehavior = PoolInstanceConfig.EXHAUSTED_POOL_WAIT;
			}
			setSingleElement(e, POOL_INSTANCE_EXHAUSTED_BEHAVIOR,
					PoolInstanceConfig.EXH_POOL_NAMES[exhBehavior]);
		}
	}

	/**
	 * Migrate Connector parameters from an older configuration.
	 * 
	 * @param cc
	 *            Connector configuration
	 */
	private void migrateConnectorConfig(ConnectorConfig cc) {

		String configVersion = cc.getMetamergeConfig().getConfigVersion();

		if("7.1".compareTo(configVersion) > 0) {
			/*
			 * Migration code for "Select Database driver" parameter of the
			 * System Store Connector. For pre-7.1 configurations this parameter
			 * represents the used database. Since TDI 7.1 this parameter
			 * specifies the actual java class name of the used JDBC driver.
			 * Here we do the mapping from database name to driver class name. 
			 */
			if ("system:/Connectors/ibmdi.SystemStoreConnector".equals(cc.getInheritsFromRef())) {
				String selectDBParam = (String) cc.getConnectionConfig().getParameter("selectDBDriver");

				// The default value for pre-7.1 is Derby/CloudScape
				if (selectDBParam == null || "".equals(selectDBParam) 
						|| "Derby".equalsIgnoreCase(selectDBParam) 
						|| "CloudScape".equalsIgnoreCase(selectDBParam)) {
					cc.getConnectionConfig().setParameter("selectDBDriver", StoreFactory.JDBC_DRIVER_DERBY_NET);
				} else if (selectDBParam != null && "DB2".equalsIgnoreCase(selectDBParam)) {
					cc.getConnectionConfig().setParameter("selectDBDriver", StoreFactory.JDBC_DRIVER_DB2); 
				} else if (selectDBParam != null && "Other".equalsIgnoreCase(selectDBParam)) {
					cc.getConnectionConfig().setParameter("selectDBDriver", ""); 
				}
			}
		} else if ("7.0".compareTo(configVersion) > 0) {
			/*
			 * For the IDS Changelog, the SunDS Changelog and the zOS Changelog
			 * Connectors, the default for the merge mode parameter is changed
			 * in 7.0. For pre-7.0 configurations we need to set the parameter
			 * explicitly to keep backward compatibility.
			 */
			if ("system:/Connectors/ibmdi.IBMDirectoryServerChangelog"
					.equals(cc.getInheritsFromRef())
					|| "system:/Connectors/ibmdi.NetscapeChangelog".equals(cc
							.getInheritsFromRef())
					|| "system:/Connectors/ibmdi.zOSChangelog".equals(cc
							.getInheritsFromRef())) {

				Object mergeModeParam = cc.getConnectionConfig().getParameter(
						"mergeMode");
				if (null == mergeModeParam || "".equals(mergeModeParam)) {
					cc.getConnectionConfig().setParameter("mergeMode",
							"Merge changelog and changed data");
				}
			}

			/*
			 * For the DB Changelog Connector, the the paging parameter is
			 * removed in 7.0 and the only behavior available is with enabled
			 * paging. So for pre-7.0 configurations we need to set the
			 * parameter explicitly to true.
			 */
			if ("system:/Connectors/ibmdi.MemQueue".equals(cc
					.getInheritsFromRef())) {
				Object pagingParam = cc.getConnectionConfig().getParameter(
						"paging");

				if (null == pagingParam || "false".equals(pagingParam)) {
					cc.getConnectionConfig().setParameter("paging", "true");
				}
			}
			
			/*
			 * For the Domino Users Connector, in 7.0 the 'authMechanism'
			 * parameter is replaced by the 'dominoSessionType' parameter. For
			 * pre-7.0 configurations we need to migrate the value of the
			 * 'authMechanism' parameter.
			 */
			if ("system:/Connectors/ibmdi.DominoUsersConnector".equals(cc
					.getInheritsFromRef())) {

				Object authParam = cc.getConnectionConfig().getParameter(
						"authMechanism");

				if ("Notes ID File".equals(authParam)) {
					cc.getConnectionConfig().setParameter("dominoSessionType",
							"LocalClient");
				} else if ("Internet Password".equals(authParam)) {
					cc.getConnectionConfig().setParameter("dominoSessionType",
							"LocalServer");
				} else if (authParam == null) {
					/*
					 * The default for 'authMechanism' was 'Internet Password',
					 * which corresponds to a 'LocalServer' value for
					 * 'dominoSessionType'. However, the default for
					 * 'dominoSessionType' is 'IIOP'. So we cannot leave it to
					 * the default, we have to set it explicitly to
					 * 'LocalServer'.
					 */
					cc.getConnectionConfig().setParameter("dominoSessionType",
							"LocalServer");
				}
			}
		}
	}
}
