/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.authentication;

import java.util.Map;

import javax.security.auth.login.LoginContext;

/**
 * This class provides the capability of authentication against a JAAS module.
 * 
 * @since 7.0
 */
public class JAASAuthentication implements AuthenticationInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constant used when creating the {@link LoginContext} object.
	 */
	public static final String JAAS_APPLICATION = "TDI.JAAS.Authentication";

	/**
	 * No initialization required. The call to this method is ignored.
	 * 
	 * @param aConfigMap
	 *            this parameter is ignored.
	 * @throws Exception -
	 *             never.
	 */
	public void initialize(Map<String, String> aConfigMap) throws Exception {

	}

	/**
	 * This method does not actually authenticate the provided user.
	 * 
	 * @param aMap
	 *            this parameter is ignored.
	 * @throws Exception -
	 *             never.
	 */
	public void authenticate(Map<String, String> aMap) throws Exception {

	}

	/**
	 * This method creates a login context using the {@link #JAAS_APPLICATION}
	 * as the key name of the configuration.
	 * 
	 * @param aUserName
	 *            the user name to authenticate.
	 * @param aPassword
	 *            the password used for the authentication.
	 * @throws Exception
	 *             if the authentication fails.
	 */
	public void authenticate(String aUserName, String aPassword)
			throws Exception {
		LoginContext lc = new LoginContext(JAAS_APPLICATION,
				new AuthenticationCallbackHandler(aUserName, aPassword));
		lc.login();
	}

	/**
	 * There is nothing to close. The call to this method is ignored.
	 * 
	 * @throws Exception -
	 *             never.
	 */
	public void close() throws Exception {

	}

}
