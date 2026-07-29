/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy;

import java.util.List;

/**
 * Provides features to enable password value conformance check and
 * synchonization against externally defined policies.
 */
public interface PasswordPolicyService {

	/**
	 * Set the connection.
	 * 
	 * @param conn -
	 *            The connection.
	 * 
	 * @throws IllegalArgumentException
	 *             if conn is <code>null</code>.
	 */
	void setConnection(PolicyServiceConnection conn);

	/**
	 * Ensure the password policy subsystem is available. For example, check
	 * that network connectivity is available. If used, this method should be
	 * called before validatePassword(List).
	 * 
	 * @return true if policy subsystem is ready.
	 */
	boolean ready();

	/**
	 * Check the password values conform to policy.
	 * 
	 * @param userName -
	 *            The user name, e.g. jdoe or eruid=jdoe
	 * 
	 * @param passwordValues -
	 *            List of associated password values to be checked. List must
	 *            have length > 0
	 * 
	 * @return true if all passwords conform to policy.
	 * 
	 * @throws PolicyConnectionException
	 *             if connection to policy service is lost.
	 * @throws MalformedResponseException
	 *             if a policy service response cannot be parsed.
	 * @throws IllegalArgumentException
	 *             if passwordValues.length() <= 0.
	 */
	boolean validatePassword(String userName, List passwordValues)
			throws PolicyConnectionException, MalformedResponseException;

	/**
	 * Propagate the password values for the given user.
	 * 
	 * @param userName -
	 *            The user name, e.g. jdoe or eruid=jdoe
	 * 
	 * @param passwordValues -
	 *            List of associated password values to be synchronized. List
	 *            must have length > 0
	 * 
	 * @throws PolicyConnectionException
	 *             if connection to policy service is lost.
	 * @throws MalformedResponseException
	 *             if a policy service response cannot be parsed.
	 * @throws PasswordSynchException
	 *             if any single password value cannot be synchronized, e.g.
	 *             connection failure.
	 * @throws IllegalArgumentException
	 *             if passwordValues.length() <= 0.
	 */
	void synchronizePassword(String userName, List passwordValues)
			throws PolicyConnectionException, MalformedResponseException,
			PasswordSynchException;

	/**
	 * Perform and required post construction initialization. For example,
	 * establish network connections.
	 * 
	 * @throws PolicyInitializationException
	 */
	void initialize() throws PolicyInitializationException;

	/**
	 * Perform and required cleanup in preparation for shutdown.
	 */
	void terminate();

	/**
	 * Get the status message returned from the previous invocation of
	 * {@link #synchronizePassword(String, List)} or
	 * {@link #validatePassword(String, List)}.
	 * 
	 * @return The status message from the service, or <code>null</code> if no
	 *         status was returned.
	 */
	String getLastStatusMessage();

}
