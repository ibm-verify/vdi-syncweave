/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.authentication;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

/**
 * This is the object used by the authentication mechanism for communicating
 * with the authentication engine and the authentication requester.
 */
public class AuthenticationCallbackHandler implements CallbackHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * User name.
	 */
	private String username;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * Class constructor.
	 * @param username
	 * @param password
	 */
	AuthenticationCallbackHandler(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Called by the authentication engine.
	 * 
	 * @param cb
	 *            the authentication transport object.
	 */
	public void handle(Callback[] cb) {
		for (int i = 0; i < cb.length; i++) {
			if (cb[i] instanceof NameCallback) {
				NameCallback nc = (NameCallback) cb[i];
				nc.setName(username);
			} else if (cb[i] instanceof PasswordCallback) {
				PasswordCallback pc = (PasswordCallback) cb[i];
				pc.setPassword(password.toCharArray());
				password = null;
			}
		}
	}
}
