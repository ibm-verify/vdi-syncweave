/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SimulationConfig;

/**
 * This class is used by the AssemblyLine, it contains a standalone Attribute
 * map
 */
public class AttributeMapComponent extends AssemblyLineComponent {

	/**
	 * Constructor for the AttributeMapComponent object
	 * 
	 * @param parent
	 *            The AssemblyLine that contains this AttributeMapComponent
	 * @param name
	 *            The name of this component
	 * @param config
	 *            The configuration for this component
	 * 
	 * @exception Exception
	 *                Any Exception that might occur
	 */
	public AttributeMapComponent(AssemblyLine parent, String name,
			ALMappingConfig config) throws Exception {
		super();
		this.parent = parent;
		this.config = config;
		log = new Log(parent.getLog());
		log.setDebug(parent.getLog().getDebug());
		log.setPrefix("[" + name + "] ");
		setName(name);
	}

	/**
	 * This method creates the AttributeMapping for this component
	 * 
	 * @exception Exception
	 *                Any Exception that might be thrown
	 */
	public void initialize() throws Exception {
		useInputMap(null);
	}

	/**
	 * This method always returns true
	 * 
	 * @param work
	 *            The work Entry
	 * @return always true
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		return !(((AssemblyLine) parent).isSimulating() && getSimulatingState()
				.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE));
	}

	/**
	 * Returns the configuration for this component
	 * 
	 * @return the configuration for this component
	 */
	public ConnectorConfig getConfiguration() {
		return config;
	}

	/**
	 * Returns the type of this component
	 * 
	 * @return ServerConstants.TYPE_ATTRIBUTEMAP
	 */
	public int getType() {
		return ServerConstants.TYPE_ATTRIBUTEMAP;
	}

	/**
	 * Does the attribute mapping in every cycle
	 * 
	 * @param meta
	 *            The work Entry
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {
		try {
			imap.pushStackFrame(this);
			imap.declareBean("work", meta);
			imap.mapEntry(meta.clone(), meta, false);
			dumpObjects(null, meta, null);
		} finally {
			imap.popStackFrame();
		}
	}

	/**
	 * Handles any Exception. Since Attribute Maps don't have hooks, this method
	 * just rethrows the Exception
	 * 
	 * @param oper
	 *            The operation that was performed. Ignored
	 * @param e
	 *            The Throwable that was thrown
	 * @param meta
	 *            The work Entry
	 * @throws Exception
	 *             Rethrows the Throwable, if necessary wrapped in an Exception
	 */
	public void handleException(String oper, Throwable e,
			com.ibm.di.entry.Entry meta) throws Exception {
		if (e instanceof Exception)
			throw (Exception) e;
		else
			throw new Exception(e);
	}

	/**
	 * Performs any hook. Since there are no hooks, this method is never called
	 * 
	 * @param oper
	 *            The operation that was performed
	 * @param work
	 *            The work Entry
	 * @param conn
	 *            The conn Entry
	 * @return always false
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) {
		return false;
	}

	/**
	 * Performs any hook. Since there are no hooks, this method is never called
	 * 
	 * @param oper
	 *            The operation that was performed
	 * @param work
	 *            The work Entry
	 * 
	 * @return always false
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) {
		return false;
	}

	/**
	 * This method closes the AttributeMapComponent
	 */
	public void close() throws Exception {

		if (imap != null) {
			imap.unload();
			imap = null;
		}
		parent = null;
		config = null;
		log = null;
	}

	//
	// Override the following methods to always use the input map
	//

	@Override
	public void useMap(String attributeMapName) throws Exception {
		super.useMap(attributeMapName, true);
	}

	@Override
	public void useMap(String attributeMapName, boolean input) throws Exception {
		super.useMap(attributeMapName, true);
	}

	@Override
	public void useOutputMap(AttributeMapConfig map) throws Exception {
		super.useInputMap(map);
	}

	@Override
	public void useAttributeMap(String fileName, boolean input, boolean extend) throws Exception{
		super.useAttributeMap(fileName, true, extend);
	}
}
