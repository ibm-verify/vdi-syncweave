/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * Policy Service response message type.
 */
public interface PolicyServiceResponse extends PolicyServiceMessage {

	/**
	 * The request to which this repsonse has been received. Use for correlation
	 * purposes.
	 * 
	 * @return The original request message instance.
	 */
	PolicyServiceRequest getRequest();

	/**
	 * Test if this response represents a successful
	 * 
	 * @return true if response indicates success.
	 */
	boolean isSuccess();

	/**
	 * The response descriptive text from ITIM.
	 * 
	 * @return The text string if one was present in the response,
	 *         <code>null</code> otherwise.
	 */
	String getResponseMessage();

	/**
	 * Test if the response format and mandatory data are present and correct.
	 * 
	 * @return true if message is valid.
	 */
	boolean isValid();

}
