/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Information about Reconnect for a Connector
 * 
 */
public interface ReconnectConfig extends BaseConfiguration {

	/*
	 * Should we automatically reconnect
	 */
	public boolean getAutoReconnect();

	/*
	 * Should we automatically reconnect if we fail to initialize the Connector
	 */
	public boolean getInitReconnect();

	/**
	 * Number of retries
	 */
	public int getRetries();

	/**
	 * Delay between retries (in seconds)
	 */
	public int getDelay();

	/**
	 * Should we automatically skip forward after a Reconnect.
	 * This is only meaningful for Iterators.
	 */
	public boolean getAutoSkipForward();

	/**
	 * Set the value of autoSkipForward, unless it is already set to a value.
	 * For internal use.
	 * @param value The new value
	 */
	public void setAutoSkipForwardUnlessAlreadySet(Object value);

	/**
	 * Remove all parameter values, to prepare for inheritance
	 */
	public void removeParameterValues();
	
	
	/**
	 * @return The container with the reconnect rules of this configuration object.
	 * @since 7.0
	 */
	public ContainerConfig getReconnectRules();
	
	/**
	 * Create a new reconnect rule and add it to the internal container.
	 * @return The new reconnect rule.
	 * @exception Exception A problem while creating the rule.
	 * @since 7.0
	 */
	public ReconnectRuleConfig newReconnectRule() throws Exception;

	/**
	 * @return true if Failover is enabled for this Connector.
	 * @since 7.2
	 */
	public boolean getFailoverOption();
	
	/**
	 * @return the name of the Connector used for Failover.
	 * @since 7.2
	 */
	public String getFailoverConnectorName();
	
	/**
	 * @return seconds to wait before attempting an automatic Failback.
	 * @since 7.2
	 */
	public int getFailbackAfter();
}
