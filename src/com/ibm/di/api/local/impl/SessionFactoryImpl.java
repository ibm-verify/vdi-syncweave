/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local.impl;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.authentication.AuthenticationInterface;
import com.ibm.di.api.authentication.LDAPAuthentication;
import com.ibm.di.api.exceptions.AuthenticationException;
import com.ibm.di.api.local.Session;
import com.ibm.di.api.security.LocalIdentity;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements methods for creating local Session.
 */
public class SessionFactoryImpl implements com.ibm.di.api.local.SessionFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * {@inheritDoc}
	 */
	public Session createSession() throws DIException {
		String userId = new LocalIdentity().getUserId();
		APIAuditor.sendAuthenticationAuditData(userId, true,
				APIAuditor.AUDIT_AUTH_ID_NO);
		return new SessionImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	public Session createSession(String aUserName, String aPassword)
			throws DIException {

		String notifID = null;
		String ldapGroups = null;

		if (APIEngine.isLDAPAuthenticationEnabled()
				|| APIEngine.isJAASAuthenticationEnabled()) {
			AuthenticationInterface authenticator = null;

			if (APIEngine.isLDAPAuthenticationEnabled()) {
				notifID = APIAuditor.AUDIT_AUTH_ID_LDAP;
				authenticator = APIEngine.getLDAPAuthenticator();
			} else {
				notifID = APIAuditor.AUDIT_AUTH_ID_JAAS;
				authenticator = APIEngine.getJAASAuthenticator();
			}
			try {
				if (authenticator instanceof LDAPAuthentication) {
					synchronized (authenticator) {
						authenticator.authenticate(aUserName, aPassword);
						ldapGroups = ((LDAPAuthentication) authenticator)
								.getUserGroups();
					}
				} else {
					authenticator.authenticate(aUserName, aPassword);
				}
			} catch (Exception e) {
				String funcmsg = sResHash.getString(
						"SEVER.API.AUTHENTICATION.FAILED.FOR.USER.1",
						new Object[] { aUserName, e.toString() });
				APIEngine.logError(funcmsg);
				APIAuditor.sendAuthenticationAuditData(aUserName, false,
						notifID);
				throw new com.ibm.di.api.exceptions.AuthenticationException(
						funcmsg, null, null);
			}
		} else {
			notifID = APIAuditor.AUDIT_AUTH_ID_CUSTOM;
			try {
				APIEngine.getAuthenticator().performCustomScriptAuthentication(
						aUserName, aPassword);
			} catch (AuthenticationException e) {
				APIAuditor.sendAuthenticationAuditData(aUserName, false,
						notifID);
				throw e;
			}
		}
		APIAuditor.sendAuthenticationAuditData(aUserName, true, notifID);
		if (ldapGroups != null) {
			return new SessionImpl(aUserName + ldapGroups);
		}
		return new SessionImpl(aUserName);
	}

}
