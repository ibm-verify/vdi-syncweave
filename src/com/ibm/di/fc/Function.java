/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.awt.Component;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.VersionInfoInterface;

/**
 * This abstract class contains a default implementation of all methods in the
 * FunctionInterface except the the <i>perform(obj)</i> method. Every Function
 * Component implementation class should inherit from this class.
 */

public abstract class Function implements FunctionInterface,
		VersionInfoInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * {@link BaseConfiguration}
	 */
	private BaseConfiguration configuration;

	/**
	 * {@link Log}
	 */
	protected Log logger;

	/**
	 * Initialized status flag.
	 */
	private boolean _initialized = false;

	/**
	 * User defined context.
	 */
	private Object mContext = null;
	
	/**
	 * Whether this component is in debug mode. Debug messages will be logged
	 * only in debug mode. May be accessed by different threads.
	 */
	private volatile boolean debugEnabled = false;

	/**
	 * The RSInterface from which we get additional configuration data
	 */
	private RSInterface myRS;

	/**
	 * This method is/should be called once after the object has been given its
	 * configuration
	 * 
	 * @param object
	 * @throws Exception
	 */
	public void initialize(Object object) throws Exception {
		_initialized = true;
	}

	/**
	 * This method is/should be called once before the object is released
	 * 
	 * @throws Exception
	 */
	public void terminate() throws Exception {
		_initialized = false;
	}

	/**
	 * This method is/should be called once after the object has been given its
	 * configuration
	 * 
	 * @param configuration
	 *            {@link BaseConfiguration}
	 */
	public void setConfiguration(BaseConfiguration configuration) {
		this.configuration = configuration;
		setDebug(configuration.getDebug(false));
	}

	/**
	 * This method returns the function's current configuration
	 * 
	 * @return BaseConfiguration
	 */
	public BaseConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getUI() {
		return null;
	}

	/**
	 * This method modifies the schema. The intent is to allow the FC to provide
	 * a dynamic schema definition based on a given configuration. The default
	 * implementation simply ignores the call.
	 * 
	 * @param config {@link FunctionConfig}
	 * @return boolean
	 * @throws Exception
	 */
	public boolean updateSchema(FunctionConfig config) throws Exception {
		return false; // Did not change it
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParam(String param) {
		if (configuration != null)
			return configuration.getParameter(param);
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParam(String param, Object value) {
		if (configuration != null)
			configuration.setParameter(param, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void logmsg(String msg) {
		if (logger != null)
			logger.loginfo(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLog(Log logger) {
		this.logger = logger;
	}

	/**
	 * {@inheritDoc}
	 */
	public Log getLog() {
		return logger;
	}

	//
	// Convenience methods
	//

	/**
	 * Calls initialize(null)
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		initialize(null);
	}

	/**
	 * Writes a message to the log if debug/detailed logging is turned on
	 * 
	 * @param aMsg
	 *            The message
	 */
	public void debug(String aMsg) {
		if (logger != null && debugEnabled) {
			logger.logdebug(aMsg);
		}
	}

	/**
	 * Verifies that initialize() has been called at least once after the FC has
	 * been loaded or the terminate() method has been called.
	 * 
	 * @exception java.lang.Exception
	 *                thrown if initialize() hasn't been called
	 */
	public void verifyInitialized() throws Exception {
		if (!_initialized) {
			throw new Exception(sResHash
					.getString("FUNCTION.FUNCNOT.INITIALIZED.ERROR"));
		}
	}

	/**
	 * Retrieves used defined context.
	 * @return the user defined context.
	 */
	public Object getContext() {
		return mContext;
	}

	/**
	 * Sets the user defined context.
	 * 
	 * @param aContext
	 *            String
	 */
	public void setContext(Object aContext) {
		mContext = aContext;
	}

	/** {@inheritDoc} */
	public Object querySchema(Object source) throws Exception {
		return null;
	}
	
	/**
	 * @return Whether this component is in debug mode. May be called by
	 *         different threads.
	 * @since 7.0
	 */
	public boolean getDebug() {
		return debugEnabled;
	}

	/**
	 * <p>
	 * Modify the debug mode setting of this component. May be called by
	 * different threads.
	 * </p>
	 * 
	 * <p>
	 * This method is for internal use only. Do not call it from user code.
	 * </p>
	 * 
	 * @param debug
	 *            the debug mode setting
	 * 
	 * @since 7.0
	 */
	public void setDebug(boolean debug) {
		debugEnabled = debug;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setRSInterface(RSInterface rsi) {
		myRS = rsi;
	}

	/**
	 * {@inheritDoc}
	 */
	public RSInterface getRSInterface() {
		return myRS != null ? myRS : RS.getServer();
	}

}
