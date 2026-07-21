/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import com.ibm.di.api.APIAuditor;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.authentication.AuthenticationInterface;
import com.ibm.di.api.authentication.LDAPAuthentication;
import com.ibm.di.api.exceptions.AuthenticationException;
import com.ibm.di.api.remote.impl.rmi.RMISocketFactory;
import com.ibm.di.api.remote.impl.rmi.SSLRMIServerSocketFactory;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements methods for creating remote Session.
 */
public class SessionFactoryImpl extends APIRemoteObject implements
		com.ibm.di.api.remote.SessionFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructor. Private, because instances are get with createInstance()
	 * 
	 * @param aClientSF
	 *            client socket factory
	 * @param aServerSF
	 *            server socket factory
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private SessionFactoryImpl(RMIClientSocketFactory aClientSF,
			RMIServerSocketFactory aServerSF) throws DIException,
			RemoteException {
		super(0, aClientSF, aServerSF);
	}

	/**
	 * {@inheritDoc}
	 */
	public com.ibm.di.api.remote.Session createSession() throws DIException,
			RemoteException {
		SessionImpl remoteSession = null;

		boolean authSuccessful = true;
		Exception ex = null;

		if (APIEngine.isSSLon()) {
			if (!APIEngine.isSSLClientAuthenticationOn()) {
				APIEngine
						.logErrorAndThrowException(sResHash
								.getString("SEVER.API.SSL.CLIENT.AUTHENTICATION.IS.DISABLED"));
			}
			String userId = null;

			try {
				userId = SSLRMIServerSocketFactory.getLocalThreadPrincipal()
						.toString();
			} catch (Exception e) {
				authSuccessful = false;
				ex = e;
			}
			APIAuditor.sendAuthenticationAuditData(userId, authSuccessful,
					APIAuditor.AUDIT_AUTH_ID_SSL);
			if (!authSuccessful) {
				APIEngine.logErrorAndThrowException(sResHash
						.getString("SEVER.API.UNABLE.TO.GET.USERID"), ex);

			}
			remoteSession = new SessionImpl(userId, APIEngine.getClientSF(),
					APIEngine.getServerSF());
			APIEngine.logInfo(sResHash.getString(
					"SEVER.API.REMOTE.SESSION.CREATED.FOR.USER", userId));

		} else {
			// Perform host based authentication
			boolean allow = false;
			try {
				allow = RMISocketFactory.allowConnection();
			} catch (Exception e) {
				APIEngine
						.logErrorAndThrowException(
								sResHash
										.getString("SEVER.API.ERROR.IN.HOST.AUTHENTICATION"),
								e);
			}

			APIAuditor.sendAuthenticationAuditData("remote user", allow,
					APIAuditor.AUDIT_AUTH_ID_HOST);
			if (!allow) {
				APIEngine.logErrorAndThrowException(sResHash
						.getString("SEVER.API.HOST.NOW.ALLOWED"));
			}

			remoteSession = new SessionImpl(APIEngine.getClientSF(), APIEngine
					.getServerSF());
			APIEngine
					.logInfo(sResHash
							.getString("SEVER.API.REMOTE.SESSION.CREATED.WITH.LOCAL.IDENTITY"));
		}

		return remoteSession;
	}

	/**
	 * {@inheritDoc}
	 */
	public com.ibm.di.api.remote.Session createSession(String aUserName,
			String aPassword) throws DIException, RemoteException {
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
				APIAuditor.sendAuthenticationAuditData(aUserName, false,
						notifID);

				String funcmsg = sResHash.getString(
						"SEVER.API.AUTHENTICATION.FAILED.FOR.USER.1",
						new Object[] { aUserName, e.toString() });
				APIEngine.logError(funcmsg);
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

		SessionImpl remoteSession = null;
		if (ldapGroups != null) {
			remoteSession = new SessionImpl(aUserName + ldapGroups, APIEngine
					.getClientSF(), APIEngine.getServerSF());
		} else {
			remoteSession = new SessionImpl(aUserName, APIEngine.getClientSF(),
					APIEngine.getServerSF());
		}
		APIEngine.logInfo(sResHash.getString(
				"SEVER.API.REMOTE.SESSION.CREATED.FOR.USER.1", aUserName));
		return remoteSession;
	}

	/**
	 * Creates new instance of this class.
	 * 
	 * @return SessionFactoryImpl object
	 * @throws DIException
	 *             if Runtime or Security exception occurs.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public static SessionFactoryImpl createInstance() throws DIException,
			RemoteException {
		return new SessionFactoryImpl(APIEngine.getClientSF(), APIEngine
				.getServerSF());
	}

}
