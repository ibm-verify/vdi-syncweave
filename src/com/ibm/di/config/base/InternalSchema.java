/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

/**
 * This class defines all the parameter names used in other Configuration classes
 * as String constants, to make it easier to use the same name everywhere.
  *
 */
public class InternalSchema {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// Operational attributes
	public final static String INHERITANCE = "$inheritance";

	// General
	public final static String ENABLED = "enabled";

	public final static String LOG_ENABLED = "logenabled";

	public final static String DEBUG = "debug";

	public final static String DEBUG_BREAK = "debugBreak";

	public final static String INHERITS_FROM = "inheritFrom";

	public final static String INHERIT_SECTION = "inheritSection";

	public final static String SCRIPT = "script";

	public final static String SCRIPT_ENGINE = "ScriptEngine";

	public final static String NULL_BEHAVIOR = "nullBehavior";

	public final static String NULL_BEHAVIOR_VALUE = "nullBehaviorValue";

	public final static String NULL_DEFINITION = "nullDefinition";

	public final static String NULL_DEFINITION_VALUE = "nullDefinitionValue";

	public final static String USER_COMMENT = "userComment";

	public final static String DELTA_BEHAVIOR = "deltaBehavior";

	public final static String DELTA_STRICT = "deltaStrict";

	// Connector Configuration
	public final static String CONNECTOR_LINK_CONFIG = "linkConfig";

	public final static String CONNECTOR_LINK_CRITERIA = "linkCriteria";

	public final static String CONNECTOR_ADVANCED_LINK_CRITERIA = "linkScript";

	public final static String CONNECTOR_LINK_MODE = "advancedLink";

	public final static String CONNECTOR_LINK_OR = "matchAny";

	public final static String CONNECTOR_CONNECTOR_CONFIG = "connectorConfig";

	public final static String CONNECTOR_CONNECTOR_PARSEROPTION = "parserOption";

	public final static String CONNECTOR_CONNECTOR_JAVACLASS = "connectorType";

	public final static String CONNECTOR_CONNECTOR_OPCARRIER = "@operation";

	public final static String CONNECTOR_CONNECTOR_OPCARRIERPROP = "@operation_isproperty";

	public final static String CONNECTOR_PARSER_CONFIG = "parserConfig";

	public final static String CONNECTOR_DELTA_CONFIG = "deltaConfig";

	public final static String CONNECTOR_ATTRIBUTE_MAP_IN = "inputAttributeMap";

	public final static String CONNECTOR_ATTRIBUTE_MAP_OUT = "outputAttributeMap";

	public final static String CONNECTOR_ATTRIBUTES = "attributes";

	public final static String CONNECTOR_MODE = "type";

	public final static String CONNECTOR_HOOKS = "ALEvent";

	public final static String CONNECTOR_DELTA_UNIQUE_ATTR = "uniqueAttribute";

	public final static String CONNECTOR_DELTA_DB = "deltaDB";

	public final static String CONNECTOR_DELTA_ITER_DELETED = "iterateDeletedEntries";

	public final static String CONNECTOR_DELTA_REMOVE_DELETED = "removeDeletedEntries";

	public final static String CONNECTOR_DELTA_RETURN_UNCHANGED = "returnUnchangedEntries";

	public final static String CONNECTOR_DELTA_COMMIT_ON_ENDITER = "On end of AL cycle";

	public final static String CONNECTOR_DELTA_WHEN_TO_COMMIT = "whenToCommit";

	public final static String CONNECTOR_DELTA_LEVEL = "deltaLevel";

	public final static String CONNECTOR_DELTA_FAST_ALGORITHM = "fastAlgorithm"; // 6.1.1

	public final static String CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS = "allowDuplicateDeltaKeys";

	public final static String CONNECTOR_DELTA_ROW_LOCKING = "rowLocking";
	
	public final static String CONNECTOR_DELTA_ATTRIBUTE_LIST = "attributeList";
	
	public final static String CONNECTOR_DELTA_CHANGE_DETECTION_MODE = "changeDetectionMode";

	public final static String CONNECTOR_CHILD_INHERIT = "childInheritance";

	public final static String CONNECTOR_STATE = "state";

	public final static String CONNECTOR_COMPUTE_CHANGES = "computeChanges";

	public final static String CONNECTOR_SKIP_LOOKUP = "skipLookup";

	public final static String CONNECTOR_CHECKPOINT_CONFIG = "checkpoint";

	public final static String CONNECTOR_SANDBOX_CONFIG = "sandbox";

	public final static String CONNECTOR_SCHEMA_INPUT = "inputSchema";

	public final static String CONNECTOR_SCHEMA_OUTPUT = "outputSchema";

	public static final String CONNECTOR_POOL_DEF_CONFIG = "poolDefConfig";

	public static final String CONNECTOR_POOL_DEF_ENABLED = "enabled";

	public static final String CONNECTOR_POOL_DEF_MAX_SIZE = "maxPoolSize";

	public static final String CONNECTOR_POOL_DEF_MIN_SIZE = "minPoolSize";

	public static final String CONNECTOR_POOL_DEF_PURGE_INTERVAL = "purgeInterval";

	public static final String CONNECTOR_POOL_DEF_INITIALIZE_ATTEMPTS = "initializeAttempts";

	public static final String CONNECTOR_POOL_DEF_INITIALIZE_SLEEP_INTERVAL = "initializeSleepInterval";

	public static final String CONNECTOR_POOL_INSTANCE_CONFIG = "poolInstanceConfig";

	public static final String CONNECTOR_POOL_INSTANCE_ENABLED = "enabled";

	public static final String CONNECTOR_POOL_INSTANCE_EXHAUSTED_BEHAVIOR = "exhaustedPoolBehavior";

	// 5.2
	public final static String CONNECTOR_DELTA_DRIVER = "driver";

	// 6.0
	public final static String CONNECTOR_SERVER_OPTION = "serverOption";

	public final static String CONNECTOR_SERVER_REPLY = "serverReply";

	public final static String CONNECTOR_RECONNECT_CONFIG = "reconnectConfig";

	// 6.1
	public final static String CONNECTOR_INIT_OPTION = "compInitOption";

	// Child inheritance
	public final static String CONNECTOR_INHERIT_LINK = "LinkCriteria";

	public final static String CONNECTOR_INHERIT_HOOKS = "Hooks";

	public final static String CONNECTOR_INHERIT_ATTR_IN = "InputAttributeMap";

	public final static String CONNECTOR_INHERIT_ATTR_OUT = "OutputAttributeMap";

	// 7.0
	public static final String CONNECTOR_BRANCH_TYPE = "branchType";

	public final static String CONNECTOR_RECONNECT_RULES = "ReconnectRules";
	
	public final static String CONNECTOR_LOOKUP_LIMIT = "findreturncount";

	// 7.2
	public final static String CONNECTOR_SUPPORTED_MODES = "SupportedModes";

	// Raw Connector
	public final static String RC_CONNECTOR_CLASS = "connectorType";

	// Schema
	public final static String SCHEMA_NAME = "name";

	public final static String SCHEMA_INTERNAL_SYNTAX = "syntax";

	public final static String SCHEMA_EXTERNAL_SYNTAX = "extsyntax";

	public final static String SCHEMA_SAMPLE = "sample";

	public final static String SCHEMA_EXCLUDED = "excluded";

	public final static String SCHEMA_INPUT_REQUIRED = "inputRequired";

	public final static String SCHEMA_OUTPUT_REQUIRED = "outputRequired";

	public final static String SCHEMA_DEFAULT_VALUE = "defaultValue";

	public final static String SCHEMA_PRESENCE = "presence";

	public final static String SCHEMA_INPUT = "inputSchema";

	public final static String SCHEMA_OUTPUT = "outputSchema";

	public final static String SCHEMA_OCCURS_MIN = "OccursMin";

	public final static String SCHEMA_OCCURS_MAX = "OccursMax";

	public final static String SCHEMA_PROPERTY = "Property";

	// AssemblyLine
	public final static String AL_CONNECTOR_LIST = "componentlist";

	public final static String AL_COMPONENT_LIST = "components";

	public final static String AL_PROLOG = "prolog";

	public final static String AL_PROLOG_INIT = "prolog0";

	public final static String AL_EPILOG = "epilog";

	public final static String AL_EPILOG2 = "epilog2";

	public final static String AL_STARTCYCLE = "startcycle";

	public final static String AL_ONSUCCESS = "onsuccess";

	public final static String AL_ONFAILURE = "onfailure";

	public final static String AL_AUTOMAP_ATTRIBUTES = "automapattributes";

	public final static String AL_CALL_PARAMETERS = "IOSettings";

	public final static String AL_SETTINGS = "Settings";

	public final static String AL_SHUTDOWN = "ShutdownScript";

	public final static String AL_CHECKPOINT = "checkpoint";

	// left behind because it is used internally
	public final static String AL_EH = "eventHandler";

	public final static String AL_THREADOPTIONS = "threadOptions";

	public final static String AL_DISABLE_EVENTS = "assemblyline.ehc.disableEvents";

	public final static String AL_EXECUTE_PROLOG = "assemblyline.ehc.executeProlog";

	public final static String AL_EH_PROPS2ATTRS = "assemblyline.ehc.mapPropsToAttrs";

	public final static String AL_OPERATIONS = "operations";

	public final static String AL_INIT_PARAMS = "initParams";

	public final static String AL_SIMULATE_MODE = "assemblyline.simulate";

	public final static String AL_SIMULATE_CONFIG = "assemblyline.simulate.config";

	public final static String AL_SIMULATE_HOOKS = "assemblyline.simulate.hooks";

	// Call Params
	public final static String TCB_INPUT_PARAMETERS = "Input";

	public final static String TCB_OUTPUT_PARAMETERS = "Output";

	public final static String TCB_ATTRIBUTE_TARGET = "$metamerge.tcb.targetAttributeName";

	public final static String TCB_ATTRIBUTE_SYNTAX = "$metamerge.tcb.syntax";

	public final static String TCB_ATTRIBUTE_REQUIRED = "$metamerge.tcb.required";

	public final static String TCB_ATTRIBUTE_DEFAULT = "$metamerge.tcb.default";

	// Parser
	public final static String PARSER_JAVACLASS = "class";

	// Scripts
	public final static String SC_AUTO_INCLUDE = "autoInclude";

	public final static String SC_INCLUDE_FILES = "includeFiles";

	// Include files / Namespaces
	public final static String NAMESPACE_URL = "java.naming.provider.url";

	public final static String NAMESPACE_DRIVER = "com.ibm.di.config.interfaces.driver";

	// Properties

	// Libraries

	// Attribute Map
	public final static String AMI_ADD = "add";

	public final static String AMI_MODIFY = "mod";

	public final static String AMI_SCRIPT = "script";

	public final static String AMI_TYPE = "type";

	public final static String AMI_SIMPLE = "simple";

	public final static String AMI_SUBSTITUTION = "substitution";

	// Link Criteria
	public final static String LC_ATTRIBUTE = "linkAttribute";

	public final static String LC_VALUE = "linkValue";

	public final static String LC_OPERATOR = "linkOperator";

	// Hooks
	public final static String HC_ENABLED = "active";

	public final static String HC_NAME = "name";

	public final static String HC_SCRIPT = "script";

	// Forms
	public final static String FORM_DEFAULT_VALUE = "default";

	public final static String FORM_SYNTAX = "syntax";

	public final static String FORM_LABEL = "label";

	public final static String FORM_VALUES = "values";

	public final static String FORM_LOCALIZEDVALUES = "localizedvalues";

	public final static String FORM_TOOLTIP = "description";

	public final static String FORM_SCRIPT = "script";

	public final static String FORM_SCRIPT_LABEL = "scriptLabel";

	public final static String FORM_SCRIPT_TOOLTIP = "scripthelp";

	public final static String FORM_EVENT_HANDLER = "formevents";

	public final static String FORM_WIDTH = "width";

	public final static String FORM_HEIGTH = "height";

	public final static String FORM_USEHYPERLABELS = "usehyperlabel";

	// External properties
	public final static String EXTPROP_FILE_PATH = "externalFilePath";

	public final static String EXTPROP_ENCRYPTED = "externalEncrypted";

	public final static String EXTPROP_PASSWORD = "externalPassword";

	public final static String EXTPROP_QUERY_SIBLINGS = "externalQuerySiblings";

	public final static String EXTPROP_CIPHER = "externalCipher";

	// Checkpoint
	public final static String CHECKPOINT_IDENTIFIER = "identifier";

	public final static String CHECKPOINT_RSI = "checkpointRSI";

	public final static String CHECKPOINT_WORK = "checkpointWork";

	// Sandbox
	public final static String SANDBOX_IDENTIFIER = "sbIdentifier";

	public final static String SANDBOX_RECORD = "sbRecord";

	public final static String SANDBOX_PLAYBACK = "sbPlayback";

	// Logger
	public final static String LOG_CONFIG = "logConfig";

	public final static String LOG_CONFIG_LEVEL = "com.ibm.di.log.level";

	// Function
	public final static String FUNCTION_CONFIG = "functionConfig";

	// 6.1.1
	// BranchConditions
	public final static String BRANCH_CONDITION_LEFT = "leftHand";

	public final static String BRANCH_CONDITION_RIGHT = "rightHand";

	public final static String BRANCH_CONDITION_OPER = "operator";

	public final static String BRANCH_CONDITION_NEGATE = "negate";

	public final static String BRANCH_CONDITION_CASE_SENSITIVE = "caseSensitive";

	// Loops
	public final static String LOOP_WORK_NAME = "workAttributeName";

	public final static String LOOP_ATTR_NAME = "loopAttributeName";

	public final static String LOOP_INIT_OPTION = "InitOption";

	public final static String LOOP_TYPE = "LoopType";

	// SolutionInterface
	public final static String SI_EXP_HEALTH = "HealthAssemblyLine";

	public final static String SI_EXP_HEALTH_POLL = "PollInterval";

	public final static String SI_INSTANCE_ID = "InstanceID";

	public final static String CONTAINER_CHILDREN = "Children";

	public final static String ATTRIBUTE_MAP_CHILDREN = "childAttributeMaps";
	
	//SchedulerConfig
	public final static String SCHEDULER_SCHEDULED_NAME = "assemblyLine";
	
	public final static String SCHEDULER_START_TIMES = "StartTimes";
	
	public final static String SCHEDULER_TYPE = "SchedulerType";
	
	public final static String PROTECTED_PARAMETERS = "ProtectedParameters";
}
