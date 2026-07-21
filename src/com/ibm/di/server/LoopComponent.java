/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.entry.*;

/**
 * This class is used by an AssemblyLine to mark the beginning of a loop
 */
public class LoopComponent extends BranchingComponent {

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * The configuration of this LoopComponent.
	 */
	private LoopConfig config;

	/**
	 * The AssemblyLineComponent associated with this component.
	 */
	private AssemblyLineComponent alc;

	/**
	 * The attribute map of this component.
	 */
	private AttributeMapping map;

	/**
	 * The firstLoop attribute of the LoopComponent. Used to decide if this
	 * LoopComponent should begin a new Loop, or continue the old Loop
	 */
	private boolean firstLoop = true;

	/**
	 * DetermineS how to initialize the component's AssemblyLineComponent.
	 */
	private boolean didInitConnector = false;

	/**
	 * The stored Attribute values of the component.
	 */
	private Vector<Object> values;

	/**
	 * Determines whether the component is an iterator.
	 */
	private boolean isIterator;

	/**
	 * The first entry of the component.
	 */
	private com.ibm.di.entry.Entry firstEntry;

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Constructor for the LoopComponent object
	 * 
	 * @param parent
	 *            The AssemblyLine this LoopComponent belongs in
	 * @param name
	 *            The name of this LoopComponent
	 * @param config
	 *            The configuration for this LoopComponent
	 * 
	 * @exception Exception
	 *                Any Exception that might be thrown
	 */
	public LoopComponent(AssemblyLine parent, String name, LoopConfig config)
			throws Exception {
		super(parent, name, config);
		this.config = config;
		stats = new TaskStatistics();
	}

	/**
	 * This method initializes the LoopComponent
	 * 
	 * @throws Exception
	 *             If this is a Collection Loop, and attribute names are missing
	 */
	public void initialize() throws Exception {

		// Check loop type and execute
		log.debug("init.loop.component", Integer.valueOf(config.getLoopType()));
		switch (config.getLoopType()) {
		case LoopConfig.LOOP_CONDITIONS:
			super.initialize();
			break;
		case LoopConfig.LOOP_CONNECTOR_FC:
			loadConnector(config.getLoopConnector());
			break;
		case LoopConfig.LOOP_COLLECTION:
			if (config.getWorkAttributeName() == null
					|| config.getWorkAttributeName().length() == 0) {
				throw new Exception(sResHash
						.getString("no.work.entry.attribname.specified"));
			}
			if (config.getLoopAttributeName() == null
					|| config.getLoopAttributeName().length() == 0) {
				throw new Exception(sResHash
						.getString("no.loop.entry.attribname.specified"));
			}

			break;
		}
	}

	/**
	 * Loads a connector.
	 * 
	 * @param cc
	 *            the connector to be loaded
	 * @throws Exception
	 *             if a problem occurs
	 */
	private void loadConnector(ConnectorConfig cc) throws Exception {
		cc.setupInheritanceChain();
		cc.setParent(config);
		if (! cc.getEnabled())
			cc.setEnabled(true);

		// Set a large number if the user has not specified any lookup limit
		String limit = cc.getLimitOption();
		if (limit == null || limit.length() == 0)
			cc.setLimitOption("1000000000");

		// Set the initialize option to default
		cc.setInitializeOption(ConnectorConfig.COMP_INIT_DEFAULT);

		alc = parent.loadConnector(cc);
		alc.setIgnoreMissingHooksInLookup();

		// enable user to access raw connector
		connector = alc.connector;

		isIterator = ConnectorConfig.ITERATOR_MODE.equals(cc.getMode());

		map = new AttributeMapping(getName(), parent, parent.getLog(), parent
				.getScriptEngine());
		map.loadMap(cc.getAttributeMap(false));
	}

	/**
	 * Returns the next Entry if this is a Connector loop
	 * 
	 * @param meta
	 *            The work Entry
	 * @return The next Entry
	 * @throws Exception
	 *             Any Exception thrown by the Connector
	 */
	public com.ibm.di.entry.Entry getNextEntry(com.ibm.di.entry.Entry meta)
			throws Exception {
		com.ibm.di.entry.Entry entry = null;
		try {
			if (isIterator) {
				if (alc.willExecute(meta)) {
					if (!firstLoop)
						alc.resetStatus();
					entry = alc.getnext(meta);
					if (entry == null) {
						alc.trigger("end_of_data", meta, null);
						put(END_OF_DATA, "true");
					} else {
						put(LAST_CONN, alc.get(LAST_CONN));
						alc.trigger("get_ok", entry);
						alc.trigger("default_ok", entry);
					}
				}
			} else {
				if (firstEntry != null) {
					entry = firstEntry;
					firstEntry = null;
					put(LAST_CONN, alc.get(LAST_CONN));
					alc.trigger("lookup_ok");
				} else {
					entry = alc.getNextDuplicateEntry();
					// Call attribute map
					if (entry != null) {
						put(LAST_CONN, entry);
						entry = alc.mapEntry(meta, entry);
					}
				}
				if (entry != null) {
					alc.trigger("default_ok", entry);
				}
					
			}
		} catch (Exception e) {
			alc.handleException(isIterator ? "get" : "lookup", e, meta);
		}
		return entry;
	}

	/**
	 * Set the internal firstLoop variable to the parameter value. Used to
	 * decide if this LoopComponent should begin a new Loop, or continue the old
	 * Loop
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setFirstLoop(boolean value) {
		firstLoop = value;
	}

	/**
	 * Returns the source for this loop component (e.g.
	 * LoopConfig.LOOP_CONDITIONS/CONNECTOR_FC/COLLECTION)
	 * 
	 * @return An int containing the loop type.
	 */
	public int getLoopType() {
		return config.getLoopType();
	}

	/**
	 * This method will execute the Loop condition/connector and determine
	 * whether there are more loops to do.
	 * 
	 * @param work
	 *            The work Entry
	 * @return true if there are more loops to do
	 * @throws Exception
	 *             if a problem occurs
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {

		if (parent.isSimulating()
				&& getSimulatingState().equalsIgnoreCase(
						SimulationConfig.SIM_DISABLED_STATE))
			return false;

		if (firstLoop)
			stats.loopstart();

		boolean moreCycles = false;

		// Check loop type and execute
		// getNextEntry() might throw an Exception. Make sure firstLoop is reset anyway
		try {
			switch (config.getLoopType()) {
			case LoopConfig.LOOP_CONDITIONS:
				moreCycles = super.willExecute(work);
				break;
			case LoopConfig.LOOP_CONNECTOR_FC:
				if (firstLoop)
					checkConnectorInit(work);
				moreCycles = getNextEntry(work) != null;
				break;
			case LoopConfig.LOOP_COLLECTION:
				if (firstLoop)
					checkLoopAttribute(work);
				moreCycles = values.size() > 0;
				break;
			}

			if (firstLoop && !moreCycles)
				setSuccessful(false);
		} finally {
			firstLoop = true;
		}

		if (moreCycles)
			stats.loopcycles();

		// check for the simulation state just in case something is changed
		// during the loop check execution
		return moreCycles
				&& !(parent.isSimulating() && getSimulatingState()
						.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE));
	}

	/**
	 * Returns the type of this component.
	 * 
	 * @return ServerConstants.TYPE_LOOP
	 */
	public int getType() {
		return ServerConstants.TYPE_LOOP;
	}

	/**
	 * Copy values from work entry attribute
	 * 
	 * @param work
	 *            The work Entry
	 */
	public void checkLoopAttribute(com.ibm.di.entry.Entry work) {
		values = new Vector<Object>();
		Attribute a = work.getAttribute(config.getWorkAttributeName());
		if (a == null)
			return;
		for (Object value:a.getValuesAV()) {
			values.add(value);
		}
	}

	/**
	 * Check what to do with our connector when we enter the loop.
	 * 
	 * @param work
	 *            The work Entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void checkConnectorInit(com.ibm.di.entry.Entry work)
			throws Exception {

		log.debug("check.connector.init", work);
		alc.resetStatus();

		//
		if (config.getInitConnectorOption() == LoopConfig.OPTION_NONE) {
			return;
		}

		// Perform connector config attribute mapping
		setConnectorParams(work);

		// Only initialize raw connector if ALC already loaded
		if (!didInitConnector) {
			alc.doInitialize();
			didInitConnector = true;
		} else if (config.getInitConnectorOption() == LoopConfig.OPTION_INITIALIZE) {
			alc.doConnectorTerminate();
			alc.doConnectorInitialize();
		} else if (isIterator) {
			alc.doConnectorSelectEntries();
		}

		if (!isIterator) {
			try {
				firstEntry = alc.lookup(work);
				if ( firstEntry != null && alc.getDuplicateEntryCount() > 0 ) 
					alc.getFirstDuplicateEntry();
			} catch (Exception err) {
				alc.handleException("lookup", err, work);
			}
		}
	}

	/**
	 * Executes the attribute map for the connector params
	 * 
	 * @param work
	 *            The work Entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void setConnectorParams(com.ibm.di.entry.Entry work)
			throws Exception {
		log.debug("set.connector.params");
		com.ibm.di.entry.Entry params = new com.ibm.di.entry.Entry();
		map.declareBean("work", work);
		map.declareBean("conn", params);
		map.declareBean("thisConnector", this);
		map.declareBean("thisComponent", this);
		map.mapEntry(work, params);
		dumpObjects(params, work, null);

		for (String name : params.getAttributeNames()) {
			log.debug("setting.connector.parameter", new Object[] { name,
					params.getString(name) });
			alc.connector.setParam(name, params.getString(name));
		}
	}

	/**
	 * If necessary, move a value from the stored Attribute values to the work
	 * Entry
	 * 
	 * @param meta
	 *            The work Entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {

		switch (config.getLoopType()) {
		case LoopConfig.LOOP_CONDITIONS:
			break;
		case LoopConfig.LOOP_COLLECTION:
			meta.setAttribute(config.getLoopAttributeName(), values.remove(0));
			break;
		case LoopConfig.LOOP_CONNECTOR_FC:
			// already merged in willExecute
			setSuccessful(alc.wasSuccessful());
			put(HOOKS_INVOKED, alc.get(HOOKS_INVOKED));
			break;
		}
	}

	/**
	 * Releases resource.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void close() throws Exception {
		config = null;
		if (alc != null)
			alc.close();
		alc = null;
		map = null;
		values = null;
		super.close();
	}

	/**
	 * Returns the associated AssemblyLineComponent with the Connector. If this
	 * is not a Connector LoopComponent, returns null.
	 * 
	 * @return an AssemblyLineComponent, or null
	 * @since 7.0
	 */
	public AssemblyLineComponent getAssemblyLineComponent() {
		return alc;
	}
	
	/**
	 * Returns the LoopConnector Config. 
	 * @since 7.1.1.
	 * @Override
	 */	
	public ConnectorConfig getConfiguration() {
		if (config.getLoopType() != LoopConfig.LOOP_CONNECTOR_FC)
			return null;
		try {
			return config.getLoopConnector();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Used for setting the connector Param. 
	* @since 7.1.1.
	* @Override
	*/
	public void setConnectorParam(String param, Object value) {
		ConnectorConfig cc = getConfiguration();
		if (cc != null)
			cc.getConnectionConfig().setParameter(param, value);
	}
}
