/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ScriptComponent.java
//
//
package com.ibm.di.server;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.script.ScriptEngine;

/**
 * This class is used by the AssemblyLine for scripts.
 */
public class ScriptComponent extends AssemblyLineComponent {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The ScriptEngine object of the AssemblyLine.
	 */
	private ScriptEngine engine;

	/**
	 * The script attribute of the ScriptComponent.
	 */
	private String script;

	/**
	 * A boolean flag determining if the component should be executed.
	 */
	private boolean willExec;

	/**
	 * The script configuration.
	 */
	private ScriptConfig sconfig;

	private com.ibm.di.entry.Entry scriptObject = new com.ibm.di.entry.Entry();

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the AssemblyLine using this component
	 * @param name
	 *            the name of hte component
	 * @param config
	 *            script configuration of this component
	 * @param conn
	 *            an optional connector to use if needed
	 * @throws Exception
	 *             if problem occurs
	 */
	public ScriptComponent(AssemblyLine parent, String name,
			ScriptConfig config, ConnectorInterface conn) throws Exception {

		this.parent = parent;
		this.name = name;
		this.sconfig = config;
		log = new Log(parent.getLog());
		log.setDebug(parent.getLog().getDebug());
		log.setPrefix("[" + name + "] ");

		script = config.getStringParameter("script");
		engine = parent.getScriptEngine();
		stats = new TaskStatistics();
		willExec = config.getEnabled();
	}

	/**
	 * Constructor for the script component object.
	 * 
	 * @param parent
	 *            the parent AssemblyLine.
	 * @param name
	 *            the component's name given by the AssemblyLine.
	 * @param config
	 *            the configuration.
	 * @param conn
	 *            an optional connector to use if needed
	 * @throws Exception
	 *             if problem occurs
	 * 
	 */
	public ScriptComponent(AssemblyLine parent, String name,
			ConnectorConfig config, ConnectorInterface conn) throws Exception {

		this.parent = parent;
		this.name = name;
		this.config = config;
		log = new Log(parent.getLog());
		log.setDebug(parent.getLog().getDebug());
		log.setPrefix("[" + name + "] ");

		script = config.getStringParameter("script");
		engine = parent.getScriptEngine();
		stats = new TaskStatistics();
		willExec = config.getEnabled();
	}

	/**
	 * This method initializes the script engine.
	 * 
	 * @throws Exception
	 *             if initialization encounters problems
	 */
	public void initialize() throws Exception {
		if (sconfig == null)
			return;
		String files = sconfig.getStringParameter("includeFiles");
		scriptObject.setAttribute("AssemblyLine", parent.getName());
		scriptObject.setAttribute("Component", name);

		if (files != null && files.trim().length() > 0) {
			engine.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);
			engine.includeScript(files);
		}
	}

	/**
	 * This method closes the script engine.
	 * 
	 */
	public void close() {
		engine = null;
		script = null;
	}

	/**
	 * This method returns the Connector configuration
	 * 
	 * @return the connector configuration.
	 */
	public ConnectorConfig getConfiguration() {
		if (config == null && sconfig != null) {
			config = new ConnectorConfigImpl();
			try {
				config.init();
			} catch (Exception ignore) {
			}
			config.setMode(ConnectorConfig.SCRIPT_MODE);
			config.setScript(sconfig.getScript());
			config.setEnabled(sconfig.getEnabled());
			config.setParent(sconfig.getParent());
		}
		return config;
	}

	/**
	 * This method returns the Script configuration.
	 * 
	 * @return the script configuration
	 * @since 7.0
	 */
	public ScriptConfig getScriptConfiguration() {
		return sconfig;
	}

	/**
	 * Returns ServerConstants.TYPE_SCRIPT
	 * 
	 * @return the <code>ServerConstants.TYPE_SCRIPT</code> value
	 */
	public int getType() {
		return ServerConstants.TYPE_SCRIPT;
	}

	/**
	 * This method executes the script. If an error occurs, it will also declare
	 * the error bean.
	 * 
	 * @param meta
	 *            Entry parameter
	 * @throws Exception
	 *             if error occurs
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {

		if (script == null)
			return;

		try {
			engine.pushStackFrame();
			stats.add++;
			add1(meta);
		} catch (Exception e) {
			if (!e.getClass().getName().startsWith("com.ibm.di.exceptions.")) {
				com.ibm.di.entry.Entry err = new com.ibm.di.entry.Entry();
				err.setAttribute("status", "fail");
				err.setAttribute("exception", e);
				err.setAttribute("message", e.getMessage());
				err.setAttribute("class", e.getClass().getName());
				err.setAttribute("operation", "execute");
				err.setAttribute("connectorname", getName());

				engine.declareStaticBean("error", err);
			}
			throw e;
		} finally {
			engine.popStackFrame();
		}
	}

	/**
	 * This method executes the script.
	 * 
	 * @param meta
	 *            Entry parameter
	 * @throws Exception
	 *             if error occurs
	 */
	public void add1(com.ibm.di.entry.Entry meta) throws Exception {

		engine.declareBean("work", meta);
		engine.declareBean("thisConnector", this);
		engine.declareBean("thisComponent", this);
		engine.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);
		
		engine.interpret(script, false, getName());
	}

	/**
	 * Calls the hook named oper.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper) {
		return false;
	}

	/**
	 * Calls the hook named oper, declaring work as the corresponding bean. The
	 * trigger function calls one of the AssemblyLine hooks defined for this
	 * Connector using the provided work.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @param work
	 *            This will be the work bean in the hook.
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) {
		return false;
	}

	/**
	 * Calls the hook named oper, declaring work and conn as the corresponding
	 * beans. The trigger function calls one of the AssemblyLine hooks defined
	 * for this Connector using the provided conn/work.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @param work
	 *            This will be the work bean in the hook.
	 * @param conn
	 *            This will be the conn bean in the hook
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) {
		return false;
	}

	/**
	 * Return true/false if this component should be executed.
	 * 
	 * @param work
	 *            the work Entry object
	 * @return <code>true</code> if this component should be executed,
	 *         otherwise false
	 * @throws Exception
	 *             if problem occurs
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		return willExec
				&& !(parent.isSimulating() && getSimulatingState()
						.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE));
	}

}
