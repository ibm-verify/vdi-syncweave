/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.CallConfig;
import com.ibm.di.config.interfaces.CallParamConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.OperationsConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;

/**
 * This class implements the reading and writing of a {@link AssemblyLineConfig} in XML format.
 */
public class AssemblyLineFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * XML tag - {@value #ASSEMBLYLINE_TAG}
	 */
	public final static String ASSEMBLYLINE_TAG = "AssemblyLine";

	/**
	 * XML tag - {@value #CONNECTORS_TAG}
	 */
	public final static String CONNECTORS_TAG = "Connectors";

	/**
	 * XML tag - {@value #LOG_ENABLED_TAG}
	 */
	public final static String LOG_ENABLED_TAG = "LogEnabled";

	/**
	 * XML tag - {@value #PROLOG_TAG}
	 */
	public final static String PROLOG_TAG = "Prolog";

	/**
	 * XML tag - {@value #PROLOGINIT_TAG}
	 */
	public final static String PROLOGINIT_TAG = "PrologInit";

	/**
	 * XML tag - {@value #EPILOG_TAG}
	 */
	public final static String EPILOG_TAG = "Epilog";

	/**
	 * XML tag - {@value #EPILOG2_TAG}
	 */
	public final static String EPILOG2_TAG = "Epilog2";

	/**
	 * XML tag - {@value #SHUTDOWN_TAG}
	 */
	public final static String SHUTDOWN_TAG = "Shutdown";

	/**
	 * XML tag - {@value #STARTCYCLE_TAG}
	 */
	public final static String STARTCYCLE_TAG = "StartCycle";

	/**
	 * XML tag - {@value #ONSUCCESS_TAG}
	 */
	public final static String ONSUCCESS_TAG = "OnSuccess";

	/**
	 * XML tag - {@value #ONFAILURE_TAG}
	 */
	public final static String ONFAILURE_TAG = "OnFailure";

	/**
	 * XML tag - {@value #IOSETTINGS_TAG}
	 */
	public final static String IOSETTINGS_TAG = "IOSettings";

	/**
	 * XML tag - {@value #SETTINGS_TAG}
	 */
	public final static String SETTINGS_TAG = "Settings";

	/**
	 * XML tag - {@value #OPTIONS_TAG}
	 */
	public final static String OPTIONS_TAG = "ThreadOptions";

	/**
	 * XML tag - {@value #OPERATIONS_TAG}
	 */
	public final static String OPERATIONS_TAG = "Operations";

	/**
	 * XML tag - {@value #OPERATION_TAG}
	 */
	public final static String OPERATION_TAG = "Operation";

	/**
	 * XML tag - {@value #OPERATION_PUBLIC_TAG}
	 */
	public final static String OPERATION_PUBLIC_TAG = "Public";

	/**
	 * XML tag - {@value #INIT_PARAMS_TAG}
	 */
	public final static String INIT_PARAMS_TAG = "InitParams";

	/**
	 * XML tag - {@value #CALLPARAM_TAG}
	 */
	public final static String CALLPARAM_TAG = "CallParam";

	/**
	 * IO setting - {@value #IOSETTINGS_INPUT}
	 */
	public final static String IOSETTINGS_INPUT = "InputParameters";

	/**
	 * IO setting - {@value #IOSETTINGS_OUTPUT}
	 */
	public final static String IOSETTINGS_OUTPUT = "OutputParameters";

	/**
	 * IO setting - {@value #IOSETTINGS_TARGETATTR}
	 */
	public final static String IOSETTINGS_TARGETATTR = "TargetAttribute";

	/**
	 * IO setting - {@value #IOSETTINGS_NULLBEHAVIOR}
	 */
	public final static String IOSETTINGS_NULLBEHAVIOR = "NullBehavior";

	/**
	 * IO setting - {@value #IOSETTINGS_NULLBEHAVIORVALUE}
	 */
	public final static String IOSETTINGS_NULLBEHAVIORVALUE = "NullBehaviorValue";

	/**
	 * IO setting - {@value #IOSETTINGS_SYNTAX}
	 */
	public final static String IOSETTINGS_SYNTAX = "Syntax";

	/**
	 * IO setting - {@value #IOSETTINGS_DEFAULT}
	 * <p>
	 * Note: Deprecated, only for backwards compatibility
	 */
	public final static String IOSETTINGS_DEFAULT = "DefaultValue";

	/**
	 * IO setting - {@value #IOSETTINGS_SYNTAX}
	 */
	public final static String IOSETTINGS_REQUIRED = "Required";

	/**
	 * IO setting - {@value #LOG_SETTINGS_TAG}
	 */
	public final static String LOG_SETTINGS_TAG = "Logging";

	/**
	 * IO setting - {@value #CALLRETURN_TAG}
	 */
	public final static String CALLRETURN_TAG = "CallReturn";

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * Parse a XML element into a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             in case the configuration object the provided XML element
	 *             represents is not recognized.
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		Trace.entrymax(this, "parse", config, elem);
		NodeList list;
		Element e;
		String str;
		AssemblyLineConfig assemblyLine = (AssemblyLineConfig) config;

		// Initialize config and set name
		assemblyLine.init();
		getBaseName(assemblyLine, elem);

		// Logging enabled
		if ((str = getNodeTextByName(elem, LOG_ENABLED_TAG)) != null)
			assemblyLine.setLogEnabled(Boolean.valueOf(str).booleanValue());

		// Add assemblyline settings
		if ((e = getSingleElement(elem, SETTINGS_TAG)) != null)
			getParameters(e, assemblyLine.getSettings());

		// Add connectors
		if ((e = getSingleElement(elem, CONNECTORS_TAG)) != null) {
			// list = e.getElementsByTagName ( ConnectorFactory.CONNECTOR_TAG );
			list = e.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				ConnectorConfig cc = null;
				if (list.item(i).getNodeName().equals(
						ConnectorFactory.CONNECTOR_TAG)) {
					cc = (ConnectorConfig) getImpl(ConnectorFactory.CONNECTOR_TAG);
					cc.setParent(assemblyLine);
					getFactory(ConnectorFactory.CONNECTOR_TAG).parse(cc,
							(Element) list.item(i));
				} else if (list.item(i).getNodeName().equals(
						FunctionFactory.FUNCTION_TAG)) {
					cc = (FunctionConfig) getImpl(FunctionFactory.FUNCTION_TAG);
					cc.setParent(assemblyLine);
					getFactory(FunctionFactory.FUNCTION_TAG).parse(cc,
							(Element) list.item(i));
				} else {
					continue;
				}
				cc.init();
				cc.setupInheritanceChain();
				assemblyLine.addComponent(cc);
			}
		}

		/*
		 * Migrate assemblyline IO settings
		 */
		if ((e = getSingleElement(elem, IOSETTINGS_TAG)) != null) {
			Element io = getSingleElement(e, IOSETTINGS_INPUT);
			if (io != null)
				migrateCallParam(assemblyLine, io, true);

			if ((io = getSingleElement(e, IOSETTINGS_OUTPUT)) != null)
				migrateCallParam(assemblyLine, io, false);
		}

		// Checkpoint config
		if ((e = getSingleElement(elem, CheckpointFactory.CHECKPOINT_TAG)) != null)
			getFactory(CheckpointFactory.CHECKPOINT_TAG).parse(
					assemblyLine.getCheckpointConfig(), e);

		// Sandbox config
		if ((e = getSingleElement(elem, SandboxFactory.SANDBOX_TAG)) != null)
			getFactory(SandboxFactory.SANDBOX_TAG).parse(
					assemblyLine.getSandboxConfig(), e);

		// Simulation config
		if ((e = getSingleElement(elem, SimulationFactory.SIMULATE_TAG)) != null)
			getFactory(SimulationFactory.SIMULATE_TAG).parse(
					assemblyLine.getSimulationConfig(), e);

		// Log settings
		if ((e = getSingleElement(elem, LoggingFactory.LOGGING_TAG)) != null)
			getFactory(LoggingFactory.LOGGING_TAG).parse(
					assemblyLine.getLogConfig(), e);

		// Components
		if ((e = getSingleElement(elem, ContainerFactory.CONTAINER_TAG)) != null)
			getFactory(ContainerFactory.CONTAINER_TAG).parse(
					assemblyLine.getEntryFeedComponents(), e);
		else if ((e = getSingleElement(elem, ContainerFactory.CONTAINER_TAG
				+ "EF")) != null)
			getFactory(ContainerFactory.CONTAINER_TAG).parse(
					assemblyLine.getEntryFeedComponents(), e);

		// DataFlow Components
		if ((e = getSingleElement(elem, ContainerFactory.CONTAINER_TAG + "DF")) != null)
			getFactory(ContainerFactory.CONTAINER_TAG).parse(
					assemblyLine.getDataFlowComponents(), e);

		// Thread options
		if ((e = getSingleElement(elem, OPTIONS_TAG)) != null)
			getFactory(PropertyFactory.PROPERTY_TAG).parse(
					assemblyLine.getThreadOptions(), e);

		// Call/return schema and attribute map
		if ((e = getSingleElement(elem, CALLRETURN_TAG)) != null) {
			// Schemas
			getSchemas(e, assemblyLine);

			// Attribute maps
			getAttributeMaps(e, assemblyLine);
		}

		// AL Operations
		if ((e = getSingleElement(elem, OPERATIONS_TAG)) != null) {
			getOperations(e, assemblyLine);
		}

		// AL Init Params
		getInitParams(elem, assemblyLine);

		// Move default into operations container
		if (assemblyLine.getSchema(true) != null
				&& assemblyLine.getOperations().size() == 0) {
			e = getSingleElement(elem, CALLRETURN_TAG);
			if (e != null)
				getOperation(e, assemblyLine, "Default");
			// assemblyLine.getOperations().getConfig(0).setName("Default");
		}

		// AssemblyLine Hooks
		if ((e = getSingleElement(elem, HookFactory.HOOK_TAG)) != null)
			getFactory(HookFactory.HOOK_TAG).parse(assemblyLine.getHooks(), e);

		// Null Behavior and Definitions
		getNullBehavior(elem, assemblyLine);

		// String version =
		// ((MetamergeConfigXML)config.getMetamergeConfig()).getConfigVersion();
		// if ( version.equals("1.0") ) {
		// Migrate old style prolog and epilog scripts
		migrateHook(assemblyLine, elem, PROLOG_TAG, InternalSchema.AL_PROLOG);
		migrateHook(assemblyLine, elem, PROLOGINIT_TAG,
				InternalSchema.AL_PROLOG_INIT);
		migrateHook(assemblyLine, elem, EPILOG_TAG, InternalSchema.AL_EPILOG);
		migrateHook(assemblyLine, elem, EPILOG2_TAG, InternalSchema.AL_EPILOG2);
		migrateHook(assemblyLine, elem, SHUTDOWN_TAG,
				InternalSchema.AL_SHUTDOWN);
		migrateHook(assemblyLine, elem, STARTCYCLE_TAG,
				InternalSchema.AL_STARTCYCLE);
		migrateHook(assemblyLine, elem, ONSUCCESS_TAG,
				InternalSchema.AL_ONSUCCESS);
		migrateHook(assemblyLine, elem, ONFAILURE_TAG,
				InternalSchema.AL_ONFAILURE);
		// }

		Trace.exitmax(this, "parse");
	}

	/**
	 * This method migrates old style prolog and epilog scripts.
	 * 
	 * @param al
	 *            AssemblyLineConfig object
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @param tag
	 *            tag name
	 * @param name
	 *            name of the hook
	 * @throws Exception
	 *             if error occurs
	 */
	private void migrateHook(AssemblyLineConfig al, Element elem, String tag,
			String name) throws Exception {
		Trace.entrymax(this, "migratehook");
		String str = getNodeTextByName(elem, tag);
		if (str != null && str.length() > 0) {
			HooksConfig hs = al.getHooks();
			HookConfig h = hs.getHook(name);
			h.setScript(str);
			h.setEnabled(true);
			h.setDebugBreak(true);
			hs.setHook(h);
		}
		Trace.exitmax(this, "migrateHook");
	}

	/**
	 * Get operations. Calls getOperation for every Operation element in the
	 * config.
	 * 
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @param alc
	 *            OperationsConfig object
	 * @throws Exception
	 *             if error occurs
	 */
	public void getOperations(Element elem, OperationsConfig alc)
			throws Exception {
		NodeList list = elem.getElementsByTagName(OPERATION_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			getOperation((Element) list.item(i), alc, null);
		}
	}

	/**
	 * 
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @param alc
	 *            OperationsConfig object
	 * @param forceName
	 *            if not <code>null</code> sets the Operation name; otherwise
	 *            the Operation name is retrieved from its 'name' tag;
	 * @throws Exception
	 */
	public void getOperation(Element elem, OperationsConfig alc,
			String forceName) throws Exception {
		Trace.entrymax(this, "getOperation");

		String name;
		if (forceName != null)
			name = forceName;
		else
			name = elem.getAttribute(NAME_ATTRIBUTE);

		OperationConfig oc = alc.createOperation(name);
		Element e;
		String str;

		NodeList list = elem.getElementsByTagName(SchemaFactory.SCHEMA_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			name = e.getAttribute(NAME_ATTRIBUTE);
			getFactory(SchemaFactory.SCHEMA_TAG).parse(
					oc.getSchema(name.equals("Input")), e);
		}

		list = elem.getElementsByTagName(ConnectorFactory.ATTRIBUTE_MAP);
		for (int i = 0; i < list.getLength(); i++) {
			e = (Element) list.item(i);
			name = e.getAttribute(NAME_ATTRIBUTE);
			((ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG))
					.getAttributeMap(e, oc
							.getAttributeMap(name.equals("Input")));
		}

		if ((str = getNodeTextByName(elem, OPERATION_PUBLIC_TAG)) != null)
			oc.setPublic(Boolean.valueOf(str).booleanValue());

		getNullBehavior(elem, oc);

		Trace.exitmax(this, "getOperation");
	}

	/**
	 * This method converts old style and reads the new style initialization
	 * parameters.
	 * 
	 * @param elem
	 *            Element object
	 * @param al
	 *            OperationsConfig object
	 * @throws Exception
	 *             <li>if could not locate single Element from <code>elem</code>
	 *             's children.</li> <br>
	 *             <li>if could not publish parameters.</li> <br>
	 *             <li>if could not parse XML file into a configuration object.</li>
	 */
	public void getInitParams(Element elem, OperationsConfig al)
			throws Exception {
		Trace.entrymax(this, "getInitParams", elem, al);
		// Convert old style Init params
		OperationConfig oc = al.getOperation(OperationConfig.INIT_OPERATION);
		if (oc != null) {
			al.setPublishedInitParams(oc.getSchema(true));
			al.getOperations().removeConfig(oc);
		}

		// Read in new style Init params
		Element e = getSingleElement(elem, INIT_PARAMS_TAG);
		if (e != null)
			e = getSingleElement(e, SchemaFactory.SCHEMA_TAG);
		if (e != null)
			getFactory(SchemaFactory.SCHEMA_TAG).parse(
					al.getPublishedInitParams(), e);
		Trace.exitmax(this, "getInitParams");
	}

	/**
	 * This method parses all Schema elements in the XML file into configuration
	 * objects.
	 * 
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link AssemblyLineConfig} object.
	 * @param config
	 *            an instance of the {@link AssemblyLineConfig} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @throws Exception
	 *             if could not parse the XML file into a configuration object
	 */
	public void getSchemas(Element elem, AssemblyLineConfig config)
			throws Exception {
		Trace.entrymax(this, "getSchemas");
		NodeList list = elem.getElementsByTagName(SchemaFactory.SCHEMA_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute(NAME_ATTRIBUTE);
			getFactory(SchemaFactory.SCHEMA_TAG).parse(config.getSchema(name),
					e);
		}
		Trace.exitmax(this, "getSchemas");
	}

	/**
	 * This method parses all AttributeMap elements in the XML file into
	 * configuration objects.
	 * 
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link AssemblyLineConfig} object.
	 * @param config
	 *            an instance of the {@link AssemblyLineConfig} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @throws Exception
	 *             if could not parse the XML file into a configuration object
	 */
	public void getAttributeMaps(Element elem, AssemblyLineConfig config)
			throws Exception {
		Trace.entrymax(this, "getAttributeMaps");
		NodeList list = elem
				.getElementsByTagName(ConnectorFactory.ATTRIBUTE_MAP);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute(NAME_ATTRIBUTE);
			((ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG))
					.getAttributeMap(e, config.getAttributeMap(name));
		}
		Trace.exitmax(this, "getAttributeMaps");
	}

	/**
	 * Reads and converts the old style call parameters to the new style.
	 * 
	 * @param assemblyLine
	 *            an instance of the {@link AssemblyLineConfig} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link AssemblyLineConfig} object.
	 * @param input
	 *            if <code>true</code> add call parameters to input attribute
	 *            map item, else to the output
	 * @throws Exception
	 */
	public void migrateCallParam(AssemblyLineConfig assemblyLine, Element elem,
			boolean input) throws Exception {
		Trace.entrymax(this, "migrateCallParam");
		AttributeMapConfig amc = assemblyLine.getAttributeMap(input);
		SchemaConfig schema = assemblyLine.getSchema(input);

		// old style
		CallConfig cc = new com.ibm.di.config.base.CallConfigImpl();
		getCallParam(cc, elem);

		// convert to new style
		java.util.List params = cc.getCallParameters();
		for (int i = 0; i < params.size(); i++) {
			String attributeName;
			String schemaName;
			CallParamConfig cpc = cc.getCallParameter(params.get(i));

			if (input) {
				attributeName = cpc.getTargetAttributeName();
				schemaName = params.get(i).toString();
			} else {
				schemaName = cpc.getTargetAttributeName();
				attributeName = params.get(i).toString();
			}

			if (amc.hasAttributeMapItem(attributeName)) {
				logger
						.info(sResHash
								.getString(
										"MMCONFIG.ALFACTORY.FOUND.EXISTING.ATTRIBUTE.WHILE.MIGRATING",
										attributeName));
			} else {
				AttributeMapItem ami = amc.newAttributeMapItem(attributeName);
				ami.setSimple(params.get(i).toString());
				ami.setNullBehavior(cpc.getNullBehavior());
				ami.setNullBehaviorValue(cpc.getNullBehaviorValue());

				schema.newItem(schemaName).setExternalSyntax(cpc.getSyntax());
			}
		}
		Trace.exitmax(this, "migrateCallParam");
	}

	/**
	 * 
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link AssemblyLineConfig} object.
	 * @param config
	 *            an instance of the {@link AssemblyLineConfig} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @throws Exception
	 *             if could not return some text from a node's children
	 */
	public void getCallParam(CallConfig config, Element elem) throws Exception {
		Trace.entrymax(this, "getCallParam");
		NodeList list = elem.getElementsByTagName(CALLPARAM_TAG);
		String str = null;

		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			str = getNodeTextByName(e, NAME_TAG);
			if (str == null) {
				throw new Exception(sResHash
						.getString("MMCONFIG.ALFACTORY.NO.NAME.FOR.CALLPARAM"));
			}

			CallParamConfig cpc = config.newCallParameter(str);
			if ((str = getNodeTextByName(e, IOSETTINGS_TARGETATTR)) != null)
				cpc.setTargetAttributeName(str);

			// Backwards compatibility
			if ((str = getNodeTextByName(e, IOSETTINGS_DEFAULT)) != null) {
				cpc.setNullBehavior("Value");
				cpc.setNullBehaviorValue(str);
			}
			// Backwards compatibility
			if ((str = getNodeTextByName(e, IOSETTINGS_REQUIRED)) != null
					&& str.equalsIgnoreCase("true"))
				cpc.setNullBehavior("Error");

			if ((str = getNodeTextByName(e, IOSETTINGS_NULLBEHAVIOR)) != null)
				cpc.setNullBehavior(str);
			if ((str = getNodeTextByName(e, IOSETTINGS_NULLBEHAVIORVALUE)) != null)
				cpc.setNullBehaviorValue(str);
			if ((str = getNodeTextByName(e, IOSETTINGS_SYNTAX)) != null)
				cpc.setSyntax(str);
		}
		Trace.exitmax(this, "getCallParam");
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		Trace.entrymax(this, "build");
		AssemblyLineConfig assemblyLine = (AssemblyLineConfig) config;

		logmsg(sResHash.getString("MMCONFIG.ALFACTORY.BUILD.START",
				assemblyLine.getName()));

		// Name
		setBaseName(assemblyLine, elem);

		// Logging enabled
		setSingleElement(elem, LOG_ENABLED_TAG, assemblyLine,
				InternalSchema.LOG_ENABLED);

		// Settings
		setParameters(elem, assemblyLine.getSettings(), SETTINGS_TAG);

		// -- Hooks
		getFactory(HookFactory.HOOK_TAG).build(assemblyLine.getHooks(), elem);

		Element e;
		// Checkpoint config
		e = elem.getOwnerDocument().createElement(
				CheckpointFactory.CHECKPOINT_TAG);
		elem.appendChild(e);
		getFactory(CheckpointFactory.CHECKPOINT_TAG).build(
				assemblyLine.getCheckpointConfig(), e);

		// Sandbox config
		e = elem.getOwnerDocument().createElement(SandboxFactory.SANDBOX_TAG);
		elem.appendChild(e);
		getFactory(SandboxFactory.SANDBOX_TAG).build(
				assemblyLine.getSandboxConfig(), e);

		// Simulation config
		e = elem.getOwnerDocument().createElement(
				SimulationFactory.SIMULATE_TAG);
		elem.appendChild(e);
		getFactory(SimulationFactory.SIMULATE_TAG).build(
				assemblyLine.getSimulationConfig(), e);

		// Log settings
		e = elem.getOwnerDocument().createElement(LoggingFactory.LOGGING_TAG);
		elem.appendChild(e);
		getFactory(LoggingFactory.LOGGING_TAG).build(
				assemblyLine.getLogConfig(), e);

		// EntryFeed components
		e = elem.getOwnerDocument().createElement(
				ContainerFactory.CONTAINER_TAG + "EF");
		elem.appendChild(e);
		getFactory(ContainerFactory.CONTAINER_TAG).build(
				assemblyLine.getEntryFeedComponents(), e);

		// DataFlow Components
		e = elem.getOwnerDocument().createElement(
				ContainerFactory.CONTAINER_TAG + "DF");
		elem.appendChild(e);
		getFactory(ContainerFactory.CONTAINER_TAG).build(
				assemblyLine.getDataFlowComponents(), e);

		// Thread Options
		e = elem.getOwnerDocument().createElement(OPTIONS_TAG);
		elem.appendChild(e);
		getFactory(PropertyFactory.PROPERTY_TAG).build(
				assemblyLine.getThreadOptions(), e);

		// AL Operations
		e = elem.getOwnerDocument().createElement(OPERATIONS_TAG);
		elem.appendChild(e);
		setOperations(e, assemblyLine);

		// AL Init Params
		setInitParams(elem, assemblyLine);

		// Null Behavior and Definitions
		setNullBehavior(assemblyLine, elem);

		logmsg(sResHash.getString("MMCONFIG.ALFACTORY.BUILD.END"));
		Trace.exitmax(this, "build");
	}

	/**
	 * Generates a XML element from the provided operation configuration and add
	 * it to the Input and Output Attribute Map of the connector.
	 * 
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @param alc
	 *            an instance of the {@link OperationsConfig} class to set
	 * @throws Exception
	 */
	public void setOperations(Element elem, OperationsConfig alc)
			throws Exception {
		Trace.entrymax(this, "setOperations");
		ContainerConfig cc = alc.getOperations();
		Element child;
		for (int i = 0; i < cc.size(); i++) {
			OperationConfig oc = alc.getOperation(cc.getConfig(i)
					.getShortName());
			child = elem.getOwnerDocument().createElement(OPERATION_TAG);
			elem.appendChild(child);
			setBaseName(oc, child);

			getFactory(SchemaFactory.SCHEMA_TAG).build(oc.getSchema(true),
					child);
			getFactory(SchemaFactory.SCHEMA_TAG).build(oc.getSchema(false),
					child);

			((ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG))
					.setAttributeMap(oc.getAttributeMap(true), child);
			((ConnectorFactory) getFactory(ConnectorFactory.CONNECTOR_TAG))
					.setAttributeMap(oc.getAttributeMap(false), child);

			setSingleElement(child, OPERATION_PUBLIC_TAG, oc, "public");
			setNullBehavior(oc, child);
		}

		Trace.exitmax(this, "setOperations");
	}

	/**
	 * Generates a XML element from the init params of the provided operation
	 * configuration and add the newly created element to the it to the Input
	 * and Output Attribute Map of the connector.
	 * 
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @param alc
	 *            an instance of the {@link OperationsConfig} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @throws Exception
	 */
	public void setInitParams(Element elem, OperationsConfig alc)
			throws Exception {
		Trace.entrymax(this, "setInitParams");
		Element e = elem.getOwnerDocument().createElement(INIT_PARAMS_TAG);
		elem.appendChild(e);
		getFactory(SchemaFactory.SCHEMA_TAG).build(
				alc.getPublishedInitParams(), e);
		Trace.exitmax(this, "setInitParams");

	}

}
