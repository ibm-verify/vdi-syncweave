/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

import java.net.URL;

/**
 * Factory for password policy service object family.
 */
public interface PasswordPolicyFactory {

	/**
	 * Configure this factory.
	 * 
	 * @throws PolicyInitializationException
	 *             if required property names are missing or values are not
	 *             valid.
	 */
	void configure() throws PolicyInitializationException;

	/**
	 * Factory method for PasswordPolicyService instances.
	 * 
	 * @return new PasswordPolicyService instance.
	 * 
	 * @throws PolicyConnectionException
	 *             if underlying connection cannot be created.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PasswordPolicyService newPasswordPolicyService()
			throws PolicyConnectionException;

	/**
	 * Factory method for PasswordPolicyService instances.
	 * 
	 * @param conn
	 *            The conn to the policy service provider. Stored config is
	 *            ignored if using this overload.
	 * 
	 * @return new PasswordPolicyService instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if conn is null.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PasswordPolicyService newPasswordPolicyService(PolicyServiceConnection conn);

	/**
	 * Factory method for request objects.
	 * 
	 * @param op
	 *            The opeation type.
	 * 
	 * @return a new request object.
	 * 
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PolicyServiceRequest newPolicyServiceRequest(
			PolicyServiceMessage.ServiceOp op);

	/**
	 * Factory method for response objects.
	 * 
	 * @param req
	 *            The original request.
	 * 
	 * @return new response object instance.
	 * 
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PolicyServiceResponse newPolicyServiceResponse(PolicyServiceRequest req);

	/**
	 * Factory method for connection objects.
	 * 
	 * @return new connection based on stored configuration.
	 * 
	 * @throws PolicyConnectionException
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PolicyServiceConnection newPolicyServiceConnection()
			throws PolicyConnectionException;

	/**
	 * Factory method for connection objects.
	 * 
	 * @param url
	 *            The url
	 * 
	 * @return new connection based on URL. Stored config is ignored if this
	 *         overload is used.
	 * 
	 * @throws PolicyConnectionException
	 * @throws IllegalArgumentException
	 *             if url is null.
	 * @throws IllegalStateException
	 *             if not configured.
	 */
	PolicyServiceConnection newPolicyServiceConnection(URL url)
			throws PolicyConnectionException;

}
