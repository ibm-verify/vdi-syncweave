/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.fc.Function;
import com.ibm.di.fc.FunctionInterface;

/**
 * This is a wrapper class for SyncWeave Function
 * Components. Objects of this class are instantiated by the AssemblyLine, as
 * the AssemblyLine only works with AssemblyLineComponent objects, not
 * Connectors, Functions Components, etc.
 */
public class FunctionComponent extends AssemblyLineComponent {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The SyncWeave configuration of the Function
	 * Component
	 */
	protected FunctionConfig config;

	/**
	 * The reference to the wrapped Function Component object
	 */
	public FunctionInterface function;

	/**
	 * The FunctionInterface of this Function Component.
	 */
	protected FunctionInterface rtp_function;

	/**
	 * The default attribute name.
	 */
	protected String defaultAttributeName;

	/**
	 * This is the object performing output attribute mapping
	 */
	protected AttributeMapping omap;

	/**
	 * Constructor for the FunctionComponent object
	 *
	 * @param parent
	 *            the AssemblyLine instantiating this Function Component
	 * @param name
	 *            the name of this Function Component
	 * @param config
	 *            the SyncWeave configuration of this
	 *            Function Component
	 * @exception Exception
	 *                this exception is thrown if the construction of the
	 *                Function Component object fails
	 */
	public FunctionComponent(AssemblyLine parent, String name,
			FunctionConfig config) throws Exception {
		this(parent, name, config, null);
	}

	/**
	 * Constructor for the FunctionComponent object
	 *
	 * @param parent
	 *            the AssemblyLine instantiating this Function Component
	 * @param name
	 *            the name of this Function Component
	 * @param config
	 *            the SyncWeave configuration of this
	 *            Function Component
	 * @param function
	 *            the FunctionInterface of this Function Component.
	 * @exception Exception
	 *                this exception is thrown if the construction of the
	 *                Function Component object fails
	 */
	public FunctionComponent(AssemblyLine parent, String name,
			FunctionConfig config, FunctionInterface function) throws Exception {
		this.config = config;
		this.rtp_function = function;

		initCommon(parent, name, config);

		log = new Log(parent.getLog());
		if (config.getFunctionConfig() != null)
			log.setDebug(config.getFunctionConfig().getDebug(false));
		log.setPrefix("[" + getName() + "] ");

		// AssemblyLine Hooks
		log.debug("load.hooks");
		handler = new AttributeMapping(getName(), parent, log, parent
				.getScriptEngine());
		HooksConfig handlerMap = config.getHooks();
		if (handlerMap != null) {
			handler.loadEventMap(handlerMap);
		}

		stats = new TaskStatistics();
		if (rtp_function == null)
		{
			this.function = com.ibm.di.function.SystemFunctions.loadFunction(((FunctionConfig) config), log);
			this.function.setRSInterface(parent.getParent());
		}
		else
		{
			this.function = rtp_function;
		}
	}

	/**
	 * This method initializes the FunctionComponent, if it should initialized at startup.
	 *
	 * @exception Exception
	 *                this exception is thrown if the initialization of this
	 *                Function Component fails
	 */

	public void initialize() throws Exception {
		if (config.getInitializeOption() == ConnectorConfig.COMP_INIT_DEFAULT)
			doInitialize();
	}

	/**
	 * This method initializes the component, calls initialization hooks and
	 * creates input and output maps.
	 *
	 * @exception Exception
	 *                this exception is thrown if the initialization fails.
	 */
	public void doInitialize() throws Exception {
		if (is_initialized) {
			return;
		}
		try {
			handler.pushStackFrame(this);

			trigger("before_initialize", parent.getWork(), null);
			if (rtp_function == null) {

				function.initialize(null);
			} else {

				log.debug(log.getString("dont.init.runtime.function"));
			}
			function.setContext(parent);
			trigger("after_initialize", parent.getWork(), null);
			is_initialized = true;
			initializeCount++;
		} catch (Throwable error) {
			handleException(INITIALIZE, error, parent.getWork());
		} finally {
			handler.popStackFrame();
		}

		omap = new AttributeMapping(name, parent, parent.getLog(), parent
				.getScriptEngine());
		omap.loadMap(config.getAttributeMap(false));

		imap = new AttributeMapping(name, parent, parent.getLog(), parent
				.getScriptEngine());
		imap.loadMap(config.getAttributeMap(true));

		String am = (String) parent.getConfig("automapattributes");
		if (am != null && am.equalsIgnoreCase("true")) {
			imap.setAutomap(true);
			omap.setAutomap(true);
		}

		defaultAttributeName = config.getFunctionConfig().getStringParameter(
				"defaultAttribute");
	}

	/**
	 * This method closes the script engine.
	 *
	 * @throws Exception
	 *             if problem occurs
	 */
	public void close() throws Exception {
		if (function != null && rtp_function == null) {
			doConnectorTerminate();
		}

		function = null;
		rtp_function = null;
	}

	/**
	 * This method calls closing hooks and terminates the wrapped function
	 * component object.
	 *
	 * @exception Exception
	 *                this exception is thrown if the operation fails.
	 */
	public void doConnectorTerminate() throws Exception {
		try {
			handler.pushStackFrame(this);
			if (function != null && rtp_function == null) {
				trigger("before_close", parent.getResult(), null);
				if (is_initialized) {
					is_initialized = false;
					function.terminate();
				}
				trigger("after_close", parent.getResult(), null);
			}
		} catch (Exception err) {
			handleException("close", err, parent.getResult());
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * This method returns the Function interface.
	 *
	 * @return the function interface
	 */
	public FunctionInterface getFunction() {
		return function;
	}

	/**
	 * Gets the type attribute of the FunctionComponent object
	 *
	 * @return The type value
	 */
	public int getType() {
		return ServerConstants.TYPE_FUNCTION;
	}

	/**
	 * Return true/false if this component should be executed.
	 *
	 * @param work
	 *            The current work Entry
	 * @return True if this component is enabled
	 * @exception Exception
	 *                Any exception thrown by the executed Hook
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		if (!config.getEnabled()
				|| (parent.isSimulating() && getSimulatingState()
						.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE)))
			return false;

		try {
			handler.pushStackFrame(this);
			trigger("before_execute", work, null);
			return true;
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * This method implements the CallReply mode operation.
	 *
	 * @param meta
	 *            The work entry to send
	 * @exception Exception
	 *                this exception is thrown if this method fails
	 */
	public void callreply(com.ibm.di.entry.Entry meta) throws Exception {
		checkInitialize();
		try {
			omap.pushStackFrame(this);

			com.ibm.di.entry.Entry upd = new com.ibm.di.entry.Entry();
			omap.declareBean("work", meta);
			omap.declareBean("conn", upd);
			upd = omap.mapEntry(meta, upd);
			dumpObjects(upd, meta, null);

			trigger("before_functioncall", meta, upd);

			// Do the Call
			Object res = executeOperation(SimulationConfig.SIM_OP_PERFORM,
					meta, upd, null, null);

			stats.callreply();

			if (res == null) {
				setSuccessful(false);
				if (!trigger("no_reply", meta, null)) {
					log.exception("entry.not.found");
				}
			} else {
				// If not an entry then check for Attribute and FC's default
				// attribute name
				com.ibm.di.entry.Entry resultEntry = new com.ibm.di.entry.Entry();
				if (res instanceof com.ibm.di.entry.Entry) {
					resultEntry = (com.ibm.di.entry.Entry) res;
				} else if (res instanceof Attribute) {
					resultEntry.setAttribute((Attribute) res);
				} else if (defaultAttributeName != null) {
					resultEntry.setAttribute(defaultAttributeName, res);
				} else {
					throw new Exception(log.getString(
							"function.ret.nonentry.atrib", res.getClass()
									.getName(), res));
				}

				put(LAST_CONN, resultEntry);
				trigger("after_functioncall", meta, resultEntry);

				imap.declareBean("work", meta);
				imap.declareBean("conn", resultEntry);
				imap.mapEntry(resultEntry, meta);
				dumpObjects(resultEntry, meta, null);
			}
		} finally {
			omap.popStackFrame();
			checkTerminate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDebug(boolean debug) {
		synchronized (log) {
			super.setDebug(debug);
			if (function instanceof Function) {
				((Function) function).setDebug(debug);
			}
		}
	}

}
