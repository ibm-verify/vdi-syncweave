/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.itim.policy.impl;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.ibm.di.plugin.pwstore.itim.policy.MalformedResponseException;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordPolicyService;
import com.ibm.di.plugin.pwstore.itim.policy.PasswordSynchException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyConnectionException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyInitializationException;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceConnection;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceMessage;
import com.ibm.di.plugin.pwstore.itim.policy.PolicyServiceResponse;

/**
 * The class represents a facade that supports password policy validation check,
 * and password synchronization against ITIM. Instances must be created using a
 * PasswordPolicyFactory; {@link ITIMPasswordPolicyFactoryImpl}
 */
public final class ITIMPasswordPolicyImpl implements PasswordPolicyService {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String ERUID_PREFIX = "eruid=";

	private String itimPrincipalName;

	private String itimPrincipalPassword;

	private String itimSourceDn;

	private PolicyServiceConnection policyConn;

	private String statusMessage;

	ITIMPasswordPolicyImpl() {
		super();
	}

	void setItimPrincipalName(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}

		itimPrincipalName = name;
	}

	String getItimPrincipalName() {
		return itimPrincipalName;
	}

	void setItimPrincipalPassword(String pass) {
		if (pass == null) {
			throw new IllegalArgumentException();
		}

		itimPrincipalPassword = pass;
	}

	String getItimPrincipalPassword() {
		return itimPrincipalPassword;
	}

	void setItimSourceDn(String val) {
		if (val == null) {
			throw new IllegalArgumentException();
		}

		itimSourceDn = val;
	}

	String getItimSourceDn() {
		return itimSourceDn;
	}

	private void checkObjectState() throws IllegalStateException {
		if (getItimPrincipalName() == null
				|| getItimPrincipalPassword() == null
				|| getItimSourceDn() == null || policyConn == null) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Set the connection.
	 * 
	 * @param conn
	 *            - The connection.
	 * 
	 * @throws IllegalArgumentException
	 *             if conn is <code>null</code>.
	 */
	public void setConnection(PolicyServiceConnection conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}

		policyConn = conn;
	}

	/**
	 * Ensure the password policy subsystem is available. Check that
	 * communication with ITIM is working. If used, this method should be called
	 * before validatePassword(List).
	 * 
	 * @return true if policy subsystem is ready.
	 * 
	 * @throws IllegalStateException
	 *             of {@link #getItimPrincipalName()} or
	 *             {@link #getItimPrincipalPassword()} equal <code>null</code>.
	 */
	public boolean ready() {
		boolean result = true;
		try {
			checkObjectState();
		} catch (IllegalStateException x) {
			result = false;
		}

		return result;
	}

	private String createItimUserDn(String proposedUid) {
		String result = proposedUid;
		if (!proposedUid.startsWith(ITIMPasswordPolicyImpl.ERUID_PREFIX)) {
			StringBuffer newResult = new StringBuffer();
			newResult.append(ITIMPasswordPolicyImpl.ERUID_PREFIX);
			newResult.append(proposedUid);
			result = newResult.toString();
		}

		return result;
	}

	/**
	 * Check against ITIM that the password values conform to policy.
	 * 
	 * @param userName
	 *            - The user name, e.g. jdoe or eruid=jdoe
	 * 
	 * @param passwordValues
	 *            - List of associated password values to be checked. List must
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
	 * @throws IllegalStateException
	 *             of {@link #getItimPrincipalName()} or
	 *             {@link #getItimPrincipalPassword()} equal <code>null</code>.
	 */
	public boolean validatePassword(String userName, List passwordValues)
			throws PolicyConnectionException, MalformedResponseException {

		//
		// APAR - IO07550
		//
		if (passwordValues == null) {
			throw new IllegalArgumentException();
		}

		if (userName == null) {
			throw new IllegalArgumentException();
		}

		if (passwordValues.size() == 0) {
			throw new IllegalArgumentException();
		}

		checkObjectState();

		String userDn = createItimUserDn(userName);

		PolicyServiceResponse resp = null;

		Iterator i = passwordValues.iterator();

		// ADD TRACE
		while (i.hasNext()) {
			String pass = (String) i.next();
			ITIMPolicyServiceRequestImpl req = new ITIMPolicyServiceRequestImpl();
			req.setOperation(PolicyServiceMessage.ServiceOp.VALIDATE_PASSWORD);
			req.setUserName(userDn);
			req.setPrincipalName(this.getItimPrincipalName());
			req.setPrincipalPswd(this.getItimPrincipalPassword());
			req.setSourceDn(this.getItimSourceDn());
			req.setPassword(pass);

			resp = policyConn.sendReceive(req);

			if (!resp.isSuccess()) {
				statusMessage = resp.getResponseMessage();
				return false;
			}
		}
		return true;
	}

	/**
	 * Synchronize the password values via ITIM.
	 * 
	 * @param userName
	 *            - The user name whose password values will be synchronized.
	 * 
	 * @param passwordValues
	 *            - List of associated password values to be checked. List must
	 *            have length > 0
	 * 
	 * @throws PolicyConnectionException
	 *             if connection to policy service is lost.
	 * @throws MalformedResponseException
	 *             if a policy service response cannot be parsed.
	 * @throws PasswordSynchException
	 *             if any single password value cannot be synchronized, e.g.
	 *             connection failure.
	 * @throws IllegalArgumentException
	 *             if passwordValues.length() <= 0, or either parameter
	 *             reference is null..
	 * @throws IllegalStateException
	 *             of {@link #getItimPrincipalName()} or
	 *             {@link #getItimPrincipalPassword()} equal <code>null</code>.
	 */
	public void synchronizePassword(String userName, List passwordValues)
			throws PolicyConnectionException, MalformedResponseException,
			PasswordSynchException {
		if (passwordValues.size() == 0 || userName == null) {
			throw new IllegalArgumentException();
		}

		checkObjectState();

		String userDn = createItimUserDn(userName);
		PolicyServiceResponse resp = null;
		ListIterator i = passwordValues.listIterator();

		while (i.hasNext()) {
			String pass = (String) i.next();
			ITIMPolicyServiceRequestImpl req = new ITIMPolicyServiceRequestImpl();
			req.setOperation(PolicyServiceMessage.ServiceOp.SYNC_PASSWORD);
			req.setUserName(userDn);
			req.setPrincipalName(this.getItimPrincipalName());
			req.setPrincipalPswd(this.getItimPrincipalPassword());
			req.setSourceDn(this.getItimSourceDn());
			req.setPassword(pass);
			resp = policyConn.sendReceive(req);
			if (!resp.isSuccess()) {
				statusMessage = resp.getResponseMessage();
				PasswordSynchException x = new PasswordSynchException(resp
						.getResponseMessage());
				x.initUnSynchronizedPasswords(i, passwordValues);
				throw x;
			}
		}
	}

	/**
	 * Perform and required post construction initialization. For example,
	 * establish network connections.
	 * 
	 * @throws PolicyInitializationException
	 */
	public void initialize() throws PolicyInitializationException {
		// no op
	}

	/**
	 * Perform and required cleanup in preparation for shutdown.
	 */
	public void terminate() {
	}

	/**
	 * Get the status message returned from the previous invocation of
	 * {@link #synchronizePassword(String, List)} or
	 * {@link #validatePassword(String, List)}.
	 * 
	 * @return The status message from the service, or <code>null</code> if no
	 *         status was returned.
	 */
	public String getLastStatusMessage() {
		return statusMessage;
	}
}
