/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

/**
 * Represents a low level connection to the actual password policy service
 * provider. Typical implementations will encapsulate an HTTP/TCP socket
 * connection, however any other conceptual connection can be supported.
 */
public interface PolicyServiceConnection {

	/**
	 * Send and receive message against the password policy service.
	 * 
	 * @param request
	 * @return The response message.
	 * @throws PolicyConnectionException
	 * @throws MalformedResponseException
	 */
	PolicyServiceResponse sendReceive(PolicyServiceRequest request)
			throws PolicyConnectionException, MalformedResponseException;
}
