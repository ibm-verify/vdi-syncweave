/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A configuration object that describes a single reconnect rule.
 * 
 * @since 7.0
 */
public interface ReconnectRuleConfig extends BaseConfiguration {

	/**
	 * @return What action should be taken if an error occurs and that error
	 *         matches the rule. Will either be null, empty string or one of
	 *         'reconnect' or 'error'. If null or empty, the engine that
	 *         interprets the rule should use its default action.
	 */
	public String getAction();

	/**
	 * @return The Java class of exceptions to which this rule applies. If null
	 *         or empty, the rule applies to all classes of exceptions.
	 */
	public String getExceptionClass();

	/**
	 * @return A regular expression that matches the messages of exceptions to
	 *         which this rule applies. If null or empty, the rule applies to
	 *         all exception messages.
	 */
	public String getExceptionMessageRegExp();

	/**
	 * Validate the contents of this reconnect rule.
	 * 
	 * @exception Exception
	 *                If the reconnect action is neither null, an empty string,
	 *                'error' nor 'reconnect'. If the class definition of the
	 *                exception class is not available to the JVM.
	 */
	public void validate() throws Exception;

}
