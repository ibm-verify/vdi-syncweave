/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * 
 * This class is used to configure the AssemblyLine's simulation mode.
 * 
 */
public interface SimulationConfig extends BaseConfiguration {

	// Avilable components' states related to simulation.
	public final static String SIM_ENABLED_STATE = "Enabled";

	public final static String SIM_DISABLED_STATE = "Disabled";

	public final static String SIM_SIMULATED_STATE = "Simulated";

	public final static String SIM_PROXY_STATE = "Proxy";

	public final static String SIM_SCRIPTED_STATE = "Scripted";

	public final static String AL_SIMULATE_PROXY_NAME = "proxyAL.name";

	public final static String AL_SIMULATE_PROXY_SERVER = "proxyAL.server";

	public final static String AL_SIMULATE_PROXY_CONFIG = "proxyAL.config";

	public final static String AL_SIMULATE_PROXY_MODE = "proxyAL.mode";

	public final static String AL_SIMULATE_PROXY_DEBUG = "proxyAL.debug";

	public final static int SIM_OP_GET_NEXT_CLIENT = 0;

	public final static int SIM_OP_GET_NEXT_ENTRY = 1;

	public final static int SIM_OP_FIND_ENTRY = 2;

	public final static int SIM_OP_SELECT_ENTRIES = 3;

	public final static int SIM_OP_REPLY_ENTRY = 4;

	public final static int SIM_OP_QUERY_REPLY = 5;

	public final static int SIM_OP_PERFORM = 6;

	public final static int SIM_OP_PUT_ENTRY = 7;

	public final static int SIM_OP_MOD_ENTRY = 8;

	public final static int SIM_OP_DELETE_ENTRY = 9;

	public final static String[] SIM_OP_AS_STRING = { "getNextClient",
			"getNextEntry", "findEntry", "selectEntries", "replyEntry",
			"queryReply", "perform", "putEntry", "modEntry", "deleteEntry" };

	// ****** METHODS NAMES FOR THE APPROPRIATE COMPONENT MODES *******
	public static final String[] METHODS_FC_PERFORM = new String[] { "perform" };

	public static final String[] METHODS_CON_UPDATE = new String[] {
			"findEntry", "putEntry", "modEntry" };

	public static final String[] METHODS_CON_DELETE = new String[] {
			"findEntry", "deleteEntry" };

	public static final String[] METHODS_CON_DELTA = new String[] {
			"findEntry", "putEntry", "modEntry", "deleteEntry" };

	public static final String[] METHODS_CON_ITERATOR = new String[] {
			"selectEntries", "getNextEntry" };

	public static final String[] METHODS_CON_REPLY = new String[] { "replyEntry" };

	public static final String[] METHODS_CON_CALL_REPLY = new String[] { "queryReply" };

	public static final String[] METHODS_CON_SERVER = new String[] {
			"getNextEntry", "replyEntry" };

	public static final String[] METHODS_CON_LOOKUP = new String[] { "findEntry" };

	public static final String[] METHODS_CON_ADDONLY = new String[] { "putEntry" };

	// ****** END OF METHODS NAMES CONSTANTS *******

	public static final String SIMULATE_HOOK_NAME = "simulate_scipt";

	/**
	 * Sets the name of the AL to use as proxy.
	 * 
	 * @param name
	 *            The name of the AL
	 */
	public void setProxyALName(String name);

	/**
	 * Returns the name of the AL set as a proxy.
	 * 
	 * @return The name of the AL used as proxy or null if nothing was set.
	 */
	public String getProxyALName();

	/**
	 * Sets the remote server host name.
	 * 
	 * @param server
	 *            The name of the server
	 */
	public void setProxyALServer(String server);

	/**
	 * Returns the name of the remote server.
	 * 
	 * @return The name of the server
	 */
	public String getProxyALServer();

	/**
	 * Sets the ID of the configInstance to use
	 * 
	 * @param config
	 *            The ID of the ConfigInstance
	 */
	public void setProxyALConfigInstance(String config);

	/**
	 * Returns the configInstance ID
	 * 
	 * @return The ID of the configInstance
	 */
	public String getProxyALConfigInstance();

	/**
	 * Sets the mode the proxy AL will run in
	 * <ul>
	 * <li>0 - Run and wait for result</li>
	 * <li>1 - Run in background</li>
	 * <li>2 - Run in Manual mode</li>
	 * </ul>
	 * 
	 * @param mode
	 *            The mode the AL will run in
	 */
	public void setProxyALMode(int mode);

	/**
	 * Gets the mode the proxy AL will run in
	 * <ul>
	 * <li>0 - Run and wait for result</li>
	 * <li>1 - Run in background</li>
	 * <li>2 - Run in Manual mode</li>
	 * </ul>
	 * 
	 * @return The mode the AL will run in
	 */
	public int getProxyALMode();

	/**
	 * Sets proxy AL debug flag on/off
	 * 
	 * @param debug
	 *            The new value of the debug flag
	 */
	public void setProxyALDebug(boolean debug);

	/**
	 * Gets the debug flag state
	 * 
	 * @return The value of the proxy AL debug flag
	 */
	public boolean getProxyALDebug();

	/**
	 * Gets the proxySettings Config holder
	 * 
	 * @return The proxySettings
	 * @throws Exception
	 */
	public BaseConfiguration getProxySettings() throws Exception;

	/**
	 * This method checks the current simulation config settings and uses the
	 * ProxyAL setting to lookup the ProxyAL's AssibmlyLineConfig object. If
	 * that object is found it is updated with the missing
	 * {@link BranchingConfig} components. Any of the existing component in the
	 * AssemblyLineConfig that are user defined are not changed. If that object
	 * could not be found during the lookup process it is created.
	 * 
	 * @return the object that was updated with the necessary branching
	 *         components
	 */
	public AssemblyLineConfig createOrUpdateProxyAL() throws Exception;

	/**
	 * Return the simulation hook config object for the specified by
	 * <code>name</code> component
	 * 
	 * @param name
	 *            The name of the component which hook configuration to get
	 * @return The simulation hook specified by the name parameter or null if
	 *         the component is not found. If it is found but the requested
	 *         HookConfig does not exist then it is created and returned.
	 */
	public HookConfig getHook(String name);

	/**
	 * Returns the state of the specified by the name parameter Component
	 * 
	 * @param name
	 *            The name of the component
	 * @return the simulation state of the component as String or null if
	 *         undefined
	 */
	public String getComponentSimState(String name);

	/**
	 * Set the state of the Component specified by the name parameter
	 * 
	 * @param name
	 *            The name of the Component
	 * @param state
	 *            The new state of the Component
	 */
	public void setComponentSimState(String name, String state);

}
