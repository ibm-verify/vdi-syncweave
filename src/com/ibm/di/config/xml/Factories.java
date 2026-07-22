/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import java.util.Arrays;
import java.util.Hashtable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.ExposedProperty;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LogConfigItem;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.config.interfaces.SolutionInterface;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * General class used to create a configuration object representing parsed XML
 * file.
 * <p>
 * This class is inherited by all factories for specific components -
 * AssemblyLineFactory, FunctionFactory, HookFactory etc.
 */
public class Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Logger
	 */
	public final static Log logger = new Log("mmconfig",
			"com.ibm.di.config.xml.MetamergeConfigXML");

	/**
	 * Tags / class mapping
	 */
	public final static Hashtable<String, Factories> classMap = new Hashtable<String, Factories>();

	/**
	 * Hashtable containig the mapping between the XML tag names and the
	 * corresponding factory classes.
	 */
	public final static Hashtable<String, String> implMap = new Hashtable<String, String>();

	/**
	 * General tag - {@value #PARAMETER_TAG}
	 */
	public final static String PARAMETER_TAG = "parameter";

	/**
	 * General tag - {@value #NAME_TAG}
	 */
	public final static String NAME_TAG = "Name";

	/**
	 * General tag - {@value #INHERIT_TAG}
	 */
	public final static String INHERIT_TAG = "InheritFrom";

	/**
	 * General attribute - {@value #NAME_ATTRIBUTE}
	 */
	public final static String NAME_ATTRIBUTE = "name";

	/**
	 * General attribute - {@value #VALUE_ATTRIBUTE}
	 */
	public final static String VALUE_ATTRIBUTE = "value";

	/**
	 * General attribute - {@value #USER_COMMENT_ATTRIBUTE}
	 */
	public final static String USER_COMMENT_ATTRIBUTE = "UserComment";

	/**
	 * General attribute - {@value #NULLBEHAVIOR}
	 */
	public final static String NULLBEHAVIOR = "NullBehavior";

	/**
	 * General attribute - {@value #NULLBEHAVIORVALUE}
	 */
	public final static String NULLBEHAVIORVALUE = "NullBehaviorValue";

	/**
	 * General attribute - {@value #NULLDEFINITION}
	 */
	public final static String NULLDEFINITION = "NullDefinition";

	/**
	 * General attribute - {@value #NULLDEFINITIONVALUE}
	 */
	public final static String NULLDEFINITIONVALUE = "NullDefinitionValue";
	
	/**
	 * Modification time stamp
	 */
	public final static String MOD_TS_TAG = "ModTime";
	
	/**
	 * Tag to mark parameter values as encrypted
	 */
	private final static String ENCRYPTED = "encrypted";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * Parent of this factory.
	 */
	public Factories parent;

	/**
	 * This array contains old names of renamed files.
	 */
	private final static String[] oldNames = {
			"system:/Connectors/ibmdi.PESConnector",
			"system:/Connectors/ibmdi.DSMLv2Connector" };

	/**
	 * This array contains the new names for files in <code>oldNames</code>
	 * array.
	 */
	private final static String[] newNames = {
			"system:/Connectors/ibmdi.SystemStoreConnector",
			"system:/Connectors/ibmdi.TIMDSMLv2Connector", };

	static {
		classMap.put(AssemblyLineFactory.ASSEMBLYLINE_TAG,
				new com.ibm.di.config.xml.AssemblyLineFactory());
		classMap.put(ConnectorFactory.CONNECTOR_TAG,
				new com.ibm.di.config.xml.ConnectorFactory());
		classMap.put(ParserFactory.PARSER_TAG,
				new com.ibm.di.config.xml.ParserFactory());
		classMap.put(ScriptFactory.SCRIPT_TAG,
				new com.ibm.di.config.xml.ScriptFactory());
		classMap.put(PropertyFactory.PROPERTY_TAG,
				new com.ibm.di.config.xml.PropertyFactory());
		classMap.put(LibraryFactory.LIBRARY_TAG,
				new com.ibm.di.config.xml.LibraryFactory());
		classMap.put(FolderFactory.FOLDER_TAG,
				new com.ibm.di.config.xml.FolderFactory());
		classMap.put(SchemaFactory.SCHEMA_TAG,
				new com.ibm.di.config.xml.SchemaFactory());
		classMap.put(NamespaceFactory.NAMESPACE_TAG,
				new com.ibm.di.config.xml.NamespaceFactory());
		classMap.put(HookFactory.HOOK_TAG,
				new com.ibm.di.config.xml.HookFactory());
		classMap.put(ExtPropFactory.EXTPROP_TAG,
				new com.ibm.di.config.xml.ExtPropFactory());
		classMap.put(CheckpointFactory.CHECKPOINT_TAG,
				new com.ibm.di.config.xml.CheckpointFactory());
		classMap.put(SandboxFactory.SANDBOX_TAG,
				new com.ibm.di.config.xml.SandboxFactory());
		classMap.put(LoggingFactory.LOGGING_TAG,
				new com.ibm.di.config.xml.LoggingFactory());
		classMap.put(FunctionFactory.FUNCTION_TAG,
				new com.ibm.di.config.xml.FunctionFactory());
		classMap.put(ContainerFactory.CONTAINER_TAG,
				new com.ibm.di.config.xml.ContainerFactory());
		classMap.put(BranchingFactory.BRANCH_TAG,
				new com.ibm.di.config.xml.BranchingFactory());
		classMap.put(BranchingFactory.BRANCH_CONDITION_TAG,
				new com.ibm.di.config.xml.BranchingFactory());
		classMap.put(LoopFactory.LOOP_TAG,
				new com.ibm.di.config.xml.LoopFactory());
		classMap.put(ALMappingFactory.ALMAPPING_TAG,
				new com.ibm.di.config.xml.ALMappingFactory());
		classMap.put(InstanceFactory.INSTANCE_TAG,
				new com.ibm.di.config.xml.InstanceFactory());
		classMap.put(TombstonesFactory.TOMBSTONES_TAG,
				new com.ibm.di.config.xml.TombstonesFactory());
		classMap.put(PropertyStoreFactory.PROPERTIES_TAG,
				new com.ibm.di.config.xml.PropertyStoreFactory());
		classMap.put(SolutionInterfaceFactory.SOLUTION_INTERFACE_TAG,
				new com.ibm.di.config.xml.SolutionInterfaceFactory());
		classMap.put(SolutionInterfaceFactory.EXPOSED_PROPERTY_TAG,
				new com.ibm.di.config.xml.SolutionInterfaceFactory());
		classMap.put(SimulationFactory.SIMULATE_TAG,
				new com.ibm.di.config.xml.SimulationFactory());
		classMap.put(FormFactory.FORM_TAG,
				new com.ibm.di.config.xml.FormFactory());
		classMap.put(LogItemFactory.LOGGER_TAG,
				new com.ibm.di.config.xml.LogItemFactory());
		classMap.put(ReconnectFactory.RECONNECT_TAG,
				new com.ibm.di.config.xml.ReconnectFactory());
		classMap.put(SchedulerFactory.SCHEDULER_TAG,
				new com.ibm.di.config.xml.SchedulerFactory());
		classMap.put(SequenceFactory.SEQUENCE_TAG,
				new com.ibm.di.config.xml.SequenceFactory());

		implMap.put(ConnectorFactory.CONNECTOR_TAG,
				"com.ibm.di.config.base.ConnectorConfigImpl");
		implMap.put(AssemblyLineFactory.ASSEMBLYLINE_TAG,
				"com.ibm.di.config.base.AssemblyLineConfigImpl");
		implMap.put(ParserFactory.PARSER_TAG,
				"com.ibm.di.config.base.ParserConfigImpl");
		implMap.put(ScriptFactory.SCRIPT_TAG,
				"com.ibm.di.config.base.ScriptConfigImpl");
		implMap.put(LibraryFactory.LIBRARY_TAG,
				"com.ibm.di.config.base.LibraryConfigImpl");
		implMap.put(PropertyFactory.PROPERTY_TAG,
				"com.ibm.di.config.base.PropertyConfigImpl");
		implMap.put(FolderFactory.FOLDER_TAG,
				"com.ibm.di.config.base.MetamergeFolderImpl");
		implMap.put(SchemaFactory.SCHEMA_TAG,
				"com.ibm.di.config.base.ConnectorSchemaConfigImpl");
		implMap.put(NamespaceFactory.NAMESPACE_TAG,
				"com.ibm.di.config.base.NamespaceConfigImpl");
		implMap.put(ExtPropFactory.EXTPROP_TAG,
				"com.ibm.di.config.base.ExternalPropertiesImpl");
		implMap.put(LoggingFactory.LOGGING_TAG,
				"com.ibm.di.config.base.LogConfigImpl");
		implMap.put(FunctionFactory.FUNCTION_TAG,
				"com.ibm.di.config.base.FunctionConfigImpl");
		implMap.put(ContainerFactory.CONTAINER_TAG,
				"com.ibm.di.config.base.ContainerConfigImpl");
		implMap.put(BranchingFactory.BRANCH_TAG,
				"com.ibm.di.config.base.BranchingConfigImpl");
		implMap.put(BranchingFactory.BRANCH_CONDITION_TAG,
				"com.ibm.di.config.base.BranchConditionImpl");
		implMap.put(LoopFactory.LOOP_TAG,
				"com.ibm.di.config.base.LoopConfigImpl");
		implMap.put(ALMappingFactory.ALMAPPING_TAG,
				"com.ibm.di.config.base.ALMappingConfigImpl");
		implMap.put(InstanceFactory.INSTANCE_TAG,
				"com.ibm.di.config.base.InstanceConfigImpl");
		implMap.put(TombstonesFactory.TOMBSTONES_TAG,
				"com.ibm.di.config.base.TombstonesConfigImpl");
		implMap.put(PropertyStoreFactory.PROPERTIES_TAG,
				"com.ibm.di.config.base.PropertyManagerImpl");
		implMap.put(SolutionInterfaceFactory.SOLUTION_INTERFACE_TAG,
				"com.ibm.di.config.base.SolutionInterfaceImpl");
		implMap.put(SolutionInterfaceFactory.EXPOSED_PROPERTY_TAG,
				"com.ibm.di.config.base.ExposedPropertyImpl");
		implMap.put(SimulationFactory.SIMULATE_TAG,
				"com.ibm.di.config.base.SimulationConfigImpl");
		implMap.put(FormFactory.FORM_TAG,
				"com.ibm.di.config.base.FormConfigImpl");
		implMap.put(LogItemFactory.LOGGER_TAG,
				"com.ibm.di.config.base.LogConfigItemImpl");
		implMap.put(ReconnectFactory.RECONNECT_TAG,
				"com.ibm.di.config.base.ReconnectConfigImpl");
		implMap.put(SchedulerFactory.SCHEDULER_TAG,
				"com.ibm.di.config.base.SchedulerConfigImpl");
		implMap.put(SequenceFactory.SEQUENCE_TAG,
			"com.ibm.di.config.base.SequenceConfigImpl");
	}

	/**
	 * @param tag
	 *            XML tag name
	 * @return factory for the specified XML element
	 * 
	 * @exception Exception
	 *                no mapping for such XML tag exist
	 */
	public static Factories getFactory(String tag) throws Exception {
		Factories f = classMap.get(tag);
		if (f == null) {
			throw new Exception(sResHash.getString(
					"MMCONFIG.FACTORIES.NO.XML.FACTORY.DEFINED.FOR.TAG", tag));
		} else
			return f;
	}

	/**
	 * @param tag
	 *            XML tag name
	 * @return new instance of configuration implementation
	 * 
	 * @throws Exception
	 *             no mapping for such XML tag exist
	 */
	public static BaseConfiguration getImpl(String tag) throws Exception {
		String cls = implMap.get(tag);

		if (isDebugMode()) {
			debug(sResHash.getString("MMCONFIG.FACTORIES.GETIMPL.TAG",
					new Object[] { tag, cls }));
		}

		if (cls == null) {
			throw new Exception(
					sResHash.getString(
									"MMCONFIG.FACTORIES.NO.CONFIGURATION.IMPLEMENTATION.DEFINED.FOR.TAG",
									tag));
		}

		return (BaseConfiguration) Class.forName(cls).newInstance();
	}

	/**
	 * @param config
	 *            Factory object
	 * @return XML tag name used for the specified class
	 */
	public static String getClassTag(Object config) {
		if (config instanceof AssemblyLineConfig)
			return AssemblyLineFactory.ASSEMBLYLINE_TAG;

		// Note: FunctionConfig subclasses ConnectorConfig and must precede
		// Connectorconfig test
		else if (config instanceof FunctionConfig)
			return FunctionFactory.FUNCTION_TAG;

		// Note: ALMappingConfig subclasses ConnectorConfig and must precede
		// Connectorconfig test
		else if (config instanceof ALMappingConfig)
			return ALMappingFactory.ALMAPPING_TAG;

		else if (config instanceof ConnectorConfig)
			return ConnectorFactory.CONNECTOR_TAG;

		else if (config instanceof ParserConfig)
			return ParserFactory.PARSER_TAG;

		else if (config instanceof LibraryConfig)
			return LibraryFactory.LIBRARY_TAG;

		else if (config instanceof PropertyConfig)
			return PropertyFactory.PROPERTY_TAG;

		else if (config instanceof PropertyManager)
			return PropertyStoreFactory.PROPERTIES_TAG;

		else if (config instanceof MetamergeFolder)
			return FolderFactory.FOLDER_TAG;

		else if (config instanceof NamespaceConfig)
			return NamespaceFactory.NAMESPACE_TAG;

		else if (config instanceof ScriptConfig)
			return ScriptFactory.SCRIPT_TAG;

		else if (config instanceof ExternalPropertiesConfig)
			return ExtPropFactory.EXTPROP_TAG;

		else if (config instanceof LogConfig)
			return LoggingFactory.LOGGING_TAG;

		else if (config instanceof LoopConfig)
			return LoopFactory.LOOP_TAG;

		else if (config instanceof BranchingConfig)
			return BranchingFactory.BRANCH_TAG;

		// Note: InstanceConfig subclasses ContainerConfig and must precede
		// ContainerConfig test
		else if (config instanceof InstanceConfig)
			return InstanceFactory.INSTANCE_TAG;

		else if (config instanceof TombstonesConfig)
			return TombstonesFactory.TOMBSTONES_TAG;

		else if (config instanceof SolutionInterface)
			return SolutionInterfaceFactory.SOLUTION_INTERFACE_TAG;

		else if (config instanceof ExposedProperty)
			return SolutionInterfaceFactory.EXPOSED_PROPERTY_TAG;

		else if (config instanceof BranchCondition)
			return BranchingFactory.BRANCH_CONDITION_TAG;

		else if (config instanceof FormConfig)
			return FormFactory.FORM_TAG;

		else if (config instanceof LogConfigItem)
			return LogItemFactory.LOGGER_TAG;

		else if (config instanceof SchedulerConfig)
			return SchedulerFactory.SCHEDULER_TAG;

		else if (config instanceof SequenceConfig)
			return SequenceFactory.SEQUENCE_TAG;

		else if (config instanceof ContainerConfig)
			return ContainerFactory.CONTAINER_TAG;

		else
			return null;

	}

	/**
	 * 
	 * @return true if debug is enabled. Otherwise, false is returned.
	 */
	public static boolean isDebugMode() {
		return logger.isDebugEnabled();
	}

	/**
	 * Parse a XML element into a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param element
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             in case the configuration object the provided XML element
	 *             represents is not recognized.
	 */
	public void parse(BaseConfiguration config, Element element)
			throws Exception {
		getFactory(element.getTagName()).parse(config, element);
	}

	/**
	 * Generate a XML element from a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will be represented as a XML sub-tree.
	 * @param element
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @throws Exception
	 *             in case the tag name of the provided XML element is not
	 *             recognized as a valid name which is used for configuration
	 *             object representation as a XML.
	 * 
	 */
	public void build(BaseConfiguration config, Element element)
			throws Exception {
		getFactory(element.getTagName()).build(config, element);
	}

	/**
	 * Prints debug message.
	 * 
	 * @param msg
	 *            text of the message
	 */
	public static void debug(String msg) {
		logger.debug(msg);
	}

	/**
	 * Logs message.
	 * 
	 * @param msg
	 *            text of the message.
	 */
	public static void logmsg(String msg) {
		logger.info(msg);
	}

	/**
	 * List information for this factory.
	 * 
	 * @param config
	 *            configuration object
	 */
	public static void dump(BaseConfiguration config) {
		if (isDebugMode()) {
			debug(sResHash.getString("MMCONFIG.FACTORIES.DUMP"));
		}

		for (String key : config.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (isDebugMode()) {
				debug(sResHash.getString("MMCONFIG.FACTORIES.LISTING",
						new Object[] { key, config.getParameter(key) }));
			}
		}
	}

	/**
	 * This method retries the name and inheritFrom attributes/elements and
	 * configures the base object with its name and also calls MetamergeConfig
	 * for any inherited objects.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             if could not return text from node's children
	 */
	public void getBaseName(BaseConfiguration config, Element elem)
			throws Exception {
		String str = elem.getAttribute(NAME_ATTRIBUTE);
		if (str == null || str.length() == 0)
			str = getNodeTextByName(elem, NAME_TAG);

		if (str != null && str.length() > 0 && config.getName() == null)
			config.setName(MetamergeConfigFactory.parseName(str));

		// User comment
		str = getNodeTextByName(elem, USER_COMMENT_ATTRIBUTE);
		if (str != null && str.length() > 0)
			config.setUserComment(str);

		getInheritsFrom(config, elem);
		
		// Modification time stamp
		getModTS(config, elem);
	}

	void getModTS(BaseConfiguration config, Element elem) throws Exception {
		String str = getNodeTextByName(elem, MOD_TS_TAG);

		if (str == null || str.length() == 0)
			return;

		config.setModTS(Long.parseLong(str));
	}

	/**
	 * This method retries the inheritsFrom element and configures the base
	 * object with the retrieved information.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             if could not return text from node's children
	 */
	public void getInheritsFrom(BaseConfiguration config, Element elem)
			throws Exception {
		String str = getNodeTextByName(elem, INHERIT_TAG);

		if (str == null || str.length() == 0)
			return;

		int i = Arrays.asList(oldNames).indexOf(str);
		if (i != -1) {
			logger.info(sResHash.getString("MMCONFIG.HAS.BEEN.RENAMED",
					new Object[] { str, newNames[i] }));
			str = newNames[i];
		}

		config.setInheritsFromRef(str);
	}

	/**
	 * Attaches to the XML tree a single element representing the user comment.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @throws Exception
	 *             if could not return text from node's children
	 */
	public void setBaseName(BaseConfiguration config, Element elem)
			throws Exception {
		String name = config.getShortName();
		if (name != null)
			elem.setAttribute(NAME_ATTRIBUTE, name);

		setSingleElement(elem, USER_COMMENT_ATTRIBUTE, config,
				InternalSchema.USER_COMMENT);

		setInheritsFrom(config, elem);
		
		setModTS(config, elem);
	}

	private void setModTS(BaseConfiguration config, Element elem) throws Exception {
		long l = config.getModTS();
		if (l != 0)
			setSingleElement(elem, MOD_TS_TAG, String.valueOf(l));
	}

	/**
	 * Set the inheritFrom if it exists.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @throws Exception
	 */
	public void setInheritsFrom(BaseConfiguration config, Element elem)
			throws Exception {
		Object o = config.getParameterRaw(InternalSchema.INHERITS_FROM);
		if (o != null )
			setSingleElement(elem, INHERIT_TAG, o.toString() );
	}

	/**
	 * Add simple parameters to configuration.
	 * 
	 * @param p
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class
	 * @throws Exception
	 */
	public void getParameters(Element p, BaseConfiguration config)
			throws Exception {

		String name = p.getAttribute(NAME_ATTRIBUTE);
		if (name.length() > 0 && config.getName() == null)
			config.setName(name);

		NodeList list = p.getElementsByTagName(PARAMETER_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			if (e.getParentNode() == p)
				getParameter(e, config);
		}
	}

	/**
	 * Add simple parameter to configuration.
	 * 
	 * @param p
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class
	 * @throws Exception
	 */
	public void getParameter(Element p, BaseConfiguration config)
			throws Exception {
		String value = getNodeText(p);
		String name = p.getAttribute(NAME_ATTRIBUTE);
		if (value != null && Boolean.valueOf(p.getAttribute(ENCRYPTED))) {
			try {
				byte[] data = UserFunctions.base64Decode(value); 
				value = new String(CryptoUtils.decryptWithServerKey(data), "UTF-8");				
			} catch (Exception e) {
				if (logger != null)
					logger.warn("Unable to decrypt parameter " + name + " in " + config.getPath());
				value = "";
			}
			config.setProtectedParameter(name);
		}
		config.setParameter(name, value, false);
	}

	/**
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param p
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @param tag
	 *            XML tag name to be created
	 * @return Element containing the simple parameters
	 * @throws Exception
	 */
	public Element setParameters(Element p, BaseConfiguration config, String tag)
			throws Exception {
		Element param;
		if (tag != null) {
			param = p.getOwnerDocument().createElement(tag);
			if (config.getShortName() != null)
				param.setAttribute(NAME_ATTRIBUTE, config.getShortName());
		} else {
			param = p;
		}

		for (String key : config.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (key.equals(InternalSchema.INHERITS_FROM) ||
				key.equals(InternalSchema.PROTECTED_PARAMETERS)) {
				continue;
			}
			if (tag == null && key.equals(InternalSchema.USER_COMMENT) ) {
				continue;
			}
			setParameter(param, config, key);
		}
		if (tag != null)
			p.appendChild(param);
		return param;
	}

	/**
	 * Generates XML element for parameter, initialize it and attach it to the
	 * <code>p</code> object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param p
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @param pname
	 * @throws Exception
	 */
	public void setParameter(Element p, BaseConfiguration config, Object pname)
			throws Exception {
		Element param = p.getOwnerDocument().createElement(PARAMETER_TAG);
		Object value = config.getParameterRaw(pname);
		Text text;

		param.setAttribute(NAME_ATTRIBUTE, pname.toString());

		p.appendChild(param);
		if (value == null)
			return;

		String strValue = value.toString();
		if (strValue == null)
			return;

		if (strValue.indexOf("\n") != -1) {
			text = p.getOwnerDocument().createCDATASection(
					strValue.replaceAll("\r", ""));
		} else {

			if (config.isProtectedParameter(pname.toString()) && ! isProperty(config, strValue)) {
				MetamergeConfig mc = config.getMetamergeConfig();
				if (mc == null || mc.shouldEncryptProtected()) {
					try {
						byte[] data = CryptoUtils.encryptWithServerKey(strValue.getBytes("UTF-8"));
						strValue = UserFunctions.base64Encode(data);
						param.setAttribute(ENCRYPTED, "true");
					} catch (Exception e) {
						logmsg("Unable to encrypt parameter " + pname + " in " + config.getPath());
					}
				}
			}
			
			text = p.getOwnerDocument().createTextNode(strValue);
		}
		param.appendChild(text);
	}
	
	private boolean isProperty(BaseConfiguration config, String value) {
		if (!(config instanceof BaseConfigurationImpl))
			return false;
		String propname = ((BaseConfigurationImpl) config).getParameterPropertySourceFromValue(value);
		return propname != null && propname.startsWith("{property.")
				&& propname.endsWith("}");
	}

	/**
	 * @param node
	 *            Node object
	 * @return text from a node's children
	 * @throws Exception
	 */
	public String getNodeText(Node node) throws Exception {
		StringBuffer buf = new StringBuffer();
		Node n = node.getFirstChild();
		while (n != null) {
			switch (n.getNodeType()) {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				buf.append(n.getNodeValue());
				break;
			case Node.ELEMENT_NODE:
				buf.append(getNodeText(n));
				break;
			default:
				break;
			}
			n = n.getNextSibling();
		}

		return buf.toString();
	}

	/**
	 * @param node
	 *            Node object
	 * @param name
	 *            name to locate
	 * @return a single Element from a node's children
	 * @throws Exception
	 */
	public Element getSingleElement(Element node, String name) throws Exception {
		Node child = node.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals(name))
				return (Element) child;
			child = child.getNextSibling();
		}
		return null;
	}

	/**
	 * @param node
	 *            Node object
	 * @param name
	 *            child name
	 * @return text from a single named node's children
	 * @throws Exception
	 */
	public String getNodeTextByName(Element node, String name) throws Exception {
		Element e = getSingleElement(node, name);
		if (e == null)
			return null;
		else
			return getNodeText(e);
	}

	/**
	 * Sets a simple tag/value.
	 * 
	 * @param node
	 *            Node object
	 * @param tag
	 *            XML tag name
	 * @param bc
	 *            BaseConfiguration object
	 * @param name
	 *            name of the single Element
	 * @throws Exception
	 */
	public void setSingleElement(Element node, String tag,
			BaseConfiguration bc, String name) throws Exception {

		Object o = bc.getParameterRaw(name);
		if (o != null)
			setSingleElement(node, tag, o.toString());
	}

	/**
	 * Sets a simple tag/value.
	 * 
	 * @param node
	 *            Node object
	 * @param tag
	 *            XML tag name
	 * @param value
	 *            text node value
	 * @throws Exception
	 */
	public void setSingleElement(Element node, String tag, String value)
			throws Exception {
		if (value == null)
			return;

		Text text;
		if (value.indexOf("\n") != -1)
			text = node.getOwnerDocument().createCDATASection(
					value.replaceAll("\r", ""));
		else
			text = node.getOwnerDocument().createTextNode(value);

		Element child = node.getOwnerDocument().createElement(tag);
		child.appendChild(text);
		node.appendChild(child);
	}

	/**
	 * Get Null Behavior and Null Definition
	 * 
	 * @param elem
	 *            The Element to parse
	 * @param bc
	 *            The BaseConfiguration to set
	 * @throws Exception
	 */
	void getNullBehavior(Element elem, BaseConfiguration bc) throws Exception {
		String str = getNodeTextByName(elem, NULLBEHAVIOR);
		if (str != null && str.length() > 0)
			bc.setNullBehavior(str);

		str = getNodeTextByName(elem, NULLBEHAVIORVALUE);
		if (str != null && str.length() > 0)
			bc.setNullBehaviorValue(str);

		str = getNodeTextByName(elem, NULLDEFINITION);
		if (str != null && str.length() > 0)
			bc.setNullDefinition(str);

		str = getNodeTextByName(elem, NULLDEFINITIONVALUE);
		if (str != null && str.length() > 0)
			bc.setNullDefinitionValue(str);
	}

	/**
	 * Set Null Behavior and Null Definition
	 * 
	 * @param bc
	 *            The BaseConfiguration to use
	 * @param e
	 *            The Element to build
	 * @throws Exception
	 */
	void setNullBehavior(BaseConfiguration bc, Element e) throws Exception {
		// Null Behavior
		setSingleElement(e, NULLBEHAVIOR, bc, InternalSchema.NULL_BEHAVIOR);
		setSingleElement(e, NULLBEHAVIORVALUE, bc,
				InternalSchema.NULL_BEHAVIOR_VALUE);
		setSingleElement(e, NULLDEFINITION, bc, InternalSchema.NULL_DEFINITION);
		setSingleElement(e, NULLDEFINITIONVALUE, bc,
				InternalSchema.NULL_DEFINITION_VALUE);
	}

}
