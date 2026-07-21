/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Configuration for a Loop Component in an AssemblyLine.
 */
public interface LoopConfig extends BranchingConfig {

	/**
	 * While conditions are true
	 */
	public final static int LOOP_CONDITIONS = 0;

	/**
	 * For each Entry returned by connector or FC
	 */
	public final static int LOOP_CONNECTOR_FC = 1;

	/**
	 * For each value of an Attribute in the work Entry
	 */
	public final static int LOOP_COLLECTION = 2;

	/**
	 * Initialize once in the AssemblyLine
	 */
	public final static int OPTION_NONE = 0;

	/**
	 * Initialize the Connector every time the Loop is entered
	 */
	public final static int OPTION_INITIALIZE = 1;

	/**
	 * Initialize the connector and select every time the Loop is entered.
	 */
	public final static int OPTION_SELECT = 2;

	/**
	 * Returns the Loop connector configuration
	 */
	public ConnectorConfig getLoopConnector() throws Exception;

	/**
	 * Returns the type of loop we are doing.
	 * The Loop type will be one of LOOP_CONDITIONS, LOOP_CONNECTOR_FC or LOOP_COLLECTION.
 	*/
	public int getLoopType();

	/**
	 * Sets the loop type.
	 * One of LOOP_CONDITIONS, LOOP_CONNECTOR_FC or LOOP_COLLECTION.
	 */
	public void setLoopType(int type);

	/**
	 * Returns the initialization option.
	 * Only interesting for a Connector Loop.
	 * One of OPTION_NONE, OPTION_INITIALIZE, OPTION_SELECT
	 */
	public int getInitConnectorOption();

	/**
	 * Sets the initialization option.
	 * One of OPTION_NONE, OPTION_INITIALIZE, OPTION_SELECT
	 */
	public void setInitConnectorOption(int option);

	/**
	 * Returns the name of the work attribute whose values to loop over
	 * Only interesting for type LOOP_COLLECTION.
	 */
	public String getWorkAttributeName();

	/**
	 * Sets the name of the work attribute whose values to loop over
	 */
	public void setWorkAttributeName(String name);

	/**
	 * Returns the name of the loop attribute that gets one value from the work
	 * attribute for each loop.
	 * Only interesting for type LOOP_COLLECTION.
	 */
	public String getLoopAttributeName();

	/**
	 * Sets the name of the loop attribute that gets one value from the work
	 * attribute for each loop
	 */
	public void setLoopAttributeName(String name);

}
