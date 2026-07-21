/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import com.ibm.di.api.exceptions.AuthenticationException;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class is used to execute a custom, user-defined script that will take
 * care for authentication of the users.<br>
 * The user-defined script file path is taken from the Java property with the
 * key name {@link APIEngine#PROP_API_CUSTOM_AUTH}
 * 
 */
public class APIAuthenticator {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Custom authentication flag.
	 */
	private boolean mCustomAuthenticationOn = false;

	/**
	 * Custom authentication script.
	 */
	private String mCustomAuthenticationScript = "";

	/**
	 * Script engine wrapper.
	 */
	private ScriptEngine mScriptEngine = null;

	/**
	 * Used to synchronize threads.
	 */
	private Object mEngineLock = new Object();
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * 
	 * This constructor creates an instance of the {@link APIAuthenticator}
	 * class.
	 * 
	 * @param aCustomScriptEnabled
	 *            if this is false a DIException will be thrown because this
	 *            class is specialized in custom script authentication and this
	 *            means that the custom script interpretation should be enabled.
	 * @param aScript
	 *            the script to interpret in order to authenticate the users.
	 * @throws DIException
	 *             if the custom script authentication is disabled
	 */
	public APIAuthenticator(boolean aCustomScriptEnabled, String aScript)
			throws DIException {
		mCustomAuthenticationOn = aCustomScriptEnabled;
		mCustomAuthenticationScript = aScript;
		if (mCustomAuthenticationOn) {
			try {
				mScriptEngine = new ScriptEngine("javascript", RS.getServer());
			} catch (Exception e) {
				APIEngine
						.logErrorAndThrowException(
								sResHash
										.getString("SEVER.API.UNABLE.TO.INITIALIZE.SCRIPT.ENGINE"),
								e);
			}
		}
	}

	/**
	 * Performs a user authentication using the provided user credentials.
	 * 
	 * @param aUserName
	 *            the user name used for the authentication.
	 * @param aPassword
	 *            the user password used for the authentication.
	 * @throws DIException
	 *             if the custom script authentication is disabled.
	 * @throws AuthenticationException
	 *             if the user cannot be authenticated.
	 */
	public void performCustomScriptAuthentication(String aUserName,
			String aPassword) throws DIException {
		if (!mCustomAuthenticationOn) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.CUSTOM.AUTHENTICATION.IS.DISABLED"));
		}

		UserData userdata = new UserData();
		userdata.username = aUserName;
		userdata.password = aPassword;
		Ret ret = new Ret();
		try {
			synchronized (mEngineLock) {
				mScriptEngine.declareBean("userdata", userdata);
				mScriptEngine.declareBean("ret", ret);
				mScriptEngine.declareStaticBean("main", RS.gRS,
						com.ibm.di.server.RSInterface.class);
				mScriptEngine.exec(mCustomAuthenticationScript);
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.SCRIPT.ERROR.1"), e);
		}
		if (!ret.auth) {
			String funcmsg = sResHash.getString(
					"SEVER.API.AUTHENTICATION.FAILED.FOR.USER", aUserName);
			if (ret.errordescr != null && ret.errordescr.trim().length() > 0) {
				APIEngine.logError(funcmsg + ret.errordescr);
			} else {
				APIEngine.logError(funcmsg);
			}
			throw new AuthenticationException(funcmsg, ret.errordescr,
					ret.errorcode);
		}
	}

	/**
	 * This class is a holder for the user credentials. It is exposed as a
	 * script bean by the name "userdata". Its public member variables
	 * "username" and "password" could be used in the script by the custom
	 * authentication process.
	 */
	static public class UserData {

		/**
		 * Copyright.
		 */
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * The name of the user to authenticate.
		 */
		public String username = "";

		/**
		 * The password to use for the authenticating the user.
		 */
		public String password = "";
	}

	/**
	 * This class holds the status of the authentication. It is exposed as a
	 * script bean by the name "ret". Its public member variables "auth",
	 * "errordescr" and "errorcode" could be used in the script by the custom
	 * authentication process.
	 * 
	 */
	static public class Ret {

		/**
		 * Copyright.
		 */
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * If this is set to true, it is considered that the provided by the
		 * user credentials are authentic. If this is false then the
		 * authentication has failed.
		 */
		public boolean auth = false;

		/**
		 * The description of the error which will be returned with the thrown
		 * {@link AuthenticationException}.
		 */
		public String errordescr = "";

		/**
		 * A general purpose object which string representation will be returned
		 * with the thrown {@link AuthenticationException}.
		 */
		public Object errorcode = null;
	}
}
