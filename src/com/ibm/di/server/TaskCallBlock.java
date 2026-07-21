/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;

/**
 * 
 * The TaskCallBlock (TCB) is used by a caller to set a number of parameters for
 * an AssemblyLine. The TCB can provide the user with a list of input/output
 * parameters specified by an AssemblyLine and also all the connectors and their
 * parameters the user can set. You can use this feature to discover dynamically
 * what an AssemblyLine is expecting as its initial work entry and also what it
 * will return in its result entry. The TCB consists of the following logical
 * sections:
 * <OL>
 * <LI>The Initial Work Entry passed to the AssemblyLine
 * <LI>The connector parameters
 * <LI>The input/output mapping rules for the AssemblyLine
 * <LI>An optional user provided accumulator object that receives all work
 * entries from the AL
 * </OL>
 * Calling an AssemblyLine with an initial work entry and setting the input
 * connector's filePath parameter to 'd:/myinput.txt' is accomplished with the
 * following code:
 * 
 * <pre>
 * var tcb = system.newTCB();
 * tcb.setInitialWorkEntry(iwe);
 * tcb.setConnectorParameter(&quot;input&quot;, &quot;filePath&quot;, &quot;d:/myinput.txt&quot;);
 * 
 * var al = main.startAL(&quot;MyAssemblyLine&quot;, tcb);
 * </pre>
 * 
 * The TCB is also called by the AssemblyLine for each work entry in the AL.
 * This work entry can be accumulated by the TCB into an object called the
 * accumulator. The accumulator can be one of the following:
 * <UL>
 * <LI>java.util.Collection - All work entries are cloned and added to the
 * collection (e.g. ArrayList, Vector ..)
 * <LI>ConnectorInterface - The putEntry() method is called
 * <LI>ParserInterface - The writeEntry() method is called
 * <LI>AssemblyLineComponent - The add() method is called
 * </UL>
 * If the accumulator is not of the above classes/interfaces an exception is
 * thrown.
 * 
 * To accumulate all work entries in an AssemblyLine into an XML file you could
 * do this:
 * 
 * <pre>
 * 
 * var parser = system.getParser ( &quot;ibmdi.XML&quot; );
 * parser.setOutputStream ( new java.io.FileOutputStream ( &quot;d:/accum.xml&quot; );
 * parser.initParser();
 * tcb.setAccumulator ( parser );
 * 
 * var al = main.startAL ( &quot;MyAssemblyLine&quot;, tcb );
 * al.join();
 * 
 * parser.closeParser();
 * 
 * </pre>
 * 
 * Of course, you could configure a connector instead of programming the parser
 * manually as in:
 * 
 * <pre>
 * 
 *  var connector = system.getConnector (&quot;MypreconfiguredOutputConnectorWithXMLParser&quot;);
 *  tcb.setAccumulator ( connector );
 *  ...
 *  connector.terminate();
 * 
 * </pre>
 * 
 * The TCB is typically initialized by the user and then used by the
 * assemblyline. If the assemblyline has a call/return specification the TCB
 * will remap input attributes (initial work entry) into what the AL expects
 * internally and likewise for setting the result object. This is done so that
 * the external call interface to an assemblyline can remain the same even
 * though the internal work entry names change in the assemblyline itself. Once
 * the TCB is passed to an assemblyline you should not expect anything more from
 * the TCB. Use the assemblyline's getResult() and getStats() to retrieve the
 * result object and statistics.
 * 
 * The TCB result mapping is performed before the Epilog so you can still access
 * the final result before the caller of the AL gets to it.
 */

public class TaskCallBlock extends Entry {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 115072761837771375L;

	//
	public final static String MAGIC_PROPERTY = "$metamerge.taskcallblock";

	public final static String INITIAL_WORK_ENTRY = "$metamerge.tcb.initialWorkEntry";

	public final static String RESULT_ENTRY = "$metamerge.tcb.resultEntry";

	public final static String CONNECTOR_PARAMETERS = "$metamerge.tcb.connectorParameters";

	public final static String ACCUMULATOR_OBJECT = "$metamerge.tcb.accumulator";

	public final static String RUNTIME_CONNECTOR = "$metamerge.tcb.runtimeConnector";

	public final static String RUNTIME_FUNCTION = "$metamerge.tcb.runtimeFunction";

	public final static String RUNMODE_PROPNAME = "assemblyline.runmode";

	public final static String AL_SETTINGS = "$metamerge.tcb.alSettings";

	public final static String AL_OPERATION = "assemblyline.operation";

	public final static String AL_OPERATION_INIT = "assemblyline.operation.initialize";

	public final static String INPUT_PARAMETERS = "$metamerge.tcb.inputParameters";

	public final static String OUTPUT_PARAMETERS = "$metamerge.tcb.outputParameters";

	public final static String TCB_PERMIT_EMPTY = "$metamerge.tcb.permitEmptyEntry";

	public final static String TCB_ATTRIBUTE_TARGET = "$metamerge.tcb.targetAttributeName";

	public final static String TCB_ATTRIBUTE_REQUIRED = "$metamerge.tcb.required";

	public final static String TCB_ATTRIBUTE_DEFAULT = "$metamerge.tcb.default";

	public final static String TCB_ATTRIBUTE_SYNTAX = "$metamerge.tcb.syntax";
	
	public final static String AL_REGRESSION_INPUT = "$al.regression.input";
	
	public final static String AL_REGRESSION_OUTPUT = "$al.regression.output";
	
	public final static String AL_REGRESSION_IGNORE_WORK = "$al.regression.ignore.work";
	
	public final static String AL_TASK_NAME = "$al.task.name";

	private final static String ADD_LOG_APPENDERS = "add.log.appenders";

	// The assemblyline this TCB applies to (optional)
	private String assemblyLine;

	/*
	 * 
	 * [TaskCallBlock] MagicProperty:1.0 InitialWorkEntry:Entry
	 * ConnectorParameters { "ConnectorName" { "ParameterName", "ParameterValue" } } //
	 * Verification of IWE InputParameters { "ParamName-1" { Required:
	 * true/false DestAttribute: "IWE Attribute name" } } // Result object
	 * populates from OutputParameters { "ParamName-1" { Required: true/false
	 * SourceAttribute: "LastWorkEntry Attribute name" } } [end]
	 */

	private AssemblyLineConfig alconfig = null;

	private transient TaskInterface task;

	private Log logger;

	private transient AttributeMapping inputMapping = null;

	private transient AttributeMapping outputMapping = null;

	private Hashtable<String, Boolean> enabledComponents = null;

	public TaskCallBlock() {
		super();
		setProperty(MAGIC_PROPERTY, "1.0");
		setProperty(CONNECTOR_PARAMETERS, new Entry());
		setTask(null);
	}

	/**
	 * Constructor - Populate object with assemblyline input/output parameters
	 * and connector parameters.
	 */
	public TaskCallBlock(String assemblyLine) throws Exception {
		this(assemblyLine, SystemFunctions.loadAssemblyLine(assemblyLine), null);
	}

	public TaskCallBlock(String assemblyLine, TaskInterface task)
			throws Exception {
		this(assemblyLine, SystemFunctions.loadAssemblyLine(assemblyLine), task);
	}

	/**
	 * Constructor - Populate object with assemblyline input/output parameters
	 * and connector parameters, and a task context.
	 */
	public TaskCallBlock(String assemblyLine, AssemblyLineConfig alc,
			TaskInterface task) throws Exception {
		super();
		this.alconfig = alc;
		this.assemblyLine = assemblyLine;
		setTask(task);

		if (alc == null)
			logger.exception("no.such.assemblyline", assemblyLine);

		// Call/Return Specification

		// Input parameters
		setInputParameters(buildCR(alc.getSchema(true)));
		setOutputParameters(buildCR(alc.getSchema(false)));

		// Connector parameters
		for (int i = 0; i < alc.getConnectorCount(); i++) {
			ConnectorConfig cc = alc.getConnector(i);
			String name = cc.getShortName();

			RawConnectorConfig rcc = cc.getConnectionConfig();
			if (rcc == null)
				continue;
			String javaClass = rcc.getJavaClass();
			// Reuse parameter
			if (name.startsWith("@") || (javaClass != null && javaClass.startsWith("@"))) {
				setConnectorParameters(name, null);
				continue;
			}

			setConnectorParameters(name, rcc);
		}
	}

	/**
	 * Constructor - Populate object with saved data and current task context.
	 */
	public TaskCallBlock(Entry old, AssemblyLineConfig alc, TaskInterface task) {
		super();
		this.alconfig = alc;
		setTask(task);

		// Restore parameters
		String[] list = old.getPropertyNames();
		for (int i = 0; i < list.length; i++)
			setProperty(list[i], old.getProperty(list[i]));
	}

	private Entry buildCR(SchemaConfig schema) {

		Entry out = new Entry();

		for (String name: schema.getItemNames()) {

			SchemaItemConfig sic = schema.getItem(name);

			Entry e = new Entry();

			e.setAttribute(TCB_ATTRIBUTE_TARGET, sic.getAttributeName());

			e.setAttribute(TCB_ATTRIBUTE_REQUIRED, sic.getPresenceFlag());

			out.setAttribute(sic.getAttributeName(), e);
		}

		return out;
	}

	/**
	 * Sets the logger from the given task context
	 */
	public void setTask(TaskInterface task) {
		this.task = task;
		if (task != null) {
			logger = task.getLog();
			loadMapping();
		} else if (RS.getServer() != null) {
			logger = RS.getServer().getLog();
		}
	}

	/**
	 * Set the Call/Return mapping
	 */
	public void loadMapping() {
		Trace.entrymax(this, "loadMapping");
		if (alconfig != null && task != null && task.getScriptEngine() != null) {

			AttributeMapConfig amc = null;

			String oper = getALOperation();
			if (oper != null && oper.trim().length() > 0) {
				if (alconfig.getOperation(oper) != null)
					amc = alconfig.getOperation(oper).getAttributeMap(true);
			} else {
				amc = alconfig.getAttributeMap(true);
			}

			if (amc != null && amc.size() > 0) {
				inputMapping = new AttributeMapping(alconfig.getName()
						+ ".Call", task, logger, task.getScriptEngine());
				try {
					inputMapping.loadMap(amc);
				} catch (Exception ex) {
					inputMapping = null;
				}
			} else {
				inputMapping = null;
			}

			if (oper != null && oper.trim().length() > 0) {
				if (alconfig.getOperation(oper) != null)
					amc = alconfig.getOperation(oper).getAttributeMap(false);
			} else {
				amc = alconfig.getAttributeMap(false);
			}

			if (amc != null && amc.size() > 0) {
				outputMapping = new AttributeMapping(alconfig.getName()
						+ ".Return", task, logger, task.getScriptEngine());
				try {
					outputMapping.loadMap(amc);
				} catch (Exception ex) {
					outputMapping = null;
				}
			} else {
				outputMapping = null;
			}
		}
		Trace.exitmax(this, "loadMapping");
	}

	/**
	 * Returns the assemblyline name this TCB was built from
	 */
	public String getAssemblyLineName() {
		return assemblyLine;
	}

	/**
	 * Sets the assemblyline name this TCB applies to
	 */
	public void setAssemblyLineName(String assemblyLine) {
		this.assemblyLine = assemblyLine;
	}

	/**
	 * Returns the initial work entry provided by caller
	 */
	public Entry getInitialWorkEntry() {
		return (Entry) getProperty(INITIAL_WORK_ENTRY);
	}

	/**
	 * Sets the initial work entry
	 */
	public void setInitialWorkEntry(Entry entry) {
		setProperty(INITIAL_WORK_ENTRY, entry.clone());
	}

	/**
	 * Constructs a new initial work entry if there are an input parameter
	 * specification in the assemblyline. Otherwise, uses the provided IWE asis.
	 */
	public Entry buildInitialWorkEntry() throws Exception {
		if (inputMapping == null)
			return getInitialWorkEntry();
		else
			return buildTCBEntry(getInitialWorkEntry(), true);
	}

	/**
	 * Returns the input parameter entry (from the AssemblyLine specification)
	 */
	public Entry getInputParameters() {
		return (Entry) getProperty(INPUT_PARAMETERS);
	}

	/**
	 * Sets the input parameter entry (from the AssemblyLine specification)
	 */
	public void setInputParameters(Entry entry) {
		if (entry.size() > 0)
			setProperty(INPUT_PARAMETERS, entry);
	}

	/*
	 * Returns the output parameter entry (from the AssemblyLine specification)
	 */
	public Entry getOutputParameters() {
		return (Entry) getProperty(OUTPUT_PARAMETERS);
	}

	/**
	 * Sets the output parameter entry (from the AssemblyLine specification)
	 */
	public void setOutputParameters(Entry entry) {
		if (entry.size() > 0)
			setProperty(OUTPUT_PARAMETERS, entry);
	}

	/**
	 * Returns the result entry provided by the caller
	 */
	public Entry getResultEntry() {
		return (Entry) getProperty(RESULT_ENTRY);
	}

	/*
	 * Sets the result entry
	 */
	public void setResultEntry(Entry entry) {
		if (entry != null)
			setProperty(RESULT_ENTRY, entry.clone());
		else
			setProperty(RESULT_ENTRY, null);
	}

	/**
	 * Builds the result object based on AL's result object and the output
	 * parameter specification.
	 */
	public Entry buildResultEntry() throws Exception {
		if (outputMapping == null)
			return getResultEntry();
		else
			return buildTCBEntry(getResultEntry(), false);
	}

	// Connector/Parser parameters
	public Entry getConnectorParameters() {
		Entry cp = (Entry) getProperty(CONNECTOR_PARAMETERS);
		if (cp == null) {
			cp = new Entry();
			setProperty(CONNECTOR_PARAMETERS, cp);
		}
		return cp;
	}

	/**
	 * Returns the parameters Entry for a specific connector.
	 */
	public Entry getConnectorParameters(String connectorName) {
		Trace.entrymax(this, "getConnectorParameters", connectorName);
		Entry cp = getConnectorParameters();
		if (cp.getAttribute(connectorName) == null)
			cp.newAttribute(connectorName);

		Entry cpe = (Entry) cp.getObject(connectorName);
		if (cpe == null) {
			cpe = new Entry();
			cp.setAttribute(connectorName, cpe);
		}
		Trace.exitmax(this, "getConnectorParameters", cpe);
		return cpe;
	}

	/**
	 * Returns the current value for a connector parameter as a String
	 */
	public Object getConnectorParameter(String connectorName,
			String connectorParameter) {
		return getConnectorParameter(connectorName, connectorParameter, true);
	}

	/**
	 * Returns the current value for a connector parameter.
	 */
	public Object getConnectorParameter(String connectorName,
			String connectorParameter, boolean string) {
		if (string)
			return getConnectorParameters(connectorName).getString(
					connectorParameter);
		else
			return getConnectorParameters(connectorName).getObject(
					connectorParameter);
	}

	/**
	 * Modifies connector configurations in the AssemblyLine (method is called
	 * by AssemblyLine)
	 */
	public void setConnectorParameters(AssemblyLine task) throws Exception {
		Trace.entrymax(this, "setConnectorParameters", task);
		logger.debug("begin.tcb.setconnectorparameters");
		
		for (AssemblyLineComponent tc: task.getConnectors()) {

			if (tc.reusingConnector)
				continue;
			
			// Set bc to the RawConnectorConfig/RawFunctionConfig
			BaseConfiguration bc = null;
			if (tc.connector != null) {
				bc = tc.connector.getRawConnectorConfiguration();
				String javaClass = null;
				if (bc instanceof RawConnectorConfig)
					javaClass = ((RawConnectorConfig) bc).getJavaClass();
				// If this is a re-used Connector, do not change parameters.
				if (javaClass != null && javaClass.startsWith("@"))
					continue;
			} else if (tc instanceof FunctionComponent) { // L3: Defect # 11579
				bc = ((FunctionComponent)tc).getConfiguration();
				if (bc instanceof FunctionConfig) 
					bc = ((FunctionConfig)bc).getFunctionConfig();
			}
			
			if (bc == null)
				continue;

			logger.debug("changing.component", tc.getName());
			Entry tp = getConnectorParameters(tc.getName());

			for (String key: tp.getAttributeNames()) {

				// Runtime conn or function is already set at this point
				if (key.equals(RUNTIME_CONNECTOR) || key.equals(RUNTIME_FUNCTION))
					continue;

				// get parameter attribute
				Attribute parameterAttribute = tp.getAttribute(key);
				if (parameterAttribute == null)
					continue; // Impossible
				
				// Get value for parameter
				String value = parameterAttribute.getValue();
				if (value == null)
					continue;

				setParamIfChanged(bc, key, value);

				// Remove the value from the TCB, no need to set it again.
				parameterAttribute.clear();
			}
		}
		logger.debug("end.tcb.setconnectorparameters");
		Trace.exitmax(this, "setConnectorParameters");
	}

	/**
	 * Sets a parameter if the value has changed. This way we avoid changing
	 * configurations unneeded, which could cause trouble for Connectors that
	 * are re-initialized when the config is changed.
	 */
	private void setParamIfChanged(BaseConfiguration bc, String key, String val) {
		if (val.equals(bc.getParameter(key)))
			return;

		logger.debug("setting.parameter", key);

		bc.setParameter(key, val);
	}

	/**
	 * Populates the parameter Entry for a specific connector with names from
	 * the configuration.
	 */
	public void setConnectorParameters(String connectorName,
			BaseConfiguration parameters) {
		Trace.entrymax(this, "setConnectorParameters", connectorName,
				parameters);
		Entry cpe = getConnectorParameters(connectorName);
		if (parameters == null)
			return;

		List<String> list = parameters.getKeys(BaseConfiguration.ONE_LEVEL
				| BaseConfiguration.RECURSIVE);
		for (String param:list) {
			if (RUNTIME_CONNECTOR.equals(param))
				continue;
			
			// Create a new Attribute, but do not copy the value.
			cpe.newAttribute(param);
//			if (cpe.getAttribute(param) == null){
//				if (parameters.getParameterPropertySource(param) != null)
//					cpe.newAttribute(param);
//				else
//					cpe.setAttribute(param, parameters.getParameter(param));
//			}
		}
		Trace.exitmax(this, "setConnectorParameters");
	}

	/**
	 * Sets a connector parameter.
	 * 
	 * @param connectorName
	 *            The Connector's name
	 * @param parameterName
	 *            The name of the parameter for the Connector
	 * @param parameterValue
	 *            The new value for the parameter
	 */
	public void setConnectorParameter(String connectorName,
			String parameterName, Object parameterValue) {
		Entry cpe = getConnectorParameters(connectorName);
		cpe.setAttribute(parameterName, parameterValue);
	}

	/**
	 * Sets a component parameter.
	 * 
	 * @param componentName
	 *            The Component's name
	 * @param parameterName
	 *            The name of the parameter for the Component
	 * @param parameterValue
	 *            The new value for the parameter
	 */
	public void setComponentParameter(String componentName,
			String parameterName, Object parameterValue) {
		getConnectorParameters(componentName).setAttribute(parameterName,
				parameterValue);
	}

	/**
	 * Sets a connector parameter.
	 * 
	 * @param connectorName
	 *            The Connector's name
	 * @param parameterName
	 *            The name of the parameter for the Connector
	 * @param parameterValue
	 *            The new value for the parameter
	 * @param protect
	 *            If true, do not print the value of this parameter in log files
	 */
	public void setConnectorParameter(String connectorName,
			String parameterName, Object parameterValue, boolean protect) {
		getConnectorParameters(connectorName).setAttribute(parameterName,
				parameterValue, protect);
	}

	/**
	 * Sets a component parameter, with a possibility to protect the parameter.
	 * 
	 * @param componentName
	 *            The Component's name
	 * @param parameterName
	 *            The name of the parameter for the Component
	 * @param parameterValue
	 *            The new value for the parameter
	 * @param protect
	 *            If true, do not print the value of this parameter in log files
	 */
	public void setComponentParameter(String componentName,
			String parameterName, Object parameterValue, boolean protect) {
		getConnectorParameters(componentName).setAttribute(parameterName,
				parameterValue, protect);
	}

	/**
	 * Sets the runtime connector for a named connector. If connectorName is
	 * null, the runtimeConnector is anonymous.
	 */
	public void setRuntimeConnector(String connectorName,
			ConnectorInterface runtimeConnector) {
		Trace.entrymax(this, "setRuntimeConnector");
		if (runtimeConnector != null) {
			if (connectorName != null)
				setConnectorParameter(connectorName, RUNTIME_CONNECTOR,
						runtimeConnector);
			else
				setProperty(RUNTIME_CONNECTOR, runtimeConnector);
		}
		Trace.exitmax(this, "setRuntimeConnector");
	}

	/**
	 * Returns the runtime connector for a named connector
	 */
	public ConnectorInterface getRuntimeConnector(String connectorName) {
		Trace.entrymax(this, "getRuntimeConnector", connectorName);
		if (connectorName != null) {
			Trace.exitmax(this, "getRuntimeConnector");
			return (ConnectorInterface) getConnectorParameter(connectorName,
					RUNTIME_CONNECTOR, false);
		} else {
			Trace.exitmax(this, "getRuntimeConnector");
			return (ConnectorInterface) getProperty(RUNTIME_CONNECTOR);
		}
	}

	/**
	 * Sets the runtime function for a named function. If name is null, the
	 * runtime function is anonymous.
	 */
	public void setRuntimeFunction(String name, FunctionInterface function) {
		Trace.entrymax(this, "setRuntimeFunction", name, function);
		if (function != null) {
			if (name != null)
				setConnectorParameter(name, RUNTIME_FUNCTION, function);
			else
				setProperty(RUNTIME_FUNCTION, function);
		}
		Trace.exitmax(this, "setRuntimeFunction");
	}

	/**
	 * Returns the runtime function for a named function
	 */
	public FunctionInterface getRuntimeFunction(String name) {
		Trace.entrymax(this, "getRuntimeFunction", name);
		if (name != null) {
			Trace.exitmax(this, "getRuntimeFunction");
			return (FunctionInterface) getConnectorParameter(name,
					RUNTIME_FUNCTION, false);
		} else {
			Trace.exitmax(this, "getRuntimeFunction");
			return (FunctionInterface) getProperty(RUNTIME_FUNCTION);
		}
	}

	/**
	 * Sets the runmode.
	 * 
	 * @param value
	 *            The runmode to set. Legal values are "normal", "record",
	 *            "playback", "manual" and "simulate".
	 */
	public void setRunMode(String value) {
		setProperty(RUNMODE_PROPNAME, value);
	}

	/**
	 * Returns the runmode
	 * 
	 * @return The RunMode for this AssemblyLine, "normal" if not set
	 */
	public String getRunMode() {
		return getStringProperty(RUNMODE_PROPNAME, AssemblyLine.RUNMODE_NORMAL);
	}

	/**
	 * Constructs a new Entry based on the parameter descriptors in params and
	 * source attributes in input entry.
	 */
	public Entry buildTCBEntry(Entry input, boolean inputMap) throws Exception {
		Entry result = input;
		logger.debug("begin.buildtcbentry");

		AttributeMapping am = inputMap ? inputMapping : outputMapping;
		// If no alconfig then we have no attribute map
		if (am != null) {
			am.declareBean("work", input);
			am.declareBean("event", input);
			result = am.mapEntry(input, new Entry());
			if (inputMap)
				am.releaseBeans();
		}

		logger.debug("end.buildtcbentry");
		return result;
	}

	/**
	 * Set the accumulator object to the provided object. This must be an
	 * instance of java.util.Collection, ConnectorInterface, ParserInterface or
	 * AssemblyLineComponent.
	 * 
	 * @param accumulator
	 *            The Object to use as the accumulator object.
	 */
	public void setAccumulator(Object accumulator) {
		setProperty(ACCUMULATOR_OBJECT, accumulator);
	}

	/**
	 * Set the accumulator object to the provided object.
	 * 
	 * @see #setAccumulator(Object )
	 */
	public void setAccumulatorObject(Object accumulator) {
		setProperty(ACCUMULATOR_OBJECT, accumulator);
	}

	/**
	 * Adds an entry to the accumulator object (if any configured).
	 */
	@SuppressWarnings("unchecked")
	public void accumulateEntry(Entry work) throws Exception {

		if (work == null)
			return;

		Object accum = getProperty(ACCUMULATOR_OBJECT);
		if (accum == null)
			return;

		if (accum instanceof Collection) {
			((Collection<Entry>) accum).add(work.clone());
		} else if (accum instanceof ConnectorInterface) {
			((ConnectorInterface) accum).putEntry(work);
		} else if (accum instanceof ParserInterface) {
			((ParserInterface) accum).writeEntry(work);
		} else if (accum instanceof AssemblyLineComponent) {
			((AssemblyLineComponent) accum).add(work);
		} else {
			logger.exception("wrong.accumulator.object.type", accum.getClass()
					.getName());
		}
	}

	public String getStringProperty(String propname, String defval) {
		Object val = getProperty(propname);
		if (val == null)
			return defval;
		else if (val instanceof String)
			return (String) val;
		else
			return val.toString();
	}

	public int getIntProperty(String propname, int defval) throws Exception {
		Object val = getProperty(propname);
		if (val == null)
			return defval;
		else if (val instanceof Integer)
			return ((Integer) val).intValue();
		else
			return Integer.parseInt(val.toString());
	}

	public boolean getBoolProperty(String propname, boolean defval)
			throws Exception {
		Object val = getProperty(propname);
		if (val == null)
			return defval;
		else if (val instanceof Boolean)
			return ((Boolean) val).booleanValue();
		else if (val instanceof Integer)
			return (((Integer) val).intValue() != 0);
		else
			return Boolean.valueOf(val.toString()).booleanValue();
	}

	public AssemblyLineConfig getAssemblyLineConfig() {
		return alconfig;
	}

	/**
	 * Returns the Entry containing the user specified AL settings.
	 */
	public Entry getALSettings() {
		Entry cp = (Entry) getProperty(AL_SETTINGS);
		if (cp == null) {
			cp = new Entry();
			setProperty(AL_SETTINGS, cp);
		}
		return cp;
	}

	/**
	 * Sets the AL Settings parameter to value. The settings are the AL config
	 * section where you can set things like debug level, max reads etc.
	 */
	public void setALSetting(String paramname, Object value) {
		getALSettings().setAttribute(paramname, value);
	}

	/**
	 * Returns the AL Settings parameter to value. The settings are the AL
	 * config section where you can set things like debug level, max reads etc.
	 */
	public Object getALSetting(String paramname) {
		return getALSettings().getObject(paramname);
	}

	/**
	 * Applies the user defined AL settings to the AssemblyLineConfig. Also sets
	 * the components to enabled/disabled as specified by setComponentEnabled().
	 * This method is used by AssemblyLine on a cloned AssemblyLineConfig.
	 * 
	 * @see #setComponentEnabled(String, boolean )
	 * @param alc
	 *            The AssemblyLineConfig to modify
	 */
	public void applyALSettings(AssemblyLineConfig alc) {
		Trace.entrymax(this, "applyALSettings", alc);
		this.alconfig = alc;
		BaseConfiguration bc = alc.getSettings();
		Entry settings = getALSettings();
		String[] names = settings.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			bc.setParameter(names[i], settings.getObject(names[i]));
		}

		if (enabledComponents == null) {
			Trace.exitmax(this, "applyALSettings");
			return;
		}
		for (Enumeration<String> e = enabledComponents.keys(); e
				.hasMoreElements();) {
			String name = e.nextElement();
			boolean value = enabledComponents.get(name).booleanValue();
			try {
				BaseConfiguration cc = alc.getComponent(name);
				if (cc instanceof ContainerConfig) {
					((ContainerConfig) cc).setEnabled(value);
				} else if (cc instanceof ConnectorConfig) {
					((ConnectorConfig) cc)
							.setState(value ? ConnectorConfig.ENABLED_STATE
									: ConnectorConfig.DISABLED_STATE);
				}
			} catch (Exception notFound) {
				// maybe log an info message?
			}
		}
		Trace.exitmax(this, "applyALSettings");
	}

	/**
	 * Sets the AL Operation.
	 * 
	 * @param operation
	 *            The AL operations
	 */
	public void setALOperation(String operation) {
		String curop = getALOperation();
		setProperty(AL_OPERATION, operation);
		if (curop == null || !curop.equalsIgnoreCase(operation))
			loadMapping();
	}

	/**
	 * Returns the AL operation
	 * 
	 * @return The operation for this AssemblyLine (null if not specified)
	 */
	public String getALOperation() {
		return getStringProperty(AL_OPERATION, null);
	}

	/**
	 * Sets the AL Operation initialize parameters. This call replaces all
	 * previous init params
	 * 
	 * @param params
	 *            The AL operations
	 */
	public void setOperationInitParams(Entry params) {
		setProperty(AL_OPERATION_INIT, params);
	}

	/**
	 * Sets an operation init parameter - this call creates the init param entry
	 * if not present and adds/replaces the param if one has already been set.
	 * 
	 * @param paramName
	 *            The name of the parameter
	 * @param paramValue
	 *            The value for the parameter
	 */
	public void setOperationInitParam(String paramName, Object paramValue) {
		getOperationInitParams().setAttribute(paramName, paramValue);
	}

	/**
	 * Returns the operation init parameters
	 * 
	 * @return The operation init parameters ( an empty Entry if nothing is
	 *         specified )
	 */
	public Entry getOperationInitParams() {
		Entry e = (Entry) getProperty(AL_OPERATION_INIT);
		if (e == null) {
			setProperty(AL_OPERATION_INIT, (e = new Entry()));
		}
		return e;
	}

	/**
	 * Set the named component to enabled or disabled status.
	 * 
	 * @param name
	 *            The name of the Component
	 * @param enabled
	 *            True if this component should be enabled, false if it should
	 *            be disabled
	 */
	public void setComponentEnabled(String name, boolean enabled) {
		if (enabledComponents == null)
			enabledComponents = new Hashtable<String, Boolean>();
		enabledComponents.put(name, Boolean.valueOf(enabled));
	}

	/**
	 * Returns the SchemaConfig for the Published AssemblyLine Initialization
	 * Parameters.
	 * 
	 * @since 7.0
	 */
	public SchemaConfig getALInitializationParameters() {
		if (alconfig == null)
			return null;
		return alconfig.getPublishedInitParams();
	}

	/**
	 * Returns a String[] with names of operations for the AssemblyLine.
	 * 
	 * @since 7.0
	 */
	public String[] getALOperations() {
		if (alconfig == null)
			return null;
		ContainerConfig cc = alconfig.getOperations();
		int n = cc.size();
		String[] ret = new String[n];
		for (int i = 0; i < n; i++)
			ret[i] = cc.getConfig(i).getShortName();

		return ret;
	}

	/**
	 * Returns the AssemblyLine's OperationConfig for the current operation
	 * 
	 * @since 7.0
	 */
	public OperationConfig getALOperationConfig() {
		String name = getALOperation();
		if (name == null || alconfig == null)
			return null;
		return alconfig.getOperation(name);
	}

	/**
	 * Returns the OperationConfig for the named operation in the AssemblyLine.
	 * 
	 * @param name
	 *            Name of the operation
	 * @since 7.0
	 */
	public OperationConfig getALOperationConfig(String name) {
		if (alconfig == null)
			return null;
		return alconfig.getOperation(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TaskCallBlock clone() {
		TaskCallBlock clone = (TaskCallBlock) super.clone();

		if (enabledComponents != null)
			clone.enabledComponents = (Hashtable<String, Boolean>) enabledComponents
				.clone();

		return clone;
	}

	/**
	 * Sets the filename to use when reading regression info.
	 * @param fileName
	 * @since 7.2
	 */
	public void setRegressionInputName(String fileName) {
		setProperty(AL_REGRESSION_INPUT, fileName);
	}
	
	/**
	 * Returns the filename to use when reading regression info.
	 * @since 7.2
	 */
	public String getRegressionInputName() {
		return getStringProperty(AL_REGRESSION_INPUT, null);
	}
	
	/**
	 * Sets the filename to use when writing regression info.
	 * @param fileName
	 * @since 7.2
	 */
	public void setRegressionOutputName(String fileName) {
		setProperty(AL_REGRESSION_OUTPUT, fileName);
	}
	
	/**
	 * Returns the filename to use when writing regression info.
	 * @since 7.2
	 */
	public String getRegressionOutputName() {
		return getStringProperty(AL_REGRESSION_OUTPUT, null);
	}
	
	/**
	 * Sets if the work Entry should be ignored when reading or writing regression info.
	 * @param value
	 */
	public void setRegressionIgnoreWork(boolean value) {
		setProperty(AL_REGRESSION_IGNORE_WORK, value);
	}
	
	/**
	 * Returns true if the work Entry should be ignored when reading or writing regression info.
	 * The default value is false.
	 * @return
	 */
	public boolean getRegressionIgnoreWork() {
		try {
			return getBoolProperty(AL_REGRESSION_IGNORE_WORK, false);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Sets the value returned by getAddLogAppenders()
	 * @param value
	 */
	public void setAddLogAppenders(boolean value) {
		setProperty(ADD_LOG_APPENDERS, value);
	}
	
	/**
	 * Returns true if the configured AssemblyLine Log Appenders should be added to the Logger.
	 * The default value is true.
	 * @return
	 */
	public boolean getAddLogAppenders() {
		try {
			return getBoolProperty(ADD_LOG_APPENDERS, true);
		} catch (Exception e) {
			return true;
		}
	}
	
	/**
	 * Sets the name the AL will get while running.
	 * This may be useful to give the AL a unique name, instead
	 * of the default name which is the same as the config name.
	 * @param name
	 */
	public void setTaskName(String name) {
		setProperty(AL_TASK_NAME, name);
	}
	
	
	/**
	 * Returns the name the AL will get while running.
	 * @return The specific AL name, or null if not set
	 */
	public String getTaskName() {
		return getStringProperty(AL_TASK_NAME, null);
	}
}
