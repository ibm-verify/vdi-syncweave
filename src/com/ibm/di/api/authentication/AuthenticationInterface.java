/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.authentication;

import java.util.Map;

/**
 * This is the base interface used by the supported authentication mechanisms.
 */
public interface AuthenticationInterface {

	/**
	 * This method initializes the authentication mechanism.
	 * 
	 * @param aConfigMap
	 *            the map containing properties used for the authentication.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Map<String, String> aConfigMap) throws Exception;

	/**
	 * This method authenticates the user using the entries in the provided map.
	 * 
	 * @param aMap
	 *            the map containing the credentials used for the
	 *            authentication.
	 * @throws Exception
	 *             if the authentication fails.
	 */
	public void authenticate(Map<String, String> aMap) throws Exception;

	/**
	 * This method authenticates the user specified by the passed arguments.
	 * 
	 * @param aUserName
	 *            this is the user that will be authenticated.
	 * @param aPassword
	 *            this is the password used for the authentication.
	 * @throws Exception
	 *             if the authentication fails.
	 */
	public void authenticate(String aUserName, String aPassword)
			throws Exception;

	/**
	 * Closes any resources opened for the authentication process.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void close() throws Exception;
}
