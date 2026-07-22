/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;

/**
 * This is the interface implemented by Function objects.
 */

public interface FunctionInterface {

	/**
	 * Called once to initialize the function
	 * 
	 * @param obj
	 *            this object provides information to the Function Component
	 *            needed on initialization
	 * @exception Exception
	 *                An exception is thrown if the initialization fails.
	 */
	public void initialize(Object obj) throws Exception;

	/**
	 * This is the main method of a Function Component. This method is called
	 * for the Function Component to actually do its job. The implementation of
	 * this methods performs the function which the Function Component was
	 * created to do.
	 * 
	 * @param obj
	 *            the input to the function
	 * @return the output from the function
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Object perform(Object obj) throws Exception;

	/**
	 * The implementation of this method must take care to cleanup any resources
	 * which the Function Component has allocated during its operation. Software
	 * modules which use a Function Component must make sure they call this
	 * method so that all resources used by the Function Component are freed
	 * properly.
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void terminate() throws Exception;

	/**
	 * This method returns null.
	 * 
	 * @return null
	 * @deprecated
	 */
	public java.awt.Component getUI();

	/**
	 * Gets the configuration attribute of the FunctionInterface object
	 * 
	 * @return The configuration value
	 */
	public BaseConfiguration getConfiguration();

	/**
	 * Sets the configuration attribute of the FunctionInterface object
	 * 
	 * @param config
	 *            The new configuration value
	 */
	public void setConfiguration(BaseConfiguration config);

	/**
	 * This method modifies the schema in the provided configuration. The intent
	 * is to allow the FC to provide a schema definition dynamically based on a
	 * given configuration.
	 * 
	 * @param config
	 *            {@link FunctionConfig}
	 * @return boolean
	 * @throws Exception
	 */
	public boolean updateSchema(FunctionConfig config) throws Exception;

	/**
	 * Gets a named parameter in the configuration.
	 * 
	 * @param param
	 *            the name of the parameter whose value this method returns
	 * @return the value of the parameter
	 */
	public Object getParam(String param);

	/**
	 * Sets a named parameter in the configuration.
	 * 
	 * @param param
	 *            The new parameter's name
	 * @param value
	 *            The new parameter value
	 */
	public void setParam(String param, Object value);

	/**
	 * Logs a message to the currently used log
	 * 
	 * @param msg
	 *            The message appearing in the log
	 */
	public void logmsg(String msg);

	/**
	 * Sets the logger object to use in this FC
	 * 
	 * @param logger
	 *            The log object
	 */
	public void setLog(Log logger);

	/**
	 * Returns the logger object in use by this FC
	 * 
	 * @return The log object
	 */
	public Log getLog();

	/**
	 * If debug is turned on, logs a message to the currently used
	 * log.
	 * 
	 * @param aMsg
	 *            The debug message appearing in the log
	 */
	public void debug(String aMsg);

	/**
	 * Retrieves used defined context.
	 * @return the user defined context.
	 */
	public Object getContext();

	/**
	 * Sets the user defined context.
	 * 
	 * @param aContext
	 *            String
	 */
	public void setContext(Object aContext);

	/**
	 * This method translates to whatever means a function component has to
	 * discover schema for a connection. The specific FC may implement this, in
	 * which case a Vector of Entry objects is returned for each
	 * column/attribute it discovered.
	 * <p>
	 * 
	 * Each Entry in the Vector returned should contain the following
	 * attributes:
	 * <p>
	 * <table border="1">
	 * <tr>
	 * <th>Name</th>
	 * <th>Value</th>
	 * </tr>
	 * <tr>
	 * <td>name</td>
	 * <td>The name of the column/attribute/field ....</td>
	 * </tr>
	 * <tr>
	 * <td>syntax</td>
	 * <td>The syntax or expected value type</td>
	 * </tr>
	 * <tr>
	 * <td>size</td>
	 * <td>If specified this will give the user a hint as to how long the field
	 * may be</td>
	 * </tr>
	 * </table>
	 * <p>
	 * 
	 * @param source
	 *            The object on which to discover schema. This may be an Entry
	 *            or a string value. Boolean value will tell which schema to
	 *            discover: true - InputMapSchema, false - OutputMapSchema. If
	 *            not specified the default will be true. Might be a
	 *            FunctionConfig object which will be set as the configuration
	 *            of this object. Could be an array of objects (i.e. Object[]).
	 *            Only the first object of a specific type will be considered,
	 *            the rest (if any) will be ignored.
	 * 
	 * @return A Vector of com.ibm.di.entry.Entry objects describing each entity
	 * @throws Exception
	 * @see com.ibm.di.entry.Entry
	 * @see java.util.Vector
	 */
	public Object querySchema(Object source) throws Exception;
	
	/**
	 * Sets the RSInterface object for this Function.
	 * @since 7.0
	 */
	public void setRSInterface(RSInterface rsi);

	/**
	 * Returns the current RSInterface object in use by this Function.
	 * If not set, return <code>com.ibm.di.server.RS.getServer()</code>
	 * @since 7.0
	 * 
	 * @return Function's RSInterface object
	 */
	public RSInterface getRSInterface();

}
