/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote.impl.rmi;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

//import com.ibm.di.api.APIEngine; // Incorrect imports. Cause class not found exception.
//import com.ibm.di.server.ResourceHash; // Defect # 14183

/**
 * 
 * This class implements <code>CallbackHandler</code> interface handling
 * behaviour but without doing any prompting.
 * <p>
 * The constructor with no parameters is overridden by <code>NullPrompter</code>
 * class to hide the constructor with no parameters, since we are not prompting. *
 */
public class NullPrompter implements
		javax.security.auth.callback.CallbackHandler {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	// private final static ResourceHash sResHash = APIEngine.getResHash();// Defect # 14183
	/**
	 * username
	 */
	private String userName;

	/**
	 * password
	 */
	private char[] authenticator;

	/**
	 * default constructor. Made private so the system does not generate one.
	 */
	private NullPrompter() {
	}

	/**
	 * Constructor with two parameters.
	 * 
	 * @param userName
	 * @param authenticator
	 * 
	 */
	public NullPrompter(String userName, char authenticator[]) {
		this.userName = userName;
		this.authenticator = authenticator;
	}

	/**
	 * Clears the user name and authenticator.
	 */
	public void nukeEm() {
		this.userName = null;
		for (int i = 0; i < authenticator.length; i++)
			authenticator[i] = ' ';
	}

	/**
	 * Handles callbacks passed from the underlying security services.
	 * 
	 * @param callbacks
	 *            an array of <code>Callback</code> objects provided by an
	 *            underlying security service which contains the information
	 *            requested to be retrieved or displayed
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws UnsupportedCallbackException
	 *             if unrecognized callback occurs
	 */
	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof TextOutputCallback) {
				continue;
			} else if (callbacks[i] instanceof TextInputCallback) {
				((TextInputCallback) callbacks[i]).setText(userName);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(authenticator);
			} else {// Message SERVER.API.REMOTE.RMI.UNRECOGNIZED.CALLBACK
				throw new UnsupportedCallbackException(
						callbacks[i], "Unrecognised Callback.");
			}
		}
	}
}
